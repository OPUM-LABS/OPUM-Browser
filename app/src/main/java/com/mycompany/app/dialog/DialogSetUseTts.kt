package com.mycompany.app.dialog

import android.content.Intent
import com.mycompany.app.main.RequestCode
import com.mycompany.app.pref.store.TtsSettings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.mycompany.app.ui.components.TickSwitch as Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.ui.components.Sym

/**
 * Read-aloud quick settings: the on/off switch (`TtsSettings.ttsMode.value`, `mTtsMode`), a "Detailed settings" row (opens [DialogSetTts])
 * and a gear that opens the TTS settings screen. The listener is called when the dismissed sheet finds the mode changed.
 * Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogSetUseTts(mainActivity: MainActivity?, dialogApplyListener: DialogApplyListener?) : ComposeBottomDialog(mainActivity) {
    private var host: MainActivity? = mainActivity
    private var listener: DialogApplyListener? = dialogApplyListener
    /** The open "Detailed settings" sheet, if any (the activity forwards some events to it). */
    var ttsDialog: DialogSetTts? = null
    private var initial = TtsSettings.ttsMode.value
    private val on = mutableStateOf(TtsSettings.ttsMode.value)

    /** The TTS settings screen returned: show the mode as it is now. */
    fun refreshMode() { on.value = TtsSettings.ttsMode.value }

    private fun openDetail() {
        val activity = host ?: return
        if (ttsDialog != null) return
        val dialogSetTts = DialogSetTts(activity)
        ttsDialog = dialogSetTts
        dialogSetTts.setOnDismissListener { ttsDialog?.dismiss(); ttsDialog = null }
    }

    init {
        setSheet {
            Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                IconButton(onClick = {
                    val activity = host ?: return@IconButton
                    val intent = Intent(context, com.mycompany.app.ui.settings.TtsSettingsActivity::class.java as Class<*>)
                    intent.putExtra("EXTRA_POPUP", true)
                    activity.launchExternalForResult(intent, RequestCode.DIALOG_SETTINGS)
                }) { Icon(Sym.Settings, contentDescription = null) }
            }
            Row(
                Modifier.fillMaxWidth().clickable { on.value = !on.value; applyMode() }.padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.tts_on), style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(R.string.tts_info_1), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = on.value, onCheckedChange = { on.value = it; applyMode() })
            }
            Text(
                stringResource(R.string.detail_setting), style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth().clickable { openDetail() }.padding(horizontal = 24.dp, vertical = 16.dp),
            )
        }
        show()
    }

    private fun applyMode() {
        TtsSettings.edit(context) { set(TtsSettings.ttsMode, on.value) }
    }

    override fun dismiss() {
        if (initial != TtsSettings.ttsMode.value) {
            initial = TtsSettings.ttsMode.value
            listener?.onApply()
        }
        ttsDialog?.dismiss()
        ttsDialog = null
        host = null
        listener = null
        super.dismiss()
    }
}
