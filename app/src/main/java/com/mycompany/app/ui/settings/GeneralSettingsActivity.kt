package com.mycompany.app.ui.settings

import android.content.Context
import com.mycompany.app.main.RequestCode
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.MainSettings
import android.content.Intent
import android.os.Build
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
import com.mycompany.app.dialog.DialogEditShort
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefTts
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
import com.mycompany.app.ui.prefs.rememberPref
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * General settings: default browser, notifications, language, app keyboard, double back, PDF shortcut.
 * The PDF shortcut dialog is the legacy [DialogEditShort], shown through `DialogShow.direct`.
 */
class GeneralSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val keypad = rememberPref({ TtsSettings.appKeypad.value }) { TtsSettings.edit(context) { set(TtsSettings.appKeypad, it) } }
        val doubleBack = rememberPref({ MainSettings.doubleBack.value }) { MainSettings.edit(context) { set(MainSettings.doubleBack, it) } }
        val tipsOn = rememberPref({ com.mycompany.app.pref.store.ReadSettings.tipsOn.value }) {
            com.mycompany.app.pref.store.ReadSettings.edit(context) { set(com.mycompany.app.pref.store.ReadSettings.tipsOn, it) }
        }
        var locale by remember { mutableIntStateOf(currentLocaleIndex()) }
        var choosingLocale by remember { mutableStateOf(false) }
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.general),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.set_group_system))
            item {
                SettingRow(
                    title = stringResource(R.string.default_browser),
                    onClick = {
                        try { startActivity(Intent("android.settings.MANAGE_DEFAULT_APPS_SETTINGS")) } catch (_: Exception) {}
                    },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.notification),
                    onClick = {
                        try {
                            startActivity(
                                Intent("android.settings.APP_NOTIFICATION_SETTINGS")
                                    .putExtra("android.provider.extra.APP_PACKAGE", packageName)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        } catch (_: Exception) {}
                    },
                )
            }
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.locale),
                    value = localeName(locale) +
                        if (locale == 0) "\n" + stringResource(R.string.screen_info_system) else "",
                    onClick = { choosingLocale = true },
                )
            }
            settingsHeader(getString(R.string.hd_start_exit))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.app_keyboard),
                    checked = keypad.value,
                    onCheckedChange = keypad::set,
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.double_back),
                    checked = doubleBack.value,
                    onCheckedChange = doubleBack::set,
                )
            }
            // tips while browsing (ui/tips)
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.tips_show),
                    summary = stringResource(R.string.tips_show_sum),
                    checked = tipsOn.value,
                    onCheckedChange = tipsOn::set,
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.tips_reset),
                    onClick = {
                        com.mycompany.app.ui.tips.TipManager.reset(context)
                        tipsOn.reload(true)
                        com.mycompany.app.main.MainUtil.showToast(context, R.string.tips_reset_done)
                    },
                )
            }
            settingsHeader(getString(R.string.hd_home_screen))
            item {
                SettingRow(
                    title = stringResource(R.string.album_shortcut),
                    onClick = {
                        var dialog: DialogEditShort? = null
                        com.mycompany.app.ui.prefs.DialogShow.direct(
                            context as android.app.Activity,
                            requestCodes = intArrayOf(9, 12),
                            onResult = { code, result, data -> dialog?.onPickResult(code, result, data) },
                            onPermission = { code, grants ->
                                val dialogNow = dialog
                                if (dialogNow != null && code == 30 && grants.isNotEmpty() && grants[0] == 0) {
                                    dialogNow.cameraUri = MainUtil.newMediaUri(RequestCode.CAMERA_OR_FILE, dialogNow.hostActivity, false)
                                }
                            },
                        ) { host ->
                            DialogEditShort(host, null, getString(R.string.pdf), 1, null).also { dialog = it }
                        }
                    },
                )
            }
            settingsFooter(getString(R.string.album_short_info))
        }

        if (choosingLocale) {
            ChoiceDialog(
                title = stringResource(R.string.locale),
                options = MainConst.localeList.indices.map { localeName(it) },
                selectedIndex = locale,
                onSelect = { index ->
                    if (index != locale) {
                        locale = index
                        applyLocale(index)
                    }
                },
                onDismiss = { choosingLocale = false },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    if (TtsSettings.appKeypad.value) TtsSettings.reset(context, listOf(TtsSettings.appKeypad))
                    if (!MainSettings.doubleBack.isDefault) MainSettings.reset(context, listOf(MainSettings.doubleBack))
                    keypad.reload(TtsSettings.appKeypad.value)
                    doubleBack.reload(MainSettings.doubleBack.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    private fun localeName(index: Int): String {
        if (index > 0 && index < MainConst.localeList.size) return MainConst.localeList[index]!![2]!!
        return getString(R.string.system_name)
    }

    /** Index into [MainConst.localeList] of the language the app currently uses (0 = follow the system). */
    private fun currentLocaleIndex(): Int {
        val list = AppCompatDelegate.getApplicationLocales()
        val current = if (!list.isEmpty()) list.get(0) else null
        if (current == null) return 0
        val language = current.language ?: ""
        val country = current.country ?: ""
        val table = MainConst.localeList
        for (index in table.indices) if (table[index]!![0] == language && table[index]!![1] == country) return index
        for (index in table.indices) if (table[index]!![0] == language) return index
        return 0
    }

    private fun applyLocale(index: Int) {
        if (index == 0) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
        } else {
            val entry = MainConst.localeList[index]!!
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(entry[0] + "-" + entry[1]))
        }
    }
}
