package com.mycompany.app.wview

import android.content.Context
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefZtri

/**
 * The floating video button (videos found on the page; on YouTube its YouTube actions), with its colour, icon and
 * position settings (one button since 2026-11-26: YouTube had a variant of its own). Tap, move, fade and auto-hide come from [FloatButton]; on top of it
 * [pulse] flashes the press circle once to draw the eye, and [setHideBlocked] keeps it shown.
 */
class WebDownView(context: Context) : FloatButton(context) {
    /** [setHideBlocked] asked to keep it shown (not just a pulse). */
    private var keepShown = false
    /** A [pulse] is running. */
    private var pulsing = false
    private var iconRes = 0

    init {
        iconRes = MainUtil.downMenuCount(0, true)
        if (clearOverLight()) iconAlpha = 176
        setIconRes(iconRes)
    }

    fun setListener(fltViewListener: WebFltView.FltViewListener?) = setFltListener(fltViewListener)

    private fun colorPos() = EditorSettings.iconPos.value
    private fun clearOverLight() = colorPos() > 0.9 && AppState.darkWeb

    override fun readPos() = floatArrayOf(ZtriSettings.downLtX.value, ZtriSettings.downRtX.value, ZtriSettings.downUpY.value, ZtriSettings.downDnY.value)

    override fun writePos(left: Float, right: Float, top: Float, bottom: Float) {
        ZtriSettings.editLocal { set(ZtriSettings.downLtX, left) }; ZtriSettings.editLocal { set(ZtriSettings.downRtX, right) }; ZtriSettings.editLocal { set(ZtriSettings.downUpY, top) }; ZtriSettings.editLocal { set(ZtriSettings.downDnY, bottom) }
    }

    override fun savePos() {
        PrefZtri.saveDownPos(context)
    }

    override fun defaultPos(areaWidth: Int, areaHeight: Int) =
        intArrayOf(areaWidth - size, (((areaHeight - size) - MainUtil.bottomInsetTotal()) - MainApp.dp16) - MainApp.dp4)

    /** It stays while the page has a video (2026-11-26: it faded out after 2 s, too fast to reach); scrolling still hides it. */
    override val autoHides: Boolean get() = false

    override fun hide(fade: Boolean) {
        hideBlocked = false
        super.hide(fade)
    }

    /** Re-reads the colour, outline and icon transparency settings. */
    fun refreshColors() {
        val color = PrefEditor.iconArgb
        var changed = false
        if (bgColor != color) { bgColor = color; changed = true }
        val ring = if (colorPos() > 0.9) -1066044043 else 0
        if (ringColor != ring) { ringColor = ring; changed = true }
        if (icon != null && setIconAlpha(if (clearOverLight()) 176 else 255)) changed = true
        if (changed) invalidate()
    }

    /** Re-reads the chosen icon. */
    fun refreshIcon() {
        val res = MainUtil.downMenuCount(0, true)
        if (iconRes == res) return
        iconRes = res
        iconAlpha = if (clearOverLight()) 176 else 255
        setIconRes(res)
        invalidate()
    }

    /** Shows the button and restarts the auto-hide. */
    fun showNow() = show(false)

    /** Shows the button and flashes the press circle once; it may hide again 2 s later. */
    fun pulse() {
        if (keepShown) {
            if (pulsing) {
                pulsing = false
                pressUp()
            }
            return
        }
        hideBlocked = true
        cancelAutoHide()
        visibility = VISIBLE
        if (pulsing) return
        pulsing = true
        pressDown()
        postDelayed({
            if (pulsing) {
                pulsing = false
                pressUp()
            }
            if (!keepShown) {
                hideBlocked = false
                scheduleHide()
            }
        }, 2000L)
    }

    override fun onPressGrown() {
        if (pulsing) {
            pulsing = false
            pressUp()
        }
    }

    override fun setHideBlocked(blocked: Boolean) {
        if (hideBlocked != blocked) keepShown = blocked
        super.setHideBlocked(blocked)
    }
}
