package com.mycompany.app.ui.browser

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Hides and shows the browser bars while the page scrolls. Replaces
 * the coordinator's `AppBarLayout` (an empty scroll | enterAlways | snap child) and its behaviours: the offset is a
 * Material 3 [TopAppBarScrollBehavior] (`enterAlwaysScrollBehavior`, owner decision: M3 standard feel) created by
 * [BrowserScreen], fed directly by the page (`WebNestView` passes its touch-scroll deltas to [preScroll] / [postScroll]
 * / [stop]) instead of Android nested scrolling through the coordinator.
 *
 * Keeps the parts of the `AppBarLayout` API the activity used, so call sites only changed their type:
 * [setExpanded], [getTop] (0 … -range), [getHeight] (the range), the offset listener list [offsetListeners], and the scroll switch
 * [enabled] (was the child's scroll flags 21 / 0).
 */
@OptIn(ExperimentalMaterial3Api::class)
class BarScroll {
    /** Old `AppBarLayout.OnOffsetChangedListener`: called with the offset (0 = bars shown, -range = hidden). */
    fun interface OffsetListener {
        fun onOffsetChanged(offset: Int)
    }

    @JvmField val offsetListeners = ArrayList<OffsetListener>()

    /** How far the bars travel (px); was the `AppBarLayout` height. */
    private var rangeState by mutableIntStateOf(0)

    /** Bars follow the page scroll; false keeps them where they are (old scroll flags 0). */
    @JvmField var enabled = true

    private var behavior: TopAppBarScrollBehavior? = null
    private var scope: CoroutineScope? = null
    private var expandJob: Job? = null
    private var lastPublished = Int.MIN_VALUE
    private var pendingExpanded: Boolean? = null

    fun getTop(): Int = behavior?.state?.heightOffset?.roundToInt() ?: 0

    fun getHeight(): Int = rangeState

    /** Was `appBarLayout.layoutParams.height = px; requestLayout()`. */
    fun setRange(px: Int) {
        if (px == rangeState) return
        rangeState = px
        val state = behavior?.state ?: return
        state.heightOffsetLimit = -px.toFloat()
        publish()
    }

    /** Was `AppBarLayout.setExpanded(expanded)` (animated). */
    fun setExpanded(expanded: Boolean) {
        val behaviorNow = behavior
        val scopeNow = scope
        if (behaviorNow == null || scopeNow == null) {
            pendingExpanded = expanded
            return
        }
        val target = if (expanded) 0f else behaviorNow.state.heightOffsetLimit
        if (behaviorNow.state.heightOffset == target) return
        expandJob?.cancel()
        expandJob = scopeNow.launch {
            val anim = Animatable(behaviorNow.state.heightOffset)
            anim.animateTo(target, tween(250)) { behaviorNow.state.heightOffset = value; publish() }
        }
    }

    /** Was `setExpanded(expanded, false)`: jump without animation. */
    fun setExpandedNow(expanded: Boolean) {
        val behaviorNow = behavior
        if (behaviorNow == null) {
            pendingExpanded = expanded
            return
        }
        expandJob?.cancel()
        behaviorNow.state.heightOffset = if (expanded) 0f else behaviorNow.state.heightOffsetLimit
        publish()
    }

    // ---- page scroll (WebNestView), Android sign: dy > 0 = finger moves up, content scrolls down ----

    /** Before the page scrolls by [dy]: the bars take what they need; returns the part consumed. */
    fun preScroll(dy: Int): Int {
        val bar = behavior ?: return 0
        if (!enabled || rangeState == 0 || dy == 0) return 0
        expandJob?.cancel()
        val used = bar.nestedScrollConnection.onPreScroll(Offset(0f, -dy.toFloat()), NestedScrollSource.UserInput)
        publish()
        return -used.y.roundToInt()
    }

    /** After the page scrolled by [dyConsumed] and left [dyUnconsumed]. */
    fun postScroll(dyConsumed: Int, dyUnconsumed: Int) {
        val bar = behavior ?: return
        if (!enabled || rangeState == 0) return
        bar.nestedScrollConnection.onPostScroll(
            Offset(0f, -dyConsumed.toFloat()), Offset(0f, -dyUnconsumed.toFloat()), NestedScrollSource.UserInput,
        )
        publish()
    }

    /** Finger lifted: M3 settles the bars (snaps to shown or hidden). */
    fun stop() {
        val bar = behavior ?: return
        val s = scope ?: return
        if (!enabled || rangeState == 0) return
        val offset = bar.state.heightOffset
        if (offset == 0f || offset == bar.state.heightOffsetLimit) return
        s.launch { bar.nestedScrollConnection.onPostFling(Velocity.Zero, Velocity.Zero) }
    }

    /** Calls the listeners with the current offset (also re-applies it, e.g. after a bar's fixed / hidden flag changed). */
    fun publish(force: Boolean = false) {
        val top = getTop()
        if (!force && top == lastPublished) return
        lastPublished = top
        for (listener in ArrayList(offsetListeners)) {
            try {
                listener.onOffsetChanged(top)
            } catch (_: Exception) {
            }
        }
    }

    /** Creates the M3 scroll behaviour; called once by [BrowserScreen]. */
    @Composable
    internal fun Install() {
        val state = rememberTopAppBarState(initialHeightOffsetLimit = -rangeState.toFloat())
        val enterAlwaysScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state, canScroll = { enabled })
        val effectScope = rememberCoroutineScope()
        DisposableEffect(enterAlwaysScrollBehavior, effectScope) {
            behavior = enterAlwaysScrollBehavior
            scope = effectScope
            pendingExpanded?.let { pendingExpanded = null; setExpandedNow(it) }
            onDispose {
                if (behavior === enterAlwaysScrollBehavior) behavior = null
                if (scope === effectScope) scope = null
            }
        }
        LaunchedEffect(rangeState) { state.heightOffsetLimit = -rangeState.toFloat() }
        // Settling animations (snap) change the offset outside the calls above.
        LaunchedEffect(state) { snapshotFlow { state.heightOffset }.collect { publish() } }
    }
}
