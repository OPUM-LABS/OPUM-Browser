package com.mycompany.app.web

import com.mycompany.app.ui.components.BubbleMenu
import com.mycompany.app.ui.components.BubbleMenuItem
import com.mycompany.app.dialog.ListBookListener
import android.app.Activity
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogDeleteBook
import com.mycompany.app.dialog.DialogDeleteBook.DeleteBookListener
import com.mycompany.app.dialog.DialogInfo
import com.mycompany.app.dialog.DialogSetDown
import com.mycompany.app.dialog.DialogSetDown.SetDownListener
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.dialog.DialogSetHistory
import com.mycompany.app.dialog.DialogSetHistory.SetHistoryListener
import com.mycompany.app.dialog.DialogSetSort
import com.mycompany.app.list.ListTask.ListTaskConfig
import com.mycompany.app.list.ListTask.ListTaskSimpleListener
import com.mycompany.app.list.book.ListBookHistory
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainItem.GroupItem
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainListLoader.ListLoadListener
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefUtil
import com.mycompany.app.ui.browser.ComposeFullDialog
import com.mycompany.app.view.MyDialogBottom
import java.util.regex.Pattern
import com.mycompany.app.ui.components.Sym

/**
 * The browser's History (main menu > History; replaces the type 18 page of `DialogListBook` + `MainListView`):
 * visits grouped by day (the old loader `ListBookHistory`: same sort settings, secret-mode history, nothing when
 * history is off), each row with the site icon (or its letters), title and, with "Show details", the address.
 * Tap opens it like before (the browser decides tab / current page); the row menu has New tab / Group tab /
 * Background tab / Copy URL / Delete / Share / Open with / Details; long-press selects rows (select all, Delete,
 * Share as "title + address" text). The bar has search, and the menu Sort, Show details, Single line, History
 * retention (`DialogSetHistory`) and Delete all. Contract with `WebViewActivity`:
 * [listener] gets (-1, item, action) with action 2 = open, 3 = new tab, 4 = group tab, 5 = background tab.
 */
@Suppress("deprecation")
class WebHistoryDialog(activity: MainActivity?, private var listener: ListBookListener?) : ComposeFullDialog(activity) {

    private var act: MainActivity? = activity
    private val ui = Handler(Looper.getMainLooper())
    private var task: ListBookHistory? = null
    private var groups by mutableStateOf<List<GroupItem>>(emptyList())
    private var rows by mutableStateOf<List<ChildItem>>(emptyList())
    private var loaded by mutableStateOf(false)
    private var showDetail by mutableStateOf(PrefUtil.showDetail(18))
    private var single by mutableStateOf(PrefUtil.singleLine(18))
    private var iconTick by mutableIntStateOf(0)
    private val selected = mutableStateListOf<Long>()
    private val icons = mutableStateMapOf<String, Bitmap>()
    private val noIcon = HashSet<String>()
    private var loader: MainListLoader? = null
    private val pending = HashMap<String, ChildItem>()
    private val punct: Pattern = Pattern.compile("\\p{Punct}")
    private var sub: android.app.Dialog? = null
    private var searching by mutableStateOf(false)
    private var query by mutableStateOf("")

