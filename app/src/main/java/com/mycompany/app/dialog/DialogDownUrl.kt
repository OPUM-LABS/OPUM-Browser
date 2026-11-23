package com.mycompany.app.dialog

import com.mycompany.app.view.anyListener
import com.mycompany.app.main.RequestCode
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.PdfSettings
import java.util.ArrayList
import java.util.HashMap
import ch.opumlabs.opumbrowser.R as AppR
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.os.Build
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ImageView.ScaleType
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.RequestListener
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mycompany.app.compress.Compress.Companion.isSvgName
import com.mycompany.app.data.DataUtil.saveFile
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.DbUtil.updateById
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.db.book.DbBookQuick.Companion.iconRes
import com.mycompany.app.db.book.DbBookSearch.Companion.get
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.dialog.DialogDownSize.DownSizeListener
import com.mycompany.app.dialog.DialogPreview.PreviewListener
import com.mycompany.app.down.DownParseDzen
import com.mycompany.app.down.DownParseList
import com.mycompany.app.down.DownParseList.SortM3u8
import com.mycompany.app.down.DownParseVimeo.loadConfig
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainDownAdapter.DownListItem
import com.mycompany.app.main.MainDownAdapter.MainDownListener
import com.mycompany.app.main.MainDownSvc
import com.mycompany.app.main.MainDownSvc.M3u8Item
import com.mycompany.app.main.MainNative
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUri.splitNumber
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.treeDisplayPath
import com.mycompany.app.main.MainUri.findChild
import com.mycompany.app.main.MainUri.externalVolumes
import com.mycompany.app.main.MainUriDoc.renameNumbered
import com.mycompany.app.main.MainUriVol.createUriItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.parseIntSafe
import com.mycompany.app.main.MainUtil.urlDecode
import com.mycompany.app.main.MainUtil.pickTreeFolder
import com.mycompany.app.main.MainUtil.parseColorSafe
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.absoluteUrl
import com.mycompany.app.main.MainUtil.applyScreenRotation
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.feedbackText
import com.mycompany.app.main.MainUtil.hostOf
import com.mycompany.app.main.MainUtil.destroyWebViewNow
import com.mycompany.app.main.MainUtil.urlFromText
import com.mycompany.app.main.MainUtil.firstUrlIn
import com.mycompany.app.main.MainUtil.editTextValue
import com.mycompany.app.main.MainUtil.archiveKindOf
import com.mycompany.app.main.MainUtil.openHttpConnection
import com.mycompany.app.main.MainUtil.detachFromParent
import com.mycompany.app.main.MainUtil.fileExtOf
import com.mycompany.app.main.MainUtil.guessDownloadName
import com.mycompany.app.main.MainUtil.closeKeyboard
import com.mycompany.app.main.MainUtil.lowerExtOf
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.canUseBiometric
import com.mycompany.app.main.MainUtil.selectAllOnFocus
import com.mycompany.app.main.MainUtil.faceUrl
import com.mycompany.app.main.MainUtil.sizeLabel
import com.mycompany.app.main.MainUtil.safeTitleFileName
import com.mycompany.app.main.MainUtil.stripExtension
import com.mycompany.app.main.MainUtil.clearWebData
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.safeFileName
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.main.MainUtil.isFakeStream
import com.mycompany.app.main.MainUtil.defaultUserAgent
import com.mycompany.app.main.MainUtil.mimeForExt
import com.mycompany.app.main.MainUtil.copyFile
import com.mycompany.app.main.MainUtil.keepUriPermission
import com.mycompany.app.main.MainUtil.restartLater
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefPath
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.pref.PrefSet.remove
import com.mycompany.app.pref.PrefUtil.itemMode
import com.mycompany.app.pref.PrefUtil.setDetail
import com.mycompany.app.subtitle.Subtitle
import com.mycompany.app.view.GlideApp
import com.mycompany.app.view.GlideRequest
import com.mycompany.app.view.GlideRequests
import com.mycompany.app.view.MyPopupMenu
import com.mycompany.app.view.MyPopupMenu.MyPopupListener
import com.mycompany.app.ui.browser.DownRow
import com.mycompany.app.view.MyRoundImage
import com.mycompany.app.view.MyWebSafe
import com.mycompany.app.web.WebNestView
import com.mycompany.app.web.WebSnsLoad
import com.mycompany.app.web.WebSnsLoad.SnsLoadListener
import com.mycompany.app.web.WebViewActivity.FaceItem
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Parser
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.util.Collections
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * The download sheet (a link / image / video / page media from the browser, the download list, the image viewer):
 * file header (type icon and name, replaced by the picture preview once it loads), "file exists" note, name field
 * (size or the size button, settings), download folder (menu of known folders + "direct select") with open / share /
 * link details / play, the list of video variants (HLS, DASH, Reddit, Dzen, Vimeo, page face items) and the SNS
 * loader states (waiting, countdown, error + report), and Download / Fast download (or Retry / List / Link).
 * All the work is the old code; the old views became [Node] stand-ins that the Compose layout reads, except the
 * icon / preview images and the variant list, which stay the old views. Same constructor; shows itself once ready.
 */
