package com.mycompany.app.dialog

import android.webkit.JsPromptResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainUtil.trimTrailingSpace
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * A web page's JavaScript `prompt()`: the message [messageParam] over a text field pre-filled with the default [initialParam] (all selected).
 * OK or the keyboard's Done confirms the [JsPromptResult] with the text (trailing blanks trimmed); closing the sheet any
 * other way cancels it. Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogPrompt(mainActivity: MainActivity?, messageParam: String?, initialParam: String?, jsPromptResult: JsPromptResult?) : ComposeBottomDialog(mainActivity) {
    private var result: JsPromptResult? = jsPromptResult

    private fun confirm(input: String) {
        val pending = result ?: return
        result = null
        pending.confirm(trimTrailingSpace(input) ?: "")
        dismiss()
    }

    init {
        val message = messageParam.orEmpty()
        val initial = initialParam.orEmpty()
        setSheet {
            var value by remember { mutableStateOf(TextFieldValue(initial, TextRange(0, initial.length))) }
            val focus = remember { FocusRequester() }
            val keyboard = LocalSoftwareKeyboardController.current
            LaunchedEffect(Unit) { try { focus.requestFocus(); keyboard?.show() } catch (_: Exception) {} }
            Text(
                message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(start = 24.dp, end = 24.dp, top = 24.dp),
            )
            OutlinedTextField(
                value = value, onValueChange = { value = it }, singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { confirm(value.text) }),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp).focusRequester(focus),
            )
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { confirm(value.text) }) { Text(stringResource(R.string.ok)) }
            }
        }
        show()
    }

    override fun dismiss() {
        result?.cancel()
        result = null
        super.dismiss()
    }
}
