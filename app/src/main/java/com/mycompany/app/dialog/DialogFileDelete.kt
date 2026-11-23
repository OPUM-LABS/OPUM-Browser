package com.mycompany.app.dialog

import com.mycompany.app.main.ViewItemListener
import com.mycompany.app.main.JarBridge
import com.mycompany.app.main.MainItem
import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.data.DataUtil.removePath
import com.mycompany.app.db.DbUtil.updateById
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainItem.ViewItem
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.externalVolumes
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.isReadableFile
import com.mycompany.app.main.MainUtil.countLabel
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.durationTextLong
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.pref.PrefSet.remove
import ch.opumlabs.opumbrowser.R

import java.lang.ref.WeakReference

import com.mycompany.app.ui.browser.ComposeBottomDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Deletes files (download list [listType] = 31, album / PDF / ZIP / other lists): header with the file's thumbnail or type
 * icon and name (or "n items"), Delete; while deleting: Total n / m with a progress bar, failures and the estimated
 * time left, Cancel / Canceling; with failures afterwards: Total / Fail / Success and OK. The delete loop ([DialogTask],
 * one file after another, list data updated) is the old code; [FileDeleteListener] hears the start and the result.
 * Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogFileDelete(
    activity: Activity?,
    listType: Int,
    list: MutableList<Any?>?,
    fileDeleteListener: FileDeleteListener?,
) : ComposeBottomDialog(activity) {
    @JvmField
    var failCount: Int = 0
    @JvmField
    var totalBytes: Long = 0
    @JvmField
    var startTime: Long = 0
    @JvmField
    var headPath: String? = null
    @JvmField
    var lastPath: String? = null
    @JvmField
    var headItem: ChildItem? = null
    @JvmField
    var headFixed: Boolean = false
    @JvmField
    var mainBusy: Boolean = false
    @JvmField
    var appContext: Context? = null
    @JvmField
    var listType: Int = listType
    @JvmField
    var deleteItems: MutableList<Any?>? = null
    @JvmField
    var headColor: Int = -460552
    @JvmField
    var headIconRes: Int = when (listType) {
        1 -> AppR.drawable.outline_local_library_black_24
        2 -> AppR.drawable.outline_picture_as_pdf_black_24
        3 -> AppR.drawable.outline_folder_zip_black_24
        else -> AppR.drawable.outline_draft_black_24
    }
    @JvmField
    var deleteListener: FileDeleteListener? = null
    @JvmField
    var deleteTask: DialogTask? = null
    @JvmField
    var msgHandler: EventHandler? = null
    @JvmField
    var totalCount: Int = 0
    @JvmField
    var doneCount: Int = 0

    private var headIcon by mutableIntStateOf(0)
    private var headBitmap by mutableStateOf<Bitmap?>(null)
    private var headName by mutableStateOf("")
    private var running by mutableStateOf(false)
    private var resultShown by mutableStateOf(false)
    private var countText by mutableStateOf("")
    private var progress by mutableFloatStateOf(0f)
    private var progressMax by mutableIntStateOf(1)
    private var failText by mutableStateOf("0")
    private var failRed by mutableStateOf(false)
    internal var etaText by mutableStateOf("0:00:00")
    private var resTotal by mutableStateOf("")
    private var resFail by mutableStateOf("")
    private var resOk by mutableStateOf("")
    private var mainRes by mutableIntStateOf(AppR.string.delete)
    private var mainEnabled by mutableStateOf(true)

    private class CopyInfo {
        var path: String? = null
        var name: String? = null
    }

    class DialogTask(dialogFileDelete: DialogFileDelete?) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>
        @JvmField
        var deleteItems: MutableList<Any?>? = null

        init {
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogFileDelete)
            this.dialogRef = weakReference
            val dialogFileDelete = weakReference.get() as DialogFileDelete?
            if (dialogFileDelete != null) {
                this.deleteItems = dialogFileDelete.deleteItems
                dialogFileDelete.msgHandler = EventHandler(dialogFileDelete)
                dialogFileDelete.totalCount = 0
                dialogFileDelete.doneCount = 0
                dialogFileDelete.failCount = 0
                dialogFileDelete.totalBytes = 0L
                dialogFileDelete.startTime = System.currentTimeMillis()
                dialogFileDelete.lastPath = null
                val fileDeleteListener = dialogFileDelete.deleteListener
                if (fileDeleteListener != null) {
                    fileDeleteListener.onCancelled()
                }
            }
        }

        /**
         * Deletes the selected files one by one (a file that no longer exists counts as deleted), removing them from
         * the list data and reporting progress (name: message 0, counts: message 1; failures are counted).
         */
        public override fun doInBackground() {
            val dialog = this.dialogRef.get() as DialogFileDelete?
            if (dialog == null || this.cancelled || this.deleteItems == null || this.deleteItems!!.size == 0) {
                return
            }
            this.deleteItems = ArrayList<Any?>(this.deleteItems!!)
            dialog.totalCount = this.deleteItems!!.size
            dialog.totalBytes = this.deleteItems!!.size.toLong()
            if (dialog.msgHandler != null) {
                dialog.msgHandler!!.sendEmptyMessage(1)
            }
            for (item2 in this.deleteItems!!) {
                val item = item2 as ChildItem
                if (this.cancelled) {
                    return
                }
                if (TextUtils.isEmpty(item.path)) {
                    continue
                }
                if (dialog.msgHandler != null) {
                    val info = CopyInfo()
                    info.path = item.path
                    info.name = item.name
                    val message = Message()
                    message.what = 0
                    message.obj = info
                    dialog.msgHandler!!.sendMessage(message)
                }
                val deleted = MainUtil.deleteUri(dialog.appContext!!, item.path) || !MainUtil.isReadableFile(dialog.appContext, item.path)
                if (deleted) {
                    removePath(dialog.appContext, dialog.listType, item.path)
                }
                if (dialog.msgHandler != null) {
                    dialog.msgHandler!!.removeMessages(1)
                    dialog.doneCount++
                    if (!deleted) {
                        dialog.failCount++
                    }
                    dialog.msgHandler!!.sendEmptyMessage(1)
                }
            }
        }

        public override fun onCancelled() {
            val dialogFileDelete = this.dialogRef.get() as DialogFileDelete?
            if (dialogFileDelete != null) {
                dialogFileDelete.deleteTask = null
                val eventHandler = dialogFileDelete.msgHandler
                if (eventHandler != null) {
                    eventHandler.removeMessages(0)
                    dialogFileDelete.msgHandler!!.removeMessages(1)
                    dialogFileDelete.msgHandler = null
                }
                val fileDeleteListener = dialogFileDelete.deleteListener
                if (fileDeleteListener != null) {
                    fileDeleteListener.onDeleted(true)
                }
            }
        }

        public override fun onPostExecute() {
            val d = this.dialogRef.get() as DialogFileDelete? ?: return
            d.deleteTask = null
            d.msgHandler?.removeMessages(0)
            d.msgHandler?.removeMessages(1)
            d.msgHandler = null
            d.etaText = "0:00:00"
            val ok = d.failCount == 0
            d.deleteListener?.onDeleted(ok)
            if (ok) {
                showToast(d.appContext, AppR.string.deleted)
                return
            }
            if (d.appContext != null) d.showResult()
        }
    }

    class EventHandler(dialogFileDelete: DialogFileDelete?) : Handler(Looper.getMainLooper()) {
        @JvmField
        var dialogRef: WeakReference<*> = WeakReference<Any?>(dialogFileDelete)

        override fun handleMessage(message: Message) {
            val d = this.dialogRef.get() as DialogFileDelete? ?: return
            if (d.deleteTask == null) return
            if (message.what == 0) {
                val info = message.obj as? CopyInfo ?: return
                val path = info.path
                if (TextUtils.isEmpty(path)) return
                if (path != d.lastPath) {
                    d.lastPath = path
                    if (!d.headFixed && path != d.headPath) d.setHead(d.headIconRes, null, info.name)
                }
                d.tick()
                return
            }
            if (message.what != 1) return
            if (d.doneCount > d.totalCount) d.doneCount = d.totalCount
            if (d.failCount > d.totalCount) d.failCount = d.totalCount
            d.countText = MainUtil.countLabel(d.doneCount, d.totalCount)
            d.progressMax = d.totalCount
            d.tick()
            d.failText = d.failCount.toString()
            d.failRed = d.failCount > 0
            d.running = true
            d.setCanceledOnTouchOutside(false)
        }
    }

    internal fun tick() {
        progress = doneCount.toFloat()
        if (doneCount > 0) {
            val elapsed = System.currentTimeMillis() - startTime
            val left = ((totalBytes - doneCount.toLong()) * elapsed) / doneCount.toLong()
            etaText = MainUtil.durationTextLong(if (left <= 0 || left >= 1000) left else 1000L)
        }
    }

    internal fun setHead(icon: Int, bitmap: Bitmap?, name: String?) {
        headIcon = icon
        headBitmap = bitmap
        headName = name ?: ""
    }

    internal fun showResult() {
        var fail = failCount
        if (fail > totalCount) {
            failCount = totalCount
            fail = totalCount
        }
        doneCount = totalCount - fail
        resTotal = totalCount.toString()
        resFail = failCount.toString()
        resOk = doneCount.toString()
        running = false
        resultShown = true
        mainRes = AppR.string.ok
        mainEnabled = true
        setCanceledOnTouchOutside(true)
    }

    interface FileDeleteListener {
        fun onCancelled()
        fun onDeleted(isOk: Boolean)
    }

    init {
        if (list != null && !list.isEmpty()) {
            appContext = context
            deleteItems = list
            deleteListener = fileDeleteListener
            setupHead(list)
            countText = MainUtil.countLabel(0, list.size)
            progressMax = list.size
        }
        setSheet {
            Row(
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val bmp = headBitmap
                val mod = Modifier.size(40.dp).clip(CircleShape)
                if (bmp != null) {
                    Image(bmp.asImageBitmap(), contentDescription = null, modifier = mod, contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                } else if (headIcon != 0) {
                    Box(mod.background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                        Icon(painterResource(headIcon), contentDescription = null, modifier = Modifier.size(24.dp))
                    }
                }
                Text(
                    headName, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 16.dp),
                )
            }
            if (running) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    Pair(stringResource(AppR.string.total), countText)
                    LinearProgressIndicator(
                        progress = { (progress / progressMax.coerceAtLeast(1)).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    )
                    Pair(stringResource(AppR.string.fail), failText, failRed)
                    Pair(stringResource(AppR.string.estimated), etaText)
                }
            } else if (resultShown) {
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    Pair(stringResource(AppR.string.total), resTotal)
                    Pair(stringResource(AppR.string.fail), resFail, failCount > 0)
                    Pair(stringResource(AppR.string.success), resOk)
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onMain() }, enabled = mainEnabled) { Text(stringResource(mainRes)) }
            }
        }
        show()
    }

    @Composable
    private fun Pair(label: String, value: String, red: Boolean = false) {
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(
                value, style = MaterialTheme.typography.bodyLarge,
                color = if (red) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
        }
    }

    private fun onMain() {
        if (resultShown) {
            dismiss()
            return
        }
        if (deleteTask != null) {
            cancelDelete()
            return
        }
        if (mainBusy) return
        mainBusy = true
        running = true
        mainRes = AppR.string.cancel
        setCanceledOnTouchOutside(false)
        val task = DialogTask(this)
        deleteTask = task
        task.start(appContext)
        mainBusy = false
    }

    /** Cancel: stops the running delete ("Canceling"), or closes. */
    fun cancelDelete() {
        if (deleteTask == null) {
            dismiss()
            return
        }
        mainEnabled = false
        mainRes = AppR.string.canceling
        deleteTask?.cancelled = true
        deleteTask = null
    }

    /** Header: one file (thumbnail through the image loader for media) or "n items". */
    fun setupHead(list: MutableList<Any?>?) {
        if (headFixed) return
        var item = headItem
        if (item == null && list != null) {
            val first = list.get(0) as ChildItem?
            if (first != null) {
                val head = ChildItem()
                headItem = head
                head.count = list.size
                head.itemType = first.itemType
                head.path = first.path
                head.id = first.id
                head.filePath = first.filePath
                if (head.count == 1) {
                    head.iconColor = first.iconColor
                    head.iconRes = first.iconRes
                    head.name = first.name
                } else {
                    head.iconColor = headColor
                    head.iconRes = headIconRes
                    val sb = StringBuilder()
                    sb.append(head.count)
                    head.name = sb.append(appContext!!.getString(AppR.string.items)).toString()
                }
                item = head
            }
        }
        if (item == null) return
        setHead(item.iconRes, null, item.name)
        if (item.count != 1) return
        headFixed = true
        headPath = item.path
        val itemType = item.itemType.toInt()
        if (itemType !in 1..6 && itemType != 11) return
        val viewItem = ViewItem()
        viewItem.itemType = item.itemType
        viewItem.url = item.path
        viewItem.filePath = item.filePath
        viewItem.cacheType = 2
        val name = item.name
        val icon = item.iconRes
        com.mycompany.app.glide.ThumbLoader.loadFor(viewItem, android.widget.ImageView(context), object : ViewItemListener {
                override fun onFailed(viewItem: MainItem.ViewItem?, view: View?, failReason: Exception?) {}
                override fun onComplete(viewItem: MainItem.ViewItem?, view: View?, bitmap: Bitmap?) {
                    if (MainUtil.isBitmapOk(bitmap)) setHead(icon, bitmap, name)
                }
            })
    }

    override fun cancel() {
        cancelDelete()
    }

    override fun canCancelFromScrim(): Boolean = deleteTask == null

    override fun dismiss() {
        deleteTask?.cancelled = true
        deleteTask = null
        msgHandler?.removeMessages(0)
        msgHandler?.removeMessages(1)
        msgHandler = null
        appContext = null
        deleteItems = null
        deleteListener = null
        super.dismiss()
    }

    companion object {
    }
}
