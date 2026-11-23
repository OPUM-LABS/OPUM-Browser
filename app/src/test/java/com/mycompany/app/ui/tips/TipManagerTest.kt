package com.mycompany.app.ui.tips

import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.SyncSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/** The rules of when tips come (settings changed in memory only: no context). */
class TipManagerTest {

    @Before
    fun setUp() {
        ReadSettings.editLocal { set(ReadSettings.tipsOn, true); set(ReadSettings.tipsSeen, ""); set(ReadSettings.tipSessions, 0) }
        SyncSettings.editLocal { set(SyncSettings.splash, false) }
        TipManager.newProcessForTest()
        TipManager.sessionStarted(null)
    }

    private fun load(n: Int, tabs: Int = 1): Tip? {
        var tip: Tip? = null
        repeat(n) { tip = TipManager.onPageLoaded("https://example.com/$it/${System.nanoTime()}", tabs) }
        return tip
    }

    @Test
    fun firstTipAfterThreePagesThenNoMoreThisStart() {
        assertNull(load(2))
        val tip = load(1)
        assertEquals(Tip.HOLD_BUTTONS, tip)
        TipManager.shown(null, tip!!)
        assertNull(load(5, tabs = 3))
    }

    @Test
    fun tabsTipWhenTwoTabsAreOpen() {
        assertEquals(Tip.TABS, load(3, tabs = 2))
    }

    @Test
    fun laterTipsWaitForLaterStarts() {
        TipManager.shown(null, Tip.HOLD_BUTTONS)
        TipManager.shown(null, Tip.TABS)
        TipManager.newProcessForTest(); TipManager.sessionStarted(null) // 2nd start
        assertEquals(Tip.CUSTOMIZE, load(3))
        TipManager.shown(null, Tip.CUSTOMIZE)
        TipManager.newProcessForTest(); TipManager.sessionStarted(null) // 3rd start
        assertEquals(Tip.INCOGNITO, load(3))
    }

    @Test
    fun sameUrlCountsOnceAndOffMeansNever() {
        repeat(5) { TipManager.onPageLoaded("https://same.example/", 1) }
        assertNull(TipManager.onPageLoaded("https://same.example/", 1))
        ReadSettings.editLocal { set(ReadSettings.tipsOn, false) }
        assertNull(load(5))
        assertNull(TipManager.forEvent(Tip.EDGE_SWIPE))
    }

    @Test
    fun eventTipOnceAndSettingsTipOnSecondVisit() {
        assertEquals(Tip.EDGE_SWIPE, TipManager.forEvent(Tip.EDGE_SWIPE))
        TipManager.shown(null, Tip.EDGE_SWIPE)
        assertNull(TipManager.forEvent(Tip.EDGE_SWIPE))
        assertNull(TipManager.onSettingsOpened(null))
        assertEquals(Tip.SETTINGS_SEARCH, TipManager.onSettingsOpened(null))
        TipManager.shown(null, Tip.SETTINGS_SEARCH)
        assertNull(TipManager.onSettingsOpened(null))
        TipManager.reset(null)
        assertEquals(Tip.EDGE_SWIPE, TipManager.forEvent(Tip.EDGE_SWIPE))
    }

    @Test
    fun nothingDuringTheSetupWizard() {
        SyncSettings.editLocal { set(SyncSettings.splash, true) }
        assertNull(load(5))
    }

    @Test
    fun pendingEventTipHoldsBackAmbientTips() {
        assertEquals(Tip.EDGE_SWIPE, TipManager.forEvent(Tip.EDGE_SWIPE))
        assertNull(load(5))
        TipManager.shown(null, Tip.EDGE_SWIPE)
        assertEquals(Tip.HOLD_BUTTONS, load(1))
    }
}
