package com.mycompany.app.ui.settings

/**
 * One entry of the settings search index ([SettingSearchIndex]): the page to open, the option on it, and the texts the
 * search matches (kept in lower case once, so typing does not lower-case the whole index again).
 * Moved out of the old `setting/SettingListAdapter` (2026-11-17), whose View adapter was already gone. Its 27 other
 * constructors and the View fields (view type, icon, check state, colours, row position) were never used and are gone
 * (2026-11-22).
 */
class SettingItem(
    /** The settings page ([SettingsPage]). */
    @JvmField val itemId: Int,
    /** Position of the option on its page (generated rows from 1, hand-written ones from 1000). */
    @JvmField val optionId: Int,
    @JvmField val titleText: String,
    /** The option's summary, or the page name when the option has none. */
    @JvmField val summaryText: String?,
) {
    @JvmField val titleLower: String = titleText.lowercase()
    @JvmField val summaryLower: String? = summaryText?.takeIf { it.isNotEmpty() }?.lowercase()
}
