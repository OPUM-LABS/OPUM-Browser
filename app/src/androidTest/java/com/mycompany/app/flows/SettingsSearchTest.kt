package com.mycompany.app.flows

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.flows.Browser.find
import com.mycompany.app.flows.Browser.has
import com.mycompany.app.flows.Browser.str
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Settings search end to end (2026-11-21): every setting must be findable; a search result opens its page. */
@RunWith(AndroidJUnit4::class)
@LargeTest
class SettingsSearchTest {
    @Test
    fun searchFindsAndOpensASetting() {
        Browser.start()
        Browser.menu(str(R.string.setting))
        find(By.desc(str(R.string.abc_searchview_description_search))).click()
        val field = find(By.clazz("android.widget.EditText"))
        field.text = "archive"
        val result = find(By.text(str(R.string.tab_archive)))
        result.click()
        // the result opens its settings page with the setting on screen (the search field is gone)
        assertTrue("search closed", Browser.device.wait(Until.gone(By.clazz("android.widget.EditText")), 5_000) == true)
        assertTrue("setting shown", has(By.text(str(R.string.tab_archive))))
    }
}
