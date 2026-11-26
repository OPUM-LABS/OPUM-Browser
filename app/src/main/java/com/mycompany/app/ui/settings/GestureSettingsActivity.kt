package com.mycompany.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import android.content.Context
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import android.content.Intent
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.prefs.PrefState
import com.mycompany.app.ui.prefs.rememberPref

/** Settings > Gesture: the four diagonal swipe actions and the two-finger tab gesture. */
class GestureSettingsActivity : OpumComposeActivity() {
    private class Diagonal(val titleRes1: Int, val titleRes2: Int, val iconRes: Int, val key: String, val read: () -> Int, val write: (Int) -> Unit)

    private val diagonals = listOf(
        Diagonal(R.string.move_up, R.string.move_left, R.drawable.outline_subdirectory_arrow_up_left_black_24, "mGesUpLt3", { ZoneSettings.gesUpLeft.value }) { ZoneSettings.editLocal { set(ZoneSettings.gesUpLeft, it) } },
        Diagonal(R.string.move_up, R.string.move_right, R.drawable.outline_subdirectory_arrow_up_right_black_24, "mGesUpRt3", { ZoneSettings.gesUpRight.value }) { ZoneSettings.editLocal { set(ZoneSettings.gesUpRight, it) } },
        Diagonal(R.string.move_down, R.string.move_left, R.drawable.outline_subdirectory_arrow_left_black_24, "mGesDnLt3", { ZoneSettings.gesDownLeft.value }) { ZoneSettings.editLocal { set(ZoneSettings.gesDownLeft, it) } },
        Diagonal(R.string.move_down, R.string.move_right, R.drawable.outline_subdirectory_arrow_right_black_24, "mGesDnRt3", { ZoneSettings.gesDownRight.value }) { ZoneSettings.editLocal { set(ZoneSettings.gesDownRight, it) } },
    )

