package com.mycompany.app.ui.settings

import android.content.Context
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.MainSettings
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
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.dialog.DialogSetOpen
import com.mycompany.app.dialog.DialogSetTabPos
import com.mycompany.app.dialog.DialogSetTabRestore
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.ActionPickerSheet
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Tab settings. Tab bar position / detail go through `DialogShow.direct` (they need the legacy activity type);
 * the restore and "open new tab" dialogs take a plain Activity and are shown directly.
 */
class TabSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val addLast = rememberPref({ TtsSettings.tabLast.value }) { TtsSettings.edit(context) { set(TtsSettings.tabLast, it) } }
        val autoGroup = rememberPref({ TtsSettings.autoGroup.value }) { TtsSettings.edit(context) { set(TtsSettings.autoGroup, it) } }
        val swipeUp = rememberPref({ BrowserSettings.tabSwipeUp.value }) { BrowserSettings.edit(context) { set(BrowserSettings.tabSwipeUp, it) } }
        val swipeDown = rememberPref({ BrowserSettings.tabSwipeDown.value }) { BrowserSettings.edit(context) { set(BrowserSettings.tabSwipeDown, it) } }
        val indicator = rememberPref({ ZtwoSettings.tabIndi.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.tabIndi, it) } }
        val loop = rememberPref({ ZtwoSettings.tabLoop.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.tabLoop, it) } }
        val anim = rememberPref({ AlbumSettings.tabAnim.value }) { AlbumSettings.edit(context) { set(AlbumSettings.tabAnim, it) } }
        val railRight = rememberPref({ ZoneSettings.tabRailRight.value }) { ZoneSettings.edit(context) { set(ZoneSettings.tabRailRight, it) } }
        val keypad = rememberPref({ BrowserSettings.tabKeypad.value }) { BrowserSettings.edit(context) { set(BrowserSettings.tabKeypad, it) } }
        val homeClose = rememberPref({ ZoneSettings.homeClose.value }) { ZoneSettings.edit(context) { set(ZoneSettings.homeClose, it) } }
        val lastNoti = rememberPref({ MainSettings.lastNoti.value }) { MainSettings.edit(context) { set(MainSettings.lastNoti, it) } }
        val lastSwipe = rememberPref({ ZtriSettings.lastSwipe.value }) { ZtriSettings.edit(context) { set(ZtriSettings.lastSwipe, it) } }
        val lastExit = rememberPref({ TtsSettings.lastExit.value }) { TtsSettings.edit(context) { set(TtsSettings.lastExit, it) } }
        var tick by remember { mutableIntStateOf(0) } // dialogs write the legacy values directly
        var choosing by remember { mutableIntStateOf(0) } // 1 add position, 6/7 swipe menu, 20 last exit
        var picker by remember { mutableIntStateOf(0) }   // 6 or 7: action picker
        // most open tabs (moved here from Performance & memory, 2026-11-21)
        val tabLimit = rememberPref({ ZtwoSettings.tabLimit.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.tabLimit, it) } }
        // archive inactive tabs (2026-11-21, see TabArchive)
        val archive = rememberPref({ ZtwoSettings.tabArchive.value }) {
            ZtwoSettings.edit(context) { set(ZtwoSettings.tabArchive, it) }
            com.mycompany.app.web.TabArchive.onSettingChanged(context, it)
        }
        val archiveDelete = rememberPref({ ZtwoSettings.tabArchiveDelete.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.tabArchiveDelete, it) } }
        var resetting by remember { mutableStateOf(false) }
        var detail by remember { mutableStateOf(false) }

        OnScreenResume { tick++ }

        SettingsScaffold(
            title = stringResource(R.string.tab_item),
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
                SettingSwitchRow(
                    title = stringResource(R.string.auto_group),
                    checked = autoGroup.value,
                    onCheckedChange = autoGroup::set,
                )
            }
            settingsFooter(getString(R.string.auto_group_info))
            settingsHeader(getString(R.string.hd_tab_bar))
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.show_tab_bar),
                    value = "$tick".let { stringResource(DialogSetTabPos.tabBarLabels[BrowserSettings.tabBar.value]) },
                    onClick = {
                        com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host ->
                            DialogSetTabPos(host, object : DialogApplyListener { override fun onApply() {} })
                        }
                    },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.detail_setting),
                    onClick = { detail = true },
                )
            }
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.tab_swipe_up),
                    value = stringResource(MainUtil.menuIconAt(swipeUp.value)),
                    onClick = { choosing = 6 },
                )
            }
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.tab_swipe_dn),
                    value = stringResource(MainUtil.menuIconAt(swipeDown.value)),
                    onClick = { choosing = 7 },
                )
            }
            settingsHeader(getString(R.string.hd_tab_button))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.tab_indicator),
                    checked = indicator.value,
                    onCheckedChange = indicator::set,
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.tab_loop),
                    checked = loop.value,
                    onCheckedChange = loop::set,
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.use_tab_anim),
                    checked = anim.value,
                    onCheckedChange = anim::set,
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.tab_rail_right),
                    checked = railRight.value,
                    onCheckedChange = railRight::set,
                )
            }
            settingsFooter(getString(R.string.tab_indi_info) + "\n" + getString(R.string.tab_loop_info))
            settingsHeader(getString(R.string.hd_open_close))
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.undelete),
                    value = "$tick".let { restoreSummary(context) } ?: "",
                    onClick = {
                        val dialog = DialogSetTabRestore(this@TabSettingsActivity, object : DialogApplyListener {
                            override fun onApply() { tick++ }
                        })
                        dialog.setOnDismissListener { tick++ }
                    },
                )
            }
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.open_new_tab),
                    value = "$tick".let { openNewTabSummary(context) } ?: "",
                    onClick = {
                        val dialog = DialogSetOpen(this@TabSettingsActivity)
                        dialog.setOnDismissListener { tick++ }
                    },
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.tab_keyboard),
                    checked = keypad.value,
                    onCheckedChange = keypad::set,
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.home_close),
                    checked = homeClose.value,
                    onCheckedChange = homeClose::set,
                )
            }
            limitRows(R.string.open_limit, tabLimit, 3..100, 20)
            if (tabLimit.value > 2) settingsFooter(getString(R.string.limit_info_1))
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
                SettingSwitchRow(
                    title = stringResource(R.string.last_noti),
                    checked = lastNoti.value,
                    onCheckedChange = lastNoti::set,
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.last_swipe),
                    checked = lastSwipe.value,
                    onCheckedChange = lastSwipe::set,
                )
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
            6, 7 -> {
                val which = choosing
                ChoiceDialog(
                    title = stringResource(if (which == 6) R.string.tab_swipe_up else R.string.tab_swipe_dn),
                    options = listOf(stringResource(R.string.do_change), stringResource(R.string.not_used)),
                    selectedIndex = -1,
                    onSelect = { index ->
                        if (index == 0) picker = which else (if (which == 6) swipeUp else swipeDown).set(0)
                    },
                    onDismiss = { choosing = 0 },
                )
            }
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
        if (picker != 0) {
            val state = if (picker == 6) swipeUp else swipeDown
            ActionPickerSheet(
                selectedId = state.value,
                onSelect = { id -> if (id in 0 until com.mycompany.app.main.MainConst.mainMenuNames.size) state.set(id) },
                onDismiss = { picker = 0 },
            )
        }
        if (detail) TabDetailDialog(onDismiss = { detail = false })
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    addLast.reload(TtsSettings.tabLast.value); autoGroup.reload(TtsSettings.autoGroup.value)
                    swipeUp.reload(BrowserSettings.tabSwipeUp.value); swipeDown.reload(BrowserSettings.tabSwipeDown.value)
                    indicator.reload(ZtwoSettings.tabIndi.value); loop.reload(ZtwoSettings.tabLoop.value); anim.reload(AlbumSettings.tabAnim.value); railRight.reload(ZoneSettings.tabRailRight.value)
                    keypad.reload(BrowserSettings.tabKeypad.value); homeClose.reload(ZoneSettings.homeClose.value)
                    lastNoti.reload(MainSettings.lastNoti.value); lastSwipe.reload(ZtriSettings.lastSwipe.value); lastExit.reload(TtsSettings.lastExit.value)
                    tabLimit.reload(ZtwoSettings.tabLimit.value)
                    archive.reload(ZtwoSettings.tabArchive.value); archiveDelete.reload(ZtwoSettings.tabArchiveDelete.value)
                    tick++
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

    private fun restoreSummary(context: Context): String? {
        val web = BrowserSettings.tabRestore.value
        val list = ZoneSettings.tabUndelete.value
        return when {
            web && list -> context.getString(R.string.web_page) + ", " + context.getString(R.string.tab_list)
            web -> context.getString(R.string.web_page)
            list -> context.getString(R.string.tab_list)
            else -> null
        }
    }

    private fun openNewTabSummary(context: Context): String? {
        val flags = ZtwoSettings.tabOpen.value
        if (flags == 0) return null
        val labels = listOf(2 to R.string.link, 4 to R.string.search_url, 8 to R.string.quick_access, 16 to R.string.bookmark, 32 to R.string.history)
        val parts = labels.filter { (flags and it.first) == it.first }.map { context.getString(it.second) }
        return if (parts.isEmpty()) null else parts.joinToString(", ")
    }

    companion object {
        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            val color = MainConst.gradientColors[5]
            val pos = MainConst.presetPositions[5]
            if (TtsSettings.tabLast.value) TtsSettings.reset(context, listOf(TtsSettings.tabLast))
            if (ZtwoSettings.tabLimit.value != 0) ZtwoSettings.reset(context, listOf(ZtwoSettings.tabLimit))
            if (!ZtwoSettings.tabArchive.isDefault) ZtwoSettings.reset(context, listOf(ZtwoSettings.tabArchive))
            if (ZtwoSettings.tabArchiveDelete.value != 0) ZtwoSettings.reset(context, listOf(ZtwoSettings.tabArchiveDelete))
            if (TtsSettings.autoGroup.value) TtsSettings.reset(context, listOf(TtsSettings.autoGroup))
            if (BrowserSettings.tabBar.value != 0) BrowserSettings.reset(context, listOf(BrowserSettings.tabBar))
            if (!BrowserSettings.tabAccent.value) BrowserSettings.reset(context, listOf(BrowserSettings.tabAccent))
            if (BrowserSettings.tabColor.value != 5) BrowserSettings.reset(context, listOf(BrowserSettings.tabColor))
            if (PdfSettings.tabAdd.value != 3 || !PdfSettings.tabClose.value || PdfSettings.tabWidth.value != MainApp.dp144 || PdfSettings.tabHeight.value != MainApp.dp40) {
                PdfSettings.editLocal { set(PdfSettings.tabAdd, 3) }; PdfSettings.editLocal { set(PdfSettings.tabClose, true) }; PdfSettings.editLocal { set(PdfSettings.tabWidth, MainApp.dp144) }; PdfSettings.editLocal { set(PdfSettings.tabHeight, MainApp.dp40) }
                val p = PrefPdf.get(context, false)
                p!!.removeKey("mTabAdd"); p.removeKey("mTabClose"); p.removeKey("mTabWidth"); p.removeKey("mTabHeight"); p.save()
            }
            if (EditorSettings.gridAlpha.value != 0 || EditorSettings.gridColor.value != color || EditorSettings.gridPos.value.compareTo(pos) != 0) {
                EditorSettings.editLocal { set(EditorSettings.gridAlpha, 0) }; EditorSettings.editLocal { set(EditorSettings.gridColor, color) }; EditorSettings.editLocal { set(EditorSettings.gridPos, pos) }; PrefEditor.gridArgb = PrefEditor.withAlpha(color, 0)
                val p = PrefEditor.get(context)
                p!!.removeKey("mGridAlpha"); p.removeKey("mGridColor"); p.removeKey("mGridPos"); p.save()
            }
            if (BrowserSettings.tabSwipeUp.value != 30) BrowserSettings.reset(context, listOf(BrowserSettings.tabSwipeUp))
            if (BrowserSettings.tabSwipeDown.value != 38) BrowserSettings.reset(context, listOf(BrowserSettings.tabSwipeDown))
            if (ZtwoSettings.tabIndi.value) ZtwoSettings.reset(context, listOf(ZtwoSettings.tabIndi))
            if (!ZtwoSettings.tabLoop.value) ZtwoSettings.reset(context, listOf(ZtwoSettings.tabLoop))
            if (!BrowserSettings.tabRestore.value) BrowserSettings.reset(context, listOf(BrowserSettings.tabRestore))
            if (!AlbumSettings.tabAnim.value) AlbumSettings.reset(context, listOf(AlbumSettings.tabAnim))
            if (ZoneSettings.tabRailRight.value) ZoneSettings.reset(context, listOf(ZoneSettings.tabRailRight))
            if (ZtwoSettings.tabOpen.value != 0) ZtwoSettings.reset(context, listOf(ZtwoSettings.tabOpen))
            if (BrowserSettings.tabKeypad.value) BrowserSettings.reset(context, listOf(BrowserSettings.tabKeypad))
            if (ZoneSettings.homeClose.value) ZoneSettings.reset(context, listOf(ZoneSettings.homeClose))
            if (!MainSettings.lastNoti.isDefault) MainSettings.reset(context, listOf(MainSettings.lastNoti))
            if (!ZtriSettings.lastSwipe.value) ZtriSettings.reset(context, listOf(ZtriSettings.lastSwipe))
            if (TtsSettings.lastExit.value) TtsSettings.reset(context, listOf(TtsSettings.lastExit))
        }
    }
}
