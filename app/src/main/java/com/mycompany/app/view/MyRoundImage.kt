package com.mycompany.app.view

import android.content.Context
import com.mycompany.app.ui.state.AppState
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.View
import android.view.ViewOutlineProvider
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.image.ImageSizeListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import java.util.regex.Pattern

/**
 * The rounded icon of list rows, shortcut tiles and dialogs. It stays an [ImageView] because the image loaders write
 * favicons and thumbnails into it ([path] marks the path being loaded, [setImageBitmap] fades the result in). Besides an
 * image it can draw, clipped to a rounded rectangle of [setRoundRadius]:
 *  - a filled circle ([applyCircle] / [setCircleColor]) with a centred icon ([showIcon], [setCircleImage]) or letter ([showLetterIcon], [showLetterNeutral]);
 *  - a letter on a white rounded box ([showBoxLetter]);
 *  - a folder: up to four small cells, each a favicon or a coloured letter ([setFolder]).
 * The image itself is drawn by ImageView; the rest is a background drawable ([Face]), so `getDrawable()` stays null
 * for the letter and circle modes as before (2026-11-17: the custom onDraw only gates the image now).
 * Colours that were made for light pages (the grey circle −460552, black icons) switch to their dark versions when the
 * app is dark or [setDarkColor] asks for it.
 */
class MyRoundImage(@JvmField var ctx: Context?) : ImageView(ctx) {
    /** False after [release]: draws and loads nothing more. */
    @JvmField var alive: Boolean = true
    /** The URL this icon stands for (set by the lists). */
    /** The image path being loaded into this view. */
    @JvmField var path: String? = null

    private var sizeListener: ImageSizeListener? = null
    /** Fill of the whole view (0 = none). */
    private var backColor = 0
    private var roundRadius: Int = MainApp.dp8
    private var circleRadius = 0f
    private var circleColor = 0
    private var circlePaint: Paint? = null
    /** Draw the image at this width instead of the view's (0 = full size). */
    private var smallIconSize = 0
    private var circleIconRes = 0
    private var circleIcon: Drawable? = null
    /** Letter drawn over the circle, or on a white box when [boxText]. */
    private var shownLetter: String? = null
    private var textPaint: Paint? = null
    private var boxText = false
    private val boxRect = RectF()
    /** Fade in the next image (the path being loaded changed). */
    private var fadeNext = false
    private var anim: AlphaAnimation? = null
    private var forceDark = false

    // folder mode
    private val cells = FolderCells(ctx) { redraw() }
    private var cellGap = 0
    private var cellBox: Paint? = null
    private var cellLetter: Paint? = null
    private val srcRect = Rect()
    private val dstRect = RectF()

