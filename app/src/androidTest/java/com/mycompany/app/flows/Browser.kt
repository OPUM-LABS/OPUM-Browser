package com.mycompany.app.flows

import android.content.Intent
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import ch.opumlabs.opumbrowser.R
import org.junit.Assert.fail

/**
 * Drives the browser in the UI tests (2026-11-21). Runs in the `uitest` package with its data cleared before each test
 * (orchestrator), so every test starts on the onboarding of a fresh install. Finds things by their (translated) labels.
 */
object Browser {
    val device: UiDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    val pkg: String get() = context.packageName

    fun str(id: Int, vararg args: Any): String = context.getString(id, *args)

    /** Starts the browser, skips the onboarding and waits for the bottom toolbar. */
    fun start() {
        device.pressHome()
        context.startActivity(
            Intent().setClassName(pkg, "com.mycompany.app.web.WebViewActivity")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
        )
        device.wait(Until.findObject(By.text(str(R.string.ob_skip_setup))), 15_000)?.click()
        find(By.desc(str(R.string.list_menu)), 15_000)
    }

    fun find(selector: BySelector, timeout: Long = 5_000): UiObject2 =
        device.wait(Until.findObject(selector), timeout) ?: run { fail("not found: $selector"); throw AssertionError() }

    fun has(selector: BySelector, timeout: Long = 5_000): Boolean = device.wait(Until.hasObject(selector), timeout) == true

    fun tapDesc(label: String) = find(By.desc(label)).click()
    fun tapText(label: String) = find(By.text(label)).click()

    /** Main menu > [item]. */
    fun menu(item: String) {
        tapDesc(str(R.string.list_menu))
        tapText(item)
    }

    /** Opens the tab overview from the tab button of the bottom toolbar. */
    fun overview() {
        tapDesc(str(R.string.tab_list))
        find(By.textStartsWith(str(R.string.tab_items)))
    }

    /** "Tabs N" in the overview header. */
    fun tabsHeader(count: Int) = By.text(str(R.string.tab_items) + " " + count)
}
