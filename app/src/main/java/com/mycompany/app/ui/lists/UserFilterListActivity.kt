package com.mycompany.app.ui.lists

import com.mycompany.app.ui.components.AppListItem
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.data.book.DataBookUser
import com.mycompany.app.db.book.DbBookUser
import com.mycompany.app.dialog.DialogDeleteBook
import com.mycompany.app.dialog.DialogDeleteBook.DeleteBookListener
import com.mycompany.app.dialog.DialogEditCmd
import com.mycompany.app.dialog.DialogEditUrl.EditUrlListener
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.dialog.DialogSetSort
import com.mycompany.app.dialog.DialogWebView
import com.mycompany.app.list.ListTask.ListTaskConfig
import com.mycompany.app.list.ListTask.ListTaskSimpleListener
import com.mycompany.app.list.book.ListBookUser
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingsTopBar

/**
 * The user's own ad-filter rules (Filters & scripts > "User filter"; replaces the type 25 page of `DialogListBook`):
 * one row per rule (`DbBookUser_table`) with the site it applies to and an on / off switch; tap edits the rule
 * (`DialogEditCmd`), long-press offers delete; + adds a rule; the menu has turn all on / off, sort (`DialogSetSort`
 * type 25, the list's sort settings as before), info (the Adblock Plus "how to write filters" page in the web sheet,
 * as before) and delete all. Rows come from the old loader `ListBookUser` (same sorting); switches write `_use` and
 * refresh the browser's rule cache (`DataBookUser`).
 */
class UserFilterListActivity : OpumComposeActivity() {
    companion object {
        private const val HELP = "https://help.adblockplus.org/hc/en-us/articles/360062733293-How-to-write-filters#basic"
        private const val UBO_HELP = "https://github.com/gorhill/uBlock/wiki/Static-filter-syntax"
    }

    private var task: ListBookUser? = null
    private lateinit var editAsText: androidx.activity.result.ActivityResultLauncher<android.content.Intent>
    private var items by mutableStateOf<List<ChildItem>>(emptyList())
    private var loaded by mutableStateOf(false)
    private val switched = mutableStateMapOf<Long, Boolean>()
    private val uboOn: Boolean get() = true

