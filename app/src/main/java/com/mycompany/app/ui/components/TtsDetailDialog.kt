package com.mycompany.app.ui.components

import android.content.Context
import com.mycompany.app.pref.store.TtsSettings
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefTts
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Material 3 replacement of the old `DialogSetTts`: the voice language (automatic or one of the engine's languages),
 * speed (0.5x..3.0x) and pitch (0.5x..2.0x), a sample button that reads the explanation aloud, Reset and Apply. Values
 * are stored in `PrefTts`; Reset goes back to automatic / 1.0 / 1.0 and forgets the stored values.
 */
@Composable
fun TtsDetailDialog(context: Context, onDismiss: () -> Unit) {
    var lang by remember { mutableStateOf(TtsSettings.ttsLang.value?.takeIf { it.isNotEmpty() }) }
    var rate by remember { mutableFloatStateOf(TtsSettings.ttsRate.value.coerceIn(0.5f, 3.0f)) }
    var pitch by remember { mutableFloatStateOf(TtsSettings.ttsPitch.value.coerceIn(0.5f, 2.0f)) }
    var languages by remember { mutableStateOf<List<Locale>?>(null) }
    var pickLanguage by remember { mutableStateOf(false) }
    var resetAsk by remember { mutableStateOf(false) }
    val sample = stringResource(R.string.tts_sample_text)
    val waitRetry = stringResource(R.string.wait_retry)
    val auto = stringResource(R.string.auto_detect)
    val tts = remember { arrayOfNulls<TextToSpeech>(1) }

    fun apply() {
        val engine = tts[0] ?: return
        try {
            val locale = MainUtil.parseLocale(lang)
            if (locale != null && engine.isLanguageAvailable(locale) >= 0) engine.language = locale else MainApp.currentLocale().let { engine.language = it }
            engine.setSpeechRate(rate)
            engine.setPitch(pitch)
        } catch (_: Exception) {
        }
    }

    // the sample tells the speech helper when it started or ended, so a silent engine is noticed (2026-11-26)
    val listener = remember {
        object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = com.mycompany.app.main.SpeechGuard.started(utteranceId)
            override fun onDone(utteranceId: String?) = com.mycompany.app.main.SpeechGuard.ended(utteranceId)
            override fun onStop(utteranceId: String?, interrupted: Boolean) = com.mycompany.app.main.SpeechGuard.ended(utteranceId)
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                com.mycompany.app.main.SpeechGuard.cancelAll()
                com.mycompany.app.main.SpeechGuard.showDown(context as? android.app.Activity)
            }
            override fun onError(utteranceId: String?, errorCode: Int) { @Suppress("DEPRECATION") onError(utteranceId) }
        }
    }

    DisposableEffect(Unit) {
        try {
            tts[0] = TextToSpeech(context) { status ->
                val engine = tts[0]
                if (status != TextToSpeech.SUCCESS || engine == null) {
                    tts[0] = null
                } else {
                    try {
                        val preferred = MainApp.currentLocale().language
                        languages = engine.availableLanguages?.toList()?.sortedWith(
                            compareBy<Locale>({ preferred == null || it.language != preferred }, { it.displayName })
                        ) ?: emptyList()
                    } catch (_: Exception) {
                        languages = emptyList()
                    }
                    apply()
                }
            }
            tts[0]?.setOnUtteranceProgressListener(listener)
        } catch (_: Exception) {
        }
        onDispose {
            com.mycompany.app.main.SpeechGuard.cancelAll()
            try { tts[0]?.stop(); tts[0]?.shutdown() } catch (_: Exception) {}
            tts[0] = null
        }
    }

    fun save(forget: Boolean) {
        if (!MainUtil.sameObject(TtsSettings.ttsLang.value, lang) || TtsSettings.ttsRate.value != rate || TtsSettings.ttsPitch.value != pitch) {
            TtsSettings.editLocal { set(TtsSettings.ttsLang, lang) }; TtsSettings.editLocal { set(TtsSettings.ttsRate, rate) }; TtsSettings.editLocal { set(TtsSettings.ttsPitch, pitch) }
            val prefs = PrefTts.get(context, false) ?: return
            if (forget) { prefs.removeKey("mTtsLang"); prefs.removeKey("mTtsRate"); prefs.removeKey("mTtsPitch") }
            else { prefs.putString("mTtsLang", lang); prefs.putFloat("mTtsRate", rate); prefs.putFloat("mTtsPitch", pitch) }
            prefs.saveAsync()
        }
    }

    fun stopSpeaking() { try { if (tts[0]?.isSpeaking == true) tts[0]?.stop() } catch (_: Exception) {} }

    val languageName = MainUtil.parseLocale(lang)?.displayName ?: auto

    com.mycompany.app.ui.components.OpumSheetDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.detail_setting)) },
        text = {
            Column {
                Text(stringResource(R.string.locale), style = MaterialTheme.typography.labelLarge)
                Text(
                    languageName, style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth().clickable {
                        if (languages == null) com.mycompany.app.main.MainUtil.showToastText(context, waitRetry) else pickLanguage = true
                    }.padding(vertical = 8.dp),
                )
                Text(stringResource(R.string.voice_speed) + ": " + "%.1fx".format(rate), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                AppSlider(
                    value = ((rate - 0.5f) * 10f), valueRange = 0f..25f, steps = 24,
                    onValueChange = { stopSpeaking(); rate = (it.roundToInt() / 10f + 0.5f); try { tts[0]?.setSpeechRate(rate) } catch (_: Exception) {} },
                )
                Text(stringResource(R.string.voice_tone) + ": " + "%.1fx".format(pitch), style = MaterialTheme.typography.labelLarge)
                AppSlider(
                    value = ((pitch - 0.5f) * 10f), valueRange = 0f..15f, steps = 14,
                    onValueChange = { stopSpeaking(); pitch = (it.roundToInt() / 10f + 0.5f); try { tts[0]?.setPitch(pitch) } catch (_: Exception) {} },
                )
                Text(sample, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                TextButton(onClick = {
                    stopSpeaking()
                    com.mycompany.app.main.SpeechGuard.speak(
                        tts[0], sample.replace('\n', ' '), "sample",
                        prepare = { apply() },
                        recreate = { done ->
                            com.mycompany.app.main.SpeechGuard.release(tts[0])
                            tts[0] = com.mycompany.app.main.SpeechGuard.create(context, listener, done)
                        },
                        onFailed = { com.mycompany.app.main.SpeechGuard.showDown(context as? android.app.Activity) },
                    )
                }) { Text(stringResource(R.string.play)) }
            }
        },
        confirmButton = { TextButton(onClick = { save(false); onDismiss() }) { Text(stringResource(R.string.apply)) } },
        dismissButton = { TextButton(onClick = { resetAsk = true }) { Text(stringResource(R.string.reset)) } },
    )

    if (pickLanguage) {
        com.mycompany.app.ui.components.OpumSheetDialog(
            onDismissRequest = { pickLanguage = false },
            title = { Text(stringResource(R.string.locale)) },
            text = {
                LazyColumn(Modifier.heightIn(max = 420.dp)) {
                    item {
                        LanguageRow(auto, lang == null) { lang = null; pickLanguage = false; apply() }
                        HorizontalDivider()
                    }
                    items(languages ?: emptyList(), key = { it.toString() }) { locale ->
                        LanguageRow(locale.displayName, lang == locale.toString()) { lang = locale.toString(); pickLanguage = false; apply() }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { pickLanguage = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }

    if (resetAsk) {
        AlertDialog(
            onDismissRequest = { resetAsk = false },
            text = { Text(stringResource(R.string.reset_setting)) },
            confirmButton = {
                TextButton(onClick = {
                    resetAsk = false
                    stopSpeaking()
                    lang = null; rate = 1f; pitch = 1f
                    apply()
                    save(true)
                }) { Text(stringResource(R.string.reset)) }
            },
            dismissButton = { TextButton(onClick = { resetAsk = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun LanguageRow(name: String, selected: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onClick)
        Text(name, style = MaterialTheme.typography.bodyLarge)
    }
}
