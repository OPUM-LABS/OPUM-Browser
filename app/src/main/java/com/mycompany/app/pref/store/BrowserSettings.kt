package com.mycompany.app.pref.store

import android.content.Context
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefCore
import com.mycompany.app.pref.PrefWeb

/**
 * Browser settings (file `PrefWeb`): home / start pages, blocking, privacy clean-up, system bars, tab bar, cookies,
 * JavaScript, themes, suggestions, downloads. Named `BrowserSettings` so it does not clash with `android.webkit.WebSettings`. The two
 * run-time colours `PrefWeb.headColor` / `PrefWeb.iconColor` (worked out from [headIndex]) are not stored and stay on `PrefWeb`.
 */
object BrowserSettings : SettingStore() {
    override fun open(context: Context): PrefCore? = PrefWeb.get(context, false)

    /** Desktop mode for all pages. */
    val deskLock = bool("mDeskLock") { false }
    val homePage = string("mHomePage3") { "file:///android_asset/shortcut.html" }
    val startPage = string("mStartPage") { "" }
    val tabPage = string("mTabPage") { "..home_link" }
    /** Days of history kept. */
    val historyTime = int("mHistoryTime") { 7 }
    val adsBlock = bool("mAdsBlock") { true }
    val areaBlock = bool("mAreaBlock") { true }
    /** Pop-up blocking level. */
    val popBlock = int("mPopBlock2", legacy = 1) { 2 } // factory default changed 2026-11-23 (old one kept by existing installs)
    val dataDelete = int("mDataDelete2") { 30 }
    val exitDelete = int("mExitDelete2") { 0 }
    val showStatus = bool("mShowStatus") { true }
    val showNavi = bool("mShowNavi") { true }
    val fixTop = bool("mFixTop") { false }
    val fixBot = bool("mFixBot") { false }
    /** Tab bar position. */
    val tabBar = int("mTabBar2") { 0 }
    val tabAccent = bool("mTabAccent") { true }
    val tabColor = int("mTabColor") { 5 }
    val tabRestore = bool("mTabRestore") { true }
    val tabKeypad = bool("mTabKeypad") { false }
    val tabSwipeUp = int("mTabSwipeUp3") { 30 }
    val tabSwipeDown = int("mTabSwipeDn3") { 38 }
    val tabMulti = bool("mTabMulti") { false }
    val cookieType = int("mCookieType") { 0 }
    val thirdType = int("mThirdType", legacy = 0) { 1 } // factory default changed 2026-11-23 (old one kept by existing installs)
    val enableJs = bool("mEnableJs") { true }
    /** Site permissions turned off for every site ("Ask before allowing" off in Site settings): `DbBookPms` bits. */
    val sitePermBlock = int("mSitePermBlock") { 0 }
    val exitNoti = bool("mExitNoti") { true }
    /** Follow the system theme. */
    val themeSys = bool("mThemeSys", legacy = false) { true } // factory default changed 2026-11-23 (old one kept by existing installs)
    /** App theme (stored 2 is read as 0). */
    val themeUi = int("mThemeUi") { 0 }
    /** Page theme (stored 2 is read as 0). */
    val themeWeb = int("mThemeWeb") { 0 }
    val themeImg = bool("mThemeImg") { true }
    val imgBright = int("mImgBright") { 70 }
    val themeHead = int("mThemeHead") { 1 }
    /** Text size index (`MainConst.mainMenuIconsBlack`). */
    val headIndex = int("mHeadIndex") { 16 }
    /**
     * Appearance > Theme > Colour: the app uses its own colour theme ([themeColor]; light or dark by [themeUi]) instead
     * of System / Light / Dark.
     */
    val colorTheme = bool("mColorTheme") { false }
    /** The colour theme's colour (ARGB), chosen with the colour wheel. */
    val themeColor = int("mThemeColor") { 0xFF3F51B5.toInt() }
    /** Colour theme only: the top and bottom browser bars are filled with [themeColor] too. */
    val colorBars = bool("mColorBars") { false }
    /**
     * Appearance > Theme > Colour "System": the device's wallpaper colours (Android 12+) instead of the neutral grey.
     * Separate from [themeSys] (which only decides light / dark) since 2026-11-23, so the day / night switch never
     * changes the colours. -1 = not set yet; [migrateWallColors] takes it from the old "System" choice.
     */
    val wallColors = int("mWallColors") { -1 }

    /** The app's colours come from the wallpaper (Colour row "System"). */
    fun systemColors(): Boolean = !colorTheme.value && wallColors.value == 1

    /**
     * How the browser bars are coloured, as the old [themeHead] values the bar code reads: 2 = the theme colour (Colour
     * theme with [colorBars] on; it uses the bar path of the old "Custom" header), else [themeHead] (1 site colour,
     * 0 plain bars).
     */
    fun headMode(): Int = if (colorTheme.value && colorBars.value) 2 else themeHead.value
    /** Quick back in normal mode. */
    val quickBack = bool("mQuickBack") { false }
    /** Quick back in secret mode. */
    val quickBackSecret = bool("mQckBckSec") { false }
    val darkHome = bool("mDarkHome") { true }
    /** Suggestion engine (-1 = none chosen). */
    val sugEngine = int("mSugEng") { -1 }
    val sugType = int("mSugType3") { 14 }
    /** Order of the suggestion sources, nearest the address bar first ([SuggestOrder]). */
    val sugOrder = string("mSugOrder") { SuggestOrder.DEFAULT }
    /** "Preload pages" while typing an address: 0 off, 1 on Wi-Fi only, 2 always ([com.mycompany.app.web.Preload]). */
    val preload = int("mPreload") { 1 }
    val downLimit = int("mDownLimit") { 2 }
    val downBack = int("mDownBack") { 2 }
    val checkDown = bool("mCheckDown") { false }

    /**
     * The old Header "Custom" colour becomes the colour theme (once; the old setting had no other use for it): colour
     * theme on with that colour, light / dark kept, the site's colour in the address bar off (as it was with Custom).
     * Runs after every load, so an old backup restored later is migrated too.
     */
    fun migrateHeaderColor(context: Context) {
        if (themeHead.value != 2) return
        val palette = com.mycompany.app.main.MainConst.headColors
        val index = headIndex.value
        val color = if (index in palette.indices) palette[index] else themeColor.value
        edit(context) {
            set(colorTheme, true)
            set(themeColor, color or 0xFF000000.toInt())
            set(themeSys, false)
            set(themeHead, 0)
        }
    }

    /**
     * Before 2026-11-23 "System" meant wallpaper colours and following the device's light / dark together. Keeps that
     * look once: wallpaper colours on when the old choice was System. Runs after every load (old backups too).
     */
    fun migrateWallColors(context: Context) {
        if (wallColors.value != -1) return
        edit(context) { set(wallColors, if (themeSys.value && !colorTheme.value) 1 else 0) }
    }

    override fun afterLoad() {
        if (themeUi.value == 2) themeUi.fix(0)
        if (themeWeb.value == 2) themeWeb.fix(0)
        if (headMode() == 2) MainUtil.resetTextSizePref()
        // PrefWeb.r reloads the file while this marker is empty
        PrefWeb.loadedTag = "PrefWeb"
    }
}
