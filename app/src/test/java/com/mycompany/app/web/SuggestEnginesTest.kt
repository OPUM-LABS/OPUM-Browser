package com.mycompany.app.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SuggestEnginesTest {
    @Test
    fun openSearchAnswer() {
        val list = SuggestEngines.parse("""["weather",["weather","weather zurich","Weather"," weather today "]]""", "Weather")
        assertEquals(listOf("weather zurich", "weather today"), list.map { it.title })
        assertTrue(list.all { it.url == null })
    }

    @Test
    fun googleSitesKeepTheirAddress() {
        val list = SuggestEngines.parse("""["sbb",["sbb","https://www.sbb.ch/","sbb fahrplan"],["","SBB CFF FFS",""],[],{}]""", "sbb")
        assertEquals(SuggestEngines.Suggestion("SBB CFF FFS", "https://www.sbb.ch/"), list[0])
        assertEquals(SuggestEngines.Suggestion("sbb fahrplan", null), list[1])
    }

    @Test
    fun siteWithoutDescriptionShowsItsHost() {
        val list = SuggestEngines.parse("""["x",["https://example.com/a"]]""", "x")
        assertEquals("example.com", list[0].title)
    }

    @Test
    fun naverAnswer() {
        val list = SuggestEngines.parse("""{"query":["weather"],"items":[[["weather"],["weather 뜻"],["bbc weather"]]]}""", "weather")
        assertEquals(listOf("weather 뜻", "bbc weather"), list.map { it.title })
    }

    @Test
    fun brokenOrEmptyAnswers() {
        assertTrue(SuggestEngines.parse(null, "a").isEmpty())
        assertTrue(SuggestEngines.parse("<html>", "a").isEmpty())
        assertTrue(SuggestEngines.parse("[\"a\"", "a").isEmpty())
    }

    @Test
    fun atMostFifteen() {
        val terms = (1..30).joinToString(",") { "\"t$it\"" }
        assertEquals(SuggestEngines.MAX, SuggestEngines.parse("[\"t\",[$terms]]", "t").size)
    }

    @Test
    fun everyBuiltInEngineHasAService() {
        for (i in 0..9) assertTrue(SuggestEngines.url(i, "a b", "de-CH")!!.startsWith("https://"))
        assertNull(SuggestEngines.url(100, "a", null))
        assertTrue(SuggestEngines.url(4, "a b", "de-CH")!!.endsWith("hl=de-CH&q=a+b"))
    }
}
