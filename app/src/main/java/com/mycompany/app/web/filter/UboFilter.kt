package com.mycompany.app.web.filter

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookFilter
import com.mycompany.app.db.book.DbBookUser
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.BrowserSettings
import java.io.File
import java.security.MessageDigest
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/**
 * The app-wide uBlock Origin–compatible engine.
 *
 * Built in the background from the same sources `WebClean.load()` reads: every enabled list in `DbBookFilter_table`
 * (downloaded copy in `files/.filter/<md5>` or a local file / content URI) plus the enabled user rules
 * (`DbBookUser_table`). `builtin.txt` is skipped: it is in the app's private syntax. The built engine is saved in
 * `noBackupFilesDir/ubo/` (not in backups, not in `.filter` which the Web Filter page tidies) together with a key of
 * the sources (paths, sizes, dates, rule text), so an unchanged start only deserializes it.
 *
 * The browser's only filter engine since the switch-over (`WebClean.classify()` asks [check]). Old engines are closed after the swap; `UboEngine.close()` waits for
 * calls still running.
 */
object UboFilter {
    private const val TAG = "UboFilter"
    private const val USER_RULES = "sb_user_filter_path"

    @Volatile
    private var current: UboEngine? = null
    @Volatile
    private var currentKey: String? = null
    /** Engine with the lists' `$popup` rules (see [PopupRules]); built and cached together with [current]. */
    @Volatile
    private var currentPopup: UboEngine? = null
    /** The lists' `$removeparam` exceptions as plain rules ([KeepParamRules]); built and cached with [current]. */
    @Volatile
    private var currentKeep: UboEngine? = null

    /** uBO's `:others()` rules of the current lists ([OthersRules]; kept out of the engine, applied by the script). */
    @Volatile
    private var currentOthers: List<OthersRules.Rule> = emptyList()

    /** The `:others()` selector lists for a page or frame at [url] (empty when there are none or no engine). */
    @JvmStatic
    fun othersFor(url: String): List<String> = if (current == null) emptyList() else OthersRules.selectorsFor(currentOthers, url)
    private val popupBlocks = java.util.concurrent.atomic.AtomicLong()

    /**
     * Pop-up blocker check for a new window / tab to [url] opened by the page [openerUrl], or null when the engine is
     * not in use (the caller then asks `WebClean`). Blocked when a `$popup` rule matches, when the address itself is
     * blocked as a page (an ad server), or — as the old engine did — when a third-party target looks like an ad host
     * or ad link.
     */
    @JvmStatic
    fun checkPopup(url: String?, openerUrl: String?): Boolean? {
        val main = awaitEngine(null) ?: return null
        if (url.isNullOrEmpty() || !URLUtil.isNetworkUrl(url)) return false
        val opener = openerUrl ?: ""
        val why = when {
            currentPopup?.check(url, opener, "other")?.blocks == true -> "popup rule"
            main.check(url, opener, "document").blocks -> "blocked address"
            else -> {
                val host = MainUtil.hostOfUrl(url, true)
                val openerHost = MainUtil.hostOfUrl(opener, false)
                if (!host.isNullOrEmpty() && !openerHost.isNullOrEmpty() && !MainUtil.sameSite(openerHost, host)) {
                    val start = url.indexOf("://").let { if (it < 0) 0 else url.indexOf('/', it + 3) }
                    val path = if (start < 0) "" else url.substring(start)
                    if (com.mycompany.app.web.WebClean.looksLikeAdHost(host) ||
                        com.mycompany.app.web.WebClean.looksLikeAdPath(path)
                    ) "ad-like address" else null
                } else {
                    null
                }
            }
        } ?: return false
        popupBlocks.incrementAndGet()
        Log.d(TAG, "pop-up blocked ($why): $url")
        return true
    }
    @Volatile
    private var status: String = "not loaded"
    private val building = AtomicBoolean(false)
    private val checks = java.util.concurrent.atomic.AtomicLong()
    private val blocks = java.util.concurrent.atomic.AtomicLong()
    @Volatile
    private var again = false

    /** The engine once it is built (a few hundred ms after start or a list change), else null (requests are allowed). */
    @JvmStatic
    fun engine(): UboEngine? = current

    private val ready = Object()

    /** Longest wait for the first engine of the process ([awaitEngine]). */
    private const val START_WAIT_MS = 5_000L

