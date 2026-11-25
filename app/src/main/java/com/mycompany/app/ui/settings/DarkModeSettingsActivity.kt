package com.mycompany.app.ui.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingSliderRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.prefs.rememberPref
import com.mycompany.app.ui.state.AppState

/**
 * Settings › Appearance › Dark mode (2026-11-25, was part of Appearance): web pages light or dark, dimming their
 * images in dark mode (with a sample photo), and the dark home page with a wallpaper.
 */
class DarkModeSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val darkImage = rememberPref({ BrowserSettings.themeImg.value }) { BrowserSettings.edit(context) { set(BrowserSettings.themeImg, it) } }
        val imgBright = rememberPref({ BrowserSettings.imgBright.value }) { BrowserSettings.edit(context) { set(BrowserSettings.imgBright, it) } }
        val darkHome = rememberPref({ BrowserSettings.darkHome.value }) { BrowserSettings.edit(context) { set(BrowserSettings.darkHome, it) } }
        var webTheme by remember { mutableIntStateOf(BrowserSettings.themeWeb.value) }
        var resetting by remember { mutableStateOf(false) }
        // the web page theme is its own choice only while the app does not follow the system
        val own = !BrowserSettings.themeSys.value

        SettingsScaffold(
            title = stringResource(R.string.dark_mode),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.web_page),
                    summary = if (own) null else stringResource(R.string.web_theme_sys),
                    options = DisplaySettingsActivity.themeValues.map { stringResource(DisplaySettingsActivity.themeLabels[it]) },
                    selectedIndex = DisplaySettingsActivity.themeValues.indexOf(webTheme),
                    enabled = own,
                    onSelect = { index ->
                        val value = DisplaySettingsActivity.themeValues[index]
                        if (value != webTheme) {
                            BrowserSettings.editLocal { set(BrowserSettings.themeWeb, value) }
                            AppState.darkWeb = MainUtil.isDarkResources(resources, false)
                            PrefSet.putInt(context, 14, value, "mThemeWeb")
                            MainUtil.applyNightMode()
                            webTheme = value
                        }
                    },
                )
            }
            item {
                SettingSwitchRow(title = stringResource(R.string.dark_image), checked = darkImage.value, onCheckedChange = darkImage::set)
            }
            item {
                SettingSliderRow(
                    title = stringResource(R.string.img_bright), value = imgBright.value, range = 20..100, label = { "$it%" },
                    onValueChange = imgBright::set, enabled = darkImage.value,
                    below = { BrightnessPreview(it) },
                )
            }
            settingsFooter(getString(R.string.img_brt_info))
            item {
                SettingSwitchRow(title = stringResource(R.string.dark_home), checked = darkHome.value, onCheckedChange = darkHome::set)
            }
            settingsFooter(getString(R.string.dark_home_info))
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    darkImage.reload(BrowserSettings.themeImg.value); imgBright.reload(BrowserSettings.imgBright.value)
                    darkHome.reload(BrowserSettings.darkHome.value); webTheme = BrowserSettings.themeWeb.value
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (BrowserSettings.themeWeb.value != 0) { BrowserSettings.editLocal { set(BrowserSettings.themeWeb, 0) }; AppState.darkWeb = false; PrefSet.removeNow(context, 14, "mThemeWeb") }
            if (!BrowserSettings.themeImg.value) BrowserSettings.reset(context, listOf(BrowserSettings.themeImg))
            if (BrowserSettings.imgBright.value != 70) BrowserSettings.reset(context, listOf(BrowserSettings.imgBright))
            if (!BrowserSettings.darkHome.value) BrowserSettings.reset(context, listOf(BrowserSettings.darkHome))
        }
    }
}
