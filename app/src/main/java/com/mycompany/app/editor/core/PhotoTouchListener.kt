package com.mycompany.app.editor.core

import android.graphics.PointF
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import com.mycompany.app.main.MainUtil
import ch.opumlabs.opumbrowser.R
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

@Suppress("deprecation")
class PhotoTouchListener : OnTouchListener {
    @JvmField
    var touchSlop: Float = 0f
    @JvmField
    var scaleDetector: ScaleGestureDetector? = null
    @JvmField
    var tapDetector: GestureDetector? = null
    @JvmField
    var objectListener: PhotoObjectListener? = null
    @JvmField
    var deleteView: ImageView? = null
    @JvmField
    var heldObject: ObjHolder? = null
    @JvmField
    var touching: Boolean = false
    @JvmField
    var longPressed: Boolean = false
    @JvmField
    var activePointerId: Int = 0
    @JvmField
    var multiTouch: Boolean = false
    @JvmField
    var startTransX: Float = 0f
    @JvmField
    var startTransY: Float = 0f
    @JvmField
    var prevX: Float = 0f
    @JvmField
    var prevY: Float = 0f

    internal inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @JvmField
        var floatValue: Float = 0f
        @JvmField
        var floatValue2: Float = 0f
        @JvmField
        val spanVector: Vector2D = Vector2D()

