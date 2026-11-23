package com.mycompany.app.ui.browser

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import com.mycompany.app.ui.components.BubbleMenu
import com.mycompany.app.ui.components.BubbleMenuItem
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R

/**
 * Rows shared by the small settings sheets that used the old `SettingListAdapter` (title + switch, title + current
 * value with a pick menu, a plain note, the Apply / Cancel buttons). Disabled rows are dimmed and ignore taps, like
 * the old greyed-out items.
 */

/** Title (and optional summary) with a switch; the whole row toggles. */
@Composable
fun SheetSwitchRow(title: String, checked: Boolean, onToggle: (Boolean) -> Unit, enabled: Boolean = true, summary: String? = null) {
    Row(
        Modifier.fillMaxWidth().clickable(enabled = enabled) { onToggle(!checked) }
            .padding(horizontal = 24.dp, vertical = 12.dp).alpha(if (enabled) 1f else 0.38f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (!summary.isNullOrEmpty()) {
                Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked = checked, onCheckedChange = { onToggle(it) }, enabled = enabled)
    }
}

/**
 * Title with the current value below it; a tap opens a menu of [options] (string resources), the current one
 * ([selected], an index into [options]) checked. [onPick] gets the index.
 */
@Composable
fun SheetChoiceRow(title: String, options: List<Int>, selected: Int, onPick: (Int) -> Unit, enabled: Boolean = true, value: String? = null) {
    var open by remember { mutableStateOf(false) }
    // 2026-11-20: a ▾ shows that the row opens a choice (the history retention sheet looked like it had no control),
    // and the choice opens in OPUM's bubble menu next to the value
    Row(
        Modifier.fillMaxWidth().clickable(enabled = enabled) { open = true }
            .padding(start = 24.dp, end = 16.dp, top = 12.dp, bottom = 12.dp).alpha(if (enabled) 1f else 0.38f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            val shown = value ?: options.getOrNull(selected)?.let { stringResource(it) } ?: ""
            Text(shown, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
        Box {
            Icon(Icons.Filled.ArrowDropDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            BubbleMenu(expanded = open, onDismiss = { open = false }) {
                options.forEachIndexed { option, label ->
                    BubbleMenuItem(stringResource(label), { open = false; onPick(option) }, checked = option == selected)
                }
            }
        }
    }
}

/** A title + value row that opens something else when tapped. */
@Composable
fun SheetLinkRow(title: String, value: String?, onClick: () -> Unit, enabled: Boolean = true) {
    // a › shows that the row opens something (2026-11-20; it looked like plain text)
    Row(
        Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick)
            .padding(start = 24.dp, end = 16.dp, top = 12.dp, bottom = 12.dp).alpha(if (enabled) 1f else 0.38f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (!value.isNullOrEmpty()) {
                Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
        Icon(painterResource(R.drawable.outline_chevron_right_black_24), null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** A short note (the old grey info rows). */
@Composable
fun SheetNote(note: String) {
    Text(
        note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
    )
}

/** The bottom button row: optional Cancel / extra, then the main button (Apply by default; red when [destructive]). */
@Composable
fun SheetButtons(onMain: () -> Unit, mainLabel: Int = R.string.apply, onCancel: (() -> Unit)? = null, mainEnabled: Boolean = true, destructive: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
        if (onCancel != null) TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
        TextButton(
            onClick = onMain, enabled = mainEnabled,
            colors = if (destructive) ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error) else ButtonDefaults.textButtonColors(),
        ) { Text(stringResource(mainLabel)) }
    }
}

/**
 * The small bar every sheet starts with (2026-11-20, drawn by `ComposeBottomDialog`): shows that the sheet can be
 * dragged down to close; [modifier] carries the drag.
 */
@Composable
fun SheetHandle(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(top = 10.dp, bottom = 6.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(width = 32.dp, height = 4.dp)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(2.dp)),
        )
    }
}

/** Title of a sheet or card, with an optional [subtitle] (count, file name, address) below it. */
@Composable
fun SheetTitle(title: String, subtitle: String? = null) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 4.dp, bottom = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        if (!subtitle.isNullOrEmpty()) {
            Text(
                subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}
