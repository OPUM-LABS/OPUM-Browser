package com.mycompany.app.ui.browser

import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.foundation.layout.requiredWidth
import com.mycompany.app.main.RequestCode
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.gestures.detectTapGestures
import android.content.Context
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.MainSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.ZoneSettings
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.snapshotFlow
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefZtwo
import androidx.compose.foundation.lazy.items as listItems
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.mycompany.app.dialog.DialogEditIcon
import com.mycompany.app.dialog.DialogEditorText.EditorSetListener
import com.mycompany.app.dialog.DialogSetTabRestore
import com.mycompany.app.pref.PrefSet
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import com.mycompany.app.web.WorkspaceSwitch
import com.mycompany.app.pref.store.Workspaces
import com.mycompany.app.pref.store.WorkspaceSettings
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import kotlin.math.roundToInt
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.luminance
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.book.DbBookTab
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.dialog.DialogTabEdit
import com.mycompany.app.dialog.ListTabListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.web.TabSnapshot
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.web.WebTabItem
import com.mycompany.app.web.WebViewActivity

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
import com.mycompany.app.ui.components.Sym

/**
 * The tab overview, stage 4 (groups): two pages (normal / incognito tabs) of cards for the open tabs and tab groups, the
 * current tab outlined. Tap a tab to switch to it; the X closes a tab, with an Undelete snackbar when tab undelete is on;
 * a group card has a menu (Edit: name and colour, Delete: the whole group) and opens a group popup on a tap: its tabs as
 * cards, each can be closed or taken out of the group, the popup can ungroup all. "Delete all", "New tab", back. Edits are
 * made on this dialog's own list; the page of the current mode is handed to the activity when the dialog is left, the other
 * page is written to the database directly (rows of closed tabs deleted once the undo time is over; links and group
 * fields of the changed tabs updated).
 * Grouping by dragging, multi-select, search, list layout and the rest of the old `DialogTabMain` follow in stage 5; the old
 * dialog is used unless the hidden flag is on ([enabled]).
 */
/** The view types of the tab overview (`ZoneSettings.tabListType`): value, name, icon. */
private val LAYOUTS = listOf(
    Triple(0, R.string.thumb_mode, R.drawable.outline_grid_view_black_24),
    Triple(1, R.string.list_mode, R.drawable.outline_view_list_black_24),
    Triple(2, R.string.simple_mode, R.drawable.outline_view_headline_black_24),
    Triple(3, R.string.stack_mode, R.drawable.outline_view_day_black_24),
    Triple(4, R.string.carousel_mode, R.drawable.outline_view_carousel_black_24),
)

/** How many cards one fling of the stack or the carousel may travel at most (it slows down on its own before). */
private const val CAROUSEL_FLING_CARDS = 50

/** Friction of a stack / carousel fling (1 = Compose's exponential default); lower carries further. */
private const val CAROUSEL_FRICTION = 0.4f

/** Card shape (width / height) the carousel's card width is worked out from. */
private const val CAROUSEL_ASPECT = 0.62f

/**
 * The carousel card's own shape (width / height, 2026-11-23): the same width as with [CAROUSEL_ASPECT] but much taller,
 * about a phone page, since the carousel has free space above and below (at most 86 % of the height).
 */
private const val CAROUSEL_TALL_ASPECT = 0.46f

/** Card shape of the stack (vertical) layout: squarer, so the cards can use most of the screen width (owner's wish). */
/** The touch length of the fast-scroll handles (the bar inside is 32 dp, like the page scrollbar). */
private val THUMB_TOUCH = 48.dp

private const val START_PAGE = "file:///android_asset/shortcut.html"

private const val STACK_ASPECT = 0.78f

