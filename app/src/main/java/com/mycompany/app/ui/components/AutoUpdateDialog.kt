package com.mycompany.app.ui.components

import android.content.Context
import com.mycompany.app.pref.store.PdfSettings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.PrefPdf

/**
 * Material 3 replacement of the old `DialogSeekFilter`: whether the ad-filter lists (or the user scripts) update on
 * their own and how many days apart (1..100). Stored in `PrefPdf` (`J/K` filters, `M/N` scripts): the time is when it
 * was switched on (0 = off), the day count the cycle. Reset goes back to the defaults (filters on / scripts off, 7 days)
 * and forgets the stored values.
 */
@Composable
fun AutoUpdateDialog(context: Context, scripts: Boolean, onDismiss: () -> Unit) {
    val storedTime = if (scripts) PdfSettings.scriptTime.value else PdfSettings.filterTime.value
    val storedDays = if (scripts) PdfSettings.scriptDay.value else PdfSettings.filterDay.value
    var enabled by remember { mutableStateOf(storedTime > 0) }
    var days by remember { mutableIntStateOf(storedDays.coerceIn(1, 100)) }
    var resetAsk by remember { mutableStateOf(false) }

    fun save(forget: Boolean) {
        val wasOn = storedTime > 0
        if (wasOn == enabled && days == storedDays && !forget) return
        val now = if (enabled) System.currentTimeMillis() else 0L
        val prefs = PrefPdf.get(context, false) ?: return
        if (scripts) {
            PdfSettings.editLocal { set(PdfSettings.scriptTime, now) }; PdfSettings.editLocal { set(PdfSettings.scriptDay, days) }
            if (forget) { prefs.removeKey("mScriptTime"); prefs.removeKey("mScriptDay") } else { prefs.putLong(now, "mScriptTime"); prefs.putInt(days, "mScriptDay") }
        } else {
            PdfSettings.editLocal { set(PdfSettings.filterTime, now) }; PdfSettings.editLocal { set(PdfSettings.filterDay, days) }
            if (forget) { prefs.removeKey("mFilterTime"); prefs.removeKey("mFilterDay") } else { prefs.putLong(now, "mFilterTime"); prefs.putInt(days, "mFilterDay") }
        }
        prefs.saveAsync()
    }

    com.mycompany.app.ui.components.OpumSheetDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.auto_update)) },
        text = {
            Column {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.auto_update), style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = enabled, onCheckedChange = { enabled = it })
                }
                Text(
                    stringResource(R.string.update_time) + ": " + days + " " + stringResource(R.string.time_day),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 16.dp),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(enabled = enabled, onClick = { if (days > 1) days-- }) { Icon(painterResource(R.drawable.outline_remove_black_24), contentDescription = null) }
                    AppSlider(
                        enabled = enabled,
                        value = days.toFloat(), onValueChange = { days = it.toInt().coerceIn(1, 100) },
                        valueRange = 1f..100f, modifier = Modifier.weight(1f),
                    )
                    IconButton(enabled = enabled, onClick = { if (days < 100) days++ }) { Icon(painterResource(R.drawable.outline_add_black_24), contentDescription = null) }
                }
            }
        },
        confirmButton = { TextButton(onClick = { save(false); onDismiss() }) { Text(stringResource(R.string.apply)) } },
        dismissButton = { TextButton(onClick = { resetAsk = true }) { Text(stringResource(R.string.reset)) } },
    )

    if (resetAsk) {
        AlertDialog(
            onDismissRequest = { resetAsk = false },
            text = { Text(stringResource(R.string.reset_setting)) },
            confirmButton = {
                TextButton(colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = androidx.compose.material3.MaterialTheme.colorScheme.error), onClick = {
                    resetAsk = false
                    enabled = !scripts
                    days = 7
                    save(true)
                }) { Text(stringResource(R.string.reset)) }
            },
            dismissButton = { TextButton(onClick = { resetAsk = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}
