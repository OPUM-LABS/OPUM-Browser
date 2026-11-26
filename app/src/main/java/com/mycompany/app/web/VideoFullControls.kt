package com.mycompany.app.web

import com.mycompany.app.ui.browser.notiDot
import android.content.Context
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.browser.ComposeOverlay

/**
 * One icon button of the full-screen web video controls ([WebVideoFull]); it replaced a `MyButtonImage` in 2026-11 and
 * keeps the calls the player makes on it ([setImageResource], [setVisibility], [setNoti], [setOnClickListener],
 * [isPressed], [setAlpha]). The click gets [anchor], a real view at the button's place (popup menus anchor to it).
 */
open class VfButton(icon: Int, shown: Boolean = true) {
    var icon by mutableIntStateOf(icon)
    var shown by mutableStateOf(shown)
    var dot by mutableStateOf(false)
    var opacity by mutableFloatStateOf(1f)
    /** Held by a finger (Compose press, or a touch forwarded to [VfCheck.onTouchEvent]). */
    var held by mutableStateOf(false)
    internal var click: View.OnClickListener? = null
    /** Invisible view filling the button, created by the layout. */
    var anchor: View? = null
        internal set

    fun setImageResource(res: Int) { icon = res }
    fun setVisibility(visibility: Int) { shown = visibility == View.VISIBLE }
    fun getVisibility(): Int = if (shown) View.VISIBLE else View.GONE
    fun setNoti(on: Boolean) { dot = on }
    fun setOnClickListener(newClick: View.OnClickListener?) { click = newClick }
    fun setAlpha(newOpacity: Float) { opacity = newOpacity }
    fun isPressed(): Boolean = held

    /** Lets go of the listener (the player is closing). */
    fun clearClick() { click = null }

    internal fun fire() {
        val l = click ?: return
        l.onClick(anchor)
    }
}

/**
 * A two-state button (the lock and the "show icons" switch), replacing `MyButtonCheck`: [isChecked] is the state, [setChecked] sets it.
 * While the screen is locked the player forwards every touch to [onTouchEvent]; only a tap inside the button counts
 * then (the old `setCheckArea(true)`).
 */
class VfCheck(private val onIcon: Int, private val offIcon: Int) : VfButton(offIcon) {
    var checked by mutableStateOf(false)
        private set
    /** The checked state (the old field name). */
    val isChecked: Boolean get() = checked
    private var areaOnly = false
    private var downInside = false

    fun setChecked(on: Boolean, @Suppress("UNUSED_PARAMETER") animate: Boolean) {
        checked = on
        icon = if (on) onIcon else offIcon
    }

    fun setCheckArea(on: Boolean) { areaOnly = on }

    /** A touch forwarded from the player while locked. */
    fun onTouchEvent(ev: MotionEvent): Boolean {
        val view = anchor ?: return false
        when (ev.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downInside = !areaOnly || MainUtil.isInsideView(ev.rawX.toInt(), ev.rawY.toInt(), MainApp.dp4, view)
                held = downInside
            }
            MotionEvent.ACTION_MOVE -> if (downInside && !MainUtil.isInsideView(ev.rawX.toInt(), ev.rawY.toInt(), MainApp.dp4, view)) {
                downInside = false
                held = false
            }
            MotionEvent.ACTION_UP -> {
                if (downInside) view.post { fire() }
                downInside = false
                held = false
            }
            MotionEvent.ACTION_CANCEL -> {
                downInside = false
                held = false
            }
        }
        return true
    }
}

/** A white outlined label of the controls (replacing `MyTextView` / `AppCompatTextView`). */
class VfText(shown: Boolean = true) {
    var text by mutableStateOf("")
    var shown by mutableStateOf(shown)
    fun setText(retry: CharSequence?) { text = retry?.toString() ?: "" }
    fun setVisibility(visibility: Int) { shown = visibility == View.VISIBLE }
}

/** An invisible spacer whose visibility lifts the bottom row (was a plain `View` in an alignment frame). */
class VfSpacer {
    var shown by mutableStateOf(false)
    /** For the top row: an invisible view covering it (the player reads its screen bounds). */
    var anchor: View? = null
        internal set
    fun setVisibility(visibility: Int) { shown = visibility == View.VISIBLE }
}

