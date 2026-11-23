package com.mycompany.app.list.book

import android.content.Context
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ListSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.BrowserSettings
import android.database.Cursor
import android.text.TextUtils
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.data.book.DataBookHistory
import com.mycompany.app.data.book.DataBookSearch
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookHistory
import com.mycompany.app.list.ListTask
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainItem.GroupItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefList
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefWeb
import ch.opumlabs.opumbrowser.R
import java.lang.ref.WeakReference
import java.util.Collections

@Suppress("deprecation")
class ListBookHistory : ListTask() {
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
    @JvmField
    var todayText: String? = null

    class ListTask(listBookHistory: ListBookHistory?) : MyAsyncTask() {
        @JvmField
        var outerRef: WeakReference<*>
        @JvmField
        var succeeded: Boolean = false
        @JvmField
        var groups: ArrayList<GroupItem?>? = null
        @JvmField
        var filtered: ArrayList<ChildItem?>? = null
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
            val weakReference: WeakReference<*> = WeakReference<Any?>(listBookHistory)
            this.outerRef = weakReference
            val listBookHistory = weakReference.get() as ListBookHistory?
            if (listBookHistory != null) {
                this.searching = listBookHistory.searching
                this.searchText = listBookHistory.searchText
                val listTaskListener = listBookHistory.taskListener
                if (listTaskListener != null) {
                    listTaskListener.onDone()
                }
            }
        }

