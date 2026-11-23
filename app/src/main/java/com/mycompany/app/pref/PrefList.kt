package com.mycompany.app.pref

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class PrefList(context: Context?) : PrefCore(context, "PrefList") {
    companion object {
        // the stored values live in pref/store/ListSettings since 2026-11-17
        @JvmField
        var loadedTag: String? = null

        @JvmStatic
        fun get(context: Context?, reload: Boolean): PrefList? {
            var reload = reload
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return PrefList(null)
            }
            if (!reload && TextUtils.isEmpty(loadedTag)) {
                reload = true
            }
            val prefList = mainAppO.prefList
            if (prefList == null) {
                synchronized(PrefList::class.java) {
                    if (mainAppO.prefList == null) {
                        mainAppO.prefList = PrefList(context)
                        reload = false
                    }
                }
            } else if (TextUtils.isEmpty(prefList.filePath)) {
                synchronized(PrefList::class.java) {
                    mainAppO.prefList!!.init(context, "PrefList")
                }
                reload = false
            }
            if (reload || mainAppO.prefList!!.notLoaded()) {
                mainAppO.prefList!!.load()
            }
            return mainAppO.prefList
        }

        /** Loads the settings ([reload] = read the file again first, e.g. after a backup restore). */
        @JvmStatic
        fun load(context: Context?, reload: Boolean) {
            if (context == null) return
            if (reload) get(context, true)
            com.mycompany.app.pref.store.ListSettings.load(context)
        }
    }
}
