package com.mycompany.app.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RendererRecoveryTest {
    @Test
    fun debugAddresses() {
        assertTrue(RendererRecovery.isDebugAddress("chrome://kill/"))
        assertTrue(RendererRecovery.isDebugAddress("CHROME://CRASH"))
        assertFalse(RendererRecovery.isDebugAddress("https://example.com/chrome://kill"))
        assertFalse(RendererRecovery.isDebugAddress(null))
    }

    @Test
    fun aNormalPageStaysWhereItWas() {
        assertEquals(1, RendererRecovery.afterDebug(listOf("https://a.example/", "https://b.example/"), 1))
    }

    @Test
    fun goesBackPastDebugPagesNeverLoadingThemAgain() {
        // chrome://kill ended the renderer: the tab goes back to the page before it
        assertEquals(1, RendererRecovery.afterDebug(listOf("https://a.example/", "https://www.google.com/search?q=hallo", "chrome://kill/"), 2))
        assertEquals(0, RendererRecovery.afterDebug(listOf("https://a.example/", "chrome://crash", "chrome://kill/"), 2))
        // nothing but debug pages
        assertEquals(-1, RendererRecovery.afterDebug(listOf("chrome://kill/"), 0))
    }
}
