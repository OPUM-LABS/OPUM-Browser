package com.mycompany.app.ui.settings

import android.content.Context
import com.mycompany.app.pref.store.FloatSettings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogEditIcon
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefFloat
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.rememberPref

/**
 * The three floating buttons: the action of each, its icon colour (legacy [DialogEditIcon] through
 * `DialogShow.direct`) and whether it is always visible.
 */
class FloatSettingsActivity : OpumComposeActivity() {

    private fun action(n: Int) = when (n) { 1 -> FloatSettings.flt1Icon.value; 2 -> FloatSettings.flt2Icon.value; else -> FloatSettings.flt3Icon.value }
    private fun color(n: Int) = when (n) {
        1 -> PrefEditor.withAlpha(FloatSettings.flt1Color.value, FloatSettings.flt1Alpha.value)
        2 -> PrefEditor.withAlpha(FloatSettings.flt2Color.value, FloatSettings.flt2Alpha.value)
        else -> PrefEditor.withAlpha(FloatSettings.flt3Color.value, FloatSettings.flt3Alpha.value)
    }

    @Composable
    override fun Content() {
        val context: Context = this
        val actions = listOf(
            rememberPref({ FloatSettings.flt1Icon.value }) { FloatSettings.edit(context) { set(FloatSettings.flt1Icon, it) } },
            rememberPref({ FloatSettings.flt2Icon.value }) { FloatSettings.edit(context) { set(FloatSettings.flt2Icon, it) } },
            rememberPref({ FloatSettings.flt3Icon.value }) { FloatSettings.edit(context) { set(FloatSettings.flt3Icon, it) } },
        )
        val shows = listOf(
            rememberPref({ FloatSettings.flt1Show.value }) { FloatSettings.edit(context) { set(FloatSettings.flt1Show, it) } },
            rememberPref({ FloatSettings.flt2Show.value }) { FloatSettings.edit(context) { set(FloatSettings.flt2Show, it) } },
            rememberPref({ FloatSettings.flt3Show.value }) { FloatSettings.edit(context) { set(FloatSettings.flt3Show, it) } },
        )
        val colors = remember { listOf(mutableIntStateOf(color(1)), mutableIntStateOf(color(2)), mutableIntStateOf(color(3))) }
        var pickerFor by remember { mutableIntStateOf(0) } // button number whose action picker is open
        var resetting by remember { mutableStateOf(false) }

        OnScreenResume { for (n in 1..3) colors[n - 1].intValue = color(n) }

        SettingsScaffold(
            title = stringResource(R.string.float_button),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            // what the buttons look like on a page (colour, icon), live
            item { FloatPreview(actions.map { it.value }, colors.map { it.intValue }) }
            for (n in 1..3) {
                val used = actions[n - 1].value != 0
                settingsHeader(getString(R.string.button_title) + " " + n)
                item {
                    // on: pick an action (staying off when the picker is closed without one); off: not used
                    SettingRow(
                        title = stringResource(R.string.hd_action),
                        summary = if (used) stringResource(MainUtil.menuIconAt(actions[n - 1].value)) else stringResource(R.string.not_used),
                        onClick = { pickerFor = n },
                        trailing = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (used) FloatFace(actions[n - 1].value, colors[n - 1].intValue, 30.dp)
                                Switch(
                                    checked = used,
                                    onCheckedChange = { on -> if (on) pickerFor = n else actions[n - 1].set(0) },
                                    modifier = Modifier.padding(start = 12.dp),
                                )
                            }
                        },
                    )
                }
                if (used) {
                    item {
                        SettingRow(
                            title = stringResource(R.string.icon_color),
                            onClick = {
                                com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host -> DialogEditIcon(host, 10 + n, null) }
                            },
                            trailing = {
                                Box(
                                    Modifier.size(28.dp).clip(MaterialTheme.shapes.small).background(Color(colors[n - 1].intValue))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                                )
                            },
                        )
                    }
                    item {
                        SettingSwitchRow(
                            title = stringResource(R.string.always_visible),
                            checked = shows[n - 1].value,
                            onCheckedChange = shows[n - 1]::set,
                        )
                    }
                    // one-handed mode: how far the page moves down (was under the one-handed mode button's own settings)
                    if (actions[n - 1].value == ONE_HAND) {
                        item {
                            SettingRow(
                                title = stringResource(R.string.onehand_area),
                                onClick = { startActivity(android.content.Intent(context, HandAreaSettingsActivity::class.java)) },
                            )
                        }
                    }
                }
            }
            settingsFooter(getString(R.string.drag_move_guide))
        }

        if (pickerFor != 0) {
            val n = pickerFor
            // closing the picker without a choice leaves the button as it was (off stays off)
            com.mycompany.app.ui.components.OpumSheetBox(onDismiss = { pickerFor = 0 }) {
                androidx.compose.foundation.layout.Box {
                    Column(Modifier.padding(top = 16.dp, bottom = 8.dp)) {
                        Text(
                            stringResource(R.string.button_title) + " " + n, style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 20.dp),
                        )
                        com.mycompany.app.ui.browser.ActionCatalog(
                            onPick = { id -> if (id in 1..73) actions[n - 1].set(id); pickerFor = 0 },
                            chosen = setOf(actions[n - 1].value), maxHeight = 460.dp,
                        )
                        TextButton(onClick = { pickerFor = 0 }, modifier = Modifier.align(Alignment.End).padding(end = 12.dp)) {
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
                    FloatSettingsActivity.resetToDefaults(context)
                    for (menuFor in 1..3) {
                        actions[menuFor - 1].reload(action(menuFor))
                        colors[menuFor - 1].intValue = color(menuFor)
                    }
                    shows[0].reload(FloatSettings.flt1Show.value); shows[1].reload(FloatSettings.flt2Show.value); shows[2].reload(FloatSettings.flt3Show.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /** A floating button as the browser draws it: a circle in its colour with the action's white icon and a thin ring. */
    @Composable
    private fun FloatFace(action: Int, color: Int, size: androidx.compose.ui.unit.Dp) {
        Box(
            Modifier.size(size).clip(CircleShape).background(Color(color)).border(1.dp, Color(0x33000000), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.foundation.Image(painterResource(MainUtil.iconResFor(action, 2)), null, Modifier.size(size * 0.6f))
        }
    }

    /**
     * Preview: a small page with the bars and the buttons that are on, as on a web page (along the right edge, where they
     * start; on the real page each can be dragged to its own place).
     */
    @Composable
    private fun FloatPreview(actions: List<Int>, colors: List<Int>) {
        val dark = com.mycompany.app.ui.state.AppState.darkUi
        val bar = if (dark) MyBarFrameColors.dark else MyBarFrameColors.light
        Surface(
            shape = RoundedCornerShape(18.dp), color = if (dark) Color(0xFF101114) else Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).height(170.dp),
        ) {
            Column {
                Box(Modifier.fillMaxWidth().height(22.dp).background(bar))
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    val line = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        for (w in floatArrayOf(0.5f, 0.9f, 0.8f, 0.95f, 0.6f)) Box(Modifier.fillMaxWidth(w).height(6.dp).background(line, RoundedCornerShape(3.dp)))
                    }
                    Column(Modifier.align(Alignment.CenterEnd).padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 0..2) if (actions[i] != 0) FloatFace(actions[i], colors[i], 32.dp)
                    }
                    if (actions.all { it == 0 }) {
                        Text(
                            stringResource(R.string.not_used), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
                Box(Modifier.fillMaxWidth().height(22.dp).background(bar))
            }
        }
    }

    companion object {
    /** The floating button action "One-handed mode" (index in `MainConst.mainMenuNames`). */
    const val ONE_HAND = 46

    /**
     * Once (2026-11-25): the separate one-handed mode button is gone; a floating button does the same. If it was on,
     * it becomes the first unused floating button (its colour kept, always visible) and is switched off. Without a
     * free floating button it is just switched off (the menu and bar actions still have one-handed mode).
     */
    @JvmStatic
    fun moveHandButton(context: Context) {
        if (!com.mycompany.app.pref.store.ZtriSettings.handIcon.value) return
        val handColor = com.mycompany.app.pref.store.EditorSettings.handColor.value
        val handAlpha = com.mycompany.app.pref.store.EditorSettings.handAlpha.value
        val handPos = com.mycompany.app.pref.store.EditorSettings.handPos.value
        val slots = listOf(FloatSettings.flt1Icon, FloatSettings.flt2Icon, FloatSettings.flt3Icon)
        val used = slots.any { it.value == ONE_HAND }
        val free = slots.indexOfFirst { it.value == 0 }
        if (!used && free >= 0) {
            FloatSettings.edit(context) {
                when (free) {
                    0 -> { set(FloatSettings.flt1Icon, ONE_HAND); set(FloatSettings.flt1Show, true); set(FloatSettings.flt1Color, handColor); set(FloatSettings.flt1Alpha, handAlpha); set(FloatSettings.flt1Pos, handPos) }
                    1 -> { set(FloatSettings.flt2Icon, ONE_HAND); set(FloatSettings.flt2Show, true); set(FloatSettings.flt2Color, handColor); set(FloatSettings.flt2Alpha, handAlpha); set(FloatSettings.flt2Pos, handPos) }
                    else -> { set(FloatSettings.flt3Icon, ONE_HAND); set(FloatSettings.flt3Show, true); set(FloatSettings.flt3Color, handColor); set(FloatSettings.flt3Alpha, handAlpha); set(FloatSettings.flt3Pos, handPos) }
                }
            }
            when (free) {
                0 -> PrefFloat.flt1Argb = PrefEditor.withAlpha(handColor, handAlpha)
                1 -> PrefFloat.flt2Argb = PrefEditor.withAlpha(handColor, handAlpha)
                else -> PrefFloat.flt3Argb = PrefEditor.withAlpha(handColor, handAlpha)
            }
        }
        com.mycompany.app.pref.store.ZtriSettings.edit(context) { set(com.mycompany.app.pref.store.ZtriSettings.handIcon, false) }
    }

    /** Restores the defaults of this page; also used by the reset of the Custom page. */
    @JvmStatic
    fun resetToDefaults(context: Context) {
        val color = MainConst.gradientColors[5]
        val pos = MainConst.presetPositions[5]
        if (com.mycompany.app.pref.store.TtsSettings.handTop.value != 60) {
            com.mycompany.app.pref.store.TtsSettings.reset(context, listOf(com.mycompany.app.pref.store.TtsSettings.handTop))
        }
        if (FloatSettings.flt1Icon.value != 0) FloatSettings.reset(context, listOf(FloatSettings.flt1Icon))
        if (FloatSettings.flt2Icon.value != 0) FloatSettings.reset(context, listOf(FloatSettings.flt2Icon))
        if (FloatSettings.flt3Icon.value != 0) FloatSettings.reset(context, listOf(FloatSettings.flt3Icon))
        if (FloatSettings.flt1Alpha.value != 0 || FloatSettings.flt1Color.value != color || FloatSettings.flt1Pos.value.compareTo(pos) != 0) {
            FloatSettings.editLocal { set(FloatSettings.flt1Alpha, 0) }; FloatSettings.editLocal { set(FloatSettings.flt1Color, color) }; FloatSettings.editLocal { set(FloatSettings.flt1Pos, pos) }; PrefFloat.flt1Argb = PrefEditor.withAlpha(color, 0)
            val p = PrefFloat.get(context)
            p!!.removeKey("mFlt1Alpha"); p.removeKey("mFlt1Color"); p.removeKey("mFlt1Pos"); p.save()
        }
        if (FloatSettings.flt2Alpha.value != 0 || FloatSettings.flt2Color.value != color || FloatSettings.flt2Pos.value.compareTo(pos) != 0) {
            FloatSettings.editLocal { set(FloatSettings.flt2Alpha, 0) }; FloatSettings.editLocal { set(FloatSettings.flt2Color, color) }; FloatSettings.editLocal { set(FloatSettings.flt2Pos, pos) }; PrefFloat.flt2Argb = PrefEditor.withAlpha(color, 0)
            val p = PrefFloat.get(context)
            p!!.removeKey("mFlt2Alpha"); p.removeKey("mFlt2Color"); p.removeKey("mFlt2Pos"); p.save()
        }
        if (FloatSettings.flt3Alpha.value != 0 || FloatSettings.flt3Color.value != color || FloatSettings.flt3Pos.value.compareTo(pos) != 0) {
            FloatSettings.editLocal { set(FloatSettings.flt3Alpha, 0) }; FloatSettings.editLocal { set(FloatSettings.flt3Color, color) }; FloatSettings.editLocal { set(FloatSettings.flt3Pos, pos) }; PrefFloat.flt3Argb = PrefEditor.withAlpha(color, 0)
            val p = PrefFloat.get(context)
            p!!.removeKey("mFlt3Alpha"); p.removeKey("mFlt3Color"); p.removeKey("mFlt3Pos"); p.save()
        }
        if (FloatSettings.flt1Show.value) FloatSettings.reset(context, listOf(FloatSettings.flt1Show))
        if (FloatSettings.flt2Show.value) FloatSettings.reset(context, listOf(FloatSettings.flt2Show))
        if (FloatSettings.flt3Show.value) FloatSettings.reset(context, listOf(FloatSettings.flt3Show))
    }
    }
}
