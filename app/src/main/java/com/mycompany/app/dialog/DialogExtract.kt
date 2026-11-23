package com.mycompany.app.dialog

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.provider.DocumentsContract
import android.text.TextUtils
import android.view.View
import android.widget.ImageView.ScaleType
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.compress.Compress
import com.mycompany.app.compress.CompressUtil.CompressListener
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainListLoader.ListLoadListener
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUriDoc
import com.mycompany.app.main.MainUriVol
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.view.MyRoundImage
import java.lang.ref.WeakReference
import java.util.Locale

/**
 * Extract archives ([list], list type [itemType]) into folder [targetFolder] (album / PDF / archive list > Extract): one row per archive
 * with its icon and name and a "Sub folder" name field (unique within the folder: "name (2)"…); Extract runs them one
 * after another ([DialogTask], `Compress`): Current file + % with a bar, Total n / m with a bar, Fail, Estimated; then
 * per archive Total / Fail / Success. All OK closes with "Success", otherwise OK. Cancel / Canceling while working.
 * [DialogApplyListener] hears the start (to refresh the list). Compose now (on [ComposeBottomDialog]).
 */
@Suppress("deprecation")
class DialogExtract(
    mainListAlbum: android.app.Activity?,
    itemType: Int,
    list: MutableList<Any?>?,
    targetFolder: String?,
    dialogApplyListener: DialogApplyListener?,
) : ComposeBottomDialog(mainListAlbum) {
    /** Entries in the current archive / done / failed. */
    @JvmField
    var totalFiles: Int = 0
    @JvmField
    var doneFiles: Int = 0
    @JvmField
    var archiveIndex: Int = 0
    @JvmField
    var progressRow: Int = 0
    @JvmField
    var progressBase: Int = 0
    @JvmField
    var startTime: Long = 0
    @JvmField
    var currentEntry: String? = null
    @JvmField
    var rowsFolder: String? = null
    @JvmField
    var takenNames: ArrayList<Any?>? = null
    @JvmField
    var progressPosted: Boolean = false
    @JvmField
    var listLoader: MainListLoader? = null
    @JvmField
    var mainBusy: Boolean = false
    @JvmField
    var appContext: Context? = null
    @JvmField
    var itemType: Int = 0
    @JvmField
    var compressListener: CompressListener? = null
    @JvmField
    var items: MutableList<Any?>? = null
    @JvmField
    var targetFolder: String? = null
    @JvmField
    var applyListener: DialogApplyListener? = null
    @JvmField
    var targetNames: Array<String?>? = null
    @JvmField
    var extractTask: DialogTask? = null
    /** Archive being extracted. */
    @JvmField
    var rowIndex: Int = 0

    /** One archive row. */
    class Row(val name: String) {
        var icon by mutableStateOf<Bitmap?>(null)
        var title by mutableStateOf(name)
        var field by mutableStateOf("")
        var phase by mutableIntStateOf(0) // 0 name, 1 working, 2 result
        var current by mutableStateOf("")
        var currentPct by mutableStateOf("0.00%")
        var currentBar by mutableFloatStateOf(0f)
        var totalText by mutableStateOf("")
        var totalBar by mutableFloatStateOf(0f)
        var failText by mutableStateOf("0")
        var estText by mutableStateOf("")
        var rTotal by mutableStateOf("")
        var rFail by mutableStateOf("")
        var rOk by mutableStateOf("")
        var failed by mutableStateOf(false)
        var image: MyRoundImage? = null
    }

    private val rows = mutableStateListOf<Row>()
    private var loading by mutableStateOf(true)
    private var running by mutableStateOf(false)
    private var mainRes by mutableIntStateOf(AppR.string.extract)
    private var mainEnabled by mutableStateOf(true)
    private var finished = false

    private val ui = android.os.Handler(android.os.Looper.getMainLooper())

    init {
        compressListener = object : CompressListener {
            override fun onDone(path: String?, isOk: Boolean) {
                if (extractTask == null) return
                doneFiles++
                if (!isOk) archiveIndex++
                val index = rowIndex
                val name = MainUtil.parentPathOf(appContext, path)
                ui.post { showProgress(name, true, index) }
            }

            override fun onEntry(entryName: String?) {
                if (extractTask == null || TextUtils.isEmpty(entryName)) return
                val name = MainUtil.parentPathOf(appContext, entryName)
                ui.post {
                    val row = rows.getOrNull(rowIndex) ?: return@post
                    currentEntry = entryName
                    progressRow = rowIndex
                    progressBase = doneFiles
                    row.current = name ?: ""
                    row.currentPct = "0.00%"
                    row.currentBar = 0f
                }
            }

            override fun onProgress(entryName: String?, total: Long, done: Long) {
                if (extractTask == null || TextUtils.isEmpty(entryName)) {
                    progressPosted = false
                    return
                }
                if (progressPosted) return
                progressPosted = true
                ui.post {
                    val row0 = rows.getOrNull(rowIndex)
                    if (row0 == null) {
                        progressPosted = false
                        return@post
                    }
                    if (TextUtils.isEmpty(currentEntry) || currentEntry != entryName) {
                        currentEntry = entryName
                        progressRow = rowIndex
                        progressBase = doneFiles
                        row0.current = MainUtil.parentPathOf(appContext, entryName) ?: ""
                    }
                    var pct = if (total > 0) (done.toFloat() / total) * 100.0f else 0.0f
                    if (pct > 100.0f) pct = 100.0f
                    val row = rows.getOrNull(progressRow) ?: row0
                    row.currentPct = String.format(Locale.US, "%.2f", pct) + "%"
                    row.currentBar = pct / 100f
                    val filesDone = pct / 100.0f + progressBase
                    if (totalFiles > 0) row.totalBar = (filesDone / totalFiles).coerceIn(0f, 1f)
                    if (filesDone > 0f && totalFiles > 0) {
                        var left = (((totalFiles - filesDone) * (System.currentTimeMillis() - startTime)) / filesDone).toLong()
                        if (left in 1..999) left = 1000
                        row.estText = MainUtil.durationTextLong(left)
                    }
                    progressPosted = false
                }
            }

            override val isCancelled: Boolean
                get() = extractTask == null
        }
        if (!TextUtils.isEmpty(targetFolder) && list != null && !list.isEmpty()) {
            appContext = context
            this.itemType = itemType
            items = list
            this.targetFolder = targetFolder
            applyListener = dialogApplyListener
            targetNames = arrayOfNulls(list.size)
            listLoader = MainListLoader(context, false, object : ListLoadListener {
                override fun onFailed(childItem: ChildItem?, view: View?) {}
                override fun onLoaded(childItem: ChildItem?, view: View?, bitmap: Bitmap?) {
                    if (view !is MyRoundImage) return
                    if (childItem!!.itemType.toInt() == 4) view.setBackColor(-460552)
                    view.usePath(null, true)
                    view.setImageBitmap(bitmap)
                    rows.firstOrNull { it.image === view }?.let { refreshIcon(it) }
                }
            })
            setSheet {
                Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                    rows.forEachIndexed { index, row ->
                        if (index > 0) HorizontalDivider(Modifier.padding(horizontal = 24.dp))
                        RowView(row)
                    }
                }
                if (loading) {
                    LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp))
                }
                androidx.compose.foundation.layout.Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = { onMain() }, enabled = mainEnabled && !loading) { Text(stringResource(mainRes)) }
                }
            }
            show()
            runAsync(Runnable { buildRows() })
        }
    }

    @Composable
    private fun RowView(row: Row) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp)) {
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                val bmp = row.icon
                if (bmp != null) Image(bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.size(40.dp))
                Text(
                    row.title, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
            when (row.phase) {
                0 -> OutlinedTextField(
                    value = row.field,
                    onValueChange = { row.field = it },
                    label = { Text(stringResource(AppR.string.sub_dir)) },
                    singleLine = true,
                    enabled = !running,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                1 -> {
                    Line(stringResource(AppR.string.current), row.current)
                    Line("", row.currentPct)
                    LinearProgressIndicator(progress = { row.currentBar }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                    Line(stringResource(AppR.string.total), row.totalText)
                    LinearProgressIndicator(progress = { row.totalBar }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
                    Line(stringResource(AppR.string.fail), row.failText, red = row.failText != "0")
                    Line(stringResource(AppR.string.estimated), row.estText)
                }
                else -> {
                    Line(stringResource(AppR.string.total), row.rTotal)
                    Line(stringResource(AppR.string.fail), row.rFail, red = row.failed)
                    Line(stringResource(AppR.string.success), row.rOk)
                }
            }
        }
    }

    @Composable
    private fun Line(label: String, value: String, red: Boolean = false) {
        androidx.compose.foundation.layout.Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(
                value, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium,
                color = if (red) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
        }
    }

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

    /** Suggested sub-folder names, unique in the target folder (old naming rules). */
    private fun buildRows() {
        val list = items ?: return
        val names = ArrayList<String>()
        for (idx in list.indices) {
            val child = itemAt(idx)
            var name = MainUtil.safeFileName(MainUtil.stripExtension(child?.name)) ?: ""
            val folder = targetFolder
            if (!TextUtils.isEmpty(folder) && !TextUtils.isEmpty(name)) {
                if (folder != rowsFolder) {
                    rowsFolder = folder
                    val existing = listNames(appContext, folder)
                    takenNames = if (existing.isNullOrEmpty()) null else ArrayList<Any?>(existing.mapNotNull { (it as String?)?.lowercase() })
                }
                val taken = takenNames
                if (taken != null && taken.contains(name.lowercase())) {
                    var base = name
                    var n = 1
                    val open = name.lastIndexOf(" (")
                    if (name.endsWith(")") && open != -1 && open < name.length - 3) {
                        try {
                            n = name.substring(open + 2, name.length - 1).toInt()
                            base = name.substring(0, open)
                        } catch (unused: NumberFormatException) {
                            n = 1
                        }
                    }
                    var cand: String
                    do {
                        n++
                        cand = "$base ($n)"
                    } while (taken.contains(cand.lowercase()))
                    taken.add(cand.lowercase())
                    name = cand
                }
            }
            names.add(name)
        }
        ui.post {
            if (appContext == null) return@post
            for (idx in list.indices) {
                val child = itemAt(idx)
                val row = Row(child?.name ?: "")
                row.field = names[idx]
                targetNames!![idx] = names[idx]
                val size = MainApp.dp40
                val image = MyRoundImage(context)
                image.setScaleType(ScaleType.CENTER_CROP)
                image.setCircleRadius(size / 2.0f)
                image.measure(View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY))
                image.layout(0, 0, size, size)
                row.image = image
                rows.add(row)
                loadIcon(image, idx)
                refreshIcon(row)
            }
            loading = false
        }
    }

    private fun refreshIcon(row: Row) {
        row.icon = MainUtil.viewSnapshot(row.image, 1.0f)
    }

    private fun listNames(context: Context?, tree: String?): ArrayList<Any?>? {
        if (context == null || TextUtils.isEmpty(tree)) return null
        var cursor: Cursor? = null
        var names: ArrayList<Any?>? = null
        try {
            val uri = Uri.parse(tree)
            val children = DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri))
            cursor = context.getContentResolver().query(children, arrayOf<String?>("_display_name"), null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val col = cursor.getColumnIndex("_display_name")
                names = ArrayList<Any?>()
                do {
                    val name = cursor.getString(col)
                    if (!TextUtils.isEmpty(name)) names.add(name)
                } while (cursor.moveToNext())
            }
        } catch (unused: Exception) {
        }
        cursor?.close()
        return names
    }

    private fun onMain() {
        if (finished) {
            dismiss()
            return
        }
        if (extractTask != null) {
            cancelExtract()
            return
        }
        if (mainBusy) return
        mainBusy = true
        startExtract()
        mainBusy = false
    }

    fun itemAt(pos: Int): ChildItem? {
        val list = items ?: return null
        if (pos < 0 || pos >= list.size) return null
        val child = list[pos] as ChildItem? ?: return null
        return if (TextUtils.isEmpty(child.path)) null else child
    }

    /** Extract: checks the sub-folder names, then starts with the first archive. */
    fun startExtract() {
        val names = targetNames ?: return
        for (idx in rows.indices) {
            val name = rows[idx].field.trim { it <= ' ' }
            names[idx] = name
            if (TextUtils.isEmpty(name)) {
                showToast(appContext, AppR.string.input_name)
                return
            }
        }
        applyListener?.onApply()
        running = true
        rowIndex = 0
        val child = itemAt(0) ?: return
        extractTask?.cancelled = true
        extractTask = null
        ui.post { start(child.path, child.name) }
    }

    private fun start(path: String?, title: String?) {
        val task = DialogTask(this, path, title)
        extractTask = task
        task.start(appContext)
    }

    /** Cancel: stops ("Canceling"), or closes. */
    fun cancelExtract() {
        if (extractTask == null) {
            dismiss()
            return
        }
        mainEnabled = false
        mainRes = AppR.string.canceling
        extractTask?.cancelled = true
        extractTask = null
    }

    /** Progress of archive [index]: [done] = an entry finished ([str] its name). */
    fun showProgress(current: String?, done: Boolean, index: Int) {
        val row = rows.getOrNull(index) ?: return
        if (done) {
            row.current = current ?: ""
            row.currentPct = "100.00%"
            row.currentBar = 1f
        }
        row.totalText = "$doneFiles / $totalFiles"
        if (totalFiles > 0) row.totalBar = (doneFiles.toFloat() / totalFiles).coerceIn(0f, 1f)
        if (doneFiles > 0 && totalFiles > 0) {
            var left = (((totalFiles - doneFiles).toFloat() * (System.currentTimeMillis() - startTime)) / doneFiles).toLong()
            if (left in 1..999) left = 1000
            row.estText = MainUtil.durationTextLong(left)
        }
        row.failText = archiveIndex.toString()
    }

    /** Icon of archive [idx] (old rules: thumbnail via the list loader, else its type icon). */
    fun loadIcon(myRoundImage: MyRoundImage?, idx: Int) {
        if (myRoundImage == null) return
        val list = items
        if (list == null || idx < 0 || idx >= list.size) {
            myRoundImage.showIcon(-460552, AppR.drawable.outline_local_library_black_24)
            return
        }
        var child = list[idx] as ChildItem?
        if (child == null) {
            myRoundImage.showIcon(-460552, AppR.drawable.outline_local_library_black_24)
            return
        }
        val itemTypeNow = child.itemType
        if (itemTypeNow.toInt() != 1 && itemTypeNow.toInt() != 2 && itemTypeNow.toInt() != 3 && itemTypeNow.toInt() != 4 && itemTypeNow.toInt() != 5 && itemTypeNow.toInt() != 6 && itemTypeNow.toInt() != 11) {
            myRoundImage.showIcon(child.iconColor, child.iconRes)
            return
        }
        if (itemTypeNow.toInt() == 11) {
            val copy = ChildItem()
            copy.listType = itemType
            copy.itemType = itemTypeNow
            copy.path = child.filePath
            copy.filePath = child.filePath
            copy.id = child.id
            copy.position = child.position
            copy.iconColor = child.iconColor
            copy.iconRes = child.iconRes
            child = copy
        }
        if (TextUtils.isEmpty(child.path)) {
            myRoundImage.showIcon(child.iconColor, child.iconRes)
            return
        }
        val cached = MainListLoader.cachedIconFor(child)
        if (MainUtil.isBitmapOk(cached)) {
            if (child.itemType.toInt() == 4) myRoundImage.setBackColor(-460552)
            myRoundImage.setImageBitmap(cached)
        } else {
            val loader = listLoader ?: return
            myRoundImage.showIcon(child.iconColor, child.iconRes)
            myRoundImage.setTag(child.position)
            loader.enqueue(child, myRoundImage)
        }
    }

    internal fun working(index: Int) {
        val row = rows.getOrNull(index) ?: return
        row.phase = 1
        row.totalText = "0 / 0"
        row.totalBar = 0f
        row.currentBar = 0f
        row.failText = "0"
        row.estText = ""
        mainRes = AppR.string.cancel
        mainEnabled = true
        setCanceledOnTouchOutside(false)
    }

    internal fun result(index: Int, title: String?, total: Int, fail: Int, ok: Int) {
        val row = rows.getOrNull(index) ?: return
        if (title != null) row.title = title
        row.rTotal = total.toString()
        row.rFail = fail.toString()
        row.rOk = ok.toString()
        row.failed = fail > 0
        row.phase = 2
    }

    internal fun done() {
        running = false
        finished = true
        mainRes = AppR.string.ok
        mainEnabled = true
        setCanceledOnTouchOutside(true)
    }

    override fun cancel() {
        cancelExtract()
    }

    override fun canCancelFromScrim(): Boolean = extractTask == null

    override fun dismiss() {
        if (appContext == null) {
            super.dismiss()
            return
        }
        extractTask?.cancelled = true
        extractTask = null
        listLoader?.release()
        listLoader = null
        rows.forEach { it.image?.release(); it.image = null }
        appContext = null
        applyListener = null
        items = null
        super.dismiss()
    }

    class DialogTask(dialogExtract: DialogExtract?, path: String?, image: String?) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = WeakReference<Any?>(dialogExtract)
        @JvmField
        var path: String? = path
        @JvmField
        var image: String? = image
        @JvmField
        var compressor: Compress? = null
        @JvmField
        var withData: Boolean = false

        init {
            val d = dialogRef!!.get() as DialogExtract?
            if (d != null) {
                d.totalFiles = 0
                d.doneFiles = 0
                d.archiveIndex = 0
                d.startTime = System.currentTimeMillis()
                d.currentEntry = null
                d.working(d.rowIndex)
            }
        }

        public override fun doInBackground() {
            val d = dialogRef!!.get() as DialogExtract?
            if (d == null || cancelled) return
            val path = path
            if (TextUtils.isEmpty(path) || TextUtils.isEmpty(d.targetFolder)) return
            val compress = Compress.create(d.appContext, 3, path, null)
            compressor = compress
            compress.charsetName = MainConst.defaultCharset
            compress.password = "debug_logger_tag"
            if (!compress.open()) {
                d.archiveIndex = 1
                return
            }
            val count = compressor!!.entryCount()
            d.totalFiles = count
            if (count == 0) {
                d.archiveIndex = 1
                return
            }
            publishProgress()
            val ctx = d.appContext
            val dir = d.targetFolder
            val sub = d.targetNames!![d.rowIndex]
            val target = if (MainUri.isContentUri(dir)) MainUriDoc.findChild(ctx, dir, sub) else MainUriVol.findMediaUri(ctx, dir, sub)
            if (TextUtils.isEmpty(target)) return
            withData = compressor!!.extractTo(target, d.compressListener)
        }

        public override fun onCancelled() {
            val d = dialogRef!!.get() as DialogExtract? ?: return
            d.extractTask = null
            compressor?.release()
            compressor = null
            d.dismiss()
        }

        public override fun onPostExecute() {
            val d = dialogRef!!.get() as DialogExtract? ?: return
            d.extractTask = null
            compressor?.release()
            compressor = null
            if (cancelled) {
                d.dismiss()
                return
            }
            val index = d.rowIndex
            val total = d.totalFiles
            if (total == 0) {
                d.result(index, image, 1, 1, 0)
            } else {
                if (!withData) {
                    if (d.doneFiles > total) d.doneFiles = total
                    d.archiveIndex = (total - d.doneFiles) + d.archiveIndex
                }
                if (d.archiveIndex > total) d.archiveIndex = total
                d.result(index, image, total, d.archiveIndex, total - d.archiveIndex)
            }
            val next = index + 1
            d.rowIndex = next
            val child = d.itemAt(next)
            if (child != null && !TextUtils.isEmpty(child.path)) {
                d.ui.post { d.start(child.path, child.name) }
                return
            }
            if (d.archiveIndex == 0) {
                showToast(d.appContext, AppR.string.success)
                d.dismiss()
                return
            }
            d.done()
        }

        public override fun onProgressUpdate() {
            val d = dialogRef!!.get() as DialogExtract?
            if (d == null || cancelled) return
            d.showProgress(null, false, d.rowIndex)
        }
    }

    companion object {
    }
}
