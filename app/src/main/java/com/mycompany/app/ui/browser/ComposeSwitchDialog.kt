package com.mycompany.app.ui.browser

import android.app.Activity
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import com.mycompany.app.ui.components.TickSwitch as Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R

/**
 * A bottom sheet with a list of labelled switches and an Apply button: the shape of many small settings dialogs (open
 * links in, reset on lock, restore tabs ...). [onApply] gets the switch states in the order of [rows]; it is only called
 * when something changed, then the sheet closes. Shows itself when built.
 */
open class ComposeSwitchDialog(
    activity: Activity?,
    @StringRes private val rows: IntArray,
    initial: BooleanArray,
    private val onApply: (BooleanArray) -> Unit,
) : ComposeBottomDialog(activity) {
    private val original = initial.copyOf()
    private val states = mutableStateListOf<Boolean>().also { list -> initial.forEach { list.add(it) } }

    init {
        setSheet {
            for ((index, label) in rows.withIndex()) {
                Row(
                    Modifier.fillMaxWidth().clickable { states[index] = !states[index] }.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(label), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(checked = states[index], onCheckedChange = { states[index] = it })
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = {
                    val now = BooleanArray(states.size) { states[it] }
                    if (!now.contentEquals(original)) onApply(now)
                    dismiss()
                }) { Text(stringResource(R.string.apply)) }
            }
        }
        show()
    }
}
