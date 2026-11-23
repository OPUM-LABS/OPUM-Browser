package com.mycompany.app.data.book

import android.content.Context
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class DataBookAlbum : DataBookList() {
    companion object {
        @JvmStatic
        fun instance(context: Context?): DataBookAlbum {
            var dataBookAlbum: DataBookAlbum?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataBookAlbum()
            }
            val bookAlbum = mainAppO.bookAlbum
            if (bookAlbum != null) {
                return bookAlbum
            }
            synchronized(DataBookAlbum::class.java) {
                dataBookAlbum = mainAppO.bookAlbum
                if (dataBookAlbum == null) {
                    dataBookAlbum = DataBookAlbum()
                    mainAppO.bookAlbum = dataBookAlbum
                }
            }
            return dataBookAlbum!!
        }
    }
}
