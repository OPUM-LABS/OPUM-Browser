package com.mycompany.app.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import kotlinx.coroutines.delay

/*
 * Settings page building blocks. Since 2026-11-19 a page is drawn as rounded cards (like the settings start page):
 * the rows between two dividers or headers form one card, a header is a small label above its card and a footer is
 * the explanation below it. Pages still list their rows with `item { }`, `settingsDivider()`, `settingsHeader()`;
 * [SettingsListScope] records them and the scaffold lays them out.
 */

/**
 * M3 top app bar with a back arrow, used by every Compose screen. Title on the left (one top
 * bar app-wide).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable () -> Unit = {},
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        title = { Text(title, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Sym.ArrowBack, contentDescription = stringResource(R.string.abc_action_bar_up_description))
            }
        },
        actions = { actions() },
        scrollBehavior = scrollBehavior,
    )
}

/** What a settings page lists, in order; laid out by [SettingsScaffold]. */
class SettingsListScope internal constructor() {
    internal sealed class Entry {
        class Row(val key: Any?, val content: @Composable LazyItemScope.() -> Unit) : Entry()
        object Break : Entry()
        class Header(val text: String) : Entry()
        class Footer(val text: String) : Entry()
    }

    internal val entries = ArrayList<Entry>()

    /** One row (or one block, e.g. an editor) of the current card. */
    fun item(key: Any? = null, content: @Composable LazyItemScope.() -> Unit) {
        entries.add(Entry.Row(key, content))
    }

    fun <T> items(items: List<T>, key: ((T) -> Any)? = null, itemContent: @Composable LazyItemScope.(T) -> Unit) {
        for (entry in items) entries.add(Entry.Row(key?.invoke(entry)) { itemContent(entry) })
    }

    internal fun divider() { entries.add(Entry.Break) }
    internal fun header(text: String) { entries.add(Entry.Header(text)) }
    internal fun footer(text: String) { entries.add(Entry.Footer(text)) }
}

/** Ends the current card; the next rows start a new one. */
fun SettingsListScope.settingsDivider() = divider()

/** A small label above the next card (ends the current card). */
fun SettingsListScope.settingsHeader(title: String) = header(title)

/** Explanation text under the current card (ends it). */
fun SettingsListScope.settingsFooter(text: String) = footer(text)

/** A thin divider between groups of rows, for screens with their own list. */
fun LazyListScope.settingsDivider() {
    item { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
}

/** Header text above a group of rows, for screens with their own list. */
fun LazyListScope.settingsHeader(title: String) {
    item { HeaderText(title) }
}

@Composable
private fun HeaderText(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
    )
}

/** The option a page was opened for from the settings search (its title), and where its row is. */
internal class SettingsFocus(val title: String) {
    var foundAt by mutableIntStateOf(-1)
    var flash by mutableStateOf(false)
}

internal val LocalSettingsFocus = compositionLocalOf<SettingsFocus?> { null }
internal val LocalSettingsRowIndex = compositionLocalOf { -1 }

/** Intent extra with the title of the option to scroll to and highlight (set by the settings search). */
const val EXTRA_FOCUS = "EXTRA_FOCUS"

