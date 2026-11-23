package com.mycompany.app.quick

import android.content.Context
import android.widget.ImageView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mycompany.app.view.MyRoundImage

/**
 * One news article of the new-tab page (`QuickAdapter` item type 7), drawn by Compose: title next to a thumbnail,
 * then the source's icon, name and time, a "more" button, and the "translated" badge. The thumbnail ([thumb]) and
 * the source icon ([sourceIcon]) stay `MyRoundImage` views because the image loaders write into them. The frame
 * repeats `MyRoundFrame` mode 5 (filled only in the web style; continuing the card above when [joinsAbove]).
 * Tap, long press and "more" call [onOpen], [onLongPress], [onMore] with this card (its tag is the holder).
 */
@OptIn(ExperimentalFoundationApi::class)
class QuickNewsCard(context: Context) : QuickRowView(context) {
    var title by mutableStateOf("")
    var source by mutableStateOf("")
    var textColor by mutableIntStateOf(-16777216)
    var cardColor by mutableIntStateOf(0)
    var joinsAbove by mutableStateOf(false)
    var moreIcon by mutableIntStateOf(0)
    var pressColor by mutableIntStateOf(-2039584)
    /** The "translated by" badge (0 = none). */
    var badge by mutableIntStateOf(0)
    var onOpen: (QuickNewsCard) -> Unit = {}
    var onLongPress: (QuickNewsCard) -> Unit = {}
    var onMore: (QuickNewsCard) -> Unit = {}

    /** The article's picture (the loaders set it, `QuickAdapter.applyThumb` picks crop or fit). */
    val thumb = MyRoundImage(context)
    /** The source's icon (round). */
    val sourceIcon = MyRoundImage(context).also {
        it.scaleType = ImageView.ScaleType.CENTER_CROP
    }

    init {
        sourceIcon.setRoundRadius(Math.round(9 * context.resources.displayMetrics.density))
    }

    /** Newspaper style (the news page): own card per article, source line on top, serif headline. */
    var paper by mutableStateOf(false)
    var dark by mutableStateOf(false)
    var sourceName by mutableStateOf("")
    var age by mutableStateOf("")

    @Composable
    override fun Content() {
        if (paper) PaperCard() else ClassicCard()
    }

    @Composable
    private fun PaperCard() {
        val ink = if (dark) Color(0xFFF2EFE8) else Color(0xFF1A1A1A)
        val muted = ink.copy(alpha = 0.62f)
        val sheet = if (dark) Color(0xFF1D1C1A) else Color(0xFFF8F5EE)
        val edge = if (dark) Color(0x24FFFFFF) else Color(0x14000000)
        Box(Modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, bottom = 12.dp)) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(sheet)
                    .border(1.dp, edge, RoundedCornerShape(14.dp))
                    .combinedClickable(onClick = { onOpen(this@QuickNewsCard) }, onLongClick = { onLongPress(this@QuickNewsCard) })
                    .padding(start = 14.dp, top = 10.dp, end = 4.dp, bottom = 14.dp),
            ) {
                // source and time, like a newspaper's dateline
                Row(Modifier.fillMaxWidth().height(32.dp), verticalAlignment = Alignment.CenterVertically) {
                    AndroidView(factory = { sourceIcon }, modifier = Modifier.size(16.dp))
                    Text(
                        sourceName.uppercase(),
                        style = TextStyle(color = muted, fontSize = 11.sp, letterSpacing = 0.9.sp, fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp).widthIn(max = 190.dp),
                    )
                    if (age.isNotEmpty()) {
                        Text(
                            "  ·  $age",
                            style = TextStyle(color = muted, fontSize = 11.sp),
                            maxLines = 1,
                        )
                    }
                    Box(Modifier.weight(1f))
                    IconButton(
                        onClick = { onMore(this@QuickNewsCard) },
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color(pressColor)),
                    ) {
                        if (moreIcon != 0) Image(painterResource(moreIcon), null, Modifier.size(18.dp))
                    }
                }
                Row(Modifier.fillMaxWidth().padding(top = 6.dp, end = 10.dp), verticalAlignment = Alignment.Top) {
                    Text(
                        title,
                        style = TextStyle(
                            color = ink, fontSize = 18.sp, lineHeight = 24.sp,
                            fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold,
                        ),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 12.dp),
                    )
                    AndroidView(
                        factory = { thumb },
                        modifier = Modifier.size(104.dp, 76.dp).clip(RoundedCornerShape(10.dp)),
                    )
                }
                if (badge != 0) {
                    Image(
                        painterResource(badge), null,
                        Modifier.align(Alignment.End).padding(top = 8.dp, end = 10.dp).width(122.dp).height(16.dp),
                    )
                }
            }
        }
    }

    @Composable
    private fun ClassicCard() {
        Box(Modifier.fillMaxWidth()) {
            Frame()
            Column(
                Modifier.padding(bottom = 12.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .combinedClickable(onClick = { onOpen(this@QuickNewsCard) }, onLongClick = { onLongPress(this@QuickNewsCard) })
                    .padding(bottom = 4.dp),
            ) {
                Box(Modifier.fillMaxWidth()) {
                    Box(
                        Modifier.fillMaxWidth().heightIn(min = 76.dp).padding(start = 8.dp, end = 112.dp, top = 4.dp, bottom = 4.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(title, style = TextStyle(color = Color(textColor), fontSize = 16.sp))
                    }
                    AndroidView(
                        factory = { thumb },
                        modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp, end = 8.dp).size(96.dp, 68.dp),
                    )
                }
                Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically) {
                    AndroidView(factory = { sourceIcon }, modifier = Modifier.padding(start = 8.dp).size(18.dp))
                    Text(
                        source,
                        style = TextStyle(color = Color(textColor), fontSize = 14.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(start = 11.dp),
                    )
                    IconButton(
                        onClick = { onMore(this@QuickNewsCard) },
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color(pressColor)),
                    ) {
                        if (moreIcon != 0) Image(painterResource(moreIcon), null)
                    }
                }
                if (badge != 0) {
                    Image(
                        painterResource(badge), null,
                        Modifier.align(Alignment.End).padding(top = 8.dp, end = 8.dp).width(122.dp).height(16.dp),
                    )
                }
            }
        }
    }

    @Composable
    private fun BoxScope.Frame() {
        val color = cardColor
        if (color == 0) return
        val joins = joinsAbove
        Canvas(Modifier.matchParentSize()) {
            val r = 12.dp.toPx()
            val top = if (joins) -r else 0f
            val bottom = size.height - 12.dp.toPx()
            drawRoundRect(Color(color), Offset(0f, top), Size(size.width, bottom - top), CornerRadius(r))
        }
    }
}
