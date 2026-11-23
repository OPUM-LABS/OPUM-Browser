package com.mycompany.app.main

import androidx.annotation.RequiresApi
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.MainSettings
import android.window.OnBackInvokedDispatcher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.result.ActivityResultLauncher
import com.mycompany.app.view.MyEditAuto.KeyBackListener
import android.window.OnBackInvokedCallback
import androidx.activity.result.ActivityResultCallback
import com.mycompany.app.view.MyEditAuto
import com.mycompany.app.view.MyPopupWrap
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Insets
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowInsetsControllerCompat
import com.mycompany.app.compress.CompressUtil.extractedPaths
import com.mycompany.app.data.DataUtil.addUri
import com.mycompany.app.db.DbUtil.deleteById
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.db.book.DbBookAlbum.Companion.cached; import com.mycompany.app.db.book.DbBookAlbum.Companion.get
import com.mycompany.app.db.book.DbBookCmp.Companion.cached; import com.mycompany.app.db.book.DbBookCmp.Companion.get
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.db.book.DbBookLocale.Companion.cached; import com.mycompany.app.db.book.DbBookLocale.Companion.get
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.db.book.DbBookPdf.Companion.cached; import com.mycompany.app.db.book.DbBookPdf.Companion.get
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.db.book.DbRecentLang.Companion.get
import com.mycompany.app.down.DownParseDzen.parseItemText
import com.mycompany.app.down.DownParseDzen.segmentUrls
import com.mycompany.app.down.DownParseKakao.parseItemText
import com.mycompany.app.down.DownParseKakao.segmentUrls
import com.mycompany.app.down.DownParseReddit.parseRedditItem
import com.mycompany.app.down.DownParseReddit.redditSegments
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.pref.PrefUtil.showDetail
import com.mycompany.app.pref.PrefUtil.showRecent
import com.mycompany.app.pref.PrefUtil.singleLine
import com.mycompany.app.pref.PrefUtil.setSingleLine
import com.mycompany.app.pref.PrefWeb
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService
import kotlin.math.abs
import kotlin.math.max

@Suppress("deprecation")
open class MainActivity : AppCompatActivity(), ActivityHost {
    /** One result launcher per request code ([RequestCode]), registered in onCreate through launchForResult(null, code). */
    private val launchers = HashMap<Int, ActivityResultLauncher<Intent>>()
    @JvmField
    var imeInBottomInset: Boolean = false
    @JvmField
    var insetView: View? = null
    @JvmField
    var insetListener: MainInsetListener? = null
    @JvmField
    var viewerListener: MainViewerListener? = null
    @JvmField
    var insetsListenerSet: Boolean = false
    @JvmField
    var imeHeight: Int = 0
    @JvmField
    var insetTop: Int = 0
    @JvmField
    var insetBottom: Int = 0
    @JvmField
    var insetLeft: Int = 0
    @JvmField
    var insetRight: Int = 0
    @JvmField
    var mainHandler: Handler? = null
    @JvmField
    var screenOnView: View? = null
    @JvmField
    var savedScreenOnView: View? = null
    @JvmField
    var heldScreenOnView: View? = null
    @JvmField
    var screenOffHandler: EventHandler? = null
    @JvmField
    var screenOnFresh: Boolean = false
    @JvmField
    var screenOffTimeout: Int = 0
    @JvmField
    var ownBackHandling: Boolean = false
    @JvmField
    var backCallback: OnBackInvokedCallback? = null
    @JvmField
    var backKeyListener: KeyBackListener? = null
    @JvmField
    var popupMenu: MyPopupWrap? = null
    @JvmField
    var activeEditAuto: MyEditAuto? = null
    @JvmField
    var decorViewCache: View? = null
    @JvmField
    var insetsControllerCache: WindowInsetsControllerCompat? = null
    @JvmField
    var isBrowserScreen: Boolean = false
    @JvmField
    var insetsDeferred: Boolean = false
    @JvmField
    var pagePaused: Boolean = false
    @JvmField
    var resumeRefresh: Boolean = false
    @JvmField
    var sideGesturesKnown: Boolean = false
    @JvmField
    var sideGestures: Boolean = false
    @JvmField
    var screenSize: Point? = null
    @JvmField
    var orientationCache: Byte = 0
    @JvmField
    var smallScreen: Boolean = false
    @JvmField
    var executorCache: ExecutorService? = null
    @JvmField
    var heightListener: MainHeightListener? = null

