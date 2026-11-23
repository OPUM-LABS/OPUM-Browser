package com.mycompany.app.video

import com.mycompany.app.ui.components.AppSlider
import android.content.Context
import android.view.MotionEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.browser.ComposeOverlay
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Play / seek controls of the video preview (`DialogPreview`): rewind 10 s, play / pause, forward 10 s, and a bar with
 * the position, a slider and the duration. Compose since 2026-11-16; same API as the old View version
 * ([start] attach, [refreshLater] refresh, [release] release, [SeekCtrlListener]) and the same behaviour:
 * - the slider seeks live while dragged (`b(position, true)`), the buttons seek with `b(position, false)`;
 * - the display refreshes on each whole second of the position; durations under 1 s show "00:01";
 * - in a right-to-left layout rewind and forward swap places;
 * - while the video is zoomed ([SeekCtrlListener.isZoomed]) touches go through to the video.
 */
class VideoSeekControl(context: Context) : ComposeOverlay(context) {
    /** The player, as the dialog sees it: [isZoomed] zoomed, [seekTo] seek to ms, [pause] pause, [position] position, [duration] duration, [isPlaying] playing, [play] play. */
    interface SeekCtrlListener {
        fun isZoomed(): Boolean
        fun seekTo(position: Int, isFinal: Boolean)
        fun pause()
        fun position(): Int
        fun duration(): Int
        fun isPlaying(): Boolean
        fun play()
    }

    private var listener: SeekCtrlListener? = null
    private var rtl = false
    private var released = false

    private var duration by mutableIntStateOf(0)
    private var position by mutableIntStateOf(0)
    private var playing by mutableStateOf(false)
    private var dragging by mutableStateOf(false)
    private var dragValue by mutableFloatStateOf(0f)
    private var tick by mutableIntStateOf(0)

    /** Attaches the player and shows the controls. */
    fun start(context: Context?, seekCtrlListener: SeekCtrlListener?) {
        listener = seekCtrlListener
        rtl = MainUtil.isRtl(context)
        setPage({ true }) { Controls() }
        refresh()
    }

    /** Refreshes the display soon (after the player changed state). */
    fun refreshLater() {
        post { refresh() }
    }

    /** Stops the updates and lets go of the player. */
    fun release() {
        released = true
        listener = null
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val listenerNow = listener
        if (listenerNow != null && listenerNow.isZoomed()) return false
        return super.dispatchTouchEvent(ev)
    }

    private fun refresh() {
        val l = listener ?: return
        if (dragging) return
        duration = l.duration()
        position = l.position().coerceIn(0, maxOf(duration, 0))
        playing = l.isPlaying()
        tick++
    }

    private fun skip(ms: Int) {
        val l = listener ?: return
        val total = l.duration()
        if (total == 0) return
        l.seekTo((l.position() + ms).coerceIn(0, total), false)
        refresh()
    }

    private fun label(ms: Int): String = when {
        duration == 0 -> "00:00"
        duration < 1000 -> if (ms == duration) "00:01" else "00:00"
        else -> MainUtil.durationText(duration.toLong(), ms.toLong())
    }

    @Composable
    private fun Controls() {
        // one refresh per whole second of the position, like the old handler; skipped while hidden or dragging
        LaunchedEffect(tick) {
            delay((1000 - position % 1000).toLong().coerceAtLeast(50))
            if (!released && isShown && isEnabled) refresh() else if (!released) tick++
        }
        val white = Color.White
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { skip(if (rtl) 10000 else -10000) }, modifier = Modifier.size(48.dp)) {
                    Icon(painterResource(R.drawable.baseline_fast_rewind_white_24), contentDescription = null, tint = white)
                }
                IconButton(onClick = {
                    val l = listener ?: return@IconButton
                    if (l.isPlaying()) l.pause() else l.play()
                    refresh()
                }, modifier = Modifier.padding(horizontal = 32.dp).size(48.dp)) {
                    Icon(
                        painterResource(if (playing) R.drawable.baseline_pause_white_24 else R.drawable.baseline_play_arrow_white_24),
                        contentDescription = null, tint = white,
                    )
                }
                IconButton(onClick = { skip(if (rtl) -10000 else 10000) }, modifier = Modifier.size(48.dp)) {
                    Icon(painterResource(R.drawable.baseline_fast_forward_white_24), contentDescription = null, tint = white)
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp).background(Color(0xA1000000)).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val max = when {
                    duration == 0 -> 0f
                    duration < 1000 -> duration.toFloat()
                    else -> 1000f
                }
                val shown = if (dragging) dragValue else when {
                    duration == 0 -> 0f
                    duration < 1000 -> position.toFloat()
                    else -> (position * 1000f / duration).coerceAtMost(1000f)
                }
                Text(label(if (dragging) ((dragValue / 1000f) * duration).roundToInt() else position),
                    color = white, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.widthIn(min = 48.dp))
                AppSlider(
                    value = shown.coerceIn(0f, maxOf(max, 0f)),
                    onValueChange = { dragValue2 ->
                        val l = listener ?: return@AppSlider
                        dragging = true
                        dragValue = dragValue2
                        val total = l.duration()
                        if (total > 0) l.seekTo(if (total < 1000) dragValue2.roundToInt() else ((dragValue2 / 1000f) * total).roundToInt(), true)
                    },
                    onValueChangeFinished = {
                        dragging = false
                        refresh()
                    },
                    valueRange = 0f..maxOf(max, 1f),
                    enabled = duration > 0,
                    colors = SliderDefaults.colors(thumbColor = white, activeTrackColor = white, inactiveTrackColor = white.copy(alpha = 0.3f)),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                )
                Text(label(duration), color = white, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.widthIn(min = 48.dp))
            }
        }
    }
}
