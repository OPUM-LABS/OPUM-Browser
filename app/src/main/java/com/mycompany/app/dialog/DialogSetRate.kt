package com.mycompany.app.dialog

import androidx.compose.foundation.clickable
import com.mycompany.app.pref.store.ZtwoSettings
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.web.WebVideoFull
import com.mycompany.app.web.WebViewActivity

/**
 * Playback speed for the web video player: a "remember speed" switch (`ZtwoSettings.youSpeedSave.value`, `mYouSpdSave`) and the list of speeds
 * (`WebVideoFull.SPEEDS`, index 3 = normal). Picking a speed stores it (`ZtwoSettings.youSpeedRate.value`), calls the listener when it changed and
 * closes. [selectCurrent] re-reads the stored speed when it was changed elsewhere (the player's own buttons). Compose now (on
 * [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogSetRate(webViewActivity: WebViewActivity?, current: Int, dialogApplyListener: DialogApplyListener?) : ComposeBottomDialog(webViewActivity) {
    private var listener: DialogApplyListener? = dialogApplyListener
    private val selected = mutableIntStateOf(current)
    private val remember = mutableStateOf(ZtwoSettings.youSpeedSave.value)

    /** The speed in use changed outside this sheet: move the selection to it. */
    fun selectCurrent() {
        val speeds = WebVideoFull.Companion.SPEEDS
        var found = -1
        for (index in 0 until 8) if (ZtwoSettings.youSpeedRate.value.compareTo(speeds[index]) == 0) { found = index; break }
        selected.intValue = found
    }

    private fun toggleRemember() {
        val value = !ZtwoSettings.youSpeedSave.value
        ZtwoSettings.edit(context) { set(ZtwoSettings.youSpeedSave, value) }
        remember.value = value
    }

    private fun pick(index: Int) {
        val speed = WebVideoFull.Companion.SPEEDS[index]
        if (ZtwoSettings.youSpeedRate.value.compareTo(speed) != 0) {
            ZtwoSettings.editLocal { set(ZtwoSettings.youSpeedRate, speed) }
            PrefSet.putSpeedRate(context, speed)
            listener?.onApply()
        }
        dismiss()
    }

    init {
        setSheet {
            Row(
                Modifier.fillMaxWidth().clickable { toggleRemember() }.padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.youtube_speed), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(checked = remember.value, onCheckedChange = { toggleRemember() })
            }
            androidx.compose.foundation.layout.Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                for (index in 0 until 8) {
                    val speed = WebVideoFull.Companion.SPEEDS[index]
                    Row(Modifier.fillMaxWidth().clickable { pick(index) }.padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selected.intValue == index, onClick = { pick(index) })
                        Text(
                            if (index == 3) stringResource(R.string.normal_rate) else speed.toString(),
                            style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
        show()
    }

    override fun dismiss() {
        listener = null
        super.dismiss()
    }

    companion object {
    }
}
