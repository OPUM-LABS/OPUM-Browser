package com.mycompany.app.list.book

import android.content.Context
import com.mycompany.app.pref.store.ListSettings
import android.database.Cursor
import android.text.TextUtils
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.data.book.DataBookFilter
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookFilter
import com.mycompany.app.list.ListTask
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainItem.GroupItem
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefList
import ch.opumlabs.opumbrowser.R
import java.io.File
import java.lang.ref.WeakReference
import java.util.function.BiConsumer
import java.util.function.BooleanSupplier
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

@Suppress("deprecation")
class ListBookFilter : ListTask() {
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

    class ListTask(listBookFilter: ListBookFilter?, highlightPath: String?) : MyAsyncTask() {
        @JvmField
        var outerRef: WeakReference<*>
        @JvmField
        var succeeded: Boolean = false
        @JvmField
        var highlightPath: String? = null
        @JvmField
        var groups: ArrayList<GroupItem?>? = null
        @JvmField
        var items: ArrayList<ChildItem?>? = null
        @JvmField
        var searching: Boolean = false
        @JvmField
        var searchText: String? = null
        @JvmField
        var highlightIndex: Int = -1

        init {
            val weakReference: WeakReference<*> = WeakReference<Any?>(listBookFilter)
            this.outerRef = weakReference
            val listBookFilter = weakReference.get() as ListBookFilter?
            if (listBookFilter != null) {
                this.highlightPath = highlightPath
                this.searching = listBookFilter.searching
                this.searchText = listBookFilter.searchText
                val listTaskListener = listBookFilter.taskListener
                if (listTaskListener != null) {
                    listTaskListener.onDone()
                }
            }
        }

        public override fun doInBackground() {
            this.succeeded = true
            val owner = this.outerRef.get() as ListBookFilter?
            if (owner == null || this.cancelled) {
                return
            }
            val bookListBuilder = BookListBuilder(this, owner.appContext, this.searching, this.searchText, this.highlightPath, SPEC)
            if (!bookListBuilder.build(owner.taskListener)) {
                return
            }
            this.groups = bookListBuilder.groups
            this.items = bookListBuilder.items
            this.highlightIndex = bookListBuilder.highlightIndex
        }

        public override fun onCancelled() {
            val listBookFilter = this.outerRef.get() as ListBookFilter?
            if (listBookFilter == null) {
                return
            }
            listBookFilter.loadTask = null
            val listTaskListener: ListTaskListener? = if (this.succeeded) listBookFilter.taskListener else null
            if (this.succeeded && listTaskListener != null) {
                listTaskListener.onStart()
            }
            this.highlightPath = null
            this.groups = null
            this.items = null
        }

