package com.mycompany.app.ui.browser

import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.PdfSettings
import ch.opumlabs.opumbrowser.R as AppR
import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import android.widget.ImageView
import com.mycompany.app.db.book.DbBookSearch
import com.mycompany.app.db.book.DbBookSearch.Companion.cachedIcon; import com.mycompany.app.db.book.DbBookSearch.Companion.colorIndex
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.isRtl
import com.mycompany.app.main.MainUtil.loadDrawable
import com.mycompany.app.main.MainUtil.isBitmapOk
import com.mycompany.app.main.MainUtil.isInRect
import com.mycompany.app.main.MainUtil.isDarkBars
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.web.WebViewActivity
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.drawText
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.clipPath
import kotlin.Boolean
import kotlin.Byte
import kotlin.Exception
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.also
import kotlin.collections.minus
import kotlin.math.max
import kotlin.math.min
import kotlin.sequences.minus
import kotlin.times

@Suppress("deprecation")
/**
 * State, tap zones and press animations of the browser bar's URL pill (kind 1) and tab-count button (kind 2), plus the
 * bar colour helpers in the companion (was `view/BarFace`, an ImageView, until 2026-11-17). It is no longer a View:
 * [MyBarView] draws the face with [drawComposeFace], gives it its size ([onSizeChanged]) and its touches
 * ([onTouchEvent], same MotionEvents as before), and puts the plain [anchor] view in the slot so popups opened from
 * the pill or the tab count keep their place. Redraw requests go to [onRedraw].
 */
class BarFace(context: Context?) {
    private val ctx: Context = context!!
    fun getContext(): Context = ctx
    fun getResources(): android.content.res.Resources = ctx.resources

    /** A plain, empty view the bar places in the slot: the anchor handed to the listeners (popups use its place). */
    @JvmField val anchor: View = View(ctx)

    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
    fun post(runnable: Runnable?): Boolean {
        if (runnable != null) mainHandler.post(runnable)
        return true
    }

    private var faceW = 0
    private var faceH = 0
    fun getWidth(): Int = faceW
    fun getHeight(): Int = faceH

    /** Drawn alpha (the old view alpha: dimmed when disabled, capped by [maxAlpha]). */
    var faceAlpha: Float = 1f
        private set
    fun getAlpha(): Float = faceAlpha

    private var enabledNow = true
    fun isEnabled(): Boolean = enabledNow
    private var clickableNow = true
    fun isClickable(): Boolean = clickableNow

    private var shownState: Int = 0
    fun getVisibility(): Int = shownState
    fun isShown(): Boolean = shownState == 0

    private var tagValue: Any? = null
    fun setTag(newTagValue: Any?) {
        tagValue = newTagValue
        anchor.tag = newTagValue
    }
    fun getTag(): Any? = tagValue

    /** Kept for the old call; the face is drawn by the bar. */
    fun setScaleType(type: ImageView.ScaleType?) {}

    private var longWrapper: OnLongClickListener? = null
    private val longPress = Runnable {
        if (pressed) longWrapper?.onLongClick(anchor)
    }

    @JvmField
    var endIconX: Int = 0
    @JvmField
    var trnsPreUpAnim: ValueAnimator? = null
    @JvmField
    var hostMinX: Int = 0
    @JvmField
    var trnsPreScale: Float = 0f
    @JvmField
    var hostText: String? = null
    @JvmField
    var dotPaint: Paint? = null
    @JvmField
    var hostColor: Int = 0
    @JvmField
    var dotRadius: Float = 0f
    @JvmField
    var hostSecure: Boolean = false
    @JvmField
    var dotMargin: Float = 0f
    @JvmField
    var hostTextColor: Int = 0
    @JvmField
    var dotRtl: Boolean = false
    @JvmField
    var hostTextPaint: Paint? = null
    @JvmField
    var maxAlpha: Float
    @JvmField
    var hostTextSize: Int = 0
    @JvmField
    var clickEnabled: Boolean
    @JvmField
    var hostBounds: Rect? = null
    @JvmField
    var pressed: Boolean = false
    @JvmField
    var secondSource: String? = null
    @JvmField
    var longPressed: Boolean = false
    @JvmField
    var secondShown: Boolean = false
    @JvmField
    var longPressDone: Boolean = false
    @JvmField
    var secondText: String? = null
    @JvmField
    var clickListener: OnClickListener? = null
    @JvmField
    var secondTextColor: Int = 0
    @JvmField
    var longClickListener: OnLongClickListener? = null
    @JvmField
    var secondTextPaint: Paint? = null
    @JvmField
    var iconListener: AddrIconListener? = null
    @JvmField
    var secondBounds: Rect? = null
    @JvmField
    var bgUpValue: Float = 0f
    @JvmField
    var skipDraw: Boolean = false
    @JvmField
    var bgUpRunning: Boolean = false
    @JvmField
    var tabStroke: Float = 0f
    @JvmField
    var bgUpEnd: Runnable?
    @JvmField
    var tabOutlinePaint: Paint? = null
    @JvmField
    var bgDownValue: Float = 0f
    @JvmField
    var tabBox: RectF? = null
    @JvmField
    var bgDownRunning: Boolean = false
    @JvmField
    var tabText: String? = null
    @JvmField
    var bgDownEnd: Runnable?
    @JvmField
    var tabColor: Int = 0
    @JvmField
    var hostUpValue: Float = 0f
    @JvmField
    var tabTextPaint: Paint? = null
    @JvmField
    var hostUpRunning: Boolean = false
    @JvmField
    var tabTextSize: Byte = 0
    @JvmField
    var hostUpEnd: Runnable?
    @JvmField
    var hostDownValue: Float = 0f
    @JvmField
    var hostDownRunning: Boolean = false
    @JvmField
    var hostDownEnd: Runnable?
    @JvmField
    var alive: Boolean = true
    @JvmField
    var tabTextBounds: Rect? = null
    @JvmField
    var trnsUpValue: Float = 0f
    @JvmField
    var faceKind: Byte = 0
    @JvmField
    var pressScaled: Boolean = false
    @JvmField
    var trnsUpRunning: Boolean = false
    @JvmField
    var hasSetting: Boolean = false
    @JvmField
    var circleShown: Boolean = false
    @JvmField
    var trnsUpEnd: Runnable?
    @JvmField
    var circleFilled: Boolean = false
    @JvmField
    var trnsDownValue: Float = 0f
    @JvmField
    var circleRadius: Float = 0f
    @JvmField
    var trnsDownRunning: Boolean = false
    @JvmField
    var centerX: Float = 0f
    @JvmField
    var circleRect: RectF?
    @JvmField
    var trnsDownEnd: Runnable?
    @JvmField
    var centerY: Float = 0f
    @JvmField
    var bgPreColor: Int = 0
    @JvmField
    var bgPreAlpha: Int = 0
    @JvmField
    var bgPrePaint: Paint? = null
    @JvmField
    var bgPreDownAnim: ValueAnimator? = null
    @JvmField
    var bgPreUpAnim: ValueAnimator? = null
    @JvmField
    var pillColor: Int = 0
    @JvmField
    var bgPreScale: Float = 0f
    @JvmField
    var pillPaint: Paint? = null
    @JvmField
    var hostPreOffset: Int = 0
    @JvmField
    var pillRect: RectF? = null
    @JvmField
    var hostPreRect: RectF? = null
    @JvmField
    var bgSmall: Boolean = false
    @JvmField
    var hostPreColor: Int = 0
    @JvmField
    var leftIconRes: Int = 0
    @JvmField
    var hostPreAlpha: Int = 0
    @JvmField
    var leftIcon: Drawable? = null
    @JvmField
    var hostPrePaint: Paint? = null
    @JvmField
    var leftIconX: Int = 0
    @JvmField
    var hostPreDownAnim: ValueAnimator? = null
    @JvmField
    var midIconRes: Int = 0
    @JvmField
    var hostPreUpAnim: ValueAnimator? = null
    @JvmField
    var midIcon: Drawable? = null
    @JvmField
    var hostPreScale: Float = 0f
    @JvmField
    var midIconX: Int = 0
    @JvmField
    var trnsPreOffset: Int = 0
    @JvmField
    var rightIconRes: Int = 0
    @JvmField
    var trnsPreRect: RectF? = null
    @JvmField
    var rightIcon: Drawable? = null
    @JvmField
    var trnsPreColor: Int = 0
    @JvmField
    var rightIconX: Int = 0
    @JvmField
    var trnsPreAlpha: Int = 0
    @JvmField
    var endIconRes: Int = 0
    @JvmField
    var trnsPrePaint: Paint? = null
    @JvmField
    var endIcon: Drawable? = null
    @JvmField
    var trnsPreDownAnim: ValueAnimator? = null