    /**
     * The engine, waiting for it while the first one of the process is still being loaded (at most [timeoutMs]):
     * right after the app starts, the first page's requests and its cosmetic rules would otherwise pass unfiltered
     * (loading the engine from its cache takes a few seconds on slow devices). Never waits on the main thread, and
     * not when no build is running (no lists, engine stopped). Later rebuilds keep the old engine until the new one
     * is ready, so they never wait.
     */
    @JvmStatic
    @JvmOverloads
    fun awaitEngine(context: Context?, timeoutMs: Long = START_WAIT_MS): UboEngine? {
        current?.let { return it }
        if (android.os.Looper.myLooper() == android.os.Looper.getMainLooper()) return null
        ensure(context)
        val end = SystemClock.elapsedRealtime() + timeoutMs
        synchronized(ready) {
            while (current == null && building.get()) {
                val left = end - SystemClock.elapsedRealtime()
                if (left <= 0) break
                try {
                    ready.wait(left)
                } catch (_: InterruptedException) {
                    break
                }
            }
        }
        return current
    }

    private fun signalReady() {
        synchronized(ready) { ready.notifyAll() }
    }

    private val cosmeticFrames = java.util.concurrent.atomic.AtomicLong()
    private val genericHits = java.util.concurrent.atomic.AtomicLong()

    /** A frame got its cosmetic rules (counted for the log). */
    @JvmStatic
    fun noteCosmetic(hide: Int, procedural: Int, scriptlets: Boolean) {
        cosmeticFrames.incrementAndGet()
        Log.d(TAG, "cosmetic: $hide selectors, $procedural procedural" + if (scriptlets) ", scriptlets" else "")
    }

    private val cspFallbacks = java.util.concurrent.atomic.AtomicLong()

    /** A page's CSP refused the scriptlets; the app ran them with `evaluateJavascript` instead. */
    @JvmStatic
    fun noteCspFallback() {
        cspFallbacks.incrementAndGet()
        Log.d(TAG, "scriptlets blocked by CSP: running them through evaluateJavascript")
    }

    /** Generic class / id selectors matched on a page. */
    @JvmStatic
    fun noteGeneric(count: Int) {
        genericHits.addAndGet(count.toLong())
        Log.d(TAG, "generic: $count selectors")
    }

    /** `$redirect` body of the last blocked request on this thread, picked up by [takeRedirect]. */
    private val lastRedirect = ThreadLocal<String?>()
    private val headerLogs = java.util.concurrent.atomic.AtomicInteger()

    /**
     * Network verdict through the current engine (counted for the log); null when the engine is not in use.
     * A `$redirect` match counts as blocked and leaves its body for [takeRedirect] on the same thread.
     * `$removeparam` rewrites are not applied (WebView cannot change a sub-resource URL): such requests load as is.
     */
    @JvmStatic
    fun check(request: WebResourceRequest?, url: String, pageUrl: String?): Boolean? {
        val activeEngine = engine() ?: return null
        lastRedirect.set(null)
        notePage(pageUrl)
        val types = requestTypes(request, url)
        if (Log.isLoggable(TAG, Log.VERBOSE) && headerLogs.incrementAndGet() <= 40) {
            Log.v(TAG, "types $types main=${request?.isForMainFrame} headers=${request?.requestHeaders?.keys} $url")
        }
        val method = request?.method ?: "GET"
        var verdict: UboEngine.Verdict = UboEngine.Verdict.Allow
        var type = types[0]
        for (t in types) {
            verdict = activeEngine.check(url, pageUrl ?: "", t, method)
            type = t
            if (verdict.blocks) break
        }
        checks.incrementAndGet()
        if (verdict is UboEngine.Verdict.Redirect) lastRedirect.set(verdict.dataUrl)
        if (verdict is UboEngine.Verdict.Rewrite && currentKeep?.check(url, if (type == "document") url else (pageUrl ?: url), "other")?.blocks != true) {
            lastRewrite.set(url to verdict.url)
        }
        val blocked = verdict.blocks
        if (blocked) {
            blocks.incrementAndGet()
            Log.d(TAG, "blocked $type $url" + if (verdict is UboEngine.Verdict.Redirect) " (redirect)" else "")
        }
        return blocked
    }

    /** Page of the browser's latest checked request: service-worker requests are checked against it. */
    @Volatile
    private var lastPageUrl: String? = null
    private val swInstalled = AtomicBoolean(false)

