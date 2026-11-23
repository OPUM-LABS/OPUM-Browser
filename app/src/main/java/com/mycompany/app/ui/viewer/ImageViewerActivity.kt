package com.mycompany.app.ui.viewer

import com.mycompany.app.list.ListTaskCmp
import com.mycompany.app.list.ListTaskPdf
import com.mycompany.app.ui.components.AppSlider
import android.content.Context
import com.mycompany.app.pref.store.ListSettings
import com.mycompany.app.pref.store.ImageSettings
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.TextUtils
import android.util.LruCache
import android.view.KeyEvent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.compress.Compress
import com.mycompany.app.compress.CompressUtil
import com.mycompany.app.compress.CompressUtilAlbum
import com.mycompany.app.compress.CompressUtilZip
import com.mycompany.app.compress.CompressUtilZip2
import com.mycompany.app.data.DataAlbum
import com.mycompany.app.data.DataCmp
import com.mycompany.app.data.DataPdf
import com.mycompany.app.data.book.DataBookAlbum
import com.mycompany.app.data.book.DataBookCmp
import com.mycompany.app.data.book.DataBookPdf
import com.mycompany.app.db.book.DbBookAlbum
import com.mycompany.app.db.book.DbBookCmp
import com.mycompany.app.db.book.DbBookPdf
import com.mycompany.app.dialog.DialogSetImage
import com.mycompany.app.dialog.DialogSetImage.ChangedListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefImage
import com.mycompany.app.pref.PrefList
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.settings.ImageSettingsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Reader for picture books: image albums (`EXTRA_TYPE` 1), PDF files (2), ZIP / TAR archives (3) and the pictures of a
 * web page (12), opened with `EXTRA_INDEX` (the book in its list, or the first page for web pictures) and the other
 * extras of the old viewer; a ZIP / PDF opened from another app (VIEW intent) is read straight from its address.
 * Pages are read through [Compress] like before, so the same files, passwords-free archives, sorting and saved reading
 * positions keep working. Swipe or tap the edge areas to turn pages (direction, view mode per orientation, tap areas,
 * volume keys and thumbnail strip follow the existing image settings), pinch / double-tap to zoom, tap the middle for
 * the page slider and tools. Page-curl effects and the other per-mode details of the old viewer are gone; the
 * continuous modes share one vertical list. Replaces `ImageViewActivity` and its reader classes.
 */
open class ImageViewerActivity : OpumComposeActivity() {

    /** True for the PDF entry point. */
    protected open val pdfEntry: Boolean = false

    private var type = 0
    private var bookIndex = -1
    private var path: String? = null
    private var title: String? = null
    private var referer: String? = null
    private var loadType = 0
    private var startPage = 0
    private var compress: Compress? = null
    private var zip: java.util.zip.ZipFile? = null
    private var zipNames: List<String> = emptyList()
    private var opened by mutableStateOf(false)
    private var failed by mutableStateOf(false)
    private var count by mutableIntStateOf(0)
    private var needPassword by mutableStateOf(false)
    private var currentPage = 0
    private var prefTick by mutableIntStateOf(0)
    private var keyTurn: ((Boolean) -> Boolean)? = null

