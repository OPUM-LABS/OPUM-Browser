package com.mycompany.app.list.book

import android.content.Context
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ListSettings
import com.mycompany.app.pref.store.SecretSettings
import android.database.Cursor
import android.text.TextUtils
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.data.book.DataBookDown
import com.mycompany.app.data.book.DataBookSearch
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookDown
import com.mycompany.app.db.book.DbBookPage
import com.mycompany.app.list.ListTask
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainItem.GroupItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefList
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSync
import ch.opumlabs.opumbrowser.R
import java.lang.ref.WeakReference
import java.util.Collections

@Suppress("deprecation")
class ListBookDown : ListTask() {
    @JvmField
    var appContext: Context? = null
    @JvmField
    var taskListener: ListTaskListener? = null
    @JvmField
    var loadTask: ListTask? = null
    @JvmField
    var searching: Boolean = false
    @JvmField
    var searchText: String? = null

    class ListTask(listBookDown: ListBookDown?, forceReload: Boolean) : MyAsyncTask() {
        @JvmField
        var outerRef: WeakReference<*>
        @JvmField
        var succeeded: Boolean = false
        @JvmField
        var forceReload: Boolean = false
        @JvmField
        var highlightPath: String? = null
        @JvmField
        var findByPath: Boolean = false
        @JvmField
        var groups: ArrayList<GroupItem?>? = null
        @JvmField
        var items: ArrayList<ChildItem?>? = null
        @JvmField
        var ids: ArrayList<Long>? = null
        @JvmField
        var groupChecked: BooleanArray? = null
        @JvmField
        var groupCheckedCounts: IntArray? = null
        @JvmField
        var itemChecked: BooleanArray? = null
        @JvmField
        var checkedCount: Int = 0
        @JvmField
        var selecting: Boolean = false
        @JvmField
        var searching: Boolean = false
        @JvmField
        var searchText: String? = null

        init {
            val weakReference: WeakReference<*> = WeakReference<Any?>(listBookDown)
            this.outerRef = weakReference
            val listBookDown = weakReference.get() as ListBookDown?
            if (listBookDown != null) {
                this.forceReload = forceReload
                this.searching = listBookDown.searching
                this.searchText = listBookDown.searchText
                val str: String? = null
                this.highlightPath = str
                this.findByPath = !TextUtils.isEmpty(str)
                val listTaskListener = listBookDown.taskListener
                if (listTaskListener != null) {
                    listTaskListener.onDone()
                }
            }
        }

