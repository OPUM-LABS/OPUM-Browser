package com.mycompany.app.list

import android.content.Context
import com.mycompany.app.pref.store.ListSettings
import com.mycompany.app.pref.store.PathSettings
import android.database.Cursor
import android.text.TextUtils
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.compress.Compress
import com.mycompany.app.data.DataCmp
import com.mycompany.app.data.DataSearch
import com.mycompany.app.data.DataUtil
import com.mycompany.app.db.DbCmp
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.list.ListScan.ListScanListener
import com.mycompany.app.list.ListScan.ScanTask
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainItem.GroupItem
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefList
import com.mycompany.app.pref.PrefPath
import ch.opumlabs.opumbrowser.R
import java.lang.ref.WeakReference
import java.util.Collections

@Suppress("deprecation")
class ListTaskCmp(@JvmField var context: Context?, @JvmField var listener: ListTaskListener?) : ListTask() {
    @JvmField
    var loadTask: ListTask? = null
    @JvmField
    var scanner: ListScan? = null
    @JvmField
    var scanOn: Boolean = false
    @JvmField
    var deepScan: Boolean = false
    @JvmField
    var reloadFlag: Boolean = false
    @JvmField
    var saveJob: SaveTask? = null
    @JvmField
    var searching: Boolean = false
    @JvmField
    var searchText: String? = null

    internal inner class StartLoadStep : ListScanListener {
        override fun onScanned() {
            val listTaskCmp = this@ListTaskCmp
            listTaskCmp.scanner = null
            val context = listTaskCmp.context
            if (context != null) {
                val arrayList: ArrayList<UriItem?>? = DataCmp.instance(context).pendingUris
                DataCmp.instance(context).pendingUris = null
                if (!isScanned) {
                    listTaskCmp.startLoad(false, true, null, false, arrayList, null)
                }
                isScanned = false
                if (arrayList != null && !arrayList.isEmpty()) {
                    val saveTask = listTaskCmp.saveJob
                    if (saveTask != null) {
                        saveTask.cancelled = true
                    }
                    listTaskCmp.saveJob = null
                    val saveJob = SaveTask(listTaskCmp, arrayList)
                    listTaskCmp.saveJob = saveJob
                    saveJob.start(context)
                }
            }
            val listTaskListener = listTaskCmp.listener
            if (listTaskListener != null) {
                listTaskListener.onLoading()
            }
        }

        override fun onScanFailed() {
            val listTaskCmp = this@ListTaskCmp
            listTaskCmp.scanner = null
            val context = listTaskCmp.context
            if (context != null) {
                DataCmp.instance(context).pendingUris = null
            }
            val listTaskListener = listTaskCmp.listener
            if (listTaskListener != null) {
                listTaskListener.onLoaded()
            }
        }
    }

