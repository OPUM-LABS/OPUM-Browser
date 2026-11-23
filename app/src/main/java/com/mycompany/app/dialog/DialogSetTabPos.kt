package com.mycompany.app.dialog

import android.content.Context
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.BrowserSettings
import android.os.Handler
import android.os.Looper
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.ui.browser.SheetButtons
import com.mycompany.app.ui.browser.SheetChoiceRow

/**
 * Tab bar position (Settings > Tab > Show tab bar): a preview of the screen with the status / top / bottom /
 * navigation bars and the tab bar (highlighted) where it would go, with up / down buttons to move it, a help button
 * ([DialogGuide] 5), and the choice row (not shown, above / below the top bar, above / below the bottom bar; [tabBarOrder] /
 * [tabBarLabels]). Apply stores it (`mTabBar2`) and calls [DialogApplyListener.onApply]. The preview is hidden in landscape on small
 * screens, as before. Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogSetTabPos(mainActivity: android.app.Activity?, dialogApplyListener: DialogApplyListener?) : ComposeBottomDialog(mainActivity) {
    @JvmField var hostActivity: android.app.Activity? = mainActivity
    @JvmField var appContext: Context? = context
    @JvmField var applyListener: DialogApplyListener? = dialogApplyListener
    @JvmField var guideDialog: DialogGuide? = null

    private var tabBar by mutableIntStateOf(BrowserSettings.tabBar.value)
    private val hidePreview: Boolean = run {
        val land = (mainActivity as? com.mycompany.app.main.MainActivity)?.isLandscapeScreen() ?: MainUtil.isLandscape(context)
        land && ((mainActivity as? com.mycompany.app.main.MainActivity)?.smallScreen ?: MainUtil.isSmallScreen(context))
    }

    init {
        setSheet {
            if (!hidePreview) {
                Box(Modifier.fillMaxWidth()) {
                    Preview()
                    IconButton(onClick = { help() }, modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                        Icon(painterResource(AppR.drawable.outline_help_black_24), null)
                    }
                    if (tabBar != 0) {
                        Column(Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                            FilledTonalIconButton(onClick = { if (tabBar > 1) tabBar-- }) {
                                Icon(painterResource(AppR.drawable.outline_keyboard_arrow_up_black_24), null)
                            }
                            FilledTonalIconButton(onClick = { if (tabBar < 4) tabBar++ }, modifier = Modifier.padding(top = 8.dp)) {
                                Icon(painterResource(AppR.drawable.outline_keyboard_arrow_down_black_24), null)
                            }
                        }
                    }
                }
                HorizontalDivider()
            }
            SheetChoiceRow(
                stringResource(AppR.string.show_tab_bar), tabBarOrder.map { tabBarLabels[it] }, tabBarOrder.indexOf(tabBar), { tabBar = tabBarOrder[it % 5] },
                value = stringResource(tabBarLabels[tabBar]),
            )
            SheetButtons({ apply() })
        }
        show()
    }

    @androidx.compose.runtime.Composable
    private fun Preview() {
        val h = 20.dp
        val tabBarNow = tabBar
        // distances from the top (top bar, tab bar) or the bottom (bottom bar, tab bar), as in the old B()
        val top by animateDpAsState(if (tabBarNow == 1) h * 2 else h, label = "top")
        val bot by animateDpAsState(if (tabBarNow == 4) h * 2 else h, label = "bot")
        val tabTop = tabBarNow == 1 || tabBarNow == 2
        val tab by animateDpAsState(
            when (tabBarNow) { 1 -> h; 2 -> h * 2; 3 -> h * 2; 4 -> h; else -> h }, label = "tab",
        )
        val dark = com.mycompany.app.ui.state.AppState.darkUi
        Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.width(160.dp).height(300.dp)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 16.dp)
            ) {
                Box(Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)).background(if (dark) Color(0xFF3F3F3F.toInt()) else Color(0xFFF8F8F8.toInt()))) {
                    Image(painterResource(AppR.drawable.preview_matterhorn), null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    Bar(if (dark) AppR.drawable.sample_top_bar_b else AppR.drawable.sample_top_bar_w, true, top)
                    Bar(if (dark) AppR.drawable.sample_bot_bar_b else AppR.drawable.sample_bot_bar_w, false, bot)
                    if (tabBarNow != 0) {
                        Bar(if (dark) AppR.drawable.sample_tab_bar_b else AppR.drawable.sample_tab_bar_w, tabTop, tab, highlight = true)
                    }
                    Bar(if (dark) AppR.drawable.sample_status_bar_b else AppR.drawable.sample_status_bar_w, true, 0.dp)
                    Bar(if (dark) AppR.drawable.sample_navi_bar_b else AppR.drawable.sample_navi_bar_w, false, 0.dp)
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun BoxScope.Bar(res: Int, fromTop: Boolean, gap: Dp, highlight: Boolean = false) {
        val m = Modifier.align(if (fromTop) Alignment.TopCenter else Alignment.BottomCenter)
            .offset(y = if (fromTop) gap else -gap).fillMaxWidth().height(20.dp)
        Box(m) {
            Image(painterResource(res), null, contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize())
            if (highlight) Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)))
        }
    }

    /** Help. */
    private fun help() {
        if (hostActivity == null || guideDialog != null) return
        val guide = DialogGuide(hostActivity, 5)
        guideDialog = guide
        guide.setOnDismissListener {
            guideDialog?.dismiss()
            guideDialog = null
        }
    }

    /** Apply. */
    private fun apply() {
        val tabBarNow = tabBar
        if (BrowserSettings.tabBar.value != tabBarNow) {
            BrowserSettings.edit(appContext) { set(BrowserSettings.tabBar, tabBarNow) }
            applyListener?.onApply()
        }
        dismiss()
    }

    override fun dismiss() {
        guideDialog?.dismiss()
        guideDialog = null
        hostActivity = null
        appContext = null
        applyListener = null
        super.dismiss()
    }

    companion object {
        @JvmField
        val tabBarOrder: IntArray = intArrayOf(1, 2, 3, 4, 0)
        @JvmField
        val tabBarLabels: IntArray = intArrayOf(
            AppR.string.not_show,
            AppR.string.above_top,
            AppR.string.below_top,
            AppR.string.above_bot,
            AppR.string.below_bot
        )
    }
}
