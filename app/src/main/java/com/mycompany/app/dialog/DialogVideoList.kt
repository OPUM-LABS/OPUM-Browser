package com.mycompany.app.dialog

import java.util.ArrayList
import java.util.Comparator
import ch.opumlabs.opumbrowser.R as AppR
import android.content.Context
import android.content.DialogInterface
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.compress.Compress.Companion.isSubtitleExt
import com.mycompany.app.dialog.DialogPreview.PreviewListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainDownAdapter.DownListItem
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.externalVolumes
import com.mycompany.app.main.MainUri.isFile
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.main.MainUtil.isFilmizle
import com.mycompany.app.main.MainUtil.destroyWebViewNow
import com.mycompany.app.main.MainUtil.firstUrlIn
import com.mycompany.app.main.MainUtil.uniqueName
import com.mycompany.app.main.MainUtil.fileExtOf
import com.mycompany.app.main.MainUtil.guessDownloadName
import com.mycompany.app.main.MainUtil.stripExtension
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.isFakeStream
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
import com.mycompany.app.web.WebNestView
import com.mycompany.app.web.WebSnsLoad.SnsLoadListener
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.web.WebViewActivity.FaceItem
import com.mycompany.app.web.WebVkvLoad
import java.lang.ref.WeakReference
import java.util.Collections
import kotlin.math.min
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.ui.browser.DownRow

/**
 * Video list of the current page for download / full screen / PiP ([listTypeParam] = 0 download, 1 full screen, 2 PiP):
 * reads the page's found media in the background ([parseResults], per-site rules for soundcloud, onlyfans, pornhub, giphy,
 * dcinside, izle; or a vkv list through [startVkv]), then shows the rows ([DownRow]: tap = [VideoListListener.download], info =
 * the link details in [DialogDownLink] (download / preview), play = [VideoListListener.showInfo]). A single item is used
 * at once; none says "no video" and asks the page for more ([VideoListListener.onScanEmpty]). Callers set the page kind
 * ([startScan], [startVkv], [scanState] / [foundItems]) right after the constructor; the sheet shows on the next main-loop turn and reads it
 * then, like the old one. Compose now (on [ComposeBottomDialog]); same constructor.
 */
