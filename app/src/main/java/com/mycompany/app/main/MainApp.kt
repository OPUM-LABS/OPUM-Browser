package com.mycompany.app.main

import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.FloatSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import ch.opumlabs.opumbrowser.R

import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.app.Activity
import android.app.Application
import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.LruCache
import android.view.inputmethod.InputMethodManager
import android.webkit.CookieManager
import android.webkit.WebView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.mycompany.app.data.DataAlbum
import com.mycompany.app.data.DataCast
import com.mycompany.app.data.DataCmp
import com.mycompany.app.data.DataNews
import com.mycompany.app.data.DataPdf
import com.mycompany.app.data.DataSearch
import com.mycompany.app.data.DataTrans
import com.mycompany.app.data.DataUrl
import com.mycompany.app.data.book.DataBookAds
import com.mycompany.app.data.book.DataBookAlbum
import com.mycompany.app.data.book.DataBookBlock
import com.mycompany.app.data.book.DataBookCmp
import com.mycompany.app.data.book.DataBookDown
import com.mycompany.app.data.book.DataBookFilter
import com.mycompany.app.data.book.DataBookHistory
import com.mycompany.app.data.book.DataBookJava
import com.mycompany.app.data.book.DataBookLink
import com.mycompany.app.data.book.DataBookOver
import com.mycompany.app.data.book.DataBookPdf
import com.mycompany.app.data.book.DataBookPop
import com.mycompany.app.data.book.DataBookRecent
import com.mycompany.app.data.book.DataBookScript
import com.mycompany.app.data.book.DataBookSearch
import com.mycompany.app.data.book.DataBookTmem
import com.mycompany.app.data.book.DataBookTrans
import com.mycompany.app.data.book.DataBookUser
import com.mycompany.app.main.MainDownSvc.DownBusyListener
import com.mycompany.app.main.MainDownSvc.DownListListener
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefEditor.Companion.get
import com.mycompany.app.pref.PrefFloat
import com.mycompany.app.pref.PrefImage
import com.mycompany.app.pref.PrefList
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.pref.PrefPath
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSub
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.pref.PrefVideo
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.web.WebClean

import java.io.File
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Suppress("deprecation")
class MainApp : Application() {
    /**
     * Debug builds only (2026-11-21, stability pass): StrictMode logs main-thread disk reads / writes and network use and
     * leaked cursors / closeables to logcat (tag `StrictMode`), so hitches can be found; it never crashes or shows
     * anything. Release and qa builds are not debuggable and skip this.
     */
    private fun strictModeInDebug() {
        if ((applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) == 0) return
        android.os.StrictMode.setThreadPolicy(
            android.os.StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork().penaltyLog().build(),
        )
        android.os.StrictMode.setVmPolicy(
            android.os.StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .detectLeakedRegistrationObjects().penaltyLog().build(),
        )
    }

