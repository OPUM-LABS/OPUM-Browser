package com.mycompany.app.ui.viewer

import android.Manifest
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.PdfSettings
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.BitmapUtil
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.ui.OpumComposeActivity

import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * The browser's background pictures: one for portrait, one for landscape. Each card shows the current picture; tap an
 * empty card to choose a picture (gallery, camera or web address) or a filled card to move / zoom it again, then
 * "Crop and save" keeps what the frame shows. "Apply" asks for the size (original or reduced), writes the files the
 * browser reads (`MainUtil.backupDirPath2`), copies one orientation to the other when only one was set, and returns `EXTRA_PATH`.
 * With `EXTRA_PATH` (a picture to use, with `EXTRA_REFERER`) it opens straight in the crop. Replaces `MainImageQuick`.
 */
class BackgroundImageActivity : OpumComposeActivity() {

    private var portrait by mutableStateOf<Bitmap?>(null)
    private var landscape by mutableStateOf<Bitmap?>(null)
    private var portraitChanged = false
    private var landscapeChanged = false
    private var loadingStored by mutableStateOf(true)
    private var working by mutableStateOf(false)
    private var editing by mutableStateOf<Pair<Boolean, Bitmap>?>(null) // (landscape, source)
    private var cameraUri: Uri? = null
    private var cameraTarget = false
    private var pendingPickTarget = false

    private var startPath: String? = null
    private var startReferer: String? = null

