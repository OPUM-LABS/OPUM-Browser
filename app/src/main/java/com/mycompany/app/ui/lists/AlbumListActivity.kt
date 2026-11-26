package com.mycompany.app.ui.lists

import com.mycompany.app.ui.components.AppListItem
import android.content.Context
import com.mycompany.app.pref.store.ListSettings
import com.mycompany.app.pref.store.PathSettings
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.mycompany.app.dialog.DialogExtract
import com.mycompany.app.dialog.DialogFileDelete
import com.mycompany.app.dialog.DialogFileDelete.FileDeleteListener
import com.mycompany.app.dialog.DialogFileRename
import com.mycompany.app.dialog.DialogFileRename.FileRenameListener
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.list.ListTask
import com.mycompany.app.list.ListTask.ListTaskConfig
import com.mycompany.app.list.ListTask.ListTaskSimpleListener
import com.mycompany.app.list.ListTaskAlbum
import com.mycompany.app.list.ListTaskCmp
import com.mycompany.app.list.ListTaskPdf
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefList
import com.mycompany.app.pref.PrefPath
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.InfoDialog
import com.mycompany.app.ui.components.SettingsTopBar
import com.mycompany.app.ui.components.Sym

/**
 * Albums, PDFs and ZIP archives found in a folder you pick (`EXTRA_TYPE` 1 album = image archives, 2 PDF, 3 ZIP;
 * `EXTRA_SHORT` comes from home-screen shortcuts pinned before 2026-11-26 and opens the last type). The list and the folder scan are the old tasks (`ListTaskAlbum`,
 * `ListTaskPdf`, `ListTaskCmp`), so the stored entries, sorting, the viewer's cache and "files added" counts are
 * unchanged. Tap opens the file in the viewer; long-press selects; the menu has extract (albums), rename, share,
 * details and delete file. Replaces `MainListAlbum`.
 */
class AlbumListActivity : OpumComposeActivity() {

    private var type = 1
    private var task: ListTask? = null
    private var itemsState by mutableStateOf<List<ChildItem>>(emptyList())
    private var loading by mutableStateOf(true)
    private var loadedOnce by mutableStateOf(false)
    private var reloadTick by mutableIntStateOf(0)
    private var announceAdded = false

