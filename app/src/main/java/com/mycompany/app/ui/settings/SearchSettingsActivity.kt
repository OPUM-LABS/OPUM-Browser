package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.data.book.DataBookRecent
import com.mycompany.app.db.book.DbBookSearch
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.SuggestOrder
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.EngineLogo
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingSliderRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.rememberPref
import com.mycompany.app.web.SuggestEngines
import com.mycompany.app.web.WebSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.mycompany.app.ui.components.Sym

/**
 * Settings › Start page & search › Search (2026-11-26, was part of Start page & search): the search engine (its own
 * list screen), the address-bar suggestions as a card on the page (was `DialogSetSuggest`: the engine's suggestions
 * and the local sources, each with a switch, dragged by the handle to change the order; every change is stored at
 * once), auto-complete and preloading, and the recent searches (how many are kept, and in incognito mode).
 */
class SearchSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        var tick by remember { mutableIntStateOf(0) } // bumped on resume: the engine list changes the engine
        val autoComp = rememberPref({ ZtwoSettings.autoComp.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.autoComp, it) } }
        val preload = rememberPref({ BrowserSettings.preload.value }) { BrowserSettings.edit(context) { set(BrowserSettings.preload, it) } }
        val recentLimit = rememberPref({ ZtwoSettings.recentLimit.value }) {
            ZtwoSettings.edit(context) { set(ZtwoSettings.recentLimit, it) }
            DataBookRecent.instance(context).changed = true
        }
        val recentSecret = rememberPref({ ZtwoSettings.recentSecret.value }) {
            ZtwoSettings.edit(context) { set(ZtwoSettings.recentSecret, it) }
            if (SyncSettings.secretMode.value) DataBookRecent.instance(context).changed = true
        }
        var resetting by remember { mutableStateOf(false) }
        OnScreenResume { tick++ }

        // a custom search engine is stored in the database: load it once, then show its name
        LaunchedEffect(tick) {
            if (ZtwoSettings.searchType.value >= 10 && WebSearch.instance().engineById((ZtwoSettings.searchType.value - 100).toLong()) == null) {
                withContext(Dispatchers.IO) { DbBookSearch.engines(context) }
                tick++
            }
        }

        fun searchName(): String? =
            if (ZtwoSettings.searchType.value < 10) getString(MainConst.engineNames[ZtwoSettings.searchType.value])
            else WebSearch.instance().engineById((ZtwoSettings.searchType.value - 100).toLong())?.name

        val onlyCustom = stringResource(R.string.only_custom, stringResource(R.string.user_defined))

        SettingsScaffold(
            title = stringResource(R.string.hd_search),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.search_engine),
                    value = "$tick".let { searchName() ?: "" },
                    onClick = { startActivity(Intent(context, com.mycompany.app.ui.lists.SearchEngineListActivity::class.java)) },
                )
            }
            settingsHeader(getString(R.string.search_suggest))
            item(key = "suggest$tick") { SuggestionList() }
            settingsFooter(getString(R.string.suggest_order_hint))
            settingsHeader(getString(R.string.hd_typing))
            item {
                SettingSwitchRow(title = stringResource(R.string.auto_comp), checked = autoComp.value, onCheckedChange = autoComp::set)
            }
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.preload),
                    summary = stringResource(R.string.preload_sum),
                    options = listOf(stringResource(R.string.never), stringResource(R.string.preload_wifi), stringResource(R.string.preload_always)),
                    selectedIndex = preload.value.coerceIn(0, 2),
                    onSelect = preload::set,
                )
            }
            settingsHeader(getString(R.string.recent_search))
            // 0 = do not save, < 0 = unlimited, > 0 = how many
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.hd_keep),
                    options = listOf(stringResource(R.string.history_zero), stringResource(R.string.history_none), stringResource(R.string.user_defined)),
                    selectedIndex = when { recentLimit.value == 0 -> 0; recentLimit.value < 0 -> 1; else -> 2 },
                    onSelect = { index -> recentLimit.set(when (index) { 0 -> 0; 1 -> -1; else -> 20 }) },
                )
            }
            item {
                SettingSliderRow(
                    title = stringResource(R.string.hd_count),
                    value = recentLimit.value.coerceIn(1, 100),
                    range = 1..100,
                    enabled = recentLimit.value > 0,
                    summary = if (recentLimit.value > 0) null else onlyCustom,
                    onValueChange = recentLimit::set,
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.recent_secret),
                    summary = if (recentLimit.value != 0) null else stringResource(R.string.recent_only_saved),
                    enabled = recentLimit.value != 0,
                    checked = recentSecret.value,
                    onCheckedChange = recentSecret::set,
                )
            }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    autoComp.reload(ZtwoSettings.autoComp.value)
                    preload.reload(BrowserSettings.preload.value)
                    recentLimit.reload(ZtwoSettings.recentLimit.value)
                    recentSecret.reload(ZtwoSettings.recentSecret.value)
                    tick++
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /** The suggestion sources in their order, each with a switch and a drag handle; changes are stored at once. */
    @Composable
    private fun SuggestionList() {
        val context: Context = this
        val engine = remember { mutableIntStateOf(currentEngine(context)) }
        val sources = remember { mutableIntStateOf(BrowserSettings.sugType.value) }
        val order = remember { mutableStateListOf<Int>().apply { addAll(SuggestOrder.current()) } }
        var dragging by remember { mutableStateOf<Int?>(null) }
        var dragY by remember { mutableFloatStateOf(0f) }
        var rowHeight by remember { mutableFloatStateOf(1f) }

        fun store() {
            if (BrowserSettings.sugEngine.value != engine.intValue || BrowserSettings.sugType.value != sources.intValue) {
                BrowserSettings.editLocal { set(BrowserSettings.sugEngine, engine.intValue) }
                BrowserSettings.editLocal { set(BrowserSettings.sugType, sources.intValue) }
                val prefs = PrefWeb.get(context, false)
                prefs!!.putInt(BrowserSettings.sugEngine.value, "mSugEng")
                prefs.putInt(BrowserSettings.sugType.value, "mSugType3")
                prefs.saveAsync()
            }
            val newOrder = SuggestOrder.format(order)
            if (SuggestOrder.format(SuggestOrder.current()) != newOrder) {
                BrowserSettings.edit(context) { set(BrowserSettings.sugOrder, newOrder) }
            }
        }

        fun move(from: Int, to: Int) {
            if (from == to || from !in order.indices || to !in order.indices) return
            order.add(to, order.removeAt(from))
        }

        Column(Modifier.fillMaxWidth()) {
            for (source in order) {
                key(source) {
                    val lifted = dragging == source
                    val up = stringResource(R.string.move_up)
                    val down = stringResource(R.string.move_down)
                    Row(
                        Modifier.fillMaxWidth()
                            .zIndex(if (lifted) 1f else 0f)
                            .graphicsLayer { translationY = if (lifted) dragY else 0f }
                            .background(if (lifted) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Transparent)
                            .onGloballyPositioned { rowHeight = it.size.height.toFloat().coerceAtLeast(1f) }
                            .semantics {
                                customActions = listOf(
                                    CustomAccessibilityAction(up) { val i = order.indexOf(source); move(i, i - 1); store(); true },
                                    CustomAccessibilityAction(down) { val i = order.indexOf(source); move(i, i + 1); store(); true },
                                )
                            }
                            .padding(start = 4.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Sym.Menu, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp).padding(12.dp).pointerInput(source) {
                                detectDragGestures(
                                    onDragStart = { dragging = source; dragY = 0f },
                                    onDragEnd = { dragging = null; dragY = 0f; store() },
                                    onDragCancel = { dragging = null; dragY = 0f; store() },
                                ) { change, amount ->
                                    change.consume()
                                    dragY += amount.y
                                    val i = order.indexOf(source)
                                    if (dragY > rowHeight / 2 && i < order.size - 1) {
                                        move(i, i + 1); dragY -= rowHeight
                                    } else if (dragY < -rowHeight / 2 && i > 0) {
                                        move(i, i - 1); dragY += rowHeight
                                    }
                                }
                            },
                        )
                        if (source == SuggestOrder.ENGINE) {
                            EngineRow(engine.intValue) { engine.intValue = it; store() }
                        } else {
                            val on = (sources.intValue and source) == source
                            SourceRow(source, on) {
                                sources.intValue = if (on) sources.intValue and source.inv() else sources.intValue or source
                                store()
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Suggestions of the chosen search engine (named, with its icon) on or off. Custom engines have no suggestion
     * service: the switch is shown off and disabled.
     */
    @Composable
    private fun RowScope.EngineRow(engine: Int, onChange: (Int) -> Unit) {
        val type = ZtwoSettings.searchType.value
        val service = SuggestEngines.hasService(type)
        val on = engine != 0 && service
        val toggle = { if (service) onChange(if (engine != 0) 0 else 2) }
        Row(Modifier.weight(1f).clickable(enabled = service) { toggle() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (service) {
                EngineLogo(type, 24.dp) {
                    Icon(painterResource(MainConst.engineIcons[type]), null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(Modifier.weight(1f).padding(start = if (service) 16.dp else 0.dp, end = 8.dp)) {
                val name = if (type < 10) stringResource(MainConst.engineNames[type]) else WebSearch.instance().engineById((type - 100).toLong())?.name
                Text(name ?: stringResource(R.string.search_engine), style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(if (service) R.string.suggest_engine_on else R.string.suggest_engine_none),
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = on, enabled = service, onCheckedChange = { toggle() })
        }
    }

    /** A local source (recent searches, history, bookmarks, quick access) with its switch. */
    @Composable
    private fun RowScope.SourceRow(bit: Int, on: Boolean, toggle: () -> Unit) {
        Row(Modifier.weight(1f).clickable { toggle() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(sourceLabel(bit)), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Switch(checked = on, onCheckedChange = { toggle() })
        }
    }

    companion object {
        /** Name of a local suggestion source ([BrowserSettings.sugType] bit). */
        fun sourceLabel(bit: Int): Int = when (bit) {
            2 -> R.string.recent_search
            4 -> R.string.history
            8 -> R.string.bookmark
            else -> R.string.quick_access
        }

        /** The stored engine switch; an unset value (< 0) becomes the default first. */
        private fun currentEngine(context: Context): Int {
            if (BrowserSettings.sugEngine.value < 0) {
                BrowserSettings.editLocal { set(BrowserSettings.sugEngine, if (MainUtil.isChinaLocale()) 1 else 2) }
                PrefSet.putInt(context, 14, BrowserSettings.sugEngine.value, "mSugEng")
            }
            return BrowserSettings.sugEngine.value
        }

        /** Restores the defaults of this page (search engine, suggestions, typing, recent searches). */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (ZtwoSettings.searchType.value != 4 || ZtwoSettings.searchColor.value != 0 || !TextUtils.isEmpty(ZtwoSettings.searchUser.value)) {
                ZtwoSettings.editLocal { set(ZtwoSettings.searchType, 4) }; ZtwoSettings.editLocal { set(ZtwoSettings.searchUser, "") }; ZtwoSettings.editLocal { set(ZtwoSettings.searchColor, 0) }; ZtwoSettings.editLocal { set(ZtwoSettings.searchForm, false) }
                val p = PrefZtwo.get(context, false)
                p!!.removeKey("mSearchType2"); p.removeKey("mSearchUser"); p.removeKey("mSearchForm2"); p.removeKey("mSearchColor"); p.save()
            }
            if (!TextUtils.isEmpty(ZoneSettings.searchUse.value)) ZoneSettings.reset(context, listOf(ZoneSettings.searchUse))
            val engine = if (MainUtil.isChinaLocale()) 1 else 2
            if (BrowserSettings.sugEngine.value != engine || BrowserSettings.sugType.value != 14) {
                BrowserSettings.editLocal { set(BrowserSettings.sugEngine, engine) }; BrowserSettings.editLocal { set(BrowserSettings.sugType, 14) }
                val p = PrefWeb.get(context, false)
                p!!.removeKey("mSugEng"); p.removeKey("mSugType3"); p.save()
            }
            if (BrowserSettings.sugOrder.value != SuggestOrder.DEFAULT) BrowserSettings.reset(context, listOf(BrowserSettings.sugOrder))
            if (!ZtwoSettings.autoComp.value) ZtwoSettings.reset(context, listOf(ZtwoSettings.autoComp))
            if (BrowserSettings.preload.value != com.mycompany.app.web.Preload.WIFI) BrowserSettings.reset(context, listOf(BrowserSettings.preload))
            if (ZtwoSettings.recentLimit.value != 20) {
                ZtwoSettings.editLocal { set(ZtwoSettings.recentLimit, 20) }; PrefSet.removeNow(context, 16, "mRecentLimit"); DataBookRecent.instance(context).changed = true
            }
            if (ZtwoSettings.recentSecret.value) {
                ZtwoSettings.editLocal { set(ZtwoSettings.recentSecret, false) }; PrefSet.removeNow(context, 16, "mRecentSecret")
                if (SyncSettings.secretMode.value) DataBookRecent.instance(context).changed = true
            }
        }
    }
}
