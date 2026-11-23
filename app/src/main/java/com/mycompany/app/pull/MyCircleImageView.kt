package com.mycompany.app.pull

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.animation.Animation
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.mycompany.app.ui.browser.ComposeOverlay

/**
 * The pull-to-refresh disc (base of [MyPullView]), drawn with Compose: a Material surface circle with a shadow; inside it
 * the progress arc and arrow head from the [MyMaterialProgressDrawable] ring state while pulling (same geometry as the
 * old drawable), and an M3 `CircularProgressIndicator` while refreshing. The pull view's own view animations (move,
 * scale, alpha, the drawable's spin animation) are unchanged. Same members as the old `ImageView`: [animListener]
 * animation listener, [shadowRadius] shadow size, [setDiscColor], `setBackgroundColor`, `setImageDrawable`.
 */
open class MyCircleImageView(context: Context?) : ComposeOverlay(context!!) {
    @JvmField
    var animListener: Animation.AnimationListener? = null
    @JvmField
    var shadowRadius: Int = 0

    private var bg by mutableIntStateOf(0)
    private var tick by mutableIntStateOf(0)
    private var progress: MyMaterialProgressDrawable? = null

    init {
        setPage({ false }) { Disc() }
    }

    /** Disc colour and shadow size (old: an oval shadow drawable as background). */
    fun setDiscColor(newBg: Int) {
        this.shadowRadius = (resources.displayMetrics.density * 3.5f).toInt()
        bg = newBg
    }

    /** The ring state to draw (old `ImageView.setImageDrawable`). */
    fun setImageDrawable(drawable: Drawable?) {
        progress = drawable as? MyMaterialProgressDrawable
        drawable?.callback = this
        tick++
    }

    override fun verifyDrawable(who: Drawable): Boolean = who === progress || super.verifyDrawable(who)

    override fun invalidateDrawable(drawable: Drawable) {
        if (drawable === progress) tick++ else super.invalidateDrawable(drawable)
    }

    override fun invalidate() {
        super.invalidate()
        tick++
    }

    public override fun onAnimationEnd() {
        super.onAnimationEnd()
        animListener?.onAnimationEnd(animation)
    }

    public override fun onAnimationStart() {
        super.onAnimationStart()
        animListener?.onAnimationStart(animation)
    }

    public override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(measuredWidth + (this.shadowRadius shl 1), measuredHeight + (this.shadowRadius shl 1))
    }

    override fun setBackgroundColor(color: Int) {
        bg = color
    }

    @androidx.compose.runtime.Composable
    private fun Disc() {
        val density = LocalDensity.current
        val inset = with(density) { shadowRadius.toDp() }
        Box(Modifier.fillMaxSize().padding(inset)) {
            Surface(Modifier.fillMaxSize(), shape = CircleShape, color = Color(bg), shadowElevation = 3.dp) {
                @Suppress("UNUSED_VARIABLE") val tickNow = tick
                val d = progress ?: return@Surface
                val ring = d.mainRing
                if (d.isRunning) {
                    CircularProgressIndicator(
                        color = Color(ring.currentColor),
                        strokeWidth = with(density) { ring.strokeWidth.toDp() },
                        modifier = Modifier.fillMaxSize().padding(with(density) { (ring.strokeInset + ring.strokeWidth / 2f).toDp() }),
                    )
                } else {
                    Canvas(Modifier.fillMaxSize()) {
                        @Suppress("UNUSED_VARIABLE") val redrawTick = tick
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val offset = Offset(cx, cy)
                        rotate(d.ringRotation, offset) {
                            val start = (ring.startTrim + ring.rotation) * 360f
                            val sweep = ((ring.endTrim + ring.rotation) * 360f) - start
                            val strokeInset = ring.strokeInset
                            drawArc(
                                Color(ring.currentColor), start, sweep, false,
                                topLeft = Offset(strokeInset, strokeInset),
                                size = Size(size.width - 2 * strokeInset, size.height - 2 * strokeInset),
                                style = Stroke(ring.strokeWidth, cap = StrokeCap.Square),
                            )
                            if (ring.showArrow) {
                                val arrowScale = ring.arrowScale
                                val half = ((ring.strokeInset.toInt()) / 2) * arrowScale
                                val ox = (ring.centerRadius + cx).toFloat() - half
                                val path = Path().apply {
                                    moveTo(ox, cy)
                                    lineTo(ox + ring.arrowWidth * arrowScale, cy)
                                    lineTo(ox + (ring.arrowWidth * arrowScale) / 2f, cy + ring.arrowHeight * arrowScale)
                                    close()
                                }
                                rotate((start + sweep) - 5f, offset) { drawPath(path, Color(ring.currentColor)) }
                            }
                            if (ring.alpha < 255) {
                                drawCircle(Color(ring.backgroundColor).copy(alpha = (255 - ring.alpha) / 255f), size.width / 2f, offset)
                            }
                        }
                    }
                }
            }
        }
    }
}
