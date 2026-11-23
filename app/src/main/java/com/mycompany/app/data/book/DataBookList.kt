package com.mycompany.app.data.book

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil

@Suppress("deprecation")
abstract class DataBookList {
    @JvmField
    var items: MutableList<ChildItem?>? = null
    @JvmField
    var itemIds: MutableList<Long>? = null

    fun addItem(childItem: ChildItem?) {
        if (childItem != null) {
            var id = childItem.id
            if (id > 0) {
                if (childItem.listType == 8) {
                    id = -id
                }
                if (this.items == null) {
                    this.items = ArrayList()
                }
                val list = this.itemIds
                if (list == null) {
                    this.itemIds = ArrayList()
                } else if (list.contains(id)) {
                    return
                }
                this.items!!.add(childItem)
                this.itemIds!!.add(id)
            }
        }
    }

    fun removeId(id: Long) {
        if (id == 0L) {
            return
        }
        val list = this.itemIds
        if (list == null || list.isEmpty()) {
            return
        }
        val index = list.indexOf(id)
        if (index < 0) {
            return
        }
        val items = this.items
        if (items != null && index < items.size) {
            items.removeAt(index)
        }
        val itemIds = this.itemIds
        if (itemIds == null || index >= itemIds.size) {
            return
        }
        itemIds.removeAt(index)
    }

    /** Drops the "user filter" entry from the cache.  */
    fun removeUserFilter() {
        val list = this.items
        if (list == null) {
            return
        }
        for (item in list) {
            if (item != null && "sb_user_filter_path" == item.path) {
                removeId(item.id)
                return
            }
        }
    }

    fun itemFor(id: Long): ChildItem? {
        if (id <= 0) {
            return null
        }
        val list = this.itemIds
        if (list == null || list.isEmpty()) {
            return null
        }
        val index = list.indexOf(id)
        if (index < 0) {
            return null
        }
        val items = this.items
        if (items != null && index < items.size) {
            return items[index]
        }
        return null
    }

    fun clear() {
        this.items = null
        this.itemIds = null
    }

    fun loadSearch(context: Context?, secret: Boolean) {
        val dataBookSearchA: DataBookSearch? = DataBookSearch.instance(context)
        if (dataBookSearchA == null) {
            return
        }
        if (secret) {
            this.items = dataBookSearchA.items
            this.itemIds = dataBookSearchA.itemIds
        } else {
            this.items = null
            this.itemIds = null
        }
        dataBookSearchA.items = null
        dataBookSearchA.itemIds = null
    }

    fun updateItem(itemId: Long, listType: Int, path: String?, name: String?) {
        var itemId = itemId
        val list = this.items
        val itemIds = this.itemIds
        if (itemId <= 0 || TextUtils.isEmpty(path) || list == null || list.isEmpty() || itemIds == null || itemIds.isEmpty()) {
            return
        }
        if (listType == 8) {
            itemId = -itemId
        }
        val index = itemIds.indexOf(itemId)
        if (index < 0 || index >= list.size) {
            return
        }
        val childItem = list[index]!!
        childItem.listType = listType
        childItem.path = path
        if (listType != 8) {
            childItem.name = name
        } else {
            childItem.name = MainUtil.stripExtension(name)
            childItem.filePath = MainUtil.pageUrlOrNull(path)
        }
    }

    fun saveSearch(context: Context?) {
        val dataBookSearchA: DataBookSearch = DataBookSearch.instance(context)
        val list = this.items
        val itemIds = this.itemIds
        dataBookSearchA.items = list
        dataBookSearchA.itemIds = itemIds
    }

    fun putItem(childItem: ChildItem?) {
        if (childItem == null || childItem.id <= 0) {
            return
        }
        val list = this.items
        val itemIds = this.itemIds
        if (list == null || list.isEmpty() || itemIds == null || itemIds.isEmpty()) {
            addItem(childItem)
            return
        }
        val listType = childItem.listType
        var id = childItem.id
        if (listType == 8) {
            id = -id
        }
        val index = itemIds.indexOf(id)
        if (index < 0 || index >= list.size) {
            addItem(childItem)
        } else {
            list[index] = childItem
        }
    }
}
