package com.mycompany.app.web.filter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** uBO's `!#if` / `!#else` / `!#endif` / `!#include` handling for an Android WebView. */
class FilterListPreprocessorTest {
    @Test
    fun conditions() {
        assertTrue(FilterListPreprocessor.condition("env_mobile"))
        assertTrue(FilterListPreprocessor.condition("env_chromium"))
        assertFalse(FilterListPreprocessor.condition("env_firefox"))
        assertFalse(FilterListPreprocessor.condition("cap_html_filtering"))
        assertTrue(FilterListPreprocessor.condition("!cap_html_filtering"))
        assertFalse(FilterListPreprocessor.condition("ext_ubol"))
        assertTrue(FilterListPreprocessor.condition("env_firefox || env_chromium"))
        assertFalse(FilterListPreprocessor.condition("env_mobile && env_safari"))
        assertTrue(FilterListPreprocessor.condition("!(env_safari || ext_ubol) && env_mobile"))
        assertFalse(FilterListPreprocessor.condition("unknown_token"))
        assertFalse(FilterListPreprocessor.condition("(env_mobile")) // malformed -> false, like an unknown token
    }

    @Test
    fun ifElseNesting() {
        val text = """
            a
            !#if env_firefox
            firefox-only
            !#else
            not-firefox
            !#if env_mobile
            mobile
            !#endif
            !#endif
            !#if cap_html_filtering
            html
            !#if env_mobile
            html-mobile
            !#endif
            !#else
            no-html
            !#endif
            z
        """.trimIndent()
        val out = FilterListPreprocessor.evaluate(text).lines().filter { it.isNotBlank() }
        assertEquals(listOf("a", "not-firefox", "mobile", "no-html", "z"), out)
    }

    @Test
    fun includeUrls() {
        val list = "https://raw.githubusercontent.com/uBlockOrigin/uAssets/master/filters/filters.txt"
        assertEquals(
            "https://raw.githubusercontent.com/uBlockOrigin/uAssets/master/filters/filters-2025.txt",
            FilterListPreprocessor.includeUrl(list, "filters-2025.txt"),
        )
        assertEquals(
            "https://raw.githubusercontent.com/uBlockOrigin/uAssets/master/filters/sub/x.txt",
            FilterListPreprocessor.includeUrl("$list?v=1", "sub/x.txt"),
        )
        assertNull(FilterListPreprocessor.includeUrl(list, "../secret.txt"))
        assertNull(FilterListPreprocessor.includeUrl(list, "https://evil.example/x.txt"))
        assertNull(FilterListPreprocessor.includeUrl(list, "/etc/passwd"))
        assertNull(FilterListPreprocessor.includeUrl(list, "a b.txt"))
    }

    @Test
    fun assembleIncludesRecursively() {
        val base = "https://lists.test/f/"
        val parts = mapOf(
            base + "one.txt" to "one-rule\n!#include two.txt\n",
            base + "two.txt" to "!#if env_firefox\nff\n!#else\ntwo-rule\n!#endif\n",
        )
        val fetched = ArrayList<String>()
        val text = "main\n!#if env_mobile\n!#include one.txt\n!#endif\n!#if env_firefox\n!#include never.txt\n!#endif\n" +
            "!#include ../escape.txt\n!#include missing.txt\nend"
        val out = FilterListPreprocessor.assemble(text, base + "main.txt", { url -> fetched.add(url); parts[url] })
        val rules = out.lines().filter { it.isNotBlank() && !it.startsWith("!") }
        assertEquals(listOf("main", "one-rule", "two-rule", "end"), rules)
        assertFalse("inactive include must not be fetched", fetched.contains(base + "never.txt"))
        assertFalse(fetched.any { it.contains("escape") })
    }

    @Test
    fun noDirectivesUnchanged() {
        val text = "||ads.example.com^\nexample.com##.ad\n"
        assertEquals(text, FilterListPreprocessor.evaluate(text))
        assertTrue(FilterListPreprocessor.includes(text).isEmpty())
    }

    @Test
    fun othersRulesAreDropped() {
        val text = "a.com##.ad\n" +
            "igorslab.de###extras, .herald-main-content, .mfp-content:first-of-type:others()\n" +
            "igorslab.de/community/#@##extras, .herald-main-content:others()\n" +
            "! comment about :others()\n" +
            "||ads.example^\n"
        val out = FilterListPreprocessor.evaluate(text)
        assertTrue(out.contains("a.com##.ad"))
        assertTrue(out.contains("||ads.example^"))
        assertTrue(out.contains("! comment about :others()"))
        assertFalse(out.contains("herald-main-content"))
    }
}
