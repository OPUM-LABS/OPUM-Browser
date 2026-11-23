package com.mycompany.app.ui.browser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BarGroupsTest {

    @Test
    fun parseAndFormatRoundTrip() {
        val text = "2000:3,44,45;2001:12,13"
        val groups = BarGroups.parse(text)
        assertEquals(listOf(3, 44, 45), groups[2000])
        assertEquals(listOf(12, 13), groups[2001])
        assertEquals(text, BarGroups.format(groups))
    }

    @Test
    fun parseSkipsBadPartsCapsAndFiltersMembers() {
        val groups = BarGroups.parse(" ;x:1;5:3,4;2002:1,2,68,3,3,4,5,6,7,8,2003;2004:")
        assertEquals(setOf(2002, 2004), groups.keys)
        // address bar, menu button, blank, duplicates and groups are dropped; at most five
        assertEquals(listOf(3, 4, 5, 6, 7), groups[2002])
        assertEquals(emptyList<Int>(), groups[2004])
        assertTrue(BarGroups.parse(null).isEmpty())
        assertTrue(BarGroups.parse("").isEmpty())
    }

    @Test
    fun whoCanJoin() {
        assertFalse(BarGroups.canJoin(0))
        assertFalse(BarGroups.canJoin(1))
        assertFalse(BarGroups.canJoin(2))
        assertFalse(BarGroups.canJoin(68))
        assertFalse(BarGroups.canJoin(BarGroups.NEW))
        assertFalse(BarGroups.canJoin(BarGroups.FIRST))
        assertTrue(BarGroups.canJoin(3))
        assertTrue(BarGroups.canJoin(30))
    }

    @Test
    fun nextIdSkipsUsedOnes() {
        assertEquals(2000, BarGroups.nextId(emptyList()))
        assertEquals(2002, BarGroups.nextId(listOf(2000, 2001, 26, 27)))
    }

    @Test
    fun resolveEmptySingleAndFull() {
        val groups = mapOf(2000 to emptyList(), 2001 to listOf(44), 2002 to listOf(3, 4))
        assertNull(BarGroups.resolve(2000, groups))
        assertEquals(44, BarGroups.resolve(2001, groups))
        assertEquals(2002, BarGroups.resolve(2002, groups))
        assertNull(BarGroups.resolve(2003, groups))
        assertEquals(26, BarGroups.resolve(26, groups))
    }
}
