package com.mycompany.app.dialog

import androidx.compose.material3.Icon
import android.content.Context
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.MainSettings
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogMenuMain.DownMenuListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.browser.ComposeOverlay
import com.mycompany.app.view.MyBarView
import com.mycompany.app.view.MyWebBody
import com.mycompany.app.web.WebViewActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.mycompany.app.ui.components.Sym

/**
 * The "small list" style of the main menu: a popup that grows out of the menu button, showing the customised items as one
 * list under the same header as [DialogMenuMain] (reset, blocked count, list style, settings), with the optional quick
 * bar at the bottom. Compose now: a full-size [ComposeOverlay] over the page (scrim; tap outside closes) holding the card
 * at the position the old popup computed. Constructor, fields the activity touches and the methods it calls are as
 * before: [startClose] starts the close animation (returns false if it cannot), [release] releases the popup and tells the
 * listener, [applyBlocked] updates the blocked count. [showBelow] opens the card from the anchor downwards, [alignEnd]
 * picks the side of the anchor.
 */
class DialogMenuList(
    @JvmField var hostActivity: WebViewActivity?,
    @JvmField var appContext: Context?,
    @JvmField var webBody: MyWebBody?,
    @JvmField var anchorView: View?,
    @JvmField var topIds: IntArray?,
    @JvmField var bottomIds: IntArray?,
    @JvmField var showBelow: Boolean,
    @JvmField var alignEnd: Boolean,
    @JvmField var blockedStart: Int,
    @JvmField var menuListener: DownMenuListener?,
) {
    private var overlay: ComposeOverlay? = null
    private var closing = false
    private var blocked by mutableIntStateOf(blockedStart)
    private var pivotX = 0
    private var pivotY = 0
    private var close by mutableStateOf(false)
    /** The "All actions" page is shown instead of the menu's own items. */
    private var showAll by mutableStateOf(false)

    private class Host(context: Context) : ComposeOverlay(context) {
        fun set(content: @Composable () -> Unit) = setPage({ AppState.darkUi }, content)
    }

    init {
        val web = webBody
        if (web != null) web.post { show() }
    }

    /** Where the card sits (margins inside the full-size overlay) and the point it grows from; same geometry as before. */
    private fun place(): IntArray {
        val anchor = anchorView!!
        val web = webBody!!
        val where = IntArray(2)
        web.getLocationOnScreen(where)
        val left = web.paddingLeft + where[0]
        val top = web.paddingTop + where[1]
        val rectWidth = web.rectWidth
        val rectHeight = web.rectHeight
        anchor.getLocationOnScreen(where)
        val anchorX = anchor.width / 2 + (where[0] - left)
        val anchorY = anchor.height / 2 + (where[1] - top)
        var width = dpToPx(appContext, if ((bottomIds?.size ?: 0) < 9) 244.0f else 284.0f).toInt()
        var start: Int
        if (alignEnd) {
            start = anchorX - width
            if (start < 0) start = 0 else width = anchorX
            if (width > rectWidth) width = rectWidth
        } else {
            val end = anchorX + width
            if (end > rectWidth) { start = rectWidth - width; width = rectWidth } else { width = end; start = anchorX }
            if (start < 0) start = 0
        }
        val topMargin: Int
        val bottomEdge: Int
        if (showBelow) { bottomEdge = rectHeight; topMargin = anchorY } else { bottomEdge = if (anchorY > rectHeight) rectHeight else anchorY; topMargin = 0 }
        pivotX = anchorX - start
        pivotY = anchorY - topMargin
        return intArrayOf(start, topMargin, rectWidth - width, rectHeight - bottomEdge)
    }

    private fun show() {
        val web = webBody
        val context = hostActivity
        if (web == null || context == null || anchorView == null || overlay != null) return
        try {
            val margins = place()
            val host = Host(context)
            overlay = host
            host.set { Menu(margins) }
            web.addView(host, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        } catch (_: Exception) {
            release()
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Menu(margins: IntArray) {
        val dark = AppState.darkUi
        val density = LocalDensity.current
        val progress = remember { Animatable(0f) }
        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) { progress.animateTo(1f, tween(200)) }
        LaunchedEffect(close) {
            if (close) {
                progress.animateTo(0f, tween(200))
                delay(1)
                release()
            }
        }
        Box(
            Modifier.fillMaxSize().clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { startClose() },
        ) {
            with(density) {
                Box(
                    Modifier.padding(start = margins[0].toDp(), top = margins[1].toDp(), end = margins[2].toDp(), bottom = margins[3].toDp()).fillMaxSize(),
                    contentAlignment = if (showBelow) Alignment.TopStart else Alignment.BottomStart,
                ) {
                    Surface(
                        Modifier.fillMaxWidth().heightIn(max = 460.dp)
                            .graphicsLayer {
                                scaleX = progress.value; scaleY = progress.value; alpha = progress.value
                                transformOrigin = TransformOrigin(
                                    if (size.width > 0) pivotX / size.width else 0f,
                                    if (size.height > 0) pivotY / size.height else 0f,
                                )
                            }
                            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { },
                        shape = RoundedCornerShape(16.dp), tonalElevation = 3.dp, shadowElevation = 8.dp,
                    ) {
                        Column {
                            val startPage = hostActivity?.onStartPage == true
                            // top row: Web Filter (blocked count) · the two buttons chosen in the editor · Settings
                            com.mycompany.app.ui.browser.MenuHeaderRow(
                                com.mycompany.app.ui.browser.MenuCatalog.headSlots(com.mycompany.app.pref.store.MainSettings.headItems.value),
                                blocked, dark, startPage, Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
                                onFilter = { menuListener?.onDownloads() }, onSettings = { menuListener?.onSettings() },
                                onItem = { id -> menuListener?.onItem(overlay, id) },
                                onLong = { menuListener?.onLongPress() },
                            )
                            HorizontalDivider()
                            if (showAll) {
                                Row(Modifier.fillMaxWidth().clickable { showAll = false }.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(com.mycompany.app.ui.components.Sym.ArrowBack, null)
                                    Text(stringResource(R.string.all_actions), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp))
                                }
                                com.mycompany.app.ui.browser.ActionCatalog(
                                    onPick = { id -> menuListener?.onItem(overlay, id) }, onStartPage = startPage, maxHeight = 320.dp,
                                )
                            } else {
                                val ids = (com.mycompany.app.ui.browser.MenuCatalog.shown(topIds, startPage) ?: IntArray(0)) + com.mycompany.app.ui.browser.MenuCatalog.ALL
                                LazyColumn(Modifier.weight(1f, fill = false)) {
                                    items(ids.toList()) { id ->
                                        Row(
                                            Modifier.fillMaxWidth().combinedClickable(
                                                onClick = { if (id == com.mycompany.app.ui.browser.MenuCatalog.ALL) showAll = true else menuListener?.onItem(overlay, id) },
                                                onLongClick = { menuListener?.onLongPress() },
                                            ).padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            com.mycompany.app.ui.browser.MenuActionIcon(id, 24.dp, live = true)
                                            Text(stringResource(com.mycompany.app.ui.browser.MenuCatalog.labelRes(id)), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 16.dp))
                                        }
                                    }
                                }
                            }
                            val bottom = com.mycompany.app.ui.browser.MenuCatalog.shown(bottomIds, false)
                            if (!showAll && bottom != null && bottom.isNotEmpty()) {
                                HorizontalDivider()
                                com.mycompany.app.ui.browser.MenuQuickRow(bottom, onTap = { id -> menuListener?.onItem(overlay, id) }, onLong = { menuListener?.onLongPress() })
                            }
                        }
                    }
                }
            }
        }
    }

    private fun context(): Context = appContext ?: hostActivity!!

    /** Starts the close animation; false when there is nothing to close or it is already closing. */
    fun startClose(): Boolean {
        if (overlay == null || closing) return false
        closing = true
        close = true
        return true
    }

    /** Releases the popup and tells the listener (once). */
    fun release() {
        if (appContext == null) return
        overlay?.let { (it.parent as? ViewGroup)?.removeView(it) }
        overlay = null
        val listener = menuListener
        menuListener = null
        listener?.onClose()
        hostActivity = null; appContext = null; anchorView = null; topIds = null; bottomIds = null
    }

    /** New number of blocked items for the shield. */
    fun applyBlocked(newBlocked: Int) { blocked = newBlocked }
}
