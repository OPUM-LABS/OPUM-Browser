package com.mycompany.app.view

import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.ZoneSettings
import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil.topBarHeight
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.isRtl
import com.mycompany.app.main.MainUtil.loadDrawable
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.web.WebViewActivity
import kotlin.math.max
import kotlin.math.min

@Suppress("deprecation")
class MyScrollBar : MyFadeView {
    @JvmField
    var hRadius: Float = 0f
    @JvmField
    var hTouchSlop: Float = 0f
    @JvmField
    var vThumbHeight: Int = 0
    @JvmField
    var vThumbHalf: Float = 0f
    @JvmField
    var vBarHalf: Float = 0f
    @JvmField
    var vTouchSlop: Float = 0f
    @JvmField
    var vBarOffset: Float = 0f
    @JvmField
    var haloHalf: Float = 0f
    @JvmField
    var layoutRtl: Boolean = false
    @JvmField
    var active: Boolean = false
    @JvmField
    var leftSide: Boolean = false
    @JvmField
    var topInset: Int = 0
    @JvmField
    var bottomInset: Int = 0
    @JvmField
    var horizontal: Boolean = false
    @JvmField
    var useListenerRange: Boolean = false
    @JvmField
    var compact: Boolean = false
    @JvmField
    var barListener: ScrollBarListener? = null
    @JvmField
    var rangeStart: Int = 0
    @JvmField
    var rangeEnd: Int = 0
    @JvmField
    var skipNextPos: Boolean = false
    @JvmField
    var lastPos: Int = 0
    @JvmField
    var thumbPos: Int = 0
    @JvmField
    var lastFraction: Float = 0f
    @JvmField
    var lastValue: Int = 0
    @JvmField
    var iconDirty: Boolean = false
    @JvmField
    var thumbColor: Int = 0
    @JvmField
    var thumbAlphaFull: Int = 0
    @JvmField
    var thumbPaint: Paint? = null
    @JvmField
    var thumbRect: RectF? = null
    @JvmField
    var haloColor: Int = 0
    @JvmField
    var haloAlpha: Int = 0
    @JvmField
    var haloPaint: Paint? = null
    @JvmField
    var pressAnim: ValueAnimator? = null
    @JvmField
    var releaseAnim: ValueAnimator? = null
    @JvmField
    var haloScale: Float = 0f
    @JvmField
    var haloRect: RectF? = null
    @JvmField
    var dragging: Boolean = false
    @JvmField
    var pressValue: Float = 0f
    @JvmField
    var pressPosted: Boolean = false
    @JvmField
    var pressRunnable: Runnable?
    @JvmField
    var releaseValue: Float = 0f
    @JvmField
    var releasePosted: Boolean = false
    @JvmField
    var thumbCorner: Int = 0
    @JvmField
    var releaseRunnable: Runnable?
    @JvmField
    var thumbWidth: Int = 0
    @JvmField
    var thumbHeight: Int = 0
    @JvmField
    var hThumbSize: Int = 0
    @JvmField
    var hThumbHalf: Float = 0f
    @JvmField
    var hIconSize: Int = 0

