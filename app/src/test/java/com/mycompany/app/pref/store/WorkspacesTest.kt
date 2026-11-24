package com.mycompany.app.pref.store

import com.mycompany.app.pref.store.Workspaces.Workspace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspacesTest {
    @Test
    fun listSurvivesSavingAndLoading() {
        val list = listOf(Workspace(2, "Work", 0xFF3366CC.toInt(), locked = true), Workspace(5, "Family \"home\""))
        assertEquals(list, Workspaces.parse(Workspaces.format(list)))
    }

    @Test
    fun nothingStoredIsJustMain() {
        assertTrue(Workspaces.parse(null).isEmpty())
        assertTrue(Workspaces.parse("").isEmpty())
        assertTrue(Workspaces.parse("not json").isEmpty())
    }

    @Test
    fun mainIncognitoAndDuplicatesAreNotWorkspaces() {
        val json = """[{"n":0,"name":"x"},{"n":1,"name":"y"},{"n":3,"name":"A"},{"n":3,"name":"B"}]"""
        assertEquals(listOf(Workspace(3, "A")), Workspaces.parse(json))
    }

    @Test
    fun numbersAreNeverUsedTwice() {
        val list = listOf(Workspace(2, "A"), Workspace(3, "B"))
        assertEquals(4, Workspaces.nextNumber(Workspaces.FIRST_NUMBER, list))
        // 4 was deleted: the counter remembers it, the next one is 5
        assertEquals(5, Workspaces.nextNumber(5, list))
        assertEquals(Workspaces.FIRST_NUMBER, Workspaces.nextNumber(0, emptyList()))
    }

    @Test
    fun addingAndRemoving() {
        val list = Workspaces.added(listOf(Workspace(2, "A")), 3, "B")
        assertEquals(listOf(2, 3), list.map { it.number })
        assertEquals(listOf(3), Workspaces.removed(list, 2).map { it.number })
    }

    @Test
    fun aDeletedCurrentWorkspaceFallsBackToMain() {
        val list = listOf(Workspace(2, "A"))
        assertEquals(2, Workspaces.resolveCurrent(2, list))
        assertEquals(Workspaces.MAIN, Workspaces.resolveCurrent(7, list))
        assertEquals(Workspaces.MAIN, Workspaces.resolveCurrent(Workspaces.MAIN, list))
    }

    @Test
    fun incognitoUsesItsOwnRowsWhateverWorkspaceIsOpen() {
        assertEquals(Workspaces.INCOGNITO, Workspaces.dataNumberFor(true, 4))
        assertEquals(4, Workspaces.dataNumberFor(false, 4))
        assertEquals(Workspaces.MAIN, Workspaces.dataNumberFor(false, Workspaces.MAIN))
    }
}
