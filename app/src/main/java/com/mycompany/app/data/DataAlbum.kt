package com.mycompany.app.data

import android.content.Context
import com.mycompany.app.list.ListTaskAlbum
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUri.UriItem

@Suppress("deprecation")
class DataAlbum : DataList() {
    override fun makeChildItem(context: Context?, uriItem: UriItem?): ChildItem? {
        return ListTaskAlbum.toChildItem(uriItem)
    }

    companion object {
        @JvmStatic
        fun instance(context: Context?): DataAlbum {
            var dataAlbum: DataAlbum?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataAlbum()
            }
            val dataAlbumNow = mainAppO.dataAlbum
            if (dataAlbumNow != null) {
                return dataAlbumNow
            }
            synchronized(DataAlbum::class.java) {
                dataAlbum = mainAppO.dataAlbum
                if (dataAlbum == null) {
                    dataAlbum = DataAlbum()
                    mainAppO.dataAlbum = dataAlbum
                }
            }
            return dataAlbum!!
        }
    }
}
