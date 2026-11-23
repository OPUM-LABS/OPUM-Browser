package com.mycompany.app.dialog

import java.util.ArrayList
import com.mycompany.app.main.RequestCode
import ch.opumlabs.opumbrowser.R as AppR
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.webkit.URLUtil
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.compress.Compress
import com.mycompany.app.compress.Compress.Companion.isSvgName
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.db.DbUtil.updateById
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainDownSvc.ImageItem
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUri.splitNumber
import com.mycompany.app.main.MainUri.displayPath
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.findChild
import com.mycompany.app.main.MainUri.externalVolumes
import com.mycompany.app.main.MainUri.isContentUri
import com.mycompany.app.main.MainUriDoc
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.main.MainUtil.datePattern
import com.mycompany.app.main.MainUtil.timestampName
import com.mycompany.app.main.MainUtil.pickTreeFolder
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.editTextValue
import com.mycompany.app.main.MainUtil.uniqueName
import com.mycompany.app.main.MainUtil.guessDownloadName
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.closeKeyboard
import com.mycompany.app.main.MainUtil.trimSpaceDash
import com.mycompany.app.main.MainUtil.postRequestLayout
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.keepUriPermission
import com.mycompany.app.main.MainUtil.zeroPadFormat
import com.mycompany.app.main.MainUtil.signOf
import com.mycompany.app.pref.PrefPath
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefSet.putDownUri
import com.mycompany.app.pref.PrefSet.remove
import com.mycompany.app.pref.PrefUtil.setDetail
import com.mycompany.app.pref.PrefUtil.setSingleLine
import com.mycompany.app.view.GlideApp
import com.mycompany.app.view.GlideRequest
import com.mycompany.app.view.GlideRequests
import com.mycompany.app.web.WebViewActivity
import java.lang.ref.WeakReference
import java.net.URLDecoder
import java.util.Locale
import java.util.regex.Pattern
import android.widget.ImageView
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mycompany.app.ui.browser.ComposeBottomDialog
import androidx.webkit.URLUtilCompat

/**
 * Download the selected images of a page gallery ([arrayList] URLs, page title [pageTitleParam], referer [referer]): header with the
 * first image and the title, the image count, an optional "Subdirectory" (empty = not used; "Title" fills in the page
 * title), an optional file-name prefix (empty = real names; "Prefix" fills in the title's first word), the save
 * location (recent folders / Direct select, request 19 via [onFolderResult]) and Download (Cancel / Canceling). Naming and the
 * queueing ([DialogTask] → `MainApp.startDownloads`) are the old code. Compose now (on [ComposeBottomDialog]); same constructor,
 * shows itself.
 */
