package com.mycompany.app.ui.settings

import com.mycompany.app.ui.components.AppListItem
import android.app.Activity
import com.mycompany.app.pref.store.SyncSettings
import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookPass
import com.mycompany.app.dialog.DialogPassInfo
import com.mycompany.app.main.BitmapUtil
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingsTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.mycompany.app.ui.components.Sym

/** One saved login. */
private class PassEntry(var id: Long, val host: String, var user: String?, var pass: String?, val url: String?, val icon: Bitmap? = null)

/**
 * Saved passwords: list (sorted by site), tap a row to view/edit it, trash icon to delete one, import and export as
 * CSV from the menu (Chrome / Firefox format, [rememberBrowserTransfer]). In incognito mode the
 * normal passwords are copied into the secret store while loading, as the old screen did.
 */
class PassListSettingsActivity : OpumComposeActivity() {

    private var changed = false

    override fun finish() {
        if (changed) setResult(Activity.RESULT_OK)
        super.finish()
    }

    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context: Context = this
        var entries by remember { mutableStateOf<List<PassEntry>?>(null) }
        var reloadKey by remember { mutableIntStateOf(0) }
        var menu by remember { mutableStateOf(false) }
        var deleting by remember { mutableStateOf<PassEntry?>(null) }
        var deletingAll by remember { mutableStateOf(false) }
        var query by remember { mutableStateOf("") }
        var adding by remember { mutableStateOf(false) }
        var checking by remember { mutableStateOf(false) }

        LaunchedEffect(reloadKey) {
            val list = withContext(Dispatchers.IO) { loadEntries(context) }
            entries = list
            changed = changed || list.isNotEmpty()
        }

        // Import and export go through the Chrome / Firefox CSV code shared with Backup & restore, so the export
        // here gets the same unencrypted-file warning and lock check.
        val transfer = rememberBrowserTransfer(onChanged = { reloadKey++ })

        fun showInfo(entry: PassEntry) {
            DialogPassInfo(
                this, entry.id, entry.host, entry.user, entry.pass, entry.url,
                object : DialogPassInfo.PassInfoListener {
                    override fun onSaved(name: String?, password: String?) {
                        MainUtil.showToast(context, R.string.save_success)
                        if (!TextUtils.isEmpty(name)) { reloadKey++ }
                    }
                    override fun onDelete(name: String?) {}
                    override val icon: Bitmap? get() = null
                },
            )
        }

