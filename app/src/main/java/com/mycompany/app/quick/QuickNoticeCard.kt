package com.mycompany.app.quick

import android.content.Context
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.mutableStateOf
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.view.MyProgressBar
import com.mycompany.app.ui.components.Sym

/**
 * The notice rows of the new-tab page (`QuickAdapter` item types 3–5), drawn by Compose:
 *  - [IMPORT] (3): the "Import" button under the shortcuts;
 *  - [NEWS_START] (4): the "Check out news" card with a close button and a "Show news" button;
 *  - [NEWS_HEAD] (5): the news header with a sources button and a settings button;
 *  - [NEWS_LOADING] (6): "News loading…" / "No news" with the loading bar ([bar], the shared `MyProgressBar`, which
 *    `QuickView` drives directly); its frame runs past the top so only the bottom corners are round.
 * The card frame repeats the old `MyRoundFrame`: 12dp corners below [topPad] (the bars' height), an outline when
 * [cardColor] is the light outline grey (−2434342), otherwise a fill; the header's frame runs past its bottom so
 * only the top corners are round (the article cards continue it).
 */
class QuickNoticeCard(context: Context, private val cardKind: Int) : QuickRowView(context) {
    var topPad by mutableIntStateOf(0)
    var cardColor by mutableIntStateOf(0)
    var textColor by mutableIntStateOf(-16777216)
    /** Import button fill, and the pressed colour of all buttons. */
    var buttonColor by mutableIntStateOf(0)
    var pressColor by mutableIntStateOf(-2039584)
    var closeIcon by mutableIntStateOf(0)
    var sourcesIcon by mutableIntStateOf(0)
    var settingsIcon by mutableIntStateOf(0)
    var onMain: () -> Unit = {}
    var onClose: () -> Unit = {}
    var onSources: () -> Unit = {}
    var onSettings: () -> Unit = {}
    /** Kind 5 on the news page: the masthead (back arrow, serif title, date, double rule) instead of the old row. */
    var pageMode by mutableStateOf(false)
    var dark by mutableStateOf(false)
    var dateText by mutableStateOf("")
    /** The masthead's title ("News", "RSS"). */
    var mastTitle by mutableStateOf("")
    /** The News page's tabs (Top stories, My feeds, each feed), the selected one, and the "+" (add a feed). */
    var tabs by mutableStateOf<List<String>>(emptyList())
    var selectedTab by mutableIntStateOf(0)
    var onTab: (Int) -> Unit = {}
    var onAddFeed: () -> Unit = {}
    var onBack: () -> Unit = {}
    /** Kind 6: the message and whether the bar shows. */
    var message by mutableStateOf("")
    var barShown by mutableStateOf(false)
    /** Kind 6: the loading bar (a View; its progress is driven from outside as before). */
    val bar: MyProgressBar? = if (cardKind == NEWS_LOADING) MyProgressBar(context).also { it.setHeader(true) } else null

    @Composable
    override fun Content() {
        when (cardKind) {
            IMPORT -> ImportRow()
            NEWS_START -> NewsStart()
            NEWS_LOADING -> NewsLoading()
            else -> NewsHead()
        }
    }

    @Composable
    private fun ImportRow() {
        Box(Modifier.fillMaxSize().padding(start = 20.dp, top = 20.dp, end = 20.dp)) {
            Button(
                onClick = onMain,
                modifier = Modifier.fillMaxWidth().heightIn(min = 40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(buttonColor), contentColor = Color(textColor)),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                elevation = null,
            ) {
                Text(stringResource(R.string.import_normal), fontSize = 14.sp)
            }
        }
    }

