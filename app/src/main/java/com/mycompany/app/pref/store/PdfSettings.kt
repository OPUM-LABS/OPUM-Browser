package com.mycompany.app.pref.store

import android.content.Context
import com.mycompany.app.main.MainApp
import com.mycompany.app.pref.PrefCore
import com.mycompany.app.pref.PrefPdf

/**
 * Mixed browser settings kept in the file `PrefPdf` (despite the name: bars, tabs, crop, filter / script update
 * times, security switches, brightness).
 */
object PdfSettings : SettingStore() {
    override fun open(context: Context): PrefCore? = PrefPdf.get(context, false)

    val crop = bool("mCrop") { false }
    /** Screen timeout choice of the browser. */
    val screenOff = int("mScreenOff") { 0 }
    /** Use [brightness] instead of the system brightness. */
    val userBrightness = bool("mUserBright") { false }
    /** Brightness in percent. */
    val brightness = int("mBright") { 90 }
    val onlyHttps = bool("mOnlyHttps2", legacy = false) { true } // factory default changed 2026-11-23 (old one kept by existing installs)
    val saveData = bool("mSaveData") { false }
    val blockAmp = bool("mBlockAmp") { true }
    val blockSsl = bool("mBlockSsl") { false }
    val debugMode = bool("mDebugMode") { false }
    /** Video icon button shown. */
    val videoIcon = bool("mVideoIcon2") { true }
    /** Long-press icon (video). */
    val iconLong = bool("mIconLong") { false }
    /** Blocking of app links. */
    val appBlock = int("mAppBlock2") { 2 }
    /** Largest texture size (0 = not measured). */
    val maxTexSize = int("mMaxTexSize") { 0 }
    /** Top bar at the bottom. */
    val topToBottom = bool("mTopToBot") { false }
    val topHeight = int("mTopHeight") { MainApp.dp48 }
    val botHeight = int("mBotHeight") { MainApp.dp48 }
    /** Where new tabs are added. */
    val tabAdd = int("mTabAdd") { 3 }
    /** Close buttons on tabs. */
    val tabClose = bool("mTabClose") { true }
    val tabWidth = int("mTabWidth") { MainApp.dp144 }
    val tabHeight = int("mTabHeight") { MainApp.dp40 }
    val quickSmall = bool("mQuickSmall") { true }
    /** Address bar type. */
    val addrType = int("mAddrType2") { 0 }
    /** Ad filter not set up yet. */
    val filterInit = bool("mFilterInit") { true }
    /**
     * New installs: register the cookie-notice lists with the default lists ([filterInit]). The first-start wizard's
     * "Block cookie banners" switch sets it while the lists are not registered yet (2026-11-23).
     */
    val cookieSeed = bool("mCookieSeed") { true }
    /** Last ad filter update. */
    val filterTime = long("mFilterTime") { 0L }
    /** Ad filter update interval. */
    val filterDay = int("mFilterDay") { 0 }
    val userScript = bool("mUserScript") { false }
    /** Last user script update. */
    val scriptTime = long("mScriptTime") { 0L }
    /** User script update interval. */
    val scriptDay = int("mScriptDay") { 7 }
    val payConfirm = bool("mPayConfirm") { false }

    override fun afterLoad() {
        // PrefPdf.r reloads the file while this marker is empty
        PrefPdf.loadedTag = "PrefPdf"
    }
}
