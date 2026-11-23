package com.mycompany.app.list

import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainItem.GroupItem

@Suppress("deprecation")
open class ListTask {
    class ListTaskConfig {
        @JvmField
        var groups: MutableList<GroupItem?>? = null
        @JvmField
        var items: MutableList<ChildItem?>? = null
        @JvmField
        var markedIndex: Int = 0
        @JvmField
        var folder: String? = null
        @JvmField
        var lastIndex: Int = 0
        @JvmField
        var addedCount: Int = 0
    }

    interface ListTaskListener {
        fun onStart()

        fun onLoading()

        fun onLoaded()

        fun onDone()

        fun onConfig(listTaskConfig: ListTaskConfig?)
    }

    open class ListTaskSimpleListener : ListTaskListener {
        override fun onStart() {
        }

        override fun onLoading() {
        }

        override fun onLoaded() {
        }

        override fun onDone() {
        }

        override fun onConfig(listTaskConfig: ListTaskConfig?) {
        }
    }

    open fun cancel() {
    }

    open fun currentFolder(): String? {
        return null
    }

    open fun isAlbum(): Boolean {
        return this is ListTaskAlbum
    }

    open fun hasThumbs(): Boolean {
        return this is ListTaskAlbum
    }

    open fun isBusy(): Boolean {
        return false
    }

    open fun refresh(deepScan: Boolean) {
    }

    open fun load(scan: Boolean, deepScan: Boolean, reload: Boolean) {
    }

    open fun search(query: String?) {
    }

    open fun endSearch() {
    }

    open fun resumeTask() {
    }

    open fun loadPaths(arrayList: ArrayList<String?>?, checkExists: Boolean) {
    }


    open fun loadFolder(path: String?, force: Boolean, findCurrent: Boolean) {
    }

    open fun loadMarks(force: Boolean, folderPath: String?, checkedIds: ArrayList<Long>?, markedIds: ArrayList<Long>?) {
    }
}