    @JvmField
    var pendingDownList: ArrayList<com.mycompany.app.main.MainDownSvc.ImageItem?>? = null
    @JvmField
    var bookUser: DataBookUser? = null
    @JvmField
    var downBusyListener: DownBusyListener? = null
    @JvmField
    var downListListener: DownListListener? = null
    @JvmField
    var mainHandler: Handler? = null
    @JvmField
    var hostCache: LruCache<String, String>? = null
    @JvmField
    var webDestroy: MainWebDestroy? = null
    @JvmField
    var executorService: ExecutorService? = null
    @JvmField
    var prefAlbum: PrefAlbum? = null
    @JvmField
    var prefEditor: PrefEditor? = null
    @JvmField
    var prefFloat: PrefFloat? = null
    @JvmField
    var prefImage: PrefImage? = null
    @JvmField
    var prefList: PrefList? = null
    @JvmField
    var prefMain: PrefMain? = null
    @JvmField
    var nextDownUrl: String? = null
    @JvmField
    var prefPath: PrefPath? = null
    @JvmField
    var nextDownName: String? = null
    @JvmField
    var prefPdf: PrefPdf? = null
    @JvmField
    var nextDownItem: UriItem? = null
    @JvmField
    var prefRead: PrefRead? = null
    @JvmField
    var nextDownMulti: Int = 0
    @JvmField
    var prefSecret: PrefSecret? = null
    @JvmField
    var nextDownList: ArrayList<com.mycompany.app.main.MainDownSvc.ImageItem?>? = null
    @JvmField
    var prefSub: PrefSub? = null
    @JvmField
    var prefSync: PrefSync? = null
    @JvmField
    var prefTts: PrefTts? = null
    @JvmField
    var prefVideo: PrefVideo? = null
    @JvmField
    var prefWeb: PrefWeb? = null
    @JvmField
    var prefZone: PrefZone? = null
    @JvmField
    var prefZtri: PrefZtri? = null
    @JvmField
    var prefZtwo: PrefZtwo? = null
    @JvmField
    var webTimersPaused: Boolean = false
    @JvmField
    var dataAlbum: DataAlbum? = null
    @JvmField
    var payState: Byte = 0
    @JvmField
    var dataCast: DataCast? = null
    @JvmField
    var dataCmp: DataCmp? = null
    @JvmField
    var dataNews: DataNews? = null
    @JvmField
    var pendingVideoUrl: String? = null
    @JvmField
    var dataPdf: DataPdf? = null
    @JvmField
    var instaAppId: String? = null
    @JvmField
    var dataSearch: DataSearch? = null
    @JvmField
    var notificationCache: NotificationManager? = null
    @JvmField
    var dataTrans: DataTrans? = null
    @JvmField
    var clipboardCache: ClipboardManager? = null
    @JvmField
    var dataUrl: DataUrl? = null
    @JvmField
    var inputMethodCache: InputMethodManager? = null
    @JvmField
    var bookAds: DataBookAds? = null
    @JvmField
    var lifecycle: MyLifeCycle? = null
    @JvmField
    var bookAlbum: DataBookAlbum? = null
    @JvmField
    var startedActivities: Int = 0
    @JvmField
    var bookBlock: DataBookBlock? = null
    @JvmField
    var skipLockOnce: Boolean = false
    @JvmField
    var bookCmp: DataBookCmp? = null
    @JvmField
    var webCleanCache: WebClean? = null
    @JvmField
    var bookDown: DataBookDown? = null
    @JvmField
    var cookieCache: CookieManager? = null
    @JvmField
    var bookFilter: DataBookFilter? = null
    @JvmField
    var cookieAcceptState: Byte = 0
    @JvmField
    var bookHistory: DataBookHistory? = null
    @JvmField
    var cookieFlushPending: Boolean = false
    @JvmField
    var bookJava: DataBookJava? = null
    @JvmField
    var downBinding: Boolean = false
    @JvmField
    var bookLink: DataBookLink? = null
    @JvmField
    var downService: MainDownSvc? = null
    @JvmField
    var bookOver: DataBookOver? = null
    @JvmField
    var downStarting: Boolean = false
    @JvmField
    var bookPdf: DataBookPdf? = null
    @JvmField
    var pendingActionId: Long = 0
    @JvmField
    var pendingAction: Byte = 0
    @JvmField
    var bookPop: DataBookPop? = null
    @JvmField
    var pendingDownUrl: String? = null
    @JvmField
    var bookRecent: DataBookRecent? = null
    @JvmField
    var pendingDownName: String? = null
    @JvmField
    var bookSearch: DataBookSearch? = null
    @JvmField
    var pendingDownItem: UriItem? = null
    @JvmField
    var bookScript: DataBookScript? = null
    @JvmField
    var pendingDownMulti: Int = 0
    @JvmField
    var bookTmem: DataBookTmem? = null
    @JvmField
    var pendingRefresh: Boolean = false
    @JvmField
    var bookTrans: DataBookTrans? = null

    internal inner class InitSystemServicesTask : Runnable {

        internal inner class InitServicesStep : Runnable {
            override fun run() {
                val mainApp = this@MainApp
                val applicationContext = mainApp.getApplicationContext()
                val dp600Px: Int = dp600
                mainApp.initSystemServices(applicationContext, false)
            }
        }

        override fun run() {
            val mainApp = this@MainApp
            val applicationContext = mainApp.getApplicationContext()
            val dp600Px: Int = dp600
            val localeListCompatH = AppCompatDelegate.getApplicationLocales()
            val localeB =
                if (localeListCompatH.isEmpty()) null else localeListCompatH.get(
                    0
                )
            AppState.appLocale = localeB
            AppState.localeCache = localeB
            currentLocale()
            com.mycompany.app.glide.ImageCache.init(applicationContext)
            val cacheDir = applicationContext.getCacheDir()
            if (cacheDir == null || !cacheDir.exists() || cacheDir.length() == 0L) {
                com.mycompany.app.glide.ImageCache.clearMemory()
            }
            loadMediaSettings(applicationContext)
            if (MainUtil.isDarkMode()) {
                MainUtil.backupDirPath(applicationContext, SyncSettings.secretMode.value, false)
            }
            if (mainApp.initSystemServices(applicationContext, true)) {
                return
            }
            try {
                Handler(Looper.getMainLooper()).post(InitServicesStep())
            } catch (unused3: Exception) {
            }
        }
    }

