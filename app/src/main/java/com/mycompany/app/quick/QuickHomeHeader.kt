package com.mycompany.app.quick

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import ch.opumlabs.opumbrowser.R

/**
 * The start page's spacer row (`QuickAdapter` item kind 9) with the home strip drawn along its lower edge, right
 * above the shortcuts (or along its upper edge when the shortcuts sit at the top): a "News" chip that opens the news
 * page, the most recent bookmarks (favicon + name), and "All bookmarks". The rest of the spacer stays empty on
 * purpose: the bars and the shortcuts sit at the bottom so they are in thumb reach on large phones.
 */
class QuickHomeHeader(context: Context) : QuickRowView(context) {
    class Book(val id: Long, val path: String, val title: String, val icon: Bitmap?)

    /** Strip at the bottom edge (shortcuts at the bottom) or the top edge. */
    var atBottom by mutableStateOf(true)
    var showNews by mutableStateOf(false)
    var books by mutableStateOf<List<Book>>(emptyList())
    var hasBooks by mutableStateOf(false)
    var dark by mutableStateOf(false)
    /** Spacer height in px; the strip is left out when it does not fit. */
    var heightPx by mutableIntStateOf(0)
    var onNews: () -> Unit = {}
    var onBook: (Book) -> Unit = {}
    var onAllBooks: () -> Unit = {}

    val stripShown get() = showNews || hasBooks

    @Composable
    override fun Content() {
        val density = resources.displayMetrics.density
        if (!stripShown || heightPx < (STRIP_DP * density).toInt()) return
        Box(Modifier.fillMaxSize()) {
            Strip(Modifier.align(if (atBottom) Alignment.BottomStart else Alignment.TopStart))
        }
    }

    @Composable
    private fun Strip(modifier: Modifier) {
        val text = if (dark) Color(0xFFF0F0F0) else Color(0xFF1B1B1B)
        val chip = if (dark) Color(0x24FFFFFF) else Color(0x0F000000)
        // centred while it fits (owner's request); scrolls sideways when the bookmarks make it wider than the screen
        androidx.compose.foundation.layout.BoxWithConstraints(modifier.fillMaxWidth().height(STRIP_DP.dp)) {
        val full = maxWidth
        Row(
            Modifier.fillMaxHeight().horizontalScroll(rememberScrollState()).widthIn(min = full)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            if (showNews) {
                Chip(stringResource(R.string.news_title), chip, text, bold = true, onClick = onNews) {
                    Image(
                        painterResource(if (dark) R.drawable.outline_newspaper_dark_24 else R.drawable.outline_newspaper_black_24),
                        null, Modifier.size(18.dp),
                    )
                }
            }
            if (showNews && hasBooks) Canvas(Modifier.size(1.dp, 20.dp)) { drawRect(text.copy(alpha = 0.2f)) }
            for (book in books) {
                Chip(book.title, chip, text, onClick = { onBook(book) }) { BookIcon(book, text) }
            }
            if (hasBooks) {
                Chip(stringResource(R.string.home_all_bookmarks), chip, text, onClick = onAllBooks) {
                    Image(
                        painterResource(if (dark) R.drawable.outline_kid_star_dark_24 else R.drawable.outline_kid_star_black_24),
                        null, Modifier.size(18.dp),
                    )
                }
            }
        }
        }
    }

    @Composable
    private fun Chip(
        label: String, back: Color, text: Color, bold: Boolean = false, onClick: () -> Unit,
        icon: @Composable () -> Unit,
    ) {
        Row(
            Modifier.height(36.dp).clip(RoundedCornerShape(18.dp)).background(back).clickable(onClick = onClick)
                .padding(start = 10.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            icon()
            Spacer(Modifier.size(8.dp))
            Text(
                label,
                style = TextStyle(color = text, fontSize = 14.sp, fontWeight = if (bold) FontWeight.Medium else FontWeight.Normal),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 140.dp),
            )
        }
    }

    @Composable
    private fun BookIcon(book: Book, text: Color) {
        val icon = book.icon
        if (icon != null && !icon.isRecycled) {
            Image(icon.asImageBitmap(), null, Modifier.size(18.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
        } else {
            val letter = book.title.trim().firstOrNull()?.uppercase() ?: "•"
            Box(Modifier.size(18.dp).clip(RoundedCornerShape(9.dp)).background(text.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Text(letter, style = TextStyle(color = text, fontSize = 11.sp, fontWeight = FontWeight.Medium))
            }
        }
    }

    companion object {
        /** Height of the strip (dp). */
        const val STRIP_DP = 52
        /** Bookmarks shown before "All bookmarks". */
        const val BOOK_COUNT = 5
    }
}