    class ListTask(
        listTaskCmp: ListTaskCmp?,
        forceReload: Boolean,
        checkExists: Boolean,
        currentPath: String?,
        findCurrent: Boolean,
        arrayList: ArrayList<UriItem?>?,
        arrayList2: ArrayList<String?>?
    ) : MyAsyncTask() {
        @JvmField
        var searching: Boolean = false
        @JvmField
        var searchText: String? = null
        @JvmField
        var outerRef: WeakReference<*>
        @JvmField
        var succeeded: Boolean = false
        @JvmField
        var forceReload: Boolean = false
        @JvmField
        var checkExists: Boolean = false
        @JvmField
        var currentPath: String? = null
        @JvmField
        var findCurrent: Boolean = false
        @JvmField
        var groups: ArrayList<GroupItem?>? = null
        @JvmField
        var items: ArrayList<ChildItem?>? = null
        @JvmField
        var paths: ArrayList<String?>? = null
        @JvmField
        var newUris: ArrayList<UriItem?>? = null
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
        var markedPaths: ArrayList<String?>? = null
        @JvmField
        var highlightGroups: ArrayList<Int>? = null
        @JvmField
        var reloadFlag: Boolean = false
        @JvmField
        var addedCount: Int = 0
        @JvmField
        var lastGroup: Int = -1
        @JvmField
        var firstChecked: Int = -1

        init {
            val weakReference: WeakReference<*> = WeakReference<Any?>(listTaskCmp)
            this.outerRef = weakReference
            val listTaskCmp = weakReference.get() as ListTaskCmp?
            if (listTaskCmp != null) {
                this.forceReload = forceReload
                this.checkExists = checkExists
                this.currentPath = currentPath
                this.findCurrent = findCurrent
                this.newUris = arrayList
                this.markedPaths = arrayList2
                this.searching = listTaskCmp.searching
                this.searchText = listTaskCmp.searchText
                this.reloadFlag = listTaskCmp.reloadFlag
                listTaskCmp.reloadFlag = false
                val listTaskListener = listTaskCmp.listener
                if (listTaskListener != null) {
                    listTaskListener.onDone()
                }
            }
        }

        /**
         * Builds the archive list: cached entries (or search results), files found by the last scan,
         * and, when needed, the rows stored in DbCmp_table; then sorts, groups and selects them and
         * publishes the result to com.mycompany.app.data.DataCmp.
         */
        public override fun doInBackground() {
            this.succeeded = true
            val outer = this.outerRef.get() as ListTaskCmp?
            if (outer == null) {
                return
            }
            val ctx = outer.context
            if (this.cancelled) {
                return
            }
            if (this.findCurrent) {
                this.findCurrent = !TextUtils.isEmpty(this.currentPath)
            }
            val query = this.searchText
            val search = this.searching
            var hasQuery = false
            var selected: MutableList<*>? = null
            if (search) {
                hasQuery = !TextUtils.isEmpty(query)
            }
            this.items = ArrayList()
            this.paths = ArrayList()
            val cached: MutableList<ChildItem?>? = if (search)
                (if (hasQuery) DataSearch.instance(ctx).items else null)
            else
                DataCmp.instance(ctx).items
            var foundCurrent = false
            if (cached != null && !cached.isEmpty()) {
                val copy: ArrayList<ChildItem?> = ArrayList(cached)
                val selCount = if (selected != null) selected.size else 0
                val markCount = if (this.markedPaths != null) this.markedPaths!!.size else 0
                var selSeen = 0
                var markSeen = 0
                for (item2 in copy) {
                    val item = item2 as ChildItem
                    if (this.cancelled) {
                        return
                    }
                    if (this.checkExists && !MainUri.isFile(ctx, item.path)) {
                        // file is gone: forget it
                        DbCmp.removePath(ctx, item.path)
                        continue
                    }
                    if (this.findCurrent && !foundCurrent && this.currentPath == item.path) {
                        foundCurrent = true
                    }
                    if (selSeen < selCount) {
                        item.isChecked = selected!!.contains(item.path)
                        if (item.isChecked) {
                            selSeen++
                        }
                    } else {
                        item.isChecked = false
                    }
                    if (markSeen < markCount) {
                        item.isMarked = this.markedPaths!!.contains(item.path)
                        if (item.isMarked) {
                            markSeen++
                        }
                    } else {
                        item.isMarked = false
                    }
                    if (!search) {
                        applyFold(item)
                    }
                    this.items!!.add(item)
                    this.paths!!.add(item.path)
                }
            }
            if (!search && this.newUris != null && !this.newUris!!.isEmpty()) {
                // newly scanned files that are not in the list yet
                val known = this.paths!!.size
                var seen = 0
                var added: ArrayList<String?>? = null
                for (item2 in this.newUris) {
                    val uri = item2
                    if (this.cancelled) {
                        return
                    }
                    if (uri == null || !Compress.isArchiveName(uri.name)) {
                        continue
                    }
                    if (seen < known && this.paths!!.contains(uri.uriString)) {
                        seen++
                        continue
                    }
                    val item: ChildItem? = toChildItem(uri)
                    if (item == null) {
                        continue
                    }
                    this.items!!.add(item)
                    if (added == null) {
                        added = ArrayList()
                    }
                    added.add(item.path)
                    this.addedCount++
                }
                if (added != null) {
                    this.paths!!.addAll(added)
                }
            }
            if (this.cancelled) {
                return
            }
            if (!search && (this.forceReload || this.items!!.isEmpty())) {
                foundCurrent = loadStored(ctx, foundCurrent)
                if (this.cancelled) {
                    return
                }
            }
            if (this.findCurrent && !foundCurrent) {
                // the current file is not known yet: add it
                val uri = MainUri.createIn(ctx, this.currentPath, null)
                if (uri != null) {
                    DbCmp.addItem(ctx, uri)
                    this.items!!.add(toChildItem(uri))
                }
            }
            this.groups = ArrayList()
            this.paths = ArrayList()
            val grouped = !search && ListSettings.albumFold.value != 0
            if (grouped) {
                val header = GroupItem()
                this.groups!!.add(header)
            }
            if (hasQuery && !this.items!!.isEmpty()) {
                val filtered: ArrayList<ChildItem?> = ArrayList()
                for (item2 in this.items!!) {
                    val item = item2 as ChildItem
                    if (this.cancelled) {
                        return
                    }
                    if (!TextUtils.isEmpty(item.name) && item.name!!.lowercase().contains(query!!)) {
                        filtered.add(item)
                    }
                }
                this.items = filtered
            }
            if (!this.items!!.isEmpty()) {
                if (!search) {
                    try {
                        Collections.sort<ChildItem?>(
                            this.items!!, MainUtil.childComparator(
                                ListSettings.albumFold.value,
                                ListSettings.albumItem.value, ListSettings.albumRvse.value
                            )
                        )
                    } catch (unused: Exception) {
                    }
                }
                var total = 0
                if (this.selecting) {
                    total = this.items!!.size
                    this.itemChecked = BooleanArray(total)
                }
                var findCurrent = false
                if (!this.reloadFlag) {
                    if (this.currentPath == null) {
                        this.currentPath = PathSettings.cmpPath.value
                    }
                    findCurrent = !TextUtils.isEmpty(this.currentPath)
                }
                var groupCount = 0
                var groupSelected = 0
                var index = 0
                var inGroup = 0
                var groupName: String? = null
                var groupKey: String? = null
                for (item2 in this.items!!) {
                    val item = item2
                    if (this.cancelled) {
                        return
                    }
                    if (item == null) {
                        continue
                    }
                    this.paths!!.add(item.path)
                    if (grouped) {
                        if (!TextUtils.isEmpty(groupKey) && groupKey != item.groupKey) {
                            addGroup(this.groups, groupName, groupCount, groupSelected, index)
                            groupCount = 0
                            groupSelected = 0
                            inGroup = 0
                        }
                        groupKey = item.groupKey
                        groupName = item.dirName
                        groupCount++
                        item.groupIndex = if (this.groups != null) this.groups!!.size else 0
                    } else {
                        item.groupIndex = 0
                    }
                    item.position = index
                    if (findCurrent && item.path == this.currentPath) {
                        findCurrent = false
                    }
                    if (item.isChecked && this.itemChecked != null && item.position < total) {
                        this.itemChecked!![item.position] = true
                        this.checkedCount++
                        groupSelected++
                    }
                    if (item.isMarked) {
                        if (this.lastGroup != item.groupIndex) {
                            this.lastGroup = item.groupIndex
                            if (this.highlightGroups == null) {
                                this.highlightGroups = ArrayList()
                            }
                            if (!this.highlightGroups!!.contains(item.groupIndex)) {
                                this.highlightGroups!!.add(item.groupIndex)
                            }
                        }
                        if (this.firstChecked == -1) {
                            this.firstChecked = item.position
                        }
                    }
                    inGroup++
                    index++
                    item.lowerName = null
                    item.sortExt = null
                    item.groupKey = null
                    item.sortName = null
                }
                if (!TextUtils.isEmpty(groupName)) {
                    addGroup(this.groups, groupName, groupCount, groupSelected, index)
                }
            }
            if (!grouped) {
                val all = GroupItem()
                all.itemCount = this.items!!.size
                all.checkedCount = this.checkedCount
                all.allChecked = this.checkedCount == this.items!!.size
                this.groups!!.add(all)
            }
            if (this.selecting && !this.groups!!.isEmpty()) {
                val size = this.groups!!.size
                this.groupChecked = BooleanArray(size)
                this.groupCheckedCounts = IntArray(size)
                for (item2 in this.groups!!) {
                    val group = item2 as GroupItem
                    if (this.cancelled) {
                        return
                    }
                    this.groupChecked!![group.groupPos] = group.allChecked
                    this.groupCheckedCounts!![group.groupPos] = group.checkedCount
                }
            }
            val data = DataCmp.instance(ctx)
            data.items = this.items
            data.paths = this.paths
        }

        /** Adds the rows stored in DbCmp_table that are not listed yet; returns the updated "current file found" flag.  */
        fun loadStored(ctx: Context?, foundCurrent: Boolean): Boolean {
            var foundCurrent = foundCurrent
            var cursor: Cursor? = null
            try {
                cursor =
                    query(DbCmp.get(ctx).getWritableDatabase(), "DbCmp_table", null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val colId = cursor.getColumnIndex("_id")
                    val colDir = cursor.getColumnIndex("_dir")
                    val colDname = cursor.getColumnIndex("_dname")
                    val colPath = cursor.getColumnIndex("_path")
                    val colName = cursor.getColumnIndex("_name")
                    val colTime = cursor.getColumnIndex("_time")
                    val colSize = cursor.getColumnIndex("_size")
                    val colCount = cursor.getColumnIndex("_count")
                    val colIndex = cursor.getColumnIndex("_index")
                    val colPage = cursor.getColumnIndex("_page")
                    val known = if (this.forceReload) this.paths!!.size else 0
                    var seen = 0
                    do {
                        val name = cursor.getString(colName)
                        if (!Compress.isArchiveName(name)) {
                            continue
                        }
                        val path = cursor.getString(colPath)
                        if (TextUtils.isEmpty(path)) {
                            continue
                        }
                        if (seen < known && this.paths!!.contains(path)) {
                            seen++
                            continue
                        }
                        if (this.findCurrent && !foundCurrent && this.currentPath == path) {
                            foundCurrent = true
                        }
                        val item = ChildItem()
                        item.dir = cursor.getString(colDir)
                        item.dirName = cursor.getString(colDname)
                        item.path = path
                        item.name = name
                        item.id = cursor.getLong(colId)
                        item.count = cursor.getLong(colCount).toInt()
                        item.index = cursor.getLong(colIndex).toInt()
                        item.page = cursor.getInt(colPage)
                        item.time = cursor.getLong(colTime)
                        item.size = cursor.getLong(colSize)
                        item.itemType = 0
                        item.iconColor = -460552
                        item.iconRes = R.drawable.outline_folder_zip_black_24
                        if (ListSettings.albumFold.value == 4) {
                            item.badge = MainUtil.upperExtOf(item.name)
                        }
                        applyFold(item)
                        this.items!!.add(item)
                    } while (!this.cancelled && cursor.moveToNext())
                }
            } catch (unused: Exception) {
            }
            if (cursor != null) {
                cursor.close()
            }
            return foundCurrent
        }

        public override fun onCancelled() {
            val listTaskCmp = this.outerRef.get() as ListTaskCmp?
            if (listTaskCmp == null) {
                return
            }
            listTaskCmp.loadTask = null
            val listTaskListener: ListTaskListener? = if (this.succeeded) listTaskCmp.listener else null
            if (this.succeeded && listTaskListener != null) {
                listTaskListener.onStart()
            }
            this.currentPath = null
            this.groups = null
            this.items = null
            this.paths = null
            this.newUris = null
            this.groupChecked = null
            this.groupCheckedCounts = null
            this.itemChecked = null
            this.markedPaths = null
            this.highlightGroups = null
        }

        public override fun onPostExecute() {
            val listTaskCmp = this.outerRef.get() as ListTaskCmp?
            if (listTaskCmp != null) {
                listTaskCmp.loadTask = null
                if (this.cancelled) {
                    return
                }
                if (listTaskCmp.scanOn) {
                    listTaskCmp.refresh(listTaskCmp.deepScan)
                }
                val listTaskListener = listTaskCmp.listener
                if (listTaskListener != null) {
                    val listTaskConfig = ListTaskConfig()
                    listTaskConfig.groups = this.groups
                    listTaskConfig.items = this.items
                    listTaskConfig.markedIndex = this.firstChecked
                    listTaskConfig.addedCount = this.addedCount
                    listTaskListener.onConfig(listTaskConfig)
                }
                this.currentPath = null
                this.groups = null
                this.items = null
                this.paths = null
                this.newUris = null
                this.groupChecked = null
                this.groupCheckedCounts = null
                this.itemChecked = null
                this.markedPaths = null
                this.highlightGroups = null
            }
        }
    }

