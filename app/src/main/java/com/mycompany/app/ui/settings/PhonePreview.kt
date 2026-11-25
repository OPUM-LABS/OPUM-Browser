package com.mycompany.app.ui.settings

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainConst
import com.mycompany.app.ui.state.AppState
import kotlin.math.roundToInt

/** Sizes of the phone preview ([PhonePreview]), the same on every page that shows it. */
internal object PreviewSize {
    val screenW = 168.dp
    val screenH = 300.dp
    val frameW = screenW + 16.dp
    val frameH = screenH + 24.dp
    val sys = 14.dp
    val bar = 24.dp
    /** A tab of the default height (40 dp in the browser, a 56 dp bar there). */
    val tab = bar * (40f / 56f)
    /** Browser dp to preview dp. */
    const val SCALE = 24f / 56f
}

/** How the tab bar looks: outline or filled, its colour, the add button's side (0 none, 1 left, 3 right), close buttons, size in % of the default. */
internal class PreviewTabs(
    val outline: Boolean, val color: Int, val add: Int, val close: Boolean, val widthPct: Int, val heightPct: Int,
)

/** The tab bar's look as the settings have it now (sizes as % of the default, as on the Tab bar page). */
internal fun currentPreviewTabs(): PreviewTabs = PreviewTabs(
    com.mycompany.app.pref.store.BrowserSettings.tabAccent.value,
    com.mycompany.app.pref.store.BrowserSettings.tabColor.value,
    com.mycompany.app.pref.store.PdfSettings.tabAdd.value,
    com.mycompany.app.pref.store.PdfSettings.tabClose.value,
    Math.round(com.mycompany.app.pref.store.PdfSettings.tabWidth.value * 100f / com.mycompany.app.main.MainApp.dp144).coerceIn(50, 200),
    Math.round(com.mycompany.app.pref.store.PdfSettings.tabHeight.value * 100f / com.mycompany.app.main.MainApp.dp40).coerceIn(50, 200),
)

/**
 * What the phone shows, from the settings: status and navigation bar, which bars scroll, the top bar at the top or
 * at the bottom (as the bar editor keeps it), the buttons of both bars, their transparency, and the tab bar
 * ([tabBar]: 0 none, 1 above / 2 below the top bar, 3 above / 4 below the bottom bar) drawn as [tabs] says.
 * [tabLabel] is what the tab button shows.
 */
internal class PreviewBars(
    val showStatus: Boolean, val showNavi: Boolean, val fixTop: Boolean, val fixBot: Boolean,
    val toBottom: Boolean, val topIds: List<Int>, val botIds: List<Int>, val topAlpha: Int, val botAlpha: Int,
    val tabBar: Int = 0, val tabs: PreviewTabs? = null, val tabLabel: String = "3",
) {
    val hasTop get() = topIds.isNotEmpty()
    val hasBot get() = botIds.isNotEmpty()
    val hasTabs get() = tabBar in 1..4 && tabs != null
}

/** A part of the phone a tap can hit (the parts every preview has). */
internal enum class PreviewBar { STATUS, NAVI, TOP, BOTTOM, TABS }

/**
 * Where the parts rest in a screen [height] px tall ([sys] status / navigation bar, [bar] a browser bar, [tab] the
 * tab bar), as the browser lays them out: the top frame holds the top bar (unless it was moved down) and the tab bar
 * when it is above or below the top bar; the bottom frame holds the bottom bar, the top bar when moved down, and the
 * tab bar when it is above or below the bottom bar. Each frame scrolls as a whole: the top one up unless the top
 * bar is fixed, the bottom one down unless the bottom bar is fixed.
 */
internal class PreviewPlaces(private val look: PreviewBars, height: Float, sys: Float, private val bar: Float, private val tab: Float) {
    val above = if (look.showStatus) sys else 0f
    val below = height - if (look.showNavi) sys else 0f
    val topAtBottom = look.hasTop && look.toBottom
    private val tabsTop = look.hasTabs && look.tabBar <= 2
    private val tabsBot = look.hasTabs && look.tabBar >= 3

