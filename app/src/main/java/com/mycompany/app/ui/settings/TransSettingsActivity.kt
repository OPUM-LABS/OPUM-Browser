package com.mycompany.app.ui.settings

import android.content.Context
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ZtwoSettings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogEditIcon
import com.mycompany.app.dialog.DialogSeekSimple
import com.mycompany.app.dialog.DialogTransLang
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingSliderRow
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Translator settings: when to translate, target language, recent languages, the translate button (visibility,
 * position, colour) and the list of excluded sites. Language picker, icon colour editor and the exception list are
 * legacy dialogs shown through `DialogShow.direct`.
 */
class TransSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val path = intent.getStringExtra("EXTRA_PATH")
        val mode = rememberPref({ AlbumSettings.transMode.value }) { AlbumSettings.edit(context) { set(AlbumSettings.transMode, it) } }
        val recent = rememberPref({ ZtwoSettings.recentTrans.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.recentTrans, it) } }
        val iconAlways = rememberPref({ AlbumSettings.transIcon.value }) { AlbumSettings.edit(context) { set(AlbumSettings.transIcon, it) } }
        val onPage = rememberPref({ AlbumSettings.transPage.value }) { AlbumSettings.edit(context) { set(AlbumSettings.transPage, it) } }
        var locale by remember { mutableStateOf(AlbumSettings.transPick.value ?: "") }
        var iconColor by remember { mutableIntStateOf(PrefEditor.withAlpha(EditorSettings.trnsColor.value, EditorSettings.trnsAlpha.value)) }
        var choosing by remember { mutableIntStateOf(0) } // 1 mode, 3 recent, 6 icon position
        var resetting by remember { mutableStateOf(false) }

        OnScreenResume {
            locale = AlbumSettings.transPick.value ?: ""
            iconColor = PrefEditor.withAlpha(EditorSettings.trnsColor.value, EditorSettings.trnsAlpha.value)
            recent.reload(ZtwoSettings.recentTrans.value)
        }

        SettingsScaffold(
            title = stringResource(R.string.set_trans_tts),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            // text to speech had its own entry on the settings start page until 2026-11-19
            settingsHeader(getString(R.string.hd_read_aloud))
            screenLinkRow(R.string.tts_mode) { it.startActivity(android.content.Intent(it, TtsSettingsActivity::class.java)) }
            settingsHeader(getString(R.string.translator))
            item {
                SettingRow(
                    title = stringResource(R.string.trans_detect),
                    summary = stringResource(modeLabels[mode.value]) +
                        if (modeInfos[mode.value] != 0) "\n" + stringResource(modeInfos[mode.value]) else "",
                    onClick = { choosing = 1 },
                )
            }
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.locale),
                    value = locale,
                    onClick = {
                        com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host -> DialogTransLang(host, false, null) }
                    },
                )
            }
            // recent languages: 0 = do not save, < 0 = unlimited, > 0 = how many (slider instead of the number dialog)
            settingsHeader(getString(R.string.recent_lang))
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.hd_keep),
                    options = listOf(stringResource(R.string.history_zero), stringResource(R.string.history_none), stringResource(R.string.user_defined)),
                    selectedIndex = when { recent.value == 0 -> 0; recent.value < 0 -> 1; else -> 2 },
                    onSelect = { index -> recent.set(when (index) { 0 -> 0; 1 -> -1; else -> 5 }) },
                )
            }
            if (recent.value > 0) {
                item {
                    SettingSliderRow(title = stringResource(R.string.hd_count), value = recent.value, range = 1..50, onValueChange = recent::set)
                }
            }
            settingsHeader(getString(R.string.hd_trans_button))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.trans_icon_always),
                    checked = iconAlways.value,
                    onCheckedChange = iconAlways::set,
                )
            }
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.icon_pos),
                    summary = if (onPage.value) stringResource(R.string.drag_move_guide) else null,
                    options = listOf(stringResource(R.string.address_bar), stringResource(R.string.float_button)),
                    selectedIndex = if (onPage.value) 1 else 0,
                    onSelect = { onPage.set(it == 1) },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.icon_color),
                    onClick = { com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host -> DialogEditIcon(host, 10, null) } },
                    trailing = {
                        Box(
                            Modifier
                                .size(28.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(Color(iconColor))
                                .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                        )
                    },
                )
            }
            settingsDivider()
            item {
                SettingRow(
                    title = stringResource(R.string.trans_except),
                    onClick = {
                        com.mycompany.app.ui.lists.SiteListActivity.open(context, 29)
                    },
                )
            }
            settingsFooter(getString(R.string.trans_except_info))
        }

        when (choosing) {
            1 -> ChoiceDialog(
                title = stringResource(R.string.trans_detect),
                options = modeValues.map { stringResource(modeLabels[it]) },
                selectedIndex = modeValues.indexOf(mode.value),
                onSelect = { mode.set(modeValues[it]) },
                onDismiss = { choosing = 0 },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    mode.reload(AlbumSettings.transMode.value); recent.reload(ZtwoSettings.recentTrans.value)
                    iconAlways.reload(AlbumSettings.transIcon.value); onPage.reload(AlbumSettings.transPage.value)
                    iconColor = PrefEditor.withAlpha(EditorSettings.trnsColor.value, EditorSettings.trnsAlpha.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Translation modes in menu order (1 auto, 2 always, 0 not used); labels/infos are indexed by mode value. */
        @JvmField
        val modeValues: IntArray = intArrayOf(1, 2, 0)

        @JvmField
        val modeLabels: IntArray = intArrayOf(R.string.not_used, R.string.trans_auto, R.string.trans_always)

        @JvmField
        val modeInfos: IntArray = intArrayOf(0, R.string.trans_auto_info, R.string.trans_always_info)

        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            val color = MainConst.gradientColors[5]
            val pos = MainConst.presetPositions[5]
            if (AlbumSettings.transMode.value != 0) AlbumSettings.reset(context, listOf(AlbumSettings.transMode))
            if (ZtwoSettings.recentTrans.value != 5) ZtwoSettings.reset(context, listOf(ZtwoSettings.recentTrans))
            if (AlbumSettings.transIcon.value) AlbumSettings.reset(context, listOf(AlbumSettings.transIcon))
            if (AlbumSettings.transPage.value) AlbumSettings.reset(context, listOf(AlbumSettings.transPage))
            if (EditorSettings.trnsAlpha.value != 0 || EditorSettings.trnsColor.value != color || EditorSettings.trnsPos.value.compareTo(pos) != 0) {
                EditorSettings.editLocal { set(EditorSettings.trnsAlpha, 0) }; EditorSettings.editLocal { set(EditorSettings.trnsColor, color) }; EditorSettings.editLocal { set(EditorSettings.trnsPos, pos) }; PrefEditor.trnsArgb = PrefEditor.withAlpha(color, 0)
                val p = PrefEditor.get(context)
                p!!.removeKey("mTrnsAlpha"); p.removeKey("mTrnsColor"); p.removeKey("mTrnsPos"); p.save()
            }
        }
    }
}
