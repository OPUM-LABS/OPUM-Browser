package com.mycompany.app.data.book

import android.content.Context
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class DataBookBlock : DataBookList() {
    companion object {
        @JvmStatic
        fun instance(context: Context?): DataBookBlock {
            var dataBookBlock: DataBookBlock?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataBookBlock()
            }
            val bookBlock = mainAppO.bookBlock
            if (bookBlock != null) {
                return bookBlock
            }
            synchronized(DataBookBlock::class.java) {
                dataBookBlock = mainAppO.bookBlock
                if (dataBookBlock == null) {
                    dataBookBlock = DataBookBlock()
                    mainAppO.bookBlock = dataBookBlock
                }
            }
            return dataBookBlock!!
        }
    }
}
