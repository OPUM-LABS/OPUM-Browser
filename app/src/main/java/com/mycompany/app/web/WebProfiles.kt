package com.mycompany.app.web

import android.os.Looper
import android.util.Log
import android.webkit.CookieManager
import android.webkit.ServiceWorkerController
import android.webkit.WebStorage
import android.webkit.WebView
import androidx.webkit.Profile
import androidx.webkit.ProfileStore
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.mycompany.app.pref.store.Workspaces

/**
 * WebView profiles (2026-11-24): every space keeps its own cookies, site storage, cache and service workers.
 *
 * Main uses WebView's default profile (so existing logins stay) and workspace `n` the profile `space_n`. Incognito needs
 * none: its process already runs on a WebView data folder of its own (`app_webview_secret`, set in `MainApp.onCreate`),
 * which is emptied on leaving incognito unless "Keep logged in" is on; in it the default profile is used.
 * Where the installed WebView has no profiles ([supported] is false) the workspaces share Main's.
 *
 * [ProfileStore] answers on the main thread only; a profile once fetched there is kept, so its cookie manager and
 * storage can then be used from any thread ([Profile]'s getters may be).
 */
object WebProfiles {
    private const val TAG = "OpumProfile"
    private const val SPACE_PREFIX = "space_"

    /** The profile name of the rows numbered [dataNumber] ([Workspaces.dataNumber]). */
    fun nameFor(dataNumber: Int): String =
        if (dataNumber >= Workspaces.FIRST_NUMBER) SPACE_PREFIX + dataNumber else Profile.DEFAULT_PROFILE_NAME

    /** The workspace number of profile [name], or -1 when it is not a workspace's ([nameFor] the other way round). */
    fun spaceNumberOf(name: String): Int {
        if (!name.startsWith(SPACE_PREFIX)) return -1
        val number = name.substring(SPACE_PREFIX.length).toIntOrNull() ?: return -1
        return if (number >= Workspaces.FIRST_NUMBER) number else -1
    }

    /** The installed WebView keeps profiles apart. */
    fun supported(): Boolean = try {
        WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)
    } catch (_: Throwable) {
        false
    }

    /** The profile name the browser uses now. */
    fun currentName(): String = nameFor(Workspaces.dataNumber())

    @Volatile
    private var cached: Profile? = null

    /** The profile in use, or null where profiles are not supported (or, off the main thread, not fetched yet). */
    fun current(): Profile? {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) return null
        val name = currentName()
        val known = cached
        if (known != null && known.name == name) return known
        if (Looper.myLooper() != Looper.getMainLooper()) return null
        return fetch(name)
    }

    private fun fetch(name: String): Profile? {
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) return null
        return try {
            ProfileStore.getInstance().getOrCreateProfile(name).also { cached = it }
        } catch (e: Throwable) {
            Log.w(TAG, "profile $name: ${e.message}")
            null
        }
    }

    /** Gives a new [webView] the profile in use. Call before it loads anything. Main thread. */
    fun apply(webView: WebView) {
        val name = currentName()
        if (name == Profile.DEFAULT_PROFILE_NAME) return
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) return
        try {
            WebViewCompat.setProfile(webView, name)
            if (cached?.name != name) fetch(name)
        } catch (e: Throwable) {
            Log.w(TAG, "set profile $name: ${e.message}")
        }
    }

    /**
     * The cookie manager of the profile in use. Null only when a separate profile is in use but could not be had here
     * (never the default one in its place: that would mix the spaces' logins).
     */
    fun cookieManager(): CookieManager? {
        if (currentName() == Profile.DEFAULT_PROFILE_NAME) return CookieManager.getInstance()
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) return null
        return try {
            current()?.cookieManager
        } catch (_: Throwable) {
            null
        }
    }

    /** The site storage of the profile in use (null as in [cookieManager]). */
    fun webStorage(): WebStorage? {
        if (currentName() == Profile.DEFAULT_PROFILE_NAME) return WebStorage.getInstance()
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) return null
        return try {
            current()?.webStorage
        } catch (_: Throwable) {
            null
        }
    }

    /** The service worker controller of the profile in use (null as in [cookieManager]). */
    fun serviceWorkers(): ServiceWorkerController? {
        if (currentName() == Profile.DEFAULT_PROFILE_NAME) return ServiceWorkerController.getInstance()
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) return null
        return try {
            current()?.serviceWorkerController
        } catch (_: Throwable) {
            null
        }
    }

    /** Deletes profile [name] with all its data (a deleted workspace's; never the default). Main thread. */
    fun delete(name: String): Boolean {
        if (name == Profile.DEFAULT_PROFILE_NAME) return false
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.MULTI_PROFILE)) return false
        return try {
            val store = ProfileStore.getInstance()
            if (store.getProfile(name) == null) return false
            if (cached?.name == name) cached = null
            val done = store.deleteProfile(name)
            Log.i(TAG, "deleted profile $name: $done")
            done
        } catch (e: Throwable) {
            // still in use: a page of it is open
            Log.w(TAG, "delete profile $name: ${e.message}")
            false
        }
    }
}
