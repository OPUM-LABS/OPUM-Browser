package com.mycompany.app.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderExtractTest {

    private val result = ReaderExtract.Result(
        title = "Bern", byline = "A. Writer", excerpt = "The capital", dir = "ltr", lang = "de",
        content = "<div id=\"readability-page-1\" class=\"page\"><p><b>Bern</b> is the capital.</p><img src=\"/a.jpg\"></div>",
        head = "<meta property=\"og:image\" content=\"https://example.org/og.jpg\"><meta name=\"theme-color\" content=\"#123456\"><title>Bern - Wiki</title>",
    )

    @Test
    fun documentHasHeadTagsLanguageAndArticle() {
        val doc = result.document("https://example.org/wiki/Bern")
        assertEquals("de", doc.select("html").attr("lang"))
        assertEquals("https://example.org/og.jpg", doc.select("meta[property=og:image]").attr("content"))
        assertEquals("#123456", doc.select("meta[name=theme-color]").attr("content"))
        // relative addresses resolve against the page
        assertEquals("https://example.org/a.jpg", doc.select("img").first()!!.absUrl("src"))
    }

    @Test
    fun articleCarriesFieldsAndContent() {
        val doc = result.document("https://example.org/wiki/Bern")
        val article = result.article("https://example.org/wiki/Bern", doc)
        assertEquals("Bern", article.title)
        assertEquals("A. Writer", article.byline)
        assertEquals("readability-page-1", article.articleContent!!.id())
        assertTrue(article.articleContent!!.text().startsWith("Bern is the capital"))
    }

    @Test
    fun parseRejectsNothing() {
        assertNull(ReaderExtract.parse(null))
        assertNull(ReaderExtract.parse("null"))
        assertNull(ReaderExtract.parse(""))
    }

    @Test
    fun youTubeUsesTheOldWay() {
        assertTrue(ReaderExtract.applies("https://www.computerbase.de/news/x.1/"))
        assertEquals(false, ReaderExtract.applies(null))
    }
}
