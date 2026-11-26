package com.mycompany.app.ui.viewer

import com.mycompany.app.ui.components.AppSlider
import android.content.Context
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.PdfSettings
import android.content.Intent
import android.graphics.Typeface
import android.media.AudioManager
import android.net.Uri
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.text.TextUtils
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSaveSource
import com.mycompany.app.dialog.DialogDownPage.DownPageListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.TtsDetailDialog
import com.mycompany.app.ui.settings.FontSettingsActivity
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.zip.GZIPInputStream
import kotlin.math.roundToInt
import com.mycompany.app.ui.components.Sym

/**
 * Plain-text viewer (`.txt`, `.xml`, source files, or a page address): opens `EXTRA_PATH` (a file path, content URI or
 * web address, with `EXTRA_REFERER` / `EXTRA_NAME`) or the intent's data. Tap shows or hides the bars; pinch zooms the
 * text (the stored text size percent); long-press a line to have it read aloud from there. Replaces `MainTxtView`.
 */
class TextViewerActivity : OpumComposeActivity() {

    companion object {
        const val EXTRA_PATH = "EXTRA_PATH"
        const val EXTRA_REFERER = "EXTRA_REFERER"
        const val EXTRA_NAME = "EXTRA_NAME"
        private const val TEXT_MIN = 50
        private const val TEXT_MAX = 500
        private const val BASE_SP = 18f

        /** Splits a long line at a sentence or word end after about 80 characters, as the old viewer did. */
        private fun breakAt(length: Int, start: Int, line: String): Int {
            if (length - start > 128) {
                val from = start + 80
                if (from < length) {
                    val sentence = line.indexOf(". ", from)
                    val sentenceEnd = sentence + 2
                    if (sentence <= from || sentenceEnd > length || sentenceEnd - start > 128) {
                        val dot = line.indexOf('.', from)
                        val dotEnd = dot + 1
                        if (dot <= from || dotEnd > length || dotEnd - start > 128) {
                            val space = line.indexOf(' ', from)
                            val spaceEnd = space + 1
                            if (space > from && spaceEnd <= length && spaceEnd - start <= 128 && length - spaceEnd >= 10) return spaceEnd
                        } else if (length - dotEnd >= 10) {
                            return dotEnd
                        }
                    } else if (length - sentenceEnd >= 10) {
                        return sentenceEnd
                    }
                }
                return from
            }
            return length
        }
    }

