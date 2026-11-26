package com.mycompany.app.ui.settings

import com.mycompany.app.ui.components.AppSlider
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import android.content.Context
import com.mycompany.app.pref.store.ZoneSettings
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSaveConfirm
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.InfoDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.Sym

/**
 * Swipe zones (reworked 2026-11-19): a picture of the screen, to scale, with the five zones (top, bottom, left and right
 * edge, centre) in the colour of what a sideways swipe there does, labelled. Tap a zone to change its action (segmented:
 * back / forward, switch tab, Android back or off for the side edges, off) and its size (edges, per orientation; the
 * Portrait / Landscape switch picks which sizes are shown). Bounds as before: top / bottom at least a bar height, left /
 * right at least 16 dp, the centre keeps at least 128 dp. Edited on a copy, saved with the check; leaving asks.
 */
class SwipeSettingsActivity : OpumComposeActivity() {

    // indexes of `values`: 0..4 actions (top, bottom, left, right, centre); 5..8 portrait top, bottom, left, right;
    // 9..12 landscape top, bottom, left, right
    private fun stored() = listOf(
        ZoneSettings.gesTop.value, ZoneSettings.gesBot.value, ZoneSettings.gesLeft.value, ZoneSettings.gesRight.value, ZoneSettings.gesCenter.value,
        ZoneSettings.portAreaTop.value, ZoneSettings.portAreaBot.value, ZoneSettings.portAreaLeft.value, ZoneSettings.portAreaRight.value,
        ZoneSettings.landAreaTop.value, ZoneSettings.landAreaBot.value, ZoneSettings.landAreaLeft.value, ZoneSettings.landAreaRight.value,
    )

