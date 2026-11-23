package com.mycompany.app.main

import android.content.pm.ServiceInfo

import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import java.util.ArrayList
import java.util.Comparator
import ch.opumlabs.opumbrowser.R as AppR
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.gson.JsonObject
import com.mycompany.app.data.DataUtil.addUri
import com.mycompany.app.data.DataUtil.fileKind
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.DbUtil.existsWhere
import com.mycompany.app.db.DbUtil.queryById
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.db.book.DbBookDown
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.db.book.DbBookDown.Companion.hasPath
import com.mycompany.app.db.book.DbBookDown.Companion.setStatus
import com.mycompany.app.db.book.DbBookDown.Companion.setSize
import com.mycompany.app.db.book.DbBookDown.Companion.insertItem
import com.mycompany.app.db.book.DbBookSub.Companion.putSub
import com.mycompany.app.down.DownParseDzen
import com.mycompany.app.down.DownParseDzen.segmentUrls
import com.mycompany.app.down.DownParseKakao
import com.mycompany.app.down.DownParseKakao.segmentUrls
import com.mycompany.app.down.DownParseM3u8
import com.mycompany.app.down.DownParseM3u8.Companion.queryOf
import com.mycompany.app.down.DownParseM3u8.DownParseListener
import com.mycompany.app.down.DownParseReddit
import com.mycompany.app.down.DownParseTsfile.findSegments
import com.mycompany.app.down.DownParseVimeo.vimeoItem
import com.mycompany.app.down.DownParseVimeo.vimeoSegments
import com.mycompany.app.down.DownSaveAudio.saveAudio
import com.mycompany.app.down.DownSaveImage.saveImageAt
import com.mycompany.app.down.DownSavePath.savePathPart
import com.mycompany.app.down.DownSaveSplit.saveSegment
import com.mycompany.app.down.DownSaveSplit.encItemAt
import com.mycompany.app.down.DownSaveSplit.fetchBytes
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUri.NumItem
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.pref.PrefPath
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.pref.PrefUtil.foldMode
import com.mycompany.app.pref.PrefUtil.setSingleLine
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.PrefZone
import java.io.File
import com.mycompany.app.down.DownState
import com.mycompany.app.down.DownTransferJob
import com.mycompany.app.down.DownHls
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.Collections
import java.util.Locale
import java.util.concurrent.ExecutorService
import kotlin.math.max

@Suppress("deprecation")
class MainDownSvc : Service() {
    @JvmField
    var mergeTotal: Int = 0
    @JvmField
    var mergeDone: Int = 0
    @JvmField
    var mergeFail: Int = 0
    @JvmField
    var mergeDoneBase: Int = 0
    @JvmField
    var mergeFailBase: Int = 0
    @JvmField
    var mergeNotiTime: Long = 0
    @JvmField
    var mergeToastTime: Long = 0
    @JvmField
    var mergeNoti: NotificationCompat.Builder? = null
    @JvmField
    var eventReceiver: EventReceiver? = null
    @JvmField
    var hourText: String? = null
    @JvmField
    var minuteText: String? = null
    @JvmField
    var secondText: String? = null
    @JvmField
    var downloadTitle: String? = null
    @JvmField
    var executorCache: ExecutorService? = null
    @JvmField
    var restartPending: Boolean = false
    @JvmField
    var appCtx: Context? = null
    @JvmField
    var started: Boolean = false
    @JvmField
    var keepAlive: Boolean = false
    @JvmField
    var stopping: Boolean = false
    @JvmField
    var stopPending: Boolean = false
    @JvmField
    var uiHandler: Handler? = null
    @JvmField
    var timerTime: Long = 0
    @JvmField
    var busyListener: DownBusyListener? = null
    @JvmField
    var listListener: DownListListener? = null
    @JvmField
    var downItems: ArrayList<Any?>? = null
    @JvmField
    var runningItems: ArrayList<Any?>? = null
    @JvmField
    var waitingItems: ArrayList<Any?>? = null
    @JvmField
    var finishedItems: ArrayList<Any?>? = null
    @JvmField
    var pendingItems: ArrayList<Any?>? = null
    @JvmField
    var waitingNoti: Boolean = false
    @JvmField
    var busyState: Byte = 0
    @JvmField
    var lastProgressTime: Long = 0
    @JvmField
    var inForeground: Boolean = false
    @JvmField
    var merging: Boolean = false
    @JvmField
    var mergeQueue: ArrayList<ImageItem?>? = null
    @JvmField
    var mergeDir: String? = null
    @JvmField
    val foregroundLock: Any = Any()
    @JvmField
    val itemLock: Any = Any()
    @JvmField
    val runningLock: Any = Any()
    @JvmField
    val progressLock: Any = Any()
    @JvmField
    val fileLock: Any = Any()
    @JvmField
    val timerRunnable: Runnable = TimerTick()
    @JvmField
    val progressRunnable: Runnable = UpdateNotiTask()

    internal inner class CreateSetupTask : Runnable {
        override fun run() {
            val mainDownSvc = this@MainDownSvc
            if (mainDownSvc.uiHandler == null) {
                mainDownSvc.uiHandler = Handler(Looper.getMainLooper())
            }
            mainDownSvc.updateForeground()
            val context = mainDownSvc.appCtx
            val notificationManagerP: NotificationManager? = if (context != null) MainApp.Companion.notifications(context) else null
            if (context != null && notificationManagerP != null) {
                notificationManagerP.cancel(2147483643)
            }
            if (mainDownSvc.eventReceiver == null) {
                try {
                    mainDownSvc.eventReceiver = mainDownSvc.EventReceiver()
                    val intentFilter = IntentFilter()
                    intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_DOWN_PAUSE")
                    intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_DOWN_RETRY")
                    intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_DOWN_CANCEL")
                    intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_DOWN_EXIT")
                    intentFilter.addAction("ch.opumlabs.opumbrowser.ACTION_IMAGE_CANCEL")
                    intentFilter.addAction("android.intent.action.SCREEN_ON")
                    intentFilter.addAction("android.intent.action.SCREEN_OFF")
                    ContextCompat.registerReceiver(mainDownSvc, mainDownSvc.eventReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED)
                } catch (unused: Exception) {
                }
            }
            mainDownSvc.keepAlive = false
        }
    }

    internal inner class StartDownloadTask(@JvmField val outer: MainDownSvc,
                                          @JvmField val downItem: DownItem
    ) : Runnable {

        override fun run() {
            var m3u8ItemB: M3u8Item?
            var arrayListA: ArrayList<String?>?
            val downItem: DownItem
            var downItemNow = this.downItem
            val urlType = downItemNow.urlType
            val mainDownSvc = this.outer
            if (urlType == 0 || urlType == 15) {
                mainDownSvc.startDownload(downItemNow)
                return
            }
            if (urlType == 13) {
                return
            }
            if (urlType == 14) {
                if (TextUtils.isEmpty(downItemNow.url) || TextUtils.isEmpty(downItemNow.path) || mainDownSvc.appCtx == null || mainDownSvc.uiHandler == null) {
                    return
                }
                mainDownSvc.showProgressNoti(downItemNow)
                downItemNow.uriItem!!.javaClass
                downItemNow.status = 4
                mainDownSvc.finishItem(downItemNow)
                return
            }
            if (TextUtils.isEmpty(downItemNow.url) || TextUtils.isEmpty(downItemNow.path) || mainDownSvc.appCtx == null || mainDownSvc.uiHandler == null) {
                return
            }
            downItemNow.isRunning = true
            mainDownSvc.showProgressNoti(downItemNow)
            val urlTypeNow = downItemNow.urlType
            if (urlTypeNow == 1) {
                val parseItem = ParseItem()
                val itemUrl = MainUtil.urlFromText(downItemNow.url)
                if (downItemNow.url!!.startsWith("izle:")) {
                    parseItem.base = itemUrl
                    parseItem.altAudio = true
                } else if (downItemNow.url!!.endsWith("_mp4_hd.audio.m3u8")) {
                    parseItem.hdAudio = true
                }
                var isFake = false
                val itemQuery = queryOf(itemUrl)
                val downParseM3u = DownParseM3u8(mainDownSvc.appCtx)
                val url = downItemNow.url
                if (!TextUtils.isEmpty(url)) {
                    isFake = url!!.startsWith("m3fake:")
                }
                downParseM3u.fakeM3u8 = isFake
                downParseM3u.parseMaster(
                    itemUrl,
                    downItemNow.referer,
                    itemQuery,
                    ReleaseStep(
                        mainDownSvc,
                        downParseM3u,
                        downItemNow,
                        itemUrl,
                        itemQuery ?: "", // no query in the address (was !!: every such HLS download crashed the app)
                        parseItem
                    )
                )
                return
            }
            if (urlTypeNow == 2) {
                val m3u8ItemL = DownParseM3u8.parseItemText(downItemNow.url)
                if (m3u8ItemL == null) {
                    mainDownSvc.startM3u8(downItemNow, m3u8ItemL, null, null)
                    return
                }
                val m3u8Query = queryOf(m3u8ItemL.url)
                val downParseM3u = DownParseM3u8(mainDownSvc.appCtx)
                downParseM3u.parseSegments(
                    null,
                    m3u8ItemL.url,
                    downItemNow.referer,
                    m3u8Query ?: "",
                    m3u8ItemL.src,
                    ResolveKeysStep(
                        mainDownSvc,
                        downParseM3u,
                        downItemNow,
                        m3u8ItemL,
                        m3u8Query ?: ""
                    )
                )
                return
            }
            if (urlTypeNow == 4) {
                m3u8ItemB = DownParseReddit.parseRedditItem(downItemNow.url)
                if (m3u8ItemB != null) {
                    arrayListA = DownParseReddit.redditSegments(m3u8ItemB.src)
                } else {
                    arrayListA = null
                }
            } else if (urlTypeNow == 6) {
                m3u8ItemB = DownParseKakao.parseItemText(downItemNow.url)
                if (m3u8ItemB != null) {
                    downItem = downItemNow
                    arrayListA = DownParseKakao.segmentUrls(
                        mainDownSvc.appCtx,
                        m3u8ItemB.url,
                        downItemNow.referer,
                        m3u8ItemB.template,
                        m3u8ItemB.src,
                        downItem,
                        null
                    )
                    downItemNow = downItem
                } else {
                    arrayListA = null
                }
            } else {
                if (urlTypeNow == 8) {
                    m3u8ItemB = DownParseDzen.parseItemText(downItemNow.url)
                    if (m3u8ItemB != null) {
                        downItem = downItemNow
                        arrayListA = DownParseDzen.segmentUrls(
                            mainDownSvc.appCtx,
                            m3u8ItemB.url,
                            downItemNow.referer,
                            m3u8ItemB.template,
                            m3u8ItemB.src,
                            downItem,
                            null
                        )
                        downItemNow = downItem
                    }
                } else if (urlTypeNow == 10) {
                    val vimeoM3u8 = vimeoItem(mainDownSvc.appCtx, downItemNow.url, downItemNow.referer)
                    if (vimeoM3u8 != null) {
                        arrayListA = vimeoSegments(vimeoM3u8.vimeoJson, vimeoM3u8.base, vimeoM3u8.src, false)
                        m3u8ItemB = vimeoM3u8
                    } else {
                        m3u8ItemB = vimeoM3u8
                    }
                } else {
                    if (urlTypeNow == 11) {
                        arrayListA =
                            findSegments(mainDownSvc.appCtx, downItemNow.url, downItemNow.referer, downItemNow, false, null)
                    } else if (urlTypeNow == 12) {
                        arrayListA =
                            findSegments(mainDownSvc.appCtx, downItemNow.url, downItemNow.referer, downItemNow, true, null)
                    } else {
                        m3u8ItemB = null
                        arrayListA = null
                    }
                    m3u8ItemB = null
                }
                arrayListA = null
            }
            mainDownSvc.startM3u8(downItemNow, m3u8ItemB, null, arrayListA)
        }
    }

    internal inner class FinishItemStep : DownSaveListener {
        override fun uiHandler(): Handler? {
            return this@MainDownSvc.uiHandler
        }

        override fun onStarted(downItem: DownItem?) {
            if (downItem == null || downItem.status == 4) {
                return
            }
            downItem.status = 4
            this@MainDownSvc.finishItem(downItem)
        }

        override fun onPaused(downItem: DownItem?) {
            if (downItem == null || downItem.status == 2) {
                return
            }
            downItem.status = 2
            this@MainDownSvc.finishItem(downItem)
        }

        override fun onFinished(downItem: DownItem?) {
            if (downItem == null || downItem.status != 1) {
                return
            }
            val mainDownSvc = this@MainDownSvc
            mainDownSvc.onItemProgress(downItem)
            mainDownSvc.notifyItemChanged(downItem)
        }

        override fun onFailed(downItem: DownItem?) {
            var mainDownSvc: MainDownSvc? = null
            val handler: Handler? = if (downItem == null || downItem.status != 1) null else (this@MainDownSvc.also {
                    mainDownSvc = it
                }).uiHandler
            if (downItem == null || downItem.status != 1 || handler == null) {
                return
            }
            handler.postDelayed(ItemDoneTask(mainDownSvc!!, downItem), 200L)
        }
    }

    internal inner class SavePartStep(@JvmField val outer: MainDownSvc,
                                          @JvmField val downItem: DownItem?,
                                          @JvmField val url: String?,
                                          @JvmField val part: Int,
                                          @JvmField val digits: Int,
                                          @JvmField val listener: DownSaveListener
    ) : Runnable {
        override fun run() {
            savePathPart(this.outer.appCtx, this.downItem, this.url, this.part, this.digits, 0, 0, false, this.listener)
        }
    }

    internal inner class SaveAudioTask(@JvmField val outer: MainDownSvc,
                                          @JvmField val downItem: DownItem?,
                                          @JvmField val url: String?,
                                          @JvmField val listener: DownSaveListener
    ) : Runnable {
        override fun run() {
            saveAudio(this.outer.appCtx, this.downItem, this.url, 0, 0, false, this.listener)
        }
    }

    internal inner class ReleaseStep(@JvmField val outer: MainDownSvc,
                                          @JvmField val downParseM3u8: DownParseM3u8,
                                          @JvmField val downItem: DownItem,
                                          @JvmField val playlistUrl: String?,
                                          @JvmField val query: String,
                                          @JvmField val parseItem: ParseItem?
    ) : DownParseListener {
        override fun onParsed(list: MutableList<String?>?) {
            val mainDownSvc = this.outer
            val downParseM3u = this.downParseM3u8
            val downItem = this.downItem
            if ((list == null || list.isEmpty()) && downParseM3u.fakeM3u8) {
                downParseM3u.release()
                downItem.url = MainUtil.urlFromText(downItem.url)
                downItem.urlType = 0
                downItem.status = 1
                mainDownSvc.startDownload(downItem)
                return
            }
            if (downParseM3u.masterFound) {
                val isApi = MainConst.isApi28
                downItem.referer = null
            }
            val encKeys = downParseM3u.encKeys
            if (encKeys != null && !encKeys.isEmpty()) {
                downItem.videoKeys = downParseM3u.resolveKeys(this.playlistUrl, downItem.referer, this.query)
                downItem.videoSegments = downParseM3u.segmentList
            }
            val live = downParseM3u.liveList && list != null && !list.isEmpty()
            downParseM3u.release()
            if (live) {
                mainDownSvc.failLive(downItem)
                return
            }
            mainDownSvc.startM3u8(downItem, null, this.parseItem, list)
        }
    }

    internal inner class ResolveKeysStep(@JvmField val outer: MainDownSvc,
                                          @JvmField val downParseM3u8: DownParseM3u8,
                                          @JvmField val downItem: DownItem,
                                          @JvmField val m3u8Item: M3u8Item,
                                          @JvmField val query: String
    ) : DownParseListener {
        override fun onParsed(list: MutableList<String?>?) {
            val downParseM3u = this.downParseM3u8
            val masterFound = downParseM3u.masterFound
            val downItem = this.downItem
            if (masterFound) {
                val isApi = MainConst.isApi28
                downItem.referer = null
            }
            val encKeys = downParseM3u.encKeys
            val m3u8Item = this.m3u8Item
            if (encKeys != null && !encKeys.isEmpty()) {
                downItem.videoKeys = downParseM3u.resolveKeys(m3u8Item.url, downItem.referer, this.query)
                downItem.videoSegments = downParseM3u.segmentList
            }
            val live = downParseM3u.liveList && list != null && !list.isEmpty()
            downParseM3u.release()
            if (live) {
                this.outer.failLive(downItem)
                return
            }
            this.outer.startM3u8(downItem, m3u8Item, null, list)
        }
    }

    internal inner class StartSegmentsStep(@JvmField val outer: MainDownSvc,
                                          @JvmField val parser: DownParseM3u8,
                                          @JvmField val downItem: DownItem,
                                          @JvmField val items: MutableList<String?>
    ) : DownParseListener {
        override fun onParsed(list: MutableList<String?>?) {
            this.parser.release()
            this.outer.startSegments(this.downItem, this.items, list)
        }
    }

    internal inner class CheckKeysStep(@JvmField val outer: MainDownSvc,
                                          @JvmField val downParseM3u8: DownParseM3u8,
                                          @JvmField val downItem: DownItem,
                                          @JvmField val m3u8Item: M3u8Item,
                                          @JvmField val query: String,
                                          @JvmField val items: MutableList<String?>
    ) : DownParseListener {
        override fun onParsed(list: MutableList<String?>?) {
            val downParseM3u = this.downParseM3u8
            val encKeys = downParseM3u.encKeys
            val downItem = this.downItem
            if (encKeys != null && !encKeys.isEmpty()) {
                downItem.audioKeys = downParseM3u.resolveKeys(this.m3u8Item.url, downItem.referer, this.query)
                downItem.audioSegments = downParseM3u.segmentList
            }
            downParseM3u.release()
            this.outer.startSegments(downItem, this.items, list)
        }
    }

