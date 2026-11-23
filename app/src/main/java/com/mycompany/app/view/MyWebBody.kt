package com.mycompany.app.view

import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.MainSettings
import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.widget.RelativeLayout
import com.mycompany.app.image.ImageSizeListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil.screenSize
import com.mycompany.app.main.MainUtil.loadDrawable
import com.mycompany.app.main.MainUtil.barTint
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.pref.PrefTts
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.web.WebViewActivity
import kotlin.math.abs

@Suppress("deprecation")
class MyWebBody(context: Context?) : RelativeLayout(context) {
    @JvmField
    var flipSides: Boolean = false
    @JvmField
    var topBarColor: Int = 0
    @JvmField
    var topPaint: Paint? = null
    @JvmField
    var topHeight: Int = 0
    @JvmField
    var topRect: Rect? = null
    @JvmField
    var bottomColor: Int = 0
    @JvmField
    var bottomPaint: Paint? = null
    @JvmField
    var bottomHeight: Int = 0
    @JvmField
    var bottomRect: Rect? = null
    @JvmField
    var insetTop: Int = 0
    @JvmField
    var themeColor: Int = 0
    @JvmField
    var filterTint: Int = 0
    @JvmField
    var editTint: Int = 0
    @JvmField
    var editPaint: Paint? = null
    @JvmField
    var editLeftRect: Rect? = null
    @JvmField
    var editTopRect: Rect? = null
    @JvmField
    var editRightRect: Rect? = null
    @JvmField
    var editBottomRect: Rect? = null
    @JvmField
    var sizeCallback: ImageSizeListener? = null
    @JvmField
    var handCallback: HandListener? = null
    @JvmField
    var handMoved: Boolean = false
    @JvmField
    var downRawX: Float = 0f
    @JvmField
    var downRawY: Float = 0f
    @JvmField
    var handNotiIcon: Drawable? = null
    @JvmField
    var handIconRes: Int = 0

    internal inner class NotifyHandTask : Runnable {
        override fun run() {
            val handListener = this@MyWebBody.handCallback
            if (handListener != null) {
                handListener.onHand()
            }
        }
    }

    interface HandListener {
        fun onHand()
    }

    private fun setHandNotiPos(activity: Activity?) {
        if (this.handNotiIcon == null) {
            return
        }
        val width = getWidth() / 2
        val handTop = handTopPos(activity) / 2
        val half = (MainApp.dp40 - MainApp.dp16) / 2
        this.handNotiIcon!!.setBounds(width - half, handTop - half, width + half, handTop + half)
        invalidate()
    }

    fun addViewQuick(view: View?, layoutParams: LayoutParams?) {
        if (view == null) {
            return
        }
        try {
            addViewInLayout(view, 0, layoutParams, true)
        } catch (unused: Exception) {
        }
    }

    public override fun dispatchDraw(canvas: Canvas) {
        syncBottomStrip()
        syncTopStrip()
        val topRect: Rect? = if (this.topHeight != 0) this.topRect else null
        val topPaint: Paint? = if (this.topHeight != 0 && topRect != null) this.topPaint else null
        if (this.topHeight != 0 && topRect != null && topPaint != null) {
            canvas.drawRect(topRect, topPaint)
        }
        val bottomRect: Rect? = if (this.bottomHeight != 0) this.bottomRect else null
        val bottomPaint: Paint? = if (this.bottomHeight != 0 && bottomRect != null) this.bottomPaint else null
        if (this.bottomHeight != 0 && bottomRect != null && bottomPaint != null) {
            canvas.drawRect(bottomRect, bottomPaint)
        }
        val themeColor = this.themeColor
        if (themeColor != 0) {
            canvas.drawColor(themeColor)
        }
        val drawable = this.handNotiIcon
        if (drawable != null) {
            drawable.draw(canvas)
        }
        super.dispatchDraw(canvas)
        if (this.editTint != 0 && this.editPaint != null) {
            val editLeftRect: Rect? = if (getPaddingLeft() > 0) this.editLeftRect else null
            if (getPaddingLeft() > 0 && editLeftRect != null) {
                canvas.drawRect(editLeftRect, this.editPaint!!)
            }
            val editTopRect: Rect? = if (getPaddingTop() > 0) this.editTopRect else null
            if (getPaddingTop() > 0 && editTopRect != null) {
                canvas.drawRect(editTopRect, this.editPaint!!)
            }
            val editRightRect: Rect? = if (getPaddingRight() > 0) this.editRightRect else null
            if (getPaddingRight() > 0 && editRightRect != null) {
                canvas.drawRect(editRightRect, this.editPaint!!)
            }
            val editBottomRect: Rect? = if (getPaddingBottom() > 0) this.editBottomRect else null
            if (getPaddingBottom() > 0 && editBottomRect != null) {
                canvas.drawRect(editBottomRect, this.editPaint!!)
            }
        }
        val filterTint = this.filterTint
        if (filterTint != 0) {
            canvas.drawColor(filterTint)
        }
    }

