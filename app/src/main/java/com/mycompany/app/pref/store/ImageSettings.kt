package com.mycompany.app.pref.store

import android.content.Context
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefCore
import com.mycompany.app.pref.PrefImage

/**
 * Image viewer settings (file `PrefImage`).
 * Read by the image viewer (`ImageViewerActivity`), the page-layout dialog (`DialogSetImage`),
 * the brightness dialog, `MainUtil`'s page-layout helpers and the two image settings pages.
 */
object ImageSettings : SettingStore() {
    override fun open(context: Context): PrefCore? = PrefImage.get(context, false)

    /** Last page index (kept across a backup restore). */
    val pageIndex = int("mIndex") { 0 }
    /** 0 follow the device, 1 portrait, 2 landscape. */
    val rotation = int("mRotate") { 0 }
    /** Thumbnail strip. */
    val showThumb = bool("mShowThumb") { true }
    /** Page-turn guide. */
    val showGuide = bool("mShowGuide") { true }
    /** Volume keys turn pages. */
    val volumeTurn = bool("mVolTurn") { false }
    /** Screen timeout choice. */
    val screenOff = int("mScreenOff") { 1 }
    /** Use [brightness] instead of the system brightness. */
    val userBrightness = bool("mUserBright3") { false }
    /** Brightness in percent. */
    val brightness = int("mBright3") { 90 }
    /** Right-to-left page order. */
    val reverse = bool("mReverse") { false }
    /** Page mode in portrait. */
    val viewPort = int("mViewPort") { 0 }
    /** Page mode in landscape. */
    val viewLand = int("mViewLand") { 3 }
    val fitPort = bool("mFitPort") { false }
    val fitLand = bool("mFitLand") { false }
    val splitPort = bool("mSplitPort") { false }
    val splitLand = bool("mSplitLand") { false }
    /** Stored -1 means `MainApp.dp15` ([afterLoad]). */
    val marginPort = int("mMarginPort") { -1 }
    /** Stored -1 means `MainApp.dp15`. */
    val marginLand = int("mMarginLand") { -1 }
    val backColor = int("mBackColor") { MainConst.backColors[5] }
    /** Left tap action (0 = turn the page). */
    val tapLeft = int("mTapLeft") { 0 }
    val tapRight = int("mTapRight") { 0 }
    /** Left tap area width in portrait. */
    val portAreaLeft = int("mPortAreaLeft") { MainApp.dp64 }
    val portAreaRight = int("mPortAreaRight") { MainApp.dp64 }
    /** Left tap area width in landscape. */
    val landAreaLeft = int("mLandAreaLeft") { MainApp.dp64 shl 1 }
    val landAreaRight = int("mLandAreaRight") { MainApp.dp64 shl 1 }

    /** The tap page's values, in its order. */
    val tapAreas = listOf(tapLeft, tapRight, portAreaLeft, portAreaRight, landAreaLeft, landAreaRight)

    override fun afterLoad() {
        if (marginPort.value == -1) marginPort.fix(MainApp.dp15)
        if (marginLand.value == -1) marginLand.fix(MainApp.dp15)
        // PrefImage.r reloads the file while this marker is empty
        PrefImage.loadedTag = "PrefImage"
    }
}