    @JvmStatic
    fun notePage(pageUrl: String?) {
        if (!pageUrl.isNullOrEmpty()) lastPageUrl = pageUrl
    }

    /**
     * Service-worker requests never reach `shouldInterceptRequest` of a WebView. While the engine is in use they are
     * checked here, with the browser's current page as their source; the page's allow-list entry and the global
     * Ad blocker switch apply as for normal requests. Installed once per process.
     */
    @JvmStatic
    fun installServiceWorkerClient(context: Context?) {
        if (context == null || !swInstalled.compareAndSet(false, true)) return
        val app = context.applicationContext ?: context
        try {
            android.webkit.ServiceWorkerController.getInstance().setServiceWorkerClient(
                object : android.webkit.ServiceWorkerClient() {
                    override fun shouldInterceptRequest(request: WebResourceRequest): android.webkit.WebResourceResponse? {
                        if (!BrowserSettings.adsBlock.value || awaitEngine(app) == null) return null
                        val url = request.url?.toString() ?: return null
                        if (!URLUtil.isNetworkUrl(url)) return null
                        val page = lastPageUrl ?: url
                        val pageHost = MainUtil.hostOfUrl(page, false)
                        if (com.mycompany.app.data.book.DataBookAds.instance(app).hasSiteOf(page, pageHost)) return null
                        clearRedirect()
                        if (check(request, url, page) != true) return null
                        return takeRedirect() ?: android.webkit.WebResourceResponse(
                            "text/plain", "UTF-8", java.io.ByteArrayInputStream(ByteArray(0)),
                        )
                    }
                },
            )
        } catch (e: Throwable) {
            swInstalled.set(false)
            Log.w(TAG, "service worker client not installed", e)
        }
    }

    /** Forgets a redirect / rewrite left on this thread (called before each request is classified). */
    @JvmStatic
    fun clearRedirect() {
        lastRedirect.set(null)
        lastRewrite.set(null)
    }

    /** `$removeparam` result of the request [check] just looked at on this thread: (original URL, cleaned URL). */
    private val lastRewrite = ThreadLocal<Pair<String, String>?>()

    /** Original URLs rewritten in the last seconds, so a server that adds the parameter back cannot cause a loop. */
    private val recentRewrites = java.util.concurrent.ConcurrentHashMap<String, Long>()
    private val rewrites = java.util.concurrent.atomic.AtomicLong()

    /**
     * Tracking-parameter stripping for page loads: the URL without the parameters the lists' `$removeparam` rules
     * remove (AdGuard's Tracking URL blocker, uBO Privacy, …), when [check] just found one for [url] on this thread;
     * else null. Only http(s), and the same URL is not rewritten twice within 10 seconds.
     */
    @JvmStatic
    fun takeRewrite(url: String?): String? {
        val rewrite = lastRewrite.get() ?: return null
        lastRewrite.set(null)
        if (url == null || rewrite.first != url || rewrite.second == url || !URLUtil.isNetworkUrl(rewrite.second)) return null
        val now = SystemClock.elapsedRealtime()
        if (recentRewrites.size > 64) recentRewrites.entries.removeIf { now - it.value > 10_000 }
        val last = recentRewrites[url]
        if (last != null && now - last < 10_000) return null
        recentRewrites[url] = now
        rewrites.incrementAndGet()
        Log.d(TAG, "tracking parameters removed: $url -> ${rewrite.second}")
        PageStats.noteCleaned(url, rewrite.second)
        return rewrite.second
    }

    /**
     * Tracking-parameter stripping for link clicks and redirects (`shouldOverrideUrlLoading`): the clean URL to load
     * instead of [request]'s, or null. Not for allow-listed sites or with the Ad blocker off, only main-frame
     * http(s) GET navigations.
     */
    @JvmStatic
    fun cleanNavigation(context: Context?, request: WebResourceRequest?): String? {
        if (context == null || request == null || !request.isForMainFrame || !BrowserSettings.adsBlock.value) return null
        if (!"GET".equals(request.method ?: "GET", ignoreCase = true)) return null
        return cleanAddress(context, request.url?.toString())
    }