    private val listener = object : ListTaskSimpleListener() {
        override fun onConfig(listTaskConfig: ListTaskConfig?) {
            val list = listTaskConfig?.items?.filterNotNull()?.filter { it.rowType.toInt() == 0 } ?: emptyList()
            runOnUiThread {
                switched.clear()
                items = list
                loaded = true
            }
        }
    }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        reload()
    }

    override fun onDestroy() {
        task?.cancel()
        super.onDestroy()
        if (isFinishing) ListReturn.fire()
    }

    private fun reload() {
        // rules may have changed (edit, delete, My filters): the engine rebuilds when they did
        com.mycompany.app.web.filter.UboFilter.onListsChanged(applicationContext)
        task?.cancel()
        val listBookUser = ListBookUser().also { it.appContext = this; it.taskListener = listener }
        task = listBookUser
        listBookUser.load(false, false, false)
    }

    private fun legacy(factory: (android.app.Activity) -> android.app.Dialog?) {
        com.mycompany.app.ui.prefs.DialogShow.direct(this as android.app.Activity, onDismiss = { reload() }, factory = factory)
    }

    /** Add (null) or edit a rule. */
    private fun edit(item: ChildItem?) {
        var dialog: DialogEditCmd? = null
        legacy { host ->
            DialogEditCmd(host, item?.id ?: 0L, item?.path, false, object : EditUrlListener {
                override fun onSaved(id: Long, path: String?, extra: String?) { dialog?.dismiss() }
            }).also { dialog = it }
        }
    }

    private fun setEnabled(list: List<ChildItem>, on: Boolean) {
        val context: Context = this
        for (item in list) {
            item.isEnabled = on
            switched[item.id] = on
        }
        val ids = list.map { it.id }
        Thread {
            for (id in ids) {
                try { DbBookUser.setUse(context, id, on) } catch (_: Exception) {}
            }
            DataBookUser.instance(context).clear()
            com.mycompany.app.web.filter.UboFilter.onListsChanged(context)
        }.start()
    }

    private fun delete(list: List<ChildItem>, all: Boolean) {
        if (list.isEmpty()) return
        var dialog: DialogDeleteBook? = null
        dialog = DialogDeleteBook(
            this, 25, ArrayList<Any?>(list), null, all, true,
            object : DeleteBookListener {
                override fun onCancelled() {}
                override fun onDeleted() {
                    reload()
                    dialog?.dismiss()
                }
            },
        )
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        editAsText = androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
        ) { reload() }
        var moreMenu by remember { mutableStateOf(false) }
        var rowMenu by remember { mutableIntStateOf(-1) }
        Scaffold(
            topBar = {
                SettingsTopBar(
                    title = stringResource(R.string.user_filter),
                    onBack = { finish() },
                    actions = {
                        IconButton(onClick = { edit(null) }) {
                            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
                        }
                        Box {
                            IconButton(onClick = { moreMenu = true }) { Icon(Icons.Filled.MoreVert, contentDescription = null) }
                            com.mycompany.app.ui.components.BubbleMenu(expanded = moreMenu, onDismiss = { moreMenu = false }) {
                                DropdownMenuItem(text = { Text(stringResource(R.string.used_on)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_verified_user_black_24) }, onClick = { moreMenu = false; setEnabled(items, true) })
                                DropdownMenuItem(text = { Text(stringResource(R.string.not_used)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_block_black_24) }, onClick = { moreMenu = false; setEnabled(items, false) })
                                DropdownMenuItem(text = { Text(stringResource(R.string.sort)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_sort_black_24) }, onClick = {
                                    moreMenu = false
                                    var dialog: DialogSetSort? = null
                                    legacy { host ->
                                        DialogSetSort(host, 25, object : DialogApplyListener {
                                            override fun onApply() { dialog?.dismiss() }
                                        }).also { dialog = it }
                                    }
                                })
                                if (uboOn) {
                                    DropdownMenuItem(text = { Text(stringResource(R.string.ubo_edit_as_text)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_edit_black_24) }, onClick = {
                                        moreMenu = false
                                        editAsText.launch(android.content.Intent(this@UserFilterListActivity, MyFiltersActivity::class.java))
                                    })
                                }
                                DropdownMenuItem(text = { Text(stringResource(R.string.info)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_info_black_24) }, onClick = {
                                    moreMenu = false
                                    val help = if (uboOn) UBO_HELP else HELP
                                    legacy { host -> DialogWebView(host, help, help, false, 2, null) }
                                })
                                DropdownMenuItem(text = { Text(stringResource(R.string.delete_all)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_delete_sweep_black_24) }, colors = com.mycompany.app.ui.components.destructiveItemColors(), onClick = { moreMenu = false; delete(items, true) })
                            }
                        }
                    },
                )
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
                if (items.isEmpty() && loaded) {
                    com.mycompany.app.ui.components.EmptyState(icon = R.drawable.outline_verified_user_black_24, title = stringResource(R.string.down_empty_filter))
                }
                LazyColumn(contentPadding = PaddingValues(bottom = 16.dp + padding.calculateBottomPadding())) {
                    itemsIndexed(items, key = { _, it -> it.id }) { index, item ->
                        val on = switched[item.id] ?: item.isEnabled
                        Box {
                            AppListItem(
                                leadingContent = {
                                    Icon(painterResource(R.drawable.outline_verified_user_black_24), null, modifier = Modifier.size(24.dp))
                                },
                                headlineContent = { Text(item.path ?: "", maxLines = 3, overflow = TextOverflow.Ellipsis) },
                                supportingContent = {
                                    Column {
                                        item.subText?.takeIf { it.isNotEmpty() }?.let { host -> Text(host, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                                        val err = remember(item.path, uboOn) {
                                            if (uboOn) item.path?.trim()?.takeIf { it.isNotEmpty() && !it.startsWith("!") }?.let { com.mycompany.app.web.filter.UboEngine.validate(it) } else null
                                        }
                                        if (err != null) {
                                            Text(stringResource(R.string.ubo_rule_not_supported, err), color = MaterialTheme.colorScheme.error, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                },
                                trailingContent = { Switch(checked = on, onCheckedChange = { setEnabled(listOf(item), it) }) },
                                modifier = Modifier.combinedClickable(onClick = { edit(item) }, onLongClick = { rowMenu = index }),
                            )
                            com.mycompany.app.ui.components.BubbleMenu(expanded = rowMenu == index, onDismiss = { rowMenu = -1 }) {
                                DropdownMenuItem(text = { Text(stringResource(R.string.edit)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_edit_black_24) }, onClick = { rowMenu = -1; edit(item) })
                                DropdownMenuItem(text = { Text(stringResource(R.string.delete)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_delete_black_20) }, colors = com.mycompany.app.ui.components.destructiveItemColors(), onClick = { rowMenu = -1; delete(listOf(item), false) })
                            }
                        }
                    }
                }
            }
        }
    }
}
