package com.mycompany.app.pref

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUri

@Suppress("deprecation")
class PrefPath(context: Context?) : PrefCore(context, "PrefPath") {
    companion object {
        // the stored values live in pref/store/PathSettings since 2026-11-17
        @JvmField
        var loadedTag: String? = null
        @JvmField
        var picked: String? = null

        @JvmStatic
        fun get(context: Context?, reload: Boolean): PrefPath? {
            var reload = reload
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return PrefPath(null)
            }
            if (!reload && TextUtils.isEmpty(loadedTag)) {
                reload = true
            }
            val prefPath = mainAppO.prefPath
            if (prefPath == null) {
                synchronized(PrefPath::class.java) {
                    if (mainAppO.prefPath == null) {
                        mainAppO.prefPath = PrefPath(context)
                        reload = false
                    }
                }
            } else if (TextUtils.isEmpty(prefPath.filePath)) {
                synchronized(PrefPath::class.java) {
                    mainAppO.prefPath!!.init(context, "PrefPath")
                }
                reload = false
            }
            if (reload || mainAppO.prefPath!!.notLoaded()) {
                mainAppO.prefPath!!.load()
            }
            return mainAppO.prefPath
        }

        /** Loads the settings ([reload] = read the file again first, e.g. after a backup restore). */
        @JvmStatic
        fun load(context: Context?, reload: Boolean) {
            if (context == null) return
            if (reload) get(context, true)
            com.mycompany.app.pref.store.PathSettings.load(context)
            picked = MainUri.storageVolume()
        }

        @JvmStatic
        fun downFolder(context: Context?): String? {
            if (context == null) {
                return picked
            }
            val prefPathR: PrefPath? = get(context, true)
            return if (prefPathR == null) picked else MainUri.findChild(
                context,
                prefPathR.readString("mUriDown", ""),
                MainUri.externalVolumes(context)
            )
        }

        @JvmStatic
        fun savePaths(context: Context?) {
            val prefPathR: PrefPath? = if (context == null) null else get(context, false)
            if (context == null || prefPathR == null) {
                return
            }
            prefPathR.putInt(com.mycompany.app.pref.store.PathSettings.sdkVer.value, "mSdkVer2")
            prefPathR.putBool("mCheckPath", com.mycompany.app.pref.store.PathSettings.checkPath.value)
            prefPathR.putString("mPathDown", com.mycompany.app.pref.store.PathSettings.pathDown.value)
            prefPathR.putString("mPathAlbum", com.mycompany.app.pref.store.PathSettings.pathAlbum.value)
            prefPathR.putString("mDocUri", com.mycompany.app.pref.store.PathSettings.docUri.value)
            prefPathR.saveAsync()
        }
    }
}
