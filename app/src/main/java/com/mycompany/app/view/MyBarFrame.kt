package com.mycompany.app.view

import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil.topBarSize
import com.mycompany.app.main.MainUtil.topBarHeight
import com.mycompany.app.main.MainUtil.hasBottomTitle
import com.mycompany.app.main.MainUtil.cancelToast
import com.mycompany.app.main.MainUtil.bottomBarHeightPref
import com.mycompany.app.main.MainUtil.bottomInsetTotal
import com.mycompany.app.main.MainUtil.barTint
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefEditor.Companion.withAlpha
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.web.WebViewActivity
import kotlin.math.max

@Suppress("deprecation")
class MyBarFrame(webViewActivity: WebViewActivity?) : FrameLayout(webViewActivity!!) {
    @JvmField
    var isTop: Boolean = false
    @JvmField
    var lineColor: Int = 0
    @JvmField
    var lineHalf: Float
    @JvmField
    var linePaint: Paint?
    @JvmField
    var lineAlpha: Int = 0
    @JvmField
    var touchOff: Boolean = false
    @JvmField
    var roundOn: Boolean = false
    @JvmField
    var roundBottom: Boolean = false
    @JvmField
    var lastTabBar: Int = 0
    @JvmField
    var mainBar: View? = null
    @JvmField
    var secondBar: View? = null
    @JvmField
    var tabBarView: View? = null

