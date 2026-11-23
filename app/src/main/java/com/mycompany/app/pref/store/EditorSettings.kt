package com.mycompany.app.pref.store

import android.content.Context
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefCore
import com.mycompany.app.pref.PrefEditor

/**
 * Settings of the file `PrefEditor`, formerly its static fields (2026-11-17). Same keys and defaults. Names come from the keys.
 */
object EditorSettings : SettingStore() {
    override fun open(context: Context): PrefCore? = PrefEditor.get(context)

    val iconAlpha = int("mIconAlpha") { 0 }
    val iconColor = int("mIconColor") { MainConst.gradientColors[5] }
    val iconPos = float("mIconPos") { MainConst.presetPositions[5] }
    val ttsAlpha = int("mTtsAlpha") { 0 }
    val ttsColor = int("mTtsColor") { MainConst.gradientColors[5] }
    val ttsPos = float("mTtsPos") { MainConst.presetPositions[5] }
    val zoomAlpha = int("mZoomAlpha") { 0 }
    val zoomColor = int("mZoomColor") { MainConst.gradientColors[5] }
    val zoomPos = float("mZoomPos") { MainConst.presetPositions[5] }
    val ypipAlpha = int("mYpipAlpha") { 0 }
    val ypipColor = int("mYpipColor") { MainConst.gradientColors[5] }
    val ypipPos = float("mYpipPos") { MainConst.presetPositions[5] }
    val scrFilUse = int("mScrFilUse") { 0 }
    val scrFilAlpha = int("mScrFilAlpha") { 60 }
    val scrFilColor = int("mScrFilColor") { MainConst.gradientColors[7] }
    val scrFilPos = float("mScrFilPos") { MainConst.presetPositions[7] }
    val gridAlpha = int("mGridAlpha") { 0 }
    val gridColor = int("mGridColor") { MainConst.gradientColors[5] }
    val gridPos = float("mGridPos") { MainConst.presetPositions[5] }
    val botAlpha = int("mBotAlpha") { 0 }
    /** Top bar transparency 0–90 % (added 2026-11-19, set in the layout editor like [botAlpha]). */
    val topAlpha = int("mTopAlpha") { 0 }
    val upAlpha = int("mUpAlpha") { 25 }
    val newsAlpha = int("mNewsAlpha") { 0 }
    val newsColor = int("mNewsColor") { MainConst.gradientColors[5] }
    val newsPos = float("mNewsPos") { MainConst.presetPositions[5] }
    val handAlpha = int("mHandAlpha") { 0 }
    val handColor = int("mHandColor") { MainConst.gradientColors[5] }
    val handPos = float("mHandPos") { MainConst.presetPositions[5] }
    val passAlpha = int("mPassAlpha") { 0 }
    val passColor = int("mPassColor") { MainConst.gradientColors[5] }
    val passPos = float("mPassPos") { MainConst.presetPositions[5] }
    val trnsAlpha = int("mTrnsAlpha") { 0 }
    val trnsColor = int("mTrnsColor") { MainConst.gradientColors[5] }
    val trnsPos = float("mTrnsPos") { MainConst.presetPositions[5] }

    override fun afterLoad() {
        // PrefEditor.r reloads the file while this marker is empty
        PrefEditor.loadedTag = "PrefEditor"
    }
}
