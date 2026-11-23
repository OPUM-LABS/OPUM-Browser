package com.mycompany.app.pref

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class PrefFloat(context: Context?) : PrefCore(context, "PrefFloat") {
    companion object {
        // the stored values live in pref/store/FloatSettings since 2026-11-17
        @JvmField
        var flt3Argb: Int = 0
        @JvmField
        var loadedTag: String? = null
        @JvmField
        var flt1Argb: Int = 0
        @JvmField
        var flt2Argb: Int = 0

        @JvmStatic
        fun get(context: Context?): PrefFloat? {
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return PrefFloat(null)
            }
            var loadedTagEmpty = TextUtils.isEmpty(loadedTag)
            val prefFloat = mainAppO.prefFloat
            if (prefFloat == null) {
                synchronized(PrefFloat::class.java) {
                    if (mainAppO.prefFloat == null) {
                        mainAppO.prefFloat = PrefFloat(context)
                        loadedTagEmpty = false
                    }
                }
            } else if (TextUtils.isEmpty(prefFloat.filePath)) {
                synchronized(PrefFloat::class.java) {
                    mainAppO.prefFloat!!.init(context, "PrefFloat")
                }
                loadedTagEmpty = false
            }
            if (loadedTagEmpty || mainAppO.prefFloat!!.notLoaded()) {
                mainAppO.prefFloat!!.load()
            }
            return mainAppO.prefFloat
        }
    }
}
