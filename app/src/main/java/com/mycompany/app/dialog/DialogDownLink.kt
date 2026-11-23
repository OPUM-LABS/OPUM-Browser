package com.mycompany.app.dialog

import android.content.DialogInterface
import android.text.TextUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogDownSize.DownSizeListener
import com.mycompany.app.dialog.DialogPreview.PreviewListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Link details of a download ([linkUrlParam] = URL, [referer] = referer / agent for the size lookup, [fileSize] = size or 0): a row with
 * the size (or a "?" that opens [DialogDownSize]), open-with, share and copy-link, then the full URL with the file
 * extension highlighted, and OK. Listener calls as before: `b(null, size, false)` after a looked-up size, `c(url, null)`
 * open with, `d(url)` share. Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogDownLink(
    mainActivity: android.app.Activity?,
    linkUrlParam: String?,
    referer: String?,
    fileSize: Long,
    previewListener: PreviewListener?,
) : ComposeBottomDialog(mainActivity) {
    @JvmField
    var hostActivity: android.app.Activity? = mainActivity
    @JvmField
    var previewCallback: PreviewListener? = previewListener
    @JvmField
    var linkUrl: String? = linkUrlParam
    @JvmField
    var referer: String? = referer
    @JvmField
    var sizeDialog: DialogDownSize? = null

    private var size by mutableLongStateOf(fileSize)

    init {
        setSheet {
            Row(
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (size > 0) {
                    Text(MainUtil.sizeLabel(size), style = MaterialTheme.typography.titleSmall)
                } else {
                    IconButton(onClick = { openSize() }, modifier = Modifier.padding(start = 0.dp)) {
                        Icon(painterResource(R.drawable.outline_contact_support_black_20), contentDescription = stringResource(R.string.estimated_size))
                    }
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { previewCallback?.download(linkUrl, null) }) {
                    Icon(painterResource(R.drawable.outline_open_with_black_24), contentDescription = null)
                }
                IconButton(onClick = { previewCallback?.share(linkUrl) }) {
                    Icon(painterResource(R.drawable.outline_share_black_24), contentDescription = null)
                }
                IconButton(onClick = { MainUtil.copyTextToast(context, R.string.copied_clipboard, "Copied URL", linkUrl) }) {
                    Icon(painterResource(R.drawable.outline_link_black_24), contentDescription = null)
                }
            }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp))
            SelectionContainer(
                Modifier.fillMaxWidth().weight(1f, fill = false).heightIn(min = 128.dp)
                    .verticalScroll(rememberScrollState()).padding(24.dp),
            ) {
                val mark = MaterialTheme.colorScheme.primaryContainer
                val urlText = androidx.compose.runtime.remember(mark) { buildUrlText(linkUrl, mark) }
                Text(urlText, style = MaterialTheme.typography.bodyLarge)
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { dismiss() }) { Text(stringResource(R.string.ok)) }
            }
        }
        show()
    }

    /** URL text: `data:image/` cut at 256 characters, otherwise the extension highlighted. */
    private fun buildUrlText(url: String?, mark: androidx.compose.ui.graphics.Color): AnnotatedString {
        if (url == null) return AnnotatedString("")
        if (url.startsWith("data:image/")) {
            return AnnotatedString(if (url.length > 256) url.substring(0, 256) + "..." else url)
        }
        val ext = MainUtil.fileExtOf(url, false)
        var index = -1
        if (!TextUtils.isEmpty(ext)) {
            val dot = url.lastIndexOf(".$ext")
            index = if (dot != -1) dot + 1 else {
                val eq = url.lastIndexOf("=$ext")
                if (eq != -1) eq + 1 else url.lastIndexOf(ext!!)
            }
        }
        if (index < 0 || ext == null || index + ext.length > url.length) return AnnotatedString(url)
        return buildAnnotatedString {
            append(url.substring(0, index))
            withStyle(SpanStyle(background = mark)) {
                append(url.substring(index, index + ext.length))
            }
            append(url.substring(index + ext.length))
        }
    }

    private fun openSize() {
        if (hostActivity == null || sizeDialog != null) return
        val dialog = DialogDownSize(hostActivity, linkUrl, referer, size, object : DownSizeListener {
            override fun onSize(size: Long) {
                this@DialogDownLink.size = size
                previewCallback?.openFile(null, size, false)
            }
        })
        sizeDialog = dialog
        dialog.setOnDismissListener(DialogInterface.OnDismissListener { sizeDialog = null })
    }

    override fun dismiss() {
        val sizeDialogNow = sizeDialog
        sizeDialog = null
        sizeDialogNow?.dismiss()
        hostActivity = null
        previewCallback = null
        super.dismiss()
    }

    companion object {
    }
}
