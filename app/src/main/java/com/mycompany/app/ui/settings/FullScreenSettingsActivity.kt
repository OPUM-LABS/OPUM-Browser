package com.mycompany.app.ui.settings

import android.content.Context
import android.content.res.Configuration
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.FloatSettings
import com.mycompany.app.pref.store.MainSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingSliderRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsJump
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.rememberPref
import com.mycompany.app.ui.state.AppState
import kotlin.math.roundToInt

/**
 * Settings › Toolbars & controls › Screen and scrolling (2026-11-25): a live phone preview stays at the top and shows
 * every change at once: status and navigation bar, the top and bottom bar sliding away with the page (or fixed), the
 * top bar at the top or at the bottom (as the bar editor has it), the back to top button, the floating buttons. Tapping a part of the preview scrolls to its options below and flashes them (the floating
 * buttons open their own page, which is part of Buttons on the page since 2026-11-26). The options are
 * plain rows, so the settings search finds them. (The full screen options and the back to top button had a dialog
 * each before; the browser menu keeps its quick full screen dialog.)
 */
class FullScreenSettingsActivity : OpumComposeActivity() {

    /** The parts of the preview that can be tapped, and the title of the row each one jumps to. */
    private enum class Part(val row: Int) {
        STATUS(R.string.show_status), TOP(R.string.scroll_top), BOTTOM(R.string.scroll_bot), NAVI(R.string.show_navi), UP(R.string.location),
        PULL(R.string.pull_refresh), SCROLL(R.string.show_scroll), VOLUME(R.string.vol_scroll), FLOAT(R.string.float_button)
    }

    @Composable
    override fun Content() {
        val context: Context = this
        val showStatus = rememberPref({ BrowserSettings.showStatus.value }) { BrowserSettings.edit(context) { set(BrowserSettings.showStatus, it) } }
        val showNavi = rememberPref({ BrowserSettings.showNavi.value }) { BrowserSettings.edit(context) { set(BrowserSettings.showNavi, it) } }
        val fixTop = rememberPref({ BrowserSettings.fixTop.value }) { BrowserSettings.edit(context) { set(BrowserSettings.fixTop, it) } }
        val fixBot = rememberPref({ BrowserSettings.fixBot.value }) { BrowserSettings.edit(context) { set(BrowserSettings.fixBot, it) } }
        val scrollSide = rememberPref({ ZoneSettings.scrollPos.value }) { ZoneSettings.edit(context) { set(ZoneSettings.scrollPos, it) } }
        val pull = rememberPref({ ZoneSettings.pullRefresh.value }) { ZoneSettings.edit(context) { set(ZoneSettings.pullRefresh, it) } }
        val volScroll = rememberPref({ ZoneSettings.volScroll.value }) { ZoneSettings.edit(context) { set(ZoneSettings.volScroll, it) } }
        val scrollAnim = rememberPref({ ZtwoSettings.scrollAnim.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.scrollAnim, it) } }
        val upPlace = rememberPref({ ZoneSettings.showUpPos.value }) { ZoneSettings.edit(context) { set(ZoneSettings.showUpPos, it) } }
        val upAlpha = rememberPref({ EditorSettings.upAlpha.value.let { if (it < 0 || it > 90) 25 else it } }) {
            EditorSettings.edit(context) { set(EditorSettings.upAlpha, it) }
        }
        // read again when coming back from the bar editor or the floating buttons page
        var tick by remember { mutableIntStateOf(0) }
        OnScreenResume { tick++ }
        @Suppress("UNUSED_VARIABLE") val read = tick
        // the buttons of both bars as the Layout editor keeps them (single bar: only the bottom one, with the address)
        val topIds = com.mycompany.app.main.MainUtil.parseIntList(MainSettings.topItems.value)?.toList().orEmpty()
        val botIds = com.mycompany.app.main.MainUtil.parseIntList(MainSettings.botItems.value)?.toList().orEmpty()
        val floats = listOf(
            FloatSettings.flt1Icon.value to PrefEditor.withAlpha(FloatSettings.flt1Color.value, FloatSettings.flt1Alpha.value),
            FloatSettings.flt2Icon.value to PrefEditor.withAlpha(FloatSettings.flt2Color.value, FloatSettings.flt2Alpha.value),
            FloatSettings.flt3Icon.value to PrefEditor.withAlpha(FloatSettings.flt3Color.value, FloatSettings.flt3Alpha.value),
        ).filter { it.first != 0 }
        // the transparency while its slider is dragged, so the preview follows the finger
        var liveAlpha by remember { mutableIntStateOf(upAlpha.value) }
        var selected by remember { mutableStateOf<Part?>(null) }
        // the outline only marks the part for a moment
        LaunchedEffect(selected) { if (selected != null) { kotlinx.coroutines.delay(1800); selected = null } }
        var jump by remember { mutableStateOf<SettingsJump?>(null) }
        var resetting by remember { mutableStateOf(false) }
        val portrait = LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE

