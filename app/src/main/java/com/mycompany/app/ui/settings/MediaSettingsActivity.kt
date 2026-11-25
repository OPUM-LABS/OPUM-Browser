package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.text.TextUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.mycompany.app.dialog.DialogSetDown
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.MainSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ChoiceDialog
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
import com.mycompany.app.ui.prefs.rememberPref
import kotlin.math.roundToInt

/**
 * Settings › Media (2026-11-26: one video button; YouTube had a button and a page of its own, and until 2026-11-25 the
 * button's colour and buttons were dialogs): the phone preview with the video button where it is set (at the end of
 * the top or bottom bar, or floating, in its colour), its place, colour and transparency, the buttons it offers on a
 * video (on YouTube picture-in-picture and play speed, elsewhere picture-in-picture, full screen and download; one
 * always stays on), the full screen player, background playback, and YouTube autoplay and unmute. Rows that need the
 * button stay, greyed, with the reason.
 */
class MediaSettingsActivity : com.mycompany.app.ui.OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val pos = rememberPref({ ZoneSettings.downPos.value }) { ZoneSettings.edit(context) { set(ZoneSettings.downPos, it) } }
        val color = rememberPref({ EditorSettings.iconColor.value }) { storeColor(it) }
        val alpha = rememberPref({ EditorSettings.iconAlpha.value.coerceIn(0, 90) }) { storeAlpha(it) }
        val pip = rememberPref({ ZoneSettings.showPip.value }) { ZoneSettings.edit(context) { set(ZoneSettings.showPip, it) } }
        val full = rememberPref({ ZoneSettings.showFull.value }) { ZoneSettings.edit(context) { set(ZoneSettings.showFull, it) } }
        val down = rememberPref({ ZoneSettings.showDown.value }) { ZoneSettings.edit(context) { set(ZoneSettings.showDown, it) } }
        val speed = rememberPref({ ZoneSettings.youRate.value }) { ZoneSettings.edit(context) { set(ZoneSettings.youRate, it) } }
        val autoPlay = rememberPref({ ZtwoSettings.autoPlay.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.autoPlay, it) } }
        val unmute = rememberPref({ ZtwoSettings.youUnmute.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.youUnmute, it) } }
        val backPlay = rememberPref({ ZoneSettings.backPlay.value }) {
            ZoneSettings.edit(context) { set(ZoneSettings.backPlay, it) }
            if (!it) {
                MainUtil.clearHistoryData(context)
                sendBroadcast(Intent("ch.opumlabs.opumbrowser.ACTION_BACK_STOP").setPackage(packageName))
            }
        }
        var playerName by remember { mutableStateOf(AlbumSettings.playName.value) }
        var choosingPlayer by remember { mutableStateOf(false) }
        var liveAlpha by remember { mutableIntStateOf(alpha.value) }
        var flash by remember { mutableStateOf(false) }
        LaunchedEffect(flash) { if (flash) { kotlinx.coroutines.delay(1800); flash = false } }
        var jump by remember { mutableStateOf<SettingsJump?>(null) }
        var resetting by remember { mutableStateOf(false) }
        val portrait = LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE
        val shown = pos.value != 0
        // the buttons on a video: one of them always stays on, on YouTube (picture-in-picture, play speed) and elsewhere
        // (picture-in-picture, full screen, download)
        fun canSwitch(on: Boolean, others: List<Boolean>) = shown && !(on && others.none { it })
        val off = if (shown) null else stringResource(R.string.media_btn_off)

        SettingsScaffold(
            title = stringResource(R.string.media),
            onBack = { finish() },
            onReset = { resetting = true },
            jumpTo = jump,
            above = if (!portrait) null else {
                {
                    ManagerPreview(pos.value, PrefEditor.withAlpha(color.value, liveAlpha), flash) {
                        flash = true
                        jump = SettingsJump(getString(R.string.video_manager))
                    }
                }
            },
        ) {
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.video_manager),
                    summary = if (pos.value == 3) stringResource(R.string.drag_move_guide) else null,
                    options = MediaSettingsActivity.positionValues.map { stringResource(MediaSettingsActivity.positionLabels[it]) },
                    selectedIndex = MediaSettingsActivity.positionValues.indexOf(pos.value),
                    onSelect = { flash = true; pos.set(MediaSettingsActivity.positionValues[it]) },
                )
            }
            item { ColorCircles(title = stringResource(R.string.icon_color), shown = shown, selected = color.value, transparency = liveAlpha) { flash = true; color.set(it) } }
            item {
                SettingSliderRow(
                    title = stringResource(R.string.color_alpha), value = alpha.value, range = 0..90, label = { "$it%" },
                    onValueChange = { alpha.set(it); liveAlpha = it }, enabled = shown,
                    below = { v -> LaunchedEffect(v) { liveAlpha = v } },
                )
            }
            settingsFooter(getString(R.string.video_down_guide_1))
            settingsFooter(getString(R.string.video_down_guide_2))
            settingsFooter(getString(R.string.video_button_info))
            settingsHeader(getString(R.string.hd_video_buttons))
            item { SettingSwitchRow(title = stringResource(R.string.pip_mode), summary = off, checked = pip.value, enabled = canSwitch(pip.value, listOf(full.value, down.value)) && canSwitch(pip.value, listOf(speed.value)), onCheckedChange = pip::set) }
            item { SettingSwitchRow(title = stringResource(R.string.full_screen), summary = off ?: stringResource(R.string.not_youtube), checked = full.value, enabled = canSwitch(full.value, listOf(pip.value, down.value)), onCheckedChange = full::set) }
            item { SettingSwitchRow(title = stringResource(R.string.download), summary = off ?: stringResource(R.string.not_youtube), checked = down.value, enabled = canSwitch(down.value, listOf(pip.value, full.value)), onCheckedChange = down::set) }
            item { SettingSwitchRow(title = stringResource(R.string.play_speed), summary = off ?: stringResource(R.string.only_youtube), checked = speed.value, enabled = canSwitch(speed.value, listOf(pip.value)), onCheckedChange = speed::set) }
            // the player for full screen videos, while the full screen button is off
            item {
                SettingRow(
                    title = stringResource(R.string.video_player),
                    summary = if (!TextUtils.isEmpty(playerName)) playerName else stringResource(R.string.default_title),
                    enabled = shown && !full.value,
                    onClick = { choosingPlayer = true },
                )
            }
            settingsFooter(getString(R.string.keep_one_button))
            settingsHeader(getString(R.string.hd_playback))
            item { SettingSwitchRow(title = stringResource(R.string.back_play), checked = backPlay.value, onCheckedChange = backPlay::set) }
            settingsFooter(getString(R.string.not_support_site))
            settingsFooter(getString(R.string.back_play_info))
            settingsHeader(getString(R.string.youtube_title))
            item { SettingSwitchRow(title = stringResource(R.string.youtube_auto), checked = autoPlay.value, onCheckedChange = autoPlay::set) }
            item { SettingSwitchRow(title = stringResource(R.string.youtube_unmute), checked = unmute.value, onCheckedChange = unmute::set) }
        }

        if (choosingPlayer) {
            ChoiceDialog(
                title = stringResource(R.string.video_player),
                options = listOf(stringResource(R.string.default_title), stringResource(R.string.other_app)),
                selectedIndex = if (TextUtils.isEmpty(playerName)) 0 else 1,
                onSelect = { index ->
                    if (index == 0) {
                        storePlayer(context, "", "", ""); playerName = ""
                    } else {
                        DialogSetDown(this@MediaSettingsActivity, "video/*", MainUtil.isLandscape(context), object : DialogSetDown.SetDownListener {
                            override fun onPick(label: String?, packageName: String?, className: String?) {
                                if (TextUtils.isEmpty(label) || TextUtils.isEmpty(packageName) || TextUtils.isEmpty(className)) return
                                storePlayer(context, label, packageName, className); playerName = label
                            }
                        })
                    }
                },
                onDismiss = { choosingPlayer = false },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    pos.reload(ZoneSettings.downPos.value); color.reload(EditorSettings.iconColor.value)
                    alpha.reload(EditorSettings.iconAlpha.value); liveAlpha = alpha.value
                    pip.reload(ZoneSettings.showPip.value); full.reload(ZoneSettings.showFull.value); down.reload(ZoneSettings.showDown.value)
                    backPlay.reload(ZoneSettings.backPlay.value); playerName = AlbumSettings.playName.value
                    speed.reload(ZoneSettings.youRate.value); autoPlay.reload(ZtwoSettings.autoPlay.value); unmute.reload(ZtwoSettings.youUnmute.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /** The phone with the manager button at [pos] in [argb]; a tap on the button calls [onTapButton]. */
    @Composable
    private fun ManagerPreview(pos: Int, argb: Int, flash: Boolean, onTapButton: () -> Unit) {
        val bars = PreviewBars(
            BrowserSettings.showStatus.value, BrowserSettings.showNavi.value, BrowserSettings.fixTop.value, BrowserSettings.fixBot.value,
            PdfSettings.topToBottom.value,
            MainUtil.parseIntList(MainSettings.topItems.value)?.toList().orEmpty(),
            MainUtil.parseIntList(MainSettings.botItems.value)?.toList().orEmpty(),
            EditorSettings.topAlpha.value.coerceIn(0, 90), EditorSettings.botAlpha.value.coerceIn(0, 90),
            BrowserSettings.tabBar.value, currentPreviewTabs(),
        )
        val density = LocalDensity.current
        PhonePreview(
            bars,
            onTap = { at, places, width ->
                val r = with(density) { managerRect(pos, places, width, PreviewSize.bar.toPx(), 26.dp.toPx(), 8.dp.toPx()) }
                val slack = with(density) { 8.dp.toPx() }
                if (r != null && at.x >= r.first - slack && at.y in (r.second - slack)..(r.second + r.third + slack)) onTapButton()
            },
            over = { places, _ -> ManagerButton(pos, places, argb, flash) },
            scrolling = false,
        )
        Text(
            stringResource(R.string.look_tap_guide), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )
    }

    /** The button colours as circles at the chosen transparency, the chosen one ticked; greyed with a reason when not [shown]. */
    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun ColorCircles(title: String, shown: Boolean, selected: Int, transparency: Int, onPick: (Int) -> Unit) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (shown) 1f else 0.38f))
            if (!shown) Text(stringResource(R.string.media_btn_off), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FlowRow(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MainConst.gradientColors.forEach { argb ->
                    Box(
                        Modifier.size(34.dp)
                            .background(Color(argb).copy(alpha = (100 - transparency) / 100f * (if (shown) 1f else 0.38f)), CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable(enabled = shown) { onPick(argb) },
                        contentAlignment = Alignment.Center,
                    ) { if (argb == selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White) }
                }
            }
        }
    }

    /** Stores the button's colour (and its place on the old colour strip, when it is one of the presets). */
    private fun storeColor(c: Int) {
        val at = MainConst.gradientColors.indexOf(c)
        EditorSettings.edit(this) {
            set(EditorSettings.iconColor, c)
            if (at >= 0) set(EditorSettings.iconPos, MainConst.presetPositions[at])
        }
        PrefEditor.iconArgb = PrefEditor.withAlpha(EditorSettings.iconColor.value, EditorSettings.iconAlpha.value)
    }

    private fun storeAlpha(a: Int) {
        EditorSettings.edit(this) { set(EditorSettings.iconAlpha, a) }
        PrefEditor.iconArgb = PrefEditor.withAlpha(EditorSettings.iconColor.value, EditorSettings.iconAlpha.value)
    }

    /**
     * Where the video button sits in a screen [width] px wide: left, top and height in px, or null when not shown. In a
     * bar it takes the bar's last slot ([bar] px); floating it is a circle of [size] px at the right edge, just above
     * the bars at the bottom (as the browser puts it).
     */
    private fun managerRect(pos: Int, places: PreviewPlaces, width: Float, bar: Float, size: Float, margin: Float): Triple<Float, Float, Float>? = when (pos) {
        1 -> places.topY?.let { Triple(width - bar, it, bar) }
        2 -> places.botY?.let { Triple(width - bar, it, bar) }
        3 -> Triple(width - size - margin, places.anchor - size - margin * 2, size)
        else -> null
    }

    /** The video button on the phone, outlined while [flash]. */
    @Composable
    private fun BoxScope.ManagerButton(pos: Int, places: PreviewPlaces, argb: Int, flash: Boolean) {
        if (pos == 0) return
        var box by remember { mutableStateOf(IntSize.Zero) }
        val density = LocalDensity.current
        val dark = com.mycompany.app.ui.state.AppState.darkUi
        val icon = R.drawable.outline_video_library_white_24
        val barIcon = if (dark) R.drawable.outline_video_library_dark_24 else R.drawable.outline_video_library_black_24
        Box(Modifier.fillMaxSize().onSizeChanged { box = it }) {
            val r = with(density) { managerRect(pos, places, box.width.toFloat(), PreviewSize.bar.toPx(), 26.dp.toPx(), 8.dp.toPx()) } ?: return@Box
            val side = with(density) { r.third.toDp() }
            val ring = if (flash) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, if (pos == 3) CircleShape else RoundedCornerShape(6.dp)) else Modifier
            if (pos == 3) {
                Box(
                    Modifier.offset { IntOffset(r.first.roundToInt(), r.second.roundToInt()) }.size(side)
                        .background(Color(argb), CircleShape).border(1.dp, Color(0x33000000), CircleShape).then(ring),
                    contentAlignment = Alignment.Center,
                ) { Icon(painterResource(icon), null, tint = Color.White, modifier = Modifier.size(side * 0.6f)) }
            } else {
                // the bar's last slot: the button is added at the end of the bar
                val base = if (dark) MyBarFrameColors.dark else MyBarFrameColors.light
                Box(
                    Modifier.offset { IntOffset(r.first.roundToInt(), r.second.roundToInt()) }.size(side)
                        .background(base).then(ring),
                    contentAlignment = Alignment.Center,
                ) {
                    androidx.compose.foundation.Image(painterResource(barIcon), null, Modifier.size(side * 0.55f))
                    Box(Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 4.dp).size(5.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun storePlayer(context: Context, label: String?, packageName: String?, className: String?) {
            AlbumSettings.editLocal { set(AlbumSettings.playName, label) }
            AlbumSettings.editLocal { set(AlbumSettings.playPkg, packageName) }
            AlbumSettings.editLocal { set(AlbumSettings.playCls, className) }
            PrefAlbum.get(context, false)?.let {
                it.putString("mPlayName2", AlbumSettings.playName.value)
                it.putString("mPlayPkg2", AlbumSettings.playPkg.value)
                it.putString("mPlayCls2", AlbumSettings.playCls.value)
                it.saveAsync()
            }
        }

        /** Selectable button positions, in menu order (1 top bar, 2 bottom bar, 3 floating, 0 not used). */
        @JvmField
        val positionValues: IntArray = intArrayOf(1, 2, 3, 0)

        /** Label of each position value (index = value). Used by the settings search index too. */
        @JvmField
        val positionLabels: IntArray = intArrayOf(R.string.not_used, R.string.top_bar, R.string.bot_bar, R.string.float_button)

        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            val color = MainConst.gradientColors[5]
            val pos = MainConst.presetPositions[5]
            if (ZoneSettings.downPos.value != 3) ZoneSettings.reset(context, listOf(ZoneSettings.downPos))
            if (EditorSettings.iconAlpha.value != 0 || EditorSettings.iconColor.value != color || EditorSettings.iconPos.value.compareTo(pos) != 0) {
                EditorSettings.editLocal { set(EditorSettings.iconAlpha, 0) }; EditorSettings.editLocal { set(EditorSettings.iconColor, color) }; EditorSettings.editLocal { set(EditorSettings.iconPos, pos) }; PrefEditor.iconArgb = PrefEditor.withAlpha(color, 0)
                val p = PrefEditor.get(context)
                p!!.removeKey("mIconAlpha"); p.removeKey("mIconColor"); p.removeKey("mIconPos"); p.save()
            }
            if (!ZoneSettings.showPip.value) ZoneSettings.reset(context, listOf(ZoneSettings.showPip))
            if (!ZoneSettings.showFull.value) ZoneSettings.reset(context, listOf(ZoneSettings.showFull))
            if (!ZoneSettings.showDown.value) ZoneSettings.reset(context, listOf(ZoneSettings.showDown))
            if (!TextUtils.isEmpty(AlbumSettings.playName.value) || !TextUtils.isEmpty(AlbumSettings.playPkg.value) || !TextUtils.isEmpty(AlbumSettings.playCls.value)) {
                AlbumSettings.editLocal { set(AlbumSettings.playName, "") }; AlbumSettings.editLocal { set(AlbumSettings.playPkg, "") }; AlbumSettings.editLocal { set(AlbumSettings.playCls, "") }
                val p = PrefAlbum.get(context, false)
                p!!.removeKey("mPlayName2"); p.removeKey("mPlayPkg2"); p.removeKey("mPlayCls2"); p.save()
            }
            if (ZoneSettings.backPlay.value) ZoneSettings.reset(context, listOf(ZoneSettings.backPlay))
            if (!ZoneSettings.youRate.value) ZoneSettings.reset(context, listOf(ZoneSettings.youRate))
            if (!ZtwoSettings.autoPlay.value) ZtwoSettings.reset(context, listOf(ZtwoSettings.autoPlay))
            if (!ZtwoSettings.youUnmute.value) ZtwoSettings.reset(context, listOf(ZtwoSettings.youUnmute))
        }
    }
}
