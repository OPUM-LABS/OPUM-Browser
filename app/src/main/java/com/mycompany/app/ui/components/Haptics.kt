package com.mycompany.app.ui.components

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Touch feedback (2026-11-27). Everything goes through Android's own feedback types, so each phone plays its own feel
 * and Android's "Touch feedback" setting turns it off for the app too (no setting of our own).
 */
object Haptics {
    /** A light tick: a gesture has gone far enough that letting go will act (refresh, switch tab, close a card). */
    fun tick(view: View?) {
        view?.performHapticFeedback(
            if (android.os.Build.VERSION.SDK_INT >= 34) HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE else HapticFeedbackConstants.CLOCK_TICK,
        )
    }

    /** A long press took effect (a bar button's long-press action, a card picked up). */
    fun longPress(view: View?) {
        view?.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }
}

/** The toggle feel for an on / off change ([TickSwitch] and the rows that toggle a switch). */
@Composable
fun rememberToggleTick(): (Boolean) -> Unit {
    val haptic = LocalHapticFeedback.current
    return { on -> haptic.performHapticFeedback(if (on) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff) }
}

/** Material 3's switch with the toggle feel; the files import it as `Switch`. */
@Composable
fun TickSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    thumbContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(),
    interactionSource: MutableInteractionSource? = null,
) {
    val tick = rememberToggleTick()
    androidx.compose.material3.Switch(
        checked = checked,
        onCheckedChange = onCheckedChange?.let { change -> { on: Boolean -> tick(on); change(on) } },
        modifier = modifier,
        thumbContent = thumbContent,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
    )
}
