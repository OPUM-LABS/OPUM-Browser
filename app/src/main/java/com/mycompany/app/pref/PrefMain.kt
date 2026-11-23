package com.mycompany.app.pref

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class PrefMain(context: Context?) : PrefCore(context, "PrefMain") {
    companion object {
        @JvmField
        var loadedTag: String? = null
        // the values live in pref/store/MainSettings since 2026-11-17

        @JvmStatic
        fun get(context: Context?, reload: Boolean): PrefMain? {
            var reload = reload
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return PrefMain(null)
            }
            if (!reload && TextUtils.isEmpty(loadedTag)) {
                reload = true
            }
            val prefMain = mainAppO.prefMain
            if (prefMain == null) {
                synchronized(PrefMain::class.java) {
                    if (mainAppO.prefMain == null) {
                        mainAppO.prefMain = PrefMain(context)
                        reload = false
                    }
                }
            } else if (TextUtils.isEmpty(prefMain.filePath)) {
                synchronized(PrefMain::class.java) {
                    mainAppO.prefMain!!.init(context, "PrefMain")
                }
                reload = false
            }
            if (reload || mainAppO.prefMain!!.notLoaded()) {
                mainAppO.prefMain!!.load()
            }
            return mainAppO.prefMain
        }

        /** Loads the settings ([reload] = read the file again first, e.g. after a backup restore). */
        @JvmStatic
        fun load(context: Context?, reload: Boolean) {
            if (context == null) return
            if (reload) get(context, true)
            com.mycompany.app.pref.store.MainSettings.load(context)
        }
    }
}
