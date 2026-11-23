package com.mycompany.app.pref

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp

@Suppress("deprecation")
class PrefEditor(context: Context?) : PrefCore(context, "PrefEditor") {
    companion object {
        // the stored values live in pref/store/EditorSettings since 2026-11-17
        @JvmField
        var scrFilArgb: Int = 0
        @JvmField
        var gridArgb: Int = 0
        @JvmField
        var newsArgb: Int = 0
        @JvmField
        var handArgb: Int = 0
        @JvmField
        var passArgb: Int = 0
        @JvmField
        var trnsArgb: Int = 0
        @JvmField
        var loadedTag: String? = null
        @JvmField
        var iconArgb: Int = 0
        @JvmField
        var color: Int = 0
        @JvmField
        var zoomArgb: Int = 0
        @JvmField
        var ypipArgb: Int = 0

        @JvmStatic
        fun withAlpha(color: Int, alpha: Int): Int {
            var alpha = alpha
            if (alpha < 0) {
                alpha = 0
            } else if (alpha > 90) {
                alpha = 90
            }
            return (Math.round(((100 - alpha) * KotlinVersion.MAX_COMPONENT_VALUE) / 100.0f) shl 24) or (color and 16777215)
        }

        @JvmStatic
        fun get(context: Context?): PrefEditor? {
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return PrefEditor(null)
            }
            var loadedTagEmpty = TextUtils.isEmpty(loadedTag)
            val prefEditor = mainAppO.prefEditor
            if (prefEditor == null) {
                synchronized(PrefEditor::class.java) {
                    if (mainAppO.prefEditor == null) {
                        mainAppO.prefEditor = PrefEditor(context)
                        loadedTagEmpty = false
                    }
                }
            } else if (TextUtils.isEmpty(prefEditor.filePath)) {
                synchronized(PrefEditor::class.java) {
                    mainAppO.prefEditor!!.init(context, "PrefEditor")
                }
                loadedTagEmpty = false
            }
            if (loadedTagEmpty || mainAppO.prefEditor!!.notLoaded()) {
                mainAppO.prefEditor!!.load()
            }
            return mainAppO.prefEditor
        }
    }
}