    fun handTopPos(activity: Activity?): Int {
        var height = getHeight()
        if (height == 0) {
            val screen = screenSize(activity)
            height = if (screen == null) 0 else screen.height
        }
        var topPercent = TtsSettings.handTop.value
        var bottom = 50
        if (topPercent >= 50) {
            bottom = 90
            if (topPercent > 90) {
                TtsSettings.editLocal { set(TtsSettings.handTop, 90) }
            }
            return Math.round((1.0f - (topPercent / 100.0f)) * height)
        }
        TtsSettings.editLocal { set(TtsSettings.handTop, 50) }
        topPercent = bottom
        return Math.round((1.0f - (topPercent / 100.0f)) * height)
    }

    fun updateEditRects(width: Int, height: Int) {
        if (this.editTint == 0 || width == 0 || height == 0 || this.editTopRect == null) {
            return
        }
        val paddingLeft = getPaddingLeft()
        val paddingTop = getPaddingTop()
        val paddingRight = getPaddingRight()
        val paddingBottom = getPaddingBottom()
        this.editTopRect!!.set(0, 0, width, paddingTop)
        val bottom = height - paddingBottom
        this.editBottomRect!!.set(0, bottom, width, height)
        this.editLeftRect!!.set(0, paddingTop, paddingLeft, bottom)
        this.editRightRect!!.set(width - paddingRight, paddingTop, width, bottom)
    }

    fun updateHandIcon(webViewActivity: WebViewActivity?, onStartPage: Boolean, iconColor: Int) {
        val iconRes = if (MainUtil.barTint(
                iconColor,
                onStartPage
            ) == 0
        ) R.drawable.outline_keyboard_arrow_up_black_24 else R.drawable.outline_keyboard_arrow_up_dark_24
        if (this.handIconRes == iconRes) {
            return
        }
        this.handIconRes = iconRes
        val drawableR = loadDrawable(getContext(), this.handIconRes)
        this.handNotiIcon = drawableR
        drawableR!!.setAlpha(178)
        setHandNotiPos(webViewActivity)
    }

    /**
     * The strip under the bars follows the current navigation-bar height at every draw. It used to be taken from the stored
     * height only when the colour was set, so a 0 from an inset pass while the theme switched left no strip (the page colour
     * showed, black in dark mode) until the app restarted.
     */
    /**
     * The same for the strip under the status bar: returning from another screen can set its height to 0 (the status-bar
     * inset is not measured yet), which left the window colour (black) above coloured bars until the colour changed.
     */
    private fun syncTopStrip() {
        if (this.topPaint == null) return
        val insets = getRootWindowInsets()
        var height = if (insets != null) insets.getInsets(android.view.WindowInsets.Type.statusBars()).top else MainSettings.statusHeight.value
        if (TtsSettings.handMode.value) height += this.insetTop
        if (this.topHeight != height) this.topHeight = height
        if (height == 0) return
        val rect = this.topRect ?: return
        val w = getWidth()
        if (w != 0 && (rect.left != 0 || rect.right != w || rect.top != 0 || rect.bottom != height)) rect.set(0, 0, w, height)
    }

    private fun syncBottomStrip() {
        if (this.bottomPaint == null) return
        // the window's current navigation-bar inset; the stored height can be left at 0 by an inset pass during a theme switch
        val insets = getRootWindowInsets()
        val height = if (insets != null) insets.getInsets(android.view.WindowInsets.Type.navigationBars()).bottom else MainSettings.naviHeight.value
        if (this.bottomHeight != height) this.bottomHeight = height
        if (height == 0) return
        val rect = this.bottomRect ?: return
        val w = getWidth()
        val h = getHeight()
        if (w != 0 && h != 0 && (rect.left != 0 || rect.right != w || rect.bottom != h || rect.top != h - height)) rect.set(0, h - height, w, h)
    }

    var botColor: Int
        get() = this.bottomColor
        set(i) {
            if (this.bottomPaint != null) {
                val i2 = MainSettings.naviHeight.value
                var z = false
                var z2 = true
                if (this.bottomHeight != i2) {
                    this.bottomHeight = i2
                    if (i2 == 0) {
                        invalidate()
                        return
                    }
                    val width = getWidth()
                    val height = getHeight()
                    val i3 = this.bottomHeight
                    val rect: Rect? = if (i3 != 0 && width != 0 && height != 0) this.bottomRect else null
                    if (i3 != 0 && width != 0 && height != 0 && rect != null) {
                        rect.set(0, height - i3, width, height)
                    }
                    z = true
                }
                if (this.bottomColor != i) {
                    this.bottomColor = i
                    this.bottomPaint!!.setColor(i)
                } else {
                    z2 = z
                }
                if (z2) {
                    invalidate()
                }
            }
        }

    val rectBottom: Int
        get() = getHeight() - getPaddingBottom()

    val rectEnd: Int
        get() = if (this.flipSides) getPaddingLeft() else getWidth() - getPaddingRight()

    val rectHeight: Int
        get() = (getHeight() - getPaddingTop()) - getPaddingBottom()

    val rectStart: Int
        get() = if (this.flipSides) getWidth() - getPaddingRight() else getPaddingLeft()

    val rectTop: Int
        get() = getPaddingTop()

