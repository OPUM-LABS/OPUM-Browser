package com.mycompany.app.ui.browser

import android.app.Dialog
import android.os.Build
import android.window.BackEvent
import android.window.OnBackAnimationCallback
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Predictive back for the Compose dialogs ([ComposeBottomDialog], [ComposeFullDialog]), 2026-11-27: while a back swipe
 * is held, the sheet or page follows the finger (shrinks, moves), letting go closes it, swiping back cancels and it
 * springs back. Android 14 and later; before, back stays as it was. The preview only plays when [closes] says back will
 * close the dialog now: a back that first ends a selection, a search or leaves a folder ([Dialog.onBackPressed] of the
 * dialog decides, as before) shows no preview. The back itself always goes to [Dialog.onBackPressed].
 */
internal class DialogBackPreview(private val dialog: Dialog, private val closes: () -> Boolean) {
    /** 0 (not moved) .. 1 (fully pulled). */
    var progress by mutableFloatStateOf(0f)
        private set
    private var dragging by mutableStateOf(false)
    /** The swipe started at the right edge (the page moves left then). */
    private var fromRight by mutableStateOf(false)
    private var callback: OnBackInvokedCallback? = null

    /** On [Dialog.onStart]: after the dialog's own callback, so this one is asked first. */
    fun register() {
        if (Build.VERSION.SDK_INT < 34 || callback != null) return
        val cb = object : OnBackAnimationCallback {
            override fun onBackStarted(backEvent: BackEvent) {
                dragging = closes()
                fromRight = backEvent.swipeEdge == BackEvent.EDGE_RIGHT
                progress = 0f
            }

            override fun onBackProgressed(backEvent: BackEvent) {
                if (dragging) progress = backEvent.progress
            }

            override fun onBackCancelled() {
                dragging = false
                progress = 0f
            }

            override fun onBackInvoked() {
                dragging = false
                @Suppress("DEPRECATION") dialog.onBackPressed()
                // still open (back stepped inside it): spring back
                progress = 0f
            }
        }
        dialog.onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, cb)
        callback = cb
    }

    /** On [Dialog.onStop]. */
    fun unregister() {
        val cb = callback ?: return
        callback = null
        if (Build.VERSION.SDK_INT >= 33) dialog.onBackInvokedDispatcher.unregisterOnBackInvokedCallback(cb)
        dragging = false
        progress = 0f
    }

    /** The progress as drawn: follows the finger exactly, eases back after a cancel. */
    @Composable
    internal fun shown(): Float {
        // eased like the system's own preview: most of the movement in the first part of the swipe
        val eased = 1f - (1f - progress).let { it * it * it }
        return animateFloatAsState(eased, if (dragging) snap() else spring(), label = "back").value
    }

    /** The swipe came from the right edge. */
    internal val rightEdge: Boolean get() = fromRight

    /** How much of the scrim stays (it fades as the sheet is pulled away). */
    @Composable
    fun scrimFactor(): Float = 1f - 0.5f * shown()
}

/** A bottom sheet following a back swipe ([DialogBackPreview]): shrinks towards its bottom edge and sinks a little. */
@Composable
internal fun Modifier.backSheet(preview: DialogBackPreview): Modifier {
    val p = preview.shown()
    return this.graphicsLayer {
        val scale = 1f - 0.08f * p
        scaleX = scale
        scaleY = scale
        transformOrigin = TransformOrigin(0.5f, 1f)
        translationY = 24.dp.toPx() * p
    }
}

/** A centred card following a back swipe: shrinks in place. */
@Composable
internal fun Modifier.backCard(preview: DialogBackPreview): Modifier {
    val p = preview.shown()
    return this.graphicsLayer {
        val scale = 1f - 0.1f * p
        scaleX = scale
        scaleY = scale
    }
}

/** A full-screen page following a back swipe: shrinks to 90 %, rounds its corners, moves away from the swipe's edge. */
@Composable
internal fun Modifier.backPage(preview: DialogBackPreview): Modifier {
    val p = preview.shown()
    val right = preview.rightEdge
    return this.graphicsLayer {
        val scale = 1f - 0.1f * p
        scaleX = scale
        scaleY = scale
        translationX = (if (right) -1f else 1f) * 8.dp.toPx() * p
        shape = RoundedCornerShape(32.dp * p)
        clip = p > 0f
    }
}
