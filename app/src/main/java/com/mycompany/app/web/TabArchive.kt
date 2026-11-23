package com.mycompany.app.web

import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.book.DbBookTab
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZtwoSettings

/**
 * Archiving inactive tabs (2026-11-21, Settings > Tabs > "Archive inactive tabs", off by default). Tabs not used for the
 * chosen number of days leave the tab list; they stay in the database (see [DbBookTab.archiveTabs]) and are listed under
 * "Archived tabs" in the tab overview, where they can be opened again (with their back history) or deleted.
 *
 * Never archived: incognito tabs, the current tab and the tabs next to it, sites on "Sites kept in memory", and a group
 * unless every tab of it is inactive (a group goes as a whole). A tab whose last use was never recorded (`lastUsed` is
 * kept since 2026-11-19) counts as used when archiving was turned on, so turning it on does not empty the list at once.
 */
object TabArchive {
    const val DAY = 86_400_000L

    /** The choices of the setting (days; 0 = off). */
    @JvmField val DAYS = intArrayOf(0, 7, 14, 21, 30)

    /** The choices for deleting archived tabs (days in the archive; 0 = keep). */
    @JvmField val DELETE_DAYS = intArrayOf(0, 30, 90)

    /** Not more often than this while the browser keeps running. */
    private const val CHECK_EVERY = 30 * 60_000L
    private var lastCheck = 0L

    /** When [tab] was last used for the archive rule: its recorded last use, else when archiving was turned on. */
    fun usedAt(tab: WebTabItem, since: Long): Long = if (tab.lastUsed > 0) tab.lastUsed else since

    /**
     * The tabs to archive, in list order. [tabs] is the tab list, [currentIndex] the current tab, [days] the setting,
     * [since] when archiving was turned on, [kept] whether an address is on the keep-in-memory list.
     */
    fun pick(tabs: List<WebTabItem?>, currentIndex: Int, days: Int, now: Long, since: Long, kept: (String?) -> Boolean): List<WebTabItem> {
        if (days <= 0 || tabs.isEmpty()) return emptyList()
        val cutoff = now - days * DAY
        val out = ArrayList<WebTabItem>()
        var start = 0
        while (start < tabs.size) {
            // a run: one single tab, or the tabs of one group next to each other
            val first = tabs[start]
            var end = start + 1
            if (first != null && first.groupId != 0L) {
                while (end < tabs.size && tabs[end]?.groupId == first.groupId) end++
            }
            val run = tabs.subList(start, end)
            val nearCurrent = currentIndex in (start - 1)..end
            val inactive = run.all { it != null && it.uid > 0 && usedAt(it, since) in 1 until cutoff && !kept(it.tabUrl) }
            if (!nearCurrent && inactive) run.forEach { out.add(it!!) }
            start = end
        }
        return out
    }

    /** [tabs] in order, re-linked the way the activity and the database expect (each tab's parent is the previous one). */
    fun relink(tabs: List<WebTabItem>): ArrayList<WebTabItem?> {
        val out = ArrayList<WebTabItem?>()
        var previous = -1L
        for ((index, tab) in tabs.withIndex()) {
            tab.parentId = previous
            tab.position = index
            previous = tab.uid
            out.add(tab)
        }
        return out
    }

    /** Remembers when archiving was turned on (for tabs with no recorded last use). */
    @JvmStatic
    fun onSettingChanged(context: android.content.Context?, days: Int) {
        if (days > 0 && ZtwoSettings.tabArchiveSince.value == 0L) {
            ZtwoSettings.edit(context) { set(ZtwoSettings.tabArchiveSince, System.currentTimeMillis()) }
        }
        lastCheck = 0L
    }

    /** From the browser's onResume: checks a little later (the tab list and the page are up by then). */
    @JvmStatic
    fun schedule(activity: WebViewActivity) {
        val handler = activity.mainHandler ?: return
        handler.postDelayed({ run(activity) }, 3000L)
    }

    /** Deletes archived tabs past "Delete archived tabs after", then archives the inactive tabs of the live tab list. */
    private fun run(activity: WebViewActivity) {
        val now = System.currentTimeMillis()
        if (now - lastCheck < CHECK_EVERY) return
        if (activity.isPaused || activity.isClosing) return
        val context = activity.dialogsState.appContext ?: return
        lastCheck = now
        val keepDays = ZtwoSettings.tabArchiveDelete.value
        if (keepDays > 0) MainApp.runInBackground(context, Runnable { DbBookTab.deleteArchivedBefore(context, now - keepDays * DAY) })
        val days = ZtwoSettings.tabArchive.value
        if (days <= 0 || SyncSettings.secretMode.value) return
        // archiving on by default (2026-11-23): count from the first check, not from tabs without a recorded last use
        if (ZtwoSettings.tabArchiveSince.value == 0L) { onSettingChanged(context, days); return }
        // not under an open dialog or the tab overview (it works on its own copy of the list)
        if (activity.tabsState.composeOverview != null || activity.isAnyDialogOpen()) { lastCheck = 0L; return }
        val tabs = activity.tabsState.tabItems ?: return
        val current = tabs.getOrNull(activity.currentTabIndex) ?: return
        val picked = pick(tabs, activity.currentTabIndex, days, now, ZtwoSettings.tabArchiveSince.value) {
            MemoryRelease.keptInMemory(context, it)
        }
        if (picked.isEmpty()) return
        val gone = picked.map { it.uid }.toHashSet()
        val before = tabs.filterNotNull().associate { it.uid to it.parentId }
        val list = relink(tabs.filterNotNull().filter { it.uid !in gone })
        DbBookTab.archiveTabs(context, gone, now)
        // the new links of the tabs that stay (the database keeps the order as links)
        val changed = ArrayList<WebTabItem?>()
        for (any in list) {
            val tab = any ?: continue
            if (before[tab.uid] != tab.parentId) {
                val copy = WebTabItem()
                copy.uid = tab.uid; copy.parentId = tab.parentId; copy.groupId = tab.groupId; copy.groupName = tab.groupName; copy.groupColor = tab.groupColor
                changed.add(copy)
            }
        }
        if (changed.isNotEmpty()) DbBookTab.updateTabs(context, changed)
        // free the pages of archived tabs that are still alive
        for (tab in picked) {
            val frame = tab.webFrame
            if (frame != null && frame.alive && frame !== activity.tabsState.currentFrame) frame.destroyFrame(null)
            tab.webFrame = null
        }
        activity.applyTabList(list.indexOf(current).coerceAtLeast(0), list)
        MainUtil.showToastText(context, activity.resources.getQuantityString(R.plurals.tabs_archived, picked.size, picked.size))
    }
}
