package com.mycompany.app.quick

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.AbstractComposeView

/**
 * Base of the new-tab page's row views ([QuickTileView], [QuickNoticeCard], [QuickNewsCard], [QuickSearchRow]).
 * In the Compose grid ([QuickGridView]) a row view is never attached: the grid calls its [Content] and forwards taps
 * to its listeners. View properties the adapter sets on rows that the grid has to see are mirrored into state here.
 */
abstract class QuickRowView(context: Context) : AbstractComposeView(context) {
    /** [setAlpha] as state (the dimmed "add" tile while selecting). */
    var rowAlpha by mutableFloatStateOf(1f)
        private set

    override fun setAlpha(alpha: Float) {
        super.setAlpha(alpha)
        rowAlpha = alpha
    }
}
