package com.mycompany.app.ui.components

import android.content.Context
import com.mycompany.app.pref.store.PdfSettings
import android.view.Window
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
import androidx.compose.runtime.DisposableEffect
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
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefPdf

/**
 * Material 3 replacement of the old `DialogSeekBright` for the default screen brightness (the reader / text viewer
 * brightness in `PrefPdf`): use the system brightness or a user-defined percentage. The window follows the slider while
 * it moves, and goes back to the stored value if the dialog is closed without Apply. Reset goes back to the system
 * brightness (90 % when switched to user-defined) and forgets the stored values.
 */
@Composable
fun BrightnessDialog(context: Context, window: Window?, onDismiss: () -> Unit) {
    val storedUser = PdfSettings.userBrightness.value
    val storedValue = PdfSettings.brightness.value
    var user by remember { mutableStateOf(storedUser) }
    var value by remember { mutableIntStateOf(storedValue.coerceIn(0, 100)) }
    var applied by remember { mutableStateOf(false) }
    var resetAsk by remember { mutableStateOf(false) }

    // preview on the window, undone when the dialog goes away without Apply
    DisposableEffect(Unit) {
        onDispose { if (!applied) MainUtil.applyWindowBrightness(window, PdfSettings.brightness.value, PdfSettings.userBrightness.value) }
    }

    fun save(forget: Boolean) {
        if (PdfSettings.userBrightness.value != user || PdfSettings.brightness.value != value) {
            PdfSettings.editLocal { set(PdfSettings.userBrightness, user) }
            PdfSettings.editLocal { set(PdfSettings.brightness, value) }
            val prefs = PrefPdf.get(context, false) ?: return
            if (forget) { prefs.removeKey("mUserBright"); prefs.removeKey("mBright") } else { prefs.putBool("mUserBright", user); prefs.putInt(value, "mBright") }
            prefs.saveAsync()
        }
        applied = true
    }

    com.mycompany.app.ui.components.OpumSheetDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.brightness)) },
        text = {
            Column {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(if (user) R.string.user_defined else R.string.system_name), style = MaterialTheme.typography.bodyLarge)
                    Switch(checked = user, onCheckedChange = { user = it; MainUtil.applyWindowBrightness(window, value, it) })
                }
                Text(
                    stringResource(if (user) R.string.bright_info else R.string.screen_info_system),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text("$value%", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(enabled = user, onClick = { if (value > 0) { value--; MainUtil.applyWindowBrightness(window, value, user) } }) { Icon(painterResource(R.drawable.outline_remove_black_24), contentDescription = null) }
                    AppSlider(
                        enabled = user, value = value.toFloat(),
                        onValueChange = { value = it.toInt().coerceIn(0, 100); MainUtil.applyWindowBrightness(window, value, user) },
                        valueRange = 0f..100f, modifier = Modifier.weight(1f),
                    )
                    IconButton(enabled = user, onClick = { if (value < 100) { value++; MainUtil.applyWindowBrightness(window, value, user) } }) { Icon(painterResource(R.drawable.outline_add_black_24), contentDescription = null) }
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
                    user = false; value = 90
                    MainUtil.applyWindowBrightness(window, value, user)
                    save(true)
                    applied = false
                }) { Text(stringResource(R.string.reset)) }
            },
            dismissButton = { TextButton(onClick = { resetAsk = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}
