package com.mycompany.app.ui.settings

import com.mycompany.app.ui.browser.groupNotch
import android.content.Context
import com.mycompany.app.pref.store.MainSettings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import com.mycompany.app.ui.browser.ActionCatalog
import com.mycompany.app.ui.browser.MenuActionIcon
import com.mycompany.app.ui.browser.MenuCatalog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSaveConfirm
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.dialog.DialogSetColumn
import com.mycompany.app.dialog.DialogSetItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.prefs.OnScreenResume

internal const val BLANK = 68
internal const val MID_MAX = 9
private const val MENU_ZONE = "menu"
private const val QUICK_ZONE = "quick"

/**
 * The menu editor (drag and drop, 2026-11-19): a preview of the menu as it opens (header, actions in the chosen style,
 * "All actions", quick bar) with the style and columns under it, and the grouped, searchable action list below. Hold an
 * action to drag it to another place or between the menu and the quick bar, or down onto the list to remove it; drag new
 * actions (and spaces) up from the list, or tap one to add it to the menu. Tap an item to change, move or remove it.
 * Ready-made layouts are in the ⋮ menu. The lists are saved with the check; leaving asks. Style and columns apply at once.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class MenuSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val orig = remember {
            Pair(
                MenuCatalog.clean(MainUtil.parseIntList(MainSettings.menuItems.value))?.toList() ?: emptyList(),
                MenuCatalog.clean(MainUtil.parseIntList(MainSettings.midItems.value))?.toList() ?: emptyList(),
            )
        }
        var savedMenu by remember { mutableStateOf(orig.first) }
        var savedMid by remember { mutableStateOf(orig.second) }
        val menu: SnapshotStateList<Int> = remember { mutableStateListOf<Int>().also { it.addAll(orig.first) } }
        val mid: SnapshotStateList<Int> = remember { mutableStateListOf<Int>().also { it.addAll(orig.second) } }
        var columns by remember { mutableIntStateOf(if (MainSettings.menuColumns.value == 0) 5 else MainSettings.menuColumns.value) }
        var style by remember { mutableIntStateOf(MainSettings.menuType.value) }
        var action by remember { mutableStateOf<Pair<Boolean, Int>?>(null) } // (inMenu, index)
        var picking by remember { mutableStateOf<Pair<Boolean, Int>?>(null) } // (inMenu, index or -1 to add)
        var clearing by remember { mutableStateOf<Boolean?>(null) }
        var resetting by remember { mutableStateOf(false) }
        val changed = menu.toList() != savedMenu || mid.toList() != savedMid

        OnScreenResume { columns = if (MainSettings.menuColumns.value == 0) 5 else MainSettings.menuColumns.value }

        fun listFor(inMenu: Boolean) = if (inMenu) menu else mid

        fun save() {
            MainSettings.edit(context) {
                if (menu.toList() != savedMenu) { set(MainSettings.menuItems, MainUtil.joinIntList(menu.toIntArray()) ?: ""); savedMenu = menu.toList() }
                if (mid.toList() != savedMid) { set(MainSettings.midItems, MainUtil.joinIntList(mid.toIntArray()) ?: ""); savedMid = mid.toList() }
            }
        }

        fun apply(preset: Pair<IntArray, IntArray>) {
            menu.clear(); menu.addAll(preset.first.toList())
            mid.clear(); mid.addAll(preset.second.toList())
        }

        fun reset() {
            MainSettings.reset(context, listOf(MainSettings.menuItems, MainSettings.midItems, MainSettings.menuType, MainSettings.menuColumns))
            style = MainSettings.menuType.value
            columns = MainSettings.menuColumns.value
            apply(MenuCatalog.STANDARD)
            savedMenu = menu.toList()
            savedMid = mid.toList()
        }

        fun leave() {
            if (!changed) { finish(); return }
            DialogSaveConfirm(this, object : DialogSeekListener {
                override fun onValue(newValue: Int) {
                    if (newValue == 0) save()
                    finish()
                }
            })
        }

        fun add(inMenu: Boolean) {
            if (!inMenu && mid.size >= MID_MAX) { MainUtil.showToast(context, R.string.not_space); return }
            picking = inMenu to -1
        }

        val dnd = remember { DndState() }
        dnd.onDrop = drop@{ item, zone, index ->
            val target = if (zone == MENU_ZONE || zone == QUICK_ZONE) zone else null
            fun listOf2(zone: String) = if (zone == MENU_ZONE) menu else mid
            when {
                item.zone == TRAY && target != null -> {
                    if (target == QUICK_ZONE && mid.size >= MID_MAX) { MainUtil.showToast(context, R.string.not_space); return@drop }
                    listOf2(target).add(index.coerceIn(0, listOf2(target).size), item.id)
                }
                item.zone != TRAY && zone == TRAY -> listOf2(item.zone).removeAt(item.index)
                item.zone != TRAY && target != null -> {
                    if (item.zone == target) {
                        val list = listOf2(target)
                        val to = if (index > item.index) index - 1 else index
                        if (to != item.index) list.add(to.coerceIn(0, list.size - 1), list.removeAt(item.index))
                    } else {
                        if (target == QUICK_ZONE && mid.size >= MID_MAX) { MainUtil.showToast(context, R.string.not_space); return@drop }
                        val id = listOf2(item.zone).removeAt(item.index)
                        listOf2(target).add(index.coerceIn(0, listOf2(target).size), id)
                    }
                }
            }
        }

        BackHandler { leave() }
        val (rootMod, rootOrigin) = rememberRootOrigin()

        androidx.compose.material3.Scaffold(
            topBar = {
                com.mycompany.app.ui.components.SettingsTopBar(stringResource(R.string.list_menu), { leave() }, actions = {
                    // resets the whole page (menu, quick bar, style, columns) after asking
                    IconButton(onClick = { resetting = true }) { Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.reset)) }
                    if (changed) IconButton(onClick = { save(); finish() }) { Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.ok)) }
                })
            },
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize().then(rootMod)) {
                Column(Modifier.fillMaxSize()) {
                    MenuPreview(dnd, menu, mid, style, columns, onTap = { inMenu, index -> action = inMenu to index })
                    // style and columns, applied at once
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        var open by remember { mutableStateOf(false) }
                        Box(Modifier.weight(1f)) {
                            OutlinedButton(onClick = { open = true }, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 12.dp)) {
                                Text(stringResource(styleLabel(style)), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                Icon(Icons.Filled.ArrowDropDown, null)
                            }
                            com.mycompany.app.ui.components.BubbleMenu(expanded = open, onDismiss = { open = false }) {
                                for (kind in intArrayOf(4, 3, 2, 1, 0)) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(styleLabel(kind))) },
                                        trailingIcon = { if (style == kind) Icon(Icons.Filled.Check, null) },
                                        onClick = { open = false; style = kind; MainSettings.edit(context) { set(MainSettings.menuType, kind) } },
                                    )
                                }
                            }
                        }
                        if (style != 0 && style != 1) {
                            val counts = intArrayOf(4, 5, 6)
                            SingleChoiceSegmentedButtonRow(Modifier.weight(1f)) {
                                counts.forEachIndexed { index, count ->
                                    SegmentedButton(
                                        selected = columns == count, shape = SegmentedButtonDefaults.itemShape(index, counts.size), icon = {},
                                        onClick = { columns = count; MainSettings.edit(context) { set(MainSettings.menuColumns, count) } },
                                        label = { Text(count.toString()) },
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        stringResource(R.string.dnd_hint_menu), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                    )
                    HorizontalDivider()
                    ActionTray(
                        dnd, Modifier.fillMaxWidth().weight(1f), used = (menu + mid).toSet(),
                        onTap = { id -> menu.add(id) },
                        extraGroup = R.string.space_title to intArrayOf(BLANK),
                        removeHint = { context.getString(R.string.drop_remove) },
                    )
                }
                DragGhost(dnd, rootOrigin) { item -> editorLabelPlain(context, item.id) }
            }
        }

        action?.let { (inMenu, index) ->
            val list = listFor(inMenu)
            if (index >= list.size) { action = null; return@let }
            val other = listFor(!inMenu)
            com.mycompany.app.ui.components.OpumSheetDialog(
                onDismissRequest = { action = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (list[index] == BLANK) Box(Modifier.size(28.dp)) else MenuActionIcon(list[index], 28.dp)
                        Text(editorLabel(list[index]), modifier = Modifier.padding(start = 12.dp))
                    }
                },
                text = {
                    Column {
                        ActionRow(stringResource(R.string.do_change)) { action = null; picking = inMenu to index }
                        ActionRow(stringResource(if (inMenu) R.string.move_to_quick else R.string.move_to_menu)) {
                            if (inMenu && other.size >= MID_MAX) MainUtil.showToast(context, R.string.not_space) else other.add(list.removeAt(index))
                            action = null
                        }
                        ActionRow(stringResource(R.string.remove_item)) { list.removeAt(index); action = null }
                    }
                },
                confirmButton = {},
                dismissButton = { TextButton(onClick = { action = null }) { Text(stringResource(R.string.cancel)) } },
            )
        }
        picking?.let { (inMenu, index) ->
            com.mycompany.app.ui.components.OpumSheetBox(onDismiss = { picking = null }) {
                androidx.compose.foundation.layout.Box {
                    Column(Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                        Text(
                            stringResource(if (index < 0) R.string.add_action else R.string.do_change), style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                        ActionCatalog(
                            onPick = { id ->
                                val list = listFor(inMenu)
                                if (index < 0) list.add(id) else if (index < list.size) list[index] = id
                                picking = null
                            },
                            chosen = (menu + mid).toSet(), maxHeight = 460.dp,
                        )
                        TextButton(onClick = { picking = null }, modifier = Modifier.align(Alignment.End).padding(end = 12.dp)) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                }
            }
        }
        clearing?.let { inMenu ->
            ConfirmDialog(
                message = stringResource(R.string.del_all_confirm),
                title = stringResource(R.string.clear_list),
                onConfirm = { listFor(inMenu).clear() },
                onDismiss = { clearing = null },
            )
        }
        if (resetting) {
            ConfirmDialog(
                title = stringResource(R.string.reset),
                message = stringResource(R.string.reset_setting),
                onConfirm = { reset() },
                onDismiss = { resetting = false },
            )
        }
    }

    /**
     * The menu as it opens in the browser: the labelled header (for orientation), the actions in the chosen style (pages of
     * the grid separated), the fixed "All actions" tile, and the quick bar. Items are drag sources; the grid and the quick
     * bar are drop zones.
     */
    @Composable
    private fun MenuPreview(dnd: DndState, menu: List<Int>, mid: List<Int>, style: Int, columns: Int, onTap: (Boolean, Int) -> Unit) {
        val list = style == 0 || style == 1
        val cols = if (list) 1 else columns.coerceAtLeast(1)
        val perPage = if (style == 2 || list) Int.MAX_VALUE else cols * (if (style == 3) 2 else 3)
        Surface(
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Column {
                // header, as in the menu (not editable here)
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp).alpha(0.55f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (label in intArrayOf(R.string.mh_restart, R.string.mh_cleaner, R.string.mh_edit, R.string.setting)) {
                        Text(stringResource(label), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    }
                }
                HorizontalDivider()
                dnd.trim(MENU_ZONE, menu.size)
                Column(
                    Modifier.fillMaxWidth().heightIn(max = 250.dp).verticalScroll(rememberScrollState())
                        .dropZone(dnd, MENU_ZONE).dropMarker(dnd, MENU_ZONE).padding(horizontal = 4.dp, vertical = 4.dp),
                ) {
                    val cells = menu.indices.toList() + listOf(-1) // -1: the "All actions" tile
                    cells.chunked(perPage).forEachIndexed { page, pageCells ->
                        if (page > 0) {
                            Text(
                                "· ${page + 1} ·", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), textAlign = TextAlign.Center,
                            )
                        }
                        for (row in pageCells.chunked(cols)) {
                            Row(Modifier.fillMaxWidth()) {
                                for (index in row) {
                                    val id = if (index < 0) MenuCatalog.ALL else menu[index]
                                    val cell = Modifier.weight(1f).height(if (list) 36.dp else 62.dp)
                                    if (index < 0) {
                                        Box(cell.alpha(0.6f)) { PreviewCell(id, list) }
                                    } else {
                                        Box(cell.dropSlot(dnd, MENU_ZONE, index).dragSource(dnd, DragItem(id, MENU_ZONE, index)) { onTap(true, index) }) { PreviewCell(id, list) }
                                    }
                                }
                                repeat(cols - row.size) { Box(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
                HorizontalDivider()
                dnd.trim(QUICK_ZONE, mid.size)
                if (mid.isEmpty()) {
                    EmptyBarZone(dnd, QUICK_ZONE, stringResource(R.string.drop_add_bar), Modifier.fillMaxWidth().height(48.dp).padding(6.dp))
                } else {
                    Row(Modifier.fillMaxWidth().height(56.dp).dropZone(dnd, QUICK_ZONE).dropMarker(dnd, QUICK_ZONE).padding(horizontal = 4.dp)) {
                        mid.forEachIndexed { index, id ->
                            Box(Modifier.weight(1f).fillMaxHeight().dropSlot(dnd, QUICK_ZONE, index).dragSource(dnd, DragItem(id, QUICK_ZONE, index)) { onTap(false, index) }) {
                                PreviewCell(id, false)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun PreviewCell(id: Int, list: Boolean) {
        val label = if (id == MenuCatalog.ALL) stringResource(R.string.all_actions) else editorLabel(id)
        if (list) {
            Row(Modifier.fillMaxSize().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (id == MenuCatalog.ALL) MenuActionIcon(id, 18.dp) else EditorIcon(id, 18.dp)
                Text(label, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 12.dp))
            }
        } else {
            Column(Modifier.fillMaxSize().padding(2.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                if (id == MenuCatalog.ALL) MenuActionIcon(id, 20.dp) else EditorIcon(id, 20.dp)
                Text(
                    label, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified),
                    maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
    }

    private fun styleLabel(kind: Int) = when (kind) {
        0 -> R.string.small_list
        1 -> R.string.large_list
        3 -> R.string.two_lines
        4 -> R.string.three_lines
        else -> R.string.expand_mode
    }
}

/** A block of the editor: a rounded card with a title (and an optional line on the right). */
@Composable
internal fun EditorBlock(title: String, summary: String? = null, content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                if (summary != null) Text(summary, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content()
        }
    }
}

/** Labelled buttons under a list: add an action, add a blank space, clear the list. */
@Composable
internal fun EditorButtons(onAdd: () -> Unit, onSpace: () -> Unit, onClear: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilledTonalButton(onClick = onAdd, modifier = Modifier.weight(1.4f), contentPadding = PaddingValues(horizontal = 8.dp)) {
            Icon(Icons.Filled.Add, null, Modifier.size(18.dp))
            Text(stringResource(R.string.add_action), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 6.dp))
        }
        OutlinedButton(onClick = onSpace, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp)) {
            Text(stringResource(R.string.add_space), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        TextButton(onClick = onClear, contentPadding = PaddingValues(horizontal = 8.dp)) { Text(stringResource(R.string.clear_list), maxLines = 1) }
    }
}

@Composable
internal fun editorLabel(id: Int): String = if (id == BLANK) stringResource(R.string.space_title) else stringResource(MenuCatalog.labelRes(id))

/** An item's icon in the editors: blank space, the address bar (search icon), the menu button, or the action's icon. */
@Composable
internal fun EditorIcon(id: Int, size: Dp) {
    when (id) {
        BLANK -> Box(Modifier.size(size))
        1 -> Icon(painterResource(R.drawable.outline_search_black_24), null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(size))
        2 -> Icon(painterResource(MainUtil.moreIconFor(MainUtil.barTint(0, false), false)), null, tint = androidx.compose.ui.graphics.Color.Unspecified, modifier = Modifier.size(size))
        else -> if (com.mycompany.app.ui.browser.BarGroups.isGroup(id)) {
            // a button group: its lead button's icon (a stack while empty) with the corner notch
            val lead = com.mycompany.app.ui.browser.LocalGroupMembers.current(id).firstOrNull() ?: com.mycompany.app.ui.browser.BarGroups.NEW
            Box(Modifier.size(size).groupNotch(MaterialTheme.colorScheme.onSurface)) { MenuActionIcon(lead, size) }
        } else MenuActionIcon(id, size)
    }
}

/** One item in the previews: a tile (icon over label) or, for the list styles, a row. */
@Composable
internal fun EditorTile(id: Int, list: Boolean) {
    Surface(
        shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), modifier = Modifier.fillMaxSize(),
    ) {
        if (list) {
            Row(Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                EditorIcon(id, 22.dp)
                Text(editorLabel(id), style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 14.dp))
            }
        } else {
            Column(Modifier.padding(2.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                EditorIcon(id, 24.dp)
                Text(
                    // no extra letter spacing, so words like "Downloads" fit a narrow tile without breaking
                    editorLabel(id), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified), maxLines = 2, overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

/**
 * The items of [items] in a grid of [columns]; a tap reports the index, a long press picks an item up so it can be dragged
 * to another place (the list is reordered while it moves).
 */
@Composable
internal fun DragGrid(items: List<Int>, columns: Int, cellHeight: Dp, onTap: (Int) -> Unit, onMove: (Int, Int) -> Unit, cell: @Composable (Int) -> Unit) {
    if (items.isEmpty()) {
        Text(stringResource(R.string.not_used), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(8.dp))
        return
    }
    var dragIndex by remember { mutableIntStateOf(-1) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val cols = columns.coerceAtLeast(1)
        val cellW = maxWidth / cols
        val rows = (items.size + cols - 1) / cols
        val cwPx = with(density) { cellW.toPx() }
        val chPx = with(density) { cellHeight.toPx() }
        val size = items.size
        fun indexAt(p: Offset): Int {
            val c = (p.x / cwPx).toInt().coerceIn(0, cols - 1)
            val r = (p.y / chPx).toInt().coerceIn(0, rows - 1)
            return (r * cols + c).coerceIn(0, size - 1)
        }
        Box(
            Modifier.fillMaxWidth().height(cellHeight * rows)
                .pointerInput(cols, size) { detectTapGestures(onTap = { p -> onTap(indexAt(p)) }) }
                .pointerInput(cols, size) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { p -> dragIndex = indexAt(p); offset = Offset.Zero },
                        onDragEnd = { dragIndex = -1; offset = Offset.Zero },
                        onDragCancel = { dragIndex = -1; offset = Offset.Zero },
                    ) { change, amount ->
                        change.consume()
                        val i = dragIndex
                        if (i < 0) return@detectDragGesturesAfterLongPress
                        offset += amount
                        val centre = Offset((i % cols) * cwPx + cwPx / 2 + offset.x, (i / cols) * chPx + chPx / 2 + offset.y)
                        val target = indexAt(centre)
                        if (target != i) {
                            onMove(i, target)
                            offset -= Offset(((target % cols) - (i % cols)) * cwPx, ((target / cols) - (i / cols)) * chPx)
                            dragIndex = target
                        }
                    }
                },
        ) {
            items.forEachIndexed { i, id ->
                Box(
                    Modifier.offset(cellW * (i % cols), cellHeight * (i / cols)).size(cellW, cellHeight)
                        .zIndex(if (i == dragIndex) 1f else 0f)
                        .padding(2.dp)
                        // the layer has the tile's own size and rounded shape, so the lifted tile casts a rounded shadow
                        .graphicsLayer {
                            shape = RoundedCornerShape(12.dp)
                            clip = false
                            if (i == dragIndex) { translationX = offset.x; translationY = offset.y; scaleX = 1.06f; scaleY = 1.06f; shadowElevation = 12f }
                        },
                ) { cell(id) }
            }
        }
    }
}

@Composable
internal fun ActionRow(label: String, onClick: () -> Unit) {
    Text(
        label,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
    )
}

@Composable
internal fun ListSection(
    title: String,
    items: List<Int>,
    columns: Int,
    inMenu: Boolean,
    onItem: (Int) -> Unit,
    onSettings: () -> Unit,
    onDeleteAll: () -> Unit,
    onBlank: () -> Unit,
    onAdd: () -> Unit,
    editable: Boolean = true,
) {
    Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f).padding(start = 4.dp))
            if (editable) {
                IconButton(onClick = onSettings) { Icon(Icons.Filled.Settings, contentDescription = null) }
                IconButton(onClick = onDeleteAll) {
                    Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete_all))
                }
                IconButton(onClick = onBlank) {
                    Icon(painterResource(R.drawable.outline_border_clear_black_24), contentDescription = stringResource(R.string.space_title))
                }
                IconButton(onClick = onAdd) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add)) }
            }
        }
        if (items.isEmpty()) {
            Text(
                stringResource(R.string.not_used),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp),
            )
        }
        val perRow = columns.coerceAtLeast(1)
        items.withIndex().chunked(perRow).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.Start) {
                row.forEach { (index, id) ->
                    Box(Modifier.weight(1f).padding(2.dp)) {
                        OutlinedCard(
                            onClick = { onItem(index) },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(72.dp),
                        ) { ItemCell(id, inMenu) }
                    }
                }
                repeat(perRow - row.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
internal fun ItemCell(id: Int, inMenu: Boolean) {
    Column(Modifier.fillMaxWidth().padding(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        ItemIcon(id, inMenu)
        Text(
            itemLabel(id, inMenu),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun ItemLabel(id: Int, inMenu: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ItemIcon(id, inMenu)
        Text(itemLabel(id, inMenu), modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
internal fun ItemIcon(id: Int, inMenu: Boolean) {
    val color = MainUtil.barTint(0, false)
    when (id) {
        BLANK -> Box(Modifier.size(28.dp))
        1 -> Icon(painterResource(R.drawable.outline_search_black_24), contentDescription = null, modifier = Modifier.size(28.dp))
        else -> {
            val res = when {
                id == 2 -> MainUtil.moreIconFor(color, false)
                inMenu -> MainUtil.menuIconDarkAt(id, color)
                else -> MainUtil.iconResFor(id, color)
            }
            Icon(painterResource(res), contentDescription = null, tint = androidx.compose.ui.graphics.Color.Unspecified, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
internal fun itemLabel(id: Int, inMenu: Boolean): String = when (id) {
    BLANK -> stringResource(R.string.space_title)
    1 -> stringResource(R.string.address_bar)
    else -> stringResource(if (inMenu) MainUtil.menuIconAt(id) else MainUtil.menuLabelAt(id))
}
