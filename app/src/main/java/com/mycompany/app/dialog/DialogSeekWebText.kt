package com.mycompany.app.dialog

import com.mycompany.app.ui.components.AppSlider
import android.view.View
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.ZoneSettings
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.mycompany.app.ui.components.TickSwitch as Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogEditorText.EditorSetListener
import com.mycompany.app.dialog.DialogSetImage.ChangedListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.web.WebNestView
import com.mycompany.app.wview.WebFltView
import com.mycompany.app.wview.WebFltView.FltTouchListener
import com.mycompany.app.wview.WebFltView.FltViewListener
import kotlin.math.roundToInt

/**
 * "Text zoom in" for the page: a floating zoom button (switch, its colour, the enlarged size 50-500 %) and the page's
 * normal text size (50-500 %, applied to the page live). The real floating button is shown above the sheet as a
 * preview: drag it to move it, tap it to flip the page between the two sizes. Apply stores everything (and calls the
 * listener); Reset (after asking) goes back to off / 200 % / 100 % / the default colour and forgets the stored values;
 * closing without Apply puts the page text size and the button colour back. No scrim, so the page stays visible.
 * Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogSeekWebText(
    mainActivity: MainActivity?,
    webNestView: WebNestView?,
    changedListener: ChangedListener?,
) : ComposeBottomDialog(mainActivity) {
    private var listener: ChangedListener? = changedListener
    private var page: WebNestView? = webNestView
    private val activity: MainActivity? = mainActivity

    private var zoomIcon by mutableStateOf(ZtriSettings.zoomIcon.value)
    private var zoomSize by mutableIntStateOf(ZtriSettings.zoomSize.value.let { if (it < MIN || it > MAX) 100 else it })
    private var textSize by mutableIntStateOf(ZoneSettings.textSize.value.let { if (it < MIN || it > MAX) 100 else it })
    private var colourTick by mutableIntStateOf(0)
    private var resetAsk by mutableStateOf(false)
    private var sheetHeight by mutableIntStateOf(0)

    // what to go back to when the dialog closes without Apply
    private var pageZoomAtOpen: Int = webNestView?.getSettings()?.textZoom ?: 100
    private var pageChanged = false
    private var colourAlpha = EditorSettings.zoomAlpha.value
    private var colourValue = EditorSettings.zoomColor.value
    private var colourPos = EditorSettings.zoomPos.value

    private var colourDialog: DialogEditIcon? = null
    private var flt: WebFltView? = null
    /** The frame holding the floating button preview; the activity hides it while the bars are away. */
    @JvmField
    var sampleFrame: FrameLayout? = null
    private var overlay: FrameLayout? = null

    override val scrimAlpha: Float get() = 0f

    /** Whether the activity runs on a small screen. */
    fun isSmallScreen(): Boolean = activity?.smallScreen ?: false

    private fun setPageZoom(value: Int) {
        val p = page ?: return
        p.getSettings().setTextZoom(value)
        pageChanged = true
    }

    private fun changeTextSize(newValue: Int) {
        val newSize = newValue.coerceIn(MIN, MAX)
        if (newSize == textSize) return
        textSize = newSize
        setPageZoom(newSize)
    }

    private fun changeZoomSize(newValue: Int) {
        zoomSize = newValue.coerceIn(MIN, MAX)
    }

    private fun chooseSwitch(on: Boolean) {
        zoomIcon = on
        val floatingButton = flt ?: return
        if (on) floatingButton.show(true) else floatingButton.hide(true)
    }

    private fun colourDiffers(alpha: Int, colour: Int, pos: Float): Boolean =
        !(EditorSettings.zoomAlpha.value == alpha && EditorSettings.zoomColor.value == colour && EditorSettings.zoomPos.value.compareTo(pos) == 0)

    private fun storeColour(alpha: Int, colour: Int, pos: Float) {
        EditorSettings.editLocal { set(EditorSettings.zoomAlpha, alpha) }
        EditorSettings.editLocal { set(EditorSettings.zoomColor, colour) }
        EditorSettings.editLocal { set(EditorSettings.zoomPos, pos) }
        PrefEditor.zoomArgb = PrefEditor.withAlpha(colour, alpha)
        val core = PrefEditor.get(context)
        core!!.putInt(EditorSettings.zoomAlpha.value, "mZoomAlpha")
        core.putInt(EditorSettings.zoomColor.value, "mZoomColor")
        core.putFloat("mZoomPos", EditorSettings.zoomPos.value)
        core.saveAsync()
    }

    private fun openColour() {
        if (colourDialog != null) return
        val dialogEditIcon = DialogEditIcon(activity, 2, object : EditorSetListener {
            override fun onApply(color: Int, label: String?) {
                colourTick++
                flt?.refreshColors()
            }
        })
        dialogEditIcon.setOnDismissListener { colourDialog = null }
        colourDialog = dialogEditIcon
    }

    init {
        setSheet {
            Column(Modifier.onSizeChanged { sheetHeight = it.height }.verticalScroll(rememberScrollState())) {
                Row(
                    Modifier.fillMaxWidth().clickable { chooseSwitch(!zoomIcon) }.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f).padding(end = 16.dp)) {
                        Text(stringResource(R.string.zoom_icon), style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.drag_move_guide), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = zoomIcon, onCheckedChange = { chooseSwitch(it) })
                }
                HorizontalDivider()
                Row(
                    Modifier.fillMaxWidth().alpha(if (zoomIcon) 1f else 0.2f).clickable(enabled = zoomIcon) { openColour() }.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(stringResource(R.string.icon_color), style = MaterialTheme.typography.titleMedium)
                    colourTick.let {
                        Box(
                            Modifier.size(48.dp).background(Color(PrefEditor.withAlpha(EditorSettings.zoomColor.value, EditorSettings.zoomAlpha.value)), RoundedCornerShape(12.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(12.dp)),
                        )
                    }
                }
                SliderRow(R.string.zoom_size, zoomSize, zoomIcon, { changeZoomSize(it) })
                SliderRow(R.string.default_size, textSize, true, { changeTextSize(it) })
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { resetAsk = true }) { Text(stringResource(R.string.reset)) }
                    TextButton(onClick = { save(true) }) { Text(stringResource(R.string.apply)) }
                }
                if (resetAsk) {
                    AlertDialog(
                        onDismissRequest = { resetAsk = false },
                        text = { Text(stringResource(R.string.reset_setting)) },
                        confirmButton = { TextButton(colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = androidx.compose.material3.MaterialTheme.colorScheme.error), onClick = { resetAsk = false; reset() }) { Text(stringResource(R.string.reset)) } },
                        dismissButton = { TextButton(onClick = { resetAsk = false }) { Text(stringResource(R.string.cancel)) } },
                    )
                }
            }
        }
        show()
        addPreview()
    }

    @androidx.compose.runtime.Composable
    private fun SliderRow(title: Int, value: Int, enabled: Boolean, onChange: (Int) -> Unit) {
        Column(Modifier.alpha(if (enabled) 1f else 0.2f)) {
            Row(
                Modifier.fillMaxWidth().padding(top = 12.dp, start = 24.dp, end = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(title), style = MaterialTheme.typography.titleMedium)
                Text("$value%", style = MaterialTheme.typography.titleMedium)
            }
            Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onChange(value - 1) }, enabled = enabled) { Icon(painterResource(R.drawable.outline_remove_black_24), contentDescription = null) }
                AppSlider(
                    value = value.toFloat(), onValueChange = { onChange(it.roundToInt()) },
                    valueRange = MIN.toFloat()..MAX.toFloat(), enabled = enabled, modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { onChange(value + 1) }, enabled = enabled) { Icon(painterResource(R.drawable.outline_add_black_24), contentDescription = null) }
            }
        }
    }

    /** The real floating zoom button, shown above the sheet so it can be dragged and tried. */
    private fun addPreview() {
        val box = object : FrameLayout(context) {
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                setPadding(0, 0, 0, sheetHeight)
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }
        try {
            val webFltView = WebFltView(context, 4)
            flt = webFltView
            webFltView.setPreview(true)
            webFltView.refreshColors()
            if (ZtriSettings.notiZoom.value) webFltView.setNoti(true)
            if (!zoomIcon) webFltView.setVisibility(View.GONE)
            webFltView.setFltListener(object : FltViewListener {
                override fun onTap(view: View?, button: Int) {
                    val p = page ?: return
                    if (ZtriSettings.notiZoom.value) {
                        ZtriSettings.edit(context) { set(ZtriSettings.notiZoom, false) }
                        webFltView.setNoti(false)
                    }
                    val current = p.getSettings().textZoom
                    if (current != zoomSize) setPageZoom(zoomSize) else if (current != textSize) setPageZoom(textSize)
                }

                override fun onGone() {}
                override fun onLongPress(view: View?) {}
            })
            webFltView.setTouchListener(object : FltTouchListener {
                override fun onPressed(down: Boolean) {}
            })
            box.addView(webFltView, FrameLayout.LayoutParams(MainApp.dp48, MainApp.dp48))
        } catch (unused: Exception) {
        }
        overlay = box
        sampleFrame = box
        addContentView(box, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    private fun reset() {
        if (page == null) return
        if (zoomIcon) chooseSwitch(false)
        zoomSize = 200
        textSize = 100
        pageZoomAtOpen = textSize
        page?.getSettings()?.setTextZoom(textSize)
        colourAlpha = MainConst.BUTTON_ALPHA
        colourValue = MainConst.gradientColors[5]
        colourPos = MainConst.presetPositions[5]
        if (colourDiffers(colourAlpha, colourValue, colourPos)) {
            storeColour(colourAlpha, colourValue, colourPos)
            colourTick++
            flt?.refreshColors()
        }
        save(false)
    }

    /** Stores ([persist]) or forgets the stored values (reset); persisting also closes the dialog. */
    private fun save(persist: Boolean) {
        var changed: Boolean
        if (ZtriSettings.zoomIcon.value == zoomIcon && ZtriSettings.zoomSize.value == zoomSize) {
            changed = false
        } else {
            ZtriSettings.editLocal { set(ZtriSettings.zoomIcon, zoomIcon) }
            ZtriSettings.editLocal { set(ZtriSettings.zoomSize, zoomSize) }
            val core = PrefZtri.get(context)
            if (persist) {
                core!!.putBool("mZoomIcon", ZtriSettings.zoomIcon.value)
                core.putInt(ZtriSettings.zoomSize.value, "mZoomSize")
            } else {
                core!!.removeKey("mZoomIcon")
                core.removeKey("mZoomSize")
            }
            core.saveAsync()
            changed = true
        }
        if (ZoneSettings.textSize.value != textSize) {
            ZoneSettings.editLocal { set(ZoneSettings.textSize, textSize) }
            if (persist) PrefSet.putInt(context, 15, textSize, "mTextSize") else PrefSet.remove(context, 15, "mTextSize")
            changed = true
        }
        if (colourDiffers(colourAlpha, colourValue, colourPos)) {
            colourAlpha = EditorSettings.zoomAlpha.value
            colourValue = EditorSettings.zoomColor.value
            colourPos = EditorSettings.zoomPos.value
            changed = true
        }
        if (changed) listener?.onChanged()
        pageChanged = false
        if (persist) dismiss()
    }

    override fun dismiss() {
        if (pageChanged) {
            pageChanged = false
            page?.getSettings()?.setTextZoom(pageZoomAtOpen)
        }
        if (colourDiffers(colourAlpha, colourValue, colourPos)) storeColour(colourAlpha, colourValue, colourPos)
        colourDialog?.dismiss()
        colourDialog = null
        flt?.release()
        flt = null
        overlay?.let { (it.parent as? ViewGroup)?.removeView(it) }
        overlay = null
        sampleFrame = null
        listener = null
        page = null
        super.dismiss()
    }

    companion object {
        private const val MIN = 50
        private const val MAX = 500
    }
}