/** All controls of the full-screen web video, in the old field order of [WebVideoFull]. */
class VideoFullControls {
    // top
    @JvmField val title = VfText(shown = false)
    @JvmField val back = VfButton(ch.opumlabs.opumbrowser.R.drawable.outline_chevron_left_white_24)
    @JvmField val topRow = VfSpacer().also { it.shown = true }
    @JvmField val rotate = VfButton(ch.opumlabs.opumbrowser.R.drawable.outline_screen_rotation_white_24)
    @JvmField val zoom = VfButton(ch.opumlabs.opumbrowser.R.drawable.outline_zoom_out_map_white_24)
    @JvmField val next = VfButton(ch.opumlabs.opumbrowser.R.drawable.outline_arrow_right_alt_white_24)
    @JvmField val speed = VfButton(ch.opumlabs.opumbrowser.R.drawable.outline_slow_motion_video_white_24)
    @JvmField val pip = VfButton(ch.opumlabs.opumbrowser.R.drawable.outline_picture_in_picture_alt_white_24)
    @JvmField val settings = VfButton(ch.opumlabs.opumbrowser.R.drawable.outline_settings_white_24)

    // middle / bottom
    @JvmField val lock = VfCheck(ch.opumlabs.opumbrowser.R.drawable.outline_lock_red_24, ch.opumlabs.opumbrowser.R.drawable.outline_lock_white_24)
    @JvmField val rewind = VfButton(ch.opumlabs.opumbrowser.R.drawable.baseline_fast_rewind_white_24, shown = false)
    @JvmField val play = VfButton(ch.opumlabs.opumbrowser.R.drawable.baseline_pause_white_24, shown = false)
    @JvmField val forward = VfButton(ch.opumlabs.opumbrowser.R.drawable.baseline_fast_forward_white_24, shown = false)
    @JvmField val icons = VfCheck(
        ch.opumlabs.opumbrowser.R.drawable.outline_radio_button_checked_white_24,
        ch.opumlabs.opumbrowser.R.drawable.outline_radio_button_unchecked_dark_24,
    )
    @JvmField val download = VfButton(ch.opumlabs.opumbrowser.R.drawable.outline_download_white_24, shown = false)
    @JvmField val bright = VfButton(ch.opumlabs.opumbrowser.R.drawable.outline_brightness_6_white_24)
    @JvmField val brightText = VfText()
    @JvmField val volume = VfButton(ch.opumlabs.opumbrowser.R.drawable.outline_volume_up_white_24)
    @JvmField val volumeText = VfText()

    /** Bottom spacers: [lift1] (64 dp) or [lift2] (12 dp) under the centre / right row, [lift3] (64 dp) under the left. */
    @JvmField val lift1 = VfSpacer()
    @JvmField val lift2 = VfSpacer()
    @JvmField val lift3 = VfSpacer()

    /** Opens / closes the tray with the rarer buttons (2026-11-20); a real button so the player's touch check sees it. */
    @JvmField val more = VfButton(ch.opumlabs.opumbrowser.R.drawable.outline_more_vert_white_24)
}

/** The Compose page of [VideoFullControls]; sits in the player's fading frame, filling it. */
class VideoFullControlsView(context: Context, private val outer: VideoFullControls) : ComposeOverlay(context) {
    init {
        setPage({ true }) { Controls() }
    }

    /** The "more" tray (speed, repeat, download, brightness, volume, settings) is open. */
    private var tray by mutableStateOf(false)

    init {
        outer.more.setOnClickListener {
            tray = !tray
            outer.more.setImageResource(if (tray) ch.opumlabs.opumbrowser.R.drawable.outline_close_white_24 else ch.opumlabs.opumbrowser.R.drawable.outline_more_vert_white_24)
        }
    }