    private val screenLong: Int get() = max(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
    private val screenShort: Int get() = minOf(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        startPath = intent.getStringExtra("EXTRA_PATH")
        startReferer = intent.getStringExtra("EXTRA_REFERER")
        super.onCreate(savedInstanceState)
        MainUtil.applyScreenOrientation(this)
        Thread {
            val context = applicationContext
            val portraitImage = decode(MainUtil.backupDirPath2(context, SyncSettings.secretMode.value, false))
            val landscapeImage = decode(MainUtil.backupDirPath2(context, SyncSettings.secretMode.value, true))
            runOnUiThread {
                portrait = portraitImage
                landscape = landscapeImage
                loadingStored = false
                if (!TextUtils.isEmpty(startPath)) loadSource(startPath, null, startReferer, resources.displayMetrics.widthPixels > resources.displayMetrics.heightPixels)
            }
        }.start()
    }

    override fun onResume() {
        super.onResume()
        MainUtil.applyWindowBrightness(window, PdfSettings.brightness.value, PdfSettings.userBrightness.value)
    }

    override fun finish() {
        if (working) return
        super.finish()
    }

    private fun decode(path: String?): Bitmap? {
        if (path.isNullOrEmpty() || !File(path).exists()) return null
        return try { BitmapFactory.decodeFile(path) } catch (_: Throwable) { null }
    }

    /** Loads a picture from a path / address / URI and opens it in the crop for the given orientation. */
    private fun loadSource(path: String?, uri: Uri?, referer: String?, land: Boolean) {
        working = true
        Thread {
            val bitmap = ImageSource.load(applicationContext, path, uri, referer)
            runOnUiThread {
                working = false
                if (bitmap == null) MainUtil.showToast(this, R.string.image_fail) else editing = Pair(land, bitmap)
            }
        }.start()
    }

    /** The part of [source] that a frame of the orientation shows, scaled to the screen's pixel size. */
    private fun cropToScreen(source: Bitmap, land: Boolean, left: Float, top: Float, scale: Float, frameW: Int, frameH: Int): Bitmap? {
        return try {
            val x = (-left / scale).roundToInt().coerceIn(0, source.width - 1)
            val y = (-top / scale).roundToInt().coerceIn(0, source.height - 1)
            val w = (frameW / scale).roundToInt().coerceIn(1, source.width - x)
            val h = (frameH / scale).roundToInt().coerceIn(1, source.height - y)
            val cropped = Bitmap.createBitmap(source, x, y, w, h)
            val targetW = if (land) screenLong else screenShort
            val targetH = if (land) screenShort else screenLong
            if (cropped.width == targetW && cropped.height == targetH) cropped else Bitmap.createScaledBitmap(cropped, targetW, targetH, true)
        } catch (_: Throwable) {
            null
        }
    }

    private fun saveFile(bitmap: Bitmap?, land: Boolean, reduced: Boolean): Boolean {
        val context = applicationContext
        if (bitmap == null) return false
        return try {
            var out: Bitmap = bitmap
            if (reduced) MainUtil.scaleBitmap(bitmap.width / 2, bitmap.height / 2, bitmap)?.let { out = it }
            val path = MainUtil.backupDirPath2(context, SyncSettings.secretMode.value, land)
            if (!MainUtil.saveBitmap(context, out, path, if (out.hasAlpha()) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG)) return false
            val cached = BitmapUtil.decodeFile(path)
            if (!TextUtils.isEmpty(path) && MainUtil.isBitmapOk(cached)) {
                com.mycompany.app.glide.ImageCache.put(com.mycompany.app.glide.ImageCache.key(0, path), cached)
            }
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun apply(reduced: Boolean) {
        working = true
        Thread {
            val context = applicationContext
            val portraitPath = MainUtil.backupDirPath2(context, SyncSettings.secretMode.value, false)
            val landscapePath = MainUtil.backupDirPath2(context, SyncSettings.secretMode.value, true)
            val portraitNow = portrait
            val landscapeNow = landscape
            var okPortrait = portraitNow != null && (!portraitChanged || saveFile(portraitNow, false, reduced))
            var okLandscape = landscapeNow != null && (!landscapeChanged || saveFile(landscapeNow, true, reduced))
            if (okPortrait && !okLandscape && landscapeNow == null) {
                okLandscape = MainUtil.copyFile(portraitPath, landscapePath)
            } else if (okLandscape && !okPortrait && portraitNow == null) {
                okPortrait = MainUtil.copyFile(landscapePath, portraitPath)
            }
            val ok = okPortrait || okLandscape
            runOnUiThread {
                working = false
                if (!ok) {
                    MainUtil.showToast(this, R.string.fail)
                } else {
                    MainUtil.showToast(this, R.string.setted)
                    setResult(RESULT_OK, android.content.Intent().putExtra("EXTRA_PATH", portraitPath))
                    finish()
                }
            }
        }.start()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context = this
        var sourceMenu by remember { mutableStateOf<Boolean?>(null) } // which card's source menu is open (landscape flag)
        var urlDialog by remember { mutableStateOf<Boolean?>(null) }
        var sizeSheet by remember { mutableStateOf(false) }
        val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                MainUtil.keepUriPermission(applicationContext, uri)
                loadSource(null, uri, null, pendingPickTarget)
            }
        }
        val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            val uri = cameraUri
            cameraUri = null
            if (ok && uri != null) loadSource(null, uri, null, cameraTarget) else MainUtil.showToast(this, R.string.invalid_path)
        }
        fun takePhoto(land: Boolean) {
            val name = MainUtil.newCacheFilePath(applicationContext)
            val uri = if (name.isNullOrEmpty()) null else MainUtil.fileProviderUri(applicationContext, "$name.jpg")
            if (uri == null) { MainUtil.showToast(this, R.string.not_supported); return }
            cameraUri = uri
            cameraTarget = land
            try { camera.launch(uri) } catch (_: Exception) { MainUtil.showToast(this, R.string.not_supported) }
        }
        val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) takePhoto(cameraTarget)
        }

        val current = editing
        if (current != null) {
            FrameEditor(
                source = current.second,
                land = current.first,
                onCancel = { editing = null },
                onDone = { result ->
                    if (current.first) { landscape = result; landscapeChanged = true } else { portrait = result; portraitChanged = true }
                    editing = null
                },
            )
        } else {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.fillMaxSize().statusBarsPadding()) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { finish() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                        Text(stringResource(R.string.background), style = MaterialTheme.typography.titleLarge)
                    }
                    Column(
                        Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        for (land in listOf(false, true)) {
                            val bitmap = if (land) landscape else portrait
                            Text(stringResource(if (land) R.string.view_land else R.string.view_port), style = MaterialTheme.typography.titleMedium)
                            Box(
                                Modifier.fillMaxWidth(if (land) 1f else 0.5f).aspectRatio(if (land) screenLong / screenShort.toFloat() else screenShort / screenLong.toFloat())
                                    .align(Alignment.CenterHorizontally)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        if (bitmap == null) sourceMenu = land else editing = Pair(land, bitmap)
                                    },
                            ) {
                                if (bitmap != null) {
                                    Image(bitmap.asImageBitmap(), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                    IconButton(onClick = { sourceMenu = land }, modifier = Modifier.align(Alignment.TopEnd)) {
                                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.image), tint = Color.White)
                                    }
                                } else if (loadingStored) {
                                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                                } else {
                                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.align(Alignment.Center))
                                }
                                com.mycompany.app.ui.components.BubbleMenu(expanded = sourceMenu == land, onDismiss = { sourceMenu = null }) {
                                    DropdownMenuItem(text = { Text(stringResource(R.string.image)) }, onClick = {
                                        sourceMenu = null
                                        pendingPickTarget = land
                                        try { pick.launch(arrayOf("image/*")) } catch (_: Exception) { MainUtil.showToast(context, R.string.apps_none) }
                                    })
                                    DropdownMenuItem(text = { Text(stringResource(R.string.camera)) }, onClick = {
                                        sourceMenu = null
                                        cameraTarget = land
                                        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) takePhoto(land)
                                        else cameraPermission.launch(Manifest.permission.CAMERA)
                                    })
                                    DropdownMenuItem(text = { Text(stringResource(R.string.url)) }, onClick = { sourceMenu = null; urlDialog = land })
                                }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().navigationBarsPadding()) {
                        TextButton(onClick = { finish() }, modifier = Modifier.weight(1f).height(56.dp)) { Text(stringResource(R.string.cancel)) }
                        TextButton(
                            onClick = { sizeSheet = true },
                            enabled = (portrait != null || landscape != null) && !working,
                            modifier = Modifier.weight(1f).height(56.dp),
                        ) { Text(stringResource(R.string.apply)) }
                    }
                }
            }
        }

        urlDialog?.let { land ->
            var url by remember { mutableStateOf("") }
            com.mycompany.app.ui.components.OpumSheetDialog(
                onDismissRequest = { urlDialog = null },
                title = { Text(stringResource(R.string.url)) },
                text = { OutlinedTextField(value = url, onValueChange = { url = it }, singleLine = true, placeholder = { Text("https://...") }) },
                confirmButton = {
                    TextButton(onClick = {
                        urlDialog = null
                        if (url.isBlank()) MainUtil.showToast(context, R.string.invalid_path) else loadSource(url.trim(), null, null, land)
                    }) { Text(stringResource(R.string.ok)) }
                },
                dismissButton = { TextButton(onClick = { urlDialog = null }) { Text(stringResource(R.string.cancel)) } },
            )
        }

        if (sizeSheet) {
            var reduced by remember { mutableStateOf(true) }
            ModalBottomSheet(onDismissRequest = { sizeSheet = false }) {
                Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                    Row(Modifier.fillMaxWidth().selectable(selected = !reduced, onClick = { reduced = false }), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = !reduced, onClick = null)
                        Column(Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp)) {
                            Text(stringResource(R.string.original_size), style = MaterialTheme.typography.bodyLarge)
                            Text(stringResource(R.string.memory_warning_1), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Row(Modifier.fillMaxWidth().selectable(selected = reduced, onClick = { reduced = true }), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = reduced, onClick = null)
                        Text(stringResource(R.string.reduced_size), Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp), style = MaterialTheme.typography.bodyLarge)
                    }
                    TextButton(onClick = { sizeSheet = false; apply(reduced) }, modifier = Modifier.align(Alignment.End)) { Text(stringResource(R.string.apply)) }
                }
            }
        }

        if (working) {
            Box(Modifier.fillMaxSize().background(Color(0x99000000)).pointerInput(Unit) { detectTransformGestures { _, _, _, _ -> } }) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        }
    }

    /** Full screen: the picture moves and zooms under a frame with the screen's shape; "Crop and save" keeps it. */
    @Composable
    private fun FrameEditor(source: Bitmap, land: Boolean, onCancel: () -> Unit, onDone: (Bitmap) -> Unit) {
        var area by remember { mutableStateOf(IntSize.Zero) }
        var zoom by remember(source) { mutableFloatStateOf(1f) }
        var offset by remember(source) { mutableStateOf(Offset.Zero) }
        val image = remember(source) { source.asImageBitmap() }
        val aspect = if (land) screenLong / screenShort.toFloat() else screenShort / screenLong.toFloat()

        Box(Modifier.fillMaxSize().background(Color.Black).statusBarsPadding().navigationBarsPadding()) {
            Box(Modifier.fillMaxSize().onSizeChanged { area = it }) {
                if (area.width > 0 && area.height > 0) {
                    val margin = 24.dp
                    val marginPx = with(androidx.compose.ui.platform.LocalDensity.current) { margin.toPx() }
                    val barPx = with(androidx.compose.ui.platform.LocalDensity.current) { 56.dp.toPx() }
                    val maxW = area.width - 2 * marginPx
                    val maxH = area.height - barPx - 2 * marginPx
                    val frameW = minOf(maxW, maxH * aspect)
                    val frameH = frameW / aspect
                    val frameLeft = (area.width - frameW) / 2f
                    val frameTop = (area.height - barPx - frameH) / 2f
                    val cover = max(frameW / source.width, frameH / source.height)
                    val scale = cover * zoom
                    val shownW = source.width * scale
                    val shownH = source.height * scale
                    val limitX = (shownW - frameW) / 2f
                    val limitY = (shownH - frameH) / 2f
                    val clamped = Offset(offset.x.coerceIn(-limitX, limitX), offset.y.coerceIn(-limitY, limitY))
                    val imgLeft = frameLeft + frameW / 2f - shownW / 2f + clamped.x
                    val imgTop = frameTop + frameH / 2f - shownH / 2f + clamped.y

                    Canvas(
                        Modifier.fillMaxSize().pointerInput(source) {
                            detectTransformGestures { _, pan, gestureZoom, _ ->
                                zoom = (zoom * gestureZoom).coerceIn(1f, 8f)
                                offset += pan
                            }
                        },
                    ) {
                        // the whole picture dimmed, then the frame area at full brightness
                        drawImage(image, dstOffset = IntOffset(imgLeft.roundToInt(), imgTop.roundToInt()), dstSize = IntSize(shownW.roundToInt(), shownH.roundToInt()), alpha = 0.4f)
                        clipRect(frameLeft, frameTop, frameLeft + frameW, frameTop + frameH) {
                            drawImage(image, dstOffset = IntOffset(imgLeft.roundToInt(), imgTop.roundToInt()), dstSize = IntSize(shownW.roundToInt(), shownH.roundToInt()))
                        }
                        drawRect(Color.White, Offset(frameLeft, frameTop), androidx.compose.ui.geometry.Size(frameW, frameH), style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
                    }
                    Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.Black)) {
                        TextButton(onClick = onCancel, modifier = Modifier.weight(1f).height(56.dp)) { Text(stringResource(R.string.cancel), color = Color.White, textAlign = TextAlign.Center) }
                        TextButton(
                            onClick = {
                                val result = cropToScreen(source, land, imgLeft - frameLeft, imgTop - frameTop, scale, frameW.roundToInt(), frameH.roundToInt())
                                if (result == null) MainUtil.showToast(this@BackgroundImageActivity, R.string.fail) else onDone(result)
                            },
                            modifier = Modifier.weight(1f).height(56.dp),
                        ) { Text(stringResource(R.string.crop_save), color = Color.White, textAlign = TextAlign.Center) }
                    }
                }
            }
        }
    }
}
