package com.mycompany.app.dialog

import android.content.DialogInterface
import com.mycompany.app.pref.store.TtsSettings
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.text.TextUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.parseLocale
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.ui.browser.ComposeBottomDialog
import java.util.Collections
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Read-aloud voice settings: a sample sentence (tap to hear it; hidden in landscape, [applyLandscape]), the language ("Auto detect"
 * or one of the engine's languages, M3 menu), voice speed (0.5..3.0) and tone (0.5..2.0) sliders that apply live to the
 * test voice, Reset (asks) and Apply (stores `TtsSettings.ttsLang.value/l/m`). Kept for the callers: [applyLandscape] (orientation) and [stopSpeaking] (stop
 * speaking). Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogSetTts(mainActivity: MainActivity?) : ComposeBottomDialog(mainActivity) {
    @JvmField
    var hostActivity: MainActivity? = mainActivity
    @JvmField
    var appContext: android.content.Context? = context
    @JvmField
    var sampleText: String? = null
    @JvmField
    var ttsLang: String? = TtsSettings.ttsLang.value
    @JvmField
    var ttsRate: Float = TtsSettings.ttsRate.value
    @JvmField
    var ttsPitch: Float = TtsSettings.ttsPitch.value
    @JvmField
    var speech: TextToSpeech? = null
    @JvmField
    var ttsLocale: Locale? = null
    @JvmField
    var langItems: ArrayList<Any?>? = null
    @JvmField
    var langMenuShown: Boolean = false
    @JvmField
    var msgDialog: DialogSetMsg? = null

    private val sample: String = context.getString(AppR.string.tts_info_2)
    private var sampleVisible by mutableStateOf(true)
    private var langName by mutableStateOf<String?>(null)
    private var langMenu by mutableStateOf(false)
    private var speedStep by mutableFloatStateOf(0f)
    private var toneStep by mutableFloatStateOf(0f)

    /** Background work. */
    private var executor: java.util.concurrent.ExecutorService? = null
    fun runAsync(runnable: Runnable?) {
        var ex = executor
        if (ex == null || ex.isShutdown() || ex.isTerminated()) {
            ex = MainApp.executor(context) ?: return
            executor = ex
        }
        try { ex.execute(runnable) } catch (unused: Exception) { }
    }

    private val initListener = OnInitListener { status ->
        if (status == -1) {
            releaseTts()
            langMenuShown = true
        } else if (speech == null) {
            langMenuShown = true
        } else {
            runAsync(Runnable {
                try {
                    val langs = speech!!.getAvailableLanguages()
                    val list: ArrayList<Any?>? = if (langs == null || langs.isEmpty()) null else ArrayList<Any?>(langs)
                    if (list != null) {
                        val sort = SortTts()
                        MainApp.currentLocale().let { sort.lang = it.getLanguage() }
                        try {
                            @Suppress("UNCHECKED_CAST")
                            Collections.sort(list as MutableList<Locale?>, sort)
                        } catch (unused: Exception) {
                        }
                    }
                    langItems = list
                    val locale = ttsLocale
                    if (locale != null && speech!!.isLanguageAvailable(locale) >= 0) speech!!.setLanguage(locale)
                } catch (unused: Exception) {
                }
                langMenuShown = true
            })
        }
    }

    init {
        try {
            speech = TextToSpeech(appContext, initListener)
            applyRatePitch()
        } catch (unused: Exception) {
        }
        sampleText = if (TextUtils.isEmpty(sample)) null else sample.replace('\n', ' ')
        ttsLocale = parseLocale(ttsLang)
        langName = ttsLocale?.getDisplayName()
        speedStep = valueToStep(ttsRate, 25).toFloat()
        toneStep = valueToStep(ttsPitch, 15).toFloat()
        applyLandscape(hostActivity?.isLandscapeScreen() ?: MainUtil.isLandscape(context))
        setSheet {
            if (sampleVisible && !TextUtils.isEmpty(sample)) {
                Card(
                    onClick = { speakSample() },
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp).heightIn(min = 72.dp),
                ) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(sample, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                Box {
                    ListItem(
                        headlineContent = { Text(stringResource(AppR.string.locale)) },
                        supportingContent = {
                            Column {
                                Text(langName ?: stringResource(AppR.string.auto_detect))
                                if (langName == null) Text(stringResource(AppR.string.not_support_site))
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        modifier = Modifier.clickable { openLangMenu() },
                    )
                    com.mycompany.app.ui.components.BubbleMenu(expanded = langMenu, onDismiss = { langMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(AppR.string.auto_detect)) },
                            trailingIcon = { RadioButton(selected = TextUtils.isEmpty(ttsLang), onClick = null) },
                            onClick = { langMenu = false; pickLang(0) },
                        )
                        langItems?.forEachIndexed { index, any ->
                            val locale = any as Locale
                            DropdownMenuItem(
                                text = { Text(locale.getDisplayName()) },
                                trailingIcon = { RadioButton(selected = !TextUtils.isEmpty(ttsLang) && ttsLang == locale.toString(), onClick = null) },
                                onClick = { langMenu = false; pickLang(index + 1) },
                            )
                        }
                    }
                }
                SliderRow(stringResource(AppR.string.voice_speed), speedStep, 25) { step ->
                    speedStep = step.toFloat()
                    if (speech != null) {
                        stopSpeaking()
                        ttsRate = stepToValue(step, 25)
                        speech!!.setSpeechRate(ttsRate)
                    }
                }
                SliderRow(stringResource(AppR.string.voice_tone), toneStep, 15) { step ->
                    toneStep = step.toFloat()
                    if (speech != null) {
                        stopSpeaking()
                        ttsPitch = stepToValue(step, 15)
                        speech!!.setPitch(ttsPitch)
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { askReset() }) { Text(stringResource(AppR.string.reset)) }
                TextButton(onClick = { saveSettings(true) }) { Text(stringResource(AppR.string.apply)) }
            }
        }
        show()
    }

    @Composable
    private fun SliderRow(title: String, value: Float, max: Int, onChange: (Int) -> Unit) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Text(String.format(Locale.US, "%.1f", stepToValue(value.roundToInt(), max)), style = MaterialTheme.typography.bodyLarge)
            }
            Slider(
                value = value,
                onValueChange = { onChange(it.roundToInt().coerceIn(0, max)) },
                valueRange = 0f..max.toFloat(),
                steps = max - 1,
            )
        }
    }

    private fun speakSample() {
        val sampleTextNow = sampleText
        val tts = speech
        if (TextUtils.isEmpty(sampleTextNow) || tts == null) return
        try {
            if (tts.isSpeaking()) tts.stop()
            tts.speak(sampleTextNow, 0, null, null)
        } catch (unused: Exception) {
        }
    }

    private fun openLangMenu() {
        if (!langMenuShown) {
            showToast(appContext, AppR.string.wait_retry)
            return
        }
        langMenu = true
    }

    /** Menu pick: 0 = auto detect, i = language i - 1 of [langItems]. */
    private fun pickLang(position: Int) {
        if (speech == null) return
        var key: String? = null
        ttsLocale = null
        val list = if (position <= 0) null else langItems
        val index = if (position <= 0 || list == null) 0 else position - 1
        var name: String? = null
        if (position > 0 && list != null && index < list.size) {
            val locale = list[index] as Locale
            ttsLocale = locale
            key = locale.toString()
            name = locale.getDisplayName()
        }
        if (!MainUtil.sameObject(ttsLang, key)) {
            ttsLang = key
            langName = if (TextUtils.isEmpty(name)) null else name
            runAsync(Runnable {
                stopSpeaking()
                try {
                    val locale = ttsLocale
                    if (locale != null) speech!!.setLanguage(locale) else speech!!.setLanguage(MainApp.currentLocale())
                } catch (unused: Exception) {
                }
            })
        }
    }

    private fun askReset() {
        if (hostActivity == null || msgDialog != null) return
        val msg = DialogSetMsg(hostActivity, AppR.string.reset_setting, AppR.string.reset, object : DialogApplyListener {
            override fun onApply() {
                msgDialog?.dismiss()
                msgDialog = null
                stopSpeaking()
                if (!MainUtil.sameObject(ttsLang, "")) {
                    ttsLang = ""
                    ttsLocale = parseLocale("")
                    langName = ttsLocale?.getDisplayName()
                    try {
                        val locale = ttsLocale
                        if (locale != null) speech?.setLanguage(locale) else speech?.setLanguage(MainApp.currentLocale())
                    } catch (unused: Exception) {
                    }
                }
                if (ttsRate.compareTo(1.0f) != 0) {
                    ttsRate = 1.0f
                    speedStep = valueToStep(1.0f, 25).toFloat()
                    speech?.setSpeechRate(ttsRate)
                }
                if (ttsPitch.compareTo(1.0f) != 0) {
                    ttsPitch = 1.0f
                    toneStep = valueToStep(1.0f, 15).toFloat()
                    speech?.setPitch(ttsPitch)
                }
                saveSettings(false)
            }
        })
        msgDialog = msg
        msg.setOnDismissListener(DialogInterface.OnDismissListener {
            msgDialog?.dismiss()
            msgDialog = null
        })
    }

    /** Landscape hides the sample sentence ([newLandscape] = landscape). */
    fun applyLandscape(newLandscape: Boolean) {
        var landscape = newLandscape
        if (landscape) landscape = hostActivity?.smallScreen ?: MainUtil.isSmallScreen(context)
        sampleVisible = !landscape
    }

    fun releaseTts() {
        this.langItems = null
        val textToSpeech = this.speech
        if (textToSpeech == null) {
            return
        }
        try {
            if (textToSpeech.isSpeaking()) {
                this.speech!!.stop()
            }
            this.speech!!.shutdown()
        } catch (unused: Exception) {
        }
        this.speech = null
    }

    fun saveSettings(apply: Boolean) {
        if (!MainUtil.sameObject(TtsSettings.ttsLang.value, this.ttsLang) || (TtsSettings.ttsRate.value).compareTo(this.ttsRate) != 0 || (TtsSettings.ttsPitch.value).compareTo(this.ttsPitch) != 0
        ) {
            TtsSettings.editLocal { set(TtsSettings.ttsLang, this@DialogSetTts.ttsLang) }
            TtsSettings.editLocal { set(TtsSettings.ttsRate, this@DialogSetTts.ttsRate) }
            TtsSettings.editLocal { set(TtsSettings.ttsPitch, this@DialogSetTts.ttsPitch) }
            val prefTtsR = PrefTts.get(this.appContext, false)
            if (apply) {
                prefTtsR!!.putString("mTtsLang", TtsSettings.ttsLang.value)
                prefTtsR.putFloat("mTtsRate", TtsSettings.ttsRate.value)
                prefTtsR.putFloat("mTtsPitch", TtsSettings.ttsPitch.value)
            } else {
                prefTtsR!!.removeKey("mTtsLang")
                prefTtsR.removeKey("mTtsRate")
                prefTtsR.removeKey("mTtsPitch")
            }
            prefTtsR.saveAsync()
        }
        if (apply) {
            dismiss()
        }
    }

    fun applyRatePitch() {
        if (this.speech != null) {
            try {
                var pitch = 0.5f
                if ((this.ttsRate).compareTo(1.0f) != 0) {
                    var ttsRate = this.ttsRate
                    if (ttsRate < 0.5f) {
                        this.ttsRate = 0.5f
                        ttsRate = 0.5f
                    } else if (ttsRate > 3.0f) {
                        this.ttsRate = 3.0f
                        ttsRate = 3.0f
                    }
                    this.speech!!.setSpeechRate(ttsRate)
                }
                if ((this.ttsPitch).compareTo(1.0f) != 0) {
                    val ttsPitch = this.ttsPitch
                    if (ttsPitch < 0.5f) {
                        this.ttsPitch = 0.5f
                    } else {
                        pitch = 2.0f
                        if (ttsPitch > 2.0f) {
                            this.ttsPitch = 2.0f
                        } else {
                            pitch = ttsPitch
                        }
                    }
                    this.speech!!.setPitch(pitch)
                }
            } catch (unused: Exception) {
            }
        }
    }

    fun stopSpeaking() {
        val textToSpeech = this.speech
        if (textToSpeech != null) {
            try {
                if (textToSpeech.isSpeaking()) {
                    this.speech!!.stop()
                }
            } catch (unused: Exception) {
            }
        }
    }

    override fun dismiss() {
        releaseTts()
        msgDialog?.dismiss()
        msgDialog = null
        hostActivity = null
        super.dismiss()
    }

    private class SortTts : Comparator<Locale?> {
        var lang: String? = null

        override fun compare(first: Locale?, second: Locale?): Int {
            val locale = first
            val other = second
            val lang = this.lang
            if (locale == null) {
                return if (other == null) 0 else 1
            }
            if (other == null) {
                return -1
            }
            val displayName = locale.getDisplayName()
            val otherDisplayName = other.getDisplayName()
            val displayNameEmpty = TextUtils.isEmpty(displayName)
            val displayName2Empty = TextUtils.isEmpty(otherDisplayName)
            if (displayNameEmpty) {
                return if (displayName2Empty) 0 else 1
            }
            if (displayName2Empty) {
                return -1
            }
            if (TextUtils.isEmpty(lang)) {
                return displayName.compareTo(otherDisplayName)
            }
            val language = locale.getLanguage()
            val otherLanguage = other.getLanguage()
            if (lang == language && lang != otherLanguage) {
                return -1
            }
            if (lang == otherLanguage && lang != language) {
                return 1
            }
            val name = locale.toString()
            val otherName = other.toString()
            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(otherName)) {
                if (name.startsWith(lang!!) && !otherName.startsWith(lang)) {
                    return -1
                }
                if (otherName.startsWith(lang) && !name.startsWith(lang)) {
                    return 1
                }
            }
            return displayName.compareTo(otherDisplayName)
        }
    }

    companion object {
        @JvmStatic
        fun stepToValue(step: Int, max: Int): kotlin.Float {
            val maxValue = (max / 10.0f) + 0.5f
            val floatValue = (step / 10.0f) + 0.5f
            if (floatValue < 0.5f) {
                return 0.5f
            }
            return if (floatValue > maxValue) maxValue else floatValue
        }

        @JvmStatic
        fun valueToStep(ttsRate: kotlin.Float, maxStep: Int): Int {
            val step = Math.round((ttsRate - 0.5f) * 10.0f)
            if (step < 0) {
                return 0
            }
            return if (step > maxStep) maxStep else step
        }
    }
}
