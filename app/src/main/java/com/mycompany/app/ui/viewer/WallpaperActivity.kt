package com.mycompany.app.ui.viewer

import android.app.WallpaperManager
import com.mycompany.app.pref.store.PdfSettings
import android.graphics.Bitmap
import android.text.TextUtils
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.ui.OpumComposeActivity
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Picks the part of a picture that becomes the wallpaper: the picture fills the screen, pinch to zoom and drag to
 * move, "Set wallpaper" applies exactly what is on screen. Source: `EXTRA_PATH` (file, content URI or web address,
 * with `EXTRA_REFERER`). Replaces `MainImageWallpaper`.
 */
class WallpaperActivity : OpumComposeActivity() {

    private var path: String? = null
    private var referer: String? = null
    private var bitmap by mutableStateOf<Bitmap?>(null)
    private var loading by mutableStateOf(true)
    private var saving by mutableStateOf(false)

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        path = intent.getStringExtra("EXTRA_PATH")
        referer = intent.getStringExtra("EXTRA_REFERER")
        if (TextUtils.isEmpty(path)) {
            MainUtil.showToast(this, R.string.invalid_path)
            finish()
            return
        }
        super.onCreate(savedInstanceState)
        Thread {
            val result = ImageSource.load(applicationContext, path, null, referer)
            runOnUiThread {
                bitmap = result
                loading = false
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        MainUtil.applyWindowBrightness(window, PdfSettings.brightness.value, PdfSettings.userBrightness.value)
    }

    override fun finish() {
        if (saving) return
        super.finish()
    }

    /** Sets the part of [source] that is visible in a [width] x [height] view as the wallpaper. */
    private fun apply(source: Bitmap, left: Float, top: Float, scale: Float, width: Int, height: Int) {
        saving = true
        Thread {
            var ok = false
            try {
                val x = (-left / scale).roundToInt().coerceIn(0, source.width - 1)
                val y = (-top / scale).roundToInt().coerceIn(0, source.height - 1)
                val w = (width / scale).roundToInt().coerceIn(1, source.width - x)
                val h = (height / scale).roundToInt().coerceIn(1, source.height - y)
                var crop = Bitmap.createBitmap(source, x, y, w, h)
                val limit = 2 * max(width, height)
                if (max(crop.width, crop.height) > limit) {
                    val factor = limit.toFloat() / max(crop.width, crop.height)
                    crop = Bitmap.createScaledBitmap(crop, (crop.width * factor).roundToInt(), (crop.height * factor).roundToInt(), true)
                }
                WallpaperManager.getInstance(applicationContext).setBitmap(crop)
                ok = true
            } catch (_: Throwable) {
            }
            runOnUiThread {
                saving = false
                MainUtil.showToast(this, if (ok) R.string.setted else R.string.fail)
                if (ok) {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }.start()
    }

    @Composable
    override fun Content() {
        val bmp = bitmap
        var view by remember { mutableStateOf(IntSize.Zero) }
        var zoom by remember(bmp) { mutableFloatStateOf(1f) } // relative to the fill-screen scale
        var offset by remember(bmp) { mutableStateOf(Offset.Zero) } // image centre relative to the view centre

        Box(Modifier.fillMaxSize().background(Color.Black).onSizeChanged { view = it }) {
            if (bmp != null && view.width > 0) {
                val cover = max(view.width / bmp.width.toFloat(), view.height / bmp.height.toFloat())
                val scale = cover * zoom
                val shownW = bmp.width * scale
                val shownH = bmp.height * scale
                val maxX = (shownW - view.width) / 2f
                val maxY = (shownH - view.height) / 2f
                val clamped = Offset(offset.x.coerceIn(-maxX, maxX), offset.y.coerceIn(-maxY, maxY))
                val image = remember(bmp) { bmp.asImageBitmap() }
                val left = view.width / 2f - shownW / 2f + clamped.x
                val top = view.height / 2f - shownH / 2f + clamped.y

                Canvas(
                    Modifier.fillMaxSize().pointerInput(bmp) {
                        detectTransformGestures { _, pan, gestureZoom, _ ->
                            zoom = (zoom * gestureZoom).coerceIn(1f, 8f)
                            offset += pan
                        }
                    },
                ) {
                    drawImage(
                        image,
                        dstOffset = IntOffset(left.roundToInt(), top.roundToInt()),
                        dstSize = IntSize(shownW.roundToInt(), shownH.roundToInt()),
                    )
                }

                Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().background(Color(0x99000000))) {
                    TextButton(onClick = { finish() }, modifier = Modifier.weight(1f).height(56.dp)) {
                        Text(stringResource(R.string.cancel), color = Color.White)
                    }
                    TextButton(
                        onClick = { if (!saving) apply(bmp, left, top, scale, view.width, view.height) },
                        modifier = Modifier.weight(1f).height(56.dp),
                    ) { Text(stringResource(R.string.set_wallpaper), color = Color.White) }
                }
            } else if (loading || bmp != null) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                Text(stringResource(R.string.image_fail), Modifier.align(Alignment.Center), color = Color.White)
            }
            if (saving) {
                Box(Modifier.fillMaxSize().background(Color(0x99000000)).pointerInput(Unit) { detectTransformGestures { _, _, _, _ -> } }) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
        }
    }
}
