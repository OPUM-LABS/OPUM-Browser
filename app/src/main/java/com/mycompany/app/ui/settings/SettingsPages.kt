package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import ch.opumlabs.opumbrowser.R

/** Ids of the settings pages. They are the same numbers the search index ([SettingSearchIndex]) uses. */
object SettingsPage {
    const val GENERAL = 1
    // 2 (Advanced) and 3 (Site) were split up on 2026-11-19
    const val BACKUP = 4
    const val START_SEARCH = 5
    const val CLEAN = 7
    const val DOWNLOAD = 8
    const val MEDIA = 9
    const val MEMORY = 10
    const val DISPLAY = 12
    const val LAYOUT = 13
    const val MENU = 14
    const val TAB = 15
    const val CUSTOM = 16
    const val WEB = 18
    const val GESTURE = 19
    const val TRANSLATOR = 20
    const val TTS = 21
    const val SECURITY = 24
    const val SECRET = 25
    const val PASSWORD = 26
    const val PRIVACY = 27
    const val RATE = 30
    const val SHARE = 31
    const val FEEDBACK = 32
    const val INFO = 33
    // pages opened from other places (the news page, the start page, the video player, the image viewer, Toolbars,
    // Appearance); ids added 2026-11-20 so the settings search reaches their rows
    const val NEWS = 34
    const val QUICK = 35
    const val VIDEO = 36
    const val IMAGE = 37
    const val FLOAT = 38
    const val FONT = 39
    // helper pages opened from a row of their parent page (ids added 2026-11-20 for the settings search). Not here on
    // purpose: HomeSettingsActivity (a picker that needs EXTRA_TYPE) and PassListSettingsActivity (saved passwords; the
    // search must not open it past the password lock of PASSWORD)
    const val HAND_AREA = 40
    const val LAND = 41
    const val SWIPE = 42
    const val SENSE = 43
    const val IMAGE_TAP = 44
    const val VIDEO_TAP = 45
    const val SITES = 46
    // 2026-11-24
    const val WORKSPACES = 47
    // 2026-11-25: were dialogs of Toolbars & controls
    const val FULL_SCREEN = 48
    const val ADDR_ICONS = 49
    const val BACK_TO_TOP = 50
}

/** One row on the settings start page: title, one-line summary of what is inside, icon. */
data class SettingsPageEntry(val id: Int, val titleRes: Int, val iconRes: Int, val summaryRes: Int = 0)

/** A group on the settings start page: a header, a colour for its icons (light / dark theme) and its rows. */
data class SettingsPageGroup(val titleRes: Int, val color: Long, val colorDark: Long, val entries: List<SettingsPageEntry>)

/**
 * The start page, in display order (regrouped 2026-11-19: 25 rows in 6 unnamed groups became 17 rows in 5 named,
 * colour-coded groups). Pages without a row here (Layout, Menu, Gestures, Text to speech, Password) are opened from
 * the page they now belong to, and from the settings search.
 */
val settingsPageGroups: List<SettingsPageGroup> = listOf(
    SettingsPageGroup(
        R.string.set_group_basics, 0xFF1E6FD9, 0xFF8AB4F8,
        listOf(
            SettingsPageEntry(SettingsPage.GENERAL, R.string.general, R.drawable.outline_settings_black_24, R.string.set_sum_general),
            SettingsPageEntry(SettingsPage.START_SEARCH, R.string.set_start_search, R.drawable.outline_search_black_24, R.string.set_sum_start_search),
        ),
    ),
    SettingsPageGroup(
        R.string.set_group_look, 0xFFE8710A, 0xFFFDB863,
        listOf(
            SettingsPageEntry(SettingsPage.DISPLAY, R.string.set_appearance, R.drawable.outline_palette_black_24, R.string.set_sum_appearance),
            SettingsPageEntry(SettingsPage.CUSTOM, R.string.set_toolbars, R.drawable.outline_space_dashboard_black_24, R.string.set_sum_toolbars),
            SettingsPageEntry(SettingsPage.TAB, R.string.set_tabs, R.drawable.outline_filter_none_black_24, R.string.set_sum_tabs),
        ),
    ),
    SettingsPageGroup(
        R.string.set_group_privacy, 0xFF188038, 0xFF81C995,
        listOf(
            SettingsPageEntry(SettingsPage.CLEAN, R.string.clean_mode, R.drawable.outline_verified_user_black_24, R.string.set_sum_blocking),
            SettingsPageEntry(SettingsPage.PRIVACY, R.string.set_privacy, R.drawable.outline_security_black_24, R.string.set_sum_privacy),
            SettingsPageEntry(SettingsPage.SECURITY, R.string.set_security_pass, R.drawable.outline_lock_black_24, R.string.set_sum_security),
            SettingsPageEntry(SettingsPage.SECRET, R.string.secret_mode, R.drawable.outline_secret_mode_black_24, R.string.set_sum_incognito),
            SettingsPageEntry(SettingsPage.WORKSPACES, R.string.workspaces, R.drawable.outline_workspaces_black_24, R.string.set_sum_workspaces),
        ),
    ),
    SettingsPageGroup(
        R.string.set_group_content, 0xFF8E24AA, 0xFFD7AEFB,
        listOf(
            SettingsPageEntry(SettingsPage.WEB, R.string.set_web_pages, R.drawable.outline_web_black_24, R.string.set_sum_web),
            SettingsPageEntry(SettingsPage.MEDIA, R.string.media, R.drawable.baseline_play_arrow_black_24, R.string.set_sum_media),
            SettingsPageEntry(SettingsPage.TRANSLATOR, R.string.set_trans_tts, R.drawable.outline_translate_black_24, R.string.set_sum_trans_tts),
            SettingsPageEntry(SettingsPage.DOWNLOAD, R.string.set_downloads, R.drawable.outline_download_black_24, R.string.set_sum_downloads),
        ),
    ),
    SettingsPageGroup(
        R.string.set_group_system, 0xFF5F6368, 0xFFBDC1C6,
        listOf(
            SettingsPageEntry(SettingsPage.MEMORY, R.string.set_performance, R.drawable.outline_energy_savings_leaf_black_24, R.string.set_sum_performance),
            SettingsPageEntry(SettingsPage.BACKUP, R.string.set_backup, R.drawable.outline_autorenew_black_24, R.string.set_sum_backup),
            SettingsPageEntry(SettingsPage.INFO, R.string.set_about, R.drawable.outline_info_black_24, R.string.set_sum_about),
        ),
    ),
)

