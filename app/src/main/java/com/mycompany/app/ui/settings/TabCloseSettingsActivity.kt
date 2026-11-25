package com.mycompany.app.ui.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.MainSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Settings › Tabs › Closing tabs (2026-11-25, was part of the Tabs page): what can be brought back (a dialog before,
 * now one switch each), closing all tabs with the home button, archiving inactive tabs, and the last page of a tab.
 */
class TabCloseSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val restoreWeb = rememberPref({ BrowserSettings.tabRestore.value }) { BrowserSettings.edit(context) { set(BrowserSettings.tabRestore, it) } }
        val undelete = rememberPref({ ZoneSettings.tabUndelete.value }) { ZoneSettings.edit(context) { set(ZoneSettings.tabUndelete, it) } }
        val homeClose = rememberPref({ ZoneSettings.homeClose.value }) { ZoneSettings.edit(context) { set(ZoneSettings.homeClose, it) } }
        // archive inactive tabs (2026-11-21, see TabArchive)
        val archive = rememberPref({ ZtwoSettings.tabArchive.value }) {
            ZtwoSettings.edit(context) { set(ZtwoSettings.tabArchive, it) }
            com.mycompany.app.web.TabArchive.onSettingChanged(context, it)
        }
        val archiveDelete = rememberPref({ ZtwoSettings.tabArchiveDelete.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.tabArchiveDelete, it) } }
        val lastNoti = rememberPref({ MainSettings.lastNoti.value }) { MainSettings.edit(context) { set(MainSettings.lastNoti, it) } }
        val lastSwipe = rememberPref({ ZtriSettings.lastSwipe.value }) { ZtriSettings.edit(context) { set(ZtriSettings.lastSwipe, it) } }
        val lastExit = rememberPref({ TtsSettings.lastExit.value }) { TtsSettings.edit(context) { set(TtsSettings.lastExit, it) } }
        var choosing by remember { mutableIntStateOf(0) } // 30 archive, 31 delete archived
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.tab_close_page),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.undelete))
            item { SettingSwitchRow(title = stringResource(R.string.web_page), checked = restoreWeb.value, onCheckedChange = restoreWeb::set) }
            item { SettingSwitchRow(title = stringResource(R.string.tab_list), checked = undelete.value, onCheckedChange = undelete::set) }
            settingsHeader(getString(R.string.hd_open_close))
            item {
                SettingSwitchRow(title = stringResource(R.string.home_close), checked = homeClose.value, onCheckedChange = homeClose::set)
            }
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.tab_archive),
                    value = archiveLabel(context, archive.value),
                    onClick = { choosing = 30 },
                )
            }
            if (archive.value > 0) item {
                SettingChoiceRow(
                    title = stringResource(R.string.tab_archive_delete),
                    value = archiveDeleteLabel(context, archiveDelete.value),
                    onClick = { choosing = 31 },
                )
            }
            settingsFooter(getString(R.string.tab_archive_info))
            settingsHeader(getString(R.string.hd_last_page))
            item {
                SettingSwitchRow(title = stringResource(R.string.last_noti), checked = lastNoti.value, onCheckedChange = lastNoti::set)
            }
            item {
                SettingSwitchRow(title = stringResource(R.string.last_swipe), checked = lastSwipe.value, onCheckedChange = lastSwipe::set)
            }
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.last_exit),
                    options = listOf(stringResource(R.string.delete_tab), stringResource(R.string.app_exit)),
                    selectedIndex = if (lastExit.value) 1 else 0,
                    onSelect = { lastExit.set(it == 1) },
                )
            }
            settingsFooter(getString(R.string.last_noti_info))
        }

        when (choosing) {
            30 -> ChoiceDialog(
                title = stringResource(R.string.tab_archive),
                options = com.mycompany.app.web.TabArchive.DAYS.map { archiveLabel(context, it) },
                selectedIndex = com.mycompany.app.web.TabArchive.DAYS.indexOf(archive.value),
                onSelect = { index -> archive.set(com.mycompany.app.web.TabArchive.DAYS[index]) },
                onDismiss = { choosing = 0 },
            )
            31 -> ChoiceDialog(
                title = stringResource(R.string.tab_archive_delete),
                options = com.mycompany.app.web.TabArchive.DELETE_DAYS.map { archiveDeleteLabel(context, it) },
                selectedIndex = com.mycompany.app.web.TabArchive.DELETE_DAYS.indexOf(archiveDelete.value),
                onSelect = { index -> archiveDelete.set(com.mycompany.app.web.TabArchive.DELETE_DAYS[index]) },
                onDismiss = { choosing = 0 },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    restoreWeb.reload(BrowserSettings.tabRestore.value); undelete.reload(ZoneSettings.tabUndelete.value)
                    homeClose.reload(ZoneSettings.homeClose.value)
                    archive.reload(ZtwoSettings.tabArchive.value); archiveDelete.reload(ZtwoSettings.tabArchiveDelete.value)
                    lastNoti.reload(MainSettings.lastNoti.value); lastSwipe.reload(ZtriSettings.lastSwipe.value); lastExit.reload(TtsSettings.lastExit.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /** "Not used" or "After 7 days". */
    private fun archiveLabel(context: Context, days: Int): String =
        if (days <= 0) context.getString(R.string.not_used) else context.getString(R.string.after_days, context.resources.getQuantityString(R.plurals.mem_days, days, days))

    /** "Never" or "After 30 days" (time in the archive). */
    private fun archiveDeleteLabel(context: Context, days: Int): String =
        if (days <= 0) context.getString(R.string.never) else context.getString(R.string.after_days, context.resources.getQuantityString(R.plurals.mem_days, days, days))

    companion object {
        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (!BrowserSettings.tabRestore.value) BrowserSettings.reset(context, listOf(BrowserSettings.tabRestore))
            if (!ZoneSettings.tabUndelete.isDefault) ZoneSettings.reset(context, listOf(ZoneSettings.tabUndelete))
            if (ZoneSettings.homeClose.value) ZoneSettings.reset(context, listOf(ZoneSettings.homeClose))
            if (!ZtwoSettings.tabArchive.isDefault) ZtwoSettings.reset(context, listOf(ZtwoSettings.tabArchive))
            if (ZtwoSettings.tabArchiveDelete.value != 0) ZtwoSettings.reset(context, listOf(ZtwoSettings.tabArchiveDelete))
            if (!MainSettings.lastNoti.isDefault) MainSettings.reset(context, listOf(MainSettings.lastNoti))
            if (!ZtriSettings.lastSwipe.value) ZtriSettings.reset(context, listOf(ZtriSettings.lastSwipe))
            if (TtsSettings.lastExit.value) TtsSettings.reset(context, listOf(TtsSettings.lastExit))
        }
    }
}