    /** Height of the top frame and of the bottom frame. */
    private val topFrameH = (if (look.hasTop && !topAtBottom) bar else 0f) + (if (tabsTop) tab else 0f)
    private val botFrameH = (if (look.hasBot) bar else 0f) + (if (topAtBottom) bar else 0f) + (if (tabsBot) tab else 0f)

    /** Top of the bottom frame, or null when it is empty. */
    val frameY: Float? = if (botFrameH > 0f) below - botFrameH else null

    val tabY: Float? = when {
        tabsTop -> if (look.tabBar == 1 || !look.hasTop || topAtBottom) above else above + bar
        tabsBot -> if (look.tabBar == 3) frameY else below - tab
        else -> null
    }
    val topY: Float? = when {
        !look.hasTop -> null
        topAtBottom -> frameY!! + (if (tabsBot && look.tabBar == 3) tab else 0f)
        else -> above + (if (tabsTop && look.tabBar == 1) tab else 0f)
    }
    val botY: Float? = if (!look.hasBot) null else (frameY!! + (if (tabsBot && look.tabBar == 3) tab else 0f) + (if (topAtBottom) bar else 0f))

    /** How far the top frame moves at full scroll ([phase] 0..1): up out of sight unless fixed. */
    private fun topFrameShift(phase: Float): Float = if (look.fixTop) 0f else -phase * (above + topFrameH)

    /** How far the bottom frame moves at [phase]: down out of sight unless fixed. */
    fun frameShift(phase: Float): Float {
        val y = frameY ?: return 0f
        return if (look.fixBot) 0f else phase * (below - y)
    }

    fun topShift(phase: Float): Float = if (topAtBottom) frameShift(phase) else topFrameShift(phase)
    fun botShift(phase: Float): Float = frameShift(phase)
    fun tabShift(phase: Float): Float = if (tabsBot) frameShift(phase) else topFrameShift(phase)

    /** The page between the frames at rest: where the scroll bar runs. */
    val pageTop: Float = above + topFrameH
    val pageBottom: Float = frameY ?: below

    /** The top of the bottom frame at rest: the back to top button sits just above it (anchored to it). */
    val anchor: Float = frameY ?: below

    /** How far that anchor moves at [phase]. */
    fun anchorShift(phase: Float): Float = frameShift(phase)

    /** Height of the bottom frame (the outline of "the bars at the bottom"). */
    val bottomFrameHeight: Float get() = botFrameH

    /** The bar at [y] px (with [slack] px around each), or null. */
    fun barAt(y: Float, slack: Float, sys: Float): PreviewBar? = when {
        look.showStatus && y < sys -> PreviewBar.STATUS
        look.showNavi && y > below -> PreviewBar.NAVI
        tabY != null && y in tabY..(tabY + tab) -> PreviewBar.TABS
        topY != null && y in (topY - slack)..(topY + bar + slack) -> if (topAtBottom) PreviewBar.BOTTOM else PreviewBar.TOP
        botY != null && y in (botY - slack)..(botY + bar + slack) -> PreviewBar.BOTTOM
        else -> null
    }
}

/**
 * The phone preview of the settings pages (Screen and scrolling, Tab bar): a page scrolls down and back, the bars and
 * the tab bar follow it unless fixed, status and navigation bar are shown or not. [under] draws on the page below
 * the bars, [over] above everything on the screen (outlines), [beside] next to the phone (its side keys). A tap on
 * the screen reports where ([onTap]: the point, the places, the screen width). Without [scrolling] the page stands still.
 */
