package com.mycompany.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R

/**
 * Pick one of [options] (2026-11-20: a sheet with radio rows, was a centred dialog). Picking an entry calls [onSelect]
 * and slides the sheet down.
 */
@Composable
fun ChoiceDialog(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    /** An optional second, smaller line per option (null = none), e.g. a language's name in the app's language. */
    details: List<String?>? = null,
) {
    OpumSheet(onDismiss = onDismiss, title = title) {
        options.forEachIndexed { index, label ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onSelect(index); close() }.padding(horizontal = 12.dp, vertical = 2.dp),
            ) {
                RadioButton(selected = index == selectedIndex, onClick = { onSelect(index); close() })
                val detail = details?.getOrNull(index)
                if (detail == null) Text(label, modifier = Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyLarge)
                else androidx.compose.foundation.layout.Column(Modifier.padding(start = 8.dp, top = 6.dp, bottom = 6.dp)) {
                    Text(label, style = MaterialTheme.typography.bodyLarge)
                    Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        androidx.compose.foundation.layout.Spacer(Modifier.padding(bottom = 12.dp))
    }
}

/**
 * A short destructive question (reset, delete, leave without saving): stays a centred card, as the plan keeps them; the
 * confirm button is red (2026-11-20).
 */
@Composable
fun ConfirmDialog(message: String, onConfirm: () -> Unit, onDismiss: () -> Unit, title: String? = null) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = title?.let { { Text(it) } },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(); onDismiss() },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

/** A short information text with OK (2026-11-20: a sheet, was a centred dialog). */
@Composable
fun InfoDialog(title: String, message: String, onDismiss: () -> Unit) {
    OpumSheet(
        onDismiss = onDismiss, title = title,
        actions = { TextButton(onClick = { close() }) { Text(stringResource(R.string.ok)) } },
    ) {
        Text(
            message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        )
    }
}

/**
 * Picks one or two column counts (e.g. portrait and landscape). [labels] has one entry per value in [values]; Apply
 * calls [onApply] with the chosen numbers in the same order. A sheet since 2026-11-20.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColumnCountDialog(
    title: String,
    labels: List<String>,
    values: List<Int>,
    options: List<Int>,
    onApply: (List<Int>) -> Unit,
    onDismiss: () -> Unit,
) {
    val chosen = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateListOf(*values.toTypedArray()) }
    OpumSheet(
        onDismiss = onDismiss, title = title,
        actions = {
            TextButton(onClick = { close() }) { Text(stringResource(R.string.cancel)) }
            TextButton(onClick = { onApply(chosen.toList()); close() }) { Text(stringResource(R.string.apply)) }
        },
    ) {
        androidx.compose.foundation.layout.Column(Modifier.padding(horizontal = 24.dp)) {
            labels.forEachIndexed { index, label ->
                Text(label, modifier = Modifier.padding(top = 8.dp), style = MaterialTheme.typography.labelLarge)
                androidx.compose.foundation.layout.FlowRow {
                    options.forEach { option ->
                        androidx.compose.material3.FilterChip(
                            selected = chosen[index] == option,
                            onClick = { chosen[index] = option },
                            label = { Text(option.toString()) },
                            modifier = Modifier.padding(end = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
