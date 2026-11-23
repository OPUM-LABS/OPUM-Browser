package com.mycompany.app.dialog

import com.mycompany.app.ui.components.AppPasswordField
import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.webkit.URLUtil
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.compress.CompressUtilZip2
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.ui.browser.ComposeBottomDialog
import java.lang.ref.WeakReference

/**
 * One text field with Apply. Modes: [passwordModeParam] = password (hidden, eye toggle; with an archive path [initialText] the password is
 * checked against it by [DialogTask] before the listener hears it, "Checking" while it runs); [titleRes] =
 * `news_info_1` / `url` = URL (prefilled with [initialText], must be a network URL); otherwise plain text. The label is [titleRes]
 * ("RSS Feed URL" for `news_info_1`, "Name" when 0). Cancel (back / scrim) calls `EditTextListener.onCleared()`. Compose now
 * (on [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogEditText(
    activity: Activity?,
    titleRes: Int,
    initialText: String?,
    unusedText: String?,
    passwordModeParam: Boolean,
    editTextListener: EditTextListener?,
) : ComposeBottomDialog(activity) {
    @JvmField
    var appContext: Context? = context
    @JvmField
    var textListener: EditTextListener? = editTextListener
    @JvmField
    var checkTask: DialogTask? = null
    @JvmField
    var applying: Boolean = false
    @JvmField
    var passwordMode: Boolean = passwordModeParam
    @JvmField
    var urlMode: Boolean = !passwordModeParam && (titleRes == AppR.string.news_info_1 || titleRes == AppR.string.url)
    @JvmField
    var checkPath: String? = initialText

    private var field by mutableStateOf(
        if (urlMode && !TextUtils.isEmpty(initialText)) TextFieldValue(initialText!!, TextRange(0, initialText.length)) else TextFieldValue("")
    )
    private var visible by mutableStateOf(false)
    internal var checking by mutableStateOf(false)

    class DialogTask(dialogEditText: DialogEditText?, zipPath: String?, entryName: String?) :
        MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var zipPath: String? = null
        @JvmField
        var entryName: String? = null
        @JvmField
        var withFiles: Boolean = false

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogEditText)
            this.dialogRef = weakReference
            if ((weakReference.get() as DialogEditText?) == null) {
                return@init
            }
            this.zipPath = zipPath
            this.entryName = entryName
        
        }
    }

        /** Extracts the archive with the given password and reports whether it worked. */
        public override fun doInBackground() {
            if ((this.dialogRef!!.get() as DialogEditText?) == null || this.cancelled) {
                return
            }
            try {
                this.withFiles = CompressUtilZip2.extractStream(this.zipPath, this.entryName)
            } catch (throwable: Throwable) {
                this.withFiles = false
            }
        }

        public override fun onPostExecute() {
            val dialogEditText = this.dialogRef!!.get() as DialogEditText?
            if (dialogEditText != null) {
                dialogEditText.checkTask = null
                if (this.withFiles) {
                    val editTextListener = dialogEditText.textListener
                    if (editTextListener != null) {
                        editTextListener.onText(this.entryName)
                        return
                    }
                    return
                }
                showToast(dialogEditText.appContext, AppR.string.invalid_password)
                dialogEditText.checking = false
            }
        }
    }

    interface EditTextListener {
        fun onText(input: String?)
        fun onCleared()
    }

    private val label: String = when {
        titleRes == AppR.string.news_info_1 -> "RSS Feed URL"
        titleRes <= 0 -> context.getString(AppR.string.name)
        else -> context.getString(titleRes)
    }

    init {
        setSheet {
            val focus = remember { FocusRequester() }
            val keyboard = LocalSoftwareKeyboardController.current
            val eye: (@Composable () -> Unit)? = if (!passwordMode) null else {
                {
                    IconButton(onClick = { visible = !visible }) {
                        Icon(
                            painterResource(if (visible) AppR.drawable.outline_visibility_black_24 else AppR.drawable.outline_visibility_off_black_24),
                            contentDescription = null,
                        )
                    }
                }
            }
            if (passwordMode) AppPasswordField(
                value = field,
                onValueChange = { field = it },
                visible = visible,
                enabled = !checking,
                label = { Text(label) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                onDone = { apply() },
                trailingIcon = eye,
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 24.dp).focusRequester(focus),
            ) else OutlinedTextField(
                value = field,
                onValueChange = { field = it },
                enabled = !checking,
                singleLine = true,
                label = { Text(label) },
                placeholder = { if (urlMode) Text("https://...") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = when {
                        passwordMode -> KeyboardType.Password
                        urlMode -> KeyboardType.Uri
                        else -> KeyboardType.Text
                    },
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { apply() }),
                trailingIcon = eye,
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 24.dp).focusRequester(focus),
            )
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { apply() }, enabled = !checking) {
                    Text(stringResource(if (checking) AppR.string.checking else AppR.string.apply))
                }
            }
            LaunchedEffect(Unit) {
                focus.requestFocus()
                keyboard?.show()
            }
        }
        show()
    }

    private fun apply() {
        if (applying || checking) return
        applying = true
        submit()
        applying = false
    }

    /** Checks the input for the mode and hands it to the listener (password with an archive: after [DialogTask]). */
    fun submit() {
        val listener = textListener ?: return
        val input = field.text
        if (urlMode) {
            val trimmed = input.trim { it <= ' ' }
            if (TextUtils.isEmpty(trimmed)) {
                showToast(appContext, AppR.string.empty)
                return
            }
            val url = MainUtil.trimTrailingSlash(trimmed)
            if (URLUtil.isNetworkUrl(url)) listener.onText(url) else showToast(appContext, AppR.string.invalid_url)
            return
        }
        if (!passwordMode) {
            listener.onText(input.trim { it <= ' ' })
            return
        }
        val password = MainUtil.trimTrailingSpace(input)
        if (TextUtils.isEmpty(password)) {
            showToast(appContext, AppR.string.input_password)
            return
        }
        if (TextUtils.isEmpty(checkPath)) {
            listener.onText(password)
            return
        }
        checking = true
        checkTask?.cancelled = true
        checkTask = null
        val task = DialogTask(this, checkPath, password)
        checkTask = task
        task.start(appContext)
    }

    override fun canCancelFromScrim(): Boolean = !checking

    override fun cancel() {
        textListener?.onCleared()
        dismiss()
    }

    override fun dismiss() {
        checkTask?.cancelled = true
        checkTask = null
        textListener = null
        super.dismiss()
    }

    companion object {
    }
}
