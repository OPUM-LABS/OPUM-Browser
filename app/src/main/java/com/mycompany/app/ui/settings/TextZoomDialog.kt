package com.mycompany.app.ui.settings

import com.mycompany.app.ui.components.AppSlider
import androidx.compose.foundation.background
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.ZoneSettings
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.ui.components.ConfirmDialog

/**
 * Text size of web pages and the floating "zoom in" button that switches to a larger size: the default size, the
 * button switch, its enlarged size, colour, transparency and position along the screen edge. Same preferences and
 * ranges as the old `DialogSeekWeb`; the old live web preview and the drag-to-place gesture are replaced by a text
 * sample and a position slider. Reset removes the stored values.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TextZoomDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var zoomOn by remember { mutableStateOf(ZtriSettings.zoomIcon.value) }
    var zoomSize by remember { mutableIntStateOf(ZtriSettings.zoomSize.value.takeIf { it in 50..500 } ?: 100) }
    var textSize by remember { mutableIntStateOf(ZoneSettings.textSize.value.takeIf { it in 50..500 } ?: 100) }
    var color by remember { mutableIntStateOf(EditorSettings.zoomColor.value) }
    var transparency by remember { mutableIntStateOf(EditorSettings.zoomAlpha.value.coerceIn(0, 90)) }
    var position by remember { mutableFloatStateOf(EditorSettings.zoomPos.value.coerceIn(0f, 1f)) }
    var resetting by remember { mutableStateOf(false) }

    fun storeButton() {
        if (EditorSettings.zoomAlpha.value == transparency && EditorSettings.zoomColor.value == color && EditorSettings.zoomPos.value.compareTo(position) == 0) return
        EditorSettings.editLocal { set(EditorSettings.zoomAlpha, transparency) }
        EditorSettings.editLocal { set(EditorSettings.zoomColor, color) }
        EditorSettings.editLocal { set(EditorSettings.zoomPos, position) }
        PrefEditor.zoomArgb = PrefEditor.withAlpha(color, transparency)
        PrefEditor.get(context)?.let {
            it.putInt(EditorSettings.zoomAlpha.value, "mZoomAlpha"); it.putInt(EditorSettings.zoomColor.value, "mZoomColor"); it.putFloat("mZoomPos", EditorSettings.zoomPos.value); it.saveAsync()
        }
    }

    fun write(store: Boolean) {
        if (ZtriSettings.zoomIcon.value != zoomOn || ZtriSettings.zoomSize.value != zoomSize) {
            ZtriSettings.editLocal { set(ZtriSettings.zoomIcon, zoomOn) }
            ZtriSettings.editLocal { set(ZtriSettings.zoomSize, zoomSize) }
            PrefZtri.get(context)?.let {
                if (store) { it.putBool("mZoomIcon", ZtriSettings.zoomIcon.value); it.putInt(ZtriSettings.zoomSize.value, "mZoomSize") }
                else { it.removeKey("mZoomIcon"); it.removeKey("mZoomSize") }
                it.saveAsync()
            }
        }
        if (ZoneSettings.textSize.value != textSize) {
            ZoneSettings.editLocal { set(ZoneSettings.textSize, textSize) }
            if (store) PrefSet.putInt(context, 15, textSize, "mTextSize") else PrefSet.remove(context, 15, "mTextSize")
        }
        storeButton()
    }

    com.mycompany.app.ui.components.OpumSheetDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.text_size)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "ABCDEabcde 1234567890",
                    fontSize = (16 * textSize / 100f).sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Text(stringResource(R.string.default_size) + "  " + textSize + "%", style = MaterialTheme.typography.labelLarge)
                AppSlider(value = textSize.toFloat(), onValueChange = { textSize = Math.round(it) }, valueRange = 50f..500f)
                Row(Modifier.fillMaxWidth().clickable { zoomOn = !zoomOn }, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.zoom_icon), Modifier.weight(1f))
                    Switch(checked = zoomOn, onCheckedChange = { zoomOn = it })
                }
                Text(
                    stringResource(R.string.zoom_size) + "  " + zoomSize + "%",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (zoomOn) Color.Unspecified else MaterialTheme.colorScheme.outline,
                )
                AppSlider(
                    value = zoomSize.toFloat(),
                    onValueChange = { zoomSize = Math.round(it) },
                    valueRange = 50f..500f,
                    enabled = zoomOn,
                )
                Text(stringResource(R.string.icon_color), style = MaterialTheme.typography.labelLarge)
                FlowRow(
                    Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MainConst.gradientColors.forEach { argb ->
                        Box(
                            Modifier
                                .size(36.dp)
                                .background(Color(argb).copy(alpha = (100 - transparency) / 100f), CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable(enabled = zoomOn) { color = argb },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (argb == color) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                        }
                    }
                }
                Text("${100 - transparency}%", style = MaterialTheme.typography.labelMedium)
                AppSlider(
                    value = (90 - transparency).toFloat(),
                    onValueChange = { transparency = 90 - Math.round(it) },
                    valueRange = 0f..90f,
                    enabled = zoomOn,
                )
                Text(stringResource(R.string.drag_move_guide), style = MaterialTheme.typography.bodySmall)
                AppSlider(value = position, onValueChange = { position = it }, valueRange = 0f..1f, enabled = zoomOn)
            }
        },
        confirmButton = { TextButton(onClick = { write(true); onDismiss() }) { Text(stringResource(R.string.apply)) } },
        dismissButton = { TextButton(onClick = { resetting = true }) { Text(stringResource(R.string.reset)) } },
    )

    if (resetting) {
        ConfirmDialog(
            message = stringResource(R.string.reset_setting),
            onConfirm = {
                zoomOn = false; zoomSize = 200; textSize = 100
                transparency = 0; color = MainConst.gradientColors[5]; position = MainConst.presetPositions[5]
                write(false)
                onDismiss()
            },
            onDismiss = { resetting = false },
        )
    }
}