    class SaveTask(listTaskCmp: ListTaskCmp?, arrayList: ArrayList<UriItem?>?) : MyAsyncTask() {
        @JvmField
        var outerRef: WeakReference<*>
        @JvmField
        var uris: ArrayList<UriItem?>? = null

        init {
            val weakReference: WeakReference<*> = WeakReference<Any?>(listTaskCmp)
            this.outerRef = weakReference
            if ((weakReference.get() as ListTaskCmp?) != null) {
            this.uris = arrayList
            }
        }

        public override fun doInBackground() {
            val listTaskCmp = this.outerRef.get() as ListTaskCmp?
            val arrayList: ArrayList<UriItem?>? = if (listTaskCmp == null || this.cancelled) null else this.uris
            if (listTaskCmp == null || this.cancelled || arrayList == null || arrayList.isEmpty()) {
                return
            }
            val uris = this.uris
            val size = uris!!.size
            var index = 0
            while (index < size) {
                val rawUriItem: Any? = uris.get(index)
                index++
                val uriItem = rawUriItem as UriItem?
                if (this.cancelled) {
                    return
                }
                if (uriItem != null) {
                    DataUtil.insertUri(listTaskCmp.context, uriItem)
                }
            }
        }

        public override fun onCancelled() {
            val listTaskCmp = this.outerRef.get() as ListTaskCmp?
            if (listTaskCmp == null) {
                return
            }
            listTaskCmp.saveJob = null
            this.uris = null
        }

