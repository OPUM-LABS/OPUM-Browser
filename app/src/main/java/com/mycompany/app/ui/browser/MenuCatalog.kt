package com.mycompany.app.ui.browser

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.components.Sym

/**
 * What the browser menu offers (2026-11-19 menu clean-up): the actions grouped for "All actions" and the editor's picker,
 * the retired ids that only showed as "Not used", the actions that only make sense on a web page (hidden on the start
 * page), the ready-made layouts and clearer names for actions that looked alike. Ids are the menu ids of
 * `MainConst.mainMenuNames` / `runMenuItem`.
 */
object MenuCatalog {
    /** The "All actions" tile at the end of the menu (never stored). */
    const val ALL = 1000

    /** Ids that do nothing any more (they showed as "Not used" with an old icon), and those that make no sense in the menu. */
    private val retired = setOf(0, 24, 34, 41)

    /** Actions grouped for "All actions" and the editor's picker. */
    val groups: List<Pair<Int, IntArray>> = listOf(
        R.string.mg_page to intArrayOf(75, 14, 11, 69, 31, 12, 43, 15, 16, 9, 13, 45, 63, 32, 67, 17, 42, 35),
        R.string.mg_tabs to intArrayOf(5, 30, 70, 36, 37, 38, 59, 39, 71, 72, 21, 74),
        R.string.mg_nav to intArrayOf(26, 27, 28, 44, 53, 52, 51, 55, 56, 57, 58),
        R.string.mg_library to intArrayOf(3, 4, 29, 33, 23, 7, 50, 10, 8, 64),
        R.string.mg_view to intArrayOf(22, 66, 19, 47, 18, 54, 20, 46, 6, 60),
        R.string.mg_privacy to intArrayOf(62, 73, 48, 49, 40),
        R.string.mg_app to intArrayOf(65, 61, 25, 76),
    )

    /** Actions that work on a web page only: left out of the menu while the start page is shown. */
    private val pageOnly = setOf(7, 8, 9, 11, 12, 13, 14, 15, 16, 17, 31, 32, 43, 45, 47, 63, 64, 67, 69, 73, 75)

    /** The default layout (menu, quick bar), also what the menu settings' reset restores. */
    val STANDARD = intArrayOf(5, 3, 4, 29, 12, 14, 11, 31, 69, 15, 22, 19, 17, 9) to intArrayOf(21, 20, 62, 16, 25)

    const val STANDARD_MENU = "5,3,4,29,12,14,11,31,69,15,22,19,17,9"
    const val STANDARD_QUICK = "21,20,62,16,25"
    /** The two free buttons of the top row (2026-11-26): This site and Workspaces (Restart and Edit menu before). */
    const val STANDARD_HEAD = "75,74"

    /** The two free buttons of the top row as stored, 0 for an empty one (always two). */
    fun headSlots(stored: String?): IntArray {
        val ids = MainUtil.parseIntList(stored) ?: IntArray(0)
        return IntArray(2) { ids.getOrElse(it) { 0 }.takeIf { id -> id > 0 && id != ALL && id !in retired } ?: 0 }
    }

    /** The defaults before the clean-up: a list still equal to them is moved to the new default. */
    const val OLD_MENU = "3,4,5,7,8,20,66,11,69,60,45,12,14,15,16,41,62,73,6,17,9,54,18,19,47,10,46,32,33,40"
    const val OLD_QUICK = "21,22,23,24,25"

    /** A stored list without the retired ids (null / empty stays as it is). */
    fun clean(ids: IntArray?): IntArray? = ids?.filter { it !in retired }?.toIntArray()

    /** The ids the menu shows now: retired ones dropped, page-only ones left out on the start page. */
    fun shown(ids: IntArray?, onStartPage: Boolean): IntArray? =
        ids?.filter { it !in retired && !(onStartPage && it in pageOnly) }?.toIntArray()

    fun isPageOnly(id: Int) = id in pageOnly

    /** Label of an action; two that looked alike get a clearer name. */
    fun labelRes(id: Int): Int = when (id) {
        ALL -> R.string.all_actions
        in BarGroups.FIRST..BarGroups.LAST, BarGroups.NEW -> R.string.bar_group
        66 -> R.string.dark_settings_item
        60 -> R.string.tts_settings_item
        else -> MainUtil.menuLabelAt(id)
    }

    fun label(context: Context, id: Int): String = try { context.getString(labelRes(id)) } catch (_: Exception) { "" }

    fun iconPainterRes(id: Int): Int =
        if (id == ALL) R.drawable.outline_dashboard_customize_black_24 else MainUtil.iconResFor(id, MainUtil.barTint(0, false))

}

/** An action's icon: the menu's own black / dark drawables; the "All actions" icon is tinted like text. */
@Composable
fun MenuActionIcon(id: Int, size: Dp = 24.dp, live: Boolean = false) {
    // [live] (the real menus, not the editors): Downloads (4) is filled with the main colour while finished downloads
    // are unseen (2026-11-20)
    if (live && id == 4) {
        val ctx = androidx.compose.ui.platform.LocalContext.current
        val n = com.mycompany.app.down.DownState.unseen.intValue.let { if (it < 0) com.mycompany.app.down.DownState.unseenCount(ctx) else it }
        if (n > 0) {
            androidx.compose.foundation.layout.Box(
                // same footprint as the plain icon (labels stay aligned), the icon a little smaller inside the circle
                Modifier.size(size).background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                Icon(painterResource(MenuCatalog.iconPainterRes(id)), null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(size * 0.7f))
            }
            return
        }
    }
    if (id == MenuCatalog.ALL) {
        Icon(painterResource(MenuCatalog.iconPainterRes(id)), null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(size))
    } else {
        Image(painterResource(MenuCatalog.iconPainterRes(id)), null, Modifier.size(size))
    }
}