    internal inner class SegmentsDoneStep : DownSaveListener {
        override fun uiHandler(): Handler? {
            return this@MainDownSvc.uiHandler
        }

        override fun onStarted(downItem: DownItem?) {
            if (downItem == null || downItem.status == 4) {
                return
            }
            downItem.status = 4
            this@MainDownSvc.finishItem(downItem)
        }

        override fun onPaused(downItem: DownItem?) {
            if (downItem == null || downItem.status == 2) {
                return
            }
            downItem.status = 2
            this@MainDownSvc.finishItem(downItem)
        }

        override fun onFinished(downItem: DownItem?) {
            if (downItem == null || downItem.status != 1) {
                return
            }
            val mainDownSvc = this@MainDownSvc
            mainDownSvc.onItemProgress(downItem)
            mainDownSvc.notifyItemChanged(downItem)
        }

        override fun onFailed(downItem: DownItem?) {
            var mainDownSvc: MainDownSvc? = null
            val handler: Handler? = if (downItem == null || downItem.status != 1) null else (this@MainDownSvc.also {
                    mainDownSvc = it
                }).uiHandler
            if (downItem == null || downItem.status != 1 || handler == null) {
                return
            }
            handler.postDelayed(ItemDoneTask(mainDownSvc!!, downItem), 200L)
        }
    }

    internal inner class StartedTask(@JvmField val outer: MainDownSvc,
                                          @JvmField val downItem: DownItem,
                                          @JvmField val items: MutableList<String?>,
                                          @JvmField val width: Int,
                                          @JvmField val height: Int,
                                          @JvmField val digits: Int,
                                          @JvmField val listener: DownSaveListener
    ) : Runnable {
        override fun run() {
            var downItem: DownItem
            var downSaveListener: DownSaveListener
            val context = this.outer.appCtx
            val digits = this.digits
            val list = this.items
            val size = list.size
            var step = 0
            while (true) {
                downItem = this.downItem
                val width = this.width
                downSaveListener = this.listener
                val index = if (step >= width || downItem.status != 1) 0 else ((downItem.oldParts * step) + this.height)
                if (step >= width || downItem.status != 1 || index >= size) {
                    break
                }
                val audioStart = downItem.audioStart
                try {
                    saveSegment(
                        context,
                        downItem,
                        list.get(index),
                        index,
                        digits,
                        index >= audioStart,
                        downItem.urlType == 10 && (index == 0 || index == audioStart),
                        0,
                        0,
                        false,
                        downSaveListener
                    )
                    step++
                } catch (unused: OutOfMemoryError) {
                    downItem.outOfMemory = true
                    (downSaveListener as SegmentsDoneStep).onStarted(downItem)
                    return
                }
            }
            if (downItem.status == 1) {
                (downSaveListener as SegmentsDoneStep).onFailed(downItem)
            }
        }
    }

    internal inner class StartSetupTask : Runnable {
        override fun run() {
            val mainDownSvc = this@MainDownSvc
            if (mainDownSvc.uiHandler == null) {
                mainDownSvc.uiHandler = Handler(Looper.getMainLooper())
            }
            mainDownSvc.updateForeground()
            val mainAppO: MainApp? = MainApp.Companion.from(mainDownSvc.appCtx)
            if (mainAppO != null) {
                if (!mainAppO.downBinding) {
                    mainAppO.downBinding = true
                    mainAppO.downStarting = true
                    mainAppO.pendingRefresh = true
                    mainAppO.pendingActionId = 0L
                    mainAppO.pendingAction = 0.toByte()
                    mainAppO.pendingDownUrl = null
                    mainAppO.pendingDownName = null
                    mainAppO.pendingDownItem = null
                    mainAppO.pendingDownMulti = 0
                    mainAppO.pendingDownList = null
                }
                mainAppO.downService = mainDownSvc
                val downBusyListener = mainAppO.downBusyListener
                val downListListener = mainAppO.downListListener
                mainDownSvc.busyListener = downBusyListener
                mainDownSvc.listListener = downListListener
                mainAppO.execute(mainAppO.LoadSavedTask())
            }
        }
    }

    internal inner class ItemDoneTask(@JvmField val outer: MainDownSvc,
                                          @JvmField val downItem: DownItem
    ) : Runnable {

        internal inner class CompleteItemStep(@JvmField val outer: ItemDoneTask,
                                             @JvmField val downItem: DownItem
        ) : Runnable {
            override fun run() {
                val wasResumed: Boolean
                val fileArrListFiles: Array<File>? = if (this.downItem.status != 1) null else File(this.downItem.segmentDir!!).listFiles()
                if (this.downItem.status != 1 || fileArrListFiles == null || fileArrListFiles.size == 0) {
                    return
                }
                val downItem = this.downItem
                var partCount = downItem.partCount
                if (downItem.urlType == 15) {
                    partCount++
                }
                var path: String? = null
                var index = 0
                for (file in fileArrListFiles) {
                    val name = file.getName()
                    if (!TextUtils.isEmpty(name) && name.endsWith("f")) {
                        if (this.downItem.urlType == 15 && name.endsWith("af") && file.length() != 0L) {
                            path = file.getPath()
                        }
                        index++
                    }
                }
                if (index < partCount || this.outer.outer.appCtx == null) {
                    return
                }
                if (partCount == 1) {
                    val firstFile: File? = fileArrListFiles[0]
                    val firstPath = if (firstFile != null) firstFile.getPath() else null
                    wasResumed = true
                    path = firstPath
                } else {
                    wasResumed = false
                }
                val mainDownSvc = this.outer.outer
                val downItemNow = this.downItem
                try {
                    synchronized(mainDownSvc.fileLock) {
                        if (downItemNow.status != 1) {
                            return
                        }
                        downItemNow.status = 7
                        downItemNow.isMerging = false
                        downItemNow.wasResumed = wasResumed
                        downItemNow.tempPath = path
                        val arrayList = mainDownSvc.pendingItems
                        if (arrayList == null) {
                            val pendingItems: ArrayList<Any?> = ArrayList<Any?>()
                            mainDownSvc.pendingItems = pendingItems
                            pendingItems.add(downItemNow)
                        } else {
                            val noItems = arrayList.isEmpty()
                            val pending = mainDownSvc.pendingItems!!
                            if (!noItems) {
                                if (pending.contains(downItemNow)) {
                                    return
                                }
                                mainDownSvc.pendingItems!!.add(downItemNow)
                                return
                            }
                            pending.add(downItemNow)
                        }
                        this.outer.outer.onItemDone(this.downItem)
                    }
                } catch (unused: Exception) {
                }
            }
        }

        override fun run() {
            val downItem = this.downItem
            if (downItem.status != 1) {
                return
            }
            val completeItemStep: CompleteItemStep = CompleteItemStep(this, downItem)
            this.outer.runInBackground(completeItemStep)
        }
    }

    internal inner class RunNextPendingTask : Runnable {

        internal inner class NextPendingStep : Runnable {
            override fun run() {
                val mainDownSvc = this@MainDownSvc
                var downItem: DownItem? = null
                try {
                    synchronized(mainDownSvc.fileLock) {
                        val arrayList = mainDownSvc.pendingItems
                        if (arrayList != null && !arrayList.isEmpty()) {
                            downItem = mainDownSvc.pendingItems!!.get(0) as DownItem?
                        }
                    }
                } catch (unused: Exception) {
                }
                mainDownSvc.onItemDone(downItem)
            }
        }

        override fun run() {
            val nextPendingStep: NextPendingStep = NextPendingStep()
            this@MainDownSvc.runInBackground(nextPendingStep)
        }
    }

    internal inner class ItemProgressTask(@JvmField val outer: MainDownSvc,
                                          @JvmField val downItem: DownItem
    ) : Runnable {

        internal inner class ProgressStep(@JvmField val outer: ItemProgressTask,
                                             @JvmField val downItem: DownItem,
                                             @JvmField val type: String?
        ) : Runnable {
            override fun run() {
                val mainDownSvc = this.outer.outer
                if (mainDownSvc.appCtx != null) {
                    val downItem = this.downItem
                    mainDownSvc.onItemProgress(downItem)
                    mainDownSvc.notifyComplete(downItem, this.type)
                    if (downItem.hasSub) {
                        downItem.hasSub = false
                        mainDownSvc.notifyBusy(true)
                    }
                    val status = downItem.status
                    if (status == 2 || status == 4) {
                        return
                    }
                    mainDownSvc.updateItem(downItem)
                }
            }
        }

        override fun run() {
            val mime: String?
            val mainDownSvc = this.outer
            val context = mainDownSvc.appCtx
            if (context == null) {
                return
            }
            val downItem = this.downItem
            val status = downItem.status
            if (status == 2) {
                mime = null
            } else {
                if (status == 3) {
                    if (!ZoneSettings.notiDown.value) {
                        MainUtil.showToast(context, AppR.string.down_complete)
                    }
                } else if (status == 4) {
                    MainUtil.showToast(mainDownSvc.appCtx, errorText(downItem))
                } else if (status == 5) {
                    MainUtil.showToast(context, AppR.string.invalid_url)
                }
                val uriItem: UriItem? = if (downItem.status != 3) null else downItem.uriItem
                if (downItem.status != 3 || uriItem == null) {
                    mime = null
                } else {
                    mime = MainUtil.mimeForUrl(uriItem.name)
                }
            }
            mainDownSvc.runInBackground(ProgressStep(this, downItem, mime))
        }
    }

    internal inner class PauseBusyToastTask : Runnable {
        override fun run() {
            MainUtil.showToast(this@MainDownSvc.appCtx, AppR.string.wait_retry)
        }
    }

    internal inner class CancelBusyToastTask : Runnable {
        override fun run() {
            MainUtil.showToast(this@MainDownSvc.appCtx, AppR.string.wait_retry)
        }
    }

    internal inner class TimerTick : Runnable {
        override fun run() {
            val mainDownSvc = this@MainDownSvc
            val runnable = mainDownSvc.timerRunnable
            val handler = mainDownSvc.uiHandler
            if (handler != null) {
                handler.removeCallbacks(runnable)
                val timerTime = mainDownSvc.timerTime
                if (timerTime == 0L) {
                    return
                }
                if (timerTime != 0L) {
                    var index = BrowserSettings.downBack.value
                    if (index < 1) {
                        index = 1
                    } else if (index > 5) {
                        index = 5
                    }
                    if (System.currentTimeMillis() - timerTime >= (index.toLong()) * 3600000) {
                        try {
                            val arrayList = mainDownSvc.downItems
                            if (arrayList != null && !arrayList.isEmpty()) {
                                val size = arrayList.size
                                var itemIndex = 0
                                var active = false
                                while (itemIndex < size) {
                                    val rawDownItem: Any? = arrayList.get(itemIndex)
                                    itemIndex++
                                    val downItem = rawDownItem as DownItem?
                                    if (downItem != null && downItem.cancelRequested) {
                                        active = true
                                        break
                                    }
                                }
                                if (!active) {
                                    mainDownSvc.stopAll(false, true)
                                    return
                                }
                            }
                        } catch (unused: Exception) {
                        }
                    }
                }
                handler.postDelayed(runnable, 600000L)
            }
        }
    }

    internal inner class DownloadProgressTask(@JvmField val outer: MainDownSvc,
                                          @JvmField val downItem: DownItem?
    ) : Runnable {
        override fun run() {
            val downItem = this.downItem
            if (downItem == null || downItem.totalSize == 0L || downItem.lastTime != 0L || downItem.status != 1) {
                return
            }
            downItem.lastTime = System.currentTimeMillis()
            val mainDownSvc = this.outer
            mainDownSvc.onItemProgress(downItem)
            mainDownSvc.notifyItemChanged(downItem)
        }
    }

    internal inner class UpdateNotiTask : Runnable {

        internal inner class UpdateNotiStep : Runnable {

            override fun run() {
                val mainDownSvc = this@MainDownSvc
                val busy =
                    mainDownSvc.busyState.toInt() == 2 || (mainDownSvc.busyListener != null && mainDownSvc.busyListener!!.hasFocus())
                if (busy) {
                    return
                }
                val wait = mainDownSvc.waitingNoti || (mainDownSvc.busyListener != null && mainDownSvc.busyListener!!.isWaiting())
                if (wait) {
                    val handler = mainDownSvc.uiHandler
                    if (handler == null) {
                        return
                    }
                    handler.removeCallbacks(mainDownSvc.progressRunnable)
                    mainDownSvc.uiHandler!!.postDelayed(mainDownSvc.progressRunnable, 400L)
                    return
                }
                var item: DownItem? = null
                try {
                    synchronized(mainDownSvc.progressLock) {
                        val arrayList = mainDownSvc.finishedItems
                        if (arrayList == null || arrayList.isEmpty()) {
                            return
                        }
                        item = arrayList.removeAt(0) as DownItem?
                    }
                } catch (unused: Exception) {
                }
                if (item != null && (item.status == 1 || item.status == 7)) {
                    mainDownSvc.updateNoti(item)
                }
                mainDownSvc.flushFinished(true)
            }
        }

        override fun run() {
            val updateNotiStep: UpdateNotiStep = UpdateNotiStep()
            this@MainDownSvc.runInBackground(updateNotiStep)
        }
    }

    internal inner class CancelMergeStep : DownImageListener {
        override fun uiHandler(): Handler? {
            return this@MainDownSvc.uiHandler
        }

        override fun onImagesSaved(arrayList: ArrayList<*>?, isOk: Boolean) {
            var index: Int = 0
            var builder: NotificationCompat.Builder? = null
            val size = arrayList!!.size
            var finished = 0
            var failed = 0
            var itemIndex = 0
            while (itemIndex < size) {
                val rawImageItem: Any? = arrayList.get(itemIndex)
                itemIndex++
                val imageItem = rawImageItem as ImageItem?
                if (!this@MainDownSvc.merging) {
                    return
                }
                if (imageItem != null) {
                    val state = imageItem.state
                    if (state.toInt() == 3) {
                        finished++
                        continue
                    } else if (state.toInt() != 4) {
                        continue
                    }
                }
                finished++
                failed++
            }
            val mainDownSvc = this@MainDownSvc
            mainDownSvc.mergeDone = mainDownSvc.mergeDoneBase + finished
            mainDownSvc.mergeFail = mainDownSvc.mergeFailBase + failed
            val total = arrayList.size
            val svc = this@MainDownSvc
            if (finished >= total) {
                synchronized(svc.itemLock) {
                    val svcQueue = this@MainDownSvc
                    if (svcQueue.merging) {
                        val mergeQueue = svcQueue.mergeQueue
                        if (mergeQueue == null || mergeQueue.isEmpty()) {
                            this@MainDownSvc.cancelMerge()
                        } else {
                            val svcMerge = this@MainDownSvc
                            val mergeQueueNow = svcMerge.mergeQueue
                            svcMerge.mergeQueue = null
                            svcMerge.startMerge(mergeQueueNow, isOk)
                        }
                        return
                    }
                    return
                }
            }
            val doneNow = if (svc.merging) svc.mergeDone else 0
            if (svc.merging && doneNow < (svc.mergeTotal.also {
                    index = it
                }) && (svc.mergeNoti.also { builder = it }) != null && svc.appCtx != null) {
                if (index < 0) {
                    svc.mergeTotal = 0
                    index = 0
                }
                if (doneNow > index) {
                    svc.mergeDone = index
                }
                if (svc.mergeFail > index) {
                    svc.mergeFail = index
                }
                val now = System.currentTimeMillis()
                if (now - svc.mergeNotiTime >= 500) {
                    svc.mergeNotiTime = now
                    val sb = StringBuilder()
                    sb.append(svc.getString(AppR.string.success))
                    sb.append(" (")
                    sb.append(svc.mergeDone - svc.mergeFail)
                    sb.append(")  ")
                    sb.append(svc.getString(AppR.string.fail))
                    sb.append(" (")
                    sb.append(svc.mergeFail)
                    sb.append(")")
                    builder!!.setProgress(svc.mergeTotal, svc.mergeDone, false)
                    builder.setContentTitle(
                        svc.mergeDone.toString() + " / " + svc.mergeTotal + " - " + svc.getString(
                            AppR.string.down_image
                        )
                    )
                    builder.setContentText(sb.toString())
                    val notificationB = builder.build()
                    setNotiFlags(notificationB, 1)
                    val notificationManagerP: NotificationManager? =
                        MainApp.Companion.notifications(svc.appCtx)
                    if (notificationManagerP != null) {
                        notificationManagerP.notify(2147483644, notificationB)
                    }
                }
            }
            val now = System.currentTimeMillis()
            val svcToast = this@MainDownSvc
            if (now - svcToast.mergeToastTime > 2000) {
                svcToast.mergeToastTime = now
                svcToast.notifyBusy(true)
            }
        }

        override val isRunning: Boolean
            get() = this@MainDownSvc.merging
    }

    internal inner class SaveImageAtTask(@JvmField val outer: MainDownSvc,
                                          @JvmField val items: ArrayList<ImageItem?>,
                                          @JvmField val tmpPath: String?,
                                          @JvmField val count: Int,
                                          @JvmField val step: Int,
                                          @JvmField val offset: Int,
                                          @JvmField val isOk: Boolean,
                                          @JvmField val downImageListener: DownImageListener
    ) : Runnable {
        override fun run() {
            val context = this.outer.appCtx
            val tmpPath = this.tmpPath
            val isOk = this.isOk
            val sessionId = SecretSettings.secretDown.value
            val arrayList = this.items
            val size = arrayList.size
            for (index in 0..<this.count) {
                val downImageListener = this.downImageListener
                val cancelStep = downImageListener as CancelMergeStep?
                val imageIndex = if (!this@MainDownSvc.merging) 0 else ((this.step * index) + this.offset)
                if (!this@MainDownSvc.merging || imageIndex >= size) {
                    return
                }
                try {
                    saveImageAt(context, arrayList, tmpPath, imageIndex, isOk, sessionId, 0, 0, downImageListener)
                } catch (unused: OutOfMemoryError) {
                    val imageItem = arrayList.get(imageIndex)
                    if (imageItem == null) {
                        return
                    }
                    imageItem.state = 4.toByte()
                    cancelStep!!.onImagesSaved(arrayList, isOk)
                }
            }
        }
    }

