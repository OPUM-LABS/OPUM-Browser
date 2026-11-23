package com.mycompany.app.pref

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class PrefAlbum(context: Context?) : PrefCore(context, "PrefAlbum") {
    companion object {
        // the stored values live in pref/store/AlbumSettings since 2026-11-17
        @JvmField
        var loadedTag: String? = null

        @JvmStatic
        fun get(context: Context?, reload: Boolean): PrefAlbum? {
            var reload = reload
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return PrefAlbum(null)
            }
            if (!reload && TextUtils.isEmpty(loadedTag)) {
                reload = true
            }
            val prefAlbum = mainAppO.prefAlbum
            if (prefAlbum == null) {
                synchronized(PrefAlbum::class.java) {
                    if (mainAppO.prefAlbum == null) {
                        mainAppO.prefAlbum = PrefAlbum(context)
                        reload = false
                    }
                }
            } else if (TextUtils.isEmpty(prefAlbum.filePath)) {
                synchronized(PrefAlbum::class.java) {
                    mainAppO.prefAlbum!!.init(context, "PrefAlbum")
                }
                reload = false
            }
            if (reload || mainAppO.prefAlbum!!.notLoaded()) {
                mainAppO.prefAlbum!!.load()
            }
            return mainAppO.prefAlbum
        }

        /** Loads the settings ([reload] = read the file again first, e.g. after a backup restore). */
        @JvmStatic
        fun load(context: Context?, reload: Boolean) {
            if (context == null) return
            if (reload) get(context, true)
            com.mycompany.app.pref.store.AlbumSettings.load(context)
        }

        @JvmStatic
        fun saveDefaultApp(context: Context?) {
            val prefAlbumR: PrefAlbum? = if (context == null) null else get(context, false)
            if (context == null || prefAlbumR == null) {
                return
            }
            prefAlbumR.putString("mDappName2", com.mycompany.app.pref.store.AlbumSettings.dappName.value)
            prefAlbumR.putString("mDappPkg2", com.mycompany.app.pref.store.AlbumSettings.dappPkg.value)
            prefAlbumR.putString("mDappCls2", com.mycompany.app.pref.store.AlbumSettings.dappCls.value)
            prefAlbumR.saveAsync()
        }

        @JvmStatic
        fun saveTrans(context: Context?) {
            val prefAlbumR: PrefAlbum? = if (context == null) null else get(context, false)
            if (context == null || prefAlbumR == null) {
                return
            }
            prefAlbumR.putString("mTransPick", com.mycompany.app.pref.store.AlbumSettings.transPick.value)
            prefAlbumR.putString("mTransCode", com.mycompany.app.pref.store.AlbumSettings.transCode.value)
            prefAlbumR.saveAsync()
        }
    }
}
