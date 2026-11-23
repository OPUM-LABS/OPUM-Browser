package com.mycompany.app.view

import android.view.View

import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import ch.opumlabs.opumbrowser.R
import ch.opumlabs.opumbrowser.R as AppR
import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.MotionEvent

import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil.pointDistance
import com.mycompany.app.main.MainUtil.maxTextureSize
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.web.WebNestView
import com.mycompany.app.web.WebViewActivity
import kotlin.Boolean
import kotlin.Byte
import kotlin.Exception
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.also
import kotlin.collections.minus
import kotlin.compareTo
import kotlin.math.abs
import kotlin.math.min
import kotlin.sequences.minus
import kotlin.unaryMinus

@Suppress("deprecation")
/**
 * The page container of the browser screen: since the bars and every overlay moved into the Compose
 * overlay stack it only holds the page (`WebNestLayout`) and the load view, so it is a plain FrameLayout (it was a
 * `CoordinatorLayout` laying the bars out by behaviours). Keeps its own touch handling (pull trigger, page move).
 */
class MyWebCoord(webViewActivity: WebViewActivity?) : android.widget.FrameLayout(webViewActivity!!) {
    @JvmField
    var captureHeight: Int = 0
    @JvmField
    var captureStart: Boolean = false
    @JvmField
    var captureMeasured: Boolean = false
    @JvmField
    var captureDrawn: Boolean = false
    @JvmField
    var pullActive: Boolean = false
    @JvmField
    var pullArmed: Boolean = false
    @JvmField
    var pullState: Byte = 0
    @JvmField
    var seekListener: DialogSeekListener? = null
    @JvmField
    var iconHalf: Int = 0
    @JvmField
    var iconRadius: Int = 0
    @JvmField
    var iconFill: Paint? = null
    @JvmField
    var iconStroke: Paint? = null
    @JvmField
    var iconDrawable: Drawable? = null
    @JvmField
    var touchX: Int = 0
    @JvmField
    var touchY: Int = 0
    @JvmField
    var dragOffsetX: Int = 0
    @JvmField
    var dragOffsetY: Int = 0
    @JvmField
    var iconWidth: Int = 0
    @JvmField
    var iconHeight: Int = 0
    @JvmField
    var areaWidth: Int = 0
    @JvmField
    var areaHeight: Int = 0
    @JvmField
    var animFromX: Int = 0
    @JvmField
    var animFromY: Int = 0
    @JvmField
    var animToX: Int = 0
    @JvmField
    var animToY: Int = 0
    @JvmField
    var iconShiftX: Int = 0
    @JvmField
    var iconShiftY: Int = 0
    @JvmField
    var iconX: Int = 0
    @JvmField
    var iconY: Int = 0
    @JvmField
    var circleX: Int = 0
    @JvmField
    var circleY: Int = 0
    @JvmField
    var snapSide: Byte = 0
    @JvmField
    var moveAnim: ValueAnimator? = null
    @JvmField
    var closeFill: Paint? = null
    @JvmField
    var closeRect: RectF? = null
    @JvmField
    var closeWidth: Float = 0f
    @JvmField
    var closeStroke: Paint? = null
    @JvmField
    var closeStrokeRect: RectF? = null
    @JvmField
    var closeText: String? = null
    @JvmField
    var closeTextPaint: Paint? = null
    @JvmField
    var closeTextBounds: Rect? = null
    @JvmField
    var closeTextX: Float = 0f
    @JvmField
    var closeTextY: Float = 0f
    @JvmField
    var closeShift: Float = 0f
    @JvmField
    var closeShiftLast: Float = 0f
    @JvmField
    var animDistance: Float = 0f
    @JvmField
    var animValue: Float = 0f
    @JvmField
    var captureListener: MyCaptureListener? = null
    @JvmField
    var animPosted: Boolean = false
    @JvmField
    var pullListener: MyTouchListener? = null
    @JvmField
    var animRunnable: Runnable?

