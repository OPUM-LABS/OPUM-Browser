package com.mycompany.app.ui.components

import android.content.Context
import com.mycompany.app.ui.state.AppState
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.mycompany.app.main.MainApp
import com.mycompany.app.ui.browser.ComposeOverlay

/**
 * A check-circle toggle for legacy View screens (replaces `view/MyButtonCheck`, 2026-11-17): shows [applyIcons]'s checked or
 * unchecked icon and cross-fades between them when [applyChecked] changes the state with animation. Taps go to the view's own
 * click listener, as before.
 */
class CheckIcon(context: Context) : ComposeOverlay(context) {
    private var checked by mutableStateOf(false)
    private var animate by mutableStateOf(false)
    private var onIcon by mutableIntStateOf(0)
    private var offIcon by mutableIntStateOf(0)

    init {
        isClickable = true
        setPage({ AppState.darkUi }) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Crossfade(checked, animationSpec = if (animate) tween(200) else snap(), label = "check") { on ->
                    val res = if (on) onIcon else offIcon
                    if (res != 0) Icon(painterResource(res), null, tint = Color.Unspecified)
                }
            }
        }
    }

    /** The checked and unchecked icons. */
    fun applyIcons(on: Int, off: Int) {
        onIcon = on
        offIcon = off
    }

    /** Sets the state; [animated] cross-fades. */
    fun applyChecked(on: Boolean, animated: Boolean) {
        animate = animated
        checked = on
    }
}
