package com.mycompany.app.ui.browser

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.viewinterop.AndroidView
import com.mycompany.app.wview.FloatFaceView
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Where a coordinator overlay goes (replaces the `CoordinatorLayout.LayoutParams` the activity built for them):
 * [width] / [height] in px or -1 (match) / -2 (wrap), [gravity] (`Gravity.*`), margins, and optionally an [anchor] bar
 * with [anchorGravity], placed like `CoordinatorLayout` placed anchored children (the point on the anchor's edge chosen by
 * [anchorGravity], the child on the [gravity] side of it, kept inside the parent), following the bar while it slides.
 * [shiftY] moves the slot down (the snackbar follows the bottom bar with it). After changing a field call
 * [OverlayStack.relayout].
 */
class OverlayParams(@JvmField var width: Int, @JvmField var height: Int) {
    @JvmField var gravity = 0
    @JvmField var anchor = NO_ANCHOR
    @JvmField var anchorGravity = 0
    @JvmField var leftMargin = 0
    @JvmField var topMargin = 0
    @JvmField var rightMargin = 0
    @JvmField var bottomMargin = 0
    @JvmField var shiftY = 0f
    /** Draw outside the slot (only for views that move / scale themselves, the pull disc); others are clipped to it
     *  like their old parent clipped them (some, e.g. the bars, draw far past their bounds). */
    @JvmField var unclipped = false

    companion object {
        const val NO_ANCHOR = 0
        const val TOP_BAR = 1
        const val BOTTOM_BAR = 2
    }
}

/**
 * The views that used to be stacked on top of the page, now children of the browser's Compose root,
 * drawn above the legacy tree (which still holds the coordinator with the page,
 * the bars and the overlays between them). Two bands, each in the order its views were added (the old child order):
 *
 * 1. coordinator band (lower): what the activity appended to the coordinator (`MyWebCoord`) after the bars, laid out in
 *    the coordinator's area by [OverlayParams] (gravity, margins, bar anchors);
 * 2. body band (upper): what it added on top of the page body (`MyWebBody`):
 *    - full size (-1 × -1): fills the layer inside the body's padding (the window insets);
 *    - floating buttons ([FloatFaceView]): their own size at the position the button reports (body coordinates), only
 *      while visible, so a hidden or moved button never blocks touches meant for the page.
 *
 * Each view sits in a [PassThroughFrame] (touches it does not take go on to the page body). A view that removes itself
 * from its parent (that frame) also leaves the stack.
 */
class OverlayStack(private val body: View) {
    /**
     * What a slot's Compose holder contains: the hosted view inside a frame that (1) hands a gesture the view does not
     * take on to the legacy page body, as the old view stacking let unhandled touches fall through to the views below,
     * and (2) clips its view to the slot like the old parent did, except when asked not to ([OverlayParams.unclipped]):
     * views that move or scale themselves (the pull disc) then still draw outside their slot.
     */
    private class PassThroughFrame(
        context: android.content.Context, private val target: View, private val unclipped: Boolean,
    ) : android.widget.FrameLayout(context) {
        private var forwarding = false
        private val here = IntArray(2)
        private val there = IntArray(2)

        init {
            clipChildren = !unclipped
            clipToPadding = !unclipped
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            if (unclipped) (parent as? ViewGroup)?.let { it.clipChildren = false; it.clipToPadding = false }
        }

        override fun dispatchTouchEvent(event: android.view.MotionEvent): Boolean {
            if (event.actionMasked == android.view.MotionEvent.ACTION_DOWN) forwarding = false
            if (forwarding) return forward(event)
            val handled = super.dispatchTouchEvent(event)
            if (!handled && event.actionMasked == android.view.MotionEvent.ACTION_DOWN) {
                forwarding = forward(event)
                return forwarding
            }
            return handled
        }

        private fun forward(event: android.view.MotionEvent): Boolean {
            getLocationOnScreen(here)
            target.getLocationOnScreen(there)
            val copy = android.view.MotionEvent.obtain(event)
            copy.offsetLocation((here[0] - there[0]).toFloat(), (here[1] - there[1]).toFloat())
            return try { target.dispatchTouchEvent(copy) } finally { copy.recycle() }
        }
    }

    private class Slot(val view: View, val width: Int, val height: Int, val params: OverlayParams?, val frame: PassThroughFrame) {
        var version by mutableIntStateOf(0)
        /** The view is VISIBLE; a hidden view's slot is placed off-screen so its holder cannot catch touches. */
        var shown by mutableStateOf(view.visibility == View.VISIBLE)
    }

