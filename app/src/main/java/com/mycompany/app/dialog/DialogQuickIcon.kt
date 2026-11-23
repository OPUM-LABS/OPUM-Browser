package com.mycompany.app.dialog

import android.app.Activity
import android.graphics.Bitmap
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.JarBridge
import com.mycompany.app.main.MainItem
import com.mycompany.app.main.MainItem.ViewItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.ViewItemListener
import com.mycompany.app.ui.browser.ComposeBottomDialog

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

/**
 * Fetch the icon of the site at [iconUrl]: first from the local icon cache (`MainUtil.siteIcon`), then over the network. A spinner
 * shows while loading; then the icon (button: Apply) or "no icon" (button: OK). The button hands the bitmap (or null) to
 * [QuickLoadListener] and closes. Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogQuickIcon(activity: Activity?, iconUrl: String?, quickLoadListener: QuickLoadListener?) : ComposeBottomDialog(activity) {
    private var listener: QuickLoadListener? = quickLoadListener
    private val loaded = mutableStateOf(false)
    private val bitmap = mutableStateOf<Bitmap?>(null)

    interface QuickLoadListener {
        fun onIcon(bitmap: Bitmap?)
    }

    private suspend fun fetch(address: String?): Bitmap? {
        val cached = withContext(Dispatchers.IO) { MainUtil.siteIcon(context, address) }
        if (MainUtil.isBitmapOk(cached) || address == null) return cached
        return suspendCancellableCoroutine { cont ->
            val item = ViewItem()
            item.itemType = 7.toByte()
            item.url = MainUtil.faviconUrlFor(address)
            item.cacheType = 2
            com.mycompany.app.glide.ThumbLoader.loadAlone(item, object : ViewItemListener {
                override fun onFailed(viewItem: MainItem.ViewItem?, view: View?, failReason: Exception?) { if (cont.isActive) cont.resume(null) }
                override fun onComplete(viewItem: MainItem.ViewItem?, view: View?, bitmap: Bitmap?) { if (cont.isActive) cont.resume(bitmap) }
            })
        }
    }

    init {
        setSheet {
            LaunchedEffect(Unit) {
                bitmap.value = try { fetch(iconUrl) } catch (_: Exception) { null }
                loaded.value = true
            }
            Box(Modifier.fillMaxWidth().heightIn(min = 96.dp).padding(16.dp), contentAlignment = Alignment.Center) {
                val icon = bitmap.value
                when {
                    !loaded.value -> CircularProgressIndicator(Modifier.size(32.dp))
                    MainUtil.isBitmapOk(icon) -> Image(icon!!.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(64.dp).clip(CircleShape))
                    else -> Text(stringResource(R.string.no_icon), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { listener?.onIcon(bitmap.value); dismiss() }) {
                    Text(stringResource(if (!loaded.value) R.string.cancel else if (MainUtil.isBitmapOk(bitmap.value)) R.string.apply else R.string.ok))
                }
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
