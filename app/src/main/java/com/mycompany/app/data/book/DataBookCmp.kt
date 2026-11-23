package com.mycompany.app.data.book

import android.content.Context
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class DataBookCmp : DataBookList() {
    companion object {
        @JvmStatic
        fun instance(context: Context?): DataBookCmp {
            var dataBookCmp: DataBookCmp?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataBookCmp()
            }
            val bookCmp = mainAppO.bookCmp
            if (bookCmp != null) {
                return bookCmp
            }
            synchronized(DataBookCmp::class.java) {
                dataBookCmp = mainAppO.bookCmp
                if (dataBookCmp == null) {
                    dataBookCmp = DataBookCmp()
                    mainAppO.bookCmp = dataBookCmp
                }
            }
            return dataBookCmp!!
        }
    }
}
