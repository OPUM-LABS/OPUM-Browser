package com.mycompany.app.view

import androidx.compose.ui.Alignment
import android.animation.ValueAnimator
import com.mycompany.app.ui.state.AppState
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.view.View
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mycompany.app.ui.browser.ComposeOverlay
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil.isRtl
import com.mycompany.app.quick.QuickView

/**
 * The page / tile loading bar. State and API as before (progress `progressValue` of `maxValue`, colours via [applyColors], indeterminate mode via
 * [applyMode], fake progress via [increaseTo], header mode with a gradient backdrop, right-to-left via [rtl]); drawn with Compose: an M3
 * `LinearProgressIndicator` (its own animation for the indeterminate mode, which replaces the old hand-run animators).
 */
@Suppress("deprecation")
class MyProgressBar : ComposeOverlay {
    @JvmField
    var quickTarget: Int = 0
    @JvmField
    var stepRunnable: Runnable?
    @JvmField
    var increaseRunnable: Runnable?
    @JvmField
    var loopOn: Boolean = false
    @JvmField
    var animValue: Float = 0f
    @JvmField
    var animPosted: Boolean = false
    @JvmField
    var animRunnable: Runnable?
    @JvmField
    var rtl: Boolean = false
    @JvmField
    var active: Boolean = false
    @JvmField
    var fixedColor: Boolean = false
    @JvmField
    var showTrack: Boolean = false
    @JvmField
    var barColor: Int = 0
    @JvmField
    var trackTint: Int = 0
    @JvmField
    var barHeightPx: Int = 0
    /** Placed above the bar (the address bar is at the bottom): the line hugs the slot's lower edge and the track fades upwards. */
    @JvmField
    var alongTopEdge: Boolean = false
    @JvmField
    var barPaint: Paint? = null
    @JvmField
    var trackPaint: Paint? = null
    @JvmField
    var viewWidth: Int = 0
    @JvmField
    var maxValue: Int = 0
    @JvmField
    var increaseTarget: Int = 0
    @JvmField
    var progressValue: Float = 0f
    @JvmField
    var indeterminate: Boolean = false
    @JvmField
    var loopFlag: Boolean = false
    @JvmField
    var loopAnimA: ValueAnimator? = null
    @JvmField
    var loopAnimB: ValueAnimator? = null
    @JvmField
    var drawSkipped: Boolean = false
    @JvmField
    var progressListener: MyProgressListener? = null
    @JvmField
    var pendingQuick: QuickView? = null

    internal inner class StepQuickTask : Runnable {
        override fun run() {
            val myProgressBar = this@MyProgressBar
            val quickView = myProgressBar.pendingQuick
            myProgressBar.pendingQuick = null
            if (quickView == null || !quickView.isDragging()) {
                return
            }
            myProgressBar.stepQuick(quickView, myProgressBar.quickTarget)
        }
    }

    internal inner class IncreaseToTask : Runnable {
        override fun run() {
            val myProgressBar = this@MyProgressBar
            myProgressBar.increaseTo(false, myProgressBar.increaseTarget, null)
        }
    }

