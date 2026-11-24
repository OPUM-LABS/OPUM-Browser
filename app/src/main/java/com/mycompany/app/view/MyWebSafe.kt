package com.mycompany.app.view

import android.view.View

import android.content.Context
import android.os.Build
import android.webkit.WebView

@Suppress("deprecation")
open class MyWebSafe(context: Context?) : WebView(context!!) {
    init {
        // the space's cookies and site data, before anything is loaded
        com.mycompany.app.web.WebProfiles.apply(this)
        try {
            setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS)
        } catch (unused: Throwable) {
        }
    }
}
