package com.mycompany.app.list.book

import android.text.TextUtils
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.pref.store.ListSettings

/**
 * Grouping / sort keys for the blocked-images list (Saved sites > blocked images). The old loader task that used to
 * live here was only reachable through the removed `ListTask.create` type 23; `SavedSitesActivity` loads the rows itself.
 */
object ListBookBlock {
    @JvmStatic
    fun applyFold(childItem: ChildItem?) {
        if (childItem == null) {
            return
        }
        if (ListSettings.bookBlockFold.value == 6) {
            val subText = childItem.subText
            childItem.groupKey = subText
            childItem.sortName = subText
            if (!TextUtils.isEmpty(subText)) {
                childItem.sortName = childItem.sortName!!.lowercase()
            }
        }
        if (ListSettings.bookBlockItem.value != 0 || TextUtils.isEmpty(childItem.name)) {
            return
        }
        childItem.lowerName = childItem.name!!.lowercase()
    }
}
