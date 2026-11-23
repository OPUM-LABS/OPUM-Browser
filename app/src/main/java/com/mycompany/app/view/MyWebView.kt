package com.mycompany.app.view

import android.content.Context
import android.util.AttributeSet
import android.view.ViewStructure
import android.webkit.WebView

@Suppress("deprecation")
open class MyWebView : WebView {
    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    override fun onProvideAutofillVirtualStructure(viewStructure: ViewStructure?, flags: Int) {
        if (viewStructure == null) {
            return
        }
        try {
            super.onProvideAutofillVirtualStructure(viewStructure, flags)
        } catch (unused: Throwable) {
        }
    }

    override fun onProvideVirtualStructure(viewStructure: ViewStructure?) {
        if (viewStructure == null) {
            return
        }
        try {
            super.onProvideVirtualStructure(viewStructure)
        } catch (unused: Throwable) {
        }
    }
}
