package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.prefs.OnScreenResume

/**
 * Settings › Start page & search › Start pages (2026-11-26, was part of Start page & search): the home page (the
 * address editor [HomeSettingsActivity]), what opens when the app starts (last page / home page / custom) and in a new
 * tab (home page / custom). A custom address is edited in the same editor; its row stays, greyed out, without Custom.
 */
class StartPagesSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val path = intent.getStringExtra("EXTRA_PATH")
        var tick by remember { mutableIntStateOf(0) } // bumped on resume: the editor changes the pages
        var resetting by remember { mutableStateOf(false) }
        OnScreenResume { tick++ }

        fun openEditor(type: Int) {
            startActivity(Intent(context, HomeSettingsActivity::class.java).putExtra("EXTRA_TYPE", type).putExtra("EXTRA_PATH", path))
        }

        val onlyCustom = stringResource(R.string.only_custom, stringResource(R.string.user_defined))

        SettingsScaffold(
            title = stringResource(R.string.hd_start_pages),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            item {
                SettingRow(
                    title = stringResource(R.string.home_page),
                    summary = "$tick".let { pageName(context, 1) } + "\n" + stringResource(R.string.page_home_info),
                    onClick = { openEditor(1) },
                )
            }
            settingsDivider()
            item {
                val start = "$tick".let { BrowserSettings.startPage.value }
                SettingSegmentedRow(
                    title = stringResource(R.string.start_page),
                    summary = stringResource(R.string.page_start_info),
                    options = listOf(stringResource(R.string.last_page), stringResource(R.string.home_page), stringResource(R.string.user_defined)),
                    selectedIndex = startIndex(start),
                    onSelect = { index ->
                        when (index) {
                            0 -> { BrowserSettings.edit(context) { set(BrowserSettings.startPage, "") }; tick++ }
                            1 -> { BrowserSettings.edit(context) { set(BrowserSettings.startPage, HOME_LINK) }; tick++ }
                            else -> if (startIndex(start) != 2) openEditor(2)
                        }
                    },
                )
            }
            item {
                val custom = "$tick".let { startIndex(BrowserSettings.startPage.value) == 2 }
                SettingRow(
                    title = stringResource(R.string.page_address),
                    summary = if (custom) pageName(context, 2) else onlyCustom,
                    enabled = custom,
                    onClick = { openEditor(2) },
                )
            }
            settingsDivider()
            item {
                val tab = "$tick".let { BrowserSettings.tabPage.value }
                SettingSegmentedRow(
                    title = stringResource(R.string.new_url),
                    summary = stringResource(R.string.page_tab_info),
                    options = listOf(stringResource(R.string.home_page), stringResource(R.string.user_defined)),
                    selectedIndex = if (HOME_LINK == tab) 0 else 1,
                    onSelect = { index ->
                        if (index == 0) { BrowserSettings.edit(context) { set(BrowserSettings.tabPage, HOME_LINK) }; tick++ }
                        else if (HOME_LINK == tab) openEditor(3)
                    },
                )
            }
            item {
                val custom = "$tick".let { HOME_LINK != BrowserSettings.tabPage.value }
                SettingRow(
                    title = stringResource(R.string.page_address),
                    summary = if (custom) pageName(context, 3) else onlyCustom,
                    enabled = custom,
                    onClick = { openEditor(3) },
                )
            }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = { resetToDefaults(context); tick++ },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        private const val HOME_LINK = "..home_link"
        private const val OPUM_HOME = "file:///android_asset/shortcut.html"

        /** 0 last page, 1 home page, 2 a custom address. */
        private fun startIndex(value: String?): Int = if (TextUtils.isEmpty(value)) 0 else if (HOME_LINK == value) 1 else 2

        /** What a page setting opens, in words (1 home page, 2 start page, 3 new tab). */
        fun pageName(context: Context, type: Int): String {
            val value = when (type) { 1 -> BrowserSettings.homePage.value; 2 -> BrowserSettings.startPage.value; else -> BrowserSettings.tabPage.value }
            if (TextUtils.isEmpty(value)) return context.getString(if (type == 2) R.string.last_page else R.string.soul_home)
            return when (value) {
                HOME_LINK -> context.getString(R.string.home_page)
                OPUM_HOME -> context.getString(R.string.soul_home)
                "about:blank" -> context.getString(R.string.blank_page)
                else -> value!!
            }
        }

        /** Restores the defaults of this page: the OPUM homepage, the last page at start, the home page in a new tab. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (OPUM_HOME != BrowserSettings.homePage.value) {
                BrowserSettings.editLocal { set(BrowserSettings.homePage, OPUM_HOME) }; PrefSet.removeNow(context, 14, "mHomePage3")
            }
            if (!TextUtils.isEmpty(BrowserSettings.startPage.value)) BrowserSettings.reset(context, listOf(BrowserSettings.startPage))
            if (HOME_LINK != BrowserSettings.tabPage.value) BrowserSettings.reset(context, listOf(BrowserSettings.tabPage))
        }
    }
}
