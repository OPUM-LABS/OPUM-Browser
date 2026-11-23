package com.mycompany.app.zoom

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.view.View.OnTouchListener
import android.widget.OverScroller
import com.mycompany.app.zoom.ZoomGestureDetector.FroyoDetector
import java.lang.ref.WeakReference

class ZoomVideoAttacher(textureView: TextureView, videoAttacherListener: VideoAttacherListener?) :
    OnTouchListener, ZoomGestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    @JvmField
    var viewRef: WeakReference<*>?
    @JvmField
    var tapDetector: GestureDetector?
    @JvmField
    var scaleDetector: ZoomGestureDetector?
    @JvmField
    var zoomListener: VideoAttacherListener?
    @JvmField
    var videoWidth: Int = 0
    @JvmField
    var videoHeight: Int = 0
    @JvmField
    var lastWidth: Int = 0
    @JvmField
    var lastHeight: Int = 0
    @JvmField var doubleTapOff: Boolean = false
    @JvmField var handledTouch: Boolean = false
    var isScaling: Boolean = false
    @JvmField
    var doubleTapped: Boolean = false
    @JvmField
    var scaling: Boolean = false
    private var flingRunnable: FlingRunnable? = null
    @JvmField
    val baseMatrix: Matrix = Matrix()
    @JvmField
    val drawMatrixBuf: Matrix = Matrix()
    @JvmField
    val suppMatrix: Matrix = Matrix()
    @JvmField val displayRect: RectF = RectF()
    val matrixValues: FloatArray = FloatArray(9)

    internal inner class NoOpGestures : SimpleOnGestureListener()

    private inner class AnimatedZoomRunnable(startScale: Float, var targetScale: Float, var focalX: Float, var focalY: Float) :
        Runnable {
        var stepScale: Float = 0f

        init {
            if (startScale < targetScale) {
                this.stepScale = 1.07f
            } else {
                this.stepScale = 0.93f
            }
        }
        override fun run() {
            val zoomVideoAttacher = this@ZoomVideoAttacher
            val matrix = zoomVideoAttacher.suppMatrix
            val textureViewI = zoomVideoAttacher.viewOrRelease()
            if (textureViewI == null) {
                return
            }
            val stepScaleNow = this.stepScale
            val focalX = this.focalX
            val focalY = this.focalY
            matrix.postScale(stepScaleNow, stepScaleNow, focalX, focalY)
            zoomVideoAttacher.applyMatrix()
            val scale = zoomVideoAttacher.currentScale()
            val targetScale = this.targetScale
            if ((stepScaleNow > 1.0f && scale < targetScale) || (stepScaleNow < 1.0f && targetScale < scale)) {
                textureViewI.postOnAnimation(this)
                return
            }
            zoomVideoAttacher.doubleTapped = false
            val stepScale = targetScale / scale
            matrix.postScale(stepScale, stepScale, focalX, focalY)
            zoomVideoAttacher.applyMatrix()
        }
    }

    private inner class FlingRunnable(context: Context?) : Runnable {
        val overScroller = OverScroller(context)
        var currentX: Int = 0
        var currentY: Int = 0
        override fun run() {
            val zoomVideoAttacher = this@ZoomVideoAttacher
            val textureViewI = zoomVideoAttacher.viewOrRelease()
            if (textureViewI != null) {
                val overScroller = this.overScroller
                if (overScroller.computeScrollOffset()) {
                    val currX = overScroller.getCurrX()
                    val currY = overScroller.getCurrY()
                    zoomVideoAttacher.suppMatrix.postTranslate(
                        (this.currentX - currX).toFloat(),
                        (this.currentY - currY).toFloat()
                    )
                    val matrixF = zoomVideoAttacher.currentMatrix()
                    val viewOrRelease = zoomVideoAttacher.viewOrRelease()
                    if (viewOrRelease != null) {
                        viewOrRelease.setTransform(matrixF)
                        viewOrRelease.invalidate()
                        val videoAttacherListener = zoomVideoAttacher.zoomListener
                        if (videoAttacherListener != null) {
                            videoAttacherListener.onMatrixChanged(zoomVideoAttacher.displayRectOf(matrixF))
                        }
                    }
                    this.currentX = currX
                    this.currentY = currY
                    textureViewI.postOnAnimation(this)
                }
            }
        }
    }

    interface VideoAttacherListener {
        fun onMatrixChanged(rectF: RectF?)

        fun onTap(): Boolean

        fun canScale(): Boolean

        fun onDoubleTap()

        fun onTouch(motionEvent: MotionEvent?)
    }

    init {
        this.viewRef = WeakReference<Any?>(textureView)
        textureView.setOnTouchListener(this)
        val gestureDetector =
            GestureDetector(textureView.getContext(), NoOpGestures())
        this.tapDetector = gestureDetector
        gestureDetector.setOnDoubleTapListener(this)
        val froyoDetector = FroyoDetector(textureView.getContext())
        froyoDetector.gestureListener = this
        this.scaleDetector = froyoDetector
        this.zoomListener = videoAttacherListener
    }
    override fun onScale(scale: Float, focusX: Float, focusY: Float) {
        if (this.videoWidth == 0 || this.videoHeight == 0 || viewOrRelease() == null) {
            return
        }
        val currentZoom = currentScale()
        val matrix = this.suppMatrix
        if (currentZoom > 100.0f) {
            matrix.postScale(0.9f, 0.9f, focusX, focusY)
            applyMatrix()
        } else if (currentZoom > 0.01f) {
            matrix.postScale(scale, scale, focusX, focusY)
            applyMatrix()
        }
    }
    override fun onDrag(dx: Float, dy: Float) {
        if (viewOrRelease() == null) {
            return
        }
        this.suppMatrix.postTranslate(dx, dy)
        applyMatrix()
    }
    override fun onFling(velocityX: Float, velocityY: Float) {
        val flingX: Int
        val startX: Int
        val startYPick: Int
        val flingY: Int
        val textureViewI = viewOrRelease()
        if (textureViewI == null) {
            return
        }
        val flingRunnable: FlingRunnable =
            FlingRunnable(textureViewI.getContext())
        this.flingRunnable = flingRunnable
        val width = textureViewI.getWidth()
        val height = textureViewI.getHeight()
        val velX = Math.round(velocityX)
        val velY = Math.round(velocityY)
        checkMatrixBounds()
        val rectFG = displayRectOf(currentMatrix())
        if (rectFG != null) {
            val flingStartX = Math.round(-rectFG.left)
            val flingStartY = Math.round(-rectFG.top)
            val viewWidth = width.toFloat()
            if (viewWidth < rectFG.width()) {
                flingX = Math.round(rectFG.width() - viewWidth)
                startX = 0
            } else {
                flingX = flingStartX
                startX = flingX
            }
            val viewHeight = height.toFloat()
            if (viewHeight < rectFG.height()) {
                flingY = Math.round(rectFG.height() - viewHeight)
                startYPick = 0
            } else {
                startYPick = flingStartY
                flingY = startYPick
            }
            flingRunnable.currentX = flingStartX
            flingRunnable.currentY = flingStartY
            if (flingStartX != flingX || flingStartY != flingY) {
                this.scaling = true
                flingRunnable.overScroller.fling(
                    flingStartX,
                    flingStartY,
                    velX,
                    velY,
                    startX,
                    flingX,
                    startYPick,
                    flingY,
                    0,
                    0
                )
            }
        }
        textureViewI.post(this.flingRunnable)
    }

    fun applyMatrix() {
        checkMatrixBounds()
        val matrixF = currentMatrix()
        val textureViewI = viewOrRelease()
        if (textureViewI != null) {
            textureViewI.setTransform(matrixF)
            textureViewI.invalidate()
            val videoAttacherListener = this.zoomListener
            if (videoAttacherListener != null) {
                videoAttacherListener.onMatrixChanged(displayRectOf(matrixF))
            }
        }
    }

    fun checkMatrixBounds() {
        val dx: Float
        val textureViewI = viewOrRelease() ?: return
        val rectFG = displayRectOf(currentMatrix()) ?: return
        val boundsWidth = rectFG.width()
        val boundsHeight = rectFG.height()
        val width = textureViewI.getWidth()
        val height = textureViewI.getHeight()
        val viewWidth = width.toFloat()
        val left = rectFG.left
        var dy = 0.0f
        if (boundsWidth <= viewWidth) {
            dx = ((viewWidth - boundsWidth) / 2.0f) - left
        } else if (left > 0.0f) {
            dx = -left
        } else {
            val right = rectFG.right
            dx = if (right < viewWidth) viewWidth - right else 0.0f
        }
        val viewHeight = height.toFloat()
        val top = rectFG.top
        if (boundsHeight <= viewHeight) {
            dy = ((viewHeight - boundsHeight) / 2.0f) - top
        } else if (top > 0.0f) {
            dy = -top
        } else {
            val bottom = rectFG.bottom
            if (bottom < viewHeight) {
                dy = viewHeight - bottom
            }
        }
        this.suppMatrix.postTranslate(dx, dy)
    }

    fun currentMatrix(): Matrix {
        val matrix = this.baseMatrix
        val drawMatrixBuf = this.drawMatrixBuf
        drawMatrixBuf.set(matrix)
        drawMatrixBuf.postConcat(this.suppMatrix)
        return drawMatrixBuf
    }

    fun displayRectOf(matrix: Matrix): RectF? {
        if (this.videoWidth == 0 || this.videoHeight == 0) return null
        val textureViewI = viewOrRelease() ?: return null
        val width = textureViewI.getWidth().toFloat()
        val height = textureViewI.getHeight().toFloat()
        val rectF = this.displayRect
        rectF.set(0.0f, 0.0f, width, height)
        matrix.mapRect(rectF)
        return rectF
    }

    fun currentScale(): Float {
        val matrix = this.suppMatrix
        val values = this.matrixValues
        matrix.getValues(values)
        return values[0]
    }

    fun viewOrRelease(): TextureView? {
        val weakReference = this.viewRef
        val textureView = if (weakReference != null) weakReference.get() as TextureView? else null
        if (textureView == null) {
            detach()
        }
        return textureView
    }

    fun detach() {
        val weakReference = this.viewRef
        if (weakReference != null) {
            val textureView = weakReference.get() as TextureView?
            if (textureView != null) {
                textureView.setOnTouchListener(null)
            }
            this.viewRef = null
        }
        this.tapDetector = null
        this.scaleDetector = null
        this.zoomListener = null
    }

    fun updateBaseMatrix(videoWidth: Int, newSwap: Int, rotation: Int, force: Boolean) {
        var videoWidth = videoWidth
        var newSwap = newSwap
        if (rotation % 180 != 0) {
            // Rotated 90/270 degrees: the content's width and height swap.
            val swap = newSwap
            newSwap = videoWidth
            videoWidth = swap
        }
        if (videoWidth == 0 || newSwap == 0) {
            this.lastHeight = 0
            this.lastWidth = 0
            this.videoHeight = 0
            this.videoWidth = 0
            applyMatrix()
            return
        }
        val textureViewI = viewOrRelease()
        if (textureViewI == null) {
            this.lastHeight = 0
            this.lastWidth = 0
            this.videoHeight = 0
            this.videoWidth = 0
            applyMatrix()
            return
        }
        val width = textureViewI.getWidth()
        val height = textureViewI.getHeight()
        if (width == 0 || height == 0) {
            this.lastHeight = 0
            this.lastWidth = 0
            this.videoHeight = 0
            this.videoWidth = 0
            applyMatrix()
            return
        }
        if (!force && videoWidth == this.videoWidth && newSwap == this.videoHeight && width == this.lastWidth && height == this.lastHeight) {
            return
        }
        this.videoWidth = videoWidth
        this.videoHeight = newSwap
        this.lastWidth = width
        this.lastHeight = height
        val matrix = this.baseMatrix
        matrix.reset()
        matrix.postScale(videoWidth / width.toFloat(), newSwap / height.toFloat())
        matrix.postTranslate((width - videoWidth) / 2.0f, (height - newSwap) / 2.0f)
        matrix.postRotate(rotation.toFloat())
        this.suppMatrix.reset()
        applyMatrix()
    }
    override fun onDoubleTap(e: MotionEvent): Boolean {
        if (viewOrRelease() != null && !this.doubleTapOff) {
            val targetScale = if (currentScale() >= 2.0f) 1.0f else 2.0f
            val x = e.getX()
            val y = e.getY()
            val textureViewI = if (this.videoWidth != 0 && this.videoHeight != 0) viewOrRelease() else null
            if (textureViewI != null) {
                this.doubleTapped = true
                textureViewI.post(AnimatedZoomRunnable(currentScale(), targetScale, x, y))
            }
        }
        return true
    }
    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        return false
    }
    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        val videoAttacherListener = this.zoomListener
        return videoAttacherListener != null && videoAttacherListener.onTap()
    }
    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        var zoomVideoAttacher: ZoomVideoAttacher
        var found = false
        if (view == null) {
            return false
        }
        val actionMasked = event.getActionMasked()
        if (actionMasked != 0) {
            if (actionMasked == 1 || actionMasked == 3) {
                this.handledTouch = false
                if (currentScale() < 1.0f) {
                    checkMatrixBounds()
                    val rectFG = displayRectOf(currentMatrix())
                    if (rectFG != null) {
                        zoomVideoAttacher = this
                        view.post(
                            zoomVideoAttacher.AnimatedZoomRunnable(
                                currentScale(),
                                1.0f,
                                rectFG.centerX(),
                                rectFG.centerY()
                            )
                        )
                        found = true
                    }
                }
            } else if (actionMasked == 5 && this.zoomListener?.canScale() != true) {
                this.isScaling = true
            }
            zoomVideoAttacher = this
        } else {
            zoomVideoAttacher = this
            val videoAttacherListener = zoomVideoAttacher.zoomListener
            if (videoAttacherListener == null || !videoAttacherListener.canScale()) {
                zoomVideoAttacher.handledTouch = true
            }
            zoomVideoAttacher.isScaling = false
            zoomVideoAttacher.scaling = false
            val flingRunnable = zoomVideoAttacher.flingRunnable
            if (flingRunnable != null) {
                flingRunnable.overScroller.forceFinished(true)
                zoomVideoAttacher.flingRunnable = null
            }
        }
        val zoomListener = zoomVideoAttacher.zoomListener
        if (zoomListener == null || !zoomListener.canScale()) {
            val gestureDetector = zoomVideoAttacher.tapDetector
            if (gestureDetector != null && gestureDetector.onTouchEvent(event)) {
                found = true
            }
            val zoomGestureDetector = zoomVideoAttacher.scaleDetector
            if (zoomGestureDetector != null) {
                zoomGestureDetector.onTouch(event)
                found = true
            }
        } else {
            found = true
        }
        val videoAttacherListener = zoomVideoAttacher.zoomListener
        if (videoAttacherListener != null) {
            if (!zoomVideoAttacher.isScaling && !zoomVideoAttacher.doubleTapped && !zoomVideoAttacher.scaling) {
                videoAttacherListener.onTouch(event)
                return found
            }
            if (event.getActionMasked() == 1 || event.getActionMasked() == 3) {
                zoomVideoAttacher.zoomListener!!.onDoubleTap()
            }
        }
        return found
    }
}