    internal inner class StopIfIdleTask : Runnable {
        override fun run() {
            this@MainDownSvc.stopIfIdle(false)
        }
    }

    internal inner class StopServiceTask : Runnable {
        override fun run() {
            try {
                val mainDownSvc = this@MainDownSvc
                val restartPending = mainDownSvc.restartPending
                if (mainDownSvc.keepAlive) {
                    mainDownSvc.stopPending = false
                    return
                }
                mainDownSvc.releaseService()
                if (restartPending) {
                    mainDownSvc.showDoneNoti()
                }
                // nothing left: end the download job (Android 14+)
                DownTransferJob.finish()
                try {
                    mainDownSvc.stopSelf()
                } catch (unused: Exception) {
                }
                mainDownSvc.stopPending = false
            } catch (_ex: Throwable) {
                throw RuntimeException(_ex)
            }
        }
    }

    internal inner class ExistFileToastTask : Runnable {
        override fun run() {
            MainUtil.showToast(this@MainDownSvc.appCtx, AppR.string.exist_file)
        }
    }

    internal inner class RegisteredToastTask : Runnable {
        override fun run() {
            MainUtil.showToast(this@MainDownSvc.appCtx, AppR.string.registered)
        }
    }

    internal inner class DownStartToastTask : Runnable {
        override fun run() {
            MainUtil.showToast(this@MainDownSvc.appCtx, AppR.string.down_start)
        }
    }

    interface DownBusyListener {
        fun hasFocus(): Boolean

        fun isWaiting(): Boolean
    }

    interface DownImageListener {
        fun uiHandler(): Handler?

        fun onImagesSaved(arrayList: ArrayList<*>?, isOk: Boolean)

        val isRunning: Boolean
    }

    class DownItem {
        @JvmField
        var videoLength: Long = 0
        @JvmField
        var audioLength: Long = 0
        @JvmField
        var videoRead: Long = 0
        @JvmField
        var audioRead: Long = 0
        @JvmField
        var audioOffset: Long = 0
        @JvmField
        var partSize: Long = 0
        @JvmField
        var notifyId: Int = 0
        @JvmField
        var pauseRequested: Boolean = false
        @JvmField
        var urlType: Int = 0
        @JvmField
        var cancelRequested: Boolean = false
        @JvmField
        var isReserved: Boolean = false
        @JvmField
        var isRunning: Boolean = false
        @JvmField
        var isIndeterminate: Boolean = false
        @JvmField
        var outOfMemory: Boolean = false
        /** Why a download failed, for the list (2026-11-20): too little free space, the finished size did not match
         *  the server's, or the server ignored the ranges of a split download. */
        @JvmField
        var noSpace: Boolean = false
        @JvmField
        var sizeMismatch: Boolean = false
        @JvmField
        var rangeIgnored: Boolean = false
        /** The playlist is a live stream (no end marker). */
        @JvmField
        var isLiveStream: Boolean = false
        /** The server's answer when it refused the download for good (403 …), 0 = none. */
        @JvmField
        var httpStatus: Int = 0
        @JvmField
        var hasSub: Boolean = false
        @JvmField
        var noteFlag: Boolean = false
        @JvmField
        var isMerging: Boolean = false
        @JvmField
        var wasResumed: Boolean = false
        @JvmField
        var tempPath: String? = null
        @JvmField
        var builder: NotificationCompat.Builder? = null
        @JvmField
        var isSecret: Boolean = false
        @JvmField
        var id: Long = 0
        @JvmField
        var status: Int = 0
        @JvmField
        var prevStatus: Int = 0
        @JvmField
        var fileType: Int = 0
        @JvmField
        var url: String? = null
        @JvmField
        var referer: String? = null
        @JvmField
        var videoKeys: MutableList<EncItem?>? = null
        /** The per-segment IV items of the video playlist (`DownParseM3u8.segmentList`). */
        @JvmField
        var videoSegments: ArrayList<EncItem?>? = null
        @JvmField
        var audioKeys: MutableList<EncItem?>? = null
        /** The per-segment IV items of the audio playlist. */
        @JvmField
        var audioSegments: ArrayList<EncItem?>? = null
        @JvmField
        var path: String? = null
        @JvmField
        var segmentDir: String? = null
        @JvmField
        var uriItem: UriItem? = null
        @JvmField
        var readSize: Long = 0
        @JvmField
        var totalSize: Long = 0
        @JvmField
        var lastSize: Long = 0
        @JvmField
        var startTime: Long = 0
        @JvmField
        var firstSize: Long = 0
        @JvmField
        var lastTime: Long = 0
        @JvmField
        var oldParts: Int = 0
        @JvmField
        var partCount: Int = 0
        @JvmField
        var audioStart: Int = 0
        @JvmField
        var segmentCount: Int = 0
        @JvmField
        var partsDone: Int = 0
        @JvmField
        var audioDone: Int = 0
    }

    interface DownListListener {
        fun onProgress(progress: Int, id: Long)

        fun onAdded(downItem: DownItem?)

        fun onDisconnected()

        fun onChanged(downItem: DownItem?)

        fun onListChanged(isAdded: Boolean)

        fun onConnected()
    }

    interface DownSaveListener {
        fun uiHandler(): Handler?

        fun onStarted(downItem: DownItem?)

        fun onPaused(downItem: DownItem?)

        fun onFinished(downItem: DownItem?)

        fun onFailed(downItem: DownItem?)
    }

    interface DownZipListener {
        fun uiHandler(): Handler?

        fun onSaved(arrayList: ArrayList<*>?)

        val isRunning: Boolean
    }

    class EncItem {
        @JvmField
        var lastKeyIndex: Int = 0
        @JvmField
        var keyUri: String? = null
        @JvmField
        var fetched: ByteArray? = null
    }

