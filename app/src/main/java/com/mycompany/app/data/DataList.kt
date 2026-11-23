package com.mycompany.app.data

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.main.MainUtil

@Suppress("deprecation")
abstract class DataList {
    @JvmField
    var items: ArrayList<ChildItem?>? = null
    @JvmField
    var paths: ArrayList<String?>? = null
    @JvmField
    var pendingUris: ArrayList<UriItem?>? = null

    fun addItem(context: Context?, uriItem: UriItem?) {
        if (uriItem == null) {
            return
        }
        val childItemH = makeChildItem(context, uriItem)
        if (childItemH == null || TextUtils.isEmpty(childItemH.path)) {
            return
        }
        if (this.items == null) {
            this.items = ArrayList()
        }
        val arrayList = this.paths
        if (arrayList == null) {
            this.paths = ArrayList()
        } else if (arrayList.contains(childItemH.path)) {
            return
        }
        this.items!!.add(childItemH)
        this.paths!!.add(childItemH.path)
    }

    fun addUri(uriItem: UriItem?) {
        var arrayList = this.pendingUris
        if (arrayList == null) {
            arrayList = ArrayList()
            this.pendingUris = arrayList
        }
        arrayList.add(uriItem)
    }

    fun removePath(path: String?) {
        if (TextUtils.isEmpty(path)) {
            return
        }
        val arrayList = this.paths
        if (arrayList == null || arrayList.isEmpty()) {
            return
        }
        val index = arrayList.indexOf(path)
        if (index < 0) {
            return
        }
        val items = this.items
        if (items != null && index < items.size) {
            items.removeAt(index)
        }
        val paths = this.paths
        if (paths == null || index >= paths.size) {
            return
        }
        paths.removeAt(index)
    }

    fun pathCount(): Int {
        val arrayList = this.paths
        if (arrayList == null) {
            return 0
        }
        return arrayList.size
    }

    fun indexOfPath(name: String?): Int {
        if (TextUtils.isEmpty(name)) {
            return -1
        }
        val arrayList = this.paths
        if (arrayList == null || arrayList.isEmpty()) {
            return -1
        }
        return arrayList.indexOf(name)
    }

    fun itemAt(pos: Int): ChildItem? {
        val arrayList = this.items
        if (arrayList == null || pos < 0 || pos >= arrayList.size) {
            return null
        }
        return arrayList[pos]
    }

    fun itemFor(path: String?): ChildItem? {
        if (TextUtils.isEmpty(path)) {
            return null
        }
        val arrayList = this.paths
        if (arrayList == null || arrayList.isEmpty()) {
            return null
        }
        val index = arrayList.indexOf(path)
        if (index < 0) {
            return null
        }
        val items = this.items
        if (items == null || index >= items.size) {
            return null
        }
        return items[index]
    }

    abstract fun makeChildItem(context: Context?, uriItem: UriItem?): ChildItem?

    fun hasPath(path: String?): Boolean {
        if (TextUtils.isEmpty(path)) {
            return false
        }
        val arrayList = this.paths
        if (arrayList == null || arrayList.isEmpty()) {
            return false
        }
        return arrayList.contains(path)
    }

    fun clear() {
        this.items = null
        this.paths = null
    }

    fun loadSearch(context: Context?, reload: Boolean) {
        val dataSearchA: DataSearch? = DataSearch.instance(context)
        if (dataSearchA == null) {
            return
        }
        if (reload) {
            this.items = dataSearchA.items
            this.paths = dataSearchA.paths
        } else {
            this.items = null
            this.paths = null
        }
        dataSearchA.items = null
        dataSearchA.paths = null
    }

    fun updateItem(itemId: Int, url: String?, path: String?, name: String?) {
        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(path)) {
            return
        }
        val arrayList = this.items
        val paths = this.paths
        if (arrayList == null || arrayList.isEmpty() || paths == null || paths.isEmpty()) {
            return
        }
        val index = paths.indexOf(url)
        if (index < 0 || index >= arrayList.size) {
            return
        }
        val childItem = arrayList[index]!!
        childItem.path = path
        childItem.name = if (itemId == 1) MainUtil.stripExtension(name) else name
        if (!TextUtils.isEmpty(childItem.badge)) {
            childItem.badge = MainUtil.upperExtOf(name)
        }
        paths[index] = path
    }
}
