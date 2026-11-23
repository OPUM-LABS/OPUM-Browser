package com.mycompany.app.ui.components

import android.graphics.RectF
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.mycompany.app.main.MainApp
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * The small overview of a zoomed full-page capture (replaces `view/MyAreaView`, 2026-11-17): a dimmed copy of the
 * page's outline with the visible part marked. It shows only while the page is larger than the screen ([rect], the
 * displayed image rect from the zoom attacher, is wider or taller than [viewWidth] x [viewHeight]). The size follows
 * the page's aspect ([imageWidth] x [imageHeight]), at most 160 dp, at least 32 dp wide (the old "full mode"), and it
 * fades in when the size changes. [offset] is the distance from the top-left corner of the image area, in px.
 */
@Composable
fun AreaOverview(
    viewWidth: Int,
    viewHeight: Int,
    imageWidth: Int,
    imageHeight: Int,
    rect: RectF?,
    offset: Int,
    modifier: Modifier = Modifier,
) {
    if (viewWidth <= 0 || viewHeight <= 0 || imageWidth <= 0 || imageHeight <= 0 || rect == null) return
    if (rect.width().toInt() <= viewWidth && rect.height().toInt() <= viewHeight) return
    val density = LocalDensity.current
    val max = with(density) { 160.dp.toPx() }.toInt()
    val line = with(density) { 0.5.dp.toPx() }
    val margin = MainApp.dp8
    val aspect = imageHeight.toFloat() / imageWidth
    val roomH = viewHeight - offset - margin
    var w = min(max, viewWidth - offset - margin)
    var h = (w * aspect).roundToInt()
    if (h > min(max, roomH)) {
        h = min(max, roomH)
        w = (h / aspect).roundToInt()
    }
    val least = MainApp.dp16 shl 1
    if (w < least) {
        w = least
        h = (w * aspect).roundToInt()
        if (h > roomH) {
            w = (roomH / aspect).roundToInt()
            h = roomH
        }
    }
    if (w <= 0 || h <= 0) return
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(w, h) {
        alpha.snapTo(0f)
        alpha.animateTo(1f, tween(160, easing = LinearEasing))
    }
    Canvas(modifier.size(with(density) { w.toDp() }, with(density) { h.toDp() }).graphicsLayer { this.alpha = alpha.value }) {
        val sx = size.width / rect.width()
        val sy = size.height / rect.height()
        val left = -rect.left * sx + line
        val top = -rect.top * sy + line
        val right = viewWidth * sx + left - line
        val bottom = viewHeight * sy + top - line
        val stroke = Stroke(line * 2f)
        drawRect(Color(0xA1000000.toInt()))
        drawRect(Color(0xA1FFFFFF.toInt()), Offset(left, top), Size(right - left, bottom - top))
        drawRect(Color.White, Offset(left, top), Size(right - left, bottom - top), style = stroke)
        drawRect(Color.White, Offset(line, line), Size(size.width - line * 2f, size.height - line * 2f), style = stroke)
    }
}
