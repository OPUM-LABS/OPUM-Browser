package com.mycompany.app.data

import android.content.Context
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class DataTrans {
    @JvmField
    var languages: ArrayList<String?>? = null

    fun hasLanguages(): Boolean {
        try {
            val arrayList = this.languages
            return if (arrayList == null || arrayList.isEmpty()) false else true
        } catch (unused: Exception) {
            return false
        }
    }

    companion object {
        @JvmStatic
        fun instance(context: Context?): DataTrans {
            var dataTrans: DataTrans?
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return DataTrans()
            }
            val dataTransNow = mainAppO.dataTrans
            if (dataTransNow != null) {
                return dataTransNow
            }
            synchronized(DataTrans::class.java) {
                dataTrans = mainAppO.dataTrans
                if (dataTrans == null) {
                    dataTrans = DataTrans()
                    mainAppO.dataTrans = dataTrans
                }
            }
            return dataTrans!!
        }
    }
}
