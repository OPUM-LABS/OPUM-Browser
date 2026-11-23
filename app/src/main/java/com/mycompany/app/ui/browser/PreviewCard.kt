package com.mycompany.app.ui.browser

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.MainSettings
import com.mycompany.app.pref.store.PdfSettings
import kotlinx.coroutines.launch

/**
 * True when the main address bar is at the top of the screen (it is in the top bar and the top bar is not moved to the
 * bottom): the previews put their bar on the same side (2026-11-20).
 */
fun previewBarOnTop(): Boolean {
    val top = MainUtil.parseIntList(MainSettings.topItems.value)
    return top != null && 1 in top && !PdfSettings.topToBottom.value
}

/**
 * The floating card of the quick previews (link preview, image preview; 2026-11-20): a dimmed screen, a card 10 dp from
 * the edges with a 44 dp gap on the far side (the page behind stays visible), rounded 24 dp with a 1.5 dp accent
 * outline, and the [bar] on the main bar's side ([barOnTop]) with a drag handle. Dragging the bar down closes it
 * ([onClosed] after the slide-out; > 48 dp or a flick); a tap on the dimmed area calls [onOutside].
 */
@Composable
fun PreviewCard(
    barOnTop: Boolean,
    onClosed: () -> Unit,
    onOutside: () -> Unit,
    bar: @Composable ColumnScope.() -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val drag = remember { Animatable(0f) }
    // little room below a bottom bar: a short drag or a flick closes it
    val closePx = with(density) { 48.dp.toPx() }
    val none = remember { MutableInteractionSource() }
    val fade = (1f - drag.value / (closePx * 6f)).coerceIn(0f, 1f)
    fun settle(velocity: Float) {
        scope.launch {
            if (drag.value > closePx || velocity > 1000f) {
                drag.animateTo(drag.value + closePx * 10f, tween(160))
                onClosed()
            } else {
                drag.animateTo(0f, spring())
            }
        }
    }
    val dragModifier = Modifier.draggable(
        state = rememberDraggableState { d -> scope.launch { drag.snapTo((drag.value + d).coerceAtLeast(0f)) } },
        orientation = Orientation.Vertical,
        onDragStopped = { v -> settle(v) },
    )
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.45f * fade))
            .clickable(interactionSource = none, indication = null) { onOutside() }
            .statusBarsPadding().navigationBarsPadding().imePadding()
            .padding(start = 10.dp, end = 10.dp, top = if (barOnTop) 10.dp else 44.dp, bottom = if (barOnTop) 44.dp else 10.dp),
    ) {
        val outline = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
        Surface(
            Modifier.fillMaxSize()
                .graphicsLayer { translationY = drag.value; alpha = 0.4f + 0.6f * fade }
                .clickable(interactionSource = none, indication = null) { }
                .drawWithContent {
                    drawContent()
                    val w = 1.5.dp.toPx()
                    drawRoundRect(
                        outline, topLeft = Offset(w / 2, w / 2), size = Size(size.width - w, size.height - w),
                        cornerRadius = CornerRadius(24.dp.toPx()), style = Stroke(width = w),
                    )
                },
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            shadowElevation = 12.dp,
        ) {
            Column(Modifier.fillMaxSize()) {
                if (barOnTop) Bar(dragModifier, bar)
                Box(Modifier.weight(1f).fillMaxWidth(), content = content)
                if (!barOnTop) Bar(dragModifier, bar)
            }
        }
    }
}

@Composable
private fun Bar(dragModifier: Modifier, bar: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().then(dragModifier).background(MaterialTheme.colorScheme.surfaceContainer)) {
        Box(Modifier.fillMaxWidth().padding(top = 6.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(width = 36.dp, height = 4.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
        }
        bar()
    }
}
