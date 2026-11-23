package com.mycompany.app.ui.prefs

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * Bridge between Compose and the legacy preference classes (`PrefImage`, `PrefMain`, ... with static fields and
 * `PrefSet` writers). The legacy classes stay the source of truth; this holds the current value as Compose state
 * and writes every change through. [read] is called once when the screen is composed, [write] on every change.
 */
@Stable
class PrefState<T>(initial: T, private val write: (T) -> Unit) {
    var value: T by mutableStateOf(initial)
        private set

    fun set(newValue: T) {
        if (newValue == value) return
        value = newValue
        write(newValue)
    }

    /** Re-reads the value from the legacy field, e.g. after a reset. */
    fun reload(newValue: T) {
        value = newValue
    }
}

@Composable
fun <T> rememberPref(read: () -> T, write: (T) -> Unit): PrefState<T> = remember { PrefState(read(), write) }

/**
 * Runs [block] every time the hosting screen returns to the foreground, and when a dialog shown with
 * [DialogShow.direct] closes, e.g. after an old dialog changed preferences. Use it to call [PrefState.reload] with the fresh legacy values.
 */
@Composable
fun OnScreenResume(block: () -> Unit) {
    androidx.lifecycle.compose.LifecycleResumeEffect(Unit) {
        block()
        onPauseOrDispose { }
    }
    // a dialog shown directly over the screen (DialogShow.direct) does not pause it; re-read when one closes
    val tick = ScreenRefresh.tick
    androidx.compose.runtime.LaunchedEffect(tick) { if (tick > 0) block() }
}

/** Counts dialogs closed over Compose screens, so [OnScreenResume] blocks re-read their values (2026-11-17). */
object ScreenRefresh {
    var tick by androidx.compose.runtime.mutableIntStateOf(0)
}

/**
 * Shows an old dialog directly over a Compose screen, for dialogs that take a plain `Activity` (2026-11-17; before, they
 * all went through the transparent `LegacyDialogHostActivity`, removed the same day). Results of intents the dialog starts (through
 * `ActivityHost.launchForResult`) go to [onResult], permission answers to [onPermission], while it is open. When it closes,
 * [onDismiss] runs and the screen's [OnScreenResume] blocks run again, as the host activity's closing did.
 */
object DialogShow {
    fun direct(
        activity: android.app.Activity,
        requestCodes: IntArray = IntArray(0),
        onResult: ((Int, Int, android.content.Intent?) -> Unit)? = null,
        onPermission: ((Int, IntArray) -> Unit)? = null,
        onDismiss: (() -> Unit)? = null,
        factory: (android.app.Activity) -> android.app.Dialog?,
    ) {
        val host = activity as? com.mycompany.app.ui.OpumComposeActivity
        val dialog = factory(activity) ?: return
        // [requestCodes] are kept from the old host's call shape; the Compose screen has one launcher for all codes
        host?.resultHandler = onResult
        host?.permissionHandler = onPermission
        dialog.setOnDismissListener {
            if (host?.resultHandler === onResult) host?.resultHandler = null
            if (host?.permissionHandler === onPermission) host?.permissionHandler = null
            onDismiss?.invoke()
            ScreenRefresh.tick++
        }
        if (!dialog.isShowing) dialog.show()
    }
}
