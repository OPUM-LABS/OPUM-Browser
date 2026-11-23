package com.mycompany.app.main

import android.view.View

import com.mycompany.app.web.WebReadTask.ReadItem
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.dialog.DialogSetDesk.SetDeskListener
import java.util.ArrayList
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
import com.mycompany.app.data.DataUtil.addUri
import com.mycompany.app.db.DbUtil.deleteById
import com.mycompany.app.db.DbUtil.queryById
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.db.book.DbBookAlbum.Companion.cached; import com.mycompany.app.db.book.DbBookAlbum.Companion.get
import com.mycompany.app.db.book.DbBookCmp.Companion.cached; import com.mycompany.app.db.book.DbBookCmp.Companion.get
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.db.book.DbBookLocale.Companion.cached; import com.mycompany.app.db.book.DbBookLocale.Companion.get
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.db.book.DbBookPdf.Companion.cached; import com.mycompany.app.db.book.DbBookPdf.Companion.get
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.down.DownParseDzen.parseItemText
import com.mycompany.app.down.DownParseKakao.parseItemText
import com.mycompany.app.down.DownParseKakao.segmentUrls
import com.mycompany.app.down.DownParseReddit.parseRedditItem
import com.mycompany.app.down.DownParseReddit.redditSegments
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefAlbum.Companion.saveTrans
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.pref.PrefUtil.showDetail
import com.mycompany.app.pref.PrefUtil.showRecent
import com.mycompany.app.pref.PrefUtil.reversed
import com.mycompany.app.pref.PrefUtil.setSingleLine
import com.mycompany.app.view.MyWebSafe
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.concurrent.ExecutorService

