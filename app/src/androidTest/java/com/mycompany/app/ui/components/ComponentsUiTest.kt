package com.mycompany.app.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.theme.OpumTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests of shared components (2026-11-17), run on a device / emulator:
 * `./gradlew connectedDebugAndroidTest`. They render the components alone, without the app's activities or data.
 */
@RunWith(AndroidJUnit4::class)
class ComponentsUiTest {
    @get:Rule
    val compose = createComposeRule()

    private val pinchText get() = InstrumentationRegistry.getInstrumentation().targetContext.getString(R.string.guide_pinch)

    @Test
    fun pinchGuideShowsAndFadesOut() {
        val state = GuideState()
        var gone = false
        compose.setContent { OpumTheme(darkTheme = false) { PinchGuideCard(state, onGone = { gone = true }) } }
        compose.onNodeWithText(pinchText).assertExists()
        compose.runOnIdle { state.hideNow(true) }
        compose.waitUntil(3_000) { gone }
        compose.onNodeWithText(pinchText).assertDoesNotExist()
        assertTrue(gone)
    }

    @Test
    fun settingSwitchRowToggles() {
        compose.setContent {
            OpumTheme(darkTheme = false) {
                var on by remember { mutableStateOf(false) }
                SettingSwitchRow(title = "Test switch", checked = on, onCheckedChange = { on = it })
            }
        }
        val row = compose.onNodeWithText("Test switch")
        row.assertExists()
        compose.onNode(androidx.compose.ui.test.isToggleable()).assertIsOff().performClick().assertIsOn()
    }
}
