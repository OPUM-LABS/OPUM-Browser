package com.mycompany.app.ui.settings

import com.mycompany.app.ui.components.AppSlider
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSaveConfirm
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.MainSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.browser.ActionCatalog
import com.mycompany.app.ui.browser.BarGroups
import com.mycompany.app.ui.browser.LocalGroupMembers
import com.mycompany.app.ui.components.SettingsTopBar
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.view.MyBarFrame

// the classic layout (also the defaults) and the single bottom bar; shared with the setup wizard
internal const val DEFAULT_TOP = "63,1,31"
internal const val DEFAULT_BOT = "26,27,28,29,30,2"
internal const val DEFAULT_LONG = "3,3,44,45,70,65"
internal const val DEFAULT_SWIPE = "0,0,0,0,0,0"
internal const val SINGLE_BOT = "26,27,1,30,2"
internal const val SINGLE_LONG = "3,3,51,70,65"
internal const val SINGLE_SWIPE = "0,0,0,0,0"
private const val ADDRESS_BAR = 1
private const val MENU_BUTTON = 2
private const val TAB_LIST = 30
private const val RELOAD = 35
private const val TOP = "top"
private const val BOT = "bot"
/** Drag id of a whole bar (dragged by holding the bar itself). */
private const val BAR_DRAG = -10

/**
 * Layout of the browser's bars as a picture of the real screen (reworked 2026-11-19): a preview of the phone with the top
 * bar and the bottom bar where they really are. Hold a button and drag it to another place, onto the other bar, or down onto
 * the action list to remove it; drag new actions up from the list. Hold the top bar to drag the whole bar to the top or the
 * bottom. Tap a button for its hold / swipe-up actions, change, move or remove; tap a bar for its height (and the bottom
 * bar's transparency, the top bar's position) right under the preview. Ready-made layouts are in the ⋮ menu. The menu button
 * always stays on a bar. Everything is saved with the check; leaving asks.
 */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
class LayoutSettingsActivity : OpumComposeActivity() {

    /** A ready-made layout: top, bottom, hold and swipe lists, and whether the top bar sits at the bottom. */
    private class Preset(val top: String, val bot: String, val longs: String, val swipes: String, val toBottom: Boolean)

    private val presets = listOf(
        Preset(DEFAULT_TOP, DEFAULT_BOT, DEFAULT_LONG, DEFAULT_SWIPE, false) to R.string.layout_classic,
        Preset(DEFAULT_TOP, DEFAULT_BOT, DEFAULT_LONG, DEFAULT_SWIPE, true) to R.string.layout_bottom,
        Preset("", SINGLE_BOT, SINGLE_LONG, SINGLE_SWIPE, false) to R.string.layout_single,
    )

    private fun pct(px: Int) = Math.round(px * 100f / MainApp.dp48).coerceIn(50, 200)
    private fun px(pct: Int): Int = Math.round(pct * MainApp.dp48 / 100f).coerceIn(Math.round(MainApp.dp48 / 2f), MainApp.dp48 * 2)

