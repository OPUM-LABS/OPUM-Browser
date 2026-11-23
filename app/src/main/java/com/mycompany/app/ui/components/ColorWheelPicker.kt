package com.mycompany.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.sin

/** Quick picks shown under the wheel. */
private val PRESETS = intArrayOf(
    0xFFE53935.toInt(), 0xFFD81B60.toInt(), 0xFF8E24AA.toInt(), 0xFF3F51B5.toInt(),
    0xFF1E88E5.toInt(), 0xFF00897B.toInt(), 0xFF43A047.toInt(), 0xFFFB8C00.toInt(),
)

/**
 * Free colour choice: a colour wheel (angle = hue, distance from the centre = saturation, the rim is fully saturated)
 * and a brightness slider under it, plus a row of quick picks. [onChange] runs while dragging (for a live preview),
 * [onCommit] when a drag or tap ends. [color] is ARGB; alpha is ignored.
 */
@Composable
fun ColorWheelPicker(
    color: Int,
    onChange: (Int) -> Unit,
    onCommit: (Int) -> Unit,
    wheelDescription: String,
    brightnessLabel: String,
    modifier: Modifier = Modifier,
) {
    // hue 0..360, saturation 0..1, value (brightness) 0..1
    var hsv by remember { mutableStateOf(toHsv(color)) }
    var dragging by remember { mutableStateOf(false) }
    LaunchedEffect(color) { if (!dragging && toArgb(hsv) != (color or 0xFF000000.toInt())) hsv = toHsv(color) }
    val change by rememberUpdatedState(onChange)
    val commit by rememberUpdatedState(onCommit)

    fun update(h: Float = hsv[0], s: Float = hsv[1], v: Float = hsv[2], done: Boolean) {
        hsv = floatArrayOf(h, s.coerceIn(0f, 1f), v.coerceIn(0f, 1f))
        val argb = toArgb(hsv)
        if (done) commit(argb) else change(argb)
    }

    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        // wheel
        Box(
            Modifier
                .widthIn(max = 260.dp)
                .fillMaxWidth()
                .aspectRatio(1f)
                .semantics { contentDescription = wheelDescription },
        ) {
            fun pick(size: androidx.compose.ui.unit.IntSize, p: Offset, done: Boolean) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = min(cx, cy)
                val dx = p.x - cx
                val dy = p.y - cy
                var angle = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat()
                if (angle < 0) angle += 360f
                update(h = angle, s = hypot(dx, dy) / radius, done = done)
            }
            Canvas(
                Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures { pick(size, it, true) } }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { dragging = true; pick(size, it, false) },
                            onDragEnd = { dragging = false; update(done = true) },
                            onDragCancel = { dragging = false; update(done = true) },
                            onDrag = { change, _ -> change.consume(); pick(size, change.position, false) },
                        )
                    },
            ) {
                val radius = min(size.width, size.height) / 2f
                val centre = Offset(size.width / 2f, size.height / 2f)
                val side = min(size.width, size.height).toInt()
                val wheel = wheelBitmap(side)
                drawImage(wheel, Offset(centre.x - side / 2f, centre.y - side / 2f))
                drawCircle(Color.Black.copy(alpha = 1f - hsv[2]), radius - 0.5f, centre)
                // thumb
                val a = Math.toRadians(hsv[0].toDouble())
                val r = hsv[1] * radius
                val thumb = Offset(centre.x + (cos(a) * r).toFloat(), centre.y + (sin(a) * r).toFloat())
                val t = 14.dp.toPx()
                drawCircle(Color(toArgb(hsv)), t, thumb)
                drawCircle(Color.White, t, thumb, style = Stroke(3.dp.toPx()))
                drawCircle(Color.Black.copy(alpha = 0.35f), t + 1.5.dp.toPx(), thumb, style = Stroke(1.dp.toPx()))
            }
        }
        // brightness slider
        Text(
            brightnessLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 6.dp),
        )
        Canvas(
            Modifier
                .fillMaxWidth()
                .height(36.dp)
                .semantics { contentDescription = brightnessLabel }
                .pointerInput(Unit) { detectTapGestures { update(v = it.x / size.width, done = true) } }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { dragging = true; update(v = it.x / size.width, done = false) },
                        onDragEnd = { dragging = false; update(done = true) },
                        onDragCancel = { dragging = false; update(done = true) },
                        onDrag = { change, _ -> change.consume(); update(v = change.position.x / size.width, done = false) },
                    )
                },
        ) {
            val h = 20.dp.toPx()
            val top = (size.height - h) / 2f
            drawRoundRect(
                Brush.horizontalGradient(listOf(Color.Black, Color.hsv(hsv[0], hsv[1], 1f))),
                topLeft = Offset(0f, top),
                size = androidx.compose.ui.geometry.Size(size.width, h),
                cornerRadius = CornerRadius(h / 2f, h / 2f),
            )
            val x = (hsv[2] * size.width).coerceIn(h / 2f, size.width - h / 2f)
            val c = Offset(x, size.height / 2f)
            drawCircle(Color(toArgb(hsv)), 13.dp.toPx(), c)
            drawCircle(Color.White, 13.dp.toPx(), c, style = Stroke(3.dp.toPx()))
            drawCircle(Color.Black.copy(alpha = 0.35f), 14.5.dp.toPx(), c, style = Stroke(1.dp.toPx()))
        }
        // quick picks
        Row(
            Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val current = toArgb(hsv)
            for (preset in PRESETS) {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(preset))
                        .clickable { hsv = toHsv(preset); commit(preset) }
                        .border(
                            if (preset == current) 3.dp else 1.dp,
                            if (preset == current) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant,
                            CircleShape,
                        ),
                )
            }
        }
    }
}

