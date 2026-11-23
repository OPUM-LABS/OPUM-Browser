package com.mycompany.app.data

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class DataUrl {
    @JvmField
    var urls: ArrayList<String?>? = null
    @JvmField
    var thumbUrls: ArrayList<String?>? = null

    class ImgCntItem {
        @JvmField
        var jpgCount: Int = 0
        @JvmField
        var pngCount: Int = 0
        @JvmField
        var gifCount: Int = 0
        @JvmField
        var webpCount: Int = 0
        @JvmField
        var otherCount: Int = 0
        @JvmField
        var noExtCount: Int = 0
    }

    fun addUrl(url: String?, alt: String?) {
        if (TextUtils.isEmpty(url)) {
            return
        }
        val arrayList = this.urls
        if (arrayList == null || arrayList.isEmpty()) {
            return
        }
        val index = arrayList.indexOf(url)
        if (index < 0 || index >= arrayList.size) {
            return
        }
        arrayList[index] = alt
    }

    companion object {
        @JvmStatic
        fun instance(context: Context?): DataUrl {
            var dataUrl: DataUrl?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataUrl()
            }
            val dataUrlNow = mainAppO.dataUrl
            if (dataUrlNow != null) {
                return dataUrlNow
            }
            synchronized(DataUrl::class.java) {
                dataUrl = mainAppO.dataUrl
                if (dataUrl == null) {
                    dataUrl = DataUrl()
                    mainAppO.dataUrl = dataUrl
                }
            }
            return dataUrl!!
        }
    }
}
