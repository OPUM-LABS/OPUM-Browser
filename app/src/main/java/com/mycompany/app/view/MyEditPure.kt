package com.mycompany.app.view

import android.content.Context
import android.view.DragEvent
import androidx.appcompat.widget.AppCompatEditText
import com.mycompany.app.main.MainUtil.disablePersonalizedIme

@Suppress("deprecation")
class MyEditPure(context: Context?) : AppCompatEditText(context!!, null) {
    init {
        disablePersonalizedIme(this)
    }

    // landscape: keep the keyboard below the app instead of Android's full-screen typing box, which hides the
    // suggestions and the page (Compose fields do the same by default)
    override fun onCreateInputConnection(outAttrs: android.view.inputmethod.EditorInfo): android.view.inputmethod.InputConnection? {
        val connection = super.onCreateInputConnection(outAttrs)
        outAttrs.imeOptions = outAttrs.imeOptions or android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI or
            android.view.inputmethod.EditorInfo.IME_FLAG_NO_FULLSCREEN
        return connection
    }

    override fun onDragEvent(dragEvent: DragEvent?): Boolean {
        try {
            return super.onDragEvent(dragEvent)
        } catch (unused: Exception) {
            return false
        }
    }
}