    /**
     * [url] without tracking parameters for a page load the app starts itself (typed address, bookmark, link from
     * another app: `WebNestView.openUrl`), or null when nothing is removed. Same conditions as [cleanNavigation].
     */
    @JvmStatic
    fun cleanAddress(context: Context?, url: String?): String? {
        if (context == null || url == null || !BrowserSettings.adsBlock.value) return null
        if (!URLUtil.isNetworkUrl(url) || !url.contains('?')) return null
        if (com.mycompany.app.data.book.DataBookAds.instance(context).hasSiteOf(url, MainUtil.hostOfUrl(url, false))) return null
        clearRedirect()
        check(null, url, url) ?: return null
        return takeRewrite(url)
    }

    /** A page that replaces itself with [cleanUrl] (no history entry left behind, no Referer with the old URL). */
    @JvmStatic
    fun rewriteResponse(cleanUrl: String): android.webkit.WebResourceResponse {
        val target = org.json.JSONObject.quote(cleanUrl).replace("</", "<\\/")
        val html = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><meta name=\"referrer\" content=\"no-referrer\">" +
            // same origin (only the query changes): give this history entry the clean address and reload it — no
            // extra history entry, and the old address does not travel on as Referer
            "<script>try{history.replaceState(null,'',$target);location.reload();}catch(e){location.replace($target);}</script>" +
            "</head><body></body></html>"
        // the Referrer-Policy header / meta tag are not honoured by WebView for this response; replaceState above is
        return android.webkit.WebResourceResponse(
            "text/html", "UTF-8", 200, "OK",
            mapOf("Referrer-Policy" to "no-referrer", "Cache-Control" to "no-store"),
            java.io.ByteArrayInputStream(html.toByteArray()),
        )
    }

    /** The `$redirect` replacement for the request [check] just blocked on this thread, or null. */
    @JvmStatic
    fun takeRedirect(): android.webkit.WebResourceResponse? {
        val dataUrl = lastRedirect.get() ?: return null
        lastRedirect.set(null)
        val (mime, bytes) = decodeDataUrl(dataUrl) ?: return null
        val textual = mime.startsWith("text/") || mime.contains("javascript") || mime.contains("json")
        return android.webkit.WebResourceResponse(mime, if (textual) "UTF-8" else null, java.io.ByteArrayInputStream(bytes))
    }

    /** `data:<mime>[;base64],<data>` → mime type and bytes; null when it is not a data URL. */
    @JvmStatic
    fun decodeDataUrl(dataUrl: String): Pair<String, ByteArray>? {
        if (!dataUrl.startsWith("data:")) return null
        val comma = dataUrl.indexOf(',')
        if (comma < 0) return null
        val meta = dataUrl.substring(5, comma)
        val data = dataUrl.substring(comma + 1)
        val base64 = meta.endsWith(";base64")
        val mime = meta.removeSuffix(";base64").substringBefore(';').ifEmpty { "text/plain" }
        val bytes = try {
            if (base64) java.util.Base64.getDecoder().decode(data)
            else java.net.URLDecoder.decode(data, "UTF-8").toByteArray()
        } catch (_: Exception) {
            return null
        }
        return mime to bytes
    }

    /** Cheap (called per request): starts the first build when the switch is on and no engine exists yet. */
    @JvmStatic
    fun ensure(context: Context?) {
        if (context == null || current != null || building.get()) return
        rebuild(context.applicationContext ?: context)
    }

    /** Called after `WebClean.load()` read the lists again (a list was added, updated, turned on / off). */
    @JvmStatic
    fun onListsChanged(context: Context?) {
        if (context == null) return
        rebuild(context.applicationContext ?: context)
    }

    private fun rebuild(context: Context) {
        if (!building.compareAndSet(false, true)) {
            again = true
            return
        }
        // the first engine of the process is awaited by page loads (awaitEngine): load it at normal priority
        val first = current == null
        val thread = Thread({
            android.os.Process.setThreadPriority(
                if (first) android.os.Process.THREAD_PRIORITY_DEFAULT else android.os.Process.THREAD_PRIORITY_BACKGROUND,
            )
            try {
                do {
                    again = false
                    build(context)
                } while (again)
            } catch (e: Throwable) {
                status = "failed: " + e.javaClass.simpleName
                Log.w(TAG, "build failed", e)
            } finally {
                building.set(false)
                signalReady()
            }
        }, "UboFilter")
        thread.start()
    }

