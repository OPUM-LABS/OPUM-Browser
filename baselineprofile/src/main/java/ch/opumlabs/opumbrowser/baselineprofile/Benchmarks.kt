package ch.opumlabs.opumbrowser.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Before / after numbers for the baseline profile: cold start and page scrolling, once without any compilation (a
 * fresh sideloaded install) and once with the profile. `./gradlew :baselineprofile:connectedBenchmarkQaAndroidTest`
 * (emulator numbers are for comparison only).
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class Benchmarks {
    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test fun startupNoProfile() = startup(CompilationMode.None())
    @Test fun startupProfile() = startup(CompilationMode.Partial(BaselineProfileMode.Require))
    @Test fun scrollNoProfile() = scroll(CompilationMode.None())
    @Test fun scrollProfile() = scroll(CompilationMode.Partial(BaselineProfileMode.Require))

    private fun startup(mode: CompilationMode) = rule.measureRepeated(
        packageName = TARGET, metrics = listOf(StartupTimingMetric()), compilationMode = mode,
        startupMode = StartupMode.COLD, iterations = 8,
        setupBlock = { pressHome() },
    ) {
        startBrowser()
    }

    private fun scroll(mode: CompilationMode) = rule.measureRepeated(
        packageName = TARGET, metrics = listOf(FrameTimingMetric()), compilationMode = mode,
        startupMode = StartupMode.WARM, iterations = 5,
        setupBlock = { pressHome(); startBrowser(); openPage() },
    ) {
        scrollPage(times = 4)
    }
}
