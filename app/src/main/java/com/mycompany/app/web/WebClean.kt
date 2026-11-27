package com.mycompany.app.web

import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilter
import com.mycompany.app.pref.PrefPdf.Companion.get
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.main.MainUtil.filterFilePath
import com.mycompany.app.main.MainUtil.urlDecode
import com.mycompany.app.main.MainUtil.hasChar
import com.mycompany.app.main.MainUtil.openHeadConnection
import com.mycompany.app.main.MainUtil.userAgentPref
import com.mycompany.app.main.MainUtil.nativeJsN
import com.mycompany.app.main.MainUtil.githubRawToRepo
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.main.MainUtil.stripHostPrefix
import com.mycompany.app.main.MainUtil.trimTrailingSlash
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.data.book.DataBookLink.Companion.instance
import com.mycompany.app.main.MainUtil.hostOfUrl
import com.mycompany.app.data.DataUrl.Companion.instance
import com.mycompany.app.main.MainUtil.normalizeUrl
import com.mycompany.app.data.book.DataBookOver.Companion.instance
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.data.book.DataBookAds.Companion.instance
import com.mycompany.app.data.book.DataBookJava.Companion.instance
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.db.book.DbBookTab.Companion.get
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.db.book.DbTabThumb.Companion.get
import com.mycompany.app.db.book.DbBookIcon.Companion.get
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.data.book.DataBookPop.Companion.instance
import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.data.DataTrans.Companion.instance
import com.mycompany.app.data.DataNews.Companion.instance
import com.mycompany.app.data.book.DataBookRecent.Companion.instance
import com.mycompany.app.db.book.DbBookPms.Companion.get
import com.mycompany.app.db.book.DbBookPass.Companion.get
import com.mycompany.app.pref.PrefSync.Companion.get
import com.mycompany.app.pref.PrefWeb.Companion.get
import com.mycompany.app.db.book.DbBookAds.Companion.get
import com.mycompany.app.db.book.DbBookOver.Companion.get
import com.mycompany.app.db.book.DbBookPop.Companion.get
import com.mycompany.app.db.book.DbBookLink.Companion.get
import com.mycompany.app.db.book.DbBookJava.Companion.get
import com.mycompany.app.data.book.DataBookTmem.Companion.instance
import com.mycompany.app.db.book.DbBookTmem.Companion.get
import com.mycompany.app.data.book.DataBookTrans.Companion.instance
import com.mycompany.app.db.book.DbBookTrans.Companion.get
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.data.DataAlbum.Companion.instance
import com.mycompany.app.data.DataPdf.Companion.instance
import com.mycompany.app.data.DataCmp.Companion.instance
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.web.WebClean.WebCleanListener
import com.mycompany.app.web.WebClean
import com.mycompany.app.dialog.DialogSetFilter
import android.text.TextUtils
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainApp
import com.mycompany.app.db.book.DbAdsCmd
import android.webkit.WebView
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import com.mycompany.app.data.book.DataBookLink
import com.mycompany.app.main.MainFilterDown
import org.jsoup.Jsoup
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookFilter
import android.content.Context
import android.database.Cursor
import android.webkit.URLUtil
import androidx.core.app.NotificationCompat
import com.mycompany.app.db.book.DbBookBlock
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.lang.Exception
import java.lang.StringBuilder
import java.net.HttpURLConnection
import java.util.ArrayList
import java.util.HashMap
import java.util.Locale

/**
 * The browser's web filter front-end. Since the uBlock Origin migration the filtering
 * itself is done by the uBlock Origin-compatible engine (`web/filter/UboFilter`, adblock-rust): network requests,
 * cosmetic filtering, scriptlets and pop-ups. What stays here:
 *  - start-up ([UserBlockCopyTask]): first-run default lists, the user's blocked images, the filter lists' local files
 *    ([load]: renamed list addresses, missing files downloaded, then the engine is rebuilt),
 *  - [intercept] for the WebViews: blocked links, the user's blocked images ("Block area" / blocked images),
 *    the engine's verdict, the "Ads Blocked" page for blocked main frames, `$redirect` resources,
 *  - the user's blocked images ([addUserBlock], [removeUserBlock], [isUserBlockedUrl], [isUserBlockedSrc]),
 *  - static helpers used by the browser (blocked-page HTML, site-fix responses, image removal script, ...).
 * The old Adblock Plus-style rule engine (≈6,000 lines: list parsing, matching, page scan, learned commands in
 * `DbAdsCmd`) was removed on 2026-11-19; `DbAdsCmd.db` stays only so old backups still restore.
 */
