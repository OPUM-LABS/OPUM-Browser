package com.mycompany.app.wview

import android.content.Context
import com.mycompany.app.ui.state.AppState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil

/**
 * The pill that shows a link's title after a long press on it; a tap copies the title ("Copied Title") and hides the
 * pill. Compose since 2026-11-16; placing and swiping away come from [SwipeAwayOverlay].
 */
class WebAreaText(context: Context) : SwipeAwayOverlay(context, Math.round(MainUtil.dpToPx(context, 208.0f)), MainApp.dp48) {
    private var dark by mutableStateOf(AppState.darkUi)
    private var areaText by mutableStateOf("")

    init {
        elevation = MainApp.dp16.toFloat()
        setPage({ dark }) {
            TextPill(areaText, dark) {
                val copied = areaText
                tap { MainUtil.copyTextToast(context, ch.opumlabs.opumbrowser.R.string.copied_clipboard, "Copied Title", copied) }
            }
        }
    }

    fun setLinkText(linkPillText: String?) {
        areaText = linkPillText ?: ""
    }

    override fun refresh() {
        dark = AppState.darkUi
    }
}