    internal inner class BarOutline : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            if (outline != null) {
                val myBarFrame = this@MyBarFrame
                if (myBarFrame.roundOn) {
                    val width = myBarFrame.getWidth()
                    val height = myBarFrame.getHeight()
                    val dp4Px = MainApp.dp4
                    if (!myBarFrame.isTop || !hasBottomTitle()) {
                        outline.setRoundRect(dp4Px, 0, width - dp4Px, height, MainApp.dp16.toFloat())
                        return
                    }
                    val tabBar = BrowserSettings.tabBar.value
                    if (tabBar == 1) {
                        outline.setRoundRect(dp4Px, 0, width - dp4Px, height - dp4Px, MainApp.dp16.toFloat())
                    } else if (tabBar == 2) {
                        outline.setRoundRect(dp4Px, dp4Px, width - dp4Px, height, MainApp.dp16.toFloat())
                    } else {
                        val dp8Px = MainApp.dp8
                        outline.setRoundRect(dp4Px, dp8Px, width - dp4Px, height - dp8Px, MainApp.dp16.toFloat())
                    }
                }
            }
        }
    }

    internal inner class UpdateLayoutTask : Runnable {
        override fun run() {
            this@MyBarFrame.updateLayout()
        }
    }

    init {
        setOutlineProvider(ViewOutlineProvider.BOUNDS)
        setElevation(MainApp.dp4 * 0.75f)
        val f = MainApp.dp1.toFloat()
        this.lineHalf = f / 2.0f
        val paint = Paint()
        this.linePaint = paint
        paint.setAntiAlias(true)
        this.linePaint!!.setStyle(Paint.Style.STROKE)
        this.linePaint!!.setStrokeWidth(f)
    }

    private val barMgnBot: Int
        get() {
            val i = if (BrowserSettings.tabBar.value == 3) PdfSettings.tabHeight.value else 0
            return if (!PdfSettings.topToBottom.value || MainActivity.splitLand) i else topBarSize() + i
        }

    private val barMgnTab: Int
        get() {
            val i = BrowserSettings.tabBar.value
            if (i == 2) {
                if (PdfSettings.topToBottom.value) {
                    return 0
                }
                return topBarSize()
            }
            if (i != 4) {
                return 0
            }
            if (!PdfSettings.topToBottom.value) {
                return MainUtil.bottomBarHeightPref()
            }
            if (MainActivity.splitLand) {
                return max(topBarSize(), MainUtil.bottomBarHeightPref())
            }
            return MainUtil.bottomBarHeightPref() + topBarSize()
        }

    private val barMgnTop: Int
        get() {
            val z = PdfSettings.topToBottom.value
            val i = BrowserSettings.tabBar.value
            if (z) {
                if (i == 3) {
                    return PdfSettings.tabHeight.value
                }
                return 0
            }
            if (i == 1) {
                return PdfSettings.tabHeight.value
            }
            return 0
        }

    private fun setChildAlpha(alpha: Float) {
        val childCount = getChildCount()
        if (childCount != 0) {
            for (index in 0..<childCount) {
                val childAt = getChildAt(index)
                if (childAt != null) {
                    childAt.setAlpha(alpha)
                }
            }
        }
    }

    private fun setChildVisible(visibility: Int) {
        val childCount = getChildCount()
        if (childCount != 0) {
            for (index in 0..<childCount) {
                val childAt = getChildAt(index)
                if (childAt != null) {
                    childAt.setVisibility(visibility)
                }
            }
        }
    }

    private fun setRoundClip(roundOn: Boolean) {
        if (this.isTop) {
            val bottomTitle = hasBottomTitle()
            if (this.roundOn == roundOn && this.roundBottom == bottomTitle && this.lastTabBar == BrowserSettings.tabBar.value) {
                return
            }
            this.roundOn = roundOn
            this.roundBottom = bottomTitle
            this.lastTabBar = BrowserSettings.tabBar.value
        } else if (this.roundOn == roundOn) {
            return
        } else {
            this.roundOn = roundOn
        }
        if (!roundOn) {
            setClipToOutline(false)
            // a soft shadow on the bar's edges (seen where the page scrolls under the bar)
            setOutlineProvider(ViewOutlineProvider.BOUNDS)
        } else {
            setOutlineProvider(BarOutline())
            setClipToOutline(true)
        }
    }

    fun setChild(view: View?, index: Int) {
        val barMgnTab: Int
        if (index == 0) {
            this.mainBar = view
        } else if (index == 1) {
            this.secondBar = view
        } else if (index == 2) {
            this.tabBarView = view
        }
        updateVisibility()
        val width = childWidth(index)
        val height = childHeight(index)
        if (index == 0) {
            barMgnTab = this.barMgnTop
        } else if (index == 1) {
            barMgnTab = this.barMgnBot
        } else {
            barMgnTab = if (index == 2) this.barMgnTab else 0
        }
        val layoutParams = LayoutParams(width, height)
        layoutParams.topMargin = barMgnTab
        if (index == 1) {
            layoutParams.gravity = 8388613
        } else {
            layoutParams.gravity = 8388611
        }
        if (index == 3) {
            setChildVisible(8)
        }
        addView(view, layoutParams)
    }

    fun updateLayout() {
        updateVisibility()
        val mainChanged = layoutChild(this.mainBar, 0)
        val secondChanged = layoutChild(this.secondBar, 1)
        val tabBarChanged = layoutChild(this.tabBarView, 2)
        if (mainChanged || secondChanged || tabBarChanged) {
            requestLayout()
        }
    }

    fun layoutChild(view: View?, index: Int): Boolean {
        val barMgnTab: Int
        val layoutParams: LayoutParams? = if (view != null) (view.getLayoutParams() as LayoutParams?) else null
        if (view != null && layoutParams != null) {
            val width = childWidth(index)
            val height = childHeight(index)
            if (index == 0) {
                barMgnTab = this.barMgnTop
            } else if (index == 1) {
                barMgnTab = this.barMgnBot
            } else {
                barMgnTab = if (index == 2) this.barMgnTab else 0
            }
            if (layoutParams.width != width || layoutParams.height != height || layoutParams.topMargin != barMgnTab) {
                layoutParams.width = width
                layoutParams.height = height
                layoutParams.topMargin = barMgnTab
                return true
            }
        }
        return false
    }

    fun reapplyRound() {
        setRoundClip(this.roundOn)
    }

    public override fun dispatchDraw(canvas: Canvas) {
        val target: Canvas
        val height = getHeight()
        if (height == 0) {
            return
        }
        val lineColor = this.lineColor
        if (lineColor != 0) {
            canvas.drawColor(lineColor)
        }
        target = canvas
        super.dispatchDraw(target)
        // the hairline on the edge that faces the page, drawn over the bars so nothing covers it
        if (this.lineAlpha != 0 && this.linePaint != null) {
            val lineHalf = this.lineHalf
            if (this.isTop) {
                val bottom = height.toFloat()
                target.drawLine(0.0f, bottom - lineHalf, getWidth().toFloat(), bottom - lineHalf, this.linePaint!!)
            } else {
                target.drawLine(0.0f, lineHalf, getWidth().toFloat(), lineHalf, this.linePaint!!)
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (this.touchOff) {
            return true
        }
        // A touch on the bar never reaches the page behind it (the page runs under the bars). A button that does not
        // take the touch (the tab button while it is being set up for a new page, the gaps between buttons) used to
        // leave it unconsumed, and the link right behind the bar was tapped (owner, 2026-11-20: "About Google" on
        // google.com behind the tab button).
        super.dispatchTouchEvent(event)
        return true
    }

    fun updateVisibility() {
        if ((if (this.isTop) topBarHeight() else MainUtil.bottomInsetTotal()) != 0) {
            setVisibility(View.VISIBLE)
        } else if (this.isTop) {
            setVisibility(View.INVISIBLE)
        } else {
            setVisibility(View.GONE)
        }
    }

    fun childHeight(index: Int): Int {
        if (index == 0) {
            return if (PdfSettings.topToBottom.value && MainActivity.splitLand) max(topBarSize(), MainUtil.bottomBarHeightPref()) else topBarSize()
        }
        if (index == 1) {
            return if (PdfSettings.topToBottom.value && MainActivity.splitLand) max(topBarSize(), MainUtil.bottomBarHeightPref()) else MainUtil.bottomBarHeightPref()
        }
        if (index == 2) {
            return PdfSettings.tabHeight.value
        }
        val barSize = if (this.isTop) topBarSize() else MainUtil.bottomBarHeightPref()
        val dp48Px = MainApp.dp48
        return if (barSize < dp48Px) dp48Px else barSize
    }

    fun childWidth(index: Int): Int {
        if (index == 0) {
            if (PdfSettings.topToBottom.value && MainActivity.splitLand) {
                return MyBarView.Companion.splitWidth(getWidth(), true)
            }
            return -1
        }
        if (index == 1 && PdfSettings.topToBottom.value && MainActivity.splitLand) {
            return MyBarView.Companion.splitWidth(getWidth(), false)
        }
        return -1
    }

    fun removeChild(view: View?) {
        removeView(view)
        setChildVisible(0)
    }

    fun applyTint(roundOn: Boolean, onStartPage: Boolean) {
        val newLineColor: Int
        val found: Boolean
        var lineAlpha = 0
        val tint = MainUtil.barTint(MainUtil.bottomBarColor(), onStartPage)
        val alpha = EditorSettings.botAlpha.value
        var tinted = true
        // a step off pure white / black, so the bar stands apart from a white / black page (2026-11-19); the theme
        // colour when the bars take it
        newLineColor = PrefEditor.withAlpha(if (tint == 0) BAR_LIGHT else if (tint == 1) BAR_DARK else tint, alpha)
        if (this.lineColor != newLineColor) {
            this.lineColor = newLineColor
            found = true
        } else {
            found = false
        }
        val paint = this.linePaint
        if (paint == null) {
            tinted = found
        } else {
            if (!roundOn) {
                lineAlpha = dividerFor(newLineColor)
            }
            if (this.lineAlpha != lineAlpha) {
                this.lineAlpha = lineAlpha
                paint.setColor(lineAlpha)
            } else {
                tinted = found
            }
        }
        setRoundClip(roundOn)
        if (tinted) {
            invalidate()
        }
    }

    fun applyTintFor(themeColor: Int, roundOn: Boolean, onStartPage: Boolean) {
        val lineColor: Int
        val found: Boolean
        val tint = MainUtil.barTint(themeColor, onStartPage)
        var tinted = true
        val base = if (tint == 0) BAR_LIGHT else if (tint == 1) BAR_DARK else tint
        // the top bar's transparency (layout editor, 2026-11-19), like the bottom bar's in [applyTint]
        lineColor = PrefEditor.withAlpha(base, EditorSettings.topAlpha.value)
        var lineAlpha = 0
        if (this.lineColor != lineColor) {
            this.lineColor = lineColor
            found = true
        } else {
            found = false
        }
        val paint = this.linePaint
        if (paint == null) {
            tinted = found
        } else {
            if (!roundOn) {
                lineAlpha = dividerFor(lineColor)
            }
            if (this.lineAlpha != lineAlpha) {
                this.lineAlpha = lineAlpha
                paint.setColor(lineAlpha)
            } else {
                tinted = found
            }
        }
        setRoundClip(roundOn)
        if (tinted) {
            invalidate()
        }
    }

    public override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (PdfSettings.topToBottom.value) {
            post(UpdateLayoutTask())
        }
    }

    fun setDisabled(touchOff: Boolean) {
        if (this.touchOff == touchOff) {
            return
        }
        this.touchOff = touchOff
        setChildAlpha(if (touchOff) 0.4f else 1.0f)
        invalidate()
    }

    fun setIsTop(isTop: Boolean) {
        this.isTop = isTop
    }

    companion object {
        /** Bar tones: a step off pure white / black so the bars do not melt into a white / black page. */
        const val BAR_LIGHT: Int = 0xFFF1F2F5.toInt()
        const val BAR_DARK: Int = 0xFF1C1D22.toInt()

        /** The hairline between bar and page: dark on light bars, light on dark ones, about 12 %. */
        @JvmStatic
        fun dividerFor(barColor: Int): Int {
            val r = (barColor shr 16) and 0xFF
            val g = (barColor shr 8) and 0xFF
            val b = barColor and 0xFF
            val light = (0.299 * r + 0.587 * g + 0.114 * b) > 140
            return if (light) 0x1F000000 else 0x24FFFFFF
        }
    }
}
