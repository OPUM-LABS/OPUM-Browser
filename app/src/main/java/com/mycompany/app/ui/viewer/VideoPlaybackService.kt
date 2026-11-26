package com.mycompany.app.ui.viewer

import android.content.Context
import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Background playback of the video player ([VideoPlayerActivity], setting "Background playback"): while the player is
 * hidden and the setting is on, this service holds the player's [MediaSession], so Media3 shows the media notification
 * (play / pause, seek, lock screen) and keeps the sound going. The player itself stays with the activity; the service
 * neither makes nor releases it. It stops when the player comes back or closes, or when the app is swiped away.
 */
class VideoPlaybackService : MediaSessionService() {

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = current

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val session = current
        if (session == null) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (!isSessionAdded(session)) addSession(session)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // the app was swiped away: stop the sound with it
        try { current?.player?.pause() } catch (_: Throwable) {}
        stopSelf()
    }

    override fun onDestroy() {
        current?.let { if (isSessionAdded(it)) removeSession(it) }
        super.onDestroy()
    }

    companion object {
        /** The session of the player that plays in the background (the activity owns and releases it). */
        private var current: MediaSession? = null

        fun start(context: Context, session: MediaSession) {
            current = session
            try {
                context.startService(Intent(context, VideoPlaybackService::class.java))
            } catch (_: Throwable) {
                current = null
            }
        }

        fun stop(context: Context) {
            if (current == null) return
            try { context.stopService(Intent(context, VideoPlaybackService::class.java)) } catch (_: Throwable) {}
            current = null
        }
    }
}
