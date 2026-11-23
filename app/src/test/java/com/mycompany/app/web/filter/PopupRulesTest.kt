package com.mycompany.app.web.filter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Splitting `$popup` rules out of filter lists, and pop-up checks with the engine. */
class PopupRulesTest {
    @Test
    fun splitLines() {
        assertEquals(null to "||ads.example^", PopupRules.splitLine("||ads.example^\$popup"))
        assertEquals(null to "||ads.example^\$third-party", PopupRules.splitLine("||ads.example^\$popup,third-party"))
        assertEquals(
            "/earn.php?z=\$subdocument" to "/earn.php?z=",
            PopupRules.splitLine("/earn.php?z=\$popup,subdocument"),
        )
        assertEquals(null to "@@||good.example^\$domain=a.com", PopupRules.splitLine("@@||good.example^\$popup,domain=a.com"))
        assertEquals(null to "||x.example^", PopupRules.splitLine("||x.example^\$popup,redirect=noopjs"))
        // rules without popup, cosmetic rules, comments, regex patterns with $ inside: untouched
        assertNull(PopupRules.splitLine("||ads.example^\$script"))
        assertNull(PopupRules.splitLine("example.com##.popup"))
        assertNull(PopupRules.splitLine("! popup comment"))
        assertNull(PopupRules.splitLine("/ad\$popup/"))
        assertEquals(null to "/ad[0-9]\$/", PopupRules.splitLine("/ad[0-9]\$/\$popup"))
    }

    @Test
    fun splitText() {
        val text = "! t\n||a.example^\n||pop.example^\$popup\n||both.example^\$popup,script\nexample.com##.ad\n"
        val s = PopupRules.split(text)
        assertEquals(2, s.popupRules)
        assertTrue(s.main.contains("||a.example^\n"))
        assertFalse(s.main.contains("pop.example"))
        assertTrue(s.main.contains("||both.example^\$script"))
        assertTrue(s.main.contains("example.com##.ad"))
        assertEquals("||pop.example^\n||both.example^\n", s.popup)
        val none = PopupRules.split("||a.example^\n")
        assertEquals("||a.example^\n", none.main)
        assertEquals(0, none.popupRules)
    }

    @Test
    fun engineChecks() {
        assumeTrue(UboEngine.loadLibrary())
        val list = "||ads.example^\n||pop.example^\$popup,third-party\n@@||pop.example/ok^\$popup\n"
        val s = PopupRules.split(list)
        UboEngine.create(listOf(s.main))!!.use { main ->
            UboEngine.create(listOf(s.popup))!!.use { pop ->
                // pop-up engine: third-party pop-up blocked, first-party not, exception honoured
                assertTrue(pop.check("https://pop.example/x", "https://news.test/", "other").blocks)
                assertFalse(pop.check("https://pop.example/x", "https://pop.example/", "other").blocks)
                assertFalse(pop.check("https://pop.example/ok/1", "https://news.test/", "other").blocks)
                // main engine: a plain block rule also blocks the address as a document (a pop-up to an ad server)
                assertTrue(main.check("https://ads.example/landing", "https://news.test/", "document").blocks)
                // the $popup-only rule did not leak into the main engine as "block everything"
                assertFalse(main.check("https://pop.example/x", "https://news.test/", "script").blocks)
            }
        }
    }
}
