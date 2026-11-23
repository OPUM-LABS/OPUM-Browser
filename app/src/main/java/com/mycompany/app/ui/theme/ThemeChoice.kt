package com.mycompany.app.ui.theme

import android.app.Activity
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.state.AppState

/**
 * The app theme as two independent choices (2026-11-23), shared by Appearance > Theme and the first-start wizard:
 *
 * - **Colour**: [COLOR_SYSTEM] (the device's wallpaper colours), [COLOR_NEUTRAL] (blue grey) or [COLOR_OWN] (a colour
 *   from the wheel). Stored as `wallColors` / `colorTheme` / `themeColor`.
 * - **Mode**: [MODE_SYSTEM] (follow the device), [MODE_LIGHT] or [MODE_DARK]. Stored as `themeSys` / `themeUi`.
 *
 * The menu's Day / Night switch changes only the mode, so every colour has a day and a night look.
 */
object ThemeChoice {
    const val COLOR_SYSTEM = 0
    const val COLOR_NEUTRAL = 1
    const val COLOR_OWN = 2

    const val MODE_SYSTEM = 0
    const val MODE_LIGHT = 1
    const val MODE_DARK = 2

    fun currentColor(): Int = when {
        BrowserSettings.colorTheme.value -> COLOR_OWN
        BrowserSettings.systemColors() -> COLOR_SYSTEM
        else -> COLOR_NEUTRAL
    }

    fun currentMode(): Int = when {
        BrowserSettings.themeSys.value -> MODE_SYSTEM
        BrowserSettings.themeUi.value == 1 -> MODE_DARK
        else -> MODE_LIGHT
    }

    fun chooseColor(activity: OpumComposeActivity, choice: Int) {
        if (choice == currentColor()) return
        BrowserSettings.edit(activity) {
            set(BrowserSettings.colorTheme, choice == COLOR_OWN)
            set(BrowserSettings.wallColors, if (choice == COLOR_SYSTEM) 1 else 0)
        }
        MainUtil.resetTextSizePref()
        activity.refreshTheme()
    }

    fun chooseMode(activity: OpumComposeActivity, choice: Int) {
        if (choice == currentMode()) return
        val follow = choice == MODE_SYSTEM
        if (follow != BrowserSettings.themeSys.value) {
            BrowserSettings.edit(activity) { set(BrowserSettings.themeSys, follow) }
            MainUtil.applyNightMode()
        }
        if (follow) {
            AppState.darkUi = MainUtil.isDarkResources(activity.resources, true)
            AppState.darkWeb = MainUtil.isDarkResources(activity.resources, false)
            MainUtil.resetTextSizePref()
            activity.refreshTheme()
        } else {
            setDark(activity, choice == MODE_DARK)
        }
    }

    /** Dark web pages (the web page theme while the app does not follow the system). */
    fun setWebDark(activity: OpumComposeActivity, dark: Boolean) {
        val value = if (dark) 1 else 0
        if (BrowserSettings.themeWeb.value == value) return
        BrowserSettings.editLocal { set(BrowserSettings.themeWeb, value) }
        AppState.darkWeb = MainUtil.isDarkResources(activity.resources, false)
        PrefSet.putInt(activity, 14, value, "mThemeWeb")
        MainUtil.applyNightMode()
    }

    /** Light or dark app (mode Light / Dark). */
    fun setDark(activity: Activity, dark: Boolean) {
        val value = if (dark) 1 else 0
        if (BrowserSettings.themeUi.value == value) return
        BrowserSettings.editLocal { set(BrowserSettings.themeUi, value) }
        AppState.darkUi = MainUtil.isDarkResources(activity.resources, true)
        PrefSet.putInt(activity, 14, value, "mThemeUi")
        MainUtil.resetTextSizePref()
        (activity as? OpumComposeActivity)?.refreshTheme()
    }
}
