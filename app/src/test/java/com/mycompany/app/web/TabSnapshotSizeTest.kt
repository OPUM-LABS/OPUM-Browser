package com.mycompany.app.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tab overview snapshots: 0.4 of the page, at most 1.5 times as tall as wide (the card crops the rest). */
class TabSnapshotSizeTest {
    @Test
    fun tallPageIsCutToTheCardShape() {
        assertEquals(432 to 648, TabSnapshot.size(1080, 2154))
    }

    @Test
    fun widePageKeepsItsHeight() {
        assertEquals(960 to 400, TabSnapshot.size(2400, 1000))
    }

    @Test
    fun emptyPageHasNoSnapshot() {
        assertNull(TabSnapshot.size(0, 2000))
        assertNull(TabSnapshot.size(1080, 0))
        assertNull(TabSnapshot.size(1, 1))
    }

    @Test
    fun storedSnapshotOnlyForTheSameSite() {
        val key = "https://www.instagram.com/reel/x/falsefalse_2"
        assertTrue(TabSnapshot.sameSite(key, "https://instagram.com/explore/"))
        assertTrue(TabSnapshot.sameSite("https://m.facebook.com/falsetrue_2", "https://www.facebook.com/login"))
        assertFalse(TabSnapshot.sameSite("file:///android_asset/shortcut.htmlfalsefalse_2", "https://play.google.com/store"))
        assertFalse(TabSnapshot.sameSite("https://www.heise.de/falsefalse_2", "https://www.google.com/"))
        assertFalse(TabSnapshot.sameSite(null, "https://www.google.com/"))
        assertTrue(TabSnapshot.sameSite("file:///android_asset/shortcut.htmlfalsefalse_2", "file:///android_asset/shortcut.html"))
        assertFalse(TabSnapshot.sameSite("file:///android_asset/shortcut.htmlfalsefalse_2", "file:///data/page/a.mht"))
    }
}
