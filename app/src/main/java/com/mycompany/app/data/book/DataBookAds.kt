package com.mycompany.app.data.book

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil

@Suppress("deprecation")
class DataBookAds : DataBookList() {
    @JvmField
    var sites: ArrayList<String?>? = null

    fun addSite(path: String?) {
        if (TextUtils.isEmpty(path)) {
            return
        }
        try {
            val arrayList = this.sites
            if (arrayList == null) {
                this.sites = ArrayList()
            } else if (arrayList.contains(path)) {
                return
            }
            this.sites!!.add(path)
        } catch (unused: Exception) {
        }
    }

    fun hasPage(pageUrl: String?): Boolean {
        try {
            val arrayList = this.sites
            if (arrayList == null || arrayList.isEmpty()) {
                return false
            }
            val trimmedUrl = MainUtil.trimTrailingSlash(pageUrl)
            if (TextUtils.isEmpty(trimmedUrl)) {
                return false
            }
            return this.sites!!.contains(trimmedUrl)
        } catch (unused: Exception) {
            return false
        }
    }

    fun hasSiteOf(pageUrl: String?, pageHost: String?): Boolean {
        try {
            val arrayList = this.sites
            if (arrayList == null || arrayList.isEmpty()) {
                return false
            }
            val trimmedUrl = MainUtil.trimTrailingSlash(pageUrl)
            if (TextUtils.isEmpty(trimmedUrl)) {
                return false
            }
            if (this.sites!!.contains(trimmedUrl)) {
                return true
            }
            if (TextUtils.isEmpty(pageHost)) {
                return false
            }
            return this.sites!!.contains(pageHost)
        } catch (unused: Exception) {
            return false
        }
    }

    companion object {
        @JvmStatic
        fun instance(context: Context?): DataBookAds {
            var dataBookAds: DataBookAds?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataBookAds()
            }
            val bookAds = mainAppO.bookAds
            if (bookAds != null) {
                return bookAds
            }
            synchronized(DataBookAds::class.java) {
                dataBookAds = mainAppO.bookAds
                if (dataBookAds == null) {
                    dataBookAds = DataBookAds()
                    mainAppO.bookAds = dataBookAds
                }
            }
            return dataBookAds!!
        }
    }
}
