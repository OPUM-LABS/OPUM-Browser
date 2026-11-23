package com.mycompany.app.data

import android.content.Context
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ChildItem

@Suppress("deprecation")
class DataSearch {
    @JvmField
    var items: ArrayList<ChildItem?>? = null
    @JvmField
    var paths: ArrayList<String?>? = null

    companion object {
        @JvmStatic
        fun instance(context: Context?): DataSearch {
            var dataSearch: DataSearch?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataSearch()
            }
            val dataSearchNow = mainAppO.dataSearch
            if (dataSearchNow != null) {
                return dataSearchNow
            }
            synchronized(DataSearch::class.java) {
                dataSearch = mainAppO.dataSearch
                if (dataSearch == null) {
                    dataSearch = DataSearch()
                    mainAppO.dataSearch = dataSearch
                }
            }
            return dataSearch!!
        }
    }
}