        public override fun doInBackground() {
            this.succeeded = true
            val owner = this.outerRef.get() as ListBookDown?
            if (owner == null || this.cancelled) {
                return
            }
            val context = owner.appContext
            val query = this.searchText
            val search = this.searching
            val hasQuery = search && !TextUtils.isEmpty(query)
            var checked: ArrayList<*>? = null
            this.items = ArrayList()
            val minTime = if (SyncSettings.secretMode.value && SecretSettings.secretDown.value > 0) SecretSettings.secretDown.value else 0
            // Secret mode without a secret session shows nothing.
            val hidden = SyncSettings.secretMode.value && SecretSettings.secretDown.value == 0L

            if (search) {
                if (hasQuery) {
                    var all: MutableList<ChildItem?>? = DataBookSearch.instance(context).items
                    // With the "all types" filter the list view seeds this cache from the download list;
                    // with a type filter it is cleared and everything is read again here.
                    if (ListSettings.bookDownType.value != 0 && (all == null || all.isEmpty())) {
                        all = null
                        if (context != null && !hidden) {
                            all = ArrayList()
                            loadDownloads(context, minTime, false, all)
                            if (this.cancelled) {
                                return
                            }
                            loadPages(context, minTime, false, all)
                        }
                        if (this.cancelled) {
                            return
                        }
                        val cache = DataBookSearch.instance(context)
                        cache.items = all
                        cache.itemIds = null
                    }
                    if (all != null && !all.isEmpty()) {
                        for (item2 in ArrayList(all)) {
                            if (this.cancelled) {
                                return
                            }
                            val item = item2 as ChildItem
                            if (item.time >= minTime) {
                                item.isChecked = false
                                this.items!!.add(item)
                            }
                        }
                    }
                    if (this.cancelled) {
                        return
                    }
                }
            } else {
                if (!this.forceReload && !hidden) {
                    val cached: MutableList<ChildItem?>? = DataBookDown.instance(context).items
                    if (cached != null && !cached.isEmpty()) {
                        var checkedLeft = if (checked != null) checked.size else 0
                        for (item2 in ArrayList(cached)) {
                            if (this.cancelled) {
                                return
                            }
                            val item = item2 as ChildItem
                            if (item.time < minTime) {
                                continue
                            }
                            item.isChecked = checkedLeft > 0 && checked!!.contains(item.id)
                            if (item.isChecked) {
                                checkedLeft--
                            }
                            applyFold(item)
                            this.items!!.add(item)
                        }
                    }
                    if (this.cancelled) {
                        return
                    }
                }
                if (this.forceReload || this.items!!.isEmpty()) {
                    if (!hidden) {
                        loadDownloads(context, minTime, true, this.items!!)
                        if (this.cancelled) {
                            return
                        }
                    }
                    if ((ListSettings.bookDownType.value == 0 || ListSettings.bookDownType.value == 8) && !hidden) {
                        loadPages(context, minTime, true, this.items!!)
                    }
                    if (this.cancelled) {
                        return
                    }
                }
            }

            this.groups = ArrayList()
            this.ids = ArrayList()
            val grouped = !search && ListSettings.bookDownFold.value != 0
            if (grouped) {
                // Slot 0 is reserved; section headers start at index 1.
                val first = GroupItem()
                this.groups!!.add(first)
            }
            if (hasQuery && !this.items!!.isEmpty()) {
                val found: ArrayList<ChildItem?> = ArrayList()
                for (item2 in this.items!!) {
                    if (this.cancelled) {
                        return
                    }
                    val item = item2 as ChildItem
                    if (!TextUtils.isEmpty(item.name) && item.name!!.lowercase().contains(query!!)) {
                        found.add(item)
                    }
                }
                this.items = found
            }
            if (!this.items!!.isEmpty()) {
                if (!search) {
                    try {
                        Collections.sort<ChildItem?>(
                            this.items!!,
                            MainUtil.childComparator(ListSettings.bookDownFold.value, ListSettings.bookDownItem.value, ListSettings.bookDownRvse.value)
                        )
                    } catch (unused: Exception) {
                    }
                }
                var checkSize = 0
                if (this.selecting) {
                    checkSize = this.items!!.size
                    this.itemChecked = BooleanArray(checkSize)
                }
                var groupType = -1
                var groupStatus = -1
                var groupName: String? = null
                var groupKey: String? = null
                var groupCount = 0
                var groupChecked = 0
                var indexInGroup = 0
                var pos = 0
                for (item2 in this.items!!) {
                    if (this.cancelled) {
                        return
                    }
                    val item = item2
                    if (item == null) {
                        continue
                    }
                    // Saved pages share the id space with downloads; they are stored negated.
                    this.ids!!.add(if (item.listType == 8) -item.id else item.id)
                    if (grouped) {
                        if (!TextUtils.isEmpty(groupKey) && groupKey != item.groupKey) {
                            addGroup(
                                this.groups,
                                owner.groupTitle(groupType, groupStatus, groupName),
                                groupCount,
                                groupChecked,
                                pos
                            )
                            groupCount = 0
                            groupChecked = 0
                            indexInGroup = 0
                        }
                        groupKey = item.groupKey
                        groupName = item.dirName
                        groupType = if (item.listType == 2) 7 else item.listType
                        groupStatus = item.status
                        groupCount++
                        item.groupIndex = this.groups!!.size
                    } else {
                        item.groupIndex = 0
                    }
                    item.position = pos
                    if (item.isChecked && this.itemChecked != null && item.position < checkSize) {
                        this.itemChecked!![item.position] = true
                        this.checkedCount++
                        groupChecked++
                    }
                    indexInGroup++
                    pos++
                    item.lowerName = null
                    item.sortExt = null
                    item.groupKey = null
                    item.sortName = null
                }
                if (!TextUtils.isEmpty(groupName)) {
                    addGroup(
                        this.groups,
                        owner.groupTitle(groupType, groupStatus, groupName),
                        groupCount,
                        groupChecked,
                        pos
                    )
                }
            }
            if (!grouped) {
                val group = GroupItem()
                group.itemCount = this.items!!.size
                group.checkedCount = this.checkedCount
                group.allChecked = this.checkedCount == group.itemCount
                this.groups!!.add(group)
            }
            if (this.selecting && !this.groups!!.isEmpty()) {
                val size = this.groups!!.size
                this.groupChecked = BooleanArray(size)
                this.groupCheckedCounts = IntArray(size)
                for (item2 in this.groups!!) {
                    if (this.cancelled) {
                        return
                    }
                    val group = item2 as GroupItem
                    this.groupChecked!![group.groupPos] = group.allChecked
                    this.groupCheckedCounts!![group.groupPos] = group.checkedCount
                }
            }
            val cache = DataBookDown.instance(context)
            cache.items = this.items
            cache.itemIds = this.ids as MutableList<Long>?
        }

