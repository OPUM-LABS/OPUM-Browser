package com.mycompany.app.pref.store

import android.content.Context
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefCore
import com.mycompany.app.pref.PrefFloat

/**
 * Settings of the file `PrefFloat`, formerly its static fields (2026-11-17). Same keys and defaults. Names come from the keys.
 */
object FloatSettings : SettingStore() {
    override fun open(context: Context): PrefCore? = PrefFloat.get(context)

    val flt1Icon = int("mFlt1Icon") { 0 }
    val flt2Icon = int("mFlt2Icon") { 0 }
    val flt3Icon = int("mFlt3Icon") { 0 }
    val flt1Show = bool("mFlt1Show") { false }
    val flt2Show = bool("mFlt2Show") { false }
    val flt3Show = bool("mFlt3Show") { false }
    val flt1Alpha = int("mFlt1Alpha") { MainConst.BUTTON_ALPHA }
    val flt1Color = int("mFlt1Color") { MainConst.gradientColors[5] }
    val flt1Pos = float("mFlt1Pos") { MainConst.presetPositions[5] }
    val flt2Alpha = int("mFlt2Alpha") { MainConst.BUTTON_ALPHA }
    val flt2Color = int("mFlt2Color") { MainConst.gradientColors[5] }
    val flt2Pos = float("mFlt2Pos") { MainConst.presetPositions[5] }
    val flt3Alpha = int("mFlt3Alpha") { MainConst.BUTTON_ALPHA }
    val flt3Color = int("mFlt3Color") { MainConst.gradientColors[5] }
    val flt3Pos = float("mFlt3Pos") { MainConst.presetPositions[5] }
    val flt1LtX = float("mFlt1LtX") { -1.0f }
    val flt1RtX = float("mFlt1RtX") { -1.0f }
    val flt1UpY = float("mFlt1UpY") { -1.0f }
    val flt1DnY = float("mFlt1DnY") { -1.0f }
    val flt2LtX = float("mFlt2LtX") { -1.0f }
    val flt2RtX = float("mFlt2RtX") { -1.0f }
    val flt2UpY = float("mFlt2UpY") { -1.0f }
    val flt2DnY = float("mFlt2DnY") { -1.0f }
    val flt3LtX = float("mFlt3LtX") { -1.0f }
    val flt3RtX = float("mFlt3RtX") { -1.0f }
    val flt3UpY = float("mFlt3UpY") { -1.0f }
    val flt3DnY = float("mFlt3DnY") { -1.0f }

    override fun afterLoad() {
        // PrefFloat.r reloads the file while this marker is empty
        PrefFloat.loadedTag = "PrefFloat"
    }
}
