package com.mycompany.app.ui.settings

import android.content.Context
import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.ZtwoSettings
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSaveConfirm
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.dialog.DialogSeekSimple
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingSliderRow
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingsScaffold

/**
 * Swipe sensitivity of the five gesture zones (top, bottom, left, right, centre). The values are edited on a copy and
 * written when saved; leaving with unsaved changes asks to save or discard, like the old screen did. The value picker is
 * the legacy [DialogSeekSimple].
 */
class SenseSettingsActivity : OpumComposeActivity() {

    private fun stored() = listOf(ZtwoSettings.senseTop.value, ZtwoSettings.senseBot.value, ZtwoSettings.senseLeft.value, ZtwoSettings.senseRight.value, ZtwoSettings.senseCenter.value)

    @Composable
    override fun Content() {
        val context: Context = this
        val values = remember { mutableStateListOf<Int>().apply { addAll(stored()) } }
        var resetting by remember { mutableStateOf(false) }
        val changed = values.toList() != stored()

        fun write(save: Boolean) {
            ZtwoSettings.editLocal { set(ZtwoSettings.senseTop, values[0]) }; ZtwoSettings.editLocal { set(ZtwoSettings.senseBot, values[1]) }; ZtwoSettings.editLocal { set(ZtwoSettings.senseLeft, values[2]) }; ZtwoSettings.editLocal { set(ZtwoSettings.senseRight, values[3]) }; ZtwoSettings.editLocal { set(ZtwoSettings.senseCenter, values[4]) }
            val prefs = PrefZtwo.get(context, false)!!
            if (save) {
                prefs.putInt(ZtwoSettings.senseTop.value, "mSenseTop2"); prefs.putInt(ZtwoSettings.senseBot.value, "mSenseBot2")
                prefs.putInt(ZtwoSettings.senseLeft.value, "mSenseLeft2"); prefs.putInt(ZtwoSettings.senseRight.value, "mSenseRight2"); prefs.putInt(ZtwoSettings.senseCenter.value, "mSenseCenter2")
            } else {
                prefs.removeKey("mSenseTop2"); prefs.removeKey("mSenseBot2"); prefs.removeKey("mSenseLeft2"); prefs.removeKey("mSenseRight2"); prefs.removeKey("mSenseCenter2")
            }
            prefs.saveAsync()
        }

        fun leave() {
            if (!changed) { finish(); return }
            DialogSaveConfirm(this, object : DialogSeekListener {
                override fun onValue(newValue: Int) {
                    if (newValue == 0) { write(true) }
                    finish()
                }
            })
        }

        BackHandler { leave() }

        val zones = listOf(
            stringResource(R.string.move_up), stringResource(R.string.move_down),
            stringResource(R.string.move_left), stringResource(R.string.move_right), stringResource(R.string.show_center),
        )
        SettingsScaffold(
            title = stringResource(R.string.swipe_sense),
            onBack = { leave() },
            onReset = { resetting = true },
            actions = {
                if (changed) {
                    IconButton(onClick = { write(true); finish() }) {
                        Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.ok))
                    }
                }
            },
        ) {
            // one slider per swipe zone (they were number dialogs); saved with the check mark, as before
            for (index in 0..4) {
                item {
                    SettingSliderRow(
                        title = zones[index],
                        value = values[index],
                        range = 10..200,
                        label = { "$it%" },
                        onValueChange = { newValue ->
                            if (ReadSettings.guideSense.value) { ReadSettings.edit(context) { set(ReadSettings.guideSense, false) } }
                            if (values[index] != newValue) values[index] = newValue
                        },
                    )
                }
            }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    for (newValue in 0..4) values[newValue] = 100
                    write(false) // the default is "no stored value"
                },
                onDismiss = { resetting = false },
            )
        }
    }
}