class ComposeTabOverview(
    private val activity: WebViewActivity,
    list: ArrayList<WebTabItem?>?,
    private val listener: ListTabListener?,
    /** Mini mode: the same overview as a bottom sheet over the page instead of full screen. */
    private val mini: Boolean = false,
) : ComposeFullDialog(activity) {
    /** One card: a single tab, or a group (consecutive tabs with the same group id). */
    private class Entry(val tabs: List<WebTabItem>) {
        /** Two or more tabs of one group, or a single tab that keeps its group (the first tab of a new group). */
        val group get() = tabs.size > 1 || tabs[0].groupId != 0L
    }

    /** State to go back to: the cards, the group fields of every tab, the closed tabs added since, the current tab. */
    private class Snapshot(
        val structure: List<List<WebTabItem>>,
        val fields: List<Pair<WebTabItem, Triple<Long, String?, Int>>>,
        val pendingBefore: Int,
        val current: WebTabItem?,
    )

    /** One page: the normal tabs or the incognito tabs, with its own edits. */
    private inner class Pane(val secret: Boolean) {
        val entries = mutableStateListOf<Entry>()
        var currentTab: WebTabItem? = null
        var changed = false
        val pending = ArrayList<WebTabItem>()
        var undo: Snapshot? = null
        var loaded by mutableStateOf(false)
        /** Bumped when a tab's name / colour changed behind the list's back (group edit). */
        var tick by mutableIntStateOf(0)
        /** Link and group fields as loaded, to write only what changed. */
        val original = HashMap<Long, List<Any?>>()
        /** This page is the one of the activity's current mode (its list is the activity's own). */
        val active get() = secret == SyncSettings.secretMode.value

        fun fill(tabs: List<Any?>?, currentIndex: Int) {
            entries.clear()
            original.clear()
            currentTab = null
            var run: ArrayList<WebTabItem>? = null
            var runGroup = 0L
            fun flush() { run?.let { entries.add(Entry(it)) }; run = null }
            for ((index, any) in (tabs ?: emptyList()).withIndex()) {
                val tab = any as? WebTabItem ?: continue
                original[tab.uid] = listOf(tab.parentId, tab.groupId, tab.groupName, tab.groupColor)
                if (index == currentIndex) currentTab = tab
                if (tab.groupId != 0L && run != null && tab.groupId == runGroup) { run!!.add(tab); continue }
                flush()
                run = ArrayList<WebTabItem>().also { it.add(tab) }
                runGroup = tab.groupId
            }
            flush()
            loaded = true
        }
    }

    private val panes = listOf(Pane(false), Pane(true))
    private var page by mutableIntStateOf(if (SyncSettings.secretMode.value) 1 else 0)
    /** The group whose popup is open (its group id). */
    private var openGroup by mutableStateOf<Long?>(null)
    private var finished = false
    private var edit: DialogTabEdit? = null
    /** Multi-select: the cards ticked so far. */
    private var selecting by mutableStateOf(false)
    /** Card layout: 0 thumbnails (grid), 1 list, 2 simple list (`ZoneSettings.tabListType.value`). */
    private var layout by mutableIntStateOf(ZoneSettings.tabListType.value.coerceIn(0, 4))
    /** The tab search text (null = search is closed). */
    private var query by mutableStateOf<String?>(null)
    private var swipeDelete by mutableStateOf(ZoneSettings.swipeDelete.value)
    /** Where the current tab is put when the overview opens (`ZtwoSettings.tabCurrentPos`): 0 like the address bar, 1 top, 2 bottom. */
    private var currentPos by mutableIntStateOf(ZtwoSettings.tabCurrentPos.value.coerceIn(0, 2))
    /** The cards sit at the bottom of the screen, close to the thumb, and the current tab opens at the bottom edge. */
    private val atBottom get() = when (currentPos) { 1 -> false; 2 -> true; else -> addressAtBottom() }
    /** The clean-up dialog is open. */
    private var cleanUpOpen by mutableStateOf(false)
    /** The duplicate-tabs hint above the cards was closed (for as long as the overview is open). */
    private var hintClosed by mutableStateOf(false)
    /** How many tabs are archived ("Archive inactive tabs", see `TabArchive`); the row above the cards shows when > 0. */
    private var archivedCount by mutableIntStateOf(0)
    /** The archived-tabs sheet is open. */
    private var archiveOpen by mutableStateOf(false)
    /** A new group that has no tabs yet (id, name, colour): its popup is open until a tab is added or it is left. */
    private var draftGroup by mutableStateOf<Triple<Long, String?, Int>?>(null)
    /** A card is being dragged somewhere (the page pager must not take the finger). */
    private var dragging by mutableStateOf(false)
    /** The first-use hint of mini mode (`AlbumSettings.guideTab.value`, saved as `mGuideTab`). */
    private var showHint by mutableStateOf(AlbumSettings.guideTab.value)
    private var editIcon: DialogEditIcon? = null
    private var restoreDialog: DialogSetTabRestore? = null
    private val selected = mutableStateListOf<Entry>()
    /** A card being dragged in the grid: [dx] / [dy] its offset from its slot, [hover] the card it rests over (drop = group). */
    /** Where an entry is drawn (px, in the scrolling container), grid and list alike. */
    private class Slot(val index: Int, val left: Float, val top: Float, val width: Float, val height: Float)

    private class GridDrag(val entry: Entry) {
        var dx by mutableFloatStateOf(0f)
        var dy by mutableFloatStateOf(0f)
        var hover by mutableStateOf<Entry?>(null)
        /** The card is being swiped away sideways (swipe delete) instead of dragged. */
        var swipe by mutableStateOf(false)
    }
    /**
     * Snapshots taken while the overview is open (the current tab is captured as the overview opens): they replace
     * what a card loaded. Everything else lives only as long as its card is composed (plus the memory cache), so
     * scrolling through many tabs does not keep every bitmap alive.
     */
    private val fresh: SnapshotStateMap<Long, Bitmap> = mutableStateMapOf()

    /** A new snapshot of tab [tabId] (from `WvaStoreTabThumb`, main thread). */
    fun onSnapshot(tabId: Long, bitmap: Bitmap) {
        if (MainUtil.isBitmapOk(bitmap)) fresh[tabId] = bitmap
    }

    private val activePane get() = panes[if (SyncSettings.secretMode.value) 1 else 0]
    private val otherPane get() = panes[if (SyncSettings.secretMode.value) 0 else 1]
    private fun currentIndexOf(secret: Boolean) = if (secret) PrefSync.currentSecretIndex else SyncSettings.normalIndex.value
    /** The list of spaces under the first half of the Tabs | Incognito switch is open. */
    private var spaceMenu by mutableStateOf(false)

    /**
     * The first half of the switch (2026-11-24): the space whose tabs it shows ("Tabs" while there are no workspaces;
     * a workspace with its colour), with a small arrow. Tapped while chosen, it lists Main, the workspaces, a new one
     * and their settings; picking one restarts the browser in it ([WorkspaceSwitch]).
     */
    @Composable
    private fun SpaceLabel(count: String) {
        // read again whenever the list changes (a colour or name changed in Manage workspaces)
        val list by WorkspaceSettings.list.flow.collectAsState()
        val spaces = list.let { Workspaces.all() }
        val number = Workspaces.currentNumber()
        val name = when {
            spaces.isEmpty() -> stringResource(R.string.tab_items)
            else -> Workspaces.nameOf(number, stringResource(R.string.space_main))
        }
        val color = spaces.firstOrNull { it.number == number }?.color
        Box {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (color != null) {
                    WorkspaceDot(color, 10.dp)
                    Spacer(Modifier.width(6.dp))
                }
                // the name gives way when room is short (large text, long names); the tab count always stays whole
                Text(
                    name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (count.isNotBlank()) {
                    // a space's name could end in a number itself ("Workspace 2"): its tab count is set apart
                    Text(if (spaces.isEmpty()) " ${count.trim()}" else " · ${count.trim()}", maxLines = 1, softWrap = false, style = MaterialTheme.typography.labelLarge)
                }
                Icon(Sym.ArrowDropDown, contentDescription = stringResource(R.string.workspaces), modifier = Modifier.size(18.dp))
            }
            DropdownMenu(expanded = spaceMenu, onDismissRequest = { spaceMenu = false }) {
                val incognito = SyncSettings.secretMode.value
                val current = if (incognito) -1 else number
                @Composable
                fun item(label: String, dotColor: Int, locked: Boolean, target: Int) {
                    DropdownMenuItem(
                        text = { Text(label, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        leadingIcon = { WorkspaceDot(dotColor, 14.dp) },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (locked) Icon(Sym.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                if (target == current) Icon(Sym.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        },
                        onClick = {
                            spaceMenu = false
                            if (target != current) {
                                dismiss()
                                WorkspaceSwitch.open(activity, target)
                            }
                        },
                    )
                }
                item(stringResource(R.string.space_main), 0, false, Workspaces.MAIN)
                for (space in spaces) item(space.name, space.color, space.locked, space.number)
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.space_new)) },
                    leadingIcon = { Icon(Sym.Add, contentDescription = null) },
                    onClick = { spaceMenu = false; dismiss(); WorkspaceSheet.show(activity, adding = true) },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.space_manage)) },
                    leadingIcon = { Icon(Sym.Settings, contentDescription = null) },
                    onClick = {
                        spaceMenu = false
                        activity.startActivity(android.content.Intent(activity, com.mycompany.app.ui.settings.WorkspaceSettingsActivity::class.java))
                    },
                )
            }
        }
    }

    init {
        activePane.fill(list, currentIndexOf(SyncSettings.secretMode.value))
        setPage { Box(Modifier.fillMaxSize()) { Box(Modifier.fillMaxSize().graphicsLayer { alpha = behind.value }) { Page() }; OpenZoom() } }
        show()
    }

    /** Loads the other page's tabs from the database. */
    private suspend fun loadOther() {
        val pane = otherPane
        if (pane.loaded) return
        val tabs = withContext(Dispatchers.IO) { DbBookTab.loadTabs(activity, pane.secret) }
        pane.fill(tabs, currentIndexOf(pane.secret))
    }

    // ---- list edits -------------------------------------------------------------------------------------------

    /** The tabs of the pane in order, renumbered and re-linked the way the activity expects ([WebTabItem.position] index, [WebTabItem.parentId] previous id). */
    private fun flatten(pane: Pane): ArrayList<WebTabItem?> {
        val out = ArrayList<WebTabItem?>()
        var previous = -1L
        var position = 0
        for (entry in pane.entries) for (tab in entry.tabs) {
            tab.parentId = previous
            tab.position = position++
            previous = tab.uid
            out.add(tab)
        }
        return out
    }

    /** Writes the link and group fields of the tabs that changed since they were loaded (the database keeps the order as links). */
    private fun writeChanges(pane: Pane, flat: List<Any?>) {
        val changes = ArrayList<WebTabItem?>()
        for (any in flat) {
            val tab = any as WebTabItem
            val now = listOf(tab.parentId, tab.groupId, tab.groupName, tab.groupColor)
            if (pane.original[tab.uid] != now) {
                val copy = WebTabItem()
                copy.uid = tab.uid; copy.parentId = tab.parentId; copy.groupId = tab.groupId; copy.groupName = tab.groupName; copy.groupColor = tab.groupColor
                changes.add(copy)
                pane.original[tab.uid] = now
            }
        }
        if (changes.isNotEmpty()) DbBookTab.updateTabs(activity, changes)
    }

    /** Deletes the rows of the tabs closed so far (undo is over) and frees their live pages. */
    private fun commitPending(pane: Pane) {
        if (pane.pending.isEmpty()) { pane.undo = null; return }
        val closed = ArrayList(pane.pending)
        pane.pending.clear()
        pane.undo = null
        for (tab in closed) {
            if (tab.uid > 0) DbBookTab.deleteById(activity, tab.uid)
            val frame = tab.webFrame
            if (frame != null && frame.alive) {
                if (frame === activity.tabsState.currentFrame) frame.setValid(false) else frame.destroyFrame(null)
            }
        }
    }

    private fun snapshot(pane: Pane) = Snapshot(
        pane.entries.map { it.tabs }, pane.entries.flatMap { it.tabs }.map { it to Triple(it.groupId, it.groupName, it.groupColor) },
        pane.pending.size, pane.currentTab,
    )

    /** Runs an edit as one undoable step: the previous undo is committed, a snapshot is kept, the pane is marked changed. */
    private fun edit(pane: Pane, block: () -> Unit) {
        commitPending(pane)
        val before = snapshot(pane)
        block()
        pane.undo = before
        pane.changed = true
        pane.tick++
    }

    private fun restore(pane: Pane) {
        val saved = pane.undo ?: return
        pane.entries.clear()
        pane.entries.addAll(saved.structure.map { Entry(it) })
        for ((tab, fields) in saved.fields) { tab.groupId = fields.first; tab.groupName = fields.second; tab.groupColor = fields.third }
        while (pane.pending.size > saved.pendingBefore) pane.pending.removeAt(pane.pending.size - 1)
        pane.currentTab = saved.current ?: pane.entries.firstOrNull()?.tabs?.firstOrNull()
        pane.undo = null
        pane.tick++
    }

    private fun dissolve(tab: WebTabItem) { tab.groupId = 0L; tab.groupName = null; tab.groupColor = 0 }

    /** After tabs were taken out: the neighbouring tab becomes current if the current one is gone. */
    private fun fixCurrent(pane: Pane, removedAt: Int) {
        val current = pane.currentTab
        if (current != null && pane.entries.any { it.tabs.contains(current) }) return
        val neighbour = if (removedAt > 0) pane.entries.getOrNull(removedAt - 1) else pane.entries.firstOrNull()
        pane.currentTab = neighbour?.tabs?.let { if (removedAt > 0) it.last() else it.first() }
    }

    /** Closes a card (a tab or a whole group); true when an undo should be offered. */
    private fun closeEntry(pane: Pane, entry: Entry): Boolean {
        val position = pane.entries.indexOf(entry)
        if (position < 0) return false
        edit(pane) {
            pane.entries.removeAt(position)
            pane.pending.addAll(entry.tabs)
            fixCurrent(pane, position)
        }
        return ZoneSettings.tabUndelete.value || entry.group
    }

    /** Closes one tab of a group; a group left with one tab is dissolved. */
    private fun closeMember(pane: Pane, entry: Entry, tab: WebTabItem): Boolean {
        val position = pane.entries.indexOf(entry)
        if (position < 0) return false
        edit(pane) {
            val rest = entry.tabs - tab
            pane.pending.add(tab)
            when {
                rest.isEmpty() -> pane.entries.removeAt(position)
                rest.size == 1 -> { dissolve(rest[0]); pane.entries[position] = Entry(rest) }
                else -> pane.entries[position] = Entry(rest)
            }
            fixCurrent(pane, position)
        }
        return ZoneSettings.tabUndelete.value
    }

    /** Takes a tab out of its group; it becomes a plain card right after the group. */
    private fun removeFromGroup(pane: Pane, entry: Entry, tab: WebTabItem) {
        val position = pane.entries.indexOf(entry)
        if (position < 0) return
        edit(pane) {
            val rest = entry.tabs - tab
            dissolve(tab)
            if (rest.isEmpty()) {
                // the only tab of a one-tab group: the group is gone
                pane.entries[position] = Entry(listOf(tab))
                return@edit
            }
            if (rest.size == 1) dissolve(rest[0])
            pane.entries[position] = Entry(rest)
            pane.entries.add(position + 1, Entry(listOf(tab)))
        }
    }

    private fun ungroupAll(pane: Pane, entry: Entry) {
        val position = pane.entries.indexOf(entry)
        if (position < 0) return
        edit(pane) {
            entry.tabs.forEach { dissolve(it) }
            pane.entries.removeAt(position)
            pane.entries.addAll(position, entry.tabs.map { Entry(listOf(it)) })
        }
    }

    private fun closeAll(pane: Pane) {
        if (pane.entries.isEmpty()) return
        edit(pane) {
            pane.pending.addAll(pane.entries.flatMap { it.tabs })
            pane.entries.clear()
            pane.currentTab = null
        }
    }

    /** Moves card [from] to position [to] (drag reorder). */
    private fun moveEntry(pane: Pane, from: Int, to: Int) {
        if (from == to || from !in pane.entries.indices || to !in pane.entries.indices) return
        pane.entries.add(to, pane.entries.removeAt(from))
        pane.changed = true
    }

    /** Drops [dragged] onto [target]: they become one group (the target's if it is one) at the target's place. */
    private fun mergeEntries(pane: Pane, dragged: Entry, target: Entry) {
        if (dragged === target) return
        val from = pane.entries.indexOf(dragged)
        val to = pane.entries.indexOf(target)
        if (from < 0 || to < 0) return
        edit(pane) {
            val id: Long
            val name: String?
            val colour: Int
            when {
                target.group -> { id = target.tabs[0].groupId; name = target.tabs[0].groupName; colour = target.tabs[0].groupColor }
                dragged.group -> { id = System.currentTimeMillis(); name = dragged.tabs[0].groupName; colour = dragged.tabs[0].groupColor }
                else -> { id = System.currentTimeMillis(); name = null; colour = NEW_GROUP_COLOR }
            }
            val tabs = target.tabs + dragged.tabs
            for (tab in tabs) { tab.groupId = id; tab.groupName = name; tab.groupColor = colour }
            pane.entries.removeAt(from)
            pane.entries[pane.entries.indexOf(target)] = Entry(tabs)
        }
    }

    private fun endSelection() { selecting = false; selected.clear() }

    /** Closes the ticked cards as one step; true when an undo should be offered. */
    private fun deleteSelected(pane: Pane): Boolean {
        val chosen = selected.toList()
        endSelection()
        if (chosen.isEmpty()) return false
        edit(pane) {
            val firstAt = pane.entries.indexOf(chosen.first()).coerceAtLeast(0)
            pane.entries.removeAll(chosen.toSet())
            chosen.forEach { pane.pending.addAll(it.tabs) }
            fixCurrent(pane, firstAt)
        }
        return ZoneSettings.tabUndelete.value
    }

    private fun editGroup(pane: Pane, entry: Entry) {
        if (edit != null) return
        val head = entry.tabs.first()
        val flat = flatten(pane)
        var dialog: DialogTabEdit? = null
        dialog = DialogTabEdit(
            activity, flat, ArrayList<WebTabItem?>(entry.tabs),
            head.groupName?.takeIf { it.isNotEmpty() } ?: activity.getString(R.string.group_title), head.groupColor,
            object : DialogApplyListener {
                override fun onApply() {
                    // the dialog wrote the new name and colour into the tabs and the database
                    for (tab in entry.tabs) pane.original[tab.uid] = listOf(tab.parentId, tab.groupId, tab.groupName, tab.groupColor)
                    pane.tick++
                    dialog?.dismiss()
                }
            },
        )
        dialog.setOnDismissListener { edit = null }
        edit = dialog
    }

    // ---- clean-up ----------------------------------------------------------------------------------------------

    /** When a tab was last current; tabs never recorded fall back to when they were opened (the uid is that time in ms). */
    private fun lastUsedOf(tab: WebTabItem): Long = when {
        tab.lastUsed > 0 -> tab.lastUsed
        tab.uid > 1_000_000_000_000L -> tab.uid
        else -> 0L
    }

    /** Single tabs whose address another single tab already has; per address the current tab, else the most recently used, stays. */
    private fun duplicateEntries(pane: Pane): List<Entry> {
        val kept = HashMap<String, Entry>()
        val extra = ArrayList<Entry>()
        for (entry in pane.entries) {
            if (entry.group) continue
            val key = sameAddressKey(entry.tabs[0].tabUrl) ?: continue
            val other = kept[key]
            if (other == null) { kept[key] = entry; continue }
            val keepNew = when {
                entry.tabs[0] === pane.currentTab -> true
                other.tabs[0] === pane.currentTab -> false
                else -> lastUsedOf(entry.tabs[0]) > lastUsedOf(other.tabs[0])
            }
            if (keepNew) { extra.add(other); kept[key] = entry } else extra.add(entry)
        }
        return extra
    }

    /** Single tabs (not the current one) not used for [days] days. */
    private fun unusedEntries(pane: Pane, days: Int): List<Entry> {
        val cutoff = System.currentTimeMillis() - days * 86_400_000L
        return pane.entries.filter { !it.group && it.tabs[0] !== pane.currentTab && lastUsedOf(it.tabs[0]).let { used -> used in 1 until cutoff } }
    }

    /** Closes [chosen] as one undoable step; returns how many tabs were closed. */
    private fun closeEntries(pane: Pane, chosen: List<Entry>): Int {
        if (chosen.isEmpty()) return 0
        edit(pane) {
            val firstAt = pane.entries.indexOf(chosen.first()).coerceAtLeast(0)
            pane.entries.removeAll(chosen.toSet())
            chosen.forEach { pane.pending.addAll(it.tabs) }
            fixCurrent(pane, firstAt)
        }
        return chosen.sumOf { it.tabs.size }
    }

    /** Orders the cards by when they were last used, the most recent next to the address bar (bottom or top); groups stay whole. */
    private fun sortByLastUsed(pane: Pane) {
        if (pane.entries.size < 2) return
        edit(pane) {
            val ascending = pane.entries.sortedBy { entry -> entry.tabs.maxOf { lastUsedOf(it) } }
            val sorted = if (atBottom) ascending else ascending.reversed()
            pane.entries.clear()
            pane.entries.addAll(sorted)
        }
    }

    /** A slim hint above the cards when several tabs are open twice: close the duplicates in one go. */
    @Composable
    private fun CleanUpHint(pane: Pane) {
        if (hintClosed || !pane.loaded) return
        val duplicates = remember(pane.tick, pane.entries.size, pane.currentTab) { duplicateEntries(pane) }
        if (duplicates.size < 5) return
        Surface(
            shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 4.dp),
        ) {
            Row(Modifier.padding(start = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    pluralStringResource(R.plurals.duplicates_hint, duplicates.size, duplicates.size), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.weight(1f),
                )
                TextButton(onClick = {
                    val closed = closeEntries(pane, duplicates)
                    if (closed > 0) offerUndoFromCard(pane, activity.resources.getQuantityString(R.plurals.tabs_closed, closed, closed))
                }) { Text(stringResource(R.string.close)) }
                IconButton(onClick = { hintClosed = true }) { Icon(Sym.Close, contentDescription = stringResource(R.string.close), modifier = Modifier.size(18.dp)) }
            }
        }
    }

    /**
     * The landscape side rail (2026-11-23): a phone on its side has width to spare and little height, so the header,
     * the Tabs / Incognito switch, the duplicate-tabs hint (as a badge), the archive and the New group / New tab buttons
     * sit in two narrow columns on the left instead of above and below the cards. Scrolls on very short screens.
     */
    @Composable
    private fun SideRail(
        pane: Pane, usable: Boolean,
        layoutButton: @Composable () -> Unit, searchButton: @Composable () -> Unit,
        deleteButton: @Composable () -> Unit, moreButton: @Composable () -> Unit,
        onDeleteSelected: () -> Unit,
    ) {
        // the top part scrolls when the screen is short; New group / New tab keep their place at the bottom
        Column(Modifier.width(112.dp).fillMaxHeight().padding(horizontal = 4.dp, vertical = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Column(
                        Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        if (selecting) {
                            IconButton(onClick = { endSelection() }) { Icon(Sym.Close, contentDescription = stringResource(R.string.close)) }
                            Text(
                                MainUtil.countLabel(selected.size, pane.entries.size), style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(R.string.all), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                                Checkbox(
                                    checked = pane.entries.isNotEmpty() && selected.size == pane.entries.size,
                                    onCheckedChange = { all -> selected.clear(); if (all) selected.addAll(pane.entries) },
                                )
                            }
                        } else {
                            Row {
                                IconButton(onClick = { dismiss() }) { Icon(Sym.ArrowBack, contentDescription = stringResource(R.string.abc_action_bar_up_description)) }
                                moreButton()
                            }
                            Row { layoutButton(); searchButton() }
                            Row {
                                deleteButton()
                                if (!pane.secret && archivedCount > 0 && pane.loaded) {
                                    IconButton(onClick = { openGroup = null; archiveOpen = true }) {
                                        androidx.compose.material3.BadgedBox(badge = { androidx.compose.material3.Badge { Text(archivedCount.toString()) } }) {
                                            Icon(painterResource(R.drawable.outline_archive_black_24), contentDescription = stringResource(R.string.archived_tabs))
                                        }
                                    }
                                } else {
                                    Spacer(Modifier.size(48.dp))
                                }
                            }
                            // Tabs / Incognito: the portrait switch standing upright (Material 3 has no vertical segmented
                            // button): the same colours and outline, the two halves joined, rounded at the outer ends only
                            val segColors = SegmentedButtonDefaults.colors()
                            Column(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                for (index in 0..1) {
                                    val item = panes[index]
                                    val count = if (item.loaded) item.entries.sumOf { it.tabs.size }.toString() else ""
                                    val chosen = page == index
                                    val shape = if (index == 0) RoundedCornerShape(topStartPercent = 50, topEndPercent = 50)
                                        else RoundedCornerShape(bottomStartPercent = 50, bottomEndPercent = 50)
                                    Surface(
                                        onClick = { if (index == 0 && page == 0) spaceMenu = true else { page = index; openGroup = null; draftGroup = null } },
                                        shape = shape,
                                        color = if (chosen) segColors.activeContainerColor else segColors.inactiveContainerColor,
                                        contentColor = if (chosen) segColors.activeContentColor else segColors.inactiveContentColor,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (chosen) segColors.activeBorderColor else segColors.inactiveBorderColor),
                                        // the halves share their middle line, as in a segmented row
                                        modifier = Modifier.fillMaxWidth().height(44.dp).then(if (index == 1) Modifier.offset(y = (-1).dp) else Modifier),
                                    ) {
                                        Row(Modifier.padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                                            // one text per half ("Tabs 6"), as on the portrait switch: read as one label
                                            if (index == 1) Icon(painterResource(R.drawable.outline_secret_mode_white_18), contentDescription = null, modifier = Modifier.size(16.dp).padding(end = 4.dp))
                                            if (index == 0) {
                                                SpaceLabel(count)
                                            } else {
                                                Text(
                                                    stringResource(R.string.incognito_short).let { if (count.isEmpty()) it else "$it $count" },
                                                    style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            DuplicatesBadge(pane)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                        if (selecting) {
                            TextButton(onClick = onDeleteSelected, enabled = selected.isNotEmpty()) {
                                Text(stringResource(R.string.delete), style = MaterialTheme.typography.titleMedium)
                            }
                        } else {
                            // a new group gets its tabs added right away, so only on the page of the current mode
                            androidx.compose.material3.OutlinedIconButton(
                                onClick = { openGroup = null; draftGroup = Triple(System.currentTimeMillis(), null, NEW_GROUP_COLOR) },
                                enabled = usable && pane.active,
                            ) { Icon(painterResource(R.drawable.outline_create_new_folder_black_20), contentDescription = stringResource(R.string.new_group)) }
                            androidx.compose.material3.FilledIconButton(onClick = { newTab(pane) }, enabled = usable) {
                                Icon(Sym.Add, contentDescription = stringResource(R.string.new_url))
                            }
                        }
                    }
        }
    }

    /** The duplicate-tabs hint of the side rail: a small badge with the count; its menu closes them in one go. */
    @Composable
    private fun DuplicatesBadge(pane: Pane) {
        if (hintClosed || !pane.loaded) return
        val duplicates = remember(pane.tick, pane.entries.size, pane.currentTab) { duplicateEntries(pane) }
        if (duplicates.size < 5) return
        var menu by remember { mutableStateOf(false) }
        Box {
            Surface(
                onClick = { menu = true }, shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(painterResource(R.drawable.outline_tab_close_black_24), contentDescription = null, modifier = Modifier.size(16.dp))
                    Text(
                        duplicates.size.toString(), style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.padding(start = 6.dp),
                    )
                }
            }
            com.mycompany.app.ui.components.BubbleMenu(expanded = menu, onDismiss = { menu = false }) {
                DropdownMenuItem(
                    text = { Text(pluralStringResource(R.plurals.duplicates_hint, duplicates.size, duplicates.size)) },
                    onClick = {}, enabled = false,
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.close_duplicates, duplicates.sumOf { it.tabs.size })) },
                    leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_tab_close_black_24) },
                    onClick = {
                        menu = false
                        val closed = closeEntries(pane, duplicates)
                        if (closed > 0) offerUndoFromCard(pane, activity.resources.getQuantityString(R.plurals.tabs_closed, closed, closed))
                    },
                )
            }
        }
    }

    /** The clean-up dialog: close duplicates, close tabs not used for 7 / 30 days, sort by last used. */
    @Composable
    private fun CleanUpDialog(pane: Pane) {
        val duplicates = remember(pane.tick, pane.entries.size) { duplicateEntries(pane) }
        val week = remember(pane.tick, pane.entries.size) { unusedEntries(pane, 7) }
        val month = remember(pane.tick, pane.entries.size) { unusedEntries(pane, 30) }
        fun closeAndReport(chosen: List<Entry>) {
            cleanUpOpen = false
            val closed = closeEntries(pane, chosen)
            if (closed > 0) offerUndoFromCard(pane, activity.resources.getQuantityString(R.plurals.tabs_closed, closed, closed))
        }
        AlertDialog(
            onDismissRequest = { cleanUpOpen = false },
            title = { Text(stringResource(R.string.clean_up)) },
            text = {
                Column {
                    TextButton(onClick = { closeAndReport(duplicates) }, enabled = duplicates.isNotEmpty()) {
                        Text(stringResource(R.string.close_duplicates, duplicates.sumOf { it.tabs.size }), modifier = Modifier.fillMaxWidth())
                    }
                    TextButton(onClick = { closeAndReport(week) }, enabled = week.isNotEmpty()) {
                        Text(pluralStringResource(R.plurals.close_unused, 7, 7, week.size), modifier = Modifier.fillMaxWidth())
                    }
                    TextButton(onClick = { closeAndReport(month) }, enabled = month.isNotEmpty()) {
                        Text(pluralStringResource(R.plurals.close_unused, 30, 30, month.size), modifier = Modifier.fillMaxWidth())
                    }
                    TextButton(onClick = { cleanUpOpen = false; sortByLastUsed(pane) }, enabled = pane.entries.size > 1) {
                        Text(stringResource(R.string.sort_last_used), modifier = Modifier.fillMaxWidth())
                    }
                    Text(
                        stringResource(R.string.clean_up_info), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 12.dp, top = 8.dp),
                    )
                }
            },
            confirmButton = { TextButton(colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = androidx.compose.material3.MaterialTheme.colorScheme.error), onClick = { cleanUpOpen = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }

    // ---- archived tabs (2026-11-21) -----------------------------------------------------------------------------

    /** Archived tabs of one group (archived together) or one single tab, for the sheet. */
    private class ArchivedEntry(val tabs: List<WebTabItem>, val archivedAt: Long)

    private fun archivedEntries(items: List<DbBookTab.Archived>): List<ArchivedEntry> {
        val out = ArrayList<ArchivedEntry>()
        val groups = LinkedHashMap<Long, ArrayList<DbBookTab.Archived>>()
        for (item in items) {
            val groupId = item.tab.groupId
            if (groupId == 0L) out.add(ArchivedEntry(listOf(item.tab), item.archivedAt))
            else groups.getOrPut(groupId) { ArrayList() }.add(item)
        }
        for (group in groups.values) out.add(ArchivedEntry(group.map { it.tab }, group.maxOf { it.archivedAt }))
        return out.sortedByDescending { it.archivedAt }
    }

    /**
     * Puts archived tabs back into the tab list, right after the current tab, as used now (the database row is
     * unmarked at once; the new links are written when the overview is left, like every other edit). Not undoable.
     */
    private fun restoreArchived(pane: Pane, tabs: List<WebTabItem>) {
        if (tabs.isEmpty()) return
        commitPending(pane)
        val now = System.currentTimeMillis()
        tabs.forEach { it.lastUsed = now; it.webFrame = null }
        DbBookTab.restoreArchived(activity, tabs.map { it.uid }, now)
        val currentAt = pane.entries.indexOfFirst { entry -> entry.tabs.any { it === pane.currentTab } }
        val at = if (currentAt < 0) pane.entries.size else currentAt + 1
        pane.entries.add(at, Entry(tabs))
        pane.undo = null
        pane.changed = true
        pane.tick++
        archivedCount = (archivedCount - tabs.size).coerceAtLeast(0)
    }

    /** The row above the cards: "Archived tabs" and how many (normal tabs only); opens the sheet. */
    @Composable
    private fun ArchivedRow(pane: Pane) {
        if (pane.secret || archivedCount <= 0 || !pane.loaded) return
        Surface(
            onClick = { openGroup = null; archiveOpen = true },
            shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 12.dp, top = 4.dp),
        ) {
            Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(R.drawable.outline_archive_black_24), contentDescription = null, modifier = Modifier.size(20.dp))
                Text(
                    stringResource(R.string.archived_tabs), style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                )
                Text(archivedCount.toString(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }

    /** The archived tabs: tap opens one again (with its back history), X deletes it; restore all / delete all. */
    @Composable
    private fun ArchivedSheet(pane: Pane) {
        val items = remember { mutableStateListOf<ArchivedEntry>() }
        var loaded by remember { mutableStateOf(false) }
        var confirmDelete by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            val all = withContext(Dispatchers.IO) { archivedEntries(DbBookTab.archivedTabs(activity)) }
            items.clear(); items.addAll(all); loaded = true
            archivedCount = all.sumOf { it.tabs.size }
        }
        fun delete(chosen: List<ArchivedEntry>) {
            val uids = chosen.flatMap { it.tabs }.map { it.uid }
            items.removeAll(chosen.toSet())
            archivedCount = items.sumOf { it.tabs.size }
            scope.launch(Dispatchers.IO) { uids.forEach { DbBookTab.deleteById(activity, it) } }
            if (items.isEmpty()) archiveOpen = false
        }
        com.mycompany.app.ui.components.OpumSheet(
            onDismiss = { archiveOpen = false },
            title = stringResource(R.string.archived_tabs),
            subtitle = stringResource(R.string.archived_hint),
            actions = {
                TextButton(onClick = { confirmDelete = true }, enabled = items.isNotEmpty()) { Text(stringResource(R.string.delete_all)) }
                TextButton(onClick = {
                    restoreArchived(pane, items.flatMap { it.tabs })
                    items.clear()
                    close()
                }, enabled = items.isNotEmpty()) { Text(stringResource(R.string.restore_all)) }
            },
        ) {
            if (!loaded) return@OpumSheet
            Column {
                for (entry in items) {
                    val first = entry.tabs[0]
                    val title = if (entry.tabs.size > 1) (first.groupName?.takeIf { it.isNotBlank() } ?: stringResource(R.string.group_title)) + " (" + entry.tabs.size + ")"
                        else first.tabTitle?.takeIf { it.isNotBlank() } ?: first.tabUrl ?: ""
                    val host = MainUtil.shortHostOf(first.tabUrl) ?: ""
                    // when it was last used (what made it inactive); tabs never recorded show when they were archived
                    val usedAt = entry.tabs.maxOf { it.lastUsed }.takeIf { it > 0 } ?: entry.archivedAt
                    val ago = android.text.format.DateUtils.getRelativeTimeSpanString(usedAt, System.currentTimeMillis(), android.text.format.DateUtils.DAY_IN_MILLIS).toString()
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable {
                            // open it again: back into the list after the current tab, then switch to it
                            restoreArchived(pane, entry.tabs)
                            archiveOpen = false
                            select(pane, first)
                        }.padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val icon by androidx.compose.runtime.produceState(cachedFavicon(first), first.tabUrl) { if (value == null) value = loadFavicon(first) }
                        Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                            val bitmap = icon
                            if (bitmap != null) androidx.compose.foundation.Image(bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.size(20.dp))
                            else Icon(painterResource(R.drawable.outline_archive_black_24), contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(Modifier.weight(1f).padding(start = 14.dp)) {
                            Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                if (host.isEmpty()) ago else "$host · $ago", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                        }
                        IconButton(onClick = { delete(listOf(entry)) }) { Icon(Sym.Close, contentDescription = stringResource(R.string.delete), modifier = Modifier.size(18.dp)) }
                    }
                }
            }
        }
        if (confirmDelete) {
            AlertDialog(
                onDismissRequest = { confirmDelete = false },
                title = { Text(stringResource(R.string.delete_all)) },
                text = { Text(stringResource(R.string.archived_delete_q)) },
                confirmButton = {
                    TextButton(colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error), onClick = {
                        confirmDelete = false
                        delete(items.toList())
                    }) { Text(stringResource(R.string.delete)) }
                },
                dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } },
            )
        }
    }

    // ---- leaving ------------------------------------------------------------------------------------------------

    private fun indexOfCurrent(pane: Pane, flat: List<Any?>): Int {
        val index = flat.indexOf(pane.currentTab)
        return if (index >= 0) index else 0
    }

    /** Writes the edits of the page that is not the activity's current mode to the database (the activity only knows the current one). */
    private fun persistOther(pane: Pane) {
        if (!pane.changed || pane.active) return
        commitPending(pane)
        writeChanges(pane, flatten(pane))
        pane.changed = false
    }

    /** Hands the edits to the activity (the current mode's page) or the database (the other page) when the dialog is left. */
    private fun applyOnExit() {
        if (finished) return
        finished = true
        persistOther(otherPane)
        val pane = activePane
        if (!pane.changed) return
        commitPending(pane)
        val flat = flatten(pane)
        writeChanges(pane, flat)
        listener?.applyList(indexOfCurrent(pane, flat), flat)
    }

    /**
     * Where each tab's picture was last laid out (window coordinates), for the zoom when it opens. Written on layout,
     * read on a tap; not state.
     */
    private val cardBounds = HashMap<Long, androidx.compose.ui.geometry.Rect>()

    /** A tab being opened with the zoom ([OpenZoom]): its picture grows from [from] to the page, then [select] runs. */
    private class Opening(val pane: Pane, val tab: WebTabItem, val from: androidx.compose.ui.geometry.Rect, val bitmap: Bitmap?)
    private var opening by mutableStateOf<Opening?>(null)

    /** The overview under the zoom: faded out once the picture fills the page, so the browser's own bars show around it. */
    private val behind = androidx.compose.animation.core.Animatable(1f)

    /** Set when the wait after the zoom is over: the picture fades out ([OpenZoom]), then the overview closes. */
    private var handOver by mutableStateOf(false)

    /**
     * Opens [tab] from a tap on its card (2026-11-21): the card's picture zooms to the page area in about a quarter
     * second, then the overview closes onto the page, instead of the page flashing in. Straight [select] when the card's
     * place is not known (rows, a card not laid out), in the mini sheet, or when the system turns animations off.
     */
    private fun open(pane: Pane, tab: WebTabItem) {
        if (opening != null || finished) return
        val from = cardBounds[tab.uid]
        if (mini || from == null || from.isEmpty || !android.animation.ValueAnimator.areAnimatorsEnabled()) { select(pane, tab); return }
        opening = Opening(pane, tab, from, (if (hasPicture(tab.tabUrl)) fresh[tab.uid] else null) ?: cachedSnapshot(tab, pane.secret))
    }

    /** The page area (the current tab's frame, above the bottom toolbar) in this dialog window's coordinates, or null (then the whole window). */
    private fun pageBounds(window: View): androidx.compose.ui.geometry.Rect? {
        val frame = activity.tabsState.currentFrame ?: return null
        if (!frame.isAttachedToWindow || frame.width <= 0 || frame.height <= 0) return null
        val at = IntArray(2); frame.getLocationOnScreen(at)
        val origin = IntArray(2); window.rootView.getLocationOnScreen(origin)
        val left = (at[0] - origin[0]).toFloat(); val top = (at[1] - origin[1]).toFloat()
        var bottom = top + frame.height
        // the page runs on under the bottom toolbar: stop above it, so the toolbar stays visible once the overview fades
        val bar = activity.bottomBarFrame
        if (bar != null && bar.isShown && bar.height > 0) {
            val barAt = IntArray(2); bar.getLocationOnScreen(barAt)
            val barTop = (barAt[1] - origin[1]).toFloat()
            if (barTop > top && barTop < bottom) bottom = barTop
        }
        return androidx.compose.ui.geometry.Rect(left, top, left + frame.width, bottom)
    }

    /** The zoom of [open]: drawn over the whole overview, takes every touch while it runs. */
    @Composable
    private fun OpenZoom() {
        val open = opening ?: return
        val view = LocalView.current
        val density = LocalDensity.current
        val progress = remember(open) { androidx.compose.animation.core.Animatable(0f) }
        var origin by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
        var area by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
        val target = remember(open) { pageBounds(view) }
        LaunchedEffect(open) {
            progress.animateTo(1f, androidx.compose.animation.core.tween(260, easing = androidx.compose.animation.core.FastOutSlowInEasing))
            launch { behind.animateTo(0f, androidx.compose.animation.core.tween(140)) }
            select(open.pane, open.tab)
        }
        val fade = remember(open) { androidx.compose.animation.core.Animatable(1f) }
        LaunchedEffect(handOver) {
            if (!handOver) return@LaunchedEffect
            fade.animateTo(0f, androidx.compose.animation.core.tween(180))
            close()
        }
        Box(
            Modifier.fillMaxSize()
                .onGloballyPositioned { origin = it.positionInWindow(); area = it.size }
                .pointerInput(Unit) { awaitPointerEventScope { while (true) awaitPointerEvent().changes.forEach { it.consume() } } },
        ) {
            if (area == androidx.compose.ui.unit.IntSize.Zero) return@Box
            val to = (target ?: androidx.compose.ui.geometry.Rect(origin, androidx.compose.ui.geometry.Size(area.width.toFloat(), area.height.toFloat())))
            val p = progress.value
            val r = androidx.compose.ui.geometry.lerp(open.from, to, p).translate(-origin)
            val dark = AppState.darkUi
            with(density) {
                Box(
                    Modifier.offset { androidx.compose.ui.unit.IntOffset(r.left.roundToInt(), r.top.roundToInt()) }
                        .graphicsLayer { alpha = fade.value }
                        .size(r.width.toDp(), r.height.toDp())
                        .clip(RoundedCornerShape(14.dp * (1f - p)))
                        .background(if (dark) Color.Black else Color.White),
                ) {
                    val bitmap = open.bitmap
                    // the snapshot is only the top of the page (TabSnapshot.MAX_ASPECT): full width, top aligned, so it lines
                    // up with the card at the start and with the page at the end (cropping to fill would blow it up sideways)
                    if (bitmap != null) {
                        Image(bitmap.asImageBitmap(), contentDescription = null, contentScale = ContentScale.FillWidth, alignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }

    private fun select(pane: Pane, tab: WebTabItem) {
        finished = true
        if (!pane.active) {
            persistOther(pane)
            persistOther(otherPane)
            val index = pane.entries.flatMap { it.tabs }.indexOf(tab).coerceAtLeast(0)
            listener?.selectTab(false, null, index, pane.secret)
            dismiss()
            return
        }
        persistOther(otherPane)
        if (pane.changed) {
            commitPending(pane)
            val flat = flatten(pane)
            writeChanges(pane, flat)
            listener?.selectTab(true, flat, flat.indexOf(tab).coerceAtLeast(0), pane.secret)
        } else {
            listener?.selectTab(false, null, pane.entries.flatMap { it.tabs }.indexOf(tab).coerceAtLeast(0), pane.secret)
        }
        dismiss()
    }

    private fun newTab(pane: Pane) {
        finished = true
        if (!pane.active) {
            persistOther(pane)
            persistOther(otherPane)
            listener?.onClose(false, null, 0, pane.secret)
            dismiss()
            return
        }
        persistOther(otherPane)
        if (pane.changed) {
            commitPending(pane)
            val flat = flatten(pane)
            writeChanges(pane, flat)
            listener?.onClose(true, flat, indexOfCurrent(pane, flat), pane.secret)
        } else {
            listener?.onClose(false, null, 0, pane.secret)
        }
        dismiss()
    }

    /**
     * "New tab" in a group's page: hands the list over with the group's last tab as current, and the activity adds a new tab
     * right after it that joins the group. [entry] null: the new, still empty group ([draftGroup]); the tab is added after the
     * current tab (after its group, if it has one).
     */
    private fun newTabInGroup(pane: Pane, entry: Entry?) {
        if (!pane.active) return
        val draft = draftGroup
        val head = entry?.tabs?.first()
        val groupId = head?.groupId ?: draft?.first ?: return
        finished = true
        draftGroup = null
        persistOther(otherPane)
        commitPending(pane)
        val flat = flatten(pane)
        writeChanges(pane, flat)
        val anchor = entry?.tabs?.last() ?: pane.currentTab
        val index = flat.indexOf(anchor).let { if (it >= 0) it else flat.size - 1 }.coerceAtLeast(0)
        listener?.newTabInGroup(
            if (flat.isEmpty()) null else flat, index, groupId,
            head?.groupName ?: draft?.second, head?.groupColor ?: draft?.third ?: 0,
        )
        dismiss()
    }

    // ---- thumbnails ---------------------------------------------------------------------------------------------

    // Loaded straight from the database on a small background pool. (They used to go through MainListLoader, which
    // drops every request whose view is not a tagged list row; the overview's Compose view has no tag, so no
    // snapshot or favicon was ever shown.) A card that scrolls away cancels its load.

    // saved pages get snapshots too, the start page its house instead (a small copy of it tells nothing apart);
    // favicons only for web pages
    private fun hasPicture(url: String?) = !url.isNullOrEmpty() && url != START_PAGE
    private fun hasFavicon(url: String?) = !(url.isNullOrEmpty() || url.startsWith("file:///") || url.startsWith("/data/"))

    private fun snapshotKey(tab: WebTabItem, secret: Boolean) =
        TabSnapshot.memoryKey(tab.uid, com.mycompany.app.glide.ImageCache.key(2, tab.tabUrl + tab.deskMode + secret))

    private fun cachedSnapshot(tab: WebTabItem, secret: Boolean): Bitmap? =
        if (!hasPicture(tab.tabUrl)) null
        else com.mycompany.app.glide.ImageCache.get(snapshotKey(tab, secret))?.takeIf { MainUtil.isBitmapOk(it) }

    private suspend fun loadSnapshot(tab: WebTabItem, secret: Boolean): Bitmap? {
        if (!hasPicture(tab.tabUrl)) return null
        val key = snapshotKey(tab, secret)
        val context = activity.applicationContext
        return withContext(pictureLoads) {
            val row = com.mycompany.app.db.book.DbTabThumb.thumbBytes(context, tab.uid)
            // a snapshot of the site the tab showed before is misleading: none instead
            val bitmap = if (row != null && TabSnapshot.sameSite(row.second, tab.tabUrl)) TabSnapshot.decode(row.first) else null
            if (MainUtil.isBitmapOk(bitmap)) com.mycompany.app.glide.ImageCache.put(key, bitmap)
            bitmap?.takeIf { MainUtil.isBitmapOk(it) }
        }
    }

    private fun cachedFavicon(tab: WebTabItem): Bitmap? =
        if (!hasFavicon(tab.tabUrl)) null else MainUtil.cachedIcon(MainUtil.shortHostOf(tab.tabUrl))?.takeIf { MainUtil.isBitmapOk(it) }

    private suspend fun loadFavicon(tab: WebTabItem): Bitmap? {
        val url = tab.tabUrl
        if (!hasFavicon(url)) return null
        val context = activity.applicationContext
        return withContext(pictureLoads) {
            val bitmap = try { MainUtil.loadAlbumThumb(39, tab.uid, context, url) } catch (_: Exception) { null }
            if (MainUtil.isBitmapOk(bitmap)) { MainUtil.cacheIcon(MainUtil.shortHostOf(url), bitmap); bitmap } else null
        }
    }

    // ---- UI -----------------------------------------------------------------------------------------------------

    /** Full screen, or in mini mode a bottom sheet (about two thirds of the screen) with a scrim that closes it. */
    @Composable
    private fun Shell(background: Color, content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit) {
        androidx.compose.runtime.CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            ShellBody(background, content)
        }
    }

    @Composable
    private fun ShellBody(background: Color, content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit) {
        if (mini) {
            val none = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.32f)).clickable(interactionSource = none, indication = null) { dismiss() }) {
                Box(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().fillMaxHeight(0.66f)
                        .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(background)
                        .clickable(interactionSource = none, indication = null) { },
                    content = content,
                )
            }
        } else {
            Box(Modifier.fillMaxSize().background(background), content = content)
        }
    }

    @Composable
    private fun Page() {
        val dark = AppState.darkUi
        val background = if (dark) Color.Black else Color(0xFFF8F8F8)
        val token = LocalView.current
        val snackbar = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        var confirmAll by remember { mutableStateOf(false) }
        val undoLabel = stringResource(R.string.undelete)
        val pane = panes[page]
        LaunchedEffect(page, pane.loaded) { loadOther() }
        LaunchedEffect(Unit) { archivedCount = withContext(Dispatchers.IO) { DbBookTab.archivedCount(activity) } }
        fun offerUndo(forPane: Pane, title: String) {
            scope.launch {
                snackbar.currentSnackbarData?.dismiss()
                val result = snackbar.showSnackbar(title, undoLabel, duration = SnackbarDuration.Long)
                if (result == SnackbarResult.ActionPerformed) restore(forPane) else commitPending(forPane)
            }
        }
        val request = undoRequest
        LaunchedEffect(request) {
            if (request != null) { undoRequest = null; offerUndo(request.first, request.second) }
        }
        Shell(background) {
            val usable = pane.loaded
            val railRight by ZoneSettings.tabRailRight.flow.collectAsState()
            val pager = rememberPagerState(initialPage = page) { 2 }
            // the tab row and the finger switch pages; both ways keep `page` and the pager together
            LaunchedEffect(pager) { snapshotFlow { pager.currentPage }.collect { if (page != it) { page = it; openGroup = null; draftGroup = null } } }
            LaunchedEffect(page) { if (pager.currentPage != page) pager.animateScrollToPage(page) }
            // the header's buttons, shared by the portrait header row and the landscape side rail (2026-11-23)
            val layoutButton: @Composable () -> Unit = {
                // the view type (moved out of the ⋮ menu, 2026-11-21): the icon shows the current one
                var layoutMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { layoutMenu = true }) {
                        Icon(painterResource(LAYOUTS.first { it.first == layout }.third), contentDescription = stringResource(R.string.type))
                    }
                    com.mycompany.app.ui.components.BubbleMenu(expanded = layoutMenu, onDismiss = { layoutMenu = false }) {
                        for ((value, label, icon) in LAYOUTS) {
                            DropdownMenuItem(
                                text = { Text(stringResource(label)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(icon) },
                                onClick = { layoutMenu = false; chooseLayout(value) },
                                trailingIcon = { if (layout == value) Icon(Sym.Check, contentDescription = null) },
                            )
                        }
                    }
                }
            }
            val searchButton: @Composable () -> Unit = {
                IconButton(onClick = { query = if (query == null) "" else null }) { Icon(Sym.Search, contentDescription = stringResource(R.string.abc_searchview_description_search)) }
            }
            val deleteButton: @Composable () -> Unit = {
                IconButton(onClick = { confirmAll = true }, enabled = pane.loaded && pane.entries.isNotEmpty()) {
                    Icon(Sym.Delete, contentDescription = stringResource(R.string.delete_all))
                }
            }
            val moreButton: @Composable () -> Unit = {
                var menu by remember { mutableStateOf(false) }
                var posMenu by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Sym.MoreVert, contentDescription = stringResource(R.string.more_options)) }
                    com.mycompany.app.ui.components.BubbleMenu(expanded = menu, onDismiss = { menu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.tab_select)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_check_box_outline_blank_black_24) }, enabled = pane.loaded && pane.entries.isNotEmpty(),
                            onClick = { menu = false; openGroup = null; selecting = true; selected.clear() },
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.clean_up)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_tab_close_black_24) }, enabled = pane.loaded && pane.entries.isNotEmpty(),
                            onClick = { menu = false; openGroup = null; cleanUpOpen = true },
                        )
                        DropdownMenuItem(text = { Text(stringResource(R.string.outline_color)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_palette_black_24) }, onClick = { menu = false; chooseOutlineColour() })
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.mini_mode)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_center_focus_weak_black_24) }, onClick = { menu = false; toggleMini() },
                            trailingIcon = { if (ZoneSettings.tabMiniMode.value) Icon(Sym.Check, contentDescription = null) },
                        )
                        DropdownMenuItem(
                        text = { Text(stringResource(R.string.tab_rail_right)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_tab_move_black_24) },
                        onClick = { menu = false; ZoneSettings.edit(activity) { set(ZoneSettings.tabRailRight, !ZoneSettings.tabRailRight.value) } },
                        trailingIcon = { if (railRight) Icon(Sym.Check, contentDescription = null) },
                    )
                    DropdownMenuItem(text = { Text(stringResource(R.string.tab_current_pos)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_tab_move_black_24) }, onClick = { menu = false; posMenu = true })
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.swipe_delete)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_swipe_down_black_24) }, onClick = { menu = false; toggleSwipeDelete() },
                            trailingIcon = { if (swipeDelete) Icon(Sym.Check, contentDescription = null) },
                        )
                        DropdownMenuItem(text = { Text(stringResource(R.string.undelete)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_restart_alt_black_20) }, onClick = { menu = false; openUndeleteSettings() })
                    }
                    com.mycompany.app.ui.components.BubbleMenu(expanded = posMenu, onDismiss = { posMenu = false }) {
                        for ((value, label) in listOf(0 to R.string.pos_like_address, 1 to R.string.at_top, 2 to R.string.at_bottom)) {
                            DropdownMenuItem(
                                text = { Text(stringResource(label)) }, onClick = { posMenu = false; chooseCurrentPos(value) },
                                trailingIcon = { if (currentPos == value) Icon(Sym.Check, contentDescription = null) },
                            )
                        }
                    }
                }
            }
            val searchField: @Composable () -> Unit = {
            val search = query
            if (search != null && !selecting) {
                val focus = remember { FocusRequester() }
                LaunchedEffect(Unit) { try { focus.requestFocus() } catch (_: Exception) {} }
                OutlinedTextField(
                    value = search, onValueChange = { query = it }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).focusRequester(focus),
                    label = { Text(stringResource(R.string.search_url)) },
                )
            }
            }
            val pages: @Composable (Modifier) -> Unit = { modifier ->
                Box(modifier) {
                HorizontalPager(
                    state = pager, modifier = Modifier.fillMaxSize(),
                    userScrollEnabled = !selecting && !dragging && !swipeDelete && query == null, beyondViewportPageCount = 1,
                ) { index ->
                    val item = panes[index]
                    Box(Modifier.fillMaxSize()) {
                        if (!item.loaded) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        } else {
                            Content(item, token, onCloseEntry = { title, entry -> if (closeEntry(item, entry)) offerUndo(item, title) })
                        }
                    }
                }
                // (no edge strip any more, 2026-11-23: a strip on the left edge for switching Tabs / Incognito took the system's
                // back gesture there; the pages switch with the Tabs | Incognito switch, or by a swipe when swipe delete is off)
                }
            }
            androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize()) {
                // a phone on its side: the header and the buttons move to a rail on the left, so the cards keep the height
                if (maxWidth > maxHeight && maxHeight < 520.dp) {
                    Row(
                        Modifier.fillMaxSize().then(if (mini) Modifier else Modifier.statusBarsPadding())
                            .navigationBarsPadding().displayCutoutPadding(),
                    ) {
                        val rail: @Composable () -> Unit = {
                            SideRail(pane, usable, layoutButton, searchButton, deleteButton, moreButton, onDeleteSelected = {
                                val count = selected.size; if (deleteSelected(pane)) offerUndo(pane, count.toString())
                            })
                        }
                        // the rail on the left, or on the right ("Tab overview controls on the right", ZoneSettings.tabRailRight)
                        if (!railRight) { rail(); androidx.compose.material3.VerticalDivider() }
                        Column(Modifier.weight(1f).fillMaxHeight()) {
                            searchField()
                            pages(Modifier.weight(1f).fillMaxWidth())
                        }
                        if (railRight) { androidx.compose.material3.VerticalDivider(); rail() }
                    }
                } else {
            Column(Modifier.fillMaxSize().then(if (mini) Modifier else Modifier.statusBarsPadding())) {
                Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (selecting) {
                        IconButton(onClick = { endSelection() }) { Icon(Sym.Close, contentDescription = stringResource(R.string.close)) }
                        Text(
                            MainUtil.countLabel(selected.size, pane.entries.size), style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f),
                        )
                        Text(stringResource(R.string.all), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                        Checkbox(
                            checked = pane.entries.isNotEmpty() && selected.size == pane.entries.size,
                            onCheckedChange = { all -> selected.clear(); if (all) selected.addAll(pane.entries) },
                        )
                    } else {
                        IconButton(onClick = { dismiss() }) { Icon(Sym.ArrowBack, contentDescription = stringResource(R.string.abc_action_bar_up_description)) }
                        Box(Modifier.weight(1f))
                        layoutButton()
                        searchButton()
                        deleteButton()
                        moreButton()
                    }
                }
                searchField()
                // Tabs | Incognito with their counts on a row of their own, so two- and three-digit counts fit
                if (!selecting) com.mycompany.app.ui.components.CapFontScale {
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                        for (index in 0..1) {
                            val item = panes[index]
                            val count = if (item.loaded) " " + item.entries.sumOf { it.tabs.size } else ""
                            SegmentedButton(
                                selected = page == index,
                                onClick = { if (index == 0 && page == 0) spaceMenu = true else { page = index; openGroup = null; draftGroup = null } },
                                shape = SegmentedButtonDefaults.itemShape(index, 2),
                                icon = {
                                    if (index == 1) Icon(painterResource(R.drawable.outline_secret_mode_white_18), contentDescription = null, modifier = Modifier.size(16.dp))
                                },
                                label = {
                                    if (index == 0) {
                                        SpaceLabel(count.trim())
                                    } else {
                                        Text(
                                            stringResource(R.string.incognito_short) + count,
                                            maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelLarge,
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
                if (!selecting) CleanUpHint(pane)
                if (!selecting) ArchivedRow(pane)
                pages(Modifier.weight(1f).fillMaxWidth())
                HorizontalDivider()
                com.mycompany.app.ui.components.CapFontScale {
                Row(Modifier.fillMaxWidth().navigationBarsPadding().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (selecting) {
                        TextButton(onClick = { val count = selected.size; if (deleteSelected(pane)) offerUndo(pane, count.toString()) }, enabled = selected.isNotEmpty(), modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.delete), style = MaterialTheme.typography.titleMedium)
                        }
                    } else {
                        // a new group gets its tabs added right away, so only on the page of the current mode
                        OutlinedButton(
                            onClick = { openGroup = null; draftGroup = Triple(System.currentTimeMillis(), null, NEW_GROUP_COLOR) }, enabled = usable && pane.active,
                            modifier = Modifier.weight(1f).padding(start = 12.dp, end = 6.dp),
                        ) {
                            Icon(painterResource(R.drawable.outline_create_new_folder_black_20), contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(stringResource(R.string.new_group), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 8.dp))
                        }
                        Button(onClick = { newTab(pane) }, enabled = usable, modifier = Modifier.weight(1f).padding(start = 6.dp, end = 12.dp)) {
                            Icon(Sym.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text(stringResource(R.string.new_url), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
                }
            }
                }
            }
            // the group popup
            val groupId = openGroup
            val groupEntry = if (groupId == null) null else pane.entries.firstOrNull { it.group && it.tabs[0].groupId == groupId }
            if (groupId != null && groupEntry == null) openGroup = null
            if (groupEntry != null) {
                GroupPopup(pane, groupEntry, token, onCloseMember = { title, entry, tab -> if (closeMember(pane, entry, tab)) offerUndo(pane, title) })
            }
            val draft = draftGroup
            if (draft != null && groupEntry == null) {
                GroupFrame(pane, draft.second, draft.third, 0, onBack = { draftGroup = null }, menu = null, newTab = { newTabInGroup(pane, null) }) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.group_empty), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (mini && showHint) {
                Surface(
                    shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.inverseSurface,
                    modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 72.dp)
                        .clickable { dismissHint() },
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.tab_guide_1), color = MaterialTheme.colorScheme.inverseOnSurface, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            stringResource(R.string.tab_guide_2), color = MaterialTheme.colorScheme.inverseOnSurface, style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
            }
            SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 64.dp))
        }
        if (cleanUpOpen) CleanUpDialog(pane)
        if (archiveOpen) ArchivedSheet(panes[0])
        if (confirmAll) {
            AlertDialog(
                onDismissRequest = { confirmAll = false },
                title = { Text(stringResource(R.string.delete_all)) },
                confirmButton = {
                    TextButton(colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = androidx.compose.material3.MaterialTheme.colorScheme.error), onClick = {
                        confirmAll = false
                        closeAll(pane)
                        if (pane.active) dismiss() else { persistOther(pane); commitPending(pane) }
                    }) { Text(stringResource(R.string.delete_all)) }
                },
                dismissButton = { TextButton(onClick = { confirmAll = false }) { Text(stringResource(R.string.cancel)) } },
            )
        }
    }

    private fun entryTitle(entry: Entry): String {
        val head = entry.tabs.first()
        return if (entry.group) (head.groupName?.takeIf { it.isNotEmpty() } ?: activity.getString(R.string.group_title)) else (WebViewActivity.tabTitleFor(activity, head) ?: "")
    }

    @Composable
    private fun Content(pane: Pane, token: View, onCloseEntry: (String, Entry) -> Unit) {
        val scope = rememberCoroutineScope()
        val shown = pane.entries.filter { matches(it) }
        if (shown.isEmpty() && !query.isNullOrBlank()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("—", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            return
        }
        if (layout == 0) { Grid(pane, token, onCloseEntry); return }
        if (layout == 3 || layout == 4) { Carousel(pane, token, layout == 3, onCloseEntry); return }
        val listState = rememberLazyListState()
        LaunchedEffect(pane) {
            val target = shown.indexOfFirst { pane.currentTab != null && it.tabs.contains(pane.currentTab) }
            if (target > 0) listState.scrollToItem(target)
            if (atBottom && target >= 0) {
                // the current row rests at the bottom edge instead of the top
                val info = listState.layoutInfo
                val item = info.visibleItemsInfo.firstOrNull()
                if (item != null) {
                    val room = info.viewportEndOffset - info.viewportStartOffset - item.size - info.afterContentPadding
                    if (room > 0) listState.scrollBy(-room.toFloat())
                }
            }
        }
        var drag by remember { mutableStateOf<GridDrag?>(null) }
        val density = LocalDensity.current
        val rowSlots = { listState.layoutInfo.visibleItemsInfo.map { Slot(it.index, 0f, it.offset.toFloat(), listState.layoutInfo.viewportSize.width.toFloat(), it.size.toFloat()) } }
        LaunchedEffect(drag) { dragging = drag != null }
        LaunchedEffect(drag) {
            val held = drag ?: return@LaunchedEffect
            while (true) {
                delay(16)
                val info = listState.layoutInfo
                val slot = info.visibleItemsInfo.firstOrNull { it.index == pane.entries.indexOf(held.entry) } ?: continue
                val centre = slot.offset + held.dy + slot.size / 2f
                val edge = with(density) { 90.dp.toPx() }
                val step = with(density) { 12.dp.toPx() }
                val delta = when {
                    centre < info.viewportStartOffset + edge -> -step
                    centre > info.viewportEndOffset - edge -> step
                    else -> 0f
                }
                if (delta != 0f) listState.scrollBy(delta)
            }
        }
        Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState, modifier = Modifier.fillMaxSize(), userScrollEnabled = drag == null, contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp, if (atBottom) Alignment.Bottom else Alignment.Top),
        ) {
            listItems(shown, key = { System.identityHashCode(it) }) { entry ->
                val mine = drag?.entry === entry
                val hovered = drag?.hover === entry
                val modifier = Modifier
                    .pointerInput(entry) { cardGestures(pane, entry, rowSlots, { drag }, { drag = it }, { onCloseEntry(entryTitle(it), it) }) }
                    // the gestures above are raw touch handling: one node for TalkBack / tests (reads the title, opens like a tap)
                    .semantics(mergeDescendants = true) { onClick { onCardTap(pane, entry); true } }
                    .graphicsLayer {
                        val dragNow = drag
                        if (dragNow != null && dragNow.entry === entry && dragNow.swipe) { translationX = dragNow.dx; alpha = (1f - abs(dragNow.dx) / size.width).coerceIn(0f, 1f) }
                        else if (dragNow != null && dragNow.entry === entry) { translationX = dragNow.dx; translationY = dragNow.dy; scaleX = 1.03f; scaleY = 1.03f; shadowElevation = 16f }
                        else if (hovered) { scaleX = 1.04f; scaleY = 1.04f }
                    }
                    .zIndex(if (mine) 1f else 0f)
                EntryRow(pane, entry, token, layout == 2, modifier) { title -> onCloseEntry(title, entry) }
            }
        }
        ScrollThumb(
            {
                val size = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
                listState.firstVisibleItemIndex + if (size > 0) listState.firstVisibleItemScrollOffset.toFloat() / (size + listState.layoutInfo.mainAxisItemSpacing) else 0f
            },
            shown.size, Modifier.align(Alignment.CenterEnd), label = { "${it + 1} / ${shown.size}" },
        ) { pos ->
            // continuous: the row and how far into it
            val index = pos.toInt().coerceIn(0, (shown.size - 1).coerceAtLeast(0))
            val size = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
            scope.launch { listState.scrollToItem(index, ((pos - index) * (size + listState.layoutInfo.mainAxisItemSpacing)).roundToInt()) }
        }
        }
    }

    /**
     * Layouts 3 (vertical: stacked cards) and 4 (horizontal: cover flow), 2026-11-21. One card is in focus in the middle;
     * the others get smaller and slide under it (in the cover flow also turned away), all following the swipe. Tap a
     * side card to bring it to the middle, tap the middle card to open it (or tick it while selecting); swipe a card
     * across the carousel (sideways in the vertical one, up in the horizontal one) to close it, with undo as elsewhere.
     * Moving cards by long press and dropping one on another are the grid's and the list's.
     */
    @Composable
    private fun Carousel(pane: Pane, token: View, vertical: Boolean, onCloseEntry: (String, Entry) -> Unit) {
        val shown = pane.entries.filter { matches(it) }
        if (shown.isEmpty()) return
        val start = shown.indexOfFirst { entry -> pane.currentTab != null && entry.tabs.contains(pane.currentTab) }.coerceAtLeast(0)
        // the count follows the list (closing and undo change it while the pager stays)
        val count by androidx.compose.runtime.rememberUpdatedState(shown.size)
        val pager = androidx.compose.foundation.pager.rememberPagerState(initialPage = start) { count }
        val scope = rememberCoroutineScope()
        androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize()) {
            // a card about the shape of the page; the space around it shows the neighbours
            val aspect = if (vertical) STACK_ASPECT else CAROUSEL_ASPECT
            val cardWidth = if (vertical) minOf(maxWidth * 0.88f, maxHeight * 0.66f * aspect, 480.dp)
                else minOf(maxWidth * 0.62f, maxHeight * 0.72f * aspect, 340.dp)
            val cardHeight = if (vertical) cardWidth / aspect else minOf(cardWidth / CAROUSEL_TALL_ASPECT, maxHeight * 0.86f)
            // the shape the cards are drawn in (the carousel's is taller than the one its width came from)
            val cardAspect = cardWidth / cardHeight
            val density = LocalDensity.current
            val item: @Composable (Int) -> Unit = item@{ page ->
                val entry = shown.getOrNull(page) ?: return@item
                val swipe = remember(entry) { androidx.compose.animation.core.Animatable(0f) }
                // distance from the card in focus, in cards (negative before it), following the finger
                val distance = { (page - pager.currentPage - pager.currentPageOffsetFraction) }
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val gesture = Modifier.pointerInput(entry, vertical) {
                        val limit = (if (vertical) size.width else size.height) * 0.35f
                        val onEnd: () -> Unit = {
                            dragging = false
                            if (abs(swipe.value) > limit) onCloseEntry(entryTitle(entry), entry)
                            else scope.launch { swipe.animateTo(0f) }
                        }
                        if (vertical) {
                            detectHorizontalDragGestures(onDragEnd = onEnd, onDragCancel = onEnd) { change, amount ->
                                change.consume(); dragging = true; scope.launch { swipe.snapTo(swipe.value + amount) }
                            }
                        } else {
                            detectVerticalDragGestures(onDragEnd = onEnd, onDragCancel = onEnd) { change, amount ->
                                // up only: a card is thrown off the top
                                change.consume(); dragging = true; scope.launch { swipe.snapTo((swipe.value + amount).coerceAtMost(0f)) }
                            }
                        }
                    }
                    val tap = Modifier.pointerInput(entry, page) {
                        detectTapGestures {
                            if (page != pager.currentPage) scope.launch { pager.animateScrollToPage(page) } else onCardTap(pane, entry)
                        }
                    }
                    val transform = Modifier.graphicsLayer {
                        val d = distance()
                        val far = abs(d)
                        if (vertical) {
                            // stacked: the neighbours slide behind the card in focus and peek out above and below it
                            // the pages are 0.62 of a card apart; pulled in, each neighbour peeks out about 0.3 of a card
                            translationY = -d * size.height * 0.32f
                            val scale = (1f - 0.08f * far).coerceAtLeast(0.6f)
                            scaleX = scale; scaleY = scale
                            translationX = swipe.value
                            alpha = (1f - abs(swipe.value) / size.width).coerceIn(0f, 1f) * (if (far > 3.5f) 0f else 1f)
                        } else {
                            // cover flow: smaller, turned away and tucked under the middle card
                            // the pages are 0.62 of a card apart; the neighbours sit a little closer, half under the middle card
                            translationX = -d * size.width * 0.08f
                            val scale = (1f - 0.16f * far).coerceAtLeast(0.55f)
                            scaleX = scale; scaleY = scale
                            rotationY = (-d * 32f).coerceIn(-55f, 55f)
                            cameraDistance = 12f * density.density
                            translationY = swipe.value
                            alpha = (1f - abs(swipe.value) / size.height).coerceIn(0f, 1f) * (if (far > 2.5f) 0f else 1f)
                        }
                    }
                    val checked: Boolean? = if (selecting) selected.contains(entry) else null
                    val modifier = Modifier.then(transform).then(gesture).then(tap)
                    Box(Modifier.width(cardWidth)) {
                        if (entry.group) GroupCard(pane, entry, token, modifier, checked, aspect = cardAspect)
                        else TabCard(pane, entry.tabs[0], token, modifier, onClose = { title -> onCloseEntry(title, entry) }, checked = checked, aspect = cardAspect)
                    }
                }
            }
            // scrolls like a page: a fast fling carries on over many cards and slows down, then settles on the nearest one
            val fling = androidx.compose.foundation.pager.PagerDefaults.flingBehavior(
                state = pager, pagerSnapDistance = androidx.compose.foundation.pager.PagerSnapDistance.atMost(CAROUSEL_FLING_CARDS),
                // a lighter slow-down than the default (which stops a fling of these big cards after one)
                decayAnimationSpec = remember { androidx.compose.animation.core.exponentialDecay(frictionMultiplier = CAROUSEL_FRICTION) },
            )
            val position = { index: Int -> "${index + 1} / ${shown.size}" }
            // the handle scrolls continuously (a page and a fraction of the next), then settles on the nearest card
            val at = { pager.currentPage + pager.currentPageOffsetFraction }
            val jump = { pos: Float ->
                val page = pos.roundToInt().coerceIn(0, (shown.size - 1).coerceAtLeast(0))
                scope.launch { pager.scrollToPage(page, (pos - page).coerceIn(-0.5f, 0.5f)) }; Unit
            }
            val settle = { scope.launch { pager.animateScrollToPage(pager.currentPage) }; Unit }
            if (vertical) {
                androidx.compose.foundation.pager.VerticalPager(
                    state = pager, modifier = Modifier.fillMaxSize(), flingBehavior = fling, pageSize = androidx.compose.foundation.pager.PageSize.Fixed(cardHeight * 0.62f),
                    contentPadding = PaddingValues(vertical = (maxHeight - cardHeight * 0.62f) / 2), beyondViewportPageCount = 4,
                    key = { index -> shown.getOrNull(index)?.let { System.identityHashCode(it) } ?: -1 - index },
                ) { page -> Box(Modifier.zIndex(-abs(page - pager.currentPage - pager.currentPageOffsetFraction)).requiredHeight(cardHeight), contentAlignment = Alignment.Center) { item(page) } }
                ScrollThumb(at, shown.size, Modifier.align(Alignment.CenterEnd), label = position, min = 2, onRelease = settle, onJump = jump)
            } else {
                HorizontalPager(
                    state = pager, modifier = Modifier.fillMaxSize(), flingBehavior = fling, pageSize = androidx.compose.foundation.pager.PageSize.Fixed(cardWidth * 0.62f),
                    contentPadding = PaddingValues(horizontal = (maxWidth - cardWidth * 0.62f) / 2), beyondViewportPageCount = 3,
                    key = { index -> shown.getOrNull(index)?.let { System.identityHashCode(it) } ?: -1 - index },
                ) { page -> Box(Modifier.zIndex(-abs(page - pager.currentPage - pager.currentPageOffsetFraction)).requiredWidth(cardWidth), contentAlignment = Alignment.Center) { item(page) } }
                ScrollThumbHorizontal(at, shown.size, Modifier.align(Alignment.BottomCenter), label = position, min = 2, onRelease = settle, onJump = jump)
            }
        }
    }

    /**
     * The handle itself, drawn like the web page's scrollbar ([com.mycompany.app.view.MyScrollBar]): a 12 dp wide, 32 dp
     * long rounded bar in the page bar's colours (lavender on light, near-white on dark, at its alpha); the accent while held.
     */
    @Composable
    private fun ThumbBar(held: Boolean, modifier: Modifier, vertical: Boolean) {
        val color = if (held) MaterialTheme.colorScheme.primary else Color(if (AppState.darkUi) 0xFFFAFAFA else 0xFFA6A9EA).copy(alpha = 161f / 255f)
        val thick = if (held) 14.dp else 12.dp
        Box(
            modifier.size(width = if (vertical) thick else 32.dp, height = if (vertical) 32.dp else thick)
                .clip(RoundedCornerShape(8.dp)).background(color),
        )
    }

    /** [ScrollThumb] lying along the bottom edge, for the horizontal carousel: drag it sideways to jump. */
    @Composable
    private fun ScrollThumbHorizontal(first: () -> Float, count: Int, modifier: Modifier, label: (Int) -> String, min: Int = 12, onRelease: () -> Unit = {}, onJump: (Float) -> Unit) {
        if (count < min) return
        var held by remember { mutableStateOf(false) }
        androidx.compose.foundation.layout.BoxWithConstraints(modifier.fillMaxWidth().height(36.dp).padding(horizontal = 24.dp)) {
            val density = LocalDensity.current
            val trackPx = with(density) { maxWidth.toPx() }
            val thumbPx = with(density) { THUMB_TOUCH.toPx() }
            val at = first()
            val fraction = (at / (count - 1).coerceAtLeast(1)).coerceIn(0f, 1f)
            val thumbX = with(density) { (fraction * (trackPx - thumbPx)).toDp() }
            Box(
                Modifier.fillMaxSize().pointerInput(count) {
                    detectHorizontalDragGestures(
                        onDragStart = { held = true },
                        onDragEnd = { held = false; onRelease() },
                        onDragCancel = { held = false },
                    ) { change, _ ->
                        change.consume()
                        val fractionValue = ((change.position.x - thumbPx / 2f) / (trackPx - thumbPx)).coerceIn(0f, 1f)
                        // a position between cards (not a card number): the cards glide with the finger like a scroll
                        onJump(fractionValue * (count - 1))
                    }
                },
            ) {
                Box(Modifier.align(Alignment.CenterStart).offset(x = thumbX).size(width = THUMB_TOUCH, height = 36.dp)) {
                    ThumbBar(held, Modifier.align(Alignment.Center), vertical = false)
                }
            }
            if (held) {
                Surface(
                    shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer, shadowElevation = 4.dp,
                    // above the handle
                    modifier = Modifier.align(Alignment.BottomStart).offset(x = thumbX - 12.dp, y = (-40).dp).wrapContentWidth(Alignment.Start, unbounded = true),
                ) {
                    Text(
                        label(at.roundToInt()), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }

    /** A card in the list layouts: icon, title (and the address), close / group menu. */
    @Composable
    private fun EntryRow(pane: Pane, entry: Entry, token: View, simple: Boolean, modifier: Modifier, onClose: (String) -> Unit) {
        pane.tick
        val tabs = entry.tabs
        val head = tabs.first()
        val isCurrent = pane.currentTab != null && tabs.contains(pane.currentTab)
        val accent = accent()
        val outline = if (isCurrent) accent else if (entry.group) Color(if (head.groupColor == 0) -65536 else head.groupColor).copy(alpha = 0.7f) else MaterialTheme.colorScheme.outlineVariant
        val title = entryTitle(entry)
        val iconBackground = if (entry.group) Color(if (head.groupColor == 0) -65536 else head.groupColor) else Color(if (AppState.darkUi) -12632257 else -12434878)
        val checked: Boolean? = if (selecting) this@ComposeTabOverview.selected.contains(entry) else null
        Surface(
            shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(if (isCurrent) 2.5.dp else 1.dp, outline),
            modifier = modifier.fillMaxWidth().height(if (simple) 48.dp else 68.dp).halo(isCurrent, accent, 12.dp)
                // a row opens with the zoom too, from the row (replaces a card's place left from another layout)
                .then(if (entry.group) Modifier else Modifier.onGloballyPositioned { cardBounds[head.uid] = it.boundsInWindow() }),
        ) {
            Row(Modifier.fillMaxSize().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(if (simple) 28.dp else 36.dp).background(iconBackground, CircleShape), contentAlignment = Alignment.Center) {
                    if (entry.group) Image(painterResource(R.drawable.baseline_folder_white_24), contentDescription = null, modifier = Modifier.size(20.dp))
                    else Favicon(head, token)
                }
                Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                    Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    if (!simple) Text(
                        if (entry.group) tabs.size.toString() else (head.tabUrl ?: ""), maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (checked != null) {
                    Checkbox(checked = checked, onCheckedChange = null)
                } else if (entry.group) {
                    var menu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { menu = true }) { Icon(Sym.MoreVert, contentDescription = stringResource(R.string.more_options)) }
                        com.mycompany.app.ui.components.BubbleMenu(expanded = menu, onDismiss = { menu = false }) {
                            DropdownMenuItem(text = { Text(stringResource(R.string.edit)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_edit_black_24) }, onClick = { menu = false; editGroup(pane, entry) })
                            DropdownMenuItem(text = { Text(stringResource(R.string.delete)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_delete_black_20) }, colors = com.mycompany.app.ui.components.destructiveItemColors(), onClick = { menu = false; onClose(title) })
                        }
                    }
                } else {
                    IconButton(onClick = { onClose(title) }) { Icon(Sym.Close, contentDescription = stringResource(R.string.close)) }
                }
            }
        }
    }

    @Composable
    private fun Grid(pane: Pane, token: View, onCloseEntry: (String, Entry) -> Unit) {
        val state = rememberLazyGridState()
        var drag by remember { mutableStateOf<GridDrag?>(null) }
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        LaunchedEffect(pane) {
            val target = pane.entries.indexOfFirst { pane.currentTab != null && it.tabs.contains(pane.currentTab) }
            if (target > 1) state.scrollToItem(target)
            if (atBottom && target >= 0) {
                // the current card rests at the bottom edge instead of the top
                val info = state.layoutInfo
                val item = info.visibleItemsInfo.firstOrNull()
                if (item != null) {
                    val room = info.viewportEndOffset - info.viewportStartOffset - item.size.height - info.afterContentPadding
                    if (room > 0) state.scrollBy(-room.toFloat())
                }
            }
        }
        val gridSlots = { state.layoutInfo.visibleItemsInfo.map { Slot(it.index, it.offset.x.toFloat(), it.offset.y.toFloat(), it.size.width.toFloat(), it.size.height.toFloat()) } }
        LaunchedEffect(drag) { dragging = drag != null }
        // scroll while a card is held near the top or bottom edge
        LaunchedEffect(drag) {
            val held = drag ?: return@LaunchedEffect
            while (true) {
                delay(16)
                val info = state.layoutInfo
                val slot = info.visibleItemsInfo.firstOrNull { it.index == pane.entries.indexOf(held.entry) } ?: continue
                val centre = slot.offset.y + held.dy + slot.size.height / 2f
                val edge = with(density) { 90.dp.toPx() }
                val step = with(density) { 12.dp.toPx() }
                val delta = when {
                    centre < info.viewportStartOffset + edge -> -step
                    centre > info.viewportEndOffset - edge -> step
                    else -> 0f
                }
                if (delta != 0f) state.scrollBy(delta)
            }
        }
        androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize()) {
        val cols = gridColumns(maxWidth)
        LazyVerticalGrid(
            columns = GridCells.Fixed(cols), state = state, modifier = Modifier.fillMaxSize(), userScrollEnabled = drag == null,
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, if (atBottom) Alignment.Bottom else Alignment.Top),
        ) {
            items(pane.entries.filter { matches(it) }, key = { System.identityHashCode(it) }) { entry ->
                val mine = drag?.entry === entry
                val hovered = drag?.hover === entry
                val modifier = Modifier
                    .pointerInput(entry) { cardGestures(pane, entry, gridSlots, { drag }, { drag = it }, { onCloseEntry(entryTitle(it), it) }) }
                    // the gestures above are raw touch handling: one node for TalkBack / tests (reads the title, opens like a tap)
                    .semantics(mergeDescendants = true) { onClick { onCardTap(pane, entry); true } }
                    .graphicsLayer {
                        val dragNow = drag
                        if (dragNow != null && dragNow.entry === entry && dragNow.swipe) { translationX = dragNow.dx; alpha = (1f - abs(dragNow.dx) / size.width).coerceIn(0f, 1f) }
                        else if (dragNow != null && dragNow.entry === entry) { translationX = dragNow.dx; translationY = dragNow.dy; scaleX = 1.05f; scaleY = 1.05f; shadowElevation = 16f }
                        else if (hovered) { scaleX = 1.06f; scaleY = 1.06f }
                    }
                    .zIndex(if (mine) 1f else 0f)
                val checked: Boolean? = if (selecting) selected.contains(entry) else null
                if (entry.group) {
                    GroupCard(pane, entry, token, modifier, checked)
                } else {
                    val tab = entry.tabs[0]
                    TabCard(pane, tab, token, modifier, onClose = { title -> onCloseEntry(title, entry) }, checked = checked)
                }
            }
        }
        val shownCount = pane.entries.count { matches(it) }
        val rows = (shownCount + cols - 1) / cols
        ScrollThumb(
            {
                val height = state.layoutInfo.visibleItemsInfo.firstOrNull()?.size?.height ?: 0
                state.firstVisibleItemIndex / cols + if (height > 0) state.firstVisibleItemScrollOffset.toFloat() / (height + state.layoutInfo.mainAxisItemSpacing) else 0f
            },
            rows, Modifier.align(Alignment.CenterEnd),
            label = { row -> "${(row * cols + 1).coerceAtMost(shownCount)} / $shownCount" },
        ) { pos ->
            // continuous: the row and how far into it
            val row = pos.toInt()
            val height = state.layoutInfo.visibleItemsInfo.firstOrNull()?.size?.height ?: 0
            scope.launch {
                state.scrollToItem((row * cols).coerceIn(0, (shownCount - 1).coerceAtLeast(0)), ((pos - row) * (height + state.layoutInfo.mainAxisItemSpacing)).roundToInt())
            }
        }
        }
    }

    /**
     * A fast-scroll handle at the right edge: shows where the visible part is, drag it to jump ([count] rows, [first] the first
     * visible one). While it is dragged a bubble beside it shows where the jump goes ([label] of the row).
     */
    @Composable
    private fun ScrollThumb(first: () -> Float, count: Int, modifier: Modifier, label: (Int) -> String, min: Int = 12, onRelease: () -> Unit = {}, onJump: (Float) -> Unit) {
        if (count < min) return
        var held by remember { mutableStateOf(false) }
        androidx.compose.foundation.layout.BoxWithConstraints(modifier.fillMaxHeight().width(36.dp)) {
            val density = LocalDensity.current
            val trackPx = with(density) { maxHeight.toPx() }
            val thumbPx = with(density) { THUMB_TOUCH.toPx() }
            val at = first()
            val fraction = (at / (count - 1).coerceAtLeast(1)).coerceIn(0f, 1f)
            val thumbY = with(density) { (fraction * (trackPx - thumbPx)).toDp() }
            Box(
                Modifier.align(Alignment.TopEnd).fillMaxHeight().width(36.dp).pointerInput(count) {
                    detectVerticalDragGestures(
                        onDragStart = { held = true },
                        onDragEnd = { held = false; onRelease() },
                        onDragCancel = { held = false },
                    ) { change, _ ->
                        change.consume()
                        val fractionValue = ((change.position.y - thumbPx / 2f) / (trackPx - thumbPx)).coerceIn(0f, 1f)
                        // a position between cards (not a card number): the cards glide with the finger like a scroll
                        onJump(fractionValue * (count - 1))
                    }
                },
            ) {
                // the strip at the screen edge belongs to the system back gesture unless it is excluded
                Box(
                    Modifier.align(Alignment.TopEnd).offset(y = thumbY).size(width = 36.dp, height = THUMB_TOUCH).then(if (groupShown) Modifier else Modifier.gestureExclusion()),
                ) {
                    ThumbBar(held, Modifier.align(Alignment.Center), vertical = true)
                }
            }
            if (held) {
                Surface(
                    shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer, shadowElevation = 4.dp,
                    // drawn outside the narrow strip, to the left of the handle
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = (-42).dp, y = thumbY + 6.dp).wrapContentWidth(Alignment.End, unbounded = true),
                ) {
                    Text(
                        label(at.roundToInt()), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }

    /** Tap = open / select / tick; long press = pick the card up: it follows the finger, passing a card's middle swaps places, resting over a card and letting go groups them. */
    private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.cardGestures(
        pane: Pane, entry: Entry, slots: () -> List<Slot>, current: () -> GridDrag?, set: (GridDrag?) -> Unit, swipeClose: (Entry) -> Unit,
    ) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = true)
            val slop = viewConfiguration.touchSlop
            val outcome = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                var result = 0 // 1 tap, 3 moved (the grid scrolls)
                while (result == 0) {
                    val event = awaitPointerEvent()
                    val change = event.changes.firstOrNull { it.id == down.id } ?: return@withTimeoutOrNull 3
                    if (change.isConsumed && !change.pressed) return@withTimeoutOrNull 3
                    if (!change.pressed) { change.consume(); return@withTimeoutOrNull 1 }
                    val d = change.position - down.position
                    if (ZoneSettings.swipeDelete.value && !selecting && query.isNullOrBlank() && abs(d.x) > slop && abs(d.x) > abs(d.y)) return@withTimeoutOrNull 2
                    if (abs(d.x) > slop || abs(d.y) > slop) return@withTimeoutOrNull 3
                }
                result
            }
            when (outcome) {
                1 -> onCardTap(pane, entry)
                2 -> {
                    val held = GridDrag(entry)
                    held.swipe = true
                    set(held)
                    val width = slots().firstOrNull { it.index == pane.entries.filter { entry2 -> matches(entry2) }.indexOf(entry) }?.width ?: 1f
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) { change.consume(); break }
                        held.dx = change.position.x - down.position.x
                        change.consume()
                    }
                    val commit = abs(held.dx) > width * 0.4f
                    set(null)
                    if (commit) swipeClose(entry)
                }
                null -> {
                    if (selecting) { onCardTap(pane, entry); return@awaitEachGesture }
                    if (!query.isNullOrBlank()) return@awaitEachGesture
                    val held = GridDrag(entry)
                    set(held)
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) { change.consume(); break }
                        held.dx = change.position.x - down.position.x
                        held.dy = change.position.y - down.position.y
                        change.consume()
                        val all = slots()
                        val myIndex = pane.entries.indexOf(held.entry)
                        val slot = all.firstOrNull { it.index == myIndex } ?: continue
                        val cx = slot.left + held.dx + slot.width / 2f
                        val cy = slot.top + held.dy + slot.height / 2f
                        val target = all.firstOrNull {
                            it.index != myIndex && cx >= it.left && cx < it.left + it.width && cy >= it.top && cy < it.top + it.height
                        }
                        if (target == null) { held.hover = null; continue }
                        val tx = target.left + target.width / 2f
                        val ty = target.top + target.height / 2f
                        val sameRow = abs(target.top - slot.top) < slot.height / 2
                        val passed = if (sameRow) { if (target.index > myIndex) cx >= tx else cx <= tx } else { if (target.index > myIndex) cy >= ty else cy <= ty }
                        if (passed) {
                            moveEntry(pane, myIndex, target.index)
                            held.hover = null
                        } else {
                            held.hover = pane.entries.getOrNull(target.index)
                        }
                    }
                    val over = held.hover
                    set(null)
                    if (over != null) mergeEntries(pane, held.entry, over)
                }
            }
        }
    }

    private fun matches(entry: Entry): Boolean {
        val queryText = query?.trim().orEmpty()
        if (queryText.isEmpty()) return true
        return entry.tabs.any {
            (it.tabUrl?.contains(queryText, ignoreCase = true) == true) || (it.tabTitle?.contains(queryText, ignoreCase = true) == true) ||
                (WebViewActivity.tabTitleFor(activity, it)?.contains(queryText, ignoreCase = true) == true) || (it.groupName?.contains(queryText, ignoreCase = true) == true)
        }
    }

    private fun chooseLayout(value: Int) {
        if (layout == value) return
        layout = value
        ZoneSettings.edit(activity) { set(ZoneSettings.tabListType, value) }
    }

    private fun chooseOutlineColour() {
        if (editIcon != null) return
        // in a workspace its own outline colour, kept with the workspace
        val type = if (Workspaces.current() != null) DialogEditIcon.SPACE_OUTLINE else 6
        val dialog = DialogEditIcon(activity, type, object : EditorSetListener {
            override fun onApply(color: Int, label: String?) { panes.forEach { it.tick++ } }
        })
        dialog.setOnDismissListener { editIcon = null; panes.forEach { it.tick++ } }
        editIcon = dialog
    }

    private fun openUndeleteSettings() {
        if (restoreDialog != null) return
        val dialog = DialogSetTabRestore(activity, null)
        dialog.setOnDismissListener { restoreDialog = null }
        restoreDialog = dialog
    }

    private fun toggleMini() {
        val value = !ZoneSettings.tabMiniMode.value
        ZoneSettings.edit(activity) { set(ZoneSettings.tabMiniMode, value) }
        dismiss()
    }

    /** The address bar is at the bottom: it is in the bottom bar, or in the top bar that is shown at the bottom. */
    private fun addressAtBottom(): Boolean {
        val address = 1 // the address bar's id in the bar layouts (`LayoutSettingsActivity`)
        if (MainUtil.parseIntList(MainSettings.botItems.value)?.contains(address) == true) return true
        return PdfSettings.topToBottom.value && MainUtil.parseIntList(MainSettings.topItems.value)?.contains(address) == true
    }

    private fun chooseCurrentPos(value: Int) {
        currentPos = value
        ZtwoSettings.edit(activity) {
            set(ZtwoSettings.tabCurrentPos, value)
            set(ZtwoSettings.tabDown, value == 2) // the old "At the bottom" switch, kept in step
        }
    }

    private fun dismissHint() {
        if (!showHint) return
        showHint = false
        if (AlbumSettings.guideTab.value) {
            AlbumSettings.edit(activity) { set(AlbumSettings.guideTab, false) }
        }
    }

    private fun toggleSwipeDelete() {
        val value = !ZoneSettings.swipeDelete.value
        ZoneSettings.editLocal { set(ZoneSettings.swipeDelete, value) }
        swipeDelete = value
        PrefSet.putBool(15, activity, "mSwipeDelete", value)
    }

    private fun onCardTap(pane: Pane, entry: Entry) {
        if (selecting) {
            if (selected.contains(entry)) selected.remove(entry) else selected.add(entry)
        } else if (entry.group) {
            openGroup = entry.tabs[0].groupId
        } else {
            open(pane, entry.tabs[0])
        }
    }

    /** The group popup: the group's tabs as cards over the page. */
    @Composable
    private fun GroupPopup(pane: Pane, entry: Entry, token: View, onCloseMember: (String, Entry, WebTabItem) -> Unit) {
        val head = entry.tabs.first()
        pane.tick
        GroupFrame(
            pane, head.groupName, head.groupColor, entry.tabs.size, onBack = { openGroup = null },
            menu = { dismiss ->
                DropdownMenuItem(text = { Text(stringResource(R.string.edit)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_edit_black_24) }, onClick = { dismiss(); editGroup(pane, entry) })
                DropdownMenuItem(text = { Text(stringResource(R.string.group_none)) }, onClick = { dismiss(); ungroupAll(pane, entry); openGroup = null })
            },
            newTab = { newTabInGroup(pane, entry) },
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp), modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(entry.tabs, key = { it.uid }) { tab ->
                    TabCard(
                        pane, tab, token, Modifier.clickable { open(pane, tab) },
                        onClose = { t -> onCloseMember(t, entry, tab) },
                        extra = { dismissMenu -> DropdownMenuItem(text = { Text(stringResource(R.string.group_none)) }, onClick = { dismissMenu(); removeFromGroup(pane, entry, tab) }) },
                    )
                }
            }
        }
    }

    /**
     * A group's page over the overview, in the floating card of the link / image previews ([PreviewCard], 2026-11-20): the
     * bar (on the main bar's side) has close, the group's folder icon in its colour, name and tab count, "New tab" (adds a
     * tab to the group; only on the page of the current mode) and the optional menu; [content] below / above it. Back,
     * a tap beside the card and dragging the bar down close it ([onBack]).
     */
    @Composable
    private fun GroupFrame(
        pane: Pane, name: String?, colour: Int, count: Int, onBack: () -> Unit, menu: (@Composable (() -> Unit) -> Unit)?, newTab: () -> Unit,
        content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit,
    ) {
        val ctx = LocalContext.current
        val title = name?.takeIf { it.isNotEmpty() } ?: stringResource(R.string.group_title)
        val color = Color(if (colour == 0) -65536 else colour)
        val barOnTop = remember { previewBarOnTop() }
        PreviewCard(
            barOnTop = barOnTop, onClosed = onBack, onOutside = onBack,
            bar = {
                Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Sym.Close, contentDescription = stringResource(R.string.close)) }
                    Box(Modifier.size(28.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
                        Image(painterResource(R.drawable.baseline_folder_white_24), contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        if (count > 0) "$title ($count)" else title, style = MaterialTheme.typography.titleMedium, maxLines = 1,
                        overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                    )
                    IconButton(onClick = newTab, enabled = pane.active) { Icon(Sym.Add, contentDescription = stringResource(R.string.new_url)) }
                    if (menu != null) {
                        var open by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { open = true }) { Icon(Sym.MoreVert, contentDescription = stringResource(R.string.more_options)) }
                            com.mycompany.app.ui.components.BubbleMenu(expanded = open, onDismiss = { open = false }) { menu { open = false } }
                        }
                    }
                }
            },
            content = content,
        )
    }

    /**
     * The accent colour of the current card: in a workspace the outline colour chosen there, else the workspace's
     * colour (2026-11-25); otherwise the outline colour setting.
     */
    @Composable
    private fun accent(): Color {
        val list by WorkspaceSettings.list.flow.collectAsState()
        val space = list.let { Workspaces.current() }
        return Color((Workspaces.outlineOf(space) ?: PrefEditor.gridArgb) or (0xFF shl 24))
    }

    /** A single tab as a card: the page's picture (or a placeholder), then a slim row with favicon, two-line title, menu and close. */
    @Composable
    private fun TabCard(
        pane: Pane, tab: WebTabItem, token: View, modifier: Modifier, onClose: (String) -> Unit,
        extra: (@Composable (() -> Unit) -> Unit)? = null, checked: Boolean? = null, aspect: Float = 0.9f,
    ) {
        val ctx = LocalContext.current
        pane.tick
        val selected = pane.currentTab === tab
        val accent = accent()
        val outline = if (selected) accent else MaterialTheme.colorScheme.outlineVariant
        val title = WebViewActivity.tabTitleFor(ctx, tab) ?: ""
        Surface(
            shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(if (selected) 2.5.dp else 1.dp, outline),
            modifier = Modifier.fillMaxWidth().aspectRatio(aspect).then(modifier).halo(selected, accent, 14.dp),
        ) {
            Column {
                Box(Modifier.weight(1f).fillMaxWidth().onGloballyPositioned { cardBounds[tab.uid] = it.boundsInWindow() }) {
                    Thumb(tab, token, pane.secret)
                    if (checked != null) {
                        Checkbox(checked = checked, onCheckedChange = null, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp))
                    } else {
                        // a small close button on the picture instead of a wide header bar
                        Box(
                            Modifier.align(Alignment.TopEnd).padding(6.dp).size(26.dp).clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.45f)).clickable { onClose(title) },
                            contentAlignment = Alignment.Center,
                        ) { Icon(Sym.Close, contentDescription = stringResource(R.string.close), tint = Color.White, modifier = Modifier.size(16.dp)) }
                    }
                }
                Row(Modifier.fillMaxWidth().height(46.dp).padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Favicon(tab, token)
                    Text(
                        title, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(start = 8.dp, end = if (extra == null) 8.dp else 0.dp).weight(1f),
                    )
                    if (extra != null) {
                        var menu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { menu = true }, modifier = Modifier.size(32.dp)) {
                                Icon(Sym.MoreVert, contentDescription = stringResource(R.string.more_options), modifier = Modifier.size(18.dp))
                            }
                            com.mycompany.app.ui.components.BubbleMenu(expanded = menu, onDismiss = { menu = false }) { extra { menu = false } }
                        }
                    }
                }
            }
        }
    }

    /** A group as a card: tinted in the group colour, up to four member pictures, folder icon, name, tab count and a menu (Edit, Delete). */
    @Composable
    private fun GroupCard(pane: Pane, entry: Entry, token: View, modifier: Modifier, checked: Boolean?, aspect: Float = 0.9f) {
        val ctx = LocalContext.current
        pane.tick
        val tabs = entry.tabs
        val selected = pane.currentTab != null && tabs.contains(pane.currentTab)
        val head = tabs.first()
        val accent = accent()
        val groupColor = Color(if (head.groupColor == 0) -65536 else head.groupColor)
        val outline = if (selected) accent else groupColor.copy(alpha = 0.7f)
        val title = head.groupName?.takeIf { it.isNotEmpty() } ?: stringResource(R.string.group_title)
        val groupTitle = title
        Surface(
            shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(if (selected) 2.5.dp else 1.5.dp, outline),
            modifier = Modifier.fillMaxWidth().aspectRatio(aspect).then(modifier).halo(selected, accent, 14.dp),
        ) {
            Column(Modifier.background(groupColor.copy(alpha = 0.10f))) {
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    // a one-tab group (a new group's first tab) shows that tab's picture full size
                    if (tabs.size == 1) Box(Modifier.fillMaxSize()) { Thumb(head, token, pane.secret) }
                    else Column(Modifier.fillMaxSize().padding(5.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (row in tabs.take(4).chunked(2)) {
                            Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                for (member in row) {
                                    Box(Modifier.weight(1f).fillMaxSize().clip(RoundedCornerShape(8.dp))) { Thumb(member, token, pane.secret) }
                                }
                                if (row.size == 1) Box(Modifier.weight(1f))
                            }
                        }
                    }
                    if (checked != null) Checkbox(checked = checked, onCheckedChange = null, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp))
                }
                Row(Modifier.fillMaxWidth().height(46.dp).padding(start = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.baseline_folder_white_24), contentDescription = null, tint = groupColor, modifier = Modifier.size(20.dp))
                    Text(
                        title, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(start = 8.dp).weight(1f, fill = false),
                    )
                    // tab count badge
                    Text(
                        tabs.size.toString(), color = if (groupColor.luminance() > 0.5f) Color.Black else Color.White, style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 6.dp).background(groupColor, RoundedCornerShape(50)).padding(horizontal = 7.dp, vertical = 1.dp),
                    )
                    Box(Modifier.weight(1f, fill = true))
                    var menu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { menu = true }, modifier = Modifier.size(36.dp)) {
                            Icon(Sym.MoreVert, contentDescription = stringResource(R.string.more_options), modifier = Modifier.size(18.dp))
                        }
                        com.mycompany.app.ui.components.BubbleMenu(expanded = menu, onDismiss = { menu = false }) {
                            DropdownMenuItem(text = { Text(stringResource(R.string.edit)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_edit_black_24) }, onClick = { menu = false; editGroup(pane, entry) })
                            DropdownMenuItem(text = { Text(stringResource(R.string.delete)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_delete_black_20) }, colors = com.mycompany.app.ui.components.destructiveItemColors(), onClick = {
                                menu = false
                                if (closeEntry(pane, entry)) offerUndoFromCard(pane, groupTitle)
                            })
                        }
                    }
                }
            }
        }
    }

    /** Snackbar requests from cards that have no access to the page's snackbar state. */
    private var undoRequest by mutableStateOf<Pair<Pane, String>?>(null)
    private fun offerUndoFromCard(pane: Pane, title: String) { undoRequest = pane to title }

    @Composable
    private fun Favicon(tab: WebTabItem, token: View) {
        var loadedIcon by remember(tab.uid, tab.tabUrl) { mutableStateOf(cachedFavicon(tab)) }
        LaunchedEffect(tab.uid, tab.tabUrl) { if (loadedIcon == null) loadedIcon = loadFavicon(tab) }
        val bitmap = loadedIcon
        val url = tab.tabUrl
        Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
            if (bitmap != null) {
                Image(bitmap.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(CircleShape))
            } else {
                val res = when {
                    url.isNullOrEmpty() -> R.drawable.outline_public_white_24
                    START_PAGE == url -> R.drawable.outline_home_white_24
                    url.startsWith("file:///") || url.startsWith("/data/") -> R.drawable.outline_offline_pin_white_24
                    else -> R.drawable.outline_public_white_24
                }
                Image(painterResource(res), contentDescription = null, modifier = Modifier.fillMaxSize())
            }
        }
    }

    @Composable
    private fun Thumb(tab: WebTabItem, token: View, secret: Boolean) {
        var loaded by remember(tab.uid, tab.tabUrl, secret) { mutableStateOf(cachedSnapshot(tab, secret)) }
        LaunchedEffect(tab.uid, tab.tabUrl, secret) { if (loaded == null) loaded = loadSnapshot(tab, secret) }
        val bitmap = if (hasPicture(tab.tabUrl)) fresh[tab.uid] ?: loaded else null
        val dark = AppState.darkUi
        if (bitmap != null) {
            Box(Modifier.fillMaxSize().background(if (dark) Color.Black else Color.White)) {
                Image(bitmap.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, alignment = Alignment.TopCenter, modifier = Modifier.fillMaxSize())
            }
            return
        }
        // no snapshot yet (the page has not been shown since the app started): the site's icon and name on its own colour
        val url = tab.tabUrl
        val host = hostOf(url)
        Column(
            Modifier.fillMaxSize().background(siteColor(host, dark)).padding(8.dp),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            var icon by remember(tab.uid, tab.tabUrl) { mutableStateOf(cachedFavicon(tab)) }
            LaunchedEffect(tab.uid, tab.tabUrl) { if (icon == null) icon = loadFavicon(tab) }
            val bitmapIcon = icon
            if (bitmapIcon != null) {
                Image(
                    bitmapIcon.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color.White),
                )
            } else {
                val res = when {
                    START_PAGE == url -> R.drawable.outline_home_white_24
                    !url.isNullOrEmpty() && (url.startsWith("file:///") || url.startsWith("/data/")) -> R.drawable.outline_offline_pin_white_24
                    else -> R.drawable.outline_public_white_24
                }
                Icon(painterResource(res), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(44.dp))
            }
            if (host != null) {
                Text(
                    host, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f), modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }

    /** True while a group's page is open over the overview. */
    private val groupShown get() = openGroup != null || draftGroup != null

    /** Back (key and edge gesture, through the dialog's default back callback) closes a group's page first. */
    @Deprecated("Dialog back callback")
    override fun onBackPressed() {
        if (holding) { close(); return }
        if (groupShown) { openGroup = null; draftGroup = null; return }
        @Suppress("DEPRECATION") super.onBackPressed()
    }

    /** True once [dismiss] after a zoom is waiting for the page ([holdForPage]). */
    private var holding = false

    override fun dismiss() {
        // after the zoom the enlarged picture stays up until the tab's page is in place: otherwise the page shown before
        // flashes up for a moment when the tab switches
        if (opening != null && isShowing && !holding) { holding = true; holdForPage(opening!!); return }
        // select() dismisses again right after the listener did: still waiting (unless the browser is going away)
        if (holding && isShowing && !activity.isFinishing && !activity.isDestroyed) return
        close()
    }

    /**
     * Waits (at most [HOLD_MAX] ms) until the opened tab's page is really there, then [close]s: its frame is the current
     * one and on screen (the browser swaps it in through a chain of posts, about half a second for a tab whose page was
     * freed), its WebView has a page with laid-out content (a freed page starts blank and loads again; a neighbour's frame
     * may exist with nothing in it yet), and that content has been drawn. A tab whose page is there passes at once.
     */
    private fun holdForPage(open: Opening) {
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        var done = false
        fun finish() {
            if (done) return
            done = true
            handler.removeCallbacksAndMessages(null)
            handOver = true
            // in case the fade never runs (the window lost its frames): close anyway
            handler.postDelayed({ close() }, 600L)
        }
        handler.postDelayed({ finish() }, HOLD_MAX)
        // the picture is only a picture now: touches go to the browser underneath (its bars are already showing)
        window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        fun check() {
            if (done) return
            // the switch is posted after the overview starts closing: wait for the opened tab's own frame on screen
            val frame = activity.tabsState.currentFrame
            val swapped = frame != null && frame === open.tab.webFrame && frame.isShown
            val web = if (swapped) firstWebView(frame) else null
            if (web != null && web.isAttachedToWindow && web.url != null && (web.contentHeight > 0 || web.progress >= 60)) web.postVisualStateCallback(0L, object : android.webkit.WebView.VisualStateCallback() {
                override fun onComplete(requestId: Long) { handler.post { finish() } }
            }) else handler.postDelayed({ check() }, 16L)
        }
        check()
    }

    private fun firstWebView(view: View?): android.webkit.WebView? {
        if (view is android.webkit.WebView) return if (view.visibility == View.VISIBLE) view else null
        if (view is android.view.ViewGroup) for (i in 0 until view.childCount) firstWebView(view.getChildAt(i))?.let { return it }
        return null
    }

    private fun close() {
        val wasShowing = isShowing
        if (wasShowing) applyOnExit()
        edit?.dismiss()
        edit = null
        editIcon?.dismiss()
        editIcon = null
        restoreDialog?.dismiss()
        restoreDialog = null
        fresh.clear()
        super.dismiss()
    }

    companion object {
        /** At most two pictures decode at once, so a fast scroll does not crowd out the UI. */
        /**
         * The colour a new group gets (2026-11-20): a calm slate teal that reads in light and dark mode, instead of the old
         * red. Stored in the group's tabs; groups saved with colour 0 (from before) still show red.
         */
        const val NEW_GROUP_COLOR = 0xFF5B8A8C.toInt()

        /** The longest the zoomed picture waits for the opened tab's page before the overview closes anyway. */
        private const val HOLD_MAX = 1500L

        @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
        private val pictureLoads = Dispatchers.IO.limitedParallelism(2)

        /**
         * The Compose overview is the default; the old `DialogTabMain` stays as a fallback until it is deleted and is used when
         * the hidden preference `opum_flags` / `compose_tab_overview` is set to false.
         */
        @JvmStatic
        fun enabled(context: Context): Boolean = try {
            context.getSharedPreferences("opum_flags", Context.MODE_PRIVATE).getBoolean("compose_tab_overview", true)
        } catch (_: Exception) { true }
    }
}

