package com.mycompany.app.pref.store

import android.content.Context
import com.mycompany.app.main.MainApp
import com.mycompany.app.pref.PrefCore
import com.mycompany.app.pref.PrefZone

/**
 * Page and gesture settings (file `PrefZone`): fonts, floating buttons, text size, scroll / pull / volume keys, tab
 * list, link and image menus, gestures and their areas, OCR, download counts.
 */
object ZoneSettings : SettingStore() {
    override fun open(context: Context): PrefCore? = PrefZone.get(context, false)

    /** Right-to-left layout, set by `PrefZone.load` before loading: the default of [scrollPos] depends on it. */
    @JvmField var rtl = false

    val homeClose = bool("mHomeClose") { false }
    val userFont = bool("mUserFont") { false }
    val fontPath = string("mFontPath") { "" }
    val fontBold = bool("mFontBold") { false }
    /** Download button position. */
    val downPos = int("mDownPos") { 3 }
    val showPip = bool("mShowPip") { true }
    val showFull = bool("mShowFull") { true }
    val showDown = bool("mShowDown") { true }
    /** Play speed among the video button's YouTube actions (the YouTube button merged into the video button, 2026-11-26). */
    val youRate = bool("mYouRate") { true }
    val hideImage = bool("mHideImage") { false }
    val preRaster = bool("mPreRaster") { false }
    /** Page text size in percent. */
    val textSize = int("mTextSize") { 100 }
    /** Scroll button position (default 3, 1 in right-to-left layouts). */
    val scrollPos = int("mScrollPos") { if (rtl) 1 else 3 }
    val showUpPos = int("mShowUpPos") { 2 }
    val pullRefresh = bool("mPullRefresh") { true }
    val notiShort = bool("mNotiShort") { true }
    val volScroll = bool("mVolScroll") { false }
    val tabListType = int("mTabListType") { 0 }
    val tabMiniMode = bool("mTabMiniMode") { false }
    val swipeDelete = bool("mSwipeDelete") { true }
    /** Tab overview in landscape: the side rail (header and buttons) on the right instead of the left (2026-11-23). */
    val tabRailRight = bool("mTabRailRight") { false }
    val tabUndelete = bool("mTabUndelete") { true }
    val backPlay = bool("mBackPlay") { false }
    val openLink = int("mOpenLink4") { 0 }
    val openImage = int("mOpenImage3") { 0 }
    val linkImage = bool("mLinkImage") { false }
    val gesTop = int("mGesTop") { 1 }
    val gesBot = int("mGesBot") { 1 }
    val gesLeft = int("mGesLeft") { 2 }
    val gesRight = int("mGesRight") { 2 }
    val gesCenter = int("mGesCenter") { 2 }
    val portAreaTop = int("mPortAreaTop") { MainApp.dp48 }
    val portAreaBot = int("mPortAreaBot") { MainApp.dp48 }
    val portAreaLeft = int("mPortAreaLeft") { MainApp.dp24 }
    val portAreaRight = int("mPortAreaRight") { MainApp.dp24 }
    val landAreaTop = int("mLandAreaTop") { MainApp.dp48 }
    val landAreaBot = int("mLandAreaBot") { MainApp.dp48 }
    val landAreaLeft = int("mLandAreaLeft") { MainApp.dp40 }
    val landAreaRight = int("mLandAreaRight") { MainApp.dp40 }
    val gesUpLeft = int("mGesUpLt3") { 0 }
    val gesUpRight = int("mGesUpRt3") { 0 }
    val gesDownLeft = int("mGesDnLt3") { 0 }
    val gesDownRight = int("mGesDnRt3") { 0 }
    val popNoti = bool("mPopNoti") { true }
    val snkGuide = bool("mSnkGuide") { true }
    /** Link menu items (bit mask). */
    val useLink = int("mUseLink7") { 8190 }
    /** Image menu items (bit mask). */
    val useImg = int("mUseImg5") { 8190 }
    val linkOrder = string("mLinkOrder6") { "" }
    val imgOrder = string("mImgOrder4") { "" }
    val searchUse = string("mSearchUse2") { "" }
    /** Connections of a split download: 0 = Auto (default since 2026-11-20, was a fixed 10), 1–8 fixed. */
    val multiDown = int("mMultiDown") { 0 }
    val retryDown = int("mRetryDown") { 10 }
    val notiDown = bool("mNotiDown") { true }
    val notiAddr = bool("mNotiAddr") { true }

    override fun afterLoad() {
        // PrefZone.r reloads the file while this marker is empty
        PrefZone.loadedTag = "PrefZone"
    }
}
