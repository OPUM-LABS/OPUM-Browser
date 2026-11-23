package com.mycompany.app.quick

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.View
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.ripple
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.app.image.ImageSizeListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.quick.QuickAdapter.QuickHolder
import com.mycompany.app.quick.QuickDragListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The new-tab page's list as a Compose grid (replaces `MyRecyclerView` + `MyManagerGrid` + `ItemTouchHelper` in
 * [QuickView]). [QuickAdapter] stays the data source: the grid listens to its change notifications, creates holders
 * with `onCreateViewHolder` and fills them with `onBindViewHolder`; a row view is never attached, the grid draws
 * its Compose [QuickRowView.Content] (plain views, the footer and logo images, sit in an `AndroidView`). Shortcut
 * tiles take one column, every other row the full width. Taps go to the listeners the adapter set on the row.
 *
 * Drag keeps the old helper's rules and callbacks ([QuickDragListener]): once the dragged tile's centre passes the
 * centre of the tile it is over (in the direction of travel), `b(from, to)` may swap them; a tile under the centre
 * that has not been passed is the merge target (drawn 1.2×) and a drop there calls `c(from, target)`; `a(state)`
 * reports start (2) and end (0). The grid scrolls while a tile is held near its top or bottom edge.
 *
 * The scrolling API is the part of `RecyclerView` / `GridLayoutManager` that [QuickView] used.
 */
@SuppressLint("ViewConstructor")
@OptIn(ExperimentalFoundationApi::class)
class QuickGridView(context: Context) : AbstractComposeView(context), QuickAdapter.RowHost {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    private val state = LazyGridState()
    private var adapter: QuickAdapter? = null
    private var dragListener: QuickDragListener? = null
    /** Bumped on every adapter change notification (the grid re-reads the items). */
    private var version by mutableIntStateOf(0)
    private var columns by mutableIntStateOf(5)
    private var cancelNextTouch = false
    private var sizeListener: ImageSizeListener? = null
    private var scrollListener: ((Boolean) -> Unit)? = null
    private var roundRadius = MainApp.dp16
    /** Rows are laid out at their screen bounds (main page) or relative to the grid (folder popup). */
    @JvmField var screenLayout = true
    /**
     * All rows laid out at once, sized to them (the search panel's list sits in a scroll view, where a lazy grid
     * cannot be measured). No drag in this mode.
     */
    @JvmField var wrapContent = false
    /** Dropping a shortcut on another merges them into a folder (main page only). */
    @JvmField var mergeEnabled = true
    /** The folder popup's view of a drag (it draws its own ghost and can drag a shortcut out of the folder). */
    @JvmField var dragHooks: DragHooks? = null
    private var fade: android.animation.ValueAnimator? = null
    private var fadeOut = false
    private var fadeListener: com.mycompany.app.view.MyFadeListener? = null
    /** Recorded drawing of each composed row (for the folder popup's drag ghost). */
    private val layers = HashMap<Int, androidx.compose.ui.graphics.layer.GraphicsLayer>()

    /** What the folder popup needs to know about a drag. */
    interface DragHooks {
        /** A drag of [holder] at [pos] started; [ghost] (the tile's picture) follows when ready. */
        fun started(holder: QuickHolder, pos: Int)
        fun ghost(bitmap: android.graphics.Bitmap)
        /** The dragged tile now sits at [pos]. */
        fun moved(pos: Int)
        fun ended()
    }
    /** Holders of the rows now composed, by item position. */
    private val live = HashMap<QuickHolder, Unit>()

    // drag state
    private var dragKey by mutableStateOf<Any?>(null)
    private var dragIndex = -1
    private var dragStart = Offset.Zero
    private var dragDelta by mutableStateOf(Offset.Zero)
    private var hoverKey by mutableStateOf<Any?>(null)
    private var hoverIndex = -1

