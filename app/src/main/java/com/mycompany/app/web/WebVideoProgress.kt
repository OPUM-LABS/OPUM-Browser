package com.mycompany.app.web

import android.content.Context
import com.mycompany.app.pref.store.VideoSettings
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.view.Gravity
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefVideo
import com.mycompany.app.ui.theme.OpumTheme
import com.mycompany.app.view.MyFadeLinear
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * One overlay of the full-screen web video ([WebVideoFull]): brightness (kind 1) and volume (kind 2) show an icon,
 * the value and a vertical bar; seek (kind 3) shows the time over a horizontal bar. It keeps the fading frame
 * (`MyFadeLinear`: auto-hide, fade, [MyFadeLinear.hide]), draws its dark rounded background, and since 2026-11-16 its content is
 * one Compose page; the value logic lives in [VideoHudLevel] (was the `MyProgressVideo` view).
 */
class WebVideoProgress(context: Context?) : MyFadeLinear(context) {
    interface WebVidProgListener {
        fun onLevelSent(level: Int)
    }

    /** Finger travel at the start of a drag (−1 = take it from the bar on the next move). */
    @JvmField var startLevel: Float = 0f
    /** Seek: the video duration (ms). */
    @JvmField var durationMs: Long = 0
    /** Seek: finger travel for the position. */
    @JvmField var seekStart: Float = 0f
    /** Width of the background for seek (0 = full width). */
    @JvmField var barWidth: Int = 0
    /** System brightness read at start (−123456 = not yet). */
    @JvmField var lastBrightness: Int = 0
    private var sent = 0
    private var alive = false
    private var progressType = 0
    private var listener: WebVidProgListener? = null
    private var bgRect: RectF? = null

    private val level = VideoHudLevel()
    private var label by mutableStateOf("")
    private var on by mutableStateOf(true)
    /** Seek bar width in px (≤ 0 = as wide as the overlay). */
    private var seekWidth by mutableIntStateOf(-1)
    private var built = false

    /** Builds the content for [kind] (1 brightness, 2 volume, 3 seek); [seekBar] is the seek bar width or −1. */
    fun build(type: Int, seekBar: Int = -1) {
        if (built) return
        built = true
        this.progressType = type
        val ctx = context
        // 2026-11-20: drawn as the small pill of OPUM's own player; the finger still travels as far as on the old bars
        // (160 dp for brightness / volume, the frame's short side minus 2 × 64 dp for seek), so drags feel the same
        if (type == 3) {
            orientation = VERTICAL
            seekWidth = seekBar
            val dm = ctx.resources.displayMetrics
            val inner = if (seekBar > 0) seekBar else min(dm.widthPixels, dm.heightPixels) - (MainApp.dp64 shl 1)
            level.setBarSize(inner.coerceAtLeast(MainApp.dp64), MainUtil.dpToPx(ctx, 10f).roundToInt())
        } else {
            gravity = Gravity.CENTER_VERTICAL
            isBaselineAligned = false
            orientation = HORIZONTAL
            level.setBarSize(MainUtil.dpToPx(ctx, 10f).roundToInt(), MainUtil.dpToPx(ctx, 160f).roundToInt())
        }
        setTouchable(true)
        setAutoHide(true)
        visibility = GONE
        val compose = ComposeView(ctx)
        compose.setContent { OpumTheme(darkTheme = true) { Content() } }
        addView(compose, LinearLayout.LayoutParams(if (type == 3) -1 else -2, -2))
    }

    /** Starts the overlay for [type] with [webVidProgListener] receiving applied seek / level values. */
    fun start(type: Int, webVidProgListener: WebVidProgListener?) {
        alive = true
        progressType = type
        listener = webVidProgListener
        lastBrightness = -123456
        level.start(context, type, object : VideoHudLevel.Listener {
            override fun onLevel(level: Int, isFinal: Boolean) {
                on = level != 0
                label = if (progressType == 3) MainUtil.durationText(durationMs, ((level / 1000.0f) * durationMs).toLong()) else level.toString()
                if (isFinal) {
                    sent = level
                    val l = listener ?: return
                    post { l.onLevelSent(sent) }
                }
            }

            override fun onSystemLevel(level: Int) {
                lastBrightness = level
            }
        })
        bgRect = RectF()
    }

    var progress: Int
        get() = level.progress
        set(value) {
            label = level.moveTo(value).toString()
        }

