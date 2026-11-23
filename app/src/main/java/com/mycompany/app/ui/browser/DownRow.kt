package com.mycompany.app.ui.browser

import android.graphics.Bitmap
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ch.opumlabs.opumbrowser.R
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.mycompany.app.main.JarBridge
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainDownAdapter.DownListItem
import com.mycompany.app.main.MainItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.ViewItemListener
import com.mycompany.app.view.GlideApp
import com.mycompany.app.view.MyRoundImage

/**
 * One row of a download list (the Compose version of the old `MainDownAdapter` row): a thumbnail (the poster
 * when there is one, else the row number; downloaded = check, failed = "!"), the name (2 lines), a "no sound"
 * icon, the format badge (MP4 for split / m3u8 files) and the info and play buttons. "HDSD" rows have no
 * badge and no buttons. [mode] is the list kind of the old adapter (2 = gif page, 3 = dcinside: Glide posters).
 */
@Composable
fun DownRow(
    act: android.app.Activity?,
    item: DownListItem,
    pos: Int,
    mode: Int,
    referer: String?,
    onClick: () -> Unit,
    onInfo: () -> Unit,
    onPlay: () -> Unit,
) {
    val fmt = item.fmt
    val hdsd = "HDSD" == fmt
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(onClick = onClick).padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
            val poster = item.thumb
            if (TextUtils.isEmpty(poster)) {
                when (item.checkState.toInt()) {
                    1, 2 -> Surface(shape = CircleShape, color = Color(0xFF9E9E9E.toInt()), modifier = Modifier.size(40.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painterResource(if (item.checkState.toInt() == 1) R.drawable.outline_check_white_20 else R.drawable.outline_priority_high_white_20),
                                null, tint = Color.White,
                            )
                        }
                    }
                    else -> Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHighest, modifier = Modifier.size(40.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text((pos + 1).toString(), style = MaterialTheme.typography.labelLarge, maxLines = 1)
                        }
                    }
                }
            } else {
                AndroidView(
                    factory = { ctx ->
                        MyRoundImage(ctx).apply {
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            setCircleRadius(MainUtil.dpToPx(ctx, 20.0f).toInt().toFloat())
                        }
                    },
                    update = { view -> loadDownThumb(act, view, poster, referer, mode, item.index) },
                    modifier = Modifier.size(40.dp).clip(CircleShape),
                )
                if (item.checkState.toInt() == 1 || item.checkState.toInt() == 2) {
                    Surface(shape = CircleShape, color = Color(0x99000000.toInt()), modifier = Modifier.size(40.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painterResource(if (item.checkState.toInt() == 1) R.drawable.outline_check_white_20 else R.drawable.outline_priority_high_white_20),
                                null, tint = Color.White,
                            )
                        }
                    }
                }
            }
        }
        Text(
            item.title ?: "", maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 8.dp),
        )
        if (item.isHidden) {
            Icon(
                painterResource(R.drawable.outline_no_sound_black_24), null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 8.dp),
            )
        }
        if (!hdsd) {
            if (!TextUtils.isEmpty(fmt)) {
                val src = item.url
                val split = !TextUtils.isEmpty(src) && (src!!.startsWith("tsfile:") || src.startsWith("tsfake:"))
                val badge = if (split || fmt!!.endsWith("M3U8")) "MP4" else fmt
                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                    Text(badge, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            IconButton(onClick = onInfo) {
                Icon(painterResource(R.drawable.outline_info_black_24), stringResource(R.string.info))
            }
            if (canPlay(item, mode)) {
                IconButton(onClick = onPlay) {
                    Icon(painterResource(R.drawable.baseline_play_arrow_black_24), null)
                }
            }
        }
    }
}

/** Old adapter rule for the play button: subtitles-capable / gif lists, or an image / video / audio type. */
private fun canPlay(item: DownListItem, mode: Int): Boolean {
    if (item.isLive || mode == 2) return true
    val fmt = item.fmt
    if (TextUtils.isEmpty(fmt)) return false
    val mime = MainUtil.mimeForExt(fmt)
    if (TextUtils.isEmpty(mime)) return false
    return mime!!.startsWith("image") || mime.startsWith("video") || mime.startsWith("audio")
}


/** Loads a poster into [view] the way the old adapter did (Glide for dcinside lists, else the image loader). */
private fun loadDownThumb(act: android.app.Activity?, view: MyRoundImage, url: String?, referer: String?, mode: Int, id: Int) {
    if (view.tag == url) return
    view.tag = url
    view.showIcon(-460552, R.drawable.outline_image_black_24)
    if (mode == 3 && MainUtil.isDcinsideImage(url)) {
        if (act == null) return
        (GlideApp.with(act)!!.load(MainUtil.glideUrlFor(act, url, referer)).diskCacheStrategy(DiskCacheStrategy.NONE) as RequestBuilder<*>).into(view)
        return
    }
    val cached = com.mycompany.app.glide.ImageCache.get(com.mycompany.app.glide.ImageCache.key(2, url))
    if (MainUtil.isBitmapOk(cached)) {
        view.setImageBitmap(cached)
        return
    }
    val viewItem = MainItem.ViewItem()
    viewItem.itemType = 7.toByte()
    viewItem.url = url
    viewItem.referer = referer
    viewItem.position = id
    viewItem.cacheType = 2
    com.mycompany.app.glide.ThumbLoader.loadFor(viewItem, view, object : ViewItemListener {
        override fun onComplete(item: MainItem.ViewItem?, view: View?, bitmap: Bitmap?) {
            if (item != null && view is MyRoundImage && view.tag == item.url && MainUtil.isBitmapOk(bitmap)) view.setImageBitmap(bitmap)
        }
    })
}
