package com.mycompany.app.list.book

import android.content.Context
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ListSettings
import android.database.Cursor
import android.text.TextUtils
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.data.book.DataBookSearch
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookWeb
import com.mycompany.app.db.book.DbBookWeb.Companion.hasFolder
import com.mycompany.app.db.book.DbBookWeb.Companion.setOrder
import com.mycompany.app.list.ListTask
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.SortChildUser
import com.mycompany.app.pref.PrefList
import com.mycompany.app.pref.PrefSync
import ch.opumlabs.opumbrowser.R
import java.lang.ref.WeakReference
import java.util.Collections

@Suppress("deprecation")
class ListBookWeb : ListTask() {
    @JvmField
    var appContext: Context? = null
    @JvmField
    var folderPath: String? = null
    @JvmField
    var taskListener: ListTaskListener? = null
    @JvmField
    var loadTask: ListTask? = null
    @JvmField
    var lastFolder: String? = null
    @JvmField
    var cachedItems: ArrayList<ChildItem?>? = null
    @JvmField
    var searching: Boolean = false
    @JvmField
    var searchText: String? = null

    class ListTask(
        listBookWeb: ListBookWeb?,
        forceReload: Boolean,
        folder: String?,
        arrayList: ArrayList<Long>?,
        arrayList2: ArrayList<Long>?
    ) : MyAsyncTask() {
        @JvmField
        var outerRef: WeakReference<*>
        @JvmField
        var succeeded: Boolean = false
        @JvmField
        var folderLost: Boolean = false
        @JvmField
        var forceReload: Boolean = false
        @JvmField
        var folder: String? = null
        @JvmField
        var checkedIds: ArrayList<Long>? = null
        @JvmField
        var items: MutableList<ChildItem?>? = null
        @JvmField
        var itemChecked: BooleanArray? = null
        @JvmField
        var checkedCount: Int = 0
        @JvmField
        var folderCount: Int = 0
        @JvmField
        var fileCount: Int = 0
        @JvmField
        var selecting: Boolean = false
        @JvmField
        var markedIds: ArrayList<Long>? = null
        @JvmField
        var searching: Boolean = false
        @JvmField
        var searchText: String? = null
        @JvmField
        var firstIndex: Int = -1
        @JvmField
        var markedIndex: Int = -1
        @JvmField
        var lastIndex: Int = -1

        init {
            val weakReference: WeakReference<*> = WeakReference<Any?>(listBookWeb)
            this.outerRef = weakReference
            val listBookWeb = weakReference.get() as ListBookWeb?
            if (listBookWeb != null) {
                this.forceReload = forceReload
                this.folder = folder
                this.checkedIds = arrayList
                this.markedIds = arrayList2
                this.searching = listBookWeb.searching
                this.searchText = listBookWeb.searchText
                val listTaskListener = listBookWeb.taskListener
                if (listTaskListener != null) {
                    listTaskListener.onDone()
                }
            }
        }

        /**
         * Loads one bookmark folder (or, in search mode, all bookmarks filtered by the query).
         */
        public override fun doInBackground() {
            this.succeeded = true
            val owner = this.outerRef.get() as ListBookWeb?
            if (owner == null || this.cancelled) {
                return
            }
            val context = owner.appContext
            if (this.searching) {
                loadSearch(owner, context)
                return
            }
            this.folder = MainUtil.trimSlashes(this.folder)
            if (TextUtils.isEmpty(this.folder)) {
                this.folder = "/"
            } else {
                this.folder = "/" + this.folder
                if (!hasFolder(context, MainUtil.uriDisplayName(null, this.folder), MainUtil.parentPathOf(null, this.folder))) {
                    this.folder = "/"
                }
            }
            val prevFolder = owner.lastFolder
            if (this.folder != prevFolder) {
                owner.lastFolder = this.folder
                this.folderLost = true
            }
            if (this.cancelled) {
                return
            }
            this.items = ArrayList()
            var selected: MutableList<Long>? = null
            if (this.checkedIds != null) {
                this.selecting = true
                selected = this.checkedIds
            }
            val isRoot = "/" == this.folder
            if (!isRoot) {
                val up = ChildItem()
                up.rowType = 1.toByte()
                up.path = MainUtil.uriDisplayName(null, this.folder)
                up.name = ".."
                up.iconColor = 0
                up.iconRes = R.drawable.outline_shift_2_black_24
                up.isDir = true
                this.items!!.add(up)
                this.folderCount++
            }
            var items: ArrayList<ChildItem?>? = owner.cachedItems
            if (this.forceReload || this.folderLost || items == null || items.isEmpty()) {
                items = loadFolder(owner, context, this.folder, selected, this.markedIds)
                if (this.cancelled) {
                    return
                }
            }
            if (items != null && !items.isEmpty()) {
                try {
                    if (ListSettings.bookWebUser.value) {
                        Collections.sort<ChildItem?>(items, SortChildUser())
                    } else {
                        Collections.sort<ChildItem?>(items, MainUtil.childComparator(0, ListSettings.bookWebItem.value, ListSettings.bookWebRvse.value))
                    }
                } catch (unused: Exception) {
                }
                this.items!!.addAll(items)
            }
            owner.cachedItems = items
            if (this.items!!.isEmpty()) {
                return
            }
            val footer = ChildItem()
            footer.rowType = 2.toByte()
            this.items!!.add(footer)
            this.fileCount++
            var selectable = 0
            if (this.selecting) {
                selectable = this.items!!.size
                this.itemChecked = BooleanArray(selectable)
            }
            // After leaving a folder, remember it so the list can scroll back to it.
            var findPrev = false
            var target = prevFolder
            if (this.folderLost && !TextUtils.isEmpty(prevFolder)) {
                if (isRoot) {
                    findPrev = true
                    target = "/"
                } else if (this.folder == MainUtil.uriDisplayName(null, prevFolder)) {
                    findPrev = true
                }
            }
            var index = 0
            for (item2 in this.items!!) {
                if (this.cancelled) {
                    return
                }
                val item = item2
                if (item == null) {
                    continue
                }
                if (!ListSettings.bookWebUser.value) {
                    item.position = index
                } else if (item.position != index) {
                    item.position = index
                    item.order = index.toLong()
                    setOrder(context, item, index)
                }
                if (item.isDir && findPrev && item.path == target) {
                    this.lastIndex = item.position
                    findPrev = false
                }
                if (item.isChecked && this.itemChecked != null && item.position < selectable) {
                    this.itemChecked!![item.position] = true
                    this.checkedCount++
                    if (this.firstIndex == -1) {
                        this.firstIndex = item.position
                    }
                }
                if (item.isMarked && this.markedIndex == -1) {
                    this.markedIndex = item.position
                }
                item.lowerName = null
                item.sortExt = null
                item.groupKey = null
                item.sortName = null
                index++
            }
        }

        fun cancelled(owner: ListBookWeb): Boolean {
            return owner.loadTask != null && owner.loadTask!!.cancelled
        }

        /** Reads the rows of one bookmark folder. Returns null if the task was cancelled.  */
        fun loadFolder(
            owner: ListBookWeb,
            context: Context?,
            folder: String?,
            selected: MutableList<Long>?,
            marks: ArrayList<Long>?
        ): ArrayList<ChildItem?>? {
            if (context == null || TextUtils.isEmpty(folder)) {
                return null
            }
            val list: ArrayList<ChildItem?> = ArrayList()
            val selectedCount = if (selected != null) selected.size else 0
            val markCount = if (marks != null) marks.size else 0
            var found = 0
            var marked = 0
            val args = arrayOf<String?>(if (SyncSettings.secretMode.value) "1" else "0", folder)
            var cursor: Cursor? = null
            try {
                cursor = query(
                    DbBookWeb.get(context).getWritableDatabase(),
                    "DbBookWeb_table",
                    null,
                    "_secret=? AND _dir=?",
                    args,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val colId = cursor.getColumnIndex("_id")
                    val colIsDir = cursor.getColumnIndex("_isdir")
                    val colPath = cursor.getColumnIndex("_path")
                    val colTitle = cursor.getColumnIndex("_title")
                    val colTime = cursor.getColumnIndex("_time")
                    val colOrder = cursor.getColumnIndex("_rsv4")
                    do {
                        val item = ChildItem()
                        item.isDir = cursor.getInt(colIsDir) == 1
                        item.dir = folder
                        item.name = cursor.getString(colTitle)
                        if (item.isDir) {
                            item.path = if ("/" == folder) "/" + item.name else folder + "/" + item.name
                            item.subText = " "
                            item.itemType = 0.toByte()
                            item.iconColor = 0
                            item.iconRes = R.drawable.outline_folder_black_24
                        } else {
                            item.path = cursor.getString(colPath)
                            if (TextUtils.isEmpty(item.path)) {
                                continue
                            }
                            setLinkFields(item)
                        }
                        item.id = cursor.getLong(colId)
                        item.time = cursor.getLong(colTime)
                        if (ListSettings.bookWebUser.value) {
                            item.order = cursor.getLong(colOrder)
                            item.position = item.order.toInt()
                        } else {
                            lowerTitle(item)
                        }
                        if (found < selectedCount) {
                            item.isChecked = selected!!.contains(item.id)
                            if (item.isChecked) {
                                found++
                            }
                        }
                        if (marked < markCount) {
                            item.isMarked = marks!!.contains(item.id)
                            if (item.isMarked) {
                                marked++
                            }
                        }
                        list.add(item)
                    } while (!cancelled(owner) && cursor.moveToNext())
                }
            } catch (unused: Exception) {
            } finally {
                if (cursor != null) {
                    cursor.close()
                }
            }
            return if (cancelled(owner)) null else list
        }

        /** Search mode: all bookmarks (cached in DataBookSearch), filtered by title or host.  */
        fun loadSearch(owner: ListBookWeb, context: Context?) {
            val query = this.searchText
            if (TextUtils.isEmpty(query)) {
                return
            }
            this.items = DataBookSearch.instance(context).items
            if (this.items == null || this.items!!.isEmpty()) {
                var all: ArrayList<ChildItem?>? = null
                if (context != null) {
                    all = ArrayList()
                    val args = arrayOf<String?>(if (SyncSettings.secretMode.value) "1" else "0", "0")
                    var cursor: Cursor? = null
                    try {
                        cursor = query(
                            DbBookWeb.get(context).getWritableDatabase(),
                            "DbBookWeb_table",
                            null,
                            "_secret=? AND _isdir=?",
                            args,
                            "_title ASC"
                        )
                        if (cursor != null && cursor.moveToFirst()) {
                            val colId = cursor.getColumnIndex("_id")
                            val colDir = cursor.getColumnIndex("_dir")
                            val colPath = cursor.getColumnIndex("_path")
                            val colTitle = cursor.getColumnIndex("_title")
                            val colTime = cursor.getColumnIndex("_time")
                            val colOrder = cursor.getColumnIndex("_rsv4")
                            do {
                                val item = ChildItem()
                                item.isDir = false
                                item.dir = cursor.getString(colDir)
                                item.name = cursor.getString(colTitle)
                                item.path = cursor.getString(colPath)
                                if (TextUtils.isEmpty(item.path)) {
                                    continue
                                }
                                setLinkFields(item)
                                item.id = cursor.getLong(colId)
                                item.time = cursor.getLong(colTime)
                                if (ListSettings.bookWebUser.value) {
                                    item.order = cursor.getLong(colOrder)
                                    item.position = item.order.toInt()
                                } else {
                                    lowerTitle(item)
                                }
                                all.add(item)
                            } while (!cancelled(owner) && cursor.moveToNext())
                        }
                    } catch (unused: Exception) {
                    } finally {
                        if (cursor != null) {
                            cursor.close()
                        }
                    }
                    if (cancelled(owner)) {
                        all = null
                    }
                }
                this.items = all
                if (this.cancelled) {
                    return
                }
                val cache = DataBookSearch.instance(context)
                cache.items = this.items
                cache.itemIds = null
            }
            if (this.items == null || this.items!!.isEmpty()) {
                return
            }
            val filtered: ArrayList<ChildItem?> = ArrayList()
            for (item2 in this.items) {
                if (this.cancelled) {
                    return
                }
                val item = item2 as ChildItem
                if (!TextUtils.isEmpty(item.name) && item.name!!.lowercase().contains(query!!)) {
                    filtered.add(item)
                } else if (!TextUtils.isEmpty(item.subText) && item.subText!!.lowercase().contains(query!!)) {
                    filtered.add(item)
                }
            }
            this.items = filtered
            var index = 0
            for (item2 in this.items!!) {
                if (this.cancelled) {
                    return
                }
                val item = item2
                if (item != null) {
                    item.position = index++
                }
            }
        }

        public override fun onCancelled() {
            val listBookWeb = this.outerRef.get() as ListBookWeb?
            if (listBookWeb == null) {
                return
            }
            listBookWeb.loadTask = null
            val listTaskListener: ListTaskListener? = if (this.succeeded) listBookWeb.taskListener else null
            if (this.succeeded && listTaskListener != null) {
                listTaskListener.onStart()
            }
            this.folder = null
            this.checkedIds = null
            this.items = null
            this.itemChecked = null
            this.markedIds = null
        }

        public override fun onPostExecute() {
            val listBookWeb = this.outerRef.get() as ListBookWeb?
            if (listBookWeb != null) {
                listBookWeb.loadTask = null
                if (this.cancelled) {
                    return
                }
                val listTaskListener = listBookWeb.taskListener
                if (listTaskListener != null) {
                    val listTaskConfig = ListTaskConfig()
                    listTaskConfig.items = this.items
                    listTaskConfig.markedIndex = this.markedIndex
                    listTaskConfig.folder = this.folder
                    listTaskConfig.lastIndex = this.lastIndex
                    listTaskListener.onConfig(listTaskConfig)
                }
                this.folder = null
                this.checkedIds = null
                this.items = null
                this.itemChecked = null
                this.markedIds = null
            }
        }

        companion object {
            private fun setLinkFields(item: ChildItem) {
                item.subText = MainUtil.hostOfUrl(item.path, false)
                if (TextUtils.isEmpty(item.subText)) {
                    item.subText = item.path
                }
                item.filePath = MainUtil.pageUrlOrNull(item.path)
                item.itemType = 11.toByte()
                item.iconColor = -460552
                item.iconRes = R.drawable.outline_public_black_24
            }
        }
    }

