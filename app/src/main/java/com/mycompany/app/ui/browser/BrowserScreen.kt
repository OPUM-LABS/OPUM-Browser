package com.mycompany.app.ui.browser

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.viewinterop.AndroidView

/**
 * The browser screen's Compose root. For now it only hosts
 * the existing hand-built layout ([legacyRoot], the `MyWebBody` with the coordinator, bars and page) full size; the next
 * stages move the bars and the overlays out of that tree into this composable. It also owns the bar
 * scroll-hide state ([BarScroll], Material 3 enter-always); the views that sat on top of the page body (floating
 * buttons, full-size panels) are its children too ([OverlayStack]).
 *
 * Window insets keep working as before: `MainActivity` reads them on the window's decor view with its own listener and
 * applies the paddings itself, independent of what the content view is.
 */
object BrowserScreen {
    /**
     * Makes a Compose view holding [legacyRoot] the activity's content view and returns it. [bars] gets its Material 3
     * scroll behaviour from this composition.
     */
    @JvmStatic
    fun setContent(activity: Activity, legacyRoot: View, bars: BarScroll, overlays: OverlayStack): ComposeView {
        val compose = ComposeView(activity)
        compose.setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
        compose.setContent { Screen(legacyRoot, bars, overlays) }
        activity.setContentView(compose)
        return compose
    }

    @Composable
    private fun Screen(legacyRoot: View, bars: BarScroll, overlays: OverlayStack) {
        bars.Install()
        Box(Modifier.fillMaxSize()) {
            AndroidView(
                factory = { _ -> (legacyRoot.parent as? ViewGroup)?.removeView(legacyRoot); legacyRoot },
                modifier = Modifier.fillMaxSize(),
            )
            // What used to be stacked on top of the page body, in the order it was added.
            overlays.Layer()
        }
    }
}
