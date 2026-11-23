package com.mycompany.app.ui.settings

import android.content.Context
import com.mycompany.app.pref.store.BrowserSettings
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSetPrivacy
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Privacy (was "Clear data"; cookies, JavaScript, HTTPS and site permissions moved in on 2026-11-19). Clear data: delete now, delete on exit (what to delete), exit notification. The what-to-delete dialog, which also
 * performs the deletion, is still the legacy [DialogSetPrivacy] shown through `DialogShow.direct`.
 */
class PrivacySettingsActivity : OpumComposeActivity() {
    private var fromPopup = false
    private var tabsDeleted = false
    private var dataDeleteAtStart = 0

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        fromPopup = intent.getBooleanExtra("EXTRA_POPUP", false)
        dataDeleteAtStart = BrowserSettings.dataDelete.value
        super.onCreate(savedInstanceState)
    }

    override fun finish() {
        if (fromPopup) {
            if (tabsDeleted || dataDeleteAtStart != BrowserSettings.dataDelete.value) {
                dataDeleteAtStart = BrowserSettings.dataDelete.value
                val data = Intent().putExtra("EXTRA_TYPE", 40)
                if (tabsDeleted) data.putExtra("EXTRA_STATUS", true)
                setResult(RESULT_OK, data)
            }
        } else if (tabsDeleted) {
            setResult(RESULT_OK)
        }
        super.finish()
    }

    @Composable
    override fun Content() {
        val context: Context = this
        val exitNoti = rememberPref({ BrowserSettings.exitNoti.value }) { BrowserSettings.edit(context) { set(BrowserSettings.exitNoti, it) } }
        var exitSummary by remember { mutableStateOf(exitDeleteSummary(context)) }
        var resetting by remember { mutableStateOf(false) }
        var version by remember { androidx.compose.runtime.mutableIntStateOf(0) } // bumped by the reset for the moved rows

        // the dialog runs in the host and changes BrowserSettings.exitDelete.value
        OnScreenResume { exitSummary = exitDeleteSummary(context) }

        SettingsScaffold(
            title = stringResource(R.string.set_privacy),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            // cookies, JavaScript, HTTPS (from Advanced) and site permissions (from Site)
            privacyWebSection(version) { context.getString(it) }
            settingsHeader(context.getString(R.string.clear_data))
            item {
                SettingRow(
                    title = stringResource(R.string.now_delete),
                    onClick = {
                        com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host ->
                            DialogSetPrivacy(host, false, false, object : DialogSetPrivacy.TabDeletedListener {
                                override fun onDeleted() { tabsDeleted = true }
                            })
                        }
                    },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.exit_delete),
                    summary = exitSummary ?: stringResource(R.string.not_used),
                    onClick = {
                        com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host -> DialogSetPrivacy(host, true, false, null) }
                    },
                )
            }
            // the notification only matters when something is deleted on exit
            if (exitSummary != null) {
                item {
                    SettingSwitchRow(
                        title = stringResource(R.string.exit_del_noti),
                        checked = exitNoti.value,
                        onCheckedChange = exitNoti::set,
                    )
                }
            }
            settingsFooter(context.getString(R.string.exit_guide) + "\n" + context.getString(R.string.clean_data_info))
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    resetPrivacyWeb(context)
                    version++
                    exitNoti.reload(BrowserSettings.exitNoti.value)
                    exitSummary = exitDeleteSummary(context)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (BrowserSettings.dataDelete.value != 30) BrowserSettings.reset(context, listOf(BrowserSettings.dataDelete))
            if (BrowserSettings.exitDelete.value != 0) BrowserSettings.reset(context, listOf(BrowserSettings.exitDelete))
            if (!BrowserSettings.exitNoti.value) BrowserSettings.reset(context, listOf(BrowserSettings.exitNoti))
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
