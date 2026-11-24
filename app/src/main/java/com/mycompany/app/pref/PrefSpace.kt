package com.mycompany.app.pref

import android.content.Context

/**
 * The preference file `PrefSpace`: the workspaces (2026-11-24, [com.mycompany.app.pref.store.WorkspaceSettings]).
 * Like the other `Pref*` files it lives in `files/.pref` and is part of backups.
 */
class PrefSpace(context: Context?) : PrefCore(context, "PrefSpace") {
    companion object {
        @Volatile
        private var instance: PrefSpace? = null

        @JvmStatic
        fun get(context: Context?, reload: Boolean): PrefSpace? {
            if (context == null) return null
            var pref = instance
            if (pref == null) {
                synchronized(PrefSpace::class.java) {
                    pref = instance ?: PrefSpace(context.applicationContext).also { instance = it }
                }
                return pref
            }
            if (reload || pref!!.notLoaded()) pref!!.load()
            return pref
        }

        /** Loads the settings ([reload] = read the file again first, e.g. after a backup restore). */
        @JvmStatic
        fun load(context: Context?, reload: Boolean) {
            if (context == null) return
            get(context, reload)
            com.mycompany.app.pref.store.WorkspaceSettings.load(context)
        }
    }
}