    /** Back: leave selection, then search, then close (the old list did the same). */
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when {
            selected.isNotEmpty() -> selected.clear()
            searching -> { searching = false; query = "" }
            else -> dismiss()
        }
    }

    private val taskListener = object : ListTaskSimpleListener() {
        override fun onConfig(listTaskConfig: ListTaskConfig?) {
            val filterNotNull = listTaskConfig?.groups?.filterNotNull()?.filterIsInstance<GroupItem>() ?: emptyList()
            val list = listTaskConfig?.items?.filterNotNull() ?: emptyList()
            ui.post {
                groups = filterNotNull
                rows = list
                selected.retainAll(list.map { it.id }.toSet())
                loaded = true
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

    init {
        setPage { Page() }
        show()
        reload()
    }

    private fun reload() {
        task?.cancel()
        val listBookHistory = ListBookHistory().also { it.appContext = context; it.taskListener = taskListener }
        task = listBookHistory
        listBookHistory.load(false, false, false)
    }

    /** Site icon from the old icon cache, else loaded once through the old loader. */
    private fun requestIcon(item: ChildItem) {
        val url = item.path ?: return
        if (icons.containsKey(url) || noIcon.contains(url) || pending.containsKey(url)) return
        item.listType = 18 // list type: the loader reads the icon from DbBookIcon by host for 18
        val cached = try { MainListLoader.cachedIconFor(item) } catch (_: Exception) { null }
        if (MainUtil.isBitmapOk(cached)) { icons[url] = cached!!; return }
        pending[url] = item
        val current = loader ?: MainListLoader(context, false, iconListener).also { loader = it }
        current.enqueue(item, ImageView(context).also { it.tag = item.position })
    }

    private fun open(item: ChildItem, action: Int) {
        listener?.onItem(-1, item, action)
    }

    private fun share(list: List<ChildItem>) {
        val activity = act ?: return
        if (list.isEmpty()) return
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

    private fun delete(list: List<ChildItem>, all: Boolean) {
        if (list.isEmpty() || sub != null) return
        val dialogDeleteBook = DialogDeleteBook(act, 18, ArrayList<Any?>(list), null, all, true, object : DeleteBookListener {
            override fun onCancelled() {}
            override fun onDeleted() {
                selected.clear()
                reload()
                sub?.dismiss()
            }
        })
        dialogDeleteBook.setOnDismissListener { sub = null }
        sub = dialogDeleteBook
    }

    /** Open with (old popup 14): the download / open-with sheet. */
    private fun openWith(item: ChildItem) {
        val context = act ?: return
        val url = item.path
        if (url.isNullOrEmpty()) { MainUtil.showToast(context, R.string.empty); return }
        if ("file:///android_asset/shortcut.html" == url) { MainUtil.showToast(context, R.string.not_supported_page); return }
        if (MainUtil.openInExternalApp(context, url) || sub != null) return
        val dialogSetDown = DialogSetDown(context, url, null, context.isLandscapeScreen(), false, false, 0, object : SetDownListener {
            override fun onPick(label: String?, packageName: String?, className: String?) {
                MainUtil.openWithChooser(act, packageName, className, url, null, null, null)
            }
        })
        dialogSetDown.setOnDismissListener { sub = null }
        sub = dialogSetDown
    }

    private fun details(item: ChildItem) {
        val a = act ?: return
        if (sub != null) return
        val dialogInfo = DialogInfo(a, 18, item)
        dialogInfo.setOnDismissListener { sub = null }
        sub = dialogInfo
    }

    private fun sort() {
        val a = act ?: return
        if (sub != null) return
        var sortDialog: DialogSetSort? = null
        sortDialog = DialogSetSort(a, 18, object : DialogApplyListener {
            override fun onApply() { sortDialog?.dismiss(); reload() }
        })
        sortDialog.setOnDismissListener { sub = null }
        sub = sortDialog
    }

    private fun retention() {
        val a = act ?: return
        if (sub != null) return
        val dialogSetHistory = DialogSetHistory(a, object : SetHistoryListener {
            override fun onChanged(changed: Boolean) { if (changed) reload() }
        })
        dialogSetHistory.setOnDismissListener { sub = null; reload() }
        sub = dialogSetHistory
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    private fun Page() {
        var moreMenu by remember { mutableStateOf(false) }
        var rowMenu by remember { mutableLongStateOf(-1L) }
        val selecting = selected.isNotEmpty()
        @Suppress("UNUSED_VARIABLE") val iconTickNow = iconTick
        val q = query.trim()
        val shown = if (q.isEmpty()) rows else rows.filter {
            (it.name ?: "").contains(q, ignoreCase = true) || (it.path ?: "").contains(q, ignoreCase = true)
        }
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
                TopAppBar(
                    title = {
                        Text(if (selecting) "${selected.size} / ${rows.size}" else stringResource(R.string.history))
                    },
                    navigationIcon = {
                        IconButton(onClick = { if (selecting) selected.clear() else dismiss() }) {
                            Icon(if (selecting) Sym.Close else Sym.ArrowBack, null)
                        }
                    },
                    actions = {
                        if (selecting) {
                            TextButton(onClick = {
                                if (selected.size == rows.size) selected.clear() else { selected.clear(); selected.addAll(rows.map { it.id }) }
                            }) { Text(stringResource(R.string.all)) }
                        } else {
                            IconButton(onClick = { searching = !searching; if (!searching) query = "" }) {
                                Icon(if (searching) Sym.Close else Sym.Search, stringResource(R.string.search_url))
                            }
                            Box {
                                IconButton(onClick = { moreMenu = true }) { Icon(Sym.MoreVert, null) }
                                com.mycompany.app.ui.components.BubbleMenu(expanded = moreMenu, onDismiss = { moreMenu = false }) {
                                    DropdownMenuItem(text = { Text(stringResource(R.string.sort)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_sort_black_24) }, onClick = { moreMenu = false; sort() })
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.show_detail)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_view_agenda_black_20) },
                                        trailingIcon = { Checkbox(showDetail, null) },
                                        onClick = { showDetail = !showDetail; PrefUtil.setDetail(context, 18, showDetail) },
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.show_single)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_text_fields_black_24) },
                                        trailingIcon = { Checkbox(single, null) },
                                        onClick = { single = !single; PrefUtil.setSingleLine(context, 18, single) },
                                    )
                                    DropdownMenuItem(text = { Text(stringResource(R.string.history_time)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_history_black_24) }, onClick = { moreMenu = false; retention() })
                                    DropdownMenuItem(text = { Text(stringResource(R.string.delete_all)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_delete_sweep_black_24) }, colors = com.mycompany.app.ui.components.destructiveItemColors(), onClick = { moreMenu = false; delete(rows, true) })
                                }
                            }
                        }
                    },
                )
                if (searching && !selecting) {
                    val focus = androidx.compose.runtime.remember { androidx.compose.ui.focus.FocusRequester() }
                    OutlinedTextField(
                        value = query, onValueChange = { query = it }, singleLine = true,
                        label = { Text(stringResource(R.string.search_url)) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                            .focusRequester(focus),
                    )
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        try { focus.requestFocus() } catch (_: Exception) {}
                    }
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    if (shown.isEmpty() && loaded) {
                        com.mycompany.app.ui.components.EmptyState(icon = R.drawable.outline_history_black_24, title = stringResource(if (q.isEmpty()) R.string.no_history else R.string.down_empty_filter))
                    }
                    LazyColumn(Modifier.fillMaxSize()) {
                        var lastGroup = -1
                        for (item in shown) {
                            val groupIndex = item.groupIndex
                            if (groupIndex != lastGroup && q.isEmpty()) {
                                lastGroup = groupIndex
                                val label = groups.getOrNull(groupIndex)?.title
                                if (!label.isNullOrEmpty()) {
                                    item(key = "g$groupIndex") {
                                        Text(
                                            label, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                                        )
                                    }
                                }
                            }
                            item(key = item.id) {
                                HistoryRow(item, selecting, rowMenu == item.id, { rowMenu = item.id }, { rowMenu = -1L })
                            }
                        }
                    }
                }
                if (selecting) {
                    HorizontalDivider()
                    androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                        val chosen = rows.filter { selected.contains(it.id) }
                        TextButton(onClick = { delete(chosen, chosen.size == rows.size) }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.delete)) }
                        TextButton(onClick = { share(chosen) }, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.share)) }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun HistoryRow(item: ChildItem, selecting: Boolean, menuOpen: Boolean, openMenu: () -> Unit, closeMenu: () -> Unit) {
        val url = item.path ?: ""
        LaunchedEffect(url) { requestIcon(item) }
        val isSelected = selected.contains(item.id)
        Box {
            ListItem(
                colors = if (isSelected) ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.secondaryContainer) else ListItemDefaults.colors(),
                leadingContent = {
                    if (selecting) {
                        Checkbox(isSelected, { toggle(item) })
                    } else {
                        SiteIcon(icons[url], item)
                    }
                },
                headlineContent = {
                    Text(item.name?.takeIf { it.isNotEmpty() } ?: url, maxLines = if (single) 1 else 2, overflow = TextOverflow.Ellipsis)
                },
                supportingContent = if (showDetail) ({ Text(url, maxLines = 1, overflow = TextOverflow.Ellipsis) }) else null,
                trailingContent = if (selecting) null else ({
                    // the menu belongs to the ⋮ (it opened at the row's start, far from the button; 2026-11-20)
                    Box {
                        IconButton(onClick = openMenu) { Icon(Sym.MoreVert, null) }
                        BubbleMenu(expanded = menuOpen, onDismiss = closeMenu) {
                            BubbleMenuItem(stringResource(R.string.new_url), { closeMenu(); open(item, 3) }, icon = R.drawable.outline_add_box_black_24)
                            BubbleMenuItem(stringResource(R.string.group_url), { closeMenu(); open(item, 4) }, icon = R.drawable.outline_library_add_black_24)
                            BubbleMenuItem(stringResource(R.string.back_url), { closeMenu(); open(item, 5) }, icon = R.drawable.outline_flip_to_back_black_24)
                            BubbleMenuItem(stringResource(R.string.copy_url), {
                                closeMenu(); MainUtil.copyTextToast(act, R.string.copied_clipboard, "Copied URL", item.path)
                            }, icon = R.drawable.outline_content_copy_black_24)
                            BubbleMenuItem(stringResource(R.string.share), { closeMenu(); share(listOf(item)) }, icon = R.drawable.outline_share_black_24)
                            BubbleMenuItem(stringResource(R.string.open_with), { closeMenu(); openWith(item) }, icon = R.drawable.outline_apps_black_24)
                            BubbleMenuItem(stringResource(R.string.details), { closeMenu(); details(item) }, icon = R.drawable.outline_info_black_24)
                            BubbleMenuItem(stringResource(R.string.delete), { closeMenu(); delete(listOf(item), false) }, icon = R.drawable.outline_delete_black_20, destructive = true)
                        }
                    }
                }),
                modifier = Modifier.combinedClickable(
                    onClick = { if (selecting) toggle(item) else open(item, 2) },
                    onLongClick = { toggle(item) },
                ),
            )
        }
    }

    private fun toggle(item: ChildItem) {
        if (selected.contains(item.id)) selected.remove(item.id) else selected.add(item.id)
    }

    @Composable
    private fun SiteIcon(bitmap: Bitmap?, item: ChildItem) {
        if (bitmap != null) {
            Image(bitmap.asImageBitmap(), null, contentScale = ContentScale.Crop, modifier = Modifier.size(40.dp).clip(CircleShape))
            return
        }
        val letters = MainUtil.stripPunctuation(item.subText ?: item.name, punct) ?: ""
        Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHighest), contentAlignment = Alignment.Center) {
            if (letters.isEmpty() || item.iconRes != R.drawable.outline_public_black_24) {
                Icon(painterResource(if (item.iconRes != 0) item.iconRes else R.drawable.outline_public_black_24), null, tint = MaterialTheme.colorScheme.onSurface)
            } else {
                Text(letters, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }

    // ------------------------------------------------------------------ contract kept for WebViewActivity

    /** Back already handled inside (Compose does that now). */
    fun hasPopup(): Boolean = false

    /** Old configuration hook; the Compose page re-lays itself out. */
    @Suppress("UNUSED_PARAMETER")
    fun onConfig(configuration: Configuration?) {}

    /** Old resume / pause hook: reload on resume. */
    fun onPrefChanged(isClosing: Boolean) { if (isClosing) reload() }

    /** Old refresh hook. */
    @Suppress("UNUSED_PARAMETER")
    fun refresh(deepScan: Boolean) { reload() }

    /** A site icon was saved: drop the icons so they load again. */
    fun clearIcons() {
        ui.post {
            icons.clear()
            noIcon.clear()
            iconTick++
        }
    }

    override fun dismiss() {
        task?.cancel()
        task = null
        try { sub?.dismiss() } catch (_: Exception) {}
        sub = null
        loader?.release()
        loader = null
        listener = null
        act = null
        super.dismiss()
    }
}
