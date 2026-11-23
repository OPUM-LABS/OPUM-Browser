package com.mycompany.app.dialog

import android.content.Context
import com.mycompany.app.pref.store.ImageSettings
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefImage
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.ui.browser.SheetChoiceRow
import com.mycompany.app.ui.browser.SheetNote
import com.mycompany.app.ui.browser.SheetSwitchRow

/**
 * Image viewer layout (image viewer > settings) for the current orientation (landscape or portrait, its own set of
 * values): view type ([MainConst.viewModeNames]), size ([MainConst.imageSizeNames] with the explanation [MainConst.imageSizeInfos]), page split, and the
 * margin (0–50 dp, only for types other than the first). Apply stores both sets (`mViewLand` … `mMarginPort`), Reset
 * (asks first) restores the defaults; closing reports to [ChangedListener.onChanged]. Compose now (on [ComposeBottomDialog]);
 * same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogSetImage(mainActivity: android.app.Activity?, changedListener: ChangedListener?) : ComposeBottomDialog(mainActivity) {
    @JvmField var hostActivity: android.app.Activity? = mainActivity
    @JvmField var appContext: Context? = context
    @JvmField var changeCallback: ChangedListener? = changedListener
    /** Landscape. */
    @JvmField var landscape: Boolean = (mainActivity as? com.mycompany.app.main.MainActivity)?.isLandscapeScreen() ?: MainUtil.isLandscape(context)
    @JvmField var msgDialog: DialogSetMsg? = null

    // landscape set and portrait set, as before
    private var viewLandSel by mutableIntStateOf(ImageSettings.viewLand.value)
    private var fitLandSel by mutableStateOf(ImageSettings.fitLand.value)
    private var splitLandSel by mutableStateOf(ImageSettings.splitLand.value)
    private var marginLandSel by mutableIntStateOf(ImageSettings.marginLand.value)
    private var viewPortSel by mutableIntStateOf(ImageSettings.viewPort.value)
    private var fitPortSel by mutableStateOf(ImageSettings.fitPort.value)
    private var splitPortSel by mutableStateOf(ImageSettings.splitPort.value)
    private var marginPortSel by mutableIntStateOf(ImageSettings.marginPort.value)

    init {
        setSheet {
            val land = landscape
            Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(if (land) AppR.drawable.outline_stay_current_landscape_black_24 else AppR.drawable.outline_stay_current_portrait_black_24), null)
                Text(
                    stringResource(if (land) AppR.string.view_land else AppR.string.view_port),
                    style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp),
                )
            }
            HorizontalDivider()
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                val type = if (land) viewLandSel else viewPortSel
                SheetChoiceRow(stringResource(AppR.string.type), MainConst.viewModeNames.toList(), type, { k ->
                    val v = k % MainConst.viewModeNames.size
                    if (land) viewLandSel = v else viewPortSel = v
                })
                val fit = if (land) fitLandSel else fitPortSel
                val r1 = if (fit) 1 else 0
                SheetChoiceRow(stringResource(AppR.string.size), MainConst.imageSizeNames.toList(), r1, { k ->
                    val z = k % MainConst.imageSizeNames.size == 1
                    if (land) fitLandSel = z else fitPortSel = z
                })
                SheetNote(stringResource(MainConst.imageSizeInfos[r1]))
                SheetSwitchRow(
                    stringResource(AppR.string.page_split), if (land) splitLandSel else splitPortSel, { if (land) splitLandSel = it else splitPortSel = it },
                    summary = stringResource(AppR.string.split_info),
                )
                val marginOn = type != 0
                val px = if (land) marginLandSel else marginPortSel
                val dp = Math.round(MainUtil.pxToDp(appContext, px.toFloat())).coerceIn(0, 50)
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp).alpha(if (marginOn) 1f else 0.38f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(AppR.string.margin), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text("$dp", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = dp.toFloat(), valueRange = 0f..50f, enabled = marginOn,
                        onValueChange = { v ->
                            val value = Math.round(MainUtil.dpToPx(appContext, Math.round(v).toFloat()))
                            if (land) marginLandSel = value else marginPortSel = value
                        },
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { askReset() }) { Text(stringResource(AppR.string.reset)) }
                TextButton(onClick = { save(true) }) { Text(stringResource(AppR.string.apply)) }
            }
        }
        show()
    }

    /** Reset. */
    private fun askReset() {
        if (hostActivity == null || msgDialog != null) return
        val msg = DialogSetMsg(hostActivity, AppR.string.reset_setting, AppR.string.reset, object : DialogApplyListener {
            override fun onApply() {
                msgDialog?.dismiss()
                msgDialog = null
                viewLandSel = 3; fitLandSel = false; splitLandSel = false; marginLandSel = -1
                viewPortSel = 0; fitPortSel = false; splitPortSel = false; marginPortSel = -1
                save(false)
            }
        })
        msgDialog = msg
        msg.setOnDismissListener {
            msgDialog?.dismiss()
            msgDialog = null
        }
    }

    interface ChangedListener {
        fun onChanged()
    }

    fun save(close: Boolean) {
        with(ImageSettings) {
            if (viewLand.value != viewLandSel || fitLand.value != fitLandSel || splitLand.value != splitLandSel || marginLand.value != marginLandSel) {
                edit(appContext) { set(viewLand, viewLandSel); set(fitLand, fitLandSel); set(splitLand, splitLandSel); set(marginLand, marginLandSel) }
            }
            if (viewPort.value != viewPortSel || fitPort.value != fitPortSel || splitPort.value != splitPortSel || marginPort.value != marginPortSel) {
                edit(appContext) { set(viewPort, viewPortSel); set(fitPort, fitPortSel); set(splitPort, splitPortSel); set(marginPort, marginPortSel) }
            }
        }
        if (close) dismiss()
    }

    override fun dismiss() {
        if (appContext == null) {
            super.dismiss()
            return
        }
        changeCallback?.onChanged()
        changeCallback = null
        msgDialog?.dismiss()
        msgDialog = null
        hostActivity = null
        appContext = null
        super.dismiss()
    }
}
