package com.mycompany.app.view

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout

/**
 * A LinearLayout that fades in and out (logic in [FadeHelper], shared since 2026-11-17). Calls as before: [show] show
 * (animated), [hide] hide (`forceHide` ignores blocking), [isShowingNow] shown or showing, [release] release.
 */
open class MyFadeLinear(context: Context?) : LinearLayout(context) {
    interface MyVisibleListener {
        fun onVisible(visible: Boolean)
    }

    private val fade = FadeHelper(this, { super.setVisibility(it) }, timerOnOnlyVisibility = false)
    private var visibleListener: MyVisibleListener? = null

    fun setOnlyVisibility(visibility: Int) = fade.setOnlyVisibility(visibility)
    fun hide(animated: Boolean, forceHide: Boolean) = fade.hide(animated, force = forceHide)
    fun isShowingNow(): Boolean = fade.isShownOrShowing()
    open fun release() {
        fade.release()
        visibleListener = null
    }
    fun show() = fade.show(true)

    fun setAnimTime(animTime: Int) { fade.animTime = animTime }
    fun setAutoHide(autoHide: Boolean) = fade.setAutoHideOn(autoHide)
    fun setBlocking(blocking: Boolean) { fade.blocking = blocking }
    fun setInvisible(invisible: Boolean) { fade.invisible = invisible }
    fun setListener(listener: MyFadeListener?) { fade.listener = listener }
    fun setShowTime(showTime: Int) { fade.showTime = showTime }
    fun setTouchable(touchable: Boolean) { fade.touchable = touchable }
    fun setVisibleListener(newVisibleListener: MyVisibleListener?) { visibleListener = newVisibleListener }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        fade.onTouch(event)
        return super.dispatchTouchEvent(event)
    }

    override fun setVisibility(visibility: Int) {
        fade.beforeSetVisibility(visibility)
        super.setVisibility(visibility)
    }

    override fun invalidate() {
        if (fade.alive) super.invalidate()
    }

    override fun onDetachedFromWindow() {
        fade.onDetached()
        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        visibleListener?.onVisible(visibility == View.VISIBLE)
    }
}
