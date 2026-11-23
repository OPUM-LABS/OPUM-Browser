package com.mycompany.app.data

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.main.MainUtil
import ch.opumlabs.opumbrowser.R

@Suppress("deprecation")
class DataCast : DataList() {
    override fun makeChildItem(context: Context?, uriItem: UriItem?): ChildItem? {
        if (uriItem == null) {
            return null
        }
        val childItem = ChildItem()
        childItem.path = uriItem.uriString
        val name = uriItem.name
        childItem.name = name
        childItem.time = uriItem.time
        childItem.size = uriItem.size
        val type = uriItem.type
        childItem.listType = type
        childItem.iconColor = -460552
        if (type == 4) {
            childItem.itemType = 1.toByte()
            childItem.iconRes = R.drawable.outline_image_black_24
        } else if (type == 6) {
            childItem.itemType = 3.toByte()
            childItem.iconRes = R.drawable.baseline_music_note_black_24
            childItem.mimeType = MainUtil.mimeForUrl(name)
        } else {
            childItem.itemType = 2.toByte()
            childItem.iconRes = R.drawable.baseline_play_arrow_black_24
            childItem.mimeType = MainUtil.mimeForUrl(name)
        }
        lowerName(childItem)
        return childItem
    }

    companion object {
        /** Lower-case name without extension, and the extension, for sorting (was `ListTaskCast.lowerName`). */
        private fun lowerName(childItem: ChildItem?) {
            if (childItem == null || TextUtils.isEmpty(childItem.name)) {
                return
            }
            val lowerCase = childItem.name!!.lowercase()
            val dotIndex = lowerCase.lastIndexOf('.')
            if (dotIndex != -1) {
                childItem.lowerName = lowerCase.substring(0, dotIndex)
                childItem.sortExt = lowerCase.substring(dotIndex + 1)
            } else {
                childItem.lowerName = lowerCase
                childItem.sortExt = null
            }
        }

        @JvmStatic
        fun instance(context: Context?): DataCast {
            var dataCast: DataCast?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataCast()
            }
            val dataCastNow = mainAppO.dataCast
            if (dataCastNow != null) {
                return dataCastNow
            }
            synchronized(DataCast::class.java) {
                dataCast = mainAppO.dataCast
                if (dataCast == null) {
                    dataCast = DataCast()
                    mainAppO.dataCast = dataCast
                }
            }
            return dataCast!!
        }
    }
}