    init {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                outline?.setRoundRect(0, 0, width, height, roundRadius.toFloat())
            }
        }
        clipToOutline = true
        background = Face()
    }

    // ---- folder ----

    /**
     * Folder mode: draws the first four [list] items (`QuickSubItem`s) of folder [cell] as small cells over [newBackColor].
     * [fade] = secret mode (favicons come from the secret cache). An empty list or folder 0 leaves folder mode.
     */
    fun setFolder(cell: Int, list: List<Any?>?, fade: Boolean, newBackColor: Int) {
        if (cell == 0 || list == null || list.isEmpty()) {
            cells.set(cell, list, fade)
            return
        }
        path = null
        circlePaint = null
        circleIconRes = 0
        circleIcon = null
        shownLetter = null
        boxText = false
        cancelFade()
        cellGap = MainApp.dp4 / 4
        val changed = backColor != newBackColor
        backColor = newBackColor
        // every cell is loaded
        if (cells.set(cell, list, fade) || changed) redraw()
    }

    /** Leaves folder mode. */
    fun clearCells() = cells.clear()

    /** Redraws (after cells changed). */
    fun redraw() {
        if (alive) invalidate()
    }

    // ---- colours ----

    /** The light grey circle becomes dark grey on dark pages. */
    fun darkCircle(color: Int): Int = if (useDark() && color == -460552) -11513776 else color

    /** Black icons become their dark versions on dark pages. */
    fun darkIcon(iconRes: Int): Int = if (useDark()) DARK_ICONS[iconRes] ?: iconRes else iconRes

    /** Dark colours apply (app dark or [setDarkColor]). */
    fun useDark(): Boolean = AppState.darkUi || forceDark

    fun setDarkColor(newForceDark: Boolean) {
        forceDark = newForceDark
    }

    /** Sets the circle colour (0 = no circle); true if anything changed. */
    fun applyCircle(newCircleColor: Int): Boolean {
        if (newCircleColor == 0) {
            val had = circlePaint != null || circleColor != 0
            circlePaint = null
            circleColor = 0
            return had
        }
        val paint = circlePaint ?: fillPaint().also { circlePaint = it }
        val changed = circleColor != newCircleColor || paint.color != newCircleColor
        circleColor = newCircleColor
        paint.color = newCircleColor
        return changed
    }

    // ---- content setters (each replaces what was shown) ----

    /** Drops the image, letter and folder, and stops a fade. */
    private fun reset(keepBack: Boolean = false) {
        path = null
        if (!keepBack) backColor = 0
        shownLetter = null
        boxText = false
        cancelFade()
        clearCells()
    }

    private fun setCircleIcon(res: Int) {
        if (circleIconRes == res && (res == 0) == (circleIcon == null)) return
        circleIconRes = res
        circleIcon = if (res == 0) null else MainUtil.loadDrawable(ctx, res)
    }

    /** A circle of colour [iconColor] (light grey = none) with icon [iconRes]; both follow dark mode. */
    fun showIcon(iconColor: Int, iconRes: Int) {
        if (ctx == null) return
        reset()
        super.setImageDrawable(null)
        applyCircle(if (iconColor == -460552) 0 else darkCircle(iconColor))
        setCircleIcon(darkIcon(iconRes))
        invalidate()
    }

    fun setCircleColor(color: Int) {
        // the letter stays (as before)
        path = null
        backColor = 0
        cancelFade()
        clearCells()
        applyCircle(darkCircle(color))
        super.setImageDrawable(null)
        invalidate()
    }

    fun setCircleImage(color: Int) {
        if (ctx == null) return
        reset()
        super.setImageDrawable(null)
        setCircleIcon(darkIcon(color))
        invalidate()
    }

    private fun showLetter(letterText: String, circle: Int, size: Float, color: Int) {
        reset()
        circleIconRes = 0
        circleIcon = null
        super.setImageDrawable(null)
        applyCircle(circle)
        shownLetter = letterText
        val paint = textPaint ?: textPaint().also { textPaint = it }
        paint.textSize = size
        paint.color = color
        invalidate()
    }

    /** The initial of [str] (by [pattern]) on colour [iconColor]; nothing to show → circle [iconColor] with icon [iconRes] ([showIcon]). */
    fun showLetterIcon(iconColor: Int, iconRes: Int, name: String?, pattern: Pattern?) {
        val initial = MainUtil.stripPunctuation(name, pattern)
        if (initial.isNullOrEmpty()) {
            showIcon(0, iconRes)
            return
        }
        val circle = if (iconColor != -460552) darkCircle(iconColor) else 0
        val size = if (circle == 0) maxOf(circleRadius, MainApp.dp16.toFloat()) else circleRadius
        showLetter(initial, circle, size, if (useDark()) -328966 else -16777216)
    }

    /** The initial of [str] (by [pattern]) on colour [color] (0 = grey). */
    fun showLetterNeutral(color: Int, title: String?, pattern: Pattern?) {
        val circle = neutral(color, useDark())
        showLetter(MainUtil.stripPunctuation(title, pattern) ?: "", circle, circleRadius, letterColor(circle, useDark(), -328966))
    }

    /** The initial of [str] in blue on a white box; empty → image [res]. */
    fun showBoxLetter(res: Int, name: String?) {
        val initial = MainUtil.stripPunctuation(name, null)
        if (initial.isNullOrEmpty()) {
            setImageResource(res)
            return
        }
        showLetter(initial, 0, circleRadius, -9807617)
        boxText = true
    }

    override fun setImageBitmap(bitmap: Bitmap?) {
        // the load path stays: the loaders compare it with the next request
        shownLetter = null
        boxText = false
        cancelFade()
        clearCells()
        if (!MainUtil.isBitmapOk(bitmap)) {
            path = null
            backColor = 0
            super.setImageDrawable(null)
            return
        }
        super.setImageBitmap(bitmap)
        fadeIn()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        shownLetter = null
        boxText = false
        cancelFade()
        clearCells()
        if (drawable == null) {
            path = null
            backColor = 0
            super.setImageDrawable(null)
            return
        }
        super.setImageDrawable(drawable)
        fadeIn()
    }

    override fun setImageResource(resId: Int) {
        reset()
        super.setImageResource(resId)
    }

    // ---- loading and fades ----

    /** [str] is being loaded into this view; [fade] fades the image in when it arrives (only for a new path). */
    fun usePath(newPath: String?, fade: Boolean) {
        fadeNext = if (TextUtils.isEmpty(path) || path != newPath) fade else false
        path = newPath
        if (!fadeNext) cancelFade()
    }

    private fun fadeIn() {
        if (!fadeNext || visibility != VISIBLE) return
        val fade = AlphaAnimation(0.0f, 1.0f)
        fade.duration = 400L
        fade.interpolator = DecelerateInterpolator()
        anim = fade
        startAnimation(fade)
    }

    /** Fades out, then stays visible only if activated. */
    fun fadeOut() {
        path = null
        backColor = 0
        cancelFade()
        val fade = AlphaAnimation(1.0f, 0.0f)
        fade.duration = 400L
        fade.interpolator = DecelerateInterpolator()
        fade.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                anim = null
                clearAnimation()
                visibility = if (isActivated) VISIBLE else INVISIBLE
            }

            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}
        })
        anim = fade
        visibility = VISIBLE
        startAnimation(fade)
    }

    /** Stops a running fade. */
    fun cancelFade() {
        val fade = anim ?: return
        fade.cancel()
        anim = null
        clearAnimation()
    }

    /** Asks for a new layout on the next frame. */
    fun relayout() {
        if (alive) post { if (alive) requestLayout() }
    }

    /** Releases everything; the view draws nothing afterwards. */
    fun release() {
        alive = false
        cancelFade()
        cells.release()
        sizeListener = null
        circlePaint = null
        circleIcon = null
        shownLetter = null
        textPaint = null
        path = null
        background = null
    }

    // ---- simple settings ----

    /** Fill of the whole view behind everything (light grey follows dark mode). */
    fun setBackColor(color: Int) {
        backColor = darkCircle(color)
    }

    fun setCircleRadius(newCircleRadius: Float) {
        circleRadius = newCircleRadius
    }

    /** Draw the image smaller (three small margins wide). */
    fun setIconSmall(small: Boolean) {
        smallIconSize = if (small) MainApp.dp8 * 3 else 0
    }

    fun setListener(imageSizeListener: ImageSizeListener?) {
        sizeListener = imageSizeListener
    }

    fun setRoundRadius(newRoundRadius: Int) {
        roundRadius = newRoundRadius
    }

    override fun setVisibility(visibility: Int) {
        if (visibility != VISIBLE) cancelFade()
        super.setVisibility(visibility)
    }

    override fun invalidate() {
        if (alive) super.invalidate()
    }

    // ---- drawing ----

    override fun onDetachedFromWindow() {
        cancelFade()
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        sizeListener?.onSize(this, w, h)
    }

    /**
     * Only gates the stock image drawing: nothing after [release] or in folder mode, a recycled bitmap is skipped, and the
     * small-icon scale. Everything else (fill, circle, icon, letter, cells) is the [Face] background.
     */
    override fun onDraw(canvas: Canvas) {
        if (!alive || cells.isActive) return
        val drawable = drawable ?: return
        if (drawable is BitmapDrawable && !MainUtil.isBitmapOk(drawable.bitmap)) return
        if (smallIconSize != 0 && width != 0) {
            // the old integer division: only shrinks when the icon is at least as wide as the view
            val scale = (smallIconSize / width).toFloat()
            canvas.scale(scale, scale, width / 2.0f, height / 2.0f)
        }
        try {
            super.onDraw(canvas)
        } catch (_: Exception) {
        }
    }

    /**
     * The view's background: the fill of [setBackColor], then either the folder cells or, while no image is set, the
     * circle with its icon or letter (on the white box for [showBoxLetter]). It reads the view's state when drawn, so [invalidate]
     * is all a change needs.
     */
    private inner class Face : Drawable() {
        override fun draw(canvas: Canvas) {
            if (!alive) return
            try {
                if (backColor != 0) canvas.drawColor(backColor)
                if (cells.isActive) {
                    drawCells(canvas)
                    return
                }
                if (drawable != null) return
                val cx = bounds.exactCenterX()
                val cy = bounds.exactCenterY()
                circlePaint?.let { canvas.drawCircle(cx, cy, circleRadius, it) }
                val initial = shownLetter
                if (initial.isNullOrEmpty()) {
                    val icon = circleIcon ?: return
                    val iw = icon.intrinsicWidth
                    val ih = icon.intrinsicHeight
                    val left = (bounds.width() - iw) / 2
                    val top = (bounds.height() - ih) / 2
                    icon.setBounds(left, top, left + iw, top + ih)
                    icon.draw(canvas)
                    return
                }
                val paint = textPaint ?: return
                if (boxText) {
                    val color = paint.color
                    paint.color = -1
                    boxRect.set(bounds)
                    val radius = roundRadius.toFloat()
                    canvas.drawRoundRect(boxRect, radius, radius, paint)
                    paint.color = color
                }
                canvas.drawText(initial, cx, cy - (paint.descent() + paint.ascent()) / 2.0f, paint)
            } catch (_: Exception) {
            }
        }

        override fun setAlpha(alpha: Int) {}
        override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {}
        @Deprecated("Deprecated in Java")
        override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
    }

    /** Four cells in the corners (0 top-left, 1 top-right, 2 bottom-left, 3 bottom-right). */
    private fun drawCells(canvas: Canvas) {
        val widthNow = width
        val heightNow = height
        if (widthNow == 0 || heightNow == 0) return
        val cw = widthNow / 2 - cellGap * 3
        val ch = heightNow / 2 - cellGap * 3
        val pad = cellGap shl 1
        for (cell in 0..3) {
            val left = (if (cell == 1 || cell == 3) widthNow - cw - pad else pad).toFloat()
            val top = (if (cell >= 2) heightNow - ch - pad else pad).toFloat()
            dstRect.set(left, top, left + cw, top + ch)
            val bitmap = cells.bitmap[cell]
            if (bitmap != null) {
                if (!MainUtil.isBitmapOk(bitmap)) continue
                srcRect.set(0, 0, bitmap.width, bitmap.height)
                canvas.drawBitmap(bitmap, srcRect, dstRect, null)
                continue
            }
            val initial = cells.letters[cell]
            if (initial.isNullOrEmpty()) continue
            val box = cellBox ?: fillPaint().also { cellBox = it }
            val paint = cellLetter ?: textPaint().also { cellLetter = it; it.color = -1 }
            box.color = cells.color[cell]
            val half = cw / 2.0f
            canvas.drawRoundRect(dstRect, half, ch / 2.0f, box)
            paint.textSize = half
            canvas.drawText(initial, left + half, top + half - (paint.descent() + paint.ascent()) / 2.0f, paint)
        }
    }

    private fun neutral(color: Int, dark: Boolean): Int = when {
        color == 0 -> if (dark) -14606047 else -460552
        dark && color == -460552 -> -14606047
        else -> color
    }

    /** Letters on grey circles follow dark mode ([darkColor]); on coloured circles they are white. */
    private fun letterColor(circle: Int, dark: Boolean, darkColor: Int): Int =
        if (circle == 0 || circle == -460552 || circle == -11513776 || circle == -14606047) {
            if (dark) darkColor else -16777216
        } else {
            -1
        }

    private fun fillPaint() = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private fun textPaint() = fillPaint().apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private companion object {
        val DARK_ICONS = mapOf(
            AppR.drawable.outline_draft_black_24 to AppR.drawable.outline_draft_dark_24,
            AppR.drawable.outline_image_black_24 to AppR.drawable.outline_image_dark_24,
            AppR.drawable.baseline_play_arrow_black_24 to AppR.drawable.baseline_play_arrow_dark_24,
            AppR.drawable.baseline_music_note_black_24 to AppR.drawable.baseline_music_note_dark_24,
            AppR.drawable.outline_folder_zip_black_24 to AppR.drawable.outline_folder_zip_dark_24,
            AppR.drawable.outline_verified_user_black_24 to AppR.drawable.outline_verified_user_dark_24,
            AppR.drawable.outline_local_library_black_24 to AppR.drawable.outline_local_library_dark_24,
            AppR.drawable.outline_picture_as_pdf_black_24 to AppR.drawable.outline_picture_as_pdf_dark_24,
            AppR.drawable.outline_description_black_24 to AppR.drawable.outline_description_dark_24,
            AppR.drawable.outline_public_black_24 to AppR.drawable.outline_public_dark_24,
            AppR.drawable.outline_android_black_24 to AppR.drawable.outline_android_dark_24,
            AppR.drawable.outline_search_black_24 to AppR.drawable.outline_search_dark_24,
            AppR.drawable.outline_find_in_page_black_24 to AppR.drawable.outline_find_in_page_dark_24,
            AppR.drawable.outline_text_snippet_black_24 to AppR.drawable.outline_text_snippet_dark_24,
            AppR.drawable.outline_kid_star_black_24 to AppR.drawable.outline_kid_star_dark_24,
            AppR.drawable.baseline_star_add_black_24 to AppR.drawable.baseline_star_add_dark_24,
            AppR.drawable.outline_home_black_24 to AppR.drawable.outline_home_dark_24,
            AppR.drawable.outline_offline_pin_black_24 to AppR.drawable.outline_offline_pin_dark_24,
            AppR.drawable.outline_shift_2_black_24 to AppR.drawable.outline_shift_2_dark_24,
            AppR.drawable.outline_folder_black_24 to AppR.drawable.outline_folder_dark_24,
            AppR.drawable.outline_extension_black_24 to AppR.drawable.outline_extension_dark_24,
        )
    }
}
