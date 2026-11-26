package com.mycompany.app.ui.settings

import ch.opumlabs.opumbrowser.R
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Settings search: options that moved page (the 2026-11-19 regrouping, later pages) are found on their current page.
 * Since 2026-11-20 the search index is generated from the settings pages (`app/settings-index.gradle` →
 * [SettingSearchGenerated]); the hand-written old-page → new-page table (`SettingSearchIndex.pageNow`) it replaced is
 * gone, so these checks read the generated rows.
 */
class SettingPageMoveTest {
    private fun pagesOf(title: Int): Set<Int> {
        val rows = SettingSearchGenerated.rows
        val pages = HashSet<Int>()
        for (i in rows.indices step 3) if (rows[i + 1] == title) pages.add(rows[i])
        return pages
    }

    private fun assertOn(page: Int, title: Int, what: String) {
        val pages = pagesOf(title)
        assertTrue("$what should be on page $page, found on $pages", page in pages)
    }

    @Test
    fun advancedIsSplitUp() {
        assertOn(SettingsPage.MEMORY, R.string.pre_raster, "pre-raster")
        assertOn(SettingsPage.WEB_CONTENT, R.string.block_amp, "AMP")
        assertOn(SettingsPage.SITE_SAFETY, R.string.accept_cookie, "cookies")
        assertOn(SettingsPage.SITE_SAFETY, R.string.java_script, "JavaScript")
        assertOn(SettingsPage.SITE_SAFETY, R.string.block_ssl, "SSL")
    }

    @Test
    fun memoryPageRework() {
        assertOn(SettingsPage.MEMORY, R.string.mem_mode, "memory use")
        assertOn(SettingsPage.MEMORY, R.string.instant_back, "instant back")
        assertOn(SettingsPage.MEMORY, R.string.mem_keep_sites, "sites kept in memory")
        assertOn(SettingsPage.TAB_OPEN, R.string.open_limit, "open tab limit")
        assertOn(SettingsPage.WEB_SCRIPTS, R.string.debug_mode, "web debugging")
    }

    @Test
    fun siteIsSplitUp() {
        assertOn(SettingsPage.SEARCH, R.string.search_engine, "search engine")
        assertOn(SettingsPage.WEB_LINKS, R.string.user_agent, "user agent")
        assertOn(SettingsPage.PRIVACY, R.string.pms_site_settings, "site settings entry")
    }

    @Test
    fun movedRowsOfOtherPages() {
        assertOn(SettingsPage.TEXT, R.string.text_size, "text size")
        assertOn(SettingsPage.ADS, R.string.cookie_block, "cookie banners")
    }

    @Test
    fun siteSettingsPageIsIndexed() {
        assertOn(SettingsPage.SITES, R.string.ss_all_sites, "all sites")
    }

    @Test
    fun everyIndexedPageHasATitle() {
        val titles = SettingSearchGenerated.pageTitles
        val titled = HashSet<Int>()
        for (i in titles.indices step 2) titled.add(titles[i])
        val rows = SettingSearchGenerated.rows
        for (i in rows.indices step 3) {
            assertTrue("page ${rows[i]} has rows but no title", rows[i] in titled)
        }
    }
}
