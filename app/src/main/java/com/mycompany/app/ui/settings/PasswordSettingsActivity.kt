package com.mycompany.app.ui.settings

import androidx.compose.material3.Text
import androidx.core.net.toUri
import androidx.compose.ui.Modifier

import android.content.Context
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.SecretSettings
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.components.SettingSliderRow
import androidx.compose.runtime.LaunchedEffect
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
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Password manager settings: save passwords, login lock, button (its colour as circles and its
 * transparency, the colour dialog before 2026-11-26), the password list, lock type and the reset of the password lock. The reset dialog is the legacy
 * [DialogLockReset] (takes a plain Activity).
 */
class PasswordSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val doNotSave = rememberPref({ SecretSettings.savePass.value }) { SecretSettings.edit(context) { set(SecretSettings.savePass, it) } }
        val button = rememberPref({ ZtriSettings.passIcon.value }) { ZtriSettings.edit(context) { set(ZtriSettings.passIcon, it) } }
        var neverSites by remember { mutableStateOf(com.mycompany.app.dialog.PassNever.all()) }
        var showingNever by remember { mutableStateOf(false) }
        val fillBar = rememberPref({ ZtriSettings.passBar.value }) { ZtriSettings.edit(context) { set(ZtriSettings.passBar, it) } }
        // the passwords are guarded by the app lock or a lock of their own (SecretSettings.guardType, 2026-11-27)
        var loginLock by remember { mutableStateOf(SecretSettings.loginLock.value && SecretSettings.guardType(1) != 0) }
        var lockType by remember { mutableIntStateOf(SecretSettings.passLockType.value) }
        var ownLock by remember { mutableStateOf(SecretSettings.ownLock(1)) }
        val color = rememberPref({ EditorSettings.passColor.value }) { c ->
            val at = MainConst.gradientColors.indexOf(c)
            EditorSettings.edit(context) {
                set(EditorSettings.passColor, c)
                if (at >= 0) set(EditorSettings.passPos, MainConst.presetPositions[at])
            }
            PrefEditor.passArgb = PrefEditor.withAlpha(EditorSettings.passColor.value, EditorSettings.passAlpha.value)
        }
        val alpha = rememberPref({ EditorSettings.passAlpha.value.coerceIn(0, 90) }) {
            EditorSettings.edit(context) { set(EditorSettings.passAlpha, it) }
            PrefEditor.passArgb = PrefEditor.withAlpha(EditorSettings.passColor.value, EditorSettings.passAlpha.value)
        }
        var liveAlpha by remember { mutableIntStateOf(alpha.value) }
        var choosingLock by remember { mutableStateOf(false) }
        var lockGuide by remember { mutableIntStateOf(0) } // 1 login lock needs a lock type, 2 list needs a lock type
        var resetting by remember { mutableStateOf(false) }

        fun refresh() {
            lockType = SecretSettings.passLockType.value
            ownLock = SecretSettings.ownLock(1)
            loginLock = SecretSettings.loginLock.value && SecretSettings.guardType(1) != 0
            color.reload(EditorSettings.passColor.value); alpha.reload(EditorSettings.passAlpha.value); liveAlpha = alpha.value
        }

        OnScreenResume { refresh() }

        val lockResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) refresh()
        }

        // OPUM's own manager, or another app through Android's autofill (PassManager)
        val owner = rememberPref({ ZtriSettings.passOwner.value }) { ZtriSettings.edit(context) { set(ZtriSettings.passOwner, it) } }
        val builtIn = owner.value != com.mycompany.app.web.PassManager.OTHER_APP
        val enabled = builtIn && doNotSave.value // the options below only apply while saving passwords is on
        SettingsScaffold(
            title = stringResource(R.string.password),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.pass_manager),
                    options = listOf(stringResource(R.string.pass_manager_opum), stringResource(R.string.pass_manager_other)),
                    selectedIndex = if (builtIn) 0 else 1,
                    onSelect = { owner.set(if (it == 0) com.mycompany.app.web.PassManager.OPUM else com.mycompany.app.web.PassManager.OTHER_APP) },
                )
            }
            if (!builtIn) {
                // Bitwarden, 1Password … are chosen in Android's settings; OPUM's manager is off
                item {
                    SettingRow(
                        title = stringResource(R.string.pass_choose_app),
                        summary = stringResource(R.string.pass_choose_app_sum),
                        onClick = { openAutofillSettings() },
                    )
                }
                settingsFooter(getString(R.string.pass_other_info))
            }
            if (builtIn) item {
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
                            if (on && SecretSettings.guardType(1) == 0) {
                                lockGuide = 1
                            } else {
                                SecretSettings.edit(context) { set(SecretSettings.loginLock, on) }
                                loginLock = on && SecretSettings.guardType(1) != 0
                            }
                        },
                    )
                }
            }
            if (builtIn) settingsFooter(
                getString(R.string.not_support_site) + "\n" + getString(R.string.password_info) +
                    if (enabled) "\n" + getString(R.string.login_lock_info) else ""
            )
            if (enabled) {
                // filling: only when the user picks a login (bar or button), never on its own
                settingsHeader(getString(R.string.hd_fill_login))
                item {
                    SettingSwitchRow(
                        title = stringResource(R.string.pass_bar),
                        summary = stringResource(R.string.pass_bar_sum),
                        checked = fillBar.value,
                        onCheckedChange = fillBar::set,
                    )
                }
                settingsHeader(getString(R.string.hd_screen_button))
                item {
                    SettingSwitchRow(
                        title = stringResource(R.string.password_button),
                        checked = button.value,
                        onCheckedChange = button::set,
                    )
                }
                item {
                    ButtonColorCircles(
                        title = stringResource(R.string.icon_color), enabled = button.value, reason = stringResource(R.string.btn_off_reason),
                        selected = color.value, transparency = liveAlpha, onPick = color::set,
                    )
                }
                item {
                    SettingSliderRow(
                        title = stringResource(R.string.color_alpha), value = alpha.value, range = 0..90, label = { "$it%" },
                        onValueChange = { alpha.set(it); liveAlpha = it }, enabled = button.value,
                        below = { v -> LaunchedEffect(v) { liveAlpha = v } },
                    )
                }
                settingsFooter(getString(R.string.drag_move_guide))
            }
            settingsHeader(getString(R.string.hd_saved_passwords))
            item {
                SettingRow(
                    title = stringResource(R.string.list),
                    onClick = {
                        if (SecretSettings.guardType(1) == 0) lockGuide = 2
                        else startActivity(Intent(context, PassListSettingsActivity::class.java))
                    },
                )
            }
            // sites the user said never to save logins for, with a way to take them off again
            if (builtIn) item {
                SettingRow(
                    title = stringResource(R.string.pass_never_list),
                    summary = neverSites.size.toString(),
                    onClick = { showingNever = true },
                )
            }
            item { LockWithRow(ownLock) { own -> setOwnLock(context, 1, own); refresh() } }
            if (ownLock) {
                item {
                    SettingChoiceRow(
                        title = stringResource(R.string.lock_type),
                        value = stringResource(MainConst.lockNames[lockType]),
                        onClick = { choosingLock = true },
                    )
                }
            } else {
                item { AppLockRow() }
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
        if (showingNever) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showingNever = false },
                title = { Text(stringResource(R.string.pass_never_list)) },
                text = {
                    if (neverSites.isEmpty()) {
                        Text(stringResource(R.string.pass_never_empty))
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn {
                            items(neverSites.size) { i ->
                                val host = neverSites[i]
                                androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                    Text(host, modifier = Modifier.weight(1f))
                                    androidx.compose.material3.IconButton(onClick = {
                                        com.mycompany.app.dialog.PassNever.remove(context, host)
                                        neverSites = com.mycompany.app.dialog.PassNever.all()
                                    }) { androidx.compose.material3.Icon(com.mycompany.app.ui.components.Sym.Close, stringResource(R.string.delete)) }
                                }
                            }
                        }
                    }
                },
                confirmButton = { androidx.compose.material3.TextButton(onClick = { showingNever = false }) { Text(stringResource(R.string.ok)) } },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    doNotSave.reload(SecretSettings.savePass.value)
                    button.reload(ZtriSettings.passIcon.value); fillBar.reload(ZtriSettings.passBar.value); owner.reload(ZtriSettings.passOwner.value)
                    refresh()
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /**
     * Android's page where the autofill app is chosen: "Passwords, passkeys & accounts" (Android 14 and later, it
     * takes a package address), the accounts page that holds it before, or the settings.
     */
    private fun openAutofillSettings() {
        val intents = listOf(
            android.content.Intent("android.settings.CREDENTIAL_PROVIDER", "package:$packageName".toUri()),
            android.content.Intent(android.provider.Settings.ACTION_SYNC_SETTINGS),
            android.content.Intent(android.provider.Settings.ACTION_SETTINGS),
        )
        for (intent in intents) {
            try { startActivity(intent); return } catch (_: Exception) {}
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
            if (!ZtriSettings.passBar.value) ZtriSettings.reset(context, listOf(ZtriSettings.passBar))
            if (ZtriSettings.passOwner.value != 0) ZtriSettings.reset(context, listOf(ZtriSettings.passOwner))
            if (EditorSettings.passAlpha.value == MainConst.BUTTON_ALPHA && EditorSettings.passColor.value == color && EditorSettings.passPos.value.compareTo(pos) == 0) return
            EditorSettings.editLocal { set(EditorSettings.passAlpha, MainConst.BUTTON_ALPHA) }
            EditorSettings.editLocal { set(EditorSettings.passColor, color) }
            EditorSettings.editLocal { set(EditorSettings.passPos, pos) }
            PrefEditor.passArgb = PrefEditor.withAlpha(color, MainConst.BUTTON_ALPHA)
            val prefs = PrefEditor.get(context)
            prefs!!.removeKey("mPassAlpha"); prefs.removeKey("mPassColor"); prefs.removeKey("mPassPos"); prefs.save()
        }
    }
}
