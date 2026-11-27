package com.mycompany.app.quick

import android.content.Context
import com.mycompany.app.ui.state.AppState
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.view.FolderCells

/**
 * One shortcut tile of the new-tab grid and of a folder (`QuickAdapter` item types 0 and 1), drawn by Compose. It is
 * the row's own view, so the adapter's click / long-press listeners, the round ripple background, the alpha of the
 * "add" tile while selecting and the drag scaling act on it as before. [compact] (type 1) puts a small icon on a
 * coloured disc; the normal tile shows a larger icon. The icon is set like the old `MyRoundImage` calls the adapter
 * made ([iconCircle], [iconImage], [iconLetter], [iconBitmap], [iconFolder]).
 */
class QuickTileView(context: Context, private val compact: Boolean) : QuickRowView(context) {
    /** The shortcut's name; [labelShown] false keeps its space (the "add" tile). */
    var label by mutableStateOf("")
    var labelColor by mutableIntStateOf(-16777216)
    var labelShown by mutableStateOf(true)
    /** Compact tile: the disc behind the icon (0 = none). */
    var discColor by mutableIntStateOf(0)

    var checkShown by mutableStateOf(false)
    private var checked by mutableStateOf(false)
    private var checkAnimate = false
    private var checkBg by mutableIntStateOf(0)
    private var checkOnRes by mutableIntStateOf(0)
    private var checkOffRes by mutableIntStateOf(0)

    private var tileKind by mutableIntStateOf(NONE)
    private var bitmap by mutableStateOf<Bitmap?>(null)
    private var imageRes by mutableIntStateOf(0)
    private var circleColor by mutableIntStateOf(0)
    private var letterText by mutableStateOf("")
    private var letterColor by mutableIntStateOf(-1)
    private var backColor by mutableIntStateOf(0)
    /** Bumped when a folder cell finished loading. */
    private var cellsVersion by mutableIntStateOf(0)
    private val cells = FolderCells(context) { cellsVersion++ }

    private val iconDp = if (compact) 24 else 38
    private val letterPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    // ---- icon (one of these per bind) ----

    private fun show(newKind: Int) {
        tileKind = newKind
        if (newKind != FOLDER) cells.clear()
        if (newKind != BITMAP) bitmap = null
        backColor = 0
    }

    /** Disc of [color] with icon [res] at its own size (the "add" tile). */
    fun iconCircle(color: Int, res: Int) {
        show(CIRCLE_ICON)
        circleColor = color
        imageRes = res
    }

    /** Image [res] filling the icon. */
    fun iconImage(res: Int) {
        show(IMAGE)
        imageRes = res
    }

    /** [text] on a disc of [color] (0 = grey); [secret] also draws dark (secret mode). */
    fun iconLetter(title: String?, secret: Boolean, color: Int) {
        val dark = AppState.darkUi || MainUtil.isDarkPage(secret)
        val circle = when {
            color == 0 -> if (dark) -14606047 else -460552
            dark && color == -460552 -> -14606047
            else -> color
        }
        show(LETTER)
        circleColor = circle
        letterText = title ?: ""
        letterColor = if (circle == -460552 || circle == -11513776 || circle == -14606047) {
            if (dark) -5197648 else -16777216
        } else {
            -1
        }
    }

    /** The favicon [bmp]; when it is not usable a disc of [color]. */
    fun iconBitmap(bmp: Bitmap?, color: Int) {
        if (!MainUtil.isBitmapOk(bmp)) {
            show(CIRCLE_ICON)
            circleColor = if (AppState.darkUi && color == -460552) -11513776 else color
            imageRes = 0
            return
        }
        show(BITMAP)
        bitmap = bmp
    }

    /** Folder [id]: its first four shortcuts as small cells over [back]; [secret] reads the secret favicon cache. */
    fun iconFolder(id: Int, list: List<Any?>?, secret: Boolean, back: Int) {
        show(FOLDER)
        backColor = back
        cells.set(id, list, secret)
        cellsVersion++
    }

    // ---- selection check ----

    /** Colours of the check: disc [bg], icons when on / off. */
    fun setCheckColors(bg: Int, onRes: Int, offRes: Int) {
        checkBg = bg
        checkOnRes = onRes
        checkOffRes = offRes
    }

    /** Ticks or unticks; [animate] fades between the two icons. */
    fun setChecked(on: Boolean, animate: Boolean) {
        checkAnimate = animate
        checked = on
    }

    /** Stops background loads (the grid is going away). */
    fun release() = cells.release()

