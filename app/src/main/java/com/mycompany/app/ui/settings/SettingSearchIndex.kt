package com.mycompany.app.ui.settings

import android.content.Context
import ch.opumlabs.opumbrowser.R

/**
 * The settings search index (rebuilt 2026-11-20). The rows come from [SettingSearchGenerated], which the build writes
 * from the settings pages themselves (`app/settings-index.gradle`), so a new or moved setting is searchable without
 * touching this file. The hand-written list that was here fell behind (bar transparency, the News page, Favourites,
 * the video player and more could not be found).
 *
 * Only add to [manual] what the build cannot see as a row: controls that are not `SettingRow`-style rows (the sliders
 * in the Layout editor) or rows whose title is computed. Each result shows the page it is on as its second line;
 * opening it scrolls that page to the row with the same title.
 */
object SettingSearchIndex {
    /** Page id, title, summary (0 = the page's name), three ints per entry. */
    private val manual = intArrayOf(
        // the Layout editor's bar panel (tap a bar chip): height and transparency of the top / bottom bar
        SettingsPage.LAYOUT, R.string.color_alpha, 0,
        SettingsPage.LAYOUT, R.string.size_height, 0,
        SettingsPage.LAYOUT, R.string.layout_single, 0,
        // the video player's and the image viewer's settings pages are only titled "Settings"
        SettingsPage.VIDEO, R.string.video, 0,
        SettingsPage.IMAGE, R.string.image, 0,
    )

    /** Page names for pages whose own title says nothing in a result list. */
    private val pageNameOverrides = mapOf(SettingsPage.VIDEO to R.string.video, SettingsPage.IMAGE to R.string.image)

    @JvmStatic
    fun build(context: Context?): ArrayList<SettingItem>? {
        if (context == null) return null
        val pageNames = HashMap<Int, String>()
        val titles = SettingSearchGenerated.pageTitles
        for (i in titles.indices step 2) pageNames[titles[i]] = context.getString(titles[i + 1])
        for ((page, name) in pageNameOverrides) pageNames[page] = context.getString(name)
        // the bars' names with the Layout editor's controls, so "transparency" shows which bars it is about
        val barNames = context.getString(R.string.top_bar) + ", " + context.getString(R.string.bot_bar)

        val out = ArrayList<SettingItem>()
        val seen = HashSet<String>()
        fun add(rows: IntArray, hand: Boolean) {
            for (i in rows.indices step 3) {
                val page = rows[i]
                val title = context.getString(rows[i + 1])
                if (!seen.add("$page/$title")) continue
                val summary = when {
                    rows[i + 2] != 0 -> context.getString(rows[i + 2])
                    hand && page == SettingsPage.LAYOUT -> pageNames[page]?.let { "$it · $barNames" } ?: barNames
                    else -> pageNames[page]
                }
                out.add(SettingItem(page, i / 3 + if (hand) 1000 else 1, title, summary))
            }
        }
        add(SettingSearchGenerated.rows, hand = false)
        add(manual, hand = true)
        return out
    }
}
