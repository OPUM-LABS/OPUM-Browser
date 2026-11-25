package com.mycompany.app.ui.settings

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.FullScreenPreview
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Settings › Toolbars & controls › Full screen and Toolbar (2026-11-25, was a dialog): the animated preview and the four
 * switches, which apply at once like the other pages. The browser menu keeps its dialog ([com.mycompany.app.dialog.DialogSetFull]).
 */
class FullScreenSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val showStatus = rememberPref({ BrowserSettings.showStatus.value }) { BrowserSettings.edit(context) { set(BrowserSettings.showStatus, it) } }
        val showNavi = rememberPref({ BrowserSettings.showNavi.value }) { BrowserSettings.edit(context) { set(BrowserSettings.showNavi, it) } }
        val fixTop = rememberPref({ BrowserSettings.fixTop.value }) { BrowserSettings.edit(context) { set(BrowserSettings.fixTop, it) } }
        val fixBot = rememberPref({ BrowserSettings.fixBot.value }) { BrowserSettings.edit(context) { set(BrowserSettings.fixBot, it) } }
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.full_tool),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            if (resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                item {
                    // re-read on rotation
                    @Suppress("UNUSED_VARIABLE") val orientation = LocalConfiguration.current.orientation
                    FullScreenPreview(showStatus.value, showNavi.value, fixTop.value, fixBot.value)
                }
            }
            item { SettingSwitchRow(title = stringResource(R.string.show_status), checked = showStatus.value, onCheckedChange = showStatus::set) }
            item { SettingSwitchRow(title = stringResource(R.string.show_navi), checked = showNavi.value, onCheckedChange = showNavi::set) }
            item { SettingSwitchRow(title = stringResource(R.string.scroll_top), checked = !fixTop.value, onCheckedChange = { fixTop.set(!it) }) }
            item { SettingSwitchRow(title = stringResource(R.string.scroll_bot), checked = !fixBot.value, onCheckedChange = { fixBot.set(!it) }) }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    BrowserSettings.reset(context, listOf(BrowserSettings.showStatus, BrowserSettings.showNavi, BrowserSettings.fixTop, BrowserSettings.fixBot))
                    showStatus.reload(BrowserSettings.showStatus.value); showNavi.reload(BrowserSettings.showNavi.value)
                    fixTop.reload(BrowserSettings.fixTop.value); fixBot.reload(BrowserSettings.fixBot.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }
}
