package com.mycompany.app.ui

import androidx.compose.runtime.collectAsState
import android.content.res.Configuration
import com.mycompany.app.ui.state.AppState
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.theme.OpumTheme

/**
 * Base class of every screen that is written in Compose. It extends `androidx.activity.ComponentActivity` and keeps the
 * app's global day/night flag ([AppState.darkUi]) in sync, as the View based activities do; the theme follows
 * [AppState.darkUiFlow].
 */
abstract class OpumComposeActivity : ComponentActivity(), com.mycompany.app.main.ActivityHost {
    // results / permission answers for an old dialog shown with DialogShow.direct (set while it is open)
    internal var resultHandler: ((Int, Int, android.content.Intent?) -> Unit)? = null
    internal var permissionHandler: ((Int, IntArray) -> Unit)? = null
    private var resultLauncher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>? = null
    private var pendingCode = 0

    /** [com.mycompany.app.main.ActivityHost]: starts [intent] for a result; the answer goes to [resultHandler]. */
    override fun launchForResult(intent: android.content.Intent?, requestCode: Int) {
        if (intent == null) return
        pendingCode = requestCode
        try { resultLauncher?.launch(intent) } catch (_: Exception) {}
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        // old dialogs shown here still ask with requestPermissions(..., code) and get the answer through this
        @Suppress("DEPRECATION") super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHandler?.invoke(requestCode, grantResults)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // registered before the screen starts, as the activity result API requires
        resultLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) {
            resultHandler?.invoke(pendingCode, it.resultCode, it.data)
        }
        ensurePreferencesLoaded()
        applySystemBars(MainUtil.isDarkTheme(true, resources.configuration))
        super.onCreate(savedInstanceState)
        updateDark(resources.configuration)
        setContent {
            // follows AppState.darkUi live, so a theme change re-themes open screens (2026-11-17)
            val dark by AppState.darkUiFlow.collectAsState()
            androidx.compose.runtime.LaunchedEffect(dark) { applySystemBars(dark) }
            OpumTheme(darkTheme = dark) {
                Content()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateDark(newConfig)
    }

    /**
     * The legacy `Pref*` statics are filled by the browser activity's startup. A settings screen can be the first
     * activity of a fresh process (the system restores the top activity after the process was killed), so load them
     * here when that has not happened yet; otherwise the screen would show, and write back, default values.
     */
    private fun ensurePreferencesLoaded() {
        if (AppState.settingsLoaded) return
        MainApp.loadEarlySettings(this)
        MainApp.loadMediaSettings(this)
        MainApp.loadLateSettings(this, resources)
        AppState.settingsLoaded = true
    }

    /**
     * Edge to edge with status / navigation bar icons for the app's own light or dark theme ([dark]); the default
     * follows the device's dark mode, which left dark icons on the app's Dark themes.
     */
    private fun applySystemBars(dark: Boolean) {
        val transparent = android.graphics.Color.TRANSPARENT
        val style = if (dark) androidx.activity.SystemBarStyle.dark(transparent)
        else androidx.activity.SystemBarStyle.light(transparent, transparent)
        enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }

    private fun updateDark(configuration: Configuration) {
        AppState.darkUi = MainUtil.isDarkTheme(true, configuration)
    }

    /** Re-reads the app's day/night flag after a theme setting changed, so this screen switches immediately. */
    fun refreshTheme() {
        updateDark(resources.configuration)
    }

    @Composable
    protected abstract fun Content()
}