    internal inner class PressAnimUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val myScrollBar = this@MyScrollBar
            if (myScrollBar.barListener != null) {
                val animatedValue = (valueAnimator.getAnimatedValue() as Float)
                if (myScrollBar.pressAnim != null) {
                    myScrollBar.pressValue = animatedValue
                    if (myScrollBar.pressPosted) {
                        return
                    }
                    myScrollBar.pressPosted = true
                    MainApp.postToMain(myScrollBar.getContext(), myScrollBar.pressRunnable!!)
                }
            }
        }
    }

    internal inner class PressAnimListener : Animator.AnimatorListener {
        override fun onAnimationCancel(animator: Animator) {
            val myScrollBar = this@MyScrollBar
            if (myScrollBar.pressAnim == null) {
                return
            }
            myScrollBar.pressAnim = null
            myScrollBar.invalidate()
        }

        override fun onAnimationEnd(animator: Animator) {
            val myScrollBar = this@MyScrollBar
            if (myScrollBar.pressAnim == null) {
                return
            }
            MainApp.postToMain(myScrollBar.getContext(), myScrollBar.PressAnimEndTask())
        }

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    }

    internal inner class SetValPreScaleUpTask : Runnable {
        override fun run() {
            val myScrollBar = this@MyScrollBar
            myScrollBar.pressPosted = false
            if (myScrollBar.pressAnim == null) {
                return
            }
            myScrollBar.setValPreScaleUp(myScrollBar.pressValue)
        }
    }

    internal inner class PressAnimEndTask : Runnable {
        override fun run() {
            val myScrollBar = this@MyScrollBar
            if (myScrollBar.pressAnim == null) {
                return
            }
            myScrollBar.pressAnim = null
            myScrollBar.setValPreScaleUp(1.0f)
        }
    }

    internal inner class ReleaseAnimUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val myScrollBar = this@MyScrollBar
            if (myScrollBar.barListener != null) {
                val animatedValue = (valueAnimator.getAnimatedValue() as Float)
                if (myScrollBar.releaseAnim != null) {
                    myScrollBar.releaseValue = animatedValue
                    if (myScrollBar.releasePosted) {
                        return
                    }
                    myScrollBar.releasePosted = true
                    MainApp.postToMain(myScrollBar.getContext(), myScrollBar.releaseRunnable!!)
                }
            }
        }
    }

    internal inner class ReleaseAnimListener : Animator.AnimatorListener {
        override fun onAnimationCancel(animator: Animator) {
            val myScrollBar = this@MyScrollBar
            if (myScrollBar.releaseAnim == null) {
                return
            }
            myScrollBar.releaseAnim = null
            myScrollBar.invalidate()
        }

        override fun onAnimationEnd(animator: Animator) {
            val myScrollBar = this@MyScrollBar
            if (myScrollBar.releaseAnim == null) {
                return
            }
            MainApp.postToMain(myScrollBar.getContext(), myScrollBar.ReleaseAnimEndTask())
        }

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    }

    internal inner class SetValPreScaleDnTask : Runnable {
        override fun run() {
            val myScrollBar = this@MyScrollBar
            myScrollBar.releasePosted = false
            if (myScrollBar.releaseAnim == null) {
                return
            }
            myScrollBar.setValPreScaleDn(myScrollBar.releaseValue)
        }
    }

    internal inner class ReleaseAnimEndTask : Runnable {
        override fun run() {
            val myScrollBar = this@MyScrollBar
            if (myScrollBar.releaseAnim == null) {
                return
            }
            myScrollBar.releaseAnim = null
            myScrollBar.setValPreScaleDn(0.8f)
        }
    }

    interface ScrollBarListener {
        fun scrollTo(position: Int)

        fun scrollPos(): Int

        fun onDragEnd()

        fun scrollRange(): Int

        fun scrollExtent(): Int
    }

    constructor(context: Context?) : super(context) {
        this.pressRunnable = SetValPreScaleUpTask()
        this.releaseRunnable = SetValPreScaleDnTask()
        setupBar(context, false)
    }

    private fun setScrollPos(scrollPosState: Float) {
        val offset: Float
        val height: Int
        if (this.skipNextPos) {
            this.skipNextPos = false
            return
        }
        if (this.horizontal) {
            offset = this.hThumbHalf
            height = getWidth()
        } else {
            offset = this.vThumbHalf
            height = getHeight()
        }
        val thumbPos =
            min(max(Math.round(this.topInset + offset), Math.round(scrollPosState)), Math.round((height - this.bottomInset) - offset))
        this.thumbPos = thumbPos
        if (thumbPos == this.lastPos) {
            show(true)
            return
        }
        this.lastPos = thumbPos
        this.iconDirty = true
        invalidate()
        show(true)
    }

    fun setValPreScaleDn(haloScale: Float) {
        if (this.barListener == null) {
            return
        }
        this.haloScale = haloScale
        invalidate()
    }

    fun setValPreScaleUp(haloScale: Float) {
        if (this.barListener == null) {
            return
        }
        this.haloScale = haloScale
        invalidate()
    }

    fun setupBar(context: Context?, vertical: Boolean) {
        val found: Boolean
        this.active = true
        if (vertical) {
            setImmediate(true)
            this.topInset = topBarHeight()
            if (ZoneSettings.scrollPos.value == 1) {
                this.leftSide = true
            }
            found = true
        } else {
            found = false
        }
        setShowTime(1000)
        setTouchable(true)
        setAutoHide(true)
        setInvisible(true)
        this.lastPos = -1
        this.lastValue = -1
        if (!found) {
            this.layoutRtl = isRtl(context)
        } else {
            this.useListenerRange = true
            this.compact = true
        }
    }

    override fun invalidate() {
        if (this.active) {
            super.invalidate()
            drawTick++
        }
    }

    fun releaseAll() {
        this.active = false
        fadeRelease()
        val pressAnim = this.pressAnim
        if (pressAnim != null) {
            pressAnim.cancel()
            this.pressAnim = null
        }
        val valueAnimator = this.releaseAnim
        if (valueAnimator != null) {
            valueAnimator.cancel()
            this.releaseAnim = null
        }
        this.barListener = null
        this.iconRes = 0
        this.thumbPaint = null
        this.haloPaint = null
        this.thumbRect = null
        this.haloRect = null
    }

    fun applyTheme() {
        val paint = this.thumbPaint
        if (paint == null || this.haloPaint == null) {
            return
        }
        val darkUi = AppState.darkUi
        val thumbColor = if (darkUi) -328966 else -5854742
        this.thumbColor = thumbColor
        this.haloColor = if (darkUi) -12632257 else -2434342
        paint.setColor(thumbColor)
        this.haloPaint!!.setColor(this.haloColor)
        this.haloPaint!!.setAlpha(this.haloAlpha)
    }

    fun reportScroll(scrollPosState: Float, vertical: Boolean) {
        val thumbSize: Int
        val thumbPos: Float
        val height: Int
        val value: Int
        val scrollBarListener = this.barListener
        if (scrollBarListener != null) {
            val scrollRange = if (this.useListenerRange) scrollBarListener.scrollRange() - this.barListener!!.scrollExtent() else (this.rangeEnd - this.rangeStart) + 1
            if (scrollRange > 0) {
                if (this.horizontal) {
                    thumbSize = this.hThumbSize
                    thumbPos = this.hThumbHalf
                    height = getWidth()
                } else {
                    thumbSize = this.vThumbHeight
                    thumbPos = this.vThumbHalf
                    height = getHeight()
                }
                val topInset = this.topInset
                val range = height - ((this.bottomInset + topInset) + thumbSize)
                if (range > 0) {
                    val moved = scrollPosState - (topInset + thumbPos)
                    var lastFraction = 0.0f
                    if (moved > 0.0f) {
                        val rangeF = range.toFloat()
                        lastFraction = if (moved >= rangeF) 1.0f else moved / rangeF
                    }
                    if (this.useListenerRange) {
                        value = Math.round(scrollRange * lastFraction)
                    } else {
                        val rangeFValue = scrollRange.toFloat()
                        value = min(max(0, Math.round(rangeFValue * lastFraction)), Math.round(rangeFValue))
                    }
                    val lastFractionNow = this.lastFraction
                    if (lastFraction >= lastFractionNow || value <= this.lastValue) {
                        if (lastFraction <= lastFractionNow || value >= this.lastValue) {
                            this.lastFraction = lastFraction
                            if (value != this.lastValue) {
                                this.lastValue = value
                                if (vertical) {
                                    this.barListener!!.scrollTo(value)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun useCompact() {
        this.useListenerRange = true
        this.compact = true
        this.layoutRtl = false
    }

    fun updatePos(rangeStart: Int, rangeEnd: Int) {
        val thumbSize: Int
        val thumbPos: Float
        val height: Int
        val thumbPosValue: Float
        if (this.barListener != null) {
            if (!this.useListenerRange) {
                this.rangeStart = rangeStart
                this.rangeEnd = rangeEnd
                if (rangeEnd == 0) {
                    hide(false)
                    return
                }
            }
            if (this.horizontal) {
                thumbSize = this.hThumbSize
                thumbPos = this.hThumbHalf
                height = getWidth()
            } else {
                thumbSize = this.vThumbHeight
                thumbPos = this.vThumbHalf
                height = getHeight()
            }
            val index = height - ((this.topInset + this.bottomInset) + thumbSize)
            if (index <= 0) {
                hide(false)
                return
            }
            val range = this.barListener!!.scrollRange() - this.barListener!!.scrollExtent()
            if (range <= 0) {
                hide(false)
                return
            }
            if (this.dragging) {
                return
            }
            val pos = this.barListener!!.scrollPos()
            if (pos <= 0) {
                thumbPosValue = 0.0f
            } else {
                // a float division: (pos / range) on two Ints was always 0 below the end, so the thumb jumped to the top whenever
                // the page scrolled by itself (2026-11-19)
                thumbPosValue = if (pos >= range) 1.0f else pos.toFloat() / range
            }
            setScrollPos((index * thumbPosValue) + this.topInset + thumbPos)
            restartTimer()
        }
    }

    override fun onDetachedFromWindow() {
        val valueAnimator = this.pressAnim
        if (valueAnimator != null) {
            valueAnimator.cancel()
            this.pressAnim = null
        }
        val releaseAnim = this.releaseAnim
        if (releaseAnim != null) {
            releaseAnim.cancel()
            this.releaseAnim = null
        }
        super.onDetachedFromWindow()
    }

    /** Redraw counter for the Compose face (bumped by [invalidate]). */
    private var drawTick by androidx.compose.runtime.mutableIntStateOf(0)
    /** Icon in the thumb (resource) and where it goes; computed as before when [iconDirty] is set. */
    /** State, so a new icon recomposes [Face]; scrolling only redraws (2026-11-21). */
    private var iconRes by androidx.compose.runtime.mutableIntStateOf(0)
    private var iconL = 0f
    private var iconT = 0f
    private var iconW = 0f
    private var iconH = 0f

    init {
        setPage({ AppState.darkUi }) { Face() }
    }

    /** The old onDraw, drawn with Compose: thumb (circle or rounded bar), press halo, and the icon. */
    @androidx.compose.runtime.Composable
    private fun Face() {
        // [drawTick] is read only in the draw block below: a scroll step redraws the bar without recomposing it
        val res = iconRes
        val painter = if (res != 0) androidx.compose.ui.res.painterResource(res) else null
        androidx.compose.foundation.Canvas(androidx.compose.ui.Modifier.fillMaxSize()) {
            @Suppress("UNUSED_VARIABLE") val drawTickNow = drawTick
            if (!active || barListener == null) return@Canvas
            if (iconDirty) {
                iconDirty = false
                if (iconRes != 0) {
                    val pos = thumbPos
                    if (horizontal) {
                        iconL = Math.round(pos - hThumbHalf).toFloat()
                        iconW = hThumbSize.toFloat()
                        iconH = hIconSize.toFloat()
                        iconT = ((height - hIconSize) / 2).toFloat()
                    } else {
                        iconL = 0f
                        iconT = Math.round(pos - vThumbHalf).toFloat()
                        iconW = width.toFloat()
                        iconH = vThumbHeight.toFloat()
                    }
                }
            }
            val pressing = pressAnim != null || releaseAnim != null
            val thumb = thumbPaint
            fun thumbAlpha(): Int {
                if (!pressing) return thumbAlphaFull
                val a = Math.round((1.0f - haloScale) * thumbAlphaFull * 5.0f)
                return if (a > thumbAlphaFull) thumbAlphaFull else a
            }
            if (horizontal) {
                if (thumb != null && (pressing || !dragging)) {
                    drawCircle(color(thumbColor, thumbAlpha()), hRadius, androidx.compose.ui.geometry.Offset(thumbPos.toFloat(), size.height / 2f))
                }
                if (haloPaint != null && (dragging || pressing)) {
                    val cx = thumbPos.toFloat()
                    val cy = size.height / 2f
                    scale(haloScale, haloScale, androidx.compose.ui.geometry.Offset(cx, cy)) {
                        drawCircle(color(haloColor, Math.round((haloScale - 0.8f) * haloAlpha * 5.0f)), MainApp.dp20.toFloat(), androidx.compose.ui.geometry.Offset(cx, cy))
                    }
                }
            } else {
                if (thumb != null && thumbRect != null && (pressing || !dragging)) {
                    val left = if (leftSide || layoutRtl) vBarOffset - thumbWidth else size.width - vBarOffset
                    val top = thumbPos - vBarHalf
                    drawRoundRect(
                        color(thumbColor, thumbAlpha()),
                        androidx.compose.ui.geometry.Offset(left, top),
                        androidx.compose.ui.geometry.Size(thumbWidth.toFloat(), thumbHeight.toFloat()),
                        androidx.compose.ui.geometry.CornerRadius(thumbCorner.toFloat()),
                    )
                }
                if (haloPaint != null && haloRect != null && (dragging || pressing)) {
                    val width = size.width
                    val px = if (leftSide || layoutRtl) 0f else width
                    val py = thumbPos.toFloat()
                    scale(haloScale, haloScale, androidx.compose.ui.geometry.Offset(px, py)) {
                        drawOval(
                            color(haloColor, Math.round((haloScale - 0.8f) * haloAlpha * 5.0f)),
                            androidx.compose.ui.geometry.Offset(px - width, py - haloHalf),
                            androidx.compose.ui.geometry.Size(width * 2f, haloHalf * 2f),
                        )
                    }
                }
            }
            if (painter != null && iconW > 0f && iconH > 0f) {
                translate(iconL, iconT) {
                    with(painter) { draw(androidx.compose.ui.geometry.Size(iconW, iconH)) }
                }
            }
        }
    }

    private fun color(argb: Int, alpha: Int): androidx.compose.ui.graphics.Color =
        androidx.compose.ui.graphics.Color((argb and 0xFFFFFF) or (alpha.coerceIn(0, 255) shl 24))

    public override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        updatePos(this.rangeStart, this.rangeEnd)
        super.onSizeChanged(width, height, oldWidth, oldHeight)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (this.barListener == null) {
            this.dragging = false
            return false
        } else {
            val actionMasked = event!!.getActionMasked()
            if (actionMasked != 0) {
                if (actionMasked != 1) {
                    if (actionMasked == 2) {
                        if (this.dragging) {
                            val scrollPosState: Float
                            if (this.horizontal) {
                                scrollPosState = event.getX()
                            } else {
                                scrollPosState = event.getY()
                            }

                            this.setScrollPos(scrollPosState)
                            this.reportScroll(scrollPosState, true)
                            return true
                        }

                        return false
                    }

                    if (actionMasked != 3) {
                        return true
                    }
                }

                if (this.dragging) {
                    this.show(true)
                    if (this.barListener != null && this.releaseAnim == null) {
                        val haloScale = this.haloScale
                        if (!(haloScale <= 0.8f)) {
                            this.releaseValue = haloScale
                            this.releasePosted = false
                            val releaseAnim = ValueAnimator.ofFloat(*floatArrayOf(haloScale, 0.8f))
                            this.releaseAnim = releaseAnim
                            releaseAnim.setDuration(200L)
                            this.releaseAnim!!.interpolator = android.view.animation.AccelerateInterpolator()
                            this.releaseAnim!!.addUpdateListener(ReleaseAnimUpdate())
                            this.releaseAnim!!.addListener(ReleaseAnimListener())
                            val pressAnim = this.pressAnim
                            if (pressAnim != null) {
                                pressAnim.cancel()
                            }

                            this.releaseAnim!!.start()
                        }
                    }

                    this.dragging = false
                }

                val barListener = this.barListener
                if (barListener != null) {
                    barListener.onDragEnd()
                    return true
                }
            } else {
                val scrollPosState: Float
                if (this.horizontal) {
                    val x = event.getX()
                    val thumbX = this.thumbPos.toFloat()
                    val hTouchSlop = this.hTouchSlop
                    if (x < thumbX - hTouchSlop) {
                        return false
                    }

                    scrollPosState = x
                    if (x > thumbX + hTouchSlop) {
                        return false
                    }
                } else {
                    if (if (this.leftSide) event.getX() > this.vThumbHeight else event.getX() < this.getWidth() - this.vThumbHeight) {
                        return false
                    }

                    val y = event.getY()
                    val thumbY = this.thumbPos.toFloat()
                    val vTouchSlop = this.vTouchSlop
                    if (y < thumbY - vTouchSlop) {
                        return false
                    }

                    scrollPosState = y
                    if (y > thumbY + vTouchSlop) {
                        return false
                    }
                }

                this.dragging = true
                this.show(true)
                this.setScrollPos(scrollPosState)
                this.reportScroll(scrollPosState, false)
                if (this.barListener != null && this.pressAnim == null) {
                    this.haloScale = 0.8f
                    this.pressValue = 0.8f
                    this.pressPosted = false
                    val pressAnim = ValueAnimator.ofFloat(*floatArrayOf(0.8f, 1.0f))
                    this.pressAnim = pressAnim
                    pressAnim.setDuration(200L)
                    this.pressAnim!!.interpolator = android.view.animation.DecelerateInterpolator()
                    this.pressAnim!!.addUpdateListener(PressAnimUpdate())
                    this.pressAnim!!.addListener(PressAnimListener())
                    val releaseAnim = this.releaseAnim
                    if (releaseAnim != null) {
                        releaseAnim.cancel()
                    }

                    this.pressAnim!!.start()
                    return true
                }
            }

            return true
        }
    }

    fun setListener(scrollBarListener: ScrollBarListener?) {
        val context = getContext()
        this.barListener = scrollBarListener
        if (this.horizontal) {
            val hThumbSize = MainApp.dp48
            this.hThumbSize = hThumbSize
            val hThumbHalf = hThumbSize / 2.0f
            this.hThumbHalf = hThumbHalf
            val hIconSize = MainApp.dp16
            this.hIconSize = hIconSize shl 1
            this.hRadius = hIconSize.toFloat()
            this.hTouchSlop = hThumbHalf + MainApp.dp4
            this.iconRes = AppR.drawable.outline_integration_instructions_2_white_24
            this.thumbColor = -1593835520
            this.haloColor = -1586137739
            val paint = Paint()
            this.thumbPaint = paint
            paint.setAntiAlias(true)
            val thumbPaint = this.thumbPaint
            val style = Paint.Style.FILL
            thumbPaint!!.setStyle(style)
            this.thumbPaint!!.setColor(this.thumbColor)
            val haloPaint = Paint()
            this.haloPaint = haloPaint
            haloPaint.setAntiAlias(true)
            this.haloPaint!!.setStyle(style)
            this.haloPaint!!.setColor(this.haloColor)
            this.thumbAlphaFull = this.thumbPaint!!.getAlpha()
            this.haloAlpha = this.haloPaint!!.getAlpha()
            return
        }
        this.thumbCorner = MainApp.dp8
        this.thumbWidth = MainApp.dp4 * 3
        this.thumbHeight = MainApp.dp16 shl 1
        val barSize = if (this.compact) dpToPx(context, 32.0f).toInt() else MainApp.dp48
        val vThumbHeight = MainApp.dp40
        this.vThumbHeight = vThumbHeight
        val vThumbHalf = vThumbHeight / 2.0f
        this.vThumbHalf = vThumbHalf
        this.vBarHalf = this.thumbHeight / 2.0f
        val vTouchSlop = vThumbHalf + MainApp.dp4
        this.vTouchSlop = vTouchSlop
        val thumbWidth = this.thumbWidth
        this.vBarOffset = ((barSize - thumbWidth) / 2.0f) + thumbWidth
        this.haloHalf = vTouchSlop * 2.0f
        this.iconRes = AppR.drawable.outline_unfold_more_2_white_36
        if (this.compact) {
            this.thumbColor = -1582979675
        } else {
            this.thumbColor = if (AppState.darkUi) -328966 else -5854742
        }
        this.haloColor = if (AppState.darkUi) -12632257 else -2434342
        this.thumbAlphaFull = 161
        this.haloAlpha = 161
        val thumbPaint = Paint()
        this.thumbPaint = thumbPaint
        thumbPaint.setAntiAlias(true)
        val thumbPaintNow = this.thumbPaint
        val style = Paint.Style.FILL
        thumbPaintNow!!.setStyle(style)
        this.thumbPaint!!.setColor(this.thumbColor)
        val paint = Paint()
        this.haloPaint = paint
        paint.setAntiAlias(true)
        this.haloPaint!!.setStyle(style)
        this.haloPaint!!.setColor(this.haloColor)
        this.haloPaint!!.setAlpha(this.haloAlpha)
        this.thumbRect = RectF()
        this.haloRect = RectF()
    }

    fun setPosLeft(leftSide: Boolean) {
        if (this.leftSide == leftSide) {
            return
        }
        this.leftSide = leftSide
        requestLayout()
    }

    fun setPreColor(haloColor: Int) {
        val paint: Paint? = if (this.haloColor == haloColor) null else this.haloPaint
        if (this.haloColor == haloColor || paint == null) {
            return
        }
        this.haloColor = haloColor
        paint.setColor(haloColor)
        this.haloPaint!!.setAlpha(this.haloAlpha)
        invalidate()
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility != 0) {
            var visible = true
            var found = false
            if (this.dragging) {
                this.dragging = false
                found = true
            }
            val valueAnimator = this.pressAnim
            if (valueAnimator != null) {
                valueAnimator.cancel()
                this.pressAnim = null
                found = true
            }
            val releaseAnim = this.releaseAnim
            if (releaseAnim != null) {
                releaseAnim.cancel()
                this.releaseAnim = null
            } else {
                visible = found
            }
            if (visible) {
                invalidate()
            }
        }
    }

    constructor(webViewActivity: WebViewActivity?) : super(webViewActivity) {
        this.pressRunnable = SetValPreScaleUpTask()
        this.releaseRunnable = SetValPreScaleDnTask()
        setupBar(webViewActivity, true)
    }

    companion object {
    }
}
