package com.mycompany.app.data.book

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil

@Suppress("deprecation")
class DataBookLink : DataBookList() {
    @JvmField
    var links: ArrayList<String?>? = null

    fun addLink(path: String?) {
        if (TextUtils.isEmpty(path)) {
            return
        }
        try {
            val arrayList = this.links
            if (arrayList == null) {
                this.links = ArrayList()
            } else if (arrayList.contains(path)) {
                return
            }
            this.links!!.add(path)
        } catch (unused: Exception) {
        }
    }

    fun hasPage(pageUrl: String?): Boolean {
        try {
            val arrayList = this.links
            if (arrayList != null && !arrayList.isEmpty()) {
                val trimmedUrl = MainUtil.trimTrailingSlash(pageUrl)
                if (!TextUtils.isEmpty(trimmedUrl)) {
                    if (this.links!!.contains(trimmedUrl)) {
                        return true
                    }
                    val urlHost = MainUtil.hostOfUrl(trimmedUrl, false)
                    if (!TextUtils.isEmpty(urlHost)) {
                        return this.links!!.contains(urlHost)
                    }
                }
            }
        } catch (unused: Exception) {
        }
        return false
    }

    fun hasExact(linkHost: String?): Boolean {
        try {
            val arrayList = this.links
            if (arrayList == null || arrayList.isEmpty() || TextUtils.isEmpty(linkHost)) {
                return false
            }
            return this.links!!.contains(linkHost)
        } catch (unused: Exception) {
            return false
        }
    }

    fun hasSite(pageUrl: String?): Boolean {
        try {
            val arrayList = this.links
            if (arrayList == null || arrayList.isEmpty()) {
                return false
            }
            val trimmedUrl = MainUtil.trimTrailingSlash(pageUrl)
            if (TextUtils.isEmpty(trimmedUrl)) {
                return false
            }
            return this.links!!.contains(trimmedUrl)
        } catch (unused: Exception) {
            return false
        }
    }

    companion object {
        @JvmStatic
        fun instance(context: Context?): DataBookLink {
            var dataBookLink: DataBookLink?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataBookLink()
            }
            val bookLink = mainAppO.bookLink
            if (bookLink != null) {
                return bookLink
            }
            synchronized(DataBookLink::class.java) {
                dataBookLink = mainAppO.bookLink
                if (dataBookLink == null) {
                    dataBookLink = DataBookLink()
                    mainAppO.bookLink = dataBookLink
                }
            }
            return dataBookLink!!
        }
    }
}