    internal inner class CurrentLocaleTask : Runnable {
        override fun run() {
            val mainActivity = this@MainActivity
            val resumeRefresh = mainActivity.resumeRefresh
            mainActivity.resumeRefresh = false
            val dp600Px: Int = MainApp.Companion.dp600
            val localeListCompatH = AppCompatDelegate.getApplicationLocales()
            val localeB =
                if (localeListCompatH.isEmpty()) null else localeListCompatH.get(
                    0
                )
            AppState.appLocale = localeB
            AppState.localeCache = localeB
            MainApp.Companion.currentLocale()
            val pointP = MainUtil.screenPoint(mainActivity)
            if (pointP != null) {
                mainActivity.screenSize = pointP
                if (mainActivity.isBrowserScreen) {
                    screenWidth = pointP.x
                    screenHeight = pointP.y
                }
            }
            val point = mainActivity.screenSize
            if (point == null || point.x <= point.y) {
                mainActivity.orientationCache = 1.toByte()
            } else {
                mainActivity.orientationCache = 2.toByte()
            }
            if (point != null) {
                var smallScreen = point.x
                val screenY = point.y
                if (smallScreen != 0 && screenY != 0) {
                    if (smallScreen > screenY) {
                        smallScreen = screenY
                    }
                    mainActivity.smallScreen = smallScreen < MainApp.Companion.dp600
                }
            }
            if (!resumeRefresh || mainActivity.insetsListenerSet) {
                return
            }
            if (!MainConst.isApi30 && mainActivity.isBrowserScreen) {
                if (BrowserSettings.showStatus.value) {
                    MainSettings.editLocal { set(MainSettings.statusConfirmed, false) }
                }
                if (BrowserSettings.showNavi.value) {
                    MainSettings.editLocal { set(MainSettings.naviConfirmed, false) }
                    MainSettings.editLocal { set(MainSettings.naviHeight, 0) }
                }
            }
            if (!MainSettings.statusConfirmed.value || MainSettings.statusHeight.value <= 0 || !MainSettings.naviConfirmed.value || MainSettings.naviHeight.value <= 0) {
                val handler: Handler? = if ((!BrowserSettings.showStatus.value && !BrowserSettings.showNavi.value) || mainActivity.isLandscapeScreen()) null else mainActivity.mainHandler
                if ((!BrowserSettings.showStatus.value && !BrowserSettings.showNavi.value) || mainActivity.isLandscapeScreen() || handler == null) {
                    return
                }
                handler.post(mainActivity.UpdateCutoutInsetsTask())
            }
        }
    }

    internal inner class UpdateCutoutInsetsTask : Runnable {
        override fun run() {
            if (!MainSettings.statusConfirmed.value || MainSettings.statusHeight.value <= 0 || !MainSettings.naviConfirmed.value || MainSettings.naviHeight.value <= 0) {
                if (BrowserSettings.showStatus.value || BrowserSettings.showNavi.value) {
                    val mainActivity = this@MainActivity
                    if (mainActivity.isLandscapeScreen()) {
                        return
                    }
                    val statusHeight = MainSettings.statusHeight.value
                    val naviHeight = MainSettings.naviHeight.value
                    MainUtil.updateCutoutInsets(mainActivity, mainActivity.getWindow(), mainActivity.decorViewCached())
                    val mainHeightListener: MainHeightListener? = if ((statusHeight == MainSettings.statusHeight.value && naviHeight == MainSettings.naviHeight.value)) null else mainActivity.heightListener
                    if ((statusHeight == MainSettings.statusHeight.value && naviHeight == MainSettings.naviHeight.value) || mainHeightListener == null) {
                        return
                    }
                    mainHeightListener.onHeightChanged()
                }
            }
        }
    }

