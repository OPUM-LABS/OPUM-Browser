package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSliderRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Settings › Appearance › Text and font (2026-11-25, was a dialog on Appearance): the text size of web pages with a
 * sample line, the floating "zoom in" button that switches to a larger size (its size, colour and transparency; it is
 * dragged to its place), and the font ([FontSettingsActivity]).
 */
class TextSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val textSize = rememberPref({ ZoneSettings.textSize.value.takeIf { it in 50..500 } ?: 100 }) { ZoneSettings.edit(context) { set(ZoneSettings.textSize, it) } }
        val zoomOn = rememberPref({ ZtriSettings.zoomIcon.value }) { ZtriSettings.edit(context) { set(ZtriSettings.zoomIcon, it) } }
        val zoomSize = rememberPref({ ZtriSettings.zoomSize.value.takeIf { it in 50..500 } ?: 200 }) { ZtriSettings.edit(context) { set(ZtriSettings.zoomSize, it) } }
        val zoomColor = rememberPref({ EditorSettings.zoomColor.value }) { c ->
            val at = MainConst.gradientColors.indexOf(c)
            EditorSettings.edit(context) {
                set(EditorSettings.zoomColor, c)
                if (at >= 0) set(EditorSettings.zoomPos, MainConst.presetPositions[at])
            }
            PrefEditor.zoomArgb = PrefEditor.withAlpha(c, EditorSettings.zoomAlpha.value)
        }
        val zoomAlpha = rememberPref({ EditorSettings.zoomAlpha.value.coerceIn(0, 90) }) {
            EditorSettings.edit(context) { set(EditorSettings.zoomAlpha, it) }
            PrefEditor.zoomArgb = PrefEditor.withAlpha(EditorSettings.zoomColor.value, it)
        }
        var tick by remember { mutableIntStateOf(0) }
        OnScreenResume { tick++ }
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.appear_text_page),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            item {
                SettingSliderRow(
                    title = stringResource(R.string.text_size), value = textSize.value, range = 50..500, label = { "$it%" },
                    onValueChange = textSize::set,
                    below = { v ->
                        Text("ABCDEabcde 1234567890", fontSize = (16 * v / 100f).sp, modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
                    },
                )
            }
            settingsHeader(getString(R.string.zoom_icon))
            item { SettingSwitchRow(title = stringResource(R.string.zoom_icon), checked = zoomOn.value, onCheckedChange = zoomOn::set) }
            item {
                SettingSliderRow(
                    title = stringResource(R.string.zoom_size), value = zoomSize.value, range = 50..500, label = { "$it%" },
                    onValueChange = zoomSize::set, enabled = zoomOn.value,
                )
            }
            item {
                ButtonColorCircles(
                    title = stringResource(R.string.icon_color), enabled = zoomOn.value, reason = stringResource(R.string.btn_off_reason),
                    selected = zoomColor.value, transparency = zoomAlpha.value, onPick = zoomColor::set,
                )
            }
            item {
                SettingSliderRow(
                    title = stringResource(R.string.color_alpha), value = zoomAlpha.value, range = 0..90, label = { "$it%" },
                    onValueChange = zoomAlpha::set, enabled = zoomOn.value,
                )
            }
            settingsFooter(getString(R.string.drag_move_guide))
            settingsHeader(getString(R.string.font))
            item {
                SettingRow(
                    title = stringResource(R.string.font),
                    summary = "$tick".let { fontName(context) },
                    onClick = { context.startActivity(Intent(context, FontSettingsActivity::class.java)) },
                )
            }
            settingsFooter(getString(R.string.font_info_1))
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetTextFont(context)
                    textSize.reload(ZoneSettings.textSize.value); zoomOn.reload(ZtriSettings.zoomIcon.value); zoomSize.reload(ZtriSettings.zoomSize.value)
                    zoomColor.reload(EditorSettings.zoomColor.value); zoomAlpha.reload(EditorSettings.zoomAlpha.value)
                    tick++
                },
                onDismiss = { resetting = false },
            )
        }
    }
}
