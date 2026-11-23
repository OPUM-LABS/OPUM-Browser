package com.mycompany.app.ui.settings

import android.content.Context
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.SecretSettings
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogEditIcon
import com.mycompany.app.dialog.DialogLockReset
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.InfoDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Password manager settings: save passwords, login lock, button, icon colour, the password list, lock type and the
 * reset of the password lock. Icon colour editor via `DialogShow.direct`; the reset dialog is the legacy
 * [DialogLockReset] (takes a plain Activity).
 */
class PasswordSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val doNotSave = rememberPref({ SecretSettings.savePass.value }) { SecretSettings.edit(context) { set(SecretSettings.savePass, it) } }
        val button = rememberPref({ ZtriSettings.passIcon.value }) { ZtriSettings.edit(context) { set(ZtriSettings.passIcon, it) } }
        var loginLock by remember { mutableStateOf(SecretSettings.loginLock.value && SecretSettings.passLockType.value != 0) }
        var lockType by remember { mutableIntStateOf(SecretSettings.passLockType.value) }
        var iconColor by remember { mutableIntStateOf(PrefEditor.withAlpha(EditorSettings.passColor.value, EditorSettings.passAlpha.value)) }
        var choosingLock by remember { mutableStateOf(false) }
        var lockGuide by remember { mutableIntStateOf(0) } // 1 login lock needs a lock type, 2 list needs a lock type
        var resetting by remember { mutableStateOf(false) }

        fun refresh() {
            lockType = SecretSettings.passLockType.value
            loginLock = SecretSettings.loginLock.value && SecretSettings.passLockType.value != 0
            iconColor = PrefEditor.withAlpha(EditorSettings.passColor.value, EditorSettings.passAlpha.value)
        }

        OnScreenResume { refresh() }

        val lockResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) refresh()
        }

        val enabled = doNotSave.value // the options below only apply while saving passwords is on
        SettingsScaffold(
            title = stringResource(R.string.password),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.password_save),
                    checked = doNotSave.value,
                    onCheckedChange = doNotSave::set,
                )
            }
            // login lock and the button only while passwords are saved
            if (enabled) {
                item {
                    SettingSwitchRow(
                        title = stringResource(R.string.login_lock),
                        checked = loginLock,
                        onCheckedChange = { on ->
                            if (on && SecretSettings.passLockType.value == 0) {
                                lockGuide = 1
                            } else {
                                SecretSettings.edit(context) { set(SecretSettings.loginLock, on) }
                                loginLock = on && SecretSettings.passLockType.value != 0
                            }
                        },
                    )
                }
            }
            settingsFooter(
                getString(R.string.not_support_site) + "\n" + getString(R.string.password_info) +
                    if (enabled) "\n" + getString(R.string.login_lock_info) else ""
            )
            if (enabled) {
                settingsHeader(getString(R.string.hd_screen_button))
                item {
                    SettingSwitchRow(
                        title = stringResource(R.string.password_button),
                        checked = button.value,
                        onCheckedChange = button::set,
                    )
                }
                if (button.value) {
                item {
                    SettingRow(
                        title = stringResource(R.string.icon_color),
                        onClick = { com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host -> DialogEditIcon(host, 9, null) } },
                        trailing = {
                            Box(
                                Modifier
                                    .size(28.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(Color(iconColor))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                            )
                        },
                    )
                }
                }
                settingsFooter(getString(R.string.drag_move_guide))
            }
            settingsHeader(getString(R.string.hd_saved_passwords))
            item {
                SettingRow(
                    title = stringResource(R.string.list),
                    onClick = {
                        if (SecretSettings.passLockType.value == 0) lockGuide = 2
                        else startActivity(Intent(context, PassListSettingsActivity::class.java))
                    },
                )
            }
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.lock_type),
                    value = stringResource(MainConst.lockNames[lockType]),
                    onClick = { choosingLock = true },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.reset),
                    onClick = {
                        DialogLockReset(this@PasswordSettingsActivity, 1, object : DialogApplyListener {
                            override fun onApply() { refresh(); doNotSave.reload(SecretSettings.savePass.value); button.reload(ZtriSettings.passIcon.value) }
                        })
                    },
                )
            }
            settingsFooter(getString(R.string.password_reset_guide))
        }

        if (lockGuide != 0) {
            InfoDialog(
                title = stringResource(R.string.password),
                message = stringResource(R.string.password_lock_1) +
                    if (lockGuide == 2) "\n" + stringResource(R.string.password_lock_2) else "",
                onDismiss = { lockGuide = 0 },
            )
        }
        if (choosingLock) {
            ChoiceDialog(
                title = stringResource(R.string.lock_type),
                options = MainConst.lockValues.map { stringResource(MainConst.lockNames[it]) },
                selectedIndex = MainConst.lockValues.indexOf(lockType),
                onSelect = { index ->
                    val type = MainConst.lockValues[index]
                    if (type != 4 && type != 0) {
                        // password / pattern / PIN are set up on their own screen
                        lockResult.launch(
                            MainUtil.lockIntent(context, type).putExtra("EXTRA_PASS", 1).putExtra("EXTRA_TYPE", 1)
                        )
                    } else if (SecretSettings.passLockType.value != type && (type != 4 || MainUtil.canUseBiometric(context, true))) {
                        SecretSettings.editLocal { set(SecretSettings.passLockType, type) }
                        SecretSettings.editLocal { set(SecretSettings.passLockCode, "") }
                        PrefSecret.savePassLock(context)
                        if (type == 0 && SecretSettings.loginLock.value) {
                            SecretSettings.edit(context) { set(SecretSettings.loginLock, false) }
                        }
                        refresh()
                    }
                },
                onDismiss = { choosingLock = false },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    doNotSave.reload(SecretSettings.savePass.value)
                    button.reload(ZtriSettings.passIcon.value)
                    refresh()
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            val color = MainConst.gradientColors[5]
            val pos = MainConst.presetPositions[5]
            if (!SecretSettings.savePass.isDefault) SecretSettings.reset(context, listOf(SecretSettings.savePass))
            if (!ZtriSettings.passIcon.value) ZtriSettings.reset(context, listOf(ZtriSettings.passIcon))
            if (EditorSettings.passAlpha.value == 0 && EditorSettings.passColor.value == color && EditorSettings.passPos.value.compareTo(pos) == 0) return
            EditorSettings.editLocal { set(EditorSettings.passAlpha, 0) }
            EditorSettings.editLocal { set(EditorSettings.passColor, color) }
            EditorSettings.editLocal { set(EditorSettings.passPos, pos) }
            PrefEditor.passArgb = PrefEditor.withAlpha(color, 0)
            val prefs = PrefEditor.get(context)
            prefs!!.removeKey("mPassAlpha"); prefs.removeKey("mPassColor"); prefs.removeKey("mPassPos"); prefs.save()
        }
    }
}
