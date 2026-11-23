package com.mycompany.app.quick

import android.view.View

import java.util.ArrayList
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.text.TextUtils
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import com.mycompany.app.data.DataNews
import com.mycompany.app.data.DataNews.Companion.instance
import com.mycompany.app.data.book.DataBookRecent.Companion.instance
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.isHttpUrl
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.main.MainUtil.destroyWebViewNow
import com.mycompany.app.main.MainUtil.indexOfWord
import com.mycompany.app.main.MainUtil.unescapeJavaText
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.quick.QuickAdapter.QuickItem
import com.mycompany.app.view.MyWebSafe

import java.util.Collections

@Suppress("deprecation")
class QuickNewsGoogle(rfcDates: Boolean) : QuickNews(rfcDates) {
    @JvmField
    var listTitles: String? = null
    @JvmField
    var listLinks: String? = null
    @JvmField
    var listImages: String? = null
    @JvmField
    var listDates: String? = null
    @JvmField
    var dataJson: String? = null
    @JvmField
    var cachedList: MutableList<QuickAdapter.QuickItem?>? = null
    @JvmField
    var resultList: MutableList<QuickAdapter.QuickItem?>? = null
    @JvmField
    var scanPos: Int = 0
    @JvmField
    var hostActivity: MainActivity? = null
    @JvmField
    var parentView: ViewGroup? = null
    @JvmField
    var webSafe: MyWebSafe? = null
    @JvmField
    var pageLoading: Boolean = false
    @JvmField
    var urlLoading: Boolean = false
    @JvmField
    var pageFinished: Boolean = false
    @JvmField
    var progressDone: Boolean = false
    @JvmField
    var retryCount: Int = 0
    @JvmField
    var foundNew: Boolean = false
    @JvmField
    var fewAccepted: Boolean = false
    @JvmField
    var newsItems: ArrayList<QuickAdapter.QuickItem?>? = null
    @JvmField
    var hasImages: Boolean = false
    @JvmField
    var lastDate: String? = null
    @JvmField
    var lastTime: Long = 0
    @JvmField
    var finishHandled: Boolean = false
    @JvmField
    var lastFinishedUrl: String? = null
    @JvmField
    var listIds: String? = null

    internal inner class CreateWebTask : Runnable {
        override fun run() {
            val quickNewsGoogle = this@QuickNewsGoogle
            if (quickNewsGoogle.hostActivity == null || quickNewsGoogle.parentView == null || quickNewsGoogle.webSafe != null) {
                return
            }
            val myWebSafe = MyWebSafe(quickNewsGoogle.hostActivity!!)
            quickNewsGoogle.webSafe = myWebSafe
            MainApp.resumeWebTimers(quickNewsGoogle.appContext, myWebSafe)
            quickNewsGoogle.webSafe!!.setVisibility(View.INVISIBLE)
            quickNewsGoogle.parentView!!.addView(quickNewsGoogle.webSafe, 0, ViewGroup.LayoutParams(-1, -1))
            val handler = quickNewsGoogle.mainHandler
            if (handler != null) {
                handler.post(quickNewsGoogle.LoadNewsTask())
            }
        }
    }

    internal inner class DeliverCachedTask : Runnable {
        override fun run() {
            val quickNewsGoogle = this@QuickNewsGoogle
            val list = quickNewsGoogle.cachedList
            quickNewsGoogle.cachedList = null
            val newsListListener = quickNewsGoogle.newsListener
            if (newsListListener != null) {
                newsListListener.onTranslated(list)
            }
        }
    }

    internal inner class QueryLaterTask : Runnable {
        override fun run() {
            val quickNewsGoogle = this@QuickNewsGoogle
            if (quickNewsGoogle.webSafe == null) {
                return
            }
            quickNewsGoogle.runAsync(quickNewsGoogle.QueryItemsTask())
        }
    }

