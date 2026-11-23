package com.mycompany.app.ui.settings

import com.mycompany.app.ui.components.AppSlider
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
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
import com.mycompany.app.pref.store.ImageSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.components.settingsHeader

/**
 * Tap areas of the image viewer: what the left / right tap area does and how wide the two areas are in portrait and in
 * landscape. Edited on a copy; leaving with unsaved changes asks to save or discard like the old screen did. Material 3
 * sliders replace dragging the area edges. The widths are bounded as before: at least one button size, and the centre
 * keeps at least two button sizes.
 */
class ImageTapSettingsActivity : OpumComposeActivity() {

    private fun stored() = ImageSettings.tapAreas.map { it.value }

    @Composable
    override fun Content() {
        val context: Context = this
        val values = remember { mutableStateListOf<Int>().apply { addAll(stored()) } } // D E | F G port | H I land
        var choosing by remember { mutableIntStateOf(-1) } // 0 left, 1 right action
        var resetting by remember { mutableStateOf(false) }
        val changed = values.toList() != stored()
        val metrics = resources.displayMetrics
        val portraitWidth = minOf(metrics.widthPixels, metrics.heightPixels)
        val landscapeWidth = maxOf(metrics.widthPixels, metrics.heightPixels)
        val minArea = MainApp.dp40
        val minCenter = MainApp.dp64 shl 1

        fun save() {
            ImageSettings.edit(context) { ImageSettings.tapAreas.forEachIndexed { newValue, setting -> set(setting, values[newValue]) } }
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

        val actionLabel = { storedValue: Int -> if (storedValue == 0) getString(R.string.page_move) else getString(R.string.not_used) }
        SettingsScaffold(
            title = stringResource(R.string.tap_turn),
            onBack = { leave() },
            onReset = { resetting = true },
            actions = {
                if (changed) {
                    IconButton(onClick = { save(); finish() }) {
                        Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.ok))
                    }
                }
            },
        ) {
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.show_left),
                    value = actionLabel(values[0]),
                    onClick = { choosing = 0 },
                )
            }
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.show_right),
                    value = actionLabel(values[1]),
                    onClick = { choosing = 1 },
                )
            }
            settingsDivider()
            settingsHeader(getString(R.string.view_port))
            item { widthSlider(getString(R.string.show_left), 2, 3, portraitWidth) }
            item { widthSlider(getString(R.string.show_right), 3, 2, portraitWidth) }
            settingsDivider()
            settingsHeader(getString(R.string.view_land))
            item { widthSlider(getString(R.string.show_left), 4, 5, landscapeWidth) }
            item { widthSlider(getString(R.string.show_right), 5, 4, landscapeWidth) }
        }

        if (choosing >= 0) {
            val side = choosing
            ChoiceDialog(
                title = stringResource(if (side == 0) R.string.show_left else R.string.show_right),
                options = listOf(stringResource(R.string.page_move), stringResource(R.string.not_used)),
                selectedIndex = values[side],
                onSelect = { values[side] = it },
                onDismiss = { choosing = -1 },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    // defaults are "no stored value"
                    ImageSettings.reset(context, ImageSettings.tapAreas)
                    stored().forEachIndexed { newValue, storedValue -> values[newValue] = storedValue }
                },
                onDismiss = { resetting = false },
            )
        }
    }
}