        SettingsScaffold(
            title = stringResource(R.string.full_tool),
            onBack = { finish() },
            onReset = { resetting = true },
            jumpTo = jump,
            above = if (!portrait) null else {
                {
                    PhonePreview(
                        Look(
                            PreviewBars(
                                showStatus.value, showNavi.value, fixTop.value, fixBot.value, PdfSettings.topToBottom.value, topIds, botIds,
                                EditorSettings.topAlpha.value.coerceIn(0, 90), EditorSettings.botAlpha.value.coerceIn(0, 90),
                                BrowserSettings.tabBar.value, currentPreviewTabs(),
                            ),
                            upPlace.value, liveAlpha, scrollSide.value, volScroll.value, pull.value, floats,
                        ),
                        selected,
                    ) { part ->
                        selected = part
                        // the floating buttons have their own page (Buttons on the page) since 2026-11-26
                        if (part == Part.FLOAT) startActivity(android.content.Intent(context, FloatSettingsActivity::class.java))
                        else jump = SettingsJump(getString(part.row))
                    }
                    Text(
                        stringResource(R.string.look_tap_guide), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    )
                }
            },
        ) {
            settingsHeader(getString(R.string.top_bar))
            item {
                SettingSwitchRow(title = stringResource(R.string.show_status), checked = showStatus.value, onCheckedChange = { selected = Part.STATUS; showStatus.set(it) })
            }
            item {
                // with the top bar moved down in the Layout editor, it is in the bottom frame and follows the bottom bar
                SettingSwitchRow(
                    title = stringResource(R.string.scroll_top), checked = !fixTop.value, onCheckedChange = { selected = Part.TOP; fixTop.set(!it) },
                    summary = if (PdfSettings.topToBottom.value && topIds.isNotEmpty()) stringResource(R.string.scroll_top_at_bot, stringResource(R.string.layout_title)) else null,
                )
            }
            settingsHeader(getString(R.string.bot_bar))
            item {
                SettingSwitchRow(title = stringResource(R.string.scroll_bot), checked = !fixBot.value, onCheckedChange = { selected = Part.BOTTOM; fixBot.set(!it) })
            }
            item {
                SettingSwitchRow(title = stringResource(R.string.show_navi), checked = showNavi.value, onCheckedChange = { selected = Part.NAVI; showNavi.set(it) })
            }
            settingsHeader(getString(R.string.show_up))
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.location),
                    options = MainConst.colourKinds.map { stringResource(MainConst.scrollPosNames[it]) },
                    selectedIndex = MainConst.colourKinds.indexOf(upPlace.value.coerceIn(0, 3)),
                    onSelect = { selected = Part.UP; upPlace.set(MainConst.colourKinds[it]) },
                )
            }
            item {
                SettingSliderRow(
                    title = stringResource(R.string.color_alpha),
                    value = upAlpha.value,
                    range = 0..90,
                    onValueChange = { upAlpha.set(it); liveAlpha = it },
                    label = { "$it%" },
                    enabled = upPlace.value != 0,
                    below = { v -> LaunchedEffect(v) { liveAlpha = v } },
                )
            }
            // glide instead of jump: the back to top button, page up / down and the start page grid
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.use_scroll_anim), checked = scrollAnim.value,
                    onCheckedChange = { selected = Part.UP; scrollAnim.set(it) },
                )
            }
            settingsHeader(getString(R.string.hd_scrolling))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.pull_refresh), checked = pull.value,
                    onCheckedChange = { selected = Part.PULL; pull.set(it) },
                )
            }
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.show_scroll),
                    options = MainConst.scrollPosValues.map { stringResource(MainConst.scrollPosNames[it]) },
                    selectedIndex = MainConst.scrollPosValues.indexOf(scrollSide.value),
                    onSelect = { selected = Part.SCROLL; scrollSide.set(MainConst.scrollPosValues[it]) },
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.vol_scroll), checked = volScroll.value,
                    onCheckedChange = { selected = Part.VOLUME; volScroll.set(it) },
                )
            }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    BrowserSettings.reset(context, listOf(BrowserSettings.showStatus, BrowserSettings.showNavi, BrowserSettings.fixTop, BrowserSettings.fixBot))
                    ZoneSettings.reset(context, listOf(ZoneSettings.showUpPos))
                    EditorSettings.reset(context, listOf(EditorSettings.upAlpha))
                    // the scroll bar's default side follows the reading direction (as on Toolbars & controls)
                    val side = if (com.mycompany.app.main.MainUtil.isRtl(context)) 1 else 3
                    if (ZoneSettings.scrollPos.value != side) { ZoneSettings.editLocal { set(ZoneSettings.scrollPos, side) }; com.mycompany.app.pref.PrefSet.removeNow(context, 15, "mScrollPos") }
                    scrollSide.reload(ZoneSettings.scrollPos.value)
                    ZoneSettings.reset(context, listOf(ZoneSettings.pullRefresh)); pull.reload(ZoneSettings.pullRefresh.value)
                    ZoneSettings.reset(context, listOf(ZoneSettings.volScroll)); volScroll.reload(ZoneSettings.volScroll.value)
                    ZtwoSettings.reset(context, listOf(ZtwoSettings.scrollAnim)); scrollAnim.reload(ZtwoSettings.scrollAnim.value)
                    showStatus.reload(BrowserSettings.showStatus.value); showNavi.reload(BrowserSettings.showNavi.value)
                    fixTop.reload(BrowserSettings.fixTop.value); fixBot.reload(BrowserSettings.fixBot.value)
                    upPlace.reload(ZoneSettings.showUpPos.value); upAlpha.reload(EditorSettings.upAlpha.value); liveAlpha = upAlpha.value
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /** What the preview shows (all from the settings, read by [Content]): the phone's bars and this page's parts. */
    private class Look(
        val bars: PreviewBars, val upPlace: Int, val upAlpha: Int, val scrollSide: Int, val volScroll: Boolean, val pull: Boolean,
        val floats: List<Pair<Int, Int>>,
    )

    /**
     * The phone ([PhonePreview]) with this page's parts: the back to top button where chosen, anchored to the bars at
     * the bottom, the scroll bar, the pull to refresh circle, the floating buttons along the right edge (where they
     * start; on a page each can be dragged to its own place) and the volume rocker. A tap on a part reports it
     * ([onTap]); [selected] is outlined.
     */
    @Composable
    private fun PhonePreview(look: Look, selected: Part?, onTap: (Part) -> Unit) {
        val density = LocalDensity.current
        val dark = AppState.darkUi
        val upSize = 26.dp
        val floatSize = 22.dp
        val scrollW = 6.dp
        val scrollH = 40.dp
        val pullSize = 24.dp
        PhonePreview(
            look.bars,
            onTap = { at, p, width ->
                with(density) {
                    val slack = 6.dp.toPx()
                    val up = upCentre(width, p.anchor, look.upPlace, upSize.toPx())
                    val floatsTop = floatsTop(p.below + (if (look.bars.showNavi) PreviewSize.sys.toPx() else 0f), look.floats.size, floatSize.toPx())
                    val thumb = scrollThumb(width, look.scrollSide, p, 0f, scrollH.toPx(), scrollW.toPx())
                    val pullAt = pullCentre(width, p, pullSize.toPx())
                    val part = when {
                        look.pull && kotlin.math.hypot(at.x - pullAt.first, at.y - pullAt.second) < pullSize.toPx() -> Part.PULL
                        up != null && kotlin.math.hypot(at.x - up.first, at.y - up.second) < upSize.toPx() -> Part.UP
                        look.floats.isNotEmpty() && at.x > width - floatSize.toPx() - 14.dp.toPx() &&
                            at.y in (floatsTop - slack)..(floatsTop + floatsHeight(look.floats.size, floatSize.toPx()) + slack) -> Part.FLOAT
                        thumb != null && kotlin.math.abs(at.x - (thumb.first + scrollW.toPx() / 2f)) < 12.dp.toPx() &&
                            at.y in p.pageTop..p.pageBottom -> Part.SCROLL
                        else -> when (p.barAt(at.y, slack, PreviewSize.sys.toPx())) {
                            PreviewBar.STATUS -> Part.STATUS
                            PreviewBar.NAVI -> Part.NAVI
                            PreviewBar.TOP -> Part.TOP
                            PreviewBar.BOTTOM -> Part.BOTTOM
                            // the tab bar scrolls with the bar it sits by
                            PreviewBar.TABS -> if (look.bars.tabBar >= 3) Part.BOTTOM else Part.TOP
                            null -> null
                        }
                    }
                    if (part != null) onTap(part)
                }
            },
            under = { places, phase ->
                ScrollThumb(look.scrollSide, places, scrollW, scrollH, phase)
                if (look.pull) PullCircle(places, pullSize, dark, phase)
            },
            over = { places, phase ->
                UpButton(look.upPlace, look.upAlpha, upSize, dark, places.anchor) { places.anchorShift(phase()) }
                Floats(look.floats, floatSize)
                Outline(selected, look, places, upSize, floatSize, scrollW, scrollH, pullSize)
            },
            // scrolling with the volume keys: a rocker on the phone's side, the key that matches the page's way lit
            beside = { phase ->
                if (look.volScroll) VolumeRocker(PreviewSize.frameW, PreviewSize.frameH, selected == Part.VOLUME, phase) { onTap(Part.VOLUME) }
            },
        )
    }

    /** Height of the column of [count] floating buttons of [size] px. */
    private fun floatsHeight(count: Int, size: Float): Float = count * size + (count - 1).coerceAtLeast(0) * size / 3f

    /** Top of that column, centred on the screen [height]. */
    private fun floatsTop(height: Float, count: Int, size: Float): Float = (height - floatsHeight(count, size)) / 2f

    /** The floating buttons that are on ([floats]: action and colour), one above the other along the right edge. */
    @Composable
    private fun BoxScope.Floats(floats: List<Pair<Int, Int>>, size: Dp) {
        if (floats.isEmpty()) return
        androidx.compose.foundation.layout.Column(
            Modifier.align(Alignment.CenterEnd).padding(end = 8.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(size / 3),
        ) {
            for ((action, color) in floats) {
                Box(
                    Modifier.size(size).clip(CircleShape).background(Color(color)).border(1.dp, Color(0x33000000), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(painterResource(com.mycompany.app.main.MainUtil.iconResFor(action, 2)), null, Modifier.size(size * 0.6f))
                }
            }
        }
    }

    /**
     * Centre of the back to top button in a screen [width] px wide, just above [anchor] (the top of the bars at the
     * bottom), or null when it is not shown.
     */
    private fun upCentre(width: Float, anchor: Float, place: Int, size: Float): Pair<Float, Float>? {
        val x = when (place) {
            1 -> size / 2f + size / 3f
            2 -> width / 2f
            3 -> width - size / 2f - size / 3f
            else -> return null
        }
        return x to anchor - size / 2f - size / 4f
    }

    @Composable
    private fun BoxScope.UpButton(place: Int, alpha: Int, size: Dp, dark: Boolean, anchor: Float, shift: () -> Float) {
        if (place == 0) return
        var box by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
        Box(Modifier.fillMaxSize().onSizeChanged { box = it }) {
            val sizePx = with(LocalDensity.current) { size.toPx() }
            val centre = upCentre(box.width.toFloat(), anchor, place, sizePx) ?: return@Box
            Box(
                // it moves with the bar it is anchored to
                Modifier.offset { IntOffset((centre.first - sizePx / 2f).roundToInt(), (centre.second - sizePx / 2f + shift()).roundToInt()) }
                    .size(size)
                    .background(Color(PrefEditor.withAlpha(if (dark) -16777216 else -1, alpha)), CircleShape)
                    .border(1.dp, Color(-12632257), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(if (dark) R.drawable.outline_arrow_upward_dark_24 else R.drawable.outline_arrow_upward_black_24),
                    contentDescription = null, modifier = Modifier.size(16.dp),
                )
            }
        }
    }

    /**
     * Top left of the scroll bar's thumb in a screen [width] px wide at [phase] (it goes down the page as the page
     * scrolls), on the [side] chosen (1 left, 3 right), or null when it is not shown.
     */
    private fun scrollThumb(width: Float, side: Int, places: PreviewPlaces, phase: Float, height: Float, thick: Float): Pair<Float, Float>? {
        val x = when (side) {
            1 -> thick / 2f
            3 -> width - thick * 1.5f
            else -> return null
        }
        val room = (places.pageBottom - places.pageTop - height).coerceAtLeast(0f)
        return x to places.pageTop + room * (0.12f + 0.3f * phase)
    }

    @Composable
    private fun BoxScope.ScrollThumb(side: Int, places: PreviewPlaces, thick: Dp, length: Dp, phase: () -> Float) {
        if (side != 1 && side != 3) return
        var box by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
        Box(Modifier.fillMaxSize().onSizeChanged { box = it }) {
            Box(
                Modifier.offset {
                    val t = scrollThumb(box.width.toFloat(), side, places, phase(), length.toPx(), thick.toPx()) ?: (0f to 0f)
                    IntOffset(t.first.roundToInt(), t.second.roundToInt())
                }.size(thick, length).background(Color(0xE6303030.toInt()), RoundedCornerShape(50))
                    .border(1.dp, Color(0xCCFFFFFF.toInt()), RoundedCornerShape(50)),
            )
        }
    }

    /** Centre of the pull to refresh circle when it is pulled all the way, near the top of the page. */
    private fun pullCentre(width: Float, places: PreviewPlaces, size: Float): Pair<Float, Float> =
        width / 2f to places.pageTop + size * 1.1f

    /**
     * The pull to refresh circle: while the page is at its top ([phase] near 0) it comes down from under the top edge
     * of the page, turning, and goes back up as the page scrolls on.
     */
    @Composable
    private fun BoxScope.PullCircle(places: PreviewPlaces, size: Dp, dark: Boolean, phase: () -> Float) {
        var box by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
        Box(Modifier.fillMaxSize().onSizeChanged { box = it }) {
            val sizePx = with(LocalDensity.current) { size.toPx() }
            Box(
                Modifier.offset {
                    val c = pullCentre(box.width.toFloat(), places, sizePx)
                    val out = (1f - phase() / 0.3f).coerceIn(0f, 1f)
                    IntOffset((c.first - sizePx / 2f).roundToInt(), (places.pageTop - sizePx + (c.second - sizePx / 2f - places.pageTop + sizePx) * out).roundToInt())
                }.graphicsLayer { alpha = (1f - phase() / 0.3f).coerceIn(0f, 1f) }
                    .size(size).background(if (dark) Color(0xFF303030.toInt()) else Color.White, CircleShape)
                    .border(1.dp, Color(0x33000000), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(if (dark) R.drawable.outline_refresh_dark_24 else R.drawable.outline_refresh_black_24),
                    contentDescription = null, tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(size * 0.7f).graphicsLayer { rotationZ = phase() * 540f },
                )
            }
        }
    }

    /**
     * The volume rocker on the right side of a phone [frameW] x [frameH], with an up and a down arrow beside it. While
     * the page goes down ([phase] rises) the down key is lit, on the way back the up key. [outlined] marks it.
     */
    @Composable
    private fun BoxScope.VolumeRocker(frameW: Dp, frameH: Dp, outlined: Boolean, phase: () -> Float, onTap: () -> Unit) {
        val dark = AppState.darkUi
        val key = MaterialTheme.colorScheme.onSurfaceVariant
        val lit = MaterialTheme.colorScheme.primary
        val keyH = 22.dp
        val rowW = 26.dp
        // the way the page went since the last frame (true: down)
        val last = remember { floatArrayOf(0f, 1f) }
        fun down(): Boolean {
            val p = phase()
            if (p != last[0]) { last[1] = if (p > last[0]) 1f else 0f; last[0] = p }
            return last[1] == 1f
        }
        androidx.compose.foundation.layout.Row(
            Modifier.align(Alignment.Center).offset(x = frameW / 2 + rowW / 2 - 1.dp, y = -frameH / 4)
                .width(rowW)
                .then(if (outlined) Modifier.border(2.dp, lit, RoundedCornerShape(6.dp)) else Modifier)
                .clickable(onClick = onTap).padding(vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.foundation.layout.Column {
                Box(Modifier.width(5.dp).height(keyH).graphicsLayer { alpha = if (down()) 0.45f else 1f }
                    .background(lit, RoundedCornerShape(topEnd = 3.dp)))
                Box(Modifier.width(5.dp).height(1.dp).background(key))
                Box(Modifier.width(5.dp).height(keyH).graphicsLayer { alpha = if (down()) 1f else 0.45f }
                    .background(lit, RoundedCornerShape(bottomEnd = 3.dp)))
            }
            androidx.compose.foundation.layout.Column(Modifier.padding(start = 2.dp)) {
                Icon(
                    painterResource(if (dark) R.drawable.outline_keyboard_arrow_up_dark_24 else R.drawable.outline_keyboard_arrow_up_black_24),
                    contentDescription = null, tint = key, modifier = Modifier.size(18.dp).padding(top = 2.dp),
                )
                Box(Modifier.height(keyH * 2 + 1.dp - 36.dp))
                Icon(
                    painterResource(if (dark) R.drawable.outline_keyboard_arrow_down_dark_24 else R.drawable.outline_keyboard_arrow_down_black_24),
                    contentDescription = null, tint = key, modifier = Modifier.size(18.dp).padding(bottom = 2.dp),
                )
            }
        }
    }

    /** The outline around the [selected] part, at the part's resting place. */
    @Composable
    private fun BoxScope.Outline(selected: Part?, look: Look, places: PreviewPlaces, upSize: Dp, floatSize: Dp, scrollW: Dp, scrollH: Dp, pullSize: Dp) {
        if (selected == null) return
        val density = LocalDensity.current
        val colour = MaterialTheme.colorScheme.primary
        when (selected) {
            Part.STATUS -> BarOutline(PreviewBar.STATUS, look.bars, places)
            Part.TOP -> BarOutline(PreviewBar.TOP, look.bars, places)
            Part.BOTTOM -> BarOutline(PreviewBar.BOTTOM, look.bars, places)
            Part.SCROLL -> {
                var box by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
                Box(Modifier.fillMaxSize().onSizeChanged { box = it }) {
                    val w = with(density) { scrollW.toPx() }
                    val thumb = scrollThumb(box.width.toFloat(), look.scrollSide, places, 0f, with(density) { scrollH.toPx() }, w) ?: return@Box
                    val pad = with(density) { 4.dp.toPx() }
                    Box(
                        Modifier.offset { IntOffset((thumb.first - pad).roundToInt(), (thumb.second - pad).roundToInt()) }
                            .size(scrollW + 8.dp, scrollH + 8.dp).border(2.dp, colour, RoundedCornerShape(6.dp))
                    )
                }
            }
            Part.PULL -> if (look.pull) {
                var box by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
                Box(Modifier.fillMaxSize().onSizeChanged { box = it }) {
                    val sizePx = with(density) { pullSize.toPx() }
                    val centre = pullCentre(box.width.toFloat(), places, sizePx)
                    val ring = sizePx + with(density) { 8.dp.toPx() }
                    Box(
                        Modifier.offset { IntOffset((centre.first - ring / 2f).roundToInt(), (centre.second - ring / 2f).roundToInt()) }
                            .size(pullSize + 8.dp).border(2.dp, colour, CircleShape)
                    )
                }
            }
            // the rocker outlines itself, outside the screen
            Part.VOLUME -> Unit
            Part.NAVI -> BarOutline(PreviewBar.NAVI, look.bars, places)
            Part.FLOAT -> if (look.floats.isNotEmpty()) {
                val count = look.floats.size
                val height = with(density) { floatsHeight(count, floatSize.toPx()).toDp() } + 8.dp
                Box(Modifier.align(Alignment.CenterEnd).padding(end = 4.dp).width(floatSize + 8.dp).height(height).border(2.dp, colour, RoundedCornerShape(16.dp)))
            }
            Part.UP -> {
                var box by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
                Box(Modifier.fillMaxSize().onSizeChanged { box = it }) {
                    val sizePx = with(density) { upSize.toPx() }
                    val centre = upCentre(box.width.toFloat(), places.anchor, look.upPlace, sizePx) ?: return@Box
                    val ring = sizePx + with(density) { 8.dp.toPx() }
                    Box(
                        Modifier.offset { IntOffset((centre.first - ring / 2f).roundToInt(), (centre.second - ring / 2f).roundToInt()) }
                            .size(upSize + 8.dp).border(2.dp, colour, CircleShape)
                    )
                }
            }
        }
    }
}
