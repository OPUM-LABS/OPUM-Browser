package com.mycompany.app.ui.settings

import com.mycompany.app.ui.components.AppListItem
import android.content.Context
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.BrowserSettings
import android.content.Intent
import android.database.Cursor
import android.text.TextUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookHistory
import com.mycompany.app.db.book.DbBookWeb
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingsScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Editor for the home page (type 1), the start page (type 2) and the new-tab page (type 3): a URL field, three quick
 * choices and the bookmarks / history to pick from. The result extra `EXTRA_TYPE` tells the settings page what changed.
 */
class HomeSettingsActivity : OpumComposeActivity() {
    private val shortcutPage = "file:///android_asset/shortcut.html"

    private class Entry(val title: String?, val url: String?, val dir: Boolean)

    @Composable
    override fun Content() {
        val context: Context = this
        val type = remember { intent.getIntExtra("EXTRA_TYPE", 1) }
        val current = remember { intent.getStringExtra("EXTRA_PATH") }
        var url by remember {
            mutableStateOf(
                run {
                    var value = when (type) { 1 -> BrowserSettings.homePage.value; 2 -> BrowserSettings.startPage.value; else -> BrowserSettings.tabPage.value }
                    if ("..home_link" == value) value = BrowserSettings.homePage.value
                    if (!TextUtils.isEmpty(value)) value = value!!.trim()
                    if (type != 2 && TextUtils.isEmpty(value)) value = shortcutPage
                    value ?: ""
                }
            )
        }
        var tab by remember { mutableIntStateOf(0) }
        var dir by remember { mutableStateOf("/") }
        var entries by remember { mutableStateOf<List<Entry>>(emptyList()) }
        var loaded by remember { mutableStateOf(false) }
        var menu by remember { mutableStateOf(false) }
        val secret = SyncSettings.secretMode.value

        LaunchedEffect(tab, dir) {
            loaded = false
            entries = withContext(Dispatchers.IO) { if (tab == 0) loadBookmarks(context, dir, secret) else loadHistory(context, secret) }
            loaded = true
        }

        fun save() {
            val trimmed = url.trim()
            if (TextUtils.isEmpty(trimmed)) {
                MainUtil.showToast(context, R.string.input_url)
                return
            }
            val changed = when (type) {
                1 -> if (trimmed != BrowserSettings.homePage.value) { BrowserSettings.edit(context) { set(BrowserSettings.homePage, trimmed) }; true } else false
                2 -> if (trimmed != BrowserSettings.startPage.value) { BrowserSettings.edit(context) { set(BrowserSettings.startPage, trimmed) }; true } else false
                else -> if (trimmed != BrowserSettings.tabPage.value) { BrowserSettings.edit(context) { set(BrowserSettings.tabPage, trimmed) }; true } else false
            }
            if (changed) setResult(RESULT_OK, Intent().putExtra("EXTRA_TYPE", type))
            finish()
        }

        SettingsScaffold(
            title = stringResource(when (type) { 1 -> R.string.home_page; 2 -> R.string.start_page; else -> R.string.new_url }),
            onBack = { finish() },
            actions = {
                IconButton(onClick = { save() }) { Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.ok)) }
            },
        ) {
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = { Text("URL") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { save() }),
                        trailingIcon = {
                            if (url.isNotEmpty()) {
                                IconButton(onClick = { url = "" }) { Icon(Icons.Filled.Clear, contentDescription = null) }
                            } else {
                                IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, contentDescription = null) }
                                com.mycompany.app.ui.components.BubbleMenu(expanded = menu, onDismiss = { menu = false }) {
                                    for (prefix in listOf("https://", "www.")) {
                                        DropdownMenuItem(text = { Text(prefix) }, onClick = { url = prefix; menu = false })
                                    }
                                }
                            }
                        },
                    )
                    Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = { url = shortcutPage }, label = { Text(stringResource(R.string.soul_home)) })
                        AssistChip(onClick = { url = current ?: "" }, label = { Text(stringResource(R.string.current_page)) })
                        AssistChip(onClick = { url = "about:blank" }, label = { Text(stringResource(R.string.blank_page)) })
                    }
                    SecondaryTabRow(selectedTabIndex = tab, containerColor = TabRowDefaults.primaryContainerColor, contentColor = TabRowDefaults.primaryContentColor, modifier = Modifier.padding(top = 8.dp)) {
                        Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.bookmark)) })
                        Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(R.string.history)) })
                    }
                }
            }
            if (tab == 0 && dir != "/") {
                item {
                    AppListItem(
                        modifier = Modifier.clickable { dir = dir.substringBeforeLast('/').ifEmpty { "/" } },
                        headlineContent = { Text("..") },
                    )
                }
            }
            items(entries) { entry ->
                AppListItem(
                    modifier = Modifier.clickable {
                        if (entry.dir) dir = if (dir == "/") "/" + entry.title else dir + "/" + entry.title
                        else url = entry.url ?: ""
                    },
                    headlineContent = { Text(entry.title ?: entry.url ?: "", maxLines = 1) },
                    supportingContent = if (entry.dir) null else ({ Text(entry.url ?: "", maxLines = 1) }),
                )
            }
            if (loaded && entries.isEmpty()) {
                // the shared empty state (2026-11-20): the tab showed an empty area
                item {
                    com.mycompany.app.ui.components.EmptyState(
                        icon = if (tab == 0) R.drawable.outline_kid_star_black_24 else R.drawable.outline_history_black_24,
                        title = stringResource(if (tab == 0) R.string.no_bookmark else R.string.no_history),
                        modifier = Modifier.padding(top = 32.dp),
                    )
                }
            }
        }
    }

    private fun loadBookmarks(context: Context, dir: String, secret: Boolean): List<Entry> {
        val list = ArrayList<Entry>()
        var cursor: Cursor? = null
        try {
            cursor = DbUtil.query(
                DbBookWeb.get(context).getWritableDatabase(), "DbBookWeb_table",
                arrayOf<String?>("_isdir", "_path", "_title"), "_secret=? AND _dir=?",
                arrayOf<String?>(if (secret) "1" else "0", dir), null,
            )
            if (cursor != null && cursor.moveToFirst()) {
                val isDir = cursor.getColumnIndex("_isdir")
                val path = cursor.getColumnIndex("_path")
                val title = cursor.getColumnIndex("_title")
                do {
                    val folder = cursor.getInt(isDir) == 1
                    val url = cursor.getString(path)
                    if (!folder && TextUtils.isEmpty(url)) continue
                    list.add(Entry(cursor.getString(title), url, folder))
                } while (cursor.moveToNext())
            }
        } catch (_: Exception) {
        }
        cursor?.close()
        return list.sortedWith(compareByDescending<Entry> { it.dir }.thenBy { (it.title ?: "").lowercase() })
    }

    private fun loadHistory(context: Context, secret: Boolean): List<Entry> {
        if (secret && SecretSettings.secretHist.value == 0L) return emptyList() // history is not saved in secret mode
        var since = if (BrowserSettings.historyTime.value == -1) 0L else System.currentTimeMillis() - BrowserSettings.historyTime.value * 86400000L
        if (BrowserSettings.historyTime.value != -1 && secret && SecretSettings.secretHist.value > since) since = SecretSettings.secretHist.value
        val list = ArrayList<Entry>()
        var cursor: Cursor? = null
        try {
            cursor = DbUtil.query(
                DbBookHistory.get(context).getWritableDatabase(), "DbBookHistory_table",
                arrayOf<String?>("_path", "_title"), "_secret=? AND _time>?",
                arrayOf<String?>(if (secret) "1" else "0", since.toString()), "_time DESC",
            )
            if (cursor != null && cursor.moveToFirst()) {
                val path = cursor.getColumnIndex("_path")
                val title = cursor.getColumnIndex("_title")
                do {
                    list.add(Entry(cursor.getString(title), cursor.getString(path), false))
                } while (list.size < 100 && cursor.moveToNext())
            }
        } catch (_: Exception) {
        }
        cursor?.close()
        return list
    }
}