@Composable
internal fun PhonePreview(
    look: PreviewBars,
    onTap: (Offset, PreviewPlaces, Float) -> Unit,
    under: @Composable BoxScope.(PreviewPlaces, () -> Float) -> Unit = { _, _ -> },
    over: @Composable BoxScope.(PreviewPlaces, () -> Float) -> Unit = { _, _ -> },
    beside: @Composable BoxScope.(() -> Float) -> Unit = {},
    scrolling: Boolean = true,
) {
    val density = LocalDensity.current
    val dark = AppState.darkUi
    // 0: at rest, 1: scrolled so far that every bar that can hide is out of sight
    val moving by rememberInfiniteTransition(label = "scroll").animateFloat(
        0f, 1f, infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Reverse), label = "phase",
    )
    val phase = if (scrolling) moving else 0f
    var box by remember { mutableStateOf(IntSize.Zero) }
    val sysPx = with(density) { PreviewSize.sys.toPx() }
    val barPx = with(density) { PreviewSize.bar.toPx() }
    val tabH = PreviewSize.tab * ((look.tabs?.heightPct ?: 100) / 100f)
    val tabPx = with(density) { tabH.toPx() }
    val places = PreviewPlaces(look, box.height.toFloat(), sysPx, barPx, tabPx)
    val barH = PreviewSize.bar
    Box(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 6.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier.width(PreviewSize.frameW).height(PreviewSize.frameH)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Box(
                Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp))
                    .background(if (dark) Color(0xFF3F3F3F.toInt()) else Color(0xFFDADADA.toInt()))
                    .onSizeChanged { box = it }
                    .pointerInput(look) {
                        detectTapGestures { at ->
                            onTap(at, PreviewPlaces(look, size.height.toFloat(), sysPx, barPx, tabPx), size.width.toFloat())
                        }
                    },
            ) {
                // the page is taller than the screen and scrolls up, so no empty band shows at either end
                Image(
                    painterResource(R.drawable.preview_matterhorn), null, contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().wrapContentHeight(Alignment.Top, unbounded = true).height(PreviewSize.screenH + barH * 3)
                        .graphicsLayer { translationY = places.above - phase * barPx * 2f },
                )
                under(places) { phase }
                // the bars: fixed ones stay, the others slide out of sight with the page (up at the top, down at the
                // bottom; everything in the bottom frame moves as one)
                places.topY?.let { y ->
                    LayoutBarPicture(
                        look.topIds, barH, look.topAlpha, pageEdgeTop = places.topAtBottom,
                        Modifier.graphicsLayer { translationY = y + places.topShift(phase) }, look.tabLabel,
                    )
                }
                places.botY?.let { y ->
                    LayoutBarPicture(
                        look.botIds, barH, look.botAlpha, pageEdgeTop = true,
                        Modifier.graphicsLayer { translationY = y + places.botShift(phase) }, look.tabLabel,
                    )
                }
                val tabs = look.tabs
                places.tabY?.let { y ->
                    if (tabs != null) TabStripPicture(tabs, tabH, Modifier.graphicsLayer { translationY = y + places.tabShift(phase) })
                }
                if (look.showStatus) BarImage(if (dark) R.drawable.sample_status_bar_b else R.drawable.sample_status_bar_w, PreviewSize.sys) { 0f }
                if (look.showNavi) BarImage(if (dark) R.drawable.sample_navi_bar_b else R.drawable.sample_navi_bar_w, PreviewSize.sys) { box.height - sysPx }
                over(places) { phase }
            }
        }
        beside { phase }
    }
}

/** An outline around a band of the screen [height] tall whose top is at [y] px. */
@Composable
internal fun BandOutline(y: Float, height: Dp) {
    Box(
        Modifier.offset { IntOffset(0, y.roundToInt()) }.fillMaxWidth().height(height)
            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
    )
}

/** Outline of a bar of the phone ([bar]) where it rests, as every preview marks it. */
@Composable
internal fun BoxScope.BarOutline(bar: PreviewBar, look: PreviewBars, places: PreviewPlaces) {
    val density = LocalDensity.current
    val barH = PreviewSize.bar
    when (bar) {
        PreviewBar.STATUS -> if (look.showStatus) Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().height(PreviewSize.sys).border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)))
        PreviewBar.NAVI -> if (look.showNavi) Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(PreviewSize.sys).border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)))
        PreviewBar.TOP -> places.topY?.takeIf { !places.topAtBottom }?.let { BandOutline(it, barH) }
        // the bars at the bottom move as one frame, so the outline takes all of it
        PreviewBar.BOTTOM -> places.frameY?.let { BandOutline(it, with(density) { places.bottomFrameHeight.toDp() }) }
        PreviewBar.TABS -> places.tabY?.let { BandOutline(it, PreviewSize.tab * ((look.tabs?.heightPct ?: 100) / 100f)) }
    }
}

