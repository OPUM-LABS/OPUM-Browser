package com.mycompany.app.ui.browser

import androidx.compose.ui.res.stringResource
import android.content.Context
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import android.view.View
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.quick.TabDragListener
import com.mycompany.app.web.WebTabItem
import com.mycompany.app.web.WebTabBar
import com.mycompany.app.web.WebViewActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The browser's tab bar: a row of tabs drawn from the [WebTabBar] model (tabs and tab groups), in Compose.
 * Tap a tab -> [WebTabBar.Listener.onTabClick]; the close / group-menu button -> [WebTabBar.Listener.onCloseTab]; long
 * press -> [WebTabBar.Listener.onTabLongPress], then the
 * tab follows the finger: moving over a neighbour's far half reorders ([TabDragListener.onMove]), resting over its near half
 * highlights it and letting go there groups the two ([WebTabBar.Listener.onTabsMerged]), and a vertical flick is the swipe action when the
 * swipe preference is "close tab". The bar scrolls with the finger and to the selected tab ([scrollTo], [smoothScrollTo]).
 */
class ComposeTabBar(context: Context?) : ComposeOverlay(context!!) {
    @JvmField var dragListener: TabDragListener? = null
    /** False in the group popup: resting over a neighbour does not offer to group. */
    @JvmField var allowMerge = true
    /** True in the group popup: a vertical flick in either direction is the swipe action. */
    @JvmField var swipeAny = false
    /** Reports the dragged tab: phase 0 start, 1 move, 2 end; x / y in this view's coordinates; the bar position. */
    @JvmField var dragReport: ((Int, Float, Float, Int) -> Unit)? = null
    /** Called when a drag ends (bar position given); true = the tab was dropped elsewhere, the normal end is skipped. */
    @JvmField var dragDrop: ((Int) -> Boolean)? = null
    /** Hides the dragged tab here (the popup draws its own floating copy). */
    var hideDragged by mutableStateOf(false)
    var adapter: WebTabBar? by mutableStateOf<WebTabBar?>(null)

    private val listState = LazyListState()
    private var scrollRequest by mutableStateOf<ScrollRequest?>(null)
    private var seq = 0
    private var drag by mutableStateOf<Drag?>(null)
    private val anchor = View(context)

    private class ScrollRequest(val pos: Int, val smooth: Boolean, val id: Int)

    private class Drag(val key: Any, pos: Int, val swipe: Boolean) {
        var pos by mutableIntStateOf(pos)
        var dx by mutableFloatStateOf(0f)
        var dy by mutableFloatStateOf(0f)
        var hover by mutableIntStateOf(-1)
        var gone by mutableStateOf(false)
    }

    init {
        addView(anchor, LayoutParams(1, 1))
        setPage({ AppState.darkUi }) { Bar() }
    }

    /** Makes bar position [pos] visible, jumping. */
    fun scrollTo(pos: Int) { scrollRequest = ScrollRequest(pos, false, ++seq) }

    /** Makes bar position [pos] visible, animated. */
    fun smoothScrollTo(pos: Int) { scrollRequest = ScrollRequest(pos, true, ++seq) }

    /** The bar position of the tab under screen point ([x], [y]), or -1. */
    fun tabAt(rawX: Int, rawY: Int): Int {
        val where = IntArray(2)
        getLocationOnScreen(where)
        val localX = rawX - where[0]
        val localY = rawY - where[1]
        if (localY < 0 || localY > height) return -1
        return listState.layoutInfo.visibleItemsInfo.firstOrNull { localX >= it.offset && localX < it.offset + it.size }?.index ?: -1
    }

