package com.mycompany.app.web

import android.content.Context
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ImageSettings
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ViewItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.ViewItemListener
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefImage
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.browser.ComposeFullDialog
import com.mycompany.app.view.MyDialogBottom.UserShowListener
import com.mycompany.app.web.WebLoadTask.WebLoadTaskListener

import java.lang.ref.WeakReference
import java.util.Locale
import kotlin.math.max
import kotlinx.coroutines.launch
import androidx.collection.LruCache

/**
 * The "Image list" screen (main menu > Save all images): the pictures of the current page (loaded through [WebLoadTask]
 * and a hidden WebView), a type filter ([DialogImageType]), tap = picture viewer, long press = select, and the three
 * actions Download / ZIP / Album on the whole list or the selection. Compose now (on [ComposeFullDialog]); the
 * constructor, the members `WebViewActivity` calls and the companion [viewerPage] are kept. [WebGridAdapter] is used only as the data model here (list, selection, per-picture flags).
 */
class WebGridDialog(
    webViewActivity: WebViewActivity?,
    url: String?,
    bigPage: Boolean,
    smallPage: Boolean,
    kindParam: Int,
    webImgListener: WebImgListener?,
) : ComposeFullDialog(webViewActivity), PictureHost {
    @JvmField var downListDialog: DialogDownList? = null
    @JvmField var downZipDialog: DialogDownZip? = null
    @JvmField var albumDialog: DialogCreateAlbum? = null

    private val act: WebViewActivity? = webViewActivity
    private val ctx: Context = context
    private val listener: WebImgListener? = webImgListener
    private val url: String? = url
    private val bigPage = bigPage
    private val smallPage = smallPage
    private var title: String? = null
    private var list: ArrayList<String?>? = null
    private var count: ImgCntItem? = null
    private var loadMode: Byte = kindParam.toByte()
    private var firstShow = true
    private var viewerBusy = false
    private var viewerIndex = 0
    private var skipLoad = false
    private var progressNow = 0
    private var progressLast = 0
    private var progressSince = 0L
    private var delayWarned = false
    private var retry = 0
    private var progressStep = 0
    private var noList = false
    private var filteredCount = 0
    private var lastType = 0

    override val adapter = WebGridAdapter(webViewActivity, url, 0)
    private var confirm: DialogConfirm? = null
    private var typeDialog: DialogImageType? = null
    private var typeTask: TypeTask? = null
    private var loadView: WebLoadView? = null
    private var host: FrameLayout? = null
    private val ui = Handler(Looper.getMainLooper())

    // state the screen draws
    override var version by mutableIntStateOf(0)
        private set
    private var loadState by mutableIntStateOf(0) // 0 idle, 1 loading, 2 done
    override var filtering by mutableStateOf(false)
        private set
    override var barVisible by mutableStateOf(true)
        private set
    override var progress by mutableFloatStateOf(0f)
        private set
    private var progressRun by mutableIntStateOf(0)
    private var progressActive = false
    override val thumbs: SnapshotStateMap<Int, Bitmap> = mutableStateMapOf()
    override val failed: SnapshotStateMap<Int, Boolean> = mutableStateMapOf()
    override val loaded: Boolean get() = isLoaded()
    override val statusText: String? get() = null

    private fun bump() { version++ }
    private fun isLoaded(): Boolean = loadState == 2 && typeTask == null

    private val loadListener = object : WebLoadTaskListener {
        override fun onFailed() {
            loadMode = 2
            loadState = 2
            progressNow = 100
            showList(AlbumSettings.imageType.value, list)
        }

        override fun onLoading() {
            if (loadState == 2) return
            loadState = 1
        }

        override fun onResult(title: String?, arrayList: ArrayList<String?>?, imgCntItem: ImgCntItem?) {
            var name = title
            if (loadState == 2) return
            loadState = 0
            if (arrayList == null || arrayList.isEmpty()) {
                noList = false
                if (retry != 4) {
                    val webView: WebView? = WebLoadTask.Companion.instance().loadWeb
                    val p = if (webView != null) webView.getProgress() else 0
                    tick(p)
                    if (retry == 0) {
                        if (p == 100) { retry = 1; return }
                        return
                    } else {
                        if (retry == 3) { retry = 4; return }
                        return
                    }
                }
            }
            loadState = 2
            if (arrayList == null || arrayList.isEmpty()) {
                if (WebLoadTask.Companion.instance().netError) {
                    MainUtil.showToast(ctx, R.string.server_error)
                } else if (noList) {
                    MainUtil.showToast(ctx, R.string.check_network)
                }
            } else {
                if (name.isNullOrEmpty()) name = ctx.getString(R.string.no_title)
                this@WebGridDialog.title = name
                list = arrayList
                count = imgCntItem
            }
            showList(AlbumSettings.imageType.value, list)
        }

        override fun onProgress(progress: Int) {
            progressNow = progress
            loadMode = 1
            if (loadState == 2) return
            tick(progress)
        }

        override fun onStopped() {
            loadMode = 2
            if (loadState == 2) return
            loadState = 0
            tick(-1)
        }
    }

    init {
        setPage {
            LaunchedEffect(progressRun) { if (progressRun > 0) runProgress() }
            PictureListScreen(this)
        }
        show()
        startLoad()
    }

    // ------------------------------------------------------------------ load state machine (as before)

    private fun startLoad() {
        val box = FrameLayout(ctx)
        host = box
        addContentView(box, ViewGroup.LayoutParams(1, 1))
        if (URLUtil.isNetworkUrl(url)) {
            if (smallPage || bigPage) loadView = WebLoadView(act, box, url)
            WebLoadTask.Companion.instance().taskListener = loadListener
            var state = loadMode
            if (state.toInt() == 0) {
                state = WebLoadTask.Companion.instance().loadState
                loadMode = state
            }
            skipLoad = (smallPage || bigPage) && state.toInt() == 1
            tick(-1)
        } else {
            loadMode = 2
            loadState = 2
            progressNow = 100
            showList(AlbumSettings.imageType.value, list)
        }
        onShowAgain()
    }

    /** -1 starts a (re)load, other values are the page progress; starts the real load once it is far enough. */
    private fun tick(newProgress: Int) {
        if (loadState == 2) return
        if (newProgress == -1) {
            val webView: WebView? = WebLoadTask.Companion.instance().loadWeb
            progressNow = if (webView == null) 0 else webView.getProgress()
            progressLast = newProgress
            retry = 0
        } else if (newProgress != 100) {
            if (progressLast == newProgress) {
                if (delayWarned) return
                val now = System.currentTimeMillis()
                if (progressSince == 0L) {
                    progressSince = now
                    return
                } else {
                    if (now - progressSince > 5000) {
                        delayWarned = true
                        MainUtil.showToast(ctx, R.string.server_delay)
                        return
                    }
                    return
                }
            }
            progressLast = newProgress
            progressSince = 0L
            if (newProgress < 30) return
        }
        if (loadState != 0) return
        if (loadMode.toInt() == 0) {
            val task = WebLoadTask.Companion.instance()
            task.netError = false
            val webView = task.loadWeb
            if (webView == null) {
                task.taskListener?.onFailed()
            } else {
                webView.loadUrl(url!!)
            }
        } else if (skipLoad) {
            skipLoad = false
        } else {
            loadState = 1
            ui.postDelayed({
                if (loadState != 2) WebLoadTask.Companion.instance().onPageDone(url)
            }, 200L)
        }
        startProgress()
    }

    /** Old progress bar: runs until the list is loaded, polling [tick] so the real load gets started and retried. */
    private fun startProgress() {
        if (progressActive) return
        progressActive = true
        barVisible = true
        progress = 0f
        progressRun++
        bump()
    }

    private suspend fun runProgress() {
        var steps = 0
        var shown = 0
        while (loadState != 2) {
            val target = if (adapter.itemCount() != 0) 0 else {
                tick(progressNow)
                when {
                    progressNow == 100 -> 0
                    progressStep == 0 -> { progressStep++; progressStep }
                    progressStep == 100 -> progressStep
                    progressStep < max(progressNow, 50) -> { progressStep++; progressStep }
                    else -> progressStep
                }
            }
            shown = max(shown, target)
            progress = (shown / 100f).coerceIn(0.03f, 1f)
            kotlinx.coroutines.delay(100)
            steps++
            if (steps % 20 == 0 && retry in 1..2) retry++
        }
        progressActive = false
        barVisible = false
        bump()
    }

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
        thumbs.clear()
        failed.clear()
        filtering = false
        bump()
    }

    // ------------------------------------------------------------------ selection and actions

    private fun selecting(): Boolean { version; return adapter.selectMode }

    private fun enterSelect(index: Int, on: Boolean) {
        if (on == adapter.selectMode) return
        adapter.setSelectMode(index, on)
        bump()
    }

    private fun toggle(index: Int) {
        adapter.toggleChecked(index)
        bump()
    }

    override fun onSelectAll() {
        if (!isLoaded() || !adapter.selectMode) return
        adapter.setAllChecked(!adapter.allChecked(), true)
        bump()
    }

    override fun onTileLong(position: Int) {
        if (!isLoaded() || position < 0 || position >= adapter.itemCount()) return
        if (!adapter.selectMode) enterSelect(position, true) else toggle(position)
    }

    override fun onTileTap(position: Int) {
        if (!isLoaded() || position < 0 || position >= adapter.itemCount()) return
        if (adapter.selectMode) { toggle(position); return }
        if (viewerBusy) return
        viewerBusy = true
        viewerIndex = position
        ui.post { openViewer(); viewerBusy = false }
    }

    private fun openViewer() {
        val items = list
        val index = viewerIndex
        if (act != null && listener != null) {
            if (items == null || items.isEmpty()) {
                MainUtil.showToast(ctx, R.string.invalid_path)
            } else {
                val data = DataUrl.instance(ctx)
                @Suppress("UNCHECKED_CAST")
                data.urls = items as ArrayList<String?>?
                val intent = MainUtil.imageViewerIntent(ctx)
                intent.putExtra("EXTRA_TYPE", 12)
                intent.putExtra("EXTRA_NAME", title)
                intent.putExtra("EXTRA_INDEX", index)
                intent.putExtra("EXTRA_PAGE", viewerPage(act, adapter, index))
                intent.putExtra("EXTRA_REFERER", url)
                intent.putExtra("EXTRA_PRELOAD", true)
                act.startActivity(intent)
            }
        }
    }

    private fun busyOtherDialog(): Boolean =
        confirm != null || typeDialog != null || downListDialog != null || downZipDialog != null || albumDialog != null

    private fun chosen(): ArrayList<String?>? = if (adapter.selectMode) adapter.checkedUrls() else adapter.imageUrls

    override fun onAction(which: Int) {
        if (!isLoaded()) return
        if (AlbumSettings.guideDown.value) { askConfirm(which); return }
        runAction(which)
    }

    private fun runAction(which: Int) {
        when (which) {
            0 -> openDown(url, title, chosen())
            1 -> openZip(url, title, chosen())
            else -> openAlbum(url, title, chosen())
        }
    }

    private fun checkItems(url: String?, title: String?, items: ArrayList<String?>?): Boolean {
        if (act == null || busyOtherDialog()) return false
        if (url.isNullOrEmpty() || title.isNullOrEmpty()) { MainUtil.showToast(ctx, R.string.invalid_url); return false }
        if (items == null || items.isEmpty()) { MainUtil.showToast(ctx, R.string.no_image); return false }
        return true
    }

    private fun openAlbum(pageUrl: String?, title: String?, items: ArrayList<String?>?) {
        albumDialog?.dismiss(); albumDialog = null
        if (!checkItems(pageUrl, title, items)) return
        val dialogCreateAlbum = DialogCreateAlbum(act, title, items, url, object : DialogApplyListener {
            override fun onApply() { enterSelect(-1, false) }
        })
        albumDialog = dialogCreateAlbum
        dialogCreateAlbum.setOnDismissListener { albumDialog?.dismiss(); albumDialog = null }
    }

    private fun openDown(pageUrl: String?, title: String?, items: ArrayList<String?>?) {
        downListDialog?.dismiss(); downListDialog = null
        if (!checkItems(pageUrl, title, items)) return
        val dialogDownList = DialogDownList(act, title, items, url, object : DialogApplyListener {
            override fun onApply() { enterSelect(-1, false) }
        })
        downListDialog = dialogDownList
        dialogDownList.setOnDismissListener { downListDialog?.dismiss(); downListDialog = null }
    }

    private fun openZip(pageUrl: String?, title: String?, items: ArrayList<String?>?) {
        downZipDialog?.dismiss(); downZipDialog = null
        if (!checkItems(pageUrl, title, items)) return
        val dialogDownZip = DialogDownZip(act, title, items, url, object : DialogApplyListener {
            override fun onApply() { enterSelect(-1, false) }
        })
        downZipDialog = dialogDownZip
        dialogDownZip.setOnDismissListener { downZipDialog?.dismiss(); downZipDialog = null }
    }

    private fun askConfirm(which: Int) {
        if (!AlbumSettings.guideDown.value || act == null || busyOtherDialog()) return
        confirm?.dismiss()
        val dialogConfirm = DialogConfirm(act, object : DialogConfListener {
            override fun onResult(confirmed: Boolean) {
                if (confirmed) {
                    AlbumSettings.edit(ctx) { set(AlbumSettings.guideDown, false) }
                }
                confirm?.dismiss()
                confirm = null
            }
        })
        confirm = dialogConfirm
        dialogConfirm.setOnDismissListener {
            confirm?.dismiss()
            confirm = null
            runAction(which)
        }
    }

    override fun onFilter() {
        if (act == null || busyOtherDialog()) return
        typeDialog?.dismiss()
        val dialogImageType = DialogImageType(act, count, object : ChangedListener {
            override fun onChanged() { showList(AlbumSettings.imageType.value, list) }
        })
        typeDialog = dialogImageType
        dialogImageType.setOnDismissListener { typeDialog?.dismiss(); typeDialog = null }
    }

    override fun onRefresh() {
        if (progressActive || WebLoadTask.Companion.instance().loadWeb == null) return
        if (!URLUtil.isNetworkUrl(url)) { return }
        noList = false
        loadState = 0
        progressSince = 0L
        delayWarned = false
        progressStep = 0
        tick(-1)
    }

    // ------------------------------------------------------------------ thumbnails

    override fun requestThumb(position: Int) {
        val adapterNow = adapter
        val url = if (adapterNow.validIndex(position)) adapterNow.imageUrls!!.get(position) else null
        if (url == null) return
        if (url.isEmpty()) { failed[position] = true; return }
        val cached = com.mycompany.app.glide.ImageCache.get(com.mycompany.app.glide.ImageCache.key(2, url))
        if (MainUtil.isBitmapOk(cached)) { thumbs[position] = cached!!; return }
        val referer = if (adapterNow.noRefererFlags != null && position < adapterNow.noRefererFlags!!.size && adapterNow.noRefererFlags!![position]) null else adapterNow.pageUrl
        val item = ViewItem()
        item.itemType = 7.toByte()
        item.url = url
        item.referer = referer
        item.position = position
        item.cacheType = 2
        com.mycompany.app.glide.ThumbLoader.loadAlone(item, object : ViewItemListener {
            override fun onFailed(item: ViewItem?, view: View?, reason: Exception?) {
                ui.post { thumbFailed(item, reason) }
            }

            override fun onComplete(item: ViewItem?, view: View?, bitmap: Bitmap?) {
                ui.post { thumbDone(item, bitmap) }
            }
        })
    }

    private fun thumbFailed(item: ViewItem?, reason: Exception?) {
        if (item == null) return
        val position = item.position
        val adapterNow = adapter
        val lowerCase = if (adapterNow.validIndex(position)) adapterNow.imageUrls!!.get(position) else null
        if (lowerCase == null) return
        val failedFlags = adapterNow.failedFlags
        if (failedFlags != null && position < failedFlags.size && !failedFlags[position]) {
            failedFlags[position] = true
            val alt = MainUtil.extractRedirect(lowerCase)
            if (!alt.isNullOrEmpty() && alt != lowerCase) {
                adapterNow.imageUrls!!.set(position, alt)
                DataUrl.instance(ctx).addUrl(lowerCase, alt)
                bump()
                requestThumb(position)
                return
            }
        }
        val noRefererFlags = adapterNow.noRefererFlags
        if (noRefererFlags != null && position < noRefererFlags.size && !noRefererFlags[position]) {
            noRefererFlags[position] = true
            requestThumb(position)
            return
        }
        failed[position] = true
    }

    private fun thumbDone(item: ViewItem?, bitmap: Bitmap?) {
        if (item == null) return
        val position = item.position
        if (MainUtil.isBitmapOk(bitmap)) {
            val shown: Bitmap = bitmap!! // checked by isBitmapOk
            val imageSizes = adapter.imageSizes
            if (imageSizes != null && position >= 0 && position < imageSizes.size) imageSizes[position] = if (shown.getWidth() > shown.getHeight()) 2 else 1
            thumbs[position] = shown
        } else {
            failed[position] = true
        }
    }

    override fun badge(url: String?): String {
        if (url.isNullOrEmpty()) return "None"
        val ext = MainUtil.fileExtFromName(url, true)
        return if (ext.isNullOrEmpty()) "None" else ext.uppercase()
    }

    // ------------------------------------------------------------------ members WebViewActivity calls

    override fun onBack() { tryClose() }
    override fun onStatusClick() {}

    /** Back: leaves select mode first, then closes. */
    fun tryClose() {
        if (!adapter.selectMode) dismiss() else enterSelect(-1, false)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { tryClose() }

    /** The old "back is being handled by an animation" check; there is none now. */
    fun hasPopup(): Boolean = false

    /** Configuration or theme change. */
    fun refresh() { bump() }

    /** onResume: re-apply the album filter when it changed in the settings while away. */
    fun onShowAgain() {
        if (!firstShow) {
            if (isLoaded() && lastType != AlbumSettings.imageType.value && MainUtil.imageCountChanged(count, lastType, AlbumSettings.imageType.value)) {
                showList(AlbumSettings.imageType.value, list)
            }
            lastType = AlbumSettings.imageType.value
        } else {
            lastType = AlbumSettings.imageType.value
        }
        firstShow = false
    }

    /** `false` remembers the album filter, `true` (closing) cancels everything that is still running. */
    fun closeChildren(isClosing: Boolean) {
        if (!isClosing) { lastType = AlbumSettings.imageType.value; return }
        confirm?.dismiss(); confirm = null
        typeDialog?.dismiss(); typeDialog = null
        downListDialog?.dismiss(); downListDialog = null
        downZipDialog?.dismiss(); downZipDialog = null
        albumDialog?.dismiss(); albumDialog = null
        typeTask?.cancelled = true
        typeTask = null
        val task = WebLoadTask.Companion.instance()
        task.netError = false
        task.imageTask?.cancelled = true
        task.imageTask = null
        task.started = false
        task.loadWeb = null
        task.taskListener = null
        task.loadState = 0.toByte()
        task.netError = false
        loadView?.release()
        loadView = null
    }

    override fun dismiss() {
        closeChildren(true)
        loadState = 2
        host?.let { (it.parent as? ViewGroup)?.removeView(it) }
        host = null
        super.dismiss()
    }

    /** Filters the list by picture type in the background (old `TypeTask`), then hands the result to the model. */
    class TypeTask(dialog: WebGridDialog?, items: ArrayList<String?>?, type: Int) : MyAsyncTask() {
        @JvmField var dialogRef: WeakReference<*> = WeakReference<Any?>(dialog)
        @JvmField var items: ArrayList<String?>? = items
        @JvmField var images: ArrayList<String?>? = null
        @JvmField var type: Int = type

        init {
            dialog?.filtering = true
        }

        override fun doInBackground() {
            val dialog = dialogRef.get() as WebGridDialog?
            val typeValue = if (dialog == null || cancelled) 0 else type
            val source: ArrayList<String?>? = if (dialog == null || cancelled || typeValue == 0) null else items
            if (dialog == null || cancelled || typeValue == 0 || source == null || source.isEmpty()) return
            if (typeValue == 126) {
                images = ArrayList<String?>(source)
                return
            }
            images = ArrayList<String?>()
            val copy: ArrayList<String?> = ArrayList<String?>(source)
            for (item in copy) {
                val url = item
                if (cancelled) return
                val ext = MainUtil.fileExtFromName(url, true)
                if (ext.isNullOrEmpty()) {
                    if ((typeValue and 64) == 64) images!!.add(url)
                } else if (ext == "jpg") {
                    if ((typeValue and 2) == 2) images!!.add(url)
                } else if (ext == "png") {
                    if ((typeValue and 4) == 4) images!!.add(url)
                } else if (ext == "gif") {
                    if ((typeValue and 8) == 8) images!!.add(url)
                } else if (ext == "webp") {
                    if ((typeValue and 16) == 16) images!!.add(url)
                } else if ((typeValue and 32) == 32) {
                    images!!.add(url)
                }
            }
        }

        override fun onCancelled() {
            val dialog = dialogRef.get() as WebGridDialog?
            if (dialog != null) {
                dialog.typeTask = null
                dialog.filtering = false
                dialog.bump()
            }
        }

        override fun onPostExecute() {
            val dialog = dialogRef.get() as WebGridDialog?
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
                    dialog.thumbs.clear()
                    dialog.failed.clear()
                }
                dialog.filtering = false
                dialog.bump()
            }
        }
    }

    interface WebImgListener

    companion object {

        /** Viewer page mode for the image at index: 1 = single, 2 = type 1, 3/4 = type 2 (4 when ImageSettings.reverse.value).  */
        @JvmStatic
        fun viewerPage(activity: MainActivity?, adapter: WebGridAdapter?, index: Int): Int {
            if (!MainUtil.imageViewerDualPage(activity) || adapter == null) {
                return 1
            }
            val types = adapter.imageSizes
            val type = if (types != null && index >= 0 && index < types.size) types[index] else 0
            if (type == 2) {
                return if (ImageSettings.reverse.value) 4 else 3
            }
            return if (type == 1) 2 else 1
        }
    }
}
