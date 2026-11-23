package com.mycompany.app.ui.components

import android.content.Context
import com.mycompany.app.ui.state.AppState
import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.ui.browser.ComposeOverlay

/**
 * The one-time "pinch to zoom" card shown over the reader the first time (replaces the reader's `MyFadeFrame` guide,
 * 2026-11-17): a dark rounded card, 132 dp wide, with the pinch icon and the text, in the middle. The first touch
 * anywhere calls [onTouch] (the reader turns the guide off for good there) and fades the card out (400 ms); the touch
 * still goes on to the page. When it has faded out, [onGone] runs (the reader removes it and shows its bars).
 */
class PinchGuide(context: Context, private val onTouch: () -> Unit, private val onGone: () -> Unit) : ComposeOverlay(context) {
    private val visible = MutableTransitionState(false).apply { targetState = true }
    private var touched = false

    init {
        setPage({ AppState.darkUi }) {
            val state = remember { visible }
            LaunchedEffect(Unit) {
                snapshotFlow { state.isIdle && !state.currentState && !state.targetState }.collect { done ->
                    if (done && touched) onGone()
                }
            }
            AnimatedVisibility(state, enter = fadeIn(tween(400)), exit = fadeOut(tween(400))) {
                Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
                    GuideCard(stringResource(R.string.guide_pinch), icon = R.drawable.outline_pinch, iconSize = 84.dp)
                }
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (!touched) {
            touched = true
            onTouch()
            visible.targetState = false
        }
        return false
    }
}

/**
 * State of a pinch guide drawn inside a Compose screen (2026-11-17, for `DialogPreview` and `DialogViewSrc`, which had
 * `MyFadeFrame`s from `VideoGuide.pinchGuide`). [hideNow] fades it out (the screen calls it on the first touch, after turning
 * the guide off for good); [reset] drops it at once.
 */
class GuideState {
    var shown by mutableStateOf(true)
    fun hideNow(animate: Boolean) { shown = false }
    fun reset() { shown = false }
}

/**
 * The pinch guide card for [state], centred in the parent; it takes no touches (the screen sees the first touch in
 * the early pointer pass, so the touch still reaches the content). [onGone] runs once it has faded out.
 */
@Composable
fun PinchGuideCard(state: GuideState, onGone: () -> Unit, modifier: Modifier = Modifier) {
    AnimatedVisibility(state.shown, modifier = modifier, enter = fadeIn(tween(0)), exit = fadeOut(tween(400))) {
        DisposableEffect(Unit) { onDispose { if (!state.shown) onGone() } }
        Box(Modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
            GuideCard(stringResource(R.string.guide_pinch), icon = R.drawable.outline_pinch, iconSize = 84.dp)
        }
    }
}
