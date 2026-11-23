package com.mycompany.app.pref

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil

@Suppress("deprecation")
class PrefWeb(context: Context?) : PrefCore(context, "PrefWeb") {
    companion object {
        /**
         * Run-time bar colours worked out from the settings (`MainUtil.resetTextSizePref`); not stored. With the own
         * colour filling the bars they follow light / dark at every read: the colour by day, a deep tone of it at night
         * ([com.mycompany.app.ui.theme.AppColor.barColor]).
         */
        var headColor: Int = 0
            get() = if (colourBars()) nightAware() else field
        var iconColor: Int = 0
            get() = if (colourBars()) MainUtil.lighterColor(nightAware()) else field

        private fun colourBars(): Boolean =
            com.mycompany.app.pref.store.BrowserSettings.colorTheme.value && com.mycompany.app.pref.store.BrowserSettings.colorBars.value

        private var toneKey = 0L
        private var toneValue = 0

        /** The bar colour for the current light / dark (cached per colour and mode). */
        private fun nightAware(): Int {
            val seed = com.mycompany.app.pref.store.BrowserSettings.themeColor.value
            val dark = com.mycompany.app.ui.state.AppState.darkUi
            val key = (seed.toLong() shl 1) or (if (dark) 1L else 0L)
            if (key != toneKey || toneValue == 0) {
                toneValue = com.mycompany.app.ui.theme.AppColor.barColor(seed, dark)
                toneKey = key
            }
            return toneValue
        }
        @JvmField
        var loadedTag: String? = null
        // the stored values live in pref/store/BrowserSettings since 2026-11-17

        @JvmStatic
        fun get(context: Context?, reload: Boolean): PrefWeb? {
            var reload = reload
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return PrefWeb(null)
            }
            if (!reload && TextUtils.isEmpty(loadedTag)) {
                reload = true
            }
            val prefWeb = mainAppO.prefWeb
            if (prefWeb == null) {
                synchronized(PrefWeb::class.java) {
                    if (mainAppO.prefWeb == null) {
                        mainAppO.prefWeb = PrefWeb(context)
                        reload = false
                    }
                }
            } else if (TextUtils.isEmpty(prefWeb.filePath)) {
                synchronized(PrefWeb::class.java) {
                    mainAppO.prefWeb!!.init(context, "PrefWeb")
                }
                reload = false
            }
            if (reload || mainAppO.prefWeb!!.notLoaded()) {
                mainAppO.prefWeb!!.load()
            }
            return mainAppO.prefWeb
        }

        /** Loads the settings ([reload] = read the file again first, e.g. after a backup restore). */
        @JvmStatic
        fun load(context: Context?, reload: Boolean) {
            if (context == null) return
            if (reload) get(context, true)
            com.mycompany.app.pref.store.BrowserSettings.load(context)
            com.mycompany.app.pref.store.BrowserSettings.migrateHeaderColor(context)
            com.mycompany.app.pref.store.BrowserSettings.migrateWallColors(context)
        }

        /** Stores the quick-back switch of the current mode (secret or normal). */
        @JvmStatic
        fun saveQuickBack(context: Context?, quickBack: Boolean) {
            val settings = com.mycompany.app.pref.store.BrowserSettings
            settings.edit(context) { set(if (com.mycompany.app.pref.store.SyncSettings.secretMode.value) settings.quickBackSecret else settings.quickBack, quickBack) }
        }
    }
}
