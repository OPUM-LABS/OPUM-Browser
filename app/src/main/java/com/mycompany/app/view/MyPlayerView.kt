package com.mycompany.app.view

import com.mycompany.app.main.MainUtil
import android.content.Context
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.media.MediaPlayer.OnSeekCompleteListener
import android.media.MediaPlayer.OnVideoSizeChangedListener
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.view.ViewParent
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil.requestHeadersFor
import java.lang.ref.WeakReference

@Suppress("deprecation")
class MyPlayerView(var ctx: Context?) : TextureView(ctx!!) {
    @JvmField
    var mainHandler: Handler?
    @JvmField
    var playerListener: PlayerViewListener? = null
    @JvmField
    var mediaUri: Uri? = null
    @JvmField
    var referer: String? = null
    @JvmField
    var userAgentText: String? = null
    @JvmField
    var texture: SurfaceTexture? = null
    @JvmField
    var playSurface: Surface? = null
    @JvmField
    var player: MediaPlayer? = null
    @JvmField
    var prepareTask: PlayTask? = null
    @JvmField
    var prepared: Boolean = false
    @JvmField
    var progressOn: Boolean = false
    @JvmField
    var resumeAfterSeek: Boolean = false
    @JvmField
    var seekPos: Int = 0
    @JvmField
    var lastWidth: Int = 0
    @JvmField
    var lastHeight: Int = 0
    @JvmField
    var lastVideoWidth: Int = 0
    @JvmField
    var lastVideoHeight: Int = 0
    @JvmField
    var oldPlayer: MediaPlayer? = null

