package com.mycompany.app.ui.lists

import com.mycompany.app.ui.components.AppListItem
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.URLUtil
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import com.mycompany.app.ui.components.TickSwitch as Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.data.book.DataBookFilter
import com.mycompany.app.data.book.DataBookScript
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookFilter
import com.mycompany.app.db.book.DbBookScript
import com.mycompany.app.db.book.DbBookUser
import com.mycompany.app.dialog.DialogDeleteBook
import com.mycompany.app.dialog.DialogDeleteBook.DeleteBookListener
import com.mycompany.app.dialog.DialogEditScript
import com.mycompany.app.dialog.DialogEditUrl
import com.mycompany.app.dialog.DialogEditUrl.EditUrlListener
import com.mycompany.app.dialog.DialogInfo
import com.mycompany.app.ui.components.AutoUpdateDialog
import com.mycompany.app.dialog.DialogSetFilter
import com.mycompany.app.dialog.DialogSetFilter.SetFilterListener
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.dialog.DialogSetSort
import com.mycompany.app.dialog.DialogUpdateFilter
import com.mycompany.app.dialog.DialogUpdateScript
import com.mycompany.app.list.ListTask
import com.mycompany.app.list.ListTask.ListTaskConfig
import com.mycompany.app.list.ListTask.ListTaskSimpleListener
import com.mycompany.app.list.book.ListBookFilter
import com.mycompany.app.list.book.ListBookScript
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingsTopBar
import com.mycompany.app.ui.components.Sym

/**
 * The ad-block filter lists (`EXTRA_TYPE` 24) and the UserScripts (26). Rows come from the old loaders
 * ([ListBookFilter], [ListBookScript]) so the stored entries, sorting and the browser's caches are unchanged. The
 * switch turns an entry on or off; tapping opens it (a filter's text file, the user filter rules, or the script
 * editor). The entry menu has update, delete, edit, share and details; the top bar adds new entries, sorts, updates
 * everything that is turned on and opens the auto-update setting. Replaces the type 24 / 26 pages of the old list
 * dialog; the editors that still need the old browser activity run through `DialogShow.direct`.
 */
class FilterScriptListActivity : OpumComposeActivity() {

    companion object {
        const val EXTRA_TYPE = "EXTRA_TYPE"
        private const val USER_FILTER = "sb_user_filter_path"
        private const val FILTER_HELP = "https://help.adblockplus.org/hc/en-us/articles/360062733293-How-to-write-filters#basic"
        /** Filter syntax help while the uBlock Origin-compatible engine is in use. */
        private const val UBO_FILTER_HELP = "https://github.com/gorhill/uBlock/wiki/Static-filter-syntax"
    }

    private var type = 24
    private var task: ListTask? = null
    private var itemsState by mutableStateOf<List<ChildItem>>(emptyList())
    private var loaded by mutableStateOf(false)
    /** Rule counts per list path (uBlock Origin engine only), filled in the background. */
    private val listInfos = mutableStateMapOf<String, com.mycompany.app.web.filter.UboFilter.ListInfo>()
    private val uboOn: Boolean get() = type == 24

