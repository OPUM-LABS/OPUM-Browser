package com.mycompany.app.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TabArchiveTest {
    private val now = 100L * TabArchive.DAY
    private val since = now - 1 * TabArchive.DAY   // archiving turned on yesterday

    private fun tab(uid: Long, daysAgo: Int?, group: Long = 0L, url: String = "https://site$uid.example/"): WebTabItem {
        val tab = WebTabItem()
        tab.uid = uid
        tab.groupId = group
        tab.tabUrl = url
        tab.lastUsed = if (daysAgo == null) 0L else now - daysAgo * TabArchive.DAY
        return tab
    }

    private fun picked(tabs: List<WebTabItem>, current: Int, days: Int = 7, kept: (String?) -> Boolean = { false }) =
        TabArchive.pick(tabs, current, days, now, since, kept).map { it.uid }

    @Test
    fun offArchivesNothing() {
        val tabs = listOf(tab(1, 40), tab(2, 40), tab(3, 40), tab(4, 40))
        assertEquals(emptyList<Long>(), picked(tabs, 0, days = 0))
    }

    @Test
    fun inactiveSingleTabsGo() {
        val tabs = listOf(tab(1, 1), tab(2, 30), tab(3, 2), tab(4, 10), tab(5, 8))
        // current is tab 1 (index 0): tab 2 is its neighbour and stays
        assertEquals(listOf(4L, 5L), picked(tabs, 0))
    }

    @Test
    fun currentAndNeighboursStay() {
        val tabs = listOf(tab(1, 30), tab(2, 30), tab(3, 30), tab(4, 30), tab(5, 30))
        assertEquals(listOf(1L, 5L), picked(tabs, 2))
    }

    @Test
    fun groupGoesOnlyAsAWhole() {
        val tabs = listOf(tab(1, 0), tab(2, 1), tab(3, 30, group = 9), tab(4, 30, group = 9), tab(5, 30, group = 7), tab(6, 2, group = 7))
        // group 9 is all inactive: goes; group 7 has a tab used 2 days ago: stays
        assertEquals(listOf(3L, 4L), picked(tabs, 0))
    }

    @Test
    fun groupNextToCurrentStays() {
        val tabs = listOf(tab(1, 30, group = 9), tab(2, 30, group = 9), tab(3, 0), tab(4, 30), tab(5, 30))
        assertEquals(listOf(5L), picked(tabs, 2))
    }

    @Test
    fun keptSitesStay() {
        val tabs = listOf(tab(1, 0), tab(2, 0), tab(3, 30, url = "https://keep.example/"), tab(4, 30))
        assertEquals(listOf(4L), picked(tabs, 0, kept = { it?.contains("keep.example") == true }))
    }

    @Test
    fun unrecordedTabsCountFromWhenArchivingWasTurnedOn() {
        val tabs = listOf(tab(1, 0), tab(2, 0), tab(3, null), tab(4, null))
        assertEquals(emptyList<Long>(), picked(tabs, 0))
        // eight days after turning it on they are old enough
        assertEquals(listOf(3L, 4L), TabArchive.pick(tabs, 0, 7, now, now - 8 * TabArchive.DAY) { false }.map { it.uid })
    }

    @Test
    fun relinkChainsTheTabs() {
        val tabs = listOf(tab(5, 0), tab(9, 0), tab(2, 0))
        val list = TabArchive.relink(tabs)
        assertEquals(listOf(-1L, 5L, 9L), list.map { it!!.parentId })
        assertEquals(listOf(0, 1, 2), list.map { it!!.position })
        assertTrue(list.size == 3)
    }
}
