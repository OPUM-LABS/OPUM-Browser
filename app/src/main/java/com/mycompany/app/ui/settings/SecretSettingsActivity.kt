package com.mycompany.app.ui.settings

import android.content.Context
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.SecretSettings
import android.content.Intent
import androidx.compose.material.icons.filled.Refresh
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
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.ConfirmDialog
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
        ) {
            // incognito keeps nothing since 2026-11-24 (its keep options are gone): what happens is said here
            settingsFooter(getString(R.string.secret_temp_info))
            settingsHeader(getString(R.string.hd_incognito_data))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.notification),
                    checked = noti.value,
                    onCheckedChange = noti::set,
                )
            }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    val notiBefore = SecretSettings.secretNoti.value
                    resetToDefaults(context)
                    reloadAll(noti to (if (MainUtil.hasPermission(context, 5)) false else SecretSettings.secretNoti.value))
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
            if (!SecretSettings.secretNoti.value) SecretSettings.reset(context, listOf(SecretSettings.secretNoti))
        }
    }
}
