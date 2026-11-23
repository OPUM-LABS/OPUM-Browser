package com.mycompany.app.dialog

import androidx.compose.material3.Icon
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.MainSettings
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.view.MyBarView
import com.mycompany.app.web.WebViewActivity

/**
 * The browser's main menu: a header (reset, ad-block count, list style, settings), the customised menu items as pages of
 * icon tiles (or one long list / one scrolling grid, by `MainSettings.menuType.value`), and, when configured, a bar of quick buttons at the
 * bottom. Compose now (on [ComposeBottomDialog]); the constructor and the 8-method [DownMenuListener] are as before, and
 * `WebViewActivity` still calls [applyBlocked] (new blocked count) and the open-state checks. Long-pressing an item opens the menu
 * settings; the page last shown is remembered (`MainSettings.menuPage.value`). Parameters: [iArr] item ids, [iArr2] bottom-bar ids, [isRtl]
 * right-to-left, [blockedCount] blocked count; [landscape] and [theme] only steered the old sheet's placement and theme.
 */
class DialogMenuMain(
    private val webViewActivity: WebViewActivity?,
    theme: Int,
    private val topIds: IntArray?,
    private val bottomIds: IntArray?,
    landscape: Boolean,
    private val isRtl: Boolean,
    blockedCount: Int,
    downMenuListener: DownMenuListener?,
) : ComposeBottomDialog(webViewActivity) {
    interface DownMenuListener {
        fun onClose()
        fun onItem(view: android.view.View?, id: Int)
        fun onDownloads()
        fun onHistory()
        fun onLongPress()
        fun onSettings()
        fun onEdit()
        fun onExtra()
    }

    private var listener: DownMenuListener? = downMenuListener
    private var blocked by mutableIntStateOf(blockedCount)
    /** The "All actions" page is shown instead of the menu's own items. */
    private var showAll by mutableStateOf(false)
    private val onStartPage = webViewActivity?.onStartPage == true
    private var page = MainSettings.menuPage.value
    private var columns = MainSettings.menuColumns.value.let { if (it == 0) { MainSettings.editLocal { set(MainSettings.menuColumns, 5) }; 5 } else it }

    /** New number of blocked items (ads / trackers) for the shield. */
    fun applyBlocked(newBlocked: Int) { blocked = newBlocked }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Tile(id: Int, onClick: () -> Unit, onLong: () -> Unit, modifier: Modifier) {
        val colour = MainUtil.barTint(0, false)
        Column(
            modifier.combinedClickable(onClick = onClick, onLongClick = onLong).padding(vertical = 6.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            com.mycompany.app.ui.browser.MenuActionIcon(id, 28.dp, live = true)
            Text(
                stringResource(com.mycompany.app.ui.browser.MenuCatalog.labelRes(id)), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center,
                maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp),
            )
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Items(ids: IntArray) {
        val menuType = MainSettings.menuType.value
        val tap: (Int) -> Unit = { id -> if (id == com.mycompany.app.ui.browser.MenuCatalog.ALL) showAll = true else listener?.onItem(window?.decorView, id) }
        // a long press is still in progress while this runs and the action closes the menu: do it after the gesture
        val long: () -> Unit = { window?.decorView?.post { listener?.onLongPress() } }
        when (menuType) {
            1 -> LazyColumn(Modifier.height(380.dp)) {
                items(ids.toList()) { id ->
                    Row(
                        Modifier.fillMaxWidth().combinedClickable(onClick = { tap(id) }, onLongClick = long).padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        com.mycompany.app.ui.browser.MenuActionIcon(id, 24.dp, live = true)
                        Text(stringResource(com.mycompany.app.ui.browser.MenuCatalog.labelRes(id)), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 20.dp))
                    }
                }
            }
            2 -> Column(Modifier.height(380.dp).verticalScroll(rememberScrollState())) {
                for (rowIds in ids.toList().chunked(columns)) {
                    Row(Modifier.fillMaxWidth()) {
                        for (id in rowIds) Tile(id, { tap(id) }, long, Modifier.weight(1f))
                        repeat(columns - rowIds.size) { Box(Modifier.weight(1f)) }
                    }
                }
            }
            else -> {
                val maxRows = if (menuType == 3) 2 else 3
                val perPage = columns * maxRows
                // a short menu (e.g. on the start page) only takes the rows it needs
                val rows = minOf(maxRows, ((ids.size + columns - 1) / columns).coerceAtLeast(1))
                val pages = (ids.size + perPage - 1) / perPage
                val pager = rememberPagerState(initialPage = page.coerceIn(0, (pages - 1).coerceAtLeast(0))) { pages }
                LaunchedEffect(pager) { snapshotFlow { pager.currentPage }.collect { page = it } }
                HorizontalPager(pager, Modifier.fillMaxWidth().height((rows * 84 + 12).dp), verticalAlignment = Alignment.Top) { index ->
                    val slice = ids.toList().drop(index * perPage).take(perPage)
                    Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        for (rowIds in slice.chunked(columns)) {
                            Row(Modifier.fillMaxWidth().height(84.dp)) {
                                for (id in rowIds) Tile(id, { tap(id) }, long, Modifier.weight(1f))
                                repeat(columns - rowIds.size) { Box(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
                if (pages > 1) {
                    Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), horizontalArrangement = Arrangement.Center) {
                        repeat(pages) { index ->
                            Box(
                                Modifier.padding(horizontal = 3.dp).size(7.dp).background(
                                    if (index == pager.currentPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    CircleShape,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    init {
        setSheet {
            val dark = AppState.darkUi
            var noti by remember { mutableStateOf(AlbumSettings.notiClean.value) }
            androidx.compose.runtime.CompositionLocalProvider(
                LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr,
            ) {
                // labelled header: restart, web filter (blocked count), edit menu, settings; the menu style moved to the editor
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    com.mycompany.app.ui.browser.MenuHeaderItem(
                        painterResource(if (dark) R.drawable.outline_restart_alt_dark_20 else R.drawable.outline_restart_alt_black_20),
                        stringResource(R.string.mh_restart), Modifier.weight(1f),
                    ) { listener?.onEdit() }
                    com.mycompany.app.ui.browser.MenuHeaderItem(
                        painterResource(
                            if (blocked > 0) R.drawable.outline_verified_user_red_20
                            else if (dark) R.drawable.outline_verified_user_dark_20 else R.drawable.outline_verified_user_black_20,
                        ),
                        stringResource(R.string.mh_cleaner), Modifier.weight(1f), badge = blocked, dot = noti,
                    ) {
                        if (AlbumSettings.notiClean.value) {
                            AlbumSettings.edit(context) { set(AlbumSettings.notiClean, false) }
                            noti = false
                        }
                        listener?.onDownloads()
                    }
                    com.mycompany.app.ui.browser.MenuHeaderItem(
                        painterResource(if (dark) R.drawable.outline_view_agenda_dark_20 else R.drawable.outline_view_agenda_black_20),
                        stringResource(R.string.mh_edit), Modifier.weight(1f),
                    ) { window?.decorView?.post { listener?.onLongPress() } }
                    com.mycompany.app.ui.browser.MenuHeaderItem(
                        painterResource(if (dark) R.drawable.outline_settings_dark_20 else R.drawable.outline_settings_black_20),
                        stringResource(R.string.setting), Modifier.weight(1f),
                    ) { listener?.onSettings() }
                }
                HorizontalDivider()
                if (showAll) {
                    // every action, grouped and searchable; page actions are greyed out on the start page
                    Row(Modifier.fillMaxWidth().clickable { showAll = false }.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, null)
                        Text(stringResource(R.string.all_actions), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp))
                    }
                    com.mycompany.app.ui.browser.ActionCatalog(
                        onPick = { id -> listener?.onItem(window?.decorView, id) }, onStartPage = onStartPage, maxHeight = 420.dp,
                    )
                } else {
                    val shown = com.mycompany.app.ui.browser.MenuCatalog.shown(topIds, onStartPage) ?: IntArray(0)
                    Items(shown + com.mycompany.app.ui.browser.MenuCatalog.ALL)
                }
                val bottom = com.mycompany.app.ui.browser.MenuCatalog.shown(bottomIds, false)
                if (!showAll && bottom != null && bottom.isNotEmpty()) {
                    HorizontalDivider()
                    com.mycompany.app.ui.browser.MenuQuickRow(
                        bottom, onTap = { id -> listener?.onItem(window?.decorView, id) },
                        onLong = { window?.decorView?.post { listener?.onLongPress() } },
                    )
                }
            }
        }
        show()
    }

    override fun dismiss() {
        if (MainSettings.menuPage.value != page) {
            MainSettings.editLocal { set(MainSettings.menuPage, page) }
            PrefSet.putInt(context, 5, page, "mMenuPage")
        }
        listener = null
        super.dismiss()
    }
}
