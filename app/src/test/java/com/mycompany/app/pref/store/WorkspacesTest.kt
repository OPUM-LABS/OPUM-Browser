package com.mycompany.app.pref.store

import com.mycompany.app.pref.store.Workspaces.Workspace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    @Test
    fun changingOneWorkspaceLeavesTheOthers() {
        val list = listOf(Workspace(2, "A"), Workspace(3, "B"))
        val out = Workspaces.changed(list, 3) { it.copy(name = "C", locked = true) }
        assertEquals(listOf(Workspace(2, "A"), Workspace(3, "C", locked = true)), out)
        assertTrue(Workspaces.isLocked(3, out))
        assertFalse(Workspaces.isLocked(2, out))
        assertFalse(Workspaces.isLocked(Workspaces.MAIN, out))
    }

    @Test
    fun newNamesDoNotRepeat() {
        assertEquals("Work", Workspaces.freeName("Work", emptyList()))
        assertEquals("Work 2", Workspaces.freeName("Work", listOf(Workspace(2, "work"))))
        assertEquals("Work 3", Workspaces.freeName("Work", listOf(Workspace(2, "Work"), Workspace(3, "Work 2"))))
    }

    @Test
    fun openTabOfEachSpaceSurvivesSavingAndLoading() {
        val tabs = mapOf(0 to 5, 2 to 0, 7 to 12)
        assertEquals(tabs, Workspaces.parseTabs(Workspaces.formatTabs(tabs)))
        assertTrue(Workspaces.parseTabs("junk").isEmpty())
    }

    @Test
    fun onlyWorkspaceNumbersAreDropped() {
        assertEquals(listOf(2, 5), Workspaces.dropList("2, 5,x,0,1,5"))
        assertTrue(Workspaces.dropList(null).isEmpty())
    }

    @Test
    fun leftoversOfUnlistedWorkspacesAreFound() {
        val list = listOf(Workspace(2, "Work"))
        // 0 Main and 1 incognito are never workspaces; 2 is listed; 5 and 3 were made after the backup
        assertEquals(listOf(3, 5), Workspaces.unlisted(setOf(0, 1, 2, 5, 3), list))
        assertTrue(Workspaces.unlisted(setOf(0, 1, 2), list).isEmpty())
    }

    @Test
    fun profilesNoWorkspaceOwnsAreFound() {
        val list = listOf(Workspace(2, "Work"), Workspace(4, "Home"))
        // -1: the default profile and other names (WebProfiles.spaceNumberOf)
        assertEquals(listOf(3, 7), Workspaces.unowned(listOf(-1, 2, 3, 4, 7, 7), list))
        assertTrue(Workspaces.unowned(listOf(-1, 2, 4), list).isEmpty())
    }

    @Test
    fun aRestoreAddsTheBackupsWorkspacesAndKeepsThePhones() {
        val phone = listOf(Workspace(2, "Work", locked = true), Workspace(4, "Home"))
        val backup = listOf(Workspace(2, "Old name"), Workspace(3, "Private"))
        val out = Workspaces.merged(phone, backup)
        // the phone's entry wins for a number both have; the backup's others come back
        assertEquals(listOf(Workspace(2, "Work", locked = true), Workspace(4, "Home"), Workspace(3, "Private")), out)
        assertEquals(5, Workspaces.nextNumber(3, out))
    }

    @Test
    fun theOutlineColourIsKeptWithTheWorkspace() {
        val chosen = Workspaces.Workspace(2, "Work", 0xFF1E88E5.toInt(), outline = 0xFF43A047.toInt(), outlineAlpha = 20, outlinePos = 0.25f)
        val plain = Workspaces.Workspace(3, "Home", 0xFFD81B60.toInt())
        assertEquals(listOf(chosen, plain), Workspaces.parse(Workspaces.format(listOf(chosen, plain))))
        // an older list without the outline fields
        assertEquals(listOf(plain), Workspaces.parse("""[{"n":3,"name":"Home","color":${0xFFD81B60.toInt()},"locked":false}]"""))
    }

    @Test
    fun theOutlineFollowsTheWorkspaceColourUntilOneIsChosen() {
        assertEquals(null, Workspaces.outlineOf(null))
        assertEquals(null, Workspaces.outlineOf(Workspaces.Workspace(2, "Work")))
        assertEquals(0xFF1E88E5.toInt(), Workspaces.outlineOf(Workspaces.Workspace(2, "Work", 0xFF1E88E5.toInt())))
        assertEquals(0xFF43A047.toInt(), Workspaces.outlineOf(Workspaces.Workspace(2, "Work", 0xFF1E88E5.toInt(), outline = 0xFF43A047.toInt())))
    }

    @Test
    fun aLockResetUnlocksOnlyTheLockedWorkspaces() {
        val work = Workspaces.Workspace(2, "Work", locked = true)
        val home = Workspaces.Workspace(3, "Home")
        val bank = Workspaces.Workspace(4, "Bank", locked = true)
        val after = Workspaces.unlocked(listOf(work, home, bank), listOf(2, 4))
        assertEquals(listOf(work.copy(locked = false), home, bank.copy(locked = false)), after)
        assertFalse(after.any { it.locked })
    }
}
