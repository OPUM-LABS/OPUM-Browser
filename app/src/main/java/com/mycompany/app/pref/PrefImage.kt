package com.mycompany.app.pref

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class PrefImage(context: Context?) : PrefCore(context, "PrefImage") {
    companion object {
        @JvmField
        var loadedTag: String? = null
        // the values live in pref/store/ImageSettings since 2026-11-17

        @JvmStatic
        fun get(context: Context?, reload: Boolean): PrefImage? {
            var reload = reload
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return PrefImage(null)
            }
            if (!reload && TextUtils.isEmpty(loadedTag)) {
                reload = true
            }
            val prefImage = mainAppO.prefImage
            if (prefImage == null) {
                synchronized(PrefImage::class.java) {
                    if (mainAppO.prefImage == null) {
                        mainAppO.prefImage = PrefImage(context)
                        reload = false
                    }
                }
            } else if (TextUtils.isEmpty(prefImage.filePath)) {
                synchronized(PrefImage::class.java) {
                    mainAppO.prefImage!!.init(context, "PrefImage")
                }
                reload = false
            }
            if (reload || mainAppO.prefImage!!.notLoaded()) {
                mainAppO.prefImage!!.load()
            }
            return mainAppO.prefImage
        }

        /** Loads the settings ([reload] = read the file again first, e.g. after a backup restore). */
        @JvmStatic
        fun load(context: Context?, reload: Boolean) {
            if (context == null) return
            if (reload) get(context, true)
            com.mycompany.app.pref.store.ImageSettings.load(context)
        }
    }
}
