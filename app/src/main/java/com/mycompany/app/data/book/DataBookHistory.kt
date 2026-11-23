package com.mycompany.app.data.book

import android.content.Context
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class DataBookHistory : DataBookList() {
    companion object {
        @JvmStatic
        fun instance(context: Context?): DataBookHistory {
            var dataBookHistory: DataBookHistory?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataBookHistory()
            }
            val bookHistory = mainAppO.bookHistory
            if (bookHistory != null) {
                return bookHistory
            }
            synchronized(DataBookHistory::class.java) {
                dataBookHistory = mainAppO.bookHistory
                if (dataBookHistory == null) {
                    dataBookHistory = DataBookHistory()
                    mainAppO.bookHistory = dataBookHistory
                }
            }
            return dataBookHistory!!
        }
    }
}
