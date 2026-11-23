package com.mycompany.app.pref

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class PrefVideo(context: Context?) : PrefCore(context, "PrefVideo") {
    companion object {
        @JvmField
        var loadedTag: String? = null
        // the values live in pref/store/VideoSettings since 2026-11-17

        @JvmStatic
        fun get(context: Context?): PrefVideo? {
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return PrefVideo(null)
            }
            var loadedTagEmpty = TextUtils.isEmpty(loadedTag)
            val prefVideo = mainAppO.prefVideo
            if (prefVideo == null) {
                synchronized(PrefVideo::class.java) {
                    if (mainAppO.prefVideo == null) {
                        mainAppO.prefVideo = PrefVideo(context)
                        loadedTagEmpty = false
                    }
                }
            } else if (TextUtils.isEmpty(prefVideo.filePath)) {
                synchronized(PrefVideo::class.java) {
                    mainAppO.prefVideo!!.init(context, "PrefVideo")
                }
                loadedTagEmpty = false
            }
            if (loadedTagEmpty || mainAppO.prefVideo!!.notLoaded()) {
                mainAppO.prefVideo!!.load()
            }
            return mainAppO.prefVideo
        }
    }
}
