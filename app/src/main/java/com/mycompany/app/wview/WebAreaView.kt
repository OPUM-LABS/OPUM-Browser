package com.mycompany.app.wview

import android.content.Context
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.ReadSettings
import android.graphics.Outline
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.browser.ComposeOverlay
import kotlin.math.roundToInt

/**
 * The small toolbar of the "select an area" tool (pick a block of the page): help, single / multiple selection, done,
 * close. Compose since 2026-11-16, same API as the old LinearLayout:
 * - it is added to the page body at [getViewWidth] × [getViewHeight] and placed by [placeIn] (near a point, centred with
 *   -1234 / -1234, bottom right with -5678 / -5678) and [moveTo] (clamped to the body);
 * - dragging it by more than the touch slop moves it (the press on a button is cancelled then);
 * - [applyDark] re-reads the theme, [applyMulti] the multi-selection icon, [release] releases it; [touched] is true while a finger is down.
 */
class WebAreaView(context: Context) : ComposeOverlay(context) {
    interface WebAreaListener {
        /** Close. */
        fun onClose()
        /** Done. */
        fun onDone()
        /** Multi-selection was toggled. */
        fun onMultiToggled()
        /** Help. */
        fun onHelp()
    }

    /** False after [release]. */
    @JvmField var alive = true
    @JvmField var touched = false

    private var listener: WebAreaListener? = null
    private val viewWidth = Math.round(MainUtil.dpToPx(context, 208.0f))
    private val viewHeight = MainApp.dp48

    private var busy = false
    private var dragging = false
    private var downX = 0
    private var downY = 0
    private var offX = 0
    private var offY = 0
    private var left = 0
    private var top = 0
    private var areaX = 0
    private var areaY = 0
    private var areaW = 0
    private var areaH = 0

    private var dark by mutableStateOf(AppState.darkUi)
    private var multi by mutableStateOf(ReadSettings.areaMulti.value)
    private var noti by mutableStateOf(ReadSettings.notiAre4.value)

