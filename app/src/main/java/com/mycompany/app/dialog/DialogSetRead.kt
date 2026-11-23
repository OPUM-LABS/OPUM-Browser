package com.mycompany.app.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainActivity
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * How to open a page in reader mode (reworked 2026-11-21: it offered "TEXT" and "HTML"): **Reading view** (just the
 * text in the reader window, with the reader's font size and read aloud) or **Simple page** (the article with its
 * pictures and links as a page in this tab), each with an icon and one line saying what it is. "Don't ask again"
 * stores the choice ([ZtwoSettings.readMode]: 1 reading view, 2 simple page; Settings > Web pages > Reader mode
 * changes it back). Constructor, [SetReadListener] and the auto-show are as before; [SetReadListener.onPick]'s
 * `startRead` is true for the simple page.
 */
class DialogSetRead(mainActivity: MainActivity?, pathParam: String?, setReadListener: SetReadListener?) :
    ComposeBottomDialog(mainActivity) {
    private var activity: MainActivity? = mainActivity
    private var listener: SetReadListener? = setReadListener
    private val path: String? = pathParam

    /** Non-null while the dialog is built; the activity only checks it for null (was the choice adapter). */
    @JvmField
    var resultTarget: Any? = this

    interface SetReadListener {
        fun onPick(url: String?, startRead: Boolean)
    }

    init {
        setSheet {
            var remember by remember { mutableStateOf(false) }
            fun pick(simplePage: Boolean) {
                if (remember) {
                    val context = activity
                    ZtwoSettings.edit(context) { set(ZtwoSettings.readMode, if (simplePage) 2 else 1) }
                }
                listener?.onPick(path, simplePage)
            }
            Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.reader_mode), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 8.dp, bottom = 4.dp))
                ReadChoice(R.drawable.outline_article_black_24, R.string.read_view, R.string.read_view_sum) { pick(false) }
                ReadChoice(R.drawable.outline_chrome_reader_mode_black_24, R.string.read_page, R.string.read_page_sum) { pick(true) }
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { remember = !remember }.padding(start = 8.dp, top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.read_remember), style = MaterialTheme.typography.bodyLarge)
                        Text(stringResource(R.string.read_remember_sum), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = remember, onCheckedChange = { remember = it })
                }
            }
        }
        show()
    }

    override fun dismiss() {
        resultTarget = null
        activity = null
        listener = null
        super.dismiss()
    }
}

/** One way to read: a tonal card with an icon, a name and one line saying what it is. */
@androidx.compose.runtime.Composable
private fun ReadChoice(icon: Int, title: Int, summary: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Column(Modifier.padding(start = 16.dp)) {
                Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(summary), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
