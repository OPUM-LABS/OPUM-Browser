package com.mycompany.app.main

import android.view.View

import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.quick.QuickAdapter.QuickItem
import android.content.Context
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
import com.mycompany.app.data.DataNews
import com.mycompany.app.data.DataNews.Companion.instance
import com.mycompany.app.data.DataSearch.Companion.instance
import com.mycompany.app.data.DataTrans.Companion.instance
import com.mycompany.app.data.DataUtil.addUri
import com.mycompany.app.data.book.DataBookSearch.Companion.instance
import com.mycompany.app.db.DbUtil.queryById
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.db.book.DbBookAlbum.Companion.cached; import com.mycompany.app.db.book.DbBookAlbum.Companion.get
import com.mycompany.app.db.book.DbBookCmp.Companion.cached; import com.mycompany.app.db.book.DbBookCmp.Companion.get
import com.mycompany.app.db.book.DbBookLocale.Companion.cached; import com.mycompany.app.db.book.DbBookLocale.Companion.get
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.db.book.DbBookPdf.Companion.cached; import com.mycompany.app.db.book.DbBookPdf.Companion.get
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.down.DownParseKakao.parseItemText
import com.mycompany.app.down.DownParseReddit.parseRedditItem
import com.mycompany.app.pref.PrefFloat.Companion.get
import com.mycompany.app.pref.PrefMain.Companion.get
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.pref.PrefSub.Companion.get
import com.mycompany.app.pref.PrefUtil.showDetail
import com.mycompany.app.pref.PrefUtil.reversed
import com.mycompany.app.pref.PrefUtil.setSingleLine
import com.mycompany.app.pref.PrefVideo.Companion.get
import com.mycompany.app.pref.PrefZtri.Companion.get
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.view.MyWebSafe
import org.jsoup.Jsoup
import java.util.concurrent.ExecutorService

