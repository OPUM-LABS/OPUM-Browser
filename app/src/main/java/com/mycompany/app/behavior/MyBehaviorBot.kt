package com.mycompany.app.behavior

import android.content.Context
import com.mycompany.app.pref.store.BrowserSettings
import android.util.AttributeSet
import android.view.View
import com.mycompany.app.pref.PrefWeb

/**
 * Places the bottom bar for the bar scroll offset. Was a coordinator behaviour depending on the old
 * `AppBarLayout`; now the activity calls [apply] from the `BarScroll` offset listener. [locked] keeps the bar shown (error /
 * SSL page); `BrowserSettings.fixBot.value` = bottom bar fixed.
 */
class MyBehaviorBot(context: Context?, attributeSet: AttributeSet?) {
    @JvmField
    var locked: Boolean = false

    /** [top] 0 … -[range]: how far the bar moves (px) for that offset, the same fraction of its own height. */
    fun shift(view: View, top: Int, range: Int): Float {
        val height = view.height
        if (BrowserSettings.fixBot.value || locked || view.visibility == View.GONE || height == 0 || range <= 0) return 0f
        return (top.toFloat() / range) * -height
    }

    /** Moves [view] itself (used while the bar was a coordinator child). */
    fun apply(view: View, top: Int, range: Int) {
        view.translationY = shift(view, top, range)
    }
}
