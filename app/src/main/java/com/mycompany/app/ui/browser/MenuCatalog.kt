package com.mycompany.app.ui.browser

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
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

    /**
     * A family (2026-11-27): one entry for actions of one kind that opens a small sheet with its members
     * ([ActionFamilySheet]). It can be placed like any action (menu, bars, gestures); its members stay pickable one by one.
     */
    class Family(val id: Int, val label: Int, val icon: Int, val iconDark: Int, val members: IntArray)

    const val FAMILY_FIRST = 3001
    const val FAMILY_LAST = 3099

    val families: List<Family> = listOf(
        Family(3001, R.string.share, R.drawable.outline_share_black_24, R.drawable.outline_share_dark_24, intArrayOf(12, 43, 67)),
        Family(3002, R.string.save, R.drawable.outline_file_save_black_24, R.drawable.outline_file_save_dark_24, intArrayOf(15, 16, 9, 8, 64)),
        Family(3003, R.string.fam_add, R.drawable.outline_library_add_black_24, R.drawable.outline_library_add_dark_24, intArrayOf(63, 45, 13)),
        Family(3004, R.string.fam_close, R.drawable.outline_tab_close_black_24, R.drawable.outline_tab_close_dark_24, intArrayOf(38, 59, 39)),
        Family(3005, R.string.fam_switch, R.drawable.outline_tab_move_black_24, R.drawable.outline_tab_move_dark_24, intArrayOf(36, 37, 72)),
        Family(3006, R.string.fam_scroll, R.drawable.outline_swap_vert_black_24, R.drawable.outline_swap_vert_dark_24, intArrayOf(55, 56, 57, 58)),
        Family(3007, R.string.fam_viewers, R.drawable.outline_folder_open_black_24, R.drawable.outline_folder_open_dark_24, intArrayOf(23, 7, 50, 10)),
    )

    fun isFamily(id: Int) = id in FAMILY_FIRST..FAMILY_LAST

    fun family(id: Int): Family? = if (isFamily(id)) families.firstOrNull { it.id == id } else null

    /**
     * Actions grouped for "All actions" and the editor's picker (2026-11-27: families instead of their members, about 45
     * entries instead of 77; the members are found by the search and stay pickable in the editor). The last group,
     * [LINKS], is folded: actions that only open a settings page.
     */
    val groups: List<Pair<Int, IntArray>> = listOf(
        R.string.mg_page to intArrayOf(75, 14, 11, 69, 31, 17, 3001, 3002, 3003, 35, 32),
        R.string.mg_tabs to intArrayOf(5, 30, 70, 21, 74, 71, 3004, 3005),
        R.string.mg_nav to intArrayOf(26, 27, 28, 44, 53, 52, 51, 3006),
        R.string.mg_library to intArrayOf(3, 4, 29, 33, 3007),
        R.string.mg_view to intArrayOf(22, 19, 47, 20, 18, 54, 46, 6),
        R.string.mg_privacy to intArrayOf(62, 73, 40),
        R.string.mg_app to intArrayOf(65, 25, 76),
        R.string.mg_links to intArrayOf(42, 66, 60, 61, 48, 49),
    )

    /** The folded group's title: actions that only open a settings page. */
    val LINKS = R.string.mg_links

    /** One line of an action list: an action, and whether it is a family's member shown under it (indented). */
    class Shown(val id: Int, val member: Boolean = false)

    /**
     * The groups as the action lists show them: a family's members follow it while it is in [open]; the settings
     * shortcuts ([LINKS]) are listed only while [linksOpen]. A search ([needle]) lists every action whose name matches,
     * members and shortcuts included.
     */
    fun shownGroups(groups: List<Pair<Int, IntArray>>, labels: Map<Int, String>, needle: String, open: Set<Int>, linksOpen: Boolean): List<Pair<Int, List<Shown>>> =
        groups.map { (title, ids) ->
            val rows = ArrayList<Shown>()
            if (needle.isNotEmpty()) {
                for (id in ids) {
                    if ((labels[id] ?: "").contains(needle, ignoreCase = true)) rows.add(Shown(id))
                    family(id)?.members?.forEach { m -> if ((labels[m] ?: "").contains(needle, ignoreCase = true)) rows.add(Shown(m)) }
                }
            } else if (title != LINKS || linksOpen) {
                for (id in ids) {
                    rows.add(Shown(id))
                    if (id in open) family(id)?.members?.forEach { rows.add(Shown(it, member = true)) }
                }
            }
            title to rows
        }

    /** The names of [groups]' actions and of the families' members (for the search). */
    fun labelsOf(context: Context, groups: List<Pair<Int, IntArray>>): Map<Int, String> =
        groups.flatMap { (_, ids) -> ids.toList() + ids.flatMap { family(it)?.members?.toList() ?: emptyList() } }.associateWith { label(context, it) }

    /** Actions that work on a web page only: left out of the menu while the start page is shown. */
    private val pageOnly = setOf(7, 8, 9, 11, 12, 13, 14, 15, 16, 17, 31, 32, 43, 45, 47, 63, 64, 67, 69, 73, 75, 3001, 3002, 3003)

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
        in FAMILY_FIRST..FAMILY_LAST -> family(id)?.label ?: R.string.all_actions
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
    // Desktop (17) is a switch for the current tab: filled while the tab shows the desktop site (2026-11-27)
    if (live && id == 17 && com.mycompany.app.ui.state.DesktopState.on) {
        androidx.compose.foundation.layout.Box(
            Modifier.size(size).background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape),
            contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
            Icon(painterResource(MenuCatalog.iconPainterRes(id)), null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(size * 0.7f))
        }
        return
    }
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
    /** A tap on a family shows its members instead of picking it (floating buttons and gestures pick one action). */
    expandFamilies: Boolean = false,
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val groups = remember(extraGroup) { listOfNotNull(extraGroup) + MenuCatalog.groups }
    val labels = remember(groups) { MenuCatalog.labelsOf(context, groups) }
    // unfolded families, and whether the settings shortcuts are shown
    val open = remember { androidx.compose.runtime.mutableStateListOf<Int>() }
    var linksOpen by remember { mutableStateOf(false) }
    Column(modifier) {
        OutlinedTextField(
            value = query, onValueChange = { query = it }, singleLine = true,
            leadingIcon = { Icon(Sym.Search, null) },
            placeholder = { Text(stringResource(R.string.search_actions)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        )
        val needle = query.trim()
        val shownGroups = MenuCatalog.shownGroups(groups, labels, needle, open.toSet(), linksOpen)
        LazyColumn(Modifier.fillMaxWidth().heightIn(max = maxHeight)) {
            if (noneLabel != null && needle.isEmpty()) {
                item(key = "none") {
                    Text(
                        stringResource(noneLabel), style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth().clickable { onPick(0) }.padding(horizontal = 20.dp, vertical = 14.dp),
                    )
                }
            }
            for ((title, shown) in shownGroups) {
                val folded = title == MenuCatalog.LINKS && needle.isEmpty()
                if (shown.isEmpty() && !folded) continue
                item(key = "h$title") {
                    if (folded) {
                        // the settings shortcuts: folded until opened
                        Row(
                            Modifier.fillMaxWidth().clickable { linksOpen = !linksOpen }.padding(start = 20.dp, end = 16.dp, top = 14.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(stringResource(title), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                            Icon(if (linksOpen) Sym.KeyboardArrowUp else Sym.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        Text(
                            stringResource(title), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 4.dp),
                        )
                    }
                }
                items(shown, key = { row -> (if (row.member) "m" else "a") + row.id }) { row ->
                    val id = row.id
                    val unavailable = onStartPage && MenuCatalog.isPageOnly(id)
                    val isFamily = MenuCatalog.isFamily(id) && needle.isEmpty()
                    Row(
                        Modifier.fillMaxWidth().clickable(enabled = !unavailable) {
                            if (!expandFamilies || !MenuCatalog.isFamily(id)) onPick(id)
                            // found by the search: the list comes back with the family open
                            else if (needle.isNotEmpty()) { query = ""; if (id !in open) open.add(id) }
                            else if (id in open) open.remove(id) else open.add(id)
                        }
                            .padding(start = if (row.member) 44.dp else 20.dp, end = if (isFamily) 4.dp else 20.dp, top = 11.dp, bottom = 11.dp),
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
                        if (isFamily) {
                            // shows the family's members, each can be picked on its own
                            androidx.compose.material3.IconButton(onClick = { if (id in open) open.remove(id) else open.add(id) }) {
                                Icon(if (id in open) Sym.KeyboardArrowUp else Sym.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * A menu button's label (2026-11-27): up to two lines, centred. A word wider than the room (long compound words such as
 * "Munkaterületek", or a whole label in scripts without spaces) is drawn smaller, down to 70 %, instead of being cut
 * after any letter; only what still does not fit ends in "…".
 */
@Composable
fun MenuLabel(text: String, style: TextStyle, color: Color, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(modifier, contentAlignment = Alignment.TopCenter) {
        val room = constraints.maxWidth
        val fitted = remember(text, style, room) {
            val widest = text.split(' ', '\n').filter { it.isNotEmpty() }
                .maxOfOrNull { measurer.measure(it, style, softWrap = false, maxLines = 1).size.width } ?: 0
            if (room == Constraints.Infinity || widest <= room) style
            // Japanese, Chinese, Thai: no spaces, a line may break between letters; two even lines rather than one letter alone
            else if (widest * MIN_LABEL_SCALE > room * 0.97f && breaksAnywhere(text)) style.copy(lineBreak = EVEN_LINES)
            else {
                // a little room to spare: the measured width is rounded
                val scale = maxOf(MIN_LABEL_SCALE, room * 0.97f / widest)
                fun TextUnit.scaled() = if (isSp) this * scale else this
                style.copy(fontSize = style.fontSize.scaled(), lineHeight = style.lineHeight.scaled(), letterSpacing = style.letterSpacing.scaled())
            }
        }
        Text(text, style = fitted, color = color, textAlign = TextAlign.Center, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

private const val MIN_LABEL_SCALE = 0.7f

private val EVEN_LINES = LineBreak(LineBreak.Strategy.Balanced, LineBreak.Strictness.Default, LineBreak.WordBreak.Phrase)

/** Scripts written without spaces between words. */
private fun breaksAnywhere(text: String): Boolean = text.codePoints().anyMatch {
    when (Character.UnicodeScript.of(it)) {
        Character.UnicodeScript.HAN, Character.UnicodeScript.HIRAGANA, Character.UnicodeScript.KATAKANA, Character.UnicodeScript.THAI -> true
        else -> false
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
        // the label gives way when room is short; the count always stays whole
        val colour = if (badge > 0) Color(0xFFF4433A.toInt()) else MaterialTheme.colorScheme.onSurfaceVariant
        Row(Modifier.padding(top = 3.dp, start = 2.dp, end = 2.dp)) {
            MenuLabel(label, MaterialTheme.typography.labelSmall, colour, Modifier.weight(1f, fill = false))
            if (badge > 0) Text(" · $badge", style = MaterialTheme.typography.labelSmall, maxLines = 1, softWrap = false, color = colour)
        }
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
                MenuLabel(
                    stringResource(MenuCatalog.labelRes(id)), MaterialTheme.typography.labelSmall,
                    MaterialTheme.colorScheme.onSurfaceVariant, Modifier.padding(top = 3.dp, start = 2.dp, end = 2.dp),
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
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
        for (id in ids) {
            if (id == 68) { Box(Modifier.weight(1f)); continue }
            Column(
                Modifier.weight(1f).combinedClickable(onClick = { onTap(id) }, onLongClick = onLong).padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MenuActionIcon(id, 24.dp, live = true)
                MenuLabel(
                    stringResource(MenuCatalog.labelRes(id)), MaterialTheme.typography.labelSmall,
                    MaterialTheme.colorScheme.onSurfaceVariant, Modifier.padding(top = 3.dp, start = 2.dp, end = 2.dp),
                )
            }
        }
    }
}
