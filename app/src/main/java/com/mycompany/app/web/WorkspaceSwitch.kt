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
 * page's state and the open tab are kept, and the browser starts again in the new space: its own tabs, data and
 * WebView profile. Leaving incognito this way runs incognito's usual exit (its data is cleared as set).
 */
object WorkspaceSwitch {
    /** The workspace waiting for the lock screen's answer. */
    @Volatile
    private var pending = -1

    /** Opens workspace [number] (Main is [Workspaces.MAIN]). */
    fun open(wa: WebViewActivity, number: Int) {
        val incognito = SyncSettings.secretMode.value
        if (number == Workspaces.currentNumber() && !incognito) return
        if (number != Workspaces.MAIN && number !in Workspaces.all().map { it.number }) return
        val lockType = SecretSettings.lockType.value
        if (lockType != 0 && Workspaces.isLocked(number, Workspaces.all())) {
            pending = number
            val intent = MainUtil.lockIntent(wa.dialogsState.appContext, lockType)
            // verify only: the lock screen offers "Cancel", not the lock reset
            intent.putExtra("EXTRA_TYPE", 3)
            wa.launchExternalForResult(intent, RequestCode.UNLOCK_SPACE)
            return
        }
        enter(wa, number)
    }

    /** The lock screen's answer for [open]. */
    fun onUnlockResult(wa: WebViewActivity, resultCode: Int) {
        val number = pending
        pending = -1
        if (resultCode == Activity.RESULT_OK && number >= 0) enter(wa, number)
    }

    private fun enter(wa: WebViewActivity, number: Int) {
        val context = wa.dialogsState.appContext ?: return
        val incognito = SyncSettings.secretMode.value
        // in incognito the space's open tab is the one kept when it was left
        val leftTab = if (incognito) SyncSettings.normalIndex.value else wa.currentTabIndex
        val tab = Workspaces.switchTo(context, number, leftTab)
        SyncSettings.editLocal { set(SyncSettings.normalIndex, tab) }
        PrefSync.normalIndex = tab
        PrefSync.get(context, false)?.let {
            it.putInt(tab, "mNormalIndex")
            it.save()
        }
        // unlocked here (or never locked): the restart does not ask again
        if (MainUtil.isLockEnabled() && !SecretSettings.lockSkip.value) {
            SecretSettings.editNow(context) { set(SecretSettings.lockSkip, true) }
        }
        if (incognito) {
            wa.switchSecretMode(false)
            return
        }
        // downloads stop with the restart, as when incognito is switched
        val app = MainApp.from(wa.applicationContext)
        if (app != null && app.downBinding) app.downService?.stopAll(false, false)
        val handler = wa.mainHandler
        var done = false
        val restart = Runnable {
            if (done) return@Runnable
            done = true
            MainUtil.restartTask(wa, "web_restart_url", wa.onStartPage)
        }
        val page = wa.currentPage
        val item = wa.tabItemAt(wa.currentTabIndex)
        if (handler == null || page == null || item == null || page.blankPending) {
            restart.run()
            return
        }
        handler.postDelayed(restart, 1000L)
        page.saveTabState(wa.tabsState.currentFrame, item.uid, item.tabUrl, object : WebNestView.WebBundleListener {
            override fun onBundle() {
                handler.post(restart)
            }
        })
    }
}
