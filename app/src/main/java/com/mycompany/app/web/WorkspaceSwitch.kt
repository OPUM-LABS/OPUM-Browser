package com.mycompany.app.web

import android.app.Activity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.RequestCode
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.Workspaces

/**
 * Opening another workspace from the browser (2026-11-24). A locked one asks for the app's lock first. Then the open
 * page's state, the open tab and a picture of it ([SpaceCover]) are kept, and the browser screen opens again in the
 * new space, its own tabs, data and WebView profile, without restarting the app (2026-11-25). Leaving incognito this
 * way runs incognito's usual exit, which restarts the app (its data is cleared as set).
 */
object WorkspaceSwitch {
    /** The workspace waiting for the lock screen's answer. */
    @Volatile
    private var pending = -1

    /** The browser screen being left for another space: it saves nothing more. */
    @Volatile
    private var leavingScreen: java.lang.ref.WeakReference<WebViewActivity>? = null

    /** Set while the browser screen is rebuilt for another space ([takeRelaunch]). */
    @Volatile
    private var relaunching = false

    /**
     * Whether the browser screen being created is the one rebuilt for another space; asked once, at its creation. It
     * then starts fresh (no saved state, no link of the old screen's start), like after a restart.
     */
    @JvmStatic
    fun takeRelaunch(): Boolean {
        val was = relaunching
        relaunching = false
        return was
    }

    /** Whether [wa] is being left for another space (it must not save its tabs, page states or pictures now). */
    @JvmStatic
    fun isLeaving(wa: WebViewActivity): Boolean = leavingScreen?.get() === wa

    /** Opens workspace [number] (Main is [Workspaces.MAIN]). */
    fun open(wa: WebViewActivity, number: Int) {
        val incognito = SyncSettings.secretMode.value
        if (number == Workspaces.currentNumber() && !incognito) return
        if (number != Workspaces.MAIN && number !in Workspaces.all().map { it.number }) return
        val lockType = SecretSettings.lockType.value
        if (lockType != 0 && Workspaces.isLocked(number, Workspaces.all())) {
            pending = number
            val ask = Runnable {
                val intent = MainUtil.lockIntent(wa.dialogsState.appContext, lockType)
                // verify only: the lock screen offers "Cancel", not the lock reset
                intent.putExtra("EXTRA_TYPE", 3)
                wa.launchExternalForResult(intent, RequestCode.UNLOCK_SPACE)
            }
            // the picture of the space being left is taken now: after the lock screen the page is not drawn yet
            if (incognito) {
                ask.run()
            } else {
                pictureTaken = true
                SpaceCover.keep(wa, Workspaces.currentNumber(), wa.currentTabIndex, ask)
            }
            return
        }
        pictureTaken = false
        enter(wa, number)
    }

    /** The lock screen's answer for [open]. */
    fun onUnlockResult(wa: WebViewActivity, resultCode: Int) {
        val number = pending
        pending = -1
        if (resultCode == Activity.RESULT_OK && number >= 0) enter(wa, number)
        pictureTaken = false
    }

    /** The picture of the space being left was taken before the lock screen ([open]). */
    private var pictureTaken = false

    private fun enter(wa: WebViewActivity, number: Int) {
        val context = wa.dialogsState.appContext ?: return
        val incognito = SyncSettings.secretMode.value
        if (incognito) {
            // the space's open tab is the one kept when it was left; incognito's own exit restarts the browser
            switchNumber(context, number, SyncSettings.normalIndex.value)
            // unlocked here (or never locked): the restart does not ask again
            if (MainUtil.isLockEnabled() && !SecretSettings.lockSkip.value) {
                SecretSettings.editNow(context) { set(SecretSettings.lockSkip, true) }
            }
            SpaceCover.arm(context, number)
            wa.switchSecretMode(false)
            return
        }
        if (isLeaving(wa)) return
        // from now on the screen being left writes nothing more (it would land in the new space)
        leavingScreen = java.lang.ref.WeakReference(wa)
        val leaving = Workspaces.currentNumber()
        val leftTab = wa.currentTabIndex
        val handler = wa.mainHandler ?: android.os.Handler(android.os.Looper.getMainLooper())
        // the open page's state and the space's picture are written first, still under the space being left
        var waiting = 2
        var done = false
        val go = Runnable {
            if (done) return@Runnable
            done = true
            relaunch(wa, context, number, leftTab)
        }
        val one = Runnable {
            waiting--
            if (waiting <= 0) go.run()
        }
        handler.postDelayed(go, 1000L)
        if (pictureTaken) one.run() else SpaceCover.keep(wa, leaving, leftTab, one)
        val page = wa.currentPage
        val item = wa.tabItemAt(leftTab)
        if (page == null || item == null || page.blankPending) {
            one.run()
            return
        }
        page.saveTabState(wa.tabsState.currentFrame, item.uid, item.tabUrl, object : WebNestView.WebBundleListener {
            override fun onBundle() {
                handler.post(one)
            }
        })
    }

    /** Makes [number] the space in use; [leftTab] is the open tab of the one left. */
    private fun switchNumber(context: android.content.Context, number: Int, leftTab: Int) {
        val tab = Workspaces.switchTo(context, number, leftTab)
        SyncSettings.editLocal { set(SyncSettings.normalIndex, tab) }
        PrefSync.normalIndex = tab
        PrefSync.get(context, false)?.let {
            it.putInt(tab, "mNormalIndex")
            it.save()
        }
    }

    /**
     * Opens the browser screen again in space [number], in the same process: each space's pages run in its own WebView
     * profile, so nothing of the old space's logins reaches the new one. The old screen closes without saving.
     */
    private fun relaunch(wa: WebViewActivity, context: android.content.Context, number: Int, leftTab: Int) {
        // pages of the old space stop loading (their history and tab entries belong to it)
        try {
            wa.currentPage?.stopLoading()
        } catch (_: Exception) {
        }
        switchNumber(context, number, leftTab)
        // downloads stop, as on any switch: their list entries are kept per space
        val app = MainApp.from(wa.applicationContext)
        if (app != null && app.downBinding) app.downService?.stopAll(false, false)
        // lists the app keeps in memory belong to the old space
        if (app != null) {
            app.bookHistory = null
            app.bookDown = null
            app.bookRecent = null
            app.bookSearch = null
        }
        SpaceCover.arm(context, number)
        try {
            // the screen is rebuilt in place: no starting window (white) as for a new screen, no restart
            if (app != null) app.skipLockOnce = true
            relaunching = true
            wa.recreate()
        } catch (e: Exception) {
            relaunching = false
            android.util.Log.w("OpumSpace", "switch without restart failed: ${e.message}")
            MainUtil.restartTask(wa, "web_restart_url", wa.onStartPage)
        }
    }
}
