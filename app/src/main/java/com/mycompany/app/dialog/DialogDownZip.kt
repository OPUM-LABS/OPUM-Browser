package com.mycompany.app.dialog

import com.mycompany.app.view.anyListener
import com.mycompany.app.main.RequestCode
import com.mycompany.app.main.JarBridge
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.PathSettings
import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.os.Build
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.webkit.URLUtil
import android.widget.ImageView.ScaleType
import android.widget.TextView
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.compress.Compress.Companion.isSvgName
import com.mycompany.app.compress.CompressUtil.CompressListener
import com.mycompany.app.db.DbCmp.Companion.addItem
import com.mycompany.app.db.DbCmp.Companion.removePath
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.db.DbUtil.updateById
import com.mycompany.app.db.book.DbBookWeb.Companion.addItem
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.down.DownSaveZip.saveAll
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainDownSvc.DownZipListener
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.splitNumber
import com.mycompany.app.main.MainUri.displayPath
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.findChild
import com.mycompany.app.main.MainUri.externalVolumes
import com.mycompany.app.main.MainUri.isMediaUri
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.main.MainUtil.pickTreeFolder
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.editTextValue
import com.mycompany.app.main.MainUtil.fileExtOf
import com.mycompany.app.main.MainUtil.guessDownloadName
import com.mycompany.app.main.MainUtil.closeKeyboard
import com.mycompany.app.main.MainUtil.replaceExt
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.countLabel
import com.mycompany.app.main.MainUtil.safeTitleFileName
import com.mycompany.app.main.MainUtil.stripExtension
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.newCacheFilePath
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.safeFileName
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUtil.moveFileOrUri
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.main.MainUtil.keepUriPermission
import com.mycompany.app.main.MainUtil.zeroPadFormat
import com.mycompany.app.main.MainUtil.signOf
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.pref.PrefPath
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.pref.PrefSet.putDownUri
import com.mycompany.app.pref.PrefSet.remove
import com.mycompany.app.pref.PrefUtil.setDetail
import com.mycompany.app.pref.PrefUtil.setSingleLine
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.view.GlideApp
import com.mycompany.app.view.GlideRequest
import com.mycompany.app.view.GlideRequests
import com.mycompany.app.view.MyRoundImage
import com.mycompany.app.web.WebViewActivity
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.progress.ProgressMonitor
import java.io.File
import java.lang.ref.WeakReference
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Save the selected gallery images as one zip ([arrayList] URLs, page title [pageTitleParam], referer [referer]): header with the first
 * image and the title, the image count, the zip Name (title + ".zip") and save location; Create zip first downloads
 * ("Verify image") every image with Total n / m, a bar and the not-loaded count, then shows Total / Fail / Success
 * (Retry for the failed ones, or Create zip anyway), then zips them ("Create zip" progress) and shows the result; List
 * opens the archive list. "No image" when nothing could be loaded. The work (`DownSaveZip.saveAll`, [ZipTask] with zip4j,
 * `DbCmp`) and its state logic are the old code: its view fields are now small Compose-state stand-ins ([UiNode],
 * [UiBar]) so it runs unchanged. Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogDownZip(
    webViewActivity: WebViewActivity?,
    pageTitleParam: String?,
    arrayList: ArrayList<String?>?,
    referer: String?,
    dialogApplyListener: DialogApplyListener?,
) : ComposeBottomDialog(webViewActivity) {
    /** Stand-in for one old view: text (string or resource), colour, visibility, activated / enabled. */
    inner class UiNode(visible: Boolean = true) {
        var shown by mutableStateOf(visible)
        var textValue by mutableStateOf<String?>(null)
        var textRes by mutableIntStateOf(0)
        var color by mutableIntStateOf(0)
        var active by mutableStateOf(false)
        var enabledState by mutableStateOf(true)
        var cover by mutableStateOf(false)
        fun setVisibility(visibility: Int) { shown = visibility == 0 }
        fun getVisibility(): Int = if (shown) 0 else 8
        fun setText(retry: CharSequence?) { textValue = retry?.toString(); textRes = 0 }
        fun setText(res: Int) { textRes = res; textValue = null }
        fun setTextColor(newColor: Int) { color = newColor }
        fun setActivated(newActive: Boolean) { active = newActive }
        fun isActivated(): Boolean = active
        fun setEnabled(newEnabledState: Boolean) { enabledState = newEnabledState }
        fun setDrawLine(enabled: Boolean) {}
        fun setBackgroundColor(headColor: Int) {}
        fun scrollTo(groupSelected: Int) {}
        fun setCover(iconType: Int, iconSize: Int, newCover: Boolean, animate: Boolean) { cover = newCover }
        fun post(r: Runnable): Boolean = ui.post(r)
        @Composable
        fun str(): String = if (textRes != 0) stringResource(textRes) else textValue ?: ""
    }

    class UiBar {
        var max by mutableIntStateOf(1)
        var progress by mutableFloatStateOf(0f)
    }

    private val ui = android.os.Handler(android.os.Looper.getMainLooper())
    @JvmField val mainHandler: android.os.Handler? = ui

    @JvmField var zipTotalText: UiNode? = UiNode()
    @JvmField var zipFailText: UiNode? = UiNode()
    @JvmField var zipSuccessText: UiNode? = UiNode()
    /** Progress section (verify / create zip). */
    @JvmField var resultNode: UiNode? = UiNode(false)
    @JvmField var sectionNode: UiNode? = UiNode()
    @JvmField var totalText: UiNode? = UiNode()
    @JvmField var progressBar: UiBar? = UiBar()
    @JvmField var statusText: UiNode? = UiNode()
    /** Main button (activated = cancel / close). */
    @JvmField var messageText: UiNode? = UiNode()
    /** Second button (Create zip anyway / Retry). */
    @JvmField var retryButton: UiNode? = UiNode(false)
    @JvmField var baseName: String? = null
    @JvmField var zipName: String? = null
    @JvmField var tempUri: String? = null
    @JvmField var cleanName: String? = null
    @JvmField var nameEdited: Boolean = false
    @JvmField var pageTitle: String? = pageTitleParam
    @JvmField var suggestedName: String? = null
    @JvmField var imageUrls: ArrayList<String?>? = arrayList
    @JvmField var hostActivity: WebViewActivity? = webViewActivity
    @JvmField var checking: Boolean = false
    @JvmField var appCtx: Context? = context
    @JvmField var validImages: ArrayList<Any?>? = null
    @JvmField var applyListener: DialogApplyListener? = dialogApplyListener
    @JvmField var imageTotal: Int = 0
    @JvmField var referer: String? = referer
    @JvmField var imageDone: Int = 0
    /** Busy cover. */
    @JvmField var rootNode: UiNode? = UiNode()
    @JvmField var failCount: Int = 0
    @JvmField var formNode: UiNode? = UiNode()
    @JvmField var zipFailed: Boolean = false
    @JvmField var previewImage: MyRoundImage? = null
    @JvmField var runningZip: ZipTask? = null
    @JvmField var zipFiles: ArrayList<Any?>? = null
    /** Form (name, location). */
    @JvmField var formShown: UiNode? = UiNode()
    @JvmField var zipRemaining: Int = 0
    @JvmField var zipDone: Int = 0
    @JvmField var zipCount: Int = 0
    @JvmField var zipMonitor: ProgressMonitor? = null
    @JvmField var folderList: ArrayList<Any?>? = null
    @JvmField var folderText: String? = null
    @JvmField var cacheDir: String? = null
    @JvmField var lastImage: String? = null
    /** Progress / result area. */
    @JvmField var scrollNode: UiNode? = UiNode(false)
    @JvmField var creating: Boolean = false
    /** Verify result. */
    @JvmField var progressNode: UiNode? = UiNode(false)
    @JvmField var checkStopped: Boolean = false
    @JvmField var glide: GlideRequests? = null
    @JvmField var pendingPath: String? = null
    @JvmField var checkTotalText: UiNode? = UiNode()
    @JvmField var previewListener: RequestListener<Any?>? = null
    @JvmField var pendingName: String? = null
    @JvmField var checkFailText: UiNode? = UiNode()
    @JvmField var thumbListener: RequestListener<Any?>? = null
    @JvmField var zipListener: CompressListener? = null
    @JvmField var checkSuccessText: UiNode? = UiNode()
    /** "No image". */
    @JvmField var checkNode: UiNode? = UiNode(false)
    /** Zip result. */
    @JvmField var actionsNode: UiNode? = UiNode(false)

    private var name by mutableStateOf("")
    private var folderLabel by mutableStateOf<String?>(null)
    private var folderMenu by mutableStateOf(false)

    /** Background work. */
    private var executor: java.util.concurrent.ExecutorService? = null
    fun runInBackground(runnable: Runnable?) {
        var ex = executor
        if (ex == null || ex.isShutdown() || ex.isTerminated()) {
            ex = MainApp.executor(context) ?: return
            executor = ex
        }
        try { ex.execute(runnable) } catch (unused: Exception) { }
    }

    init {
        previewListener = ShowIconLoadListener()
        thumbListener = PreviewRequestListener()
        zipListener = SetTextStep()
        messageText!!.setText(AppR.string.create_zip)
        retryButton!!.setText(AppR.string.retry)
        runInBackground(Runnable {
            val ctx = appCtx ?: return@Runnable
            val suggested = MainUtil.safeTitleFileName(186, pageTitle, "Zip")
            val list = MainUri.externalVolumes(ctx)
            PrefPath.picked = MainUri.findChild(ctx, PrefPath.picked!!, list)
            val label = MainUri.displayPath(ctx, MainUri.storageVolume())
            ui.post {
                if (appCtx == null) return@post
                suggestedName = suggested
                folderList = list
                folderText = label
                applyName(suggestedName)
                build()
            }
        })
    }

    private fun build() {
        setSheet {
            Row(
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AndroidView(
                    factory = { ctx ->
                        MyRoundImage(ctx).also {
                            it.setScaleType(ScaleType.CENTER_CROP)
                            it.setCircleRadius(MainApp.dp40 / 2.0f)
                            previewImage = it
                            ui.post { ShowIconTask().run() }
                        }
                    },
                    modifier = Modifier.size(40.dp),
                )
                Text(
                    pageTitle ?: "", maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                if (formShown!!.shown) Form()
                if (scrollNode!!.shown) {
                    if (resultNode!!.shown) {
                        Section(sectionNode!!.str())
                        Line(stringResource(AppR.string.total), totalText!!.str())
                        val bar = progressBar!!
                        LinearProgressIndicator(
                            progress = { (bar.progress / bar.max.coerceAtLeast(1)).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                        )
                        Line(stringResource(AppR.string.fail), statusText!!.str(), red = statusText!!.color == -769226)
                    }
                    if (progressNode!!.shown) {
                        Section(stringResource(AppR.string.verify_image))
                        Line(stringResource(AppR.string.total), checkTotalText!!.str())
                        Line(stringResource(AppR.string.fail), checkFailText!!.str(), red = checkFailText!!.str() != "0")
                        Line(stringResource(AppR.string.success), checkSuccessText!!.str())
                    }
                    if (checkNode!!.shown) {
                        Text(
                            stringResource(AppR.string.no_image), color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                        )
                    }
                    if (actionsNode!!.shown) {
                        Section(stringResource(AppR.string.create_zip))
                        Line(stringResource(AppR.string.total), zipTotalText!!.str())
                        Line(stringResource(AppR.string.fail), zipFailText!!.str(), red = zipFailText!!.str() != "0")
                        Line(stringResource(AppR.string.success), zipSuccessText!!.str())
                    }
                }
            }
            if (rootNode!!.cover) LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                if (retryButton!!.shown) {
                    TextButton(onClick = { CancelZipClick().onClick(null) }) { Text(retryButton!!.str()) }
                }
                TextButton(onClick = { CreateZipClick().onClick(null) }, enabled = messageText!!.enabledState) { Text(messageText!!.str()) }
            }
        }
        show()
    }

    @Composable
    private fun Form() {
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
            Text(stringResource(AppR.string.image), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text((imageUrls?.size ?: 0).toString(), style = MaterialTheme.typography.bodyLarge)
        }
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                if (!nameEdited && !MainUtil.sameObject(cleanName, it)) nameEdited = true
            },
            label = { Text(stringResource(AppR.string.name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        )
        Box {
            ListItem(
                headlineContent = { Text(stringResource(AppR.string.down_location)) },
                supportingContent = { Text(folderLabel ?: stringResource(AppR.string.folder_not_selected), maxLines = 2) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.clickable { openFolders() }.padding(horizontal = 8.dp),
            )
            com.mycompany.app.ui.components.BubbleMenu(expanded = folderMenu, onDismiss = { folderMenu = false }) {
                val ctx = appCtx
                folderList?.forEach { any ->
                    val path = any as String?
                    DropdownMenuItem(
                        text = { Text(if (ctx == null) "" else MainUri.volumeLabel(ctx, path)) },
                        onClick = { folderMenu = false; setFolder(path) },
                    )
                }
                DropdownMenuItem(
                    text = { Text(stringResource(AppR.string.direct_select)) },
                    onClick = { folderMenu = false; pickTreeFolder(hostActivity, MainUri.storageVolume()) },
                )
            }
        }
    }

    @Composable
    private fun Section(title: String) {
        Text(
            title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 4.dp),
        )
    }

    @Composable
    private fun Line(label: String, value: String, red: Boolean = false) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(
                value, style = MaterialTheme.typography.bodyLarge,
                color = if (red) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    private fun openFolders() {
        val list = folderList
        if (list == null || list.isEmpty()) {
            pickTreeFolder(hostActivity, MainUri.storageVolume())
            return
        }
        folderMenu = true
    }

    private fun setFolder(picked: String?) {
        if (TextUtils.isEmpty(picked) || picked == PrefPath.picked) return
        PrefPath.picked = picked
        runInBackground(ApplyNameTask())
    }

    internal inner class KeepSegmentDirTask(@JvmField val segmentDir: String?) : Runnable {
        override fun run() {
            MainUtil.noOp(this.segmentDir)
        }
    }

    internal inner class CheckPathTask : Runnable {

        internal inner class ApplyPathStep : Runnable {
            override fun run() {
                val dialogDownZip = this@DialogDownZip
                val pendingPath = dialogDownZip.pendingPath
                dialogDownZip.pendingPath = null
                if (dialogDownZip.glide == null) {
                    return
                }
                val isNetworkPath = URLUtil.isNetworkUrl(pendingPath)
                val diskCacheStrategy = DiskCacheStrategy.NONE
                if (isNetworkPath) {
                    (dialogDownZip.glide!!.load(MainUtil.glideUrlFor(dialogDownZip.appCtx, pendingPath!!, dialogDownZip.referer))
                        .diskCacheStrategy(diskCacheStrategy)).anyListener(dialogDownZip.previewListener)
                        .into(dialogDownZip.previewImage!!)
                } else {
                    (dialogDownZip.glide!!.load(pendingPath).diskCacheStrategy(diskCacheStrategy)).anyListener(
                        dialogDownZip.previewListener
                    ).into(dialogDownZip.previewImage!!)
                }
            }
        }
        override fun run() {
            val dialogDownZip = this@DialogDownZip
            val webViewActivity = dialogDownZip.hostActivity
            if (webViewActivity != null) {
                if (dialogDownZip.glide == null) {
                    dialogDownZip.glide = GlideApp.with(webViewActivity)
                }
                val handler = dialogDownZip.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(ApplyPathStep())
            }
        }
    }

    internal inner class ShowIconLoadListener : RequestListener<Any?> {
        override fun onLoadFailed(glideException: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Any?>, isFirstResource: Boolean): Boolean {
            val myRoundImage = this@DialogDownZip.previewImage
            if (myRoundImage == null) {
                return true
            }
            myRoundImage.showIcon(-460552, AppR.drawable.outline_image_black_24)
            return true
        }
        override fun onResourceReady(resource: Any?, model: Any, target: com.bumptech.glide.request.target.Target<Any?>?, dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
            return false
        }
    }

    internal inner class CheckNameTask : Runnable {

        internal inner class ApplyNameStep : Runnable {
            override fun run() {
                val dialogDownZip = this@DialogDownZip
                val pendingName = dialogDownZip.pendingName
                dialogDownZip.pendingName = null
                if (dialogDownZip.glide == null) {
                    return
                }
                if (!URLUtil.isNetworkUrl(pendingName)) {
                    ((dialogDownZip.glide!!.`as`(PictureDrawable::class.java)).load(pendingName)).anyListener(
                        dialogDownZip.thumbListener
                    ).into(dialogDownZip.previewImage!!)
                    return
                }
                ((dialogDownZip.glide!!.`as`(PictureDrawable::class.java)).load(
                    MainUtil.glideUrlFor(
                        dialogDownZip.appCtx,
                        pendingName!!,
                        dialogDownZip.referer
                    )
                )).anyListener(dialogDownZip.thumbListener).into(dialogDownZip.previewImage!!)
            }
        }
        override fun run() {
            val dialogDownZip = this@DialogDownZip
            val webViewActivity = dialogDownZip.hostActivity
            if (webViewActivity != null) {
                if (dialogDownZip.glide == null) {
                    dialogDownZip.glide = GlideApp.with(webViewActivity)
                }
                val handler = dialogDownZip.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(ApplyNameStep())
            }
        }
    }

    internal inner class PreviewRequestListener : RequestListener<Any?> {
        override fun onLoadFailed(glideException: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Any?>, isFirstResource: Boolean): Boolean {
            val dialogDownZip = this@DialogDownZip
            val myRoundImage = dialogDownZip.previewImage
            if (myRoundImage == null) {
                return true
            }
            myRoundImage.setLayerType(View.LAYER_TYPE_NONE, null)
            dialogDownZip.previewImage!!.showIcon(-460552, AppR.drawable.outline_image_black_24)
            return true
        }
        override fun onResourceReady(resource: Any?, model: Any, target: com.bumptech.glide.request.target.Target<Any?>?, dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
            val myRoundImage = this@DialogDownZip.previewImage
            if (myRoundImage != null) {
                myRoundImage.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            }
            return false
        }
    }

    internal inner class ApplyNameTask : Runnable {

        internal inner class ClearNameStep : Runnable {
            override fun run() {
                val dialogDownZip = this@DialogDownZip
                dialogDownZip.applyName(null)
            }
        }
        override fun run() {
            val dialogDownZip = this@DialogDownZip
            if (dialogDownZip.appCtx != null) {
                val volume = MainUri.storageVolume()
                PrefSet.putDownUri(dialogDownZip.appCtx, volume)
                dialogDownZip.folderText = MainUri.displayPath(dialogDownZip.appCtx!!, volume)
                val handler = dialogDownZip.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(ClearNameStep())
            }
        }
    }

    internal inner class ScrollDownTask : Runnable {
        override fun run() {
            val nestedScrollView = this@DialogDownZip.scrollNode
            if (nestedScrollView != null) {
                nestedScrollView.scrollTo(130)
            }
        }
    }

    internal inner class SignOfTask(@JvmField val outer: DialogDownZip,
                                          @JvmField val checked: Boolean
    ) : Runnable {

        internal inner class CheckImagesTask(@JvmField val outer: SignOfTask,
                                             @JvmField val checked: Boolean
        ) : Runnable {
            override fun run() {
                val dialogDownZip = this.outer.outer
                if (dialogDownZip.imageUrls != null) {
                    val arrayList: ArrayList<Any?> = ArrayList<Any?>()
                    val sign = MainUtil.signOf(dialogDownZip.imageUrls!!.size)
                    val cachePath = MainUtil.newCacheFilePath(dialogDownZip.appCtx)
                    dialogDownZip.cacheDir = cachePath
                    if (!TextUtils.isEmpty(cachePath)) {
                        val sb = StringBuilder()
                        sb.append(dialogDownZip.cacheDir)
                        sb.append("/")
                        val namePrefix = sb.append(dialogDownZip.zipName).append("_").toString()
                        val paddedSign = MainUtil.zeroPadFormat(sign)
                        val imageUrls = dialogDownZip.imageUrls
                        val size = imageUrls!!.size
                        var index = 1
                        var nameIndex = 0
                        while (true) {
                            if (nameIndex >= size) {
                                dialogDownZip.validImages = arrayList
                                File(dialogDownZip.cacheDir!!).mkdir()
                                break
                            }
                            val obj: Any? = imageUrls.get(nameIndex)
                            nameIndex++
                            val url = obj as String?
                            if (dialogDownZip.imageUrls == null) {
                                break
                            }
                            val nameBuilder = StringBuilder(namePrefix)
                            if (TextUtils.isEmpty(paddedSign)) {
                                nameBuilder.append(index)
                            } else {
                                nameBuilder.append(String.format(Locale.US, paddedSign!!, index))
                            }
                            val fileExt = fileExtOf(url, false)
                            if (!TextUtils.isEmpty(fileExt)) {
                                nameBuilder.append(".")
                                nameBuilder.append(fileExt)
                            }
                            index++
                            val childItem = ChildItem()
                            childItem.url = url
                            childItem.referer = dialogDownZip.referer
                            childItem.path = nameBuilder.toString()
                            arrayList.add(childItem)
                        }
                    }
                }
                val validImages = dialogDownZip.validImages
                if (validImages == null || validImages.isEmpty()) {
                    return
                }
                dialogDownZip.startCheck(dialogDownZip.validImages, this.checked)
            }
        }
        override fun run() {
            val dialogDownZip = this.outer
            if (dialogDownZip.totalText != null) {
                val arrayList = dialogDownZip.validImages
                if (arrayList == null || arrayList.isEmpty()) {
                    val imageUrls = dialogDownZip.imageUrls
                    if (imageUrls != null && !imageUrls.isEmpty()) {
                        dialogDownZip.imageTotal = dialogDownZip.imageUrls!!.size
                    }
                } else {
                    dialogDownZip.imageTotal = dialogDownZip.validImages!!.size
                }
                dialogDownZip.checking = true
                dialogDownZip.imageDone = 0
                dialogDownZip.failCount = 0
                dialogDownZip.zipFiles = null
                dialogDownZip.showForm()
                dialogDownZip.setCanceledOnTouchOutside(false)
                dialogDownZip.scrollNode!!.setVisibility(0)
                dialogDownZip.resultNode!!.setVisibility(0)
                dialogDownZip.sectionNode!!.setText(AppR.string.verify_image)
                dialogDownZip.totalText!!.setText(MainUtil.countLabel(0, dialogDownZip.imageTotal))
                dialogDownZip.progressBar!!.max = dialogDownZip.imageTotal
                dialogDownZip.progressBar!!.progress = 0.0f
                dialogDownZip.statusText!!.setText("0")
                dialogDownZip.statusText!!.setTextColor(if (AppState.darkUi) -328966 else -16777216)
            }
            val validImages = dialogDownZip.validImages
            val checked = this.checked
            if (validImages == null || validImages.isEmpty()) {
                dialogDownZip.runInBackground(CheckImagesTask(this, checked))
            } else {
                dialogDownZip.startCheck(dialogDownZip.validImages, checked)
            }
        }
    }

    internal inner class ShowFormStep : DownZipListener {

        internal inner class ShowTotalStep : Runnable {
            override fun run() {
                val dialogDownZip = this@DialogDownZip
                val appCompatTextView: UiNode? = if (dialogDownZip.checking) dialogDownZip.totalText else null
                if (dialogDownZip.checking && appCompatTextView != null) {
                    appCompatTextView.setText(MainUtil.countLabel(dialogDownZip.imageDone, dialogDownZip.imageTotal))
                    dialogDownZip.progressBar!!.progress = (dialogDownZip.imageDone).toFloat()
                    val failCount = dialogDownZip.failCount
                    val statusText = dialogDownZip.statusText
                    if (failCount > 0) {
                        val sb = StringBuilder()
                        sb.append(dialogDownZip.appCtx!!.getString(AppR.string.not_loaded)).append("    ")
                        sb.append(dialogDownZip.failCount)
                        statusText!!.setText(sb.toString())
                        dialogDownZip.statusText!!.setTextColor(-769226)
                    } else {
                        statusText!!.setText("0")
                        dialogDownZip.statusText!!.setTextColor(if (AppState.darkUi) -328966 else -16777216)
                    }
                }
                dialogDownZip.checkStopped = false
            }
        }

        internal inner class ShowFormTask : Runnable {
            override fun run() {
                val dialogDownZip = this@DialogDownZip
                if (dialogDownZip.progressNode != null) {
                    dialogDownZip.showForm()
                    dialogDownZip.scrollNode!!.setVisibility(0)
                    dialogDownZip.progressNode!!.setVisibility(0)
                    val arrayList = dialogDownZip.zipFiles
                    val size = if (arrayList != null) arrayList.size else 0
                    dialogDownZip.checkTotalText!!.setText(dialogDownZip.imageTotal.toString())
                    dialogDownZip.checkSuccessText!!.setText(size.toString())
                    val failCount = dialogDownZip.failCount
                    val appCompatTextView = dialogDownZip.checkFailText
                    if (failCount > 0) {
                        val sb = StringBuilder()
                        sb.append(dialogDownZip.appCtx!!.getString(AppR.string.not_loaded)).append("    ")
                        sb.append(dialogDownZip.failCount)
                        appCompatTextView!!.setText(sb.toString())
                        dialogDownZip.checkFailText!!.setTextColor(-769226)
                    } else {
                        appCompatTextView!!.setText("0")
                        dialogDownZip.checkFailText!!.setTextColor(if (AppState.darkUi) -328966 else -16777216)
                    }
                    if (dialogDownZip.failCount > 0) {
                        val messageText = dialogDownZip.messageText
                        if (size == 0) {
                            messageText!!.setActivated(false)
                            dialogDownZip.messageText!!.setText(AppR.string.retry)
                            dialogDownZip.messageText!!.setTextColor(if (AppState.darkUi) -328966 else -14784824)
                            return
                        } else {
                            messageText!!.setActivated(false)
                            dialogDownZip.messageText!!.setText(AppR.string.create_zip)
                            dialogDownZip.messageText!!.setTextColor(if (AppState.darkUi) -328966 else -14784824)
                            dialogDownZip.retryButton!!.setVisibility(0)
                            return
                        }
                    }
                    if (size == 0) {
                        dialogDownZip.messageText!!.setActivated(true)
                        dialogDownZip.messageText!!.setText(AppR.string.close)
                        dialogDownZip.messageText!!.setTextColor(if (AppState.darkUi) -328966 else -16777216)
                        dialogDownZip.checkNode!!.setVisibility(0)
                        val nestedScrollView = dialogDownZip.scrollNode
                        if (nestedScrollView != null) {
                            nestedScrollView.post(dialogDownZip.ScrollDownTask())
                            return
                        }
                        return
                    }
                    val zipFiles = dialogDownZip.zipFiles
                    if (zipFiles == null || zipFiles.isEmpty()) {
                        return
                    }
                    dialogDownZip.cancelZip()
                    val messageText = dialogDownZip.messageText
                    if (messageText != null) {
                        messageText.post(dialogDownZip.StartZipTask())
                    }
                }
            }
        }
        override fun uiHandler(): Handler? {
            return this@DialogDownZip.mainHandler
        }
        override fun onSaved(arrayList: ArrayList<*>?) {
            val size = arrayList!!.size
            var itemIndex = 0
            var index = 0
            var failCount = 0
            var savedIndex = 0
            while (true) {
                val dialogDownZip = this@DialogDownZip
                if (savedIndex >= size) {
                    dialogDownZip.imageDone = index
                    dialogDownZip.failCount = failCount
                    var imageTotal = dialogDownZip.imageTotal
                    if (imageTotal < 0) {
                        dialogDownZip.imageTotal = 0
                        imageTotal = 0
                    }
                    if (index > imageTotal) {
                        dialogDownZip.imageDone = imageTotal
                    }
                    if (failCount > imageTotal) {
                        dialogDownZip.failCount = imageTotal
                    }
                    if (index < arrayList.size) {
                        if (dialogDownZip.checkStopped) {
                            return
                        }
                        dialogDownZip.checkStopped = true
                        val appCompatTextView = dialogDownZip.totalText
                        if (appCompatTextView != null) {
                            appCompatTextView.post(ShowTotalStep())
                            return
                        }
                        return
                    }
                    if (dialogDownZip.checking) {
                        dialogDownZip.checking = false
                        if (dialogDownZip.imageTotal > dialogDownZip.failCount) {
                            val zipFiles: ArrayList<Any?> = ArrayList<Any?>()
                            val itemCount = arrayList.size
                            while (itemIndex < itemCount) {
                                val rawChildItem: Any? = arrayList.get(itemIndex)
                                itemIndex++
                                val childItem = rawChildItem as ChildItem?
                                if (childItem != null && childItem.status == 3) {
                                    zipFiles.add(childItem.path)
                                }
                            }
                            dialogDownZip.zipFiles = zipFiles
                        }
                        val appCompatTextView = dialogDownZip.totalText
                        if (appCompatTextView == null) {
                            return
                        }
                        appCompatTextView.post(ShowFormTask())
                        return
                    }
                    return
                }
                val rawChildItem: Any? = arrayList.get(savedIndex)
                savedIndex++
                val childItem = rawChildItem as ChildItem?
                if (!dialogDownZip.checking) {
                    return
                }
                if (childItem != null) {
                    val status = childItem.status
                    if (status == 3) {
                        index++
                        continue
                    } else if (status != 4) {
                        continue
                    }
                }
                index++
                failCount++
            }
        }

        override val isRunning: Boolean
            get() = this@DialogDownZip.checking
    }

    internal inner class SaveAllTask(@JvmField val outer: DialogDownZip,
                                          @JvmField val items: ArrayList<Any?>,
                                          @JvmField val type: Int,
                                          @JvmField val width: Int,
                                          @JvmField val height: Int,
                                          @JvmField val listener: DownZipListener
    ) : Runnable {
        override fun run() {
            saveAll(this.outer.appCtx, this.items, this.type, this.width, this.height, this.listener)
        }
    }

    internal inner class StartZipTask : Runnable {
        override fun run() {
            val dialogDownZip = this@DialogDownZip
            val zipTask = ZipTask(dialogDownZip)
            dialogDownZip.runningZip = zipTask
            zipTask.start(dialogDownZip.appCtx)
        }
    }

    internal inner class CreateZipClick : View.OnClickListener {

        internal inner class CreateZipStep : Runnable {
            override fun run() {
                val dialogDownZip = this@DialogDownZip
                if (dialogDownZip.hostActivity != null) {
                    if (dialogDownZip.zipFailed) {
                        val lastImage = dialogDownZip.lastImage
                        PathSettings.edit(dialogDownZip.appCtx) { set(PathSettings.cmpPath, lastImage) }
                        val intent = Intent(dialogDownZip.appCtx, com.mycompany.app.ui.lists.AlbumListActivity::class.java as Class<*>)
                        intent.putExtra("EXTRA_TYPE", 3)
                        dialogDownZip.hostActivity!!.startActivity(intent)
                        dialogDownZip.dismiss()
                    } else {
                        val arrayList = dialogDownZip.zipFiles
                        if (arrayList == null || arrayList.isEmpty()) {
                            val validImages = dialogDownZip.validImages
                            if (validImages == null || validImages.isEmpty()) {
                                dialogDownZip.createZip()
                            } else {
                                val appCompatTextView = dialogDownZip.messageText
                                if (appCompatTextView != null) {
                                    appCompatTextView.post(
                                        SignOfTask(
                                            dialogDownZip,
                                            false
                                        )
                                    )
                                }
                            }
                        } else {
                            dialogDownZip.cancelZip()
                            val appCompatTextView = dialogDownZip.messageText
                            if (appCompatTextView != null) {
                                appCompatTextView.post(dialogDownZip.StartZipTask())
                            }
                        }
                    }
                }
                dialogDownZip.creating = false
            }
        }
        override fun onClick(view: View?) {
            val dialogDownZip = this@DialogDownZip
            val appCompatTextView = dialogDownZip.messageText
            if (appCompatTextView != null) {
                if (appCompatTextView.isActivated()) {
                    dialogDownZip.finishCheck()
                } else {
                    if (dialogDownZip.creating) {
                        return
                    }
                    dialogDownZip.creating = true
                    dialogDownZip.messageText!!.post(CreateZipStep())
                }
            }
        }
    }

    internal inner class CancelZipClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogDownZip = this@DialogDownZip
            val arrayList = dialogDownZip.validImages
            if (arrayList != null && !arrayList.isEmpty()) {
                val appCompatTextView = dialogDownZip.messageText
                if (appCompatTextView != null) {
                    appCompatTextView.post(SignOfTask(dialogDownZip, true))
                    return
                }
                return
            }
            val zipFiles = dialogDownZip.zipFiles
            if (zipFiles == null || zipFiles.isEmpty()) {
                return
            }
            dialogDownZip.cancelZip()
            val appCompatTextView = dialogDownZip.messageText
            if (appCompatTextView != null) {
                appCompatTextView.post(dialogDownZip.StartZipTask())
            }
        }
    }

    internal inner class ShowIconTask : Runnable {
        override fun run() {
            val dialogDownZip = this@DialogDownZip
            val arrayList = dialogDownZip.imageUrls
            if (arrayList == null || arrayList.isEmpty()) {
                return
            }
            val pendingName = dialogDownZip.imageUrls!!.get(0)
            val myRoundImage = dialogDownZip.previewImage
            if (myRoundImage != null) {
                myRoundImage.showIcon(-460552, AppR.drawable.outline_image_black_24)
                if (isSvgName(guessDownloadName(pendingName, null, null, true))) {
                    dialogDownZip.pendingName = pendingName
                    dialogDownZip.runInBackground(dialogDownZip.CheckNameTask())
                } else {
                    dialogDownZip.pendingPath = pendingName
                    dialogDownZip.runInBackground(dialogDownZip.CheckPathTask())
                }
            }
        }
    }

    class ZipTask(dialogDownZip: DialogDownZip?) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var items: ArrayList<Any?>? = null
        @JvmField
        var isOk: Boolean = false

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogDownZip)
            this.dialogRef = weakReference
            val dialogDownZip = weakReference.get() as DialogDownZip?
            if (dialogDownZip != null) {
                val arrayList = dialogDownZip.zipFiles
                this.items = arrayList
                if (dialogDownZip.progressNode == null || arrayList == null || arrayList.isEmpty()) {
                    return@init
                }
                dialogDownZip.zipRemaining = dialogDownZip.zipFiles!!.size
                dialogDownZip.zipFailed = false
                dialogDownZip.zipDone = 0
                dialogDownZip.zipCount = 0
                dialogDownZip.zipMonitor = null
                dialogDownZip.validImages = null
                dialogDownZip.showForm()
                dialogDownZip.setCanceledOnTouchOutside(false)
                dialogDownZip.scrollNode!!.setVisibility(0)
                dialogDownZip.progressNode!!.setVisibility(0)
                dialogDownZip.resultNode!!.setVisibility(0)
                val nestedScrollView = dialogDownZip.scrollNode
                if (nestedScrollView != null) {
                    nestedScrollView.post(dialogDownZip.ScrollDownTask())
                }
                dialogDownZip.sectionNode!!.setText(AppR.string.create_zip)
                dialogDownZip.totalText!!.setText(MainUtil.countLabel(0, dialogDownZip.zipRemaining))
                dialogDownZip.progressBar!!.max = dialogDownZip.zipRemaining
                dialogDownZip.progressBar!!.progress = 0.0f
                dialogDownZip.statusText!!.setText("0")
                dialogDownZip.statusText!!.setTextColor(if (AppState.darkUi) -328966 else -16777216)
            }

        }
    }
        public override fun doInBackground() {
            try {
                val dialogDownZip = this.dialogRef!!.get() as DialogDownZip?
                val arrayList: ArrayList<Any?>? = if (dialogDownZip == null || this.cancelled) null else this.items
                if (dialogDownZip == null || this.cancelled || arrayList == null || arrayList.isEmpty()) {
                    return
                }
                if (!TextUtils.isEmpty(dialogDownZip.tempUri)) {
                    MainUtil.deleteUri(dialogDownZip.appCtx!!, dialogDownZip.tempUri)
                    removePath(dialogDownZip.appCtx, dialogDownZip.tempUri)
                }
                val files: ArrayList<Any?> = ArrayList<Any?>()
                val size = arrayList.size
                var isOk = false
                var index = 0
                while (index < size) {
                    val obj: Any? = arrayList.get(index)
                    index++
                    val pathText = obj as String?
                    if (this.cancelled || pathText == null) {
                        return
                    } else {
                        files.add(File(pathText))
                    }
                }
                val path = dialogDownZip.cacheDir + "/" + System.currentTimeMillis()
                MainUtil.deleteUri(dialogDownZip.appCtx!!, path)
                val compressListener = dialogDownZip.zipListener
                try {
                    val zipFile = ZipFile(path)
                    zipFile.setFileNameCharset(MainConst.defaultCharset)
                    dialogDownZip.zipMonitor = zipFile.progressMonitor
                    val zipParameters = ZipParameters()
                    JarBridge.zipListener(zipParameters, compressListener)
                    zipParameters.compressionMethod = 8
                    zipParameters.compressionLevel = 5
                    zipFile.addFiles(files, zipParameters)
                } catch (unused: Exception) {
                }
                if (compressListener != null) {
                    val progressMonitor = dialogDownZip.zipMonitor
                    if (progressMonitor != null && progressMonitor.result != 2) {
                        isOk = true
                    }
                    (compressListener as SetTextStep).onDone(null, isOk)
                }
                dialogDownZip.tempUri = null
                val uriItemC = createUri(
                    dialogDownZip.appCtx,
                    MainUri.storageVolume(),
                    null,
                    (StringBuilder()).append(dialogDownZip.zipName).append(".zip").toString()
                )
                if (uriItemC != null) {
                    val uriString = uriItemC.uriString
                    dialogDownZip.tempUri = uriString
                    val moved = MainUtil.moveFileOrUri(dialogDownZip.appCtx, path, uriString)
                    this.isOk = moved
                    if (moved) {
                        addItem(dialogDownZip.appCtx, uriItemC)
                        dialogDownZip.lastImage = uriItemC.uriString
                    }
                }
            } catch (_ex: Throwable) {
                throw RuntimeException(_ex)
            }
        }
        public override fun onCancelled() {
            val dialogDownZip = this.dialogRef!!.get() as DialogDownZip?
            if (dialogDownZip == null) {
                return
            }
            dialogDownZip.runningZip = null
            dialogDownZip.zipMonitor = null
            dialogDownZip.dismiss()
        }
        public override fun onPostExecute() {
            val dialogDownZip = this.dialogRef!!.get() as DialogDownZip?
            if (dialogDownZip != null) {
                if (!this.isOk) {
                    dialogDownZip.zipCount = dialogDownZip.zipRemaining
                }
                dialogDownZip.runningZip = null
                dialogDownZip.zipMonitor = null
                val linearLayout = dialogDownZip.actionsNode
                if (linearLayout == null || linearLayout.getVisibility() == 0) {
                    return
                }
                dialogDownZip.showForm()
                dialogDownZip.scrollNode!!.setVisibility(0)
                dialogDownZip.progressNode!!.setVisibility(0)
                dialogDownZip.actionsNode!!.setVisibility(0)
                val nestedScrollView = dialogDownZip.scrollNode
                if (nestedScrollView != null) {
                    nestedScrollView.post(dialogDownZip.ScrollDownTask())
                }
                val zipRemaining = dialogDownZip.zipRemaining
                var index = zipRemaining - dialogDownZip.zipCount
                if (index < 0) {
                    index = 0
                }
                dialogDownZip.zipTotalText!!.setText(zipRemaining.toString())
                dialogDownZip.zipFailText!!.setText(dialogDownZip.zipCount.toString())
                dialogDownZip.zipSuccessText!!.setText(index.toString())
                if (dialogDownZip.zipCount <= 0) {
                    dialogDownZip.zipFailed = true
                    dialogDownZip.zipFailText!!.setTextColor(if (AppState.darkUi) -328966 else -16777216)
                    dialogDownZip.messageText!!.setActivated(false)
                    dialogDownZip.messageText!!.setText(AppR.string.list)
                    dialogDownZip.messageText!!.setTextColor(if (AppState.darkUi) -328966 else -14784824)
                    return
                }
                if (index == 0) {
                    dialogDownZip.zipFailText!!.setTextColor(-769226)
                    dialogDownZip.messageText!!.setActivated(false)
                    dialogDownZip.messageText!!.setText(AppR.string.retry)
                    dialogDownZip.messageText!!.setTextColor(if (AppState.darkUi) -328966 else -14784824)
                    return
                }
                dialogDownZip.zipFailed = true
                dialogDownZip.zipFailText!!.setTextColor(-769226)
                dialogDownZip.messageText!!.setActivated(false)
                dialogDownZip.messageText!!.setText(AppR.string.list)
                dialogDownZip.messageText!!.setTextColor(if (AppState.darkUi) -328966 else -14784824)
                dialogDownZip.retryButton!!.setVisibility(0)
            }
        }
    }

    fun cancelZip() {
        val progressMonitor = this.zipMonitor
        if (progressMonitor != null) {
            progressMonitor.cancelAllTasks()
        }
        val zipTask = this.runningZip
        if (zipTask != null) {
            zipTask.cancelled = true
        }
        this.runningZip = null
    }

    fun onActivityResultCode(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
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
                runInBackground(ApplyNameTask())
            }
            MainUtil.keepUriPermission(this.appCtx, data)
        }
        return true
    }

    fun finishCheck() {
        val myDialogLinear = this.rootNode
        if (myDialogLinear == null) {
            dismiss()
            return
        }
        this.checking = false
        if (this.runningZip == null) {
            dismiss()
            return
        }
        myDialogLinear.setCover(0, 0, true, false)
        this.messageText!!.setEnabled(false)
        this.messageText!!.setActivated(true)
        this.messageText!!.setText(AppR.string.canceling)
        this.messageText!!.setTextColor(if (AppState.darkUi) -8355712 else -2434342)
        cancelZip()
    }

    fun showForm() {
        if (this.formNode == null) {
            return
        }
        setCanceledOnTouchOutside(true)
        this.formNode!!.setDrawLine(false)
        this.formShown!!.setVisibility(8)
        this.scrollNode!!.setVisibility(8)
        this.progressNode!!.setVisibility(8)
        this.actionsNode!!.setVisibility(8)
        this.resultNode!!.setVisibility(8)
        this.retryButton!!.setVisibility(8)
        this.messageText!!.setActivated(true)
        this.messageText!!.setText(AppR.string.cancel)
        this.messageText!!.setTextColor(if (AppState.darkUi) -328966 else -16777216)
    }

    fun startCheck(arrayList: ArrayList<Any?>?, checked: Boolean) {
        if (arrayList == null || arrayList.isEmpty()) {
            return
        }
        val size = arrayList.size
        val batchSize = if (checked) 1 else 10
        val batchEnd = if (batchSize > size) size else batchSize
        var batches = size / batchEnd
        if (size % batchEnd != 0) {
            batches++
        }
        val batchStart = batches
        val showFormStep: ShowFormStep = ShowFormStep()
        if (checked) {
            val itemCount = arrayList.size
            var index = 0
            while (index < itemCount) {
                val rawChildItem: Any? = arrayList.get(index)
                index++
                val childItem = rawChildItem as ChildItem?
                if (!this.checking) {
                    return
                }
                if (childItem != null && childItem.status != 3) {
                    childItem.status = 1
                }
            }
        }
        var index = 0
        while (index < batchEnd && this.checking) {
            runInBackground(SaveAllTask(this, arrayList, batchStart, index, batchEnd, showFormStep))
            index++
        }
    }

    internal inner class SetTextStep : CompressListener {

        internal inner class ShowZipProgressStep : Runnable {
            override fun run() {
                val dialogDownZip = this@DialogDownZip
                val appCompatTextView: UiNode? = if (dialogDownZip.runningZip == null) null else dialogDownZip.totalText
                if (dialogDownZip.runningZip == null || appCompatTextView == null) {
                    return
                }
                appCompatTextView.setText(MainUtil.countLabel(dialogDownZip.zipDone, dialogDownZip.zipRemaining))
                dialogDownZip.progressBar!!.progress = (dialogDownZip.zipDone).toFloat()
                val zipCount = dialogDownZip.zipCount
                val statusText = dialogDownZip.statusText
                if (zipCount > 0) {
                    statusText!!.setText(zipCount.toString())
                    dialogDownZip.statusText!!.setTextColor(-769226)
                } else {
                    statusText!!.setText("0")
                    dialogDownZip.statusText!!.setTextColor(if (AppState.darkUi) -328966 else -16777216)
                }
            }
        }
        override fun onDone(path: String?, isOk: Boolean) {
            val dialogDownZip = this@DialogDownZip
            val doneCount = dialogDownZip.zipDone + 1
            dialogDownZip.zipDone = doneCount
            val zipRemaining = dialogDownZip.zipRemaining
            if (doneCount > zipRemaining) {
                dialogDownZip.zipDone = zipRemaining
            }
            if (!isOk) {
                val zipCount = dialogDownZip.zipCount + 1
                dialogDownZip.zipCount = zipCount
                if (zipCount > zipRemaining) {
                    dialogDownZip.zipCount = zipRemaining
                }
            }
            val appCompatTextView = dialogDownZip.totalText
            if (appCompatTextView == null) {
                return
            }
            appCompatTextView.post(ShowZipProgressStep())
        }
        override fun onProgress(entryName: String?, total: Long, done: Long) {
        }

        override val isCancelled: Boolean
            get() = this@DialogDownZip.runningZip == null
        override fun onEntry(entryName: String?) {
        }
    }

    /** Suggested zip name [str] (".zip" while a folder is chosen); the location label. */
    fun applyName(newBaseName: String?) {
        if (!TextUtils.isEmpty(newBaseName)) baseName = newBaseName
        val cleaned = MainUtil.safeFileName(if (nameEdited) name.trim() else baseName)
        if (TextUtils.isEmpty(MainUri.storageVolume())) {
            cleanName = cleaned
            name = cleaned ?: ""
            folderLabel = null
            return
        }
        folderLabel = folderText
        if (TextUtils.isEmpty(cleaned)) {
            cleanName = cleaned
            name = cleaned ?: ""
            return
        }
        val withExt = MainUtil.stripExtension(replaceExt(cleaned, ".zip"))
        cleanName = withExt
        name = withExt ?: ""
    }

    /** Create zip: checks the name, then starts verifying the images. */
    fun createZip() {
        if (appCtx == null) return
        if (TextUtils.isEmpty(MainUri.storageVolume())) {
            showToast(appCtx, AppR.string.select_dir)
            return
        }
        val cleaned = name.trim { it <= ' ' }
        if (TextUtils.isEmpty(cleaned)) {
            showToast(appCtx, AppR.string.input_name)
            return
        }
        if (cleaned.toByteArray().size > 200) {
            showToast(appCtx, AppR.string.long_name)
            return
        }
        val full = replaceExt(cleaned, ".zip")
        if (TextUtils.isEmpty(full)) {
            showToast(appCtx, AppR.string.input_name)
            return
        }
        zipName = MainUtil.stripExtension(MainUtil.safeFileName(full))
        ui.post(SignOfTask(this, false))
        applyListener?.onApply()
    }

    override fun cancel() {
        finishCheck()
    }

    override fun canCancelFromScrim(): Boolean = !checking && runningZip == null

    override fun dismiss() {
        if (appCtx == null) {
            super.dismiss()
            return
        }
        checking = false
        cancelZip()
        val tmp = cacheDir
        cacheDir = null
        if (!TextUtils.isEmpty(tmp)) runInBackground(KeepSegmentDirTask(tmp))
        val image = previewImage
        if (image != null) glide?.clear(image)
        glide = null
        previewImage?.release()
        previewImage = null
        hostActivity = null
        appCtx = null
        applyListener = null
        imageUrls = null
        validImages = null
        zipFiles = null
        zipMonitor = null
        folderList = null
        super.dismiss()
    }

    companion object {
    }
}
