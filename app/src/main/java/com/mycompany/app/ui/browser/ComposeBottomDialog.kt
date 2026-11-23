package com.mycompany.app.ui.browser

import android.app.Dialog
import com.mycompany.app.ui.state.AppState
import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.mycompany.app.ui.theme.OpumDimens
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.mycompany.app.main.MainApp
import com.mycompany.app.ui.theme.OpumTheme

/**
 * Base of the browser's small bottom dialogs once they are Compose. It is a plain `android.app.Dialog` (so the old call
 * sites keep working: constructor with the activity, `setOnDismissListener`, `setOnCancelListener`, `dismiss()`), with a
 * translucent full-screen window that holds one `ComposeView`: a scrim (tap = close, like the system back key) and a
 * Material 3 sheet at the bottom. The window root gets its own [ComposeOwners], so it needs nothing from the legacy
 * activity. [isOpen] answers "is it open" for the activity's "a dialog is up" checks. Subclasses call [setSheet] in `init`
 * and then [show]; the old dialogs showed themselves when built, so the ported ones do too.
 */
open class ComposeBottomDialog(context: Context?) : Dialog(context!!, android.R.style.Theme_Translucent_NoTitleBar) {
    private val owners = ComposeOwners()

    init {
        window?.let {
            it.setBackgroundDrawable(ColorDrawable(AndroidColor.TRANSPARENT))
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            it.setDimAmount(0f)
            @Suppress("DEPRECATION") it.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            it.addFlags(android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or android.view.WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            WindowCompat.setDecorFitsSystemWindows(it, false)
            @Suppress("DEPRECATION") it.setStatusBarColor(AndroidColor.TRANSPARENT)
            @Suppress("DEPRECATION") it.setNavigationBarColor(AndroidColor.TRANSPARENT)
            it.isNavigationBarContrastEnforced = false
            // still needed below Android 15 (from 15 on the bars are always transparent)
            @Suppress("DEPRECATION") it.setStatusBarContrastEnforced(false)
        }
    }

    private val noDimState = androidx.compose.runtime.mutableStateOf(false)

    /**
     * No grey shade behind the sheet: set by the sheets that open over a full-screen video (download, video list), so
     * the video stays fully visible. Tapping beside the sheet still closes it. Compose state, so it also applies when
     * set after [show].
     */
    var noDim: Boolean
        get() = noDimState.value
        set(value) { noDimState.value = value }

    /** True while the dialog is open. */
    fun isOpen(): Boolean = isShowing

    /**
     * Placement hook (callers pass the bar-aware margins / colours of the page behind the
     * dialog). The Compose sheet is always at the bottom of the window with the navigation-bar inset, so it is ignored.
     */
    @Suppress("UNUSED_PARAMETER")
    fun applyColors(onStartPage: Boolean, dark: Boolean, themeColor: Int, navColor: Int, animate: Boolean) {}

    /** True while the dialog is open. */
    fun isShownNow(): Boolean = isShowing

    /** Sets what the sheet shows; [content] is laid out in a column inside the sheet. */
    protected fun setSheet(content: @androidx.compose.runtime.Composable ColumnScope.() -> Unit) {
        val view = ComposeView(context)
        view.setContent {
            @Suppress("UNUSED_VARIABLE") val tick = themeTick.intValue
            OpumTheme(darkTheme = AppState.darkUi) {
                val none = remember { MutableInteractionSource() }
                Box(
                    Modifier.fillMaxSize().background(Color.Black.copy(alpha = if (noDim) 0f else scrimAlpha))
                        .clickable(interactionSource = none, indication = null) { dismissFromScrim() }
                        // a tall sheet stops below the status bar (the scrim still covers it)
                        .statusBarsPadding().padding(top = 32.dp),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    // 2026-11-20: every sheet has the drag handle; dragging it down closes the
                    // sheet (like the scrim), a short drag springs back
                    val scope = rememberCoroutineScope()
                    val drag = remember { Animatable(0f) }
                    val closeAt = with(LocalDensity.current) { 96.dp.toPx() }
                    Surface(
                        Modifier.fillMaxWidth().widthIn(max = 640.dp)
                            .offset { IntOffset(0, drag.value.roundToInt()) }
                            .clickable(interactionSource = none, indication = null) { },
                        shape = RoundedCornerShape(topStart = OpumDimens.sheetCorner, topEnd = OpumDimens.sheetCorner),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                    ) {
                        Column(Modifier.navigationBarsPadding().imePadding()) {
                            SheetHandle(
                                Modifier.draggable(
                                    orientation = Orientation.Vertical,
                                    state = rememberDraggableState { delta -> scope.launch { drag.snapTo((drag.value + delta).coerceAtLeast(0f)) } },
                                    onDragStopped = {
                                        if (drag.value > closeAt && canCancelFromScrim()) cancel()
                                        else scope.launch { drag.animateTo(0f) }
                                    },
                                ),
                            )
                            content()
                        }
                    }
                }
            }
        }
        setContentView(view)
        owners.install(window!!.decorView)
        owners.onStart()
        owners.onResume()
    }

    /**
     * A centred card instead of a sheet (2026-11-20): for short destructive questions ("Delete all?"), which the plan
     * keeps in the middle of the screen. Same scrim and window as [setSheet]; [content] is laid out in a column.
     */
    protected fun setCard(content: @androidx.compose.runtime.Composable ColumnScope.() -> Unit) {
        val view = ComposeView(context)
        view.setContent {
            @Suppress("UNUSED_VARIABLE") val tick = themeTick.intValue
            OpumTheme(darkTheme = AppState.darkUi) {
                val none = remember { MutableInteractionSource() }
                Box(
                    Modifier.fillMaxSize().background(Color.Black.copy(alpha = if (noDim) 0f else scrimAlpha))
                        .clickable(interactionSource = none, indication = null) { dismissFromScrim() }
                        .systemBarsPadding().imePadding().padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        Modifier.widthIn(min = 280.dp, max = 400.dp).clickable(interactionSource = none, indication = null) { },
                        shape = RoundedCornerShape(OpumDimens.sheetCorner),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Column(Modifier.padding(top = 24.dp, bottom = 8.dp)) { content() }
                    }
                }
            }
        }
        setContentView(view)
        owners.install(window!!.decorView)
        owners.onStart()
        owners.onResume()
    }