    val rectWidth: Int
        get() = (getWidth() - getPaddingLeft()) - getPaddingRight()

    var topColor: Int
        get() = this.topBarColor
        set(i) {
            if (this.topPaint != null) {
                var i2 = MainSettings.statusHeight.value
                if (TtsSettings.handMode.value) {
                    i2 += this.insetTop
                }
                var z = false
                var z2 = true
                if (this.topHeight != i2) {
                    this.topHeight = i2
                    if (i2 == 0) {
                        invalidate()
                        return
                    }
                    val width = getWidth()
                    val height = getHeight()
                    val i3 = this.topHeight
                    val rect: Rect? = if (i3 != 0 && width != 0 && height != 0) this.topRect else null
                    if (i3 != 0 && width != 0 && height != 0 && rect != null) {
                        rect.set(0, 0, width, i3)
                    }
                    z = true
                }
                if (this.topBarColor != i) {
                    this.topBarColor = i
                    this.topPaint!!.setColor(i)
                } else {
                    z2 = z
                }
                if (z2) {
                    invalidate()
                }
            }
        }

    public override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        if (MainConst.isApi30) {
            val topHeight = this.topHeight
            val topRect: Rect? = if (topHeight != 0 && width != 0 && height != 0) this.topRect else null
            if (topHeight != 0 && width != 0 && height != 0 && topRect != null) {
                topRect.set(0, 0, width, topHeight)
            }
            val bottomHeight = this.bottomHeight
            val bottomRect: Rect? = if (bottomHeight != 0 && width != 0 && height != 0) this.bottomRect else null
            if (bottomHeight != 0 && width != 0 && height != 0 && bottomRect != null) {
                bottomRect.set(0, height - bottomHeight, width, height)
            }
        }
        updateEditRects(width, height)
        if (this.handCallback != null) {
            setHandNotiPos(null)
        }
        val imageSizeListener = this.sizeCallback
        if (imageSizeListener != null) {
            imageSizeListener.onSize(this, width, height)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (this.handCallback == null) {
            return super.onTouchEvent(event)
        } else {
            val rawX = event!!.getRawX()
            val rawY = event.getRawY()
            var actionMasked = event.getActionMasked()
            if (actionMasked == 0) {
                this.handMoved = true
                this.downRawX = rawX
                this.downRawY = rawY
                return true
            } else {
                var movedUp = false
                if (actionMasked != 1) {
                    if (actionMasked == 2) {
                        val dx = abs(rawX - this.downRawX)
                        val dy = abs(rawY - this.downRawY)
                        val slop = MainApp.dp40.toFloat()
                        val delta: Float
                        actionMasked = if (((dx - slop).also {
                                delta = it
                            }) == 0.0f) 0 else (if (delta < 0.0f) -1 else 1)
                        if (actionMasked <= 0 && !(dy > slop)) {
                            return true
                        }

                        if (dx < slop && dy > slop) {
                            if (rawY < this.downRawY) {
                                movedUp = true
                            }

                            this.handMoved = movedUp
                        } else if (actionMasked > 0 && dy < slop) {
                            this.handMoved = false
                        }

                        this.downRawX = rawX
                        this.downRawY = rawY
                        return true
                    }

                    if (actionMasked != 3) {
                        return true
                    }
                } else if (this.handMoved && this.handCallback != null) {
                    this.post(NotifyHandTask())
                }

                this.handMoved = false
                return true
            }
        }
    }

    fun setEditColor(editTint: Int) {
        val paint = this.editPaint
        if (paint == null) {
            return
        }
        if (this.editTint != editTint) {
            this.editTint = editTint
            paint.setColor(editTint)
        }
        updateEditRects(getWidth(), getHeight())
        invalidate()
    }

    fun setFilterColor(filterTint: Int) {
        if (this.filterTint == filterTint) {
            return
        }
        this.filterTint = filterTint
        invalidate()
    }

    fun setHandListener(handListener: HandListener?) {
        this.handCallback = handListener
    }

    fun setOldTheme(themeColor: Int) {
        if (this.themeColor == themeColor) {
            return
        }
        this.themeColor = themeColor
        invalidate()
    }

    /** The browser's Compose overlay stack: its full-size slots are laid out inside this padding. */
    @JvmField
    var overlays: com.mycompany.app.ui.browser.OverlayStack? = null

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        overlays?.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom())
        if (this.editTint == 0) {
            return
        }
        updateEditRects(getWidth(), getHeight())
    }

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        super.setPaddingRelative(start, top, end, bottom)
        overlays?.setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom())
        if (this.editTint == 0) {
            return
        }
        updateEditRects(getWidth(), getHeight())
    }

    fun setSizeListener(imageSizeListener: ImageSizeListener?) {
        this.sizeCallback = imageSizeListener
    }

    companion object {
        @JvmStatic
        fun fillPaint(topBarColor: Int): Paint {
            val paint = Paint()
            paint.setAntiAlias(true)
            paint.setStyle(Paint.Style.FILL)
            if (topBarColor != 0) {
                paint.setColor(topBarColor)
            }
            return paint
        }
    }
}
