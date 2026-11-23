package com.mycompany.app.web

import android.content.Context
import com.mycompany.app.pref.store.AlbumSettings
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.URLUtil
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.data.DataUrl
import com.mycompany.app.data.DataUrl.ImgCntItem
import com.mycompany.app.dialog.DialogConfirm
import com.mycompany.app.dialog.DialogConfirm.DialogConfListener
import com.mycompany.app.dialog.DialogCreateAlbum
import com.mycompany.app.dialog.DialogDownList
import com.mycompany.app.dialog.DialogDownZip
import com.mycompany.app.dialog.DialogImageType
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.dialog.DialogSetImage.ChangedListener
import com.mycompany.app.main.JarBridge
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainItem.ViewItem
import com.mycompany.app.main.ViewItemListener
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.browser.ComposeFullDialog
import com.mycompany.app.web.WebEmgTask.EmgTaskListener
import com.mycompany.app.web.WebHmgTask.HmgTaskListener
import com.mycompany.app.web.WebLoadWrap.EmgLoadListener

import java.lang.ref.WeakReference
import java.util.Locale
import kotlin.math.max
import androidx.collection.LruCache

/**
 * The "Image list" screen for the two gallery sites ([kind] 1 = the `WebEmgTask` site, 2 = the `WebHmgTask` site):
 * the same screen as [WebGridDialog] (shared [PictureListScreen]), but the pictures come from the site's own page
 * through a private hidden `WebNestView` and its scraper task, and their real addresses are resolved one by one by a
 * [WebLoadWrap] loader (a "n / total" line is shown until all are known). (Replaced the old `WebEmgDialog` /
 * `WebHmgDialog`, which were removed on 2026-11-15.) NOT verified against the real sites (they are not reachable from the
 * development emulator): the load, scrape and resolve flows are ported 1:1 from the two old classes.
 */
