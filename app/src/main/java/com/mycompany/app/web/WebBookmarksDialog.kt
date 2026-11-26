package com.mycompany.app.web

import com.mycompany.app.ui.components.EmptyState
import com.mycompany.app.main.RequestCode
import android.content.ContentValues
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ListSettings
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ReadSettings
import android.content.Intent
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.data.book.DataBookSearch
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookWeb
import com.mycompany.app.dialog.DialogInfo
import com.mycompany.app.dialog.DialogSetDown
import com.mycompany.app.dialog.DialogSetDown.SetDownListener
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.dialog.DialogSetSort2
import com.mycompany.app.dialog.DialogWebBookDir
import com.mycompany.app.dialog.DialogWebBookEdit
import com.mycompany.app.dialog.DialogWebBookEdit.BookEditListener
import com.mycompany.app.dialog.DialogWebBookMove
import com.mycompany.app.dialog.DialogWebBookMove.BookMoveListener
import com.mycompany.app.list.ListTask.ListTaskConfig
import com.mycompany.app.list.ListTask.ListTaskSimpleListener
import com.mycompany.app.list.book.ListBookWeb
import com.mycompany.app.main.BitmapUtil
import com.mycompany.app.main.JarBridge
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainItem.ViewItem
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainListLoader.ListLoadListener
import com.mycompany.app.data.transfer.BrowserTransfer
import com.mycompany.app.data.transfer.TransferSummary
import com.mycompany.app.ui.settings.TransferSummaryDialog
import com.mycompany.app.ui.settings.showExportResult
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.ViewItemListener
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefList
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefUtil
import com.mycompany.app.ui.browser.ComposeFullDialog
import com.mycompany.app.view.MyDialogBottom

import java.util.regex.Pattern
import kotlin.math.roundToInt
import com.mycompany.app.ui.components.Sym

/**
 * The browser's Bookmarks and the bookmark folder picker (replaces `DialogWebBookList` + `MainListView2`). Same
 * constructor as the old dialog: [startDir] is the folder to open, [mode] 0 = Bookmarks, 3 = "Move to" (pick the
 * destination for [list]; the button is off while the folder is the one the items are in), 6 = "Save location" (pick
 * a folder for the bookmark being edited). [listener]`.a(folder)` gets the picked folder (3 / 6), `.b(action, url)`
 * opens a bookmark (2 = open, 3 = new tab, 4 = group tab, 5 = background tab).
 *
 * Bookmarks (mode 0): path bar (home + folders), ".." to go up, rows from the old loader `ListBookWeb` (same sorting,
 * secret-mode bookmarks). Tap opens a bookmark or a folder. Row menu (bookmark): Move to top / bottom (custom order
 * only), New tab, Group tab, Background tab, Copy URL, Delete, Move, Edit, Share, Open with, Details; (folder): Move
 * to top / bottom, Delete, Move, Rename. Long-press selects (Delete, Move, Share). With custom order the handle drags
 * a row: drop between rows to reorder, on a folder to move into it, on ".." to move up, on a bookmark to make a new
 * folder holding both (as the old list did). Bar: search (all bookmarks), + (Add folder, Direct input, Add current
 * page), menu (Sort, Show details, Single line, Import / Export HTML through [BrowserTransfer] as in Backup & restore,
 * in secret mode Import normal bookmarks). Folder and bookmark dialogs (`DialogWebBookDir`, `DialogWebBookEdit`,
 * `DialogWebBookMove`, `DialogSetSort2`) are the existing ones. Closing Bookmarks remembers the folder as before.
 */