    internal inner class ApplyInsetsStep : View.OnApplyWindowInsetsListener {
        override fun onApplyWindowInsets(view: View, windowInsets: WindowInsets): WindowInsets {
            val widthNow: Int = screenWidth
            this@MainActivity.applyInsets(windowInsets)
            return windowInsets
        }
    }

    internal inner class InsetPaddingChangedTask : Runnable {
        override fun run() {
            val widthNow: Int = screenWidth
            val mainActivity = this@MainActivity
            if (mainActivity.insetPaddingChanged()) {
                mainActivity.insetView!!.setPadding(
                    mainActivity.insetLeft,
                    mainActivity.insetTop,
                    mainActivity.insetRight,
                    mainActivity.insetBottom
                )
                val mainInsetListener = mainActivity.insetListener
                if (mainInsetListener != null) {
                    mainInsetListener.onImeHidden()
                }
            }
        }
    }

    internal inner class InsetTask : Runnable {
        override fun run() {
            val mainActivity = this@MainActivity
            val mainViewerListener = mainActivity.viewerListener
            if (mainViewerListener != null) {
                mainViewerListener.onInset(mainActivity.insetBottom)
            }
        }
    }

    internal inner class ScheduleScreenOffTask : Runnable {

        internal inner class ScheduleScreenOffStep : Runnable {
            override fun run() {
                val mainActivity = this@MainActivity
                val widthNow: Int = screenWidth
                mainActivity.scheduleScreenOff()
            }
        }

        override fun run() {
            val mainActivity = this@MainActivity
            mainActivity.screenOffTimeout = MainUtil.screenOffTimeout(mainActivity.getApplicationContext())
            val handler = mainActivity.mainHandler
            if (handler == null) {
                return
            }
            handler.post(ScheduleScreenOffStep())
        }
    }

    internal inner class KeepScreenOnTask : Runnable {
        override fun run() {
            val mainActivity = this@MainActivity
            val view = mainActivity.screenOnView
            if (view == null || view.getKeepScreenOn()) {
                return
            }
            mainActivity.screenOnView!!.setKeepScreenOn(true)
        }
    }

    internal inner class ReleaseScreenOnTask : Runnable {
        override fun run() {
            val mainActivity = this@MainActivity
            val view = mainActivity.screenOnView
            if (view == null || !view.getKeepScreenOn()) {
                return
            }
            mainActivity.screenOnView!!.setKeepScreenOn(false)
        }
    }

    @RequiresApi(33)
    internal inner class BackKeyStep : OnBackInvokedCallback {
        override fun onBackInvoked() {
            val mainActivity = this@MainActivity
            val keyBackListener: KeyBackListener? = mainActivity.backKeyListener
            if (keyBackListener != null) {
                if (keyBackListener.onBackKey(true, true)) {
                    return
                }
            } else if (mainActivity.dismissPopups()) {
                return
            }
            mainActivity.onBackRequested()
        }
    }

    internal inner class DismissNowTask : Runnable {
        override fun run() {
            val widthNow: Int = screenWidth
            val mainActivity = this@MainActivity
            val myPopupWrap: MyPopupWrap? = mainActivity.popupMenu
            mainActivity.popupMenu = null
            if (myPopupWrap == null) {
                return
            }
            myPopupWrap.dismissNow()
        }
    }

    class EventHandler(mainActivity: MainActivity?) : Handler(Looper.getMainLooper()) {
        @JvmField
        var activityRef: WeakReference<*>

        init {
            this.activityRef = WeakReference<Any?>(mainActivity)
        }

        override fun handleMessage(message: Message) {
            val mainActivity = this.activityRef.get() as MainActivity?
            if (mainActivity == null || message.what != 0) {
                return
            }
            mainActivity.updateScreenOn(false)
        }
    }

    interface MainHeightListener {
        fun onHeightChanged()
    }

