package com.mycompany.app.pref

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class PrefZtri(context: Context?) : PrefCore(context, "PrefZtri") {
    companion object {
        // the stored values live in pref/store/ZtriSettings since 2026-11-17
        @JvmField
        var loadedTag: String? = null

        @JvmStatic
        fun get(context: Context?): PrefZtri? {
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return PrefZtri(null)
            }
            var loadedTagEmpty = TextUtils.isEmpty(loadedTag)
            val prefZtri = mainAppO.prefZtri
            if (prefZtri == null) {
                synchronized(PrefZtri::class.java) {
                    if (mainAppO.prefZtri == null) {
                        mainAppO.prefZtri = PrefZtri(context)
                        loadedTagEmpty = false
                    }
                }
            } else if (TextUtils.isEmpty(prefZtri.filePath)) {
                synchronized(PrefZtri::class.java) {
                    mainAppO.prefZtri!!.init(context, "PrefZtri")
                }
                loadedTagEmpty = false
            }
            if (loadedTagEmpty || mainAppO.prefZtri!!.notLoaded()) {
                mainAppO.prefZtri!!.load()
            }
            return mainAppO.prefZtri
        }

        @JvmStatic
        fun saveDownPos(context: Context?) {
            val prefZtriR: PrefZtri? = if (context == null) null else get(context)
            if (context == null || prefZtriR == null) {
                return
            }
            prefZtriR.putFloat("mDownLtX", com.mycompany.app.pref.store.ZtriSettings.downLtX.value)
            prefZtriR.putFloat("mDownRtX", com.mycompany.app.pref.store.ZtriSettings.downRtX.value)
            prefZtriR.putFloat("mDownUpY", com.mycompany.app.pref.store.ZtriSettings.downUpY.value)
            prefZtriR.putFloat("mDownDnY", com.mycompany.app.pref.store.ZtriSettings.downDnY.value)
            prefZtriR.saveAsync()
        }
    }
}