        public override fun onPostExecute() {
            val listTaskCmp = this.outerRef.get() as ListTaskCmp?
            if (listTaskCmp == null) {
                return
            }
            listTaskCmp.saveJob = null
            this.uris = null
        }
    }

    override fun cancel() {
        val listTask = this.loadTask
        if (listTask != null) {
            listTask.cancelled = true
        }
        this.loadTask = null
        val listScan = this.scanner
        if (listScan != null) {
            val scanTask = listScan.scanTask
            if (scanTask != null) {
                scanTask.cancelled = true
            }
            listScan.scanTask = null
        }
        this.scanner = null
    }

    override fun isAlbum(): Boolean {
        return true
    }

    override fun hasThumbs(): Boolean {
        return true
    }

    override fun isBusy(): Boolean {
        return this.scanOn || this.scanner != null
    }

    override fun refresh(deepScan: Boolean) {
        this.scanOn = false
        this.deepScan = false
        isScanned = false
        if (this.loadTask != null) {
            this.scanOn = true
            return
        }
        val listScan = this.scanner
        if (listScan != null) {
            val scanTask = listScan.scanTask
            if (scanTask != null) {
                scanTask.cancelled = true
            }
            listScan.scanTask = null
        }
        val startLoadStep: StartLoadStep = StartLoadStep()
        val context = this.context
        val scanner = ListScan(context, 3, deepScan, startLoadStep)
        this.scanner = scanner
        val scanTaskNow = scanner.scanTask
        if (scanTaskNow != null) {
            scanTaskNow.cancelled = true
        }
        scanner.scanTask = null
        val scanTask = ScanTask(scanner)
        scanner.scanTask = scanTask
        scanTask.start(context)
    }

