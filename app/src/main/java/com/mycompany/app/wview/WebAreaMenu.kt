package com.mycompany.app.wview

import android.content.Context
import com.mycompany.app.ui.state.AppState
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil

/**
 * The small menu shown after a long press on an empty part of the page (when "select an area" is on): select an area,
 * its settings, close. Compose since 2026-11-16; placing, swiping away and taps come from [SwipeAwayOverlay].
 * The listener gets [WebAreaMenuListener.onClear] (select) and [WebAreaMenuListener.onSettings] (settings) after the menu hid.
 */
class WebAreaMenu(context: Context) : SwipeAwayOverlay(context, Math.round(MainUtil.dpToPx(context, 160.0f)), MainApp.dp48) {
    interface WebAreaMenuListener {
        fun onClear()
        fun onSettings()
    }

    private var listener: WebAreaMenuListener? = null
    private var dark by mutableStateOf(AppState.darkUi)

    init {
        elevation = MainApp.dp4.toFloat()
        setPage({ dark }) { Bar() }
    }

    fun setFltListener(webAreaMenuListener: WebAreaMenuListener?) {
        listener = webAreaMenuListener
    }

    override fun refresh() {
        dark = AppState.darkUi
    }

    @Composable
    private fun Bar() {
        val density = LocalDensity.current
        val corner = with(density) { MainApp.dp16.toDp() }
        val pad = with(density) { MainApp.dp8.toDp() }
        val button = with(density) { getViewHeight().toDp() }
        val shape = RoundedCornerShape(corner)
        val bg = if (dark) Color(0xFF212121) else Color.White
        val tint = if (dark) Color(0xFFF1F1F1) else Color.Black
        Row(
            Modifier.fillMaxSize().background(bg, shape).border(with(density) { 1f.toDp() }, Color(0xFF757575), shape).padding(horizontal = pad),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MenuButton(R.drawable.outline_remove_selection_black_24, tint, button) { tap { listener?.onClear() } }
            MenuButton(R.drawable.outline_settings_black_20, tint, button) { tap { listener?.onSettings() } }
            MenuButton(R.drawable.outline_close_black_24, tint, button) { tap(null) }
        }
    }

    @Composable
    private fun MenuButton(icon: Int, tint: Color, size: Dp, onClick: () -> Unit) {
        Box(
            Modifier.size(size).clickable(remember { MutableInteractionSource() }, ripple(bounded = false, radius = size / 2), onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(painterResource(icon), contentDescription = null, tint = tint)
        }
    }
}
