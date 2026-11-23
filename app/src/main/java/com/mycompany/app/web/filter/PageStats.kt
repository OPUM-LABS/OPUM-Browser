package com.mycompany.app.web.filter

import android.content.Context
import android.net.Uri
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.AlbumSettings
import java.util.WeakHashMap
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * What the web filter did on the page each WebView shows, for the Web Filter dialog (`DialogSetAdblock`), like the
 * popup of the uBlock Origin extension: requests checked / blocked per domain and tracking parameters removed. A page's
 * numbers start with its main-frame request (`WebClean.intercept`); requests of later in-page navigations add to them.
 * Also keeps the number of requests blocked since install (`AlbumSettings.uboBlockedTotal`, saved now and then).
 * Only what passes the filter is counted: nothing while the Ad blocker is off or the site is allowed.
 */
object PageStats {
    class Domain(val host: String, val requests: Int, val blocked: Int)

    /** A copy of one page's numbers. */
    class Snapshot(
        val pageUrl: String?,
        val requests: Int,
        val blocked: Int,
        val paramsRemoved: Int,
        /** Domains by blocked count, then request count. */
        val domains: List<Domain>,
    )

    private class Counter {
        var requests = 0
        var blocked = 0
    }

    private class Page(val url: String?) {
        var requests = 0
        var blocked = 0
        var paramsRemoved = 0
        val domains = LinkedHashMap<String, Counter>()
    }

    private val pages = WeakHashMap<Any, Page>()

    /** Clean URL → parameters removed, for the page load that follows a stripped address. */
    private val pendingParams = ConcurrentHashMap<String, Int>()

    private val total = AtomicLong(-1L)
    private val unsaved = AtomicLong()

    /** A main-frame request for [url] in [view]: the page's numbers start again (counting [url] itself). */
    @JvmStatic
    fun startPage(view: Any?, url: String?) {
        if (view == null) return
        val page = Page(url)
        if (url != null) page.paramsRemoved = pendingParams.remove(url) ?: 0
        if (pendingParams.size > 32) pendingParams.clear()
        synchronized(pages) { pages[view] = page }
    }

    /** One request of [view]'s page, [blocked] or not. */
    @JvmStatic
    fun note(context: Context?, view: Any?, url: String?, blocked: Boolean) {
        if (view == null || url == null) return
        val host = MainUtil.hostOfUrl(url, false)
        if (host.isNullOrEmpty()) return
        synchronized(pages) {
            val page = pages[view] ?: Page(null).also { pages[view] = it }
            page.requests++
            val domain = page.domains.getOrPut(host) { Counter() }
            domain.requests++
            if (blocked) {
                page.blocked++
                domain.blocked++
            }
        }
        if (blocked) addTotal(context)
    }

    /** [original] was replaced by [clean] (tracking parameters removed): counted for the page that loads [clean]. */
    @JvmStatic
    fun noteCleaned(original: String?, clean: String?) {
        if (original == null || clean == null) return
        val removed = paramCount(original) - paramCount(clean)
        if (removed > 0) pendingParams[clean] = removed
    }

    /** Query parameters in [url]. */
    @JvmStatic
    fun paramCount(url: String): Int = try {
        Uri.parse(url).queryParameterNames.size
    } catch (_: Exception) {
        0
    }

    /** [view]'s page numbers, or null when nothing was counted for it. */
    @JvmStatic
    fun snapshot(view: Any?): Snapshot? {
        if (view == null) return null
        synchronized(pages) {
            val page = pages[view] ?: return null
            val domains = page.domains.map { Domain(it.key, it.value.requests, it.value.blocked) }
                .sortedWith(compareByDescending<Domain> { it.blocked }.thenByDescending { it.requests })
            return Snapshot(page.url, page.requests, page.blocked, page.paramsRemoved, domains)
        }
    }

    /** Requests blocked since install. */
    @JvmStatic
    fun totalBlocked(context: Context?): Long {
        loadTotal()
        save(context)
        return total.get().coerceAtLeast(0L)
    }

    private fun loadTotal() {
        if (total.get() < 0L) total.compareAndSet(-1L, AlbumSettings.uboBlockedTotal.value)
    }

    private fun addTotal(context: Context?) {
        loadTotal()
        total.incrementAndGet()
        if (unsaved.incrementAndGet() >= 50) save(context)
    }

    private fun save(context: Context?) {
        if (unsaved.getAndSet(0) == 0L) return
        val app = context?.applicationContext
        // settings are written on the main thread (requests are counted on WebView's network threads)
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            AlbumSettings.edit(app) { set(AlbumSettings.uboBlockedTotal, total.get().coerceAtLeast(0L)) }
        }
    }
}
