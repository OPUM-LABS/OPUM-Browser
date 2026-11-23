package com.mycompany.app.data

import android.content.Context
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class DataNews {
    @JvmField
    var items: MutableList<com.mycompany.app.quick.QuickAdapter.QuickItem?>? = null
    @JvmField
    var newsPick: String? = null
    @JvmField
    var loadedAt: Long = 0

    fun setItems(list: MutableList<com.mycompany.app.quick.QuickAdapter.QuickItem?>?): Long {
        this.items = list
        this.newsPick = null
        if (list == null || list.isEmpty()) {
            this.loadedAt = 0L
            return 0L
        }
        val now = System.currentTimeMillis()
        this.loadedAt = now
        return now
    }

    companion object {
        @JvmStatic
        fun instance(context: Context?): DataNews {
            var dataNews: DataNews?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataNews()
            }
            val dataNewsNow = mainAppO.dataNews
            if (dataNewsNow != null) {
                return dataNewsNow
            }
            synchronized(DataNews::class.java) {
                dataNews = mainAppO.dataNews
                if (dataNews == null) {
                    dataNews = DataNews()
                    mainAppO.dataNews = dataNews
                }
            }
            return dataNews!!
        }
    }
}
