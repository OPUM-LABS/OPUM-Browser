package com.mycompany.app.web

import android.os.Bundle
import com.mycompany.app.pref.store.SyncSettings
import androidx.activity.ComponentActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSync

/**
 * The launcher shortcuts "new tab" and "secret tab" (`ACTION_TAB_NORMAL` / `ACTION_TAB_SECRET`): switches the secret mode
 * when needed and opens a new tab in the browser. Invisible; the class name stays because `shortcuts.xml` targets it.
 */
class WebShortTab : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainApp.loadSettingsOnce(applicationContext, resources)
        val secret = "ch.opumlabs.opumbrowser.ACTION_TAB_SECRET" == intent.action
        if (SyncSettings.secretMode.value == secret) {
            WebShortcut.openShortcut(this, NEW_TAB, false)
            return
        }
        SyncSettings.editLocal { set(SyncSettings.secretMode, secret) }
        val context = applicationContext
        Thread {
            PrefSync.saveNormalIndex(context)
            if (MainConst.isApi28) {
                runOnUiThread { MainUtil.restartTask(this, NEW_TAB, false) }
                return@Thread
            }
            if (!SyncSettings.secretMode.value) MainUtil.clearAppData(context)
            runOnUiThread { WebShortcut.openShortcut(this, NEW_TAB, false) }
        }.start()
    }

    private companion object {
        const val NEW_TAB = "short_new_tab"
    }
}
