package com.mycompany.app.pref.store

import android.content.Context
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefCore
import com.mycompany.app.pref.PrefMain

/**
 * General app settings (file `PrefMain`): menus, bars, rotation, the measured system-bar heights.
 *
 * The status / navigation bar heights are measured at run time and saved later by `MainUtil` / `MainActivity`
 * (they set the values with [editLocal] and write the file themselves), and the backup restore resets them in the
 * file; both work as before.
 */
object MainSettings : SettingStore() {
    override fun open(context: Context): PrefCore? = PrefMain.get(context, false)

    /** App screen rotation (0 = follow the device). */
    val appRotation = int("mAppRotate") { 0 }
    /** Press back twice to leave. */
    val doubleBack = bool("mDoubleBack") { false }
    /** Notification for the last tab. */
    val lastNoti = bool("mLastNoti") { false }
    /** Measured status bar height. */
    val statusHeight = int("mStatusHeight") { 0 }
    /** [statusHeight] has been measured. */
    val statusConfirmed = bool("mStatusConfirm") { false }
    /** Measured navigation bar height. */
    val naviHeight = int("mNaviHeight2") { 0 }
    /** [naviHeight] has been measured. */
    val naviConfirmed = bool("mNaviConfirm") { false }
    /** Main menu style. */
    val menuType = int("mMenuType") { 4 }
    /** Main menu columns in portrait. */
    val menuColumns = int("mMenuPort") { 5 }
    /** Last main menu page. */
    val menuPage = int("mMenuPage") { 0 }
    /** Main menu items. */
    val menuItems = string("mMenuItems") { com.mycompany.app.ui.browser.MenuCatalog.STANDARD_MENU }
    /** Main menu bottom row. */
    val midItems = string("mMidItems") { com.mycompany.app.ui.browser.MenuCatalog.STANDARD_QUICK }
    /** Top bar buttons. */
    /** The reload button sits in the address pill, so the default top bar no longer has its own (2026-11-19). */
    val topItems = string("mTopItems") { "63,1,31" }
    /** Bottom bar buttons. */
    val botItems = string("mBotItems") { "26,27,28,29,30,2" }
    /** Bottom bar long-press actions. */
    val botLongs = string("mBotLongs") { "3,3,44,45,70,65" }
    /** Bottom bar swipe actions. */
    val botSwipe = string("mBotSwipe") { "0,0,0,0,0,0" }
    /** Button groups on the bars: `2000:3,44,45;2001:12,13` (see ui/browser/BarGroups, 2026-11-20). */
    val barGroups = string("mBarGroups") { "" }
    /** Address bar icons. */
    val addrItems = string("mAddrItems2") { "0,1,2" }
    /** The bookmark widget's data. */
    val widgetBook = string("mWidgetBook") { "" }

    override fun afterLoad() {
        // menu clean-up (2026-11-19): the old default layout becomes the new one; retired "Not used" ids are dropped
        val catalog = com.mycompany.app.ui.browser.MenuCatalog
        val menu = if (menuItems.value == catalog.OLD_MENU) catalog.STANDARD_MENU
            else MainUtil.joinIntList(catalog.clean(MainUtil.parseIntList(menuItems.value))) ?: menuItems.value
        val quick = if (midItems.value == catalog.OLD_QUICK) catalog.STANDARD_QUICK
            else MainUtil.joinIntList(catalog.clean(MainUtil.parseIntList(midItems.value))) ?: midItems.value
        if (menu != menuItems.value || quick != midItems.value) editLocal { set(menuItems, menu); set(midItems, quick) }
        // PrefMain.r reloads the file while this marker is empty
        PrefMain.loadedTag = "PrefMain"
    }
}