    init {
        // the card shadow and rounded clip stay on the view itself
        elevation = MainApp.dp16.toFloat()
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                if (view == null || outline == null) return
                outline.setRoundRect(0, 0, view.width, view.height, MainApp.dp16.toFloat())
            }
        }
        clipToOutline = true
        // swallow taps between the buttons, as before
        setOnClickListener { }
        setPage({ dark }) { Bar() }
    }

    fun getViewWidth(): Int = viewWidth
    fun getViewHeight(): Int = viewHeight

    fun setFltListener(webAreaListener: WebAreaListener?) {
        listener = webAreaListener
    }

    /** Re-reads the dark-mode setting. */
    fun applyDark() {
        if (!alive) return
        dark = AppState.darkUi
        applyMulti()
    }

    /** Releases the toolbar. */
    fun release() {
        alive = false
        listener = null
    }

    /** Re-reads the single / multiple selection setting. */
    fun applyMulti() {
        multi = ReadSettings.areaMulti.value
    }

    /** Moves the toolbar to ([x], [y]) inside the body, clamped. */
    fun moveTo(touchX: Int, touchY: Int) {
        if (!alive) return
        var clampedLeft = touchX
        var clampedTop = touchY
        if (clampedLeft + viewWidth > areaW) clampedLeft = areaW - viewWidth
        if (clampedTop + viewHeight > areaH) clampedTop = areaH - viewHeight
        if (clampedLeft < 0) clampedLeft = 0
        if (clampedTop < 0) clampedTop = 0
        left = clampedLeft
        top = clampedTop
        setX((clampedLeft + areaX).toFloat())
        setY((clampedTop + areaY).toFloat())
    }

    /** Places the toolbar in [view] (the page body) near ([x], [y]); see the class comment for the special values. */
    fun placeIn(view: View?, x: Int, y: Int) {
        if (view == null) return
        areaX = view.paddingLeft
        areaY = view.paddingTop
        areaW = view.width - areaX - view.paddingRight
        areaH = view.height - areaY - view.paddingBottom
        val viewHeightNow = viewHeight
        val viewWidthNow = viewWidth
        val left: Int
        val top: Int
        if (x == -1234 && y == -1234) {
            left = (areaW - viewWidthNow) / 2
            top = (areaH - viewHeightNow) / 2
        } else if (x == -5678 && y == -5678) {
            left = areaW - viewWidthNow
            top = areaH - viewHeightNow
        } else {
            val py = y - areaY
            left = (x - areaX) - (viewWidthNow / 2)
            top = if (py > (viewHeightNow shl 1)) (py - (viewHeightNow shl 1)) - MainApp.dp4 else (viewHeightNow / 2) + py
        }
        moveTo(left, top)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (!alive) return super.dispatchTouchEvent(ev)
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                touched = true
                dragging = false
                downX = ev.rawX.roundToInt()
                downY = ev.rawY.roundToInt()
                offX = left - downX
                offY = top - downY
            }
            MotionEvent.ACTION_MOVE -> {
                val dist = MainUtil.pointDistance(downX.toFloat(), ev.rawX, downY.toFloat(), ev.rawY)
                if (dragging) {
                    if (dist >= MainApp.dp4 / 2.0f) {
                        downX = ev.rawX.roundToInt()
                        downY = ev.rawY.roundToInt()
                        moveTo(downX + offX, downY + offY)
                    }
                    return true
                } else if (dist >= MainApp.dp4) {
                    dragging = true
                    // the finger is moving the bar now: cancel the press on the button under it
                    val cancel = MotionEvent.obtain(ev)
                    cancel.action = MotionEvent.ACTION_CANCEL
                    super.dispatchTouchEvent(cancel)
                    cancel.recycle()
                    downX = ev.rawX.roundToInt()
                    downY = ev.rawY.roundToInt()
                    moveTo(downX + offX, downY + offY)
                    return true
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touched = false
                val was = dragging
                dragging = false
                if (was) return true
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /** Runs [action] once per tap, after the press finished drawing (the old buttons posted their actions). */
    private fun tap(action: () -> Unit) {
        if (dragging || busy) return
        busy = true
        post {
            action()
            busy = false
        }
    }

    @Composable
    private fun Bar() {
        val density = LocalDensity.current
        val corner = with(density) { MainApp.dp16.toDp() }
        val stroke = with(density) { MainApp.dp1.toDp() }
        val pad = with(density) { MainApp.dp8.toDp() }
        val button = with(density) { viewHeight.toDp() }
        val shape = RoundedCornerShape(corner)
        val bg = if (dark) Color.Black else Color.White
        val line = if (dark) Color(0xFFB0B0B0) else Color.Black
        val tint = if (dark) Color(0xFFF1F1F1) else Color.Black
        Row(
            Modifier.fillMaxSize().background(bg, shape).border(stroke, line, shape).padding(horizontal = pad),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BarButton(R.drawable.outline_help_black_24, tint, button, dot = noti) {
                tap {
                    if (ReadSettings.notiAre4.value) {
                        ReadSettings.edit(context) { set(ReadSettings.notiAre4, false) }
                        noti = false
                    }
                    listener?.onHelp()
                }
            }
            BarButton(if (multi) R.drawable.outline_shadow_add_black_24 else R.drawable.outline_shadow_minus_black_24, tint, button) {
                if (!dragging && !busy && listener != null) {
                    busy = true
                    post {
                        val multiFlag = !ReadSettings.areaMulti.value
                        ReadSettings.edit(context) { set(ReadSettings.areaMulti, multiFlag) }
                        applyMulti()
                        listener?.onMultiToggled()
                        postDelayed({ busy = false }, 200L)
                    }
                }
            }
            BarButton(R.drawable.outline_check_black_24, tint, button) { tap { listener?.onDone() } }
            BarButton(R.drawable.outline_close_black_24, tint, button) { tap { listener?.onClose() } }
        }
    }

    @Composable
    private fun BarButton(icon: Int, tint: Color, size: androidx.compose.ui.unit.Dp, dot: Boolean = false, onClick: () -> Unit) {
        val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        Box(
            Modifier.size(size)
                .clickable(remember { MutableInteractionSource() }, ripple(bounded = false, radius = size / 2), onClick = onClick)
                .drawWithContent {
                    drawContent()
                    if (dot) {
                        val off = 9.dp.toPx()
                        val cx = center.x + if (rtl) off else -off
                        drawCircle(Color.Red, 3.dp.toPx(), Offset(cx, center.y - off))
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(icon), contentDescription = null, tint = tint)
        }
    }
}