    /** Views have no visibility callback: check the hosted views before each frame (cheap, a few views). */
    private val visibilityCheck = android.view.ViewTreeObserver.OnPreDrawListener {
        for (list in arrayOf(coordSlots, bodySlots)) {
            for (slot in list) {
                val now = slot.view.visibility == View.VISIBLE
                if (now != slot.shown) slot.shown = now
            }
        }
        true
    }
    private var watching: android.view.ViewTreeObserver? = null

    private fun watchVisibility() {
        val observer = body.viewTreeObserver
        if (watching === observer && observer.isAlive) return
        observer.addOnPreDrawListener(visibilityCheck)
        watching = observer
    }

    private val coordSlots = mutableStateListOf<Slot>()
    private val bodySlots = mutableStateListOf<Slot>()

    private var padLeft by mutableIntStateOf(0)
    private var padTop by mutableIntStateOf(0)
    private var padRight by mutableIntStateOf(0)
    private var padBottom by mutableIntStateOf(0)

    // the coordinator's area in body coordinates, and the bars in coordinator coordinates (with their slide)
    private var coordLeft by mutableIntStateOf(0)
    private var coordTop by mutableIntStateOf(0)
    private var coordWidth by mutableIntStateOf(0)
    private var coordHeight by mutableIntStateOf(0)
    private var topBarTop by mutableIntStateOf(0)
    private var topBarBottom by mutableIntStateOf(0)
    private var botBarTop by mutableIntStateOf(0)
    private var botBarBottom by mutableIntStateOf(0)

    /** Hosted views that are being moved into their Compose holder (their old parent drops them; not a removal). */
    private val moving = HashSet<View>()