    internal inner class CaptureTask : Runnable {
        override fun run() {
            val myWebCoord = this@MyWebCoord
            val myCaptureListener = myWebCoord.captureListener
            if (myCaptureListener != null) {
                myCaptureListener.onCapture()
                myWebCoord.captureListener = null
            }
        }
    }

    internal inner class RequestLayoutTask : Runnable {
        override fun run() {
            this@MyWebCoord.requestLayout()
        }
    }

    internal inner class MoveAnimUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val myWebCoord = this@MyWebCoord
            if (myWebCoord.moveAnim != null) {
                val animatedValue = (valueAnimator.getAnimatedValue() as Float)
                if (myWebCoord.moveAnim != null) {
                    myWebCoord.animValue = animatedValue
                    if (myWebCoord.animPosted) {
                        return
                    }
                    myWebCoord.animPosted = true
                    MainApp.postToMain(myWebCoord.getContext(), myWebCoord.animRunnable!!)
                }
            }
        }
    }

    internal inner class ClearIconAnimListener : Animator.AnimatorListener {
        override fun onAnimationCancel(animator: Animator) {
            val myWebCoord = this@MyWebCoord
            if (myWebCoord.moveAnim == null) {
                return
            }
            myWebCoord.moveAnim = null
            myWebCoord.clearIcon()
        }

        override fun onAnimationEnd(animator: Animator) {
            val myWebCoord = this@MyWebCoord
            if (myWebCoord.moveAnim == null) {
                return
            }
            MainApp.postToMain(myWebCoord.getContext(), myWebCoord.FinishMoveTask())
        }

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    }

    internal inner class SetValAnimMoveTask : Runnable {
        override fun run() {
            val myWebCoord = this@MyWebCoord
            myWebCoord.animPosted = false
            if (myWebCoord.moveAnim == null) {
                return
            }
            myWebCoord.setValAnimMove(myWebCoord.animValue)
        }
    }

    internal inner class FinishMoveTask : Runnable {
        override fun run() {
            val myWebCoord = this@MyWebCoord
            if (myWebCoord.moveAnim == null) {
                return
            }
            myWebCoord.moveAnim = null
            myWebCoord.clearIcon()
        }
    }

    internal inner class NotifyCaptureTask : Runnable {
        override fun run() {
            val myWebCoord = this@MyWebCoord
            val myCaptureListener = myWebCoord.captureListener
            if (myCaptureListener != null) {
                myCaptureListener.onCapture()
                myWebCoord.captureListener = null
            }
        }
    }

    interface MyCaptureListener {
        fun onCapture()
    }

    interface MyTouchListener {
        fun onPull(x: Float, y: Float, action: Int): Boolean
    }

    init {
        this.animRunnable = SetValAnimMoveTask()
    }

    fun setValAnimMove(animValue: Float) {
        val progress = animValue / this.animDistance
        val toX = this.animToX
        val x = Math.round(toX - ((toX - this.animFromX) * progress))
        val toY = this.animToY
        val y = Math.round(toY - ((toY - this.animFromY) * progress))
        if (x == this.iconX && y == this.iconY) {
            return
        }
        moveIcon(x, y)
    }

    fun clearCloseHint() {
        if (this.closeFill == null) {
            return
        }
        this.closeFill = null
        this.closeRect = null
        this.closeStroke = null
        this.closeStrokeRect = null
        this.closeText = null
        this.closeTextPaint = null
        this.closeTextBounds = null
        invalidate()
    }

    fun isPullActive(): Boolean {
        if (this.pullState.toInt() == 1) {
            return false
        }
        return this.pullActive
    }

    fun startCapture(webNestView: WebNestView?, coverTime: Long, myCaptureListener: MyCaptureListener?) {
        var pageHeight: Int
        var coverHeight: Int = 0
        if (webNestView != null) {
            try {
                pageHeight = webNestView.computeVerticalScrollRange()
                try {
                    var width = webNestView.getWidth()
                    if (width == 0) {
                        width = getWidth()
                    }
                    if (coverTime > 0 && pageHeight > ((coverTime / ((width.toLong()) shl 5)).toInt()
                            .also { coverHeight = it })
                    ) {
                        pageHeight = coverHeight
                    }
                    val scrollArea = width * pageHeight
                    val maxTexture = MainUtil.maxTextureSize()
                    val area = maxTexture * maxTexture
                    if (scrollArea >= area) {
                        val maxHeight = area / width
                        pageHeight = maxHeight - (maxHeight / 10)
                    }
                    if (pageHeight <= getHeight()) {
                        this.captureListener = myCaptureListener
                        this.captureHeight = 0
                        this.captureStart = false
                        this.captureMeasured = false
                        this.captureDrawn = false
                        post(CaptureTask())
                        return
                    }
                } catch (unused: Exception) {
                }
            } catch (unused2: Exception) {
                pageHeight = 0
            }
        } else {
            pageHeight = 0
        }
        if (this.captureHeight == pageHeight) {
            return
        }
        this.captureHeight = pageHeight
        if (pageHeight > 0) {
            this.captureListener = myCaptureListener
            this.captureStart = true
            this.captureMeasured = false
            this.captureDrawn = false
        } else {
            this.captureListener = null
            this.captureStart = false
            this.captureMeasured = false
            this.captureDrawn = false
        }
        post(RequestLayoutTask())
    }

    fun moveIcon(x: Int, y: Int) {
        var x = x
        var y = y
        val drawable = this.iconDrawable
        if (drawable == null) {
            return
        }
        val iconWidth = this.iconWidth
        val right = x + iconWidth
        val areaWidth = this.areaWidth
        if (right > areaWidth) {
            x = areaWidth - iconWidth
        }
        val iconHeight = this.iconHeight
        val bottom = y + iconHeight
        val areaHeight = this.areaHeight
        if (bottom > areaHeight) {
            y = areaHeight - iconHeight
        }
        if (x < 0) {
            x = 0
        }
        if (y < 0) {
            y = 0
        }
        this.iconX = x
        this.iconY = y
        val iconHalf = this.iconHalf
        val shiftX = this.iconShiftX
        this.circleX = x + iconHalf + shiftX
        val shiftY = this.iconShiftY
        this.circleY = iconHalf + y + shiftY
        val iconSize = this.iconRadius
        val inset = (MainApp.dp48 - iconSize) / 2
        val left = x + inset + shiftX
        val top = inset + y + shiftY
        drawable.setBounds(left, top, left + iconSize, iconSize + top)
        invalidate()
    }

    fun updateCloseHint(tabX: Float, layoutRtl: Boolean) {
        var tabX = tabX
        if (layoutRtl) {
            tabX = getWidth() - tabX
        }
        var closeShift = 0.0f
        if (tabX < 0.0f) {
            closeShift = -1.0f
        } else {
            val dp40Px = MainApp.dp40
            if (tabX < dp40Px) {
                closeShift = (tabX / dp40Px) - 1.0f
            }
        }
        if (this.closeFill != null) {
            var shift = this.closeWidth * closeShift
            this.closeShift = shift
            if (layoutRtl) {
                shift = -shift
                this.closeShift = shift
            }
            if ((this.closeShiftLast).compareTo(shift) != 0) {
                this.closeShiftLast = this.closeShift
                invalidate()
                return
            }
            return
        }
        val fillPaint = Paint()
        this.closeFill = fillPaint
        fillPaint.setAntiAlias(true)
        this.closeFill!!.setStyle(Paint.Style.FILL)
        this.closeFill!!.setColor(if (AppState.darkWeb) -14606047 else -16777216)
        val strokeWidth = MainApp.dp1.toFloat()
        val strokePaint = Paint()
        this.closeStroke = strokePaint
        strokePaint.setAntiAlias(true)
        this.closeStroke!!.setStyle(Paint.Style.STROKE)
        this.closeStroke!!.setStrokeWidth(strokeWidth)
        this.closeStroke!!.setColor(-5197648)
        val textPaint = Paint()
        this.closeTextPaint = textPaint
        textPaint.setAntiAlias(true)
        this.closeTextPaint!!.setColor(if (AppState.darkWeb) -328966 else -1)
        this.closeTextPaint!!.setTextSize(MainApp.dp16.toFloat())
        this.closeRect = RectF()
        this.closeStrokeRect = RectF()
        this.closeTextBounds = Rect()
        val text = getContext().getString(AppR.string.delete_tab)
        this.closeText = text
        this.closeTextPaint!!.getTextBounds(text, 0, text.length, this.closeTextBounds)
        val halfTextWidth = this.closeTextBounds!!.width() / 2.0f
        val halfTextHeight = this.closeTextBounds!!.height() / 2.0f
        val hintHalfWidth = MainApp.dp16 + halfTextWidth
        val dp8Px = MainApp.dp8
        val hintHeight = dp8Px + halfTextHeight
        var width = hintHalfWidth - dp8Px
        val height = getHeight() / 2.0f
        if (layoutRtl) {
            width = getWidth() - width
            this.closeRect!!.set(width - hintHalfWidth, height - hintHeight, width + hintHalfWidth + MainApp.dp8, hintHeight + height)
        } else {
            this.closeRect!!.set((width - hintHalfWidth) - MainApp.dp8, height - hintHeight, width + hintHalfWidth, hintHeight + height)
        }
        val hintWidth = hintHalfWidth * 2.0f
        this.closeWidth = hintWidth
        val strokeRect = this.closeStrokeRect
        val hintRect = this.closeRect
        strokeRect!!.left = hintRect!!.left + strokeWidth
        strokeRect.top = hintRect.top + strokeWidth
        strokeRect.right = hintRect.right + strokeWidth
        strokeRect.bottom = hintRect.bottom + strokeWidth
        val textBounds = this.closeTextBounds
        this.closeTextX = (width - halfTextWidth) - textBounds!!.left
        this.closeTextY = (height - halfTextHeight) - textBounds.top
        var initialShift = hintWidth * closeShift
        this.closeShift = initialShift
        if (layoutRtl) {
            initialShift = -initialShift
            this.closeShift = initialShift
        }
        this.closeShiftLast = initialShift
        if (abs(initialShift) < this.closeWidth) {
            invalidate()
        }
    }

    public override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (this.captureStart && this.captureMeasured) {
            this.captureStart = false
            this.captureMeasured = false
            this.captureDrawn = true
            invalidate()
        } else if (this.captureDrawn) {
            this.captureStart = false
            this.captureMeasured = false
            this.captureDrawn = false
            post(NotifyCaptureTask())
        }
        if (this.iconDrawable != null) {
            val iconFill = this.iconFill
            if (iconFill != null) {
                canvas.drawCircle(this.circleX.toFloat(), this.circleY.toFloat(), this.iconRadius.toFloat(), iconFill)
            }
            val iconStroke = this.iconStroke
            if (iconStroke != null) {
                canvas.drawCircle(
                    this.circleX.toFloat(),
                    this.circleY.toFloat(),
                    this.iconRadius - (MainApp.dp1 / 2.0f),
                    iconStroke
                )
            }
            this.iconDrawable!!.draw(canvas)
        }
        if (this.closeFill == null || this.closeRect == null || this.closeStroke == null || this.closeStrokeRect == null || this.closeTextPaint == null || this.closeTextBounds == null || abs(
                this.closeShift
            ) >= this.closeWidth
        ) {
            return
        }
        canvas.translate(this.closeShift, 0.0f)
        val closeRect = this.closeRect
        val radius = MainApp.dp16.toFloat()
        canvas.drawRoundRect(closeRect!!, radius, radius, this.closeFill!!)
        val closeStrokeRect = this.closeStrokeRect
        val strokeRadius = MainApp.dp16.toFloat()
        canvas.drawRoundRect(closeStrokeRect!!, strokeRadius, strokeRadius, this.closeStroke!!)
        canvas.drawText(this.closeText!!, this.closeTextX, this.closeTextY, this.closeTextPaint!!)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val snapZone: Int
        val iconCenter: Int
        val areaWidth: Int
        val toX: Int
        val distance: kotlin.Float
        val actionMasked = event!!.getActionMasked()
        if (actionMasked == 0) {
            val x = event.getX()
            val y = event.getY()
            this.touchX = Math.round(x)
            this.touchY = Math.round(y)
        } else if (actionMasked == 1) {
            if (this.iconDrawable != null && this.moveAnim == null) {
                snapZone = Math.round(this.areaWidth / 3.0f)
                val iconX = this.iconX
                this.animFromX = iconX
                this.animFromY = this.iconY
                iconCenter = iconX + this.iconHalf
                if (iconCenter < snapZone) {
                    this.snapSide = 1.toByte()
                    toX = MainApp.dp4
                    this.animToX = toX
                } else {
                    areaWidth = this.areaWidth
                    if (iconCenter > areaWidth - snapZone) {
                        this.snapSide = 3.toByte()
                        toX = (areaWidth - this.iconWidth) - MainApp.dp4
                        this.animToX = toX
                    } else {
                        this.snapSide = 2.toByte()
                        toX = Math.round((areaWidth - this.iconWidth) / 2.0f)
                        this.animToX = toX
                    }
                }
                val toY = (this.areaHeight - this.iconHeight) - MainApp.dp8
                this.animToY = toY
                distance = pointDistance(this.animFromX.toFloat(), toX.toFloat(), this.animFromY.toFloat(), toY.toFloat())
                if (distance <= 0.0f) {
                    clearIcon()
                } else {
                    this.animDistance = distance
                    this.animValue = distance
                    this.animPosted = false
                    val anim = ValueAnimator.ofFloat(distance, 0.0f)
                    this.moveAnim = anim
                    anim.setDuration(
                        ((distance * 400.0f) / min(
                            this.areaWidth,
                            this.areaHeight
                        )).toLong()
                    )
                    this.moveAnim!!.addUpdateListener(MoveAnimUpdate())
                    this.moveAnim!!.addListener(ClearIconAnimListener())
                    this.moveAnim!!.start()
                }
            }
            if (this.pullActive && this.pullState.toInt() != 2 && ZoneSettings.pullRefresh.value && this.pullListener != null) {
                this.pullListener!!.onPull(event.getX(), event.getY(), actionMasked)
            }
            this.pullActive = false
            this.pullArmed = false
            this.pullState = 0.toByte()
        } else if (actionMasked != 2) {
            if (actionMasked == 3) {
                if (this.iconDrawable != null) {
                    snapZone = Math.round(this.areaWidth / 3.0f)
                    val iconX = this.iconX
                    this.animFromX = iconX
                    this.animFromY = this.iconY
                    iconCenter = iconX + this.iconHalf
                    if (iconCenter < snapZone) {
                        this.snapSide = 1.toByte()
                        toX = MainApp.dp4
                        this.animToX = toX
                    } else {
                        areaWidth = this.areaWidth
                        if (iconCenter > areaWidth - snapZone) {
                            this.snapSide = 3.toByte()
                            toX = (areaWidth - this.iconWidth) - MainApp.dp4
                            this.animToX = toX
                        } else {
                            this.snapSide = 2.toByte()
                            toX = Math.round((areaWidth - this.iconWidth) / 2.0f)
                            this.animToX = toX
                        }
                    }
                    val toY = (this.areaHeight - this.iconHeight) - MainApp.dp8
                    this.animToY = toY
                    distance = pointDistance(this.animFromX.toFloat(), toX.toFloat(), this.animFromY.toFloat(), toY.toFloat())
                    if (distance <= 0.0f) {
                        clearIcon()
                    } else {
                        this.animDistance = distance
                        this.animValue = distance
                        this.animPosted = false
                        val anim = ValueAnimator.ofFloat(distance, 0.0f)
                        this.moveAnim = anim
                        anim.setDuration(
                            ((distance * 400.0f) / min(
                                this.areaWidth,
                                this.areaHeight
                            )).toLong()
                        )
                        this.moveAnim!!.addUpdateListener(MoveAnimUpdate())
                        this.moveAnim!!.addListener(ClearIconAnimListener())
                        this.moveAnim!!.start()
                    }
                }
                if (this.pullActive) {
                    this.pullListener!!.onPull(event.getX(), event.getY(), actionMasked)
                }
                this.pullActive = false
                this.pullArmed = false
                this.pullState = 0.toByte()
            } else if (actionMasked == 5) {
                cancelPull()
            }
        } else if (this.iconDrawable != null) {
            val moveX = event.getX()
            val moveY = event.getY()
            if (pointDistance(this.touchX.toFloat(), moveX, this.touchY.toFloat(), moveY) >= MainApp.dp4 / 2.0f) {
                this.touchX = Math.round(moveX)
                val newTouchY = Math.round(moveY)
                this.touchY = newTouchY
                moveIcon(this.touchX + this.dragOffsetX, newTouchY + this.dragOffsetY)
                if (this.pullActive && this.pullState.toInt() != 2 && ZoneSettings.pullRefresh.value && this.pullListener != null && this.pullListener!!.onPull(
                        event.getX(),
                        event.getY(),
                        actionMasked
                    )
                ) {
                    this.pullState = 1.toByte()
                }
            }
        } else if (this.pullActive) {
            this.pullState = 1.toByte()
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val captureHeight = this.captureHeight
        if (captureHeight > 0) {
            this.captureMeasured = true
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(captureHeight, View.MeasureSpec.EXACTLY))
        } else {
            this.captureMeasured = false
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val armed: Boolean
        val actionMasked = event!!.getActionMasked()
        if (actionMasked == 0) {
            if (BrowserSettings.showStatus.value) {
                this.pullArmed = true
                armed = true
            } else {
                armed = event.getY() > (MainApp.dp16.toFloat())
                this.pullArmed = armed
            }
            if (armed && !this.pullActive) {
                this.pullActive = true
                this.pullState = 0.toByte()
                return true
            }
        } else if (actionMasked == 2 && this.pullArmed && !this.pullActive) {
            this.pullActive = true
            this.pullState = 0.toByte()
        }
        return true
    }

    fun setMoveListener(dialogSeekListener: DialogSeekListener?) {
        this.seekListener = dialogSeekListener
    }

    fun setTouchListener(myTouchListener: MyTouchListener?) {
        this.pullListener = myTouchListener
    }

    fun cancelPull() {
        if (this.pullActive) {
            val myTouchListener: MyTouchListener? = if (this.pullState.toInt() == 1 && ZoneSettings.pullRefresh.value) this.pullListener else null
            if (this.pullState.toInt() == 1 && ZoneSettings.pullRefresh.value && myTouchListener != null) {
                myTouchListener.onPull(0.0f, 0.0f, 3)
            }
            this.pullState = 2.toByte()
        }
    }

    fun clearIcon() {
        if (this.iconDrawable != null) {
            this.iconDrawable = null
            this.iconFill = null
            this.iconStroke = null
            this.moveAnim = null
            invalidate()
            val dialogSeekListener = this.seekListener
            if (dialogSeekListener != null) {
                dialogSeekListener.onValue(this.snapSide.toInt())
            }
        }
    }

    companion object {
    }
}