@Suppress("deprecation")
class WebClean {
    @JvmField
    var userBlocks: HashMap<String?, MutableList<String?>>? = null
    @JvmField
    var userBlocksCopy: HashMap<String?, MutableList<String?>>? = null
    @JvmField
    var listener: WebCleanListener? = null
    /** Start-up ran ([UserBlockCopyTask]). */
    @JvmField
    var started: Boolean = false
    /** True while no engine filters (kept for the browser's old checks; the engine decides per request). */
    @JvmField
    var loadStarted: Boolean = false
    /** Korean app locale: the first-run default lists include the Korean set. */
    @JvmField
    var fullLists: Boolean = false
    /** The "loading" notification builder (cleared by the browser). Not used any more; kept for the browser. */

    internal inner class UserBlockCopyTask(@JvmField val outer: WebClean,
                                         @JvmField val context: Context?
    ) : Runnable {

        /**
         * Ad blocker start-up (background): on first run registers the default filter lists for the
         * user's language, loads the blocked-image map (page path -> image URLs), starts the filter
         * engine, and migrates disabled lists whose download URL has changed.
         */
        override fun run() {
            val context = this.context
            val clean = this.outer
            if (PdfSettings.filterInit.value && context != null) {
                seedDefaultFilters(context, clean.fullLists)
            }
            clean.userBlocks = loadBlockedImages(context)
            clean.userBlocksCopy = clean.userBlockCopy()
            clean.load(context, false, true)
            migrateDisabledFilters(context)
        }

            /** Registers the default filter lists once ("mFilterInit") and records which were added.  */
            private fun seedDefaultFilters(context: Context?, fullSet: Boolean) {
                // Since the switch-over to the uBlock Origin-compatible engine new
                // installs start with uBlock Origin's default lists instead of EasyList + AdGuard Mobile; the
                // language list and the Korean set below stay.
                var filters: StringBuilder? = null
                var adguard: StringBuilder? = null
                DialogSetFilter.addUboDefaults(context)
                // cookie banners blocked on new installs (2026-11-23): the lists behind "Block cookie banners"
                if (PdfSettings.cookieSeed.value) for (list in DialogSetFilter.cookieLists()) addFilter(context, list[1], list[0])
                AlbumSettings.edit(context) { set(AlbumSettings.uboAdd, DialogSetFilter.uboDefaultSelection()) }
                if (fullSet) {
                    filters = addFilters(42, context, addFilters(43, context, filters))
                } else {
                    val regional: Int = regionalFilter()
                    val url = DialogSetFilter.adguardUrl(regional)
                    if (!TextUtils.isEmpty(url)) {
                        addFilter(context, url, DialogSetFilter.adguardFilters[regional]!![0])
                        if (adguard == null) {
                            adguard = StringBuilder()
                        } else {
                            adguard.append("/")
                        }
                        adguard.append(regional)
                    }
                }
                val pref = PrefPdf.get(context, false)
                PdfSettings.editLocal { set(PdfSettings.filterInit, false) }
                pref!!.putBool("mFilterInit", false)
                if (PdfSettings.filterTime.value == 0L) {
                    PdfSettings.editLocal { set(PdfSettings.filterTime, System.currentTimeMillis()) }
                    pref.putLong(PdfSettings.filterTime.value, "mFilterTime")
                }
                if (PdfSettings.filterDay.value == 0) {
                    PdfSettings.editLocal { set(PdfSettings.filterDay, 7) }
                    pref.putInt(7, "mFilterDay")
                }
                pref.saveAsync()
                if (filters != null) {
                    val added = filters.toString()
                    if (!MainUtil.sameObject(AlbumSettings.filterAdd.value, added)) {
                        AlbumSettings.edit(context) { set(AlbumSettings.filterAdd, added) }
                    }
                }
                if (adguard != null) {
                    val added = adguard.toString()
                    if (!MainUtil.sameObject(AlbumSettings.adguardAdd.value, added)) {
                        AlbumSettings.edit(context) { set(AlbumSettings.adguardAdd, added) }
                    }
                }
            }

            /** Index of the AdGuard language filter for the app language, or -1.  */
            private fun regionalFilter(): Int {
                val locale: Locale? = MainApp.currentLocale()
                if (locale == null || TextUtils.isEmpty(locale.getLanguage())) {
                    return -1
                }
                when (locale.getLanguage().lowercase()) {
                    "zh" -> return 9
                    "nl" -> return 10
                    "fr" -> return 11
                    "de" -> return 12
                    "ja" -> return 13
                    "ru" -> return 14
                    "es", "pt" -> return 15
                    "tr" -> return 16
                    "uk" -> return 17
                    else -> return -1
                }
            }

            /** Blocked images grouped by page path.  */
            private fun loadBlockedImages(context: Context?): HashMap<String?, MutableList<String?>>? {
                var map: HashMap<String?, MutableList<String?>>? = null
                var cursor: Cursor? = null
                try {
                    cursor = DbUtil.query(
                        DbBookBlock.get(context).getWritableDatabase(),
                        "DbBookBlock_table",
                        arrayOf<String?>("_path", "_image"),
                        null,
                        null,
                        "_path ASC"
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        val colPath = cursor.getColumnIndex("_path")
                        val colImage = cursor.getColumnIndex("_image")
                        var page: String? = null
                        var images: ArrayList<String?>? = null
                        do {
                            try {
                                val path = cursor.getString(colPath)
                                val image = cursor.getString(colImage)
                                if (TextUtils.isEmpty(path) || TextUtils.isEmpty(image)) {
                                    continue
                                }
                                if (!MainUtil.sameObject(path, page)) {
                                    if (!TextUtils.isEmpty(page) && images != null && !images.isEmpty()) {
                                        if (map == null) {
                                            map = HashMap()
                                        }
                                        map.put(page, images)
                                    }
                                    images = null
                                    page = path
                                }
                                if (images == null) {
                                    images = ArrayList()
                                }
                                images.add(image)
                            } catch (unused: Exception) {
                            }
                        } while (cursor.moveToNext())
                        if (!TextUtils.isEmpty(page) && images != null && !images.isEmpty()) {
                            if (map == null) {
                                map = HashMap()
                            }
                            map.put(page, images)
                        }
                    }
                } catch (unused: Exception) {
                }
                if (cursor != null) {
                    cursor.close()
                }
                return map
            }

            /** Disabled filter lists: rename moved URLs, then tidy each list's local file.  */
            private fun migrateDisabledFilters(context: Context?) {
                var renamed: HashMap<String?, String?>? = null
                var paths: ArrayList<String?>? = null
                var cursor: Cursor? = null
                try {
                    cursor = DbUtil.query(
                        DbBookFilter.get(context).getWritableDatabase(),
                        "DbBookFilter_table",
                        arrayOf<String?>("_path"),
                        "_use=?",
                        arrayOf<String?>("0"),
                        null
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        val colPath = cursor.getColumnIndex("_path")
                        do {
                            try {
                                var path = cursor.getString(colPath)
                                if (TextUtils.isEmpty(path)) {
                                    continue
                                }
                                val moved = DialogSetFilter.normalizeUrl(path)
                                if (!TextUtils.isEmpty(moved)) {
                                    if (renamed == null) {
                                        renamed = HashMap()
                                    }
                                    renamed.put(path, moved)
                                    path = moved
                                }
                                if (paths == null) {
                                    paths = ArrayList()
                                }
                                paths.add(path)
                            } catch (unused: Exception) {
                            }
                        } while (cursor.moveToNext())
                    }
                } catch (unused: Exception) {
                }
                if (cursor != null) {
                    cursor.close()
                }
                if (renamed != null) {
                    DialogSetFilter.addFilters(context, renamed)
                }
                if (paths == null) {
                    return
                }
                for (path2 in paths) {
                    val path = path2 as String
                    ensureFilterFile(context, path, filterFilePath(context, path))
                }
            }
    }

