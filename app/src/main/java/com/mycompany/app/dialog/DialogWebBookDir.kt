package com.mycompany.app.dialog

import android.content.Context
import android.text.TextUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.db.book.DbBookWeb
import com.mycompany.app.dialog.DialogWebBookMove.BookMoveListener
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.ui.browser.ComposeBottomDialog
import java.lang.ref.WeakReference
import java.util.ArrayList

/**
 * "Create folder" in the bookmarks list: a name field (focused, keyboard up) and the create button. The caller sets
 * [appContext] (context), [parentFolder] (parent folder path) and [moveListener] (list listener), then calls [build], which shows the sheet. [DialogTask]
 * creates the folder in the background (button turns into Cancel / Canceling), unchanged. Compose now (on
 * [ComposeBottomDialog]).
 */
class DialogWebBookDir(context: Context?) : ComposeBottomDialog(context) {
    @JvmField
    var appContext: Context? = null
    @JvmField
    var moveListener: BookMoveListener? = null
    @JvmField
    var parentFolder: String? = null
    @JvmField
    var createTask: DialogTask? = null
    @JvmField
    var creating: Boolean = false

    private var field by mutableStateOf(TextFieldValue(""))
    private var busy by mutableStateOf(false)
    private var canceling by mutableStateOf(false)
    private var shown = false

    class DialogTask(dialogWebBookDir: DialogWebBookDir?, name: String?) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var name: String? = null
        @JvmField
        var moved: ArrayList<Long>? = null
        @JvmField
        var withFiles: Boolean = false

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogWebBookDir)
            this.dialogRef = weakReference
            val dialogWebBookDir = weakReference.get() as DialogWebBookDir?
            if (dialogWebBookDir == null) {
                return@init
            }
            this.name = name
            val i: Int = 0
            dialogWebBookDir.applyBusy(true)
        
        }
    }

        public override fun doInBackground() {
            val dialogWebBookDir = this.dialogRef!!.get() as DialogWebBookDir?
            if (dialogWebBookDir == null || this.cancelled) {
                return
            }
            this.moved = ArrayList()
            val childItemJ = DbBookWeb.addFolder(-1L, dialogWebBookDir.appContext, dialogWebBookDir.parentFolder, this.name, true)
            if (childItemJ != null) {
                this.moved!!.add(childItemJ.id)
                this.withFiles = true
            }
        }

        public override fun onCancelled() {
            val dialogWebBookDir = this.dialogRef!!.get() as DialogWebBookDir?
            if (dialogWebBookDir == null) {
                return
            }
            dialogWebBookDir.createTask = null
            dialogWebBookDir.dismiss()
        }

        public override fun onPostExecute() {
            val dialogWebBookDir = this.dialogRef!!.get() as DialogWebBookDir?
            if (dialogWebBookDir != null) {
                dialogWebBookDir.createTask = null
                val withFiles = this.withFiles
                val context = dialogWebBookDir.appContext
                if (!withFiles) {
                    showToast(context, AppR.string.fail)
                    dialogWebBookDir.applyBusy(false)
                    return
                }
                showToast(context, AppR.string.success)
                val bookMoveListener = dialogWebBookDir.moveListener
                if (bookMoveListener != null) {
                    bookMoveListener.onMoved(this.moved, dialogWebBookDir.parentFolder)
                }
            }
        }
    }

    /** Builds and shows the sheet (old: built the views, then showed). */
    fun build() {
        if (shown || appContext == null) return
        shown = true
        field = TextFieldValue("")
        setSheet {
            val focus = remember { FocusRequester() }
            val keyboard = LocalSoftwareKeyboardController.current
            Text(
                stringResource(R.string.create_folder),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 8.dp),
            )
            OutlinedTextField(
                value = field,
                onValueChange = { field = it },
                enabled = !busy,
                singleLine = true,
                label = { Text(stringResource(R.string.input_name)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { createFolder() }),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).focusRequester(focus),
            )
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { if (busy) cancelCreate() else createFolder() }, enabled = !canceling) {
                    Text(
                        stringResource(
                            when {
                                canceling -> R.string.canceling
                                busy -> R.string.cancel
                                else -> R.string.create_folder
                            }
                        )
                    )
                }
            }
            LaunchedEffect(Unit) {
                focus.requestFocus()
                keyboard?.show()
            }
        }
        show()
    }

    private fun createFolder() {
        if (creating || busy) return
        creating = true
        startCreate()
        creating = false
    }

    /** Checks the name and starts [DialogTask]. */
    fun startCreate() {
        val name = field.text.trim()
        val context = this.appContext
        if (TextUtils.isEmpty(name)) {
            showToast(context, AppR.string.input_name)
            return
        }
        if (DbBookWeb.hasFolder(context, this.parentFolder, name)) {
            showToast(this.appContext, AppR.string.exist_name)
            return
        }
        val dialogTask = this.createTask
        if (dialogTask != null) {
            dialogTask.cancelled = true
        }
        this.createTask = null
        val createTask = DialogTask(this, name)
        this.createTask = createTask
        createTask.start(this.appContext)
    }

    /** Cancel: stops a running task (button shows "Canceling"), or closes. */
    fun cancelCreate() {
        if (this.createTask == null) {
            dismiss()
            return
        }
        canceling = true
        val dialogTask = this.createTask
        if (dialogTask != null) {
            dialogTask.cancelled = true
        }
        this.createTask = null
    }

    /** Busy state while the task runs. */
    fun applyBusy(newBusy: Boolean) {
        busy = newBusy
        if (!newBusy) canceling = false
        setCanceledOnTouchOutside(!newBusy)
    }

    override fun cancel() {
        cancelCreate()
    }

    override fun canCancelFromScrim(): Boolean = !busy

    override fun dismiss() {
        val dialogTask = this.createTask
        if (dialogTask != null) {
            dialogTask.cancelled = true
        }
        this.createTask = null
        this.appContext = null
        this.moveListener = null
        this.parentFolder = null
        super.dismiss()
    }

    companion object {
    }
}
