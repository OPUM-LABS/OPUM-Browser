package com.mycompany.app.view

import android.content.Context
import android.view.MotionEvent

/**
 * Fading base of the page's fast-scroll bar ([MyScrollBar]): a Compose overlay (it draws with Compose since
 * 2026-11-17) with the shared fade logic of [FadeHelper]. Calls as before: [show] show, [hide] hide, [restartTimer] restart the
 * auto-hide timer; [fadeRelease] releases the fade state.
 */
open class MyFadeView(context: Context?) : com.mycompany.app.ui.browser.ComposeOverlay(context!!) {
    private val fade = FadeHelper(this, { super.setVisibility(it) }, timerOnOnlyVisibility = false)

    /** Show and hide without animation (the scroll bar in the reader / web dialog). */
    fun setImmediate(immediate: Boolean) { fade.immediate = immediate }

    fun setOnlyVisibility(visibility: Int) = fade.setOnlyVisibility(visibility)
    fun hide(animated: Boolean) = fade.hide(animated)
    fun restartTimer() = fade.restartTimer()
    fun show(animated: Boolean) = fade.show(animated)
    fun fadeRelease() = fade.release()

    fun setAnimTime(animTime: Int) { fade.animTime = animTime }
    fun setAutoHide(autoHide: Boolean) = fade.setAutoHideOn(autoHide)
    fun setBlocking(blocking: Boolean) { fade.blocking = blocking }
    fun setInvisible(invisible: Boolean) { fade.invisible = invisible }
    fun setListener(listener: MyFadeListener?) { fade.listener = listener }
    fun setShowTime(showTime: Int) { fade.showTime = showTime }
    fun setTouchable(touchable: Boolean) { fade.touchable = touchable }

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
}
