package com.mycompany.app.ui.browser

import android.view.View

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import kotlin.math.abs

/**
 * The round arrow at the page edge that follows a back / forward swipe (was `view/MyScrollNavi`, 2026-11-17). The
 * gesture logic is the old one (translation follows the finger in [onScroll], [hide] slides it out, [isOut] says whether the swipe
 * went far enough); only the drawing moved to Compose: a translucent dark circle (radius `MainApp.dp16`) with a white
 * chevron, as before.
 */
@Suppress("deprecation")
class ScrollNavi(context: Context?) : ComposeOverlay(context!!) {
    @JvmField var leftSide: Boolean = false
    @JvmField var shownSlop: Int = 0
    @JvmField var lastY: Float = 0f
    @JvmField var hideOffset: Int = 0
    @JvmField var hideAnim: ValueAnimator? = null
    @JvmField var animFrom: Float = 0f
    @JvmField var animSpan: Float = 0f
    @JvmField var animValue: Float = 0f
    @JvmField var animPosted: Boolean = false
    @JvmField var animRunnable: Runnable? = null
    private var icon by mutableIntStateOf(0)

    internal inner class HideAnimUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val navi = this@ScrollNavi
            if (navi.hideAnim != null) {
                val animatedValue = (valueAnimator.getAnimatedValue() as Float)
                if (navi.hideAnim != null) {
                    navi.animValue = animatedValue
                    if (navi.animPosted) {
                        return
                    }
                    navi.animPosted = true
                    MainApp.postToMain(navi.getContext(), navi.animRunnable!!)
                }
            }
        }
    }

    internal inner class HideAnimListener : Animator.AnimatorListener {
        override fun onAnimationCancel(animator: Animator) {
            val navi = this@ScrollNavi
            if (navi.hideAnim == null) {
                return
            }
            navi.hideAnim = null
            navi.invalidate()
        }

        override fun onAnimationEnd(animator: Animator) {
            val navi = this@ScrollNavi
            if (navi.hideAnim == null) {
                return
            }
            MainApp.postToMain(navi.getContext(), navi.HideAnimEndTask())
        }

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    }

    internal inner class SetValAnimNormalTask : Runnable {
        override fun run() {
            val navi = this@ScrollNavi
            navi.animPosted = false
            if (navi.hideAnim == null) {
                return
            }
            navi.setValAnimNormal(navi.animValue)
        }
    }

    internal inner class HideAnimEndTask : Runnable {
        override fun run() {
            val navi = this@ScrollNavi
            if (navi.hideAnim == null) {
                return
            }
            navi.hideAnim = null
            navi.setOnlyVisibility(4)
        }
    }

    init {
        this.animRunnable = SetValAnimNormalTask()
        setPage({ true }) {
            val res = icon
            if (res != 0) {
                val radius = with(LocalDensity.current) { MainApp.dp16.toDp() }
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(radius * 2).background(Color(-509171222), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(painterResource(res), null, tint = Color.White)
                    }
                }
            }
        }
    }

    fun setOnlyVisibility(visibility: Int) {
        super.setVisibility(visibility)
    }

    fun setValAnimNormal(animValue: Float) {
        val animFrom = this.animFrom
        setAlpha(1.0f - (abs(animValue - animFrom) / this.animSpan))
        setTranslationX(animValue)
    }

    fun hide() {
        if (this.hideAnim == null && getVisibility() == View.VISIBLE) {
            val hideOffset = this.hideOffset
            val translationX = getTranslationX()
            val target = (if (this.leftSide) -hideOffset else hideOffset).toFloat()
            val distance = abs(target - translationX)
            if ((distance).compareTo(0.0f) == 0) {
                setOnlyVisibility(4)
                return
            }
            this.animFrom = translationX
            this.animSpan = distance
            this.animValue = translationX
            this.animPosted = false
            val valueAnimatorOfFloat = ValueAnimator.ofFloat(translationX, target)
            this.hideAnim = valueAnimatorOfFloat
            valueAnimatorOfFloat.setDuration(((distance * 200.0f) / hideOffset).toLong())
            this.hideAnim!!.interpolator = android.view.animation.DecelerateInterpolator()
            this.hideAnim!!.addUpdateListener(HideAnimUpdate())
            this.hideAnim!!.addListener(HideAnimListener())
            setOnlyVisibility(0)
            this.hideAnim!!.start()
        }
    }

    fun setup(layoutRtl: Boolean, leftSide: Boolean) {
        this.leftSide = leftSide
        this.shownSlop = MainApp.dp4
        this.hideOffset = MainApp.dp64
        val leftSideNow = this.leftSide
        if (layoutRtl) {
            if (leftSideNow) {
                icon = R.drawable.outline_chevron_right_white_24
            } else {
                icon = R.drawable.outline_chevron_left_white_24
            }
        } else if (leftSideNow) {
            icon = R.drawable.outline_chevron_left_white_24
        } else {
            icon = R.drawable.outline_chevron_right_white_24
        }
    }

    /** The arrow was out at the last move (the tick has been given). */
    private var wasOut = false

    fun isOut(): Boolean {
        if (!isIdle()) {
            return false
        }
        if (this.leftSide) {
            return (-getTranslationX()) < (this.shownSlop.toFloat())
        }
        return getTranslationX() < (this.shownSlop.toFloat())
    }

    fun isIdle(): Boolean {
        return this.hideAnim == null && getVisibility() == View.VISIBLE
    }

    fun release() {
        val valueAnimator = this.hideAnim
        if (valueAnimator != null) {
            valueAnimator.cancel()
            this.hideAnim = null
        }
    }

    fun onScroll(lastY: kotlin.Float, newY: kotlin.Float) {
        val offset: kotlin.Float
        val visibility = getVisibility() == View.VISIBLE
        if (!visibility) {
            this.lastY = lastY
        }
        val hideOffsetNow = this.hideOffset
        val leftSide = this.leftSide
        val lastYNow = this.lastY
        var translation = 0.0f
        if (leftSide) {
            val delta = newY - lastYNow
            val hideOffset = hideOffsetNow.toFloat()
            offset = delta - hideOffset
            if (offset > 0.0f) {
                this.lastY = newY - hideOffset
            } else {
                translation = -hideOffsetNow.toFloat()
                if (offset < translation) {
                    this.lastY = newY
                } else {
                    translation = offset
                }
            }
        } else {
            val hideOffset2Value = hideOffsetNow.toFloat()
            offset = (newY - lastYNow) + hideOffset2Value
            if (offset < 0.0f) {
                this.lastY = newY + hideOffset2Value
            } else if (offset > hideOffset2Value) {
                this.lastY = newY
                translation = hideOffset2Value
            } else {
                translation = offset
            }
        }
        setTranslationX(translation)
        if (visibility) {
            // a tick when the arrow is fully out: letting go now goes back / forward
            val out = isOut()
            if (out && !wasOut) com.mycompany.app.ui.components.Haptics.tick(this)
            wasOut = out
            return
        }
        wasOut = false
        setVisibility(View.VISIBLE)
    }

    override fun onDetachedFromWindow() {
        val valueAnimator = this.hideAnim
        if (valueAnimator != null) {
            valueAnimator.cancel()
            this.hideAnim = null
        }
        super.onDetachedFromWindow()
    }

    override fun setVisibility(visibility: Int) {
        val valueAnimator = this.hideAnim
        if (valueAnimator != null) {
            valueAnimator.cancel()
            this.hideAnim = null
        }
        setAlpha(1.0f)
        super.setVisibility(visibility)
    }
}
