package com.mycompany.app.ui.components

import android.content.Context
import com.mycompany.app.ui.state.AppState
import android.view.MotionEvent
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.mycompany.app.main.MainApp
import com.mycompany.app.ui.browser.ComposeOverlay

/**
 * Loading cover over a legacy View screen: an optional back colour with a spinner in the middle. Replaces
 * `view/MyCoverView` everywhere (2026-11-17). Calls as before: [showNow] fade in (600 ms), [showCover] fade in to an alpha after a
 * delay, [hideCover] hide, [isCoverShown] shown or showing, [release] release, `setVisibility` shows or hides at once; [setBackColor] is the
 * cover colour, [setForeSize] the spinner size. While shown it takes the touches (taps reach a click listener, if
 * any); hidden, it lets them through.
 *
 * @param spinColor spinner colour (0 = theme colour), @param stroke spinner line width in px (0 = default),
 * @param size spinner size in px.
 */
class LoadCover(
    context: Context?,
    spinColor: Int = 0,
    stroke: Int = 0,
    size: Int = MainApp.dp24,
) : ComposeOverlay(context!!) {
    private var shown by mutableStateOf(false)
    private var fade by mutableStateOf(false)
    private var target by mutableFloatStateOf(1f)
    private var back by mutableIntStateOf(0)
    private var spinSize by mutableIntStateOf(size)
    private val pending = Runnable { showNow(true) }
    private var alive = true

    init {
        super.setVisibility(View.GONE)
        setPage({ AppState.darkUi }) {
            AnimatedVisibility(
                shown,
                enter = if (fade) fadeIn(tween(600)) else EnterTransition.None,
                exit = ExitTransition.None,
            ) {
                val px = with(LocalDensity.current) { spinSize.toDp() }
                val line = with(LocalDensity.current) { if (stroke > 0) stroke.toDp() else 4.dp }
                Box(
                    Modifier.fillMaxSize().alpha(target).then(if (back != 0) Modifier.background(Color(back)) else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    if (spinColor != 0) {
                        CircularProgressIndicator(Modifier.size(px), color = Color(spinColor), strokeWidth = line)
                    } else {
                        CircularProgressIndicator(Modifier.size(px), strokeWidth = line)
                    }
                }
            }
        }
    }

    /** Fades the cover in, or shows it at once. */
    fun showNow(animate: Boolean) {
        if (!alive) return
        removeCallbacks(pending)
        fade = animate
        super.setVisibility(View.VISIBLE)
        shown = true
    }

    /** Shows the cover with [alpha] after [delay] ms; [animated] false shows it at once. */
    fun showCover(animated: Boolean, alpha: Float, delay: Long) {
        target = alpha
        removeCallbacks(pending)
        if (!animated) {
            showNow(false)
        } else if (delay <= 0) {
            showNow(true)
        } else {
            postDelayed(pending, delay)
        }
    }

    /** Hides the cover; the fading hide was not used, both hide at once. */
    fun hideCover(animate: Boolean) {
        removeCallbacks(pending)
        shown = false
        super.setVisibility(View.GONE)
    }

    /** True while shown or about to show. */
    fun isCoverShown(): Boolean = shown

    /** Release. */
    fun release() {
        alive = false
        removeCallbacks(pending)
        shown = false
    }

    fun setBackColor(color: Int) {
        back = color
    }

    fun setForeSize(size: Int) {
        spinSize = size
    }

    /** Kept for the old call; the cover always takes touches while shown. */
    fun setBlockTouch(block: Boolean) {}

    override fun setVisibility(visibility: Int) {
        removeCallbacks(pending)
        if (visibility == View.VISIBLE) {
            fade = false
            shown = true
        } else {
            shown = false
        }
        super.setVisibility(visibility)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean = shown

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (!shown) return false
        if (isClickable) super.onTouchEvent(event)
        return true
    }
}
