package com.mycompany.app.ui.settings

import android.content.Context
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.SecretSettings
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookDown
import com.mycompany.app.db.book.DbBookHistory
import com.mycompany.app.db.book.DbBookQuick
import com.mycompany.app.db.book.DbBookTab
import com.mycompany.app.db.book.DbBookWeb
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.InfoDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.PrefState
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Secret (incognito) mode: lock type, what is kept, notification, and "reset secret data". The notification switch
 * is off while the system notification permission is missing; see [syncNotification].
 */
class SecretSettingsActivity : OpumComposeActivity() {
    private var waitingForNotificationSettings = false

    /** The sync service shows or hides the secret notification according to the current setting. */
    private fun syncNotification(context: Context, enabled: Boolean) {
        if (!SyncSettings.secretMode.value) return
        if (enabled) MainUtil.lockAppData(context) else MainUtil.clearAppData(context)
    }

    @Composable
    override fun Content() {
        val context: Context = this
        val hist = rememberPref({ SecretSettings.secretHist.value > 0 }) {
            if (it) {
                val now = System.currentTimeMillis()
                SecretSettings.edit(context) { set(SecretSettings.secretHist, now) }
            } else {
                SecretSettings.edit(context) { set(SecretSettings.secretHist, 0L) }
                DbBookHistory.clearSecret(context, true)
            }
        }
        val down = rememberPref({ SecretSettings.secretDown.value > 0 }) {
            if (it) {
                val now = System.currentTimeMillis()
                SecretSettings.edit(context) { set(SecretSettings.secretDown, now) }
            } else {
                SecretSettings.edit(context) { set(SecretSettings.secretDown, 0L) }
                DbBookDown.clearSecretDone(context)
            }
        }
        val keepTab = rememberPref({ SecretSettings.keepTab.value }) { SecretSettings.edit(context) { set(SecretSettings.keepTab, it) } }
        val keepLogin = rememberPref({ SecretSettings.keepLogin.value }) { SecretSettings.edit(context) { set(SecretSettings.keepLogin, it) } }
        val noti = rememberPref({ if (MainUtil.hasPermission(context, 5)) false else SecretSettings.secretNoti.value }) { on ->
            SecretSettings.edit(context) { set(SecretSettings.secretNoti, on) }
            var effective = SecretSettings.secretNoti.value
            if (effective && MainUtil.hasPermission(context, 5)) {
                // notifications are blocked for the app: send the user to the system settings
                waitingForNotificationSettings = openNotificationSettings(context)
                if (!waitingForNotificationSettings) effective = false
            }
            if (waitingForNotificationSettings.not()) syncNotification(context, effective)
        }
        var showInfo by remember { mutableStateOf(false) }
        var confirmReset by remember { mutableStateOf(false) }
        var resetting by remember { mutableStateOf(false) }

        OnScreenResume {
            if (waitingForNotificationSettings) {
                waitingForNotificationSettings = false
                val blocked = MainUtil.hasPermission(context, 5)
                noti.reload(if (blocked) false else SecretSettings.secretNoti.value)
                if (!blocked) syncNotification(context, SecretSettings.secretNoti.value)
            }
        }

        fun reloadAll(vararg states: Pair<PrefState<Boolean>, Boolean>) = states.forEach { (s, newValue) -> s.reload(newValue) }

        SettingsScaffold(
            title = stringResource(R.string.secret_mode),
            onBack = { finish() },
            onReset = { resetting = true },
            actions = {
                if (!MainConst.isApi28) {
                    IconButton(onClick = { showInfo = true }) { Icon(Icons.Filled.Info, contentDescription = null) }
                }
            },
        ) {
            item {
                SettingRow(
                    title = stringResource(R.string.lock_type),
                    onClick = {
                        startActivity(
                            Intent(context, com.mycompany.app.ui.settings.SecureSettingsActivity::class.java)
                                .putExtra("EXTRA_NOTI", true).putExtra("EXTRA_INDEX", 6)
                        )
                    },
                )
            }
            settingsHeader(getString(R.string.history))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.secret_hist),
                    checked = hist.value,
                    onCheckedChange = hist::set,
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.secret_down),
                    checked = down.value,
                    onCheckedChange = down::set,
                )
            }
            settingsFooter(getString(R.string.secret_hist_info) + "\n" + getString(R.string.secret_down_info))
            settingsHeader(getString(R.string.hd_to_normal))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.keep_tab),
                    checked = keepTab.value,
                    onCheckedChange = keepTab::set,
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.keep_login),
                    checked = keepLogin.value,
                    onCheckedChange = keepLogin::set,
                )
            }
            settingsFooter(getString(R.string.keep_tab_info) + "\n" + getString(R.string.keep_login_info))
            settingsHeader(getString(R.string.hd_incognito_data))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.notification),
                    checked = noti.value,
                    onCheckedChange = noti::set,
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.reset),
                    onClick = { confirmReset = true },
                )
            }
            settingsFooter(getString(R.string.secret_reset_guide))
        }

        if (showInfo) {
            InfoDialog(
                title = stringResource(R.string.secret_mode),
                message = stringResource(R.string.secret_data_info),
                onDismiss = { showInfo = false },
            )
        }
        if (confirmReset) {
            ConfirmDialog(
                message = stringResource(R.string.secret_reset_guide),
                onConfirm = {
                    // wipes the data of secret mode: history, downloads, secret tabs, secret bookmarks and shortcuts
                    Thread {
                        resetSecretData(context)
                        runOnUiThread {
                            MainUtil.showToast(context, R.string.reset_noti)
                            reloadAll(
                                hist to (SecretSettings.secretHist.value > 0), down to (SecretSettings.secretDown.value > 0),
                                keepTab to SecretSettings.keepTab.value, keepLogin to SecretSettings.keepLogin.value, noti to SecretSettings.secretNoti.value,
                            )
                            syncNotification(context, SecretSettings.secretNoti.value)
                        }
                    }.start()
                },
                onDismiss = { confirmReset = false },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    val notiBefore = SecretSettings.secretNoti.value
                    resetToDefaults(context)
                    reloadAll(
                        hist to (SecretSettings.secretHist.value > 0), down to (SecretSettings.secretDown.value > 0),
                        keepTab to SecretSettings.keepTab.value, keepLogin to SecretSettings.keepLogin.value,
                        noti to (if (MainUtil.hasPermission(context, 5)) false else SecretSettings.secretNoti.value),
                    )
                    if (notiBefore != SecretSettings.secretNoti.value) {
                        syncNotification(context, SecretSettings.secretNoti.value && !MainUtil.hasPermission(context, 5))
                    }
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /** Opens the system notification settings of the app. Returns true when the screen could be opened. */
    private fun openNotificationSettings(context: Context): Boolean = try {
        startActivity(
            Intent("android.settings.APP_NOTIFICATION_SETTINGS")
                .putExtra("android.provider.extra.APP_PACKAGE", context.packageName)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        true
    } catch (_: Exception) {
        false
    }

    companion object {
        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (SecretSettings.secretHist.value != 0L) {
                SecretSettings.editLocal { set(SecretSettings.secretHist, 0L) }; PrefSet.removeNow(context, 9, "mSecretHist"); DbBookHistory.clearSecret(context, true)
            }
            if (SecretSettings.secretDown.value == 0L) { SecretSettings.editLocal { set(SecretSettings.secretDown, System.currentTimeMillis()) }; PrefSet.removeNow(context, 9, "mSecretDown") }
            if (!SecretSettings.keepTab.value) SecretSettings.reset(context, listOf(SecretSettings.keepTab))
            if (!SecretSettings.keepLogin.isDefault) SecretSettings.reset(context, listOf(SecretSettings.keepLogin))
            if (!SecretSettings.secretNoti.value) SecretSettings.reset(context, listOf(SecretSettings.secretNoti))
        }

        /** The "reset secret data" action (blocking; run it off the main thread). */
        private fun resetSecretData(context: Context) {
            val hist = SecretSettings.secretHist.value
            val down = SecretSettings.secretDown.value
            val keepTab = SecretSettings.keepTab.value
            val keepLogin = SecretSettings.keepLogin.value
            val noti = SecretSettings.secretNoti.value
            SecretSettings.editLocal { set(SecretSettings.secretHist, 0L) }
            val newDown = if (down == 0L) System.currentTimeMillis().also { SecretSettings.editLocal { set(SecretSettings.secretDown, it) } } else down
            SecretSettings.editLocal { set(SecretSettings.keepTab, true) }
            SecretSettings.editLocal { set(SecretSettings.keepLogin, false) } // default since 2026-11-23
            SecretSettings.editLocal { set(SecretSettings.secretNoti, true) }
            if (hist != SecretSettings.secretHist.value || down != newDown || !keepTab || keepLogin || !noti) {
                val prefs = PrefSecret.get(context, false)
                prefs!!.removeKey("mSecretHist"); prefs.removeKey("mSecretDown"); prefs.removeKey("mKeepTab")
                prefs.putBool("mKeepLogin", false); prefs.removeKey("mSecretNoti"); prefs.save() // written: an old install would read the old default
            }
            DbUtil.deleteWhere(DbBookQuick.get(context).getWritableDatabase(), "DbBookQuick_table", "_secret=?", arrayOf<String?>("1"))
            DbUtil.deleteWhere(DbBookWeb.get(context).getWritableDatabase(), "DbBookWeb_table", "_secret=?", arrayOf<String?>("1"))
            DbBookDown.clearSecretDone(context)
            DbBookHistory.clearSecret(context, true)
            DbBookTab.clearSecret(context, true)
        }
    }
}
