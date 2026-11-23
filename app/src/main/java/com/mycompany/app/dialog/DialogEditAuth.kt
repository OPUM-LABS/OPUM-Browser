package com.mycompany.app.dialog

import com.mycompany.app.ui.components.AppPasswordField
import android.app.Activity
import android.webkit.HttpAuthHandler
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * The HTTP authentication prompt of a page: name and password (with "show password"); Apply checks both are filled and
 * hands them to the WebView's [HttpAuthHandler]; closing the dialog any other way cancels the request. Compose now (on
 * [ComposeBottomDialog]); same constructor, it shows itself.
 */
class DialogEditAuth(host: Activity?, httpAuthHandler: HttpAuthHandler?) : ComposeBottomDialog(host) {
    private var handler: HttpAuthHandler? = httpAuthHandler
    private var applying = false

    private fun apply(name: String, password: String): Boolean {
        val user = name.trim()
        val pass = password.trim()
        if (user.isEmpty()) { MainUtil.showToast(context, R.string.input_name); return false }
        if (pass.isEmpty()) { MainUtil.showToast(context, R.string.input_password); return false }
        handler?.proceed(user, pass)
        handler = null
        dismiss()
        return true
    }

    init {
        setSheet {
            var name by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var visible by remember { mutableStateOf(false) }
            val nameFocus = remember { FocusRequester() }
            val passwordFocus = remember { FocusRequester() }
            val keyboard = LocalSoftwareKeyboardController.current
            LaunchedEffect(Unit) {
                try { nameFocus.requestFocus(); keyboard?.show() } catch (_: Exception) {}
            }
            OutlinedTextField(
                value = name, onValueChange = { name = it }, singleLine = true,
                label = { Text(stringResource(R.string.name)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { passwordFocus.requestFocus() }),
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 24.dp).focusRequester(nameFocus),
            )
            AppPasswordField(
                value = password, onValueChange = { password = it }, visible = visible,
                label = { Text(stringResource(R.string.password)) },
                trailingIcon = {
                    androidx.compose.material3.IconButton(onClick = { visible = !visible }) {
                        androidx.compose.material3.Icon(
                            androidx.compose.ui.res.painterResource(if (visible) R.drawable.outline_visibility_black_20 else R.drawable.outline_visibility_off_black_20), contentDescription = null,
                            tint = if (visible) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                onDone = { if (!applying) { applying = true; apply(name, password); applying = false } },
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 12.dp).focusRequester(passwordFocus),
            )
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
            ) {
                TextButton(onClick = { if (!applying) { applying = true; apply(name, password); applying = false } }) {
                    Text(stringResource(R.string.apply))
                }
            }
        }
        show()
    }

    override fun dismiss() {
        handler?.cancel()
        handler = null
        super.dismiss()
    }
}
