package com.mycompany.app.dialog

import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.ReadSettings
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Handler
import android.text.TextUtils
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.mycompany.app.dialog.DialogDownPage.DownPageListener
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainUtil.destroyWebViewNow
import com.mycompany.app.main.MainUtil.stripQuotes
import com.mycompany.app.main.MainUtil.setAlgorithmicDark
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.cancelToast
import com.mycompany.app.main.MainUtil.openWithApp
import com.mycompany.app.main.MainUtil.unescapeJavaText
import com.mycompany.app.main.MainUtil.lacksAlgorithmicDark
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.defaultUserAgent
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSet.putBool
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.view.MyFadeFrame
import com.mycompany.app.view.MyFadeListener
import com.mycompany.app.view.MyFindView
import com.mycompany.app.ui.components.GuideState
import com.mycompany.app.ui.components.PinchGuideCard
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import com.mycompany.app.view.MySnackbar
import com.mycompany.app.view.MySnackbar.SnackbarListener
import com.mycompany.app.web.WebNestView
import com.mycompany.app.web.WebSrcView
import org.jsoup.Jsoup
import kotlin.math.max
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mycompany.app.ui.browser.ComposeFullDialog

/**
 * Full-screen page source viewer ([pageUrl] the page URL, [pageTitle] its title): loads "view-source:" in a [WebSrcView]
 * (progress bar, dark filter in dark mode, pinch guide once), with controls that fade in on a tap: back, save
 * (the page HTML from [WebNestView], or Jsoup for plain-text pages, into [DialogSaveSource] + a snackbar),
 * brightness ([DialogSeekBright]) and find ([MyFindView]). Compose now (on [ComposeFullDialog]); same constructor,
 * shows itself.
 */
