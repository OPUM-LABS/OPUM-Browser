package com.mycompany.app.widget

import android.content.Context
import com.mycompany.app.pref.store.MainSettings
import android.text.TextUtils
import android.util.SparseArray
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.pref.PrefSet

@Suppress("deprecation")
class WidgetBookData {
    @JvmField
    var widgetPaths: SparseArray<String?>? = null
    @JvmField
    var widgetItems: SparseArray<MutableList<ChildItem?>?>? = null

    fun pathFor(widgetId: Int): String? {
        val sparseArray = this.widgetPaths
        if (sparseArray != null && sparseArray.size() != 0) {
            if (widgetId == 0) {
                return null
            }
            try {
                return this.widgetPaths!!.get(widgetId)
            } catch (unused: Exception) {
                return null
            }
        }
        val widgetBook = MainSettings.widgetBook.value
        val entries: Array<String>? = if (TextUtils.isEmpty(widgetBook)) null else widgetBook!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        if (TextUtils.isEmpty(widgetBook) || entries == null || entries.size == 0) {
            return null
        }
        for (entry in entries) {
            val idAndPath =
                entry.split("#".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (idAndPath.size == 2 && widgetId == MainUtil.parseColorSafe(idAndPath[0])) {
                return idAndPath[1]
            }
        }
        return null
    }

    /** Saves the widget id to folder map as "id#path,id#path" in `MainSettings.widgetBook` ("/" for the root folder).  */
    fun savePaths(context: Context?) {
        var value = ""
        if (this.widgetPaths != null && this.widgetPaths!!.size() != 0) {
            var sb: StringBuilder? = null
            val size = this.widgetPaths!!.size()
            for (index in 0..<size) {
                var id: Int = 0
                var path: String?
                try {
                    id = this.widgetPaths!!.keyAt(index)
                    path = this.widgetPaths!!.get(id)
                } catch (exception: Exception) {
                    id = 0
                    path = null
                }
                if (TextUtils.isEmpty(path)) {
                    path = "/"
                }
                if (sb == null) {
                    sb = StringBuilder()
                } else {
                    sb.append(",")
                }
                sb.append(id).append("#").append(path)
            }
            if (sb != null) {
                value = sb.toString()
            }
            if (MainUtil.sameObject(MainSettings.widgetBook.value, value)) {
                return
            }
        } else if (TextUtils.isEmpty(MainSettings.widgetBook.value)) {
            return
        }
        MainSettings.editLocal { set(MainSettings.widgetBook, value) }
        PrefSet.putString(context, 5, "mWidgetBook", value)
    }

    companion object {
        @JvmField
        var cached: WidgetBookData? = null
        @JvmStatic
        fun instance(): WidgetBookData {
            var widgetBookData: WidgetBookData?
            val existing: WidgetBookData? = cached
            if (existing != null) {
                return existing
            }
            synchronized(WidgetBookData::class.java) {
                widgetBookData = cached
                if (widgetBookData == null) {
                    widgetBookData = WidgetBookData()
                    cached = widgetBookData
                }
            }
            return widgetBookData!!
        }
    }
}
