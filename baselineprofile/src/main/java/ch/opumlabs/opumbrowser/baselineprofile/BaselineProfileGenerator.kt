package ch.opumlabs.opumbrowser.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Records the code a typical session runs (start, a page, scrolling it, the tab overview, the menu) into the app's
 * baseline profile, and the start alone into its startup profile: `./gradlew :app:generateQaBaselineProfile` (writes app/src/main/generated/baselineProfiles/).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BaselineProfileGenerator {
    @get:Rule
    val rule = BaselineProfileRule()

    /** The whole session: goes into the baseline profile (compiled at install). */
    @Test
    fun session() = rule.collect(packageName = TARGET) {
        pressHome()
        startBrowser()
        openPage()
        scrollPage()
        tabOverview()
        mainMenu()
    }

    /** Only the start: also the startup profile (which code goes into the first dex file). */
    @Test
    fun startup() = rule.collect(packageName = TARGET, includeInStartupProfile = true) {
        pressHome()
        startBrowser()
    }
}
