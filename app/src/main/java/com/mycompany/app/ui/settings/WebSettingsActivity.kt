package com.mycompany.app.ui.settings

import android.content.Context
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.PdfSettings
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Web pages (was "Web content"; 2026-11-19 text size and font moved to Appearance, data saving / AMP / user agent / link
 * handling moved in): images, free scale, reader mode, user scripts. The user
 * script list opens as `FilterScriptListActivity` (type 26).
 */
class WebSettingsActivity : OpumComposeActivity() {
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

    // reader mode: ask, the reading view (was "TEXT") or the simple page (was "HTML"), 2026-11-21
    private fun readModeLabel(value: Int) = when (value) { 1 -> getString(R.string.read_view); 2 -> getString(R.string.read_page); else -> getString(R.string.read_ask) }

    @Composable
    override fun Content() {
        val context: Context = this
        val path = intent.getStringExtra("EXTRA_PATH")
        val showImage = rememberPref({ !ZoneSettings.hideImage.value }) { ZoneSettings.edit(context) { set(ZoneSettings.hideImage, !it) } }
        val scale = rememberPref({ ZtwoSettings.webScale.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.webScale, it) } }
        val readMode = rememberPref({ ZtwoSettings.readMode.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.readMode, it) } }
        val userScript = rememberPref({ PdfSettings.userScript.value }) { PdfSettings.edit(context) { set(PdfSettings.userScript, it) } }
        var version by remember { mutableIntStateOf(0) } // bumped by the reset: the moved rows read their values again
        var choosing by remember { mutableIntStateOf(0) } // 2 scale, 3 reader mode
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.set_web_pages),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.hd_page_content))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.show_image),
                    checked = showImage.value,
                    onCheckedChange = showImage::set,
                )
            }
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.free_scale),
                    options = scaleValues.map { stringResource(scaleLabels[it]) },
                    selectedIndex = scaleValues.indexOf(scale.value),
                    onSelect = { scale.set(scaleValues[it]) },
                )
            }
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.reader_mode),
                    value = readModeLabel(readMode.value),
                    onClick = { choosing = 3 },
                )
            }
            settingsFooter(getString(R.string.show_image_info))
            // data saving and AMP (from Advanced), user agent and link handling (from Site)
            pageDataSection(version) { getString(it) }
            pageLinksSection(version) { getString(it) }
            settingsHeader(getString(R.string.hd_user_scripts))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.user_script_on),
                    checked = userScript.value,
                    onCheckedChange = userScript::set,
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.user_script_list),
                    onClick = {
                        startActivity(Intent(context, com.mycompany.app.ui.lists.FilterScriptListActivity::class.java).putExtra(com.mycompany.app.ui.lists.FilterScriptListActivity.EXTRA_TYPE, 26))
                    },
                )
            }
            settingsFooter(getString(R.string.user_script_info) + "\n" + getString(R.string.dark_mode_info_2))
            // web debugging (from Performance & memory, 2026-11-21)
            developerSection(version, getString(R.string.hd_developer))
        }

        if (choosing == 3) {
            ChoiceDialog(
                title = stringResource(R.string.reader_mode),
                options = readModeValues.map { readModeLabel(it) },
                selectedIndex = readModeValues.indexOf(readMode.value),
                onSelect = { readMode.set(readModeValues[it]) },
                onDismiss = { choosing = 0 },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    showImage.reload(!ZoneSettings.hideImage.value)
                    scale.reload(ZtwoSettings.webScale.value)
                    readMode.reload(ZtwoSettings.readMode.value)
                    userScript.reload(PdfSettings.userScript.value)
                    version++
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Free scale values in menu order, and the label of each value (index = value). Used by the search index. */
        @JvmField
        val scaleValues: IntArray = intArrayOf(1, 2, 0)

        @JvmField
        val scaleLabels: IntArray = intArrayOf(R.string.not_used, R.string.web_page, R.string.only_text)

        private val readModeValues = intArrayOf(1, 2, 0)

        /** Restores the defaults of this page (text size and font are on Appearance). */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (ZoneSettings.hideImage.value) ZoneSettings.reset(context, listOf(ZoneSettings.hideImage))
            if (ZtwoSettings.webScale.value != 1) ZtwoSettings.reset(context, listOf(ZtwoSettings.webScale))
            if (ZtwoSettings.readMode.value != 0) ZtwoSettings.reset(context, listOf(ZtwoSettings.readMode))
            if (PdfSettings.userScript.value) PdfSettings.reset(context, listOf(PdfSettings.userScript))
            if (PdfSettings.scriptTime.value != 0L) PdfSettings.reset(context, listOf(PdfSettings.scriptTime))
            if (PdfSettings.scriptDay.value != 7) PdfSettings.reset(context, listOf(PdfSettings.scriptDay))
            resetPageData(context)
            resetPageLinks(context)
            resetDeveloper(context)
        }
    }
}
