package com.mycompany.app.ui.settings

import com.mycompany.app.ui.components.AppSlider
import android.content.Context
import com.mycompany.app.pref.store.TtsSettings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.Sym

/**
 * Size of the one-hand area: where its top edge sits, as a percentage of the screen height (50 to 90). The value is
 * edited on a copy; leaving with unsaved changes asks to save or discard like the old screen. A Material 3 slider
 * replaces the old drag handle, with a phone-shaped preview of the area.
 */
class HandAreaSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        if (TtsSettings.handTop.value > 90) TtsSettings.editLocal { set(TtsSettings.handTop, 90) }
        if (TtsSettings.handTop.value < 50) TtsSettings.editLocal { set(TtsSettings.handTop, 50) }
        var value by remember { mutableIntStateOf(TtsSettings.handTop.value) }
        var resetting by remember { mutableStateOf(false) }
        val changed = value != TtsSettings.handTop.value

        fun save() {
            TtsSettings.edit(context) { set(TtsSettings.handTop, value) }
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

        SettingsScaffold(
            title = stringResource(R.string.onehand_area),
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
            item {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // phone preview: the highlighted part is the one-hand area, below the chosen top edge
                    Box(
                        Modifier
                            .width(140.dp)
                            .height(280.dp)
                            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight((100 - value) / 100f)
                                .padding(3.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp))
                        )
                    }
                    Text(
                        text = "$value%",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    AppSlider(
                        value = value.toFloat(),
                        onValueChange = { value = it.toInt().coerceIn(50, 90) },
                        valueRange = 50f..90f,
                        steps = 39,
                    )
                }
            }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    value = 60
                    TtsSettings.editLocal { set(TtsSettings.handTop, 60) }
                    PrefSet.remove(context, 12, "mHandTop2") // default: no stored value
                },
                onDismiss = { resetting = false },
            )
        }
    }
}