private fun activityOf(context: Context): Activity? {
    var c: Context? = context
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

/** The widest a page's rows get (tablets, landscape): wider, and a row's arrow drifts far from its text. */
val READABLE_WIDTH = 840.dp

/** The side space that keeps a full-width list's rows at most [READABLE_WIDTH] wide and centred; 0 on phones. */
@Composable
fun readableSide(): androidx.compose.ui.unit.Dp {
    val width = with(androidx.compose.ui.platform.LocalDensity.current) {
        androidx.compose.ui.platform.LocalWindowInfo.current.containerSize.width.toDp()
    }
    return ((width - READABLE_WIDTH) / 2).coerceAtLeast(0.dp)
}

/**
 * Standard settings page: M3 top app bar with a back arrow and the rows below it as rounded cards. [onReset] adds a
 * "Reset to defaults" card at the end (it used to be an icon in the app bar). [cards] = false lays the rows out flat,
 * for editors that draw their own blocks.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScaffold(
    title: String,
    onBack: () -> Unit,
    actions: @Composable () -> Unit = {},
    onReset: (() -> Unit)? = null,
    cards: Boolean = true,
    above: (@Composable () -> Unit)? = null,
    jumpTo: SettingsJump? = null,
    content: SettingsListScope.() -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val focus = remember {
        activityOf(context)?.intent?.getStringExtra(EXTRA_FOCUS)?.takeIf { it.isNotBlank() }?.let { SettingsFocus(it) }
    }
    // a row the page itself points at (a tap on its preview): scrolled to and flashed like a search hit
    val jumpFocus = remember(jumpTo) { jumpTo?.let { SettingsFocus(it.title) } }
    val scope = SettingsListScope().apply(content)
    val side = readableSide()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { SettingsTopBar(title, onBack, actions, scrollBehavior) },
    ) { padding ->
        CompositionLocalProvider(LocalSettingsFocus provides (jumpFocus ?: focus)) {
            if (above == null) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = side,
                        top = padding.calculateTopPadding(),
                        end = side,
                        bottom = padding.calculateBottomPadding() + 24.dp,
                    ),
                ) {
                    layOut(scope.entries, cards, onReset)
                }
            } else {
                // [above] stays in place (a live preview); the rows scroll under it
                Column(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
                    androidx.compose.foundation.layout.Box(Modifier.padding(horizontal = side)) { above() }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentPadding = PaddingValues(start = side, top = 8.dp, end = side, bottom = padding.calculateBottomPadding() + 24.dp),
                    ) {
                        layOut(scope.entries, cards, onReset)
                    }
                }
            }
        }
    }
    if (jumpFocus != null) FocusScroller(jumpFocus, listState, fromTop = true)
    else if (focus != null) FocusScroller(focus, listState)
}

/** A row to scroll to and flash ([SettingsScaffold] `jumpTo`), by its title; a new instance jumps again. */
class SettingsJump(val title: String)

/** Scrolls until the row of [focus] has been composed, then brings it into view and lets it flash. */
@Composable
private fun FocusScroller(focus: SettingsFocus, listState: LazyListState, fromTop: Boolean = false) {
    LaunchedEffect(focus) {
        if (fromTop) {
            // the row may be above: look from the start (the page is short)
            listState.scrollToItem(0)
            delay(50)
        } else {
            delay(300)
        }
        var guard = 0
        while (focus.foundAt < 0 && listState.canScrollForward && guard++ < 40) {
            val visible = listState.layoutInfo.visibleItemsInfo.size.coerceAtLeast(1)
            listState.scrollToItem((listState.firstVisibleItemIndex + visible - 1).coerceAtLeast(listState.firstVisibleItemIndex + 1))
            delay(50)
        }
        if (focus.foundAt >= 0) {
            listState.animateScrollToItem((focus.foundAt - 1).coerceAtLeast(0))
            repeat(2) { // two pulses, so the eye finds the row
                focus.flash = true
                delay(1100)
                focus.flash = false
                delay(450)
            }
        }
    }
}

private val cardCorner = 20.dp
private val innerCorner = 0.dp

private fun cardShape(first: Boolean, last: Boolean): Shape = RoundedCornerShape(
    topStart = if (first) cardCorner else innerCorner,
    topEnd = if (first) cardCorner else innerCorner,
    bottomStart = if (last) cardCorner else innerCorner,
    bottomEnd = if (last) cardCorner else innerCorner,
)

private fun LazyListScope.layOut(entries: List<SettingsListScope.Entry>, cards: Boolean, onReset: (() -> Unit)?) {
    var lazyIndex = 0 // position of the next emitted item, so a row can tell the search where it is
    fun emit(key: Any? = null, content: @Composable LazyItemScope.() -> Unit) {
        val at = lazyIndex++
        item(key = key) { CompositionLocalProvider(LocalSettingsRowIndex provides at) { content() } }
    }
    var index = 0
    while (index < entries.size) {
        when (val entry = entries[index]) {
            is SettingsListScope.Entry.Break -> index++
            is SettingsListScope.Entry.Header -> { emit { HeaderText(entry.text) }; index++ }
            is SettingsListScope.Entry.Footer -> { emit { FooterText(entry.text) }; index++ }
            is SettingsListScope.Entry.Row -> {
                // a card: the run of rows up to the next divider, header or footer
                val afterHeader = entries.getOrNull(index - 1) is SettingsListScope.Entry.Header
                val run = ArrayList<SettingsListScope.Entry.Row>()
                while (index < entries.size && entries[index] is SettingsListScope.Entry.Row) {
                    run.add(entries[index] as SettingsListScope.Entry.Row); index++
                }
                val gap = if (lazyIndex == 0) 8.dp else 16.dp
                if (cards && !afterHeader) emit { Spacer(Modifier.height(gap)) }
                else if (!cards && !afterHeader && lazyIndex > 0) emit { HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp)) }
                run.forEachIndexed { i, row ->
                    emit(row.key) { RowFrame(cards, first = i == 0, last = i == run.size - 1) { row.content(this) } }
                }
            }
        }
    }
    if (onReset != null) {
        emit { Spacer(Modifier.height(24.dp)) }
        emit {
            // always a card of its own, also on pages that draw their rows without cards (swipe zones)
            RowFrame(true, first = true, last = true) {
                AppListItem(
                    modifier = Modifier.clickable(onClick = onReset),
                    headlineContent = { Text(stringResource(R.string.set_reset_defaults), color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Sym.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }
    }
}

@Composable
private fun FooterText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 8.dp, bottom = 4.dp),
    )
}

