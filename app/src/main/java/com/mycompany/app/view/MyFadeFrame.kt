package com.mycompany.app.view

import android.content.Context
import android.view.MotionEvent
import android.widget.FrameLayout
import com.mycompany.app.image.ImageSizeListener

/**
 * A FrameLayout that fades in and out (logic in [FadeHelper], shared with the other fading containers since
 * 2026-11-17). Calls as before: [h] show, [hide] hide, [toggle] toggle, [isShowingNow] shown or showing, [restartTimer] restart the auto-hide timer,
 * [release] release.
 */
open class MyFadeFrame(context: Context?) : FrameLayout(context!!) {
    private val fade = FadeHelper(this, { super.setVisibility(it) })
    private var sizeListener: ImageSizeListener? = null

    fun setOnlyVisibility(visibility: Int) = fade.setOnlyVisibility(visibility)
    fun hide(animated: Boolean) = fade.hide(animated)
    fun isShowingNow(): Boolean = fade.isShownOrShowing()
    open fun release() {
        fade.release()
        sizeListener = null
    }
    fun restartTimer() = fade.restartTimer()
    fun show(animated: Boolean) = fade.show(animated)
    fun toggle(showFlag: Boolean) = fade.toggle(showFlag)

    fun setAnimTime(animTime: Int) { fade.animTime = animTime }
    fun setAutoHide(autoHide: Boolean) = fade.setAutoHideOn(autoHide)
    fun setBlocking(blocking: Boolean) { fade.blocking = blocking }
    fun setInvisible(invisible: Boolean) { fade.invisible = invisible }
    fun setListener(listener: MyFadeListener?) { fade.listener = listener }
    fun setShowTime(showTime: Int) { fade.showTime = showTime }
    fun setSizeListener(newSizeListener: ImageSizeListener?) { sizeListener = newSizeListener }
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        sizeListener?.onSize(this, w, h)
    }
}
