package com.mycompany.app.pull

import android.graphics.PixelFormat

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.Transformation
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.mycompany.app.web.WebViewActivity
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

@Suppress("deprecation")
class MyMaterialProgressDrawable(
    webViewActivity: WebViewActivity,
    myPullView: MyPullView,
    dark: Boolean
) : Drawable(), Animatable {
    @JvmField
    val animators: ArrayList<Animation> = ArrayList()
    @JvmField
    var mainRing: Ring
    @JvmField
    var ringRotation: Float = 0f
    @JvmField
    var parentView: MyPullView
    @JvmField
    var spinAnim: Animation
    @JvmField
    var rotationCount: Float = 0f
    @JvmField
    var drawWidth: Double
    @JvmField
    var drawHeight: Double
    @JvmField
    var finishing: Boolean = false

    internal inner class StoreOriginalsStep(val outer: MyMaterialProgressDrawable,
                                         val ring: Ring
    ) : Animation() {
        public override fun applyTransformation(interpolatedTime: Float, transformation: Transformation?) {
            val myMaterialProgressDrawable = this.outer
            val finishing = myMaterialProgressDrawable.finishing
            val ring = this.ring
            if (finishing) {
                updateRingColor(interpolatedTime, ring)
                val rotationSteps = (floor((ring.startingRotation / 0.8f).toDouble()) + 1.0).toFloat()
                val radians =
                    Math.toRadians((ring.strokeWidth.toDouble()) / (ring.centerRadius * 6.283185307179586)).toFloat()
                val startingStartTrim = ring.startingStartTrim
                ring.startTrim = (((ring.startingEndTrim - radians) - startingStartTrim) * interpolatedTime) + startingStartTrim
                ring.storeOriginals()
                ring.endTrim = ring.startingEndTrim
                ring.storeOriginals()
                val startingRotation = ring.startingRotation
                ring.rotation = (((rotationSteps - startingRotation) * interpolatedTime) + startingRotation)
                ring.storeOriginals()
                return
            }
            val radians =
                Math.toRadians((ring.strokeWidth.toDouble()) / (ring.centerRadius * 6.283185307179586)).toFloat()
            val startingEndTrim = ring.startingEndTrim
            val startingStartTrim = ring.startingStartTrim
            val startingRotation = ring.startingRotation
            updateRingColor(interpolatedTime, ring)
            if (interpolatedTime <= 0.5f) {
                ring.startTrim = (FAST_OUT_SLOW_IN.getInterpolation(interpolatedTime / 0.5f) * (0.8f - radians)) + startingStartTrim
                ring.storeOriginals()
            }
            if (interpolatedTime > 0.5f) {
                ring.endTrim = (FAST_OUT_SLOW_IN.getInterpolation((interpolatedTime - 0.5f) / 0.5f) * (0.8f - radians)) + startingEndTrim
                ring.storeOriginals()
            }
            ring.rotation = (0.25f * interpolatedTime) + startingRotation
            ring.storeOriginals()
            myMaterialProgressDrawable.ringRotation =
                ((myMaterialProgressDrawable.rotationCount / 5.0f) * 1080.0f) + (interpolatedTime * 216.0f)
            myMaterialProgressDrawable.invalidateSelf()
        }
    }

    internal inner class StoreRingStep(val outer: MyMaterialProgressDrawable,
                                         val ring: Ring
    ) : Animation.AnimationListener {
        override fun onAnimationEnd(animation: Animation?) {
        }

        override fun onAnimationRepeat(animation: Animation) {
            val ring = this.ring
            ring.startingStartTrim = ring.startTrim
            ring.startingEndTrim = ring.endTrim
            ring.startingRotation = ring.rotation
            ring.setColorIndex((ring.colorIndex + 1) % ring.colors.size)
            ring.startTrim = ring.endTrim
            ring.storeOriginals()
            val myMaterialProgressDrawable = this.outer
            if (!myMaterialProgressDrawable.finishing) {
                myMaterialProgressDrawable.rotationCount = (myMaterialProgressDrawable.rotationCount + 1.0f) % 5.0f
                return
            }
            myMaterialProgressDrawable.finishing = false
            animation.setDuration(1332L)
            if (ring.showArrow) {
                ring.showArrow = false
                ring.storeOriginals()
            }
        }

        override fun onAnimationStart(animation: Animation?) {
            this.outer.rotationCount = 0.0f
        }
    }

    internal inner class InvalidateSelfCallback : Callback {
        override fun invalidateDrawable(drawable: Drawable) {
            this@MyMaterialProgressDrawable.invalidateSelf()
        }

        fun invalidateSelfNow() {
            this@MyMaterialProgressDrawable.invalidateSelf()
        }

        override fun scheduleDrawable(drawable: Drawable, what: Runnable, time: Long) {
            this@MyMaterialProgressDrawable.scheduleSelf(what, time)
        }

        override fun unscheduleDrawable(drawable: Drawable, what: Runnable) {
            this@MyMaterialProgressDrawable.unscheduleSelf(what)
        }
    }

    class Ring(callback: Callback) {
        @JvmField
        val tempBounds: RectF = RectF()
        @JvmField
        var paint: Paint
        @JvmField
        var arrowPaint: Paint
        @JvmField
        var callback: Callback
        @JvmField
        var startTrim: Float
        @JvmField
        var endTrim: Float
        @JvmField
        var rotation: Float
        @JvmField
        var strokeWidth: Float
        @JvmField
        var strokeInset: Float
        @JvmField
        var colors: IntArray = IntArray(0)
        @JvmField
        var colorIndex: Int = 0
        @JvmField
        var startingStartTrim: Float = 0f
        @JvmField
        var startingEndTrim: Float = 0f
        @JvmField
        var startingRotation: Float = 0f
        @JvmField
        var showArrow: Boolean = false
        @JvmField
        var path: Path? = null
        @JvmField
        var arrowScale: Float = 0f
        @JvmField
        var centerRadius: Double = 0.0
        @JvmField
        var arrowWidth: Int = 0
        @JvmField
        var arrowHeight: Int = 0
        @JvmField
        var alpha: Int = 0
        @JvmField
        var circlePaint: Paint
        @JvmField
        var backgroundColor: Int = 0
        @JvmField
        var currentColor: Int = 0

        init {
            val paint = Paint()
            this.paint = paint
            val arrowPaint = Paint()
            this.arrowPaint = arrowPaint
            this.startTrim = 0.0f
            this.endTrim = 0.0f
            this.rotation = 0.0f
            this.strokeWidth = 5.0f
            this.strokeInset = 2.5f
            this.circlePaint = Paint(1)
            this.callback = callback
            paint.setAntiAlias(true)
            paint.setStrokeCap(Paint.Cap.SQUARE)
            paint.setStyle(Paint.Style.STROKE)
            arrowPaint.setAntiAlias(true)
            arrowPaint.setStyle(Paint.Style.FILL)
        }

        fun storeOriginals() {
            (this.callback as InvalidateSelfCallback).invalidateSelfNow()
        }

        fun setColorIndex(colorIndex: Int) {
            this.colorIndex = colorIndex
            this.currentColor = this.colors[colorIndex]
        }
    }

    init {
        val invalidateCallback: InvalidateSelfCallback = InvalidateSelfCallback()
        this.parentView = myPullView
        val resources = webViewActivity.getResources()
        val ring = Ring(invalidateCallback)
        this.mainRing = ring
        if (dark) {
            ring.colors = intArrayOf(-328966)
            ring.setColorIndex(0)
        } else {
            ring.colors = intArrayOf(-16777216)
            ring.setColorIndex(0)
        }
        val f = resources.getDisplayMetrics().density
        val d = f.toDouble()
        val d2 = 40.0 * d
        this.drawWidth = d2
        this.drawHeight = d2
        val f2 = 2.5f * f
        ring.strokeWidth = f2
        ring.paint.setStrokeWidth(f2)
        ring.storeOriginals()
        ring.centerRadius = 8.75 * d
        ring.setColorIndex(0)
        ring.arrowWidth = (10.0f * f).toInt()
        ring.arrowHeight = (5.0f * f).toInt()
        val minSide = min(this.drawWidth.toInt(), this.drawHeight.toInt()).toFloat()
        val d3 = ring.centerRadius
        ring.strokeInset =
            if (d3 <= 0.0 || minSide < 0.0f) ceil((ring.strokeWidth / 2.0f).toDouble()).toFloat() else (((minSide / 2.0f).toDouble()) - d3).toFloat()
        val animation: StoreOriginalsStep =
            StoreOriginalsStep(this, ring)
        animation.setRepeatCount(-1)
        animation.setRepeatMode(1)
        animation.setInterpolator(LINEAR)
        animation.setAnimationListener(StoreRingStep(this, ring))
        this.spinAnim = animation
    }

    override fun draw(canvas: Canvas) {
        val bounds = getBounds()
        val saveCount = canvas.save()
        canvas.rotate(this.ringRotation, bounds.exactCenterX(), bounds.exactCenterY())
        val ring = this.mainRing
        val paint = ring.circlePaint
        val rectF = ring.tempBounds
        rectF.set(bounds)
        val strokeInset = ring.strokeInset
        rectF.inset(strokeInset, strokeInset)
        val startTrim = ring.startTrim
        val rotation = ring.rotation
        val startAngle = (startTrim + rotation) * 360.0f
        val sweepAngle = ((ring.endTrim + rotation) * 360.0f) - startAngle
        val paintNow = ring.paint
        paintNow.setColor(ring.currentColor)
        canvas.drawArc(rectF, startAngle, sweepAngle, false, paintNow)
        val arrowPaint = ring.arrowPaint
        if (ring.showArrow) {
            val path = ring.path
            if (path == null) {
                val newPath = Path()
                ring.path = newPath
                newPath.setFillType(Path.FillType.EVEN_ODD)
            } else {
                path.reset()
            }
            val arrowOffset = ((ring.strokeInset.toInt()) / 2) * ring.arrowScale
            val centerX = ((ring.centerRadius * 1.0) + (bounds.exactCenterX().toDouble())).toFloat()
            val centerY = ((ring.centerRadius * 0.0) + (bounds.exactCenterY().toDouble())).toFloat()
            ring.path!!.moveTo(0.0f, 0.0f)
            ring.path!!.lineTo(ring.arrowWidth * ring.arrowScale, 0.0f)
            val arrowPath = ring.path!!
            val arrowWidth = ring.arrowWidth.toFloat()
            val arrowScale = ring.arrowScale
            arrowPath.lineTo((arrowWidth * arrowScale) / 2.0f, ring.arrowHeight * arrowScale)
            ring.path!!.offset(centerX - arrowOffset, centerY)
            ring.path!!.close()
            arrowPaint.setColor(ring.currentColor)
            canvas.rotate((startAngle + sweepAngle) - 5.0f, bounds.exactCenterX(), bounds.exactCenterY())
            canvas.drawPath(ring.path!!, arrowPaint)
        }
        if (ring.alpha < 255) {
            paint.setColor(ring.backgroundColor)
            paint.setAlpha(KotlinVersion.MAX_COMPONENT_VALUE - ring.alpha)
            canvas.drawCircle(
                bounds.exactCenterX(),
                bounds.exactCenterY(),
                (bounds.width() / 2).toFloat(),
                paint
            )
        }
        canvas.restoreToCount(saveCount)
    }

    override fun getAlpha(): Int {
        return this.mainRing.alpha
    }

    override fun getIntrinsicHeight(): Int {
        return this.drawHeight.toInt()
    }

    override fun getIntrinsicWidth(): Int {
        return this.drawWidth.toInt()
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun isRunning(): Boolean {
        val arrayList = this.animators
        val size = arrayList.size
        for (index in 0..<size) {
            val animation = arrayList[index]
            if (animation.hasStarted() && !animation.hasEnded()) {
                return true
            }
        }
        return false
    }

    override fun setAlpha(alpha: Int) {
        this.mainRing.alpha = alpha
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        val ring = this.mainRing
        ring.paint.setColorFilter(colorFilter)
        ring.storeOriginals()
    }

    override fun start() {
        val animation = this.spinAnim
        animation.reset()
        val ring = this.mainRing
        val startTrim = ring.startTrim
        ring.startingStartTrim = startTrim
        val endTrim = ring.endTrim
        ring.startingEndTrim = endTrim
        ring.startingRotation = ring.rotation
        val myPullView = this.parentView
        if (endTrim != startTrim) {
            this.finishing = true
            animation.setDuration(666L)
            myPullView.startAnimation(animation)
            return
        }
        ring.setColorIndex(0)
        ring.startingStartTrim = 0.0f
        ring.startingEndTrim = 0.0f
        ring.startingRotation = 0.0f
        ring.startTrim = 0.0f
        ring.storeOriginals()
        ring.endTrim = 0.0f
        ring.storeOriginals()
        ring.rotation = 0.0f
        ring.storeOriginals()
        animation.setDuration(1332L)
        myPullView.startAnimation(animation)
    }

    override fun stop() {
        this.parentView.clearAnimation()
        this.ringRotation = 0.0f
        invalidateSelf()
        val ring = this.mainRing
        if (ring.showArrow) {
            ring.showArrow = false
            ring.storeOriginals()
        }
        ring.setColorIndex(0)
        ring.startingStartTrim = 0.0f
        ring.startingEndTrim = 0.0f
        ring.startingRotation = 0.0f
        ring.startTrim = 0.0f
        ring.storeOriginals()
        ring.endTrim = 0.0f
        ring.storeOriginals()
        ring.rotation = 0.0f
        ring.storeOriginals()
    }

    companion object {
        @JvmField
        val LINEAR: LinearInterpolator = LinearInterpolator()
        @JvmField
        val FAST_OUT_SLOW_IN: FastOutSlowInInterpolator = FastOutSlowInInterpolator()
        @JvmStatic
        fun updateRingColor(fraction: Float, ring: Ring) {
            if (fraction > 0.75f) {
                val colorFraction = (fraction - 0.75f) / 0.25f
                val colors = ring.colors
                val colorIndex = ring.colorIndex
                val startColor = colors[colorIndex]
                val endColor = colors[(colorIndex + 1) % colors.size]
                val startA = (startColor shr 24) and KotlinVersion.MAX_COMPONENT_VALUE
                val startR = (startColor shr 16) and KotlinVersion.MAX_COMPONENT_VALUE
                val startG = (startColor shr 8) and KotlinVersion.MAX_COMPONENT_VALUE
                val startB = startColor and KotlinVersion.MAX_COMPONENT_VALUE
                ring.currentColor =
                    ((startA + (((((endColor shr 24) and KotlinVersion.MAX_COMPONENT_VALUE) - startA) * colorFraction).toInt())) shl 24) or ((startR + (((((endColor shr 16) and KotlinVersion.MAX_COMPONENT_VALUE) - startR) * colorFraction).toInt())) shl 16) or ((startG + (((((endColor shr 8) and KotlinVersion.MAX_COMPONENT_VALUE) - startG) * colorFraction).toInt())) shl 8) or (startB + ((colorFraction * ((endColor and KotlinVersion.MAX_COMPONENT_VALUE) - startB)).toInt()))
            }
        }
    }
}
