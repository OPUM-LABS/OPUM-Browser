package com.mycompany.app.dialog

import com.mycompany.app.main.MainUtil
import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.webkit.URLUtil
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.data.book.DataBookAds.Companion.instance
import com.mycompany.app.data.book.DataBookFilter
import com.mycompany.app.data.book.DataBookJava.Companion.instance
import com.mycompany.app.data.book.DataBookPop.Companion.instance
import com.mycompany.app.data.book.DataBookTmem.Companion.instance
import com.mycompany.app.data.book.DataBookTrans.Companion.instance
import com.mycompany.app.db.book.DbBookFilter
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainFilterDown
import com.mycompany.app.main.MainFilterDown.Hooks
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.externalVolumes
import com.mycompany.app.main.MainUri.isMediaUri
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.filterFilePath
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.countLabel
import com.mycompany.app.main.MainUtil.pairText
import com.mycompany.app.main.MainUtil.sizeLabel
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUtil.copyFileOrUri
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.view.MyDialogBottom
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import kotlin.math.max

import com.mycompany.app.ui.browser.ComposeBottomDialog
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Updates filter lists ([list], or the single [path] / [name] one): for each list the name and "n / total", the size
 * downloaded / total with a progress bar, then the next; a failed list shows "Update failed" with Retry and, with more
 * lists, Skip. Cancel stops the download ("Canceling"). The download ([downloadFilter] via `MainFilterDown`, stream handling in
 * [closeStreams]), the database update and the built-in filter refresh are the old code; [DialogApplyListener] hears it on close
 * when something was updated. Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogUpdateFilter(
    activity: Activity?,
    list: MutableList<Any?>?,
    path: String?,
    name: String?,
    dialogApplyListener: DialogApplyListener?,
) : ComposeBottomDialog(activity) {
    @JvmField
    var appContext: Context? = context
    @JvmField
    var applyListener: DialogApplyListener? = dialogApplyListener
    @JvmField
    var filterItems: MutableList<Any?>? = null
    @JvmField
    var totalBytes: Long = 0
    @JvmField
    var doneBytes: Long = 0
    @JvmField
    var updateTask: DialogTask? = null
    @JvmField
    var itemIndex: Int = 0
    @JvmField
    var itemRunning: Boolean = false
    @JvmField
    var mainBusy: Boolean = false
    @JvmField
    var cancelRequested: Boolean = false
    @JvmField
    var changed: Boolean = false
    @JvmField
    var httpConn: HttpURLConnection? = null
    @JvmField
    var inStream: InputStream? = null
    @JvmField
    var outStream: OutputStream? = null
    @JvmField
    var keepOut: Boolean = false

    internal val ui = Handler(Looper.getMainLooper())
    private var nameText by mutableStateOf<String?>(null)
    private var countText by mutableStateOf("")
    internal var sizeText by mutableStateOf("")
    internal var progress by mutableFloatStateOf(0f)
    private var failedShown by mutableStateOf(false)
    private var skipShown by mutableStateOf(false)
    private var mainRes by mutableIntStateOf(AppR.string.cancel)
    private var mainEnabled by mutableStateOf(true)

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

    internal fun startItem(name: String?, count: String) {
        nameText = name
        countText = count
        sizeText = ""
        progress = 0f
        failedShown = false
        skipShown = false
        cancelRequested = false
        itemRunning = false
        mainRes = AppR.string.cancel
        mainEnabled = true
        setCanceledOnTouchOutside(false)
    }

    internal fun failed(more: Boolean) {
        itemRunning = true
        failedShown = true
        skipShown = more
        mainRes = AppR.string.retry
        mainEnabled = true
        setCanceledOnTouchOutside(true)
    }

    private fun progressTick() {
        if (updateTask == null) return
        val doneBytesNow = doneBytes
        if (doneBytesNow == 0L) return
        if (totalBytes < doneBytesNow) totalBytes = doneBytesNow
        sizeText = MainUtil.pairText(MainUtil.sizeLabel(doneBytesNow), MainUtil.sizeLabel(totalBytes))
        progress = (doneBytes * 100.0f) / totalBytes
    }

    internal inner class ProgressTickTask : Runnable {
        override fun run() { progressTick() }
    }

    internal inner class DisconnectTask(@JvmField val connection: HttpURLConnection) : Runnable {
        override fun run() { this.connection.disconnect() }
    }

    class DialogTask(dialogUpdateFilter: DialogUpdateFilter?) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var items: MutableList<Any?>? = null
        @JvmField
        var path: String? = null
        @JvmField
        var title: String? = null
        @JvmField
        var withData: Boolean = false

        inner class NextItemTask(@JvmField val outer: DialogUpdateFilter) : Runnable {
            override fun run() {
                this.outer.nextItem()
            }
        }

        init {
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogUpdateFilter)
            this.dialogRef = weakReference
            val d = weakReference.get() as DialogUpdateFilter?
            if (d != null) {
                val list = d.filterItems
                this.items = list
                val i = d.itemIndex
                val childItem: ChildItem? = if (list == null || i >= list.size) null else (list.get(i) as ChildItem?)
                if (childItem != null) {
                    this.path = childItem.path
                    this.title = childItem.name
                    d.totalBytes = 0L
                    d.doneBytes = 0L
                    d.startItem(childItem.name, MainUtil.countLabel(i + 1, list!!.size))
                }
            }
        }

        public override fun doInBackground() {
            val dialogUpdateFilter = this.dialogRef!!.get() as DialogUpdateFilter?
            val list: MutableList<Any?>? = if (dialogUpdateFilter == null || this.cancelled) null else this.items
            if (dialogUpdateFilter == null || this.cancelled || list == null || list.isEmpty()) {
                return
            }
            val path = this.path
            if (URLUtil.isNetworkUrl(path)) {
                val downloaded = dialogUpdateFilter.downloadFilter(dialogUpdateFilter.appContext, path, path!!)
                this.withData = downloaded
                if (downloaded) {
                    val total = max(dialogUpdateFilter.totalBytes, dialogUpdateFilter.doneBytes)
                    dialogUpdateFilter.totalBytes = total
                    dialogUpdateFilter.doneBytes = total
                }
            } else {
                this.withData = true
                dialogUpdateFilter.totalBytes = 1L
                dialogUpdateFilter.doneBytes = 1L
            }
            if (this.withData) {
                val childItemF = DbBookFilter.addPathItem(dialogUpdateFilter.appContext, path, this.title)
                val dataBookFilterK = DataBookFilter.instance(dialogUpdateFilter.appContext)
                val itemIds: MutableList<Long>? = dataBookFilterK.itemIds
                if (itemIds != null && itemIds.size > 0) {
                    dataBookFilterK.putItem(childItemF)
                }
                dialogUpdateFilter.changed = true
            }
        }

        public override fun onCancelled() {
            val dialogUpdateFilter = this.dialogRef!!.get() as DialogUpdateFilter?
            if (dialogUpdateFilter == null) {
                return
            }
            dialogUpdateFilter.updateTask = null
            showToast(dialogUpdateFilter.appContext, AppR.string.cancelled)
            dialogUpdateFilter.dismiss()
        }

        public override fun onPostExecute() {
            val d = this.dialogRef!!.get() as DialogUpdateFilter? ?: return
            d.updateTask = null
            if (d.cancelled()) {
                showToast(d.appContext, AppR.string.cancelled)
                d.dismiss()
                return
            }
            val list = this.items
            if (list == null || list.isEmpty()) {
                d.dismiss()
                return
            }
            if (this.withData) {
                val total = MainUtil.sizeLabel(d.totalBytes)
                d.sizeText = MainUtil.pairText(total, total)
                d.progress = 100f
                d.ui.post { d.nextItem() }
                return
            }
            d.failed(list.size > 1)
        }
    }
    init {
        var filterItems = list
        if (!TextUtils.isEmpty(path)) {
            val childItem = ChildItem()
            childItem.path = path
            childItem.name = name
            val arrayList: ArrayList<Any?> = ArrayList<Any?>()
            arrayList.add(childItem)
            filterItems = arrayList
        }
        this.filterItems = filterItems
        setSheet {
            Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp)) {
                Text(
                    nameText ?: "", maxLines = 2, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f),
                )
                Text(countText, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 16.dp))
            }
            if (failedShown) {
                Text(
                    stringResource(AppR.string.update_fail),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                )
            } else {
                Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp)) {
                    Text(stringResource(AppR.string.size), modifier = Modifier.weight(1f))
                    Text(sizeText)
                }
                LinearProgressIndicator(
                    progress = { (progress / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                )
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                if (skipShown) {
                    TextButton(onClick = { if (!mainBusy) { mainBusy = true; nextItem(); mainBusy = false } }) { Text(stringResource(AppR.string.skip)) }
                }
                TextButton(onClick = { onMain() }, enabled = mainEnabled) { Text(stringResource(mainRes)) }
            }
        }
        show()
        ui.post {
            if (appContext != null) {
                val task = DialogTask(this)
                updateTask = task
                task.start(appContext)
            }
        }
    }

    private fun onMain() {
        if (mainBusy) return
        mainBusy = true
        if (itemRunning) {
            itemRunning = false
            updateTask?.cancelled = true
            updateTask = null
            val task = DialogTask(this)
            updateTask = task
            task.start(appContext)
        } else {
            cancelUpdate()
        }
        mainBusy = false
    }

    fun cancelled(): Boolean {
        if (this.cancelRequested) {
            return true
        }
        val dialogTask = this.updateTask
        return dialogTask != null && dialogTask.cancelled
    }

    /** Cancel: stops the running download ("Canceling"), or closes. */
    fun cancelUpdate() {
        if (updateTask == null) {
            dismiss()
            return
        }
        mainEnabled = false
        mainRes = AppR.string.canceling
        cancelRequested = true
        closeStreams(false)
        updateTask?.cancelled = true
        updateTask = null
    }

    fun nextItem() {
        if (this.appContext == null) {
            return
        }
        val itemIndex = this.itemIndex + 1
        this.itemIndex = itemIndex
        val list = this.filterItems
        if (list == null || itemIndex >= list.size) {
            showToast(this.appContext, AppR.string.success)
            dismiss()
            return
        }
        val dialogTask = this.updateTask
        if (dialogTask != null) {
            dialogTask.cancelled = true
        }
        this.updateTask = null
        val updateTask = DialogTask(this)
        this.updateTask = updateTask
        updateTask.start(this.appContext)
    }

    /** Downloads filter list `str` for list `str2`, showing progress and honoring Cancel.  */
    fun downloadFilter(context: Context?, url: String?, key: String?): Boolean {
        closeStreams(true)
        val ok = MainFilterDown(null).downloadWith(context, url, key, object : Hooks {
            private var lastPost: Long = 0

            override val isCancelled: Boolean
                get() = cancelled()

            override fun onStreams(
                connection: HttpURLConnection?,
                `in`: InputStream?,
                out: OutputStream?,
                total: Long
            ) {
                httpConn = connection
                inStream = `in`
                outStream = out
                totalBytes = total
                doneBytes = 0L
            }

            override fun onProgress(done: Long, total: Long) {
                doneBytes = done
                val now = System.currentTimeMillis()
                if (lastPost == 0L || now - lastPost > 500) {
                    ui.post(ProgressTickTask())
                    lastPost = now
                }
            }

            override fun onStreamsDone(keepOutput: Boolean) {
                keepOut = keepOutput
                closeStreams(true)
            }
        })
        closeStreams(true)
        return ok
    }

    fun closeStreams(disconnect: Boolean) {
        val outputStream: OutputStream? = if (!this.keepOut) this.outStream else null
        if (!this.keepOut && outputStream != null) {
            try {
                outputStream.close()
            } catch (unused: Exception) {
            }
            this.outStream = null
        }
        val inputStream = this.inStream
        if (inputStream != null) {
            try {
                inputStream.close()
            } catch (unused2: Exception) {
            }
            this.inStream = null
        }
        val httpURLConnection = this.httpConn
        if (disconnect) {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect()
                this.httpConn = null
                return
            }
            return
        }
        this.httpConn = null
        if (httpURLConnection == null) {
            return
        }
        runAsync(DisconnectTask(httpURLConnection))
    }

    override fun cancel() {
        cancelUpdate()
    }

    override fun canCancelFromScrim(): Boolean = itemRunning || updateTask == null

    override fun dismiss() {
        if (appContext == null) {
            super.dismiss()
            return
        }
        closeStreams(false)
        updateTask?.cancelled = true
        updateTask = null
        val listener = applyListener
        applyListener = null
        if (listener != null && changed) listener.onApply()
        appContext = null
        filterItems = null
        super.dismiss()
    }

    companion object {
    }
}
