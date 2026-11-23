package com.mycompany.app.wview

import android.content.Context
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.ZtriSettings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefZtri

/**
 * The pill offered after a long press on a floating button: "Long press to move" / "Move freely". A tap flips the
 * setting (`ZtriSettings.moveLong.value`, "mMoveLong") and hides the pill. Compose since 2026-11-16; placing and swiping away come from
 * [SwipeAwayOverlay] (this pill sits a little to the right of the point and closer above it).
 */
class WebMoveText(context: Context) : SwipeAwayOverlay(context, Math.round(MainUtil.dpToPx(context, 208.0f)), MainApp.dp48) {
    private var dark by mutableStateOf(AppState.darkUi)
    private var free by mutableStateOf(ZtriSettings.moveLong.value)

    init {
        elevation = MainApp.dp16.toFloat()
        setPage({ dark }) {
            TextPill(context.getString(if (free) ch.opumlabs.opumbrowser.R.string.move_free else ch.opumlabs.opumbrowser.R.string.move_long), dark) {
                tap {
                    val z = !ZtriSettings.moveLong.value
                    ZtriSettings.edit(context) { set(ZtriSettings.moveLong, z) }
                }
            }
        }
    }

    override fun anchorX(px: Int): Int = px - getViewWidth() / 2 + MainApp.dp48 / 2
    override fun anchorY(py: Int): Int = if (py > getViewHeight()) (py - getViewHeight()) - MainApp.dp4 else py + getViewHeight() / 2

    override fun refresh() {
        dark = AppState.darkUi
        free = ZtriSettings.moveLong.value
    }
}