    private val cache = object : LruCache<Int, Bitmap>(((Runtime.getRuntime().maxMemory() / 6).coerceAtMost(256L * 1024 * 1024)).toInt()) {
        override fun sizeOf(key: Int, value: Bitmap): Int = value.byteCount
    }
    private val thumbs = LruCache<Int, Bitmap>(80)

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        parseIntent()
        super.onCreate(savedInstanceState)
        MainUtil.applyViewerOrientation(this)
        openBook()
    }

    override fun onResume() {
        super.onResume()
        MainUtil.applyWindowBrightness(window, ImageSettings.brightness.value, ImageSettings.userBrightness.value)
        prefTick++
    }

    override fun onPause() {
        super.onPause()
        savePosition()
    }

    override fun finish() {
        if (type in 1..3 && bookIndex >= 0) setResult(RESULT_OK, Intent().putExtra("EXTRA_INDEX", bookIndex))
        super.finish()
    }

    override fun onDestroy() {
        try { compress?.release() } catch (_: Throwable) {}
        compress = null
        try { zip?.close() } catch (_: Throwable) {}
        zip = null
        cache.evictAll()
        thumbs.evictAll()
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (ImageSettings.volumeTurn.value && (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            if (keyTurn?.invoke(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) == true) return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (ImageSettings.volumeTurn.value && (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP)) return true
        return super.onKeyUp(keyCode, event)
    }

    // ---- book ----

    private fun parseIntent() {
        val data = intent.data
        if (data != null) {
            type = if (pdfEntry) 2 else 3
            path = data.toString()
            bookIndex = 0
            (if (pdfEntry) ListTaskPdf(applicationContext, null) else ListTaskCmp(applicationContext, null)).load(false, false, true)
            return
        }
        type = intent.getIntExtra("EXTRA_TYPE", 0)
        val index = intent.getIntExtra("EXTRA_INDEX", -1)
        val resume = intent.getBooleanExtra("EXTRA_BOOK", false) || ListSettings.albumContinue.value
        val context = applicationContext
        when (type) {
            1, 2, 3 -> {
                path = intent.getStringExtra("EXTRA_PATH")
                bookIndex = index
                if (path.isNullOrEmpty()) {
                    val item = when (type) {
                        1 -> DataAlbum.instance(context).itemAt(index)
                        2 -> DataPdf.instance(context).itemAt(index)
                        else -> DataCmp.instance(context).itemAt(index)
                    }
                    if (item != null) {
                        path = item.path
                        title = item.name
                        if (resume) startPage = item.index
                    }
                }
            }
            12 -> {
                title = intent.getStringExtra("EXTRA_NAME")
                referer = intent.getStringExtra("EXTRA_REFERER")
                loadType = intent.getIntExtra("EXTRA_LOAD_TYPE", 0)
                startPage = index.coerceAtLeast(0)
            }
        }
    }

    private fun openBook() {
        if (type != 1 && type != 2 && type != 3 && type != 12) {
            MainUtil.showToast(this, R.string.invalid_path)
            failed = true
            return
        }
        val context = applicationContext
        Thread {
            var book: Compress? = null
            try {
                book = Compress.create(context, type, path, referer)
                if (book.javaClass == Compress::class.java && (type == 1 || type == 3) && CompressUtilZip.canOpen(context, path)) {
                    // plain file / content addresses (from a file manager) are not document addresses; read them as ZIP anyway
                    book = if (type == 1) CompressUtilAlbum(context, path, null) else CompressUtilZip(context, path, null)
                }
                book.loadType = loadType
                book.charsetName = if (book.archiveType() == 3) MainConst.defaultCharset else "UTF-8"
                book.load()
            } catch (_: Throwable) {
            }
            val noPages = book == null || max(book.entryCount(), book.pathNames?.size ?: 0) == 0
            if (noPages && (type == 1 || type == 3) && !path.isNullOrEmpty()) openWithPlatformZip(context)
            runOnUiThread { bookOpened(book) }
        }.start()
    }

    /** Reads the picture entries with the platform ZIP support when the bundled ZIP library cannot open the file. */
    private fun openWithPlatformZip(context: Context) {
        try {
            val local = CompressUtil.realPath(context, path) ?: return
            val file = java.util.zip.ZipFile(File(local))
            val names = file.entries().asSequence()
                .filter { !it.isDirectory && !it.name.contains("__MACOSX") && imageName(it.name) }
                .map { it.name }.toList()
                .sortedWith { a, b -> naturalCompare(a, b) }
            if (names.isEmpty()) { file.close(); return }
            zip = file
            zipNames = names
        } catch (_: Throwable) {
        }
    }

    private fun imageName(name: String): Boolean {
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in setOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "avif", "heic", "heif")
    }

    private fun naturalCompare(first: String, second: String): Int {
        var index = 0
        var secondPos = 0
        while (index < first.length && secondPos < second.length) {
            if (first[index].isDigit() && second[secondPos].isDigit()) {
                var x = index; while (x < first.length && first[x].isDigit()) x++
                var y = secondPos; while (y < second.length && second[y].isDigit()) y++
                val n1 = first.substring(index, x).trimStart('0')
                val n2 = second.substring(secondPos, y).trimStart('0')
                if (n1.length != n2.length) return n1.length - n2.length
                val c = n1.compareTo(n2)
                if (c != 0) return c
                index = x; secondPos = y
            } else {
                val c = first[index].lowercaseChar().compareTo(second[secondPos].lowercaseChar())
                if (c != 0) return c
                index++; secondPos++
            }
        }
        return (first.length - index) - (second.length - secondPos)
    }

    private fun zipBytes(index: Int): ByteArray? {
        val file = zip ?: return null
        val name = zipNames.getOrNull(index) ?: return null
        return try { file.getInputStream(file.getEntry(name)).use { it.readBytes() } } catch (_: Throwable) { null }
    }

    private fun bookOpened(book: Compress?) {
        compress = book
        val total = if (zip != null) zipNames.size else if (book == null) 0 else max(book.entryCount(), book.pathNames?.size ?: 0)
        count = total
        if (title.isNullOrEmpty()) title = path?.let { android.net.Uri.decode(it).substringAfterLast('/') }
        if (total == 0) {
            if (book != null && (type == 1 || type == 3) && CompressUtilZip2.isValid(book.realPath())) needPassword = true else failed = true
        } else {
            opened = true
            startPage = startPage.coerceIn(0, total - 1)
            currentPage = startPage
        }
    }

    private fun tryPassword(password: String) {
        val book = compress ?: return
        needPassword = false
        val context = applicationContext
        Thread {
            try {
                book.password = password
                book.load()
            } catch (_: Throwable) {
            }
            runOnUiThread { bookOpened(book).also { if (count == 0) { failed = true } } }
        }.start()
    }

    private fun savePosition() {
        val book = compress ?: return
        val file = path
        if (!opened || type !in 1..3 || file.isNullOrEmpty() || bookIndex < 0) return
        val page = currentPage
        val name = title
        val total = count
        val context = applicationContext
        Thread {
            try {
                var thumb: Bitmap? = null
                try {
                    (if (zip != null) null else book.bitmapAt(page))?.let { source ->
                        val shortSide = max(1, minOf(source.width, source.height) / MainApp.dp48)
                        thumb = MainUtil.scaleBitmap(source.width / shortSide, source.height / shortSide, source) ?: source
                    }
                } catch (_: Throwable) {
                }
                when (type) {
                    1 -> { val item = DbBookAlbum.addItem(context, file, name, total, page, 0, thumb); DataBookAlbum.instance(context).putItem(item) }
                    2 -> { val item = DbBookPdf.addItem(context, file, name, total, page, 0, thumb); DataBookPdf.instance(context).putItem(item) }
                    else -> { val item = DbBookCmp.addItem(context, file, name, total, page, 0, thumb); DataBookCmp.instance(context).putItem(item) }
                }
            } catch (_: Throwable) {
            }
        }.start()
    }

    // ---- pages ----

    private fun decodePage(bytes: ByteArray): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().also { it.inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
            val limit = 2 * max(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
            var sample = 1
            while (bounds.outWidth / sample > limit || bounds.outHeight / sample > limit) sample *= 2
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().also { it.inSampleSize = sample })
        } catch (_: Throwable) {
            null
        }
    }

    private fun pageName(index: Int): String? = try { compress?.nameAt(index) } catch (_: Throwable) { null }

    private suspend fun page(index: Int): Bitmap? {
        cache.get(index)?.let { if (!it.isRecycled) return it }
        if (zip != null) {
            val decoded = withContext(Dispatchers.IO) { zipBytes(index)?.let { decodePage(it) } }
            if (decoded != null) cache.put(index, decoded)
            return decoded
        }
        val book = compress ?: return null
        val result = withContext(Dispatchers.IO) {
            try {
                val name = book.nameAt(index)
                if (type == 12) {
                    ImageSource.load(applicationContext, name, null, referer)
                } else {
                    val bytes = book.streamFor(name)?.use { it.readBytes() }
                    if (bytes == null || bytes.isEmpty()) null else decodePage(bytes)
                }
            } catch (_: Throwable) {
                null
            }
        }
        if (result != null) cache.put(index, result)
        return result
    }

    private suspend fun thumb(index: Int): Bitmap? {
        thumbs.get(index)?.let { return it }
        if (zip != null) {
            val small = withContext(Dispatchers.IO) {
                zipBytes(index)?.let { bytes ->
                    val bounds = BitmapFactory.Options().also { it.inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                    var sample = 1
                    while (bounds.outWidth / sample > 256 || bounds.outHeight / sample > 256) sample *= 2
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().also { it.inSampleSize = sample })
                }
            }
            if (small != null) thumbs.put(index, small)
            return small
        }
        val book = compress ?: return null
        val result = withContext(Dispatchers.IO) { try { book.bitmapAt(index) } catch (_: Throwable) { null } }
        if (result != null) thumbs.put(index, result)
        return result
    }

    private fun sharePage(index: Int) {
        val book = compress ?: return
        val name = (if (zip != null) zipNames.getOrNull(index) else pageName(index)) ?: return
        if (type == 12) { MainUtil.shareText(this, name, null); return }
        val context = applicationContext
        Thread {
            var shared: String? = null
            try {
                val bytes = if (zip != null) zipBytes(index) else book.streamFor(name)?.use { it.readBytes() }
                if (bytes != null) {
                    val ext = MainUtil.dataUriExt(name).takeIf { it.length in 2..4 } ?: (if (type == 2) "png" else "jpg")
                    val file = File(context.cacheDir, "page_${System.currentTimeMillis()}.$ext")
                    file.writeBytes(bytes)
                    shared = file.path
                }
            } catch (_: Throwable) {
            }
            runOnUiThread { if (shared == null || !MainUtil.shareFile(4, this, shared, null, "image/*")) MainUtil.showToast(this, R.string.image_fail) }
        }.start()
    }

    // ---- screen ----

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        var bars by remember { mutableStateOf(true) }
        var menu by remember { mutableStateOf(false) }
        var brightnessSheet by remember { mutableStateOf(false) }
        val rtlLayout = LocalLayoutDirection.current == LayoutDirection.Rtl
        val background = Color(ImageSettings.backColor.value)

        Surface(Modifier.fillMaxSize(), color = background) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val landscape = maxWidth > maxHeight
                val mode = remember(prefTick, landscape) { if (landscape) ImageSettings.viewLand.value else ImageSettings.viewPort.value }
                val reverse = remember(prefTick) { ImageSettings.reverse.value != rtlLayout }
                val tapLeft = remember(prefTick, landscape) { if (landscape) ImageSettings.landAreaLeft.value else ImageSettings.portAreaLeft.value }
                val tapRight = remember(prefTick, landscape) { if (landscape) ImageSettings.landAreaRight.value else ImageSettings.portAreaRight.value }
                val density = androidx.compose.ui.platform.LocalDensity.current

                when {
                    opened && count > 0 -> {
                        val pager = rememberPagerState(initialPage = startPage.coerceIn(0, count - 1)) { count }
                        val list = rememberLazyListState(initialFirstVisibleItemIndex = startPage.coerceIn(0, count - 1))
                        val vertical = mode == 3
                        val current = if (vertical) list.firstVisibleItemIndex else pager.currentPage
                        currentPage = current

                        fun goTo(index: Int) {
                            val target = index.coerceIn(0, count - 1)
                            scope.launch { if (vertical) list.animateScrollToItem(target) else pager.animateScrollToPage(target) }
                        }
                        keyTurn = { forward ->
                            val delta = if (forward) 1 else -1
                            if (vertical) goTo(list.firstVisibleItemIndex + delta) else goTo(pager.currentPage + delta)
                            true
                        }

                        if (vertical) {
                            LazyColumn(state = list, modifier = Modifier.fillMaxSize().clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) { bars = !bars }) {
                                itemsIndexed(List(count) { it }) { _, index ->
                                    val state by pageState(index)
                                    val loaded = state.bitmap
                                    if (loaded != null) {
                                        Image(loaded.asImageBitmap(), null, contentScale = ContentScale.FillWidth, modifier = Modifier.fillMaxWidth())
                                    } else {
                                        Box(Modifier.fillMaxWidth().aspectRatio(0.7f), contentAlignment = Alignment.Center) { PageFailedOrLoading(state.done) }
                                    }
                                }
                            }
                        } else {
                            HorizontalPager(state = pager, reverseLayout = reverse, beyondViewportPageCount = 1, modifier = Modifier.fillMaxSize()) { index ->
                                val state by pageState(index)
                                ZoomPage(
                                    bitmap = state.bitmap,
                                    done = state.done,
                                    onTap = { x, width ->
                                        val leftZone = tapLeft.toFloat()
                                        val rightZone = width - tapRight.toFloat()
                                        val leftAction = ImageSettings.tapLeft.value == 0
                                        val rightAction = ImageSettings.tapRight.value == 0
                                        // the left area goes back unless the direction is reversed
                                        when {
                                            x < leftZone && leftAction -> goTo(pager.currentPage + if (reverse) 1 else -1)
                                            x > rightZone && rightAction -> goTo(pager.currentPage + if (reverse) -1 else 1)
                                            else -> bars = !bars
                                        }
                                    },
                                )
                            }
                        }

                        LaunchedEffect(Unit) {
                            snapshotFlow { current }.collect { c -> for (index in intArrayOf(c + 1, c - 1)) if (index in 0 until count) page(index) }
                        }

                        Overlay(bars, current, count, ::goTo, { sharePage(current) }, onMenu = { menu = true }, menuOpen = menu,
                            onMenuDismiss = { menu = false }, onBrightness = { brightnessSheet = true })
                    }
                    needPassword -> PasswordDialog(onOk = { tryPassword(it) }, onCancel = { finish() })
                    failed -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(stringResource(R.string.image_fail), color = Color.White)
                        TextButton(onClick = { finish() }) { Text(stringResource(R.string.cancel), color = Color.White) }
                    }
                    else -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            }
        }

        if (brightnessSheet) {
            ModalBottomSheet(onDismissRequest = { brightnessSheet = false }) {
                Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) { BrightnessControls() }
            }
        }
    }

    @Composable
    private fun androidx.compose.foundation.layout.BoxScope.Overlay(
        visible: Boolean, current: Int, total: Int, goTo: (Int) -> Unit, onShare: () -> Unit,
        onMenu: () -> Unit, menuOpen: Boolean, onMenuDismiss: () -> Unit, onBrightness: () -> Unit,
    ) {
        androidx.compose.animation.AnimatedVisibility(visible, Modifier.align(Alignment.TopCenter)) {
            Row(Modifier.fillMaxWidth().background(Color(0xCC000000)).statusBarsPadding().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { finish() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) }
                Column(Modifier.weight(1f)) {
                    Text(title ?: "", color = Color.White, maxLines = 1, style = MaterialTheme.typography.titleMedium)
                    Text("${current + 1} / $total", color = Color.LightGray, style = MaterialTheme.typography.bodySmall)
                }
                Box {
                    IconButton(onClick = onMenu) { Icon(Icons.Filled.MoreVert, null, tint = Color.White) }
                    com.mycompany.app.ui.components.BubbleMenu(expanded = menuOpen, onDismiss = onMenuDismiss) {
                        DropdownMenuItem(text = { Text(stringResource(R.string.brightness)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_brightness_6_black_24) }, onClick = { onMenuDismiss(); onBrightness() })
                        DropdownMenuItem(text = { Text(stringResource(R.string.share)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_share_black_24) }, onClick = { onMenuDismiss(); onShare() })
                        DropdownMenuItem(text = { Text(stringResource(R.string.view_port) + " / " + stringResource(R.string.view_land)) }, onClick = {
                            onMenuDismiss()
                            com.mycompany.app.ui.prefs.DialogShow.direct(this@ImageViewerActivity as android.app.Activity, onDismiss = { prefTick++ }) { host ->
                                DialogSetImage(host, object : ChangedListener { override fun onChanged() { prefTick++ } })
                            }
                        })
                        DropdownMenuItem(text = { Text(stringResource(R.string.setting)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(R.drawable.outline_settings_black_20) }, onClick = {
                            onMenuDismiss()
                            startActivity(Intent(this@ImageViewerActivity, ImageSettingsActivity::class.java))
                        })
                    }
                }
            }
        }
        androidx.compose.animation.AnimatedVisibility(visible, Modifier.align(Alignment.BottomCenter)) {
            Column(Modifier.fillMaxWidth().background(Color(0xCC000000)).navigationBarsPadding()) {
                // follows the setting live (the settings page is opened on top of the viewer)
                val showThumb by ImageSettings.showThumb.flow.collectAsState()
                if (showThumb) {
                    val row = rememberLazyListState(initialFirstVisibleItemIndex = (current - 2).coerceAtLeast(0))
                    LaunchedEffect(current) { row.animateScrollToItem((current - 2).coerceAtLeast(0)) }
                    LazyRow(state = row, modifier = Modifier.fillMaxWidth().height(88.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        itemsIndexed(List(total) { it }) { _, index ->
                            val bmp by produceState<Bitmap?>(thumbs.get(index), index) { if (value == null) value = thumb(index) }
                            Box(
                                Modifier.width(64.dp).height(88.dp).padding(2.dp)
                                    .then(if (index == current) Modifier.border(2.dp, Color.White) else Modifier)
                                    .clickable { goTo(index) },
                                contentAlignment = Alignment.Center,
                            ) {
                                val bmp2 = bmp
                                if (bmp2 != null) Image(bmp2.asImageBitmap(), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                else Text("${index + 1}", color = Color.White, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                if (total > 1) {
                    var value by remember(current) { mutableFloatStateOf(current.toFloat()) }
                    AppSlider(
                        value = value, onValueChange = { value = it }, onValueChangeFinished = { goTo(value.roundToInt()) },
                        valueRange = 0f..(total - 1).toFloat(), modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }

    @Composable
    private fun ZoomPage(bitmap: Bitmap?, done: Boolean, onTap: (Float, Int) -> Unit) {
        var zoom by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }
        var size by remember { mutableStateOf(IntSize.Zero) }
        Box(
            Modifier.fillMaxSize().onSizeChanged { size = it }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onTap(it.x, size.width) },
                        onDoubleTap = { if (zoom > 1.01f) { zoom = 1f; offset = Offset.Zero } else zoom = 2.5f },
                    )
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        do {
                            val event = awaitPointerEvent()
                            val pressed = event.changes.count { it.pressed }
                            if (pressed >= 2 || zoom > 1.01f) {
                                val zoomChange = event.calculateZoom()
                                val calculatePan = event.calculatePan()
                                if (zoomChange != 1f || calculatePan != Offset.Zero) {
                                    zoom = (zoom * zoomChange).coerceIn(1f, 6f)
                                    offset += calculatePan
                                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                                }
                            }
                        } while (event.changes.any { it.pressed })
                        if (zoom <= 1.01f) { zoom = 1f; offset = Offset.Zero }
                    }
                }
                .graphicsLayer {
                    val limitX = size.width * (zoom - 1f) / 2f
                    val limitY = size.height * (zoom - 1f) / 2f
                    scaleX = zoom; scaleY = zoom
                    translationX = offset.x.coerceIn(-limitX, limitX)
                    translationY = offset.y.coerceIn(-limitY, limitY)
                },
            contentAlignment = Alignment.Center,
        ) {
            if (bitmap != null) Image(bitmap.asImageBitmap(), null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize())
            else PageFailedOrLoading(done)
        }
    }

    /** One page's picture; [done] tells "could not be decoded" (e.g. an SVG) apart from "still loading". */
    private class PageState(val bitmap: Bitmap?, val done: Boolean)

    @Composable
    private fun pageState(index: Int) = produceState(cache.get(index).let { PageState(it, it != null) }, index) {
        if (value.bitmap == null) value = PageState(page(index), true)
    }

    @Composable
    private fun PageFailedOrLoading(done: Boolean) {
        if (done) Text(stringResource(R.string.image_fail), color = Color.White.copy(alpha = 0.7f))
        else CircularProgressIndicator()
    }

    @Composable
    private fun PasswordDialog(onOk: (String) -> Unit, onCancel: () -> Unit) {
        var password by remember { mutableStateOf("") }
        com.mycompany.app.ui.components.OpumSheetDialog(
            onDismissRequest = onCancel,
            title = { Text(stringResource(R.string.password)) },
            text = { com.mycompany.app.ui.components.AppPasswordField(value = password, onValueChange = { password = it }) },
            confirmButton = { TextButton(onClick = { onOk(password) }) { Text(stringResource(R.string.ok)) } },
            dismissButton = { TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) } },
        )
    }

    @Composable
    private fun BrightnessControls() {
        var user by remember { mutableStateOf(ImageSettings.userBrightness.value) }
        var value by remember { mutableFloatStateOf(ImageSettings.brightness.value.toFloat()) }
        fun store() {
            ImageSettings.edit(applicationContext) {
                set(ImageSettings.userBrightness, user)
                set(ImageSettings.brightness, value.roundToInt())
            }
            MainUtil.applyWindowBrightness(window, ImageSettings.brightness.value, ImageSettings.userBrightness.value)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.user_defined), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Switch(checked = user, onCheckedChange = { user = it; store() })
        }
        Text(stringResource(R.string.brightness) + ": ${value.roundToInt()}%", Modifier.padding(top = 8.dp))
        AppSlider(value = value, onValueChange = { value = it; if (user) store() }, valueRange = 0f..100f, enabled = user, modifier = Modifier.alpha(if (user) 1f else 0.5f))
        Text(stringResource(R.string.bright_info), style = MaterialTheme.typography.bodySmall)
    }
}
