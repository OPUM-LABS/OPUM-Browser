package com.mycompany.app.pref

import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.text.TextUtils
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class PrefZone(context: Context?) : PrefCore(context, "PrefZone") {
    companion object {
        @JvmField
        var loadedTag: String? = null
        // the values live in pref/store/ZoneSettings since 2026-11-17

        @JvmStatic
        fun get(context: Context?, reload: Boolean): PrefZone? {
            var reload = reload
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return PrefZone(null)
            }
            if (!reload && TextUtils.isEmpty(loadedTag)) {
                reload = true
            }
            val prefZone = mainAppO.prefZone
            if (prefZone == null) {
                synchronized(PrefZone::class.java) {
                    if (mainAppO.prefZone == null) {
                        mainAppO.prefZone = PrefZone(context)
                        reload = false
                    }
                }
            } else if (TextUtils.isEmpty(prefZone.filePath)) {
                synchronized(PrefZone::class.java) {
                    mainAppO.prefZone!!.init(context, "PrefZone")
                }
                reload = false
            }
            if (reload || mainAppO.prefZone!!.notLoaded()) {
                mainAppO.prefZone!!.load()
            }
            return mainAppO.prefZone
        }

        /** Loads the settings ([reload] = read the file again first, e.g. after a backup restore). */
        @JvmStatic
        fun load(context: Context?, resources: Resources?, marks: Boolean) {
            if (context == null) return
            if (marks) get(context, true)
            val store = com.mycompany.app.pref.store.ZoneSettings
            store.rtl = resources != null && resources.configuration.layoutDirection == 1
            store.load(context)
        }
    }
}
