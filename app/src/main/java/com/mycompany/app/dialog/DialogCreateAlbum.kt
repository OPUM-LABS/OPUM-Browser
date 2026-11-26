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
import android.net.Uri
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.webkit.URLUtil
import android.widget.ImageView.ScaleType
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.compress.Compress.Companion.isSvgName
import com.mycompany.app.compress.CompressUtil.CompressListener
import com.mycompany.app.db.DbAlbum
import com.mycompany.app.db.DbAlbum.Companion.addItem
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.db.DbUtil.updateById
import com.mycompany.app.db.book.DbBookWeb.Companion.setIcon
import com.mycompany.app.db.book.DbBookWeb.Companion.addItem
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.down.DownSaveZip.saveAll
import com.mycompany.app.main.BitmapUtil.decodeFile
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
import com.mycompany.app.main.MainUtil.requestCamera
import com.mycompany.app.main.MainUtil.pickTreeFolder
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.requestStoragePermission
import com.mycompany.app.main.MainUtil.editTextValue
import com.mycompany.app.main.MainUtil.fileExtOf
import com.mycompany.app.main.MainUtil.guessDownloadName
import com.mycompany.app.main.MainUtil.closeKeyboard
import com.mycompany.app.main.MainUtil.replaceExt
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.isBitmapOk
import com.mycompany.app.main.MainUtil.countLabel
import com.mycompany.app.main.MainUtil.scaleBitmap
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
import com.mycompany.app.main.MainUtil.copyFile
import com.mycompany.app.main.MainUtil.keepUriPermission
import com.mycompany.app.main.MainUtil.zeroPadFormat
import com.mycompany.app.main.MainUtil.signOf
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.main.MainUtil.newMediaUri
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefPath
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefSet.putBool
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
import kotlin.math.min
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Save the selected gallery images as an album ([arrayList] URLs, page title [pageTitleParam], referer [referer]): header with the
 * album icon (tap: default image / pick an image / camera, then crop) and the title, the image count, the album Name
 * (title + ".album") and save location; Create album first downloads ("Verify image") every image with Total n / m, a
 * bar and the not-loaded count, then shows Total / Fail / Success (Retry for the failed ones, or Create album anyway),
 * then packs them ("Create album" progress) and shows the result; List opens the album list. "No image" when nothing
 * could be loaded. The work (`DownSaveZip.saveAll`, [ZipTask] with zip4j, `DbAlbum`) and its state logic are the old code:
 * its view fields are now small Compose-state stand-ins ([UiNode], [UiBar]) so it runs unchanged. Compose now (on
 * [ComposeBottomDialog]); same constructor, shows itself; `onActivityResultCode` / `onPermissionResult` still take the picker, crop and camera results.
 */
