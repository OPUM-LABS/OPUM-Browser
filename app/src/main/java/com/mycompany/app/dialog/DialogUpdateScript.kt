package com.mycompany.app.dialog

import com.mycompany.app.db.book.DbBookScript
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
import com.mycompany.app.data.book.DataBookJava.Companion.instance
import com.mycompany.app.data.book.DataBookPop.Companion.instance
import com.mycompany.app.data.book.DataBookScript
import com.mycompany.app.data.book.DataBookTmem.Companion.instance
import com.mycompany.app.data.book.DataBookTrans.Companion.instance
import com.mycompany.app.db.book.DbBookScript.Companion.saveScript
import com.mycompany.app.dialog.DialogEditUrl.EditUrlListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.externalVolumes
import com.mycompany.app.main.MainUri.isMediaUri
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.scriptNameFor
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.countLabel
import com.mycompany.app.main.MainUtil.pairText
import com.mycompany.app.main.MainUtil.sizeLabel
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.openBrowserConnection
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.main.MainUtil.readTextFile
import com.mycompany.app.script.Script
import com.mycompany.app.script.Script.Companion.parse
import com.mycompany.app.script.UnicodeReader
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.view.MyDialogBottom
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.util.Locale
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
 * Installs or updates user scripts ([list], or one [childItem], or a new one from [path]): for each script its name and
 * "n / total", the size downloaded with a progress bar, then the next; a failure shows the parser / install message
 * with Retry (and Skip with more scripts). Cancel stops the download ("Canceling"). Fetching ("userscript://" ids,
 * network with progress, local files), parsing (`Script.parse`) and saving (`DbBookScript`, `DataBookScript`) are the old
 * code; [EditUrlListener] hears the last saved id on close. Compose now (on [ComposeBottomDialog]); same constructor,
 * shows itself.
 */
