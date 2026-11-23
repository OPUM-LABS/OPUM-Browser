package com.mycompany.app.crop

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import android.widget.ImageView
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Image view with a movable crop frame. The frame's sides live in [Edge] (LEFT, TOP, RIGHT,
 * BOTTOM).
 */
class CropImageView(context: Context) : ImageView(context) {
    /** Frame outline. */
    @JvmField var borderPaint: Paint? = null

    /** Corner marks, outer stroke. */
    @JvmField var cornerPaint: Paint? = null

    /** Corner marks, inner stroke. */
    @JvmField var guidePaint: Paint? = null

    /** Dimming outside the frame. */
    @JvmField var dimPaint: Paint? = null

    /** Where the image is drawn; null until the next draw computes it. */
    @JvmField var cropRect: RectF? = null

    /** Offset from the touch point to the dragged handle. */
    @JvmField var touchOffset: PointF? = PointF()

    /** The handle being dragged. */
    @JvmField var activeHandle: Handle? = null

    /** Aspect ratio [aspectX]:[aspectY] is enforced while dragging. */
    @JvmField var fixedAspect: Boolean = false
    @JvmField var aspectX: Int = 1
    @JvmField var aspectY: Int = 1

    /** Drawing enabled (false once released). */
    @JvmField var alive: Boolean = true

    private val touchRadius = MainUtil.dpToPx(context, 24.0f)
    private val markOffset: Float
    private val markLength: Float
    private val markInset: Float
    private val snapRadius: Float

