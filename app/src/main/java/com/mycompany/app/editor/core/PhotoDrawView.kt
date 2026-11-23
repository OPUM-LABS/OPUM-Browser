package com.mycompany.app.editor.core

import android.content.Context
import com.mycompany.app.pref.store.ReadSettings
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefRead
import java.util.Stack

@Suppress("deprecation")
class PhotoDrawView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    @JvmField
    var hostContext: Context? = null
    @JvmField
    var drawListener: PhotoDrawListener? = null
    @JvmField
    var penSize: Float = 0f
    @JvmField
    var penColor: Int = 0
    @JvmField
    var penAlpha: Int = 0
    @JvmField
    var erasing: Boolean = false
    @JvmField
    var eraseWidth: Float = 0f
    @JvmField
    var drawPaint: Paint? = null
    @JvmField
    var drawPath: Path? = null
    @JvmField
    var drawnLines: Stack<SaveLine>? = null
    @JvmField
    var redoLines: Stack<SaveLine>? = null
    @JvmField
    var moved: Boolean = false
    @JvmField
    var touchX: Float = 0f
    @JvmField
    var touchY: Float = 0f
    @JvmField
    var drawEnabled: Boolean = false

    internal inner class DrawingTask : Runnable {
        override fun run() {
            val photoDrawListener = this@PhotoDrawView.drawListener
            if (photoDrawListener != null) {
                photoDrawListener.onDrawing(false)
            }
        }
    }

    interface PhotoDrawListener {
        fun onDrawing(isDrawing: Boolean)

        fun isDrawMode(): Boolean

        fun canDraw(): Boolean
    }

    class SaveLine {
        @JvmField var path: Path? = null
        @JvmField var paint: Paint? = null
    }

    fun setup(editorActivity: Context?) {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
        this.hostContext = editorActivity
        this.penSize = ReadSettings.penSize.value.toFloat()
        this.penColor = ReadSettings.penColor.value
        this.penAlpha = ReadSettings.penAlpha.value
        this.eraseWidth = ReadSettings.eraseSize.value.toFloat()
        this.drawPath = Path()
        val paint = Paint()
        this.drawPaint = paint
        paint.setAntiAlias(true)
        this.drawPaint!!.setStyle(Paint.Style.STROKE)
        this.drawPaint!!.setStrokeCap(Paint.Cap.ROUND)
        this.drawPaint!!.setStrokeJoin(Paint.Join.ROUND)
        this.drawPaint!!.setStrokeWidth(MainUtil.dpToPx(this.hostContext, this.penSize))
        this.drawPaint!!.setColor(this.penColor)
        this.drawPaint!!.setAlpha(MainUtil.alphaFromPercent(this.penAlpha))
        this.drawPaint!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
        this.drawnLines = Stack()
        this.redoLines = Stack()
    }

    public override fun onDraw(canvas: Canvas) {
        val stack: Stack<SaveLine>? = this.drawnLines
        if (stack == null) {
            return
        }
        for (saveLine in stack) {
            canvas.drawPath(saveLine.path!!, saveLine.paint!!)
        }
        canvas.drawPath(this.drawPath!!, this.drawPaint!!)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val path = this.drawPath ?: return false
        val action = event.getAction()
        val masked = event.getActionMasked() and action
        if (masked == 0) {
            val listener = this.drawListener
            if (listener != null && !listener.canDraw()) {
                this.moved = true
                this.touchX = event.getX()
                this.touchY = event.getY()
                val isDrawMode = listener.isDrawMode()
                this.drawEnabled = isDrawMode
                if (!isDrawMode) {
                    this.redoLines!!.clear()
                    path.reset()
                    path.moveTo(this.touchX, this.touchY)
                    this.invalidate()
                }
                listener.onDrawing(true)
                return true
            }
            return false
        }
        if (masked == 2) {
            if (!this.moved) {
                return true
            }
            val touchX = event.getX()
            val touchY = event.getY()
            val dist = MainUtil.pointDistance(this.touchX, touchX, this.touchY, touchY)
            if (this.drawEnabled) {
                if (dist > 8.0f) {
                    this.drawEnabled = false
                    this.redoLines!!.clear()
                    path.reset()
                    path.moveTo(this.touchX, this.touchY)
                } else {
                    return true
                }
            } else if (dist <= 4.0f) {
                return true
            }
            val px = this.touchX
            val py = this.touchY
            path.quadTo(px, py, (touchX + px) / 2.0f, (touchY + py) / 2.0f)
            this.touchX = touchX
            this.touchY = touchY
            this.invalidate()
            return true
        }
        if (masked != 1 && masked != 3) {
            if (masked == 6 && ((action and 0xFF00) shr 8) == 0) {
                this.moved = false
            }
            return true
        }
        this.moved = false
        if (!this.drawEnabled) {
            path.lineTo(this.touchX, this.touchY)
            val saveLine = SaveLine()
            saveLine.path = Path(path)
            saveLine.paint = Paint(this.drawPaint)
            this.drawnLines!!.push(saveLine)
            path.reset()
            this.invalidate()
        }
        this.drawEnabled = false
        if (this.drawListener != null) {
            this.post(DrawingTask())
        }
        return true
    }

    fun setEraseMode(erasing: Boolean) {
        if (this.erasing == erasing) {
            return
        }
        this.erasing = erasing
        val paint = this.drawPaint!!
        val editorActivity = this.hostContext
        if (erasing) {
            paint.setStrokeWidth(MainUtil.dpToPx(editorActivity, this.eraseWidth))
            this.drawPaint!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.CLEAR))
        } else {
            paint.setStrokeWidth(MainUtil.dpToPx(editorActivity, this.penSize))
            this.drawPaint!!.setColor(this.penColor)
            this.drawPaint!!.setAlpha(MainUtil.alphaFromPercent(this.penAlpha))
            this.drawPaint!!.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_OVER))
        }
    }

    fun setEraseSize(size: Int) {
        val paint = this.drawPaint
        if (paint != null) {
            val eraseWidth = size.toFloat()
            if (this.eraseWidth == eraseWidth) {
                return
            }
            this.eraseWidth = eraseWidth
            paint.setStrokeWidth(MainUtil.dpToPx(this.hostContext, eraseWidth))
        }
    }

    fun setListener(photoDrawListener: PhotoDrawListener?) {
        this.drawListener = photoDrawListener
    }
}
