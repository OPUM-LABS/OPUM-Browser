package com.mycompany.app.main

import android.view.View

import com.mycompany.app.main.MainLangAdapter.MainLangItem
import com.mycompany.app.db.book.DbBookLocale
import com.mycompany.app.ui.browser.DialogFrame
import java.util.ArrayList
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.text.TextUtils
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.mycompany.app.compress.CompressUtil.extractedPaths
import com.mycompany.app.data.DataUtil.addUri
import com.mycompany.app.data.DataUtil.fileKind
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.DbUtil.existsWhere
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.db.book.DbBookAlbum.Companion.cached; import com.mycompany.app.db.book.DbBookAlbum.Companion.get
import com.mycompany.app.db.book.DbBookCmp.Companion.cached; import com.mycompany.app.db.book.DbBookCmp.Companion.get
import com.mycompany.app.db.book.DbBookLocale.Companion.cached; import com.mycompany.app.db.book.DbBookLocale.Companion.get
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.db.book.DbBookPdf.Companion.cached; import com.mycompany.app.db.book.DbBookPdf.Companion.get
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.db.book.DbRecentLang.Companion.get
import com.mycompany.app.down.DownParseDzen.segmentUrls
import com.mycompany.app.down.DownParseKakao.parseItemText
import com.mycompany.app.down.DownParseReddit.parseRedditItem
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.pref.PrefUtil.showDetail
import com.mycompany.app.pref.PrefUtil.singleLine
import com.mycompany.app.pref.PrefUtil.foldMode
import com.mycompany.app.pref.PrefUtil.setSingleLine
import com.mycompany.app.script.Script.Companion.parse
import com.mycompany.app.view.MyWebSafe
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.Locale
import java.util.concurrent.ExecutorService

