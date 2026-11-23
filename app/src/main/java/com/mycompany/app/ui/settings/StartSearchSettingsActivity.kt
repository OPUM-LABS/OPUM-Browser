package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.data.book.DataBookRecent
import com.mycompany.app.db.book.DbBookSearch
import com.mycompany.app.dialog.DialogSetSuggest
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingSliderRow
import com.mycompany.app.ui.components.SettingSegmentedRow
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
import com.mycompany.app.web.WebSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Start page & search (2026-11-19 regrouping): home / start / new-tab pages and the search engine (from the old Site
 * page), search suggestions, auto-complete and recent searches (from Composition). The home-page editor and the
 * search-engine list are separate screens; their results are read again when this screen resumes.
 */
class StartSearchSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val path = intent.getStringExtra("EXTRA_PATH")
        var tick by remember { mutableIntStateOf(0) } // bumped on resume so summaries are read again
        val autoComp = rememberPref({ ZtwoSettings.autoComp.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.autoComp, it) } }
        val recentLimit = rememberPref({ ZtwoSettings.recentLimit.value }) {
            ZtwoSettings.edit(context) { set(ZtwoSettings.recentLimit, it) }
            DataBookRecent.instance(context).changed = true
        }
        val recentSecret = rememberPref({ ZtwoSettings.recentSecret.value }) {
            ZtwoSettings.edit(context) { set(ZtwoSettings.recentSecret, it) }
            if (SyncSettings.secretMode.value) DataBookRecent.instance(context).changed = true
        }
        var choosing by remember { mutableIntStateOf(0) } // 2 start page, 3 new tab page
        var resetting by remember { mutableStateOf(false) }

        OnScreenResume { tick++ }

        // a custom search engine is stored in the database: load it once, then show its name
        LaunchedEffect(tick) {
            if (ZtwoSettings.searchType.value >= 10 && WebSearch.instance().engineById((ZtwoSettings.searchType.value - 100).toLong()) == null) {
                withContext(Dispatchers.IO) { DbBookSearch.engines(context) }
                tick++
            }
        }

        fun pageSummary(type: Int): String {
            val value = when (type) { 1 -> BrowserSettings.homePage.value; 2 -> BrowserSettings.startPage.value; else -> BrowserSettings.tabPage.value }
            if (TextUtils.isEmpty(value)) {
                return getString(if (type == 2) R.string.last_page else R.string.soul_home)
            }
            return when (value) {
                "..home_link" -> getString(R.string.home_page)
                "file:///android_asset/shortcut.html" -> getString(R.string.soul_home)
                "about:blank" -> getString(R.string.blank_page)
                else -> value!!
            }
        }

        fun searchName(): String? =
            if (ZtwoSettings.searchType.value < 10) getString(MainConst.engineNames[ZtwoSettings.searchType.value])
            else WebSearch.instance().engineById((ZtwoSettings.searchType.value - 100).toLong())?.name

        fun openHome(type: Int) {
            startActivity(Intent(context, HomeSettingsActivity::class.java).putExtra("EXTRA_TYPE", type).putExtra("EXTRA_PATH", path))
        }

        SettingsScaffold(
            title = stringResource(R.string.set_start_search),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.hd_pages))
            item {
                SettingRow(
                    title = stringResource(R.string.home_page),
                    summary = "$tick".let { pageSummary(1) } + "\n" + stringResource(R.string.page_home_info),
                    onClick = { openHome(1) },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.start_page),
                    summary = "$tick".let { pageSummary(2) } + "\n" + stringResource(R.string.page_start_info),
                    onClick = { choosing = 2 },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.new_url),
                    summary = "$tick".let { pageSummary(3) } + "\n" + stringResource(R.string.page_tab_info),
                    onClick = { choosing = 3 },
                )
            }
            settingsHeader(getString(R.string.hd_search))
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.search_engine),
                    value = "$tick".let { searchName() ?: "" },
                    onClick = { startActivity(Intent(context, com.mycompany.app.ui.lists.SearchEngineListActivity::class.java)) },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.search_suggest),
                    summary = "$tick".let { suggestSummary(context) } ?: "",
                    onClick = { com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host -> DialogSetSuggest(host) } },
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.auto_comp),
                    checked = autoComp.value,
                    onCheckedChange = autoComp::set,
                )
            }
            settingsHeader(getString(R.string.recent_search))
            // 0 = do not save, < 0 = unlimited, > 0 = how many (a slider instead of the old number dialog)
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.hd_keep),
                    options = listOf(stringResource(R.string.history_zero), stringResource(R.string.history_none), stringResource(R.string.user_defined)),
                    selectedIndex = when { recentLimit.value == 0 -> 0; recentLimit.value < 0 -> 1; else -> 2 },
                    onSelect = { index -> recentLimit.set(when (index) { 0 -> 0; 1 -> -1; else -> 20 }) },
                )
            }
            if (recentLimit.value > 0) {
                item {
                    SettingSliderRow(
                        title = stringResource(R.string.hd_count),
                        value = recentLimit.value,
                        range = 1..100,
                        onValueChange = recentLimit::set,
                    )
                }
            }
            if (recentLimit.value != 0) {
                item {
                    SettingSwitchRow(
                        title = stringResource(R.string.recent_secret),
                        checked = recentSecret.value,
                        onCheckedChange = recentSecret::set,
                    )
                }
            }
        }

        when (choosing) {
            2 -> {
                val current = if (TextUtils.isEmpty(BrowserSettings.startPage.value)) 0 else if ("..home_link" == BrowserSettings.startPage.value) 1 else 2
                ChoiceDialog(
                    title = stringResource(R.string.start_page),
                    options = listOf(stringResource(R.string.last_page), stringResource(R.string.home_page), stringResource(R.string.user_defined)),
                    selectedIndex = current,
                    onSelect = { index ->
                        when (index) {
                            0 -> { BrowserSettings.edit(context) { set(BrowserSettings.startPage, "") }; tick++ }
                            1 -> { BrowserSettings.edit(context) { set(BrowserSettings.startPage, "..home_link") }; tick++ }
                            else -> openHome(2)
                        }
                    },
                    onDismiss = { choosing = 0 },
                )
            }
            3 -> ChoiceDialog(
                title = stringResource(R.string.new_url),
                options = listOf(stringResource(R.string.home_page), stringResource(R.string.user_defined)),
                selectedIndex = if ("..home_link" == BrowserSettings.tabPage.value) 0 else 1,
                onSelect = { index ->
                    if (index == 0) { BrowserSettings.edit(context) { set(BrowserSettings.tabPage, "..home_link") }; tick++ }
                    else openHome(3)
                },
                onDismiss = { choosing = 0 },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    autoComp.reload(ZtwoSettings.autoComp.value)
                    recentLimit.reload(ZtwoSettings.recentLimit.value)
                    recentSecret.reload(ZtwoSettings.recentSecret.value)
                    tick++
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /** Which suggestion sources are on (null when none). */
    private fun suggestSummary(context: Context): String? {
        val engine = BrowserSettings.sugEngine.value
        val types = BrowserSettings.sugType.value
        if (engine == 0 && types == 0) return null
        val parts = ArrayList<String>()
        if (engine == 1) parts.add(context.getString(R.string.duckduckgo))
        else if (engine == 2) parts.add(context.getString(R.string.google))
        listOf(2 to R.string.recent_search, 4 to R.string.history, 8 to R.string.bookmark, 16 to R.string.quick_access)
            .forEach { (bit, label) -> if ((types and bit) == bit) parts.add(context.getString(label)) }
        return if (parts.isEmpty()) null else parts.joinToString(", ")
    }

    companion object {
        /** Restores the defaults of this page (pages, search engine, suggestions, recent searches). */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if ("file:///android_asset/shortcut.html" != BrowserSettings.homePage.value) {
                BrowserSettings.editLocal { set(BrowserSettings.homePage, "file:///android_asset/shortcut.html") }; PrefSet.removeNow(context, 14, "mHomePage3")
            }
            if (!TextUtils.isEmpty(BrowserSettings.startPage.value)) BrowserSettings.reset(context, listOf(BrowserSettings.startPage))
            if ("..home_link" != BrowserSettings.tabPage.value) BrowserSettings.reset(context, listOf(BrowserSettings.tabPage))
            if (ZtwoSettings.searchType.value != 4 || ZtwoSettings.searchColor.value != 0 || !TextUtils.isEmpty(ZtwoSettings.searchUser.value)) {
                ZtwoSettings.editLocal { set(ZtwoSettings.searchType, 4) }; ZtwoSettings.editLocal { set(ZtwoSettings.searchUser, "") }; ZtwoSettings.editLocal { set(ZtwoSettings.searchColor, 0) }; ZtwoSettings.editLocal { set(ZtwoSettings.searchForm, false) }
                val p = PrefZtwo.get(context, false)
                p!!.removeKey("mSearchType2"); p.removeKey("mSearchUser"); p.removeKey("mSearchForm2"); p.removeKey("mSearchColor"); p.save()
            }
            if (!TextUtils.isEmpty(ZoneSettings.searchUse.value)) ZoneSettings.reset(context, listOf(ZoneSettings.searchUse))
            val engine = if (MainUtil.isChinaLocale()) 1 else 2
            if (BrowserSettings.sugEngine.value != engine || BrowserSettings.sugType.value != 14) {
                BrowserSettings.editLocal { set(BrowserSettings.sugEngine, engine) }; BrowserSettings.editLocal { set(BrowserSettings.sugType, 14) }
                val p = PrefWeb.get(context, false)
                p!!.removeKey("mSugEng"); p.removeKey("mSugType3"); p.save()
            }
            if (!ZtwoSettings.autoComp.value) ZtwoSettings.reset(context, listOf(ZtwoSettings.autoComp))
            if (ZtwoSettings.recentLimit.value != 20) {
                ZtwoSettings.editLocal { set(ZtwoSettings.recentLimit, 20) }; PrefSet.removeNow(context, 16, "mRecentLimit"); DataBookRecent.instance(context).changed = true
            }
            if (ZtwoSettings.recentSecret.value) {
                ZtwoSettings.editLocal { set(ZtwoSettings.recentSecret, false) }; PrefSet.removeNow(context, 16, "mRecentSecret")
                if (SyncSettings.secretMode.value) DataBookRecent.instance(context).changed = true
            }
        }
    }
}