/**
 * Builds the intent that opens a settings page, or null for pages that are no screens (rate, share, feedback) and for the
 * pages that first need the app lock (see [MainSettingsActivity]). While a page is still a legacy activity the extras
 * [EXTRA_NOTI] / [EXTRA_INDEX] make it scroll to and highlight option [option].
 */
fun settingsPageIntent(context: Context, page: Int, fromSearch: Boolean, option: Int, path: String?, focus: String? = null): Intent? {
    val target: Class<*> = when (page) {
        SettingsPage.GENERAL -> com.mycompany.app.ui.settings.GeneralSettingsActivity::class.java
        SettingsPage.START_SEARCH -> StartSearchSettingsActivity::class.java
        SettingsPage.BACKUP -> com.mycompany.app.ui.settings.BackupSettingsActivity::class.java
        SettingsPage.CLEAN -> com.mycompany.app.ui.settings.CleanSettingsActivity::class.java
        SettingsPage.DOWNLOAD -> com.mycompany.app.ui.settings.DownSettingsActivity::class.java
        SettingsPage.MEDIA -> com.mycompany.app.ui.settings.MediaSettingsActivity::class.java
        SettingsPage.MEMORY -> com.mycompany.app.ui.settings.MemorySettingsActivity::class.java
        SettingsPage.DISPLAY -> com.mycompany.app.ui.settings.DisplaySettingsActivity::class.java
        SettingsPage.LAYOUT -> LayoutSettingsActivity::class.java
        SettingsPage.MENU -> MenuSettingsActivity::class.java
        SettingsPage.TAB -> com.mycompany.app.ui.settings.TabSettingsActivity::class.java
        SettingsPage.CUSTOM -> com.mycompany.app.ui.settings.CompositionSettingsActivity::class.java
        SettingsPage.WEB -> com.mycompany.app.ui.settings.WebSettingsActivity::class.java
        SettingsPage.GESTURE -> GestureSettingsActivity::class.java
        SettingsPage.TRANSLATOR -> com.mycompany.app.ui.settings.TransSettingsActivity::class.java
        SettingsPage.TTS -> com.mycompany.app.ui.settings.TtsSettingsActivity::class.java
        SettingsPage.SECURITY -> com.mycompany.app.ui.settings.SecureSettingsActivity::class.java
        SettingsPage.SECRET -> com.mycompany.app.ui.settings.SecretSettingsActivity::class.java
        SettingsPage.PASSWORD -> com.mycompany.app.ui.settings.PasswordSettingsActivity::class.java
        SettingsPage.PRIVACY -> com.mycompany.app.ui.settings.PrivacySettingsActivity::class.java
        SettingsPage.INFO -> InfoSettingsActivity::class.java
        SettingsPage.NEWS -> NewsSettingsActivity::class.java
        SettingsPage.QUICK -> QuickSettingsActivity::class.java
        SettingsPage.VIDEO -> VideoSettingsActivity::class.java
        SettingsPage.IMAGE -> ImageSettingsActivity::class.java
        SettingsPage.FLOAT -> FloatSettingsActivity::class.java
        SettingsPage.FONT -> FontSettingsActivity::class.java
        SettingsPage.HAND_AREA -> HandAreaSettingsActivity::class.java
        SettingsPage.LAND -> LandSettingsActivity::class.java
        SettingsPage.SWIPE -> SwipeSettingsActivity::class.java
        SettingsPage.SENSE -> SenseSettingsActivity::class.java
        SettingsPage.IMAGE_TAP -> ImageTapSettingsActivity::class.java
        SettingsPage.VIDEO_TAP -> VideoTapSettingsActivity::class.java
        SettingsPage.SITES -> SiteSettingsActivity::class.java
        SettingsPage.WORKSPACES -> WorkspaceSettingsActivity::class.java
        SettingsPage.FULL_SCREEN -> FullScreenSettingsActivity::class.java
        SettingsPage.ADDR_ICONS -> AddrIconSettingsActivity::class.java
        SettingsPage.BACK_TO_TOP -> BackToTopSettingsActivity::class.java
        else -> return null
    }
    val intent = Intent(context, target)
    if (fromSearch) {
        intent.putExtra(EXTRA_NOTI, true)
        intent.putExtra(EXTRA_INDEX, option)
        // the page scrolls to the row with this title and highlights it (SettingsScaffold, 2026-11-19)
        if (!focus.isNullOrBlank()) intent.putExtra(com.mycompany.app.ui.components.EXTRA_FOCUS, focus)
    }
    // These pages read the page the user came from (feedback text, site settings ...).
    if (path != null) intent.putExtra(EXTRA_PATH, path)
    return intent
}

const val EXTRA_NOTI = "EXTRA_NOTI"
const val EXTRA_INDEX = "EXTRA_INDEX"
const val EXTRA_PATH = "EXTRA_PATH"
