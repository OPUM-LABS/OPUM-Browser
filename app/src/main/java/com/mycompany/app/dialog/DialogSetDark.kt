package com.mycompany.app.dialog

import android.app.Activity
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.BrowserSettings
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.dialog.DialogConfirm.DialogConfBtnListener
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.ui.browser.SheetChoiceRow
import com.mycompany.app.ui.browser.SheetLinkRow
import com.mycompany.app.ui.browser.SheetSwitchRow
import com.mycompany.app.ui.settings.DisplaySettingsActivity

/**
 * Dark mode (browser menu > Dark mode): quick light / dark buttons (apply and close), a help button when the WebView
 * may need an update (`AppState.webViewHint`, [DialogConfirm] with a link to the WebView's store page), UI theme and web page
 * theme (light / dark, off while "follow the system" is on), follow the system, dark images and their brightness
 * ([DialogSeekSimple], off while dark images are off). Apply stores them (`mThemeSys`, `mThemeUi`, `mThemeWeb`,
 * `mThemeImg`, `mImgBright`) and closes; Reset (asks first) applies the defaults at once and keeps the sheet open.
 * The prefs logic ([save]) is the old code. Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogSetDark(mainActivity: MainActivity?, setDarkListener: SetDarkListener?) : ComposeBottomDialog(mainActivity) {
    @JvmField val mainHandler: Handler? = Handler(Looper.getMainLooper())
    @JvmField var hostActivity: MainActivity? = mainActivity
    @JvmField var appContext: Context? = context
    @JvmField var darkListener: SetDarkListener? = setDarkListener
    @JvmField var confirmDialog: DialogConfirm? = null
    @JvmField var seekDialog: DialogSeekSimple? = null
    @JvmField var msgDialog: DialogSetMsg? = null
    @JvmField var quickBusy: Boolean = false

    private var followSystem by mutableStateOf(BrowserSettings.themeSys.value)
    private var uiTheme by mutableIntStateOf(if (BrowserSettings.themeUi.value == 2) 0 else BrowserSettings.themeUi.value)
    private var webTheme by mutableIntStateOf(if (BrowserSettings.themeWeb.value == 2) 0 else BrowserSettings.themeWeb.value)
    private var darkImage by mutableStateOf(BrowserSettings.themeImg.value)
    private var imageBright by mutableIntStateOf(BrowserSettings.imgBright.value)

    init {
        if (BrowserSettings.themeUi.value == 2) BrowserSettings.editLocal { set(BrowserSettings.themeUi, 0) }
        if (BrowserSettings.themeWeb.value == 2) BrowserSettings.editLocal { set(BrowserSettings.themeWeb, 0) }
        setSheet {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                if (AppState.webViewHint.toInt() == 1) {
                    IconButton(onClick = { help() }) { Icon(painterResource(AppR.drawable.outline_help_black_20), null) }
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { quick(0) }) { Icon(painterResource(AppR.drawable.outline_sunny_black_20), null) }
                IconButton(onClick = { quick(1) }) { Icon(painterResource(AppR.drawable.outline_dark_mode_black_20), null) }
            }
            HorizontalDivider()
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                val values = DisplaySettingsActivity.themeValues
                val labels = DisplaySettingsActivity.themeLabels
                SheetChoiceRow(
                    "UI", listOf(labels[values[0]], labels[values[1]]), values.indexOf(uiTheme), { uiTheme = values[it % 2] },
                    enabled = !followSystem, value = stringResource(labels[uiTheme]),
                )
                SheetChoiceRow(
                    stringResource(AppR.string.web_page), listOf(labels[values[0]], labels[values[1]]), values.indexOf(webTheme),
                    { webTheme = values[it % 2] }, enabled = !followSystem, value = stringResource(labels[webTheme]),
                )
                SheetSwitchRow(stringResource(AppR.string.screen_info_system), followSystem, { followSystem = it })
                HorizontalDivider(Modifier.padding(horizontal = 24.dp))
                SheetSwitchRow(stringResource(AppR.string.dark_image), darkImage, { darkImage = it }, summary = stringResource(AppR.string.img_brt_info))
                SheetLinkRow(stringResource(AppR.string.img_bright), percentText(imageBright), { brightness() }, enabled = darkImage)
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { askReset() }) { Text(stringResource(AppR.string.reset)) }
                TextButton(onClick = { if (!quickBusy) { quickBusy = true; mainHandler?.post { save(true); quickBusy = false } } }) { Text(stringResource(AppR.string.apply)) }
            }
        }
        show()
    }

    /** Sun / moon: light or dark for both, then apply and close. */
    private fun quick(theme: Int) {
        if (quickBusy) return
        quickBusy = true
        mainHandler?.post {
            followSystem = false
            uiTheme = theme
            webTheme = theme
            save(true)
            quickBusy = false
        }
    }

    /** Help. */
    private fun help() {
        if (hostActivity == null || confirmDialog != null || seekDialog != null || msgDialog != null) return
        closeConfirm()
        val confirm = DialogConfirm(hostActivity as Activity?, false, object : DialogConfBtnListener {
            override fun onConfirm(confirmed: Boolean) { closeConfirm() }
            override fun onButton() {
                closeConfirm()
                MainUtil.openPlayStore(hostActivity, "com.google.android.webview")
            }
            override fun onCancel() { closeConfirm() }
        })
        confirmDialog = confirm
        confirm.setOnDismissListener { closeConfirm() }
    }

    /** Image brightness . */
    private fun brightness() {
        if (hostActivity == null || confirmDialog != null || seekDialog != null || msgDialog != null) return
        val seek = DialogSeekSimple(hostActivity, 3, imageBright, object : DialogSeekListener {
            override fun onValue(newValue: Int) { if (imageBright != newValue) imageBright = newValue }
        })
        seekDialog = seek
        seek.setOnDismissListener {
            seekDialog?.dismiss()
            seekDialog = null
        }
    }

    /** Reset. */
    private fun askReset() {
        if (hostActivity == null || confirmDialog != null || seekDialog != null || msgDialog != null) return
        val msg = DialogSetMsg(hostActivity, AppR.string.reset_setting, AppR.string.reset, object : DialogApplyListener {
            override fun onApply() {
                msgDialog?.dismiss()
                msgDialog = null
                followSystem = false
                uiTheme = 0
                webTheme = 0
                darkImage = true
                imageBright = 70
                save(false)
            }
        })
        msgDialog = msg
        msg.setOnDismissListener {
            msgDialog?.dismiss()
            msgDialog = null
        }
    }

    interface SetDarkListener {
        fun onChanged(changed: Boolean)
    }

    fun closeConfirm() {
        confirmDialog?.dismiss()
        confirmDialog = null
    }

    /** Applies the edited theme settings; [notifyChange] = closing the dialog. */
    fun save(notifyChange: Boolean) {
        var changed = false
        var notify = false
        if (BrowserSettings.themeSys.value != followSystem || BrowserSettings.themeUi.value != uiTheme || BrowserSettings.themeWeb.value != webTheme || BrowserSettings.themeImg.value != darkImage || BrowserSettings.imgBright.value != imageBright) {
            BrowserSettings.editLocal { set(BrowserSettings.themeSys, followSystem) }
            BrowserSettings.editLocal { set(BrowserSettings.themeUi, uiTheme) }
            BrowserSettings.editLocal { set(BrowserSettings.themeWeb, webTheme) }
            BrowserSettings.editLocal { set(BrowserSettings.themeImg, darkImage) }
            BrowserSettings.editLocal { set(BrowserSettings.imgBright, imageBright) }
            val pref = PrefWeb.get(appContext, false)
            pref!!.putBool("mThemeSys", BrowserSettings.themeSys.value)
            pref.putInt(BrowserSettings.themeUi.value, "mThemeUi")
            pref.putInt(BrowserSettings.themeWeb.value, "mThemeWeb")
            pref.putBool("mThemeImg", BrowserSettings.themeImg.value)
            pref.putInt(BrowserSettings.imgBright.value, "mImgBright")
            pref.saveAsync()
            changed = true
            if (notifyChange) notify = true else MainUtil.applyNightMode()
        }
        if (notifyChange) {
            if (changed) darkListener?.onChanged(notify)
            dismiss()
            return
        }
        if (changed) {
            // redraw in the new theme and tell the browser
            mainHandler?.post {
                refreshTheme()
                darkListener?.onChanged(false)
            }
        }
    }

    override fun dismiss() {
        if (appContext == null) {
            super.dismiss()
            return
        }
        closeConfirm()
        seekDialog?.dismiss()
        seekDialog = null
        msgDialog?.dismiss()
        msgDialog = null
        hostActivity = null
        appContext = null
        darkListener = null
        super.dismiss()
    }

    companion object {
        @JvmStatic
        fun percentText(percent: Int): String = percent.toString() + "%"
    }
}