    private fun build(context: Context) {
        if (!UboEngine.loadLibrary()) {
            status = "library missing"
            return
        }
        val keyStart = SystemClock.elapsedRealtime()
        val sources = sources(context)
        val userRules = if (userRulesOn(context)) userRules(context) else ""
        val fixes = readAsset(context, FIXES_ASSET) ?: ""
        val key = key(sources, userRules + "\u0003" + fixes.hashCode() + "\u0003popup1-keep1")
        if (key == currentKey && current != null) return

        val dir = File(context.noBackupFilesDir, "ubo").apply { mkdirs() }
        val dataFile = File(dir, "engine.dat")
        val popupFile = File(dir, "popup.dat")
        val keepFile = File(dir, "keep.dat")
        val othersFile = File(dir, "others.txt")
        var othersLines: List<String>? = null
        val keyFile = File(dir, "engine.key")
        val start = SystemClock.elapsedRealtime()

        var engine: UboEngine? = null
        var popupEngine: UboEngine? = null
        var keepEngine: UboEngine? = null
        var how = "parsed"
        val keyMs = start - keyStart
        var readMs = 0L
        if (dataFile.isFile && keyFile.isFile && runCatching { keyFile.readText() }.getOrNull() == key) {
            val bytes = runCatching { dataFile.readBytes() }.getOrNull()
            readMs = SystemClock.elapsedRealtime() - start
            engine = bytes?.let { runCatching { UboEngine.deserialize(it) }.getOrNull() }
            popupEngine = runCatching { UboEngine.deserialize(popupFile.readBytes()) }.getOrNull()
            keepEngine = runCatching { UboEngine.deserialize(keepFile.readBytes()) }.getOrNull()
            othersLines = runCatching { othersFile.readLines() }.getOrNull()
            if (engine != null && (popupEngine == null || keepEngine == null || othersLines == null)) {
                engine.close()
                popupEngine?.close()
                keepEngine?.close()
                engine = null // the files belong together: parse again
                popupEngine = null
                keepEngine = null
            }
            if (engine != null) how = "cached"
        }
        var popupRules = 0
        var rules = 0
        if (engine == null) {
            val texts = ArrayList<String>(sources.size + 1)
            val trusted = ArrayList<Boolean>(sources.size + 1)
            val popupTexts = ArrayList<String>()
            val keepTexts = ArrayList<String>()
            val others = ArrayList<String>()
            // $popup rules go to a small second engine for the pop-up blocker (PopupRules).
            fun add(input: String, isTrusted: Boolean) {
                // :others() rules are applied by the cosmetic script (OthersRules), adblock-rust would invert them
                val evaluated = FilterListPreprocessor.directives(input)
                others.addAll(OthersRules.extractLines(evaluated))
                val text = FilterListPreprocessor.dropUnsupported(evaluated)
                val split = PopupRules.split(text)
                texts.add(split.main)
                trusted.add(isTrusted)
                if (split.popupRules > 0) popupTexts.add(split.popup)
                popupRules += split.popupRules
                // $removeparam exceptions, which adblock-rust ignores (KeepParamRules)
                val keep = KeepParamRules.extract(split.main)
                if (keep.second > 0) keepTexts.add(keep.first)
            }
            for (source in sources) {
                // Lists downloaded before step 6 may still contain uBO's !#if blocks: evaluate them here too.
                val text = readText(context, source.path) ?: continue
                rules += text.count { it == '\n' } + 1
                add(text, source.trusted)
            }
            if (fixes.isNotEmpty()) add(fixes, false) // the app's own site fixes (converted builtin.txt), untrusted
            // like uBO's default: the user's own rules may not use trusted scriptlets
            if (userRules.isNotEmpty()) add(userRules, false)
            engine = UboEngine.create(texts, trusted) ?: run {
                status = "build failed"
                return
            }
            popupEngine = UboEngine.create(popupTexts)
            keepEngine = UboEngine.create(keepTexts)
            othersLines = others
            val data = engine.serialize()
            val popupData = popupEngine?.serialize()
            val keepData = keepEngine?.serialize()
            if (data != null && popupData != null && keepData != null) {
                runCatching {
                    keyFile.delete()
                    File(dir, "engine.dat.tmp").apply { writeBytes(data) }.renameTo(dataFile)
                    File(dir, "popup.dat.tmp").apply { writeBytes(popupData) }.renameTo(popupFile)
                    File(dir, "keep.dat.tmp").apply { writeBytes(keepData) }.renameTo(keepFile)
                    File(dir, "others.txt.tmp").apply { writeText(others.joinToString("\n")) }.renameTo(othersFile)
                    keyFile.writeText(key)
                }
            }
        }
        // Scriptlets and redirect resources are not part of the serialized engine: load them every time.
        val resourcesJson = mergedResources(context)
        val resources = if (resourcesJson != null) engine.useResources(resourcesJson) else -1
        val old = current
        val oldPopup = currentPopup
        val oldKeep = currentKeep
        current = engine
        signalReady()
        currentPopup = popupEngine
        currentKeep = keepEngine
        currentOthers = OthersRules.parse(othersLines ?: emptyList())
        currentKey = key
        old?.close()
        oldPopup?.close()
        oldKeep?.close()
        val ms = SystemClock.elapsedRealtime() - start
        status = String.format(
            Locale.US, "%s, %d lists%s%s, %d ms (sources %d ms, read %d ms)%s", how, sources.size,
            if (rules > 0) ", $rules lines" else "", if (popupRules > 0) ", $popupRules pop-up rules" else "",
            ms, keyMs, readMs, ", $resources uBO resources",
        )
        Log.i(TAG, status)
    }