    inner class EventReceiver : BroadcastReceiver() {

        inner class PauseItemTask(
                                    @JvmField val itemId: Long,
                                    @JvmField val pause: Boolean
        ) : Runnable {
            override fun run() {
                val mainDownSvc = this@MainDownSvc
                mainDownSvc.pauseItem(this.itemId, this.pause)
            }
        }

        inner class CancelItemTask(
                                    @JvmField val itemId: Long
        ) : Runnable {
            override fun run() {
                val mainDownSvc = this@MainDownSvc
                mainDownSvc.cancelItem(this.itemId)
            }
        }

        inner class DeleteItemTask(
                                    @JvmField val withFile: Boolean,
                                    @JvmField val itemId: Long,
                                    @JvmField val path: String?
        ) : Runnable {
            override fun run() {
                val mainDownSvc = this@MainDownSvc
                val context = mainDownSvc.appCtx
                if (context == null) {
                    return
                }
                val withFile = this.withFile
                val id = this.itemId
                if (withFile) {
                    DbBookDown.deleteById(context, id)
                    mainDownSvc.notifyProgress(3, id)
                } else {
                    val path = this.path
                    DbBookDown.deleteFile(id, context, path, MainUtil.pageUrlOrNull(path), true)
                    mainDownSvc.notifyProgress(-1234, id)
                }
                mainDownSvc.removeItem(id, withFile)
            }
        }

        inner class StopAllTask : Runnable {
            override fun run() {
                this@MainDownSvc.stopAll(true, false)
            }
        }

        inner class CancelMergeTask : Runnable {
            override fun run() {
                val mainDownSvc = this@MainDownSvc
                mainDownSvc.cancelMerge()
            }
        }

        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                val action = intent.getAction()
                action!!.javaClass
                var actionKind: Byte = -1
                when (action.hashCode()) {
                    -2128145023 -> if (action == "android.intent.action.SCREEN_OFF") {
                        actionKind = 0
                    }

                    -1454123155 -> if (action == "android.intent.action.SCREEN_ON") {
                        actionKind = 1
                    }

                    -830137506 -> if (action == "ch.opumlabs.opumbrowser.ACTION_DOWN_CANCEL") {
                        actionKind = 2
                    }

                    832948279 -> if (action == "ch.opumlabs.opumbrowser.ACTION_IMAGE_CANCEL") {
                        actionKind = 3
                    }

                    1786349682 -> if (action == "ch.opumlabs.opumbrowser.ACTION_DOWN_PAUSE") {
                        actionKind = 4
                    }

                    1788314916 -> if (action == "ch.opumlabs.opumbrowser.ACTION_DOWN_RETRY") {
                        actionKind = 5
                    }

                    2135528194 -> if (action == "ch.opumlabs.opumbrowser.ACTION_DOWN_EXIT") {
                        actionKind = 6
                    }
                }
                val mainDownSvc = this@MainDownSvc
                run sw0@{
when (actionKind.toInt()) {
                    0 -> mainDownSvc.busyState = 2.toByte()
                    1 -> {
                        mainDownSvc.busyState = 1.toByte()
                        val downBusyListener = mainDownSvc.busyListener
                        if (!(if (downBusyListener != null) downBusyListener.hasFocus() else false)) {
                            mainDownSvc.flushFinished(true)
                            return@sw0
                        }
                    }

                    2 -> {
                        val longExtra = intent.getLongExtra("EXTRA_ID", -1L)
                        if (longExtra != -1L) {
                            val deleteItemTask: DeleteItemTask = DeleteItemTask(
                                intent.getBooleanExtra("EXTRA_STOP", false),
                                longExtra,
                                intent.getStringExtra("EXTRA_PATH")
                            )
                            mainDownSvc.runInBackground(deleteItemTask)
                        }
                    }

                    3 -> {
                        val cancelMergeTask: CancelMergeTask = CancelMergeTask()
                        mainDownSvc.runInBackground(cancelMergeTask)
                    }

                    4 -> {
                        val longExtra = intent.getLongExtra("EXTRA_ID", -1L)
                        if (longExtra != -1L) {
                            val pauseItemTask: PauseItemTask = PauseItemTask(
                                longExtra,
                                intent.getBooleanExtra("pause", false)
                            )
                            mainDownSvc.runInBackground(pauseItemTask)
                        }
                    }

                    5 -> {
                        val longExtra = intent.getLongExtra("EXTRA_ID", -1L)
                        if (longExtra != -1L) {
                            val cancelItemTask: CancelItemTask =
                                CancelItemTask(longExtra)
                            mainDownSvc.runInBackground(cancelItemTask)
                        }
                    }

                    6 -> if (!mainDownSvc.stopping) {
                        mainDownSvc.runInBackground(StopAllTask())
                    }
                }
}
            }
        }
    }

    class ImageItem {
        @JvmField
        var url: String? = null
        @JvmField
        var referer: String? = null
        @JvmField
        var dirPath: String? = null
        @JvmField
        var dirName: String? = null
        @JvmField
        var fileName: String? = null
        @JvmField
        var isGif: Boolean = false
        @JvmField
        var state: Byte = 0
    }

    class M3u8Item {
        @JvmField
        var url: String? = null
        @JvmField
        var src: String? = null
        @JvmField
        var audioUrl: String? = null
        @JvmField
        var label: String? = null
        @JvmField
        var size: Int = 0
        @JvmField
        var base: String? = null
        @JvmField
        var template: String? = null
        @JvmField
        var audioTemplate: String? = null
        @JvmField
        var fileSize: Long = 0
        @JvmField
        var vimeoJson: JsonObject? = null
    }

    class ParseItem {
        @JvmField
        var base: String? = null
        @JvmField
        var altAudio: Boolean = false
        @JvmField
        var hdAudio: Boolean = false
    }

    private class SortDown : Comparator<ChildItem?> {
        override fun compare(first: ChildItem?, second: ChildItem?): Int {
            val childItem = first
            val other = second
            if (childItem == null) {
                return if (other == null) 0 else 1
            }
            if (other == null) {
                return -1
            }
            return MainUtil.compareStringsIgnoreCase(childItem.name, other.name, false)
        }
    }

    /**
     * Starts (or resumes) a plain HTTP download: works out how many parallel range parts to use,
     * checks whether the server still accepts the saved resume offset, then starts one worker per
     * part (plus one for the separate audio stream of type 15 items).
     */
    fun startDownload(item: DownItem?) {
        if (item == null || TextUtils.isEmpty(item.url) || TextUtils.isEmpty(item.path) || this.appCtx == null || this.uiHandler == null) {
            return
        }
        var url = item.url
        var audioUrl: String? = null
        if (item.urlType == 15) {
            url = MainUtil.urlFromText(item.url)
            audioUrl = MainUtil.mixUrlOf(item.url)
        }
        val oldTotal = item.readSize
        val oldParts = item.oldParts
        // nothing on disk yet for this download (the size can already be known from the download sheet, so
        // readSize says nothing about that)
        val fresh = MainUtil.downFilePath(this.appCtx, item.path)?.let { dir ->
            File(dir).listFiles()?.none { it.length() > 0 } ?: true
        } ?: true
        if (item.readSize > 0L && item.videoLength <= 0L && TextUtils.isEmpty(audioUrl)) {
            // the size came with the download (sheet): it is the file's length, which splitting needs (it stayed 0,
            // so such downloads were never split)
            item.videoLength = item.readSize
        }
        if (item.readSize == 0L) {
            item.videoLength = MainUtil.contentLengthOf(this.appCtx, url, item.referer)
            item.readSize = item.videoLength
            if (!TextUtils.isEmpty(audioUrl)) {
                item.audioLength = MainUtil.contentLengthOf(this.appCtx, audioUrl, item.referer)
                item.readSize += item.audioLength
            }
        }
        if (item.oldParts < 2) {
            item.oldParts = 1
        }
        // a new split download: only when the server really answers ranges (206 + Content-Range); otherwise one
        // connection (2026-11-20; servers that ignore ranges used to give a broken file)
        if (fresh && item.oldParts >= 2 && !MainUtil.supportsRanges(this.appCtx, url, item.referer)) {
            item.oldParts = 1
        }
        val partSize: Long = if (item.oldParts >= 2) partSize(item, url) else 0
        item.partSize = partSize
        if (item.oldParts < 2 || partSize == 0L) {
            item.oldParts = 1
        }
        item.partCount = item.oldParts
        val digits = MainUtil.signOf(item.partCount)
        if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(item.path) && rangeRejected(
                item,
                url,
                digits
            )
        ) {
            // The server no longer accepts our resume offset: throw the parts away and start over.
            for (index in 0..<item.partCount) {
                MainUtil.deleteUri(this.appCtx, segmentName(index, digits, item.segmentDir))
            }
            item.partSize = 0
            item.oldParts = 1
            item.partCount = 1
        }
        item.segmentDir = ensureDir(item.path)
        if (TextUtils.isEmpty(item.segmentDir)) {
            item.status = 4
            finishItem(item)
            return
        }
        // enough free space for the rest of the file (the parts go to the temp folder and then, one by one, to the
        // download location on the same storage): fail now with a clear reason rather than half way through
        if (fresh && item.readSize > 0) {
            val free = try { android.os.StatFs(item.segmentDir).availableBytes } catch (_: Exception) { Long.MAX_VALUE }
            if (free < item.readSize + MIN_FREE_BYTES) {
                item.noSpace = true
                item.status = 4
                finishItem(item)
                return
            }
        }
        // always for a new download: the part count must survive a restart (it was only saved when the size or the
        // count changed here, so a download whose size came from the sheet resumed as one connection and took a
        // finished first part for the whole file)
        if (fresh || item.readSize != oldTotal || item.oldParts != oldParts) {
            setSize(this.appCtx, item.id, item.readSize, item.oldParts)
        }
        item.totalSize = 0
        item.startTime = System.currentTimeMillis()
        item.lastTime = 0
        if (item.lastSize > 0 && item.builder != null) {
            onItemProgress(item)
        } else {
            showProgressNoti(item)
        }
        val partDigits = MainUtil.signOf(item.partCount)
        val listener: FinishItemStep = FinishItemStep()
        // the download connections' own bounded pool (was the app's unbounded one)
        for (index in 0..<item.partCount) {
            com.mycompany.app.down.DownPool.execute(SavePartStep(this, item, url, index, partDigits, listener))
        }
        if (item.urlType == 15) {
            com.mycompany.app.down.DownPool.execute(SaveAudioTask(this, item, audioUrl, listener))
        }
        if (this.uiHandler != null) {
            this.uiHandler!!.postDelayed(DownloadProgressTask(this, item), 1000L)
        }
    }

    /** True when a range request for the last part's resume offset gets HTTP 416.  */
    private fun rangeRejected(item: DownItem?, url: String?, digits: Int): Boolean {
        val last = item!!.partCount - 1
        val single = item.partCount == 1
        val start: Long
        val length: Long
        if (single) {
            start = 0
            length = item.videoLength
        } else {
            start = last * item.partSize
            length = item.videoLength - start
        }
        val have = MainUtil.fileSizeOf(this.appCtx, segmentName(last, digits, item.segmentDir))
        if (single && have <= 0) {
            return false
        }
        val conn = MainUtil.openHeadConnection(this.appCtx, url, item.referer, 0, 0, false, MainUtil.userAgentPref(), true)
        if (conn == null) {
            return false
        }
        var rejected = false
        try {
            val range = StringBuilder("bytes=").append(start + have).append("-")
            if (length > 0) {
                range.append(start + length - 1)
            }
            conn.setRequestProperty("Accept-Ranges", "bytes")
            conn.setRequestProperty("Range", range.toString())
            conn.setDoInput(true)
            conn.connect()
            rejected = conn.getResponseCode() == 416
        } catch (unused: Exception) {
        }
        conn.disconnect()
        return rejected
    }

    /**
     * Second stage of a stream download: `segments` is the media playlist's segment list.
     * Some sources have a separate audio playlist, which is parsed first (async) or derived here;
     * everything ends in [.C] with the video segments and the optional audio segments.
     */
    fun startM3u8(item: DownItem?, m3u: M3u8Item?, parse: ParseItem?, segments: MutableList<String?>?) {
        if (TextUtils.isEmpty(item!!.url) || TextUtils.isEmpty(item.path) || this.appCtx == null) {
            return
        }
        if (segments == null || segments.isEmpty()) {
            item.status = 5
            finishItem(item)
            return
        }
        item.audioStart = segments.size
        var audio: MutableList<String?>? = null
        when (item.urlType) {
            1 -> {
                val base = if (parse != null) parse.base else null
                val altAudio = parse != null && parse.altAudio
                val hdAudio = parse != null && parse.hdAudio
                if (altAudio) {
                    var audioUrl = "${base}a1"
                    var found = MainUtil.isImageUrl(this.appCtx, audioUrl, item.referer)
                    if (!found) {
                        audioUrl = "${base}a2"
                        found = MainUtil.isImageUrl(this.appCtx, audioUrl, item.referer)
                    }
                    if (found) {
                        val key = queryOf(audioUrl)
                        val parser = DownParseM3u8(this.appCtx)
                        parser.parseMaster(
                            audioUrl,
                            item.referer,
                            key,
                            StartSegmentsStep(this, parser, item, segments)
                        )
                        return
                    }
                } else if (hdAudio && segments.size == 1) {
                    // The "hd" playlist we were given is the audio track; download the video next to it.
                    val first = segments.get(0)
                    if (!TextUtils.isEmpty(first) && first!!.endsWith("_mp4_hd.audio.mp4")) {
                        segments.set(0, first.replace("_mp4_hd.audio.mp4", "_mp4_hd.mp4"))
                        audio = ArrayList()
                        audio.add(first)
                    }
                }
            }

            2 -> if (m3u != null && !TextUtils.isEmpty(m3u.audioUrl)) {
                val key = queryOf(m3u.url)
                val parser = DownParseM3u8(this.appCtx)
                parser.parseSegments(
                    null,
                    m3u.url,
                    item.referer,
                    key,
                    m3u.audioUrl,
                    CheckKeysStep(this, parser, item, m3u, key!!, segments)
                )
                return
            }

            4 -> if (m3u != null) {
                audio = DownParseReddit.redditSegments(m3u.audioUrl)
            }

            6 -> if (m3u != null) {
                audio = DownParseKakao.segmentUrls(this.appCtx, m3u.url, item.referer, m3u.audioTemplate, m3u.audioUrl, item, null)
            }

            8 -> if (m3u != null) {
                audio = DownParseDzen.segmentUrls(this.appCtx, m3u.url, item.referer, m3u.audioTemplate, m3u.audioUrl, item, null)
            }

            10 -> if (m3u != null) {
                audio = vimeoSegments(m3u.vimeoJson, m3u.base, null, true)
            }

            else -> {}
        }
        startSegments(item, segments, audio)
    }

    /** A segment's size: its byte range (#EXT-X-BYTERANGE), else asked from the server. */
    private fun segmentLength(url: String?, referer: String?): Long {
        val ranged = DownHls.rangeLength(url)
        return if (ranged > 0) ranged else MainUtil.contentLengthOf(this.appCtx, DownHls.split(url).first, referer)
    }

    /** A live stream (playlist without an end marker) cannot be downloaded: fail with a clear reason (2026-11-20). */
    private fun failLive(downItem: DownItem): Boolean {
        downItem.isLiveStream = true
        downItem.status = 4
        finishItem(downItem)
        return true
    }

    fun startSegments(downItem: DownItem?, list: MutableList<String?>?, segments: MutableList<String?>?) {
        var segmentList: MutableList<String?>?
        val type: String?
        if (TextUtils.isEmpty(downItem!!.url) || TextUtils.isEmpty(downItem.path) || this.appCtx == null) {
            return
        }
        val readSizeNow = downItem.readSize
        val oldParts = downItem.oldParts
        val size = if (segments != null) segments.size else 0
        downItem.segmentCount = size
        if (size > 0) {
            list!!.addAll(segments!!)
        }
        val partCount = list!!.size
        downItem.partCount = partCount
        var oldPartsNow = downItem.oldParts
        if (oldPartsNow < 2) {
            downItem.oldParts = 1
            oldPartsNow = 1
        }
        if (oldPartsNow > partCount) {
            downItem.oldParts = partCount
            oldPartsNow = partCount
        }
        var perTask = partCount / oldPartsNow
        val rest = partCount % oldPartsNow
        if (rest != 0) {
            perTask++
        }
        if (perTask == 0 && rest == 0) {
            downItem.status = 4
            finishItem(downItem)
            return
        }
        val dir = ensureDir(downItem.path)
        downItem.segmentDir = dir
        if (TextUtils.isEmpty(dir)) {
            downItem.status = 4
            finishItem(downItem)
            return
        }
        var doneSize = downItem.readSize
        if (doneSize == 0L) {
            segmentList = list
            doneSize = segmentLength(segmentList.get(downItem.audioStart / 2), downItem.referer) * (downItem.audioStart.toLong())
            downItem.readSize = doneSize
            val segmentCount = downItem.segmentCount
            if (segmentCount > 0) {
                val segmentSize = segmentLength(segments!!.get(segmentCount / 2), downItem.referer)
                downItem.audioOffset = segmentSize
                doneSize = (segmentSize * (downItem.segmentCount.toLong())) + downItem.readSize
                downItem.readSize = doneSize
            }
        } else {
            segmentList = list
        }
        val readSize = doneSize
        if (readSize != readSizeNow || downItem.oldParts != oldParts) {
            setSize(this.appCtx, downItem.id, readSize, downItem.oldParts)
        }
        downItem.partsDone = 0
        downItem.audioDone = 0
        downItem.videoRead = 0L
        downItem.audioRead = 0L
        downItem.totalSize = 0L
        downItem.startTime = System.currentTimeMillis()
        downItem.lastTime = 0L
        onItemProgress(downItem)
        downItem.isRunning = false
        if (downItem.status == 1) {
            var sign = MainUtil.signOf(downItem.partCount)
            val segmentsDoneStep: SegmentsDoneStep = SegmentsDoneStep()
            val context = this.appCtx
            val videoKeys = downItem.videoKeys
            val firstKey: EncItem? = if (videoKeys == null || videoKeys.isEmpty()) null else encItemAt(0, downItem.videoKeys)
            var bArrD: ByteArray? = if (videoKeys == null || videoKeys.isEmpty() || firstKey == null) null else fetchBytes(context, firstKey.keyUri, downItem.referer)
            if (videoKeys == null || videoKeys.isEmpty() || firstKey == null || bArrD == null) {
                bArrD = null
                type = null
            } else {
                firstKey.fetched = bArrD
                type = firstKey.keyUri
            }
            val audioKeys = downItem.audioKeys
            val encItemC: EncItem? = if (audioKeys != null && !audioKeys.isEmpty()) encItemAt(0, downItem.audioKeys) else null
            if (audioKeys != null && !audioKeys.isEmpty() && encItemC != null) {
                if (MainUtil.sameObject(encItemC.keyUri, type)) {
                    encItemC.fetched = bArrD
                } else {
                    val fetched = fetchBytes(context, encItemC.keyUri, downItem.referer)
                    if (fetched != null) {
                        encItemC.fetched = fetched
                    }
                }
            }
            val oldPartCount = downItem.oldParts
            var index = 0
            while (index < oldPartCount && downItem.status == 1) {
                val nextIndex = sign
                com.mycompany.app.down.DownPool.execute(StartedTask(this, downItem, segmentList!!, perTask, index, nextIndex, segmentsDoneStep))
                index++
                segmentList = list
                sign = nextIndex
            }
            val handler = this.uiHandler
            if (handler != null) {
                handler.postDelayed(DownloadProgressTask(this, downItem), 1000L)
            }
        }
    }

    fun notifyComplete(downItem: DownItem?, type: String?) {
        if (downItem != null) {
            val status = downItem.status
            if (status != 2 && (ZoneSettings.notiDown.value || status != 3)) {
                val intent = Intent("ch.opumlabs.opumbrowser.ACTION_DOWN_COMPLETE")
                intent.putExtra("EXTRA_ID", downItem.id)
                intent.putExtra("EXTRA_STATUS", downItem.status)
                intent.putExtra("EXTRA_TYPE", type)
                intent.putExtra("secretMode", downItem.isSecret)
                intent.setPackage(getPackageName())
                sendBroadcast(intent)
            }
            val downListListener = this.listListener
            if (downListListener != null) {
                downListListener.onAdded(downItem)
            }
        }
    }

    fun notifyItemChanged(downItem: DownItem?) {
        val downListListener = this.listListener
        if (downListListener == null || downItem == null) {
            return
        }
        downListListener.onChanged(downItem)
    }

    fun notifyProgress(done: Int, total: Long) {
        val downListListener = this.listListener
        if (downListListener == null) {
            return
        }
        downListListener.onProgress(done, total)
    }

    fun notifyBusy(isAdded: Boolean) {
        val downListListener = this.listListener
        if (downListListener == null) {
            return
        }
        downListListener.onListChanged(isAdded)
    }

    /**
     * Queues download row `id` from the database. `action` 2 resumes it (Q), anything
     * else starts it (N). Does nothing if the item is already queued.
     */
    fun handleAction(action: Int, id: Long) {
        val context = this.appCtx
        if (id == 0L || action == 0 || context == null) {
            return
        }
        var item: DownItem? = null
        if (id > 0) {
            var cursor: Cursor? = null
            try {
                cursor = queryById(get(context).getWritableDatabase(), "DbBookDown_table", null, id)
                if (cursor != null && cursor.moveToFirst()) {
                    val uri = UriItem()
                    uri.dirPath = cursor.getString(cursor.getColumnIndex("_dir"))
                    uri.dirName = cursor.getString(cursor.getColumnIndex("_dname"))
                    uri.uriString = cursor.getString(cursor.getColumnIndex("_path"))
                    uri.name = cursor.getString(cursor.getColumnIndex("_name"))
                    uri.size = cursor.getLong(cursor.getColumnIndex("_size"))
                    val row = DownItem()
                    row.uriItem = uri
                    row.isSecret = cursor.getInt(cursor.getColumnIndex("_secret")) == 1
                    row.id = cursor.getLong(cursor.getColumnIndex("_id"))
                    row.notifyId = ((row.id % 2147483635).toInt()) + 1
                    row.status = cursor.getInt(cursor.getColumnIndex("_status"))
                    row.fileType = cursor.getInt(cursor.getColumnIndex("_type"))
                    row.url = cursor.getString(cursor.getColumnIndex("_url"))
                    row.referer = cursor.getString(cursor.getColumnIndex("_rsv1"))
                    row.path = uri.uriString
                    row.readSize = uri.size
                    row.totalSize = cursor.getLong(cursor.getColumnIndex("_read"))
                    row.urlType = urlType(row.url)
                    row.oldParts = cursor.getInt(cursor.getColumnIndex("_fast"))
                    row.isReserved = false
                    item = row
                }
            } catch (unused: Exception) {
            }
            if (cursor != null) {
                try {
                    cursor.close()
                } catch (unused: Exception) {
                }
            }
        }
        if (item == null) {
            return
        }
        synchronized(this.itemLock) {
            if (this.downItems != null) {
                for (queued2 in this.downItems) {
                    val queued = queued2 as DownItem?
                    if (queued != null && queued.id == item.id) {
                        return
                    }
                }
            }
            if (this.downItems == null) {
                this.downItems = ArrayList<Any?>()
            }
            this.downItems!!.add(item)
            this.lastProgressTime = 0L
            if (action == 2) {
                cancelItem(id)
            } else {
                pauseItem(id, false)
            }
            showProgressNoti(item)
            if (item.builder == null) {
                return
            }
            val notification = item.builder!!.build()
            setNotiFlags(notification, item.status)
            addRunning(item)
            val manager: NotificationManager? = MainApp.Companion.notifications(this.appCtx)
            if (manager != null) {
                manager.notify(item.notifyId, notification)
            }
            notifyBusy(false)
        }
    }

    fun removeItem(id: Long, withFile: Boolean) {
        if (this.appCtx != null) {
            synchronized(this.itemLock) {
                val arrayList = this.downItems
                if (arrayList == null || arrayList.isEmpty()) {
                    return
                }
                try {
                    val downItems = this.downItems
                    val size = downItems!!.size
                    var index = 0
                    while (index < size) {
                        val rawDownItem: Any? = downItems.get(index)
                        index++
                        val downItem = rawDownItem as DownItem?
                        if (downItem != null && downItem.id == id) {
                            if (withFile) {
                                downItem.status = 3
                            } else {
                                downItem.status = 6
                            }
                            removeRunning(downItem)
                            val newDownItems: ArrayList<Any?> = ArrayList<Any?>(this.downItems!!)
                            var remaining = newDownItems.size
                            val it: MutableIterator<Any?> = newDownItems.iterator()
                            while (it.hasNext()) {
                                val candidate = it.next() as DownItem?
                                if (candidate != null && candidate.id == downItem.id) {
                                    it.remove()
                                    remaining--
                                    break
                                }
                            }
                            if (remaining > 0 && newDownItems.size > 0) {
                                this.downItems = newDownItems
                                startNext()
                                break
                            } else {
                                this.downItems = null
                                stopIfIdle(false)
                                break
                            }
                        }
                    }
                } catch (unused: Exception) {
                }
            }
        }
    }

    fun releaseService() {
        if (!this.started) {
            return
        }
        this.started = false
        if (this.eventReceiver != null) {
            try {
                unregisterReceiver(this.eventReceiver)
                this.eventReceiver = null
            } catch (unused: Exception) {
            }
        }
        val wasForeground: Boolean
        synchronized(this.foregroundLock) {
            wasForeground = this.inForeground
            this.inForeground = false
        }
        if (wasForeground) {
            val nm: NotificationManager? = MainApp.Companion.notifications(this.appCtx)
            if (nm != null) {
                nm.cancel(2147483646)
            }
        }
        if (this.appCtx != null) {
            var nm: NotificationManager? = null
            try {
                synchronized(this.runningLock) {
                    // Running items: re-post their notification so it stays after the service stops.
                    val running = this.runningItems
                    this.runningItems = null
                    if (running != null && !running.isEmpty()) {
                        nm = MainApp.Companion.notifications(this.appCtx)
                        if (nm != null) {
                            for (item2 in running) {
                                val item = item2 as DownItem?
                                if (item == null || item.builder == null) {
                                    continue
                                }
                                val n = item.builder!!.build()
                                nm.notify(item.notifyId, n)
                            }
                        }
                    }
                }
            } catch (unused: Exception) {
            }
            try {
                synchronized(this.runningLock) {
                    // Waiting items: drop their notification.
                    val waiting = this.waitingItems
                    this.waitingItems = null
                    if (waiting != null && !waiting.isEmpty()) {
                        if (nm == null) {
                            nm = MainApp.Companion.notifications(this.appCtx)
                        }
                        if (nm != null) {
                            for (item2 in waiting) {
                                val item = item2 as DownItem?
                                if (item != null) {
                                    nm.cancel(item.notifyId)
                                }
                            }
                        }
                    }
                }
            } catch (unused: Exception) {
            }
        }
        val app: MainApp? = MainApp.Companion.from(this.appCtx)
        if (app != null) {
            app.downBinding = false
            app.downService = null
            app.downStarting = false
            if (app.downListListener != null) {
                app.downListListener!!.onDisconnected()
            }
        }
        MainUtil.clearHandler(this.uiHandler)
        this.uiHandler = null
        this.busyListener = null
        this.listListener = null
        this.downItems = null
        this.hourText = null
        this.minuteText = null
        this.secondText = null
        this.downloadTitle = null
        this.executorCache = null
    }

    fun retryLater(downItem: DownItem?) {
        downItem!!.outOfMemory = false
        downItem.noSpace = false; downItem.sizeMismatch = false; downItem.rangeIgnored = false; downItem.httpStatus = 0
        runInBackground(StartDownloadTask(this, downItem))
    }

    fun stopAll(restart: Boolean, restartPending: Boolean) {
        if (this.stopping) {
            return
        }
        val arrayList = this.downItems
        this.downItems = null
        val hasItems = if (arrayList == null || arrayList.isEmpty()) false else true
        if (!hasItems && !this.merging) {
            stopIfIdle(false)
            return
        }
        val context = this.appCtx
        if (context == null) {
            return
        }
        this.stopping = true
        if (restartPending && !BrowserSettings.checkDown.value) {
            BrowserSettings.edit(context) { set(BrowserSettings.checkDown, true) }
        }
        synchronized(this.itemLock) {
            try {
                finishMerge()
                if (hasItems) {
                    val size = arrayList!!.size
                    var index = 0
                    while (index < size) {
                        val rawDownItem: Any? = arrayList.get(index)
                        index++
                        val downItem = rawDownItem as DownItem?
                        if (downItem != null) {
                            val status = downItem.status
                            if (status == 1) {
                                downItem.pauseRequested = true
                                if (restart) {
                                    downItem.status = 6
                                    val appCtx = this.appCtx
                                    val id = downItem.id
                                    val path = downItem.path
                                    DbBookDown.deleteFile(id, appCtx, path, MainUtil.pageUrlOrNull(path), true)
                                    MainUtil.noOp(downItem.segmentDir)
                                } else {
                                    downItem.status = 2
                                    setStatus(this.appCtx, downItem.id, downItem.readSize, downItem.totalSize, true)
                                }
                            } else if (status == 2 && downItem.isReserved) {
                                downItem.pauseRequested = true
                                setStatus(this.appCtx, downItem.id, 0L, 0L, true)
                            }
                            removeRunning(downItem)
                            downItem.pauseRequested = false
                        }
                    }
                }
                notifyBusy(true)
                stopIfIdle(restartPending)
            } catch (unused: Exception) {
            }
        }
        this.stopping = false
    }

    fun pauseItem(id: Long, pause: Boolean) {
        var found: Boolean
        synchronized(this.itemLock) {
            val arrayList = this.downItems
            if (arrayList == null || arrayList.isEmpty()) {
                return
            }
            val now = System.currentTimeMillis()
            if (now - this.lastProgressTime >= 500) {
                this.lastProgressTime = now
                synchronized(this.itemLock) {
                    val downItems = this.downItems
                    val size = downItems!!.size
                    found = false
                    var index = 0
                    while (index < size) {
                        val rawDownItem: Any? = downItems.get(index)
                        index++
                        val downItem = rawDownItem as DownItem?
                        if (downItem != null && downItem.id == id) {
                            if (downItem.pauseRequested || downItem.cancelRequested) {
                                val handler = this.uiHandler
                                if (handler != null) {
                                    handler.post(PauseBusyToastTask())
                                }
                            } else {
                                if (!pause) {
                                    val context = this.appCtx
                                    if (downItem.status == 2) {
                                        downItem.pauseRequested = true
                                        downItem.status = 1
                                        downItem.isReserved = false
                                        setStatus(context, id, downItem.readSize, downItem.totalSize, pause)
                                        notifyProgress(downItem.status, id)
                                        retryLater(downItem)
                                    }
                                } else if (downItem.status == 1) {
                                    downItem.pauseRequested = true
                                    downItem.status = 2
                                    setStatus(this.appCtx, id, downItem.readSize, downItem.totalSize, pause)
                                    notifyProgress(downItem.status, id)
                                    onItemProgress(downItem)
                                }
                                downItem.pauseRequested = false
                            }
                            found = true
                            break
                        }
                    }
                }
                if (found || pause) {
                    return
                }
                handleAction(1, id)
            }
        }
    }

    fun onItemDone(downItem: DownItem?) {
        val copied: Boolean
        if (downItem != null) {
            if (downItem.status != 7 || downItem.isMerging) {
                val handler: Handler? = if (!checkFile(downItem)) null else this.uiHandler
                if (!checkFile(downItem) || handler == null) {
                    return
                }
                handler.postDelayed(RunNextPendingTask(), 200L)
                return
            }
            downItem.isMerging = true
            val wasResumed = downItem.wasResumed
            val tempPath = downItem.tempPath
            if (wasResumed) {
                copied = copyFile(downItem, tempPath, downItem.path, true)
            } else if (downItem.urlType == 0) {
                // plain HTTP download (urlType 0; HLS / video types keep the old join, their sizes are estimates):
                // the parts go straight into the download location (no joined temp copy)
                copied = copyParts(downItem, downItem.path)
            } else {
                val segmentDir = segmentPath(downItem, false)
                if (TextUtils.isEmpty(segmentDir)) {
                    copied = false
                } else if (downItem.urlType == 15) {
                    copied = if (!TextUtils.isEmpty(tempPath)) muxAudio(downItem, segmentDir!!, tempPath!!) else copyFile(
                        downItem,
                        segmentDir,
                        downItem.path,
                        true
                    )
                } else if (downItem.segmentCount > 0) {
                    val segmentFile = segmentPath(downItem, true)
                    if (TextUtils.isEmpty(segmentFile)) {
                        copied = false
                    } else {
                        copied = muxAudio(downItem, segmentDir!!, segmentFile!!)
                    }
                } else {
                    copied = saveStream(downItem, segmentDir!!)
                }
            }
            if (copied) {
                downItem.status = 3
            } else {
                downItem.status = 4
            }
            finishItem(downItem)
            if (copied) {
                MainUtil.noOp(downItem.segmentDir)
            }
            val handler: Handler? = if (!checkFile(downItem)) null else this.uiHandler
            if (!checkFile(downItem) || handler == null) {
                return
            }
            handler.postDelayed(RunNextPendingTask(), 200L)
        }
    }

    fun loadSaved() {
        var cursorG: Cursor?
        var mainDownSvc = this
        val context = mainDownSvc.appCtx
        if (context != null) {
            var arrayList: ArrayList<Any?>? = null
            try {
                cursorG = DbUtil.query(
                    get(context).getWritableDatabase(),
                    "DbBookDown_table",
                    null,
                    "_status!=?",
                    arrayOf<String?>("3"),
                    null
                )
                if (cursorG != null) {
                    try {
                        if (cursorG.moveToFirst()) {
                            val columnIndex = cursorG.getColumnIndex("_secret")
                            val statusCol = cursorG.getColumnIndex("_status")
                            val urlCol = cursorG.getColumnIndex("_url")
                            val rsv1Col = cursorG.getColumnIndex("_rsv1")
                            val dirCol = cursorG.getColumnIndex("_dir")
                            val dnameCol = cursorG.getColumnIndex("_dname")
                            val pathCol = cursorG.getColumnIndex("_path")
                            val nameCol = cursorG.getColumnIndex("_name")
                            val sizeCol = cursorG.getColumnIndex("_size")
                            val fastCol = cursorG.getColumnIndex("_fast")
                            do {
                                val sdkInt = cursorG.getInt(statusCol)
                                if (sdkInt == 3 || sdkInt == 4 || sdkInt == 5) {
                                    arrayList = arrayList
                                } else {
                                    val uriItem = UriItem()
                                    uriItem.dirPath = cursorG.getString(dirCol)
                                    uriItem.dirName = cursorG.getString(dnameCol)
                                    uriItem.uriString = cursorG.getString(pathCol)
                                    uriItem.name = cursorG.getString(nameCol)
                                    val loaded = arrayList
                                    try {
                                        uriItem.size = cursorG.getLong(sizeCol)
                                        val downItem = DownItem()
                                        downItem.uriItem = uriItem
                                        downItem.isSecret = cursorG.getInt(columnIndex) == 1
                                        downItem.url = cursorG.getString(urlCol)
                                        downItem.referer = cursorG.getString(rsv1Col)
                                        downItem.oldParts = cursorG.getInt(fastCol)
                                        arrayList =
                                            if (loaded == null) ArrayList<Any?>() else loaded
                                        arrayList.add(downItem)
                                    } catch (unused: Exception) {
                                        arrayList = loaded
                                    }
                                }
                            } while (cursorG.moveToNext())
                        }
                    } catch (unused2: Exception) {
                    }
                }
            } catch (unused3: Exception) {
                cursorG = null
            }
            val items = arrayList
            if (cursorG != null) {
                try {
                    cursorG.close()
                } catch (unused4: Exception) {
                }
            }
            if (items == null || items.isEmpty()) {
                return
            }
            val size = items.size
            var index = 0
            while (index < size) {
                val nextIndex = index + 1
                val downItem = items.get(index) as DownItem?
                if (downItem != null) {
                    mainDownSvc.addDownload(
                        downItem.url!!,
                        downItem.referer,
                        downItem.uriItem,
                        downItem.oldParts,
                        downItem.isSecret,
                        true
                    )
                    mainDownSvc = this
                }
                index = nextIndex
            }
        }
    }

    fun cancelItem(id: Long) {
        var found: Boolean
        synchronized(this.itemLock) {
            val arrayList = this.downItems
            if (arrayList == null || arrayList.isEmpty()) {
                return
            }
            val now = System.currentTimeMillis()
            if (now - this.lastProgressTime >= 500) {
                this.lastProgressTime = now
                synchronized(this.itemLock) {
                    val downItems = this.downItems
                    val size = downItems!!.size
                    found = false
                    var index = 0
                    while (index < size) {
                        val rawDownItem: Any? = downItems.get(index)
                        index++
                        val downItem = rawDownItem as DownItem?
                        if (downItem != null && downItem.id == id) {
                            if (downItem.pauseRequested) {
                                val handler = this.uiHandler
                                if (handler != null) {
                                    handler.post(CancelBusyToastTask())
                                }
                            } else {
                                if (downItem.status != 1) {
                                    downItem.pauseRequested = true
                                    downItem.status = 1
                                    downItem.isReserved = false
                                    setStatus(this.appCtx, id, downItem.readSize, downItem.totalSize, false)
                                    notifyProgress(downItem.status, id)
                                    retryLater(downItem)
                                }
                                downItem.pauseRequested = false
                            }
                            found = true
                            break
                        }
                    }
                }
                if (found) {
                    return
                }
                handleAction(2, id)
            }
        }
    }

    fun stopIfIdle(restartPending: Boolean) {
        if (this.downItems != null || this.merging) {
            return
        }
        if (this.keepAlive) {
            this.stopPending = false
        } else {
            if (this.stopPending) {
                return
            }
            this.stopPending = true
            this.restartPending = restartPending
            runInBackground(StopServiceTask())
        }
    }

    fun setTimer(enabled: Boolean) {
        val timerTime = this.timerTime
        val runnable = this.timerRunnable
        val handler: Handler? = if (timerTime != 0L) this.uiHandler else null
        if (timerTime != 0L && handler != null) {
            handler.removeCallbacks(runnable)
        }
        if (!enabled) {
            this.timerTime = 0L
            return
        }
        this.timerTime = System.currentTimeMillis()
        var uiHandler = this.uiHandler
        if (uiHandler == null) {
            uiHandler = Handler(Looper.getMainLooper())
            this.uiHandler = uiHandler
        }
        uiHandler.postDelayed(runnable, 600000L)
    }

    fun updateForeground() {
        val context = this.appCtx
        val notificationManagerP: NotificationManager? = if (context == null) null else MainApp.Companion.notifications(context)
        if (context == null || notificationManagerP == null) {
            return
        }
        synchronized(this.foregroundLock) {
            if (this.inForeground) {
                return
            }
            this.inForeground = true
            val intent = Intent(this.appCtx, com.mycompany.app.ui.lists.DownloadListActivity::class.java as Class<*>)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val activity = PendingIntent.getActivity(this.appCtx, nextNotiId(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val exitIntent = Intent("ch.opumlabs.opumbrowser.ACTION_DOWN_EXIT")
            exitIntent.setPackage(getPackageName())
            val action = NotificationCompat.Action(
                AppR.drawable.outline_close_black_24,
                getString(AppR.string.down_exit),
                PendingIntent.getBroadcast(this.appCtx, nextNotiId(), exitIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            )
            val builder = NotificationCompat.Builder(this.appCtx!!, "Download")
            builder.setSmallIcon(AppR.drawable.baseline_file_download_white_24)
            builder.setContentTitle(getString(AppR.string.down_manager))
            builder.setContentIntent(activity)
            builder.setPriority(1)
            builder.setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_DOWN")
            builder.addAction(action)
            val notificationB = builder.build()
            val sdkInt = Build.VERSION.SDK_INT
            if (sdkInt >= 26) {
                notificationManagerP.createNotificationChannel(
                    NotificationChannel(
                        "Download", getString(
                            AppR.string.download
                        ), NotificationManager.IMPORTANCE_LOW
                    )
                )
            }
            // run by the download job (Android 14+): its notification instead of a foreground service
            if (DownTransferJob.isActive()) {
                DownTransferJob.setNotification(notificationB)
                return
            }
            try {
                if (sdkInt >= 29) {
                    startForeground(2147483646, notificationB, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
                } else {
                    startForeground(2147483646, notificationB)
                }
            } catch (unused: Exception) {
            }
        }
    }

    fun showFailNoti(downItem: DownItem?) {
        var uriItem: UriItem? = null
        val context = this.appCtx
        val notificationManagerP: NotificationManager? = if (context == null) null else MainApp.Companion.notifications(context)
        if (context == null || notificationManagerP == null) {
            return
        }
        var title = if (downItem == null || (downItem.uriItem.also {
                uriItem = it
            }) == null) null else uriItem!!.name
        if (TextUtils.isEmpty(title)) {
            title = getString(AppR.string.down_fail)
        }
        val errorRes: Int = errorText(downItem)
        val intent = Intent(this.appCtx, com.mycompany.app.ui.lists.DownloadListActivity::class.java as Class<*>)
        intent.putExtra("EXTRA_ID", downItem!!.id)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val activity = PendingIntent.getActivity(this.appCtx, nextNotiId(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(this.appCtx!!, "Download")
        builder.setSmallIcon(AppR.drawable.outline_error_white_24)
        builder.setContentTitle(title)
        builder.setContentText(getString(errorRes))
        builder.setContentIntent(activity)
        builder.setPriority(1)
        builder.setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_DOWN")
        val bitmapY = MainUtil.resourceBitmap(this.appCtx, AppR.drawable.baseline_error_gray_20)
        if (MainUtil.isBitmapOk(bitmapY)) {
            builder.setLargeIcon(bitmapY)
        }
        val notificationB = builder.build()
        setNotiFlags(notificationB, downItem.status)
        notificationManagerP.createNotificationChannel(
            NotificationChannel(
                "Download", getString(
                    AppR.string.download
                ), NotificationManager.IMPORTANCE_LOW
            )
        )
        notificationManagerP.notify(2147483645, notificationB)
    }

    fun showProgressNoti(downItem: DownItem?) {
        val notificationManagerP: NotificationManager? = if (this.appCtx == null || downItem == null || downItem.uriItem == null || downItem.notifyId <= 0 || TextUtils.isEmpty(
                downItem.path
            )) null else MainApp.Companion.notifications(this.appCtx)
        if (this.appCtx == null || downItem == null || downItem.uriItem == null || downItem.notifyId <= 0 || TextUtils.isEmpty(
                downItem.path
            ) || notificationManagerP == null) {
            return
        }
        val intent = Intent(this.appCtx, com.mycompany.app.ui.lists.DownloadListActivity::class.java as Class<*>)
        intent.putExtra("EXTRA_ID", downItem.id)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val activity = PendingIntent.getActivity(this.appCtx, nextNotiId(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val status = downItem.status
        downItem.prevStatus = status
        if (status == 2) {
            val builder = NotificationCompat.Builder(this.appCtx!!, "Download")
            builder.setSmallIcon(AppR.drawable.baseline_file_download_white_24)
            builder.setContentTitle(downItem.uriItem!!.name)
            builder.setContentText(
                getString(if (downItem.isReserved) AppR.string.reserved else AppR.string.paused))
            builder.setContentIntent(activity)
            builder.setPriority(1)
            builder.setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_DOWN")
            builder.addAction(cancelAction(downItem))
            builder.addAction(pauseAction(downItem))
            downItem.builder = builder
        } else if (status == 4 || status == 5) {
            val errorRes: Int = errorText(downItem)
            val builder = NotificationCompat.Builder(this.appCtx!!, "Download")
            builder.setSmallIcon(AppR.drawable.outline_error_white_24)
            builder.setContentTitle(downItem.uriItem!!.name)
            builder.setContentText(getString(errorRes))
            builder.setContentIntent(activity)
            builder.setPriority(1)
            builder.setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_DOWN")
            builder.addAction(cancelAction(downItem))
            builder.addAction(retryAction(downItem))
            downItem.builder = builder
            val bitmapY = MainUtil.resourceBitmap(this.appCtx, AppR.drawable.baseline_error_gray_20)
            if (MainUtil.isBitmapOk(bitmapY)) {
                downItem.builder!!.setLargeIcon(bitmapY)
            }
        } else {
            var isIndeterminate = false
            if (downItem.isRunning) {
                val builder = NotificationCompat.Builder(this.appCtx!!, "Download")
                builder.setSmallIcon(AppR.drawable.ic_download)
                builder.setContentTitle(downItem.uriItem!!.name)
                builder.setContentText("")
                builder.setProgress(100, 0, true)
                builder.setContentIntent(activity)
                builder.setPriority(1)
                builder.setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_DOWN")
                builder.addAction(cancelAction(downItem))
                builder.addAction(pauseAction(downItem))
                downItem.builder = builder
            } else {
                val readSize = downItem.readSize
                if (readSize == 0L && downItem.totalSize > 0) {
                    isIndeterminate = true
                }
                downItem.isIndeterminate = isIndeterminate
                val name = downItem.uriItem!!.name
                val percent = if (!isIndeterminate) (downItem.totalSize * 100.0f) / readSize else 0.0f
                val builder = NotificationCompat.Builder(this.appCtx!!, "Download")
                builder.setSmallIcon(AppR.drawable.ic_download)
                builder.setContentTitle(name)
                builder.setContentText("")
                builder.setProgress(100, Math.round(percent), downItem.isIndeterminate)
                builder.setContentIntent(activity)
                builder.setPriority(1)
                builder.setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_DOWN")
                builder.addAction(cancelAction(downItem))
                builder.addAction(pauseAction(downItem))
                downItem.builder = builder
            }
        }
        val notificationB = downItem.builder!!.build()
        setNotiFlags(notificationB, downItem.status)
        notificationManagerP.createNotificationChannel(
            NotificationChannel(
                "Download", getString(
                    AppR.string.download
                ), NotificationManager.IMPORTANCE_LOW
            )
        )
        addRunning(downItem)
        notificationManagerP.notify(downItem.notifyId, notificationB)
    }

    fun showDoneNoti() {
        val context = this.appCtx
        val notificationManagerP: NotificationManager? = if (context == null) null else MainApp.Companion.notifications(context)
        if (context == null || notificationManagerP == null) {
            return
        }
        val intentL = MainUtil.browserIntent(this.appCtx)
        intentL.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val activity = PendingIntent.getActivity(this.appCtx, 4, intentL, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val action = NotificationCompat.Action(
            AppR.drawable.baseline_play_arrow_black_24,
            getString(AppR.string.resume),
            activity
        )
        val builder = NotificationCompat.Builder(this.appCtx!!, "Download")
        builder.setSmallIcon(AppR.drawable.baseline_file_download_white_24)
        builder.setContentTitle(getString(AppR.string.down_timeout))
        builder.setContentIntent(activity)
        builder.setPriority(1)
        builder.setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_DOWN")
        builder.addAction(action)
        val notificationB = builder.build()
        notificationB.flags = notificationB.flags or 48
        notificationManagerP.createNotificationChannel(
            NotificationChannel(
                "Download", getString(
                    AppR.string.download
                ), NotificationManager.IMPORTANCE_LOW
            )
        )
        notificationManagerP.notify(2147483643, notificationB)
    }

    fun startMerge(arrayList: ArrayList<ImageItem?>?, isOk: Boolean) {
        if (arrayList == null || arrayList.isEmpty() || this.appCtx == null || this.uiHandler == null) {
            return
        }
        var mergeDir = this.mergeDir
        if (TextUtils.isEmpty(mergeDir)) {
            mergeDir = MainUtil.newCacheFilePath(this.appCtx)
            if (TextUtils.isEmpty(mergeDir) || !File(mergeDir!!).mkdir()) {
                return
            } else {
                this.mergeDir = mergeDir
            }
        }
        val path = mergeDir
        this.merging = true
        val mergeDone = this.mergeDone
        this.mergeDoneBase = mergeDone
        this.mergeFailBase = this.mergeFail
        this.mergeToastTime = 0L
        val context: Context? = if (mergeDone == 0) this.appCtx else null
        val notificationManagerP: NotificationManager? = if (mergeDone == 0 && context != null) MainApp.Companion.notifications(context) else null
        if (mergeDone == 0 && context != null && notificationManagerP != null) {
            if (this.mergeNoti != null) {
                notificationManagerP.cancel(2147483644)
            }
            var mergeTotal = this.mergeTotal
            if (mergeTotal < 0) {
                this.mergeTotal = 0
                mergeTotal = 0
            }
            if (this.mergeDone > mergeTotal) {
                this.mergeDone = mergeTotal
            }
            if (this.mergeFail > mergeTotal) {
                this.mergeFail = mergeTotal
            }
            val intent = Intent(this.appCtx, com.mycompany.app.ui.lists.DownloadListActivity::class.java as Class<*>)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val activity = PendingIntent.getActivity(this.appCtx, nextNotiId(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val cancelIntent = Intent("ch.opumlabs.opumbrowser.ACTION_IMAGE_CANCEL")
            cancelIntent.setPackage(getPackageName())
            val action = NotificationCompat.Action(
                AppR.drawable.outline_close_black_24,
                getString(AppR.string.cancel),
                PendingIntent.getBroadcast(this.appCtx, nextNotiId(), cancelIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            )
            val sb = StringBuilder()
            sb.append(getString(AppR.string.success))
            sb.append(" (")
            sb.append(this.mergeDone - this.mergeFail)
            sb.append(")  ")
            sb.append(getString(AppR.string.fail))
            sb.append(" (")
            sb.append(this.mergeFail)
            sb.append(")")
            val builder = NotificationCompat.Builder(this.appCtx!!, "Download")
            builder.setSmallIcon(AppR.drawable.ic_download)
            builder.setContentTitle(
                this.mergeDone.toString() + " / " + this.mergeTotal + " - " + getString(
                    AppR.string.down_image
                )
            )
            builder.setContentText(sb.toString())
            builder.setProgress(this.mergeTotal, this.mergeDone, false)
            builder.setContentIntent(activity)
            builder.setPriority(1)
            builder.setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_DOWN")
            builder.addAction(action)
            this.mergeNoti = builder
            val notificationB = builder.build()
            setNotiFlags(notificationB, 1)
            notificationManagerP.createNotificationChannel(
                NotificationChannel(
                    "Download", getString(
                        AppR.string.download
                    ), NotificationManager.IMPORTANCE_LOW
                )
            )
            notificationManagerP.notify(2147483644, notificationB)
            this.mergeNotiTime = System.currentTimeMillis()
        }
        val size = arrayList.size
        val batch = if (10 > size) size else 10
        var perBatch = size / batch
        if (size % batch != 0) {
            perBatch++
        }
        val start = perBatch
        val cancelMergeStep: CancelMergeStep = CancelMergeStep()
        var index = 0
        while (index < batch && this.merging) {
            runInBackground(SaveImageAtTask(this, arrayList, path, start, batch, index, isOk, cancelMergeStep))
            index++
        }
    }

    fun finishMerge() {
        if (this.appCtx != null) {
            this.merging = false
            this.mergeQueue = null
            this.mergeTotal = 0
            this.mergeDone = 0
            this.mergeFail = 0
            this.mergeDoneBase = 0
            this.mergeFailBase = 0
            val mergeDir = this.mergeDir
            this.mergeDir = null
            MainUtil.noOp(mergeDir)
            if (this.mergeNoti != null) {
                this.mergeNoti = null
                val notificationManagerP: NotificationManager? = MainApp.Companion.notifications(this.appCtx)
                if (notificationManagerP != null) {
                    notificationManagerP.cancel(2147483644)
                }
            }
        }
    }

    fun updateNoti(downItem: DownItem?) {
        val percent: Float
        val intent: Intent?
        try {
            if (this.appCtx == null || downItem == null || downItem.uriItem == null || downItem.builder == null) {
                if (downItem == null || downItem.status != 5) {
                    return
                }
                showFailNoti(downItem)
                return
            }
            val status = downItem.status
            if (status == 6) {
                downItem.prevStatus = status
                removeRunning(downItem)
                return
            }
            val totalSize = max(downItem.totalSize, downItem.lastSize)
            val readSize = downItem.readSize
            val isIndeterminate = readSize == 0L && totalSize > 0
            val idle = if (!downItem.isRunning && downItem.prevStatus == downItem.status && downItem.isIndeterminate == isIndeterminate) false else true
            val prevStatus = downItem.status
            downItem.prevStatus = prevStatus
            downItem.isIndeterminate = isIndeterminate
            downItem.isRunning = false
            if (prevStatus == 1 || prevStatus == 7) {
                var name = downItem.uriItem!!.name
                val indeterminate = isIndeterminate
                val idleFlag = idle
                val firstSize = downItem.firstSize
                if (indeterminate) {
                    percent = 0.0f
                    if (totalSize - firstSize > 0) {
                        val sb = StringBuilder()
                        appendSize(sb, totalSize)
                        sb.append("  ")
                        sb.append(name)
                        name = sb.toString()
                    }
                } else {
                    percent = (totalSize * 100.0f) / readSize
                    if (totalSize - firstSize > 0) {
                        val now = System.currentTimeMillis() - downItem.startTime
                        val readSizeNow = downItem.readSize
                        val firstSizeNow = downItem.firstSize
                        val remaining = (((readSizeNow - firstSizeNow) * now) / (totalSize - firstSizeNow))
                        val sb = StringBuilder()
                        if (downItem.cancelRequested) {
                            if (TextUtils.isEmpty(this.downloadTitle)) {
                                this.downloadTitle = getString(AppR.string.finishing)
                            }
                            sb.append(this.downloadTitle)
                        } else {
                            appendSize(sb, totalSize)
                            sb.append(" / ")
                            appendSize(sb, downItem.readSize)
                        }
                        sb.append(" (")
                        appendTime(sb, remaining - now)
                        sb.append(")  ")
                        sb.append(name)
                        name = sb.toString()
                    }
                }
                if (idleFlag) {
                    val idleIntent = Intent(this.appCtx, com.mycompany.app.ui.lists.DownloadListActivity::class.java as Class<*>)
                    idleIntent.putExtra("EXTRA_ID", downItem.id)
                    idleIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val activity = PendingIntent.getActivity(this.appCtx, nextNotiId(), idleIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                    val builder = NotificationCompat.Builder(this.appCtx!!, "Download")
                    builder.setSmallIcon(AppR.drawable.ic_download)
                    builder.setContentTitle(name)
                    builder.setContentText("")
                    builder.setProgress(100, Math.round(percent), downItem.isIndeterminate)
                    builder.setContentIntent(activity)
                    builder.setPriority(1)
                    builder.setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_DOWN")
                    builder.addAction(cancelAction(downItem))
                    builder.addAction(pauseAction(downItem))
                    downItem.builder = builder
                } else {
                    val builder = downItem.builder
                    builder!!.javaClass
                    builder.setContentTitle(name)
                    builder.setContentText("")
                    builder.setProgress(100, Math.round(percent), downItem.isIndeterminate)
                }
            } else if (prevStatus == 2) {
                if (!idle) {
                    return
                }
                var name = downItem.uriItem!!.name
                val firstSize = downItem.firstSize
                if (isIndeterminate) {
                    if (totalSize - firstSize > 0) {
                        val sb = StringBuilder()
                        appendSize(sb, totalSize)
                        sb.append("  ")
                        sb.append(name)
                        name = sb.toString()
                    }
                } else if (totalSize - firstSize > 0) {
                    val sb = StringBuilder()
                    appendSize(sb, totalSize)
                    sb.append(" / ")
                    appendSize(sb, downItem.readSize)
                    sb.append("  ")
                    sb.append(name)
                    name = sb.toString()
                }
                val progressIntent = Intent(this.appCtx, com.mycompany.app.ui.lists.DownloadListActivity::class.java as Class<*>)
                progressIntent.putExtra("EXTRA_ID", downItem.id)
                progressIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val activity = PendingIntent.getActivity(this.appCtx, nextNotiId(), progressIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                val builder = NotificationCompat.Builder(this.appCtx!!, "Download")
                builder.setSmallIcon(AppR.drawable.baseline_file_download_white_24)
                builder.setContentTitle(name)
                builder.setContentText(getString(AppR.string.paused))
                builder.setProgress(0, 0, false)
                builder.setContentIntent(activity)
                builder.setPriority(1)
                builder.setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_DOWN")
                builder.addAction(cancelAction(downItem))
                builder.addAction(pauseAction(downItem))
                downItem.builder = builder
            } else if (prevStatus == 3) {
                if (!idle) {
                    return
                }
                var name = downItem.uriItem!!.name
                if (readSize > 0) {
                    val sb = StringBuilder()
                    appendSize(sb, downItem.readSize)
                    sb.append("  ")
                    sb.append(name)
                    name = sb.toString()
                }
                if (downItem.noteFlag) {
                    intent = Intent(this.appCtx, com.mycompany.app.ui.lists.DownloadListActivity::class.java as Class<*>)
                    intent.putExtra("EXTRA_ID", downItem.id)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                } else {
                    intent = Intent(this.appCtx, MainLauncher::class.java as Class<*>)
                    intent.putExtra("EXTRA_NOTI", downItem.path)
                    val mime = MainUtil.mimeForUrl(downItem.uriItem!!.name)
                    if (!TextUtils.isEmpty(mime)) {
                        intent.putExtra("EXTRA_TYPE", mime)
                    }
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
                val activity = PendingIntent.getActivity(this.appCtx, nextNotiId(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                val builder = NotificationCompat.Builder(this.appCtx!!, "Download")
                builder.setSmallIcon(AppR.drawable.outline_download_done_white_24)
                builder.setContentTitle(name)
                builder.setContentText(getString(AppR.string.down_complete))
                builder.setProgress(0, 0, false)
                builder.setContentIntent(activity)
                builder.setPriority(1)
                builder.setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_DOWN")
                downItem.builder = builder
                val bitmapY = MainUtil.resourceBitmap(this.appCtx, AppR.drawable.baseline_offline_pin_gray_20)
                if (MainUtil.isBitmapOk(bitmapY)) {
                    downItem.builder!!.setLargeIcon(bitmapY)
                }
            } else if (prevStatus == 4 || prevStatus == 5) {
                if (!idle) {
                    return
                }
                val errorRes: Int = errorText(downItem)
                val endIntent = Intent(this.appCtx, com.mycompany.app.ui.lists.DownloadListActivity::class.java as Class<*>)
                endIntent.putExtra("EXTRA_ID", downItem.id)
                endIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val activity = PendingIntent.getActivity(this.appCtx, nextNotiId(), endIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                val builder = NotificationCompat.Builder(this.appCtx!!, "Download")
                builder.setSmallIcon(AppR.drawable.outline_error_white_24)
                builder.setContentTitle(downItem.uriItem!!.name)
                builder.setContentText(getString(errorRes))
                builder.setProgress(0, 0, false)
                builder.setContentIntent(activity)
                builder.setPriority(1)
                builder.setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_DOWN")
                builder.addAction(cancelAction(downItem))
                builder.addAction(retryAction(downItem))
                downItem.builder = builder
                val bitmapY = MainUtil.resourceBitmap(this.appCtx, AppR.drawable.baseline_error_gray_20)
                if (MainUtil.isBitmapOk(bitmapY)) {
                    downItem.builder!!.setLargeIcon(bitmapY)
                }
            }
            val notificationB = downItem.builder!!.build()
            setNotiFlags(notificationB, downItem.status)
            addRunning(downItem)
            val notificationManagerP: NotificationManager? = MainApp.Companion.notifications(this.appCtx)
            if (notificationManagerP != null) {
                notificationManagerP.notify(downItem.notifyId, notificationB)
            }
        } catch (unused: Exception) {
        }
    }

    fun addRunning(downItem: DownItem?) {
        if (downItem != null) {
            try {
                synchronized(this.runningLock) {
                    val arrayList = this.runningItems
                    if (arrayList == null) {
                        this.runningItems = ArrayList<Any?>()
                    } else if (arrayList.contains(downItem)) {
                        return
                    }
                    this.runningItems!!.add(downItem)
                }
            } catch (unused: Exception) {
            }
        }
    }

    fun onItemProgress(downItem: DownItem?) {
        if (this.appCtx == null || downItem == null || downItem.uriItem == null || downItem.builder == null) {
            if (downItem == null || downItem.status != 5) {
                return
            }
            showFailNoti(downItem)
            return
        }
        val status = downItem.status
        if (status == 6) {
            downItem.prevStatus = status
            removeRunning(downItem)
            return
        }
        var listenerFocused = false
        if (status != 1 && status != 7) {
            this.waitingNoti = true
            updateNoti(downItem)
            this.waitingNoti = false
            return
        }
        try {
            synchronized(this.progressLock) {
                val arrayList = this.finishedItems
                if (arrayList == null) {
                    val finishedItems: ArrayList<Any?> = ArrayList<Any?>()
                    this.finishedItems = finishedItems
                    finishedItems.add(downItem)
                } else {
                    val noItems = arrayList.isEmpty()
                    val finishedList = this.finishedItems!!
                    if (noItems) {
                        finishedList.add(downItem)
                    } else if (finishedList.contains(downItem)) {
                        return
                    } else {
                        this.finishedItems!!.add(downItem)
                    }
                }
                if (this.busyState.toInt() == 2) {
                    listenerFocused = true
                } else {
                    val downBusyListener = this.busyListener
                    if (downBusyListener != null) {
                        listenerFocused = downBusyListener.hasFocus()
                    }
                }
                val handler: Handler? = if (listenerFocused) null else this.uiHandler
                if (listenerFocused || handler == null) {
                    return
                }
                handler.removeCallbacks(this.progressRunnable)
                this.uiHandler!!.postDelayed(this.progressRunnable, 400L)
            }
        } catch (unused: Exception) {
        }
    }

    fun flushFinished(sync: Boolean) {
        try {
            synchronized(this.progressLock) {
                val arrayList = this.finishedItems
                if (arrayList == null || arrayList.isEmpty()) {
                    return
                }
                val handler = this.uiHandler
                if (handler != null) {
                    handler.removeCallbacks(this.progressRunnable)
                    val runnable = this.progressRunnable
                    if (sync) {
                        handler.postDelayed(runnable, 400L)
                    } else {
                        handler.post(runnable)
                    }
                }
            }
        } catch (unused: Exception) {
        }
    }

    fun addDownload(url: String?, referer: String?, uriItem: UriItem?, oldParts: Int, isSecret: Boolean, sync: Boolean) {
        val newUriItem: UriItem?
        val isReserved: Boolean
        if (TextUtils.isEmpty(url) || uriItem == null || TextUtils.isEmpty(uriItem.uriString) || this.appCtx == null) {
            return
        }
        if (sync || !isDownloading(uriItem.uriString, isSecret)) {
            newUriItem = uriItem
        } else {
            val name = uriItem.name
            var targetItem: UriItem? = null
            val numItemG: NumItem? = if (!TextUtils.isEmpty(name)) MainUri.splitNumber(name) else null
            if (!TextUtils.isEmpty(name) && numItemG != null) {
                val uriItemC = MainUri.createUri(
                    this.appCtx,
                    PrefPath.downFolder(this.appCtx),
                    null,
                    numItemG.base + "_" + System.currentTimeMillis() + numItemG.suffix
                )
                if (uriItemC != null && !isDownloading(uriItemC.uriString, isSecret)) {
                    targetItem = uriItemC
                }
            }
            if (targetItem == null) {
                val handler = this.uiHandler
                if (handler != null) {
                    handler.post(ExistFileToastTask())
                    return
                }
                return
            }
            newUriItem = targetItem
        }
        val archiveIcon = MainUtil.archiveIconFor(newUriItem.name)
        val isGif = archiveIcon == 4
        var status = 2
        if (BrowserSettings.downLimit.value <= 0 || runningCount(isGif) < BrowserSettings.downLimit.value) {
            isReserved = false
            status = 1
        } else {
            isReserved = true
        }
        val itemId = insertItem(this.appCtx, status, archiveIcon, url, referer, newUriItem, newUriItem.size, 0L, isReserved, isSecret, SecretSettings.secretDown.value, true)
        if (itemId > 0) {
            var parts = oldParts
            if (parts < 2) {
                parts = 1
            }
            val downItem = DownItem()
            downItem.isSecret = isSecret
            downItem.id = itemId
            downItem.notifyId = ((itemId % 2147483635).toInt()) + 1
            downItem.status = status
            downItem.fileType = archiveIcon
            downItem.url = url
            downItem.referer = referer
            downItem.path = newUriItem.uriString
            downItem.readSize = newUriItem.size
            downItem.lastSize = 0L
            downItem.uriItem = newUriItem
            downItem.oldParts = parts
            downItem.urlType = urlType(url)
            downItem.isReserved = isReserved
            synchronized(this.itemLock) {
                var arrayList = this.downItems
                if (arrayList == null) {
                    arrayList = ArrayList<Any?>()
                    this.downItems = arrayList
                }
                arrayList.add(downItem)
            }
            if (isReserved) {
                showProgressNoti(downItem)
                val handler: Handler? = if (sync) null else this.uiHandler
                if (sync || handler == null) {
                    return
                }
                handler.post(RegisteredToastTask())
                return
            }
            if (!sync) {
                val handler = this.uiHandler
                if (handler == null) {
                    return
                } else {
                    handler.post(DownStartToastTask())
                }
            }
            retryLater(downItem)
        }
    }

    fun startNext() {
        if (BrowserSettings.downLimit.value > 0) {
            synchronized(this.itemLock) {
                val arrayList = this.downItems
                if (arrayList == null || arrayList.isEmpty()) {
                    return
                }
                if (this.appCtx == null || runningCount(false) >= BrowserSettings.downLimit.value) {
                    return
                }
                synchronized(this.itemLock) {
                    val downItems = this.downItems
                    val size = downItems!!.size
                    var index = 0
                    while (index < size) {
                        val rawDownItem: Any? = downItems.get(index)
                        index++
                        val downItem = rawDownItem as DownItem?
                        if (downItem != null) {
                            val context = this.appCtx
                            if (downItem.status == 2 && downItem.isReserved) {
                                downItem.status = 1
                                downItem.isReserved = false
                                setStatus(context, downItem.id, downItem.readSize, downItem.totalSize, false)
                                notifyProgress(downItem.status, downItem.id)
                                retryLater(downItem)
                                break
                            }
                        }
                    }
                }
            }
        }
    }

    fun cancelAction(downItem: DownItem?): NotificationCompat.Action? {
        if (this.appCtx == null || downItem == null) {
            return null
        }
        downItem.isIndeterminate = downItem.readSize == 0L && max(downItem.totalSize, downItem.lastSize) > 0
        val intent = Intent("ch.opumlabs.opumbrowser.ACTION_DOWN_CANCEL")
        intent.putExtra("EXTRA_ID", downItem.id)
        intent.putExtra("EXTRA_PATH", downItem.path)
        intent.putExtra("EXTRA_STOP", downItem.isIndeterminate)
        intent.setPackage(getPackageName())
        return NotificationCompat.Action(
            AppR.drawable.outline_close_black_24,
            getString(if (downItem.isIndeterminate) AppR.string.stop else AppR.string.cancel),
            PendingIntent.getBroadcast(this.appCtx, nextNotiId(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        )
    }

    fun ensureDir(path: String?): String? {
        val filePath = MainUtil.downFilePath(this.appCtx, path)
        if (TextUtils.isEmpty(filePath)) {
            return null
        }
        val file = File(filePath!!)
        if (file.isDirectory() || file.mkdir()) {
            return filePath
        }
        return null
    }

    fun pauseAction(downItem: DownItem?): NotificationCompat.Action? {
        if (this.appCtx == null || downItem == null) {
            return null
        }
        if (downItem.status != 2) {
            val intent = Intent("ch.opumlabs.opumbrowser.ACTION_DOWN_PAUSE")
            intent.putExtra("EXTRA_ID", downItem.id)
            intent.putExtra("pause", true)
            intent.setPackage(getPackageName())
            return NotificationCompat.Action(
                AppR.drawable.baseline_pause_black_24,
                getString(AppR.string.pause),
                PendingIntent.getBroadcast(this.appCtx, nextNotiId(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            )
        }
        val labelRes = if (downItem.isReserved) AppR.string.start else AppR.string.resume
        val intent = Intent("ch.opumlabs.opumbrowser.ACTION_DOWN_PAUSE")
        intent.putExtra("EXTRA_ID", downItem.id)
        intent.putExtra("pause", false)
        intent.setPackage(getPackageName())
        return NotificationCompat.Action(
            AppR.drawable.baseline_play_arrow_black_24,
            getString(labelRes),
            PendingIntent.getBroadcast(this.appCtx, nextNotiId(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        )
    }

    fun retryAction(downItem: DownItem?): NotificationCompat.Action? {
        if (this.appCtx == null || downItem == null) {
            return null
        }
        val intent = Intent("ch.opumlabs.opumbrowser.ACTION_DOWN_RETRY")
        intent.putExtra("EXTRA_ID", downItem.id)
        intent.setPackage(getPackageName())
        return NotificationCompat.Action(
            AppR.drawable.baseline_play_arrow_black_24,
            getString(AppR.string.retry),
            PendingIntent.getBroadcast(this.appCtx, nextNotiId(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        )
    }

    fun runningCount(gifOnly: Boolean): Int {
        synchronized(this.itemLock) {
            val arrayList = this.downItems
            var running = 0
            if (arrayList == null || arrayList.isEmpty()) {
                return 0
            }
            val downItems = this.downItems
            val size = downItems!!.size
            var index = 0
            while (index < size) {
                val rawDownItem: Any? = downItems.get(index)
                index++
                val downItem = rawDownItem as DownItem?
                if (downItem != null && downItem.status == 1 && (!gifOnly || downItem.fileType == 4)) {
                    running++
                }
            }
            return running
        }
    }

    fun appendTime(sb: StringBuilder?, millis: Long) {
        if (millis <= 0) {
            if (TextUtils.isEmpty(this.secondText)) {
                this.secondText = getString(AppR.string.time_s)
            }
            sb!!.append("0")
            sb.append(this.secondText)
            return
        }
        val seconds = millis / 1000
        if (seconds == 0L) {
            if (TextUtils.isEmpty(this.secondText)) {
                this.secondText = getString(AppR.string.time_s)
            }
            sb!!.append("1")
            sb.append(this.secondText)
            return
        }
        val minutes = millis / 60000
        if (minutes == 0L) {
            if (TextUtils.isEmpty(this.secondText)) {
                this.secondText = getString(AppR.string.time_s)
            }
            sb!!.append(seconds)
            sb.append(this.secondText)
            return
        }
        var hours = millis / 3600000
        if (hours == 0L) {
            if (TextUtils.isEmpty(this.minuteText)) {
                this.minuteText = getString(AppR.string.time_m)
            }
            if (TextUtils.isEmpty(this.secondText)) {
                this.secondText = getString(AppR.string.time_s)
            }
            sb!!.append(minutes)
            sb.append(this.minuteText)
            sb.append(" ")
            sb.append((millis % 60000) / 1000)
            sb.append(this.secondText)
            return
        }
        val days = millis / 86400000
        val hourText = this.hourText
        if (days != 0L) {
            if (TextUtils.isEmpty(hourText)) {
                this.hourText = getString(AppR.string.time_h)
            }
            if (hours > 999) {
                hours = 999
            }
            sb!!.append(hours)
            sb.append(this.hourText)
            return
        }
        if (TextUtils.isEmpty(hourText)) {
            this.hourText = getString(AppR.string.time_h)
        }
        if (TextUtils.isEmpty(this.minuteText)) {
            this.minuteText = getString(AppR.string.time_m)
        }
        sb!!.append(hours)
        sb.append(this.hourText)
        sb.append(" ")
        sb.append((millis % 3600000) / 60000)
        sb.append(this.minuteText)
    }

    /** Bound by the download job (Android 14+): register with MainApp and take the pending downloads, like
     *  onStartCommand does for the foreground-service start. */
    override fun onBind(intent: Intent?): IBinder? {
        this.appCtx = getApplicationContext()
        setTimer(false)
        runInBackground(StartSetupTask())
        return android.os.Binder()
    }

    override fun onCreate() {
        super.onCreate()
        this.started = true
        this.appCtx = getApplicationContext()
        setTimer(false)
        if (this.keepAlive) {
            return
        }
        this.keepAlive = true
        if (BrowserSettings.checkDown.value) {
            BrowserSettings.edit(this.appCtx) { set(BrowserSettings.checkDown, false) }
        }
        runInBackground(CreateSetupTask())
    }

    override fun onDestroy() {
        try {
            releaseService()
            super.onDestroy()
        } catch (_ex: Throwable) {
            throw RuntimeException(_ex)
        }
    }

    /**
     * Android 15+: the daily time for data-sync services (6 h) is used up. The service has to stop within seconds or
     * Android ends the app; the downloads are paused like the background timeout does it (they continue when OPUM
     * is opened again), and a notification says why (2026-11-20).
     */
    override fun onTimeout(startId: Int, fgsType: Int) {
        try {
            stopAll(false, true)
        } catch (_: Exception) {
        }
        try {
            val ctx = this.appCtx ?: getApplicationContext()
            val nm = MainApp.Companion.notifications(ctx)
            if (nm != null) {
                val open = Intent(ctx, com.mycompany.app.ui.lists.DownloadListActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val builder = NotificationCompat.Builder(ctx, "Download")
                    .setSmallIcon(AppR.drawable.outline_error_white_24)
                    .setContentTitle(getString(AppR.string.down_paused_title))
                    .setContentText(getString(AppR.string.down_paused_limit))
                    .setStyle(NotificationCompat.BigTextStyle().bigText(getString(AppR.string.down_paused_limit)))
                    .setContentIntent(PendingIntent.getActivity(ctx, nextNotiId(), open, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
                    .setAutoCancel(true)
                    .setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_DOWN")
                nm.createNotificationChannel(NotificationChannel("Download", getString(AppR.string.download), NotificationManager.IMPORTANCE_LOW))
                nm.notify(2147483644, builder.build())
            }
        } catch (_: Exception) {
        }
        try {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } catch (_: Exception) {
        }
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        this.appCtx = getApplicationContext()
        setTimer(false)
        runInBackground(StartSetupTask())
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onTaskRemoved(intent: Intent?) {
        stopAll(false, false)
        super.onTaskRemoved(intent)
    }

    fun cancelMerge() {
        this.merging = false
        this.mergeQueue = null
        val mergeDir = this.mergeDir
        this.mergeDir = null
        MainUtil.noOp(mergeDir)
        if (this.appCtx == null || this.mergeNoti == null) {
            return
        }
        this.mergeNoti = null
        var mergeTotal = this.mergeTotal
        if (mergeTotal < 0) {
            this.mergeTotal = 0
            mergeTotal = 0
        }
        if (this.mergeDone > mergeTotal) {
            this.mergeDone = mergeTotal
        }
        if (this.mergeFail > mergeTotal) {
            this.mergeFail = mergeTotal
        }
        val intent = Intent(this.appCtx, com.mycompany.app.ui.lists.DownloadListActivity::class.java as Class<*>)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val activity = PendingIntent.getActivity(this.appCtx, nextNotiId(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        this.mergeTotal = 0
        this.mergeDone = 0
        this.mergeFail = 0
        this.mergeDoneBase = 0
        this.mergeFailBase = 0
        val builder = NotificationCompat.Builder(this.appCtx!!, "Download")
        builder.setSmallIcon(AppR.drawable.outline_download_done_white_24)
        builder.setContentTitle(getString(AppR.string.down_image))
        builder.setContentText(
            getString(AppR.string.success) + " (" + (this.mergeDone - this.mergeFail) + ")  " + getString(
                AppR.string.fail
            ) + " (" + this.mergeFail + ")"
        )
        builder.setProgress(0, 0, false)
        builder.setContentIntent(activity)
        builder.setPriority(1)
        builder.setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_DOWN")
        val notificationB = builder.build()
        setNotiFlags(notificationB, 0)
        val notificationManagerP: NotificationManager? = MainApp.Companion.notifications(this.appCtx)
        if (notificationManagerP != null) {
            notificationManagerP.notify(2147483644, notificationB)
        }
        notifyBusy(true)
        stopIfIdle(false)
    }

    fun removeRunning(downItem: DownItem?) {
        if (this.appCtx == null || downItem == null || downItem.builder == null) {
            return
        }
        downItem.builder = null
        try {
            synchronized(this.runningLock) {
                try {
                    val arrayList = this.runningItems
                    if (arrayList != null && !arrayList.isEmpty()) {
                        this.runningItems!!.remove(downItem)
                    }
                } catch (th: Throwable) {
                    throw th
                }
            }
        } catch (unused: Exception) {
        }
        try {
            synchronized(this.runningLock) {
                val waitingItems = this.waitingItems
                if (waitingItems == null) {
                    this.waitingItems = ArrayList<Any?>()
                    this.waitingItems!!.add(downItem)
                } else if (!waitingItems.contains(downItem)) {
                    this.waitingItems!!.add(downItem)
                }
            }
        } catch (unused2: Exception) {
        }
        val notificationManagerP: NotificationManager? = MainApp.Companion.notifications(this.appCtx)
        if (notificationManagerP != null) {
            notificationManagerP.cancel(downItem.notifyId)
        }
    }

    fun isDownloading(uriString: String?, isSecret: Boolean): Boolean {
        var index = 0
        if (TextUtils.isEmpty(uriString)) {
            return false
        }
        synchronized(this.itemLock) {
            val arrayList = this.downItems
            if (arrayList != null && !arrayList.isEmpty()) {
                val downItems = this.downItems
                val size = downItems!!.size
                while (index < size) {
                    val rawDownItem: Any? = downItems.get(index)
                    index++
                    val downItem = rawDownItem as DownItem?
                    if (downItem != null && uriString == downItem.path) {
                        return true
                    }
                }
            }
            return hasPath(this.appCtx, uriString, isSecret)
        }
    }

    /**
     * Muxes a separately downloaded video and audio file into one mp4 and saves it to the item's
     * destination. If muxing fails the video is saved alone and the audio is kept as a separate
     * .aac download.
     */
    fun muxAudio(item: DownItem?, video: String?, audio: String?): Boolean {
        if (item == null) {
            return false
        }
        var out: String? = null
        var ok = false
        try {
            out = MainUtil.downFilePath(this.appCtx, item.path) + ".mp4"
            ok = mux(item, out, video, audio)
        } catch (unused: Exception) {
        } catch (unused: OutOfMemoryError) {
        }
        if (ok) {
            if (!TextUtils.isEmpty(video)) {
                File(video!!).delete()
            }
            if (!TextUtils.isEmpty(audio)) {
                File(audio!!).delete()
            }
            return copyFile(item, out, item.path, true)
        }
        if (!TextUtils.isEmpty(out)) {
            File(out!!).delete()
        }
        val saved = copyFile(item, video, item.path, true)
        val audioSize = MainUtil.fileSizeOf(this.appCtx, audio)
        val target = MainUri.createUri(this.appCtx, PrefPath.downFolder(this.appCtx), null, MainUtil.stripExtension(item.uriItem!!.name) + ".aac")
        if (target != null) {
            MainUtil.moveFileOrUri(this.appCtx, audio, target.uriString)
            insertItem(
                this.appCtx,
                3,
                6,
                item.url,
                item.referer,
                target,
                audioSize,
                audioSize,
                false,
                item.isSecret,
                SecretSettings.secretDown.value,
                false
            )
            putSub(this.appCtx, 0, item.path, target.uriString)
            item.hasSub = true
        }
        return saved
    }

    @Throws(Exception::class)
    private fun mux(item: DownItem?, out: String?, video: String?, audio: String?): Boolean {
        val muxer = MediaMuxer(out!!, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val videoIn = MediaExtractor()
        videoIn.setDataSource(video!!)
        videoIn.selectTrack(0)
        val videoFormat = videoIn.getTrackFormat(0)
        val videoTrack = muxer.addTrack(videoFormat)
        val audioIn = MediaExtractor()
        audioIn.setDataSource(audio!!)
        audioIn.selectTrack(0)
        val audioFormat = audioIn.getTrackFormat(0)
        val audioTrack = muxer.addTrack(audioFormat)
        var pixels = 0
        try {
            pixels = videoFormat.getInteger("width") * videoFormat.getInteger("height")
        } catch (unused: Exception) {
        }
        var adts = false
        try {
            adts = audioFormat.getInteger("is-adts") == 1
        } catch (unused: Exception) {
        }
        val buffer = ByteBuffer.allocate(max(230400, pixels))
        val info = MediaCodec.BufferInfo()
        item!!.cancelRequested = true
        item.startTime = System.currentTimeMillis()
        item.lastTime = 0
        item.firstSize = 0
        item.readSize = File(video).length() + File(audio).length()
        item.totalSize = 0
        muxer.start()
        videoIn.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
        val videoOk = copyTrack(item, videoIn, muxer, videoTrack, buffer, info, false)
        videoIn.release()
        var audioOk = false
        if (videoOk) {
            audioIn.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            audioOk = copyTrack(item, audioIn, muxer, audioTrack, buffer, info, adts)
        }
        audioIn.release()
        muxer.stop()
        muxer.release()
        return videoOk && audioOk
    }

    /** Copies all samples of one track; ADTS audio has its 7-byte header stripped.  */
    private fun copyTrack(
        item: DownItem?, `in`: MediaExtractor, muxer: MediaMuxer?, track: Int, buffer: ByteBuffer?,
        info: MediaCodec.BufferInfo?, adts: Boolean
    ): Boolean {
        var wrote = false
        while (true) {
            var size = `in`.readSampleData(buffer!!, 0)
            if (adts) {
                info!!.offset = 7
                size -= 7
            } else {
                info!!.offset = 0
            }
            info.size = size
            if (size < 0) {
                return wrote
            }
            info.presentationTimeUs = `in`.getSampleTime()
            info.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME
            muxer!!.writeSampleData(track, buffer, info)
            if (!`in`.advance()) {
                return true
            }
            addProgress(item, info.size.toLong())
            wrote = true
        }
    }

    /** Adds `bytes` to the item's progress and refreshes the notification at most every 800 ms.  */
    private fun addProgress(item: DownItem?, bytes: Long) {
        item!!.totalSize += bytes
        if (item.readSize != 0L && item.readSize < item.totalSize) {
            item.readSize = item.totalSize
        }
        val now = System.currentTimeMillis()
        if (item.lastTime == 0L || now - item.lastTime > 800) {
            item.lastTime = now
            onItemProgress(item)
            notifyItemChanged(item)
        }
    }

    /**
     * Joins the downloaded part files ("...vf" for video, "...af" for audio) in the item's temp
     * folder into the first part, deleting the others. Returns the joined file, or null.
     */
    fun segmentPath(item: DownItem?, audio: Boolean): String? {
        if (item == null) {
            return null
        }
        val files = File(item.segmentDir!!).listFiles()
        if (files == null || files.size == 0) {
            return null
        }
        val suffix = if (audio) "af" else "vf"
        val parts: ArrayList<ChildItem?> = ArrayList()
        for (file in files) {
            val name = file.getName()
            if (!TextUtils.isEmpty(name) && name.endsWith(suffix) && file.length() != 0L) {
                val part = ChildItem()
                part.path = file.getPath()
                part.name = name
                parts.add(part)
            }
        }
        if (parts.isEmpty()) {
            return null
        }
        try {
            Collections.sort<ChildItem?>(parts, SortDown())
        } catch (unused: Exception) {
        }
        val first = (parts.get(0) as ChildItem).path
        val firstSize = File(first!!).length()
        item.cancelRequested = true
        item.startTime = System.currentTimeMillis()
        item.lastTime = 0
        item.firstSize = firstSize
        item.readSize = item.totalSize
        item.totalSize = firstSize
        var out: OutputStream?
        try {
            out = MainUtil.openOutput(this.appCtx, first, true)
        } catch (unused: Exception) {
            out = null
        }
        if (out == null) {
            return null
        }
        for (index in 1..<parts.size) {
            val path = (parts.get(index) as ChildItem).path
            val size = File(path!!).length()
            if (!appendFile(path, out)) {
                try {
                    out.close()
                } catch (unused: Exception) {
                }
                return null
            }
            item.totalSize += size
            if (item.readSize != 0L && item.readSize < item.totalSize) {
                item.readSize = item.totalSize
            }
            MainUtil.deleteUri(this.appCtx, path)
            val now = System.currentTimeMillis()
            if (item.lastTime == 0L || now - item.lastTime > 800) {
                item.lastTime = now
                onItemProgress(item)
                notifyItemChanged(item)
            }
        }
        try {
            out.close()
        } catch (unused: Exception) {
        }
        item.totalSize = File(first).length()
        if (item.readSize != 0L && item.readSize < item.totalSize) {
            item.readSize = item.totalSize
        }
        return first
    }

    private fun appendFile(path: String?, out: OutputStream?): Boolean {
        if (TextUtils.isEmpty(path)) {
            return false
        }
        var `in`: InputStream? = null
        var ok = false
        try {
            `in` = MainUtil.openUriInput(this.appCtx, path)
            val buf = ByteArray(com.mycompany.app.down.DownSavePath.BUFFER_SIZE)
            var fileType: Int
            while ((`in`!!.read(buf, 0, buf.size).also { fileType = it }) != -1) {
                out!!.write(buf, 0, fileType)
            }
            ok = true
        } catch (unused: Exception) {
        }
        if (`in` != null) {
            try {
                `in`.close()
            } catch (unused: Exception) {
            }
        }
        return ok
    }

    /**
     * Copies the finished temp file `src` to the destination content uri `dest`, then
     * deletes the temp file. With `track` the copy is shown as progress.
     */
    fun copyFile(item: DownItem?, src: String?, dest: String?, track: Boolean): Boolean {
        if (this.appCtx == null || item == null || TextUtils.isEmpty(src) || TextUtils.isEmpty(dest)) {
            return false
        }
        var `in`: InputStream? = null
        var out: OutputStream? = null
        var ok = false
        try {
            `in` = MainUtil.openFileInput(src)
            out = this.appCtx!!.getContentResolver().openOutputStream(Uri.parse(dest))
            val length = File(src!!).length()
            val buf = MainUri.bufferFor(length)
            if (track) {
                item.cancelRequested = true
                item.startTime = System.currentTimeMillis()
                item.lastTime = 0
                item.firstSize = 0
                item.readSize = length
                item.totalSize = 0
            }
            var fileType: Int
            while ((`in`!!.read(buf, 0, buf.size).also { fileType = it }) != -1) {
                out!!.write(buf, 0, fileType)
                addProgress(item, fileType.toLong())
            }
            ok = true
        } catch (unused: Exception) {
        }
        if (out != null) {
            try {
                out.close()
            } catch (unused: Exception) {
            }
        }
        if (`in` != null) {
            try {
                `in`.close()
            } catch (unused: Exception) {
            }
        }
        if (ok) {
            File(src!!).delete()
        }
        return ok
    }

    /**
     * The joined segments of an HLS video: an MPEG-TS stream saved under an ".mp4" name is rewritten as a real MP4
     * first (DownHls.remuxTsToMp4; 2026-11-20). When that fails, or the name is not .mp4, the stream is saved as it is.
     */
    private fun saveStream(item: DownItem, joined: String): Boolean {
        val name = item.uriItem?.name ?: ""
        if (name.endsWith(".mp4", true) && DownHls.isMpegTs(joined)) {
            val out = MainUtil.downFilePath(this.appCtx, item.path) + ".remux.mp4"
            item.cancelRequested = true
            item.startTime = System.currentTimeMillis()
            item.lastTime = 0
            item.firstSize = 0
            item.readSize = File(joined).length() * 2
            item.totalSize = 0
            if (DownHls.remuxTsToMp4(joined, out) { addProgress(item, it) }) {
                File(joined).delete()
                return copyFile(item, out, item.path, true)
            }
        }
        return copyFile(item, joined, item.path, true)
    }

    /**
     * Writes the finished parts ("...vf") of a plain download one after the other into [dest] and deletes each part
     * once it is copied (2026-11-20). Replaces "join the parts into the first one, then copy that": one full write
     * less and less temporary space. Fails when the total does not match the size the server announced.
     */
    fun copyParts(item: DownItem?, dest: String?): Boolean {
        val ctx = this.appCtx
        if (ctx == null || item == null || TextUtils.isEmpty(dest)) {
            return false
        }
        val files = File(item.segmentDir ?: return false).listFiles()?.filter { it.name.endsWith("vf") && it.length() > 0 }
            ?.sortedBy { it.name } ?: return false
        if (files.isEmpty()) {
            return false
        }
        val total = files.sumOf { it.length() }
        // the expected size: the server's length, or after a restart the saved total (videoLength is not saved;
        // without this a split download stopped half way was "finished" with only its first part)
        val expected = if (item.videoLength > 0) item.videoLength else item.readSize
        if ((expected > 0 && total != expected) || (item.partCount > 1 && files.size != item.partCount)) {
            item.sizeMismatch = true
            return false
        }
        var out: OutputStream? = null
        var ok = false
        try {
            out = ctx.getContentResolver().openOutputStream(Uri.parse(dest))
            item.cancelRequested = true
            item.startTime = System.currentTimeMillis()
            item.lastTime = 0
            item.firstSize = 0
            item.readSize = total
            item.totalSize = 0
            val buf = ByteArray(com.mycompany.app.down.DownSavePath.BUFFER_SIZE)
            for (file in files) {
                MainUtil.openFileInput(file.path).use { input ->
                    var n: Int
                    while ((input!!.read(buf, 0, buf.size).also { n = it }) != -1) {
                        out!!.write(buf, 0, n)
                        addProgress(item, n.toLong())
                    }
                }
                file.delete()
            }
            ok = true
        } catch (unused: Exception) {
        }
        try {
            out?.close()
        } catch (unused: Exception) {
        }
        return ok
    }

    fun finishItem(downItem: DownItem?) {
        var downType: Int = 0
        if (downItem != null) {
            downItem.isRunning = false
            val context = this.appCtx
            val handler: Handler? = if (context == null) null else this.uiHandler
            if (context == null || handler == null) {
                return
            }
            val status = downItem.status
            if (status == 6) {
                removeRunning(downItem)
                return
            }
            if (status == 3 || status == 5) {
                val fileSize = MainUtil.fileSizeOf(context, downItem.path)
                downItem.readSize = fileSize
                downItem.totalSize = fileSize
                if (fileSize == 0L) {
                    if (downItem.urlType == 0 && !TextUtils.isEmpty(downItem.url) && downItem.url!!.startsWith(
                            "http://"
                        ) && downItem.url!!.length > 7
                    ) {
                        downItem.url = "https://" + downItem.url!!.substring(7)
                        downItem.status = 1
                        startDownload(downItem)
                        return
                    } else {
                        if (downItem.status == 3) {
                            downItem.status = 4
                        }
                        MainUtil.deleteUri(this.appCtx, downItem.path)
                    }
                } else if (downItem.status == 5) {
                    downItem.status = 3
                }
            }
            val uriItemK: UriItem? = if (downItem.fileType == 4 && downItem.status == 3) MainUtil.gifUriFor(this.appCtx, downItem) else null
            if (downItem.fileType == 4 && downItem.status == 3 && uriItemK != null) {
                val appCtx = this.appCtx
                val id = downItem.id
                val path = downItem.path
                DbBookDown.deleteFile(id, appCtx, path, MainUtil.pageUrlOrNull(path), true)
                downItem.uriItem = uriItemK
                downItem.path = uriItemK.uriString
                downItem.hasSub = true
            }
            insertItem(
                this.appCtx,
                downItem.status,
                downItem.fileType,
                downItem.url,
                downItem.referer,
                downItem.uriItem,
                downItem.readSize,
                downItem.totalSize,
                false,
                downItem.isSecret,
                SecretSettings.secretDown.value,
                false
            )
            val uriItem: UriItem? = if (downItem.status == 3) downItem.uriItem else null
            val fileType = if (downItem.status == 3 && uriItem != null) downItem.fileType else 0
            if (downItem.status == 3 && uriItem != null && fileType != 4 && fileType != 5 && fileType != 6 && ((fileKind(uriItem.name).also {
                    downType = it
                }) == 1 || downType == 2 || downType == 3)) {
                addUri(this.appCtx, downType, uriItem)
            }
            // for the downloads list and the Downloads button (2026-11-20)
            if (downItem.status == 3) {
                DownState.clearFailReason(this.appCtx, downItem.id)
                DownState.markFinished(this.appCtx)
            } else if (downItem.status == 4 || downItem.status == 5) {
                DownState.setFailReason(this.appCtx, downItem.id, errorText(downItem))
            }
            handler.postDelayed(ItemProgressTask(this, downItem), 200L)
        }
    }

    fun checkFile(downItem: DownItem?): Boolean {
        val isOk: Boolean
        if (downItem != null) {
            try {
                synchronized(this.fileLock) {
                    downItem.isMerging = false
                    downItem.wasResumed = false
                    downItem.tempPath = null
                    val arrayList = this.pendingItems
                    if (arrayList == null || arrayList.isEmpty()) {
                        return false
                    }
                    this.pendingItems!!.remove(downItem)
                    try {
                        synchronized(this.fileLock) {
                            val pendingItems = this.pendingItems
                            isOk = if (pendingItems == null || pendingItems.isEmpty()) false else true
                        }
                        return isOk
                    } catch (unused: Exception) {
                    }
                }
            } catch (unused2: Exception) {
            }
        }
        return false
    }

    fun updateItem(downItem: DownItem?) {
        if (downItem != null) {
            synchronized(this.itemLock) {
                if (this.downItems == null) {
                    return
                }
                try {
                    val arrayList: ArrayList<Any?> = ArrayList<Any?>(this.downItems!!)
                    var size = arrayList.size
                    val it: MutableIterator<Any?> = arrayList.iterator()
                    while (it.hasNext()) {
                        val candidate = it.next() as DownItem?
                        if (candidate != null && candidate.id == downItem.id) {
                            it.remove()
                            size--
                            break
                        }
                    }
                    if (size <= 0 || arrayList.size <= 0) {
                        this.downItems = null
                        stopIfIdle(false)
                    } else {
                        this.downItems = arrayList
                        startNext()
                    }
                } catch (unused: Exception) {
                }
            }
        }
    }

    fun runInBackground(runnable: Runnable?) {
        var executorServiceH = this.executorCache
        if (executorServiceH == null || executorServiceH.isShutdown() || executorServiceH.isTerminated()) {
            executorServiceH = MainApp.Companion.executor(this.appCtx)
            if (executorServiceH == null) {
                return
            } else {
                this.executorCache = executorServiceH
            }
        }
        try {
            executorServiceH.execute(runnable)
        } catch (unused: Exception) {
        }
    }

    companion object {
        @JvmField
        var lastNotifyId: Int = 0
        @JvmStatic
        fun setNotiFlags(notification: Notification?, status: Int) {
            if (notification == null) {
                return
            }
            val flags = notification.flags
            if (status == 1) {
                notification.flags = (flags or 32) and (-17)
            } else if (status == 2) {
                notification.flags = flags and (-49)
            } else {
                notification.flags = (flags and (-33)) or 16
            }
        }

        /** The "Download manager" notification for the download job's first moment (before the engine is up). */
        @JvmStatic
        fun placeholderNotification(context: Context): android.app.Notification? = try {
            MainApp.Companion.notifications(context)?.createNotificationChannel(
                NotificationChannel("Download", context.getString(AppR.string.download), NotificationManager.IMPORTANCE_LOW)
            )
            val open = Intent(context, com.mycompany.app.ui.lists.DownloadListActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            NotificationCompat.Builder(context, "Download")
                .setSmallIcon(AppR.drawable.baseline_file_download_white_24)
                .setContentTitle(context.getString(AppR.string.down_manager))
                .setContentIntent(PendingIntent.getActivity(context, 0, open, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
                .setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_DOWN")
                .build()
        } catch (_: Exception) { null }

        /** Free space kept besides the download itself. */
        const val MIN_FREE_BYTES = 32L * 1024 * 1024

        @JvmStatic
        fun errorText(downItem: DownItem?): Int {
            if (downItem == null) {
                return AppR.string.server_error
            }
            if (downItem.status == 5) {
                return AppR.string.invalid_url
            }
            if (downItem.outOfMemory) {
                return AppR.string.out_of_memory
            }
            if (downItem.noSpace) {
                return AppR.string.down_no_space
            }
            if (downItem.sizeMismatch) {
                return AppR.string.down_size_mismatch
            }
            if (downItem.rangeIgnored) {
                return AppR.string.down_no_ranges
            }
            // 401 / 403: the site refuses the download (it needs a login, a cookie or the page it came from)
            if (downItem.httpStatus == 401 || downItem.httpStatus == 403) {
                return AppR.string.down_forbidden
            }
            // 429: the site limits how many downloads / requests it accepts
            if (downItem.httpStatus == 429) {
                return AppR.string.down_rate_limited
            }
            // 404 / 410: the file is no longer at that address (moved or deleted on the site)
            if (downItem.httpStatus == 404 || downItem.httpStatus == 410) {
                return AppR.string.down_not_found
            }
            return if (downItem.isLiveStream || Companion.isLiveUrl(downItem.url!!)) AppR.string.live_fail else AppR.string.server_error
        }

        @JvmStatic
        fun appendSize(sb: StringBuilder?, readSize: Long) {
            if (readSize / 1024L == 0L) {
                sb!!.append(readSize)
                sb.append(".0 B")
            } else if (readSize / 1048576L == 0L) {
                sb!!.append(String.format(Locale.US, "%.1f", readSize / 1024.0f))
                sb.append(" KB")
            } else if (readSize / 1073741824 == 0L) {
                sb!!.append(String.format(Locale.US, "%.1f", readSize / 1048576.0f))
                sb.append(" MB")
            } else {
                sb!!.append(String.format(Locale.US, "%.1f", readSize / 1.0737418E9f))
                sb.append(" GB")
            }
        }

        @JvmStatic
        fun urlType(downUrl: String?): Int {
            if (TextUtils.isEmpty(downUrl)) {
                return 0
            }
            if (downUrl!!.startsWith("m3u8:")) {
                return 2
            }
            if (downUrl.startsWith("reddit:")) {
                return 4
            }
            if ((downUrl.startsWith("https://v.redd.it/") && downUrl.contains("DASHPlaylist.mpd")) || downUrl.startsWith(
                    "https://www.redditmedia.com/mediaembed/"
                ) || downUrl.startsWith("https://cdn.embedly.com/widgets/media.html")
            ) {
                return 3
            }
            if (downUrl.startsWith("kakao1:")) {
                return 5
            }
            if (downUrl.startsWith("kakao2:")) {
                return 6
            }
            if (downUrl.startsWith("dzen1:")) {
                return 7
            }
            if (downUrl.startsWith("dzen2:")) {
                return 8
            }
            if (downUrl.startsWith("vimeo1:")) {
                return 9
            }
            if (downUrl.startsWith("vimeo2:")) {
                return 10
            }
            if (downUrl.startsWith("tsfile:")) {
                return 11
            }
            if (downUrl.startsWith("tsfake:")) {
                return 12
            }
            if (downUrl.startsWith("m3fake:") || downUrl.startsWith("izle:")) {
                return 1
            }
            if (downUrl.startsWith("torrent:")) {
                return 13
            }
            if (downUrl.startsWith("blob:")) {
                return 14
            }
            if (downUrl.startsWith("mix:")) {
                return 15
            }
            return if (downUrl.endsWith(".m3u8") || "m3u8".equals(
                    MainUtil.fileExtOf(downUrl, false),
                    ignoreCase = true
                )
            ) 1 else 0
        }

        @JvmStatic
        fun nextNotiId(): Int {
            val nextId: Int = (lastNotifyId + 1) % 2147483636
            lastNotifyId = nextId
            if (nextId >= 13) {
                return nextId
            }
            lastNotifyId = 13
            return 13
        }

        @JvmStatic
        fun segmentName(index: Int, digits: Int, segmentDir: String?): String {
            val sbV = StringBuilder().append(segmentDir).append("/")
            sbV.append(MainUtil.zeroPadded(index, digits))
            sbV.append("v")
            return sbV.toString()
        }

        @JvmStatic
        fun isLiveUrl(url: String?): Boolean {
            var length: Int = 0
            val host = MainUtil.hostOf(url, true)
            return if (TextUtils.isEmpty(host) || ((host!!.length + 1).also {
                    length = it
                }) >= url!!.length || url.indexOf("live", length) == -1) false else true
        }

        /**
         * Size of each range part (a multiple of 1 KB), lowering the part count until every part but the
         * last gets at least one unit (4 MB, 8 MB for fbcdn.net). Returns 0 when splitting is not worth it.
         */
        private fun partSize(item: DownItem?, url: String?): Long {
            val host = MainUtil.hostOf(url, true)
            // at least 4 MB per part (8 MB for fbcdn.net; was 1 / 4 MB): files under 8 MB use one connection
            val unit =
                if (!TextUtils.isEmpty(host) && host!!.endsWith("fbcdn.net")) 0x800000L else 0x400000L
            val rest = item!!.videoLength - unit
            if (rest <= 0) {
                return 0
            }
            while (true) {
                val parts = item.oldParts
                if (rest < (parts - 1) * unit) {
                    item.oldParts = parts - 1
                    if (item.oldParts < 2) {
                        return 0
                    }
                    continue
                }
                if (parts < 2) {
                    return 0
                }
                var size = item.videoLength / parts
                if (size % 1024 != 0L) {
                    size += 1024 - size % 1024
                }
                while (rest < (item.oldParts - 1) * size) {
                    size -= 1024
                    if (size < 0) {
                        return 0
                    }
                }
                return max(size, 0)
            }
        }
    }
}
