package com.mycompany.app.ui.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Settings › Privacy › Cookies, JavaScript and HTTPS (2026-11-25, was part of Privacy): which cookies are accepted
 * (choices on the page, dialogs before), JavaScript and the sites where it is blocked, HTTPS only and SSL error
 * blocking.
 */
class SiteSafetySettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val cookie = rememberPref({ BrowserSettings.cookieType.value }) {
            BrowserSettings.edit(context) { set(BrowserSettings.cookieType, it) }; MainApp.applyCookieSetting(context)
        }
        val third = rememberPref({ BrowserSettings.thirdType.value }) { BrowserSettings.edit(context) { set(BrowserSettings.thirdType, it) } }
        val js = rememberPref({ BrowserSettings.enableJs.value }) { BrowserSettings.edit(context) { set(BrowserSettings.enableJs, it) } }
        val https = rememberPref({ PdfSettings.onlyHttps.value }) { PdfSettings.edit(context) { set(PdfSettings.onlyHttps, it) } }
        val ssl = rememberPref({ PdfSettings.blockSsl.value }) { PdfSettings.edit(context) { set(PdfSettings.blockSsl, it) } }
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.priv_web_page),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.cookie))
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.accept_cookie),
                    options = MainConst.cookieValues.map { stringResource(MainConst.cookieNames[it]) },
                    selectedIndex = MainConst.cookieValues.indexOf(cookie.value),
                    onSelect = { cookie.set(MainConst.cookieValues[it]) },
                )
            }
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.third_cookie),
                    options = MainConst.cookieValues.map { stringResource(MainConst.cookieNames[it]) },
                    selectedIndex = MainConst.cookieValues.indexOf(third.value),
                    onSelect = { third.set(MainConst.cookieValues[it]) },
                )
            }
            settingsHeader("JavaScript")
            item { SettingSwitchRow(title = stringResource(R.string.java_script), checked = js.value, onCheckedChange = js::set) }
            settingsFooter(getString(R.string.java_script_info))
            item {
                SettingRow(
                    title = stringResource(R.string.js_black),
                    onClick = { com.mycompany.app.ui.lists.SiteListActivity.open(context, 27) },
                )
            }
            settingsHeader(getString(R.string.hd_connection))
            item { SettingSwitchRow(title = stringResource(R.string.only_https), checked = https.value, onCheckedChange = https::set) }
            settingsFooter(getString(R.string.only_https_info))
            item { SettingSwitchRow(title = stringResource(R.string.block_ssl), checked = ssl.value, onCheckedChange = ssl::set) }
            settingsFooter(getString(R.string.block_ssl_info))
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    cookie.reload(BrowserSettings.cookieType.value); third.reload(BrowserSettings.thirdType.value)
                    js.reload(BrowserSettings.enableJs.value); https.reload(PdfSettings.onlyHttps.value); ssl.reload(PdfSettings.blockSsl.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (BrowserSettings.cookieType.value != 0) {
                BrowserSettings.editLocal { set(BrowserSettings.cookieType, 0) }; PrefSet.removeNow(context, 14, "mCookieType"); MainApp.applyCookieSetting(context)
            }
            if (!BrowserSettings.thirdType.isDefault) BrowserSettings.reset(context, listOf(BrowserSettings.thirdType))
            if (!BrowserSettings.enableJs.value) BrowserSettings.reset(context, listOf(BrowserSettings.enableJs))
            if (!PdfSettings.onlyHttps.isDefault) PdfSettings.reset(context, listOf(PdfSettings.onlyHttps))
            if (PdfSettings.blockSsl.value) PdfSettings.reset(context, listOf(PdfSettings.blockSsl))
        }
    }
}