    interface MainInsetListener {
        fun onInsetChanged(bottom: Int)

        fun onImeHidden()
    }

    interface MainViewerListener {
        fun onInset(bottom: Int)
    }

    fun dismissPopups(): Boolean {
        val myEditAuto: MyEditAuto? = this.activeEditAuto
        this.activeEditAuto = null
        if (myEditAuto != null) {
            myEditAuto.dismissDropDown()
            return true
        }
        val myPopupWrap: MyPopupWrap? = this.popupMenu
        this.popupMenu = null
        if (myPopupWrap == null) {
            return false
        }
        myPopupWrap.dismiss()
        return true
    }

    fun updateSplitLand(split: Boolean) {
        var splitNow = false
        if (split && !isInMultiWindowMode()) {
            var widthNow: Int = screenWidth
            var heightNow: Int = screenHeight
            if (widthNow != 0 && heightNow != 0) {
                if (widthNow < heightNow) {
                    heightNow = widthNow
                    widthNow = heightNow
                }
                val index = TtsSettings.webLand.value + TtsSettings.webLand2.value
                if (index <= widthNow / 3 && widthNow - index > heightNow) {
                    splitNow = true
                }
            }
        }
        splitLand = splitNow
    }

    fun decorViewCached(): View? {
        val view = this.decorViewCache
        if (view != null) {
            return view
        }
        try {
            val window = getWindow()
            if (window == null) {
                return this.decorViewCache
            }
            this.decorViewCache = window.getDecorView()
            return this.decorViewCache
        } catch (unused: Exception) {
        }
        return null
    }

    fun insetsControllerCompat(): WindowInsetsControllerCompat? {
        val windowInsetsControllerCompat = this.insetsControllerCache
        if (windowInsetsControllerCompat != null) {
            return windowInsetsControllerCompat
        }
        val windowInsetsControllerCompatO = MainUtil.insetsController(getWindow(), decorViewCached())
        this.insetsControllerCache = windowInsetsControllerCompatO
        return windowInsetsControllerCompatO
    }

    fun installInsetsListener() {
        val viewZ: View? = if (!MainConst.isApi30Insets || this.insetsListenerSet) null else decorViewCached()
        if (!MainConst.isApi30Insets || this.insetsListenerSet || viewZ == null) {
            return
        }
        this.insetsListenerSet = true
        viewZ.setOnApplyWindowInsetsListener(ApplyInsetsStep())
        MainUtil.setDecorFits(getWindow(), false)
    }

    fun registerBackCallback(keyBackListener: KeyBackListener?) {
        if (Build.VERSION.SDK_INT >= 33 && MainConst.isApi33 && this.backCallback == null) {
            this.backKeyListener = keyBackListener
            this.backCallback = BackKeyStep()
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                if (keyBackListener != null) OnBackInvokedDispatcher.PRIORITY_OVERLAY else 0,
                this.backCallback!!
            )
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val actionMasked: Int
        if (this.screenOnView != null && ((event!!.getActionMasked()
                .also { actionMasked = it }) == 1 || actionMasked == 3)
        ) {
            updateScreenOn(true)
        }
        return super.dispatchTouchEvent(event)
    }

    fun insetPaddingChanged(): Boolean {
        val view = this.insetView
        if (view == null) {
            return false
        }
        return if (view.getPaddingTop() == this.insetTop && view.getPaddingBottom() == this.insetBottom && view.getPaddingLeft() == this.insetLeft && view.getPaddingRight() == this.insetRight) false else true
    }

    fun isLandscapeScreen(): Boolean {
        var orientationCache = this.orientationCache
        if (orientationCache.toInt() == 0) {
            val pointP = MainUtil.screenPoint(this)
            if (pointP != null) {
                this.screenSize = pointP
                if (this.isBrowserScreen) {
                    screenWidth = pointP.x
                    screenHeight = pointP.y
                }
            }
            val point = this.screenSize
            if (point == null || point.x <= point.y) {
                this.orientationCache = 1.toByte()
                orientationCache = 1
            } else {
                this.orientationCache = 2.toByte()
                orientationCache = 2
            }
        }
        return orientationCache.toInt() == 2
    }

