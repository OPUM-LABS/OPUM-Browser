package com.mycompany.app.data.book

import android.content.Context
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class DataBookFilter : DataBookList() {
    companion object {
        @JvmStatic
        fun instance(context: Context?): DataBookFilter {
            var dataBookFilter: DataBookFilter?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataBookFilter()
            }
            val bookFilter = mainAppO.bookFilter
            if (bookFilter != null) {
                return bookFilter
            }
            synchronized(DataBookFilter::class.java) {
                dataBookFilter = mainAppO.bookFilter
                if (dataBookFilter == null) {
                    dataBookFilter = DataBookFilter()
                    mainAppO.bookFilter = dataBookFilter
                }
            }
            return dataBookFilter!!
        }
    }
}
