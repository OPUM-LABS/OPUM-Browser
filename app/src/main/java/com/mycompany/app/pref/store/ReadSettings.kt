package com.mycompany.app.pref.store

import android.content.Context
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefCore
import com.mycompany.app.pref.PrefRead

/**
 * Settings of the file `PrefRead`, formerly its static fields (2026-11-17). Same keys and defaults. Names come from the keys.
 */
object ReadSettings : SettingStore() {
    override fun open(context: Context): PrefCore? = PrefRead.get(context, false)

    val guideRead = bool("mGuideRead") { true }
    val guideSrc = bool("mGuideSrc") { true }
    val textSize = int("mTextSize") { 100 }
    val userFont = bool("mUserFont") { false }
    val fontPath = string("mFontPath") { "" }
    val fontBold = bool("mFontBold") { false }
    val guideDown = bool("mGuideDown") { true }
    val guidePrev = bool("mGuidePrev") { true }
    val guideSort = bool("mGuideSort") { true }
    val guideGesture = bool("mGuideGesture") { true }
    /** Tips while browsing (`ui/tips`): on / off (Settings > General). */
    val tipsOn = bool("mTipsOn") { true }
    /** Tips already shown, as their ids separated by commas. */
    val tipsSeen = string("mTipsSeen") { "" }
    /** Browser starts so far (the tips space themselves out over the first sessions). */
    val tipSessions = int("mTipSessions") { 0 }
    val guideSense = bool("mGuideSense") { true }
    val guideCap = bool("mGuideCap2") { true }
    val guideLicense = bool("mGuideLicense") { true }
    val guideTts = bool("mGuideTts") { true }
    val notiPath = bool("mNotiPath") { true }
    val notiArea = bool("mNotiArea") { true }
    val notiAre = bool("mNotiAre2") { true }
    val notiAre3 = bool("mNotiAre3") { true }
    val autoSpeak = bool("mAutoSpeak") { false }
    val autoTrans = bool("mAutoTrans") { false }
    /** Pictures in the reader (off: just the text, and the pictures are not downloaded). */
    val readImages = bool("mReadImages") { false }
    val viewTrans = bool("mViewTrans") { false }
    val areaMulti = bool("mAreaMulti") { true }
    val penSize = int("mPenSize") { 10 }
    val penAlpha = int("mPenAlpha") { 0 }
    val penColor = int("mPenColor") { MainConst.presetColors[1] }
    val penPos = float("mPenPos") { MainConst.presetPositions[1] }
    val eraseSize = int("mEraseSize") { 10 }
    val textColor = int("mTextColor") { MainConst.presetColors[0] }
    val textPos = float("mTextPos") { MainConst.presetPositions[0] }
    val readAlpha = int("mReadAlpha") { 0 }
    val readColor = int("mReadColor") { MainConst.gradientColorsAlt[3] }
    val readPos = float("mReadPos") { MainConst.presetPositions[3] }

    override fun afterLoad() {
        // PrefRead.r reloads the file while this marker is empty
        PrefRead.loadedTag = "PrefRead"
    }
}