    internal inner class ScheduleTask : Runnable {
        override fun run() {
            val mainApp = this@MainApp
            try {
                // Android 14+: a user-initiated transfer job keeps the engine alive (no daily limit, waits for the
                // network); else, or when Android refuses the job, the foreground service as before (2026-11-20)
                if (com.mycompany.app.down.DownTransferJob.schedule(mainApp.getApplicationContext())) {
                    return
                }
                val intent =
                    Intent(mainApp.getApplicationContext(), MainDownSvc::class.java as Class<*>)
                mainApp.startForegroundService(intent)
            } catch (unused: Exception) {
                mainApp.downBinding = false
                mainApp.downService = null
                mainApp.downStarting = false
                val downListListener = mainApp.downListListener
                if (downListListener != null) {
                    downListListener.onDisconnected()
                }
                Handler(Looper.getMainLooper()).post(mainApp.DownFailToastTask())
            }
        }
    }

    internal inner class DownFailToastTask : Runnable {
        override fun run() {
            MainUtil.showToast(this@MainApp.getApplicationContext(), AppR.string.down_fail)
        }
    }

    internal inner class LoadSavedTask : Runnable {
        override fun run() {
            val mainApp = this@MainApp
            val pendingRefresh = mainApp.pendingRefresh
            mainApp.pendingRefresh = false
            try {
                val mainDownSvc: MainDownSvc? = if (!mainApp.downBinding) null else mainApp.downService
                if (!mainApp.downBinding || mainDownSvc == null) {
                    return
                }
                if (pendingRefresh) {
                    mainDownSvc.loadSaved()
                }
                val pendingActionId = mainApp.pendingActionId
                if (pendingActionId != 0L) {
                    val pendingAction = mainApp.pendingAction
                    val downService: MainDownSvc? = if (mainApp.downBinding) mainApp.downService else null
                    if (mainApp.downBinding && downService != null && pendingActionId != 0L && pendingAction.toInt() != 0) {
                        downService.handleAction(pendingAction.toInt(), pendingActionId)
                    }
                    mainApp.pendingActionId = 0L
                    mainApp.pendingAction = 0.toByte()
                }
                val uriItem: UriItem? = if (!TextUtils.isEmpty(mainApp.pendingDownUrl)) mainApp.pendingDownItem else null
                if (!TextUtils.isEmpty(mainApp.pendingDownUrl) && uriItem != null) {
                    mainApp.sendDownload(mainApp.pendingDownUrl, mainApp.pendingDownName, uriItem, mainApp.pendingDownMulti)
                    mainApp.pendingDownUrl = null
                    mainApp.pendingDownName = null
                    mainApp.pendingDownItem = null
                    mainApp.pendingDownMulti = 0
                }
                val arrayList = mainApp.pendingDownList
                if (arrayList != null) {
                    mainApp.sendDownloads(arrayList)
                    mainApp.pendingDownList = null
                }
                mainApp.downStarting = false
                val downListListener = mainApp.downListListener
                if (downListListener != null) {
                    downListListener.onConnected()
                }
                val downService = mainApp.downService
                val handler: Handler? = if (downService!!.downItems != null || downService.merging) null else downService.uiHandler
                if (downService.downItems != null || downService.merging || handler == null) {
                    return
                }
                handler.post(downService.StopIfIdleTask())
            } catch (unused: Exception) {
            }
        }
    }

    internal inner class FlushTask : Runnable {
        override fun run() {
            val mainApp = this@MainApp
            val dp600Px: Int = dp600
            try {
                val cookieManagerL: CookieManager? = cookieManager(mainApp.getApplicationContext())
                if (cookieManagerL != null) {
                    cookieManagerL.flush()
                }
            } catch (unused: Exception) {
            } catch (unused: NoSuchMethodError) {
            }
            mainApp.cookieFlushPending = false
        }
    }

    internal inner class CancelNotificationsTask : Runnable {
        override fun run() {
            MainUtil.cancelNotifications(this@MainApp.getApplicationContext(), SyncSettings.secretMode.value, false)
        }
    }

    internal inner class SendDownloadTask : Runnable {
        override fun run() {
            val mainApp = this@MainApp
            val nextDownUrl = mainApp.nextDownUrl
            val nextDownName = mainApp.nextDownName
            val uriItem = mainApp.nextDownItem
            val nextDownMulti = mainApp.nextDownMulti
            mainApp.nextDownUrl = null
            mainApp.nextDownName = null
            mainApp.nextDownItem = null
            mainApp.sendDownload(nextDownUrl, nextDownName, uriItem, nextDownMulti)
        }
    }

    internal inner class SendDownloadsTask : Runnable {
        override fun run() {
            val mainApp = this@MainApp
            val arrayList = mainApp.nextDownList
            mainApp.nextDownList = null
            mainApp.sendDownloads(arrayList)
        }
    }

