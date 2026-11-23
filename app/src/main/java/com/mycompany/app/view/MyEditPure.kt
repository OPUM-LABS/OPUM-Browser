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

    override fun onDragEvent(dragEvent: DragEvent?): Boolean {
        try {
            return super.onDragEvent(dragEvent)
        } catch (unused: Exception) {
            return false
        }
    }
}
