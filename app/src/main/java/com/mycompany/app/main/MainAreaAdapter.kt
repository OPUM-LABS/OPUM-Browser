package com.mycompany.app.main

import java.util.ArrayList
import android.graphics.Outline
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSet.putBool
import ch.opumlabs.opumbrowser.R

/**
 * Holder for the types other code still uses. The rest of the class (a legacy View-based adapter / dialog) was unreachable
 * after the Compose port and was removed in T5 (2026-11-16).
 */
class MainAreaAdapter {
    class AreaItem {
        @JvmField
        var level: Byte = 0
        @JvmField
        var section: Int = 0
        @JvmField
        var corner: Byte = 0
        @JvmField
        var topPad: Int = 0
        @JvmField
        var botPad: Int = 0
        @JvmField
        var cmdId: Int = 0
        @JvmField
        var key: String? = null
        @JvmField
        var sel: String? = null
        @JvmField
        var isChecked: Boolean = false
    }
}