    /** Closes the engine (the app's web filter was stopped for good). */
    @JvmStatic
    fun release() {
        val old = current
        currentPopup?.close()
        currentPopup = null
        currentKeep?.close()
        currentKeep = null
        currentOthers = emptyList()
        current = null
        currentKey = null
        status = "not loaded"
        old?.close()
    }

    // -----------------------------------------------------------------------------------------------------------

    private class Source(val path: String, val stamp: String, val trusted: Boolean)

    /** Built-in uBlock Origin scriptlets and redirect resources (`tools/ubo-resources/build.py`, GPLv3). */
    const val RESOURCES_ASSET = "filter/ubo-resources.json"
    const val RESOURCES_META_ASSET = "filter/ubo-resources.meta.json"
    /** Brave's own scriptlets and redirect resources (brave/adblock-resources dist/resources.json, MPL-2.0). */
    const val BRAVE_RESOURCES_ASSET = "filter/brave-resources.json"

    /**
     * uBlock Origin's and Brave's resources as one JSON array: adblock-rust's `use_resources` replaces the whole set,
     * and the two sets share no names.
     */
    private fun mergedResources(context: Context): String? {
        val parts = listOfNotNull(readAsset(context, RESOURCES_ASSET), readAsset(context, BRAVE_RESOURCES_ASSET))
            .map { it.trim().removePrefix("[").removeSuffix("]").trim() }
            .filter { it.isNotEmpty() }
        return if (parts.isEmpty()) null else parts.joinToString(",", "[", "]")
    }

    /** The app's own site fixes in uBO syntax (`tools/ubo-resources/convert_builtin.py` from `builtin.txt`). */
    const val FIXES_ASSET = "filter/opum-fixes.txt"

    /**
     * uBlock Origin's own lists (uAssets) and Brave's own lists may use scriptlets marked "requires trust", as in uBO
     * and Brave themselves. Every other list and the user's own rules may not.
     */
    @JvmStatic
    fun isTrustedListUrl(url: String?): Boolean {
        if (url.isNullOrEmpty()) return false
        val lowerUrl = url.lowercase(Locale.US)
        return lowerUrl.startsWith("https://raw.githubusercontent.com/ublockorigin/uassets/") ||
            lowerUrl.startsWith("https://ublockorigin.github.io/uassets/") ||
            lowerUrl.startsWith("https://ublockorigin.pages.dev/") ||
            lowerUrl.startsWith("https://cdn.jsdelivr.net/gh/ublockorigin/uassets") ||
            lowerUrl.startsWith("https://cdn.statically.io/gh/ublockorigin/uassets/") ||
            lowerUrl.startsWith("https://raw.githubusercontent.com/brave/adblock-lists/") // Brave's own lists, as in Brave
    }

    /** Statistics of one list for the Ad filter screen. */
    class ListInfo(val rules: Int, val rejected: Int, val rejectedSamples: List<String>, val expiresHours: Int?)

    private val listInfos = java.util.concurrent.ConcurrentHashMap<String, Pair<String, ListInfo>>()