    /** Seek: fits the bar to a frame of [w] × [h]. */
    fun applySize(newWidth: Int, newHeight: Int) {
        val inner = if (newWidth == 0 || newHeight == 0) 0 else min(newWidth, newHeight) - (MainApp.dp64 shl 1)
        if (inner <= 0) return
        barWidth = MainApp.dp40 + inner
        seekWidth = inner
        level.setBarSize(inner, MainUtil.dpToPx(context, 10f).roundToInt())
        layoutBar(width, height)
    }

    /** Places the dark background (centred and [barWidth] wide for seek). */
    fun layoutBar(width: Int, height: Int) {
        val r = bgRect ?: return
        val bw = barWidth
        if (bw <= 0) r.set(0f, 0f, width.toFloat(), height.toFloat())
        else {
            val x = (width - bw) / 2
            r.set(x.toFloat(), 0f, (x + bw).toFloat(), height.toFloat())
        }
    }

    /** A drag of [delta] px; returns the travel at the start of the drag, or −1 while it is being taken. */
    fun onDrag(motionEvent: MotionEvent?, delta: Float): Float {
        if (!alive) return -1.0f
        visibility = VISIBLE
        if (startLevel.compareTo(-1.0f) == 0) {
            startLevel = level.size
            return -1.0f
        }
        val travel = if (progressType == 3) seekStart + delta else startLevel - delta
        if (motionEvent != null) level.touch(motionEvent, travel)
        if (travel < 0.0f) {
            startLevel = 0.0f
            if (progressType == 3) {
                seekStart = 0.0f
                return -1.0f
            }
        } else {
            if (travel <= level.maxSize) return startLevel
            startLevel = level.maxSize
            if (progressType == 3) seekStart = level.maxSize
        }
        return -1.0f
    }

    /** Shows the overlay with the current value (brightness: the player's or the system's). */
    fun applyBrightness() {
        if (progressType == 1) {
            var brightness: Int
            if (VideoSettings.userBrightness.value) {
                brightness = VideoSettings.brightness.value
            } else {
                brightness = lastBrightness
                if (brightness == -123456) {
                    brightness = MainUtil.screenBrightnessPercent(context)
                    lastBrightness = brightness
                }
            }
            progress = brightness
        }
        showSeek(0L, 0L)
    }

    /** Shows the overlay; for seek at [position] of [duration] ms. */
    fun showSeek(duration: Long, position: Long) {
        if (!alive) return
        visibility = VISIBLE
        startLevel = -1.0f
        if (progressType == 3) {
            var pos = position
            if (pos > duration) pos = duration
            durationMs = duration
            // the old code divided two Longs here, so the bar only ever showed 0 or the end
            val item = if (duration <= 0) 0 else ((pos.toFloat() / duration) * 1000.0f).roundToInt()
            level.moveTo(item)
            seekStart = level.seekPx
        }
    }

    override fun release() {
        alive = false
        super.release()
        level.release()
        listener = null
        bgRect = null
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (!alive) return
        super.dispatchDraw(canvas)
    }

    override fun invalidate() {
        if (alive) super.invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        layoutBar(w, h)
    }

    /**
     * The player's pill: icon, value and a thin level line (brightness / volume), or target and total time over a
     * thin progress line (seek), on a rounded dark background.
     */
    @Composable
    private fun Content() {
        val length = level.length
        val fraction = if (length > 0f) (level.fill / length).coerceIn(0f, 1f) else 0f
        Row(
            Modifier.background(Color(0xCC000000), RoundedCornerShape(24.dp)).padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (progressType != 3) {
                val icon = when {
                    progressType == 1 -> R.drawable.outline_brightness_6_white_24
                    on -> R.drawable.outline_volume_up_white_24
                    else -> R.drawable.outline_volume_off_white_24
                }
                Icon(painterResource(icon), contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
            }
            Column {
                val text = if (progressType == 3) {
                    if (durationMs > 0) label + " / " + MainUtil.durationText(durationMs, durationMs) else label
                } else {
                    "${(fraction * 100).roundToInt()}%"
                }
                Text(text, color = Color.White, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                LinearProgressIndicator(
                    progress = { fraction }, Modifier.padding(top = 6.dp).width(if (progressType == 3) 160.dp else 120.dp).height(4.dp),
                    color = Color.White, trackColor = Color(0x4DFFFFFF), drawStopIndicator = {},
                )
            }
        }
    }
}