@Suppress("deprecation")
class DialogUpdateScript(
    activity: Activity?,
    list: MutableList<Any?>?,
    childItem: ChildItem?,
    path: String?,
    editUrlListener: EditUrlListener?,
) : ComposeBottomDialog(activity) {
    @JvmField
    var scriptLocale: String? = null
    @JvmField
    var appContext: Context? = context
    @JvmField
    var editListener: EditUrlListener? = editUrlListener
    @JvmField
    var installFailed: Boolean = false
    @JvmField
    var scriptItems: MutableList<Any?>? = null
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
    var connection: HttpURLConnection? = null
    @JvmField
    var inStream: InputStream? = null
    @JvmField
    var reader: UnicodeReader? = null
    @JvmField
    var scriptId: Long = 0
    @JvmField
    var scriptLang: String? = null

    internal val ui = Handler(Looper.getMainLooper())
    private var nameText by mutableStateOf<String?>(null)
    private var countText by mutableStateOf("")
    internal var sizeText by mutableStateOf("")
    internal var progress by mutableFloatStateOf(0f)
    private var failText by mutableStateOf<String?>(null)
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
        failText = null
        skipShown = false
        cancelRequested = false
        itemRunning = false
        mainRes = AppR.string.cancel
        mainEnabled = true
        setCanceledOnTouchOutside(false)
    }

    internal fun failed(message: String, more: Boolean) {
        itemRunning = true
        failText = message
        skipShown = more
        mainRes = AppR.string.retry
        mainEnabled = true
        setCanceledOnTouchOutside(true)
    }

    internal inner class ProgressTickTask : Runnable {
        override fun run() {
            if (updateTask == null) return
            val doneBytesNow = doneBytes
            if (doneBytesNow == 0L) return
            if (totalBytes < doneBytesNow) totalBytes = doneBytesNow
            sizeText = MainUtil.pairText(MainUtil.sizeLabel(doneBytesNow), MainUtil.sizeLabel(totalBytes))
            progress = (doneBytes * 100.0f) / totalBytes
        }
    }

    internal inner class DisconnectTask(@JvmField val connection: HttpURLConnection) : Runnable {
        override fun run() { this.connection.disconnect() }
    }

    class DialogTask(dialogUpdateScript: DialogUpdateScript?) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var items: MutableList<Any?>? = null
        @JvmField
        var childItem: ChildItem? = null
        @JvmField
        var source: String? = null
        @JvmField
        var namespace: String? = null
        @JvmField
        var updated: Boolean = false
        @JvmField
        var done: Int = 0
        @JvmField
        var newVersion: String? = null

        inner class NextItemTask(@JvmField val outer: DialogUpdateScript) : Runnable {
            override fun run() {
                this.outer.nextItem()
            }
        }

        init {
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogUpdateScript)
            this.dialogRef = weakReference
            val d = weakReference.get() as DialogUpdateScript?
            if (d != null) {
                val list = d.scriptItems
                this.items = list
                val i = d.itemIndex
                if (list != null && i < list.size) {
                    val childItem = list.get(i) as ChildItem?
                    this.childItem = childItem
                    if (childItem != null) {
                        this.source = childItem.path
                        this.namespace = childItem.name
                        d.totalBytes = 0L
                        d.doneBytes = 0L
                        d.startItem(childItem.name, MainUtil.countLabel(i + 1, list.size))
                    }
                }
            }
        }

        /**
         * Fetches the script source h ("userscript://" ids are local-only and just finish; network urls are
         * downloaded with progress; anything else is read as a local uri), parses it with the app language and saves
         * it over the item. The result is success, an error string id, or the parser's error text.
         */
        public override fun doInBackground() {
            val dialog = this.dialogRef!!.get() as DialogUpdateScript?
            if (dialog == null || this.cancelled || this.childItem == null) {
                return
            }
            val source = this.source
            if (source!!.startsWith("userscript://")) {
                this.updated = true
                dialog.totalBytes = 1
                dialog.doneBytes = 1
                dialog.scriptId = this.childItem!!.id
                return
            }
            val context = dialog.appContext
            var sourceText: String? = null
            if (URLUtil.isNetworkUrl(source)) {
                dialog.closeStreams(true)
                if (context != null) {
                    dialog.connection = MainUtil.openBrowserConnection(context, source)
                    if (dialog.connection != null) {
                        try {
                            dialog.connection!!.setDoInput(true)
                            dialog.connection!!.connect()
                            dialog.totalBytes = dialog.connection!!.getContentLengthLong()
                            if (dialog.totalBytes < 0) {
                                dialog.totalBytes = 0
                            }
                            dialog.doneBytes = 0
                            dialog.inStream = dialog.connection!!.getInputStream()
                            dialog.reader = UnicodeReader(dialog.inStream, dialog.connection!!.getContentEncoding())
                            val chars = CharArray(4096)
                            var sb: StringBuilder? = null
                            var lastPost: Long = 0
                            while (!dialog.isCancelled()) {
                                val n = dialog.reader!!.reader!!.read(chars, 0, 4096)
                                if (n == -1 || dialog.isCancelled()) {
                                    break
                                }
                                if (n > 0) {
                                    if (sb == null) {
                                        sb = StringBuilder()
                                    }
                                    sb.append(chars, 0, n)
                                }
                                dialog.doneBytes += n.toLong()
                                val now = System.currentTimeMillis()
                                if (lastPost == 0L || now - lastPost > 500) {
                                    if (dialog.appContext == null) {
                                        break
                                    }
                                    dialog.ui.post(dialog.ProgressTickTask())
                                    lastPost = now
                                }
                            }
                            sourceText = if (sb == null) null else sb.toString()
                        } catch (exception: Exception) {
                            sourceText = null
                        }
                        dialog.closeStreams(true)
                    }
                }
                if (!TextUtils.isEmpty(sourceText)) {
                    this.updated = true
                    val size = max(dialog.totalBytes, dialog.doneBytes)
                    dialog.totalBytes = size
                    dialog.doneBytes = size
                }
            } else {
                try {
                    sourceText = if (TextUtils.isEmpty(source)) null else MainUtil.readTextFile(
                        context,
                        MainUtil.openUriInput(context!!, source),
                        false
                    )
                } catch (exception: Exception) {
                    sourceText = null
                }
                if (!TextUtils.isEmpty(sourceText)) {
                    this.updated = true
                    dialog.totalBytes = 1
                    dialog.doneBytes = 1
                } else {
                    this.done = AppR.string.invalid_path
                }
            }
            if (!this.updated) {
                return
            }
            if (TextUtils.isEmpty(dialog.scriptLang)) {
                val locale: Locale? = MainApp.currentLocale()
                if (locale != null) {
                    var language = locale.getLanguage()
                    if (!TextUtils.isEmpty(language)) {
                        language = language.lowercase()
                        val country = locale.getCountry()
                        dialog.scriptLang = language
                        dialog.scriptLocale =
                            if (TextUtils.isEmpty(country)) null else language + "-" + country.lowercase()
                    }
                }
            }
            val script = Script.parse(context, this.source, this.namespace, sourceText, dialog.scriptLang, dialog.scriptLocale, true)
            this.newVersion = Script.pendingText
            Script.pendingText = null
            if (script == null) {
                this.updated = false
                return
            }
            val saved = DbBookScript.saveScript(context, script, this.childItem, dialog.scriptLang, dialog.scriptLocale)
            if (saved == null) {
                this.updated = false
                return
            }
            val data = DataBookScript.instance(context)
            if (data.itemIds != null && data.itemIds!!.size > 0) {
                data.putItem(saved)
            }
            dialog.scriptId = saved.id
        }

        public override fun onCancelled() {
            val dialogUpdateScript = this.dialogRef!!.get() as DialogUpdateScript?
            if (dialogUpdateScript == null) {
                return
            }
            dialogUpdateScript.updateTask = null
            showToast(dialogUpdateScript.appContext, AppR.string.cancelled)
            dialogUpdateScript.dismiss()
        }

        public override fun onPostExecute() {
            val d = this.dialogRef!!.get() as DialogUpdateScript? ?: return
            d.updateTask = null
            if (d.isCancelled()) {
                showToast(d.appContext, AppR.string.cancelled)
                d.dismiss()
                return
            }
            val list = this.items
            if (list == null || list.isEmpty()) {
                d.dismiss()
                return
            }
            if (this.updated) {
                val total = MainUtil.sizeLabel(d.totalBytes)
                d.sizeText = MainUtil.pairText(total, total)
                d.progress = 100f
                d.ui.post { d.nextItem() }
                return
            }
            val message = when {
                this.done != 0 -> d.context.getString(this.done)
                !TextUtils.isEmpty(this.newVersion) -> this.newVersion!!
                d.installFailed -> d.context.getString(AppR.string.install_fail)
                else -> d.context.getString(AppR.string.update_fail)
            }
            d.failed(message, list.size > 1)
        }
    }
    init {
        var scriptItems = list
        var item = childItem
        if (scriptItems == null) {
            if (item == null && !TextUtils.isEmpty(path)) {
                item = ChildItem()
                item.path = path
                item.name = scriptNameFor(this.appContext, path)
                this.installFailed = true
            }
            if (item != null) {
                scriptItems = ArrayList<Any?>()
                scriptItems.add(item)
            }
        }
        this.scriptItems = scriptItems
        setSheet {
            Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp)) {
                Text(
                    nameText ?: "", maxLines = 2, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f),
                )
                Text(countText, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 16.dp))
            }
            val fail = failText
            if (fail != null) {
                Text(fail, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.fillMaxWidth().padding(24.dp))
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

    fun isCancelled(): Boolean {
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
        val list = this.scriptItems
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

    fun closeStreams(disconnect: Boolean) {
        val unicodeReader = this.reader
        if (unicodeReader != null) {
            try {
                unicodeReader.close()
            } catch (unused: Exception) {
            }
            this.reader = null
        }
        val inputStream = this.inStream
        if (inputStream != null) {
            try {
                inputStream.close()
            } catch (unused2: Exception) {
            }
            this.inStream = null
        }
        val httpURLConnection = this.connection
        if (disconnect) {
            if (httpURLConnection != null) {
                httpURLConnection.disconnect()
                this.connection = null
                return
            }
            return
        }
        this.connection = null
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
        val listener = editListener
        editListener = null
        if (listener != null && scriptId > 0) listener.onSaved(scriptId, null, null)
        appContext = null
        scriptItems = null
        super.dismiss()
    }

    companion object {
    }
}
