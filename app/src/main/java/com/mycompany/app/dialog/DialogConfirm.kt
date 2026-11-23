package com.mycompany.app.dialog

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.main.MainActivity
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * The confirm / notice sheet used for the copyright notice (screen capture and site images), the dark-mode notice
 * (with "Check version"), storage notices (with Settings), filter and free-text warnings. Content as before, chosen by
 * the constructor: optional "Tip" header ([showIcon]), message ([messageRes] / [messageText]) and second message ([subText]), a title ([titleRes]) with
 * indented points ([point1Res] / [point1Text], [point2Res], [point3Res], [point4Res]), a centred note ([noteRes]), an extra button ([checkRes] → [DialogConfBtnListener.onButton]), a
 * "Do not show this message again" checkbox ([okFirst]) that must be ticked before OK works (as before), then OK ([okRes] → [DialogConfListener.onResult] or [DialogConfBtnListener.onConfirm]; the caller closes)
 * with an optional second button ([settingRes] → [DialogConfBtnListener.onCancel], else close). Compose now (on [ComposeBottomDialog]); same
 * constructors and listeners, shows itself.
 */
@Suppress("deprecation")
class DialogConfirm : ComposeBottomDialog {
    @JvmField val mainHandler: Handler? = Handler(Looper.getMainLooper())
    @JvmField var appContext: Context?
    @JvmField var confListener: DialogConfListener? = null
    @JvmField var btnListener: DialogConfBtnListener? = null
    @JvmField var showIcon: Boolean = false
    @JvmField var messageRes: Int = 0
    @JvmField var messageText: String? = null
    @JvmField var subText: String? = null
    @JvmField var titleRes: Int = 0
    @JvmField var point1Res: Int = 0
    @JvmField var point1Text: String? = null
    @JvmField var point2Res: Int = 0
    @JvmField var point3Res: Int = 0
    @JvmField var point4Res: Int = 0
    @JvmField var noteRes: Int = 0
    @JvmField var checkRes: Int = 0
    @JvmField var okFirst: Boolean = false
    @JvmField var okRes: Int
    @JvmField var settingRes: Int = 0

    private var checked by mutableStateOf(false)

    interface DialogConfBtnListener {
        fun onConfirm(confirmed: Boolean)

        fun onButton()

        fun onCancel()
    }

    interface DialogConfListener {
        fun onResult(confirmed: Boolean)
    }

    /** Copyright notice. */
    constructor(mainActivity: MainActivity?, dialogConfListener: DialogConfListener?) : super(mainActivity) {
        this.appContext = context
        this.confListener = dialogConfListener
        this.titleRes = AppR.string.right_title
        this.noteRes = AppR.string.important_copyright
        this.okFirst = true
        this.okRes = AppR.string.ok
        this.point1Text = context.getString(AppR.string.guide_right_1) + " " + context.getString(AppR.string.guide_right_2) + " " +
            context.getString(AppR.string.guide_right_3)
        mainHandler?.post { build() }
    }

    /** Tip with a free text, with or without the confirm checkbox. */
    constructor(activity: Activity?, str: String?, z: Boolean, dialogConfListener: DialogConfListener?) : super(activity) {
        this.appContext = context
        this.confListener = dialogConfListener
        this.showIcon = true
        this.messageText = str
        this.okFirst = z
        this.okRes = AppR.string.ok
        mainHandler?.post { build() }
    }

    /** Tip with a title and one point, with the confirm checkbox. */
    constructor(activity: Activity?, i: Int, i2: Int, dialogConfListener: DialogConfListener?) : super(activity) {
        this.appContext = context
        this.confListener = dialogConfListener
        this.showIcon = true
        this.titleRes = i
        this.point1Res = i2
        this.okFirst = true
        this.okRes = AppR.string.ok
        mainHandler?.post { build() }
    }

    /** Tip with one message. */
    constructor(activity: Activity?, i: Int, dialogConfListener: DialogConfListener?) : super(activity) {
        this.appContext = context
        this.confListener = dialogConfListener
        this.showIcon = true
        this.messageRes = i
        this.okFirst = false
        this.okRes = AppR.string.ok
        mainHandler?.post { build() }
    }

