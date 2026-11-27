package com.mycompany.app.ui.lists

import com.mycompany.app.ui.components.AppListItem
import android.content.Context
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ListSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.AlbumSettings
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.data.book.DataBookDown
import com.mycompany.app.dialog.DialogDeleteBook
import com.mycompany.app.dialog.DialogDeleteBook.DeleteBookListener
import com.mycompany.app.dialog.DialogDownFile
import com.mycompany.app.dialog.DialogDownUrl
import com.mycompany.app.dialog.DialogDownUrl.DownUrlListener
import com.mycompany.app.dialog.DialogFileDelete
import com.mycompany.app.dialog.DialogFileDelete.FileDeleteListener
import com.mycompany.app.dialog.DialogFileRename
import com.mycompany.app.dialog.DialogFileRename.FileRenameListener
import com.mycompany.app.dialog.DialogPreview
import com.mycompany.app.dialog.DialogPreview.PreviewListener
import com.mycompany.app.dialog.DialogSetDown
import com.mycompany.app.dialog.DialogSetDown.SetDownListener
import com.mycompany.app.db.book.DbBookDown
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.web.WebNestView
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.mutableStateListOf
import java.io.File
import com.mycompany.app.dialog.DialogOpenType
import com.mycompany.app.list.book.ListBookDown
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainDownSvc
import com.mycompany.app.main.MainDownSvc.DownItem
import com.mycompany.app.main.MainDownSvc.DownListListener
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefList
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.InfoDialog
import com.mycompany.app.ui.components.SettingsTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections
import com.mycompany.app.ui.components.Sym

/**
 * Download list: everything downloaded or being downloaded (and saved pages), with the type filter chips, search,
 * sort and grouping, live progress from the download service, and pause/resume/retry. Tap opens a finished file (or,
 * when the browser asked to pick a saved page, returns it), long-press opens the item menu (open with, delete,
 * details, copy URL). Reads the same `DbBookDown`/`DbBookPage` tables through the old loader, so statuses, sorting and
 * secret-mode rules are unchanged. Replaces `MainListDown`.
 *
 * Extras (as before): `EXTRA_ID` scrolls to that download (notification tap), `EXTRA_PATH` to a file path. Started for
 * a result by the browser: a tapped saved page comes back as `EXTRA_PATH` + `EXTRA_URL`.
 */
class DownloadListActivity : OpumComposeActivity() {

    /** Bumped when the service reports progress, so rows re-read their (mutable) items. */
    private var version by mutableIntStateOf(0)
    private var reloadRequest by mutableIntStateOf(0)
    private var focusId by mutableLongStateOf(-1L)
    private var focusPath by mutableStateOf<String?>(null)
    private var connected = false

    private val downListener = object : DownListListener {
        override fun onProgress(progress: Int, id: Long) {
            if (!connected) return
            val cache = DataBookDown.instance(applicationContext)
            if (progress == -1234) cache.removeId(id)
            else cache.itemFor(id)?.let { it.status = progress; if (progress != 2) it.isSecret = false }
            runOnUiThread { if (progress == -1234) reloadRequest++ else version++ }
        }

        override fun onAdded(downItem: DownItem?) = progress(downItem)

        override fun onDisconnected() { connected = false }

        override fun onChanged(downItem: DownItem?) = progress(downItem)

        override fun onListChanged(isAdded: Boolean) {
            if (!connected) return
            runOnUiThread { if (isAdded) reloadRequest++ else version++ }
        }

        override fun onConnected() { connected = true }

        private fun progress(item: DownItem?) {
            if (!connected || item == null) return
            DataBookDown.instance(applicationContext).updateState(item.status, item.id, item.readSize, item.totalSize)
            trackSpeed(item.id, item.status, item.totalSize)
            // the site answered 429 (too many requests): the row says so while it waits
            if (item.httpStatus == 429) limited.add(item.id) else limited.remove(item.id)
            // finished while the list is on screen: already seen
            if (item.status == 3 && lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                runOnUiThread { com.mycompany.app.down.DownState.markSeen(applicationContext) }
            }
            runOnUiThread { version++ }
        }
    }

    /** Downloads the site is limiting right now (429), by id. */
    internal val limited: MutableSet<Long> = java.util.concurrent.ConcurrentHashMap.newKeySet()

    /** Bytes per second per running download, smoothed (2026-11-20: the row shows speed and time left). */
    private val speeds = java.util.concurrent.ConcurrentHashMap<Long, Double>()
    private val lastSample = java.util.concurrent.ConcurrentHashMap<Long, LongArray>()

