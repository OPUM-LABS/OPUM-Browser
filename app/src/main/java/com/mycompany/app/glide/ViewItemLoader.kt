package com.mycompany.app.glide

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.bumptech.glide.signature.ObjectKey
import com.mycompany.app.main.MainItem
import com.mycompany.app.main.MainUtil
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection

/**
 * Glide source for the app's [MainItem.ViewItem] image items, opened the way the old Universal Image Loader's
 * `BaseImageDownloader` did, by item kind:
 * 1–3 database / cached icons (`filePath` is a cached file, else the source in `url`), 4 an app's icon, 6 a picture from a
 * content address, 7 a web image (`MainUtil.openHttpConnection`: the app's HTTP connection with User-Agent and Referer `r`, 5 s / 20 s
 * timeouts, up to 5 redirects, 200 only; or a `data:image/` URI), 8 an entry of an archive, 9 an asset,
 * 10 a raw resource, 11 a video frame, otherwise a file path (a video file gives its thumbnail before Android 10).
 * The cache key leaves out the Referer, which changes how a web image is fetched but not what it is.
 */
class ViewItemLoader(private val context: Context) : ModelLoader<MainItem.ViewItem, InputStream> {
    override fun handles(model: MainItem.ViewItem): Boolean = handlesItem(model)

    override fun buildLoadData(model: MainItem.ViewItem, width: Int, height: Int, options: Options): ModelLoader.LoadData<InputStream> =
        ModelLoader.LoadData(ObjectKey("${model.itemType}|${model.url}|${model.filePath}"), Fetcher(context, model))

    class Factory(private val context: Context) : ModelLoaderFactory<MainItem.ViewItem, InputStream> {
        override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<MainItem.ViewItem, InputStream> = ViewItemLoader(context.applicationContext)
        override fun teardown() {}
    }

    private class Fetcher(private val context: Context, private val item: MainItem.ViewItem) : DataFetcher<InputStream> {
        private val url: String? = item.url
        private val referer: String? = item.referer
        @Volatile private var connection: HttpURLConnection? = null
        @Volatile private var stream: InputStream? = null

        override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
            try {
                callback.onDataReady(open())
            } catch (glideException: Exception) {
                callback.onLoadFailed(glideException)
            }
        }

        private fun open(): InputStream = when (item.itemType.toInt()) {
            7 -> openWeb()
            else -> openLocal() ?: throw IOException("no image for item kind ${item.itemType}")
        }

        /** Every kind but web images; ported from Universal Image Loader's `BaseImageDownloader`. */
        private fun openLocal(): InputStream? {
            val url = item.url
            val filePath = item.filePath
            val hasCached = !filePath.isNullOrEmpty() && java.io.File(filePath).length() > 0
            return when (item.itemType.toInt()) {
                1 -> if (hasCached) MainUtil.urlInputStream(filePath) else MainUtil.urlInputStream(url)
                2 -> if (hasCached) MainUtil.urlInputStream(filePath) else MainUtil.bitmapToStream(MainUtil.videoFrameBitmap(context, url))
                3 -> when {
                    hasCached -> MainUtil.urlInputStream(filePath)
                    !url.isNullOrEmpty() -> MainUtil.embeddedPictureOf(context, url)?.let { java.io.ByteArrayInputStream(it) }
                    else -> null
                }
                4 -> {
                    if (url.isNullOrEmpty()) return null
                    if (item.filePath.isNullOrEmpty()) item.filePath = MainUtil.cacheFilePathFor(context, url)
                    if (java.io.File(item.filePath ?: "").length() > 0) return MainUtil.urlInputStream(item.filePath)
                    item.compress?.coverStream() ?: com.mycompany.app.compress.Compress.create(context, 1, url, null as String?).let { c ->
                        val stream = c.coverStream(); c.release(); stream
                    }
                }
                5 -> null
                6 -> if (url.isNullOrEmpty()) null else MainUtil.bitmapToStream(MainUtil.appIcon(context, url))
                8 -> if (url.isNullOrEmpty()) null else item.compress?.streamFor(url)
                9 -> if (url.isNullOrEmpty()) null else context.assets.open(url)
                10 -> if (url.isNullOrEmpty()) null else context.resources.openRawResource(url.toInt())
                11 -> if (url.isNullOrEmpty()) null else MainUtil.bitmapToStream(MainUtil.loadAlbumThumb(0, 0L, context, url))
                else -> {
                    if (url.isNullOrEmpty() || !java.io.File(url).isFile) return null
                    val ext = MainUtil.fileExtOf(url, false)
                    val mime = if (ext.isNullOrEmpty()) null else android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
                    val video = !ext.isNullOrEmpty() && (ext.equals("flv", true) || mime?.startsWith("video/") == true)
                    if (!video) return BufferedInputStream(MainUtil.openFileInput(url), 32768)
                    null
                }
            }
        }

