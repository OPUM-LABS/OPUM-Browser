package com.mycompany.app.web

import android.content.Context
import android.net.ConnectivityManager
import android.os.CancellationSignal
import android.util.Log
import android.util.Patterns
import android.view.View
import android.webkit.URLUtil
import androidx.webkit.PrefetchException
import androidx.webkit.Profile
import androidx.webkit.ProfileStore
import androidx.webkit.WebViewFeature
import androidx.webkit.WebViewOutcomeReceiver
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.SyncSettings

/**
 * "Preload pages" (Settings > Start page & search): while the address is being typed, the page that Enter would open
 * is fetched in the background once the typing pauses, so it shows almost at once when Enter comes. Only the page
 * itself is fetched (no images or scripts), through the WebView's own prefetch (androidx.webkit, still experimental;
 * nothing happens where the WebView lacks it), and a navigation to the same address takes the fetched answer.
 *
 * Off in secret mode, with "Save data", and while Android's Data Saver is on, whatever the setting says. A search is
 * only preloaded when the search engine already sees the typing (search suggestions on).
 */
object Preload {
    const val OFF = 0
    const val WIFI = 1
    const val ALWAYS = 2

    /** How long the typing must pause before the page is fetched. */
    private const val PAUSE_MS = 400L

    /** Shorter text is not worth a fetch (most of it is still being typed). */
    private const val MIN_LENGTH = 3

    private val handler by lazy { android.os.Handler(android.os.Looper.getMainLooper()) }
    private var pending: Runnable? = null
    private var lastUrl: String? = null
    private var cancel: CancellationSignal? = null

    /** Whether to preload now: [mode] and the connection, and none of the reasons that always stop it. */
    fun allowed(mode: Int, unmetered: Boolean, secret: Boolean, saveData: Boolean, dataSaver: Boolean): Boolean =
        !secret && !saveData && !dataSaver && when (mode) {
            ALWAYS -> true
            WIFI -> unmetered
            else -> false
        }

    /**
     * The address Enter would open for [text] (the rules of `MainUtil.normalizeUrl` without its side effects), or null
     * when there is nothing to preload: too short, not https, or a search while [searchAllowed] is false.
     * [isUrl] is a complete address, [looksLikeAddress] one typed without a scheme ("example.com").
     */
    fun targetFor(
        text: String?,
        httpsOnly: Boolean,
        searchAllowed: Boolean,
        isUrl: (String) -> Boolean,
        looksLikeAddress: (String) -> Boolean,
        searchUrl: (String) -> String?,
    ): String? {
        val typed = text?.trim() ?: return null
        if (typed.length < MIN_LENGTH) return null
        val url = when {
            isUrl(typed) -> typed
            looksLikeAddress(typed) -> (if (httpsOnly) "https://" else "http://") + typed
            searchAllowed -> searchUrl(typed)
            else -> null
        } ?: return null
        // the WebView prefetches only over HTTPS
        return if (url.startsWith("https://")) url else null
    }

    /** The address text changed: fetch what Enter would open once the typing pauses ([text] is read then). */
    fun onTyping(view: View, editing: () -> Boolean, text: () -> String?) {
        pending?.let { handler.removeCallbacks(it) }
        pending = null
        if (BrowserSettings.preload.value == OFF) return
        val context = view.context.applicationContext
        val task = Runnable {
            pending = null
            if (editing()) fetch(context, text())
        }
        pending = task
        handler.postDelayed(task, PAUSE_MS)
    }

    private fun fetch(context: Context, text: String?) {
        try {
            if (!allowed(BrowserSettings.preload.value, isUnmetered(context), SyncSettings.secretMode.value, PdfSettings.saveData.value, isDataSaverOn(context))) return
            val url = targetFor(
                text, PdfSettings.onlyHttps.value, BrowserSettings.sugEngine.value != 0,
                isUrl = { URLUtil.isValidUrl(it) },
                looksLikeAddress = { Patterns.WEB_URL.matcher(it).matches() },
                searchUrl = { MainUtil.searchUrlFor(it) },
            ) ?: return
            if (url == lastUrl) return
            cancel?.cancel()
            val signal = CancellationSignal()
            cancel = signal
            lastUrl = url
            prefetch(url, signal)
        } catch (e: Exception) {
            Log.w(TAG, "preload failed: ${e.message}")
        }
    }

    @androidx.annotation.OptIn(Profile.ExperimentalUrlPrefetch::class)
    private fun prefetch(url: String, signal: CancellationSignal) {
        // where the WebView lacks either, nothing is preloaded
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) return
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.PROFILE_URL_PREFETCH)) return
        // into the space in use (its cookies and cache)
        val profile = ProfileStore.getInstance().getOrCreateProfile(WebProfiles.currentName())
        profile.prefetchUrlAsync(url, signal, { it.run() }, object : WebViewOutcomeReceiver<Void?, PrefetchException> {
            override fun onResult(result: Void?) {
                Log.i(TAG, "preloaded ${MainUtil.hostOfUrl(url, true)}")
            }

            override fun onError(error: PrefetchException) {
                if (lastUrl == url) lastUrl = null
                Log.i(TAG, "preload skipped: ${error.message}")
            }
        })
    }

    /** Not a metered connection (Wi-Fi, Ethernet; a metered Wi-Fi hotspot counts as mobile data). */
    private fun isUnmetered(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        return cm.activeNetwork != null && !cm.isActiveNetworkMetered
    }

    /** Android's Data Saver is on for this app. */
    private fun isDataSaverOn(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        return cm.isActiveNetworkMetered && cm.restrictBackgroundStatus == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED
    }

    private const val TAG = "OpumPreload"
}
