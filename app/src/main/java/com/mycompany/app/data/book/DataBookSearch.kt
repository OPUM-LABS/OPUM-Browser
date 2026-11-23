package com.mycompany.app.data.book

import android.content.Context
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ChildItem

@Suppress("deprecation")
class DataBookSearch {
    @JvmField
    var items: MutableList<ChildItem?>? = null
    @JvmField
    var itemIds: MutableList<Long>? = null

    companion object {
        @JvmStatic
        fun instance(context: Context?): DataBookSearch {
            var dataBookSearch: DataBookSearch?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataBookSearch()
            }
            val bookSearch = mainAppO.bookSearch
            if (bookSearch != null) {
                return bookSearch
            }
            synchronized(DataBookSearch::class.java) {
                dataBookSearch = mainAppO.bookSearch
                if (dataBookSearch == null) {
                    dataBookSearch = DataBookSearch()
                    mainAppO.bookSearch = dataBookSearch
                }
            }
            return dataBookSearch!!
        }
    }
}
