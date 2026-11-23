package com.mycompany.app.dialog

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.webkit.URLUtil
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.data.DataUrl
import com.mycompany.app.data.DataUrl.ImgCntItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.web.WebLoadTask
import com.mycompany.app.web.WebLoadTask.WebLoadTaskListener
import com.mycompany.app.web.WebViewActivity
import kotlinx.coroutines.delay
import kotlin.math.max

/**
 * "Save all images" loader for a normal page ([pageUrlParam] = page URL): drives the shared hidden page of [WebLoadTask]
 * (loads the page if needed, waits for progress, asks the task for the picture list), shows "Loading" / "Server delay"
 * with a progress bar (determinate while the page loads; indeterminate in mode [loadModeParam] = 2), then opens the image viewer
 * (or hands the list to [LoadImgListener.onImages]); without pictures: "No image" / server error / check network with Retry
 * and, with [showNewUrl], "New URL". The state machine (`loadMode` page state, `loadState` task state, `retryStep` retry counter, 5 s delay text,
 * progress polling in [showCount]) is the old one 1:1; only the views became Compose state. Compose now (on
 * [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogLoadImg(
    webViewActivity: WebViewActivity?,
    pageUrlParam: String?,
    canNewUrl: Boolean,
    showNewUrl: Boolean,
    loadModeParam: Int,
    loadImgListener: LoadImgListener?,
) : ComposeBottomDialog(webViewActivity) {
    @JvmField
    var hostActivity: WebViewActivity? = webViewActivity
    @JvmField
    var appContext: Context? = context
    @JvmField
    var loadListener: LoadImgListener? = loadImgListener
    @JvmField
    var loadMode: Byte = loadModeParam.toByte()
    @JvmField
    var loadState: Byte = 0
    @JvmField
    var fromList: Boolean = loadModeParam == 2
    @JvmField
    var opening: Boolean = false
    @JvmField
    var pageUrl: String? = pageUrlParam
    @JvmField
    var canNewUrl: Boolean = canNewUrl
    @JvmField
    var showNewUrl: Boolean = showNewUrl
    @JvmField
    var newUrlMode: Boolean = false
    @JvmField
    var lastTick: Long = 0
    @JvmField
    var pageProgress: Int = 0
    @JvmField
    var lastCount: Int = 0
    @JvmField
    var waitStart: Long = 0
    @JvmField
    var mainBusy: Boolean = false
    @JvmField
    var retryStep: Int = 0
    @JvmField
    var loadFailed: Boolean = false
    @JvmField
    var tickCount: Int = 0

    private val ui = Handler(Looper.getMainLooper())
    private val tick = Runnable { showCount(tickCount, false) }
    private var alive = true

    private var titleRes by mutableIntStateOf(AppR.string.loading)
    private var loadingVisible by mutableStateOf(false)
    private var progress by mutableFloatStateOf(0f)
    private var indeterminate by mutableStateOf(false)
    private var messageRes by mutableIntStateOf(0)
    private var rowVisible by mutableStateOf(false)
    private var mainCloses by mutableStateOf(true)
    private var mainRes by mutableIntStateOf(AppR.string.retry)
    private var newUrlVisible by mutableStateOf(false)

    interface LoadImgListener {
        fun openNewUrl(url: String?)
        fun onImages(arrayList: ArrayList<String?>?, title: String?): Boolean
        fun onClose(isDone: Boolean)
    }

    init {
        setSheet {
            Box(Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(horizontal = 24.dp, vertical = 16.dp), contentAlignment = Alignment.Center) {
                if (loadingVisible) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(stringResource(titleRes), style = MaterialTheme.typography.bodyLarge)
                        if (indeterminate) {
                            LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 14.dp))
                        } else {
                            LinearProgressIndicator(
                                progress = { (progress / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                            )
                        }
                    }
                } else if (messageRes != 0) {
                    Text(stringResource(messageRes), style = MaterialTheme.typography.bodyLarge)
                }
            }
            if (rowVisible) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    if (newUrlVisible) {
                        TextButton(onClick = { loadListener?.openNewUrl(pageUrl) }) { Text(stringResource(AppR.string.new_url)) }
                    }
                    TextButton(onClick = { onMain() }) { Text(stringResource(mainRes)) }
                }
            }
            // indeterminate mode, asks the listener each frame whether to go on
            LaunchedEffect(indeterminate) {
                while (indeterminate) {
                    val i2 = retryStep
                    if (i2 in 1..2) retryStep = i2 + 1
                    if (loadState.toInt() == 2) {
                        indeterminate = false
                        fromList = false
                        break
                    }
                    delay(16)
                }
            }
        }
        ui.post { start() }
        show()
    }

    private fun start() {
        if (appContext == null) return
        // like the old sheet: no button row while the first load runs
        rowVisible = false
        mainCloses = true
        mainRes = AppR.string.retry
        if (URLUtil.isNetworkUrl(pageUrl)) {
            WebLoadTask.instance().taskListener = taskListener
            var loadModeNow = loadMode
            if (loadModeNow.toInt() == 0) {
                loadModeNow = WebLoadTask.instance().loadState
                loadMode = loadModeNow
            }
            newUrlMode = (showNewUrl || canNewUrl) && loadModeNow.toInt() == 1
            onProgress(-1)
            if (fromList) {
                loadingVisible = true
                indeterminate = true
            } else {
                showCount(0, true)
            }
        } else {
            loadMode = 2
            loadState = 2
            showResult(true, false, false)
        }
    }

    private fun onMain() {
        if (mainCloses) {
            dismiss()
            return
        }
        loadFailed = false
        loadState = 0
        waitStart = 0L
        mainBusy = false
        onProgress(-1)
        val webView = WebLoadTask.instance().loadWeb
        showCount(webView?.getProgress() ?: 0, true)
    }

    private val taskListener = object : WebLoadTaskListener {
        override fun onFailed() {
            loadMode = 2
            loadState = 2
            showResult(false, false, false)
        }

        override fun onLoading() {
            if (loadState.toInt() == 2) return
            loadState = 1
        }

        override fun onResult(title: String?, arrayList: ArrayList<String?>?, imgCntItem: ImgCntItem?) {
            var pageTitle = title
            if (appContext == null || loadState.toInt() == 2) return
            loadState = 0
            if (arrayList == null || arrayList.isEmpty()) {
                loadFailed = false
                if (retryStep != 4) {
                    val webView = WebLoadTask.instance().loadWeb
                    val progress = webView?.getProgress() ?: 0
                    this@DialogLoadImg.onProgress(progress)
                    val retryStepNow = retryStep
                    if (retryStepNow == 0) {
                        if (progress == 100) retryStep = 1
                    } else if (retryStepNow == 3) {
                        retryStep = 4
                    }
                    return
                }
            }
            loadState = 2
            if (arrayList == null || arrayList.isEmpty()) {
                when {
                    WebLoadTask.instance().netError -> showResult(false, true, false)
                    loadFailed -> showResult(false, false, true)
                    else -> showResult(false, false, false)
                }
                return
            }
            if (opening || hostActivity == null) return
            opening = true
            if (TextUtils.isEmpty(pageTitle)) {
                pageTitle = appContext?.getString(AppR.string.no_title) ?: "No title"
            }
            val listener = loadListener
            if (listener == null || !listener.onImages(arrayList, pageTitle)) {
                val data = DataUrl.instance(appContext)
                @Suppress("UNCHECKED_CAST")
                data.urls = arrayList as ArrayList<String?>?
                val intent = MainUtil.imageViewerIntent(appContext)
                intent.putExtra("EXTRA_TYPE", 12)
                intent.putExtra("EXTRA_NAME", pageTitle)
                intent.putExtra("EXTRA_INDEX", 0)
                intent.putExtra("EXTRA_REFERER", pageUrl)
                intent.putExtra("EXTRA_PRELOAD", true)
                hostActivity!!.startActivity(intent)
                loadListener?.onClose(false)
            }
        }

        override fun onProgress(progress: Int) {
            if (appContext == null) return
            pageProgress = progress
            loadMode = 1
            showCount(progress, false)
        }

        override fun onStopped() {
            if (appContext == null) return
            loadMode = 2
            if (loadState.toInt() == 2) return
            loadState = 0
            this@DialogLoadImg.onProgress(-1)
        }
    }

    /** Finished: [done] = not a network URL (Close), else error kind: [serverError] server error, [networkError] network, otherwise no image. */
    fun showResult(done: Boolean, serverError: Boolean, networkError: Boolean) {
        if (appContext == null) return
        loadState = 2
        loadingVisible = false
        indeterminate = false
        rowVisible = true
        if (done) {
            messageRes = AppR.string.no_image
            mainCloses = true
            mainRes = AppR.string.close
            return
        }
        messageRes = when {
            serverError -> AppR.string.server_error
            networkError -> AppR.string.check_network
            else -> AppR.string.no_image
        }
        mainCloses = false
        mainRes = AppR.string.retry
        if (showNewUrl) newUrlVisible = true
    }

    /** Progress tick of the page ([newCount] = page progress, -1 = (re)start). */
    fun onProgress(newCount: Int) {
        if (appContext == null || loadState.toInt() == 2) return
        if (newCount == -1) {
            val webView = WebLoadTask.instance().loadWeb
            pageProgress = webView?.getProgress() ?: 0
            lastCount = newCount
            retryStep = 0
        } else if (newCount != 100) {
            if (lastCount == newCount) {
                if (mainBusy) return
                val now = System.currentTimeMillis()
                if (waitStart == 0L) {
                    waitStart = now
                } else if (now - waitStart > 5000) {
                    mainBusy = true
                    titleRes = AppR.string.server_delay
                }
                return
            }
            lastCount = newCount
            waitStart = 0L
            if (newCount < 30) return
        }
        if (loadState.toInt() != 0) return
        if (loadMode.toInt() == 0) {
            val task = WebLoadTask.instance()
            task.netError = false
            val webView = task.loadWeb
            if (webView == null) {
                task.taskListener?.onFailed()
            } else {
                webView.loadUrl(pageUrl!!)
            }
        } else if (newUrlMode) {
            newUrlMode = false
        } else {
            loadState = 1
            ui.postDelayed({ if (appContext != null && loadState.toInt() != 2) WebLoadTask.instance().onPageDone(pageUrl) }, 200L)
        }
        if (!mainBusy) titleRes = AppR.string.loading
        messageRes = 0
        mainCloses = true
        mainRes = AppR.string.cancel
        newUrlVisible = false
    }

    /** Determinate progress: [done] = reset and show; otherwise step toward [newTickCount] and keep polling while shown. */
    fun showCount(newTickCount: Int, done: Boolean) {
        tickCount = newTickCount
        if (opening || appContext == null) return
        if (done) {
            loadingVisible = true
            indeterminate = false
            progress = 0f
            showCount(max(newTickCount, 50), false)
            return
        }
        var current = Math.round(progress)
        val retry = if (current == 100) retryStep else 0
        if (current == 100 && retry > 0 && retry < 3) {
            retryStep = retry + 1
            current = -1
        }
        onProgress(pageProgress)
        if (current < newTickCount) {
            val now = System.currentTimeMillis()
            if (current == -1 || now - lastTick > 1) progress = (current + 1).toFloat()
            lastTick = now
        }
        if (!alive || !loadingVisible || indeterminate) return
        ui.removeCallbacks(tick)
        ui.post(tick)
    }

    override fun dismiss() {
        alive = false
        ui.removeCallbacksAndMessages(null)
        val task = WebLoadTask.instance()
        task.netError = false
        task.imageTask?.cancelled = true
        task.imageTask = null
        task.started = false
        task.loadWeb = null
        task.taskListener = null
        task.loadState = 0
        task.netError = false
        hostActivity = null
        appContext = null
        loadListener = null
        pageUrl = null
        super.dismiss()
    }

    companion object {
    }
}