@Suppress("deprecation")
class DialogDownList(
    webViewActivity: WebViewActivity?,
    pageTitleParam: String?,
    arrayList: ArrayList<String?>?,
    referer: String?,
    dialogApplyListener: DialogApplyListener?,
) : ComposeBottomDialog(webViewActivity) {
    @JvmField
    var dateText: String? = null
    @JvmField
    var timeText: String? = null
    @JvmField
    var cleanPattern: Pattern? = null
    @JvmField
    var namePattern1: Pattern? = null
    @JvmField
    var namePattern2: Pattern? = null
    @JvmField
    var namePattern3: Pattern? = null
    @JvmField
    var namePattern4: Pattern? = null
    @JvmField
    var hostActivity: WebViewActivity? = webViewActivity
    @JvmField
    var appContext: Context? = context
    @JvmField
    var applyListener: DialogApplyListener? = dialogApplyListener
    @JvmField
    var referer: String? = referer
    @JvmField
    var pageTitle: String? = pageTitleParam
    @JvmField
    var downItems: ArrayList<String?>? = arrayList
    @JvmField
    var firstImage: ImageView? = null
    @JvmField
    var downTask: DialogTask? = null
    /** Cleaned page title (suggestion for the sub-folder). */
    @JvmField
    var titleDir: String? = null
    @JvmField
    var started: Boolean = false
    @JvmField
    var glideRequests: GlideRequests? = null
    @JvmField
    var folderList: ArrayList<Any?>? = null

    private var subDir by mutableStateOf("")
    private var prefix by mutableStateOf("")
    private var folderLabel by mutableStateOf<String?>(null)
    private var folderMenu by mutableStateOf(false)
    internal var busy by mutableStateOf(false)
    private var mainRes by mutableIntStateOf(AppR.string.download)
    private var mainEnabled by mutableStateOf(true)
    private val ui = android.os.Handler(android.os.Looper.getMainLooper())

    /** Background work. */
    private var executor: java.util.concurrent.ExecutorService? = null
    fun runAsync(runnable: Runnable?) {
        var ex = executor
        if (ex == null || ex.isShutdown() || ex.isTerminated()) {
            ex = MainApp.executor(context) ?: return
            executor = ex
        }
        try { ex.execute(runnable) } catch (unused: Exception) { }
    }

    init {
        runAsync(Runnable {
            val ctx = appContext ?: return@Runnable
            val title = fileTitle(pageTitle)
            val list = MainUri.externalVolumes(ctx)
            PrefPath.picked = MainUri.findChild(ctx, PrefPath.picked!!, list)
            val label = MainUri.displayPath(ctx, MainUri.storageVolume())
            ui.post {
                if (appContext == null) return@post
                titleDir = title
                folderList = list
                folderLabel = if (TextUtils.isEmpty(MainUri.storageVolume())) null else label
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
                        ImageView(ctx).also {
                            it.setScaleType(ImageView.ScaleType.CENTER_CROP)
                            it.setImageResource(AppR.drawable.outline_image_black_24)
                            firstImage = it
                            ui.post { loadFirst() }
                        }
                    },
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                )
                Text(
                    pageTitle ?: "", maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
                    Text(stringResource(AppR.string.image), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Text((downItems?.size ?: 0).toString(), style = MaterialTheme.typography.bodyLarge)
                }
                Field(stringResource(AppR.string.sub_dir), stringResource(AppR.string.not_used), subDir, { subDir = it }, stringResource(AppR.string.title)) {
                    subDir = titleDir ?: ""
                }
                Field(stringResource(AppR.string.file_name), stringResource(AppR.string.real_name), prefix, { prefix = it }, stringResource(AppR.string.prefix)) {
                    var first = titleDir ?: ""
                    val space = first.indexOf(' ')
                    if (space > 0) first = first.substring(0, space).trim { it <= ' ' }
                    prefix = first
                }
                Box {
                    ListItem(
                        headlineContent = { Text(stringResource(AppR.string.down_location)) },
                        supportingContent = { Text(folderLabel ?: stringResource(AppR.string.folder_not_selected), maxLines = 2) },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        modifier = Modifier.clickable(enabled = !busy) { openFolders() }.padding(horizontal = 8.dp),
                    )
                    com.mycompany.app.ui.components.BubbleMenu(expanded = folderMenu, onDismiss = { folderMenu = false }) {
                        val ctx = appContext
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
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp))
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { if (downTask != null) cancelDown() else start() }, enabled = mainEnabled) { Text(stringResource(mainRes)) }
            }
        }
        show()
    }

    @androidx.compose.runtime.Composable
    private fun Field(label: String, hint: String, value: String, onChange: (String) -> Unit, chip: String, onChip: () -> Unit) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = value,
                onValueChange = onChange,
                label = { Text(label) },
                placeholder = { Text(hint) },
                singleLine = true,
                enabled = !busy,
                modifier = Modifier.weight(1f),
            )
            if (value.isEmpty()) {
                AssistChip(onClick = onChip, label = { Text(chip) }, enabled = !busy, modifier = Modifier.padding(start = 8.dp))
            }
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
        runAsync(Runnable {
            val ctx = appContext ?: return@Runnable
            val dir = MainUri.storageVolume()
            PrefSet.putDownUri(ctx, dir)
            val label = MainUri.displayPath(ctx, dir)
            ui.post {
                folderLabel = label
            }
        })
    }

    /** First image as the header (SVG through `PictureDrawable`). */
    private fun loadFirst() {
        val list = downItems
        if (list == null || list.isEmpty()) return
        val url = list[0]
        val svg = isSvgName(guessDownloadName(url, null, null, true))
        runAsync(Runnable {
            val act = hostActivity ?: return@Runnable
            if (glideRequests == null) glideRequests = GlideApp.with(act)
            ui.post {
                val requests = glideRequests ?: return@post
                val view = firstImage ?: return@post
                if (svg) {
                    val src: Any? = if (URLUtil.isNetworkUrl(url)) MainUtil.glideUrlFor(appContext, url!!, referer) else url
                    ((requests.`as`(PictureDrawable::class.java) as GlideRequest<*>).load(src)).into(view)
                } else if (URLUtil.isNetworkUrl(url)) {
                    (requests.load(MainUtil.glideUrlFor(appContext, url!!, referer)).diskCacheStrategy(DiskCacheStrategy.NONE) as RequestBuilder<*>).into(view)
                } else {
                    (requests.load(url).diskCacheStrategy(DiskCacheStrategy.NONE) as RequestBuilder<*>).into(view)
                }
            }
        })
    }

    class DialogTask(
        dialogDownList: DialogDownList?,
        url: String?,
        referer: String?,
        arrayList: ArrayList<String?>?
    ) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>
        @JvmField
        var url: String? = null
        @JvmField
        var referer: String? = null
        @JvmField
        var items: ArrayList<String?>? = null

        init {
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogDownList)
            this.dialogRef = weakReference
            val dialog = weakReference.get() as DialogDownList?
            if (dialog != null) {
                this.url = url
                this.referer = referer
                this.items = arrayList
                dialog.busy = true
            }
        }

        public override fun doInBackground() {
            var keptUrls: ArrayList<String?>?
            var itemTitle: String?
            var fileName: String?
            var dotIndex: Int
            var baseTitle: String?
            var nameStart: Int
            var extPart: String?
            var dialogTask = this
            val dialogDownList = dialogTask.dialogRef.get() as DialogDownList?
            var urls: ArrayList<String?>? = if (dialogDownList == null || dialogTask.cancelled) null else dialogTask.items
            if (dialogDownList == null || dialogTask.cancelled || urls == null) {
                return
            }
            var volume = MainUri.storageVolume()
            val isContent = MainUri.isContentUri(volume)
            var dirName = dialogTask.url
            if (isContent) {
                val childUri = if (!TextUtils.isEmpty(dirName)) MainUriDoc.findChild(
                    dialogDownList.appContext,
                    volume,
                    dirName!!
                ) else null
                if (!TextUtils.isEmpty(childUri)) {
                    volume = childUri
                }
                dirName = MainUri.treeDisplayPath(dialogDownList.appContext!!, volume)
            } else if (TextUtils.isEmpty(dirName)) {
                dirName = null
            }
            val referer = dialogTask.referer
            val refererEmpty = TextUtils.isEmpty(referer)
            val numberFormat = if (!refererEmpty) MainUtil.zeroPadFormat(MainUtil.signOf(urls.size)) else null
            val size = urls.size
            var imageItems: ArrayList<ImageItem?>? = null
            var number = 1
            var usedNames: ArrayList<String?>? = null
            var index = 0
            while (index < size) {
                val obj: Any? = urls!!.get(index)
                index++
                val url = obj as String?
                if (dialogTask.cancelled) {
                    return
                }
                if (refererEmpty) {
                    if (TextUtils.isEmpty(url)) {
                        keptUrls = urls
                        itemTitle = null
                    } else {
                        var guessedName = dialogDownList.cleanTitle(URLUtilCompat.guessFileName(url!!, null, "image/*"))
                        if (TextUtils.isEmpty(guessedName)) {
                            guessedName = timestampName(null, null)
                            if (TextUtils.isEmpty(guessedName)) {
                                keptUrls = urls
                                itemTitle = null
                            } else {
                                dotIndex = guessedName.lastIndexOf(46.toChar())
                                baseTitle = baseName(guessedName)
                                itemTitle = dialogDownList.fileTitle(baseTitle)
                                if (TextUtils.isEmpty(itemTitle)) {
                                    keptUrls = urls
                                    itemTitle = null
                                } else if (dotIndex > 0) {
                                    keptUrls = urls
                                    if (dotIndex < guessedName.length - 1) {
                                        val sbR = StringBuilder().append(itemTitle)
                                        sbR.append(guessedName.substring(dotIndex))
                                        itemTitle = sbR.toString()
                                    }
                                } else {
                                    keptUrls = urls
                                }
                            }
                        } else {
                            dotIndex = guessedName!!.lastIndexOf(46.toChar())
                            // the title is built from the name without its extension, which is appended again below
                            // (keeping it here gave "logo.png.png")
                            baseTitle = baseName(guessedName)
                            itemTitle = dialogDownList.fileTitle(baseTitle)
                            if (TextUtils.isEmpty(itemTitle)) {
                                keptUrls = urls
                                itemTitle = null
                            } else if (dotIndex > 0) {
                                keptUrls = urls
                                if (dotIndex < guessedName.length - 1) {
                                    val sbR = StringBuilder().append(itemTitle)
                                    sbR.append(guessedName.substring(dotIndex))
                                    itemTitle = sbR.toString()
                                }
                            } else {
                                keptUrls = urls
                            }
                        }
                    }
                    if (TextUtils.isEmpty(itemTitle)) {
                        if (dialogDownList.dateText == null) {
                            dialogDownList.dateText = datePattern("yyyyMMdd")
                        }
                        var timeText = dialogDownList.timeText
                        if (timeText == null) {
                            timeText = datePattern("kkmmss")
                            dialogDownList.timeText = timeText
                        }
                        fileName = timestampName(dialogDownList.dateText, timeText)
                    } else {
                        fileName = itemTitle
                    }
                } else {
                    val nameBuilder = StringBuilder().append(referer).append("_")
                    if (TextUtils.isEmpty(numberFormat)) {
                        nameBuilder.append(number)
                    } else {
                        nameBuilder.append(String.format(Locale.US, numberFormat!!, number))
                    }
                    val ext = fileExtFromName(url, true)
                    if (!TextUtils.isEmpty(ext)) {
                        nameBuilder.append(".")
                        nameBuilder.append(ext)
                    }
                    fileName = nameBuilder.toString()
                    number++
                    keptUrls = urls
                }
                val imageItem = ImageItem()
                imageItem.url = url
                imageItem.referer = dialogDownList.referer
                imageItem.dirPath = volume
                imageItem.dirName = dirName
                if (Compress.isImageName(fileName!!, true, true)) {
                    nameStart = 0
                } else {
                    imageItem.isGif = true
                    val gifDot = fileName.lastIndexOf(46.toChar())
                    if (gifDot <= 0 || gifDot >= fileName.length) {
                        nameStart = 0
                        fileName = fileName + ".jpg"
                    } else {
                        nameStart = 0
                        fileName = fileName.substring(0, gifDot) + ".jpg"
                    }
                }
                if (refererEmpty) {
                    val nameDot = fileName.lastIndexOf(46.toChar())
                    if (nameDot != -1) {
                        val namePart = fileName.substring(nameStart, nameDot)
                        extPart = fileName.substring(nameDot)
                        fileName = namePart
                    } else {
                        extPart = null
                    }
                    val uniqueBase = uniqueName(usedNames, fileName)
                    val finalName =
                        if (!TextUtils.isEmpty(extPart)) "$uniqueBase$extPart" else uniqueBase
                    if (usedNames == null) {
                        usedNames = ArrayList()
                    }
                    usedNames.add(uniqueBase)
                    fileName = finalName
                }
                imageItem.fileName = fileName
                if (imageItems == null) {
                    imageItems = ArrayList()
                }
                imageItems.add(imageItem)
                dialogTask = this
                urls = keptUrls
            }
            val mainApp: MainApp? = if (imageItems == null || imageItems.isEmpty()) null else MainApp.from(dialogDownList.appContext)
            if (imageItems == null || imageItems.isEmpty() || mainApp == null) {
                return
            }
            mainApp.startDownloads(imageItems)
        }

        public override fun onCancelled() {
            val dialogDownList = this.dialogRef.get() as DialogDownList?
            if (dialogDownList == null) {
                return
            }
            dialogDownList.downTask = null
            dialogDownList.dismiss()
        }

        public override fun onPostExecute() {
            val dialogDownList = this.dialogRef.get() as DialogDownList?
            if (dialogDownList == null) {
                return
            }
            dialogDownList.downTask = null
            dialogDownList.dismiss()
        }
    }

    fun fileTitle(name: String?): String {
        if (!TextUtils.isEmpty(name)) {
            val cleaned = cleanTitle(name!!)
            var noPictographs: String? = null
            if (!TextUtils.isEmpty(cleaned)) {
                var patternCompile = this.namePattern1
                if (patternCompile == null) {
                    patternCompile = Pattern.compile("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]&&[^()-]+")
                    this.namePattern1 = patternCompile
                }
                val noEmoji = patternCompile.matcher(cleaned!!).replaceAll("")
                if (!TextUtils.isEmpty(noEmoji)) {
                    var emoticonPattern = this.namePattern2
                    if (emoticonPattern == null) {
                        emoticonPattern = Pattern.compile("\\p{InEmoticons}&&[^()-]+")
                        this.namePattern2 = emoticonPattern
                    }
                    val noEmoticons = emoticonPattern.matcher(noEmoji).replaceAll("")
                    if (!TextUtils.isEmpty(noEmoticons)) {
                        var symbolPattern = this.namePattern3
                        if (symbolPattern == null) {
                            symbolPattern = Pattern.compile("\\p{So}&&[^()-]+")
                            this.namePattern3 = symbolPattern
                        }
                        val noSymbols = symbolPattern.matcher(noEmoticons).replaceAll("")
                        if (!TextUtils.isEmpty(noSymbols)) {
                            var pictographPattern = this.namePattern4
                            if (pictographPattern == null) {
                                pictographPattern =
                                    Pattern.compile("\\p{InMiscellaneousSymbolsAndPictographs}&&[^()-]+")
                                this.namePattern4 = pictographPattern
                            }
                            noPictographs = pictographPattern.matcher(noSymbols).replaceAll("")
                        }
                    }
                }
            }
            val truncated = MainUtil.truncateUtf8(170, noPictographs!!)
            if (!TextUtils.isEmpty(truncated)) {
                return truncated!!
            }
        }
        return "Image"
    }

    fun cleanTitle(dir: String?): String? {
        var dir = dir
        if (TextUtils.isEmpty(dir)) {
            return dir
        }
        try {
            var patternCompile = this.cleanPattern
            if (patternCompile == null) {
                patternCompile = Pattern.compile("[\\\\/:*?\"<>|]")
                this.cleanPattern = patternCompile
            }
            dir = URLDecoder.decode(patternCompile.matcher(dir!!).replaceAll("-"), "UTF-8")
            return trimSpaceDash(dir)!!
        } catch (unused: Exception) {
            return dir
        }
    }

    /** Folder picker result (request code 19). */
    fun onFolderResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        if (requestCode != RequestCode.SAVE_LOCATION) return false
        if (resultCode == -1 && intent != null) {
            val data = intent.getData()
            if (data == null) {
                showToast(appContext, AppR.string.invalid_path)
                return true
            }
            val path = treeDocUri(data)
            if (TextUtils.isEmpty(path)) {
                showToast(appContext, AppR.string.invalid_path)
                return true
            }
            setFolder(path)
            MainUtil.keepUriPermission(appContext, data)
        }
        return true
    }

    /** Cancel: stops the queueing ("Canceling"), or closes. */
    fun cancelDown() {
        if (downTask == null) {
            dismiss()
            return
        }
        mainEnabled = false
        mainRes = AppR.string.canceling
        downTask?.cancelled = true
        downTask = null
    }

    /** Download: checks the names and queues the images. */
    fun startDown() {
        val list = downItems
        if (list == null || list.isEmpty() || appContext == null) return
        if (TextUtils.isEmpty(MainUri.storageVolume())) {
            showToast(appContext, AppR.string.select_dir)
            return
        }
        var dir: String? = subDir.trim { it <= ' ' }
        if (!TextUtils.isEmpty(dir)) {
            if (dir!!.toByteArray().size > 200) {
                showToast(appContext, AppR.string.long_name)
                return
            }
            dir = cleanTitle(dir)
        }
        var pre: String? = prefix.trim { it <= ' ' }
        if (!TextUtils.isEmpty(pre)) {
            if (pre!!.toByteArray().size > 200) {
                showToast(appContext, AppR.string.long_name)
                return
            }
            pre = cleanTitle(pre)
        }
        applyListener?.onApply()
        setCanceledOnTouchOutside(false)
        mainRes = AppR.string.cancel
        downTask?.cancelled = true
        downTask = null
        ui.post {
            val task = DialogTask(this, dir, pre, list)
            downTask = task
            task.start(appContext)
        }
    }

    private fun start() {
        if (started) return
        started = true
        startDown()
        started = false
    }

    override fun cancel() {
        cancelDown()
    }

    override fun canCancelFromScrim(): Boolean = !busy

    override fun dismiss() {
        if (appContext == null) {
            super.dismiss()
            return
        }
        downTask?.cancelled = true
        downTask = null
        val view = firstImage
        if (view != null) glideRequests?.clear(view)
        glideRequests = null
        firstImage = null
        hostActivity = null
        appContext = null
        applyListener = null
        downItems = null
        folderList = null
        super.dismiss()
    }

    companion object {

        /** [fileName] without its extension; the whole name when it has none (no dot, or only a leading dot). */
        @JvmStatic
        fun baseName(fileName: String): String {
            val dot = fileName.lastIndexOf('.')
            return if (dot > 0) fileName.substring(0, dot) else fileName
        }
    }
}
