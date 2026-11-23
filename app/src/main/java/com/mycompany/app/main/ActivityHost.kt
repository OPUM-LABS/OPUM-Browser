package com.mycompany.app.main

import android.content.Intent

/**
 * What the old dialogs need from their activity beyond being an `Activity` (2026-11-17): start an intent for a result
 * under a request code. [MainActivity] does it with its per-code launchers (results go to its `onLauncherResult`); the Compose
 * screens' base `OpumComposeActivity` with one launcher registered up front, handing results and permission answers to
 * the dialog shown with `DialogShow.direct`. This replaced the transparent `LegacyDialogHostActivity`.
 */
interface ActivityHost {
    /** Starts [intent] for a result under [requestCode] (a null intent only registers the launcher). */
    fun launchForResult(intent: Intent?, requestCode: Int)

    /**
     * Same as [launchForResult], for intents that leave the app: the browser (`WebViewActivity`) overrides it to note
     * that the user is leaving first.
     */
    fun launchExternalForResult(intent: Intent?, requestCode: Int) = launchForResult(intent, requestCode)
}