    @Composable
    override fun Content() {
        val context: Context = this
        fun ints(labelText: String?) = MainUtil.parseIntList(labelText)?.toList() ?: emptyList()
        // a reload button next to the address bar is not shown (the pill has its own), so it is left out here too
        val origTop = remember { ints(MainSettings.topItems.value).let { if (ADDRESS_BAR in it) it - RELOAD else it } }
        val loadedBot = remember { ints(MainSettings.botItems.value) }
        // hold / swipe lists padded to the bottom bar's length (older settings may hold shorter ones)
        val loadedLong = remember { ints(MainSettings.botLongs.value).let { it + List((loadedBot.size - it.size).coerceAtLeast(0)) { 0 } } }
        val loadedSwipe = remember { ints(MainSettings.botSwipe.value).let { it + List((loadedBot.size - it.size).coerceAtLeast(0)) { 0 } } }
        val keepBot = remember { loadedBot.indices.filter { !(ADDRESS_BAR in loadedBot && loadedBot[it] == RELOAD) } }
        val origBot = remember { keepBot.map { loadedBot[it] } }
        val origLong = remember { keepBot.map { loadedLong[it] } }
        val origSwipe = remember { keepBot.map { loadedSwipe[it] } }
        val origSizes = remember { listOf(pct(PdfSettings.topHeight.value), pct(PdfSettings.botHeight.value), EditorSettings.botAlpha.value.coerceIn(0, 90), EditorSettings.topAlpha.value.coerceIn(0, 90)) }
        // button groups on the bars (only those a bar holds)
        val origGroups = remember { BarGroups.parse(MainSettings.barGroups.value).filterKeys { it in origTop || it in origBot } }
        var saved by remember { mutableStateOf(listOf<Any>(origTop, origBot, origLong, origSwipe, PdfSettings.topToBottom.value, origSizes, origGroups)) }
        val groups = remember { androidx.compose.runtime.mutableStateMapOf<Int, List<Int>>().also { it.putAll(origGroups) } }
        val top: SnapshotStateList<Int> = remember { mutableStateListOf<Int>().also { it.addAll(origTop) } }
        val bot: SnapshotStateList<Int> = remember { mutableStateListOf<Int>().also { it.addAll(origBot) } }
        val longs: SnapshotStateList<Int> = remember { mutableStateListOf<Int>().also { it.addAll(origLong) } }
        val swipes: SnapshotStateList<Int> = remember { mutableStateListOf<Int>().also { it.addAll(origSwipe) } }
        var topToBottom by remember { mutableStateOf(PdfSettings.topToBottom.value) }
        var topPct by remember { mutableIntStateOf(origSizes[0]) }
        var botPct by remember { mutableIntStateOf(origSizes[1]) }
        var botAlpha by remember { mutableIntStateOf(origSizes[2]) }
        var topAlpha by remember { mutableIntStateOf(origSizes[3]) }
        var selectedBar by remember { mutableStateOf<String?>(null) }
        var resetting by remember { mutableStateOf(false) }
        var action by remember { mutableStateOf<Pair<String, Int>?>(null) } // (bar, index)
        // what the picker fills: 0 top, 1 bottom, 2 hold, 3 swipe up; index of the button
        var picking by remember { mutableStateOf<Pair<Int, Int>?>(null) }
        val dnd = remember { DndState() }
        fun current(): List<Any> = listOf(top.toList(), bot.toList(), longs.toList(), swipes.toList(), topToBottom, listOf(topPct, botPct, botAlpha, topAlpha), groups.toMap())
        val changed = current() != saved

        fun addBottom(at: Int, id: Int, long: Int, swipe: Int) { bot.add(at, id); longs.add(at, long); swipes.add(at, swipe) }
        fun removeBottom(index: Int): Triple<Int, Int, Int> = Triple(bot.removeAt(index), longs.removeAt(index), swipes.removeAt(index))

        /** Before saving: an empty group leaves its bar, a group of one becomes that button, unused groups are forgotten. */
        fun tidyGroups() {
            for (i in top.indices.reversed()) { val r = BarGroups.resolve(top[i], groups); if (r == null) top.removeAt(i) else if (r != top[i]) top[i] = r }
            for (i in bot.indices.reversed()) { val r = BarGroups.resolve(bot[i], groups); if (r == null) removeBottom(i) else if (r != bot[i]) bot[i] = r }
            groups.keys.filter { it !in top && it !in bot }.forEach { groups.remove(it) }
        }

        fun save() {
            tidyGroups()
            MainSettings.edit(context) {
                set(MainSettings.barGroups, BarGroups.format(groups.toSortedMap()))
                set(MainSettings.topItems, MainUtil.joinIntList(top.toIntArray()) ?: "")
                set(MainSettings.botItems, MainUtil.joinIntList(bot.toIntArray()) ?: "")
                set(MainSettings.botLongs, MainUtil.joinIntList(longs.toIntArray()) ?: "")
                set(MainSettings.botSwipe, MainUtil.joinIntList(swipes.toIntArray()) ?: "")
            }
            PdfSettings.edit(context) {
                set(PdfSettings.topToBottom, topToBottom)
                set(PdfSettings.topHeight, px(topPct))
                set(PdfSettings.botHeight, px(botPct))
            }
            EditorSettings.edit(context) { set(EditorSettings.botAlpha, botAlpha); set(EditorSettings.topAlpha, topAlpha) }
            saved = current()
        }

        fun apply(preset: Preset) {
            top.clear(); top.addAll(ints(preset.top))
            bot.clear(); bot.addAll(ints(preset.bot))
            longs.clear(); longs.addAll(ints(preset.longs))
            swipes.clear(); swipes.addAll(ints(preset.swipes))
            groups.clear()
            topToBottom = preset.toBottom
            topPct = 100; botPct = 100; botAlpha = 0; topAlpha = 0
            selectedBar = null
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

        fun full(bar: String): Boolean {
            if ((if (bar == TOP) top.size else bot.size) < MID_MAX) return false
            MainUtil.showToast(context, R.string.not_space)
            return true
        }

        /** Puts [id] (with its hold / swipe actions when it comes from the bottom bar) into [bar] at [at]; "Group" makes a new, empty group. */
        fun insert(bar: String, at: Int, itemId: Int, long: Int = if (itemId == ADDRESS_BAR) 51 else 0, swipe: Int = 0) {
            val id = if (itemId == BarGroups.NEW) BarGroups.nextId(groups.keys + top + bot).also { groups[it] = emptyList() } else itemId
            if (bar == TOP) top.add(at.coerceIn(0, top.size), id) else addBottom(at.coerceIn(0, bot.size), id, long, swipe)
        }

        fun barList(bar: String): SnapshotStateList<Int> = if (bar == TOP) top else bot

        // dropping a button onto another makes a group of the two (or adds it to the group it lands on)
        dnd.mergeable = { item, zone, slot ->
            val list = if (zone == TOP) top else if (zone == BOT) bot else null
            val target = list?.getOrNull(slot)
            when {
                target == null || (item.zone == zone && item.index == slot) -> false
                BarGroups.isGroup(target) -> BarGroups.canJoin(item.id)
                else -> BarGroups.canJoin(target) && (BarGroups.canJoin(item.id) || item.id == BarGroups.NEW)
            }
        }

        /** Drops [item] onto the button at [slot] of [bar] (see [DndState.mergeable]); the dragged button leaves its bar. */
        fun merge(item: DragItem, bar: String, slot: Int) {
            val list = barList(bar)
            val target = list[slot]
            if (BarGroups.isGroup(target)) {
                val members = groups[target].orEmpty()
                if (item.id !in members) {
                    if (members.size >= BarGroups.MAX) { MainUtil.showToast(context, R.string.group_full); return }
                    groups[target] = members + item.id
                }
            } else {
                val gid = BarGroups.nextId(groups.keys + top + bot)
                groups[gid] = if (item.id == BarGroups.NEW) listOf(target) else listOf(target, item.id)
                list[slot] = gid
            }
            // the slot was changed in place, so the dragged button's index still holds
            if (item.zone == TOP) top.removeAt(item.index) else if (item.zone == BOT) removeBottom(item.index)
        }

        dnd.onDrop = drop@{ item, zone, index ->
            if (item.id == BAR_DRAG) {
                // the top bar dropped above or below the middle of the phone; the finger often ends on a bar (the bottom
                // bar when dragging down), so the zone under it does not matter, only its height
                val middle = dnd.zoneRect("upper")?.bottom
                val phone = (dnd.zoneRect("upper")?.top ?: 0f)..(dnd.zoneRect("lower")?.bottom ?: Float.MAX_VALUE)
                if (middle != null && dnd.pointer.y in phone) topToBottom = dnd.pointer.y > middle
                return@drop
            }
            val target = if (zone == TOP || zone == BOT) zone else null
            if (target != null) dnd.mergeSlot(item, target)?.let { merge(item, target, it); return@drop }
            when {
                item.zone == TRAY && target != null -> {
                    if (item.id == ADDRESS_BAR && (ADDRESS_BAR in top || ADDRESS_BAR in bot)) return@drop
                    if (!full(target)) insert(target, index, item.id)
                }
                item.zone != TRAY && zone == TRAY -> {
                    if (item.id == MENU_BUTTON) { MainUtil.showToast(context, R.string.menu_must_stay); return@drop }
                    if (item.zone == TOP) top.removeAt(item.index) else removeBottom(item.index)
                }
                item.zone != TRAY && target != null -> {
                    if (item.zone == target) {
                        val to = if (index > item.index) index - 1 else index
                        if (to == item.index) return@drop
                        if (target == TOP) top.add(to, top.removeAt(item.index))
                        else { val (a, b, c) = removeBottom(item.index); addBottom(to, a, b, c) }
                    } else {
                        if (full(target)) return@drop
                        if (item.zone == TOP) { val id = top.removeAt(item.index); insert(BOT, index, id) }
                        else { val (a, b, c) = removeBottom(item.index); insert(TOP, index, a, b, c) }
                    }
                }
            }
        }

        // a reload button that ends up in the same bar as the address bar is dropped (the pill has one)
        androidx.compose.runtime.LaunchedEffect(top.toList(), bot.toList()) {
            if (ADDRESS_BAR in top) top.remove(RELOAD)
            if (ADDRESS_BAR in bot) bot.indexOf(RELOAD).takeIf { it >= 0 }?.let { removeBottom(it) }
        }

        BackHandler { leave() }
        val (rootMod, rootOrigin) = rememberRootOrigin()

        androidx.compose.runtime.CompositionLocalProvider(LocalGroupMembers provides { gid -> groups[gid].orEmpty() }) {
        Scaffold(
            topBar = {
                SettingsTopBar(stringResource(R.string.layout_title), { leave() }, actions = {
                    // resets the whole page (bars, hold / swipe actions, position, sizes) after asking
                    IconButton(onClick = { resetting = true }) { Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.reset)) }
                    if (changed) IconButton(onClick = { save(); finish() }) { Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.ok)) }
                })
            },
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize().then(rootMod)) {
                Column(Modifier.fillMaxSize()) {
                    PhonePreview(
                        dnd, top, bot, topToBottom, topPct, botPct, botAlpha, topAlpha, selectedBar,
                        onSelectBar = { selectedBar = if (selectedBar == it) null else it },
                        onTapItem = { bar, index -> action = bar to index },
                    )
                    // single bar: on = the single-bar layout (one bottom bar with the address bar), off = the classic layout
                    Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.layout_single), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        androidx.compose.material3.Switch(
                            checked = top.isEmpty(),
                            onCheckedChange = { on -> apply(if (on) presets[2].first else presets[0].first) },
                        )
                    }
                    // which bar's size panel is open (a bar can also be picked by tapping its empty space)
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // bottom bar first, so its chip stays put when the top bar (and its chip) goes away
                        for ((bar, label) in listOf(BOT to R.string.bot_bar, TOP to R.string.top_bar)) {
                            if (bar == TOP && top.isEmpty()) continue
                            androidx.compose.material3.FilterChip(
                                selected = selectedBar == bar,
                                onClick = { selectedBar = if (selectedBar == bar) null else bar },
                                label = { Text(stringResource(label) + " · " + (if (bar == TOP) topPct else botPct) + "%") },
                            )
                        }
                    }
                    val sel = selectedBar
                    if (sel == null) {
                        Text(
                            stringResource(R.string.dnd_hint_layout) + " " + stringResource(R.string.group_hint), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                        )
                    } else {
                        // each bar keeps its own height and transparency wherever it sits
                        BarPanel(
                            title = stringResource(if (sel == TOP) R.string.top_bar else R.string.bot_bar),
                            height = if (sel == TOP) topPct else botPct,
                            onHeight = { if (sel == TOP) topPct = it else botPct = it },
                            alpha = if (sel == TOP) topAlpha else botAlpha,
                            onAlpha = { if (sel == TOP) topAlpha = it else botAlpha = it },
                        )
                    }
                    HorizontalDivider()
                    val barItems = (intArrayOf(ADDRESS_BAR).filter { it !in top && it !in bot } + BarGroups.NEW).toIntArray()
                    ActionTray(
                        dnd, Modifier.fillMaxWidth().weight(1f), used = (top + bot + groups.values.flatten()).toSet(),
                        onTap = { id ->
                            val bar = selectedBar ?: BOT
                            if (id == ADDRESS_BAR && (ADDRESS_BAR in top || ADDRESS_BAR in bot)) return@ActionTray
                            if (!full(bar)) insert(bar, if (bar == TOP) top.size else bot.size, id)
                        },
                        extraGroup = R.string.bar_items to barItems,
                        removeHint = { item ->
                            when (item.id) {
                                BAR_DRAG -> null
                                MENU_BUTTON -> context.getString(R.string.menu_must_stay)
                                else -> context.getString(R.string.drop_remove)
                            }
                        },
                    )
                }
                DragGhost(dnd, rootOrigin) { item -> if (item.id == BAR_DRAG) context.getString(R.string.top_bar) else editorLabelPlain(context, item.id) }
            }
        }

        if (resetting) {
            com.mycompany.app.ui.components.ConfirmDialog(
                title = stringResource(R.string.reset),
                message = stringResource(R.string.reset_setting),
                onConfirm = { apply(presets[0].first); save() },
                onDismiss = { resetting = false },
            )
        }
        action?.let { (bar, index) ->
            val list: SnapshotStateList<Int> = if (bar == TOP) top else bot
            if (index >= list.size) { action = null; return@let }
            val id = list[index]
            com.mycompany.app.ui.components.OpumSheetDialog(
                onDismissRequest = { action = null },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        EditorIcon(id, 28.dp)
                        Text(editorLabel(id), modifier = Modifier.padding(start = 12.dp))
                    }
                },
                text = {
                    Column {
                        if (BarGroups.isGroup(id)) GroupMembers(
                            members = groups[id].orEmpty(),
                            onChange = { groups[id] = it },
                            onAdd = { action = null; picking = (if (bar == TOP) 4 else 5) to index },
                            onUngroup = {
                                // the buttons take the group's place, as many as the bar has room for
                                val members = groups.remove(id).orEmpty()
                                val room = MID_MAX - list.size
                                if (members.isEmpty()) { if (bar == TOP) top.removeAt(index) else removeBottom(index) }
                                else {
                                    list[index] = members[0]
                                    members.drop(1).take(room).forEachIndexed { i, m -> insert(bar, index + 1 + i, m) }
                                    if (members.size - 1 > room) MainUtil.showToast(context, R.string.not_space)
                                }
                                action = null
                            },
                        )
                        if (bar == BOT) {
                            GestureLine(stringResource(R.string.gesture_hold), longs.getOrElse(index) { 0 }) { action = null; picking = 2 to index }
                            GestureLine(stringResource(R.string.gesture_swipe), swipes.getOrElse(index) { 0 }) { action = null; picking = 3 to index }
                            HorizontalDivider(Modifier.padding(vertical = 6.dp))
                        }
                        if (id != ADDRESS_BAR && id != MENU_BUTTON && !BarGroups.isGroup(id)) ActionRow(stringResource(R.string.do_change)) { action = null; picking = (if (bar == TOP) 0 else 1) to index }
                        ActionRow(stringResource(if (bar == TOP) R.string.move_to_bottom_bar else R.string.move_to_top_bar)) {
                            val other = if (bar == TOP) BOT else TOP
                            if (!full(other)) {
                                if (bar == TOP) insert(BOT, bot.size, top.removeAt(index)) else { val (a, b, c) = removeBottom(index); insert(TOP, top.size, a, b, c) }
                            }
                            action = null
                        }
                        if (id != MENU_BUTTON) ActionRow(stringResource(R.string.remove_item)) {
                            if (bar == TOP) top.removeAt(index) else removeBottom(index)
                            action = null
                        }
                    }
                },
                confirmButton = {},
                dismissButton = { TextButton(onClick = { action = null }) { Text(stringResource(R.string.cancel)) } },
            )
        }
        picking?.let { (kind, index) ->
            com.mycompany.app.ui.components.OpumSheetBox(onDismiss = { picking = null }) {
                androidx.compose.foundation.layout.Box {
                    Column(Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                        Text(
                            stringResource(when (kind) { 2 -> R.string.gesture_hold; 3 -> R.string.gesture_swipe; 4, 5 -> R.string.group_add; else -> R.string.do_change }),
                            style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 20.dp),
                        )
                        ActionCatalog(
                            onPick = { id ->
                                when (kind) {
                                    0 -> if (index < top.size) top[index] = id
                                    1 -> if (index < bot.size) bot[index] = id
                                    2 -> if (index < longs.size) longs[index] = id
                                    3 -> if (index < swipes.size) swipes[index] = id
                                    4, 5 -> {
                                        val gid = (if (kind == 4) top else bot).getOrNull(index)
                                        val members = gid?.let { groups[it] }
                                        when {
                                            gid == null || members == null || id in members -> {}
                                            !BarGroups.canJoin(id) -> MainUtil.showToast(context, R.string.group_cannot_join)
                                            members.size >= BarGroups.MAX -> MainUtil.showToast(context, R.string.group_full)
                                            else -> groups[gid] = members + id
                                        }
                                    }
                                }
                                picking = null
                            },
                            chosen = if (kind == 2 || kind == 3) emptySet() else (top + bot + groups.values.flatten()).toSet(), maxHeight = 460.dp,
                            noneLabel = if (kind == 2 || kind == 3) R.string.gesture_none else null,
                        )
                        TextButton(onClick = { picking = null }, modifier = Modifier.align(Alignment.End).padding(end = 12.dp)) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                }
            }
        }
        }
    }

    /**
     * The phone: status strip, the page, and the bars where they really are (both at the bottom when the top bar is moved
     * down), scaled to the preview's width. The upper and lower halves are drop zones for dragging the top bar.
     */
    @Composable
    private fun PhonePreview(
        dnd: DndState, top: List<Int>, bot: List<Int>, topToBottom: Boolean, topPct: Int, botPct: Int, botAlpha: Int, topAlpha: Int, selected: String?,
        onSelectBar: (String) -> Unit, onTapItem: (String, Int) -> Unit,
    ) {
        val dark = AppState.darkUi
        val screenW = LocalConfiguration.current.screenWidthDp.toFloat()
        BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {
            val scale = (maxWidth.value / screenW).coerceIn(0.5f, 1f)
            val barH: (Int) -> Dp = { pct -> (48f * pct / 100f * scale).dp }
            val page = if (dark) Color(0xFF101114) else Color.White
            val strip = if (dark) MyBarFrameColors.dark else MyBarFrameColors.light
            Surface(
                shape = RoundedCornerShape(22.dp), color = page,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth().height(250.dp),
            ) {
                Box {
                    // halves of the phone, for dropping the top bar (below the bars, so the bars are the smaller zones)
                    Column(Modifier.fillMaxSize()) {
                        Box(Modifier.weight(1f).fillMaxWidth().dropZone(dnd, "upper"))
                        Box(Modifier.weight(1f).fillMaxWidth().dropZone(dnd, "lower"))
                    }
                    Column(Modifier.fillMaxSize()) {
                        Row(Modifier.fillMaxWidth().height(14.dp).background(strip).padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("12:00", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (!topToBottom) {
                            TopOrHint(dnd, top, barH(topPct), topAlpha, selected == TOP, true, onSelectBar, onTapItem)
                            if (top.isNotEmpty()) BarGrip(dnd, below = true)
                        }
                        PagePlaceholder(Modifier.weight(1f).fillMaxWidth())
                        if (topToBottom) {
                            if (top.isNotEmpty()) BarGrip(dnd, below = false)
                            TopOrHint(dnd, top, barH(topPct), topAlpha, selected == TOP, false, onSelectBar, onTapItem)
                        }
                        if (bot.isEmpty()) {
                            EmptyBarZone(dnd, BOT, stringResource(R.string.drop_add_bar), Modifier.fillMaxWidth().height(32.dp).padding(4.dp))
                        } else {
                            PreviewBar(dnd, BOT, bot, barH(botPct), alpha = botAlpha, selected = selected == BOT, pageEdgeTop = true, onSelectBar, onTapItem)
                        }
                        Box(Modifier.fillMaxWidth().height(10.dp).background(strip))
                    }
                }
            }
        }
    }

    @Composable
    private fun TopOrHint(dnd: DndState, top: List<Int>, h: Dp, alpha: Int, selected: Boolean, atTop: Boolean, onSelectBar: (String) -> Unit, onTapItem: (String, Int) -> Unit) {
        if (top.isEmpty()) {
            EmptyBarZone(dnd, TOP, stringResource(R.string.drop_add_bar), Modifier.fillMaxWidth().height(32.dp).padding(4.dp))
        } else {
            PreviewBar(dnd, TOP, top, h, alpha = alpha, selected = selected, pageEdgeTop = !atTop, onSelectBar, onTapItem)
        }
    }

    /**
     * A small tab hanging from the top bar on the page side: hold it and drag the whole top bar to the top or the bottom
     * of the phone (the bar itself is full of buttons, which are dragged one by one).
     */
    @Composable
    private fun BarGrip(dnd: DndState, below: Boolean) {
        val coords = remember { arrayOfNulls<LayoutCoordinates>(1) }
        val haptic = LocalHapticFeedback.current
        val dark = AppState.darkUi
        Box(Modifier.fillMaxWidth().height(18.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.width(88.dp).fillMaxHeight()
                    .background(if (dark) MyBarFrameColors.dark else MyBarFrameColors.light, if (below) RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp) else RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .onGloballyPositioned { coords[0] = it }
                    // the grip moves the bar as soon as it is dragged (no long press first)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { o ->
                                val c = coords[0] ?: return@detectDragGestures
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                dnd.start(DragItem(BAR_DRAG, TOP, -1), c.localToRoot(o))
                            },
                            onDragEnd = { dnd.end() }, onDragCancel = { dnd.cancel() },
                        ) { change, amount -> change.consume(); dnd.move(amount) }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(2) { Box(Modifier.width(22.dp).height(2.dp).background(MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(1.dp))) }
                }
            }
        }
    }

    /** Faint lines standing in for a web page. */
    @Composable
    private fun PagePlaceholder(modifier: Modifier) {
        val line = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        Column(modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Box(Modifier.fillMaxWidth(0.55f).height(10.dp).background(line, RoundedCornerShape(5.dp)))
            for (w in floatArrayOf(1f, 0.92f, 0.97f, 0.6f, 1f, 0.85f)) Box(Modifier.fillMaxWidth(w).height(6.dp).background(line, RoundedCornerShape(3.dp)))
        }
    }

    /**
     * One bar drawn like the browser draws it: the bar tone, a hairline toward the page, buttons in equal slots (the address
     * pill takes the free width). Tap the bar to select it, hold the top bar to drag it; buttons are drag sources.
     */
    @Composable
    private fun PreviewBar(
        dnd: DndState, zone: String, ids: List<Int>, h: Dp, alpha: Int, selected: Boolean, pageEdgeTop: Boolean,
        onSelectBar: (String) -> Unit, onTapItem: (String, Int) -> Unit,
    ) {
        val dark = AppState.darkUi
        val base = if (dark) MyBarFrameColors.dark else MyBarFrameColors.light
        val tone = base.copy(alpha = 1f - alpha / 100f)
        val select by rememberUpdatedState(onSelectBar)
        val hasAddress = ADDRESS_BAR in ids
        dnd.trim(zone, ids.size)
        Box(
            Modifier.fillMaxWidth().height(h).background(tone)
                .then(if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary) else Modifier)
                .dropZone(dnd, zone).dropMarker(dnd, zone)
                .pointerInput(zone) { detectTapGestures(onTap = { select(zone) }) },
        ) {
            Box(Modifier.fillMaxWidth().height(1.dp).align(if (pageEdgeTop) Alignment.TopCenter else Alignment.BottomCenter).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)))
            Row(Modifier.fillMaxSize().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                ids.forEachIndexed { index, id ->
                    val slot = when {
                        id == ADDRESS_BAR -> Modifier.weight(1f)
                        hasAddress -> Modifier.width(h)
                        else -> Modifier.weight(1f)
                    }
                    Box(
                        slot.fillMaxHeight().dropSlot(dnd, zone, index).dragSource(dnd, DragItem(id, zone, index)) { onTapItem(zone, index) },
                        contentAlignment = Alignment.Center,
                    ) { LayoutBarButton(id, h, dark) }
                }
            }
        }
    }

    /**
     * A group's buttons in its sheet: the first one stands for the group on the bar; move a button up (to the top makes
     * it the lead), take it out, add more (up to [BarGroups.MAX]) or ungroup.
     */
    @Composable
    private fun GroupMembers(members: List<Int>, onChange: (List<Int>) -> Unit, onAdd: () -> Unit, onUngroup: () -> Unit) {
        Column {
            Text(stringResource(R.string.group_lead), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            members.forEachIndexed { i, m ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) { EditorIcon(m, 22.dp) }
                    Text(
                        editorLabel(m), style = if (i == 0) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyLarge,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(start = 10.dp),
                    )
                    if (i > 0) IconButton(onClick = { onChange(members.toMutableList().also { l -> l.add(i - 1, l.removeAt(i)) }) }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = stringResource(R.string.group_move_up))
                    }
                    IconButton(onClick = { onChange(members - m) }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.remove_item))
                    }
                }
            }
            if (members.size < BarGroups.MAX) ActionRow(stringResource(R.string.group_add), onAdd)
            ActionRow(stringResource(R.string.group_ungroup), onUngroup)
            HorizontalDivider(Modifier.padding(vertical = 6.dp))
        }
    }

    /** Height and transparency of the selected bar, under the preview (the position is set by dragging the bar's grip). */
    @Composable
    private fun BarPanel(title: String, height: Int, onHeight: (Int) -> Unit, alpha: Int, onAlpha: (Int) -> Unit) {
        Surface(
            shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        ) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                PanelSlider(stringResource(R.string.size_height), height, 50, 200, 100, onHeight)
                PanelSlider(stringResource(R.string.color_alpha), alpha, 0, 90, 0, onAlpha)
            }
        }
    }

    @Composable
    private fun PanelSlider(label: String, value: Int, min: Int, max: Int, default: Int, onChange: (Int) -> Unit) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(96.dp))
            AppSlider(value = value.toFloat(), valueRange = min.toFloat()..max.toFloat(), onValueChange = { onChange(Math.round(it)) }, modifier = Modifier.weight(1f))
            Text("$value%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(44.dp).padding(start = 6.dp))
            // back to the default (100 % height, 0 % transparency); greyed out while already there
            IconButton(onClick = { onChange(default) }, enabled = value != default, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.reset), modifier = Modifier.size(20.dp))
            }
        }
    }

    @Composable
    private fun GestureLine(label: String, id: Int, onClick: () -> Unit) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(96.dp))
            if (id != 0) EditorIcon(id, 20.dp) else Spacer(Modifier.size(20.dp))
            Text(
                if (id == 0) stringResource(R.string.gesture_none) else editorLabel(id), style = MaterialTheme.typography.bodyLarge,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 10.dp),
            )
        }
    }
}