    private var source: String? = null
    private var referer: String? = null
    private var name: String? = null
    private var lines by mutableStateOf<List<String>?>(null)
    private var loading by mutableStateOf(true)
    private var memoryFailed by mutableStateOf(false)
    private var loadToken = 0
    private var textPercent by mutableIntStateOf(ReadSettings.textSize.value)
    private var typefaceTick by mutableIntStateOf(0)

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pendingStart = -1
    private var speakState by mutableIntStateOf(0) // 0 stopped, 1 playing, 2 paused
    private var speakLine by mutableIntStateOf(-1)

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        readIntent(intent)
        textPercent = ReadSettings.textSize.value.coerceIn(TEXT_MIN, TEXT_MAX)
        super.onCreate(savedInstanceState)
        MainUtil.applyScreenOrientation(this)
        load()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        stopSpeaking()
        readIntent(intent)
        load()
    }

    private fun readIntent(intent: Intent) {
        val data = intent.data
        if (data != null) {
            source = data.toString()
            referer = null
            name = null
        } else {
            source = intent.getStringExtra(EXTRA_PATH)
            referer = intent.getStringExtra(EXTRA_REFERER)
            name = intent.getStringExtra(EXTRA_NAME)
        }
    }

    override fun onResume() {
        super.onResume()
        MainUtil.applyWindowBrightness(window, PdfSettings.brightness.value, PdfSettings.userBrightness.value)
        typefaceTick++
        if (speakState != 0) applyTtsPrefs()
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) saveTextSize()
    }

    override fun onDestroy() {
        loadToken++
        stopSpeaking()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }

    private fun saveTextSize() {
        ReadSettings.edit(applicationContext) { set(ReadSettings.textSize, textPercent) }
    }

    private fun load() {
        val path = source
        val ref = referer
        val token = ++loadToken
        lines = null
        memoryFailed = false
        loading = true
        if (TextUtils.isEmpty(path)) {
            loading = false
            return
        }
        val context = applicationContext
        Thread {
            val result = ArrayList<String>()
            var failedMemory = false
            try {
                var input: java.io.InputStream?
                var charset: Charset? = null
                var gzip = false
                if (URLUtil.isNetworkUrl(path)) {
                    val first = MainUtil.openHttpConnection(0, 0, context, path, ref, true)
                    if (first == null || first.responseCode != 200) {
                        input = null
                    } else {
                        input = first.inputStream
                        gzip = "gzip" == first.contentEncoding
                        charset = try {
                            Charset.forName(MainUtil.detectCharset(if (gzip) GZIPInputStream(input) else input))
                        } catch (_: Exception) {
                            StandardCharsets.UTF_8
                        }
                        first.disconnect()
                        val again = MainUtil.openHttpConnection(0, 0, context, path, ref, true)
                        if (again != null && again.responseCode == 200) input = again.inputStream
                    }
                } else {
                    charset = try {
                        Charset.forName(MainUtil.detectCharset(MainUtil.openUriInput(context, path)))
                    } catch (_: Exception) {
                        StandardCharsets.UTF_8
                    }
                    input = MainUtil.openUriInput(context, path)
                }
                if (input != null) {
                    val reader = BufferedReader(InputStreamReader(if (gzip) GZIPInputStream(input) else input, charset ?: StandardCharsets.UTF_8))
                    reader.use {
                        while (token == loadToken) {
                            val line = try { it.readLine() } catch (_: OutOfMemoryError) { failedMemory = true; null } catch (_: Exception) { continue } ?: break
                            val length = line.length
                            if (length <= 128) {
                                result.add(line)
                                continue
                            }
                            var start = 0
                            var end = breakAt(length, 0, line)
                            while (end < length) {
                                result.add(line.substring(start, end))
                                val next = breakAt(length, end, line)
                                start = end
                                end = next
                            }
                            if (start < length) result.add(line.substring(start))
                        }
                    }
                }
            } catch (_: Exception) {
            }
            runOnUiThread {
                if (token != loadToken) return@runOnUiThread
                lines = if (result.isEmpty()) null else result
                loading = false
                memoryFailed = failedMemory
                if (failedMemory) MainUtil.showToast(this, R.string.out_of_memory)
            }
        }.start()
    }

    // ---- speech ----

    private fun applyTtsPrefs() {
        val engine = tts ?: return
        if (!ttsReady) return
        try {
            val tag = TtsSettings.ttsLang.value
            val locale = if (!tag.isNullOrEmpty()) Locale.forLanguageTag(tag) else Locale.getDefault()
            engine.language = locale
            val rate = TtsSettings.ttsRate.value.coerceIn(0.5f, 3.0f)
            val pitch = TtsSettings.ttsPitch.value.coerceIn(0.5f, 2.0f)
            engine.setSpeechRate(rate)
            engine.setPitch(pitch)
        } catch (_: Exception) {
        }
    }

    private fun startSpeaking(index: Int) {
        val list = lines ?: return
        if (index !in list.indices) return
        speakLine = index
        speakState = 1
        if (tts == null) {
            pendingStart = index
            tts = com.mycompany.app.main.SpeechGuard.create(applicationContext, speechListener) { engine ->
                runOnUiThread {
                    if (engine == null) {
                        speechFailed()
                    } else {
                        ttsReady = true
                        applyTtsPrefs()
                        if (pendingStart >= 0 && speakState == 1) speakCurrent()
                        pendingStart = -1
                    }
                }
            }
        } else if (ttsReady) {
            speakCurrent()
        }
    }

    private fun speakCurrent() {
        val list = lines ?: return
        var speakLineNow = speakLine
        while (speakLineNow in list.indices && list[speakLineNow].isBlank()) speakLineNow++
        if (speakLineNow !in list.indices) { stopSpeaking(); return }
        speakLine = speakLineNow
        val max = minOf(TextToSpeech.getMaxSpeechInputLength(), 160)
        val chunk = list[speakLineNow].take(max)
        // a dead engine is made again once; when it still does not speak, the viewer says so (2026-11-26)
        com.mycompany.app.main.SpeechGuard.speak(
            tts, chunk, "line$speakLineNow",
            recreate = { done ->
                com.mycompany.app.main.SpeechGuard.release(tts)
                ttsReady = false
                tts = com.mycompany.app.main.SpeechGuard.create(applicationContext, speechListener) { engine ->
                    runOnUiThread { if (engine != null) { ttsReady = true; applyTtsPrefs() }; done(engine) }
                }
            },
            onFailed = { speechFailed() },
        )
    }

    /** What the engine reports while speaking: the next line when one is done; a failure stops and says so. */
    private val speechListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) { com.mycompany.app.main.SpeechGuard.started(utteranceId) }
        override fun onStop(utteranceId: String?, interrupted: Boolean) { com.mycompany.app.main.SpeechGuard.ended(utteranceId) }
        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
            com.mycompany.app.main.SpeechGuard.ended(utteranceId)
            runOnUiThread { speechFailed() }
        }
        override fun onError(utteranceId: String?, errorCode: Int) { @Suppress("DEPRECATION") onError(utteranceId) }
        override fun onDone(utteranceId: String?) {
            com.mycompany.app.main.SpeechGuard.ended(utteranceId)
            runOnUiThread {
                if (speakState != 1 || utteranceId != "line$speakLine") return@runOnUiThread
                val size = lines?.size ?: 0
                if (speakLine >= size - 1) stopSpeaking() else moveSpeaking(1)
            }
        }
    }

    /** Speaking did not work: it stops, and a message offers Android's speech settings. */
    private fun speechFailed() {
        if (speakState == 0) return
        stopSpeaking()
        com.mycompany.app.main.SpeechGuard.showDown(this)
    }

    private fun moveSpeaking(delta: Int) {
        val list = lines ?: return
        val next = (speakLine + delta).coerceIn(0, list.size - 1)
        speakLine = next
        if (speakState == 1) speakCurrent()
    }

    private fun pauseSpeaking() {
        if (speakState != 1) return
        speakState = 2
        tts?.stop()
    }

    private fun stopSpeaking() {
        com.mycompany.app.main.SpeechGuard.cancelAll()
        speakState = 0
        speakLine = -1
        pendingStart = -1
        try { tts?.stop() } catch (_: Exception) {}
    }

    // ---- screen ----

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    override fun Content() {
        val context: Context = this
        val state = rememberLazyListState()
        val snack = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        var bars by remember { mutableStateOf(true) }
        var menu by remember { mutableStateOf(false) }
        var ttsDialog by remember { mutableStateOf(false) }
        var sheet by remember { mutableIntStateOf(0) } // 1 brightness, 2 text size, 3 volume
        val list = lines
        val network = URLUtil.isNetworkUrl(source)
        val family = remember(typefaceTick) {
            var face: Typeface? = null
            if (ReadSettings.userFont.value && !TextUtils.isEmpty(ReadSettings.fontPath.value)) {
                face = MainUtil.loadTypeface(context, MainUtil.readerFontPath(context))
            } else if (ReadSettings.fontBold.value) {
                face = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            face?.let { FontFamily(it) } ?: FontFamily.Default
        }
        val saveLabel = stringResource(R.string.save_success)
        val openLabel = stringResource(R.string.open_with)
        val openDoc = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (result.resultCode == RESULT_OK && uri != null) {
                MainUtil.keepUriPermission(applicationContext, uri)
                source = uri.toString()
                referer = null
                name = null
                load()
            }
        }

        LaunchedEffect(speakLine) {
            val speakLineNow = speakLine
            if (speakLineNow >= 0 && state.layoutInfo.visibleItemsInfo.none { it.index == speakLineNow }) state.animateScrollToItem(speakLineNow)
        }

        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Box(Modifier.fillMaxSize()) {
                Box(
                    Modifier.fillMaxSize()
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                do {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                    val zoom = event.calculateZoom()
                                    if (zoom != 1f) {
                                        textPercent = (textPercent * zoom).roundToInt().coerceIn(TEXT_MIN, TEXT_MAX)
                                    }
                                } while (event.changes.any { it.pressed })
                            }
                        },
                ) {
                    when {
                        list != null -> LazyColumn(
                            state = state,
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 72.dp, bottom = 96.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            itemsIndexed(list) { index, line ->
                                Text(
                                    text = line.ifEmpty { " " },
                                    fontFamily = family,
                                    fontSize = (BASE_SP * textPercent / 100f).sp,
                                    // relative to the font size: the theme's fixed 24 sp made wrapped lines overlap
                                    // once the text was zoomed past it
                                    lineHeight = 1.4.em,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.fillMaxWidth()
                                        .background(if (index == speakLine && speakState != 0) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                        .combinedClickable(onClick = { bars = !bars }, onLongClick = { if (line.isNotBlank()) startSpeaking(index) }),
                                )
                            }
                        }
                        loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                        else -> Text(stringResource(R.string.empty), Modifier.align(Alignment.Center), style = MaterialTheme.typography.bodyLarge)
                    }
                }

                AnimatedVisibility(bars, Modifier.align(Alignment.TopCenter)) {
                    Surface(color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f)) {
                        Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { finish() }) { Icon(Sym.ArrowBack, contentDescription = null) }
                            Text(
                                name ?: source?.let { Uri.decode(it).substringAfterLast('/') } ?: stringResource(R.string.doc),
                                Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            if (network && list != null) {
                                IconButton(onClick = {
                                    var dialog: DialogSaveSource? = null
                                    com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity, requestCodes = intArrayOf(19), onResult = { c, r, intent -> dialog?.onPickResult(c, r, intent) }) { host ->
                                        DialogSaveSource(host, name, null, ArrayList<String?>(list), object : DownPageListener {
                                            override fun onSave(url: String?, path: String?, bitmap: android.graphics.Bitmap?) {
                                                dialog?.dismiss()
                                                runOnUiThread {
                                                    scope.launch {
                                                        if (path.isNullOrEmpty()) {
                                                            snack.showSnackbar(getString(R.string.save_fail))
                                                        } else if (snack.showSnackbar(saveLabel, openLabel) == SnackbarResult.ActionPerformed) {
                                                            MainUtil.openWithApp(this@TextViewerActivity, path, "text/plain", true, false)
                                                        }
                                                    }
                                                }
                                            }
                                        }).also { dialog = it }
                                    }
                                }) { Icon(painterResource(R.drawable.outline_download_black_24), contentDescription = stringResource(R.string.save), tint = MaterialTheme.colorScheme.onSurface) }
                                IconButton(onClick = { MainUtil.copyTextToast(context, R.string.copied_clipboard, "Copied URL", source) }) {
                                    Icon(painterResource(R.drawable.outline_link_black_24), contentDescription = stringResource(R.string.copy_url), tint = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                            IconButton(onClick = {
                                try {
                                    val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension("txt") ?: "text/*"
                                    val pick = Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(mime).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                                    openDoc.launch(pick)
                                } catch (_: Exception) {
                                }
                            }) { Icon(painterResource(R.drawable.outline_sticky_note_black_24), contentDescription = stringResource(R.string.open_with), tint = MaterialTheme.colorScheme.onSurface) }
                            Box {
                                IconButton(onClick = { menu = true }) { Icon(Sym.MoreVert, contentDescription = null) }
                                com.mycompany.app.ui.components.BubbleMenu(expanded = menu, onDismiss = { menu = false }) {
                                    DropdownMenuItem(text = { Text(stringResource(R.string.font)) }, onClick = {
                                        menu = false
                                        startActivity(Intent(context, FontSettingsActivity::class.java).putExtra(FontSettingsActivity.EXTRA_PAGE, true))
                                    })
                                    DropdownMenuItem(text = { Text("TTS") }, onClick = {
                                        menu = false
                                        stopSpeaking()
                                        ttsDialog = true
                                    })
                                }
                            }
                        }
                    }
                }

                AnimatedVisibility(bars && list != null, Modifier.align(Alignment.BottomCenter)) {
                    Surface(color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f)) {
                        Row(
                            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val tint = MaterialTheme.colorScheme.onSurface
                            IconButton(onClick = { sheet = 1 }) { Icon(painterResource(R.drawable.outline_brightness_6_black_24), stringResource(R.string.brightness), tint = tint) }
                            IconButton(onClick = { sheet = 2 }) { Icon(painterResource(R.drawable.outline_text_fields_black_24), stringResource(R.string.text_size), tint = tint) }
                            IconButton(onClick = { sheet = 3 }) { Icon(painterResource(R.drawable.outline_volume_up_black_24), stringResource(R.string.volume), tint = tint) }
                            Box(Modifier.weight(1f))
                            if (speakState != 0) {
                                IconButton(onClick = { moveSpeaking(-1) }) { Icon(painterResource(R.drawable.baseline_fast_rewind_black_24), null, tint = tint) }
                            }
                            if (speakState == 1) {
                                IconButton(onClick = { pauseSpeaking() }) { Icon(painterResource(R.drawable.baseline_pause_black_24), stringResource(R.string.pause), tint = tint) }
                            } else {
                                IconButton(onClick = {
                                    val l = lines ?: return@IconButton
                                    val first = if (speakLine in l.indices) speakLine else state.firstVisibleItemIndex
                                    startSpeaking(first)
                                }) { Icon(painterResource(R.drawable.baseline_play_arrow_black_24), stringResource(R.string.play), tint = tint) }
                            }
                            if (speakState != 0) {
                                IconButton(onClick = { stopSpeaking() }) { Icon(painterResource(R.drawable.baseline_stop_black_24), stringResource(R.string.stop), tint = tint) }
                                IconButton(onClick = { moveSpeaking(1) }) { Icon(painterResource(R.drawable.baseline_fast_forward_black_24), null, tint = tint) }
                            }
                        }
                    }
                }

                SnackbarHost(snack, Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 64.dp))

                if (sheet != 0) {
                    ModalBottomSheet(onDismissRequest = { if (sheet == 2) saveTextSize(); sheet = 0 }) {
                        Column(Modifier.padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                            when (sheet) {
                                1 -> BrightnessControls()
                                2 -> {
                                    Text(stringResource(R.string.text_size) + ": $textPercent%", style = MaterialTheme.typography.titleMedium)
                                    AppSlider(
                                        value = textPercent.toFloat(),
                                        onValueChange = { textPercent = it.roundToInt().coerceIn(TEXT_MIN, TEXT_MAX) },
                                        valueRange = TEXT_MIN.toFloat()..TEXT_MAX.toFloat(),
                                    )
                                    TextButton(onClick = { textPercent = 100 }) { Text(stringResource(R.string.reset)) }
                                }
                                3 -> VolumeControls()
                            }
                        }
                    }
                }
            }
        }
        if (ttsDialog) TtsDetailDialog(context) { ttsDialog = false; applyTtsPrefs() }
    }

    @Composable
    private fun BrightnessControls() {
        var user by remember { mutableStateOf(PdfSettings.userBrightness.value) }
        var value by remember { mutableFloatStateOf(PdfSettings.brightness.value.toFloat()) }
        fun store() {
            PdfSettings.editLocal { set(PdfSettings.userBrightness, user) }
            PdfSettings.editLocal { set(PdfSettings.brightness, value.roundToInt()) }
            MainUtil.applyWindowBrightness(window, PdfSettings.brightness.value, PdfSettings.userBrightness.value)
            val editor = PrefPdf.get(applicationContext, false)
            editor?.putBool("mUserBright", user)
            editor?.putInt(PdfSettings.brightness.value, "mBright")
            editor?.saveAsync()
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.user_defined), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            Switch(checked = user, onCheckedChange = { user = it; store() })
        }
        Text(stringResource(R.string.brightness) + ": ${value.roundToInt()}%", Modifier.padding(top = 8.dp))
        AppSlider(
            value = value,
            onValueChange = { value = it; if (user) store() },
            valueRange = 0f..100f,
            enabled = user,
            modifier = Modifier.alpha(if (user) 1f else 0.5f),
        )
        Text(stringResource(R.string.bright_info), style = MaterialTheme.typography.bodySmall)
    }

    @Composable
    private fun VolumeControls() {
        val audio = remember { getSystemService(Context.AUDIO_SERVICE) as AudioManager }
        val max = remember { audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1) }
        var value by remember { mutableFloatStateOf(audio.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()) }
        Text(stringResource(R.string.volume) + ": ${(value * 100 / max).roundToInt()}%", style = MaterialTheme.typography.titleMedium)
        AppSlider(
            value = value,
            onValueChange = { value = it; audio.setStreamVolume(AudioManager.STREAM_MUSIC, it.roundToInt(), 0) },
            valueRange = 0f..max.toFloat(),
            steps = (max - 1).coerceAtLeast(0),
        )
    }
}