class WebSiteDialog(
    webViewActivity: WebViewActivity?,
    urlParam: String?,
    private val siteKind: Int,
    webImgListener: WebGridDialog.WebImgListener?,
) : ComposeFullDialog(webViewActivity), PictureHost {
    /** The Download / ZIP / Album sheets, for `WebViewActivity.onActivityResult` and the permission callback. */
    @JvmField var downListDialog: DialogDownList? = null
    @JvmField var downZipDialog: DialogDownZip? = null
    @JvmField var albumDialog: DialogCreateAlbum? = null

    private val act: WebViewActivity? = webViewActivity
    private val ctx: Context = context
    private val listener = webImgListener
    private var url: String? = urlParam
    private var title: String? = null
    private var list: ArrayList<String?>? = null
    private var count: ImgCntItem? = null
    private var firstShow = true
    private var viewerBusy = false
    private var lastType = 0
    private var progressNow = -1
    private var progressSince = 0L
    private var delayWarned = false
    private var noList = false
    private var retry = false
    private var jsAdded = false
    private var pageClean = false
    private var lastPage: String? = null
    private var filteredCount = 0
    private var resolvedAll = false

    override val adapter = WebGridAdapter(webViewActivity, urlParam, siteKind)
    private var web: WebNestView? = null
    private var host: FrameLayout? = null
    private var emgTask: WebEmgTask? = null
    private var hmgTask: WebHmgTask? = null
    private var firstLoad: WebLoadWrap? = null
    private var lastLoad: WebLoadWrap? = null
    private var confirm: DialogConfirm? = null
    private var typeDialog: DialogImageType? = null
    private var typeTask: TypeTask? = null
    private val ui = Handler(Looper.getMainLooper())

    override var version by mutableIntStateOf(0)
        private set
    private var loadState by mutableIntStateOf(0)
    override var filtering by mutableStateOf(false)
        private set
    override var barVisible by mutableStateOf(true)
        private set
    override var progress by mutableFloatStateOf(-1f)
        private set
    private var status by mutableStateOf<String?>(null)
    override val thumbs: SnapshotStateMap<Int, Bitmap> = mutableStateMapOf()
    override val failed: SnapshotStateMap<Int, Boolean> = mutableStateMapOf()
    override val loaded: Boolean get() = loadState == 2 && typeTask == null
    override val statusText: String? get() = status

    private fun bump() { version++ }

    init {
        setPage {
            LaunchedEffect(Unit) { /* the bar is indeterminate until the list is there */ }
            PictureListScreen(this)
        }
        show()
        // properties declared further down (the task listeners) are only initialised after this block
        ui.post { start() }
    }

    // ------------------------------------------------------------------ site load (ported from WebEmgDialog / WebHmgDialog)

    private fun start() {
        val box = FrameLayout(ctx)
        host = box
        addContentView(box, ViewGroup.LayoutParams(1, 1))
        if (!URLUtil.isNetworkUrl(url)) {
            loadState = 2
            barVisible = false
            showList(AlbumSettings.imageType.value, list)
            return
        }
        val view = WebNestView(act)
        web = view
        view.setNoAutofill(true)
        view.setVisibility(View.INVISIBLE)
        box.addView(view, 0, ViewGroup.LayoutParams(-1, -1))
        if (siteKind == 1) emgTask = WebEmgTask(ctx, view, emgListener) else hmgTask = WebHmgTask(ctx, view, hmgListener)
        ui.post {
            val webView = web ?: return@post
            MainUtil.applyZoomSettings(webView, false)
            webView.setWebViewClient(LocalClient())
            addBridge(webView)
            ui.post {
                if (web == null) return@post
                barVisible = true
                ui.post {
                    val w2 = web ?: return@post
                    loadState = 0
                    w2.openUrl(url, null)
                }
            }
        }
    }

    private fun addBridge(webView: WebView) {
        jsAdded = true
        webView.addJavascriptInterface(Bridge(), "android")
    }

    private inner class Bridge {
        @JavascriptInterface
        fun onViewHtml(href: String?, innerHTML: String?) {
            web?.notifyHtml(href, innerHTML)
        }
    }

    /** The bridge is only wanted on pages the scraper reads; on other pages it is removed. */
    private fun checkBridge(pageUrl: String?) {
        val w = web ?: return
        val allowed = MainUtil.isHttpUrl(pageUrl)
        if (allowed) {
            if (jsAdded) { jsAdded = false; w.post { web?.removeJavascriptInterface("android") } }
            return
        }
        if (jsAdded) return
        jsAdded = true
        w.post { web?.addJavascriptInterface(Bridge(), "android") }
    }

    private fun taskProgress(): Int = if (siteKind == 1) emgTask?.loadProgress() ?: 0 else hmgTask?.loadProgress() ?: 0
    private fun taskStart() {
        if (siteKind == 1) emgTask?.readPage() else hmgTask?.readPage()
    }
    private fun taskError(code: Int) { if (siteKind == 1) emgTask?.checkNetError(code) else hmgTask?.checkNetError(code) }
    private fun taskStop() { if (siteKind == 1) { emgTask?.release(); emgTask = null } else { hmgTask?.release(); hmgTask = null } }
    private fun hasTask(): Boolean = if (siteKind == 1) emgTask != null else hmgTask != null

    /** Page progress; once past 30 % (or retried) the scraper is started. */
    private fun poll(newProgress: Int) {
        if (web == null || loadState == 2) return
        if (newProgress == -1) {
            progressNow = newProgress
            retry = true
        } else if (newProgress != 100) {
            if (progressNow == newProgress) {
                if (!delayWarned) {
                    val now = System.currentTimeMillis()
                    if (progressSince == 0L) progressSince = now
                    else if (now - progressSince > 5000) { delayWarned = true; MainUtil.showToast(ctx, R.string.server_delay) }
                }
                ui.postDelayed({ if (hasTask()) poll(taskProgress()) }, 400L)
                return
            }
            progressNow = newProgress
            progressSince = 0L
            if (newProgress < 30) {
                ui.postDelayed({ if (hasTask()) poll(taskProgress()) }, 400L)
                return
            }
        }
        if (loadState != 0) return
        loadState = 1
        ui.postDelayed({ if (hasTask() && loadState != 2) taskStart() }, 200L)
    }

    private val emgListener = object : EmgTaskListener {
        override fun onFailed() { loadState = 2; showList(AlbumSettings.imageType.value, list) }
        override fun onLoading() { if (loadState != 2) loadState = 1 }
        override fun onResult(arrayList: ArrayList<String?>?, position: Int, count: Int, title: String?) {
            if (emgTask == null || loadState == 2) return
            loadState = 0
            if (arrayList == null || arrayList.isEmpty() || count == 0 || title.isNullOrEmpty()) {
                noList = false
                val progress = emgTask!!.loadProgress()
                if (progress < 100) { poll(progress); return }
                else if (retry) { retry = false; poll(progress); return }
            }
            loadState = 2
            if (arrayList == null || arrayList.isEmpty()) {
                if (emgTask?.netError == true) MainUtil.showToast(ctx, R.string.server_error) else if (noList) MainUtil.showToast(ctx, R.string.check_network)
            }
            if (arrayList != null && !arrayList.isEmpty() && count != 0 && !title.isNullOrEmpty()) {
                setTitleFromPage()
                emgTask?.release(); emgTask = null
                web?.let { MainUtil.destroyWebView(it, true) }; web = null
                val names = ArrayList<String?>()
                val fallback = ArrayList<String?>()
                for (n in 0..<count) {
                    val name = n.toString() + ".jpg"
                    names.add(if (n == position) title else name)
                    fallback.add(name)
                }
                var minStr: String? = null; var maxStr: String? = null; var minAt = -1; var maxAt = -1
                for (minStr3 in arrayList) {
                    val minText = minStr3
                    val dash = if (!minText.isNullOrEmpty()) minText.lastIndexOf('-') else 0
                    val from = if (!minText.isNullOrEmpty() && dash != -1) dash + 1 else 0
                    val idx = if (!minText.isNullOrEmpty() && dash != -1 && from < minText.length) MainUtil.parseIntSafe(minText.substring(from)) - 1 else -1
                    if (idx != position && idx >= 0 && idx < names.size) {
                        fallback[idx] = minText
                        if (maxAt == -1) { maxAt = idx; minAt = idx; minStr = minText; maxStr = minText }
                        else {
                            if (idx < minAt) { minAt = idx; minStr = minText }
                            if (idx > maxAt) { maxAt = idx; maxStr = minText }
                        }
                    }
                }
                useList(names, fallback, ImgCntItem().also { it.jpgCount = count }, minStr, minAt, maxStr, maxAt)
            }
            showList(AlbumSettings.imageType.value, list)
        }
    }

    private val hmgListener = object : HmgTaskListener {
        override fun onFailed() { loadState = 2; showList(AlbumSettings.imageType.value, list) }
        override fun onLoading() { if (loadState != 2) loadState = 1 }
        override fun onResult(urls: ArrayList<String?>?, thumbs: ArrayList<String?>?, position: Int) {
            if (hmgTask == null || loadState == 2) return
            loadState = 0
            if (urls == null || urls.isEmpty()) {
                noList = false
                val progress = hmgTask!!.loadProgress()
                if (progress < 100) { poll(progress); return }
                else if (retry) { retry = false; poll(progress); return }
            }
            loadState = 2
            if (urls == null || urls.isEmpty()) {
                if (hmgTask?.netError == true) MainUtil.showToast(ctx, R.string.server_error) else if (noList) MainUtil.showToast(ctx, R.string.check_network)
            }
            if (urls != null && !urls.isEmpty()) {
                var name = MainUtil.siteNameOf(web?.getTitle(), true)
                if (name.isNullOrEmpty()) name = MainUtil.hostLabelUpper(MainUtil.hostNameOf(url!!, true))
                else if (name.length > 12 && name.endsWith(" | Hitomi.la")) name = name.substring(0, name.length - 12)
                title = name
                hmgTask?.release(); hmgTask = null
                web?.let { MainUtil.destroyWebView(it, true) }; web = null
                var first: String? = null; var last: String? = null; var firstAt = -1; var lastAt = -1
                for (firstAt2 in urls.indices) {
                    if (!URLUtil.isNetworkUrl(urls[firstAt2])) {
                        if (firstAt == -1) {
                            firstAt = firstAt2; first = thumbs!![firstAt2]; last = first; lastAt = firstAt2
                        } else {
                            if (firstAt2 < firstAt) { first = thumbs!![firstAt2]; firstAt = firstAt2 }
                            if (firstAt2 > lastAt) { last = thumbs!![firstAt2]; lastAt = firstAt2 }
                        }
                    }
                }
                val cnt = ImgCntItem().also { it.webpCount = urls.size }
                val data = DataUrl.instance(ctx)
                data.urls = urls
                data.thumbUrls = thumbs
                list = urls
                count = cnt
                startLoaders(first, firstAt, last, lastAt)
            }
            showList(AlbumSettings.imageType.value, list)
        }
    }

    private fun setTitleFromPage() {
        var name = MainUtil.siteNameOf(web?.getTitle(), true)
        if (name.isNullOrEmpty()) name = MainUtil.hostLabelUpper(MainUtil.hostNameOf(url!!, true))
        title = name
    }

    /** Emg: the list of names is built here, then the same loaders as Hmg start. */
    private fun useList(names: ArrayList<String?>, fallback: ArrayList<String?>, cnt: ImgCntItem, first: String?, firstAt: Int, last: String?, lastAt: Int) {
        val data = DataUrl.instance(ctx)
        @Suppress("UNCHECKED_CAST")
        data.urls = names as ArrayList<String?>?
        @Suppress("UNCHECKED_CAST")
        data.thumbUrls = fallback as ArrayList<String?>?
        list = names
        count = cnt
        startLoaders(first, firstAt, last, lastAt)
    }

    private fun startLoaders(first: String?, firstAt: Int, last: String?, lastAt: Int) {
        var nothing = true
        if (!first.isNullOrEmpty()) {
            firstLoad = WebLoadWrap.create(siteKind, act, host, first, firstAt, true, loaderListener)
            nothing = false
        }
        if (!last.isNullOrEmpty()) {
            lastLoad = WebLoadWrap.create(siteKind, act, host, last, lastAt, false, loaderListener)
            nothing = false
        }
        status = "0 / 0"
        if (nothing) refreshStatus()
    }

    private val loaderListener = object : EmgLoadListener {
        override fun onDone() {
            resolvedAll = true
            status = null
            bump()
        }

        override fun onImage(index: Int, url: String?) {
            val items = list
            if (items == null || index < 0 || index >= items.size) return
            items[index] = url
            adapter.updateUrl(index, url)
            thumbs.remove(index); failed.remove(index)
            refreshStatus()
            bump()
        }

        override fun onFailed() {
            if (resolvedAll) return
            Thread { syncFromViewer() }.start()
        }
    }

    /** Show "n / total" until every address is a real one. */
    private fun refreshStatus() {
        if (resolvedAll) { status = null; return }
        val items = list
        if (items == null || items.isEmpty()) { status = "0 / 0"; return }
        var have = 0
        for (o in items) if (URLUtil.isNetworkUrl(o)) have++
        if (have >= items.size) { resolvedAll = true; status = null } else status = MainUtil.countLabel(have, items.size)
    }

    /** The viewer resolves addresses too and leaves them in [DataUrl]; copy them over. */
    private fun syncFromViewer() {
        @Suppress("UNCHECKED_CAST")
        val fromViewer = DataUrl.instance(ctx).urls
        ui.post {
            val items = list
            if (resolvedAll || items == null || fromViewer == null || fromViewer.isEmpty() || fromViewer.size != items.size) return@post
            for (index in items.indices) items[index] = fromViewer[index]
            adapter.mergeUrls(fromViewer)
            thumbs.clear(); failed.clear()
            refreshStatus()
            bump()
        }
    }

    private inner class LocalClient : WebViewClient() {
        override fun onPageFinished(view: WebView?, loadedUrl: String?) {
            val w = web ?: return
            w.setWebLoading(false)
            if (!URLUtil.isNetworkUrl(loadedUrl)) {
                w.postDelayed({ web?.let { loadState = 0; it.openUrl(url, null) } }, 1000L)
                return
            }
            checkBridge(loadedUrl)
            if (!pageClean && !loadedUrl.isNullOrEmpty() && loadedUrl != lastPage) {
                pageClean = true
                lastPage = loadedUrl
                ui.post { MainUtil.clearWebCache(web); pageClean = false }
            }
            url = loadedUrl
            poll(-1)
        }

        override fun onPageStarted(view: WebView?, startUrl: String?, bitmap: Bitmap?) {
            val w = web ?: return
            w.setWebLoading(true)
            checkBridge(startUrl)
            if (URLUtil.isNetworkUrl(startUrl)) url = startUrl
        }

        override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
            super.onReceivedError(view, request, error)
            if (error == null) return
            taskError(error.getErrorCode())
        }

        override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
            web = null
            MainUtil.onRenderGone(view, detail)
            ui.post { dismiss() }
            return true
        }

        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            if (web != null && request != null && request.getUrl() != null) checkBridge(request.getUrl().toString())
            return null
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            if (web != null && request != null && request.getUrl() != null) {
                val url = request.getUrl().toString()
                if (url.isNotEmpty()) { checkBridge(url); web!!.openUrl(url, null) }
            }
            return true
        }
    }

    // ------------------------------------------------------------------ list, selection, actions (as WebGridDialog)

    private fun showList(type: Int, items: ArrayList<String?>?) {
        typeTask?.cancelled = true
        typeTask = null
        filteredCount = 0
        val have = items != null && items.size > 0
        val none = !have || type == 0
        if (have && type == 0) {
            filteredCount = items.size
            MainUtil.showToastText(ctx, String.format(Locale.US, ctx.getString(R.string.filtered_image), filteredCount))
        }
        if (!none && type != 126) {
            val task = TypeTask(this, items, type)
            typeTask = task
            task.start(ctx)
            return
        }
        adapter.setImages(if (none) null else items)
        thumbs.clear(); failed.clear()
        filtering = false
        barVisible = false
        bump()
    }

    override fun onSelectAll() {
        if (!loaded || !adapter.selectMode) return
        adapter.setAllChecked(!adapter.allChecked(), true)
        bump()
    }

    private fun enterSelect(index: Int, on: Boolean) {
        if (on == adapter.selectMode) return
        adapter.setSelectMode(index, on)
        bump()
    }

    override fun onTileLong(position: Int) {
        if (!loaded || position < 0 || position >= adapter.itemCount()) return
        if (status != null) return // old: no selecting until every address is known
        if (!adapter.selectMode) enterSelect(position, true) else { adapter.toggleChecked(position); bump() }
    }

    override fun onTileTap(position: Int) {
        if (!loaded || position < 0 || position >= adapter.itemCount()) return
        if (adapter.selectMode) { adapter.toggleChecked(position); bump(); return }
        if (viewerBusy) return
        viewerBusy = true
        ui.post { openViewer(position); viewerBusy = false }
    }

    private fun openViewer(index: Int) {
        val items = list
        if (act == null || (siteKind == 1 && listener == null)) return
        if (items == null || items.isEmpty()) { MainUtil.showToast(ctx, R.string.invalid_path); return }
        val intent = MainUtil.imageViewerIntent(ctx)
        intent.putExtra("EXTRA_TYPE", 12)
        intent.putExtra("EXTRA_NAME", title)
        intent.putExtra("EXTRA_INDEX", index)
        intent.putExtra("EXTRA_PAGE", WebGridDialog.viewerPage(act, adapter, index))
        intent.putExtra("EXTRA_REFERER", url)
        intent.putExtra("EXTRA_PRELOAD", true)
        intent.putExtra("EXTRA_LOAD_TYPE", siteKind)
        act.startActivity(intent)
    }

    private fun otherDialogOpen(): Boolean = confirm != null || typeDialog != null || downListDialog != null || downZipDialog != null || albumDialog != null

    private fun chosen(): ArrayList<String?>? = if (adapter.selectMode) adapter.checkedUrls() else adapter.imageUrls

    override fun onAction(which: Int) {
        if (!loaded) return
        if (AlbumSettings.guideDown.value) { askConfirm(which); return }
        runAction(which)
    }

    private fun runAction(which: Int) {
        val items = chosen()
        if (act == null || otherDialogOpen()) return
        if (url.isNullOrEmpty() || title.isNullOrEmpty()) { MainUtil.showToast(ctx, R.string.invalid_url); return }
        if (items == null || items.isEmpty()) { MainUtil.showToast(ctx, R.string.no_image); return }
        val done = object : DialogApplyListener { override fun onApply() { enterSelect(-1, false) } }
        when (which) {
            0 -> { val dialogDownList = DialogDownList(act, title, items, url, done); downListDialog = dialogDownList; dialogDownList.setOnDismissListener { downListDialog?.dismiss(); downListDialog = null } }
            1 -> { val dialogDownList = DialogDownZip(act, title, items, url, done); downZipDialog = dialogDownList; dialogDownList.setOnDismissListener { downZipDialog?.dismiss(); downZipDialog = null } }
            else -> { val dialogDownList = DialogCreateAlbum(act, title, items, url, done); albumDialog = dialogDownList; dialogDownList.setOnDismissListener { albumDialog?.dismiss(); albumDialog = null } }
        }
    }

    private fun askConfirm(which: Int) {
        if (!AlbumSettings.guideDown.value || act == null || otherDialogOpen()) return
        confirm?.dismiss()
        val dialogConfirm = DialogConfirm(act, object : DialogConfListener {
            override fun onResult(confirmed: Boolean) {
                if (confirmed) { AlbumSettings.edit(ctx) { set(AlbumSettings.guideDown, false) } }
                confirm?.dismiss(); confirm = null
            }
        })
        confirm = dialogConfirm
        dialogConfirm.setOnDismissListener { confirm?.dismiss(); confirm = null; runAction(which) }
    }

    override fun onFilter() {
        if (act == null || otherDialogOpen()) return
        typeDialog?.dismiss()
        val dialogImageType = DialogImageType(act, count, object : ChangedListener { override fun onChanged() { showList(AlbumSettings.imageType.value, list) } })
        typeDialog = dialogImageType
        dialogImageType.setOnDismissListener { typeDialog?.dismiss(); typeDialog = null }
    }

    override fun onRefresh() {
        val webNow = web
        if (webNow == null) { MainUtil.showToast(ctx, R.string.wait_retry); return }
        // old: the refresh icon had no action while the list was being scraped; reload the page
        if (loadState == 2) return
        loadState = 0
        poll(-1)
    }

    override fun onStatusClick() { MainUtil.showToast(ctx, R.string.wait_retry) }
    override fun onBack() { tryClose() }

    // ------------------------------------------------------------------ thumbnails

    override fun requestThumb(position: Int) {
        val adapterNow = adapter
        val url = if (adapterNow.validIndex(position)) adapterNow.imageUrls!!.get(position) else null
        if (url == null) return
        if (url.isEmpty()) { failed[position] = true; return }
        if (!URLUtil.isNetworkUrl(url)) return // not resolved yet: placeholder until the loader reports the address
        val cached = com.mycompany.app.glide.ImageCache.get(com.mycompany.app.glide.ImageCache.key(2, url))
        if (MainUtil.isBitmapOk(cached)) { thumbs[position] = cached!!; return }
        val item = ViewItem()
        item.itemType = 7.toByte()
        item.url = url
        item.referer = adapterNow.pageUrl
        item.position = position
        item.cacheType = 2
        com.mycompany.app.glide.ThumbLoader.loadAlone(item, object : ViewItemListener {
            override fun onFailed(item: ViewItem?, view: View?, reason: Exception?) {
                ui.post { failed[position] = true; firstLoad?.loadImage(position); lastLoad?.loadImage(position) }
            }

            override fun onComplete(item: ViewItem?, view: View?, bitmap: Bitmap?) {
                ui.post {
                    if (MainUtil.isBitmapOk(bitmap)) {
                        val shown: Bitmap = bitmap!! // checked by isBitmapOk
                        val imageSizes = adapter.imageSizes
                        if (imageSizes != null && position >= 0 && position < imageSizes.size) imageSizes[position] = if (shown.getWidth() > shown.getHeight()) 2 else 1
                        thumbs[position] = shown
                    } else failed[position] = true
                }
            }
        })
    }

    override fun badge(url: String?): String {
        if (adapter.imageType.toInt() == 2) return "WEBP"
        if (adapter.imageType.toInt() == 1 && !URLUtil.isNetworkUrl(url)) return "JPG"
        if (url.isNullOrEmpty()) return "None"
        val ext = MainUtil.fileExtFromName(url, true)
        return if (ext.isNullOrEmpty()) "None" else ext.uppercase()
    }

    // ------------------------------------------------------------------ members WebViewActivity calls

    /** Back: leaves select mode first, then closes. */
    fun tryClose() {
        if (!adapter.selectMode) dismiss() else enterSelect(-1, false)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { tryClose() }

    fun hasPopup(): Boolean = false
    fun refresh() { bump() }

    /** onResume: take over what the viewer resolved and re-apply a changed album filter. */
    fun onShowAgain() {
        if (!firstShow) {
            if (!resolvedAll && list != null) Thread { syncFromViewer() }.start()
            if (loaded && lastType != AlbumSettings.imageType.value && MainUtil.imageCountChanged(count, lastType, AlbumSettings.imageType.value)) showList(AlbumSettings.imageType.value, list)
        }
        lastType = AlbumSettings.imageType.value
        firstShow = false
    }

    fun closeChildren(isClosing: Boolean) {
        if (!isClosing) { lastType = AlbumSettings.imageType.value; return }
        confirm?.dismiss(); confirm = null
        typeDialog?.dismiss(); typeDialog = null
        downListDialog?.dismiss(); downListDialog = null
        downZipDialog?.dismiss(); downZipDialog = null
        albumDialog?.dismiss(); albumDialog = null
        typeTask?.cancelled = true
        typeTask = null
        taskStop()
        firstLoad?.release(); firstLoad = null
        lastLoad?.release(); lastLoad = null
    }

    override fun dismiss() {
        closeChildren(true)
        loadState = 2
        web?.let { MainUtil.destroyWebView(it, true) }
        web = null
        host?.let { (it.parent as? ViewGroup)?.removeView(it) }
        host = null
        super.dismiss()
    }

    /** Filters the list by picture type in the background (same as [WebGridDialog.TypeTask]). */
    class TypeTask(dialog: WebSiteDialog?, items: ArrayList<String?>?, type: Int) : MyAsyncTask() {
        @JvmField var dialogRef: WeakReference<*> = WeakReference<Any?>(dialog)
        @JvmField var items: ArrayList<String?>? = items
        @JvmField var images: ArrayList<String?>? = null
        @JvmField var type: Int = type

        init { dialog?.filtering = true }

        override fun doInBackground() {
            val dialog = dialogRef.get() as WebSiteDialog?
            val typeValue = if (dialog == null || cancelled) 0 else type
            val source: ArrayList<String?>? = if (dialog == null || cancelled || typeValue == 0) null else items
            if (dialog == null || cancelled || typeValue == 0 || source == null || source.isEmpty()) return
            if (typeValue == 126) { images = ArrayList<String?>(source); return }
            images = ArrayList<String?>()
            for (item in ArrayList<String?>(source)) {
                val url = item
                if (cancelled) return
                val ext = MainUtil.fileExtFromName(url, true)
                if (ext.isNullOrEmpty()) { if ((typeValue and 64) == 64) images!!.add(url) }
                else if (ext == "jpg") { if ((typeValue and 2) == 2) images!!.add(url) }
                else if (ext == "png") { if ((typeValue and 4) == 4) images!!.add(url) }
                else if (ext == "gif") { if ((typeValue and 8) == 8) images!!.add(url) }
                else if (ext == "webp") { if ((typeValue and 16) == 16) images!!.add(url) }
                else if ((typeValue and 32) == 32) images!!.add(url)
            }
        }

        override fun onCancelled() {
            val dialog = dialogRef.get() as WebSiteDialog?
            if (dialog != null) { dialog.typeTask = null; dialog.filtering = false; dialog.bump() }
        }

        override fun onPostExecute() {
            val dialog = dialogRef.get() as WebSiteDialog?
            if (dialog != null) {
                dialog.typeTask = null
                if (!cancelled) {
                    val before = items?.size ?: 0
                    val after = images?.size ?: 0
                    if (before > after) {
                        dialog.filteredCount = before - after
                        MainUtil.showToastText(dialog.ctx, String.format(Locale.US, dialog.ctx.getString(R.string.filtered_image), dialog.filteredCount))
                    }
                    dialog.adapter.setImages(images)
                    dialog.thumbs.clear(); dialog.failed.clear()
                }
                dialog.filtering = false
                dialog.bump()
            }
        }
    }

}