@Suppress("deprecation")
class MainTransLocale(
    mainActivity: android.app.Activity?,
    context: Context?,
    myDialogRelative: DialogFrame?,
    list: MutableList<com.mycompany.app.main.MainLangAdapter.MainLangItem?>?,
    arrayList: ArrayList<com.mycompany.app.main.MainLangAdapter.MainLangItem?>?,
    localeTag: String?,
    transLocaleListener: TransLocaleListener?
) {
    @JvmField
    var hostActivity: android.app.Activity? = null
    @JvmField
    var appContext: Context? = null
    @JvmField
    var localeListener: TransLocaleListener? = null
    @JvmField
    var dialogFrame: DialogFrame? = null
    @JvmField
    var otherItems: MutableList<com.mycompany.app.main.MainLangAdapter.MainLangItem?>? = null
    @JvmField
    var localeItems: ArrayList<com.mycompany.app.main.MainLangAdapter.MainLangItem?>? = null
    @JvmField
    var localeNames: ArrayList<String?>? = null
    @JvmField
    var webSafe: MyWebSafe? = null
    @JvmField
    var pageLoading: Boolean = false
    @JvmField
    var jsReady: Boolean = false
    @JvmField
    var urlLoading: Boolean = false
    @JvmField
    var pendingJs: String? = null
    @JvmField
    var observeState: Byte = 0
    @JvmField
    var observeDone: Boolean = false
    @JvmField
    var localeTag: String? = null
    @JvmField
    var baseUrl: String? = null
    @JvmField
    var trimNames: Boolean = false
    @JvmField
    var retryPending: Boolean = false
    @JvmField
    var retryCount: Int = 0
    @JvmField
    var transExecutor: ExecutorService? = null
    @JvmField
    var finishHandled: Boolean = false
    @JvmField
    var lastFinishedUrl: String? = null
    @JvmField
    var pendingHtml: String? = null
    @JvmField
    var docHtml: String? = null

    internal inner class CreateWebTask : Runnable {
        override fun run() {
            val myWebSafe: MyWebSafe?
            val mainTransLocale = this@MainTransLocale
            if (mainTransLocale.dialogFrame == null || mainTransLocale.webSafe != null) {
                return
            }
            if (mainTransLocale.hostActivity != null) {
                myWebSafe = MyWebSafe(mainTransLocale.hostActivity)
                mainTransLocale.webSafe = myWebSafe
            } else {
                if (mainTransLocale.appContext == null) {
                    return
                }
                myWebSafe = MyWebSafe(mainTransLocale.appContext)
                mainTransLocale.webSafe = myWebSafe
            }
            MainApp.Companion.resumeWebTimers(mainTransLocale.appContext, myWebSafe)
            mainTransLocale.webSafe!!.setVisibility(View.INVISIBLE)
            mainTransLocale.dialogFrame!!.addView(mainTransLocale.webSafe, 0, ViewGroup.LayoutParams(-1, -2))
            mainTransLocale.dialogFrame!!.post(mainTransLocale.LoadLocalesTask())
        }
    }

    internal inner class LoadLocalesTask : Runnable {

        internal inner class LoadLocalesStep : Runnable {

            internal inner class PrepareWebStep : Runnable {

                internal inner class RunTransScriptStep : Runnable {
                    override fun run() {
                        val mainTransLocale = this@MainTransLocale
                        if (mainTransLocale.webSafe == null) {
                            return
                        }
                        mainTransLocale.runAsync(mainTransLocale.TransScriptTask())
                    }
                }

                override fun run() {
                    val loadLocalesTask = this@LoadLocalesTask
                    val mainTransLocale = this@MainTransLocale
                    val myWebSafe = mainTransLocale.webSafe
                    if (myWebSafe != null) {
                        mainTransLocale.urlLoading = true
                        myWebSafe.addJavascriptInterface(
                            mainTransLocale.WebAppInterface(),
                            "android"
                        )
                        val myDialogRelative: DialogFrame? = this@MainTransLocale.dialogFrame
                        if (myDialogRelative == null) {
                            return
                        }
                        myDialogRelative.post(RunTransScriptStep())
                    }
                }
            }

            override fun run() {
                val mainTransLocale = this@MainTransLocale
                val myWebSafe = mainTransLocale.webSafe
                if (myWebSafe != null) {
                    myWebSafe.setWebViewClient(mainTransLocale.LocalWebViewClient())
                    val myDialogRelative: DialogFrame? = mainTransLocale.dialogFrame
                    if (myDialogRelative == null) {
                        return
                    }
                    myDialogRelative.post(PrepareWebStep())
                }
            }
        }

        override fun run() {
            val mainTransLocale = this@MainTransLocale
            val myWebSafe = mainTransLocale.webSafe
            if (myWebSafe != null) {
                val settings = myWebSafe.getSettings()
                settings.setSupportZoom(true)
                settings.setBuiltInZoomControls(true)
                settings.setDisplayZoomControls(false)
                settings.setUseWideViewPort(true)
                settings.setLoadWithOverviewMode(true)
                settings.setJavaScriptCanOpenWindowsAutomatically(false)
                settings.setSupportMultipleWindows(false)
                settings.setMediaPlaybackRequiresUserGesture(false)
                settings.setJavaScriptEnabled(true)
                myWebSafe.setOverScrollMode(2)
                val myDialogRelative: DialogFrame? = mainTransLocale.dialogFrame
                if (myDialogRelative == null) {
                    return
                }
                myDialogRelative.post(LoadLocalesStep())
            }
        }
    }

    internal inner class ClearCacheTask : Runnable {
        override fun run() {
            val mainTransLocale = this@MainTransLocale
            MainUtil.clearWebCache(mainTransLocale.webSafe)
            mainTransLocale.finishHandled = false
        }
    }

    internal inner class DeliverNamesTask : Runnable {
        override fun run() {
            val mainTransLocale = this@MainTransLocale
            val transLocaleListener = mainTransLocale.localeListener
            if (transLocaleListener != null) {
                transLocaleListener.onNames(mainTransLocale.localeNames)
            }
            mainTransLocale.retryPending = false
        }
    }

    internal inner class RemoveJsBridgeTask : Runnable {
        override fun run() {
            val mainTransLocale = this@MainTransLocale
            val myWebSafe = mainTransLocale.webSafe
            if (myWebSafe == null) {
                return
            }
            mainTransLocale.urlLoading = false
            myWebSafe.removeJavascriptInterface("android")
        }
    }

    internal inner class AddJsBridgeTask : Runnable {
        override fun run() {
            val mainTransLocale = this@MainTransLocale
            val myWebSafe = mainTransLocale.webSafe
            if (myWebSafe == null) {
                return
            }
            mainTransLocale.urlLoading = true
            myWebSafe.addJavascriptInterface(mainTransLocale.WebAppInterface(), "android")
        }
    }

    internal inner class TransScriptTask : Runnable {

        internal inner class EvalTransScriptStep : Runnable {

            internal inner class TransScriptJsTask : Runnable {

                override fun run() {
                    val pendingJs: String?
                    val transScriptTask = this@TransScriptTask
                    val mainTransLocale = this@MainTransLocale
                    if (mainTransLocale.webSafe != null) {
                        val sbK = MainUtil.transScriptJs()
                        if (sbK != null) {
                            val classJs = MainUtil.classSelectorJs(null)
                            if (TextUtils.isEmpty(classJs)) {
                                pendingJs = null
                            } else {
                                sbK.insert(0, classJs)
                                pendingJs = sbK.toString()
                            }
                        } else {
                            pendingJs = null
                        }
                        mainTransLocale.pendingJs = pendingJs
                        val owner = this@MainTransLocale
                        if (!owner.jsReady || TextUtils.isEmpty(owner.pendingJs)) {
                            return
                        }
                        val pendingJsNow = owner.pendingJs
                        owner.jsReady = false
                        owner.pendingJs = null
                        MainUtil.evalJsNow(owner.webSafe, pendingJsNow, true)
                    }
                }
            }

            override fun run() {
                val mainTransLocale = this@MainTransLocale
                val pendingHtml = mainTransLocale.pendingHtml
                mainTransLocale.pendingHtml = null
                val myWebSafe: MyWebSafe? = if (TextUtils.isEmpty(pendingHtml)) null else mainTransLocale.webSafe
                if (TextUtils.isEmpty(pendingHtml) || myWebSafe == null) {
                    return
                }
                MainUtil.loadHtml(myWebSafe, mainTransLocale.baseUrl, pendingHtml)
                mainTransLocale.runAsync(TransScriptJsTask())
            }
        }

        override fun run() {
            val pendingHtml: String?
            val mainTransLocale = this@MainTransLocale
            val arrayList = mainTransLocale.localeItems
            var isKorean = false
            if (arrayList == null || arrayList.isEmpty()) {
                pendingHtml = null
            } else {
                val sb =
                    StringBuilder("<!DOCTYPE html><html><head><meta charset=\"utf-8\"/><meta name='viewport' content='width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no'/><style>body{margin:0;}div{margin:0;position:absolute;max-width:100%;height:auto;}p,h1{margin:0;font-size:10px;}</style></head><body>")
                val size = arrayList.size
                var index = 0
                while (index < size) {
                    val obj: Any? = arrayList.get(index)
                    index++
                    val mainLangItem: MainLangItem? = obj as MainLangItem?
                    if (mainLangItem != null) {
                        val langPair = mainLangItem.lang!!.replace(" (", "=").replace(")", "")
                        if (!TextUtils.isEmpty(langPair)) {
                            sb.append("<div id='")
                            sb.append(mainLangItem.code)
                            sb.append("'><p>")
                            sb.append(langPair)
                            sb.append("</p><h1>This is test.</h1><h1>이것은 테스트다.</h1><h1>これはテストです。</h1></div>")
                        }
                    }
                }
                sb.append("</body></html>")
                pendingHtml = sb.toString()
            }
            mainTransLocale.pendingHtml = pendingHtml
            if (TextUtils.isEmpty(mainTransLocale.localeTag)) {
                mainTransLocale.localeTag = MainUtil.appLocaleTag()
            }
            mainTransLocale.baseUrl = MainUtil.httpsUrl("soul_loc_", mainTransLocale.localeTag)
            val localeTag = mainTransLocale.localeTag
            if (!TextUtils.isEmpty(localeTag)) {
                val lowerCase = localeTag!!.lowercase()
                if (!TextUtils.isEmpty(lowerCase)) {
                    isKorean = lowerCase.startsWith("ko")
                }
            }
            mainTransLocale.trimNames = isKorean
            val myWebSafe = mainTransLocale.webSafe
            if (myWebSafe == null) {
                return
            }
            myWebSafe.post(EvalTransScriptStep())
        }
    }

    internal inner class ReadResultTask : Runnable {

        internal inner class ReadAgainStep : Runnable {
            override fun run() {
                val myWebSafe = this@MainTransLocale.webSafe
                if (myWebSafe == null) {
                    return
                }
                MainUtil.evalJs(
                    myWebSafe,
                    "(function(){android.onDocHtml(document.documentElement.innerHTML);})();",
                    false
                )
            }
        }

        internal inner class DeliverNamesStep : Runnable {
            override fun run() {
                val mainTransLocale = this@MainTransLocale
                val transLocaleListener = mainTransLocale.localeListener
                if (transLocaleListener != null) {
                    transLocaleListener.onNames(mainTransLocale.localeNames)
                }
                mainTransLocale.retryPending = false
            }
        }

        override fun run() {
            val mainTransLocale = this@MainTransLocale
            val html = mainTransLocale.docHtml
            mainTransLocale.docHtml = null
            var done = false
            var document: Document? = null
            if (!TextUtils.isEmpty(html)) {
                try {
                    document = Jsoup.parse(html!!, mainTransLocale.baseUrl!!)
                } catch (unused: OutOfMemoryError) {
                    document = null
                } catch (unused: LinkageError) {
                    document = null
                } catch (unused: Exception) {
                    document = null
                }
            }
            val divs = if (document != null) document.select("div") else null
            if (divs != null && divs.size != 0) {
                val canRetry = mainTransLocale.retryCount < 5
                var pending = false
                for (div in divs) {
                    val index = MainUtil.parseColorSafe(div.id())
                    val list = mainTransLocale.localeItems
                    val item: MainLangItem? =
                        if (list != null && index >= 0 && index < list.size) list.get(index) else null
                    if (item == null) {
                        continue
                    }
                    val para = div.selectFirst("p")
                    if (para != null) {
                        var paraText = para.text()
                        if (!TextUtils.isEmpty(paraText)) {
                            paraText = paraText.trim { it <= ' ' }
                            if (!TextUtils.isEmpty(paraText)) {
                                item.paraText = paraText
                                // a <font> child means the page translator has replaced the text
                                item.corner =
                                    if (para.selectFirst("font") == null) 1.toByte() else 2.toByte()
                            }
                        }
                    }
                    if (item.corner.toInt() != 2) {
                        pending = true
                        if (canRetry) {
                            break
                        }
                    }
                }
                done = !pending
            }
            if (!done && mainTransLocale.retryCount < 5) {
                mainTransLocale.retryCount++
                val myWebSafe = mainTransLocale.webSafe
                if (myWebSafe != null) {
                    myWebSafe.postDelayed(ReadAgainStep(), 100L)
                }
                return
            }
            val list = mainTransLocale.localeItems
            val code = mainTransLocale.localeTag
            if (list != null && !list.isEmpty() && !TextUtils.isEmpty(code)) {
                val names: ArrayList<String?> = ArrayList()
                val locale: Locale = MainApp.Companion.currentLocale()
                var index = 0
                for (obj in list) {
                    val item: MainLangItem? = obj
                    if (item == null) {
                        continue
                    }
                    val paraText: String? = item.paraText
                    item.paraText = null
                    if (!TextUtils.isEmpty(paraText)) {
                        val name = cleanName(paraText!!, mainTransLocale.trimNames, locale)
                        if (name != null) {
                            item.translated = "$name)"
                            if (!TextUtils.isEmpty(item.translated)) {
                                item.lowerTrans = item.translated!!.lowercase()
                            }
                        }
                    }
                    saveName(mainTransLocale.appContext, code, index, item.translated)
                    names.add(item.translated)
                    index++
                }
                mainTransLocale.localeNames = names
                try {
                    val others = mainTransLocale.otherItems
                    if (others != null && !others.isEmpty()) {
                        for (obj in others) {
                            val item: MainLangItem? = obj
                            if (item == null || item.rowType.toInt() != 1) {
                                continue
                            }
                            val pos: Int = item.code - 1000
                            val translated = mainTransLocale.localeNames
                            item.translated =
                                if (translated != null && pos >= 0 && pos < translated.size) translated.get(
                                    pos
                                ) else null
                            if (!TextUtils.isEmpty(item.translated)) {
                                item.lowerTrans = item.translated!!.lowercase()
                            }
                            item.corner = 2
                        }
                    }
                } catch (unused: Exception) {
                }
            }
            val myWebSafe = mainTransLocale.webSafe
            if (myWebSafe != null) {
                myWebSafe.post(DeliverNamesStep())
            }
        }

        /** Normalises a translated "Language (Region" string; returns null when nothing usable is left.  */
        private fun cleanName(rawName: String?, korean: Boolean, locale: Locale?): String? {
            var name = rawName!!.replace('＝', '=').replace(" = ", " (").replace("=", " (")
                .replace("Turkey", "Türkiye")
            if (TextUtils.isEmpty(name)) {
                return null
            }
            if (korean) {
                name = name.replace("Türkiye", "튀르키예")
                    .replace("체슈티나", "체코")
                    .replace("방글라 ", "벵골어 ")
                if (!name.contains("어 (")) {
                    name = name.replace(" (", "어 (")
                }
                val length = name.length
                if (name.endsWith("어") && length > 1) {
                    name = name.substring(0, length - 1)
                } else if (name.endsWith(" 왕국") && length > 3) {
                    name = name.substring(0, length - 3)
                }
                if (TextUtils.isEmpty(name)) {
                    return null
                }
            }
            var capitalised: String? = null
            if (locale != null) {
                try {
                    val parts: Array<String?> =
                        name.split("\\(".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (parts.size == 2) {
                        val first = MainUtil.capitalize(parts[0], locale)
                        val second =
                            if (TextUtils.isEmpty(first)) null else MainUtil.capitalize(parts[1], locale)
                        if (!TextUtils.isEmpty(second)) {
                            capitalised = first + "(" + second
                        }
                    }
                } catch (unused: Exception) {
                    capitalised = null
                }
            }
            return if (TextUtils.isEmpty(capitalised)) name else capitalised
        }

        /** Caches the translated name in DbBookLocale (update when the row exists, insert when not).  */
        private fun saveName(context: Context?, code: String?, index: Int, name: String?) {
            if (context == null || TextUtils.isEmpty(name)) {
                return
            }
            val args = arrayOf<String?>(code, index.toString())
            val where = "_code=? AND _index=?"
            val db: SQLiteDatabase? = DbBookLocale.get(context).getWritableDatabase()
            val state = DbUtil.existsWhere(db, "DbBookLocale_table", arrayOf<String?>("_name"), where, args)
            if (state == 0) {
                return
            }
            val values = android.content.ContentValues().also { cv -> cv.put("_code", code) }
            values.put("_index", index)
            values.put("_name", name)
            if (state == 1) {
                DbUtil.updateWhere(db, "DbBookLocale_table", values, where, args)
            } else {
                DbUtil.insert(db, "DbBookLocale_table", values)
            }
        }
    }

    interface TransLocaleListener {
        fun onNames(arrayList: ArrayList<String?>?)
    }

    private inner class WebAppInterface {

        inner class PageReadyStep : Runnable {

            internal inner class EvalJsTask : Runnable {
                override fun run() {
                    val myWebSafe = this@MainTransLocale.webSafe
                    if (myWebSafe == null) {
                        return
                    }
                    MainUtil.evalJs(
                        myWebSafe,
                        "(function(){android.onDocHtml(document.documentElement.innerHTML);})();",
                        false
                    )
                }
            }

            override fun run() {
                val mainTransLocale = this@MainTransLocale
                if (mainTransLocale.observeState.toInt() == 1 || !mainTransLocale.observeDone || mainTransLocale.retryPending) {
                    return
                }
                mainTransLocale.retryPending = true
                mainTransLocale.retryCount = 0
                val myWebSafe = mainTransLocale.webSafe
                if (myWebSafe != null) {
                    myWebSafe.postDelayed(EvalJsTask(), 100L)
                }
            }
        }

        @JavascriptInterface
        fun onDocHtml(docHtml: String?) {
            val mainTransLocale = this@MainTransLocale
            if (mainTransLocale.webSafe == null) {
                return
            }
            mainTransLocale.docHtml = docHtml
            mainTransLocale.runAsync(mainTransLocale.ReadResultTask())
        }

        @JavascriptInterface
        fun onObserDet(innerText: String?, state: Int) {
            val mainTransLocale = this@MainTransLocale
            if (state == 0) {
                mainTransLocale.observeState = 1.toByte()
            } else {
                mainTransLocale.observeState = 3.toByte()
                mainTransLocale.observeDone = state == 2
            }
            val myDialogRelative: DialogFrame? = mainTransLocale.dialogFrame
            if (myDialogRelative == null) {
                return
            }
            myDialogRelative.post(PageReadyStep())
        }
    }

    init {
        run init@{
        if (context == null || myDialogRelative == null) {
            return@init
        }
        this.hostActivity = mainActivity
        this.appContext = context
        this.localeListener = transLocaleListener
        this.dialogFrame = myDialogRelative
        this.otherItems = list
        this.localeItems = arrayList
        this.localeTag = localeTag
        this.observeState = 1.toByte()
        myDialogRelative.post(CreateWebTask())

        }
    }

    fun loadUrl(pageUrl: String?) {
        if (this.webSafe != null) {
            val isHttp = MainUtil.isHttpUrl(pageUrl)
            val urlLoading = this.urlLoading
            if (isHttp) {
                if (urlLoading) {
                    this.urlLoading = false
                    val myWebSafe = this.webSafe
                    if (myWebSafe != null) {
                        myWebSafe.post(RemoveJsBridgeTask())
                        return
                    }
                    return
                }
                return
            }
            if (urlLoading) {
                return
            }
            this.urlLoading = true
            val myWebSafe = this.webSafe
            if (myWebSafe == null) {
                return
            }
            myWebSafe.post(AddJsBridgeTask())
        }
    }

    fun release() {
        val myWebSafe = this.webSafe
        if (myWebSafe != null) {
            if (this.pageLoading) {
                this.pageLoading = false
                myWebSafe.stopLoading()
            }
            MainUtil.destroyWebViewNow(this.webSafe)
            this.webSafe = null
        }
        this.hostActivity = null
        this.appContext = null
        this.localeListener = null
        this.dialogFrame = null
        this.otherItems = null
        this.localeItems = null
        this.localeNames = null
        this.pendingJs = null
        this.localeTag = null
        this.baseUrl = null
        this.transExecutor = null
    }

    fun runAsync(runnable: Runnable?) {
        var executorServiceH = this.transExecutor
        if (executorServiceH == null || executorServiceH.isShutdown() || executorServiceH.isTerminated()) {
            executorServiceH = MainApp.Companion.executor(this.appContext)
            if (executorServiceH == null) {
                return
            } else {
                this.transExecutor = executorServiceH
            }
        }
        try {
            executorServiceH.execute(runnable)
        } catch (unused: Exception) {
        }
    }

    private inner class LocalWebViewClient : WebViewClient() {
        override fun onPageFinished(webView: WebView?, url: String?) {
            val mainTransLocale = this@MainTransLocale
            if (mainTransLocale.webSafe != null) {
                mainTransLocale.pageLoading = false
                mainTransLocale.loadUrl(url)
                if (!mainTransLocale.finishHandled && !TextUtils.isEmpty(url) && (url != mainTransLocale.lastFinishedUrl)) {
                    mainTransLocale.finishHandled = true
                    mainTransLocale.lastFinishedUrl = url
                    val myDialogRelative: DialogFrame? = mainTransLocale.dialogFrame
                    if (myDialogRelative != null) {
                        myDialogRelative.post(mainTransLocale.ClearCacheTask())
                    }
                }
                mainTransLocale.jsReady = true
                if (TextUtils.isEmpty(mainTransLocale.pendingJs)) {
                    return
                }
                val pendingJs = mainTransLocale.pendingJs
                mainTransLocale.jsReady = false
                mainTransLocale.pendingJs = null
                MainUtil.evalJsNow(mainTransLocale.webSafe, pendingJs, true)
            }
        }

        override fun onPageStarted(webView: WebView?, url: String?, bitmap: Bitmap?) {
            val mainTransLocale = this@MainTransLocale
            if (mainTransLocale.webSafe == null) {
                return
            }
            mainTransLocale.pageLoading = true
            mainTransLocale.loadUrl(url)
        }

        override fun onRenderProcessGone(
            webView: WebView?,
            renderProcessGoneDetail: RenderProcessGoneDetail?
        ): Boolean {
            val mainTransLocale = this@MainTransLocale
            mainTransLocale.webSafe = null
            MainUtil.onRenderGone(webView, renderProcessGoneDetail)
            val myDialogRelative: DialogFrame? = mainTransLocale.dialogFrame
            if (myDialogRelative == null) {
                return true
            }
            myDialogRelative.post(mainTransLocale.DeliverNamesTask())
            return true
        }

        override fun shouldInterceptRequest(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): WebResourceResponse? {
            val mainTransLocale = this@MainTransLocale
            if (mainTransLocale.webSafe != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                mainTransLocale.loadUrl(webResourceRequest.getUrl().toString())
            }
            return null
        }

        override fun shouldOverrideUrlLoading(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): Boolean {
            val mainTransLocale = this@MainTransLocale
            if (mainTransLocale.webSafe != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                val url = webResourceRequest.getUrl().toString()
                if (!TextUtils.isEmpty(url)) {
                    mainTransLocale.loadUrl(url)
                }
            }
            return false
        }
    }
}
