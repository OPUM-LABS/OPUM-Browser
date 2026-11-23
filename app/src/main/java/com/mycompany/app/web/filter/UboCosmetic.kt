package com.mycompany.app.web.filter

import android.content.Context
import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.mycompany.app.data.book.DataBookAds
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.BrowserSettings
import org.json.JSONArray
import org.json.JSONObject

/**
 * Cosmetic filtering of the uBlock Origin-compatible engine (migration step 4).
 *
 * [attach] registers `assets/filter/ubo-cosmetic.js` as a document-start script (all frames, all origins) and this
 * object as the JS bridge `opumUbo` on a browser WebView — only while the test switch is on and the WebView supports
 * `DOCUMENT_START_SCRIPT`. The script calls [cosmetic] once per frame and [classId] for classes / ids it sees.
 *
 * The bridge answers null (script does nothing) when the engine is not in use, the Ad blocker is off, the page is
 * on the allow list (`DataBookAds`, checked against the tab's top page), or the page is YouTube (kept on
 * `WebClean`'s own handling). When it served a top page, [servedTop] holds that page's host so `WebNestView` can
 * skip the old list-based injection for it.
 *
 * Bridge methods run on WebView's JavaBridge thread, never on the UI thread.
 */
class UboCosmetic private constructor(context: Context, view: WebView) {
    private val app: Context = context.applicationContext ?: context
    private val viewRef = java.lang.ref.WeakReference(view)

    /** Scriptlet code last served for the top frame, for [runBlockedScriptlets]. */
    @Volatile
    private var topScript: Pair<String, String>? = null

    /** Top page of the tab (set from the browser's `shouldInterceptRequest` for main-frame requests). */
    @Volatile
    var topUrl: String? = null

    /** Host of the top page loaded without element hiding ("Show dialog", [ScrollUnlock]), or null. */
    @Volatile
    private var unfilteredHost: String? = null

    /** Host of the top page the script was served for, or null. */
    @Volatile
    var servedTop: String? = null
        private set

    @JavascriptInterface
    fun cosmetic(url: String?, isTop: Boolean): String? {
        if (url.isNullOrEmpty() || !url.startsWith("http")) return null
        if (isTop) {
            topUrl = url
            servedTop = null
        }
        if (!BrowserSettings.adsBlock.value) return null
        // JavaBridge thread: right after the app starts, wait for the engine instead of leaving the page unfiltered
        val engine = UboFilter.awaitEngine(app) ?: return null
        val page = if (isTop) url else (topUrl ?: url)
        val pageHost = MainUtil.hostOfUrl(page, false)
        if (DataBookAds.instance(app).hasSiteOf(page, pageHost)) return null
        // "Show dialog" (ScrollUnlock): this load of the site without element hiding, all its frames
        if (isTop) unfilteredHost = if (ScrollUnlock.consumeShowOnce(pageHost)) pageHost else null
        if (unfilteredHost != null && unfilteredHost == pageHost) return null
        val pageCosmetic = engine.cosmetic(url) ?: return null
        val json = JSONObject()
            .put("hide", JSONArray(pageCosmetic.hideSelectors))
            .put("procedural", JSONArray(pageCosmetic.proceduralActions))
            .put("exceptions", JSONArray(pageCosmetic.exceptions))
            .put("script", pageCosmetic.script)
            .put("generichide", pageCosmetic.genericHide)
            // uBO's :others() rules (OthersRules): hide everything except these, applied by the script
            .put("others", JSONArray(UboFilter.othersFor(url)))
            // the user unlocked scrolling on this site before (ScrollUnlock): the script lifts the lock on every visit
            .put("unlock", isTop && ScrollUnlock.remembered(pageHost))
            .toString()
        if (isTop) {
            servedTop = MainUtil.hostOfUrl(url, false)
            topScript = if (pageCosmetic.script.isNotEmpty()) url to pageCosmetic.script else null
        }
        UboFilter.noteCosmetic(pageCosmetic.hideSelectors.size, pageCosmetic.proceduralActions.size, pageCosmetic.script.isNotEmpty())
        return json
    }

    /**
     * The page's Content-Security-Policy refused the scriptlets (no eval, no inline script). `evaluateJavascript`
     * is not bound by the page's CSP, so run them from here: a little later than document start and only in the top
     * frame, but far better than not at all.
     */
    @JavascriptInterface
    fun runBlockedScriptlets(url: String?) {
        val (forUrl, script) = topScript ?: return
        if (url != forUrl) return
        topScript = null
        val view = viewRef.get() ?: return
        val code = "(function(){if(window.__opumUboScriptletsRan)return;var scriptletGlobals = {};\n" + script +
            "\nwindow.__opumUboScriptletsRan = true;})();"
        UboFilter.noteCspFallback()
        view.post { try { view.evaluateJavascript(code, null) } catch (_: Throwable) {} }
    }

    @JavascriptInterface
    fun classId(classesJson: String?, idsJson: String?, exceptionsJson: String?): String? {
        val engine = UboFilter.engine() ?: return null
        val classes = strings(classesJson)
        val ids = strings(idsJson)
        if (classes.isEmpty() && ids.isEmpty()) return null
        val sel = engine.classIdSelectors(classes, ids, strings(exceptionsJson))
        if (sel.isEmpty()) return null
        UboFilter.noteGeneric(sel.size)
        return JSONArray(sel).toString()
    }

    /** True when the script was served for the top page with host [host] (the old injection can be skipped). */
    fun servedFor(host: String?): Boolean {
        val served = servedTop ?: return false
        return host != null && (served == host || served.endsWith(".$host") || host.endsWith(".$served"))
    }

    companion object {
        private const val TAG = "UboFilter"
        private const val BRIDGE = "opumUbo"

        @Volatile
        private var script: String? = null

        /**
         * Registers the document-start script and the bridge on [view] (once per WebView). Returns the bridge, or
         * null when the switch is off or the WebView cannot run document-start scripts (the old injection stays).
         */
        @JvmStatic
        fun attach(view: WebView?): UboCosmetic? {
            if (view == null) return null
            if (!WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) return null
            val js = loadScript(view.context) ?: return null
            return try {
                val bridge = UboCosmetic(view.context, view)
                view.addJavascriptInterface(bridge, BRIDGE)
                WebViewCompat.addDocumentStartJavaScript(view, js, setOf("*"))
                bridge
            } catch (e: Throwable) {
                Log.w(TAG, "document-start script not registered", e)
                null
            }
        }

        private fun loadScript(context: Context): String? {
            script?.let { return it }
            return try {
                context.assets.open("filter/ubo-cosmetic.js").use { it.readBytes().toString(Charsets.UTF_8) }
                    .also { script = it }
            } catch (_: Exception) {
                null
            }
        }

        private fun strings(json: String?): List<String> {
            if (json.isNullOrEmpty()) return emptyList()
            return try {
                val array = JSONArray(json)
                List(array.length()) { array.optString(it) }.filter { it.isNotEmpty() }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
