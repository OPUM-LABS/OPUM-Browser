package com.mycompany.app.pref.store

import android.content.Context
import com.mycompany.app.pref.PrefCore
import com.mycompany.app.pref.PrefSync

/**
 * Settings of the file `PrefSync`, formerly its static fields (2026-11-17). Same keys and defaults. Names come from the keys.
 */
object SyncSettings : SettingStore() {
    override fun open(context: Context): PrefCore? = PrefSync.get(context, false)

    val splash = bool("mSplash") { true }
    val secretMode = bool("mSecretMode") { false }
    val normalIndex = int("mNormalIndex") { 0 }

    override fun afterLoad() {
        // PrefSync.r reloads the file while this marker is empty
        PrefSync.loadedTag = "PrefSync"
    }
}
