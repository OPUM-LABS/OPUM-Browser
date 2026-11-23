package com.mycompany.app.ui.state

import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** [AppState] values read and assign like the old `MainApp` fields, and the dark-UI flow follows. */
class AppStateTest {
    @After
    fun tearDown() {
        AppState.darkUi = false
        AppState.darkWeb = false
    }

    @Test
    fun darkUiAssignmentReachesTheFlow() {
        AppState.darkUi = true
        assertTrue(AppState.darkUi)
        assertTrue(AppState.darkUiFlow.value)
        AppState.darkUi = false
        assertFalse(AppState.darkUiFlow.value)
    }

    @Test
    fun darkWebIsSeparateFromDarkUi() {
        AppState.darkWeb = true
        assertFalse(AppState.darkUi)
        assertTrue(AppState.darkWeb)
    }
}
