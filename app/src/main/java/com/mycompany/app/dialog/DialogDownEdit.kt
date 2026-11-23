package com.mycompany.app.dialog

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.dialog.DialogDownPage.DownPageListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.showToast

/**
 * Save an edited / cropped / captured image [bitmap] (image editor, crop screen, capture): header with the image and
 * a button that opens it full size ([DialogPreview]; hidden in landscape, [applyLandscape]), or the image icon and [pageTitle]; the name
 * ([pageTitle] + ".jpg") and save location. Save creates the file in the folder (`MainUri.createUri`) and hands its path and the
 * bitmap to [DownPageListener], then closes. Folder picker results come through [onPickResult]. Compose now (on
 * [SaveFolderDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogDownEdit(
    mainActivity: android.app.Activity?,
    pageTitle: String?,
    bitmap: Bitmap?,
    downPageListener: DownPageListener?,
) : SaveFolderDialog(mainActivity, ".jpg") {
    @JvmField
    var imageBitmap: Bitmap? = bitmap
    @JvmField
    var pageTitle: String? = pageTitle
    @JvmField
    var pageListener: DownPageListener? = downPageListener
    @JvmField
    var previewDialog: DialogPreview? = null
    @JvmField
    var saveUri: UriItem? = null
    /** Old status / navigation bar flags (set by `DialogCapture`; no effect on the Compose sheet). */

    init {
        start()
    }

    override fun prepare(): String? = MainUtil.safeTitleFileName(186, pageTitle, "Capture") + ".jpg"

    @Composable
    override fun ColumnScope.Header() {
        val bmp = imageBitmap
        if (MainUtil.isBitmapOk(bmp)) {
            Row(
                Modifier.fillMaxWidth().height(96.dp).padding(start = 24.dp, end = 16.dp, top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    bmp!!.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Fit,
                    modifier = Modifier.weight(1f).height(80.dp),
                )
                FilledTonalIconButton(onClick = { preview() }, modifier = Modifier.padding(start = 12.dp)) {
                    Icon(painterResource(AppR.drawable.baseline_play_arrow_black_24), contentDescription = null)
                }
            }
        } else {
            Row(
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                    Icon(painterResource(AppR.drawable.outline_image_black_24), contentDescription = null, modifier = Modifier.size(24.dp))
                }
                Text(
                    pageTitle ?: "", maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp),
                )
            }
        }
    }

    private fun preview() {
        if (hostActivity == null || previewDialog != null) return
        if (!MainUtil.isBitmapOk(imageBitmap)) {
            showToast(appContext, AppR.string.image_fail)
            return
        }
        val dialogPreview = DialogPreview(hostActivity, null, null, imageBitmap, "image/*", null)
        previewDialog = dialogPreview
        dialogPreview.setOnDismissListener(DialogInterface.OnDismissListener {
            previewDialog?.dismiss()
            previewDialog = null
        })
    }

    override fun onSave(fileName: String) {
        runAsync(Runnable {
            if (pageListener != null) saveUri = MainUri.createUri(appContext, MainUri.storageVolume(), null, fileName)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                val item = saveUri
                saveUri = null
                pageListener?.onSave(null, item?.uriString, imageBitmap)
                dismiss()
            }
        })
    }

    /** Folder picker result (request code 19). */
    fun onPickResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean = onFolderResult(requestCode, resultCode, intent)

    /** Landscape ([newLandscape]) hides the header. */
    fun applyLandscape(newLandscape: Boolean) {
        var landscape = newLandscape
        if (landscape) landscape = (hostActivity as? MainActivity)?.smallScreen ?: MainUtil.isSmallScreen(context)
        headerShown = !landscape
        previewDialog?.updateLayout(landscape)
    }

    override fun dismiss() {
        previewDialog?.dismiss()
        previewDialog = null
        pageListener = null
        super.dismiss()
    }

    companion object {
    }
}
