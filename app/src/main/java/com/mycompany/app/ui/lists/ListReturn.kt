package com.mycompany.app.ui.lists

/**
 * "The list screen was closed" for whoever opened it. The page dialogs (ad allow list, popups, blocked links, blocked
 * images, JavaScript, memory, translation exceptions) used to open the list as a dialog and refresh their switches in
 * its dismiss listener; they now open [SiteListActivity] / [SavedSitesActivity] and register the refresh here.
 * One pending callback at a time (only one list screen is open at once); it runs once, on the main thread, when the
 * list screen finishes.
 */
object ListReturn {
    private var pending: (() -> Unit)? = null

    fun set(onClosed: (() -> Unit)?) {
        pending = onClosed
    }

    fun fire() {
        val callback = pending
        pending = null
        try {
            callback?.invoke()
        } catch (_: Exception) {
        }
    }
}
