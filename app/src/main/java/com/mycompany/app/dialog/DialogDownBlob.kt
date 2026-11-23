package com.mycompany.app.dialog

import com.mycompany.app.main.MainUri
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.db.book.DbBookDown
import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.View
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.core.app.NotificationCompat
import com.mycompany.app.data.DataUtil
import com.mycompany.app.data.DataUtil.addUri
import com.mycompany.app.db.DbUtil.updateById
import com.mycompany.app.db.book.DbBookDown.Companion.hasPath
import com.mycompany.app.db.book.DbBookDown.Companion.insertItem
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainDownSvc
import com.mycompany.app.main.MainDownSvc.DownItem
import com.mycompany.app.main.MainLauncher
import com.mycompany.app.main.MainNative
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUri.NumItem
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.splitNumber
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.externalVolumes
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.main.MainUtil.archiveIconFor
import com.mycompany.app.main.MainUtil.archiveKindOf
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.isBitmapOk
import com.mycompany.app.main.MainUtil.getBlobJs
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.mimeForUrl
import com.mycompany.app.main.MainUtil.resourceBitmap
import com.mycompany.app.pref.PrefPath
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSet.remove
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.view.MyDialogBottom
import java.io.OutputStream

import com.mycompany.app.ui.browser.ComposeBottomDialog
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Saves a `blob:` download ([downItem]) from the page [webView]: the page sends the data in up to 2 MB chunks through
 * the JS bridge ([onChunk]); a background runnable writes them in order and updates "Total n / m" with a progress bar; at the
 * end the download is registered (`DbBookDown`, media scan, a "download complete" notification) and
 * [DialogBlobListener] hears the id, path and mime type. Cancel aborts the transfer (deletes the partial file);
 * an existing file name gets a time suffix. The whole transfer code is the old one; only the views became Compose
 * state. Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogDownBlob(
    activity: Activity?,
    webView: WebView?,
    downItem: DownItem?,
    dialogBlobListener: DialogBlobListener?,
) : ComposeBottomDialog(activity) {
    @JvmField
    var appContext: Context? = context
    @JvmField
    var blobListener: DialogBlobListener? = dialogBlobListener
    @JvmField
    var webViewRef: WebView? = webView
    @JvmField
    var downItemRef: DownItem? = downItem
    @JvmField
    var filePath: String? = downItem!!.path
    @JvmField
    var fileName: String? = downItem!!.uriItem!!.name
    @JvmField
    var downId: Long = 0
    @JvmField
    var outStream: OutputStream? = null
    @JvmField
    var blobChunks: Array<BlobItem?>? = null
    @JvmField
    var chunksReceived: Int = 0
    @JvmField
    var chunkSize: Int = 0
    @JvmField
    var totalSize: Long = 0
    @JvmField
    var chunkCount: Int = 0
    @JvmField
    var chunksWritten: Int = 0
    @JvmField
    var finished: Boolean = false
    @JvmField
    var stopped: Boolean = false

    @JvmField
    val mainHandler: Handler = Handler(Looper.getMainLooper())

    private var nameText by mutableStateOf(fileName ?: "")
    private var totalVisible by mutableStateOf(false)
    private var totalText by mutableStateOf("")
    private var progressMax by mutableIntStateOf(1)
    private var progressValue by mutableFloatStateOf(0f)
    private var canceling by mutableStateOf(false)

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

    internal inner class CancelledToastTask : Runnable {
        override fun run() {
            val dialogDownBlob = this@DialogDownBlob
            if (dialogDownBlob.appContext == null) {
                return
            }
            showToast(dialogDownBlob.appContext, AppR.string.cancelled)
            dialogDownBlob.dismiss()
        }
    }

    internal inner class ShowProgressTask(@JvmField val outer: DialogDownBlob,
                                          @JvmField val path: String?
    ) : Runnable {
        override fun run() {
            val dialogDownBlob = this.outer
            if (dialogDownBlob.blobListener == null) {
                return
            }
            dialogDownBlob.showProgress()
            dialogDownBlob.blobListener!!.onSaved(dialogDownBlob.downId, dialogDownBlob.filePath, this.path)
            dialogDownBlob.dismiss()
        }
    }

    internal inner class HasPathTask : Runnable {

        internal inner class ExistFileToastTask : Runnable {
            override fun run() {
                val dialogDownBlob = this@DialogDownBlob
                if (dialogDownBlob.appContext == null) {
                    return
                }
                showToast(dialogDownBlob.appContext, AppR.string.exist_file)
                dialogDownBlob.dismiss()
            }
        }

        override fun run() {
            var uriItemC: UriItem?
            val dialogDownBlob = this@DialogDownBlob
            val downItem = dialogDownBlob.downItemRef
            var arrayList: ArrayList<Any?>? = null
            val uriItem: UriItem? = if (downItem != null) downItem.uriItem else null
            if (downItem != null && uriItem != null && DbBookDown.hasPath(dialogDownBlob.appContext, uriItem.uriString, SyncSettings.secretMode.value)) {
                val context = dialogDownBlob.appContext
                val name = uriItem.name
                val secretMode = SyncSettings.secretMode.value
                val numItemG: NumItem? = if (TextUtils.isEmpty(name)) null else MainUri.splitNumber(name)
                if (TextUtils.isEmpty(name) || numItemG == null) {
                    uriItemC = null
                } else {
                    uriItemC = MainUri.createUri(
                        context,
                        PrefPath.downFolder(context),
                        null,
                        numItemG.base + "_" + System.currentTimeMillis() + numItemG.suffix
                    )
                    if (uriItemC == null || DbBookDown.hasPath(context, uriItemC.uriString, secretMode)) {
                        uriItemC = null
                    }
                }
                if (uriItemC == null) {
                    val handler = dialogDownBlob.mainHandler
                    handler.post(ExistFileToastTask())
                    return
                    return
                }
                val downItemRef = dialogDownBlob.downItemRef
                downItemRef!!.uriItem = uriItemC
                val uriString = uriItemC.uriString
                downItemRef.path = uriString
                dialogDownBlob.filePath = uriString
                dialogDownBlob.fileName = uriItemC.name
            }
            if (dialogDownBlob.downItemRef != null) {
                try {
                    dialogDownBlob.outStream = MainUtil.openOutput(dialogDownBlob.appContext!!, dialogDownBlob.filePath, false)
                } catch (unused: Exception) {
                }
                val webView = dialogDownBlob.webViewRef
                val url = dialogDownBlob.downItemRef!!.url
                val isBlob = !TextUtils.isEmpty(url) && url!!.startsWith("blob:")
                if (!TextUtils.isEmpty(url) && url!!.startsWith("blob:") && isBlob) {
                    if (isBlob) {
                        if (!partsReady(15, MainNative.blobParts)) {
                            val blobParts: ArrayList<Any?> = ArrayList<Any?>()
                            var index = 0
                            while (true) {
                                if (index >= 15) {
                                    MainNative.blobParts = blobParts
                                    arrayList = blobParts
                                    break
                                } else {
                                    val blobJs = MainUtil.getBlobJs(index)
                                    if (TextUtils.isEmpty(blobJs)) {
                                        break
                                    }
                                    blobParts.add(blobJs)
                                    index++
                                }
                            }
                        } else {
                            arrayList = MainNative.blobParts
                        }
                    }
                    if (arrayList == null || arrayList.size != 15) {
                        return
                    }
                    val sb = StringBuilder()
                    for (index in 0..14) {
                        val chunk = arrayList.get(index) as String?
                        if (TextUtils.isEmpty(chunk)) {
                            return
                        }
                        sb.append(chunk)
                        if (index == 0) {
                            sb.append(url)
                        }
                    }
                    evalJs(webView, sb.toString(), true)
                }
            }
        }
    }

    internal inner class DeleteUriTask : Runnable {
        override fun run() {
            var downType: Int = 0
            var bytes: ByteArray?
            val dialogDownBlob = this@DialogDownBlob
            if (dialogDownBlob.finished) {
                return
            }
            dialogDownBlob.finished = true
            while (dialogDownBlob.chunksWritten < dialogDownBlob.chunksReceived) {
                val blobItemArr: Array<BlobItem?>? = if (!dialogDownBlob.stopped) dialogDownBlob.blobChunks else null
                if (!dialogDownBlob.stopped && blobItemArr != null) {
                    for (blobItem in blobItemArr) {
                        if (blobItem != null && blobItem.index != -1) {
                            val dataNow = blobItem.data
                            if (TextUtils.isEmpty(dataNow)) {
                                break
                            }
                            try {
                                bytes = dataNow!!.toByteArray(charset("windows-1252"))
                            } catch (unused: Exception) {
                                bytes = null
                            }
                            if (bytes == null) {
                                break
                            }
                            val index = blobItem.index
                            blobItem.index = -1
                            blobItem.data = null
                            try {
                                dialogDownBlob.outStream!!.write(bytes, 0, index)
                                dialogDownBlob.chunksWritten++
                            } catch (unused2: Exception) {
                            }
                            val handler = dialogDownBlob.mainHandler
                            handler.post(dialogDownBlob.ShowProgressNowTask())
                        }
                    }
                }
                if (dialogDownBlob.stopped) {
                    break
                }
            }
            if (dialogDownBlob.stopped) {
                val outputStream = dialogDownBlob.outStream
                if (outputStream != null) {
                    try {
                        outputStream.close()
                    } catch (unused3: Exception) {
                    }
                    dialogDownBlob.outStream = null
                }
                val downItem = dialogDownBlob.downItemRef
                dialogDownBlob.downItemRef = null
                if (downItem != null) {
                    MainUtil.deleteUri(dialogDownBlob.appContext!!, dialogDownBlob.filePath)
                    val handler = dialogDownBlob.mainHandler
                    handler.post(dialogDownBlob.CancelledToastTask())
                }
            } else if (dialogDownBlob.chunksWritten >= dialogDownBlob.chunkCount) {
                val outputStream = dialogDownBlob.outStream
                if (outputStream != null) {
                    try {
                        outputStream.close()
                    } catch (unused4: Exception) {
                    }
                    dialogDownBlob.outStream = null
                }
                val downItem = dialogDownBlob.downItemRef
                dialogDownBlob.downItemRef = null
                if (downItem != null) {
                    val uriItemK = MainUtil.gifUriFor(dialogDownBlob.appContext!!, downItem)
                    if (uriItemK != null) {
                        downItem.uriItem = uriItemK
                        val uriString = uriItemK.uriString
                        downItem.path = uriString
                        dialogDownBlob.filePath = uriString
                        dialogDownBlob.fileName = uriItemK.name
                    }
                    downItem.status = 3
                    val archiveIcon = archiveIconFor(dialogDownBlob.fileName)
                    downItem.fileType = archiveIcon
                    val totalSize = dialogDownBlob.totalSize
                    downItem.totalSize = totalSize
                    downItem.readSize = totalSize
                    dialogDownBlob.downId = DbBookDown.insertItem(
                        dialogDownBlob.appContext,
                        downItem.status,
                        archiveIcon,
                        downItem.url,
                        downItem.referer,
                        downItem.uriItem,
                        totalSize,
                        totalSize,
                        false,
                        SyncSettings.secretMode.value,
                        SecretSettings.secretDown.value,
                        false
                    )
                    val fileType = downItem.fileType
                    if (fileType != 4 && fileType != 5 && fileType != 6 && ((DataUtil.fileKind(dialogDownBlob.fileName)
                            .also { downType = it }) == 1 || downType == 2 || downType == 3)
                    ) {
                        addUri(dialogDownBlob.appContext, downType, downItem.uriItem)
                    }
                    val mime = MainUtil.mimeForUrl(dialogDownBlob.fileName)
                    val context = dialogDownBlob.appContext
                    if (context != null) {
                        val sb = StringBuilder()
                        MainDownSvc.appendSize(sb, dialogDownBlob.totalSize)
                        sb.append("  ")
                        sb.append(dialogDownBlob.fileName)
                        val title = sb.toString()
                        val intent = Intent(context, MainLauncher::class.java as Class<*>)
                        intent.putExtra("EXTRA_NOTI", dialogDownBlob.filePath)
                        if (!TextUtils.isEmpty(mime)) {
                            intent.putExtra("EXTRA_TYPE", mime)
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        val activity =
                            PendingIntent.getActivity(context, MainDownSvc.nextNotiId(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                        val builder = NotificationCompat.Builder(context, "Download")
                        builder.setSmallIcon(AppR.drawable.outline_download_done_white_24)
                        builder.setContentTitle(title)
                        builder.setContentText(
                            context.getString(AppR.string.down_complete))
                        builder.setProgress(0, 0, false)
                        builder.setContentIntent(activity)
                        builder.setPriority(1)
                        builder.setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_DOWN")
                        val bitmapY = MainUtil.resourceBitmap(context, AppR.drawable.baseline_offline_pin_gray_20)
                        if (MainUtil.isBitmapOk(bitmapY)) {
                            builder.setLargeIcon(bitmapY)
                        }
                        val notifyId = ((dialogDownBlob.downId % 2147483635).toInt()) + 1
                        val notificationB = builder.build()
                        notificationB.flags = (notificationB.flags and (-33)) or 16
                        val notificationManagerP = MainApp.notifications(context)
                        if (notificationManagerP != null) {
                            notificationManagerP.notify(notifyId, notificationB)
                        }
                    }
                    val handler = dialogDownBlob.mainHandler
                    handler.post(ShowProgressTask(dialogDownBlob, mime))
                }
            }
            dialogDownBlob.finished = false
        }
    }

    internal inner class ShowProgressNowTask : Runnable {
        override fun run() {
            this@DialogDownBlob.showProgress()
        }
    }

    class BlobItem {
        @JvmField
        var data: String? = null
        @JvmField
        var index: Int = 0
    }

    interface DialogBlobListener {
        fun onSaved(id: Long, path: String?, mimeType: String?)
    }

    init {
        var icon = archiveKindOf(fileName)
        if (icon == AppR.drawable.outline_draft_black_24) icon = AppR.drawable.outline_public_black_24
        val iconRes = icon
        setSheet {
            Row(
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                androidx.compose.foundation.layout.Box(
                    Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(painterResource(iconRes), contentDescription = null, modifier = Modifier.size(24.dp))
                }
                Text(
                    nameText, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 16.dp),
                )
            }
            if (totalVisible) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    Text(stringResource(AppR.string.total), modifier = Modifier.weight(1f))
                    Text(totalText)
                }
                LinearProgressIndicator(
                    progress = { (progressValue / progressMax.coerceAtLeast(1)).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                )
            } else {
                LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp))
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { stopBlob() }, enabled = !canceling) {
                    Text(stringResource(if (canceling) AppR.string.canceling else AppR.string.cancel))
                }
            }
        }
        show()
        setCanceledOnTouchOutside(false)
        mainHandler.post { if (appContext != null) runAsync(HasPathTask()) }
    }

    /** Cancel: aborts the page transfer; while writing, waits for the writer ("Canceling"), otherwise closes. */
    fun stopBlob() {
        if (!stopped) {
            stopped = true
            evalJs(
                webViewRef,
                "var sbblb=document.getElementById('sb_down_blob');if(sbblb){document.body.removeChild(sbblb);}if(xhr){xhr.abort();xhr=null;}",
                true
            )
        }
        if (appContext == null || !finished) {
            dismiss()
            return
        }
        canceling = true
    }

    fun onChunk(index: Int, dataLength: Int, chunkIndex: Int, chunkData: String?) {
        if (this.appContext != null) {
            this.chunksReceived++
            if (this.blobChunks == null) {
                val chunkSize = if (2097152 > dataLength) dataLength else 2097152
                var newChunkCount = dataLength / chunkSize
                if (dataLength % chunkSize != 0) {
                    newChunkCount++
                }
                this.chunkSize = chunkSize
                this.totalSize = dataLength.toLong()
                this.chunkCount = newChunkCount
                this.blobChunks = arrayOfNulls<BlobItem>(newChunkCount)
                for (index3 in 0..<newChunkCount) {
                    this.blobChunks!![index3] = BlobItem()
                }
                this.mainHandler.post {
                    totalText = "0 / " + totalSize
                    progressMax = chunkCount
                    totalVisible = true
                }
            }
            val blobItem = this.blobChunks!![chunkIndex]!!
            blobItem.data = chunkData
            blobItem.index = index
            if (this.finished) {
                return
            }
            runAsync(DeleteUriTask())
        }
    }

    /** Progress text and bar from the written chunk count. */
    fun showProgress() {
        if (appContext == null) return
        var chunksWrittenNow = chunksWritten
        if (chunksWrittenNow > chunkCount) {
            chunksWritten = chunkCount
            chunksWrittenNow = chunkCount
        }
        var written = (chunksWrittenNow * chunkSize).toLong()
        if (written > totalSize) written = totalSize
        val sb = StringBuilder()
        MainDownSvc.appendSize(sb, written)
        sb.append(" / ")
        MainDownSvc.appendSize(sb, totalSize)
        totalText = sb.toString()
        progressValue = chunksWritten.toFloat()
    }

    override fun cancel() {
        stopBlob()
    }

    override fun canCancelFromScrim(): Boolean = false

    override fun dismiss() {
        val out = outStream
        outStream = null
        try { out?.close() } catch (unused: Exception) { }
        appContext = null
        blobListener = null
        webViewRef = null
        downItemRef = null
        blobChunks = null
        super.dismiss()
    }

    companion object {
    }
}