/** A row's place in its card: rounded outer corners, a hairline between rows. */
@Composable
private fun RowFrame(cards: Boolean, first: Boolean, last: Boolean, content: @Composable () -> Unit) {
    if (!cards) { content(); return }
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .clip(cardShape(first, last))
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        // one card: a hairline between rows, inset like the settings start page
        if (!first) HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        content()
    }
}

/**
 * Highlight of a row the settings search pointed at: reports where it is (so the page can scroll to it) and returns
 * the background to draw behind it.
 */
@Composable
private fun focusBackground(title: String): Color {
    val focus = LocalSettingsFocus.current
    val matches = focus != null && focus.title == title
    if (matches && focus.foundAt < 0) {
        val index = LocalSettingsRowIndex.current
        LaunchedEffect(Unit) { focus.foundAt = if (index >= 0) index else 0 }
    }
    val target = if (matches && focus.flash) MaterialTheme.colorScheme.primary.copy(alpha = 0.22f) else Color.Transparent
    return animateColorAsState(target, tween(400), label = "focus").value
}

/** A plain row: title, optional summary, optional leading icon and trailing content. */
@Composable
fun SettingRow(
    title: String,
    summary: String? = null,
    iconRes: Int? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val highlight = focusBackground(title)
    AppListItem(
        modifier = (if (onClick != null && enabled) Modifier.clickable(onClick = onClick) else Modifier).background(highlight),
        headlineContent = { Text(title) },
        supportingContent = summary?.takeIf { it.isNotEmpty() }?.let { { Text(it) } },
        leadingContent = iconRes?.let {
            { Icon(painterResource(it), contentDescription = null, modifier = Modifier.size(24.dp)) }
        },
        trailingContent = trailing,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

/** A row with a switch; the whole row toggles it. */
@Composable
fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    summary: String? = null,
    iconRes: Int? = null,
    enabled: Boolean = true,
) {
    SettingRow(
        title = title,
        summary = summary,
        iconRes = iconRes,
        enabled = enabled,
        onClick = { onCheckedChange(!checked) },
        trailing = { Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled) },
    )
}

/** A row that shows the current value of a choice as its summary and opens a picker when clicked. */
@Composable
fun SettingChoiceRow(title: String, value: String, onClick: () -> Unit, iconRes: Int? = null) {
    SettingRow(title = title, summary = value, iconRes = iconRes, onClick = onClick)
}

/**
 * A choice of two or three short options as a segmented button under the title (instead of a dialog). Labels may
 * wrap to two lines.
 */
@Composable
fun SettingSegmentedRow(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    summary: String? = null,
    enabled: Boolean = true,
) {
    val highlight = focusBackground(title)
    Column(Modifier.fillMaxWidth().background(highlight).padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 14.dp)) {
        Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        if (!summary.isNullOrEmpty()) {
            Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(10.dp))
        // equal heights, so a two-line label does not make its segment stick out
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    selected = index == selectedIndex,
                    onClick = { if (index != selectedIndex) onSelect(index) },
                    shape = SegmentedButtonDefaults.itemShape(index, options.size),
                    modifier = Modifier.fillMaxHeight(),
                    enabled = enabled,
                    icon = {},
                ) {
                    Text(label, maxLines = 2, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

/**
 * A number set with a slider right on the page (instead of a dialog). The value is written when the finger lifts;
 * [label] formats it, values above [warnAbove] are shown in the error colour.
 */
@Composable
fun SettingSliderRow(
    title: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    label: (Int) -> String = { it.toString() },
    summary: String? = null,
    enabled: Boolean = true,
    warnAbove: Int? = null,
    below: (@Composable (Int) -> Unit)? = null,
) {
    val highlight = focusBackground(title)
    var current by remember(value) { mutableIntStateOf(value.coerceIn(range.first, range.last)) }
    val warn = warnAbove != null && current > warnAbove
    Column(Modifier.fillMaxWidth().background(highlight).padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                modifier = Modifier.weight(1f),
            )
            Text(
                label(current),
                style = MaterialTheme.typography.labelLarge,
                color = if (warn) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            )
        }
        if (!summary.isNullOrEmpty()) {
            Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        AppSlider(
            value = current.toFloat(),
            onValueChange = { current = Math.round(it).coerceIn(range.first, range.last) },
            onValueChangeFinished = { if (current != value) onValueChange(current) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            steps = if (range.last - range.first <= 20) range.last - range.first - 1 else 0,
            enabled = enabled,
        )
        // extra content that follows the slider while it is dragged (e.g. a live preview)
        below?.invoke(current)
    }
}

/** Simple container used by screens that need a plain column below the app bar. */
@Composable
fun SettingsSectionColumn(content: @Composable () -> Unit) {
    Column { content() }
}