@Suppress("deprecation")
class MainTransNews(
    mainActivity: MainActivity?,
    context: Context?,
    viewGroup: ViewGroup?,
    list: MutableList<QuickItem?>?,
    transNewsListener: TransNewsListener?
) {
    @JvmField
    var hostActivity: MainActivity? = null
    @JvmField
    var appContext: Context? = null
    @JvmField
    var newsListener: TransNewsListener? = null
    @JvmField
    var parentView: ViewGroup? = null
    @JvmField
    var newsItems: MutableList<QuickItem?>? = null
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
    var observeUrl: String? = null
    @JvmField
    var baseUrl: String? = null
    @JvmField
    var retryPending: Boolean = false
    @JvmField
    var retryCount: Int = 0
    @JvmField
    var transExecutor: ExecutorService? = null
    @JvmField
    var targetCount: Int = 0
    @JvmField
    var secretMode: Boolean = false
    @JvmField
    var earlyMode: Boolean = false
    @JvmField
    var stopped: Boolean = false
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
            val mainTransNews = this@MainTransNews
            if (mainTransNews.parentView == null || mainTransNews.webSafe != null) {
                return
            }
            if (mainTransNews.hostActivity != null) {
                myWebSafe = MyWebSafe(mainTransNews.hostActivity)
                mainTransNews.webSafe = myWebSafe
            } else {
                if (mainTransNews.appContext == null) {
                    return
                }
                myWebSafe = MyWebSafe(mainTransNews.appContext)
                mainTransNews.webSafe = myWebSafe
            }
            MainApp.Companion.resumeWebTimers(mainTransNews.appContext, myWebSafe)
            mainTransNews.webSafe!!.setVisibility(View.INVISIBLE)
            mainTransNews.parentView!!.addView(mainTransNews.webSafe, 0, ViewGroup.LayoutParams(-1, -2))
            mainTransNews.parentView!!.post(mainTransNews.CountTargetsTask())
        }
    }

    internal inner class CountTargetsTask : Runnable {

        internal inner class TargetCountStep : Runnable {

            internal inner class TargetCountTask : Runnable {

                internal inner class ReportCountStep : Runnable {
                    override fun run() {
                        val mainTransNews = this@MainTransNews
                        if (mainTransNews.webSafe == null) {
                            return
                        }
                        val transNewsListener = mainTransNews.newsListener
                        if (transNewsListener != null) {
                            mainTransNews.targetCount = transNewsListener.targetCount()
                        }
                        mainTransNews.runAsync(mainTransNews.TransScriptTask())
                    }
                }

                override fun run() {
                    val countTargetsTask = this@CountTargetsTask
                    val mainTransNews = this@MainTransNews
                    val myWebSafe = mainTransNews.webSafe
                    if (myWebSafe != null) {
                        mainTransNews.urlLoading = true
                        myWebSafe.addJavascriptInterface(mainTransNews.WebAppInterface(), "android")
                        val viewGroup = this@MainTransNews.parentView
                        if (viewGroup == null) {
                            return
                        }
                        viewGroup.post(ReportCountStep())
                    }
                }
            }

            override fun run() {
                val mainTransNews = this@MainTransNews
                val myWebSafe = mainTransNews.webSafe
                if (myWebSafe != null) {
                    myWebSafe.setWebViewClient(mainTransNews.LocalWebViewClient())
                    val viewGroup = mainTransNews.parentView
                    if (viewGroup == null) {
                        return
                    }
                    viewGroup.post(TargetCountTask())
                }
            }
        }

        override fun run() {
            val mainTransNews = this@MainTransNews
            val myWebSafe = mainTransNews.webSafe
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
                val viewGroup = mainTransNews.parentView
                if (viewGroup == null) {
                    return
                }
                viewGroup.post(TargetCountStep())
            }
        }
    }

    internal inner class ClearCacheTask : Runnable {
        override fun run() {
            val mainTransNews = this@MainTransNews
            MainUtil.clearWebCache(mainTransNews.webSafe)
            mainTransNews.finishHandled = false
        }
    }

    internal inner class ReportFailedTask : Runnable {
        override fun run() {
            val mainTransNews = this@MainTransNews
            val transNewsListener = mainTransNews.newsListener
            if (transNewsListener != null) {
                transNewsListener.onDone(false)
            }
            mainTransNews.retryPending = false
        }
    }

    internal inner class RemoveJsBridgeTask : Runnable {
        override fun run() {
            val mainTransNews = this@MainTransNews
            val myWebSafe = mainTransNews.webSafe
            if (myWebSafe == null) {
                return
            }
            mainTransNews.urlLoading = false
            myWebSafe.removeJavascriptInterface("android")
        }
    }

    internal inner class AddJsBridgeTask : Runnable {
        override fun run() {
            val mainTransNews = this@MainTransNews
            val myWebSafe = mainTransNews.webSafe
            if (myWebSafe == null) {
                return
            }
            mainTransNews.urlLoading = true
            myWebSafe.addJavascriptInterface(mainTransNews.WebAppInterface(), "android")
        }
    }

    internal inner class TransScriptTask : Runnable {

        internal inner class EvalTransScriptStep : Runnable {

            internal inner class TransScriptJsTask : Runnable {

                override fun run() {
                    val pendingJs: String?
                    val transScriptTask = this@TransScriptTask
                    val mainTransNews = this@MainTransNews
                    if (mainTransNews.webSafe != null) {
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
                        mainTransNews.pendingJs = pendingJs
                        val owner = this@MainTransNews
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
                val mainTransNews = this@MainTransNews
                val pendingHtml = mainTransNews.pendingHtml
                mainTransNews.pendingHtml = null
                val myWebSafe: MyWebSafe? = if (TextUtils.isEmpty(pendingHtml)) null else mainTransNews.webSafe
                if (TextUtils.isEmpty(pendingHtml) || myWebSafe == null) {
                    return
                }
                MainUtil.loadHtml(myWebSafe, mainTransNews.baseUrl, pendingHtml)
                mainTransNews.runAsync(TransScriptJsTask())
            }
        }

        override fun run() {
            val pendingHtml: String?
            var targetCount: Int
            val mainTransNews = this@MainTransNews
            val list = mainTransNews.newsItems
            val size = if (list == null) 0 else list.size
            if (list == null || size == 0) {
                pendingHtml = null
            } else {
                val sb =
                    StringBuilder("<!DOCTYPE html><html><head><meta charset=\"utf-8\"/><meta name='viewport' content='width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no'/><style>body{margin:0;}div{margin:0;position:absolute;max-width:100%;height:auto;}p,h1,h2{margin:0;font-size:10px;}</style></head><body>")
                for (index in 0..<size) {
                    val secretMode = mainTransNews.secretMode
                    mainTransNews.secretMode = !secretMode
                    val targetCountNow = mainTransNews.targetCount
                    if (secretMode) {
                        targetCount = targetCountNow + index
                        if (targetCount >= size) {
                            targetCount = targetCountNow - 1
                            mainTransNews.secretMode = secretMode
                        }
                    } else {
                        targetCount = targetCountNow - index
                        if (targetCount < 0) {
                            targetCount = targetCountNow + 1
                            mainTransNews.secretMode = secretMode
                        }
                    }
                    mainTransNews.targetCount = targetCount
                    val quickItem: QuickItem? = if (targetCount >= 0 && targetCount < size) (list.get(targetCount)) else null
                    if (targetCount >= 0 && targetCount < size && quickItem != null && quickItem.itemKind.toInt() == 7) {
                        sb.append("<div id='")
                        sb.append(quickItem.newsIndex)
                        sb.append("'><p>")
                        sb.append(quickItem.label)
                        sb.append("</p><h1>")
                        sb.append(quickItem.summary)
                        sb.append("</h1></div>")
                    }
                }
                sb.append("</body></html>")
                pendingHtml = sb.toString()
            }
            mainTransNews.pendingHtml = pendingHtml
            mainTransNews.baseUrl = MainUtil.httpsUrl("soul_title_", null)
            val myWebSafe = mainTransNews.webSafe
            if (myWebSafe == null) {
                return
            }
            myWebSafe.post(EvalTransScriptStep())
        }
    }

    internal inner class EvalLongPressJsTask(val outer: MainTransNews,
                                         val script: String?
    ) : Runnable {
        override fun run() {
            MainUtil.evalLongPressJs(this.outer.webSafe, this.script)
        }
    }

    internal inner class ReadResultTask : Runnable {

        internal inner class ReportDoneStep : Runnable {
            override fun run() {
                val transNewsListener = this@MainTransNews.newsListener
                if (transNewsListener != null) {
                    transNewsListener.onDone(true)
                }
            }
        }

        internal inner class ReadAgainStep : Runnable {
            override fun run() {
                val myWebSafe = this@MainTransNews.webSafe
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

        internal inner class ReportResultStep : Runnable {
            override fun run() {
                val mainTransNews = this@MainTransNews
                val transNewsListener = mainTransNews.newsListener
                if (transNewsListener != null) {
                    transNewsListener.onDone(false)
                }
                mainTransNews.retryPending = false
            }
        }

        /**
         * Reads the translated news HTML ([.y]) back into the news items (div id = item index):
         * m = 1 unchanged / not translated, 2 translated (title and description). Retries up to
         * 5 times while items are still untranslated, then reports completion.
         */
        override fun run() {
            val news = this@MainTransNews
            val html = news.docHtml
            news.docHtml = null
            news.stopped = false
            var complete = false
            try {
                if (!TextUtils.isEmpty(html)) {
                    val doc = Jsoup.parse(html!!, news.baseUrl!!)
                    DataNews.instance(news.appContext).newsPick = ZtwoSettings.newsPick.value
                    val divs = doc.select("div")
                    if (divs.size != 0) {
                        val earlyStop = !news.earlyMode && news.retryCount < 5
                        var pending = false
                        for (div in divs) {
                            val index = MainUtil.parseColorSafe(div.id())
                            val item: QuickItem? =
                                if (news.newsItems != null && index >= 0 && index < news.newsItems!!.size)
                                    news.newsItems!!.get(index)
                                else
                                    null
                            if (item == null || item.itemKind.toInt() != 7) {
                                continue
                            }
                            val p = div.selectFirst("p")
                            if (p != null) {
                                if (p.selectFirst("font") == null) {
                                    item.transState = 1
                                } else {
                                    val pageUrl = p.text()
                                    if (TextUtils.isEmpty(pageUrl)) {
                                        item.transState = 1
                                    } else {
                                        val h1 = div.selectFirst("h1")
                                        val title = if (h1 != null) h1.text() else null
                                        if (MainUtil.sameObject(item.label, pageUrl) && MainUtil.sameObject(
                                                item.summary,
                                                title
                                            )
                                        ) {
                                            item.transState = 1
                                        } else {
                                            item.transState = 2
                                            item.transTitle = pageUrl
                                            item.sourceName = title
                                        }
                                    }
                                }
                            }
                            if (item.transState.toInt() != 2) {
                                pending = true
                                if (earlyStop) {
                                    break
                                }
                            } else {
                                news.stopped = true
                            }
                        }
                        complete = !pending
                    }
                }
            } catch (unused: Exception) {
                complete = false
            } catch (unused: LinkageError) {
                complete = false
            } catch (unused: OutOfMemoryError) {
                complete = false
            }
            if (!complete) {
                if (news.earlyMode && news.stopped) {
                    // First pass produced some translations: show them while retrying.
                    news.earlyMode = false
                    if (news.webSafe == null) {
                        return
                    }
                    news.webSafe!!.post(ReportDoneStep())
                }
                if (news.retryCount < 5) {
                    news.retryCount++
                    if (news.webSafe != null) {
                        news.webSafe!!.postDelayed(ReadAgainStep(), 100L)
                    }
                    return
                }
            }
            if (news.webSafe != null) {
                news.webSafe!!.post(ReportResultStep())
            }
        }
    }

    interface TransNewsListener {
        fun onDone(isOk: Boolean)

        fun targetCount(): Int
    }

    private inner class WebAppInterface {

        inner class PageReadyStep : Runnable {

            internal inner class EvalJsTask : Runnable {
                override fun run() {
                    val myWebSafe = this@MainTransNews.webSafe
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
                val mainTransNews = this@MainTransNews
                if (mainTransNews.observeState.toInt() != 1) {
                    val observeUrl = mainTransNews.observeUrl
                    mainTransNews.observeUrl = null
                    if (!TextUtils.isEmpty(observeUrl)) {
                        if (TextUtils.isEmpty(observeUrl)) {
                            return
                        }
                        mainTransNews.runAsync(EvalLongPressJsTask(mainTransNews, observeUrl))
                    } else {
                        if (!mainTransNews.observeDone || mainTransNews.retryPending) {
                            return
                        }
                        mainTransNews.retryPending = true
                        mainTransNews.retryCount = 0
                        val myWebSafe = mainTransNews.webSafe
                        if (myWebSafe != null) {
                            myWebSafe.postDelayed(EvalJsTask(), 100L)
                        }
                    }
                }
            }
        }

        @JavascriptInterface
        fun onDocHtml(docHtml: String?) {
            val mainTransNews = this@MainTransNews
            if (mainTransNews.webSafe == null) {
                return
            }
            mainTransNews.docHtml = docHtml
            mainTransNews.runAsync(mainTransNews.ReadResultTask())
        }

        @JavascriptInterface
        fun onObserDet(innerText: String?, state: Int) {
            val mainTransNews = this@MainTransNews
            if (state == 0) {
                mainTransNews.observeState = 1.toByte()
            } else {
                mainTransNews.observeState = 3.toByte()
                mainTransNews.observeDone = state == 2
                if (TextUtils.isEmpty(ZtwoSettings.newsPick.value)) {
                    ZtwoSettings.edit(mainTransNews.appContext) { set(ZtwoSettings.newsPick, innerText) }
                }
                if (MainUtil.sameObject(mainTransNews.observeUrl, innerText)) {
                    mainTransNews.observeUrl = null
                }
            }
            val viewGroup = mainTransNews.parentView
            if (viewGroup == null) {
                return
            }
            viewGroup.post(PageReadyStep())
        }
    }

    init {
        run init@{
        if (context == null || viewGroup == null) {
            return@init
        }
        this.hostActivity = mainActivity
        this.appContext = context
        this.newsListener = transNewsListener
        this.parentView = viewGroup
        this.newsItems = list
        this.earlyMode = true
        this.observeState = 1.toByte()
        this.observeUrl = ZtwoSettings.newsPick.value
        viewGroup.post(CreateWebTask())

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
        this.newsListener = null
        this.parentView = null
        this.newsItems = null
        this.pendingJs = null
        this.observeUrl = null
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
            val mainTransNews = this@MainTransNews
            if (mainTransNews.webSafe != null) {
                mainTransNews.pageLoading = false
                mainTransNews.loadUrl(url)
                if (!mainTransNews.finishHandled && !TextUtils.isEmpty(url) && (url != mainTransNews.lastFinishedUrl)) {
                    mainTransNews.finishHandled = true
                    mainTransNews.lastFinishedUrl = url
                    val viewGroup = mainTransNews.parentView
                    if (viewGroup != null) {
                        viewGroup.post(mainTransNews.ClearCacheTask())
                    }
                }
                mainTransNews.jsReady = true
                if (TextUtils.isEmpty(mainTransNews.pendingJs)) {
                    return
                }
                val pendingJs = mainTransNews.pendingJs
                mainTransNews.jsReady = false
                mainTransNews.pendingJs = null
                MainUtil.evalJsNow(mainTransNews.webSafe, pendingJs, true)
            }
        }

        override fun onPageStarted(webView: WebView?, url: String?, bitmap: Bitmap?) {
            val mainTransNews = this@MainTransNews
            if (mainTransNews.webSafe == null) {
                return
            }
            mainTransNews.pageLoading = true
            mainTransNews.loadUrl(url)
        }

        override fun onRenderProcessGone(
            webView: WebView?,
            renderProcessGoneDetail: RenderProcessGoneDetail?
        ): Boolean {
            val mainTransNews = this@MainTransNews
            mainTransNews.webSafe = null
            MainUtil.onRenderGone(webView, renderProcessGoneDetail)
            val viewGroup = mainTransNews.parentView
            if (viewGroup == null) {
                return true
            }
            viewGroup.post(mainTransNews.ReportFailedTask())
            return true
        }

        override fun shouldInterceptRequest(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): WebResourceResponse? {
            val mainTransNews = this@MainTransNews
            if (mainTransNews.webSafe != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                mainTransNews.loadUrl(webResourceRequest.getUrl().toString())
            }
            return null
        }

        override fun shouldOverrideUrlLoading(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): Boolean {
            val mainTransNews = this@MainTransNews
            if (mainTransNews.webSafe != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                val url = webResourceRequest.getUrl().toString()
                if (!TextUtils.isEmpty(url)) {
                    mainTransNews.loadUrl(url)
                }
            }
            return false
        }
    }
}
