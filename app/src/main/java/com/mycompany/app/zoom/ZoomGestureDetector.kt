package com.mycompany.app.zoom

import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.VelocityTracker
import android.view.ViewConfiguration
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

abstract class ZoomGestureDetector {
    @JvmField
    var gestureListener: OnGestureListener? = null

    internal class FroyoDetector(context: Context) : ZoomGestureDetector() {
        @JvmField
        var activePointerId: Int = -1
        @JvmField
        var activePointerIndex: Int = 0
        @JvmField
        var scaleDetector: ScaleGestureDetector
        @JvmField
        var lastTouchX: Float = 0f
        @JvmField
        var lastTouchY: Float = 0f
        @JvmField
        var touchSlop: Float
        @JvmField
        var minFlingVelocity: Float
        @JvmField
        var velocityTracker: VelocityTracker? = null
        @JvmField
        var isDragging: Boolean = false

        internal inner class ScaleListener : ScaleGestureDetector.OnScaleGestureListener {
            override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
                this@FroyoDetector.gestureListener!!.onScale(
                    scaleGestureDetector.getScaleFactor(),
                    scaleGestureDetector.getFocusX(),
                    scaleGestureDetector.getFocusY()
                )
                return true
            }
            override fun onScaleBegin(scaleGestureDetector: ScaleGestureDetector): Boolean {
                return true
            }
            override fun onScaleEnd(scaleGestureDetector: ScaleGestureDetector) {
            }
        }

        init {
            val scaleListener = ScaleListener()
            val viewConfiguration = ViewConfiguration.get(context)
            this.minFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity().toFloat()
            this.touchSlop = viewConfiguration.getScaledTouchSlop().toFloat()
            this.scaleDetector = ScaleGestureDetector(context, scaleListener)
        }
        override fun onTouch(motionEvent: MotionEvent) {
            var lastTouchX: Float
            var lastTouchY: Float
            var newLastTouchX: Float
            var newLastTouchY: Float
            var touchX: Float
            var touchY: Float
            this.scaleDetector.onTouchEvent(motionEvent)
            val action = motionEvent.actionMasked
            if (action != 0) {
                if (action == 1) {
                    this.activePointerId = -1
                    if (this.isDragging && this.velocityTracker != null) {
                        try {
                            newLastTouchX = motionEvent.getX(this.activePointerIndex)
                        } catch (unused: Exception) {
                            newLastTouchX = motionEvent.getX()
                        }
                        this.lastTouchX = newLastTouchX
                        try {
                            newLastTouchY = motionEvent.getY(this.activePointerIndex)
                        } catch (unused2: Exception) {
                            newLastTouchY = motionEvent.getY()
                        }
                        this.lastTouchY = newLastTouchY
                        this.velocityTracker!!.addMovement(motionEvent)
                        this.velocityTracker!!.computeCurrentVelocity(1000)
                        val xVelocity = this.velocityTracker!!.getXVelocity()
                        val yVelocity = this.velocityTracker!!.getYVelocity()
                        if (max(abs(xVelocity), abs(yVelocity)) >= this.minFlingVelocity) {
                            this.gestureListener!!.onFling(-xVelocity, -yVelocity)
                        }
                    }
                    val velocityTracker = this.velocityTracker
                    if (velocityTracker != null) {
                        velocityTracker.recycle()
                        this.velocityTracker = null
                    }
                } else if (action == 2) {
                    try {
                        touchX = motionEvent.getX(this.activePointerIndex)
                    } catch (unused3: Exception) {
                        touchX = motionEvent.getX()
                    }
                    try {
                        touchY = motionEvent.getY(this.activePointerIndex)
                    } catch (unused4: Exception) {
                        touchY = motionEvent.getY()
                    }
                    val dx = touchX - this.lastTouchX
                    val dy = touchY - this.lastTouchY
                    var isDragging = this.isDragging
                    if (!isDragging) {
                        val newIsDragging = sqrt(((dy * dy) + (dx * dx)).toDouble()) >= (this.touchSlop.toDouble())
                        this.isDragging = newIsDragging
                        isDragging = newIsDragging
                    }
                    if (isDragging) {
                        this.gestureListener!!.onDrag(dx, dy)
                        this.lastTouchX = touchX
                        this.lastTouchY = touchY
                        val velocityTracker = this.velocityTracker
                        if (velocityTracker != null) {
                            velocityTracker.addMovement(motionEvent)
                        }
                    }
                } else if (action == 3) {
                    this.activePointerId = -1
                    val velocityTracker = this.velocityTracker
                    if (velocityTracker != null) {
                        velocityTracker.recycle()
                        this.velocityTracker = null
                    }
                } else if (action == 6) {
                    val upIndex = (motionEvent.getAction() and 65280) shr 8
                    if (motionEvent.getPointerId(upIndex) == this.activePointerId) {
                        val pointerIndex = if (upIndex != 0) 0 else 1
                        this.activePointerId = motionEvent.getPointerId(pointerIndex)
                        this.lastTouchX = motionEvent.getX(pointerIndex)
                        this.lastTouchY = motionEvent.getY(pointerIndex)
                    }
                }
            } else {
                this.activePointerId = motionEvent.getPointerId(0)
                val velocityTrackerObtain = VelocityTracker.obtain()
                this.velocityTracker = velocityTrackerObtain
                velocityTrackerObtain.addMovement(motionEvent)
                try {
                    lastTouchX = motionEvent.getX(this.activePointerIndex)
                } catch (unused5: Exception) {
                    lastTouchX = motionEvent.getX()
                }
                this.lastTouchX = lastTouchX
                try {
                    lastTouchY = motionEvent.getY(this.activePointerIndex)
                } catch (unused6: Exception) {
                    lastTouchY = motionEvent.getY()
                }
                this.lastTouchY = lastTouchY
                this.isDragging = false
            }
            val activePointerId = this.activePointerId
            this.activePointerIndex = motionEvent.findPointerIndex(if (activePointerId != -1) activePointerId else 0)
        }
    }

    interface OnGestureListener {
        fun onScale(scale: Float, focusX: Float, focusY: Float)

        fun onDrag(dx: Float, dy: Float)

        fun onFling(velocityX: Float, velocityY: Float)
    }

    abstract fun onTouch(motionEvent: MotionEvent)
}
