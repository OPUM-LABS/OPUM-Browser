package com.mycompany.app.ui.lists

import com.mycompany.app.ui.components.AppListItem
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.OpumComposeActivity
import kotlinx.coroutines.launch
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingsTopBar

/**
 * Editor for the plain site lists (see [SiteListStore]): newest first, search, add with the + button, tap a row to
 * edit it, the trash icon deletes one entry and the bar action deletes all. Replaces the legacy list dialog for
 * these list types. Opened from a page (`EXTRA_PAGE`, the page dialogs), + offers what the old page list offered:
 * direct input, add this site (host only) or add this page (full address); an address already in the list is
 * scrolled to with "already added". Whoever opened it can be told when it closes ([ListReturn]).
 */
class SiteListActivity : OpumComposeActivity() {

    companion object {
        private const val EXTRA_CODE = "code"
        private const val EXTRA_PAGE = "page"

        fun supports(code: Int) = code in SiteListStore.supported

        fun open(context: Context, code: Int) {
            context.startActivity(Intent(context, SiteListActivity::class.java).putExtra(EXTRA_CODE, code))
        }

        /** From a page dialog: [page] is the page's address (for "add site / add page"), [onClosed] runs when the list closes. */
        fun open(context: Context, code: Int, page: String?, onClosed: (() -> Unit)?) {
            ListReturn.set(onClosed)
            context.startActivity(
                Intent(context, SiteListActivity::class.java).putExtra(EXTRA_CODE, code).putExtra(EXTRA_PAGE, page)
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) ListReturn.fire()
    }

    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context: Context = this
        val code = intent.getIntExtra(EXTRA_CODE, 19)
        val store = remember { SiteListStore(context, code) }
        var entries by remember { mutableStateOf(store.load()) }
        var query by remember { mutableStateOf("") }
        var searching by remember { mutableStateOf(false) }
        var editing by remember { mutableStateOf<SiteEntry?>(null) }
        var adding by remember { mutableStateOf(false) }
        var deleting by remember { mutableStateOf<SiteEntry?>(null) }
        var deletingAll by remember { mutableStateOf(false) }
        val page = remember { MainUtil.trimTrailingSlash(intent.getStringExtra(EXTRA_PAGE)) }
        var addMenu by remember { mutableStateOf(false) }
        val listState = rememberLazyListState()
        val scope = rememberCoroutineScope()

        fun toast(result: SiteListStore.AddResult) {
            val res = when (result) {
                SiteListStore.AddResult.EMPTY -> R.string.empty
                SiteListStore.AddResult.EXISTS -> R.string.already_added
                SiteListStore.AddResult.UNSUPPORTED -> R.string.not_supported_page
                SiteListStore.AddResult.OK -> return
            }
            com.mycompany.app.main.MainUtil.showToast(context, res)
        }

        val shown = if (query.isBlank()) entries else entries.filter { it.path.contains(query.trim(), ignoreCase = true) }

        /** Add the page's host ([hostOnly]) or full address, or scroll to it when it is there. */
        fun addFromPage(hostOnly: Boolean) {
            val path = if (hostOnly) MainUtil.hostOfUrl(page ?: "", true) else page
            if (path.isNullOrEmpty()) {
                com.mycompany.app.main.MainUtil.showToast(context, R.string.invalid_url)
                return
            }
            val result = store.add(path, entries)
            toast(result)
            if (result == SiteListStore.AddResult.OK) entries = store.load()
            val target = MainUtil.trimTrailingSlash(path)
            query = ""
            searching = false
            val index = entries.indexOfFirst { it.path.equals(target, ignoreCase = true) }
            if (index >= 0) scope.launch { listState.animateScrollToItem(index) }
        }

        Scaffold(
            topBar = {
                SettingsTopBar(
                    title = stringResource(SiteListStore.titleRes(code)),
                    onBack = { finish() },
                    actions = {
                        IconButton(onClick = { searching = !searching; if (!searching) query = "" }) {
                            Icon(
                                if (searching) Icons.Filled.Close else Icons.Filled.Search,
                                contentDescription = null,
                            )
                        }
                        IconButton(onClick = { if (entries.isNotEmpty()) deletingAll = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete_all))
                        }
                    },
                )
            },
            floatingActionButton = {
                Box {
                    FloatingActionButton(onClick = { if (page.isNullOrEmpty()) adding = true else addMenu = true }) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
                    }
                    com.mycompany.app.ui.components.BubbleMenu(expanded = addMenu, onDismiss = { addMenu = false }) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.direct_input)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_link_black_24) }, onClick = { addMenu = false; adding = true })
                        DropdownMenuItem(text = { Text(stringResource(R.string.add_site)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_add_black_24) }, onClick = { addMenu = false; addFromPage(true) })
                        DropdownMenuItem(text = { Text(stringResource(R.string.add_page)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_add_box_black_24) }, onClick = { addMenu = false; addFromPage(false) })
                    }
                }
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
                androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
                    if (searching) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            label = { Text(stringResource(R.string.search_url)) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 96.dp + padding.calculateBottomPadding())) {
                        items(shown, key = { it.id }) { entry ->
                            AppListItem(
                                headlineContent = { Text(entry.path, maxLines = 2) },
                                trailingContent = {
                                    IconButton(onClick = { deleting = entry }) {
                                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
                                    }
                                },
                                modifier = Modifier.clickable { editing = entry },
                            )
                        }
                    }
                }
            }
        }

        if (adding) {
            PathDialog(
                title = stringResource(R.string.add),
                initial = "",
                onDismiss = { adding = false },
                onConfirm = { newPath ->
                    val result = store.add(newPath, entries)
                    toast(result)
                    if (result == SiteListStore.AddResult.OK) { entries = store.load(); adding = false }
                },
            )
        }
        editing?.let { entry ->
            PathDialog(
                title = stringResource(R.string.edit),
                initial = entry.path,
                onDismiss = { editing = null },
                onConfirm = { newPath ->
                    val result = store.rename(entry, newPath, entries)
                    toast(result)
                    if (result == SiteListStore.AddResult.OK) { entries = store.load(); editing = null }
                },
            )
        }
        deleting?.let { entry ->
            ConfirmDialog(
                message = entry.path,
                title = stringResource(R.string.delete),
                onConfirm = { store.delete(listOf(entry)); entries = store.load() },
                onDismiss = { deleting = null },
            )
        }
        if (deletingAll) {
            ConfirmDialog(
                message = stringResource(R.string.delete_all),
                onConfirm = { store.delete(entries); entries = store.load() },
                onDismiss = { deletingAll = false },
            )
        }
    }
}

@Composable
private fun PathDialog(title: String, initial: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var confirmed by remember { mutableStateOf(initial) }
    com.mycompany.app.ui.components.OpumSheetDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = confirmed,
                onValueChange = { confirmed = it },
                singleLine = true,
                label = { Text(stringResource(R.string.input_url)) },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = { TextButton(onClick = { onConfirm(confirmed) }) { Text(stringResource(R.string.ok)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
