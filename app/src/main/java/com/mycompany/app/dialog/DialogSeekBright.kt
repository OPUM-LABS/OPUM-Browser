package com.mycompany.app.dialog

import com.mycompany.app.ui.components.AppSlider
import android.content.Context
import com.mycompany.app.pref.store.PdfSettings
import android.view.Window
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefCore
import com.mycompany.app.pref.store.ImageSettings
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.store.VideoSettings
import com.mycompany.app.ui.browser.ComposeBottomDialog
import kotlin.math.roundToInt

/**
 * Screen brightness for the video player ([mode] = 1), the image viewer (2) and the PDF/reader pages (0): "System" or
 * "Custom" and, for custom, a 0..100 % slider that changes the window's brightness live. Apply stores it (and tells the
 * video player the brightness to use through [DialogSeekListener]); Reset (after asking) goes back to system / 90 %;
 * closing without Apply puts the window back to the stored brightness. No scrim, so the page behind shows the real
 * brightness. Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogSeekBright(
    mainActivity: MainActivity?,
    window: Window?,
    mode: Int,
    dialogSeekListener: DialogSeekListener?,
) : ComposeBottomDialog(mainActivity) {
    private var listener: DialogSeekListener? = dialogSeekListener
    private val appContext: Context = context
    private val target: Window? = window
    private val mode = mode

    // the values stored when the dialog opened / last applied (closing without Apply goes back to them)
    private var storedUser: Boolean
    private var storedValue: Int
    private var user by mutableStateOf(false)
    private var value by mutableIntStateOf(0)
    private var systemBrightness = -123456
    private var resetAsk by mutableStateOf(false)
    private var typeMenu by mutableStateOf(false)

    override val scrimAlpha: Float get() = 0f

    init {
        when (mode) {
            1 -> { storedUser = VideoSettings.userBrightness.value; storedValue = VideoSettings.brightness.value }
            2 -> { storedUser = ImageSettings.userBrightness.value; storedValue = ImageSettings.brightness.value }
            else -> { storedUser = PdfSettings.userBrightness.value; storedValue = PdfSettings.brightness.value }
        }
        user = storedUser
        value = storedValue
        setSheet {
            Box(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.fillMaxWidth().clickable { typeMenu = true }.padding(horizontal = 24.dp, vertical = 16.dp),
                ) {
                    Text(stringResource(R.string.type), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(if (user) R.string.user_defined else R.string.system_name),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        stringResource(if (user) R.string.bright_info else R.string.screen_info_system),
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                com.mycompany.app.ui.components.BubbleMenu(expanded = typeMenu, onDismiss = { typeMenu = false }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.system_name)) }, onClick = { typeMenu = false; chooseType(false) })
                    DropdownMenuItem(text = { Text(stringResource(R.string.user_defined)) }, onClick = { typeMenu = false; chooseType(true) })
                }
            }
            HorizontalDivider()
            Row(
                Modifier.fillMaxWidth().padding(top = 16.dp, start = 24.dp, end = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(R.string.brightness), style = MaterialTheme.typography.titleMedium)
                Text("$value%", style = MaterialTheme.typography.titleMedium)
            }
            Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { chooseValue(value - 1) }, enabled = user) { Icon(painterResource(R.drawable.outline_remove_black_24), contentDescription = null) }
                AppSlider(
                    value = value.toFloat(), onValueChange = { chooseValue(it.roundToInt()) },
                    valueRange = 0f..100f, enabled = user, modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { chooseValue(value + 1) }, enabled = user) { Icon(painterResource(R.drawable.outline_add_black_24), contentDescription = null) }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { resetAsk = true }) { Text(stringResource(R.string.reset)) }
                TextButton(onClick = { apply(true) }) { Text(stringResource(R.string.apply)) }
            }
            if (resetAsk) {
                AlertDialog(
                    onDismissRequest = { resetAsk = false },
                    text = { Text(stringResource(R.string.reset_setting)) },
                    confirmButton = {
                        TextButton(colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = androidx.compose.material3.MaterialTheme.colorScheme.error), onClick = {
                            resetAsk = false
                            user = false
                            value = 90
                            MainUtil.applyWindowBrightness(target, value, user)
                            apply(false)
                        }) { Text(stringResource(R.string.reset)) }
                    },
                    dismissButton = { TextButton(onClick = { resetAsk = false }) { Text(stringResource(R.string.cancel)) } },
                )
            }
        }
        show()
    }

    private fun chooseType(newUser: Boolean) {
        if (user == newUser) return
        user = newUser
        MainUtil.applyWindowBrightness(target, value, user)
    }

    private fun chooseValue(newValue: Int) {
        val v = newValue.coerceIn(0, 100)
        if (!user || v == value) return
        value = v
        MainUtil.applyWindowBrightness(target, v, user)
    }

    /** Stores the values ([persist]) or forgets the stored ones (reset); persisting also closes the dialog. */
    private fun apply(persist: Boolean) {
        val core: PrefCore?
        var userKey = "mUserBright3"
        var valueKey = "mBright3"
        when (mode) {
            1 -> {
                val vs = VideoSettings
                if (vs.userBrightness.value != user || vs.brightness.value != value) {
                    if (persist) {
                        vs.edit(appContext) { set(vs.userBrightness, user); set(vs.brightness, value) }
                    } else {
                        vs.editLocal { set(vs.userBrightness, user); set(vs.brightness, value) }
                        vs.remove(appContext, listOf(vs.userBrightness, vs.brightness))
                    }
                    val shown = if (vs.userBrightness.value) vs.brightness.value else {
                        if (systemBrightness == -123456) systemBrightness = MainUtil.screenBrightnessPercent(appContext)
                        systemBrightness
                    }
                    listener?.onValue(shown)
                }
            }
            2 -> {
                val img = ImageSettings
                if (img.userBrightness.value != user || img.brightness.value != value) {
                    if (persist) {
                        img.edit(appContext) { set(img.userBrightness, user); set(img.brightness, value) }
                    } else {
                        img.editLocal { set(img.userBrightness, user); set(img.brightness, value) }
                        img.remove(appContext, listOf(img.userBrightness, img.brightness))
                    }
                }
            }
            else -> {
                if (PdfSettings.userBrightness.value != user || PdfSettings.brightness.value != value) {
                    PdfSettings.editLocal { set(PdfSettings.userBrightness, user) }
                    PdfSettings.editLocal { set(PdfSettings.brightness, value) }
                    userKey = "mUserBright"
                    valueKey = "mBright"
                    core = PrefPdf.get(appContext, false)
                    store(core, persist, userKey, valueKey)
                    if (!persist) listener?.onValue(0)
                }
            }
        }
        storedUser = user
        storedValue = value
        if (persist) dismiss()
    }

    private fun store(core: PrefCore?, persist: Boolean, userKey: String, valueKey: String) {
        if (core == null) return
        if (persist) {
            core.putBool(userKey, user)
            core.putInt(value, valueKey)
        } else {
            core.removeKey(userKey)
            core.removeKey(valueKey)
        }
        core.saveAsync()
    }

    override fun dismiss() {
        if (user != storedUser || value != storedValue) {
            user = storedUser
            value = storedValue
            MainUtil.applyWindowBrightness(target, value, user)
        }
        listener = null
        super.dismiss()
    }

    companion object {
    }
}
