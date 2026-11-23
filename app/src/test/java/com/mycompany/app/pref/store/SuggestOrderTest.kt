package com.mycompany.app.pref.store

import org.junit.Assert.assertEquals
import org.junit.Test

class SuggestOrderTest {
    @Test
    fun defaultKeepsTheOldOrder() {
        assertEquals(listOf(2, 1, 4, 8, 16), SuggestOrder.parse(SuggestOrder.DEFAULT))
        assertEquals(listOf(2, 1, 4, 8, 16), SuggestOrder.parse(null))
    }

    @Test
    fun storedOrderIsKept() {
        assertEquals(listOf(16, 8, 4, 1, 2), SuggestOrder.parse("16,8,4,1,2"))
    }

    @Test
    fun badEntriesAreDroppedAndMissingOnesAdded() {
        assertEquals(listOf(4, 2, 1, 8, 16), SuggestOrder.parse(" 4, x,4, 3, 2"))
    }

    @Test
    fun formatRoundTrips() {
        val order = listOf(8, 1, 16, 2, 4)
        assertEquals(order, SuggestOrder.parse(SuggestOrder.format(order)))
    }
}
