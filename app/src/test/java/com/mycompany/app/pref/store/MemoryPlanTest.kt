package com.mycompany.app.pref.store

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MemoryPlanTest {

    @Test
    fun automaticFollowsThePhonesMemory() {
        assertEquals(MemoryPlan.Limits(3, 3, 1), MemoryPlan.limitsFor(MemoryPlan.AUTO, 2800))
        assertEquals(MemoryPlan.Limits(4, 4, 2), MemoryPlan.limitsFor(MemoryPlan.AUTO, 3700))
        assertEquals(MemoryPlan.Limits(5, 6, 2), MemoryPlan.limitsFor(MemoryPlan.AUTO, 7600))
        assertEquals(MemoryPlan.Limits(8, 8, 3), MemoryPlan.limitsFor(MemoryPlan.AUTO, 11500))
        // memory unknown: a middle choice
        assertEquals(MemoryPlan.Limits(4, 4, 2), MemoryPlan.limitsFor(MemoryPlan.AUTO, 0))
    }

    @Test
    fun fixedModesAndCustom() {
        assertEquals(MemoryPlan.Limits(3, 3, 1), MemoryPlan.limitsFor(MemoryPlan.SAVE, 16000))
        assertEquals(MemoryPlan.Limits(10, 10, 7), MemoryPlan.limitsFor(MemoryPlan.FAST, 2000))
        assertNull(MemoryPlan.limitsFor(MemoryPlan.CUSTOM, 8000))
    }

    @Test
    fun oldDefaultsBecomeAutomaticChangedValuesStayCustom() {
        assertEquals(MemoryPlan.AUTO, MemoryPlan.initialMode(true, 5, 5, 2))
        assertEquals(MemoryPlan.CUSTOM, MemoryPlan.initialMode(false, 5, 5, 2))
        assertEquals(MemoryPlan.CUSTOM, MemoryPlan.initialMode(true, 0, 5, 2))
        assertEquals(MemoryPlan.CUSTOM, MemoryPlan.initialMode(true, 5, 10, 2))
        assertEquals(MemoryPlan.CUSTOM, MemoryPlan.initialMode(true, 5, 5, 0))
    }
}

class MemoryPlanPrerenderTest {
    @org.junit.Test
    fun prerenderOnlyWithRoomAndNotWhileSavingData() {
        org.junit.Assert.assertTrue(MemoryPlan.prerenderFor(MemoryPlan.FAST, 2000, false))
        org.junit.Assert.assertTrue(MemoryPlan.prerenderFor(MemoryPlan.AUTO, 7600, false))
        org.junit.Assert.assertFalse(MemoryPlan.prerenderFor(MemoryPlan.AUTO, 3700, false))
        org.junit.Assert.assertFalse(MemoryPlan.prerenderFor(MemoryPlan.SAVE, 16000, false))
        org.junit.Assert.assertFalse(MemoryPlan.prerenderFor(MemoryPlan.CUSTOM, 16000, false))
        org.junit.Assert.assertFalse(MemoryPlan.prerenderFor(MemoryPlan.FAST, 16000, true))
    }
}

class MemoryPlanRendererTest {
    @Test
    fun rendererGoesFirstUnlessTabsShouldStayReady() {
        org.junit.Assert.assertTrue(MemoryPlan.yieldRendererFor(MemoryPlan.SAVE, 16000, false))
        org.junit.Assert.assertTrue(MemoryPlan.yieldRendererFor(MemoryPlan.CUSTOM, 8000, false))
        org.junit.Assert.assertTrue(MemoryPlan.yieldRendererFor(MemoryPlan.AUTO, 3700, false))
        org.junit.Assert.assertFalse(MemoryPlan.yieldRendererFor(MemoryPlan.AUTO, 7600, false))
        org.junit.Assert.assertFalse(MemoryPlan.yieldRendererFor(MemoryPlan.FAST, 2000, false))
        // memory unknown: keep it as before
        org.junit.Assert.assertFalse(MemoryPlan.yieldRendererFor(MemoryPlan.AUTO, 0, false))
    }

    @Test
    fun neverWhileBackgroundPlayIsOn() {
        org.junit.Assert.assertFalse(MemoryPlan.yieldRendererFor(MemoryPlan.SAVE, 2000, true))
    }
}
