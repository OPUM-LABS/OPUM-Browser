package com.mycompany.app.data.book

import android.content.Context
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class DataBookScript : DataBookList() {
    companion object {
        @JvmStatic
        fun instance(context: Context?): DataBookScript {
            var dataBookScript: DataBookScript?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataBookScript()
            }
            val bookScript = mainAppO.bookScript
            if (bookScript != null) {
                return bookScript
            }
            synchronized(DataBookScript::class.java) {
                dataBookScript = mainAppO.bookScript
                if (dataBookScript == null) {
                    dataBookScript = DataBookScript()
                    mainAppO.bookScript = dataBookScript
                }
            }
            return dataBookScript!!
        }
    }
}
