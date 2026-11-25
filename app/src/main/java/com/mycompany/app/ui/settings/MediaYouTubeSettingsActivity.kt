package com.mycompany.app.ui.settings

import android.content.Context
import android.content.res.Configuration
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
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingSliderRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsJump
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Settings › Media › YouTube (2026-11-25): the YouTube manager button with the phone preview ([MediaButtonActivity]),
 * its buttons on a video, autoplay and unmute.
 */
class MediaYouTubeSettingsActivity : MediaButtonActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val pos = rememberPref({ ZoneSettings.youPos.value }) { ZoneSettings.edit(context) { set(ZoneSettings.youPos, it) } }
        val color = rememberPref({ EditorSettings.ypipColor.value }) { storeColor(true, it) }
        val alpha = rememberPref({ EditorSettings.ypipAlpha.value.coerceIn(0, 90) }) { storeAlpha(true, it) }
        val pip = rememberPref({ ZoneSettings.youPip.value }) { ZoneSettings.edit(context) { set(ZoneSettings.youPip, it) } }
        val speed = rememberPref({ ZoneSettings.youRate.value }) { ZoneSettings.edit(context) { set(ZoneSettings.youRate, it) } }
        val autoPlay = rememberPref({ ZtwoSettings.autoPlay.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.autoPlay, it) } }
        val unmute = rememberPref({ ZtwoSettings.youUnmute.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.youUnmute, it) } }
        var liveAlpha by remember { mutableIntStateOf(alpha.value) }
        var flash by remember { mutableStateOf(false) }
        LaunchedEffect(flash) { if (flash) { kotlinx.coroutines.delay(1800); flash = false } }
        var jump by remember { mutableStateOf<SettingsJump?>(null) }
        var resetting by remember { mutableStateOf(false) }
        val portrait = LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE
        val shown = pos.value != 0
        // the buttons on a video: one of them always stays on
        val onCount = listOf(pip.value, speed.value).count { it }
        fun canSwitch(on: Boolean) = shown && !(on && onCount <= 1)
        val off = if (shown) null else stringResource(R.string.media_btn_off)

        SettingsScaffold(
            title = stringResource(R.string.youtube_title),
            onBack = { finish() },
            onReset = { resetting = true },
            jumpTo = jump,
            above = if (!portrait) null else {
                {
                    ManagerPreview(pos.value, true, PrefEditor.withAlpha(color.value, liveAlpha), flash) {
                        flash = true
                        jump = SettingsJump(getString(R.string.youtube_manager))
                    }
                }
            },
        ) {
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.youtube_manager),
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
            settingsHeader(getString(R.string.hd_video_buttons))
            item { SettingSwitchRow(title = stringResource(R.string.pip_mode), summary = off, checked = pip.value, enabled = canSwitch(pip.value), onCheckedChange = pip::set) }
            item { SettingSwitchRow(title = stringResource(R.string.play_speed), summary = off, checked = speed.value, enabled = canSwitch(speed.value), onCheckedChange = speed::set) }
            settingsFooter(getString(R.string.keep_one_button))
            settingsHeader(getString(R.string.hd_playback))
            item { SettingSwitchRow(title = stringResource(R.string.youtube_auto), checked = autoPlay.value, onCheckedChange = autoPlay::set) }
            item { SettingSwitchRow(title = stringResource(R.string.youtube_unmute), checked = unmute.value, onCheckedChange = unmute::set) }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetYouTube(context)
                    pos.reload(ZoneSettings.youPos.value); color.reload(EditorSettings.ypipColor.value)
                    alpha.reload(EditorSettings.ypipAlpha.value); liveAlpha = alpha.value
                    pip.reload(ZoneSettings.youPip.value); speed.reload(ZoneSettings.youRate.value)
                    autoPlay.reload(ZtwoSettings.autoPlay.value); unmute.reload(ZtwoSettings.youUnmute.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }
}
