package com.mycompany.app.dialog

import android.content.Context
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.mycompany.app.ui.browser.ComposeOverlay

/**
 * Stand-in for the reader's old `MyButtonImage` buttons: the reader engine keeps calling the same methods
 * (`setImageResource`, `setNoti`, `setLoad`, `setLoading`, `post` ...), which now only update Compose state; the bars in
 * [ReaderBar] draw it. Pressed / normal colours are ignored (Material 3 decides).
 */
class ReadBtn(private val host: View) {
    var visible by mutableStateOf(true)
    var res by mutableIntStateOf(0)
    var badge by mutableStateOf(false)
    var loading by mutableStateOf(false)
    var click: View.OnClickListener? = null

    fun setVisibility(newVisible: Int) { visible = newVisible == View.VISIBLE }
    fun setImageResource(newRes: Int) { res = newRes }
    fun setBgNorColor(color: Int) {}
    fun setBgPreColor(press: Int) {}
    fun setOnClickListener(newClick: View.OnClickListener?) { click = newClick }
    fun setNoti(newBadge: Boolean) { badge = newBadge }
    fun setLoad(newLoading: Boolean) { loading = newLoading }
    fun setLoading(color: Int, newLoading: Boolean) { loading = newLoading }

    /** Old: cancels the press animations, true when one was running. There are none now. */
    fun isPressed(): Boolean = false

    /** Old: release. */
    fun clearClick() { click = null }

    fun post(runnable: Runnable): Boolean = host.post(runnable)

    fun tap(anchor: View) { click?.onClick(anchor) }
}

/**
 * One piece of the reader chrome (top bar, either bottom row, mini-window controls): a real view, so the engine's
 * visibility toggles, hit tests (`MainUtil.isInsideView`) and click-through blocking keep working, that shows a Compose page.
 */
class ReaderBar(context: Context) : ComposeOverlay(context) {
    init {
        // like the old bars: a tap on the empty part of a bar must not reach the page behind it
        setOnClickListener { }
    }

    fun content(dark: () -> Boolean, page: @Composable () -> Unit) = setPage(dark, page)
}

@Composable
private fun pxDp(px: Int): Dp = with(LocalDensity.current) { px.toDp() }

@Composable
private fun BtnIcon(btn: ReadBtn) {
    if (btn.loading) {
        CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
    } else if (btn.res != 0) {
        Icon(painterResource(btn.res), contentDescription = null)
    }
}

@Composable
internal fun ReaderIconButton(btn: ReadBtn, anchor: View, modifier: Modifier = Modifier) {
    if (!btn.visible) return
    IconButton(onClick = { btn.tap(anchor) }, modifier = modifier) {
        if (btn.badge) {
            BadgedBox(badge = { Badge() }) { BtnIcon(btn) }
        } else {
            BtnIcon(btn)
        }
    }
}

/** One entry of the two top-bar menus; [checked] null = no check box. */
internal class ReaderMenuItem(val id: Int, val label: String?, val labelRes: Int = 0, val checked: Boolean? = null)

@Composable
internal fun ReaderMenu(items: List<ReaderMenuItem>?, onDismiss: () -> Unit, onPick: (Int) -> Unit) {
    com.mycompany.app.ui.components.BubbleMenu(expanded = items != null, onDismiss = onDismiss) {
        items?.forEach { item ->
            DropdownMenuItem(
                text = { Text(item.label ?: stringResource(item.labelRes)) },
                onClick = { onPick(item.id) },
                trailingIcon = item.checked?.let { checked2 -> { Checkbox(checked = checked2, onCheckedChange = null) } },
            )
        }
    }
}

/**
 * Top bar: back on the start side; mini window, open (quick items only), save (menu), reload and more (menu) on the
 * end side. [topPx] is the old top margin (room for the status bar).
 */
@Composable
internal fun ReaderTopBar(
    anchor: View,
    topPx: Int,
    startPx: Int,
    back: ReadBtn,
    mini: ReadBtn,
    open: ReadBtn,
    save: ReadBtn,
    reload: ReadBtn,
    more: ReadBtn,
    saveMenu: List<ReaderMenuItem>?,
    moreMenu: List<ReaderMenuItem>?,
    onMenuDismiss: (Boolean) -> Unit,
    onMenuPick: (Boolean, Int) -> Unit,
) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            Modifier.fillMaxWidth().padding(top = pxDp(topPx), start = (pxDp(startPx) - 12.dp).coerceAtLeast(0.dp), end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ReaderIconButton(back, anchor)
            Spacer(Modifier.weight(1f))
            ReaderIconButton(mini, anchor)
            ReaderIconButton(open, anchor)
            Box {
                ReaderIconButton(save, anchor)
                ReaderMenu(saveMenu, { onMenuDismiss(false) }) { onMenuPick(false, it) }
            }
            ReaderIconButton(reload, anchor)
            Box {
                ReaderIconButton(more, anchor)
                ReaderMenu(moreMenu, { onMenuDismiss(true) }) { onMenuPick(true, it) }
            }
        }
    }
}

/** A bottom row of five evenly spaced tonal buttons (tools row, or the read-aloud row). */
@Composable
internal fun ReaderBottomRow(anchor: View, buttons: List<ReadBtn>) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            buttons.forEach { btn ->
                if (btn.visible) {
                    FilledTonalIconButton(onClick = { btn.tap(anchor) }, modifier = Modifier.size(48.dp)) { BtnIcon(btn) }
                }
            }
        }
    }
}

/** Mini-window controls: full screen / close on top, rewind / play-pause / forward below. */
@Composable
internal fun ReaderMiniControls(anchor: View, full: ReadBtn, close: ReadBtn, rew: ReadBtn, play: ReadBtn, fwd: ReadBtn) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceContainer) {
        Box(Modifier.fillMaxSize().padding(horizontal = 4.dp)) {
            ReaderIconButton(full, anchor, Modifier.align(Alignment.TopStart))
            ReaderIconButton(close, anchor, Modifier.align(Alignment.TopEnd))
            ReaderIconButton(rew, anchor, Modifier.align(Alignment.BottomStart))
            ReaderIconButton(play, anchor, Modifier.align(Alignment.BottomCenter))
            ReaderIconButton(fwd, anchor, Modifier.align(Alignment.BottomEnd))
        }
    }
}
