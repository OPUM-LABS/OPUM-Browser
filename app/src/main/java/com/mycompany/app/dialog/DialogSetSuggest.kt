package com.mycompany.app.dialog

import androidx.compose.foundation.clickable
import com.mycompany.app.pref.store.BrowserSettings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Address-bar suggestions: the suggestion engine (`BrowserSettings.sugEngine.value`, `mSugEng`: 0 not used, 1 DuckDuckGo, 2 Google) and which local
 * sources are shown (`BrowserSettings.sugType.value`, `mSugType3`: bits 2 recent searches, 4 history, 8 bookmarks, 16 quick access). Apply stores
 * the changes and closes. Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogSetSuggest(mainActivity: android.app.Activity?) : ComposeBottomDialog(mainActivity) {
    private val engine = mutableIntStateOf(0)
    private val sources = mutableIntStateOf(BrowserSettings.sugType.value)

    private fun apply() {
        if (BrowserSettings.sugEngine.value != engine.intValue || BrowserSettings.sugType.value != sources.intValue) {
            BrowserSettings.editLocal { set(BrowserSettings.sugEngine, engine.intValue) }
            BrowserSettings.editLocal { set(BrowserSettings.sugType, sources.intValue) }
            val prefs = PrefWeb.get(context, false)
            prefs!!.putInt(BrowserSettings.sugEngine.value, "mSugEng")
            prefs.putInt(BrowserSettings.sugType.value, "mSugType3")
            prefs.saveAsync()
        }
        dismiss()
    }

    init {
        if (BrowserSettings.sugEngine.value < 0) {
            BrowserSettings.editLocal { set(BrowserSettings.sugEngine, if (MainUtil.isChinaLocale()) 1 else 2) }
            PrefSet.putInt(context, 14, BrowserSettings.sugEngine.value, "mSugEng")
        }
        engine.intValue = BrowserSettings.sugEngine.value
        setSheet {
            var open by remember { mutableStateOf(false) }
            Box {
                Column(Modifier.fillMaxWidth().clickable { open = true }.padding(horizontal = 24.dp, vertical = 12.dp)) {
                    Text(stringResource(R.string.suggest_engine), style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(engineLabels[engine.intValue]), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                com.mycompany.app.ui.components.BubbleMenu(expanded = open, onDismiss = { open = false }) {
                    for (value in engineOrder) DropdownMenuItem(text = { Text(stringResource(engineLabels[value])) }, onClick = { engine.intValue = value; open = false })
                }
            }
            for ((bit, label) in listOf(2 to R.string.recent_search, 4 to R.string.history, 8 to R.string.bookmark, 16 to R.string.quick_access)) {
                val on = (sources.intValue and bit) == bit
                val toggle = { sources.intValue = if (on) sources.intValue and bit.inv() else sources.intValue or bit }
                Row(Modifier.fillMaxWidth().clickable { toggle() }.padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(label), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Switch(checked = on, onCheckedChange = { toggle() })
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { apply() }) { Text(stringResource(R.string.apply)) }
            }
        }
        show()
    }

    companion object {
        @JvmField
        val engineLabels: IntArray = intArrayOf(R.string.not_used, R.string.duckduckgo, R.string.google)
        @JvmField
        val engineOrder: IntArray = intArrayOf(2, 1, 0)
    }
}
