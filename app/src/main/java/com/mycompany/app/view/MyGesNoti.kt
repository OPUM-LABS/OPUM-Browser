package com.mycompany.app.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.ui.browser.ComposeOverlay
import com.mycompany.app.web.WebViewActivity

/**
 * The square notice in the middle of the page while a page gesture is armed (a swipe that will go back / forward,
 * close the tab, …): a translucent card with an arrow that points the gesture's way ([showDirection] direction 1..4: flipped
 * both ways, upside down, mirrored, as drawn). [showDirection] fades it in (400 ms), [fadeOutNow] fades it out. Compose since 2026-11-16.
 */
class MyGesNoti(webViewActivity: WebViewActivity?) : ComposeOverlay(webViewActivity!!) {
    private var direction by mutableIntStateOf(4)
    private var fadeIn: ValueAnimator? = null
    private var fadeOut: ValueAnimator? = null
    /** A fade-out was asked while fading in. */
    private var hidePending = false

    init {
        setPage({ true }) {
            val corner = with(LocalDensity.current) { MainApp.dp16.toDp() }
            val iconSize = with(LocalDensity.current) { MainApp.dp48.toDp() }
            Box(Modifier.fillMaxSize().background(Color(0xE1A6A9EA.toInt()), RoundedCornerShape(corner)), contentAlignment = Alignment.Center) {
                val (sx, sy) = when (direction) {
                    1 -> -1f to -1f
                    2 -> 1f to -1f
                    3 -> -1f to 1f
                    else -> 1f to 1f
                }
                Icon(
                    painterResource(R.drawable.outline_subdirectory_arrow_right_white_24), contentDescription = null,
                    tint = Color.Unspecified, modifier = Modifier.size(iconSize).scale(sx, sy),
                )
            }
        }
    }

    /** Fades the notice out (now, or once the fade-in ends). */
    fun fadeOutNow() {
        hidePending = false
        if (fadeOut != null || visibility != VISIBLE) return
        val from = alpha
        val anim = ValueAnimator.ofFloat(from, 0f).setDuration((from * 400f).toLong())
        fadeOut = anim
        anim.addUpdateListener { if (fadeOut === anim) alpha = it.animatedValue as Float }
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                if (fadeOut === anim) fadeOut = null
            }

            override fun onAnimationEnd(animation: Animator) {
                if (fadeOut !== anim) return
                fadeOut = null
                visibility = GONE
            }
        })
        fadeIn?.let { fadeIn = null; it.cancel() }
        anim.start()
    }

    /** Fades it out shortly (0.4 s, or 0.8 s while still fading in). */
    fun hideLater() {
        hidePending = true
        postDelayed({ if (hidePending) fadeOutNow() }, if (fadeIn == null) 400L else 800L)
    }

    /** Shows the notice for gesture [newDirection] (its arrow direction), fading in. */
    fun showDirection(newDirection: Int) {
        hidePending = false
        direction = newDirection
        if (fadeIn != null) return
        if (visibility == VISIBLE && fadeOut == null) return
        val from = if (visibility == VISIBLE) alpha else 0f
        if (from >= 1f) return
        val anim = ValueAnimator.ofFloat(from, 1f).setDuration(((1f - from) * 400f).toLong())
        fadeIn = anim
        anim.addUpdateListener {
            if (fadeIn !== anim) return@addUpdateListener
            alpha = it.animatedValue as Float
            if (visibility != VISIBLE) visibility = VISIBLE
        }
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                if (fadeIn !== anim) return
                fadeIn = null
                if (hidePending) fadeOutNow()
            }

            override fun onAnimationEnd(animation: Animator) {
                if (fadeIn !== anim) return
                fadeIn = null
                alpha = 1f
                if (visibility != VISIBLE) visibility = VISIBLE
                if (hidePending) fadeOutNow()
            }
        })
        fadeOut?.let { fadeOut = null; it.cancel() }
        anim.start()
    }

    /** Stops the fades (the activity is dropping the notice). */
    fun release() {
        hidePending = false
        fadeIn?.let { fadeIn = null; it.cancel() }
        fadeOut?.let { fadeOut = null; it.cancel() }
    }

    override fun onDetachedFromWindow() {
        fadeIn?.let { fadeIn = null; it.cancel() }
        fadeOut?.let { fadeOut = null; it.cancel() }
        super.onDetachedFromWindow()
    }
}
