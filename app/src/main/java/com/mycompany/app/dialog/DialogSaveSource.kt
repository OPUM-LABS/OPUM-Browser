package com.mycompany.app.dialog

import android.content.Intent
import android.text.TextUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.db.book.DbBookDown
import com.mycompany.app.dialog.DialogDownPage.DownPageListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.subtitle.Subtitle
import java.io.BufferedWriter
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.lang.ref.WeakReference
import java.nio.charset.StandardCharsets
import java.util.Scanner

/**
 * Save text as a file (page source viewer, reader, text viewer): header with a note icon and title [pageTitle], the name
 * (title, ".txt" unless it already ends in a subtitle / text extension) and save location; Save writes [list] (lines)
 * or [sourceText] in [DialogTask] (with a progress bar and Cancel), records the download and hands the file name to
 * [DownPageListener]; a failure shows "Fail" and Retry. Folder picker results come through [onPickResult]. Compose now (on
 * [SaveFolderDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogSaveSource(
    mainActivity: android.app.Activity?,
    pageTitle: String?,
    sourceText: String?,
    list: MutableList<String?>?,
    downPageListener: DownPageListener?,
) : SaveFolderDialog(mainActivity, null) {
    @JvmField
    var pageListener: DownPageListener? = downPageListener
    @JvmField
    var saveTask: DialogTask? = null
    @JvmField
    var pageTitle: String? = pageTitle
    @JvmField
    var sourceText: String? = sourceText
    @JvmField
    var sourceLines: MutableList<String?>? = list
    /** Cancel requested. */
    @JvmField
    var cancelRequested: Boolean = false

    init {
        start()
    }

    override fun prepare(): String? = MainUtil.safeTitleFileName(186, pageTitle, "Source")

    override fun suggestedWithExt(cleaned: String?): String? {
        if (TextUtils.isEmpty(cleaned)) return MainUtil.replaceExt(cleaned, ".txt")
        for (index in 0..9) {
            if (cleaned!!.endsWith(Subtitle.DOT_EXTENSIONS[index])) return cleaned
        }
        return MainUtil.replaceExt(cleaned, ".txt")
    }

    @Composable
    override fun ColumnScope.Header() {
        Row(
            Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                Icon(painterResource(AppR.drawable.outline_sticky_note_black_24), contentDescription = null, modifier = Modifier.size(24.dp))
            }
            Text(
                pageTitle ?: "", maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }

    internal fun working(on: Boolean) {
        busy = on
        mainRes = if (on) AppR.string.cancel else AppR.string.save
        mainEnabled = true
        setCanceledOnTouchOutside(!on)
    }

    internal fun failed() {
        mainRes = AppR.string.retry
    }

    override fun onSave(fileName: String) {
        saveTask?.cancelled = true
        saveTask = null
        val task = DialogTask(this, fileName)
        saveTask = task
        task.start(appContext)
    }

    override fun onCancelWork() {
        cancelRequested = true
        mainEnabled = false
        mainRes = AppR.string.canceling
        saveTask?.cancelled = true
    }

    /** Folder picker result (request code 19). */
    fun onPickResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean = onFolderResult(requestCode, resultCode, intent)

    /** Saving was cancelled. */
    fun isCancelled(): Boolean {
        if (cancelRequested) return true
        val task = saveTask
        return task != null && task.cancelled
    }

    override fun dismiss() {
        cancelRequested = true
        saveTask?.cancelled = true
        saveTask = null
        pageListener = null
        super.dismiss()
    }

    class DialogTask(dialogSaveSource: DialogSaveSource?, url: String?) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var url: String? = null
        @JvmField
        var path: String? = null
        @JvmField
        var withFiles: Boolean = false

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogSaveSource)
            this.dialogRef = weakReference
            val dialogSaveSource = weakReference.get() as DialogSaveSource?
            if (dialogSaveSource == null) {
                return@init
            }
            this.url = url
            dialogSaveSource.cancelRequested = false
            dialogSaveSource.working(true)
        }
    }
        public override fun doInBackground() {
            var outputStreamOpenOutputStream: OutputStream?
            var bufferedWriter: BufferedWriter?
            val saved: Boolean
            val found: Boolean
            val dialogSaveSource = this.dialogRef!!.get() as DialogSaveSource?
            val uriItemC: UriItem? = if (dialogSaveSource == null || this.cancelled) null else MainUri.createUri(
                    dialogSaveSource.appContext,
                    MainUri.storageVolume(),
                    null,
                    this.url
                )
            if (dialogSaveSource == null || this.cancelled || uriItemC == null) {
                return
            }
            try {
                outputStreamOpenOutputStream =
                    dialogSaveSource.appContext!!.getContentResolver().openOutputStream(uriItemC.uri!!)
                try {
                    bufferedWriter = BufferedWriter(
                        OutputStreamWriter(
                            outputStreamOpenOutputStream,
                            StandardCharsets.UTF_8
                        )
                    )
                    try {
                        val list = dialogSaveSource.sourceLines
                        if (list != null && list.size != 0) {
                            val size = list.size - 1
                            val it: MutableIterator<Any?> = list.iterator()
                            var index = 0
                            while (true) {
                                if (!it.hasNext()) {
                                    found = false
                                    break
                                }
                                val save = it.next() as String?
                                if (dialogSaveSource.isCancelled()) {
                                    found = true
                                    break
                                }
                                if (!TextUtils.isEmpty(save)) {
                                    bufferedWriter.write(save)
                                    bufferedWriter.write("\n")
                                } else if (index != 0 && index != size) {
                                    bufferedWriter.write("\n")
                                }
                                index++
                            }
                        } else {
                            val scanner = Scanner(dialogSaveSource.sourceText)
                            while (true) {
                                if (!scanner.hasNextLine()) {
                                    saved = false
                                    break
                                } else {
                                    if (dialogSaveSource.isCancelled()) {
                                        saved = true
                                        break
                                    }
                                    val line = scanner.nextLine()
                                    if (!TextUtils.isEmpty(line)) {
                                        bufferedWriter.write(line)
                                    }
                                    bufferedWriter.write("\n")
                                }
                            }
                            scanner.close()
                            found = saved
                        }
                        this.withFiles = !found
                    } catch (unused: Exception) {
                    }
                } catch (unused2: Exception) {
                    bufferedWriter = null
                }
            } catch (unused3: Exception) {
                outputStreamOpenOutputStream = null
                bufferedWriter = null
            }
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close()
                } catch (unused4: Exception) {
                    this.withFiles = false
                }
            }
            if (outputStreamOpenOutputStream != null) {
                try {
                    outputStreamOpenOutputStream.close()
                } catch (unused5: Exception) {
                    this.withFiles = false
                }
            }
            if (this.withFiles) {
                val uriString = uriItemC.uriString
                this.path = uriString
                val uriItemJ = MainUri.createIn(dialogSaveSource.appContext, uriString, MainUri.storageVolume())
                if (uriItemJ != null) {
                    DbBookDown.addFile(dialogSaveSource.appContext, this.path, null, uriItemJ)
                }
            }
        }

        public override fun onCancelled() {
            val dialogSaveSource = this.dialogRef!!.get() as DialogSaveSource?
            if (dialogSaveSource == null) {
                return
            }
            dialogSaveSource.saveTask = null
            showToast(dialogSaveSource.appContext, AppR.string.cancelled)
            dialogSaveSource.dismiss()
        }

        public override fun onPostExecute() {
            val dialogSaveSource = this.dialogRef!!.get() as DialogSaveSource?
            if (dialogSaveSource != null) {
                dialogSaveSource.saveTask = null
                if (dialogSaveSource.isCancelled()) {
                    showToast(dialogSaveSource.appContext, AppR.string.cancelled)
                    dialogSaveSource.dismiss()
                    return
                }
                if (this.withFiles) {
                    val downPageListener = dialogSaveSource.pageListener
                    if (downPageListener != null) {
                        downPageListener.onSave(null, this.path, null)
                    }
                    dialogSaveSource.dismiss()
                    return
                }
                if (dialogSaveSource.appContext == null) {
                    return
                }
                showToast(dialogSaveSource.appContext, AppR.string.fail)
                dialogSaveSource.working(false)
                dialogSaveSource.failed()
            }
        }
    }

    companion object {
    }
}
