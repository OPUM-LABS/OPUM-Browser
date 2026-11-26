package com.mycompany.app.wview

import android.animation.Animator
import com.mycompany.app.pref.store.ZtriSettings
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.MotionEvent
import android.view.View
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefZtri
import kotlin.math.roundToInt

/**
 * A movable floating page button: the media button ([WebDownView]), read aloud ([WebTtsView]) and the zoom / lock /
 * translate / RSS / full screen / user buttons ([WebFltView]). The three used to carry the same copied Java code; it
 * lives here now (Kotlin, 2026-11-16) with the look drawn by [FloatFaceView]:
 * - **tap** calls `FltViewListener.a(this, tapCode)` once the finger lifts on the button;
 * - **move**: with "Long press to move" (`ZtriSettings.moveLong.value`) a long press starts moving, otherwise dragging past the touch
 *   slop does; a long press always calls the long-press listener (the move hint) and the hint goes once the button
 *   moved a slop away. The position is kept as left / right / top / bottom distances ([readPos] / [writePos]) so it
 *   keeps its relative place when the page body changes size, and stored with [savePos] when the finger lifts;
 * - **auto-hide** ([autoHides]): fades out 2 s after the last touch unless [isHideBlocked];
 * - [show] / [hide] with an optional 400 ms fade; going away calls [onHidden];
 * - [touching] is true while a finger is down (the activity keeps page gestures away then).
 */
abstract class FloatButton(context: Context) : FloatFaceView(context) {
    /** A finger is down on the button. */
    @JvmField var touching = false
    /** The owner asked to keep it shown (no auto-hide). */
    @JvmField var hideBlocked = false

    protected var owner: WebFltView.FltViewListener? = null
    protected var alive = true
    /** The finger is still on the button, so lifting it is a tap. */
    protected var tapping = false
    /** The button follows the finger. */
    protected var moving = false

    protected val size = MainApp.dp48
    protected val center = (MainApp.dp48 / 2).toFloat()
    private var fingerX = 0
    private var fingerY = 0
    private var grabX = 0
    private var grabY = 0
    private var moveStartX = -1234
    private var moveStartY = -1234
    private var padX = 0
    private var padY = 0
    /** The [placeRound] this button was last placed in; a newer one places it again (dragged places forgotten). */
    private var placedRound = placeRound
    protected var areaW = 0
        private set
    protected var areaH = 0
        private set

    /** Preview (settings dialog): the position is kept here, not stored. */
    protected var inPreview = false
    private var previewX = -1
    private var previewY = -1

    // ---- look ----
    protected var bgColor = 0
    protected var ringColor = 0
    protected var icon: Drawable? = null
    protected var iconAlpha = 255
    private var noti = false
    private var rtl = false
    protected var spinning = false
    protected var loadingAlpha = 255

    private var fadeIn: ValueAnimator? = null
    private var fadeOut: ValueAnimator? = null
    private val exclusion = Rect()

    private val hideRunnable = Runnable { if (alive && !touching && !isHideBlocked()) hide(true) }

    init {
        setOnClickListener { }
        setOnLongClickListener {
            if (!alive || !touching || !tapping || moving) return@setOnLongClickListener false
            if (ZtriSettings.moveLong.value) {
                moving = true
                moveStartX = fingerX
                moveStartY = fingerY
            }
            endTap()
            onMoveStarted()
            owner?.onLongPress(this)
            true
        }
    }

    // ---- what the subclasses decide ----

    /** Stored left, right, top, bottom distances (all negative = never moved). */
    protected abstract fun readPos(): FloatArray
    protected abstract fun writePos(left: Float, right: Float, top: Float, bottom: Float)
    /** Writes the stored distances to the preference file. */
    protected abstract fun savePos()
    /** Where a never-moved button goes in a body of [w] × [h]. */
    protected abstract fun defaultPos(areaWidth: Int, areaHeight: Int): IntArray
    /** Code passed with a tap. */
    protected open val tapCode: Int get() = 0
    /** Fades out on its own 2 s after the last touch. */
    protected open val autoHides: Boolean get() = true
    open fun isHideBlocked(): Boolean = hideBlocked
    /** A finger went down (true) or up (false). */
    protected open fun onFinger(down: Boolean) {}
    /** Moving started (long press or drag): show it fully. */
    protected open fun onMoveStarted() = show(false)
    /** After a finger lifted or the press was reset. */
    protected open fun afterRelease() {}
    /** Extra drawing (read aloud stop square). */
    protected open fun decorate(face: Face) {}

    // ---- show / hide ----

