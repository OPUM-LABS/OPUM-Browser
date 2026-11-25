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
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.ZoneSettings
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
 * Settings › Toolbars & controls › Full screen and Toolbar (2026-11-25): a live phone preview stays at the top and shows
 * every change at once: status and navigation bar, the top and bottom bar sliding away with the page (or fixed), the
 * top bar at the top or at the bottom, the back to top button, the floating buttons. Tapping a part of the preview scrolls to its options below and flashes them. The options are
 * plain rows, so the settings search finds them. (The full screen options and the back to top button had a dialog
 * each before; the browser menu keeps its quick full screen dialog.)
 */
class FullScreenSettingsActivity : OpumComposeActivity() {

    /** The parts of the preview that can be tapped, and the title of the row each one jumps to. */
    private enum class Part(val row: Int) {
        STATUS(R.string.show_status), TOP(R.string.bar_pos), BOTTOM(R.string.scroll_bot), NAVI(R.string.show_navi), UP(R.string.location),
        FLOAT(R.string.float_button)
    }

    @Composable
    override fun Content() {
        val context: Context = this
        val showStatus = rememberPref({ BrowserSettings.showStatus.value }) { BrowserSettings.edit(context) { set(BrowserSettings.showStatus, it) } }
        val showNavi = rememberPref({ BrowserSettings.showNavi.value }) { BrowserSettings.edit(context) { set(BrowserSettings.showNavi, it) } }
        val fixTop = rememberPref({ BrowserSettings.fixTop.value }) { BrowserSettings.edit(context) { set(BrowserSettings.fixTop, it) } }
        val fixBot = rememberPref({ BrowserSettings.fixBot.value }) { BrowserSettings.edit(context) { set(BrowserSettings.fixBot, it) } }
        val upPlace = rememberPref({ ZoneSettings.showUpPos.value }) { ZoneSettings.edit(context) { set(ZoneSettings.showUpPos, it) } }
        val upAlpha = rememberPref({ EditorSettings.upAlpha.value.let { if (it < 0 || it > 90) 25 else it } }) {
            EditorSettings.edit(context) { set(EditorSettings.upAlpha, it) }
        }
        val toBottom = rememberPref({ PdfSettings.topToBottom.value }) { PdfSettings.edit(context) { set(PdfSettings.topToBottom, it) } }
        // read again when coming back from the bar editor or the floating buttons page
        var tick by remember { mutableIntStateOf(0) }
        OnScreenResume { tick++ }
        @Suppress("UNUSED_VARIABLE") val read = tick
        val hasTop = !MainSettings.topItems.value.isNullOrEmpty()
        val hasBot = !MainSettings.botItems.value.isNullOrEmpty()
        val floats = listOf(
            FloatSettings.flt1Icon.value to PrefEditor.withAlpha(FloatSettings.flt1Color.value, FloatSettings.flt1Alpha.value),
            FloatSettings.flt2Icon.value to PrefEditor.withAlpha(FloatSettings.flt2Color.value, FloatSettings.flt2Alpha.value),
            FloatSettings.flt3Icon.value to PrefEditor.withAlpha(FloatSettings.flt3Color.value, FloatSettings.flt3Alpha.value),
        ).filter { it.first != 0 }
        // the transparency while its slider is dragged, so the preview follows the finger
        var liveAlpha by remember { mutableIntStateOf(upAlpha.value) }
        var selected by remember { mutableStateOf<Part?>(null) }
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
                            showStatus.value, showNavi.value, fixTop.value, fixBot.value, toBottom.value, hasTop, hasBot,
                            upPlace.value, liveAlpha, floats,
                        ),
                        selected,
                    ) { part ->
                        selected = part
                        jump = SettingsJump(getString(part.row))
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
                SettingSegmentedRow(
                    title = stringResource(R.string.bar_pos),
                    options = listOf(stringResource(R.string.bar_pos_top), stringResource(R.string.bar_pos_bottom)),
                    selectedIndex = if (toBottom.value) 1 else 0,
                    onSelect = { selected = Part.TOP; toBottom.set(it == 1) },
                    enabled = hasTop,
                )
            }
            item {
                SettingSwitchRow(title = stringResource(R.string.show_status), checked = showStatus.value, onCheckedChange = { selected = Part.STATUS; showStatus.set(it) })
            }
            item {
                SettingSwitchRow(title = stringResource(R.string.scroll_top), checked = !fixTop.value, onCheckedChange = { selected = Part.TOP; fixTop.set(!it) })
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
            settingsHeader(getString(R.string.float_button))
            item {
                SettingRow(
                    title = stringResource(R.string.float_button),
                    summary = if (floats.isEmpty()) stringResource(R.string.not_used)
                    else floats.joinToString(", ") { getString(com.mycompany.app.main.MainUtil.menuIconAt(it.first)) },
                    onClick = { selected = Part.FLOAT; startActivity(android.content.Intent(context, FloatSettingsActivity::class.java)) },
                )
            }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    BrowserSettings.reset(context, listOf(BrowserSettings.showStatus, BrowserSettings.showNavi, BrowserSettings.fixTop, BrowserSettings.fixBot))
                    PdfSettings.reset(context, listOf(PdfSettings.topToBottom)); toBottom.reload(PdfSettings.topToBottom.value)
                    ZoneSettings.reset(context, listOf(ZoneSettings.showUpPos))
                    EditorSettings.reset(context, listOf(EditorSettings.upAlpha))
                    showStatus.reload(BrowserSettings.showStatus.value); showNavi.reload(BrowserSettings.showNavi.value)
                    fixTop.reload(BrowserSettings.fixTop.value); fixBot.reload(BrowserSettings.fixBot.value)
                    upPlace.reload(ZoneSettings.showUpPos.value); upAlpha.reload(EditorSettings.upAlpha.value); liveAlpha = upAlpha.value
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /** What the preview shows (all from the settings, read by [Content]). */
    private class Look(
        val showStatus: Boolean, val showNavi: Boolean, val fixTop: Boolean, val fixBot: Boolean,
        val toBottom: Boolean, val hasTop: Boolean, val hasBot: Boolean,
        val upPlace: Int, val upAlpha: Int, val floats: List<Pair<Int, Int>>,
    )

    /**
     * Where the parts rest in a screen [height] px tall ([sys] status / navigation bar, [bar] a browser bar): the top
     * of the top bar and of the bottom bar (null when that bar has no buttons), and whether the top bar is at the bottom.
     */
    private class Places(look: Look, height: Float, sys: Float, bar: Float) {
        val above = if (look.showStatus) sys else 0f
        val below = height - if (look.showNavi) sys else 0f
        val botY: Float? = if (look.hasBot) below - bar else null
        val topY: Float? = when {
            !look.hasTop -> null
            look.toBottom -> below - (if (look.hasBot) bar else 0f) - bar
            else -> above
        }
        val topAtBottom = look.hasTop && look.toBottom
    }

    /**
     * The phone: a page scrolls by up to one bar height and back, the bars follow it unless fixed (up at the top, down
     * at the bottom), status and navigation bar are shown or not, the top bar sits at the top or above the bottom bar,
     * the back to top button where chosen, the floating buttons along the right edge (where they start; on a page
     * each can be dragged to its own place). A tap on a part reports it ([onTap]); [selected] is outlined.
     */
    @Composable
    private fun PhonePreview(look: Look, selected: Part?, onTap: (Part) -> Unit) {
        val density = LocalDensity.current
        val dark = AppState.darkUi
        val sysH = 14.dp
        val barH = 24.dp
        val upSize = 26.dp
        val floatSize = 22.dp
        val w = with(density) { barH.toPx() }
        val r by rememberInfiniteTransition(label = "scroll").animateFloat(
            0f, w, infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse), label = "r",
        )
        val screenW = 168.dp
        val screenH = 300.dp
        var box by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
        val sysPx = with(density) { sysH.toPx() }
        val barPx = with(density) { barH.toPx() }
        val places = Places(look, box.height.toFloat(), sysPx, barPx)
        Box(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.width(screenW + 16.dp).height(screenH + 24.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Box(
                    Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp))
                        .background(if (dark) Color(0xFF3F3F3F.toInt()) else Color(0xFFDADADA.toInt()))
                        .onSizeChanged { box = it }
                        .pointerInput(look) {
                            detectTapGestures { at ->
                                val h = size.height.toFloat()
                                val p = Places(look, h, sysPx, barPx)
                                val slack = 6.dp.toPx()
                                val up = upCentre(size.width.toFloat(), h, look.upPlace, upSize.toPx())
                                val floatsTop = floatsTop(h, look.floats.size, floatSize.toPx())
                                val part = when {
                                    up != null && kotlin.math.hypot(at.x - up.first, at.y - up.second) < upSize.toPx() -> Part.UP
                                    look.floats.isNotEmpty() && at.x > size.width - floatSize.toPx() - 14.dp.toPx() &&
                                        at.y in (floatsTop - slack)..(floatsTop + floatsHeight(look.floats.size, floatSize.toPx()) + slack) -> Part.FLOAT
                                    look.showStatus && at.y < sysPx -> Part.STATUS
                                    look.showNavi && at.y > h - sysPx -> Part.NAVI
                                    p.topY != null && at.y in (p.topY - slack)..(p.topY + barPx + slack) -> Part.TOP
                                    p.botY != null && at.y in (p.botY - slack)..(p.botY + barPx + slack) -> Part.BOTTOM
                                    else -> null
                                }
                                if (part != null) onTap(part)
                            }
                        },
                ) {
                    Image(
                        painterResource(R.drawable.preview_matterhorn), null, contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().graphicsLayer { translationY = places.above + r },
                    )
                    // the bars: fixed ones stay, the others slide out with the page (up at the top, down at the bottom)
                    places.botY?.let { y ->
                        Bar(if (dark) R.drawable.sample_bot_bar_b else R.drawable.sample_bot_bar_w, barH) { if (look.fixBot) y else y + r }
                    }
                    places.topY?.let { y ->
                        Bar(if (dark) R.drawable.sample_top_bar_b else R.drawable.sample_top_bar_w, barH) {
                            if (look.fixTop) y else if (places.topAtBottom) y + r else y - r
                        }
                    }
                    if (look.showStatus) Bar(if (dark) R.drawable.sample_status_bar_b else R.drawable.sample_status_bar_w, sysH) { 0f }
                    if (look.showNavi) Bar(if (dark) R.drawable.sample_navi_bar_b else R.drawable.sample_navi_bar_w, sysH) { box.height - sysPx }
                    UpButton(look.upPlace, look.upAlpha, upSize, dark)
                    Floats(look.floats, floatSize)
                    Outline(selected, look, places, sysH, barH, upSize, floatSize)
                }
            }
        }
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

    /** Centre of the back to top button in a screen of [width] x [height] px, or null when it is not shown. */
    private fun upCentre(width: Float, height: Float, place: Int, size: Float): Pair<Float, Float>? {
        val x = when (place) {
            1 -> size / 2f + size / 3f
            2 -> width / 2f
            3 -> width - size / 2f - size / 3f
            else -> return null
        }
        return x to height * 0.62f
    }

    @Composable
    private fun BoxScope.UpButton(place: Int, alpha: Int, size: Dp, dark: Boolean) {
        if (place == 0) return
        var box by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
        Box(Modifier.fillMaxSize().onSizeChanged { box = it }) {
            val sizePx = with(LocalDensity.current) { size.toPx() }
            val centre = upCentre(box.width.toFloat(), box.height.toFloat(), place, sizePx) ?: return@Box
            Box(
                Modifier.offset { IntOffset((centre.first - sizePx / 2f).roundToInt(), (centre.second - sizePx / 2f).roundToInt()) }
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

    /** The outline around the [selected] part, at the part's resting place. */
    @Composable
    private fun BoxScope.Outline(selected: Part?, look: Look, places: Places, sysH: Dp, barH: Dp, upSize: Dp, floatSize: Dp) {
        if (selected == null) return
        val density = LocalDensity.current
        val colour = MaterialTheme.colorScheme.primary
        val line = Modifier.border(2.dp, colour, RoundedCornerShape(4.dp))
        fun at(y: Float) = Modifier.offset { IntOffset(0, y.roundToInt()) }
        when (selected) {
            Part.STATUS -> if (look.showStatus) Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().height(sysH).then(line))
            Part.TOP -> places.topY?.let { Box(at(it).fillMaxWidth().height(barH).then(line)) }
            Part.BOTTOM -> places.botY?.let { Box(at(it).fillMaxWidth().height(barH).then(line)) }
            Part.NAVI -> if (look.showNavi) Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(sysH).then(line))
            Part.FLOAT -> if (look.floats.isNotEmpty()) {
                val count = look.floats.size
                val height = with(density) { floatsHeight(count, floatSize.toPx()).toDp() } + 8.dp
                Box(Modifier.align(Alignment.CenterEnd).padding(end = 4.dp).width(floatSize + 8.dp).height(height).border(2.dp, colour, RoundedCornerShape(16.dp)))
            }
            Part.UP -> {
                var box by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
                Box(Modifier.fillMaxSize().onSizeChanged { box = it }) {
                    val sizePx = with(density) { upSize.toPx() }
                    val centre = upCentre(box.width.toFloat(), box.height.toFloat(), look.upPlace, sizePx) ?: return@Box
                    val ring = sizePx + with(density) { 8.dp.toPx() }
                    Box(
                        Modifier.offset { IntOffset((centre.first - ring / 2f).roundToInt(), (centre.second - ring / 2f).roundToInt()) }
                            .size(upSize + 8.dp).border(2.dp, colour, CircleShape)
                    )
                }
            }
        }
    }

    /** A bar picture [height] tall whose top is at [y] px. */
    @Composable
    private fun Bar(res: Int, height: Dp, y: () -> Float) {
        Image(
            painterResource(res), null, contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxWidth().height(height).graphicsLayer { translationY = y() },
        )
    }
}