/** A status or navigation bar picture [height] tall whose top is at [y] px. */
@Composable
private fun BarImage(res: Int, height: Dp, y: () -> Float) {
    Image(
        painterResource(res), null, contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxWidth().height(height).graphicsLayer { translationY = y() },
    )
}

/**
 * The tab bar as the browser draws it, scaled down: three tabs (the second one open: outlined in the tab colour, or
 * filled with it), close buttons when on, the add button at its side, [height] tall.
 */
@Composable
internal fun TabStripPicture(tabs: PreviewTabs, height: Dp, modifier: Modifier = Modifier) {
    val dark = AppState.darkUi
    val base = if (dark) MyBarFrameColors.dark else MyBarFrameColors.light
    val style = if (tabs.color in MainConst.tabColors.indices) tabs.color else 5
    val accent = Color(if (tabs.outline) MainConst.tabColors[style] else MainConst.tabColorsFill[style])
    val tabW = 144.dp * PreviewSize.SCALE * (tabs.widthPct / 100f)
    val ink = MaterialTheme.colorScheme.onSurface
    val addW = 40.dp * PreviewSize.SCALE
    Box(modifier.fillMaxWidth().height(height).background(base)) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            if (tabs.add == 1) AddButton(addW, ink)
            Row(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(0.dp))) {
                for (i in 0 until 3) {
                    val open = i == 1
                    Box(Modifier.width(tabW).fillMaxHeight()) {
                        val fill = when {
                            !open -> if (dark) Color(0xFF212121.toInt()) else Color(0xFFF8F8F8.toInt())
                            tabs.outline -> if (dark) Color.Black else Color.White
                            else -> accent
                        }
                        Canvas(Modifier.fillMaxSize()) {
                            val inset = 2.dp.toPx()
                            val r = 4.dp.toPx()
                            drawRoundRect(fill, Offset(inset, inset), Size(size.width - 2 * inset, size.height - 2 * inset), CornerRadius(r, r))
                            if (open && tabs.outline) {
                                drawRoundRect(accent, Offset(inset / 2, inset / 2), Size(size.width - inset, size.height - inset), CornerRadius(r, r), style = Stroke(1.dp.toPx()))
                            }
                        }
                        val onFill = open && !tabs.outline
                        val text = if (onFill) Color.White else ink.copy(alpha = 0.55f)
                        // favicon and a line for the title
                        Box(Modifier.align(Alignment.CenterStart).padding(start = 6.dp).size(7.dp).background(text, CircleShape))
                        Box(
                            Modifier.align(Alignment.CenterStart).padding(start = 16.dp, end = if (tabs.close) 14.dp else 6.dp)
                                .fillMaxWidth().height(2.dp).background(text, RoundedCornerShape(1.dp))
                        )
                        if (tabs.close) {
                            Icon(
                                painterResource(if (dark || onFill) R.drawable.outline_close_white_18 else R.drawable.outline_close_black_18), null,
                                tint = text, modifier = Modifier.align(Alignment.CenterEnd).padding(end = 3.dp).size(9.dp),
                            )
                        }
                    }
                }
            }
            if (tabs.add == 3) AddButton(addW, ink)
        }
    }
}

@Composable
private fun AddButton(width: Dp, ink: Color) {
    Box(Modifier.width(width).fillMaxHeight(), contentAlignment = Alignment.Center) {
        Icon(painterResource(R.drawable.outline_add_black_24), null, tint = ink, modifier = Modifier.size(12.dp))
    }
}
