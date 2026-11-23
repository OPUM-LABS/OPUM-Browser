package com.mycompany.app.ui.browser

import android.content.Context
import com.mycompany.app.ui.state.AppState
import android.graphics.Canvas
import android.graphics.Outline
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.RelativeLayout
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil

/**
 * Container of a dialog's web page (DialogWebView, and the hidden translation page of DialogNewsLocale), replacing
 * `view/MyDialogRelative` (2026-11-17): the app's light / dark background, all corners rounded by `MainApp.dp16`, focusable,
 * and the screen-filter colour (MainUtil.editorTextColor) drawn over the content as before. The old loading spinner and
 * border were never used by these hosts. After [release] (release) it ignores touches.
 */
class DialogFrame(context: Context?) : RelativeLayout(context) {
    private var alive = true

    init {
        setBackgroundColor(if (AppState.darkUi) -14606047 else -1)
        val radius = MainApp.dp16.toFloat()
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radius)
            }
        }
        clipToOutline = true
        isFocusable = true
        isFocusableInTouchMode = true
    }

    /** Release. */
    fun release() {
        alive = false
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        val filter = MainUtil.editorTextColor()
        if (filter != 0) canvas.drawColor(filter)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (!alive) return false
        return super.dispatchTouchEvent(ev)
    }
}