    @Composable
    override fun Content() {
        val context: Context = this
        val states: List<PrefState<Int>> = diagonals.map { d ->
            rememberPref(d.read) { value -> d.write(value); PrefSet.putInt(context, 15, value, d.key) }
        }
        val multiTab = rememberPref({ BrowserSettings.tabMulti.value }) { BrowserSettings.edit(context) { set(BrowserSettings.tabMulti, it) } }
        var pickerFor by remember { mutableStateOf<Int?>(null) }
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.gesture),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsDivider()
            item {
                SettingRow(
                    title = stringResource(R.string.swipe),
                    iconRes = R.drawable.outline_arrow_range_black_24,
                    onClick = { startActivity(Intent(context, com.mycompany.app.ui.settings.SwipeSettingsActivity::class.java)) },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.swipe_sense),
                    onClick = { startActivity(Intent(context, com.mycompany.app.ui.settings.SenseSettingsActivity::class.java)) },
                )
            }
            settingsHeader(getString(R.string.hd_diagonal))
            // what each diagonal swipe does, at a glance
            item { DiagonalPreview(states.map { it.value }) }
            diagonals.forEachIndexed { index, d ->
                item {
                    // on: pick an action (closing the picker without one leaves it off); off: not used
                    val used = states[index].value != 0
                    SettingRow(
                        title = stringResource(d.titleRes1) + " + " + stringResource(d.titleRes2),
                        summary = stringResource(if (used) MainUtil.menuIconAt(states[index].value) else R.string.not_used),
                        iconRes = d.iconRes,
                        onClick = { pickerFor = index },
                        trailing = {
                            Switch(checked = used, onCheckedChange = { on -> if (on) pickerFor = index else states[index].set(0) })
                        },
                    )
                }
            }
            settingsHeader(getString(R.string.hd_two_finger))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.tab_gesture) + " " + stringResource(R.string.two_finger),
                    checked = multiTab.value,
                    onCheckedChange = multiTab::set,
                )
            }
            settingsFooter(getString(R.string.two_finger_info_1) + "\n" + getString(R.string.two_finger_info_2))
        }

        pickerFor?.let { index ->
            val d = diagonals[index]
            // closing the picker without a choice leaves the gesture as it was (off stays off)
            com.mycompany.app.ui.components.OpumSheetBox(onDismiss = { pickerFor = null }) {
                androidx.compose.foundation.layout.Box {
                    Column(Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                        Text(
                            stringResource(d.titleRes1) + " + " + stringResource(d.titleRes2), style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                        com.mycompany.app.ui.browser.ActionCatalog(
                            onPick = { id -> if (id in 1 until com.mycompany.app.main.MainConst.mainMenuNames.size) states[index].set(id); pickerFor = null },
                            chosen = setOf(states[index].value), maxHeight = 460.dp,
                        )
                        TextButton(onClick = { pickerFor = null }, modifier = Modifier.align(Alignment.End).padding(end = 12.dp)) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                }
            }
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    states.forEachIndexed { position, s -> s.reload(diagonals[position].read()) }
                    multiTab.reload(BrowserSettings.tabMulti.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /**
     * A small page with the four diagonal swipes: in each corner the direction and the action it runs (or "Not used"),
     * so the four settings read as one picture.
     */
    @Composable
    private fun DiagonalPreview(actions: List<Int>) {
        val dark = com.mycompany.app.ui.state.AppState.darkUi
        Surface(
            shape = RoundedCornerShape(18.dp), color = if (dark) Color(0xFF101114) else Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).height(190.dp),
        ) {
            Column(Modifier.padding(10.dp)) {
                for (row in 0..1) {
                    Row(Modifier.weight(1f).fillMaxWidth()) {
                        for (col in 0..1) {
                            val i = row * 2 + col
                            val id = actions[i]
                            Column(
                                Modifier.weight(1f).fillMaxHeight().padding(4.dp)
                                    .background(if (id != 0) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else Color.Transparent, RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                horizontalAlignment = if (col == 0) Alignment.Start else Alignment.End,
                                verticalArrangement = if (row == 0) Arrangement.Top else Arrangement.Bottom,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(painterResource(diagonals[i].iconRes), null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                    if (id != 0) Box(Modifier.padding(start = 6.dp)) { com.mycompany.app.ui.browser.MenuActionIcon(id, 20.dp) }
                                }
                                Text(
                                    stringResource(if (id != 0) com.mycompany.app.ui.browser.MenuCatalog.labelRes(id) else R.string.not_used),
                                    style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    color = if (id != 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /** Restores the defaults of the gesture pages (swipe areas, sensitivity, diagonal actions, tab gesture). */
    private fun resetToDefaults(context: Context) {
        val gestureDefault = 2
        val area = MainApp.dp48
        ZoneSettings.editLocal { set(ZoneSettings.gesTop, 1) }; ZoneSettings.editLocal { set(ZoneSettings.gesBot, 1) }
        ZoneSettings.editLocal { set(ZoneSettings.gesLeft, gestureDefault) }; ZoneSettings.editLocal { set(ZoneSettings.gesRight, gestureDefault) }; ZoneSettings.editLocal { set(ZoneSettings.gesCenter, 2) }
        ZoneSettings.editLocal { set(ZoneSettings.portAreaTop, area) }; ZoneSettings.editLocal { set(ZoneSettings.portAreaBot, area) }
        ZoneSettings.editLocal { set(ZoneSettings.portAreaLeft, MainApp.dp24) }; ZoneSettings.editLocal { set(ZoneSettings.portAreaRight, MainApp.dp24) }
        ZoneSettings.editLocal { set(ZoneSettings.landAreaTop, area) }; ZoneSettings.editLocal { set(ZoneSettings.landAreaBot, area) }
        ZoneSettings.editLocal { set(ZoneSettings.landAreaLeft, MainApp.dp40) }; ZoneSettings.editLocal { set(ZoneSettings.landAreaRight, MainApp.dp40) }
        val zone = PrefZone.get(context, false)!!
        for (key in listOf(
            "mGesTop", "mGesBot", "mGesLeft", "mGesRight", "mGesCenter",
            "mPortAreaTop", "mPortAreaBot", "mPortAreaLeft", "mPortAreaRight",
            "mLandAreaTop", "mLandAreaBot", "mLandAreaLeft", "mLandAreaRight",
        )) zone.removeKey(key)
        zone.save()
        ZtwoSettings.editLocal { set(ZtwoSettings.senseTop, 100) }; ZtwoSettings.editLocal { set(ZtwoSettings.senseBot, 100) }; ZtwoSettings.editLocal { set(ZtwoSettings.senseLeft, 100) }; ZtwoSettings.editLocal { set(ZtwoSettings.senseRight, 100) }; ZtwoSettings.editLocal { set(ZtwoSettings.senseCenter, 100) }
        val two = PrefZtwo.get(context, false)!!
        for (key in listOf("mSenseTop2", "mSenseBot2", "mSenseLeft2", "mSenseRight2", "mSenseCenter2")) two.removeKey(key)
        two.save()
        diagonals.forEach { d -> d.write(0); PrefSet.removeNow(context, 15, d.key) }
        BrowserSettings.editLocal { set(BrowserSettings.tabMulti, false) }
        PrefSet.removeNow(context, 14, "mTabMulti")
    }
}
