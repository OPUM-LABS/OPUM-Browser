package com.mycompany.app.view

import android.content.Context
import android.view.MotionEvent
import android.widget.FrameLayout

@Suppress("deprecation")
class MyTouchFrame(context: Context?) : FrameLayout(context!!) {
    @JvmField
    var touchActive: Boolean = false

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val handled = super.dispatchTouchEvent(event)
        this.touchActive = handled
        return handled
    }
}
