package com.mycompany.app.ui.viewer

import com.mycompany.app.ui.components.AppSlider
import android.Manifest
import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.PdfSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.text.TextUtils
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.book.DbBookDown
import com.mycompany.app.dialog.DialogDownEdit
import com.mycompany.app.dialog.DialogDownPage.DownPageListener
import com.mycompany.app.dialog.DialogSaveConfirm
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.editor.EditorEmoji
import com.mycompany.app.editor.core.PhotoDrawView
import com.mycompany.app.editor.core.PhotoEditor
import com.mycompany.app.editor.core.PhotoEditor.PhotoListener
import com.mycompany.app.editor.core.PhotoEditorView
import com.mycompany.app.editor.core.PhotoEffectView
import com.mycompany.app.editor.core.PhotoEffectView.PhotoSaveListener
import com.mycompany.app.editor.core.PhotoTouchListener.ObjHolder
import com.mycompany.app.editor.core.TextureRenderer
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.GradientPalette
import kotlin.math.roundToInt

/**
 * Photo editor: draw with a pen or erase, add text and emoji stickers (move, scale, rotate them with two fingers), apply
 * one of 23 effects, undo / redo / reset, then save (name and folder chosen in the old "save as" dialog) or share.
 * Source: the intent's data (`EDIT` action) or `EXTRA_PATH` (+ `EXTRA_TYPE`, `EXTRA_REFERER`); without one you pick a
 * picture or take one. On exit with changes it asks to save and returns `EXTRA_PATH` of the saved file. The drawing /
 * effect / sticker engine is the old `editor/core` code hosted in the page; this replaces `EditorActivity`.
 */
class ImageEditActivity : OpumComposeActivity() {

    private var source: String? = null
    private var referer: String? = null

    private var editorView: PhotoEditorView? = null
    private var editor: PhotoEditor? = null
    private var hasImage by mutableStateOf(false)
    private var loading by mutableStateOf(false)
    private var busy by mutableStateOf(false)
    private var canUndo by mutableStateOf(false)
    private var canRedo by mutableStateOf(false)
    private var effect by mutableIntStateOf(0)
    private var eraser by mutableStateOf(false)
    private var textTarget by mutableStateOf<TextTarget?>(null)
    private var cameraUri: Uri? = null

