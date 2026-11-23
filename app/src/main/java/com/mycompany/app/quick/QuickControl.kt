package com.mycompany.app.quick

import android.content.Context
import com.mycompany.app.pref.store.BrowserSettings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.browser.ComposeOverlay

/**
 * The selection mode of the quick-access grid: a header (back, "Quick access", the "selected / total" count and a
 * select-all toggle) and a bottom bar (Delete, Edit), with rounded corners for the grid between them. The middle is
 * empty and lets touches through to the grid underneath. Compose since 2026-11-16; same API as the old RelativeLayout:
 * [applyMode] builds it (with the quick / dark mode), [release] releases it, [setCount] updates count and buttons, [setQuickMode],
 * [setQuickView].
 */
class QuickControl(context: Context?) : ComposeOverlay(context!!) {
    /** Quick (dark page) mode. */
    @JvmField var quickMode: Boolean = false

    private var quickView: QuickView? = null
    private var mode by mutableStateOf(false)
    private var selected by mutableIntStateOf(0)
    private var total by mutableIntStateOf(0)
    private var built = false

    /** Builds the bars for [onStartPage] (quick mode). */
    fun applyMode(onStartPage: Boolean) {
        quickMode = onStartPage
        mode = onStartPage
        if (built) return
        built = true
        setPage({ MainUtil.isDarkPage(mode) }) { Bars() }
    }

    /** Releases it. */
    fun release() {
        quickView = null
    }

    /** Shows [newSelected] of [newTotal] selected: Delete needs one or more, Edit exactly one; all selected checks the toggle. */
    fun setCount(newSelected: Int, newTotal: Int) {
        selected = newSelected
        total = newTotal
    }

    fun setColor(night: Boolean) {
        quickMode = night
        mode = night
    }

    fun setQuickMode(onStartPage: Boolean) {
        if (quickMode == onStartPage) return
        setColor(onStartPage)
    }

    fun setQuickView(quickView: QuickView?) {
        this.quickView = quickView
    }

    // ---- actions (as the old click listeners) ----

    private fun back() {
        quickView?.exitEditMode()
    }

    private fun toggleAll() {
        val qv = quickView ?: return
        val adapter = qv.itemAdapter ?: return
        if (qv.controlPanel == null) return
        adapter.setAllChecked(!adapter.allChecked(), true)
        qv.controlPanel!!.setCount(adapter.checkedCount(), adapter.bookCount())
    }

    private fun delete() {
        val qv = quickView ?: return
        val adapter = qv.itemAdapter ?: return
        val l = qv.quickListener ?: return
        val n = adapter.checkedCount()
        if (n == 0) return
        if (n == 1) l.showDeleteDialog(adapter.firstChecked(), n) else l.showDeleteDialog(null, n)
    }

    private fun edit() {
        val qv = quickView ?: return
        val adapter = qv.itemAdapter ?: return
        val l = qv.quickListener ?: return
        l.showEditDialog(adapter.firstChecked())
    }

    @Composable
    private fun Bars() {
        val density = LocalDensity.current
        val night = MainUtil.isDarkPage(mode)
        val barColor = if (night) Color.Black else Color(0xFFF8F8F8)
        val textColor = if (night) Color(0xFFFAFAFA) else Color.Black
        val action = if (night) Color(0xFFFAFAFA) else Color(0xFF1E66C8)
        val actionOff = if (night) Color(0xFF808080) else Color(0xFFDADADA)
        val barH = with(density) { MainApp.dp48.toDp() }
        val headH = with(density) {
            val x = if (mode && !MainUtil.hasBottomTitle()) BrowserSettings.tabBar.value else 0
            (if (mode && !MainUtil.hasBottomTitle() && x != 1 && x != 2) MainApp.dp40 else MainApp.dp48).toDp()
        }
        val corner = with(density) { MainApp.dp16.toDp() }
        val edge = with(density) { MainApp.dp8.toDp() }
        val button = with(density) { MainApp.dp48.toDp() }
        Column(Modifier.fillMaxSize()) {
            // header; taps on it must not reach the grid
            Row(
                Modifier.fillMaxWidth().height(headH).background(barColor)
                    .clickable(remember { MutableInteractionSource() }, null) { },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { back() }, modifier = Modifier.padding(start = edge).size(button)) {
                    Icon(painterResource(R.drawable.outline_chevron_left_black_24), contentDescription = null, tint = textColor)
                }
                Text(
                    stringResource(R.string.quick_access), color = textColor, fontSize = 18.sp, maxLines = 1,
                    overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f).padding(start = (with(density) { MainApp.dp64.toDp() } - button - edge).coerceAtLeast(androidx.compose.ui.unit.Dp(0f))),
                )
                Text(MainUtil.countLabel(selected, total), color = textColor, fontSize = 16.sp, maxLines = 1, modifier = Modifier.padding(horizontal = edge))
                val all = total > 0 && selected >= total
                IconButton(onClick = { toggleAll() }, modifier = Modifier.size(button)) {
                    Icon(
                        painterResource(if (all) R.drawable.baseline_check_circle_black_24 else R.drawable.outline_radio_button_unchecked_black_24),
                        contentDescription = null, tint = if (all && !night) Color(0xFF1E66C8) else textColor,
                    )
                }
            }
            Box(Modifier.fillMaxWidth().weight(1f)) {
                if (mode) {
                    Box(Modifier.fillMaxWidth().height(with(density) { MainApp.dp16.toDp() })
                        .background(Brush.verticalGradient(listOf(Color(0x26000000), Color.Transparent))))
                }
                Corner(if (night) R.drawable.round_top_left_b else R.drawable.round_top_left_g, corner, Alignment.TopStart)
                Corner(if (night) R.drawable.round_top_right_b else R.drawable.round_top_right_g, corner, Alignment.TopEnd)
                Corner(if (night) R.drawable.round_bot_left_b else R.drawable.round_bot_left_g, corner, Alignment.BottomStart)
                Corner(if (night) R.drawable.round_bot_right_b else R.drawable.round_bot_right_g, corner, Alignment.BottomEnd)
            }
            Row(Modifier.fillMaxWidth().height(barH).background(barColor)) {
                val canDelete = selected > 0
                TextButton(
                    onClick = { delete() }, enabled = canDelete, modifier = Modifier.weight(1f).fillMaxHeight(),
                    colors = ButtonDefaults.textButtonColors(contentColor = action, disabledContentColor = actionOff),
                ) { Text(stringResource(R.string.delete), fontSize = 16.sp) }
                val canEdit = selected == 1
                TextButton(
                    onClick = { edit() }, enabled = canEdit, modifier = Modifier.weight(1f).fillMaxHeight(),
                    colors = ButtonDefaults.textButtonColors(contentColor = action, disabledContentColor = actionOff),
                ) { Text(stringResource(R.string.edit), fontSize = 16.sp) }
            }
        }
    }

    @Composable
    private fun androidx.compose.foundation.layout.BoxScope.Corner(res: Int, size: Dp, align: Alignment) {
        Icon(painterResource(res), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.align(align).size(size))
    }
}