    internal inner class HostPressUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val face = this@BarFace
            if (face.hostPrePaint != null) {
                val animatedValue = (valueAnimator.getAnimatedValue() as Float)
                if (face.hostPreDownAnim != null) {
                    face.hostUpValue = animatedValue
                    if (face.hostUpRunning) {
                        return
                    }
                    face.hostUpRunning = true
                    MainApp.postToMain(face.getContext(), face.hostUpEnd!!)
                }
            }
        }
    }

    internal inner class HostPressListener : Animator.AnimatorListener {
        override fun onAnimationCancel(animator: Animator) {
            val face = this@BarFace
            if (face.hostPreDownAnim == null) {
                return
            }
            face.hostPreDownAnim = null
            face.invalidate()
        }

        override fun onAnimationEnd(animator: Animator) {
            val face = this@BarFace
            if (face.hostPreDownAnim == null) {
                return
            }
            MainApp.postToMain(face.getContext(), face.HostPressEndTask())
        }

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    }

    internal inner class SetValHostScaleUpTask : Runnable {
        override fun run() {
            val face = this@BarFace
            face.hostUpRunning = false
            if (face.hostPreDownAnim == null) {
                return
            }
            face.setValHostScaleUp(face.hostUpValue)
        }
    }

    internal inner class HostPressEndTask : Runnable {
        override fun run() {
            val face = this@BarFace
            if (face.hostPreDownAnim == null) {
                return
            }
            face.hostPreDownAnim = null
            face.setValHostScaleUp(1.0f)
        }
    }

    internal inner class HostReleaseUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val face = this@BarFace
            if (face.hostPrePaint != null) {
                val animatedValue = (valueAnimator.getAnimatedValue() as Float)
                if (face.hostPreUpAnim != null) {
                    face.hostDownValue = animatedValue
                    if (face.hostDownRunning) {
                        return
                    }
                    face.hostDownRunning = true
                    MainApp.postToMain(face.getContext(), face.hostDownEnd!!)
                }
            }
        }
    }

    internal inner class HostReleaseListener : Animator.AnimatorListener {
        override fun onAnimationCancel(animator: Animator) {
            val face = this@BarFace
            if (face.hostPreUpAnim == null) {
                return
            }
            face.hostPreUpAnim = null
            face.invalidate()
        }

        override fun onAnimationEnd(animator: Animator) {
            val face = this@BarFace
            if (face.hostPreUpAnim == null) {
                return
            }
            MainApp.postToMain(face.getContext(), face.HostReleaseEndTask())
        }

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    }

    internal inner class SetValHostScaleDnTask : Runnable {
        override fun run() {
            val face = this@BarFace
            face.hostDownRunning = false
            if (face.hostPreUpAnim == null) {
                return
            }
            face.setValHostScaleDn(face.hostDownValue)
        }
    }

    internal inner class HostReleaseEndTask : Runnable {
        override fun run() {
            val face = this@BarFace
            if (face.hostPreUpAnim == null) {
                return
            }
            face.hostPreUpAnim = null
            face.setValHostScaleDn(0.8f)
        }
    }

    internal inner class TransPressUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val face = this@BarFace
            if (face.trnsPrePaint != null) {
                val animatedValue = (valueAnimator.getAnimatedValue() as Float)
                if (face.trnsPreDownAnim != null) {
                    face.trnsUpValue = animatedValue
                    if (face.trnsUpRunning) {
                        return
                    }
                    face.trnsUpRunning = true
                    MainApp.postToMain(face.getContext(), face.trnsUpEnd!!)
                }
            }
        }
    }

    internal inner class TransPressListener : Animator.AnimatorListener {
        override fun onAnimationCancel(animator: Animator) {
            val face = this@BarFace
            if (face.trnsPreDownAnim == null) {
                return
            }
            face.trnsPreDownAnim = null
            face.invalidate()
        }

        override fun onAnimationEnd(animator: Animator) {
            val face = this@BarFace
            if (face.trnsPreDownAnim == null) {
                return
            }
            MainApp.postToMain(face.getContext(), face.TransPressEndTask())
        }

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    }

    internal inner class BgPressUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val face = this@BarFace
            if (face.bgPrePaint != null) {
                val animatedValue = (valueAnimator.getAnimatedValue() as Float)
                if (face.bgPreDownAnim != null) {
                    face.bgUpValue = animatedValue
                    if (face.bgUpRunning) {
                        return
                    }
                    face.bgUpRunning = true
                    MainApp.postToMain(face.getContext(), face.bgUpEnd!!)
                }
            }
        }
    }

    internal inner class SetValTrnsScaleUpTask : Runnable {
        override fun run() {
            val face = this@BarFace
            face.trnsUpRunning = false
            if (face.trnsPreDownAnim == null) {
                return
            }
            face.setValTrnsScaleUp(face.trnsUpValue)
        }
    }

    internal inner class TransPressEndTask : Runnable {
        override fun run() {
            val face = this@BarFace
            if (face.trnsPreDownAnim == null) {
                return
            }
            face.trnsPreDownAnim = null
            face.setValTrnsScaleUp(1.0f)
        }
    }

    internal inner class TransReleaseUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val face = this@BarFace
            if (face.trnsPrePaint != null) {
                val animatedValue = (valueAnimator.getAnimatedValue() as Float)
                if (face.trnsPreUpAnim != null) {
                    face.trnsDownValue = animatedValue
                    if (face.trnsDownRunning) {
                        return
                    }
                    face.trnsDownRunning = true
                    MainApp.postToMain(face.getContext(), face.trnsDownEnd!!)
                }
            }
        }
    }

    internal inner class TransReleaseListener : Animator.AnimatorListener {
        override fun onAnimationCancel(animator: Animator) {
            val face = this@BarFace
            if (face.trnsPreUpAnim == null) {
                return
            }
            face.trnsPreUpAnim = null
            face.invalidate()
        }

        override fun onAnimationEnd(animator: Animator) {
            val face = this@BarFace
            if (face.trnsPreUpAnim == null) {
                return
            }
            MainApp.postToMain(face.getContext(), face.TransReleaseEndTask())
        }

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    }

    internal inner class SetValTrnsScaleDnTask : Runnable {
        override fun run() {
            val face = this@BarFace
            face.trnsDownRunning = false
            if (face.trnsPreUpAnim == null) {
                return
            }
            face.setValTrnsScaleDn(face.trnsDownValue)
        }
    }

    internal inner class TransReleaseEndTask : Runnable {
        override fun run() {
            val face = this@BarFace
            if (face.trnsPreUpAnim == null) {
                return
            }
            face.trnsPreUpAnim = null
            face.setValTrnsScaleDn(0.8f)
        }
    }

    internal inner class LongClickForwarder : OnLongClickListener {
        override fun onLongClick(view: View?): Boolean {
            val face = this@BarFace
            val onLongClickListener: OnLongClickListener? = if (face.longPressed || face.longPressDone || !face.pressed) null else face.longClickListener
            if (face.longPressed || face.longPressDone || !face.pressed || onLongClickListener == null) {
                return false
            }
            onLongClickListener.onLongClick(face.anchor)
            face.pressed = false
            face.releaseBgPress()
            return true
        }
    }

    internal inner class PerformClickTask : Runnable {
        override fun run() {
            val face = this@BarFace
            val onClickListener = face.clickListener
            if (onClickListener != null) {
                onClickListener.onClick(face.anchor)
            }
        }
    }

    internal inner class BgPressListener : Animator.AnimatorListener {
        override fun onAnimationCancel(animator: Animator) {
            val face = this@BarFace
            if (face.bgPreDownAnim == null) {
                return
            }
            face.bgPreDownAnim = null
            face.invalidate()
        }

        override fun onAnimationEnd(animator: Animator) {
            val face = this@BarFace
            if (face.bgPreDownAnim == null) {
                return
            }
            MainApp.postToMain(face.getContext(), face.BgPressEndTask())
        }

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    }

    internal inner class SetValPreScaleUpTask : Runnable {
        override fun run() {
            val face = this@BarFace
            face.bgUpRunning = false
            if (face.bgPreDownAnim == null) {
                return
            }
            face.setValPreScaleUp(face.bgUpValue)
        }
    }

    internal inner class BgPressEndTask : Runnable {
        override fun run() {
            val face = this@BarFace
            if (face.bgPreDownAnim == null) {
                return
            }
            face.bgPreDownAnim = null
            face.setValPreScaleUp(1.0f)
        }
    }

    internal inner class BgReleaseUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val face = this@BarFace
            if (face.bgPrePaint != null) {
                val animatedValue = (valueAnimator.getAnimatedValue() as Float)
                if (face.bgPreUpAnim != null) {
                    face.bgDownValue = animatedValue
                    if (face.bgDownRunning) {
                        return
                    }
                    face.bgDownRunning = true
                    MainApp.postToMain(face.getContext(), face.bgDownEnd!!)
                }
            }
        }
    }

    internal inner class BgReleaseListener : Animator.AnimatorListener {
        override fun onAnimationCancel(animator: Animator) {
            val face = this@BarFace
            if (face.bgPreUpAnim == null) {
                return
            }
            face.bgPreUpAnim = null
            face.invalidate()
        }

        override fun onAnimationEnd(animator: Animator) {
            val face = this@BarFace
            if (face.bgPreUpAnim == null) {
                return
            }
            MainApp.postToMain(face.getContext(), face.BgReleaseEndTask())
        }

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    }

    internal inner class SetValPreScaleDnTask : Runnable {
        override fun run() {
            val face = this@BarFace
            face.bgDownRunning = false
            if (face.bgPreUpAnim == null) {
                return
            }
            face.setValPreScaleDn(face.bgDownValue)
        }
    }

    internal inner class BgReleaseEndTask : Runnable {
        override fun run() {
            val face = this@BarFace
            if (face.bgPreUpAnim == null) {
                return
            }
            face.bgPreUpAnim = null
            face.setValPreScaleDn(0.8f)
        }
    }

    interface AddrIconListener {
        fun onClick(face: View?)

        fun onLongClick(face: View?)
    }

    init {
        this.bgUpEnd = SetValPreScaleUpTask()
        this.bgDownEnd = SetValPreScaleDnTask()
        this.hostUpEnd = SetValHostScaleUpTask()
        this.hostDownEnd = SetValHostScaleDnTask()
        this.trnsUpEnd = SetValTrnsScaleUpTask()
        this.trnsDownEnd = SetValTrnsScaleDnTask()
        this.circleRect = RectF()
        this.maxAlpha = 1.0f
        this.clickEnabled = true
    }

    private fun setHostPreRect(faceH: Int) {
        val rectF = this.hostPreRect
        val offset = if (rectF == null) 0 else this.hostPreOffset
        if (rectF == null || offset == 0 || faceH == 0) {
            return
        }
        val halfSize = MainApp.dp48 / 2
        rectF.set((offset - halfSize).toFloat(), 0.0f, (offset + halfSize).toFloat(), faceH.toFloat())
    }

    private fun setTrnsPreRect(faceH: Int) {
        val rectF = this.trnsPreRect
        val offset = if (rectF == null) 0 else this.trnsPreOffset
        if (rectF == null || offset == 0 || faceH == 0) {
            return
        }
        val halfSize = MainApp.dp48 / 2
        rectF.set((offset - halfSize).toFloat(), 0.0f, (offset + halfSize).toFloat(), faceH.toFloat())
    }

    fun setValHostScaleDn(hostPreScale: Float) {
        if (this.hostPrePaint == null) {
            return
        }
        this.hostPreScale = hostPreScale
        invalidate()
    }

    fun setValHostScaleUp(hostPreScale: Float) {
        if (this.hostPrePaint == null) {
            return
        }
        this.hostPreScale = hostPreScale
        invalidate()
    }

    fun setValPreScaleDn(bgPreScale: Float) {
        if (this.bgPrePaint == null) {
            return
        }
        this.bgPreScale = bgPreScale
        invalidate()
    }

    fun setValPreScaleUp(bgPreScale: Float) {
        if (this.bgPrePaint == null) {
            return
        }
        this.bgPreScale = bgPreScale
        invalidate()
    }

    fun setValTrnsScaleDn(trnsPreScale: Float) {
        if (this.trnsPrePaint == null) {
            return
        }
        this.trnsPreScale = trnsPreScale
        invalidate()
    }

    fun setValTrnsScaleUp(trnsPreScale: Float) {
        if (this.trnsPrePaint == null) {
            return
        }
        this.trnsPreScale = trnsPreScale
        invalidate()
    }

    fun iconOffset(drawable: Drawable?, x: Int, height: Int): Int {
        if (drawable == null || height == 0) {
            return 0
        }
        val intrinsicWidth = drawable.getIntrinsicWidth()
        val intrinsicHeight = drawable.getIntrinsicHeight()
        val top = (height - intrinsicHeight) / 2
        this.trnsPreOffset = (intrinsicWidth / 2) + x
        drawable.setBounds(x, top, x + intrinsicWidth, intrinsicHeight + top)
        return intrinsicWidth
    }

    fun releaseBgPress() {
        if (this.clickEnabled && this.bgPrePaint != null && this.bgPreUpAnim == null) {
            val bgPreScale = this.bgPreScale
            if (bgPreScale <= 0.8f) {
                return
            }
            if (this.pressScaled) {
                this.bgPreScale = 0.8f
                invalidate()
                return
            }
            this.bgDownValue = bgPreScale
            this.bgDownRunning = false
            val valueAnimatorOfFloat = ValueAnimator.ofFloat(bgPreScale, 0.8f)
            this.bgPreUpAnim = valueAnimatorOfFloat
            valueAnimatorOfFloat.setDuration(200L)
            this.bgPreUpAnim!!.addUpdateListener(BgReleaseUpdate())
            this.bgPreUpAnim!!.addListener(BgReleaseListener())
            val valueAnimator = this.bgPreDownAnim
            if (valueAnimator != null) {
                valueAnimator.cancel()
            }
            this.bgPreUpAnim!!.start()
        }
    }

    fun releaseHostPress() {
        if (this.hostPrePaint == null || this.hostPreUpAnim != null) {
            return
        }
        val hostPreScale = this.hostPreScale
        if (hostPreScale <= 0.8f) {
            return
        }
        if (this.pressScaled) {
            this.hostPreScale = 0.8f
            invalidate()
            return
        }
        this.hostDownValue = hostPreScale
        this.hostDownRunning = false
        val valueAnimatorOfFloat = ValueAnimator.ofFloat(hostPreScale, 0.8f)
        this.hostPreUpAnim = valueAnimatorOfFloat
        valueAnimatorOfFloat.setDuration(200L)
        this.hostPreUpAnim!!.addUpdateListener(HostReleaseUpdate())
        this.hostPreUpAnim!!.addListener(HostReleaseListener())
        val valueAnimator = this.hostPreDownAnim
        if (valueAnimator != null) {
            valueAnimator.cancel()
        }
        this.hostPreUpAnim!!.start()
    }

    fun releaseTrnsPress() {
        if (this.trnsPrePaint == null || this.trnsPreUpAnim != null) {
            return
        }
        val trnsPreScale = this.trnsPreScale
        if (trnsPreScale <= 0.8f) {
            return
        }
        if (this.pressScaled) {
            this.trnsPreScale = 0.8f
            invalidate()
            return
        }
        this.trnsDownValue = trnsPreScale
        this.trnsDownRunning = false
        val valueAnimatorOfFloat = ValueAnimator.ofFloat(trnsPreScale, 0.8f)
        this.trnsPreUpAnim = valueAnimatorOfFloat
        valueAnimatorOfFloat.setDuration(200L)
        this.trnsPreUpAnim!!.addUpdateListener(TransReleaseUpdate())
        this.trnsPreUpAnim!!.addListener(TransReleaseListener())
        val valueAnimator = this.trnsPreDownAnim
        if (valueAnimator != null) {
            valueAnimator.cancel()
        }
        this.trnsPreUpAnim!!.start()
    }

    fun cancelAnims() {
        val valueAnimator = this.bgPreDownAnim
        if (valueAnimator != null) {
            valueAnimator.cancel()
            this.bgPreDownAnim = null
        }
        val bgPreUpAnim = this.bgPreUpAnim
        if (bgPreUpAnim != null) {
            bgPreUpAnim.cancel()
            this.bgPreUpAnim = null
        }
        val hostPreDownAnim = this.hostPreDownAnim
        if (hostPreDownAnim != null) {
            hostPreDownAnim.cancel()
            this.hostPreDownAnim = null
        }
        val hostPreUpAnim = this.hostPreUpAnim
        if (hostPreUpAnim != null) {
            hostPreUpAnim.cancel()
            this.hostPreUpAnim = null
        }
        val trnsPreDownAnim = this.trnsPreDownAnim
        if (trnsPreDownAnim != null) {
            trnsPreDownAnim.cancel()
            this.trnsPreDownAnim = null
        }
        val trnsPreUpAnim = this.trnsPreUpAnim
        if (trnsPreUpAnim != null) {
            trnsPreUpAnim.cancel()
            this.trnsPreUpAnim = null
        }
    }

    fun cancelPress() {
        var changed = true
        var found = false
        if (this.pressed) {
            this.pressed = false
            found = true
        }
        val valueAnimator = this.bgPreDownAnim
        if (valueAnimator != null) {
            valueAnimator.cancel()
            this.bgPreDownAnim = null
            found = true
        }
        val bgPreUpAnim = this.bgPreUpAnim
        if (bgPreUpAnim != null) {
            bgPreUpAnim.cancel()
            this.bgPreUpAnim = null
            found = true
        }
        val hostPreDownAnim = this.hostPreDownAnim
        if (hostPreDownAnim != null) {
            hostPreDownAnim.cancel()
            this.hostPreDownAnim = null
            found = true
        }
        val hostPreUpAnim = this.hostPreUpAnim
        if (hostPreUpAnim != null) {
            hostPreUpAnim.cancel()
            this.hostPreUpAnim = null
            found = true
        }
        val trnsPreDownAnim = this.trnsPreDownAnim
        if (trnsPreDownAnim != null) {
            trnsPreDownAnim.cancel()
            this.trnsPreDownAnim = null
            found = true
        }
        val trnsPreUpAnim = this.trnsPreUpAnim
        if (trnsPreUpAnim != null) {
            trnsPreUpAnim.cancel()
            this.trnsPreUpAnim = null
        } else {
            changed = found
        }
        if (changed) {
            invalidate()
        }
    }

    fun invalidate() {
        if (this.alive) {
            onRedraw?.run()
        }
    }

    /**
     * Set by the Compose bar ([MyBarView]) for the URL pill (kind 1) and the tab count (kind 2): the view keeps its state,
     * tap zones and press animations but no longer draws them; the bar draws [drawComposeFace] behind it and is told
     * about every redraw through [onRedraw].
     */
    @JvmField
    var onRedraw: Runnable? = null
    /** Set by the Compose bar: told when the visibility changes (a GONE icon leaves the bar's row). */
    @JvmField
    var onVisibility: Runnable? = null

    /** The old onDraw for kinds 1 and 2, drawn with Compose (shapes and text) into [ds]. */
    fun drawComposeFace(ds: androidx.compose.ui.graphics.drawscope.DrawScope, measurer: androidx.compose.ui.text.TextMeasurer) {
        if (!this.alive) return
        val faceKind = this.faceKind.toInt()
        if (faceKind != 1 && faceKind != 2) return
        with(ds) {
            val offset = androidx.compose.ui.geometry.Offset(this@BarFace.centerX, this@BarFace.centerY)
            val press = this@BarFace.bgPrePaint
            if (press != null && (this@BarFace.pressed || this@BarFace.bgPreDownAnim != null || this@BarFace.bgPreUpAnim != null)) {
                val col = faceColor(press.color, Math.round((this@BarFace.bgPreScale - 0.8f) * this@BarFace.bgPreAlpha * 5.0f))
                val rr = this@BarFace.circleRect
                if (!this@BarFace.circleShown || rr == null) {
                    drawCircle(col, this@BarFace.circleRadius * this@BarFace.bgPreScale, offset)
                } else {
                    scale(this@BarFace.bgPreScale, offset) {
                        val radius = MainApp.dp8.toFloat()
                        drawRoundRect(col, androidx.compose.ui.geometry.Offset(rr.left, rr.top), androidx.compose.ui.geometry.Size(rr.width(), rr.height()), androidx.compose.ui.geometry.CornerRadius(radius, radius))
                    }
                }
            }
            if (faceKind == 2) {
                val outline = this@BarFace.tabOutlinePaint
                val box = this@BarFace.tabBox ?: return
                val label = this@BarFace.tabText
                val textPaint = this@BarFace.tabTextPaint
                if (!TextUtils.isEmpty(label) && textPaint != null) {
                    val layout = measurer.measure(label!!, faceText(textPaint))
                    drawText(layout, topLeft = androidx.compose.ui.geometry.Offset(this@BarFace.centerX - layout.size.width / 2f, this@BarFace.centerY - layout.size.height / 2f))
                }
                if (outline != null) {
                    val radius = MainApp.dp4.toFloat()
                    val colour = androidx.compose.ui.graphics.Color(outline.color)
                    // a second card behind, up and to the right: only its top and right edges show (a stack of tabs)
                    val shift = MainApp.dp1 * 3f
                    val front = androidx.compose.ui.graphics.Path().apply {
                        addRoundRect(androidx.compose.ui.geometry.RoundRect(box.left - outline.strokeWidth, box.top - outline.strokeWidth, box.right + outline.strokeWidth, box.bottom + outline.strokeWidth, radius + outline.strokeWidth, radius + outline.strokeWidth))
                    }
                    clipPath(front, androidx.compose.ui.graphics.ClipOp.Difference) {
                        drawRoundRect(
                            colour.copy(alpha = colour.alpha * 0.6f), androidx.compose.ui.geometry.Offset(box.left + shift, box.top - shift),
                            androidx.compose.ui.geometry.Size(box.width(), box.height()), androidx.compose.ui.geometry.CornerRadius(radius, radius),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(outline.strokeWidth * 0.85f),
                        )
                    }
                    drawRoundRect(colour, androidx.compose.ui.geometry.Offset(box.left, box.top), androidx.compose.ui.geometry.Size(box.width(), box.height()), androidx.compose.ui.geometry.CornerRadius(radius, radius), style = androidx.compose.ui.graphics.drawscope.Stroke(outline.strokeWidth))
                }
                return
            }
            val bg = this@BarFace.pillPaint
            val rect = this@BarFace.pillRect
            if (this@BarFace.skipDraw || bg == null || rect == null) return
            val radius = MainApp.dp8.toFloat()
            drawRoundRect(androidx.compose.ui.graphics.Color(bg.color), androidx.compose.ui.geometry.Offset(rect.left, rect.top), androidx.compose.ui.geometry.Size(rect.width(), rect.height()), androidx.compose.ui.geometry.CornerRadius(radius, radius))
            if (bg.color == -1) {
                // a white pill on the light bar gets a faint edge
                val edge = MainApp.dp1.toFloat()
                drawRoundRect(
                    androidx.compose.ui.graphics.Color(0x1A000000), androidx.compose.ui.geometry.Offset(rect.left + edge / 2, rect.top + edge / 2),
                    androidx.compose.ui.geometry.Size(rect.width() - edge, rect.height() - edge), androidx.compose.ui.geometry.CornerRadius(radius, radius),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(edge),
                )
            }
            val left = this@BarFace.hostPrePaint
            val right = this@BarFace.trnsPrePaint
            if (left != null && (this@BarFace.longPressed || this@BarFace.hostPreDownAnim != null || this@BarFace.hostPreUpAnim != null)) {
                drawCircle(faceColor(left.color, Math.round((this@BarFace.hostPreScale - 0.8f) * this@BarFace.hostPreAlpha * 5.0f)), this@BarFace.circleRadius * this@BarFace.hostPreScale, androidx.compose.ui.geometry.Offset(this@BarFace.hostPreOffset.toFloat(), this@BarFace.centerY))
            } else if (right != null && (this@BarFace.longPressDone || this@BarFace.trnsPreDownAnim != null || this@BarFace.trnsPreUpAnim != null)) {
                drawCircle(faceColor(right.color, Math.round((this@BarFace.trnsPreScale - 0.8f) * this@BarFace.trnsPreAlpha * 5.0f)), this@BarFace.circleRadius * this@BarFace.trnsPreScale, androidx.compose.ui.geometry.Offset(this@BarFace.trnsPreOffset.toFloat(), this@BarFace.centerY))
            }
            val dot = this@BarFace.dotPaint
            if (this@BarFace.hasSetting && dot != null) {
                val dotMargin = this@BarFace.dotMargin
                val x = if (this@BarFace.dotRtl) size.width - dotMargin else dotMargin
                drawCircle(androidx.compose.ui.graphics.Color(dot.color), this@BarFace.dotRadius, androidx.compose.ui.geometry.Offset(x, this@BarFace.centerY - dotMargin))
            }
            val host = this@BarFace.hostText
            val hostPaint = this@BarFace.hostTextPaint
            if (TextUtils.isEmpty(host) || hostPaint == null || this@BarFace.hostBounds == null) return
            // tighter inner padding (2026-11-19): text and icons start 4 dp inside the pill, and with the reload button at the
            // end the text runs up to it
            val dp16Px = MainApp.dp8 + MainApp.dp4
            val rightInset = (if (this@BarFace.endIconX != 0) MainApp.dp40 else if (this@BarFace.endReserve > 0) MainApp.dp4 else MainApp.dp4 + MainApp.dp16) + this@BarFace.endReserve
            clipRect(dp16Px.toFloat(), 0f, size.width - rightInset, size.height) {
                drawIntoCanvas { canvas ->
                    if (this@BarFace.leftIconRes != 0) this@BarFace.leftIcon?.draw(canvas.nativeCanvas)
                    if (this@BarFace.midIconRes != 0) this@BarFace.midIcon?.draw(canvas.nativeCanvas)
                    else if (this@BarFace.rightIconRes != 0) this@BarFace.rightIcon?.draw(canvas.nativeCanvas)
                }
                val hostLayout = measurer.measure(emphasiseHost(host!!, hostPaint.color, this@BarFace.secondShown), faceText(hostPaint), maxLines = 1, softWrap = false)
                val second = this@BarFace.secondText
                val secondPaint = this@BarFace.secondTextPaint
                val withSecond = PdfSettings.addrType.value == 1 && !TextUtils.isEmpty(second) && secondPaint != null
                val secondLayout = if (withSecond) measurer.measure(second!!, faceText(secondPaint), maxLines = 1, softWrap = false) else null
                var total = hostLayout.size.width.toFloat()
                if (secondLayout != null) total += MainApp.dp4 / 2f + secondLayout.size.width
                val x = max(this@BarFace.centerX - total / 2f, this@BarFace.hostMinX.toFloat())
                val y = this@BarFace.centerY - hostLayout.size.height / 2f
                drawText(hostLayout, topLeft = androidx.compose.ui.geometry.Offset(x, y))
                if (secondLayout != null) {
                    drawText(secondLayout, topLeft = androidx.compose.ui.geometry.Offset(x + hostLayout.size.width + MainApp.dp4 / 2f, this@BarFace.centerY - secondLayout.size.height / 2f))
                }
            }
            if (this@BarFace.endIconX != 0 && this@BarFace.endIconRes != 0) {
                val icon = this@BarFace.endIcon ?: return
                drawIntoCanvas { icon.draw(it.nativeCanvas) }
                if (dot != null) {
                    val dp48Px = MainApp.dp48
                    val cx = (dp48Px / 2f) + (size.width - dp48Px - this@BarFace.endReserve)
                    val dotMargin = this@BarFace.dotMargin
                    drawCircle(androidx.compose.ui.graphics.Color(dot.color), this@BarFace.dotRadius, androidx.compose.ui.geometry.Offset(if (this@BarFace.dotRtl) cx + dotMargin else cx - dotMargin, this@BarFace.centerY - dotMargin))
                }
            }
        }
    }

    /**
     * A full address in the pill: the site (host) at full strength, scheme and path dimmed, so where you are reads at a
     * glance. A plain host, a title or the placeholder hint stay as they are.
     */
    private fun emphasiseHost(text: String, color: Int, hint: Boolean): androidx.compose.ui.text.AnnotatedString {
        val plain = androidx.compose.ui.text.AnnotatedString(text)
        if (hint || text.contains(' ')) return plain
        val schemeEnd = text.indexOf("://")
        val start = if (schemeEnd >= 0) schemeEnd + 3 else 0
        val slash = text.indexOf('/', start)
        val end = if (slash >= 0) slash else text.length
        if (start == 0 && end == text.length) return plain
        val dim = androidx.compose.ui.text.SpanStyle(color = androidx.compose.ui.graphics.Color(color).copy(alpha = 0.55f))
        return androidx.compose.ui.text.buildAnnotatedString {
            append(text)
            if (start > 0) addStyle(dim, 0, start)
            if (end < text.length) addStyle(dim, end, text.length)
        }
    }

    private fun faceColor(color: Int, alpha: Int): androidx.compose.ui.graphics.Color =
        androidx.compose.ui.graphics.Color((color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24))

    private fun androidx.compose.ui.graphics.drawscope.DrawScope.faceText(paint: Paint): androidx.compose.ui.text.TextStyle =
        androidx.compose.ui.text.TextStyle(
            color = androidx.compose.ui.graphics.Color(paint.color),
            fontSize = paint.textSize.toSp(),
            // The exact typeface the old Paint used (Typeface.DEFAULT when none was set), so the text looks the same.
            fontFamily = androidx.compose.ui.text.font.FontFamily(paint.typeface ?: android.graphics.Typeface.DEFAULT),
            fontWeight = if (paint.isFakeBoldText) androidx.compose.ui.text.font.FontWeight.Bold else null,
            platformStyle = androidx.compose.ui.text.PlatformTextStyle(includeFontPadding = false),
        )

    fun isPressed(): Boolean {
        return this.pressed
    }

    fun clearHost() {
        this.pillColor = 0
        this.pillPaint = null
        this.pillRect = null
        this.bgSmall = false
        this.leftIconRes = 0
        this.leftIcon = null
        this.leftIconX = 0
        this.midIconRes = 0
        this.midIcon = null
        this.midIconX = 0
        this.rightIconRes = 0
        this.rightIcon = null
        this.rightIconX = 0
        this.endIconRes = 0
        this.endIcon = null
        this.endIconX = 0
        this.hostMinX = 0
        this.hostText = null
        this.hostColor = 0
        this.hostSecure = false
        this.hostTextColor = 0
        this.hostTextPaint = null
        this.hostTextSize = 0
        this.hostBounds = null
        this.secondSource = null
        this.secondShown = false
        this.secondText = null
        this.secondTextColor = 0
        this.secondTextPaint = null
        this.secondBounds = null
        this.skipDraw = false
        this.hostPreRect = null
        this.hostPrePaint = null
        this.trnsPreRect = null
        this.trnsPrePaint = null
        this.iconListener = null
    }

    fun clearTab() {
        this.tabOutlinePaint = null
        this.tabBox = null
        this.tabText = null
        this.tabColor = 0
        this.tabTextPaint = null
        this.tabTextSize = 12.toByte()
        this.tabTextBounds = null
    }

    /** The slot left the bar: stops the press animations. */
    fun onDetachedFromWindow() {
        cancelAnims()
        mainHandler.removeCallbacks(longPress)
    }


    fun onSizeChanged(width: Int, height: Int, oldw: Int, oldh: Int) {
        faceW = width
        faceH = height
        this.centerX = width / 2.0f
        this.centerY = height / 2.0f
        layoutCircle(width, height)
        setHostPreRect(height)
        setTrnsPreRect(height)
        val faceKind = this.faceKind
        if (faceKind.toInt() != 1) {
            if (faceKind.toInt() == 2) {
                layoutTabBox(this.centerX, this.centerY)
            }
        } else if (this.pillRect != null) {
            this.leftIconX = 0
            this.midIconX = 0
            this.rightIconX = 0
            this.endIconX = 0
            layoutBackground(this.centerY, width)
            layoutIcons(height)
        }
    }

    fun onTouchEvent(motionEvent: MotionEvent?): Boolean {
        if (this.clickListener == null || !isEnabled() || !isClickable() || this.bgPrePaint == null) {
            this.pressed = false
            this.longPressed = false
            this.longPressDone = false
            return false
        }
        val actionMasked = motionEvent!!.getActionMasked()
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked != 2) {
                    if (actionMasked == 3) {
                    }
                } else if (this.pressed) {
                    val x = motionEvent.getX().toInt()
                    val y = motionEvent.getY().toInt()
                    if (this.longPressed && !MainUtil.isInRect(this.hostPreRect, x, y)) {
                        this.pressed = false
                        this.longPressed = false
                        releaseHostPress()
                    } else if (this.longPressDone && !MainUtil.isInRect(this.trnsPreRect, x, y)) {
                        this.pressed = false
                        this.longPressDone = false
                        releaseTrnsPress()
                    } else if (!MainUtil.isInRect(this.circleRect, x, y)) {
                        this.pressed = false
                        releaseBgPress()
                    }
                }
            } else if (this.longPressed) {
                val addrIconListener = this.iconListener
                if (addrIconListener != null) {
                    addrIconListener.onLongClick(anchor)
                }
            } else if (this.longPressDone) {
                val iconListener = this.iconListener
                if (iconListener != null) {
                    iconListener.onClick(anchor)
                }
            } else if (this.pressed && this.clickEnabled && this.clickListener != null) {
                post(PerformClickTask())
            }
            if (actionMasked == 1 || actionMasked == 3) {
                if (this.pressed) {
                    this.pressed = false
                    if (this.longPressed) {
                        this.longPressed = false
                        releaseHostPress()
                    } else if (this.longPressDone) {
                        this.longPressDone = false
                        releaseTrnsPress()
                    } else {
                        releaseBgPress()
                    }
                }
            }
        } else {
            val x2 = motionEvent.getX().toInt()
            val y2 = motionEvent.getY().toInt()
            if (MainUtil.isInRect(this.circleRect, x2, y2)) {
                this.pressed = true
                val rectF = this.hostPreRect
                if (rectF == null || !MainUtil.isInRect(rectF, x2, y2)) {
                    val trnsPreRect = this.trnsPreRect
                    if (trnsPreRect != null && MainUtil.isInRect(trnsPreRect, x2, y2)) {
                        this.longPressDone = true
                        if (this.trnsPrePaint != null && this.trnsPreDownAnim == null) {
                            if (this.pressScaled) {
                                this.trnsPreScale = 1.0f
                                invalidate()
                            } else {
                                this.trnsPreScale = 0.8f
                                this.trnsUpValue = 0.8f
                                this.trnsUpRunning = false
                                val valueAnimatorOfFloat = ValueAnimator.ofFloat(0.8f, 1.0f)
                                this.trnsPreDownAnim = valueAnimatorOfFloat
                                valueAnimatorOfFloat.setDuration(200L)
                                this.trnsPreDownAnim!!.addUpdateListener(TransPressUpdate())
                                this.trnsPreDownAnim!!.addListener(TransPressListener())
                                val valueAnimator = this.trnsPreUpAnim
                                if (valueAnimator != null) {
                                    valueAnimator.cancel()
                                }
                                this.trnsPreDownAnim!!.start()
                            }
                        }
                    } else if (this.clickEnabled && this.bgPrePaint != null && this.bgPreDownAnim == null) {
                        if (this.pressScaled) {
                            this.bgPreScale = 1.0f
                            invalidate()
                        } else {
                            this.bgPreScale = 0.8f
                            this.bgUpValue = 0.8f
                            this.bgUpRunning = false
                            val releaseAnim = ValueAnimator.ofFloat(0.8f, 1.0f)
                            this.bgPreDownAnim = releaseAnim
                            releaseAnim.setDuration(200L)
                            this.bgPreDownAnim!!.addUpdateListener(BgPressUpdate())
                            this.bgPreDownAnim!!.addListener(BgPressListener())
                            val bgPreUpAnim = this.bgPreUpAnim
                            if (bgPreUpAnim != null) {
                                bgPreUpAnim.cancel()
                            }
                            this.bgPreDownAnim!!.start()
                        }
                    }
                } else {
                    this.longPressed = true
                    if (this.hostPrePaint != null && this.hostPreDownAnim == null) {
                        if (this.pressScaled) {
                            this.hostPreScale = 1.0f
                            invalidate()
                        } else {
                            this.hostPreScale = 0.8f
                            this.hostUpValue = 0.8f
                            this.hostUpRunning = false
                            val releaseAnim = ValueAnimator.ofFloat(0.8f, 1.0f)
                            this.hostPreDownAnim = releaseAnim
                            releaseAnim.setDuration(200L)
                            this.hostPreDownAnim!!.addUpdateListener(HostPressUpdate())
                            this.hostPreDownAnim!!.addListener(HostPressListener())
                            val hostPreUpAnim = this.hostPreUpAnim
                            if (hostPreUpAnim != null) {
                                hostPreUpAnim.cancel()
                            }
                            this.hostPreDownAnim!!.start()
                        }
                    }
                }
            }
        }
        // long press (the View's own detection before): armed on down, dropped on up / cancel / leaving the zones
        when (motionEvent.getActionMasked()) {
            0 -> {
                mainHandler.removeCallbacks(longPress)
                if (this.pressed && longWrapper != null) {
                    mainHandler.postDelayed(longPress, android.view.ViewConfiguration.getLongPressTimeout().toLong())
                }
            }
            1, 3 -> mainHandler.removeCallbacks(longPress)
            2 -> if (!this.pressed) mainHandler.removeCallbacks(longPress)
        }
        return true
    }

    fun setupBackground(barColor: Int, iconColor: Int, onStartPage: Boolean, darkBars: Boolean) {
        var found: Boolean
        var changed = true
        this.faceKind = 1.toByte()
        var small = false
        clearTab()
        if (this.pillPaint == null) {
            val paint = Paint()
            this.pillPaint = paint
            paint.setAntiAlias(true)
            this.pillPaint!!.setStyle(Paint.Style.FILL)
            found = true
        } else {
            found = false
        }
        if (!PdfSettings.topToBottom.value && darkBars && MainUtil.isDarkBars(onStartPage)) {
            small = true
        }
        if (this.bgSmall != small) {
            this.bgSmall = small
            this.pillRect = null
        }
        if (this.pillRect == null) {
            this.pillRect = RectF()
            val width = getWidth()
            val height = getHeight()
            if (width != 0 && height != 0) {
                layoutBackground(height / 2.0f, width)
            }
            found = true
        }
        val wantedPillColor: Int = pillColorFor(barColor, iconColor, this.hasSetting)
        if (this.pillColor != wantedPillColor) {
            this.pillColor = wantedPillColor
            this.pillPaint!!.setColor(wantedPillColor)
        } else {
            changed = found
        }
        if (changed) {
            invalidate()
        }
    }

    fun layoutBackground(centerY: Float, width: Int) {
        val rectF = this.pillRect
        if (rectF == null) {
            return
        }
        var halfHeight = (MainApp.dp48 / 2) - MainApp.dp8
        if (this.bgSmall) {
            halfHeight -= MainApp.dp4
        }
        val dp8Px = MainApp.dp8
        val halfHeightF = halfHeight.toFloat()
        rectF.set(dp8Px.toFloat(), centerY - halfHeightF, (width - dp8Px).toFloat(), centerY + halfHeightF)
    }

    fun layoutIcons(faceH: Int): Boolean {
        var found: Boolean
        var midX: Int
        var dp16Px = MainApp.dp8 + MainApp.dp4
        if (this.leftIconRes != 0) {
            var leftIconX = this.leftIconX
            if (leftIconX != 0) {
                found = false
            } else {
                val drawable = this.leftIcon
                if (drawable == null || faceH == 0) {
                    leftIconX = 0
                } else {
                    val intrinsicWidth = drawable.getIntrinsicWidth()
                    val intrinsicHeight = drawable.getIntrinsicHeight()
                    val top = (faceH - intrinsicHeight) / 2
                    drawable.setBounds(dp16Px, top, dp16Px + intrinsicWidth, intrinsicHeight + top)
                    leftIconX = intrinsicWidth
                }
                this.leftIconX = leftIconX
                if (leftIconX != 0) {
                    leftIconX += MainApp.dp4
                    this.leftIconX = leftIconX
                    found = true
                } else {
                    found = false
                }
            }
            dp16Px += leftIconX
        } else {
            found = false
        }
        if (this.midIconRes != 0) {
            midX = this.midIconX
            if (midX == 0) {
                val midIcon = this.midIcon
                if (midIcon == null || faceH == 0) {
                    midX = 0
                } else {
                    val midWidth = midIcon.getIntrinsicWidth()
                    val midHeight = midIcon.getIntrinsicHeight()
                    val midTop = (faceH - midHeight) / 2
                    this.hostPreOffset = (midWidth / 2) + dp16Px
                    midIcon.setBounds(dp16Px, midTop, dp16Px + midWidth, midHeight + midTop)
                    midX = midWidth
                }
                this.midIconX = midX
                if (midX != 0) {
                    midX += MainApp.dp4
                    this.midIconX = midX
                    found = true
                }
            }
            dp16Px += midX
        } else if (this.rightIconRes != 0) {
            midX = this.rightIconX
            if (midX == 0) {
                val rightIcon = this.rightIcon
                if (rightIcon == null || faceH == 0) {
                    midX = 0
                } else {
                    val rightWidth = rightIcon.getIntrinsicWidth()
                    val rightHeight = rightIcon.getIntrinsicHeight()
                    val rightTop = (faceH - rightHeight) / 2
                    rightIcon.setBounds(dp16Px, rightTop, dp16Px + rightWidth, rightHeight + rightTop)
                    midX = rightWidth
                }
                this.rightIconX = midX
                if (midX != 0) {
                    midX += MainApp.dp4
                    this.rightIconX = midX
                    found = true
                }
            }
            dp16Px += midX
        }
        if (this.endIconRes != 0 && this.endIconX == 0) {
            val offset = iconOffset(
                this.endIcon,
                (getWidth() - MainApp.dp48 - endReserve) + ((MainApp.dp48 - Math.round(dpToPx(getContext(), 18.0f))) / 2),
                faceH
            )
            this.endIconX = offset
            if (offset != 0) {
                found = true
            }
        }
        val midIconX = this.midIconX
        val rectF = this.hostPreRect
        if (midIconX != 0) {
            if (rectF == null) {
                this.hostPreRect = RectF()
                found = true
            }
            setHostPreRect(getHeight())
        } else if (rectF != null) {
            this.hostPreRect = null
            found = true
        }
        val endIconX = this.endIconX
        val trnsPreRect = this.trnsPreRect
        if (endIconX != 0) {
            if (trnsPreRect == null) {
                this.trnsPreRect = RectF()
                found = true
            }
            setTrnsPreRect(getHeight())
        } else if (trnsPreRect != null) {
            this.trnsPreRect = null
            found = true
        }
        if (!this.hasSetting) {
            if (setDot(this.endIconX != 0 && AlbumSettings.transNoti.value, false)) {
                found = true
            }
        }
        val hostMinX = if (dp16Px == MainApp.dp8 + MainApp.dp4) dp16Px + MainApp.dp4 else dp16Px + (MainApp.dp4 / 2)
        if (this.hostMinX == hostMinX) {
            return found
        }
        this.hostMinX = hostMinX
        return true
    }

    fun setupHost(color: Int, rawColorType: Int, context: Context?, host: String?, second: String?, secure: Boolean) {
        // a light custom colour gets the light bar's dark icons and text
        val colorType = MainUtil.barInk(rawColorType)
        var resources: String? = null
        var host = host
        var found: Boolean
        val textColor: Int
        var textChanged: Boolean
        val leftRes: Int
        val midRes: Int
        val rightRes: Int
        val offlineRes: Int
        val endRes: Int
        var iconsChanged: Boolean
        if (context != null) {
            this.faceKind = 1.toByte()
            clearTab()
            if (TextUtils.isEmpty(host)) {
                this.secondShown = true
                if (TextUtils.isEmpty(this.secondSource)) {
                    val webEditHint = AppR.string.web_edit_hint
                    try {
                        resources = context.getResources().getString(webEditHint)
                    } catch (unused2: Exception) {
                        resources = null
                    }
                    this.secondSource = resources
                }
                host = this.secondSource
                if (TextUtils.isEmpty(host)) {
                    if (TextUtils.isEmpty(this.hostText)) {
                        return
                    }
                    this.hostText = null
                    this.hostColor = 0
                    this.hostSecure = false
                    invalidate()
                    return
                }
            } else {
                this.secondShown = false
            }
            if (this.hostTextPaint == null) {
                val paint = Paint()
                this.hostTextPaint = paint
                paint.setAntiAlias(true)
                this.hostText = null
                this.hostTextSize = 0
                found = true
            } else {
                found = false
            }
            val textSize = if (this.secondShown) MainApp.dp16 - (MainApp.dp4 / 2) else MainApp.dp16
            if (this.hostTextSize != textSize) {
                this.hostTextSize = textSize
                this.hostTextPaint!!.setTextSize(textSize.toFloat())
                found = true
            }
            if (this.hostBounds == null) {
                this.hostBounds = Rect()
                this.hostText = null
                found = true
            }
            if (host != this.hostText) {
                this.hostText = host
                this.hostTextPaint!!.getTextBounds(host, 0, host!!.length, this.hostBounds)
                found = true
            }
            if (this.hostColor != color) {
                this.hostColor = color
                found = true
            }
            if (this.hostSecure != secure) {
                this.hostSecure = secure
                found = true
            }
            var wantedSecondColor = -1577058305
            if (this.secondShown) {
                if (colorType == 0) {
                    textColor = -1593835520
                } else if (colorType == 1) {
                    textColor = -1577058305
                } else {
                    textColor = -1
                }
            } else if (colorType == 0) {
                textColor = -16777216
            } else if (colorType == 1) {
                textColor = -328966
            } else {
                textColor = -1
            }
            if (this.hostTextColor != textColor) {
                this.hostTextColor = textColor
                this.hostTextPaint!!.setColor(textColor)
                found = true
            }
            if (this.faceKind.toInt() != 1) {
                textChanged = false
            } else {
                if (!TextUtils.isEmpty(second)) {
                    if (this.secondTextPaint == null) {
                        val secondPaint = Paint()
                        this.secondTextPaint = secondPaint
                        secondPaint.setAntiAlias(true)
                        this.secondTextPaint!!.setTextSize(MainApp.dp16.toFloat())
                        this.secondText = null
                        textChanged = true
                    } else {
                        textChanged = false
                    }
                    if (this.secondBounds == null) {
                        this.secondBounds = Rect()
                        this.secondText = null
                        textChanged = true
                    }
                    if (second != this.secondText) {
                        this.secondText = second
                        this.secondTextPaint!!.getTextBounds(second, 0, second!!.length, this.secondBounds)
                        textChanged = true
                    }
                    if (colorType == 0) {
                        wantedSecondColor = -1593835520
                    } else if (colorType != 1) {
                        wantedSecondColor = -1
                    }
                    if (this.secondTextColor != wantedSecondColor) {
                        this.secondTextColor = wantedSecondColor
                        this.secondTextPaint!!.setColor(wantedSecondColor)
                    }
                } else if (TextUtils.isEmpty(this.secondText)) {
                    textChanged = false
                } else {
                    this.secondText = null
                }
                textChanged = true
            }
            if (textChanged) {
                found = true
            }
            if (!SyncSettings.secretMode.value) {
                this.leftIconX = 0
                leftRes = 0
            } else if (colorType == 0) {
                leftRes = AppR.drawable.outline_secret_mode_black_18
            } else {
                leftRes =
                    if (colorType == 1) AppR.drawable.outline_secret_mode_dark_18 else AppR.drawable.outline_secret_mode_white_18
            }
            val hostColor = this.hostColor
            if (hostColor == 1) {
                if (colorType == 0) {
                    midRes = AppR.drawable.outline_lock_black_18
                } else {
                    midRes =
                        if (colorType == 1) AppR.drawable.outline_lock_dark_18 else AppR.drawable.outline_lock_white_18
                }
                this.rightIconX = 0
                rightRes = 0
            } else if (hostColor == 2) {
                if (colorType == 0) {
                    offlineRes = AppR.drawable.outline_offline_pin_black_18
                } else {
                    offlineRes =
                        if (colorType == 1) AppR.drawable.outline_offline_pin_dark_18 else AppR.drawable.outline_offline_pin_white_18
                }
                this.midIconX = 0
                rightRes = offlineRes
                midRes = 0
            } else {
                this.midIconX = 0
                this.rightIconX = 0
                midRes = 0
                rightRes = 0
            }
            if (!this.hostSecure) {
                this.endIconX = 0
                endRes = 0
            } else if (colorType == 0) {
                endRes = AppR.drawable.outline_g_translate_black_18
            } else {
                endRes =
                    if (colorType == 1) AppR.drawable.outline_g_translate_dark_18 else AppR.drawable.outline_g_translate_white_18
            }
            if (this.leftIconRes != leftRes) {
                this.leftIconRes = leftRes
                this.leftIconX = 0
                this.midIconX = 0
                this.rightIconX = 0
                if (leftRes != 0) {
                    this.leftIcon = loadDrawable(context, leftRes)
                } else {
                    this.leftIcon = null
                }
                iconsChanged = true
            } else {
                iconsChanged = false
            }
            if (this.midIconRes != midRes) {
                this.midIconRes = midRes
                this.midIconX = 0
                if (midRes != 0) {
                    this.midIcon = loadDrawable(context, midRes)
                }
                iconsChanged = true
            }
            if (this.rightIconRes != rightRes) {
                this.rightIconRes = rightRes
                this.rightIconX = 0
                if (rightRes != 0) {
                    this.rightIcon = loadDrawable(context, rightRes)
                } else {
                    this.rightIcon = null
                }
                iconsChanged = true
            }
            if (this.endIconRes != endRes) {
                this.endIconRes = endRes
                this.endIconX = 0
                if (endRes != 0) {
                    this.endIcon = loadDrawable(context, endRes)
                } else {
                    this.endIcon = null
                }
                iconsChanged = true
            }
            if (layoutIcons(getHeight())) {
                iconsChanged = true
            }
            if (iconsChanged || found) {
                invalidate()
            }
        }
    }

    fun setAddrIconListener(addrIconListener: AddrIconListener?) {
        this.iconListener = addrIconListener
    }

    fun setAlpha(haloAlpha: Float) {
        faceAlpha = if (haloAlpha > this.maxAlpha) this.maxAlpha else haloAlpha
        onRedraw?.run()
    }




    fun setBgPreColor(bgPreColor: Int) {
        if (this.bgPreColor != bgPreColor) {
            this.bgPreColor = bgPreColor
            if (bgPreColor != 0) {
                val paint = Paint()
                this.bgPrePaint = paint
                paint.setAntiAlias(true)
                this.bgPrePaint!!.setStyle(Paint.Style.FILL)
                this.bgPrePaint!!.setColor(this.bgPreColor)
                this.bgPreAlpha = this.bgPrePaint!!.getAlpha()
            } else {
                this.bgPrePaint = null
            }
            invalidate()
        }
    }

    fun setClickEnabled(clickEnabled: Boolean) {
        this.clickEnabled = clickEnabled
        setAlpha(if (clickEnabled) this.maxAlpha else 0.4f)
        if (clickEnabled) {
            return
        }
        cancelPress()
    }

    fun setEnabled(newEnabledNow: Boolean) {
        enabledNow = newEnabledNow
        setAlpha(if (newEnabledNow) this.maxAlpha else 0.4f)
        if (newEnabledNow) {
            return
        }
        cancelPress()
    }

    fun setHostPreColor(hostPreColor: Int) {
        if (this.hostPreColor != hostPreColor) {
            this.hostPreColor = hostPreColor
            if (hostPreColor != 0) {
                val paint = Paint()
                this.hostPrePaint = paint
                paint.setAntiAlias(true)
                this.hostPrePaint!!.setStyle(Paint.Style.FILL)
                this.hostPrePaint!!.setColor(this.hostPreColor)
                this.hostPreAlpha = this.hostPrePaint!!.getAlpha()
            } else {
                this.hostPrePaint = null
            }
            invalidate()
        }
    }



    fun setMaxAlpha(alpha: Float) {
        if ((this.maxAlpha).compareTo(alpha) == 0) {
            return
        }
        this.maxAlpha = alpha
        setAlpha(alpha)
    }

    fun setNoAnim(pressScaled: Boolean) {
        this.pressScaled = pressScaled
    }

    fun setOnClickListener(onClickListener: OnClickListener?) {
        this.clickListener = onClickListener
    }

    fun setOnLongClickListener(onLongClickListener: OnLongClickListener?) {
        this.longClickListener = onLongClickListener
        longWrapper = if (onLongClickListener != null) LongClickForwarder() else null
    }

    fun setSetting(hasSetting: Boolean) {
        this.hasSetting = hasSetting
    }

    fun setTrnsPreColor(trnsPreColor: Int) {
        if (this.trnsPreColor != trnsPreColor) {
            this.trnsPreColor = trnsPreColor
            if (trnsPreColor != 0) {
                val paint = Paint()
                this.trnsPrePaint = paint
                paint.setAntiAlias(true)
                this.trnsPrePaint!!.setStyle(Paint.Style.FILL)
                this.trnsPrePaint!!.setColor(this.trnsPreColor)
                this.trnsPreAlpha = this.trnsPrePaint!!.getAlpha()
            } else {
                this.trnsPrePaint = null
            }
            invalidate()
        }
    }

    fun setVisibility(visibility: Int) {
        setAlpha(if (this.clickEnabled && isEnabled()) this.maxAlpha else 0.4f)
        shownState = visibility
        onVisibility?.run()
        if (visibility != 0) {
            cancelPress()
        }
    }

    fun setupIcons(webViewActivity: WebViewActivity?, secure: Boolean, rawColorType: Int) {
        // a light custom colour gets the light bar's dark icons and text
        val colorType = MainUtil.barInk(rawColorType)
        var secure = secure
        var found: Boolean
        val endRes: Int
        var changed = false
        if (this.faceKind.toInt() == 1) {
            if (TextUtils.isEmpty(this.hostText)) {
                secure = false
            }
            val hostSecure = this.hostSecure
            if (hostSecure != secure) {
                this.hostSecure = secure
                found = true
            } else {
                secure = hostSecure
                found = false
            }
            if (!secure) {
                this.endIconX = 0
                endRes = 0
            } else if (colorType == 0) {
                endRes = AppR.drawable.outline_g_translate_black_18
            } else {
                endRes =
                    if (colorType == 1) AppR.drawable.outline_g_translate_dark_18 else AppR.drawable.outline_g_translate_white_18
            }
            if (this.endIconRes != endRes) {
                this.endIconRes = endRes
                this.endIconX = 0
                if (endRes != 0) {
                    this.endIcon = loadDrawable(webViewActivity, endRes)
                } else {
                    this.endIcon = null
                }
                found = true
            }
            if (this.endIconRes != 0 && this.endIconX == 0) {
                val offset = iconOffset(
                    this.endIcon,
                    (getWidth() - MainApp.dp48) + ((MainApp.dp48 - Math.round(
                        dpToPx(
                            webViewActivity,
                            18.0f
                        )
                    )) / 2),
                    getHeight()
                )
                this.endIconX = offset
                if (offset != 0) {
                    found = true
                }
            }
            val endIconX = this.endIconX
            val rectF = this.trnsPreRect
            if (endIconX != 0) {
                if (rectF == null) {
                    this.trnsPreRect = RectF()
                    found = true
                }
                setTrnsPreRect(getHeight())
            } else if (rectF != null) {
                this.trnsPreRect = null
                found = true
            }
            if (!this.hasSetting) {
                changed = (if (this.endIconX != 0 && AlbumSettings.transNoti.value) setDot(true, false) else setDot(false, false)) || found
            }
            if (changed) {
                invalidate()
            }
        }
    }

    fun layoutCircle(width: Int, height: Int) {
        val radius = min(this.centerX, this.centerY)
        this.circleRadius = radius
        val cappedRadius = min(radius, MainApp.dp48.toFloat())
        this.circleRadius = cappedRadius
        val rectF = this.circleRect
        if (rectF == null) {
            return
        }
        if (!this.circleFilled) {
            rectF.set(0.0f, 0.0f, width.toFloat(), height.toFloat())
        } else {
            val centerX = this.centerX
            rectF.set(centerX - cappedRadius, 0.0f, centerX + cappedRadius, height.toFloat())
        }
    }

    fun setCircle(circleShown: Boolean, circleFilled: Boolean) {
        this.circleShown = circleShown
        this.circleFilled = circleFilled
        val width = getWidth()
        val height = getHeight()
        if (width == 0 || height == 0) {
            return
        }
        this.centerX = width / 2.0f
        this.centerY = height / 2.0f
        layoutCircle(width, height)
    }

    fun setDot(changed: Boolean, animate: Boolean): Boolean {
        val paint = this.dotPaint
        if (!changed) {
            if (paint != null) {
                this.dotPaint = null
                if (animate) {
                    invalidate()
                    return false
                }
                return true
            }
            return false
        }
        if (paint == null) {
            val dotPaint = Paint()
            this.dotPaint = dotPaint
            dotPaint.setAntiAlias(true)
            this.dotPaint!!.setStyle(Paint.Style.FILL)
            this.dotPaint!!.setColor(-65536)
            val context = getContext()
            this.dotRtl = isRtl(context)
            this.dotRadius = dpToPx(context, 3.0f)
            this.dotMargin = dpToPx(context, 9.0f)
            if (animate) {
                invalidate()
                return false
            }
            return true
        }
        return false
    }

    fun setupTab(currentTabIndex: Int, size: Int, rawColorType: Int) {
        // a light custom colour gets the light bar's dark icons and text
        val colorType = MainUtil.barInk(rawColorType)
        var found: Boolean
        val textSize: Byte
        var color: Int
        var paint: Paint?
        var width: Int
        var height: Int
        this.faceKind = 2.toByte()
        clearHost()
        var changed = true
        if (this.tabStroke == 0.0f) {
            this.tabStroke = dpToPx(getContext(), 1.5f)
            found = true
        } else {
            found = false
        }
        if (this.tabOutlinePaint == null) {
            val outlinePaint = Paint()
            this.tabOutlinePaint = outlinePaint
            outlinePaint.setAntiAlias(true)
            this.tabOutlinePaint!!.setStyle(Paint.Style.STROKE)
            this.tabOutlinePaint!!.setStrokeWidth(this.tabStroke)
            found = true
        }
        if (this.tabBox == null) {
            this.tabBox = RectF()
            found = true
        }
        var label = " "
        if (size != 0) {
            if (size == 1) {
                label = "1"
            } else if (ZtwoSettings.tabIndi.value) {
                label = (currentTabIndex + 1).toString() + "/" + size
            } else {
                // more than 99 tabs: "99+", so the number never crowds the icon
                val countText = if (size > 99) "99+" else size.toString()
                if (!TextUtils.isEmpty(countText)) {
                    label = countText
                }
            }
        }
        val length = label.length
        if (length > 7) {
            textSize = 9
        } else {
            textSize = if (length > 3) 11.toByte() else 12.toByte()
        }
        val tabTextPaint = this.tabTextPaint
        if (tabTextPaint != null) {
            if (this.tabTextSize != textSize) {
                this.tabTextSize = textSize
                tabTextPaint.setTextSize(Math.round(dpToPx(getContext(), textSize.toFloat())).toFloat())
                this.tabText = null
            }
            if (this.tabTextBounds == null) {
                this.tabTextBounds = Rect()
                this.tabText = null
                found = true
            }
            if (label != this.tabText) {
                this.tabText = label
                this.tabTextPaint!!.getTextBounds(label, 0, label.length, this.tabTextBounds)
                width = getWidth()
                height = getHeight()
                if (width != 0 && height != 0) {
                    layoutTabBox(width / 2.0f, height / 2.0f)
                }
                found = true
            }
            if (colorType == 0) {
                color = -16777216
            } else if (colorType == 1) {
                color = -328966
            } else {
                color = -1
            }
            if (this.tabColor != color) {
                this.tabColor = color
                this.tabOutlinePaint!!.setColor(color)
                paint = this.tabTextPaint
                if (paint != null) {
                    paint.setColor(color)
                }
            } else {
                changed = found
            }
            if (changed) {
                invalidate()
            }
        }
        val textPaint = Paint()
        this.tabTextPaint = textPaint
        textPaint.setAntiAlias(true)
        this.tabTextPaint!!.setColor(this.tabColor)
        this.tabTextSize = textSize
        this.tabTextPaint!!.setTextSize(Math.round(dpToPx(getContext(), textSize.toFloat())).toFloat())
        this.tabText = null
        found = true
        if (this.tabTextBounds == null) {
            this.tabTextBounds = Rect()
            this.tabText = null
            found = true
        }
        if (label != this.tabText) {
            this.tabText = label
            this.tabTextPaint!!.getTextBounds(label, 0, label.length, this.tabTextBounds)
            width = getWidth()
            height = getHeight()
            if (width != 0) {
                layoutTabBox(width / 2.0f, height / 2.0f)
            }
            found = true
        }
        if (colorType == 0) {
            color = -16777216
        } else if (colorType == 1) {
            color = -328966
        } else {
            color = -1
        }
        if (this.tabColor != color) {
            this.tabColor = color
            this.tabOutlinePaint!!.setColor(color)
            paint = this.tabTextPaint
            if (paint != null) {
                paint.setColor(color)
            }
        } else {
            changed = found
        }
        if (changed) {
            invalidate()
        }
    }

    /**
     * Room kept free at the pill's right end (px) for a button the bar draws there (the reload button, 2026-11-19); the
     * translate icon and the text move left by it.
     */
    var endReserve: Int = 0
        set(value) {
            if (field == value) return
            field = value
            this.endIconX = 0
            if (getHeight() != 0) layoutIcons(getHeight())
            invalidate()
        }

    fun layoutTabBox(centerX: kotlin.Float, centerY: kotlin.Float) {
        val rect = this.tabTextBounds
        if (rect == null || this.tabBox == null) {
            return
        }
        var halfWidth = (rect.width() / 2.0f) + MainApp.dp4
        val halfHeight = dpToPx(getContext(), 7.8f)
        if (halfWidth < halfHeight) {
            halfWidth = halfHeight
        } else {
            val maxHalfWidth = centerX - this.tabStroke
            if (halfWidth > maxHalfWidth) {
                halfWidth = maxHalfWidth
            }
        }
        this.tabBox!!.set(centerX - halfWidth, centerY - halfHeight, centerX + halfWidth, centerY + halfHeight)
    }

    companion object {
        @JvmStatic
        fun pressScale(barColor: Int): kotlin.Float {
            return if (barColor == 0 || barColor == 1) 0.7f else 0.8f
        }

        @JvmStatic
        fun bgColorFor(barColor: Int, defaultColor: Int, hasSetting: Boolean): Int {
            if (barColor == 0) {
                return -1118482
            }
            if (barColor == 1) {
                return if (hasSetting) -12632257 else -14606047
            }
            return defaultColor
        }

        @JvmStatic
        fun pressColorFor(rawBarColor: Int, pressed: Boolean): Int {
            val barColor = MainUtil.barInk(rawBarColor)
            if (pressed) {
                return if (barColor == 0) 1895825408 else 1912602623
            }
            return if (barColor == 0) -1308622848 else -1292174598
        }

        @JvmStatic
        fun pillColorFor(barColor: Int, naviColor: Int, hasSetting: Boolean): Int {
            if (hasSetting) {
                return bgColorFor(barColor, naviColor, hasSetting)
            }
            // the address pill: white on the light bar, a lifted grey on the dark bar (2026-11-19, bars are no longer
            // pure white / black, see MyBarFrame.BAR_LIGHT / BAR_DARK)
            if (barColor == 0) {
                return -1
            }
            if (barColor == 1) {
                return 0xFF2E3036.toInt()
            }
            return naviColor
        }
    }
}