        val list = entries
        Scaffold(
            topBar = {
                SettingsTopBar(
                    title = stringResource(R.string.password) + " " + stringResource(R.string.list),
                    onBack = { finish() },
                    actions = {
                        if (!list.isNullOrEmpty()) {
                            IconButton(onClick = { deletingAll = true }) {
                                Icon(Sym.Delete, contentDescription = stringResource(R.string.delete_all))
                            }
                        }
                        Box {
                            IconButton(onClick = { menu = true }) { Icon(Sym.MoreVert, contentDescription = null) }
                            com.mycompany.app.ui.components.BubbleMenu(expanded = menu, onDismiss = { menu = false }) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.pass_check)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_vpn_key_black_24) },
                                    onClick = { menu = false; checking = true },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.import_csv)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_download_black_20) },
                                    onClick = { menu = false; transfer.importPasswords() },
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.export_csv)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_upload_black_24) },
                                    onClick = { menu = false; transfer.exportPasswords() },
                                )
                            }
                        }
                    },
                )
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
                if (list != null && list.isEmpty()) {
                    com.mycompany.app.ui.components.EmptyState(icon = R.drawable.outline_lock_black_24, title = stringResource(R.string.no_password))
                } else if (list != null) {
                    // grouped by site (several accounts of one site under one name), filtered by the search
                    val needle = query.trim()
                    val groups = list.filter { needle.isEmpty() || it.host.contains(needle, true) || (it.user ?: "").contains(needle, true) }
                        .groupBy { it.host }.toSortedMap(String.CASE_INSENSITIVE_ORDER)
                    LazyColumn(contentPadding = PaddingValues(bottom = 88.dp + padding.calculateBottomPadding())) {
                        item(key = "search") {
                            androidx.compose.material3.OutlinedTextField(
                                value = query, onValueChange = { query = it }, singleLine = true,
                                leadingIcon = { Icon(Sym.Search, null) },
                                placeholder = { Text(stringResource(R.string.pass_search)) },
                                modifier = androidx.compose.ui.Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                        for ((host, accounts) in groups) {
                            item(key = "h$host") {
                                androidx.compose.foundation.layout.Row(
                                    Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    val icon = accounts.firstNotNullOfOrNull { it.icon }
                                    val shape = androidx.compose.foundation.shape.CircleShape
                                    if (icon != null) {
                                        androidx.compose.foundation.Image(icon.asImageBitmap(), null, Modifier.size(24.dp).clip(shape))
                                    } else {
                                        Icon(androidx.compose.ui.res.painterResource(R.drawable.outline_public_black_24), null, Modifier.size(24.dp), tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(host, style = androidx.compose.material3.MaterialTheme.typography.titleSmall, maxLines = 1, modifier = Modifier.padding(start = 12.dp))
                                }
                            }
                            items(accounts, key = { it.id }) { entry ->
                                val unreadable = entry.pass == null
                                AppListItem(
                                    headlineContent = { Text(entry.user ?: "", maxLines = 1) },
                                    supportingContent = if (unreadable) {
                                        { Text(stringResource(R.string.pass_unreadable), color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
                                    } else null,
                                    trailingContent = {
                                        androidx.compose.foundation.layout.Row {
                                            if (!unreadable) {
                                                IconButton(onClick = { com.mycompany.app.main.SecretClip.copyPassword(context, entry.pass) }) {
                                                    Icon(androidx.compose.ui.res.painterResource(R.drawable.outline_content_copy_black_24), contentDescription = stringResource(R.string.pass_copy))
                                                }
                                            }
                                            IconButton(onClick = { deleting = entry }) {
                                                Icon(Sym.Delete, contentDescription = stringResource(R.string.delete))
                                            }
                                        }
                                    },
                                    modifier = Modifier.clickable(enabled = !unreadable) { showInfo(entry) }.padding(start = 20.dp),
                                )
                            }
                        }
                    }
                }
                // a login added by hand
                androidx.compose.material3.ExtendedFloatingActionButton(
                    onClick = { adding = true },
                    icon = { Icon(Sym.Add, null) },
                    text = { Text(stringResource(R.string.pass_add)) },
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp + padding.calculateBottomPadding()),
                )
            }
        }

        if (checking) {
            PassCheckDialog(entries.orEmpty().filter { it.pass != null }, onDismiss = { checking = false })
        }
        if (adding) {
            AddLoginDialog(
                onAdd = { site, user, pass ->
                    val url = if (site.contains("://")) site else "https://$site"
                    val host = MainUtil.hostOfUrl(url, true)?.lowercase()
                    if (host.isNullOrEmpty()) false else {
                        DbBookPass.insertItem(context, host, androidx.core.graphics.createBitmap(1, 1), user, pass, url)
                        reloadKey++
                        true
                    }
                },
                onDismiss = { adding = false },
            )
        }

        deleting?.let { entry ->
            ConfirmDialog(
                message = entry.host,
                title = stringResource(R.string.delete),
                onConfirm = {
                    DbUtil.deleteById(DbBookPass.get(context).writableDatabase, "DbBookPass_table", entry.id)
                    reloadKey++
                },
                onDismiss = { deleting = null },
            )
        }
        if (deletingAll) {
            ConfirmDialog(
                message = stringResource(R.string.delete_all),
                onConfirm = {
                    DbUtil.deleteWhere(
                        DbBookPass.get(context).writableDatabase,
                        "DbBookPass_table",
                        "_secret=?",
                        arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey()),
                    )
                    reloadKey++
                },
                onDismiss = { deletingAll = false },
            )
        }
    }

    /** Saved logins of the current mode; in incognito the normal ones are first copied into the secret store. */
    private fun loadEntries(context: Context): List<PassEntry> {
        val result = ArrayList<PassEntry>()
        val secret = SyncSettings.secretMode.value
        var cursor: android.database.Cursor? = null
        try {
            cursor = DbUtil.query(
                DbBookPass.get(context).writableDatabase, "DbBookPass_table", null,
                "_secret=?", arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey()), "_path ASC",
            )
            if (cursor != null && cursor.moveToFirst()) {
                val colId = cursor.getColumnIndex("_id")
                val colPath = cursor.getColumnIndex("_path")
                val colIcon = cursor.getColumnIndex("_icon")
                val colUser = cursor.getColumnIndex("_user_val")
                val colPass = cursor.getColumnIndex("_pass_val")
                val colUrl = cursor.getColumnIndex("_rsv1")
                do {
                    val host = cursor.getString(colPath) ?: continue
                    val user = cursor.getString(colUser)
                    // opened (sealed with this phone's key, see PassCrypt); null: cannot be read here
                    val pass = com.mycompany.app.db.book.PassCrypt.open(cursor.getString(colPass))
                    var url = cursor.getString(colUrl)
                    if (TextUtils.isEmpty(url)) url = "https://$user"
                    var id = cursor.getLong(colId)
                    if (SyncSettings.secretMode.value && !secret) {
                        val key = MainUtil.shortHostOf(host)
                        var icon = MainUtil.cachedIcon(key)
                        if (!MainUtil.isBitmapOk(icon)) {
                            val blob = cursor.getBlob(colIcon)
                            if (blob != null && blob.isNotEmpty()) {
                                icon = BitmapUtil.decodeBytes(blob, blob.size)
                                if (MainUtil.isBitmapOk(icon)) MainUtil.cacheIcon(key, icon)
                            }
                        }
                        id = DbBookPass.insertItem(context, host, icon!!, user, pass, url)
                        if (id == 0L) continue
                    }
                    val shownIcon = MainUtil.cachedIcon(MainUtil.shortHostOf(host)) ?: try {
                        cursor.getBlob(colIcon)?.takeIf { it.size > 8 }?.let { BitmapUtil.decodeBytes(it, it.size) }
                    } catch (_: Exception) { null }
                    result.add(PassEntry(id, host, user, pass, url, shownIcon?.takeIf { MainUtil.isBitmapOk(it) }))
                } while (cursor.moveToNext())
            }
        } catch (_: Exception) {
        } finally {
            cursor?.close()
        }
        return result
    }
}