@Suppress("deprecation")
class DialogDownUrl(
    mainActivity: android.app.Activity?,
    downUrlParam: String?,
    refererParam: String?,
    headerNameParam: String?,
    dispositionHeaderParam: String?,
    contentTypeParam: String?,
    fileSizeParam: Long,
    mimeTypeParam: Int,
    listStateParam: Int,
    arrayList: ArrayList<Any?>?,
    appCode: Int,
    list: MutableList<Any?>?,
    mode: Int,
    downUrlListener: DownUrlListener?
) : ComposeBottomDialog(mainActivity) {
    /** Stand-in for one old view: what the old code set on it, for the Compose layout to show. */
    inner class Node(initVis: Int = 0) {
        var vis by mutableIntStateOf(initVis)
        var textRes by mutableIntStateOf(0)
        var txt by mutableStateOf<CharSequence?>(null)
        var color by mutableIntStateOf(0)
        var dot by mutableStateOf(false)
        var busy by mutableStateOf(false)
        var count by mutableStateOf<String?>(null)
        var click: View.OnClickListener? = null
        var nodeWidth = 0
        var nodeHeight = 0
        fun setVisibility(newVis: Int) { vis = newVis }
        fun getVisibility(): Int = vis
        fun setText(newTxt: CharSequence?) { txt = newTxt; textRes = 0 }
        fun setText(res: Int) { textRes = res; txt = null }
        fun str(): String? {
            val s = (if (textRes != 0) appCtx?.getString(textRes) else txt?.toString()) ?: return null
            return s.trim { it <= ' ' }
        }
        fun shown(): String = (if (textRes != 0) appCtx?.getString(textRes) else txt?.toString()) ?: ""
        fun setTextColor(newColor: Int) { color = newColor }
        fun setOnClickListener(newClick: View.OnClickListener?) { click = newClick }
        fun setNoti(newDot: Boolean) { dot = newDot }
        fun post(r: Runnable?): Boolean = r != null && mainHandler?.post(r) == true
        fun postDelayed(r: Runnable?, ms: Long): Boolean = r != null && mainHandler?.postDelayed(r, ms) == true
        fun getWidth(): Int = nodeWidth
        fun getHeight(): Int = nodeHeight
        /** The busy spinner over the sheet. */
        fun setBusy(height: Int, progress: Int, newBusy: Boolean, animate: Boolean) { busy = newBusy }
        /** The countdown text in the loading cover. */
        fun setCount(color: Int, dp: Int, label: String?) { count = label }
        /** Old `MyLineFrame.addView`: the picture preview moved into the header. */
        fun addView(view: View?, lp: ViewGroup.LayoutParams?) { headerImage = view; headerImageW = lp?.width ?: 0; tick++ }
        fun setLineDn(enabled: Boolean) {}
        fun setDrawLine(enabled: Boolean) {}
        fun setBackgroundResource(transLogoShortBackColor: Int) {}
        fun setBackgroundColor(headColor: Int) {}
        fun setImageResource(outlineErrorDarkWeb: Int) {}
        fun setBgPreColor(press: Int) {}
        fun setBgNorColor(color: Int) {}
        fun setColors(color: Int, lineColor: Int) {}
    }

    @JvmField val mainHandler: Handler? = Handler(Looper.getMainLooper())
    @JvmField var previewImage: MyRoundImage? = null
    @JvmField var m3u8Items: ArrayList<M3u8Item?>? = null
    @JvmField var loadCover: Node? = null
    @JvmField var faceItems: ArrayList<Any?>?
    @JvmField var progressValue: Int = 0
    @JvmField var appCode: Int = 0
    @JvmField var statusText: Node? = null
    @JvmField var loadState: Byte = 0
    @JvmField var errorReport: Node? = null
    @JvmField var itemsIn: ArrayList<DownListItem?>? = null
    /** The list of found files (was a `MyRecyclerView` with `MainDownAdapter`; Compose rows since 2026-11-17). */
    @JvmField var listNode: Node? = null
    @JvmField var sizeList: MutableList<Any?>?
    private var downItems by mutableStateOf<List<DownListItem>>(emptyList())
    private var downListener: MainDownListener? = null
    @JvmField var downItemList: ArrayList<DownListItem?>? = null
    @JvmField var formNode: Node? = null
    @JvmField var downItemIn: DownListItem? = null
    @JvmField var mainButton: Node? = null
    @JvmField var m3u8In: M3u8Item? = null
    @JvmField var msgText: Node? = null
    @JvmField var faceIn: FaceItem? = null
    @JvmField var bodyToggle: Node? = null
    @JvmField var posterUrl: String? = null
    @JvmField var playNode: Node? = null
    @JvmField var sizeDialog: DialogDownSize? = null
    @JvmField var waitingText: Node? = null
    @JvmField var linkDialog: DialogDownLink? = null
    @JvmField var reportButton: Node? = null
    @JvmField var nameList: ArrayList<Any?>? = null
    @JvmField var fastButton: Node? = null
    /** The file can be split ("fast"): the one Download button then uses the Speed setting's connections (the
     *  separate "Fast download" button is gone, 2026-11-20). */
    @JvmField var fastAllowed: Boolean = false
    @JvmField var savedPath: String? = null
    @JvmField var downUrl: String?
    @JvmField var moreMenu: MyPopupMenu? = null
    @JvmField var referer: String?
    @JvmField var pendingName: String? = null
    @JvmField var urlListener: DownUrlListener?
    @JvmField var targetUri: UriItem? = null
    @JvmField var posterListener: DownPosterListener? = null
    @JvmField var nameEdited: Boolean = false
    @JvmField var posterSrc: String? = null
    @JvmField var imageOpened: Boolean = false
    @JvmField var iconTinted: Boolean = false
    @JvmField var dismissed: Boolean = false
    @JvmField var baseName: String? = null
    @JvmField var smallLayout: Boolean = false
    @JvmField var hostActivity: android.app.Activity?
    @JvmField var fileName: String? = null
    @JvmField var lineHidden: Boolean = false
    @JvmField var appCtx: Context?
    @JvmField var nameTyped: Boolean = false
    @JvmField var headerName: String?
    @JvmField var apkType: Boolean
    @JvmField var dispositionHeader: String?
    @JvmField var skipCheck: Boolean = false
    @JvmField var mimeType: Int
    @JvmField var iconFixed: Boolean = false
    @JvmField var hasDownloaded: Boolean
    @JvmField var sizeLoaded: Boolean = false
    @JvmField var pendingMessage: String? = null
    @JvmField var pendingTitle: String? = null
    @JvmField var iconRes: Int = 0
    @JvmField var refUrl: String? = null
    @JvmField var hasDownloadedCopy: Boolean = false
    @JvmField var rootNode: Node? = null
    @JvmField var canOpen: Boolean = false
    @JvmField var headerNode: Node? = null
    @JvmField var checkName: Boolean = false
    @JvmField var parseDone: Boolean = false
    @JvmField var bodyNode: Node? = null
    @JvmField var embedLoaded: Boolean = false
    @JvmField var iconIndex: Int = 0
    @JvmField var lineFrame: Node? = null
    @JvmField var actionBusy: Boolean = false
    @JvmField var pickIndex: Int = 0
    @JvmField var iconImage: MyRoundImage? = null
    @JvmField var contentType: String?
    @JvmField var downItemOut: DownListItem? = null
    @JvmField var nameText: Node? = null
    @JvmField var fileSize: Long
    @JvmField var faceOut: FaceItem? = null
    @JvmField var thumbImage: MyRoundImage? = null
    @JvmField var openMode: Int = 0
    @JvmField var progressMax: Int = 0
    @JvmField var lineNode: Node? = null
    @JvmField var listState: Byte
    @JvmField var errorType: Byte = 0
    @JvmField var existsText: Node? = null
    @JvmField var mediaState: Byte = 0
    @JvmField var progressNow: Int = 0
    @JvmField var nameLabel: Node? = null
    @JvmField var downType: Int = 0
    @JvmField var iconListener: RequestListener<Any?>?
    @JvmField var nameField: Node? = null
    @JvmField var coverRoot: Node? = null
    @JvmField var previewListener: RequestListener<Any?>?
    @JvmField var startState: Byte = 0
    @JvmField var pickedItem: DownListItem? = null
    @JvmField var sizeText: Node? = null
    @JvmField var webCoord: ViewGroup? = null
    @JvmField var pickedM3u8: M3u8Item? = null
    @JvmField var sizeLabel: Node? = null
    @JvmField var sourcePage: WebNestView? = null
    @JvmField var pickedFace: FaceItem? = null
    @JvmField var settingsNode: Node? = null
    @JvmField var snsState: Byte = 0
    @JvmField var pendingText: String? = null
    @JvmField var locationNode: Node? = null
    @JvmField var previewGlideUrl: GlideUrl? = null
    @JvmField var locationLabel: Node? = null
    @JvmField var glide: GlideRequests? = null
    @JvmField var pathNode: Node? = null
    @JvmField var iconColor: Int = 0
    @JvmField var moveAction: Node? = null
    @JvmField var previewFailed: Boolean = false
    @JvmField var shareAction: Node? = null
    @JvmField var snsLoad: WebSnsLoad? = null
    @JvmField var infoAction: Node? = null
    @JvmField var m3u8Url: String? = null
    @JvmField var playAction: Node? = null
    @JvmField var parseList: DownParseList? = null
    /** Whether the link was downloaded before (set by the duplicate check). */
    @JvmField var alreadyDownloaded: Boolean = false

    /** Bumped when the old code changes the real views (icon, preview, list), so the layout re-reads them. */
    private var tick by mutableIntStateOf(0)
    private var built by mutableStateOf(false)
    private var headerImage: View? = null
    private var headerImageW = 0
    private var coverTall by mutableStateOf(true)
    private var folderMenu by mutableStateOf(false)
    private var folderItems by mutableStateOf<List<String>>(emptyList())
    private var executor: java.util.concurrent.ExecutorService? = null

    init {
        var effectiveType = contentTypeParam
        var effectiveMime = mimeTypeParam
        this.iconListener = ApplyIconLoadListener()
        this.previewListener = LoadPreviewLoadListener()
        if (MainUtil.isFakeStream(downUrlParam)) {
            effectiveType = "video/*"
            effectiveMime = 5
        }
        this.hostActivity = mainActivity
        this.appCtx = getContext()
        this.downUrl = downUrlParam
        this.referer = refererParam
        this.contentType = effectiveType
        this.fileSize = fileSizeParam
        this.apkType = effectiveMime == 4
        this.listState = (listStateParam).toByte()
        this.faceItems = arrayList
        this.sizeList = list
        this.urlListener = downUrlListener
        if (arrayList != null && !arrayList.isEmpty()) {
            this.appCode = appCode
        }
        this.headerName = headerNameParam
        this.dispositionHeader = dispositionHeaderParam
        this.mimeType = effectiveMime
        this.hasDownloaded = alreadyDownloaded
        setSheet { Sheet() }
        runInBackground(ApplyInfoTask())
    }

    // ---------------------------------------------------------------------------------------------- layout

    private fun Modifier.dim(n: Node?): Modifier = if (n?.vis == 4) this.alpha(0f) else this

    private fun on(n: Node?): Boolean = n != null && n.vis != 8

    @Composable
    private fun ColumnScope.Sheet() {
        @Suppress("UNUSED_VARIABLE") val tickNow = tick
        if (!built) return
        val density = LocalDensity.current
        Box {
            Column(Modifier.fillMaxWidth().onGloballyPositioned { lineFrame?.nodeWidth = it.size.width; rootNode?.nodeWidth = it.size.width }) {
                Header()
                Column(
                    Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).dim(bodyNode)
                        .onGloballyPositioned { headerNode?.nodeHeight = it.size.height },
                ) {
                    if (on(bodyToggle)) Body()
                    val list = listNode
                    if (on(list)) {
                        val bg = list!!.color
                        Column(Modifier.fillMaxWidth().then(if (bg != 0) Modifier.background(Color(bg)) else Modifier)) {
                            downItems.forEachIndexed { pos, item ->
                                DownRow(
                                    hostActivity, item, pos, 0, referer,
                                    onClick = { downListener?.onDownload(item.index) },
                                    onInfo = { downListener?.onInfo(item.index) },
                                    onPlay = { downListener?.onPlay(item.index) },
                                )
                            }
                        }
                    }
                }
                if (on(formNode)) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).dim(formNode)
                            .onGloballyPositioned { formNode?.nodeHeight = it.size.height },
                        horizontalArrangement = Arrangement.End,
                    ) {
                        val fast = msgText
                        if (fast != null && on(fast)) {
                            TextButton(onClick = { fast.click?.onClick(null) }) { Text(fast.shown()) }
                        }
                        val main = mainButton
                        if (main != null) {
                            TextButton(onClick = { main.click?.onClick(null) }) { Text(main.shown()) }
                        }
                    }
                }
            }
            if (rootNode?.busy == true) {
                LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(horizontal = 28.dp))
            }
        }
        @Suppress("UNUSED_EXPRESSION") density
    }

    @Composable
    private fun Header() {
        @Suppress("UNUSED_VARIABLE") val tickNow = tick
        if (!on(lineFrame)) return
        val img = headerImage
        val big = thumbImage
        if (img != null && big != null && big.visibility == 0) {
            val w = with(LocalDensity.current) { headerImageW.toDp() }
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                AndroidView(
                    factory = { img },
                    modifier = (if (headerImageW > 0) Modifier.width(w) else Modifier.fillMaxWidth()).height(140.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            }
            return
        }
        Row(
            Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val icon = iconImage
            if (icon != null && icon.visibility == 0) {
                AndroidView(factory = { icon }, modifier = Modifier.size(40.dp).clip(CircleShape))
                Spacer(Modifier.width(16.dp))
            }
            val name = nameText
            if (name != null && on(name)) {
                Text(name.shown(), style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }

    @Composable
    private fun Body() {
        @Suppress("UNUSED_VARIABLE") val tickNow = tick
        val preview = previewImage
        if (on(lineNode)) Form()
        if (preview != null && preview.visibility != 8) {
            AndroidView(
                factory = { preview },
                modifier = Modifier.fillMaxWidth().height(140.dp).padding(horizontal = 16.dp).alpha(if (preview.visibility == 0) 1f else 0f),
            )
        }
        val cover = loadCover
        if (on(cover)) {
            Box(Modifier.fillMaxWidth().height(if (coverTall) 224.dp else 96.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(48.dp))
                val count = cover!!.count
                if (!count.isNullOrEmpty()) Text(count, style = MaterialTheme.typography.titleMedium)
            }
        }
        val note = statusText
        if (note != null && on(note)) {
            Text(
                note.shown(), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            )
        }
        val report = errorReport
        if (report != null && on(report)) {
            OutlinedButton(onClick = { report.click?.onClick(null) }, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                Text(report.shown())
            }
        }
    }

    @Composable
    private fun Form() {
        val exists = existsText
        if (exists != null && on(exists)) {
            Text(
                exists.shown(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceContainerHighest).padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }
        val ed = nameField ?: return
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = ed.shown(),
                onValueChange = { onNameChanged(it) },
                label = { Text(nameLabel?.shown() ?: "") },
                singleLine = true,
                supportingText = if (on(sizeText)) { { Text(sizeText!!.shown()) } } else null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onNameDone() }),
                modifier = Modifier.weight(1f),
            )
            val size = sizeLabel
            if (size != null && on(size)) {
                IconButton(onClick = { size.click?.onClick(null) }) {
                    Icon(painterResource(AppR.drawable.outline_contact_support_black_20), stringResource(AppR.string.size))
                }
            }
            val set = settingsNode
            if (set != null && on(set)) {
                IconButton(onClick = { set.click?.onClick(null) }) {
                    Icon(painterResource(AppR.drawable.outline_settings_black_20), stringResource(AppR.string.setting))
                }
            }
        }
        val loc = locationNode
        if (loc != null && on(loc)) {
            Box {
                Row(
                    Modifier.fillMaxWidth().clickable { loc.click?.onClick(null) }.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        BadgedBox(badge = { if (loc.dot) Badge() }) {
                            Text(locationLabel?.shown() ?: "", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        val path = pathNode
                        Text(
                            path?.shown() ?: "", style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis,
                            color = if (path?.color == -769226) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                com.mycompany.app.ui.components.BubbleMenu(expanded = folderMenu, onDismiss = { folderMenu = false }) {
                    folderItems.forEachIndexed { id, label ->
                        DropdownMenuItem(text = { Text(label) }, onClick = { folderMenu = false; DismissNowStep().onItem(null, id) })
                    }
                    DropdownMenuItem(
                        text = { Text(stringResource(AppR.string.direct_select)) },
                        onClick = { folderMenu = false; DismissNowStep().onItem(null, folderItems.size) },
                    )
                }
            }
            // the file's actions with labels (they were four unlabelled round icons next to the location)
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly,
            ) {
                Action(moveAction, AppR.drawable.outline_apps_black_24, AppR.string.open_with)
                Action(shareAction, AppR.drawable.outline_share_black_24, AppR.string.share)
                Action(infoAction, AppR.drawable.outline_info_black_24, AppR.string.info)
                Action(playAction, AppR.drawable.baseline_play_arrow_black_24, AppR.string.play)
            }
        }
    }

    @Composable
    private fun Action(node: Node?, icon: Int, label: Int) {
        if (node == null || !on(node)) return
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.widthIn(min = 64.dp)) {
            FilledTonalIconButton(onClick = { node.click?.onClick(null) }) {
                Icon(painterResource(icon), null)
            }
            Text(stringResource(label), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }

    // ---------------------------------------------------------------------------------------------- old callbacks

    /** Name field changed. */
    private fun onNameChanged(res: String) {
        nameField?.setText(res)
        if (this.nameTyped || MainUtil.sameObject(this.fileName, res)) return
        this.nameTyped = true
    }

    /** Done on the keyboard. */
    private fun onNameDone() {
        if (nameField == null || actionBusy) return
        actionBusy = true
        mainHandler?.post {
            checkFileName(fastAllowed || msgText != null)
            actionBusy = false
        }
    }

    /** Folder row (old `LocationNodeClick`): clears the one-time dot, then the folder menu or the system picker. */
    internal inner class LocationNodeClick : View.OnClickListener {
        override fun onClick(view: View?) {
            if (actionBusy) return
            actionBusy = true
            mainHandler?.post {
                if (ReadSettings.notiPath.value) {
                    ReadSettings.edit(appCtx) { set(ReadSettings.notiPath, false) }
                    locationNode?.setNoti(false)
                }
                val list = nameList
                if (list == null || list.isEmpty()) {
                    pickTreeFolder(hostActivity, MainUri.storageVolume())
                } else if (hostActivity != null && locationNode != null) {
                    val ctx = appCtx
                    if (ctx != null) {
                        folderItems = list.map { MainUri.volumeLabel(ctx, it as String?) }
                        folderMenu = true
                    }
                }
                actionBusy = false
            }
        }
    }

    fun runInBackground(runnable: Runnable?) {
        var ex = executor
        if (ex == null || ex.isShutdown() || ex.isTerminated()) {
            ex = MainApp.executor(context) ?: return
            executor = ex
        }
        try { ex.execute(runnable) } catch (unused: Exception) { }
    }

    /** Landscape. */
    fun isLandscapeLayout(): Boolean = (hostActivity as? MainActivity)?.isLandscapeScreen() ?: MainUtil.isLandscape(context)

    /** Keyboard shown. */
    fun isSmallLayout(): Boolean = (hostActivity as? MainActivity)?.smallScreen ?: MainUtil.isSmallScreen(context)

    // ---------------------------------------------------------------------------------------------- build

    fun buildNodes() {
        val context = this.appCtx ?: return
        rootNode = Node()
        headerNode = Node()
        bodyNode = Node()
        lineFrame = Node()
        bodyToggle = Node()
        lineNode = Node()
        existsText = Node(8).also { it.setText(AppR.string.exist_file) }
        nameLabel = Node().also { it.setText(AppR.string.name) }
        nameField = Node()
        sizeText = Node(8)
        sizeLabel = Node(8)
        settingsNode = Node(8)
        val icon = MyRoundImage(context)
        icon.setScaleType(ScaleType.CENTER_CROP)
        icon.setCircleRadius(MainApp.dp40 / 2.0f)
        iconImage = icon
        nameText = Node()
        handlerPostBuild()
    }

    private fun handlerPostBuild() {
        mainHandler?.post(SetScaleTypeTask())
    }

    internal inner class SetScaleTypeTask : Runnable {
        override fun run() {
            val context = appCtx ?: return
            if (rootNode == null) return
            locationNode = Node()
            locationLabel = Node().also { it.setText(AppR.string.down_location) }
            pathNode = Node()
            moveAction = Node(8)
            shareAction = Node(8)
            infoAction = Node(8)
            playNode = Node(8)
            val preview = MyRoundImage(context)
            preview.setScaleType(ScaleType.FIT_CENTER)
            preview.setVisibility(View.GONE)
            previewImage = preview
            loadCover = Node(8)
            waitingText = Node(8).also { it.setText(AppR.string.waiting) }
            reportButton = Node(8).also { it.setText(AppR.string.report_error) }
            listNode = Node(8)
            formNode = Node()
            mainButton = Node().also { it.setText(AppR.string.download) }
            fastButton = Node(8).also { it.setText(AppR.string.fast_down) }
            mainHandler?.post(UpdateSizeTask())
        }
    }

    internal inner class UpdateSizeTask : Runnable {
        override fun run() {
            val dialogDownUrl = this@DialogDownUrl
            val listState = dialogDownUrl.listState
            val pendingMessage = dialogDownUrl.pendingMessage
            val iconRes = dialogDownUrl.iconRes
            val hasDownloadedCopy = dialogDownUrl.hasDownloadedCopy
            val canOpen = dialogDownUrl.canOpen
            dialogDownUrl.pendingMessage = null
            if (dialogDownUrl.rootNode == null || dialogDownUrl.appCtx == null) {
                return
            }
            val downType = dialogDownUrl.downType
            val faces = dialogDownUrl.faceItems
            if (downType == 1 || downType == 3 || downType == 5 || downType == 7 || downType == 9 || downType == 13 || listState.toInt() != 0 || (faces != null && !faces.isEmpty()) || dialogDownUrl.loadState.toInt() != 0) {
                if (faces != null && !faces.isEmpty()) {
                    coverTall = false
                }
                dialogDownUrl.lineFrame!!.setLineDn(true)
                dialogDownUrl.lineNode!!.setVisibility(8)
                dialogDownUrl.formNode!!.setVisibility(8)
                dialogDownUrl.loadCover!!.setVisibility(0)
                dialogDownUrl.coverRoot = dialogDownUrl.rootNode
            }
            dialogDownUrl.moveAction!!.setVisibility(0)
            dialogDownUrl.shareAction!!.setVisibility(0)
            dialogDownUrl.infoAction!!.setVisibility(0)
            if (canOpen) {
                val play = dialogDownUrl.playNode!!
                dialogDownUrl.playAction = play
                play.setVisibility(0)
            }
            val mediaState = if (!dialogDownUrl.skipCheck) dialogDownUrl.mediaState else 0
            if (!dialogDownUrl.skipCheck && mediaState.toInt() != 3 && dialogDownUrl.downType != 13 && (mediaState.toInt() != 1 || URLUtil.isNetworkUrl(dialogDownUrl.downUrl))) {
                dialogDownUrl.fastAllowed = true
            }
            if (listState.toInt() != 0) {
                dialogDownUrl.baseName = pendingMessage
            } else {
                dialogDownUrl.setFileName(pendingMessage)
            }
            if (ReadSettings.notiPath.value) {
                dialogDownUrl.locationNode!!.setNoti(true)
            }
            dialogDownUrl.locationNode!!.setOnClickListener(dialogDownUrl.LocationNodeClick())
            val faceItems = dialogDownUrl.faceItems
            if (dialogDownUrl.downType != 13 && listState.toInt() == 0 && (faceItems == null || faceItems.isEmpty()) && dialogDownUrl.loadState.toInt() == 0) {
                dialogDownUrl.updateSize()
            }
            dialogDownUrl.sizeLabel!!.setOnClickListener(dialogDownUrl.SizeLabelClick())
            dialogDownUrl.settingsNode!!.setVisibility(0)
            dialogDownUrl.settingsNode!!.setOnClickListener(dialogDownUrl.SettingsNodeClick())
            dialogDownUrl.moveAction?.setOnClickListener(dialogDownUrl.MoveActionClick())
            dialogDownUrl.shareAction?.setOnClickListener(dialogDownUrl.OpenAppClick())
            dialogDownUrl.infoAction?.setOnClickListener(dialogDownUrl.InfoActionClick())
            dialogDownUrl.playAction?.setOnClickListener(dialogDownUrl.PlayActionClick())
            dialogDownUrl.mainButton!!.setOnClickListener(dialogDownUrl.MainButtonClick())
            dialogDownUrl.msgText?.setOnClickListener(dialogDownUrl.MsgTextClick())
            dialogDownUrl.updateLayout(dialogDownUrl.isLandscapeLayout())
            val startState = dialogDownUrl.startState
            if (startState.toInt() == 1) {
                dialogDownUrl.startSnsLoad(0, 0, dialogDownUrl.downUrl!!)
            } else if (startState.toInt() == 2) {
                dialogDownUrl.startSnsLoad(0, 1, dialogDownUrl.downUrl!!)
            }
            dialogDownUrl.nameText!!.setText(pendingMessage)
            dialogDownUrl.parseDone = hasDownloadedCopy
            dialogDownUrl.iconIndex = iconRes
            built = true
            tick++
            dialogDownUrl.IconClick().onClick(null)
        }
    }

    // ---------------------------------------------------------------------------------------------- old code

    internal inner class ApplyInfoTask : Runnable {

        internal inner class DismissStep : Runnable {
            override fun run() {
                this@DialogDownUrl.dismiss()
            }
        }

        override fun run() {
            val dialogDownUrl = this@DialogDownUrl
            try {
                dialogDownUrl.applyInfo()
            } catch (unused: Exception) {
                val handler = dialogDownUrl.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(DismissStep())
            }
        }
    }

    internal inner class SettingsNodeClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogDownUrl = this@DialogDownUrl
            val mainActivity = dialogDownUrl.hostActivity
            if (mainActivity == null) {
                return
            }
            applyScreenRotation(mainActivity, false)
            val intent = Intent(dialogDownUrl.appCtx, com.mycompany.app.ui.settings.DownSettingsActivity::class.java as Class<*>)
            intent.putExtra("EXTRA_PATH", dialogDownUrl.referer)
            dialogDownUrl.hostActivity!!.startActivity(intent)
        }
    }

    internal inner class MoveActionClick : View.OnClickListener {

        internal inner class OpenWithStep : Runnable {
            override fun run() {
                val dialogDownUrl = this@DialogDownUrl
                if (dialogDownUrl.urlListener == null) {
                    return
                }
                dialogDownUrl.urlListener!!.openWith(
                    firstUrlIn(dialogDownUrl.downUrl),
                    dialogDownUrl.nameField?.str(),
                    dialogDownUrl.contentType
                )
                dialogDownUrl.actionBusy = false
            }
        }

        override fun onClick(view: View?) {
            val dialogDownUrl = this@DialogDownUrl
            if (dialogDownUrl.urlListener == null || dialogDownUrl.actionBusy) {
                return
            }
            dialogDownUrl.actionBusy = true
            val handler = dialogDownUrl.mainHandler
            if (handler == null) {
                return
            }
            handler.post(OpenWithStep())
        }
    }

    internal inner class OpenAppClick : View.OnClickListener {

        internal inner class OpenAppStep : Runnable {
            override fun run() {
                val dialogDownUrl = this@DialogDownUrl
                if (dialogDownUrl.urlListener == null) {
                    return
                }
                dialogDownUrl.urlListener!!.openApp(firstUrlIn(dialogDownUrl.downUrl), dialogDownUrl.nameField?.str())
                dialogDownUrl.actionBusy = false
            }
        }

        override fun onClick(view: View?) {
            val dialogDownUrl = this@DialogDownUrl
            if (dialogDownUrl.urlListener == null || dialogDownUrl.actionBusy) {
                return
            }
            dialogDownUrl.actionBusy = true
            val handler = dialogDownUrl.mainHandler
            if (handler == null) {
                return
            }
            handler.post(OpenAppStep())
        }
    }

    internal inner class InfoActionClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogDownUrl = this@DialogDownUrl
            val firstUrl = firstUrlIn(dialogDownUrl.downUrl)
            dialogDownUrl.pickedItem = null
            dialogDownUrl.pickedM3u8 = null
            dialogDownUrl.pickedFace = null
            dialogDownUrl.openLinkDialog(firstUrl)
        }
    }

    internal inner class PlayActionClick : View.OnClickListener {

        internal inner class PlayStep : Runnable {
            override fun run() {
                val dialogDownUrl = this@DialogDownUrl
                val downUrlListener = dialogDownUrl.urlListener
                if (downUrlListener == null) {
                    return
                }
                dialogDownUrl.downItemIn = null
                dialogDownUrl.m3u8In = null
                dialogDownUrl.faceIn = null
                val sizeLoaded = dialogDownUrl.sizeLoaded
                val downUrl = dialogDownUrl.downUrl
                if (sizeLoaded) {
                    downUrlListener.preview(downUrl, null, dialogDownUrl.contentType, true)
                } else {
                    dialogDownUrl.urlListener!!.preview(
                        firstUrlIn(downUrl),
                        dialogDownUrl.nameField?.str(),
                        dialogDownUrl.contentType,
                        false
                    )
                }
                dialogDownUrl.actionBusy = false
            }
        }

        override fun onClick(view: View?) {
            val dialogDownUrl = this@DialogDownUrl
            if (dialogDownUrl.urlListener == null || dialogDownUrl.actionBusy) {
                return
            }
            dialogDownUrl.actionBusy = true
            val handler = dialogDownUrl.mainHandler
            if (handler == null) {
                return
            }
            handler.post(PlayStep())
        }
    }

    internal inner class MainButtonClick : View.OnClickListener {

        internal inner class MainActionStep : Runnable {

            internal inner class UpdateListTask : Runnable {
                override fun run() {
                    val mainButtonClick = this@MainButtonClick
                    val dialogDownUrl = this@DialogDownUrl
                    dialogDownUrl.updateList(true)
                    this@DialogDownUrl.actionBusy = false
                }
            }

            override fun run() {
                val dialogDownUrl = this@DialogDownUrl
                if (dialogDownUrl.snsLoad == null) {
                    dialogDownUrl.checkFileName(dialogDownUrl.fastAllowed)
                    dialogDownUrl.actionBusy = false
                    return
                }
                val arrayList = dialogDownUrl.faceItems
                if (arrayList == null || arrayList.isEmpty()) {
                    dialogDownUrl.showSnsWaiting()
                    dialogDownUrl.actionBusy = false
                    return
                }
                dialogDownUrl.cancelSnsLoad()
                val handler = dialogDownUrl.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(UpdateListTask())
            }
        }

        override fun onClick(view: View?) {
            val dialogDownUrl = this@DialogDownUrl
            val appCompatTextView = dialogDownUrl.mainButton
            if (appCompatTextView == null || dialogDownUrl.actionBusy) {
                return
            }
            dialogDownUrl.actionBusy = true
            appCompatTextView.post(MainActionStep())
        }
    }

    internal inner class MsgTextClick : View.OnClickListener {

        internal inner class ShowFaceItemsStep : Runnable {
            override fun run() {
                val dialogDownUrl = this@DialogDownUrl
                val arrayList: ArrayList<Any?>? = if (dialogDownUrl.snsLoad == null) null else dialogDownUrl.faceItems
                if (dialogDownUrl.snsLoad == null || arrayList == null || arrayList.isEmpty()) {
                    dialogDownUrl.checkFileName(true)
                } else {
                    dialogDownUrl.showSnsWaiting()
                }
                dialogDownUrl.actionBusy = false
            }
        }

        override fun onClick(view: View?) {
            val dialogDownUrl = this@DialogDownUrl
            val myLineText = dialogDownUrl.msgText
            if (myLineText == null || dialogDownUrl.actionBusy) {
                return
            }
            dialogDownUrl.actionBusy = true
            myLineText.post(ShowFaceItemsStep())
        }
    }

    internal inner class IconClick {

        fun onClick(view: View?) {
            val dialogDownUrl = this@DialogDownUrl
            val parseDone = dialogDownUrl.parseDone
            val iconIndex = dialogDownUrl.iconIndex
            if (dialogDownUrl.rootNode == null || dialogDownUrl.appCtx == null) {
                return
            }
            dialogDownUrl.show()
            if (dialogDownUrl.listState.toInt() == 0 && dialogDownUrl.coverRoot != null) {
                if (dialogDownUrl.loadState.toInt() != 1) {
                    val downType = dialogDownUrl.downType
                    val myCoverView = dialogDownUrl.loadCover
                    if (downType == 13) {
                        if (myCoverView != null) {
                            dialogDownUrl.setWaiting(true)
                            dialogDownUrl.loadCover!!.postDelayed(
                                dialogDownUrl.CoverClick(),
                                5000L
                            )
                            dialogDownUrl.runInBackground(dialogDownUrl.PrepareUrlTask())
                        }
                    } else if (myCoverView != null) {
                        if (parseDone) {
                            dialogDownUrl.updateList(false)
                        } else {
                            val list = dialogDownUrl.sizeList
                            if (list != null) {
                                try {
                                    val it: MutableIterator<Any?> = list.iterator()
                                    while (it.hasNext()) {
                                        if (dialogDownUrl.downUrl == (it.next() as FaceItem).url) {
                                            dialogDownUrl.updateList(false)
                                            break
                                        }
                                    }
                                    if (dialogDownUrl.loadCover != null) {
                                        dialogDownUrl.setWaiting(true)
                                        dialogDownUrl.loadCover!!.postDelayed(
                                            dialogDownUrl.CoverClick(),
                                            5000L
                                        )
                                    }
                                    dialogDownUrl.runInBackground(dialogDownUrl.FindVideoVariantsTask())
                                } catch (unused: Exception) {
                                }
                            } else {
                                if (dialogDownUrl.loadCover != null) {
                                    dialogDownUrl.setWaiting(true)
                                    dialogDownUrl.loadCover!!.postDelayed(
                                        dialogDownUrl.CoverClick(),
                                        5000L
                                    )
                                }
                                dialogDownUrl.runInBackground(dialogDownUrl.FindVideoVariantsTask())
                            }
                        }
                    }
                } else if (dialogDownUrl.loadCover != null) {
                    dialogDownUrl.downItemList = dialogDownUrl.itemsIn
                    dialogDownUrl.lineFrame!!.setLineDn(true)
                    dialogDownUrl.lineNode!!.setVisibility(8)
                    dialogDownUrl.formNode!!.setVisibility(8)
                    dialogDownUrl.loadCover!!.setVisibility(8)
                    val handler = dialogDownUrl.mainHandler
                    if (handler != null) {
                        handler.post(dialogDownUrl.PickFirstItemTask())
                    }
                }
            }
            dialogDownUrl.posterSrc = dialogDownUrl.downUrl
            val handler = dialogDownUrl.mainHandler
            if (handler != null) {
                handler.post(dialogDownUrl.ApplyIconTask())
            }
        }
    }

    internal inner class ApplyIconTask : Runnable {
        override fun run() {
            val dialogDownUrl = this@DialogDownUrl
            val iconIndex = dialogDownUrl.iconIndex
            val downPosterListener: DownPosterListener? = if (dialogDownUrl.openMode == 5) dialogDownUrl.posterListener else null
            if (dialogDownUrl.openMode == 5 && downPosterListener != null) {
                val posterUrl = downPosterListener.posterUrl(dialogDownUrl.downUrl)
                if (!TextUtils.isEmpty(posterUrl)) {
                    dialogDownUrl.posterSrc = posterUrl
                    dialogDownUrl.iconTinted = true
                }
            }
            dialogDownUrl.applyIcon(iconIndex)
        }
    }

    internal inner class BuildNodesTask : Runnable {
        override fun run() {
            this@DialogDownUrl.buildNodes()
        }
    }

    internal inner class SetWaitingStep : SnsLoadListener {
        override fun onStart() {
            val dialogDownUrl = this@DialogDownUrl
            val appCompatTextView: Node? = if (dialogDownUrl.listState.toInt() == 2) dialogDownUrl.statusText else null
            if (dialogDownUrl.listState.toInt() == 2 && appCompatTextView != null && dialogDownUrl.loadCover != null && appCompatTextView.getVisibility() == 0 && dialogDownUrl.loadCover!!.getVisibility() == 0) {
                dialogDownUrl.statusText!!.setText(AppR.string.insta_down)
            }
        }

        override fun onResult(pickIndex: Int, arrayList: ArrayList<DownListItem?>?) {
            val dialogDownUrl = this@DialogDownUrl
            if (dialogDownUrl.snsLoad != null) {
                dialogDownUrl.downItemList = arrayList
                dialogDownUrl.pickIndex = pickIndex
                val handler = dialogDownUrl.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(dialogDownUrl.PickFirstTask())
            }
        }

        override fun onError(errorType: Int) {
            val dialogDownUrl = this@DialogDownUrl
            val webSnsLoad = dialogDownUrl.snsLoad
            if (webSnsLoad == null) {
                return
            }
            dialogDownUrl.errorType = (errorType).toByte()
            val myWebSafe = webSnsLoad.webSafe
            if (myWebSafe != null) {
                webSnsLoad.loading = false
                webSnsLoad.retryPosted = false
                webSnsLoad.retryCount = 0
                webSnsLoad.pageLoading = false
                myWebSafe.stopLoading()
            }
            dialogDownUrl.loadCover!!.setVisibility(8)
            dialogDownUrl.setWaiting(false)
            dialogDownUrl.statusText!!.setText("Error code : 1")
            dialogDownUrl.statusText!!.setVisibility(0)
            dialogDownUrl.formNode!!.setVisibility(0)
            val arrayList = dialogDownUrl.faceItems
            if (arrayList == null || arrayList.isEmpty()) {
                val myLineText = dialogDownUrl.msgText
                if (myLineText != null) {
                    myLineText.setVisibility(8)
                }
                dialogDownUrl.mainButton!!.setText(AppR.string.retry)
            } else {
                val msgText = dialogDownUrl.msgText
                if (msgText != null) {
                    msgText.setVisibility(0)
                    dialogDownUrl.msgText!!.setText(AppR.string.retry)
                }
                dialogDownUrl.mainButton!!.setText(AppR.string.list)
            }
            if (dialogDownUrl.errorReport == null) {
                val myButtonText = dialogDownUrl.reportButton!!
                dialogDownUrl.errorReport = myButtonText
                if (AppState.darkUi) {
                    myButtonText.setTextColor(-328966)
                    dialogDownUrl.errorReport!!.setColors(-16777216, -14211289)
                } else {
                    myButtonText.setTextColor(-16777216)
                    dialogDownUrl.errorReport!!.setColors(-460552, 553648128)
                }
                dialogDownUrl.errorReport!!.setOnClickListener(dialogDownUrl.ErrorReportClick())
            }
            dialogDownUrl.errorReport!!.setVisibility(0)
        }
    }

    internal inner class PickFirstTask : Runnable {
        override fun run() {
            this@DialogDownUrl.pickFirst()
        }
    }

    internal inner class ShowLoadErrorTask : Runnable {
        override fun run() {
            val dialogDownUrl = this@DialogDownUrl
            val downListItem = dialogDownUrl.downItemOut
            val faceItem = dialogDownUrl.faceOut
            dialogDownUrl.downItemOut = null
            dialogDownUrl.faceOut = null
            if (downListItem == null) {
                dialogDownUrl.showLoadError()
                return
            }
            if (dialogDownUrl.locationNode == null) {
                return
            }
            dialogDownUrl.nameTyped = false
            dialogDownUrl.setFileName(dialogDownUrl.baseName)
            dialogDownUrl.locationNode!!.setVisibility(0)
            val myLineText = dialogDownUrl.msgText
            if (myLineText != null) {
                myLineText.setVisibility(0)
            }
            if (dialogDownUrl.downItemList!!.size != 1) {
                dialogDownUrl.runInBackground(dialogDownUrl.LoadDownloadsTask())
                return
            }
            if (faceItem != null) {
                dialogDownUrl.posterUrl = faceItem.posterUrl
                dialogDownUrl.m3u8Url = joinUrl(dialogDownUrl.downUrl, downListItem.title)
            } else {
                dialogDownUrl.posterUrl = downListItem.audioUrl
                if (dialogDownUrl.snsState.toInt() == 1 && "JPG" == downListItem.fmt) {
                    val nameWithoutExt = MainUtil.stripExtension(dialogDownUrl.nameField?.str())
                    if (TextUtils.isEmpty(nameWithoutExt)) {
                        dialogDownUrl.nameField!!.setText("Instagram.jpg")
                    } else {
                        dialogDownUrl.nameField!!.setText(StringBuilder().append(nameWithoutExt).append(".jpg").toString())
                    }
                }
            }
            dialogDownUrl.downUrl = downListItem.url
            dialogDownUrl.openExternal()
        }
    }

    internal inner class LoadDownloadsTask : Runnable {
        override fun run() {
            val downloaded: MutableList<String?>?
            val hasFailed: Boolean
            val downFail: MutableList<String?>?
            val dialogDownUrl = this@DialogDownUrl
            val downUrlListener = dialogDownUrl.urlListener
            if (downUrlListener != null) {
                val webNestViewA = downUrlListener.currentWeb()
                var index = 0
                if (webNestViewA != null) {
                    downloaded = webNestViewA.downloaded
                    alreadyDownloaded = if (downloaded == null || downloaded.isEmpty()) false else true
                    downFail = webNestViewA.downFail
                    hasFailed = if (downFail == null || downFail.isEmpty()) false else true
                } else {
                    downloaded = null
                    alreadyDownloaded = false
                    hasFailed = false
                    downFail = null
                }
                if (alreadyDownloaded || hasFailed) {
                    val arrayList = dialogDownUrl.downItemList
                    val size = arrayList!!.size
                    while (index < size) {
                        val entry: Any? = arrayList.get(index)
                        index++
                        val downListItem = entry as DownListItem?
                        if (downListItem != null) {
                            val url = downListItem.url
                            if (hasFailed && downFail!!.contains(url)) {
                                downListItem.checkState = 2.toByte()
                            } else if (alreadyDownloaded && downloaded!!.contains(url)) {
                                downListItem.checkState = 1.toByte()
                            }
                        }
                    }
                }
                val handler = dialogDownUrl.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(dialogDownUrl.ShowDownloadsTask())
            }
        }
    }

    internal inner class ShowDownloadsTask : Runnable {
        override fun run() {
            val dialogDownUrl = this@DialogDownUrl
            val mainActivity = dialogDownUrl.hostActivity
            if (mainActivity != null) {
                dialogDownUrl.downListener = dialogDownUrl.DownItemListener()
                dialogDownUrl.downItems = dialogDownUrl.downItemList?.filterIsInstance<DownListItem>() ?: emptyList()
                val handler = dialogDownUrl.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(dialogDownUrl.RefreshListTask())
            }
        }
    }

    internal inner class DownItemListener : MainDownListener {

        internal inner class DownItemStep : Runnable {
            override fun run() {
                val dialogDownUrl = this@DialogDownUrl
                val progressMax = dialogDownUrl.progressMax
                dialogDownUrl.downItemIn = null
                dialogDownUrl.m3u8In = null
                dialogDownUrl.faceIn = null
                var downUrl = dialogDownUrl.downUrl
                val downListItemB = dialogDownUrl.downItemAt(progressMax)
                if (downListItemB != null) {
                    downUrl = downListItemB.url
                    dialogDownUrl.downItemIn = downListItemB
                }
                val typedName = dialogDownUrl.nameField?.str()
                val downUrlListener = dialogDownUrl.urlListener
                if (downUrlListener != null) {
                    downUrlListener.preview(downUrl, typedName, dialogDownUrl.contentType, false)
                }
                dialogDownUrl.actionBusy = false
            }
        }

        override fun onDownload(position: Int) {
            val dialogDownUrl = this@DialogDownUrl
            val downListItemB = dialogDownUrl.downItemAt(position)
            if (downListItemB == null) {
                dialogDownUrl.updateList(false)
                return
            }
            val url = downListItemB.url
            dialogDownUrl.m3u8Url = url
            dialogDownUrl.downUrl = url
            dialogDownUrl.posterUrl = downListItemB.audioUrl
            dialogDownUrl.fileSize = downListItemB.fileSize
            dialogDownUrl.setExtension(downListItemB.title)
            dialogDownUrl.openExternal()
        }

        override fun onPlay(position: Int) {
            val dialogDownUrl = this@DialogDownUrl
            if (dialogDownUrl.urlListener == null || dialogDownUrl.actionBusy) {
                return
            }
            dialogDownUrl.actionBusy = true
            dialogDownUrl.progressMax = position
            val handler = dialogDownUrl.mainHandler
            if (handler == null) {
                return
            }
            handler.post(DownItemStep())
        }

        override fun onInfo(position: Int) {
            val dialogDownUrl = this@DialogDownUrl
            var downUrl = dialogDownUrl.downUrl
            val downListItemB = dialogDownUrl.downItemAt(position)
            if (downListItemB != null) {
                downUrl = downListItemB.url
            }
            dialogDownUrl.pickedM3u8 = null
            dialogDownUrl.pickedFace = null
            dialogDownUrl.pickedItem = downListItemB
            dialogDownUrl.openLinkDialog(downUrl)
        }
    }

    internal inner class RefreshListTask : Runnable {
        override fun run() {
            this@DialogDownUrl.updateList(true)
        }
    }

    internal inner class SetProgressTask : Runnable {
        override fun run() {
            val dialogDownUrl = this@DialogDownUrl
            val myCoverView: Node? = if (dialogDownUrl.coverRoot == null) null else dialogDownUrl.loadCover
            if (dialogDownUrl.coverRoot == null || myCoverView == null || myCoverView.getVisibility() != 0) {
                return
            }
            dialogDownUrl.setProgress(dialogDownUrl.progressValue - 1)
        }
    }

    internal inner class CoverClick : Runnable {
        override fun run() {
            val dialogDownUrl = this@DialogDownUrl
            val myCoverView: Node? = if (dialogDownUrl.coverRoot == null) null else dialogDownUrl.loadCover
            if (dialogDownUrl.coverRoot == null || myCoverView == null || myCoverView.getVisibility() != 0) {
                return
            }
            dialogDownUrl.statusText!!.setVisibility(0)
        }
    }

    internal inner class ErrorReportClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogDownUrl = this@DialogDownUrl
            if (dialogDownUrl.hostActivity != null) {
                val sb = StringBuilder()
                val errorType = dialogDownUrl.errorType
                if (errorType.toInt() == 0) {
                    sb.append("X download error")
                } else if (errorType.toInt() == 1) {
                    sb.append("Instagram download error")
                } else {
                    sb.append("Facebook download error")
                }
                sb.append("\n\nError code : 1\n\n")
                sb.append(feedbackText(dialogDownUrl.appCtx, dialogDownUrl.referer))
                try {
                    val intent = Intent("android.intent.action.SENDTO")
                    intent.setData(Uri.parse("mailto:"))
                    intent.putExtra(
                        "android.intent.extra.EMAIL",
                        arrayOf<String?>("app@opum.ch")
                    )
                    intent.putExtra(
                        "android.intent.extra.SUBJECT",
                        dialogDownUrl.appCtx!!.getString(AppR.string.download)
                    )
                    intent.putExtra("android.intent.extra.TEXT", sb.toString())
                    dialogDownUrl.hostActivity!!.startActivity(intent)
                } catch (unused: ActivityNotFoundException) {
                    showToast(dialogDownUrl.appCtx, AppR.string.apps_none)
                } catch (unused2: Exception) {
                    showToast(dialogDownUrl.appCtx, AppR.string.apps_none)
                }
            }
        }
    }

    internal inner class FindVideoVariantsTask : Runnable {
        /**
         * Background lookup of the downloadable variants of a video source, by source type
         * ([.n1]): HLS playlists, Embedly/Reddit embeds (DASH), DASH manifests, Dzen and Vimeo.
         * Fills [.A1] (or uses the page's face items [.B1]), builds the dialog list
         * [.G1] and posts [PickItemTask] to show it.
         */
        override fun run() {
            val d = this@DialogDownUrl
            if (d.loadCover == null) {
                return
            }
            if (d.loadState.toInt() == 2) {
                // Page url: look for a playlist url in the page source.
                try {
                    val page = Jsoup.connect(d.downUrl!!).referrer(d.referer!!).userAgent(MainUtil.defaultUserAgent(d.appCtx)).get()
                    val found: String? = m3u8Base(page.html())
                    if (URLUtil.isNetworkUrl(found)) {
                        d.downType = 1
                        d.downUrl = found
                        d.parseM3u8List()
                    }
                } catch (unused: Exception) {
                } catch (unused: LinkageError) {
                } catch (unused: OutOfMemoryError) {
                }
            } else if (d.downType == 1) {
                d.parseM3u8List()
            } else if (d.downType == 3) {
                d.m3u8Items = embedVariants(d, d.downUrl)
            } else if (d.downType == 5) {
                d.m3u8Items = dashTemplateVariants(d.appCtx, urlFromText(d.downUrl))
            } else if (d.downType == 7) {
                d.m3u8Items = DownParseDzen.parsePage(d.appCtx, d.downUrl)
            } else if (d.downType == 9) {
                d.m3u8Items = vimeoVariants(loadConfig(d.appCtx, d.downUrl, d.referer))
            }
            d.downItemList = ArrayList()
            val listener = d.urlListener
            if (listener == null) {
                return
            }
            val web = listener.currentWeb()
            val done: MutableList<String?>? = web?.downloaded
            val failed: MutableList<String?>? = web?.downFail
            val downType = d.downType
            if (downType == 1 || downType == 3 || downType == 5 || downType == 7 || downType == 9) {
                if (d.m3u8Items != null) {
                    for (index in d.m3u8Items!!.indices) {
                        val item = d.m3u8Items!!.get(index)
                        if (item == null) {
                            continue
                        }
                        val row = DownListItem(index, item.label, "MP4")
                        markState(row, joinUrl(d.downUrl, item.label), done, failed)
                        d.downItemList!!.add(row)
                    }
                }
            } else if (d.faceItems != null) {
                for (index in d.faceItems!!.indices) {
                    val face = d.faceItems!!.get(index) as FaceItem?
                    if (face == null) {
                        continue
                    }
                    val label = face.label
                    val streamed =
                        ("HD" == label || "SD" == label) && !TextUtils.isEmpty(face.url) && !face.url!!.startsWith(
                            "http"
                        )
                    val row = DownListItem(index, label, if (streamed) "HDSD" else "MP4")
                    row.isHidden = face.isHidden
                    markState(row, joinUrl(d.downUrl, label), done, failed)
                    d.downItemList!!.add(row)
                }
            }
            if (d.loadCover != null) {
                d.loadCover!!.post(d.PickItemTask())
            }
        }

        /** Marks a row as failed (2) or already downloaded (1).  */
        private fun markState(
            row: DownListItem?,
            key: String?,
            done: MutableList<String?>?,
            failed: MutableList<String?>?
        ) {
            if (failed != null && failed.contains(key)) {
                row!!.checkState = 2
            } else if (done != null && done.contains(key)) {
                row!!.checkState = 1
            }
        }

        /** Embedly (direct video src) and Reddit (DASH manifest) embeds. Marks YouTube embeds.  */
        private fun embedVariants(dialogDownUrl: DialogDownUrl?, url: String?): ArrayList<M3u8Item?>? {
            if (TextUtils.isEmpty(url)) {
                return null
            }
            if (url!!.startsWith("https://cdn.embedly.com/widgets/media.html")) {
                val src = embedlyVideoSrc(url)
                if (TextUtils.isEmpty(src)) {
                    return null
                }
                if (src == "youtube.com") {
                    dialogDownUrl!!.embedLoaded = true
                    return null
                }
                val list: ArrayList<M3u8Item?> = ArrayList()
                val item = M3u8Item()
                item.src = src
                list.add(item)
                return list
            }
            var manifest = url
            if (url.startsWith("https://www.redditmedia.com/mediaembed/")) {
                manifest = redditManifest(url)
                if (TextUtils.isEmpty(manifest)) {
                    return null
                }
                if (manifest == "youtube.com") {
                    dialogDownUrl!!.embedLoaded = true
                    return null
                }
            }
            return dashBaseUrlVariants(dialogDownUrl!!.appCtx, manifest)
        }

        /** First usable <video src> of the page embedded by an Embedly widget, or "youtube.com". </video> */
        private fun embedlyVideoSrc(widgetUrl: String?): String? {
            val query = urlDecode(widgetUrl)
            val at = if (TextUtils.isEmpty(query)) 0 else query!!.indexOf("&url=https")
            val start = if (TextUtils.isEmpty(query) || at == -1) 0 else (at + 10)
            val end = if (TextUtils.isEmpty(query) || at == -1 || start >= query!!.length) 0 else query.indexOf('&', start)
            if (TextUtils.isEmpty(query) || at == -1 || start >= query!!.length || end <= start) {
                return null
            }
            val pageUrl = query.substring(at + 5, end)
            if (TextUtils.isEmpty(pageUrl)) {
                return null
            }
            val page: Document
            try {
                page = Jsoup.connect(pageUrl).get()
            } catch (unused: Exception) {
                return null
            } catch (unused: LinkageError) {
                return null
            } catch (unused: OutOfMemoryError) {
                return null
            }
            var youtube = false
            try {
                var host: String? = null
                for (video in page.select("video")) {
                    var src = video.attr("src")
                    if (TextUtils.isEmpty(src) || src.startsWith("blob:")) {
                        continue
                    }
                    if (src.contains("youtube.com")) {
                        youtube = true
                        continue
                    }
                    if (!URLUtil.isNetworkUrl(src)) {
                        if (host == null) {
                            host = hostOf(pageUrl, true)
                        }
                        if (TextUtils.isEmpty(host)) {
                            continue
                        }
                        src = absoluteUrl(src, host) ?: continue
                        if (!URLUtil.isNetworkUrl(src)) {
                            continue
                        }
                    }
                    return src
                }
            } catch (unused: Exception) {
            }
            return if (youtube) "youtube.com" else null
        }

        /** DASH manifest url referenced anywhere in a Reddit media embed, or "youtube.com".  */
        private fun redditManifest(embedUrl: String?): String? {
            val page: Document
            try {
                page = Jsoup.connect(embedUrl!!).get()
            } catch (unused: Exception) {
                return null
            } catch (unused: LinkageError) {
                return null
            } catch (unused: OutOfMemoryError) {
                return null
            }
            var youtube = false
            try {
                for (element in page.getAllElements()) {
                    for (attribute in element.attributes()) {
                        val value = attribute.value
                        if (!URLUtil.isNetworkUrl(value)) {
                            continue
                        }
                        if (value.contains("DASHPlaylist.mpd")) {
                            return value
                        }
                        if (value.contains("youtube.com")) {
                            youtube = true
                        }
                    }
                }
            } catch (unused: Exception) {
            }
            return if (youtube) "youtube.com" else null
        }

        private fun loadXml(context: Context?, url: String?): Document? {
            try {
                return Jsoup.connect(url!!).parser(Parser.xmlParser()).get()
            } catch (unused: Exception) {
                return null
            } catch (unused: LinkageError) {
                return null
            } catch (unused: OutOfMemoryError) {
                MainUtil.restartLater(context)
                return null
            }
        }

        /** Video representations of a Reddit-style DASH manifest (BaseURL files) with the audio track.  */
        private fun dashBaseUrlVariants(context: Context?, manifest: String?): ArrayList<M3u8Item?>? {
            val at = if (TextUtils.isEmpty(manifest)) 0 else manifest!!.indexOf("DASHPlaylist.mpd")
            val slash = if (TextUtils.isEmpty(manifest) || at == -1) 0 else manifest!!.lastIndexOf('/', at)
            if (TextUtils.isEmpty(manifest) || at == -1 || slash == -1) {
                return null
            }
            val base = manifest!!.substring(0, slash + 1)
            val doc = loadXml(context, manifest)
            if (doc == null) {
                return null
            }
            try {
                val videos = doc.select("[contentType=video]")
                if (videos.isEmpty()) {
                    return null
                }
                val list: ArrayList<M3u8Item?> = ArrayList()
                for (set in videos) {
                    for (rep in set.children()) {
                        if (rep.children().isEmpty()) {
                            continue
                        }
                        val width = rep.attr("width")
                        val height = rep.attr("height")
                        if (TextUtils.isEmpty(width) || TextUtils.isEmpty(height)) {
                            continue
                        }
                        var file: String? = null
                        for (child in rep.children()) {
                            if ("BaseURL" == child.tagName() && !TextUtils.isEmpty(child.html())) {
                                file = child.html()
                                break
                            }
                        }
                        if (!TextUtils.isEmpty(file)) {
                            val item = M3u8Item()
                            item.src = base + file
                            item.label = width + "x" + height
                            item.size = parseIntSafe(width) * parseIntSafe(height)
                            list.add(item)
                        }
                    }
                }
                if (list.isEmpty()) {
                    // No sizes in the manifest: take them from file names like "DASH_720.mp4".
                    for (set in videos) {
                        for (baseUrl in set.getElementsByTag("BaseURL")) {
                            val file = baseUrl.html()
                            if (TextUtils.isEmpty(file)) {
                                continue
                            }
                            var label: String? = null
                            var size = 0
                            val under = file.indexOf('_')
                            val dot = file.lastIndexOf('.')
                            if (under > 0 && dot > under + 1) {
                                val part = file.substring(under + 1, dot)
                                val value = parseColorSafe(part)
                                if (value != -1) {
                                    label = part
                                    size = value
                                }
                            }
                            val item = M3u8Item()
                            item.src = base + file
                            item.label = label
                            item.size = size
                            list.add(item)
                        }
                    }
                }
                if (list.isEmpty()) {
                    return null
                }
                try {
                    list.sortWith(SortM3u8())
                } catch (unused: Exception) {
                }
                var audio: String? = null
                for (set in doc.select("[contentType=audio]")) {
                    for (baseUrl in set.getElementsByTag("BaseURL")) {
                        if (!TextUtils.isEmpty(baseUrl.html())) {
                            audio = baseUrl.html()
                            break
                        }
                    }
                    if (audio != null) {
                        break
                    }
                }
                if (!TextUtils.isEmpty(audio)) {
                    for (obj in list) {
                        (obj as M3u8Item).audioUrl = base + audio
                    }
                }
                return list
            } catch (unused: Exception) {
                return null
            }
        }

        /** Video representations of a SegmentTemplate DASH manifest (init segment and media template).  */
        private fun dashTemplateVariants(context: Context?, manifest: String?): ArrayList<M3u8Item?>? {
            if (TextUtils.isEmpty(manifest)) {
                return null
            }
            val doc = loadXml(context, manifest)
            if (doc == null) {
                return null
            }
            try {
                val protection = doc.getElementsByTag("ContentProtection")
                if (!protection.isEmpty()) {
                    return null // DRM
                }
                val videos = doc.select("[contentType=video]")
                if (videos.isEmpty()) {
                    return null
                }
                val list: ArrayList<M3u8Item?> = ArrayList()
                for (set in videos) {
                    for (rep in set.children()) {
                        if (rep.children().isEmpty()) {
                            continue
                        }
                        val width = rep.attr("width")
                        val height = rep.attr("height")
                        if (TextUtils.isEmpty(width) || TextUtils.isEmpty(height)) {
                            continue
                        }
                        val template = segmentTemplate(rep)
                        if (template == null) {
                            continue
                        }
                        val item = M3u8Item()
                        item.template = template[0]
                        item.src = template[1]
                        item.label = width + "x" + height
                        item.size = parseIntSafe(width) * parseIntSafe(height)
                        list.add(item)
                    }
                }
                if (list.isEmpty()) {
                    return null
                }
                try {
                    list.sortWith(SortM3u8())
                } catch (unused: Exception) {
                }
                var audio: Array<String?>? = null
                for (set in doc.select("[contentType=audio]")) {
                    for (rep in set.children()) {
                        val template = segmentTemplate(rep)
                        if (template != null) {
                            audio = template
                        }
                    }
                }
                if (audio != null) {
                    for (item2 in list) {
                        val item = item2 as M3u8Item
                        item.audioTemplate = audio[0]
                        item.audioUrl = audio[1]
                    }
                }
                return list
            } catch (unused: Exception) {
                return null
            }
        }

        /** {initialization, media} of the last SegmentTemplate child of `rep`, or null.  */
        private fun segmentTemplate(rep: Element?): Array<String?>? {
            var found: Array<String?>? = null
            for (child in rep!!.children()) {
                val init = child.attr("initialization")
                var media = child.attr("media")
                if (TextUtils.isEmpty(init) || TextUtils.isEmpty(media)) {
                    continue
                }
                media = media.replace("\$Number", "").replace("$", "")
                if (!TextUtils.isEmpty(media)) {
                    found = arrayOf<String?>(init, media)
                }
            }
            return found
        }

        /** Vimeo player config: one entry per video rendition (base url and id).  */
        private fun vimeoVariants(config: JsonObject?): ArrayList<M3u8Item?>? {
            if (config == null) {
                return null
            }
            try {
                val clip = config.get("clip_id")
                val baseUrl = config.get("base_url")
                if (clip == null || TextUtils.isEmpty(clip.getAsString()) || baseUrl == null) {
                    return null
                }
                val base = baseUrl.getAsString()
                val videos = config.get("video") as JsonArray?
                if (TextUtils.isEmpty(base) || !base!!.startsWith("../") || videos == null || videos.size() == 0) {
                    return null
                }
                val list: ArrayList<M3u8Item?> = ArrayList()
                for (index in 0 until videos.size()) {
                    try {
                        val video = videos.get(index) as JsonObject?
                        val id = if (video != null) video.get("id") else null
                        if (id == null || TextUtils.isEmpty(id.getAsString())) {
                            continue
                        }
                        val videoBase = video!!.get("base_url")
                        val width = video.get("width")
                        val height = video.get("height")
                        val init = video.get("init_segment")
                        val segments = video.get("segments") as JsonArray?
                        if (videoBase == null || TextUtils.isEmpty(videoBase.getAsString()) || width == null || width.getAsInt() <= 0 || height == null || height.getAsInt() <= 0 || init == null || TextUtils.isEmpty(
                                init.getAsString()
                            )
                            || segments == null || segments.size() == 0
                        ) {
                            continue
                        }
                        val item = M3u8Item()
                        item.base = base
                        item.src = id.getAsString()
                        item.label = width.getAsInt().toString() + "x" + height.getAsInt()
                        item.size = width.getAsInt() * height.getAsInt()
                        list.add(item)
                    } catch (unused: Exception) {
                    }
                }
                if (list.isEmpty()) {
                    return null
                }
                try {
                    list.sortWith(SortM3u8())
                } catch (unused: Exception) {
                }
                return list
            } catch (unused: Exception) {
                return null
            }
        }
    }

    internal inner class PickItemTask : Runnable {
        override fun run() {
            val arrayList: ArrayList<DownListItem?>?
            val dialogDownUrl = this@DialogDownUrl
            if (dialogDownUrl.listNode != null) {
                val downItemList = dialogDownUrl.downItemList
                if (downItemList != null && downItemList.size >= 2) {
                    dialogDownUrl.downListener = dialogDownUrl.M3u8ItemListener()
                    dialogDownUrl.downItems = downItemList.filterIsInstance<DownListItem>()
                    val handler = dialogDownUrl.mainHandler
                    if (handler != null) {
                        handler.post(dialogDownUrl.ReloadListTask())
                        return
                    }
                    return
                }
                val downType = dialogDownUrl.downType
                if ((downType != 3 && downType != 5 && downType != 7 && downType != 9) || ((dialogDownUrl.downItemList.also {
                        arrayList = it
                    }) != null && !arrayList!!.isEmpty())) {
                    dialogDownUrl.pickItem(0)
                    return
                }
                val myCoverView = dialogDownUrl.loadCover
                if (myCoverView == null) {
                    return
                }
                myCoverView.setVisibility(8)
                dialogDownUrl.setWaiting(false)
                val embedLoaded = dialogDownUrl.embedLoaded
                val appCompatTextView = dialogDownUrl.statusText
                if (embedLoaded) {
                    appCompatTextView!!.setText(AppR.string.video_down_guide_3)
                } else {
                    appCompatTextView!!.setText(AppR.string.no_down_video)
                }
                dialogDownUrl.statusText!!.setVisibility(0)
                dialogDownUrl.formNode!!.setVisibility(8)
            }
        }
    }

    internal inner class M3u8ItemListener : MainDownListener {

        internal inner class M3u8ItemStep : Runnable {
            override fun run() {
                val dialogDownUrl = this@DialogDownUrl
                val progressNow = dialogDownUrl.progressNow
                dialogDownUrl.downItemIn = null
                dialogDownUrl.m3u8In = null
                dialogDownUrl.faceIn = null
                val m3u8Url = dialogDownUrl.m3u8UrlAt(progressNow)
                val typedName = dialogDownUrl.nameField?.str()
                val downUrlListener = dialogDownUrl.urlListener
                if (downUrlListener != null) {
                    downUrlListener.preview(m3u8Url, typedName, dialogDownUrl.contentType, false)
                }
                dialogDownUrl.actionBusy = false
            }
        }

        override fun onDownload(position: Int) {
            this@DialogDownUrl.pickItem(position)
        }

        override fun onPlay(position: Int) {
            val dialogDownUrl = this@DialogDownUrl
            if (dialogDownUrl.urlListener == null || dialogDownUrl.actionBusy) {
                return
            }
            dialogDownUrl.actionBusy = true
            dialogDownUrl.progressNow = position
            val handler = dialogDownUrl.mainHandler
            if (handler == null) {
                return
            }
            handler.post(M3u8ItemStep())
        }

        override fun onInfo(position: Int) {
            val dialogDownUrl = this@DialogDownUrl
            val m3u8Url = dialogDownUrl.m3u8UrlAt(position)
            dialogDownUrl.pickedItem = null
            dialogDownUrl.pickedM3u8 = dialogDownUrl.m3u8In
            dialogDownUrl.pickedFace = dialogDownUrl.faceIn
            dialogDownUrl.m3u8In = null
            dialogDownUrl.faceIn = null
            dialogDownUrl.openLinkDialog(m3u8Url)
        }
    }

    internal inner class ReloadListTask : Runnable {
        override fun run() {
            this@DialogDownUrl.updateList(true)
        }
    }

    internal inner class PrepareUrlTask : Runnable {

        internal inner class UpdateListStep : Runnable {
            override fun run() {
                val dialogDownUrl = this@DialogDownUrl
                dialogDownUrl.updateList(false)
            }
        }

        /** Torrent support was removed: the url is only cleaned up and treated as a normal download. */
        override fun run() {
            val dialog = this@DialogDownUrl
            if (dialog.loadCover == null) {
                return
            }
            dialog.downType = 0
            dialog.downUrl = firstUrlIn(dialog.downUrl)
            dialog.loadCover!!.post(UpdateListStep())
        }
    }

    internal inner class PickFirstItemTask : Runnable {
        override fun run() {
            this@DialogDownUrl.pickFirst()
        }
    }

    internal inner class LoadIconTask : Runnable {

        internal inner class LoadIconStep : Runnable {
            override fun run() {
                val dialogDownUrl = this@DialogDownUrl
                val glideRequests = dialogDownUrl.glide
                if (glideRequests == null) {
                    return
                }
                val glideUrl = dialogDownUrl.previewGlideUrl
                val diskCacheStrategy = DiskCacheStrategy.NONE
                if (glideUrl != null) {
                    (glideRequests.load(glideUrl).diskCacheStrategy(diskCacheStrategy)).anyListener(
                        dialogDownUrl.iconListener
                    ).into(dialogDownUrl.previewImage!!)
                } else {
                    (glideRequests.load(dialogDownUrl.posterSrc)
                        .diskCacheStrategy(diskCacheStrategy)).anyListener(dialogDownUrl.iconListener)
                        .into(dialogDownUrl.previewImage!!)
                }
            }
        }

        override fun run() {
            val dialogDownUrl = this@DialogDownUrl
            val mainActivity = dialogDownUrl.hostActivity
            if (mainActivity != null) {
                if (dialogDownUrl.glide == null) {
                    dialogDownUrl.glide = GlideApp.with(mainActivity)
                }
                val handler = dialogDownUrl.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(LoadIconStep())
            }
        }
    }

    internal inner class ApplyIconLoadListener : RequestListener<Any?> {

        internal inner class ApplyIconStep : Runnable {
            override fun run() {
                val dialogDownUrl = this@DialogDownUrl
                dialogDownUrl.applyIcon(dialogDownUrl.iconColor)
            }
        }

        override fun onLoadFailed(glideException: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Any?>, isFirstResource: Boolean): Boolean {
            val dialogDownUrl = this@DialogDownUrl
            if (dialogDownUrl.previewGlideUrl != null && !dialogDownUrl.previewFailed && dialogDownUrl.previewImage != null) {
                dialogDownUrl.previewFailed = true
                val handler = dialogDownUrl.mainHandler
                if (handler != null) {
                    handler.post(ApplyIconStep())
                }
            }
            return true
        }

        override fun onResourceReady(resource: Any?, model: Any, target: com.bumptech.glide.request.target.Target<Any?>?, dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
            val drawable = resource as Drawable
            val dialogDownUrl = this@DialogDownUrl
            if (dialogDownUrl.glide == null || dialogDownUrl.iconImage == null) {
                return false
            }
            dialogDownUrl.applyPreviewSize(drawable)
            if (dialogDownUrl.thumbImage != null) {
                dialogDownUrl.iconImage!!.setVisibility(View.GONE)
                dialogDownUrl.nameText!!.setVisibility(8)
                dialogDownUrl.thumbImage!!.setVisibility(View.VISIBLE)
            }
            return false
        }
    }

    internal inner class LoadPreviewTask : Runnable {

        internal inner class LoadPreviewStep : Runnable {
            override fun run() {
                val dialogDownUrl = this@DialogDownUrl
                val glideRequests = dialogDownUrl.glide
                if (glideRequests == null) {
                    return
                }
                if (dialogDownUrl.previewGlideUrl != null) {
                    ((glideRequests.`as`(PictureDrawable::class.java)).load(
                        dialogDownUrl.previewGlideUrl
                    )).anyListener(dialogDownUrl.previewListener).into(dialogDownUrl.previewImage!!)
                } else {
                    ((glideRequests.`as`(PictureDrawable::class.java)).load(
                        dialogDownUrl.posterSrc
                    )).anyListener(dialogDownUrl.previewListener).into(dialogDownUrl.previewImage!!)
                }
            }
        }

        override fun run() {
            val dialogDownUrl = this@DialogDownUrl
            val mainActivity = dialogDownUrl.hostActivity
            if (mainActivity != null) {
                if (dialogDownUrl.glide == null) {
                    dialogDownUrl.glide = GlideApp.with(mainActivity)
                }
                val handler = dialogDownUrl.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(LoadPreviewStep())
            }
        }
    }

    internal inner class LoadPreviewLoadListener : RequestListener<Any?> {

        internal inner class LoadPreviewAgainStep : Runnable {
            override fun run() {
                val dialogDownUrl = this@DialogDownUrl
                dialogDownUrl.loadPreview()
            }
        }

        override fun onLoadFailed(glideException: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Any?>, isFirstResource: Boolean): Boolean {
            val dialogDownUrl = this@DialogDownUrl
            if (dialogDownUrl.previewGlideUrl != null && !dialogDownUrl.previewFailed && dialogDownUrl.previewImage != null) {
                dialogDownUrl.previewFailed = true
                val handler = dialogDownUrl.mainHandler
                if (handler != null) {
                    handler.post(LoadPreviewAgainStep())
                }
            }
            return true
        }

        override fun onResourceReady(resource: Any?, model: Any, target: com.bumptech.glide.request.target.Target<Any?>?, dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
            val pictureDrawable = resource as PictureDrawable
            val dialogDownUrl = this@DialogDownUrl
            if (dialogDownUrl.glide == null || dialogDownUrl.iconImage == null) {
                return false
            }
            dialogDownUrl.applyPreviewSize(pictureDrawable)
            if (dialogDownUrl.thumbImage != null) {
                dialogDownUrl.iconImage!!.setVisibility(View.GONE)
                dialogDownUrl.nameText!!.setVisibility(8)
                dialogDownUrl.thumbImage!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                dialogDownUrl.thumbImage!!.setVisibility(View.VISIBLE)
            }
            return false
        }
    }

    internal inner class SetFileNameTask : Runnable {

        internal inner class ClearFileNameStep : Runnable {
            override fun run() {
                val dialogDownUrl = this@DialogDownUrl
                dialogDownUrl.setFileName(null)
            }
        }

        override fun run() {
            val dialogDownUrl = this@DialogDownUrl
            if (dialogDownUrl.appCtx != null) {
                val volume = MainUri.storageVolume()
                PrefSet.putDownUri(dialogDownUrl.appCtx, volume)
                dialogDownUrl.savedPath = MainUri.displayPath(dialogDownUrl.appCtx!!, volume)
                val handler = dialogDownUrl.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(ClearFileNameStep())
            }
        }
    }

    internal inner class CheckFileNameClick : View.OnClickListener {

        internal inner class CheckFileNameStep : Runnable {
            override fun run() {
                val dialogDownUrl = this@DialogDownUrl
                dialogDownUrl.checkFileName(true)
                dialogDownUrl.actionBusy = false
            }
        }

        override fun onClick(view: View?) {
            val dialogDownUrl = this@DialogDownUrl
            val myLineText = dialogDownUrl.msgText
            if (myLineText == null || dialogDownUrl.actionBusy) {
                return
            }
            dialogDownUrl.actionBusy = true
            myLineText.post(CheckFileNameStep())
        }
    }

    internal inner class DownloadTask : Runnable {

        internal inner class StartDownloadStep : Runnable {
            override fun run() {
                var nameEdited: Boolean = false
                val dialogDownUrl = this@DialogDownUrl
                val uriItem = dialogDownUrl.targetUri
                dialogDownUrl.targetUri = null
                if (uriItem != null) {
                    val nameEdited = if (dialogDownUrl.checkName) dialogDownUrl.nameEdited else false
                    if (dialogDownUrl.checkName && !nameEdited) {
                        val downUrlListener = dialogDownUrl.urlListener
                        if (downUrlListener != null) {
                            downUrlListener.onDownload(
                                dialogDownUrl.refUrl,
                                uriItem,
                                dialogDownUrl.mediaState.toInt(),
                                nameEdited,
                                dialogDownUrl.m3u8Url,
                                dialogDownUrl.referer
                            )
                            return
                        }
                        return
                    }
                    val fileSize = dialogDownUrl.fileSize
                    if (fileSize > 0) {
                        uriItem.size = fileSize
                    }
                    var downUrl = dialogDownUrl.downUrl
                    if (!TextUtils.isEmpty(dialogDownUrl.posterUrl)) {
                        downUrl = dialogDownUrl.downUrl
                        val posterUrl = dialogDownUrl.posterUrl
                        if (!TextUtils.isEmpty(posterUrl) && posterUrl!!.startsWith("https://")) {
                            downUrl = "mix:" + posterUrl.substring(6) + "<,>" + downUrl
                        }
                    }
                    val downUrlNow = downUrl
                    val downUrlListener = dialogDownUrl.urlListener
                    if (downUrlListener != null) {
                        downUrlListener.onDownload(
                            downUrlNow,
                            uriItem,
                            dialogDownUrl.mediaState.toInt(),
                            dialogDownUrl.nameEdited,
                            dialogDownUrl.m3u8Url,
                            dialogDownUrl.referer
                        )
                    }
                }
            }
        }

        override fun run() {
            val dialogDownUrl = this@DialogDownUrl
            var name = dialogDownUrl.pendingName
            dialogDownUrl.pendingName = null
            if (dialogDownUrl.urlListener != null) {
                if (dialogDownUrl.checkName && !dialogDownUrl.nameEdited && !TextUtils.isEmpty(name)) {
                    name = "$name.torrent"
                }
                val uriItemC = MainUri.createUri(dialogDownUrl.appCtx, MainUri.storageVolume(), null, name!!)
                if (uriItemC != null) {
                    dialogDownUrl.targetUri = uriItemC
                    val myEditText = dialogDownUrl.nameField
                    if (myEditText == null) {
                        return
                    }
                    myEditText.post(StartDownloadStep())
                }
            }
        }
    }

    internal inner class DownSizeChanged : DownSizeListener {
        override fun onSize(size: Long) {
            val dialogDownUrl = this@DialogDownUrl
            dialogDownUrl.fileSize = size
            dialogDownUrl.updateSize()
        }
    }

    internal inner class DownSizeDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogDownUrl = this@DialogDownUrl
            val dialogDownSize = dialogDownUrl.sizeDialog
            if (dialogDownSize != null) {
                dialogDownSize.dismiss()
                dialogDownUrl.sizeDialog = null
            }
        }
    }

    internal inner class UpdateSizeStep : PreviewListener {

        internal inner class OpenWithUrlStep : Runnable {
            override fun run() {
                val dialogDownUrl = this@DialogDownUrl
                val pendingText = dialogDownUrl.pendingText
                dialogDownUrl.pendingText = null
                if (dialogDownUrl.urlListener == null) {
                    return
                }
                dialogDownUrl.urlListener!!.openWith(pendingText, dialogDownUrl.nameField?.str(), dialogDownUrl.contentType)
                dialogDownUrl.actionBusy = false
            }
        }

        internal inner class OpenAppUrlStep : Runnable {
            override fun run() {
                val dialogDownUrl = this@DialogDownUrl
                val pendingText = dialogDownUrl.pendingText
                dialogDownUrl.pendingText = null
                if (dialogDownUrl.urlListener == null) {
                    return
                }
                dialogDownUrl.urlListener!!.openApp(pendingText, dialogDownUrl.nameField?.str())
                dialogDownUrl.actionBusy = false
            }
        }

        override fun copyUrl(url: String?) {
        }

        override fun openFile(url: String?, size: Long, isVideo: Boolean) {
            val dialogDownUrl = this@DialogDownUrl
            val downListItem = dialogDownUrl.pickedItem
            if (downListItem != null) {
                downListItem.fileSize = size
                return
            }
            val m3u8Item = dialogDownUrl.pickedM3u8
            if (m3u8Item != null) {
                m3u8Item.fileSize = size
                return
            }
            val faceItem = dialogDownUrl.pickedFace
            if (faceItem != null) {
                faceItem.fileSize = size
            } else {
                dialogDownUrl.fileSize = size
                dialogDownUrl.updateSize()
            }
        }

        override fun download(url: String?, mimeType: String?) {
            val dialogDownUrl = this@DialogDownUrl
            if (dialogDownUrl.urlListener != null) {
                dialogDownUrl.dismissLinkDialog()
                if (dialogDownUrl.actionBusy) {
                    return
                }
                dialogDownUrl.actionBusy = true
                dialogDownUrl.pendingText = url
                val handler = dialogDownUrl.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(OpenWithUrlStep())
            }
        }

        override fun share(url: String?) {
            val dialogDownUrl = this@DialogDownUrl
            if (dialogDownUrl.urlListener != null) {
                dialogDownUrl.dismissLinkDialog()
                if (dialogDownUrl.actionBusy) {
                    return
                }
                dialogDownUrl.actionBusy = true
                dialogDownUrl.pendingText = url
                val handler = dialogDownUrl.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(OpenAppUrlStep())
            }
        }

        override fun openWith(url: String?, mimeType: String?) {
        }
    }

    internal inner class DismissLinkDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogDownUrl = this@DialogDownUrl
            dialogDownUrl.dismissLinkDialog()
            dialogDownUrl.pickedItem = null
            dialogDownUrl.pickedM3u8 = null
            dialogDownUrl.pickedFace = null
        }
    }

    internal inner class DismissNowStep : MyPopupListener {
        override fun onClose() {
            val dialogDownUrl = this@DialogDownUrl
            val myPopupMenu = dialogDownUrl.moreMenu
            if (myPopupMenu != null) {
                myPopupMenu.dismissNow()
                dialogDownUrl.moreMenu = null
            }
        }

        override fun onItem(view: View?, id: Int): Boolean {
            val dialogDownUrl = this@DialogDownUrl
            val arrayList = dialogDownUrl.nameList
            if (arrayList == null || id >= arrayList.size) {
                pickTreeFolder(dialogDownUrl.hostActivity, MainUri.storageVolume())
                return true
            }
            val picked = dialogDownUrl.nameList!!.get(id) as String?
            if (TextUtils.isEmpty(picked) || picked == PrefPath.picked) {
                return true
            }
            PrefPath.picked = picked
            dialogDownUrl.runInBackground(dialogDownUrl.SetFileNameTask())
            return true
        }
    }

    internal inner class SizeLabelClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogDownUrl = this@DialogDownUrl
            val dialogDownSize: DialogDownSize? = if (dialogDownUrl.hostActivity != null) dialogDownUrl.sizeDialog else null
            if (dialogDownUrl.hostActivity != null && dialogDownSize == null && dialogDownUrl.linkDialog == null) {
                val sizeDialog = DialogDownSize(
                    dialogDownUrl.hostActivity,
                    dialogDownUrl.downUrl,
                    dialogDownUrl.referer,
                    dialogDownUrl.fileSize,
                    dialogDownUrl.DownSizeChanged()
                )
                dialogDownUrl.sizeDialog = sizeDialog
                sizeDialog.setOnDismissListener(dialogDownUrl.DownSizeDialogDismiss())
            }
        }
    }

    interface DownPosterListener {
        fun posterUrl(url: String?): String?
    }

    interface DownUrlListener {
        fun currentWeb(): WebNestView?

        fun openApp(url: String?, name: String?)

        fun openWith(url: String?, name: String?, dirName: String?)

        fun onDownload(url: String?, uriItem: UriItem?, type: Int, isAudio: Boolean, name: String?, dirName: String?)

        fun openInApp(appCode: Int, url: String?)

        fun preview(url: String?, name: String?, dirName: String?, isImage: Boolean)

        fun share(url: String?, name: String?, dirName: String?)
    }

    fun releaseAll() {
        if (this.appCtx == null) {
            return
        }
        this.sizeDialog?.dismiss()
        this.sizeDialog = null
        dismissLinkDialog()
        this.moreMenu?.dismissNow()
        this.moreMenu = null
        cancelSnsLoad()
        val downParseList = this.parseList
        if (downParseList != null) {
            this.parseList = null
            downParseList.parsing = false
        }
        val glideRequests = this.glide
        if (glideRequests != null) {
            this.previewImage?.let { glideRequests.clear(it) }
            this.thumbImage?.let { glideRequests.clear(it) }
            this.glide = null
        }
        this.iconImage?.release()
        this.iconImage = null
        this.thumbImage?.release()
        this.thumbImage = null
        this.previewImage?.release()
        this.previewImage = null
        this.listNode = null
        downListener = null
        downItems = emptyList()
        this.headerImage = null
        this.hostActivity = null
        this.appCtx = null
        this.downUrl = null
        this.referer = null
        this.urlListener = null
        this.posterListener = null
        this.posterSrc = null
        this.baseName = null
        this.fileName = null
        this.pendingTitle = null
        this.refUrl = null
        this.contentType = null
        this.coverRoot = null
        this.webCoord = null
        this.sourcePage = null
        this.previewGlideUrl = null
        this.m3u8Url = null
        this.m3u8Items = null
        this.faceItems = null
        this.itemsIn = null
        this.sizeList = null
        this.downItemList = null
        this.nameList = null
        this.savedPath = null
        this.posterUrl = null
        super.dismiss()
    }

    fun downItemAt(index: Int): DownListItem? {
        val arrayList = this.downItemList
        if (arrayList == null || index < 0 || index >= arrayList.size) {
            return null
        }
        return this.downItemList!!.get(index)
    }

    fun faceAt(index: Int): FaceItem? {
        val arrayList = this.faceItems
        if (arrayList == null || index < 0 || index >= arrayList.size) {
            return null
        }
        return this.faceItems!!.get(index) as FaceItem?
    }

    fun m3u8UrlAt(index: Int): String? {
        val downUrl = this.downUrl
        val downType = this.downType
        if (downType == 1 || downType == 3 || downType == 5 || downType == 7 || downType == 9) {
            val arrayList = this.m3u8Items
            val m3u8Item =
                if (arrayList == null || index < 0 || index >= arrayList.size) null else this.m3u8Items!!.get(index)
            if (m3u8Item != null) {
                val url = if (this.downType == 3) m3u8Item.src else firstUrlIn(this.downUrl)
                this.m3u8In = m3u8Item
                return url
            }
        } else {
            val faceItemC = faceAt(index)
            if (faceItemC != null) {
                val url = faceItemC.url
                this.faceIn = faceItemC
                return url
            }
        }
        return downUrl
    }

    fun onFolderResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        if (requestCode != RequestCode.SAVE_LOCATION) {
            return false
        }
        if (resultCode == -1 && intent != null) {
            val data = intent.getData()
            if (data == null) {
                showToast(this.appCtx, AppR.string.invalid_path)
                return true
            }
            val treeUri = treeDocUri(data)
            if (TextUtils.isEmpty(treeUri)) {
                showToast(this.appCtx, AppR.string.invalid_path)
                return true
            }
            if (!TextUtils.isEmpty(treeUri) && treeUri != PrefPath.picked) {
                PrefPath.picked = treeUri
                runInBackground(SetFileNameTask())
            }
            MainUtil.keepUriPermission(this.appCtx, data)
        }
        return true
    }

    fun dismissLinkDialog() {
        val dialogDownLink = this.linkDialog
        if (dialogDownLink != null) {
            dialogDownLink.dismiss()
            this.linkDialog = null
        }
    }

    fun startSnsLoad(position: Int, snsType: Int, downUrl: String?) {
        val myLineFrame: Node? = if (this.snsLoad != null || this.webCoord == null) null else this.lineFrame
        if (this.snsLoad != null || this.webCoord == null || myLineFrame == null) {
            return
        }
        if (!AppState.nativeLoaded) {
            showLoadError()
            return
        }
        myLineFrame.setLineDn(true)
        this.lineNode!!.setVisibility(8)
        this.formNode!!.setVisibility(8)
        setProgress(30)
        setWaiting(true)
        this.statusText!!.setVisibility(0)
        this.snsLoad = WebSnsLoad(
            this.hostActivity,
            this.webCoord,
            this.sourcePage,
            downUrl,
            position,
            snsType,
            this.snsState.toInt(),
            SetWaitingStep()
        )
    }

    /** Header state while the keyboard is shown.  */
    fun updateLayout(smallLayout: Boolean) {
        var smallLayout = smallLayout
        if (this.rootNode == null || this.lineFrame == null) {
            return
        }
        if (smallLayout) {
            smallLayout = isSmallLayout()
        }
        this.smallLayout = smallLayout
        updateLine()
    }

    fun cancelSnsLoad() {
        val webSnsLoad = this.snsLoad
        if (webSnsLoad != null) {
            val webSnsTask = webSnsLoad.snsTask
            if (webSnsTask != null) {
                val loadTask = webSnsTask.loadTask
                if (loadTask != null) {
                    loadTask.cancelled = true
                }
                webSnsTask.loadTask = null
                webSnsTask.taskListener = null
                webSnsLoad.snsTask = null
            }
            val webSnsTwit = webSnsLoad.twitLoad
            if (webSnsTwit != null) {
                webSnsTwit.release()
                webSnsLoad.twitLoad = null
            }
            val webSnsInsta = webSnsLoad.instaLoad
            if (webSnsInsta != null) {
                webSnsInsta.release()
                webSnsLoad.instaLoad = null
            }
            val myWebSafe = webSnsLoad.webSafe
            if (myWebSafe != null) {
                if (webSnsLoad.pageLoading) {
                    webSnsLoad.pageLoading = false
                    myWebSafe.stopLoading()
                }
                destroyWebViewNow(webSnsLoad.webSafe)
                webSnsLoad.webSafe = null
            }
            webSnsLoad.hostActivity = null
            webSnsLoad.appContext = null
            webSnsLoad.loadListener = null
            webSnsLoad.parentView = null
            webSnsLoad.mediaUrl = null
            webSnsLoad.pageUrl = null
            webSnsLoad.jsPart1 = null
            webSnsLoad.jsPart2 = null
            webSnsLoad.webNest = null
            if (PdfSettings.debugMode.value) {
                WebView.setWebContentsDebuggingEnabled(true)
            }
            this.snsLoad = null
        }
    }

    fun checkFileName(nameEdited: Boolean) {
        var nameEdited = nameEdited
        val arrayList: ArrayList<DownListItem?>?
        val isCoomer: Boolean
        if (nameEdited) {
            if (this.checkName) {
                isCoomer = false
            } else {
                val host = MainUtil.hostOf(this.referer!!, true)
                if (TextUtils.isEmpty(host)) {
                    isCoomer = false
                } else {
                    isCoomer = host!!.endsWith("coomer.party")
                }
            }
            if (isCoomer) {
                nameEdited = false
            }
        }
        val listState = this.listState
        if (listState.toInt() != 0 && ((this.downItemList.also {
                arrayList = it
            }) == null || arrayList!!.isEmpty())) {
            val typedName = this.nameField?.str()
            if (TextUtils.isEmpty(typedName)) {
                showToast(this.appCtx, AppR.string.input_url)
                return
            }
            if (!URLUtil.isNetworkUrl(typedName)) {
                showToast(this.appCtx, AppR.string.invalid_url)
                return
            }
            if (listState.toInt() == 1) {
                startSnsLoad(0, 0, typedName!!)
                return
            } else if (listState.toInt() == 2) {
                startSnsLoad(0, 1, typedName!!)
                return
            } else {
                startSnsLoad(0, 2, typedName!!)
                return
            }
        }
        if (this.appCtx == null || this.nameField == null) {
            return
        }
        if (TextUtils.isEmpty(MainUri.storageVolume())) {
            showToast(this.appCtx, AppR.string.select_dir)
            return
        }
        val currentName = this.nameField?.str()
        if (TextUtils.isEmpty(currentName)) {
            showToast(this.appCtx, AppR.string.input_name)
            return
        }
        val bytes: ByteArray? = currentName!!.toByteArray()
        if (bytes != null && bytes.size > 200) {
            showToast(this.appCtx, AppR.string.long_name)
            return
        }
        val safeName = MainUtil.safeFileName(currentName)
        MainUri.storageVolume()
        closeKeyboard(this.appCtx, window?.decorView)
        this.pendingName = safeName
        this.targetUri = null
        this.nameEdited = nameEdited
        runInBackground(DownloadTask())
    }

    /**
     * Works out the download's type, mime type and default file name (runs off the UI thread), then
     * posts [BuildNodesTask] to build the dialog views.
     */
    fun applyInfo() {
        var name = this.headerName
        val disposition = this.dispositionHeader
        this.headerName = null
        this.dispositionHeader = null
        if (this.appCtx == null) {
            return
        }
        if (TextUtils.isEmpty(this.downUrl)) {
            this.downUrl = ""
        }
        if (TextUtils.isEmpty(this.contentType)) {
            this.contentType = if (this.apkType) "image/*" else MainUtil.mimeForExt(fileExtOf(this.downUrl, false))
        }
        if (TextUtils.isEmpty(name)) {
            name = guessDownloadName(this.downUrl, disposition, this.contentType, true)
            if (!TextUtils.isEmpty(name) && name!!.endsWith(".m3u8")) {
                name = name.substring(0, name.length - 4) + "mp4"
            }
        }
        if ("application/pdf" == this.contentType && !TextUtils.isEmpty(name) && !name!!.endsWith(".pdf")) {
            val dot = name.lastIndexOf('.')
            if (dot == -1) {
                name = name + ".pdf"
            } else if (dot > 0) {
                name = name.substring(0, dot) + ".pdf"
            }
        }
        var icon = archiveKindOf(name)
        var type: Int
        if (icon == AppR.drawable.outline_image_black_24) {
            this.apkType = true
            this.contentType = "image/*"
            type = 4
        } else {
            type = this.mimeType
            if (icon == AppR.drawable.outline_draft_black_24) {
                icon = AppR.drawable.outline_public_black_24
            }
        }
        if (TextUtils.isEmpty(this.contentType)) {
            this.contentType = MainUtil.mimeForExt(lowerExtOf(name))
        }
        if (!this.apkType && this.downType != 13) {
            if ("application/pdf" == this.contentType) {
                this.iconFixed = true
            }
        }
        this.mediaState = 0
        if (this.downUrl!!.startsWith("blob:")) {
            this.apkType = false
            this.mediaState = 3
            this.downType = 14
        } else if (this.downUrl!!.startsWith("mix:")) {
            this.apkType = false
            this.downType = 15
        } else if (this.apkType) {
            this.mediaState = (if (MainUtil.isPhnGif(this.downUrl!!)) 2 else 1).toByte()
        } else if (this.downType == 0) {
            this.downType = MainDownSvc.urlType(this.downUrl)
        }
        this.skipCheck = MainUtil.isGoogleUserContent(this.downUrl!!)
        // Whether the "open after download" style option applies to this file.
        var openable: Boolean
        if (this.downType != 0) {
            openable = this.downType != 13 && this.downType != 14
            val downType = this.downType
            if (type == 6 && !TextUtils.isEmpty(name) && name!!.endsWith(".aac")
                && (downType == 1 || downType == 2 || downType == 4 || downType == 6 || downType == 8 || downType == 10)
            ) {
                // Stream parsers deliver muxed video: save as mp4 instead of the audio track name.
                val dot = name.lastIndexOf('.')
                if (dot > 0 && dot < name.length) {
                    name = name.substring(0, dot) + ".mp4"
                    this.contentType = "video/*"
                    icon = AppR.drawable.baseline_play_arrow_black_24
                    type = 5
                }
            }
        } else if (type == 4 || type == 5 || type == 6) {
            openable = true
        } else if (this.iconFixed) {
            this.sizeLoaded = true
            openable = true
        } else {
            openable = false
            if (!TextUtils.isEmpty(name)) {
                for (index in 0..9) {
                    if (name!!.endsWith(Subtitle.DOT_EXTENSIONS[index])) {
                        openable = true
                        break
                    }
                }
            }
        }
        this.openMode = type
        if (!TextUtils.isEmpty(name)) {
            val dot = name!!.lastIndexOf('.')
            var max = 190
            var ext: String? = null
            if (dot != -1) {
                ext = name.substring(dot)
                max = 190 - ext.length
                name = name.substring(0, dot)
            }
            name = MainUtil.safeTitleFileName(max, name, "Download")
            if (ext != null) {
                name = name + ext
            }
        }
        this.nameList = MainUri.externalVolumes(this.appCtx!!)
        PrefPath.picked = MainUri.findChild(this.appCtx, PrefPath.picked!!, this.nameList)
        this.savedPath = MainUri.displayPath(this.appCtx!!, MainUri.storageVolume())
        this.pendingMessage = name
        this.iconRes = icon
        this.hasDownloadedCopy = this.hasDownloaded
        this.canOpen = openable
        if (this.mainHandler != null) {
            this.mainHandler.post(BuildNodesTask())
        }
    }

    fun showLoadError() {
        val myCoverView = this.loadCover
        if (myCoverView == null) {
            return
        }
        myCoverView.setVisibility(8)
        setWaiting(false)
        this.statusText!!.setVisibility(0)
        this.formNode!!.setVisibility(0)
        val arrayList = this.faceItems
        if (arrayList == null || arrayList.isEmpty()) {
            val myLineText = this.msgText
            if (myLineText != null) {
                myLineText.setVisibility(8)
            }
            this.mainButton!!.setText(AppR.string.retry)
            return
        }
        val msgText = this.msgText
        if (msgText != null) {
            msgText.setVisibility(0)
            this.msgText!!.setText(AppR.string.retry)
        }
        this.mainButton!!.setText(AppR.string.list)
    }

    fun showItems(index: Int, snsType: Int, url: String?) {
        if (this.coverRoot == null || this.listNode == null) {
            return
        }
        if (!AppState.nativeLoaded) {
            showLoadError()
            return
        }
        this.bodyNode!!.setVisibility(0)
        this.listNode!!.setVisibility(8)
        val sb = StringBuilder()
        val nativeLoaded = AppState.nativeLoaded
        var referer: String? = null
        if (nativeLoaded && nativeLoaded) {
            if (TextUtils.isEmpty(MainNative.faceUrl)) {
                MainNative.faceUrl = faceUrl
            }
            referer = MainNative.faceUrl
        }
        startSnsLoad(index, snsType, sb.append(referer).append(url).toString())
    }

    fun applyIcon(iconColor: Int) {
        val myRoundImage = this.iconImage
        if (myRoundImage != null) {
            this.iconColor = iconColor
            myRoundImage.showIcon(-460552, iconColor)
            if (!this.iconTinted) {
                if (this.skipCheck || this.iconFixed || this.mediaState.toInt() == 3 || this.downType != 0) {
                    return
                }
                if (!this.apkType && iconColor != AppR.drawable.outline_public_black_24) {
                    return
                }
            }
            var referer: String? = null
            if (isSvgName(guessDownloadName(this.posterSrc, null, null, true))) {
                loadPreview()
                return
            }
            this.previewImage!!.setVisibility(View.INVISIBLE)
        tick++
            if (URLUtil.isNetworkUrl(this.posterSrc)) {
                val refererNow = this.referer
                if (this.previewFailed) {
                    val isApi = MainConst.isApi28
                } else {
                    referer = refererNow
                }
                this.previewGlideUrl = MainUtil.glideUrlFor(this.appCtx, this.posterSrc!!, referer)
            } else {
                this.previewGlideUrl = null
            }
            runInBackground(LoadIconTask())
        }
    }

    fun pickItem(index: Int) {
        val downType = this.downType
        if (downType == 1 || downType == 3 || downType == 5 || downType == 7 || downType == 9) {
            val arrayList = this.m3u8Items
            pickM3u8(if (arrayList == null || index < 0 || index >= arrayList.size) null else this.m3u8Items!!.get(index))
        } else {
            val faceItemC = faceAt(index)
            if (faceItemC != null) {
                val label = faceItemC.label
                if ("HD" == label) {
                    if (!TextUtils.isEmpty(faceItemC.url) && !faceItemC.url!!.startsWith("http")) {
                        showItems(index, 3, faceItemC.url)
                        return
                    }
                } else if ("SD" == label && !TextUtils.isEmpty(faceItemC.url) && !faceItemC.url!!.startsWith(
                        "http"
                    )
                ) {
                    showItems(index, 4, faceItemC.url)
                    return
                }
                this.m3u8Url = joinUrl(this.downUrl, label)
                this.downUrl = faceItemC.url
                this.posterUrl = faceItemC.posterUrl
                this.fileSize = faceItemC.fileSize
                setExtension(label)
            }
        }
        updateList(false)
    }

    fun updateList(refresh: Boolean) {
        val myRecyclerView: Node? = if (this.coverRoot == null) null else this.listNode
        if (this.coverRoot == null || myRecyclerView == null) {
            return
        }
        if (refresh) {
            val listState = this.listState
            if (listState.toInt() == 1 || listState.toInt() == 2) {
                myRecyclerView.color = if (AppState.darkUi) -16777216 else -460552
            }
            this.lineHidden = true
            updateLine()
            this.lineFrame!!.setLineDn(true)
            this.bodyToggle!!.setVisibility(8)
            this.lineNode!!.setVisibility(8)
            this.formNode!!.setVisibility(8)
            this.listNode!!.setVisibility(0)
        } else {
            if (this.appCode != 0) {
                if (this.urlListener != null) {
                    this.urlListener!!.openInApp(this.appCode, firstUrlIn(this.downUrl))
                    return
                }
                return
            }
            this.lineHidden = false
            updateLine()
            this.lineFrame!!.setLineDn(false)
            this.bodyToggle!!.setVisibility(0)
            this.lineNode!!.setVisibility(0)
            this.formNode!!.setVisibility(0)
            this.listNode!!.setVisibility(8)
            val myLineText = this.msgText
            if (myLineText != null) {
                myLineText.setVisibility(0)
                this.msgText!!.setText(AppR.string.fast_down)
            }
            this.mainButton!!.setText(AppR.string.download)
            updateSize()
            if (this.downType == 13) {
                if (!TextUtils.isEmpty(this.pendingTitle)) {
                    this.nameField!!.setText(this.pendingTitle)
                }
                if (!TextUtils.isEmpty(this.refUrl)) {
                    this.checkName = true
                    var msgText = this.msgText
                    if (msgText == null) {
                        msgText = this.fastButton
                        this.msgText = msgText
                    }
                    if (AppState.darkUi) {
                        msgText!!.setBackgroundResource(AppR.drawable.selector_normal_dark)
                        this.msgText!!.setTextColor(-328966)
                    } else {
                        msgText!!.setBackgroundResource(AppR.drawable.selector_normal)
                        this.msgText!!.setTextColor(-14784824)
                    }
                    this.msgText!!.setVisibility(0)
                    this.msgText!!.setText(AppR.string.download)
                    this.mainButton!!.setText(AppR.string.link)
                    this.msgText!!.setOnClickListener(CheckFileNameClick())
                }
            }
        }
        this.loadCover!!.setVisibility(8)
        val appCompatTextView = this.statusText
        if (appCompatTextView != null) {
            appCompatTextView.setVisibility(8)
        }
        val myButtonText = this.errorReport
        if (myButtonText != null) {
            myButtonText.setVisibility(8)
        }
    }

    fun setProgress(progressValue: Int) {
        var progressValue = progressValue
        if (this.loadCover == null || this.coverRoot == null) {
            return
        }
        if (progressValue < 0) {
            progressValue = 0
        }
        this.progressValue = progressValue
        if (progressValue != 0) {
            this.loadCover!!.setCount(if (AppState.darkUi) -328966 else -16777216, MainApp.dp48, progressValue.toString())
            this.loadCover!!.setVisibility(0)
            this.coverRoot!!.postDelayed(SetProgressTask(), 1400L)
            return
        }
        val webSnsLoad = this.snsLoad
        val myWebSafe: MyWebSafe? = if (webSnsLoad != null) webSnsLoad.webSafe else null
        if (webSnsLoad != null && myWebSafe != null) {
            webSnsLoad.loading = false
            webSnsLoad.retryPosted = false
            webSnsLoad.retryCount = 0
            webSnsLoad.pageLoading = false
            myWebSafe.stopLoading()
        }
        showLoadError()
    }

    fun pickM3u8(m3u8Item: M3u8Item?) {
        val audioUrl: String?
        if (m3u8Item == null) {
            return
        }
        this.m3u8Url = joinUrl(this.downUrl, m3u8Item.label)
        val downType = this.downType
        var downUrl: String? = null
        if (downType == 1) {
            val downUrlNow = this.downUrl
            val src = m3u8Item.src
            val audioUrlNow = m3u8Item.audioUrl
            if (!TextUtils.isEmpty(src)) {
                val m3u8PageUrl = urlFromText(downUrlNow)
                if (!TextUtils.isEmpty(m3u8PageUrl)) {
                    audioUrl = if (TextUtils.isEmpty(audioUrlNow)) "isNull" else audioUrlNow
                    val sbW = StringBuilder("m3u8:").append(m3u8PageUrl).append("<,>").append(src).append("<,>")
                    sbW.append(audioUrl)
                    downUrl = sbW.toString()
                }
            }
            this.downUrl = downUrl
        } else if (downType == 3) {
            val src = m3u8Item.src
            val audioUrlNow = m3u8Item.audioUrl
            if (!TextUtils.isEmpty(src)) {
                downUrl =
                    "reddit:$src<,>${if (TextUtils.isEmpty(audioUrlNow)) "isNull" else audioUrlNow}"
            }
            this.downUrl = downUrl
        } else if (downType == 5) {
            val downUrlNow = this.downUrl
            val template = m3u8Item.template
            val src = m3u8Item.src
            var audioTemplate = m3u8Item.audioTemplate
            val audioUrlNow = m3u8Item.audioUrl
            if (!TextUtils.isEmpty(template) && !TextUtils.isEmpty(src)) {
                val kakaoPageUrl = urlFromText(downUrlNow)
                if (!TextUtils.isEmpty(kakaoPageUrl)) {
                    if (TextUtils.isEmpty(audioTemplate)) {
                        audioTemplate = "isNull"
                    }
                    audioUrl = if (TextUtils.isEmpty(audioUrlNow)) "isNull" else audioUrlNow
                    val sbW = StringBuilder("kakao2:").append(kakaoPageUrl).append("<,>").append(template).append("<,>")
                    sbW.append(src).append("<,>").append(audioTemplate).append("<,>")
                    sbW.append(audioUrl)
                    downUrl = sbW.toString()
                }
            }
            this.downUrl = downUrl
        } else if (downType == 7) {
            val url = m3u8Item.url
            val template = m3u8Item.template
            val src = m3u8Item.src
            var audioTemplate = m3u8Item.audioTemplate
            val audioUrlNow = m3u8Item.audioUrl
            if (!TextUtils.isEmpty(template) && !TextUtils.isEmpty(src)) {
                val dzenPageUrl = urlFromText(url)
                if (!TextUtils.isEmpty(dzenPageUrl)) {
                    if (TextUtils.isEmpty(audioTemplate)) {
                        audioTemplate = "isNull"
                    }
                    audioUrl = if (TextUtils.isEmpty(audioUrlNow)) "isNull" else audioUrlNow
                    val sbW = StringBuilder("dzen2:").append(dzenPageUrl).append("<,>").append(template).append("<,>")
                    sbW.append(src).append("<,>").append(audioTemplate).append("<,>")
                    sbW.append(audioUrl)
                    downUrl = sbW.toString()
                }
            }
            this.downUrl = downUrl
        } else if (downType == 9) {
            val downUrlNow = this.downUrl
            val base = m3u8Item.base
            val src = m3u8Item.src
            if (!TextUtils.isEmpty(base) && !TextUtils.isEmpty(src)) {
                val vimeoPageUrl = urlFromText(downUrlNow)
                if (!TextUtils.isEmpty(vimeoPageUrl)) {
                    val sbW = StringBuilder("vimeo2:").append(vimeoPageUrl).append("<,>").append(base).append("<,>")
                    sbW.append(src)
                    downUrl = sbW.toString()
                }
            }
            this.downUrl = downUrl
        }
        this.fileSize = m3u8Item.fileSize
        setExtension(m3u8Item.label)
    }

    fun parseM3u8List() {
        val downParseList = DownParseList()
        this.parseList = downParseList
        try {
            this.m3u8Items = downParseList.parseUrl(this.appCtx, this.downUrl, this.referer)
            if (downParseList.cancelled) {
                this.downUrl = urlFromText(this.downUrl)
            }
            if (downParseList.masterFound) {
                val isApi = MainConst.isApi28
                this.referer = null
            }
            downParseList.parsing = false
        } catch (unused: Exception) {
        }
        this.parseList = null
    }

    fun setExtension(title: String?) {
        if (TextUtils.isEmpty(this.fileName)) {
            setFileName(title + ".mp4")
            return
        }
        val dotIndex = this.fileName!!.lastIndexOf(46.toChar())
        if (dotIndex <= 0 || dotIndex >= this.fileName!!.length) {
            return
        }
        setFileName(this.fileName!!.substring(0, dotIndex) + "_" + title + this.fileName!!.substring(dotIndex))
    }

    fun setFileName(baseName: String?) {
        if (this.nameField != null) {
            if (!TextUtils.isEmpty(baseName)) {
                this.baseName = baseName
            }
            val safeName = MainUtil.safeFileName(if (this.nameTyped) this.nameField?.str() else this.baseName)
            if (TextUtils.isEmpty(MainUri.storageVolume())) {
                this.fileName = safeName
                this.nameField!!.setText(safeName)
                this.pathNode!!.setText(AppR.string.folder_not_selected)
                this.pathNode!!.setTextColor(-769226)
                this.lineNode!!.setDrawLine(true)
                this.existsText!!.setVisibility(8)
                return
            }
            this.pathNode!!.setText(this.savedPath)
            this.pathNode!!.setTextColor(if (AppState.darkUi) -328966 else -16777216)
            if (TextUtils.isEmpty(safeName)) {
                this.fileName = safeName
                this.nameField!!.setText(safeName)
                this.lineNode!!.setDrawLine(true)
                this.existsText!!.setVisibility(8)
                return
            }
            MainUri.storageVolume()
            this.lineNode!!.setDrawLine(true)
            this.existsText!!.setVisibility(8)
            this.fileName = safeName
            this.nameField!!.setText(safeName)
        }
    }

    fun showSnsWaiting() {
        if (this.statusText == null || this.snsLoad == null) {
            return
        }
        setWaiting(true)
        this.statusText!!.setVisibility(0)
        this.formNode!!.setVisibility(8)
        val myLineText = this.msgText
        if (myLineText != null) {
            myLineText.setVisibility(0)
        }
        setProgress(30)
        val webSnsLoad = this.snsLoad
        val myWebSafe = webSnsLoad!!.webSafe
        if (myWebSafe != null) {
            webSnsLoad.loading = true
            webSnsLoad.retryPosted = false
            webSnsLoad.retryCount = 0
            myWebSafe.loadUrl(webSnsLoad.pageUrl!!)
        }
    }

    fun openExternal() {
        if (this.appCode != 0) {
            if (this.urlListener != null) {
                this.urlListener!!.openInApp(this.appCode, firstUrlIn(this.downUrl))
                return
            }
            return
        }
        if (TextUtils.isEmpty(AlbumSettings.dappPkg.value) || TextUtils.isEmpty(AlbumSettings.dappCls.value)) {
            updateList(false)
        } else {
            if (this.urlListener == null) {
                return
            }
            this.urlListener!!.share(firstUrlIn(this.downUrl), this.nameField?.str(), this.contentType)
        }
    }

    fun pickFirst() {
        val pickIndex = this.pickIndex
        if (this.listNode != null) {
            val downListItemB = downItemAt(0)
            if (downListItemB == null) {
                showLoadError()
                return
            }
            val faceItemC = faceAt(pickIndex)
            if (faceItemC != null) {
                faceItemC.url = downListItemB.url
            }
            cancelSnsLoad()
            this.downItemOut = downListItemB
            this.faceOut = faceItemC
            val handler = this.mainHandler
            if (handler == null) {
                return
            }
            handler.post(ShowLoadErrorTask())
        }
    }

    fun loadPreview() {
        this.previewImage!!.setVisibility(View.INVISIBLE)
        tick++
        var referer: String? = null
        if (URLUtil.isNetworkUrl(this.posterSrc)) {
            val refererNow = this.referer
            if (this.previewFailed) {
                val isApi = MainConst.isApi28
            } else {
                referer = refererNow
            }
            this.previewGlideUrl = MainUtil.glideUrlFor(this.appCtx, this.posterSrc!!, referer)
        } else {
            this.previewGlideUrl = null
        }
        runInBackground(LoadPreviewTask())
    }

    fun openLinkDialog(downUrl: String?) {
        val fileSize: Long
        if (this.hostActivity != null && this.sizeDialog == null && this.linkDialog == null) {
            dismissLinkDialog()
            val downListItem = this.pickedItem
            if (downListItem != null) {
                fileSize = downListItem.fileSize
            } else {
                val m3u8Item = this.pickedM3u8
                if (m3u8Item != null) {
                    fileSize = m3u8Item.fileSize
                } else {
                    val faceItem = this.pickedFace
                    fileSize = if (faceItem != null) faceItem.fileSize else this.fileSize
                }
            }
            val dialogDownLink =
                DialogDownLink(this.hostActivity, downUrl, this.referer, fileSize, UpdateSizeStep())
            this.linkDialog = dialogDownLink
            dialogDownLink.setOnDismissListener(DismissLinkDialogDismiss())
        }
    }

    fun updateLine() {
        val myLineFrame = this.lineFrame
        if (myLineFrame == null) {
            return
        }
        if (this.smallLayout || this.lineHidden) {
            myLineFrame.setVisibility(8)
        } else {
            myLineFrame.setVisibility(0)
        }
    }

    /** Moves the loaded picture into the header (it is shown instead of the icon and name). */
    fun applyPreviewSize(drawable: Drawable?) {
        if (this.previewImage == null || this.thumbImage != null || this.lineFrame == null) {
            return
        }
        try {
            val intrinsicWidth = drawable!!.getIntrinsicWidth()
            val intrinsicHeight = drawable.getIntrinsicHeight()
            var width = if (intrinsicWidth <= 0 || intrinsicHeight <= 0) 0 else (this.lineFrame!!.getWidth() - (MainApp.dp16 shl 1))
            if (intrinsicWidth <= 0 || intrinsicHeight <= 0 || width <= 0) {
                return
            }
            val aspect = (intrinsicWidth / intrinsicHeight).toFloat()
            val previewHeight = dpToPx(this.appCtx, 140.0f).toInt()
            val previewWidth = Math.round(previewHeight * aspect)
            if (previewWidth <= width) {
                width = previewWidth
            }
            val myRoundImage = this.previewImage
            this.previewImage = null
            detachFromParent(myRoundImage)
            this.thumbImage = myRoundImage
            this.lineFrame!!.addView(myRoundImage, FrameLayout.LayoutParams(width, previewHeight))
        } catch (unused: Exception) {
        }
    }

    /** Loading cover text: "waiting" while loading, "no video" (taller) after. */
    fun setWaiting(waiting: Boolean) {
        if (this.coverRoot != null) {
            if (this.statusText == null) {
                this.statusText = this.waitingText
            }
            if (waiting) {
                this.statusText!!.setText(AppR.string.waiting)
            } else {
                this.statusText!!.setText(AppR.string.no_down_video)
            }
            this.errorReport?.setVisibility(8)
        }
    }

    /** Size line under the name: the known size, or the size button when there is none. */
    fun updateSize() {
        val size = this.sizeText ?: return
        val fileSize = this.fileSize
        if (fileSize > 0) {
            size.setText(MainUtil.sizeLabel(fileSize))
            size.setVisibility(0)
            this.sizeLabel!!.setVisibility(8)
        } else {
            size.setVisibility(8)
            this.sizeLabel!!.setVisibility(0)
        }
    }

    override fun dismiss() {
        if (this.dismissed) {
            return
        }
        this.dismissed = true
        releaseAll()
    }

    companion object {
        @JvmStatic
        fun m3u8Base(url: String?): String? {
            val length = if (url == null) 0 else url.length
            if (url == null || length == 0) {
                return null
            }
            try {
                val m3u8Index = url.indexOf(".m3u8")
                val pathStart = if (m3u8Index <= 0) 0 else (m3u8Index + 5)
                val urlStart = if (m3u8Index <= 0 || pathStart > length) 0 else url.lastIndexOf("https://", m3u8Index - 1)
                val quoteIndex = if (m3u8Index <= 0 || pathStart > length || urlStart < 0 || urlStart >= m3u8Index) 0 else url.indexOf(39.toChar(), pathStart)
                if (m3u8Index <= 0 || pathStart > length || urlStart < 0 || urlStart >= m3u8Index || quoteIndex < pathStart || quoteIndex >= length) {
                    return null
                }
                return url.substring(urlStart, quoteIndex)
            } catch (unused: Exception) {
                return null
            }
        }

        @JvmStatic
        fun joinUrl(downUrl: String?, label: String?): String {
            return "$downUrl$label"
        }
    }
}
