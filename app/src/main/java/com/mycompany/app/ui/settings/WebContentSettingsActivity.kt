package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.store.PdfSettings
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
 * reader mode (choices on the page, a dialog before), data saving and Google AMP. Opened from the reader dialog
 * (`EXTRA_POPUP`) it tells the browser when the reader mode changed (`EXTRA_TYPE` 31).
 */
class WebContentSettingsActivity : OpumComposeActivity() {
    private var fromPopup = false
    private var readModeAtStart = 0

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        fromPopup = intent.getBooleanExtra("EXTRA_POPUP", false)
        readModeAtStart = ZtwoSettings.readMode.value
        super.onCreate(savedInstanceState)
    }

    override fun finish() {
        if (fromPopup && readModeAtStart != ZtwoSettings.readMode.value) {
            readModeAtStart = ZtwoSettings.readMode.value
            setResult(RESULT_OK, Intent().putExtra("EXTRA_TYPE", 31))
        }
        super.finish()
    }

    @Composable
    override fun Content() {
        val context: Context = this
        val showImage = rememberPref({ !ZoneSettings.hideImage.value }) { ZoneSettings.edit(context) { set(ZoneSettings.hideImage, !it) } }
        val scale = rememberPref({ ZtwoSettings.webScale.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.webScale, it) } }
        val readMode = rememberPref({ ZtwoSettings.readMode.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.readMode, it) } }
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
            // reader mode: ask, the reading view (was "TEXT") or the simple page (was "HTML"), 2026-11-21
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.reader_mode),
                    options = readModeValues.map { stringResource(readModeLabels[it]) },
                    selectedIndex = readModeValues.indexOf(readMode.value),
                    onSelect = { readMode.set(readModeValues[it]) },
                )
            }
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
                    showImage.reload(!ZoneSettings.hideImage.value); scale.reload(ZtwoSettings.webScale.value); readMode.reload(ZtwoSettings.readMode.value)
                    saveData.reload(PdfSettings.saveData.value); blockAmp.reload(PdfSettings.blockAmp.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Reader mode values in the order shown, and the label of each value (index = value: 0 ask, 1 reading view, 2 simple page). */
        private val readModeValues = intArrayOf(0, 1, 2)
        private val readModeLabels = intArrayOf(R.string.read_ask, R.string.read_view, R.string.read_page)

        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (ZoneSettings.hideImage.value) ZoneSettings.reset(context, listOf(ZoneSettings.hideImage))
            if (ZtwoSettings.webScale.value != 1) ZtwoSettings.reset(context, listOf(ZtwoSettings.webScale))
            if (ZtwoSettings.readMode.value != 0) ZtwoSettings.reset(context, listOf(ZtwoSettings.readMode))
            if (PdfSettings.saveData.value) PdfSettings.reset(context, listOf(PdfSettings.saveData))
            if (!PdfSettings.blockAmp.value) PdfSettings.reset(context, listOf(PdfSettings.blockAmp))
        }
    }
}
