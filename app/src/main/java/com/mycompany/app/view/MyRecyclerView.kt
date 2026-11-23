package com.mycompany.app.view

import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.ui.state.AppState
import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.app.image.ImageSizeListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil.loadDrawable

@Suppress("deprecation")
class MyRecyclerView(context: Context?) : RecyclerView(context!!) {
    @JvmField
    var alive: Boolean
    @JvmField
    var shadowDrawable: Drawable? = null
    @JvmField
    var shadowHidden: Boolean = false
    @JvmField
    var roundTop: Boolean = false
    @JvmField
    var roundBottom: Boolean = false
    @JvmField
    var clipOn: Boolean = false
    @JvmField
    var cornerSize: Int
    @JvmField
    var sizeCallback: ImageSizeListener? = null
    @JvmField
    var fadeListener: MyFadeListener? = null
    @JvmField
    var fadeAnim: ValueAnimator? = null
    @JvmField
    var fadeOut: Boolean = false
    @JvmField
    var borderRect: RectF? = null
    @JvmField
    var borderWidth: Float = 0f
    @JvmField
    var borderHalf: Float = 0f
    @JvmField
    var borderPaint: Paint? = null
    @JvmField
    var lineY: Float = 0f
    @JvmField
    var linePaint: Paint? = null
    @JvmField
    var pullDownOn: Boolean = false
    @JvmField
    var animValue: Float = 0f
    @JvmField
    var animPosted: Boolean = false
    @JvmField
    var animRunnable: Runnable?

