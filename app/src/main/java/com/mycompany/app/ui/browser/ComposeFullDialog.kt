package com.mycompany.app.ui.browser

import android.app.Dialog
import com.mycompany.app.ui.state.AppState
import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.ComposeView
import androidx.core.view.WindowCompat
import com.mycompany.app.main.MainApp
import com.mycompany.app.ui.theme.OpumTheme

/**
 * Base of the browser's full-screen dialogs once they are Compose (the tab overview first): a plain `android.app.Dialog`
 * (old call sites keep `setOnDismissListener`, `dismiss()`) with an edge-to-edge window holding one `ComposeView` inside
 * [OpumTheme]; the content draws its own background and handles the system bar insets. The window root gets its own
 * [ComposeOwners]. [isFullOpen] answers "is it open". Subclasses call [setPage] in `init`, then [show].
 */
open class ComposeFullDialog(context: Context?) : Dialog(context!!, android.R.style.Theme_Translucent_NoTitleBar) {
    private val owners = ComposeOwners()

    init {
        window?.let {
            it.setBackgroundDrawable(ColorDrawable(AndroidColor.TRANSPARENT))
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            it.setDimAmount(0f)
            it.addFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            WindowCompat.setDecorFitsSystemWindows(it, false)
            @Suppress("DEPRECATION") it.setStatusBarColor(AndroidColor.TRANSPARENT)
            @Suppress("DEPRECATION") it.setNavigationBarColor(AndroidColor.TRANSPARENT)
            it.isNavigationBarContrastEnforced = false
            // still needed below Android 15 (from 15 on the bars are always transparent)
            @Suppress("DEPRECATION") it.setStatusBarContrastEnforced(false)
        }
    }

    /** True while the dialog is open. */
    fun isFullOpen(): Boolean = isShowing

    private val backPreview = DialogBackPreview(this) { backCloses() }

    /**
     * Whether back closes this page now (the predictive-back preview plays then). Pages whose back first steps inside
     * them (ends a selection or a search, goes up a folder) answer false while it does.
     */
    protected open fun backCloses(): Boolean = true

    override fun onStart() {
        super.onStart()
        backPreview.register()
    }

    override fun onStop() {
        backPreview.unregister()
        super.onStop()
    }

    protected fun setPage(content: @Composable () -> Unit) {
        val view = ComposeView(context)
        view.setContent {
            OpumTheme(darkTheme = AppState.darkUi) {
                // the page follows a back swipe; the browser shows around it
                androidx.compose.foundation.layout.Box(Modifier.fillMaxSize().backPage(backPreview)) { content() }
            }
        }
        setContentView(view)
        window?.let { w -> WindowCompat.getInsetsController(w, w.decorView).apply {
            isAppearanceLightStatusBars = !AppState.darkUi
            isAppearanceLightNavigationBars = !AppState.darkUi
        } }
        owners.install(window!!.decorView)
        owners.onStart()
        owners.onResume()
    }

    override fun dismiss() {
        val wasShowing = isShowing
        super.dismiss()
        if (wasShowing) owners.onDestroy()
    }
}
