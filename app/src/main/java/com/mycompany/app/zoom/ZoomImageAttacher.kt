package com.mycompany.app.zoom

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import android.widget.OverScroller
import com.mycompany.app.view.MySizeFrame
import com.mycompany.app.zoom.ZoomGestureDetector.FroyoDetector
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class ZoomImageAttacher : OnTouchListener, ZoomGestureDetector.OnGestureListener,
    GestureDetector.OnDoubleTapListener, OnGlobalLayoutListener {
    @JvmField
    var lastTop: Int = 0
    @JvmField
    var lastBottom: Int = 0
    @JvmField var sizeFrame: MySizeFrame? = null
    @JvmField var attachedView: ImageView? = null
    var viewRef: WeakReference<*>? = null
    @JvmField
    var tapDetector: GestureDetector? = null
    @JvmField
    var scaleDetector: ZoomGestureDetector? = null
    @JvmField
    var zoomListener: AttacherListener? = null
    @JvmField
    var doubleTapped: Boolean = false
    var blockParent: Boolean = false
    @JvmField
    var settling: Boolean = false
    var isScaling: Boolean = false
    private var flingRunnable: FlingRunnable? = null
    @JvmField
    var baseMatrix: Matrix
    @JvmField
    var drawMatrixBuf: Matrix
    @JvmField var suppMatrix: Matrix
    @JvmField var displayRect: RectF
    var matrixValues: FloatArray
    @JvmField
    var zoomScaleType: ScaleType?
    @JvmField var pausedA: Boolean = false
    @JvmField var pausedB: Boolean = false
    @JvmField var allowParentIntercept: Boolean = false
    var isZoomEnabled: Boolean = false
    @JvmField
    var scrollEdge: Int
    @JvmField
    var lastLeft: Int = 0
    @JvmField
    var lastRight: Int = 0

    internal inner class NoOpGestures : SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent) {
        }
    }

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
            val zoomImageAttacher = this@ZoomImageAttacher
            val imageViewM = zoomImageAttacher.viewOrRelease()
            if (imageViewM == null) {
                return
            }
            val matrix = zoomImageAttacher.suppMatrix
            val stepScaleNow = this.stepScale
            val focalX = this.focalX
            val focalY = this.focalY
            matrix.postScale(stepScaleNow, stepScaleNow, focalX, focalY)
            zoomImageAttacher.refreshDisplay(true)
            val scale = zoomImageAttacher.currentScale()
            val targetScale = this.targetScale
            if ((stepScaleNow > 1.0f && scale < targetScale) || (stepScaleNow < 1.0f && targetScale < scale)) {
                imageViewM.postOnAnimation(this)
                return
            }
            zoomImageAttacher.settling = false
            val stepScale = targetScale / scale
            zoomImageAttacher.suppMatrix.postScale(stepScale, stepScale, focalX, focalY)
            zoomImageAttacher.refreshDisplay(true)
        }
    }

    interface AttacherListener {
        fun onMatrixChanged(rectF: RectF?, isZoomed: Boolean)

        fun onTouch(motionEvent: MotionEvent?, isDown: Boolean)

        fun onTap(): Boolean

        fun canScale(): Boolean

        fun onDoubleTap()
    }

    private inner class FlingRunnable(context: Context?) : Runnable {
        val overScroller = OverScroller(context)
        var currentX: Int = 0
        var currentY: Int = 0
        override fun run() {
            val zoomImageAttacher = this@ZoomImageAttacher
            val imageViewM = zoomImageAttacher.viewOrRelease()
            if (imageViewM != null) {
                val overScroller = this.overScroller
                if (overScroller.computeScrollOffset()) {
                    val currX = overScroller.getCurrX()
                    val currY = overScroller.getCurrY()
                    zoomImageAttacher.suppMatrix.postTranslate(
                        (this.currentX - currX).toFloat(),
                        (this.currentY - currY).toFloat()
                    )
                    zoomImageAttacher.applyMatrix(zoomImageAttacher.currentMatrix(), true)
                    this.currentX = currX
                    this.currentY = currY
                    imageViewM.postOnAnimation(this)
                }
            }
        }
    }

    constructor(imageView: ImageView, attacherListener: AttacherListener?, i: Int) {
        this.baseMatrix = Matrix()
        this.drawMatrixBuf = Matrix()
        this.suppMatrix = Matrix()
        this.displayRect = RectF()
        this.matrixValues = FloatArray(9)
        val scaleType = ScaleType.FIT_CENTER
        this.zoomScaleType = scaleType
        this.scrollEdge = 0
        this.doubleTapped = true
        attach(imageView, scaleType, attacherListener)
    }
    override fun onScale(scale: Float, focusX: Float, focusY: Float) {
        if (this.pausedA || this.pausedB || viewOrRelease() == null) {
            return
        }
        val currentZoom = currentScale()
        val matrix = this.suppMatrix
        if (currentZoom > 100.0f) {
            matrix.postScale(0.99f, 0.99f, focusX, focusY)
            refreshDisplay(true)
        } else if (currentZoom > 0.01f) {
            matrix.postScale(scale, scale, focusX, focusY)
            refreshDisplay(true)
        }
    }
    override fun onDrag(dx: Float, dy: Float) {
        val imageViewM = viewOrRelease()
        if (imageViewM != null) {
            this.suppMatrix.postTranslate(dx, dy)
            refreshDisplay(true)
            val scrollEdge = this.scrollEdge
            if (!this.allowParentIntercept || (scrollEdge != 0 && ((scrollEdge != 1 || dx < 1.0f) && (scrollEdge != 2 || dx > -1.0f)))) {
                return
            }
            val parent = imageViewM.getParent()
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
    }
    override fun onFling(velocityX: Float, velocityY: Float) {
        val flingX: Int
        val startX: Int
        val startYPick: Int
        val flingY: Int
        if (this.pausedA || this.pausedB) return
        val imageViewM = viewOrRelease() ?: return
        val flingRunnable = FlingRunnable(imageViewM.getContext())
        this.flingRunnable = flingRunnable
        val width = imageViewM.getWidth()
        val height = imageViewM.getHeight()
        val velX = Math.round(velocityX)
        val velY = Math.round(velocityY)
        checkMatrixBounds()
        val rectFJ = displayRectOf(currentMatrix())
        if (rectFJ != null) {
            val flingStartX = Math.round(-rectFJ.left)
            val flingStartY = Math.round(-rectFJ.top)
            val viewWidth = width.toFloat()
            if (viewWidth < rectFJ.width()) {
                flingX = Math.round(rectFJ.width() - viewWidth)
                startX = 0
            } else {
                flingX = flingStartX
                startX = flingX
            }
            val viewHeight = height.toFloat()
            if (viewHeight < rectFJ.height()) {
                flingY = Math.round(rectFJ.height() - viewHeight)
                startYPick = 0
            } else {
                startYPick = flingStartY
                flingY = startYPick
            }
            flingRunnable.currentX = flingStartX
            flingRunnable.currentY = flingStartY
            if (flingStartX != flingX || flingStartY != flingY) {
                this.isScaling = true
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
        imageViewM.post(this.flingRunnable)
    }

    fun cancelFling() {
        this.isScaling = false
        val flingRunnable = this.flingRunnable
        if (flingRunnable != null) {
            flingRunnable.overScroller.forceFinished(true)
            this.flingRunnable = null
        }
    }

    fun fitBounds() {
        if (this.settling) return
        val imageViewM = viewOrRelease() ?: return
        val rectF = this.displayRect
        run {
            val rectWidth = rectF.right - rectF.left
            val rectHeight = rectF.bottom - rectF.top
            if (rectWidth.compareTo(0.0f) == 0 || rectHeight.compareTo(0.0f) == 0) {
                return
            }
            val width = imageViewM.getWidth().toFloat()
            val height = imageViewM.getHeight().toFloat()
            val topValue =
                if (width / rectWidth > height / rectHeight) (rectHeight * width) / (rectWidth * height) else (rectWidth * height) / (rectHeight * width)
            val scale = currentScale()
            if (scale.compareTo(topValue) != 0) {
                cancelFling()
                this.settling = true
                imageViewM.post(
                    AnimatedZoomRunnable(
                        scale,
                        topValue,
                        width / 2.0f,
                        height / 2.0f
                    )
                )
            }
        }
    }

    fun resetIfZoomed() {
        if (this.settling) return
        val imageViewM = viewOrRelease() ?: return
        val scale = currentScale()
        if (scale > 1.0f || scale < 0.9f) {
            cancelFling()
            this.settling = true
            imageViewM.post(
                AnimatedZoomRunnable(
                    scale,
                    1.0f,
                    (imageViewM.getWidth() / 2).toFloat(),
                    (imageViewM.getHeight() / 2).toFloat()
                )
            )
        }
    }

    fun refreshDisplay(fromUser: Boolean) {
        checkMatrixBounds()
        applyMatrix(currentMatrix(), fromUser)
    }

    fun checkMatrixBounds() {
        val dx: Float
        val rectWidth: Float
        val imageViewM = viewOrRelease() ?: return
        val rectFJ = displayRectOf(currentMatrix()) ?: return
        val boundsWidth = rectFJ.width()
        val boundsHeight = rectFJ.height()
        val width = imageViewM.getWidth()
        val height = imageViewM.getHeight()
        val viewWidth = width.toFloat()
        var dy = 0.0f
        if (boundsWidth <= viewWidth) {
            val scaleType = this.zoomScaleType
            if (scaleType == ScaleType.FIT_START) {
                dx = -rectFJ.left
            } else {
                val fitScale = ScaleType.FIT_END
                val left = rectFJ.left
                var spaceX = viewWidth - boundsWidth
                if (scaleType != fitScale) {
                    spaceX /= 2.0f
                }
                dx = spaceX - left
            }
            this.scrollEdge = 0
        } else {
            val left = rectFJ.left
            if (left > 0.0f) {
                dx = -left
                this.scrollEdge = 1
            } else {
                val right = rectFJ.right
                if (right < viewWidth) {
                    dx = viewWidth - right
                    this.scrollEdge = 2
                } else {
                    this.scrollEdge = -1
                    dx = 0.0f
                }
            }
        }
        var viewHeight = height.toFloat()
        if (boundsHeight <= viewHeight) {
            val zoomScaleType = this.zoomScaleType
            if (zoomScaleType == ScaleType.FIT_START) {
                dy = -rectFJ.top
            } else {
                val scaleType = ScaleType.FIT_END
                rectWidth = rectFJ.top
                viewHeight -= boundsHeight
                if (zoomScaleType != scaleType) {
                    viewHeight /= 2.0f
                }
                dy = viewHeight - rectWidth
            }
        } else {
            val top = rectFJ.top
            if (top > 0.0f) {
                dy = -top
            } else {
                rectWidth = rectFJ.bottom
                if (rectWidth < viewHeight) {
                    dy = viewHeight - rectWidth
                }
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
        val width: Int
        val imageViewM = viewOrRelease() ?: return null
        val drawable = imageViewM.getDrawable() ?: return null
        val intrinsicWidth = drawable.getIntrinsicWidth().toFloat()
        val intrinsicHeight = drawable.getIntrinsicHeight().toFloat()
        val rectF = this.displayRect
        rectF.set(0.0f, 0.0f, intrinsicWidth, intrinsicHeight)
        matrix.mapRect(rectF)
        imageViewM.getContext()
        if (imageViewM.getHeight() > frameHeight()) {
            val viewHeight = frameHeight().toFloat()
            val mySizeFrame = this.sizeFrame
            if (mySizeFrame != null) {
                width = mySizeFrame.getWidth()
            } else {
                val imageView = this.attachedView
                width = (imageView?.getParent() as ViewGroup?)?.getWidth() ?: 0
            }
            val scaleX = viewHeight / width
            val rectWidth = rectF.right - rectF.left
            val rectHeight = rectF.bottom - rectF.top
            rectF.bottom = rectF.top + rectHeight + ((rectHeight - (rectWidth * scaleX)) / currentScale())
        }
        return rectF
    }

    fun frameHeight(): Int {
        val mySizeFrame = this.sizeFrame
        if (mySizeFrame != null) {
            return mySizeFrame.getHeight()
        }
        return (this.attachedView?.getParent() as ViewGroup?)?.getHeight() ?: 0
    }

    fun currentScale(): Float {
        val matrix = this.suppMatrix
        val values = this.matrixValues
        matrix.getValues(values)
        return values[0]
    }

    fun viewOrRelease(): ImageView? {
        val weakReference = this.viewRef
        val imageView = if (weakReference != null) weakReference.get() as ImageView? else null
        if (imageView == null) {
            detach()
        }
        return imageView
    }

    fun attach(imageView: ImageView, scaleType: ScaleType?, attacherListener: AttacherListener?) {
        this.attachedView = imageView
        this.zoomScaleType = scaleType
        this.viewRef = WeakReference<Any?>(imageView)
        imageView.setScaleType(ScaleType.MATRIX)
        imageView.setOnTouchListener(this)
        imageView.getViewTreeObserver().addOnGlobalLayoutListener(this)
        val gestureDetector =
            GestureDetector(imageView.getContext(), NoOpGestures())
        this.tapDetector = gestureDetector
        gestureDetector.setOnDoubleTapListener(this)
        val froyoDetector = FroyoDetector(imageView.getContext())
        froyoDetector.gestureListener = this
        this.scaleDetector = froyoDetector
        this.zoomListener = attacherListener
        updateBaseMatrix(imageView)
    }

    fun atBottom(): Boolean {
        if (isZoomed()) return false
        val imageViewM = viewOrRelease() ?: return false
        if (imageViewM.getHeight() <= frameHeight()) return false
        val rectF = this.displayRect
        val rectHeight = rectF.bottom - rectF.top
        val viewHeight = frameHeight().toFloat()
        val bottom = rectF.bottom
        if (rectHeight >= viewHeight) {
            return abs(bottom + rectF.top) < viewHeight + 1.0f
        }
        return abs(((viewHeight - rectHeight) / 2.0f) + bottom) < viewHeight + 1.0f
    }
    override fun onDoubleTap(e: MotionEvent): Boolean {
        var targetScale: Float
        if (viewOrRelease() != null) {
            val attacherListener = this.zoomListener
            if (attacherListener != null) {
                attacherListener.onDoubleTap()
            }
            if (this.doubleTapped) {
                val imageViewM = viewOrRelease()
                targetScale = -1.0f
                if (imageViewM != null) {
                    val rectF = this.displayRect
                    run {
                        val rectWidth = rectF.right - rectF.left
                        val rectHeight = rectF.bottom - rectF.top
                        if (rectWidth.compareTo(0.0f) != 0 && rectHeight.compareTo(0.0f) != 0) {
                            val width = imageViewM.getWidth().toFloat()
                            val height = imageViewM.getHeight().toFloat()
                            targetScale =
                                if (width / rectWidth > height / rectHeight) (width * rectHeight) / (rectWidth * height) else (height * rectWidth) / (rectHeight * width)
                        }
                    }
                }
            } else {
                targetScale = 2.0f
            }
            if (targetScale - currentScale() <= 0.01f) {
                targetScale = 1.0f
            }
            val targetScaleValue = targetScale
            val x = e.getX()
            val y = e.getY()
            val imageViewM = if (!this.pausedA && !this.pausedB) viewOrRelease() else null
            if (imageViewM != null) {
                this.settling = true
                imageViewM.post(AnimatedZoomRunnable(currentScale(), targetScaleValue, x, y))
            }
        }
        return true
    }
    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        return false
    }
    override fun onGlobalLayout() {
        val imageViewM = viewOrRelease()
        if (imageViewM != null) {
            val left = imageViewM.getLeft()
            val right = imageViewM.getRight()
            val top = imageViewM.getTop()
            val bottom = imageViewM.getBottom()
            if (left == this.lastLeft && right == this.lastRight && top == this.lastTop && bottom == this.lastBottom) {
                return
            }
            if (!this.isZoomEnabled || currentScale() <= 1.0f) {
                this.isZoomEnabled = true
                updateBaseMatrix(imageViewM)
                this.lastLeft = left
                this.lastRight = right
                this.lastTop = top
                this.lastBottom = bottom
            }
        }
    }
    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        val attacherListener = this.zoomListener
        return attacherListener != null && attacherListener.onTap()
    }
    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        if (view == null) {
            return false
        }
        var handled = false
        val action = event.getActionMasked()
        if (action == 0) {
            this.blockParent = false
            if (this.allowParentIntercept && view.getParent() != null) {
                view.getParent().requestDisallowInterceptTouchEvent(true)
            }
            cancelFling()
        } else if (action == 1 || action == 3) {
            if (currentScale() < 1.0f) {
                // zoomed out below fit: animate back to scale 1
                checkMatrixBounds()
                val rect = displayRectOf(currentMatrix())
                if (rect != null) {
                    view.post(
                        AnimatedZoomRunnable(
                            currentScale(),
                            1.0f,
                            rect.centerX(),
                            rect.centerY()
                        )
                    )
                    handled = true
                }
            }
        } else if (action == 5) {
            if (this.zoomListener == null || !this.zoomListener!!.canScale()) {
                this.blockParent = true
            }
        }
        val result: Boolean
        if (this.zoomListener == null || !this.zoomListener!!.canScale()) {
            if (this.tapDetector != null && this.tapDetector!!.onTouchEvent(event)) {
                handled = true
            }
            if (this.scaleDetector != null) {
                this.scaleDetector!!.onTouch(event)
                result = true
            } else {
                result = handled
            }
        } else {
            result = true
        }
        if (this.zoomListener != null && !this.blockParent && !this.settling && !this.isScaling) {
            this.zoomListener!!.onTouch(event, false)
        }
        return result
    }

    fun atTop(): Boolean {
        if (isZoomed()) return false
        val imageViewM = viewOrRelease() ?: return false
        if (imageViewM.getHeight() <= frameHeight()) return false
        val rectF = this.displayRect
        val rectHeight = rectF.bottom - rectF.top
        val viewHeight = frameHeight().toFloat()
        val top = rectF.top
        if (rectHeight >= viewHeight) {
            return abs(top) < 1.0f
        }
        return abs(top - ((viewHeight - rectHeight) / 2.0f)) < 1.0f
    }

    fun isZoomed(): Boolean {
        val scale = currentScale()
        return scale > 1.0f || scale < 0.9f
    }

    fun detach() {
        this.sizeFrame = null
        this.attachedView = null
        val weakReference = this.viewRef
        if (weakReference != null) {
            val imageView = weakReference.get() as ImageView?
            if (imageView != null) {
                imageView.setOnTouchListener(null)
                imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this)
            }
            this.viewRef = null
        }
        this.tapDetector = null
        this.scaleDetector = null
        this.zoomListener = null
    }

    fun updateBaseMatrix(imageView: ImageView?) {
        if (imageView == null) return
        val drawable = imageView.getDrawable() ?: return
        val intrinsicWidth = drawable.getIntrinsicWidth()
        val intrinsicHeight = drawable.getIntrinsicHeight()
        if (intrinsicWidth == 0 || intrinsicHeight == 0) {
            return
        }
        val width = imageView.getWidth().toFloat()
        val height = imageView.getHeight().toFloat()
        val imageWidth = intrinsicWidth.toFloat()
        val scaleX = width / imageWidth
        val imageHeight = intrinsicHeight.toFloat()
        val scaleY = height / imageHeight
        val matrix = this.baseMatrix
        matrix.reset()
        val scaleType = this.zoomScaleType
        if (scaleType == ScaleType.CENTER) {
            matrix.postTranslate((width - imageWidth) / 2.0f, (height - imageHeight) / 2.0f)
        } else if (scaleType == ScaleType.CENTER_CROP) {
            val fillScale = max(scaleX, scaleY)
            matrix.postScale(fillScale, fillScale)
            matrix.postTranslate((width - (imageWidth * fillScale)) / 2.0f, (height - (imageHeight * fillScale)) / 2.0f)
        } else if (scaleType == ScaleType.CENTER_INSIDE) {
            val fitScale = min(1.0f, min(scaleX, scaleY))
            matrix.postScale(fitScale, fitScale)
            matrix.postTranslate((width - (imageWidth * fitScale)) / 2.0f, (height - (imageHeight * fitScale)) / 2.0f)
        } else {
            val rectF = RectF(0.0f, 0.0f, imageWidth, imageHeight)
            val viewRect = RectF(0.0f, 0.0f, width, height)
            val zoomScaleType = this.zoomScaleType
            if (zoomScaleType == ScaleType.FIT_CENTER) {
                matrix.setRectToRect(rectF, viewRect, Matrix.ScaleToFit.CENTER)
            } else if (zoomScaleType == ScaleType.FIT_START) {
                matrix.setRectToRect(rectF, viewRect, Matrix.ScaleToFit.START)
            } else if (zoomScaleType == ScaleType.FIT_END) {
                matrix.setRectToRect(rectF, viewRect, Matrix.ScaleToFit.END)
            } else if (zoomScaleType == ScaleType.FIT_XY) {
                matrix.setRectToRect(rectF, viewRect, Matrix.ScaleToFit.FILL)
            }
        }
        this.suppMatrix.reset()
        refreshDisplay(false)
    }

    fun applyMatrix(matrix: Matrix, fromUser: Boolean) {
        val imageViewM = viewOrRelease()
        if (imageViewM == null || imageViewM.getScaleType() != ScaleType.MATRIX) {
            return
        }
        imageViewM.setImageMatrix(matrix)
        if (this.pausedA || this.pausedB) return
        this.zoomListener?.onMatrixChanged(displayRectOf(matrix), fromUser)
    }

    fun resetMatrix() {
        val imageViewM = viewOrRelease()
        if (imageViewM == null) {
            return
        }
        imageViewM.setScaleType(ScaleType.MATRIX)
        updateBaseMatrix(imageViewM)
    }

    constructor(imageView: ImageView, attacherListener: AttacherListener?) {
        this.baseMatrix = Matrix()
        this.drawMatrixBuf = Matrix()
        this.suppMatrix = Matrix()
        this.displayRect = RectF()
        this.matrixValues = FloatArray(9)
        val scaleType = ScaleType.FIT_CENTER
        this.zoomScaleType = scaleType
        this.scrollEdge = 0
        attach(imageView, scaleType, attacherListener)
    }
}
