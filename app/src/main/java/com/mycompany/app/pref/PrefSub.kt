package com.mycompany.app.pref

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class PrefSub(context: Context?) : PrefCore(context, "PrefSub") {
    companion object {
        @JvmField
        var loadedTag: String? = null
        // the values live in pref/store/SubtitleSettings since 2026-11-17

        @JvmStatic
        fun get(context: Context?): PrefSub? {
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return PrefSub(null)
            }
            var loadedTagEmpty = TextUtils.isEmpty(loadedTag)
            val prefSub = mainAppO.prefSub
            if (prefSub == null) {
                synchronized(PrefSub::class.java) {
                    if (mainAppO.prefSub == null) {
                        mainAppO.prefSub = PrefSub(context)
                        loadedTagEmpty = false
                    }
                }
            } else if (TextUtils.isEmpty(prefSub.filePath)) {
                synchronized(PrefSub::class.java) {
                    mainAppO.prefSub!!.init(context, "PrefSub")
                }
                loadedTagEmpty = false
            }
            if (loadedTagEmpty || mainAppO.prefSub!!.notLoaded()) {
                mainAppO.prefSub!!.load()
            }
            return mainAppO.prefSub
        }
    }
}
