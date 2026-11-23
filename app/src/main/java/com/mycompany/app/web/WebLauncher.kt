package com.mycompany.app.web

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil

/**
 * Entry for web links and searches from other apps (`VIEW` of http / https, `WEB_SEARCH`, `SEND` of text): works out the
 * address and hands it to the browser, then closes. It shows nothing, so it is a plain activity with a transparent
 * theme rather than part of the old screen hierarchy. The class name stays because it is the default-browser entry and
 * is referenced by the system.
 */
class WebLauncher : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainApp.loadSettingsOnce(applicationContext, resources)
        val soul = intent.getBooleanExtra("EXTRA_SOUL", false)
        try {
            when (intent.action) {
                "android.intent.action.WEB_SEARCH" -> {
                    open(MainUtil.searchUrlFor(intent.getStringExtra("query")!!), false, soul)
                }
                "android.intent.action.SEND" -> {
                    val shared = if ("text/plain" == intent.type) intent.getStringExtra("android.intent.extra.TEXT") else null
                    open(MainUtil.normalizeUrl(null, firstLink(shared)), false, soul)
                }
                else -> {
                    val data = intent.data
                    open(data?.toString() ?: WebViewActivity.homePageUrl(), true, soul)
                }
            }
        } catch (_: Exception) {
            open(WebViewActivity.homePageUrl(), true, false)
        }
    }

    /** The web address inside shared text (up to the next space), or the whole text when there is none. */
    private fun firstLink(input: String?): String? {
        if (input == null || input.length < 8) return input
        val https = input.indexOf("https://")
        val http = input.indexOf("http://")
        val start = when {
            https != -1 && http != -1 -> minOf(https, http)
            https != -1 -> https
            else -> http
        }
        if (start == -1) return input
        val end = input.indexOf(' ', start)
        return if (end == -1) input.substring(start) else input.substring(start, end)
    }

    private fun open(address: String?, external: Boolean, soul: Boolean) {
        val context = applicationContext
        val browser = MainUtil.browserIntent(context)
        browser.putExtra("EXTRA_PATH", address)
        if (external) browser.putExtra("EXTRA_EXT", true)
        if (!soul && MainUtil.isLockEnabled()) {
            MainApp.from(context)?.skipLockOnce = true
            browser.putExtra("EXTRA_LAUNCH_LOCK", true)
        }
        browser.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(browser)
        finish()
    }
}