    internal inner class ProgressAnimUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val animatedValue = (valueAnimator.getAnimatedValue() as Float)
            val myProgressBar = this@MyProgressBar
            myProgressBar.animValue = animatedValue
            if (myProgressBar.animPosted) {
                return
            }
            myProgressBar.animPosted = true
            MainApp.postToMain(myProgressBar.getContext(), myProgressBar.animRunnable!!)
        }
    }

    internal inner class SetValAnimLoopTask : Runnable {
        override fun run() {
            val myProgressBar = this@MyProgressBar
            myProgressBar.animPosted = false
            myProgressBar.setValAnimLoop(myProgressBar.animValue)
        }
    }

    interface MyProgressListener {
        fun onFinished()

        fun targetProgress(): Int

        fun isLoading(): Boolean
    }

    private var tick by mutableIntStateOf(0)

    constructor(context: Context?) : super(context!!) {
        this.stepRunnable = StepQuickTask()
        this.increaseRunnable = IncreaseToTask()
        this.animRunnable = SetValAnimLoopTask()
        setupBar(context, false)
        setPage({ false }) { Bar() }
    }

    @androidx.compose.runtime.Composable
    private fun Bar() {
        val bar = this@MyProgressBar
        @Suppress("UNUSED_VARIABLE") val tickNow = tick
        if (!bar.active || bar.drawSkipped) return
        val density = LocalDensity.current
        val barHeight = with(density) { bar.barHeightPx.toDp() }
        val fill = bar.barPaint?.color ?: 0
        val track = bar.trackPaint
        Box(Modifier.fillMaxSize()) {
            if (bar.showTrack && bar.trackTint != 0 && track != null && !bar.indeterminate) {
                val trackTint = bar.trackTint
                val gradientHeight = MainApp.dp4.toFloat()
                Canvas(Modifier.fillMaxSize()) {
                    if (bar.alongTopEdge) {
                        drawRect(
                            Brush.verticalGradient(0f to Color.Transparent, 0.5f to Color(trackTint), 1f to Color(trackTint), startY = size.height - gradientHeight, endY = size.height),
                            Offset.Zero, Size(size.width, size.height),
                        )
                    } else {
                        drawRect(
                            Brush.verticalGradient(0f to Color(trackTint), 0.5f to Color(trackTint), 1f to Color.Transparent, startY = 0f, endY = gradientHeight),
                            Offset.Zero, Size(size.width, size.height),
                        )
                    }
                }
            }
            val trackColor = if (track != null && !bar.showTrack) Color(track.color) else Color.Transparent
            val direction = if (bar.rtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            CompositionLocalProvider(LocalLayoutDirection provides direction) {
                val lineAt = if (bar.alongTopEdge) Modifier.align(Alignment.BottomCenter) else Modifier
                if (bar.indeterminate) {
                    LinearProgressIndicator(
                        modifier = lineAt.fillMaxWidth().height(barHeight),
                        color = Color(fill), trackColor = trackColor, strokeCap = StrokeCap.Butt, gapSize = 0.dp,
                    )
                } else {
                    val max = if (bar.maxValue <= 0) 1 else bar.maxValue
                    val value = (bar.progressValue / max).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { value },
                        modifier = lineAt.fillMaxWidth().height(barHeight),
                        color = Color(fill), trackColor = trackColor, strokeCap = StrokeCap.Butt, gapSize = 0.dp,
                        drawStopIndicator = {},
                    )
                }
            }
        }
    }

    private fun setProgress2(progressValue: Float) {
        var progressValue = progressValue
        this.progressValue = progressValue
        if (progressValue < 0.0f) {
            this.progressValue = 0.0f
            progressValue = 0.0f
        } else {
            val maxValue = this.maxValue
            if (progressValue > maxValue) {
                progressValue = maxValue.toFloat()
                this.progressValue = progressValue
            }
        }
        invalidate()
    }

    fun setValAnimLoop(loopValueA: Float) {
        if (this.loopOn) {
            if (this.loopAnimA == null) {
                return
            }
            if (loopValueA >= this.viewWidth / 2) {
                applyLoop(false)
            }
        } else if (this.loopAnimB == null) {
            return
        }
        invalidate()
    }

    fun cancelLoops() {
        val valueAnimator = this.loopAnimA
        if (valueAnimator != null) {
            valueAnimator.cancel()
            this.loopAnimA = null
        }
        val loopAnimB = this.loopAnimB
        if (loopAnimB != null) {
            loopAnimB.cancel()
            this.loopAnimB = null
        }
    }

    fun setupBar(context: Context?, fixedColor: Boolean) {
        this.rtl = isRtl(context)
        this.active = true
        if (fixedColor) {
            this.fixedColor = true
            this.showTrack = true
            this.drawSkipped = true
            this.barHeightPx = MainApp.dp4 / 2
        } else {
            this.barColor = 0
            this.barHeightPx = MainApp.dp4
        }
        this.trackTint = 0
        this.maxValue = 100
        val paint = Paint()
        this.barPaint = paint
        paint.setAntiAlias(true)
        val barPaint = this.barPaint
        val style = Paint.Style.FILL
        barPaint!!.setStyle(style)
        if (this.fixedColor) {
            if (!AppState.darkUi) {
                this.barPaint!!.setColor(this.barColor)
                return
            } else {
                this.barColor = -922746881
                this.barPaint!!.setColor(-922746881)
                return
            }
        }
        val trackPaint = Paint()
        this.trackPaint = trackPaint
        trackPaint.setAntiAlias(true)
        this.trackPaint!!.setStyle(style)
        if (AppState.darkUi) {
            this.barColor = -328966
            this.trackTint = -12632257
            this.barPaint!!.setColor(-328966)
            this.barPaint!!.setAlpha(200)
            this.trackPaint!!.setColor(this.trackTint)
            return
        }
        this.barColor = -13022805
        this.trackTint = -1250068
        this.barPaint!!.setColor(-13022805)
        this.barPaint!!.setAlpha(200)
        this.trackPaint!!.setColor(this.trackTint)
    }

    fun startBar() {
        this.drawSkipped = false
        this.progressListener = null
        this.progressValue = 0.0f
        super.setVisibility(View.VISIBLE)
    }

    fun release() {
        this.active = false
        cancelLoops()
        this.barPaint = null
        this.trackPaint = null
        this.progressListener = null
    }

    fun applyColors(barColor: Int, trackTint: Int) {
        var barColor = barColor
        val found: Boolean
        if (!this.fixedColor && AppState.darkUi && barColor == -13022805) {
            barColor = -328966
        }
        var tinted = true
        if (this.barColor != barColor) {
            this.barColor = barColor
            if (this.barPaint == null) {
                val paint = Paint()
                this.barPaint = paint
                paint.setAntiAlias(true)
                this.barPaint!!.setStyle(Paint.Style.FILL)
            }
            this.barPaint!!.setColor(this.barColor)
            if (!this.fixedColor) {
                this.barPaint!!.setAlpha(200)
            }
            found = true
        } else {
            found = false
        }
        if (!this.fixedColor || this.trackTint == trackTint) {
            tinted = found
        } else {
            this.trackTint = trackTint
            if (this.trackPaint == null) {
                val paint = Paint()
                this.trackPaint = paint
                paint.setAntiAlias(true)
                this.trackPaint!!.setStyle(Paint.Style.FILL)
            }
            if (this.showTrack) {
                val trackTintNow = this.trackTint
                val paint: Paint? = if (trackTintNow != 0) this.trackPaint else null
                if (trackTintNow != 0 && paint != null) {
                    paint.setShader(
                        LinearGradient(
                            0.0f,
                            0.0f,
                            0.0f,
                            MainApp.dp4.toFloat(),
                            intArrayOf(trackTintNow, trackTintNow, 0),
                            floatArrayOf(0.0f, 0.5f, 1.0f),
                            Shader.TileMode.REPEAT
                        )
                    )
                }
            } else {
                this.trackPaint!!.setColor(this.trackTint)
            }
        }
        if (tinted) {
            invalidate()
        }
    }

    var max: Int
        get() = this.maxValue
        set(i) {
            if (this.maxValue == i) {
                return
            }
            this.maxValue = if (i == 0) 1 else i
            invalidate()
        }

    var progress: Float
        get() = this.progressValue
        set(f) {
            this.progressListener = null
            setProgress2(f)
        }

    fun applyMode(indeterminate: Boolean, loopFlag: Boolean) {
        if (this.indeterminate == indeterminate && this.loopFlag == loopFlag) {
            return
        }
        this.indeterminate = indeterminate
        this.loopFlag = loopFlag
        if (loopFlag) {
            applyLoop(true)
        } else {
            cancelLoops()
        }
        invalidate()
    }

    fun stepQuick(quickView: QuickView?, quickTarget: Int) {
        if (!this.active || quickTarget == 0) {
            return
        }
        this.quickTarget = quickTarget
        val progress = Math.round(this.progressValue)
        if (progress < quickTarget) {
            this.progress = (progress + 1).toFloat()
            val runnable = this.stepRunnable
            if (runnable != null) {
                this.pendingQuick = quickView
                removeCallbacks(runnable)
                post(runnable)
            }
        }
    }

    override fun invalidate() {
        if (this.active) {
            super.invalidate()
            tick++
        }
    }

    fun applyLoop(loopOn: Boolean) {
        // The indeterminate mode is animated by the M3 indicator; the old loop animators are no longer started.
        this.loopOn = loopOn
        invalidate()
        if (loopOn || !loopOn) return
        val viewWidth = this.viewWidth
        if (viewWidth != 0) {
            if ((if (loopOn) this.loopAnimA else this.loopAnimB) != null) {
                return
            }
            this.loopOn = loopOn
            this.animValue = 0.0f
            this.animPosted = false
            val valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, viewWidth.toFloat())
            if (loopOn) {
                this.loopAnimA = valueAnimatorOfFloat
            } else {
                this.loopAnimB = valueAnimatorOfFloat
            }
            valueAnimatorOfFloat.setDuration(1000L)
            valueAnimatorOfFloat.setInterpolator(if (loopOn) DecelerateInterpolator() else AccelerateInterpolator())
            valueAnimatorOfFloat.setRepeatMode(ValueAnimator.RESTART)
            valueAnimatorOfFloat.setRepeatCount(-1)
            valueAnimatorOfFloat.addUpdateListener(ProgressAnimUpdate())
            valueAnimatorOfFloat.start()
        }
    }

    fun increaseTo(fixedColor: Boolean, increaseTarget: Int, myProgressListener: MyProgressListener?) {
        this.increaseTarget = increaseTarget
        if (fixedColor) {
            if (this.progressListener != null) {
                return
            }
            this.progressListener = myProgressListener
            this.progressValue = 0.0f
        }
        val progressListener = this.progressListener
        if (progressListener == null) {
            return
        }
        if (this.progressValue >= 100.0f) {
            if (!progressListener.isLoading()) {
                val progressListenerNow = this.progressListener
                if (progressListenerNow != null) {
                    progressListenerNow.onFinished()
                    setSkipDraw(true)
                    return
                }
                return
            }
            this.progressValue = 0.0f
        }
        val target = this.progressListener!!.targetProgress()
        if (target != 0) {
            setProgress2(target.toFloat())
        } else {
            setProgress2(this.progressValue + this.increaseTarget)
        }
        setSkipDraw(false)
        val runnable = this.increaseRunnable
        if (runnable != null) {
            removeCallbacks(runnable)
            post(runnable)
        }
    }

    public override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        this.viewWidth = width
        if (this.maxValue == 0) {
            this.maxValue = 1
        }
        if (this.loopFlag) {
            cancelLoops()
            applyLoop(true)
        }
    }

    fun setHeader(fixedColor: Boolean) {
        this.fixedColor = fixedColor
        if (fixedColor) {
            if (!AppState.darkUi) {
                this.barPaint!!.setColor(this.barColor)
            } else {
                this.barColor = -922746881
                this.barPaint!!.setColor(-922746881)
            }
        }
    }

    fun setSkipDraw(drawSkipped: Boolean) {
        if (this.drawSkipped == drawSkipped) {
            return
        }
        this.drawSkipped = drawSkipped
        if (drawSkipped) {
            cancelLoops()
            this.progressListener = null
        }
        if (this.showTrack) {
            super.setVisibility(if (this.drawSkipped) View.GONE else View.VISIBLE)
        }
        invalidate()
    }

    override fun setVisibility(visibility: Int) {
        if (visibility != 0) {
            cancelLoops()
            this.progressListener = null
        }
        super.setVisibility(visibility)
    }

    constructor(context: Context?, i: Int) : super(context!!) {
        this.stepRunnable = StepQuickTask()
        this.increaseRunnable = IncreaseToTask()
        this.animRunnable = SetValAnimLoopTask()
        setupBar(context, true)
        setPage({ false }) { Bar() }
    }

    companion object {
    }
}
