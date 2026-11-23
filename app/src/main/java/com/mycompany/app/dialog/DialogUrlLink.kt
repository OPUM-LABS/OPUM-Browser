package com.mycompany.app.dialog

import com.mycompany.app.view.anyListener
import com.mycompany.app.main.ViewItemListener
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.main.JarBridge
import com.mycompany.app.main.MainItem
import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.os.Build
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.FrameLayout
import android.widget.ImageView.ScaleType
import android.widget.TextView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.util.Executors
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.compress.Compress
import com.mycompany.app.compress.Compress.Companion.isSvgName
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainItem.ViewItem
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainListLoader.ListLoadListener
import com.mycompany.app.main.MainUri.volumeLabel
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.urlDecode
import com.mycompany.app.main.MainUtil.googleLensJs
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.drawableToBitmap
import com.mycompany.app.main.MainUtil.isRtl
import com.mycompany.app.main.MainUtil.fileProviderUri
import com.mycompany.app.main.MainUtil.imageTypeOf
import com.mycompany.app.main.MainUtil.removeFromParent
import com.mycompany.app.main.MainUtil.dataUriExt
import com.mycompany.app.main.MainUtil.guessDownloadName
import com.mycompany.app.main.MainUtil.shareFile
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.isBitmapOk
import com.mycompany.app.main.MainUtil.zoneIconList
import com.mycompany.app.main.MainUtil.cacheFilePath
import com.mycompany.app.main.MainUtil.editorTextColor
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.newCacheFilePath
import com.mycompany.app.main.MainUtil.clipRound3
import com.mycompany.app.main.MainUtil.compareLongs
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUtil.copyFile
import com.mycompany.app.main.MainUtil.stripPunctuation
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefZone
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.view.GlideApp
import com.mycompany.app.view.GlideRequest
import com.mycompany.app.view.GlideRequests
import com.mycompany.app.view.MyGlideTarget
import com.mycompany.app.view.MyRoundImage
import com.mycompany.app.web.WebViewActivity

import java.io.File
import java.lang.ref.WeakReference
import android.os.Looper
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Long-press menu for a link / image in the browser: a header (site icon or image thumbnail, title, a settings button
 * that picks which actions are listed), Link / Image tabs when both apply, and the action rows. Actions go back to
 * [UrlLinkListener] (open, copy, share, download, search by image ...); a few open a choice menu first. Mode 4 / 9
 * (direct image download / share) shows only a busy sheet while the image is fetched ([ShareTask], unchanged).
 * Compose now (on [ComposeBottomDialog]); same constructor ([theme] the old style, unused), shows itself.
 */