    /**
     * Rule counts and `! Expires:` of the list stored for [path] (URL, local file or content URI; the user rules for
     * `sb_user_filter_path`). Slow (parses the list): call it off the main thread. Cached per file version.
     */
    @JvmStatic
    fun listInfo(context: Context, path: String?): ListInfo? {
        if (path.isNullOrEmpty()) return null
        val text: String
        val stamp: String
        if (path == USER_RULES) {
            text = userRules(context)
            stamp = "user:" + text.hashCode()
        } else {
            val local = (if (URLUtil.isNetworkUrl(path)) MainUtil.filterFilePath(context, path) else path) ?: return null
            val file = File(local)
            stamp = if (file.isFile) "${file.length()}@${file.lastModified()}" else local
            listInfos[path]?.let { if (it.first == stamp) return it.second }
            text = readText(context, local) ?: return null
        }
        listInfos[path]?.let { if (it.first == stamp) return it.second }
        // $popup rules are used by the pop-up blocker (PopupRules): count them as rules, not as rejected
        val split = PopupRules.split(FilterListPreprocessor.evaluate(text))
        val stats = UboEngine.listStats(split.main) ?: return null
        val popupStats = if (split.popupRules > 0) UboEngine.listStats(split.popup, 0) else null
        val info = ListInfo(
            stats.rules + (popupStats?.rules ?: 0), stats.rejected + (popupStats?.rejected ?: 0),
            stats.rejectedSamples, UboEngine.listMetadata(text)?.expiresHours,
        )
        listInfos[path] = stamp to info
        return info
    }


    private fun readAsset(context: Context, name: String): String? = try {
        context.assets.open(name).use { it.readBytes().toString(Charsets.UTF_8) }
    } catch (_: Exception) {
        null
    }

    /** Enabled lists in the order `WebClean.load()` reads them. */
    private fun sources(context: Context): List<Source> {
        val out = ArrayList<Source>()
        val cursor = DbUtil.query(
            DbBookFilter.get(context).getWritableDatabase(), "DbBookFilter_table", arrayOf<String?>("_path"),
            "_use=?", arrayOf<String?>("1"), null,
        ) ?: return out
        try {
            val col = cursor.getColumnIndex("_path")
            if (cursor.moveToFirst()) do {
                val path = cursor.getString(col)
                if (TextUtils.isEmpty(path) || path == USER_RULES) continue
                val local = if (URLUtil.isNetworkUrl(path)) MainUtil.filterFilePath(context, path) else path
                if (TextUtils.isEmpty(local)) continue
                val file = File(local!!)
                val stamp = if (file.isFile) "${file.length()}@${file.lastModified()}" else if (file.isAbsolute) continue else ""
                out.add(Source(local, stamp, isTrustedListUrl(path)))
            } while (cursor.moveToNext())
        } catch (_: Exception) {
        } finally {
            cursor.close()
        }
        return out
    }

    private val COSMETIC_SEP = Regex("#@?[?$%]?#")

    /**
     * A user rule as the engine needs it. The app's element picker writes the page's host with its port
     * (`10.0.2.2:8773##div...`); uBO syntax has no ports, so they are dropped from the cosmetic rule's host list.
     */
    @JvmStatic
    fun engineRule(rule: String): String {
        val separator = COSMETIC_SEP.find(rule) ?: return rule
        if (separator.range.first == 0) return rule
        val hosts = rule.substring(0, separator.range.first)
        if (!hosts.contains(':')) return rule
        val fixed = hosts.split(',').joinToString(",") { h -> h.replace(Regex(":\\d+$"), "") }
        return fixed + rule.substring(separator.range.first)
    }

    /** The "User filter" row of the list (`sb_user_filter_path`) is turned on. */
    private fun userRulesOn(context: Context): Boolean {
        val cursor = DbUtil.query(
            DbBookFilter.get(context).getWritableDatabase(), "DbBookFilter_table", arrayOf<String?>("_path"),
            "_path=? AND _use=?", arrayOf<String?>(USER_RULES, "1"), null,
        ) ?: return false
        return try { cursor.moveToFirst() } catch (_: Exception) { false } finally { cursor.close() }
    }

