package com.mycompany.app.web

import android.view.View
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.ZtriSettings

/**
 * Who handles logins on web pages (2026-11-28): OPUM's own password manager, or another app (Bitwarden, 1Password,
 * Google …) through Android's autofill. With another app, OPUM's manager is off entirely: no save prompt, no fill
 * bar, no password button, no strong-password offer and no login lock; Android's autofill gets the page fields. With
 * OPUM, Android's autofill is kept out of the pages, so a login is never offered for saving twice.
 */
object PassManager {
    const val OPUM = 0
    const val OTHER_APP = 1

    val builtIn: Boolean get() = ZtriSettings.passOwner.value != OTHER_APP

    /** The built-in manager saves logins (its "Save passwords" switch). */
    val saving: Boolean get() = builtIn && SecretSettings.savePass.value

    /** The password button on pages of sites with saved logins. */
    val button: Boolean get() = saving && ZtriSettings.passIcon.value

    /**
     * Lets Android's autofill (another app) into [page]'s fields, or keeps it out (OPUM's own manager): on the page and
     * on the browser window around it (marking the page alone does not stop the autofill service).
     */
    fun applyAutofill(page: View?) {
        page ?: return
        val wanted = if (builtIn) View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS else View.IMPORTANT_FOR_AUTOFILL_AUTO
        for (view in listOf(page, page.rootView)) {
            if (view != null && view.importantForAutofill != wanted) view.importantForAutofill = wanted
        }
    }

    /** Ends Android's autofill session of the page, so the autofill app does not offer to save the login too. */
    fun cancelAutofill(context: android.content.Context) {
        try { context.getSystemService(android.view.autofill.AutofillManager::class.java)?.cancel() } catch (_: Exception) {}
    }
}
