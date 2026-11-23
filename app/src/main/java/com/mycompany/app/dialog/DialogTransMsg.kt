package com.mycompany.app.dialog

import com.mycompany.app.ui.components.AppListItem
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSetDesk.SetDeskListener
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.web.WebViewActivity

/**
 * Shown when the translator is blocked by the site's policy: allow it for this time, or open the page in Chrome. Compose
 * now (on [ComposeBottomDialog]); constructor and the listener (true = allow, false = Chrome) are as before, and the
 * dialog shows itself when built. A second tap while a choice is being handled is ignored, as before.
 */
class DialogTransMsg(webViewActivity: WebViewActivity?, setDeskListener: SetDeskListener?) :
    ComposeBottomDialog(webViewActivity) {
    private var listener: SetDeskListener? = setDeskListener
    private var busy = false

    private fun choose(allow: Boolean) {
        if (busy) return
        busy = true
        window?.decorView?.post {
            listener?.onChanged(allow)
            busy = false
        }
    }

    init {
        setSheet {
            Box(Modifier.fillMaxWidth().heightIn(min = 88.dp).padding(16.dp), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.trans_blocked), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            AppListItem(
                headlineContent = { Text(stringResource(R.string.trans_allowed)) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable { choose(true) },
            )
            AppListItem(
                headlineContent = { Text(stringResource(R.string.open_chrome)) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable { choose(false) },
            )
            Box(Modifier.padding(bottom = 8.dp))
        }
        show()
    }

    override fun dismiss() {
        listener = null
        super.dismiss()
    }
}
