package com.mycompany.app.data.book

import android.content.Context
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class DataBookOver : DataBookList() {
    @JvmField
    var paths: ArrayList<String?>? = null

    companion object {
        @JvmStatic
        fun instance(context: Context?): DataBookOver {
            var dataBookOver: DataBookOver?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataBookOver()
            }
            val bookOver = mainAppO.bookOver
            if (bookOver != null) {
                return bookOver
            }
            synchronized(DataBookOver::class.java) {
                dataBookOver = mainAppO.bookOver
                if (dataBookOver == null) {
                    dataBookOver = DataBookOver()
                    mainAppO.bookOver = dataBookOver
                }
            }
            return dataBookOver!!
        }
    }
}
