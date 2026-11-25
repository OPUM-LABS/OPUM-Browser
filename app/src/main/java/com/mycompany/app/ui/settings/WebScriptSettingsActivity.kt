package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Settings › Web pages › User scripts and developer (2026-11-25, was part of Web pages): user scripts and their list
 * (`FilterScriptListActivity`, type 26), and web debugging over USB.
 */
class WebScriptSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val userScript = rememberPref({ PdfSettings.userScript.value }) { PdfSettings.edit(context) { set(PdfSettings.userScript, it) } }
        val debug = rememberPref({ PdfSettings.debugMode.value }) {
            PdfSettings.edit(context) { set(PdfSettings.debugMode, it) }
            WebView.setWebContentsDebuggingEnabled(it)
        }
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.web_scripts_page),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.hd_user_scripts))
            item { SettingSwitchRow(title = stringResource(R.string.user_script_on), checked = userScript.value, onCheckedChange = userScript::set) }
            item {
                SettingRow(
                    title = stringResource(R.string.user_script_list),
                    onClick = {
                        startActivity(
                            Intent(context, com.mycompany.app.ui.lists.FilterScriptListActivity::class.java)
                                .putExtra(com.mycompany.app.ui.lists.FilterScriptListActivity.EXTRA_TYPE, 26)
                        )
                    },
                )
            }
            settingsFooter(getString(R.string.user_script_info))
            settingsHeader(getString(R.string.hd_developer))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.debug_mode), summary = stringResource(R.string.debug_mode_info),
                    checked = debug.value, onCheckedChange = debug::set,
                )
            }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = { resetToDefaults(context); userScript.reload(PdfSettings.userScript.value); debug.reload(PdfSettings.debugMode.value) },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (PdfSettings.userScript.value) PdfSettings.reset(context, listOf(PdfSettings.userScript))
            if (PdfSettings.scriptTime.value != 0L) PdfSettings.reset(context, listOf(PdfSettings.scriptTime))
            if (PdfSettings.scriptDay.value != 7) PdfSettings.reset(context, listOf(PdfSettings.scriptDay))
            if (PdfSettings.debugMode.value) {
                PdfSettings.reset(context, listOf(PdfSettings.debugMode))
                WebView.setWebContentsDebuggingEnabled(PdfSettings.debugMode.value)
            }
        }
    }
}