    internal inner class DeliverResultTask : Runnable {
        override fun run() {
            val quickNewsGoogle = this@QuickNewsGoogle
            val list = quickNewsGoogle.resultList
            quickNewsGoogle.resultList = null
            val newsListListener = quickNewsGoogle.newsListener
            if (newsListListener != null) {
                newsListListener.onList(list, quickNewsGoogle.newsItems != null)
                quickNewsGoogle.newsListener = null
            }
        }
    }

    internal inner class LoadNewsTask : Runnable {

        internal inner class LoadNewsStep : Runnable {

            internal inner class LoadUrlTask : Runnable {

                internal inner class LoadFeedStep : Runnable {
                    override fun run() {
                        val quickNewsGoogle = this@QuickNewsGoogle
                        val myWebSafe = quickNewsGoogle.webSafe
                        if (myWebSafe == null) {
                            return
                        }
                        myWebSafe.loadUrl(quickNewsGoogle.feedUrl!!)
                    }
                }

                override fun run() {
                    val loadNewsTask = this@LoadNewsTask
                    val quickNewsGoogle = this@QuickNewsGoogle
                    val myWebSafe = quickNewsGoogle.webSafe
                    if (myWebSafe != null) {
                        quickNewsGoogle.urlLoading = true
                        myWebSafe.addJavascriptInterface(
                            quickNewsGoogle.WebAppInterface(),
                            "android"
                        )
                        val handler = this@QuickNewsGoogle.mainHandler
                        if (handler == null) {
                            return
                        }
                        handler.post(LoadFeedStep())
                    }
                }
            }

            override fun run() {
                val quickNewsGoogle = this@QuickNewsGoogle
                val myWebSafe = quickNewsGoogle.webSafe
                if (myWebSafe != null) {
                    myWebSafe.setWebViewClient(quickNewsGoogle.LocalWebViewClient())
                    myWebSafe.setWebChromeClient(quickNewsGoogle.LocalChromeClient())
                    val handler = quickNewsGoogle.mainHandler
                    if (handler == null) {
                        return
                    }
                    handler.post(LoadUrlTask())
                }
            }
        }

        override fun run() {
            val quickNewsGoogle = this@QuickNewsGoogle
            val myWebSafe = quickNewsGoogle.webSafe
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
                MainApp.setAcceptCookies(quickNewsGoogle.appContext, false)
                myWebSafe.setOverScrollMode(2)
                val handler = quickNewsGoogle.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(LoadNewsStep())
            }
        }
    }

    internal inner class ClearCacheTask : Runnable {
        override fun run() {
            val quickNewsGoogle = this@QuickNewsGoogle
            MainUtil.clearWebCache(quickNewsGoogle.webSafe)
            quickNewsGoogle.finishHandled = false
        }
    }

    internal inner class DeliverEmptyTask : Runnable {
        override fun run() {
            val quickNewsGoogle = this@QuickNewsGoogle
            val newsListListener = quickNewsGoogle.newsListener
            if (newsListListener != null) {
                newsListListener.onList(null, false)
                quickNewsGoogle.newsListener = null
            }
        }
    }

    internal inner class RemoveJsBridgeTask : Runnable {
        override fun run() {
            val quickNewsGoogle = this@QuickNewsGoogle
            val myWebSafe = quickNewsGoogle.webSafe
            if (myWebSafe == null) {
                return
            }
            quickNewsGoogle.urlLoading = false
            myWebSafe.removeJavascriptInterface("android")
        }
    }

    internal inner class AddJsBridgeTask : Runnable {
        override fun run() {
            val quickNewsGoogle = this@QuickNewsGoogle
            val myWebSafe = quickNewsGoogle.webSafe
            if (myWebSafe == null) {
                return
            }
            quickNewsGoogle.urlLoading = true
            myWebSafe.addJavascriptInterface(quickNewsGoogle.WebAppInterface(), "android")
        }
    }

    internal inner class QueryItemsTask : Runnable {
        override fun run() {
            val quickNewsGoogle = this@QuickNewsGoogle
            val myWebSafe = quickNewsGoogle.webSafe
            if (myWebSafe == null) {
                return
            }
            if (quickNewsGoogle.newsItems != null) {
                evalJs(
                    myWebSafe,
                    "(function(){var dsl=null;var sds=document.querySelectorAll(\"script[class*='ds:']\");if(sds&&(sds.length>0)){for(var i=0;i<sds.length;i++){var sde=sds[i].innerText;if(sde&&sde.includes('http')){if(!dsl||(sde.length>dsl.length)){dsl=sde;}}}}android.onLoadData(dsl);})();",
                    true
                )
            } else {
                evalJs(
                    myWebSafe,
                    "function myArt(doc,tag){if(doc){var ele=doc.querySelectorAll(tag);if((ele!=null)&&(ele.length>0)){return ele;}}return null;}function myAr2(doc){if(doc){var ele=myArt(doc,\"div[role='listitem']\");if(ele){return ele;}ele=myArt(doc,\"div[class*='Iisyzf']\");if(ele){return ele;}ele=myArt(doc,\"div[jsdata]\");if(ele){return ele;}ele=myArt(doc,\"article\");if(ele){return ele;}}return null;}function myRef(doc){if(doc){var ele=doc.querySelector(\"a[href]\");if(ele&&ele.href&&(ele.href.indexOf('http')==0)){return ele.href;}}return null;}function myIco(doc){if(doc){var ele=doc.querySelector(\"img[src^='https://']\");if(ele&&ele.src){return ele.src;}}return 'na';}function myDat(doc){if(doc){var ele=doc.querySelector(\"time[datetime]\");if(ele&&ele.dateTime){return ele.dateTime;}}return 'na';}(function(){var ttl=null;var srl=null;var rel=null;var icl=null;var dtl=null;var ats=myAr2(document);if((ats!=null)&&(ats.length>0)){for(var i=0;i<ats.length;i++){var atc=ats[i];var tds=atc.querySelectorAll(\"a[data-n-tid]\");if((tds==null)||(tds.length==0))continue;var tex1=null;var tex2=null;for(var j=0;j<tds.length;j++){var text=tds[j].innerText;if(!text)continue;if(!tex1){tex1=text;}else if(!tex2){tex2=text;}else{break;}}if(!tex1)continue;var len1=tex1.length;if(len1==0)continue;if(!tex2)continue;var len2=tex2.length;if(len2==0)continue;var tte;var src;if(len1>len2){tte=tex1;src=tex2;}else{tte=tex2;src=tex1;}var ref=myRef(atc);if(!ref)continue;var ico=myIco(atc);var dat=myDat(atc);if(!ttl){ttl=tte;}else{ttl+='!@!'+tte;}if(!srl){srl=src;}else{srl+='!@!'+src;}if(!rel){rel=ref;}else{rel+='!@!'+ref;}if(!icl){icl=ico;}else{icl+='!@!'+ico;}if(!dtl){dtl=dat;}else{dtl+='!@!'+dat;}}}android.onLoadList(ttl,srl,rel,icl,dtl);})();",
                    true
                )
            }
        }
    }

    internal inner class MakeItemsTask : Runnable {

        /**
         * Turns the article fields scraped from the Google News page (five "!@!"-joined lists)
         * into news items. With new articles they replace the list; otherwise the cached list is
         * kept (retrying once with an earlier page when fewer than 10 were found).
         */
        override fun run() {
            val news = this@QuickNewsGoogle
            val ids = news.listIds
            val titles = news.listTitles
            val links = news.listLinks
            val images = news.listImages
            val dates = news.listDates
            news.listIds = null
            news.listTitles = null
            news.listLinks = null
            news.listImages = null
            news.listDates = null
            news.foundNew = false
            news.newsItems = null
            var items: ArrayList<QuickAdapter.QuickItem?>? = null
            if (!TextUtils.isEmpty(ids)) {
                try {
                    val array: Array<String?> =
                        ids!!.split("!@!".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val kinds: Array<String?> =
                        titles!!.split("!@!".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    val linkArray: Array<String?> =
                        links!!.split("!@!".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val imageArray: Array<String?> =
                        images!!.split("!@!".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                    val dateArray: Array<String?> =
                        dates!!.split("!@!".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    val count = array.size
                    if (count != 0 && kinds.size == count && linkArray.size == count && imageArray.size == count && dateArray.size == count) {
                        val cached = DataNews.instance(news.appContext).items
                        for (k in 0..<count) {
                            if (news.appContext == null) {
                                items = null
                                break
                            }
                            val item = news.makeItem(array[k], kinds[k], linkArray[k], imageArray[k], dateArray[k], (cached))
                            if (item != null) {
                                if (items == null) {
                                    items = ArrayList()
                                }
                                items.add(item)
                            }
                        }
                    }
                } catch (unused: Exception) {
                }
            }
            if (!news.foundNew) {
                // Nothing new.
                if (items == null || items.isEmpty()) {
                    news.deliverList(null)
                } else if (items.size >= 10 || news.fewAccepted) {
                    news.deliverList(news.sortedNew(items))
                } else {
                    news.fewAccepted = true
                    news.retryCount--
                    news.deliverList(null)
                }
                return
            }
            if (items == null || items.isEmpty()) {
                news.deliverList(null)
                return
            }
            news.newsItems = items
            news.retryCount = 0
            if (news.webSafe != null) {
                news.runAsync(news.QueryItemsTask())
            }
            var copy: ArrayList<QuickAdapter.QuickItem?>? = ArrayList()
            for (obj in items) {
                if (news.appContext == null) {
                    copy = null
                    break
                }
                if (obj != null) {
                    copy!!.add(obj)
                }
            }
            news.cachedList = news.sortedNew(copy)
            if (news.mainHandler != null) {
                news.mainHandler!!.post(news.DeliverCachedTask())
            }
        }
    }

    internal inner class LocateTask : Runnable {
        /**
         * Finds each scraped article ([newsItems]) in the page's embedded data ([dataJson]) and takes from its section the article's
         * own link ([QuickItem.color]) and a picture ([QuickItem.imageUrl]).
         */
        override fun run() {
            val news = this@QuickNewsGoogle
            val data = news.dataJson
            news.dataJson = null
            val items = news.newsItems
            if (items != null && !items.isEmpty() && !data.isNullOrEmpty()) {
                try {
                    if (locate(news, items, data)) enrich(news, items, data)
                } catch (_: Exception) {
                }
            }
            if (!news.hasImages) news.deliverList(null) else news.deliverList(news.sortedList(items))
        }

        /** Marks where each article sits in [data] ([QuickItem.blockStart] … [QuickItem.keyEnd]); true if any was found. */
        private fun locate(news: QuickNewsGoogle, items: ArrayList<QuickAdapter.QuickItem?>, data: String): Boolean {
            var found = false
            var from = 0
            for (item2 in items) {
                if (news.appContext == null) return false
                val item = item2 ?: continue
                // the last part of the article's Google News link, else its title as it is escaped in the data
                var key = linkKey(item.link)
                var at = find(data, key, from)
                if (at < 0) {
                    key = escapeJs(item.label)
                    at = find(data, key, from)
                }
                if (at < 0 || key == null) continue
                item.blockStart = at
                item.keyEnd = at + key.length
                from = item.keyEnd
                found = true
            }
            return found
        }

        /** [key] in [data] from [from] on, else before it; −1 if missing. */
        private fun find(data: String, key: String?, from: Int): Int {
            if (key.isNullOrEmpty()) return -1
            val at = data.indexOf(key, from)
            if (at >= 0 || from <= 0) return at
            return MainUtil.indexOfWord(data, 0, from, key)
        }

        /** The part of [link] after its last '/' and before '?'. */
        private fun linkKey(link: String?): String? {
            if (link.isNullOrEmpty()) return null
            val slash = link.lastIndexOf('/')
            if (slash == -1) return null
            val q = link.lastIndexOf('?')
            val end = if (q != -1) q else link.length
            return if (slash + 1 < end) link.substring(slash + 1, end) else null
        }

        /** Fills link and picture of each located article from its section of [data]. */
        private fun enrich(news: QuickNewsGoogle, items: ArrayList<QuickAdapter.QuickItem?>, data: String) {
            try {
                Collections.sort<QuickItem?>((items as MutableList<QuickItem?>), SortPos())
            } catch (_: Exception) {
            }
            for (idx in items.indices) {
                if (news.appContext == null) return
                val item = items[idx] ?: continue
                val start = item.keyEnd
                if (start == 0) continue
                // the section runs to the next located article, or to the end of the data
                val next = items.getOrNull(idx + 1)
                val end = if (next != null && next.blockStart > start) next.blockStart else data.length
                if (item.imageUrl.isNullOrEmpty()) {
                    item.imageUrl = quotedUrlAround(data, imageDot(data, start, end))
                    if (!item.imageUrl.isNullOrEmpty()) news.hasImages = true
                }
                if (item.path.isNullOrEmpty()) {
                    item.path = news.findUrl(start, end, data, item.imageUrl, false)
                    if (item.path.isNullOrEmpty()) continue
                    news.hasImages = true
                }
                if (item.imageUrl.isNullOrEmpty()) {
                    item.imageUrl = news.findUrl(start, end, data, item.path, true)
                    if (item.imageUrl.isNullOrEmpty()) item.imageUrl = item.thumbUrl
                    if (item.imageUrl.isNullOrEmpty()) item.imageUrl = quotedUrlAround(data, MainUtil.indexOfWord(data, start, end, "favicon"))
                }
                if (!item.imageUrl.isNullOrEmpty()) news.hasImages = true
            }
        }

        /** Index of the first '.' in [from, to) that starts an image extension ("jpg", "png", …), or −1. */
        private fun imageDot(data: String, from: Int, to: Int): Int {
            var pos = from
            while (pos + 1 < to) {
                if (data[pos] == '.' && TLD_PARTS.any { it != null && data.startsWith(it, pos + 1) }) return pos
                pos++
            }
            return -1
        }

        /** The quoted "http… URL that contains position [at] (unescaped), or null. */
        private fun quotedUrlAround(data: String, at: Int): String? {
            if (at < 5) return null
            val open = data.lastIndexOf("\"http", at)
            if (open < 0) return null
            val close = data.indexOf("\"", at)
            return if (close > open + 1) MainUtil.unescapeJavaText(data.substring(open + 1, close)) else null
        }
    }

    private inner class LocalChromeClient : WebChromeClient() {
        override fun onProgressChanged(webView: WebView?, newProgress: Int) {
            val quickNewsGoogle = this@QuickNewsGoogle
            if (newProgress == 100) {
                quickNewsGoogle.progressDone = true
            }
            val newsListListener = quickNewsGoogle.newsListener
            if (newsListListener != null) {
                newsListListener.onProgress(newProgress)
            }
        }
    }

    private inner class WebAppInterface {
        @JavascriptInterface
        fun onLoadData(dataJson: String?) {
            val dataJsonEmpty = TextUtils.isEmpty(dataJson)
            val quickNewsGoogle = this@QuickNewsGoogle
            if (dataJsonEmpty) {
                quickNewsGoogle.deliverList(null)
            } else {
                quickNewsGoogle.dataJson = dataJson
                quickNewsGoogle.runAsync(quickNewsGoogle.LocateTask())
            }
        }

        @JavascriptInterface
        fun onLoadList(listIds: String?, listTitles: String?, listLinks: String?, listImages: String?, listDates: String?) {
            val listIdsEmpty = TextUtils.isEmpty(listIds)
            val quickNewsGoogle = this@QuickNewsGoogle
            if (listIdsEmpty) {
                quickNewsGoogle.deliverList(null)
                return
            }
            quickNewsGoogle.listIds = listIds
            quickNewsGoogle.listTitles = listTitles
            quickNewsGoogle.listLinks = listLinks
            quickNewsGoogle.listImages = listImages
            quickNewsGoogle.listDates = listDates
            quickNewsGoogle.runAsync(quickNewsGoogle.MakeItemsTask())
        }
    }

    override fun start(
        context: Context?,
        handler: Handler?,
        url: String?,
        newsListListener: NewsListListener?
    ) {
        super.start(context, handler, url, newsListListener)
        val mainHandler = this.mainHandler
        if (mainHandler == null) {
            return
        }
        mainHandler.post(CreateWebTask())
    }

    override fun release() {
        MainApp.applyCookieSetting(this.appContext)
        super.release()
        val myWebSafe = this.webSafe
        if (myWebSafe != null) {
            if (this.pageLoading) {
                this.pageLoading = false
                myWebSafe.stopLoading()
            }
            destroyWebViewNow(this.webSafe)
            this.webSafe = null
        }
        this.hostActivity = null
        this.parentView = null
        this.newsItems = null
        this.lastDate = null
    }

    fun loadUrl(pageUrl: String?) {
        if (this.webSafe != null) {
            val isHttp = isHttpUrl(pageUrl)
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

    fun deliverList(list: MutableList<QuickAdapter.QuickItem?>?) {
        var list = list
        if (list == null || list.isEmpty()) {
            val retryCount = this.retryCount
            if (retryCount < 20) {
                if (this.progressDone) {
                    this.retryCount = retryCount + 1
                }
                val handler = this.mainHandler
                if (handler != null) {
                    handler.postDelayed(QueryLaterTask(), 200L)
                    return
                }
                return
            }
            list = sortedList(this.newsItems)
        }
        this.resultList = list
        val handler = this.mainHandler
        if (handler == null) {
            return
        }
        handler.post(DeliverResultTask())
    }

    fun sortedList(arrayList: ArrayList<QuickAdapter.QuickItem?>?): MutableList<QuickAdapter.QuickItem?>? {
        if (arrayList == null || arrayList.isEmpty()) {
            return QuickNews.Companion.withHeader(null)
        }
        try {
            Collections.sort<QuickItem?>((arrayList as MutableList<QuickAdapter.QuickItem?>), SortNews())
        } catch (unused: Exception) {
        }
        val listA: MutableList<QuickAdapter.QuickItem?> = QuickNews.Companion.withHeader(arrayList)
        val list = DataNews.instance(this.appContext).items
        val size = if (list != null) listA.size else 0
        if (list != null && size != 0 && size == list.size) {
            for (index in 0..<size) {
                if (this.appContext == null) {
                    return null
                }
                val entry = list.get(index)
                val quickItem: QuickItem? = if (entry != null) (listA.get(index)) else null
                if (entry != null && quickItem != null) {
                    if (!TextUtils.isEmpty(quickItem.path)) {
                        entry.path = quickItem.path
                        entry.link = null
                    }
                    entry.imageUrl = quickItem.imageUrl
                }
            }
        }
        return list
    }

    fun sortedNew(arrayList: ArrayList<QuickAdapter.QuickItem?>?): MutableList<QuickAdapter.QuickItem?> {
        if (arrayList != null && !arrayList.isEmpty()) {
            try {
                Collections.sort<QuickItem?>((arrayList as MutableList<QuickAdapter.QuickItem?>), SortNews())
            } catch (unused: Exception) {
            }
        }
        val listA: MutableList<QuickAdapter.QuickItem?> = QuickNews.Companion.withHeader(arrayList)
        com.mycompany.app.glide.ThumbLoader.stopAll()
        DataNews.instance(this.appContext).setItems(listA)
        return listA
    }

    /**
     * News item for one scraped article. Articles already in `cached` (same id) are copied
     * from there; new ones get a sortable time (digits of the ISO date, or a running counter when
     * missing) and set [.r].
     */
    fun makeItem(
        id: String?,
        title: String?,
        link: String?,
        image: String?,
        date: String?,
        cached: MutableList<QuickAdapter.QuickItem?>?
    ): QuickItem? {
        var image = image
        var date = date
        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(title) || TextUtils.isEmpty(link)) {
            return null
        }
        if (cached != null) {
            for (old2 in cached) {
                val old = old2
                if (old != null && id == old.label) {
                    val item = QuickItem()
                    item.itemKind = 7
                    item.label = id
                    item.summary = title
                    item.path = old.path
                    item.link = old.link
                    item.imageUrl = old.imageUrl
                    item.thumbUrl = old.thumbUrl
                    item.sourceHtml = old.sourceHtml
                    item.time = old.time
                    return item
                }
            }
        }
        if ("na" == image) {
            image = null
        }
        if ("na" == date) {
            date = null
        }
        var time: Long = 0
        if (!TextUtils.isEmpty(date)) {
            date = date!!.replace("-", ".").replace("T", " ").replace("Z", "")
            if (!TextUtils.isEmpty(date)) {
                try {
                    time = date.replace("[^0-9]".toRegex(), "").toLong()
                } catch (unused: Exception) {
                    time = 0
                }
            }
        }
        if (time == 0L) {
            time = ++this.lastTime
            date = this.lastDate
        } else {
            this.lastDate = date
            this.lastTime = time
        }
        this.foundNew = true
        val item = QuickItem()
        item.itemKind = 7
        item.label = id
        item.summary = title
        item.link = link
        item.thumbUrl = image
        item.sourceHtml = date
        item.time = time
        return item
    }

    fun findUrl(scanPos: Int, end: Int, html: String?, imageUrl: String?, decode: Boolean): String? {
        var scanPos = scanPos
        var foundUrl: String?
        var urlStart: Int = 0
        this.scanPos = scanPos
        var url: String? = null
        while (scanPos < end) {
            val httpIndex = indexOfWord(html, scanPos, end, "\"http")
            val quoteEnd = if (httpIndex < 0) 0 else html!!.indexOf("\"", httpIndex + 5)
            if (httpIndex < 0 || quoteEnd <= ((httpIndex + 1).also { urlStart = it })) {
                foundUrl = null
            } else {
                this.scanPos = quoteEnd
                foundUrl = MainUtil.unescapeJavaText(html!!.substring(urlStart, quoteEnd))
            }
            if (TextUtils.isEmpty(foundUrl)) {
                break
            }
            var found = false
            if (decode) {
                val length = if (foundUrl != null) foundUrl.length else 0
                if (foundUrl != null && length >= 8 && (foundUrl != imageUrl) && !foundUrl.contains("/faviconV2?")) {
                    // Accept if the URL mentions "image"/"img"; otherwise reject if it mentions "article"/"amp".
                    var index = 7
                    var isImage = false
                    while (index < length) {
                        val next = index + 1
                        if (next == length) {
                            break
                        }
                        val cCharAt = foundUrl.get(index)
                        if (cCharAt == 'i' || cCharAt == 'I') {
                            if (foundUrl.startsWith("mage", next) || foundUrl.startsWith("mg", next)) {
                                isImage = true
                                break
                            }
                        } else if (!found && ((cCharAt == 'a' || cCharAt == 'A') && (foundUrl.startsWith(
                                "rticle",
                                next
                            ) || foundUrl.startsWith("mp", next)))
                        ) {
                            found = true
                        }
                        index = next
                    }
                    found = isImage || !found
                }
                if (found) {
                    return foundUrl
                }
            } else if (foundUrl!!.startsWith("https://lh3.googleusercontent.com")) {
                url = foundUrl
            } else {
                if (!TextUtils.isEmpty(foundUrl) && foundUrl != imageUrl) {
                    found = !foundUrl.contains("/faviconV2?")
                }
                if (found) {
                    return foundUrl
                }
            }
            val scanPosNow = this.scanPos
            if (scanPosNow <= scanPos) {
                return url
            }
            scanPos = scanPosNow
        }
        return url
    }

    private inner class LocalWebViewClient : WebViewClient() {
        override fun onPageFinished(webView: WebView?, url: String?) {
            val quickNewsGoogle = this@QuickNewsGoogle
            if (quickNewsGoogle.webSafe != null) {
                quickNewsGoogle.pageLoading = false
                quickNewsGoogle.loadUrl(url)
                if (!quickNewsGoogle.finishHandled && !TextUtils.isEmpty(url) && (url != quickNewsGoogle.lastFinishedUrl)) {
                    quickNewsGoogle.finishHandled = true
                    quickNewsGoogle.lastFinishedUrl = url
                    val handler = quickNewsGoogle.mainHandler
                    if (handler != null) {
                        handler.post(quickNewsGoogle.ClearCacheTask())
                    }
                }
                if (quickNewsGoogle.pageFinished) {
                    return
                }
                quickNewsGoogle.pageFinished = true
                quickNewsGoogle.retryCount = 0
                if (quickNewsGoogle.webSafe == null) {
                    return
                }
                quickNewsGoogle.runAsync(quickNewsGoogle.QueryItemsTask())
            }
        }

        override fun onPageStarted(webView: WebView?, url: String?, bitmap: Bitmap?) {
            val quickNewsGoogle = this@QuickNewsGoogle
            if (quickNewsGoogle.webSafe == null) {
                return
            }
            quickNewsGoogle.pageLoading = true
            quickNewsGoogle.loadUrl(url)
        }

        override fun onRenderProcessGone(
            webView: WebView?,
            renderProcessGoneDetail: RenderProcessGoneDetail?
        ): Boolean {
            val quickNewsGoogle = this@QuickNewsGoogle
            quickNewsGoogle.webSafe = null
            onRenderGone(webView, renderProcessGoneDetail)
            val handler = quickNewsGoogle.mainHandler
            if (handler == null) {
                return true
            }
            handler.post(quickNewsGoogle.DeliverEmptyTask())
            return true
        }

        override fun shouldInterceptRequest(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): WebResourceResponse? {
            val quickNewsGoogle = this@QuickNewsGoogle
            if (quickNewsGoogle.webSafe != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                quickNewsGoogle.loadUrl(webResourceRequest.getUrl().toString())
            }
            return null
        }

        override fun shouldOverrideUrlLoading(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): Boolean {
            val quickNewsGoogle = this@QuickNewsGoogle
            if (quickNewsGoogle.webSafe != null && webResourceRequest != null && webResourceRequest.getUrl() != null) {
                val url = webResourceRequest.getUrl().toString()
                if (!TextUtils.isEmpty(url)) {
                    quickNewsGoogle.loadUrl(url)
                }
            }
            return false
        }
    }

    companion object {
        @JvmField
        val TLD_PARTS: Array<String?> =
            arrayOf<String?>("jpg", "jpeg", "webp", "png", "gif", "JPG", "WEBP", "PNG", "GIF")

        @JvmStatic
        fun escapeJs(label: String?): String? {
            if (label == null) {
                return null
            }
            return label.replace("\\", "\\\\").replace("\"", "\\\"").replace("&", "\\u0026")
                .replace("=", "\\u003d").replace(">", "\\u003e")
        }
    }
}
