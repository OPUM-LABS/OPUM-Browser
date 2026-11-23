package com.mycompany.app.ui.browser

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.WebView
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * What the browser chrome shows about the current tab, as Compose snapshot state, so a Compose piece can read
 * it and recompose when it changes. It only mirrors: `WebViewActivity` stays the owner of the real state (its fields and the
 * `WebView`), and [sync] copies the values over. Nothing renders from it yet.
 *
 * Mirrored now: url, title, progress, back / forward availability and the app's dark flag. Not mirrored yet (each is
 * added by the slice that first needs it): tab count and list (6.8 / 6.9), find-in-page (6.4), bar visibility (6.10),
 * secure / certificate state (6.2).
 */
@Stable
class BrowserState {
    var url by mutableStateOf<String?>(null); private set
    var title by mutableStateOf<String?>(null); private set
    /** 0..100; 100 when idle. */
    var progress by mutableIntStateOf(100); private set
    var canGoBack by mutableStateOf(false); private set
    var canGoForward by mutableStateOf(false); private set
    var dark by mutableStateOf(false); private set

    val loading: Boolean get() = progress in 1..99

    /** Copies the current page's values; [webView] is the active tab's view, or null when there is none. */
    @Suppress("DEPRECATION")
    fun sync(webView: WebView?, dark: Boolean) {
        val newUrl = try { webView?.url } catch (_: Exception) { null }
        val newTitle = try { webView?.title } catch (_: Exception) { null }
        val newProgress = try { webView?.progress ?: 100 } catch (_: Exception) { 100 }
        val back = try { webView?.canGoBack() == true } catch (_: Exception) { false }
        val forward = try { webView?.canGoForward() == true } catch (_: Exception) { false }
        var changed = false
        if (url != newUrl) { url = newUrl; changed = true }
        if (title != newTitle) { title = newTitle; changed = true }
        if (progress != newProgress) { progress = newProgress; changed = true }
        if (canGoBack != back) { canGoBack = back; changed = true }
        if (canGoForward != forward) { canGoForward = forward; changed = true }
        if (this.dark != dark) { this.dark = dark; changed = true }
        if (changed && Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "url=$url title=$title progress=$progress back=$canGoBack fwd=$canGoForward dark=$dark")
    }

    /** Runs [sync] every [PERIOD_MS] on the main thread while started (the activity is resumed). */
    class Ticker(private val state: BrowserState, private val source: () -> Pair<WebView?, Boolean>) {
        private val handler = Handler(Looper.getMainLooper())
        private var running = false
        private val tick = object : Runnable {
            override fun run() {
                if (!running) return
                val (view, dark) = source()
                state.sync(view, dark)
                handler.postDelayed(this, PERIOD_MS)
            }
        }
        fun start() { if (!running) { running = true; handler.post(tick) } }
        fun stop() { running = false; handler.removeCallbacks(tick) }
    }

    companion object {
        const val TAG = "BrowserState"
        const val PERIOD_MS = 150L
    }
}
