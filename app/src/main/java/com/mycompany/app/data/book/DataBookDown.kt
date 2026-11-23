package com.mycompany.app.data.book

import android.content.Context
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class DataBookDown : DataBookList() {
    fun updateState(status: Int, id: Long, size: Long, readSize: Long) {
        val childItemE = itemFor(id)
        if (childItemE != null) {
            childItemE.status = status
            childItemE.size = size
            childItemE.readSize = readSize
            childItemE.subText = null
            if (status != 2) {
                childItemE.isSecret = false
            }
        }
    }

    companion object {
        @JvmStatic
        fun instance(context: Context?): DataBookDown {
            var dataBookDown: DataBookDown?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataBookDown()
            }
            val bookDown = mainAppO.bookDown
            if (bookDown != null) {
                return bookDown
            }
            synchronized(DataBookDown::class.java) {
                dataBookDown = mainAppO.bookDown
                if (dataBookDown == null) {
                    dataBookDown = DataBookDown()
                    mainAppO.bookDown = dataBookDown
                }
            }
            return dataBookDown!!
        }
    }
}