    override fun loadPaths(arrayList: ArrayList<String?>?, checkExists: Boolean) {
        startLoad(false, checkExists, null, false, null, arrayList)
    }

    override fun load(scan: Boolean, deepScan: Boolean, reload: Boolean) {
        this.scanOn = scan && !TextUtils.isEmpty(PathSettings.scanZip.value)
        this.deepScan = deepScan
        this.reloadFlag = reload
        startLoad(true, false, null, false, null, null)
    }

    override fun search(query: String?) {
        cancel()
        this.scanOn = false
        this.deepScan = false
        this.reloadFlag = true
        this.searching = true
        this.searchText = query
        val listTask = ListTask(this, false, false, null, false, null, null)
        this.loadTask = listTask
        listTask.start(this.context)
    }

    override fun loadFolder(path: String?, force: Boolean, findCurrent: Boolean) {
        startLoad(force, false, path, findCurrent, null, null)
    }

    override fun endSearch() {
        this.searching = false
        this.searchText = null
    }

    fun startLoad(
        force: Boolean,
        checkExists: Boolean,
        path: String?,
        findCurrent: Boolean,
        list: MutableList<UriItem?>?,
        arrayList: ArrayList<String?>?
    ) {
        isScanned = false
        cancel()
        val listTask = ListTask(this, force, checkExists, path, findCurrent, list as ArrayList<UriItem?>?, arrayList)
        this.loadTask = listTask
        listTask.start(this.context)
    }