/** The last wheel drawn (hue around, saturation outwards, full brightness), reused while its size stays the same. */
private var wheelCache: androidx.compose.ui.graphics.ImageBitmap? = null

/**
 * The colour wheel as a bitmap, computed per pixel (a sweep gradient leaves a visible seam), with an anti-aliased rim.
 */
private fun wheelBitmap(side: Int): androidx.compose.ui.graphics.ImageBitmap {
    wheelCache?.let { if (it.width == side) return it }
    val pixels = IntArray(side * side)
    val c = side / 2f
    val radius = c
    val hsv = FloatArray(3)
    hsv[2] = 1f
    for (y in 0 until side) {
        for (x in 0 until side) {
            val dx = x + 0.5f - c
            val dy = y + 0.5f - c
            val d = hypot(dx, dy)
            if (d > radius) continue
            var angle = Math.toDegrees(atan2(dy, dx).toDouble()).toFloat()
            if (angle < 0) angle += 360f
            hsv[0] = angle
            hsv[1] = (d / radius).coerceAtMost(1f)
            val alpha = ((radius - d).coerceIn(0f, 1f) * 255f).toInt()
            pixels[y * side + x] = (toArgb(hsv) and 0x00FFFFFF) or (alpha shl 24)
        }
    }
    val bitmap = android.graphics.Bitmap.createBitmap(pixels, side, side, android.graphics.Bitmap.Config.ARGB_8888)
    return bitmap.asImageBitmap().also { wheelCache = it }
}

/** ARGB → [hue 0..360, saturation 0..1, value 0..1]. */
internal fun toHsv(argb: Int): FloatArray {
    val r = (argb shr 16 and 255) / 255f
    val g = (argb shr 8 and 255) / 255f
    val b = (argb and 255) / 255f
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val d = max - min
    var h = when {
        d == 0f -> 0f
        max == r -> 60f * (((g - b) / d) % 6f)
        max == g -> 60f * ((b - r) / d + 2f)
        else -> 60f * ((r - g) / d + 4f)
    }
    if (h < 0) h += 360f
    val s = if (max == 0f) 0f else d / max
    return floatArrayOf(h, s, max)
}

/** [hue, saturation, value] → opaque ARGB. */
internal fun toArgb(hsv: FloatArray): Int {
    val h = ((hsv[0] % 360f) + 360f) % 360f
    val s = hsv[1].coerceIn(0f, 1f)
    val v = hsv[2].coerceIn(0f, 1f)
    val c = v * s
    val x = c * (1 - kotlin.math.abs((h / 60f) % 2f - 1))
    val m = v - c
    val (r, g, b) = when {
        h < 60 -> Triple(c, x, 0f)
        h < 120 -> Triple(x, c, 0f)
        h < 180 -> Triple(0f, c, x)
        h < 240 -> Triple(0f, x, c)
        h < 300 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    fun ch(f: Float) = ((f + m) * 255f + 0.5f).toInt().coerceIn(0, 255)
    return (0xFF shl 24) or (ch(r) shl 16) or (ch(g) shl 8) or ch(b)
}
