package com.mycompany.app.dialog

import com.mycompany.app.ui.components.AppSlider
import android.content.Context
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.FloatSettings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.dialog.DialogEditorText.EditorSetListener
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefCore
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefFloat
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.ui.browser.ComposeBottomDialog
import kotlin.math.roundToInt

/**
 * Edit one of the app's overlay icons: transparency (slider with - / +), one of eight colours or any colour along the
 * palette strip, Reset and Apply. [type] picks which icon (and which preferences): 0 general, 1 TTS, 2 zoom, 3 YouTube pip,
 * 4 reader, 5 screen filter (no scrim, the filter tints the sheet itself, and [seekListener] is told every colour change so the page
 * behind can follow live), 6 tab outline, 7 news, 8 hand, 9 password, 10 translation, 11-13 floating buttons 1-3.
 * Compose now (on [ComposeBottomDialog]); same constructor and members, shows itself.
 */
class DialogEditIcon(mainActivity: android.app.Activity?, type: Int, editorSetListener: EditorSetListener?) : ComposeBottomDialog(mainActivity) {
    /** Live colour listener (the screen-filter dialog sets it after building this one). */
    @JvmField
    var seekListener: DialogSeekListener? = null

    private var listener: EditorSetListener? = editorSetListener
    private val type = type
    private val appContext: Context? = context

    /** Where one kind of icon keeps its values: the static fields, the preference keys and how to open the file. */
    private class Spec(
        val size: () -> Int, val color: () -> Int, val pos: () -> Float,
        val store: (Int, Int, Float, Int) -> Unit,
        val keys: Array<String>, val core: (Context?) -> PrefCore?,
    )

