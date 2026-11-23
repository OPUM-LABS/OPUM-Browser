package com.mycompany.app.pref

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class PrefPdf(context: Context?) : PrefCore(context, "PrefPdf") {
    companion object {
        @JvmField
        var loadedTag: String? = null
        // the values live in pref/store/PdfSettings since 2026-11-17

        @JvmStatic
        fun get(context: Context?, reload: Boolean): PrefPdf? {
            var reload = reload
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return PrefPdf(null)
            }
            if (!reload && TextUtils.isEmpty(loadedTag)) {
                reload = true
            }
            val prefPdf = mainAppO.prefPdf
            if (prefPdf == null) {
                synchronized(PrefPdf::class.java) {
                    if (mainAppO.prefPdf == null) {
                        mainAppO.prefPdf = PrefPdf(context)
                        reload = false
                    }
                }
            } else if (TextUtils.isEmpty(prefPdf.filePath)) {
                synchronized(PrefPdf::class.java) {
                    mainAppO.prefPdf!!.init(context, "PrefPdf")
                }
                reload = false
            }
            if (reload || mainAppO.prefPdf!!.notLoaded()) {
                mainAppO.prefPdf!!.load()
            }
            return mainAppO.prefPdf
        }

        /** Loads the settings ([reload] = read the file again first, e.g. after a backup restore). */
        @JvmStatic
        fun load(context: Context?, reload: Boolean) {
            if (context == null) return
            if (reload) get(context, true)
            com.mycompany.app.pref.store.PdfSettings.load(context)
        }
    }
}
