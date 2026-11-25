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
import com.mycompany.app.dialog.DialogSetPrivacy
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Settings › Privacy › Clear data (2026-11-25, was part of Privacy): delete data now (the delete sheet [DialogSetPrivacy],
 * which the browser menu opens too), what is deleted when the app closes (one switch per item, a dialog before), and
 * the notification then. The browser opens this page from the delete sheet (`EXTRA_POPUP`) and learns from the result
 * whether the open tabs were deleted or what to delete now changed.
 */
class ClearDataSettingsActivity : OpumComposeActivity() {
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
        val exitMask = rememberPref({ BrowserSettings.exitDelete.value }) { BrowserSettings.edit(context) { set(BrowserSettings.exitDelete, it) } }
        val exitNoti = rememberPref({ BrowserSettings.exitNoti.value }) { BrowserSettings.edit(context) { set(BrowserSettings.exitNoti, it) } }
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.clear_data),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            item {
                SettingRow(
                    title = stringResource(R.string.now_delete),
                    onClick = {
                        com.mycompany.app.ui.prefs.DialogShow.direct(this@ClearDataSettingsActivity) { host ->
                            DialogSetPrivacy(host, false, false, object : DialogSetPrivacy.TabDeletedListener {
                                override fun onDeleted() { tabsDeleted = true }
                            })
                        }
                    },
                )
            }
            settingsFooter(getString(R.string.clean_data_info))
            settingsHeader(getString(R.string.priv_exit))
            item { ExitRow(title = stringResource(R.string.recent_search), bit = 2, mask = exitMask) }
            item { ExitRow(title = stringResource(R.string.history), bit = 4, mask = exitMask) }
            item { ExitRow(title = stringResource(R.string.cookie), bit = 8, mask = exitMask) }
            item { ExitRow(title = stringResource(R.string.cache), bit = 16, mask = exitMask) }
            item { ExitRow(title = stringResource(R.string.normal_tab), bit = 32, mask = exitMask) }
            item { ExitRow(title = stringResource(R.string.secret_tab), bit = 64, mask = exitMask) }
            settingsFooter(getString(R.string.exit_guide))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.exit_del_noti), checked = exitNoti.value, onCheckedChange = exitNoti::set,
                    enabled = exitMask.value != 0,
                )
            }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    exitMask.reload(BrowserSettings.exitDelete.value); exitNoti.reload(BrowserSettings.exitNoti.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /** One thing deleted when the app closes ([bit] of `exitDelete`). */
    @Composable
    private fun ExitRow(title: String, bit: Int, mask: com.mycompany.app.ui.prefs.PrefState<Int>) {
        SettingSwitchRow(
            title = title, checked = (mask.value and bit) != 0,
            onCheckedChange = { on -> mask.set(if (on) mask.value or bit else mask.value and bit.inv()) },
        )
    }

    companion object {

        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (BrowserSettings.dataDelete.value != 30) BrowserSettings.reset(context, listOf(BrowserSettings.dataDelete))
            if (BrowserSettings.exitDelete.value != 0) BrowserSettings.reset(context, listOf(BrowserSettings.exitDelete))
            if (!BrowserSettings.exitNoti.value) BrowserSettings.reset(context, listOf(BrowserSettings.exitNoti))
        }
    }
}
