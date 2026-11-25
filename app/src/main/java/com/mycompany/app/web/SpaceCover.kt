package com.mycompany.app.web

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.PixelCopy
import android.view.View
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.doOnLayout
import com.mycompany.app.main.MainApp
import com.mycompany.app.pref.store.SyncSettings
import java.io.File
import java.lang.ref.WeakReference

/**
 * The picture of a space as it was left, shown the moment the browser opens that space again (2026-11-25).
 *
 * When a space is left, the screen (bars and page) is copied at half size and kept as a small WebP in the app's own
 * cache folder, one per space (Main and the workspaces; never incognito). Left from the tab overview, the copy taken as
 * the overview opened is used. Right after a switch ([arm]) the new browser screen starts with it as its window
 * background, so its first frame already looks like the space; the page area shows its part until the tab's frame
 * takes it over, which keeps it until the page has loaded ([ReloadCover]'s cover, fading out).
 */
object SpaceCover {
    private const val TAG = "OpumCover"
    private const val DIR = "space_cover"

    /** A switch older than this did not lead to the browser starting: no picture then. */
    private const val ARM_MS = 15000L
    private const val MAX_SHOW_MS = 5000L

    private val main by lazy { Handler(Looper.getMainLooper()) }

    /**
     * The picture file's data: the tab it shows, where that tab's frame was in the window ([frame]), the window size,
     * and the page address.
     */
    data class Meta(
        val tab: Int, val left: Int, val top: Int, val right: Int, val bottom: Int, val width: Int, val height: Int, val url: String,
    ) {
        fun frame() = Rect(left, top, right, bottom)
    }

    fun formatMeta(meta: Meta): String = listOf(
        meta.tab, meta.left, meta.top, meta.right, meta.bottom, meta.width, meta.height, meta.url,
    ).joinToString("\n")

    fun parseMeta(text: String?): Meta? {
        val parts = text?.split('\n', limit = 8) ?: return null
        if (parts.size < 8 || parts[7].isEmpty()) return null
        val n = parts.take(7).map { it.toIntOrNull() ?: return null }
        if (n[3] <= n[1] || n[4] <= n[2] || n[5] <= 0 || n[6] <= 0) return null
        return Meta(n[0], n[1], n[2], n[3], n[4], n[5], n[6], parts[7])
    }

    /** Whether the switch armed at [armedAt] is the one this start belongs to. */
    fun armedFor(armedAt: Long, now: Long): Boolean = armedAt in (now - ARM_MS)..now

    private fun dir(context: Context) = File(context.cacheDir, DIR)
    private fun image(context: Context, space: Int) = File(dir(context), "$space.webp")
    private fun meta(context: Context, space: Int) = File(dir(context), "$space.txt")
    private fun armFile(context: Context) = File(dir(context), "armed")

    /** A copy of the screen with its page's frame at [frame] showing [url]. */
    private class Shot(val bitmap: Bitmap, val frame: Rect, val width: Int, val height: Int, val url: String)

    /** The copy taken as the tab overview opened (it then covers the page). */
    private var opened: Shot? = null

    /** The tab overview is opening over [wa]'s page: copies the screen as it is now, for a switch made from there. */
    fun takeOnOverview(wa: WebViewActivity) {
        opened = null
        if (SyncSettings.secretMode.value) return
        copyScreen(wa) { shot -> opened = shot }
    }

    /** Copies the screen at half size, with where the open tab's frame is; null when it cannot be had. */
    private fun copyScreen(wa: WebViewActivity, result: (Shot?) -> Unit) {
        try {
            val frame = wa.tabsState.currentFrame ?: return result(null)
            val url = wa.currentPage?.pageUrl
            // the start page draws at once by itself: no picture needed
            if (url.isNullOrEmpty() || url.startsWith("file:///android_asset/")) return result(null)
            if (!frame.isShown || frame.width <= 0 || frame.height <= 0) return result(null)
            if (frame.translationX != 0f || frame.translationY != 0f) return result(null)
            val window = wa.window ?: return result(null)
            val decor = window.peekDecorView() ?: return result(null)
            if (decor.width <= 0 || decor.height <= 0) return result(null)
            val spot = IntArray(2)
            frame.getLocationInWindow(spot)
            val rect = Rect(spot[0], spot[1], spot[0] + frame.width, spot[1] + frame.height)
            if (!Rect(0, 0, decor.width, decor.height).contains(rect)) return result(null)
            val width = decor.width
            val height = decor.height
            val bitmap = androidx.core.graphics.createBitmap((width / 2).coerceAtLeast(1), (height / 2).coerceAtLeast(1))
            PixelCopy.request(window, bitmap, { code ->
                if (code != PixelCopy.SUCCESS || TabSnapshot.isBlank(bitmap)) {
                    bitmap.recycle()
                    result(null)
                } else {
                    // half the memory: no transparency
                    val small = bitmap.copy(Bitmap.Config.RGB_565, false)
                    bitmap.recycle()
                    result(small?.let { Shot(it, rect, width, height, url) })
                }
            }, main)
        } catch (_: Exception) {
            result(null)
        } catch (_: OutOfMemoryError) {
            result(null)
        }
    }

