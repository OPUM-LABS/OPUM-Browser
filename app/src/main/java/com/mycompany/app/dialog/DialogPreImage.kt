package com.mycompany.app.dialog

import android.view.View

import com.mycompany.app.view.anyListener
import android.content.Context
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.text.TextUtils
import android.view.MotionEvent
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ch.opumlabs.opumbrowser.R as AppR
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.mycompany.app.compress.Compress.Companion.isSvgName
import com.mycompany.app.dialog.DialogPreview.PreviewListener
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.guessDownloadName
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.view.GlideApp
import com.mycompany.app.view.GlideRequest
import com.mycompany.app.view.GlideRequests
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.zoom.ZoomImageAttacher
import com.mycompany.app.zoom.ZoomImageAttacher.AttacherListener

/**
 * Image preview (image long-press > Preview image). Reworked 2026-11-20 (owner) to match the link preview: the shared
 * floating [com.mycompany.app.ui.browser.PreviewCard] (bar on the main bar's side, drag the bar down to close) with the
 * picture filling the card on the card's own colour (a checkerboard behind transparent pictures), pinch / double-tap
 * zoom, swipe left / right for the previous / next picture of the page, and one bar: close · file name with
 * "1200 × 675 · JPEG · 84 KB · 3 / 17" · Save · Share · menu (open in new tab, view, download…, search by image, copy /
 * share URL, wallpaper, block). The actions run through the link menu (`DialogUrlLink.onItemClick`, which the preview
 * hides while it is open and shows again for them), so they behave exactly as there. Loading (Glide, SVG through
 * `PictureDrawable`, retry without the referer) and zoom (`ZoomImageAttacher`) are the old code. Same constructor.
 */
