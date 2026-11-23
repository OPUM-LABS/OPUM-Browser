package com.mycompany.app.pref.store

/**
 * Order of the address-bar suggestion sources ([BrowserSettings.sugOrder]), stored as their numbers joined by commas,
 * nearest the address bar first: [ENGINE] (Google / DuckDuckGo) and the [BrowserSettings.sugType] bits 2 recent
 * searches, 4 history, 8 bookmarks, 16 quick access. With the bar at the top the first one is the topmost group, with
 * the bar at the bottom the bottommost one.
 */
object SuggestOrder {
    const val ENGINE = 1
    /** All sources in the order the list always had. */
    val ALL = listOf(2, ENGINE, 4, 8, 16)
    val DEFAULT: String = format(ALL)

    /** The stored order: unknown or repeated entries dropped, missing sources added at the end in their default place. */
    fun parse(text: String?): List<Int> {
        val order = ArrayList<Int>()
        text?.split(',')?.forEach { part ->
            val value = part.trim().toIntOrNull()
            if (value != null && value in ALL && value !in order) order.add(value)
        }
        for (value in ALL) if (value !in order) order.add(value)
        return order
    }

    fun format(order: List<Int>): String = order.joinToString(",")

    /** The current order. */
    fun current(): List<Int> = parse(BrowserSettings.sugOrder.value)
}
