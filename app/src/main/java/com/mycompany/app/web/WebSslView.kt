package com.mycompany.app.web

import android.content.Context
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.BrowserSettings
import android.net.http.SslError
import android.webkit.SslErrorHandler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil.topBarHeight
import com.mycompany.app.main.MainUtil.bottomInsetTotal
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.browser.ComposeOverlay
import com.mycompany.app.ui.browser.SslErrorPage
import androidx.appcompat.widget.AppCompatTextView
import com.mycompany.app.ui.browser.IconButton

/**
 * The page shown over a tab when its site has an SSL certificate problem. Compose now (`SslErrorPage`); the class keeps
 * its old shape because `WebNestView` and `WebViewActivity` drive it: [setListener], [close] (theme changed), [showError] (new error
 * to show), [applyPadding] (bar padding changed), and the handler / error fields [sslHandler] / [pendingError]. The old per-widget fields stay (always
 * null) so the teardown code that clears them still compiles.
 */
class WebSslView(context: Context?) : ComposeOverlay(context!!) {
    @JvmField var sslListener: SslViewListener? = null
    @JvmField var closeButton: IconButton? = null
    /** The WebView's pending decision for this error; resolved by Continue (proceed) or Exit (cancel). */
    @JvmField var sslHandler: SslErrorHandler? = null
    @JvmField var pendingError: SslError? = null

    private var shown by mutableStateOf<SslError?>(null)
    private var dark by mutableStateOf(AppState.darkUi)
    private var shownTick by mutableIntStateOf(0)

    interface SslViewListener {
        fun onClose()
        fun onSettings()
        fun onProceed()
    }

    /** Theme changed. */
    fun applyDark() { dark = AppState.darkUi }

    /** [pendingError] holds a new error: show it. */
    fun showError() { shown = pendingError; shownTick++ }

    /** Keeps the page clear of the bars. */
    fun applyPadding() {
        var pad = MainApp.dp8
        if (!BrowserSettings.fixTop.value) pad += topBarHeight()
        if (!BrowserSettings.fixBot.value) pad += bottomInsetTotal()
        if (paddingBottom != pad) setPadding(0, 0, 0, pad)
    }

    fun setListener(sslViewListener: SslViewListener?) {
        sslListener = sslViewListener
        setPage({ dark }) {
            @Suppress("UNUSED_EXPRESSION") shownTick
            SslErrorPage(
                error = shown,
                onSettings = { sslListener?.onSettings() },
                onContinue = {
                    val listener = sslListener ?: return@SslErrorPage
                    sslHandler?.proceed(); sslHandler = null
                    listener.onClose()
                },
                onExit = {
                    val listener = sslListener ?: return@SslErrorPage
                    sslHandler?.cancel(); sslHandler = null
                    listener.onProceed()
                },
            )
        }
        showError()
    }

    companion object {
    }
}