    internal inner class RoundOutline : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            if (outline != null) {
                val myRecyclerView = this@MyRecyclerView
                val roundTop = myRecyclerView.roundTop
                val roundBottom = myRecyclerView.roundBottom
                if (!roundTop) {
                    if (roundBottom) {
                        outline.setRoundRect(
                            0,
                            -myRecyclerView.cornerSize,
                            myRecyclerView.getWidth(),
                            myRecyclerView.getHeight(),
                            myRecyclerView.cornerSize.toFloat()
                        )
                    }
                } else {
                    if (roundBottom) {
                        outline.setRoundRect(
                            0,
                            0,
                            myRecyclerView.getWidth(),
                            myRecyclerView.getHeight(),
                            myRecyclerView.cornerSize.toFloat()
                        )
                        return
                    }
                    val width = myRecyclerView.getWidth()
                    val height = myRecyclerView.getHeight()
                    val cornerSize = myRecyclerView.cornerSize
                    outline.setRoundRect(0, 0, width, height + cornerSize, cornerSize.toFloat())
                }
            }
        }
    }

    internal inner class FadeAnimUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val myRecyclerView = this@MyRecyclerView
            if (myRecyclerView.fadeAnim != null) {
                val animatedValue = (valueAnimator.getAnimatedValue() as Float)
                if (myRecyclerView.fadeAnim != null) {
                    myRecyclerView.animValue = animatedValue
                    if (myRecyclerView.animPosted) {
                        return
                    }
                    myRecyclerView.animPosted = true
                    MainApp.postToMain(myRecyclerView.getContext(), myRecyclerView.animRunnable!!)
                }
            }
        }
    }

    internal inner class FadeEndAnimListener : Animator.AnimatorListener {
        override fun onAnimationCancel(animator: Animator) {
            val myRecyclerView = this@MyRecyclerView
            if (myRecyclerView.fadeAnim != null) {
                myRecyclerView.fadeAnim = null
                if (myRecyclerView.fadeOut) {
                    myRecyclerView.setVisibility(View.GONE)
                    val myFadeListener = myRecyclerView.fadeListener
                    if (myFadeListener != null) {
                        myFadeListener.onFadeEnd(false)
                    }
                }
            }
        }

        override fun onAnimationEnd(animator: Animator) {
            val myRecyclerView = this@MyRecyclerView
            if (myRecyclerView.fadeAnim == null) {
                return
            }
            MainApp.postToMain(myRecyclerView.getContext(), myRecyclerView.FadeAnimEndTask())
        }

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    }

    internal inner class SetValAnimShowTask : Runnable {
        override fun run() {
            val myRecyclerView = this@MyRecyclerView
            myRecyclerView.animPosted = false
            if (myRecyclerView.fadeAnim == null) {
                return
            }
            myRecyclerView.setValAnimShow(myRecyclerView.animValue)
        }
    }

    internal inner class FadeAnimEndTask : Runnable {
        override fun run() {
            val myRecyclerView = this@MyRecyclerView
            if (myRecyclerView.fadeAnim != null) {
                myRecyclerView.fadeAnim = null
                if (!myRecyclerView.fadeOut) {
                    myRecyclerView.setValAnimShow(1.0f)
                    return
                }
                myRecyclerView.setVisibility(View.GONE)
                val myFadeListener = myRecyclerView.fadeListener
                if (myFadeListener != null) {
                    myFadeListener.onFadeEnd(false)
                }
            }
        }
    }

    init {
        this.animRunnable = SetValAnimShowTask()
        this.cornerSize = MainApp.dp16
        updateClip()
        this.alive = true
    }

    private fun setShadowRect(color: Int) {
        val drawable = this.shadowDrawable
        if (drawable == null || color == 0) {
            return
        }
        drawable.setBounds(0, 0, color, MainApp.dp16)
    }

    fun setValAnimShow(alpha: Float) {
        setAlpha(alpha)
        setScaleX(alpha)
        setScaleY(alpha)
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE)
        }
        invalidate()
    }

    public override fun dispatchDraw(canvas: Canvas) {
        val target: Canvas
        if (this.alive) {
            if (this.linePaint != null) {
                val width = getWidth()
                val height = getHeight()
                val lineY = this.lineY
                val right = width.toFloat()
                target = canvas
                target.drawLine(0.0f, lineY, right, lineY, this.linePaint!!)
                val bottom = height - this.lineY
                target.drawLine(0.0f, bottom, right, bottom, this.linePaint!!)
            } else {
                target = canvas
            }
            super.dispatchDraw(target)
            val rectF = this.borderRect
            val paint: Paint? = if (rectF != null) this.borderPaint else null
            if (rectF != null && paint != null) {
                val radius = this.cornerSize.toFloat()
                target.drawRoundRect(rectF, radius, radius, paint)
            }
            val drawable: Drawable? = if (this.shadowHidden) null else this.shadowDrawable
            if (this.shadowHidden || drawable == null) {
                return
            }
            drawable.draw(target)
        }
    }

    override fun invalidate() {
        if (this.alive) {
            super.invalidate()
        }
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        val rectF = this.borderRect
        if (rectF != null) {
            val borderHalf = this.borderHalf
            rectF.set(borderHalf, borderHalf, width - borderHalf, height - borderHalf)
        }
        setShadowRect(width)
        val imageSizeListener = this.sizeCallback
        if (imageSizeListener != null) {
            imageSizeListener.onSize(this, width, height)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (this.pullDownOn) {
            this.pullDownOn = false
            event!!.setAction(3)
        }
        return super.onTouchEvent(event)
    }

    fun release() {
        if (this.alive) {
            this.alive = false
            stopScroll()
            val valueAnimator = this.fadeAnim
            if (valueAnimator != null) {
                valueAnimator.cancel()
                this.fadeAnim = null
            }
            this.shadowDrawable = null
            this.sizeCallback = null
            this.fadeListener = null
            this.borderRect = null
            this.borderPaint = null
            this.linePaint = null
        }
    }

    fun setPullDown(pullDownOn: Boolean) {
        this.pullDownOn = pullDownOn
    }

    fun setRoundSize(cornerSize: Int) {
        this.cornerSize = cornerSize
    }

    fun setSizeListener(imageSizeListener: ImageSizeListener?) {
        this.sizeCallback = imageSizeListener
    }

    fun applyRound(roundTop: Boolean, roundBottom: Boolean) {
        if (this.roundTop == roundTop && this.roundBottom == roundBottom) {
            return
        }
        this.roundTop = roundTop
        this.roundBottom = roundBottom
        updateClip()
    }

    fun updateClip() {
        val roundTop = this.roundTop
        val clipOn = roundTop || this.roundBottom
        if (this.clipOn == clipOn) {
            return
        }
        this.clipOn = clipOn
        if (!roundTop && !this.roundBottom) {
            setClipToOutline(false)
        } else {
            setOutlineProvider(RoundOutline())
            setClipToOutline(true)
        }
    }

    fun startFade(originX: Int, originY: Int, fadeIn: Boolean, myFadeListener: MyFadeListener?) {
        if (this.fadeAnim != null) {
            return
        }
        this.fadeOut = !fadeIn
        this.fadeListener = myFadeListener
        setPivotX(originX.toFloat())
        setPivotY(originY.toFloat())
        if (fadeIn) {
            this.animValue = 0.0f
            this.animPosted = false
            val valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f)
            this.fadeAnim = valueAnimatorOfFloat
            valueAnimatorOfFloat!!.interpolator = android.view.animation.DecelerateInterpolator()
        } else {
            this.animValue = 1.0f
            this.animPosted = false
            val valueAnimatorOfFloat = ValueAnimator.ofFloat(1.0f, 0.0f)
            this.fadeAnim = valueAnimatorOfFloat
            valueAnimatorOfFloat!!.interpolator = android.view.animation.AccelerateInterpolator()
        }
        this.fadeAnim!!.setDuration(200L)
        this.fadeAnim!!.addUpdateListener(FadeAnimUpdate())
        this.fadeAnim!!.addListener(FadeEndAnimListener())
        this.fadeAnim!!.start()
    }

    companion object {
    }
}
