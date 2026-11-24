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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
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

/** One saved login. */
private class PassEntry(var id: Long, val host: String, var user: String?, var pass: String?, val url: String?)

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
                                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete_all))
                            }
                        }
                        Box {
                            IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, contentDescription = null) }
                            com.mycompany.app.ui.components.BubbleMenu(expanded = menu, onDismiss = { menu = false }) {
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
                    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp + padding.calculateBottomPadding())) {
                        items(list, key = { it.id }) { entry ->
                            AppListItem(
                                headlineContent = { Text(entry.host, maxLines = 1) },
                                supportingContent = { Text(entry.user ?: "", maxLines = 1) },
                                trailingContent = {
                                    IconButton(onClick = { deleting = entry }) {
                                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
                                    }
                                },
                                modifier = Modifier.clickable { showInfo(entry) },
                            )
                        }
                    }
                }
            }
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
                        arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.dataKey()),
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
                "_secret=?", arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.keyFor(secret)), "_path ASC",
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
                    val pass = cursor.getString(colPass)
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
                    result.add(PassEntry(id, host, user, pass, url))
                } while (cursor.moveToNext())
            }
        } catch (_: Exception) {
        } finally {
            cursor?.close()
        }
        return result
    }
}