    /** Enabled user rules, one per line. */
    private fun userRules(context: Context): String {
        val sb = StringBuilder()
        val cursor = DbUtil.query(
            DbBookUser.get(context).getWritableDatabase(), "DbBookUser_table", arrayOf<String?>("_path"),
            "_use=?", arrayOf<String?>("1"), null,
        ) ?: return ""
        try {
            val col = cursor.getColumnIndex("_path")
            if (cursor.moveToFirst()) do {
                val rule = cursor.getString(col)
                if (!TextUtils.isEmpty(rule)) sb.append(engineRule(rule.trim())).append('\n')
            } while (cursor.moveToNext())
        } catch (_: Exception) {
        } finally {
            cursor.close()
        }
        return sb.toString()
    }

    private fun key(sources: List<Source>, userRules: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update((UboEngine.version() ?: "").toByteArray())
        md.update("pp3".toByteArray()) // lists are pre-processed (FilterListPreprocessor) before parsing; pp3: :others() rules kept apart (others.txt)
        for (s in sources) md.update("\u0000${s.path}\u0001${s.stamp}\u0001${s.trusted}".toByteArray())
        md.update("\u0002".toByteArray())
        md.update(userRules.toByteArray())
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    private fun readText(context: Context, path: String): String? = try {
        MainUtil.openUriInput(context, path)?.use { it.readBytes().toString(Charsets.UTF_8) }
    } catch (_: Throwable) {
        null
    }

    // -----------------------------------------------------------------------------------------------------------

    /**
     * uBO request type(s) for a WebView request. Android WebView (checked on WebView 156) passes neither
     * `Sec-Fetch-Dest` nor `X-Requested-With` to `shouldInterceptRequest`, but `Accept` is specific for documents and
     * frames (`text/html`), images (`image/...`) and CSS (`text/css`). Scripts, XHR and fetch all send the "any type" Accept value:
     * then the extension decides when it is clear, else both `script` and `xmlhttprequest` are returned and
     * [check] blocks when either matches. Main frame → `document`. `Sec-Fetch-Dest` is still used when present.
     */
    @JvmStatic
    fun requestTypes(request: WebResourceRequest?, url: String): List<String> =
        // no request: a page load the app starts itself (cleanAddress)
        requestTypes(request == null || request.isForMainFrame, request?.requestHeaders, url)

    @JvmStatic
    fun requestTypes(mainFrame: Boolean, headers: Map<String, String>?, url: String): List<String> {
        if (mainFrame) return DOCUMENT
        fun header(name: String): String? =
            headers?.entries?.firstOrNull { it.key.equals(name, ignoreCase = true) }?.value
        when (header("Sec-Fetch-Dest")) {
            "script" -> return SCRIPT
            "style" -> return listOf("stylesheet")
            "image" -> return IMAGE
            "font" -> return listOf("font")
            "iframe", "frame" -> return SUBDOCUMENT
            "video", "audio", "track" -> return listOf("media")
            "empty" -> return listOf("xmlhttprequest")
        }
        val accept = header("Accept")?.lowercase(Locale.US)
        if (accept != null) {
            if (accept.startsWith("image/")) return IMAGE
            if (accept.startsWith("text/css")) return listOf("stylesheet")
            if (accept.startsWith("text/html") || accept.startsWith("application/xhtml")) return SUBDOCUMENT
            if (accept.startsWith("application/json") || accept.startsWith("text/event-stream")) {
                return listOf("xmlhttprequest")
            }
        }
        val path = url.substringBefore('?').substringBefore('#').lowercase(Locale.US)
        val ext = path.substringAfterLast('/', "").substringAfterLast('.', "")
        return when (ext) {
            "js", "mjs" -> SCRIPT
            "css" -> listOf("stylesheet")
            "png", "jpg", "jpeg", "gif", "webp", "avif", "svg", "ico", "bmp" -> IMAGE
            "woff", "woff2", "ttf", "otf", "eot" -> listOf("font")
            "mp4", "webm", "m3u8", "mp3", "m4a", "ogg", "mpd", "m4s" -> listOf("media")
            "htm", "html" -> SUBDOCUMENT
            "json" -> listOf("xmlhttprequest")
            else -> SCRIPT_OR_XHR
        }
    }

    private val DOCUMENT = listOf("document")
    private val SUBDOCUMENT = listOf("subdocument")
    private val SCRIPT = listOf("script")
    private val IMAGE = listOf("image")
    private val SCRIPT_OR_XHR = listOf("script", "xmlhttprequest")
}