    private val listener = object : ListTaskSimpleListener() {
        override fun onDone() { runOnUiThread { loading = true } }

        override fun onStart() { runOnUiThread { loading = false } }

        override fun onConfig(listTaskConfig: ListTaskConfig?) {
            val config = listTaskConfig ?: return
            val list = config.items?.filterNotNull() ?: emptyList()
            runOnUiThread {
                itemsState = list
                loading = false
                loadedOnce = true
                if (config.addedCount > 0) {
                    announceAdded = false
                    MainUtil.showToastText(this@AlbumListActivity, resources.getQuantityString(R.plurals.file_added, config.addedCount, config.addedCount))
                } else if (announceAdded && list.isEmpty().not()) {
                    announceAdded = false
                    MainUtil.showToast(this@AlbumListActivity, R.string.no_added)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        val short = intent.getBooleanExtra("EXTRA_SHORT", false)
        type = if (short) ListSettings.viewType.value.takeIf { it in 1..3 } ?: 1 else intent.getIntExtra("EXTRA_TYPE", 1)
        if (type !in 1..3) type = 1
        if (ListSettings.viewType.value != type) {
            ListSettings.edit(applicationContext) { set(ListSettings.viewType, type) }
        }
        super.onCreate(savedInstanceState)
        task = when (type) {
            2 -> ListTaskPdf(applicationContext, listener)
            3 -> ListTaskCmp(applicationContext, listener)
            else -> ListTaskAlbum(applicationContext, listener)
        }
        task?.load(true, true, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        task?.cancel()
        task = null
    }

    private fun scanPath(): String? = when (type) {
        2 -> PathSettings.scanPdf.value
        3 -> PathSettings.scanZip.value
        else -> PathSettings.scanAlbum.value
    }

    private fun scanKey(): String = when (type) {
        2 -> "mScanPdf"
        3 -> "mScanZip"
        else -> "mScanAlbum"
    }

    private fun rescan() {
        announceAdded = true
        task?.refresh(false)
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context: Context = this
        val selected = remember { mutableStateListOf<String>() }
        var query by remember { mutableStateOf("") }
        var searching by remember { mutableStateOf(false) }
        var menuFor by remember { mutableStateOf<ChildItem?>(null) }
        var details by remember { mutableStateOf<ChildItem?>(null) }
        var extractTarget by remember { mutableStateOf<List<ChildItem>?>(null) }
        var more by remember { mutableStateOf(false) }

        val items = itemsState
        val shown = if (query.isBlank()) items else items.filter { it.name?.lowercase()?.contains(query.trim().lowercase()) == true }
        fun keyOf(item: ChildItem) = item.path ?: ""
        fun chosen() = items.filter { keyOf(it) in selected }

        val folderPick = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data?.data ?: return@rememberLauncherForActivityResult
            if (result.resultCode != RESULT_OK) return@rememberLauncherForActivityResult
            val path = MainUri.treeDocUri(data)
            if (path.isNullOrEmpty()) {
                MainUtil.showToast(context, R.string.invalid_path)
                return@rememberLauncherForActivityResult
            }
            // extracting: this folder is where the files go; otherwise it is the folder to scan
            val extract = extractTarget
            if (extract != null) {
                extractTarget = null
                if (extract.isNotEmpty()) {
                    DialogExtract(this, type, ArrayList<Any?>(extract), path, object : DialogApplyListener {
                        override fun onApply() { reloadTick++ }
                    })
                }
                return@rememberLauncherForActivityResult
            }
            when (type) {
                2 -> if (path != PathSettings.scanPdf.value) { PathSettings.editLocal { set(PathSettings.scanPdf, path) }; PrefSet.putString(context, 6, scanKey(), path) }
                3 -> if (path != PathSettings.scanZip.value) { PathSettings.editLocal { set(PathSettings.scanZip, path) }; PrefSet.putString(context, 6, scanKey(), path) }
                else -> if (path != PathSettings.scanAlbum.value) { PathSettings.editLocal { set(PathSettings.scanAlbum, path) }; PrefSet.putString(context, 6, scanKey(), path) }
            }
            MainUtil.keepUriPermission(context, data)
            MainUtil.showToast(context, R.string.dir_scanning)
            rescan()
        }

        fun pickFolder() {
            try {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                val initial = scanPath()
                if (!TextUtils.isEmpty(initial)) {
                    intent.putExtra("android.provider.extra.INITIAL_URI", Uri.parse(initial))
                }
                folderPick.launch(intent)
            } catch (_: Exception) {
                MainUtil.showToast(context, R.string.not_supported)
            }
        }

        LaunchedEffect(reloadTick) {
            if (reloadTick > 0) task?.load(false, false, false)
        }

        fun open(item: ChildItem) {
            if (!MainUri.isFile(context, item.path)) {
                MainUtil.showToast(context, R.string.invalid_path)
                return
            }
            startActivity(
                Intent(context, com.mycompany.app.ui.viewer.ImageViewerActivity::class.java)
                    .putExtra("EXTRA_TYPE", type)
                    .putExtra("EXTRA_BOOK", false)
                    .putExtra("EXTRA_INDEX", item.position)
            )
        }

        fun deleteFiles(list: List<ChildItem>) {
            var dialog: DialogFileDelete? = null
            dialog = DialogFileDelete(this, type, ArrayList<Any?>(list), object : FileDeleteListener {
                override fun onCancelled() { reloadTick++ }
                override fun onDeleted(isOk: Boolean) {
                    reloadTick++
                    dialog?.dismiss()
                }
            })
        }

        fun rename(item: ChildItem) {
            DialogFileRename(this, type, item, object : FileRenameListener {
                override fun onRenamed(id: Long, newPath: String?, listType: Int) { reloadTick++ }
            })
        }

        fun extract(list: List<ChildItem>) {
            if (list.isEmpty()) return
            extractTarget = list
            pickFolder()
        }

        BackHandler(enabled = selected.isNotEmpty()) { selected.clear() }

        Scaffold(
            topBar = {
                if (selected.isNotEmpty()) {
                    SettingsTopBar(
                        title = "${selected.size}",
                        onBack = { selected.clear() },
                        actions = {
                            IconButton(onClick = { selected.clear(); selected.addAll(items.map { keyOf(it) }) }) {
                                Icon(Sym.Check, contentDescription = stringResource(R.string.all))
                            }
                            if (type == 1) {
                                TextButton(onClick = { val list = chosen(); selected.clear(); extract(list) }) {
                                    Text(stringResource(R.string.extract))
                                }
                            }
                            IconButton(onClick = { val list = chosen(); selected.clear(); deleteFiles(list) }) {
                                Icon(Sym.Delete, contentDescription = stringResource(R.string.delete_file))
                            }
                        },
                    )
                } else {
                    SettingsTopBar(
                        title = stringResource(when (type) { 2 -> R.string.pdf; 3 -> R.string.zip; else -> R.string.album }),
                        onBack = { finish() },
                        actions = {
                            IconButton(onClick = { searching = !searching; if (!searching) query = "" }) {
                                Icon(if (searching) Sym.Close else Sym.Search, contentDescription = null)
                            }
                            IconButton(onClick = { rescan() }) {
                                Icon(Sym.Refresh, contentDescription = stringResource(R.string.scan_dir))
                            }
                            Box {
                                IconButton(onClick = { more = true }) { Icon(Sym.MoreVert, contentDescription = null) }
                                com.mycompany.app.ui.components.BubbleMenu(expanded = more, onDismiss = { more = false }) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.select_dir)) },
                                        onClick = { more = false; pickFolder() },
                                    )
                                }
                            }
                        },
                    )
                }
            },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
                if (searching) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        label = { Text(stringResource(R.string.search_url)) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                Box(Modifier.fillMaxSize()) {
                    if (shown.isEmpty()) {
                        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                            if (loading) {
                                CircularProgressIndicator()
                            } else if (loadedOnce) {
                                com.mycompany.app.ui.components.EmptyState(icon = R.drawable.outline_photo_library_black_24, title = stringResource(R.string.no_found)) {
                                    Button(onClick = { pickFolder() }) { Text(stringResource(R.string.scan_dir)) }
                                }
                            }
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp + padding.calculateBottomPadding())) {
                            shown.forEach { item ->
                                item(key = keyOf(item)) {
                                    val key = keyOf(item)
                                    AppListItem(
                                        leadingContent = {
                                            if (selected.isNotEmpty()) {
                                                Checkbox(checked = key in selected, onCheckedChange = {
                                                    if (key in selected) selected.remove(key) else selected.add(key)
                                                })
                                            } else {
                                                val res = if (item.iconRes != 0) item.iconRes else R.drawable.outline_public_black_24
                                                Icon(painterResource(res), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(32.dp))
                                            }
                                        },
                                        headlineContent = { Text(item.name ?: "", maxLines = 2, overflow = TextOverflow.Ellipsis) },
                                        supportingContent = {
                                            val date = if (item.time > 0) MainUtil.formatDate(item.time, null, "yyyy.MM.dd") else null
                                            val size = if (item.size > 0) MainUtil.sizeLabel(item.size) else null
                                            Text(listOfNotNull(date, size).joinToString("   "))
                                        },
                                        trailingContent = if (selected.isNotEmpty()) null else {
                                            { IconButton(onClick = { menuFor = item }) { Icon(Sym.MoreVert, contentDescription = null) } }
                                        },
                                        modifier = Modifier.combinedClickable(
                                            onClick = {
                                                if (selected.isNotEmpty()) { if (key in selected) selected.remove(key) else selected.add(key) }
                                                else open(item)
                                            },
                                            onLongClick = { if (key in selected) selected.remove(key) else selected.add(key) },
                                        ),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        menuFor?.let { item ->
            com.mycompany.app.ui.components.OpumSheetDialog(
                onDismissRequest = { menuFor = null },
                title = { Text(item.name ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        if (type == 1) MenuText(stringResource(R.string.extract)) { menuFor = null; extract(listOf(item)) }
                        MenuText(stringResource(R.string.delete_file)) { menuFor = null; deleteFiles(listOf(item)) }
                        MenuText(stringResource(R.string.rename)) { menuFor = null; rename(item) }
                        MenuText(stringResource(R.string.share)) {
                            menuFor = null
                            MainUtil.shareFile(type, context as android.app.Activity, item.path, item.name, MainUtil.mimeForUrl(item.name))
                        }
                        MenuText(stringResource(R.string.details)) { menuFor = null; details = item }
                    }
                },
                confirmButton = {},
                dismissButton = { TextButton(onClick = { menuFor = null }) { Text(stringResource(R.string.cancel)) } },
            )
        }
        details?.let { item ->
            val labelText = buildString {
                append(item.path ?: "")
                if (item.size > 0) append("\n\n").append(MainUtil.sizeLabel(item.size))
                if (item.time > 0) append("\n").append(java.text.DateFormat.getDateTimeInstance().format(java.util.Date(item.time)))
            }
            InfoDialog(item.name ?: stringResource(R.string.details), labelText) { details = null }
        }
    }

}

@Composable
private fun MenuText(label: String, onClick: () -> Unit) {
    Text(
        label,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
    )
}
