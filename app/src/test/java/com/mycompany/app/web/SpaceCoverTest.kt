package com.mycompany.app.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SpaceCoverTest {
    @Test
    fun metaRoundTrip() {
        val meta = SpaceCover.Meta(3, 0, 120, 1080, 2274, 1080, 2400, "https://example.com/a?b=c\nd")
        assertEquals(meta, SpaceCover.parseMeta(SpaceCover.formatMeta(meta)))
    }

    @Test
    fun brokenMetaIsNone() {
        assertNull(SpaceCover.parseMeta(null))
        assertNull(SpaceCover.parseMeta(""))
        // an older picture's data (fewer fields)
        assertNull(SpaceCover.parseMeta("1\n1080\n2274\nhttps://example.com"))
        // no address
        assertNull(SpaceCover.parseMeta("1\n0\n0\n10\n10\n10\n10\n"))
        // empty frame
        assertNull(SpaceCover.parseMeta("1\n0\n50\n10\n50\n10\n100\nhttps://example.com"))
        assertNull(SpaceCover.parseMeta("x\n0\n0\n10\n10\n10\n10\nhttps://example.com"))
    }

    @Test
    fun armedOnlyRightAfterTheSwitch() {
        val now = 1_000_000L
        assertTrue(SpaceCover.armedFor(now - 200, now))
        assertTrue(SpaceCover.armedFor(now, now))
        assertFalse(SpaceCover.armedFor(now - 60_000, now))
        // a clock set back: not this start's
        assertFalse(SpaceCover.armedFor(now + 5_000, now))
        assertFalse(SpaceCover.armedFor(0, now))
    }
}
