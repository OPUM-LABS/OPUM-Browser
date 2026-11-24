package com.mycompany.app.pref.store

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Workspaces (2026-11-24): separate, permanent browser spaces, each with its own tabs, history, bookmarks, quick access,
 * recent searches and passwords. A workspace stays until the user deletes it, and backups carry all of them (the list
 * is in `PrefSpace`, the rows in the databases).
 *
 * The rows of every space are told apart by a number in each table's `_secret` column (the column incognito always
 * used): [MAIN] is the normal browser, [INCOGNITO] the data incognito mode keeps, and workspaces get [FIRST_NUMBER] and
 * up. A number is never given twice, even after its workspace was deleted, so an older backup can never attach its
 * rows to a different workspace. Incognito mode itself is not a workspace: it stays a temporary mode.
 */
object Workspaces {
    const val MAIN = 0
    const val INCOGNITO = 1
    const val FIRST_NUMBER = 2

    /** A workspace: its number, name, colour (0 = none) and whether opening it asks to unlock. */
    data class Workspace(val number: Int, val name: String, val color: Int = 0, val locked: Boolean = false)

    /** The workspaces in [json] (as [format] writes them), in their order; unreadable or duplicate entries are left out. */
    fun parse(json: String?): List<Workspace> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            val seen = HashSet<Int>()
            val list = ArrayList<Workspace>()
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val number = item.optInt("n", -1)
                if (number < FIRST_NUMBER || !seen.add(number)) continue
                list.add(Workspace(number, item.optString("name", ""), item.optInt("color", 0), item.optBoolean("locked", false)))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    /** [list] as JSON for [WorkspaceSettings.list]. */
    fun format(list: List<Workspace>): String {
        val array = JSONArray()
        for (space in list) {
            array.put(JSONObject().put("n", space.number).put("name", space.name).put("color", space.color).put("locked", space.locked))
        }
        return array.toString()
    }

    /**
     * The number for the next new workspace: never one in [list], never below [stored] (numbers of deleted workspaces
     * stay retired), never below [FIRST_NUMBER].
     */
    fun nextNumber(stored: Int, list: List<Workspace>): Int =
        maxOf(stored, (list.maxOfOrNull { it.number } ?: 0) + 1, FIRST_NUMBER)

    /** [list] with a new workspace [name] at number [number] added at the end. */
    fun added(list: List<Workspace>, number: Int, name: String, color: Int = 0): List<Workspace> =
        list + Workspace(number, name, color)

    /** [list] without workspace [number] (Main and incognito are not in the list and cannot be removed). */
    fun removed(list: List<Workspace>, number: Int): List<Workspace> = list.filter { it.number != number }

    /** The workspace in use: [current] if it still exists, else Main. */
    fun resolveCurrent(current: Int, list: List<Workspace>): Int =
        if (current == MAIN || list.any { it.number == current }) current else MAIN

    /** The number whose rows the browser reads and writes: incognito's in incognito mode, else the workspace in use. */
    fun dataNumberFor(incognito: Boolean, current: Int): Int = if (incognito) INCOGNITO else current

    /** [list] with workspace [number] changed by [change] (unknown numbers leave it as it is). */
    fun changed(list: List<Workspace>, number: Int, change: (Workspace) -> Workspace): List<Workspace> =
        list.map { if (it.number == number) change(it) else it }

    /** Whether opening workspace [number] asks to unlock (Main never does). */
    fun isLocked(number: Int, list: List<Workspace>): Boolean = list.any { it.number == number && it.locked }

    /** A name for a new workspace: [base] alone, or "[base] 2", "[base] 3" ... when that name is taken. */
    fun freeName(base: String, list: List<Workspace>): String {
        val names = list.map { it.name.trim().lowercase() }.toSet()
        if (base.trim().lowercase() !in names) return base
        var i = 2
        while ("$base $i".lowercase() in names) i++
        return "$base $i"
    }

    /** The open tab of each space by number ([WorkspaceSettings.tabs]). */
    fun parseTabs(json: String?): Map<Int, Int> {
        if (json.isNullOrBlank()) return emptyMap()
        return try {
            val item = JSONObject(json)
            val out = HashMap<Int, Int>()
            for (key in item.keys()) {
                val number = key.toIntOrNull() ?: continue
                out[number] = item.optInt(key, 0)
            }
            out
        } catch (_: Exception) {
            emptyMap()
        }
    }

    fun formatTabs(tabs: Map<Int, Int>): String {
        val item = JSONObject()
        for ((number, index) in tabs.toSortedMap()) item.put(number.toString(), index)
        return item.toString()
    }

    /** The numbers in [WorkspaceSettings.drop]. */
    fun dropList(text: String?): List<Int> =
        text.orEmpty().split(',').mapNotNull { it.trim().toIntOrNull() }.filter { it >= FIRST_NUMBER }.distinct()