    private fun trackSpeed(id: Long, status: Int, done: Long) {
        if (status != 1) { speeds.remove(id); lastSample.remove(id); return }
        val now = System.currentTimeMillis()
        val last = lastSample[id]
        lastSample[id] = longArrayOf(now, done)
        if (last == null) return
        val dt = now - last[0]
        val db = done - last[1]
        if (dt < 300 || db < 0) return
        val sample = db * 1000.0 / dt
        speeds[id] = speeds[id]?.let { it * 0.7 + sample * 0.3 } ?: sample
    }

    /** "2.1 MB/s · 15 s left" for a running download, or null. */
    internal fun speedText(item: ChildItem): String? {
        val bps = speeds[item.id] ?: return null
        if (bps < 1) return null
        val sb = StringBuilder(MainUtil.sizeLabel(bps.toLong())).append("/s")
        if (item.size > 0 && item.readSize < item.size) {
            val secs = ((item.size - item.readSize) / bps).toLong()
            val left = when {
                secs < 60 -> "${secs} s"
                secs < 3600 -> "${secs / 60} min"
                else -> "${secs / 3600} h ${(secs % 3600) / 60} min"
            }
            sb.append(" · ").append(getString(R.string.down_left, left))
        }
        return sb.toString()
    }

    /** Pause or resume every download that is running / paused. */
    private fun pauseAll(items: List<ChildItem>, pause: Boolean) {
        for (item in items) {
            if (item.listType == 8) continue
            if (pause && item.status == 1) toggle(item)
            else if (!pause && item.status == 2) toggle(item)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // opening the list counts as having seen the finished downloads (the Downloads button stops being filled)
        com.mycompany.app.down.DownState.markSeen(applicationContext)
        readExtras(intent)
        super.onCreate(savedInstanceState)
        attachService()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readExtras(intent)
    }

    private fun readExtras(intent: Intent?) {
        val id = intent?.getLongExtra("EXTRA_ID", -1L) ?: -1L
        focusId = id
        focusPath = if (id == -1L) intent?.getStringExtra("EXTRA_PATH") else null
    }

    private fun attachService() {
        val app = MainApp.from(applicationContext) ?: return
        app.downListListener = downListener
        val svc: MainDownSvc? = if (app.downBinding) app.downService else null
        if (app.downBinding && svc != null) {
            svc.listListener = downListener
            downListener.onConnected()
        }
    }

    private fun detachService() {
        connected = false
        val app = MainApp.from(applicationContext) ?: return
        app.downListListener = null
        val svc: MainDownSvc? = if (app.downBinding) app.downService else null
        if (app.downBinding && svc != null) svc.listListener = null
    }

    override fun onResume() {
        super.onResume()
        com.mycompany.app.down.DownState.markSeen(applicationContext)
        reloadRequest++
    }

    override fun onDestroy() {
        super.onDestroy()
        detachService()
    }

    /** Pause (running) or resume (paused) a download, or restart a failed one, like the old row button. */
    private fun toggle(item: ChildItem) {
        val app = MainApp.from(applicationContext) ?: return
        if (item.status == 4) {
            com.mycompany.app.down.DownState.clearFailReason(applicationContext, item.id)
            app.sendDownAction(item.id)
            return
        }
        val pause = item.status != 2
        if (pause || app.downService != null) {
            val svc: MainDownSvc? = if (app.downBinding) app.downService else null
            if (app.downBinding && svc != null) svc.pauseItem(item.id, pause)
        } else if (!app.downStarting) {
            app.downStarting = true
            app.pendingActionId = item.id
            app.pendingAction = 1.toByte()
            app.bindDownService()
        }
        version++
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context: Context = this
        val scope = rememberCoroutineScope()
        var items by remember { mutableStateOf<List<ChildItem>>(emptyList()) }
        var loaded by remember { mutableStateOf(false) }
        var query by remember { mutableStateOf("") }
        var searching by remember { mutableStateOf(false) }
        var filter by remember { mutableIntStateOf(ListSettings.bookDownType.value) }
        var menu by remember { mutableStateOf(false) }
        var itemMenu by remember { mutableStateOf<ChildItem?>(null) }
        var details by remember { mutableStateOf<ChildItem?>(null) }
        var sorting by remember { mutableStateOf(false) }
        var sortKey by remember { mutableIntStateOf(ListSettings.bookDownItem.value) }
        var reverse by remember { mutableStateOf(ListSettings.bookDownRvse.value) }
        var group by remember { mutableIntStateOf(ListSettings.bookDownFold.value) }
        val listState = rememberLazyListState()
        val selected = remember { mutableStateListOf<String>() }   // keys "type-id" of the chosen rows
        fun keyOf(item: ChildItem) = "${item.listType}-${item.id}"

        LaunchedEffect(reloadRequest, filter, sortKey, reverse, group) {
            items = withContext(Dispatchers.IO) { loadItems(context) }
            loaded = true
        }
        LaunchedEffect(items, focusId, focusPath) {
            val target = items.indexOfFirst { if (focusId != -1L) it.listType != 8 && it.id == focusId else focusPath != null && MainUtil.sameObject(focusPath, it.path) }
            if (target >= 0) listState.animateScrollToItem(target)
        }

        val shown = if (query.isBlank()) items else items.filter { it.name?.lowercase()?.contains(query.trim().lowercase()) == true }

        /** Delete records / files; the "all" shortcut only when the whole list (no filter) is chosen. */
        fun recordsOf(list: List<ChildItem>) = deleteRecords(list, filter == 0 && list.size == items.size)
        fun chosen() = items.filter { keyOf(it) in selected }

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
                            Box {
                                var more by remember { mutableStateOf(false) }
                                @Suppress("UNUSED_VARIABLE") val unused = more
                                IconButton(onClick = { val list = chosen(); selected.clear(); deleteAsk(list, false) }) {
                                    Icon(Sym.Delete, contentDescription = stringResource(R.string.delete))
                                }
                            }
                        },
                    )
                } else {
                SettingsTopBar(
                    title = stringResource(MainConst.downFilterNames[MainConst.downFilterValues.indexOf(filter).coerceAtLeast(0)]),
                    onBack = { finish() },
                    actions = {
                        IconButton(onClick = { searching = !searching; if (!searching) query = "" }) {
                            Icon(if (searching) Sym.Close else Sym.Search, contentDescription = null)
                        }
                        Box {
                            IconButton(onClick = { menu = true }) { Icon(Sym.MoreVert, contentDescription = null) }
                            com.mycompany.app.ui.components.BubbleMenu(expanded = menu, onDismiss = { menu = false }) {
                                if (items.any { it.listType != 8 && it.status == 1 }) {
                                    DropdownMenuItem(text = { Text(stringResource(R.string.down_pause_all)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.baseline_pause_black_24) }, onClick = { menu = false; pauseAll(items, true) })
                                }
                                if (items.any { it.listType != 8 && it.status == 2 }) {
                                    DropdownMenuItem(text = { Text(stringResource(R.string.down_resume_all)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.baseline_play_arrow_black_24) }, onClick = { menu = false; pauseAll(items, false) })
                                }
                                DropdownMenuItem(text = { Text(stringResource(R.string.sort)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_sort_black_24) }, onClick = { menu = false; sorting = true })
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.delete_all)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_delete_sweep_black_24) }, colors = com.mycompany.app.ui.components.destructiveItemColors(),
                                    onClick = { menu = false; if (items.isNotEmpty()) recordsOf(items) },
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
                LazyRow(contentPadding = PaddingValues(horizontal = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MainConst.downFilterValues.size) { index ->
                        val type = MainConst.downFilterValues[index]
                        FilterChip(
                            selected = filter == type,
                            onClick = {
                                filter = type
                                ListSettings.edit(context) { set(ListSettings.bookDownType, type) }
                            },
                            label = { Text(stringResource(MainConst.downFilterNames[index])) },
                        )
                    }
                }
                val versionNow = version // rows re-read their items when the service reports progress
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(start = com.mycompany.app.ui.components.readableSide(), end = com.mycompany.app.ui.components.readableSide(), bottom = 16.dp + padding.calculateBottomPadding()),
                ) {
                    var lastKey: String? = null
                    shown.forEachIndexed { index, item ->
                        if (group != 0 && query.isBlank()) {
                            val key = item.groupKey
                            if (key != lastKey) {
                                lastKey = key
                                val title = groupTitle(context, group, item)
                                item(key = "group-$index-$key") {
                                    Text(
                                        title ?: "",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    )
                                }
                            }
                        }
                        item(key = "${item.listType}-${item.id}") {
                            val key = keyOf(item)
                            DownloadRow(
                                item = item,
                                version = versionNow,
                                selecting = selected.isNotEmpty(),
                                isSelected = key in selected,
                                onClick = {
                                    if (selected.isNotEmpty()) { if (key in selected) selected.remove(key) else selected.add(key) }
                                    else if (item.listType != 8 && item.status in intArrayOf(1, 2, 4) &&
                                        !(item.listType == 5 && item.status != 4 && !item.url.isNullOrEmpty() && item.blockPerms > 1)) toggle(item)
                                    else openItem(item)
                                },
                                onLongClick = { if (key in selected) selected.remove(key) else selected.add(key) },
                                onMore = { itemMenu = item },
                                onToggle = { toggle(item) },
                                onCancel = { recordsOf(listOf(item)) },
                                failText = if (item.status == 4) com.mycompany.app.down.DownState.failReason(context, item.id) else 0,
                                speed = if (item.status == 1 && item.id !in limited) speedText(item) else null,
                                limitedText = item.listType != 8 && (item.status == 1 || item.status == 2) && item.id in limited,
                            )
                        }
                    }
                }
                if (loaded && shown.isEmpty()) {
                    val title = when {
                        query.isNotBlank() -> R.string.no_found
                        filter != 0 -> R.string.down_empty_filter
                        else -> R.string.down_empty_none
                    }
                    com.mycompany.app.ui.components.EmptyState(
                        icon = R.drawable.outline_download_black_24,
                        title = stringResource(title),
                        hint = if (query.isBlank() && filter == 0) stringResource(R.string.down_empty_hint) else null,
                    )
                }
            }
        }

        itemMenu?.let { item ->
            // the item's actions as a sheet with icons (was a dialog of 9 text lines); one Delete with a "keep the
            // file" choice instead of "Delete file" / "Delete record" (2026-11-20)
            androidx.compose.material3.ModalBottomSheet(onDismissRequest = { itemMenu = null }) {
                Text(
                    item.name ?: "", style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp),
                )
                val black = { res: Int, dark: Int -> if (AppState.darkUi) dark else res }
                if (item.listType != 8) {
                    SheetLine(black(R.drawable.outline_apps_black_24, R.drawable.outline_apps_dark_24), stringResource(R.string.open_with)) {
                        itemMenu = null
                        if (!MainUtil.isReadableFile(context, item.path)) MainUtil.showToast(context, R.string.invalid_path)
                        else openWith(item, true)
                    }
                    SheetLine(black(R.drawable.outline_download_black_24, R.drawable.outline_download_dark_24), stringResource(R.string.down_again)) { itemMenu = null; redownload(item) }
                    SheetLine(black(R.drawable.outline_public_black_24, R.drawable.outline_public_dark_24), stringResource(R.string.visit_site)) {
                        itemMenu = null
                        startActivity(MainUtil.browserIntent(context).putExtra("EXTRA_PATH", item.referer).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                    }
                }
                SheetLine(black(R.drawable.outline_link_black_24, R.drawable.outline_link_dark_24), stringResource(R.string.copy_url)) {
                    itemMenu = null
                    MainUtil.copyTextToast(context, R.string.copied_clipboard, "Copied URL", MainUtil.firstUrlIn(item.url))
                }
                SheetLine(black(R.drawable.outline_share_black_24, R.drawable.outline_share_dark_24), stringResource(R.string.share)) { itemMenu = null; share(item) }
                SheetLine(0, stringResource(R.string.rename), Sym.Edit) { itemMenu = null; rename(item) }
                SheetLine(0, stringResource(R.string.details), Sym.Info) { itemMenu = null; details = item }
                SheetLine(0, stringResource(R.string.delete), Sym.Delete) { itemMenu = null; deleteAsk(listOf(item), false) }
                androidx.compose.foundation.layout.Spacer(Modifier.size(16.dp))
            }
        }
        details?.let { item ->
            val lines = buildString {
                append(item.path ?: "")
                if (!item.url.isNullOrEmpty()) append("\n\n").append(item.url)
                if (item.size > 0) append("\n\n").append(MainUtil.sizeLabel(item.size))
                if (item.time > 0) append("\n").append(java.text.DateFormat.getDateTimeInstance().format(java.util.Date(item.time)))
            }
            InfoDialog(item.name ?: stringResource(R.string.details), lines) { details = null }
        }
        if (sorting) {
            com.mycompany.app.ui.components.OpumSheetDialog(
                onDismissRequest = { sorting = false },
                title = { Text(stringResource(R.string.sort_by)) },
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        val keys = listOf(
                            0 to R.string.sort_name, 3 to R.string.sort_time, 4 to R.string.sort_size,
                            2 to R.string.sort_ext, 1 to R.string.sort_data,
                        )
                        keys.forEach { (key, label) ->
                            Row(
                                Modifier.fillMaxWidth().clickable {
                                    sortKey = key; ListSettings.edit(context) { set(ListSettings.bookDownItem, key) }
                                },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = sortKey == key, onClick = {
                                    sortKey = key; ListSettings.edit(context) { set(ListSettings.bookDownItem, key) }
                                })
                                Text(stringResource(label))
                            }
                        }
                        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.reverse), Modifier.weight(1f))
                            Switch(checked = reverse, onCheckedChange = {
                                reverse = it; ListSettings.edit(context) { set(ListSettings.bookDownRvse, it) }
                            })
                        }
                        Text(
                            stringResource(R.string.group_by),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                        listOf(0 to R.string.group_none, 1 to R.string.group_folder, 3 to R.string.type).forEach { (value, label) ->
                            Row(
                                Modifier.fillMaxWidth().clickable {
                                    group = value; ListSettings.edit(context) { set(ListSettings.bookDownFold, value) }
                                },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = group == value, onClick = {
                                    group = value; ListSettings.edit(context) { set(ListSettings.bookDownFold, value) }
                                })
                                Text(stringResource(label))
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { sorting = false }) { Text(stringResource(R.string.ok)) } },
            )
        }
    }

    /** Opens a finished file with the system or the built-in viewers; falls back to the "open as" sheet. */
    private fun openWith(item: ChildItem, chooser: Boolean) {
        showOpenType(item.path, MainUtil.mimeForUrl(item.name), chooser)
    }

    private fun showOpenType(path: String?, mime: String?, chooser: Boolean) {
        if (MainUtil.openWithApp(this, path, mime, false, chooser)) return
        DialogOpenType(this, 0, path, chooser)
    }

    /** Tap on a row (see the class comment). */
    private fun openItem(item: ChildItem) {
        if (!MainUtil.isReadableFile(this, item.path)) {
            MainUtil.showToast(this, R.string.invalid_path)
            return
        }
        if (item.listType == 8) {
            setResult(RESULT_OK, Intent().putExtra("EXTRA_PATH", item.path).putExtra("EXTRA_URL", item.url))
            finish()
            return
        }
        // a video that is still downloading in segments: play what is there so far
        if (item.listType == 5 && (item.status == 1 || item.status == 2) && !item.url.isNullOrEmpty() && item.blockPerms > 1) {
            val context = applicationContext
            Thread {
                val dir = MainUtil.downFilePath(context, item.path)
                val file = firstSegment(dir)
                var uri: String? = null
                if (file != null && file.length() != 0L) uri = MainUtil.fileProviderUri(context, file.path)?.toString()
                if (uri.isNullOrEmpty()) uri = item.path
                runOnUiThread { showOpenType(uri, "video/*", false) }
            }.start()
            return
        }
        showOpenType(item.path, MainUtil.mimeForUrl(item.name), false)
    }

    /** A segmented download folder holds files named "0...0v" / "0...0vf"; the first one is all zeros. */
    private fun firstSegment(dir: String?): File? {
        if (dir.isNullOrEmpty()) return null
        val folder = File(dir)
        if (!folder.isDirectory) return null
        val name = folder.listFiles()?.firstOrNull()?.name ?: return null
        var digits = name.length
        val suffix = when {
            name.endsWith("vf") -> { digits -= 2; "vf" }
            name.endsWith("v") -> { digits -= 1; "v" }
            else -> return null
        }
        if (digits == 0) return null
        return File(dir + "/" + "0".repeat(digits) + suffix)
    }

    /** "Download again": straight into the default folder when one is set, else the download dialogs (as before). */
    private fun redownload(item: ChildItem) {
        if (!AlbumSettings.dappPkg.value.isNullOrEmpty() && !AlbumSettings.dappCls.value.isNullOrEmpty()) {
            startDownload(AlbumSettings.dappPkg.value, AlbumSettings.dappCls.value, item.url, item.referer, item.name, MainUtil.mimeForUrl(item.name))
            return
        }
        val referer = item.referer
        if (item.listType == 4 && MainUri.isContentUri(item.url)) {
            // an image saved from a content URI: copy it into a folder of your choice
            var dialog: DialogDownFile? = null
            com.mycompany.app.ui.prefs.DialogShow.direct(
                this as android.app.Activity,
                requestCodes = intArrayOf(19),
                onResult = { code, result, data ->
                    val dialogNow = dialog
                    if (dialogNow != null && code == 19 && result == RESULT_OK && data != null) {
                        val uri = data.data
                        val path = if (uri == null) null else MainUri.treeDocUri(uri)
                        if (path.isNullOrEmpty()) MainUtil.showToast(dialogNow.appContext, R.string.invalid_path)
                        else { dialogNow.setFolderPath(path); MainUtil.keepUriPermission(dialogNow.appContext, uri) }
                    }
                },
            ) { host ->
                DialogDownFile(host, item.url, referer, MainUtil.baseNameAndExt(item.name), object : DownUrlListener {
                    override fun currentWeb(): WebNestView? = null
                    override fun openApp(url: String?, name: String?) {}
                    override fun openWith(url: String?, name: String?, dirName: String?) {}
                    override fun onDownload(url: String?, uriItem: UriItem?, type: Int, isAudio: Boolean, name: String?, dirName: String?) {
                        dialog?.dismiss()
                        if (uriItem != null) copyLocalDownload(url, uriItem.uriString, dirName)
                    }
                    override fun openInApp(appCode: Int, url: String?) {}
                    override fun preview(url: String?, name: String?, dirName: String?, isImage: Boolean) {
                        showPreview(url, referer, name, dirName)
                    }
                    override fun share(url: String?, name: String?, dirName: String?) {}
                }).also { dialog = it }
            }
            return
        }
        var dialog: DialogDownUrl? = null
        com.mycompany.app.ui.prefs.DialogShow.direct(
            this as android.app.Activity,
            requestCodes = intArrayOf(19),
            onResult = { code, result, data -> dialog?.onFolderResult(code, result, data) },
        ) { host ->
            DialogDownUrl(
                host, item.url, referer, MainUtil.baseNameAndExt(item.name), null, null, item.size, item.listType,
                0, null, 0, null, 0,
                object : DownUrlListener {
                    override fun currentWeb(): WebNestView? = null
                    override fun openApp(url: String?, name: String?) {
                        dialog?.dismiss()
                        MainUtil.shareText(this@DownloadListActivity, url, name)
                    }
                    override fun openWith(url: String?, name: String?, dirName: String?) {
                        dialog?.dismiss()
                        showSetDown(url, referer, name, dirName)
                    }
                    override fun onDownload(url: String?, uriItem: UriItem?, type: Int, isAudio: Boolean, name: String?, dirName: String?) {
                        dialog?.dismiss()
                        if (uriItem == null) return
                        if (type == 3) {
                            MainUtil.showToast(this@DownloadListActivity, R.string.invalid_url)
                            return
                        }
                        val app = MainApp.from(applicationContext)
                        if (app == null) {
                            MainUtil.showToast(this@DownloadListActivity, R.string.down_fail)
                            return
                        }
                        app.startDownload(url, dirName, uriItem, if (type == 1) false else isAudio)
                        reloadRequest++
                    }
                    override fun openInApp(appCode: Int, url: String?) {}
                    override fun preview(url: String?, name: String?, dirName: String?, isImage: Boolean) {
                        if (isImage) {
                            if (MainUtil.quickViewPdf(this@DownloadListActivity, url)) dialog?.dismiss()
                            return
                        }
                        if (!dirName.isNullOrEmpty() && dirName.startsWith("text")) {
                            startActivity(
                                Intent(applicationContext, com.mycompany.app.ui.viewer.TextViewerActivity::class.java)
                                    .putExtra("EXTRA_PATH", url).putExtra("EXTRA_NAME", name)
                            )
                            return
                        }
                        showPreview(url, referer, name, dirName)
                    }
                    override fun share(url: String?, name: String?, dirName: String?) {}
                },
            ).also { dialog = it }
        }
    }

    /** Copies a local file of an old download to the chosen place and records it in the download list. */
    private fun copyLocalDownload(src: String?, dest: String?, name: String?) {
        val context = applicationContext
        MainUtil.showToast(this, R.string.down_start)
        Thread {
            val ok = MainUtil.copyFileOrUri(context, src, dest)
            if (ok) {
                val uriItem = MainUri.createIn(context, dest, MainUri.storageVolume())
                if (uriItem != null) DbBookDown.addFile(context, src, name, uriItem)
            }
            runOnUiThread {
                MainUtil.showToast(this, if (ok) R.string.down_complete else R.string.down_fail)
                reloadRequest++
            }
        }.start()
    }

    /** `MainUtil.openWithChooser` wants the legacy activity type, so it runs on the (invisible) dialog host that closes right after. */
    private fun startDownload(dir: String?, dname: String?, url: String?, referer: String?, name: String?, mime: String?) {
        MainUtil.openWithChooser(this, dir, dname, url, referer, name, mime)
    }

    private fun showSetDown(url: String?, referer: String?, name: String?, mime: String?) {
        DialogSetDown(this, url, mime, true, false, false, 0, object : SetDownListener {
            override fun onPick(label: String?, packageName: String?, className: String?) {
                startDownload(packageName, className, url, referer, name, mime)
            }
        })
    }

    private fun showPreview(url: String?, referer: String?, name: String?, mime: String?) {
        if (url.isNullOrEmpty()) {
            MainUtil.showToast(this, R.string.invalid_url)
            return
        }
        var dialog: DialogPreview? = null
        com.mycompany.app.ui.prefs.DialogShow.direct(this as android.app.Activity) { host ->
            DialogPreview(host, url, referer, null, mime, object : PreviewListener {
                override fun copyUrl(url: String?) {
                    MainUtil.copyTextToast(this@DownloadListActivity, R.string.copied_clipboard, "Copied URL", url)
                }
                override fun openFile(url: String?, size: Long, isVideo: Boolean) {
                    dialog?.dismiss()
                    if (isVideo) {
                        startActivity(
                            Intent(applicationContext, com.mycompany.app.ui.viewer.ImagePreviewActivity::class.java)
                                .putExtra("EXTRA_PATH", url).putExtra("EXTRA_REFERER", referer).putExtra("EXTRA_POPUP", false)
                        )
                    } else {
                        startActivity(
                            Intent(applicationContext, com.mycompany.app.ui.viewer.VideoPlayerActivity::class.java)
                                .setData(android.net.Uri.parse(url)).putExtra("EXTRA_POPUP", true)
                                .putExtra("EXTRA_HOST", MainUtil.hostOfUrl(url, true)).putExtra("EXTRA_INDEX", 0)
                        )
                    }
                }
                override fun download(url: String?, mimeType: String?) {
                    dialog?.dismiss()
                    showSetDown(url, referer, name, mimeType)
                }
                override fun share(url: String?) {
                    dialog?.dismiss()
                    MainUtil.shareText(this@DownloadListActivity, url, referer)
                }
                override fun openWith(url: String?, mimeType: String?) {
                    dialog?.dismiss()
                }
            }).also { dialog = it }
        }
    }

    /** "Delete file": removes the files and the records (the old file-delete sheet). */
    private fun deleteFiles(list: List<ChildItem>) {
        var dialog: DialogFileDelete? = null
        dialog = DialogFileDelete(this, 31, ArrayList<Any?>(list), object : FileDeleteListener {
            override fun onCancelled() { reloadRequest++ }
            override fun onDeleted(isOk: Boolean) {
                reloadRequest++
                dialog?.dismiss()
            }
        })
    }

    /** "Delete record": removes only the list entries, keeps the files. [all] only when nothing is filtered. */
    private fun deleteRecords(list: List<ChildItem>, all: Boolean) {
        var dialog: DialogDeleteBook? = null
        dialog = DialogDeleteBook(
            this, 31, ArrayList<Any?>(list), null, all, false,
            object : DeleteBookListener {
                override fun onCancelled() { reloadRequest++ }
                override fun onDeleted() {
                    reloadRequest++
                    dialog?.dismiss()
                }
            },
        )
    }

    /** One Delete: the sheet confirms and offers "Keep the file" (unchecked: the file goes too). */
    private fun deleteAsk(list: List<ChildItem>, all: Boolean) {
        var dialog: DialogDeleteBook? = null
        dialog = DialogDeleteBook(
            this, 31, ArrayList<Any?>(list), null, all, true,
            object : DeleteBookListener {
                override fun onCancelled() { reloadRequest++ }
                override fun onDeleted() {
                    reloadRequest++
                    dialog?.dismiss()
                }
            },
        )
        if (list.any { it.listType != 8 }) dialog.askKeepFile()
    }

    @Composable
    private fun SheetLine(iconRes: Int, label: String, vector: androidx.compose.ui.graphics.vector.ImageVector? = null, onClick: () -> Unit) {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (vector != null) Icon(vector, null, modifier = Modifier.size(24.dp))
            else androidx.compose.foundation.Image(painterResource(iconRes), null, modifier = Modifier.size(24.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 20.dp))
        }
    }

    private fun rename(item: ChildItem) {
        DialogFileRename(this, 31, item, object : FileRenameListener {
            override fun onRenamed(id: Long, newPath: String?, listType: Int) {
                // an item of another type than the filter moves the filter back to "all", like before
                if (ListSettings.bookDownType.value != 0 && ListSettings.bookDownType.value != listType) {
                    ListSettings.edit(applicationContext) { set(ListSettings.bookDownType, 0) }
                }
                focusId = id
                reloadRequest++
            }
        })
    }

    private fun share(item: ChildItem) {
        MainUtil.shareFile(31, this, item.path, item.name, MainUtil.mimeForUrl(item.name))
    }

    /** Group header text, same rule as the old list. */
    private fun groupTitle(context: Context, group: Int, item: ChildItem): String? {
        if (item.status == 1 || item.status == 2 || item.status == 4) return context.getString(R.string.download)
        if (group != 3) return item.dirName
        val type = if (item.listType == 2) 7 else item.listType
        val index = MainConst.downFilterValues.indexOf(type).let { if (it < 0) MainConst.downFilterValues.size - 1 else it }
        return context.getString(MainConst.downFilterNames[index % MainConst.downFilterNames.size])
    }

    /** Same query as the old list: downloads (+ saved pages for the all/page filters), sorted like the old list. */
    private fun loadItems(context: Context): List<ChildItem> {
        val hidden = SyncSettings.secretMode.value && SecretSettings.secretDown.value == 0L
        if (hidden) return emptyList()
        val minTime = if (SyncSettings.secretMode.value && SecretSettings.secretDown.value > 0) SecretSettings.secretDown.value else 0L
        val out = ArrayList<ChildItem?>()
        val task = ListBookDown.ListTask(null, true)
        task.loadDownloads(context, minTime, true, out)
        if (ListSettings.bookDownType.value == 0 || ListSettings.bookDownType.value == 8) task.loadPages(context, minTime, true, out)
        try {
            Collections.sort(out, MainUtil.childComparator(ListSettings.bookDownFold.value, ListSettings.bookDownItem.value, ListSettings.bookDownRvse.value))
        } catch (_: Exception) {
        }
        val list = out.filterNotNull()
        val cache = DataBookDown.instance(context)
        cache.items = ArrayList<ChildItem?>(list)
        cache.itemIds = ArrayList(list.map { if (it.listType == 8) -it.id else it.id })
        return list
    }
}

