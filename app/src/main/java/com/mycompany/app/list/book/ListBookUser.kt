package com.mycompany.app.list.book

import android.content.Context
import com.mycompany.app.pref.store.ListSettings
import android.database.Cursor
import android.text.TextUtils
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.data.book.DataBookUser
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookUser
import com.mycompany.app.list.ListTask
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainItem.GroupItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefList
import ch.opumlabs.opumbrowser.R
import java.lang.ref.WeakReference
import java.util.function.BiConsumer
import java.util.function.BooleanSupplier
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

@Suppress("deprecation")
class ListBookUser : ListTask() {
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

    class ListTask(listBookUser: ListBookUser?, highlightPath: String?) : MyAsyncTask() {
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
            val weakReference: WeakReference<*> = WeakReference<Any?>(listBookUser)
            this.outerRef = weakReference
            val listBookUser = weakReference.get() as ListBookUser?
            if (listBookUser != null) {
                this.highlightPath = highlightPath
                this.searching = listBookUser.searching
                this.searchText = listBookUser.searchText
                val listTaskListener = listBookUser.taskListener
                if (listTaskListener != null) {
                    listTaskListener.onDone()
                }
            }
        }

        public override fun doInBackground() {
            this.succeeded = true
            val owner = this.outerRef.get() as ListBookUser?
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
            val listBookUser = this.outerRef.get() as ListBookUser?
            if (listBookUser == null) {
                return
            }
            listBookUser.loadTask = null
            val listTaskListener: ListTaskListener? = if (this.succeeded) listBookUser.taskListener else null
            if (this.succeeded && listTaskListener != null) {
                listTaskListener.onStart()
            }
            this.highlightPath = null
            this.groups = null
            this.items = null
        }

        public override fun onPostExecute() {
            val listBookUser = this.outerRef.get() as ListBookUser?
            if (listBookUser != null) {
                listBookUser.loadTask = null
                if (this.cancelled) {
                    return
                }
                val listTaskListener = listBookUser.taskListener
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

    override fun isAlbum(): Boolean {
        return true
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
            Function { c: Context? -> DataBookUser.instance(c) },
            BiConsumer { context: Context?, b: BookListBuilder? ->
                Companion.readRows(
                    context,
                    b!!
                )
            },
            Consumer { childItem: ChildItem? -> Companion.applyFold(childItem!!) },
            Supplier<Comparator<ChildItem?>?> { MainUtil.childComparator(ListSettings.bookUserFold.value, ListSettings.bookUserItem.value, ListSettings.bookUserRvse.value) as Comparator<ChildItem?>? },
            BooleanSupplier { ListSettings.bookUserFold.value != 0 })

        @JvmStatic
        fun applyFold(childItem: ChildItem) {
            if (ListSettings.bookUserFold.value == 6) {
                val subText = childItem.subText
                childItem.groupKey = subText
                childItem.sortName = subText
                if (!TextUtils.isEmpty(subText)) {
                    childItem.sortName = childItem.sortName!!.lowercase()
                }
            }
            if (ListSettings.bookUserItem.value != 0 || TextUtils.isEmpty(childItem.name)) {
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

        /** User filter rules: one rule per row, with its enabled flag.  */
        private fun readRows(context: Context?, builder: BookListBuilder) {
            var cursor: Cursor? = null
            try {
                cursor = query(
                    DbBookUser.get(context).getWritableDatabase(),
                    "DbBookUser_table",
                    null,
                    null,
                    null,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val colId = cursor.getColumnIndex("_id")
                    val colPath = cursor.getColumnIndex("_path")
                    val colTime = cursor.getColumnIndex("_time")
                    val colUse = cursor.getColumnIndex("_use")
                    do {
                        val path = cursor.getString(colPath)
                        if (!TextUtils.isEmpty(path)) {
                            val item: ChildItem = BookListBuilder.Companion.siteItem(
                                cursor.getLong(colId),
                                path,
                                cursor.getLong(colTime)
                            )
                            item.isEnabled = cursor.getInt(colUse) == 1
                            item.iconRes = R.drawable.outline_verified_user_black_24
                            item.subText = DbBookUser.hostOfRule(path)
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
