package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.text.TextUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSetDown
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingSliderRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsJump
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Settings › Media › Videos (2026-11-25): the video manager button with the phone preview ([MediaButtonActivity]),
 * the buttons on a video, the player for full screen videos, and background playback.
 */
class MediaVideoSettingsActivity : MediaButtonActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val pos = rememberPref({ ZoneSettings.downPos.value }) { ZoneSettings.edit(context) { set(ZoneSettings.downPos, it) } }
        val color = rememberPref({ EditorSettings.iconColor.value }) { storeColor(false, it) }
        val alpha = rememberPref({ EditorSettings.iconAlpha.value.coerceIn(0, 90) }) { storeAlpha(false, it) }
        val pip = rememberPref({ ZoneSettings.showPip.value }) { ZoneSettings.edit(context) { set(ZoneSettings.showPip, it) } }
        val full = rememberPref({ ZoneSettings.showFull.value }) { ZoneSettings.edit(context) { set(ZoneSettings.showFull, it) } }
        val down = rememberPref({ ZoneSettings.showDown.value }) { ZoneSettings.edit(context) { set(ZoneSettings.showDown, it) } }
        val backPlay = rememberPref({ ZoneSettings.backPlay.value }) {
            ZoneSettings.edit(context) { set(ZoneSettings.backPlay, it) }
            if (!it) {
                MainUtil.clearHistoryData(context)
                sendBroadcast(Intent("ch.opumlabs.opumbrowser.ACTION_BACK_STOP").setPackage(packageName))
            }
        }
        var playerName by remember { mutableStateOf(AlbumSettings.playName.value) }
        var choosingPlayer by remember { mutableStateOf(false) }
        var liveAlpha by remember { mutableIntStateOf(alpha.value) }
        var flash by remember { mutableStateOf(false) }
        LaunchedEffect(flash) { if (flash) { kotlinx.coroutines.delay(1800); flash = false } }
        var jump by remember { mutableStateOf<SettingsJump?>(null) }
        var resetting by remember { mutableStateOf(false) }
        val portrait = LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE
        val shown = pos.value != 0
        // the buttons on a video: one of them always stays on
        val onCount = listOf(pip.value, full.value, down.value).count { it }
        fun canSwitch(on: Boolean) = shown && !(on && onCount <= 1)
        val off = if (shown) null else stringResource(R.string.media_btn_off)

        SettingsScaffold(
            title = stringResource(R.string.media_videos),
            onBack = { finish() },
            onReset = { resetting = true },
            jumpTo = jump,
            above = if (!portrait) null else {
                {
                    ManagerPreview(pos.value, false, PrefEditor.withAlpha(color.value, liveAlpha), flash) {
                        flash = true
                        jump = SettingsJump(getString(R.string.video_manager))
                    }
                }
            },
        ) {
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.video_manager),
                    summary = if (pos.value == 3) stringResource(R.string.drag_move_guide) else null,
                    options = MediaSettingsActivity.positionValues.map { stringResource(MediaSettingsActivity.positionLabels[it]) },
                    selectedIndex = MediaSettingsActivity.positionValues.indexOf(pos.value),
                    onSelect = { flash = true; pos.set(MediaSettingsActivity.positionValues[it]) },
                )
            }
            item { ColorCircles(title = stringResource(R.string.icon_color), shown = shown, selected = color.value, transparency = liveAlpha) { flash = true; color.set(it) } }
            item {
                SettingSliderRow(
                    title = stringResource(R.string.color_alpha), value = alpha.value, range = 0..90, label = { "$it%" },
                    onValueChange = { alpha.set(it); liveAlpha = it }, enabled = shown,
                    below = { v -> LaunchedEffect(v) { liveAlpha = v } },
                )
            }
            settingsFooter(getString(R.string.video_down_guide_1))
            settingsFooter(getString(R.string.video_down_guide_2) + " " + getString(R.string.cast_info_6))
            settingsHeader(getString(R.string.hd_video_buttons))
            item { SettingSwitchRow(title = stringResource(R.string.pip_mode), summary = off, checked = pip.value, enabled = canSwitch(pip.value), onCheckedChange = pip::set) }
            item { SettingSwitchRow(title = stringResource(R.string.full_screen), summary = off, checked = full.value, enabled = canSwitch(full.value), onCheckedChange = full::set) }
            item { SettingSwitchRow(title = stringResource(R.string.download), summary = off, checked = down.value, enabled = canSwitch(down.value), onCheckedChange = down::set) }
            // the player for full screen videos, while the full screen button is off
            item {
                SettingRow(
                    title = stringResource(R.string.video_player),
                    summary = if (!TextUtils.isEmpty(playerName)) playerName else stringResource(R.string.default_title),
                    enabled = shown && !full.value,
                    onClick = { choosingPlayer = true },
                )
            }
            settingsFooter(getString(R.string.keep_one_button))
            settingsHeader(getString(R.string.hd_playback))
            item { SettingSwitchRow(title = stringResource(R.string.back_play), checked = backPlay.value, onCheckedChange = backPlay::set) }
            settingsFooter(getString(R.string.not_support_site))
            settingsFooter(getString(R.string.back_play_info))
        }

        if (choosingPlayer) {
            ChoiceDialog(
                title = stringResource(R.string.video_player),
                options = listOf(stringResource(R.string.default_title), stringResource(R.string.other_app)),
                selectedIndex = if (TextUtils.isEmpty(playerName)) 0 else 1,
                onSelect = { index ->
                    if (index == 0) {
                        storePlayer(context, "", "", ""); playerName = ""
                    } else {
                        DialogSetDown(this@MediaVideoSettingsActivity, "video/*", MainUtil.isLandscape(context), object : DialogSetDown.SetDownListener {
                            override fun onPick(label: String?, packageName: String?, className: String?) {
                                if (TextUtils.isEmpty(label) || TextUtils.isEmpty(packageName) || TextUtils.isEmpty(className)) return
                                storePlayer(context, label, packageName, className); playerName = label
                            }
                        })
                    }
                },
                onDismiss = { choosingPlayer = false },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetVideos(context)
                    pos.reload(ZoneSettings.downPos.value); color.reload(EditorSettings.iconColor.value)
                    alpha.reload(EditorSettings.iconAlpha.value); liveAlpha = alpha.value
                    pip.reload(ZoneSettings.showPip.value); full.reload(ZoneSettings.showFull.value); down.reload(ZoneSettings.showDown.value)
                    backPlay.reload(ZoneSettings.backPlay.value); playerName = AlbumSettings.playName.value
                },
                onDismiss = { resetting = false },
            )
        }
    }
}
