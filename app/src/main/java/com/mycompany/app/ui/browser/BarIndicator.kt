package com.mycompany.app.ui.browser

import android.content.Context
import com.mycompany.app.ui.state.AppState
import android.widget.ImageView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil

/**
 * The download / YouTube indicator icon the browser adds to its bars, and the icons of the address-bar settings list
 * (both were `MyIconView`s, 2026-11-17): the icon, a round press ripple in the bar's press colour and, for the download icon, the small red dot
 * ([showDot]) up and to the side of the icon centre (3 dp radius, 9 dp away; the side follows night mode as before). It keeps
 * the calls the activity makes ([setImageResource], [setBgPreColor], [showDot], [release], [setDownIcon],
 * [setScaleType]); alpha, visibility and the click listener are the view's own.
 */
@OptIn(ExperimentalFoundationApi::class)
class BarIndicator(context: Context) : ComposeOverlay(context) {
    private var icon by mutableIntStateOf(0)
    private var dot by mutableStateOf(false)
    private var pressColor by mutableIntStateOf(0)
    private var download = false
    private var setting = false
    private val nightSide = MainUtil.isRtl(context)

    init {
        setPage({ AppState.darkUi }) {
            val res = icon
            val source = remember { MutableInteractionSource() }
            val radius = with(LocalDensity.current) { (MainApp.dp48 / 2).toDp() }
            Box(
                Modifier.fillMaxSize().combinedClickable(
                    interactionSource = source,
                    indication = ripple(bounded = false, radius = radius, color = if (pressColor != 0) Color(pressColor) else Color.Unspecified),
                    onLongClick = { if (isLongClickable) performLongClick() },
                    onClick = { performClick() },
                ),
                contentAlignment = Alignment.Center,
            ) {
                if (res != 0) Image(painterResource(res), null)
                if (dot && (download || setting)) {
                    Canvas(Modifier.fillMaxSize()) { notiDot(center, nightSide) }
                }
            }
        }
    }

    /** Told when the visibility changes, so the hosting bar can redraw (it skips GONE icons). */
    @JvmField var onVisibility: Runnable? = null

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        onVisibility?.run()
    }

    fun setImageResource(res: Int) {
        icon = res
    }

    /** Press colour of the ripple. */
    fun setBgPreColor(color: Int) {
        pressColor = color
    }

    /** Shows / hides the red dot; returns whether it changed without a redraw request (old contract). */
    fun showDot(show: Boolean, redraw: Boolean): Boolean {
        if (dot == show) return false
        dot = show
        return !redraw
    }

    /** Release: nothing to free. */
    fun release() {}

    fun setDownIcon(on: Boolean) {
        download = on
    }

    /** Icon in the address-bar settings list: the dot ([showDot]) marks the configurable icon there. */
    fun setSetting(on: Boolean) {
        setting = on
    }

    /** Old highlight-shape flags; the ripple needs none. */
    fun applyShape(circle: Boolean, square: Boolean) {}

    /** Kept for the old call; the icon is always centred. */
    fun setScaleType(type: ImageView.ScaleType?) {}
}
