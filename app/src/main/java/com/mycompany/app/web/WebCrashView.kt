package com.mycompany.app.web

import android.content.Context
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.BrowserSettings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil.topBarHeight
import com.mycompany.app.main.MainUtil.bottomInsetTotal
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.browser.ComposeOverlay
import com.mycompany.app.ui.browser.CrashPage
import androidx.appcompat.widget.AppCompatTextView

/**
 * The page shown over a tab when its renderer process died. Compose now (`CrashPage`); the class keeps its old shape for
 * `WebNestView`: [setListener], [close] (theme changed), [applyPadding] (bar padding changed) and [crashUrl]. The old widget fields stay
 * (always null) so the teardown code that clears them still compiles.
 */
class WebCrashView(context: Context?) : ComposeOverlay(context!!) {
    @JvmField var crashListener: CrashViewListener? = null
    @JvmField var crashedUrl: String? = null

    private var shownUrl by mutableStateOf<String?>(null)
    private var dark by mutableStateOf(AppState.darkUi)

    interface CrashViewListener {
        fun onAction(action: Int)
        fun onRefresh(url: String?)
    }

    /** Theme changed. */
    fun applyDark() { dark = AppState.darkUi }

    /** Keeps the page clear of the bars. */
    fun applyPadding() {
        var pad = MainApp.dp8
        if (!BrowserSettings.fixTop.value) pad += topBarHeight()
        if (!BrowserSettings.fixBot.value) pad += bottomInsetTotal()
        if (paddingBottom != pad) setPadding(0, 0, 0, pad)
    }

    var crashUrl: String?
        get() = crashedUrl
        set(str) { crashedUrl = str; shownUrl = str }

    fun setListener(crashViewListener: CrashViewListener?) {
        crashListener = crashViewListener
        setPage({ dark }) {
            CrashPage(
                url = shownUrl,
                onRefresh = { crashListener?.onRefresh(crashedUrl) },
                onMemorySave = { crashListener?.onAction(10) },
                onCleanMode = { crashListener?.onAction(7) },
                onClearData = { crashListener?.onAction(27) },
            )
        }
    }
}
