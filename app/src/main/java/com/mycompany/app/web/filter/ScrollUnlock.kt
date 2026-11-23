package com.mycompany.app.web.filter

import android.content.Context
import com.mycompany.app.pref.store.ZtwoSettings

/**
 * Pages that lock scrolling while they show a dialog (cookie consent, sign-in prompt, "open in app") and stay locked
 * when the web filter hides that dialog. `WebNestView` notices swipes that move nothing and asks the page script
 * (`ubo-cosmetic.js`, `__opumScrollCheck`); the browser then offers "Unlock scrolling" (lifts the lock, remembered per
 * site here) or "Show dialog" (the next load of that site without element hiding, so the dialog can be answered).
 */
object ScrollUnlock {
    /** Host whose next top-page load is served without element hiding ("Show dialog"). */
    @Volatile
    private var showOnceHost: String? = null

    private fun hosts(): List<String> =
        (ZtwoSettings.scrollUnlock.value ?: "").split('\n').map { it.trim() }.filter { it.isNotEmpty() }

    /** The user unlocked scrolling on [host] before: lift the lock on every visit. */
    fun remembered(host: String?): Boolean = !host.isNullOrEmpty() && host in hosts()

    fun remember(context: Context?, host: String?) {
        if (host.isNullOrEmpty() || remembered(host)) return
        val list = hosts() + host
        ZtwoSettings.edit(context) { set(ZtwoSettings.scrollUnlock, list.joinToString("\n")) }
    }

    /** Serves the next load of [host] without element hiding. */
    fun showDialogOnce(host: String?) {
        showOnceHost = host
    }

    /** True once for the host set by [showDialogOnce]. */
    fun consumeShowOnce(host: String?): Boolean {
        if (host.isNullOrEmpty() || showOnceHost != host) return false
        showOnceHost = null
        return true
    }
}
