package com.mycompany.app.ui.settings

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingSliderRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.prefs.rememberPref
import com.mycompany.app.ui.state.AppState
import kotlin.math.roundToInt

/**
 * Settings › Toolbars & controls › Back to top button (2026-11-25, was a dialog): where the button sits (not shown /
 * left / centre / right, [ZoneSettings.showUpPos]) and its transparency (0-90 %, [EditorSettings.upAlpha]). The preview
 * shows it on a page; it can also be long-pressed and dragged to a side there. Changes apply at once.
 */
class BackToTopSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val place = rememberPref({ ZoneSettings.showUpPos.value }) { ZoneSettings.edit(context) { set(ZoneSettings.showUpPos, it) } }
        val alpha = rememberPref({ EditorSettings.upAlpha.value.let { if (it < 0 || it > 90) 25 else it } }) {
            EditorSettings.edit(context) { set(EditorSettings.upAlpha, it) }
        }
        // the slider's value while it is dragged, so the preview follows the finger
        var liveAlpha by remember { mutableIntStateOf(alpha.value) }
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.show_up),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            item { Preview(place.value, liveAlpha) { place.set(it) } }
            settingsFooter(getString(R.string.long_move_guide))
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.location),
                    options = MainConst.colourKinds.map { stringResource(MainConst.scrollPosNames[it]) },
                    selectedIndex = MainConst.colourKinds.indexOf(place.value.coerceIn(0, 3)),
                    onSelect = { place.set(MainConst.colourKinds[it]) },
                )
            }
            item {
                SettingSliderRow(
                    title = stringResource(R.string.color_alpha),
                    value = alpha.value,
                    range = 0..90,
                    onValueChange = { alpha.set(it); liveAlpha = it },
                    label = { "$it%" },
                    // the preview follows the slider while it is dragged
                    below = { v -> androidx.compose.runtime.LaunchedEffect(v) { liveAlpha = v } },
                    enabled = place.value != 0,
                )
            }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    ZoneSettings.reset(context, listOf(ZoneSettings.showUpPos))
                    EditorSettings.reset(context, listOf(EditorSettings.upAlpha))
                    place.reload(ZoneSettings.showUpPos.value); alpha.reload(EditorSettings.upAlpha.value); liveAlpha = alpha.value
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /** A page with the button on it at [place]; long-press and drag it to the left, centre or right third. */
    @Composable
    private fun Preview(place: Int, alpha: Int, onPlace: (Int) -> Unit) {
        val density = LocalDensity.current
        val buttonSize = 48.dp
        val dark = AppState.darkUi
        var width by remember { mutableIntStateOf(0) }
        var height by remember { mutableIntStateOf(0) }
        var dragX by remember { mutableStateOf<Float?>(null) }
        Box(
            Modifier.padding(16.dp).fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .onSizeChanged { width = it.width; height = it.height },
        ) {
            Image(painterResource(R.drawable.preview_matterhorn), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            val buttonPx = with(density) { buttonSize.toPx() }
            val edge = with(density) { 8.dp.toPx() }
            val restX = when (place) {
                1 -> edge
                3 -> width - buttonPx - edge
                else -> (width - buttonPx) / 2f
            }
            val shownX = dragX ?: restX
            if (place != 0 || dragX != null) {
                Box(
                    Modifier
                        .offset { IntOffset(shownX.roundToInt(), ((height - buttonPx) / 2f).roundToInt()) }
                        .size(buttonSize)
                        .background(Color(PrefEditor.withAlpha(if (dark) -16777216 else -1, alpha)), CircleShape)
                        .border(1.dp, Color(-12632257), CircleShape)
                        .pointerInput(width, place) {
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture
                                var x = restX
                                dragX = x
                                var last = down.position.x
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) break
                                    // the button moves with the finger: the position is relative to where it was grabbed
                                    x = (x + (change.position.x - last)).coerceIn(0f, (width - buttonPx).coerceAtLeast(0f))
                                    last = change.position.x
                                    dragX = x
                                    change.consume()
                                }
                                val centre = x + buttonPx / 2f
                                val third = width / 3f
                                onPlace(if (centre < third) 1 else if (centre < third * 2f) 2 else 3)
                                dragX = null
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(if (dark) R.drawable.outline_arrow_upward_dark_24 else R.drawable.outline_arrow_upward_black_24),
                        contentDescription = null, modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}