    // ------------------------------------------------------------------ the current settings

    /** The parsed list and the JSON it came from (asked on every database query, so parsed only when it changed). */
    @Volatile
    private var cache: Pair<String?, List<Workspace>> = null to emptyList()

    /** The workspaces other than Main. */
    fun all(): List<Workspace> {
        val json = WorkspaceSettings.list.value
        val cached = cache
        if (cached.first == json) return cached.second
        val list = parse(json)
        cache = json to list
        return list
    }

    /** The number of the workspace in use. */
    fun currentNumber(): Int = resolveCurrent(WorkspaceSettings.current.value, all())

    /** The number of the rows to use now (the `_secret` column). */
    @JvmStatic
    fun dataNumber(): Int = dataNumberFor(SyncSettings.secretMode.value, currentNumber())

    /**
     * The rows of incognito ([secret]) or of the workspace in use: for code that asks for one of the two explicitly
     * (the tab overview shows both, a download remembers where it started).
     */
    @JvmStatic
    fun numberFor(secret: Boolean): Int = if (secret) INCOGNITO else currentNumber()

    /** [numberFor] as a query argument. */
    @JvmStatic
    fun keyFor(secret: Boolean): String = numberFor(secret).toString()

    /**
     * The rows of bookmarks, quick access, saved passwords and saved pages: always the space in use, also in incognito
     * (2026-11-24, owner's choice: incognito keeps no such things of its own, it uses those of the space it came from).
     */
    @JvmStatic
    fun sharedNumber(): Int = currentNumber()

    /** [sharedNumber] as a query argument. */
    @JvmStatic
    fun sharedKey(): String = sharedNumber().toString()

    /** [dataNumber] as a query argument. */
    @JvmStatic
    fun dataKey(): String = dataNumber().toString()

    /** Whether the workspace in use asks to unlock (outside incognito). */
    @JvmStatic
    fun currentLocked(): Boolean = !SyncSettings.secretMode.value && isLocked(currentNumber(), all())

    /**
     * The colour the browser shows for the space in use: null in Main and in incognito (nothing to show), else the
     * workspace's colour (0 when it has none).
     */
    @JvmStatic
    fun currentColor(): Int? {
        if (SyncSettings.secretMode.value) return null
        val number = currentNumber()
        if (number == MAIN) return null
        return all().firstOrNull { it.number == number }?.color
    }

    /** The name of workspace [number] ([mainName] for Main). */
    fun nameOf(number: Int, mainName: String): String =
        if (number == MAIN) mainName else all().firstOrNull { it.number == number }?.name ?: mainName

    private fun save(context: Context, list: List<Workspace>) {
        WorkspaceSettings.edit(context) { set(WorkspaceSettings.list, format(list)) }
    }

    /** Adds a workspace [name] and returns its number. */
    fun create(context: Context, name: String, color: Int = 0): Int {
        val list = all()
        val number = nextNumber(WorkspaceSettings.next.value, list)
        WorkspaceSettings.edit(context) {
            set(WorkspaceSettings.list, format(added(list, number, name, color)))
            set(WorkspaceSettings.next, number + 1)
        }
        return number
    }

    fun rename(context: Context, number: Int, name: String) = save(context, changed(all(), number) { it.copy(name = name) })

    fun recolor(context: Context, number: Int, color: Int) = save(context, changed(all(), number) { it.copy(color = color) })

    fun setLocked(context: Context, number: Int, locked: Boolean) = save(context, changed(all(), number) { it.copy(locked = locked) })

    /**
     * Takes workspace [number] off the list (never Main, never the one in use) and marks its WebView profile for
     * deletion; its rows are deleted by the caller ([com.mycompany.app.web.WorkspaceData.deleteRows]).
     */
    fun remove(context: Context, number: Int): Boolean {
        if (number < FIRST_NUMBER || number == currentNumber()) return false
        val tabs = parseTabs(WorkspaceSettings.tabs.value) - number
        WorkspaceSettings.edit(context) {
            set(WorkspaceSettings.list, format(removed(all(), number)))
            set(WorkspaceSettings.tabs, formatTabs(tabs))
            set(WorkspaceSettings.drop, (dropList(WorkspaceSettings.drop.value) + number).distinct().joinToString(","))
        }
        return true
    }

    /**
     * Makes [number] the workspace in use, written to the file at once (the app restarts next): the open tab of the
     * space left ([leftTab]) is kept, and the one of [number] is returned (0 when it was never open).
     */
    fun switchTo(context: Context, number: Int, leftTab: Int): Int {
        val tabs = HashMap(parseTabs(WorkspaceSettings.tabs.value))
        tabs[currentNumber()] = leftTab
        val tab = tabs[number] ?: 0
        WorkspaceSettings.editNow(context) {
            set(WorkspaceSettings.current, number)
            set(WorkspaceSettings.tabs, formatTabs(tabs))
        }
        return tab
    }
}
