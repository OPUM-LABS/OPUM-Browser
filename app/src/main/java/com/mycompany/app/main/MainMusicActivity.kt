package com.mycompany.app.main

import androidx.core.content.ContextCompat
import com.mycompany.app.ui.components.AppSlider
import android.content.BroadcastReceiver
import com.mycompany.app.ui.state.AppState
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.OpumComposeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Small audio player opened for audio files (a window at the bottom of the screen): file name, play/pause,
 * a seek slider with elapsed and total time, close. It starts playing as soon as the file is ready, pauses when
 * another player asks (the `ACTION_MUSIC_PAUSE` broadcast, also sent by this player when it starts), reacts to
 * media keys, and rewinds when the track ends. Plays through `MediaPlayer` like before.
 */
class MainMusicActivity : OpumComposeActivity() {

    private var uri: Uri? = null
    private var player: MediaPlayer? = null
    private var receiver: BroadcastReceiver? = null
    private var ignoreOwnBroadcast = false

    // state read by the UI
    private var prepared by mutableStateOf(false)
    private var failed by mutableStateOf(false)
    private var paused by mutableStateOf(false)        // true: shows the play symbol
    private var seeking by mutableStateOf(false)
    private var position by mutableIntStateOf(0)
    private var duration by mutableIntStateOf(0)
    private var title by mutableStateOf<String?>(null)
    private var pendingSeek = -1
    private var tick by mutableIntStateOf(0)           // forces a refresh after play state changes

    override fun onCreate(savedInstanceState: Bundle?) {
        val data = intent.data
        uri = data
        if (data == null) {
            super.onCreate(savedInstanceState)
            MainUtil.showToast(this, R.string.invalid_path)
            finish()
            return
        }
        super.onCreate(savedInstanceState)
        MainUtil.refreshZoneIcons(applicationContext)
        window?.let { w ->
            val attributes = w.attributes
            attributes.gravity = android.view.Gravity.BOTTOM
            w.attributes = attributes
        }
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (ignoreOwnBroadcast) ignoreOwnBroadcast = false else pausePlayback()
            }
        }
        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter("ch.opumlabs.opumbrowser.ACTION_MUSIC_PAUSE"),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        startPlayer()
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) release()
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
    }

    private fun release() {
        receiver?.let { try { unregisterReceiver(it) } catch (_: Exception) {} }
        receiver = null
        stopPlayer()
    }

    private fun stopPlayer() {
        val old = player
        player = null
        prepared = false
        failed = false
        paused = false
        pendingSeek = -1
        if (old != null) {
            try { old.stop() } catch (_: Exception) {}
            old.release()
        }
    }

    private fun startPlayer() {
        stopPlayer()
        val source = uri ?: return
        val mp = MediaPlayer()
        player = mp
        mp.setOnPreparedListener {
            prepared = true
            duration = it.duration.coerceAtLeast(0)
            playPlayback()
        }
        mp.setOnSeekCompleteListener {
            pendingSeek = -1
            playPlayback()
        }
        mp.setOnCompletionListener {
            if (player == null || !prepared) return@setOnCompletionListener
            paused = true
            pausePlayback()
            pendingSeek = 0
            try { it.seekTo(0) } catch (_: Exception) {}
            position = 0
        }
        mp.setOnErrorListener { _, _, _ ->
            failed = true
            MainUtil.showToast(this, R.string.play_error)
            true
        }
        try {
            mp.setDataSource(applicationContext, source)
            mp.prepareAsync()
        } catch (_: Exception) {
            failed = true
            MainUtil.showToast(this, R.string.play_error)
        }
    }

    private fun playPlayback() {
        val mp = player ?: return
        if (!prepared || paused) return
        pendingSeek = -1
        ignoreOwnBroadcast = true
        sendBroadcast(Intent("ch.opumlabs.opumbrowser.ACTION_MUSIC_PAUSE").setPackage(packageName))
        try { if (!mp.isPlaying) mp.start() } catch (_: Exception) {}
        tick++
    }

    private fun pausePlayback() {
        val mp = player ?: return
        if (!prepared) return
        paused = true
        try { if (mp.isPlaying) mp.pause() } catch (_: Exception) {}
        tick++
    }

    private fun togglePlayback() {
        if (failed) { startPlayer(); return }
        val mp = player ?: return
        if (prepared && !paused && try { mp.isPlaying } catch (_: Exception) { false }) {
            pausePlayback()
        } else if (prepared) {
            paused = false
            playPlayback()
        }
    }

    private fun readPosition() {
        val mp = player ?: return
        if (!prepared || seeking) return
        position = if (pendingSeek != -1) pendingSeek else try { mp.currentPosition.coerceAtLeast(0) } catch (_: Exception) { 0 }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (seeking) return true
        return when (keyCode) {
            KeyEvent.KEYCODE_HEADSETHOOK, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> { togglePlayback(); true }
            KeyEvent.KEYCODE_MEDIA_STOP, KeyEvent.KEYCODE_MEDIA_PAUSE -> { pausePlayback(); true }
            KeyEvent.KEYCODE_MEDIA_PLAY -> { paused = false; playPlayback(); true }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    @Composable
    override fun Content() {
        if (uri == null) return
        // file name, loaded once (a content URI may need a query)
        LaunchedEffect(Unit) {
            if (title == null) title = withContext(Dispatchers.IO) { MainUtil.parentPathOf(applicationContext, uri.toString()) }
        }
        // progress once per second, like the old handler
        LaunchedEffect(prepared, tick) {
            while (prepared) {
                readPosition()
                delay(1000L - (position % 1000))
            }
        }
        val dark = AppState.darkUi
        val playing = prepared && !paused && !failed
        var drag by remember { mutableFloatStateOf(0f) }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.extraLarge,
        ) {
            Column(Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { togglePlayback() }, enabled = !seeking) {
                        Icon(
                            painterResource(
                                when {
                                    failed -> if (dark) R.drawable.outline_error_dark_24 else R.drawable.outline_error_black_24
                                    playing -> if (dark) R.drawable.baseline_pause_dark_24 else R.drawable.baseline_pause_black_24
                                    else -> if (dark) R.drawable.baseline_play_arrow_dark_24 else R.drawable.baseline_play_arrow_black_24
                                }
                            ),
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color.Unspecified,
                        )
                    }
                    Text(
                        title ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { release(); finish() }, enabled = !seeking) {
                        Icon(
                            painterResource(if (dark) R.drawable.outline_close_dark_24 else R.drawable.outline_close_black_24),
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color.Unspecified,
                        )
                    }
                }
                val total = duration.coerceAtLeast(0)
                val shown = if (seeking) (drag * total).toInt() else position.coerceIn(0, total.coerceAtLeast(0))
                AppSlider(
                    value = if (seeking) drag else if (total > 0) shown / total.toFloat() else 0f,
                    onValueChange = { seeking = true; drag = it },
                    onValueChangeFinished = {
                        val target = Math.round(drag * total)
                        seeking = false
                        val mp = player
                        if (mp != null && prepared) {
                            pendingSeek = target
                            position = target
                            try { mp.seekTo(target) } catch (_: Exception) {}
                        }
                    },
                    enabled = prepared && total > 0,
                )
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(MainUtil.durationText(total.toLong(), shown.toLong()), style = MaterialTheme.typography.labelMedium)
                    Text(MainUtil.durationText(total.toLong(), total.toLong()), style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
