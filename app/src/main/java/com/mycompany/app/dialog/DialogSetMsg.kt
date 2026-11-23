package com.mycompany.app.dialog

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * The generic message / confirm sheet of the browser and of many legacy dialogs: a message, an optional gear, a positive
 * action and, when given, a cancel button. Compose now (on [ComposeBottomDialog]); the three constructors, the listeners
 * ([DialogMsgListener]: `onConfirm()` = positive, `onHelp()` = gear; or a [DialogApplyListener] for the positive action) and
 * [startBusy] ("busy": the sheet locks and the positive action is disabled) are as before, and it shows itself when built.
 */
class DialogSetMsg : ComposeBottomDialog {
    interface DialogMsgListener {
        fun onConfirm()
        fun onHelp()
    }

    private var apply: DialogApplyListener? = null
    private var messages: DialogMsgListener? = null
    private var withGear = false
    private var messageRes = 0
    private var messageText: String? = null
    private var positiveRes = 0
    private var negativeRes = 0
    private var busy by mutableStateOf(false)

    /** Colour filter of the screen-filter dialogs; the Material sheet does not tint, so it is ignored. */

    /** Message with an optional gear (`onHelp()`), a positive action (`onConfirm()`) and, if [negative] is not 0, a cancel button. */
    constructor(activity: Activity?, gear: Boolean, message: Int, positive: Int, negative: Int, listener: DialogMsgListener?) : super(activity) {
        messages = listener
        withGear = gear
        messageRes = message
        positiveRes = positive
        negativeRes = negative
        build()
    }

    /** Message text with a single positive action. */
    constructor(activity: Activity?, message: String?, positive: Int, listener: DialogApplyListener?) : super(activity) {
        apply = listener
        messageText = message
        positiveRes = positive
        build()
    }

    /** Message resource with a single positive action. */
    constructor(activity: Activity?, message: Int, positive: Int, listener: DialogApplyListener?) : super(activity) {
        apply = listener
        messageRes = message
        positiveRes = positive
        build()
    }

    /** Busy: ignore the scrim and disable the positive action until the dialog is closed. */
    fun startBusy() {
        busy = true
    }

    override fun canCancelFromScrim(): Boolean = !busy

    private fun positive() {
        val applyListener = apply
        if (applyListener != null) { applyListener.onApply(); return }
        messages?.onConfirm()
    }

    private fun build() {
        setSheet {
            if (withGear) {
                Box(Modifier.fillMaxWidth().padding(top = 8.dp, end = 8.dp)) {
                    IconButton(onClick = { messages?.onHelp() }, modifier = Modifier.align(Alignment.CenterEnd)) {
                        Icon(painterResource(R.drawable.outline_settings_black_20), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            val message = if (messageRes != 0) stringResource(messageRes) else messageText
            if (!message.isNullOrEmpty()) {
                Box(
                    Modifier.fillMaxWidth().heightIn(min = if (messages != null) 88.dp else 72.dp).padding(horizontal = 24.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                    )
                }
            }
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 24.dp))
            if (positiveRes != 0 || negativeRes != 0) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    if (negativeRes != 0) {
                        TextButton(onClick = { dismiss() }, modifier = Modifier.weight(1f)) { Text(stringResource(negativeRes)) }
                    }
                    if (positiveRes != 0) {
                        TextButton(onClick = { positive() }, enabled = !busy, modifier = Modifier.weight(1f)) { Text(stringResource(positiveRes)) }
                    }
                }
            }
        }
        show()
    }

    override fun dismiss() {
        apply = null
        messages = null
        messageText = null
        super.dismiss()
    }
}
