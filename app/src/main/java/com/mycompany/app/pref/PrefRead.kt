package com.mycompany.app.pref

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst

@Suppress("deprecation")
class PrefRead(context: Context?) : PrefCore(context, "PrefRead") {
    companion object {
        // the stored values live in pref/store/ReadSettings since 2026-11-17
        @JvmField
        var loadedTag: String? = null

        @JvmStatic
        fun get(context: Context?, reload: Boolean): PrefRead? {
            var reload = reload
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return PrefRead(null)
            }
            if (!reload && TextUtils.isEmpty(loadedTag)) {
                reload = true
            }
            val prefRead = mainAppO.prefRead
            if (prefRead == null) {
                synchronized(PrefRead::class.java) {
                    if (mainAppO.prefRead == null) {
                        mainAppO.prefRead = PrefRead(context)
                        reload = false
                    }
                }
            } else if (TextUtils.isEmpty(prefRead.filePath)) {
                synchronized(PrefRead::class.java) {
                    mainAppO.prefRead!!.init(context, "PrefRead")
                }
                reload = false
            }
            if (reload || mainAppO.prefRead!!.notLoaded()) {
                mainAppO.prefRead!!.load()
            }
            return mainAppO.prefRead
        }

        /** Loads the settings ([reload] = read the file again first, e.g. after a backup restore). */
        @JvmStatic
        fun load(context: Context?, reload: Boolean) {
            if (context == null) return
            if (reload) get(context, true)
            com.mycompany.app.pref.store.ReadSettings.load(context)
        }
    }
}
