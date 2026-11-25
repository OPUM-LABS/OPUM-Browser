package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycompany.app.main.MainUtil
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
 * (`FilterScriptListActivity`, type 26) with a help sheet (what they are, uses, how to add one, an example to copy),
 * and web debugging over USB.
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
        var help by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.web_scripts_page),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.hd_user_scripts))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.user_script_on), summary = stringResource(R.string.us_summary),
                    checked = userScript.value, onCheckedChange = userScript::set,
                )
            }
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
            item { SettingRow(title = stringResource(R.string.us_help_title), onClick = { help = true }) }
            settingsFooter(getString(R.string.user_script_info))
            settingsHeader(getString(R.string.hd_developer))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.debug_mode), summary = stringResource(R.string.debug_mode_info),
                    checked = debug.value, onCheckedChange = debug::set,
                )
            }
        }

        if (help) UserScriptHelp(onDismiss = { help = false })
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = { resetToDefaults(context); userScript.reload(PdfSettings.userScript.value); debug.reload(PdfSettings.debugMode.value) },
                onDismiss = { resetting = false },
            )
        }
    }

    /** What user scripts are, what they are good for, how to add one, the header lines, and an example to copy. */
    @Composable
    private fun UserScriptHelp(onDismiss: () -> Unit) {
        val context: Context = this
        com.mycompany.app.ui.components.OpumSheetDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.us_help_title)) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(stringResource(R.string.us_help_what))
                    Text(stringResource(R.string.us_help_uses))
                    Text(stringResource(R.string.us_help_add, stringResource(R.string.add), stringResource(R.string.add_from_url), stringResource(R.string.new_script)))
                    Text(stringResource(R.string.us_help_meta))
                    Text(stringResource(R.string.us_help_example))
                    // code stays as it is, whatever the language
                    Text(
                        EXAMPLE, fontFamily = FontFamily.Monospace, fontSize = 12.sp, lineHeight = 16.sp,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .horizontalScroll(rememberScrollState()).padding(12.dp),
                    )
                    TextButton(onClick = { MainUtil.copyText(context, R.string.copied_clipboard, "user script", EXAMPLE) }) {
                        Text(stringResource(R.string.sec_copy))
                    }
                }
            },
            confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) } },
        )
    }

    companion object {
        /** The example in the help: bigger text on Wikipedia. */
        private const val EXAMPLE = "// ==UserScript==\n" +
            "// @name     Bigger text on Wikipedia\n" +
            "// @match    https://*.wikipedia.org/*\n" +
            "// @run-at   document-end\n" +
            "// ==/UserScript==\n" +
            "\n" +
            "document.body.style.fontSize = '120%';"

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
