package com.mycompany.app.dialog

import android.content.Intent
import com.mycompany.app.main.RequestCode
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.BrowserSettings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.book.DbBookAgent
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Desktop mode for the current page: mobile mode (shows the user agent in use), desktop for this tab, desktop for every
 * tab (`BrowserSettings.deskLock.value`, `mDeskLock`). The gear opens the user-agent list. Compose now (on [ComposeBottomDialog]); same
 * constructor, shows itself. The listener gets the new "desktop for this tab" state only when it changed.
 */
class DialogSetDesk(mainActivity: MainActivity?, deskParam: Boolean, setDeskListener: SetDeskListener?) : ComposeBottomDialog(mainActivity) {
    private var host: MainActivity? = mainActivity
    private var listener: SetDeskListener? = setDeskListener
    private var desk: Boolean = deskParam

    private val agent = androidx.compose.runtime.mutableStateOf(currentAgent())

    private fun currentAgent(): String? = ZtwoSettings.agentType.value.let { if (it < 4) MainConst.agentNames[it] else DbBookAgent.titleById(context, (it - 100).toLong()) }

    /** The user-agent list changed the user agent in use: show its name on the mobile-mode row. */
    fun refreshAgent() { agent.value = currentAgent() }

    interface SetDeskListener {
        fun onChanged(deskMode: Boolean)
    }

    private fun pick(index: Int) {
        val all = index == 2
        val one = index != 0
        if (BrowserSettings.deskLock.value != all) {
            BrowserSettings.edit(context) { set(BrowserSettings.deskLock, all) }
        }
        if (desk != one) {
            desk = one
            listener?.onChanged(one)
        }
        dismiss()
    }

    init {
        val selected = if (BrowserSettings.deskLock.value) 2 else if (desk) 1 else 0
        setSheet {
            Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                IconButton(onClick = {
                    val activity = host ?: return@IconButton
                    val intent = Intent(context, com.mycompany.app.ui.lists.UserAgentListActivity::class.java as Class<*>)
                    intent.putExtra("EXTRA_POPUP", true)
                    activity.launchExternalForResult(intent, RequestCode.DIALOG_SETTINGS)
                }) { Icon(Icons.Outlined.Settings, contentDescription = null) }
            }
            val rows = listOf(
                Triple(stringResource(R.string.mobile_mode), agent.value, 0),
                Triple(stringResource(R.string.desk_one), null, 1),
                Triple(stringResource(R.string.desk_all), null, 2),
            )
            for ((label, sub, index) in rows) {
                Row(Modifier.fillMaxWidth().clickable { pick(index) }.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected == index, onClick = { pick(index) })
                    Column(Modifier.padding(start = 8.dp)) {
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                        if (sub != null) Text(sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        show()
    }

    override fun dismiss() {
        host = null
        listener = null
        super.dismiss()
    }
}
