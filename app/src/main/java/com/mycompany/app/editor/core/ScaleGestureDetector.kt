package com.mycompany.app.editor.core

import android.view.MotionEvent
import android.view.View

@Suppress("deprecation")
class ScaleGestureDetector(@JvmField var listener: OnScaleGestureListener?) {
    @JvmField
    var gestureInProgress: Boolean = false
    @JvmField
    var prevEvent: MotionEvent? = null
    @JvmField
    var currEvent: MotionEvent? = null
    @JvmField
    val currSpanVector: Vector2D = Vector2D()
    @JvmField
    var focusX: Float = 0f
    @JvmField
    var focusY: Float = 0f
    @JvmField
    var prevFingerDiffX: Float = 0f
    @JvmField
    var prevFingerDiffY: Float = 0f
    @JvmField
    var currFingerDiffX: Float = 0f
    @JvmField
    var currFingerDiffY: Float = 0f
    @JvmField
    var currLen: Float = 0f
    @JvmField
    var prevLen: Float = 0f
    @JvmField
    var scaleFactor: Float = 0f
    @JvmField
    var currPressure: Float = 0f
    @JvmField
    var prevPressure: Float = 0f
    @JvmField
    var invalidGesture: Boolean = false
    @JvmField
    var activeId0: Int = 0
    @JvmField
    var activeId1: Int = 0
    @JvmField
    var active0MostRecent: Boolean = false

    interface OnScaleGestureListener

    open class SimpleOnScaleGestureListener : OnScaleGestureListener

    fun reset() {
        val motionEvent = this.prevEvent
        if (motionEvent != null) {
            motionEvent.recycle()
            this.prevEvent = null
        }
        val currEvent = this.currEvent
        if (currEvent != null) {
            currEvent.recycle()
            this.currEvent = null
        }
        this.gestureInProgress = false
        this.activeId0 = -1
        this.activeId1 = -1
        this.invalidGesture = false
    }

    fun setContext(view: View?, motionEvent: MotionEvent) {
        val currEvent = this.currEvent
        if (currEvent != null) {
            currEvent.recycle()
        }
        this.currEvent = MotionEvent.obtain(motionEvent)
        this.currLen = -1.0f
        this.prevLen = -1.0f
        this.scaleFactor = -1.0f
        val vector2D = this.currSpanVector
        vector2D.set(0.0f, 0.0f)
        val prevEvent = this.prevEvent
        val prevIndex = prevEvent!!.findPointerIndex(this.activeId0)
        val findPointerIndex = prevEvent.findPointerIndex(this.activeId1)
        val currIndex = motionEvent.findPointerIndex(this.activeId0)
        val currIndex1 = motionEvent.findPointerIndex(this.activeId1)
        if (prevIndex < 0 || findPointerIndex < 0 || currIndex < 0 || currIndex1 < 0) {
            this.invalidGesture = true
            return
        }
        val x = prevEvent.getX(prevIndex)
        val y = prevEvent.getY(prevIndex)
        val prevFingerDiffX = prevEvent.getX(findPointerIndex)
        val prevFingerDiffY = prevEvent.getY(findPointerIndex)
        val x3 = motionEvent.getX(currIndex)
        val y3 = motionEvent.getY(currIndex)
        val currFingerDiffX = motionEvent.getX(currIndex1) - x3
        val currFingerDiffY = motionEvent.getY(currIndex1) - y3
        vector2D.set(currFingerDiffX, currFingerDiffY)
        this.prevFingerDiffX = prevFingerDiffX - x
        this.prevFingerDiffY = prevFingerDiffY - y
        this.currFingerDiffX = currFingerDiffX
        this.currFingerDiffY = currFingerDiffY
        this.focusX = (currFingerDiffX * 0.5f) + x3
        this.focusY = (currFingerDiffY * 0.5f) + y3
        motionEvent.getEventTime()
        prevEvent.getEventTime()
        this.currPressure =
            motionEvent.getPressure(currIndex1) + motionEvent.getPressure(currIndex)
        this.prevPressure = prevEvent.getPressure(findPointerIndex) + prevEvent.getPressure(
            prevIndex
        )
    }

    companion object {
        @JvmStatic
        fun findNewActiveIndex(activeId: Int, actionIndex: Int, motionEvent: MotionEvent): Int {
            val pointerCount = motionEvent.getPointerCount()
            val activeIndex = motionEvent.findPointerIndex(activeId)
            for (index in 0..<pointerCount) {
                if (index != actionIndex && index != activeIndex) {
                    return index
                }
            }
            return -1
        }
    }
}
