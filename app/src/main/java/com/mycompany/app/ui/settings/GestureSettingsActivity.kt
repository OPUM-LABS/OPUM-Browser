package com.mycompany.app.ui.settings

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.MainSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.Setting
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.browser.MenuActionIcon
import com.mycompany.app.ui.browser.MenuCatalog
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.EXTRA_FOCUS
import com.mycompany.app.ui.components.OpumSheetBox
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingSliderRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import kotlin.math.roundToInt
import com.mycompany.app.ui.components.TickSwitch as Switch

/**
 * Settings > Gestures (one page since 2026-11-27; were Gestures, Swipe and Swipe sensitivity): the phone preview of the
 * other pages with the gestures drawn on it, and a switch between the three kinds:
 * - **Edges**: the five swipe zones (top, bottom, left, right edge, centre) in the colour of what a sideways swipe
 *   there does; tap one for its action, its size (portrait and landscape) and its sensitivity;
 * - **Diagonal**: up or down, then sideways (four paths, each with any action);
 * - **Two fingers**: left, right, down and up, each with the tab gesture (left / right move the tab, down removes it),
 *   any action, or nothing.
 * A switched-off gesture keeps its action for when it is switched on again. Settings are written at once.
 */
class GestureSettingsActivity : OpumComposeActivity() {

    /** A gesture with an action: the setting, the action it had while switched off, its name and icon. */
    private class Gesture(val setting: Setting<Int>, val last: Setting<Int>, val name1: Int, val name2: Int, val iconRes: Int)

    private val diagonals = listOf(
        Gesture(ZoneSettings.gesUpLeft, ZoneSettings.gesUpLeftLast, R.string.move_up, R.string.move_left, R.drawable.outline_subdirectory_arrow_up_left_black_24),
        Gesture(ZoneSettings.gesUpRight, ZoneSettings.gesUpRightLast, R.string.move_up, R.string.move_right, R.drawable.outline_subdirectory_arrow_up_right_black_24),
        Gesture(ZoneSettings.gesDownLeft, ZoneSettings.gesDownLeftLast, R.string.move_down, R.string.move_left, R.drawable.outline_subdirectory_arrow_left_black_24),
        Gesture(ZoneSettings.gesDownRight, ZoneSettings.gesDownRightLast, R.string.move_down, R.string.move_right, R.drawable.outline_subdirectory_arrow_right_black_24),
    )

    /** Two-finger swipes, in the order left, right, down, up. */
    private val twos = listOf(
        Gesture(ZoneSettings.twoLeft, ZoneSettings.twoLeftLast, R.string.move_left, 0, R.drawable.outline_keyboard_arrow_left_black_24),
        Gesture(ZoneSettings.twoRight, ZoneSettings.twoRightLast, R.string.move_right, 0, R.drawable.outline_keyboard_arrow_right_black_24),
        Gesture(ZoneSettings.twoDown, ZoneSettings.twoDownLast, R.string.move_down, 0, R.drawable.outline_keyboard_arrow_down_black_24),
        Gesture(ZoneSettings.twoUp, ZoneSettings.twoUpLast, R.string.move_up, 0, R.drawable.outline_keyboard_arrow_up_black_24),
    )

    private val zoneActions = listOf(ZoneSettings.gesTop, ZoneSettings.gesBot, ZoneSettings.gesLeft, ZoneSettings.gesRight, ZoneSettings.gesCenter)
    private val portSizes = listOf(ZoneSettings.portAreaTop, ZoneSettings.portAreaBot, ZoneSettings.portAreaLeft, ZoneSettings.portAreaRight)
    private val landSizes = listOf(ZoneSettings.landAreaTop, ZoneSettings.landAreaBot, ZoneSettings.landAreaLeft, ZoneSettings.landAreaRight)
    private val senses = listOf(ZtwoSettings.senseTop, ZtwoSettings.senseBot, ZtwoSettings.senseLeft, ZtwoSettings.senseRight, ZtwoSettings.senseCenter)
    private val zoneNames = listOf(R.string.zone_top, R.string.zone_bottom, R.string.zone_left, R.string.zone_right, R.string.show_center)

