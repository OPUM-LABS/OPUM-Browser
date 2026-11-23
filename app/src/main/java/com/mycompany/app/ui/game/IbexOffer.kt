package com.mycompany.app.ui.game

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.SystemClock
import android.webkit.WebViewClient
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.view.MySnackbar
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.web.WebNestView
import com.mycompany.app.web.closeAllSnackbars

/**
 * The offline entry to [IbexGameActivity]: when the page in front fails to load because the device has no
 * connection, a bar offers a round with the ibex. Shown at most once every few minutes.
 */
object IbexOffer {
    private const val QUIET_MS = 5 * 60_000L
    private var lastShown = -QUIET_MS

    private fun isNetError(code: Int) = code == WebViewClient.ERROR_HOST_LOOKUP ||
        code == WebViewClient.ERROR_CONNECT || code == WebViewClient.ERROR_TIMEOUT

    fun offline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return true
        return !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
            !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /** Called for a failed main-frame load of [view]. */
    fun onMainFrameError(activity: WebViewActivity, view: WebNestView?, code: Int) {
        if (!isNetError(code) || view == null || view !== activity.currentPage) return
        if (activity.webCoord == null || activity.isClosing) return
        val now = SystemClock.elapsedRealtime()
        if (now - lastShown < QUIET_MS || !offline(activity)) return
        lastShown = now
        activity.closeAllSnackbars()
        val bar = MySnackbar(activity)
        activity.snackbar = bar
        bar.setAppBarView(activity.barScroll)
        bar.snackType = 5.toByte()
        bar.show(activity.webCoord, R.string.game_offline_hint, R.string.game_play, 0, object : MySnackbar.SnackbarListener {
            override fun onFirst() = IbexGameActivity.open(activity)
            override fun onSecond() {}
            override fun onDismissed() {}
            override fun onDismiss() {
                if (activity.snackbar === bar) activity.snackbar = null
            }
        })
    }
}
