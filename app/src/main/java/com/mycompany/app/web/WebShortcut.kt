package com.mycompany.app.web

import android.app.Activity
import com.mycompany.app.pref.store.TtsSettings
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefTts

/**
 * Home-screen shortcut of a page: the shortcut's data is the address (or the start page when there is none); opens it
 * in the browser and closes. Invisible, so a plain activity; the class name stays because pinned shortcuts reference it.
 */
class WebShortcut : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainApp.loadSettingsOnce(applicationContext, resources)
        openShortcut(this, intent.data?.toString() ?: WebViewActivity.homePageUrl(), true)
    }

    companion object {
        /** Starts the browser on [address] (`start` marks a shortcut launch) and closes the whole task of [activity]. */
        @JvmStatic
        fun openShortcut(activity: Activity?, address: String?, start: Boolean) {
            activity!!.finishAffinity()
            val browser = MainUtil.browserIntent(activity)
            browser.putExtra("EXTRA_PATH", address)
            browser.putExtra("EXTRA_START", start)
            if (TtsSettings.appKeypad.value) browser.putExtra("EXTRA_KEYPAD", true)
            if (MainUtil.isLockEnabled()) {
                MainApp.from(activity)?.skipLockOnce = true
                browser.putExtra("EXTRA_LAUNCH_LOCK", true)
            }
            activity.startActivity(browser)
        }
    }
}