    private val spec: Spec = when (type) {
        1 -> Spec({ EditorSettings.ttsAlpha.value }, { EditorSettings.ttsColor.value }, { EditorSettings.ttsPos.value }, { a, c, p, d -> EditorSettings.editLocal { set(EditorSettings.ttsAlpha, a) }; EditorSettings.editLocal { set(EditorSettings.ttsColor, c) }; EditorSettings.editLocal { set(EditorSettings.ttsPos, p) }; PrefEditor.color = d }, arrayOf("mTtsAlpha", "mTtsColor", "mTtsPos")) { PrefEditor.get(it) }
        2 -> Spec({ EditorSettings.zoomAlpha.value }, { EditorSettings.zoomColor.value }, { EditorSettings.zoomPos.value }, { a, c, p, d -> EditorSettings.editLocal { set(EditorSettings.zoomAlpha, a) }; EditorSettings.editLocal { set(EditorSettings.zoomColor, c) }; EditorSettings.editLocal { set(EditorSettings.zoomPos, p) }; PrefEditor.zoomArgb = d }, arrayOf("mZoomAlpha", "mZoomColor", "mZoomPos")) { PrefEditor.get(it) }
        3 -> Spec({ EditorSettings.ypipAlpha.value }, { EditorSettings.ypipColor.value }, { EditorSettings.ypipPos.value }, { a, c, p, d -> EditorSettings.editLocal { set(EditorSettings.ypipAlpha, a) }; EditorSettings.editLocal { set(EditorSettings.ypipColor, c) }; EditorSettings.editLocal { set(EditorSettings.ypipPos, p) }; PrefEditor.ypipArgb = d }, arrayOf("mYpipAlpha", "mYpipColor", "mYpipPos")) { PrefEditor.get(it) }
        4 -> Spec({ ReadSettings.readAlpha.value }, { ReadSettings.readColor.value }, { ReadSettings.readPos.value }, { a, c, p, _ -> ReadSettings.editLocal { set(ReadSettings.readAlpha, a) }; ReadSettings.editLocal { set(ReadSettings.readColor, c) }; ReadSettings.editLocal { set(ReadSettings.readPos, p) } }, arrayOf("mReadAlpha", "mReadColor", "mReadPos")) { PrefRead.get(it, false) }
        5 -> Spec({ EditorSettings.scrFilAlpha.value }, { EditorSettings.scrFilColor.value }, { EditorSettings.scrFilPos.value }, { a, c, p, d -> EditorSettings.editLocal { set(EditorSettings.scrFilAlpha, a) }; EditorSettings.editLocal { set(EditorSettings.scrFilColor, c) }; EditorSettings.editLocal { set(EditorSettings.scrFilPos, p) }; PrefEditor.scrFilArgb = d }, arrayOf("mScrFilAlpha", "mScrFilColor", "mScrFilPos")) { PrefEditor.get(it) }
        6 -> Spec({ EditorSettings.gridAlpha.value }, { EditorSettings.gridColor.value }, { EditorSettings.gridPos.value }, { a, c, p, d -> EditorSettings.editLocal { set(EditorSettings.gridAlpha, a) }; EditorSettings.editLocal { set(EditorSettings.gridColor, c) }; EditorSettings.editLocal { set(EditorSettings.gridPos, p) }; PrefEditor.gridArgb = d }, arrayOf("mGridAlpha", "mGridColor", "mGridPos")) { PrefEditor.get(it) }
        7 -> Spec({ EditorSettings.newsAlpha.value }, { EditorSettings.newsColor.value }, { EditorSettings.newsPos.value }, { a, c, p, d -> EditorSettings.editLocal { set(EditorSettings.newsAlpha, a) }; EditorSettings.editLocal { set(EditorSettings.newsColor, c) }; EditorSettings.editLocal { set(EditorSettings.newsPos, p) }; PrefEditor.newsArgb = d }, arrayOf("mNewsAlpha", "mNewsColor", "mNewsPos")) { PrefEditor.get(it) }
        8 -> Spec({ EditorSettings.handAlpha.value }, { EditorSettings.handColor.value }, { EditorSettings.handPos.value }, { a, c, p, d -> EditorSettings.editLocal { set(EditorSettings.handAlpha, a) }; EditorSettings.editLocal { set(EditorSettings.handColor, c) }; EditorSettings.editLocal { set(EditorSettings.handPos, p) }; PrefEditor.handArgb = d }, arrayOf("mHandAlpha", "mHandColor", "mHandPos")) { PrefEditor.get(it) }
        9 -> Spec({ EditorSettings.passAlpha.value }, { EditorSettings.passColor.value }, { EditorSettings.passPos.value }, { a, c, p, d -> EditorSettings.editLocal { set(EditorSettings.passAlpha, a) }; EditorSettings.editLocal { set(EditorSettings.passColor, c) }; EditorSettings.editLocal { set(EditorSettings.passPos, p) }; PrefEditor.passArgb = d }, arrayOf("mPassAlpha", "mPassColor", "mPassPos")) { PrefEditor.get(it) }
        10 -> Spec({ EditorSettings.trnsAlpha.value }, { EditorSettings.trnsColor.value }, { EditorSettings.trnsPos.value }, { a, c, p, d -> EditorSettings.editLocal { set(EditorSettings.trnsAlpha, a) }; EditorSettings.editLocal { set(EditorSettings.trnsColor, c) }; EditorSettings.editLocal { set(EditorSettings.trnsPos, p) }; PrefEditor.trnsArgb = d }, arrayOf("mTrnsAlpha", "mTrnsColor", "mTrnsPos")) { PrefEditor.get(it) }
        11 -> Spec({ FloatSettings.flt1Alpha.value }, { FloatSettings.flt1Color.value }, { FloatSettings.flt1Pos.value }, { a, c, p, d -> FloatSettings.editLocal { set(FloatSettings.flt1Alpha, a) }; FloatSettings.editLocal { set(FloatSettings.flt1Color, c) }; FloatSettings.editLocal { set(FloatSettings.flt1Pos, p) }; PrefFloat.flt1Argb = d }, arrayOf("mFlt1Alpha", "mFlt1Color", "mFlt1Pos")) { PrefFloat.get(it) }
        12 -> Spec({ FloatSettings.flt2Alpha.value }, { FloatSettings.flt2Color.value }, { FloatSettings.flt2Pos.value }, { a, c, p, d -> FloatSettings.editLocal { set(FloatSettings.flt2Alpha, a) }; FloatSettings.editLocal { set(FloatSettings.flt2Color, c) }; FloatSettings.editLocal { set(FloatSettings.flt2Pos, p) }; PrefFloat.flt2Argb = d }, arrayOf("mFlt2Alpha", "mFlt2Color", "mFlt2Pos")) { PrefFloat.get(it) }
        13 -> Spec({ FloatSettings.flt3Alpha.value }, { FloatSettings.flt3Color.value }, { FloatSettings.flt3Pos.value }, { a, c, p, d -> FloatSettings.editLocal { set(FloatSettings.flt3Alpha, a) }; FloatSettings.editLocal { set(FloatSettings.flt3Color, c) }; FloatSettings.editLocal { set(FloatSettings.flt3Pos, p) }; PrefFloat.flt3Argb = d }, arrayOf("mFlt3Alpha", "mFlt3Color", "mFlt3Pos")) { PrefFloat.get(it) }
        else -> Spec({ EditorSettings.iconAlpha.value }, { EditorSettings.iconColor.value }, { EditorSettings.iconPos.value }, { a, c, p, d -> EditorSettings.editLocal { set(EditorSettings.iconAlpha, a) }; EditorSettings.editLocal { set(EditorSettings.iconColor, c) }; EditorSettings.editLocal { set(EditorSettings.iconPos, p) }; PrefEditor.iconArgb = d }, arrayOf("mIconAlpha", "mIconColor", "mIconPos")) { PrefEditor.get(it) }
    }