    @Composable
    private fun NewsStart() {
        val top = with(LocalDensity.current) { topPad.toDp() }
        Box(Modifier.fillMaxSize()) {
            Frame(roundBottom = true)
            Box(Modifier.fillMaxSize().padding(top = top)) {
                Column(
                    Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        stringResource(R.string.news_start),
                        style = TextStyle(color = Color(textColor), fontSize = 16.sp, textAlign = TextAlign.Center),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedButton(
                        onClick = onMain,
                        modifier = Modifier.padding(top = 24.dp).fillMaxWidth().heightIn(min = 40.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(textColor)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(textColor)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(stringResource(R.string.news_show), fontSize = 14.sp)
                    }
                }
                CardButton(closeIcon, onClose, Modifier.align(Alignment.TopEnd))
            }
        }
    }

    @Composable
    private fun Masthead() {
        val ink = if (dark) Color(0xFFF2EFE8) else Color(0xFF1A1A1A)
        val muted = ink.copy(alpha = 0.6f)
        Column(Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
            Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    androidx.compose.material3.Icon(
                        Sym.ArrowBack,
                        stringResource(R.string.abc_action_bar_up_description), tint = ink,
                    )
                }
                Text(
                    mastTitle.ifEmpty { stringResource(R.string.news_title) },
                    style = TextStyle(
                        color = ink, fontSize = 30.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    ),
                    maxLines = 1,
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                )
                if (settingsIcon != 0) CardButton(settingsIcon, onSettings, Modifier)
            }
            Text(
                dateText.uppercase(),
                style = TextStyle(color = muted, fontSize = 11.sp, letterSpacing = 1.2.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                maxLines = 1,
                modifier = Modifier.padding(start = 8.dp, top = 2.dp),
            )
            // the newspaper's double rule under the masthead
            Canvas(Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 10.dp).height(6.dp)) {
                val thick = 2.dp.toPx()
                val thin = 1.dp.toPx()
                drawRect(ink, Offset(0f, 0f), Size(size.width, thick))
                drawRect(ink, Offset(0f, size.height - thin), Size(size.width, thin))
            }
            Tabs(ink)
        }
    }

    /** The sections of the News page as pills; the selected one filled with the ink colour. */
    @Composable
    private fun Tabs(ink: Color) {
        val list = tabs
        androidx.compose.foundation.lazy.LazyRow(
            Modifier.fillMaxWidth().padding(top = 12.dp).height(40.dp),
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(list.size) { index ->
                val on = index == selectedTab
                Box(
                    Modifier.height(34.dp).clip(RoundedCornerShape(17.dp))
                        .then(if (on) Modifier.background(ink) else Modifier.border(1.dp, ink.copy(alpha = 0.35f), RoundedCornerShape(17.dp)))
                        .clickable { onTab(index) }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        list[index],
                        style = TextStyle(
                            color = if (on) (if (dark) Color(0xFF1A1A1A) else Color(0xFFF8F5EE)) else ink,
                            fontSize = 14.sp,
                            fontWeight = if (on) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Normal,
                        ),
                        maxLines = 1,
                    )
                }
            }
            item {
                Box(
                    Modifier.size(34.dp).clip(RoundedCornerShape(17.dp)).border(1.dp, ink.copy(alpha = 0.35f), RoundedCornerShape(17.dp))
                        .clickable { onAddFeed() },
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.material3.Icon(
                        com.mycompany.app.ui.components.Sym.Add, stringResource(R.string.rss_feeds), tint = ink,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }

    @Composable
    private fun NewsHead() {
        if (pageMode) {
            Masthead()
            return
        }
        val top = with(LocalDensity.current) { topPad.toDp() }
        Box(Modifier.fillMaxSize()) {
            Frame(roundBottom = false)
            Row(
                Modifier.fillMaxWidth().padding(top = top).height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                Text(
                    stringResource(R.string.news_title),
                    style = TextStyle(color = Color(textColor), fontSize = 16.sp),
                    maxLines = 1,
                    modifier = Modifier.weight(1f).padding(start = 8.dp, end = 8.dp),
                )
                if (sourcesIcon != 0) CardButton(sourcesIcon, onSources, Modifier)
                CardButton(settingsIcon, onSettings, Modifier)
            }
        }
    }

    @Composable
    private fun NewsLoading() {
        Box(Modifier.fillMaxSize()) {
            Frame(roundBottom = true, roundTop = false)
            Column(
                Modifier.align(Alignment.Center).padding(bottom = 32.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    message,
                    style = TextStyle(color = Color(textColor), fontSize = 16.sp, textAlign = TextAlign.Center),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                )
                val barNow = bar
                if (barNow != null) {
                    AndroidView(
                        factory = { barNow },
                        // hidden = no space at all (the old bar was GONE), so the message centres alone
                        modifier = Modifier.padding(start = 72.dp, top = if (barShown) 32.dp else 0.dp, end = 72.dp)
                            .fillMaxWidth().height(if (barShown) 2.dp else 0.dp),
                    )
                }
            }
        }
    }

    /** 48dp icon button, icon at 70 % like the old buttons' maximum alpha. */
    @Composable
    private fun CardButton(icon: Int, onClick: () -> Unit, modifier: Modifier) {
        IconButton(
            onClick = onClick,
            modifier = modifier.size(48.dp),
            colors = IconButtonDefaults.iconButtonColors(contentColor = Color(pressColor)),
        ) {
            if (icon != 0) Image(painterResource(icon), null, Modifier.alpha(0.7f))
        }
    }

    @Composable
    private fun Frame(roundBottom: Boolean, roundTop: Boolean = true) {
        val color = cardColor
        if (color == 0) return
        val top = topPad.toFloat()
        Canvas(Modifier.fillMaxSize()) {
            val r = 12.dp.toPx()
            if (roundBottom && color == OUTLINE_GREY) {
                val s = 1.dp.toPx()
                drawRoundRect(
                    Color(color), Offset(s / 2, top + s / 2), Size(size.width - s, size.height - top - s),
                    CornerRadius(r), style = Stroke(s),
                )
            } else {
                // the header's frame runs past the bottom (the loading notice's past the top), hiding those corners
                val bottom = if (roundBottom) size.height else size.height + r
                val from = if (roundTop) top else -r
                drawRoundRect(Color(color), Offset(0f, from), Size(size.width, bottom - from), CornerRadius(r))
            }
        }
    }

    companion object {
        const val IMPORT = 3
        const val NEWS_START = 4
        const val NEWS_HEAD = 5
        const val NEWS_LOADING = 6
        /** The light outline grey: the news-start card draws an outline instead of a fill. */
        const val OUTLINE_GREY = -2434342
    }
}