    /**
     * Keeps the picture of [space] (it is being left, with tab [tab] open). [done] runs on the main thread once it is
     * written, or at once when there is none to take (an older one of the space is deleted then).
     */
    fun keep(wa: WebViewActivity, space: Int, tab: Int, done: Runnable) {
        val context = wa.applicationContext
        val url = wa.currentPage?.pageUrl
        val take = { shot: Shot? ->
            if (shot == null || url.isNullOrEmpty() || shot.url.trimEnd('/') != url.trimEnd('/')) {
                forget(context, space)
                done.run()
            } else {
                val f = shot.frame
                write(context, space, shot.bitmap, Meta(tab, f.left, f.top, f.right, f.bottom, shot.width, shot.height, shot.url), done)
            }
        }
        if (wa.tabsState.composeOverview != null) {
            // the overview covers the page: the copy taken as it opened shows it
            take(opened)
            return
        }
        copyScreen(wa, take)
    }

    private fun write(context: Context, space: Int, bitmap: Bitmap, meta: Meta, done: Runnable) {
        MainApp.runInBackground(context, Runnable {
            try {
                dir(context).mkdirs()
                val bytes = TabSnapshot.encode(bitmap)
                if (bytes == null) {
                    forgetNow(context, space)
                } else {
                    image(context, space).writeBytes(bytes)
                    meta(context, space).writeText(formatMeta(meta))
                }
            } catch (e: Exception) {
                Log.w(TAG, "space picture not kept: ${e.message}")
                forgetNow(context, space)
            }
            main.post(done)
        })
    }

    /** Deletes the picture of [space] (deleted workspace, or nothing to show). */
    fun forget(context: Context, space: Int) {
        MainApp.runInBackground(context, Runnable { forgetNow(context, space) })
    }

    private fun forgetNow(context: Context, space: Int) {
        try {
            image(context, space).delete()
            meta(context, space).delete()
        } catch (_: Exception) {
        }
    }

    /** The browser is about to open [space]: its next start shows the space's picture. */
    fun arm(context: Context, space: Int) {
        try {
            dir(context).mkdirs()
            armFile(context).writeText("$space\n${System.currentTimeMillis()}")
        } catch (_: Exception) {
        }
    }

    /** The space the switch this start belongs to opens, or null. */
    private fun armedSpace(context: Context): Int? {
        val parts = try {
            armFile(context).takeIf { it.exists() }?.readText()?.split('\n')
        } catch (_: Exception) {
            null
        } ?: return null
        val space = parts.getOrNull(0)?.toIntOrNull() ?: return null
        return if (armedFor(parts.getOrNull(1)?.toLongOrNull() ?: 0L, System.currentTimeMillis())) space else null
    }