    private val selfRemoval = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {}
        override fun onViewDetachedFromWindow(v: View) {
            if (v in moving) return
            // Removed from its holder by someone else (e.g. `(parent as ViewGroup).removeView(this)`): drop the slot,
            // or an empty holder would keep catching touches.
            v.post { if (v.parent == null) removeView(v) }
        }
    }

    /** Mirrors the body's padding (the window insets), which the old full-size children were laid out inside. */
    fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        padLeft = left; padTop = top; padRight = right; padBottom = bottom
    }

    /** The coordinator's rectangle inside the body (it sits inside the body padding, above the cast controls). */
    fun setCoordRect(left: Int, top: Int, width: Int, height: Int) {
        coordLeft = left; coordTop = top; coordWidth = width; coordHeight = height
    }

    /** The bars' edges in coordinator coordinates, including their scroll slide. */
    fun setBars(topBarTop: Int, topBarBottom: Int, botBarTop: Int, botBarBottom: Int) {
        this.topBarTop = topBarTop; this.topBarBottom = topBarBottom
        this.botBarTop = botBarTop; this.botBarBottom = botBarBottom
    }

    private fun detachForMove(view: View) {
        (view.parent as? ViewGroup)?.let { parent ->
            moving.add(view)
            try { parent.removeView(view) } finally { moving.remove(view) }
        }
    }

    private fun framed(view: View, width: Int, height: Int, unclipped: Boolean = false): PassThroughFrame {
        detachForMove(view)
        val frame = PassThroughFrame(body.context, body, unclipped)
        val lp = android.widget.FrameLayout.LayoutParams(
            if (width == ViewGroup.LayoutParams.WRAP_CONTENT) width else ViewGroup.LayoutParams.MATCH_PARENT,
            if (height == ViewGroup.LayoutParams.WRAP_CONTENT) height else ViewGroup.LayoutParams.MATCH_PARENT,
        )
        moving.add(view)
        try { frame.addView(view, lp) } finally { moving.remove(view) }
        return frame
    }

    /** Was `MyWebBody.addView(view, w, h)`. */
    fun addView(view: View?, width: Int, height: Int) {
        if (view == null || contains(view)) return
        val frame = framed(view, width, height)
        if (view is FloatFaceView) view.hostIn(body)
        view.addOnAttachStateChangeListener(selfRemoval)
        bodySlots.add(Slot(view, width, height, null, frame))
        watchVisibility()
    }

    /**
     * Was `MyWebCoord.addView(view, coordinatorLayoutParams)` (appended: above the other coordinator overlays), or with
     * [index] `addView(view, index, …)`: the views the activity inserted between the bars go under the appended ones.
     */
    @JvmOverloads
    fun addCoordView(view: View?, params: OverlayParams, index: Int = -1) {
        if (view == null || contains(view)) return
        val frame = framed(view, params.width, params.height, params.unclipped)
        view.addOnAttachStateChangeListener(selfRemoval)
        val slot = Slot(view, params.width, params.height, params, frame)
        if (index < 0 || index >= coordSlots.size) coordSlots.add(slot) else coordSlots.add(index, slot)
        watchVisibility()
    }

    fun removeView(view: View?) {
        if (view == null) return
        for (list in arrayOf(coordSlots, bodySlots)) {
            val index = list.indexOfFirst { it.view === view }
            if (index < 0) continue
            view.removeOnAttachStateChangeListener(selfRemoval)
            val slot = list.removeAt(index)
            if (view.parent === slot.frame) {
                moving.add(view)
                try { slot.frame.removeView(view) } finally { moving.remove(view) }
            }
            if (view is FloatFaceView) view.unhost()
            return
        }
    }

    fun contains(view: View?): Boolean =
        view != null && (coordSlots.any { it.view === view } || bodySlots.any { it.view === view })

    /** The placement of a coordinator overlay (edit it, then call [relayout]); null if [view] is not one. */
    fun params(view: View?): OverlayParams? = coordSlots.firstOrNull { it.view === view }?.params

    /** Re-places [view] after its [OverlayParams] changed (was `view.requestLayout()`). */
    fun relayout(view: View?) {
        coordSlots.firstOrNull { it.view === view }?.let { it.version++ }
    }

    private fun factoryFor(slot: Slot): (android.content.Context) -> View = { _ ->
        // The frame may still sit in the holder of an earlier composition of this slot.
        (slot.frame.parent as? ViewGroup)?.removeView(slot.frame)
        slot.frame
    }

    @Composable
    internal fun Layer() {
        Box(Modifier.fillMaxSize()) {
            // The coordinator band lives in the coordinator's area and is clipped to it, as the coordinator clipped its
            // children (a sliding top bar must not draw over the status bar).
            Box(
                Modifier
                    .layout { measurable, _ ->
                        val w = max(0, coordWidth)
                        val h = max(0, coordHeight)
                        val placeable = measurable.measure(Constraints.fixed(w, h))
                        layout(w, h) { placeable.place(coordLeft, coordTop) }
                    }
                    .clipToBounds(),
            ) {
                for (slot in coordSlots) key(slot) { CoordSlot(slot) }
            }
            for (slot in bodySlots) key(slot) { BodySlot(slot) }
        }
    }

    @Composable
    private fun CoordSlot(slot: Slot) {
        val p = slot.params ?: return
        AndroidView(
            factory = factoryFor(slot),
            modifier = Modifier.layout { measurable, _ ->
                @Suppress("UNUSED_VARIABLE") val version = slot.version
                val areaW = max(0, coordWidth)
                val areaH = max(0, coordHeight)
                val maxW = max(0, areaW - p.leftMargin - p.rightMargin)
                val maxH = max(0, areaH - p.topMargin - p.bottomMargin)
                val cw = when {
                    p.width >= 0 -> Constraints.fixedWidth(p.width)
                    p.width == -1 -> Constraints.fixedWidth(maxW)
                    else -> Constraints(maxWidth = maxW)
                }
                val ch = when {
                    p.height >= 0 -> Constraints.fixedHeight(p.height)
                    p.height == -1 -> Constraints.fixedHeight(maxH)
                    else -> Constraints(maxHeight = maxH)
                }
                val placeable = measurable.measure(
                    Constraints(cw.minWidth, cw.maxWidth, ch.minHeight, ch.maxHeight),
                )
                val width = placeable.width
                val height = placeable.height
                val (view, y) = if (p.anchor != OverlayParams.NO_ANCHOR) anchored(p, width, height, areaW, areaH) else plain(p, width, height, areaW, areaH)
                layout(width, height) {
                    if (slot.shown) {
                        placeable.place(view, y + p.shiftY.roundToInt())
                    } else {
                        placeable.place(-4 * width - 10000, -4 * height - 10000)
                    }
                }
            },
        )
    }

    /** Child of the coordinator without an anchor: placed by its gravity inside the area minus margins. */
    private fun plain(overlayParams: OverlayParams, width: Int, height: Int, areaW: Int, areaH: Int): Pair<Int, Int> {
        val g = if (overlayParams.gravity == 0) Gravity.TOP or Gravity.START else overlayParams.gravity
        val hg = Gravity.getAbsoluteGravity(g, body.layoutDirection) and Gravity.HORIZONTAL_GRAVITY_MASK
        val x = when (hg) {
            Gravity.RIGHT -> areaW - width - overlayParams.rightMargin
            Gravity.CENTER_HORIZONTAL -> (areaW - width) / 2 + overlayParams.leftMargin - overlayParams.rightMargin
            else -> overlayParams.leftMargin
        }
        val y = when (g and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.BOTTOM -> areaH - height - overlayParams.bottomMargin
            Gravity.CENTER_VERTICAL -> (areaH - height) / 2 + overlayParams.topMargin - overlayParams.bottomMargin
            else -> overlayParams.topMargin
        }
        return x to y
    }

    /** `CoordinatorLayout`'s anchored placement: point on the bar by [OverlayParams.anchorGravity], child on the
     *  [OverlayParams.gravity] side of it, then kept inside the area (minus margins). The bars span the full width. */
    private fun anchored(overlayParams: OverlayParams, width: Int, height: Int, areaW: Int, areaH: Int): Pair<Int, Int> {
        val aTop = if (overlayParams.anchor == OverlayParams.TOP_BAR) topBarTop else botBarTop
        val aBottom = if (overlayParams.anchor == OverlayParams.TOP_BAR) topBarBottom else botBarBottom
        val dir = body.layoutDirection
        val ag = Gravity.getAbsoluteGravity(if (overlayParams.anchorGravity == 0) Gravity.TOP or Gravity.START else overlayParams.anchorGravity, dir)
        val absoluteGravity = Gravity.getAbsoluteGravity(if (overlayParams.gravity == 0) Gravity.TOP or Gravity.START else overlayParams.gravity, dir)
        var x = when (ag and Gravity.HORIZONTAL_GRAVITY_MASK) {
            Gravity.RIGHT -> areaW
            Gravity.CENTER_HORIZONTAL -> areaW / 2
            else -> 0
        }
        var y = when (ag and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.BOTTOM -> aBottom
            Gravity.CENTER_VERTICAL -> (aTop + aBottom) / 2
            else -> aTop
        }
        when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
            Gravity.RIGHT -> {}
            Gravity.CENTER_HORIZONTAL -> x -= width / 2
            else -> x -= width
        }
        when (absoluteGravity and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.BOTTOM -> {}
            Gravity.CENTER_VERTICAL -> y -= height / 2
            else -> y -= height
        }
        x = max(overlayParams.leftMargin, min(x, areaW - width - overlayParams.rightMargin))
        y = max(overlayParams.topMargin, min(y, areaH - height - overlayParams.bottomMargin))
        return x to y
    }

    @Composable
    private fun BodySlot(slot: Slot) {
        val density = LocalDensity.current
        val view = slot.view
        if (view is FloatFaceView) {
            // Always composed (leaving composition would release the holder and detach the button); while hidden it
            // is placed off-screen so its holder cannot catch touches.
            AndroidView(
                factory = factoryFor(slot),
                modifier = Modifier.layout { measurable, constraints ->
                    val w = if (slot.width > 0) slot.width else constraints.maxWidth
                    val h = if (slot.height > 0) slot.height else constraints.maxHeight
                    val placeable = measurable.measure(Constraints.fixed(w, h))
                    layout(w, h) {
                        if (view.slotShown) {
                            placeable.place(view.slotX.roundToInt(), view.slotY.roundToInt())
                        } else {
                            placeable.place(-4 * w - 10000, -4 * h - 10000)
                        }
                    }
                },
            )
        } else {
            val inset = with(density) {
                Modifier.padding(start = padLeft.toDp(), top = padTop.toDp(), end = padRight.toDp(), bottom = padBottom.toDp())
            }
            val sized = if (slot.width > 0 && slot.height > 0) {
                with(density) { Modifier.size(slot.width.toDp(), slot.height.toDp()) }
            } else {
                Modifier.fillMaxSize()
            }
            AndroidView(
                factory = factoryFor(slot),
                modifier = Modifier
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, placeable.height) {
                            if (slot.shown) placeable.place(0, 0) else placeable.place(-4 * placeable.width - 10000, -4 * placeable.height - 10000)
                        }
                    }
                    .then(inset)
                    .then(sized),
            )
        }
    }
}
