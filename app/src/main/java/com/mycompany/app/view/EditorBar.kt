package com.mycompany.app.view

import com.mycompany.app.ui.browser.BarFace
import com.mycompany.app.ui.state.AppState

import android.content.Context
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.browser.ComposeOverlay
import com.mycompany.app.view.MyBarView.BarListener

/**
 * The address editor row of the browser (what `MyAddrView` was): engine icon, the `MyEditAuto` text field, clear / copy /
 * delete icons and the configurable extra buttons, on a rounded field over the bar colour. Compose now. The activity's
 * fields and calls stay: [engineIcon]..[deleteIcon] are the four icons as [AddrIcon] state, [useExtraIds] sets the extra buttons, [applyColors] / [applyExtraTint] apply the
 * bar colours, [showExtras] shows or hides the extras, [setListener] gets their taps. The text field is still the real
 * `MyEditAuto` (hosted in an `AndroidView`): the activity wires its listeners, IME flags and autocomplete directly.
 * (`MyAddrView` stays for the address-button settings dialog, which previews a bar with its own icon views.)
 */
class EditorBar(context: Context) : ComposeOverlay(context) {
    val engineIcon = AddrIcon(this)
    val leftIcon = AddrIcon(this)
    val rightIcon = AddrIcon(this)
    val deleteIcon = AddrIcon(this)

    /** True when the bar is at the top of the screen (the separator line is drawn at the bottom then). */
    @JvmField var editing: Boolean = false

    private var edit: MyEditAuto? = null
    private var listener: BarListener? = null
    private var extraIds by mutableStateOf<IntArray?>(null)
    private var extrasShown by mutableStateOf(true)
    private var extraTint by mutableIntStateOf(0)
    private var extraAlpha by androidx.compose.runtime.mutableFloatStateOf(1f)
    private var background by mutableIntStateOf(0)
    private var field by mutableIntStateOf(0)
    private var line by mutableIntStateOf(0)

    init {
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setPage({ AppState.darkUi }) { Bar() }
    }

    /** The text field to host; call once before the bar is shown. */
    fun setEdit(field: MyEditAuto) { edit = field }

    fun setListener(barListener: BarListener?) { listener = barListener }

    /** Sets the configurable extra buttons (ids 0..2 are shown; others are hidden). */
    fun useExtraIds(ids: IntArray?) { extraIds = ids }

    /** Bar colours: [barColor] bar colour setting, [lineColor] accent, [transparent] the bar is transparent over the page, [newEditing] bar at the top. */
    fun applyColors(barColor: Int, lineColor: Int, transparent: Boolean, newEditing: Boolean) {
        background = if (transparent) 0 else if (barColor == 0) -1 else if (barColor == 1) -16777216 else barColor
        field = BarFace.Companion.pillColorFor(barColor, lineColor, false)
        line = if (!transparent && barColor == 0) -2115968800 else 0
        editing = newEditing
    }

    /** Extra-button icon set, alpha and press colour for the bar colour [newExtraTint]. */
    fun applyExtraTint(newExtraTint: Int, tint: Int) {
        extraTint = newExtraTint
        extraAlpha = BarFace.Companion.pressScale(newExtraTint)
    }

    /** [shown]: the text is empty, so the extras (paste helpers) show; otherwise they hide. */
    fun showExtras(shown: Boolean) { extrasShown = shown }

    /** Releases the bar (the activity is going away). */
    fun release() {
        listener = null
        (edit?.parent as? ViewGroup)?.removeView(edit)
        edit = null
        engineIcon.release(); leftIcon.release(); rightIcon.release(); deleteIcon.release()
    }

    @Composable
    private fun px(value: Int): Dp = with(LocalDensity.current) { value.toDp() }

    @Composable
    private fun Slot(icon: AddrIcon) {
        if (!icon.visible) return
        Box(
            Modifier.width(px(MainApp.dp48)).height(px(MainApp.dp48)).alpha(icon.alpha).clickable { icon.performClick() },
            contentAlignment = Alignment.Center,
        ) {
            val bitmap = icon.bitmap
            if (bitmap != null) Image(bitmap.asImageBitmap(), null, Modifier.size(24.dp))
            else if (icon.res != 0) {
                // The engine icons (magnifier logos, colour dots) exist only in the dark #212121 ink: on a dark bar (the
                // "_dark" icon set, extraTint != 0) tint them the light ink of the other bar icons (2026-11-22).
                val light = icon === engineIcon && extraTint != 0
                Image(painterResource(icon.res), null, colorFilter = if (light) ColorFilter.tint(Color(0xFFF1F1F1)) else null)
            }
        }
    }

    @Composable
    private fun Bar() {
        val rowHeight = px(MainApp.dp48)
        val editor = edit
        Box(Modifier.fillMaxSize().background(Color(background))) {
            Box(Modifier.fillMaxWidth().align(Alignment.Center).padding(horizontal = px(MainApp.dp4)).height(rowHeight - px(MainApp.dp8) * 2)
                .background(Color(field), RoundedCornerShape(px(MainApp.dp8))))
            if (line != 0) {
                Box(
                    Modifier.fillMaxWidth().height(px(MainApp.dp1)).background(Color(line))
                        .align(if (editing) Alignment.BottomCenter else Alignment.TopCenter),
                )
            }
            Row(Modifier.fillMaxWidth().height(rowHeight).align(Alignment.Center), verticalAlignment = Alignment.CenterVertically) {
                Slot(engineIcon)
                if (editor != null) {
                    AndroidView(
                        factory = { _ ->
                            (editor.parent as? ViewGroup)?.removeView(editor)
                            editor
                        },
                        modifier = Modifier.weight(1f).height(rowHeight),
                    )
                } else Box(Modifier.weight(1f))
                Slot(leftIcon)
                Slot(rightIcon)
                Slot(deleteIcon)
                val ids = extraIds
                if (ids != null && extrasShown) {
                    for (index in ids.indices) {
                        val id = ids[index]
                        if (id < 0 || id >= 3) continue
                        Box(
                            Modifier.width(px(MainApp.dp48)).height(rowHeight).alpha(extraAlpha)
                                .clickable { listener?.onItemClick(id, this@EditorBar, false) },
                            contentAlignment = Alignment.Center,
                        ) { Image(painterResource(MainUtil.themeColorAt(id, extraTint)), null) }
                    }
                }
            }
        }
    }
}