    /** Callbacks into the browser activity. */
    interface WebCleanListener {
        /** Injects `css` into the current tab if it still shows `host`.  */
        fun injectCss(host: String?, css: String?)

        /**
         * Filter lists finished (re)loading: the browser starts the periodic list update when it is due.
         *
         * @param notified the "loading" notification was shown and should be cleared
         * @param changed  rules changed, so the current page should re-apply ad hiding
         */
        fun filtersLoaded(notified: Boolean, changed: Boolean)
    }

    /**
     * Reads the enabled filter lists ([partial] / [marks] kept from the old signature): list addresses that moved are
     * renamed, lists without a local copy are downloaded, then the uBlock Origin-compatible engine is rebuilt and the
     * browser hears [WebCleanListener.filtersLoaded] (which also starts the periodic list update).
     */
    fun load(context: Context?, partial: Boolean, marks: Boolean) {
        if (this.loadStarted) {
            return
        }
        this.loadStarted = true
        var urls: ArrayList<String>? = null
        var renamed: HashMap<String?, String?>? = null
        var cursor: Cursor? = null
        try {
            cursor = DbUtil.query(
                DbBookFilter.get(context).getWritableDatabase(),
                "DbBookFilter_table",
                arrayOf<String?>("_path"),
                "_use=?",
                arrayOf<String?>("1"),
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val colPath = cursor.getColumnIndex("_path")
                do {
                    var path = cursor.getString(colPath)
                    if (TextUtils.isEmpty(path)) {
                        continue
                    }
                    val moved = DialogSetFilter.normalizeUrl(path)
                    if (!TextUtils.isEmpty(moved)) {
                        if (renamed == null) {
                            renamed = HashMap()
                        }
                        renamed.put(path, moved)
                        path = moved
                    }
                    if (URLUtil.isNetworkUrl(path)) {
                        if (urls == null) {
                            urls = ArrayList()
                        }
                        urls.add(path!!)
                    }
                } while (cursor.moveToNext())
            }
        } catch (unused: Exception) {
        }
        cursor?.close()
        if (renamed != null) {
            DialogSetFilter.addFilters(context, renamed)
        }
        // lists added without a download yet (first-run defaults, restored backups): fetch them now
        if (marks && urls != null) {
            for (url in urls) {
                ensureFilterFile(context, url, filterFilePath(context, url))
            }
        }
        this.loadStarted = false
        if (!partial) {
            com.mycompany.app.web.filter.UboFilter.onListsChanged(context)
        }
        this.listener?.filtersLoaded(false, false)
    }