    fun hasSideGestures(): Boolean {
        if (this.sideGesturesKnown) {
            return this.sideGestures
        }
        if (!MainConst.isApi30) {
            return false
        }
        try {
            val viewZ = decorViewCached()
            if (viewZ != null) {
                return readSideGestures(viewZ.getRootWindowInsets())
            }
            return false
        } catch (unused: Exception) {
            return false
        }
    }

    fun readSideGestures(windowInsets: WindowInsets?): Boolean {
        if (MainConst.isApi30 && windowInsets != null) {
            try {
                val insets = windowInsets.getInsets(WindowInsets.Type.systemGestures())
                val sideGestures = insets.left > 0 || insets.right > 0
                this.sideGesturesKnown = true
                this.sideGestures = sideGestures
                return sideGestures
            } catch (unused: Exception) {
            }
        }
        return false
    }

    fun initMainScreenOn(view: View?) {
        this.screenOnView = view
    }

    open fun onLauncherResult(requestCode: Int, resultCode: Int, intent: Intent?) {
    }

    open fun onBackRequested() {
        if (dismissPopups()) {
            return
        }
        finish()
    }

    fun runInBackground(runnable: Runnable?) {
        var executorServiceH = this.executorCache
        if (executorServiceH == null || executorServiceH.isShutdown() || executorServiceH.isTerminated()) {
            executorServiceH = MainApp.Companion.executor(getApplicationContext())
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

    fun swapScreenOnView(view: View?, keepOn: Boolean) {
        if (keepOn) {
            this.savedScreenOnView = this.screenOnView
            this.screenOnView = view
        } else {
            this.screenOnView = this.savedScreenOnView
            this.savedScreenOnView = null
        }
        this.screenOnFresh = true
        updateScreenOn(true)
    }

    fun applyInsets(windowInsets: WindowInsets?) {
        var leftPad: Int
        val insetBottom: Int
        var insetRight: Int
        var bottomPad: Int
        var insetTop: Int
        if (!MainConst.isApi30Insets || windowInsets == null) {
            return
        }
        try {
            val statusInsets = windowInsets.getInsets(WindowInsets.Type.statusBars())
            val insets: Insets? = windowInsets.getInsets(WindowInsets.Type.navigationBars())
            val barInsets: Insets? = if (insets == null) null else windowInsets.getInsets(WindowInsets.Type.ime())
            if (insets == null || barInsets == null) {
                return
            }
            if (this.isBrowserScreen && !this.sideGesturesKnown) {
                readSideGestures(windowInsets)
            }
            val top = statusInsets.top
            var topPad = 0
            if (top > 0) {
                insetBottom = top
                insetTop = insetBottom
                leftPad = 0
                bottomPad = 0
                insetRight = 0
            } else {
                val bottom = statusInsets.bottom
                if (bottom > 0) {
                    insetBottom = bottom
                    bottomPad = insetBottom
                    leftPad = 0
                    insetTop = 0
                } else {
                    leftPad = statusInsets.left
                    if (leftPad > 0) {
                        insetBottom = 0
                    } else {
                        val right = statusInsets.right
                        if (right > 0) {
                            insetRight = right
                            leftPad = 0
                            insetBottom = 0
                            bottomPad = 0
                            insetTop = 0
                        } else {
                            leftPad = 0
                            insetBottom = 0
                        }
                    }
                    bottomPad = insetBottom
                    insetTop = bottomPad
                }
                insetRight = insetTop
            }
            val topNow = insets.top
            if (topNow > 0) {
                topPad = topNow
                insetTop = topPad
            } else {
                val bottom = insets.bottom
                if (bottom > 0) {
                    topPad = bottom
                    bottomPad = topPad
                } else {
                    val left = insets.left
                    if (left > 0) {
                        leftPad = left
                    } else {
                        val right = insets.right
                        if (right > 0) {
                            insetRight = right
                        }
                    }
                }
            }
            val imeSize = abs(barInsets.bottom - barInsets.top)
            if (this.imeHeight != imeSize) {
                this.imeHeight = imeSize
                val mainInsetListener = this.insetListener
                if (mainInsetListener != null) {
                    mainInsetListener.onInsetChanged(imeSize)
                }
            }
            MainSettings.editLocal { set(MainSettings.statusHeight, insetBottom) }
            MainSettings.editLocal { set(MainSettings.naviHeight, topPad) }
            MainSettings.editLocal { set(MainSettings.statusConfirmed, true) }
            MainSettings.editLocal { set(MainSettings.naviConfirmed, true) }
            if (this.isBrowserScreen) {
                leftInset = leftPad
            } else if (this.imeInBottomInset && imeSize > bottomPad) {
                bottomPad = imeSize
            }
            this.insetTop = insetTop
            this.insetBottom = bottomPad
            this.insetLeft = leftPad
            this.insetRight = insetRight
            if (this.insetsDeferred) {
                val handler = this.mainHandler
                if (handler != null) {
                    handler.post(InsetTask())
                    return
                }
                return
            }
            val handler: Handler? = if (!insetPaddingChanged()) null else this.mainHandler
            if (!insetPaddingChanged() || handler == null) {
                return
            }
            handler.post(InsetPaddingChangedTask())
        } catch (unused: Exception) {
        }
    }

    override fun launchForResult(intent: Intent?, requestCode: Int) {
        if (requestCode !in 0..RequestCode.LAST_LAUNCHER) {
            if (intent != null) {
                startActivity(intent)
            }
            return
        }
        if (intent == null) {
            // registration: before the activity starts (onCreate); the result comes back with the same code
            this.launchers[requestCode] = registerForActivityResult(StartActivityForResult()) { result ->
                onLauncherResult(requestCode, result.resultCode, result.data)
            }
            return
        }
        // a code nobody registered does nothing, as before
        this.launchers[requestCode]?.launch(intent, null)
    }

    /**
     * Android 12L only (minSdk 32): from Android 13 on, back goes through [BackKeyStep] instead. No `super` call on
     * purpose: it would close the activity, while back here first closes popups and steps back in the page / tabs.
     */
    @Deprecated("Deprecated in Java")
    @android.annotation.SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (dismissPopups()) {
            return
        }
        onBackRequested()
    }

    override fun onConfigurationChanged(configuration: Configuration) {
        super.onConfigurationChanged(configuration)
        if (configuration.orientation == 2) {
            this.orientationCache = 2.toByte()
        } else {
            this.orientationCache = 1.toByte()
        }
        runInBackground(CurrentLocaleTask())
        val handler: Handler? = if (this.popupMenu == null) null else this.mainHandler
        if (this.popupMenu == null || handler == null) {
            return
        }
        handler.post(DismissNowTask())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.mainHandler = Handler(Looper.getMainLooper())
        runInBackground(CurrentLocaleTask())
        if (this.ownBackHandling) {
            return
        }
        registerBackCallback(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        val eventHandler = this.screenOffHandler
        if (eventHandler != null) {
            eventHandler.removeMessages(0)
            this.screenOffHandler = null
        }
        this.screenOnView = null
        this.savedScreenOnView = null
        this.heldScreenOnView = null
        MainUtil.clearHandler(this.mainHandler)
        this.mainHandler = null
        this.decorViewCache = null
        this.insetsControllerCache = null
        this.screenSize = null
        this.executorCache = null
        this.insetView = null
        this.insetListener = null
        this.heightListener = null
        this.backCallback = null
        this.backKeyListener = null
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        updateScreenOn(true)
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        super.onPause()
        updateScreenOn(false)
        val handler: Handler? = if (this.popupMenu != null) this.mainHandler else null
        if (this.popupMenu != null && handler != null) {
            handler.post(DismissNowTask())
        }
        val mainAppO: MainApp? = MainApp.Companion.from(getApplicationContext())
        val mainDownSvc: MainDownSvc? = if (mainAppO == null || !mainAppO.downBinding) null else mainAppO.downService
        if (mainAppO == null || !mainAppO.downBinding || mainDownSvc == null) {
            return
        }
        mainDownSvc.setTimer(true)
    }

    override fun onResume() {
        super.onResume()
        this.resumeRefresh = true
        this.sideGesturesKnown = false
        runInBackground(CurrentLocaleTask())
        this.screenOnFresh = true
        updateScreenOn(true)
        val mainAppO: MainApp? = MainApp.Companion.from(getApplicationContext())
        val mainDownSvc: MainDownSvc? = if (mainAppO == null || !mainAppO.downBinding) null else mainAppO.downService
        if (mainAppO == null || !mainAppO.downBinding || mainDownSvc == null) {
            return
        }
        mainDownSvc.setTimer(false)
    }

    fun scheduleScreenOff() {
        val view = this.screenOnView
        if (view != null) {
            val index = 3600000 - this.screenOffTimeout
            if (index <= 0) {
                val handler: Handler? = if (!view.getKeepScreenOn()) null else this.mainHandler
                if (!view.getKeepScreenOn() || handler == null) {
                    return
                }
                handler.post(ReleaseScreenOnTask())
                return
            }
            var eventHandler = this.screenOffHandler
            if (eventHandler == null) {
                eventHandler = EventHandler(this)
                this.screenOffHandler = eventHandler
            }
            eventHandler.sendEmptyMessageDelayed(0, index.toLong())
            val screenOnView = this.screenOnView
            val handler: Handler? = if (screenOnView == null || screenOnView.getKeepScreenOn()) null else this.mainHandler
            if (screenOnView == null || screenOnView.getKeepScreenOn() || handler == null) {
                return
            }
            handler.post(KeepScreenOnTask())
        }
    }

    fun updateScreenOn(keepOn: Boolean) {
        if (this.screenOnView != null) {
            val eventHandler = this.screenOffHandler
            if (eventHandler != null) {
                eventHandler.removeMessages(0)
                if (PdfSettings.screenOff.value != 1) {
                    this.screenOffHandler = null
                }
            }
            val screenOff = PdfSettings.screenOff.value
            if (screenOff == 2) {
                val view = this.screenOnView
                val handler: Handler? = if (view == null || view.getKeepScreenOn()) null else this.mainHandler
                if (view == null || view.getKeepScreenOn() || handler == null) {
                    return
                }
                handler.post(KeepScreenOnTask())
                return
            }
            if (keepOn && screenOff != 0) {
                if (!this.screenOnFresh) {
                    scheduleScreenOff()
                    return
                } else {
                    this.screenOnFresh = false
                    runInBackground(ScheduleScreenOffTask())
                    return
                }
            }
            val view = this.screenOnView
            val handler: Handler? = if (view == null || !view.getKeepScreenOn()) null else this.mainHandler
            if (view == null || !view.getKeepScreenOn() || handler == null) {
                return
            }
            handler.post(ReleaseScreenOnTask())
        }
    }

    fun holdScreenOn(view: View?, keepOn: Boolean) {
        val eventHandler = this.screenOffHandler
        if (eventHandler != null) {
            eventHandler.removeMessages(0)
        }
        if (keepOn) {
            this.heldScreenOnView = this.screenOnView
            this.screenOnView = null
            view!!.setKeepScreenOn(true)
        } else {
            view!!.setKeepScreenOn(false)
            this.screenOnView = this.heldScreenOnView
            this.heldScreenOnView = null
            this.screenOnFresh = true
            updateScreenOn(true)
        }
    }

    override fun launchExternalForResult(intent: Intent?, requestCode: Int) {
        launchForResult(intent, requestCode)
    }

    companion object {
        @JvmField
        var screenWidth: Int = 0
        @JvmField
        var screenHeight: Int = 0
        @JvmField
        var leftInset: Int = 0
        @JvmField
        var splitLand: Boolean = false
    }
}
