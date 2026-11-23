package com.mycompany.app.ui.settings

import android.content.Context
import com.mycompany.app.pref.store.SecretSettings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import com.mycompany.app.dialog.DialogSetLock
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Security settings: Do Not Track / Global Privacy Control / keyboard protection, the app lock (type, what is locked,
 * what the lock resets) and the screenshot policy. [DialogSetLock] takes a plain Activity and is shown directly.
 */
class SecureSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val dnt = rememberPref({ SecretSettings.secureDnt.value }) { SecretSettings.edit(context) { set(SecretSettings.secureDnt, it) } }
        val gpc = rememberPref({ SecretSettings.secureGpc.value }) { SecretSettings.edit(context) { set(SecretSettings.secureGpc, it) } }
        val keyboard = rememberPref({ SecretSettings.secureKey.value }) { SecretSettings.edit(context) { set(SecretSettings.secureKey, it) } }
        val lockSecretOnly = rememberPref({ SecretSettings.lockSecret.value }) { SecretSettings.edit(context) { set(SecretSettings.lockSecret, it) } }
        val shotType = rememberPref({ SecretSettings.shotType.value }) { SecretSettings.edit(context) { set(SecretSettings.shotType, it) } }
        val shotSecretOnly = rememberPref({ SecretSettings.shotSecret.value }) { SecretSettings.edit(context) { set(SecretSettings.shotSecret, it) } }
        var lockType by remember { mutableIntStateOf(SecretSettings.lockType.value) }
        var tick by remember { mutableIntStateOf(0) }
        var choosing by remember { mutableIntStateOf(0) } // 1 DNT, 2 GPC, 4 keyboard, 6 lock type, 10 screenshot
        var resetting by remember { mutableStateOf(false) }

        OnScreenResume { lockType = SecretSettings.lockType.value; lockSecretOnly.reload(SecretSettings.lockSecret.value); tick++ }

        val lockResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) { lockType = SecretSettings.lockType.value; tick++ }
        }
        // the password page has its own lock (as it had when it was opened from the settings start page)
        val passwordUnlock = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) startActivity(android.content.Intent(context, PasswordSettingsActivity::class.java))
        }
        val lockEnabled = lockType != 0
        val shotEnabled = shotType.value != 0

        SettingsScaffold(
            title = stringResource(R.string.set_security_pass),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsDivider()
            item {
                SettingRow(
                    title = stringResource(R.string.password),
                    onClick = {
                        val passLock = SecretSettings.passLockType.value
                        if (passLock == 0) {
                            startActivity(android.content.Intent(context, PasswordSettingsActivity::class.java))
                        } else {
                            val lock = MainUtil.lockIntent(context, passLock)
                            lock.putExtra("EXTRA_PASS", 1)
                            lock.putExtra("EXTRA_TYPE", 2)
                            passwordUnlock.launch(lock)
                        }
                    },
                )
            }
            settingsHeader(getString(R.string.hd_tracking))
            item {
                SettingChoiceRow(title = "Do Not Track", value = stringResource(MainConst.secretUseNames[dnt.value]), onClick = { choosing = 1 })
            }
            item {
                SettingChoiceRow(title = "Global Privacy Control", value = stringResource(MainConst.secretUseNames[gpc.value]), onClick = { choosing = 2 })
            }
            settingsHeader(getString(R.string.hd_keyboard))
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.keyboard_secure),
                    value = stringResource(MainConst.secretUseNames[keyboard.value]),
                    onClick = { choosing = 4 },
                )
            }
            settingsFooter(getString(R.string.keyboard_secure_info_1) + "\n" + getString(R.string.keyboard_secure_info_2))
            settingsDivider()
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.app_lock),
                    value = stringResource(MainConst.lockNames[lockType]),
                    onClick = { choosing = 6 },
                )
            }
            // only with a lock set
            if (lockEnabled) {
                item {
                    SettingSwitchRow(
                        title = stringResource(R.string.only_secret),
                        checked = lockSecretOnly.value,
                        onCheckedChange = { lockSecretOnly.set(it); tick++ },
                    )
                }
                item {
                    SettingRow(
                        title = stringResource(R.string.lock_reset_target),
                        summary = "$tick".let { lockResetSummary(context) },
                        onClick = {
                            val before = SecretSettings.lockReset.value
                            val dialog = DialogSetLock(this@SecureSettingsActivity)
                            dialog.setOnDismissListener { if (before != SecretSettings.lockReset.value) tick++ }
                        },
                    )
                }
                "$tick".let { lockResetInfo(context) }?.let { settingsFooter(it) }
            }
            settingsHeader(getString(R.string.hd_screenshots))
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.android_shot),
                    value = stringResource(MainConst.shotNames[shotType.value]),
                    onClick = { choosing = 10 },
                )
            }
            if (shotEnabled) {
                item {
                    SettingSwitchRow(
                        title = stringResource(R.string.only_secret),
                        checked = shotSecretOnly.value,
                        onCheckedChange = shotSecretOnly::set,
                    )
                }
            }
        }

        when (choosing) {
            1, 2, 4 -> {
                val state = when (choosing) { 1 -> dnt; 2 -> gpc; else -> keyboard }
                ChoiceDialog(
                    title = when (choosing) {
                        1 -> "Do Not Track"
                        2 -> "Global Privacy Control"
                        else -> stringResource(R.string.keyboard_secure)
                    },
                    options = MainConst.secretUseValues.map { stringResource(MainConst.secretUseNames[it]) },
                    selectedIndex = MainConst.secretUseValues.indexOf(state.value),
                    onSelect = { state.set(MainConst.secretUseValues[it]) },
                    onDismiss = { choosing = 0 },
                )
            }
            6 -> ChoiceDialog(
                title = stringResource(R.string.app_lock),
                options = MainConst.lockValues.map { stringResource(MainConst.lockNames[it]) },
                selectedIndex = MainConst.lockValues.indexOf(lockType),
                onSelect = { index ->
                    val type = MainConst.lockValues[index]
                    if (type != 4 && type != 0) {
                        // password / pattern / PIN are set up on their own screen
                        lockResult.launch(MainUtil.lockIntent(context, type).putExtra("EXTRA_TYPE", 1))
                    } else if (SecretSettings.lockType.value != type && (type != 4 || MainUtil.canUseBiometric(context, true))) {
                        SecretSettings.editLocal { set(SecretSettings.lockType, type) }
                        SecretSettings.editLocal { set(SecretSettings.lockCode, "") }
                        PrefSecret.saveLock(context)
                        lockType = type
                        tick++
                    }
                },
                onDismiss = { choosing = 0 },
            )
            10 -> ChoiceDialog(
                title = stringResource(R.string.android_shot),
                options = MainConst.shotValues.map { stringResource(MainConst.shotNames[it]) },
                selectedIndex = MainConst.shotValues.indexOf(shotType.value),
                onSelect = { shotType.set(MainConst.shotValues[it]) },
                onDismiss = { choosing = 0 },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    dnt.reload(SecretSettings.secureDnt.value); gpc.reload(SecretSettings.secureGpc.value); keyboard.reload(SecretSettings.secureKey.value)
                    shotType.reload(SecretSettings.shotType.value); shotSecretOnly.reload(SecretSettings.shotSecret.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /** What the lock covers, as a comma separated list (null when nothing). */
    private fun lockResetSummary(context: Context): String? {
        val flags = SecretSettings.lockReset.value
        if (flags == 0) return null
        if (flags == 126) {
            return listOf(R.string.quick_access, R.string.bookmark, R.string.down_list, R.string.history, R.string.tab_item)
                .joinToString(", ") { context.getString(it) }
        }
        val labels = listOf(
            2 to R.string.quick_access, 4 to R.string.bookmark, 8 to R.string.down_list,
            16 to R.string.recent_search, 32 to R.string.history, 64 to R.string.tab_item,
        )
        val parts = labels.filter { (flags and it.first) == it.first }.map { context.getString(it.second) }
        return if (parts.isEmpty()) null else parts.joinToString(", ")
    }

    companion object {
        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (SecretSettings.secureDnt.value != 0) SecretSettings.reset(context, listOf(SecretSettings.secureDnt))
            if (SecretSettings.secureGpc.value != 0) SecretSettings.reset(context, listOf(SecretSettings.secureGpc))
            if (SecretSettings.secureKey.value != 2) SecretSettings.reset(context, listOf(SecretSettings.secureKey))
            if (!SecretSettings.shotType.isDefault) SecretSettings.reset(context, listOf(SecretSettings.shotType))
            if (!SecretSettings.shotSecret.isDefault) SecretSettings.reset(context, listOf(SecretSettings.shotSecret))
        }

        /** Explanation under "what the lock resets"; also used by the settings search index. */
        @JvmStatic
        fun lockResetInfo(context: Context?): String? {
            if (context == null) return null
            val info = context.getString(R.string.lock_reset_info)
            if (!SecretSettings.lockSecret.value) return info
            return info + "\n" + context.getString(R.string.lock_secret_guide)
        }
    }
}
