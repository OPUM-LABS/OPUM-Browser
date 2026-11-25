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
 * back to top button. Tapping a part of the preview scrolls to its options below and flashes them. The options are
 * plain rows, so the settings search finds them. (The full screen options and the back to top button had a dialog
 * each before; the browser menu keeps its quick full screen dialog.)
 */
class FullScreenSettingsActivity : OpumComposeActivity() {

    /** The parts of the preview that can be tapped, and the title of the row each one jumps to. */
    private enum class Part(val row: Int) {
        STATUS(R.string.show_status), TOP(R.string.scroll_top), BOTTOM(R.string.scroll_bot), NAVI(R.string.show_navi), UP(R.string.location)
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
                        showStatus.value, showNavi.value, fixTop.value, fixBot.value, upPlace.value, liveAlpha, selected,
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
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    BrowserSettings.reset(context, listOf(BrowserSettings.showStatus, BrowserSettings.showNavi, BrowserSettings.fixTop, BrowserSettings.fixBot))
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

    /**
     * The phone: a page scrolls by up to `w` and back, the bars follow it unless fixed, status and navigation bar are
     * shown or not, the back to top button sits where chosen. A tap on a part reports it ([onTap]); [selected] is
     * outlined.
     */
    @Composable
    private fun PhonePreview(
        showStatus: Boolean, showNavi: Boolean, fixTop: Boolean, fixBot: Boolean, upPlace: Int, upAlpha: Int,
        selected: Part?, onTap: (Part) -> Unit,
    ) {
        val density = LocalDensity.current
        val dark = AppState.darkUi
        val sysH = 14.dp
        val barH = 24.dp
        val upSize = 26.dp
        val w = with(density) { barH.toPx() }
        val r by rememberInfiniteTransition(label = "scroll").animateFloat(
            0f, w, infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse), label = "r",
        )
        val screenW = 168.dp
        val screenH = 300.dp
        Box(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.width(screenW + 16.dp).height(screenH + 24.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                Box(
                    Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp))
                        .background(if (dark) Color(0xFF3F3F3F.toInt()) else Color(0xFFDADADA.toInt()))
                        .pointerInput(showStatus, showNavi, upPlace) {
                            val sys = sysH.toPx()
                            val bar = barH.toPx()
                            detectTapGestures { at ->
                                val h = size.height.toFloat()
                                val upCentre = upCentre(size.width.toFloat(), h, upPlace, upSize.toPx())
                                val part = when {
                                    upCentre != null && kotlin.math.hypot(at.x - upCentre.first, at.y - upCentre.second) < upSize.toPx() -> Part.UP
                                    showStatus && at.y < sys -> Part.STATUS
                                    at.y < (if (showStatus) sys else 0f) + bar + 8.dp.toPx() -> Part.TOP
                                    showNavi && at.y > h - sys -> Part.NAVI
                                    at.y > h - (if (showNavi) sys else 0f) - bar - 8.dp.toPx() -> Part.BOTTOM
                                    else -> null
                                }
                                if (part != null) onTap(part)
                            }
                        },
                ) {
                    val sysPx = with(density) { sysH.toPx() }
                    Image(
                        painterResource(R.drawable.preview_matterhorn), null, contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().graphicsLayer { translationY = if (showStatus) r + sysPx else r },
                    )
                    // the bars: fixed ones stay, the others slide out with the page
                    Bar(if (dark) R.drawable.sample_top_bar_b else R.drawable.sample_top_bar_w, Alignment.TopCenter, barH) {
                        val base = if (showStatus) sysPx else 0f
                        if (fixTop) base else base - r
                    }
                    Bar(if (dark) R.drawable.sample_bot_bar_b else R.drawable.sample_bot_bar_w, Alignment.BottomCenter, barH) {
                        val base = if (showNavi) -sysPx else 0f
                        if (fixBot) base else base + r
                    }
                    if (showStatus) Bar(if (dark) R.drawable.sample_status_bar_b else R.drawable.sample_status_bar_w, Alignment.TopCenter, sysH) { 0f }
                    if (showNavi) Bar(if (dark) R.drawable.sample_navi_bar_b else R.drawable.sample_navi_bar_w, Alignment.BottomCenter, sysH) { 0f }
                    UpButton(upPlace, upAlpha, upSize, dark)
                    Outline(selected, showStatus, showNavi, sysH, barH, upPlace, upSize)
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
    private fun BoxScope.Outline(selected: Part?, showStatus: Boolean, showNavi: Boolean, sysH: Dp, barH: Dp, upPlace: Int, upSize: Dp) {
        if (selected == null) return
        val colour = MaterialTheme.colorScheme.primary
        val line = Modifier.border(2.dp, colour, RoundedCornerShape(4.dp))
        when (selected) {
            Part.STATUS -> if (showStatus) Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().height(sysH).then(line))
            Part.TOP -> Box(Modifier.align(Alignment.TopCenter).offset(y = if (showStatus) sysH else 0.dp).fillMaxWidth().height(barH).then(line))
            Part.BOTTOM -> Box(Modifier.align(Alignment.BottomCenter).offset(y = if (showNavi) -sysH else 0.dp).fillMaxWidth().height(barH).then(line))
            Part.NAVI -> if (showNavi) Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(sysH).then(line))
            Part.UP -> {
                var box by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
                Box(Modifier.fillMaxSize().onSizeChanged { box = it }) {
                    val sizePx = with(LocalDensity.current) { upSize.toPx() }
                    val centre = upCentre(box.width.toFloat(), box.height.toFloat(), upPlace, sizePx) ?: return@Box
                    val ring = sizePx + with(LocalDensity.current) { 8.dp.toPx() }
                    Box(
                        Modifier.offset { IntOffset((centre.first - ring / 2f).roundToInt(), (centre.second - ring / 2f).roundToInt()) }
                            .size(upSize + 8.dp).border(2.dp, colour, CircleShape)
                    )
                }
            }
        }
    }

    @Composable
    private fun BoxScope.Bar(res: Int, align: Alignment, height: Dp, dy: () -> Float) {
        Image(
            painterResource(res), null, contentScale = ContentScale.FillBounds,
            modifier = Modifier.align(align).fillMaxWidth().height(height).graphicsLayer { translationY = dy() },
        )
    }
}