@Suppress("deprecation")
class MainTransText(
    mainActivity: MainActivity?,
    context: Context?,
    viewGroup: ViewGroup?,
    arrayList: ArrayList<com.mycompany.app.web.WebReadTask.ReadItem?>?,
    targetCount: Int,
    baseUrl: String?,
    setDeskListener: SetDeskListener?
) {
    @JvmField
    var hostActivity: MainActivity? = null
    @JvmField
    var appContext: Context? = null
    @JvmField
    var deskListener: SetDeskListener? = null
    @JvmField
    var parentView: ViewGroup? = null
    @JvmField
    var textItems: ArrayList<com.mycompany.app.web.WebReadTask.ReadItem?>? = null
    @JvmField
    var targetCount: Int = 0
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
            val mainTransText = this@MainTransText
            if (mainTransText.parentView == null || mainTransText.webSafe != null) {
                return
            }
            if (mainTransText.hostActivity != null) {
                myWebSafe = MyWebSafe(mainTransText.hostActivity)
                mainTransText.webSafe = myWebSafe
            } else {
                if (mainTransText.appContext == null) {
                    return
                }
                myWebSafe = MyWebSafe(mainTransText.appContext)
                mainTransText.webSafe = myWebSafe
            }
            MainApp.Companion.resumeWebTimers(mainTransText.appContext, myWebSafe)
            mainTransText.webSafe!!.setVisibility(View.INVISIBLE)
            var targetCount = mainTransText.targetCount
            if (targetCount <= 0) {
                targetCount = -2
            }
            mainTransText.parentView!!.addView(mainTransText.webSafe, 0, ViewGroup.LayoutParams(-1, targetCount))
            mainTransText.parentView!!.post(mainTransText.LoadTextTask())
        }
    }

    internal inner class LoadTextTask : Runnable {

        internal inner class LoadTextStep : Runnable {

            internal inner class PrepareWebStep : Runnable {

                internal inner class RunTransScriptStep : Runnable {
                    override fun run() {
                        val mainTransText = this@MainTransText
                        if (mainTransText.webSafe == null) {
                            return
                        }
                        mainTransText.runAsync(mainTransText.TransScriptTask())
                    }
                }

                override fun run() {
                    val loadTextTask = this@LoadTextTask
                    val mainTransText = this@MainTransText
                    val myWebSafe = mainTransText.webSafe
                    if (myWebSafe != null) {
                        mainTransText.urlLoading = true
                        myWebSafe.addJavascriptInterface(mainTransText.WebAppInterface(), "android")
                        val viewGroup = this@MainTransText.parentView
                        if (viewGroup == null) {
                            return
                        }
                        viewGroup.post(RunTransScriptStep())
                    }
                }
            }

            override fun run() {
                val mainTransText = this@MainTransText
                val myWebSafe = mainTransText.webSafe
                if (myWebSafe != null) {
                    myWebSafe.setWebViewClient(mainTransText.LocalWebViewClient())
                    val viewGroup = mainTransText.parentView
                    if (viewGroup == null) {
                        return
                    }
                    viewGroup.post(PrepareWebStep())
                }
            }
        }

        override fun run() {
            val mainTransText = this@MainTransText
            val myWebSafe = mainTransText.webSafe
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
                val viewGroup = mainTransText.parentView
                if (viewGroup == null) {
                    return
                }
                viewGroup.post(LoadTextStep())
            }
        }
    }

    internal inner class ClearCacheTask : Runnable {
        override fun run() {
            val mainTransText = this@MainTransText
            MainUtil.clearWebCache(mainTransText.webSafe)
            mainTransText.finishHandled = false
        }
    }

    internal inner class ReportFailedTask : Runnable {
        override fun run() {
            val mainTransText = this@MainTransText
            val setDeskListener: SetDeskListener? = mainTransText.deskListener
            if (setDeskListener != null) {
                setDeskListener.onChanged(false)
            }
            mainTransText.retryPending = false
        }
    }

    internal inner class RemoveJsBridgeTask : Runnable {
        override fun run() {
            val mainTransText = this@MainTransText
            val myWebSafe = mainTransText.webSafe
            if (myWebSafe == null) {
                return
            }
            mainTransText.urlLoading = false
            myWebSafe.removeJavascriptInterface("android")
        }
    }

    internal inner class AddJsBridgeTask : Runnable {
        override fun run() {
            val mainTransText = this@MainTransText
            val myWebSafe = mainTransText.webSafe
            if (myWebSafe == null) {
                return
            }
            mainTransText.urlLoading = true
            myWebSafe.addJavascriptInterface(mainTransText.WebAppInterface(), "android")
        }
    }

    internal inner class TransScriptTask : Runnable {

        internal inner class EvalTransScriptStep : Runnable {

            internal inner class TransScriptJsTask : Runnable {

                override fun run() {
                    val pendingJs: String?
                    val transScriptTask = this@TransScriptTask
                    val mainTransText = this@MainTransText
                    if (mainTransText.webSafe != null) {
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
                        mainTransText.pendingJs = pendingJs
                        val owner = this@MainTransText
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
                val mainTransText = this@MainTransText
                val pendingHtml = mainTransText.pendingHtml
                mainTransText.pendingHtml = null
                val myWebSafe: MyWebSafe? = if (TextUtils.isEmpty(pendingHtml)) null else mainTransText.webSafe
                if (TextUtils.isEmpty(pendingHtml) || myWebSafe == null) {
                    return
                }
                MainUtil.loadHtml(myWebSafe, mainTransText.baseUrl, pendingHtml)
                mainTransText.runAsync(TransScriptJsTask())
            }
        }

        override fun run() {
            val pendingHtml: String?
            val mainTransText = this@MainTransText
            val arrayList = mainTransText.textItems
            if (arrayList == null || arrayList.isEmpty()) {
                pendingHtml = null
            } else {
                val sb =
                    StringBuilder("<!DOCTYPE html><html><head><meta charset=\"utf-8\"/><meta name='viewport' content='width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no'/><style>body{margin:0;}div{margin:0;position:absolute;max-width:100%;height:auto;}p{margin:0;font-size:10px;}</style></head><body>")
                val size = arrayList.size
                var index = 0
                while (index < size) {
                    val obj: Any? = arrayList.get(index)
                    index++
                    val readItem: ReadItem? = obj as ReadItem?
                    if (readItem != null) {
                        sb.append("<div id='")
                        sb.append(readItem.index)
                        sb.append("'><p>")
                        sb.append(readItem.articleTitle)
                        sb.append("</p></div>")
                    }
                }
                sb.append("</body></html>")
                pendingHtml = sb.toString()
            }
            mainTransText.pendingHtml = pendingHtml
            if (TextUtils.isEmpty(mainTransText.baseUrl)) {
                mainTransText.baseUrl = MainUtil.httpsUrl("soul_trans_", null)
            }
            val myWebSafe = mainTransText.webSafe
            if (myWebSafe == null) {
                return
            }
            myWebSafe.post(EvalTransScriptStep())
        }
    }

    internal inner class EvalLongPressJsTask(val outer: MainTransText,
                                         val script: String?
    ) : Runnable {
        override fun run() {
            MainUtil.evalLongPressJs(this.outer.webSafe, this.script)
        }
    }

    internal inner class ReadResultTask : Runnable {

        internal inner class ReadAgainStep : Runnable {
            override fun run() {
                val myWebSafe = this@MainTransText.webSafe
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

        internal inner class ReportResultStep(val outer: ReadResultTask?,
                                             val deskMode: Boolean
        ) : Runnable {
            override fun run() {
                val mainTransText = this@MainTransText
                val setDeskListener: SetDeskListener? = mainTransText.deskListener
                if (setDeskListener != null) {
                    setDeskListener.onChanged(this.deskMode)
                }
                mainTransText.retryPending = false
            }
        }

        override fun run() {
            var document: Document?
            val mainTransText = this@MainTransText
            val docHtml = mainTransText.docHtml
            mainTransText.docHtml = null
            var isRetry = false
            try {
                if (!TextUtils.isEmpty(docHtml)) {
                    document = Jsoup.parse(docHtml!!, mainTransText.baseUrl!!)
                    val elementsSelect: Elements? = document.select("div")
                    if (elementsSelect != null && elementsSelect.size != 0) {
                        val canRetry = mainTransText.retryCount < 5
                        val size = elementsSelect.size
                        var index = 0
                        while (index < size) {
                            val rawElement: Any? = elementsSelect.get(index)
                            index++
                            val element = rawElement as Element?
                            if (element != null) {
                                val color = MainUtil.parseColorSafe(element.id())
                                val arrayList = mainTransText.textItems
                                val readItem: ReadItem? =
                                    if (arrayList == null || color < 0 || color >= arrayList.size) null else arrayList.get(
                                        color
                                    )
                                if (readItem == null) {
                                    continue
                                } else {
                                    val elementSelectFirst = element.selectFirst("p")
                                    if (elementSelectFirst != null) {
                                        readItem.transText = elementSelectFirst.text()
                                        if (elementSelectFirst.selectFirst("font") == null) {
                                            readItem.mediaKind = 1.toByte()
                                        } else {
                                            readItem.mediaKind = 2.toByte()
                                        }
                                    }
                                    if (readItem.mediaKind.toInt() != 2) {
                                        isRetry = true
                                        if (canRetry) {
                                            break
                                        }
                                    } else {
                                        continue
                                    }
                                }
                            }
                        }
                        isRetry = !isRetry
                    }
                }
            } catch (unused: Exception) {
                document = null
            } catch (unused: LinkageError) {
                document = null
            } catch (unused: OutOfMemoryError) {
                document = null
            }
            val retryCount = if (isRetry) 0 else mainTransText.retryCount
            if (isRetry || retryCount >= 5) {
                val myWebSafe = mainTransText.webSafe
                if (myWebSafe == null) {
                    return
                }
                myWebSafe.post(ReportResultStep(this, isRetry))
                return
            }
            mainTransText.retryCount = retryCount + 1
            val myWebSafe = mainTransText.webSafe
            if (myWebSafe != null) {
                myWebSafe.postDelayed(ReadAgainStep(), 100L)
            }
        }
    }

    private inner class WebAppInterface {

        inner class PageReadyStep : Runnable {

            internal inner class EvalJsTask : Runnable {
                override fun run() {
                    val myWebSafe = this@MainTransText.webSafe
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
                val mainTransText = this@MainTransText
                if (mainTransText.observeState.toInt() != 1) {
                    val observeUrl = mainTransText.observeUrl
                    mainTransText.observeUrl = null
                    if (!TextUtils.isEmpty(observeUrl)) {
                        if (TextUtils.isEmpty(observeUrl)) {
                            return
                        }
                        mainTransText.runAsync(EvalLongPressJsTask(mainTransText, observeUrl))
                    } else {
                        if (!mainTransText.observeDone || mainTransText.retryPending) {
                            return
                        }
                        mainTransText.retryPending = true
                        mainTransText.retryCount = 0
                        val myWebSafe = mainTransText.webSafe
                        if (myWebSafe != null) {
                            myWebSafe.postDelayed(EvalJsTask(), 100L)
                        }
                    }
                }
            }
        }

        @JavascriptInterface
        fun onDocHtml(docHtml: String?) {
            val mainTransText = this@MainTransText
            if (mainTransText.webSafe == null) {
                return
            }
            mainTransText.docHtml = docHtml
            mainTransText.runAsync(mainTransText.ReadResultTask())
        }

        @JavascriptInterface
        fun onObserDet(innerText: String?, state: Int) {
            val mainTransText = this@MainTransText
            if (state == 0) {
                mainTransText.observeState = 1.toByte()
            } else {
                mainTransText.observeState = 3.toByte()
                mainTransText.observeDone = state == 2
                if (TextUtils.isEmpty(AlbumSettings.transPick.value)) {
                    AlbumSettings.editLocal { set(AlbumSettings.transPick, innerText) }
                    AlbumSettings.editLocal { set(AlbumSettings.transCode, "") }
                    PrefAlbum.saveTrans(mainTransText.appContext)
                }
                if (MainUtil.sameObject(mainTransText.observeUrl, innerText)) {
                    mainTransText.observeUrl = null
                }
            }
            val viewGroup = mainTransText.parentView
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
        this.deskListener = setDeskListener
        this.parentView = viewGroup
        this.textItems = arrayList
        this.targetCount = targetCount
        this.baseUrl = baseUrl
        this.observeState = 1.toByte()
        this.observeUrl = AlbumSettings.transPick.value
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
        this.deskListener = null
        this.parentView = null
        this.textItems = null
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
            val mainTransText = this@MainTransText
            if (mainTransText.webSafe != null) {
                mainTransText.pageLoading = false
                mainTransText.loadUrl(url)
                if (!mainTransText.finishHandled && !TextUtils.isEmpty(url) && (url != mainTransText.lastFinishedUrl)) {
                    mainTransText.finishHandled = true
                    mainTransText.lastFinishedUrl = url
                    val viewGroup = mainTransText.parentView
                    if (viewGroup != null) {
                        viewGroup.post(mainTransText.ClearCacheTask())
                    }
                }
                mainTransText.jsReady = true
                if (TextUtils.isEmpty(mainTransText.pendingJs)) {
                    return
                }
                val pendingJs = mainTransText.pendingJs
                mainTransText.jsReady = false
                mainTransText.pendingJs = null
                MainUtil.evalJsNow(mainTransText.webSafe, pendingJs, true)
            }
        }

        override fun onPageStarted(webView: WebView?, url: String?, bitmap: Bitmap?) {
            val mainTransText = this@MainTransText
            if (mainTransText.webSafe == null) {
                return
            }
            mainTransText.pageLoading = true
            mainTransText.loadUrl(url)
        }

        override fun onRenderProcessGone(
            webView: WebView?,
            renderProcessGoneDetail: RenderProcessGoneDetail?
        ): Boolean {
            val mainTransText = this@MainTransText
            mainTransText.webSafe = null
            MainUtil.onRenderGone(webView, renderProcessGoneDetail)
            val viewGroup = mainTransText.parentView
            if (viewGroup == null) {
                return true
            }
            viewGroup.post(mainTransText.ReportFailedTask())
            return true
        }

        override fun shouldInterceptRequest(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): WebResourceResponse? {
            val mainTransText = this@MainTransText
            if (mainTransText.webSafe != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                mainTransText.loadUrl(webResourceRequest.getUrl().toString())
            }
            return null
        }

        override fun shouldOverrideUrlLoading(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): Boolean {
            val mainTransText = this@MainTransText
            if (mainTransText.webSafe != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                val url = webResourceRequest.getUrl().toString()
                if (!TextUtils.isEmpty(url)) {
                    mainTransText.loadUrl(url)
                }
            }
            return false
        }
    }
}