    private val listener = object : ListTaskSimpleListener() {
        override fun onConfig(listTaskConfig: ListTaskConfig?) {
            val list = listTaskConfig?.items?.filterNotNull() ?: emptyList()
            runOnUiThread {
                itemsState = list
                loaded = true
                loadListInfos(list)
            }
        }
    }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        type = if (intent.getIntExtra(EXTRA_TYPE, 24) == 26) 26 else 24
        super.onCreate(savedInstanceState)
        reload()
    }

    override fun onDestroy() {
        task?.cancel()
        super.onDestroy()
    }

    private fun reload() {
        task?.cancel()
        val newTask: ListTask = if (type == 26) {
            ListBookScript().also { it.appContext = this; it.taskListener = listener }
        } else {
            ListBookFilter().also { it.appContext = this; it.taskListener = listener }
        }
        task = newTask
        newTask.load(false, false, false)
    }

    /** Counts the rules of every list in the background (cached by file version in `UboFilter`). */
    private fun loadListInfos(list: List<ChildItem>) {
        if (!uboOn) return
        val paths = list.mapNotNull { it.path }
        val app = applicationContext
        Thread({
            for (p in paths) {
                if (isFinishing || isDestroyed) return@Thread
                val info = com.mycompany.app.web.filter.UboFilter.listInfo(app, p) ?: continue
                runOnUiThread { listInfos[p] = info }
            }
        }, "UboListInfo").apply { priority = Thread.MIN_PRIORITY + 2 }.start()
    }

    private fun listInfoText(info: com.mycompany.app.web.filter.UboFilter.ListInfo): String {
        val parts = ArrayList<String>()
        parts.add(resources.getQuantityString(R.plurals.ubo_list_rules, info.rules, java.text.NumberFormat.getIntegerInstance().format(info.rules)))
        if (info.rejected > 0) {
            parts.add(getString(R.string.ubo_list_rejected, java.text.NumberFormat.getIntegerInstance().format(info.rejected)))
        }
        val h = info.expiresHours
        if (h != null && h > 0) {
            parts.add(
                if (h % 24 == 0) resources.getQuantityString(R.plurals.ubo_list_expires_days, h / 24, h / 24)
                else resources.getQuantityString(R.plurals.ubo_list_expires_hours, h, h),
            )
        }
        return parts.joinToString(" · ")
    }

    private fun legacy(requestCodes: IntArray = IntArray(0), factory: (android.app.Activity) -> android.app.Dialog?) {
        com.mycompany.app.ui.prefs.DialogShow.direct(this as android.app.Activity, requestCodes = requestCodes, onDismiss = { reload() }, factory = factory)
    }

    private fun clearCaches() {
        if (type == 26) DataBookScript.instance(this).clear() else DataBookFilter.instance(this).clear()
    }

    private fun setEnabled(item: ChildItem, on: Boolean) {
        item.isEnabled = on
        val context: Context = this
        Thread {
            try {
                if (type == 26) {
                    val name = item.name
                    val space = item.namespace
                    if (!name.isNullOrEmpty() && !space.isNullOrEmpty()) {
                        val values = ContentValues()
                        values.put("_enabled", if (on) 1 else 0)
                        DbUtil.updateWhere(DbBookScript.get(context).writableDatabase, "DbBookScript_table", values, "_name=? AND _namespace=?", arrayOf(name, space))
                    }
                } else {
                    val values = ContentValues()
                    values.put("_use", if (on) 1 else 0)
                    DbUtil.updateWhere(DbBookFilter.get(context).writableDatabase, "DbBookFilter_table", values, "_id=?", arrayOf(item.id.toString()))
                }
            } catch (_: Exception) {
            }
            clearCaches()
        }.start()
    }

    private fun deleteItem(item: ChildItem) {
        var dialog: DialogDeleteBook? = null
        dialog = DialogDeleteBook(
            this, type, ArrayList<Any?>(listOf(item)), null, false, true,
            object : DeleteBookListener {
                override fun onCancelled() {}
                override fun onDeleted() {
                    reload()
                    dialog?.dismiss()
                }
            },
        )
    }

    private fun updateItems(list: MutableList<Any?>?, item: ChildItem?, url: String?, title: String?, id: Long) {
        if (type == 26) {
            var dialog: DialogUpdateScript? = null
            dialog = DialogUpdateScript(this, list, item, url, object : EditUrlListener {
                override fun onSaved(id: Long, path: String?, extra: String?) { reload(); dialog?.dismiss() }
            })
        } else {
            var dialog: DialogUpdateFilter? = null
            dialog = DialogUpdateFilter(this, list, url, title, object : DialogApplyListener {
                override fun onApply() { reload(); dialog?.dismiss() }
            })
        }
    }

    private fun updateEnabled() {
        val enabled = ArrayList<Any?>(itemsState.filter { it.rowType.toInt() == 0 && it.isEnabled })
        if (enabled.isEmpty()) return
        updateItems(enabled, null, null, null, -1L)
    }

    private fun addFromUrl() {
        var dialog: DialogEditUrl? = null
        val blank = ChildItem().also { it.id = -1L }
        dialog = DialogEditUrl(this, type, blank, object : EditUrlListener {
            override fun onSaved(id: Long, path: String?, extra: String?) {
                dialog?.dismiss()
                if (path.isNullOrEmpty()) { reload(); return }
                updateItems(null, null, path, extra, id)
            }
        })
    }

    private fun openUserFilter() {
        ListReturn.set { reload() }
        startActivity(Intent(this, UserFilterListActivity::class.java))
    }

    private fun open(item: ChildItem) {
        if (type == 26) {
            var dialog: DialogEditScript? = null
            legacy { host ->
                DialogEditScript(host, item, object : EditUrlListener {
                    override fun onSaved(id: Long, path: String?, extra: String?) { dialog?.dismiss() }
                }).also { dialog = it }
            }
            return
        }
        if (item.path == USER_FILTER) { openUserFilter(); return }
        val file = if (URLUtil.isNetworkUrl(item.path)) item.filePath else item.path
        if (!MainUtil.isReadableFile(this, file)) {
            MainUtil.showToast(this, R.string.invalid_file)
            return
        }
        startActivity(Intent(this, com.mycompany.app.ui.viewer.TextViewerActivity::class.java).putExtra("EXTRA_PATH", file))
    }

    private fun share(item: ChildItem) {
        if (type == 26 || URLUtil.isNetworkUrl(item.path)) {
            MainUtil.shareText(this, item.path, item.name)
        } else if (item.path == USER_FILTER) {
            val context: Context = this
            Thread {
                val rules = StringBuilder()
                try {
                    DbUtil.query(DbBookUser.get(context).writableDatabase, "DbBookUser_table", null, null, null, null)?.use { cursor ->
                        val col = cursor.getColumnIndex("_path")
                        while (cursor.moveToNext()) {
                            val rule = cursor.getString(col)
                            if (!rule.isNullOrEmpty()) {
                                if (rules.isNotEmpty()) rules.append('\n')
                                rules.append(rule)
                            }
                        }
                    }
                } catch (_: Exception) {
                }
                runOnUiThread { MainUtil.shareText(this, rules.toString(), item.name) }
            }.start()
        } else {
            MainUtil.shareFile(24, this, item.path, item.name, null)
        }
    }

    private fun edit(item: ChildItem) {
        if (item.path == USER_FILTER) { openUserFilter(); return }
        var dialog: DialogEditUrl? = null
        dialog = DialogEditUrl(this, 24, item, object : EditUrlListener {
            override fun onSaved(id: Long, path: String?, extra: String?) {
                dialog?.dismiss()
                if (path.isNullOrEmpty()) { reload(); return }
                updateItems(null, null, path, extra, id)
            }
        })
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val toggled = remember { mutableStateMapOf<Long, Boolean>() }
        var addMenu by remember { mutableStateOf(false) }
        var moreMenu by remember { mutableStateOf(false) }
        var autoUpdate by remember { mutableStateOf(false) }
        var rowMenu by remember { mutableIntStateOf(-1) }
        var rejectedOf by remember { mutableStateOf<com.mycompany.app.web.filter.UboFilter.ListInfo?>(null) }
        var offerDone by remember { mutableStateOf(com.mycompany.app.pref.store.AlbumSettings.uboOfferDone.value) }
        val missingDefaults = if (uboOn && loaded && !offerDone) DialogSetFilter.missingUboDefaults(itemsState.map { it.path }) else emptyList()

        Scaffold(
            topBar = {
                SettingsTopBar(
                    title = stringResource(if (type == 26) R.string.user_script_title else R.string.ads_filter),
                    onBack = { finish() },
                    actions = {
                        Box {
                            IconButton(onClick = {
                                if (type == 26) addMenu = true else addMenu = true
                            }) { Icon(Sym.Add, contentDescription = stringResource(R.string.add)) }
                            com.mycompany.app.ui.components.BubbleMenu(expanded = addMenu, onDismiss = { addMenu = false }) {
                                if (type == 24) {
                                    DropdownMenuItem(text = { Text(stringResource(R.string.add_from_list)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_add_black_24) }, onClick = {
                                        addMenu = false
                                        var dialog: DialogSetFilter? = null
                                        legacy { host ->
                                            DialogSetFilter(host, false, object : SetFilterListener {
                                                override fun onAdded(arrayList: ArrayList<Any?>?) {
                                                    dialog?.dismiss()
                                                    if (!arrayList.isNullOrEmpty()) runOnUiThread { updateItems(arrayList, null, null, null, -1L) }
                                                }
                                            }).also { dialog = it }
                                        }
                                    })
                                    DropdownMenuItem(text = { Text(stringResource(R.string.add_from_list) + " (uBlock Origin)") }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_add_black_24) }, onClick = {
                                        addMenu = false
                                        var dialog: DialogSetFilter? = null
                                        legacy { host ->
                                            DialogSetFilter(host, DialogSetFilter.CATALOG_UBO, object : SetFilterListener {
                                                override fun onAdded(arrayList: ArrayList<Any?>?) {
                                                    dialog?.dismiss()
                                                    if (!arrayList.isNullOrEmpty()) runOnUiThread { updateItems(arrayList, null, null, null, -1L) }
                                                }
                                            }).also { dialog = it }
                                        }
                                    })
                                    DropdownMenuItem(text = { Text(stringResource(R.string.add_from_list) + " (Brave)") }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_add_black_24) }, onClick = {
                                        addMenu = false
                                        var dialog: DialogSetFilter? = null
                                        legacy { host ->
                                            DialogSetFilter(host, DialogSetFilter.CATALOG_BRAVE, object : SetFilterListener {
                                                override fun onAdded(arrayList: ArrayList<Any?>?) {
                                                    dialog?.dismiss()
                                                    if (!arrayList.isNullOrEmpty()) runOnUiThread { updateItems(arrayList, null, null, null, -1L) }
                                                }
                                            }).also { dialog = it }
                                        }
                                    })
                                } else {
                                    DropdownMenuItem(text = { Text(stringResource(R.string.new_script)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_add_black_24) }, onClick = {
                                        addMenu = false
                                        var dialog: DialogEditScript? = null
                                        legacy { host ->
                                            DialogEditScript(host, null, object : EditUrlListener {
                                                override fun onSaved(id: Long, path: String?, extra: String?) { dialog?.dismiss() }
                                            }).also { dialog = it }
                                        }
                                    })
                                }
                                DropdownMenuItem(text = { Text(stringResource(R.string.add_from_url)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_link_black_24) }, onClick = { addMenu = false; addFromUrl() })
                            }
                        }
                        Box {
                            IconButton(onClick = { moreMenu = true }) { Icon(Sym.MoreVert, contentDescription = null) }
                            com.mycompany.app.ui.components.BubbleMenu(expanded = moreMenu, onDismiss = { moreMenu = false }) {
                                DropdownMenuItem(text = { Text(stringResource(R.string.update)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_refresh_black_20) }, onClick = { moreMenu = false; updateEnabled() })
                                DropdownMenuItem(text = { Text(stringResource(R.string.sort)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_sort_black_24) }, onClick = {
                                    moreMenu = false
                                    var dialog: DialogSetSort? = null
                                    legacy { host ->
                                        DialogSetSort(host, type, object : DialogApplyListener {
                                            override fun onApply() { dialog?.dismiss() }
                                        }).also { dialog = it }
                                    }
                                })
                                DropdownMenuItem(text = { Text(stringResource(R.string.auto_update)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_restart_alt_black_20) }, onClick = {
                                    moreMenu = false
                                    autoUpdate = true
                                })
                                if (type == 24) {
                                    DropdownMenuItem(text = { Text(stringResource(R.string.info)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_info_black_24) }, onClick = {
                                        moreMenu = false
                                        try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(if (uboOn) UBO_FILTER_HELP else FILTER_HELP))) } catch (_: Exception) {}
                                    })
                                }
                            }
                        }
                    },
                )
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
                if (itemsState.isEmpty() && loaded) {
                    com.mycompany.app.ui.components.EmptyState(icon = R.drawable.outline_description_black_24, title = stringResource(R.string.down_empty_filter))
                }
                LazyColumn(contentPadding = PaddingValues(bottom = 16.dp + padding.calculateBottomPadding())) {
                    if (missingDefaults.isNotEmpty()) {
                        item(key = "ubo-offer") {
                            androidx.compose.material3.Card(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(stringResource(R.string.ubo_offer_title), style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        stringResource(R.string.ubo_offer_text) + "\n" + missingDefaults.joinToString(", ") { it[0] ?: "" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 6.dp),
                                    )
                                    androidx.compose.foundation.layout.Row(
                                        Modifier.fillMaxWidth().padding(top = 8.dp),
                                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                                    ) {
                                        androidx.compose.material3.TextButton(onClick = {
                                            offerDone = true
                                            com.mycompany.app.pref.store.AlbumSettings.edit(this@FilterScriptListActivity) {
                                                set(com.mycompany.app.pref.store.AlbumSettings.uboOfferDone, true)
                                            }
                                        }) { Text(stringResource(R.string.close)) }
                                        androidx.compose.material3.TextButton(onClick = {
                                            offerDone = true
                                            val app = this@FilterScriptListActivity
                                            com.mycompany.app.pref.store.AlbumSettings.edit(app) {
                                                set(com.mycompany.app.pref.store.AlbumSettings.uboOfferDone, true)
                                                set(com.mycompany.app.pref.store.AlbumSettings.uboAdd, DialogSetFilter.uboDefaultSelection())
                                            }
                                            val add = ArrayList<Any?>()
                                            for (f in missingDefaults) add.add(ChildItem().also { it.path = f[1]; it.name = f[0] })
                                            updateItems(add, null, null, null, -1L)
                                        }) { Text(stringResource(R.string.add)) }
                                    }
                                }
                            }
                        }
                    }
                    items(itemsState, key = { "${it.id}-${it.name}-${it.path}" }) { item ->
                        val index = itemsState.indexOf(item)
                        val on = toggled[item.id] ?: item.isEnabled
                        AppListItem(
                            leadingContent = {
                                val res = if (item.iconRes != 0) item.iconRes else R.drawable.outline_public_black_24
                                Icon(painterResource(res), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(32.dp))
                            },
                            headlineContent = {
                                Text(item.badge?.takeIf { it.isNotEmpty() } ?: item.name ?: item.path ?: "", maxLines = 2, overflow = TextOverflow.Ellipsis)
                            },
                            supportingContent = {
                                Column {
                                    val info = listOfNotNull(item.dateText, item.subText).filter { it.isNotEmpty() }.joinToString("  ")
                                    if (info.isNotEmpty()) Text(info, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    val stats = item.path?.let { listInfos[it] }
                                    if (stats != null) Text(listInfoText(stats), maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    if (type == 26) {
                                        val desc = item.mimeType?.takeIf { it.isNotEmpty() } ?: item.dirName
                                        if (!desc.isNullOrEmpty()) Text(desc, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                            },
                            trailingContent = {
                                Box {
                                    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                                        Switch(checked = on, onCheckedChange = { toggled[item.id] = it; setEnabled(item, it) })
                                        IconButton(onClick = { rowMenu = index }) { Icon(Sym.MoreVert, contentDescription = null) }
                                    }
                                    com.mycompany.app.ui.components.BubbleMenu(expanded = rowMenu == index, onDismiss = { rowMenu = -1 }) {
                                        val network = URLUtil.isNetworkUrl(item.path)
                                        DropdownMenuItem(text = { Text(stringResource(R.string.update)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_refresh_black_20) }, onClick = {
                                            rowMenu = -1
                                            if (type == 26) updateItems(null, item, null, null, item.id)
                                            else updateItems(null, null, item.path, item.name, item.id)
                                        })
                                        DropdownMenuItem(text = { Text(stringResource(R.string.delete)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_delete_black_20) }, colors = com.mycompany.app.ui.components.destructiveItemColors(), onClick = { rowMenu = -1; deleteItem(item) })
                                        if (type == 24 && (item.path == USER_FILTER || network)) {
                                            DropdownMenuItem(text = { Text(stringResource(R.string.edit)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_edit_black_24) }, onClick = { rowMenu = -1; edit(item) })
                                        }
                                        if (type == 24 || network) {
                                            DropdownMenuItem(text = { Text(stringResource(R.string.share)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_share_black_24) }, onClick = { rowMenu = -1; share(item) })
                                        }
                                        val stats = item.path?.let { listInfos[it] }
                                        if (stats != null && stats.rejected > 0) {
                                            DropdownMenuItem(text = { Text(stringResource(R.string.ubo_rejected_title)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_block_black_24) }, onClick = {
                                                rowMenu = -1
                                                rejectedOf = stats
                                            })
                                        }
                                        DropdownMenuItem(text = { Text(stringResource(R.string.details)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_info_black_24) }, onClick = {
                                            rowMenu = -1
                                            var dialog: DialogInfo? = null
                                            legacy { host -> DialogInfo(host, type, item).also { dialog = it } }
                                        })
                                    }
                                }
                            },
                            modifier = Modifier.clickable { open(item) },
                        )
                    }
                }
            }
        }
        if (autoUpdate) AutoUpdateDialog(this, type == 26) { autoUpdate = false }
        rejectedOf?.let { info ->
            val more = info.rejected - info.rejectedSamples.size
            com.mycompany.app.ui.components.InfoDialog(
                title = stringResource(R.string.ubo_rejected_title),
                message = info.rejectedSamples.joinToString("\n\n") + if (more > 0) "\n\n… +$more" else "",
                onDismiss = { rejectedOf = null },
            )
        }
    }
}