    /** The kind shown: 0 edges, 1 diagonal, 2 two fingers. */
    private var kind by mutableIntStateOf(0)

    /** Bumped after every write, so the page reads the settings again. */
    private var tick by mutableIntStateOf(0)

    private fun <T> write(setting: Setting<T>, value: T) {
        ZoneSettings.edit(this) { set(setting, value) }
        tick++
    }

    // ---- gestures with an action (diagonal, two fingers) ----

    /** On: the action runs. A two-finger swipe also needs the two-finger switch (`BrowserSettings.tabMulti`). */
    private fun isOn(g: Gesture): Boolean = g.setting.value != 0 && (g.setting !in twoSettings() || BrowserSettings.tabMulti.value)

    /** The action shown: the current one, or the one kept while off. */
    private fun kept(g: Gesture): Int = if (isOn(g)) g.setting.value else g.last.value

    private fun twoSettings() = twos.map { it.setting }

    private fun switchOn(g: Gesture, action: Int) {
        if (g.setting in twoSettings() && !BrowserSettings.tabMulti.value) {
            // the two-finger swipes were off as a whole: only this one comes on, the others keep their actions
            for (other in twos) if (other !== g && other.setting.value != 0) { write(other.last, other.setting.value); write(other.setting, 0) }
            BrowserSettings.edit(this) { set(BrowserSettings.tabMulti, true) }
        }
        write(g.setting, action)
        write(g.last, action)
    }

    private fun switchOff(g: Gesture) {
        if (isOn(g)) write(g.last, g.setting.value)
        write(g.setting, 0)
        if (g.setting in twoSettings() && twos.all { it.setting.value == 0 }) BrowserSettings.edit(this) { set(BrowserSettings.tabMulti, false) }
        tick++
    }

    private fun label(g: Gesture): String =
        if (g.name2 == 0) getString(g.name1) else getString(g.name1) + " + " + getString(g.name2)

    /** The name of [action] of [g]: the tab gesture has one per direction. */
    @Composable
    private fun actionName(g: Gesture, action: Int): String = when {
        action == ZoneSettings.TWO_TAB -> stringResource(if (g.setting === ZoneSettings.twoDown) R.string.two_tab_close else R.string.two_tab_move)
        action != 0 -> stringResource(MenuCatalog.labelRes(action))
        else -> stringResource(R.string.not_used)
    }