    inner class MyLifeCycle : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        }

        override fun onActivityDestroyed(activity: Activity) {
            val mainApp = this@MainApp
            if (mainApp.startedActivities == 0) {
                mainApp.execute(mainApp.CancelNotificationsTask())
            }
        }

        override fun onActivityPaused(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        }

        override fun onActivityStarted(activity: Activity) {
            val mainApp = this@MainApp
            val startedActivities = mainApp.startedActivities + 1
            mainApp.startedActivities = startedActivities
            if (startedActivities == 1 && !mainApp.skipLockOnce && !SecretSettings.lockSkip.value && MainUtil.isLockEnabled()) {
                val intentH = MainUtil.lockIntent(mainApp.getApplicationContext(), SecretSettings.lockType.value)
                intentH.putExtra("EXTRA_TYPE", 0)
                intentH.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                mainApp.startActivity(intentH)
            }
            mainApp.skipLockOnce = false
            if (SecretSettings.lockSkip.value) {
                SecretSettings.editLocal { set(SecretSettings.lockSkip, false) }
                PrefSet.putBool(9, mainApp.getApplicationContext(), "mLockSkip", SecretSettings.lockSkip.value)
            }
        }

        override fun onActivityStopped(activity: Activity) {
            this@MainApp.startedActivities--
        }
    }

    fun execute(runnable: Runnable?) {
        val executorServiceG = ensureExecutor()
        if (executorServiceG == null) {
            return
        }
        try {
            executorServiceG.execute(runnable)
        } catch (unused: Exception) {
        }
    }

    fun startDownloads(arrayList: ArrayList<com.mycompany.app.main.MainDownSvc.ImageItem?>?) {
        if (this.downService != null) {
            this.nextDownList = arrayList
            execute(SendDownloadsTask())
        } else {
            if (this.downStarting) {
                return
            }
            this.downStarting = true
            this.pendingDownList = arrayList
            bindDownService()
        }
    }

    fun startDownload(nextDownUrl: String?, nextDownName: String?, uriItem: UriItem?, isAudio: Boolean) {
        // isAudio is the "split this download" flag: the connections come from the Speed setting (Auto by default)
        val nextDownMulti = if (isAudio) com.mycompany.app.down.DownSpeed.connections(this) else 0
        if (this.downService != null) {
            this.nextDownUrl = nextDownUrl
            this.nextDownName = nextDownName
            this.nextDownItem = uriItem
            this.nextDownMulti = nextDownMulti
            execute(SendDownloadTask())
            return
        }
        if (this.downStarting) {
            return
        }
        this.downStarting = true
        this.pendingDownUrl = nextDownUrl
        this.pendingDownName = nextDownName
        this.pendingDownItem = uriItem
        this.pendingDownMulti = nextDownMulti
        bindDownService()
    }

    fun bindDownService() {
        if (this.downBinding) {
            return
        }
        this.downBinding = true
        execute(ScheduleTask())
    }

    fun sendDownloads(arrayList: ArrayList<com.mycompany.app.main.MainDownSvc.ImageItem?>?) {
        if (!this.downBinding || this.downService == null || arrayList == null || arrayList.isEmpty()) {
            return
        }
        val mainDownSvc = this.downService
        mainDownSvc!!.javaClass
        val secretMode = SyncSettings.secretMode.value
        if (arrayList.isEmpty()) {
            return
        }
        synchronized(mainDownSvc.itemLock) {
            mainDownSvc.mergeTotal += arrayList.size
            if (mainDownSvc.merging) {
                var mergeQueue = mainDownSvc.mergeQueue
                if (mergeQueue == null) {
                    mergeQueue = ArrayList()
                    mainDownSvc.mergeQueue = mergeQueue
                }
                mergeQueue.addAll(arrayList)
            } else {
                mainDownSvc.startMerge(arrayList, secretMode)
            }
        }
    }

    fun sendDownAction(pendingActionId: Long) {
        val mainDownSvc = this.downService
        if (mainDownSvc != null) {
            if (this.downBinding) {
                mainDownSvc.cancelItem(pendingActionId)
            }
        } else {
            if (this.downStarting) {
                return
            }
            this.downStarting = true
            this.pendingActionId = pendingActionId
            this.pendingAction = 2.toByte()
            bindDownService()
        }
    }

    fun sendDownload(pendingDownUrl: String?, pendingDownName: String?, uriItem: UriItem?, pendingDownMulti: Int) {
        if (!this.downBinding || this.downService == null || TextUtils.isEmpty(pendingDownUrl) || uriItem == null || TextUtils.isEmpty(
                uriItem.uriString
            )
        ) {
            return
        }
        this.downService!!.addDownload(pendingDownUrl, pendingDownName, uriItem, pendingDownMulti, SyncSettings.secretMode.value, false)
    }

    fun ensureExecutor(): ExecutorService? {
        var executorServiceNewCachedThreadPool = this.executorService
        if (executorServiceNewCachedThreadPool != null && !executorServiceNewCachedThreadPool.isShutdown() && !executorServiceNewCachedThreadPool.isTerminated()) {
            return executorServiceNewCachedThreadPool
        }
        if (executorServiceNewCachedThreadPool != null) {
            try {
                executorServiceNewCachedThreadPool.shutdownNow()
            } catch (unused: Exception) {
            }
        }
        try {
            executorServiceNewCachedThreadPool = Executors.newCachedThreadPool()
            if (executorServiceNewCachedThreadPool == null) {
                return null
            }
            this.executorService = executorServiceNewCachedThreadPool
        } catch (unused2: Exception) {
        }
        return executorServiceNewCachedThreadPool
    }

    fun hostCacheValue(input: String?): String? {
        try {
            val lruCache = this.hostCache
            if (lruCache == null) {
                return null
            }
            val cached = if (input == null) null else lruCache.get(input)
            if (!TextUtils.isEmpty(cached)) {
                return cached
            }
            return MainUtil.hostOfUrl(input, false)?.let { lruCache.get(it) }
        } catch (unused: Exception) {
            return null
        }
    }

    override fun onCreate() {
        super.onCreate()
        strictModeInDebug()
        val applicationContext = getApplicationContext()
        CrashGuard.install(applicationContext)
        com.mycompany.app.ui.components.OpumMessage.attach(this)
        PrefSync.load(applicationContext, false)
        com.mycompany.app.pref.PrefSpace.load(applicationContext, false)
        // incognito keeps nothing any more: what it kept moves into a workspace once (before the incognito folder is chosen)
        com.mycompany.app.web.WorkspaceData.atStart(applicationContext, getString(R.string.space_private))
        // read off the main thread now: the first menu (Downloads badge) otherwise read this file while composing (StrictMode)
        runInBackground(applicationContext, Runnable { com.mycompany.app.down.DownState.unseenCount(applicationContext) })
        this.payState = 0.toByte()
        this.pendingVideoUrl = null
        this.instaAppId = null
        this.notificationCache = null
        this.clipboardCache = null
        this.inputMethodCache = null
        this.cookieCache = null
        this.mainHandler = null
        AppState.appLocale = null
        AppState.localeCache = null
        AppState.webViewHint = 0.toByte()
        AppState.startupPending = true
        if (SyncSettings.secretMode.value && MainConst.isApi28) {
            WebView.setDataDirectorySuffix("secret")
            com.mycompany.app.web.WebProfiles.incognitoProcess = true
        }
        this.startedActivities = 0
        val myLifeCycle = MyLifeCycle()
        this.lifecycle = myLifeCycle
        registerActivityLifecycleCallbacks(myLifeCycle)
        val resources = getResources()
        val displayMetrics = resources.getDisplayMetrics().density
        dp600 = Math.round(600.0f * displayMetrics)
        dp144 = Math.round(144.0f * displayMetrics)
        dp140 = Math.round(140.0f * displayMetrics)
        dp90 = Math.round(90.0f * displayMetrics)
        dp80 = Math.round(80.0f * displayMetrics)
        dp64 = Math.round(64.0f * displayMetrics)
        dp56 = Math.round(56.0f * displayMetrics)
        dp48 = Math.round(48.0f * displayMetrics)
        dp40 = Math.round(40.0f * displayMetrics)
        dp24 = Math.round(24.0f * displayMetrics)
        dp20 = Math.round(20.0f * displayMetrics)
        dp16 = Math.round(16.0f * displayMetrics)
        dp15 = Math.round(15.0f * displayMetrics)
        dp10 = Math.round(10.0f * displayMetrics)
        dp8 = Math.round(8.0f * displayMetrics)
        dp4 = Math.round(4.0f * displayMetrics)
        dp3 = Math.round(3.0f * displayMetrics)
        dp1 = Math.round(1.0f * displayMetrics)
        loadEarlySettings(applicationContext)
        com.mycompany.app.web.WorkspaceData.enforceIncognitoRules()
        com.mycompany.app.ui.settings.FloatSettingsActivity.moveHandButton(applicationContext)
        updateDarkMode(resources)
        execute(InitSystemServicesTask())
    }

    override fun onTerminate() {
        super.onTerminate()
        val myLifeCycle = this.lifecycle
        if (myLifeCycle != null) {
            unregisterActivityLifecycleCallbacks(myLifeCycle)
            this.lifecycle = null
        }
        this.webCleanCache = null
        this.cookieCache = null
        execute(CancelNotificationsTask())
    }

    fun initSystemServices(context: Context?, retry: Boolean): Boolean {
        var first = true
        if (context == null) {
            return true
        }
        if (this.notificationCache == null) {
            try {
                this.notificationCache = context.getSystemService("notification") as NotificationManager?
            } catch (exc: Exception) {
                if (retry && isLooperError(exc)) {
                    first = false
                }
            }
        }
        if (this.clipboardCache == null) {
            try {
                this.clipboardCache = context.getSystemService("clipboard") as ClipboardManager?
            } catch (exc2: Exception) {
                if (retry && first && isLooperError(exc2)) {
                    first = false
                }
            }
        }
        if (this.inputMethodCache == null) {
            try {
                this.inputMethodCache = context.getSystemService("input_method") as InputMethodManager?
            } catch (exc3: Exception) {
                if (retry && first && isLooperError(exc3)) {
                    return false
                }
            }
        }
        return first
    }

    companion object {
        // Sizes in pixels for the given dp, set once in onCreate.
        @JvmField
        var dp600: Int = 0
        @JvmField
        var dp144: Int = 0
        @JvmField
        var dp140: Int = 0
        @JvmField
        var dp90: Int = 0
        @JvmField
        var dp80: Int = 0
        @JvmField
        var dp64: Int = 0
        @JvmField
        var dp56: Int = 0
        @JvmField
        var dp48: Int = 0
        @JvmField
        var dp40: Int = 0
        @JvmField
        var dp24: Int = 0
        @JvmField
        var dp20: Int = 0
        @JvmField
        var dp16: Int = 0
        @JvmField
        var dp15: Int = 0
        @JvmField
        var dp10: Int = 0
        @JvmField
        var dp8: Int = 0
        @JvmField
        var dp4: Int = 0
        @JvmField
        var dp3: Int = 0
        @JvmField
        var dp1: Int = 0
        /** Set while MainUtil.restartActivity restarts the app. */
        @JvmField
        var restarting: Boolean = false
        /** Last result of MainUtil.isTapInside for the current gesture. */
        @JvmField
        var edgeTouchActive: Boolean = false
        /** The toast shown last, cancelled before the next one. */
        @JvmField
        var lastToast: Toast? = null
        /** Cached path of the user font. */
        @JvmField
        var userFontPathCache: String? = null
        /** Cached path of the reader font. */
        @JvmField
        var readerFontPathCache: String? = null
        /** Cached background image paths (`.back` folder), portrait / landscape: from backupDirPath
         *  and backupDirPath2. */
        @JvmField
        var backPortPath: String? = null
        @JvmField
        var backLandPath: String? = null
        @JvmField
        var backPortPath2: String? = null
        @JvmField
        var backLandPath2: String? = null
        /** WebView default user agent, cached. */
        @JvmField
        var defaultUserAgent: String? = null
        /** Chrome version taken from [defaultUserAgent], cached. */
        @JvmField
        var chromeVersionCache: String? = null
        /** Reused buffer for View.getLocationOnScreen. */
        @JvmField
        var locationBuffer: IntArray? = null
        @JvmStatic
        fun isLooperError(exc: Exception?): Boolean {
            try {
                val message = exc!!.message
                if (TextUtils.isEmpty(message)) {
                    return false
                }
                return message!!.contains("Can't create handler inside thread")
            } catch (unused: Exception) {
                return false
            }
        }

        @JvmStatic
        fun pauseWebTimers(context: Context?, webView: WebView?) {
            val mainAppO: MainApp? = if (webView == null) null else from(context)
            if (webView == null || mainAppO == null || mainAppO.webTimersPaused) {
                return
            }
            try {
                mainAppO.webTimersPaused = true
                webView.pauseTimers()
            } catch (unused: Exception) {
            }
        }

        @JvmStatic
        fun resumeWebTimers(context: Context?, webView: WebView?) {
            val mainAppO: MainApp? = if (webView == null) null else from(context)
            if (webView == null || mainAppO == null || !mainAppO.webTimersPaused) {
                return
            }
            try {
                webView.resumeTimers()
                mainAppO.webTimersPaused = false
            } catch (unused: Exception) {
            }
        }

        @JvmStatic
        fun runInBackground(context: Context?, runnable: Runnable?) {
            val mainAppO: MainApp? = from(context)
            if (mainAppO == null) {
                return
            }
            mainAppO.execute(runnable)
        }

        @JvmStatic
        fun setAcceptCookies(context: Context?, accept: Boolean) {
            val mainAppO: MainApp? = from(context)
            if (mainAppO != null) {
                val acceptState = if (accept) 1.toByte() else 2.toByte()
                if (mainAppO.cookieAcceptState != acceptState) {
                    try {
                        val cookieManagerL: CookieManager? = cookieManager(context)
                        if (cookieManagerL != null) {
                            cookieManagerL.setAcceptCookie(accept)
                            mainAppO.cookieAcceptState = acceptState
                        }
                    } catch (unused: Exception) {
                    } catch (unused: NoSuchMethodError) {
                    }
                }
            }
        }

        @JvmStatic
        fun applyCookieSetting(context: Context?) {
            setAcceptCookies(context, cookiesAllowed(false))
        }

        @JvmStatic
        fun postToMain(context: Context?, runnable: Runnable?) {
            val mainAppO: MainApp? = from(context)
            if (mainAppO == null) {
                return
            }
            var handler = mainAppO.mainHandler
            if (handler == null) {
                handler = Handler(Looper.getMainLooper())
                mainAppO.mainHandler = handler
            }
            handler.post(runnable!!)
        }

        @JvmStatic
        fun setThirdPartyCookies(context: Context?, webView: WebView?, third: Boolean) {
            if (webView != null) {
                try {
                    val cookieManagerL: CookieManager? = cookieManager(context)
                    if (cookieManagerL != null) {
                        cookieManagerL.setAcceptThirdPartyCookies(webView, third)
                    }
                } catch (unused: Exception) {
                } catch (unused: NoSuchMethodError) {
                }
            }
        }

        @JvmStatic
        fun executor(context: Context?): ExecutorService? {
            val mainAppO: MainApp? = from(context)
            if (mainAppO == null) {
                return null
            }
            return mainAppO.ensureExecutor()
        }

        @JvmStatic
        fun currentLocale(): Locale {
            val locale: Locale? = AppState.localeCache
            if (locale != null) {
                return locale
            }
            val appLocale: Locale? = AppState.appLocale
            AppState.localeCache = appLocale
            if (appLocale != null) {
                return appLocale
            }
            val default = Locale.getDefault()
            AppState.localeCache = default
            return default
        }

        @JvmStatic
        fun clipboard(context: Context?): ClipboardManager? {
            val mainAppO: MainApp? = from(context)
            if (mainAppO == null) {
                return null
            }
            var clipboardManager = mainAppO.clipboardCache
            if (clipboardManager != null) {
                return clipboardManager
            }
            try {
                clipboardManager = context!!.getSystemService("clipboard") as ClipboardManager?
            } catch (unused: Exception) {
            }
            if (clipboardManager == null) {
                return null
            }
            mainAppO.clipboardCache = clipboardManager
            return clipboardManager
        }

        @JvmStatic
        fun cookiesAllowed(third: Boolean): Boolean {
            val cookieType = if (third) BrowserSettings.thirdType.value else BrowserSettings.cookieType.value
            if (cookieType != 1) {
                return if (cookieType == 2 && SyncSettings.secretMode.value) false else true
            }
            return false
        }

        /** The cookie manager of the space in use (its WebView profile, [com.mycompany.app.web.WebProfiles]). */
        @JvmStatic
        fun cookieManager(context: Context?): CookieManager? {
            val mainAppO: MainApp? = from(context)
            val manager = try {
                com.mycompany.app.web.WebProfiles.cookieManager()
            } catch (unused: Throwable) {
                null
            }
            if (mainAppO != null && manager !== mainAppO.cookieCache) {
                // another profile: its accept setting is not known yet
                mainAppO.cookieCache = manager
                mainAppO.cookieAcceptState = 0
            }
            return manager
        }

        @JvmStatic
        fun inputMethods(context: Context?): InputMethodManager? {
            val mainAppO: MainApp? = from(context)
            if (mainAppO == null) {
                return null
            }
            var inputMethodManager = mainAppO.inputMethodCache
            if (inputMethodManager != null) {
                return inputMethodManager
            }
            try {
                inputMethodManager =
                    context!!.getSystemService("input_method") as InputMethodManager?
            } catch (unused: Exception) {
            }
            if (inputMethodManager == null) {
                return null
            }
            mainAppO.inputMethodCache = inputMethodManager
            return inputMethodManager
        }

        @JvmStatic
        fun from(context: Context?): MainApp? {
            if (context == null) {
                return null
            }
            try {
                val applicationContext = context.getApplicationContext()
                if (applicationContext != null && (applicationContext is MainApp)) {
                    return applicationContext
                }
                return null
            } catch (unused: Exception) {
                return null
            }
        }

        @JvmStatic
        fun notifications(context: Context?): NotificationManager? {
            val mainAppO: MainApp? = from(context)
            if (mainAppO == null) {
                return null
            }
            var notificationManager = mainAppO.notificationCache
            if (notificationManager != null) {
                return notificationManager
            }
            try {
                notificationManager =
                    context!!.getSystemService("notification") as NotificationManager?
            } catch (unused: Exception) {
            }
            if (notificationManager == null) {
                return null
            }
            mainAppO.notificationCache = notificationManager
            return notificationManager
        }

        @JvmStatic
        fun loadEarlySettings(context: Context?) {
            if (context != null) {
                com.mycompany.app.pref.store.EditorSettings.load(context)
                PrefEditor.iconArgb = PrefEditor.withAlpha(EditorSettings.iconColor.value, EditorSettings.iconAlpha.value)
                PrefEditor.color = PrefEditor.withAlpha(EditorSettings.ttsColor.value, EditorSettings.ttsAlpha.value)
                PrefEditor.zoomArgb = PrefEditor.withAlpha(EditorSettings.zoomColor.value, EditorSettings.zoomAlpha.value)
                PrefEditor.ypipArgb = PrefEditor.withAlpha(EditorSettings.ypipColor.value, EditorSettings.ypipAlpha.value)
                PrefEditor.scrFilArgb = PrefEditor.withAlpha(EditorSettings.scrFilColor.value, EditorSettings.scrFilAlpha.value)
                PrefEditor.gridArgb = PrefEditor.withAlpha(EditorSettings.gridColor.value, EditorSettings.gridAlpha.value)
                PrefEditor.newsArgb = PrefEditor.withAlpha(EditorSettings.newsColor.value, EditorSettings.newsAlpha.value)
                PrefEditor.handArgb = PrefEditor.withAlpha(EditorSettings.handColor.value, EditorSettings.handAlpha.value)
                PrefEditor.passArgb = PrefEditor.withAlpha(EditorSettings.passColor.value, EditorSettings.passAlpha.value)
                PrefEditor.trnsArgb = PrefEditor.withAlpha(EditorSettings.trnsColor.value, EditorSettings.trnsAlpha.value)
            }
            if (context != null) {
                com.mycompany.app.pref.store.FloatSettings.load(context)
                PrefFloat.flt1Argb = PrefEditor.withAlpha(FloatSettings.flt1Color.value, FloatSettings.flt1Alpha.value)
                PrefFloat.flt2Argb = PrefEditor.withAlpha(FloatSettings.flt2Color.value, FloatSettings.flt2Alpha.value)
                PrefFloat.flt3Argb = PrefEditor.withAlpha(FloatSettings.flt3Color.value, FloatSettings.flt3Alpha.value)
            }
            PrefMain.load(context, false)
            PrefPdf.load(context, false)
            PrefSecret.load(context, false)
            PrefTts.load(context, false)
            PrefWeb.load(context, false)
        }

        @JvmStatic
        fun loadMediaSettings(context: Context?) {
            PrefAlbum.load(context, false)
            PrefImage.load(context, false)
            PrefList.load(context, false)
            PrefPath.load(context, false)
            PrefRead.load(context, false)
            if (context != null) com.mycompany.app.pref.store.SubtitleSettings.load(context)
            if (context != null) com.mycompany.app.pref.store.VideoSettings.load(context)
        }

        @JvmStatic
        fun loadLateSettings(context: Context?, resources: Resources?) {
            PrefZone.load(context, resources, false)
            PrefZtwo.load(context, false)
            if (context != null) com.mycompany.app.pref.store.ZtriSettings.load(context)
        }

        @JvmStatic
        fun loadSettingsOnce(context: Context?, resources: Resources?) {
            if (AppState.settingsLoaded) {
                return
            }
            AppState.settingsLoaded = true
            loadLateSettings(context, resources)
        }

        @JvmStatic
        fun webClean(context: Context?, create: Boolean): WebClean? {
            val mainAppO: MainApp? = from(context)
            if (mainAppO == null) {
                return null
            }
            val webClean = mainAppO.webCleanCache
            if (webClean != null) {
                return webClean
            }
            if (!create) {
                return null
            }
            val webCleanCache = WebClean()
            mainAppO.webCleanCache = webCleanCache
            return webCleanCache
        }

        @JvmStatic
        fun webDestroy(context: Context?): MainWebDestroy? {
            val mainAppO: MainApp? = from(context)
            if (mainAppO == null) {
                return null
            }
            return mainAppO.webDestroy
        }

        @JvmStatic
        fun updateDarkMode(resources: Resources?) {
            val darkWeb: Boolean
            var darkUi = false
            if (!BrowserSettings.themeSys.value) {
                val darkNow = BrowserSettings.themeUi.value == 1
                darkWeb = BrowserSettings.themeWeb.value == 1
                darkUi = darkNow
            } else if (resources != null) {
                darkWeb = (resources.getConfiguration().uiMode and 48) == 32
                darkUi = darkWeb
            } else {
                darkWeb = false
            }
            AppState.darkUi = darkUi
            AppState.darkWeb = darkWeb
            MainUtil.applyNightMode()
        }

    }
}