    internal inner class SurfaceListener : SurfaceTextureListener {

        internal inner class ReleaseSurfaceStep : Runnable {

            internal inner class OpenMediaStep : Runnable {
                override fun run() {
                    val myPlayerView = this@MyPlayerView
                    myPlayerView.open(myPlayerView.mediaUri, myPlayerView.referer, myPlayerView.userAgentText)
                }
            }

            override fun run() {
                val myPlayerView = this@MyPlayerView
                val surfaceTexture = myPlayerView.texture
                myPlayerView.texture = null
                if (surfaceTexture != null) {
                    myPlayerView.playSurface = Surface(surfaceTexture)
                    val handler = myPlayerView.mainHandler
                    if (handler == null) {
                        return
                    }
                    handler.post(OpenMediaStep())
                }
            }
        }

        override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
            val myPlayerView = this@MyPlayerView
            myPlayerView.texture = surfaceTexture
            val handler = myPlayerView.mainHandler
            if (handler == null) {
                return
            }
            handler.post(ReleaseSurfaceStep())
        }

        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
            val myPlayerView = this@MyPlayerView
            myPlayerView.texture = null
            myPlayerView.playSurface = null
            return false
        }

        override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
            this@MyPlayerView.fitSize()
        }

        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {
            val myPlayerView = this@MyPlayerView
            val playerViewListener = myPlayerView.playerListener
            if (playerViewListener == null) {
                return
            }
            myPlayerView.progressOn = false
            playerViewListener.onPlaying(false)
        }
    }

    internal inner class ReportErrorTask : Runnable {

        internal inner class ReportErrorStep : Runnable {
            override fun run() {
                val playerViewListener = this@MyPlayerView.playerListener
                if (playerViewListener != null) {
                    playerViewListener.onCompleted()
                }
            }
        }

        override fun run() {
            val myPlayerView = this@MyPlayerView
            val mediaPlayer = myPlayerView.player
            if (mediaPlayer != null) {
                try {
                    mediaPlayer.reset()
                } catch (unused: IllegalStateException) {
                }
            }
            val handler = myPlayerView.mainHandler
            if (handler == null) {
                return
            }
            handler.post(ReportErrorStep())
        }
    }

    internal inner class PlayPauseStep : OnPreparedListener {
        override fun onPrepared(mediaPlayer: MediaPlayer?) {
            val myPlayerView = this@MyPlayerView
            myPlayerView.prepared = true
            myPlayerView.progressOn = true
            myPlayerView.playPause(true, false)
            myPlayerView.postProgress(800)
        }
    }

    internal inner class PlayingStep : OnSeekCompleteListener {
        override fun onSeekComplete(mediaPlayer: MediaPlayer?) {
            val myPlayerView = this@MyPlayerView
            myPlayerView.progressOn = true
            val playerViewListener: PlayerViewListener? = if (myPlayerView.resumeAfterSeek && myPlayerView.seekPos != -1) myPlayerView.playerListener else null
            if (myPlayerView.resumeAfterSeek && myPlayerView.seekPos != -1 && playerViewListener != null) {
                myPlayerView.progressOn = false
                playerViewListener.onPlaying(false)
            }
            myPlayerView.seekPos = -1
            myPlayerView.playPause(true, false)
            myPlayerView.postProgress(400)
        }
    }

    internal inner class FitSizeStep : OnVideoSizeChangedListener {
        override fun onVideoSizeChanged(mediaPlayer: MediaPlayer?, width: Int, height: Int) {
            this@MyPlayerView.fitSize()
        }
    }

    internal inner class OpenPlayerTask : OnCompletionListener {
        override fun onCompletion(mediaPlayer: MediaPlayer?) {
            val myPlayerView = this@MyPlayerView
            if (myPlayerView.mediaUri != null) {
                if (myPlayerView.player == null) {
                    val playerViewListener = myPlayerView.playerListener
                    if (playerViewListener == null) {
                        return
                    }
                    myPlayerView.progressOn = false
                    playerViewListener.onPlaying(false)
                    return
                }
                myPlayerView.prepared = false
                myPlayerView.progressOn = false
                myPlayerView.resumeAfterSeek = false
                myPlayerView.seekPos = -1
                val playerViewListener = myPlayerView.playerListener
                if (playerViewListener != null) {
                    playerViewListener.onPlaying(true)
                }
                MainApp.runInBackground(myPlayerView.ctx, myPlayerView.ResetPlayerTask())
            }
        }
    }

    internal inner class PlayerErrorListener : MediaPlayer.OnErrorListener {
        override fun onError(mediaPlayer: MediaPlayer?, what: Int, extra: Int): Boolean {
            val myPlayerView = this@MyPlayerView
            if (myPlayerView.player == null) {
                return true
            }
            MainApp.runInBackground(myPlayerView.ctx, myPlayerView.ReportErrorTask())
            return true
        }
    }

    internal inner class ResetPlayerTask : Runnable {
        override fun run() {
            val myPlayerView = this@MyPlayerView
            val mediaPlayer = myPlayerView.player
            if (mediaPlayer == null) {
                return
            }
            try {
                mediaPlayer.reset()
            } catch (unused: IllegalStateException) {
            }
            val playTask = myPlayerView.prepareTask
            if (playTask != null) {
                playTask.cancelled = true
            }
            myPlayerView.prepareTask = null
            val prepareTask = PlayTask(myPlayerView)
            myPlayerView.prepareTask = prepareTask
            prepareTask.start(myPlayerView.ctx)
        }
    }

    internal inner class StopOldPlayerTask : Runnable {
        override fun run() {
            val myPlayerView = this@MyPlayerView
            val mediaPlayer = myPlayerView.oldPlayer
            myPlayerView.oldPlayer = null
            if (mediaPlayer == null) {
                return
            }
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }

    internal inner class PlayingTask : Runnable {
        override fun run() {
            val myPlayerView = this@MyPlayerView
            val mediaPlayer: MediaPlayer? = if (!myPlayerView.progressOn || !myPlayerView.prepared) null else myPlayerView.player
            if (!myPlayerView.progressOn || !myPlayerView.prepared || mediaPlayer == null || myPlayerView.playerListener == null) {
                myPlayerView.progressOn = false
                return
            }
            myPlayerView.progressOn = false
            val playerViewListener: PlayerViewListener? = if (mediaPlayer.getDuration() <= 0) null else myPlayerView.playerListener
            if (mediaPlayer.getDuration() <= 0 || playerViewListener == null) {
                return
            }
            myPlayerView.progressOn = false
            playerViewListener.onPlaying(false)
        }
    }

    class PlayTask(myPlayerView: MyPlayerView?) : MyAsyncTask() {
        @JvmField
        var viewRef: WeakReference<*>
        @JvmField
        var isCancelled: Boolean = false

        init {
            this.viewRef = WeakReference<Any?>(myPlayerView)
        }

        public override fun doInBackground() {
            val myPlayerView = this.viewRef.get() as MyPlayerView?
            val uri: Uri? = if (myPlayerView == null || this.cancelled) null else myPlayerView.mediaUri
            if (myPlayerView == null || this.cancelled || uri == null) {
                return
            }
            val mapU = MainUtil.requestHeadersFor(myPlayerView.ctx, myPlayerView.referer, myPlayerView.userAgentText)
            val mediaPlayer = myPlayerView.player
            if (mediaPlayer != null) {
                try {
                    val context = myPlayerView.ctx
                    if (mapU != null) {
                        mediaPlayer.setDataSource(context!!, uri, mapU)
                    } else {
                        mediaPlayer.setDataSource(context!!, uri)
                    }
                } catch (unused: Exception) {
                    this.isCancelled = true
                }
            }
        }

        public override fun onCancelled() {
            val myPlayerView = this.viewRef.get() as MyPlayerView?
            if (myPlayerView == null) {
                return
            }
            myPlayerView.prepareTask = null
        }

        public override fun onPostExecute() {
            val myPlayerView = this.viewRef.get() as MyPlayerView?
            if (myPlayerView != null) {
                myPlayerView.prepareTask = null
                val mediaPlayer: MediaPlayer? = if (this.isCancelled) null else myPlayerView.player
                if (this.isCancelled || mediaPlayer == null) {
                    if (myPlayerView.player == null) {
                        return
                    }
                    MainApp.runInBackground(myPlayerView.ctx, myPlayerView.ReportErrorTask())
                } else {
                    try {
                        mediaPlayer.prepareAsync()
                    } catch (unused: Exception) {
                        if (myPlayerView.player != null) {
                            MainApp.runInBackground(myPlayerView.ctx, myPlayerView.ReportErrorTask())
                        }
                    }
                }
            }
        }
    }

    interface PlayerViewListener {
        fun onVideoSize(width: Int, height: Int)

        fun onPlaying(playing: Boolean)

        fun onProgress()

        fun onCompleted()
    }

    init {
        this.mainHandler = Handler(Looper.getMainLooper())
        setSurfaceTextureListener(SurfaceListener())
    }

    fun postProgress(delayMs: Int) {
        if (!this.progressOn || !this.prepared || this.player == null || this.playerListener == null) {
            this.progressOn = false
            return
        }
        val handler = this.mainHandler
        if (handler == null) {
            return
        }
        handler.postDelayed(PlayingTask(), delayMs.toLong())
    }

    fun open(uri: Uri?, referer: String?, userAgent: String?) {
        this.mediaUri = uri
        this.referer = referer
        this.userAgentText = userAgent
        if (uri == null || this.player != null || this.playSurface == null) {
            return
        }
        this.prepared = false
        this.progressOn = false
        this.resumeAfterSeek = false
        this.seekPos = -1
        val playerViewListener = this.playerListener
        if (playerViewListener != null) {
            playerViewListener.onPlaying(true)
        }
        val mediaPlayer = MediaPlayer()
        this.player = mediaPlayer
        mediaPlayer.setSurface(this.playSurface)
        this.player!!.setOnPreparedListener(PlayPauseStep())
        this.player!!.setOnSeekCompleteListener(PlayingStep())
        this.player!!.setOnVideoSizeChangedListener(FitSizeStep())
        this.player!!.setOnCompletionListener(OpenPlayerTask())
        this.player!!.setOnErrorListener(PlayerErrorListener())
        val playTask = this.prepareTask
        if (playTask != null) {
            playTask.cancelled = true
        }
        this.prepareTask = null
        val prepareTask = PlayTask(this)
        this.prepareTask = prepareTask
        prepareTask.start(this.ctx)
    }

    fun stop() {
        val playTask = this.prepareTask
        if (playTask != null) {
            playTask.cancelled = true
        }
        this.prepareTask = null
        val mediaPlayer = this.player
        this.oldPlayer = mediaPlayer
        this.player = null
        if (mediaPlayer == null) {
            return
        }
        MainApp.runInBackground(this.ctx, StopOldPlayerTask())
    }

    fun fitSize() {
        val mediaPlayer = this.player
        if (mediaPlayer != null) {
            val videoWidth = mediaPlayer.getVideoWidth()
            val videoHeight = this.player!!.getVideoHeight()
            val parent: ViewParent? = if (videoWidth == 0 || videoHeight == 0) null else getParent()
            if (videoWidth == 0 || videoHeight == 0 || parent == null || (parent !is ViewGroup)) {
                return
            }
            val viewGroup = parent
            var width = viewGroup.getWidth()
            var height = viewGroup.getHeight()
            if (width == 0 || height == 0) {
                return
            }
            if (this.lastWidth == width && this.lastHeight == height && this.lastVideoWidth == videoWidth && this.lastVideoHeight == videoHeight) {
                return
            }
            this.lastWidth = width
            this.lastHeight = height
            this.lastVideoWidth = videoWidth
            this.lastVideoHeight = videoHeight
            val ratio = (videoHeight / videoWidth).toFloat()
            val fitHeight = (width * ratio).toInt()
            if (fitHeight > height) {
                width = (height / ratio).toInt()
            } else {
                height = fitHeight
            }
            val playerViewListener = this.playerListener
            if (playerViewListener != null) {
                playerViewListener.onVideoSize(width, height)
            }
        }
    }

    fun playPause(play: Boolean, resume: Boolean) {
        val mediaPlayer = this.player
        if (if (mediaPlayer == null) false else this.prepared) {
            if (resume) {
                this.resumeAfterSeek = !play
            }
            if (play) {
                if (!this.resumeAfterSeek) {
                    this.seekPos = -1
                    if (!mediaPlayer!!.isPlaying()) {
                        this.player!!.start()
                    }
                }
            } else if (mediaPlayer!!.isPlaying()) {
                this.player!!.pause()
            }
            val playerViewListener = this.playerListener
            if (playerViewListener != null) {
                playerViewListener.onProgress()
            }
        }
    }

    val duration: Int
        get() {
            val mediaPlayer = this.player
            val duration = if (!(if (mediaPlayer == null) false else this.prepared)) 0 else mediaPlayer!!.getDuration()
            if (!(if (mediaPlayer == null) false else this.prepared) || duration < 0) {
                return 0
            }
            return duration
        }

    val position: Int
        get() {
            val mediaPlayer = this.player
            if (if (mediaPlayer == null) false else this.prepared) {
                val i = this.seekPos
                if (i != -1) {
                    return i
                }
                val currentPosition = mediaPlayer!!.getCurrentPosition()
                if (currentPosition >= 0) {
                    return currentPosition
                }
            }
            return 0
        }

    fun setListener(playerViewListener: PlayerViewListener?) {
        this.playerListener = playerViewListener
    }

    companion object {
    }
}
