package com.mycompany.app.ui.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.TtsSettings
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
 * Settings › Tabs › Opening tabs (2026-11-25, was part of the Tabs page): where new tabs go, grouping, which places
 * open a link in a new tab (a dialog before, now one switch each), the keyboard and the most open tabs.
 */
class TabOpenSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val addLast = rememberPref({ TtsSettings.tabLast.value }) { TtsSettings.edit(context) { set(TtsSettings.tabLast, it) } }
        val autoGroup = rememberPref({ TtsSettings.autoGroup.value }) { TtsSettings.edit(context) { set(TtsSettings.autoGroup, it) } }
        val keypad = rememberPref({ BrowserSettings.tabKeypad.value }) { BrowserSettings.edit(context) { set(BrowserSettings.tabKeypad, it) } }
        val tabLimit = rememberPref({ ZtwoSettings.tabLimit.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.tabLimit, it) } }
        // the places a link can open in a new tab from, one bit each
        val openFrom = rememberPref({ ZtwoSettings.tabOpen.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.tabOpen, it) } }
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.tab_open_page),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.hd_new_tabs))
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.tab_add_pos),
                    options = listOf(stringResource(R.string.tab_add_next), stringResource(R.string.tab_add_last)),
                    selectedIndex = if (addLast.value) 1 else 0,
                    onSelect = { addLast.set(it == 1) },
                )
            }
            item {
                SettingSwitchRow(title = stringResource(R.string.auto_group), checked = autoGroup.value, onCheckedChange = autoGroup::set)
            }
            settingsFooter(getString(R.string.auto_group_info))
            item {
                SettingSwitchRow(title = stringResource(R.string.tab_keyboard), checked = keypad.value, onCheckedChange = keypad::set)
            }
            limitRows(R.string.open_limit, tabLimit, 3..100, 20)
            if (tabLimit.value > 2) settingsFooter(getString(R.string.limit_info_1))
            settingsHeader(getString(R.string.open_new_tab))
            item { OpenFromRow(stringResource(R.string.link), 2, openFrom) }
            item { OpenFromRow(stringResource(R.string.search_url), 4, openFrom) }
            item { OpenFromRow(stringResource(R.string.quick_access), 8, openFrom) }
            item { OpenFromRow(stringResource(R.string.bookmark), 16, openFrom) }
            item { OpenFromRow(stringResource(R.string.history), 32, openFrom) }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    addLast.reload(TtsSettings.tabLast.value); autoGroup.reload(TtsSettings.autoGroup.value)
                    keypad.reload(BrowserSettings.tabKeypad.value); tabLimit.reload(ZtwoSettings.tabLimit.value)
                    openFrom.reload(ZtwoSettings.tabOpen.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /** One place a link can open in a new tab from ([bit] of the stored mask). */
    @Composable
    private fun OpenFromRow(title: String, bit: Int, mask: com.mycompany.app.ui.prefs.PrefState<Int>) {
        SettingSwitchRow(
            title = title, checked = (mask.value and bit) != 0,
            onCheckedChange = { on -> mask.set(if (on) mask.value or bit else mask.value and bit.inv()) },
        )
    }

    companion object {
        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (TtsSettings.tabLast.value) TtsSettings.reset(context, listOf(TtsSettings.tabLast))
            if (TtsSettings.autoGroup.value) TtsSettings.reset(context, listOf(TtsSettings.autoGroup))
            if (BrowserSettings.tabKeypad.value) BrowserSettings.reset(context, listOf(BrowserSettings.tabKeypad))
            if (ZtwoSettings.tabLimit.value != 0) ZtwoSettings.reset(context, listOf(ZtwoSettings.tabLimit))
            if (ZtwoSettings.tabOpen.value != 0) ZtwoSettings.reset(context, listOf(ZtwoSettings.tabOpen))
        }
    }
}
