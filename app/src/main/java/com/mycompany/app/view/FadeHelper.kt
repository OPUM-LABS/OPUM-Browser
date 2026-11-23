package com.mycompany.app.view

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.MotionEvent
import android.view.View
import com.mycompany.app.main.MainApp
import java.lang.ref.WeakReference

/**
 * The fade logic shared by the fading containers (`MyFadeFrame`, `MyFadeRelative`, `MyFadeLinear`, `MyFadeView`),
 * which were four copies of the same code on different base views until 2026-11-17. The host keeps its old public
 * calls and forwards them here.
 *
 * - [show] fades the host in (alpha up to 1 over [animTime] × the missing alpha), [hide] fades it out and then sets it
 *   GONE (or INVISIBLE with [invisible]); with `animated = false` both act at once.
 * - With [autoHide], a shown host hides itself after [showTime] ms; a touch ([onTouch], when [touchable]) holds the
 *   timer until the finger lifts.
 * - [blocking]: a running show is not interrupted by a hide (and the other way round while auto-hiding).
 * - [immediate] (the scroll bar's flag): show / hide never animate.
 * - [listener] is told when a fade starts or the visibility changes, and when a fade
 *   ends. Host alpha and visibility are the host view's own.
 */
class FadeHelper(
    private val host: View,
    /** Sets the host's visibility without coming back into the host's own setVisibility override. */
    private val rawSetVisibility: (Int) -> Unit,
    /** `MyFadeLinear` never created the timer in setOnlyVisibility; kept as it was. */
    private val timerOnOnlyVisibility: Boolean = true,
) {
    @JvmField var alive = true
    @JvmField var animTime = 400
    @JvmField var showTime = 3000
    @JvmField var touchable = false
    @JvmField var autoHide = false
    @JvmField var blocking = false
    @JvmField var invisible = false
    @JvmField var immediate = false
    @JvmField var listener: MyFadeListener? = null

    private var showAnim: ValueAnimator? = null
    private var hideAnim: ValueAnimator? = null
    private var timer: EventHandler? = null
    private var held = false

    private class EventHandler(helper: FadeHelper) : Handler(Looper.getMainLooper()) {
        private val ref = WeakReference(helper)

        override fun handleMessage(message: Message) {
            val helper = ref.get() ?: return
            if (message.what != 0 || !helper.autoHide || helper.held) return
            helper.hide(true)
        }
    }

    private fun startTimer() {
        val t = timer ?: return
        t.removeMessages(0)
        if (autoHide && !held) t.sendEmptyMessageDelayed(0, showTime.toLong())
    }

    /** Old `setOnlyVisibility`: visibility without the side effects of the host's setVisibility. */
    fun setOnlyVisibility(visibility: Int) {
        if (timerOnOnlyVisibility && timer == null && autoHide) timer = EventHandler(this)
        rawSetVisibility(visibility)
    }

    private fun stepShow(alpha: Float) {
        host.alpha = alpha
        if (host.visibility != View.VISIBLE) setOnlyVisibility(View.VISIBLE)
    }

    /** Fades the host in ([animated]) or shows it at once; restarts the auto-hide timer. */
    fun show(animated: Boolean) {
        timer?.removeMessages(0)
        if (immediate || !animated) {
            host.visibility = View.VISIBLE
            val timerNow = timer
            if (timerNow == null || !autoHide || held) return
            timerNow.sendEmptyMessageDelayed(0, showTime.toLong())
            return
        }
        if (showAnim != null) return
        val running = hideAnim
        if (running != null) {
            if (autoHide && blocking) return
            running.cancel()
            hideAnim = null
        } else if (host.visibility == View.VISIBLE) {
            startTimer()
            if (timer == null) return
            return
        }
        val from = if (host.visibility == View.VISIBLE) host.alpha.also { if (it >= 1.0f) return } else 0.0f
        val anim = ValueAnimator.ofFloat(from, 1.0f)
        showAnim = anim
        anim.duration = ((1.0f - from) * animTime).toLong()
        var queued = false
        var value = from
        val step = Runnable {
            queued = false
            if (showAnim != null) stepShow(value)
        }
        anim.addUpdateListener { a ->
            if (showAnim == null) return@addUpdateListener
            value = a.animatedValue as Float
            if (queued) return@addUpdateListener
            queued = true
            MainApp.postToMain(host.context, step)
        }
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) { listener?.onFadeStart(true, true) }
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {
                if (showAnim == null) return
                showAnim = null
                host.invalidate()
            }
            override fun onAnimationEnd(animation: Animator) {
                if (showAnim == null) return
                MainApp.postToMain(host.context, Runnable {
                    if (showAnim != null) {
                        showAnim = null
                        stepShow(1.0f)
                        listener?.onFadeEnd(true)
                        startTimer()
                    }
                })
            }
        })
        anim.start()
    }

    /**
     * Fades the host out ([animated]) or hides it at once. [force] ignores [blocking].
     */
    fun hide(animated: Boolean, force: Boolean = false) {
        timer?.removeMessages(0)
        if (immediate || !animated) {
            host.visibility = if (invisible) View.INVISIBLE else View.GONE
            return
        }
        if (hideAnim != null) return
        val running = showAnim
        if (running != null) {
            if (blocking && !force) return
            running.cancel()
            showAnim = null
        }
        if (host.visibility != View.VISIBLE) return
        held = false
        val from = host.alpha
        val anim = ValueAnimator.ofFloat(from, 0.0f)
        hideAnim = anim
        anim.duration = (from * animTime).toLong()
        var queued = false
        var value = from
        val step = Runnable {
            queued = false
            if (hideAnim != null) host.alpha = value
        }
        anim.addUpdateListener { a ->
            if (hideAnim == null) return@addUpdateListener
            value = a.animatedValue as Float
            if (queued) return@addUpdateListener
            queued = true
            MainApp.postToMain(host.context, step)
        }
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) { listener?.onFadeStart(false, true) }
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {
                if (hideAnim == null) return
                hideAnim = null
                host.invalidate()
            }
            override fun onAnimationEnd(animation: Animator) {
                if (hideAnim == null) return
                MainApp.postToMain(host.context, Runnable {
                    if (hideAnim != null) {
                        hideAnim = null
                        setOnlyVisibility(if (invisible) View.INVISIBLE else View.GONE)
                        listener?.onFadeEnd(false)
                    }
                })
            }
        })
        anim.start()
    }

    /** Shows ([showFlag]) or hides, both animated. */
    fun toggle(showFlag: Boolean) {
        if (showFlag) show(true) else hide(true)
    }

    /** Shown or fading in. */
    fun isShownOrShowing(): Boolean {
        if (hideAnim != null) return false
        return showAnim != null || host.visibility == View.VISIBLE
    }

    /** Lets go of a held timer and starts it again. */
    fun restartTimer() {
        held = false
        startTimer()
    }

    fun setAutoHideOn(newAutoHide: Boolean) {
        if (autoHide == newAutoHide) return
        autoHide = newAutoHide
        if (!newAutoHide) {
            timer?.removeMessages(0)
            timer = null
            host.visibility = View.VISIBLE
            return
        }
        if (timer == null) timer = EventHandler(this)
        if (showAnim != null || (hideAnim == null && host.visibility == View.VISIBLE)) {
            timer!!.removeMessages(0)
            timer!!.sendEmptyMessageDelayed(0, showTime.toLong())
        }
    }

    /** Touch hook (call from the host's dispatchTouchEvent before super): holds the timer while a finger is down. */
    fun onTouch(ev: MotionEvent?) {
        if (!touchable || ev == null) return
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                held = true
                timer?.removeMessages(0)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> restartTimer()
        }
    }

    /** setVisibility hook (call from the host's override, before super): stops fades and resets alpha. */
    fun beforeSetVisibility(visibility: Int) {
        showAnim?.cancel()
        showAnim = null
        hideAnim?.cancel()
        hideAnim = null
        if (timer != null) timer!!.removeMessages(0) else if (autoHide) timer = EventHandler(this)
        host.alpha = 1.0f
        if (host.visibility != visibility) {
            held = false
            listener?.onFadeStart(visibility == View.VISIBLE, false)
        }
    }

    fun onDetached() {
        showAnim?.cancel()
        showAnim = null
        hideAnim?.cancel()
        hideAnim = null
    }

    /** Release. */
    fun release() {
        alive = false
        onDetached()
        listener = null
        timer?.removeMessages(0)
        timer = null
    }
}
