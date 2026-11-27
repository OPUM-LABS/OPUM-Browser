package com.mycompany.app.wview

import com.mycompany.app.ui.browser.notiDot
import android.content.Context
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.view.View
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.mycompany.app.ui.browser.ComposeOverlay
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * The look of a floating page button (scroll up / down, read aloud, and the zoom / lock / translate / user buttons),
 * drawn with Compose. The button classes keep their touch, drag, position and auto-hide logic; they fill [face] in
 * [fillFace] whenever their look changes (their `invalidate()` calls [publishFace]) and report presses with
 * [pressDown] / [pressUp] / [pressReset]; the press circle animation runs here. The Compose layer has no pointer
 * handlers, so touches still go to the button class itself.
 */
abstract class FloatFaceView(context: Context) : ComposeOverlay(context) {

    /** What to draw, in this view's pixels. A colour of 0 means "nothing". */
    class Face {
        @JvmField var cx = 0f
        @JvmField var cy = 0f
        /** Background circle, in its resting colour and alpha (it fades while pressed). */
        @JvmField var bg = 0
        @JvmField var bgRadius = 0f
        /** Press circle colour (with its full alpha); drawn while pressed, scaled 0.8 → 1 around the centre. */
        @JvmField var press = 0
        @JvmField var pressRadius = 0f
        /** Outline ring (stroke [ringWidth]): around the background, or around the press circle while pressed. */
        @JvmField var ring = 0
        @JvmField var ringWidth = 0f
        /** Icon with its bounds already set by the button. */
        @JvmField var icon: Drawable? = null
        /** Loading: an M3 spinner of [loadingSize] in [loadingColor] instead of the icon. */
        @JvmField var loading = false
        @JvmField var loadingColor = 0
        @JvmField var loadingSize = 0f
        /** Read aloud "stop" square. */
        @JvmField var stopRect: RectF? = null
        @JvmField var stopColor = 0
        /** The "something here" dot ([notiDot]) when not 0, on the corner of the icon centred at [dotX], [dotY]. */
        @JvmField var dot = 0
        @JvmField var dotX = 0f
        @JvmField var dotY = 0f
        @JvmField var dotRtl = false
        @JvmField var visible = true
    }

    @JvmField val face = Face()
    private var tick by mutableIntStateOf(0)

    // ---- press feedback (was a copied pair of ValueAnimators in each button) ----
    private val pressScale = Animatable(0.8f)
    /** A finger holds the button. */
    private var held by mutableStateOf(false)
    /** The grow / shrink animation is running. */
    private var pressAnimating by mutableStateOf(false)
    private var pressMode = 0
    private var pressGen by mutableIntStateOf(0)

    /** True while the press circle shows (held, or still shrinking after release). */
    val isPressShown: Boolean get() = held || pressAnimating

    /** Finger down: the press circle grows from 0.8 to full size in 200 ms. */
    fun pressDown() {
        held = true
        pressMode = 1
        pressGen++
    }

    /** Finger up or moved off: the circle shrinks back to 0.8 in 200 ms and goes. */
    fun pressUp() {
        if (!held && !pressAnimating) return
        held = false
        pressMode = 2
        pressGen++
    }

    /** The press circle reached full size (used by the media button's pulse). */
    protected open fun onPressGrown() {}

    /** Drops the press feedback at once (hidden, disabled, released). */
    fun pressReset() {
        held = false
        pressAnimating = false
        pressMode = 3
        pressGen++
    }

    // ---- hosted in the browser's Compose overlay stack ----
    /** The page body the button used to be a child of: its size and padding bound the drag (see [boundsParent]). */
    private var hostBody: View? = null
    /** Position in body coordinates while hosted (what `getX()` / `getY()` returned as a body child). */
    var slotX by mutableFloatStateOf(0f)
        private set
    var slotY by mutableFloatStateOf(0f)
        private set
    var slotShown by mutableStateOf(true)
        private set

    /** The button's side in px while hosted (follows the size setting); 0: the size it was added with. */
    var slotSide by mutableIntStateOf(0)

    /** Called by the overlay stack: from now on position and visibility are Compose state. */
    fun hostIn(body: View) {
        hostBody = body
        // As a body child it sat at the body's padding plus its translation.
        slotX = body.paddingLeft + super.getTranslationX()
        slotY = body.paddingTop + super.getTranslationY()
        super.setTranslationX(0f)
        super.setTranslationY(0f)
        slotShown = visibility == VISIBLE
    }

    fun unhost() {
        hostBody = null
    }

