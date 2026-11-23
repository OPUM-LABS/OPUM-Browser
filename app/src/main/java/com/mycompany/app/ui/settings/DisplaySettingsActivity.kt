package com.mycompany.app.ui.settings

import android.content.Context
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.MainSettings
import androidx.compose.foundation.background
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Text
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogEditIcon
import com.mycompany.app.dialog.DialogSeekSimple
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingSliderRow
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.BrightnessDialog
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.ColorWheelPicker
import com.mycompany.app.ui.theme.ThemeChoice
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Appearance (was "Display"; text size and font moved in on 2026-11-19): theme (System / Light / Dark / Colour with a
 * colour wheel), web page theme, dark image brightness, the site's colour in the address bar, screen filter, screen brightness, screen timeout
 * and rotation. Theme changes are applied to this screen immediately ([refreshTheme]). The filter colour and brightness
 * dialogs only accept the legacy activity type and go through `DialogShow.direct`.
 */
class DisplaySettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val themeSys = rememberPref({ BrowserSettings.themeSys.value }) { on ->
            BrowserSettings.edit(context) { set(BrowserSettings.themeSys, on) }
            MainUtil.applyNightMode()
            refreshTheme()
        }
        val darkImage = rememberPref({ BrowserSettings.themeImg.value }) { BrowserSettings.edit(context) { set(BrowserSettings.themeImg, it) } }
        val darkHome = rememberPref({ BrowserSettings.darkHome.value }) { BrowserSettings.edit(context) { set(BrowserSettings.darkHome, it) } }
        val screenFilter = rememberPref({ EditorSettings.scrFilUse.value }) { EditorSettings.edit(context) { set(EditorSettings.scrFilUse, it) } }
        val screenOff = rememberPref({ PdfSettings.screenOff.value }) { PdfSettings.edit(context) { set(PdfSettings.screenOff, it) } }
        val rotate = rememberPref({ MainSettings.appRotation.value }) {
            MainSettings.edit(context) { set(MainSettings.appRotation, it) }
            MainUtil.applyImageOrientation(this)
        }
        var webTheme by remember { mutableIntStateOf(BrowserSettings.themeWeb.value) }
        var tick by remember { mutableIntStateOf(0) }
        var filterColor by remember { mutableIntStateOf(MainUtil.alphaColor(EditorSettings.scrFilColor.value, EditorSettings.scrFilAlpha.value)) }
        var choosing by remember { mutableIntStateOf(0) } // 12 filter, 18 brightness
        var resetting by remember { mutableStateOf(false) }

        OnScreenResume { tick++; filterColor = MainUtil.alphaColor(EditorSettings.scrFilColor.value, EditorSettings.scrFilAlpha.value) }

        /** The web page's own light / dark (row "Web page"). */
        fun setWebTheme(value: Int) {
            if (value != webTheme) {
                BrowserSettings.editLocal { set(BrowserSettings.themeWeb, value) }
                AppState.darkWeb = MainUtil.isDarkResources(resources, false)
                PrefSet.putInt(context, 14, value, "mThemeWeb")
                MainUtil.applyNightMode()
                webTheme = value
            }
        }

        SettingsScaffold(
            title = stringResource(R.string.set_appearance),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.hd_theme))
            // two independent choices ([ThemeChoice], shared with the first-start wizard): the colours, and light / dark
            // (the menu's Day / Night switch changes only the mode); the web page theme stays its own row
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.color_title),
                    options = listOf(
                        stringResource(R.string.system_name), stringResource(R.string.theme_neutral),
                        stringResource(R.string.theme_own_color),
                    ),
                    selectedIndex = "$tick".let { ThemeChoice.currentColor() },
                    onSelect = { choice -> ThemeChoice.chooseColor(this@DisplaySettingsActivity, choice); tick++ },
                )
            }
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.theme_mode),
                    options = listOf(
                        stringResource(R.string.system_name), stringResource(R.string.theme_light),
                        stringResource(R.string.theme_dark),
                    ),
                    selectedIndex = "$tick".let { ThemeChoice.currentMode() },
                    onSelect = { choice ->
                        ThemeChoice.chooseMode(this@DisplaySettingsActivity, choice)
                        webTheme = BrowserSettings.themeWeb.value
                        themeSys.reload(BrowserSettings.themeSys.value)
                        tick++
                    },
                )
            }
            if ("$tick".let { BrowserSettings.colorTheme.value }) {
                item {
                    ColorWheelPicker(
                        color = "$tick".let { BrowserSettings.themeColor.value },
                        // live: the page re-colours while dragging; written to the file when the drag ends
                        onChange = { BrowserSettings.editLocal { set(BrowserSettings.themeColor, it) }; MainUtil.resetTextSizePref() },
                        onCommit = { BrowserSettings.edit(context) { set(BrowserSettings.themeColor, it) }; MainUtil.resetTextSizePref() },
                        wheelDescription = stringResource(R.string.theme_color_wheel),
                        brightnessLabel = stringResource(R.string.brightness),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                item {
                    SettingSwitchRow(
                        title = stringResource(R.string.theme_color_bars),
                        checked = "$tick".let { BrowserSettings.colorBars.value },
                        onCheckedChange = { on ->
                            BrowserSettings.edit(context) { set(BrowserSettings.colorBars, on) }
                            MainUtil.resetTextSizePref()
                            tick++
                        },
                    )
                }
            }
            // the web page's own light / dark, while the app does not follow the system
            if (!themeSys.value) {
                item {
                    SettingSegmentedRow(
                        title = stringResource(R.string.web_page),
                        options = themeValues.map { stringResource(themeLabels[it]) },
                        selectedIndex = themeValues.indexOf(webTheme),
                        onSelect = { setWebTheme(themeValues[it]) },
                    )
                }
            }
            settingsHeader(getString(R.string.dark_mode))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.dark_image),
                    checked = darkImage.value,
                    onCheckedChange = darkImage::set,
                )
            }
            if (darkImage.value) {
                item {
                    SettingSliderRow(
                        title = stringResource(R.string.img_bright),
                        value = "$tick".let { BrowserSettings.imgBright.value },
                        range = 20..100,
                        label = { "$it%" },
                        onValueChange = { brightness ->
                            if (BrowserSettings.imgBright.value != brightness) { BrowserSettings.edit(context) { set(BrowserSettings.imgBright, brightness) }; tick++ }
                        },
                        below = { BrightnessPreview(it) },
                    )
                }
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.dark_home),
                    checked = darkHome.value,
                    onCheckedChange = darkHome::set,
                )
            }
            settingsFooter(getString(R.string.img_brt_info) + "\n" + getString(R.string.dark_home_info))
            settingsDivider()
            // the site's colour in the address bar; not while the bars take the theme colour
            if ("$tick".let { BrowserSettings.headMode() != 2 }) {
                item {
                    SettingSwitchRow(
                        title = stringResource(R.string.theme_site_color),
                        checked = "$tick".let { BrowserSettings.themeHead.value == 1 },
                        onCheckedChange = { on -> BrowserSettings.edit(context) { set(BrowserSettings.themeHead, if (on) 1 else 0) }; tick++ },
                    )
                }
                settingsFooter(getString(R.string.site_theme_info))
            }
            settingsHeader(getString(R.string.hd_screen))
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.screen_filter),
                    value = stringResource(MainConst.filterNames[screenFilter.value]),
                    onClick = { choosing = 12 },
                )
            }
            if (screenFilter.value != 0) {
                item {
                    SettingRow(
                        title = stringResource(R.string.filter_color),
                        onClick = { com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host -> DialogEditIcon(host, 5, null) } },
                        trailing = {
                            Box(
                                Modifier
                                    .size(28.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(Color(filterColor))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                            )
                        },
                    )
                }
            }
            item {
                SettingRow(
                    title = stringResource(R.string.brightness),
                    summary = "$tick".let {
                        if (PdfSettings.userBrightness.value) PdfSettings.brightness.value.toString() + "%"
                        else getString(R.string.system_name) + "\n" + getString(R.string.screen_info_system)
                    },
                    onClick = {
                        choosing = 18
                    },
                )
            }
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.screen_off),
                    summary = if (MainConst.screenOffInfos[screenOff.value] != 0) stringResource(MainConst.screenOffInfos[screenOff.value]) else null,
                    options = MainConst.screenOffNames.map { stringResource(it) },
                    selectedIndex = screenOff.value,
                    onSelect = { screenOff.set(it) },
                )
            }
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.screen_rotate),
                    options = rotateLabels.map { stringResource(it) },
                    selectedIndex = rotate.value,
                    onSelect = { rotate.set(it) },
                )
            }
            settingsHeader(getString(R.string.hd_text))
            // text size and font (from Web content)
            textFontSection(tick) { getString(it) }
        }

        when (choosing) {
            12 -> ChoiceDialog(
                title = stringResource(R.string.screen_filter),
                options = MainConst.filterValues.map { stringResource(MainConst.filterNames[it]) },
                selectedIndex = MainConst.filterValues.indexOf(screenFilter.value),
                onSelect = { screenFilter.set(MainConst.filterValues[it]) },
                onDismiss = { choosing = 0 },
            )
            18 -> BrightnessDialog(context, window) { choosing = 0; tick++ }
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    resetTextFont(context)
                    themeSys.reload(BrowserSettings.themeSys.value); darkImage.reload(BrowserSettings.themeImg.value); darkHome.reload(BrowserSettings.darkHome.value)
                    screenFilter.reload(EditorSettings.scrFilUse.value); screenOff.reload(PdfSettings.screenOff.value); rotate.reload(MainSettings.appRotation.value)
                    webTheme = BrowserSettings.themeWeb.value
                    filterColor = MainUtil.alphaColor(EditorSettings.scrFilColor.value, EditorSettings.scrFilAlpha.value)
                    tick++
                    refreshTheme()
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Theme values in menu order (0 light, 1 dark) and the label of each value (index = value). */
        @JvmField
        val themeValues: IntArray = intArrayOf(0, 1)

        @JvmField
        val themeLabels: IntArray = intArrayOf(R.string.theme_light, R.string.theme_dark)

        @JvmField
        val rotateLabels: IntArray = intArrayOf(R.string.system_name, R.string.view_port, R.string.view_land)

        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            val color = MainConst.gradientColors[7]
            val pos = MainConst.presetPositions[7]
            if (!BrowserSettings.themeSys.isDefault) BrowserSettings.reset(context, listOf(BrowserSettings.themeSys))
            if (BrowserSettings.themeUi.value != 0) { BrowserSettings.editLocal { set(BrowserSettings.themeUi, 0) }; AppState.darkUi = false; PrefSet.removeNow(context, 14, "mThemeUi") }
            if (BrowserSettings.themeWeb.value != 0) { BrowserSettings.editLocal { set(BrowserSettings.themeWeb, 0) }; AppState.darkWeb = false; PrefSet.removeNow(context, 14, "mThemeWeb") }
            if (!BrowserSettings.themeImg.value) BrowserSettings.reset(context, listOf(BrowserSettings.themeImg))
            if (BrowserSettings.imgBright.value != 70) BrowserSettings.reset(context, listOf(BrowserSettings.imgBright))
            if (BrowserSettings.themeHead.value != 1) BrowserSettings.reset(context, listOf(BrowserSettings.themeHead))
            if (BrowserSettings.colorTheme.value || !BrowserSettings.themeColor.isDefault || BrowserSettings.colorBars.value) BrowserSettings.reset(context, listOf(BrowserSettings.colorTheme, BrowserSettings.themeColor, BrowserSettings.colorBars))
            if (BrowserSettings.wallColors.value != 1) BrowserSettings.edit(context) { set(BrowserSettings.wallColors, 1) } // System colours, as on a new install
            if (BrowserSettings.headIndex.value != 16) { BrowserSettings.editLocal { set(BrowserSettings.headIndex, 16) }; MainUtil.resetTextSizePref(); PrefSet.removeNow(context, 14, "mHeadIndex") }
            if (!BrowserSettings.darkHome.value) BrowserSettings.reset(context, listOf(BrowserSettings.darkHome))
            if (EditorSettings.scrFilUse.value != 0) EditorSettings.reset(context, listOf(EditorSettings.scrFilUse))
            if (EditorSettings.scrFilAlpha.value != 60 || EditorSettings.scrFilColor.value != color || EditorSettings.scrFilPos.value.compareTo(pos) != 0) {
                EditorSettings.editLocal { set(EditorSettings.scrFilAlpha, 60) }; EditorSettings.editLocal { set(EditorSettings.scrFilColor, color) }; EditorSettings.editLocal { set(EditorSettings.scrFilPos, pos) }; PrefEditor.scrFilArgb = MainUtil.alphaColor(color, 60)
                val p = PrefEditor.get(context)
                p!!.removeKey("mScrFilAlpha"); p.removeKey("mScrFilColor"); p.removeKey("mScrFilPos"); p.save()
            }
            if (PdfSettings.userBrightness.value || PdfSettings.brightness.value != 90) {
                PdfSettings.editLocal { set(PdfSettings.userBrightness, false) }; PdfSettings.editLocal { set(PdfSettings.brightness, 90) }
                val p = PrefPdf.get(context, false)
                p!!.removeKey("mUserBright"); p.removeKey("mBright"); p.save()
            }
            if (PdfSettings.screenOff.value != 0) PdfSettings.reset(context, listOf(PdfSettings.screenOff))
            if (!MainSettings.appRotation.isDefault) MainSettings.reset(context, listOf(MainSettings.appRotation))
        }
    }
}

