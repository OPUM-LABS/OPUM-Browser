package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingsScaffold

/**
 * Settings › Privacy. Only links since 2026-11-25: cookies, JavaScript and HTTPS; clear data; site settings. When
 * the open tabs were deleted on Clear data, this page passes that on to whoever opened it. Reset restores the first
 * two pages.
 */
class PrivacySettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        var resetting by remember { mutableStateOf(false) }
        var tick by remember { androidx.compose.runtime.mutableIntStateOf(0) }
        com.mycompany.app.ui.prefs.OnScreenResume { tick++ }
        val clearResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) setResult(RESULT_OK, it.data)
        }

        SettingsScaffold(
            title = stringResource(R.string.set_privacy),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            screenLinkRow(R.string.priv_web_page) { it.startActivity(Intent(it, SiteSafetySettingsActivity::class.java)) }
            item {
                SettingRow(
                    title = stringResource(R.string.clear_data),
                    summary = "$tick".let { exitDeleteSummary(context) }?.let { stringResource(R.string.priv_exit) + ": " + it },
                    onClick = { clearResult.launch(Intent(context, ClearDataSettingsActivity::class.java)) },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.pms_site_settings),
                    summary = stringResource(R.string.ss_entry_info),
                    onClick = { SiteSettingsActivity.open(context, null) },
                )
            }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = { resetToDefaults(context) },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Restores the defaults of the Privacy pages (site settings keep their own reset). */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            SiteSafetySettingsActivity.resetToDefaults(context)
            ClearDataSettingsActivity.resetToDefaults(context)
        }

        /** Comma separated list of what is deleted on exit, or null when nothing (also used by the browser menu). */
        @JvmStatic
        fun exitDeleteSummary(context: Context): String? {
            val flags = BrowserSettings.exitDelete.value
            if (flags == 126) {
                return listOf(R.string.history, R.string.cache, R.string.cookie, R.string.normal_tab, R.string.secret_tab)
                    .joinToString(", ") { context.getString(it) }
            }
            val parts = ArrayList<String>()
            if ((flags and 2) == 2) parts.add(context.getString(R.string.recent_search))
            if ((flags and 4) == 4) parts.add(context.getString(R.string.history))
            if ((flags and 8) == 8) parts.add(context.getString(R.string.cookie))
            if ((flags and 16) == 16) parts.add(context.getString(R.string.cache))
            if ((flags and 32) == 32) parts.add(context.getString(R.string.normal_tab))
            if ((flags and 64) == 64) parts.add(context.getString(R.string.secret_tab))
            return if (parts.isEmpty()) null else parts.joinToString(", ")
        }
    }
}
