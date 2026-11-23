package com.mycompany.app.dialog

import android.graphics.Bitmap
import com.mycompany.app.ui.state.AppState
import android.view.View
import android.widget.ImageView.ScaleType
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainListLoader.ListLoadListener
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.view.MyRoundImage

/**
 * What to do with a link opened by a page's pop-up: preview, open in new tab, images only, image list, allow pop-ups. The
 * header shows the site's icon and title. A pick goes to the listener with the url and referrer; the sheet stays open (the
 * caller decides). [checked] marks "allow pop-ups" as the current choice. Compose now (on [ComposeBottomDialog]); [theme] only chose
 * the old dialog theme. Same constructor, shows itself. The favicon is still loaded with the legacy `MainListLoader`.
 */
class DialogPopupMenu(mainActivity: MainActivity?, theme: Int, urlParam: String?, referrerParam: String?, checked: Boolean, popupMenuListener: PopupMenuListener?) : ComposeBottomDialog(mainActivity) {
    private var listener: PopupMenuListener? = popupMenuListener
    private var url: String? = urlParam
    private var referrer: String? = referrerParam
    private var loader: MainListLoader? = null

    interface PopupMenuListener {
        fun onItem(id: Int, url: String?, referer: String?)
    }

    init {
        val host = MainUtil.shortHostOf(urlParam ?: "")
        val title = (MainUtil.urlDecode(urlParam)).let { if (it.isNullOrEmpty()) urlParam.orEmpty() else it }
        setSheet {
            val ctx = LocalContext.current
            val dark = AppState.darkUi
            val image = remember {
                val iconView = MyRoundImage(ctx)
                iconView.setScaleType(ScaleType.CENTER_CROP)
                iconView.setCircleRadius(MainApp.dp40 / 2.0f)
                if (urlParam.isNullOrEmpty()) {
                    iconView.showLetterIcon(-460552, R.drawable.outline_public_black_24, host, null)
                } else {
                    val cached = MainUtil.cachedIcon(host)
                    if (MainUtil.isBitmapOk(cached)) {
                        iconView.setIconSmall(true)
                        iconView.setImageBitmap(cached)
                    } else {
                        val child = ChildItem()
                        child.listType = 18
                        child.itemType = 11.toByte()
                        child.path = urlParam
                        loader = MainListLoader(ctx, false, object : ListLoadListener {
                            override fun onFailed(childItem: ChildItem?, view: View?) { iconView.showLetterIcon(-460552, R.drawable.outline_public_black_24, host, null) }
                            override fun onLoaded(childItem: ChildItem?, view: View?, bitmap: Bitmap?) {
                                if (MainUtil.isBitmapOk(bitmap)) { iconView.setIconSmall(true); iconView.setImageBitmap(bitmap) }
                                else iconView.showLetterIcon(-460552, R.drawable.outline_public_black_24, host, null)
                            }
                        })
                        iconView.setTag(0)
                        loader!!.enqueue(child, iconView)
                    }
                }
                iconView
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                AndroidView(factory = { image }, modifier = Modifier.size(48.dp))
                Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 16.dp))
            }
            val rows = listOf(
                Triple(if (dark) R.drawable.outline_visibility_dark_24 else R.drawable.outline_visibility_black_24, R.string.preview, 0),
                Triple(if (dark) R.drawable.outline_add_box_dark_24 else R.drawable.outline_add_box_black_24, R.string.new_url, 1),
                Triple(if (dark) R.drawable.outline_photo_library_dark_24 else R.drawable.outline_photo_library_black_24, R.string.only_image, 2),
                Triple(if (dark) R.drawable.outline_cloud_download_dark_24 else R.drawable.outline_cloud_download_black_24, R.string.image_list, 3),
                Triple(if (dark) R.drawable.outline_block_dark_24 else R.drawable.outline_block_black_24, R.string.pop_allow, 4),
            )
            androidx.compose.foundation.layout.Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                for ((icon, label, id) in rows) {
                    val current = checked && id == 4
                    Row(
                        Modifier.fillMaxWidth()
                            .background(if (current) MaterialTheme.colorScheme.secondaryContainer else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable { listener?.onItem(id, url, referrer) }.padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(24.dp))
                        Text(stringResource(label), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 16.dp))
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { dismiss() }) { Text(stringResource(R.string.close)) }
            }
        }
        show()
    }

    override fun dismiss() {
        loader?.release()
        loader = null
        listener = null
        url = null
        referrer = null
        super.dismiss()
    }

    companion object {
    }
}
