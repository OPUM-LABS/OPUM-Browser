package com.mycompany.app.pref.store

import android.content.Context
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefCore
import com.mycompany.app.pref.PrefSub

/**
 * The video player's subtitle settings (file `PrefSub`). Settings nothing read any more were removed (2026-11-22); their keys may
 * still be in the file and in old backups, where they are ignored.
 */
object SubtitleSettings : SettingStore() {
    override fun open(context: Context): PrefCore? = PrefSub.get(context)

    /** Stored -1 means ten small margins ([afterLoad]). */
    val portraitPos = int("mPosPort") { -1 }
    /** Text size, 10..50. */
    val textSize = int("mTextSize") { 20 }
    val backColor = int("mBackColor") { MainConst.gradientColors[7] }

    override fun afterLoad() {
        if (portraitPos.value == -1) portraitPos.fix(MainApp.dp16 * 10)
        // PrefSub.r reloads the file while this marker is empty
        PrefSub.loadedTag = "PrefSub"
    }
}