@Suppress("deprecation")
class WebBookmarksDialog(
    activity: MainActivity?,
    startDir: String?,
    list: MutableList<Any?>?,
    private val mode: Int,
    private var listener: BookListListener?,
) : ComposeFullDialog(activity) {

    interface BookListListener {
        fun onPick(path: String?)

        fun onAction(action: Int, path: String?)
    }

    interface BookInfoListener {
        val icon: Bitmap?

        val title: String?

        val url: String?
    }

    /** The current page, for "Add current page" (set by the browser). */
    @JvmField var infoListener: BookInfoListener? = null

    private val picker = mode == 3 || mode == 6
    private var act: MainActivity? = activity
    private val ui = Handler(Looper.getMainLooper())
    private var task: ListBookWeb? = null
    /** The folder the items to move are in, with a trailing slash (mode 3). */
    private var sourceDir: String? = null

    private var dir by mutableStateOf(if (TextUtils.isEmpty(startDir)) "/" else startDir!!)
    private val rows = mutableStateListOf<ChildItem>()
    private var loaded by mutableStateOf(false)
    private var showDetail by mutableStateOf(PrefUtil.showDetail(17))
    private var single by mutableStateOf(PrefUtil.singleLine(17))
    private var iconTick by mutableIntStateOf(0)
    private val selected = mutableStateListOf<Long>()
    private val marked = mutableStateListOf<Long>()
    private val icons = mutableStateMapOf<String, Bitmap>()
    private val noIcon = HashSet<String>()
    private var loader: MainListLoader? = null
    private val pending = HashMap<String, ChildItem>()
    private val punct: Pattern = Pattern.compile("\\p{Punct}")
    private var sub: android.app.Dialog? = null
    /** An HTML export is waiting for the file picker (request code 19). */
    private var exportPending = false
    /** Result of the last HTML import, shown as "Import finished". */
    private var importSummary by mutableStateOf<TransferSummary?>(null)
    private var searching by mutableStateOf(false)
    private var query by mutableStateOf("")
    private var guide by mutableStateOf(false)
    private var importing by mutableStateOf(false)
    private var scrollTo by mutableIntStateOf(-1)
    private var lostFocus = false

    // drag (custom order only)
    private var dragId by mutableLongStateOf(-1L)
    private var dragY by mutableFloatStateOf(0f)
    private var dropOn by mutableStateOf<Any?>(null)
    private var orderChanged = false

    init {
        if (mode == 3 && !list.isNullOrEmpty()) {
            val first = (list[0] as ChildItem).dir
            sourceDir = if (TextUtils.isEmpty(first) || first == "/") "/" else "$first/"
        }
        setPage { Page() }
        show()
        // after the listeners below are set up (property initialisers run in order)
        ui.post { load(true, startDir, null) }
    }

    private val taskListener = object : ListTaskSimpleListener() {
        override fun onConfig(listTaskConfig: ListTaskConfig?) {
            val all = listTaskConfig?.items?.filterNotNull() ?: emptyList()
            val folder = listTaskConfig?.folder
            val prev = listTaskConfig?.lastIndex ?: -1
            val mark = listTaskConfig?.markedIndex ?: -1
            ui.post {
                if (!TextUtils.isEmpty(folder) && !searching) dir = folder!!
                rows.clear()
                rows.addAll(all.filter { it.rowType.toInt() == 0 })
                selected.retainAll(rows.map { it.id }.toSet())
                marked.clear()
                marked.addAll(rows.filter { it.isMarked }.map { it.id })
                loaded = true
                val target = if (mark >= 0) mark else prev
                scrollTo = if (target >= 0) rows.indexOfFirst { it.position == target } else -1
                guide = mode == 0 && ReadSettings.guideSort.value && rows.isNotEmpty()
            }
        }
    }

    private val iconListener = object : ListLoadListener {
        override fun onFailed(childItem: ChildItem?, view: View?) {
            val url = childItem?.path ?: return
            ui.post { pending.remove(url); noIcon.add(url) }
        }

        override fun onLoaded(childItem: ChildItem?, view: View?, bitmap: Bitmap?) {
            val url = childItem?.path ?: return
            ui.post {
                pending.remove(url)
                if (MainUtil.isBitmapOk(bitmap)) icons[url] = bitmap!! else noIcon.add(url)
            }
        }
    }

    private fun load(force: Boolean, folder: String?, marks: ArrayList<Long>?) {
        val listBookWeb = task ?: ListBookWeb().also { it.appContext = context; it.taskListener = taskListener; task = it }
        listBookWeb.loadMarks(force, folder, null, marks)
    }

    private fun reload(folder: String? = dir, marks: ArrayList<Long>? = null) = load(true, folder, marks)

    private fun go(folder: String?) {
        val deepScan = if (TextUtils.isEmpty(folder)) "/" else folder!!
        if (deepScan == dir && loaded) return
        selected.clear()
        load(false, deepScan, null)
    }

    private fun parent(): String = if (dir == "/") "/" else (MainUtil.uriDisplayName(null, dir) ?: "/")

    private fun requestIcon(item: ChildItem) {
        if (item.isDir) return
        val url = item.path ?: return
        if (icons.containsKey(url) || noIcon.contains(url) || pending.containsKey(url)) return
        item.listType = 17 // list type: the loader reads the icon from DbBookWeb by id for 17
        val cached = try { MainListLoader.cachedIconFor(item) } catch (_: Exception) { null }
        if (MainUtil.isBitmapOk(cached)) { icons[url] = cached!!; return }
        pending[url] = item
        val current = loader ?: MainListLoader(context, false, iconListener).also { loader = it }
        current.enqueue(item, ImageView(context).also { it.tag = item.position })
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            selected.isNotEmpty() -> selected.clear()
            searching -> closeSearch()
            dir != "/" -> go(parent())
            else -> dismiss()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Back from another screen or dialog (an edit elsewhere, a bookmark added from the page): show the changes.
        if (picker) return
        if (!hasFocus) lostFocus = true else if (lostFocus) {
            lostFocus = false
            if (sub == null && !searching && dragId < 0) reload()
        }
    }

    private fun closeSearch() {
        searching = false
        query = ""
        val cache = DataBookSearch.instance(context)
        cache.items = null
        cache.itemIds = null
        task?.endSearch()
        reload()
    }

    private fun search(newQuery: String) {
        query = newQuery
        val q = newQuery.trim()
        if (q.isEmpty()) task?.search(null) else task?.search(q.lowercase())
    }

    // ------------------------------------------------------------------ actions

    private fun open(item: ChildItem, action: Int) {
        if (item.isDir) { go(item.path); return }
        if (picker) return
        if (TextUtils.isEmpty(item.path)) { MainUtil.showToast(context, R.string.invalid_path); return }
        listener?.onAction(action, item.path)
    }

    private fun show(subDialog: android.app.Dialog?) {
        if (subDialog == null) return
        sub = subDialog
        subDialog.setOnDismissListener { if (sub === subDialog) sub = null }
    }

    private val moveListener = object : BookMoveListener {
        override fun onCancelled() { selected.clear() }

        override fun onMoved(arrayList: ArrayList<Long>?, folder: String?) {
            selected.clear()
            if (searching) { searching = false; query = ""; task?.endSearch() }
            reload(folder, arrayList)
            try { sub?.dismiss() } catch (_: Exception) {}
        }
    }

    /** Delete (2), move to [folder] (3) or rename (4) with the old worker sheet. */
    private fun bookMove(list: List<ChildItem>, folder: String?, type: Int) {
        val a = act ?: return
        if (list.isEmpty() || sub != null) return
        show(DialogWebBookMove(a, ArrayList<Any?>(list), folder ?: task?.currentFolder() ?: dir, type, moveListener))
    }

    /** Move: pick the folder in a second bookmark screen, then move there. */
    private fun move(list: List<ChildItem>) {
        val a = act ?: return
        if (list.isEmpty() || sub != null) return
        val from = task?.currentFolder() ?: dir
        var picker: WebBookmarksDialog? = null
        picker = WebBookmarksDialog(a, from, ArrayList<Any?>(list), 3, object : BookListListener {
            override fun onPick(path: String?) {
                picker?.dismiss()
                if (MainUtil.sameObject(from, path)) selected.clear() else ui.post { bookMove(list, path, 3) }
            }

            override fun onAction(action: Int, path: String?) {}
        })
        picker.setOnDismissListener { if (sub === picker) sub = null }
        sub = picker
    }

    private fun edit(item: ChildItem) {
        val a = act ?: return
        if (sub != null) return
        show(DialogWebBookEdit(a, item, item.path, item.name, editListener(false)))
    }

    /** The + button / page and the row edit. */
    private fun editListener(fromPage: Boolean) = object : BookEditListener {
        override fun onSaved(id: Long, folder: String?, url: String?) {
            if (searching) { searching = false; query = ""; task?.endSearch() }
            reload(folder, arrayListOf(id))
            if (fromPage && !TextUtils.isEmpty(url)) loadPageIcon(url!!)
        }

        override val icon: Bitmap?
            get() = if (fromPage) infoListener?.icon else null
    }

    /** The page's icon address from the edit sheet: load it and store it for the bookmark (old code). */
    private fun loadPageIcon(iconUrl: String) {
        val viewItem = ViewItem()
        viewItem.itemType = 7.toByte()
        viewItem.url = MainUtil.faviconUrlFor(iconUrl)
        viewItem.cacheType = 2
        viewItem.iconUrl = iconUrl
        val ctx = context
        com.mycompany.app.glide.ThumbLoader.loadAlone(viewItem, object : ViewItemListener {
            override fun onComplete(item: ViewItem?, view: View?, bitmap: Bitmap?) {
                if (item == null || TextUtils.isEmpty(item.iconUrl) || !MainUtil.isBitmapOk(bitmap)) return
                val url = item.iconUrl
                Thread {
                    DbBookWeb.setIcon(ctx, bitmap, url)
                    ui.post { icons.clear(); noIcon.clear(); iconTick++ }
                }.start()
            }
        })
    }

    /** + > Add folder. */
    private fun addFolder() {
        if (act == null || sub != null) return
        val dialogWebBookDir = DialogWebBookDir(act)
        dialogWebBookDir.appContext = dialogWebBookDir.context
        dialogWebBookDir.parentFolder = task?.currentFolder() ?: dir
        dialogWebBookDir.moveListener = object : BookMoveListener {
            override fun onCancelled() {}
            override fun onMoved(arrayList: ArrayList<Long>?, folder: String?) {
                reload(folder, arrayList)
                try { sub?.dismiss() } catch (_: Exception) {}
            }
        }
        dialogWebBookDir.build()
        show(dialogWebBookDir)
    }

    /** + > Direct input (false) / Add current page (true). */
    private fun addBookmark(page: Boolean) {
        val a = act ?: return
        if (sub != null) return
        val info = infoListener ?: return
        var url: String? = null
        var title: String? = null
        if (page) {
            url = info.url
            if (TextUtils.isEmpty(url)) { MainUtil.showToast(context, R.string.invalid_url); return }
            val id = DbBookWeb.idByPath(context, url)
            if (id > 0) {
                MainUtil.showToast(context, R.string.already_added)
                val folder = folderOf(id) ?: return
                reload(folder, arrayListOf(id))
                return
            }
            title = info.title
        }
        val item = ChildItem()
        item.dir = task?.currentFolder() ?: dir
        show(DialogWebBookEdit(a, item, url, title, editListener(true)))
    }

    private fun folderOf(id: Long): String? {
        var cursor: Cursor? = null
        var folder: String? = null
        try {
            cursor = DbUtil.queryById(DbBookWeb.get(context).getWritableDatabase(), "DbBookWeb_table", arrayOf<String?>("_dir"), id)
            if (cursor != null && cursor.moveToFirst()) folder = cursor.getString(cursor.getColumnIndex("_dir"))
        } catch (_: Exception) {
        }
        cursor?.close()
        return if (TextUtils.isEmpty(folder)) null else folder
    }

    private fun share(list: List<ChildItem>) {
        val activity = act ?: return
        if (list.isEmpty()) return
        if (list.size > 100) { MainUtil.showToast(activity, R.string.share_limit); return }
        if (list.size == 1) {
            MainUtil.shareText(activity, list[0].path, list[0].name)
        } else {
            val sb = StringBuilder()
            for (item in list) {
                if (TextUtils.isEmpty(item.path)) continue
                if (!TextUtils.isEmpty(item.name)) sb.append(item.name).append("\n")
                sb.append(item.path).append("\n\n")
            }
            MainUtil.shareText(activity, sb.toString(), null)
        }
        selected.clear()
    }

    private fun openWith(item: ChildItem) {
        val context = act ?: return
        val url = item.path
        if (url.isNullOrEmpty()) { MainUtil.showToast(context, R.string.empty); return }
        if ("file:///android_asset/shortcut.html" == url) { MainUtil.showToast(context, R.string.not_supported_page); return }
        if (MainUtil.openInExternalApp(context, url) || sub != null) return
        show(DialogSetDown(context, url, null, context.isLandscapeScreen(), false, false, 0, object : SetDownListener {
            override fun onPick(label: String?, packageName: String?, className: String?) {
                MainUtil.openWithChooser(act, packageName, className, url, null, null, null)
            }
        }))
    }

    private fun details(item: ChildItem) {
        val a = act ?: return
        if (sub != null) return
        show(DialogInfo(a, 17, item))
    }

    private fun sort() {
        val a = act ?: return
        if (sub != null) return
        show(DialogSetSort2(a, object : DialogApplyListener {
            override fun onApply() { reload() }
        }))
    }

    /**
     * Menu > Import HTML: pick a bookmark file of any browser (Chrome, Firefox, Edge, Brave …); the result comes back
     * through [onPickResult] (code 9) and is imported by [BrowserTransfer] like in Backup & restore.
     */
    private fun importHtml() {
        if (importing) return
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.setType("*/*")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, BrowserTransfer.BOOKMARK_MIME_TYPES)
            act?.launchExternalForResult(intent, RequestCode.CAMERA_OR_FILE)
        } catch (_: Exception) {
        }
    }

    /** Menu > Export HTML: pick where to save the bookmark file; written in [onPickResult] (code 19). */
    private fun exportHtml() {
        if (importing) return
        if (BrowserTransfer.bookmarkCount(context) == 0) { MainUtil.showToast(context, R.string.no_bookmark); return }
        try {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.setType("text/html")
            intent.putExtra(Intent.EXTRA_TITLE, BrowserTransfer.exportFileName(false))
            exportPending = true
            act?.launchExternalForResult(intent, RequestCode.SAVE_LOCATION)
        } catch (_: Exception) {
            exportPending = false
        }
    }

    private fun closeGuide() {
        guide = false
        if (ReadSettings.guideSort.value) {
            ReadSettings.edit(context) { set(ReadSettings.guideSort, false) }
        }
    }

    // ------------------------------------------------------------------ custom order

    /** Store the row order (indexes count the ".." row like the old list). */
    private fun storeOrder() {
        val ctx = context
        val offset = if (dir == "/") 0 else 1
        val list = rows.toList()
        Thread {
            list.forEachIndexed { index2, item ->
                val index = index2 + offset
                if (item.position != index) {
                    item.position = index
                    item.order = index.toLong()
                    DbBookWeb.setOrder(ctx, item, index)
                }
            }
        }.start()
    }

    private fun moveTo(item: ChildItem, top: Boolean) {
        val from = rows.indexOfFirst { it.id == item.id }
        if (from < 0) return
        val to = if (top) 0 else rows.lastIndex
        if (from == to) return
        rows.add(to, rows.removeAt(from))
        marked.clear()
        marked.add(item.id)
        scrollTo = to
        storeOrder()
    }

    /**
     * A row dropped on another row: on a folder it
     * moves into it, on ".." to the parent folder, on a bookmark a "New folder" holding both is made. A moved folder
     * takes its contents along; a folder of the same name there is merged.
     */
    private fun dropInto(movedIn: ChildItem, targetIn: ChildItem?) {
        val ctx = context
        val folderNow = dir
        val names = rows.map { it.name }
        Thread {
            var moved = movedIn
            var target = targetIn
            val toParent = target == null
            if (!toParent && moved.isDir && !target.isDir) {
                val tmp = moved
                moved = target
                target = tmp
            }
            val srcDir = withSlash(moved.dir)
            var destDir: String?
            var highlight: ArrayList<Long>? = null
            if (toParent) {
                destDir = MainUtil.uriDisplayName(null, srcDir)
            } else if (target!!.isDir) {
                destDir = target.path
                highlight = arrayListOf(target.id)
            } else {
                val folder = DbBookWeb.addFolder(target.position.toLong(), ctx, target.dir, newFolderName(names), true)
                if (folder == null) { ui.post { reload() }; return@Thread }
                destDir = folder.path
                highlight = arrayListOf(folder.id)
            }
            destDir = withSlash(destDir)
            val items = ArrayList<ChildItem>()
            items.add(moved)
            if (target != null && !target.isDir) items.add(target)
            if (moved.isDir) addDescendants(moved.path + "/", items)
            val srcLen = srcDir.length
            for (item in items) {
                item.dir = withSlash(item.dir)
                if (!item.dir!!.startsWith(srcDir)) continue
                val newDir = MainUtil.trimTrailingSlash(destDir + item.dir!!.substring(srcLen))
                item.dir = if (TextUtils.isEmpty(newDir)) "/" else newDir
                if (item.isDir && DbBookWeb.hasFolder(ctx, item.dir, item.name)) {
                    DbBookWeb.deleteById(ctx, item.id)
                } else {
                    val now = System.currentTimeMillis()
                    val values = ContentValues()
                    values.put("_dir", item.dir)
                    values.put("_time", now)
                    values.put("_rsv4", now)
                    DbBookWeb.updateById(ctx, item.id, values)
                }
            }
            ui.post { reload(folderNow, highlight) }
        }.start()
    }

    private fun withSlash(d: String?): String = if (TextUtils.isEmpty(d) || d == "/") "/" else "$d/"

    private fun newFolderName(titles: List<String?>): String {
        val name = context.getString(R.string.new_folder)
        if (!titles.contains(name)) return name
        var n = 2
        while (titles.contains("$name ($n)")) n++
        return "$name ($n)"
    }

    private fun addDescendants(prefix: String, items: ArrayList<ChildItem>) {
        var cursor: Cursor? = null
        try {
            cursor = DbUtil.query(
                DbBookWeb.get(context).getWritableDatabase(), "DbBookWeb_table", null, "_secret=?",
                arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey()), null,
            )
            if (cursor != null && cursor.moveToFirst()) {
                val colId = cursor.getColumnIndex("_id")
                val colIsDir = cursor.getColumnIndex("_isdir")
                val colDir = cursor.getColumnIndex("_dir")
                val colTitle = cursor.getColumnIndex("_title")
                do {
                    val dir = cursor.getString(colDir)
                    if (!TextUtils.isEmpty(dir) && ("$dir/").startsWith(prefix)) {
                        val item = ChildItem()
                        item.dir = dir
                        item.isDir = cursor.getInt(colIsDir) == 1
                        item.name = cursor.getString(colTitle)
                        item.id = cursor.getLong(colId)
                        items.add(item)
                    }
                } while (cursor.moveToNext())
            }
        } catch (_: Exception) {
        }
        cursor?.close()
    }

    /** Drag moved to [pointerY] (list viewport coordinates): reorder over row edges, mark a drop target over a row's middle. */
    private fun dragTo(state: LazyListState, pointerY: Float) {
        val info = state.layoutInfo.visibleItemsInfo
        val over = info.firstOrNull { it.key != dragId && pointerY >= it.offset && pointerY < it.offset + it.size }
        if (over == null) { dropOn = null; return }
        val frac = (pointerY - over.offset) / over.size.toFloat()
        if (over.key == UP_KEY) { dropOn = UP_KEY; return }
        val target = rows.indexOfFirst { it.id == over.key }
        val from = rows.indexOfFirst { it.id == dragId }
        if (target < 0 || from < 0) { dropOn = null; return }
        // Middle half: drop onto the row. Past its far quarter: take its place. Its near quarter: nothing yet.
        val passed = if (target > from) frac > 0.75f else frac < 0.25f
        if (frac > 0.25f && frac < 0.75f) {
            dropOn = over.key
        } else {
            dropOn = null
            if (passed) {
                rows.add(target, rows.removeAt(from))
                orderChanged = true
            }
        }
    }

    private fun dragEnd() {
        val id = dragId
        val target = dropOn
        dragId = -1L
        dragY = 0f
        dropOn = null
        val moved = rows.firstOrNull { it.id == id }
        when {
            moved == null -> {}
            target == UP_KEY -> dropInto(moved, null)
            target is Long -> rows.firstOrNull { it.id == target }?.let { dropInto(moved, it) }
            orderChanged -> storeOrder()
        }
        orderChanged = false
    }

    // ------------------------------------------------------------------ page

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    private fun Page() {
        var moreMenu by remember { mutableStateOf(false) }
        var addMenu by remember { mutableStateOf(false) }
        var rowMenu by remember { mutableLongStateOf(-1L) }
        val listState = rememberLazyListState()
        val selecting = selected.isNotEmpty()
        val custom = ListSettings.bookWebUser.value && mode == 0 && !searching && !selecting
        @Suppress("UNUSED_VARIABLE") val iconTickNow = iconTick
        LaunchedEffect(scrollTo, rows.size) {
            val scrollToNow = scrollTo
            if (scrollToNow >= 0 && scrollToNow < rows.size) {
                listState.animateScrollToItem(scrollToNow + if (dir == "/" || searching) 0 else 1)
                scrollTo = -1
            }
        }
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
                TopAppBar(
                    title = {
                        Text(
                            when {
                                selecting -> "${selected.size} / ${rows.size}"
                                mode == 3 -> stringResource(R.string.move_to)
                                mode == 6 -> stringResource(R.string.save_location)
                                else -> stringResource(R.string.bookmark)
                            },
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { if (selecting) selected.clear() else dismiss() }) {
                            Icon(if (selecting) Sym.Close else Sym.ArrowBack, null)
                        }
                    },
                    actions = {
                        when {
                            selecting -> TextButton(onClick = {
                                if (selected.size == rows.size) selected.clear() else { selected.clear(); selected.addAll(rows.map { it.id }) }
                            }) { Text(stringResource(R.string.all)) }
                            picker -> IconButton(onClick = { addFolder() }) {
                                Icon(painterResource(R.drawable.outline_create_new_folder_black_20), stringResource(R.string.add_folder))
                            }
                            else -> {
                                IconButton(onClick = { if (searching) closeSearch() else { searching = true; query = "" } }) {
                                    Icon(if (searching) Sym.Close else Sym.Search, stringResource(R.string.search_url))
                                }
                                Box {
                                    IconButton(onClick = { addMenu = true }) { Icon(Sym.Add, stringResource(R.string.add)) }
                                    com.mycompany.app.ui.components.BubbleMenu(expanded = addMenu, onDismiss = { addMenu = false }) {
                                        DropdownMenuItem(text = { Text(stringResource(R.string.add_folder)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_create_new_folder_black_20) }, onClick = { addMenu = false; addFolder() })
                                        DropdownMenuItem(text = { Text(stringResource(R.string.direct_input)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_link_black_24) }, onClick = { addMenu = false; addBookmark(false) })
                                        DropdownMenuItem(text = { Text(stringResource(R.string.add_page)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_add_box_black_24) }, onClick = { addMenu = false; addBookmark(true) })
                                    }
                                }
                                Box {
                                    IconButton(onClick = { moreMenu = true }) { Icon(Sym.MoreVert, null) }
                                    com.mycompany.app.ui.components.BubbleMenu(expanded = moreMenu, onDismiss = { moreMenu = false }) {
                                        DropdownMenuItem(text = { Text(stringResource(R.string.sort)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_sort_black_24) }, onClick = { moreMenu = false; sort() })
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.show_detail)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_view_agenda_black_20) },
                                            trailingIcon = { Checkbox(showDetail, null) },
                                            onClick = { showDetail = !showDetail; PrefUtil.setDetail(context, 17, showDetail) },
                                        )
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.show_single)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_text_fields_black_24) },
                                            trailingIcon = { Checkbox(single, null) },
                                            onClick = { single = !single; PrefUtil.setSingleLine(context, 17, single) },
                                        )
                                        DropdownMenuItem(text = { Text(stringResource(R.string.import_html)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_download_black_20) }, onClick = { moreMenu = false; importHtml() })
                                        DropdownMenuItem(text = { Text(stringResource(R.string.export_html)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_upload_black_24) }, onClick = { moreMenu = false; exportHtml() })
                                    }
                                }
                            }
                        }
                    },
                )
                if (searching && !selecting) {
                    val focus = remember { FocusRequester() }
                    OutlinedTextField(
                        value = query, onValueChange = { search(it) }, singleLine = true,
                        label = { Text(stringResource(R.string.search_url)) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).focusRequester(focus),
                    )
                    LaunchedEffect(Unit) { try { focus.requestFocus() } catch (_: Exception) {} }
                } else {
                    PathBar()
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    if (rows.isEmpty() && loaded) {
                        // the shared empty state (2026-11-20): icon, title, hint, like the downloads list
                        EmptyState(
                            icon = if (searching) R.drawable.outline_search_black_24 else R.drawable.outline_kid_star_black_24,
                            title = stringResource(if (searching) R.string.no_found else R.string.no_bookmark),
                            hint = if (searching || dir != "/") null else stringResource(R.string.book_empty_hint),
                            hintIcon = R.drawable.baseline_star_add_black_24,
                        )
                    }
                    LazyColumn(Modifier.fillMaxSize(), state = listState) {
                        if (dir != "/" && !searching) {
                            item(key = UP_KEY) { UpRow() }
                        }
                        items(rows, key = { it.id }) { item ->
                            BookRow(item, selecting, custom, listState, rowMenu == item.id, { rowMenu = item.id }, { rowMenu = -1L })
                        }
                    }
                    importSummary?.let { TransferSummaryDialog(it) { importSummary = null } }
                    if (guide) {
                        Card(
                            onClick = { closeGuide() },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.inverseSurface),
                            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(stringResource(R.string.sort_guide_1), color = MaterialTheme.colorScheme.inverseOnSurface)
                                Text(stringResource(R.string.sort_guide_2), color = MaterialTheme.colorScheme.inverseOnSurface, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }
                if (selecting) {
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                        val chosen = rows.filter { selected.contains(it.id) }
                        TextButton(onClick = { bookMove(chosen, null, 2) }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.delete)) }
                        TextButton(onClick = { move(chosen) }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.move)) }
                        TextButton(onClick = { share(chosen.filter { !it.isDir }) }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.share)) }
                    }
                } else if (picker) {
                    HorizontalDivider()
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { dismiss() }) { Text(stringResource(R.string.cancel)) }
                        val canPick = mode != 3 || (sourceDir != null && sourceDir != withSlash(dir))
                        Button(onClick = { listener?.onPick(dir) }, enabled = canPick, modifier = Modifier.padding(start = 8.dp)) {
                            Text(stringResource(if (mode == 3) R.string.move else R.string.apply))
                        }
                    }
                }
            }
        }
    }

    /** Home + the folders of the current path (old `FragmentTabPath`); tap one to go there. */
    @Composable
    private fun PathBar() {
        val parts = dir.trim('/').split('/').filter { it.isNotEmpty() }
        val scroll = rememberScrollState()
        LaunchedEffect(dir) { scroll.animateScrollTo(scroll.maxValue) }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(scroll).padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { go("/") }, enabled = dir != "/") { Icon(Sym.Home, stringResource(R.string.home_page)) }
            var path = ""
            for ((last2, name) in parts.withIndex()) {
                path += "/$name"
                val target = path
                Text("›", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 2.dp))
                val last = last2 == parts.lastIndex
                Text(
                    name,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (last) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable(enabled = !last) { go(target) }.padding(horizontal = 8.dp, vertical = 8.dp),
                )
            }
        }
    }

    @Composable
    private fun UpRow() {
        val target = dropOn == UP_KEY
        ListItem(
            colors = if (target) ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ListItemDefaults.colors(),
            leadingContent = {
                Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHighest), contentAlignment = Alignment.Center) {
                    Icon(Sym.KeyboardArrowUp, null, tint = MaterialTheme.colorScheme.onSurface)
                }
            },
            headlineContent = { Text("..") },
            modifier = Modifier.clickable { go(parent()) },
        )
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun BookRow(
        item: ChildItem, selecting: Boolean, custom: Boolean, state: LazyListState,
        menuOpen: Boolean, openMenu: () -> Unit, closeMenu: () -> Unit,
    ) {
        val url = item.path ?: ""
        LaunchedEffect(url) { requestIcon(item) }
        val isSelected = selected.contains(item.id)
        val dragging = dragId == item.id
        val target = dropOn == item.id
        val dim = picker && !item.isDir
        val color = when {
            target -> MaterialTheme.colorScheme.primaryContainer
            isSelected -> MaterialTheme.colorScheme.secondaryContainer
            marked.contains(item.id) -> MaterialTheme.colorScheme.surfaceContainerHigh
            dragging -> MaterialTheme.colorScheme.surfaceContainerHighest
            else -> MaterialTheme.colorScheme.surface
        }
        Box(
            Modifier.zIndex(if (dragging) 1f else 0f).graphicsLayer {
                if (dragging) {
                    val now = state.layoutInfo.visibleItemsInfo.firstOrNull { it.key == item.id }
                    translationY = if (now == null) 0f else dragY - (now.offset + now.size / 2f)
                    shadowElevation = 8f
                }
            },
        ) {
            ListItem(
                colors = ListItemDefaults.colors(containerColor = color),
                leadingContent = {
                    if (selecting) Checkbox(isSelected, { toggle(item) })
                    else if (item.isDir) RoundIcon(R.drawable.outline_folder_black_24)
                    else SiteIcon(icons[url], item)
                },
                headlineContent = {
                    Text(item.name?.takeIf { it.isNotEmpty() } ?: url, maxLines = if (single) 1 else 2, overflow = TextOverflow.Ellipsis)
                },
                supportingContent = if (showDetail && !item.isDir) ({ Text(url, maxLines = 1, overflow = TextOverflow.Ellipsis) }) else null,
                trailingContent = if (selecting || picker) null else ({
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (custom) {
                            Icon(
                                Sym.Menu, null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(40.dp).padding(8.dp).pointerInput(item.id) {
                                    detectDragGestures(
                                        onDragStart = {
                                            val now = state.layoutInfo.visibleItemsInfo.firstOrNull { it.key == item.id }
                                            dragY = if (now == null) 0f else now.offset + now.size / 2f
                                            dragId = item.id
                                            orderChanged = false
                                            if (guide) closeGuide()
                                        },
                                        onDrag = { change, dragDelta ->
                                            change.consume()
                                            dragY += dragDelta.y
                                            dragTo(state, dragY)
                                        },
                                        onDragEnd = { dragEnd() },
                                        onDragCancel = { dragEnd() },
                                    )
                                },
                            )
                        }
                        IconButton(onClick = openMenu) { Icon(Sym.MoreVert, null) }
                    }
                }),
                modifier = Modifier.alpha(if (dim) 0.5f else 1f).combinedClickable(
                    onClick = { if (selecting) toggle(item) else open(item, 2) },
                    onLongClick = if (picker || searching && item.isDir) null else ({ toggle(item) }),
                ),
            )
            com.mycompany.app.ui.components.BubbleMenu(expanded = menuOpen, onDismiss = closeMenu) {
                val index = rows.indexOfFirst { it.id == item.id }
                if (ListSettings.bookWebUser.value && !searching) {
                    if (index > 0) DropdownMenuItem(text = { Text(stringResource(R.string.move_top)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_vertical_align_top_black_24) }, onClick = { closeMenu(); moveTo(item, true) })
                    if (index in 0 until rows.lastIndex) DropdownMenuItem(text = { Text(stringResource(R.string.move_bot)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_vertical_align_bottom_black_24) }, onClick = { closeMenu(); moveTo(item, false) })
                }
                if (item.isDir) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.delete)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_delete_black_20) }, colors = com.mycompany.app.ui.components.destructiveItemColors(), onClick = { closeMenu(); bookMove(listOf(item), null, 2) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.move)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_drive_file_move_black_24) }, onClick = { closeMenu(); move(listOf(item)) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.rename)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_edit_black_24) }, onClick = { closeMenu(); bookMove(listOf(item), null, 4) })
                } else {
                    DropdownMenuItem(text = { Text(stringResource(R.string.new_url)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_add_box_black_24) }, onClick = { closeMenu(); open(item, 3) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.group_url)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_library_add_black_24) }, onClick = { closeMenu(); open(item, 4) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.back_url)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_flip_to_back_black_24) }, onClick = { closeMenu(); open(item, 5) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.copy_url)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_content_copy_black_20) }, onClick = {
                        closeMenu(); MainUtil.copyTextToast(act, R.string.copied_clipboard, "Copied URL", item.path)
                    })
                    DropdownMenuItem(text = { Text(stringResource(R.string.delete)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_delete_black_20) }, colors = com.mycompany.app.ui.components.destructiveItemColors(), onClick = { closeMenu(); bookMove(listOf(item), null, 2) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.move)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_drive_file_move_black_24) }, onClick = { closeMenu(); move(listOf(item)) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.edit)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_edit_black_24) }, onClick = { closeMenu(); edit(item) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.share)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_share_black_24) }, onClick = { closeMenu(); share(listOf(item)) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.open_with)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_apps_black_24) }, onClick = { closeMenu(); openWith(item) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.details)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_info_black_24) }, onClick = { closeMenu(); details(item) })
                }
            }
        }
    }

    private fun toggle(item: ChildItem) {
        if (selected.contains(item.id)) selected.remove(item.id) else selected.add(item.id)
    }

    @Composable
    private fun RoundIcon(res: Int) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHighest), contentAlignment = Alignment.Center) {
            Icon(painterResource(res), null, tint = MaterialTheme.colorScheme.onSurface)
        }
    }

    @Composable
    private fun SiteIcon(bitmap: Bitmap?, item: ChildItem) {
        if (bitmap != null) {
            Image(bitmap.asImageBitmap(), null, contentScale = ContentScale.Crop, modifier = Modifier.size(40.dp).clip(CircleShape))
            return
        }
        val letters = MainUtil.stripPunctuation(item.subText ?: item.name, punct) ?: ""
        Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHighest), contentAlignment = Alignment.Center) {
            if (letters.isEmpty()) {
                Icon(painterResource(R.drawable.outline_public_black_24), null, tint = MaterialTheme.colorScheme.onSurface)
            } else {
                Text(letters, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }

    // ------------------------------------------------------------------ contract kept for WebViewActivity

    /** Back already handled inside. */
    fun hasPopup(): Boolean = false

    /** Activity results: 9 = bookmark file to import picked, 19 = file to export into picked. */
    fun onPickResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        if (requestCode == RequestCode.SAVE_LOCATION) {
            if (!exportPending) return false
            exportPending = false
            val uri = intent?.data
            if (resultCode == -1 && uri != null) {
                val ctx = context.applicationContext
                Thread {
                    val count = BrowserTransfer.exportToUri(ctx, uri, false)
                    ui.post { showExportResult(ctx, count) }
                }.start()
            }
            return true
        }
        if (requestCode != RequestCode.CAMERA_OR_FILE) return false
        val uri = intent?.data
        if (resultCode == -1 && uri != null && !importing) {
            importing = true
            val ctx = context.applicationContext
            Thread {
                val summary = BrowserTransfer.importFromUri(ctx, uri, false)
                ui.post {
                    importing = false
                    if (summary == null) {
                        MainUtil.showToast(ctx, R.string.invalid_file)
                    } else {
                        importSummary = summary
                        if (summary.imported > 0) reload()
                    }
                }
            }.start()
        }
        return true
    }

    /** Old configuration hook; the Compose page re-lays itself out. */
    @Suppress("UNUSED_PARAMETER")
    fun onConfig(configuration: Configuration?) {}

    /** True closes the sheets opened from here. */
    fun closeSubs(isClosing: Boolean) {
        if (!isClosing) return
        try { sub?.dismiss() } catch (_: Exception) {}
        sub = null
        selected.clear()
    }

    /** Old refresh hook. */
    @Suppress("UNUSED_PARAMETER")
    fun refresh(deepScan: Boolean) { reload() }

    override fun dismiss() {
        if (mode == 0) rememberFolder()
        task?.cancel()
        task = null
        try { sub?.dismiss() } catch (_: Exception) {}
        sub = null
        exportPending = false
        if (searching) {
            val cache = DataBookSearch.instance(context)
            cache.items = null
            cache.itemIds = null
        }
        loader?.release()
        loader = null
        listener = null
        act = null
        super.dismiss()
    }

    /** The next Bookmarks opens in this folder. */
    private fun rememberFolder() {
        val folder = task?.currentFolder() ?: return
        run {
            if (MainUtil.sameObject(AlbumSettings.webBookDir.value, folder)) return
            AlbumSettings.edit(context) { set(AlbumSettings.webBookDir, folder) }
        }
    }

    private companion object {
        const val UP_KEY = "up"
    }
}
