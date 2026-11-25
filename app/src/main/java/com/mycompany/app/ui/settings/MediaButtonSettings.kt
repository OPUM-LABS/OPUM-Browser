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
 * What the two Media pages ([MediaVideoSettingsActivity], [MediaYouTubeSettingsActivity]; 2026-11-25, were one page
 * with two dialogs each) share: the phone preview of the other pages
 * ([PhonePreview], still) with the manager button where it is set (at the end of the top or bottom bar, or floating,
 * in its colour), its place as choices on the page, its colour and transparency (circles and a slider, the colour
 * editor before), and the buttons shown on a video as switches (a dialog before; one always stays on, as the dialog's
 * Apply asked). Rows that only matter with the button shown stay, greyed, with the reason.
 */
abstract class MediaButtonActivity : OpumComposeActivity() {

    /** The phone with the manager button at [pos] in [argb]; a tap on the button calls [onTapButton]. */
    @Composable
    protected fun ManagerPreview(pos: Int, you: Boolean, argb: Int, flash: Boolean, onTapButton: () -> Unit) {
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
                val r = with(density) { managerRect(pos, you, places, width, PreviewSize.bar.toPx(), 26.dp.toPx(), 8.dp.toPx()) }
                val slack = with(density) { 8.dp.toPx() }
                if (r != null && at.x >= r.first - slack && at.y in (r.second - slack)..(r.second + r.third + slack)) onTapButton()
            },
            over = { places, _ -> ManagerButton(pos, you, places, argb, flash) },
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
    protected fun ColorCircles(title: String, shown: Boolean, selected: Int, transparency: Int, onPick: (Int) -> Unit) {
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

    /** Stores the manager button's colour (and its place on the old colour strip, when it is one of the presets). */
    protected fun storeColor(you: Boolean, c: Int) {
        val at = MainConst.gradientColors.indexOf(c)
        EditorSettings.edit(this) {
            set(if (you) EditorSettings.ypipColor else EditorSettings.iconColor, c)
            if (at >= 0) set(if (you) EditorSettings.ypipPos else EditorSettings.iconPos, MainConst.presetPositions[at])
        }
        storeArgb(you)
    }

    protected fun storeAlpha(you: Boolean, a: Int) {
        EditorSettings.edit(this) { set(if (you) EditorSettings.ypipAlpha else EditorSettings.iconAlpha, a) }
        storeArgb(you)
    }

    /**
     * Where the manager button sits in a screen [width] px wide: left, top and height in px, or null when not shown.
     * In a bar it takes the bar's last slot ([bar] px); floating it is a circle of [size] px at the right edge, the
     * video one just above the bars at the bottom, the YouTube one at two fifths of the height (as the browser puts them).
     */
    private fun managerRect(pos: Int, you: Boolean, places: PreviewPlaces, width: Float, bar: Float, size: Float, margin: Float): Triple<Float, Float, Float>? = when (pos) {
        1 -> places.topY?.let { Triple(width - bar, it, bar) }
        2 -> places.botY?.let { Triple(width - bar, it, bar) }
        3 -> Triple(width - size - margin, if (you) places.below * 0.4f else places.anchor - size - margin * 2, size)
        else -> null
    }

    /** The manager button on the phone, outlined while [flash]. */
    @Composable
    private fun BoxScope.ManagerButton(pos: Int, you: Boolean, places: PreviewPlaces, argb: Int, flash: Boolean) {
        if (pos == 0) return
        var box by remember { mutableStateOf(IntSize.Zero) }
        val density = LocalDensity.current
        val dark = com.mycompany.app.ui.state.AppState.darkUi
        val icon = if (you) R.drawable.outline_youtube_library_2_white_24 else R.drawable.outline_video_library_white_24
        val barIcon = if (you) {
            if (dark) R.drawable.outline_youtube_library_2_dark_24 else R.drawable.outline_youtube_library_2_black_24
        } else {
            if (dark) R.drawable.outline_video_library_dark_24 else R.drawable.outline_video_library_black_24
        }
        Box(Modifier.fillMaxSize().onSizeChanged { box = it }) {
            val r = with(density) { managerRect(pos, you, places, box.width.toFloat(), PreviewSize.bar.toPx(), 26.dp.toPx(), 8.dp.toPx()) } ?: return@Box
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
        fun storeArgb(you: Boolean) {
            if (you) PrefEditor.ypipArgb = PrefEditor.withAlpha(EditorSettings.ypipColor.value, EditorSettings.ypipAlpha.value)
            else PrefEditor.iconArgb = PrefEditor.withAlpha(EditorSettings.iconColor.value, EditorSettings.iconAlpha.value)
        }

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

        /** Restores the defaults of the Videos page. */
        @JvmStatic
        fun resetVideos(context: Context) {
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
        }

        /** Restores the defaults of the YouTube page. */
        @JvmStatic
        fun resetYouTube(context: Context) {
            val color = MainConst.gradientColors[5]
            val pos = MainConst.presetPositions[5]
            if (ZoneSettings.youPos.value != 3) ZoneSettings.reset(context, listOf(ZoneSettings.youPos))
            if (EditorSettings.ypipAlpha.value != 0 || EditorSettings.ypipColor.value != color || EditorSettings.ypipPos.value.compareTo(pos) != 0) {
                EditorSettings.editLocal { set(EditorSettings.ypipAlpha, 0) }; EditorSettings.editLocal { set(EditorSettings.ypipColor, color) }; EditorSettings.editLocal { set(EditorSettings.ypipPos, pos) }; PrefEditor.ypipArgb = PrefEditor.withAlpha(color, 0)
                val p = PrefEditor.get(context)
                p!!.removeKey("mYpipAlpha"); p.removeKey("mYpipColor"); p.removeKey("mYpipPos"); p.save()
            }
            if (!ZoneSettings.youPip.value) ZoneSettings.reset(context, listOf(ZoneSettings.youPip))
            if (!ZoneSettings.youRate.value) ZoneSettings.reset(context, listOf(ZoneSettings.youRate))
            if (!ZtwoSettings.autoPlay.value) ZtwoSettings.reset(context, listOf(ZtwoSettings.autoPlay))
            if (!ZtwoSettings.youUnmute.value) ZtwoSettings.reset(context, listOf(ZtwoSettings.youUnmute))
        }
    }
}
