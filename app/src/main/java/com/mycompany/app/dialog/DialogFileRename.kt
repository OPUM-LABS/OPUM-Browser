package com.mycompany.app.dialog

import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUtil
import ch.opumlabs.opumbrowser.R as AppR
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.compress.Compress.Companion.isSubtitleExt
import com.mycompany.app.data.DataUtil
import com.mycompany.app.data.DataUtil.saveFile
import com.mycompany.app.data.book.DataBookDown
import com.mycompany.app.db.DbUtil.existsById
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.DbUtil.updateById
import com.mycompany.app.db.book.DbBookDown
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.db.book.DbBookPage
import com.mycompany.app.db.book.DbBookQuick.Companion.iconRes
import com.mycompany.app.db.book.DbBookSearch.Companion.get
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainListLoader.ListLoadListener
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUri.splitNumber
import com.mycompany.app.main.MainUri.externalVolumes
import com.mycompany.app.main.MainUri.volumeLabel
import com.mycompany.app.main.MainUri.isMediaUri
import com.mycompany.app.main.MainUriDoc.uriItemFor
import com.mycompany.app.main.MainUriDoc.renameNumbered
import com.mycompany.app.main.MainUriVol.createUriItem
import com.mycompany.app.main.MainUriVol.createNumbered
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.archiveIconFor
import com.mycompany.app.main.MainUtil.editTextValue
import com.mycompany.app.main.MainUtil.extOf
import com.mycompany.app.main.MainUtil.openKeyboard
import com.mycompany.app.main.MainUtil.replaceExt
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.isBitmapOk
import com.mycompany.app.main.MainUtil.canUseBiometric
import com.mycompany.app.main.MainUtil.selectAllOnFocus
import com.mycompany.app.main.MainUtil.safeFileName
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.compareLongs
import com.mycompany.app.main.MainUtil.mimeForExt
import com.mycompany.app.main.MainUtil.copyFile
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.pref.PrefSet.remove
import com.mycompany.app.pref.PrefUtil.itemMode
import ch.opumlabs.opumbrowser.R
import java.lang.ref.WeakReference

import com.mycompany.app.ui.browser.ComposeBottomDialog
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