    /**
     * Like [setSheet], but [content] gets the whole window (no scrim, no sheet): for dialogs that draw their own layout,
     * such as the floating link preview ([com.mycompany.app.dialog.DialogWebView], 2026-11-20).
     */
    protected fun setScreen(content: @androidx.compose.runtime.Composable () -> Unit) {
        val view = ComposeView(context)
        view.setContent {
            @Suppress("UNUSED_VARIABLE") val tick = themeTick.intValue
            OpumTheme(darkTheme = AppState.darkUi) { content() }
        }
        setContentView(view)
        owners.install(window!!.decorView)
        owners.onStart()
        owners.onResume()
    }

    /** Closes the dialog the way a tap on the scrim does (respecting [canCancelFromScrim]). */
    protected fun cancelFromOutside() = dismissFromScrim()

    private val themeTick = androidx.compose.runtime.mutableIntStateOf(0)

    /** Redraws the sheet with the current app theme (`AppState.darkUi`), for sheets that switch the theme while open. */
    protected fun refreshTheme() { themeTick.intValue++ }

    /** How dark the scrim behind the sheet is; the screen-filter editor wants none so the page shows its real colours. */
    protected open val scrimAlpha: Float get() = 0.32f

    /** Subclasses return false while they are busy (the old dialogs blocked touches then). */
    protected open fun canCancelFromScrim(): Boolean = true

    private fun dismissFromScrim() { if (canCancelFromScrim()) cancel() }

    override fun dismiss() {
        val wasShowing = isShowing
        super.dismiss()
        if (wasShowing) owners.onDestroy()
    }
}
