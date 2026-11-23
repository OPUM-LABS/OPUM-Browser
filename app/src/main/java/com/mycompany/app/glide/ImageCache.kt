package com.mycompany.app.glide

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Looper
import android.util.LruCache
import com.bumptech.glide.Glide
import com.mycompany.app.main.MainItem
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * The app's in-memory image caches (formerly part of Universal Image Loader):
 * - [get] / [put] / [remove]: bitmaps by [key] (`"<url>_<type>"`), an LRU of
 *   1/8 of the app's memory class counted in bytes. Thumbnails, favicons and page captures share it.
 * - [listGet] / [listPut]: the 2 MB LRU the list loader (`MainListLoader`) keeps its row icons in.
 * - [cachedFile]: the downloaded original of a web image, from Glide's disk cache (replaces the old disk cache that
 *   `Compress` read when it builds ZIPs / albums from images already seen).
 * [init] runs once from `MainApp` with the application context.
 */
// holds only the application context (see [init]), which lives as long as the process anyway
@android.annotation.SuppressLint("StaticFieldLeak")
object ImageCache {
    private var main: LruCache<String, Bitmap>? = null
    private var list: LruCache<String, Bitmap>? = null
    @Volatile private var app: Context? = null

    /** The image types the old loader used in its keys (flag combinations); [removeUrl] drops all of them. */
    @JvmField val TYPES = intArrayOf(2, 4, 8, 24, 136, 264, 152, 280, 392, 408, 40, 56, 168, 296, 184, 312, 424, 440, 72, 88, 200, 328, 216, 344, 456, 472)

    @JvmStatic
    @Synchronized
    fun init(context: Context) {
        if (main != null) return
        val appContext = context.applicationContext
        app = appContext
        val am = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        var memoryClass = am.memoryClass
        if ((appContext.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_LARGE_HEAP) != 0) memoryClass = am.largeMemoryClass
        main = bytesCache((memoryClass shl 20) / 8)
        list = bytesCache(2 * 1024 * 1024)
        ThumbLoader.appContext = appContext
        // the old loader's disk cache (cache/images, up to 512 MB) is no longer read or written
        val old = File(appContext.cacheDir, "images")
        if (old.isDirectory) Thread { old.deleteRecursively() }.start()
    }

    private fun bytesCache(bytes: Int) = object : LruCache<String, Bitmap>(bytes) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount
    }

    /** The cache key of [url] for image [type] (same format as the old loader, so stored keys keep working). */
    @JvmStatic fun key(type: Int, url: String?): String? = if (url.isNullOrEmpty()) null else "${url}_$type"

    @JvmStatic fun get(key: String?): Bitmap? = key?.let { main?.get(it) }?.takeUnless { it.isRecycled }
    @JvmStatic fun put(key: String?, bitmap: Bitmap?) { if (key != null && bitmap != null && !bitmap.isRecycled) main?.put(key, bitmap) }
    @JvmStatic fun remove(key: String?) { if (key != null) main?.remove(key) }

    /** Forgets every cached size of [url], e.g. after the file changed or was deleted. */
    @JvmStatic fun removeUrl(url: String?) {
        if (url.isNullOrEmpty()) return
        for (t in TYPES) main?.remove("${url}_$t")
    }

    /** Empties both memory caches ("clear cache" in privacy settings and on exit). */
    @JvmStatic fun clearMemory() {
        main?.evictAll()
        list?.evictAll()
    }

    /** Under memory pressure: halve the main cache and empty the list cache. */
    @JvmStatic fun trim() {
        main?.let { it.trimToSize(it.maxSize() / 2) }
        list?.evictAll()
    }

    @JvmStatic fun listGet(key: String?): Bitmap? = key?.let { list?.get(it) }?.takeUnless { it.isRecycled }
    @JvmStatic fun listPut(key: String?, bitmap: Bitmap?) { if (key != null && bitmap != null) list?.put(key, bitmap) }

    /**
     * The file Glide downloaded for the web image [url], if it is in the disk cache; never downloads. Null on the main
     * thread (the lookup blocks) and when it is not cached.
     */
    @JvmStatic fun cachedFile(url: String?): File? {
        val context = app ?: return null
        if (url.isNullOrEmpty() || Looper.myLooper() == Looper.getMainLooper()) return null
        val item = MainItem.ViewItem()
        item.itemType = 7.toByte()
        item.url = url
        return try {
            Glide.with(context).downloadOnly().load(item).onlyRetrieveFromCache(true).submit().get(5, TimeUnit.SECONDS)
        } catch (_: Exception) {
            null
        }
    }
}
