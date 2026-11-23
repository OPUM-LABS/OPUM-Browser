package com.mycompany.app.web.filter

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Request-type guessing and `$redirect` data URLs of [UboFilter] (plain JVM, no engine needed). */
class UboFilterTest {
    private val anyAccept = mapOf("Accept" to "*/*", "Referer" to "https://news.test/")

    @Test
    fun mainFrameIsDocument() {
        assertEquals(listOf("document"), UboFilter.requestTypes(true, mapOf("Accept" to "text/html"), "https://a.test/"))
    }

    @Test
    fun acceptHeaderDecides() {
        // Headers as Android WebView 156 sends them (no Sec-Fetch-Dest).
        assertEquals(listOf("image"), UboFilter.requestTypes(false, mapOf("Accept" to "image/avif,image/webp,*/*;q=0.8"), "https://a.test/pixel?x=1"))
        assertEquals(listOf("stylesheet"), UboFilter.requestTypes(false, mapOf("Accept" to "text/css,*/*;q=0.1"), "https://a.test/load.php?only=styles"))
        assertEquals(listOf("subdocument"), UboFilter.requestTypes(false, mapOf("accept" to "text/html,application/xhtml+xml"), "https://ads.test/frame"))
        assertEquals(listOf("xmlhttprequest"), UboFilter.requestTypes(false, mapOf("Accept" to "application/json, text/plain"), "https://api.test/v1"))
    }

    @Test
    fun secFetchDestWinsWhenPresent() {
        assertEquals(listOf("script"), UboFilter.requestTypes(false, mapOf("Sec-Fetch-Dest" to "script", "Accept" to "*/*"), "https://a.test/x"))
        assertEquals(listOf("xmlhttprequest"), UboFilter.requestTypes(false, mapOf("sec-fetch-dest" to "empty"), "https://a.test/x.js"))
    }

    @Test
    fun anyAcceptFallsBackToExtensionThenScriptOrXhr() {
        assertEquals(listOf("script"), UboFilter.requestTypes(false, anyAccept, "https://c.amazon-adsystem.com/aax2/apstag.js"))
        assertEquals(listOf("font"), UboFilter.requestTypes(false, anyAccept, "https://f.test/a.woff2?v=3"))
        assertEquals(listOf("media"), UboFilter.requestTypes(false, anyAccept, "https://v.test/seg/1.m4s"))
        assertEquals(listOf("script", "xmlhttprequest"), UboFilter.requestTypes(false, anyAccept, "https://config.aps.amazon-adsystem.com/configs/3023"))
        assertEquals(listOf("script", "xmlhttprequest"), UboFilter.requestTypes(false, null, "https://x.test/"))
    }

    @Test
    fun trustedListsAreUboOwn() {
        assertTrue(UboFilter.isTrustedListUrl("https://raw.githubusercontent.com/uBlockOrigin/uAssets/master/filters/filters.txt"))
        assertTrue(UboFilter.isTrustedListUrl("https://ublockorigin.github.io/uAssets/filters/quick-fixes.txt"))
        assertTrue(!UboFilter.isTrustedListUrl("https://easylist.to/easylist/easylist.txt"))
        assertTrue(!UboFilter.isTrustedListUrl("https://raw.githubusercontent.com/someone/uAssets/master/filters.txt"))
        assertTrue(!UboFilter.isTrustedListUrl(null))
    }

    @Test
    fun engineRuleDropsPorts() {
        assertEquals("10.0.2.2##div.x", UboFilter.engineRule("10.0.2.2:8773##div.x"))
        assertEquals("a.com,b.com#@#.ad", UboFilter.engineRule("a.com:8080,b.com#@#.ad"))
        assertEquals("example.com##+js(set, a, 1)", UboFilter.engineRule("example.com##+js(set, a, 1)"))
        assertEquals("||ads.example.com:8080^", UboFilter.engineRule("||ads.example.com:8080^"))
        assertEquals("##.ad[data-x=\"a:1\"]", UboFilter.engineRule("##.ad[data-x=\"a:1\"]"))
    }

    @Test
    fun dataUrls() {
        val (mime, bytes) = UboFilter.decodeDataUrl("data:application/javascript;base64,KGZ1bmN0aW9uKCl7fSkoKTs=")!!
        assertEquals("application/javascript", mime)
        assertEquals("(function(){})();", String(bytes))
        val (mime2, bytes2) = UboFilter.decodeDataUrl("data:text/plain,hello%20world")!!
        assertEquals("text/plain", mime2)
        assertArrayEquals("hello world".toByteArray(), bytes2)
        assertEquals("image/gif", UboFilter.decodeDataUrl("data:image/gif;base64,R0lGODlhAQABAAAAACw=")!!.first)
        assertNull(UboFilter.decodeDataUrl("https://x.test/"))
        assertNull(UboFilter.decodeDataUrl("data:text/plain;base64,%%%"))
    }
}
