package com.mycompany.app.dialog

import android.content.Context
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.EditorSettings
import android.os.Handler
import android.os.Looper
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.dialog.DialogEditorText.EditorSetListener
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.ui.browser.SheetChoiceRow

/**
 * Screen filter (browser menu > Screen filter): the filter mode ([MainConst.filterValues] / [MainConst.filterNames]: not used, always,
 * dark mode only) and the filter colour (opens [DialogEditIcon] in colour mode, live preview); every change is shown
 * at once on the page ([DialogSeekListener.onValue] with the filter colour) and on this sheet (no scrim, the sheet itself is
 * tinted like the page). Reset (asks first) restores the default colour; Apply stores the mode (`mScrFilUse`);
 * closing without Apply keeps the colour but restores the stored mode. The prefs logic (`filterColorNow`–`saveFilter`, dismiss) is the old
 * code. Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogSetScrFil(mainActivity: MainActivity?, dialogSeekListener: DialogSeekListener?) : ComposeBottomDialog(mainActivity) {
    @JvmField val mainHandler: Handler? = Handler(Looper.getMainLooper())
    @JvmField var hostActivity: MainActivity? = mainActivity
    @JvmField var appContext: Context? = context
    @JvmField var seekListener: DialogSeekListener? = dialogSeekListener
    @JvmField var iconDialog: DialogEditIcon? = null
    @JvmField var msgDialog: DialogSetMsg? = null
    @JvmField var filterAlpha: Int = EditorSettings.scrFilAlpha.value
    @JvmField var filterColor: Int = EditorSettings.scrFilColor.value
    @JvmField var filterPos: Float = EditorSettings.scrFilPos.value

    /** Filter mode, the shown colour and the sheet tint. */
    private var filterMode by mutableIntStateOf(EditorSettings.scrFilUse.value)
    private var swatch by mutableIntStateOf(MainUtil.alphaColor(EditorSettings.scrFilColor.value, EditorSettings.scrFilAlpha.value))
    private var tint by mutableIntStateOf(0)

    override val scrimAlpha: Float get() = 0f

    init {
        tint = filterColorNow()
        setSheet {
            Column(Modifier.fillMaxWidth().drawWithContent { drawContent(); if (tint != 0) drawRect(Color(tint)) }) {
                val modes = MainConst.filterValues
                SheetChoiceRow(
                    stringResource(AppR.string.screen_filter),
                    modes.map { MainConst.filterNames[it] },
                    modes.indexOf(filterMode),
                    { pickMode(modes[it % modes.size]) },
                    value = stringResource(MainConst.filterNames[filterMode]),
                )
                Row(
                    Modifier.fillMaxWidth().clickable { editColor() }.padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(AppR.string.filter_color), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                    Box(
                        Modifier.size(32.dp).border(1.dp, MaterialTheme.colorScheme.outline, CircleShape).padding(2.dp)
                            .background(Color(swatch), CircleShape)
                    )
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { askReset() }) { Text(stringResource(AppR.string.reset)) }
                    TextButton(onClick = { applyUse(true) }) { Text(stringResource(AppR.string.apply)) }
                }
            }
        }
        show()
    }

    /** Shows the current filter on the page and on the sheet. */
    private fun preview() {
        val color = filterColorNow()
        tint = color
        seekListener?.onValue(color)
    }

    /** Mode picked. */
    private fun pickMode(newMode: Int) {
        if (filterMode == newMode) return
        filterMode = newMode
        preview()
    }

    /** Colour row. */
    private fun editColor() {
        if (hostActivity == null || iconDialog != null || msgDialog != null) return
        val editor = DialogEditIcon(hostActivity, 5, object : EditorSetListener {
            override fun onApply(color: Int, label: String?) { swatch = color }
        })
        iconDialog = editor
        editor.seekListener = object : DialogSeekListener {
            override fun onValue(newValue: Int) { seekListener?.onValue(newValue) }
        }
        editor.setOnDismissListener {
            preview()
            iconDialog?.dismiss()
            iconDialog = null
        }
        mainHandler?.post { if (iconDialog != null) seekListener?.onValue(PrefEditor.scrFilArgb) }
    }

    /** Reset. */
    private fun askReset() {
        if (hostActivity == null || iconDialog != null || msgDialog != null) return
        val msg = DialogSetMsg(hostActivity, AppR.string.reset_setting, AppR.string.reset, object : DialogApplyListener {
            override fun onApply() {
                msgDialog?.dismiss()
                msgDialog = null
                var changed = false
                if (filterMode != 0) {
                    filterMode = 0
                    changed = true
                }
                filterAlpha = 60
                filterColor = MainConst.gradientColors[7]
                filterPos = MainConst.presetPositions[7]
                if (isChanged()) {
                    saveFilter()
                    changed = true
                }
                if (changed) {
                    swatch = MainUtil.alphaColor(EditorSettings.scrFilColor.value, EditorSettings.scrFilAlpha.value)
                    preview()
                }
                applyUse(false)
            }
        })
        msgDialog = msg
        msg.setOnDismissListener {
            msgDialog?.dismiss()
            msgDialog = null
        }
    }

    fun filterColorNow(): Int {
        val filterModeNow = filterMode
        if (filterModeNow == 1) return PrefEditor.scrFilArgb
        if (filterModeNow == 2 && AppState.darkUi && AppState.darkWeb) return PrefEditor.scrFilArgb
        return 0
    }

    fun isChanged(): Boolean {
        return !(EditorSettings.scrFilAlpha.value == filterAlpha && EditorSettings.scrFilColor.value == filterColor && EditorSettings.scrFilPos.value.compareTo(filterPos) == 0)
    }

    fun applyUse(enabled: Boolean) {
        val filterModeNow = filterMode
        if (EditorSettings.scrFilUse.value != filterModeNow) {
            EditorSettings.editLocal { set(EditorSettings.scrFilUse, filterModeNow) }
            if (enabled) PrefSet.putInt(appContext, 1, filterModeNow, "mScrFilUse") else PrefSet.remove(appContext, 1, "mScrFilUse")
        }
        if (isChanged()) {
            filterAlpha = EditorSettings.scrFilAlpha.value
            filterColor = EditorSettings.scrFilColor.value
            filterPos = EditorSettings.scrFilPos.value
        }
        if (enabled) dismiss()
    }

    fun saveFilter() {
        EditorSettings.editLocal { set(EditorSettings.scrFilAlpha, filterAlpha) }
        EditorSettings.editLocal { set(EditorSettings.scrFilColor, filterColor) }
        EditorSettings.editLocal { set(EditorSettings.scrFilPos, filterPos) }
        PrefEditor.scrFilArgb = MainUtil.alphaColor(filterColor, filterAlpha)
        val prefEditorS = PrefEditor.get(appContext)
        prefEditorS!!.putInt(EditorSettings.scrFilAlpha.value, "mScrFilAlpha")
        prefEditorS.putInt(EditorSettings.scrFilColor.value, "mScrFilColor")
        prefEditorS.putFloat("mScrFilPos", EditorSettings.scrFilPos.value)
        prefEditorS.saveAsync()
    }

    override fun dismiss() {
        if (appContext == null) {
            super.dismiss()
            return
        }
        var changed = false
        if (filterMode != EditorSettings.scrFilUse.value) {
            filterMode = EditorSettings.scrFilUse.value
            changed = true
        }
        if (isChanged()) {
            saveFilter()
            changed = true
        }
        if (changed) seekListener?.onValue(filterColorNow())
        iconDialog?.dismiss()
        iconDialog = null
        msgDialog?.dismiss()
        msgDialog = null
        hostActivity = null
        appContext = null
        seekListener = null
        super.dismiss()
    }
}