@Suppress("deprecation")
class DialogPreImage(
    webViewActivity: WebViewActivity?,
    imageUrlParam: String?,
    refererParam: String?,
    previewListener: PreviewListener?,
) : ComposeBottomDialog(webViewActivity) {
    @JvmField
    var hostActivity: WebViewActivity? = webViewActivity
    @JvmField
    var appContext: Context? = context
    @JvmField
    var previewCallback: PreviewListener? = previewListener
    @JvmField
    var imageUrl: String? = imageUrlParam
    @JvmField
    var referer: String? = refererParam
    @JvmField
    var imageView: ImageView? = null
    @JvmField
    var zoomAttacher: ZoomImageAttacher? = null
    @JvmField
    var glideRequests: GlideRequests? = null
    @JvmField
    var retryNoReferer: Boolean = false
    @JvmField
    var imageListener: RequestListener<Any?>? = null
    @JvmField
    var svgListener: RequestListener<Any?>? = null

    private var loading by mutableStateOf(true)
    private var failed by mutableStateOf(false)
    /** The picture has an alpha channel: drawn on a checkerboard. */
    private var transparent by mutableStateOf(false)
    /** "1200 × 675 · JPEG · 84 KB", filled in the background. */
    private var info by mutableStateOf("")
    /** The page's pictures (for swiping) and where this one is in it; null until read or when there is only one. */
    private var pageImages by mutableStateOf<List<String>?>(null)
    private var pageIndex by mutableIntStateOf(-1)
    private var menuOpen by mutableStateOf(false)
    /** Set when an action continues in the link menu: closing the preview then shows the menu again. */
    @JvmField var handedOff: Boolean = false
    private val barOnTop = com.mycompany.app.ui.browser.previewBarOnTop()
    private var swipeDetector: android.view.GestureDetector? = null

    private val ui = android.os.Handler(android.os.Looper.getMainLooper())

    /** Background work. */
    private var executor: java.util.concurrent.ExecutorService? = null
    fun runAsync(runnable: Runnable?) {
        var ex = executor
        if (ex == null || ex.isShutdown() || ex.isTerminated()) {
            ex = com.mycompany.app.main.MainApp.executor(context) ?: return
            executor = ex
        }
        try { ex.execute(runnable) } catch (unused: Exception) { }
    }

    init {
        imageListener = object : RequestListener<Any?> {
            override fun onLoadFailed(glideException: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Any?>, isFirstResource: Boolean): Boolean {
                if (imageView == null) return true
                if (retryNoReferer && !TextUtils.isEmpty(referer)) {
                    referer = null
                    ui.post { startLoad() }
                    return true
                }
                loading = false
                showError()
                return true
            }

            override fun onResourceReady(resource: Any?, model: Any, target: com.bumptech.glide.request.target.Target<Any?>?, dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
                loading = false
                transparent = (resource as? android.graphics.drawable.BitmapDrawable)?.bitmap?.hasAlpha() == true
                imageView?.setScaleType(ScaleType.FIT_CENTER)
                attachZoom()
                return false
            }
        }
        svgListener = object : RequestListener<Any?> {
            override fun onLoadFailed(glideException: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Any?>, isFirstResource: Boolean): Boolean {
                if (imageView == null) return true
                if (retryNoReferer && !TextUtils.isEmpty(referer)) {
                    referer = null
                    ui.post { runAsync(Runnable { loadSvg() }) }
                    return true
                }
                loading = false
                imageView?.setLayerType(View.LAYER_TYPE_NONE, null)
                showError()
                return true
            }

            override fun onResourceReady(resource: Any?, model: Any, target: com.bumptech.glide.request.target.Target<Any?>?, dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
                loading = false
                transparent = true
                imageView?.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                imageView?.setScaleType(ScaleType.FIT_CENTER)
                attachZoom()
                return false
            }
        }
        swipeDetector = android.view.GestureDetector(context, object : android.view.GestureDetector.SimpleOnGestureListener() {
            override fun onFling(down: MotionEvent?, move: MotionEvent, vx: Float, vy: Float): Boolean {
                if (zoomAttacher?.isZoomed() == true || kotlin.math.abs(vx) < kotlin.math.abs(vy) * 1.5f || kotlin.math.abs(vx) < 600f) return false
                step(if (vx < 0) 1 else -1)
                return true
            }
        })
        setScreen {
            com.mycompany.app.ui.browser.PreviewCard(
                barOnTop = barOnTop,
                onClosed = { dismiss() },
                onOutside = { cancelFromOutside() },
                bar = { Bar() },
            ) {
                val checker = transparent
                val light = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerLow
                androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                    if (checker) {
                        // checkerboard behind transparent pictures
                        val cell = 10.dp.toPx()
                        val a = Color(0xFFE0E0E0); val b = Color(0xFFFFFFFF)
                        var y = 0f; var row = 0
                        while (y < size.height) {
                            var x = 0f; var col = row
                            while (x < size.width) {
                                drawRect(if (col % 2 == 0) a else b, androidx.compose.ui.geometry.Offset(x, y), androidx.compose.ui.geometry.Size(cell, cell))
                                x += cell; col++
                            }
                            y += cell; row++
                        }
                    } else {
                        drawRect(light)
                    }
                }
                AndroidView(
                    factory = { ctx ->
                        ImageView(ctx).also {
                            it.setScaleType(ScaleType.FIT_CENTER)
                            imageView = it
                            ui.post { startLoad() }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
                if (loading) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                // previous / next hints
                val list = pageImages
                if (list != null && list.size > 1 && !loading) {
                    if (pageIndex > 0) Hint(Alignment.CenterStart, AppR.drawable.outline_chevron_left_black_24) { step(-1) }
                    if (pageIndex < list.size - 1) Hint(Alignment.CenterEnd, AppR.drawable.outline_chevron_right_black_24) { step(1) }
                }
            }
        }
        show()
        runAsync(Runnable { readInfo(imageUrl, referer) })
        readPageImages()
    }

    @androidx.compose.runtime.Composable
    private fun androidx.compose.foundation.layout.BoxScope.Hint(align: Alignment, icon: Int, onClick: () -> Unit) {
        FilledTonalIconButton(
            onClick = onClick,
            colors = androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors(containerColor = Color(0x66000000)),
            modifier = Modifier.align(align).padding(8.dp).size(36.dp),
        ) {
            Icon(painterResource(icon), contentDescription = null, tint = Color.White)
        }
    }

    /** The bar: close · name + info · Save · Share · menu. */
    @androidx.compose.runtime.Composable
    private fun Bar() {
        val mt = androidx.compose.material3.MaterialTheme
        Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            androidx.compose.material3.IconButton(onClick = { dismiss() }) {
                Icon(painterResource(AppR.drawable.outline_close_black_24), stringResource(AppR.string.close))
            }
            androidx.compose.foundation.layout.Column(Modifier.weight(1f).padding(horizontal = 4.dp)) {
                Text(
                    fileName(), style = mt.typography.bodyLarge, maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
                val list = pageImages
                val count = if (list != null && list.size > 1 && pageIndex >= 0) "${pageIndex + 1} / ${list.size}" else ""
                // the position first: the info can be long
                val line = listOf(count, info).filter { it.isNotEmpty() }.joinToString(" · ")
                if (line.isNotEmpty()) {
                    Text(
                        line, style = mt.typography.bodySmall, color = mt.colorScheme.onSurfaceVariant, maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    )
                }
            }
            androidx.compose.material3.IconButton(onClick = { save() }) {
                Icon(painterResource(AppR.drawable.outline_download_black_24), stringResource(AppR.string.down_image))
            }
            androidx.compose.material3.IconButton(onClick = { linkAction(9) }) {
                Icon(painterResource(AppR.drawable.outline_share_black_24), stringResource(AppR.string.share_image))
            }
            Box {
                androidx.compose.material3.IconButton(onClick = { menuOpen = true }) {
                    Icon(painterResource(AppR.drawable.outline_more_vert_black_24), null)
                }
                com.mycompany.app.ui.components.BubbleMenu(expanded = menuOpen, onDismiss = { menuOpen = false }) {
                    // the image menu's own entries (same labels and icons); "Download…" = the download dialog
                    for (idx in intArrayOf(3, 2, -1, 6, 7, 8, 10, 11)) {
                        val label = if (idx == -1) AppR.string.download else com.mycompany.app.main.MainConst.imageMenuNames[idx]
                        val icon = if (idx == -1) AppR.drawable.outline_cloud_download_black_24 else com.mycompany.app.main.MainConst.imageMenuIconsBlack[idx]
                        androidx.compose.material3.DropdownMenuItem(
                            leadingIcon = { Icon(painterResource(icon), null, Modifier.size(20.dp)) },
                            text = { Text(stringResource(label)) },
                            onClick = {
                                menuOpen = false
                                if (idx == -1) {
                                    handedOff = false
                                    previewCallback?.download(imageUrl, "image/*")
                                } else {
                                    linkAction(idx)
                                }
                            },
                        )
                    }
                }
            }
        }
    }

    /** File name of the picture ("shutterstock_20229…jpg"), or its host. */
    private fun fileName(): String {
        val url = imageUrl ?: return ""
        val name = try { guessDownloadName(url, null, null, true) } catch (_: Exception) { null }
        return name?.takeIf { it.isNotBlank() } ?: (try { android.net.Uri.parse(url).host } catch (_: Exception) { null } ?: url)
    }

    /**
     * Runs image-menu action [index] in the link menu (behind the preview, hidden while it is open): it is pointed at
     * the picture shown now, the preview closes and the menu shows again (for its choices and loading state).
     */
    private fun linkAction(index: Int) {
        val wa = hostActivity
        val link = wa?.urlLinkDialog
        val url = imageUrl
        if (link == null || url == null) {
            // no link menu (should not happen): the old direct actions
            when (index) {
                4 -> previewCallback?.download(url, "image/*")
                7 -> previewCallback?.copyUrl(url)
                9, 8 -> previewCallback?.share(url)
                3 -> previewCallback?.openFile(url, 0L, true)
            }
            return
        }
        if (link.imageUrl != url) {
            // swiped to another picture: the menu works on this one now (its loaded data was for the old one)
            link.imageUrl = url
            link.dataState = 0
            link.mimeType = null
        }
        handedOff = true
        dismiss()
        wa.webCoord?.post { link.onItemClick(index, null, true) }
    }

    /** Save: the link menu's "Save image" (for the picture shown now). */
    private fun save() = linkAction(4)

    /** Previous / next picture of the page. */
    private fun step(delta: Int) {
        val list = pageImages ?: return
        val next = pageIndex + delta
        if (next < 0 || next >= list.size) return
        pageIndex = next
        imageUrl = list[next]
        info = ""
        transparent = false
        zoomAttacher?.resetMatrix()
        startLoad()
        val url = imageUrl
        runAsync(Runnable { readInfo(url, referer) })
    }

    /** The page's pictures (at least 100 × 100 px and drawn at least 64 × 64, so no icons; http only, no duplicates), read with a small script. */
    private fun readPageImages() {
        val page = hostActivity?.currentPage ?: return
        val js = "(function(){var s=[],seen={};document.querySelectorAll('img').forEach(function(i){var u=i.currentSrc||i.src;" +
            "if(u&&/^https?:/.test(u)&&!seen[u]&&i.naturalWidth>=100&&i.naturalHeight>=100&&i.width>=64&&i.height>=64){seen[u]=1;s.push(u)}});return s})()"
        try {
            page.evaluateJavascript(js) { result ->
                val current = imageUrl ?: return@evaluateJavascript
                val list = try {
                    val arr = org.json.JSONArray(result ?: "[]")
                    // SVGs on pages are almost always icons: left out (except the picture that was opened)
                    (0 until arr.length()).map { arr.getString(it) }
                        .filter { it == current || !isSvgName(guessDownloadName(it, null, null, true)) }
                } catch (_: Exception) { emptyList() }
                var at = list.indexOf(current)
                if (at < 0) at = list.indexOfFirst { it.substringBefore('?') == current.substringBefore('?') }
                if (at >= 0 && list.size > 1) {
                    pageImages = list
                    pageIndex = at
                }
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Blocking: size, type and dimensions of [url] from its first 64 KB (one request: the bounds decoder needs only the
     * header; the total size comes from Content-Range / Content-Length). Leaves [info] empty when that fails.
     */
    private fun readInfo(url: String?, ref: String?) {
        if (url == null || !URLUtil.isNetworkUrl(url)) return
        var conn: java.net.HttpURLConnection? = null
        try {
            conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.setRequestProperty("Range", "bytes=0-65535")
            conn.setRequestProperty("User-Agent", MainUtil.defaultUserAgent(appContext))
            if (!ref.isNullOrEmpty()) conn.setRequestProperty("Referer", ref)
            val total = conn.getHeaderField("Content-Range")?.substringAfter('/')?.toLongOrNull()
                ?: conn.contentLengthLong.takeIf { conn.responseCode == 200 }
            val type = conn.contentType?.substringBefore(';')?.trim()
            val bytes = conn.inputStream.use { input -> input.readNBytesCompat(65536) }
            val opts = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
            android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
            val parts = ArrayList<String>()
            if (opts.outWidth > 0 && opts.outHeight > 0) parts.add("${opts.outWidth} × ${opts.outHeight}")
            val mime = opts.outMimeType ?: type
            mime?.substringAfter('/')?.uppercase()?.let { t ->
                parts.add(when {
                    t.contains("SVG") -> "SVG"
                    t == "PJPEG" || t == "JPG" -> "JPEG"
                    else -> t.removePrefix("X-")
                })
            }
            if (total != null && total > 0) parts.add(android.text.format.Formatter.formatShortFileSize(appContext ?: return, total))
            val text = parts.joinToString(" · ")
            ui.post { if (url == imageUrl) info = text }
        } catch (_: Exception) {
        } finally {
            try { conn?.disconnect() } catch (_: Exception) { }
        }
    }

    private fun java.io.InputStream.readNBytesCompat(max: Int): ByteArray {
        val out = java.io.ByteArrayOutputStream()
        val buf = ByteArray(8192)
        while (out.size() < max) {
            val n = read(buf, 0, minOf(buf.size, max - out.size()))
            if (n <= 0) break
            out.write(buf, 0, n)
        }
        return out.toByteArray()
    }

    private fun attachZoom() {
        val view = imageView ?: return
        if (zoomAttacher != null) return
        zoomAttacher = ZoomImageAttacher(view, object : AttacherListener {
            override fun onMatrixChanged(rectF: RectF?, isZoomed: Boolean) {}
            override fun onTouch(motionEvent: MotionEvent?, isDown: Boolean) {
                if (motionEvent != null) swipeDetector?.onTouchEvent(motionEvent)
            }
            override fun onTap(): Boolean = false
            override fun canScale(): Boolean = false
            override fun onDoubleTap() {}
        })
    }

    /** Rotation: the card fills the screen either way; only the zoom is reset. */
    fun applyLandscape(landscape: Boolean) {
        zoomAttacher?.resetMatrix()
    }

    /** Could not load: error icon (the bar's actions still work). */
    fun showError() {
        val view = imageView ?: return
        failed = true
        view.setScaleType(ScaleType.CENTER_INSIDE)
        view.setImageResource(AppR.drawable.outline_error_dark_web_48)
    }

    /** Loads the image (SVG through `PictureDrawable`). */
    fun startLoad() {
        if (imageView == null) return
        if (TextUtils.isEmpty(imageUrl)) {
            loading = false
            showError()
            return
        }
        loading = true
        failed = false
        if (isSvgName(guessDownloadName(imageUrl, null, null, true))) runAsync(Runnable { loadSvg() }) else runAsync(Runnable { loadImage() })
    }

    private fun loadImage() {
        val act = hostActivity ?: return
        if (glideRequests == null) glideRequests = GlideApp.with(act)
        ui.post {
            val requests = glideRequests ?: return@post
            val view = imageView ?: return@post
            val cache = DiskCacheStrategy.NONE
            if (URLUtil.isNetworkUrl(imageUrl)) {
                retryNoReferer = true
                (requests.load(MainUtil.glideUrlFor(appContext, imageUrl!!, referer)).diskCacheStrategy(cache)).anyListener(imageListener).into(view)
            } else {
                retryNoReferer = false
                (requests.load(imageUrl).diskCacheStrategy(cache)).anyListener(imageListener).into(view)
            }
        }
    }

    private fun loadSvg() {
        val act = hostActivity ?: return
        if (glideRequests == null) glideRequests = GlideApp.with(act)
        ui.post {
            val requests = glideRequests ?: return@post
            val view = imageView ?: return@post
            if (URLUtil.isNetworkUrl(imageUrl)) {
                retryNoReferer = true
                ((requests.`as`(PictureDrawable::class.java)).load(MainUtil.glideUrlFor(appContext, imageUrl!!, referer))).anyListener(svgListener).into(view)
                return@post
            }
            retryNoReferer = false
            ((requests.`as`(PictureDrawable::class.java)).load(imageUrl)).anyListener(svgListener).into(view)
        }
    }

    override fun dismiss() {
        if (appContext == null) {
            super.dismiss()
            return
        }
        val view = imageView
        if (view != null) glideRequests?.clear(view)
        glideRequests = null
        zoomAttacher?.detach()
        zoomAttacher = null
        hostActivity = null
        appContext = null
        previewCallback = null
        imageView = null
        super.dismiss()
    }

    companion object {
    }
}
