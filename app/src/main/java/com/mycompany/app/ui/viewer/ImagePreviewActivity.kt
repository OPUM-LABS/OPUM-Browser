package com.mycompany.app.ui.viewer

import android.content.Context
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.PdfSettings
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.webkit.URLUtil
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogDownUrl
import com.mycompany.app.dialog.DialogDownUrl.DownUrlListener
import com.mycompany.app.dialog.DialogSetDown
import com.mycompany.app.dialog.DialogSetDown.SetDownListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.web.WebNestView
import java.io.File
import kotlin.math.max
import kotlin.math.min

/**
 * Shows one picture full screen: pinch and double-tap to zoom, drag to move, tap to show or hide the bars. Source:
 * the intent's data (a file / content picture) or `EXTRA_PATH` (path or web address) with `EXTRA_REFERER`;
 * `EXTRA_FILE` marks a local file and `EXTRA_POPUP` a web thumbnail whose full-size address is derived first.
 * Actions: download (web pictures), share, set as home background or phone wallpaper, crop, edit. Animated GIF / WebP
 * play. Replaces `MainImagePreview` (web `blob:` and SVG pictures are not supported here).
 */
class ImagePreviewActivity : OpumComposeActivity() {

    companion object {
        /** Set when a home background was changed from here, so the browser refreshes it when it comes back. */
        @JvmField
        var backgroundChanged: Boolean = false
    }

    private var source: String? = null
    private var referer: String? = null
    private var mime: String? = null
    private var isFile = false
    private var shown by mutableStateOf<Shown?>(null)
    private var loading by mutableStateOf(true)
    private var token = 0
    private var savedPath by mutableStateOf<String?>(null)

