package com.mycompany.app.ui.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Settings › Web pages › Content and reading (2026-11-25, was part of Web pages): images, zooming with two fingers,
 * pictures in reader mode, data saving and Google AMP.
 */
class WebContentSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val showImage = rememberPref({ !ZoneSettings.hideImage.value }) { ZoneSettings.edit(context) { set(ZoneSettings.hideImage, !it) } }
        val scale = rememberPref({ ZtwoSettings.webScale.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.webScale, it) } }
        val readImages = rememberPref({ ReadSettings.readImages.value }) { ReadSettings.edit(context) { set(ReadSettings.readImages, it) } }
        val saveData = rememberPref({ PdfSettings.saveData.value }) { PdfSettings.edit(context) { set(PdfSettings.saveData, it) } }
        val blockAmp = rememberPref({ PdfSettings.blockAmp.value }) { PdfSettings.edit(context) { set(PdfSettings.blockAmp, it) } }
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.web_content_page),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.hd_page_content))
            item { SettingSwitchRow(title = stringResource(R.string.show_image), checked = showImage.value, onCheckedChange = showImage::set) }
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.free_scale),
                    options = WebSettingsActivity.scaleValues.map { stringResource(WebSettingsActivity.scaleLabels[it]) },
                    selectedIndex = WebSettingsActivity.scaleValues.indexOf(scale.value),
                    onSelect = { scale.set(WebSettingsActivity.scaleValues[it]) },
                )
            }
            // reader mode is one reader now (the "simple page" in the tab went, 2026-11-26): only its pictures are a choice
            item { SettingSwitchRow(title = stringResource(R.string.read_images), checked = readImages.value, onCheckedChange = readImages::set) }
            settingsHeader(getString(R.string.hd_data))
            item { SettingSwitchRow(title = stringResource(R.string.save_data), checked = saveData.value, onCheckedChange = saveData::set) }
            settingsFooter(getString(R.string.save_data_info))
            item { SettingSwitchRow(title = stringResource(R.string.block_amp), checked = blockAmp.value, onCheckedChange = blockAmp::set) }
            settingsFooter(getString(R.string.block_amp_info))
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    showImage.reload(!ZoneSettings.hideImage.value); scale.reload(ZtwoSettings.webScale.value); readImages.reload(ReadSettings.readImages.value)
                    saveData.reload(PdfSettings.saveData.value); blockAmp.reload(PdfSettings.blockAmp.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (ZoneSettings.hideImage.value) ZoneSettings.reset(context, listOf(ZoneSettings.hideImage))
            if (ZtwoSettings.webScale.value != 1) ZtwoSettings.reset(context, listOf(ZtwoSettings.webScale))
            if (ReadSettings.readImages.value) ReadSettings.reset(context, listOf(ReadSettings.readImages))
            if (PdfSettings.saveData.value) PdfSettings.reset(context, listOf(PdfSettings.saveData))
            if (!PdfSettings.blockAmp.value) PdfSettings.reset(context, listOf(PdfSettings.blockAmp))
        }
    }
}
