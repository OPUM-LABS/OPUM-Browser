package com.mycompany.app.dialog

import com.mycompany.app.ui.components.AppSlider
import android.app.Activity
import com.mycompany.app.pref.store.ReadSettings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * The reader's text size (50..500 %): the slider (and the -1 / +1 buttons) apply live through [DialogSeekListener];
 * Apply stores it (`ReadSettings.textSize.value`), Reset (after asking) goes back to 100 % and forgets the stored value, and closing the
 * dialog without Apply puts the page back to the stored size. Compose now (on [ComposeBottomDialog]); same constructor,
 * the dialog shows itself.
 */
class DialogSeekText(activity: Activity?, dialogSeekListener: DialogSeekListener?) : ComposeBottomDialog(activity) {
    private var listener: DialogSeekListener? = dialogSeekListener

    /** The size stored when the dialog opened / last applied; what closing without Apply goes back to. */
    private var stored = ReadSettings.textSize.value.coerceIn(MIN, MAX)
    private var value by mutableIntStateOf(stored)
    private var resetAsk by mutableStateOf(false)

    private fun change(newValue: Int) {
        val v = newValue.coerceIn(MIN, MAX)
        if (v == value) return
        value = v
        listener?.onValue(v)
    }

    private fun save(forget: Boolean) {
        if (ReadSettings.textSize.value != value) {
            ReadSettings.editLocal { set(ReadSettings.textSize, value) }
            if (forget) PrefSet.remove(context, 8, "mTextSize") else PrefSet.putInt(context, 8, value, "mTextSize")
        }
        stored = value
    }

    init {
        setSheet {
            Text(
                stringResource(R.string.text_size), style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp, start = 24.dp, end = 24.dp),
            )
            Text(
                "$value%", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { change(value - 1) }) { Icon(painterResource(R.drawable.outline_remove_black_24), contentDescription = null) }
                AppSlider(
                    value = value.toFloat(), onValueChange = { change(it.toInt()) },
                    valueRange = MIN.toFloat()..MAX.toFloat(), modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { change(value + 1) }) { Icon(painterResource(R.drawable.outline_add_black_24), contentDescription = null) }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { resetAsk = true }) { Text(stringResource(R.string.reset)) }
                TextButton(onClick = { save(false); dismiss() }) { Text(stringResource(R.string.apply)) }
            }
            if (resetAsk) {
                AlertDialog(
                    onDismissRequest = { resetAsk = false },
                    text = { Text(stringResource(R.string.reset_setting)) },
                    confirmButton = {
                        TextButton(colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = androidx.compose.material3.MaterialTheme.colorScheme.error), onClick = {
                            resetAsk = false
                            change(100)
                            save(true)
                        }) { Text(stringResource(R.string.reset)) }
                    },
                    dismissButton = { TextButton(onClick = { resetAsk = false }) { Text(stringResource(R.string.cancel)) } },
                )
            }
        }
        show()
    }

    override fun dismiss() {
        if (value != stored) {
            value = stored
            listener?.onValue(stored)
        }
        listener = null
        super.dismiss()
    }

    companion object {
        private const val MIN = 50
        private const val MAX = 500
    }
}
