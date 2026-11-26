package com.mycompany.app.ui.viewer

import com.mycompany.app.ui.components.rememberFullSheetState
import com.mycompany.app.ui.components.AppListItem
import android.app.PictureInPictureParams
import com.mycompany.app.pref.store.VideoSettings
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.app.PendingIntent
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.graphics.Rect
import android.graphics.drawable.Icon as PlatformIcon
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.util.Rational
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.webkit.URLUtil
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.ResolvingDataSource
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import androidx.media3.session.MediaSession
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.text.CueGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.mandatorySystemGestures
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookVpos
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefVideo
import com.mycompany.app.subtitle.Subtitle
import com.mycompany.app.subtitle.SubtitleItem
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.settings.VideoSettingsActivity
import com.mycompany.app.pref.store.ZoneSettings
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Video player for a file, a content address or a stream (`VIEW` of any video type, or the stream address found by the
 * browser with `EXTRA_ORG` as the page it came from and `EXTRA_POPUP`): Media3 ExoPlayer (2026-11-20, was the platform
 * MediaPlayer: adaptive HLS / DASH, the site's cookies, audio focus, headset buttons) on a texture view with Material 3
 * controls. Tap shows / hides the controls, double-tap left / right skips 10 seconds (middle pauses),
 * drag horizontally to seek, drag the left half vertically for brightness and the right half for volume (each follows
 * its setting), pinch to zoom, picture-in-picture, speed, repeat, fit / fill / stretch, rotation, and an external
 * subtitle file with a sync offset. The playing position is remembered per address. Replaces `VideoActivity`.
 *
 * 2026-11-26: the picture is a SurfaceView (the display hardware shows it directly: less battery, HDR); a tap acts at
 * once (a quick second tap on the side skips, undoing the first tap's controls toggle); dragging the seek bar shows the
 * frame live (Media3 scrubbing mode); picture-in-picture starts with the home gesture (auto-enter) and has ±10 s / play
 * buttons; split screen keeps playing while visible; with "Background playback" on, leaving the player keeps the sound
 * going with a media notification ([VideoPlaybackService]).
 */
class VideoPlayerActivity : OpumComposeActivity() {

    private var uri: Uri? = null
    private var referer: String? = null
    private var title: String? = null

    private var player: ExoPlayer? = null
    /** Headset / Bluetooth buttons and the system's media controls (PiP window). */
    private var session: MediaSession? = null
    /** The picture; the player takes it over (it then follows resolution changes of adaptive streams). */
    private var surfaceView: SurfaceView? = null
    /** Dragging the seek bar or swiping to seek: the player shows frames as the finger moves. */
    private var scrubbing = false
    /** The picture-in-picture buttons (±10 s, play / pause). */
    private val pipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                PIP_BACK -> seekBy(-10_000)
                PIP_FORWARD -> seekBy(10_000)
                PIP_PLAY -> if (playing) pause() else play()
            }
            updatePip()
        }
    }
    private var preparedFlag = false
    private var autoPlay = true
    private var pendingSeek = 0

    private var ready by mutableStateOf(false)
    private var playing by mutableStateOf(false)
    private var buffering by mutableStateOf(true)
    private var failed by mutableStateOf(false)
    private var position by mutableIntStateOf(0)
    private var duration by mutableIntStateOf(0)
    private var videoW by mutableIntStateOf(0)
    private var videoH by mutableIntStateOf(0)
    private var speed by mutableFloatStateOf(1f)
    /** Read in [onCreate]: a fresh process loads the settings only in `super.onCreate`. */
    private var loop by mutableStateOf(false)
    private var inPip by mutableStateOf(false)
    private var subtitles by mutableStateOf<List<SubtitleItem>>(emptyList())
    private var subDelay by mutableLongStateOf(0L)
    /** The stream's tracks (quality / audio / subtitles in the options sheet). */
    private var tracks by mutableStateOf(Tracks.EMPTY)
    private var autoQuality by mutableStateOf(true)
    /** Subtitle text of the stream's own subtitle track (shown like an external subtitle file). */
    private var cueText by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        readIntent(intent)
        if (uri == null) {
            MainUtil.showToast(this, R.string.invalid_path)
            finish()
            return
        }
        super.onCreate(savedInstanceState)
        loop = VideoSettings.loop.value
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.getInsetsController(window, window.decorView).systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        val filter = IntentFilter().apply { addAction(PIP_BACK); addAction(PIP_PLAY); addAction(PIP_FORWARD) }
        try { androidx.core.content.ContextCompat.registerReceiver(this, pipReceiver, filter, androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED) } catch (_: Throwable) {}
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // the media notification only brings the player back
        if (intent.data == null) return
        setIntent(intent)
        savePosition()
        releasePlayer()
        readIntent(intent)
        ready = false; failed = false; buffering = true; position = 0; duration = 0
        subtitles = emptyList(); tracks = Tracks.EMPTY; cueText = null; autoQuality = true
        if (uri != null && surfaceView?.holder?.surface?.isValid == true) startPlayer()
    }

    override fun onStart() {
        super.onStart()
        // back from the background: the notification goes, the player shows the picture again
        VideoPlaybackService.stop(this)
    }

    override fun onResume() {
        super.onResume()
        // auto-enter hid the controls in onUserLeaveHint; when no picture-in-picture came of it they come back here
        inPip = isInPictureInPictureMode
        MainUtil.applyWindowBrightness(window, VideoSettings.brightness.value, VideoSettings.userBrightness.value)
    }

    override fun onPause() {
        super.onPause()
        // split screen: still visible, keeps playing (paused in onStop when it is really hidden)
        if (!inPip && !isInPictureInPictureMode) savePosition()
    }

    override fun onStop() {
        super.onStop()
        if (inPip || isInPictureInPictureMode || isFinishing) return
        savePosition()
        if (playing && ZoneSettings.backPlay.value && session != null) VideoPlaybackService.start(this, session!!)
        else if (playing) pause()
    }

    override fun onDestroy() {
        savePosition()
        VideoPlaybackService.stop(this)
        try { unregisterReceiver(pipReceiver) } catch (_: Throwable) {}
        releasePlayer()
        surfaceView = null
        super.onDestroy()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // auto-enter (set in updatePip) starts picture-in-picture with the home gesture; the controls go first
        if (VideoSettings.pipOnHome.value && playing && videoW > 0) inPip = true
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        inPip = isInPictureInPictureMode
        updatePip()
        if (!isInPictureInPictureMode && !lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) finish()
    }

    private fun readIntent(intent: Intent) {
        uri = intent.data
        referer = if (intent.getBooleanExtra("EXTRA_POPUP", false)) intent.getStringExtra("EXTRA_ORG") else null
        title = uri?.let { Uri.decode(it.toString()).substringAfterLast('/').substringBefore('?') }
    }

    // ---- player ----

    private fun startPlayer() {
        val source = uri ?: return
        val target = surfaceView?.takeIf { it.holder.surface?.isValid == true } ?: return
        releasePlayer()
        preparedFlag = false
        pendingSeek = 0
        val address = source.toString()
        val network = URLUtil.isNetworkUrl(address)
        try {
            // http(s): the browser's user agent, the page as referer, and the site's cookies for each request's own
            // host (signed-in or protected streams); everything else (files, content addresses) the default way
            val http = DefaultHttpDataSource.Factory()
                .setUserAgent(MainUtil.userAgentPref())
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(15_000)
                .setReadTimeoutMs(30_000)
            if (network && !referer.isNullOrEmpty()) http.setDefaultRequestProperties(mapOf("Referer" to referer!!))
            val withCookies = ResolvingDataSource.Factory(http) { spec ->
                val cookie = try { com.mycompany.app.web.WebProfiles.cookieManager()?.getCookie(spec.uri.toString()) } catch (_: Throwable) { null }
                if (cookie.isNullOrEmpty()) spec else spec.withAdditionalHeaders(mapOf("Cookie" to cookie))
            }
            // the emulator's video decoder ("goldfish") keeps the old frame size when an adaptive stream changes quality
            // (the picture is shown cropped and enlarged); use the software decoder there. Phones are not affected.
            val renderers = DefaultRenderersFactory(applicationContext).setMediaCodecSelector { mime, secure, tunnel ->
                val all = MediaCodecUtil.getDecoderInfos(mime, secure, tunnel)
                all.filter { !it.name.contains("goldfish") }.ifEmpty { all }
            }
            val media = ExoPlayer.Builder(applicationContext, renderers)
                .setMediaSourceFactory(
                    DefaultMediaSourceFactory(DefaultDataSource.Factory(applicationContext, withCookies)).setLoadErrorHandlingPolicy(NoRetryOnClientError()),
                )
                .setAudioAttributes(
                    androidx.media3.common.AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(),
                    true, // audio focus handled by the player (pauses for calls, ducks for notifications)
                )
                .setHandleAudioBecomingNoisy(true) // headphones unplugged: pause
                .setSeekBackIncrementMs(10_000)
                .setSeekForwardIncrementMs(10_000)
                .build()
            player = media
            session = try {
                // the media notification (background playback) brings this player back
                val back = PendingIntent.getActivity(
                    this, 0, Intent(this, VideoPlayerActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
                MediaSession.Builder(applicationContext, media).setSessionActivity(back).build()
            } catch (_: Throwable) { null }
            media.setVideoSurfaceView(target)
            // the remembered position, read off the main thread; applied when the player is ready (or at once if it is)
            Thread {
                val saved = savedPosition(address)
                runOnUiThread {
                    if (player !== media || saved <= 0) return@runOnUiThread
                    if (preparedFlag) { if (saved > 3000 && saved < duration - 3000 && media.currentPosition < 3000) media.seekTo(saved.toLong()) }
                    else pendingSeek = saved
                }
            }.start()
            media.repeatMode = if (loop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
            media.addListener(object : Player.Listener {
                override fun onVideoSizeChanged(size: VideoSize) {
                    if (size.width > 0 && size.height > 0) {
                        videoW = (size.width * size.pixelWidthHeightRatio).roundToInt()
                        videoH = size.height
                        updatePip()
                    }
                }

                override fun onTracksChanged(value: Tracks) {
                    tracks = value
                }

                override fun onCues(cueGroup: CueGroup) {
                    cueText = cueGroup.cues.mapNotNull { it.text?.toString() }.joinToString("\n").ifBlank { null }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    playing = isPlaying || (media.playWhenReady && media.playbackState == Player.STATE_BUFFERING)
                    updatePip()
                }

                override fun onPlaybackStateChanged(state: Int) {
                    when (state) {
                        Player.STATE_BUFFERING -> buffering = true
                        Player.STATE_READY -> {
                            buffering = false
                            duration = media.duration.let { if (it == C.TIME_UNSET || it < 0) 0 else it.coerceAtMost(Int.MAX_VALUE.toLong()).toInt() }
                            if (!preparedFlag) {
                                preparedFlag = true
                                ready = true
                                if (pendingSeek > 3000 && pendingSeek < duration - 3000) media.seekTo(pendingSeek.toLong())
                            }
                        }
                        Player.STATE_ENDED -> {
                            buffering = false
                            playing = false
                            position = duration
                            DbBookVpos.savePosition(applicationContext, 0, address)
                        }
                        else -> {}
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    failed = true
                    buffering = false
                    playing = false
                    // the server's answer helps more than "Fail" (expired link, access denied, ...)
                    var cause: Throwable? = error
                    var code = 0
                    while (cause != null && code == 0) {
                        if (cause is HttpDataSource.InvalidResponseCodeException) code = cause.responseCode
                        cause = cause.cause
                    }
                    MainUtil.showToastText(this@VideoPlayerActivity, getString(R.string.fail) + if (code > 0) " (HTTP $code)" else "")
                }
            })
            val item = MediaItem.Builder().setUri(source)
            streamMime(address)?.let { item.setMimeType(it) }
            media.setMediaItem(item.build())
            media.playWhenReady = autoPlay
            media.setPlaybackSpeed(speed)
            media.prepare()
        } catch (_: Throwable) {
            failed = true
            buffering = false
        }
    }

    /**
     * The stream type when the address does not end in it (the player guesses from the extension): the type the
     * opener gave, or ".m3u8" / ".mpd" somewhere in the address (`.../master.m3u8?token=` is found anyway).
     */
    private fun streamMime(address: String): String? {
        val type = intent?.type?.lowercase()
        if (type != null && (type.contains("mpegurl") || type.contains("m3u8"))) return MimeTypes.APPLICATION_M3U8
        if (type != null && type.contains("dash")) return MimeTypes.APPLICATION_MPD
        val path = address.substringBefore('#').lowercase()
        if (path.contains(".m3u8")) return MimeTypes.APPLICATION_M3U8
        if (path.contains(".mpd")) return MimeTypes.APPLICATION_MPD
        return null
    }

    private fun releasePlayer() {
        try { session?.release() } catch (_: Throwable) {}
        session = null
        try { player?.release() } catch (_: Throwable) {}
        player = null
        preparedFlag = false
        playing = false
    }

    private fun play() {
        val media = player ?: return
        if (!preparedFlag) return
        try {
            if (media.playbackState == Player.STATE_ENDED || (duration > 0 && position >= duration - 500)) media.seekTo(0)
            media.play()
            playing = true
        } catch (_: Throwable) {
        }
    }

    private fun pause() {
        val media = player ?: return
        try { media.pause() } catch (_: Throwable) {}
        playing = false
    }

    private fun seekBy(deltaMs: Int) {
        val media = player ?: return
        if (!preparedFlag) return
        val target = (media.currentPosition + deltaMs).coerceIn(0L, duration.toLong()).toInt()
        media.seekTo(target.toLong())
        position = target
    }

    private fun seekTo(ms: Int) {
        val media = player ?: return
        if (!preparedFlag) return
        val target = ms.coerceIn(0, duration)
        media.seekTo(target.toLong())
        position = target
    }

    /** The finger moves on the seek bar or across the picture: show the frame at [ms] right away. */
    private fun scrubTo(ms: Int) {
        val media = player ?: return
        if (!preparedFlag) return
        try {
            if (!scrubbing) { scrubbing = true; media.setScrubbingModeEnabled(true) }
            media.seekTo(ms.coerceIn(0, duration).toLong())
        } catch (_: Throwable) {
        }
    }

    /** The finger left: the exact position, then normal playback. */
    private fun endScrub(ms: Int) {
        seekTo(ms)
        if (scrubbing) {
            scrubbing = false
            try { player?.setScrubbingModeEnabled(false) } catch (_: Throwable) {}
        }
    }

    private fun applySpeed(value: Float) {
        speed = value
        try { player?.setPlaybackSpeed(value) } catch (_: Throwable) {}
    }

    private fun savedPosition(path: String): Int {
        return try {
            DbUtil.query(DbBookVpos.get(applicationContext).writableDatabase, "DbBookVpos_table", arrayOf("_pos"), "_path=?", arrayOf(path), null)?.use {
                if (it.moveToFirst()) it.getInt(0) else 0
            } ?: 0
        } catch (_: Throwable) {
            0
        }
    }

    private fun savePosition() {
        val source = uri?.toString() ?: return
        if (!preparedFlag || duration <= 0) return
        val current = try { player?.currentPosition?.toInt() ?: position } catch (_: Throwable) { position }
        if (current > 3000 && current < duration - 3000) DbBookVpos.savePosition(applicationContext, current, source)
    }

    /**
     * The picture-in-picture window: the video's shape, ±10 s and play / pause buttons, the picture's place on screen
     * (the window grows out of it) and auto-enter with the home gesture while a video plays (setting "PiP on home").
     */
    private fun pipParams(): PictureInPictureParams {
        val builder = PictureInPictureParams.Builder()
        if (videoW > 0 && videoH > 0) {
            val value = videoW.toFloat() / videoH
            builder.setAspectRatio(if (value < 0.42f) Rational(42, 100) else if (value > 2.39f) Rational(239, 100) else Rational(videoW, videoH))
        }
        fun action(icon: Int, title: String, what: String, code: Int) = RemoteAction(
            PlatformIcon.createWithResource(this, icon), title, title,
            PendingIntent.getBroadcast(this, code, Intent(what).setPackage(packageName), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT),
        )
        builder.setActions(listOf(
            action(R.drawable.baseline_fast_rewind_white_24, "-10 s", PIP_BACK, 1),
            if (playing) action(R.drawable.baseline_pause_white_24, getString(R.string.pause), PIP_PLAY, 2)
            else action(R.drawable.baseline_play_arrow_white_24, getString(R.string.play), PIP_PLAY, 2),
            action(R.drawable.baseline_fast_forward_white_24, "+10 s", PIP_FORWARD, 3),
        ))
        surfaceView?.let { view ->
            val rect = Rect()
            if (view.isAttachedToWindow && view.getGlobalVisibleRect(rect) && !rect.isEmpty) builder.setSourceRectHint(rect)
        }
        builder.setAutoEnterEnabled(VideoSettings.pipOnHome.value && playing && videoW > 0)
        builder.setSeamlessResizeEnabled(true)
        return builder.build()
    }

    private fun updatePip() {
        try { setPictureInPictureParams(pipParams()) } catch (_: Throwable) {}
    }

    private fun enterPip() {
        if (videoW <= 0 || videoH <= 0) return
        try {
            // controls off before the window shrinks (onPictureInPictureModeChanged comes after the animation)
            inPip = true
            if (!enterPictureInPictureMode(pipParams())) inPip = false
        } catch (_: Throwable) {
            inPip = false
        }
    }

    private fun loadSubtitles(file: Uri) {
        Thread {
            var cues: List<SubtitleItem> = emptyList()
            try {
                val name = Uri.decode(file.toString()).substringAfterLast('/').substringAfterLast(':')
                val ext = name.substringAfterLast('.', "").lowercase()
                val format = Subtitle.EXTENSIONS.indexOf(ext).let { if (it == -1) 4 else it }
                val lists = contentResolver.openInputStream(file)?.use { Subtitle.parse(format, it, "UTF-8") { false } }
                cues = lists?.firstOrNull()?.sortedBy { it.start } ?: emptyList()
            } catch (_: Throwable) {
            }
            runOnUiThread {
                subtitles = cues
                if (cues.isEmpty()) MainUtil.showToast(this, R.string.fail)
            }
        }.start()
    }

    // ---- screen ----
    //
    // 2026-11-20 rework: lighter controls. Gradient bars instead of solid ones; the middle holds only play / pause (it
    // turns into the loading circle); everything is reached at the bottom (thin seek bar, then lock · fit · rotate ·
    // picture-in-picture · options); the rest lives in an options sheet (speed, quality, audio track, subtitles incl.
    // the stream's own, repeat, share, settings). Gestures are unchanged; their feedback is a small pill with a level bar
    // (brightness / volume / seek) and a side bubble for double-tap skips that adds up (+10, +20, ...). Lock turns all
    // gestures off until the lock button is tapped again.

    @Composable
    override fun Content() {
        // the player is always dark, whatever the app theme (the sheet would glare in light mode)
        MaterialTheme(colorScheme = dynamicDarkColorScheme(LocalContext.current)) { PlayerScreen() }
    }

    @Composable
    private fun PlayerScreen() {
        var controls by remember { mutableStateOf(true) }
        var touched by remember { mutableLongStateOf(System.currentTimeMillis()) }
        var sheet by remember { mutableStateOf(false) }
        var locked by remember { mutableStateOf(false) }
        var fit by remember { mutableIntStateOf(0) } // 0 fit, 1 fill, 2 stretch
        var zoom by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        var hud by remember { mutableStateOf<Hud?>(null) }
        var hudStamp by remember { mutableLongStateOf(0L) }
        var seeking by remember { mutableStateOf(false) }
        var landscapeLock by remember { mutableIntStateOf(0) } // 0 auto, 1 landscape, 2 portrait
        var skip by remember { mutableIntStateOf(0) } // seconds shown in the double-tap bubble (signed)
        var skipVisible by remember { mutableStateOf(false) }
        var skipStamp by remember { mutableLongStateOf(0L) }
        val audio = remember { getSystemService(Context.AUDIO_SERVICE) as AudioManager }
        val subPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { file ->
            if (file != null) { MainUtil.keepUriPermission(applicationContext, file); loadSubtitles(file) }
        }
        val network = uri?.toString()?.let { URLUtil.isNetworkUrl(it) } == true
        val fitNames = listOf(stringResource(R.string.vid_fit), stringResource(R.string.vid_fill), stringResource(R.string.vid_stretch))
        val rotateNames = listOf(stringResource(R.string.vid_rotate_auto), stringResource(R.string.vid_landscape), stringResource(R.string.vid_portrait))
        val lockedText = stringResource(R.string.vid_locked)
        // bottom controls stay out of Android's gesture strip, also while the system bars are hidden (immersive): the
        // navigation bar inset is 0 then, but the strip still takes swipes
        val bottomSafe = WindowInsets.mandatorySystemGestures.union(WindowInsets.navigationBars).only(WindowInsetsSides.Bottom)
        // landscape: keep the row clear of the camera cut-out / side bars too
        val sideSafe = WindowInsets.displayCutout.union(WindowInsets.navigationBars).only(WindowInsetsSides.Horizontal)
        val wide = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

        fun poke() { touched = System.currentTimeMillis() }
        fun showHud(value: Hud) { hud = value; hudStamp = System.currentTimeMillis() }
        fun skipBy(seconds: Int) {
            skip = if (skipVisible && (skip > 0) == (seconds > 0)) skip + seconds else seconds
            skipVisible = true
            skipStamp = System.currentTimeMillis()
            seekBy(seconds * 1000)
        }

        LaunchedEffect(playing, ready) {
            while (true) {
                if (playing && !seeking) {
                    val media = player
                    if (media != null && preparedFlag) position = try { media.currentPosition.toInt() } catch (_: Throwable) { position }
                }
                delay(250)
            }
        }
        LaunchedEffect(controls, playing, touched, sheet, locked) {
            if (controls && (playing || locked) && !sheet) {
                delay(3000)
                if (System.currentTimeMillis() - touched >= 2900) controls = false
            }
        }
        LaunchedEffect(hudStamp) { if (hud != null) { delay(800); hud = null } }
        LaunchedEffect(skipStamp) { if (skipVisible) { delay(700); skipVisible = false } }
        LaunchedEffect(controls, inPip, locked) {
            val insets = WindowCompat.getInsetsController(window, window.decorView)
            if (controls && !inPip && !locked) insets.show(WindowInsetsCompat.Type.systemBars()) else insets.hide(WindowInsetsCompat.Type.systemBars())
        }
        BackHandler { if (sheet) sheet = false else finish() }

        val full = controls && !inPip && !locked

        Box(Modifier.fillMaxSize().background(Color.Black)) {
            BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                val density = LocalDensity.current
                val maxWpx = with(density) { maxWidth.toPx() }
                val maxHpx = with(density) { maxHeight.toPx() }
                val vw = if (videoW > 0) videoW.toFloat() else 16f
                val vh = if (videoH > 0) videoH.toFloat() else 9f
                val fitScale = min(maxWpx / vw, maxHpx / vh)
                val fillScale = max(maxWpx / vw, maxHpx / vh)
                val (viewW, viewH) = when (fit) {
                    2 -> Pair(maxWpx, maxHpx)
                    1 -> Pair(vw * fillScale, vh * fillScale)
                    else -> Pair(vw * fitScale, vh * fitScale)
                }
                AndroidView(
                    factory = { context ->
                        SurfaceView(context).apply {
                            surfaceView = this
                            // first start once the surface exists; the player then follows it on its own
                            holder.addCallback(object : SurfaceHolder.Callback {
                                override fun surfaceCreated(holder: SurfaceHolder) {
                                    if (player == null) startPlayer()
                                }
                                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) { updatePip() }
                                override fun surfaceDestroyed(holder: SurfaceHolder) {}
                            })
                        }
                    },
                    modifier = Modifier.size(with(density) { viewW.toDp() }, with(density) { viewH.toDp() }).graphicsLayer {
                        val limitX = viewW * (zoom - 1f) / 2f
                        val limitY = viewH * (zoom - 1f) / 2f
                        scaleX = zoom; scaleY = zoom
                        translationX = offset.x.coerceIn(-limitX, limitX)
                        translationY = offset.y.coerceIn(-limitY, limitY)
                    },
                )
            }

            // a surface view keeps its last frame: no frozen picture of the failed video behind "Fail"
            if (failed) Box(Modifier.fillMaxSize().background(Color.Black))

            // gestures on top of the picture (all off while locked: a tap only shows the lock button)
            Box(
                Modifier.fillMaxSize()
                    .pointerInput(Unit) {
                        // a tap acts at once (no wait for a possible second tap); a quick second tap on the same third
                        // is a double tap: it undoes the first tap's controls toggle and skips (left / right, adding
                        // up with more taps) or plays / pauses (middle)
                        var lastAt = 0L
                        var lastZone = -1
                        var taps = 0
                        var controlsBefore = false
                        detectTapGestures(
                            onTap = { point ->
                                val now = System.currentTimeMillis()
                                val third = size.width / 3f
                                val zone = if (point.x < third) 0 else if (point.x > 2 * third) 2 else 1
                                if (!locked && taps > 0 && now - lastAt < DOUBLE_TAP_MS && zone == lastZone) {
                                    if (taps == 1) controls = controlsBefore
                                    taps++
                                    when (zone) {
                                        0 -> skipBy(-10)
                                        2 -> skipBy(10)
                                        else -> if (taps == 2) { if (playing) pause() else play() }
                                    }
                                } else {
                                    controlsBefore = controls
                                    controls = !controls
                                    if (locked && controls) showHud(Hud(R.drawable.outline_lock_white_24, lockedText))
                                    taps = 1
                                }
                                lastAt = now
                                lastZone = zone
                                poke()
                            },
                        )
                    }
                    .pointerInput(Unit) {
                        var mode = 0 // 1 seek, 2 brightness, 3 volume
                        var total = Offset.Zero
                        var startPos = 0
                        var startBright = 0f
                        var startVolume = 0
                        var startX = 0f
                        val maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                        detectDragGestures(
                            onDragStart = { start ->
                                mode = 0; total = Offset.Zero
                                startPos = position
                                startBright = window.attributes.screenBrightness.let { if (it < 0f) 0.5f else it }
                                startVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
                                startX = start.x
                            },
                            onDragEnd = { if (mode == 1) { endScrub(position); seeking = false }; mode = 0 },
                            onDragCancel = { if (mode == 1) endScrub(position); seeking = false; mode = 0 },
                        ) { change, drag ->
                            if (locked || zoom > 1.01f) return@detectDragGestures
                            total += drag
                            if (mode == 0 && (abs(total.x) > 24f || abs(total.y) > 24f)) {
                                mode = if (abs(total.x) > abs(total.y)) { if (VideoSettings.dragSeek.value && duration > 0) 1 else 0 }
                                else if (startX < size.width / 2f) { if (VideoSettings.dragBright.value) 2 else 0 }
                                else { if (VideoSettings.dragVolume.value) 3 else 0 }
                                if (mode == 1) seeking = true
                            }
                            when (mode) {
                                1 -> {
                                    val span = 120_000f * (VideoSettings.seekSense.value.coerceIn(10, 300) / 100f)
                                    position = (startPos + total.x / size.width * span).roundToInt().coerceIn(0, duration)
                                    scrubTo(position)
                                    val delta = position - startPos
                                    showHud(Hud(0, format(position) + " / " + format(duration), (if (delta < 0) "−" else "+") + format(abs(delta)), position.toFloat() / max(1, duration)))
                                }
                                2 -> {
                                    val value = (startBright - total.y / size.height * 1.2f).coerceIn(0.01f, 1f)
                                    val attrs = window.attributes
                                    attrs.screenBrightness = value
                                    window.attributes = attrs
                                    showHud(Hud(R.drawable.outline_brightness_6_white_24, "${(value * 100).roundToInt()}%", fraction = value))
                                }
                                3 -> {
                                    val value = (startVolume - total.y / size.height * maxVolume * 1.2f).roundToInt().coerceIn(0, maxVolume)
                                    audio.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0)
                                    val fraction = value.toFloat() / max(1, maxVolume)
                                    showHud(Hud(if (value == 0) R.drawable.outline_volume_off_white_24 else R.drawable.outline_volume_up_white_24, "${(fraction * 100).roundToInt()}%", fraction = fraction))
                                }
                            }
                            if (mode != 0) change.consume()
                        }
                    }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                            do {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                if (!locked && (event.changes.count { it.pressed } >= 2 || zoom > 1.01f)) {
                                    val zoomChange = event.calculateZoom()
                                    val calculatePan = event.calculatePan()
                                    if (zoomChange != 1f || calculatePan != Offset.Zero) {
                                        zoom = (zoom * zoomChange).coerceIn(1f, 5f)
                                        offset += calculatePan
                                        event.changes.forEach { if (it.positionChanged()) it.consume() }
                                    }
                                }
                            } while (event.changes.any { it.pressed })
                            if (zoom <= 1.01f) { zoom = 1f; offset = Offset.Zero }
                        }
                    },
            )

            // double-tap skip bubble at the tapped side
            if (!inPip) AnimatedVisibility(skipVisible && skip < 0, Modifier.align(Alignment.CenterStart), enter = fadeIn(), exit = fadeOut()) {
                SkipBubble(left = true, seconds = -skip)
            }
            if (!inPip) AnimatedVisibility(skipVisible && skip > 0, Modifier.align(Alignment.CenterEnd), enter = fadeIn(), exit = fadeOut()) {
                SkipBubble(left = false, seconds = skip)
            }

            if (buffering && !failed && !full) CircularProgressIndicator(Modifier.align(Alignment.Center).size(44.dp), color = Color.White, strokeWidth = 3.dp)
            if (failed) {
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.fail), color = Color.White, style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { failed = false; buffering = true; startPlayer() }) { Text(stringResource(R.string.retry)) }
                }
            }

            // own scope: the position changes 4 times a second, only the subtitle line reads it
            SubtitleLine(
                if (inPip) Modifier.align(Alignment.BottomCenter).padding(4.dp).background(Color(0xAA000000), RoundedCornerShape(3.dp)).padding(horizontal = 4.dp)
                else Modifier.align(Alignment.BottomCenter).windowInsetsPadding(bottomSafe).padding(bottom = if (full) (if (wide) 80.dp else 150.dp) else if (controls && locked) 80.dp else 32.dp, start = 24.dp, end = 24.dp)
                    .background(Color(0xAA000000), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 2.dp),
            )

            // top: back and title on a gradient
            if (!inPip) AnimatedVisibility(full, Modifier.align(Alignment.TopCenter), enter = fadeIn(), exit = fadeOut()) {
                Row(
                    Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color(0xA6000000), Color.Transparent)))
                        .statusBarsPadding().padding(start = 4.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { finish() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                    Text(title ?: "", Modifier.weight(1f), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleMedium)
                }
            }

            // middle: play / pause only (the loading circle takes its place)
            if (!inPip) AnimatedVisibility(full && !failed, Modifier.align(Alignment.Center), enter = fadeIn(), exit = fadeOut()) {
                Box(
                    Modifier.size(68.dp).clip(CircleShape).background(Color(0x66000000))
                        .clickable { if (playing) pause() else play(); poke() },
                    contentAlignment = Alignment.Center,
                ) {
                    if (buffering) CircularProgressIndicator(Modifier.size(40.dp), color = Color.White, strokeWidth = 3.dp)
                    else Icon(painterResource(if (playing) R.drawable.baseline_pause_white_24 else R.drawable.baseline_play_arrow_white_24), null, tint = Color.White, modifier = Modifier.size(36.dp))
                }
            }

            // gesture feedback pill
            if (!inPip) hud?.let { HudPill(it, Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = if (full) 64.dp else 24.dp)) }

            // bottom: time, thin seek bar, actions. Portrait: three rows; landscape: one row (lock · time · seek bar ·
            // total · speed · actions), the short screen has no room for three rows above the subtitle.
            val seekBar: @Composable (Modifier) -> Unit = { m ->
                SeekBar(
                    value = if (duration > 0) position.toFloat() else 0f,
                    max = max(1, duration).toFloat(),
                    enabled = duration > 0,
                    onChange = { seeking = true; position = it.roundToInt(); scrubTo(position); poke() },
                    onDone = { endScrub(position); seeking = false },
                    modifier = m,
                )
            }
            val lockButton: @Composable () -> Unit = {
                BarIcon(R.drawable.outline_lock_white_24, stringResource(R.string.vid_lock)) { locked = true; controls = false; showHud(Hud(R.drawable.outline_lock_white_24, lockedText)) }
            }
            val speedChip: @Composable () -> Unit = {
                if (speed != 1f) {
                    Text(
                        speedLabel(speed), Modifier.clip(RoundedCornerShape(12.dp)).background(Color(0x33FFFFFF)).clickable { sheet = true }
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                        color = Color.White, style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            val actions: @Composable () -> Unit = {
                BarIcon(R.drawable.outline_zoom_out_map_white_24, fitNames[fit]) {
                    fit = (fit + 1) % 3; zoom = 1f; offset = Offset.Zero; showHud(Hud(R.drawable.outline_zoom_out_map_white_24, fitNames[fit])); poke()
                }
                BarIcon(
                    when (landscapeLock) { 1 -> R.drawable.outline_screen_lock_landscape_white_24; 2 -> R.drawable.outline_screen_lock_portrait_white_24; else -> R.drawable.outline_screen_rotation_white_24 },
                    stringResource(R.string.screen_rotate),
                ) {
                    landscapeLock = (landscapeLock + 1) % 3
                    requestedOrientation = when (landscapeLock) {
                        1 -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                        2 -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                        else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    }
                    showHud(Hud(R.drawable.outline_screen_rotation_white_24, rotateNames[landscapeLock])); poke()
                }
                BarIcon(R.drawable.outline_picture_in_picture_alt_white_24, "PiP") { enterPip() }
                IconButton(onClick = { sheet = true; poke() }) { Icon(Icons.Filled.MoreVert, stringResource(R.string.setting), tint = Color.White) }
            }
            if (!inPip) AnimatedVisibility(full, Modifier.align(Alignment.BottomCenter), enter = fadeIn(), exit = fadeOut()) {
                val barModifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xB3000000))))
                    .windowInsetsPadding(bottomSafe).windowInsetsPadding(sideSafe)
                if (wide) {
                    Row(barModifier.padding(start = 8.dp, end = 8.dp, top = 24.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        lockButton()
                        Text(format(position), Modifier.padding(start = 4.dp), color = Color.White, style = MaterialTheme.typography.labelLarge)
                        seekBar(Modifier.weight(1f))
                        Text(format(duration), Modifier.padding(end = 8.dp), color = Color.White, style = MaterialTheme.typography.labelLarge)
                        speedChip()
                        actions()
                    }
                } else {
                    Column(barModifier.padding(start = 8.dp, end = 8.dp, top = 32.dp, bottom = 4.dp)) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(format(position) + " / " + format(duration), color = Color.White, style = MaterialTheme.typography.labelLarge)
                            Box(Modifier.weight(1f))
                            speedChip()
                        }
                        seekBar(Modifier.fillMaxWidth())
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            lockButton()
                            Box(Modifier.weight(1f))
                            actions()
                        }
                    }
                }
            }

            // locked: only the lock button, where the lock icon sits in the bar
            AnimatedVisibility(controls && locked && !inPip, Modifier.align(Alignment.BottomStart), enter = fadeIn(), exit = fadeOut()) {
                FilledTonalIconButton(
                    onClick = { locked = false; controls = true; hud = null; poke() },
                    Modifier.windowInsetsPadding(bottomSafe).padding(start = 12.dp, bottom = 8.dp).size(52.dp),
                ) { Icon(painterResource(R.drawable.outline_lock_white_24), stringResource(R.string.unlock)) }
            }

            if (sheet) {
                OptionsSheet(
                    network = network,
                    onDismiss = { sheet = false; poke() },
                    onPickSubtitle = { sheet = false; try { subPicker.launch(arrayOf("*/*")) } catch (_: Exception) {} },
                    onHint = { showHud(Hud(R.drawable.outline_subtitles_white_24, it)) },
                )
            }
        }
    }

    /** The subtitle at the playing position (external file with its delay, or the stream's own). */
    @Composable
    private fun SubtitleLine(modifier: Modifier) {
        val text = remember(subtitles, position, subDelay, cueText) {
            if (subtitles.isEmpty()) cueText
            else subtitles.lastOrNull { it.start <= position - subDelay }?.text?.takeIf { it.isNotBlank() }
        } ?: return
        Text(
            text.replace(Regex("<[^>]*>"), ""), modifier,
            color = Color.White, textAlign = TextAlign.Center,
            style = if (inPip) MaterialTheme.typography.labelSmall else MaterialTheme.typography.titleMedium,
            maxLines = if (inPip) 2 else Int.MAX_VALUE,
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun OptionsSheet(network: Boolean, onDismiss: () -> Unit, onPickSubtitle: () -> Unit, onHint: (String) -> Unit) {
        val state = rememberFullSheetState()
        val videos = videoChoices()
        val audios = trackChoices(C.TRACK_TYPE_AUDIO)
        val texts = trackChoices(C.TRACK_TYPE_TEXT)
        val textOn = player?.trackSelectionParameters?.disabledTrackTypes?.contains(C.TRACK_TYPE_TEXT) != true
        ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(bottom = 16.dp)) {
                SheetSection(R.drawable.outline_slow_motion_video_white_24, stringResource(R.string.play_speed)) {
                    for (value in listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)) {
                        FilterChip(selected = speed == value, onClick = { applySpeed(value) }, label = { Text(speedLabel(value)) })
                    }
                }
                if (videos.size > 1) {
                    SheetSection(R.drawable.outline_hd_white_24, stringResource(R.string.vid_quality)) {
                        FilterChip(selected = autoQuality, onClick = { selectTrack(C.TRACK_TYPE_VIDEO, null) }, label = { Text(stringResource(R.string.vid_auto)) })
                        for (choice in videos) {
                            FilterChip(selected = !autoQuality && choice.selected, onClick = { selectTrack(C.TRACK_TYPE_VIDEO, choice) }, label = { Text(choice.label) })
                        }
                    }
                }
                if (audios.size > 1) {
                    SheetSection(R.drawable.outline_volume_up_white_24, stringResource(R.string.audio)) {
                        for (choice in audios) FilterChip(selected = choice.selected, onClick = { selectTrack(C.TRACK_TYPE_AUDIO, choice) }, label = { Text(choice.label) })
                    }
                }
                SheetSection(R.drawable.outline_subtitles_white_24, stringResource(R.string.subtitle)) {
                    val anyOn = subtitles.isNotEmpty() || (textOn && texts.any { it.selected })
                    FilterChip(selected = !anyOn, onClick = { subtitles = emptyList(); selectTrack(C.TRACK_TYPE_TEXT, null) }, label = { Text(stringResource(R.string.vid_off)) })
                    for (choice in texts) {
                        FilterChip(selected = subtitles.isEmpty() && textOn && choice.selected, onClick = { subtitles = emptyList(); selectTrack(C.TRACK_TYPE_TEXT, choice) }, label = { Text(choice.label) })
                    }
                    AssistChip(onClick = onPickSubtitle, label = { Text(stringResource(R.string.vid_sub_file)) })
                }
                if (subtitles.isNotEmpty()) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.sub_sync), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = { subDelay -= 500; onHint("${subDelay / 1000f} s") }) { Text("−0.5 s") }
                        Text("${subDelay / 1000f} s", style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = { subDelay += 500; onHint("${subDelay / 1000f} s") }) { Text("+0.5 s") }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                AppListItem(
                    headlineContent = { Text(stringResource(R.string.vid_repeat)) },
                    leadingContent = { Icon(painterResource(R.drawable.outline_repeat_white_24), null) },
                    trailingContent = { Switch(checked = loop, onCheckedChange = { applyLoop(it) }) },
                    modifier = Modifier.clickable { applyLoop(!loop) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
                if (network) {
                    AppListItem(
                        headlineContent = { Text(stringResource(R.string.share)) },
                        leadingContent = { Icon(painterResource(R.drawable.outline_share_white_24), null) },
                        modifier = Modifier.clickable { onDismiss(); MainUtil.shareText(this@VideoPlayerActivity, uri.toString(), title) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
                AppListItem(
                    headlineContent = { Text(stringResource(R.string.setting)) },
                    leadingContent = { Icon(painterResource(R.drawable.outline_settings_white_24), null) },
                    modifier = Modifier.clickable { onDismiss(); startActivity(Intent(this@VideoPlayerActivity, VideoSettingsActivity::class.java)) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }
    }

    private fun applyLoop(value: Boolean) {
        loop = value
        VideoSettings.edit(applicationContext) { set(VideoSettings.loop, value) }
        try { player?.repeatMode = if (value) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF } catch (_: Throwable) {}
    }

    // ---- tracks (quality, audio, the stream's own subtitles) ----

    private class TrackChoice(val label: String, val group: Tracks.Group, val index: Int, val selected: Boolean)

    private fun trackChoices(type: Int): List<TrackChoice> {
        val list = ArrayList<TrackChoice>()
        for (group in tracks.groups) {
            if (group.type != type) continue
            for (i in 0 until group.length) {
                if (!group.isTrackSupported(i)) continue
                val format = group.getTrackFormat(i)
                val label = format.label?.takeIf { it.isNotBlank() }
                    ?: format.language?.takeIf { it.isNotBlank() && it != "und" }?.let { java.util.Locale.forLanguageTag(it).displayName }
                    ?: "#${list.size + 1}"
                list += TrackChoice(label, group, i, group.isTrackSelected(i))
            }
        }
        return list
    }

    /** One entry per picture height (an adaptive stream often has several bitrates of the same size), best first. */
    private fun videoChoices(): List<TrackChoice> {
        val list = ArrayList<TrackChoice>()
        for (group in tracks.groups) {
            if (group.type != C.TRACK_TYPE_VIDEO) continue
            for (i in 0 until group.length) {
                if (!group.isTrackSupported(i)) continue
                val format = group.getTrackFormat(i)
                // a variant without a picture size (audio only, or not announced) is no quality to pick
                val lines = min(format.width, format.height)
                if (lines <= 0) continue
                list += TrackChoice("${lines}p", group, i, group.isTrackSelected(i))
            }
        }
        return list.sortedByDescending { it.label.removeSuffix("p").toIntOrNull() ?: 0 }
            .groupBy { it.label }.map { (_, same) -> same.firstOrNull { it.selected } ?: same.first() }
    }

    /** [choice] null: video back to automatic, subtitles off. */
    private fun selectTrack(type: Int, choice: TrackChoice?) {
        val media = player ?: return
        val params = media.trackSelectionParameters.buildUpon()
        if (choice == null) {
            params.clearOverridesOfType(type)
            if (type == C.TRACK_TYPE_TEXT) params.setTrackTypeDisabled(type, true)
        } else {
            params.setTrackTypeDisabled(type, false)
            params.setOverrideForType(TrackSelectionOverride(choice.group.mediaTrackGroup, choice.index))
        }
        media.trackSelectionParameters = params.build()
        if (type == C.TRACK_TYPE_VIDEO) autoQuality = choice == null
    }

    private fun speedLabel(value: Float): String =
        (if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()) + "×"

    private companion object {
        const val PIP_BACK = "ch.opumlabs.opumbrowser.PLAYER_PIP_BACK"
        const val PIP_PLAY = "ch.opumlabs.opumbrowser.PLAYER_PIP_PLAY"
        const val PIP_FORWARD = "ch.opumlabs.opumbrowser.PLAYER_PIP_FORWARD"
        /** A second tap within this time on the same third of the picture is a double tap. */
        const val DOUBLE_TAP_MS = 300L
    }

    private fun format(ms: Int): String {
        val total = max(0, ms) / 1000
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        return if (h > 0) String.format(java.util.Locale.US, "%d:%02d:%02d", h, m, s) else String.format(java.util.Locale.US, "%d:%02d", m, s)
    }
}

/**
 * The server said no (403 access denied, 404 / 410 gone, ...): fail right away instead of the default retries with
 * backoff (an expired stream link spun for about half a minute). Timeouts, 408 / 429 and server errors are still retried.
 */
@androidx.annotation.OptIn(markerClass = [androidx.media3.common.util.UnstableApi::class])
private class NoRetryOnClientError : DefaultLoadErrorHandlingPolicy() {
    override fun getRetryDelayMsFor(loadErrorInfo: LoadErrorHandlingPolicy.LoadErrorInfo): Long {
        val error = loadErrorInfo.exception
        if (error is HttpDataSource.InvalidResponseCodeException && error.responseCode in 400..499 && error.responseCode != 408 && error.responseCode != 429) {
            return C.TIME_UNSET
        }
        return super.getRetryDelayMsFor(loadErrorInfo)
    }
}

/** Feedback of a gesture or a bar button: icon (0 = none), text, optional second line and level (0..1). */
private class Hud(val icon: Int, val text: String, val sub: String? = null, val fraction: Float? = null)

@Composable
private fun HudPill(hud: Hud, modifier: Modifier) {
    Row(
        modifier.background(Color(0xCC000000), RoundedCornerShape(24.dp)).padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (hud.icon != 0) {
            Icon(painterResource(hud.icon), null, tint = Color.White, modifier = Modifier.size(22.dp))
            Box(Modifier.width(10.dp))
        }
        Column {
            if (hud.sub != null) Text(hud.sub, color = Color.White, style = MaterialTheme.typography.titleLarge)
            Text(hud.text, color = Color.White, style = if (hud.sub != null) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium)
            hud.fraction?.let {
                LinearProgressIndicator(
                    progress = { it.coerceIn(0f, 1f) }, Modifier.padding(top = 6.dp).width(120.dp).height(4.dp),
                    color = Color.White, trackColor = Color(0x4DFFFFFF), drawStopIndicator = {},
                )
            }
        }
    }
}

@Composable
private fun SkipBubble(left: Boolean, seconds: Int) {
    val shape = if (left) RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50) else RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50)
    Box(Modifier.fillMaxHeight(0.6f).width(140.dp).background(Color(0x26FFFFFF), shape), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(painterResource(if (left) R.drawable.baseline_fast_rewind_white_24 else R.drawable.baseline_fast_forward_white_24), null, tint = Color.White, modifier = Modifier.size(32.dp))
            Text("$seconds s", color = Color.White, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun BarIcon(icon: Int, label: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) { Icon(painterResource(icon), label, tint = Color.White) }
}

/** Thin seek bar: 4 dp track, small round thumb, still a full-height touch area. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeekBar(value: Float, max: Float, enabled: Boolean, onChange: (Float) -> Unit, onDone: () -> Unit, modifier: Modifier = Modifier.fillMaxWidth()) {
    val colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = MaterialTheme.colorScheme.primary, inactiveTrackColor = Color(0x4DFFFFFF))
    // custom thumb and track need the state-based slider (material3 2026: the value-based one has no thumb / track)
    val state = androidx.compose.runtime.remember(max) { SliderState(value.coerceIn(0f, max), 0, 0f..max) }
    // follows the playback position, except while the thumb is being dragged
    androidx.compose.runtime.SideEffect { if (!state.isDragging) state.value = value.coerceIn(0f, max) }
    Slider(
        state = state,
        // with its own onValueChange the state does not move itself: the dot follows the finger only when set here
        onValueChange = { state.value = it; onChange(it) },
        onValueChangeFinished = onDone,
        enabled = enabled,
        colors = colors,
        modifier = modifier.height(32.dp).padding(horizontal = 8.dp),
        thumb = { Box(Modifier.size(14.dp).background(Color.White, CircleShape)) },
        track = { state ->
            SliderDefaults.Track(sliderState = state, modifier = Modifier.height(4.dp), colors = colors, enabled = enabled, drawStopIndicator = null, thumbTrackGapSize = 0.dp)
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SheetSection(icon: Int, title: String, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(icon), null, Modifier.size(20.dp))
            Box(Modifier.width(12.dp))
            Text(title, style = MaterialTheme.typography.titleSmall)
        }
        // wrapped, not scrolled sideways: every choice (and "Load subtitle file" at the end) stays in sight
        FlowRow(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}
