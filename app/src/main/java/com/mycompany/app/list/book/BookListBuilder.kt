package com.mycompany.app.list.book

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.data.book.DataBookList
import com.mycompany.app.data.book.DataBookSearch
import com.mycompany.app.list.ListTask.ListTaskListener
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainItem.GroupItem
import ch.opumlabs.opumbrowser.R
import java.util.Collections
import java.util.function.BiConsumer
import java.util.function.BooleanSupplier
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

/**
 * Shared loader behind the simple book lists (ad/popup/link/... site lists, permissions, filters,
 * user rules, scripts, albums, PDFs, archives). The lists only differ in the cache, the table reader and the sort/group preferences, which are
 * passed in as a [Spec].
 * 
 * 
 * Flow: take the in-memory cache (or, outside search, read the table when the cache is empty),
 * mark selected rows, filter by the search query, sort, split into groups and build the
 * selection arrays, then store the result back into the cache.
 */
class BookListBuilder(
    private val task: MyAsyncTask,
    @JvmField val context: Context?,
    private val search: Boolean,
    private val query: String?,
    /** Path to highlight (just added/edited entry), or null.  */
    @JvmField val highlight: String?,
    private val spec: Spec
) {
    /** What differs between the lists.  */
    class Spec
    /**
     * @param cache      the list's in-memory cache
     * @param rows       reads the whole table, calling [.add] per row
     * @param prepare    sets the item's group / sort keys
     * @param comparator sort order from the list's preferences
     * @param grouped    whether the list is grouped (sort-by-domain mode)
     */(
        val cache: Function<Context?, DataBookList>,
        val rows: BiConsumer<Context?, BookListBuilder?>,
        val prepare: Consumer<ChildItem?>,
        val comparator: Supplier<Comparator<ChildItem?>?>,
        val grouped: BooleanSupplier
    ) {
        var matchDomain: Boolean = false

        /** Search also matches the item's domain label, not just its title.  */
        fun matchDomain(): Spec {
            this.matchDomain = true
            return this
        }
    }

    /** True until the highlighted row has been found.  */
    @JvmField
    var highlightPending: Boolean = false

    // results, copied into the list task's own fields
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
    var highlightGroups: ArrayList<Int>? = null
    @JvmField
    var highlightGroup: Int = -1
    @JvmField
    var highlightIndex: Int = -1

    init {
        this.highlightPending = !TextUtils.isEmpty(highlight)
    }

    fun cancelled(): Boolean {
        return this.task.cancelled
    }

    /** Adds a row read from the table.  */
    fun add(item: ChildItem?) {
        this.spec.prepare.accept(item)
        this.items!!.add(item)
    }

    /** Flags `item` as the highlighted row if it is the requested path (case-insensitive, first match).  */
    fun checkHighlight(item: ChildItem) {
        if (this.highlightPending && this.highlight.equals(item.path, ignoreCase = true)) {
            this.highlightPending = false
            item.isMarked = true
        }
    }

    /** Runs the load. Returns false when cancelled (results are then incomplete).  */
    fun build(listener: ListTaskListener?): Boolean {
        val hasQuery = this.search && !TextUtils.isEmpty(this.query)
        var selected: MutableList<*>? = null
        this.items = ArrayList()
        val cached: MutableList<*>? =
            if (this.search) (if (hasQuery) DataBookSearch.instance(this.context).items else null) else this.spec.cache.apply(
                this.context
            ).items
        if (cached != null && !cached.isEmpty()) {
            val selectedCount = if (selected != null) selected.size else 0
            var found = 0
            for (item2 in ArrayList(cached)) {
                if (cancelled()) {
                    return false
                }
                val item = item2 as ChildItem
                if (found < selectedCount) {
                    item.isChecked = selected!!.contains(item.id)
                    if (item.isChecked) {
                        found++
                    }
                } else {
                    item.isChecked = false
                }
                item.isMarked = false
                if (!this.search) {
                    this.spec.prepare.accept(item)
                }
                this.items!!.add(item)
            }
        }
        if (cancelled()) {
            return false
        }
        if (!this.search && this.items!!.isEmpty()) {
            this.spec.rows.accept(this.context, this)
            if (cancelled()) {
                return false
            }
        }
        this.groups = ArrayList()
        this.ids = ArrayList()
        val grouped = !this.search && this.spec.grouped.getAsBoolean()
        if (grouped) {
            val top = GroupItem()
            this.groups!!.add(top)
        }
        if (hasQuery && !this.items!!.isEmpty()) {
            val filtered: ArrayList<ChildItem?> = ArrayList()
            for (item2 in this.items!!) {
                if (cancelled()) {
                    return false
                }
                val item = item2 as ChildItem
                if (contains(item.name) || (this.spec.matchDomain && contains(item.subText))) {
                    filtered.add(item)
                }
            }
            this.items = filtered
        }
        if (!this.items!!.isEmpty() && !assign(grouped)) {
            return false
        }
        if (!grouped) {
            val all = GroupItem()
            all.itemCount = this.items!!.size
            all.checkedCount = this.checkedCount
            all.allChecked = this.checkedCount == this.items!!.size
            this.groups!!.add(all)
        }
        if (this.selecting && !this.groups!!.isEmpty()) {
            this.groupChecked = BooleanArray(this.groups!!.size)
            this.groupCheckedCounts = IntArray(this.groups!!.size)
            for (item2 in this.groups!!) {
                if (cancelled()) {
                    return false
                }
                val group = item2 as GroupItem
                this.groupChecked!![group.groupPos] = group.allChecked
                this.groupCheckedCounts!![group.groupPos] = group.checkedCount
            }
        }
        val cache = this.spec.cache.apply(this.context)
        cache.items = this.items
        cache.itemIds = this.ids
        return true
    }

    private fun contains(path: String?): Boolean {
        return !TextUtils.isEmpty(path) && path!!.lowercase().contains(this.query!!)
    }

    /** Sorts, then assigns group/position indexes, selection and highlight state to each item.  */
    private fun assign(grouped: Boolean): Boolean {
        if (!this.search) {
            try {
                Collections.sort(this.items!!, this.spec.comparator.get())
            } catch (unused: Exception) {
            }
        }
        var selectable = 0
        if (this.selecting) {
            selectable = this.items!!.size
            this.itemChecked = BooleanArray(selectable)
        }
        var groupKey: String? = null
        var groupSize = 0
        var groupChecked = 0
        var inGroup = 0
        var index = 0
        for (item2 in this.items!!) {
            if (cancelled()) {
                return false
            }
            val item = item2
            if (item == null) {
                continue
            }
            this.ids!!.add(item.id)
            var group = 0
            if (grouped) {
                if (!TextUtils.isEmpty(groupKey) && groupKey != item.groupKey) {
                    Companion.addGroup(this.groups!!, groupKey, groupSize, groupChecked, index)
                    groupSize = 0
                    groupChecked = 0
                    inGroup = 0
                }
                groupKey = item.groupKey
                groupSize++
                group = this.groups!!.size
            }
            item.groupIndex = group
            item.position = index
            if (item.isChecked && this.itemChecked != null && index < selectable) {
                this.itemChecked!![index] = true
                this.checkedCount++
                groupChecked++
            }
            if (item.isMarked) {
                if (this.highlightGroup != group) {
                    this.highlightGroup = group
                    if (this.highlightGroups == null) {
                        this.highlightGroups = ArrayList()
                    }
                    if (!this.highlightGroups!!.contains(item.groupIndex)) {
                        this.highlightGroups!!.add(item.groupIndex)
                    }
                }
                if (this.highlightIndex == -1) {
                    this.highlightIndex = item.position
                }
            }
            inGroup++
            index++
            item.lowerName = null
            item.sortExt = null
            item.groupKey = null
            item.sortName = null
        }
        if (!TextUtils.isEmpty(groupKey)) {
            Companion.addGroup(this.groups!!, groupKey, groupSize, groupChecked, index)
        }
        return true
    }

    companion object {
        /** Closes a group of `size` items ending before `end`.  */
        @JvmStatic
        fun addGroup(groups: ArrayList<GroupItem?>, title: String?, size: Int, checked: Int, end: Int) {
            val group = GroupItem()
            group.groupPos = groups.size
            group.title = title
            group.itemCount = size
            group.checkedCount = checked
            group.allChecked = checked == size
            groups.add(group)
        }

        // ---- row helpers ----
        /** A site-list row with the common display defaults.  */
        @JvmStatic
        fun siteItem(id: Long, path: String?, time: Long): ChildItem {
            val item = ChildItem()
            item.id = id
            item.path = path
            item.name = path
            item.time = time
            item.itemType = 0.toByte()
            item.iconColor = -460552
            item.iconRes = R.drawable.outline_public_black_24
            return item
        }
    }
}
