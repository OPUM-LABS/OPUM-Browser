package com.mycompany.app.web

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.view.isEmpty
import java.lang.ref.WeakReference

/**
 * The page renderer was ended without crashing (2026-11-24): what Android does when it needs memory, and what
 * `chrome://kill` does. All web views share that one process, so every one of them reports it at once.
 *
 * Before, the browser then restarted its whole process (a cold start, also while it was in the background). Now it
 * recovers in place: every tab lets go of its dead pages and keeps their address, history position and scroll
 * (as when pages are released to save memory), the current tab builds its page again and loads it, and the other
 * tabs load theirs when they are opened (in the background the current tab too waits until the browser is back).
 * The many reports of one renderer loss lead to one recovery.
 * A renderer that crashed keeps the old handling.
 */
object RendererRecovery {
    private const val TAG = "OpumRenderer"

    private var browser: WeakReference<WebViewActivity>? = null
    private var pending = false
    private var rebuildOnResume = false
    private val main by lazy { Handler(Looper.getMainLooper()) }

    /** A chrome:// debug address (chrome://kill, chrome://crash, ...): it ends or hangs the renderer by itself. */
    fun isDebugAddress(url: String?): Boolean = url != null && url.startsWith("chrome://", ignoreCase = true)

    /**
     * Where a tab's history ([addresses], the current entry at [index]) goes on after a renderer loss: back past
     * debug addresses at the end (loading chrome://kill again would end the new renderer at once). Returns the new
     * current index (the entries after it are dropped), or -1 when nothing is left to show.
     */
    fun afterDebug(addresses: List<String?>, index: Int): Int {
        var i = index
        while (i >= 0 && i < addresses.size && isDebugAddress(addresses[i])) i--
        return i
    }

    /** The browser window is back on screen: build the current tab's page if a recovery out of sight left it to now. */
    fun onResume(activity: WebViewActivity) {
        if (!rebuildOnResume) return
        rebuildOnResume = false
        activity.webCoord?.post(WvaReloadAttachTask(activity))
    }

    /** The browser window that recovers (set when it is created). */
    fun attach(activity: WebViewActivity) {
        browser = WeakReference(activity)
    }

    /**
     * A web view lost its renderer without a crash. Returns false when there is no browser window to recover (the
     * caller then falls back to the old restart).
     */
    fun onRendererGone(): Boolean {
        val activity = browser?.get() ?: return false
        if (activity.isFinishing || activity.isClosing) return false
        if (pending) return true
        pending = true
        // after the other web views of this loss have reported it too
        main.post {
            pending = false
            recover(activity)
        }
        return true
    }

    private fun recover(activity: WebViewActivity) {
        if (activity.isFinishing || activity.isClosing) return
        var released = 0
        try {
            val tabs = activity.tabsState.tabItems
            if (tabs != null) {
                for (tab in tabs) {
                    val frame = tab?.webFrame ?: continue
                    if (frame.isEmpty() || frame.pageItems.isNullOrEmpty()) continue
                    // keeps each page's address, history and scroll; the dead web views are destroyed later
                    frame.setBlankView(false)
                    released++
                }
            }
            // chrome://kill and the like end the renderer themselves: go back past them (loading one again would loop)
            val current = activity.tabsState.currentFrame
            val items = current?.pageItems
            if (current != null && items != null) {
                val index = current.pageIndex
                val addresses = items.map { if (it == null) null else if (it.blankPending) it.pageUrl else it.url }
                val back = afterDebug(addresses, index)
                if (back != index) {
                    while (items.size > maxOf(back + 1, 1)) items.removeAt(items.size - 1)
                    if (back >= 0) {
                        current.pageIndex = back
                    } else {
                        // only debug pages: the tab shows an empty page
                        items[0]?.let { it.blankPending = false; it.pageUrl = null; it.url = null }
                        current.pageIndex = 0
                    }
                }
            }
            // the current tab builds its page again from what was kept, with settings and listeners, and loads it;
            // out of sight only when the browser is back (a new renderer now could be ended again at once)
            if (activity.lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.STARTED)) {
                activity.webCoord?.post(WvaReloadAttachTask(activity))
            } else {
                rebuildOnResume = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "recovery failed: ${e.message}")
        }
        Log.i(TAG, "renderer gone: recovered in place, $released tabs reload when shown")
    }
}
