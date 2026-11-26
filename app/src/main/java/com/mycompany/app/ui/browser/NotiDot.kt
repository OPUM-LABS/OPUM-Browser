package com.mycompany.app.ui.browser

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.mycompany.app.ui.state.AppState

/**
 * The one "something is here now" dot (2026-11-26; four red circles before, one per kind of button): Material's error
 * colour with a thin ring in the bar's colour, on the top-end corner of a 24 dp icon whose centre is [iconCenter].
 * Used only for live states (a video was found on the page), never for "try this" hints: those are tips.
 */
fun DrawScope.notiDot(iconCenter: Offset, rtl: Boolean, ring: Color = NotiDot.ring()) {
    val off = 9.dp.toPx()
    val at = Offset(iconCenter.x + if (rtl) -off else off, iconCenter.y - off)
    val radius = 3.5.dp.toPx()
    drawCircle(ring, radius + 1.5.dp.toPx(), at)
    drawCircle(NotiDot.color(), radius, at)
}

object NotiDot {
    /** Material 3's error colour for the current look. */
    fun color(): Color = if (AppState.darkUi) Color(0xFFF2B8B5) else Color(0xFFB3261E)

    /** The ring when the caller does not know the colour behind the dot: the bar's background for the current look. */
    fun ring(): Color = if (AppState.darkUi) Color(0xFF1F2023) else Color(0xFFFFFFFF)
}
