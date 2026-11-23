package com.mycompany.app.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.SuggestOrder
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.ui.components.EngineLogo
import com.mycompany.app.web.SuggestEngines
import com.mycompany.app.web.WebSearch

/**
 * Address-bar suggestions: those of the chosen search engine on or off (`BrowserSettings.sugEngine.value`, `mSugEng`: 0 off, else on;
 * until 2026-11-23 it picked Google (2) or DuckDuckGo (1), now the search engine itself answers, see [SuggestEngines]), which local
 * sources are shown (`BrowserSettings.sugType.value`, `mSugType3`: bits 2 recent searches, 4 history, 8 bookmarks, 16 quick access) and their
 * order (`BrowserSettings.sugOrder`, [SuggestOrder]; since 2026-11-23): the rows are dragged by their handle, the first one is shown
 * next to the address bar. Apply stores the changes and closes. Compose (on [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogSetSuggest(mainActivity: android.app.Activity?) : ComposeBottomDialog(mainActivity) {
    private val engine = mutableIntStateOf(0)
    private val sources = mutableIntStateOf(BrowserSettings.sugType.value)
    private val order = mutableStateListOf<Int>().apply { addAll(SuggestOrder.current()) }

    private fun apply() {
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
        dismiss()
    }

    private fun move(from: Int, to: Int) {
        if (from == to || from !in order.indices || to !in order.indices) return
        order.add(to, order.removeAt(from))
    }

    init {
        if (BrowserSettings.sugEngine.value < 0) {
            BrowserSettings.editLocal { set(BrowserSettings.sugEngine, if (MainUtil.isChinaLocale()) 1 else 2) }
            PrefSet.putInt(context, 14, BrowserSettings.sugEngine.value, "mSugEng")
        }
        engine.intValue = BrowserSettings.sugEngine.value
        setSheet {
            var dragging by remember { mutableStateOf<Int?>(null) }
            var dragY by remember { mutableFloatStateOf(0f) }
            var rowHeight by remember { mutableFloatStateOf(1f) }
            Text(
                stringResource(R.string.suggest_order_hint), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 8.dp),
            )
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
                                    CustomAccessibilityAction(up) { val i = order.indexOf(source); move(i, i - 1); true },
                                    CustomAccessibilityAction(down) { val i = order.indexOf(source); move(i, i + 1); true },
                                )
                            }
                            .padding(start = 8.dp, end = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Menu, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp).padding(12.dp).pointerInput(source) {
                                detectDragGestures(
                                    onDragStart = { dragging = source; dragY = 0f },
                                    onDragEnd = { dragging = null; dragY = 0f },
                                    onDragCancel = { dragging = null; dragY = 0f },
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
                        if (source == SuggestOrder.ENGINE) EngineRow() else SourceRow(source)
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { apply() }) { Text(stringResource(R.string.apply)) }
            }
        }
        show()
    }

    /**
     * Suggestions of the chosen search engine (named, with its icon) on or off; the engine itself is chosen in the address
     * bar or under Search engine. Custom engines have no suggestion service: the switch is shown off and disabled.
     */
    @Composable
    private fun RowScope.EngineRow() {
        val type = ZtwoSettings.searchType.value
        val service = SuggestEngines.hasService(type)
        val on = engine.intValue != 0 && service
        val toggle = { if (service) engine.intValue = if (engine.intValue != 0) 0 else 2 }
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
    private fun RowScope.SourceRow(bit: Int) {
        val on = (sources.intValue and bit) == bit
        val toggle = { sources.intValue = if (on) sources.intValue and bit.inv() else sources.intValue or bit }
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
    }
}
