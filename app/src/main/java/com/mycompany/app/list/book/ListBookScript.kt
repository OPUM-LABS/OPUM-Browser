package com.mycompany.app.list.book

import android.content.Context
import com.mycompany.app.pref.store.ListSettings
import android.database.Cursor
import android.text.TextUtils
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.data.book.DataBookScript
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookScript
import com.mycompany.app.list.ListTask
import com.mycompany.app.main.MainApp
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
class ListBookScript : ListTask() {
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

    class ListTask(listBookScript: ListBookScript?, highlightPath: String?) : MyAsyncTask() {
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
            val weakReference: WeakReference<*> = WeakReference<Any?>(listBookScript)
            this.outerRef = weakReference
            val listBookScript = weakReference.get() as ListBookScript?
            if (listBookScript != null) {
                this.highlightPath = highlightPath
                this.searching = listBookScript.searching
                this.searchText = listBookScript.searchText
                val listTaskListener = listBookScript.taskListener
                if (listTaskListener != null) {
                    listTaskListener.onDone()
                }
            }
        }

        public override fun doInBackground() {
            this.succeeded = true
            val owner = this.outerRef.get() as ListBookScript?
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
            val listBookScript = this.outerRef.get() as ListBookScript?
            if (listBookScript == null) {
                return
            }
            listBookScript.loadTask = null
            val listTaskListener: ListTaskListener? = if (this.succeeded) listBookScript.taskListener else null
            if (this.succeeded && listTaskListener != null) {
                listTaskListener.onStart()
            }
            this.highlightPath = null
            this.groups = null
            this.items = null
        }

        public override fun onPostExecute() {
            val listBookScript = this.outerRef.get() as ListBookScript?
            if (listBookScript != null) {
                listBookScript.loadTask = null
                if (this.cancelled) {
                    return
                }
                val listTaskListener = listBookScript.taskListener
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
            Function { c: Context? -> DataBookScript.instance(c) },
            BiConsumer { context: Context?, b: BookListBuilder? ->
                Companion.readRows(
                    context,
                    b!!
                )
            },
            Consumer { childItem: ChildItem? -> Companion.applyFold(childItem!!) },
            Supplier<Comparator<ChildItem?>?> { MainUtil.childComparator(0, ListSettings.bookScrItem.value, ListSettings.bookScrRvse.value) as Comparator<ChildItem?>? },
            BooleanSupplier { false })

        @JvmStatic
        fun applyFold(childItem: ChildItem) {
            if (ListSettings.bookScrItem.value != 0 || TextUtils.isEmpty(childItem.name)) {
                return
            }
            childItem.lowerName = childItem.name!!.lowercase()
        }

        /** User scripts, using the localized name/description when it matches the app language.  */
        private fun readRows(context: Context?, builder: BookListBuilder) {
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    DbBookScript.get(context).getWritableDatabase(),
                    "DbBookScript_table",
                    arrayOf<String?>(
                        "_id",
                        "_downurl",
                        "_name",
                        "_namespace",
                        "_time",
                        "_enabled",
                        "_version",
                        "_icon",
                        "_descript",
                        "_loccode",
                        "_locname",
                        "_locdesc",
                        "_author"
                    ),
                    null,
                    null,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val colId = cursor.getColumnIndex("_id")
                    val colUrl = cursor.getColumnIndex("_downurl")
                    val colName = cursor.getColumnIndex("_name")
                    val colSpace = cursor.getColumnIndex("_namespace")
                    val colTime = cursor.getColumnIndex("_time")
                    val colEnabled = cursor.getColumnIndex("_enabled")
                    val colVersion = cursor.getColumnIndex("_version")
                    val colIcon = cursor.getColumnIndex("_icon")
                    val colDesc = cursor.getColumnIndex("_descript")
                    val colLocCode = cursor.getColumnIndex("_loccode")
                    val colLocName = cursor.getColumnIndex("_locname")
                    val colLocDesc = cursor.getColumnIndex("_locdesc")
                    val colAuthor = cursor.getColumnIndex("_author")
                    var lang: String? = null
                    var langCountry: String? = null
                    val locale = MainApp.currentLocale()
                    if (!TextUtils.isEmpty(locale.getLanguage())) {
                        lang = locale.getLanguage().lowercase()
                        val country = locale.getCountry()
                        if (!TextUtils.isEmpty(country)) {
                            langCountry = lang + "-" + country.lowercase()
                        }
                    }
                    do {
                        val id = cursor.getLong(colId)
                        val url = cursor.getString(colUrl)
                        val name = cursor.getString(colName)
                        val space = cursor.getString(colSpace)
                        if (id == 0L || TextUtils.isEmpty(url) || TextUtils.isEmpty(name) || TextUtils.isEmpty(
                                space
                            )
                        ) {
                            continue
                        }
                        val version = cursor.getString(colVersion)
                        val item = ChildItem()
                        item.id = id
                        item.path = url
                        item.name = name
                        item.namespace = space
                        item.time = cursor.getLong(colTime)
                        item.isEnabled = cursor.getInt(colEnabled) == 1
                        item.itemType = 0.toByte()
                        item.iconColor = -460552
                        item.iconRes = R.drawable.outline_extension_black_24
                        item.subText = if (TextUtils.isEmpty(version)) null else "v" + version
                        item.dir = cursor.getString(colIcon)
                        item.dirName = cursor.getString(colDesc)
                        item.author = cursor.getString(colAuthor)
                        val locCode = cursor.getString(colLocCode)
                        if (!TextUtils.isEmpty(locCode) && (locCode == langCountry || locCode == lang)) {
                            item.badge = cursor.getString(colLocName)
                            item.mimeType = cursor.getString(colLocDesc)
                        }
                        builder.checkHighlight(item)
                        builder.add(item)
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
