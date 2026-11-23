package com.mycompany.app.dialog

import com.mycompany.app.view.anyListener
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.ui.state.AppState
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.FrameLayout
import android.widget.ImageView.ScaleType
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.RequestListener
import com.mycompany.app.compress.Compress.Companion.isSvgName
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainListLoader.ListLoadListener
import com.mycompany.app.main.MainUri.findChild
import com.mycompany.app.main.MainUri.externalVolumes
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.parseIntSafe
import com.mycompany.app.main.MainUtil.urlDecode
import com.mycompany.app.main.MainUtil.parseLongSafe
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.releaseRetriever
import com.mycompany.app.main.MainUtil.stripMarker
import com.mycompany.app.main.MainUtil.detachFromParent
import com.mycompany.app.main.MainUtil.guessDownloadName
import com.mycompany.app.main.MainUtil.SizeItem
import com.mycompany.app.main.MainUtil.isBitmapOk
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.underlined
import com.mycompany.app.main.MainUtil.durationTextFull
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.formatDate
import com.mycompany.app.view.GlideApp
import com.mycompany.app.view.GlideRequest
import com.mycompany.app.view.GlideRequests
import com.mycompany.app.ui.components.LoadCover
import com.mycompany.app.view.MyRoundImage
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Item details for list screens ([listType] = list type, [childItem] the item): an icon / preview header (favicon, file
 * thumbnail, image or SVG preview through Glide / [MainListLoader], unchanged and still on the old image views) and
 * up to nine label / value rows (name, URL / domain, title, resolution, album / site, genre / link, duration,
 * size / file, modified; media details read in the background with `MediaMetadataRetriever`; site and link copy
 * the URL when tapped). The row logic is the old code running on small Compose-state stand-ins ([UiNode]). Compose
 * now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogInfo(mainActivity: android.app.Activity?, listType: Int, childItem: ChildItem?) : ComposeBottomDialog(mainActivity) {
    /** Compose stand-in for the old row layouts and their label / value text views. */
    inner class UiNode(visible: Boolean = true) {
        var shown by mutableStateOf(visible)
        var textValue by mutableStateOf<String?>(null)
        var textRes by mutableIntStateOf(0)
        var click by mutableStateOf<View.OnClickListener?>(null)
        fun setVisibility(visibility: Int) { shown = visibility == 0 }
        fun setText(retry: CharSequence?) { textValue = retry?.toString(); textRes = 0 }
        fun setText(res: Int) { textRes = res; textValue = null }
        fun setTextColor(textColor: Int) {}
        fun setTextDirection(direction: Int) {}
        fun setBackgroundResource(transLogoShortBackColor: Int) {}
        fun setOnClickListener(newClick: View.OnClickListener?) { click = newClick }
        @Composable
        fun str(): String = if (textRes != 0) stringResource(textRes) else textValue ?: ""
    }

    @JvmField val mainHandler: Handler? = Handler(Looper.getMainLooper())
    private var executor: java.util.concurrent.ExecutorService? = null

    @JvmField var listLoader: MainListLoader? = null
    @JvmField var previewUrl: GlideUrl? = null
    @JvmField var glide: GlideRequests? = null
    @JvmField var previewFailed: Boolean = false
    @JvmField var copyUrlA: String? = null
    @JvmField var copyUrlB: String? = null
    @JvmField var requestListenerA: RequestListener<Any?>? = null
    @JvmField var requestListenerB: RequestListener<Any?>? = null
    @JvmField var requestListenerC: RequestListener<Any?>? = null
    @JvmField var hostActivity: android.app.Activity? = mainActivity
    @JvmField var appContext: Context? = context
    @JvmField var listType: Int = listType
    @JvmField var item: ChildItem? = childItem
    @JvmField var isRemote: Boolean = false
    /** Non-null while open (old root view check). */
    @JvmField var aliveToken: Any? = Any()
    /** Header frame with the icon [iconView]; the preview [previewView] moves in here once loaded (old [applyPreviewSize]). */
    @JvmField var headerFrame: FrameLayout? = null
    @JvmField var iconView: MyRoundImage? = null
    @JvmField var iconAltView: MyRoundImage? = null
    @JvmField var previewView: MyRoundImage? = null
    @JvmField var loadCover: LoadCover? = null
    @JvmField var row1: UiNode? = UiNode(false)
    @JvmField var row1Label: UiNode? = UiNode()
    @JvmField var row1Value: UiNode? = UiNode()
    @JvmField var row2: UiNode? = UiNode(false)
    @JvmField var row2Label: UiNode? = UiNode()
    @JvmField var row2Value: UiNode? = UiNode()
    @JvmField var row3: UiNode? = UiNode(false)
    @JvmField var row3Label: UiNode? = UiNode()
    @JvmField var row3Value: UiNode? = UiNode()
    @JvmField var row4: UiNode? = UiNode(false)
    @JvmField var row4Label: UiNode? = UiNode()
    @JvmField var row4Value: UiNode? = UiNode()
    @JvmField var row5: UiNode? = UiNode(false)
    @JvmField var row5Label: UiNode? = UiNode()
    @JvmField var row5Value: UiNode? = UiNode()
    @JvmField var row6: UiNode? = UiNode(false)
    @JvmField var row6Label: UiNode? = UiNode()
    @JvmField var row6Value: UiNode? = UiNode()
    @JvmField var row7: UiNode? = UiNode(false)
    @JvmField var row7Label: UiNode? = UiNode()
    @JvmField var row7Value: UiNode? = UiNode()
    @JvmField var row8: UiNode? = UiNode(false)
    @JvmField var row8Label: UiNode? = UiNode()
    @JvmField var row8Value: UiNode? = UiNode()
    @JvmField var row9: UiNode? = UiNode(false)
    @JvmField var row9Label: UiNode? = UiNode()
    @JvmField var row9Value: UiNode? = UiNode()

    private val rows: List<Triple<UiNode, UiNode, UiNode>>
        get() = listOf(
            Triple(row1!!, row1Label!!, row1Value!!), Triple(row2!!, row2Label!!, row2Value!!), Triple(row3!!, row3Label!!, row3Value!!), Triple(row4!!, row4Label!!, row4Value!!),
            Triple(row5!!, row5Label!!, row5Value!!), Triple(row6!!, row6Label!!, row6Value!!), Triple(row7!!, row7Label!!, row7Value!!), Triple(row8!!, row8Label!!, row8Value!!),
            Triple(row9!!, row9Label!!, row9Value!!),
        )

    init {
        requestListenerA = ApplyPreviewSizeLoadListener()
        requestListenerB = LoadIconLoadListener()
        requestListenerC = LoadPreviewLoadListener()
        if (listType == 17 || listType == 18 || listType == 19 || listType == 20 || listType == 21 || listType == 22 || listType == 23 || listType == 27 || listType == 28 || listType == 29 || listType == 34 || listType == 35) {
            isRemote = true
        } else if (listType == 24 && URLUtil.isNetworkUrl(childItem!!.path)) {
            isRemote = true
        }
        val ctx = context
        val header = FrameLayout(ctx)
        header.visibility = View.GONE
        val icon = MyRoundImage(ctx)
        icon.scaleType = ScaleType.CENTER_CROP
        icon.setCircleRadius(MainApp.dp40 / 2.0f)
        icon.visibility = View.GONE
        val lp = FrameLayout.LayoutParams(MainApp.dp40, MainApp.dp40)
        lp.gravity = 1
        lp.topMargin = MainApp.dp16
        lp.bottomMargin = MainApp.dp16
        header.addView(icon, lp)
        val previewHost = FrameLayout(ctx)
        val preview = MyRoundImage(ctx)
        preview.scaleType = ScaleType.FIT_CENTER
        preview.visibility = View.GONE
        val lp2 = FrameLayout.LayoutParams(-1, dpToPx(ctx, 140.0f).toInt())
        lp2.marginStart = MainApp.dp16
        lp2.marginEnd = MainApp.dp16
        previewHost.addView(preview, lp2)
        val cover = LoadCover(ctx, if (AppState.darkUi) -328966 else -13022805, dpToPx(ctx, 2.0f).toInt(), MainApp.dp24)
        cover.setBlockTouch(true)
        cover.visibility = View.GONE
        val lp3 = FrameLayout.LayoutParams(MainApp.dp48, MainApp.dp48)
        lp3.gravity = 17
        previewHost.addView(cover, lp3)
        headerFrame = header
        iconView = icon
        previewView = preview
        loadCover = cover
        setSheet {
            AndroidView(factory = { header }, modifier = Modifier.fillMaxWidth())
            Box(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).fillMaxWidth().heightIn(min = 72.dp)) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
                    for ((row, label, value) in rows) {
                        if (!row.shown) continue
                        Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                            Text(label.str(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            val click = value.click
                            if (click != null) {
                                Text(
                                    value.str(), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary,
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.padding(top = 4.dp).clickable { click.onClick(null) },
                                )
                            } else {
                                Text(value.str(), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
                AndroidView(factory = { previewHost }, modifier = Modifier.fillMaxWidth())
            }
        }
        mainHandler?.post(ImageSizeTask())
    }

    fun runAsync(runnable: Runnable?) {
        var ex = executor
        if (ex == null || ex.isShutdown() || ex.isTerminated()) {
            ex = MainApp.executor(context) ?: return
            executor = ex
        }
        try { ex.execute(runnable) } catch (unused: Exception) { }
    }

    internal inner class LoadIconTask : Runnable {
        internal inner class LoadIconStep : Runnable {
            override fun run() {
                val dialogInfo = this@DialogInfo
                val glideRequests = dialogInfo.glide
                if (glideRequests == null) {
                    return
                }
                (glideRequests.load(dialogInfo.previewUrl).diskCacheStrategy(DiskCacheStrategy.NONE)).into(
                    dialogInfo.iconView!!
                )
            }
        }
        override fun run() {
            val dialogInfo = this@DialogInfo
            val mainActivity = dialogInfo.hostActivity
            if (mainActivity != null) {
                if (dialogInfo.glide == null) {
                    dialogInfo.glide = GlideApp.with(mainActivity)
                }
                val handler = dialogInfo.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(LoadIconStep())
            }
        }
    }
    internal inner class LoadIconLoadListener : RequestListener<Any?> {
        internal inner class ReloadIconStep : Runnable {
            override fun run() {
                val dialogInfo = this@DialogInfo
                dialogInfo.loadIcon()
            }
        }
        override fun onLoadFailed(glideException: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Any?>, isFirstResource: Boolean): Boolean {
            val dialogInfo = this@DialogInfo
            val myRoundImage: MyRoundImage? = if (dialogInfo.previewUrl != null && !dialogInfo.previewFailed) dialogInfo.previewView else null
            if (dialogInfo.previewUrl != null && !dialogInfo.previewFailed && myRoundImage != null) {
                dialogInfo.previewFailed = true
                myRoundImage.post(ReloadIconStep())
            }
            return true
        }
        override fun onResourceReady(resource: Any?, model: Any, target: com.bumptech.glide.request.target.Target<Any?>?, dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
            val drawable = resource as Drawable
            val dialogInfo = this@DialogInfo
            if (dialogInfo.glide == null || dialogInfo.iconView == null) {
                return false
            }
            dialogInfo.applyPreviewSize(drawable)
            if (dialogInfo.iconAltView != null) {
                dialogInfo.iconView!!.setVisibility(View.GONE)
                dialogInfo.iconAltView!!.setVisibility(View.VISIBLE)
            }
            return false
        }
    }
    internal inner class LoadPreviewTask : Runnable {
        internal inner class LoadPreviewStep : Runnable {
            override fun run() {
                val dialogInfo = this@DialogInfo
                val glideRequests = dialogInfo.glide
                if (glideRequests == null) {
                    return
                }
                if (dialogInfo.previewUrl != null) {
                    ((glideRequests.`as`(PictureDrawable::class.java)).load(dialogInfo.previewUrl)).anyListener(
                        dialogInfo.requestListenerC
                    ).into(dialogInfo.previewView!!)
                } else {
                    ((glideRequests.`as`(PictureDrawable::class.java)).load(dialogInfo.item!!.path)).anyListener(
                        dialogInfo.requestListenerC
                    ).into(dialogInfo.previewView!!)
                }
            }
        }
        override fun run() {
            val dialogInfo = this@DialogInfo
            val mainActivity = dialogInfo.hostActivity
            if (mainActivity != null) {
                if (dialogInfo.glide == null) {
                    dialogInfo.glide = GlideApp.with(mainActivity)
                }
                val handler = dialogInfo.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(LoadPreviewStep())
            }
        }
    }
    internal inner class LoadPreviewLoadListener : RequestListener<Any?> {
        internal inner class ReloadPreviewStep : Runnable {
            override fun run() {
                val dialogInfo = this@DialogInfo
                dialogInfo.loadPreview()
            }
        }
        override fun onLoadFailed(glideException: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Any?>, isFirstResource: Boolean): Boolean {
            val dialogInfo = this@DialogInfo
            val myRoundImage: MyRoundImage? = if (dialogInfo.previewUrl != null && !dialogInfo.previewFailed) dialogInfo.previewView else null
            if (dialogInfo.previewUrl != null && !dialogInfo.previewFailed && myRoundImage != null) {
                dialogInfo.previewFailed = true
                myRoundImage.post(ReloadPreviewStep())
            }
            return true
        }
        override fun onResourceReady(resource: Any?, model: Any, target: com.bumptech.glide.request.target.Target<Any?>?, dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
            val pictureDrawable = resource as PictureDrawable
            val dialogInfo = this@DialogInfo
            if (dialogInfo.glide == null || dialogInfo.iconView == null) {
                return false
            }
            dialogInfo.applyPreviewSize(pictureDrawable)
            if (dialogInfo.iconAltView != null) {
                dialogInfo.iconView!!.setVisibility(View.GONE)
                dialogInfo.iconAltView!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                dialogInfo.iconAltView!!.setVisibility(View.VISIBLE)
            }
            return false
        }
    }
    internal inner class LoadLangTask : Runnable {
        internal inner class ShowLangStep(
                                             @JvmField val lang: String?,
                                             @JvmField val row4Text: String?,
                                             @JvmField val row5Text: String?,
                                             @JvmField val row6Text: String?,
                                             @JvmField val time: Long
        ) : Runnable {
            override fun run() {
                val dialogInfo = this@DialogInfo
                val myCoverView: LoadCover? = if (dialogInfo.aliveToken == null) null else dialogInfo.loadCover
                if (dialogInfo.aliveToken == null || myCoverView == null) {
                    return
                }
                myCoverView.setVisibility(View.GONE)
                val lang = this.lang
                if (!TextUtils.isEmpty(lang)) {
                    dialogInfo.row3!!.setVisibility(0)
                    if (AppState.darkUi) {
                        dialogInfo.row3Label!!.setTextColor(-4079167)
                        dialogInfo.row3Value!!.setTextColor(-328966)
                    } else {
                        dialogInfo.row3Label!!.setTextColor(-10395295)
                        dialogInfo.row3Value!!.setTextColor(-16777216)
                    }
                    dialogInfo.row3Label!!.setText(AppR.string.title)
                    dialogInfo.row3Value!!.setText(lang)
                }
                val res = this.row4Text
                if (!TextUtils.isEmpty(res)) {
                    dialogInfo.row4!!.setVisibility(0)
                    if (AppState.darkUi) {
                        dialogInfo.row4Label!!.setTextColor(-4079167)
                        dialogInfo.row4Value!!.setTextColor(-328966)
                    } else {
                        dialogInfo.row4Label!!.setTextColor(-10395295)
                        dialogInfo.row4Value!!.setTextColor(-16777216)
                    }
                    dialogInfo.row4Label!!.setText(AppR.string.artist)
                    dialogInfo.row4Value!!.setText(res)
                }
                val row5Text = this.row5Text
                if (!TextUtils.isEmpty(row5Text)) {
                    dialogInfo.row5!!.setVisibility(0)
                    if (AppState.darkUi) {
                        dialogInfo.row5Label!!.setTextColor(-4079167)
                        dialogInfo.row5Value!!.setTextColor(-328966)
                    } else {
                        dialogInfo.row5Label!!.setTextColor(-10395295)
                        dialogInfo.row5Value!!.setTextColor(-16777216)
                    }
                    dialogInfo.row5Label!!.setText(AppR.string.album)
                    dialogInfo.row5Value!!.setText(row5Text)
                }
                val row6Text = this.row6Text
                if (!TextUtils.isEmpty(row6Text)) {
                    dialogInfo.row6!!.setVisibility(0)
                    if (AppState.darkUi) {
                        dialogInfo.row6Label!!.setTextColor(-4079167)
                        dialogInfo.row6Value!!.setTextColor(-328966)
                    } else {
                        dialogInfo.row6Label!!.setTextColor(-10395295)
                        dialogInfo.row6Value!!.setTextColor(-16777216)
                    }
                    dialogInfo.row6Label!!.setText(AppR.string.genre)
                    dialogInfo.row6Value!!.setText(row6Text)
                }
                val time = this.time
                if (time > 0) {
                    dialogInfo.row7!!.setVisibility(0)
                    if (AppState.darkUi) {
                        dialogInfo.row7Label!!.setTextColor(-4079167)
                        dialogInfo.row7Value!!.setTextColor(-328966)
                    } else {
                        dialogInfo.row7Label!!.setTextColor(-10395295)
                        dialogInfo.row7Value!!.setTextColor(-16777216)
                    }
                    dialogInfo.row7Label!!.setText(AppR.string.duration)
                    dialogInfo.row7Value!!.setText(MainUtil.durationTextFull(time))
                }
            }
        }
        override fun run() {
            val duration: Long
            val name: String?
            val size: String?
            val date: String?
            val dialogInfo = this@DialogInfo
            if (dialogInfo.loadCover == null || dialogInfo.item == null) {
                return
            }
            var mediaMetadataRetriever: MediaMetadataRetriever? = MediaMetadataRetriever()
            var path: String? = null
            try {
                mediaMetadataRetriever!!.setDataSource(dialogInfo.appContext, Uri.parse(dialogInfo.item!!.path))
            } catch (unused: Exception) {
                MainUtil.releaseRetriever(mediaMetadataRetriever!!)
                mediaMetadataRetriever = null
            }
            if (mediaMetadataRetriever != null) {
                val title = stripMarker(mediaMetadataRetriever.extractMetadata(7))
                val artist = stripMarker(mediaMetadataRetriever.extractMetadata(2))
                val album = stripMarker(mediaMetadataRetriever.extractMetadata(1))
                val genre = stripMarker(mediaMetadataRetriever.extractMetadata(6))
                duration = parseLongSafe(mediaMetadataRetriever.extractMetadata(9))
                releaseRetriever(mediaMetadataRetriever)
                name = title
                path = artist
                size = album
                date = genre
            } else {
                duration = 0
                name = null
                size = null
                date = null
            }
            val time = duration
            val myCoverView = dialogInfo.loadCover
            if (myCoverView != null) {
                myCoverView.post(ShowLangStep(name, path, size, date, time))
            }
        }
    }
    internal inner class LoadCountTask : Runnable {
        internal inner class ShowCountStep(
                                             @JvmField val count: Int,
                                             @JvmField val index: Int,
                                             @JvmField val position: Int,
                                             @JvmField val time: Long
        ) : Runnable {
            override fun run() {
                val dialogInfo = this@DialogInfo
                val myCoverView = dialogInfo.loadCover
                if (myCoverView != null) {
                    myCoverView.setVisibility(View.GONE)
                    val count = this.count
                    val position = if (count > 0) this.index else 0
                    if (count > 0 && position > 0) {
                        if (this.position % 180 == 0) {
                            dialogInfo.row4Value!!.setText("" + count + " x " + position)
                        } else {
                            dialogInfo.row4Value!!.setText("" + position + " x " + count)
                        }
                        dialogInfo.row4Value!!.setTextDirection(3)
                    }
                    val time = this.time
                    if (time > 0) {
                        dialogInfo.row7Value!!.setText(MainUtil.durationTextFull(time))
                    }
                }
            }
        }
        override fun run() {
            val duration: Long
            val files: Int
            val folders: Int
            val others: Int
            val dialogInfo = this@DialogInfo
            if (dialogInfo.loadCover == null || dialogInfo.item == null) {
                return
            }
            var mediaMetadataRetriever: MediaMetadataRetriever? = MediaMetadataRetriever()
            try {
                mediaMetadataRetriever!!.setDataSource(dialogInfo.appContext, Uri.parse(dialogInfo.item!!.path))
            } catch (unused: Exception) {
                MainUtil.releaseRetriever(mediaMetadataRetriever!!)
                mediaMetadataRetriever = null
            }
            if (mediaMetadataRetriever != null) {
                val videoWidth = parseIntSafe(mediaMetadataRetriever.extractMetadata(18))
                val videoHeight = parseIntSafe(mediaMetadataRetriever.extractMetadata(19))
                val rotation = parseIntSafe(mediaMetadataRetriever.extractMetadata(24))
                duration = parseLongSafe(mediaMetadataRetriever.extractMetadata(9))
                releaseRetriever(mediaMetadataRetriever)
                files = videoWidth
                folders = videoHeight
                others = rotation
            } else {
                duration = 0
                files = 0
                folders = 0
                others = 0
            }
            val time = duration
            val myCoverView = dialogInfo.loadCover
            if (myCoverView != null) {
                myCoverView.post(ShowCountStep(files, folders, others, time))
            }
        }
    }


    internal inner class ImageSizeTask : Runnable {
        override fun run() {
            var childItem: ChildItem? = null
            val time: Long
            val marker: Char
            val dialogInfo = this@DialogInfo
            val listType = dialogInfo.listType
            if (listType != 26) {
                val isRemote = dialogInfo.isRemote
                if (dialogInfo.aliveToken != null && dialogInfo.appContext != null) {
                    if (listType != 19 && listType != 20 && listType != 21 && listType != 22 && listType != 27 && listType != 28 && listType != 29 && !TextUtils.isEmpty(
                            dialogInfo.item!!.name
                        )
                    ) {
                        val appCompatTextView = dialogInfo.row1Label
                        if (listType == 23) {
                            appCompatTextView!!.setText(AppR.string.domain_url)
                        } else if (listType == 36) {
                            appCompatTextView!!.setText(AppR.string.memo_title)
                        } else {
                            appCompatTextView!!.setText(AppR.string.name)
                        }
                        val row1Value = dialogInfo.row1Value
                        val itemNow = dialogInfo.item
                        if (listType == 23) {
                            row1Value!!.setText(itemNow!!.path)
                        } else {
                            row1Value!!.setText(itemNow!!.name)
                        }
                        val darkUi = AppState.darkUi
                        val row1Label = dialogInfo.row1Label
                        if (darkUi) {
                            row1Label!!.setTextColor(-4079167)
                            dialogInfo.row1Value!!.setTextColor(-328966)
                        } else {
                            row1Label!!.setTextColor(-10395295)
                            dialogInfo.row1Value!!.setTextColor(-16777216)
                        }
                        dialogInfo.row1!!.setVisibility(0)
                    }
                    if (dialogInfo.item!!.itemType.toInt() == 3 && dialogInfo.aliveToken != null) {
                        dialogInfo.loadCover!!.setVisibility(View.VISIBLE)
                        dialogInfo.runAsync(dialogInfo.LoadLangTask())
                    }
                    if (isRemote && !TextUtils.isEmpty(dialogInfo.item!!.path)) {
                        if (listType == 17 || listType == 18) {
                            dialogInfo.row2Label!!.setText(AppR.string.url)
                            var decodedPath = urlDecode(dialogInfo.item!!.path)
                            if (TextUtils.isEmpty(decodedPath)) {
                                decodedPath = dialogInfo.item!!.path
                            }
                            if (decodedPath != null && decodedPath.length > 200) {
                                decodedPath = decodedPath.substring(0, 200)
                            }
                            dialogInfo.row2Value!!.setText(decodedPath)
                        } else if (listType == 19 || listType == 20 || listType == 21 || listType == 22 || listType == 27 || listType == 28 || listType == 29) {
                            dialogInfo.row2Label!!.setText(AppR.string.domain_url)
                            dialogInfo.row2Value!!.setText(dialogInfo.item!!.name)
                        } else if (listType == 23) {
                            dialogInfo.row2Label!!.setText(AppR.string.image)
                            dialogInfo.row2Value!!.setText(dialogInfo.item!!.name)
                        } else if (listType == 24 || listType == 34) {
                            dialogInfo.row2Label!!.setText(AppR.string.url)
                            dialogInfo.row2Value!!.setText(dialogInfo.item!!.path)
                        } else {
                            val appCompatTextView = dialogInfo.row2Label
                            if (listType == 35) {
                                appCompatTextView!!.setText(AppR.string.user_agent)
                                dialogInfo.row2Value!!.setText(dialogInfo.item!!.path)
                            } else {
                                appCompatTextView!!.setText(AppR.string.domain_name)
                                dialogInfo.row2Value!!.setText(dialogInfo.item!!.path)
                            }
                        }
                        val darkUi = AppState.darkUi
                        val appCompatTextView = dialogInfo.row2Label
                        if (darkUi) {
                            appCompatTextView!!.setTextColor(-4079167)
                            dialogInfo.row2Value!!.setTextColor(-328966)
                        } else {
                            appCompatTextView!!.setTextColor(-10395295)
                            dialogInfo.row2Value!!.setTextColor(-16777216)
                        }
                        dialogInfo.row2!!.setVisibility(0)
                    }
                    if (listType == 31) {
                        val itemNow = dialogInfo.item
                        if (itemNow!!.listType == 8 && !TextUtils.isEmpty(itemNow.url)) {
                            dialogInfo.row3!!.setVisibility(0)
                            dialogInfo.row3Label!!.setText(AppR.string.url)
                            val decodedUrl = urlDecode(dialogInfo.item!!.url)
                            val noUrl = TextUtils.isEmpty(decodedUrl)
                            val row3Value = dialogInfo.row3Value
                            if (noUrl) {
                                row3Value!!.setText(dialogInfo.item!!.url)
                            } else {
                                row3Value!!.setText(decodedUrl)
                            }
                            val darkUi = AppState.darkUi
                            val appCompatTextView = dialogInfo.row3Label
                            if (darkUi) {
                                appCompatTextView!!.setTextColor(-4079167)
                                dialogInfo.row3Value!!.setTextColor(-328966)
                            } else {
                                appCompatTextView!!.setTextColor(-10395295)
                                dialogInfo.row3Value!!.setTextColor(-16777216)
                            }
                        }
                        val item = dialogInfo.item
                        val itemType = item!!.itemType
                        if (itemType.toInt() == 1) {
                            val sizeItemU: SizeItem? = if (!TextUtils.isEmpty(item.path)) MainUtil.imageSize(
                                    dialogInfo.appContext!!,
                                    dialogInfo.item!!.path
                                ) else null
                            if (!TextUtils.isEmpty(item.path) && sizeItemU != null && sizeItemU.width > 0 && sizeItemU.height > 0) {
                                dialogInfo.row4!!.setVisibility(0)
                                dialogInfo.row4Label!!.setText(AppR.string.resolution)
                                dialogInfo.row4Value!!.setText("" + sizeItemU.width + " x " + sizeItemU.height)
                                dialogInfo.row4Value!!.setTextDirection(3)
                                val darkUi = AppState.darkUi
                                val appCompatTextView = dialogInfo.row4Label
                                if (darkUi) {
                                    appCompatTextView!!.setTextColor(-4079167)
                                    dialogInfo.row4Value!!.setTextColor(-328966)
                                } else {
                                    appCompatTextView!!.setTextColor(-10395295)
                                    dialogInfo.row4Value!!.setTextColor(-16777216)
                                }
                            }
                        } else if (itemType.toInt() == 2 && dialogInfo.aliveToken != null) {
                            val darkUi = AppState.darkUi
                            val appCompatTextView = dialogInfo.row4Label
                            if (darkUi) {
                                appCompatTextView!!.setTextColor(-4079167)
                                dialogInfo.row4Value!!.setTextColor(-328966)
                                dialogInfo.row7Label!!.setTextColor(-4079167)
                                dialogInfo.row7Value!!.setTextColor(-328966)
                            } else {
                                appCompatTextView!!.setTextColor(-10395295)
                                dialogInfo.row4Value!!.setTextColor(-16777216)
                                dialogInfo.row7Label!!.setTextColor(-10395295)
                                dialogInfo.row7Value!!.setTextColor(-16777216)
                            }
                            dialogInfo.row4!!.setVisibility(0)
                            dialogInfo.row7!!.setVisibility(0)
                            dialogInfo.row4Label!!.setText(AppR.string.resolution)
                            dialogInfo.row4Value!!.setText(".")
                            dialogInfo.row7Label!!.setText(AppR.string.duration)
                            dialogInfo.row7Value!!.setText(".")
                            dialogInfo.loadCover!!.setVisibility(View.VISIBLE)
                            dialogInfo.runAsync(dialogInfo.LoadCountTask())
                        }
                    }
                    if ((!isRemote || listType == 24) && !TextUtils.isEmpty(dialogInfo.item!!.subText)) {
                        if (listType == 14 || listType == 15 || listType == 16) {
                            dialogInfo.row8Label!!.setText(AppR.string.bookmark)
                        } else {
                            val isDir = dialogInfo.item!!.isDir
                            val appCompatTextView = dialogInfo.row8Label
                            if (isDir) {
                                appCompatTextView!!.setText(AppR.string.file)
                            } else {
                                appCompatTextView!!.setText(AppR.string.size)
                            }
                        }
                        dialogInfo.row8!!.setVisibility(0)
                        dialogInfo.row8Value!!.setText(dialogInfo.item!!.subText)
                        val darkUi = AppState.darkUi
                        val appCompatTextView = dialogInfo.row8Label
                        if (darkUi) {
                            appCompatTextView!!.setTextColor(-4079167)
                            dialogInfo.row8Value!!.setTextColor(-328966)
                        } else {
                            appCompatTextView!!.setTextColor(-10395295)
                            dialogInfo.row8Value!!.setTextColor(-16777216)
                        }
                    }
                    if (dialogInfo.item!!.time > 0) {
                        dialogInfo.row9!!.setVisibility(0)
                        dialogInfo.row9Label!!.setText(AppR.string.modified)
                        dialogInfo.row9Value!!.setText(
                            MainUtil.formatDate(
                                dialogInfo.item!!.time,
                                null,
                                "yyyy.MM.dd  hh:mm:ss a"
                            )
                        )
                        val darkUi = AppState.darkUi
                        val appCompatTextView = dialogInfo.row9Label
                        if (darkUi) {
                            appCompatTextView!!.setTextColor(-4079167)
                            dialogInfo.row9Value!!.setTextColor(-328966)
                        } else {
                            appCompatTextView!!.setTextColor(-10395295)
                            dialogInfo.row9Value!!.setTextColor(-16777216)
                        }
                    }
                }
            } else if (dialogInfo.aliveToken != null && dialogInfo.appContext != null && (dialogInfo.item.also {
                    childItem = it
                }) != null) {
                if (TextUtils.isEmpty(childItem!!.name) && TextUtils.isEmpty(childItem.badge)) {
                    time = 0
                } else {
                    dialogInfo.row1!!.setVisibility(0)
                    val darkUi = AppState.darkUi
                    time = 0
                    val appCompatTextView = dialogInfo.row1Label
                    if (darkUi) {
                        appCompatTextView!!.setTextColor(-4079167)
                        dialogInfo.row1Value!!.setTextColor(-328966)
                    } else {
                        appCompatTextView!!.setTextColor(-10395295)
                        dialogInfo.row1Value!!.setTextColor(-16777216)
                    }
                    val res =
                        if (!TextUtils.isEmpty(childItem.badge)) childItem.badge else childItem.name
                    dialogInfo.row1Label!!.setText(AppR.string.name)
                    dialogInfo.row1Value!!.setText(res)
                }
                if (!TextUtils.isEmpty(childItem.subText)) {
                    dialogInfo.row2!!.setVisibility(0)
                    val darkUi = AppState.darkUi
                    val appCompatTextView = dialogInfo.row2Label
                    if (darkUi) {
                        appCompatTextView!!.setTextColor(-4079167)
                        dialogInfo.row2Value!!.setTextColor(-328966)
                    } else {
                        appCompatTextView!!.setTextColor(-10395295)
                        dialogInfo.row2Value!!.setTextColor(-16777216)
                    }
                    var version: String = childItem.subText!!
                    if (version.length > 1 && version.startsWith("v")) {
                        version = version.substring(1)
                    }
                    dialogInfo.row2Label!!.setText(AppR.string.version)
                    dialogInfo.row2Value!!.setText(version)
                }
                if (!TextUtils.isEmpty(childItem.dirName) || !TextUtils.isEmpty(childItem.mimeType)) {
                    dialogInfo.row3!!.setVisibility(0)
                    val darkUi = AppState.darkUi
                    val appCompatTextView = dialogInfo.row3Label
                    if (darkUi) {
                        appCompatTextView!!.setTextColor(-4079167)
                        dialogInfo.row3Value!!.setTextColor(-328966)
                    } else {
                        appCompatTextView!!.setTextColor(-10395295)
                        dialogInfo.row3Value!!.setTextColor(-16777216)
                    }
                    val res =
                        if (!TextUtils.isEmpty(childItem.mimeType)) childItem.mimeType else childItem.dirName
                    dialogInfo.row3Label!!.setText(AppR.string.info)
                    dialogInfo.row3Value!!.setText(res)
                }
                if (!TextUtils.isEmpty(childItem.author)) {
                    dialogInfo.row4!!.setVisibility(0)
                    val darkUi = AppState.darkUi
                    val appCompatTextView = dialogInfo.row4Label
                    if (darkUi) {
                        appCompatTextView!!.setTextColor(-4079167)
                        dialogInfo.row4Value!!.setTextColor(-328966)
                    } else {
                        appCompatTextView!!.setTextColor(-10395295)
                        dialogInfo.row4Value!!.setTextColor(-16777216)
                    }
                    dialogInfo.row4Label!!.setText(AppR.string.author)
                    dialogInfo.row4Value!!.setText(childItem.author)
                }
                if (URLUtil.isNetworkUrl(childItem.namespace)) {
                    dialogInfo.row5!!.setVisibility(0)
                    val dark = AppState.darkUi
                    val row5Label = dialogInfo.row5Label
                    if (dark) {
                        row5Label!!.setTextColor(-4079167)
                    } else {
                        row5Label!!.setTextColor(-10395295)
                    }
                    val darkUi = AppState.darkUi
                    val appCompatTextView = dialogInfo.row5Value
                    if (darkUi) {
                        appCompatTextView!!.setBackgroundResource(AppR.drawable.selector_normal_dark)
                        dialogInfo.row5Value!!.setTextColor(-328966)
                    } else {
                        appCompatTextView!!.setBackgroundResource(AppR.drawable.selector_normal)
                        dialogInfo.row5Value!!.setTextColor(-14784824)
                    }
                    var decodedNamespace = urlDecode(childItem.namespace)
                    if (TextUtils.isEmpty(decodedNamespace)) {
                        decodedNamespace = childItem.namespace
                    }
                    dialogInfo.row5Label!!.setText(AppR.string.site)
                    dialogInfo.row5Value!!.setText(MainUtil.underlined(decodedNamespace))
                    dialogInfo.copyUrlA = decodedNamespace
                    dialogInfo.row5Value!!.setOnClickListener(dialogInfo.CopyUrlAClick())
                }
                val path = childItem.path
                if (TextUtils.isEmpty(path)) {
                    marker = 0.toChar()
                } else if (URLUtil.isNetworkUrl(path)) {
                    marker = 1.toChar()
                } else {
                    marker = if (path!!.startsWith("userscript://")) 2.toChar() else 3.toChar()
                }
                if (marker.code != 0) {
                    dialogInfo.row6!!.setVisibility(0)
                    val dark = AppState.darkUi
                    val row6Label = dialogInfo.row6Label
                    if (dark) {
                        row6Label!!.setTextColor(-4079167)
                    } else {
                        row6Label!!.setTextColor(-10395295)
                    }
                    dialogInfo.row6Label!!.setText(AppR.string.link)
                    if (marker.code == 1) {
                        val darkUi = AppState.darkUi
                        val appCompatTextView = dialogInfo.row6Value
                        if (darkUi) {
                            appCompatTextView!!.setBackgroundResource(AppR.drawable.selector_normal_dark)
                            dialogInfo.row6Value!!.setTextColor(-328966)
                        } else {
                            appCompatTextView!!.setBackgroundResource(AppR.drawable.selector_normal)
                            dialogInfo.row6Value!!.setTextColor(-14784824)
                        }
                        var decodedItemPath = urlDecode(childItem.path)
                        if (TextUtils.isEmpty(decodedItemPath)) {
                            decodedItemPath = childItem.path
                        }
                        dialogInfo.row6Value!!.setText(MainUtil.underlined(decodedItemPath))
                        dialogInfo.copyUrlB = decodedItemPath
                        dialogInfo.row6Value!!.setOnClickListener(dialogInfo.CopyUrlBClick())
                    } else if (marker.code == 2) {
                        val darkUi = AppState.darkUi
                        val appCompatTextView = dialogInfo.row6Value
                        if (darkUi) {
                            appCompatTextView!!.setTextColor(-328966)
                        } else {
                            appCompatTextView!!.setTextColor(-16777216)
                        }
                        dialogInfo.row6Value!!.setText(AppR.string.user_defined)
                    } else {
                        val darkUi = AppState.darkUi
                        val appCompatTextView = dialogInfo.row6Value
                        if (darkUi) {
                            appCompatTextView!!.setTextColor(-328966)
                        } else {
                            appCompatTextView!!.setTextColor(-16777216)
                        }
                        dialogInfo.row6Value!!.setText(AppR.string.local_file)
                    }
                }
                if (childItem.time > time) {
                    dialogInfo.row7!!.setVisibility(0)
                    val darkUi = AppState.darkUi
                    val appCompatTextView = dialogInfo.row7Label
                    if (darkUi) {
                        appCompatTextView!!.setTextColor(-4079167)
                        dialogInfo.row7Value!!.setTextColor(-328966)
                    } else {
                        appCompatTextView!!.setTextColor(-10395295)
                        dialogInfo.row7Value!!.setTextColor(-16777216)
                    }
                    dialogInfo.row7Label!!.setText(AppR.string.modified)
                    dialogInfo.row7Value!!.setText(MainUtil.formatDate(childItem.time, null, "yyyy.MM.dd  hh:mm:ss a"))
                }
            }
            if (dialogInfo.aliveToken == null) {
                return
            }
            dialogInfo.show()
            dialogInfo.mainHandler?.post(dialogInfo.ShowItemTask())
        }
    }
    internal inner class ShowItemTask : Runnable {
        override fun run() {
            val dialogInfo = this@DialogInfo
            val listType = dialogInfo.listType
            if (listType == 26) {
                val childItem = dialogInfo.item
                val myRoundImage = dialogInfo.iconView
                if (myRoundImage == null || childItem == null) {
                    return
                }
                myRoundImage.showIcon(childItem.iconColor, childItem.iconRes)
                dialogInfo.iconView!!.setVisibility(View.VISIBLE)
                dialogInfo.headerFrame!!.setVisibility(View.VISIBLE)
                val dir = childItem.dir
                if (URLUtil.isNetworkUrl(dir)) {
                    val context = dialogInfo.appContext
                    val isApi = MainConst.isApi28
                    dialogInfo.previewUrl = MainUtil.glideUrlFor(context, dir!!, null)
                    dialogInfo.runAsync(dialogInfo.LoadIconTask())
                    return
                }
                return
            }
            if (listType == 23) {
                dialogInfo.loadIcon()
                return
            }
            val iconView = dialogInfo.iconView
            var childItem: ChildItem? = if (iconView == null) null else dialogInfo.item
            if (iconView == null || childItem == null || listType == 19 || listType == 20 || listType == 21 || listType == 22 || listType == 24 || listType == 27 || listType == 28 || listType == 29) {
                return
            }
            if (listType == 31) {
                if (childItem.status != 3) {
                    iconView.showIcon(childItem.iconColor, childItem.iconRes)
                    dialogInfo.iconView!!.setVisibility(View.VISIBLE)
                    dialogInfo.headerFrame!!.setVisibility(View.VISIBLE)
                    return
                }
            } else if (listType == 18) {
                val iconRes = childItem.iconRes
                if (iconRes == AppR.drawable.outline_offline_pin_black_24) {
                    iconView.showIcon(childItem.iconColor, iconRes)
                    dialogInfo.iconView!!.setVisibility(View.VISIBLE)
                    dialogInfo.headerFrame!!.setVisibility(View.VISIBLE)
                    return
                }
            } else if (listType == 34) {
                val iconColor = childItem.iconColor
                if (iconColor != 0) {
                    iconView.showIcon(iconColor, childItem.iconRes)
                    dialogInfo.iconView!!.setVisibility(View.VISIBLE)
                    dialogInfo.headerFrame!!.setVisibility(View.VISIBLE)
                    return
                }
            } else if (listType == 35) {
                iconView.showLetterIcon(childItem.iconColor, childItem.iconRes, childItem.name, null)
                dialogInfo.iconView!!.setVisibility(View.VISIBLE)
                dialogInfo.headerFrame!!.setVisibility(View.VISIBLE)
                return
            } else if (listType == 36) {
                return
            }
            val itemType = childItem.itemType
            if (itemType.toInt() != 1 && itemType.toInt() != 2 && itemType.toInt() != 3 && itemType.toInt() != 4 && itemType.toInt() != 5 && itemType.toInt() != 6 && itemType.toInt() != 11) {
                iconView.showIcon(childItem.iconColor, childItem.iconRes)
                dialogInfo.iconView!!.setVisibility(View.VISIBLE)
                dialogInfo.headerFrame!!.setVisibility(View.VISIBLE)
                return
            }
            val query = ChildItem()
            if (itemType.toInt() == 11) {
                query.listType = listType
                query.itemType = itemType
                val filePath = childItem.filePath
                query.path = filePath
                query.name = childItem.name
                query.filePath = filePath
                query.id = childItem.id
                query.position = childItem.position
                query.iconColor = childItem.iconColor
                query.iconRes = childItem.iconRes
                childItem = query
            }
            if (TextUtils.isEmpty(childItem.path)) {
                val isRemote = dialogInfo.isRemote
                val myRoundImage = dialogInfo.iconView
                if (isRemote) {
                    val itemNow = dialogInfo.item
                    myRoundImage!!.showLetterIcon(itemNow!!.iconColor, itemNow.iconRes, itemNow.name, null)
                } else {
                    myRoundImage!!.showIcon(childItem.iconColor, childItem.iconRes)
                }
                dialogInfo.iconView!!.setVisibility(View.VISIBLE)
                dialogInfo.headerFrame!!.setVisibility(View.VISIBLE)
                return
            }
            if (childItem.itemType.toInt() == 1) {
                dialogInfo.iconView!!.showIcon(childItem.iconColor, childItem.iconRes)
                dialogInfo.iconView!!.setVisibility(View.VISIBLE)
                dialogInfo.headerFrame!!.setVisibility(View.VISIBLE)
                if (isSvgName(dialogInfo.item!!.name)) {
                    dialogInfo.loadPreview()
                    return
                } else {
                    dialogInfo.previewView!!.setVisibility(View.INVISIBLE)
                    dialogInfo.runAsync(dialogInfo.LoadThumbTask())
                    return
                }
            }
            val bitmapB: Bitmap = MainListLoader.cachedIconFor(childItem)!!
            if (MainUtil.isBitmapOk(bitmapB)) {
                dialogInfo.applyIcon(bitmapB)
                if (childItem.itemType.toInt() == 4) {
                    dialogInfo.iconView!!.setBackColor(-460552)
                }
                dialogInfo.iconView!!.setImageBitmap(bitmapB)
                dialogInfo.iconView!!.setVisibility(View.VISIBLE)
                dialogInfo.headerFrame!!.setVisibility(View.VISIBLE)
                return
            }
            dialogInfo.listLoader = MainListLoader(dialogInfo.appContext!!, false, dialogInfo.IconLoadListener())
            dialogInfo.iconView!!.showIcon(childItem.iconColor, childItem.iconRes)
            dialogInfo.iconView!!.setTag(childItem.position)
            dialogInfo.iconView!!.setVisibility(View.VISIBLE)
            dialogInfo.headerFrame!!.setVisibility(View.VISIBLE)
            dialogInfo.listLoader!!.enqueue(childItem, dialogInfo.iconView)
        }
    }
    internal inner class CopyUrlAClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogInfo = this@DialogInfo
            MainUtil.copyTextToast(dialogInfo.appContext, AppR.string.copied_clipboard, "Copied URL", dialogInfo.copyUrlA)
        }
    }
    internal inner class CopyUrlBClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogInfo = this@DialogInfo
            MainUtil.copyTextToast(dialogInfo.appContext, AppR.string.copied_clipboard, "Copied URL", dialogInfo.copyUrlB)
        }
    }
    internal inner class IconLoadListener : ListLoadListener {
        override fun onFailed(childItem: ChildItem?, view: View?) {
            val dialogInfo = this@DialogInfo
            val myRoundImage = dialogInfo.iconView
            if (myRoundImage == null || !dialogInfo.isRemote) {
                return
            }
            myRoundImage.usePath(null, true)
            dialogInfo.iconView!!.showLetterIcon(childItem!!.iconColor, childItem.iconRes, childItem.name, null)
        }
        override fun onLoaded(childItem: ChildItem?, view: View?, bitmap: Bitmap?) {
            val dialogInfo = this@DialogInfo
            if (dialogInfo.iconView == null) {
                return
            }
            dialogInfo.applyIcon(bitmap)
            if (childItem!!.itemType.toInt() == 4) {
                dialogInfo.iconView!!.setBackColor(-460552)
            }
            dialogInfo.iconView!!.usePath(null, true)
            dialogInfo.iconView!!.setImageBitmap(bitmap)
        }
    }
    internal inner class LoadThumbTask : Runnable {
        internal inner class LoadThumbStep : Runnable {
            override fun run() {
                val dialogInfo = this@DialogInfo
                val glideRequests = dialogInfo.glide
                if (glideRequests == null) {
                    return
                }
                (glideRequests.load(dialogInfo.item!!.path).diskCacheStrategy(DiskCacheStrategy.NONE)).anyListener(
                    dialogInfo.requestListenerA
                ).into(dialogInfo.previewView!!)
            }
        }
        override fun run() {
            val dialogInfo = this@DialogInfo
            val mainActivity = dialogInfo.hostActivity
            if (mainActivity != null) {
                if (dialogInfo.glide == null) {
                    dialogInfo.glide = GlideApp.with(mainActivity)
                }
                val handler = dialogInfo.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(LoadThumbStep())
            }
        }
    }
    internal inner class ApplyPreviewSizeLoadListener : RequestListener<Any?> {
        override fun onLoadFailed(glideException: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Any?>, isFirstResource: Boolean): Boolean {
            return true
        }
        override fun onResourceReady(resource: Any?, model: Any, target: com.bumptech.glide.request.target.Target<Any?>?, dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
            val drawable = resource as Drawable
            val dialogInfo = this@DialogInfo
            if (dialogInfo.glide == null || dialogInfo.iconView == null) {
                return false
            }
            dialogInfo.applyPreviewSize(drawable)
            if (dialogInfo.iconAltView != null) {
                dialogInfo.iconView!!.setVisibility(View.GONE)
                dialogInfo.iconAltView!!.setVisibility(View.VISIBLE)
            }
            return false
        }
    }
    internal inner class LoadImageTask : Runnable {
        internal inner class LoadImageStep : Runnable {
            override fun run() {
                val dialogInfo = this@DialogInfo
                val glideRequests = dialogInfo.glide
                if (glideRequests == null) {
                    return
                }
                (glideRequests.load(dialogInfo.previewUrl).diskCacheStrategy(DiskCacheStrategy.NONE)).anyListener(
                    dialogInfo.requestListenerB
                ).into(dialogInfo.previewView!!)
            }
        }
        override fun run() {
            val dialogInfo = this@DialogInfo
            val mainActivity = dialogInfo.hostActivity
            if (mainActivity != null) {
                if (dialogInfo.glide == null) {
                    dialogInfo.glide = GlideApp.with(mainActivity)
                }
                val handler = dialogInfo.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(LoadImageStep())
            }
        }
    }

    fun loadIcon() {
        val childItem: ChildItem? = if (this.iconView == null) null else this.item
        if (this.iconView == null || childItem == null) {
            return
        }
        val noName = TextUtils.isEmpty(childItem.name)
        val myRoundImage = this.iconView
        val iconColor = childItem.iconColor
        if (noName) {
            myRoundImage!!.showIcon(iconColor, childItem.iconRes)
            this.iconView!!.setVisibility(View.VISIBLE)
            this.headerFrame!!.setVisibility(View.VISIBLE)
            return
        }
        myRoundImage!!.showIcon(iconColor, childItem.iconRes)
        this.iconView!!.setVisibility(View.VISIBLE)
        this.headerFrame!!.setVisibility(View.VISIBLE)
        var referer: String? = null
        if (isSvgName(guessDownloadName(this.item!!.name, null, null, true))) {
            loadPreview()
            return
        }
        this.previewView!!.setVisibility(View.INVISIBLE)
        val name = this.item!!.name
        if (this.previewFailed) {
            val isApi = MainConst.isApi28
        } else {
            referer = name
        }
        this.previewUrl = MainUtil.glideUrlFor(this.appContext, name!!, referer)
        runAsync(LoadImageTask())
    }

    fun applyIcon(bitmap: Bitmap?) {
        val myRoundImage: MyRoundImage? = if (!MainUtil.isBitmapOk(bitmap)) null else this.iconView
        val layoutParams: ViewGroup.LayoutParams? = if (!MainUtil.isBitmapOk(bitmap) || myRoundImage == null) null else myRoundImage.getLayoutParams()
        if (!MainUtil.isBitmapOk(bitmap) || myRoundImage == null || layoutParams == null) {
            return
        }
        val isRemote = this.isRemote
        val listType = this.listType
        if (!isRemote && (listType != 31 || this.item!!.listType != 8)) {
            val iconSize = Math.round(dpToPx(this.appContext, 140.0f))
            layoutParams.height = iconSize
            layoutParams.width =
                Math.round(((bitmap!!.getWidth() / bitmap.getHeight()) * iconSize).toFloat())
            return
        }
        val iconView = this.iconView
        if (listType == 34) {
            iconView!!.setScaleType(ScaleType.FIT_CENTER)
        } else {
            iconView!!.setIconSmall(true)
        }
        val height = MainApp.dp40
        layoutParams.height = height
        layoutParams.width = height
    }

    fun applyPreviewSize(drawable: Drawable?) {
        if (this.previewView == null || this.iconAltView != null || this.headerFrame == null) {
            return
        }
        try {
            val intrinsicWidth = drawable!!.getIntrinsicWidth()
            val intrinsicHeight = drawable.getIntrinsicHeight()
            var width = if (intrinsicWidth <= 0 || intrinsicHeight <= 0) 0 else (this.headerFrame!!.getWidth() - (MainApp.dp16 shl 1))
            if (intrinsicWidth <= 0 || intrinsicHeight <= 0 || width <= 0) {
                return
            }
            val aspect = (intrinsicWidth / intrinsicHeight).toFloat()
            val previewHeight = dpToPx(this.appContext, 140.0f).toInt()
            val previewWidth = Math.round(previewHeight * aspect)
            if (previewWidth <= width) {
                width = previewWidth
            }
            val myRoundImage = this.previewView
            this.previewView = null
            detachFromParent(myRoundImage)
            val layoutParams = FrameLayout.LayoutParams(width, previewHeight)
            layoutParams.gravity = 1
            val dp16Px = MainApp.dp16
            layoutParams.setMargins(dp16Px, dp16Px, dp16Px, dp16Px)
            this.headerFrame!!.addView(myRoundImage, layoutParams)
            this.iconAltView = myRoundImage
        } catch (unused: Exception) {
        }
    }

    fun loadPreview() {
        val referer: String?
        this.previewView!!.setVisibility(View.INVISIBLE)
        val name = this.item!!.name
        if (this.previewFailed) {
            val isApi = MainConst.isApi28
            referer = null
        } else {
            referer = name
        }
        this.previewUrl = MainUtil.glideUrlFor(this.appContext, name!!, referer)
        runAsync(LoadPreviewTask())
    }
    override fun dismiss() {
        if (appContext == null) {
            super.dismiss()
            return
        }
        val requests = glide
        if (requests != null) {
            previewView?.let { requests.clear(it) }
            iconAltView?.let { requests.clear(it) }
            glide = null
        }
        aliveToken = null
        iconView?.release()
        iconView = null
        iconAltView?.release()
        iconAltView = null
        previewView?.release()
        previewView = null
        loadCover?.release()
        loadCover = null
        listLoader?.release()
        listLoader = null
        hostActivity = null
        appContext = null
        item = null
        headerFrame = null
        previewUrl = null
        copyUrlA = null
        copyUrlB = null
        super.dismiss()
    }

    companion object {
    }
}