    /** A view placed over the tab at [pos] (popup menus anchor to a view). */
    private fun anchorAt(pos: Int): View {
        val info = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == pos }
        if (info != null) {
            anchor.layout(info.offset, 0, info.offset + info.size, height)
        }
        return anchor
    }

    @Composable
    private fun Bar() {
        val model = adapter ?: return
        val version = model.version
        val items = model.tabItems ?: return
        val density = LocalDensity.current
        val request = scrollRequest
        LaunchedEffect(request) {
            if (request == null) return@LaunchedEffect
            val info = listState.layoutInfo
            val visible = info.visibleItemsInfo.firstOrNull { it.index == request.pos }
            val start = info.viewportStartOffset
            val end = info.viewportEndOffset
            if (visible != null && visible.offset >= start && visible.offset + visible.size <= end) return@LaunchedEffect
            val toEnd = visible != null && visible.offset > start
            val size = visible?.size ?: (info.visibleItemsInfo.firstOrNull()?.size ?: 0)
            val offset = if (toEnd) -(end - start - size) else 0
            if (request.smooth) listState.animateScrollToItem(request.pos, offset) else listState.scrollToItem(request.pos, offset)
        }
        val current = drag
        // a swiped-away tab stays hidden until the activity has updated the list
        LaunchedEffect(current?.gone) {
            if (current?.gone == true) { delay(500); drag = null }
        }
        // edge auto-scroll while dragging a tab
        LaunchedEffect(current) {
            if (current == null || current.swipe) return@LaunchedEffect
            while (true) {
                delay(16)
                val info = listState.layoutInfo
                val slot = info.visibleItemsInfo.firstOrNull { it.index == current.pos } ?: continue
                val left = slot.offset + current.dx
                val edge = with(density) { 40.dp.toPx() }
                val step = with(density) { 10.dp.toPx() }
                val delta = when {
                    left < info.viewportStartOffset + edge -> -step
                    left + slot.size > info.viewportEndOffset - edge -> step
                    else -> 0f
                }
                if (delta != 0f) listState.scrollBy(delta)
            }
        }
        LazyRow(state = listState, userScrollEnabled = current == null, modifier = Modifier.fillMaxSize()) {
            itemsIndexed(items, key = { _, item -> System.identityHashCode(item) }) { pos, item ->
                val tab = item as WebTabItem
                TabItem(model, tab, pos, version)
            }
        }
    }

    @Composable
    private fun TabItem(model: WebTabAdapter0, tab: WebTabItem, position: Int, version: Int) {
        val density = LocalDensity.current
        val ctx = LocalContext.current
        val pos by rememberUpdatedState(position)
        val custom = model.customStyle
        val outline = if (custom) model.customOutline else BrowserSettings.tabAccent.value
        var style = if (custom) model.customColor else BrowserSettings.tabColor.value
        val showClose = if (custom) model.customClose else PdfSettings.tabClose.value
        val widthPx = if (custom) model.customWidth else PdfSettings.tabWidth.value
        val tintMode = model.tintMode
        // icon / text kind: a light custom colour gets the light bar's dark icons
        val ink = MainUtil.barInk(tintMode)
        val selected = position == model.selectedPos
        val onFill = selected && !outline
        if (style < 0 || style >= MainConst.tabColors.size) style = 5
        val accent = if (selected) (if (onFill) MainConst.tabColorsFill[style] else MainConst.tabColors[style]) else 0
        val group = tab.groupTabs
        val flatIndex = WebTabBar.groupColor(tab)
        val head = model.shownAt(flatIndex)
        val fill: Int
        val stroke: Int
        if (!selected) {
            fill = if (tintMode == 0) -460552 else if (tintMode == 1) -14606047 else model.tintColor
            stroke = 0
        } else if (outline) {
            fill = if (tintMode == 0) -1 else if (tintMode == 1) -16777216 else tintMode
            stroke = accent
        } else {
            fill = accent
            stroke = 0
        }
        val textColor = if (onFill) -1 else if (ink == 0) -16777216 else -1040187393
        val press = if (onFill) accent else MainUtil.themeIconColor(tintMode, model.tintColor)
        var pressed by remember { mutableStateOf(false) }
        val state = drag
        val mine = state != null && state.key === tab
        val hovered = state != null && !state.swipe && state.hover == position
        val scale by animateFloatAsState(if (hovered) 1.1f else 1f, label = "hover")
        val title = if (group != null) {
            val groupName = head?.groupName
            if (groupName.isNullOrEmpty()) stringResource(R.string.group_title) else groupName
        } else WebViewActivity.tabTitleFor(ctx, head)
        val closeIcon = if (group != null) {
            if (onFill) R.drawable.outline_more_vert_white_20 else if (ink == 0) R.drawable.outline_more_vert_black_20 else R.drawable.outline_more_vert_dark_20
        } else {
            if (onFill) R.drawable.outline_close_white_18 else if (ink == 0) R.drawable.outline_close_black_18 else R.drawable.outline_close_dark_18
        }
        val widthDp = with(density) { widthPx.toDp() }
        val swipeUp = swipeAny || BrowserSettings.tabSwipeUp.value == 38
        val swipeDown = swipeAny || BrowserSettings.tabSwipeDown.value == 38
        Box(
            Modifier
                .width(widthDp).fillMaxHeight()
                .pointerInput(tab) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = true)
                        pressed = true
                        val slop = viewConfiguration.touchSlop
                        val outcome = withTimeoutOrNull(viewConfiguration.longPressTimeoutMillis) {
                            var result = 0 // 1 tap, 2 swipe, 3 scroll
                            while (result == 0) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: return@withTimeoutOrNull 3
                                if (change.isConsumed && !change.pressed) return@withTimeoutOrNull 3
                                if (!change.pressed) { change.consume(); return@withTimeoutOrNull 1 }
                                val d = change.position - down.position
                                if (abs(d.y) > slop && abs(d.y) > abs(d.x) && ((d.y < 0 && swipeUp) || (d.y > 0 && swipeDown))) return@withTimeoutOrNull 2
                                if (abs(d.x) > slop || (abs(d.y) > slop && !(swipeUp || swipeDown))) return@withTimeoutOrNull 3
                            }
                            result
                        }
                        when (outcome) {
                            1 -> {
                                pressed = false
                                val info = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == pos }
                                val centre = (info?.offset ?: 0) + (info?.size ?: 0) / 2
                                model.barListener?.onTabClick(centre, flatIndex, pos, group != null)
                            }
                            2 -> {
                                pressed = false
                                val started = Drag(tab, pos, true)
                                drag = started
                                dragListener?.onDragState(1)
                                var committed = false
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Main)
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) { change.consume(); break }
                                    started.dy = change.position.y - down.position.y
                                    change.consume()
                                    dragListener?.onSwipePos(pos)
                                }
                                committed = abs(started.dy) > size.height / 2f && ((started.dy < 0 && swipeUp) || (started.dy > 0 && swipeDown))
                                if (committed) {
                                    started.gone = true
                                    dragListener?.onSwiped(pos)
                                    dragListener?.onDragState(0)
                                } else {
                                    drag = null
                                    dragListener?.onDragState(0)
                                }
                            }
                            null -> {
                                // long press: the tab starts to follow the finger
                                val info = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == pos }
                                model.barListener?.onTabLongPress(anchorAt(pos), flatIndex, pos, group != null)
                                val started = Drag(tab, pos, false)
                                drag = started
                                dragListener?.onDragState(2)
                                dragReport?.invoke(0, down.position.x + (info?.offset ?: 0), down.position.y, pos)
                                var merged = false
                                while (true) {
                                    val event = awaitPointerEvent(PointerEventPass.Main)
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) { change.consume(); break }
                                    started.dx = change.position.x - down.position.x
                                    change.consume()
                                    val layout = listState.layoutInfo
                                    val slot = layout.visibleItemsInfo.firstOrNull { it.index == started.pos } ?: continue
                                    dragReport?.invoke(1, slot.offset + change.position.x, change.position.y, started.pos)
                                    val centre = slot.offset + started.dx + slot.size / 2f
                                    val target = layout.visibleItemsInfo.firstOrNull { it.index != started.pos && centre >= it.offset && centre < it.offset + it.size }
                                    if (target == null) { started.hover = -1; continue }
                                    val targetCentre = target.offset + target.size / 2f
                                    val passed = if (target.index > started.pos) centre >= targetCentre else centre <= targetCentre
                                    if (passed) {
                                        if (dragListener?.onMove(started.pos, target.index) == true) {
                                            started.pos = target.index
                                        }
                                        started.hover = -1
                                    } else {
                                        started.hover = if (allowMerge) target.index else -1
                                    }
                                }
                                val from = started.pos
                                val into = started.hover
                                if (into != -1 && into != from) {
                                    merged = true
                                    dragListener?.onMerge(from, into)
                                }
                                drag = null
                                dragReport?.invoke(2, 0f, 0f, from)
                                if (!merged && dragDrop?.invoke(from) != true) dragListener?.onDragState(0)
                            }
                        }
                        pressed = false
                    }
                }
                .graphicsLayer {
                    if (mine) {
                        val slot = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == state.pos }
                        translationX = if (state.swipe) 0f else state.dx
                        translationY = if (state.swipe) state.dy else 0f
                        alpha = if (state.gone || hideDragged) 0f else if (state.swipe) (1f - abs(state.dy) / size.height).coerceIn(0f, 1f) else 1f
                        shadowElevation = if (state.swipe) 0f else 8f
                        if (slot == null) alpha = 0f
                    }
                    scaleX = scale; scaleY = scale
                },
        ) {
            val edgeInset = MainApp.dp4.toFloat()
            val radius = MainApp.dp8.toFloat()
            val groupCount = group?.size ?: 0
            val segment = if (selected && group != null) (model.shownIndex - flatIndex).coerceIn(0, (groupCount - 1).coerceAtLeast(0)) else -1
            Canvas(Modifier.fillMaxSize()) {
                val height = size.height
                val halfButton = (MainApp.dp40 / 2).toFloat()
                val vertical = if (height - halfButton >= radius) edgeInset else (height - halfButton) / 2f
                val topLeft = Offset(edgeInset, vertical)
                val rect = Size(size.width - 2 * edgeInset, height - 2 * vertical)
                drawRoundRect(Color(fill), topLeft, rect, CornerRadius(radius, radius))
                if (pressed || mine) drawRoundRect(Color(press).copy(alpha = if (mine) 0.25f else 0.4f), topLeft, rect, CornerRadius(radius, radius))
                if (stroke != 0) {
                    val inset = edgeInset / 2f
                    val strokeWidth = MainApp.dp1.toFloat()
                    val vi = if (height - halfButton >= radius) inset else maxOf((height - halfButton) / 2f, strokeWidth / 2f)
                    drawRoundRect(
                        Color(stroke), Offset(inset, vi), Size(size.width - 2 * inset, height - 2 * vi),
                        CornerRadius(radius, radius), style = Stroke(strokeWidth),
                    )
                }
                if (segment >= 0 && groupCount > 0) {
                    val margin = edgeInset + MainApp.dp16
                    var segW = ((size.width - 2 * margin) / groupCount).roundToInt().toFloat()
                    var left = segment * segW + margin
                    if (segW < 2f) segW = 2f
                    var right = left + segW
                    val limit = size.width - margin
                    if (right > limit) { left = limit - segW; right = limit }
                    val centreY = height - (if (height - halfButton >= radius) edgeInset else maxOf((height - halfButton) / 2f, edgeInset / 4f))
                    val half = edgeInset / 2f
                    drawRoundRect(
                        Color(if (ink == 0) -16777216 else -4144960), Offset(left, centreY - half), Size(right - left, half * 2),
                        CornerRadius(half, half),
                    )
                }
            }
            // favicon / group icon
            Box(Modifier.align(Alignment.CenterStart).padding(start = 14.dp).size(20.dp)) {
                Favicon(model, tab, head, group, flatIndex, position, onFill, tintMode, title ?: "")
            }
            Text(
                title ?: "", maxLines = 1, overflow = TextOverflow.Clip, color = Color(textColor),
                style = TextStyle(fontSize = 14.sp),
                modifier = Modifier.align(Alignment.CenterStart)
                    .padding(start = with(density) { model.padStart.toDp() }, end = with(density) { (if (showClose) model.padEnd else model.padSmall).toDp() }),
            )
            if (showClose) {
                Box(
                    Modifier.align(Alignment.CenterEnd).width(with(density) { MainApp.dp40.toDp() }).fillMaxHeight()
                        .clickable { model.barListener?.onCloseTab(flatIndex, anchorAt(pos), group != null) },
                    contentAlignment = Alignment.Center,
                ) { Image(painterResource(closeIcon), contentDescription = null) }
            }
        }
    }

    @Composable
    private fun Favicon(
        model: WebTabBar, tab: WebTabItem, head: WebTabItem?, group: ArrayList<WebTabItem?>?, flatIndex: Int,
        pos: Int, onFill: Boolean, tintMode: Int, title: String,
    ) {
        val density = LocalDensity.current
        val radius = with(density) { MainApp.dp8.toDp() }
        val letterSize = with(density) { ((MainApp.dp4 / 2) + MainApp.dp8).toSp() }
        if (group != null) {
            var color = head?.groupColor ?: 0
            if (color == 0) color = -65536
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                // the folder drawables are layered legacy drawables a Compose painter cannot draw: use a plain ImageView
                val folder = WebTabBar.colorIndex(color, tintMode != 0)
                androidx.compose.ui.viewinterop.AndroidView(
                    factory = { c -> android.widget.ImageView(c).apply { scaleType = android.widget.ImageView.ScaleType.FIT_CENTER } },
                    update = { it.setImageResource(folder) },
                    modifier = Modifier.fillMaxSize(),
                )
                if (group.size != 0) Text(group.size.toString(), color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = letterSize)
            }
            return
        }
        val url = head?.tabUrl
        val ink = MainUtil.barInk(tintMode)
        val tint = if (onFill) -1 else if (ink == 0) -16777216 else -1040187393
        if (url.isNullOrEmpty()) {
            Letter(title, tint, if (onFill) R.drawable.outline_public_white_24 else if (ink == 0) R.drawable.outline_public_black_24 else R.drawable.outline_public_dark_24, letterSize)
            return
        }
        if (url.startsWith("file:///") || url.startsWith("/data/")) {
            val res = if ("file:///android_asset/shortcut.html" == url) {
                if (onFill) R.drawable.outline_home_white_24 else if (ink == 0) R.drawable.outline_home_black_24 else R.drawable.outline_home_dark_24
            } else {
                if (onFill) R.drawable.outline_offline_pin_white_24 else if (ink == 0) R.drawable.outline_offline_pin_black_24 else R.drawable.outline_offline_pin_dark_24
            }
            Image(painterResource(res), contentDescription = null, modifier = Modifier.fillMaxSize())
            return
        }
        val key = head.uid
        val cached = model.icons[key]
        LaunchedEffect(key, model.version) { if (cached == null) model.loadIcon(head, this@ComposeTabBar, pos) }
        if (cached != null) {
            Image(cached.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(radius)))
        } else {
            Letter(title, tint, if (onFill) R.drawable.outline_public_white_24 else if (ink == 0) R.drawable.outline_public_black_24 else R.drawable.outline_public_dark_24, letterSize)
        }
    }

    /** The site's first letter in a ring, or the globe when there is none. */
    @Composable
    private fun Letter(title: String, tint: Int, fallback: Int, letterSize: androidx.compose.ui.unit.TextUnit) {
        val glyph = MainUtil.stripPunctuation(title, null)
        if (glyph.isNullOrEmpty()) {
            Image(painterResource(fallback), contentDescription = null, modifier = Modifier.fillMaxSize())
            return
        }
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(Color(tint), radius = size.minDimension / 2f - MainApp.dp4 / 2f, style = Stroke(MainApp.dp1.toFloat()))
            }
            Text(glyph, color = Color(tint), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = letterSize)
        }
    }
}

private typealias WebTabAdapter0 = WebTabBar
