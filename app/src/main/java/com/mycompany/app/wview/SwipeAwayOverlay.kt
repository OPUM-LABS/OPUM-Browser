package com.mycompany.app.wview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Outline
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import com.mycompany.app.main.MainApp
import com.mycompany.app.ui.browser.ComposeOverlay
import kotlin.math.abs

/**
 * Base of the small rounded pills the browser shows over the page after a long press (the area menu, the copied-link
 * pill, the "move" hint). They used to be three Java views with the same copied touch code; this keeps that behaviour:
 * - [show] places the pill near a point of the page body (centred for -1234 / -1234) and shows it;
 * - a finger can drag it along one axis (sideways or up / down). Let go after less than a button size (a flick), or
 *   fling it, and it slides out and hides; let go after a longer drag and it slides back (animated).
 * - [tap] runs an action once: hides the pill, then runs it on the next frame.
 * [shown] is true while visible, [touching] while a finger is down (the activity reads it to keep page gestures
 * away), [hideSoon] hides it on the next frame.
 */
abstract class SwipeAwayOverlay(context: Context, private val viewWidth: Int, private val viewHeight: Int) : ComposeOverlay(context) {
    /** Visible. */
    @JvmField var shown = false
    /** A finger is down on it. */
    @JvmField var touching = false
    /** A tap or hide is pending. */
    protected var busy = false

    private var padX = 0
    private var padY = 0
    private var areaW = 0
    private var areaH = 0
    private var baseX = 0
    private var baseY = 0

    private var resetAnim: ValueAnimator? = null
    private var swipeAnim: ValueAnimator? = null
    private var downX = 0f
    private var downY = 0f
    /** 0 = not swiping, 1 = vertical, 2 = horizontal. */
    private var axis = 0
    private var shift = 0f

    protected val swiping get() = axis != 0

