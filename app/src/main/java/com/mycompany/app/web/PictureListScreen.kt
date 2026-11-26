package com.mycompany.app.web

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import kotlinx.coroutines.launch
import com.mycompany.app.ui.components.Sym

/**
 * What the shared "Image list" screen needs from its dialog ([WebGridDialog] and the gallery-site pickers). State is read
 * through Compose state inside the dialog, so reading these properties from the composition subscribes to changes.
 */
internal interface PictureHost {
    val adapter: WebGridAdapter
    /** Bumped whenever the model changes (selection, list, resolved addresses). */
    val version: Int
    val loaded: Boolean
    val barVisible: Boolean
    /** 0..1, or a negative value for an indeterminate bar. */
    val progress: Float
    val filtering: Boolean
    /** The one-line status above the buttons (gallery sites: "n / m" resolved), or null. */
    val statusText: String?
    val thumbs: SnapshotStateMap<Int, Bitmap>
    val failed: SnapshotStateMap<Int, Boolean>
    fun requestThumb(position: Int)
    fun badge(url: String?): String
    fun onBack()
    fun onFilter()
    fun onRefresh()
    fun onSelectAll()
    fun onTileTap(position: Int)
    fun onTileLong(position: Int)
    fun onAction(which: Int)
    fun onStatusClick()
}

@Composable
internal fun PictureListScreen(host: PictureHost) {
    host.version
    val adapter = host.adapter
    val loaded = host.loaded
    val select = adapter.selectMode
    val total = adapter.itemCount()
    val colors = MaterialTheme.colorScheme
    Surface(Modifier.fillMaxSize(), color = colors.surface, contentColor = colors.onSurface) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().statusBarsPadding().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { host.onBack() }) { Icon(Sym.ArrowBack, contentDescription = null) }
                Text(
                    if (select) MainUtil.countLabel(adapter.checkedCount, total) else stringResource(R.string.image_list),
                    style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f), maxLines = 1,
                )
                if (select) {
                    IconButton(onClick = { host.onSelectAll() }, enabled = loaded) {
                        if (adapter.allChecked()) Icon(Sym.Check, contentDescription = null, tint = colors.primary)
                        else Icon(painterResource(R.drawable.outline_radio_button_unchecked_black_24), contentDescription = null)
                    }
                } else {
                    IconButton(onClick = { host.onFilter() }) { Icon(painterResource(R.drawable.outline_filter_list_black_24), contentDescription = null) }
                    IconButton(onClick = { host.onRefresh() }) { Icon(Sym.Refresh, contentDescription = null) }
                }
            }
            if (host.barVisible) {
                if (host.progress < 0f) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                else LinearProgressIndicator(progress = { host.progress }, modifier = Modifier.fillMaxWidth())
            }
            Box(Modifier.weight(1f).fillMaxWidth().background(colors.surfaceContainer)) {
                if (loaded && total == 0) {
                    Text(stringResource(R.string.no_image), modifier = Modifier.align(Alignment.Center), color = colors.onSurfaceVariant)
                }
                PictureGrid(host, total, select, loaded)
                if (host.filtering) {
                    Box(Modifier.fillMaxSize().background(colors.scrim.copy(alpha = 0.2f)).clickable(enabled = false) {}, contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            host.statusText?.let {
                Text(
                    it, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth().clickable { host.onStatusClick() }.padding(vertical = 12.dp),
                )
            }
            Row(Modifier.fillMaxWidth().navigationBarsPadding().height(56.dp)) {
                val enabled = loaded && (if (select) adapter.checkedCount > 0 else total > 0)
                TextButton(onClick = { host.onAction(0) }, enabled = enabled, modifier = Modifier.weight(1f).fillMaxSize()) { Text(stringResource(R.string.download)) }
                TextButton(onClick = { host.onAction(1) }, enabled = enabled, modifier = Modifier.weight(1f).fillMaxSize()) { Text(stringResource(R.string.zip)) }
                TextButton(onClick = { host.onAction(2) }, enabled = enabled, modifier = Modifier.weight(1f).fillMaxSize()) { Text(stringResource(R.string.album)) }
            }
        }
    }
}

@Composable
private fun PictureGrid(host: PictureHost, total: Int, select: Boolean, loaded: Boolean) {
    val density = LocalDensity.current
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val widthPx = with(density) { maxWidth.toPx() }
        val gap = MainApp.dp4.toFloat()
        var cols = 3
        var tile = (widthPx - gap * 4) / 3
        while (tile > MainApp.dp140) {
            cols++
            tile = (widthPx - gap * (cols + 1)) / cols
        }
        val tileDp = with(density) { tile.toDp() }
        val gapDp = with(density) { gap.toDp() }
        LazyVerticalGrid(
            state = gridState, columns = GridCells.Fixed(cols), modifier = Modifier.fillMaxSize(),
            userScrollEnabled = loaded,
            horizontalArrangement = Arrangement.spacedBy(gapDp), verticalArrangement = Arrangement.spacedBy(gapDp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(gapDp),
        ) {
            items(total) { position -> PictureTile(host, position, tileDp, select) }
            if (total > 20) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        Modifier.fillMaxWidth().height(64.dp).clickable { scope.launch { gridState.scrollToItem(0) } },
                        contentAlignment = Alignment.Center,
                    ) { Icon(painterResource(R.drawable.outline_list_footer_black_24), contentDescription = null) }
                }
            }
        }
    }
}

@Composable
private fun PictureTile(host: PictureHost, position: Int, size: Dp, select: Boolean) {
    host.version
    val adapter = host.adapter
    val colors = MaterialTheme.colorScheme
    val address = if (adapter.validIndex(position)) adapter.imageUrls!!.get(position) else null
    // keyed on "missing" too: a refresh (showList) clears the thumbnails for the same items, which must load again
    val missing = host.thumbs[position] == null && host.failed[position] != true
    LaunchedEffect(position, address, missing) { if (missing) host.requestThumb(position) }
    val selected = select && adapter.checked?.getOrNull(position) == true
    val bitmap = host.thumbs[position]
    Box(
        Modifier.size(size).clip(RoundedCornerShape(8.dp)).background(colors.surface)
            .pointerInput(position) { detectTapGestures(onTap = { host.onTileTap(position) }, onLongPress = { host.onTileLong(position) }) },
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(bitmap.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(4.dp))
        } else if (address.isNullOrEmpty() || host.failed[position] == true) {
            Icon(painterResource(R.drawable.outline_error_black_web_24), contentDescription = null, tint = colors.onSurfaceVariant)
        } else {
            Icon(painterResource(R.drawable.outline_image_black_web_24), contentDescription = null, tint = colors.onSurfaceVariant)
        }
        Surface(
            shape = RoundedCornerShape(6.dp), color = colors.surfaceContainerHighest,
            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
        ) { Text(host.badge(address), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)) }
        if (select) {
            if (selected) Box(Modifier.fillMaxSize().background(Color(0x71000000)))
            if (selected) {
                Icon(Sym.Check, contentDescription = null, tint = colors.primary, modifier = Modifier.align(Alignment.TopStart).padding(4.dp))
            } else {
                Icon(painterResource(R.drawable.outline_radio_button_unchecked_black_24), contentDescription = null, tint = colors.onSurfaceVariant, modifier = Modifier.align(Alignment.TopStart).padding(4.dp))
            }
            Surface(shape = RoundedCornerShape(6.dp), color = colors.surfaceContainerHighest, modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)) {
                Text(MainUtil.countLabel(position + 1, adapter.itemCount()), style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
    }
}