    private class Shown(val bitmap: Bitmap?, val drawable: Drawable?, val width: Int, val height: Int, val size: Long)

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        readIntent(intent)
        if (TextUtils.isEmpty(source)) {
            MainUtil.showToast(this, R.string.invalid_path)
            finish()
            return
        }
        super.onCreate(savedInstanceState)
        MainUtil.applyScreenOrientation(this)
        load()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readIntent(intent)
        if (TextUtils.isEmpty(source)) {
            MainUtil.showToast(this, R.string.invalid_path)
            return
        }
        load()
    }

    override fun onResume() {
        super.onResume()
        MainUtil.applyWindowBrightness(window, PdfSettings.brightness.value, PdfSettings.userBrightness.value)
    }

    override fun onDestroy() {
        token++
        super.onDestroy()
    }

    private fun readIntent(intent: Intent) {
        val data = intent.data
        referer = null
        mime = null
        isFile = false
        if (data != null) {
            source = data.toString()
            mime = intent.type
            isFile = true
        } else {
            source = intent.getStringExtra("EXTRA_PATH")
            referer = intent.getStringExtra("EXTRA_REFERER")
            isFile = intent.getBooleanExtra("EXTRA_FILE", false)
            if (!isFile && URLUtil.isNetworkUrl(source) && intent.getBooleanExtra("EXTRA_POPUP", false)) {
                source = MainUtil.extractRedirect(source)
            }
        }
    }

    private fun load() {
        val path = source
        val ref = referer
        val current = ++token
        loading = true
        shown = null
        val context = applicationContext
        Thread {
            var result: Shown? = null
            try {
                val bytes = ImageSource.bytes(context, path, null, ref)
                if (bytes != null) {
                    var animated: Drawable? = null
                    try {
                        val drawable = ImageDecoder.decodeDrawable(ImageDecoder.createSource(java.nio.ByteBuffer.wrap(bytes))) { decoder, info, _ ->
                            val longest = max(info.size.width, info.size.height)
                            if (longest > 4096) decoder.setTargetSampleSize((longest / 4096) + 1)
                        }
                        if (drawable is AnimatedImageDrawable) animated = drawable
                    } catch (_: Throwable) {
                    }
                    result = if (animated != null) {
                        Shown(null, animated, animated.intrinsicWidth, animated.intrinsicHeight, bytes.size.toLong())
                    } else {
                        ImageSource.decode(bytes)?.let { Shown(it, null, it.width, it.height, bytes.size.toLong()) }
                    }
                }
            } catch (_: Throwable) {
            }
            runOnUiThread {
                if (current != token) return@runOnUiThread
                shown = result
                loading = false
                if (result == null) MainUtil.showToast(this, R.string.image_fail)
            }
        }.start()
    }

    // ---- actions ----

    /** `MainUtil.openWithChooser` wants the legacy activity type, so it runs on the (invisible) dialog host that closes right after. */
    private fun quickDownload(dir: String?, dname: String?, name: String?) {
        MainUtil.openWithChooser(this, dir, dname, source, referer, name, "image/*")
    }

    private fun download() {
        if (!AlbumSettings.dappPkg.value.isNullOrEmpty() && !AlbumSettings.dappCls.value.isNullOrEmpty()) {
            quickDownload(AlbumSettings.dappPkg.value, AlbumSettings.dappCls.value, null)
            return
        }
        val url = source
        val ext = MainUtil.dataUriExt(url)
        val type = if (ext.isNullOrEmpty()) null else "image/$ext"
        var dialog: DialogDownUrl? = null
        com.mycompany.app.ui.prefs.DialogShow.direct(this as android.app.Activity, requestCodes = intArrayOf(19), onResult = { c, r, intent -> dialog?.onFolderResult(c, r, intent) }) { host ->
            DialogDownUrl(
                host, url, referer, null, null, type, shown?.size ?: 0L, 4,
                0, null, 0, null, 0,
                object : DownUrlListener {
                    override fun currentWeb(): WebNestView? = null
                    override fun openApp(url: String?, name: String?) {
                        dialog?.dismiss()
                        MainUtil.shareText(this@ImagePreviewActivity, url, name)
                    }
                    override fun openWith(url: String?, name: String?, dirName: String?) {
                        dialog?.dismiss()
                        runOnUiThread {
                            DialogSetDown(this@ImagePreviewActivity, url, dirName, true, false, false, 0, object : SetDownListener {
                                override fun onPick(label: String?, packageName: String?, className: String?) { quickDownload(packageName, className, name) }
                            })
                        }
                    }
                    override fun onDownload(url: String?, uriItem: UriItem?, type: Int, isAudio: Boolean, name: String?, dirName: String?) {
                        dialog?.dismiss()
                        if (uriItem == null) return
                        if (type == 3) { MainUtil.showToast(this@ImagePreviewActivity, R.string.invalid_url); return }
                        val app = MainApp.from(applicationContext)
                        if (app == null) MainUtil.showToast(this@ImagePreviewActivity, R.string.down_fail)
                        else app.startDownload(url, dirName, uriItem, if (type == 1) false else isAudio)
                    }
                    override fun openInApp(appCode: Int, url: String?) {}
                    override fun preview(url: String?, name: String?, dirName: String?, isImage: Boolean) {}
                    override fun share(url: String?, name: String?, dirName: String?) {}
                },
            ).also { dialog = it }
        }
    }

    private fun share() {
        val path = source ?: return
        if (isFile || !URLUtil.isNetworkUrl(path)) {
            if (!MainUtil.shareFile(4, this, path, null, "image/*")) MainUtil.showToast(this, R.string.image_fail)
            return
        }
        MainUtil.showToast(this, R.string.down_start)
        val context = applicationContext
        val ref = referer
        Thread {
            var cached: String? = null
            try {
                val bytes = ImageSource.bytes(context, path, null, ref)
                if (bytes != null) {
                    val ext = MainUtil.dataUriExt(path).takeIf { it.length in 2..4 } ?: "jpg"
                    val file = File(context.cacheDir, "share_${System.currentTimeMillis()}.$ext")
                    file.writeBytes(bytes)
                    cached = file.path
                }
            } catch (_: Throwable) {
            }
            runOnUiThread {
                if (cached == null || !MainUtil.shareFile(4, this, cached, null, "image/*")) MainUtil.showToast(this, R.string.image_fail)
            }
        }.start()
    }

    private fun openCrop() {
        startActivity(
            Intent(this, ImageCropActivity::class.java)
                .putExtra("EXTRA_PATH", source).putExtra("EXTRA_TYPE", mime).putExtra("EXTRA_REFERER", referer),
        )
    }

    // ---- screen ----

    @Composable
    override fun Content() {
        var bars by remember { mutableStateOf(true) }
        var wallpaperMenu by remember { mutableStateOf(false) }
        val snack = remember { SnackbarHostState() }
        val savedText = stringResource(R.string.save_success)
        val openText = stringResource(R.string.open_with)
        val saved = savedPath
        if (saved != null) {
            LaunchedEffect(saved) {
                if (snack.showSnackbar(savedText, openText) == SnackbarResult.ActionPerformed) {
                    MainUtil.openWithApp(this@ImagePreviewActivity, saved, "image/*", true, false)
                }
                savedPath = null
            }
        }
        var zoom by remember(shown) { mutableFloatStateOf(1f) }
        var offset by remember(shown) { mutableStateOf(Offset.Zero) }
        var view by remember { mutableStateOf(IntSize.Zero) }
        val current = shown
        val network = !isFile && URLUtil.isNetworkUrl(source)

        val edit = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val path = result.data?.getStringExtra("EXTRA_PATH")
            if (result.resultCode == RESULT_OK && !path.isNullOrEmpty()) savedPath = path
        }
        val background = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) backgroundChanged = true
        }

        Box(Modifier.fillMaxSize().background(Color.Black).onSizeChanged { view = it }) {
            if (current != null && view.width > 0) {
                val fit = min(view.width / current.width.toFloat(), view.height / current.height.toFloat())
                val limitX = max(0f, (current.width * fit * zoom - view.width) / 2f)
                val limitY = max(0f, (current.height * fit * zoom - view.height) / 2f)
                val clamped = Offset(offset.x.coerceIn(-limitX, limitX), offset.y.coerceIn(-limitY, limitY))
                Box(
                    Modifier.fillMaxSize()
                        .pointerInput(current) {
                            detectTapGestures(
                                onTap = { bars = !bars },
                                onDoubleTap = { if (zoom > 1.01f) { zoom = 1f; offset = Offset.Zero } else zoom = 3f },
                            )
                        }
                        .pointerInput(current) {
                            detectTransformGestures { _, pan, gestureZoom, _ ->
                                zoom = (zoom * gestureZoom).coerceIn(1f, 10f)
                                offset += pan
                            }
                        }
                        .graphicsLayer(scaleX = zoom, scaleY = zoom, translationX = clamped.x, translationY = clamped.y),
                ) {
                    val bitmap = current.bitmap
                    val drawable = current.drawable
                    if (bitmap != null) {
                        Image(bitmap.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
                    } else if (drawable != null) {
                        AndroidView(
                            factory = { context ->
                                ImageView(context).apply {
                                    scaleType = ImageView.ScaleType.FIT_CENTER
                                    setImageDrawable(drawable)
                                    (drawable as? AnimatedImageDrawable)?.start()
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            } else if (loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                Text(stringResource(R.string.image_fail), Modifier.align(Alignment.Center), color = Color.White)
            }

            AnimatedVisibility(bars, Modifier.align(Alignment.TopCenter)) {
                Row(
                    Modifier.fillMaxWidth().background(Color(0x99000000)).statusBarsPadding().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { finish() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White) }
                    Text(
                        source?.let { Uri.decode(it).substringAfterLast('/').substringBefore('?') } ?: "",
                        Modifier.weight(1f), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (current != null) {
                        if (network) {
                            IconButton(onClick = { download() }) { Icon(painterResource(R.drawable.outline_download_white_24), stringResource(R.string.down_start), tint = Color.White) }
                        }
                        IconButton(onClick = { share() }) { Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.share), tint = Color.White) }
                        Box {
                            IconButton(onClick = { wallpaperMenu = true }) { Icon(painterResource(R.drawable.outline_wallpaper_white_24), null, tint = Color.White) }
                            com.mycompany.app.ui.components.BubbleMenu(expanded = wallpaperMenu, onDismiss = { wallpaperMenu = false }) {
                                DropdownMenuItem(text = { Text(stringResource(R.string.soul_home)) }, onClick = {
                                    wallpaperMenu = false
                                    background.launch(
                                        Intent(this@ImagePreviewActivity, BackgroundImageActivity::class.java)
                                            .putExtra("EXTRA_PATH", source).putExtra("EXTRA_TYPE", mime).putExtra("EXTRA_REFERER", referer),
                                    )
                                })
                                DropdownMenuItem(text = { Text(stringResource(R.string.phone_home)) }, onClick = {
                                    wallpaperMenu = false
                                    startActivity(
                                        Intent(this@ImagePreviewActivity, WallpaperActivity::class.java)
                                            .putExtra("EXTRA_PATH", source).putExtra("EXTRA_TYPE", mime).putExtra("EXTRA_REFERER", referer),
                                    )
                                })
                            }
                        }
                        IconButton(onClick = { openCrop() }) { Icon(painterResource(R.drawable.outline_crop_white_24), null, tint = Color.White) }
                        IconButton(onClick = {
                            edit.launch(
                                Intent(this@ImagePreviewActivity, com.mycompany.app.ui.viewer.ImageEditActivity::class.java)
                                    .putExtra("EXTRA_PATH", source).putExtra("EXTRA_TYPE", mime).putExtra("EXTRA_REFERER", referer),
                            )
                        }) { Icon(painterResource(R.drawable.outline_edit_white_24), null, tint = Color.White) }
                    }
                }
            }

            SnackbarHost(snack, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 56.dp))
            if (current != null) {
                AnimatedVisibility(bars, Modifier.align(Alignment.BottomStart)) {
                    Text(
                        "${current.width} x ${current.height}  ${MainUtil.sizeLabel(current.size)}",
                        Modifier.navigationBarsPadding().padding(16.dp).background(Color(0x99000000)).padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White, style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
