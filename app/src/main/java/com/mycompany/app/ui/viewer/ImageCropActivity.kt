package com.mycompany.app.ui.viewer

import android.content.Context
import com.mycompany.app.pref.store.PdfSettings
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import android.text.TextUtils
import android.webkit.URLUtil
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.book.DbBookDown
import com.mycompany.app.dialog.DialogDownEdit
import com.mycompany.app.dialog.DialogDownPage.DownPageListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.ui.OpumComposeActivity
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Crops a picture. Source: `EXTRA_PATH` (file, content URI or web address with `EXTRA_REFERER` / `EXTRA_TYPE`) or the
 * intent's data. With `EXTRA_ICON` the crop is square, scaled to the icon size and written to `EXTRA_DST`, and the
 * result is returned as `EXTRA_PATH`; otherwise the old "save as" dialog picks name and folder, the file is added to the
 * download list and a snackbar offers to open it. Replaces `MainImageCropper` (vector images are not supported here).
 */
class ImageCropActivity : OpumComposeActivity() {

    private var path: String? = null
    private var referer: String? = null
    private var uri: Uri? = null
    private var dst: String? = null
    private var icon = false
    private var iconSize = 0

    private var bitmap by mutableStateOf<Bitmap?>(null)
    private var loading by mutableStateOf(true)
    private var failed by mutableStateOf(false)
    private var saving by mutableStateOf(false)
    private var pendingMessage by mutableStateOf<Pair<Boolean, String?>?>(null)

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        path = intent.getStringExtra("EXTRA_PATH")
        referer = intent.getStringExtra("EXTRA_REFERER")
        if (TextUtils.isEmpty(path)) {
            uri = intent.data
            if (uri == null) {
                MainUtil.showToast(this, R.string.invalid_path)
                finish()
                return
            }
            dst = intent.getStringExtra("EXTRA_DST")
            icon = intent.getBooleanExtra("EXTRA_ICON", false)
            if (icon) iconSize = MainApp.dp40
        }
        super.onCreate(savedInstanceState)
        MainUtil.applyScreenOrientation(this)
        load()
    }

    override fun onResume() {
        super.onResume()
        MainUtil.applyWindowBrightness(window, PdfSettings.brightness.value, PdfSettings.userBrightness.value)
    }

    override fun finish() {
        if (saving) return
        super.finish()
    }

    private fun load() {
        Thread {
            val result = ImageSource.load(applicationContext, path, uri, referer)
            runOnUiThread {
                bitmap = result
                failed = result == null
                loading = false
            }
        }.start()
    }

    /** Writes [cropped] to [target] (scaled for icons), adds it to the download list, and reports back. */
    private fun save(target: String?, cropped: Bitmap) {
        saving = true
        val context: Context = applicationContext
        Thread {
            var ok = false
            try {
                var out = cropped
                if (icon && iconSize > 0 && out.width > iconSize) {
                    MainUtil.scaleBitmap(iconSize, iconSize, out)?.let { out = it }
                }
                val format = if (out.hasAlpha()) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                ok = MainUtil.saveBitmap(context, out, target, format)
                if (ok && !icon) {
                    val item = MainUri.createIn(context, target, MainUri.storageVolume())
                    if (item != null) DbBookDown.addFile(context, target, null, item)
                }
            } catch (_: Throwable) {
            }
            runOnUiThread {
                saving = false
                if (!ok) {
                    pendingMessage = Pair(false, null)
                } else if (icon) {
                    setResult(RESULT_OK, Intent().putExtra("EXTRA_PATH", target))
                    finish()
                } else {
                    pendingMessage = Pair(true, target)
                }
            }
        }.start()
    }

    private fun askName(cropped: Bitmap) {
        val suggestion = MainUtil.stripExtension(MainUtil.guessDownloadName(path, null, if (cropped.hasAlpha()) "image/png" else "image/jpg", true))
        var dialog: DialogDownEdit? = null
        com.mycompany.app.ui.prefs.DialogShow.direct(this as android.app.Activity, requestCodes = intArrayOf(19), onResult = { c, r, intent -> dialog?.onPickResult(c, r, intent) }) { host ->
            DialogDownEdit(host, suggestion, cropped, object : DownPageListener {
                override fun onSave(url: String?, path: String?, bitmap: Bitmap?) {
                    dialog?.dismiss()
                    runOnUiThread { save(path, bitmap ?: cropped) }
                }
            }).also { dialog = it }
        }
    }

    @Composable
    override fun Content() {
        val snack = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val bmp = bitmap
        // crop rectangle in bitmap pixels
        var crop by remember(bmp) { mutableStateOf(bmp?.let { initialCrop(it, icon) } ?: Rect.Zero) }
        val message = pendingMessage
        val savedText = stringResource(R.string.save_success)
        val failText = stringResource(R.string.save_fail)
        val openText = stringResource(R.string.open_with)
        if (message != null) {
            androidx.compose.runtime.LaunchedEffect(message) {
                if (!message.first) {
                    snack.showSnackbar(failText)
                } else if (snack.showSnackbar(savedText, openText) == SnackbarResult.ActionPerformed) {
                    MainUtil.openWithApp(this@ImageCropActivity, message.second, "image/*", true, false)
                }
                pendingMessage = null
            }
        }

        Box(Modifier.fillMaxSize().background(Color.Black)) {
            when {
                bmp != null -> CropCanvas(
                    bitmap = bmp,
                    crop = crop,
                    fixedSquare = icon,
                    onCrop = { crop = it },
                    modifier = Modifier.fillMaxSize().statusBarsPadding().padding(bottom = 56.dp).navigationBarsPadding(),
                )
                loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                else -> Text(stringResource(R.string.image_fail), Modifier.align(Alignment.Center), color = Color.White)
            }
            if (bmp != null) {
                FilledIconButton(
                    onClick = { crop = Rect(0f, 0f, bmp.width.toFloat(), bmp.height.toFloat()).let { if (icon) initialCrop(bmp, true) else it } },
                    modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(end = 16.dp, bottom = 72.dp),
                ) { Icon(painterResource(R.drawable.outline_fullscreen_white_24), contentDescription = null, tint = Color.White) }
            }
            Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().background(Color.Black)) {
                TextButton(onClick = { finish() }, modifier = Modifier.weight(1f).size(height = 56.dp, width = 0.dp)) {
                    Text(stringResource(R.string.cancel), color = Color.White)
                }
                TextButton(
                    onClick = {
                        val source = bitmap ?: return@TextButton
                        if (failed || saving) return@TextButton
                        val cropNow = crop
                        val cropped = try {
                            Bitmap.createBitmap(source, cropNow.left.toInt(), cropNow.top.toInt(), cropNow.width.toInt().coerceAtLeast(1), cropNow.height.toInt().coerceAtLeast(1))
                        } catch (_: Throwable) {
                            scope.launch { snack.showSnackbar(failText) }
                            return@TextButton
                        }
                        if (TextUtils.isEmpty(path)) save(dst, cropped) else askName(cropped)
                    },
                    modifier = Modifier.weight(1f).size(height = 56.dp, width = 0.dp),
                ) { Text(stringResource(R.string.crop_save), color = Color.White) }
            }
            SnackbarHost(snack, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 64.dp))
            if (saving) {
                Box(Modifier.fillMaxSize().background(Color(0x99000000)).pointerInput(Unit) { detectDragGestures { _, _ -> } }) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
        }
    }

    private fun initialCrop(bitmap: Bitmap, square: Boolean): Rect {
        val width = bitmap.width.toFloat()
        val height = bitmap.height.toFloat()
        if (!square) return Rect(width * 0.1f, height * 0.1f, width * 0.9f, height * 0.9f)
        val side = minOf(width, height) * 0.8f
        return Rect((width - side) / 2, (height - side) / 2, (width + side) / 2, (height + side) / 2)
    }
}