    private val filter = type == 5
    private val minSize = if (filter) 20 else 0
    private val maxSize = if (filter) 100 else 90
    private val defaultSize = if (filter) 60 else 0

    // transparency in percent, colour (opaque) and its place on the palette strip
    private var sizeValue by mutableIntStateOf(spec.size().let { if (it < minSize || it > maxSize) defaultSize else it })
    private var colorValue by mutableIntStateOf(spec.color())
    private var posValue by mutableFloatStateOf(spec.pos())
    private var confirmReset by mutableStateOf(false)

    private val swatches: IntArray = (if (type == 4) MainConst.gradientColorsAlt else MainConst.gradientColors).let { it.copyOf(minOf(8, it.size)) }
    private val stops: IntArray = if (type == 4) MainConst.gradientColorsAlt else MainConst.gradientColors
    private val poses: FloatArray = MainConst.presetPositions

    private fun derived(): Int = if (filter) MainUtil.alphaColor(colorValue, sizeValue) else PrefEditor.withAlpha(colorValue, sizeValue)

    override val scrimAlpha: Float get() = if (filter) 0f else 0.32f

    /** Colour at a place (0..1) of the palette strip: the gradient's stops blended in sRGB like the old bitmap strip. */
    private fun colourAt(posValue: Float): Int {
        if (stops.isEmpty() || stops.size != poses.size) return colorValue
        if (posValue <= poses.first()) return stops.first() or (0xFF shl 24)
        if (posValue >= poses.last()) return stops.last() or (0xFF shl 24)
        var k = 0
        while (k < poses.size - 2 && posValue >= poses[k + 1]) k++
        val span = (poses[k + 1] - poses[k]).takeIf { it > 0f } ?: 1f
        val t = ((posValue - poses[k]) / span).coerceIn(0f, 1f)
        fun mix(shift: Int): Int {
            val a = (stops[k] shr shift) and 0xFF
            val channel = (stops[k + 1] shr shift) and 0xFF
            return (a + (channel - a) * t).roundToInt().coerceIn(0, 255)
        }
        return (0xFF shl 24) or (mix(16) shl 16) or (mix(8) shl 8) or mix(0)
    }

    /** Apply ([persist]) stores the values and closes; a reset only forgets the saved ones so the defaults apply again. */
    private fun commit(persist: Boolean) {
        val size = sizeValue
        val color = colorValue
        val pos = posValue
        val changed = spec.size() != size || spec.color() != color || spec.pos().compareTo(pos) != 0
        if (changed) {
            val d = derived()
            spec.store(size, color, pos, d)
            val core = spec.core(appContext)
            if (core != null) {
                if (persist) {
                    core.putInt(size, spec.keys[0]); core.putInt(color, spec.keys[1]); core.putFloat(spec.keys[2], pos)
                } else {
                    spec.keys.forEach { core.removeKey(it) }
                }
                core.saveAsync()
            }
            listener?.onApply(if (filter) d else 0, null)
        }
        if (persist) dismiss()
    }

    private fun resetToDefaults() {
        val index = when { type == 4 -> 3; filter -> 7; else -> 5 }
        sizeValue = defaultSize
        colorValue = (if (type == 4) MainConst.gradientColorsAlt else MainConst.gradientColors).getOrElse(index) { colorValue }
        posValue = MainConst.presetPositions.getOrElse(index) { posValue }
        commit(false)
    }

    init {
        setSheet {
            LaunchedEffect(sizeValue, colorValue) { seekListener?.onValue(derived()) }
            val tint = if (filter) Color(derived()) else Color.Transparent
            Column2(tint)
        }
    }

