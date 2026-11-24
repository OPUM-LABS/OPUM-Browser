package com.mycompany.app.dialog

import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.SyncSettings
import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.os.Build
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.DbUtil.updateById
import com.mycompany.app.db.book.DbBookDown.Companion.deleteById
import com.mycompany.app.db.book.DbBookTab.Companion.deleteById
import com.mycompany.app.db.book.DbBookWeb
import com.mycompany.app.db.book.DbBookWeb.Companion.deleteById
import com.mycompany.app.db.book.DbBookWeb.Companion.updateById
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainListLoader.ListLoadListener
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.findChild
import com.mycompany.app.main.MainUri.externalVolumes
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.main.MainUtil.datePattern
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.trimTrailingSlash
import com.mycompany.app.main.MainUtil.editTextValue
import com.mycompany.app.main.MainUtil.openKeyboard
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.isBitmapOk
import com.mycompany.app.main.MainUtil.uriDisplayName
import com.mycompany.app.main.MainUtil.countLabel
import com.mycompany.app.main.MainUtil.uriFileName
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.formatDate
import com.mycompany.app.pref.PrefSet.remove
import com.mycompany.app.pref.PrefSync
import java.lang.ref.WeakReference

import com.mycompany.app.ui.browser.ComposeBottomDialog
import androidx.compose.foundation.Image
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Bookmark list actions on one or more items ([list]) in folder [targetFolderParam]: [moveTypeParam] = 2 delete (header with icon and name or
 * "n items", Delete), 3 move (starts at once), 4 rename a folder (name field, Rename). While working: Total n / m with
 * a progress bar and Cancel / Canceling. The work ([DialogTask]: collects the items and folder contents, deletes /
 * moves / renames in `DbBookWeb`) and [BookMoveListener] are the old code. Compose now (on [ComposeBottomDialog]);
 * same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogWebBookMove(
    activity: Activity?,
    list: MutableList<Any?>?,
    targetFolderParam: String?,
    moveTypeParam: Int,
    bookMoveListener: BookMoveListener?,
) : ComposeBottomDialog(activity) {
    @JvmField
    var appContext: Context? = context
    @JvmField
    var moveListener: BookMoveListener? = bookMoveListener
    @JvmField
    var targetFolder: String? = targetFolderParam
    @JvmField
    var moveItems: MutableList<Any?>? = list
    @JvmField
    var moveType: Byte = moveTypeParam.toByte()
    @JvmField
    var moveTask: DialogTask? = null
    @JvmField
    var listLoader: MainListLoader? = null
    @JvmField
    var moving: Boolean = false
    @JvmField
    var datePattern: String? = null

    private var headIcon by mutableIntStateOf(0)
    private var headBitmap by mutableStateOf<Bitmap?>(null)
    private var headName by mutableStateOf("")
    private var field by mutableStateOf(TextFieldValue(""))
    private var running by mutableStateOf(false)
    internal var countText by mutableStateOf("")
    internal var progress by mutableFloatStateOf(0f)
    internal var progressMax by mutableIntStateOf(1)
    private var mainRes by mutableIntStateOf(AppR.string.delete)
    private var mainEnabled by mutableStateOf(true)

    interface BookMoveListener {
        fun onCancelled()

        fun onMoved(arrayList: ArrayList<Long>?, folder: String?)
    }

    class DialogTask(
        dialogWebBookMove: DialogWebBookMove?,
        list: MutableList<Any?>?,
        from: String?,
        to: String?,
        mode: Int
    ) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var items: MutableList<Any?>? = null
        @JvmField
        var from: String? = null
        @JvmField
        var to: String? = null
        @JvmField
        var mode: Byte = 0
        @JvmField
        var size: Int = 0
        @JvmField
        var done: Int = 0
        @JvmField
        var ids: ArrayList<Long>? = null

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogWebBookMove)
            this.dialogRef = weakReference
            if ((weakReference.get() as DialogWebBookMove?) == null) {
                return@init
            }
            this.items = list
            this.from = from
            this.to = to
            this.mode = (mode).toByte()

        }
    }

        public override fun doInBackground() {
            val d = this.dialogRef!!.get() as DialogWebBookMove?
            if (d == null || this.cancelled || d.appContext == null || this.items == null || this.items!!.isEmpty()) {
                return
            }
            val ids: ArrayList<Long> = ArrayList()
            for (item2 in this.items) {
                ids.add((item2 as ChildItem).id)
            }
            if (ids.isEmpty()) {
                return
            }
            if (d.datePattern == null) {
                d.datePattern = datePattern("yyyy.MM.dd")
            }
            val all: ArrayList<Any?> = ArrayList<Any?>()
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    DbBookWeb.get(d.appContext).getWritableDatabase(), "DbBookWeb_table", null, "_secret=?",
                    arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.sharedKey()), null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val idCol = cursor.getColumnIndex("_id")
                    val dirFlagCol = cursor.getColumnIndex("_isdir")
                    val dirCol = cursor.getColumnIndex("_dir")
                    val pathCol = cursor.getColumnIndex("_path")
                    val titleCol = cursor.getColumnIndex("_title")
                    val timeCol = cursor.getColumnIndex("_time")
                    do {
                        val item = ChildItem()
                        item.dir = cursor.getString(dirCol)
                        if (!TextUtils.isEmpty(item.dir)) {
                            item.isDir = cursor.getInt(dirFlagCol) == 1
                            item.name = cursor.getString(titleCol)
                            var keep = true
                            if (item.isDir) {
                                item.path = if ("/" == item.dir) "/" + item.name else item.dir + "/" + item.name
                            } else {
                                item.path = cursor.getString(pathCol)
                                keep = !TextUtils.isEmpty(item.path)
                            }
                            if (keep) {
                                item.id = cursor.getLong(idCol)
                                item.time = cursor.getLong(timeCol)
                                item.dateText = MainUtil.formatDate(item.time, d.datePattern, "yyyy.MM.dd")
                                all.add(item)
                            }
                        }
                    } while (!this.cancelled && cursor.moveToNext())
                }
            } catch (unused: Exception) {
            }
            if (cursor != null) {
                cursor.close()
            }
            if (this.cancelled || all.isEmpty()) {
                return
            }
            // Selected entries plus everything nested under a selected folder.
            val targets: ArrayList<Any?> = ArrayList<Any?>()
            val size = all.size
            for (n in 0..<size) {
                val item = all.get(n) as ChildItem?
                if (this.cancelled) {
                    return
                }
                if (item == null || !ids.contains(item.id)) {
                    continue
                }
                targets.add(item)
                if (item.isDir) {
                    val prefix = item.path + "/"
                    for (m in 0..<size) {
                        val sub = all.get(m) as ChildItem
                        if ((sub.dir + "/").startsWith(prefix) && !targets.contains(sub)) {
                            targets.add(sub)
                        }
                    }
                }
            }
            if (targets.isEmpty()) {
                return
            }
            this.size = targets.size
            this.ids = ArrayList()
            val mode = this.mode
            if (mode.toInt() == 2) {
                for (item2 in targets) {
                    if (this.cancelled) {
                        return
                    }
                    val item = item2 as ChildItem?
                    if (item != null) {
                        DbBookWeb.deleteById(d.appContext, item.id)
                        step()
                    }
                }
                return
            }
            val to = this.to
            val from = this.from
            val cut = from!!.length
            for (item2 in targets) {
                if (this.cancelled) {
                    return
                }
                val item = item2 as ChildItem?
                if (item == null) {
                    continue
                }
                val cv = ContentValues()
                if (mode.toInt() != 3 && item.isDir) {
                    item.path = slash(item.path)
                    if (!item.path!!.startsWith(from)) {
                        step()
                        continue
                    }
                    item.path = trimTrailingSlash(to + item.path!!.substring(cut))
                    item.name = MainUtil.uriFileName(d.appContext, item.path)
                    item.dir = MainUtil.uriDisplayName(d.appContext, item.path)
                    cv.put("_dir", item.dir)
                    cv.put("_title", item.name)
                } else {
                    item.dir = slash(item.dir)
                    if (!item.dir!!.startsWith(from)) {
                        step()
                        continue
                    }
                    item.dir = trimTrailingSlash(to + item.dir!!.substring(cut))
                    if (mode.toInt() == 3 && TextUtils.isEmpty(item.dir)) {
                        item.dir = "/"
                    }
                    if (mode.toInt() == 3) {
                        if (item.isDir && DbBookWeb.hasFolder(d.appContext, item.dir, item.name)) {
                            // Target already has a folder of that name: drop this one.
                            DbBookWeb.deleteById(d.appContext, item.id)
                            step()
                            continue
                        }
                        val now = System.currentTimeMillis()
                        cv.put("_dir", item.dir)
                        cv.put("_time", now)
                        cv.put("_rsv4", now)
                    } else {
                        cv.put("_dir", item.dir)
                    }
                }
                DbBookWeb.updateById(d.appContext, item.id, cv)
                this.ids!!.add(item.id)
                step()
            }
        }

        fun step() {
            this.done++
            publishProgress()
        }

        public override fun onCancelled() {
            val dialogWebBookMove = this.dialogRef!!.get() as DialogWebBookMove?
            if (dialogWebBookMove != null) {
                dialogWebBookMove.moveTask = null
                val bookMoveListener = dialogWebBookMove.moveListener
                if (bookMoveListener != null) {
                    bookMoveListener.onMoved(this.ids, dialogWebBookMove.targetFolder)
                }
            }
        }

        public override fun onPostExecute() {
            val dialogWebBookMove = this.dialogRef!!.get() as DialogWebBookMove?
            if (dialogWebBookMove != null) {
                dialogWebBookMove.moveTask = null
                val bookMoveListener = dialogWebBookMove.moveListener
                if (bookMoveListener != null) {
                    bookMoveListener.onMoved(this.ids, dialogWebBookMove.targetFolder)
                }
            }
        }

        public override fun onProgressUpdate() {
            val d = this.dialogRef!!.get() as DialogWebBookMove? ?: return
            if (this.done > this.size) this.done = this.size
            d.countText = MainUtil.countLabel(this.done, this.size)
            d.progressMax = this.size
            d.progress = this.done.toFloat()
        }

        companion object {
            private fun slash(path: String?): String {
                return if (TextUtils.isEmpty(path) || "/" == path) "/" else path + "/"
            }
        }
    }

    init {
        val first = list?.firstOrNull() as ChildItem?
        val name = first?.name
        when (moveType.toInt()) {
            2 -> {
                headName = if (list!!.size == 1) name ?: "" else list.size.toString() + context.getString(AppR.string.items)
                setupHead(list)
                mainRes = AppR.string.delete
            }
            4 -> {
                headName = name ?: ""
                setupHead(list)
                mainRes = AppR.string.rename
                field = TextFieldValue(name ?: "", TextRange(0, (name ?: "").length))
            }
        }
        setSheet {
            val focus = androidx.compose.runtime.remember { FocusRequester() }
            if (moveType.toInt() == 2 || moveType.toInt() == 4) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val bmp = headBitmap
                    val mod = Modifier.size(40.dp).clip(CircleShape)
                    if (bmp != null) {
                        Image(bmp.asImageBitmap(), contentDescription = null, modifier = mod)
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
            }
            if (running) {
                Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 8.dp)) {
                    Text(stringResource(AppR.string.total), modifier = Modifier.weight(1f))
                    Text(countText)
                }
                LinearProgressIndicator(
                    progress = { (progress / progressMax.coerceAtLeast(1)).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                )
            } else if (moveType.toInt() == 4) {
                OutlinedTextField(
                    value = field,
                    onValueChange = { field = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { go() }),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).focusRequester(focus),
                )
                androidx.compose.runtime.LaunchedEffect(Unit) { focus.requestFocus() }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { if (running) cancelMove() else go() }, enabled = mainEnabled) { Text(stringResource(mainRes)) }
            }
        }
        show()
        if (moveType.toInt() == 3 && first != null) moveItemsTo(list, targetFolder, 3, name)
    }

    private fun go() {
        if (moving || running) return
        moving = true
        val list = moveItems
        if (list != null) moveItemsTo(list, targetFolder, moveType.toInt(), (list.get(0) as ChildItem).name)
        moving = false
    }

    /** Cancel: stops the running work ("Canceling"), or closes. */
    fun cancelMove() {
        if (moveTask == null) {
            dismiss()
            return
        }
        mainEnabled = false
        mainRes = AppR.string.canceling
        moveTask?.cancelled = true
        moveTask = null
    }

    /** Starts the work for mode [moveType] (rename: checks the new name first). */
    fun moveItemsTo(list: MutableList<Any?>?, targetDir: String?, moveType: Int, targetName: String?) {
        if (list == null || list.isEmpty()) return
        val from: String?
        val to: String?
        if (moveType == 3) {
            val dir = (list.get(0) as ChildItem).dir
            from = if (TextUtils.isEmpty(dir) || dir == "/") "/" else "$dir/"
            to = if (TextUtils.isEmpty(targetDir) || targetDir == "/") "/" else "$targetDir/"
        } else if (moveType == 4) {
            val name = field.text.trim { it <= ' ' }
            if (TextUtils.isEmpty(name)) {
                showToast(appContext, AppR.string.input_name)
                return
            }
            if (name == targetName) {
                showToast(appContext, AppR.string.same_name)
                return
            }
            val clean = name.replace("/", "")
            if (TextUtils.isEmpty(clean)) {
                showToast(appContext, AppR.string.input_name)
                return
            }
            val path = (list.get(0) as ChildItem).path
            val dir = (list.get(0) as ChildItem).dir
            from = if (TextUtils.isEmpty(path) || path == "/") "/" else "$path/"
            to = if (TextUtils.isEmpty(dir) || dir == "/") "/$clean/" else "$dir/$clean/"
        } else {
            from = null
            to = targetDir
        }
        moveListener?.onCancelled()
        setCanceledOnTouchOutside(false)
        running = true
        mainRes = AppR.string.cancel
        mainEnabled = true
        moveTask?.cancelled = true
        moveTask = null
        val task = DialogTask(this, list, from, to, moveType)
        moveTask = task
        task.start(appContext)
    }

    /** Header icon: one bookmark's favicon (list loader) or type icon; several items: a globe. */
    fun setupHead(list: MutableList<Any?>?) {
        if (list == null || list.isEmpty()) return
        if (list.size != 1) {
            headIcon = AppR.drawable.outline_public_black_24
            return
        }
        val item = list.get(0) as ChildItem? ?: return
        headIcon = item.iconRes
        if (item.itemType.toInt() != 11) return
        val copy = ChildItem()
        copy.listType = 17
        copy.itemType = item.itemType
        copy.path = item.filePath
        copy.filePath = item.filePath
        copy.id = item.id
        copy.position = item.position
        copy.iconColor = item.iconColor
        copy.iconRes = item.iconRes
        copy.name = item.name
        if (TextUtils.isEmpty(copy.path)) return
        val cached = MainListLoader.cachedIconFor(copy)
        if (MainUtil.isBitmapOk(cached)) {
            headBitmap = cached
            return
        }
        val ctx = appContext ?: return
        val target = View(ctx)
        target.setTag(copy.position)
        val loader = MainListLoader(ctx, false, object : ListLoadListener {
            override fun onFailed(childItem: ChildItem?, view: View?) {}
            override fun onLoaded(childItem: ChildItem?, view: View?, bitmap: Bitmap?) {
                if (MainUtil.isBitmapOk(bitmap)) headBitmap = bitmap
            }
        })
        listLoader = loader
        loader.enqueue(copy, target)
    }

    override fun cancel() {
        cancelMove()
    }

    override fun canCancelFromScrim(): Boolean = moveTask == null

    override fun dismiss() {
        moveTask?.cancelled = true
        moveTask = null
        listLoader?.release()
        listLoader = null
        moveListener = null
        super.dismiss()
    }

    companion object {
    }
}
