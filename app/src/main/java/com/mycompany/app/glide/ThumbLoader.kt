package com.mycompany.app.glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem
import com.mycompany.app.main.ViewItemListener
import java.util.WeakHashMap

/**
 * Loads the app's item images ([MainItem.ViewItem], any kind: see [ViewItemLoader]) with Glide. It replaced
 * Universal Image Loader (2026-11-16) and keeps its contract:
 * - the result goes into, and is first looked up in, [ImageCache] under [ImageCache.key] (`item.t`, `item.q`), which
 *   other code also reads and writes;
 * - [ViewItemListener.onComplete] / [ViewItemListener.onFailed] run on the main thread (synchronously on a cache hit);
 * - the old calls used a "display nothing" displayer, so [view] is only an identity: when it is bound to a newer
 *   item before this one finishes, the stale result is dropped and [ViewItemListener.onStale] is called, as the old loader did.
 * Glide decodes into its bitmap pool and recycles that bitmap when the request is released, so the cache and the
 * listener get a private copy.
 */
object ThumbLoader {
    private val main = Handler(Looper.getMainLooper())
    private val pending = WeakHashMap<View, String>()
    /** Bumped by [stopAll]; a load only delivers if no stop happened since it started. */
    @Volatile private var stopped = 0

    /** Set by `MainApp` when the old loader is configured; used by calls that pass no view. */
    @JvmStatic @Volatile var appContext: Context? = null

    @JvmStatic
    fun load(context: Context, item: MainItem.ViewItem, view: View?, listener: ViewItemListener?) {
        val url = item.url ?: return
        val key = ImageCache.key(item.cacheType, url) ?: return
        val cached = ImageCache.get(key)
        if (cached != null && !cached.isRecycled) {
            if (view != null) pending.remove(view)
            listener?.onComplete(item, view, cached)
            return
        }
        if (view != null) pending[view] = key
        val generation = stopped
        var width = view?.width ?: 0
        var height = view?.height ?: 0
        if (width <= 0 || height <= 0) {
            if (view != null && item.cacheType == 2) {
                width = MainApp.dp64; height = MainApp.dp64
            } else {
                val dm = context.resources.displayMetrics
                width = dm.widthPixels; height = dm.heightPixels
            }
        }
        val app = context.applicationContext
        val target = object : CustomTarget<Bitmap>(width.coerceAtLeast(1), height.coerceAtLeast(1)) {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                val copy = try { resource.copy(resource.config ?: Bitmap.Config.ARGB_8888, false) } catch (_: Throwable) { null }
                val target = this
                main.post { Glide.with(app).clear(target) }
                if (copy == null) { fail(item, view, key, listener, generation); return }
                ImageCache.put(key, copy)
                deliver(view, key, listener, generation) { listener?.onComplete(item, view, copy) }
            }

            override fun onLoadFailed(errorDrawable: Drawable?) = fail(item, view, key, listener, generation)
            override fun onLoadCleared(placeholder: Drawable?) {}
        }
        Glide.with(app).asBitmap().load(item.copyForGlide())
            .downsample(DownsampleStrategy.CENTER_INSIDE)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .skipMemoryCache(true)
            .into(target)
    }

    private fun fail(item: MainItem.ViewItem, view: View?, key: String, listener: ViewItemListener?, generation: Int) {
        deliver(view, key, listener, generation) { listener?.onFailed(item, view, java.io.IOException("image load failed")) }
    }

    /** Runs [block] on the main thread unless [view] has been bound to another item meanwhile. */
    private fun deliver(view: View?, key: String, listener: ViewItemListener?, generation: Int, block: () -> Unit) {
        val run = Runnable {
            if (generation != stopped) return@Runnable
            if (view != null) {
                if (pending[view] != key) { listener?.onStale(view); return@Runnable }
                pending.remove(view)
            }
            block()
        }
        if (Looper.myLooper() == Looper.getMainLooper()) run.run() else main.post(run)
    }

    /** Loads [item] for [view] (identity only: the listener sets the picture); see the class comment. */
    @JvmStatic
    fun loadFor(item: MainItem.ViewItem?, view: View?, listener: ViewItemListener?) {
        if (item == null) return
        val context = view?.context ?: appContext ?: return
        load(context, item, view, listener)
    }

    /** Loads [item] without a view (the listener gets the bitmap). */
    @JvmStatic
    fun loadAlone(item: MainItem.ViewItem?, listener: ViewItemListener?) {
        if (item == null) return
        load(appContext ?: return, item, null, listener)
    }

    /** Drops the results of every load still running (called when a list goes away). */
    @JvmStatic
    fun stopAll() {
        pending.clear()
        stopped++
    }

    /** Glide keeps the model while loading; the app reuses and mutates its items, so Glide gets its own copy. */
    private fun MainItem.ViewItem.copyForGlide(): MainItem.ViewItem {
        val viewItem = MainItem.ViewItem()
        viewItem.itemType = itemType; viewItem.url = url; viewItem.referer = referer; viewItem.filePath = filePath; viewItem.cacheType = cacheType; viewItem.position = position; viewItem.compress = compress
        return viewItem
    }
}