@Suppress("deprecation")
class DialogCreateAlbum(
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

    /** Album result (Total / Fail / Success). */
    @JvmField var actionsNode: UiNode? = UiNode(false)
    @JvmField var titleNode: UiNode? = UiNode()
    @JvmField var iconListener: RequestListener<Any?>? = null
    @JvmField var pendingIconPath: String? = null
    @JvmField var zipTotalText: UiNode? = UiNode()
    @JvmField var previewListener: RequestListener<Any?>? = null
    @JvmField var zipListener: CompressListener? = null
    @JvmField var zipFailText: UiNode? = UiNode()
    @JvmField var zipSuccessText: UiNode? = UiNode()
    /** Progress section (verify / create album). */
    @JvmField var resultNode: UiNode? = UiNode(false)
    @JvmField var sectionNode: UiNode? = UiNode()
    @JvmField var totalText: UiNode? = UiNode()
    @JvmField var progressBar: UiBar? = UiBar()
    @JvmField var statusText: UiNode? = UiNode()
    /** Main button (activated = cancel / close). */
    @JvmField var messageText: UiNode? = UiNode()
    /** Second button (Retry). */
    @JvmField var retryButton: UiNode? = UiNode(false)
    @JvmField var baseName: String? = null
    @JvmField var albumName: String? = null
    @JvmField var tempUri: String? = null
    @JvmField var cleanName: String? = null
    @JvmField var nameEdited: Boolean = false
    @JvmField var pageTitle: String? = pageTitleParam
    @JvmField var hostActivity: WebViewActivity? = webViewActivity
    @JvmField var suggestedName: String? = null
    @JvmField var appCtx: Context? = context
    @JvmField var imageUrls: ArrayList<String?>? = arrayList
    @JvmField var applyListener: DialogApplyListener? = dialogApplyListener
    @JvmField var checking: Boolean = false
    @JvmField var referer: String? = referer
    @JvmField var validImages: ArrayList<Any?>? = null
    /** Busy cover. */
    @JvmField var rootNode: UiNode? = UiNode()
    @JvmField var imageTotal: Int = 0
    @JvmField var formNode: UiNode? = UiNode()
    @JvmField var imageDone: Int = 0
    @JvmField var iconImage: MyRoundImage? = null
    @JvmField var failCount: Int = 0
    @JvmField var iconRow: UiNode? = UiNode()
    @JvmField var zipFailed: Boolean = false
    /** "New" dot on the icon (one-time hint). */
    @JvmField var runningZip: ZipTask? = null
    @JvmField var zipFiles: ArrayList<Any?>? = null
    /** Form (count, name, location). */
    @JvmField var iconNode: UiNode? = UiNode()
    @JvmField var zipRunning: Boolean = false
    @JvmField var zipRemaining: Int = 0
    @JvmField var zipDone: Int = 0
    @JvmField var zipCount: Int = 0
    @JvmField var zipMonitor: ProgressMonitor? = null
    @JvmField var folderList: ArrayList<Any?>? = null
    @JvmField var folderText: String? = null
    /** Progress / result area. */
    @JvmField var scrollNode: UiNode? = UiNode(false)
    @JvmField var cameraUri: Uri? = null
    /** Verify result. */
    @JvmField var progressNode: UiNode? = UiNode(false)
    @JvmField var iconPath: String? = null
    @JvmField var pickedIcon: String? = null
    @JvmField var iconFile: String? = null
    @JvmField var checkTotalText: UiNode? = UiNode()
    @JvmField var albumPath: String? = null
    @JvmField var cacheDir: String? = null
    @JvmField var checkFailText: UiNode? = UiNode()
    @JvmField var lastImage: String? = null
    @JvmField var creating: Boolean = false
    @JvmField var checkSuccessText: UiNode? = UiNode()
    @JvmField var checkStopped: Boolean = false
    /** "No image". */
    @JvmField var checkNode: UiNode? = UiNode(false)
    @JvmField var glide: GlideRequests? = null
    @JvmField var pendingIcon: String? = null

    private var name by mutableStateOf("")
    private var folderLabel by mutableStateOf<String?>(null)
    private var folderMenu by mutableStateOf(false)
    private var iconMenu by mutableStateOf(false)

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
        iconListener = ShowIconLoadListener()
        previewListener = IconRequestListener()
        zipListener = SetTextStep()
        titleNode!!.setText(AppR.string.create_album)
        messageText!!.setText(AppR.string.create_album)
        retryButton!!.setText(AppR.string.retry)
        runInBackground(Runnable {
            val ctx = appCtx ?: return@Runnable
            val suggested = MainUtil.safeTitleFileName(184, pageTitle, "Album")
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
                Box {
                    AndroidView(
                        factory = { ctx ->
                            MyRoundImage(ctx).also {
                                it.setScaleType(ScaleType.CENTER_CROP)
                                it.setCircleRadius(MainApp.dp40 / 2.0f)
                                iconImage = it
                                ui.post { SetIconTask().run() }
                            }
                        },
                        modifier = Modifier.size(40.dp).clip(CircleShape).clickable(enabled = iconNode!!.shown) { openIconMenu() },
                    )
                    com.mycompany.app.ui.components.BubbleMenu(expanded = iconMenu, onDismiss = { iconMenu = false }) {
                        DropdownMenuItem(text = { Text(stringResource(AppR.string.default_image)) }, onClick = { iconMenu = false; iconPick(0) })
                        DropdownMenuItem(text = { Text(stringResource(AppR.string.image)) }, onClick = { iconMenu = false; iconPick(1) })
                        DropdownMenuItem(text = { Text(stringResource(AppR.string.camera)) }, onClick = { iconMenu = false; iconPick(2) })
                    }
                }
                Text(
                    pageTitle ?: "", maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                if (iconNode!!.shown) Form()
                if (scrollNode!!.shown) {
                    if (progressNode!!.shown) {
                        Section(stringResource(AppR.string.verify_image))
                        Line(stringResource(AppR.string.total), checkTotalText!!.str())
                        Line(stringResource(AppR.string.fail), checkFailText!!.str(), red = checkFailText!!.color == -769226)
                        Line(stringResource(AppR.string.success), checkSuccessText!!.str())
                    }
                    if (checkNode!!.shown) {
                        Text(
                            stringResource(AppR.string.no_image), color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                        )
                    }
                    if (actionsNode!!.shown) {
                        Section(titleNode!!.str())
                        Line(stringResource(AppR.string.total), zipTotalText!!.str())
                        Line(stringResource(AppR.string.fail), zipFailText!!.str(), red = zipFailText!!.color == -769226)
                        Line(stringResource(AppR.string.success), zipSuccessText!!.str())
                    }
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
                }
            }
            if (rootNode!!.cover) LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                if (retryButton!!.shown) {
                    TextButton(onClick = { CancelZipClick().onClick(null) }) { Text(retryButton!!.str()) }
                }
                TextButton(onClick = { CreateAlbumClick().onClick(null) }, enabled = messageText!!.enabledState) { Text(messageText!!.str()) }
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
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (!creating) createAlbum() }),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        )
        Box {
            ListItem(
                headlineContent = { Text(stringResource(AppR.string.down_location)) },
                supportingContent = {
                    if (folderLabel == null) {
                        Text(stringResource(AppR.string.folder_not_selected), color = MaterialTheme.colorScheme.error)
                    } else {
                        Text(folderLabel!!, maxLines = 2)
                    }
                },
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

    private fun openIconMenu() {
        if (hostActivity == null) return
        cameraUri = null
        iconPath = null
        iconMenu = true
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

    /** Album icon menu: 0 default (first image), 1 pick image, 2 camera. */
    fun iconPick(iconType: Int): Boolean {
        val dialogCreateAlbum = this@DialogCreateAlbum
        if (iconType == 1) {
            requestStoragePermission(dialogCreateAlbum.hostActivity, RequestCode.CAMERA_OR_FILE)
            return true
        }
        if (iconType == 2) {
            if (!requestCamera(dialogCreateAlbum.hostActivity, 30)) {
                dialogCreateAlbum.cameraUri = MainUtil.newMediaUri(RequestCode.CAMERA_OR_FILE, dialogCreateAlbum.hostActivity, false)
                return true
            }
        } else if (!TextUtils.isEmpty(dialogCreateAlbum.iconFile)) {
            dialogCreateAlbum.setIcon(dialogCreateAlbum.pickedIcon, true)
        }
        return true
    }
    internal inner class SetIconTask : Runnable {
        override fun run() {
            val dialogCreateAlbum = this@DialogCreateAlbum
            val arrayList = dialogCreateAlbum.imageUrls
            if (arrayList == null || arrayList.isEmpty()) {
                return
            }
            dialogCreateAlbum.setIcon(dialogCreateAlbum.imageUrls!!.get(0), true)
        }
    }
    internal inner class KeepSegmentDirTask(@JvmField val segmentDir: String?) : Runnable {
        override fun run() {
            MainUtil.noOp(this.segmentDir)
        }
    }
    internal inner class GlideUrlForTask : Runnable {
        internal inner class ApplyIconStep : Runnable {
            override fun run() {
                val dialogCreateAlbum = this@DialogCreateAlbum
                val pendingIcon = dialogCreateAlbum.pendingIcon
                dialogCreateAlbum.pendingIcon = null
                if (dialogCreateAlbum.glide == null) {
                    return
                }
                val isNetworkIcon = URLUtil.isNetworkUrl(pendingIcon)
                val diskCacheStrategy = DiskCacheStrategy.NONE
                if (isNetworkIcon) {
                    (dialogCreateAlbum.glide!!.load(
                        MainUtil.glideUrlFor(
                            dialogCreateAlbum.appCtx,
                            pendingIcon!!,
                            dialogCreateAlbum.referer
                        )
                    ).diskCacheStrategy(diskCacheStrategy)).anyListener(dialogCreateAlbum.iconListener)
                        .into(dialogCreateAlbum.iconImage!!)
                } else {
                    (dialogCreateAlbum.glide!!.load(pendingIcon).diskCacheStrategy(diskCacheStrategy)).anyListener(
                        dialogCreateAlbum.iconListener
                    ).into(dialogCreateAlbum.iconImage!!)
                }
            }
        }
        override fun run() {
            val dialogCreateAlbum = this@DialogCreateAlbum
            val webViewActivity = dialogCreateAlbum.hostActivity
            if (webViewActivity != null) {
                if (dialogCreateAlbum.glide == null) {
                    dialogCreateAlbum.glide = GlideApp.with(webViewActivity)
                }
                val handler = dialogCreateAlbum.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(ApplyIconStep())
            }
        }
    }
    internal inner class ShowIconLoadListener : RequestListener<Any?> {
        override fun onLoadFailed(glideException: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Any?>, isFirstResource: Boolean): Boolean {
            val myRoundImage = this@DialogCreateAlbum.iconImage
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
    internal inner class CheckIconPathTask : Runnable {
        internal inner class ApplyIconPathStep : Runnable {
            override fun run() {
                val dialogCreateAlbum = this@DialogCreateAlbum
                val pendingIconPath = dialogCreateAlbum.pendingIconPath
                dialogCreateAlbum.pendingIconPath = null
                if (dialogCreateAlbum.glide == null) {
                    return
                }
                if (!URLUtil.isNetworkUrl(pendingIconPath)) {
                    ((dialogCreateAlbum.glide!!.`as`(PictureDrawable::class.java)).load(
                        pendingIconPath
                    )).anyListener(dialogCreateAlbum.previewListener).into(dialogCreateAlbum.iconImage!!)
                    return
                }
                ((dialogCreateAlbum.glide!!.`as`(PictureDrawable::class.java)).load(
                    MainUtil.glideUrlFor(dialogCreateAlbum.appCtx, pendingIconPath!!, dialogCreateAlbum.referer)
                )).anyListener(dialogCreateAlbum.previewListener).into(dialogCreateAlbum.iconImage!!)
            }
        }
        override fun run() {
            val dialogCreateAlbum = this@DialogCreateAlbum
            val webViewActivity = dialogCreateAlbum.hostActivity
            if (webViewActivity != null) {
                if (dialogCreateAlbum.glide == null) {
                    dialogCreateAlbum.glide = GlideApp.with(webViewActivity)
                }
                val handler = dialogCreateAlbum.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(ApplyIconPathStep())
            }
        }
    }
    internal inner class IconRequestListener : RequestListener<Any?> {
        override fun onLoadFailed(glideException: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Any?>, isFirstResource: Boolean): Boolean {
            val dialogCreateAlbum = this@DialogCreateAlbum
            val myRoundImage = dialogCreateAlbum.iconImage
            if (myRoundImage == null) {
                return true
            }
            myRoundImage.setLayerType(View.LAYER_TYPE_NONE, null)
            dialogCreateAlbum.iconImage!!.showIcon(-460552, AppR.drawable.outline_image_black_24)
            return true
        }
        override fun onResourceReady(resource: Any?, model: Any, target: com.bumptech.glide.request.target.Target<Any?>?, dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
            val myRoundImage = this@DialogCreateAlbum.iconImage
            if (myRoundImage != null) {
                myRoundImage.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            }
            return false
        }
    }
    internal inner class ApplyNameTask : Runnable {
        internal inner class ClearNameStep : Runnable {
            override fun run() {
                val dialogCreateAlbum = this@DialogCreateAlbum
                dialogCreateAlbum.applyName(null)
            }
        }
        override fun run() {
            val dialogCreateAlbum = this@DialogCreateAlbum
            if (dialogCreateAlbum.appCtx != null) {
                val volume = MainUri.storageVolume()
                PrefSet.putDownUri(dialogCreateAlbum.appCtx, volume)
                dialogCreateAlbum.folderText = MainUri.displayPath(dialogCreateAlbum.appCtx!!, volume)
                val handler = dialogCreateAlbum.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(ClearNameStep())
            }
        }
    }

    internal inner class ScrollDownTask : Runnable {
        override fun run() {
            val nestedScrollView = this@DialogCreateAlbum.scrollNode
            if (nestedScrollView != null) {
                nestedScrollView.scrollTo(130)
            }
        }
    }
    internal inner class SignOfTask(@JvmField val outer: DialogCreateAlbum,
                                          @JvmField val checked: Boolean
    ) : Runnable {
        internal inner class CheckImagesTask(@JvmField val outer: SignOfTask,
                                             @JvmField val checked: Boolean
        ) : Runnable {
            override fun run() {
                val dialogCreateAlbum = this.outer.outer
                if (dialogCreateAlbum.imageUrls != null) {
                    val arrayList: ArrayList<Any?> = ArrayList<Any?>()
                    val sign = MainUtil.signOf(dialogCreateAlbum.imageUrls!!.size)
                    val cachePath = MainUtil.newCacheFilePath(dialogCreateAlbum.appCtx)
                    dialogCreateAlbum.cacheDir = cachePath
                    if (!TextUtils.isEmpty(cachePath)) {
                        val sb = StringBuilder()
                        sb.append(dialogCreateAlbum.cacheDir)
                        sb.append("/")
                        val namePrefix = sb.append(dialogCreateAlbum.albumName).append("_").toString()
                        val paddedSign = MainUtil.zeroPadFormat(sign)
                        val imageUrls = dialogCreateAlbum.imageUrls
                        val size = imageUrls!!.size
                        var index = 1
                        var nameIndex = 0
                        while (true) {
                            if (nameIndex >= size) {
                                dialogCreateAlbum.validImages = arrayList
                                File(dialogCreateAlbum.cacheDir!!).mkdir()
                                break
                            }
                            val obj: Any? = imageUrls.get(nameIndex)
                            nameIndex++
                            val url = obj as String?
                            if (dialogCreateAlbum.imageUrls == null) {
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
                            childItem.referer = dialogCreateAlbum.referer
                            childItem.path = nameBuilder.toString()
                            arrayList.add(childItem)
                        }
                    }
                }
                val validImages = dialogCreateAlbum.validImages
                if (validImages == null || validImages.isEmpty()) {
                    return
                }
                dialogCreateAlbum.startCheck(dialogCreateAlbum.validImages, this.checked)
            }
        }
        override fun run() {
            val dialogCreateAlbum = this.outer
            if (dialogCreateAlbum.totalText != null) {
                val arrayList = dialogCreateAlbum.validImages
                if (arrayList == null || arrayList.isEmpty()) {
                    val imageUrls = dialogCreateAlbum.imageUrls
                    if (imageUrls != null && !imageUrls.isEmpty()) {
                        dialogCreateAlbum.imageTotal = dialogCreateAlbum.imageUrls!!.size
                    }
                } else {
                    dialogCreateAlbum.imageTotal = dialogCreateAlbum.validImages!!.size
                }
                dialogCreateAlbum.checking = true
                dialogCreateAlbum.imageDone = 0
                dialogCreateAlbum.failCount = 0
                dialogCreateAlbum.zipFiles = null
                dialogCreateAlbum.showForm()
                dialogCreateAlbum.setCanceledOnTouchOutside(false)
                dialogCreateAlbum.scrollNode!!.setVisibility(0)
                dialogCreateAlbum.resultNode!!.setVisibility(0)
                dialogCreateAlbum.sectionNode!!.setText(AppR.string.verify_image)
                dialogCreateAlbum.totalText!!.setText(MainUtil.countLabel(0, dialogCreateAlbum.imageTotal))
                dialogCreateAlbum.progressBar!!.max = dialogCreateAlbum.imageTotal
                dialogCreateAlbum.progressBar!!.progress = 0.0f
                dialogCreateAlbum.statusText!!.setText("0")
                dialogCreateAlbum.statusText!!.setTextColor(if (AppState.darkUi) -328966 else -16777216)
            }
            val validImages = dialogCreateAlbum.validImages
            val checked = this.checked
            if (validImages == null || validImages.isEmpty()) {
                dialogCreateAlbum.runInBackground(CheckImagesTask(this, checked))
            } else {
                dialogCreateAlbum.startCheck(dialogCreateAlbum.validImages, checked)
            }
        }
    }

    internal inner class ShowFormStep : DownZipListener {
        internal inner class ShowTotalStep : Runnable {
            override fun run() {
                val dialogCreateAlbum = this@DialogCreateAlbum
                val appCompatTextView: UiNode? = if (dialogCreateAlbum.checking) dialogCreateAlbum.totalText else null
                if (dialogCreateAlbum.checking && appCompatTextView != null) {
                    appCompatTextView.setText(MainUtil.countLabel(dialogCreateAlbum.imageDone, dialogCreateAlbum.imageTotal))
                    dialogCreateAlbum.progressBar!!.progress = (dialogCreateAlbum.imageDone).toFloat()
                    val failCount = dialogCreateAlbum.failCount
                    val statusText = dialogCreateAlbum.statusText
                    if (failCount > 0) {
                        val sb = StringBuilder()
                        sb.append(dialogCreateAlbum.appCtx!!.getString(AppR.string.not_loaded)).append("    ")
                        sb.append(dialogCreateAlbum.failCount)
                        statusText!!.setText(sb.toString())
                        dialogCreateAlbum.statusText!!.setTextColor(-769226)
                    } else {
                        statusText!!.setText("0")
                        dialogCreateAlbum.statusText!!.setTextColor(if (AppState.darkUi) -328966 else -16777216)
                    }
                }
                dialogCreateAlbum.checkStopped = false
            }
        }
        internal inner class ShowFormTask : Runnable {
            override fun run() {
                val dialogCreateAlbum = this@DialogCreateAlbum
                if (dialogCreateAlbum.progressNode != null) {
                    dialogCreateAlbum.showForm()
                    dialogCreateAlbum.scrollNode!!.setVisibility(0)
                    dialogCreateAlbum.progressNode!!.setVisibility(0)
                    val arrayList = dialogCreateAlbum.zipFiles
                    val size = if (arrayList != null) arrayList.size else 0
                    dialogCreateAlbum.checkTotalText!!.setText(dialogCreateAlbum.imageTotal.toString())
                    dialogCreateAlbum.checkSuccessText!!.setText(size.toString())
                    val failCount = dialogCreateAlbum.failCount
                    val appCompatTextView = dialogCreateAlbum.checkFailText
                    if (failCount > 0) {
                        val sb = StringBuilder()
                        sb.append(dialogCreateAlbum.appCtx!!.getString(AppR.string.not_loaded)).append("    ")
                        sb.append(dialogCreateAlbum.failCount)
                        appCompatTextView!!.setText(sb.toString())
                        dialogCreateAlbum.checkFailText!!.setTextColor(-769226)
                    } else {
                        appCompatTextView!!.setText("0")
                        dialogCreateAlbum.checkFailText!!.setTextColor(if (AppState.darkUi) -328966 else -16777216)
                    }
                    if (dialogCreateAlbum.failCount > 0) {
                        val messageText = dialogCreateAlbum.messageText
                        if (size == 0) {
                            messageText!!.setActivated(false)
                            dialogCreateAlbum.messageText!!.setText(AppR.string.retry)
                            dialogCreateAlbum.messageText!!.setTextColor(if (AppState.darkUi) -328966 else -14784824)
                            return
                        } else {
                            messageText!!.setActivated(false)
                            dialogCreateAlbum.messageText!!.setText(AppR.string.create_album)
                            dialogCreateAlbum.messageText!!.setTextColor(if (AppState.darkUi) -328966 else -14784824)
                            dialogCreateAlbum.retryButton!!.setVisibility(0)
                            return
                        }
                    }
                    if (size == 0) {
                        dialogCreateAlbum.messageText!!.setActivated(true)
                        dialogCreateAlbum.messageText!!.setText(AppR.string.close)
                        dialogCreateAlbum.messageText!!.setTextColor(if (AppState.darkUi) -328966 else -16777216)
                        dialogCreateAlbum.checkNode!!.setVisibility(0)
                        val nestedScrollView = dialogCreateAlbum.scrollNode
                        if (nestedScrollView != null) {
                            nestedScrollView.post(dialogCreateAlbum.ScrollDownTask())
                            return
                        }
                        return
                    }
                    val zipFiles = dialogCreateAlbum.zipFiles
                    if (zipFiles == null || zipFiles.isEmpty()) {
                        return
                    }
                    dialogCreateAlbum.cancelZip()
                    val messageText = dialogCreateAlbum.messageText
                    if (messageText != null) {
                        messageText.post(dialogCreateAlbum.StartZipTask())
                    }
                }
            }
        }
        override fun uiHandler(): Handler? {
            return this@DialogCreateAlbum.mainHandler
        }
        override fun onSaved(arrayList: ArrayList<*>?) {
            val size = arrayList!!.size
            var index = 0
            var failCount = 0
            var savedIndex = 0
            while (true) {
                val dialogCreateAlbum = this@DialogCreateAlbum
                if (savedIndex >= size) {
                    dialogCreateAlbum.imageDone = index
                    dialogCreateAlbum.failCount = failCount
                    var imageTotal = dialogCreateAlbum.imageTotal
                    if (imageTotal < 0) {
                        dialogCreateAlbum.imageTotal = 0
                        imageTotal = 0
                    }
                    if (index > imageTotal) {
                        dialogCreateAlbum.imageDone = imageTotal
                    }
                    if (failCount > imageTotal) {
                        dialogCreateAlbum.failCount = imageTotal
                    }
                    if (index < arrayList.size) {
                        if (dialogCreateAlbum.checkStopped) {
                            return
                        }
                        dialogCreateAlbum.checkStopped = true
                        val appCompatTextView = dialogCreateAlbum.totalText
                        if (appCompatTextView != null) {
                            appCompatTextView.post(ShowTotalStep())
                            return
                        }
                        return
                    }
                    if (dialogCreateAlbum.checking) {
                        dialogCreateAlbum.checking = false
                        if (dialogCreateAlbum.imageTotal > dialogCreateAlbum.failCount) {
                            val zipFiles: ArrayList<Any?> = ArrayList<Any?>()
                            val itemCount = arrayList.size
                            var itemIndex = 0
                            while (itemIndex < itemCount) {
                                val rawChildItem: Any? = arrayList.get(itemIndex)
                                itemIndex++
                                val childItem = rawChildItem as ChildItem?
                                if (childItem != null && childItem.status == 3) {
                                    zipFiles.add(childItem.path)
                                }
                            }
                            dialogCreateAlbum.zipFiles = zipFiles
                            if (!zipFiles.isEmpty()) {
                                if (TextUtils.isEmpty(dialogCreateAlbum.albumPath)) {
                                    val albumIconPath =
                                        (StringBuilder()).append(dialogCreateAlbum.cacheDir).append("/icon_album").toString()
                                    if (!TextUtils.isEmpty(dialogCreateAlbum.iconFile) && File(
                                            dialogCreateAlbum.iconFile!!
                                        ).exists() && MainUtil.copyFile(dialogCreateAlbum.iconFile, albumIconPath)
                                    ) {
                                        dialogCreateAlbum.albumPath = albumIconPath
                                    } else {
                                        val bitmapC = decodeFile(dialogCreateAlbum.zipFiles!!.get(0) as String?)
                                        if (MainUtil.isBitmapOk(bitmapC)) {
                                            val scale = (min(
                                                bitmapC!!.getWidth(),
                                                bitmapC.getHeight()
                                            ) / MainApp.dp40).toFloat()
                                            if (MainUtil.saveBitmapPng(
                                                    dialogCreateAlbum.appCtx,
                                                    MainUtil.scaleBitmap(
                                                        Math.round(bitmapC.getWidth() / scale),
                                                        Math.round(bitmapC.getHeight() / scale),
                                                        bitmapC
                                                    ),
                                                    albumIconPath
                                                )
                                            ) {
                                                dialogCreateAlbum.albumPath = albumIconPath
                                            }
                                        }
                                    }
                                }
                                if (!TextUtils.isEmpty(dialogCreateAlbum.albumPath)) {
                                    zipFiles.add(0, dialogCreateAlbum.albumPath)
                                }
                            }
                        }
                        val appCompatTextView = dialogCreateAlbum.totalText
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
                if (!dialogCreateAlbum.checking) {
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
            get() = this@DialogCreateAlbum.checking
    }
    internal inner class SaveAllTask(@JvmField val outer: DialogCreateAlbum,
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
            val dialogCreateAlbum = this@DialogCreateAlbum
            val zipTask = ZipTask(dialogCreateAlbum)
            dialogCreateAlbum.runningZip = zipTask
            zipTask.start(dialogCreateAlbum.appCtx)
        }
    }

    internal inner class CreateAlbumClick : View.OnClickListener {
        internal inner class CreateAlbumStep : Runnable {
            override fun run() {
                val dialogCreateAlbum = this@DialogCreateAlbum
                if (dialogCreateAlbum.hostActivity != null) {
                    if (dialogCreateAlbum.zipFailed) {
                        val lastImage = dialogCreateAlbum.lastImage
                        PathSettings.edit(dialogCreateAlbum.appCtx) { set(PathSettings.albumPath, lastImage) }
                        val intent =
                            Intent(dialogCreateAlbum.appCtx, com.mycompany.app.ui.lists.AlbumListActivity::class.java as Class<*>)
                        intent.putExtra("EXTRA_TYPE", 1)
                        dialogCreateAlbum.hostActivity!!.startActivity(intent)
                        dialogCreateAlbum.dismiss()
                    } else {
                        val arrayList = dialogCreateAlbum.zipFiles
                        if (arrayList == null || arrayList.isEmpty()) {
                            val validImages = dialogCreateAlbum.validImages
                            if (validImages == null || validImages.isEmpty()) {
                                dialogCreateAlbum.createAlbum()
                            } else {
                                val appCompatTextView = dialogCreateAlbum.messageText
                                if (appCompatTextView != null) {
                                    appCompatTextView.post(
                                        SignOfTask(
                                            dialogCreateAlbum,
                                            false
                                        )
                                    )
                                }
                            }
                        } else {
                            dialogCreateAlbum.cancelZip()
                            val appCompatTextView = dialogCreateAlbum.messageText
                            if (appCompatTextView != null) {
                                appCompatTextView.post(dialogCreateAlbum.StartZipTask())
                            }
                        }
                    }
                }
                dialogCreateAlbum.creating = false
            }
        }
        override fun onClick(view: View?) {
            val dialogCreateAlbum = this@DialogCreateAlbum
            val appCompatTextView = dialogCreateAlbum.messageText
            if (appCompatTextView != null) {
                if (appCompatTextView.isActivated()) {
                    dialogCreateAlbum.finishCheck()
                } else {
                    if (dialogCreateAlbum.creating) {
                        return
                    }
                    dialogCreateAlbum.creating = true
                    dialogCreateAlbum.messageText!!.post(CreateAlbumStep())
                }
            }
        }
    }
    internal inner class CancelZipClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogCreateAlbum = this@DialogCreateAlbum
            val arrayList = dialogCreateAlbum.validImages
            if (arrayList != null && !arrayList.isEmpty()) {
                val appCompatTextView = dialogCreateAlbum.messageText
                if (appCompatTextView != null) {
                    appCompatTextView.post(
                        SignOfTask(
                            dialogCreateAlbum,
                            true
                        )
                    )
                    return
                }
                return
            }
            val zipFiles = dialogCreateAlbum.zipFiles
            if (zipFiles == null || zipFiles.isEmpty()) {
                return
            }
            dialogCreateAlbum.cancelZip()
            val appCompatTextView = dialogCreateAlbum.messageText
            if (appCompatTextView != null) {
                appCompatTextView.post(dialogCreateAlbum.StartZipTask())
            }
        }
    }

    class ZipTask(dialogCreateAlbum: DialogCreateAlbum?) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var items: ArrayList<Any?>? = null
        @JvmField
        var isOk: Boolean = false

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogCreateAlbum)
            this.dialogRef = weakReference
            val dialogCreateAlbum = weakReference.get() as DialogCreateAlbum?
            if (dialogCreateAlbum != null) {
                val arrayList = dialogCreateAlbum.zipFiles
                this.items = arrayList
                if (dialogCreateAlbum.progressNode == null || arrayList == null || arrayList.isEmpty()) {
                    return@init
                }
                dialogCreateAlbum.zipRemaining = dialogCreateAlbum.zipFiles!!.size
                if (TextUtils.isEmpty(dialogCreateAlbum.albumPath)) {
                    dialogCreateAlbum.zipRunning = false
                } else {
                    dialogCreateAlbum.zipRemaining--
                    dialogCreateAlbum.zipRunning = true
                }
                dialogCreateAlbum.zipFailed = false
                dialogCreateAlbum.zipDone = 0
                dialogCreateAlbum.zipCount = 0
                dialogCreateAlbum.zipMonitor = null
                dialogCreateAlbum.validImages = null
                dialogCreateAlbum.showForm()
                dialogCreateAlbum.setCanceledOnTouchOutside(false)
                dialogCreateAlbum.scrollNode!!.setVisibility(0)
                dialogCreateAlbum.progressNode!!.setVisibility(0)
                dialogCreateAlbum.resultNode!!.setVisibility(0)
                val nestedScrollView = dialogCreateAlbum.scrollNode
                if (nestedScrollView != null) {
                    nestedScrollView.post(dialogCreateAlbum.ScrollDownTask())
                }
                dialogCreateAlbum.sectionNode!!.setText(AppR.string.create_album)
                dialogCreateAlbum.totalText!!.setText(MainUtil.countLabel(0, dialogCreateAlbum.zipRemaining))
                dialogCreateAlbum.progressBar!!.max = dialogCreateAlbum.zipRemaining
                dialogCreateAlbum.progressBar!!.progress = 0.0f
                dialogCreateAlbum.statusText!!.setText("0")
                dialogCreateAlbum.statusText!!.setTextColor(if (AppState.darkUi) -328966 else -16777216)
            }
        
        }
    }
        public override fun doInBackground() {
            try {
                val dialogCreateAlbum = this.dialogRef!!.get() as DialogCreateAlbum?
                val arrayList: ArrayList<Any?>? = if (dialogCreateAlbum == null || this.cancelled) null else this.items
                if (dialogCreateAlbum == null || this.cancelled || arrayList == null || arrayList.isEmpty()) {
                    return
                }
                if (!TextUtils.isEmpty(dialogCreateAlbum.tempUri)) {
                    MainUtil.deleteUri(dialogCreateAlbum.appCtx!!, dialogCreateAlbum.tempUri)
                    DbAlbum.removePath(dialogCreateAlbum.appCtx, dialogCreateAlbum.tempUri)
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
                val path = dialogCreateAlbum.cacheDir + "/" + System.currentTimeMillis()
                MainUtil.deleteUri(dialogCreateAlbum.appCtx!!, path)
                val compressListener = dialogCreateAlbum.zipListener
                try {
                    val zipFile = ZipFile(path)
                    zipFile.setFileNameCharset(MainConst.defaultCharset)
                    dialogCreateAlbum.zipMonitor = zipFile.progressMonitor
                    val zipParameters = ZipParameters()
                    JarBridge.zipListener(zipParameters, compressListener)
                    zipParameters.compressionMethod = 8
                    zipParameters.compressionLevel = 5
                    zipParameters.isEncryptFiles = true
                    zipParameters.encryptionMethod = 0
                    zipParameters.setPassword("debug_logger_tag".toCharArray())
                    zipFile.addFiles(files, zipParameters)
                } catch (unused: Exception) {
                }
                if (compressListener != null) {
                    val progressMonitor = dialogCreateAlbum.zipMonitor
                    if (progressMonitor != null && progressMonitor.result != 2) {
                        isOk = true
                    }
                    (compressListener as SetTextStep).onDone(null, isOk)
                }
                dialogCreateAlbum.tempUri = null
                val uriItemC = createUri(
                    dialogCreateAlbum.appCtx,
                    MainUri.storageVolume(),
                    null,
                    (StringBuilder()).append(dialogCreateAlbum.albumName).append(".album").toString()
                )
                if (uriItemC != null) {
                    val uriString = uriItemC.uriString
                    dialogCreateAlbum.tempUri = uriString
                    val moved = MainUtil.moveFileOrUri(dialogCreateAlbum.appCtx, path, uriString)
                    this.isOk = moved
                    if (moved) {
                        addItem(dialogCreateAlbum.appCtx, uriItemC)
                        dialogCreateAlbum.lastImage = uriItemC.uriString
                    }
                }
            } catch (_ex: Throwable) {
                throw RuntimeException(_ex)
            }
        }
        public override fun onCancelled() {
            val dialogCreateAlbum = this.dialogRef!!.get() as DialogCreateAlbum?
            if (dialogCreateAlbum == null) {
                return
            }
            dialogCreateAlbum.runningZip = null
            dialogCreateAlbum.zipMonitor = null
            dialogCreateAlbum.dismiss()
        }
        public override fun onPostExecute() {
            val dialogCreateAlbum = this.dialogRef!!.get() as DialogCreateAlbum?
            if (dialogCreateAlbum != null) {
                if (!this.isOk) {
                    dialogCreateAlbum.zipCount = dialogCreateAlbum.zipRemaining
                }
                dialogCreateAlbum.runningZip = null
                dialogCreateAlbum.zipMonitor = null
                val linearLayout = dialogCreateAlbum.actionsNode
                if (linearLayout == null || linearLayout.getVisibility() == 0) {
                    return
                }
                dialogCreateAlbum.showForm()
                dialogCreateAlbum.scrollNode!!.setVisibility(0)
                dialogCreateAlbum.progressNode!!.setVisibility(0)
                dialogCreateAlbum.actionsNode!!.setVisibility(0)
                val nestedScrollView = dialogCreateAlbum.scrollNode
                if (nestedScrollView != null) {
                    nestedScrollView.post(dialogCreateAlbum.ScrollDownTask())
                }
                val zipRemaining = dialogCreateAlbum.zipRemaining
                var index = zipRemaining - dialogCreateAlbum.zipCount
                if (index < 0) {
                    index = 0
                }
                dialogCreateAlbum.zipTotalText!!.setText(zipRemaining.toString())
                dialogCreateAlbum.zipFailText!!.setText(dialogCreateAlbum.zipCount.toString())
                dialogCreateAlbum.zipSuccessText!!.setText(index.toString())
                if (dialogCreateAlbum.zipCount <= 0) {
                    dialogCreateAlbum.zipFailed = true
                    dialogCreateAlbum.zipFailText!!.setTextColor(if (AppState.darkUi) -328966 else -16777216)
                    dialogCreateAlbum.messageText!!.setActivated(false)
                    dialogCreateAlbum.messageText!!.setText(AppR.string.list)
                    dialogCreateAlbum.messageText!!.setTextColor(if (AppState.darkUi) -328966 else -14784824)
                    return
                }
                if (index == 0) {
                    dialogCreateAlbum.zipFailText!!.setTextColor(-769226)
                    dialogCreateAlbum.messageText!!.setActivated(false)
                    dialogCreateAlbum.messageText!!.setText(AppR.string.retry)
                    dialogCreateAlbum.messageText!!.setTextColor(if (AppState.darkUi) -328966 else -14784824)
                    return
                }
                dialogCreateAlbum.zipFailed = true
                dialogCreateAlbum.zipFailText!!.setTextColor(-769226)
                dialogCreateAlbum.messageText!!.setActivated(false)
                dialogCreateAlbum.messageText!!.setText(AppR.string.list)
                dialogCreateAlbum.messageText!!.setTextColor(if (AppState.darkUi) -328966 else -14784824)
                dialogCreateAlbum.retryButton!!.setVisibility(0)
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
            if (requestCode == RequestCode.CAMERA_OR_FILE) {
                var uri = this.cameraUri
                this.cameraUri = null
                if (resultCode == -1) {
                    val data = if (intent != null) intent.getData() else null
                    if (data != null) {
                        uri = data
                    }
                    MainUtil.keepUriPermission(this.appCtx, uri)
                    if (this.hostActivity != null) {
                        val context = this.appCtx
                        if (uri == null) {
                            showToast(context, AppR.string.invalid_path)
                            return true
                        }
                        val cachePath = MainUtil.newCacheFilePath(context)
                        this.iconPath = cachePath
                        if (TextUtils.isEmpty(cachePath)) {
                            showToast(this.appCtx, AppR.string.invalid_path)
                            return true
                        }
                        val cropIntent = Intent(this.appCtx, com.mycompany.app.ui.viewer.ImageCropActivity::class.java as Class<*>)
                        cropIntent.setData(uri)
                        cropIntent.putExtra("EXTRA_DST", this.iconPath)
                        cropIntent.putExtra("EXTRA_ICON", true)
                        this.hostActivity!!.launchExternalForResult(cropIntent, RequestCode.IMAGE_FILE)
                        return true
                    }
                }
            } else {
                if (requestCode != RequestCode.IMAGE_FILE) {
                    return false
                }
                val iconPath = this.iconPath
                this.iconPath = null
                if (resultCode == -1) {
                    if (TextUtils.isEmpty(iconPath) || !File(iconPath!!).exists()) {
                        showToast(this.appCtx, AppR.string.invalid_path)
                        return true
                    }
                    setIcon(iconPath, false)
                    return true
                }
            }
        } else if (resultCode == -1 && intent != null) {
            val pickedData = intent.getData()
            if (pickedData == null) {
                showToast(this.appCtx, AppR.string.invalid_path)
                return true
            }
            val treeUri = treeDocUri(pickedData)
            if (TextUtils.isEmpty(treeUri)) {
                showToast(this.appCtx, AppR.string.invalid_path)
                return true
            }
            if (!TextUtils.isEmpty(treeUri) && treeUri != PrefPath.picked) {
                PrefPath.picked = treeUri
                runInBackground(ApplyNameTask())
            }
            MainUtil.keepUriPermission(this.appCtx, pickedData)
            return true
        }
        return true
    }

    fun onPermissionResult(requestCode: Int, grantResults: IntArray?): Boolean {
        if (requestCode != 30) {
            return false
        }
        if (grantResults == null || grantResults.size <= 0 || grantResults[0] != 0) {
            return true
        }
        this.cameraUri = MainUtil.newMediaUri(RequestCode.CAMERA_OR_FILE, this.hostActivity, false)
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
        this.iconRow!!.setVisibility(8)
        this.iconNode!!.setVisibility(8)
        this.scrollNode!!.setVisibility(8)
        this.progressNode!!.setVisibility(8)
        this.actionsNode!!.setVisibility(8)
        this.resultNode!!.setVisibility(8)
        this.retryButton!!.setVisibility(8)
        this.messageText!!.setActivated(true)
        this.messageText!!.setText(AppR.string.cancel)
        this.messageText!!.setTextColor(if (AppState.darkUi) -328966 else -16777216)
    }

    fun setIcon(pickedIcon: String?, picked: Boolean) {
        val myRoundImage = this.iconImage
        if (myRoundImage == null) {
            return
        }
        if (picked) {
            this.pickedIcon = pickedIcon
            this.iconFile = null
        } else {
            this.iconFile = pickedIcon
        }
        myRoundImage.showIcon(-460552, AppR.drawable.outline_image_black_24)
        if (isSvgName(guessDownloadName(pickedIcon, null, null, true))) {
            this.pendingIconPath = pickedIcon
            runInBackground(CheckIconPathTask())
        } else {
            this.pendingIcon = pickedIcon
            runInBackground(GlideUrlForTask())
        }
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
                val dialogCreateAlbum = this@DialogCreateAlbum
                val appCompatTextView: UiNode? = if (dialogCreateAlbum.runningZip == null) null else dialogCreateAlbum.totalText
                if (dialogCreateAlbum.runningZip == null || appCompatTextView == null) {
                    return
                }
                appCompatTextView.setText(MainUtil.countLabel(dialogCreateAlbum.zipDone, dialogCreateAlbum.zipRemaining))
                dialogCreateAlbum.progressBar!!.progress = (dialogCreateAlbum.zipDone).toFloat()
                val zipCount = dialogCreateAlbum.zipCount
                val statusText = dialogCreateAlbum.statusText
                if (zipCount > 0) {
                    statusText!!.setText(zipCount.toString())
                    dialogCreateAlbum.statusText!!.setTextColor(-769226)
                } else {
                    statusText!!.setText("0")
                    dialogCreateAlbum.statusText!!.setTextColor(if (AppState.darkUi) -328966 else -16777216)
                }
            }
        }
        override fun onDone(path: String?, isOk: Boolean) {
            val dialogCreateAlbum = this@DialogCreateAlbum
            if (dialogCreateAlbum.zipRunning) {
                dialogCreateAlbum.zipRunning = false
                return
            }
            val doneCount = dialogCreateAlbum.zipDone + 1
            dialogCreateAlbum.zipDone = doneCount
            val zipRemaining = dialogCreateAlbum.zipRemaining
            if (doneCount > zipRemaining) {
                dialogCreateAlbum.zipDone = zipRemaining
            }
            if (!isOk) {
                val zipCount = dialogCreateAlbum.zipCount + 1
                dialogCreateAlbum.zipCount = zipCount
                if (zipCount > zipRemaining) {
                    dialogCreateAlbum.zipCount = zipRemaining
                }
            }
            val appCompatTextView = dialogCreateAlbum.totalText
            if (appCompatTextView == null) {
                return
            }
            appCompatTextView.post(ShowZipProgressStep())
        }
        override fun onProgress(entryName: String?, total: Long, done: Long) {
        }

        override val isCancelled: Boolean
            get() = this@DialogCreateAlbum.runningZip == null
        override fun onEntry(entryName: String?) {
        }
    }

    /** Suggested album name [str] (".album" while a folder is chosen); the location label. */
    fun applyName(newBaseName: String?) {
        if (appCtx == null) return
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
        val withExt = MainUtil.stripExtension(replaceExt(cleaned, ".album"))
        cleanName = withExt
        name = withExt ?: ""
    }

    /** Create album: checks the name, then starts verifying the images. */
    fun createAlbum() {
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
        val full = replaceExt(cleaned, ".album")
        if (TextUtils.isEmpty(full)) {
            showToast(appCtx, AppR.string.input_name)
            return
        }
        currentFocus?.let { closeKeyboard(appCtx, it) }
        albumName = MainUtil.stripExtension(MainUtil.safeFileName(full))
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
        val image = iconImage
        if (image != null) glide?.clear(image)
        glide = null
        iconImage?.release()
        iconImage = null
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