    /** The view whose size and padding bound the drag: the old parent (page body) while hosted, else the parent. */
    fun boundsParent(): android.view.ViewParent? = (hostBody as? android.view.ViewParent) ?: parent

    override fun setX(x: Float) {
        if (hostBody != null) slotX = x else super.setX(x)
    }

    override fun setY(y: Float) {
        if (hostBody != null) slotY = y else super.setY(y)
    }

    override fun getX(): Float = if (hostBody != null) slotX else super.getX()

    override fun getY(): Float = if (hostBody != null) slotY else super.getY()

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        slotShown = visibility == VISIBLE
    }

    init {
        setPage({ false }) { Content() }
    }

    /** Fill [face] from the button's own fields (the old `onDraw`). */
    protected abstract fun fillFace(face: Face)

    /** Re-reads the face and redraws (called from the buttons' `invalidate()`). */
    fun publishFace() {
        try {
            fillFace(face)
        } catch (_: Exception) {
            face.visible = false
        }
        tick++
    }

    @androidx.compose.runtime.Composable
    private fun Content() {
        LaunchedEffect(pressGen) {
            when (pressMode) {
                1 -> {
                    pressAnimating = true
                    pressScale.snapTo(0.8f)
                    pressScale.animateTo(1f, tween(200, easing = DecelerateEasing))
                    pressAnimating = false
                    onPressGrown()
                }
                2 -> {
                    if (pressScale.value > 0.8f) {
                        pressAnimating = true
                        pressScale.animateTo(0.8f, tween(200, easing = AccelerateEasing))
                    }
                    pressAnimating = false
                }
                3 -> pressScale.snapTo(0.8f)
            }
        }
        Box(Modifier.fillMaxSize()) {
            Canvas(Modifier.fillMaxSize()) {
                @Suppress("UNUSED_VARIABLE") val redrawTick = tick
                val faceNow = face
                if (!faceNow.visible) return@Canvas
                val offset = Offset(faceNow.cx, faceNow.cy)
                val pressing = isPressShown
                val o = pressScale.value
                if (faceNow.bg != 0) {
                    val a = faceNow.bg ushr 24
                    val alpha = if (pressing) min(((1f - o) * a * 5f).roundToInt(), a) else a
                    drawCircle(Color(withAlpha(faceNow.bg, alpha)), faceNow.bgRadius, offset)
                }
                val scaled = pressing && faceNow.press != 0
                if (scaled) {
                    scale(o, offset) {
                        drawCircle(Color(withAlpha(faceNow.press, ((o - 0.8f) * (faceNow.press ushr 24) * 5f).roundToInt())), faceNow.pressRadius, offset)
                        if (faceNow.ring != 0) drawCircle(Color(faceNow.ring), faceNow.pressRadius - faceNow.ringWidth / 2f, offset, style = Stroke(faceNow.ringWidth))
                    }
                } else if (faceNow.ring != 0) {
                    drawCircle(Color(faceNow.ring), faceNow.bgRadius - faceNow.ringWidth / 2f, offset, style = Stroke(faceNow.ringWidth))
                }
                if (!faceNow.loading) {
                    val stopRect = faceNow.stopRect
                    if (stopRect != null && faceNow.stopColor != 0) {
                        drawRect(Color(faceNow.stopColor), Offset(stopRect.left, stopRect.top), Size(stopRect.width(), stopRect.height()))
                    }
                    val icon = faceNow.icon
                    if (icon != null) drawIntoCanvas { icon.draw(it.nativeCanvas) }
                }
                if (faceNow.dot != 0) notiDot(Offset(faceNow.dotX, faceNow.dotY), faceNow.dotRtl)
            }
            @Suppress("UNUSED_VARIABLE") val redrawTick = tick
            if (face.visible && face.loading && face.loadingSize > 0f) {
                val density = LocalDensity.current
                val size = face.loadingSize
                CircularProgressIndicator(
                    color = Color(face.loadingColor),
                    strokeWidth = with(density) { (size / 10f).coerceAtLeast(2f).toDp() },
                    modifier = Modifier
                        .offset { IntOffset((face.cx - size / 2f).roundToInt(), (face.cy - size / 2f).roundToInt()) }
                        .size(with(density) { size.toDp() }),
                )
            }
        }
    }

    companion object {
        private val DecelerateEasing = androidx.compose.animation.core.Easing { 1f - (1f - it) * (1f - it) }
        private val AccelerateEasing = androidx.compose.animation.core.Easing { it * it }

        /** [color] with alpha [alpha] (0..255), like `Paint.setAlpha` on a paint of that colour. */
        @JvmStatic
        fun withAlpha(color: Int, alpha: Int): Int = (color and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)
    }
}
