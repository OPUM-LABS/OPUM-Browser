package com.mycompany.app.pref.store

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

    /** [dataNumber] as a query argument. */
    @JvmStatic
    fun dataKey(): String = dataNumber().toString()
}
