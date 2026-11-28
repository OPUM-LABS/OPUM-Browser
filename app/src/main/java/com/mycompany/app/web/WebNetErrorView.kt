package com.mycompany.app.web

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil.bottomInsetTotal
import com.mycompany.app.main.MainUtil.topBarHeight
import com.mycompany.app.net.LocalNet
import com.mycompany.app.net.NetDiag
import com.mycompany.app.net.NetDiagnosis
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.ui.browser.ComposeOverlay
import com.mycompany.app.ui.net.NetAction
import com.mycompany.app.ui.net.NetErrorPage
import com.mycompany.app.ui.net.NetworkCheckActivity
import com.mycompany.app.ui.state.AppState

/**
 * The page shown over a tab when its page could not be loaded because of the network (2026-11-28): replaces
 * WebView's own error page with the likely cause (`NetDiag`) and buttons that help.
 */
class WebNetErrorView(context: Context) : ComposeOverlay(context) {
    /** Reloads the page (set by `WebNestView`). */
    var onRetry: () -> Unit = {}
    private var url by mutableStateOf<String?>(null)
    private var errorText by mutableStateOf<String?>(null)
    private var diagnosis by mutableStateOf<NetDiagnosis?>(null)
    private var dark by mutableStateOf(AppState.darkUi)
    private var host: String? = null
    private var code = 0
    private var serial = 0
    private val main = Handler(Looper.getMainLooper())

    init {
        setPage({ dark }) {
            NetErrorPage(url = url, errorText = errorText, diagnosis = diagnosis, onAction = ::onAction)
        }
    }

    fun applyDark() { dark = AppState.darkUi }

    /** Keeps the page clear of the bars. */
    fun applyPadding() {
        var pad = MainApp.dp8
        if (!BrowserSettings.fixTop.value) pad += topBarHeight()
        if (!BrowserSettings.fixBot.value) pad += bottomInsetTotal()
        if (paddingBottom != pad) setPadding(0, 0, 0, pad)
    }

    /** A new error: shows it and works out the cause in the background. */
    fun show(pageUrl: String?, errorCode: Int, description: String?) {
        url = pageUrl
        code = errorCode
        errorText = description?.takeIf { it.isNotBlank() }
        host = try { pageUrl?.toUri()?.host } catch (_: Exception) { null }
        diagnose()
    }

    /** Works the cause out again (back from the settings or the permission prompt). */
    fun diagnose() {
        diagnosis = null
        val mine = ++serial
        val app = context.applicationContext
        val h = host
        val c = code
        Thread {
            val d = NetDiag.diagnose(app, h, c)
            main.post { if (mine == serial) diagnosis = d }
        }.start()
    }

    /** Wi-Fi or mobile data switched while the page shows: the cause is worked out again. */
    private val netCallback = object : android.net.ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: android.net.Network) = changed()
        override fun onLost(network: android.net.Network) = changed()
        override fun onCapabilitiesChanged(network: android.net.Network, caps: android.net.NetworkCapabilities) = changed()
    }
    private var listening = false
    private val rediagnose = Runnable { if (diagnosis != null) diagnose() }

    private fun changed() {
        main.removeCallbacks(rediagnose)
        main.postDelayed(rediagnose, 700)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!listening) try {
            context.getSystemService(android.net.ConnectivityManager::class.java)
                ?.registerNetworkCallback(android.net.NetworkRequest.Builder().build(), netCallback)
            listening = true
        } catch (_: Exception) {
        }
    }

    override fun onDetachedFromWindow() {
        main.removeCallbacks(rediagnose)
        if (listening) try {
            context.getSystemService(android.net.ConnectivityManager::class.java)?.unregisterNetworkCallback(netCallback)
        } catch (_: Exception) {
        }
        listening = false
        super.onDetachedFromWindow()
    }

    /** Back from the Wi-Fi settings or the permission prompt: the cause may have changed. */
    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (hasWindowFocus && diagnosis != null && isVisible) diagnose()
    }

    private fun onAction(action: NetAction) {
        val activity = activityOf(context)
        when (action) {
            NetAction.RETRY -> onRetry()
            NetAction.ALLOW_LOCAL -> if (activity == null || !LocalNet.request(activity)) onRetry()
            NetAction.WIFI -> open(Intent(Settings.ACTION_WIFI_SETTINGS))
            NetAction.INTERNET -> open(Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY))
            NetAction.CHECK -> NetworkCheckActivity.open(context, url)
        }
    }

    private fun open(intent: Intent) {
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
            try { context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) {}
        }
    }

    companion object {
        fun activityOf(context: Context): Activity? {
            var c: Context? = context
            while (c is ContextWrapper) {
                if (c is Activity) return c
                c = c.baseContext
            }
            return null
        }
    }
}
