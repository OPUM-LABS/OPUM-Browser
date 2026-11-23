package com.mycompany.app.data.book

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil

@Suppress("deprecation")
class DataBookTmem : DataBookList() {
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

    fun hasSite(pageUrl: String?): Boolean {
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

    companion object {
        @JvmStatic
        fun instance(context: Context?): DataBookTmem {
            var dataBookTmem: DataBookTmem?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataBookTmem()
            }
            val bookTmem = mainAppO.bookTmem
            if (bookTmem != null) {
                return bookTmem
            }
            synchronized(DataBookTmem::class.java) {
                dataBookTmem = mainAppO.bookTmem
                if (dataBookTmem == null) {
                    dataBookTmem = DataBookTmem()
                    mainAppO.bookTmem = dataBookTmem
                }
            }
            return dataBookTmem!!
        }
    }
}
