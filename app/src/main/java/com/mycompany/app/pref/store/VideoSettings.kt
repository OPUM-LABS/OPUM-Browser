package com.mycompany.app.pref.store

import android.content.Context
import com.mycompany.app.main.MainApp
import com.mycompany.app.pref.PrefCore
import com.mycompany.app.pref.PrefVideo

/**
 * Video settings (file `PrefVideo`).
 * Read by the full-screen web video (`WebVideoFull`), the video player (`VideoPlayerActivity`), the
 * brightness controls and the two video settings pages.
 */
object VideoSettings : SettingStore() {
    override fun open(context: Context): PrefCore? = PrefVideo.get(context)

    /** Show the swipe guide on the first full-screen video. */
    val guidePlayer = bool("mGuidePlayer") { true }
    /** Show the long-press guide. */
    val guideLongPress = bool("mGuideLong2") { true }
    /** Picture-in-picture when leaving the app. */
    val pipOnHome = bool("mPipHome") { true }
    /** Use the display cut-out area. */
    val cutOut = bool("mCutOut") { true }
    /** 0 follow the device, 1 portrait, 2 landscape. */
    val rotation = int("mRotate") { 0 }
    /** Repeat. */
    val loop = bool("mLoop") { false }
    /** Vertical drag on the right changes the volume. */
    val dragVolume = bool("mDragVolume") { true }
    /** Vertical drag on the left changes the brightness. */
    val dragBright = bool("mDragBright") { true }
    /** Horizontal drag seeks. */
    val dragSeek = bool("mDragSeek") { true }
    /** Seek sensitivity in percent. */
    val seekSense = int("mSeekSense") { 100 }
    /** Use [brightness] instead of the system brightness. */
    val userBrightness = bool("mUserBright3") { false }
    /** Brightness in percent. */
    val brightness = int("mBright3") { 90 }
    /** Aspect ratio index (`MainConst.videoSizeNames`). */
    val ratio = int("mRatio2") { 0 }
    /** Red dot on the settings button. */
    val dotSettings = bool("mNotiSet2") { true }
    /** Red dot on the icon button. */
    val dotIcon = bool("mNotiShow2") { true }
    /** Left double-tap action (3 = default). */
    val tapLeft = int("mTapLeft") { 3 }
    val tapRight = int("mTapRight") { 3 }
    val tapCenter = int("mTapCenter") { 3 }
    /** Left tap area width in portrait. */
    val portAreaLeft = int("mPortAreaLeft") { MainApp.dp64 }
    val portAreaRight = int("mPortAreaRight") { MainApp.dp64 }
    /** Left tap area width in landscape. */
    val landAreaLeft = int("mLandAreaLeft") { MainApp.dp64 shl 1 }
    val landAreaRight = int("mLandAreaRight") { MainApp.dp64 shl 1 }

    /** The double-tap page's values, in its order. */
    val tapAreas = listOf(tapLeft, tapRight, tapCenter, portAreaLeft, portAreaRight, landAreaLeft, landAreaRight)

    override fun afterLoad() {
        // PrefVideo.r reloads the file while this marker is empty
        PrefVideo.loadedTag = "PrefVideo"
    }
}