/**
 * Fold-out sample photo under "Image brightness": shows the photo dimmed the way web pages dim images in dark mode
 * (CSS `brightness(n%)` = every colour channel times n / 100), following the slider while it is dragged.
 */
@Composable
private fun BrightnessPreview(percent: Int) {
    var open by rememberSaveable { mutableStateOf(false) }
    val turn by animateFloatAsState(if (open) 180f else 0f, label = "arrow")
    val f = percent / 100f
    val filter = remember(percent) { ColorFilter.colorMatrix(ColorMatrix().apply { setToScale(f, f, f, 1f) }) }
    Column(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(
            Modifier.clip(RoundedCornerShape(50)).clickable { open = !open }.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.preview), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Icon(
                Icons.Filled.KeyboardArrowDown, null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp).size(20.dp).rotate(turn),
            )
        }
        AnimatedVisibility(visible = open) {
            Image(
                painterResource(R.drawable.preview_matterhorn), null,
                contentScale = ContentScale.Crop,
                alignment = BiasAlignment(0f, -0.45f), // summit and lake, not the meadow in the middle
                colorFilter = filter,
                modifier = Modifier.padding(top = 4.dp).fillMaxWidth().height(260.dp).clip(RoundedCornerShape(12.dp)).background(Color.Black),
            )
        }
    }
}
