package com.mycompany.app.wview

import android.content.Context
import com.mycompany.app.pref.store.EditorSettings
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefEditor

/**
 * Floating "scroll to top" button (Kotlin since 2026-11-16, look drawn by [FloatFaceView]):
 * - a tap calls [UpViewListener.onTap] (scroll up); a long press, when [longPressEnabled], hides it and calls
 *   [UpViewListener.onHold] (the "move buttons" hint);
 * - it hides itself 2 s after the last touch ([scheduleHide]); sliding the finger off the button cancels the tap;
 * - [touching] is true while a finger is down (the activity keeps page gestures away then).
 */
class WebUpView(context: Context) : FloatFaceView(context) {
    interface UpViewListener {
        fun onTap()
        fun onHold()
    }

    @JvmField var listener: UpViewListener? = null
    /** Long press opens the move hint (the browser); off in the web view dialog. */
    @JvmField var longPressEnabled = false
    /** A finger is down on the button. */
    @JvmField var touching = false

    private var alive = true
    /** The finger is still on the button, so lifting it is a tap. */
    private var tapping = false
    private val center = (MainApp.dp48 / 2).toFloat()

    private var bgColor = 0
    private var pressColor = 0
    private var ringColor = 0
    private var iconRes = 0
    private var icon: Drawable? = null

    private val hideRunnable = Runnable { if (alive && !touching) hide() }

    init {
        setOnClickListener { }
        setOnLongClickListener {
            if (!alive || !touching || !tapping || !longPressEnabled) return@setOnLongClickListener false
            tapping = false
            hide()
            listener?.onHold()
            true
        }
    }

    /** Stops the press feedback (hidden or disabled), restarting the auto-hide timer if anything was going on. */
    fun reset() {
        val busy = touching || tapping || isPressShown
        touching = false
        tapping = false
        pressReset()
        if (busy) {
            invalidate()
            scheduleHide()
        }
    }

    /** Hides the button now. */
    fun hide() {
        removeCallbacks(hideRunnable)
        visibility = GONE
    }

    /** Releases the button for good. */
    fun release() {
        alive = false
        removeCallbacks(hideRunnable)
        listener = null
        icon = null
    }

    /** (Re)starts the 2 s auto-hide, unless a finger is down. */
    fun scheduleHide() {
        removeCallbacks(hideRunnable)
        if (!touching) postDelayed(hideRunnable, 2000L)
    }

    override fun invalidate() {
        if (alive) {
            super.invalidate()
            publishFace()
        }
    }

    override fun fillFace(face: Face) {
        face.visible = alive
        if (!alive) return
        face.cx = center
        face.cy = center
        face.bg = bgColor
        face.bgRadius = MainApp.dp16.toFloat()
        face.press = pressColor
        face.pressRadius = MainApp.dp20.toFloat()
        face.ring = ringColor
        face.ringWidth = MainApp.dp1.toFloat()
        face.icon = icon
        face.loading = false
        face.dot = 0
    }

    private fun letGo() {
        if (tapping) {
            tapping = false
            pressUp()
        }
        scheduleHide()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (!alive) return super.onTouchEvent(ev)
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touching = true
                tapping = true
                pressDown()
                removeCallbacks(hideRunnable)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!touching) {
                    letGo()
                } else if (tapping && !MainUtil.isInsideView(ev.rawX.toInt(), ev.rawY.toInt(), MainApp.dp4, this)) {
                    touching = false
                    letGo()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (ev.actionMasked == MotionEvent.ACTION_UP && tapping) listener?.let { l -> post { l.onTap() } }
                val wasTouching = touching
                touching = false
                letGo()
                if (wasTouching) invalidate()
            }
        }
        return super.onTouchEvent(ev)
    }

    /** Colours for a light or dark page ([dark] goes through `MainUtil.isDarkPage`), with the user's button alpha. */
    fun setBgColors(dark: Boolean) {
        val night = MainUtil.isDarkPage(dark)
        val bg = PrefEditor.withAlpha(if (night) -16777216 else -1, EditorSettings.upAlpha.value)
        val res = if (night) ch.opumlabs.opumbrowser.R.drawable.outline_arrow_upward_dark_24 else ch.opumlabs.opumbrowser.R.drawable.outline_arrow_upward_black_24
        val press = if (night) -12632257 else -2039584
        val ring = if (night) -1066044043 else -2139785867
        var changed = false
        if (bgColor != bg) { bgColor = bg; changed = true }
        if (pressColor != press) { pressColor = press; changed = true }
        if (ringColor != ring) { ringColor = ring; changed = true }
        if (iconRes != res) {
            iconRes = res
            val drawable = MainUtil.loadDrawable(context, res)
            val size = MainApp.dp20
            val inset = (MainApp.dp48 - size) / 2
            drawable?.setBounds(inset, inset, inset + size, inset + size)
            icon = drawable
            changed = true
        }
        if (changed) invalidate()
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (!enabled) reset()
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility != VISIBLE) reset()
    }
}
