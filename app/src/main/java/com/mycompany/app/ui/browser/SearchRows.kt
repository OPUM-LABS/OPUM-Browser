package com.mycompany.app.ui.browser

import android.graphics.Bitmap
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp

/** What one row of the address editor's suggestion list shows (the adapter fills it from its `SearchItem`). */
data class SuggestionRowData(
    /** 0 suggestion / history / bookmark / quick link, 1 section label, 2 "find in page". */
    val rowKind: Int,
    /** The item's source type (37 = recent search, 0 = search suggestion, 32 = quick link, ...). */
    val type: Int,
    val title: String?,
    val url: String?,
    /** The text typed in the editor; matches inside [title] and [url] are highlighted. */
    val query: String?,
    val urlStart: Int,
    val icon: Bitmap?,
    /** Colour of the letter placeholder (quick links), 0 for the default. */
    val letterColor: Int,
    val dark: Boolean,
    /** True when the list is reversed (bar at the bottom): the fill arrow points down-left. */
    val reversed: Boolean,
    /** 0 = square, 1 = rounded top corners, 2 = rounded bottom corners (outer row of the list). */
    val corner: Int,
    /** Built-in engine ([com.mycompany.app.main.MainConst.engineUrls] index) whose icon marks its search suggestions; -1 none. */
    val engine: Int = -1,
)

/** Highlights the first match of [query] at or after [from] in the accent colour and bold (the old list: bold / white). */
fun highlight(title: String?, query: String?, from: Int, accent: Color): AnnotatedString {
    val value = title ?: return AnnotatedString("")
    if (query.isNullOrEmpty()) return AnnotatedString(value)
    return try {
        val lower = value.lowercase()
        val at = if (from < lower.length) lower.indexOf(query, from) else -1
        if (from < lower.length && at >= 0 && at < lower.length) {
            val end = minOf(at + query.length, lower.length)
            buildAnnotatedString {
                append(value.substring(0, at))
                withStyle(SpanStyle(color = accent, fontWeight = FontWeight.Bold)) { append(value.substring(at, end)) }
                append(value.substring(end))
            }
        } else AnnotatedString(value)
    } catch (_: Exception) {
        AnnotatedString(value)
    }
}

@Composable
private fun px(value: Int): Dp = with(LocalDensity.current) { value.toDp() }

/**
 * A one-colour glyph tinted for the current theme (the light / dark / colour themes), instead of picking the black or
 * the white drawable by a flag, which left dark glyphs on dark themes.
 */
@Composable
private fun ThemedIcon(res: Int, modifier: Modifier = Modifier) =
    Icon(painterResource(res), contentDescription = null, modifier = modifier, tint = MaterialTheme.colorScheme.onSurfaceVariant)

/** One row of the suggestion list: suggestion, section label or "find in page". */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SuggestionRow(
    data: SuggestionRowData,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    onFill: () -> Unit,
) {
    val radius = px(MainApp.dp16)
    val shape = when (data.corner) {
        1 -> RoundedCornerShape(topStart = radius, topEnd = radius)
        2 -> RoundedCornerShape(bottomStart = radius, bottomEnd = radius)
        else -> RoundedCornerShape(0.dp)
    }
    val rowHeight = px(MainApp.dp56)
    val container = MaterialTheme.colorScheme.surfaceContainerLow
    when (data.rowKind) {
        1 -> Box(
            Modifier.fillMaxWidth().clip(shape).background(container).heightIn(min = rowHeight)
                .combinedClickable(onClick = onClick),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                data.title ?: "", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        2 -> Row(
            Modifier.fillMaxWidth().clip(shape).background(container).heightIn(min = rowHeight).combinedClickable(onClick = onClick)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ThemedIcon(R.drawable.outline_find_in_page_black_24, Modifier.size(24.dp))
            Text(stringResource(R.string.find_word), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 16.dp))
        }
        else -> Row(
            Modifier.fillMaxWidth().clip(shape).background(container).heightIn(min = rowHeight)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RowIcon(data)
            Column(Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 6.dp)) {
                Text(
                    highlight(data.title, data.query, 0, MaterialTheme.colorScheme.primary), style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface, maxLines = if (data.url.isNullOrEmpty()) 2 else 1, overflow = TextOverflow.Ellipsis,
                )
                if (!data.url.isNullOrEmpty()) {
                    Text(
                        highlight(data.url, data.query, data.urlStart, MaterialTheme.colorScheme.primary), style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            if (data.type == 37) {
                IconButton(onClick = onDelete, modifier = Modifier.size(width = px(MainApp.dp48), height = rowHeight)) {
                    ThemedIcon(R.drawable.outline_close_black_18)
                }
            }
            IconButton(onClick = onFill, modifier = Modifier.size(width = px(MainApp.dp48), height = rowHeight)) {
                ThemedIcon(if (data.reversed) R.drawable.outline_south_west_black_20 else R.drawable.outline_north_west_black_20)
            }
        }
    }
}

/** The 24 dp icon of a suggestion: history glyph, the engine's icon, the site's favicon (also for a site the engine suggests), or a letter placeholder. */
@Composable
private fun RowIcon(data: SuggestionRowData) {
    val size = 24.dp
    when {
        data.type == 37 -> ThemedIcon(R.drawable.outline_history_black_24, Modifier.size(size))
        // a search suggestion: the engine's own icon (Google / DuckDuckGo), else the search glyph
        data.type == 0 && data.url.isNullOrEmpty() ->
            if (data.engine >= 0) com.mycompany.app.ui.components.EngineLogo(data.engine, size) { ThemedIcon(R.drawable.outline_search_black_24, Modifier.size(size)) }
            else ThemedIcon(R.drawable.outline_search_black_24, Modifier.size(size))
        data.icon != null -> Image(
            data.icon.asImageBitmap(), null, Modifier.size(size).clip(CircleShape), contentScale = ContentScale.Crop,
        )
        !data.title.isNullOrEmpty() -> {
            val glyph = data.title.firstOrNull { it.isLetterOrDigit() }?.uppercaseChar()?.toString()
            val tint = if (data.letterColor != 0 && data.letterColor != -460552) Color(data.letterColor) else MaterialTheme.colorScheme.surfaceVariant
            Box(Modifier.size(size).clip(CircleShape).background(tint), contentAlignment = Alignment.Center) {
                if (glyph != null) Text(glyph, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
                else ThemedIcon(R.drawable.outline_public_black_24, Modifier.size(16.dp))
            }
        }
        else -> ThemedIcon(R.drawable.outline_public_black_24, Modifier.size(size))
    }
}

/**
 * Prepares a [androidx.compose.ui.platform.ComposeView] to live inside one of the app's legacy `RecyclerView`s (the prebuilt
 * `androidx.recyclerview` copy). Compose installs its default composition strategy in the view's constructor, which
 * stores a real-androidx `PoolingContainerListenerHolder` under the view tag `pooling_container_listener_holder_tag`; the
 * legacy RecyclerView reads that same tag as its own (legacy) holder class and fails with a ClassCastException when it
 * releases its children (for example when the adapter is replaced). So: use a strategy that does not need the pooling
 * container and drop the stale holder.
 */
fun androidx.compose.ui.platform.ComposeView.prepareForLegacyRecycler() {
    setViewCompositionStrategy(androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnDetachedFromWindow)
    val tag = context.resources.getIdentifier("pooling_container_listener_holder_tag", "id", context.packageName)
    if (tag != 0) setTag(tag, null)
}