        /** [openWeb] for callers outside Glide: the connection closes with the stream. */
        fun openWebForCaller(): InputStream = openWeb()

        private fun openWeb(): InputStream {
            val address = url
            if (TextUtils.isEmpty(address)) throw IOException("empty address")
            if (address!!.startsWith("data:image/")) {
                return MainUtil.bitmapToStream(MainUtil.decodeDataUri(context, address)) ?: throw IOException("undecodable data URI")
            }
            var conn = MainUtil.openHttpConnection(5000, 20000, context, address, referer, false)
            var hops = 0
            while (conn != null && conn.responseCode / 100 == 3 && hops < 5) {
                val next = conn.getHeaderField("Location") ?: break
                conn.disconnect()
                // a relative Location is resolved against the address that sent it
                conn = MainUtil.openHttpConnection(5000, 20000, context, java.net.URL(conn.url, next).toString(), referer, false)
                hops++
            }
            if (conn == null) throw IOException("no connection")
            connection = conn
            if (conn.responseCode != 200) throw IOException("Image request failed with response code " + conn.responseCode)
            return BufferedInputStream(conn.inputStream, 32768).also { stream = it }
        }

        override fun cleanup() {
            try { stream?.close() } catch (_: IOException) {}
            connection?.disconnect()
        }

        override fun cancel() {}
        override fun getDataClass(): Class<InputStream> = InputStream::class.java
        override fun getDataSource(): DataSource = DataSource.REMOTE
    }

    companion object {
        /**
         * An image stream for an address: http(s) through the app's connection with
         * [referer], a `data:image/` URI, or else a file path. The caller closes it.
         */
        @JvmStatic
        fun openStream(context: Context, address: String?, referer: String?): InputStream? {
            if (TextUtils.isEmpty(address)) return null
            if (address!!.startsWith("http://", true) || address.startsWith("https://", true) || address.startsWith("data:image/")) {
                val item = MainItem.ViewItem()
                item.itemType = 7.toByte(); item.url = address; item.referer = referer
                return Fetcher(context.applicationContext, item).openWebForCaller()
            }
            return BufferedInputStream(MainUtil.openFileInput(address), 32768)
        }

        @JvmStatic fun handlesItem(item: MainItem.ViewItem?): Boolean = item != null && !item.url.isNullOrEmpty()
    }
}

/**
 * Renders SVG data as a Bitmap for Glide's Bitmap requests (thumbnails of `.svg` images), with the same helper the old
 * loader used (`MainUtil.svgToBitmap`). It only claims streams that start like an SVG / XML document, so other formats go on to
 * Glide's normal decoders.
 */
class SvgBitmapDecoder(private val pool: BitmapPool) : ResourceDecoder<InputStream, Bitmap> {
    override fun handles(source: InputStream, options: Options): Boolean {
        if (!source.markSupported()) return false
        source.mark(512)
        try {
            val head = ByteArray(512)
            val n = source.read(head)
            if (n <= 0) return false
            val headText = String(head, 0, n, Charsets.UTF_8).trimStart('﻿', ' ', '\n', '\r', '\t')
            return headText.startsWith("<svg") || (headText.startsWith("<?xml") && headText.contains("<svg")) ||
                (headText.startsWith("<!") && headText.contains("<svg"))
        } catch (_: IOException) {
            return false
        } finally {
            try { source.reset() } catch (_: IOException) {}
        }
    }

    override fun decode(source: InputStream, width: Int, height: Int, options: Options): Resource<Bitmap>? =
        MainUtil.svgToBitmap(source)?.let { BitmapResource.obtain(it, pool) }
}
