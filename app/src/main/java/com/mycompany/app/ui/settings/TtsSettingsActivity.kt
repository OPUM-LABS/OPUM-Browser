package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.speech.tts.TextToSpeech
import android.text.TextUtils
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.MainSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSliderRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsJump
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.rememberPref
import java.util.Locale

/**
 * Settings › Translate & read aloud › Text to Speech (2026-11-26: the voice was a dialog, the stop button's colour the
 * colour editor): reading aloud on or off, the voice (language, speed, tone, a sample to listen to), and the stop
 * button with the phone preview (in the middle while a text is read). Opened from the browser's read-aloud dialog
 * (`EXTRA_POPUP`) it tells the browser when reading aloud was switched (`EXTRA_TYPE` 60).
 */
class TtsSettingsActivity : OpumComposeActivity() {
    private var fromPopup = false
    private var ttsAtStart = false

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        fromPopup = intent.getBooleanExtra("EXTRA_POPUP", false)
        ttsAtStart = TtsSettings.ttsMode.value
        super.onCreate(savedInstanceState)
    }

    override fun finish() {
        if (fromPopup && ttsAtStart != TtsSettings.ttsMode.value) {
            ttsAtStart = TtsSettings.ttsMode.value
            setResult(RESULT_OK, Intent().putExtra("EXTRA_TYPE", 60))
        }
        super.finish()
    }

    @Composable
    override fun Content() {
        val context: Context = this
        val on = rememberPref({ TtsSettings.ttsMode.value }) { TtsSettings.edit(context) { set(TtsSettings.ttsMode, it) } }
        val stopIcon = rememberPref({ TtsSettings.ttsIcon.value }) { TtsSettings.edit(context) { set(TtsSettings.ttsIcon, it) } }
        // speed and tone in tenths (5 = 0.5x), language as a locale tag ("" = the engine detects it)
        val lang = rememberPref({ TtsSettings.ttsLang.value ?: "" }) { TtsSettings.edit(context) { set(TtsSettings.ttsLang, it) } }
        val rate = rememberPref({ (TtsSettings.ttsRate.value.coerceIn(0.5f, 3f) * 10f).let { Math.round(it) } }) {
            TtsSettings.edit(context) { set(TtsSettings.ttsRate, it / 10f) }
        }
        val pitch = rememberPref({ (TtsSettings.ttsPitch.value.coerceIn(0.5f, 2f) * 10f).let { Math.round(it) } }) {
            TtsSettings.edit(context) { set(TtsSettings.ttsPitch, it / 10f) }
        }
        val color = rememberPref({ EditorSettings.ttsColor.value }) { c ->
            val at = MainConst.gradientColors.indexOf(c)
            EditorSettings.edit(context) {
                set(EditorSettings.ttsColor, c)
                if (at >= 0) set(EditorSettings.ttsPos, MainConst.presetPositions[at])
            }
            PrefEditor.color = PrefEditor.withAlpha(EditorSettings.ttsColor.value, EditorSettings.ttsAlpha.value)
        }
        val alpha = rememberPref({ EditorSettings.ttsAlpha.value.coerceIn(0, 90) }) {
            EditorSettings.edit(context) { set(EditorSettings.ttsAlpha, it) }
            PrefEditor.color = PrefEditor.withAlpha(EditorSettings.ttsColor.value, EditorSettings.ttsAlpha.value)
        }
        var liveAlpha by remember { mutableIntStateOf(alpha.value) }
        var languages by remember { mutableStateOf<List<Locale>?>(null) }
        var picking by remember { mutableStateOf(false) }
        var flash by remember { mutableStateOf(false) }
        LaunchedEffect(flash) { if (flash) { kotlinx.coroutines.delay(1800); flash = false } }
        var jump by remember { mutableStateOf<SettingsJump?>(null) }
        var resetting by remember { mutableStateOf(false) }
        val portrait = LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE
        val auto = stringResource(R.string.auto_detect)
        val sample = stringResource(R.string.tts_info_2)

        // a speech engine for the language list and the sample, while the page is open
        val tts = remember { arrayOfNulls<TextToSpeech>(1) }
        DisposableEffect(Unit) {
            try {
                tts[0] = TextToSpeech(context) { status ->
                    val engine = tts[0]
                    if (status != TextToSpeech.SUCCESS || engine == null) {
                        tts[0] = null
                    } else {
                        languages = try {
                            val preferred = MainApp.currentLocale().language
                            engine.availableLanguages?.toList()?.sortedWith(compareBy<Locale>({ preferred == null || it.language != preferred }, { it.displayName })) ?: emptyList()
                        } catch (_: Exception) { emptyList() }
                    }
                }
            } catch (_: Exception) {
            }
            onDispose {
                try { tts[0]?.stop(); tts[0]?.shutdown() } catch (_: Exception) {}
                tts[0] = null
            }
        }

        fun speakSample() {
            val engine = tts[0] ?: run { MainUtil.showToastText(context, getString(R.string.wait_retry)); return }
            try {
                val locale = MainUtil.parseLocale(lang.value.ifEmpty { null })
                if (locale != null && engine.isLanguageAvailable(locale) >= 0) engine.language = locale else engine.language = MainApp.currentLocale()
                engine.setSpeechRate(rate.value / 10f)
                engine.setPitch(pitch.value / 10f)
                if (engine.isSpeaking) engine.stop()
                engine.speak(sample.replace('\n', ' '), TextToSpeech.QUEUE_FLUSH, null, null)
            } catch (_: Exception) {
            }
        }

        SettingsScaffold(
            title = stringResource(R.string.tts_mode),
            onBack = { finish() },
            onReset = { resetting = true },
            jumpTo = jump,
            above = if (!portrait) null else {
                {
                    StopPreview(stopIcon.value, PrefEditor.withAlpha(color.value, liveAlpha), flash) {
                        flash = true
                        jump = SettingsJump(getString(R.string.stop_icon))
                    }
                }
            },
        ) {
            item { SettingSwitchRow(title = stringResource(R.string.tts_on), checked = on.value, onCheckedChange = on::set) }
            settingsFooter(getString(R.string.tts_info_1))
            settingsHeader(getString(R.string.hd_voice))
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.locale),
                    value = MainUtil.parseLocale(lang.value.ifEmpty { null })?.displayName ?: auto,
                    onClick = { if (languages == null) MainUtil.showToastText(context, getString(R.string.wait_retry)) else picking = true },
                )
            }
            item {
                SettingSliderRow(
                    title = stringResource(R.string.voice_speed), value = rate.value, range = 5..30,
                    label = { "%.1fx".format(it / 10f) }, onValueChange = rate::set,
                )
            }
            item {
                SettingSliderRow(
                    title = stringResource(R.string.voice_tone), value = pitch.value, range = 5..20,
                    label = { "%.1fx".format(it / 10f) }, onValueChange = pitch::set,
                )
            }
            item { SettingRow(title = stringResource(R.string.tts_sample), summary = sample, onClick = { speakSample() }) }
            settingsFooter(getString(R.string.tts_guide_1) + " " + getString(R.string.tts_guide_2))
            settingsHeader(getString(R.string.hd_screen_button))
            item { SettingSwitchRow(title = stringResource(R.string.stop_icon), checked = stopIcon.value, onCheckedChange = { flash = true; stopIcon.set(it) }) }
            item {
                ButtonColorCircles(
                    title = stringResource(R.string.icon_color), enabled = stopIcon.value, reason = stringResource(R.string.stop_btn_off),
                    selected = color.value, transparency = liveAlpha,
                ) { flash = true; color.set(it) }
            }
            item {
                SettingSliderRow(
                    title = stringResource(R.string.color_alpha), value = alpha.value, range = 0..90, label = { "$it%" },
                    onValueChange = { alpha.set(it); liveAlpha = it }, enabled = stopIcon.value,
                    below = { v -> LaunchedEffect(v) { liveAlpha = v } },
                )
            }
            settingsFooter(getString(R.string.stop_icon_info) + " " + getString(R.string.drag_move_guide))
        }

        if (picking) {
            val list = languages.orEmpty()
            ChoiceDialog(
                title = stringResource(R.string.locale),
                options = listOf(auto) + list.map { it.displayName },
                selectedIndex = if (lang.value.isEmpty()) 0 else 1 + list.indexOfFirst { it.toString() == lang.value },
                onSelect = { index -> lang.set(if (index == 0) "" else list[index - 1].toString()) },
                onDismiss = { picking = false },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    on.reload(TtsSettings.ttsMode.value); stopIcon.reload(TtsSettings.ttsIcon.value)
                    lang.reload(TtsSettings.ttsLang.value ?: ""); rate.reload(10); pitch.reload(10)
                    color.reload(EditorSettings.ttsColor.value); alpha.reload(EditorSettings.ttsAlpha.value); liveAlpha = alpha.value
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /** The phone with the stop button in the middle, where the browser shows it while a text is read; a tap on it calls [onTapButton]. */
    @Composable
    private fun StopPreview(shown: Boolean, argb: Int, flash: Boolean, onTapButton: () -> Unit) {
        val bars = PreviewBars(
            BrowserSettings.showStatus.value, BrowserSettings.showNavi.value, BrowserSettings.fixTop.value, BrowserSettings.fixBot.value,
            PdfSettings.topToBottom.value,
            MainUtil.parseIntList(MainSettings.topItems.value)?.toList().orEmpty(),
            MainUtil.parseIntList(MainSettings.botItems.value)?.toList().orEmpty(),
            EditorSettings.topAlpha.value.coerceIn(0, 90), EditorSettings.botAlpha.value.coerceIn(0, 90),
            BrowserSettings.tabBar.value, currentPreviewTabs(),
        )
        val density = LocalDensity.current
        fun spot(height: Float, width: Float): Triple<Float, Float, Float> = with(density) {
            val size = 30.dp.toPx()
            Triple((width - size) / 2f, (height - size) / 2f + 64.dp.toPx() * PreviewSize.SCALE, size)
        }
        PhonePreview(
            bars,
            onTap = { at, places, width ->
                if (!shown) return@PhonePreview
                val r = spot(places.below + (if (bars.showNavi) with(density) { PreviewSize.sys.toPx() } else 0f), width)
                val slack = with(density) { 8.dp.toPx() }
                if (at.x in (r.first - slack)..(r.first + r.third + slack) && at.y in (r.second - slack)..(r.second + r.third + slack)) onTapButton()
            },
            over = { _, _ ->
                if (shown) {
                    var box by remember { mutableStateOf(IntSize.Zero) }
                    Box(Modifier.fillMaxSize().onSizeChanged { box = it }) {
                        val r = spot(box.height.toFloat(), box.width.toFloat())
                        PreviewRoundButton(r.first, r.second, with(density) { r.third.toDp() }, argb, R.drawable.baseline_stop_black_24, flash)
                    }
                }
            },
            scrolling = false,
        )
        Text(
            stringResource(R.string.look_tap_guide), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )
    }

    companion object {
        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            val color = MainConst.gradientColors[5]
            val pos = MainConst.presetPositions[5]
            if (TtsSettings.ttsMode.value) TtsSettings.reset(context, listOf(TtsSettings.ttsMode))
            if (!TextUtils.isEmpty(TtsSettings.ttsLang.value) || TtsSettings.ttsRate.value.compareTo(1.0f) != 0 || TtsSettings.ttsPitch.value.compareTo(1.0f) != 0) {
                TtsSettings.editLocal { set(TtsSettings.ttsLang, "") }; TtsSettings.editLocal { set(TtsSettings.ttsRate, 1.0f) }; TtsSettings.editLocal { set(TtsSettings.ttsPitch, 1.0f) }
                val prefs = PrefTts.get(context, false)
                prefs!!.removeKey("mTtsLang"); prefs.removeKey("mTtsRate"); prefs.removeKey("mTtsPitch"); prefs.save()
            }
            if (!TtsSettings.ttsIcon.value) TtsSettings.reset(context, listOf(TtsSettings.ttsIcon))
            if (EditorSettings.ttsAlpha.value == 0 && EditorSettings.ttsColor.value == color && EditorSettings.ttsPos.value.compareTo(pos) == 0) return
            EditorSettings.editLocal { set(EditorSettings.ttsAlpha, 0) }
            EditorSettings.editLocal { set(EditorSettings.ttsColor, color) }
            EditorSettings.editLocal { set(EditorSettings.ttsPos, pos) }
            PrefEditor.color = PrefEditor.withAlpha(color, 0)
            val prefs = PrefEditor.get(context)
            prefs!!.removeKey("mTtsAlpha"); prefs.removeKey("mTtsColor"); prefs.removeKey("mTtsPos"); prefs.save()
        }
    }
}