/** The bar tones of [MyBarFrame] as Compose colours. */
internal object MyBarFrameColors {
    val light = Color(MyBarFrame.BAR_LIGHT)
    val dark = Color(MyBarFrame.BAR_DARK)
}

/** An item's label from a plain context (for the drag ghost). */
internal fun editorLabelPlain(context: Context, id: Int): String = when (id) {
    BLANK -> context.getString(R.string.space_title)
    1 -> context.getString(R.string.address_bar)
    2 -> context.getString(R.string.list_menu)
    else -> com.mycompany.app.ui.browser.MenuCatalog.label(context, id)
}

/** A button as the bar shows it: the address pill, the stacked tabs box, or the action's icon. */
@Composable
internal fun LayoutBarButton(id: Int, h: Dp, dark: Boolean, tabLabel: String = "3") {
    val icon = (h.value * 0.46f).coerceIn(14f, 26f).dp
    when (id) {
        BLANK -> Box(Modifier.size(icon))
        ADDRESS_BAR -> Row(
            Modifier.fillMaxWidth().padding(horizontal = 2.dp).height((h.value * 0.58f).dp)
                .background(if (dark) Color(0xFF2E3036) else Color.White, RoundedCornerShape(8.dp))
                .border(1.dp, Color(0x1A000000), RoundedCornerShape(8.dp)).padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(painterResource(R.drawable.outline_search_black_24), null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(icon * 0.7f))
            // no font padding and one line height, so the hint sits in the middle of the small pill
            val hintSize = (h.value * 0.2f).coerceIn(7f, 11f)
            Text(
                stringResource(R.string.web_edit_hint), fontSize = hintSize.sp, lineHeight = hintSize.sp, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = androidx.compose.ui.text.TextStyle(
                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false),
                    lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
                        androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center, androidx.compose.ui.text.style.LineHeightStyle.Trim.Both,
                    ),
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp).weight(1f),
            )
            // the reload button at the pill's end, as in the browser
            androidx.compose.foundation.Image(
                painterResource(MainUtil.iconResFor(RELOAD, MainUtil.barTint(0, false))), null,
                modifier = Modifier.size(icon * 0.75f), alpha = 0.7f,
            )
        }
        TAB_LIST -> Box(Modifier.size(icon)) {
            Box(Modifier.size(icon * 0.72f).align(Alignment.TopEnd).border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), RoundedCornerShape(3.dp)))
            Box(
                Modifier.size(icon * 0.72f).align(Alignment.BottomStart).background(if (dark) MyBarFrameColors.dark else MyBarFrameColors.light, RoundedCornerShape(3.dp))
                    .border(1.5.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(3.dp)),
                contentAlignment = Alignment.Center,
            ) {
                // the count may be wider than the little square ("2/3"), so it may reach past its edges
                val textSize = (icon.value * (if (tabLabel.length > 2) 0.34f else 0.42f)).coerceAtLeast(6f)
                Text(
                    tabLabel, fontSize = textSize.sp, lineHeight = textSize.sp, maxLines = 1, softWrap = false,
                    style = androidx.compose.ui.text.TextStyle(platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false)),
                    modifier = Modifier.wrapContentSize(unbounded = true), color = MaterialTheme.colorScheme.onSurface) }
        }
        else -> EditorIcon(id, icon)
    }
}

/**
 * A bar as the Layout editor draws it, without the editing: [ids] in a row (the address pill takes the room left),
 * [alpha] % see-through, a thin line on the page side ([pageEdgeTop]). Also the bars of the full screen preview.
 */
@Composable
internal fun LayoutBarPicture(ids: List<Int>, h: Dp, alpha: Int, pageEdgeTop: Boolean, modifier: Modifier = Modifier, tabLabel: String = "3") {
    val dark = AppState.darkUi
    val base = if (dark) MyBarFrameColors.dark else MyBarFrameColors.light
    val hasAddress = ADDRESS_BAR in ids
    Box(modifier.fillMaxWidth().height(h).background(base.copy(alpha = 1f - alpha / 100f))) {
        Box(Modifier.fillMaxWidth().height(1.dp).align(if (pageEdgeTop) Alignment.TopCenter else Alignment.BottomCenter).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)))
        Row(Modifier.fillMaxSize().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            for (id in ids) {
                val slot = if (id != ADDRESS_BAR && hasAddress) Modifier.width(h) else Modifier.weight(1f)
                Box(slot.fillMaxHeight(), contentAlignment = Alignment.Center) { LayoutBarButton(id, h, dark, tabLabel) }
            }
        }
    }
}
