package com.mycompany.app.web

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

/**
 * Tab overview snapshots: size, encoding and when they are taken.
 *
 * A snapshot is the visible part of the page drawn at [SCALE] into an RGB_565 bitmap, cut to at most
 * [MAX_ASPECT] times its width (the overview card shows only the top of it), and stored as lossy WebP in
 * `DbTabThumb` (older rows are PNG; both decode the same way).
 */
object TabSnapshot {
    /** Page pixels to snapshot pixels (a 1080 px wide page gives a 432 px wide snapshot, about the card width). */
    const val SCALE = 0.4f

    /** Height at most this times the width; the cards are about 1.2 times as tall as wide and crop the rest. */
    const val MAX_ASPECT = 1.5f

    /** WebP quality; snapshots are only shown small, 70 looks the same as the old PNG at a fraction of the size. */
    const val QUALITY = 70

    /**
     * After a page finished loading its snapshot is taken twice: soon, and again once pages that draw late
     * (single-page apps) are in. A blank capture is never stored, so the later one or an older snapshot stays.
     */
    @JvmField
    val AFTER_LOAD_MS = longArrayOf(1500L, 6000L)

    /**
     * Memory cache key of tab [uid]'s snapshot ([storedKey] is the one kept in the database). Tabs that show the
     * same address, such as the start page in every workspace, each keep their own picture.
     */
    @JvmStatic
    fun memoryKey(uid: Long, storedKey: String?): String? = storedKey?.let { "$uid:$it" }

    /** Snapshot size for a page of [width] x [height] pixels; null when there is nothing to draw. */
    @JvmStatic
    fun size(width: Int, height: Int): Pair<Int, Int>? {
        if (width <= 0 || height <= 0) return null
        val w = Math.round(width * SCALE)
        val h = minOf(Math.round(height * SCALE), Math.round(w * MAX_ASPECT))
        return if (w > 0 && h > 0) w to h else null
    }

    /**
     * The top part of [page] copied from the screen (2026-11-27): the real pixels, as the page shows them. Redrawing the
     * WebView into a canvas (the old way, still the fallback) misplaces some layers: on YouTube the action buttons
     * landed above the Subscribe row, the part under the video looked squeezed. Same [SCALE] and shape as before.
     * [done] runs on the main thread with the picture, or null when the page is not on screen or the copy fails.
     */
    @JvmStatic
    fun copyFromScreen(activity: android.app.Activity, page: android.view.View, done: (Bitmap?) -> Unit) {
        val window = activity.window
        if (window == null || !page.isAttachedToWindow || !page.isShown || page.width <= 0 || page.height <= 0) { done(null); return }
        val size = size(page.width, page.height) ?: run { done(null); return }
        val at = IntArray(2)
        page.getLocationInWindow(at)
        val decor = window.decorView
        val src = android.graphics.Rect(at[0], at[1], at[0] + page.width, at[1] + Math.round(size.second / SCALE))
        if (!src.intersect(0, 0, decor.width, decor.height) || src.width() <= 0 || src.height() <= 0) { done(null); return }
        val bitmap = try {
            androidx.core.graphics.createBitmap(Math.round(src.width() * SCALE).coerceAtLeast(1), Math.round(src.height() * SCALE).coerceAtLeast(1))
        } catch (_: OutOfMemoryError) { done(null); return }
        try {
            android.view.PixelCopy.request(window, src, bitmap, { result ->
                done(if (result == android.view.PixelCopy.SUCCESS && !isBlank(bitmap)) bitmap else null)
            }, android.os.Handler(android.os.Looper.getMainLooper()))
        } catch (_: Exception) {
            done(null)
        }
    }

    /** [bitmap] as lossy WebP; null if encoding fails. */
    @JvmStatic
    fun encode(bitmap: Bitmap): ByteArray? = try {
        ByteArrayOutputStream(16 * 1024).use { out ->
            if (bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, QUALITY, out)) out.toByteArray().takeIf { it.isNotEmpty() } else null
        }
    } catch (_: Exception) {
        null
    }

    /** Decodes a stored snapshot (WebP or old PNG) as RGB_565: half the memory, snapshots have no alpha. */
    @JvmStatic
    fun decode(bytes: ByteArray?): Bitmap? {
        if (bytes == null || bytes.isEmpty()) return null
        return try {
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.RGB_565 })
        } catch (_: Exception) {
            null
        } catch (_: OutOfMemoryError) {
            null
        }
    }

    /** True when every sampled pixel has the same colour: the page had not drawn yet (or is empty). */
    @JvmStatic
    fun isBlank(bitmap: Bitmap): Boolean {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= 0 || h <= 0) return true
        val first = bitmap.getPixel(0, 0)
        for (y in 0 until 24) for (x in 0 until 16) {
            if (bitmap.getPixel((x * (w - 1)) / 15, (y * (h - 1)) / 23) != first) return false
        }
        return true
    }

    /**
     * Whether a stored snapshot (its cache key [storedKey], `url + deskMode + secret + "_2"`) still fits a tab now
     * at [url]: same site. A tab that went to another site since shows no snapshot instead of the old site's.
     */
    @JvmStatic
    fun sameSite(storedKey: String?, url: String?): Boolean {
        if (storedKey.isNullOrEmpty() || url.isNullOrEmpty()) return false
        val storedUrl = storedKey.replace(Regex("(true|false)(true|false)_2$"), "")
        return siteOf(storedUrl) == siteOf(url)
    }

    private fun siteOf(url: String): String {
        // the start page and saved pages (file:// or a path): the page itself
        if (!url.startsWith("http://") && !url.startsWith("https://")) return url.substringBefore('?').substringBefore('#')
        val rest = url.substringAfter("://", url)
        return rest.substringBefore('/').substringBefore('?').substringBefore('#').substringAfterLast('@').lowercase().removePrefix("www.").removePrefix("m.")
    }

    private val pending = ArrayList<Runnable>()

    /**
     * Takes snapshots of the current tab [AFTER_LOAD_MS] after its page finished loading, so every visited tab has
     * one in the overview (before, only the tab that was open when the overview opened got a new one).
     * A newer load replaces pending ones.
     */
    @JvmStatic
    fun afterLoad(activity: WebViewActivity) {
        val coord = activity.webCoord ?: return
        for (old in pending) coord.removeCallbacks(old)
        pending.clear()
        val page = activity.currentPage ?: return
        for (delay in AFTER_LOAD_MS) {
            lateinit var task: Runnable
            task = Runnable {
                pending.remove(task)
                // only if the same page is still shown and nothing covers it
                if (activity.isClosing || activity.isPaused || activity.currentPage !== page || page.loading) return@Runnable
                if (activity.tabsState.composeOverview != null) return@Runnable
                activity.captureTabPreview(activity.currentTabIndex)
            }
            pending.add(task)
            coord.postDelayed(task, delay)
        }
    }
}
