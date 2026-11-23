package com.mycompany.app.dialog

import com.mycompany.app.ui.components.AppSlider
import androidx.compose.foundation.Image
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.ZoneSettings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.ui.browser.ComposeBottomDialog
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * The "scroll to top" button's place (not shown / left / centre / right, [ZoneSettings.showUpPos.value]) and transparency (0-90 %,
 * [EditorSettings.upAlpha.value]). The button is drawn on a preview picture: long-press it and drag it to the left, centre or right.
 * Apply stores both, Reset (after asking) goes back to centre / 25 % and forgets the stored values. Compose now
 * (on [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogEditUp(mainActivity: android.app.Activity?) : ComposeBottomDialog(mainActivity) {
    private var place by mutableIntStateOf(ZoneSettings.showUpPos.value)
    private var alpha by mutableIntStateOf(EditorSettings.upAlpha.value.let { if (it < 0 || it > 90) 25 else it })
    private var resetAsk by mutableStateOf(false)
    private var menu by mutableStateOf(false)

    private fun colour(): Int = PrefEditor.withAlpha(if (AppState.darkUi) -16777216 else -1, alpha)

    init {
        setSheet {
            val density = LocalDensity.current
            Column(Modifier.verticalScroll(rememberScrollState())) {
                PreviewPicture(density)
                Box(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().clickable { menu = true }.padding(horizontal = 24.dp, vertical = 16.dp)) {
                        Text(stringResource(R.string.location), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(MainConst.scrollPosNames[place.coerceIn(0, 3)]), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 4.dp))
                        Text(stringResource(R.string.long_move_guide), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                    }
                    com.mycompany.app.ui.components.BubbleMenu(expanded = menu, onDismiss = { menu = false }) {
                        for (kind in MainConst.colourKinds) {
                            DropdownMenuItem(
                                text = { Text(stringResource(MainConst.scrollPosNames[kind])) },
                                onClick = { menu = false; place = kind },
                            )
                        }
                    }
                }
                HorizontalDivider()
                Row(
                    Modifier.fillMaxWidth().padding(top = 16.dp, start = 24.dp, end = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(R.string.color_alpha), style = MaterialTheme.typography.titleMedium)
                    Text("$alpha%", style = MaterialTheme.typography.titleMedium)
                }
                Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { alpha = (alpha - 1).coerceIn(0, 90) }) { Icon(painterResource(R.drawable.outline_remove_black_24), contentDescription = null) }
                    AppSlider(
                        value = alpha.toFloat(), onValueChange = { alpha = it.roundToInt().coerceIn(0, 90) },
                        valueRange = 0f..90f, modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { alpha = (alpha + 1).coerceIn(0, 90) }) { Icon(painterResource(R.drawable.outline_add_black_24), contentDescription = null) }
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { resetAsk = true }) { Text(stringResource(R.string.reset)) }
                    TextButton(onClick = { save(true) }) { Text(stringResource(R.string.apply)) }
                }
                if (resetAsk) {
                    AlertDialog(
                        onDismissRequest = { resetAsk = false },
                        text = { Text(stringResource(R.string.reset_setting)) },
                        confirmButton = {
                            TextButton(colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = androidx.compose.material3.MaterialTheme.colorScheme.error), onClick = {
                                resetAsk = false
                                place = 2
                                alpha = 25
                                save(false)
                            }) { Text(stringResource(R.string.reset)) }
                        },
                        dismissButton = { TextButton(onClick = { resetAsk = false }) { Text(stringResource(R.string.cancel)) } },
                    )
                }
            }
        }
        show()
    }

    @androidx.compose.runtime.Composable
    private fun PreviewPicture(density: androidx.compose.ui.unit.Density) {
        val buttonSize = 48.dp
        var width by remember { mutableIntStateOf(0) }
        var height by remember { mutableIntStateOf(0) }
        var dragX by remember { mutableStateOf<Float?>(null) }
        Box(Modifier.fillMaxWidth().onSizeChanged { width = it.width; height = it.height }) {
            Image(painterResource(R.drawable.preview_matterhorn), contentDescription = null, contentScale = ContentScale.FillWidth, modifier = Modifier.fillMaxWidth())
            val buttonPx = with(density) { buttonSize.toPx() }
            val edge = with(density) { MainApp.dp4.toDp().toPx() }
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
                        .background(Color(colour()), CircleShape)
                        .border(1.dp, Color(-12632257), CircleShape)
                        .pointerInput(width, place) {
                            awaitEachGesture {
                                val down = awaitFirstDown()
                                val long = awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture
                                var restXNow = restX
                                dragX = restXNow
                                var last = down.position.x
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                    if (!change.pressed) break
                                    restXNow = (restXNow + (change.position.x - last)).coerceIn(0f, (width - buttonPx).coerceAtLeast(0f))
                                    // the button moves with the finger: the position is relative to where it was grabbed
                                    last = change.position.x
                                    dragX = restXNow
                                    change.consume()
                                }
                                val centre = restXNow + buttonPx / 2f
                                val third = width / 3f
                                place = if (centre < third) 1 else if (centre < third * 2f) 2 else 3
                                dragX = null
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painterResource(if (AppState.darkUi) R.drawable.outline_arrow_upward_dark_24 else R.drawable.outline_arrow_upward_black_24),
                        contentDescription = null, modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }

    /** Stores ([persist]) or forgets the stored values (reset); persisting also closes the dialog. */
    private fun save(persist: Boolean) {
        if (ZoneSettings.showUpPos.value != place) {
            ZoneSettings.editLocal { set(ZoneSettings.showUpPos, place) }
            if (persist) PrefSet.putInt(context, 15, place, "mShowUpPos") else PrefSet.remove(context, 15, "mShowUpPos")
        }
        if (EditorSettings.upAlpha.value != alpha) {
            EditorSettings.editLocal { set(EditorSettings.upAlpha, alpha) }
            if (persist) PrefSet.putInt(context, 1, alpha, "mUpAlpha") else PrefSet.remove(context, 1, "mUpAlpha")
        }
        if (persist) dismiss()
    }

    companion object {
    }
}