@Composable
private fun MenuLine(label: String, onClick: () -> Unit) {
    Text(
        label,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DownloadRow(
    item: ChildItem,
    version: Int,
    selecting: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMore: () -> Unit,
    onToggle: () -> Unit,
    onCancel: () -> Unit,
    /** Why it failed (string resource), 0 = unknown. */
    failText: Int = 0,
    /** "2.1 MB/s · 15 s left" while running. */
    speed: String? = null,
    /** The site is limiting downloads (429): waiting to retry. */
    limitedText: Boolean = false,
) {
    @Suppress("UNUSED_VARIABLE") val tick = version
    val status = item.status
    val active = item.listType != 8 && (status == 1 || status == 2 || status == 4)
    val dark = AppState.darkUi
    AppListItem(
        leadingContent = {
            if (selecting) {
                Checkbox(checked = isSelected, onCheckedChange = { onClick() })
            } else {
                val res = if (item.iconRes != 0) item.iconRes else R.drawable.outline_public_black_24
                Icon(painterResource(res), contentDescription = null, tint = androidx.compose.ui.graphics.Color.Unspecified, modifier = Modifier.size(32.dp))
            }
        },
        headlineContent = { Text(item.name ?: "", maxLines = 2, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            when {
                item.listType == 8 || status == 3 -> {
                    val date = if (item.time > 0) MainUtil.formatDate(item.time, null, "yyyy.MM.dd") else null
                    val size = if (item.size > 0) MainUtil.sizeLabel(item.size) else null
                    Text(listOfNotNull(date, size).joinToString("   "))
                }
                status == 5 -> Text(stringResource(R.string.invalid_url))
                else -> Column {
                    val known = item.size > 0
                    if (known) {
                        LinearProgressIndicator(
                            progress = { (item.readSize.toFloat() / item.size).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        )
                    } else {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                    }
                    Text(
                        (if (known) MainUtil.sizePairText(item.readSize, item.size) else MainUtil.sizeLabel(item.readSize)) + "   " +
                            when (status) {
                                2 -> stringResource(if (item.isSecret) R.string.reserved else R.string.paused)
                                1 -> speed ?: ""
                                else -> ""
                            },
                        style = MaterialTheme.typography.labelMedium,
                    )
                    if (limitedText) {
                        Text(
                            stringResource(R.string.down_rate_waiting),
                            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                    if (status == 4) {
                        Text(
                            stringResource(if (failText != 0) failText else R.string.fail),
                            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        },
        trailingContent = if (selecting) null else if (!active) {
            {
                IconButton(onClick = onMore) { Icon(Sym.MoreVert, contentDescription = null) }
            }
        } else {
            {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (status == 4) IconButton(onClick = onToggle) {
                        Icon(Sym.Refresh, contentDescription = stringResource(R.string.retry))
                    } else IconButton(onClick = onToggle) {
                        val running = status == 1
                        Icon(
                            painterResource(
                                if (running) (if (dark) R.drawable.baseline_pause_dark_24 else R.drawable.baseline_pause_black_24)
                                else (if (dark) R.drawable.baseline_play_arrow_dark_24 else R.drawable.baseline_play_arrow_black_24)
                            ),
                            contentDescription = stringResource(if (running) R.string.pause else R.string.resume),
                            tint = androidx.compose.ui.graphics.Color.Unspecified,
                        )
                    }
                    IconButton(onClick = onCancel) {
                        Icon(Sym.Close, contentDescription = stringResource(R.string.cancel))
                    }
                }
            }
        },
        modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
    )
}
