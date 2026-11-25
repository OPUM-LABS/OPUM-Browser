package com.mycompany.app.dialog

import android.content.Context
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.ZoneSettings
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.ui.browser.ComposeOverlay
import com.mycompany.app.view.MyWebBody
import com.mycompany.app.web.WebViewActivity

/**
 * The small button bar of a video or image on the page ([anchorView] is the anchor): a rounded bar above or below it
 * (`ZoneSettings.downPos`) with picture-in-picture, full screen and download for a video, or picture-in-picture and slow
 * motion for an image ([isLive]); which buttons show comes from the video button settings (`DialogSetVideo`). A button calls
 * [VideoMenuListener.onAction] (0 download, 1 full screen, 2 picture-in-picture, 3 slow motion); a tap outside closes it.
 * Compose since 2026-11-17 (was `MyRoundLinear` with `MyButtonImage`s); place, size (144 / 208 x 64 dp), the
 * grow / shrink animation (200 ms) and [startClose] / the listener are as before. [unused] was the right-to-left flag (kept for the
 * callers; the bar is placed by absolute position).
 */
@Suppress("deprecation")
class DialogVideoMenu(
    var activity: WebViewActivity?,
    var webBody: MyWebBody?,
    var anchorView: View?,
    var isLive: Boolean,
    var unused: Boolean,
    var listener: VideoMenuListener?
) {
    interface VideoMenuListener {
        fun onClose()

        fun onAction(action: Int)
    }

    private class Button(val icon: Int, val action: Int)

    private var overlay: ViewGroup? = null
    private var closing by mutableStateOf(false)
    private var box: IntArray? = null      // left, top, width, height
    private var pivot: IntArray? = null    // the anchor's centre inside the bar
    private val buttons: List<Button> = if (isLive) {
        listOfNotNull(
            if (ZoneSettings.showPip.value) Button(R.drawable.outline_picture_in_picture_alt_black_24, 2) else null,
            if (ZoneSettings.youRate.value) Button(R.drawable.outline_slow_motion_video_black_24, 3) else null,
        )
    } else {
        listOfNotNull(
            if (ZoneSettings.showPip.value) Button(R.drawable.outline_picture_in_picture_alt_black_24, 2) else null,
            if (ZoneSettings.showFull.value) Button(R.drawable.outline_fullscreen_black_24, 1) else null,
            if (ZoneSettings.showDown.value) Button(R.drawable.outline_download_black_24, 0) else null,
        )
    }

    private inner class Overlay(context: Context) : ComposeOverlay(context) {
        init {
            setPage({ AppState.darkUi }) { Bar() }
        }
    }

    init {
        webBody?.post { open() }
    }

    private fun open() {
        val body = webBody ?: return
        val act = activity ?: return
        if (anchorView == null) return
        try {
            place()
            val view = Overlay(act)
            overlay = view
            // In the browser's Compose overlay stack, above the bars (was a child of the page body).
            val stack = body.overlays
            if (stack != null) stack.addView(view, -1, -1) else body.addView(view, FrameLayout.LayoutParams(-1, -1))
        } catch (unused: Exception) {
            body.post { startClose() }
        }
    }

    /** Starts the closing animation; false when the bar is not up or already closing. */
    fun startClose(): Boolean {
        if (overlay == null || closing) return false
        closing = true
        return true
    }

    /** Centred on the anchor, above or below it, kept inside the page. */
    private fun place() {
        val body = webBody!!
        val anchor = anchorView!!
        val width = if (!isLive && ZoneSettings.showPip.value && ZoneSettings.showFull.value && ZoneSettings.showDown.value) 208 else 144
        val loc = IntArray(2)
        body.getLocationOnScreen(loc)
        val left = body.paddingLeft + loc[0]
        val top0 = body.paddingTop + loc[1]
        val rectWidth = body.rectWidth
        val rectHeight = body.rectHeight
        anchor.getLocationOnScreen(loc)
        val cx = (MainApp.dp48 / 2) + (loc[0] - left)
        val anchorTop = loc[1] - top0
        val w = Math.round(MainUtil.dpToPx(activity, width.toFloat()))
        val h = MainApp.dp64
        var l = cx - (w / 2)
        val bottom: Int
        val ref: Int
        val top: Int
        if (ZoneSettings.downPos.value == 1) {
            top = anchorTop + MainApp.dp48
            bottom = top + h
            ref = top
        } else {
            bottom = MainApp.dp4 + anchorTop
            ref = anchorTop
            top = bottom - h
        }
        if (l < 0) {
            l = 0
        } else if (l + w > rectWidth) {
            l = rectWidth - w
        }
        var t = 0
        if (top >= 0) {
            t = if (bottom > rectHeight) rectHeight - h else top
        }
        box = intArrayOf(l, t, w, h)
        pivot = intArrayOf(cx - l, if (t == 0) (MainApp.dp48 / 2) + ref else ref - t)
    }

    /** Removes the bar and lets the browser know ([VideoMenuListener.onClose]). */
    private fun teardown() {
        if (activity == null) return
        val view = overlay
        overlay = null
        if (view != null) {
            webBody?.overlays?.removeView(view)
            webBody?.removeView(view)
        }
        webBody = null
        listener?.onClose()
        listener = null
        activity = null
        anchorView = null
    }

    @Composable
    private fun Bar() {
        val area = box ?: return
        val origin = pivot ?: intArrayOf(0, 0)
        val scale = remember { Animatable(0f) }
        LaunchedEffect(closing) {
            if (!closing) {
                scale.animateTo(1f, tween(200, easing = LinearOutSlowInEasing))
            } else {
                scale.animateTo(0f, tween(200, easing = FastOutLinearInEasing))
                teardown()
            }
        }
        val density = LocalDensity.current
        val none = remember { MutableInteractionSource() }
        Box(Modifier.fillMaxSize().clickable(interactionSource = none, indication = null) { startClose() }) {
            Surface(
                shape = RoundedCornerShape(with(density) { MainApp.dp16.toDp() }),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = with(density) { MainApp.dp16.toDp() },
                modifier = Modifier
                    .absoluteOffset { IntOffset(area[0], area[1]) }
                    .size(with(density) { area[2].toDp() }, with(density) { area[3].toDp() })
                    .graphicsLayer {
                        val scaleNow = scale.value
                        scaleX = scaleNow; scaleY = scaleNow; alpha = scaleNow
                        transformOrigin = TransformOrigin(
                            if (area[2] > 0) origin[0].toFloat() / area[2] else 0f,
                            if (area[3] > 0) origin[1].toFloat() / area[3] else 0f,
                        )
                    }
                    .clickable(interactionSource = none, indication = null) { },
            ) {
                Row(
                    Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(with(density) { MainApp.dp16.toDp() }, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    for (button in buttons) {
                        FilledTonalIconButton(
                            onClick = { listener?.onAction(button.action) },
                            modifier = Modifier.size(with(density) { MainApp.dp48.toDp() }),
                        ) {
                            Icon(painterResource(button.icon), null)
                        }
                    }
                }
            }
        }
    }
}
