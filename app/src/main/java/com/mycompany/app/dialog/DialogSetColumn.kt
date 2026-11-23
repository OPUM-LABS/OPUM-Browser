package com.mycompany.app.dialog

import androidx.compose.foundation.clickable
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.MainSettings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.mycompany.app.main.MainActivity
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Column counts. [menuParam] true = the menu's column count (`MainSettings.menuColumns.value`, `mMenuPort`); false = the quick-access grid, portrait and
 * landscape (`ZtriSettings.quickPort` / `quickLand`, keys `mQuickPort` / `mQuickLand`). Each row opens a menu of the allowed counts; Apply stores the
 * changes and closes. Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogSetColumn(mainActivity: android.app.Activity?, menuParam: Boolean) : ComposeBottomDialog(mainActivity) {
    private val menu: Boolean = menuParam
    private val first = mutableIntStateOf(if (menuParam) MainSettings.menuColumns.value else ZtriSettings.quickPort.value)
    private val second = mutableIntStateOf(ZtriSettings.quickLand.value)

    private fun apply() {
        if (menu) {
            if (MainSettings.menuColumns.value != first.intValue) {
                MainSettings.editLocal { set(MainSettings.menuColumns, first.intValue) }
                val prefs = PrefMain.get(context, false)
                prefs!!.putInt(MainSettings.menuColumns.value, "mMenuPort")
                prefs.saveAsync()
            }
        } else if (ZtriSettings.quickPort.value != first.intValue || ZtriSettings.quickLand.value != second.intValue) {
            ZtriSettings.editLocal { set(ZtriSettings.quickPort, first.intValue) }
            ZtriSettings.editLocal { set(ZtriSettings.quickLand, second.intValue) }
            val prefs = PrefZtri.get(context)
            prefs!!.putInt(ZtriSettings.quickPort.value, "mQuickPort")
            prefs.putInt(ZtriSettings.quickLand.value, "mQuickLand")
            prefs.saveAsync()
        }
        dismiss()
    }

    init {
        setSheet {
            Column {
                ColumnRow(stringResource(if (menu) R.string.column_count else R.string.view_port), first.intValue, portCounts) { first.intValue = it }
                if (!menu) ColumnRow(stringResource(R.string.view_land), second.intValue, landCounts) { second.intValue = it }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { apply() }) { Text(stringResource(R.string.apply)) }
            }
        }
        show()
    }

    companion object {
        @JvmField
        val portCounts: IntArray = intArrayOf(2, 3, 4, 5, 6, 7)
        @JvmField
        val landCounts: IntArray = intArrayOf(2, 3, 4, 5, 6, 7, 8, 9, 10)
    }
}

/** A labelled row showing [value]; a tap opens a menu of [options]. */
@androidx.compose.runtime.Composable
internal fun ColumnRow(label: String, value: Int, options: IntArray, onPick: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Column(Modifier.fillMaxWidth().clickable { open = true }.padding(horizontal = 24.dp, vertical = 12.dp)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(value.toString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        com.mycompany.app.ui.components.BubbleMenu(expanded = open, onDismiss = { open = false }) {
            for (option in options) DropdownMenuItem(text = { Text(option.toString()) }, onClick = { onPick(option); open = false })
        }
    }
}
