package com.mycompany.app.ui.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.InfoDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.rememberPref
import java.util.Locale

/**
 * Settings › Web filter › Ads and cookie banners (2026-11-25, was part of Web filter): the ad blocker, its filter
 * lists, the sites where ads are allowed, and blocking cookie banners (also filter lists); with its guide.
 */
class AdsSettingsActivity : WebFilterActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val adsBlock = rememberPref({ BrowserSettings.adsBlock.value }) { BrowserSettings.edit(context) { set(BrowserSettings.adsBlock, it) } }
        var cookieBlock by remember { mutableStateOf(false) }
        var cookieBusy by remember { mutableStateOf(false) }
        var guide by remember { mutableStateOf(false) }
        var resetting by remember { mutableStateOf(false) }

        fun refreshCookieBlock() {
            Thread {
                val on = com.mycompany.app.dialog.DialogSetFilter.cookieBlockOn(context)
                runOnUiThread { cookieBlock = on }
            }.start()
        }
        LaunchedEffect(Unit) { refreshCookieBlock() }

        /** The "Block cookie banners" switch: turns the cookie-notice lists on (downloading missing ones) or off. */
        fun setCookieBlock(on: Boolean) {
            if (cookieBusy) return
            cookieBusy = true
            cookieBlock = on
            Thread {
                val missing = com.mycompany.app.dialog.DialogSetFilter.setCookieBlock(context, on)
                markFilterChanged()
                runOnUiThread {
                    if (missing.isEmpty()) {
                        cookieBusy = false
                        refreshCookieBlock()
                        return@runOnUiThread
                    }
                    com.mycompany.app.ui.prefs.DialogShow.direct(
                        this@AdsSettingsActivity,
                        onDismiss = { cookieBusy = false; refreshCookieBlock() },
                    ) { host ->
                        var dialog: com.mycompany.app.dialog.DialogUpdateFilter? = null
                        dialog = com.mycompany.app.dialog.DialogUpdateFilter(
                            host, missing, null, null,
                            object : com.mycompany.app.dialog.DialogSetFull.DialogApplyListener {
                                override fun onApply() { dialog?.dismiss() }
                            },
                        )
                        dialog
                    }
                }
            }.start()
        }

        SettingsScaffold(
            title = stringResource(R.string.wf_ads_page),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.hd_ads))
            item { SettingSwitchRow(title = stringResource(R.string.ads_block), checked = adsBlock.value, onCheckedChange = adsBlock::set) }
            item { SettingRow(title = stringResource(R.string.ads_filter), onClick = { openFilterList() }) }
            settingsFooter(getString(R.string.ads_block_info))
            item { SettingRow(title = stringResource(R.string.ads_white), onClick = { openSiteList(19) }) }
            item {
                SettingRow(title = stringResource(R.string.wf_help_ads), onClick = { guide = true })
            }
            settingsHeader(getString(R.string.hd_cookies))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.cookie_block), checked = cookieBlock,
                    onCheckedChange = { setCookieBlock(it) }, enabled = !cookieBusy,
                )
            }
            settingsFooter(getString(R.string.cookie_block_info))
        }

        if (guide) {
            InfoDialog(
                title = stringResource(R.string.wf_help_ads),
                message = listOf(R.string.filter_guide_1, R.string.filter_guide_2, R.string.filter_guide_3).joinToString("\n") { getString(it) } + "\n\n" +
                    String.format(Locale.US, getString(R.string.area_recom_1), getString(R.string.block_area)) + "\n" +
                    String.format(Locale.US, getString(R.string.area_recom_2), getString(R.string.block_area)),
                onDismiss = { guide = false },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = { resetToDefaults(context); adsBlock.reload(BrowserSettings.adsBlock.value) },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Restores the defaults of this page (the filter lists and cookie banners stay as they are). */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (!BrowserSettings.adsBlock.value) BrowserSettings.reset(context, listOf(BrowserSettings.adsBlock))
            if (PdfSettings.filterTime.value == 0L) { PdfSettings.editLocal { set(PdfSettings.filterTime, System.currentTimeMillis()) }; PrefSet.removeNow(context, 7, "mFilterTime") }
            if (PdfSettings.filterDay.value != 7) { PdfSettings.editLocal { set(PdfSettings.filterDay, 7) }; PrefSet.removeNow(context, 7, "mFilterDay") }
        }
    }
}