@Suppress("deprecation")
class DialogVideoList(
    webViewActivity: WebViewActivity?,
    webNestView: WebNestView?,
    pageUrlParam: String?,
    listTypeParam: Int,
    videoListListener: VideoListListener?
) : ComposeBottomDialog(webViewActivity) {
    private val ui = android.os.Handler(android.os.Looper.getMainLooper())
    @JvmField val mainHandler: android.os.Handler? = ui

    @JvmField var actionBusy: Boolean = false
    @JvmField var dismissed: Boolean = false
    @JvmField var downItem: DownListItem? = null
    @JvmField var infoItem: DownListItem? = null
    @JvmField var playItem: DownListItem? = null
    @JvmField var linkItem: DownListItem? = null
    @JvmField var pendingUrl: String? = null
    @JvmField var pendingItem: DownListItem? = null
    @JvmField var hostActivity: WebViewActivity? = webViewActivity
    @JvmField var appContext: Context? = context
    @JvmField var videoListener: VideoListListener? = videoListListener
    @JvmField var webNest: WebNestView? = webNestView
    @JvmField var pageUrl: String? = pageUrlParam
    @JvmField var listType: Byte = listTypeParam.toByte()
    @JvmField var scanState: Byte = 0
    @JvmField var scanStarted: Boolean = false
    @JvmField var vkvListener: VkvListListener? = null
    @JvmField var vkvLoad: WebVkvLoad? = null
    @JvmField var shownItems: ArrayList<DownListItem?>? = null
    @JvmField var loadTask: DialogTask? = null
    @JvmField var foundItems: ArrayList<DownListItem?>? = null
    @JvmField var foundState: Byte = 0
    @JvmField var downLinkDialog: DialogDownLink? = null

    /** The sheet is up and not dismissed. */
    @Volatile private var live = false
    /** Non-null while the sheet is up (the page's video callbacks check it, like the old view field). */
    val liveOrNull: Any? get() = if (live) this else null
    private var busy by mutableStateOf(false)
    private var items by mutableStateOf<List<DownListItem>>(emptyList())
    private var listMode by mutableIntStateOf(0)
    private var emptyText by mutableIntStateOf(0)

    init {
        run init@{
        if (if (webNestView == null) false else webNestView.hasDummyVideo) {
                this.scanState = 1.toByte()
                if (!this.scanStarted) {
                    this.scanStarted = true
                    evalJs(
                        webNestView,
                        "function mySrc(src){return src&&(src.indexOf('http')==0)&&!src.includes('youtube.com');}(function(){var src=null;var pst=null;var ele=document.querySelector(\"video\");if(ele){if(mySrc(ele.src)){src=ele.src;}else{var el2=ele.querySelector(\"source[type*='video']\");if(el2){if(mySrc(el2.src)){src=el2.src;}}}pst=ele.poster;}android.onVidDe2(src,pst);})();",
                        true
                    )
                }
            }
        }
        setSheet {
            if (items.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val mode = listType.toInt()
                    Icon(
                        painterResource(
                            when (mode) {
                                2 -> AppR.drawable.outline_picture_in_picture_alt_black_24
                                1 -> AppR.drawable.outline_fullscreen_black_24
                                else -> AppR.drawable.outline_download_black_24
                            }
                        ), null,
                    )
                    Text(
                        stringResource(
                            when (mode) {
                                2 -> AppR.string.pip_mode
                                1 -> AppR.string.full_screen
                                else -> AppR.string.download
                            }
                        ),
                        style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp),
                    )
                }
            }
            if (busy) {
                Box(Modifier.fillMaxWidth().heightIn(min = 112.dp), contentAlignment = Alignment.Center) {
                    LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 24.dp))
                }
            }
            if (emptyText != 0) {
                Box(Modifier.fillMaxWidth().heightIn(min = 160.dp).padding(16.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(emptyText), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                }
            }
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(bottom = 16.dp)) {
                items.forEachIndexed { pos, item ->
                    DownRow(
                        hostActivity, item, pos, listMode, pageUrl,
                        onClick = { download(item.index) }, onInfo = { info(item.index) }, onPlay = { play(item.index) },
                    )
                }
            }
        }
        ui.post { start() }
    }

    /** show, then load by page kind. */
    private fun start() {
        if (appContext == null || dismissed) return
        live = true
        busy = true
        show()
        val stateNow = scanState
        if (stateNow.toInt() == 0) {
            if (loadTask != null) return
            val dialogTask = DialogTask(this)
            loadTask = dialogTask
            dialogTask.start(appContext)
            return
        }
        if (stateNow.toInt() != 5) {
            applyBusy(true)
            return
        }
        val arrayList = foundItems
        if (arrayList == null || arrayList.isEmpty()) {
            applyBusy(true)
        } else {
            markDownloaded()
            applyFound()
        }
    }

    private fun item(index: Int): DownListItem? {
        val list = if (videoListener == null) null else shownItems
        if (list == null || index < 0 || index >= list.size) return null
        return list[index]
    }

    /** Row tap. */
    private fun download(type: Int) {
        val it = item(type) ?: return
        if (actionBusy) return
        actionBusy = true
        downItem = it
        ui.post(DownloadTask())
    }

    /** Info button. */
    private fun info(type: Int) {
        val it = item(type) ?: return
        if (TextUtils.isEmpty(it.url) || actionBusy) return
        actionBusy = true
        infoItem = it
        ui.post(CloseDownLinkTask())
    }

    /** Play button. */
    private fun play(type: Int) {
        val it = item(type) ?: return
        if (actionBusy) return
        actionBusy = true
        playItem = it
        ui.post(ShowInfoTask())
    }

    internal inner class DownloadTask : Runnable {
        override fun run() {
            val dialogVideoList = this@DialogVideoList
            val downListItem = dialogVideoList.downItem
            dialogVideoList.downItem = null
            val videoListListener: VideoListListener? = if (downListItem != null) dialogVideoList.videoListener else null
            if (downListItem != null && videoListListener != null) {
                videoListListener.download(
                    downListItem.url,
                    dialogVideoList.listType.toInt(),
                    null,
                    downListItem.isLive
                )
            }
            dialogVideoList.actionBusy = false
        }
    }
    internal inner class CloseDownLinkTask : Runnable {
        override fun run() {
            val dialogVideoList = this@DialogVideoList
            val downListItem = dialogVideoList.infoItem
            dialogVideoList.infoItem = null
            if (downListItem != null) {
                val firstUrl = firstUrlIn(downListItem.url)
                if (dialogVideoList.hostActivity != null && dialogVideoList.downLinkDialog == null) {
                    dialogVideoList.closeDownLink()
                    dialogVideoList.linkItem = downListItem
                    val dialogDownLink = DialogDownLink(
                        dialogVideoList.hostActivity,
                        firstUrl,
                        dialogVideoList.pageUrl,
                        downListItem.fileSize,
                        dialogVideoList.PreviewActions()
                    )
                    dialogVideoList.downLinkDialog = dialogDownLink
                    dialogDownLink.setOnDismissListener(dialogVideoList.DownLinkDialogDismiss())
                }
            }
            dialogVideoList.actionBusy = false
        }
    }
    internal inner class ShowInfoTask : Runnable {
        override fun run() {
            val dialogVideoList = this@DialogVideoList
            val downListItem = dialogVideoList.playItem
            dialogVideoList.playItem = null
            if (downListItem != null) {
                val fmt = downListItem.fmt
                var title = downListItem.title
                if (!TextUtils.isEmpty(fmt)) {
                    val sbV = StringBuilder().append(title).append(".")
                    sbV.append(fmt!!.lowercase())
                    title = sbV.toString()
                }
                val videoListListener = dialogVideoList.videoListener
                if (videoListListener != null) {
                    videoListListener.showInfo(downListItem.url, title, downListItem.isLive)
                }
            }
            dialogVideoList.actionBusy = false
        }
    }
    internal inner class PreviewActions : PreviewListener {
        internal inner class OpenWithTask : Runnable {
            override fun run() {
                val dialogVideoList = this@DialogVideoList
                val pendingUrl = dialogVideoList.pendingUrl
                val downListItem = dialogVideoList.pendingItem
                dialogVideoList.pendingUrl = null
                dialogVideoList.pendingItem = null
                val videoListListener: VideoListListener? = if (downListItem != null) dialogVideoList.videoListener else null
                if (downListItem != null && videoListListener != null) {
                    videoListListener.openWith(pendingUrl, downListItem.title, MainUtil.mimeForExt(downListItem.fmt))
                }
                dialogVideoList.actionBusy = false
            }
        }
        internal inner class OpenPendingTask : Runnable {
            override fun run() {
                val dialogVideoList = this@DialogVideoList
                val pendingUrl = dialogVideoList.pendingUrl
                val downListItem = dialogVideoList.pendingItem
                dialogVideoList.pendingUrl = null
                dialogVideoList.pendingItem = null
                val videoListListener: VideoListListener? = if (downListItem != null) dialogVideoList.videoListener else null
                if (downListItem != null && videoListListener != null) {
                    videoListListener.play(pendingUrl, downListItem.title)
                }
                dialogVideoList.actionBusy = false
            }
        }
        override fun copyUrl(url: String?) {
        }
        override fun openFile(url: String?, size: Long, isVideo: Boolean) {
            val downListItem = this@DialogVideoList.linkItem
            if (downListItem == null) {
                return
            }
            downListItem.fileSize = size
        }
        override fun download(url: String?, mimeType: String?) {
            val dialogVideoList = this@DialogVideoList
            if (dialogVideoList.videoListener == null || dialogVideoList.actionBusy) {
                return
            }
            dialogVideoList.actionBusy = true
            dialogVideoList.pendingUrl = url
            dialogVideoList.pendingItem = dialogVideoList.linkItem
            dialogVideoList.closeDownLink()
            val handler = dialogVideoList.mainHandler
            if (handler == null) {
                return
            }
            handler.post(OpenWithTask())
        }
        override fun share(url: String?) {
            val dialogVideoList = this@DialogVideoList
            if (dialogVideoList.videoListener == null || dialogVideoList.actionBusy) {
                return
            }
            dialogVideoList.actionBusy = true
            dialogVideoList.pendingUrl = url
            dialogVideoList.pendingItem = dialogVideoList.linkItem
            dialogVideoList.closeDownLink()
            val handler = dialogVideoList.mainHandler
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
            this@DialogVideoList.closeDownLink()
        }
    }

    internal inner class VkvLoadListener : SnsLoadListener {
        override fun onStart() {
        }
        override fun onResult(pickIndex: Int, arrayList: ArrayList<DownListItem?>?) {
            val dialogVideoList = this@DialogVideoList
            if (dialogVideoList.vkvLoad == null) {
                return
            }
            val vkvListListener = dialogVideoList.vkvListener
            if (vkvListListener != null) {
                vkvListListener.onList(arrayList)
            }
            dialogVideoList.stopVkv()
            dialogVideoList.foundItems = arrayList
            dialogVideoList.foundState = 0.toByte()
            dialogVideoList.markDownloaded()
            dialogVideoList.applyFound()
        }
        override fun onError(errorType: Int) {
            val dialogVideoList = this@DialogVideoList
            if (!dialogVideoList.live) {
                return
            }
            dialogVideoList.applyBusy(false)
            dialogVideoList.showEmpty()
        }
    }

    class DialogTask(dialogVideoList: DialogVideoList?) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>

        init {
            this.dialogRef = WeakReference<Any?>(dialogVideoList)
        }
        public override fun doInBackground() {
            val dialogVideoList = this.dialogRef.get() as DialogVideoList?
            if (dialogVideoList == null || this.cancelled) {
                return
            }
            try {
                try {
                    dialogVideoList.parseResults()
                } catch (unused: Exception) {
                }
            } catch (unused2: Exception) {
                dialogVideoList.parseResults()
            }
        }
        public override fun onCancelled() {
            val dialogVideoList = this.dialogRef.get() as DialogVideoList?
            if (dialogVideoList == null) {
                return
            }
            dialogVideoList.loadTask = null
            dialogVideoList.dismiss()
        }
        public override fun onPostExecute() {
            val dialogVideoList = this.dialogRef.get() as DialogVideoList?
            if (dialogVideoList == null) {
                return
            }
            dialogVideoList.loadTask = null
            dialogVideoList.applyFound()
        }
        public override fun onPreExecute() {
            val dialogVideoList = this.dialogRef.get() as DialogVideoList?
            if (dialogVideoList == null) {
                return
            }
            dialogVideoList.applyBusy(true)
        }
    }

    private class SortSub : Comparator<DownListItem?> {
        override fun compare(downListItem: DownListItem?, other: DownListItem?): Int {
            val left = downListItem
            val right = other
            if (left == null) {
                return if (right == null) 0 else 1
            }
            if (right == null) {
                return -1
            }
            val isLive = left.isLive
            val isLiveNow = right.isLive
            if (isLive) {
                return if (!isLiveNow) 1 else 0
            }
            return if (isLiveNow) -1 else 0
        }
    }

    interface VideoListListener {
        fun download(url: String?, type: Int, referer: String?, isLive: Boolean)

        fun play(url: String?, referer: String?)

        fun openWith(url: String?, mimeType: String?, referer: String?)

        fun onScanEmpty()

        fun showInfo(url: String?, info: String?, isLive: Boolean)
    }

    interface VkvListListener {
        fun onList(arrayList: ArrayList<DownListItem?>?)
    }

    fun closeDownLink() {
        val dialogDownLink = this.downLinkDialog
        if (dialogDownLink != null) {
            dialogDownLink.dismiss()
            this.downLinkDialog = null
        }
        this.linkItem = null
    }

    fun checkScan() {
        if (!this.live) {
            this.scanState = 0.toByte()
            return
        }
        if (this.scanState.toInt() == 1) {
            this.scanState = 0.toByte()
        }
        if (this.loadTask != null) {
            return
        }
        val dialogTask = DialogTask(this)
        this.loadTask = dialogTask
        dialogTask.start(this.appContext)
    }

    fun stopVkv() {
        val webVkvLoad = this.vkvLoad
        if (webVkvLoad != null) {
            val myWebSafe = webVkvLoad.webSafe
            if (myWebSafe != null) {
                if (webVkvLoad.pageLoading) {
                    webVkvLoad.pageLoading = false
                    myWebSafe.stopLoading()
                }
                destroyWebViewNow(webVkvLoad.webSafe)
                webVkvLoad.webSafe = null
            }
            webVkvLoad.hostActivity = null
            webVkvLoad.appContext = null
            webVkvLoad.loadListener = null
            webVkvLoad.parentView = null
            webVkvLoad.pageUrl = null
            this.vkvLoad = null
        }
        this.vkvListener = null
    }
    fun startScan(mode: Int) {
        this.scanState = (mode).toByte()
        if (mode == 1) {
            if (this.scanStarted) {
                return
            }
            this.scanStarted = true
            evalJs(
                this.webNest,
                "function mySrc(src){return src&&(src.indexOf('http')==0)&&!src.includes('youtube.com');}(function(){var src=null;var pst=null;var ele=document.querySelector(\"video\");if(ele){if(mySrc(ele.src)){src=ele.src;}else{var el2=ele.querySelector(\"source[type*='video']\");if(el2){if(mySrc(el2.src)){src=el2.src;}}}pst=ele.poster;}android.onVidDe2(src,pst);})();",
                true
            )
            return
        }
        if (mode == 2) {
            evalJs(
                this.webNest,
                "(function(){var htm=null;var eles=document.querySelectorAll(\"div[id='mobileContainer']>script[type='text/javascript']\");if(eles&&(eles.length>0)){for(var i=0;i<eles.length;i++){var val=eles[i].innerHTML;if(val&&val.includes('mediaDefinitions')){htm=val;break;}}}android.onVidDe3(htm,1);})();",
                true
            )
        } else if (mode == 3) {
            evalJs(
                this.webNest,
                "(function(){var htm=null;var eles=document.querySelectorAll(\"div[id='js-gifWebMWrapper'][data-gif^='http']\");if(eles&&(eles.length>0)){for(var i=0;i<eles.length;i++){var ele=eles[i];var val=ele.dataset.gif;if(!val)continue;if(ele.dataset.jpg)val+='!@!'+ele.dataset.jpg;if(ele.dataset.mp4)val+='!@!'+ele.dataset.mp4;if(ele.dataset.webm)val+='!@!'+ele.dataset.webm;htm=val;break;}}android.onVidDe3(htm,2);})();",
                true
            )
        } else if (mode == 4) {
            evalJs(
                this.webNest,
                "(function(){var htm=null;var ele=document.querySelector(\"script[id='initials-script']\");if(ele){htm=ele.innerHTML;}android.onVidDe3(htm,3);})();",
                true
            )
        }
    }
    fun parseResults() {
        var audioCount: Int
        val foundState: Byte
        var found: Boolean
        var fileBase: String?
        var hasAudio: Boolean
        var format: String?
        var allVideo: Boolean
        var isFake: Boolean
        var chosenFormat: String?
        var arrayList: ArrayList<String?>?
        var formatLabel: String?
        var notFound: Boolean
        this.foundItems = null
        this.foundState = 0.toByte()
        val webNestView = this.webNest
        if (webNestView != null) {
            val foundItems: ArrayList<DownListItem?> = ArrayList()
            var soundCount = 1
            val host = MainUtil.hostOf(webNestView.downUrl!!, true)
            var downList = webNestView.downList
            val size = if (downList != null) downList.size else 0
            val downPoster = webNestView.downPoster
            val hasPosters = if (downPoster == null || downPoster.isEmpty()) false else true
            val downloaded = webNestView.downloaded
            val hasDownloaded = if (downloaded == null || downloaded.isEmpty()) false else true
            val downFail = webNestView.downFail
            val hasFailed = if (downFail == null || downFail.isEmpty()) false else true
            val pubList = webNestView.pubList
            var prefix = "izle:"
            if (TextUtils.isEmpty(host)) {
                audioCount = 0
                foundState = 0
                found = false
            } else {
                if (host!!.endsWith("soundcloud.com")) {
                    var soundNo = 1
                    var index = 0
                    while (index < size) {
                        if (!this.live) {
                            return
                        }
                        val url = downList!!.get(index)
                        if (!isDummyUrl(url)) {
                            val soundLabel = if (soundNo > soundCount) "Sound ($soundNo)" else "Sound"
                            soundNo++
                            val downListItem = DownListItem(index, soundLabel, "MP3")
                            if (hasFailed && downFail!!.contains(url)) {
                                downListItem.checkState = 2.toByte()
                            } else if (hasDownloaded && downloaded!!.contains(url)) {
                                downListItem.checkState = 1.toByte()
                            }
                            if (hasPosters && index < downPoster!!.size) {
                                downListItem.thumb = downPoster.get(index)
                            }
                            downListItem.url = url
                            foundItems.add(downListItem)
                        }
                        index++
                        soundCount = 1
                    }
                    this.foundItems = foundItems
                    this.foundState = 0.toByte()
                    return
                }
                if (host.endsWith("onlyfans.com")) {
                    audioCount = 0
                    foundState = 0
                    found = true
                } else {
                    if (host.endsWith("pornhub.com")) {
                        audioCount = 0
                        foundState = 1
                    } else if (host.endsWith("giphy.com")) {
                        audioCount = 0
                        foundState = 2
                    } else if (TextUtils.isEmpty(host) || !(host.endsWith("dcinside.com") || host.endsWith(
                            "dcinside.co.kr"
                        ))
                    ) {
                        if (isFilmizle(host)) {
                            var length = 0
                            for (index in 0..<size) {
                                if (!this.live) {
                                    return
                                }
                                val item = downList!!.get(index)
                                if (item != null && item.startsWith("izle:")) {
                                    length =
                                        if (length == 0) item.length else min(length, item.length)
                                }
                            }
                            audioCount = length - 8
                        } else {
                            audioCount = 0
                        }
                        foundState = 0
                    } else {
                        audioCount = 0
                        foundState = 3
                    }
                    found = false
                }
            }
            var usedNames: ArrayList<String?>? = null
            var itemIndex = 0
            var anyAdded = false
            while (itemIndex < size) {
                val isVideo = found
                if (!this.live) {
                    return
                }
                val url = downList!!.get(itemIndex)
                if (isDummyUrl(url)) {
                    prefix = prefix
                    audioCount = audioCount
                    downList = downList
                } else {
                    if (audioCount <= 0 || !url!!.startsWith(prefix)) {
                        fileBase = guessDownloadName(url, null, "video/*", true)
                        hasAudio = false
                    } else {
                        fileBase = url.substring(audioCount)
                        hasAudio = true
                    }
                    if (TextUtils.isEmpty(fileBase)) {
                        continue
                    } else {
                        if (fileBase!!.endsWith(".mp4") || fileBase.endsWith(".m3u8") || hasAudio || MainUtil.isFakeStream(url)) {
                            format = "MP4"
                            allVideo = true
                        } else {
                            allVideo = false
                            format = null
                        }
                        var baseName = MainUtil.stripExtension(fileBase)
                        if (TextUtils.isEmpty(baseName)) {
                            continue
                        } else {
                            var videoOnly = allVideo
                            if (foundState.toInt() != 1) {
                                isFake = false
                            } else {
                                if (pubList == null) {
                                    baseName = baseName
                                    notFound = false
                                    break
                                }
                                var it: MutableIterator<FaceItem?> = pubList.iterator()
                                while (true) {
                                    if (!it.hasNext()) {
                                        baseName = baseName
                                        notFound = false
                                        break
                                    }
                                    val pending = it
                                    val next = it.next()
                                    val nextUrl = baseName
                                    if (!this.live) {
                                        return
                                    }
                                    if (url == next!!.url) {
                                        baseName = next.label
                                        format = "MP4"
                                        notFound = true
                                        videoOnly = true
                                        break
                                    }
                                    baseName = nextUrl
                                    it = pending
                                }
                                if (!notFound) {
                                    if (if (TextUtils.isEmpty(url)) false else "gif".equals(
                                            fileExtOf(
                                                url,
                                                false
                                            ), ignoreCase = true
                                        )
                                    ) {
                                        val parenIndex = baseName!!.lastIndexOf(41.toChar())
                                        val index = if (parenIndex >= 0) (parenIndex + 1) else 0
                                        if (parenIndex >= 0 && index < baseName.length) {
                                            baseName = baseName.substring(index)
                                        }
                                        format = "GIF"
                                        isFake = true
                                        videoOnly = true
                                    }
                                }
                                format = format
                                isFake = false
                            }
                            val fake = isFake
                            if (isVideo) {
                                if (baseName == null) {
                                    chosenFormat = format
                                } else {
                                    val length = baseName.length
                                    chosenFormat = format
                                    if (length != 0) {
                                        val underscoreIndex = baseName.lastIndexOf(95.toChar())
                                        val index = if (underscoreIndex > 0) (underscoreIndex + 1) else 0
                                        if (underscoreIndex > 0 && index < length) {
                                            baseName = baseName.substring(index)
                                        }
                                    }
                                }
                                baseName = null
                            } else {
                                chosenFormat = format
                                baseName = uniqueName(usedNames, baseName)
                            }
                            val mediaFormat = if (TextUtils.isEmpty(chosenFormat)) fileExtOf(url, true) else chosenFormat
                            if (TextUtils.isEmpty(mediaFormat) || "PHP" == mediaFormat) {
                                arrayList = usedNames
                                formatLabel = "MP4"
                                videoOnly = true
                            } else {
                                arrayList = usedNames
                                formatLabel =
                                    if (mediaFormat!!.length > 4) mediaFormat.substring(0, 4) else mediaFormat
                            }
                            val newItem = DownListItem(itemIndex, baseName, formatLabel)
                            if (hasFailed && downFail!!.contains(url)) {
                                newItem.checkState = 2.toByte()
                            } else if (hasDownloaded && downloaded!!.contains(url)) {
                                newItem.checkState = 1.toByte()
                            }
                            if (fake) {
                                if (hasPosters && itemIndex < downPoster!!.size) {
                                    newItem.thumb = downPoster.get(itemIndex)
                                }
                                if (TextUtils.isEmpty(newItem.thumb)) {
                                    newItem.thumb = url
                                }
                            } else if (foundState.toInt() == 2) {
                                newItem.thumb = url
                            } else if (foundState.toInt() == 3 && MainUtil.isDcinsideImage(url!!)) {
                                newItem.thumb = url
                            } else if (hasPosters && itemIndex < downPoster!!.size) {
                                newItem.thumb = downPoster.get(itemIndex)
                            }
                            if (!videoOnly && !TextUtils.isEmpty(formatLabel)) {
                                val isSubtitle = isSubtitleExt(formatLabel.lowercase())
                                newItem.isLive = isSubtitle
                                if (isSubtitle) {
                                    anyAdded = true
                                }
                            }
                            newItem.url = url
                            foundItems.add(newItem)
                            if (isVideo) {
                                usedNames = arrayList
                            } else {
                                usedNames = if (arrayList == null) ArrayList() else arrayList
                                usedNames.add(baseName)
                            }
                        }
                    }
                }
                itemIndex++
                downList = downList
                found = isVideo
                prefix = prefix
                audioCount = audioCount
            }
            if (anyAdded) {
                try {
                    Collections.sort<DownListItem?>(foundItems, SortSub())
                } catch (unused: Exception) {
                }
                val foundItemsSize = foundItems.size
                var newIndex = 0
                var index = 0
                while (index < foundItemsSize) {
                    val entry: Any? = foundItems.get(index)
                    index++
                    val downItem = entry as DownListItem
                    if (!this.live) {
                        return
                    }
                    downItem.index = newIndex
                    newIndex++
                }
            }
            this.foundItems = foundItems
            this.foundState = foundState
        }
    }

    fun markDownloaded() {
        val arrayList = this.foundItems
        val webNestView: WebNestView? = if (arrayList == null || arrayList.isEmpty()) null else this.webNest
        if (arrayList == null || arrayList.isEmpty() || webNestView == null) {
            return
        }
        val downloaded = webNestView.downloaded
        val hasDownloaded = if (downloaded == null || downloaded.isEmpty()) false else true
        val downFail = webNestView.downFail
        val hasFailed = if (downFail == null || downFail.isEmpty()) false else true
        val size = arrayList.size
        var index = 0
        while (index < size) {
            val entry: Any? = arrayList.get(index)
            index++
            val downListItem = entry as DownListItem
            if (!this.live) {
                return
            }
            val url = downListItem.url
            if (url != null) {
                if (hasFailed && downFail!!.contains(url)) {
                    downListItem.checkState = 2.toByte()
                } else if (hasDownloaded && downloaded!!.contains(url)) {
                    downListItem.checkState = 1.toByte()
                } else {
                    downListItem.checkState = 0.toByte()
                }
            }
        }
    }

    fun startVkv(viewGroup: ViewGroup?, vkvListListener: VkvListListener?) {
        this.scanState = 5.toByte()
        this.vkvListener = vkvListListener
        if (this.vkvLoad != null || viewGroup == null) {
            return
        }
        this.vkvLoad = WebVkvLoad(this.hostActivity, viewGroup, this.pageUrl, VkvLoadListener())
    }

    fun applyFound() {
        if (!live) return
        val arrayList = foundItems
        val stateNow = foundState
        foundItems = null
        applyBusy(false)
        if (arrayList == null || arrayList.isEmpty()) {
            if (scanState.toInt() == 0) videoListener?.onScanEmpty()
            showEmpty()
            return
        }
        if (arrayList.size != 1) {
            shownItems = arrayList
            listMode = stateNow.toInt()
            items = arrayList.map { it as DownListItem }
            return
        }
        if (videoListener != null) {
            val downListItem = arrayList[0]
            if (downListItem == null) {
                showEmpty()
                return
            }
            if (actionBusy) return
            actionBusy = true
            downItem = downListItem
            ui.post(DownloadTask())
        }
    }

    /** Loading on / off (old progress overlay). */
    fun applyBusy(newBusy: Boolean) {
        if (!live) return
        busy = newBusy
    }

    /** "No video" message (once). */
    fun showEmpty() {
        if (emptyText != 0 || !live) return
        items = emptyList()
        emptyText = if (listType.toInt() == 0) AppR.string.no_down_video else AppR.string.video_link_1
    }

    fun close() {
        live = false
        if (appContext == null) {
            super.dismiss()
            return
        }
        closeDownLink()
        loadTask?.cancelled = true
        loadTask = null
        stopVkv()
        hostActivity = null
        appContext = null
        videoListener = null
        webNest = null
        pageUrl = null
        shownItems = null
        super.dismiss()
    }

    override fun dismiss() {
        if (dismissed) return
        dismissed = true
        close()
    }

    companion object {
        @JvmStatic
        fun isDummyUrl(url: String?): Boolean {
            return url == null || url == "vid_dummy" || url == "pub_dummy" || url == "pgf_dummy" || url == "vkv_dummy"
        }
    }
}