    @Composable
    override fun Content() {
        val density = LocalDensity.current
        Box(Modifier.fillMaxSize()) {
            if (compact) {
                val disc = discColor
                if (disc != 0) {
                    Canvas(Modifier.align(Alignment.TopCenter).padding(top = 10.dp).size(40.dp)) {
                        drawCircle(Color(disc))
                    }
                }
            }
            Box(
                Modifier.align(Alignment.TopCenter).padding(top = if (compact) 18.dp else 11.dp).size(iconDp.dp)
                    .clip(RoundedCornerShape(4.dp))
            ) { Icon() }
            Text(
                label,
                style = TextStyle(color = Color(labelColor), fontSize = 12.sp),
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip,
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 54.dp).padding(horizontal = 6.dp)
                    .alpha(if (labelShown) 1f else 0f),
            )
            if (checkShown) {
                Check(Modifier.align(Alignment.TopCenter).offset(x = -(with(density) { MainApp.dp16.toDp() } / 2)))
            }
        }
    }

    @Composable
    private fun BoxScope.Icon() {
        when (tileKind) {
            BITMAP -> {
                val bmp = bitmap
                if (bmp != null && MainUtil.isBitmapOk(bmp)) {
                    com.mycompany.app.ui.components.SiteFavicon(bmp, Modifier.fillMaxSize(), Color.Gray.copy(alpha = 0.14f), RoundedCornerShape(4.dp))
                }
            }
            IMAGE -> if (imageRes != 0) {
                Image(painterResource(imageRes), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            CIRCLE_ICON -> {
                val color = circleColor
                if (color != 0) Canvas(Modifier.fillMaxSize()) { drawCircle(Color(color)) }
                if (imageRes != 0) {
                    Image(painterResource(imageRes), null, Modifier.align(Alignment.Center))
                }
            }
            LETTER -> {
                val color = circleColor
                val letterText = letterText
                val textColor = letterColor
                Canvas(Modifier.fillMaxSize()) {
                    if (color != 0) drawCircle(Color(color))
                    if (letterText.isNotEmpty()) drawLetter(letterText, size.width / 2f, textColor, center)
                }
            }
            FOLDER -> {
                @Suppress("UNUSED_VARIABLE") val version = cellsVersion
                val back = backColor
                Canvas(Modifier.fillMaxSize()) {
                    if (back != 0) drawRect(Color(back))
                    drawCells()
                }
            }
        }
    }

    /** Bold centred [text], [px] high, as the old view drew it. */
    private fun DrawScope.drawLetter(glyph: String, px: Float, color: Int, at: Offset) {
        letterPaint.textSize = px
        letterPaint.color = color
        val baseline = at.y - (letterPaint.descent() + letterPaint.ascent()) / 2f
        drawIntoCanvas { it.nativeCanvas.drawText(glyph, at.x, baseline, letterPaint) }
    }

    /** Four cells in the corners (0 top-left, 1 top-right, 2 bottom-left, 3 bottom-right). */
    private fun DrawScope.drawCells() {
        val gap = MainApp.dp4 / 4
        val tileW = size.width.toInt()
        val tileH = size.height.toInt()
        val cw = tileW / 2 - gap * 3
        val ch = tileH / 2 - gap * 3
        val pad = gap shl 1
        if (cw <= 0 || ch <= 0) return
        for (cell in 0..3) {
            val left = if (cell == 1 || cell == 3) tileW - cw - pad else pad
            val top = if (cell >= 2) tileH - ch - pad else pad
            val bmp = cells.bitmap[cell]
            if (bmp != null) {
                if (!MainUtil.isBitmapOk(bmp)) continue
                drawImage(
                    bmp.asImageBitmap(), srcSize = IntSize(bmp.width, bmp.height),
                    dstOffset = IntOffset(left, top), dstSize = IntSize(cw, ch),
                )
                continue
            }
            val glyph = cells.letters[cell]
            if (glyph.isNullOrEmpty()) continue
            val half = cw / 2f
            drawRoundRect(
                Color(cells.color[cell]), Offset(left.toFloat(), top.toFloat()), Size(cw.toFloat(), ch.toFloat()),
                CornerRadius(half, ch / 2f),
            )
            drawLetter(glyph, half, -1, Offset(left + half, top + half))
        }
    }

    /** The selection check: a disc with the ticked icon growing in over the open circle. */
    @Composable
    private fun Check(modifier: Modifier) {
        val on by animateFloatAsState(
            if (checked) 1f else 0f,
            if (checkAnimate) tween(200) else snap(),
            label = "check",
        )
        Box(modifier.size(32.dp), contentAlignment = Alignment.Center) {
            val bg = checkBg
            if (bg != 0) Canvas(Modifier.size(24.dp)) { drawCircle(Color(bg)) }
            if (checkOffRes != 0 && on < 1f) {
                Image(painterResource(checkOffRes), null, Modifier.alpha(1f - on))
            }
            if (checkOnRes != 0 && on > 0f) {
                Image(painterResource(checkOnRes), null, Modifier.scale(on).alpha(on))
            }
        }
    }

    private companion object {
        const val NONE = 0
        const val BITMAP = 1
        const val IMAGE = 2
        const val CIRCLE_ICON = 3
        const val LETTER = 4
        const val FOLDER = 5
    }
}
