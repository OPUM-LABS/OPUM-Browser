package com.mycompany.app.web

import android.content.ComponentCallbacks2
import com.mycompany.app.web.MemoryRelease.Step
import org.junit.Assert.assertEquals
import org.junit.Test

@Suppress("DEPRECATION")
class MemoryReleaseTest {

    @Test
    fun stepsForTrimLevels() {
        assertEquals(Step.NONE, MemoryRelease.stepFor(ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE))
        assertEquals(Step.HIDDEN, MemoryRelease.stepFor(ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW))
        assertEquals(Step.ALL, MemoryRelease.stepFor(ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL))
        // only going out of sight releases nothing
        assertEquals(Step.NONE, MemoryRelease.stepFor(ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN))
        assertEquals(Step.HIDDEN, MemoryRelease.stepFor(ComponentCallbacks2.TRIM_MEMORY_BACKGROUND))
        assertEquals(Step.ALL, MemoryRelease.stepFor(ComponentCallbacks2.TRIM_MEMORY_MODERATE))
        assertEquals(Step.ALL, MemoryRelease.stepFor(ComponentCallbacks2.TRIM_MEMORY_COMPLETE))
    }

    @Test
    fun tabsToRelease() {
        assertEquals(emptyList<Int>(), MemoryRelease.tabsToRelease(6, 2, Step.NONE))
        // the current tab and its neighbours stay
        assertEquals(listOf(0, 4, 5), MemoryRelease.tabsToRelease(6, 2, Step.HIDDEN))
        assertEquals(listOf(0, 1, 3, 4, 5), MemoryRelease.tabsToRelease(6, 2, Step.ALL))
        assertEquals(listOf(2, 3), MemoryRelease.tabsToRelease(4, 0, Step.HIDDEN))
        assertEquals(emptyList<Int>(), MemoryRelease.tabsToRelease(1, 0, Step.ALL))
    }
}
