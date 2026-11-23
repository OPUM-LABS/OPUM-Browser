package com.mycompany.app.pref.store

import android.content.Context
import android.content.ContextWrapper
import com.mycompany.app.pref.PrefCore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * The typed settings store over a real [PrefCore] without a file (no context: values live only in its JSON object), so
 * it runs on the JVM. Covers load / edit / editLocal + save / remove / reset, the flows and [Setting.isDefault].
 */
class SettingStoreTest {
    private class TestStore(private val core: PrefCore) : SettingStore() {
        override fun open(context: Context): PrefCore = core
        val flag = bool("mFlag") { true }
        val count = int("mCount") { 7 }
        val ratio = float("mRatio") { 1.5f }
        val stamp = long("mStamp") { 0L }
        val name = string("mName") { "" }
        var afterLoadRuns = 0
        override fun afterLoad() { afterLoadRuns++ }
    }

    private lateinit var core: PrefCore
    private lateinit var store: TestStore
    private val context: Context = ContextWrapper(null)

    @Before
    fun setUp() {
        core = PrefCore(null, "Test")
        store = TestStore(core)
    }

    private fun stored(key: String) = core.json?.get(key)

    @Test
    fun startsWithDefaults() {
        assertTrue(store.flag.value)
        assertEquals(7, store.count.value)
        assertEquals(1.5f, store.ratio.value)
        assertEquals(0L, store.stamp.value)
        assertEquals("", store.name.value)
        assertTrue(store.count.isDefault)
    }

    @Test
    fun loadReadsStoredValuesAndKeepsDefaultsForMissingKeys() {
        core.putBool("mFlag", false)
        core.putInt(42, "mCount")
        core.putLong(123L, "mStamp")
        store.load(context)
        assertFalse(store.flag.value)
        assertEquals(42, store.count.value)
        assertEquals(123L, store.stamp.value)
        assertEquals(1.5f, store.ratio.value)
        assertEquals(1, store.afterLoadRuns)
    }

    @Test
    fun editChangesValueAndFileTogether() {
        store.edit(context) { set(store.count, 9); set(store.name, "abc") }
        assertEquals(9, store.count.value)
        assertEquals(9, stored("mCount")!!.asInt)
        assertEquals("abc", stored("mName")!!.asString)
        assertNull(stored("mFlag"))
    }

    @Test
    fun editWithoutContextChangesOnlyTheValue() {
        store.edit(null) { set(store.count, 3) }
        assertEquals(3, store.count.value)
        assertNull(stored("mCount"))
    }

    @Test
    fun editLocalThenSaveWritesLater() {
        store.editLocal { set(store.ratio, 2.25f) }
        assertNull(stored("mRatio"))
        store.save(context, listOf(store.ratio))
        assertEquals(2.25f, stored("mRatio")!!.asFloat)
    }

    @Test
    fun removeDropsTheKeyButKeepsTheValue() {
        store.edit(context) { set(store.count, 11) }
        store.remove(context, listOf(store.count))
        assertNull(stored("mCount"))
        assertEquals(11, store.count.value)
    }

    @Test
    fun resetDropsTheKeyAndGoesBackToTheDefault() {
        store.edit(context) { set(store.flag, false) }
        store.reset(context, listOf(store.flag))
        assertNull(stored("mFlag"))
        assertTrue(store.flag.value)
        assertTrue(store.flag.isDefault)
    }

    @Test
    fun flowFollowsEveryChange() {
        val flow = store.count.flow
        store.editLocal { set(store.count, 1) }
        assertEquals(1, flow.value)
        store.edit(context) { set(store.count, 2) }
        assertEquals(2, flow.value)
        store.reset(context, listOf(store.count))
        assertEquals(7, flow.value)
    }
}
