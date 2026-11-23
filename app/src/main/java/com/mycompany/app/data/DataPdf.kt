package com.mycompany.app.data

import android.content.Context
import com.mycompany.app.list.ListTaskPdf
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUri.UriItem

@Suppress("deprecation")
class DataPdf : DataList() {
    override fun makeChildItem(context: Context?, uriItem: UriItem?): ChildItem? {
        return ListTaskPdf.toChildItem(uriItem)
    }

    companion object {
        @JvmStatic
        fun instance(context: Context?): DataPdf {
            var dataPdf: DataPdf?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataPdf()
            }
            val dataPdfNow = mainAppO.dataPdf
            if (dataPdfNow != null) {
                return dataPdfNow
            }
            synchronized(DataPdf::class.java) {
                dataPdf = mainAppO.dataPdf
                if (dataPdf == null) {
                    dataPdf = DataPdf()
                    mainAppO.dataPdf = dataPdf
                }
            }
            return dataPdf!!
        }
    }
}
