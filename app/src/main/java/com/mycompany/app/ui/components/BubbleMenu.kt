package com.mycompany.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.theme.OpumDimens

/**
 * OPUM's one menu (2026-11-20): a rounded card with a small tail pointing at the button it belongs
 * to, the Compose twin of the address-bar menu (`MyPopupMenu` with `bubble`). Put it in a `Box` around the button, like
 * a `DropdownMenu`: it opens below the button, or above when there is more room there, and sits under the button's end
 * when the button is in the right half of the screen. Rows: [BubbleMenuItem].
 */
@Composable
fun BubbleMenu(expanded: Boolean, onDismiss: () -> Unit, color: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified, content: @Composable ColumnScope.() -> Unit) {
    if (!expanded) return
    val density = LocalDensity.current
    val tail = with(density) { 8.dp.toPx() }
    var tailX by remember { mutableFloatStateOf(-1f) }
    var below by remember { mutableStateOf(true) }
    // the window the button lives in: Compose passes the popup a placeholder window size (200000 × 200000 seen on the
    // emulator), so the left / right and up / down decisions use the real root view; Android would otherwise shift the
    // popup to fit afterwards and the tail would point at the wrong place
    val root = LocalView.current.rootView
    // the visible part of the window: between the status bar (and a cut-out) and the navigation bar
    fun safeTop(): Int = androidx.core.view.ViewCompat.getRootWindowInsets(root)
        ?.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars() or androidx.core.view.WindowInsetsCompat.Type.displayCutout())?.top ?: 0
    fun safeBottom(): Int = androidx.core.view.ViewCompat.getRootWindowInsets(root)
        ?.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars() or androidx.core.view.WindowInsetsCompat.Type.ime())?.bottom ?: 0
    // a long menu next to a low button (8 entries in landscape) was taller than the screen: it is capped to the
    // visible height and scrolls (the popup itself is measured against the placeholder window, so it has to be told)
    val maxHeight = with(density) { ((root.height - safeTop() - safeBottom()).coerceAtLeast(0) - 40.dp.roundToPx()).coerceAtLeast(160.dp.roundToPx()).toDp() }
    val provider = remember(density, root) {
        object : PopupPositionProvider {
            override fun calculatePosition(anchorBounds: IntRect, windowSize: IntSize, layoutDirection: LayoutDirection, popupContentSize: IntSize): IntOffset {
                val width = root.width.takeIf { it > 0 } ?: windowSize.width
                val height = root.height.takeIf { it > 0 } ?: windowSize.height
                val margin = with(density) { 8.dp.roundToPx() }
                val gap = with(density) { 2.dp.roundToPx() }
                val top = safeTop() + margin
                val bottom = height - safeBottom() - margin
                val spaceBelow = bottom - anchorBounds.bottom
                val spaceAbove = anchorBounds.top - top
                val goBelow = spaceBelow >= popupContentSize.height + gap || spaceBelow >= spaceAbove
                val centre = anchorBounds.center.x
                var x = if (centre > width / 2) anchorBounds.right - popupContentSize.width else anchorBounds.left
                x = x.coerceIn(margin, (width - popupContentSize.width - margin).coerceAtLeast(margin))
                var y = if (goBelow) anchorBounds.bottom + gap else anchorBounds.top - popupContentSize.height - gap
                y = y.coerceIn(top, (bottom - popupContentSize.height).coerceAtLeast(top))
                below = goBelow
                tailX = (centre - x).toFloat()
                return IntOffset(x, y)
            }
        }
    }
    Popup(popupPositionProvider = provider, onDismissRequest = onDismiss, properties = PopupProperties(focusable = true)) {
        val radius = with(density) { OpumDimens.popupCorner.toPx() }
        Surface(
            shape = BubbleShape(tailX, below, tail, radius),
            // [color]: a lighter tone where the menu opens over a surface of the same colour (the reader's top bar)
            color = if (color != androidx.compose.ui.graphics.Color.Unspecified) color else MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 8.dp,
            // invisible for the first frame, until the position (and so the tail) is known
            modifier = Modifier.alpha(if (tailX < 0f) 0f else 1f),
        ) {
            val pad = with(density) { tail.toDp() }
            Column(
                Modifier.padding(top = if (below) pad + 6.dp else 6.dp, bottom = if (below) 6.dp else pad + 6.dp)
                    .width(IntrinsicSize.Max).widthIn(min = 200.dp, max = 320.dp)
                    .heightIn(max = maxHeight)
                    .verticalScroll(rememberScrollState()),
            ) { content() }
        }
    }
}

/** One menu row: optional [icon] (any 24 dp drawable, tinted), [text], a check mark when [checked]; red when [destructive]. */
@Composable
fun BubbleMenuItem(text: String, onClick: () -> Unit, icon: Int? = null, checked: Boolean = false, destructive: Boolean = false, enabled: Boolean = true) {
    val colour = when {
        destructive -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
    Row(
        Modifier.fillMaxWidth().heightIn(min = OpumDimens.menuRow).clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp).alpha(if (enabled) 1f else 0.38f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(painterResource(icon), null, Modifier.size(20.dp), tint = if (destructive) colour else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(14.dp))
        }
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = colour, maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (checked) {
            Spacer(Modifier.width(12.dp))
            Icon(painterResource(R.drawable.outline_check_black_24), null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

/** Rounded card with a triangular tail on the top ([below]) or bottom edge, centred at [tailX] (clamped to the straight part). */
private class BubbleShape(private val tailX: Float, private val below: Boolean, private val tail: Float, private val radius: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val top = if (below) tail else 0f
        val bottom = if (below) size.height else size.height - tail
        val path = Path()
        path.addRoundRect(RoundRect(0f, top, size.width, bottom, CornerRadius(radius, radius)))
        if (tailX >= 0f) {
            val x = tailX.coerceIn(radius + tail, (size.width - radius - tail).coerceAtLeast(radius + tail))
            val tip = Path()
            if (below) {
                tip.moveTo(x - tail, top + 1f); tip.lineTo(x, 0f); tip.lineTo(x + tail, top + 1f)
            } else {
                tip.moveTo(x - tail, bottom - 1f); tip.lineTo(x, size.height); tip.lineTo(x + tail, bottom - 1f)
            }
            tip.close()
            path.addPath(tip)
        }
        return Outline.Generic(path)
    }
}

/** The icon of a menu entry inside a [BubbleMenu] (a Material `DropdownMenuItem`'s `leadingIcon`), 20 dp like [BubbleMenuItem]. */
@Composable
fun MenuIcon(res: Int) {
    Icon(painterResource(res), null, Modifier.size(20.dp))
}

/** Red text and icon for a deleting `DropdownMenuItem`, like a destructive [BubbleMenuItem]. */
@Composable
fun destructiveItemColors(): androidx.compose.material3.MenuItemColors = androidx.compose.material3.MenuDefaults.itemColors(
    textColor = MaterialTheme.colorScheme.error,
    leadingIconColor = MaterialTheme.colorScheme.error,
)