        fun onScale(scaleGestureDetector: ScaleGestureDetector) {
            this.floatValue = scaleGestureDetector.focusX
            this.floatValue2 = scaleGestureDetector.focusY
            this.spanVector.set(scaleGestureDetector.currSpanVector)
        }
    }

    internal inner class AddTextViewGestures : SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent) {
            val photoTouchListener = this@PhotoTouchListener
            val photoObjectListener: PhotoObjectListener? = photoTouchListener.objectListener
            if (!photoTouchListener.touching || photoTouchListener.longPressed || photoTouchListener.multiTouch || photoObjectListener == null) {
                return
            }
            val held = photoTouchListener.heldObject
            val clearStep = photoObjectListener as PhotoEditor.ClearSelectionStep
            val photoEditor = clearStep.outer
            if (held == null || clearStep.holding) {
                return
            }
            photoEditor.listener.addTextView(
                held.frame,
                held.appCompatTextView!!.getText().toString(),
                held.appCompatTextView!!.getCurrentTextColor()
            )
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val photoTouchListener = this@PhotoTouchListener
            val held: ObjHolder? = photoTouchListener.heldObject
            if (photoTouchListener.touching && photoTouchListener.objectListener != null && held != null) {
                val visibility = held.imageView!!.getVisibility()
                val frameLayout = held.borderFrame!!
                if (visibility == 0) {
                    frameLayout.setBackgroundResource(0)
                    held.imageView!!.setVisibility(View.GONE)
                    return true
                }
                frameLayout.setBackgroundResource(R.drawable.text_border)
                held.imageView!!.setVisibility(View.VISIBLE)
            }
            return true
        }
    }

    internal inner class ObjectTouchedTask : Runnable {
        override fun run() {
            val photoTouchListener = this@PhotoTouchListener
            val photoObjectListener = photoTouchListener.objectListener
            if (photoObjectListener != null) {
                (photoObjectListener as PhotoEditor.ClearSelectionStep).onObjectHeld(photoTouchListener.heldObject, false)
            }
        }
    }

    class ObjHolder {
        @JvmField var frame: FrameLayout? = null
        @JvmField var borderFrame: FrameLayout? = null
        @JvmField var appCompatTextView: AppCompatTextView? = null
        @JvmField var imageView: ImageView? = null
    }

    interface PhotoObjectListener

    private class TransformInfo {
        var deltaX: Float = 0f
        var deltaY: Float = 0f
        var pivotX: Float = 0f
        var pivotY: Float = 0f
        var deltaScale: Float = 0f
        var deltaAngle: Float = 0f
    }

    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        var touching: Boolean
        var found: Boolean
        val imageView = this.deleteView
        val photoObjectListener = this.objectListener
        val scaleGestureDetector = this.scaleDetector
        if (scaleGestureDetector == null) {
            return false
        }
        val onScaleGestureListener = scaleGestureDetector.listener
        val actionMasked = event.getActionMasked()
        if (actionMasked == 0) {
            scaleGestureDetector.reset()
        }
        if (scaleGestureDetector.invalidGesture) {
            touching = false
        } else {
            if (scaleGestureDetector.gestureInProgress) {
                if (actionMasked == 1) {
                    touching = false
                    scaleGestureDetector.reset()
                } else if (actionMasked == 2) {
                    scaleGestureDetector.setContext(view, event)
                    if (scaleGestureDetector.currPressure / scaleGestureDetector.prevPressure > 0.67f) {
                        val scaleListener = onScaleGestureListener as ScaleListener
                        if (this@PhotoTouchListener.touching) {
                            val transformInfo = TransformInfo()
                            val floatValue = scaleListener.floatValue
                            transformInfo.deltaX = floatValue
                            val newDeltaY = scaleListener.floatValue2
                            transformInfo.deltaY = newDeltaY
                            transformInfo.pivotX = scaleGestureDetector.focusX - floatValue
                            transformInfo.pivotY = scaleGestureDetector.focusY - newDeltaY
                            var scaleFactor = scaleGestureDetector.scaleFactor
                            if (scaleFactor == -1.0f) {
                                var currLen = scaleGestureDetector.currLen
                                if (currLen == -1.0f) {
                                    val currFingerDiffX = scaleGestureDetector.currFingerDiffX
                                    val currFingerDiffY = scaleGestureDetector.currFingerDiffY
                                    currLen = sqrt(((currFingerDiffY * currFingerDiffY) + (currFingerDiffX * currFingerDiffX)).toDouble()).toFloat()
                                    scaleGestureDetector.currLen = currLen
                                }
                                var prevLen = scaleGestureDetector.prevLen
                                if (prevLen == -1082130432f) {
                                    val prevFingerDiffX = scaleGestureDetector.prevFingerDiffX
                                    val prevFingerDiffY = scaleGestureDetector.prevFingerDiffY
                                    prevLen = sqrt(((prevFingerDiffY * prevFingerDiffY) + (prevFingerDiffX * prevFingerDiffX)).toDouble()).toFloat()
                                    scaleGestureDetector.prevLen = prevLen
                                }
                                scaleFactor = currLen / prevLen
                                scaleGestureDetector.scaleFactor = scaleFactor
                            }
                            transformInfo.deltaScale = scaleFactor
                            val vector2D = scaleListener.spanVector
                            val currSpanVector = scaleGestureDetector.currSpanVector
                            val unusedZero = 0
                            val x1 = (vector2D as PointF).x
                            val y1 = (vector2D as PointF).y
                            val length1 = sqrt(((y1 * y1) + (x1 * x1)).toDouble()).toFloat()
                            (vector2D as PointF).x /= length1
                            (vector2D as PointF).y /= length1
                            val x2 = (currSpanVector as PointF).x
                            val y2 = (currSpanVector as PointF).y
                            val length = sqrt(((y2 * y2) + (x2 * x2)).toDouble()).toFloat()
                            val unitX = (currSpanVector as PointF).x / length
                            (currSpanVector as PointF).x = unitX
                            val unitY = (currSpanVector as PointF).y / length
                            (currSpanVector as PointF).y = unitY
                            touching = false
                            transformInfo.deltaAngle = ((atan2(
                                unitY.toDouble(),
                                unitX.toDouble()
                            ) - atan2(
                                (vector2D as PointF).y.toDouble(),
                                (vector2D as PointF).x.toDouble()
                            )) * 57.29577951308232).toFloat()
                            if (view != null) {
                                val deltaX = transformInfo.deltaX
                                val deltaY = transformInfo.deltaY
                                if (view.getPivotX() != deltaX || view.getPivotY() != deltaY) {
                                    val originBefore = floatArrayOf(0.0f, 0.0f)
                                    view.getMatrix().mapPoints(originBefore)
                                    view.setPivotX(deltaX)
                                    view.setPivotY(deltaY)
                                    val originAfter = floatArrayOf(0.0f, 0.0f)
                                    view.getMatrix().mapPoints(originAfter)
                                    val dx = originAfter[0] - originBefore[0]
                                    val dy = originAfter[1] - originBefore[1]
                                    view.setTranslationX(view.getTranslationX() - dx)
                                    view.setTranslationY(view.getTranslationY() - dy)
                                }
                                adjustTranslation(view, transformInfo.pivotX, transformInfo.pivotY)
                                val newScale = max(0.5f, min(10.0f, view.getScaleX() * transformInfo.deltaScale))
                                view.setScaleX(newScale)
                                view.setScaleY(newScale)
                                var rotation = view.getRotation() + transformInfo.deltaAngle
                                if (rotation > 180.0f) {
                                    rotation -= 360.0f
                                } else if (rotation < -180.0f) {
                                    rotation += 360.0f
                                }
                                view.setRotation(rotation)
                            }
                        }
                    }
                } else if (actionMasked == 3) {
                    scaleGestureDetector.reset()
                } else if (actionMasked == 5) {
                    var firstPointerId = scaleGestureDetector.activeId0
                    val activeId = scaleGestureDetector.activeId1
                    scaleGestureDetector.reset()
                    scaleGestureDetector.prevEvent = MotionEvent.obtain(event)
                    if (!scaleGestureDetector.active0MostRecent) {
                        firstPointerId = activeId
                    }
                    scaleGestureDetector.activeId0 = firstPointerId
                    scaleGestureDetector.activeId1 = event.getPointerId(event.getActionIndex())
                    scaleGestureDetector.active0MostRecent = false
                    if (event.findPointerIndex(scaleGestureDetector.activeId0) < 0 || scaleGestureDetector.activeId0 == scaleGestureDetector.activeId1) {
                        scaleGestureDetector.activeId0 = event.getPointerId(
                            ScaleGestureDetector.Companion.findNewActiveIndex(
                                scaleGestureDetector.activeId1,
                                -1,
                                event
                            )
                        )
                    }
                    scaleGestureDetector.setContext(view, event)
                    (onScaleGestureListener as ScaleListener).onScale(scaleGestureDetector)
                    scaleGestureDetector.gestureInProgress = true
                } else if (actionMasked == 6) {
                    val pointerCount = event.getPointerCount()
                    val actionIndex = event.getActionIndex()
                    val pointerId = event.getPointerId(actionIndex)
                    if (pointerCount > 2) {
                        val firstPointer = scaleGestureDetector.activeId0
                        val activeId = scaleGestureDetector.activeId1
                        if (pointerId == firstPointer) {
                            val newIndex: Int =
                                ScaleGestureDetector.Companion.findNewActiveIndex(activeId, actionIndex, event)
                            if (newIndex >= 0) {
                                scaleGestureDetector.activeId0 = event.getPointerId(newIndex)
                                scaleGestureDetector.active0MostRecent = true
                                scaleGestureDetector.prevEvent = MotionEvent.obtain(event)
                                scaleGestureDetector.setContext(view, event)
                                (onScaleGestureListener as ScaleListener).onScale(scaleGestureDetector)
                                scaleGestureDetector.gestureInProgress = true
                                found = false
                            } else {
                                found = true
                            }
                        } else {
                            if (pointerId == activeId) {
                                val newIndex: Int =
                                    ScaleGestureDetector.Companion.findNewActiveIndex(firstPointer, actionIndex, event)
                                if (newIndex >= 0) {
                                    scaleGestureDetector.activeId1 = event.getPointerId(newIndex)
                                    scaleGestureDetector.active0MostRecent = false
                                    scaleGestureDetector.prevEvent = MotionEvent.obtain(event)
                                    scaleGestureDetector.setContext(view, event)
                                    (onScaleGestureListener as ScaleListener).onScale(
                                        scaleGestureDetector
                                    )
                                    scaleGestureDetector.gestureInProgress = true
                                } else {
                                    found = true
                                }
                            }
                            found = false
                        }
                        scaleGestureDetector.prevEvent!!.recycle()
                        scaleGestureDetector.prevEvent = MotionEvent.obtain(event)
                        scaleGestureDetector.setContext(view, event)
                    } else {
                        found = true
                    }
                    if (found) {
                        scaleGestureDetector.setContext(view, event)
                        var activeId = scaleGestureDetector.activeId0
                        if (pointerId == activeId) {
                            activeId = scaleGestureDetector.activeId1
                        }
                        val focusIndex = event.findPointerIndex(activeId)
                        scaleGestureDetector.focusX = event.getX(focusIndex)
                        scaleGestureDetector.focusY = event.getY(focusIndex)
                        scaleGestureDetector.reset()
                        scaleGestureDetector.activeId0 = activeId
                        scaleGestureDetector.active0MostRecent = true
                    }
                }
            } else if (actionMasked == 0) {
                scaleGestureDetector.activeId0 = event.getPointerId(0)
                scaleGestureDetector.active0MostRecent = true
            } else if (actionMasked == 1) {
                scaleGestureDetector.reset()
            } else if (actionMasked == 5) {
                val motionEvent = scaleGestureDetector.prevEvent
                if (motionEvent != null) {
                    motionEvent.recycle()
                }
                scaleGestureDetector.prevEvent = MotionEvent.obtain(event)
                val actionIndex = event.getActionIndex()
                val index = event.findPointerIndex(scaleGestureDetector.activeId0)
                val pointerId = event.getPointerId(actionIndex)
                scaleGestureDetector.activeId1 = pointerId
                if (index < 0 || index == actionIndex) {
                    scaleGestureDetector.activeId0 = event.getPointerId(
                        ScaleGestureDetector.Companion.findNewActiveIndex(
                            pointerId,
                            -1,
                            event
                        )
                    )
                }
                scaleGestureDetector.active0MostRecent = false
                scaleGestureDetector.setContext(view, event)
                (onScaleGestureListener as ScaleListener).onScale(scaleGestureDetector)
                scaleGestureDetector.gestureInProgress = true
            }
            touching = false
        }
        this.tapDetector!!.onTouchEvent(event)
        val action = event.getAction()
        val rawX = event.getRawX().toInt()
        val rawY = event.getRawY().toInt()
        val maskedAction = event.getActionMasked() and action
        if (maskedAction == 0) {
            if (photoObjectListener == null) {
                return false
            }
            val clearStep = photoObjectListener as PhotoEditor.ClearSelectionStep
            if (clearStep.outer.editMode.toInt() != 0) {
                return false
            }
            this.touching = true
            this.longPressed = false
            val pointerId = event.getPointerId(0)
            this.activePointerId = pointerId
            this.multiTouch = pointerId > 0
            this.startTransX = view!!.getTranslationX()
            this.startTransY = view.getTranslationY()
            this.prevX = event.getX()
            this.prevY = event.getY()
            view.bringToFront()
            clearStep.onObjectHeld(this.heldObject, true)
            return true
        }
        if (maskedAction == 1) {
            this.touching = false
            this.longPressed = false
            this.activePointerId = -1
            this.multiTouch = false
            if (!MainUtil.isInsideView(rawX, rawY, 0, imageView)) {
                view!!.animate().translationX(this.startTransX).translationY(this.startTransY)
            }
            if (photoObjectListener != null && imageView != null) {
                imageView.post(ObjectTouchedTask())
            }
        } else if (maskedAction != 2) {
            if (maskedAction == 3) {
                this.touching = false
                this.longPressed = false
                this.activePointerId = -1
                this.multiTouch = false
                if (!MainUtil.isInsideView(rawX, rawY, 0, imageView)) {
                    view!!.animate().translationX(this.startTransX).translationY(this.startTransY)
                }
                if (photoObjectListener != null) {
                    imageView!!.post(ObjectTouchedTask())
                }
            } else {
                if (maskedAction == 5) {
                    this.multiTouch = true
                    return true
                }
                if (maskedAction == 6) {
                    val pointerIndex = (action and 65280) shr 8
                    if (pointerIndex == 0) {
                        this.touching = touching
                        return true
                    }
                    if (event.getPointerId(pointerIndex) == this.activePointerId) {
                        val newIndex = if (pointerIndex == 0) 1 else 0
                        this.prevX = event.getX(newIndex)
                        this.prevY = event.getY(newIndex)
                        this.activePointerId = event.getPointerId(newIndex)
                        return true
                    }
                }
            }
        } else if (this.touching) {
            if (!this.longPressed) {
                this.longPressed =
                    MainUtil.pointDistance(this.prevX, event.getX(), this.prevY, event.getY()) > this.touchSlop
            }
            val activeIndex = event.findPointerIndex(this.activePointerId)
            if (!scaleGestureDetector.gestureInProgress && activeIndex != -1) {
                adjustTranslation(
                    view,
                    event.getX(activeIndex) - this.prevX,
                    event.getY(activeIndex) - this.prevY
                )
                return true
            }
        }
        return true
    }

    companion object {
        @JvmStatic
        fun adjustTranslation(view: View?, pivotX: Float, pivotY: Float) {
            if (view == null) {
                return
            }
            val pivot = floatArrayOf(pivotX, pivotY)
            view.getMatrix().mapVectors(pivot)
            view.setTranslationX(view.getTranslationX() + pivot[0])
            view.setTranslationY(view.getTranslationY() + pivot[1])
        }
    }
}
