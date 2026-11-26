package com.mycompany.app.ui.settings

import com.mycompany.app.ui.components.AppSlider
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSaveConfirm
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.pref.store.VideoSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.Sym

/**
 * Double-tap areas of the video player: what the left, right and centre tap area does (seek back / forward 10 seconds,
 * pause, nothing) and how wide the left and right areas are in portrait and in landscape. Edited on a copy; leaving with unsaved changes asks to save or discard like the old screen did. Material 3
 * sliders replace dragging the area edges. The widths are bounded as before: at least one button size, and the centre
 * keeps at least two button sizes.
 */
class VideoTapSettingsActivity : OpumComposeActivity() {

    private fun stored() = VideoSettings.tapAreas.map { it.value }

    @Composable
    override fun Content() {
        val context: Context = this
        val values = remember { mutableStateListOf<Int>().apply { addAll(stored()) } } // A B C actions | D E portrait | F G landscape
        var choosing by remember { mutableIntStateOf(-1) } // 0 left, 1 right action
        var resetting by remember { mutableStateOf(false) }
        val changed = values.toList() != stored()
        val metrics = resources.displayMetrics
        val portraitWidth = minOf(metrics.widthPixels, metrics.heightPixels)
        val landscapeWidth = maxOf(metrics.widthPixels, metrics.heightPixels)
        val minArea = MainApp.dp40
        val minCenter = MainApp.dp64 shl 1

        fun save() {
            VideoSettings.edit(context) { VideoSettings.tapAreas.forEachIndexed { newValue, setting -> set(setting, values[newValue]) } }
        }

        fun leave() {
            if (!changed) { finish(); return }
            DialogSaveConfirm(this, object : DialogSeekListener {
                override fun onValue(newValue: Int) {
                    if (newValue == 0) save()
                    finish()
                }
            })
        }

        BackHandler { leave() }

        @Composable
        fun widthSlider(label: String, index: Int, other: Int, total: Int) {
            val max = (total - values[other] - minCenter).coerceAtLeast(minArea)
            Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Text(
                    text = label + ": " + (values[index] / metrics.density).toInt() + " dp",
                    style = MaterialTheme.typography.bodyLarge,
                )
                AppSlider(
                    value = values[index].toFloat().coerceIn(minArea.toFloat(), max.toFloat()),
                    onValueChange = { values[index] = it.toInt().coerceIn(minArea, max) },
                    valueRange = minArea.toFloat()..max.toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        val actionLabels = listOf(
            "- 10" + getString(R.string.time_s), "+ 10" + getString(R.string.time_s),
            getString(R.string.pause), getString(R.string.not_used),
        )
        val sideTitles = listOf(getString(R.string.show_left), getString(R.string.show_right), getString(R.string.show_center))
        SettingsScaffold(
            title = stringResource(R.string.double_tap),
            onBack = { leave() },
            onReset = { resetting = true },
            actions = {
                if (changed) {
                    IconButton(onClick = { save(); finish() }) {
                        Icon(Sym.Check, contentDescription = stringResource(R.string.ok))
                    }
                }
            },
        ) {
            for (side in 0..2) {
                item {
                    SettingChoiceRow(
                        title = sideTitles[side],
                        value = actionLabels[values[side].coerceIn(0, 3)],
                        onClick = { choosing = side },
                    )
                }
            }
            settingsDivider()
            settingsHeader(getString(R.string.view_port))
            item { widthSlider(getString(R.string.show_left), 3, 4, portraitWidth) }
            item { widthSlider(getString(R.string.show_right), 4, 3, portraitWidth) }
            settingsDivider()
            settingsHeader(getString(R.string.view_land))
            item { widthSlider(getString(R.string.show_left), 5, 6, landscapeWidth) }
            item { widthSlider(getString(R.string.show_right), 6, 5, landscapeWidth) }
        }

        if (choosing >= 0) {
            val side = choosing
            ChoiceDialog(
                title = sideTitles[side],
                options = actionLabels,
                selectedIndex = values[side].coerceIn(0, 3),
                onSelect = { values[side] = it },
                onDismiss = { choosing = -1 },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    // defaults are "no stored value"
                    VideoSettings.reset(context, VideoSettings.tapAreas)
                    stored().forEachIndexed { newValue, v -> values[newValue] = v }
                },
                onDismiss = { resetting = false },
            )
        }
    }
}
