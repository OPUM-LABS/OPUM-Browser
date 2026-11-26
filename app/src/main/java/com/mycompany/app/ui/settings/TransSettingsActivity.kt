package com.mycompany.app.ui.settings

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogTransLang
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.MainSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingSliderRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsJump
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.rememberPref
import kotlin.math.roundToInt

/**
 * Settings › Translate & read aloud › Translator (2026-11-26, was the whole page): when to translate (choices on the
 * page, a dialog before), the language (its picker, which the browser uses too), recent languages, the translate
 * button with the phone preview (in the address bar or floating; its colour as circles, the colour editor before),
 * and the sites never translated automatically. The browser's translate dialogs open this page.
 */
class TransSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val mode = rememberPref({ AlbumSettings.transMode.value }) { AlbumSettings.edit(context) { set(AlbumSettings.transMode, it) } }
        val recent = rememberPref({ ZtwoSettings.recentTrans.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.recentTrans, it) } }
        val iconAlways = rememberPref({ AlbumSettings.transIcon.value }) { AlbumSettings.edit(context) { set(AlbumSettings.transIcon, it) } }
        val onPage = rememberPref({ AlbumSettings.transPage.value }) { AlbumSettings.edit(context) { set(AlbumSettings.transPage, it) } }
        val color = rememberPref({ EditorSettings.trnsColor.value }) { c ->
            val at = MainConst.gradientColors.indexOf(c)
            EditorSettings.edit(context) {
                set(EditorSettings.trnsColor, c)
                if (at >= 0) set(EditorSettings.trnsPos, MainConst.presetPositions[at])
            }
            PrefEditor.trnsArgb = PrefEditor.withAlpha(EditorSettings.trnsColor.value, EditorSettings.trnsAlpha.value)
        }
        val alpha = rememberPref({ EditorSettings.trnsAlpha.value.coerceIn(0, 90) }) {
            EditorSettings.edit(context) { set(EditorSettings.trnsAlpha, it) }
            PrefEditor.trnsArgb = PrefEditor.withAlpha(EditorSettings.trnsColor.value, EditorSettings.trnsAlpha.value)
        }
        var locale by remember { mutableStateOf(AlbumSettings.transPick.value ?: "") }
        var liveAlpha by remember { mutableIntStateOf(alpha.value) }
        var flash by remember { mutableStateOf(false) }
        LaunchedEffect(flash) { if (flash) { kotlinx.coroutines.delay(1800); flash = false } }
        var jump by remember { mutableStateOf<SettingsJump?>(null) }
        var resetting by remember { mutableStateOf(false) }
        val portrait = LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE

        OnScreenResume {
            locale = AlbumSettings.transPick.value ?: ""
            recent.reload(ZtwoSettings.recentTrans.value)
        }

        SettingsScaffold(
            title = stringResource(R.string.translator),
            onBack = { finish() },
            onReset = { resetting = true },
            jumpTo = jump,
            above = if (!portrait) null else {
                {
                    TranslatePreview(onPage.value, PrefEditor.withAlpha(color.value, liveAlpha), flash) {
                        flash = true
                        jump = SettingsJump(getString(R.string.icon_pos))
                    }
                }
            },
        ) {
            item {
                val info = modeInfos[mode.value]
                SettingSegmentedRow(
                    title = stringResource(R.string.trans_detect),
                    summary = if (info != 0) stringResource(info) else null,
                    options = modeValues.map { stringResource(modeLabels[it]) },
                    selectedIndex = modeValues.indexOf(mode.value),
                    onSelect = { mode.set(modeValues[it]) },
                )
            }
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.locale),
                    value = locale.ifEmpty { stringResource(R.string.system_name) },
                    onClick = {
                        com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host -> DialogTransLang(host, false, null) }
                    },
                )
            }
            // recent languages: 0 = do not save, < 0 = unlimited, > 0 = how many
            settingsHeader(getString(R.string.recent_lang))
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.hd_keep),
                    options = listOf(stringResource(R.string.history_zero), stringResource(R.string.history_none), stringResource(R.string.user_defined)),
                    selectedIndex = when { recent.value == 0 -> 0; recent.value < 0 -> 1; else -> 2 },
                    onSelect = { index -> recent.set(when (index) { 0 -> 0; 1 -> -1; else -> 5 }) },
                )
            }
            item {
                SettingSliderRow(
                    title = stringResource(R.string.hd_count), value = recent.value.coerceIn(1, 50), range = 1..50,
                    onValueChange = recent::set, enabled = recent.value > 0,
                    summary = if (recent.value > 0) null else stringResource(R.string.only_custom, stringResource(R.string.user_defined)),
                )
            }
            settingsHeader(getString(R.string.hd_trans_button))
            item {
                SettingSwitchRow(title = stringResource(R.string.trans_icon_always), checked = iconAlways.value, onCheckedChange = iconAlways::set)
            }
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.icon_pos),
                    summary = if (onPage.value) stringResource(R.string.drag_move_guide) else null,
                    options = listOf(stringResource(R.string.address_bar), stringResource(R.string.float_button)),
                    selectedIndex = if (onPage.value) 1 else 0,
                    onSelect = { flash = true; onPage.set(it == 1) },
                )
            }
            item {
                ButtonColorCircles(
                    title = stringResource(R.string.icon_color), enabled = onPage.value, reason = stringResource(R.string.float_only),
                    selected = color.value, transparency = liveAlpha,
                ) { flash = true; color.set(it) }
            }
            item {
                SettingSliderRow(
                    title = stringResource(R.string.color_alpha), value = alpha.value, range = 0..90, label = { "$it%" },
                    onValueChange = { alpha.set(it); liveAlpha = it }, enabled = onPage.value,
                    below = { v -> LaunchedEffect(v) { liveAlpha = v } },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.trans_except),
                    onClick = { com.mycompany.app.ui.lists.SiteListActivity.open(context, 29) },
                )
            }
            settingsFooter(getString(R.string.trans_except_info))
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    mode.reload(AlbumSettings.transMode.value); recent.reload(ZtwoSettings.recentTrans.value)
                    iconAlways.reload(AlbumSettings.transIcon.value); onPage.reload(AlbumSettings.transPage.value)
                    color.reload(EditorSettings.trnsColor.value); alpha.reload(EditorSettings.trnsAlpha.value); liveAlpha = alpha.value
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /**
     * The phone with the translate button: floating at the lower right (where the browser starts it, left of the video
     * button) in its colour, or inside the address bar at the end of the address. A tap on it calls [onTapButton].
     */
    @Composable
    private fun TranslatePreview(floating: Boolean, argb: Int, flash: Boolean, onTapButton: () -> Unit) {
        val topIds = MainUtil.parseIntList(MainSettings.topItems.value)?.toList().orEmpty()
        val botIds = MainUtil.parseIntList(MainSettings.botItems.value)?.toList().orEmpty()
        val bars = PreviewBars(
            BrowserSettings.showStatus.value, BrowserSettings.showNavi.value, BrowserSettings.fixTop.value, BrowserSettings.fixBot.value,
            PdfSettings.topToBottom.value, topIds, botIds,
            EditorSettings.topAlpha.value.coerceIn(0, 90), EditorSettings.botAlpha.value.coerceIn(0, 90),
            BrowserSettings.tabBar.value, currentPreviewTabs(),
        )
        val density = LocalDensity.current
        val dark = com.mycompany.app.ui.state.AppState.darkUi

        /** Top left and size of the button in px, or null when the bars have no address field. */
        fun spot(places: PreviewPlaces, width: Float): Triple<Float, Float, Float>? = with(density) {
            val bar = PreviewSize.bar.toPx()
            if (floating) {
                val size = 26.dp.toPx()
                return Triple(width - size * 2 - 12.dp.toPx(), places.anchor - size - 16.dp.toPx(), size)
            }
            // the address field takes the room the other buttons leave; the translate icon sits at its end, before reload
            val inTop = 1 in topIds
            val ids = if (inTop) topIds else botIds
            val y = (if (inTop) places.topY else places.botY) ?: return null
            if (1 !in ids) return null
            val after = ids.size - 1 - ids.indexOf(1)
            val end = width - 4.dp.toPx() - after * bar
            val size = bar * 0.6f
            Triple(end - bar * 0.85f - size, y + (bar - size) / 2f, size)
        }

        PhonePreview(
            bars,
            onTap = { at, places, width ->
                val r = spot(places, width)
                val slack = with(density) { 8.dp.toPx() }
                if (r != null && at.x in (r.first - slack)..(r.first + r.third + slack) && at.y in (r.second - slack)..(r.second + r.third + slack)) onTapButton()
            },
            over = { places, _ ->
                var box by remember { mutableStateOf(IntSize.Zero) }
                Box(Modifier.fillMaxSize().onSizeChanged { box = it }) {
                    val r = spot(places, box.width.toFloat()) ?: return@Box
                    val side = with(density) { r.third.toDp() }
                    if (floating) {
                        PreviewRoundButton(r.first, r.second, side, argb, R.drawable.outline_g_translate_white_24, flash)
                    } else {
                        Image(
                            painterResource(if (dark) R.drawable.outline_g_translate_dark_24 else R.drawable.outline_g_translate_black_24), null,
                            Modifier.offset { IntOffset(r.first.roundToInt(), r.second.roundToInt()) }.size(side),
                        )
                        if (flash) {
                            Box(
                                Modifier.offset { IntOffset((r.first - 6f).roundToInt(), (r.second - 6f).roundToInt()) }.size(side + 4.dp)
                                    .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            )
                        }
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
        /** Translation modes in menu order (1 auto, 2 always, 0 not used); labels/infos are indexed by mode value. */
        @JvmField
        val modeValues: IntArray = intArrayOf(1, 2, 0)

        @JvmField
        val modeLabels: IntArray = intArrayOf(R.string.not_used, R.string.trans_auto, R.string.trans_always)

        @JvmField
        val modeInfos: IntArray = intArrayOf(0, R.string.trans_auto_info, R.string.trans_always_info)

        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            val color = MainConst.gradientColors[5]
            val pos = MainConst.presetPositions[5]
            if (AlbumSettings.transMode.value != 0) AlbumSettings.reset(context, listOf(AlbumSettings.transMode))
            if (ZtwoSettings.recentTrans.value != 5) ZtwoSettings.reset(context, listOf(ZtwoSettings.recentTrans))
            if (AlbumSettings.transIcon.value) AlbumSettings.reset(context, listOf(AlbumSettings.transIcon))
            if (AlbumSettings.transPage.value) AlbumSettings.reset(context, listOf(AlbumSettings.transPage))
            if (EditorSettings.trnsAlpha.value != 0 || EditorSettings.trnsColor.value != color || EditorSettings.trnsPos.value.compareTo(pos) != 0) {
                EditorSettings.editLocal { set(EditorSettings.trnsAlpha, 0) }; EditorSettings.editLocal { set(EditorSettings.trnsColor, color) }; EditorSettings.editLocal { set(EditorSettings.trnsPos, pos) }; PrefEditor.trnsArgb = PrefEditor.withAlpha(color, 0)
                val p = PrefEditor.get(context)
                p!!.removeKey("mTrnsAlpha"); p.removeKey("mTrnsColor"); p.removeKey("mTrnsPos"); p.save()
            }
        }
    }
}
