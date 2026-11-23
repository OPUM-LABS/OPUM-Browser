package com.mycompany.app.data.book

import android.content.Context
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class DataBookPdf : DataBookList() {
    companion object {
        @JvmStatic
        fun instance(context: Context?): DataBookPdf {
            var dataBookPdf: DataBookPdf?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataBookPdf()
            }
            val bookPdf = mainAppO.bookPdf
            if (bookPdf != null) {
                return bookPdf
            }
            synchronized(DataBookPdf::class.java) {
                dataBookPdf = mainAppO.bookPdf
                if (dataBookPdf == null) {
                    dataBookPdf = DataBookPdf()
                    mainAppO.bookPdf = dataBookPdf
                }
            }
            return dataBookPdf!!
        }
    }
}
