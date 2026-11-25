package com.mycompany.app.ui.settings

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.MainSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ColorWheelPicker
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsJump
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.ui.theme.ThemeChoice

/**
 * Settings › Appearance › Theme and colours (2026-11-25, was part of Appearance): the colours (System, Neutral or an
 * own colour from the wheel), light / dark, whether the bars take the own colour, and the site's colour in the address
 * bar. The phone preview of the other pages ([PhonePreview]) shows the bars in the theme; a tap on a bar jumps to the
 * colour. Rows that do nothing in the current state stay, greyed, with a line saying why (so the search finds them).
 */
class ThemeSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        var tick by remember { mutableIntStateOf(0) }
        var flash by remember { mutableStateOf(false) }
        LaunchedEffect(flash) { if (flash) { kotlinx.coroutines.delay(1800); flash = false } }
        var jump by remember { mutableStateOf<SettingsJump?>(null) }
        var resetting by remember { mutableStateOf(false) }
        val portrait = LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE
        val own = "$tick".let { BrowserSettings.colorTheme.value }

        SettingsScaffold(
            title = stringResource(R.string.appear_theme_page),
            onBack = { finish() },
            onReset = { resetting = true },
            jumpTo = jump,
            above = if (!portrait) null else {
                {
                    val bars = "$tick".let {
                        PreviewBars(
                            BrowserSettings.showStatus.value, BrowserSettings.showNavi.value, BrowserSettings.fixTop.value, BrowserSettings.fixBot.value,
                            PdfSettings.topToBottom.value,
                            MainUtil.parseIntList(MainSettings.topItems.value)?.toList().orEmpty(),
                            MainUtil.parseIntList(MainSettings.botItems.value)?.toList().orEmpty(),
                            EditorSettings.topAlpha.value.coerceIn(0, 90), EditorSettings.botAlpha.value.coerceIn(0, 90),
                            BrowserSettings.tabBar.value, currentPreviewTabs(),
                        )
                    }
                    val density = LocalDensity.current
                    PhonePreview(
                        bars,
                        onTap = { at, places, _ ->
                            val bar = with(density) { places.barAt(at.y, 6.dp.toPx(), PreviewSize.sys.toPx()) }
                            if (bar == PreviewBar.TOP || bar == PreviewBar.BOTTOM || bar == PreviewBar.TABS) {
                                flash = true
                                jump = SettingsJump(getString(R.string.color_title))
                            }
                        },
                        over = { places, _ ->
                            if (flash) {
                                BarOutline(PreviewBar.TOP, bars, places)
                                BarOutline(PreviewBar.BOTTOM, bars, places)
                            }
                        },
                        scrolling = false,
                    )
                    Text(
                        stringResource(R.string.look_tap_guide), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    )
                }
            },
        ) {
            // two independent choices ([ThemeChoice], shared with the first-start wizard): the colours, and light / dark
            // (the menu's Day / Night switch changes only the mode)
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.color_title),
                    options = listOf(
                        stringResource(R.string.system_name), stringResource(R.string.theme_neutral),
                        stringResource(R.string.theme_own_color),
                    ),
                    selectedIndex = "$tick".let { ThemeChoice.currentColor() },
                    onSelect = { choice -> ThemeChoice.chooseColor(this@ThemeSettingsActivity, choice); tick++ },
                )
            }
            if (own) {
                item {
                    ColorWheelPicker(
                        color = "$tick".let { BrowserSettings.themeColor.value },
                        // live: the page and the preview re-colour while dragging; written to the file when the drag ends
                        onChange = { BrowserSettings.editLocal { set(BrowserSettings.themeColor, it) }; MainUtil.resetTextSizePref(); tick++ },
                        onCommit = { BrowserSettings.edit(context) { set(BrowserSettings.themeColor, it) }; MainUtil.resetTextSizePref(); tick++ },
                        wheelDescription = stringResource(R.string.theme_color_wheel),
                        brightnessLabel = stringResource(R.string.brightness),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.theme_mode),
                    options = listOf(
                        stringResource(R.string.system_name), stringResource(R.string.theme_light),
                        stringResource(R.string.theme_dark),
                    ),
                    selectedIndex = "$tick".let { ThemeChoice.currentMode() },
                    onSelect = { choice -> ThemeChoice.chooseMode(this@ThemeSettingsActivity, choice); tick++ },
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.theme_color_bars),
                    summary = if (own) null else stringResource(R.string.theme_bars_own_only),
                    checked = "$tick".let { own && BrowserSettings.colorBars.value },
                    enabled = own,
                    onCheckedChange = { on ->
                        BrowserSettings.edit(context) { set(BrowserSettings.colorBars, on) }
                        MainUtil.resetTextSizePref()
                        tick++
                    },
                )
            }
            // the site's colour in the address bar; not while the bars take the theme colour
            item {
                val free = "$tick".let { BrowserSettings.headMode() != 2 }
                SettingSwitchRow(
                    title = stringResource(R.string.theme_site_color),
                    summary = if (free) null else stringResource(R.string.site_color_off),
                    checked = "$tick".let { free && BrowserSettings.themeHead.value == 1 },
                    enabled = free,
                    onCheckedChange = { on -> BrowserSettings.edit(context) { set(BrowserSettings.themeHead, if (on) 1 else 0) }; tick++ },
                )
            }
            settingsFooter(getString(R.string.site_theme_info))
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    tick++
                    refreshTheme()
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Restores the defaults of this page (the theme follows the system again, in its colours). */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (!BrowserSettings.themeSys.isDefault) BrowserSettings.reset(context, listOf(BrowserSettings.themeSys))
            if (BrowserSettings.themeUi.value != 0) { BrowserSettings.editLocal { set(BrowserSettings.themeUi, 0) }; AppState.darkUi = false; PrefSet.removeNow(context, 14, "mThemeUi") }
            if (BrowserSettings.themeHead.value != 1) BrowserSettings.reset(context, listOf(BrowserSettings.themeHead))
            if (BrowserSettings.colorTheme.value || !BrowserSettings.themeColor.isDefault || BrowserSettings.colorBars.value) BrowserSettings.reset(context, listOf(BrowserSettings.colorTheme, BrowserSettings.themeColor, BrowserSettings.colorBars))
            if (BrowserSettings.wallColors.value != 1) BrowserSettings.edit(context) { set(BrowserSettings.wallColors, 1) } // System colours, as on a new install
            if (BrowserSettings.headIndex.value != 16) { BrowserSettings.editLocal { set(BrowserSettings.headIndex, 16) }; MainUtil.resetTextSizePref(); PrefSet.removeNow(context, 14, "mHeadIndex") }
            MainUtil.applyNightMode()
        }
    }
}
