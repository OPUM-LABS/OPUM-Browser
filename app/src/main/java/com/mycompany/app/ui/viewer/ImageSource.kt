package com.mycompany.app.ui.viewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.webkit.URLUtil
import com.mycompany.app.main.MainUtil
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Reads a picture for the Compose image screens: a file path, content URI or web address (`path`, with the page it came
 * from as `referer`) or an intent data [uri]. Large pictures are sampled down to about 16 megapixels and the EXIF
 * orientation is applied. Call it off the main thread; returns null when the source cannot be read or decoded.
 */
object ImageSource {

    fun load(context: Context, path: String?, uri: Uri?, referer: String?): Bitmap? {
        return try {
            val bytes = bytes(context, path, uri, referer) ?: return null
            decode(bytes)
        } catch (_: Throwable) {
            null
        }
    }

    /** The raw bytes of the source, or null when it cannot be read. Call off the main thread. */
    fun bytes(context: Context, path: String?, uri: Uri?, referer: String?): ByteArray? {
        return try {
            val data = open(context, path, uri, referer)?.use { input ->
                val out = ByteArrayOutputStream()
                input.copyTo(out)
                out.toByteArray()
            }
            if (data == null || data.isEmpty()) null else data
        } catch (_: Throwable) {
            null
        }
    }

    /** Decodes picture [bytes] (sampled to about 16 megapixels, EXIF orientation applied); null when not a picture. */
    fun decode(bytes: ByteArray): Bitmap? {
        try {
            val bounds = BitmapFactory.Options().also { it.inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            var sample = 1
            while ((bounds.outWidth / sample).toLong() * (bounds.outHeight / sample) > 16_000_000L) sample *= 2
            val options = BitmapFactory.Options().also { it.inSampleSize = sample }
            var decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options) ?: return decodeSvg(bytes)
            val matrix = Matrix()
            try {
                val exif = ExifInterface(bytes.inputStream())
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                    ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                }
            } catch (_: Exception) {
            }
            if (!matrix.isIdentity) {
                decoded = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
            }
            return decoded
        } catch (_: Throwable) {
            return null
        }
    }

    /**
     * Renders an SVG (which BitmapFactory cannot read) at its own size, scaled so the long side is at least 1024 px and
     * at most 4096 px; null when the bytes are not an SVG either.
     */
    private fun decodeSvg(bytes: ByteArray): Bitmap? {
        return try {
            val svg = com.caverock.androidsvg.SVG.getFromInputStream(bytes.inputStream())
            val box = svg.documentViewBox
            var w = if (svg.documentWidth > 0) svg.documentWidth else box?.width() ?: 0f
            var h = if (svg.documentHeight > 0) svg.documentHeight else box?.height() ?: 0f
            if (w <= 0f || h <= 0f) { w = 1024f; h = 1024f }
            val scale = (1024f / maxOf(w, h)).coerceAtLeast(1f).coerceAtMost(4096f / maxOf(w, h))
            val bw = (w * scale).toInt().coerceAtLeast(1)
            val bh = (h * scale).toInt().coerceAtLeast(1)
            svg.setDocumentWidth(bw.toFloat())
            svg.setDocumentHeight(bh.toFloat())
            if (box == null) svg.setDocumentViewBox(0f, 0f, w, h)
            val bitmap = Bitmap.createBitmap(bw, bh, Bitmap.Config.ARGB_8888)
            svg.renderToCanvas(android.graphics.Canvas(bitmap))
            bitmap
        } catch (_: Throwable) {
            null
        }
    }

    private fun open(context: Context, path: String?, uri: Uri?, referer: String?): InputStream? {
        if (!path.isNullOrEmpty()) {
            if (URLUtil.isNetworkUrl(path)) {
                val connection = MainUtil.openHttpConnection(0, 0, context.applicationContext, path, referer, true)
                return if (connection != null && connection.responseCode == 200) connection.inputStream else null
            }
            return MainUtil.openUriInput(context.applicationContext, path)
        }
        return uri?.let { context.contentResolver.openInputStream(it) }
    }
}