    private val gestures = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onFling(down: MotionEvent?, move: MotionEvent, vx: Float, vy: Float): Boolean {
            if (axis == 0) return false
            val v = if (abs(vx) > abs(vy)) {
                if (axis != 2) return false
                vx
            } else {
                if (axis != 1) return false
                vy
            }
            if (v > 100f) {
                if (shift > MainApp.dp16) slideOut()
            } else if (v < -100f && shift < -MainApp.dp16) {
                slideOut()
            }
            return false
        }
    })

    init {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                if (view == null || outline == null) return
                outline.setRoundRect(0, 0, view.width, view.height, MainApp.dp16.toFloat())
            }
        }
        clipToOutline = true
        // taps that miss the content must not reach the page
        setOnClickListener { }
    }

    fun getViewWidth(): Int = viewWidth
    fun getViewHeight(): Int = viewHeight

    /** Where the pill's top-left goes for a point at ([px], [py]) inside the body (only for real points, not -1234). */
    protected open fun anchorX(px: Int): Int = px - viewWidth / 2
    protected open fun anchorY(py: Int): Int = if (py > (viewHeight shl 1)) (py - (viewHeight shl 1)) - MainApp.dp4 else py + (viewHeight / 2)

    /** Called by [show] before placing, to pick up a changed theme or text. */
    protected open fun refresh() {}

    override fun setVisibility(visibility: Int) {
        shown = visibility == VISIBLE
        super.setVisibility(visibility)
    }

    /** Places the pill in [view] (the page body) near ([moveHintX], [moveHintY]); -1234 / -1234 centres it. Clamped to the body. */
    fun place(view: View?, moveHintX: Int, moveHintY: Int) {
        if (view == null) return
        padX = view.paddingLeft
        padY = view.paddingTop
        areaW = view.width - padX - view.paddingRight
        areaH = view.height - padY - view.paddingBottom
        var centerX: Int
        var centerY: Int
        if (moveHintX == -1234 && moveHintY == -1234) {
            centerX = (areaW - viewWidth) / 2
            centerY = (areaH - viewHeight) / 2
        } else {
            centerX = anchorX(moveHintX - padX)
            centerY = anchorY(moveHintY - padY)
        }
        if (centerX + viewWidth > areaW) centerX = areaW - viewWidth
        if (centerY + viewHeight > areaH) centerY = areaH - viewHeight
        if (centerX < 0) centerX = 0
        if (centerY < 0) centerY = 0
        baseX = centerX
        baseY = centerY
        setX((padX + centerX).toFloat())
        setY((padY + centerY).toFloat())
    }

    /** Shows the pill fresh near ([moveHintX], [moveHintY]) in [view], unless it is already showing. */
    fun show(view: View?, moveHintX: Int, moveHintY: Int) {
        if (shown) return
        busy = false
        resetAnim?.let { resetAnim = null; it.cancel() }
        swipeAnim?.let { swipeAnim = null; it.cancel() }
        axis = 0
        refresh()
        alpha = 1.0f
        place(view, moveHintX, moveHintY)
        visibility = VISIBLE
    }

    /** Hides the pill on the next frame (once). */
    fun hideSoon() {
        if (!shown || busy) return
        busy = true
        post {
            visibility = GONE
            busy = false
        }
    }

    /** Hides the pill and runs [action] once, on the next frame; ignored while swiping. */
    protected fun tap(action: (() -> Unit)?) {
        if (axis != 0 || busy) return
        busy = true
        post {
            visibility = GONE
            action?.invoke()
            busy = false
        }
    }

    private fun moveBy(animatedValue: Float) {
        if (axis == 2) setX(padX + baseX + animatedValue) else if (axis == 1) setY(padY + baseY + animatedValue)
    }

    private fun settle() {
        resetAnim = null
        if (axis == 2) setX((padX + baseX).toFloat()) else if (axis == 1) setY((padY + baseY).toFloat())
        axis = 0
    }

    private fun slideBack() {
        val from = shift
        if (from == 0f) {
            settle()
            return
        }
        val anim = ValueAnimator.ofFloat(from, 0f).setDuration(180L)
        resetAnim = anim
        anim.addUpdateListener { if (resetAnim === anim) moveBy(it.animatedValue as Float) }
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                if (resetAnim !== anim) return
                resetAnim = null
                axis = 0
            }

            override fun onAnimationEnd(animation: Animator) {
                if (resetAnim === anim) settle()
            }
        })
        anim.start()
    }

    private fun slideOut() {
        if (axis == 0 || swipeAnim != null) return
        val from = shift
        val base = if (axis == 2) baseX else baseY
        val limit = if (axis == 2) areaW else areaH
        val step = MainApp.dp64
        val to: Float
        if (from < 0f) {
            val far = base + if (axis == 2) viewWidth else viewHeight
            if (far + from <= 0f) return hideNow()
            to = if (far + (from - step) <= 0f) -far.toFloat() else from - step
        } else {
            if (base + from >= limit) return hideNow()
            val next = step + from
            to = if (base + next >= limit) (limit - base).toFloat() else next
        }
        val ms = ((abs(to - from) / step) * 180f).toLong()
        if (ms <= 0) return hideNow()
        val anim = ValueAnimator.ofFloat(from, to).setDuration(ms)
        swipeAnim = anim
        anim.addUpdateListener {
            if (swipeAnim !== anim || axis == 0) return@addUpdateListener
            val animatedValue = it.animatedValue as Float
            moveBy(animatedValue)
            alpha = 1.0f - abs(animatedValue - shift) / step
        }
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                if (swipeAnim !== anim) return
                swipeAnim = null
                axis = 0
            }

            override fun onAnimationEnd(animation: Animator) {
                if (swipeAnim === anim) hideNow()
            }
        })
        anim.start()
    }

    private fun hideNow() {
        swipeAnim = null
        axis = 0
        visibility = GONE
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (resetAnim != null || swipeAnim != null) {
            touching = false
            return super.dispatchTouchEvent(ev)
        }
        gestures.onTouchEvent(ev)
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touching = true
                downX = ev.rawX
                downY = ev.rawY
                axis = 0
                shift = 0f
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touching = false
                if (axis != 0) {
                    if (abs(shift) < MainApp.dp48) slideOut() else if (swipeAnim == null) slideBack()
                    return true
                }
            }
            MotionEvent.ACTION_MOVE -> if (touching) {
                val dx = ev.rawX - downX
                val dy = ev.rawY - downY
                when (axis) {
                    0 -> {
                        val horizontal = abs(dx) > abs(dy)
                        if ((if (horizontal) abs(dx) else abs(dy)) >= MainApp.dp4) {
                            axis = if (horizontal) 2 else 1
                            shift = if (horizontal) dx else dy
                            moveBy(shift)
                            // the finger is moving the pill now: cancel the press under it
                            val cancel = MotionEvent.obtain(ev)
                            cancel.action = MotionEvent.ACTION_CANCEL
                            super.dispatchTouchEvent(cancel)
                            cancel.recycle()
                        }
                    }
                    2 -> if (abs(dx) >= MainApp.dp4 / 2.0f) {
                        shift = dx
                        moveBy(dx)
                    }
                    1 -> if (abs(dy) >= MainApp.dp4 / 2.0f) {
                        shift = dy
                        moveBy(dy)
                    }
                }
                if (axis != 0) return true
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}