    override fun cancel() {
        val listTask = this.loadTask
        if (listTask != null) {
            listTask.cancelled = true
        }
        this.loadTask = null
    }

    override fun currentFolder(): String? {
        return this.folderPath
    }

    override fun load(scan: Boolean, deepScan: Boolean, reload: Boolean) {
        loadMarks(true, this.folderPath, null, null)
    }

    override fun search(query: String?) {
        cancel()
        this.searching = true
        this.searchText = query
        val listTask = ListTask(this, false, this.folderPath, null, null)
        this.loadTask = listTask
        listTask.start(this.appContext)
    }


    override fun loadFolder(path: String?, force: Boolean, findCurrent: Boolean) {
        loadMarks(force, this.folderPath, null, null)
    }

    override fun loadMarks(force: Boolean, folderPath: String?, checkedIds: ArrayList<Long>?, markedIds: ArrayList<Long>?) {
        this.folderPath = folderPath
        cancel()
        val listTask = ListTask(this, force, folderPath, checkedIds, markedIds)
        this.loadTask = listTask
        listTask.start(this.appContext)
    }

    override fun endSearch() {
        this.searching = false
        this.searchText = null
    }

    companion object {
        @JvmStatic
        fun lowerTitle(childItem: ChildItem) {
            if (ListSettings.bookWebItem.value != 0 || TextUtils.isEmpty(childItem.name)) {
                return
            }
            childItem.lowerName = childItem.name!!.lowercase()
        }
    }
}
