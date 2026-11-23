package com.mycompany.app.dialog

import com.mycompany.app.main.MainUtil
import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.content.Context
import android.content.DialogInterface
import android.text.TextUtils
import android.webkit.URLUtil
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.dialog.DialogPreview.PreviewListener
import com.mycompany.app.dialog.DialogVideoList.VideoListListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainDownAdapter
import com.mycompany.app.main.MainDownAdapter.DownListItem
import com.mycompany.app.main.MainDownAdapter.MainDownListener
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.externalVolumes
import com.mycompany.app.main.MainUri.isFile
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.openHttpConnection
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.mimeForExt
import com.mycompany.app.pref.PrefAlbum.Companion.get
import com.mycompany.app.pref.PrefFloat.Companion.get
import com.mycompany.app.pref.PrefImage.Companion.get
import com.mycompany.app.pref.PrefList.Companion.get
import com.mycompany.app.pref.PrefMain.Companion.get
import com.mycompany.app.pref.PrefPdf.Companion.get
import com.mycompany.app.pref.PrefRead.Companion.get
import com.mycompany.app.pref.PrefSecret.Companion.get
import com.mycompany.app.pref.PrefSync.Companion.get
import com.mycompany.app.pref.PrefTts.Companion.get
import com.mycompany.app.pref.PrefVideo.Companion.get
import com.mycompany.app.pref.PrefWeb.Companion.get
import com.mycompany.app.pref.PrefZone.Companion.get
import com.mycompany.app.pref.PrefZtri.Companion.get
import com.mycompany.app.pref.PrefZtwo.Companion.get
import ch.opumlabs.opumbrowser.R
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.ref.WeakReference
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Font download list for a font page ([pageUrl] the font list URL, [referer] referer): reads the list in the background
 * ([DialogTask]; "filename" + "url" pairs, TTF / OTF), then shows "Download" and the fonts (number, name, TTF / OTF;
 * tap = download through [VideoListListener.download]; info = the link details in [DialogDownLink], which can download or
 * preview). A single font downloads at once; an empty list says "Invalid URL" and closes. Compose now (on
 * [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogDownFont(
    mainActivity: MainActivity?,
    pageUrl: String?,
    referer: String?,
    videoListListener: VideoListListener?,
) : ComposeBottomDialog(mainActivity) {
    private val ui = android.os.Handler(android.os.Looper.getMainLooper())
    @JvmField val mainHandler: android.os.Handler? = ui

    @JvmField var hostActivity: MainActivity? = mainActivity
    @JvmField var appContext: Context? = context
    @JvmField var videoListener: VideoListListener? = videoListListener
    @JvmField var pageUrl: String? = pageUrl
    @JvmField var referer: String? = referer
    @JvmField var fontItems: ArrayList<Any?>? = null
    @JvmField var loadTask: DialogTask? = null
    @JvmField var downLinkDialog: DialogDownLink? = null
    @JvmField var actionBusy: Boolean = false
    @JvmField var dismissed: Boolean = false
    @JvmField var downItem: DownListItem? = null
    @JvmField var infoItem: DownListItem? = null
    @JvmField var linkItem: DownListItem? = null
    @JvmField var pendingUrl: String? = null
    @JvmField var pendingItem: DownListItem? = null

    private var busy by mutableStateOf(true)
    private var items by mutableStateOf<List<DownListItem>>(emptyList())

    init {
        setSheet {
            if (items.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painterResource(AppR.drawable.outline_download_black_24), null)
                    Text(
                        stringResource(AppR.string.download), style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
            }
            if (busy) {
                Box(Modifier.fillMaxWidth().heightIn(min = 112.dp), contentAlignment = Alignment.Center) {
                    LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 24.dp))
                }
            }
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(bottom = 16.dp)) {
                items.forEachIndexed { i, item -> FontRow(i, item) }
            }
        }
        show()
        val task = DialogTask(this)
        loadTask = task
        task.start(appContext)
    }

    @androidx.compose.runtime.Composable
    private fun FontRow(position: Int, item: DownListItem) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable { download(position) }.padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHighest, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text((position + 1).toString(), style = MaterialTheme.typography.labelLarge)
                }
            }
            Text(
                item.title ?: "", maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
            )
            if (!TextUtils.isEmpty(item.fmt)) {
                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(item.fmt!!, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            IconButton(onClick = { info(position) }) {
                Icon(painterResource(AppR.drawable.outline_info_black_24), stringResource(AppR.string.info))
            }
        }
    }

    /** Row tap. */
    private fun download(index: Int) {
        val list = fontItems
        if (videoListener == null || list == null || index < 0 || index >= list.size || actionBusy) return
        val item = list[index] as DownListItem? ?: return
        actionBusy = true
        downItem = item
        ui.post(DeliverItemTask())
    }

    /** Info button. */
    private fun info(index: Int) {
        val list = fontItems
        if (videoListener == null || list == null || index < 0 || index >= list.size || actionBusy) return
        val item = list[index] as DownListItem? ?: return
        if (TextUtils.isEmpty(item.url)) return
        actionBusy = true
        infoItem = item
        ui.post(CloseDownLinkTask())
    }

    internal inner class DeliverItemTask : Runnable {
        override fun run() {
            val dialogDownFont = this@DialogDownFont
            val downListItem = dialogDownFont.downItem
            dialogDownFont.downItem = null
            val videoListListener: VideoListListener? = if (downListItem != null) dialogDownFont.videoListener else null
            if (downListItem != null && videoListListener != null) {
                videoListListener.download(downListItem.url, 0, downListItem.title, false)
            }
            dialogDownFont.actionBusy = false
        }
    }
    internal inner class CloseDownLinkTask : Runnable {
        override fun run() {
            val dialogDownFont = this@DialogDownFont
            val downListItem = dialogDownFont.infoItem
            dialogDownFont.infoItem = null
            if (downListItem != null) {
                val url = downListItem.url
                if (dialogDownFont.hostActivity != null && dialogDownFont.downLinkDialog == null) {
                    dialogDownFont.closeDownLink()
                    dialogDownFont.linkItem = downListItem
                    val dialogDownLink = DialogDownLink(
                        dialogDownFont.hostActivity,
                        url,
                        dialogDownFont.referer,
                        downListItem.fileSize,
                        dialogDownFont.PreviewActions()
                    )
                    dialogDownFont.downLinkDialog = dialogDownLink
                    dialogDownLink.setOnDismissListener(dialogDownFont.DownLinkDialogDismiss())
                }
            }
            dialogDownFont.actionBusy = false
        }
    }
    internal inner class PreviewActions : PreviewListener {
        internal inner class OpenWithTask : Runnable {
            override fun run() {
                val dialogDownFont = this@DialogDownFont
                val pendingUrl = dialogDownFont.pendingUrl
                val downListItem = dialogDownFont.pendingItem
                dialogDownFont.pendingUrl = null
                dialogDownFont.pendingItem = null
                val videoListListener: VideoListListener? = if (downListItem != null) dialogDownFont.videoListener else null
                if (downListItem != null && videoListListener != null) {
                    videoListListener.openWith(pendingUrl, downListItem.title, MainUtil.mimeForExt(downListItem.fmt))
                }
                dialogDownFont.actionBusy = false
            }
        }
        internal inner class OpenPendingTask : Runnable {
            override fun run() {
                val dialogDownFont = this@DialogDownFont
                val pendingUrl = dialogDownFont.pendingUrl
                val downListItem = dialogDownFont.pendingItem
                dialogDownFont.pendingUrl = null
                dialogDownFont.pendingItem = null
                val videoListListener: VideoListListener? = if (downListItem != null) dialogDownFont.videoListener else null
                if (downListItem != null && videoListListener != null) {
                    videoListListener.play(pendingUrl, downListItem.title)
                }
                dialogDownFont.actionBusy = false
            }
        }
        override fun copyUrl(url: String?) {
        }
        override fun openFile(url: String?, size: Long, isVideo: Boolean) {
            val downListItem = this@DialogDownFont.linkItem
            if (downListItem == null) {
                return
            }
            downListItem.fileSize = size
        }
        override fun download(url: String?, mimeType: String?) {
            val dialogDownFont = this@DialogDownFont
            if (dialogDownFont.videoListener == null || dialogDownFont.actionBusy) {
                return
            }
            dialogDownFont.actionBusy = true
            dialogDownFont.pendingUrl = url
            dialogDownFont.pendingItem = dialogDownFont.linkItem
            dialogDownFont.closeDownLink()
            val handler = dialogDownFont.mainHandler
            if (handler == null) {
                return
            }
            handler.post(OpenWithTask())
        }
        override fun share(url: String?) {
            val dialogDownFont = this@DialogDownFont
            if (dialogDownFont.videoListener == null || dialogDownFont.actionBusy) {
                return
            }
            dialogDownFont.actionBusy = true
            dialogDownFont.pendingUrl = url
            dialogDownFont.pendingItem = dialogDownFont.linkItem
            dialogDownFont.closeDownLink()
            val handler = dialogDownFont.mainHandler
            if (handler == null) {
                return
            }
            handler.post(OpenPendingTask())
        }
        override fun openWith(url: String?, mimeType: String?) {
        }
    }
    internal inner class DownLinkDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            this@DialogDownFont.closeDownLink()
        }
    }

    fun closeDownLink() {
        val dialogDownLink = this.downLinkDialog
        if (dialogDownLink != null) {
            dialogDownLink.dismiss()
            this.downLinkDialog = null
        }
        this.linkItem = null
    }
    class DialogTask(dialogDownFont: DialogDownFont?) : MyAsyncTask() {
        @JvmField var dialogRef: WeakReference<*> = WeakReference<Any?>(dialogDownFont)
        @JvmField var pageUrl: String? = dialogDownFont?.pageUrl
        @JvmField var referer: String? = dialogDownFont?.referer
        @JvmField var fonts: ArrayList<Any?>? = null

        public override fun doInBackground() {
            val dialog = this.dialogRef.get() as DialogDownFont?
            if (dialog == null || this.cancelled || TextUtils.isEmpty(this.pageUrl)) {
                return
            }
            val conn = openHttpConnection(0, 0, dialog.appContext, this.pageUrl, this.referer, false)
            if (conn == null) {
                return
            }
            var `in`: InputStream? = null
            var reader: InputStreamReader? = null
            var lines: BufferedReader? = null
            try {
                conn.setDoInput(true)
                conn.connect()
                `in` = conn.getInputStream()
                reader = InputStreamReader(`in`)
                lines = BufferedReader(reader)
                var index = 0
                var line: String?
                while ((lines.readLine().also { line = it }) != null) {
                    if (this.cancelled) {
                        this.fonts = null
                        break
                    }
                    var name: String? = fileNameOf(line)
                    var url: String? = urlOf(line)
                    if (TextUtils.isEmpty(name) && TextUtils.isEmpty(url)) {
                        continue
                    }
                    while (TextUtils.isEmpty(name) || TextUtils.isEmpty(url)) {
                        val next = lines.readLine()
                        if (next == null) {
                            break
                        }
                        if (this.cancelled) {
                            this.fonts = null
                            break
                        }
                        val nextName: String? = fileNameOf(next)
                        val nextUrl: String? = urlOf(next)
                        if (!TextUtils.isEmpty(nextName)) {
                            name = nextName
                        }
                        if (!TextUtils.isEmpty(nextUrl)) {
                            url = nextUrl
                        }
                    }
                    if (this.cancelled || TextUtils.isEmpty(name) || TextUtils.isEmpty(url)) {
                        continue
                    }
                    val item =
                        DownListItem(index, name, if (name!!.endsWith("otf")) "OTF" else "TTF")
                    item.url = url
                    if (this.fonts == null) {
                        this.fonts = ArrayList<Any?>()
                    }
                    this.fonts!!.add(item)
                    index++
                }
            } catch (ignored: Exception) {
            }
            try {
                if (lines != null) {
                    lines.close()
                }
            } catch (ignored: Exception) {
            }
            try {
                if (reader != null) {
                    reader.close()
                }
            } catch (ignored: Exception) {
            }
            try {
                if (`in` != null) {
                    `in`.close()
                }
            } catch (ignored: Exception) {
            }
            conn.disconnect()
        }
        public override fun onCancelled() {
            val dialogDownFont = this.dialogRef.get() as DialogDownFont? ?: return
            dialogDownFont.loadTask = null
            dialogDownFont.dismiss()
        }

        public override fun onPostExecute() {
            val dialogDownFont = this.dialogRef.get() as DialogDownFont? ?: return
            dialogDownFont.loadTask = null
            if (dialogDownFont.appContext == null) return
            dialogDownFont.busy = false
            val arrayList = this.fonts
            if (arrayList == null || arrayList.isEmpty()) {
                showToast(dialogDownFont.hostActivity, AppR.string.invalid_url)
                dialogDownFont.dismiss()
                return
            }
            if (arrayList.size != 1) {
                dialogDownFont.fontItems = arrayList
                dialogDownFont.items = arrayList.map { it as DownListItem }
                return
            }
            if (dialogDownFont.videoListener != null) {
                val downListItem = arrayList[0] as DownListItem?
                if (downListItem == null) {
                    showToast(dialogDownFont.hostActivity, AppR.string.invalid_url)
                    dialogDownFont.dismiss()
                } else {
                    if (dialogDownFont.actionBusy) return
                    dialogDownFont.actionBusy = true
                    dialogDownFont.downItem = downListItem
                    dialogDownFont.ui.post(dialogDownFont.DeliverItemTask())
                }
            }
        }
    }

    override fun dismiss() {
        if (dismissed) return
        dismissed = true
        if (appContext == null) {
            super.dismiss()
            return
        }
        closeDownLink()
        loadTask?.cancelled = true
        loadTask = null
        hostActivity = null
        appContext = null
        videoListener = null
        pageUrl = null
        referer = null
        fontItems = null
        super.dismiss()
    }

    companion object {
        @JvmStatic
        fun fileNameOf(json: String?): String? {
            var nameStart: Int = 0
            if (TextUtils.isEmpty(json)) {
                return null
            }
            try {
                val keyIndex = json!!.indexOf("\"filename\"")
                val quoteStart = if (keyIndex == -1) 0 else json.indexOf(34.toChar(), keyIndex + 11)
                val quoteEnd = if (keyIndex == -1 || quoteStart == -1) 0 else json.indexOf(
                        34.toChar(),
                        ((quoteStart + 1).also { nameStart = it })
                    )
                if (keyIndex == -1 || quoteStart == -1 || quoteEnd == -1) {
                    return null
                }
                val fileName = json.substring(nameStart, quoteEnd).trim { it <= ' ' }
                if (fileName.endsWith(".ttf") || fileName.endsWith(".otf")) {
                    return fileName.replace('/', '-')
                }
                return null
            } catch (unused: Exception) {
                return null
            }
        }

        @JvmStatic
        fun urlOf(json: String?): String? {
            var urlStart: Int = 0
            if (TextUtils.isEmpty(json)) {
                return null
            }
            try {
                val keyIndex = json!!.indexOf("\"url\"")
                val quoteStart = if (keyIndex == -1) 0 else json.indexOf(34.toChar(), keyIndex + 6)
                val quoteEnd = if (keyIndex == -1 || quoteStart == -1) 0 else json.indexOf(
                        34.toChar(),
                        ((quoteStart + 1).also { urlStart = it })
                    )
                if (keyIndex == -1 || quoteStart == -1 || quoteEnd == -1) {
                    return null
                }
                val foundUrl = json.substring(urlStart, quoteEnd).trim { it <= ' ' }
                if (URLUtil.isNetworkUrl(foundUrl)) {
                    return foundUrl
                }
                return null
            } catch (unused: Exception) {
                return null
            }
        }
    }
}
