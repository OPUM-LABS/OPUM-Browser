package com.mycompany.app.ui.settings

import com.mycompany.app.ui.components.AppSlider
import androidx.compose.foundation.background
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
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
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.components.ConfirmDialog

/**
 * Tab bar details: outline (accent line) and its colour, where the add button sits, the close button, and the tab
 * width and height as percent of the default. Same preferences and ranges as the old `DialogSetTabDetail`; Reset
 * removes the stored values, Apply stores them. (The old live preview of the tab bar is not recreated.)
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TabDetailDialog(onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var accent by remember { mutableStateOf(BrowserSettings.tabAccent.value) }
    var color by remember { mutableIntStateOf(if (BrowserSettings.tabColor.value in MainConst.tabColors.indices) BrowserSettings.tabColor.value else 5) }
    var addIcon by remember { mutableIntStateOf(PdfSettings.tabAdd.value) }
    var closeIcon by remember { mutableStateOf(PdfSettings.tabClose.value) }
    var width by remember { mutableFloatStateOf(Math.round(PdfSettings.tabWidth.value * 100.0f / MainApp.dp144).coerceIn(50, 200).toFloat()) }
    var height by remember { mutableFloatStateOf(Math.round(PdfSettings.tabHeight.value * 100.0f / MainApp.dp40).coerceIn(50, 200).toFloat()) }
    var resetting by remember { mutableStateOf(false) }

    fun write(store: Boolean) {
        val widthPx = Math.round(width.toInt() * MainApp.dp144 / 100.0f).coerceIn(Math.round(MainApp.dp144 / 2.0f), MainApp.dp144 shl 1)
        val heightPx = Math.round(height.toInt() * MainApp.dp40 / 100.0f).coerceIn(Math.round(MainApp.dp40 / 2.0f), MainApp.dp40 shl 1)
        BrowserSettings.editLocal { set(BrowserSettings.tabAccent, accent) }
        BrowserSettings.editLocal { set(BrowserSettings.tabColor, color) }
        PrefWeb.get(context, false)?.let {
            if (store) { it.putBool("mTabAccent", BrowserSettings.tabAccent.value); it.putInt(BrowserSettings.tabColor.value, "mTabColor") }
            else { it.removeKey("mTabAccent"); it.removeKey("mTabColor") }
            it.saveAsync()
        }
        PdfSettings.editLocal { set(PdfSettings.tabAdd, addIcon) }
        PdfSettings.editLocal { set(PdfSettings.tabClose, closeIcon) }
        PdfSettings.editLocal { set(PdfSettings.tabWidth, widthPx) }
        PdfSettings.editLocal { set(PdfSettings.tabHeight, heightPx) }
        PrefPdf.get(context, false)?.let {
            if (store) {
                it.putInt(PdfSettings.tabAdd.value, "mTabAdd"); it.putBool("mTabClose", PdfSettings.tabClose.value)
                it.putInt(PdfSettings.tabWidth.value, "mTabWidth"); it.putInt(PdfSettings.tabHeight.value, "mTabHeight")
            } else {
                it.removeKey("mTabAdd"); it.removeKey("mTabClose"); it.removeKey("mTabWidth"); it.removeKey("mTabHeight")
            }
            it.saveAsync()
        }
    }

    com.mycompany.app.ui.components.OpumSheetDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.detail_setting)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth().clickable { accent = !accent }, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.sub_line), Modifier.weight(1f))
                    Switch(checked = accent, onCheckedChange = { accent = it })
                }
                FlowRow(
                    Modifier.padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MainConst.tabColors.forEachIndexed { index, argb ->
                        Box(
                            Modifier
                                .size(36.dp)
                                .background(Color(argb), CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .clickable { color = index },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (index == color) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }
                Text(stringResource(R.string.add_icon), style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MainConst.scrollPosValues.forEach { id ->
                        FilterChip(
                            selected = addIcon == id,
                            onClick = { addIcon = id },
                            label = { Text(stringResource(MainConst.scrollPosNames[id])) },
                        )
                    }
                }
                Row(
                    Modifier.fillMaxWidth().padding(top = 8.dp).clickable { closeIcon = !closeIcon },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.close_icon), Modifier.weight(1f))
                    Switch(checked = closeIcon, onCheckedChange = { closeIcon = it })
                }
                Text(
                    stringResource(R.string.size_width) + "  " + width.toInt() + "%",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp),
                )
                AppSlider(value = width, onValueChange = { width = Math.round(it).toFloat() }, valueRange = 50f..200f)
                Text(
                    stringResource(R.string.size_height) + "  " + height.toInt() + "%",
                    style = MaterialTheme.typography.labelLarge,
                )
                AppSlider(value = height, onValueChange = { height = Math.round(it).toFloat() }, valueRange = 50f..200f)
            }
        },
        confirmButton = {
            TextButton(onClick = { write(true); onDismiss() }) { Text(stringResource(R.string.apply)) }
        },
        dismissButton = {
            TextButton(onClick = { resetting = true }) { Text(stringResource(R.string.reset)) }
        },
    )

    if (resetting) {
        ConfirmDialog(
            message = stringResource(R.string.reset_setting),
            onConfirm = {
                accent = true; color = 5; addIcon = 3; closeIcon = true; width = 100f; height = 100f
                write(false)
                onDismiss()
            },
            onDismiss = { resetting = false },
        )
    }
}