    /** Dark-mode notice with "Check version". */
    constructor(activity: Activity?, z: Boolean, dialogConfBtnListener: DialogConfBtnListener?) : super(activity) {
        this.appContext = context
        this.btnListener = dialogConfBtnListener
        this.showIcon = true
        this.messageRes = AppR.string.dark_guide_1
        this.checkRes = AppR.string.check_ver
        this.okFirst = z
        this.okRes = AppR.string.ok
        mainHandler?.post { build() }
    }

    /** Two messages, or (no [str]) the storage notice with Settings. */
    constructor(activity: Activity?, str: String?, str2: String?, dialogConfBtnListener: DialogConfBtnListener?) : super(activity) {
        this.appContext = context
        this.btnListener = dialogConfBtnListener
        this.showIcon = true
        this.okRes = AppR.string.ok
        if (!TextUtils.isEmpty(str)) {
            this.messageText = str
            this.subText = str2
        } else {
            this.titleRes = AppR.string.storage
            this.point1Res = AppR.string.storage_guide_1
            this.point2Res = AppR.string.storage_guide_2
            this.point3Res = AppR.string.storage_guide_3
            this.point4Res = AppR.string.storage_guide_4
            this.settingRes = AppR.string.setting
        }
        mainHandler?.post { build() }
    }

    /** Builds and shows the sheet (old `build()`: built the views, then showed). */
    fun build() {
        if (appContext == null) return
        val message = if (messageRes != 0) context.getString(messageRes) else messageText
        val point1 = if (point1Res != 0) context.getString(point1Res) else point1Text
        setSheet {
            if (showIcon) {
                Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(androidx.compose.material.icons.Icons.Outlined.Info, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(stringResource(AppR.string.tip), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp))
                }
            }
            Column(
                Modifier.fillMaxWidth().heightIn(min = 72.dp, max = 460.dp).verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                if (!message.isNullOrEmpty()) Text(message, style = MaterialTheme.typography.bodyMedium)
                if (!subText.isNullOrEmpty()) Text(subText!!, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 18.dp))
                if (titleRes != 0) Text(stringResource(titleRes), style = MaterialTheme.typography.titleMedium)
                for (point in listOf(point1, point2Res.takeIf { it != 0 }?.let { stringResource(it) }, point3Res.takeIf { it != 0 }?.let { stringResource(it) }, point4Res.takeIf { it != 0 }?.let { stringResource(it) })) {
                    if (!point.isNullOrEmpty()) {
                        Text(point, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 16.dp, top = 8.dp))
                    }
                }
                if (noteRes != 0) {
                    Text(
                        stringResource(noteRes), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    )
                }
                if (checkRes != 0) {
                    FilledTonalButton(onClick = { btnListener?.onButton() }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                        Text(stringResource(checkRes))
                    }
                }
            }
            if (okFirst) {
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                Row(
                    Modifier.fillMaxWidth().clickable { checked = !checked }.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked, { checked = it })
                    Text(stringResource(AppR.string.message_confirm), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(start = 8.dp))
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                if (settingRes != 0) {
                    TextButton(onClick = { val l = btnListener; if (l != null) l.onCancel() else dismiss() }) { Text(stringResource(settingRes)) }
                }
                if (okRes != 0) {
                    TextButton(onClick = { ok() }, enabled = !okFirst || checked) { Text(stringResource(okRes)) }
                }
            }
        }
        show()
    }

    /** OK: the listener gets whether the box was ticked; it closes the sheet itself. */
    private fun ok() {
        val confirmed = okFirst && checked
        val listener = confListener
        if (listener != null) {
            listener.onResult(confirmed)
            return
        }
        btnListener?.onConfirm(confirmed)
    }

    override fun dismiss() {
        appContext = null
        confListener = null
        btnListener = null
        messageText = null
        subText = null
        point1Text = null
        super.dismiss()
    }
}
