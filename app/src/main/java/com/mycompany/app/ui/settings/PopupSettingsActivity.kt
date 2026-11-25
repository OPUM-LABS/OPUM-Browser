package com.mycompany.app.ui.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogGuideArea
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.InfoDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.rememberPref
import java.util.Locale

/**
 * Settings › Web filter › Pop-ups and overlays (2026-11-25, was part of Web filter): the pop-up blocker (a choice
 * dialog before) and the overlay blocker with the sites where each is allowed, hiding single page parts by long press
 * ("Block area"), and opening apps from pages; each with its guide.
 */
class PopupSettingsActivity : WebFilterActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val popBlock = rememberPref({ BrowserSettings.popBlock.value }) { BrowserSettings.edit(context) { set(BrowserSettings.popBlock, it) } }
        val overBlock = rememberPref({ TtsSettings.checkOver.value }) { TtsSettings.edit(context) { set(TtsSettings.checkOver, it) } }
        val areaBlock = rememberPref({ BrowserSettings.areaBlock.value }) { BrowserSettings.edit(context) { set(BrowserSettings.areaBlock, it) } }
        val appBlock = rememberPref({ PdfSettings.appBlock.value }) { PdfSettings.edit(context) { set(PdfSettings.appBlock, it) } }
        var guide by remember { mutableStateOf(false) }
        var resetting by remember { mutableStateOf(false) }

        fun seenHelp() { if (AlbumSettings.notiClea.value) AlbumSettings.edit(context) { set(AlbumSettings.notiClea, false) } }

        SettingsScaffold(
            title = stringResource(R.string.wf_pop_page),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.hd_popups))
            item {
                val info = CleanSettingsActivity.popInfos[popBlock.value]
                SettingSegmentedRow(
                    title = stringResource(R.string.pop_block),
                    summary = if (info != 0) stringResource(info) else null,
                    options = CleanSettingsActivity.popValues.map { stringResource(CleanSettingsActivity.popLabels[it]) },
                    selectedIndex = CleanSettingsActivity.popValues.indexOf(popBlock.value),
                    onSelect = { popBlock.set(CleanSettingsActivity.popValues[it]) },
                )
            }
            item { SettingRow(title = stringResource(R.string.pop_white), onClick = { openSiteList(21) }) }
            settingsHeader(getString(R.string.hd_elements))
            item {
                val info = CleanSettingsActivity.overInfos[overBlock.value]
                SettingSegmentedRow(
                    title = stringResource(R.string.over_block),
                    summary = if (info != 0) stringResource(info) else null,
                    options = CleanSettingsActivity.overValues.map { stringResource(CleanSettingsActivity.overLabels[it]) },
                    selectedIndex = CleanSettingsActivity.overValues.indexOf(overBlock.value),
                    onSelect = { overBlock.set(CleanSettingsActivity.overValues[it]) },
                )
            }
            item { SettingRow(title = stringResource(R.string.over_white), onClick = { openSiteList(20) }) }
            item { SettingRow(title = stringResource(R.string.wf_help_over), onClick = { seenHelp(); guide = true }) }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.block_area), summary = stringResource(R.string.area_info_13),
                    checked = areaBlock.value, onCheckedChange = areaBlock::set,
                )
            }
            item {
                SettingRow(title = stringResource(R.string.wf_help_area), onClick = {
                    seenHelp()
                    com.mycompany.app.ui.prefs.DialogShow.direct(this@PopupSettingsActivity) { host -> DialogGuideArea(host, 3, null) }
                })
            }
            settingsHeader(getString(R.string.hd_apps))
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.app_block),
                    summary = appBlockInfo(context, appBlock.value),
                    options = CleanSettingsActivity.appValues.map { stringResource(CleanSettingsActivity.appLabels[it]) },
                    selectedIndex = CleanSettingsActivity.appValues.indexOf(appBlock.value),
                    onSelect = { appBlock.set(CleanSettingsActivity.appValues[it]) },
                )
            }
        }

        if (guide) {
            InfoDialog(
                title = stringResource(R.string.wf_help_over),
                message = listOf(R.string.over_block_guide_1, R.string.over_block_guide_2, R.string.over_block_guide_3).joinToString("\n") { getString(it) } + "\n\n" +
                    String.format(Locale.US, getString(R.string.area_recom_1), getString(R.string.block_area)) + "\n" +
                    String.format(Locale.US, getString(R.string.area_recom_2), getString(R.string.block_area)),
                onDismiss = { guide = false },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    popBlock.reload(BrowserSettings.popBlock.value); overBlock.reload(TtsSettings.checkOver.value)
                    areaBlock.reload(BrowserSettings.areaBlock.value); appBlock.reload(PdfSettings.appBlock.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    private fun appBlockInfo(context: Context, value: Int): String? {
        if (value == 0) return null
        val first = context.getString(R.string.app_block_info1)
        return if (value == 2) first else first + "\n" + context.getString(R.string.app_block_info2)
    }

    companion object {
        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (TtsSettings.checkEle.value) TtsSettings.reset(context, listOf(TtsSettings.checkEle))
            if (!BrowserSettings.areaBlock.value) BrowserSettings.reset(context, listOf(BrowserSettings.areaBlock))
            if (TtsSettings.checkOver.value != 0) TtsSettings.reset(context, listOf(TtsSettings.checkOver))
            if (!BrowserSettings.popBlock.isDefault) BrowserSettings.reset(context, listOf(BrowserSettings.popBlock))
            if (PdfSettings.appBlock.value != 2) PdfSettings.reset(context, listOf(PdfSettings.appBlock))
        }
    }
}
