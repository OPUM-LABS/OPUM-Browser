package com.mycompany.app.ui.settings

import android.content.Context
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import android.content.Intent
import android.text.TextUtils
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
import com.mycompany.app.dialog.DialogSetVideo
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.ui.OpumComposeActivity
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
 * Media settings: the video manager button and the YouTube button (position, icon colour, detail dialog), background
 * play and the YouTube options. The icon colour editor and the detail dialogs are legacy dialogs shown through
 * `DialogShow.direct`.
 */
class MediaSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val downPos = rememberPref({ ZoneSettings.downPos.value }) { ZoneSettings.edit(context) { set(ZoneSettings.downPos, it) } }
        val youPos = rememberPref({ ZoneSettings.youPos.value }) { ZoneSettings.edit(context) { set(ZoneSettings.youPos, it) } }
        val backPlay = rememberPref({ ZoneSettings.backPlay.value }) {
            ZoneSettings.edit(context) { set(ZoneSettings.backPlay, it) }
            if (!it) {
                MainUtil.clearHistoryData(context)
                sendBroadcast(Intent("ch.opumlabs.opumbrowser.ACTION_BACK_STOP").setPackage(packageName))
            }
        }
        val autoPlay = rememberPref({ ZtwoSettings.autoPlay.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.autoPlay, it) } }
        val unmute = rememberPref({ ZtwoSettings.youUnmute.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.youUnmute, it) } }
        var downColor by remember { mutableIntStateOf(PrefEditor.withAlpha(EditorSettings.iconColor.value, EditorSettings.iconAlpha.value)) }
        var youColor by remember { mutableIntStateOf(PrefEditor.withAlpha(EditorSettings.ypipColor.value, EditorSettings.ypipAlpha.value)) }
        var choosing by remember { mutableIntStateOf(0) } // 1 video manager position, 10 YouTube position
        var resetting by remember { mutableStateOf(false) }

        OnScreenResume {
            downColor = PrefEditor.withAlpha(EditorSettings.iconColor.value, EditorSettings.iconAlpha.value)
            youColor = PrefEditor.withAlpha(EditorSettings.ypipColor.value, EditorSettings.ypipAlpha.value)
        }

        @Composable
        fun swatch(color: Int) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color(color))
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
            )
        }

        fun posSummary(context: Context, pos: Int) =
            context.getString(positionLabels[pos]) + if (pos == 3) "\n" + context.getString(R.string.drag_move_guide) else ""

        SettingsScaffold(
            title = stringResource(R.string.media),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.video))
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.video_manager),
                    value = posSummary(context, downPos.value),
                    onClick = { choosing = 1 },
                )
            }
            // colour and details only while the button is shown
            if (downPos.value != 0) {
                item {
                    SettingRow(
                        title = stringResource(R.string.icon_color),
                        onClick = { com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host -> DialogEditIcon(host, 0, null) } },
                        trailing = { swatch(downColor) },
                    )
                }
                item {
                    SettingRow(
                        title = stringResource(R.string.detail_setting),
                        onClick = { com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host -> DialogSetVideo(host, false) } },
                    )
                }
            }
            settingsFooter(getString(R.string.video_down_guide_1) + "\n" + getString(R.string.video_down_guide_2) + "\n" + getString(R.string.cast_info_6))
            settingsHeader(getString(R.string.hd_playback))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.back_play),
                    checked = backPlay.value,
                    onCheckedChange = backPlay::set,
                )
            }
            settingsFooter(getString(R.string.not_support_site) + "\n" + getString(R.string.back_play_info))
            settingsHeader("YouTube")
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.youtube_manager),
                    value = posSummary(context, youPos.value),
                    onClick = { choosing = 10 },
                )
            }
            if (youPos.value != 0) {
                item {
                    SettingRow(
                        title = stringResource(R.string.icon_color),
                        onClick = { com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host -> DialogEditIcon(host, 3, null) } },
                        trailing = { swatch(youColor) },
                    )
                }
                item {
                    SettingRow(
                        title = stringResource(R.string.detail_setting),
                        onClick = { com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host -> DialogSetVideo(host, true) } },
                    )
                }
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.youtube_auto),
                    checked = autoPlay.value,
                    onCheckedChange = autoPlay::set,
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.youtube_unmute),
                    checked = unmute.value,
                    onCheckedChange = unmute::set,
                )
            }
        }

        if (choosing != 0) {
            val state = if (choosing == 10) youPos else downPos
            ChoiceDialog(
                title = stringResource(if (choosing == 10) R.string.youtube_manager else R.string.video_manager),
                options = positionValues.map { stringResource(positionLabels[it]) },
                selectedIndex = positionValues.indexOf(state.value),
                onSelect = { state.set(positionValues[it]) },
                onDismiss = { choosing = 0 },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    downPos.reload(ZoneSettings.downPos.value)
                    youPos.reload(ZoneSettings.youPos.value)
                    backPlay.reload(ZoneSettings.backPlay.value)
                    autoPlay.reload(ZtwoSettings.autoPlay.value)
                    unmute.reload(ZtwoSettings.youUnmute.value)
                    downColor = PrefEditor.withAlpha(EditorSettings.iconColor.value, EditorSettings.iconAlpha.value)
                    youColor = PrefEditor.withAlpha(EditorSettings.ypipColor.value, EditorSettings.ypipAlpha.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Selectable button positions, in menu order (1 top bar, 2 bottom bar, 3 floating, 0 not used). */
        @JvmField
        val positionValues: IntArray = intArrayOf(1, 2, 3, 0)

        /** Label of each position value (index = value). Used by the settings search index too. */
        @JvmField
        val positionLabels: IntArray = intArrayOf(R.string.not_used, R.string.top_bar, R.string.bot_bar, R.string.float_button)

        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            val color = MainConst.gradientColors[5]
            val pos = MainConst.presetPositions[5]
            if (ZoneSettings.downPos.value != 3) ZoneSettings.reset(context, listOf(ZoneSettings.downPos))
            if (EditorSettings.iconAlpha.value != 0 || EditorSettings.iconColor.value != color || EditorSettings.iconPos.value.compareTo(pos) != 0) {
                EditorSettings.editLocal { set(EditorSettings.iconAlpha, 0) }; EditorSettings.editLocal { set(EditorSettings.iconColor, color) }; EditorSettings.editLocal { set(EditorSettings.iconPos, pos) }; PrefEditor.iconArgb = PrefEditor.withAlpha(color, 0)
                val p = PrefEditor.get(context)
                p!!.removeKey("mIconAlpha"); p.removeKey("mIconColor"); p.removeKey("mIconPos"); p.save()
            }
            if (!ZoneSettings.showPip.value) ZoneSettings.reset(context, listOf(ZoneSettings.showPip))
            if (!ZoneSettings.showFull.value) ZoneSettings.reset(context, listOf(ZoneSettings.showFull))
            if (!ZoneSettings.showDown.value) ZoneSettings.reset(context, listOf(ZoneSettings.showDown))
            if (!TextUtils.isEmpty(AlbumSettings.playName.value) || !TextUtils.isEmpty(AlbumSettings.playPkg.value) || !TextUtils.isEmpty(AlbumSettings.playCls.value)) {
                AlbumSettings.editLocal { set(AlbumSettings.playName, "") }; AlbumSettings.editLocal { set(AlbumSettings.playPkg, "") }; AlbumSettings.editLocal { set(AlbumSettings.playCls, "") }
                val p = PrefAlbum.get(context, false)
                p!!.removeKey("mPlayName2"); p.removeKey("mPlayPkg2"); p.removeKey("mPlayCls2"); p.save()
            }
            if (ZoneSettings.backPlay.value) ZoneSettings.reset(context, listOf(ZoneSettings.backPlay))
            if (ZoneSettings.youPos.value != 3) ZoneSettings.reset(context, listOf(ZoneSettings.youPos))
            if (EditorSettings.ypipAlpha.value != 0 || EditorSettings.ypipColor.value != color || EditorSettings.ypipPos.value.compareTo(pos) != 0) {
                EditorSettings.editLocal { set(EditorSettings.ypipAlpha, 0) }; EditorSettings.editLocal { set(EditorSettings.ypipColor, color) }; EditorSettings.editLocal { set(EditorSettings.ypipPos, pos) }; PrefEditor.ypipArgb = PrefEditor.withAlpha(color, 0)
                val p = PrefEditor.get(context)
                p!!.removeKey("mYpipAlpha"); p.removeKey("mYpipColor"); p.removeKey("mYpipPos"); p.save()
            }
            if (!ZoneSettings.youPip.value) ZoneSettings.reset(context, listOf(ZoneSettings.youPip))
            if (!ZoneSettings.youRate.value) ZoneSettings.reset(context, listOf(ZoneSettings.youRate))
            if (!ZtwoSettings.autoPlay.value) ZtwoSettings.reset(context, listOf(ZtwoSettings.autoPlay))
            if (!ZtwoSettings.youUnmute.value) ZtwoSettings.reset(context, listOf(ZtwoSettings.youUnmute))
        }
    }
}
