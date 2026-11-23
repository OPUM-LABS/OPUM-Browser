package com.mycompany.app.lock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.lock.LockComposeActivity
import com.mycompany.app.ui.lock.LockLayout
import com.mycompany.app.ui.lock.LockSecondaryButton
import kotlin.math.abs

/**
 * Pattern lock: connect dots of a 3x3 grid. The pattern is stored as the dot numbers (row * 3 + column) in order,
 * the same format as before, so existing patterns keep working. A straight line over a skipped dot picks it up like
 * on Android's own lock. Setting a new pattern asks twice (Continue, then Apply). See [LockComposeActivity].
 */
class PatternActivity : LockComposeActivity() {

    @Composable
    override fun Content() {
        LockBack()
        androidx.compose.runtime.key(generation) { PatternScreen() }
    }

    @Composable
    private fun PatternScreen() {
        val path = remember { mutableStateListOf<Int>() }
        var drawing by remember { mutableStateOf(false) }
        var wrongShown by remember { mutableStateOf(false) }
        var confirming by remember { mutableStateOf(false) }   // second entry of a new pattern
        var drawn by remember { mutableStateOf(false) }        // a pattern was drawn in the current step
        var first by remember { mutableStateOf<String?>(null) }
        var message by remember { mutableStateOf<Int?>(if (isSetting) R.string.input else null) }

        fun reset() { path.clear() }

        fun onStart() {
            drawn = false
            reset()
            if (!isSetting && wrongShown) { wrongShown = false; message = null }
            if (isSetting && wrongShown) {
                wrongShown = false
                message = if (confirming) R.string.reinput else R.string.input
            }
        }

        fun onComplete(pattern: String) {
            if (pattern.isEmpty()) return
            if (isSetting) {
                drawn = true
                if (confirming) {
                    if (pattern != first) { wrongShown = true; message = R.string.wrong_input } else message = null
                } else {
                    first = pattern
                    message = R.string.input
                }
            } else if (pattern != expectedSecret()) {
                wrongShown = true
                message = R.string.wrong_input
            } else {
                unlocked()
            }
        }

        fun retry() {
            confirming = false; drawn = false; first = null; wrongShown = false; reset(); message = R.string.input
        }

        fun next() {
            reset()
            if (!confirming) {
                confirming = true; drawn = false
                message = R.string.reinput
            } else {
                storeSecret(1, first)
            }
        }

        LockLayout(message?.let { stringResource(it) }) {
            PatternGrid(
                path = path,
                drawing = drawing,
                error = wrongShown,
                onStart = { drawing = true; onStart() },
                onEnd = { drawing = false; onComplete(path.joinToString("")) },
            )
            if (isSetting) {
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { retry() }, enabled = first != null || drawn) { Text(stringResource(R.string.retry)) }
                    Button(onClick = { next() }, enabled = drawn && !wrongShown) {
                        Text(stringResource(if (confirming) R.string.apply else R.string.continue_input))
                    }
                }
            } else {
                LockSecondaryButton(secondaryLabel()) { onSecondary() }
            }
        }
    }
}

@Composable
private fun PatternGrid(
    path: MutableList<Int>,
    drawing: Boolean,
    error: Boolean,
    onStart: () -> Unit,
    onEnd: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    var finger by remember { mutableStateOf<Offset?>(null) }
    val normal = MaterialTheme.colorScheme.onSurfaceVariant
    val active = MaterialTheme.colorScheme.primary
    val bad = MaterialTheme.colorScheme.error
    val color = if (error) bad else active

    Canvas(
        Modifier
            .size(300.dp)
            .pointerInput(Unit) {
                val cell = size.width / 3f
                fun hit(offset: Offset): Int? {
                    val col = (offset.x / cell).toInt()
                    val row = (offset.y / cell).toInt()
                    if (col !in 0..2 || row !in 0..2) return null
                    val cx = (col + 0.5f) * cell
                    val cy = (row + 0.5f) * cell
                    // the touch area of a dot is a bit more than half of its cell
                    return if (abs(offset.x - cx) <= cell * 0.35f && abs(offset.y - cy) <= cell * 0.35f) row * 3 + col else null
                }
                fun add(index: Int) {
                    if (index in path) return
                    if (path.isNotEmpty()) {
                        val last = path.last()
                        val dr = index / 3 - last / 3
                        val dc = index % 3 - last % 3
                        var mr = last / 3
                        var mc = last % 3
                        if (abs(dr) == 2 && abs(dc) != 1) mr += if (dr > 0) 1 else -1
                        if (abs(dc) == 2 && abs(dr) != 1) mc += if (dc > 0) 1 else -1
                        val mid = mr * 3 + mc
                        if (mid != last && mid != index && mid !in path) path.add(mid)
                    }
                    path.add(index)
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                awaitEachGesture {
                    val down = awaitFirstDown()
                    onStart()
                    finger = down.position
                    hit(down.position)?.let { add(it) }
                    while (true) {
                        val change = awaitPointerEvent().changes.first()
                        if (!change.pressed) break
                        finger = change.position
                        hit(change.position)?.let { add(it) }
                        change.consume()
                    }
                    finger = null
                    onEnd()
                }
            },
    ) {
        val cell = size.width / 3f
        fun center(cellIndex: Int) = Offset((cellIndex % 3 + 0.5f) * cell, (cellIndex / 3 + 0.5f) * cell)
        val stroke = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
        for (selected2 in 1 until path.size) {
            drawLine(color, center(path[selected2 - 1]), center(path[selected2]), strokeWidth = stroke.width, cap = StrokeCap.Round)
        }
        val tip = finger
        if (drawing && tip != null && path.isNotEmpty()) {
            drawLine(color, center(path.last()), tip, strokeWidth = stroke.width, cap = StrokeCap.Round)
        }
        for (selected2 in 0 until 9) {
            val selected = selected2 in path
            drawCircle(if (selected) color else normal, radius = if (selected) 14.dp.toPx() else 8.dp.toPx(), center = center(selected2))
        }
    }
}