        /**
         * Loads the history list (from the in-memory cache, or the database on first use),
         * applies the search filter, sorts it and builds the date groups and selection state.
         */
        public override fun doInBackground() {
            this.succeeded = true
            val owner = this.outerRef.get() as ListBookHistory?
            if (owner == null || this.cancelled) {
                return
            }
            val context = owner.appContext
            if (ListSettings.bookHistFold.value == 5 && owner.todayText == null) {
                owner.todayText = MainUtil.datePattern("yyyy.MM.dd")
            }
            val query = this.searchText
            val search = this.searching
            var hasQuery = false
            var selected: MutableList<*>? = null
            if (search) {
                hasQuery = !TextUtils.isEmpty(query)
            }
            this.filtered = ArrayList()
            val enabled = BrowserSettings.historyTime.value != 0 && (!SyncSettings.secretMode.value || SecretSettings.secretHist.value != 0L)
            var minTime: Long = 0
            if (enabled) {
                if (BrowserSettings.historyTime.value != -1) {
                    minTime = System.currentTimeMillis() - (BrowserSettings.historyTime.value.toLong()) * 86400000L
                    if (SyncSettings.secretMode.value && SecretSettings.secretHist.value > minTime) {
                        minTime = SecretSettings.secretHist.value
                    }
                }
                val cached: MutableList<*>? =
                    if (search) (if (hasQuery) DataBookSearch.instance(context).items else null) else DataBookHistory.instance(
                        context
                    ).items
                if (cached != null && !cached.isEmpty()) {
                    val selectedCount = if (selected != null) selected.size else 0
                    var found = 0
                    for (item2 in ArrayList(cached)) {
                        if (this.cancelled) {
                            return
                        }
                        val item = item2 as ChildItem
                        if (item.time < minTime) {
                            continue
                        }
                        if (found < selectedCount) {
                            item.isChecked = selected!!.contains(item.id)
                            if (item.isChecked) {
                                found++
                            }
                        } else {
                            item.isChecked = false
                        }
                        if (!search) {
                            applyFold(item, owner.todayText)
                        }
                        this.filtered!!.add(item)
                    }
                }
            }
            if (this.cancelled) {
                return
            }
            if (!search && this.filtered!!.isEmpty() && enabled) {
                loadFromDb(context, owner, minTime)
                if (this.cancelled) {
                    return
                }
            }
            this.groups = ArrayList()
            this.ids = ArrayList()
            val grouped = !search && ListSettings.bookHistFold.value != 0
            if (grouped) {
                val top = GroupItem()
                this.groups!!.add(top)
            }
            if (hasQuery && !this.filtered!!.isEmpty()) {
                val filtered: ArrayList<ChildItem?> = ArrayList()
                for (item2 in this.filtered!!) {
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
                this.filtered = filtered
            }
            if (!this.filtered!!.isEmpty()) {
                if (!search) {
                    try {
                        Collections.sort<ChildItem?>(
                            this.filtered!!,
                            MainUtil.childComparator(ListSettings.bookHistFold.value, ListSettings.bookHistItem.value, ListSettings.bookHistRvse.value)
                        )
                    } catch (unused: Exception) {
                    }
                }
                var selectable = 0
                if (this.selecting) {
                    selectable = this.filtered!!.size
                    this.itemChecked = BooleanArray(selectable)
                }
                var group: String? = null
                var groupCount = 0
                var groupSelected = 0
                var inGroup = 0
                var index = 0
                for (item2 in this.filtered!!) {
                    if (this.cancelled) {
                        return
                    }
                    val item = item2
                    if (item == null) {
                        continue
                    }
                    this.ids!!.add(item.id)
                    if (grouped) {
                        if (!TextUtils.isEmpty(group) && group != item.groupKey) {
                            addGroup(this.groups, group, groupCount, groupSelected, index)
                            groupCount = 0
                            groupSelected = 0
                            inGroup = 0
                        }
                        group = item.groupKey
                        groupCount++
                        item.groupIndex = if (this.groups != null) this.groups!!.size else 0
                    } else {
                        item.groupIndex = 0
                    }
                    item.position = index
                    if (item.isChecked && this.itemChecked != null && index < selectable) {
                        this.itemChecked!![index] = true
                        this.checkedCount++
                        groupSelected++
                    }
                    inGroup++
                    index++
                    item.lowerName = null
                    item.sortExt = null
                    item.groupKey = null
                    item.sortName = null
                }
                if (!TextUtils.isEmpty(group)) {
                    addGroup(this.groups, group, groupCount, groupSelected, index)
                }
            }
            if (!grouped) {
                val all = GroupItem()
                all.itemCount = this.filtered!!.size
                all.checkedCount = this.checkedCount
                all.allChecked = this.checkedCount == this.filtered!!.size
                this.groups!!.add(all)
            }
            if (this.selecting && !this.groups!!.isEmpty()) {
                val size = this.groups!!.size
                this.groupChecked = BooleanArray(size)
                this.groupCheckedCounts = IntArray(size)
                for (item2 in this.groups!!) {
                    if (this.cancelled) {
                        return
                    }
                    val groupItem = item2 as GroupItem
                    this.groupChecked!![groupItem.groupPos] = groupItem.allChecked
                    this.groupCheckedCounts!![groupItem.groupPos] = groupItem.checkedCount
                }
            }
            val data = DataBookHistory.instance(context)
            data.items = this.filtered
            data.itemIds = this.ids
        }

        /** Reads history rows newer than `minTime` into [.j].  */
        fun loadFromDb(context: Context?, owner: ListBookHistory, minTime: Long) {
            val args = arrayOf<String?>(if (SyncSettings.secretMode.value) "1" else "0", minTime.toString())
            var cursor: Cursor? = null
            try {
                cursor = query(
                    DbBookHistory.get(context).getWritableDatabase(),
                    "DbBookHistory_table",
                    null,
                    "_secret=? AND _time>?",
                    args,
                    null
                )
                if (cursor == null || !cursor.moveToFirst()) {
                    return
                }
                val colId = cursor.getColumnIndex("_id")
                val colPath = cursor.getColumnIndex("_path")
                val colTitle = cursor.getColumnIndex("_title")
                val colTime = cursor.getColumnIndex("_time")
                do {
                    val path = cursor.getString(colPath)
                    if (!TextUtils.isEmpty(path)) {
                        val item = ChildItem()
                        item.id = cursor.getLong(colId)
                        item.path = path
                        item.filePath = MainUtil.pageUrlOrNull(path)
                        item.time = cursor.getLong(colTime)
                        item.name = cursor.getString(colTitle)
                        item.itemType = 11.toByte()
                        item.iconColor = -460552
                        item.subText = Companion.hostLabel(path!!)
                        if (path.startsWith("file:///")) {
                            item.iconRes =
                                if ("file:///android_asset/shortcut.html" == path) R.drawable.outline_home_black_24 else R.drawable.outline_offline_pin_black_24
                        } else {
                            item.iconRes = R.drawable.outline_public_black_24
                        }
                        applyFold(item, owner.todayText)
                        this.filtered!!.add(item)
                    }
                } while (!this.cancelled && cursor.moveToNext())
            } catch (unused: Exception) {
            } finally {
                if (cursor != null) {
                    cursor.close()
                }
            }
        }

        public override fun onCancelled() {
            val listBookHistory = this.outerRef.get() as ListBookHistory?
            if (listBookHistory == null) {
                return
            }
            listBookHistory.loadTask = null
            val listTaskListener: ListTaskListener? = if (this.succeeded) listBookHistory.taskListener else null
            if (this.succeeded && listTaskListener != null) {
                listTaskListener.onStart()
            }
            this.groups = null
            this.filtered = null
            this.ids = null
            this.groupChecked = null
            this.groupCheckedCounts = null
            this.itemChecked = null
        }

        public override fun onPostExecute() {
            val listBookHistory = this.outerRef.get() as ListBookHistory?
            if (listBookHistory != null) {
                listBookHistory.loadTask = null
                if (this.cancelled) {
                    return
                }
                val listTaskListener = listBookHistory.taskListener
                if (listTaskListener != null) {
                    val listTaskConfig = ListTaskConfig()
                    if (BrowserSettings.historyTime.value != 0 && (!SyncSettings.secretMode.value || SecretSettings.secretHist.value != 0L)) {
                        listTaskConfig.groups = this.groups
                        listTaskConfig.items = this.filtered
                        listTaskConfig.markedIndex = -1
                    }
                    listTaskListener.onConfig(listTaskConfig)
                }
                this.groups = null
                this.filtered = null
                this.ids = null
                this.groupChecked = null
                this.groupCheckedCounts = null
                this.itemChecked = null
            }
        }

        companion object {
            /** Host of the URL, else the part before the first path slash, else the first 20 characters.  */
            private fun hostLabel(path: String): String {
                val host = MainUtil.hostOfUrl(path, false)
                if (!TextUtils.isEmpty(host)) {
                    return host!!
                }
                var prefix: String? = null
                if (path.length >= 6) {
                    val slash = path.indexOf(47.toChar(), 5)
                    if (slash >= 5) {
                        prefix = path.substring(0, slash)
                    }
                }
                if (!TextUtils.isEmpty(prefix)) {
                    return prefix!!
                }
                return if (path.length > 20) path.substring(0, 20) else path
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
        val listTask = ListTask(this)
        this.loadTask = listTask
        listTask.start(this.appContext)
    }

    override fun search(query: String?) {
        cancel()
        this.searching = true
        this.searchText = query
        val listTask = ListTask(this)
        this.loadTask = listTask
        listTask.start(this.appContext)
    }


    override fun loadFolder(path: String?, force: Boolean, findCurrent: Boolean) {
        cancel()
        val listTask = ListTask(this)
        this.loadTask = listTask
        listTask.start(this.appContext)
    }

    override fun endSearch() {
        this.searching = false
        this.searchText = null
    }

    companion object {
        @JvmStatic
        fun applyFold(childItem: ChildItem, todayText: String?) {
            val fold = ListSettings.bookHistFold.value
            if (fold == 6) {
                val subText = childItem.subText
                childItem.groupKey = subText
                childItem.sortName = subText
                if (!TextUtils.isEmpty(subText)) {
                    childItem.sortName = childItem.sortName!!.lowercase()
                }
            } else if (fold == 5) {
                val dayText = MainUtil.formatDate(childItem.time, todayText, "yyyy.MM.dd")
                childItem.groupKey = dayText
                childItem.sortName = dayText
            }
            if (ListSettings.bookHistItem.value != 0 || TextUtils.isEmpty(childItem.name)) {
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
