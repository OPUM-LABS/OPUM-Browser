package com.mycompany.app.pref

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class PrefSync(context: Context?) : PrefCore(context, "PrefSync") {
    companion object {
        // the stored values live in pref/store/SyncSettings since 2026-11-17
        @JvmField
        var loadedTag: String? = null
        @JvmField
        var currentSecretIndex: Int = 0
        @JvmField
        var normalIndex: Int = 0
        @JvmField
        var savedIndex: Int = 0

        @JvmStatic
        fun get(context: Context?, reload: Boolean): PrefSync? {
            var reload = reload
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return PrefSync(null)
            }
            if (!reload && TextUtils.isEmpty(loadedTag)) {
                reload = true
            }
            val prefSync = mainAppO.prefSync
            if (prefSync == null) {
                synchronized(PrefSync::class.java) {
                    if (mainAppO.prefSync == null) {
                        mainAppO.prefSync = PrefSync(context)
                        reload = false
                    }
                }
            } else if (TextUtils.isEmpty(prefSync.filePath)) {
                synchronized(PrefSync::class.java) {
                    mainAppO.prefSync!!.init(context, "PrefSync")
                }
                reload = false
            }
            if (reload || mainAppO.prefSync!!.notLoaded()) {
                mainAppO.prefSync!!.load()
            }
            return mainAppO.prefSync
        }

        /** Loads the settings ([reload] = read the file again first, e.g. after a backup restore). */
        @JvmStatic
        fun load(context: Context?, reload: Boolean) {
            if (context == null) return
            if (reload) get(context, true)
            com.mycompany.app.pref.store.SyncSettings.load(context)
            val secretIndex = get(context, false)?.readInt(0, "mSecretIndex") ?: 0
            currentSecretIndex = secretIndex
            normalIndex = com.mycompany.app.pref.store.SyncSettings.normalIndex.value
            savedIndex = secretIndex
        }

        @JvmStatic
        fun saveSecretIndex(context: Context?) {
            val prefSyncR: PrefSync? = if (savedIndex == currentSecretIndex || context == null) null else get(context, false)
            if (savedIndex == currentSecretIndex || context == null || prefSyncR == null) {
                return
            }
            val secretIndexNow: Int = currentSecretIndex
            savedIndex = secretIndexNow
            prefSyncR.putInt(secretIndexNow, "mSecretIndex")
            prefSyncR.saveAsync()
        }

        @JvmStatic
        fun saveNormalIndex(context: Context?) {
            val prefSyncR: PrefSync? = if (context == null) null else get(context, false)
            if (context == null || prefSyncR == null) {
                return
            }
            val normalIndexNow: Int = normalIndex
            val savedNormalIndex: Int = com.mycompany.app.pref.store.SyncSettings.normalIndex.value
            if (normalIndexNow != savedNormalIndex) {
                normalIndex = savedNormalIndex
                prefSyncR.putInt(savedNormalIndex, "mNormalIndex")
            }
            val savedIndexNow: Int = savedIndex
            val secretIndexNow: Int = currentSecretIndex
            if (savedIndexNow != secretIndexNow) {
                savedIndex = secretIndexNow
                prefSyncR.putInt(secretIndexNow, "mSecretIndex")
            }
            prefSyncR.putBool("mSecretMode", com.mycompany.app.pref.store.SyncSettings.secretMode.value)
            prefSyncR.save()
        }

        @JvmStatic
        fun saveIndexes(context: Context?) {
            val prefSyncR: PrefSync? = if ((normalIndex == com.mycompany.app.pref.store.SyncSettings.normalIndex.value && savedIndex == currentSecretIndex) || context == null) null else get(context, false)
            if ((normalIndex == com.mycompany.app.pref.store.SyncSettings.normalIndex.value && savedIndex == currentSecretIndex) || context == null || prefSyncR == null) {
                return
            }
            val normalIndexNow: Int = normalIndex
            val savedNormalIndex: Int = com.mycompany.app.pref.store.SyncSettings.normalIndex.value
            if (normalIndexNow != savedNormalIndex) {
                normalIndex = savedNormalIndex
                prefSyncR.putInt(savedNormalIndex, "mNormalIndex")
            }
            val savedIndexNow: Int = savedIndex
            val secretIndexNow: Int = currentSecretIndex
            if (savedIndexNow != secretIndexNow) {
                savedIndex = secretIndexNow
                prefSyncR.putInt(secretIndexNow, "mSecretIndex")
            }
            prefSyncR.save()
        }
    }
}