        /**
         * Reads DbBookDown rows of the current (normal / secret) mode newer than `minTime`.
         * In list mode rows are filtered by the type filter and get their sort keys.
         */
        fun loadDownloads(
            context: Context?,
            minTime: Long,
            listMode: Boolean,
            out: MutableList<ChildItem?>
        ) {
            var previous: MutableList<ChildItem?>? = null
            var previousLoaded = false
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    DbBookDown.get(context).getWritableDatabase(),
                    "DbBookDown_table",
                    null,
                    "_secret=? AND _time>?",
                    arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.dataKey(), minTime.toString()),
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val colId = cursor.getColumnIndex("_id")
                    val colStatus = cursor.getColumnIndex("_status")
                    val colType = cursor.getColumnIndex("_type")
                    val colUrl = cursor.getColumnIndex("_url")
                    val colRsv = cursor.getColumnIndex("_rsv1")
                    val colDir = cursor.getColumnIndex("_dir")
                    val colDname = cursor.getColumnIndex("_dname")
                    val colPath = cursor.getColumnIndex("_path")
                    val colName = cursor.getColumnIndex("_name")
                    val colSize = cursor.getColumnIndex("_size")
                    val colRead = cursor.getColumnIndex("_read")
                    val colTime = cursor.getColumnIndex("_time")
                    val colFast = cursor.getColumnIndex("_fast")
                    val rsv4Col = cursor.getColumnIndex("_rsv4")
                    do {
                        val path = cursor.getString(colPath)
                        if (TextUtils.isEmpty(path)) {
                            continue
                        }
                        var name = cursor.getString(colName)
                        if (TextUtils.isEmpty(name)) {
                            name = MainUtil.parentPathOf(context, path)
                        }
                        var type = cursor.getInt(colType)
                        if (type == 0) {
                            type = MainUtil.archiveIconFor(name)
                        }
                        if (listMode && ListSettings.bookDownType.value != 0 && ListSettings.bookDownType.value != type) {
                            continue
                        }
                        val item = ChildItem()
                        item.listType = type
                        item.id = cursor.getLong(colId)
                        item.status = cursor.getInt(colStatus)
                        item.url = cursor.getString(colUrl)
                        item.referer = cursor.getString(colRsv)
                        item.dir = cursor.getString(colDir)
                        item.dirName = cursor.getString(colDname)
                        item.path = path
                        item.name = name
                        item.time = cursor.getLong(colTime)
                        item.size = cursor.getLong(colSize)
                        item.readSize = cursor.getLong(colRead)
                        if (item.listType == 5) {
                            item.blockPerms = cursor.getInt(colFast)
                        }
                        if (item.status == 1) {
                            // Still downloading: take size / progress from the in-memory list.
                            if (item.size == 0L) {
                                if (!previousLoaded) {
                                    previous = DataBookDown.instance(context).items
                                    previousLoaded = true
                                }
                                if (previous != null) {
                                    for (old2 in previous) {
                                        val old = old2
                                        if (old != null && old.id == item.id) {
                                            item.size = old.size
                                            item.readSize = old.readSize
                                            break
                                        }
                                    }
                                }
                            }
                        } else if (item.status == 3 || item.status == 4 || item.status == 5) {
                            if (item.size == 0L) {
                                item.size = MainUtil.fileSizeOf(context, item.path)
                            }
                        } else if (item.status == 2) {
                            item.isSecret = cursor.getInt(rsv4Col) == 1
                        }
                        MainUtil.classifyChildIfNeeded(item)
                        if (listMode) {
                            applyFold(item)
                        }
                        out.add(item)
                    } while (!this.cancelled && cursor.moveToNext())
                }
            } catch (unused: Exception) {
            }
            if (cursor != null) {
                cursor.close()
            }
        }

        /** Reads saved web pages (DbBookPage); they appear in the list as type 8, status "done".  */
        fun loadPages(context: Context?, minTime: Long, listMode: Boolean, out: MutableList<ChildItem?>) {
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    DbBookPage.get(context).getWritableDatabase(),
                    "DbBookPage_table",
                    null,
                    "_secret=? AND _time>?",
                    arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.dataKey(), minTime.toString()),
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val colId = cursor.getColumnIndex("_id")
                    val colDir = cursor.getColumnIndex("_dir")
                    val colDname = cursor.getColumnIndex("_dname")
                    val colPath = cursor.getColumnIndex("_path")
                    val colName = cursor.getColumnIndex("_name")
                    val colUrl = cursor.getColumnIndex("_url")
                    val colTime = cursor.getColumnIndex("_time")
                    val colSize = cursor.getColumnIndex("_size")
                    do {
                        val path = cursor.getString(colPath)
                        if (TextUtils.isEmpty(path)) {
                            continue
                        }
                        var name = cursor.getString(colName)
                        if (TextUtils.isEmpty(name)) {
                            name = MainUtil.parentPathOf(context, path)
                        }
                        val item = ChildItem()
                        item.listType = 8
                        item.id = cursor.getLong(colId)
                        item.status = 3
                        item.dir = cursor.getString(colDir)
                        item.dirName = cursor.getString(colDname)
                        item.path = path
                        item.name = MainUtil.stripExtension(name)
                        item.url = cursor.getString(colUrl)
                        item.filePath = MainUtil.pageUrlOrNull(path)
                        item.time = cursor.getLong(colTime)
                        item.size = cursor.getLong(colSize)
                        item.itemType = 11.toByte()
                        item.iconColor = -460552
                        item.iconRes = R.drawable.outline_public_black_24
                        if (listMode) {
                            applyFold(item)
                        }
                        out.add(item)
                    } while (!this.cancelled && cursor.moveToNext())
                }
            } catch (unused: Exception) {
            }
            if (cursor != null) {
                cursor.close()
            }
        }

        public override fun onCancelled() {
            val listBookDown = this.outerRef.get() as ListBookDown?
            if (listBookDown == null) {
                return
            }
            listBookDown.loadTask = null
            val listTaskListener: ListTaskListener? = if (this.succeeded) listBookDown.taskListener else null
            if (this.succeeded && listTaskListener != null) {
                listTaskListener.onStart()
            }
            this.groups = null
            this.items = null
            this.ids = null
            this.groupChecked = null
            this.groupCheckedCounts = null
            this.itemChecked = null
        }

        public override fun onPostExecute() {
            val listBookDown = this.outerRef.get() as ListBookDown?
            if (listBookDown != null) {
                listBookDown.loadTask = null
                if (this.cancelled) {
                    return
                }
                val listTaskListener = listBookDown.taskListener
                if (listTaskListener != null) {
                    val listTaskConfig = ListTaskConfig()
                    if (!SyncSettings.secretMode.value || SecretSettings.secretDown.value != 0L) {
                        listTaskConfig.groups = this.groups
                        listTaskConfig.items = this.items
                        listTaskConfig.markedIndex = -1
                    }
                    listTaskListener.onConfig(listTaskConfig)
                }
                this.groups = null
                this.items = null
                this.ids = null
                this.groupChecked = null
                this.groupCheckedCounts = null
                this.itemChecked = null
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

    override fun isAlbum(): Boolean {
        return true
    }

    override fun load(scan: Boolean, deepScan: Boolean, reload: Boolean) {
        cancel()
        val listTask = ListTask(this, true)
        this.loadTask = listTask
        listTask.start(this.appContext)
    }

    override fun search(query: String?) {
        cancel()
        this.searching = true
        this.searchText = query
        val listTask = ListTask(this, true)
        this.loadTask = listTask
        listTask.start(this.appContext)
    }


    override fun loadFolder(path: String?, force: Boolean, findCurrent: Boolean) {
        cancel()
        val listTask = ListTask(this, force)
        this.loadTask = listTask
        listTask.start(this.appContext)
    }

    override fun endSearch() {
        this.searching = false
        this.searchText = null
    }

    fun groupTitle(groupType: Int, groupStatus: Int, groupName: String?): String? {
        val context = this.appContext
        if (groupStatus == 1 || groupStatus == 2 || groupStatus == 4) {
            return if (context == null) "Download" else context.getString(R.string.download)
        }
        if (ListSettings.bookDownFold.value != 3) {
            return groupName
        }
        val length = MainConst.downFilterValues.size
        var last = length - 1
        for (index in 0..<length) {
            if (groupType == MainConst.downFilterValues[index]) {
                last = index
                break
            }
        }
        val filterNames = MainConst.downFilterNames
        return context!!.getString(filterNames[last % filterNames.size])
    }

    companion object {
        @JvmStatic
        fun applyFold(childItem: ChildItem) {
            val groupKey: String?
            val fold = ListSettings.bookDownFold.value
            if (fold != 0) {
                val status = childItem.status
                if (status == 1 || status == 2 || status == 4) {
                    childItem.groupKey = "43"
                    childItem.sortName = "43"
                } else if (fold == 1) {
                    childItem.groupKey = childItem.dir
                    val dirName = childItem.dirName
                    childItem.sortName = dirName
                    if (!TextUtils.isEmpty(dirName)) {
                        childItem.sortName = childItem.sortName!!.lowercase()
                    }
                } else if (fold == 3) {
                    val listType = childItem.listType
                    if (listType == 2) {
                        groupKey = "7"
                        childItem.groupKey = "7"
                    } else {
                        groupKey = listType.toString()
                        childItem.groupKey = groupKey
                    }
                    childItem.sortName = groupKey
                }
            }
            if (ListSettings.bookDownItem.value != 0 || TextUtils.isEmpty(childItem.name)) {
                return
            }
            childItem.lowerName = childItem.name!!.lowercase()
        }

        @JvmStatic
        fun addGroup(arrayList: ArrayList<GroupItem?>?, title: String?, itemCount: Int, checkedCount: Int, firstIndex: Int) {
            if (arrayList == null) {
                return
            }
            val groupItem = GroupItem()
            groupItem.groupPos = arrayList.size
            groupItem.title = title
            groupItem.itemCount = itemCount
            groupItem.checkedCount = checkedCount
            groupItem.allChecked = checkedCount == itemCount
            arrayList.add(groupItem)
        }
    }
}
