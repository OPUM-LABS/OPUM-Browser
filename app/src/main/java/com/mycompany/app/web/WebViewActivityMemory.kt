package com.mycompany.app.web

import android.content.ComponentCallbacks2
import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.mycompany.app.data.book.DataBookTmem
import com.mycompany.app.main.MainUtil

/**
 * Giving memory back when Android asks (2026-11-21). Before, `onTrimMemory` was not handled: when the phone ran low,
 * Android killed the whole browser and every page reloaded. Now hidden pages are released first: their web views are
 * destroyed, and the address is kept, so the page loads again when it is opened (as with the memory limits in
 * Settings > Performance & memory). The page on screen is never released, nor are sites on the "keep in memory" list.
 */
object MemoryRelease {
    /** How much to release. */
    enum class Step {
        /** Nothing (the app only went out of sight, or the warning is mild). */
        NONE,
        /**
         * Background tabs (except the ones next to the current tab) and, in every tab, the pages more than one step
         * behind or ahead of the shown one.
         */
        HIDDEN,
        /** As [HIDDEN], and the tabs next to the current tab too. */
        ALL,
    }

    /**
     * The step for an `onTrimMemory` [level]. Android 14+ only sends [ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN] and
     * [ComponentCallbacks2.TRIM_MEMORY_BACKGROUND]; older versions also send the running levels while the app is in use.
     * Going out of sight alone releases nothing (switching apps for a moment must not reload the tabs).
     */
    @Suppress("DEPRECATION")
    fun stepFor(level: Int): Step = when {
        level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE -> Step.ALL
        level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND -> Step.HIDDEN
        level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> Step.NONE
        level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> Step.ALL
        level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> Step.HIDDEN
        else -> Step.NONE
    }

    /** Indices of the tabs (of [size], [current] shown) whose pages all go at [step]: all but the current one for [Step.ALL]. */
    fun tabsToRelease(size: Int, current: Int, step: Step): List<Int> = when (step) {
        Step.NONE -> emptyList()
        Step.HIDDEN -> (0 until size).filter { it < current - 1 || it > current + 1 }
        Step.ALL -> (0 until size).filter { it != current }
    }

    /** The site of [url] is on the "keep in memory" list (Settings > Performance & memory). */
    fun keptInMemory(context: Context?, url: String?): Boolean {
        try {
            val keep = DataBookTmem.instance(context)
            val sites = keep.sites
            if (sites == null || sites.isEmpty()) return false
            val host = MainUtil.hostOfUrl(url, true)
            if (!TextUtils.isEmpty(host) && sites.contains(host)) return true
            val alt = MainUtil.trimTrailingSlash(url)
            return !TextUtils.isEmpty(alt) && sites.contains(alt)
        } catch (_: Exception) {
            return false
        }
    }
}

/** Android reports memory pressure at [level]: release what is not on screen (see [MemoryRelease]). */
fun WebViewActivity.releaseMemory(level: Int) {
    val step = MemoryRelease.stepFor(level)
    if (step == MemoryRelease.Step.NONE || this.isClosing) return
    val tabs = this.tabsState.tabItems ?: return
    val context = this.dialogsState.appContext
    var released = 0
    try {
        val current = this.currentTabIndex
        for (index in MemoryRelease.tabsToRelease(tabs.size, current, step)) {
            val tab = tabs.getOrNull(index) ?: continue
            val frame = tab.webFrame ?: continue
            if (frame.childCount == 0 || frame.pageItems.isNullOrEmpty()) continue
            if (MemoryRelease.keptInMemory(context, tab.tabUrl)) continue
            frame.setBlankView(false)
            released++
        }
        // the tabs that stay: only the pages next to the shown one
        for (index in tabs.indices) {
            val tab = tabs.getOrNull(index) ?: continue
            val frame = tab.webFrame ?: continue
            if (frame.childCount == 0) continue
            if (index != current && MemoryRelease.keptInMemory(context, tab.tabUrl)) continue
            released += frame.releaseFarPages()
        }
    } catch (_: Exception) {
    }
    Log.i("OpumMemory", "trim level $level: $step, released $released")
}
