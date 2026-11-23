package com.mycompany.app.dialog

import android.app.Activity
import com.mycompany.app.ui.state.AppState
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Save the page as PDF: shows the page's thumbnail (or a PDF icon) and title, and a file-name field pre-filled from the
 * title (`MainUtil.safeTitleFileName(186, ...)`, all selected). Apply / Done checks the name (not empty, at most 200 bytes) and hands it to
 * [PathChangeListener]; a bad name shows a toast and the sheet stays. Compose now (on [ComposeBottomDialog]); same
 * constructor, shows itself.
 */
class DialogPrintPage(activity: Activity?, titleParam: String?, bitmap: Bitmap?, pathChangeListener: PathChangeListener?) : ComposeBottomDialog(activity) {
    private var listener: PathChangeListener? = pathChangeListener

    interface PathChangeListener {
        fun onPath(path: String?)
    }

    private val thumbState = mutableStateOf<Bitmap?>(if (MainUtil.isBitmapOk(bitmap)) bitmap else null)

    /** The page thumbnail arrived after the sheet opened. */
    fun setThumb(bitmap: Bitmap?) { if (MainUtil.isBitmapOk(bitmap)) thumbState.value = bitmap }

    private fun submit(raw: String) {
        val name = raw.trim()
        if (name.isEmpty()) {
            MainUtil.showToast(context, R.string.input_name)
            return
        }
        if (name.toByteArray().size > 200) {
            MainUtil.showToast(context, R.string.long_name)
            return
        }
        listener?.onPath(name)
        dismiss()
    }

    init {
        val title = titleParam.orEmpty()
        val initial = if (title.isEmpty()) "" else (MainUtil.safeTitleFileName(186, title, "Printpage") ?: "")
        setSheet {
            var value by remember { mutableStateOf(TextFieldValue(initial, TextRange(0, initial.length))) }
            val focus = remember { FocusRequester() }
            val keyboard = LocalSoftwareKeyboardController.current
            LaunchedEffect(Unit) { try { focus.requestFocus(); keyboard?.show() } catch (_: Exception) {} }
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                val shape = RoundedCornerShape(8.dp)
                val thumb = thumbState.value
                if (thumb != null) {
                    Image(thumb.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(48.dp).clip(shape))
                } else {
                    Image(
                        painterResource(if (AppState.darkUi) R.drawable.outline_picture_as_pdf_dark_24 else R.drawable.outline_picture_as_pdf_black_24),
                        contentDescription = null, modifier = Modifier.size(48.dp),
                    )
                }
                Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 16.dp))
            }
            OutlinedTextField(
                value = value, onValueChange = { value = it }, singleLine = true,
                label = { Text(stringResource(R.string.name)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit(value.text) }),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).focusRequester(focus),
            )
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { submit(value.text) }) { Text(stringResource(R.string.apply)) }
            }
        }
        show()
    }

    override fun dismiss() {
        listener = null
        super.dismiss()
    }

    companion object {
    }
}