/**
 * All actions, grouped, with a search field: the menu's "All actions" page and the editor's picker. [onStartPage] dims the
 * actions that need a web page; [chosen] marks ids already in the menu with a check.
 */
@Composable
fun ActionCatalog(
    onPick: (Int) -> Unit, modifier: Modifier = Modifier, onStartPage: Boolean = false, chosen: Set<Int> = emptySet(), maxHeight: Dp = 520.dp,
    /** A group shown first (e.g. the address bar and menu button in the bar editor). */
    extraGroup: Pair<Int, IntArray>? = null,
    /** A "none" row on top (picks 0), e.g. for a gesture that should do nothing. */
    noneLabel: Int? = null,
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val groups = remember(extraGroup) { listOfNotNull(extraGroup) + MenuCatalog.groups }
    val labels = remember(groups) { groups.flatMap { it.second.toList() }.associateWith { MenuCatalog.label(context, it) } }
    Column(modifier) {
        OutlinedTextField(
            value = query, onValueChange = { query = it }, singleLine = true,
            leadingIcon = { Icon(Sym.Search, null) },
            placeholder = { Text(stringResource(R.string.search_actions)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        )
        val needle = query.trim()
        LazyColumn(Modifier.fillMaxWidth().heightIn(max = maxHeight)) {
            if (noneLabel != null && needle.isEmpty()) {
                item(key = "none") {
                    Text(
                        stringResource(noneLabel), style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth().clickable { onPick(0) }.padding(horizontal = 20.dp, vertical = 14.dp),
                    )
                }
            }
            for ((title, ids) in groups) {
                val shown = ids.filter { needle.isEmpty() || (labels[it] ?: "").contains(needle, ignoreCase = true) }
                if (shown.isEmpty()) continue
                item(key = "h$title") {
                    Text(
                        stringResource(title), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 4.dp),
                    )
                }
                items(shown, key = { "a$it" }) { id ->
                    val unavailable = onStartPage && MenuCatalog.isPageOnly(id)
                    Row(
                        Modifier.fillMaxWidth().clickable(enabled = !unavailable) { onPick(id) }.padding(horizontal = 20.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.alpha(if (unavailable) 0.38f else 1f)) {
                            if (id == 1) Icon(painterResource(R.drawable.outline_search_black_24), null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
                            else if (id == 2) Image(painterResource(MainUtil.moreIconFor(MainUtil.barTint(0, false), false)), null, Modifier.size(24.dp))
                            else MenuActionIcon(id)
                        }
                        Text(
                            labels[id] ?: "", style = MaterialTheme.typography.bodyLarge,
                            color = if (unavailable) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f) else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 18.dp).weight(1f),
                        )
                        if (id in chosen) Icon(Sym.Check, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

/** One labelled button of the menu's header row: icon (with an optional red count) and a short label. */
@Composable
fun MenuHeaderItem(icon: Painter, label: String, modifier: Modifier = Modifier, badge: Int = 0, onClick: () -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(icon, null, Modifier.size(22.dp))
        Text(
            if (badge > 0) "$label · $badge" else label, style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis,
            color = if (badge > 0) Color(0xFFF4433A.toInt()) else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp),
        )
    }
}

/**
 * The menu's top row (2026-11-26): Web Filter (with the page's blocked count) on the left, Settings on the right, both
 * fixed; between them the two buttons chosen in the menu editor ([heads], 0 = empty). A page action is dimmed on the
 * start page. A long press on a chosen button opens the editor, as everywhere in the menu.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MenuHeaderRow(
    heads: IntArray, blocked: Int, dark: Boolean, onStartPage: Boolean, modifier: Modifier,
    onFilter: () -> Unit, onSettings: () -> Unit, onItem: (Int) -> Unit, onLong: () -> Unit,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        MenuHeaderItem(
            painterResource(
                if (blocked > 0) R.drawable.outline_verified_user_red_20
                else if (dark) R.drawable.outline_verified_user_dark_20 else R.drawable.outline_verified_user_black_20,
            ),
            stringResource(R.string.mh_cleaner), Modifier.weight(1f), badge = blocked, onClick = onFilter,
        )
        for (id in heads) {
            if (id <= 0) { Box(Modifier.weight(1f)); continue }
            val enabled = !(onStartPage && MenuCatalog.isPageOnly(id))
            Column(
                Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                    .combinedClickable(enabled = enabled, onClick = { onItem(id) }, onLongClick = onLong)
                    .padding(vertical = 6.dp).alpha(if (enabled) 1f else 0.38f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MenuActionIcon(id, 22.dp, live = true)
                Text(
                    stringResource(MenuCatalog.labelRes(id)), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
        MenuHeaderItem(
            painterResource(if (dark) R.drawable.outline_settings_dark_20 else R.drawable.outline_settings_black_20),
            stringResource(R.string.setting), Modifier.weight(1f), onClick = onSettings,
        )
    }
}

/** The quick bar under the menu: equal slots, each icon with a short label; a long press opens the editor. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MenuQuickRow(ids: IntArray, onTap: (Int) -> Unit, onLong: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        for (id in ids) {
            if (id == 68) { Box(Modifier.weight(1f)); continue }
            Column(
                Modifier.weight(1f).combinedClickable(onClick = { onTap(id) }, onLongClick = onLong).padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MenuActionIcon(id, 24.dp, live = true)
                Text(
                    stringResource(MenuCatalog.labelRes(id)), style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
    }
}
