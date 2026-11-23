package com.mycompany.app.pref

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class PrefTts(context: Context?) : PrefCore(context, "PrefTts") {
    companion object {
        // the stored values live in pref/store/TtsSettings since 2026-11-17
        @JvmField
        var loadedTag: String? = null

        @JvmStatic
        fun get(context: Context?, reload: Boolean): PrefTts? {
            var reload = reload
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return PrefTts(null)
            }
            if (!reload && TextUtils.isEmpty(loadedTag)) {
                reload = true
            }
            val prefTts = mainAppO.prefTts
            if (prefTts == null) {
                synchronized(PrefTts::class.java) {
                    if (mainAppO.prefTts == null) {
                        mainAppO.prefTts = PrefTts(context)
                        reload = false
                    }
                }
            } else if (TextUtils.isEmpty(prefTts.filePath)) {
                synchronized(PrefTts::class.java) {
                    mainAppO.prefTts!!.init(context, "PrefTts")
                }
                reload = false
            }
            if (reload || mainAppO.prefTts!!.notLoaded()) {
                mainAppO.prefTts!!.load()
            }
            return mainAppO.prefTts
        }

        /** Loads the settings ([reload] = read the file again first, e.g. after a backup restore). */
        @JvmStatic
        fun load(context: Context?, reload: Boolean) {
            if (context == null) return
            if (reload) get(context, true)
            com.mycompany.app.pref.store.TtsSettings.load(context)
        }

        @JvmStatic
        fun saveTtsPos(context: Context?) {
            val prefTtsR: PrefTts? = if (context == null) null else get(context, false)
            if (context == null || prefTtsR == null) {
                return
            }
            prefTtsR.putFloat("mTtsLtX", com.mycompany.app.pref.store.TtsSettings.ttsLtX.value)
            prefTtsR.putFloat("mTtsRtX", com.mycompany.app.pref.store.TtsSettings.ttsRtX.value)
            prefTtsR.putFloat("mTtsUpY", com.mycompany.app.pref.store.TtsSettings.ttsUpY.value)
            prefTtsR.putFloat("mTtsDnY", com.mycompany.app.pref.store.TtsSettings.ttsDnY.value)
            prefTtsR.saveAsync()
        }
    }
}