    @Composable
    override fun Content() {
        val context: Context = this
        var picking by remember { mutableStateOf<Gesture?>(null) }
        var zone by remember { mutableIntStateOf(-1) }
        var resetting by remember { mutableStateOf(false) }
        val portrait = LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE
        // a search result for the diagonal or two-finger rows opens that kind
        remember {
            when (intent.getStringExtra(EXTRA_FOCUS)) {
                getString(R.string.hd_diagonal) -> kind = 1
                getString(R.string.hd_two_finger), getString(R.string.tab_gesture) -> kind = 2
            }
            0
        }
        @Suppress("UNUSED_VARIABLE") val readAgain = tick

        SettingsScaffold(
            title = stringResource(R.string.gesture),
            onBack = { finish() },
            onReset = { resetting = true },
            above = {
                Column {
                    if (portrait) GesturePreview(onZone = { zone = it }, onGesture = { picking = it })
                    KindSwitch()
                }
            },
        ) {
            when (kind) {
                0 -> {
                    settingsHeader(getString(R.string.ges_edges))
                    for (z in intArrayOf(0, 2, 4, 3, 1)) {
                        item {
                            @Suppress("UNUSED_VARIABLE") val readAgain = tick
                            SettingRow(
                                title = stringResource(zoneNames[z]),
                                summary = stringResource(zoneActionName(z, zoneActions[z].value.coerceIn(0, 2))),
                                onClick = { zone = z },
                                trailing = { Box(Modifier.size(20.dp).background(zoneColor(zoneActions[z].value), RoundedCornerShape(5.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(5.dp))) },
                            )
                        }
                    }
                    // pulling the page down at its top reloads it (2026-11-27; was on Screen and scrolling)
                    item {
                        @Suppress("UNUSED_VARIABLE") val readAgain = tick
                        com.mycompany.app.ui.components.SettingSwitchRow(
                            title = stringResource(R.string.pull_refresh), checked = ZoneSettings.pullRefresh.value,
                            onCheckedChange = { write(ZoneSettings.pullRefresh, it) },
                        )
                    }
                    settingsFooter(getString(R.string.swipe_hint))
                }
                1 -> {
                    settingsHeader(getString(R.string.hd_diagonal))
                    for (g in diagonals) item { GestureRow(g) { picking = g } }
                    settingsFooter(getString(R.string.ges_diagonal_hint))
                }
                else -> {
                    settingsHeader(getString(R.string.hd_two_finger))
                    for (g in twos) item { GestureRow(g) { picking = g } }
                    settingsFooter(getString(R.string.ges_two_hint))
                }
            }
        }

        picking?.let { g -> ActionSheet(g) { picking = null } }
        if (zone >= 0) ZoneSheet(zone) { zone = -1 }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = { resetToDefaults(context); tick++ },
                onDismiss = { resetting = false },
            )
        }
    }

    /** Edges · Diagonal · Two fingers. */
    @Composable
    private fun KindSwitch() {
        val labels = listOf(R.string.ges_kind_edges, R.string.ges_kind_diagonal, R.string.ges_kind_two)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            labels.forEachIndexed { i, label ->
                SegmentedButton(
                    selected = kind == i, onClick = { kind = i },
                    shape = SegmentedButtonDefaults.itemShape(i, labels.size), icon = {},
                    label = { Text(stringResource(label), maxLines = 2, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium) },
                )
            }
        }
    }

    /** A diagonal or two-finger swipe: its action (kept while off) and a switch. */
    @Composable
    private fun GestureRow(g: Gesture, onPick: () -> Unit) {
        @Suppress("UNUSED_VARIABLE") val readAgain = tick
        val on = isOn(g)
        val action = kept(g)
        SettingRow(
            title = label(g),
            summary = actionName(g, action),
            iconRes = g.iconRes,
            onClick = onPick,
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (action > 0) Box(Modifier.alpha(if (on) 1f else 0.38f)) { MenuActionIcon(action, 22.dp) }
                    Switch(
                        checked = on,
                        onCheckedChange = { now -> if (!now) switchOff(g) else if (action != 0) switchOn(g, action) else onPick() },
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
            },
        )
    }

    /** The action list for [g]; two-finger swipes offer their tab gesture first. Picking switches it on. */
    @Composable
    private fun ActionSheet(g: Gesture, onClose: () -> Unit) {
        OpumSheetBox(onDismiss = onClose) {
            Box {
                Column(Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                    Text(label(g), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 20.dp))
                    if (g.setting in twoSettings() && g.setting !== ZoneSettings.twoUp) {
                        val chosen = kept(g) == ZoneSettings.TWO_TAB
                        Row(
                            Modifier.fillMaxWidth().clickable { switchOn(g, ZoneSettings.TWO_TAB); onClose() }.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painterResource(if (g.setting === ZoneSettings.twoDown) R.drawable.outline_tab_close_black_24 else R.drawable.outline_tab_move_black_24),
                                null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp),
                            )
                            Text(actionName(g, ZoneSettings.TWO_TAB), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 18.dp).weight(1f))
                            if (chosen) Icon(com.mycompany.app.ui.components.Sym.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                    }
                    com.mycompany.app.ui.browser.ActionCatalog(
                        onPick = { id -> if (id > 0 && !MenuCatalog.isFamily(id)) switchOn(g, id); onClose() },
                        chosen = setOf(kept(g)), maxHeight = 420.dp, expandFamilies = true,
                    )
                    TextButton(onClick = onClose, modifier = Modifier.align(Alignment.End).padding(end = 12.dp)) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            }
        }
    }

    // ---- edges ----

    /** Stored value 2 is "leave it to Android" on the side edges (its back gesture) and "off" elsewhere. */
    private fun zoneActionName(zone: Int, action: Int): Int = when (action) {
        0 -> R.string.act_page
        1 -> R.string.act_tab
        else -> if (zone == 2 || zone == 3) R.string.act_system else R.string.not_used
    }

    /** Back / forward in the main colour, switch tab in the third colour, nothing (or Android's own) left clear. */
    @Composable
    private fun zoneColor(action: Int): Color = when (action.coerceIn(0, 2)) {
        0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        1 -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
        else -> Color.Transparent
    }

    /** One zone: what a sideways swipe does there, its size (edges, portrait and landscape) and its sensitivity. */
    @Composable
    private fun ZoneSheet(z: Int, onClose: () -> Unit) {
        val metrics = resources.displayMetrics
        val shortSide = minOf(metrics.widthPixels, metrics.heightPixels)
        val longSide = maxOf(metrics.widthPixels, metrics.heightPixels)
        val minCenter = MainApp.dp64 shl 1
        @Suppress("UNUSED_VARIABLE") val readAgain = tick
        // the sizes while a slider is dragged, for the two small screens
        val live = remember(z) { androidx.compose.runtime.mutableStateListOf(*(portSizes + landSizes).map { it.value }.toTypedArray()) }
        OpumSheetBox(onDismiss = onClose) {
            Column(Modifier.padding(top = 16.dp, bottom = 16.dp)) {
                Text(stringResource(zoneNames[z]), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 20.dp))
                SettingSegmentedRow(
                    title = stringResource(R.string.hd_action),
                    options = (0..2).map { stringResource(zoneActionName(z, it)) },
                    selectedIndex = zoneActions[z].value.coerceIn(0, 2),
                    onSelect = { write(zoneActions[z], it) },
                )
                if (z < 4) {
                    val vertical = z < 2
                    ZoneSizePictures(z, live, shortSide, longSide)
                    for (land in listOf(false, true)) {
                        val sizes = if (land) landSizes else portSizes
                        val other = sizes[if (z % 2 == 0) z + 1 else z - 1].value
                        val total = if (vertical) (if (land) shortSide else longSide) else (if (land) longSide else shortSide)
                        val min = if (vertical) MainApp.dp48 else MainApp.dp16
                        // an edge takes at most a third of the screen, so the edges never meet and the middle stays
                        val max = minOf(total / 3, total - other - minCenter).coerceAtLeast(min + 1)
                        val at = (if (land) 4 else 0) + z
                        SettingSliderRow(
                            title = stringResource(if (vertical) R.string.size_height else R.string.zone_width) + " · " + stringResource(if (land) R.string.view_land else R.string.view_port),
                            value = sizes[z].value.coerceIn(min, max), range = min..max,
                            label = { "" + (it / metrics.density).roundToInt() + " dp" },
                            onValueChange = { write(sizes[z], it); live[at] = it },
                            below = { v -> androidx.compose.runtime.LaunchedEffect(v) { live[at] = v } },
                        )
                    }
                }
                SettingSliderRow(
                    title = stringResource(R.string.swipe_sense), value = senses[z].value, range = 10..200, label = { "$it%" },
                    onValueChange = { v ->
                        if (ReadSettings.guideSense.value) ReadSettings.edit(this@GestureSettingsActivity) { set(ReadSettings.guideSense, false) }
                        ZtwoSettings.edit(this@GestureSettingsActivity) { set(senses[z], v) }
                        tick++
                    },
                )
                TextButton(onClick = onClose, modifier = Modifier.align(Alignment.End).padding(end = 12.dp, top = 4.dp)) {
                    Text(stringResource(R.string.ok))
                }
            }
        }
    }

    /**
     * The edge [z] on a small portrait and a small landscape screen, to scale, with the other edges outlined: what the
     * two size sliders change, live ([sizes]: portrait top, bottom, left, right, then landscape).
     */
    @Composable
    private fun ZoneSizePictures(z: Int, sizes: List<Int>, shortSide: Int, longSide: Int) {
        val fill = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        val line = MaterialTheme.colorScheme.outline
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.Bottom) {
            for (land in listOf(false, true)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val w = if (land) 150.dp else 68.dp
                    val h = if (land) 68.dp else 150.dp
                    Canvas(Modifier.size(w, h).border(1.5.dp, line, RoundedCornerShape(8.dp))) {
                        val k = size.width / (if (land) longSide else shortSide)
                        val base = if (land) 4 else 0
                        val top = sizes[base] * k
                        val bot = sizes[base + 1] * k
                        val left = sizes[base + 2] * k
                        val right = sizes[base + 3] * k
                        val rects = listOf(
                            androidx.compose.ui.geometry.Rect(0f, 0f, size.width, top),
                            androidx.compose.ui.geometry.Rect(0f, size.height - bot, size.width, size.height),
                            androidx.compose.ui.geometry.Rect(0f, top, left, size.height - bot),
                            androidx.compose.ui.geometry.Rect(size.width - right, top, size.width, size.height - bot),
                        )
                        rects.forEachIndexed { i, r ->
                            if (i == z) drawRect(fill, r.topLeft, r.size)
                            drawRect(line.copy(alpha = 0.7f), r.topLeft, r.size, style = Stroke(1.dp.toPx()))
                        }
                    }
                    Text(
                        stringResource(if (land) R.string.view_land else R.string.view_port), style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
    }

    // ---- the preview ----

    /**
     * The phone of the other settings pages (bars as set) with the gestures of the chosen kind drawn on the page: the
     * zones to scale, the diagonal paths in the four quarters, or the two-finger arrows from the middle. A tap picks
     * what it hits.
     */
    @Composable
    private fun GesturePreview(onZone: (Int) -> Unit, onGesture: (Gesture) -> Unit) {
        @Suppress("UNUSED_VARIABLE") val readAgain = tick
        val bars = PreviewBars(
            BrowserSettings.showStatus.value, BrowserSettings.showNavi.value, true, true,
            PdfSettings.topToBottom.value,
            MainUtil.parseIntList(MainSettings.topItems.value)?.toList().orEmpty(),
            MainUtil.parseIntList(MainSettings.botItems.value)?.toList().orEmpty(),
            EditorSettings.topAlpha.value.coerceIn(0, 90), EditorSettings.botAlpha.value.coerceIn(0, 90),
            BrowserSettings.tabBar.value, currentPreviewTabs(),
        )
        // browser px to preview px (the preview draws browser dp at PreviewSize.SCALE)
        val k = PreviewSize.SCALE
        val zoneColors = (0..4).map { zoneColor(zoneActions[it].value) }
        PhonePreview(
            bars,
            scrolling = false,
            onTap = { at, places, width ->
                when (kind) {
                    0 -> onZone(zoneAt(at, places, width, k))
                    1 -> {
                        val mid = (places.above + places.below) / 2f
                        onGesture(diagonals[(if (at.y < mid) 0 else 2) + (if (at.x < width / 2f) 0 else 1)])
                    }
                    else -> {
                        val cx = width / 2f
                        val cy = (places.above + places.below) / 2f
                        val dx = at.x - cx
                        val dy = at.y - cy
                        onGesture(twos[if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) (if (dx < 0) 0 else 1) else (if (dy > 0) 2 else 3)])
                    }
                }
            },
            over = { places, _ ->
                // the page dimmed, so the gestures drawn on it stand out
                Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)))
                when (kind) {
                    0 -> ZonesPicture(places, k, zoneColors)
                    1 -> DiagonalPicture(places)
                    else -> TwoFingerPicture(places)
                }
            },
        )
        Text(
            stringResource(R.string.ges_tap_guide), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )
    }

    /** Preview px of a zone edge: to scale, but never thinner than [minPx] so it stays visible. */
    private fun edgePx(sizePx: Int, k: Float, minPx: Float) = maxOf(sizePx * k, minPx)

    private fun zoneAt(at: Offset, places: PreviewPlaces, width: Float, k: Float): Int {
        val d = resources.displayMetrics.density
        val top = places.above + edgePx(ZoneSettings.portAreaTop.value, k, 12 * d)
        val bottom = places.below - edgePx(ZoneSettings.portAreaBot.value, k, 12 * d)
        return when {
            at.y < top -> 0
            at.y > bottom -> 1
            at.x < edgePx(ZoneSettings.portAreaLeft.value, k, 16 * d) -> 2
            at.x > width - edgePx(ZoneSettings.portAreaRight.value, k, 16 * d) -> 3
            else -> 4
        }
    }

    @Composable
    private fun BoxScope.ZonesPicture(places: PreviewPlaces, k: Float, colors: List<Color>) {
        val density = LocalDensity.current
        val d = density.density
        val topH = edgePx(ZoneSettings.portAreaTop.value, k, 12 * d)
        val botH = edgePx(ZoneSettings.portAreaBot.value, k, 12 * d)
        val leftW = edgePx(ZoneSettings.portAreaLeft.value, k, 16 * d)
        val rightW = edgePx(ZoneSettings.portAreaRight.value, k, 16 * d)
        val midTop = places.above + topH
        val midH = places.below - botH - midTop
        fun Float.dp(): Dp = with(density) { toDp() }
        val line = MaterialTheme.colorScheme.outline
        @Composable
        fun zone(z: Int, x: Float, y: Float, w: Float?, h: Float, text: Boolean) {
            Box(
                Modifier.offset { IntOffset(x.roundToInt(), y.roundToInt()) }
                    .then(if (w == null) Modifier.fillMaxWidth() else Modifier.width(w.dp())).height(h.dp())
                    .background(colors[z]).border(0.5.dp, line.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center,
            ) {
                if (text) {
                    Text(
                        stringResource(zoneActionName(z, zoneActions[z].value.coerceIn(0, 2))), style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        zone(0, 0f, places.above, null, topH, true)
        zone(1, 0f, places.below - botH, null, botH, true)
        Row(Modifier.offset { IntOffset(0, midTop.roundToInt()) }.fillMaxWidth().height(midH.dp())) {
            Box(Modifier.width(leftW.dp()).fillMaxSize().background(colors[2]).border(0.5.dp, line.copy(alpha = 0.6f)))
            Box(Modifier.weight(1f).fillMaxSize().background(colors[4]).border(0.5.dp, line.copy(alpha = 0.6f)), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(zoneActionName(4, zoneActions[4].value.coerceIn(0, 2))), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface, maxLines = 2, textAlign = TextAlign.Center,
                )
            }
            Box(Modifier.width(rightW.dp()).fillMaxSize().background(colors[3]).border(0.5.dp, line.copy(alpha = 0.6f)))
        }
        // a finger showing the swipe of each zone that does something: sideways along the top, the bottom and the
        // middle, inwards from the side edges
        val motion = androidx.compose.animation.core.rememberInfiniteTransition(label = "finger")
        val sway by motion.animateFloat(
            0f, 1f, androidx.compose.animation.core.infiniteRepeatable(androidx.compose.animation.core.tween(1300), androidx.compose.animation.core.RepeatMode.Reverse), label = "sway",
        )
        val push by motion.animateFloat(
            0f, 1f, androidx.compose.animation.core.infiniteRepeatable(androidx.compose.animation.core.tween(1300), androidx.compose.animation.core.RepeatMode.Restart), label = "push",
        )
        androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize()) {
            val width = constraints.maxWidth.toFloat()
            fun active(z: Int) = zoneActions[z].value.coerceIn(0, 2) != 2
            if (active(0)) Finger(width * (0.3f + 0.4f * sway), places.above + topH / 2f, 1f)
            if (active(1)) Finger(width * (0.3f + 0.4f * sway), places.below - botH / 2f, 1f)
            if (active(4)) Finger(width * (0.35f + 0.3f * sway), midTop + midH * 0.72f, 1f)
            val fade = if (push > 0.8f) (1f - push) / 0.2f else 1f
            if (active(2)) Finger(leftW / 2f + width * 0.3f * push, midTop + midH * 0.3f, fade)
            if (active(3)) Finger(width - rightW / 2f - width * 0.3f * push, midTop + midH * 0.3f, fade)
            // pull to refresh: a finger pulls the page down from its top, the refresh circle comes with it
            if (ZoneSettings.pullRefresh.value) {
                // from just under the top edge zone, so it does not cover that zone's label
                val start = maxOf(places.pageTop, midTop)
                val pull = start + midH * (0.08f + 0.2f * push)
                val circle = 20.dp
                val circlePx = with(LocalDensity.current) { circle.toPx() }
                val dark = com.mycompany.app.ui.state.AppState.darkUi
                Box(
                    Modifier.size(circle).graphicsLayer {
                        translationX = width / 2f - circlePx / 2f
                        translationY = start + (pull - start) * 0.55f - circlePx / 2f
                        alpha = fade
                    }.background(if (dark) Color(0xFF303030.toInt()) else Color.White, CircleShape).border(1.dp, Color(0x33000000), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(if (dark) R.drawable.outline_refresh_dark_24 else R.drawable.outline_refresh_black_24), null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(circle * 0.7f).graphicsLayer { rotationZ = push * 360f },
                    )
                }
                Finger(width / 2f, pull + circlePx * 0.6f, fade)
            }
        }
    }

    /** A finger whose tip is at [x], [y] (preview px). */
    @Composable
    private fun Finger(x: Float, y: Float, alpha: Float) {
        val side = 22.dp
        val sidePx = with(LocalDensity.current) { side.toPx() }
        Icon(
            painterResource(R.drawable.outline_touch_finger_white_24), null, tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(side).graphicsLayer {
                // the tip of the drawn finger is at 42 % across, 25 % down
                translationX = x - sidePx * 0.42f
                translationY = y - sidePx * 0.25f
                this.alpha = alpha
            },
        )
    }

    /** The four diagonal paths, one per quarter of the page, each ending at its action's icon (dimmed when off). */
    @Composable
    private fun BoxScope.DiagonalPicture(places: PreviewPlaces) {
        val density = LocalDensity.current
        val ink = MaterialTheme.colorScheme.primary
        val off = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        val top = places.pageTop
        val height = places.pageBottom - places.pageTop
        Box(Modifier.offset { IntOffset(0, top.roundToInt()) }.fillMaxWidth().height(with(density) { height.toDp() })) {
            Canvas(Modifier.fillMaxSize()) {
                diagonals.forEachIndexed { i, g ->
                    val up = i < 2
                    val left = i % 2 == 0
                    // the quarter's middle; the path goes 22 % of the height up or down, then sideways
                    val qx = size.width * (if (left) 0.25f else 0.75f)
                    val qy = size.height * (if (up) 0.25f else 0.75f)
                    val run = size.height * 0.16f
                    val side = size.width * 0.16f
                    val start = Offset(qx + (if (left) side / 2 else -side / 2), qy + (if (up) run / 2 else -run / 2))
                    val corner = Offset(start.x, start.y + (if (up) -run else run))
                    val end = Offset(corner.x + (if (left) -side else side), corner.y)
                    arrowPath(listOf(start, corner, end), if (isOn(g)) ink else off, !isOn(g))
                }
            }
            diagonals.forEachIndexed { i, g ->
                val up = i < 2
                val left = i % 2 == 0
                ActionBadge(g, Modifier.align(if (up) (if (left) Alignment.TopStart else Alignment.TopEnd) else (if (left) Alignment.BottomStart else Alignment.BottomEnd)).padding(10.dp))
            }
        }
    }

    /** Two parallel arrows from the middle of the page in each direction, with the action at their end. */
    @Composable
    private fun BoxScope.TwoFingerPicture(places: PreviewPlaces) {
        val density = LocalDensity.current
        val ink = MaterialTheme.colorScheme.primary
        val off = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        val top = places.pageTop
        val height = places.pageBottom - places.pageTop
        Box(Modifier.offset { IntOffset(0, top.roundToInt()) }.fillMaxWidth().height(with(density) { height.toDp() })) {
            Canvas(Modifier.fillMaxSize()) {
                val c = Offset(size.width / 2f, size.height / 2f)
                val gap = 4.dp.toPx()
                val reachX = size.width * 0.26f
                val reachY = size.height * 0.24f
                val near = 10.dp.toPx()
                twos.forEachIndexed { i, g ->
                    val color = if (isOn(g)) ink else off
                    when (i) {
                        0, 1 -> {
                            val sign = if (i == 0) -1f else 1f
                            for (o in floatArrayOf(-gap, gap)) arrowPath(listOf(Offset(c.x + sign * near, c.y + o), Offset(c.x + sign * reachX, c.y + o)), color, !isOn(g))
                        }
                        else -> {
                            val sign = if (i == 2) 1f else -1f
                            for (o in floatArrayOf(-gap, gap)) arrowPath(listOf(Offset(c.x + o, c.y + sign * near), Offset(c.x + o, c.y + sign * reachY)), color, !isOn(g))
                        }
                    }
                }
            }
            ActionBadge(twos[0], Modifier.align(Alignment.CenterStart).padding(start = 6.dp))
            ActionBadge(twos[1], Modifier.align(Alignment.CenterEnd).padding(end = 6.dp))
            ActionBadge(twos[2], Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp))
            ActionBadge(twos[3], Modifier.align(Alignment.TopCenter).padding(top = 6.dp))
        }
    }

    /** A gesture's action as a small round icon (dimmed while off; nothing when it never had one). */
    @Composable
    private fun ActionBadge(g: Gesture, modifier: Modifier) {
        val action = kept(g)
        if (action == 0) return
        Box(
            modifier.size(26.dp).alpha(if (isOn(g)) 1f else 0.45f)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (action == ZoneSettings.TWO_TAB) {
                Icon(
                    painterResource(if (g.setting === ZoneSettings.twoDown) R.drawable.outline_tab_close_black_24 else R.drawable.outline_tab_move_black_24),
                    null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp),
                )
            } else {
                MenuActionIcon(action, 16.dp)
            }
        }
    }

    /** A line through [points] with an arrow head at the last one; dashed for a gesture that is off. */
    private fun DrawScope.arrowPath(points: List<Offset>, color: Color, off: Boolean = false) {
        val width = 2.5.dp.toPx()
        val path = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (p in points.drop(1)) lineTo(p.x, p.y)
        }
        val dash = if (off) androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx())) else null
        drawPath(path, color, style = Stroke(width, cap = StrokeCap.Round, join = StrokeJoin.Round, pathEffect = dash))
        val end = points.last()
        val from = points[points.size - 2]
        val angle = kotlin.math.atan2(end.y - from.y, end.x - from.x)
        val head = 7.dp.toPx()
        for (turn in floatArrayOf(2.6f, -2.6f)) {
            val a = angle + turn
            drawLine(color, end, Offset(end.x + head * kotlin.math.cos(a), end.y + head * kotlin.math.sin(a)), width, StrokeCap.Round)
        }
    }

    /** Restores the defaults of the gestures (zones and their sizes and sensitivity, diagonal and two-finger swipes). */
    private fun resetToDefaults(context: Context) {
        ZoneSettings.reset(context, zoneActions + portSizes + landSizes + diagonals.flatMap { listOf(it.setting, it.last) } + twos.flatMap { listOf(it.setting, it.last) })
        ZtwoSettings.reset(context, senses)
        ZoneSettings.reset(context, listOf(ZoneSettings.pullRefresh))
        BrowserSettings.reset(context, listOf(BrowserSettings.tabMulti))
    }
}