    @Composable
    override fun Content() {
        val context: Context = this
        val values = remember { mutableStateListOf<Int>().apply { addAll(stored()) } }
        var saved by remember { mutableStateOf(stored()) }
        var resetting by remember { mutableStateOf(false) }
        var zone by remember { mutableIntStateOf(-1) } // selected zone, -1 none
        var land by remember { mutableStateOf(false) }
        val changed = values.toList() != saved
        val metrics = resources.displayMetrics
        val shortSide = minOf(metrics.widthPixels, metrics.heightPixels)
        val longSide = maxOf(metrics.widthPixels, metrics.heightPixels)
        val minCenter = MainApp.dp64 shl 1

        fun write(save: Boolean) {
            ZoneSettings.editLocal { set(ZoneSettings.gesTop, values[0]) }; ZoneSettings.editLocal { set(ZoneSettings.gesBot, values[1]) }; ZoneSettings.editLocal { set(ZoneSettings.gesLeft, values[2]) }; ZoneSettings.editLocal { set(ZoneSettings.gesRight, values[3]) }; ZoneSettings.editLocal { set(ZoneSettings.gesCenter, values[4]) }
            ZoneSettings.editLocal { set(ZoneSettings.portAreaTop, values[5]) }; ZoneSettings.editLocal { set(ZoneSettings.portAreaBot, values[6]) }; ZoneSettings.editLocal { set(ZoneSettings.portAreaLeft, values[7]) }; ZoneSettings.editLocal { set(ZoneSettings.portAreaRight, values[8]) }
            ZoneSettings.editLocal { set(ZoneSettings.landAreaTop, values[9]) }; ZoneSettings.editLocal { set(ZoneSettings.landAreaBot, values[10]) }; ZoneSettings.editLocal { set(ZoneSettings.landAreaLeft, values[11]) }; ZoneSettings.editLocal { set(ZoneSettings.landAreaRight, values[12]) }
            val prefs = PrefZone.get(context, false)!!
            val keys = listOf(
                "mGesTop" to ZoneSettings.gesTop.value, "mGesBot" to ZoneSettings.gesBot.value, "mGesLeft" to ZoneSettings.gesLeft.value, "mGesRight" to ZoneSettings.gesRight.value,
                "mGesCenter" to ZoneSettings.gesCenter.value, "mPortAreaTop" to ZoneSettings.portAreaTop.value, "mPortAreaBot" to ZoneSettings.portAreaBot.value,
                "mPortAreaLeft" to ZoneSettings.portAreaLeft.value, "mPortAreaRight" to ZoneSettings.portAreaRight.value, "mLandAreaTop" to ZoneSettings.landAreaTop.value,
                "mLandAreaBot" to ZoneSettings.landAreaBot.value, "mLandAreaLeft" to ZoneSettings.landAreaLeft.value, "mLandAreaRight" to ZoneSettings.landAreaRight.value,
            )
            for ((key, value) in keys) if (save) prefs.putInt(value, key) else prefs.removeKey(key)
            prefs.saveAsync()
            saved = values.toList()
        }

        fun leave() {
            if (!changed) { finish(); return }
            DialogSaveConfirm(this, object : DialogSeekListener {
                override fun onValue(newValue: Int) {
                    if (newValue == 0) write(true)
                    finish()
                }
            })
        }

        BackHandler { if (zone >= 0) zone = -1 else leave() }

        val zoneNames = listOf(R.string.zone_top, R.string.zone_bottom, R.string.zone_left, R.string.zone_right, R.string.show_center)
        // the actions a zone offers, in segment order: (stored value, label)
        // stored value 2 is "leave it to Android" on the side edges (its back gesture) and "off" elsewhere
        fun actionsFor(zone: Int) = listOf(0 to R.string.act_page, 1 to R.string.act_tab, 2 to (if (zone == 2 || zone == 3) R.string.act_system else R.string.not_used))
        fun shown(zone: Int) = values[zone].coerceIn(0, 2)
        fun setAction(zone: Int, action: Int) { values[zone] = action }

        SettingsScaffold(
            title = stringResource(R.string.swipe),
            onBack = { leave() },
            onReset = { resetting = true },
            cards = false,
            actions = {
                if (changed) IconButton(onClick = { write(true); finish() }) { Icon(Sym.Check, contentDescription = stringResource(R.string.ok)) }
            },
        ) {
            item {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    listOf(false to R.string.view_port, true to R.string.view_land).forEachIndexed { i, (value, label) ->
                        SegmentedButton(
                            selected = land == value, onClick = { land = value },
                            shape = SegmentedButtonDefaults.itemShape(i, 2), icon = {}, label = { Text(stringResource(label)) },
                        )
                    }
                }
            }
            item {
                ZonePreview(
                    land = land, shortSide = shortSide, longSide = longSide,
                    sizes = if (land) values.subList(9, 13).toList() else values.subList(5, 9).toList(),
                    actions = (0..4).map { shown(it) }, selected = zone,
                    labelFor = { z -> stringResource(actionsFor(z).first { it.first == shown(z) }.second) },
                    nameFor = { z -> stringResource(zoneNames[z]) },
                    onTap = { z -> zone = if (zone == z) -1 else z },
                )
            }
            item {
                val z = zone
                if (z < 0) {
                    Column {
                        Text(
                            stringResource(R.string.swipe_hint), style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                        )
                        // a key: each zone, its colour and action; tap to edit
                        val keyColors = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.surfaceContainerHighest)
                        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                            Column(Modifier.padding(vertical = 6.dp)) {
                                for (k in intArrayOf(0, 2, 4, 3, 1)) {
                                    Row(Modifier.fillMaxWidth().clickable { zone = k }.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(18.dp).background(keyColors[shown(k)], RoundedCornerShape(5.dp)).border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(5.dp)))
                                        Text(stringResource(zoneNames[k]), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 14.dp).weight(1f))
                                        Text(stringResource(actionsFor(k).first { it.first == shown(k) }.second), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            // a close button; back and tapping the zone again close it too
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(stringResource(zoneNames[z]), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                IconButton(onClick = { zone = -1 }, modifier = Modifier.size(36.dp)) {
                                    Icon(Sym.Close, contentDescription = stringResource(R.string.close))
                                }
                            }
                            val options = actionsFor(z)
                            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(top = 10.dp)) {
                                options.forEachIndexed { i, (value, label) ->
                                    SegmentedButton(
                                        selected = shown(z) == value, onClick = { setAction(z, value) },
                                        shape = SegmentedButtonDefaults.itemShape(i, options.size), icon = {},
                                        label = { Text(stringResource(label), maxLines = 2, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium) },
                                    )
                                }
                            }
                            if (z < 4) {
                                val vertical = z < 2
                                val base = if (land) 9 else 5
                                val index = base + z
                                val other = base + (if (z % 2 == 0) z + 1 else z - 1)
                                val total = if (vertical) (if (land) shortSide else longSide) else (if (land) longSide else shortSide)
                                val min = if (vertical) MainApp.dp48 else MainApp.dp16
                                val max = (total - values[other] - minCenter).coerceAtLeast(min)
                                Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(stringResource(if (vertical) R.string.size_height else R.string.zone_width), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(72.dp))
                                    AppSlider(
                                        value = values[index].toFloat().coerceIn(min.toFloat(), max.toFloat()),
                                        onValueChange = { values[index] = it.toInt().coerceIn(min, max) },
                                        valueRange = min.toFloat()..max.toFloat(), modifier = Modifier.weight(1f),
                                    )
                                    Text(
                                        "" + (values[index] / metrics.density).toInt() + " dp", style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(60.dp).padding(start = 6.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    val gestureDefault = 2
                    val area = MainApp.dp48
                    listOf(
                        1, 1, gestureDefault, gestureDefault, 2,
                        area, area, MainApp.dp24, MainApp.dp24,
                        area, area, MainApp.dp40, MainApp.dp40,
                    ).forEachIndexed { newValue, v -> values[newValue] = v }
                    write(false) // defaults are "no stored value"
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /**
     * The screen to scale with the five zones, each in the colour of its action and labelled; tap one to select it.
     * [sizes]: top, bottom, left, right in px for the shown orientation; [actions]: segment values per zone.
     */
    @Composable
    private fun ZonePreview(
        land: Boolean, shortSide: Int, longSide: Int, sizes: List<Int>, actions: List<Int>, selected: Int,
        labelFor: @Composable (Int) -> String, nameFor: @Composable (Int) -> String, onTap: (Int) -> Unit,
    ) {
        val colors = listOf(
            MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.surfaceContainerHighest,
        )
        BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
            val wPx = if (land) longSide else shortSide
            val hPx = if (land) shortSide else longSide
            // fit the phone into the width, at most 340 dp tall
            val scale = minOf(maxWidth.value / wPx, 340f / hPx)
            fun d(px: Int) = (px * scale).dp
            // edges drawn at least readable (to scale they are only a few dp); the real size is shown in the panel
            fun edge(px: Int, min: Float) = maxOf((px * scale), min).dp
            Surface(
                shape = RoundedCornerShape(18.dp), border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.size(d(wPx), d(hPx)),
            ) {
                Column(Modifier.fillMaxSize()) {
                    ZoneBox(0, Modifier.fillMaxWidth().height(edge(sizes[0], 26f)), colors[actions[0]], selected, labelFor, nameFor, onTap)
                    Row(Modifier.weight(1f).fillMaxWidth()) {
                        ZoneBox(2, Modifier.width(edge(sizes[2], 18f)).fillMaxHeight(), colors[actions[2]], selected, labelFor, nameFor, onTap, narrow = true)
                        ZoneBox(4, Modifier.weight(1f).fillMaxHeight(), colors[actions[4]], selected, labelFor, nameFor, onTap)
                        ZoneBox(3, Modifier.width(edge(sizes[3], 18f)).fillMaxHeight(), colors[actions[3]], selected, labelFor, nameFor, onTap, narrow = true)
                    }
                    ZoneBox(1, Modifier.fillMaxWidth().height(edge(sizes[1], 26f)), colors[actions[1]], selected, labelFor, nameFor, onTap)
                }
            }
        }
    }

    @Composable
    private fun ZoneBox(
        zone: Int, modifier: Modifier, color: Color, selected: Int,
        labelFor: @Composable (Int) -> String, nameFor: @Composable (Int) -> String, onTap: (Int) -> Unit, narrow: Boolean = false,
    ) {
        Box(
            modifier.background(color)
                .border(if (selected == zone) 2.dp else 0.5.dp, if (selected == zone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                .clickable { onTap(zone) },
            contentAlignment = Alignment.Center,
        ) {
            if (narrow) {
                // a thin side edge: its action written along the edge
                Text(
                    labelFor(zone), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, softWrap = false,
                    modifier = Modifier.rotateVertical(if (zone == 2) -90f else 90f),
                )
            } else if (zone == 4) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(nameFor(zone), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                    Text(labelFor(zone), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                }
            } else {
                Text(
                    nameFor(zone) + " · " + labelFor(zone), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Turns content by [degrees] (±90) and swaps its measured width and height, for text running along a side edge. */
private fun Modifier.rotateVertical(degrees: Float): Modifier = this
    .layout { measurable, constraints ->
        val placeable = measurable.measure(androidx.compose.ui.unit.Constraints())
        layout(placeable.height, placeable.width) { placeable.place(-(placeable.width - placeable.height) / 2, (placeable.width - placeable.height) / 2) }
    }
    .graphicsLayer { rotationZ = degrees }
