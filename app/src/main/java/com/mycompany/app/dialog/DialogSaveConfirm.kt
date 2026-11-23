package com.mycompany.app.dialog

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Leave the editor: 0 = exit with save, 1 = exit without save. The pick goes to the listener, then the sheet closes.
 * Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogSaveConfirm(activity: Activity?, dialogSeekListener: DialogSeekListener?) : ComposeBottomDialog(activity) {
    private var listener: DialogSeekListener? = dialogSeekListener

    init {
        setSheet {
            for ((id, label) in listOf(0 to R.string.exit_with_save, 1 to R.string.exit_without_save)) {
                Text(
                    stringResource(label), style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().clickable { listener?.onValue(id); dismiss() }.padding(horizontal = 24.dp, vertical = 16.dp),
                )
            }
        }
        show()
    }

    override fun dismiss() {
        listener = null
        super.dismiss()
    }
}
