package com.mycompany.app.dialog

import android.content.Context
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.BrowserSettings
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.ui.browser.SheetButtons
import com.mycompany.app.ui.browser.SheetSwitchRow

/**
 * Full screen options (Settings > Composition > Full screen, browser menu): a small animated preview of a page
 * scrolling under the bars (status bar / navigation bar shown or not, top / bottom bar hiding with the scroll or
 * fixed; hidden in landscape) and the four switches: show status bar, show navigation bar, hide the top bar on
 * scroll, hide the bottom bar on scroll. Apply stores them (`mShowStatus`, `mShowNavi`, `mFixTop`, `mFixBot`) and calls
 * [DialogApplyListener.onApply] when something changed. Compose now (on [ComposeBottomDialog]); same constructor, shows
 * itself. [DialogApplyListener] stays here (used by many classes).
 */
@Suppress("deprecation")
class DialogSetFull(mainActivity: android.app.Activity?, dialogApplyListener: DialogApplyListener?) : ComposeBottomDialog(mainActivity) {
    @JvmField var appContext: Context? = context
    @JvmField var applyListener: DialogApplyListener? = dialogApplyListener

    private var showStatus by mutableStateOf(BrowserSettings.showStatus.value)
    private var showNavi by mutableStateOf(BrowserSettings.showNavi.value)
    private var fixTop by mutableStateOf(BrowserSettings.fixTop.value)
    private var fixBot by mutableStateOf(BrowserSettings.fixBot.value)

    init {
        setSheet {
            if (LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                FullScreenPreview(showStatus, showNavi, fixTop, fixBot)
                HorizontalDivider()
            }
            SheetSwitchRow(stringResource(AppR.string.show_status), showStatus, { showStatus = it })
            SheetSwitchRow(stringResource(AppR.string.show_navi), showNavi, { showNavi = it })
            SheetSwitchRow(stringResource(AppR.string.scroll_top), !fixTop, { fixTop = !it })
            SheetSwitchRow(stringResource(AppR.string.scroll_bot), !fixBot, { fixBot = !it })
            SheetButtons({ apply() })
        }
        show()
    }

    /** Apply. */
    private fun apply() {
        if (BrowserSettings.showStatus.value != showStatus || BrowserSettings.showNavi.value != showNavi || BrowserSettings.fixTop.value != fixTop || BrowserSettings.fixBot.value != fixBot) {
            BrowserSettings.editLocal { set(BrowserSettings.showStatus, showStatus) }
            BrowserSettings.editLocal { set(BrowserSettings.showNavi, showNavi) }
            BrowserSettings.editLocal { set(BrowserSettings.fixTop, fixTop) }
            BrowserSettings.editLocal { set(BrowserSettings.fixBot, fixBot) }
            val prefWebR = PrefWeb.get(appContext, false)
            prefWebR!!.putBool("mShowStatus", BrowserSettings.showStatus.value)
            prefWebR.putBool("mShowNavi", BrowserSettings.showNavi.value)
            prefWebR.putBool("mFixTop", BrowserSettings.fixTop.value)
            prefWebR.putBool("mFixBot", BrowserSettings.fixBot.value)
            prefWebR.saveAsync()
            applyListener?.onApply()
        }
        dismiss()
    }

    /** Old landscape hook from the browser; the preview now follows the configuration itself. */
    fun applyLandscape(zz: Boolean) {}

    interface DialogApplyListener {
        fun onApply()
    }

    override fun dismiss() {
        appContext = null
        applyListener = null
        super.dismiss()
    }
}

/**
 * The full screen preview (this dialog and Settings › Toolbars & controls › Full screen): a page scrolls by up to `w`
 * and back; the bars follow unless fixed, the status / navigation bars are shown or not.
 */
@androidx.compose.runtime.Composable
fun FullScreenPreview(showStatus: Boolean, showNavi: Boolean, fixTop: Boolean, fixBot: Boolean) {
    val w = with(LocalDensity.current) { (MainApp.dp16 + MainApp.dp4).toFloat() }
    val r by rememberInfiniteTransition(label = "scroll").animateFloat(
        0f, w, infiniteRepeatable(tween((w * 20).toInt().coerceAtLeast(200), easing = LinearEasing), RepeatMode.Reverse), label = "r",
    )
    val dark = AppState.darkUi
    val suffix = if (dark) "b" else "w"
    Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier.width(160.dp).heightIn(max = 300.dp).height(300.dp)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            Box(Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)).background(if (dark) Color(0xFF3F3F3F.toInt()) else Color(0xFFDADADA.toInt()))) {
                Image(
                    painterResource(AppR.drawable.preview_matterhorn), null, contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().graphicsLayer { translationY = if (showStatus) r + w else r },
                )
                Bar(if (suffix == "b") AppR.drawable.sample_top_bar_b else AppR.drawable.sample_top_bar_w, Alignment.TopCenter) {
                    if (showStatus) (if (fixTop) w else r) else (if (fixTop) 0f else r - w)
                }
                Bar(if (suffix == "b") AppR.drawable.sample_bot_bar_b else AppR.drawable.sample_bot_bar_w, Alignment.BottomCenter) {
                    if (showNavi) (if (fixBot) -w else -r) else (if (fixBot) 0f else w - r)
                }
                if (showStatus) Bar(if (suffix == "b") AppR.drawable.sample_status_bar_b else AppR.drawable.sample_status_bar_w, Alignment.TopCenter) { 0f }
                if (showNavi) Bar(if (suffix == "b") AppR.drawable.sample_navi_bar_b else AppR.drawable.sample_navi_bar_w, Alignment.BottomCenter) { 0f }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun androidx.compose.foundation.layout.BoxScope.Bar(res: Int, align: Alignment, dy: () -> Float) {
    Image(
        painterResource(res), null, contentScale = ContentScale.FillBounds,
        modifier = Modifier.align(align).fillMaxWidth().height(20.dp).graphicsLayer { translationY = dy() },
    )
}