    companion object {
        @JvmField
        var isScanned: Boolean = false
        @JvmStatic
        fun toChildItem(uriItem: UriItem?): ChildItem? {
            if (uriItem == null) {
                return null
            }
            val childItem = ChildItem()
            childItem.dir = uriItem.dirPath
            childItem.dirName = uriItem.dirName
            childItem.path = uriItem.uriString
            val name = uriItem.name
            childItem.name = name
            childItem.time = uriItem.time
            childItem.size = uriItem.size
            childItem.itemType = 0.toByte()
            childItem.iconColor = -460552
            childItem.iconRes = R.drawable.outline_folder_zip_black_24
            if (ListSettings.albumFold.value == 4) {
                childItem.badge = MainUtil.upperExtOf(name)
            }
            applyFold(childItem)
            return childItem
        }

        @JvmStatic
        fun applyFold(childItem: ChildItem?) {
            if (childItem != null) {
                val fold = ListSettings.albumFold.value
                if (fold == 1) {
                    childItem.groupKey = childItem.dir
                    val dirName = childItem.dirName
                    childItem.sortName = dirName
                    if (!TextUtils.isEmpty(dirName)) {
                        childItem.sortName = childItem.sortName!!.lowercase()
                    }
                } else if (fold == 4) {
                    if (TextUtils.isEmpty(childItem.badge)) {
                        childItem.badge = MainUtil.upperExtOf(childItem.name)
                    }
                    val badge = childItem.badge
                    childItem.groupKey = badge
                    childItem.sortName = badge
                    if (!TextUtils.isEmpty(badge)) {
                        childItem.sortName = childItem.sortName!!.lowercase()
                    }
                }
                if (TextUtils.isEmpty(childItem.name)) {
                    return
                }
                val lowerCase = childItem.name!!.lowercase()
                val dotIndex = lowerCase.lastIndexOf(46.toChar())
                if (dotIndex != -1) {
                    childItem.lowerName = lowerCase.substring(0, dotIndex)
                    childItem.sortExt = lowerCase.substring(dotIndex + 1)
                } else {
                    childItem.lowerName = lowerCase
                    childItem.sortExt = null
                }
            }
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
