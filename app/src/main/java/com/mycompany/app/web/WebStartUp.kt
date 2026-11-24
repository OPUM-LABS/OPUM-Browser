package com.mycompany.app.web

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewTreeObserver
import androidx.webkit.Profile
import androidx.webkit.ProfileStore
import androidx.webkit.WebViewFeature

/**
 * Faster first start: right after the browser window has drawn its first frame, the page renderer process is started,
 * instead of when the first page loads, so the first page appears sooner (about 0.2 s on the test phone). Through
 * androidx.webkit (still experimental; where the WebView lacks it nothing changes). Only from the browser window, so a
 * start for a widget or a download does not start a renderer.
 *
 * Loading Chromium itself in the background (WebViewCompat.startUpWebView) was tried and dropped: the browser creates
 * its first page right after the first frame, so the main thread waited for it anyway, and the extra thread slowed
 * the first frame down.
 */
object WebStartUp {
    private const val TAG = "OpumStartUp"

    private var started = false

    fun begin(activity: Activity) {
        if (started) return
        started = true
        // Not now: asking for the window's decor view during onCreate, before the activity has set up its window,
        // makes Android build its default one, with a title bar. After the launch has run, it is the activity's own.
        val main = Handler(Looper.getMainLooper())
        main.post { afterLaunch(activity, main) }
    }

    private fun afterLaunch(activity: Activity, main: Handler) {
        try {
            if (activity.isFinishing) return
            val decor = activity.window?.peekDecorView() ?: return warmUpRenderer(activity.applicationContext)
            // the first frame is on screen already: nothing left to wait for
            if (decor.isLaidOut && decor.width > 0) return warmUpRenderer(activity.applicationContext)
            val observer = decor.viewTreeObserver
            val onDraw = object : ViewTreeObserver.OnDrawListener {
                var done = false
                override fun onDraw() {
                    if (done) return
                    done = true
                    // after this frame is on screen; a draw listener cannot remove itself while drawing
                    main.post {
                        if (decor.viewTreeObserver.isAlive) decor.viewTreeObserver.removeOnDrawListener(this)
                        warmUpRenderer(activity.applicationContext)
                    }
                }
            }
            observer.addOnDrawListener(onDraw)
        } catch (e: Exception) {
            Log.w(TAG, "start-up help failed: ${e.message}")
        }
    }

    /** Starts the page renderer process now, so the first page does not wait for it. Main thread. */
    @androidx.annotation.OptIn(Profile.ExperimentalWarmUpRendererProcess::class)
    private fun warmUpRenderer(context: android.content.Context) {
        try {
            if (!WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) return
            // cookies and site data of workspaces deleted while one of their pages was open
            WebProfiles.dropDeleted(context)
            if (!WebViewFeature.isFeatureSupported(WebViewFeature.WARM_UP_RENDERER_PROCESS)) return
            ProfileStore.getInstance().getOrCreateProfile(WebProfiles.currentName()).warmUpRendererProcess()
            Log.i(TAG, "renderer warm-up started")
        } catch (e: Throwable) {
            Log.w(TAG, "renderer warm-up failed: ${e.message}")
        }
    }
}
