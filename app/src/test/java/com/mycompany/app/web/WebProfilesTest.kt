package com.mycompany.app.web

import androidx.webkit.Profile
import com.mycompany.app.pref.store.Workspaces
import org.junit.Assert.assertEquals
import org.junit.Test

class WebProfilesTest {
    @Test
    fun mainKeepsTheDefaultProfileSoExistingLoginsStay() {
        assertEquals(Profile.DEFAULT_PROFILE_NAME, WebProfiles.nameFor(Workspaces.MAIN))
    }

    @Test
    fun incognitoUsesTheDefaultProfileOfItsOwnDataFolder() {
        assertEquals(Profile.DEFAULT_PROFILE_NAME, WebProfiles.nameFor(Workspaces.INCOGNITO))
    }

    @Test
    fun eachWorkspaceHasItsOwn() {
        assertEquals("space_2", WebProfiles.nameFor(2))
        assertEquals("space_17", WebProfiles.nameFor(17))
    }

    @Test
    fun workspaceNumberFromProfileName() {
        assertEquals(17, WebProfiles.spaceNumberOf(WebProfiles.nameFor(17)))
        assertEquals(-1, WebProfiles.spaceNumberOf(Profile.DEFAULT_PROFILE_NAME))
        assertEquals(-1, WebProfiles.spaceNumberOf("space_1"))
        assertEquals(-1, WebProfiles.spaceNumberOf("space_x"))
    }
}
