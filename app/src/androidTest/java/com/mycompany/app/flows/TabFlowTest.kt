package com.mycompany.app.flows

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.flows.Browser.find
import com.mycompany.app.flows.Browser.has
import com.mycompany.app.flows.Browser.str
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Tabs end to end (2026-11-21): new tab from the menu, the overview count, open a card, close a card and undo. */
@RunWith(AndroidJUnit4::class)
@LargeTest
class TabFlowTest {
    @Test
    fun newTabShowsInOverview() {
        Browser.start()
        Browser.overview()
        assertTrue("one tab on a fresh install", has(Browser.tabsHeader(1)))
        Browser.device.pressBack()

        Browser.menu(str(R.string.new_url))
        Browser.overview()
        assertTrue("two tabs after New tab", has(Browser.tabsHeader(2)))
    }

    @Test
    fun closeTabAndUndo() {
        Browser.start()
        Browser.menu(str(R.string.new_url))
        Browser.overview()
        assertTrue(has(Browser.tabsHeader(2)))

        find(By.desc(str(R.string.close))).click()
        assertTrue("closed", has(Browser.tabsHeader(1)))
        find(By.text(str(R.string.undelete))).click()
        assertTrue("undo brings it back", has(Browser.tabsHeader(2)))
    }

    @Test
    fun cardOpensTabAndClosesOverview() {
        Browser.start()
        Browser.menu(str(R.string.new_url))
        Browser.overview()
        // the cards: the big clickable nodes (each card is one node with its title; its close button is its own node)
        val minHeight = Browser.device.displayHeight / 8
        val cards = Browser.device.findObjects(By.clickable(true)).filter { it.visibleBounds.height() > minHeight }
        assertTrue("cards found: ${cards.size}", cards.size >= 2)
        cards[0].click()
        assertTrue("overview closed", Browser.device.wait(Until.gone(By.textStartsWith(str(R.string.tab_items) + " ")), 5_000) == true)
        assertTrue("browser toolbar back", has(By.desc(str(R.string.tab_list))))
    }
}
