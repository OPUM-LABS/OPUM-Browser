package com.mycompany.app.data.book

import android.content.Context
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class DataBookUser : DataBookList() {
    companion object {
        @JvmStatic
        fun instance(context: Context?): DataBookUser {
            var dataBookUser: DataBookUser?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataBookUser()
            }
            val bookUser = mainAppO.bookUser
            if (bookUser != null) {
                return bookUser
            }
            synchronized(DataBookUser::class.java) {
                dataBookUser = mainAppO.bookUser
                if (dataBookUser == null) {
                    dataBookUser = DataBookUser()
                    mainAppO.bookUser = dataBookUser
                }
            }
            return dataBookUser!!
        }
    }
}
