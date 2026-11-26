package com.mycompany.app.ui.browser

import android.content.Context
import com.mycompany.app.ui.state.AppState
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil

/**
 * A round icon button for the legacy View screens that still build them in code (the side add-tab strip, the intro's
 * page buttons, the image viewer's download button, the quick search and quick-access views), replacing
 * `view/MyButtonImage` (2026-11-17). Compose draws the icon, the optional filled circle behind it ([applyColors] normal colour,
 * radius from [l] / [applyRadii]), an unbounded press ripple in the press colour and the red dot ([setNoti]); the view keeps
 * the old calls. Alpha is capped by [setMaxAlpha]; disabled it shows at 0.4.
 */
class IconButton(context: Context?) : ComposeOverlay(context!!) {
    private var icon by mutableIntStateOf(0)
    private var fill by mutableIntStateOf(0)
    private var press by mutableIntStateOf(0)
    private var fillRadius by mutableFloatStateOf(0f)
    private var haloRadius by mutableFloatStateOf(0f)
    private var dot by mutableStateOf(false)
    private var maxAlpha = 1f
    private var touchListener: View.OnTouchListener? = null
    private var pressedNow = false
    private val nightSide = MainUtil.isRtl(context)

    init {
        setPage({ AppState.darkUi }) {
            val source = remember { MutableInteractionSource() }
            val density = LocalDensity.current
            val halo = if (haloRadius > 0f) haloRadius else if (fillRadius > 0f) fillRadius else MainApp.dp48 / 2f
            Box(
                Modifier.fillMaxSize().clickable(
                    interactionSource = source,
                    indication = ripple(bounded = false, radius = with(density) { halo.toDp() },
                        color = if (press != 0) Color(press) else Color.Unspecified),
                    enabled = isEnabled,
                ) { performClick() },
                contentAlignment = Alignment.Center,
            ) {
                Canvas(Modifier.fillMaxSize()) {
                    if (fill != 0 && fillRadius > 0f) drawCircle(Color(fill), fillRadius, center)
                }
                val res = icon
                if (res != 0) Image(painterResource(res), null)
                if (dot) {
                    Canvas(Modifier.fillMaxSize()) { notiDot(center, nightSide) }
                }
            }
        }
    }

    fun setImageResource(res: Int) {
        icon = res
    }

    /** Normal fill and press colours. */
    fun applyColors(normal: Int, pressed: Int) {
        fill = normal
        press = pressed
    }

    fun setBgPreColor(color: Int) {
        press = color
    }

    /** Fill radius. */
    fun applyFillRadius(radius: Float, animate: Boolean) {
        fillRadius = radius
    }

    /** Fill radius and press-halo radius. */
    fun applyRadii(radius: Float, halo: Float) {
        fillRadius = radius
        haloRadius = halo
    }

    fun setNoti(newDot: Boolean) {
        dot = newDot
    }

    fun setMaxAlpha(newMaxAlpha: Float) {
        maxAlpha = newMaxAlpha
        super.setAlpha(if (isEnabled) newMaxAlpha else 0.4f)
    }

    override fun setAlpha(alpha: Float) {
        super.setAlpha(if (alpha > maxAlpha) maxAlpha else alpha)
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        super.setAlpha(if (enabled) maxAlpha else 0.4f)
        invalidateCompose()
    }

    private fun invalidateCompose() {
        icon = icon
    }

    /** Cancels a running press (a page gesture took over); true when one was running. */
    fun cancelPress(): Boolean {
        if (!pressedNow) return false
        pressedNow = false
        val now = android.os.SystemClock.uptimeMillis()
        val cancel = MotionEvent.obtain(now, now, MotionEvent.ACTION_CANCEL, 0f, 0f, 0)
        super.dispatchTouchEvent(cancel)
        cancel.recycle()
        return true
    }

    /** Old: release. */
    fun release() {
        touchListener = null
        setOnClickListener(null)
    }

    /** Kept for the old calls (the icon is always centred; no press animation to switch off). */
    fun setScaleType(type: ImageView.ScaleType?) {}
    fun setNoAnim(enabled: Boolean) {}

    override fun setOnTouchListener(l: View.OnTouchListener?) {
        touchListener = l
    }

    override fun isPressed(): Boolean = pressedNow

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            when (ev.actionMasked) {
                MotionEvent.ACTION_DOWN -> pressedNow = true
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> pressedNow = false
            }
            if (touchListener?.onTouch(this, ev) == true) return true
        }
        return super.dispatchTouchEvent(ev)
    }
}
