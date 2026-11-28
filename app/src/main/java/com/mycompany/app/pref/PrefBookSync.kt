package com.mycompany.app.pref

import android.content.Context

/**
 * The preference file `PrefBookSync`: where each workspace syncs its bookmarks (2026-11-28,
 * [com.mycompany.app.sync.BookmarkSyncStore]). Passwords and keys in it are sealed with the phone's key; a backup
 * writes them opened (inside the password-protected backup), so it restores on another phone.
 */
class PrefBookSync(context: Context?) : PrefCore(context, NAME) {
    companion object {
        const val NAME = "PrefBookSync"

        @Volatile
        private var instance: PrefBookSync? = null

        @JvmStatic
        fun get(context: Context?, reload: Boolean): PrefBookSync? {
            if (context == null) return null
            var pref = instance
            if (pref == null) {
                synchronized(PrefBookSync::class.java) {
                    pref = instance ?: PrefBookSync(context.applicationContext).also { instance = it }
                }
                return pref
            }
            if (reload || pref!!.notLoaded()) pref!!.load()
            return pref
        }
    }
}