/** Keeps the Android back gesture off this element (the screen edge is otherwise the system's), so edge controls can be dragged. */
@androidx.compose.runtime.Composable
private fun Modifier.gestureExclusion(): Modifier {
    val view = androidx.compose.ui.platform.LocalView.current
    var rect by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<android.graphics.Rect?>(null) }
    androidx.compose.runtime.DisposableEffect(rect) {
        val mine = rect
        if (mine != null) view.systemGestureExclusionRects = view.systemGestureExclusionRects + mine
        onDispose { if (mine != null) view.systemGestureExclusionRects = view.systemGestureExclusionRects - mine }
    }
    return this.onGloballyPositioned { coordinates ->
        val bounds = coordinates.boundsInWindow()
        rect = android.graphics.Rect(bounds.left.toInt(), bounds.top.toInt(), bounds.right.toInt(), bounds.bottom.toInt())
    }
}

/** Grid columns for a content width: two on phones, more on wide screens (cards at least about 150 dp wide). */
private fun gridColumns(width: androidx.compose.ui.unit.Dp): Int = ((width.value - 12f) / (150f + 12f)).toInt().coerceAtLeast(2)

/** The host of [url] without "www.", or null (blank page, files, the start page). */
private fun hostOf(url: String?): String? {
    if (url.isNullOrEmpty() || !(url.startsWith("http://") || url.startsWith("https://"))) return null
    return try { android.net.Uri.parse(url).host?.removePrefix("www.")?.takeIf { it.isNotEmpty() } } catch (_: Exception) { null }
}

