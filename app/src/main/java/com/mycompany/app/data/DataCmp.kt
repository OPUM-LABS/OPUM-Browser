package com.mycompany.app.data

import android.content.Context
import com.mycompany.app.list.ListTaskCmp
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUri.UriItem

@Suppress("deprecation")
class DataCmp : DataList() {
    override fun makeChildItem(context: Context?, uriItem: UriItem?): ChildItem? {
        return ListTaskCmp.toChildItem(uriItem)
    }

    companion object {
        @JvmStatic
        fun instance(context: Context?): DataCmp {
            var dataCmp: DataCmp?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataCmp()
            }
            val dataCmpNow = mainAppO.dataCmp
            if (dataCmpNow != null) {
                return dataCmpNow
            }
            synchronized(DataCmp::class.java) {
                dataCmp = mainAppO.dataCmp
                if (dataCmp == null) {
                    dataCmp = DataCmp()
                    mainAppO.dataCmp = dataCmp
                }
            }
            return dataCmp!!
        }
    }
}