    private fun load(context: Context, space: Int, tab: Int): Pair<Meta, Bitmap>? {
        val meta = try {
            parseMeta(meta(context, space).takeIf { it.exists() }?.readText())
        } catch (_: Exception) {
            null
        } ?: return null
        if (meta.tab != tab) return null
        val bitmap = try {
            BitmapFactory.decodeFile(image(context, space).path, BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.RGB_565 })
        } catch (_: Throwable) {
            null
        } ?: return null
        return meta to bitmap
    }

    /** Draws the part of [bitmap] (a half-size screen copy) where [frame] (full-size window pixels) was. */
    private class Part(val bitmap: Bitmap, frame: Rect) : Drawable() {
        private val source = Rect(frame.left / 2, frame.top / 2, frame.right / 2, frame.bottom / 2)
        private val paint = Paint(Paint.FILTER_BITMAP_FLAG)

        override fun draw(canvas: Canvas) {
            canvas.drawBitmap(bitmap, source, bounds, paint)
        }

        override fun setAlpha(alpha: Int) {
            paint.alpha = alpha
            invalidateSelf()
        }

        override fun setColorFilter(colorFilter: ColorFilter?) {
            paint.colorFilter = colorFilter
        }

        @Deprecated("Deprecated in Java")
        override fun getOpacity(): Int = PixelFormat.OPAQUE
    }

    /** The picture of this start: window background and over the page area, until the tab's frame takes over. */
    private class Early(
        val space: Int, val meta: Meta, val bitmap: Bitmap,
        val activity: WeakReference<Activity>, val windowDrawable: Drawable, val windowBefore: Drawable?,
        val area: WeakReference<View>, val part: Drawable,
    )

    private var early: Early? = null

    /**
     * The browser screen of [activity] was just built for [space], with [area] where the pages go: right after a
     * switch ([arm]), the screen as the space was left shows from the first frame on (window background, and its page
     * part over [area]) until [showOnStart] hands the page part to the tab's frame. Main thread, before the first frame.
     */
    fun showEarly(activity: Activity, area: View, space: Int, tab: Int) {
        val context = activity.applicationContext
        if (armedSpace(context) != space) return
        val (meta, bitmap) = load(context, space, tab) ?: return
        val window = activity.window ?: return
        val decor = window.decorView
        val windowDrawable = bitmap.toDrawable(activity.resources)
        val part = Part(bitmap, meta.frame())
        val shown = Early(space, meta, bitmap, WeakReference(activity), windowDrawable, decor.background, WeakReference(area), part)
        early = shown
        window.setBackgroundDrawable(windowDrawable)
        decor.doOnLayout {
            if (early !== shown) return@doOnLayout
            if (decor.width != meta.width || decor.height != meta.height) {
                Log.i(TAG, "space picture skipped: other size")
                dropEarly()
            }
        }
        area.doOnLayout {
            if (early !== shown) return@doOnLayout
            val spot = IntArray(2)
            area.getLocationInWindow(spot)
            val bounds = meta.frame()
            bounds.offset(-spot[0], -spot[1])
            if (bounds.left < 0 || bounds.top < 0 || bounds.right > area.width || bounds.bottom > area.height) return@doOnLayout
            part.bounds = bounds
            area.overlay.add(part)
        }
        Log.i(TAG, "space picture shown from the start")
        main.postDelayed({ if (early === shown) dropEarly() }, MAX_SHOW_MS)
    }

    private fun dropEarly() {
        val shown = early ?: return
        early = null
        try {
            shown.area.get()?.overlay?.remove(shown.part)
            val window = shown.activity.get()?.window
            if (window != null && window.decorView.background === shown.windowDrawable) window.setBackgroundDrawable(shown.windowBefore)
        } catch (_: Exception) {
        }
    }

    /**
     * The browser starts with [frame] for the open tab ([tab]) of [space]: right after a switch, it keeps showing the
     * picture of that tab until its page has loaded (taking it over from [showEarly]). Main thread.
     */
    fun showOnStart(frame: WebNestFrame, space: Int, tab: Int) {
        val context = frame.context.applicationContext
        val armed = armedSpace(context)
        try {
            armFile(context).delete()
        } catch (_: Exception) {
        }
        val kept = early
        if (armed != space) {
            dropEarly()
            return
        }
        val (meta, bitmap) = (if (kept != null && kept.space == space) kept.meta to kept.bitmap else load(context, space, tab)) ?: return
        frame.doOnLayout {
            // the frame draws it from now on; the early copy goes once the frame has drawn
            frame.post { dropEarly() }
            if (!ReloadCover.fits(meta.right - meta.left, meta.bottom - meta.top, frame.width, frame.height)) {
                Log.i(TAG, "space picture skipped: other size")
                return@doOnLayout
            }
            if (frame.coverDrawable != null) return@doOnLayout
            val drawable = Part(bitmap, meta.frame())
            drawable.setBounds(0, 0, frame.width, frame.height)
            frame.coverDrawable = drawable
            frame.invalidate()
            Log.i(TAG, "space picture shown while the page loads")
            main.postDelayed({ if (frame.coverDrawable === drawable) ReloadCover.hide(frame, true) }, MAX_SHOW_MS)
        }
    }
}
