package com.mycompany.app.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PreloadTest {
    private fun target(text: String?, httpsOnly: Boolean = true, search: Boolean = true) = Preload.targetFor(
        text, httpsOnly, search,
        isUrl = { it.contains("://") },
        looksLikeAddress = { it.contains('.') && !it.contains(' ') },
        searchUrl = { "https://search.example/?q=$it" },
    )

    @Test
    fun modeAndConnection() {
        assertTrue(Preload.allowed(Preload.WIFI, unmetered = true, secret = false, saveData = false, dataSaver = false))
        assertFalse(Preload.allowed(Preload.WIFI, unmetered = false, secret = false, saveData = false, dataSaver = false))
        assertTrue(Preload.allowed(Preload.ALWAYS, unmetered = false, secret = false, saveData = false, dataSaver = false))
        assertFalse(Preload.allowed(Preload.OFF, unmetered = true, secret = false, saveData = false, dataSaver = false))
    }

    @Test
    fun secretModeAndDataSavingAlwaysStopIt() {
        assertFalse(Preload.allowed(Preload.ALWAYS, unmetered = true, secret = true, saveData = false, dataSaver = false))
        assertFalse(Preload.allowed(Preload.ALWAYS, unmetered = true, secret = false, saveData = true, dataSaver = false))
        assertFalse(Preload.allowed(Preload.ALWAYS, unmetered = true, secret = false, saveData = false, dataSaver = true))
    }

    @Test
    fun addressesAsEnterOpensThem() {
        assertEquals("https://example.com/a", target("https://example.com/a"))
        assertEquals("https://example.com", target(" example.com "))
        // the WebView prefetches only over HTTPS
        assertNull(target("example.com", httpsOnly = false))
        assertNull(target("http://example.com"))
    }

    @Test
    fun searchesOnlyWhenTheEngineSeesTheTypingAnyway() {
        assertEquals("https://search.example/?q=weather", target("weather"))
        assertNull(target("weather", search = false))
        // an address is still preloaded without suggestions
        assertEquals("https://example.com", target("example.com", search = false))
    }

    @Test
    fun nothingForShortTextOrOtherSchemes() {
        assertNull(target("ab"))
        assertNull(target(null))
        assertNull(target("javascript://alert(1)"))
        assertNull(target("file:///sdcard/a.html"))
    }
}
