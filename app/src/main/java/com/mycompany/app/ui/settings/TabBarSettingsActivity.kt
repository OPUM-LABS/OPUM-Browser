package com.mycompany.app.ui.settings

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.MainSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ActionPickerSheet
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
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Settings › Tabs › Tab bar and tab button (2026-11-25): the phone preview of Screen and scrolling ([PhonePreview])
 * with the tab bar where it goes and as it looks, and the tab button showing what it counts. Tapping the tab bar or
 * the tab button jumps to its options. Everything was on the Tabs page before, the position and the looks in two
 * dialogs.
 */
class TabBarSettingsActivity : OpumComposeActivity() {

    /** The parts of the preview that can be tapped, and the title of the row each one jumps to. */
    private enum class Part(val row: Int) { TABS(R.string.show_tab_bar), BUTTON(R.string.tab_indicator) }

    @Composable
    override fun Content() {
        val context: Context = this
        val tabBar = rememberPref({ BrowserSettings.tabBar.value }) { BrowserSettings.edit(context) { set(BrowserSettings.tabBar, it) } }
        val outline = rememberPref({ BrowserSettings.tabAccent.value }) { BrowserSettings.edit(context) { set(BrowserSettings.tabAccent, it) } }
        val color = rememberPref({ BrowserSettings.tabColor.value.let { if (it in MainConst.tabColors.indices) it else 5 } }) {
            BrowserSettings.edit(context) { set(BrowserSettings.tabColor, it) }
        }
        val add = rememberPref({ PdfSettings.tabAdd.value }) { PdfSettings.edit(context) { set(PdfSettings.tabAdd, it) } }
        val close = rememberPref({ PdfSettings.tabClose.value }) { PdfSettings.edit(context) { set(PdfSettings.tabClose, it) } }
        // width and height in % of the default (stored in px, as the browser reads them)
        val width = rememberPref({ Math.round(PdfSettings.tabWidth.value * 100f / MainApp.dp144).coerceIn(50, 200) }) {
            PdfSettings.edit(context) { set(PdfSettings.tabWidth, Math.round(it * MainApp.dp144 / 100f)) }
        }
        val height = rememberPref({ Math.round(PdfSettings.tabHeight.value * 100f / MainApp.dp40).coerceIn(50, 200) }) {
            PdfSettings.edit(context) { set(PdfSettings.tabHeight, Math.round(it * MainApp.dp40 / 100f)) }
        }
        val swipeUp = rememberPref({ BrowserSettings.tabSwipeUp.value }) { BrowserSettings.edit(context) { set(BrowserSettings.tabSwipeUp, it) } }
        val swipeDown = rememberPref({ BrowserSettings.tabSwipeDown.value }) { BrowserSettings.edit(context) { set(BrowserSettings.tabSwipeDown, it) } }
        val indicator = rememberPref({ ZtwoSettings.tabIndi.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.tabIndi, it) } }
        val loop = rememberPref({ ZtwoSettings.tabLoop.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.tabLoop, it) } }
        val anim = rememberPref({ AlbumSettings.tabAnim.value }) { AlbumSettings.edit(context) { set(AlbumSettings.tabAnim, it) } }
        val railRight = rememberPref({ ZoneSettings.tabRailRight.value }) { ZoneSettings.edit(context) { set(ZoneSettings.tabRailRight, it) } }
        // read again when coming back from the bar editor
        var tick by remember { mutableIntStateOf(0) }
        OnScreenResume { tick++ }
        @Suppress("UNUSED_VARIABLE") val read = tick
        // the sizes while their sliders are dragged, so the preview follows the finger
        var liveWidth by remember { mutableIntStateOf(width.value) }
        var liveHeight by remember { mutableIntStateOf(height.value) }
        var choosing by remember { mutableIntStateOf(0) } // 1 tab bar place, 6 / 7 swipe up / down
        var picker by remember { mutableIntStateOf(0) }   // 6 or 7: action picker
        var selected by remember { mutableStateOf<Part?>(null) }
        // the outline only marks the part for a moment
        LaunchedEffect(selected) { if (selected != null) { kotlinx.coroutines.delay(1800); selected = null } }
        var jump by remember { mutableStateOf<SettingsJump?>(null) }
        var resetting by remember { mutableStateOf(false) }
        val portrait = LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE

        SettingsScaffold(
            title = stringResource(R.string.tab_bar_page),
            onBack = { finish() },
            onReset = { resetting = true },
            jumpTo = jump,
            above = if (!portrait) null else {
                {
                    val bars = PreviewBars(
                        BrowserSettings.showStatus.value, BrowserSettings.showNavi.value, BrowserSettings.fixTop.value, BrowserSettings.fixBot.value,
                        PdfSettings.topToBottom.value,
                        MainUtil.parseIntList(MainSettings.topItems.value)?.toList().orEmpty(),
                        MainUtil.parseIntList(MainSettings.botItems.value)?.toList().orEmpty(),
                        EditorSettings.topAlpha.value.coerceIn(0, 90), EditorSettings.botAlpha.value.coerceIn(0, 90),
                        tabBar.value, PreviewTabs(outline.value, color.value, add.value, close.value, liveWidth, liveHeight),
                        // the second of three tabs is open
                        if (indicator.value) "2/3" else "3",
                    )
                    TabPreview(bars, selected) { part ->
                        selected = part
                        jump = SettingsJump(getString(part.row))
                    }
                    Text(
                        stringResource(R.string.look_tap_guide), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    )
                }
            },
        ) {
            settingsHeader(getString(R.string.hd_tab_bar))
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.show_tab_bar),
                    value = stringResource(tabBarLabels[tabBar.value.coerceIn(0, 4)]),
                    onClick = { choosing = 1 },
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.sub_line), checked = outline.value,
                    onCheckedChange = { selected = Part.TABS; outline.set(it) }, enabled = tabBar.value != 0,
                )
            }
            item {
                TabColorRow(
                    title = stringResource(R.string.color_title), selected = color.value, enabled = tabBar.value != 0,
                ) { selected = Part.TABS; color.set(it) }
            }
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.add_icon),
                    options = MainConst.scrollPosValues.map { stringResource(MainConst.scrollPosNames[it]) },
                    selectedIndex = MainConst.scrollPosValues.indexOf(add.value).coerceAtLeast(0),
                    onSelect = { selected = Part.TABS; add.set(MainConst.scrollPosValues[it]) },
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.close_icon), checked = close.value,
                    onCheckedChange = { selected = Part.TABS; close.set(it) }, enabled = tabBar.value != 0,
                )
            }
            item {
                SettingSliderRow(
                    title = stringResource(R.string.size_width), value = width.value, range = 50..200,
                    onValueChange = { width.set(it); liveWidth = it }, label = { "$it%" }, enabled = tabBar.value != 0,
                    below = { v -> LaunchedEffect(v) { liveWidth = v } },
                )
            }
            item {
                SettingSliderRow(
                    title = stringResource(R.string.size_height), value = height.value, range = 50..200,
                    onValueChange = { height.set(it); liveHeight = it }, label = { "$it%" }, enabled = tabBar.value != 0,
                    below = { v -> LaunchedEffect(v) { liveHeight = v } },
                )
            }
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.tab_swipe_up),
                    value = stringResource(MainUtil.menuIconAt(swipeUp.value)),
                    onClick = { choosing = 6 },
                )
            }
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.tab_swipe_dn),
                    value = stringResource(MainUtil.menuIconAt(swipeDown.value)),
                    onClick = { choosing = 7 },
                )
            }
            settingsHeader(getString(R.string.hd_tab_button))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.tab_indicator), checked = indicator.value,
                    onCheckedChange = { selected = Part.BUTTON; indicator.set(it) },
                )
            }
            item {
                SettingSwitchRow(title = stringResource(R.string.tab_loop), checked = loop.value, onCheckedChange = loop::set)
            }
            item {
                SettingSwitchRow(title = stringResource(R.string.use_tab_anim), checked = anim.value, onCheckedChange = anim::set)
            }
            settingsFooter(getString(R.string.tab_indi_info) + "\n" + getString(R.string.tab_loop_info))
            settingsHeader(getString(R.string.hd_tab_overview))
            item {
                SettingSwitchRow(title = stringResource(R.string.tab_rail_right), checked = railRight.value, onCheckedChange = railRight::set)
            }
        }

        when (choosing) {
            1 -> ChoiceDialog(
                title = stringResource(R.string.show_tab_bar),
                options = tabBarOrder.map { stringResource(tabBarLabels[it]) },
                selectedIndex = tabBarOrder.indexOf(tabBar.value),
                onSelect = { index -> selected = Part.TABS; tabBar.set(tabBarOrder[index]) },
                onDismiss = { choosing = 0 },
            )
            6, 7 -> {
                val which = choosing
                ChoiceDialog(
                    title = stringResource(if (which == 6) R.string.tab_swipe_up else R.string.tab_swipe_dn),
                    options = listOf(stringResource(R.string.do_change), stringResource(R.string.not_used)),
                    selectedIndex = -1,
                    onSelect = { index ->
                        if (index == 0) picker = which else (if (which == 6) swipeUp else swipeDown).set(0)
                    },
                    onDismiss = { choosing = 0 },
                )
            }
        }
        if (picker != 0) {
            val state = if (picker == 6) swipeUp else swipeDown
            ActionPickerSheet(
                selectedId = state.value,
                onSelect = { id -> if (id in 0 until MainConst.mainMenuNames.size) state.set(id) },
                onDismiss = { picker = 0 },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    tabBar.reload(BrowserSettings.tabBar.value); outline.reload(BrowserSettings.tabAccent.value); color.reload(BrowserSettings.tabColor.value)
                    add.reload(PdfSettings.tabAdd.value); close.reload(PdfSettings.tabClose.value)
                    width.reload(100); height.reload(100); liveWidth = 100; liveHeight = 100
                    swipeUp.reload(BrowserSettings.tabSwipeUp.value); swipeDown.reload(BrowserSettings.tabSwipeDown.value)
                    indicator.reload(ZtwoSettings.tabIndi.value); loop.reload(ZtwoSettings.tabLoop.value)
                    anim.reload(AlbumSettings.tabAnim.value); railRight.reload(ZoneSettings.tabRailRight.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /** The phone with the tab bar; a tap on the tab bar or on a bar with the tab button reports that part. */
    @Composable
    private fun TabPreview(bars: PreviewBars, selected: Part?, onTap: (Part) -> Unit) {
        val density = LocalDensity.current
        PhonePreview(
            bars,
            onTap = { at, places, _ ->
                val bar = with(density) { places.barAt(at.y, 6.dp.toPx(), PreviewSize.sys.toPx()) }
                val part = when (bar) {
                    PreviewBar.TABS -> Part.TABS
                    PreviewBar.TOP -> if (TAB_LIST in bars.topIds) Part.BUTTON else null
                    PreviewBar.BOTTOM -> if (TAB_LIST in bars.botIds || (places.topAtBottom && TAB_LIST in bars.topIds)) Part.BUTTON else null
                    else -> null
                }
                if (part != null) onTap(part)
            },
            over = { places, _ ->
                when (selected) {
                    Part.TABS -> BarOutline(PreviewBar.TABS, bars, places)
                    // the bar that holds the tab button
                    Part.BUTTON -> BarOutline(if (TAB_LIST in bars.topIds && !places.topAtBottom) PreviewBar.TOP else PreviewBar.BOTTOM, bars, places)
                    null -> Unit
                }
            },
            // the page does not need to move here: nothing on it depends on scrolling
            scrolling = false,
        )
    }

    /** The tab colours as circles, the chosen one ticked. */
    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun TabColorRow(title: String, selected: Int, enabled: Boolean, onSelect: (Int) -> Unit) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                title, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.38f),
            )
            FlowRow(
                Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MainConst.tabColors.forEachIndexed { index, argb ->
                    Box(
                        Modifier.size(34.dp).background(Color(argb).copy(alpha = if (enabled) 1f else 0.38f), CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable(enabled = enabled) { onSelect(index) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (index == selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    }

    companion object {
        /** The tab button's id in the bars. */
        private const val TAB_LIST = 30

        /** Order of the tab bar places in the choice, and their labels (index = value: 0 none, 1 / 2 by the top bar, 3 / 4 by the bottom bar). */
        @JvmField
        val tabBarOrder: IntArray = intArrayOf(1, 2, 3, 4, 0)
        @JvmField
        val tabBarLabels: IntArray = intArrayOf(R.string.not_show, R.string.above_top, R.string.below_top, R.string.above_bot, R.string.below_bot)

        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (BrowserSettings.tabBar.value != 0) BrowserSettings.reset(context, listOf(BrowserSettings.tabBar))
            if (!BrowserSettings.tabAccent.value) BrowserSettings.reset(context, listOf(BrowserSettings.tabAccent))
            if (BrowserSettings.tabColor.value != 5) BrowserSettings.reset(context, listOf(BrowserSettings.tabColor))
            if (PdfSettings.tabAdd.value != 3 || !PdfSettings.tabClose.value || PdfSettings.tabWidth.value != MainApp.dp144 || PdfSettings.tabHeight.value != MainApp.dp40) {
                PdfSettings.reset(context, listOf(PdfSettings.tabAdd, PdfSettings.tabClose, PdfSettings.tabWidth, PdfSettings.tabHeight))
            }
            if (BrowserSettings.tabSwipeUp.value != 30) BrowserSettings.reset(context, listOf(BrowserSettings.tabSwipeUp))
            if (BrowserSettings.tabSwipeDown.value != 38) BrowserSettings.reset(context, listOf(BrowserSettings.tabSwipeDown))
            if (ZtwoSettings.tabIndi.value) ZtwoSettings.reset(context, listOf(ZtwoSettings.tabIndi))
            if (!ZtwoSettings.tabLoop.value) ZtwoSettings.reset(context, listOf(ZtwoSettings.tabLoop))
            if (!AlbumSettings.tabAnim.value) AlbumSettings.reset(context, listOf(AlbumSettings.tabAnim))
            if (ZoneSettings.tabRailRight.value) ZoneSettings.reset(context, listOf(ZoneSettings.tabRailRight))
        }
    }
}
