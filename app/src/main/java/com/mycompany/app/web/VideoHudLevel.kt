package com.mycompany.app.web

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.compose.runtime.setValue
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.VideoSettings
import kotlin.math.roundToInt

/**
 * The value behind one bar of the full-screen video overlay ([WebVideoProgress]): screen brightness (kind 1, 0..100,
 * stored as the player's own brightness in `PrefVideo`), media volume (kind 2, the stream's steps, follows outside
 * changes) or the seek position (kind 3, 0..1000). Port of the old `MyProgressVideo` view without the view: the
 * finger's travel in pixels maps to steps over the bar's length ([setBarSize]); the bar is drawn by Compose from
 * [fill] / [length].
 */
class VideoHudLevel {
    /** Called after each change, and once with the system brightness ([onSystemLevel]). */
    interface Listener {
        fun onLevel(level: Int, isFinal: Boolean)
        fun onSystemLevel(level: Int)
    }

    private val main = Handler(Looper.getMainLooper())
    private var alive = false
    private var context: Context? = null
    private var hudType = 0
    private var listener: Listener? = null
    private var audio: AudioManager? = null
    private var receiver: BroadcastReceiver? = null

    /** Highest value (100, the stream maximum, 1000). */
    private var max = 0
    /** Number of steps (max + 1). */
    private var steps = 0
    private var value = 0
    /** Outline width and twice it, px. */
    private var stroke = 0
    private var stroke2 = 0
    var barW = 0
        private set
    var barH = 0
        private set
    /** Track length in px (the bar minus its outline). */
    var length = 0f
        private set
    /** Px per step. */
    private var step = 0f
    /** Finger travel that reaches the top. */
    var maxSize = 0f
        private set
    /** Finger travel for the current value. */
    var size = 0f
        private set
    /** Filled px (≤ [length]); Compose state. */
    var fill by mutableFloatStateOf(0f)
        private set
    /** The bar was set up ([start]); Compose state. */
    var ready by mutableStateOf(false)
        private set
    /** Seek position in finger px after [setBarSize]. */
    val seekPx get() = size

    private var touching = false
    private var savePending = false
    private var moveBusy = false

    /** Starts the bar for [kind] (1 brightness, 2 volume, 3 seek); reads the current value in the background. */
    fun start(context: Context?, type: Int, listener: Listener?) {
        alive = true
        this.context = context
        this.hudType = type
        this.listener = listener
        MainApp.runInBackground(context, Runnable {
            val ctx = this.context ?: return@Runnable
            when (type) {
                1 -> {
                    max = 100
                    var level = MainUtil.screenBrightnessPercent(ctx)
                    listener?.onSystemLevel(level)
                    if (VideoSettings.userBrightness.value) level = VideoSettings.brightness.value
                    value = level
                    main.post { setUp() }
                }
                2 -> {
                    val am = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
                    audio = am
                    if (am != null) {
                        max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                        value = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                    }
                    main.post {
                        val contextNow = this.context
                        if (contextNow != null && receiver == null) {
                            val broadcastReceiver = object : BroadcastReceiver() {
                                override fun onReceive(context: Context?, intent: Intent?) {
                                    val am = audio ?: return
                                    MainApp.runInBackground(this@VideoHudLevel.context, Runnable {
                                        val streamVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                                        main.post { set(streamVolume, false) }
                                    })
                                }
                            }
                            receiver = broadcastReceiver
                            val filter = IntentFilter()
                            filter.addAction("android.media.VOLUME_CHANGED_ACTION")
                            filter.addAction("android.intent.action.HEADSET_PLUG")
                            ContextCompat.registerReceiver(contextNow, broadcastReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
                        }
                        setUp()
                    }
                }
                else -> {
                    max = 1000
                    value = 0
                    main.post { setUp() }
                }
            }
        })
    }

    private fun setUp() {
        val ctx = context ?: return
        steps = max + 1
        stroke = MainUtil.dpToPx(ctx, 2.0f).roundToInt()
        stroke2 = stroke shl 1
        ready = true
        // the bar may have been measured before the step count was known
        if (barW > 0 && barH > 0) setBarSize(barW, barH)
        set(value, false)
    }

    /** The bar's size in px: the track runs along its width for seek, its height otherwise. */
    fun setBarSize(newBarW: Int, newBarH: Int) {
        barW = newBarW
        barH = newBarH
        length = (if (hudType == 3) newBarW - stroke2 else newBarH - stroke2).toFloat()
        if (steps < 2) steps = 2
        step = length / (steps - 1)
        maxSize = length + step
        moveTo(value)
    }

    /** Sets the value without applying it; returns it clamped. */
    fun moveTo(transCode: Int): Int {
        var v = transCode
        value = v
        if (v < 0) {
            v = 0
            value = 0
        } else if (v > steps) {
            value = steps
            v = steps
        }
        val px = when (v) {
            0 -> 0f
            steps -> maxSize
            else -> v * step
        }
        size = px
        fill = px.coerceAtMost(length)
        return v
    }

    /** Sets the value, applies it to the screen / stream when [apply], and tells the listener. */
    fun set(transCode: Int, apply: Boolean) {
        var v = moveTo(transCode)
        if (v > max) v = max
        if (apply) {
            if (hudType == 1) {
                VideoSettings.editLocal {
                    set(VideoSettings.userBrightness, true)
                    set(VideoSettings.brightness, v.coerceIn(0, 100))
                }
                save()
            } else if (hudType == 2) {
                audio?.setStreamVolume(AudioManager.STREAM_MUSIC, v, 0)
            }
        }
        val l = listener ?: return
        main.post { l.onLevel(v, apply) }
    }

    private fun valueFor(px: Float) = if (step == 0f) 0 else moveTo((px / step).toInt())

    /** A finger event moved the bar to [px] of travel (the first event marks the finger as down). */
    fun touch(ev: MotionEvent, px: Float) {
        // before the bar has a size every travel maps to 0 (the old view could set brightness 0 here)
        if (!ready || step == 0f) return
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            if (moveBusy) return
            moveBusy = true
            touching = true
            main.post {
                if (!alive) return@post
                val old = value
                if (old == valueFor(px)) {
                    moveBusy = false
                } else if (hudType == 2) {
                    MainApp.runInBackground(context, Runnable {
                        set(value, true)
                        if (alive) main.post { moveBusy = false }
                    })
                } else {
                    set(value, true)
                    moveBusy = false
                }
            }
        } else {
            main.post {
                if (!alive) return@post
                val old = value
                if (old == valueFor(px)) {
                    released()
                } else if (hudType == 2) {
                    MainApp.runInBackground(context, Runnable {
                        set(value, true)
                        main.post { released() }
                    })
                } else {
                    set(value, true)
                    released()
                }
            }
        }
    }

    private fun released() {
        touching = false
        if (savePending) {
            savePending = false
            save()
        }
    }

    /** Stores the player brightness; waits while a finger is down. */
    private fun save() {
        if (touching) {
            savePending = true
            return
        }
        VideoSettings.save(context, listOf(VideoSettings.userBrightness, VideoSettings.brightness))
    }

    /** The current value, clamped to 0..max. */
    val progress: Int get() = value.coerceIn(0, max)

    /** Stops listening (the overlay is going away). */
    fun release() {
        alive = false
        ready = false
        val contextNow = context
        val receiverNow = receiver
        if (contextNow != null && receiverNow != null) {
            try {
                contextNow.unregisterReceiver(receiverNow)
            } catch (_: Exception) {
            }
        }
        receiver = null
        context = null
        listener = null
        audio = null
    }

    /** The outline width (px), for drawing. */
    val outline get() = stroke
}