    @androidx.compose.runtime.Composable
    private fun androidx.compose.foundation.layout.ColumnScope.Column2(tint: Color) {
        androidx.compose.foundation.layout.Column(
            Modifier.fillMaxWidth().drawWithContent { drawContent(); if (tint.alpha > 0f) drawRect(tint) },
        ) {
            if (!filter) {
                Box(Modifier.fillMaxWidth().padding(top = 20.dp), contentAlignment = Alignment.Center) {
                    Box(
                        Modifier.size(56.dp).clip(RoundedCornerShape(14.dp)).background(Color(derived()))
                            .border(BorderStroke(1.dp, Color.Gray), RoundedCornerShape(14.dp)),
                    )
                }
            }
            Column3()
        }
        if (confirmReset) {
            AlertDialog(
                onDismissRequest = { confirmReset = false },
                text = { Text(stringResource(AppR.string.reset_setting)) },
                confirmButton = { TextButton(colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = androidx.compose.material3.MaterialTheme.colorScheme.error), onClick = { confirmReset = false; resetToDefaults() }) { Text(stringResource(AppR.string.reset)) } },
                dismissButton = { TextButton(onClick = { confirmReset = false }) { Text(stringResource(AppR.string.cancel)) } },
            )
        }
    }

    @androidx.compose.runtime.Composable
    private fun Column3() {
        androidx.compose.foundation.layout.Column(Modifier.verticalScroll(rememberScrollState()).padding(top = 8.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(AppR.string.color_alpha), style = MaterialTheme.typography.bodyLarge)
                Text("$sizeValue%", style = MaterialTheme.typography.bodyLarge)
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (sizeValue > minSize) sizeValue-- }) { Text("\u2212", style = MaterialTheme.typography.titleLarge) }
                AppSlider(
                    value = sizeValue.toFloat(), onValueChange = { sizeValue = it.roundToInt().coerceIn(minSize, maxSize) },
                    valueRange = minSize.toFloat()..maxSize.toFloat(), modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { if (sizeValue < maxSize) sizeValue++ }) { Icon(Icons.Filled.Add, contentDescription = null) }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                swatches.forEachIndexed { index, argb ->
                    val selected = colorValue == argb
                    Box(
                        Modifier.size(36.dp).clip(CircleShape).background(Color(argb))
                            .border(BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f)), CircleShape)
                            .clickable { colorValue = argb; posValue = MainConst.presetPositions.getOrElse(index) { 0f } },
                        contentAlignment = Alignment.Center,
                    ) { if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = if (argb == MainConst.gradientColorsAlt.getOrNull(3) && type == 4) Color.Black else Color.White) }
                }
            }
            Palette()
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { confirmReset = true }) { Text(stringResource(AppR.string.reset)) }
                TextButton(onClick = { commit(true) }) { Text(stringResource(AppR.string.apply)) }
            }
        }
    }

    /** The colour strip: touch or drag anywhere to pick the colour at that place. */
    @androidx.compose.runtime.Composable
    private fun Palette() {
        if (stops.isEmpty() || stops.size != poses.size) return
        val outline = if (AppState.darkUi) Color(-12632257) else Color.Black
        Canvas(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).height(44.dp).pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    fun pick(markerX: Float) {
                        val newPos = (markerX / size.width).coerceIn(0f, 1f)
                        posValue = newPos
                        colorValue = colourAt(newPos)
                    }
                    pick(down.position.x)
                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.first()
                        if (change.pressed) { pick(change.position.x); change.consume() }
                    } while (event.changes.any { it.pressed })
                }
            },
        ) {
            val stopPairs = Array(stops.size) { poses[it] to Color(stops[it] or (0xFF shl 24)) }
            drawRect(Brush.horizontalGradient(colorStops = stopPairs), size = size)
            drawRect(outline, size = size, style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
            val markerX = (posValue.coerceIn(0f, 1f)) * size.width
            val half = 6.dp.toPx()
            drawRect(Color.Black, Offset(markerX - half, -2.dp.toPx()), Size(half * 2, size.height + 4.dp.toPx()), style = androidx.compose.ui.graphics.drawscope.Stroke(4.dp.toPx()))
            drawRect(Color.White, Offset(markerX - half, -2.dp.toPx()), Size(half * 2, size.height + 4.dp.toPx()), style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
        }
    }

    init { show() }

    override fun dismiss() {
        listener = null
        seekListener = null
        super.dismiss()
    }

    companion object {
    }
}
