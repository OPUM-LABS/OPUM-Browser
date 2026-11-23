package com.mycompany.app.ui.components

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.browser.ComposeOwners
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.ui.theme.OpumTheme
import kotlinx.coroutines.delay

/**
 * OPUM's one message (2026-11-20): a pill in the snackbar colours, centred above the
 * browser's bottom bars (or the navigation bar / keyboard on other screens), replacing the system toast for every
 * `MainUtil.showToast` / `showToastText`. It lives in its own small window of the visible OPUM screen, added when shown,
 * so it also sits above an open sheet or dialog; it takes no touches. With no OPUM screen in front (e.g. a download
 * finishing in the background) the system toast is used.
 */
// the activity references are dropped when it pauses (`resumed`) or is destroyed (`host`), see the lifecycle callbacks
@android.annotation.SuppressLint("StaticFieldLeak")
object OpumMessage {
    private val main = Handler(Looper.getMainLooper())
    private var resumed: Activity? = null
    private var host: Activity? = null
    private var view: ComposeView? = null
    private var owners: ComposeOwners? = null

    private var carried: String? = null
    private var carryUntil = 0L

    private var text by mutableStateOf<String?>(null)
    private var shown by mutableStateOf(false)
    private var stamp by mutableLongStateOf(0L)

    /** Tracks the screen in front, so messages from non-activity contexts find it. Called once from `MainApp`. */
    @JvmStatic
    fun attach(app: Application) {
        app.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                resumed = activity
                // a message shown just before its screen closed ("Saved", then finish) carries on on the next one
                val message = carried
                carried = null
                if (message != null && android.os.SystemClock.uptimeMillis() < carryUntil) show(activity, message)
            }
            override fun onActivityPaused(activity: Activity) {
                if (resumed === activity) resumed = null
                if (host === activity) {
                    if (shown) {
                        carried = text
                        carryUntil = android.os.SystemClock.uptimeMillis() + 2500L
                    }
                    remove()
                }
            }
            override fun onActivityDestroyed(activity: Activity) { if (host === activity) remove() }
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        })
    }

    /** Shows [message] (any thread). */
    @JvmStatic
    fun show(context: Context?, message: CharSequence?) {
        if (message.isNullOrEmpty()) return
        if (Looper.myLooper() != Looper.getMainLooper()) {
            main.post { show(context, message) }
            return
        }
        val activity = activityOf(context)?.takeIf { it === resumed } ?: resumed
        if (activity == null || activity.isFinishing || activity.isDestroyed || !ensureWindow(activity)) {
            systemToast(context ?: activity, message)
            return
        }
        text = message.toString()
        shown = true
        stamp = System.nanoTime()
    }

    /** Hides the current message (the old `cancelToast`). */
    @JvmStatic
    fun hide() {
        shown = false
    }

    private fun systemToast(context: Context?, message: CharSequence) {
        context ?: return
        try {
            val toast = Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT)
            toast.show()
            MainApp.lastToast = toast
        } catch (_: Exception) {
        }
    }

    private fun activityOf(context: Context?): Activity? {
        var c = context
        while (c is ContextWrapper) {
            if (c is Activity) return c
            c = c.baseContext
        }
        return null
    }

    /** Adds the message window to [activity] (once per screen); false when the screen has no window yet. */
    private fun ensureWindow(activity: Activity): Boolean {
        if (host === activity && view != null) {
            place(activity)
            return true
        }
        remove()
        val decor = activity.window?.decorView ?: return false
        if (decor.windowToken == null) return false
        val compose = ComposeView(activity)
        val o = ComposeOwners()
        o.install(compose)
        o.onStart(); o.onResume()
        compose.setContent {
            OpumTheme(darkTheme = AppState.darkUi) {
                val current = text
                LaunchedEffect(stamp) {
                    if (stamp == 0L) return@LaunchedEffect
                    delay(if ((current?.length ?: 0) > 40) 3500L else 2200L)
                    shown = false
                    delay(400L)
                    if (!shown) main.post { remove() }
                }
                Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), contentAlignment = Alignment.Center) {
                    AnimatedVisibility(shown, enter = fadeIn() + slideInVertically { it / 2 }, exit = fadeOut() + slideOutVertically { it / 2 }) {
                        Text(
                            current ?: "",
                            Modifier.widthIn(max = 560.dp)
                                .background(MaterialTheme.colorScheme.inverseSurface, RoundedCornerShape(24.dp))
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        )
        params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        params.y = bottomOffset(activity)
        params.title = "OpumMessage"
        return try {
            activity.windowManager.addView(compose, params)
            view = compose
            owners = o
            host = activity
            true
        } catch (_: Exception) {
            o.onDestroy()
            false
        }
    }

    /** Moves the window to the current bar / keyboard height. */
    private fun place(activity: Activity) {
        val v = view ?: return
        val params = v.layoutParams as? WindowManager.LayoutParams ?: return
        val y = bottomOffset(activity)
        if (params.y != y) {
            params.y = y
            try { activity.windowManager.updateViewLayout(v, params) } catch (_: Exception) {}
        }
    }

    /** Navigation bar or keyboard, plus the browser's bottom bars on the browser screen. */
    private fun bottomOffset(activity: Activity): Int {
        val decor: View? = activity.window?.decorView
        val insets = decor?.let { ViewCompat.getRootWindowInsets(it) }
        val system = insets?.getInsets(WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.ime())?.bottom ?: 0
        val bars = if (activity is MainActivity) try { MainUtil.bottomInsetTotal() } catch (_: Exception) { 0 } else 0
        return system + bars + MainApp.dp8
    }

    private fun remove() {
        val v = view
        val a = host
        view = null
        host = null
        if (v != null && a != null) {
            try { a.windowManager.removeViewImmediate(v) } catch (_: Exception) {}
        }
        owners?.onDestroy()
        owners = null
        shown = false
    }
}