    /** The fading frame hid the controls: close the tray, so they come back compact. */
    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility != View.VISIBLE && tray) {
            tray = false
            outer.more.setImageResource(ch.opumlabs.opumbrowser.R.drawable.outline_more_vert_white_24)
        }
    }

    /*
     * 2026-11-20 rework, matching OPUM's own video player: no row of icons across the top and no black discs; back and
     * title on a top gradient; every button in one row at thumb reach above the page's own controls (show-icons switch,
     * lock, play, zoom, rotate, picture-in-picture, more); "more" opens a tray row above it with the rarer ones (speed,
     * repeat, download, brightness and volume with their level, settings). Rewind / forward are not drawn: double-tap
     * does that. Play sits in the row, not the middle, where the page's own play button is. The buttons are the same
     * objects as before, so the player's listeners, popups (anchored to the buttons) and dialogs are unchanged.
     */
    @Composable
    private fun Controls() {
        val c = outer
        val iconsOn = c.back.shown
        // the page's own controls (time, seek bar) sit at the bottom: the row stays above them, as the old rows did
        val lift = 80.dp + if (c.lift1.shown) 64.dp else if (c.lift2.shown) 12.dp else 0.dp
        val trayButtons = listOf(c.speed, c.next, c.download, c.bright, c.volume, c.settings)
        val trayDot = trayButtons.any { it.shown && it.dot }
        BoxWithConstraints(Modifier.fillMaxSize()) {
            // 8 buttons fit across the narrowest phones
            val slot = minOf(48.dp, (maxWidth - 8.dp) / 8)
            if (iconsOn) {
                Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().height(120.dp).background(Brush.verticalGradient(listOf(Color(0x8C000000), Color.Transparent))))
                Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(lift + if (tray) 150.dp else 100.dp).background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x8C000000)))))
                Row(Modifier.align(Alignment.TopStart).fillMaxWidth().displayCutoutPadding().padding(start = 4.dp, top = 8.dp, end = 64.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(c.back, slot, Modifier)
                    if (c.title.shown) Label(c.title, Modifier.weight(1f).padding(start = 4.dp))
                }
            }
            // bottom: tray (when open) over the main row; the whole area is the player's no-gesture zone
            Box(Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(bottom = lift, start = 4.dp, end = 4.dp)) {
                if (iconsOn) AndroidView(factory = { ctx -> View(ctx).also { c.topRow.anchor = it } }, modifier = Modifier.matchParentSize())
                Column {
                    if (iconsOn && tray) {
                        Row(Modifier.fillMaxWidth().padding(bottom = 4.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            for (b in trayButtons) {
                                if (!b.shown) continue
                                Button(b, slot, Modifier)
                                val level = when (b) { c.bright -> c.brightText; c.volume -> c.volumeText; else -> null }
                                if (level != null && level.shown && level.text.isNotEmpty()) Label(level, Modifier.padding(end = 6.dp))
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Button(c.icons, slot, Modifier)
                        if (c.lock.shown) Button(c.lock, slot, Modifier)
                        if (c.play.shown) Button(c.play, slot, Modifier)
                        Spacer(Modifier.weight(1f))
                        if (iconsOn) {
                            for (b in listOf(c.zoom, c.rotate, c.pip)) if (b.shown) Button(b, slot, Modifier)
                            SideEffect { c.more.dot = trayDot && !tray }
                            Button(c.more, slot, Modifier)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun Button(back: VfButton, size: Dp, modifier: Modifier) {
        val interaction = remember { MutableInteractionSource() }
        val shownNow = back.shown
        val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
        Box(
            modifier
                .size(size)
                .alpha(if (shownNow) back.opacity else 0f)
                .then(
                    if (shownNow) {
                        Modifier
                            // "held" right when the finger lands: the player asks on lift-off whether a button was
                            // touched (else the tap also shows / hides the controls). The old press state came a frame
                            // late through a coroutine, so a quick tap on a button toggled the controls too.
                            .pointerInput(back) {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                                    back.held = true
                                    do {
                                        val event = awaitPointerEvent(PointerEventPass.Initial)
                                    } while (event.changes.any { it.pressed })
                                    back.held = false
                                }
                            }
                            .clickable(interaction, ripple(bounded = false, radius = size / 2, color = Color(0xFF757575))) { back.fire() }
                    } else Modifier,
                )
                .drawWithContent {
                    drawContent()
                    if (back.dot) notiDot(center, rtl, Color(0xFF202124))
                },
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(factory = { ctx -> View(ctx).also { back.anchor = it } }, modifier = Modifier.fillMaxSize())
            if (shownNow) Icon(painterResource(back.icon), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(24.dp))
        }
    }

    @Composable
    private fun Label(brightText: VfText, modifier: Modifier) {
        Text(
            brightText.text,
            modifier,
            style = TextStyle(color = Color.White, fontSize = 15.sp, shadow = Shadow(Color.Black, Offset.Zero, 6f)),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * The one-time guide card of the full-screen video ("long press shows the icons…"): dark rounded card with the
 * long-press icon, the text and a close button in the corner. Sits in the player's fading guide frame.
 */
class VideoGuideCardView(context: Context, private val text: String, private val onClose: (View) -> Unit) : ComposeOverlay(context) {
    init {
        setPage({ true }) { Card() }
    }

    /** The shared guide card (2026-11-20): long-press icon, the text, close in the corner. */
    @Composable
    private fun Card() {
        com.mycompany.app.ui.components.GuideCard(
            text = text,
            icon = ch.opumlabs.opumbrowser.R.drawable.outline_touch_long_dark_20,
            iconSize = 32.dp,
            onClose = { onClose(this) },
        )
    }
}