/** A soft colour of its own for each site, behind cards that have no snapshot yet. */
private fun siteColor(host: String?, dark: Boolean): Color {
    if (host == null) return if (dark) Color(0xFF1E1F24) else Color(0xFFEDEEF2)
    val hue = ((host.hashCode() and 0x7fffffff) % 360).toFloat()
    return Color.hsl(hue, if (dark) 0.28f else 0.45f, if (dark) 0.20f else 0.88f)
}

/** Same address, ignoring the scheme, "www.", a trailing slash and the #fragment (for "close duplicate tabs"). */
private fun sameAddressKey(url: String?): String? {
    if (url.isNullOrEmpty()) return null
    var key = url.substringBefore('#').removePrefix("https://").removePrefix("http://").removePrefix("www.")
    while (key.endsWith("/")) key = key.dropLast(1)
    return key.ifEmpty { null }
}

/**
 * A soft halo around the current card: rings of [color] fading outwards, breathing slowly (the card is opaque, so only the
 * part outside it shows). [corner] is the card's corner radius.
 */
@Composable
private fun Modifier.halo(on: Boolean, color: Color, corner: androidx.compose.ui.unit.Dp): Modifier {
    if (!on) return this
    val pulse = androidx.compose.animation.core.rememberInfiniteTransition(label = "halo").animateFloat(
        0f, 1f,
        androidx.compose.animation.core.infiniteRepeatable(
            androidx.compose.animation.core.tween(1400, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            androidx.compose.animation.core.RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    return this.drawBehind {
        val p = pulse.value
        val reach = 10.dp.toPx() * (0.55f + 0.45f * p)
        val radius = corner.toPx()
        val rings = 7
        for (i in rings downTo 1) {
            val spread = reach * i / rings
            val alpha = (0.10f + 0.16f * p) * (1f - (i - 1f) / rings)
            drawRoundRect(
                color.copy(alpha = alpha),
                topLeft = androidx.compose.ui.geometry.Offset(-spread, -spread),
                size = androidx.compose.ui.geometry.Size(size.width + 2 * spread, size.height + 2 * spread),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius + spread),
            )
        }
    }
}

