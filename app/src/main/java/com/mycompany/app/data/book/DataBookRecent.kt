package com.mycompany.app.data.book

import android.content.Context
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class DataBookRecent {
    @JvmField
    var items: ArrayList<com.mycompany.app.web.WebSearchAdapter.SearchItem?>? = null
    @JvmField
    var changed: Boolean = false
    @JvmField
    var reloadNeeded: Boolean = false

    companion object {
        @JvmStatic
        fun instance(context: Context?): DataBookRecent {
            var dataBookRecent: DataBookRecent?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataBookRecent()
            }
            val bookRecent = mainAppO.bookRecent
            if (bookRecent != null) {
                return bookRecent
            }
            synchronized(DataBookRecent::class.java) {
                dataBookRecent = mainAppO.bookRecent
                if (dataBookRecent == null) {
                    dataBookRecent = DataBookRecent()
                    mainAppO.bookRecent = dataBookRecent
                }
            }
            return dataBookRecent!!
        }
    }
}
