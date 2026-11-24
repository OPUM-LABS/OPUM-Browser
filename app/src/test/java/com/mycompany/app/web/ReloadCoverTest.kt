package com.mycompany.app.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReloadCoverTest {
    @Test
    fun keepsTheTabsLeftMostRecently() {
        val (keep, drop) = ReloadCover.keepOrder(listOf("b", "c", "d"), "a", 3)
        assertEquals(listOf("a", "b", "c"), keep)
        assertEquals(listOf("d"), drop)
    }

    @Test
    fun aTabLeftAgainMovesToTheFrontOnce() {
        val (keep, drop) = ReloadCover.keepOrder(listOf("b", "a", "c"), "a", 5)
        assertEquals(listOf("a", "b", "c"), keep)
        assertTrue(drop.isEmpty())
    }

    @Test
    fun pictureFitsOnlyTheSameShape() {
        assertTrue(ReloadCover.fits(1080, 2000, 1080, 2000))
        // the bars came or went: a few percent taller or shorter still fits
        assertTrue(ReloadCover.fits(1080, 2000, 1080, 2060))
        // the phone was turned
        assertFalse(ReloadCover.fits(1080, 2000, 2000, 1080))
        assertFalse(ReloadCover.fits(0, 0, 1080, 2000))
    }
}

class ReloadCoverMatchTest {
    @Test
    fun onlyAPictureOfThePageThatLoadsAgain() {
        assertTrue(ReloadCover.matches("https://a.example/x", 1200, "https://a.example/x/", 1210))
        // the tab went on to another page since the picture
        assertFalse(ReloadCover.matches("https://www.google.com/search?q=hallo", 0, "https://buymeacoffee.com/opum_labs", 0))
        // scrolled on since the picture
        assertFalse(ReloadCover.matches("https://a.example/x", 0, "https://a.example/x", 900))
        assertFalse(ReloadCover.matches(null, 0, "https://a.example/x", 0))
    }
}