@Suppress("deprecation")
class DialogUrlLink(
    webViewActivity: WebViewActivity?,
    theme: Int,
    linkUrl: String?,
    imageUrl: String?,
    linkFlag: Boolean,
    extraUrl: String?,
    referer: String?,
    linkType: Int,
    urlLinkListener: UrlLinkListener?
) : ComposeBottomDialog(webViewActivity) {
    /** Stand-in for the old sheet view: alive while non-null, [itemType] shows / hides the busy state. */
    inner class LinkBox {
        @JvmField var isLoading: Boolean = false
        fun setLoading(newIsLoading: Boolean) {
            isLoading = newIsLoading
            loading = newIsLoading
        }
    }

    @JvmField val mainHandler: Handler? = Handler(Looper.getMainLooper())
    @JvmField var listLoader: MainListLoader? = null
    @JvmField var glide: GlideRequests? = null
    @JvmField var imageFailed: Boolean = false
    @JvmField var shareJob: ShareTask? = null
    @JvmField var popupDialog: DialogSetPopup? = null
    @JvmField var dataState: Byte = 0
    @JvmField var openAfterLoad: Boolean = false
    @JvmField var mimeType: String? = null
    @JvmField var lensPending: Boolean = false
    @JvmField var iconRequestListener: RequestListener<Any?>? = null
    @JvmField var imageRequestListener: RequestListener<Any?>? = null
    @JvmField var imageTarget: MyGlideTarget<PictureDrawable>? = null
    @JvmField var lensJsPage: String? = null
    @JvmField var hostActivity: WebViewActivity? = null
    @JvmField var lensJsImage: String? = null
    @JvmField var appContext: Context? = null
    @JvmField var linkUrl: String? = null
    @JvmField var linkTitle: String? = null
    @JvmField var linkHost: String? = null
    @JvmField var imageUrl: String? = null
    @JvmField var imageTitle: String? = null
    @JvmField var iconUrl: String? = null
    @JvmField var extraUrl: String? = null
    @JvmField var referer: String? = null
    @JvmField var hasLink: Boolean = false
    @JvmField var hasImage: Boolean = false
    @JvmField var linkFlag: Boolean = false
    @JvmField var videoLabelSet: Boolean = false
    @JvmField var showImage: Boolean = false
    @JvmField var linkType: Int = 0
    @JvmField var linkListener: UrlLinkListener? = null
    @JvmField var linkBox: LinkBox? = null
    @JvmField var iconView: MyRoundImage? = null
    @JvmField var imageView: MyRoundImage? = null

    private var loading by mutableStateOf(false)
    /** 0 = waiting, 1 = menu, 2 = busy only (mode 4 / 9). */
    private var stage by mutableIntStateOf(0)
    private var title by mutableStateOf<String?>(null)
    private var page by mutableIntStateOf(0)
    private var tabs by mutableStateOf(false)
    private var linkItems by mutableStateOf<List<MainLinkItem>>(emptyList())
    private var imageItems by mutableStateOf<List<MainLinkItem>>(emptyList())
    private var iconHost by mutableStateOf<FrameLayout?>(null)
    /** Item id whose choice menu is open (-1 none) and its options. */
    private var popupId by mutableIntStateOf(-1)
    private var popupOptions by mutableStateOf<List<Int>>(emptyList())
    private var executor: java.util.concurrent.ExecutorService? = null

    init {
        this.iconRequestListener = LoadImageLoadListener()
        this.imageRequestListener = ImageRequestListener()
        this.imageTarget = SetLoadingStep()
        this.hostActivity = webViewActivity
        this.appContext = context
        this.linkUrl = linkUrl
        this.imageUrl = imageUrl
        this.linkFlag = linkFlag
        this.extraUrl = extraUrl
        this.referer = referer
        this.linkListener = urlLinkListener
        this.showImage = ZoneSettings.linkImage.value
        this.linkType = linkType
        this.linkBox = LinkBox()
        setSheet { Sheet() }
        if (linkType != 4 && linkType != 9) {
            runAsync(PrepareLinkTask())
        } else {
            stage = 2
            loading = true
            mainHandler?.post { showReady() }
        }
    }

    @Composable
    private fun ColumnScope.Sheet() {
        when (stage) {
            0 -> {}
            2 -> {
                LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { dismiss() }) { Text(stringResource(AppR.string.close)) }
                }
            }
            else -> Menu()
        }
    }

    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    @Composable
    private fun ColumnScope.Menu() {
        Row(Modifier.fillMaxWidth().height(72.dp).padding(start = 16.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            val host = iconHost
            if (host != null) {
                AndroidView(factory = { host }, modifier = Modifier.size(with(androidx.compose.ui.platform.LocalDensity.current) { MainApp.dp40.toDp() }))
            }
            Spacer(Modifier.width(16.dp))
            Text(
                title ?: "", style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { BuildItemsClick().onClick(null) }) {
                Icon(painterResource(AppR.drawable.outline_settings_black_20), stringResource(AppR.string.setting))
            }
        }
        if (tabs) {
            PrimaryTabRow(selectedTabIndex = page) {
                Tab(selected = page == 0, onClick = { selectPage(0) }, text = { Text(stringResource(AppR.string.link)) })
                Tab(selected = page == 1, onClick = { selectPage(1) }, text = { Text(stringResource(AppR.string.image)) })
            }
        }
        if (loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        val image = page == 1
        val items = if (image) imageItems else linkItems
        Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(bottom = 8.dp).alpha(if (loading) 0.5f else 1f)) {
            for (item in items) {
                androidx.compose.runtime.key(item.itemId, image) {
                    Box {
                        Row(
                            Modifier.fillMaxWidth().height(56.dp).clickable(enabled = !loading) { onItemClick(item.itemId, null, image) }
                                .padding(start = 24.dp, end = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (item.iconRes > 0) ResIcon(item.iconRes)
                            Spacer(Modifier.width(24.dp))
                            Text(
                                if (item.textRes > 0) stringResource(item.textRes) else (item.label ?: ""),
                                style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            )
                        }
                        com.mycompany.app.ui.components.BubbleMenu(expanded = popupId == item.itemId, onDismiss = { popupId = -1 }) {
                            popupOptions.forEachIndexed { index, label ->
                                DropdownMenuItem(text = { Text(stringResource(label)) }, onClick = { popupId = -1; onPopupPick(item.itemId, index) })
                            }
                        }
                    }
                }
            }
        }
    }

    /** Item icon (any drawable, drawn as the old row did). */
    @Composable
    private fun ResIcon(res: Int) {
        val ctx = LocalContext.current
        val loaded = remember(res) { try { ContextCompat.getDrawable(ctx, res) } catch (exception: Exception) { null } }
        Canvas(Modifier.size(20.dp)) {
            val dr = loaded ?: return@Canvas
            dr.setBounds(0, 0, size.width.toInt(), size.height.toInt())
            drawIntoCanvas { dr.draw(it.nativeCanvas) }
        }
    }

    fun runAsync(runnable: Runnable?) {
        var ex = executor
        if (ex == null || ex.isShutdown() || ex.isTerminated()) {
            ex = MainApp.executor(context) ?: return
            executor = ex
        }
        try { ex.execute(runnable) } catch (unused: Exception) { }
    }

    /** Tab change. */
    private fun selectPage(pageValue: Int) {
        if (page == pageValue) return
        page = pageValue
        showImage = pageValue != 0
        showImageMode(showImage)
    }

    /** Builds the header icons and the lists, then shows the menu (old `BuildIconTask` / `3` / `8`). */
    internal inner class BuildIconTask : Runnable {
        override fun run() {
            val context = appContext ?: return
            val host = FrameLayout(context)
            val round = MyRoundImage(context)
            round.setScaleType(ScaleType.CENTER_CROP)
            round.setCircleRadius(MainApp.dp40 / 2.0f)
            round.setVisibility(View.GONE)
            host.addView(round, -1, -1)
            val myRoundImage = MyRoundImage(context)
            myRoundImage.setScaleType(ScaleType.CENTER_CROP)
            myRoundImage.setCircleRadius(MainApp.dp40 / 2.0f)
            myRoundImage.setVisibility(View.GONE)
            host.addView(myRoundImage, -1, -1)
            iconView = round
            imageView = myRoundImage
            iconHost = host
            // the remembered tab (saved on dismiss) only changes when the user switches tabs
            if (!hasLink) {
                imageItems = listOfItems(buildItems(true))
                page = 1
                showImageMode(true)
            } else if (!hasImage) {
                linkItems = listOfItems(buildItems(false))
                page = 0
                showImageMode(false)
            } else {
                linkItems = listOfItems(buildItems(false))
                imageItems = listOfItems(buildItems(true))
                tabs = true
                page = if (showImage) 1 else 0
                showImageMode(showImage)
            }
            stage = 1
            showReady()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun listOfItems(list: ArrayList<Any?>): List<MainLinkItem> = list.filterIsInstance<MainLinkItem>()

    /** Choice menu for items 4 (image / video), 6 (search engines) and 10 (home). */
    fun showPopup(view: View?, newPopupId: Int) {
        if (hostActivity == null) return
        popupOptions = when (newPopupId) {
            4 -> listOf(AppR.string.image, AppR.string.video)
            6 -> listOf(AppR.string.google, AppR.string.bing, AppR.string.yandex)
            10 -> listOf(AppR.string.soul_home, AppR.string.phone_home)
            else -> return
        }
        popupId = newPopupId
    }

    private fun onPopupPick(itemId: Int, index: Int) {
        val urlLinkListener = linkListener ?: return
        if (itemId == 4) {
            if (index == 1) {
                urlLinkListener.runAction(urlLinkListener.linkUrl(), itemId, index, extraUrl, true, null)
                return
            }
            loadImageData(true)
            return
        }
        if (itemId == 6 && index == 0) {
            lensPending = true
            searchImage()
            return
        }
        urlLinkListener.runAction(imageUrl, itemId, index, extraUrl, linkFlag, null)
    }

    /** Lists changed in the settings popup (old `ImageModeApply`). */
    internal inner class ImageModeApply(value: Int) : DialogApplyListener {
        val isOn: Boolean = value != 0

        override fun onApply() {
            if (isOn) {
                if (hasLink) imageItems = listOfItems(buildItems(true))
            } else {
                if (hasImage) linkItems = listOfItems(buildItems(false))
            }
        }
    }

    /** The video entry's label once the page reports a video. */
    fun updateVideoLabel() {
        if (videoLabelSet || !hasLink) return
        mainHandler?.post {
            val label = videoLabel(4)
            if (TextUtils.isEmpty(label)) return@post
            imageItems = imageItems.map {
                if (it.itemId == 4) MainLinkItem().also { n -> n.itemId = it.itemId; n.iconRes = it.iconRes; n.textRes = 0; n.label = label } else it
            }
        }
    }

    fun showReady() {
        if (this.linkBox != null) {
            show()
            val linkType = this.linkType
            if (linkType == 4) {
                loadImageData(true)
            } else if (linkType == 9) {
                searchImage()
            } else if (this.hasImage) {
                loadImageData(false)
            }
        }
    }

    /** Header: site icon + title, or the image thumbnail + its title. */
    fun showImageMode(showImage: Boolean) {
        val myRoundImage = this.iconView
        if (myRoundImage == null || this.imageView == null) {
            return
        }
        if (showImage) {
            myRoundImage.setVisibility(View.GONE)
            this.imageView!!.setVisibility(View.VISIBLE)
            title = this.imageTitle
        } else {
            myRoundImage.setVisibility(View.VISIBLE)
            this.imageView!!.setVisibility(View.GONE)
            title = this.linkTitle
        }
        this.mainHandler?.post(LoadImageTask())
    }

    internal inner class PrepareLinkTask : Runnable {
        override fun run() {
            val unusedZero: Int = Y0
            val dialogUrlLink = this@DialogUrlLink
            if (!TextUtils.isEmpty(dialogUrlLink.linkUrl)) {
                dialogUrlLink.hasLink = true
                var linkText = dialogUrlLink.linkUrl
                val decodedLink = urlDecode(linkText)
                if (!TextUtils.isEmpty(decodedLink)) {
                    linkText = decodedLink
                }
                if (linkText != null && linkText.length > 100) {
                    linkText = linkText.substring(0, 100)
                }
                dialogUrlLink.linkTitle = linkText
                var linkHostText = dialogUrlLink.linkUrl
                val linkHost: String = MainUtil.hostOfUrl(linkHostText!!, true)!!
                if (!TextUtils.isEmpty(linkHost)) {
                    if (linkHost.length <= 2 || !linkHost.startsWith(".", 1)) {
                        linkHostText =
                            if (linkHost.length <= 4 || !linkHost.startsWith("www.")) linkHost else linkHost.substring(
                                4
                            )
                    } else {
                        linkHostText = linkHost.substring(2)
                    }
                }
                dialogUrlLink.linkHost = MainUtil.stripPunctuation(linkHostText, null)
            }
            if (!TextUtils.isEmpty(dialogUrlLink.imageUrl)) {
                dialogUrlLink.hasImage = true
                var imageText = dialogUrlLink.imageUrl
                val decodedImage = urlDecode(imageText)
                if (!TextUtils.isEmpty(decodedImage)) {
                    imageText = decodedImage
                }
                if (imageText != null && imageText.length > 100) {
                    imageText = imageText.substring(0, 100)
                }
                dialogUrlLink.imageTitle = imageText
            }
            val handler = dialogUrlLink.mainHandler
            if (handler == null) {
                return
            }
            handler.post(dialogUrlLink.BuildIconTask())
        }
    }

    internal inner class LoadImageTask : Runnable {
        override fun run() {
            val dialogUrlLink = this@DialogUrlLink
            if (dialogUrlLink.iconView == null || dialogUrlLink.imageView == null) {
                return
            }
            val noIcon = TextUtils.isEmpty(dialogUrlLink.iconUrl)
            val myRoundImage = dialogUrlLink.imageView
            if (noIcon) {
                if (myRoundImage!!.getVisibility() == View.VISIBLE) {
                    dialogUrlLink.loadImage()
                    return
                }
                val iconView = dialogUrlLink.iconView
                if (iconView != null) {
                    iconView.setIconSmall(false)
                    if (TextUtils.isEmpty(dialogUrlLink.linkUrl)) {
                        dialogUrlLink.iconView!!.showLetterIcon(
                            -460552,
                            AppR.drawable.outline_public_black_24,
                            dialogUrlLink.linkHost,
                            null
                        )
                        return
                    }
                    val childItem = ChildItem()
                    childItem.listType = 18
                    childItem.itemType = 11.toByte()
                    childItem.path = dialogUrlLink.linkUrl
                    val bitmapB = MainListLoader.cachedIconFor(childItem)
                    if (MainUtil.isBitmapOk(bitmapB)) {
                        dialogUrlLink.iconView!!.setIconSmall(true)
                        dialogUrlLink.iconView!!.setImageBitmap(bitmapB)
                        return
                    } else {
                        dialogUrlLink.listLoader = MainListLoader(
                            dialogUrlLink.appContext!!,
                            false,
                            dialogUrlLink.IconLoadListener()
                        )
                        dialogUrlLink.iconView!!.setTag(0)
                        dialogUrlLink.listLoader!!.enqueue(childItem, dialogUrlLink.iconView)
                        return
                    }
                }
                return
            }
            if (myRoundImage!!.getVisibility() != View.VISIBLE) {
                if (dialogUrlLink.iconView != null) {
                    if (TextUtils.isEmpty(dialogUrlLink.iconUrl)) {
                        dialogUrlLink.applyIcon(null)
                        return
                    }
                    val bitmapA = com.mycompany.app.glide.ImageCache.get(com.mycompany.app.glide.ImageCache.key(2, dialogUrlLink.iconUrl))
                    if (MainUtil.isBitmapOk(bitmapA)) {
                        dialogUrlLink.applyIcon(bitmapA)
                        return
                    }
                    val viewItem = ViewItem()
                    viewItem.itemType = 7.toByte()
                    viewItem.url = dialogUrlLink.iconUrl
                    viewItem.cacheType = 2
                    com.mycompany.app.glide.ThumbLoader.loadFor(viewItem, dialogUrlLink.iconView, dialogUrlLink.IconItemListener())
                    return
                }
                return
            }
            if (dialogUrlLink.imageView != null) {
                if (TextUtils.isEmpty(dialogUrlLink.imageUrl)) {
                    dialogUrlLink.applyImage(null)
                    return
                }
                val bitmapA = com.mycompany.app.glide.ImageCache.get(com.mycompany.app.glide.ImageCache.key(2, dialogUrlLink.imageUrl))
                if (MainUtil.isBitmapOk(bitmapA)) {
                    dialogUrlLink.applyImage(bitmapA)
                    return
                }
                val viewItem = ViewItem()
                viewItem.itemType = 7.toByte()
                viewItem.url = dialogUrlLink.imageUrl
                viewItem.referer = dialogUrlLink.referer
                viewItem.cacheType = 2
                dialogUrlLink.applyImage(null)
                com.mycompany.app.glide.ThumbLoader.loadFor(viewItem, dialogUrlLink.imageView, dialogUrlLink.ImageItemListener())
            }
        }
    }

    internal inner class IconLoadListener : ListLoadListener {
        override fun onFailed(childItem: ChildItem?, view: View?) {
            val dialogUrlLink = this@DialogUrlLink
            val myRoundImage = dialogUrlLink.iconView
            if (myRoundImage == null) {
                return
            }
            myRoundImage.showLetterIcon(-460552, AppR.drawable.outline_public_black_24, dialogUrlLink.linkHost, null)
        }

        override fun onLoaded(childItem: ChildItem?, view: View?, bitmap: Bitmap?) {
            val dialogUrlLink = this@DialogUrlLink
            if (dialogUrlLink.iconView == null) {
                return
            }
            val bitmapOk = MainUtil.isBitmapOk(bitmap)
            val myRoundImage = dialogUrlLink.iconView
            if (!bitmapOk) {
                myRoundImage!!.showLetterIcon(
                    -460552,
                    AppR.drawable.outline_public_black_24,
                    dialogUrlLink.linkHost,
                    null
                )
            } else {
                myRoundImage!!.setIconSmall(true)
                dialogUrlLink.iconView!!.setImageBitmap(bitmap)
            }
        }
    }

    internal inner class AnyListenerTask : Runnable {

        internal inner class LoadImageStep : Runnable {
            override fun run() {
                val dialogUrlLink = this@DialogUrlLink
                if (dialogUrlLink.glide == null || TextUtils.isEmpty(dialogUrlLink.imageUrl)) {
                    return
                }
                val isNetworkImage = URLUtil.isNetworkUrl(dialogUrlLink.imageUrl)
                val diskCacheStrategy = DiskCacheStrategy.NONE
                if (!isNetworkImage) {
                    (dialogUrlLink.glide!!.load(dialogUrlLink.imageUrl)
                        .diskCacheStrategy(diskCacheStrategy)).anyListener(dialogUrlLink.iconRequestListener)
                        .into(dialogUrlLink.imageView!!)
                    return
                }
                var referer = dialogUrlLink.referer
                if (dialogUrlLink.imageFailed) {
                    val isApi = MainConst.isApi28
                    referer = null
                }
                (dialogUrlLink.glide!!.load(MainUtil.glideUrlFor(dialogUrlLink.appContext, dialogUrlLink.imageUrl!!, referer))
                    .diskCacheStrategy(diskCacheStrategy)).anyListener(dialogUrlLink.iconRequestListener)
                    .into(dialogUrlLink.imageView!!)
            }
        }

        override fun run() {
            val dialogUrlLink = this@DialogUrlLink
            val webViewActivity = dialogUrlLink.hostActivity
            if (webViewActivity != null) {
                if (dialogUrlLink.glide == null) {
                    dialogUrlLink.glide = GlideApp.with(webViewActivity)
                }
                val handler = dialogUrlLink.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(LoadImageStep())
            }
        }
    }

    internal inner class LoadImageLoadListener : RequestListener<Any?> {

        internal inner class ReloadImageStep : Runnable {
            override fun run() {
                val dialogUrlLink = this@DialogUrlLink
                val unusedZero: Int = Y0
                dialogUrlLink.loadImage()
            }
        }

        override fun onLoadFailed(glideException: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Any?>, isFirstResource: Boolean): Boolean {
            val dialogUrlLink = this@DialogUrlLink
            if (dialogUrlLink.imageView != null) {
                if (!URLUtil.isNetworkUrl(dialogUrlLink.imageUrl) || dialogUrlLink.imageFailed) {
                    dialogUrlLink.imageView!!.showIcon(-460552, AppR.drawable.outline_image_black_24)
                    return true
                }
                dialogUrlLink.imageFailed = true
                val handler = dialogUrlLink.mainHandler
                if (handler != null) {
                    handler.post(ReloadImageStep())
                    return true
                }
            }
            return true
        }

        override fun onResourceReady(resource: Any?, model: Any, target: com.bumptech.glide.request.target.Target<Any?>?, dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
            return false
        }
    }

    internal inner class LoadLinkImageTask : Runnable {

        internal inner class LoadLinkImageStep : Runnable {
            override fun run() {
                val dialogUrlLink = this@DialogUrlLink
                if (dialogUrlLink.glide == null || TextUtils.isEmpty(dialogUrlLink.imageUrl)) {
                    return
                }
                if (!URLUtil.isNetworkUrl(dialogUrlLink.imageUrl)) {
                    ((dialogUrlLink.glide!!.`as`(PictureDrawable::class.java)).load(
                        dialogUrlLink.imageUrl
                    )).anyListener(dialogUrlLink.imageRequestListener).into(dialogUrlLink.imageView!!)
                    return
                }
                var referer = dialogUrlLink.referer
                if (dialogUrlLink.imageFailed) {
                    val isApi = MainConst.isApi28
                    referer = null
                }
                ((dialogUrlLink.glide!!.`as`(PictureDrawable::class.java)).load(
                    MainUtil.glideUrlFor(
                        dialogUrlLink.appContext,
                        dialogUrlLink.imageUrl!!,
                        referer
                    )
                )).anyListener(dialogUrlLink.imageRequestListener).into(dialogUrlLink.imageView!!)
            }
        }

        override fun run() {
            val dialogUrlLink = this@DialogUrlLink
            val webViewActivity = dialogUrlLink.hostActivity
            if (webViewActivity != null) {
                if (dialogUrlLink.glide == null) {
                    dialogUrlLink.glide = GlideApp.with(webViewActivity)
                }
                val handler = dialogUrlLink.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(LoadLinkImageStep())
            }
        }
    }

    internal inner class ImageRequestListener : RequestListener<Any?> {

        internal inner class ShowImageStep : Runnable {
            override fun run() {
                val dialogUrlLink = this@DialogUrlLink
                val unusedZero: Int = Y0
                if (dialogUrlLink.imageView == null || TextUtils.isEmpty(dialogUrlLink.imageUrl)) {
                    return
                }
                dialogUrlLink.runAsync(dialogUrlLink.LoadLinkImageTask())
            }
        }

        override fun onLoadFailed(glideException: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Any?>, isFirstResource: Boolean): Boolean {
            val dialogUrlLink = this@DialogUrlLink
            if (dialogUrlLink.imageView != null) {
                if (!URLUtil.isNetworkUrl(dialogUrlLink.imageUrl) || dialogUrlLink.imageFailed) {
                    dialogUrlLink.imageView!!.setLayerType(View.LAYER_TYPE_NONE, null)
                    dialogUrlLink.imageView!!.showIcon(-460552, AppR.drawable.outline_image_black_24)
                    return true
                }
                dialogUrlLink.imageFailed = true
                val handler = dialogUrlLink.mainHandler
                if (handler != null) {
                    handler.post(ShowImageStep())
                    return true
                }
            }
            return true
        }

        override fun onResourceReady(resource: Any?, model: Any, target: com.bumptech.glide.request.target.Target<Any?>?, dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
            val myRoundImage = this@DialogUrlLink.imageView
            if (myRoundImage != null) {
                myRoundImage.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            }
            return false
        }
    }

    internal inner class IconItemListener : ViewItemListener {
        override fun onFailed(viewItem: MainItem.ViewItem?, view: View?, failReason: Exception?) {
            val unusedZero: Int = Y0
            this@DialogUrlLink.applyIcon(null)
        }

        override fun onComplete(viewItem: MainItem.ViewItem?, view: View?, bitmap: Bitmap?) {
            val unusedZero: Int = Y0
            this@DialogUrlLink.applyIcon(bitmap)
        }
    }

    internal inner class ImageItemListener : ViewItemListener {
        override fun onFailed(viewItem: MainItem.ViewItem?, view: View?, failReason: Exception?) {
        }

        override fun onComplete(viewItem: MainItem.ViewItem?, view: View?, bitmap: Bitmap?) {
            val unusedZero: Int = Y0
            this@DialogUrlLink.applyImage(bitmap)
        }
    }

    internal inner class RunActionTask : Runnable {

        internal inner class OpenAfterLoadStep : Runnable {
            override fun run() {
                val dialogUrlLink = this@DialogUrlLink
                if (dialogUrlLink.openAfterLoad) {
                    dialogUrlLink.openAfterLoad = false
                    val urlLinkListener: UrlLinkListener? = if (dialogUrlLink.linkBox == null) null else dialogUrlLink.linkListener
                    if (dialogUrlLink.linkBox == null || urlLinkListener == null) {
                        return
                    }
                    urlLinkListener.runAction(
                        dialogUrlLink.imageUrl,
                        4,
                        0,
                        dialogUrlLink.extraUrl,
                        dialogUrlLink.linkFlag,
                        dialogUrlLink.mimeType
                    )
                }
            }
        }

        override fun run() {
            val dialogUrlLink = this@DialogUrlLink
            if (Compress.isImageName(guessDownloadName(dialogUrlLink.imageUrl, null, null, true)!!, true, true)) {
                dialogUrlLink.mimeType = null
            } else {
                val dataExt = dataUriExt(dialogUrlLink.imageUrl)
                if (!TextUtils.isEmpty(dataExt) && dataExt.startsWith("svg")) {
                    dialogUrlLink.imageUrl = "data:image/" + dataExt
                }
                dialogUrlLink.mimeType = "image/" + dataExt
            }
            dialogUrlLink.dataState = 2.toByte()
            val handler: Handler? = if (!dialogUrlLink.openAfterLoad) null else dialogUrlLink.mainHandler
            if (!dialogUrlLink.openAfterLoad || handler == null) {
                return
            }
            handler.post(OpenAfterLoadStep())
        }
    }

    internal inner class LoadImageFileTask : Runnable {

        internal inner class LoadImageFileStep : Runnable {

            internal inner class FileTarget : MyGlideTarget<File>() {
                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    val file = resource as File?
                    val dialogUrlLink = this@DialogUrlLink
                    if (dialogUrlLink.linkBox != null) {
                        val imageUrl = dialogUrlLink.imageUrl
                        val shareTask = dialogUrlLink.shareJob
                        if (shareTask != null) {
                            shareTask.cancelled = true
                        }
                        dialogUrlLink.shareJob = null
                        val handler = dialogUrlLink.mainHandler
                        if (handler == null) {
                            return
                        }
                        handler.post(
                            ShareStep(
                                dialogUrlLink,
                                imageUrl,
                                file,
                                null,
                                null
                            )
                        )
                    }
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    val loadImageFileTask = this@LoadImageFileTask
                    val myDialogLink = this@DialogUrlLink.linkBox
                    if (myDialogLink == null) {
                        return
                    }
                    myDialogLink.setLoading(false)
                    showToast(this@DialogUrlLink.appContext, AppR.string.image_fail)
                }
            }

            internal inner class BitmapTarget : MyGlideTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val bitmap = resource as Bitmap?
                    val dialogUrlLink = this@DialogUrlLink
                    if (dialogUrlLink.linkBox != null) {
                        val imageUrl = dialogUrlLink.imageUrl
                        val shareTask = dialogUrlLink.shareJob
                        if (shareTask != null) {
                            shareTask.cancelled = true
                        }
                        dialogUrlLink.shareJob = null
                        val handler = dialogUrlLink.mainHandler
                        if (handler == null) {
                            return
                        }
                        handler.post(
                            ShareStep(
                                dialogUrlLink,
                                imageUrl,
                                null,
                                bitmap,
                                null
                            )
                        )
                    }
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    val loadImageFileTask = this@LoadImageFileTask
                    val myDialogLink = this@DialogUrlLink.linkBox
                    if (myDialogLink == null) {
                        return
                    }
                    myDialogLink.setLoading(false)
                    showToast(this@DialogUrlLink.appContext, AppR.string.image_fail)
                }
            }

            override fun run() {
                val dialogUrlLink = this@DialogUrlLink
                if (dialogUrlLink.glide == null || TextUtils.isEmpty(dialogUrlLink.imageUrl)) {
                    return
                }
                val isNetworkImage = URLUtil.isNetworkUrl(dialogUrlLink.imageUrl)
                val executor = Executors.mainThreadExecutor()
                if (!isNetworkImage) {
                    val glideRequest =
                        (dialogUrlLink.glide!!.asBitmap()).load(dialogUrlLink.imageUrl)
                    glideRequest.into(BitmapTarget())
                    return
                }
                var referer = dialogUrlLink.referer
                if (dialogUrlLink.imageFailed) {
                    val isApi = MainConst.isApi28
                    referer = null
                }
                val glideRequest = (dialogUrlLink.glide!!.downloadOnly()).load(
                    MainUtil.glideUrlFor(
                        dialogUrlLink.appContext,
                        dialogUrlLink.imageUrl!!,
                        referer
                    )
                )
                glideRequest.into(FileTarget())
            }
        }

        override fun run() {
            val dialogUrlLink = this@DialogUrlLink
            val webViewActivity = dialogUrlLink.hostActivity
            if (webViewActivity != null) {
                if (dialogUrlLink.glide == null) {
                    dialogUrlLink.glide = GlideApp.with(webViewActivity)
                }
                val handler = dialogUrlLink.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(LoadImageFileStep())
            }
        }
    }

    internal inner class LoadImageAgainTask : Runnable {

        internal inner class LoadImageAgainStep : Runnable {
            override fun run() {
                val dialogUrlLink = this@DialogUrlLink
                if (dialogUrlLink.glide == null || TextUtils.isEmpty(dialogUrlLink.imageUrl)) {
                    return
                }
                if (!URLUtil.isNetworkUrl(dialogUrlLink.imageUrl)) {
                    ((dialogUrlLink.glide!!.`as`(PictureDrawable::class.java)).load(
                        dialogUrlLink.imageUrl
                    )).into(dialogUrlLink.imageTarget!!)
                    return
                }
                var referer = dialogUrlLink.referer
                if (dialogUrlLink.imageFailed) {
                    val isApi = MainConst.isApi28
                    referer = null
                }
                ((dialogUrlLink.glide!!.`as`(PictureDrawable::class.java)).load(
                    MainUtil.glideUrlFor(
                        dialogUrlLink.appContext,
                        dialogUrlLink.imageUrl!!,
                        referer
                    )
                )).into(dialogUrlLink.imageTarget!!)
            }
        }

        override fun run() {
            val dialogUrlLink = this@DialogUrlLink
            val webViewActivity = dialogUrlLink.hostActivity
            if (webViewActivity != null) {
                if (dialogUrlLink.glide == null) {
                    dialogUrlLink.glide = GlideApp.with(webViewActivity)
                }
                val handler = dialogUrlLink.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(LoadImageAgainStep())
            }
        }
    }

    internal inner class SetLoadingStep : MyGlideTarget<PictureDrawable>() {
        override fun onResourceReady(resource: PictureDrawable, transition: Transition<in PictureDrawable>?) {
            val pictureDrawable = resource as PictureDrawable?
            val dialogUrlLink = this@DialogUrlLink
            if (dialogUrlLink.linkBox != null) {
                val imageUrl = dialogUrlLink.imageUrl
                val shareTask = dialogUrlLink.shareJob
                if (shareTask != null) {
                    shareTask.cancelled = true
                }
                dialogUrlLink.shareJob = null
                val handler = dialogUrlLink.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(
                    ShareStep(
                        dialogUrlLink,
                        imageUrl,
                        null,
                        null,
                        pictureDrawable
                    )
                )
            }
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            val dialogUrlLink = this@DialogUrlLink
            val myDialogLink = dialogUrlLink.linkBox
            if (myDialogLink == null) {
                return
            }
            myDialogLink.setLoading(false)
            showToast(dialogUrlLink.appContext, AppR.string.image_fail)
        }
    }

    internal inner class ShareStep(@JvmField val outer: DialogUrlLink,
                                          @JvmField val shareText: String?,
                                          @JvmField val file: File?,
                                          @JvmField val bitmap: Bitmap?,
                                          @JvmField val picture: PictureDrawable?
    ) : Runnable {
        override fun run() {
            val dialogUrlLink = this.outer
            if (dialogUrlLink.appContext == null) {
                return
            }
            dialogUrlLink.shareJob = ShareTask(dialogUrlLink, this.shareText, this.file, this.bitmap, this.picture)
            dialogUrlLink.shareJob!!.start(dialogUrlLink.appContext)
        }
    }

    internal inner class PopupDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val unusedZero: Int = Y0
            val dialogUrlLink = this@DialogUrlLink
            val dialogSetPopup = dialogUrlLink.popupDialog
            if (dialogSetPopup != null) {
                dialogSetPopup.dismiss()
                dialogUrlLink.popupDialog = null
            }
        }
    }

    internal inner class BuildItemsClick : View.OnClickListener {

        override fun onClick(view: View?) {
            val unusedZero: Int = Y0
            val dialogUrlLink = this@DialogUrlLink
            val dialogSetPopup: DialogSetPopup? = if (dialogUrlLink.hostActivity == null) null else dialogUrlLink.popupDialog
            if (dialogUrlLink.hostActivity == null || dialogSetPopup != null) {
                return
            }
            var imageMode = 1
            if (if (!dialogUrlLink.hasLink || !dialogUrlLink.hasImage) !dialogUrlLink.hasImage else !dialogUrlLink.showImage) {
                imageMode = 0
            }
            val webViewActivity = dialogUrlLink.hostActivity
            val imageModeApply: ImageModeApply =
                ImageModeApply(imageMode)
            val popupDialog = DialogSetPopup(webViewActivity)
            popupDialog.hostActivity = webViewActivity
            popupDialog.appContext = popupDialog.getContext()
            popupDialog.applyListener = imageModeApply
            popupDialog.imageMode = imageMode != 0
            if (imageMode == 0) {
                popupDialog.usedMask = ZoneSettings.useLink.value
                popupDialog.itemOrder = ZoneSettings.linkOrder.value
            } else {
                popupDialog.usedMask = ZoneSettings.useImg.value
                popupDialog.itemOrder = ZoneSettings.imgOrder.value
            }
            popupDialog.dragItems = popupDialog.buildItems(false)
            popupDialog.start()
            dialogUrlLink.popupDialog = popupDialog
            popupDialog.setOnDismissListener(dialogUrlLink.PopupDialogDismiss())
        }
    }

    class ShareTask(
        dialogUrlLink: DialogUrlLink?,
        url: String?,
        file: File?,
        bitmap: Bitmap?,
        pictureDrawable: PictureDrawable?
    ) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var url: String? = null
        @JvmField
        var file: File? = null
        @JvmField
        var bitmap: Bitmap? = null
        @JvmField
        var picture: PictureDrawable? = null
        @JvmField
        var cachePath: String? = null
        @JvmField
        var isSaved: Boolean = false

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogUrlLink)
            this.dialogRef = weakReference
            if ((weakReference.get() as DialogUrlLink?) == null) {
                return@init
            }
            this.url = url
            this.file = file
            this.bitmap = bitmap
            this.picture = pictureDrawable

        }
    }

        public override fun doInBackground() {
            val dialogUrlLink = this.dialogRef!!.get() as DialogUrlLink?
            if (dialogUrlLink == null || this.cancelled) {
                return
            }
            val url = this.url
            val context: Context? = if (TextUtils.isEmpty(url)) null else dialogUrlLink.appContext
            if (TextUtils.isEmpty(url) || context == null) {
                return
            }
            var fileName = guessDownloadName(url, null, null, true)
            val pictureDrawable = this.picture
            if (pictureDrawable != null) {
                this.bitmap = drawableToBitmap(0, pictureDrawable)
            }
            if (MainUtil.isBitmapOk(this.bitmap)) {
                if (!Compress.isImageName(fileName!!, true, true)) {
                    fileName =
                        guessDownloadName(url, null, if (this.bitmap!!.hasAlpha()) "image/png" else "image/jpg", true)
                }
                val pngPath = MainUtil.cacheFilePath(context, fileName)
                this.cachePath = pngPath
                val saved = MainUtil.saveBitmapPng(context, this.bitmap, pngPath)
                this.isSaved = saved
                if (saved && dialogUrlLink.lensPending) {
                    dialogUrlLink.lensJsPage = googleLensJs(0)
                    dialogUrlLink.lensJsImage = googleLensJs(1)
                    return
                }
                return
            }
            val file = this.file
            if (file == null || file.length() <= 0) {
                return
            }
            val path = file.getPath()
            if (!Compress.isImageName(fileName!!, true, true)) {
                fileName = guessDownloadName(url, null, "image/" + imageTypeOf(path), true)
            }
            val copyPath = MainUtil.cacheFilePath(context, fileName)
            this.cachePath = copyPath
            val copied = MainUtil.copyFile(path, copyPath)
            this.isSaved = copied
            if (copied && dialogUrlLink.lensPending) {
                dialogUrlLink.lensJsPage = googleLensJs(0)
                dialogUrlLink.lensJsImage = googleLensJs(1)
            }
        }

        public override fun onCancelled() {
            val dialogUrlLink = this.dialogRef!!.get() as DialogUrlLink?
            if (dialogUrlLink != null) {
                dialogUrlLink.shareJob = null
                val myDialogLink = dialogUrlLink.linkBox
                if (myDialogLink != null) {
                    myDialogLink.setLoading(false)
                }
            }
        }

        public override fun onPostExecute() {
            val urlLinkListener: UrlLinkListener?
            val dialogUrlLink = this.dialogRef!!.get() as DialogUrlLink?
            if (dialogUrlLink == null) {
                return
            }
            dialogUrlLink.shareJob = null
            val myDialogLink = dialogUrlLink.linkBox
            if (myDialogLink != null) {
                myDialogLink.setLoading(false)
            }
            if (!this.isSaved) {
                showToast(dialogUrlLink.appContext, AppR.string.image_fail)
                return
            }
            val cachePath = this.cachePath
            val lensPending = dialogUrlLink.lensPending
            val webViewActivity = dialogUrlLink.hostActivity
            if (lensPending) {
                val lensJsPage = dialogUrlLink.lensJsPage
                val lensJsImage = dialogUrlLink.lensJsImage
                if (webViewActivity == null || TextUtils.isEmpty(lensJsPage) || TextUtils.isEmpty(lensJsImage)) {
                    urlLinkListener = dialogUrlLink.linkListener
                    if (urlLinkListener != null) {
                        urlLinkListener.runAction(
                            dialogUrlLink.imageUrl,
                            6,
                            0,
                            dialogUrlLink.extraUrl,
                            dialogUrlLink.linkFlag,
                            null
                        )
                    }
                } else {
                    try {
                        val intent = Intent("android.intent.action.SEND")
                        intent.setComponent(ComponentName(lensJsPage!!, lensJsImage!!))
                        intent.setType("image/*")
                        intent.putExtra("android.intent.extra.STREAM", fileProviderUri(webViewActivity, cachePath))
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                        webViewActivity.startActivity(intent)
                    } catch (unused: Exception) {
                        urlLinkListener = dialogUrlLink.linkListener
                        if (urlLinkListener != null) {
                            urlLinkListener.runAction(
                                dialogUrlLink.imageUrl,
                                6,
                                0,
                                dialogUrlLink.extraUrl,
                                dialogUrlLink.linkFlag,
                                null
                            )
                        }
                    }
                }
            } else {
                shareFile(4, webViewActivity, cachePath, null, null)
            }
            dialogUrlLink.dismiss()
        }
    }

    interface UrlLinkListener {
        fun onAction(action: Int, url: String?, label: String?)

        fun barHeight(): Int

        fun runAction(url: String?, action: Int, type: Int, referer: String?, isImage: Boolean, title: String?)

        fun linkUrl(): String?
    }

    fun loadImageData(openAfterLoad: Boolean) {
        if (TextUtils.isEmpty(this.imageUrl)) {
            return
        }
        this.openAfterLoad = openAfterLoad
        val linkFlag = this.linkFlag
        if (linkFlag) {
            this.dataState = 2.toByte()
        }
        val dataState = this.dataState
        if (dataState.toInt() == 0) {
            this.dataState = 1.toByte()
            val myDialogLink: LinkBox? = if (openAfterLoad) this.linkBox else null
            if (openAfterLoad && myDialogLink != null) {
                myDialogLink.setLoading(true)
            }
            runAsync(RunActionTask())
            return
        }
        if (openAfterLoad) {
            if (dataState.toInt() == 1) {
                val myDialogLink = this.linkBox
                if (myDialogLink != null) {
                    myDialogLink.setLoading(true)
                    return
                }
                return
            }
            if (dataState.toInt() == 2) {
                this.openAfterLoad = false
                val urlLinkListener: UrlLinkListener? = if (this.linkBox == null) null else this.linkListener
                if (this.linkBox == null || urlLinkListener == null) {
                    return
                }
                urlLinkListener.runAction(this.imageUrl, 4, 0, this.extraUrl, linkFlag, this.mimeType)
            }
        }
    }

    fun buildItems(articleA: Boolean): ArrayList<Any?> {
        var index = 0
        val zoneIcons = MainUtil.zoneIconList(if (articleA) 1 else 0, false)
        val arrayList: ArrayList<Any?> = ArrayList<Any?>()
        if (!articleA) {
            while (index < 12) {
                val valueIndex = zoneIcons[index]
                val useLink = ZoneSettings.useLink.value
                val intValue: Int = DialogSetPopup.Companion.popupValues[valueIndex]
                if ((useLink and intValue) == intValue) {
                    if (SyncSettings.secretMode.value && valueIndex == 6) {
                        arrayList.add(
                            MainLinkItem(
                                valueIndex,
                                if (AppState.darkUi) AppR.drawable.outline_mood_dark_24 else AppR.drawable.outline_mood_black_24,
                                AppR.string.normal_tab
                            )
                        )
                    } else {
                        arrayList.add(
                            MainLinkItem(
                                valueIndex,
                                if (AppState.darkUi) MainConst.linkMenuIconsDark[valueIndex] else MainConst.linkMenuIconsBlack[valueIndex],
                                MainConst.linkMenuNames[valueIndex]
                            )
                        )
                    }
                }
                index++
            }
            return arrayList
        }
        while (index < 12) {
            val itemId = zoneIcons[index]
            val useImg = ZoneSettings.useImg.value
            val label: Int = DialogSetPopup.Companion.popupLabels[itemId]
            if ((useImg and label) == label) {
                val iconRes = if (AppState.darkUi) MainConst.imageMenuIconsDark[itemId] else MainConst.imageMenuIconsBlack[itemId]
                if (itemId != 4 || (!this.linkFlag && TextUtils.isEmpty(this.linkListener!!.linkUrl()))) {
                    arrayList.add(MainLinkItem(itemId, iconRes, MainConst.imageMenuNames[itemId]))
                } else {
                    val videoLabelText = videoLabel(itemId)
                    val mainLinkItem = MainLinkItem()
                    mainLinkItem.itemId = itemId
                    mainLinkItem.iconRes = iconRes
                    mainLinkItem.label = videoLabelText
                    arrayList.add(mainLinkItem)
                }
            }
            index++
        }
        return arrayList
    }

    fun videoLabel(itemId: Int): String? {
        if (this.appContext == null) {
            return null
        }
        this.videoLabelSet = true
        val sb = StringBuilder()
        sb.append(this.appContext!!.getString(MainConst.imageMenuNames[itemId])).append(" (")
        sb.append(this.appContext!!.getString(AppR.string.video))
        sb.append(")")
        return sb.toString()
    }

    fun onItemClick(which: Int, view: View?, image: Boolean) {
        val urlLinkListener = this.linkListener
        val myDialogLink: LinkBox? = if (urlLinkListener == null) null else this.linkBox
        if (urlLinkListener == null || myDialogLink == null) {
            return
        }
        if (myDialogLink.isLoading) {
            return
        }
        if (!image) {
            urlLinkListener.onAction(which, this.linkUrl, this.extraUrl)
            return
        }
        if (which == 4) {
            if (this.linkFlag || TextUtils.isEmpty(urlLinkListener.linkUrl())) {
                loadImageData(true)
                return
            } else {
                showPopup(view, which)
                return
            }
        }
        if (which != 6) {
            if (which == 9) {
                searchImage()
                return
            } else if (which != 10) {
                urlLinkListener.runAction(this.imageUrl, which, 0, this.extraUrl, this.linkFlag, null)
                return
            }
        }
        showPopup(view, which)
    }

    fun loadImage() {
        val myRoundImage = this.imageView
        if (myRoundImage != null) {
            myRoundImage.showIcon(-460552, AppR.drawable.outline_image_black_24)
            if (TextUtils.isEmpty(this.imageUrl)) {
                return
            }
            if (!isSvgName(guessDownloadName(this.imageUrl, null, null, true))) {
                runAsync(AnyListenerTask())
            } else {
                if (this.imageView == null || TextUtils.isEmpty(this.imageUrl)) {
                    return
                }
                runAsync(LoadLinkImageTask())
            }
        }
    }

    fun applyImage(bitmap: Bitmap?) {
        if (this.imageView == null) {
            return
        }
        if (MainUtil.isBitmapOk(bitmap)) {
            this.imageView!!.setImageBitmap(bitmap)
        } else {
            this.imageView!!.showIcon(
                -460552,
                if (AppState.darkUi) AppR.drawable.outline_newspaper_dark_24 else AppR.drawable.outline_newspaper_black_24
            )
        }
    }

    fun applyIcon(bitmap: Bitmap?) {
        if (this.iconView == null) {
            return
        }
        val bitmapOk = MainUtil.isBitmapOk(bitmap)
        val myRoundImage = this.iconView
        if (bitmapOk) {
            myRoundImage!!.setIconSmall(true)
            this.iconView!!.setImageBitmap(bitmap)
        } else {
            myRoundImage!!.setIconSmall(false)
            this.iconView!!.showLetterIcon(-460552, AppR.drawable.outline_public_black_24, this.linkHost, null)
        }
    }

    fun searchImage() {
        if (TextUtils.isEmpty(this.imageUrl)) {
            return
        }
        val myDialogLink = this.linkBox
        if (myDialogLink != null) {
            myDialogLink.setLoading(true)
        }
        if (isSvgName(guessDownloadName(this.imageUrl, null, null, true))) {
            runAsync(LoadImageAgainTask())
        } else {
            runAsync(LoadImageFileTask())
        }
    }

    override fun dismiss() {
        val context = this.appContext
        if (context != null) {
            val showImage = this.showImage
            if (ZoneSettings.linkImage.value != showImage) {
                ZoneSettings.edit(context) { set(ZoneSettings.linkImage, showImage) }
            }
        }
        this.popupDialog?.dismiss()
        this.popupDialog = null
        this.shareJob?.cancelled = true
        this.shareJob = null
        this.listLoader?.release()
        this.listLoader = null
        val glideRequests = this.glide
        if (glideRequests != null) {
            this.imageView?.let { glideRequests.clear(it) }
            this.glide = null
        }
        this.linkBox = null
        this.iconView?.release()
        this.iconView = null
        this.imageView?.release()
        this.imageView = null
        this.hostActivity = null
        this.appContext = null
        this.linkUrl = null
        this.linkTitle = null
        this.linkHost = null
        this.imageUrl = null
        this.imageTitle = null
        this.iconUrl = null
        this.extraUrl = null
        this.referer = null
        this.linkListener = null
        super.dismiss()
    }

    companion object {
        const val Y0: Int = 0
    }
}

/** One link or image row of [DialogUrlLink] (was nested in `view/MyLinkView`). */
class MainLinkItem {
    constructor() : super()

    @JvmField
    var itemId: Int = 0
    @JvmField
    var iconRes: Int = 0
    @JvmField
    var textRes: Int = 0
    @JvmField
    var label: String? = null

    constructor(i: Int, i2: Int, i3: Int) {
        this.itemId = i
        this.iconRes = i2
        this.textRes = i3
    }
}
