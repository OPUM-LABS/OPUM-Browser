package com.mycompany.app.pull

import android.view.View

import android.view.ViewConfiguration
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.Transformation
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.web.WebViewActivity
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

@Suppress("deprecation")
class MyPullView(webViewActivity: WebViewActivity, darkParam: Boolean) :
    MyCircleImageView(webViewActivity) {
    @JvmField
    var refreshListener: OnRefreshListener? = null
    @JvmField
    var refreshing: Boolean = false
    @JvmField
    var touchSlop: Int
    @JvmField
    var totalDrag: Float
    @JvmField
    var currentTarget: Int = 0
    @JvmField
    var touchDown: Boolean = false
    @JvmField
    var initialMotionY: Float = 0f
    @JvmField
    var downX: Float = 0f
    @JvmField
    var downY: Float = 0f
    @JvmField
    var beingDragged: Boolean = false
    @JvmField
    var animFrom: Int = 0
    @JvmField
    var spinner: MyMaterialProgressDrawable?
    @JvmField
    var decelerate: DecelerateInterpolator?
    @JvmField
    var scaleDownAnim: Animation? = null
    @JvmField
    var alphaStartAnim: Animation? = null
    @JvmField
    var alphaMaxAnim: Animation? = null
    @JvmField
    var spinnerOffset: Float
    @JvmField
    var ringBgColor: Int
    @JvmField
    var spinnerShown: Boolean
    @JvmField
    var toStartAnim: Animation
    @JvmField
    var refreshAnimListener: Animation.AnimationListener?
    @JvmField
    var toCorrectAnim: Animation
    @JvmField
    var overscroll: Float = 0f
    @JvmField
    var moveRunnable: Runnable?

    internal inner class SpinnerAnimListener : Animation.AnimationListener {
        override fun onAnimationEnd(animation: Animation?) {
            val myPullView = this@MyPullView
            if (myPullView.spinner == null) {
                return
            }
            val shrink = myPullView.ShrinkAnim()
            myPullView.scaleDownAnim = shrink
            shrink.setDuration(150L)
            myPullView.setAnimationListener(null)
            myPullView.clearAnimation()
            myPullView.startAnimation(myPullView.scaleDownAnim)
        }

        override fun onAnimationRepeat(animation: Animation?) {
        }

        override fun onAnimationStart(animation: Animation?) {
        }
    }

    internal inner class MoveToStartAnim : Animation() {
        public override fun applyTransformation(interpolatedTime: Float, transformation: Transformation?) {
            val myPullView = this@MyPullView
            if (myPullView.spinner == null) {
                return
            }
            val animFrom = myPullView.animFrom
            val currentTarget = animFrom - ((animFrom * interpolatedTime).toInt())
            myPullView.currentTarget = currentTarget
            myPullView.setTranslationY((myPullView.topOffset + currentTarget).toFloat())
        }
    }

    internal inner class ResetSpinnerStep : Animation.AnimationListener {

        internal inner class StopRefreshingStep : Runnable {
            override fun run() {
                val myPullView = this@MyPullView
                if (myPullView.refreshing) {
                    myPullView.refreshing = false
                    val animationListener = myPullView.refreshAnimListener
                    if (myPullView.spinner == null) {
                        return
                    }
                    val shrink = myPullView.ShrinkAnim()
                    myPullView.scaleDownAnim = shrink
                    shrink.setDuration(150L)
                    myPullView.setAnimationListener(animationListener)
                    myPullView.clearAnimation()
                    myPullView.startAnimation(myPullView.scaleDownAnim)
                }
            }
        }

        override fun onAnimationEnd(animation: Animation?) {
            val myPullView = this@MyPullView
            if (!myPullView.refreshing) {
                myPullView.resetSpinner()
                return
            }
            val myMaterialProgressDrawable = myPullView.spinner
            if (myMaterialProgressDrawable == null) {
                return
            }
            myMaterialProgressDrawable.mainRing.alpha = KotlinVersion.MAX_COMPONENT_VALUE
            myMaterialProgressDrawable.start()
            val onRefreshListener = myPullView.refreshListener
            if (onRefreshListener != null) {
                onRefreshListener.onRefresh()
            }
            myPullView.postDelayed(StopRefreshingStep(), 600L)
        }

        override fun onAnimationRepeat(animation: Animation?) {
        }

        override fun onAnimationStart(animation: Animation?) {
        }
    }

    internal inner class StoreOriginalsStep : Animation() {
        public override fun applyTransformation(interpolatedTime: Float, transformation: Transformation?) {
            val myPullView = this@MyPullView
            val myMaterialProgressDrawable = myPullView.spinner
            if (myMaterialProgressDrawable == null) {
                return
            }
            val arrowScale = 1.0f - interpolatedTime
            val ring = myMaterialProgressDrawable.mainRing
            if (arrowScale != ring.arrowScale) {
                ring.arrowScale = arrowScale
                ring.storeOriginals()
            }
            val animFrom = myPullView.animFrom
            val currentTarget = animFrom + (((myPullView.spinnerOffset - animFrom) * interpolatedTime).toInt())
            myPullView.currentTarget = currentTarget
            myPullView.setTranslationY((myPullView.topOffset + currentTarget).toFloat())
        }
    }

    internal inner class ShrinkAnim : Animation() {
        public override fun applyTransformation(interpolatedTime: Float, transformation: Transformation?) {
            val myPullView = this@MyPullView
            if (myPullView.spinner == null) {
                return
            }
            val remaining = 1.0f - interpolatedTime
            myPullView.setScaleX(remaining)
            myPullView.setScaleY(remaining)
        }
    }

    internal inner class AlphaAnim(val outer: MyPullView,
                                         val startAlpha: Int, targetAlphaParam: Int
    ) : Animation() {
        val targetAlpha: Short

        init {
            this.targetAlpha = (targetAlphaParam).toShort()
        }

        public override fun applyTransformation(interpolatedTime: Float, transformation: Transformation?) {
            val myMaterialProgressDrawable = this.outer.spinner
            if (myMaterialProgressDrawable == null) {
                return
            }
            val startAlpha = this.startAlpha
            myMaterialProgressDrawable.mainRing.alpha = (((this.targetAlpha - startAlpha) * interpolatedTime) + startAlpha).toInt()
        }
    }

    internal inner class MoveSpinnerTask : Runnable {
        override fun run() {
            val myPullView = this@MyPullView
            myPullView.moveSpinner(myPullView.overscroll)
        }
    }

    internal inner class FinishSpinnerTask(val outer: MyPullView,
                                         val floatValue: Float
    ) : Runnable {
        override fun run() {
            this.outer.finishSpinner(this.floatValue)
        }
    }

    internal inner class CancelSpinnerTask : Runnable {
        override fun run() {
            this@MyPullView.finishSpinner(0.0f)
        }
    }

    interface OnRefreshListener {
        fun onRefresh()
    }

    init {
        this.toStartAnim = MoveToStartAnim()
        this.refreshAnimListener = ResetSpinnerStep()
        this.toCorrectAnim = StoreOriginalsStep()
        this.moveRunnable = MoveSpinnerTask()
        val dark = MainUtil.isDarkPage(darkParam)
        val color = if (dark) -11513776 else -328966
        this.ringBgColor = color
        setDiscColor(color)
        this.touchSlop = ViewConfiguration.get(webViewActivity).getScaledTouchSlop()
        this.decelerate = DecelerateInterpolator(2.0f)
        val distance = MainUtil.dpToPx(webViewActivity, 64.0f)
        this.spinnerOffset = distance
        this.totalDrag = distance
        val drawable = MyMaterialProgressDrawable(webViewActivity, this, dark)
        this.spinner = drawable
        drawable.mainRing.backgroundColor = color
        setImageDrawable(drawable)
        this.spinnerShown = false
        setVisibility(View.GONE)
    }

    val topOffset: Int

        get() {
            val i = BrowserSettings.tabBar.value
            val i2 = if (i == 1 || i == 2) PdfSettings.tabHeight.value else 0
            return if (!MainUtil.hasBottomTitle()) i2 - MainApp.dp48 else i2
        }

    fun finishSpinner(floatValue: Float) {
        val myMaterialProgressDrawable = this.spinner
        if (myMaterialProgressDrawable != null) {
            if (floatValue > this.totalDrag) {
                if (this.refreshing) {
                    return
                }
                this.refreshing = true
                this.animFrom = this.currentTarget
                val animation = this.toCorrectAnim
                animation.reset()
                animation.setDuration(200L)
                animation.setInterpolator(this.decelerate)
                val animationListener = this.refreshAnimListener
                if (animationListener != null) {
                    setAnimationListener(animationListener)
                }
                clearAnimation()
                startAnimation(animation)
                return
            }
            this.refreshing = false
            val ring = myMaterialProgressDrawable.mainRing
            ring.startTrim = 0.0f
            ring.storeOriginals()
            ring.endTrim = 0.0f
            ring.storeOriginals()
            this.animFrom = this.currentTarget
            val animation = this.toStartAnim
            animation.reset()
            animation.setDuration(200L)
            animation.setInterpolator(this.decelerate)
            setAnimationListener(SpinnerAnimListener())
            clearAnimation()
            startAnimation(animation)
            val mainRing = this.spinner!!.mainRing
            if (mainRing.showArrow) {
                mainRing.showArrow = false
                mainRing.storeOriginals()
            }
        }
    }

    fun resetSpinner() {
        if (this.spinner != null) {
            if (this.touchDown) {
                this.touchDown = false
                removeCallbacks(this.moveRunnable)
            }
            this.beingDragged = false
            this.refreshing = false
            this.currentTarget = 0
            if (this.spinnerShown) {
                this.spinnerShown = false
                setVisibility(View.GONE)
                this.spinner!!.stop()
                this.spinner!!.mainRing.alpha = KotlinVersion.MAX_COMPONENT_VALUE
                clearAnimation()
                getBackground()?.setAlpha(KotlinVersion.MAX_COMPONENT_VALUE)
                setTranslationY((this.currentTarget + this.topOffset).toFloat())
            }
        }
    }

    fun moveSpinner(overscroll: Float) {
        val animation: Animation?
        val current: Animation?
        if (this.spinner == null || !this.touchDown || !this.beingDragged || overscroll <= 0.0f) {
            return
        }
        val totalDrag = this.totalDrag
        val dragPercent = min(1.0f, abs(overscroll / totalDrag))
        val adjustedPercent = ((max((dragPercent.toDouble()) - 0.4, 0.0).toFloat()) * 5.0f) / 3.0f
        val extraDrag = abs(overscroll) - totalDrag
        val spinnerOffset = this.spinnerOffset
        val dragRatio = (max(0.0f, min(extraDrag, spinnerOffset * 2.0f) / spinnerOffset) / 4.0f).toDouble()
        val tension = ((dragRatio - dragRatio.pow(2.0)).toFloat()) * 2.0f
        val currentTarget = ((spinnerOffset * dragPercent) + (spinnerOffset * tension * 2.0f)).toInt()
        val ring = this.spinner!!.mainRing
        if (!ring.showArrow) {
            ring.showArrow = true
            ring.storeOriginals()
        }
        if (!this.spinnerShown) {
            this.spinnerShown = true
            setVisibility(View.VISIBLE)
        }
        setScaleX(1.0f)
        setScaleY(1.0f)
        val myMaterialProgressDrawable = this.spinner
        if (overscroll < totalDrag) {
            if (myMaterialProgressDrawable!!.mainRing.alpha > 76 && ((this.alphaStartAnim.also {
                    current = it
                }) == null || !current!!.hasStarted() || current.hasEnded())) {
                val fadeOut: AlphaAnim =
                    AlphaAnim(this, this.spinner!!.mainRing.alpha, 76)
                fadeOut.setDuration(300L)
                setAnimationListener(null)
                clearAnimation()
                startAnimation(fadeOut)
                this.alphaStartAnim = fadeOut
            }
        } else if (myMaterialProgressDrawable!!.mainRing.alpha < 255 && ((this.alphaMaxAnim.also {
                animation = it
            }) == null || !animation!!.hasStarted() || animation.hasEnded())) {
            val fadeIn: AlphaAnim =
                AlphaAnim(this, this.spinner!!.mainRing.alpha, KotlinVersion.MAX_COMPONENT_VALUE)
            fadeIn.setDuration(300L)
            setAnimationListener(null)
            clearAnimation()
            startAnimation(fadeIn)
            this.alphaMaxAnim = fadeIn
        }
        val spinner = this.spinner
        val trimEnd = min(0.8f, adjustedPercent * 0.8f)
        val mainRing = spinner!!.mainRing
        mainRing.startTrim = 0.0f
        mainRing.storeOriginals()
        mainRing.endTrim = trimEnd
        mainRing.storeOriginals()
        val spinnerNow = this.spinner
        val arrowScale = min(1.0f, adjustedPercent)
        val mainRingNow = spinnerNow!!.mainRing
        if (arrowScale != mainRingNow.arrowScale) {
            mainRingNow.arrowScale = arrowScale
            mainRingNow.storeOriginals()
        }
        val spinnerRing = this.spinner!!.mainRing
        spinnerRing.rotation = ((tension * 2.0f) + ((adjustedPercent * 0.4f) - 0.25f)) * 0.5f
        spinnerRing.storeOriginals()
        this.currentTarget = currentTarget
        setTranslationY((currentTarget + this.topOffset).toFloat())
    }

    fun onTouchStep(downX: Float, downY: Float, action: Int): Boolean {
        val refreshing = this.refreshing
        val runnable = this.moveRunnable
        if (refreshing || this.spinner == null) {
            if (this.touchDown) {
                this.touchDown = false
                removeCallbacks(runnable)
            }
            this.beingDragged = false
            return false
        }
        if (action == 0) {
            this.touchDown = true
            this.beingDragged = false
            this.downX = downX
            this.downY = downY
        } else if (action == 1) {
            setTouchUp(downY)
        } else if (action == 2) {
            if (!this.touchDown) {
                this.touchDown = true
                this.beingDragged = false
                this.downX = downX
                this.downY = downY
            }
            if (this.beingDragged) {
                this.overscroll = (downY - this.initialMotionY) * 0.5f
                removeCallbacks(runnable)
                post(runnable)
            } else {
                if (abs(downY - this.downY) > abs(downX - this.downX)) {
                    val initialMotionY = this.downY
                    val dragDistance = downY - initialMotionY
                    val slop = this.touchSlop.toFloat()
                    if (dragDistance > slop) {
                        this.beingDragged = true
                        this.initialMotionY = initialMotionY + slop
                        this.spinner!!.mainRing.alpha = 76
                    }
                }
                if (this.beingDragged) {
                    this.overscroll = (downY - this.initialMotionY) * 0.5f
                    removeCallbacks(runnable)
                    post(runnable)
                }
            }
        } else if (action == 3) {
            cancelTouch()
        }
        return this.beingDragged
    }

    fun cancelTouch() {
        if (this.touchDown) {
            this.touchDown = false
            removeCallbacks(this.moveRunnable)
        }
        if (this.beingDragged) {
            this.beingDragged = false
            post(CancelSpinnerTask())
        }
    }

    fun applyColors(onStartPage: Boolean) {
        if (this.spinner != null) {
            val darkPage = MainUtil.isDarkPage(onStartPage)
            val ringBgColor = if (darkPage) -11513776 else -328966
            if (this.ringBgColor != ringBgColor) {
                this.ringBgColor = ringBgColor
                getContext()
                setDiscColor(ringBgColor)
                val ring = this.spinner!!.mainRing
                if (darkPage) {
                    ring.colors = intArrayOf(-328966)
                    ring.setColorIndex(0)
                } else {
                    ring.colors = intArrayOf(-16777216)
                    ring.setColorIndex(0)
                }
                ring.setColorIndex(0)
                ring.backgroundColor = ringBgColor
            }
        }
    }

    public override fun onDetachedFromWindow() {
        resetSpinner()
        super.onDetachedFromWindow()
    }

    fun setAnimationListener(animationListener: Animation.AnimationListener?) {
        this.animListener = animationListener
    }

    override fun setBackgroundColor(color: Int) {
        super.setBackgroundColor(color)
    }

    fun setListener(onRefreshListener: OnRefreshListener?) {
        this.refreshListener = onRefreshListener
    }

    fun setTouchUp(downY: Float) {
        if (this.touchDown) {
            this.touchDown = false
            removeCallbacks(this.moveRunnable)
        }
        if (this.beingDragged) {
            this.beingDragged = false
            post(FinishSpinnerTask(this, (downY - this.initialMotionY) * 0.5f))
        }
    }

    companion object {
    }
}
