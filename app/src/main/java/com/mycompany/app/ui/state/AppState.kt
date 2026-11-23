package com.mycompany.app.ui.state

import java.util.Locale
import com.mycompany.app.ui.state.AppState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App-wide run-time state. Each value reads and assigns like a plain field, and the
 * values a screen may want to follow (the themes) also come as [StateFlow]s. Nothing here is stored: the stored
 * theme choices are in `pref/store/BrowserSettings` (`themeUi`, `themeWeb`); these hold what is in effect now.
 */
object AppState {
    private val darkUiState = MutableStateFlow(false)
    private val darkWebState = MutableStateFlow(false)

    /** Old `AppState.darkUi`: the app's own screens are dark now (theme choice or system night mode). */
    @JvmStatic var darkUi: Boolean
        get() = darkUiState.value
        set(value) { darkUiState.value = value }
    val darkUiFlow: StateFlow<Boolean> = darkUiState.asStateFlow()

    /** Old `AppState.darkWeb`: web pages are shown dark now. */
    @JvmStatic var darkWeb: Boolean
        get() = darkWebState.value
        set(value) { darkWebState.value = value }

    /** Old `AppState.settingsLoaded`: the settings have been loaded into `pref/store` in this process. */
    @Volatile @JvmStatic var settingsLoaded = false

    /** Old `AppState.nativeLoaded`: the native helper library (`myutil`) is loaded. */
    @Volatile @JvmStatic var nativeLoaded = false

    /** Old `AppState.startupPending`: set when the process starts; the browser's start-up work clears it after its one-time checks. */
    @Volatile @JvmStatic var startupPending = false

    /** Old `AppState.exiting`: the app is shutting its process down (guards a second `System.exit`). */
    @Volatile @JvmStatic var exiting = false

    /** Old `AppState.webViewHint`: hint about the system WebView version (see `DialogSetDark`). */
    @Volatile @JvmStatic var webViewHint: Byte = 0

    /** Old `AppState.appLocale`: the app language chosen in the app (null = system). */
    @Volatile @JvmStatic var appLocale: Locale? = null

    /** Old `AppState.localeCache`: the locale last worked out by `MainApp.currentLocale()` (cache of [appLocale] / the system one). */
    @Volatile @JvmStatic var localeCache: Locale? = null
}