    /**
     * Request interception (shouldInterceptRequest). [pageUrl] / [pageHost] are the page, [url2] the requested URL;
     * [siteFixModeNow] selects how allowed main-frame pages are fetched (see [blockPageResponse]). Returns null to let
     * the WebView load the request normally.
     */
    fun intercept(
        context: Context?,
        webView: WebView?,
        webResourceRequest: WebResourceRequest?,
        pageUrl: String?,
        pageHost: String?,
        requestUrl: String?,
        siteFixModeNow: Int
    ): WebResourceResponse? {
        if (context == null) {
            return null
        }
        val url = requestUrl
        if (!TextUtils.isEmpty(url) && (url!!.endsWith(".ts") || url.contains(".ts?"))) {
            return null
        }
        try {
            val links = DataBookLink.instance(context)
            if (links.links != null && !links.links!!.isEmpty() && webResourceRequest!!.isForMainFrame() && links.hasPage(url)) {
                val page: String? = blockedPageHtml(context, url)
                if (!TextUtils.isEmpty(page)) {
                    return WebResourceResponse("text/html", "UTF-8", ByteArrayInputStream(page!!.toByteArray()))
                }
            }
        } catch (unused: Exception) {
        }
        // a page whose own site is on the allowed sites ("Always allow this site" on the block page): not blocked; the
        // allowed sites are otherwise checked against the page the request comes from, which for a page load is the
        // page before (2026-11-27)
        if (webResourceRequest != null && webResourceRequest.isForMainFrame() && !TextUtils.isEmpty(url)) {
            try {
                if (com.mycompany.app.data.book.DataBookAds.instance(context).hasSiteOf(url, MainUtil.hostOfUrl(url, false))) return null
            } catch (_: Exception) {}
        }
        // right after the app starts: wait (max. 5 s) for the engine instead of letting the first page through
        com.mycompany.app.web.filter.UboFilter.awaitEngine(context)
        com.mycompany.app.web.filter.UboFilter.clearRedirect()
        val verdict = classify(pageUrl, pageHost, url, webResourceRequest)
        // numbers for the Web Filter dialog (com.mycompany.app.web.filter.PageStats)
        if (webResourceRequest != null && webResourceRequest.isForMainFrame()) {
            com.mycompany.app.web.filter.PageStats.startPage(webView, url)
        }
        com.mycompany.app.web.filter.PageStats.note(context, webView, url, verdict != 0)
        if (verdict == 0) {
            // tracking parameters ($removeparam rules) are removed from page loads: load the clean address instead
            if (webResourceRequest != null && webResourceRequest.isForMainFrame() &&
                "GET".equals(webResourceRequest.getMethod(), ignoreCase = true)
            ) {
                val clean = com.mycompany.app.web.filter.UboFilter.takeRewrite(url)
                if (clean != null) {
                    return com.mycompany.app.web.filter.UboFilter.rewriteResponse(clean)
                }
            }
            return if (siteFixModeNow != 0) blockPageResponse(context, webResourceRequest, url, siteFixModeNow) else null
        }
        if (webResourceRequest!!.isForMainFrame()) {
            // the page itself is on a filter list: say so, with what can be done (BlockPage, 2026-11-27)
            val html = BlockPage.html(context, url, BlockPage.LIST)
            return WebResourceResponse("text/html", "UTF-8", ByteArrayInputStream(html.toByteArray()))
        }
        if (verdict == 1) {
            // $redirect rules answer with their resource instead of an empty body.
            val redirect = com.mycompany.app.web.filter.UboFilter.takeRedirect()
            if (redirect != null) {
                return redirect
            }
        }
        if (verdict == 2) {
            removeImages(webView, url)
        }
        return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream("".toByteArray()))
    }

    /** 0 = allow, 1 = blocked by the filter engine, 2 = image blocked by the user ("block area").  */
    private fun classify(pageUrl: String?, pageHost: String?, url: String?, request: WebResourceRequest?): Int {
        if (TextUtils.isEmpty(pageHost) || TextUtils.isEmpty(url) || Companion.isPlainHostUrl(url!!)) {
            return 0
        }
        if (TextUtils.isEmpty(MainUtil.hostOfUrl(url, false))) {
            return 0
        }
        if (isUserBlockedImage(pageUrl, pageHost, url)) {
            return 2
        }
        // null while the engine is not built yet (first seconds after a list change): allow
        return if (com.mycompany.app.web.filter.UboFilter.check(request, url, pageUrl) == true) 1 else 0
    }

    fun isUserBlockedUrl(pageUrl: String?, imageUrl: String?): Boolean {
        try {
            val map = this.userBlocks
            if (map == null || map.isEmpty()) {
                return false
            }
            val trimmedUrl = trimTrailingSlash(pageUrl)
            if (TextUtils.isEmpty(trimmedUrl)) {
                return false
            }
            return listContains(imageUrl, this.userBlocks!!.get(trimmedUrl))
        } catch (unused: Exception) {
            return false
        }
    }

    fun isUserBlockedSrc(pageHost: String?, imageUrl: String?): Boolean {
        try {
            val map = this.userBlocks
            if (map == null || map.isEmpty() || TextUtils.isEmpty(pageHost)) {
                return false
            }
            return listContains(imageUrl, this.userBlocks!!.get(pageHost))
        } catch (unused: Exception) {
            return false
        }
    }

    fun addUserBlock(path: String?, image: String?) {
        var arrayList: MutableList<String?>?
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(image)) {
            return
        }
        try {
            val map = this.userBlocks
            if (map == null) {
                this.userBlocks = HashMap()
                arrayList = null
            } else {
                arrayList = map.get(path)
                if (listContains(image, arrayList)) {
                    return
                }
            }
            if (arrayList == null) {
                arrayList = ArrayList()
            }
            arrayList.add(image)
            this.userBlocks!!.put(path, arrayList)
            this.userBlocksCopy = userBlockCopy()
        } catch (unused: Exception) {
        }
    }

    fun removeUserBlock(path: String?, oldImage: String?) {
        try {
            val map = this.userBlocks
            val list: MutableList<String?>? = if (map == null || map.isEmpty() || TextUtils.isEmpty(path) || TextUtils.isEmpty(oldImage)) null else this.userBlocks!!.get(
                    path
                )
            if (map == null || map.isEmpty() || TextUtils.isEmpty(path) || TextUtils.isEmpty(oldImage) || list == null || list.isEmpty()) {
                return
            }
            list.remove(oldImage)
            val listEmpty = list.isEmpty()
            val userBlocks = this.userBlocks
            if (listEmpty) {
                userBlocks!!.remove(path)
            } else {
                userBlocks!!.put(path, list)
            }
            this.userBlocksCopy = userBlockCopy()
        } catch (unused: Exception) {
        }
    }

    fun userBlockCopy(): HashMap<String?, MutableList<String?>>? {
        val map = this.userBlocks
        var blockMap: HashMap<String?, MutableList<String?>>? = null
        if (map == null || map.isEmpty()) {
            return null
        }
        try {
            val setKeySet: MutableSet<String?> = map.keys
            for (pageHost in setKeySet) {
                val list: MutableList<String?>? = if (!TextUtils.isEmpty(pageHost) && pageHost!!.indexOf("://") == -1) map.get(pageHost) else null
                if (!TextUtils.isEmpty(pageHost) && pageHost!!.indexOf("://") == -1 && list != null && !list.isEmpty()) {
                    val host = stripHostPrefix(pageHost)
                    if (!TextUtils.isEmpty(host) && host != pageHost) {
                        if (blockMap == null) {
                            val newMap: HashMap<String?, MutableList<String?>> = HashMap()
                            try {
                                newMap.put(host, list)
                                blockMap = newMap
                            } catch (unused: Exception) {
                                return newMap
                            }
                        } else {
                            val hostRules = blockMap.get(host)
                            if (hostRules == null || hostRules.isEmpty()) {
                                blockMap.put(host, list)
                            } else {
                                hostRules.addAll(list)
                            }
                        }
                    }
                }
            }
            return blockMap
        } catch (unused2: Exception) {
            return blockMap
        }
    }

    /** The image is in the user's blocked list for this page, host or parent domain.  */
    private fun isUserBlockedImage(pageUrl: String?, pageHost: String?, url: String?): Boolean {
        try {
            if (this.userBlocks == null || this.userBlocks!!.isEmpty()) {
                return false
            }
            val page = trimTrailingSlash(pageUrl)
            if (TextUtils.isEmpty(page)) {
                return false
            }
            if (listContains(url, this.userBlocks!!.get(page))) {
                return true
            }
            if (TextUtils.isEmpty(pageHost)) {
                return false
            }
            if (listContains(url, this.userBlocks!!.get(pageHost))) {
                return true
            }
            if (this.userBlocksCopy == null || this.userBlocksCopy!!.isEmpty()) {
                return false
            }
            val parent = stripHostPrefix(pageHost)
            return !TextUtils.isEmpty(parent) && (parent != pageHost) && listContains(
                url,
                this.userBlocksCopy!!.get(parent)
            )
        } catch (unused: Exception) {
            return false
        }
    }

    companion object {

        @JvmStatic
        fun countChar(quote: Char, text: String?): Int {
            if (TextUtils.isEmpty(text)) {
                return 0
            }
            val length = text!!.length
            var pos = 0
            for (index in 0..<length) {
                if (text.get(index) == quote) {
                    pos++
                }
            }
            return pos
        }

        @JvmStatic
        fun hasUnsupportedSelector(line: String?): Boolean {
            var next: Int = 0
            if (line == null) {
                return false
            }
            var colonIndex = line.indexOf(58.toChar())
            while (colonIndex != -1 && ((colonIndex + 1).also { next = it }) < line.length - 1) {
                if (line.startsWith("-abp-", next) || line.startsWith(
                        "contains",
                        next
                    ) || line.startsWith("has", next) || line.startsWith(
                        "if",
                        next
                    ) || line.startsWith("if-not", next) || line.startsWith(
                        "matches-css",
                        next
                    ) || line.startsWith(
                        "matches-css-after",
                        next
                    ) || line.startsWith("matches-css-before", next) || line.startsWith(
                        "nth-ancestor",
                        next
                    ) || line.startsWith("properties", next) || line.startsWith(
                        "remove",
                        next
                    ) || line.startsWith("style", next) || line.startsWith(
                        "subject",
                        next
                    ) || line.startsWith("upward", next) || line.startsWith("xpath", next)
                ) {
                    return true
                }
                colonIndex = line.indexOf(58.toChar(), next)
            }
            return false
        }

        @JvmStatic
        fun srcSelectors(source: String?): String? {
            if (TextUtils.isEmpty(source)) {
                return null
            }
            var sbD: StringBuilder = Companion.appendSrcSelector(null, source!!)
            val decoded = urlDecode(source)
            if (!TextUtils.isEmpty(decoded) && decoded != source) {
                sbD = Companion.appendSrcSelector(sbD, decoded!!)
            }
            return sbD.toString()
        }

        @JvmStatic
        fun appendSrcSelector(sb: StringBuilder?, srcText: String?): StringBuilder {
            var sb = sb
            if (sb == null) {
                sb = StringBuilder()
            } else {
                sb.append(",")
            }
            sb.append("[src*=\"").append(srcText).append("\"],[srcset*=\"").append(srcText)
            sb.append("\"]")
            if (hasChar(38, srcText)) {
                val escaped = srcText!!.replace("&", "&amp;")
                sb.append(",[src*=\"")
                sb.append(escaped)
                sb.append("\"],[srcset*=\"").append(escaped).append("\"]")
            }
            return sb
        }

        @JvmStatic
        fun blockedPageHtml(context: Context?, offlineUrl: String?): String? {
            if (context == null) {
                return null
            }
            // on the user's blocked links (BlockPage, 2026-11-27)
            return BlockPage.html(context, offlineUrl, BlockPage.LINK)
        }

        /**
         * Fetches main-frame page `str` ourselves: mode 1 returns it as-is, mode 2 parses it and
         * removes the element matched by the site fix first. Returns null to let the WebView load it.
         */
        @JvmStatic
        fun blockPageResponse(
            context: Context?,
            webResourceRequest: WebResourceRequest?,
            url: String?,
            siteFixModeNow: Int
        ): WebResourceResponse? {
            if (!webResourceRequest!!.isForMainFrame() || TextUtils.isEmpty(url)) {
                return null
            }
            if (siteFixModeNow == 1) {
                var connection: HttpURLConnection? = null
                var `in`: InputStream? = null
                var out: ByteArrayOutputStream? = null
                var page: ByteArray? = null
                try {
                    connection = openHeadConnection(context, url, null, -1, -1, false, userAgentPref(), false)
                    `in` = connection!!.getInputStream()
                    out = ByteArrayOutputStream()
                    val buffer = ByteArray(1024)
                    var read: Int
                    while ((`in`.read(buffer, 0, 1024).also { read = it }) != -1) {
                        out.write(buffer, 0, read)
                    }
                    page = out.toByteArray()
                } catch (unused: Exception) {
                } catch (unused: OutOfMemoryError) {
                }
                try {
                    if (out != null) {
                        out.close()
                    }
                    if (`in` != null) {
                        `in`.close()
                    }
                } catch (unused: Exception) {
                }
                if (connection != null) {
                    connection.disconnect()
                }
                if (page == null) {
                    return null
                }
                try {
                    return WebResourceResponse("text/html", "UTF-8", ByteArrayInputStream(page))
                } catch (unused: Exception) {
                    return null
                } catch (unused: OutOfMemoryError) {
                    return null
                }
            }
            if (siteFixModeNow == 2) {
                try {
                    val document = Jsoup.connect(url!!).referrer(url).userAgent(userAgentPref()).get()
                    val selector = nativeJsN()
                    if (!TextUtils.isEmpty(selector)) {
                        val element = document.selectFirst(selector!!)
                        if (element != null) {
                            element.remove()
                        }
                    }
                    val html = document.html()
                    if (!TextUtils.isEmpty(html)) {
                        return WebResourceResponse(
                            "text/html",
                            "UTF-8",
                            ByteArrayInputStream(html.toByteArray())
                        )
                    }
                } catch (unused: Exception) {
                } catch (unused: LinkageError) {
                } catch (unused: OutOfMemoryError) {
                }
            }
            return null
        }

        @JvmStatic
        fun listContains(url: String?, list: List<String?>?): Boolean {
            if (list == null || list.isEmpty() || TextUtils.isEmpty(url)) {
                return false
            }
            try {
                val it = list.iterator()
                while (it.hasNext()) {
                    val entry = it.next()
                    if (entry != null && url == entry) {
                        return true
                    }
                }
                return false
            } catch (unused: Exception) {
                return false
            }
        }

        /** True unless `str` is an http(s) URL with a real path (more than a trailing "/" or two characters).  */
        @JvmStatic
        fun isPlainHostUrl(url: String?): Boolean {
            val start: Int
            if (url!!.startsWith("https://")) {
                start = 8
            } else if (url.startsWith("http://")) {
                start = 7
            } else {
                return true
            }
            val length = url.length
            if (start >= length) {
                return true
            }
            val slash = url.indexOf(47.toChar(), start)
            return slash <= start || slash >= length - 2
        }

        @JvmStatic
        fun looksLikeAdHost(host: String?): Boolean {
            if (host!!.contains("cdn") || host.contains("clean") || host.contains("coin") || host.contains(
                    "ddos"
                ) || host.contains("link") || host.contains("mart") || host.contains("shop") || host.contains(
                    "store"
                ) || host.contains("syndicat") || host.contains("track")
            ) {
                return true
            }
            var adIndex = host.indexOf("ad")
            if (adIndex == -1) {
                return false
            }
            val length = host.length
            do {
                val index = adIndex - 1
                if (index < 0 || index >= host.length) {
                    return true
                }
                val cCharAt = host.get(index)
                if ((cCharAt < '0' || cCharAt > '9') && ((cCharAt < 'a' || cCharAt > 'z') && (cCharAt < 'A' || cCharAt > 'Z'))) {
                    return true
                }
                val start = adIndex + 2
                if (start >= length) {
                    return false
                }
                adIndex = host.indexOf("ad", start)
            } while (adIndex != -1)
            return false
        }

        @JvmStatic
        fun looksLikeAdPath(url: String?): Boolean {
            val length = url!!.length
            if (length == 0) {
                return false
            }
            if (length > 800 || url.startsWith("http://")) {
                return true
            }
            val lowerCase = url.lowercase()
            if (lowerCase.contains("camp") || lowerCase.contains("click") || lowerCase.contains("promo") || lowerCase.contains(
                    "redir"
                )
            ) {
                return true
            }
            val queryIndex = lowerCase.indexOf(63.toChar())
            val start = if (queryIndex <= 0) 0 else (queryIndex + 1)
            if (queryIndex <= 0 || start >= length) {
                return false
            }
            return if ((lowerCase.lastIndexOf(".php", queryIndex) != -1 || (lowerCase.indexOf(
                    "=http",
                    start
                ) == -1 && lowerCase.indexOf(
                    63.toChar(),
                    start
                ) == -1)) && lowerCase.lastIndexOf(
                    "/prod/",
                    queryIndex
                ) == -1 && lowerCase.indexOf("id=", start) == -1 && lowerCase.indexOf(
                    "key=",
                    start
                ) == -1 && lowerCase.indexOf("lik=", start) == -1 && lowerCase.indexOf(
                    "refer=",
                    start
                ) == -1 && lowerCase.indexOf("source=", start) == -1
            ) false else true
        }

        @JvmStatic
        fun ensureFilterFile(context: Context?, path: String?, fallbackPath: String?): Boolean {
            if (TextUtils.isEmpty(fallbackPath)) {
                return false
            }
            if (File(fallbackPath!!).length() > 0) {
                return true
            }
            val mainFilterDown = MainFilterDown(null)
            val repoUrl = MainUtil.githubRawToRepo(path)
            val downloaded = if (TextUtils.isEmpty(repoUrl)) false else mainFilterDown.download(context, repoUrl, path)
            return if (!downloaded) mainFilterDown.download(context, path, path) else downloaded
        }

        @JvmStatic
        fun isValidSelector(rule: String?): Boolean {
            return if (rule == null || TextUtils.isEmpty(rule) || countChar('[', rule) != countChar(']', rule) || countChar(
                    Typography.quote,
                    rule.replace('\'', Typography.quote)
                ) % 2 != 0 || rule.contains("+js") || hasUnsupportedSelector(rule)
            ) false else true
        }

        @JvmStatic
        fun stripScheme(url: String?): String? {
            val schemeEnd: Int
            var pathStart: Int = 0
            val length = if (url == null) 0 else url.length
            if (url == null || length == 0) {
                return null
            }
            if (url.startsWith("https://")) {
                schemeEnd = 8
            } else {
                if (!url.startsWith("http://")) {
                    return url
                }
                schemeEnd = 7
            }
            if (schemeEnd >= length) {
                return url
            }
            val pathSlash = url.indexOf(47.toChar(), schemeEnd)
            return if (pathSlash < schemeEnd || ((pathSlash + 1).also {
                    pathStart = it
                }) >= length) url.substring(schemeEnd) else url.substring(pathStart)
        }

        @JvmStatic
        fun appendRemoveImg(sb: StringBuilder?, selector: String?): StringBuilder {
            var sb = sb
            if (TextUtils.isEmpty(selector)) {
                return sb!!
            }
            if (sb == null) {
                sb = StringBuilder()
            }
            sb.append("myRemImg('").append(selector).append("');")
            return sb
        }

        @JvmStatic
        fun removeImages(webView: WebView?, url: String?) {
            val script: String?
            if (webView != null) {
                val selectors: String? = srcSelectors(stripScheme(url))
                if (TextUtils.isEmpty(selectors)) {
                    return
                }
                if (TextUtils.isEmpty(selectors)) {
                    script = null
                } else {
                    val sb = StringBuilder()
                    sb.append("function myRemImg(cmd){var ele=document.querySelector(cmd);if(!ele)return;var par=ele.parentNode;if(par){var pa2=par.parentNode;while(pa2&&(pa2.tagName!='HTML')&&(pa2.tagName!='BODY')&&(pa2.childElementCount==1)){ele=par;par=pa2;pa2=pa2.parentNode;}if(pa2&&(pa2.tagName!='HTML')&&(pa2.tagName!='BODY')){ele=par;par=pa2;}par.removeChild(ele);}else{ele.style.display='none';}}(function(){if(document.head){var ele=document.createElement('style');ele.innerText='")
                    sb.append(selectors)
                    sb.append("{display:none !important;}';document.head.appendChild(ele);}")
                    val sbY: StringBuilder = appendRemoveImg(sb, selectors)
                    sbY.append("})();")
                    script = sbY.toString()
                }
                evalJs(webView, script, true)
            }
        }

    }
}
