package com.mycompany.app.dialog

import android.os.Handler
import android.os.Looper
import android.webkit.URLUtil
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogLoadImg.LoadImgListener
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.web.WebNestView
import com.mycompany.app.web.WebViewActivity

/**
 * Shared Compose sheet of the two gallery-site loaders ([DialogLoadHmg], [DialogLoadEmg]): "Loading" (or "Server
 * delay" after 5 s without progress) with a progress bar while the site task runs; then a message (no image / server
 * error / check network) with Retry and, when [showNewUrl], "New URL". The state machine is the old one: [loadState] = 0 idle,
 * 1 working, 2 done; [onProgress] polls the task's progress every 400 ms and asks it for the next step once loading reached 30 %
 * or is complete. Subclasses own the task and its listener.
 */
abstract class SiteLoadDialog(
    webViewActivity: WebViewActivity?,
    webNestView: WebNestView?,
    pageUrlParam: String?,
    showNewUrlParam: Boolean,
    loadImgListener: LoadImgListener?,
) : ComposeBottomDialog(webViewActivity) {
    @JvmField
    var hostActivity: WebViewActivity? = webViewActivity
    @JvmField
    var appContext: android.content.Context? = context
    @JvmField
    var loadListener: LoadImgListener? = loadImgListener
    @JvmField
    var loadState: Byte = 0
    @JvmField
    var opening: Boolean = false
    @JvmField
    var webNest: WebNestView? = webNestView
    @JvmField
    var pageUrl: String? = pageUrlParam
    @JvmField
    var showNewUrl: Boolean = showNewUrlParam
    @JvmField
    var lastCount: Int = 0
    @JvmField
    var waitStart: Long = 0
    @JvmField
    var waitShown: Boolean = false
    @JvmField
    var progressMode: Boolean = false
    @JvmField
    var loadFailed: Boolean = false

    protected val ui = Handler(Looper.getMainLooper())

    private var titleRes by mutableIntStateOf(R.string.loading)
    private var loadingVisible by mutableStateOf(true)
    private var messageRes by mutableIntStateOf(0)
    private var rowVisible by mutableStateOf(false)
    /** true: the main button closes (Cancel / Close); false: it retries */
    private var mainCloses by mutableStateOf(true)
    private var mainRes by mutableIntStateOf(R.string.retry)
    private var newUrlVisible by mutableStateOf(false)

    /** Creates the site task (called once the subclass is constructed). */
    protected abstract fun createTask()

    /** The task's progress 0..100, or null when there is no task. */
    protected abstract fun taskProgress(): Int?

    /** Asks the task for its next step. */
    protected abstract fun taskStep()

    /** Stops and forgets the task. */
    protected abstract fun taskStop()

    init {
        setSheet {
            Box(Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(horizontal = 24.dp, vertical = 16.dp), contentAlignment = Alignment.Center) {
                if (loadingVisible) {
                    androidx.compose.foundation.layout.Column(Modifier.fillMaxWidth()) {
                        Text(stringResource(titleRes), style = MaterialTheme.typography.bodyLarge)
                        LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 14.dp))
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
                        TextButton(onClick = { loadListener?.openNewUrl(pageUrl) }) { Text(stringResource(R.string.new_url)) }
                    }
                    TextButton(onClick = { onMain() }) { Text(stringResource(mainRes)) }
                }
            }
        }
        // after construction: the subclass task fields are initialised by then
        ui.post { start() }
        show()
    }

    private fun start() {
        if (appContext == null) return
        if (URLUtil.isNetworkUrl(pageUrl)) {
            createTask()
            onProgress(-1)
            loadingVisible = true
        } else {
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
        waitShown = false
        onProgress(-1)
        loadingVisible = true
    }

    /** Finished: [done] = not a site URL (Close), else error kind: [serverError] server error, [networkError] network, otherwise no image. */
    fun showResult(done: Boolean, serverError: Boolean, networkError: Boolean) {
        if (appContext == null) return
        loadState = 2
        loadingVisible = false
        rowVisible = true
        if (done) {
            messageRes = R.string.no_image
            mainCloses = true
            mainRes = R.string.close
            return
        }
        messageRes = when {
            serverError -> R.string.server_error
            networkError -> R.string.check_network
            else -> R.string.no_image
        }
        mainCloses = false
        mainRes = R.string.retry
        if (showNewUrl) newUrlVisible = true
    }

    /** Progress tick ([newCount] = task progress, -1 = (re)start). */
    fun onProgress(newCount: Int) {
        val loadStateNow = loadState
        if (appContext == null || loadStateNow.toInt() == 2) return
        if (newCount == -1) {
            lastCount = newCount
            progressMode = true
        } else if (newCount != 100) {
            if (lastCount == newCount) {
                if (!waitShown) {
                    val now = System.currentTimeMillis()
                    if (waitStart == 0L) {
                        waitStart = now
                    } else if (now - waitStart > 5000) {
                        waitShown = true
                        titleRes = R.string.server_delay
                    }
                }
                ui.postDelayed({ taskProgress()?.let { onProgress(it) } }, 400L)
                return
            }
            lastCount = newCount
            waitStart = 0L
            if (newCount < 30) {
                ui.postDelayed({ taskProgress()?.let { onProgress(it) } }, 400L)
                return
            }
        }
        if (loadStateNow.toInt() != 0) return
        loadState = 1
        ui.postDelayed({ if (taskProgress() != null && loadState.toInt() != 2) taskStep() }, 200L)
        if (!waitShown) titleRes = R.string.loading
        messageRes = 0
        mainCloses = true
        mainRes = R.string.cancel
        newUrlVisible = false
    }

    override fun dismiss() {
        ui.removeCallbacksAndMessages(null)
        taskStop()
        hostActivity = null
        appContext = null
        loadListener = null
        webNest = null
        pageUrl = null
        super.dismiss()
    }
}