@Suppress("deprecation")
class DialogViewSrc(
    mainActivity: MainActivity?,
    webNestView: WebNestView?,
    pageUrl: String?,
    pageTitle: String?
) : ComposeFullDialog(mainActivity) {
    /** Compose stand-in for the old fading control layer (auto-hides after 3 s like `MyFadeFrame`). */
    class Fade(private val ui: Handler) {
        var shown by mutableStateOf(false)
        private val hide = Runnable { shown = false }
        fun hideNow(animate: Boolean) { ui.removeCallbacks(hide); shown = false }
        fun showNow(animate: Boolean) { ui.removeCallbacks(hide); shown = true; ui.postDelayed(hide, 3000) }
        fun toggle(show: Boolean) { if (show) showNow(true) else hideNow(true) }
        fun isShown(): Boolean = shown
        fun restartHide() { ui.removeCallbacks(hide); if (shown) ui.postDelayed(hide, 3000) }
        fun hold() { ui.removeCallbacks(hide) }
        fun cancelHide() { ui.removeCallbacks(hide) }
    }

    /** Compose stand-in for the old loading cover. */
    class Cover {
        var shown by mutableStateOf(false)
        fun show(dialogWebBookDir: Boolean) { shown = true }
        fun hide(fade: Boolean) { shown = false }
        fun reset() {}
    }

    /** Compose stand-in for the old top progress bar ([x] = hidden). */
    class Bar(private val ui: Handler) {
        private var value by mutableFloatStateOf(0f)
        /** 0..100, clamped like the old bar (the "page done" check waits for exactly 100). */
        var progress: Float
            get() = value
            set(f) { value = f.coerceIn(0f, 100f) }
        var skipping by mutableStateOf(true)
        fun setSkipDraw(newSkipping: Boolean) { skipping = newSkipping }
        fun removeCallbacks(runnable: Runnable) { ui.removeCallbacks(runnable) }
        fun post(runnable: Runnable) { ui.post(runnable) }
        fun refresh() {}
    }

    private val ui = Handler(android.os.Looper.getMainLooper())
    @JvmField val mainHandler: Handler? = ui
    private var executor: java.util.concurrent.ExecutorService? = null

    @JvmField var hostActivity: MainActivity? = mainActivity
    @JvmField var appContext: Context? = context
    @JvmField var webNest: WebNestView? = webNestView
    /** Parent for the snackbar and the pinch guide (the window's content frame). */
    @JvmField var contentRoot: ViewGroup? = null
    @JvmField var srcView: WebSrcView? = null
    @JvmField var pageLoading: Boolean = false
    @JvmField var loadBar: Bar? = Bar(ui)
    @JvmField var fadeFrame: Fade? = Fade(ui)
    @JvmField var loadCover: Cover? = Cover()
    /** The one-time pinch guide (old `MyFadeFrame` added to the window), drawn by the page. */
    @JvmField var guideHelper: GuideState? = null
    private var guide by mutableStateOf<GuideState?>(null)
    @JvmField var findView: MyFindView? = null
    @JvmField var gestureDetector: GestureDetector? = null
    @JvmField var pageUrl: String? = pageUrl
    @JvmField var bodyOnly: Boolean = false
    @JvmField var pageTitle: String? = pageTitle
    @JvmField var guideChecked: Boolean = false
    @JvmField var sourceText: String? = null
    @JvmField var saveDialog: DialogSaveSource? = null
    @JvmField var brightDialog: DialogSeekBright? = null
    @JvmField var darkShown: Boolean = AppState.darkUi
    @JvmField var jsValue: String? = null
    @JvmField var finishHandled: Boolean = false
    @JvmField var lastFinishedUrl: String? = null
    @JvmField var progressTarget: Int = 0
    @JvmField var progressRunnable: Runnable? = null
    @JvmField var snackbar: MySnackbar? = null

    private var web by mutableStateOf<WebSrcView?>(null)
    private var find by mutableStateOf<MyFindView?>(null)
    private var dark by mutableStateOf(AppState.darkUi)
    private var contentTop = 0f
    private var contentBottom = 0f

    init {
        progressRunnable = SetLoadProgressTask()
        val fade = fadeFrame!!
        val bar = loadBar!!
        val cover = loadCover!!
        setPage {
            val dark = this.dark
            Box(
                Modifier.fillMaxSize().background(if (dark) Color.Black else Color(0xFFF8F8F8.toInt())).pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            if (event.type == PointerEventType.Press && guideHelper?.shown == true) guideTouched()
                        }
                    }
                }
            ) {
                Column(Modifier.fillMaxSize().systemBarsPadding().imePadding()) {
                    Box(
                        Modifier.weight(1f).fillMaxWidth().background(if (dark) Color(0xFF212121.toInt()) else Color.White)
                            .onGloballyPositioned { val r = it.boundsInWindow(); contentTop = r.top; contentBottom = r.bottom }
                    ) {
                        web?.let { view ->
                            AndroidView(
                                factory = { view.apply { layoutParams = ViewGroup.LayoutParams(-1, -1) } },
                                modifier = Modifier.fillMaxSize(),
                            )
                        }
                        if (!bar.skipping) {
                            LinearProgressIndicator(
                                progress = { bar.progress / 100f },
                                modifier = Modifier.fillMaxWidth().height(2.dp).align(Alignment.TopCenter),
                            )
                        }
                        androidx.compose.animation.AnimatedVisibility(visible = fade.shown, modifier = Modifier.matchParentSize(), enter = fadeIn(), exit = fadeOut()) {
                            Box(Modifier.fillMaxSize()) {
                                Row(Modifier.fillMaxWidth().padding(8.dp).align(Alignment.TopCenter)) {
                                    FilledTonalIconButton(onClick = { dismiss() }) {
                                        Icon(painterResource(AppR.drawable.outline_chevron_left_black_24), null)
                                    }
                                    Spacer(Modifier.weight(1f))
                                    FilledTonalIconButton(onClick = { OpenSaveDialogClick().onClick(null) }) {
                                        Icon(painterResource(AppR.drawable.outline_download_black_20), stringResource(AppR.string.save))
                                    }
                                }
                                Row(
                                    Modifier.fillMaxWidth().height(80.dp).align(Alignment.BottomCenter),
                                    horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    FilledTonalIconButton(onClick = { HideNowClick().onClick(null) }) {
                                        Icon(painterResource(AppR.drawable.outline_brightness_6_black_24), null)
                                    }
                                    FilledTonalIconButton(onClick = { FindClick().onClick(null) }) {
                                        Icon(painterResource(AppR.drawable.outline_search_black_24), null)
                                    }
                                }
                            }
                        }
                        if (cover.shown) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        }
                        guide?.let { g -> PinchGuideCard(g, onGone = { if (guideHelper === g) guideGone() }, modifier = Modifier.matchParentSize()) }
                    }
                    find?.let { view ->
                        AndroidView(
                            factory = { view.apply { layoutParams = ViewGroup.LayoutParams(-1, MainApp.dp48) } },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
        contentRoot = window?.decorView?.findViewById(android.R.id.content)
        window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setCanceledOnTouchOutside(false)
        show()
        hostActivity?.swapScreenOnView(contentRoot, true)
        gestureDetector = GestureDetector(appContext, ToggleGestures())
        ui.post { start() }
    }

    fun runAsync(runnable: Runnable?) {
        var ex = executor
        if (ex == null || ex.isShutdown() || ex.isTerminated()) {
            ex = MainApp.executor(context) ?: return
            executor = ex
        }
        try { ex.execute(runnable) } catch (unused: Exception) { }
    }

    /** the source view, then its setup ([LoadUrlTask]). */
    private fun start() {
        if (appContext == null || hostActivity == null) return
        val webSrcView = WebSrcView(hostActivity)
        webSrcView.setBackgroundColor(if (AppState.darkUi) -14606047 else -1)
        srcView = webSrcView
        web = webSrcView
        ui.post(LoadUrlTask())
    }

    /** Old `SrcViewListener` (only drew a scroll shadow and moved the old scroll bar). */
    internal inner class SrcViewListener : com.mycompany.app.web.WebNestView.WebViewListener {
        override fun onCrashAction(action: Int) {}
        override fun openAdvanced() {}
        override fun onProgress(progress: Int) {}
        override fun canPull(): Boolean = false
        override fun onScrollStop() {}
        override fun onBlockCount(count: Int) {}
        override fun onPull(x: Float, y: Float, action: Int): Boolean = false
        override fun onPageCommit(pageUrl: String?) {}
    }

    /** Old `ToggleGestures`: a single tap on the page (below the top-bar strip) shows / hides the controls. */
    internal inner class ToggleGestures : SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val myFindView = findView
            if (myFindView != null && myFindView.getVisibility() == View.VISIBLE) return false
            val y = e.getY()
            if (y >= contentTop + MainApp.dp56 && y < contentBottom) {
                val fade = fadeFrame ?: return false
                fade.toggle(!fade.isShown())
                return true
            }
            return false
        }
    }

    internal inner class LoadUrlTask : Runnable {
        internal inner class LoadUrlStep : Runnable {
            internal inner class SetSrcClientStep : Runnable {
                internal inner class LoadSourceStep : Runnable {
                    internal inner class BuildFindViewStep : Runnable {
                        override fun run() {
                            val dialogViewSrc = this@DialogViewSrc
                            if (dialogViewSrc.appContext != null) {
                                val myFindView = MyFindView(dialogViewSrc.hostActivity)
                                dialogViewSrc.findView = myFindView
                                myFindView.setVisibility(View.GONE)
                                val findView = dialogViewSrc.findView
                                val webSrcView = dialogViewSrc.srcView
                                val closeFindApply = dialogViewSrc.CloseFindApply()
                                findView!!.findWeb = webSrcView
                                findView.startText = null
                                findView.tintBar = false
                                findView.closeListener = closeFindApply
                                dialogViewSrc.find = findView
                                val handler = dialogViewSrc.mainHandler
                                if (handler == null) {
                                    return
                                }
                                handler.post(dialogViewSrc.BuildFindViewTask())
                            }
                        }
                    }
                    override fun run() {
                        val loadUrlStep = this@LoadUrlStep
                        val webSrcView = this@DialogViewSrc.srcView
                        if (webSrcView != null) {
                            webSrcView.loadUrl("view-source:" + this@DialogViewSrc.pageUrl)
                            this@DialogViewSrc.setLoadProgress(0)
                            val handler = this@DialogViewSrc.mainHandler
                            if (handler == null) {
                                return
                            }
                            handler.post(BuildFindViewStep())
                        }
                    }
                }
                override fun run() {
                    val loadUrlTask = this@LoadUrlTask
                    val dialogViewSrc = this@DialogViewSrc
                    val webSrcView = dialogViewSrc.srcView
                    if (webSrcView != null) {
                        webSrcView.setWebViewClient(dialogViewSrc.LocalWebViewClient())
                        webSrcView.setWebChromeClient(dialogViewSrc.LocalChromeClient())
                        webSrcView.setListener(dialogViewSrc.SrcViewListener())
                        val handler = this@DialogViewSrc.mainHandler
                        if (handler == null) {
                            return
                        }
                        handler.post(LoadSourceStep())
                    }
                }
            }
            override fun run() {
                val dialogViewSrc = this@DialogViewSrc
                val webSrcView = dialogViewSrc.srcView
                if (webSrcView != null) {
                    setAlgorithmicDark(webSrcView.getSettings(), AppState.darkUi)
                    val handler = dialogViewSrc.mainHandler
                    if (handler == null) {
                        return
                    }
                    handler.post(SetSrcClientStep())
                }
            }
        }
        override fun run() {
            val dialogViewSrc = this@DialogViewSrc
            val webSrcView = dialogViewSrc.srcView
            if (webSrcView != null) {
                val settings = webSrcView.getSettings()
                settings.setSupportZoom(true)
                settings.setBuiltInZoomControls(true)
                settings.setDisplayZoomControls(false)
                settings.setUseWideViewPort(true)
                settings.setLoadWithOverviewMode(true)
                settings.setJavaScriptCanOpenWindowsAutomatically(false)
                settings.setSupportMultipleWindows(false)
                settings.setMediaPlaybackRequiresUserGesture(false)
                settings.setJavaScriptEnabled(true)
                webSrcView.setOverScrollMode(2)
                webSrcView.setVerticalScrollBarEnabled(false)
                val handler = dialogViewSrc.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(LoadUrlStep())
            }
        }
    }
    internal inner class CloseFindApply : DialogApplyListener {
        override fun onApply() {
            val myFindView = this@DialogViewSrc.findView
            if (myFindView != null) {
                myFindView.close()
            }
        }
    }
    internal inner class BuildFindViewTask : Runnable {
        override fun run() {
            val myFindView = this@DialogViewSrc.findView
            if (myFindView == null) {
                return
            }
            myFindView.build(0, 0, false)
        }
    }


    /** Shows the pinch guide (once, while `mGuideSrc` is on). */
    internal inner class ShowGuideTask : Runnable {
        override fun run() {
            if (!ReadSettings.guideSrc.value || guideHelper != null || contentRoot == null) return
            val state = GuideState()
            guideHelper = state
            guide = state
        }
    }

    /** First touch while the guide shows: never show it again, fade it out (old listener 18). */
    private fun guideTouched() {
        if (ReadSettings.guideSrc.value) ReadSettings.edit(appContext) { set(ReadSettings.guideSrc, false) }
        guideHelper?.hideNow(true)
    }

    /** The guide has faded out: drop it and show the controls (old listener 17). */
    private fun guideGone() {
        guideHelper?.reset()
        guideHelper = null
        guide = null
        fadeFrame?.showNow(true)
    }

    internal inner class SourceHtmlCallback : ValueCallback<String> {
        internal inner class ParseSourceTask : Runnable {
            override fun run() {
                val dialogViewSrc = this@DialogViewSrc
                val jsValue = dialogViewSrc.jsValue
                var source: String? = null
                dialogViewSrc.jsValue = null
                if (dialogViewSrc.webNest != null) {
                    if ("null" == jsValue) {
                        val pageUrl = dialogViewSrc.pageUrl
                        if (URLUtil.isNetworkUrl(pageUrl)) {
                            try {
                                val document = Jsoup.connect(pageUrl!!).referrer(pageUrl)
                                    .userAgent(MainUtil.defaultUserAgent(dialogViewSrc.appContext)).get()
                                source =
                                    if (dialogViewSrc.bodyOnly) document.body().html() else document.html()
                            } catch (unused: Exception) {
                            } catch (unused: LinkageError) {
                            } catch (unused: OutOfMemoryError) {
                            }
                        }
                    } else {
                        source = MainUtil.unescapeJavaText(stripQuotes(jsValue))
                    }
                    dialogViewSrc.sourceText = source
                    val handler = dialogViewSrc.mainHandler
                    if (handler != null) {
                        handler.post(dialogViewSrc.ShowSourceTask())
                    }
                }
            }
        }
        override fun onReceiveValue(value: String?) {
            val dialogViewSrc = this@DialogViewSrc
            dialogViewSrc.jsValue = value
            dialogViewSrc.runAsync(ParseSourceTask())
        }
    }


    internal inner class ShowSourceTask : Runnable {
        override fun run() {
            val dialogViewSrc = this@DialogViewSrc
            val myCoverView = dialogViewSrc.loadCover
            if (myCoverView == null) {
                return
            }
            myCoverView.hide(true)
            if (TextUtils.isEmpty(dialogViewSrc.sourceText)) {
                showToast(dialogViewSrc.appContext, AppR.string.save_empty)
            } else {
                dialogViewSrc.openSaveDialog()
            }
        }
    }


    internal inner class ClearCacheTask : Runnable {
        override fun run() {
            val dialogViewSrc = this@DialogViewSrc
            MainUtil.clearWebCache(dialogViewSrc.srcView)
            dialogViewSrc.finishHandled = false
        }
    }
    internal inner class DismissTask : Runnable {
        override fun run() {
            this@DialogViewSrc.dismiss()
        }
    }
    internal inner class SetLoadProgressTask : Runnable {
        override fun run() {
            val dialogViewSrc = this@DialogViewSrc
            dialogViewSrc.setLoadProgress(dialogViewSrc.progressTarget)
        }
    }


    internal inner class SaveSourceListener : DownPageListener {
        override fun onSave(url: String?, path: String?, bitmap: Bitmap?) {
            val dialogViewSrc = this@DialogViewSrc
            val handler = dialogViewSrc.mainHandler
            if (handler == null) {
                return
            }
            handler.post(SavedSourceStep(dialogViewSrc, path))
        }
    }
    internal inner class SaveSourceDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogViewSrc = this@DialogViewSrc
            val dialogSaveSource = dialogViewSrc.saveDialog
            if (dialogSaveSource != null) {
                dialogSaveSource.dismiss()
                dialogViewSrc.saveDialog = null
            }
        }
    }
    internal inner class BrightDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogViewSrc = this@DialogViewSrc
            val dialogSeekBright = dialogViewSrc.brightDialog
            if (dialogSeekBright != null) {
                dialogSeekBright.dismiss()
                dialogViewSrc.brightDialog = null
            }
            val myFadeFrame = dialogViewSrc.fadeFrame
            if (myFadeFrame != null) {
                myFadeFrame.showNow(true)
            }
        }
    }
    internal inner class SavedSourceStep(val outer: DialogViewSrc,
                                          val path: String?
    ) : Runnable {
        internal inner class SavedSnackbarListener : SnackbarListener {
            override fun onSecond() {
            }
            override fun onDismissed() {
            }
            override fun onFirst() {
            }
            override fun onDismiss() {
                this@SavedSourceStep.outer.snackbar = null
            }
        }
        internal inner class OpenWithAppStep(val outer: SavedSourceStep,
                                             val url: String?
        ) : SnackbarListener {
            override fun onSecond() {
            }
            override fun onDismissed() {
            }
            override fun onFirst() {
                openWithApp(this.outer.outer.hostActivity, this.url, "text/plain", true, false)
            }
            override fun onDismiss() {
                this.outer.outer.snackbar = null
            }
        }
        override fun run() {
            val dialogViewSrc = this.outer
            if (dialogViewSrc.contentRoot == null) {
                return
            }
            cancelToast()
            val mySnackbar = dialogViewSrc.snackbar
            if (mySnackbar != null) {
                mySnackbar.hide(false)
                dialogViewSrc.snackbar = null
            }
            dialogViewSrc.snackbar = MySnackbar(dialogViewSrc.hostActivity!!)
            val path = this.path
            val pathEmpty = TextUtils.isEmpty(path)
            val snackbar = dialogViewSrc.snackbar
            if (pathEmpty) {
                snackbar!!.showSingle(
                    dialogViewSrc.contentRoot,
                    AppR.string.save_fail,
                    SavedSnackbarListener()
                )
            } else {
                snackbar!!.showWithButton(
                    dialogViewSrc.contentRoot,
                    AppR.string.save_success,
                    1,
                    OpenWithAppStep(this, path)
                )
            }
        }
    }


    internal inner class OpenSaveDialogClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewSrc = this@DialogViewSrc
            if (!dialogViewSrc.guideChecked) {
                showToast(dialogViewSrc.appContext, AppR.string.wait_retry)
                return
            }
            if (dialogViewSrc.loadCover != null) {
                if (!TextUtils.isEmpty(dialogViewSrc.sourceText)) {
                    dialogViewSrc.openSaveDialog()
                    return
                }
                dialogViewSrc.loadCover!!.show(true)
                dialogViewSrc.bodyOnly = MainUtil.isUserScriptUrl(dialogViewSrc.pageUrl!!)
                if (dialogViewSrc.webNest == null) {
                    return
                }
                val sb = StringBuilder("(function(){")
                if (dialogViewSrc.bodyOnly) {
                    sb.append("if(document.body&&document.body.hasChildNodes()){for(var i=0;i<document.body.childNodes.length;i++){if(document.body.childNodes[i].tagName=='PRE'){var txt=document.body.childNodes[i].innerText;if(txt){return txt;}}}}")
                }
                sb.append("return document.documentElement.innerHTML;})();")
                dialogViewSrc.webNest!!.evaluateJavascript(
                    sb.toString(),
                    dialogViewSrc.SourceHtmlCallback()
                )
            }
        }
    }
    internal inner class HideNowClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewSrc = this@DialogViewSrc
            val dialogSeekBright: DialogSeekBright? = if (dialogViewSrc.hostActivity != null && dialogViewSrc.saveDialog == null) dialogViewSrc.brightDialog else null
            if (dialogViewSrc.hostActivity != null && dialogViewSrc.saveDialog == null && dialogSeekBright == null) {
                val brightDialog =
                    DialogSeekBright(dialogViewSrc.hostActivity, dialogViewSrc.getWindow(), 0, null)
                dialogViewSrc.brightDialog = brightDialog
                brightDialog.setOnDismissListener(dialogViewSrc.BrightDialogDismiss())
            }
            val myFadeFrame = dialogViewSrc.fadeFrame
            if (myFadeFrame != null) {
                myFadeFrame.hideNow(true)
            }
        }
    }
    internal inner class FindClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewSrc = this@DialogViewSrc
            val myFadeFrame = dialogViewSrc.fadeFrame
            if (myFadeFrame != null) {
                myFadeFrame.hideNow(true)
            }
            val myFindView = dialogViewSrc.findView
            if (myFindView == null || !myFindView.isReady || myFindView.getVisibility() == View.VISIBLE) {
                return
            }
            myFindView.setVisibility(View.VISIBLE)
            myFindView.resetCount()
            myFindView.focusEdit()
        }
    }


    private inner class LocalChromeClient : WebChromeClient() {
        override fun onProgressChanged(webView: WebView?, newProgress: Int) {
            this@DialogViewSrc.setLoadProgress(newProgress)
        }
    }


    fun applyDarkCss(darkUi: Boolean) {
        val webSrcView = this.srcView
        if (webSrcView == null) {
            return
        }
        if (darkUi) {
            evalJsNow(
                webSrcView,
                "if(document.head){if(!document.getElementById('sb_dark_style')){var ele=document.createElement('style');ele.id='sb_dark_style';ele.innerText='body{-webkit-filter:invert(1)hue-rotate(180deg);}';document.head.appendChild(ele);}}",
                false
            )
        } else {
            evalJsNow(
                webSrcView,
                "var edk=document.getElementById('sb_dark_style');if(edk){document.head.removeChild(edk);}",
                false
            )
        }
    }

    fun showGuide() {
        if (this.guideChecked) {
            return
        }
        this.guideChecked = true
        val guideSrc = ReadSettings.guideSrc.value
        val handler: Handler? = if (guideSrc && guideSrc && this.guideHelper == null && this.contentRoot != null) this.mainHandler else null
        if (guideSrc && guideSrc && this.guideHelper == null && this.contentRoot != null && handler != null) {
            handler.post(ShowGuideTask())
        }
    }

    fun setLoadProgress(progressTarget: Int) {
        this.progressTarget = progressTarget
        val myProgressBar = this.loadBar
        if (myProgressBar != null) {
            val progressValue = Math.round(myProgressBar.progress)
            if (progressTarget == 100 && progressValue == 100) {
                this.loadBar!!.setSkipDraw(true)
                showGuide()
                return
            }
            val loadBar = this.loadBar
            if (loadBar!!.skipping) {
                loadBar.progress = 0.0f
                this.loadBar!!.setSkipDraw(false)
                setLoadProgress(max(progressTarget, 50))
            } else if (progressValue < progressTarget) {
                loadBar.progress = (progressValue + 3).toFloat()
                val runnable = this.progressRunnable
                if (runnable != null) {
                    this.loadBar!!.removeCallbacks(runnable)
                    this.loadBar!!.post(runnable)
                }
            }
        }
    }

    fun openSaveDialog() {
        val dialogSaveSource: DialogSaveSource? = if (this.hostActivity != null) this.saveDialog else null
        if (this.hostActivity != null && dialogSaveSource == null && this.brightDialog == null) {
            val saveDialog =
                DialogSaveSource(this.hostActivity, this.pageTitle, this.sourceText, null, SaveSourceListener())
            this.saveDialog = saveDialog
            saveDialog.setOnDismissListener(SaveSourceDialogDismiss())
        }
    }


    private inner class LocalWebViewClient : WebViewClient() {
        override fun onPageFinished(webView: WebView?, url: String?) {
            val dialogViewSrc = this@DialogViewSrc
            if (dialogViewSrc.srcView == null) {
                return
            }
            dialogViewSrc.pageLoading = false
            if (!dialogViewSrc.finishHandled && !TextUtils.isEmpty(url) && (url != dialogViewSrc.lastFinishedUrl)) {
                dialogViewSrc.finishHandled = true
                dialogViewSrc.lastFinishedUrl = url
                val handler = dialogViewSrc.mainHandler
                if (handler != null) {
                    handler.post(dialogViewSrc.ClearCacheTask())
                }
            }
            if (AppState.darkUi && MainUtil.lacksAlgorithmicDark()) {
                dialogViewSrc.applyDarkCss(true)
            }
            dialogViewSrc.showGuide()
        }
        override fun onPageStarted(webView: WebView?, url: String?, bitmap: Bitmap?) {
            val dialogViewSrc = this@DialogViewSrc
            if (dialogViewSrc.srcView != null) {
                dialogViewSrc.pageLoading = true
                if (AppState.darkUi && MainUtil.lacksAlgorithmicDark()) {
                    dialogViewSrc.applyDarkCss(true)
                }
            }
        }
        override fun onRenderProcessGone(
            webView: WebView?,
            renderProcessGoneDetail: RenderProcessGoneDetail?
        ): Boolean {
            val dialogViewSrc = this@DialogViewSrc
            dialogViewSrc.srcView = null
            onRenderGone(webView, renderProcessGoneDetail)
            val handler = dialogViewSrc.mainHandler
            if (handler == null) {
                return true
            }
            handler.post(dialogViewSrc.DismissTask())
            return true
        }
        override fun shouldOverrideUrlLoading(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): Boolean {
            return false
        }
    }


    /** Theme switched while open (called by the browser after `darkShown` changed). */
    fun applyTheme() {
        dark = AppState.darkUi
        srcView?.setBackgroundColor(if (AppState.darkUi) -14606047 else -1)
        findView?.applyColors(0, 0, false)
    }

    /** Old popup-menu close; there is no popup now. */
    fun refreshButtons() {}

    /** The dialog is up. */
    fun isOpen(): Boolean = isShowing

    fun tryClose() {
        val myFindView = findView
        if (myFindView != null && myFindView.getVisibility() == View.VISIBLE) {
            myFindView.close()
            return
        }
        val webSrcView = srcView
        if (webSrcView == null || !webSrcView.canGoBack()) {
            dismiss()
        } else {
            webSrcView.goBack()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        tryClose()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val actionMasked = event.getActionMasked()
        if (actionMasked == 0) {
            fadeFrame?.hold()
        } else if (actionMasked == 1 || actionMasked == 3) {
            fadeFrame?.restartHide()
        }
        gestureDetector?.onTouchEvent(event)
        return super.dispatchTouchEvent(event)
    }

    override fun dismiss() {
        if (appContext == null) {
            super.dismiss()
            return
        }
        saveDialog?.dismiss()
        saveDialog = null
        brightDialog?.dismiss()
        brightDialog = null
        findView?.release()
        findView = null
        val webSrcView = srcView
        if (webSrcView != null) {
            if (pageLoading) {
                pageLoading = false
                webSrcView.stopLoading()
            }
            destroyWebViewNow(srcView)
            srcView = null
        }
        fadeFrame?.cancelHide()
        progressRunnable?.let { ui.removeCallbacks(it) }
        guideHelper?.reset()
        guideHelper = null
        snackbar?.hide(false)
        snackbar = null
        hostActivity?.swapScreenOnView(null, false)
        hostActivity = null
        appContext = null
        webNest = null
        contentRoot = null
        gestureDetector = null
        pageUrl = null
        pageTitle = null
        sourceText = null
        super.dismiss()
    }

    companion object {
    }
}
