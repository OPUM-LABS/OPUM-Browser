package com.mycompany.app.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.MainSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.ColorWheelPicker
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingSliderRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Settings › Appearance › Screen (2026-11-25, was part of Appearance): the screen filter with its colour (a colour
 * wheel and its transparency, a dialog before), the brightness (system or own, with a slider the screen follows; a
 * dialog before), the screen timeout and rotation.
 */
class ScreenSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val screenFilter = rememberPref({ EditorSettings.scrFilUse.value }) { EditorSettings.edit(context) { set(EditorSettings.scrFilUse, it) } }
        val filterColor = rememberPref({ EditorSettings.scrFilColor.value }) {
            EditorSettings.edit(context) { set(EditorSettings.scrFilColor, it) }
            PrefEditor.scrFilArgb = MainUtil.alphaColor(it, EditorSettings.scrFilAlpha.value)
        }
        val filterAlpha = rememberPref({ EditorSettings.scrFilAlpha.value.coerceIn(0, 90) }) {
            EditorSettings.edit(context) { set(EditorSettings.scrFilAlpha, it) }
            PrefEditor.scrFilArgb = MainUtil.alphaColor(EditorSettings.scrFilColor.value, it)
        }
        val userBright = rememberPref({ PdfSettings.userBrightness.value }) { PdfSettings.edit(context) { set(PdfSettings.userBrightness, it) } }
        val bright = rememberPref({ PdfSettings.brightness.value.coerceIn(0, 100) }) { PdfSettings.edit(context) { set(PdfSettings.brightness, it) } }
        val screenOff = rememberPref({ PdfSettings.screenOff.value }) { PdfSettings.edit(context) { set(PdfSettings.screenOff, it) } }
        val rotate = rememberPref({ MainSettings.appRotation.value }) {
            MainSettings.edit(context) { set(MainSettings.appRotation, it) }
            MainUtil.applyImageOrientation(this)
        }
        var choosing by remember { mutableIntStateOf(0) } // 12 filter
        var resetting by remember { mutableStateOf(false) }
        // the window shows the brightness chosen here; back to the stored one when the page closes
        DisposableEffect(Unit) {
            onDispose { MainUtil.applyWindowBrightness(window, PdfSettings.brightness.value, PdfSettings.userBrightness.value) }
        }

        SettingsScaffold(
            title = stringResource(R.string.hd_screen),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.screen_filter),
                    value = stringResource(MainConst.filterNames[screenFilter.value]),
                    onClick = { choosing = 12 },
                )
            }
            if (screenFilter.value != 0) {
                item { SettingRow(title = stringResource(R.string.filter_color), onClick = null) }
                item {
                    ColorWheelPicker(
                        color = filterColor.value,
                        onChange = { EditorSettings.editLocal { set(EditorSettings.scrFilColor, it) }; PrefEditor.scrFilArgb = MainUtil.alphaColor(it, filterAlpha.value) },
                        onCommit = { filterColor.set(it) },
                        wheelDescription = stringResource(R.string.filter_color),
                        brightnessLabel = stringResource(R.string.brightness),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                item {
                    SettingSliderRow(
                        title = stringResource(R.string.color_alpha), value = filterAlpha.value, range = 0..90,
                        onValueChange = filterAlpha::set, label = { "$it%" },
                    )
                }
            }
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.brightness),
                    summary = stringResource(if (userBright.value) R.string.bright_info else R.string.screen_info_system),
                    options = listOf(stringResource(R.string.system_name), stringResource(R.string.user_defined)),
                    selectedIndex = if (userBright.value) 1 else 0,
                    onSelect = { userBright.set(it == 1); MainUtil.applyWindowBrightness(window, bright.value, it == 1) },
                )
            }
            item {
                SettingSliderRow(
                    title = stringResource(R.string.user_defined), value = bright.value, range = 0..100, label = { "$it%" },
                    onValueChange = { bright.set(it); MainUtil.applyWindowBrightness(window, it, userBright.value) },
                    enabled = userBright.value,
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
                    options = DisplaySettingsActivity.rotateLabels.map { stringResource(it) },
                    selectedIndex = rotate.value,
                    onSelect = { rotate.set(it) },
                )
            }
        }

        if (choosing == 12) {
            ChoiceDialog(
                title = stringResource(R.string.screen_filter),
                options = MainConst.filterValues.map { stringResource(MainConst.filterNames[it]) },
                selectedIndex = MainConst.filterValues.indexOf(screenFilter.value),
                onSelect = { screenFilter.set(MainConst.filterValues[it]) },
                onDismiss = { choosing = 0 },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    screenFilter.reload(EditorSettings.scrFilUse.value); filterColor.reload(EditorSettings.scrFilColor.value); filterAlpha.reload(EditorSettings.scrFilAlpha.value)
                    userBright.reload(PdfSettings.userBrightness.value); bright.reload(PdfSettings.brightness.value)
                    MainUtil.applyWindowBrightness(window, bright.value, userBright.value)
                    screenOff.reload(PdfSettings.screenOff.value); rotate.reload(MainSettings.appRotation.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            val color = MainConst.gradientColors[7]
            val pos = MainConst.presetPositions[7]
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
