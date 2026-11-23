package com.mycompany.app.ui.browser

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.mycompany.app.pref.store.MainSettings

/**
 * Button groups on the top and bottom bars (2026-11-20): one bar slot that holds up to [MAX] buttons and opens them in a
 * small pill when tapped. A group is an id from [FIRST] to [LAST] in the bar lists (`MainSettings.topItems` /
 * `botItems`), so the bar lists keep their format; the members live in `MainSettings.barGroups` as
 * `2000:3,44,45;2001:12,13`. The first member is the lead: its icon stands for the group on the bar, with a small corner
 * notch ([groupNotch]). Groups do not nest; the address bar, the menu button and blank space cannot join one.
 */
object BarGroups {
    const val FIRST = 2000
    const val LAST = 2999
    /** Most buttons in one group. */
    const val MAX = 5
    /** The "Group" tile of the layout editor's action list (a new, empty group); never stored. */
    const val NEW = 1999

    fun isGroup(id: Int) = id in FIRST..LAST

    /** [id] may be put into a group: a real action, not the address bar (1), the menu button (2), blank space or a group. */
    fun canJoin(id: Int) = id > 2 && id != 68 && id < FIRST - 1

    /** Parses the stored text into group id → members (bad parts are skipped, members capped at [MAX]). */
    fun parse(text: String?): LinkedHashMap<Int, List<Int>> {
        val map = LinkedHashMap<Int, List<Int>>()
        if (text.isNullOrBlank()) return map
        for (part in text.split(';')) {
            val colon = part.indexOf(':')
            if (colon <= 0) continue
            val id = part.substring(0, colon).trim().toIntOrNull() ?: continue
            if (!isGroup(id)) continue
            val members = part.substring(colon + 1).split(',').mapNotNull { it.trim().toIntOrNull() }
                .filter { canJoin(it) }.distinct().take(MAX)
            map[id] = members
        }
        return map
    }

    fun format(groups: Map<Int, List<Int>>): String =
        groups.entries.filter { isGroup(it.key) }.joinToString(";") { (id, members) -> id.toString() + ":" + members.take(MAX).joinToString(",") }

    /** A free group id (the lowest not in [used]). */
    fun nextId(used: Collection<Int>): Int {
        var id = FIRST
        while (id in used && id < LAST) id++
        return id
    }

    /**
     * What a bar slot becomes when saving: an empty group goes away (null), a group of one becomes that button, anything
     * else stays.
     */
    fun resolve(id: Int, groups: Map<Int, List<Int>>): Int? {
        if (!isGroup(id)) return id
        val members = groups[id].orEmpty()
        return when (members.size) {
            0 -> null
            1 -> members[0]
            else -> id
        }
    }

    private var cacheText: String? = null
    private var cache: Map<Int, List<Int>> = emptyMap()

    /** The stored groups (parsed once per change of the setting). */
    fun stored(): Map<Int, List<Int>> {
        val text = MainSettings.barGroups.value
        if (text != cacheText) {
            cache = parse(text)
            cacheText = text
        }
        return cache
    }

    fun members(id: Int): List<Int> = stored()[id].orEmpty()

    /** The button whose icon stands for group [id], or 0 for an empty group. */
    fun lead(id: Int): Int = members(id).firstOrNull() ?: 0
}

/**
 * The members of a group as the current screen knows them: the stored groups, or the layout editor's unsaved ones (it
 * provides its own), so a group's icon there shows the lead being edited.
 */
val LocalGroupMembers = androidx.compose.runtime.staticCompositionLocalOf<(Int) -> List<Int>> { { BarGroups.members(it) } }

/**
 * The group marker: a small filled triangle at the bottom-right corner of an icon ("there is more here"), in the icon's
 * [color]. Drawn just outside the icon's box so it does not cover the glyph.
 */
fun Modifier.groupNotch(color: Color): Modifier = this.drawWithContent {
    drawContent()
    val side = (size.minDimension * 0.27f).coerceAtLeast(4.dp.toPx())
    val out = 2.dp.toPx()
    val x = size.width + out
    val y = size.height + out
    val path = Path().apply {
        moveTo(x, y - side)
        lineTo(x, y)
        lineTo(x - side, y)
        close()
    }
    drawPath(path, color)
}
