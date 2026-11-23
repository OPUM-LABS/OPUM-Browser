package ch.opumlabs.opumbrowser.baselineprofile

import android.content.Intent
import android.net.Uri
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import java.util.regex.Pattern

/** The app under test: the qa build (its own package), passed in by the baseline profile plugin. */
val TARGET: String = InstrumentationRegistry.getArguments().getString("targetAppId") ?: "ch.opumlabs.opumbrowser.qa"

/** A long, stable page with text, pictures and links. */
const val PAGE = "https://en.wikipedia.org/wiki/Web_browser"

private const val ACTIVITY = "com.mycompany.app.web.WebViewActivity"

/** Starts the browser; on a fresh install it skips the onboarding. */
fun MacrobenchmarkScope.startBrowser() {
    startActivityAndWait()
    skipOnboarding()
}

fun MacrobenchmarkScope.skipOnboarding() {
    device.wait(Until.findObject(By.text("Skip setup")), 1_500)?.click()
    device.waitForIdle()
}

/** Opens [PAGE] in the browser and waits for it to show. */
fun MacrobenchmarkScope.openPage() {
    startActivityAndWait { intent: Intent ->
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse(PAGE)
        intent.setClassName(TARGET, ACTIVITY)
    }
    device.wait(Until.hasObject(By.textContains("web browser")), 10_000)
    device.waitForIdle()
}

/** Flings the page down and back up a few times. */
fun MacrobenchmarkScope.scrollPage(times: Int = 3) {
    val w = device.displayWidth; val h = device.displayHeight
    repeat(times) {
        device.swipe(w / 2, h * 3 / 4, w / 2, h / 4, 12)
        device.waitForIdle()
    }
    repeat(times) {
        device.swipe(w / 2, h / 4, w / 2, h * 3 / 4, 12)
        device.waitForIdle()
    }
}

/**
 * Taps slot [index] (0-based) of the bottom toolbar. The default bar has six equal slots: back, forward, home, bookmarks,
 * tabs, menu. By position because the buttons have no accessibility labels and the tab counter is an embedded view
 * that is not marked clickable.
 */
private fun MacrobenchmarkScope.tapBarSlot(index: Int, slots: Int = 6): Boolean {
    val bar = device.wait(Until.findObject(By.res(Pattern.compile(".*:id/web_bot_view"))), 5_000) ?: return false
    val b = bar.visibleBounds
    device.click(b.left + b.width() * (2 * index + 1) / (2 * slots), b.centerY())
    return true
}

/** Shows the toolbars again (they hide while the page scrolls down). */
private fun MacrobenchmarkScope.showBars() {
    val w = device.displayWidth; val h = device.displayHeight
    device.swipe(w / 2, h / 3, w / 2, h * 2 / 3, 20)
    device.waitForIdle()
}

/** Opens the tab overview, scrolls it, and goes back to the page. */
fun MacrobenchmarkScope.tabOverview() {
    showBars()
    if (!tapBarSlot(4)) return
    device.wait(Until.hasObject(By.textContains("Tabs")), 3_000)
    device.waitForIdle()
    val w = device.displayWidth; val h = device.displayHeight
    device.swipe(w / 2, h * 2 / 3, w / 2, h / 3, 15)
    device.waitForIdle()
    device.pressBack()
    device.waitForIdle()
}

/** Opens the main menu and closes it. */
fun MacrobenchmarkScope.mainMenu() {
    showBars()
    if (!tapBarSlot(5)) return
    device.wait(Until.hasObject(By.text("Settings")), 3_000)
    device.pressBack()
    device.waitForIdle()
}