        public override fun onPostExecute() {
            val listBookFilter = this.outerRef.get() as ListBookFilter?
            if (listBookFilter != null) {
                listBookFilter.loadTask = null
                if (this.cancelled) {
                    return
                }
                val listTaskListener = listBookFilter.taskListener
                if (listTaskListener != null) {
                    val listTaskConfig = ListTaskConfig()
                    listTaskConfig.groups = this.groups
                    listTaskConfig.items = this.items
                    listTaskConfig.markedIndex = this.highlightIndex
                    listTaskListener.onConfig(listTaskConfig)
                }
                this.highlightPath = null
                this.groups = null
                this.items = null
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

    override fun load(scan: Boolean, deepScan: Boolean, reload: Boolean) {
        cancel()
        val listTask = ListTask(this, null)
        this.loadTask = listTask
        listTask.start(this.appContext)
    }

    override fun search(query: String?) {
        cancel()
        this.searching = true
        this.searchText = query
        val listTask = ListTask(this, null)
        this.loadTask = listTask
        listTask.start(this.appContext)
    }


    override fun loadFolder(path: String?, force: Boolean, findCurrent: Boolean) {
        cancel()
        val listTask = ListTask(this, path)
        this.loadTask = listTask
        listTask.start(this.appContext)
    }

    override fun endSearch() {
        this.searching = false
        this.searchText = null
    }

    companion object {
        /** Cache, table reader and sort settings for this list; see [BookListBuilder].  */
        @JvmField
        val SPEC: BookListBuilder.Spec = BookListBuilder.Spec(
            Function { c: Context? -> DataBookFilter.instance(c) },
            BiConsumer { context: Context?, b: BookListBuilder? ->
                Companion.readRows(
                    context!!,
                    b!!
                )
            },
            Consumer { childItem: ChildItem? -> Companion.applyFold(childItem!!) },
            Supplier<Comparator<ChildItem?>?> { MainUtil.childComparator(0, ListSettings.bookFilterItem.value, ListSettings.bookFilterRvse.value) as Comparator<ChildItem?>? },
            BooleanSupplier { false })

        @JvmStatic
        fun applyFold(childItem: ChildItem) {
            val itemMode = ListSettings.bookFilterItem.value
            if ((itemMode == 0 || itemMode == 1) && !TextUtils.isEmpty(childItem.name)) {
                childItem.lowerName = childItem.name!!.lowercase()
            }
        }

        /** Ad-block filter lists: built-in, downloaded (file or content URI) and the user filter entry.  */
        private fun readRows(context: Context, builder: BookListBuilder) {
            var cursor: Cursor? = null
            try {
                cursor = query(
                    DbBookFilter.get(context).getWritableDatabase(),
                    "DbBookFilter_table",
                    null,
                    null,
                    null,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val colId = cursor.getColumnIndex("_id")
                    val colPath = cursor.getColumnIndex("_path")
                    val colTitle = cursor.getColumnIndex("_title")
                    val colTime = cursor.getColumnIndex("_time")
                    val colUse = cursor.getColumnIndex("_use")
                    do {
                        val path = cursor.getString(colPath)
                        if (!TextUtils.isEmpty(path)) {
                            val item = ChildItem()
                            item.id = cursor.getLong(colId)
                            item.path = path
                            item.time = cursor.getLong(colTime)
                            item.isEnabled = cursor.getInt(colUse) == 1
                            item.itemType = 0.toByte()
                            val icon = DbBookFilter.iconOf(path!!)
                            item.iconRes = icon
                            val branded =
                                icon == R.drawable.outline_verified_user_red_24 || icon == R.drawable.ic_adblock || icon == R.drawable.ic_adguard
                            item.iconColor = if (branded) 0 else -460552
                            if (icon == R.drawable.outline_verified_user_red_24) {
                                item.listType = 0
                            } else if (icon == R.drawable.ic_adblock) {
                                item.listType = 1
                            } else if (icon == R.drawable.ic_adguard) {
                                item.listType = 2
                            } else if (icon == R.drawable.outline_public_black_24) {
                                item.listType = 3
                            } else {
                                item.listType = 4
                            }
                            if ("sb_user_filter_path" == path) {
                                item.name = context.getString(R.string.user_filter)
                            } else if (MainUri.isContentUri(path)) {
                                item.name = cursor.getString(colTitle)
                                item.size = MainUtil.uriFileSize(context, path)
                                if (item.size == -1234L) {
                                    item.dateText = context.getString(R.string.permission_removed)
                                } else {
                                    item.subText = MainUtil.sizeLabel(item.size)
                                }
                            } else {
                                item.name = cursor.getString(colTitle)
                                item.filePath = MainUtil.filterFilePath(context, path)
                                if (!TextUtils.isEmpty(item.filePath)) {
                                    item.size = File(item.filePath!!).length()
                                    item.subText = MainUtil.sizeLabel(item.size)
                                }
                            }
                            builder.checkHighlight(item)
                            builder.add(item)
                        }
                    } while (!builder.cancelled() && cursor.moveToNext())
                }
            } catch (unused: Exception) {
            }
            if (cursor != null) {
                cursor.close()
            }
        }
    }
}
