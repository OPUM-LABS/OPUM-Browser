package com.mycompany.app.web

import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Rect
import androidx.core.graphics.drawable.toDrawable
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import com.mycompany.app.pref.store.SyncSettings
import java.lang.ref.WeakReference

/**
 * The picture shown while a tab's page loads again (2026-11-24). A page released to save memory (memory limits, or
 * Android asking for memory back) reloads when its tab is shown; until it has loaded and scrolled back, the tab shows
 * how it looked when it was left, instead of an empty page.
 *
 * The picture is copied from the screen (PixelCopy, off the main thread) when a tab is left, when its page has loaded,
 * and when the tab overview opens, at half size, in memory only, and only for the last [KEEP] tabs. Never in incognito
 * mode. It is only shown for the same address scrolled to the same place ([matches]). The tab frame draws it in place
 * of its pages (not as a child view: the frame counts its children as pages) and it fades out once the page has
 * loaded and scrolled back; a touch removes it at once.
 */
object ReloadCover {
    private const val TAG = "OpumCover"

    /** Pictures kept: the tabs left most recently. */
    const val KEEP = 5

    /** Screen pixels to picture pixels. */
    private const val SCALE = 0.5f

    /** The longest a picture covers a page that is still loading. */
    private const val MAX_SHOW_MS = 5000L

    private const val FADE_MS = 180L

    /** A picture of a frame [width] x [height], showing [url] scrolled to [scrollY]. */
    class Shot(val bitmap: Bitmap, val width: Int, val height: Int, val url: String, val scrollY: Int)

    private val recent = ArrayDeque<WeakReference<WebNestFrame>>()
    private val main by lazy { Handler(Looper.getMainLooper()) }

    /** Which frames keep their picture when [frame] gets a new one: the newest [keep], [frame] first. */
    fun <T> keepOrder(order: List<T>, frame: T, keep: Int): Pair<List<T>, List<T>> {
        val next = listOf(frame) + order.filter { it != frame }
        return next.take(keep) to next.drop(keep)
    }

    /**
     * Whether a picture of [shotUrl] at [shotScroll] shows the page that loads again: the same address, scrolled to
     * about the same place (a tab left in mid-swipe keeps an older picture, which may show another page).
     */
    fun matches(shotUrl: String?, shotScroll: Int, url: String?, scrollY: Int): Boolean {
        if (shotUrl.isNullOrEmpty() || url.isNullOrEmpty()) return false
        return shotUrl.trimEnd('/') == url.trimEnd('/') && Math.abs(shotScroll - scrollY) <= 16
    }

    /** Whether a picture taken at [shotW] x [shotH] still fits the frame at [w] x [h] (not after turning the phone). */
    fun fits(shotW: Int, shotH: Int, w: Int, h: Int): Boolean =
        shotW > 0 && shotH > 0 && w == shotW && Math.abs(h - shotH) <= shotH / 20

    /** Copies what [frame] shows now from the screen. Only while it is on screen, in place, and not in incognito mode. */
    fun take(frame: WebNestFrame) {
        try {
            if (SyncSettings.secretMode.value) return
            val activity = frame.context as? Activity ?: return
            if (!frame.isShown || frame.width <= 0 || frame.height <= 0) return
            // mid-swipe the frame is moved aside: the screen no longer shows it where it belongs
            if (frame.translationX != 0f || frame.translationY != 0f) return
            val page = frame.pageValid ?: return
            if (page.isNotLive() || page.loading) return
            val url = page.pageUrl ?: return
            val scrollY = page.scrollY
            val spot = IntArray(2)
            frame.getLocationInWindow(spot)
            val rect = Rect(spot[0], spot[1], spot[0] + frame.width, spot[1] + frame.height)
            val window = activity.window ?: return
            val decor = window.peekDecorView() ?: return
            if (!Rect(0, 0, decor.width, decor.height).contains(rect)) return
            val w = frame.width
            val h = frame.height
            val bitmap = androidx.core.graphics.createBitmap(Math.round(w * SCALE).coerceAtLeast(1), Math.round(h * SCALE).coerceAtLeast(1))
            PixelCopy.request(window, rect, bitmap, { result ->
                if (result != PixelCopy.SUCCESS || TabSnapshot.isBlank(bitmap)) {
                    bitmap.recycle()
                    return@request
                }
                // half the memory: pictures have no transparency
                val small = bitmap.copy(Bitmap.Config.RGB_565, false)
                bitmap.recycle()
                if (small != null) store(frame, Shot(small, w, h, url, scrollY))
            }, main)
        } catch (_: Exception) {
        } catch (_: OutOfMemoryError) {
        }
    }

    private fun store(frame: WebNestFrame, shot: Shot) {
        // the old picture is not recycled by hand: it may still be fading out; the collector frees it
        frame.coverShot = shot
        val frames = recent.mapNotNull { it.get() }
        val (keep, drop) = keepOrder(frames, frame, KEEP)
        for (old in drop) old.dropCover()
        recent.clear()
        keep.forEach { recent.addLast(WeakReference(it)) }
    }

    /** A page of [frame] is being rebuilt to load [url] again at [scrollY]: cover it with its picture, if it fits. */
    fun show(frame: WebNestFrame, url: String?, scrollY: Int) {
        val shot = frame.coverShot ?: return
        if (frame.coverDrawable != null) return
        if (!matches(shot.url, shot.scrollY, url, scrollY) || !fits(shot.width, shot.height, frame.width, frame.height)) {
            android.util.Log.i(TAG, "picture skipped: the page moved on since")
            return
        }
        try {
            val drawable = shot.bitmap.toDrawable(frame.resources)
            drawable.setBounds(0, 0, frame.width, frame.height)
            frame.coverDrawable = drawable
            frame.invalidate()
            android.util.Log.i(TAG, "picture shown while the page loads again")
            main.postDelayed({ if (frame.coverDrawable === drawable) hide(frame, true) }, MAX_SHOW_MS)
        } catch (_: Exception) {
        }
    }

    /** The page has loaded and scrolled back (or the user touched it): the picture goes, faded or at once. */
    fun hide(frame: WebNestFrame, fade: Boolean) {
        val drawable = frame.coverDrawable ?: return
        frame.coverDrawable = null
        android.util.Log.i(TAG, if (fade) "picture fades out" else "picture removed")
        if (!fade) {
            frame.invalidate()
            return
        }
        val anim = ValueAnimator.ofInt(255, 0).setDuration(FADE_MS)
        // fading: the frame draws the page and the picture over it until the picture is gone
        frame.coverFading = drawable
        anim.addUpdateListener { drawable.alpha = it.animatedValue as Int; frame.invalidate() }
        anim.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                if (frame.coverFading === drawable) frame.coverFading = null
                frame.invalidate()
            }
        })
        anim.start()
    }

    /** The page in [view] finished loading: hide its frame's picture once the scroll-back had a moment. */
    fun pageFinished(view: View?) {
        val frame = (view as? WebNestView)?.parent as? WebNestFrame ?: return
        if (frame.coverDrawable == null) return
        main.postDelayed({ hide(frame, true) }, 350L)
    }
}