    private class TextTarget(val frame: FrameLayout?, val text: String?, val color: Int)

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        val data = intent.data
        if (data != null) {
            source = data.toString()
        } else {
            source = intent.getStringExtra("EXTRA_PATH")
            referer = intent.getStringExtra("EXTRA_REFERER")
        }
        if (ReadSettings.penSize.value < 1 || ReadSettings.penSize.value > 40) ReadSettings.editLocal { set(ReadSettings.penSize, 10) }
        if (ReadSettings.penAlpha.value < 0 || ReadSettings.penAlpha.value > 90) ReadSettings.editLocal { set(ReadSettings.penAlpha, 0) }
        if (ReadSettings.eraseSize.value < 1 || ReadSettings.eraseSize.value > 40) ReadSettings.editLocal { set(ReadSettings.eraseSize, 10) }
        super.onCreate(savedInstanceState)
        if (!TextUtils.isEmpty(source)) loadPicture(source, null, referer)
    }

    override fun onResume() {
        super.onResume()
        MainUtil.applyWindowBrightness(window, PdfSettings.brightness.value, PdfSettings.userBrightness.value)
    }

    override fun finish() {
        if (busy) return
        super.finish()
    }

    /** The old page's views, assembled the same way (image, GL effect layer, drawing layer). */
    private fun buildEditorView(context: Context): FrameLayout {
        val view = PhotoEditorView(context)
        view.imageViewId = R.id.photo_editor_image
        view.effectKind = 0
        val image = ImageView(context)
        view.imageView = image
        image.id = view.imageViewId
        image.adjustViewBounds = true
        val effectView = PhotoEffectView(context)
        effectView.renderer = TextureRenderer()
        effectView.textures = IntArray(2)
        effectView.effectKind = 0
        effectView.setEGLContextClientVersion(2)
        effectView.setRenderer(effectView)
        effectView.renderMode = 0
        effectView.requestRender()
        view.effectView = effectView
        effectView.visibility = android.view.View.GONE
        val draw = PhotoDrawView(context, null)
        draw.setup(context)
        view.drawView = draw
        view.addView(view.imageView, view.childParams(true))
        view.addView(view.effectView, view.childParams(false))
        view.addView(view.drawView, view.childParams(false))
        editorView = view
        editor = PhotoEditor(context, view, object : PhotoListener {
            override fun onUndoRedo(canUndo: Boolean, canRedo: Boolean) { this@ImageEditActivity.canUndo = canUndo; this@ImageEditActivity.canRedo = canRedo }
            override fun addTextView(frameLayout: FrameLayout?, label: String?, color: Int) { textTarget = TextTarget(frameLayout, label, color) }
        })
        return FrameLayout(context).also {
            it.addView(view, FrameLayout.LayoutParams(-1, -2).apply { gravity = Gravity.CENTER })
        }
    }

    private fun loadPicture(path: String?, uri: Uri?, ref: String?) {
        loading = true
        Thread {
            val bitmap = ImageSource.load(applicationContext, path, uri, ref)
            runOnUiThread {
                loading = false
                if (bitmap == null) {
                    MainUtil.showToast(this, R.string.image_fail)
                    return@runOnUiThread
                }
                if (!path.isNullOrEmpty()) source = path else if (uri != null) source = uri.path
                hasImage = true
                editor?.clearAll()
                effect = 0
                eraser = false
                editorView?.drawView?.setEraseMode(false)
                editorView?.imageBitmap = bitmap
            }
        }.start()
    }

    private fun applyPen() {
        val draw = editor?.brushView ?: return
        val paint = draw.drawPaint ?: return
        val size = ReadSettings.penSize.value.toFloat()
        if (draw.penSize != size) {
            draw.penSize = size
            paint.strokeWidth = MainUtil.dpToPx(draw.hostContext, size)
        }
        if (draw.penColor != ReadSettings.penColor.value) {
            draw.penColor = ReadSettings.penColor.value
            draw.penAlpha = ReadSettings.penAlpha.value
            paint.color = ReadSettings.penColor.value
            paint.alpha = MainUtil.alphaFromPercent(draw.penAlpha)
        } else if (draw.penAlpha != ReadSettings.penAlpha.value) {
            draw.penAlpha = ReadSettings.penAlpha.value
            paint.alpha = MainUtil.alphaFromPercent(ReadSettings.penAlpha.value)
        }
    }

    private fun addSticker(color: Int, type: Int, starts: String?) {
        editor?.addText(color, type, starts)
    }

    private fun updateTextSticker(frame: FrameLayout, color: Int, labelText: String) {
        val current = editor ?: return
        if (!current.addedViews.contains(frame) || TextUtils.isEmpty(labelText)) return
        val tag = frame.tag
        val holder = tag as? ObjHolder ?: return
        val label = holder.appCompatTextView ?: return
        label.setTextColor(color)
        label.text = labelText
        current.photoEditorView!!.updateViewLayout(frame, frame.layoutParams)
    }

    // ---- saving ----

    private fun capture(share: Boolean, thenReturn: Boolean) {
        val current = editor ?: return
        busy = true
        current.save(object : PhotoSaveListener {
            override fun onSaved(bitmap: Bitmap?) {
                busy = false
                if (!MainUtil.isBitmapOk(bitmap)) {
                    MainUtil.showToast(this@ImageEditActivity, R.string.image_fail)
                    return
                }
                val alpha = bitmap!!.hasAlpha()
                if (share) {
                    var name = MainUtil.guessDownloadName(source, null, null, true)
                    if (!com.mycompany.app.compress.Compress.isImageName(name!!, true, true)) {
                        name = MainUtil.guessDownloadName(source, null, if (alpha) "image/png" else "image/jpg", true)
                    }
                    writeFile(MainUtil.cacheFilePath(applicationContext, name), bitmap, true, thenReturn)
                    return
                }
                val suggestion = MainUtil.stripExtension(MainUtil.guessDownloadName(source, null, if (alpha) "image/png" else "image/jpg", true))
                var dialog: DialogDownEdit? = null
                val savedBitmap = bitmap
                com.mycompany.app.ui.prefs.DialogShow.direct(this@ImageEditActivity as android.app.Activity, requestCodes = intArrayOf(19), onResult = { c, r, intent -> dialog?.onPickResult(c, r, intent) }) { host ->
                    DialogDownEdit(host, suggestion, bitmap, object : DownPageListener {
                        override fun onSave(url: String?, path: String?, bitmap: Bitmap?) {
                            dialog?.dismiss()
                            runOnUiThread { writeFile(path, bitmap ?: savedBitmap, false, thenReturn) }
                        }
                    }).also { dialog = it }
                }
            }

            override fun onFailed() {
                busy = false
                MainUtil.showToast(this@ImageEditActivity, R.string.image_fail)
            }
        })
    }

    private fun writeFile(path: String?, bitmap: Bitmap?, share: Boolean, thenReturn: Boolean) {
        busy = true
        val context = applicationContext
        Thread {
            var ok = false
            try {
                if (MainUtil.isBitmapOk(bitmap)) {
                    val format = if (bitmap!!.hasAlpha()) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
                    ok = MainUtil.saveBitmap(context, bitmap, path, format)
                    val item = if (ok) MainUri.createIn(context, path, MainUri.storageVolume()) else null
                    if (ok && item != null) DbBookDown.addFile(context, path, null, item)
                }
            } catch (_: Throwable) {
            }
            runOnUiThread {
                busy = false
                if (!ok) {
                    MainUtil.showToast(this, R.string.fail)
                } else if (share) {
                    MainUtil.shareFile(4, this, path, null, null)
                } else if (thenReturn) {
                    setResult(RESULT_OK, Intent().putExtra("EXTRA_PATH", path))
                    finish()
                } else {
                    MainUtil.showToast(this, R.string.save_success)
                }
            }
        }.start()
    }

    // ---- screen ----

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        var menu by remember { mutableStateOf(false) }
        var effects by remember { mutableStateOf(false) }
        var sheet by remember { mutableIntStateOf(0) } // 1 pen, 2 eraser, 3 emoji
        var resetAsk by remember { mutableStateOf(false) }
        val container = remember { buildEditorView(this) }
        val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                MainUtil.keepUriPermission(applicationContext, uri)
                loadPicture(null, uri, null)
            }
        }
        val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            val uri = cameraUri
            cameraUri = null
            if (ok && uri != null) loadPicture(null, uri, null) else MainUtil.showToast(this, R.string.invalid_path)
        }
        fun takePhoto() {
            val name = MainUtil.newCacheFilePath(applicationContext)
            val uri = if (name.isNullOrEmpty()) null else MainUtil.fileProviderUri(applicationContext, "$name.jpg")
            if (uri == null) { MainUtil.showToast(this, R.string.not_supported); return }
            cameraUri = uri
            try { camera.launch(uri) } catch (_: Exception) { MainUtil.showToast(this, R.string.not_supported) }
        }
        val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted -> if (granted) takePhoto() }
        fun openCamera() {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) takePhoto()
            else cameraPermission.launch(Manifest.permission.CAMERA)
        }
        fun pickImage() { try { pick.launch(arrayOf("image/*")) } catch (_: Exception) { MainUtil.showToast(this, R.string.apps_none) } }

        fun leave() {
            if (busy) return
            val edited = hasImage && (canUndo || effect != 0)
            if (!edited) { finish(); return }
            DialogSaveConfirm(this, object : DialogSeekListener {
                override fun onValue(newValue: Int) { if (newValue == 0) capture(false, true) else finish() }
            })
        }
        BackHandler { leave() }

        Surface(Modifier.fillMaxSize(), color = Color.Black) {
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize().statusBarsPadding().padding(top = 56.dp, bottom = if (effects) 200.dp else 100.dp)) {
                    AndroidView(
                        factory = { container },
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (!hasImage && !loading) {
                        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            TextButton(onClick = { pickImage() }) {
                                Icon(painterResource(R.drawable.outline_image_white_24), null, tint = Color.White)
                                Text(stringResource(R.string.image), Modifier.padding(start = 12.dp), color = Color.White, fontSize = 18.sp)
                            }
                            TextButton(onClick = { openCamera() }) {
                                Icon(painterResource(R.drawable.outline_photo_camera_white_24), null, tint = Color.White)
                                Text(stringResource(R.string.camera), Modifier.padding(start = 12.dp), color = Color.White, fontSize = 18.sp)
                            }
                        }
                    }
                    if (loading) CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                Row(
                    Modifier.fillMaxWidth().background(Color(0xCC000000)).statusBarsPadding().padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { leave() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                    Box(Modifier.weight(1f))
                    if (hasImage) {
                        IconButton(onClick = { editor?.undo() }, enabled = canUndo) { Icon(painterResource(R.drawable.outline_undo_white_24), null, tint = if (canUndo) Color.White else Color.Gray) }
                        IconButton(onClick = { editor?.redo() }, enabled = canRedo) { Icon(painterResource(R.drawable.outline_redo_white_24), null, tint = if (canRedo) Color.White else Color.Gray) }
                        IconButton(onClick = { resetAsk = true }) { Icon(painterResource(R.drawable.outline_replay_white_24), stringResource(R.string.reset), tint = Color.White) }
                        IconButton(onClick = { capture(false, false) }) { Icon(painterResource(R.drawable.outline_download_white_24), stringResource(R.string.save), tint = Color.White) }
                        IconButton(onClick = { capture(true, false) }) { Icon(Icons.Filled.Share, stringResource(R.string.share), tint = Color.White) }
                    }
                    Box {
                        IconButton(onClick = { menu = true }) { Icon(Icons.Filled.MoreVert, null, tint = Color.White) }
                        com.mycompany.app.ui.components.BubbleMenu(expanded = menu, onDismiss = { menu = false }) {
                            DropdownMenuItem(text = { Text(stringResource(R.string.image)) }, onClick = { menu = false; pickImage() })
                            DropdownMenuItem(text = { Text(stringResource(R.string.camera)) }, onClick = { menu = false; openCamera() })
                        }
                    }
                }

                if (hasImage) {
                    Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color(0xCC000000)).navigationBarsPadding()) {
                        if (effects) {
                            LazyRow(Modifier.fillMaxWidth().height(100.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                itemsIndexed(PhotoEffectView.effectNames.toList()) { index, label ->
                                    Box(
                                        Modifier.size(100.dp).clickable { editorView?.setEffectType(index); effect = index }
                                            .padding(4.dp).clip(RoundedCornerShape(8.dp)),
                                    ) {
                                        Image(painterResource(PhotoEffectView.effectIcons[index]), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                        Text(
                                            label ?: "", Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                                                .background(if (index == effect) Color(0xAA3F51B5) else Color(0x99000000)).padding(2.dp),
                                            color = Color.White, fontSize = 8.sp, textAlign = TextAlign.Center,
                                        )
                                    }
                                }
                            }
                        }
                        Row(Modifier.fillMaxWidth().height(56.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                            ToolButton(R.drawable.outline_edit_white_24, !eraser) {
                                if (!eraser) sheet = 1 else { eraser = false; editor?.brushView?.setEraseMode(false) }
                            }
                            ToolButton(R.drawable.baseline_erase_white_24, eraser) {
                                if (eraser) sheet = 2 else { eraser = true; editor?.brushView?.setEraseMode(true) }
                            }
                            ToolButton(R.drawable.outline_text_format_white_24, false) { textTarget = TextTarget(null, null, 0) }
                            ToolButton(R.drawable.outline_sentiment_satisfied_white_24, false) { sheet = 3 }
                            ToolButton(R.drawable.outline_photo_filter_white_24, effects) { effects = !effects }
                        }
                    }
                }

                if (busy) {
                    Box(Modifier.fillMaxSize().background(Color(0x99000000)).clickable(enabled = true, onClick = {})) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                }
            }
        }

        if (resetAsk) {
            ConfirmDialog(
                message = stringResource(R.string.reset_confirm),
                title = stringResource(R.string.reset),
                onConfirm = { editor?.clearAll(); effect = 0 },
                onDismiss = { resetAsk = false },
            )
        }

        if (sheet != 0) {
            ModalBottomSheet(onDismissRequest = { sheet = 0 }) {
                Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                    when (sheet) {
                        1 -> PenControls(onApplied = { applyPen(); sheet = 0 })
                        2 -> EraserControls(onApplied = { sheet = 0 })
                        3 -> EmojiGrid(onPick = { addSticker(0, 1, it); sheet = 0 })
                    }
                }
            }
        }

        textTarget?.let { target -> TextDialog(target, onDismiss = { textTarget = null }) }
    }

    @Composable
    private fun ToolButton(icon: Int, active: Boolean, onClick: () -> Unit) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(48.dp).clip(CircleShape).background(if (active) Color(0x553F51B5) else Color.Transparent),
        ) { Icon(painterResource(icon), null, tint = Color.White) }
    }

    @Composable
    private fun androidx.compose.foundation.layout.ColumnScope.PenControls(onApplied: () -> Unit) {
        var size by remember { mutableIntStateOf(ReadSettings.penSize.value) }
        var alpha by remember { mutableIntStateOf(ReadSettings.penAlpha.value) }
        var color by remember { mutableIntStateOf(ReadSettings.penColor.value) }
        var position by remember { mutableFloatStateOf(ReadSettings.penPos.value) }
        Box(
            Modifier.align(Alignment.CenterHorizontally).size(72.dp).clip(RoundedCornerShape(12.dp)).background(Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size((size.coerceAtLeast(2)).dp).clip(CircleShape).background(Color(color).copy(alpha = (100 - alpha) / 100f)))
        }
        Text(stringResource(R.string.color_size) + ": $size", Modifier.padding(top = 12.dp))
        AppSlider(value = size.toFloat(), onValueChange = { size = it.roundToInt() }, valueRange = 1f..40f)
        Text(stringResource(R.string.color_alpha) + ": $alpha%")
        AppSlider(value = alpha.toFloat(), onValueChange = { alpha = it.roundToInt() }, valueRange = 0f..90f)
        ColorPicker(color, position) { color2, position2 -> color = color2; position = position2 }
        TextButton(
            onClick = {
                if (ReadSettings.penSize.value != size || ReadSettings.penAlpha.value != alpha || ReadSettings.penColor.value != color || ReadSettings.penPos.value != position) {
                    ReadSettings.editLocal { set(ReadSettings.penSize, size) }; ReadSettings.editLocal { set(ReadSettings.penAlpha, alpha) }; ReadSettings.editLocal { set(ReadSettings.penColor, color) }; ReadSettings.editLocal { set(ReadSettings.penPos, position) }
                    val prefs = PrefRead.get(applicationContext, false)
                    prefs!!.putInt(ReadSettings.penSize.value, "mPenSize")
                    prefs.putInt(ReadSettings.penAlpha.value, "mPenAlpha")
                    prefs.putInt(ReadSettings.penColor.value, "mPenColor")
                    prefs.putFloat("mPenPos", ReadSettings.penPos.value)
                    prefs.saveAsync()
                }
                onApplied()
            },
            modifier = Modifier.align(Alignment.End),
        ) { Text(stringResource(R.string.apply)) }
    }

    @Composable
    private fun androidx.compose.foundation.layout.ColumnScope.EraserControls(onApplied: () -> Unit) {
        var size by remember { mutableIntStateOf(ReadSettings.eraseSize.value) }
        Text(stringResource(R.string.color_size) + ": $size", style = MaterialTheme.typography.titleMedium)
        AppSlider(value = size.toFloat(), onValueChange = { size = it.roundToInt() }, valueRange = 1f..40f)
        TextButton(
            onClick = {
                if (ReadSettings.eraseSize.value != size) {
                    ReadSettings.edit(applicationContext) { set(ReadSettings.eraseSize, size) }
                }
                editor?.brushView?.setEraseSize(size)
                onApplied()
            },
            modifier = Modifier.align(Alignment.End),
        ) { Text(stringResource(R.string.apply)) }
    }

    @Composable
    private fun EmojiGrid(onPick: (String) -> Unit) {
        LazyVerticalGrid(columns = GridCells.Adaptive(52.dp), modifier = Modifier.height(320.dp)) {
            itemsIndexed(EditorEmoji.emojis.filterNotNull()) { _, emoji ->
                Box(Modifier.size(52.dp).clickable { onPick(emoji) }, contentAlignment = Alignment.Center) {
                    Text(emoji, fontSize = 28.sp)
                }
            }
        }
    }

    /** The old gradient palette (position along the bar gives the colour) plus the eight preset colours. */
    @Composable
    private fun ColorPicker(color: Int, position: Float, onChange: (Int, Float) -> Unit) {
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            MainConst.presetColors.forEachIndexed { index, preset ->
                Box(
                    Modifier.size(32.dp).clip(CircleShape).background(Color(preset))
                        .then(if (preset == color) Modifier.background(Color.Transparent) else Modifier)
                        .clickable { onChange(preset, MainConst.presetPositions[index]) },
                ) {
                    if (preset == color) Box(Modifier.align(Alignment.Center).size(12.dp).clip(CircleShape).background(if (preset == Color.White.hashCode() || preset == -1) Color.Black else Color.White))
                }
            }
        }
        GradientPalette(
            colors = MainConst.presetColors,
            stops = MainConst.presetPositions,
            position = position,
            onPick = { c, p -> onChange(c, p) },
            modifier = Modifier.padding(vertical = 8.dp),
        )
    }

    @Composable
    private fun TextDialog(target: TextTarget, onDismiss: () -> Unit) {
        var labelText by remember { mutableStateOf(target.text ?: "") }
        var color by remember { mutableIntStateOf(if (target.frame != null) target.color else ReadSettings.textColor.value) }
        var position by remember { mutableFloatStateOf(ReadSettings.textPos.value) }
        com.mycompany.app.ui.components.OpumSheetDialog(
            onDismissRequest = onDismiss,
            text = {
                Column {
                    OutlinedTextField(value = labelText, onValueChange = { labelText = it }, label = { Text(stringResource(R.string.empty).let { "Aa" }) }, textStyle = androidx.compose.ui.text.TextStyle(color = Color(color).let { if (it == Color.White) Color.Gray else it }))
                    ColorPicker(color, position) { color2, position2 -> color = color2; position = position2 }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (labelText.isNotBlank()) {
                        if (ReadSettings.textColor.value != color || ReadSettings.textPos.value != position) {
                            ReadSettings.editLocal { set(ReadSettings.textColor, color) }
                            ReadSettings.editLocal { set(ReadSettings.textPos, position) }
                            val prefs = PrefRead.get(applicationContext, false)
                            prefs!!.putInt(ReadSettings.textColor.value, "mTextColor")
                            prefs.putFloat("mTextPos", ReadSettings.textPos.value)
                            prefs.saveAsync()
                        }
                        val frame = target.frame
                        if (frame == null) addSticker(color, 0, labelText) else updateTextSticker(frame, color, labelText)
                    }
                    onDismiss()
                }) { Text(stringResource(R.string.apply)) }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        )
    }
}