    init {
        val size = MainApp.dp16.toFloat()
        markOffset = size / 8.0f
        markLength = size - markOffset
        snapRadius = markOffset
        val markStroke = markLength / 4.0f
        markInset = markStroke / 4.0f
        borderPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = markOffset
            color = 1895825407
        }
        dimPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = -1342177280
        }
        cornerPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = markStroke
            color = -1342177280
        }
        guidePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = markStroke - markInset
            color = -1
        }
    }

    /** The drawn image's bounds inside the view, or null without an image. */
    private val bitmapRect: RectF?
        get() {
            val drawable = drawable ?: return null
            val width = drawable.intrinsicWidth
            val height = drawable.intrinsicHeight
            if (width == 0 || height == 0) return null
            val values = FloatArray(9)
            imageMatrix.getValues(values)
            val left = max(values[2], 0.0f)
            val top = max(values[5] + paddingTop, paddingTop.toFloat())
            return RectF(left, top, min(width * values[0] + left, getWidth().toFloat()),
                min(height * values[4] + top, (getHeight() - paddingBottom).toFloat()))
        }

    /** Integer division, as in the original. */
    private val targetAspectRatio: Float
        get() = (aspectX / aspectY).toFloat()

    /** Places the frame in [rectF] (respecting the aspect ratio); unless [full], 10% inside it. */
    fun resetCropRect(rectF: RectF?, full: Boolean) {
        if (rectF == null) return
        var left: Float
        var top: Float
        var right: Float
        var bottom: Float
        if (!fixedAspect) {
            left = rectF.left
            top = rectF.top
            right = rectF.right
            bottom = rectF.bottom
        } else if (rectF.width() / rectF.height() > targetAspectRatio) {
            val half = targetAspectRatio * rectF.height() / 2.0f
            left = rectF.centerX() - half
            top = rectF.top
            right = rectF.centerX() + half
            bottom = rectF.bottom
        } else {
            val half = rectF.width() / targetAspectRatio / 2.0f
            left = rectF.left
            top = rectF.centerY() - half
            right = rectF.right
            bottom = rectF.centerY() + half
        }
        val insetX = if (full) 0.0f else (right - left) * 0.1f
        val insetY = if (full) 0.0f else (bottom - top) * 0.1f
        Edge.b.coordinate = left + insetX
        Edge.c.coordinate = top + insetY
        Edge.d.coordinate = right - insetX
        Edge.e.coordinate = bottom - insetY
    }

    /** The part of the bitmap inside the frame, or null. */
    val croppedImage: Bitmap?
        get() {
            val drawable = drawable as? BitmapDrawable ?: return null
            val values = FloatArray(9)
            imageMatrix.getValues(values)
            val scaleX = values[0]
            val scaleY = values[4]
            val transX = max(values[2], 0.0f)
            val transY = max(values[5] + paddingTop, paddingTop.toFloat())
            val bitmap = drawable.bitmap
            if (!MainUtil.isBitmapOk(bitmap)) return null
            val width = bitmap.width
            val height = bitmap.height
            var x = Math.round((Edge.b.coordinate - transX) / scaleX)
            var y = Math.round((Edge.c.coordinate - transY) / scaleY)
            if (x < 0 || x >= width) x = 0
            if (y < 0 || y >= height) y = 0
            val w = max(Math.round(min((Edge.d.coordinate - Edge.b.coordinate) / scaleX, (width - x).toFloat())), 1)
            val h = max(Math.round(min((Edge.e.coordinate - Edge.c.coordinate) / scaleY, (height - y).toFloat())), 1)
            if (x + w > width || y + h > height) return null
            return try {
                Bitmap.createBitmap(bitmap, x, y, w, h)
            } catch (e: Exception) {
                null
            } catch (e: OutOfMemoryError) {
                null
            }
        }

    override fun invalidate() {
        if (alive) super.invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (!alive) return
        try {
            super.onDraw(canvas)
        } catch (exception: Exception) {
        }
        if (cropRect == null) {
            val rect = bitmapRect ?: return
            cropRect = rect
            resetCropRect(rect, false)
        }
        val rect = cropRect
        val left = Edge.b.coordinate
        val top = Edge.c.coordinate
        val right = Edge.d.coordinate
        val bottom = Edge.e.coordinate
        val inner = guidePaint
        // Dim everything outside the frame.
        val dim = dimPaint
        if (rect != null && inner != null && dim != null) {
            canvas.drawRect(rect.left, rect.top, rect.right, top, dim)
            canvas.drawRect(rect.left, bottom, rect.right, rect.bottom, dim)
            canvas.drawRect(rect.left, top, left, bottom, dim)
            canvas.drawRect(right, top, rect.right, bottom, dim)
        }
        borderPaint?.let { canvas.drawRect(left, top, right, bottom, it) }
        if (inner == null) return
        // Corner marks: an outer stroke, then a thinner inner one.
        cornerPaint?.let { outer ->
            canvas.drawLine(left, top - markOffset, left, top + markLength, outer)
            canvas.drawLine(left - markOffset, top, left + markLength, top, outer)
            canvas.drawLine(right, top - markOffset, right, top + markLength, outer)
            canvas.drawLine(right + markOffset, top, right - markLength, top, outer)
            canvas.drawLine(left, bottom + markOffset, left, bottom - markLength, outer)
            canvas.drawLine(left - markOffset, bottom, left + markLength, bottom, outer)
            canvas.drawLine(right, bottom + markOffset, right, bottom - markLength, outer)
            canvas.drawLine(right + markOffset, bottom, right - markLength, bottom, outer)
        }
        val off = markOffset - markInset
        val len = markLength - off / 2.0f
        canvas.drawLine(left, top - off, left, top + len, inner)
        canvas.drawLine(left - off, top, left + len, top, inner)
        canvas.drawLine(right, top - off, right, top + len, inner)
        canvas.drawLine(right + off, top, right - len, top, inner)
        canvas.drawLine(left, bottom + off, left, bottom - len, inner)
        canvas.drawLine(left - off, bottom, left + len, bottom, inner)
        canvas.drawLine(right, bottom + off, right, bottom - len, inner)
        canvas.drawLine(right + off, bottom, right - len, bottom, inner)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cropRect = null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val offset = touchOffset ?: return true
                val handle = pressedHandle(event.x, event.y)
                activeHandle = handle
                if (handle != null) {
                    offset.set(handleOffset(handle, event.x, event.y))
                    invalidate()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                val offset = touchOffset
                val handle = activeHandle
                val rect = cropRect
                if (offset != null && handle != null && rect != null) {
                    val x = offset.x + event.x
                    val y = offset.y + event.y
                    if (fixedAspect) {
                        handle.helper.move(x, y, targetAspectRatio, snapRadius, rect)
                    } else {
                        handle.helper.updateCropWindow(x, y, rect, snapRadius)
                    }
                    invalidate()
                }
                parent.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                if (activeHandle != null) {
                    activeHandle = null
                    invalidate()
                }
            }
            else -> return false
        }
        return true
    }

    /** The nearest corner within [touchRadius], else a side or the inside of the frame, else null. */
    private fun pressedHandle(x: Float, y: Float): Handle? {
        val left = Edge.b.coordinate
        val top = Edge.c.coordinate
        val right = Edge.d.coordinate
        val bottom = Edge.e.coordinate
        var handle: Handle? = null
        var nearest = Float.POSITIVE_INFINITY
        for ((corner, cx, cy) in listOf(Triple(Handle.b, left, top), Triple(Handle.c, right, top),
            Triple(Handle.d, left, bottom), Triple(Handle.e, right, bottom))) {
            val distance = HandleUtil.distance(x, y, cx, cy)
            if (distance < nearest) {
                nearest = distance
                handle = corner
            }
        }
        if (nearest <= touchRadius) return handle
        return when {
            x > left && x < right && abs(y - top) <= touchRadius -> Handle.g
            x > left && x < right && abs(y - bottom) <= touchRadius -> Handle.i
            abs(x - left) <= touchRadius && y > top && y < bottom -> Handle.f
            abs(x - right) <= touchRadius && y > top && y < bottom -> Handle.h
            x >= left && x <= right && y >= top && y <= bottom -> Handle.j
            else -> null
        }
    }

    /** Distance from the touch point to the handle's position (0 on the axis a side handle does not move). */
    private fun handleOffset(handle: Handle, x: Float, y: Float): PointF {
        val left = Edge.b.coordinate
        val top = Edge.c.coordinate
        val right = Edge.d.coordinate
        val bottom = Edge.e.coordinate
        return when (handle) {
            Handle.b -> PointF(left - x, top - y)
            Handle.c -> PointF(right - x, top - y)
            Handle.d -> PointF(left - x, bottom - y)
            Handle.e -> PointF(right - x, bottom - y)
            Handle.f -> PointF(left - x, 0.0f)
            Handle.g -> PointF(0.0f, top - y)
            Handle.h -> PointF(right - x, 0.0f)
            Handle.i -> PointF(0.0f, bottom - y)
            Handle.j -> PointF((left + right) / 2.0f - x, (top + bottom) / 2.0f - y)
        }
    }

}
