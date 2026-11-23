package com.mycompany.app.lock

import com.mycompany.app.ui.components.AppPasswordField
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.lock.LockComposeActivity
import com.mycompany.app.ui.lock.LockLayout
import com.mycompany.app.ui.lock.LockSecondaryButton

/**
 * Password lock (at least 4 characters). Unlocking compares the input with the stored password and shows "Wrong
 * input" otherwise; setting a new one asks twice (Continue, then Apply). The intent contract and behaviour are in
 * [LockComposeActivity].
 */
class PassActivity : LockComposeActivity() {

    @Composable
    override fun Content() {
        LockBack()
        // restart the whole input state when the activity gets a new intent
        androidx.compose.runtime.key(generation) { PassScreen() }
    }

    @Composable
    private fun PassScreen() {
        var field by remember { mutableStateOf(TextFieldValue("")) }
        var message by remember { mutableStateOf<Int?>(null) }
        var confirming by remember { mutableStateOf(false) }   // second entry of a new password
        var first by remember { mutableStateOf<String?>(null) }
        val focus = remember { FocusRequester() }
        LaunchedEffect(Unit) { focus.requestFocus() }

        val length = field.text.length

        fun clear() { field = TextFieldValue("") }

        fun submitVerify() {
            val input = field.text
            if (input.isEmpty()) return
            if (input != expectedSecret()) {
                field = field.copy(selection = TextRange(0, input.length))
                message = R.string.wrong_input
            } else {
                unlocked()
            }
        }

        fun submitSetting() {
            val input = field.text
            if (input.length < 4) return
            if (!confirming) {
                confirming = true
                first = input
                clear()
                message = R.string.reinput
                return
            }
            if (input != first) {
                field = field.copy(selection = TextRange(0, input.length))
                message = R.string.wrong_input
                return
            }
            storeSecret(3, first)
        }

        LockLayout(message?.let { stringResource(it) }) {
            AppPasswordField(
                value = field,
                onValueChange = {
                    field = it
                    if (!isSetting) message = null
                    else if (message == R.string.wrong_input) message = if (confirming) R.string.reinput else null
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                onDone = { if (isSetting) submitSetting() else submitVerify() },
                placeholder = { if (isSetting) Text(stringResource(R.string.pass_hint), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
                textStyle = androidx.compose.material3.LocalTextStyle.current.copy(textAlign = TextAlign.Center),
                trailingIcon = {
                    if (!isSetting) {
                        IconButton(onClick = { submitVerify() }, enabled = length >= 4) {
                            Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.ok))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
            )
            if (isSetting) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(
                        onClick = {
                            confirming = false; first = null; clear(); message = null
                        },
                        enabled = length > 0 || confirming,
                    ) { Text(stringResource(R.string.retry)) }
                    Button(onClick = { submitSetting() }, enabled = length >= 4) {
                        Text(stringResource(if (confirming) R.string.apply else R.string.continue_input))
                    }
                }
            } else {
                LockSecondaryButton(secondaryLabel()) { onSecondary() }
            }
        }
    }
}
