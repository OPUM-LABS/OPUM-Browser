package com.mycompany.app.ui.settings

import com.mycompany.app.ui.components.AppSlider
import android.content.Context
import com.mycompany.app.pref.store.TtsSettings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSaveConfirm
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.Sym

/**
 * Landscape mode: the left and right margin of web pages when the phone is held sideways. A margin is shown as a
 * percentage of the largest useful margin (the space that makes the page as narrow as in portrait). Both margins
 * together stay within that space. Edited on a copy; leaving with unsaved changes asks to save or discard. Sliders and a
 * preview replace dragging the margins over a live page.
 */
class LandSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        var left by remember { mutableIntStateOf(TtsSettings.webLand.value) }
        var right by remember { mutableIntStateOf(TtsSettings.webLand2.value) }
        var resetting by remember { mutableStateOf(false) }
        val changed = left != TtsSettings.webLand.value || right != TtsSettings.webLand2.value
        val metrics = resources.displayMetrics
        val longSide = maxOf(metrics.widthPixels, metrics.heightPixels)
        val shortSide = minOf(metrics.widthPixels, metrics.heightPixels)
        val space = (longSide - shortSide).coerceAtLeast(1)  // both margins together must fit in here
        val half = space / 2f                                // 100 % of one margin

        fun write(save: Boolean) {
            TtsSettings.editLocal { set(TtsSettings.webLand, left) }
            TtsSettings.editLocal { set(TtsSettings.webLand2, right) }
            val prefs = PrefTts.get(context, false)!!
            if (save) { prefs.putInt(TtsSettings.webLand.value, "mWebLand1"); prefs.putInt(TtsSettings.webLand2.value, "mWebLand2") }
            else { prefs.removeKey("mWebLand1"); prefs.removeKey("mWebLand2") }
            prefs.saveAsync()
        }

        fun leave() {
            if (!changed) { finish(); return }
            DialogSaveConfirm(this, object : DialogSeekListener {
                override fun onValue(newValue: Int) {
                    if (newValue == 0) write(true)
                    finish()
                }
            })
        }

        BackHandler { leave() }

        fun percent(px: Int) = Math.round(px / half * 100f)

        SettingsScaffold(
            title = stringResource(R.string.land_mode),
            onBack = { leave() },
            onReset = { resetting = true },
            actions = {
                if (changed) {
                    IconButton(onClick = { write(true); finish() }) {
                        Icon(Sym.Check, contentDescription = stringResource(R.string.ok))
                    }
                }
            },
        ) {
            item {
                // landscape screen preview: the page sits between the two margins
                Row(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(120.dp)
                        .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                ) {
                    val total = longSide.toFloat()
                    Box(Modifier.weight((left / total).coerceAtLeast(0.001f)).fillMaxHeight())
                    Box(
                        Modifier
                            .weight(((total - left - right) / total).coerceAtLeast(0.001f))
                            .fillMaxHeight()
                            .padding(4.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center,
                    ) {}
                    Box(Modifier.weight((right / total).coerceAtLeast(0.001f)).fillMaxHeight())
                }
            }
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    // the standard slider row look: title on the left, value on the right (2026-11-20)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.show_left), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(percent(left).toString() + "%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    AppSlider(
                        value = left.toFloat(),
                        onValueChange = { left = it.toInt().coerceIn(0, (space - right).coerceAtLeast(0)) },
                        valueRange = 0f..space.toFloat(),
                    )
                }
            }
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    // the standard slider row look: title on the left, value on the right (2026-11-20)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.show_right), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(percent(right).toString() + "%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    }
                    AppSlider(
                        value = right.toFloat(),
                        onValueChange = { right = it.toInt().coerceIn(0, (space - left).coerceAtLeast(0)) },
                        valueRange = 0f..space.toFloat(),
                    )
                }
            }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = { left = 0; right = 0; write(false) },
                onDismiss = { resetting = false },
            )
        }
    }
}
