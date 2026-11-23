package com.mycompany.app.dialog

import com.mycompany.app.main.MainUtil
import com.mycompany.app.data.book.DataBookLink
import com.mycompany.app.db.DbUtil
import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.webkit.URLUtil
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.data.book.DataBookAds
import com.mycompany.app.data.book.DataBookBlock
import com.mycompany.app.data.book.DataBookFilter
import com.mycompany.app.data.book.DataBookJava
import com.mycompany.app.data.book.DataBookLink.Companion.instance
import com.mycompany.app.data.book.DataBookOver
import com.mycompany.app.data.book.DataBookPop
import com.mycompany.app.data.book.DataBookScript
import com.mycompany.app.data.book.DataBookTmem
import com.mycompany.app.data.book.DataBookTrans
import com.mycompany.app.db.DbSite.addPath
import com.mycompany.app.db.DbUtil.updateById
import com.mycompany.app.db.book.DbBookAds
import com.mycompany.app.db.book.DbBookBlock
import com.mycompany.app.db.book.DbBookFilter
import com.mycompany.app.db.book.DbBookJava
import com.mycompany.app.db.book.DbBookLink
import com.mycompany.app.db.book.DbBookOver
import com.mycompany.app.db.book.DbBookPop
import com.mycompany.app.db.book.DbBookTmem
import com.mycompany.app.db.book.DbBookTrans
import com.mycompany.app.db.book.DbBookWeb.Companion.editItem
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.findChild
import com.mycompany.app.main.MainUri.externalVolumes
import com.mycompany.app.main.MainUri.volumeLabel
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.main.MainUtil.pathOfUrl
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.trimTrailingSlash
import com.mycompany.app.main.MainUtil.editTextValue
import com.mycompany.app.main.MainUtil.postRequestLayout
import com.mycompany.app.main.MainUtil.filterFilePath
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.sizeLabel
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.main.MainUtil.compareLongs
import com.mycompany.app.main.MainUtil.copyFile
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.web.WebClean
import java.io.File
import java.lang.ref.WeakReference


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Add / edit one entry of a site list ([urlTypeParam]: 19 ads, 20 overlay, 21 popups, 22 links, 23 blocked images (page + image
 * URL), 24 filters (name + URL, with the filter icon), 26 scripts (only hands the URL back), 27 JavaScript, 28 tab
 * memory, 29 translation) from [childItem] (row id -1 = new). Domain / URL field, Apply (Cancel / Canceling while
 * saving). Checks (empty, unsupported page, network URL, already in the list) and the save ([DialogTask]: list DB +
 * in-memory list, filter file, blocked-image map) are the old code; [EditUrlListener] hears the result. Compose now
 * (on [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogEditUrl(
    activity: Activity?,
    urlTypeParam: Int,
    childItem: ChildItem?,
    editUrlListener: EditUrlListener?,
) : ComposeBottomDialog(activity) {
    @JvmField
    var appContext: Context? = context
    @JvmField
    var urlType: Int = urlTypeParam
    @JvmField
    var editListener: EditUrlListener? = editUrlListener
    @JvmField
    var editItem: ChildItem? = childItem
    @JvmField
    var saveTask: DialogTask? = null
    @JvmField
    var applying: Boolean = false
    @JvmField
    var webClean: WebClean? = if (urlTypeParam == 23) MainApp.webClean(context, false) else null

    private var nameField by mutableStateOf(TextFieldValue(childItem?.name ?: ""))
    private var mainField by mutableStateOf(TextFieldValue(childItem?.path ?: "", TextRange(0, (childItem?.path ?: "").length)))
    private var imageField by mutableStateOf(TextFieldValue(childItem?.name ?: ""))
    internal var busy by mutableStateOf(false)
    internal var mainRes by mutableIntStateOf(AppR.string.apply)
    private var mainEnabled by mutableStateOf(true)

    class DialogTask(
        dialogEditUrl: DialogEditUrl?,
        childItem: ChildItem?,
        dbPath: String?,
        dbExtra: String?
    ) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>
        @JvmField
        var item: ChildItem? = null
        @JvmField
        var dbPath: String? = null
        @JvmField
        var dbExtra: String? = null
        @JvmField
        var savedPath: String? = null
        @JvmField
        var savedExtra: String? = null

        init {
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogEditUrl)
            this.dialogRef = weakReference
            val dialogEditUrl = weakReference.get() as DialogEditUrl?
            if (dialogEditUrl != null) {
                this.item = childItem
                this.dbPath = dbPath
                this.dbExtra = dbExtra
                dialogEditUrl.setCanceledOnTouchOutside(false)
                dialogEditUrl.busy = true
                dialogEditUrl.mainRes = AppR.string.cancel
            }
        }

        /**
         * Saves the edited entry: updates the row in place, or (for a new entry, id -1) inserts it.
         * The first field is the URL/path, the second the image URL for blocked images or the title for filters.
         * Also keeps each list's in-memory path index and the ad blocker's image map in sync.
         */
        public override fun doInBackground() {
            val dialog = this.dialogRef.get() as DialogEditUrl?
            if (dialog == null || this.cancelled) {
                return
            }
            val context = dialog.appContext
            val type = dialog.urlType
            val extra = this.dbExtra
            val path = this.dbPath!!
            val isNew = this.item!!.id == -1L
            try {
                when (type) {
                    19 -> if (isNew) {
                        DataBookAds.instance(context).addSite(path)
                        this.item = DbBookAds.addPath(context, path)
                        DataBookAds.instance(context).putItem(this.item)
                    } else {
                        updatePath(DbBookAds.get(context).getWritableDatabase(), "DbBookAds_table")
                        renamePath(DataBookAds.instance(context).sites, path)
                    }

                    20 -> if (isNew) {
                        addPath(DataBookOver.instance(context), path)
                        this.item = addPath(
                            DbBookOver.get(context).getWritableDatabase(),
                            "DbBookOver_table",
                            path
                        )
                        DataBookOver.instance(context).putItem(this.item)
                    } else {
                        updatePath(DbBookOver.get(context).getWritableDatabase(), "DbBookOver_table")
                        renamePath(DataBookOver.instance(context).paths, path)
                    }

                    21 -> if (isNew) {
                        DataBookPop.instance(context).addSite(path)
                        this.item = DbBookPop.addPath(context, path)
                        DataBookPop.instance(context).putItem(this.item)
                    } else {
                        updatePath(DbBookPop.get(context).getWritableDatabase(), "DbBookPop_table")
                        renamePath(DataBookPop.instance(context).sites, path)
                    }

                    22 -> if (isNew) {
                        DataBookLink.instance(context).addLink(path)
                        this.item = DbBookLink.addPath(context, path)
                        DataBookLink.instance(context).putItem(this.item)
                    } else {
                        updatePath(DbBookLink.get(context).getWritableDatabase(), "DbBookLink_table")
                        renamePath(DataBookLink.instance(context).links, path)
                    }

                    23 -> saveBlockedImage(dialog, context, path, extra, isNew)
                    24 -> if (isNew) {
                        this.item = DbBookFilter.addPathItem(context, path, extra)
                        DataBookFilter.instance(context).putItem(this.item)
                        this.savedPath = path
                        this.savedExtra = extra
                    } else {
                        val values = ContentValues()
                        values.put("_path", path)
                        values.put("_title", extra)
                        DbUtil.updateById(
                            DbBookFilter.get(context).getWritableDatabase(),
                            "DbBookFilter_table",
                            values,
                            this.item!!.id
                        )
                        if (!MainUtil.sameObject(this.item!!.path, path)) {
                            this.savedPath = path
                            this.savedExtra = extra
                        }
                    }

                    27 -> if (isNew) {
                        DataBookJava.instance(context).addSite(path)
                        this.item = DbBookJava.addPath(context, path)
                        DataBookJava.instance(context).putItem(this.item)
                    } else {
                        updatePath(DbBookJava.get(context).getWritableDatabase(), "DbBookJava_table")
                        renamePath(DataBookJava.instance(context).sites, path)
                    }

                    28 -> if (isNew) {
                        DataBookTmem.instance(context).addSite(path)
                        this.item = DbBookTmem.addPath(context, path)
                        DataBookTmem.instance(context).putItem(this.item)
                    } else {
                        updatePath(DbBookTmem.get(context).getWritableDatabase(), "DbBookTmem_table")
                        renamePath(DataBookTmem.instance(context).sites, path)
                    }

                    29 -> if (isNew) {
                        DataBookTrans.instance(context).addSite(path)
                        this.item = DbBookTrans.addPath(context, path)
                        DataBookTrans.instance(context).putItem(this.item)
                    } else {
                        updatePath(
                            DbBookTrans.get(context).getWritableDatabase(),
                            "DbBookTrans_table"
                        )
                        renamePath(DataBookTrans.instance(context).sites, path)
                    }

                    else -> {}
                }
            } catch (unused: Exception) {
            }
            val item = this.item
            if (item == null) {
                return
            }
            item.path = path
            if (type == 23) {
                item.name = extra
                item.subText = path
            } else if (type != 24) {
                item.name = path
                item.subText = pathOfUrl(path, false)
            } else {
                item.name = extra
                val oldFile = item.filePath
                item.filePath = filterFilePath(context, path)
                try {
                    if (!TextUtils.isEmpty(item.filePath)) {
                        item.size = File(item.filePath!!).length()
                        item.subText = MainUtil.sizeLabel(item.size)
                    }
                } catch (unused: Exception) {
                }
                if (!TextUtils.isEmpty(oldFile) && oldFile != item.filePath) {
                    try {
                        File(oldFile!!).delete()
                    } catch (unused: Exception) {
                    }
                }
            }
        }

        /** Updates the edited row's path (the old path is still in the item).  */
        fun updatePath(db: SQLiteDatabase?, table: String?) {
            val values = ContentValues()
            values.put("_path", this.dbPath)
            DbUtil.updateById(db, table, values, this.item!!.id)
        }

        /** Replaces the old path with `path` in a list's in-memory path index.  */
        fun renamePath(paths: MutableList<String?>?, path: String?) {
            val old = this.item!!.path
            if (MainUtil.sameObject(
                    old,
                    path
                ) || paths == null || paths.isEmpty() || TextUtils.isEmpty(old) || TextUtils.isEmpty(
                    path
                )
            ) {
                return
            }
            val index = paths.indexOf(old)
            if (index >= 0 && index < paths.size) {
                paths.set(index, path)
            }
        }

        /** Blocked image: `path` is the page, `image` the image URL.  */
        fun saveBlockedImage(
            dialog: DialogEditUrl?,
            context: Context?,
            path: String?,
            image: String?,
            isNew: Boolean
        ) {
            val clean = dialog!!.webClean
            if (isNew) {
                if (clean != null) {
                    clean.addUserBlock(path, image)
                }
                this.item = DbBookBlock.addPath(context, path, image)
                DataBookBlock.instance(context).putItem(this.item)
                return
            }
            val values = ContentValues()
            values.put("_path", path)
            values.put("_image", image)
            DbUtil.updateById(DbBookBlock.get(context).getWritableDatabase(), "DbBookBlock_table", values, this.item!!.id)
            val oldPath = this.item!!.path
            val oldImage = this.item!!.name
            if (clean == null || (MainUtil.sameObject(oldPath, path) && MainUtil.sameObject(oldImage, image))) {
                return
            }
            val map = clean.userBlocks
            if (map == null || map.isEmpty()) {
                return
            }
            if (!MainUtil.sameObject(oldPath, path)) {
                clean.removeUserBlock(oldPath, oldImage)
                clean.addUserBlock(path, image)
            } else if (!TextUtils.isEmpty(oldPath) && !TextUtils.isEmpty(oldImage)) {
                val images = map.get(oldPath)
                if (images != null && !images.isEmpty()) {
                    images.remove(oldImage)
                    images.add(image)
                }
            }
        }

        public override fun onCancelled() {
            val dialogEditUrl = this.dialogRef.get() as DialogEditUrl?
            if (dialogEditUrl == null) {
                return
            }
            dialogEditUrl.saveTask = null
            dialogEditUrl.dismiss()
        }

        public override fun onPostExecute() {
            val dialogEditUrl = this.dialogRef.get() as DialogEditUrl?
            if (dialogEditUrl != null) {
                dialogEditUrl.saveTask = null
                val editUrlListener = dialogEditUrl.editListener
                if (editUrlListener != null) {
                    val childItem = this.item
                    if (childItem != null) {
                        editUrlListener.onSaved(childItem.id, this.savedPath, this.savedExtra)
                    } else {
                        editUrlListener.onSaved(0L, this.savedPath, this.savedExtra)
                    }
                }
            }
        }

        companion object {
            private fun addPath(data: DataBookOver?, path: String?) {
                if (TextUtils.isEmpty(path)) {
                    return
                }
                try {
                    if (data!!.paths == null) {
                        data.paths = ArrayList()
                    }
                    if (!data.paths!!.contains(path)) {
                        data.paths!!.add(path)
                    }
                } catch (unused: Exception) {
                }
            }
        }
    }

    interface EditUrlListener {
        fun onSaved(id: Long, path: String?, extra: String?)
    }

    init {
        val item = childItem
        if (item != null) {
            setSheet {
                val mode = urlType
                if (mode == 24) {
                    Row(
                        Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val res = if (item.iconRes != 0) item.iconRes else AppR.drawable.outline_public_black_24
                        val bg = if (item.iconRes != 0) item.iconColor else -460552
                        Box(Modifier.size(40.dp).clip(CircleShape).background(Color(bg)), contentAlignment = Alignment.Center) {
                            Icon(painterResource(res), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(24.dp))
                        }
                        OutlinedTextField(
                            value = nameField,
                            onValueChange = { nameField = it },
                            placeholder = { Text(stringResource(AppR.string.name)) },
                            singleLine = true,
                            enabled = !busy,
                            modifier = Modifier.weight(1f).padding(start = 16.dp),
                        )
                    }
                }
                OutlinedTextField(
                    value = mainField,
                    onValueChange = { mainField = it },
                    label = { Text(stringResource(if (mode == 24 || mode == 26) AppR.string.url else AppR.string.domain_url)) },
                    placeholder = { if (mode == 24 || mode == 26) Text("https://...") },
                    singleLine = true,
                    enabled = !busy,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = if (mode == 23) ImeAction.Next else ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { apply() }),
                    modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = if (mode == 24) 8.dp else 20.dp, bottom = 8.dp),
                )
                if (mode == 23) {
                    OutlinedTextField(
                        value = imageField,
                        onValueChange = { imageField = it },
                        label = { Text(stringResource(AppR.string.image)) },
                        placeholder = { Text("https://...") },
                        singleLine = true,
                        enabled = !busy,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { apply() }),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                    )
                }
                if (busy) {
                    LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp))
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { if (saveTask != null) cancelSave() else apply() }, enabled = mainEnabled) { Text(stringResource(mainRes)) }
                }
            }
            show()
        }
    }

    private fun apply() {
        if (applying || busy) return
        applying = true
        save(editItem)
        applying = false
    }

    /** Cancel: stops the running save ("Canceling"), or closes. */
    fun cancelSave() {
        if (saveTask == null) {
            dismiss()
            return
        }
        mainEnabled = false
        mainRes = AppR.string.canceling
        saveTask?.cancelled = true
        saveTask = null
    }

    /** Apply: checks the fields, then saves in [DialogTask] (scripts: hands the URL back). */
    fun save(childItem: ChildItem?) {
        val urlTypeNow = urlType
        var second: String? = null
        if (urlTypeNow == 24) {
            second = nameField.text.trim { it <= ' ' }
            if (TextUtils.isEmpty(second)) {
                showToast(appContext, AppR.string.input_name)
                return
            }
        }
        val main = mainField.text.trim { it <= ' ' }
        if (TextUtils.isEmpty(main)) {
            showToast(appContext, if (urlTypeNow == 24 || urlTypeNow == 26) AppR.string.input_url else AppR.string.empty)
            return
        }
        val url = trimTrailingSlash(main)
        if (urlTypeNow == 22) {
            if ("file:///android_asset/shortcut.html" == url || "about:blank" == url) {
                showToast(appContext, AppR.string.not_supported_page)
                return
            }
        } else if ((urlTypeNow == 24 || urlTypeNow == 26) && !URLUtil.isNetworkUrl(url)) {
            showToast(appContext, AppR.string.invalid_url)
            return
        }
        if (urlTypeNow == 23) {
            second = imageField.text.trim { it <= ' ' }
            if (TextUtils.isEmpty(second)) {
                showToast(appContext, AppR.string.input_url)
                return
            } else if (!URLUtil.isNetworkUrl(second)) {
                showToast(appContext, AppR.string.invalid_url)
                return
            }
        }
        if (!TextUtils.isEmpty(url)) {
            val list: MutableList<ChildItem?>? = when (urlTypeNow) {
                19 -> DataBookAds.instance(appContext).items
                20 -> DataBookOver.instance(appContext).items
                21 -> DataBookPop.instance(appContext).items
                22 -> DataBookLink.instance(appContext).items
                23 -> DataBookBlock.instance(appContext).items
                24 -> DataBookFilter.instance(appContext).items
                26 -> DataBookScript.instance(appContext).items
                27 -> DataBookJava.instance(appContext).items
                28 -> DataBookTmem.instance(appContext).items
                29 -> DataBookTrans.instance(appContext).items
                else -> null
            }
            if (list != null && !list.isEmpty()) {
                for (any in list) {
                    val other = any ?: continue
                    if (childItem != null && childItem.id == other.id) continue
                    val same = if (urlTypeNow != 23) url.equals(other.path, ignoreCase = true)
                    else !TextUtils.isEmpty(second) && url.equals(other.path, ignoreCase = true) && second.equals(other.name, ignoreCase = true)
                    if (same) {
                        showToast(appContext, if (urlTypeNow == 24 || urlTypeNow == 26) AppR.string.exist_url else AppR.string.already_added)
                        return
                    }
                }
            }
        }
        if (urlTypeNow == 26) {
            editListener?.onSaved(0L, url, null)
            return
        }
        saveTask?.cancelled = true
        saveTask = null
        val task = DialogTask(this, childItem, url!!, second)
        saveTask = task
        task.start(appContext)
    }

    override fun cancel() {
        cancelSave()
    }

    override fun canCancelFromScrim(): Boolean = !busy

    override fun dismiss() {
        if (appContext == null) {
            super.dismiss()
            return
        }
        saveTask?.cancelled = true
        saveTask = null
        appContext = null
        editListener = null
        editItem = null
        super.dismiss()
    }

    companion object {
    }
}
