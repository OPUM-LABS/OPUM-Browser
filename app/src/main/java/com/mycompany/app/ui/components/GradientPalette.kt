package com.mycompany.app.ui.components

import androidx.compose.foundation.Canvas
import com.mycompany.app.ui.state.AppState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * Colour bar of the image editor's text dialog (replaces `view/MyPaletteView`, 2026-11-17). The gradient runs through
 * [colors] at [stops]; the square marker sits at [position] (0..1 along the bar). Tapping the bar or dragging along it
 * picks the colour under the finger and reports it with its position; a mostly vertical drag is left alone.
 * Sizes, colours and the mirrored bar in rtl mode are those of the old view.
 */
@Composable
fun GradientPalette(
    colors: IntArray,
    stops: FloatArray,
    position: Float,
    onPick: (color: Int, position: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val rtl = remember { MainUtil.isRtl(context) }
    val inset = MainApp.dp16.toFloat()                         // side inset and top of the bar
    val barHeight = inset * 2f
    val thumbStroke = with(density) { 6.dp.toPx() }.roundToInt().toFloat()
    val innerStroke = thumbStroke - (MainApp.dp4 / 2f).roundToInt()
    val border = Color(if (AppState.darkUi) 0xFF3F3F3F.toInt() else 0xFF000000.toInt())
    val pick by rememberUpdatedState(onPick)
    var dragging by remember { mutableStateOf(false) }
    var barWidth by remember { mutableFloatStateOf(0f) }

    fun thumbRect(pos: Float): Rect {
        val half = (thumbStroke / 2f).toInt()
        val left = ((pos * barWidth) - (inset / 2f).toInt() + half + inset).roundToInt().toFloat()
        val top = inset - thumbStroke + half
        return Rect(left, top, left + inset - thumbStroke, top + barHeight + thumbStroke * 2f - thumbStroke)
    }

    fun colorAt(positionValue: Float): Int {
        val widthPx = barWidth.toInt()
        val px = positionValue.coerceIn(0f, (widthPx - 1).toFloat()).roundToInt()
        if (px == 0) return colors[0]
        if (px == widthPx - 1) return colors[colors.size - 1]
        return gradientColor(colors, stops, px / barWidth)
    }

    Canvas(
        modifier
            .fillMaxWidth()
            .height(with(density) { (inset * 2f + barHeight).toDp() })
            .onSizeChanged { barWidth = it.width - inset * 2f }
            .graphicsLayer { if (rtl) rotationY = 180f }
            .systemGestureExclusion { thumbRect(position) }
            .pointerInput(colors, stops) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    val start = down.position
                    var decided = false
                    var vertical = false
                    var active = false
                    fun apply(offset: Offset) {
                        if (barWidth <= 0f) return
                        if (!active && offset.x >= inset && offset.x < inset + barWidth && offset.y >= inset && offset.y < inset + barHeight) {
                            active = true
                            dragging = true
                        }
                        if (active) {
                            val positionValue = (offset.x - inset).coerceIn(0f, barWidth - 1f)
                            pick(colorAt(positionValue), positionValue / barWidth)
                        }
                    }
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) {
                            if (!vertical) apply(change.position)
                            break
                        }
                        if (vertical) continue
                        if (!decided && hypot(change.position.x - start.x, change.position.y - start.y) > MainApp.dp10) {
                            decided = true
                            if (abs(change.position.y - start.y) > abs(change.position.x - start.x)) {
                                vertical = true
                                continue
                            }
                        }
                        if (decided) {
                            change.consume()
                            apply(change.position)
                        }
                    }
                    dragging = false
                }
            },
    ) {
        if (barWidth <= 0f) return@Canvas
        val brush = Brush.horizontalGradient(
            colorStops = Array(colors.size) { stops[it] to Color(colors[it]) },
            startX = inset,
            endX = inset + barWidth,
        )
        drawRect(brush, Offset(inset, inset), Size(barWidth, barHeight))
        drawRect(border, Offset(inset, inset), Size(barWidth, barHeight), style = Stroke(2f))
        val r = thumbRect(position)
        drawRect(Color.Black, r.topLeft, r.size, style = Stroke(thumbStroke))
        drawRect(if (dragging) Color(0xFFE0E0E0.toInt()) else Color.White, r.topLeft, r.size, style = Stroke(innerStroke))
    }
}

/** Colour of a linear gradient through [colors] at [stops], at [position] (0..1), interpolated per ARGB channel. */
private fun gradientColor(colors: IntArray, stops: FloatArray, position: Float): Int {
    var index = 0
    while (index < stops.size - 2 && position > stops[index + 1]) index++
    val span = (stops[index + 1] - stops[index]).takeIf { it > 0f } ?: return colors[index + 1]
    val fraction = ((position - stops[index]) / span).coerceIn(0f, 1f)
    val a = colors[index]
    val next = colors[index + 1]
    fun ch(shift: Int) = (((a shr shift) and 255) + (((next shr shift) and 255) - ((a shr shift) and 255)) * fraction).roundToInt()
    return (ch(24) shl 24) or (ch(16) shl 16) or (ch(8) shl 8) or ch(0)
}