    /** Sets visibility without touching alpha; tells the owner when it goes. */
    protected fun setOnlyVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility != VISIBLE) owner?.onGone()
    }

    /** Hides the button: fading out over up to 400 ms when [fade], else at once. */
    open fun hide(fade: Boolean) {
        removeCallbacks(hideRunnable)
        if (!fade) {
            visibility = GONE
            return
        }
        fadeOutNow()
    }

    protected fun fadeOutNow() {
        if (fadeOut != null) return
        fadeIn?.let { fadeIn = null; it.cancel() }
        if (visibility != VISIBLE) return
        val from = alpha
        if (from <= 0f) {
            setOnlyVisibility(GONE)
            return
        }
        owner?.onGone()
        val anim = ValueAnimator.ofFloat(from, 0f).setDuration((from * 400f).toLong())
        fadeOut = anim
        anim.addUpdateListener { if (fadeOut === anim) alpha = it.animatedValue as Float }
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                if (fadeOut === anim) {
                    fadeOut = null
                    invalidate()
                }
            }

            override fun onAnimationEnd(animation: Animator) {
                if (fadeOut !== anim) return
                fadeOut = null
                setOnlyVisibility(GONE)
            }
        })
        anim.start()
    }

    /** Shows the button (fading in over up to 400 ms when [fade]) and restarts the auto-hide. */
    open fun show(fade: Boolean) {
        scheduleHide()
        if (!fade) {
            visibility = VISIBLE
            return
        }
        fadeInNow()
    }

    protected fun fadeInNow() {
        if (fadeIn != null) return
        val out = fadeOut
        if (out != null) {
            fadeOut = null
            out.cancel()
        } else if (visibility == VISIBLE && alpha >= 1f) {
            return
        }
        val from = if (visibility == VISIBLE) alpha else 0f
        if (from >= 1f) return
        val anim = ValueAnimator.ofFloat(from, 1f).setDuration(((1f - from) * 400f).toLong())
        fadeIn = anim
        anim.addUpdateListener {
            if (fadeIn !== anim) return@addUpdateListener
            alpha = it.animatedValue as Float
            if (visibility != VISIBLE) setOnlyVisibility(VISIBLE)
        }
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator) {
                if (fadeIn === anim) {
                    fadeIn = null
                    invalidate()
                }
            }

            override fun onAnimationEnd(animation: Animator) {
                if (fadeIn !== anim) return
                fadeIn = null
                alpha = 1f
                if (visibility != VISIBLE) setOnlyVisibility(VISIBLE)
            }
        })
        anim.start()
    }

    protected val isFadingOut get() = fadeOut != null
    protected val isFadingIn get() = fadeIn != null

    private fun stopFades() {
        fadeIn?.let { fadeIn = null; it.cancel() }
        fadeOut?.let { fadeOut = null; it.cancel() }
    }

    /** (Re)starts the 2 s auto-hide, unless blocked or a finger is down. */
    protected fun scheduleHide() {
        if (!autoHides) return
        removeCallbacks(hideRunnable)
        if (!isHideBlocked() && !touching) postDelayed(hideRunnable, 2000L)
    }

    protected fun cancelAutoHide() {
        removeCallbacks(hideRunnable)
    }

    /** Keeps the button shown ([blocked]) or lets it auto-hide again. */
    open fun setHideBlocked(blocked: Boolean) {
        if (hideBlocked == blocked) return
        hideBlocked = blocked
        if (blocked) {
            removeCallbacks(hideRunnable)
            visibility = VISIBLE
            return
        }
        if (fadeOut == null && (fadeIn != null || visibility == VISIBLE)) scheduleHide()
    }

    fun setFltListener(fltViewListener: WebFltView.FltViewListener?) {
        owner = fltViewListener
    }

    /** Releases the button for good. */
    open fun release() {
        alive = false
        stopFades()
        removeCallbacks(hideRunnable)
        owner = null
        icon = null
    }

    // ---- press state ----

    /** The finger is no longer a tap: shrink the press circle. */
    protected fun endTap() {
        if (alive && tapping) {
            tapping = false
            pressUp()
        }
    }

    /** Stops the press feedback (hidden or disabled), restarting the auto-hide timer if anything was going on. */
    private fun reset() {
        val busy = touching || tapping || moving || isPressShown
        touching = false
        tapping = false
        moving = false
        pressReset()
        if (busy) {
            invalidate()
            scheduleHide()
        }
        afterRelease()
    }

    // ---- look ----

    /** Sets the icon from a drawable resource, sized and with the current icon alpha. */
    protected fun setIconRes(res: Int): Boolean {
        if (res == 0) return false
        val drawable = MainUtil.loadDrawable(context, res) ?: return false
        if (iconAlpha != 255) drawable.alpha = iconAlpha
        val inset = (MainApp.dp48 - MainApp.dp20) / 2
        drawable.setBounds(inset, inset, inset + MainApp.dp20, inset + MainApp.dp20)
        icon = drawable
        return true
    }

    /** Changes the icon / spinner alpha (176 over light, see-through buttons); true if it changed. */
    protected fun setIconAlpha(newIconAlpha: Int): Boolean {
        var changed = false
        if (iconAlpha != newIconAlpha) {
            iconAlpha = newIconAlpha
            icon?.alpha = newIconAlpha
            changed = true
        }
        if (loadingAlpha != newIconAlpha) {
            loadingAlpha = newIconAlpha
            changed = changed || spinning
        }
        return changed
    }

    /** Shows or hides the red notification dot. */
    fun setNoti(on: Boolean) {
        if (noti == on) return
        noti = on
        if (on) rtl = MainUtil.isRtl(context)
        invalidate()
    }

    override fun invalidate() {
        if (alive) {
            super.invalidate()
            publishFace()
        }
    }

    final override fun fillFace(face: Face) {
        face.visible = alive
        if (!alive) return
        face.cx = center
        face.cy = center
        face.bg = bgColor
        face.bgRadius = MainApp.dp16.toFloat()
        face.press = bgColor
        face.pressRadius = MainApp.dp20.toFloat()
        face.ring = ringColor
        face.ringWidth = MainApp.dp1.toFloat()
        face.icon = if (spinning) null else icon
        face.loading = spinning
        face.loadingSize = MainApp.dp20.toFloat()
        face.loadingColor = withAlpha(-1, loadingAlpha)
        face.stopRect = null
        if (noti) {
            face.dot = 1
            face.dotRtl = rtl
            face.dotX = center
            face.dotY = center
        } else {
            face.dot = 0
        }
        decorate(face)
    }

    // ---- position ----

    /** Moves the button to ([x], [y]) inside the body (clamped) and keeps the position. */
    private fun moveTo(x: Int, touchY: Int) {
        if (!alive) return
        var clampedX = x
        var clampedY = touchY
        if (clampedX + size > areaW) clampedX = areaW - size
        if (clampedY + size > areaH) clampedY = areaH - size
        if (clampedX < 0) clampedX = 0
        if (clampedY < 0) clampedY = 0
        if (inPreview) {
            previewX = clampedX
            previewY = clampedY
        } else {
            writePos(clampedX.toFloat(), (areaW - (size + clampedX)).toFloat(), clampedY.toFloat(), (areaH - (size + clampedY)).toFloat())
        }
        setX((clampedX + padX).toFloat())
        setY((clampedY + padY).toFloat())
    }

    /**
     * Places the button by its stored distances to both edges (kept as a ratio, so it scales with the body).
     * When both distances on an axis are 0 there is no ratio (0 / 0); that axis falls back to [fallback].
     */
    private fun storedX(p: FloatArray, fallback: Int): Int {
        val sum = p[0] + p[1]
        return if (sum > 0f) (((areaW - size) / sum) * p[0]).roundToInt() else fallback
    }

    private fun storedY(p: FloatArray, fallback: Int): Int {
        val sum = p[2] + p[3]
        return if (sum > 0f) (((areaH - size) / sum) * p[2]).roundToInt() else fallback
    }

    /** Re-places the button when the body changed size: kept distances, or the default spot. */
    private fun fitToBody() {
        if (!alive) return
        val view = boundsParent() as? View ?: return
        // the button stays between the browser's top bar and bottom bar, so it can never sit on them (2026-11-19)
        val barTop = com.mycompany.app.main.MainUtil.topBarHeight()
        val barBottom = com.mycompany.app.main.MainUtil.bottomInsetTotal()
        val pl = view.paddingLeft
        val pt = view.paddingTop + barTop
        val newAreaW = view.width - pl - view.paddingRight
        val freeH = view.height - pt - view.paddingBottom - barBottom
        // Mid-relayout (e.g. switching day / night mode) the body can briefly be no bigger than the button. Placing it
        // then would store 0 for both distances and lose the user's spot, so wait for the next real layout.
        if (newAreaW <= size || freeH <= size) return
        val newAreaH = freeH
        if (placedRound == placeRound && pl == padX && pt == padY && newAreaW == areaW && newAreaH == areaH) return
        placedRound = placeRound
        padX = pl
        padY = pt
        areaW = newAreaW
        areaH = newAreaH
        if (inPreview) {
            if (previewX < 0) {
                previewX = (newAreaW - size) / 2
                previewY = (newAreaH - size) / 2
            }
            moveTo(previewX, previewY)
            return
        }
        val before = readPos()
        if (before.all { it < 0f }) {
            val d = defaultPos(newAreaW, newAreaH)
            moveTo(d[0], d[1])
        } else {
            val d = defaultPos(newAreaW, newAreaH)
            moveTo(storedX(before, d[0]), storedY(before, d[1]))
        }
        if (!before.contentEquals(readPos())) savePos()
    }

    /** Preview mode (settings dialog): the button starts centred and its position is not stored. */
    protected fun startPreview(on: Boolean) {
        inPreview = on
        if (on) {
            previewX = -1
            previewY = -1
            removeCallbacks(hideRunnable)
        }
    }

    private fun follow(rawX: Float, rawY: Float) {
        fingerX = rawX.roundToInt()
        fingerY = rawY.roundToInt()
        moveTo(fingerX + grabX, fingerY + grabY)
        if (!(moveStartX == -1234 && moveStartY == -1234) &&
            MainUtil.pointDistance(fingerX.toFloat(), moveStartX.toFloat(), fingerY.toFloat(), moveStartY.toFloat()) >= MainApp.dp4
        ) {
            moveStartX = -1234
            moveStartY = -1234
            owner?.onGone()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        fitToBody()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility == VISIBLE && placedRound != placeRound) post { fitToBody() }
    }

    override fun onDetachedFromWindow() {
        stopFades()
        super.onDetachedFromWindow()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        fitToBody()
        val rect = exclusion
        if (!changed) return
        try {
            rect.set(0, 0, right - left, bottom - top)
            systemGestureExclusionRects = listOf(rect)
        } catch (_: Exception) {
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        fitToBody()
    }

    // ---- touch ----

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (!alive) return super.onTouchEvent(ev)
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touching = true
                tapping = true
                moving = false
                fingerX = ev.rawX.roundToInt()
                fingerY = ev.rawY.roundToInt()
                if (inPreview) {
                    grabX = previewX - fingerX
                    grabY = previewY - fingerY
                } else {
                    val pos = readPos()
                    // fallback: where the button is drawn now
                    grabX = storedX(pos, (x - padX).roundToInt()) - fingerX
                    grabY = storedY(pos, (y - padY).roundToInt()) - fingerY
                }
                pressDown()
                removeCallbacks(hideRunnable)
                onFinger(true)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!touching) {
                    endTap()
                    moving = false
                    scheduleHide()
                } else {
                    val rawX = ev.rawX
                    val rawY = ev.rawY
                    val dist = MainUtil.pointDistance(fingerX.toFloat(), rawX, fingerY.toFloat(), rawY)
                    if (moving) {
                        if (dist >= MainApp.dp4 / 2.0f) follow(rawX, rawY)
                    } else if (!ZtriSettings.moveLong.value) {
                        if (dist >= MainApp.dp4) {
                            moving = true
                            moveStartX = fingerX
                            moveStartY = fingerY
                            post {
                                if (moving) {
                                    endTap()
                                    onMoveStarted()
                                }
                            }
                            follow(rawX, rawY)
                        }
                    } else if (tapping && !MainUtil.isInsideView(rawX.toInt(), rawY.toInt(), MainApp.dp4, this)) {
                        touching = false
                        endTap()
                        moving = false
                        scheduleHide()
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (ev.actionMasked == MotionEvent.ACTION_UP && tapping && !moving) {
                    val code = tapCode
                    owner?.let { l -> post { if (!moving) l.onTap(this, code) } }
                }
                onFinger(false)
                val wasTouching = touching
                if (wasTouching && moving && !inPreview) savePos()
                touching = false
                endTap()
                moving = false
                scheduleHide()
                if (wasTouching) invalidate()
                afterRelease()
            }
        }
        return super.onTouchEvent(ev)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (!enabled) reset()
    }

    override fun setVisibility(visibility: Int) {
        stopFades()
        alpha = 1.0f
        super.setVisibility(visibility)
        if (visibility != VISIBLE) {
            onHidden()
            reset()
            owner?.onGone()
        }
    }

    /** The button went away (before the press reset). */
    protected open fun onHidden() {}

    companion object {
        /** Raised by Settings › Buttons on the page › "Put buttons back in place": every button finds its spot again. */
        @Volatile
        @JvmStatic
        var placeRound = 0
    }
}