/** Adds a login by hand: site, name and password (the keyboard's Next key moves on, Done adds). */
@Composable
private fun AddLoginDialog(onAdd: (String, String, String) -> Boolean, onDismiss: () -> Unit) {
    var site by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var siteError by remember { mutableStateOf(false) }
    val userFocus = remember { androidx.compose.ui.focus.FocusRequester() }
    val passFocus = remember { androidx.compose.ui.focus.FocusRequester() }
    val ready = site.isNotBlank() && user.isNotBlank() && pass.isNotEmpty()
    fun add() { if (ready) { if (onAdd(site.trim(), user.trim(), pass)) onDismiss() else siteError = true } }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pass_add)) },
        text = {
            Column {
                androidx.compose.material3.OutlinedTextField(
                    value = site, onValueChange = { site = it; siteError = false }, singleLine = true, isError = siteError,
                    label = { Text(stringResource(R.string.site)) }, placeholder = { Text("example.com") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Uri, imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onNext = { userFocus.requestFocus() }),
                    modifier = Modifier.fillMaxWidth(),
                )
                androidx.compose.material3.OutlinedTextField(
                    value = user, onValueChange = { user = it }, singleLine = true, label = { Text(stringResource(R.string.name)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Email, imeAction = androidx.compose.ui.text.input.ImeAction.Next),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onNext = { passFocus.requestFocus() }),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).focusRequester(userFocus),
                )
                com.mycompany.app.ui.components.AppPasswordField(
                    value = pass, onValueChange = { pass = it }, label = { Text(stringResource(R.string.password)) },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password, imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                    onDone = { add() },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).focusRequester(passFocus),
                )
            }
        },
        confirmButton = { androidx.compose.material3.TextButton(onClick = { add() }, enabled = ready) { Text(stringResource(R.string.add)) } },
        dismissButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

/**
 * The password check (2026-11-28), on the phone only, nothing is sent anywhere: passwords used for more than one
 * login, and weak ones (shorter than 10 characters, fewer than three kinds of characters, or a well-known one).
 */
@Composable
private fun PassCheckDialog(entries: List<PassEntry>, onDismiss: () -> Unit) {
    val reused = entries.groupBy { it.pass }.values.filter { it.size > 1 }.flatten()
    val weak = entries.filter { isWeak(it.pass ?: "") }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.pass_check)) },
        text = {
            androidx.compose.foundation.lazy.LazyColumn {
                if (reused.isEmpty() && weak.isEmpty()) {
                    item { Text(stringResource(R.string.pass_check_ok)) }
                }
                if (reused.isNotEmpty()) {
                    item { Text(stringResource(R.string.pass_check_reused, reused.size), style = androidx.compose.material3.MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 4.dp)) }
                    items(reused.size) { i -> CheckLine(reused[i]) }
                }
                if (weak.isNotEmpty()) {
                    item { Text(stringResource(R.string.pass_check_weak, weak.size), style = androidx.compose.material3.MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) }
                    items(weak.size) { i -> CheckLine(weak[i]) }
                }
                item { Text(stringResource(R.string.pass_check_note), style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 12.dp)) }
            }
        },
        confirmButton = { androidx.compose.material3.TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok)) } },
    )
}

@Composable
private fun CheckLine(entry: PassEntry) {
    Column(Modifier.padding(vertical = 4.dp)) {
        Text(entry.host, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
        Text(entry.user ?: "", style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private val COMMON = setOf("password", "passwort", "123456", "12345678", "123456789", "1234567890", "qwerty", "qwertz", "azerty", "111111", "abc123", "iloveyou", "admin", "welcome", "letmein", "monkey", "dragon", "sunshine", "football", "000000")

private fun isWeak(password: String): Boolean {
    if (password.lowercase().trimEnd('!', '1', '.') in COMMON || password.lowercase() in COMMON) return true
    val kinds = listOf(password.any { it.isLowerCase() }, password.any { it.isUpperCase() }, password.any { it.isDigit() }, password.any { !it.isLetterOrDigit() }).count { it }
    return password.length < 10 || kinds < 3
}