/**
 * Rename a file (download list [listType] = 31, album 1, media lists): header with the file's thumbnail or type icon and its
 * name, a name field (focused; the whole name selected for albums / saved pages, otherwise the part before the
 * extension), Rename. Checks as before (empty, > 200 bytes, ".album" / ".mht" handling, same name); the kept
 * [DialogTask] renames the document / MediaStore item and updates the lists, then [FileRenameListener.onRenamed]. While it
 * runs the button is Cancel (then "Canceling"). Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogFileRename(
    activity: Activity?,
    listType: Int,
    childItem: ChildItem?,
    fileRenameListener: FileRenameListener?,
) : ComposeBottomDialog(activity) {
    @JvmField
    var appContext: Context? = context
    @JvmField
    var listType: Int = listType
    @JvmField
    var renameItem: ChildItem? = childItem
    @JvmField
    var wholeName: Boolean = listType == 1 || (listType == 31 && childItem?.listType == 8)
    @JvmField
    var renameListener: FileRenameListener? = fileRenameListener
    @JvmField
    var renameTask: DialogTask? = null
    @JvmField
    var listLoader: MainListLoader? = null
    @JvmField
    var renaming: Boolean = false

    private var field by androidx.compose.runtime.mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(""))
    private var thumb by androidx.compose.runtime.mutableStateOf<Bitmap?>(null)
    private var busy by androidx.compose.runtime.mutableStateOf(false)
    private var canceling by androidx.compose.runtime.mutableStateOf(false)

    class DialogTask(dialogFileRename: DialogFileRename?, name: String?) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var listType: Int = 0
        @JvmField
        var itemId: Long = 0L
        @JvmField
        var newName: String? = null
        @JvmField
        var oldName: String? = null
        @JvmField
        var newPath: String? = null
        @JvmField
        var name: String? = null
        @JvmField
        var renamed: Boolean = false

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogFileRename)
            this.dialogRef = weakReference
            val dialogFileRename = weakReference.get() as DialogFileRename?
            if (dialogFileRename == null) {
                return@init
            }
            val childItem = dialogFileRename.renameItem
            this.listType = childItem!!.listType
            this.itemId = childItem.id
            this.newName = childItem.path
            this.oldName = childItem.name
            this.name = name
            dialogFileRename.applyBusy(true)
        
        }
    }

        /**
         * Renames the file (MediaStore item: display name and mime type; other documents: renameDocument; when
         * that fails the item is looked up by its new name), then updates the item to the result, the download list and
         * database rows (list 31) and the media lists (`DataUtil.saveFile`).
         */
        public override fun doInBackground() {
            val dialog = this.dialogRef!!.get() as DialogFileRename?
            if (dialog == null || this.cancelled || TextUtils.isEmpty(this.newName) || TextUtils.isEmpty(this.name)) {
                return
            }
            val context = dialog.appContext
            val name = this.name
            var item: UriItem? = null
            if (context != null) {
                if (MainUri.isMediaUri(this.newName)) {
                    var uri: Uri? = null
                    var mime: String? = null
                    try {
                        val ext = extOf(name)
                        mime = MainUtil.mimeForExt(ext)
                        if (TextUtils.isEmpty(mime) || isSubtitleExt(ext)) {
                            mime = "application/octet-stream"
                        }
                        uri = Uri.parse(this.newName)
                        val values = ContentValues()
                        values.put("_display_name", name)
                        values.put("mime_type", mime)
                        if (context.getContentResolver().update(uri, values, null, null) > 0) {
                            item = createUriItem(context, uri, null, null, false)
                        }
                    } catch (ignored: Exception) {
                    }
                    if (item == null) {
                        item = createNumbered(context, uri, MainUri.splitNumber(name), mime)
                    }
                } else {
                    var renamed: Uri? = null
                    try {
                        renamed = DocumentsContract.renameDocument(
                            context.getContentResolver(),
                            Uri.parse(this.newName), name!!
                        )
                        if (renamed != null) {
                            item = uriItemFor(context, renamed, null, false)
                        }
                    } catch (ignored: Exception) {
                    }
                    if (item == null) {
                        item = renameNumbered(context, renamed, MainUri.splitNumber(name))
                    }
                }
            }
            if (item == null) {
                return
            }
            this.newPath = item.uriString
            this.name = item.name
            if (dialog.listType == 31) {
                val id = this.itemId
                if (this.listType == 8) {
                    DataBookDown.instance(dialog.appContext).updateItem(this.itemId, this.listType, this.newPath, this.name)
                    updateRow(
                        if (dialog.appContext == null) null else DbBookPage.get(dialog.appContext)
                            .getWritableDatabase(),
                        "DbBookPage_table", id, android.content.ContentValues().also { cv -> cv.put("_path", this.newPath); cv.put("_name", this.name) }
                    )
                } else {
                    this.listType = archiveIconFor(this.newPath)
                    DataBookDown.instance(dialog.appContext).updateItem(this.itemId, this.listType, this.newPath, this.name)
                    val values = ContentValues()
                    values.put("_type", this.listType)
                    values.put("_path", this.newPath)
                    values.put("_name", this.name)
                    updateRow(
                        if (dialog.appContext == null) null else DbBookDown.get(dialog.appContext)
                            .getWritableDatabase(),
                        "DbBookDown_table", id, values
                    )
                    val oldKind = DataUtil.fileKind(this.oldName)
                    val newKind = DataUtil.fileKind(this.name)
                    if ((oldKind >= 1 && oldKind <= 3) || (newKind >= 1 && newKind <= 3)) {
                        saveFile(dialog.appContext, oldKind, newKind, this.newName, this.newPath, item)
                    }
                }
            } else {
                saveFile(dialog.appContext, dialog.listType, DataUtil.fileKind(this.name), this.newName, this.newPath, item)
            }
            this.renamed = true
        }

        /** Updates the row id when it exists and the new path/name are set.  */
        fun updateRow(db: SQLiteDatabase?, table: String?, id: Long, values: ContentValues?) {
            if (db == null || id <= 0 || TextUtils.isEmpty(this.newPath) || TextUtils.isEmpty(this.name)) {
                return
            }
            if (existsById(db, table, id) == 1) {
                updateById(db, table, values, id)
            }
        }

        public override fun onCancelled() {
            val dialogFileRename = this.dialogRef!!.get() as DialogFileRename?
            if (dialogFileRename == null) {
                return
            }
            dialogFileRename.renameTask = null
            dialogFileRename.dismiss()
        }

        public override fun onPostExecute() {
            val dialogFileRename = this.dialogRef!!.get() as DialogFileRename?
            if (dialogFileRename != null) {
                dialogFileRename.renameTask = null
                val renamed = this.renamed
                val context = dialogFileRename.appContext
                if (!renamed) {
                    showToast(context, AppR.string.fail)
                    dialogFileRename.applyBusy(false)
                    return
                }
                showToast(context, AppR.string.success)
                val fileRenameListener = dialogFileRename.renameListener
                if (fileRenameListener != null) {
                    fileRenameListener.onRenamed(this.itemId, this.newPath, this.listType)
                }
                // the Compose list screens do not close it themselves (the old list did)
                dialogFileRename.dismiss()
            }
        }
    }

    fun startRename() {
        if (this.renameItem == null) {
            return
        }
        var typedName: String? = field.text.trim { it <= ' ' }
        if (TextUtils.isEmpty(typedName)) {
            showToast(this.appContext, AppR.string.input_name)
            return
        }
        val bytes: ByteArray? = typedName!!.toByteArray()
        if (bytes != null && bytes.size > 200) {
            showToast(this.appContext, AppR.string.long_name)
            return
        }
        var oldName = this.renameItem!!.name
        if (this.wholeName) {
            val ext = if (this.listType == 1) ".album" else ".mht"
            typedName = replaceExt(typedName, ext)
            if (TextUtils.isEmpty(typedName)) {
                showToast(this.appContext, AppR.string.input_name)
                return
            }
            oldName = (StringBuilder()).append(this.renameItem!!.name).append(ext).toString()
        } else if ("mht".equals(extOf(typedName), ignoreCase = true)) {
            showToast(this.appContext, AppR.string.noti_invalid)
            return
        }
        if (typedName.equals(oldName, ignoreCase = true)) {
            showToast(this.appContext, AppR.string.same_name)
            return
        }
        val safeName = MainUtil.safeFileName(typedName)
        val dialogTask = this.renameTask
        if (dialogTask != null) {
            dialogTask.cancelled = true
        }
        this.renameTask = null
        val renameTask = DialogTask(this, safeName)
        this.renameTask = renameTask
        renameTask.start(this.appContext)
    }

    interface FileRenameListener {
        fun onRenamed(id: Long, newPath: String?, listType: Int)
    }

    init {
        val item = childItem
        if (item == null || TextUtils.isEmpty(item.path)) {
            // nothing to rename (old: the dialog was never shown)
            appContext = null
        } else {
            val name = item.name ?: ""
            val end = if (wholeName) name.length else name.lastIndexOf('.').let { if (it > 0) it else name.length }
            field = androidx.compose.ui.text.input.TextFieldValue(name, androidx.compose.ui.text.TextRange(0, end))
            setSheet {
                val focus = androidx.compose.runtime.remember { androidx.compose.ui.focus.FocusRequester() }
                val keyboard = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
                androidx.compose.foundation.layout.Row(
                    androidx.compose.ui.Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    val bmp = thumb
                    val mod = androidx.compose.ui.Modifier.size(40.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                    if (bmp != null) {
                        androidx.compose.foundation.Image(
                            bmp.asImageBitmap(), contentDescription = null, modifier = mod,
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        )
                    } else if (item.iconRes != 0) {
                        androidx.compose.foundation.layout.Box(
                            mod.background(androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = androidx.compose.ui.Alignment.Center,
                        ) {
                            androidx.compose.material3.Icon(
                                androidx.compose.ui.res.painterResource(item.iconRes), contentDescription = null,
                                modifier = androidx.compose.ui.Modifier.size(24.dp),
                            )
                        }
                    }
                    androidx.compose.material3.Text(
                        name, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                        modifier = androidx.compose.ui.Modifier.padding(start = 16.dp),
                    )
                }
                androidx.compose.material3.OutlinedTextField(
                    value = field,
                    onValueChange = { field = it },
                    enabled = !busy,
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { rename() }),
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)
                        .focusRequester(focus),
                )
                androidx.compose.foundation.layout.Row(
                    androidx.compose.ui.Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
                ) {
                    androidx.compose.material3.TextButton(onClick = { if (busy) cancelRename() else rename() }, enabled = !canceling) {
                        androidx.compose.material3.Text(
                            androidx.compose.ui.res.stringResource(
                                when {
                                    canceling -> AppR.string.canceling
                                    busy -> AppR.string.cancel
                                    else -> AppR.string.rename
                                }
                            )
                        )
                    }
                }
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    focus.requestFocus()
                    keyboard?.show()
                }
            }
            loadThumb(item)
            show()
        }
    }

    /** Thumbnail as before: images / videos / albums etc. (list types 1..6, 11; downloads only for media). */
    private fun loadThumb(source: ChildItem) {
        if (listType == 31 && source.status != 3) return
        val itemType = source.itemType.toInt()
        if (itemType !in 1..6 && itemType != 11) return
        var item = source
        if (itemType == 11) {
            val copy = ChildItem()
            copy.listType = listType
            copy.itemType = source.itemType
            copy.path = source.filePath
            copy.filePath = source.filePath
            copy.id = source.id
            copy.position = source.position
            copy.iconColor = source.iconColor
            copy.iconRes = source.iconRes
            item = copy
        }
        if (TextUtils.isEmpty(item.path)) return
        val cached = MainListLoader.cachedIconFor(item)
        if (MainUtil.isBitmapOk(cached)) {
            thumb = cached
            return
        }
        val ctx = appContext ?: return
        val target = View(ctx)
        target.setTag(item.position)
        val loader = MainListLoader(ctx, false, object : ListLoadListener {
            override fun onFailed(childItem: ChildItem?, view: View?) {}
            override fun onLoaded(childItem: ChildItem?, view: View?, bitmap: Bitmap?) {
                if (MainUtil.isBitmapOk(bitmap)) thumb = bitmap
            }
        })
        listLoader = loader
        loader.enqueue(item, target)
    }

    private fun rename() {
        if (renaming || busy) return
        renaming = true
        startRename()
        renaming = false
    }

    /** Cancel: stops a running rename ("Canceling"), or closes. */
    fun cancelRename() {
        if (renameTask == null) {
            dismiss()
            return
        }
        canceling = true
        renameTask?.cancelled = true
        renameTask = null
    }

    /** Busy state while [DialogTask] runs. */
    fun applyBusy(newBusy: Boolean) {
        busy = newBusy
        if (!newBusy) canceling = false
        setCanceledOnTouchOutside(!newBusy)
    }

    override fun canCancelFromScrim(): Boolean = !busy

    override fun dismiss() {
        renameTask?.cancelled = true
        renameTask = null
        listLoader?.release()
        listLoader = null
        renameListener = null
        super.dismiss()
    }

    companion object {
    }
}
