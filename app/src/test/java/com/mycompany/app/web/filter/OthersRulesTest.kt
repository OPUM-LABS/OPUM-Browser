package com.mycompany.app.web.filter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** uBO's `:others()` rules: collected apart from the engine, matched per page ([OthersRules]). */
class OthersRulesTest {
    private val list = """
        ! Title: test
        igorslab.de###extras, .herald-main-content, .mfp-content:first-of-type:others()
        igorslab.de/community/#@##extras, .herald-main-content, .mfp-content:first-of-type:others()
        a.com,b.com,~sub.b.com##.keep:others()
        shortener.*##.btn:others()
        ! commented.com##.x:others()
        c.com##.plain
    """.trimIndent()

    private val rules = OthersRules.parse(OthersRules.extractLines(list))

    @Test
    fun extractsOnlyOthersRules() {
        assertEquals(4, OthersRules.extractLines(list).size)
        assertNull(OthersRules.parseLine("c.com##.plain"))
        assertNull(OthersRules.parseLine("! a.com##.x:others()"))
        val r = OthersRules.parseLine("igorslab.de###extras, .herald-main-content:others()")!!
        assertEquals("#extras, .herald-main-content", r.selector)
        assertEquals(listOf("igorslab.de"), r.include)
        assertFalse(r.exception)
        assertTrue(OthersRules.parseLine("x.com#@#.a:others()")!!.exception)
    }

    @Test
    fun matchesHostsAndSubdomains() {
        assertEquals(listOf(".keep"), OthersRules.selectorsFor(rules, "https://a.com/page"))
        assertEquals(listOf(".keep"), OthersRules.selectorsFor(rules, "https://www.b.com/"))
        assertTrue(OthersRules.selectorsFor(rules, "https://sub.b.com/").isEmpty()) // ~sub.b.com
        assertTrue(OthersRules.selectorsFor(rules, "https://c.com/").isEmpty())
        assertTrue(OthersRules.selectorsFor(rules, "https://nota.com/").isEmpty())
        assertEquals(listOf(".btn"), OthersRules.selectorsFor(rules, "https://www.shortener.io/x"))
    }

    @Test
    fun pathExceptionCancelsTheRuleThere() {
        val sel = "#extras, .herald-main-content, .mfp-content:first-of-type"
        assertEquals(listOf(sel), OthersRules.selectorsFor(rules, "https://www.igorslab.de/"))
        assertTrue(OthersRules.selectorsFor(rules, "https://www.igorslab.de/community/threads/1").isEmpty())
    }

    @Test
    fun preprocessorKeepsThemOutOfTheEngineInput() {
        val forEngine = FilterListPreprocessor.evaluate(list)
        assertFalse(forEngine.contains("igorslab.de###extras"))
        assertFalse(forEngine.contains("~sub.b.com##.keep"))
        assertTrue(forEngine.contains("! commented.com##.x:others()")) // comments stay
        assertTrue(forEngine.contains("c.com##.plain"))
        // downloads keep them (the engine build collects them)
        assertTrue(FilterListPreprocessor.directives(list).contains("a.com,b.com,~sub.b.com##.keep:others()"))
    }
}