private enum class Grip { NONE, MOVE, LEFT, TOP, RIGHT, BOTTOM, TL, TR, BL, BR }

/** The picture with a dimmed surround, a rectangle to drag (corners, edges, inside to move) and thirds guides. */
@Composable
private fun CropCanvas(bitmap: Bitmap, crop: Rect, fixedSquare: Boolean, onCrop: (Rect) -> Unit, modifier: Modifier = Modifier) {
    val image = remember(bitmap) { bitmap.asImageBitmap() }
    var size by remember { mutableStateOf(IntSize.Zero) }
    var grip by remember { mutableStateOf(Grip.NONE) }
    val accent = MaterialTheme.colorScheme.primary
    // fit-center of the bitmap in the canvas
    val scale = if (size.width == 0) 1f else minOf(size.width / bitmap.width.toFloat(), size.height / bitmap.height.toFloat())
    val originX = (size.width - bitmap.width * scale) / 2f
    val originY = (size.height - bitmap.height * scale) / 2f
    var current by remember(bitmap) { mutableStateOf(crop) }
    current = crop

    Canvas(
        modifier.onSizeChanged { size = it }.pointerInput(bitmap, size, fixedSquare) {
            val slop = 28.dp.toPx()
            val minSide = 48.dp.toPx() / scale
            detectDragGestures(
                onDragStart = { start ->
                    val current2 = current
                    val left2 = originX + current2.left * scale
                    val top2 = originY + current2.top * scale
                    val rr = originX + current2.right * scale
                    val bottom2 = originY + current2.bottom * scale
                    fun near(a: Float, b: Float) = kotlin.math.abs(a - b) <= slop
                    val x = start.x
                    val y = start.y
                    grip = when {
                        near(x, left2) && near(y, top2) -> Grip.TL
                        near(x, rr) && near(y, top2) -> Grip.TR
                        near(x, left2) && near(y, bottom2) -> Grip.BL
                        near(x, rr) && near(y, bottom2) -> Grip.BR
                        !fixedSquare && near(x, left2) && y in top2..bottom2 -> Grip.LEFT
                        !fixedSquare && near(x, rr) && y in top2..bottom2 -> Grip.RIGHT
                        !fixedSquare && near(y, top2) && x in left2..rr -> Grip.TOP
                        !fixedSquare && near(y, bottom2) && x in left2..rr -> Grip.BOTTOM
                        x in left2..rr && y in top2..bottom2 -> Grip.MOVE
                        else -> Grip.NONE
                    }
                },
                onDragEnd = { grip = Grip.NONE },
                onDragCancel = { grip = Grip.NONE },
            ) { change, drag ->
                if (grip == Grip.NONE) return@detectDragGestures
                change.consume()
                val dx = drag.x / scale
                val dy = drag.y / scale
                val maxW = bitmap.width.toFloat()
                val maxH = bitmap.height.toFloat()
                var left2 = current.left
                var top2 = current.top
                var current2 = current.right
                var bottom2 = current.bottom
                when (grip) {
                    Grip.MOVE -> {
                        val w = current2 - left2
                        val h = bottom2 - top2
                        left2 = (left2 + dx).coerceIn(0f, maxW - w); top2 = (top2 + dy).coerceIn(0f, maxH - h)
                        current2 = left2 + w; bottom2 = top2 + h
                    }
                    Grip.LEFT -> left2 = (left2 + dx).coerceIn(0f, current2 - minSide)
                    Grip.RIGHT -> current2 = (current2 + dx).coerceIn(left2 + minSide, maxW)
                    Grip.TOP -> top2 = (top2 + dy).coerceIn(0f, bottom2 - minSide)
                    Grip.BOTTOM -> bottom2 = (bottom2 + dy).coerceIn(top2 + minSide, maxH)
                    Grip.TL, Grip.TR, Grip.BL, Grip.BR -> {
                        val left = grip == Grip.TL || grip == Grip.BL
                        val top = grip == Grip.TL || grip == Grip.TR
                        if (fixedSquare) {
                            // keep it square: follow the larger movement, anchored at the opposite corner
                            val delta = if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) (if (left) -dx else dx) else (if (top) -dy else dy)
                            val side = (current2 - left2 + delta).coerceAtLeast(minSide)
                            val anchorX = if (left) current2 else left2
                            val anchorY = if (top) bottom2 else top2
                            val room = minOf(if (left) anchorX else maxW - anchorX, if (top) anchorY else maxH - anchorY)
                            val s = side.coerceAtMost(room)
                            if (left) { left2 = anchorX - s; current2 = anchorX } else { left2 = anchorX; current2 = anchorX + s }
                            if (top) { top2 = anchorY - s; bottom2 = anchorY } else { top2 = anchorY; bottom2 = anchorY + s }
                        } else {
                            if (left) left2 = (left2 + dx).coerceIn(0f, current2 - minSide) else current2 = (current2 + dx).coerceIn(left2 + minSide, maxW)
                            if (top) top2 = (top2 + dy).coerceIn(0f, bottom2 - minSide) else bottom2 = (bottom2 + dy).coerceIn(top2 + minSide, maxH)
                        }
                    }
                    else -> {}
                }
                current = Rect(left2, top2, current2, bottom2)
                onCrop(current)
            }
        }.then(Modifier.pointerInput(Unit) {}),
    ) {
        drawImage(
            image,
            dstOffset = IntOffset(originX.toInt(), originY.toInt()),
            dstSize = IntSize((bitmap.width * scale).toInt(), (bitmap.height * scale).toInt()),
        )
        val box = Rect(originX + crop.left * scale, originY + crop.top * scale, originX + crop.right * scale, originY + crop.bottom * scale)
        val dim = Color(0x99000000)
        val full = Rect(originX, originY, originX + bitmap.width * scale, originY + bitmap.height * scale)
        drawRect(dim, Offset(full.left, full.top), Size(full.width, box.top - full.top))
        drawRect(dim, Offset(full.left, box.bottom), Size(full.width, full.bottom - box.bottom))
        drawRect(dim, Offset(full.left, box.top), Size(box.left - full.left, box.height))
        drawRect(dim, Offset(box.right, box.top), Size(full.right - box.right, box.height))
        drawRect(Color.White, box.topLeft, box.size, style = Stroke(2.dp.toPx()))
        val third = Color(0x66FFFFFF)
        for (index in 1..2) {
            drawLine(third, Offset(box.left + box.width * index / 3, box.top), Offset(box.left + box.width * index / 3, box.bottom), 1.dp.toPx())
            drawLine(third, Offset(box.left, box.top + box.height * index / 3), Offset(box.right, box.top + box.height * index / 3), 1.dp.toPx())
        }
        for (corner in listOf(box.topLeft, box.topRight, box.bottomLeft, box.bottomRight)) {
            drawCircle(accent, 9.dp.toPx(), corner)
            drawCircle(Color.White, 9.dp.toPx(), corner, style = Stroke(2.dp.toPx()))
        }
    }
}