    // like a RecyclerView, a change also asks for a new layout (the folder popup sets the grid's size and relies on it)
    private val observer = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() = changed()
        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) = changed()
        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) = changed()
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = changed()
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = changed()
        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) = changed()
    }

    private fun changed() {
        version++
        if (!isInLayout) requestLayout()
    }

    fun setAdapter(quickAdapter: QuickAdapter?) {
        adapter?.unregisterAdapterDataObserver(observer)
        adapter = quickAdapter
        quickAdapter?.registerAdapterDataObserver(observer)
        quickAdapter?.host = this
        version++
    }

    fun setDragListener(listener: QuickDragListener?) {
        dragListener = listener
    }

    /** Columns for shortcut tiles. */
    var spanCount: Int
        get() = columns
        set(value) {
            if (value > 0) columns = value
        }

    val itemCount: Int get() = adapter?.itemCount ?: 0


    // ---- RowHost ----

    override fun rowAt(pos: Int): QuickHolder? = live.keys.firstOrNull { it.gridPos == pos }

    override fun firstVisible(): Int =
        if (wrapContent) (if (itemCount > 0) 0 else -1) else state.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: -1

    override fun lastVisible(): Int =
        if (wrapContent) itemCount - 1 else state.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1

    // ---- scrolling ----

    /** A scroll waiting for the grid to show the adapter's current items ([scrollWhenLaidOut]). */
    private var pendingScroll by mutableStateOf<Pair<Int, Int>?>(null)

    /**
     * Scrolls to [pos] ([offsetPx] of it above the top) once the grid has composed the adapter's current items. Use it
     * right after replacing the whole list: [scrollToPosition] runs at once (main-immediate scope) and would measure the
     * old rows against the new item count and keys ("Place was called on a node which was placed already").
     */
    fun scrollWhenLaidOut(pos: Int, offsetPx: Int = 0) {
        if (pos < 0) return
        pendingScroll = pos to offsetPx
    }

    fun scrollToPosition(pos: Int) {
        if (pos < 0) return
        scope.launch { state.scrollToItem(pos) }
    }

    /** As `GridLayoutManager.scrollToPositionWithOffset`: [pos]'s top at [offset] px from the top. */
    fun scrollToPositionWithOffset(pos: Int, offset: Int) {
        if (pos < 0) return
        scope.launch { state.scrollToItem(pos, -offset) }
    }

    override fun scrollBy(x: Int, y: Int) {
        scope.launch { state.scrollBy(y.toFloat()) }
    }

    fun smoothScrollBy(position: Int, height: Int) {
        scope.launch { state.animateScrollBy(height.toFloat()) }
    }

    /** True once the list is scrolled away from its top. */
    val isScrolled: Boolean get() = state.firstVisibleItemIndex > 0 || state.firstVisibleItemScrollOffset > 0

    /** Called with [isScrolled] after each scroll. */
    fun setOnScrolled(listener: ((Boolean) -> Unit)?) {
        scrollListener = listener
    }

    // ---- what MyRecyclerView offered ----

    /** The next touch is cancelled (a pull-down gesture took over). */
    fun setPullDown(on: Boolean) {
        cancelNextTouch = on
    }

    fun setSizeListener(listener: ImageSizeListener?) {
        sizeListener = listener
    }

    /** Rounded corners, as the view's outline so elevation follows them. */
    fun applyRound(top: Boolean, bottom: Boolean) {
        if (!top && !bottom) {
            clipToOutline = false
            return
        }
        outlineProvider = object : android.view.ViewOutlineProvider() {
            override fun getOutline(view: View, outline: android.graphics.Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, roundRadius.toFloat())
            }
        }
        clipToOutline = true
    }

    fun setRoundSize(radius: Int) {
        roundRadius = radius
        invalidateOutline()
    }

    /**
     * Fades and scales the grid in ([show]) or out around ([px], [py]) in 200 ms;
     * fading out ends with the grid gone and [listener]`.a(false)`.
     */
    fun startFade(px: Int, py: Int, show: Boolean, listener: com.mycompany.app.view.MyFadeListener?) {
        if (fade != null) return
        fadeOut = !show
        fadeListener = listener
        pivotX = px.toFloat()
        pivotY = py.toFloat()
        val anim = android.animation.ValueAnimator.ofFloat(if (show) 0f else 1f, if (show) 1f else 0f)
        anim.duration = 200L
        anim.interpolator = if (show) android.view.animation.DecelerateInterpolator() else android.view.animation.AccelerateInterpolator()
        anim.addUpdateListener {
            val animatedValue = it.animatedValue as Float
            alpha = animatedValue
            scaleX = animatedValue
            scaleY = animatedValue
            if (visibility != VISIBLE) visibility = VISIBLE
        }
        anim.addListener(object : android.animation.AnimatorListenerAdapter() {
            private var cancelled = false
            override fun onAnimationCancel(animation: android.animation.Animator) {
                cancelled = true
                fade = null
                if (fadeOut) fadedOut()
            }

            override fun onAnimationEnd(animation: android.animation.Animator) {
                if (cancelled || fade == null) return
                fade = null
                if (fadeOut) fadedOut() else { alpha = 1f; scaleX = 1f; scaleY = 1f }
            }
        })
        fade = anim
        anim.start()
    }

    /** Stops a running fade (a fade-out still ends with the grid gone). */
    fun cancelFade() {
        fade?.cancel()
        fade = null
    }

    private fun fadedOut() {
        visibility = GONE
        fadeListener?.onFadeEnd(false)
    }

    /** Releases the grid. */
    fun release() {
        adapter?.unregisterAdapterDataObserver(observer)
        live.keys.forEach { (it.itemView as? QuickTileView)?.release() }
        live.clear()
        scope.cancel()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (cancelNextTouch) {
            cancelNextTouch = false
            ev.action = MotionEvent.ACTION_CANCEL
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        sizeListener?.onSize(this, w, h)
    }

    // ---- drag ----

    /**
     * A tap clicks the tile; a long press long-clicks it (the adapter enters selection mode and may call
     * [startDrag]) and the same finger then drags. A scroll of the grid cancels both.
     */
    private suspend fun PointerInputScope.tileGestures(view: View, press: MutableInteractionSource) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val pressed = PressInteraction.Press(down.position)
            press.tryEmit(pressed)
            val long = awaitLongPressOrCancellation(down.id)
            if (long == null) {
                val up = currentEvent.changes.firstOrNull { it.id == down.id }
                if (up != null && !up.pressed && !up.isConsumed) {
                    press.tryEmit(PressInteraction.Release(pressed))
                    view.performClick()
                } else {
                    press.tryEmit(PressInteraction.Cancel(pressed))
                }
                return@awaitEachGesture
            }
            press.tryEmit(PressInteraction.Release(pressed))
            view.performLongClick()
            try {
                drag(long.id) { change ->
                    dragBy(change.positionChange())
                    change.consume()
                }
            } finally {
                endDrag()
            }
        }
    }

    /** Starts dragging [holder]'s tile (the adapter's long press asked for it). */
    fun startDrag(holder: QuickHolder?) {
        val target = holder ?: return
        val info = state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == target.gridPos } ?: return
        dragKey = info.key
        dragIndex = info.index
        dragStart = Offset(info.offset.x.toFloat(), info.offset.y.toFloat())
        dragDelta = Offset.Zero
        hoverKey = null
        hoverIndex = -1
        dragListener?.onDragState(2)
        val hooks = dragHooks
        if (hooks != null) {
            hooks.started(target, info.index)
            val layer = layers[info.key as Int]
            if (layer != null) scope.launch {
                try {
                    hooks.ghost(layer.toImageBitmap().asAndroidBitmap())
                } catch (_: Exception) {
                }
            }
        }
    }

    private fun dragging() = dragKey != null

    private fun dragBy(delta: Offset) {
        if (!dragging()) return
        dragDelta += delta
        val items = state.layoutInfo.visibleItemsInfo
        val self = items.firstOrNull { it.key == dragKey } ?: return
        val cx = dragStart.x + dragDelta.x + self.size.width / 2f
        val cy = dragStart.y + dragDelta.y + self.size.height / 2f
        val over = items.firstOrNull {
            it.key != dragKey && cx > it.offset.x && cx < it.offset.x + it.size.width &&
                cy > it.offset.y && cy < it.offset.y + it.size.height
        }
        if (over == null) {
            hoverKey = null
            hoverIndex = -1
            return
        }
        if (passed(self, over, cx, cy)) {
            val listener = dragListener
            if (listener != null && listener.onMove(dragIndex, over.index)) {
                dragIndex = over.index
                hoverKey = null
                hoverIndex = -1
                dragHooks?.moved(over.index)
                return
            }
        }
        // only a shortcut takes a merge (the old helper's rule: the row's item kind is 0)
        val item = adapter?.itemAt(over.index)
        if (mergeEnabled && item != null && item.itemKind.toInt() == 0) {
            hoverKey = over.key
            hoverIndex = over.index
        } else {
            hoverKey = null
            hoverIndex = -1
        }
    }

    /** The centre has passed [over]'s centre coming from [self]'s slot. */
    private fun passed(self: LazyGridItemInfo, over: LazyGridItemInfo, cx: Float, cy: Float): Boolean {
        val ox = over.offset.x + over.size.width / 2f
        val oy = over.offset.y + over.size.height / 2f
        return if (over.offset.y == self.offset.y) {
            if (over.offset.x > self.offset.x) cx > ox else cx < ox
        } else {
            if (over.offset.y > self.offset.y) cy > oy else cy < oy
        }
    }

    private fun endDrag() {
        if (!dragging()) return
        val from = dragIndex
        val target = hoverIndex
        dragKey = null
        hoverKey = null
        hoverIndex = -1
        dragIndex = -1
        val listener = dragListener
        if (listener != null) {
            if (from != -1 && target != -1 && from != target) listener.onMerge(from, target)
            listener.onDragState(0)
        }
        dragHooks?.ended()
    }

    // ---- content ----

    @Composable
    override fun Content() {
        // read first: setting the adapter bumps it, so the grid appears once there is one
        @Suppress("UNUSED_VARIABLE") val versionNow = version
        val item = adapter ?: return
        val count = item.itemCount
        val density = LocalDensity.current
        LaunchedEffect(Unit) {
            snapshotFlow { state.firstVisibleItemIndex to state.firstVisibleItemScrollOffset }
                .collect { scrollListener?.invoke(isScrolled) }
        }
        val pending = pendingScroll
        LaunchedEffect(pending, versionNow) {
            val target = pending ?: return@LaunchedEffect
            pendingScroll = null
            if (target.first < count) state.scrollToItem(target.first, target.second)
        }
        // scroll while a tile is held near the top or bottom
        LaunchedEffect(dragKey) {
            if (dragKey == null) return@LaunchedEffect
            val edge = with(density) { 56.dp.toPx() }
            val step = with(density) { 10.dp.toPx() }
            while (true) {
                delay(16)
                val info = state.layoutInfo
                val self = info.visibleItemsInfo.firstOrNull { it.key == dragKey } ?: continue
                val top = dragStart.y + dragDelta.y
                val delta = when {
                    top < info.viewportStartOffset + edge -> -step
                    top + self.size.height > info.viewportEndOffset - edge -> step
                    else -> 0f
                }
                // the tile stays under the finger (viewport coordinates); the content moves below it
                if (delta != 0f && state.scrollBy(delta) != 0f) dragBy(Offset.Zero)
            }
        }
        if (wrapContent) {
            AllRows(item, count)
            return
        }
        Box(Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                state = state,
                modifier = Modifier.fillMaxSize(),
            ) {
                items(
                    count = count,
                    key = { index2 -> keyOf(item, index2) },
                    span = { index2 -> if (isTile(item, index2)) GridItemSpan(1) else GridItemSpan(maxLineSpan) },
                    contentType = { index2 -> if (index2 < item.itemCount) item.getItemViewType(index2) else -1 },
                ) { index -> Row(item, index) }
            }
            if (isScrolled) {
                // the old shadow_list_up: 15 % black fading out downwards
                Box(
                    Modifier.align(Alignment.TopCenter).fillMaxWidth().height(with(density) { MainApp.dp16.toDp() })
                        .background(Brush.verticalGradient(listOf(Color(0x26000000), Color.Transparent))),
                )
            }
        }
    }

    /** A stable id per item object (the items have no ids; equal-looking items must stay apart). */
    private val ids = java.util.WeakHashMap<Any, Int>()
    private var nextId = 1

    private fun keyOf(quickAdapter: QuickAdapter, index: Int): Int {
        val item: Any = (if (quickAdapter.recentMode) quickAdapter.recentAt(index) else quickAdapter.itemAt(index)) ?: return -1 - index
        return ids.getOrPut(item) { nextId++ }
    }

    private fun isTile(quickAdapter: QuickAdapter, position: Int): Boolean {
        if (position >= quickAdapter.itemCount) return false
        val itemViewType = quickAdapter.getItemViewType(position)
        return !quickAdapter.recentMode && (itemViewType == 0 || itemViewType == 1)
    }

    /** [wrapContent] mode: tiles in lines of [columns], every other row across the full width. */
    @Composable
    private fun AllRows(item: QuickAdapter, count: Int) {
        androidx.compose.foundation.layout.Column(Modifier.fillMaxWidth()) {
            var itemIndex = 0
            while (itemIndex < count) {
                if (isTile(item, itemIndex)) {
                    val line = ArrayList<Int>()
                    while (itemIndex < count && line.size < columns && isTile(item, itemIndex)) line.add(itemIndex++)
                    androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth()) {
                        for (k in 0 until columns) {
                            Box(Modifier.weight(1f)) {
                                val index = line.getOrNull(k)
                                if (index != null) androidx.compose.runtime.key(keyOf(item, index)) { Row(item, index) }
                            }
                        }
                    }
                } else {
                    val index = itemIndex++
                    androidx.compose.runtime.key(keyOf(item, index)) { Row(item, index) }
                }
            }
        }
    }

    @Composable
    private fun Row(item: QuickAdapter, index: Int) {
        if (index >= item.itemCount) return
        val type = item.getItemViewType(index)
        val holder = remember(type) { item.onCreateViewHolder(this@QuickGridView, type) as QuickHolder }
        DisposableEffect(holder) {
            live[holder] = Unit
            onDispose { live.remove(holder) }
        }
        holder.gridPos = index
        // bind before reading the row's state; again whenever the adapter changed or the row moved
        remember(version, index, keyOf(item, index)) {
            item.onBindViewHolder(holder, index)
            true
        }
        val view = holder.itemView
        val key = keyOf(item, index)
        val isDragged = dragKey == key
        val hover by animateFloatAsState(if (hoverKey == key) 1.2f else 1f, label = "hover")
        val heightPx = view.layoutParams?.height ?: -2
        val heightMod = if (heightPx > 0) Modifier.height(with(LocalDensity.current) { heightPx.toDp() }) else Modifier
        // one fixed chain: inserting modifiers when a drag starts would recreate the gesture handler below and
        // cancel the drag that is just starting
        var mod = Modifier.fillMaxWidth().then(heightMod)
            .zIndex(if (isDragged) 1f else 0f)
            .graphicsLayer {
                if (isDragged) {
                    val self = state.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key }
                    if (self != null) {
                        translationX = dragStart.x + dragDelta.x - self.offset.x
                        translationY = dragStart.y + dragDelta.y - self.offset.y
                    }
                } else {
                    translationX = 0f
                    translationY = 0f
                }
                scaleX = hover
                scaleY = hover
            }
        // the row view is never attached: lay it out at the cell's screen bounds, so code that asks it for its size
        // or getLocationOnScreen (the folder popup opens from the tapped tile) gets the cell
        mod = mod.onGloballyPositioned { coords ->
            val r = coords.boundsInWindow()
            val onScreen = IntArray(2)
            val inWindow = IntArray(2)
            getLocationOnScreen(onScreen)
            getLocationInWindow(inWindow)
            var left = r.left.toInt() + onScreen[0] - inWindow[0]
            var top = r.top.toInt() + onScreen[1] - inWindow[1]
            if (!screenLayout) {
                // relative to the grid (the folder popup adds the grid's own offset)
                left -= onScreen[0]
                top -= onScreen[1]
            }
            view.layout(left, top, left + r.width.toInt(), top + r.height.toInt())
        }
        // record the row's drawing (the folder popup takes the dragged tile's picture from it)
        val layer = rememberGraphicsLayer()
        DisposableEffect(key) {
            layers[key] = layer
            onDispose { if (layers[key] === layer) layers.remove(key) }
        }
        // innermost, so the picture is the row alone (no press ripple, as the old helper took it without background)
        val record = Modifier.drawWithContent {
            layer.record { this@drawWithContent.drawContent() }
            // in the folder popup the dragged tile is drawn by the popup (its ghost), not here
            if (!(isDragged && dragHooks != null)) drawLayer(layer)
        }
        if (view is QuickRowView) {
            mod = mod.alpha(view.rowAlpha)
            if (view is QuickTileView) {
                // the tile itself takes taps; the long press may start a drag
                val press = remember { MutableInteractionSource() }
                mod = mod
                    .clip(RoundedCornerShape(8.dp))
                    .indication(press, ripple())
                    .pointerInput(holder) { tileGestures(view, press) }
            }
            if (view is QuickSearchRow) {
                // a recent search: the adapter's tap / long-press listeners are on the row view
                mod = mod.combinedClickable(onClick = { view.performClick() }, onLongClick = { view.performLongClick() })
            }
            Box(mod.then(record)) { view.Content() }
        } else {
            AndroidView(factory = { (view.parent as? android.view.ViewGroup)?.removeView(view); view }, modifier = mod.then(record))
        }
    }

}
