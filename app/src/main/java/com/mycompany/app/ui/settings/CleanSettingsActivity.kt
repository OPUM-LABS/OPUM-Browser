package com.mycompany.app.ui.settings

import android.content.Context
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import android.content.Intent
import android.database.Cursor
import android.text.TextUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookFilter
import com.mycompany.app.dialog.DialogGuideArea
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingSegmentedRow
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
import java.io.File
import java.util.Locale

/**
 * Ad blocking and content cleaning. The lists open the Compose list screens (`SiteListActivity`, `SavedSitesActivity`,
 * `FilterScriptListActivity`); when the ad filter list is closed the filter files on disk
 * are cleaned up exactly like the old screen did. The result for the browser (`EXTRA_FILTER`, `EXTRA_TYPE`) is kept.
 */
class CleanSettingsActivity : OpumComposeActivity() {
    private var fromPopup = false
    private var filterCountAtStart = 0L
    private var filterCountSeen = 0L
    private var filterChanged = false
    private var filterOpened = false
    private var filterPending = false
    private var filterProcessed = false

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        fromPopup = intent.getBooleanExtra("EXTRA_POPUP", false)
        super.onCreate(savedInstanceState)
        Thread {
            val count = DbBookFilter.totalCount(this)
            filterCountAtStart = count
            filterCountSeen = count
        }.start()
    }

    override fun onResume() {
        super.onResume()
        if (filterPending) {
            filterPending = false
            onFilterListClosed(this)
        }
    }

    override fun finish() {
        if (filterChanged) {
            setResult(RESULT_OK, filterResult(true))
        } else {
            if (filterOpened && !filterProcessed && filterCountAtStart != DbBookFilter.totalCount(this)) {
                setResult(RESULT_OK, filterResult(true))
            }
        }
        super.finish()
    }

    private fun filterResult(filter: Boolean): Intent {
        val data = Intent().putExtra("EXTRA_FILTER", filter)
        if (fromPopup) data.putExtra("EXTRA_TYPE", 62)
        return data
    }

    /** Runs after the ad filter list was closed: remembers that the filters changed and removes unused filter files. */
    private fun onFilterListClosed(context: Context) {
        Thread {
            val count = DbBookFilter.totalCount(context)
            if (filterCountAtStart != count) filterChanged = true
            filterProcessed = true
            if (filterCountSeen == count) return@Thread
            filterCountSeen = count
            if (PdfSettings.filterInit.value) return@Thread
            val keep = ArrayList<Any?>()
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    DbBookFilter.get(context).getWritableDatabase(), "DbBookFilter_table",
                    arrayOf<String?>("_path"), null, null, null,
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex("_path")
                    do {
                        val path = MainUtil.filterFilePath(context, cursor.getString(index))
                        if (!TextUtils.isEmpty(path)) keep.add(path)
                    } while (cursor.moveToNext())
                }
            } catch (_: Exception) {
            }
            cursor?.close()
            val dir = MainUtil.filesSubdir(context, ".filter")
            if (TextUtils.isEmpty(dir)) return@Thread
            deleteUnused(File(dir!!), true, keep)
        }.start()
    }

    private fun deleteUnused(file: File, keepSelf: Boolean, keep: List<Any?>) {
        try {
            val children = if (file.isDirectory) file.listFiles() else null
            if (file.isDirectory && children != null) {
                for (child in children) {
                    if (child.isDirectory) deleteUnused(child, false, keep)
                    else if (!keep.contains(child.path)) child.delete()
                }
            }
            if (!keepSelf) file.delete()
        } catch (_: Exception) {
        }
    }

    @Composable
    override fun Content() {
        val context: Context = this
        val adsBlock = rememberPref({ BrowserSettings.adsBlock.value }) { BrowserSettings.edit(context) { set(BrowserSettings.adsBlock, it) } }
        val areaBlock = rememberPref({ BrowserSettings.areaBlock.value }) { BrowserSettings.edit(context) { set(BrowserSettings.areaBlock, it) } }
        val overBlock = rememberPref({ TtsSettings.checkOver.value }) { TtsSettings.edit(context) { set(TtsSettings.checkOver, it) } }
        val popBlock = rememberPref({ BrowserSettings.popBlock.value }) { BrowserSettings.edit(context) { set(BrowserSettings.popBlock, it) } }
        val appBlock = rememberPref({ PdfSettings.appBlock.value }) { PdfSettings.edit(context) { set(PdfSettings.appBlock, it) } }
        var linkLock by remember { mutableIntStateOf(SecretSettings.linkLockType.value) }
        var choosing by remember { mutableIntStateOf(0) } // 8 over, 11 pop, 14 app, 17 lock type, 100 help menu
        var guide by remember { mutableStateOf<String?>(null) }
        var lockGuide by remember { mutableStateOf(false) }
        var areaGuide by remember { mutableStateOf(false) }
        var resetting by remember { mutableStateOf(false) }
        var launchPurpose by remember { mutableIntStateOf(0) } // 1 open blocked links, 2 set lock, 3 clear lock
        var cookieBlock by remember { mutableStateOf(false) }
        var cookieBusy by remember { mutableStateOf(false) }

        fun refreshCookieBlock() {
            Thread {
                val on = com.mycompany.app.dialog.DialogSetFilter.cookieBlockOn(context)
                runOnUiThread { cookieBlock = on }
            }.start()
        }

        /** The "Block cookie banners" switch: turns the cookie-notice lists on (downloading missing ones) or off. */
        fun setCookieBlock(on: Boolean) {
            if (cookieBusy) return
            cookieBusy = true
            cookieBlock = on
            Thread {
                val missing = com.mycompany.app.dialog.DialogSetFilter.setCookieBlock(context, on)
                filterChanged = true
                runOnUiThread {
                    if (missing.isEmpty()) {
                        cookieBusy = false
                        refreshCookieBlock()
                        return@runOnUiThread
                    }
                    com.mycompany.app.ui.prefs.DialogShow.direct(
                        this@CleanSettingsActivity,
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

        fun openList(code: Int) {
            if (com.mycompany.app.ui.lists.SiteListActivity.supports(code)) {
                com.mycompany.app.ui.lists.SiteListActivity.open(context, code)
                return
            }
            if (code == 23) {
                context.startActivity(Intent(context, com.mycompany.app.ui.lists.SavedSitesActivity::class.java).putExtra(com.mycompany.app.ui.lists.SavedSitesActivity.EXTRA_TYPE, 23))
                return
            }
            if (code == 24) {
                filterOpened = true
                filterPending = true
                context.startActivity(Intent(context, com.mycompany.app.ui.lists.FilterScriptListActivity::class.java).putExtra(com.mycompany.app.ui.lists.FilterScriptListActivity.EXTRA_TYPE, 24))
                return
            }
        }

        val lockResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != RESULT_OK) { launchPurpose = 0; return@rememberLauncherForActivityResult }
            when (launchPurpose) {
                1 -> if (SecretSettings.linkLockType.value != 0) openList(22)
                3 -> {
                    SecretSettings.editLocal { set(SecretSettings.linkLockType, 0) }
                    SecretSettings.editLocal { set(SecretSettings.linkLockCode, "") }
                    val prefs = PrefSecret.get(context, false)
                    prefs!!.removeKey("mLinkLockType")
                    prefs.removeKey("mLinkLockCode")
                    prefs.saveAsync()
                }
            }
            launchPurpose = 0
            linkLock = SecretSettings.linkLockType.value
        }

        OnScreenResume { linkLock = SecretSettings.linkLockType.value; refreshCookieBlock() }

        SettingsScaffold(
            title = stringResource(R.string.clean_mode),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.hd_ads))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.ads_block),
                    checked = adsBlock.value,
                    onCheckedChange = adsBlock::set,
                )
            }
            item { SettingRow(title = stringResource(R.string.ads_filter), onClick = { openList(24) }) }
            item { SettingRow(title = stringResource(R.string.ads_white), onClick = { openList(19) }) }
            settingsFooter(getString(R.string.ads_block_info) + "\n" + getString(R.string.guide_ads_allow))
            settingsHeader(getString(R.string.hd_elements))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.block_area),
                    checked = areaBlock.value,
                    onCheckedChange = areaBlock::set,
                )
            }
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.over_block),
                    summary = if (overInfos[overBlock.value] != 0) stringResource(overInfos[overBlock.value]) else null,
                    options = overValues.map { stringResource(overLabels[it]) },
                    selectedIndex = overValues.indexOf(overBlock.value),
                    onSelect = { overBlock.set(overValues[it]) },
                )
            }
            item { SettingRow(title = stringResource(R.string.over_white), onClick = { openList(20) }) }
            settingsFooter(getString(R.string.area_info_13) + "\n" + getString(R.string.guide_over_allow))
            settingsHeader(getString(R.string.hd_cookies))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.cookie_block),
                    checked = cookieBlock,
                    onCheckedChange = { setCookieBlock(it) },
                    enabled = !cookieBusy,
                )
            }
            settingsFooter(getString(R.string.cookie_block_info))
            settingsHeader(getString(R.string.hd_popups))
            item {
                SettingRow(
                    title = stringResource(R.string.pop_block),
                    summary = stringResource(popLabels[popBlock.value]) +
                        if (popInfos[popBlock.value] != 0) "\n" + stringResource(popInfos[popBlock.value]) else "",
                    onClick = { choosing = 11 },
                )
            }
            item { SettingRow(title = stringResource(R.string.pop_white), onClick = { openList(21) }) }
            settingsFooter(getString(R.string.guide_pop_allow))
            settingsHeader(getString(R.string.hd_apps))
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.app_block),
                    summary = appBlockInfo(context),
                    options = appValues.map { stringResource(appLabels[it]) },
                    selectedIndex = appValues.indexOf(appBlock.value),
                    onSelect = { appBlock.set(appValues[it]) },
                )
            }
            settingsHeader(getString(R.string.hd_blocked))
            item {
                SettingRow(
                    title = stringResource(R.string.blocked_link),
                    onClick = {
                        if (SecretSettings.linkLockType.value != 0) {
                            launchPurpose = 1
                            lockResult.launch(
                                MainUtil.lockIntent(context, SecretSettings.linkLockType.value).putExtra("EXTRA_PASS", 2).putExtra("EXTRA_TYPE", 2)
                            )
                        } else {
                            lockGuide = true
                        }
                    },
                )
            }
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.lock_type),
                    value = stringResource(MainConst.lockNames[linkLock]),
                    onClick = { choosing = 17 },
                )
            }
            item { SettingRow(title = stringResource(R.string.blocked_image), onClick = { openList(23) }) }
            settingsDivider()
            item { SettingRow(title = stringResource(R.string.web_filter_help), onClick = { choosing = 100 }) }
        }

        when (choosing) {
            11 -> ChoiceDialog(
                title = stringResource(R.string.pop_block),
                options = popValues.map { stringResource(popLabels[it]) },
                selectedIndex = popValues.indexOf(popBlock.value),
                onSelect = { popBlock.set(popValues[it]) },
                onDismiss = { choosing = 0 },
            )
            17 -> ChoiceDialog(
                title = stringResource(R.string.lock_type),
                options = MainConst.lockValues.map { stringResource(MainConst.lockNames[it]) },
                selectedIndex = MainConst.lockValues.indexOf(linkLock),
                onSelect = { index ->
                    val type = MainConst.lockValues[index]
                    if (type == 4) {
                        if (SecretSettings.linkLockType.value != type && MainUtil.canUseBiometric(context, true)) {
                            SecretSettings.editLocal { set(SecretSettings.linkLockType, type) }
                            SecretSettings.editLocal { set(SecretSettings.linkLockCode, "") }
                            PrefSecret.saveLinkLock(context)
                            linkLock = type
                        }
                    } else if (type != 0) {
                        launchPurpose = 2
                        lockResult.launch(MainUtil.lockIntent(context, type).putExtra("EXTRA_PASS", 2).putExtra("EXTRA_TYPE", 1))
                    } else if (SecretSettings.linkLockType.value != type) {
                        launchPurpose = 3 // the current lock must be confirmed before it is removed
                        lockResult.launch(
                            MainUtil.lockIntent(context, SecretSettings.linkLockType.value).putExtra("EXTRA_PASS", 2).putExtra("EXTRA_TYPE", 2)
                        )
                    }
                },
                onDismiss = { choosing = 0 },
            )
            100 -> ChoiceDialog(
                title = stringResource(R.string.clean_mode),
                options = listOf(
                    stringResource(R.string.ads_block),
                    stringResource(R.string.block_area), stringResource(R.string.over_block),
                ),
                selectedIndex = -1,
                onSelect = { index ->
                    if (AlbumSettings.notiClea.value) { AlbumSettings.edit(context) { set(AlbumSettings.notiClea, false) } }
                    if (index == 1) {
                        com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host -> DialogGuideArea(host, 3, null) }
                    } else {
                        guide = when (index) {
                            0 -> listOf(R.string.filter_guide_1, R.string.filter_guide_2, R.string.filter_guide_3)
                            else -> listOf(R.string.over_block_guide_1, R.string.over_block_guide_2, R.string.over_block_guide_3)
                        }.joinToString("\n") { getString(it) } + "\n\n" +
                            String.format(Locale.US, getString(R.string.area_recom_1), getString(R.string.block_area)) + "\n" +
                            String.format(Locale.US, getString(R.string.area_recom_2), getString(R.string.block_area))
                    }
                },
                onDismiss = { choosing = 0 },
            )
        }
        guide?.let { InfoDialog(title = stringResource(R.string.clean_mode), message = it, onDismiss = { guide = null }) }
        if (lockGuide) {
            InfoDialog(
                title = stringResource(R.string.clean_mode),
                message = stringResource(R.string.password_lock_1),
                onDismiss = { lockGuide = false },
            )
        }
        if (areaGuide) areaGuide = false
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    adsBlock.reload(BrowserSettings.adsBlock.value); areaBlock.reload(BrowserSettings.areaBlock.value)
                    overBlock.reload(TtsSettings.checkOver.value); popBlock.reload(BrowserSettings.popBlock.value); appBlock.reload(PdfSettings.appBlock.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    private fun appBlockInfo(context: Context): String? {
        if (PdfSettings.appBlock.value == 0) return null
        val first = context.getString(R.string.app_block_info1)
        return if (PdfSettings.appBlock.value == 2) first else first + "\n" + context.getString(R.string.app_block_info2)
    }

    companion object {
        private val none = R.string.not_used

        /** Pop-up / overlay / app block modes: values in menu order and label / info of each value (index = value). */
        @JvmField val overLabels: IntArray = intArrayOf(none, R.string.check_once, R.string.check_keep)
        @JvmField val overInfos: IntArray = intArrayOf(0, R.string.over_block_info, R.string.fast_down_guide)
        @JvmField val overValues: IntArray = intArrayOf(1, 2, 0)
        @JvmField val popLabels: IntArray = intArrayOf(none, R.string.level_weak, R.string.normal_rate, R.string.level_strong)
        @JvmField val popInfos: IntArray = intArrayOf(0, R.string.pop_block_info3, R.string.pop_block_info2, R.string.pop_block_info1)
        @JvmField val popValues: IntArray = intArrayOf(3, 2, 1, 0)
        @JvmField val appLabels: IntArray = intArrayOf(none, R.string.app_site, R.string.always_block)
        @JvmField val appValues: IntArray = intArrayOf(2, 1, 0)

        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (!BrowserSettings.adsBlock.value) BrowserSettings.reset(context, listOf(BrowserSettings.adsBlock))
            if (TtsSettings.checkEle.value) TtsSettings.reset(context, listOf(TtsSettings.checkEle))
            if (!BrowserSettings.areaBlock.value) BrowserSettings.reset(context, listOf(BrowserSettings.areaBlock))
            if (TtsSettings.checkOver.value != 0) TtsSettings.reset(context, listOf(TtsSettings.checkOver))
            if (!BrowserSettings.popBlock.isDefault) BrowserSettings.reset(context, listOf(BrowserSettings.popBlock))
            if (PdfSettings.appBlock.value != 2) PdfSettings.reset(context, listOf(PdfSettings.appBlock))
            if (PdfSettings.filterTime.value == 0L) { PdfSettings.editLocal { set(PdfSettings.filterTime, System.currentTimeMillis()) }; PrefSet.removeNow(context, 7, "mFilterTime") }
            if (PdfSettings.filterDay.value != 7) { PdfSettings.editLocal { set(PdfSettings.filterDay, 7) }; PrefSet.removeNow(context, 7, "mFilterDay") }
        }
    }
}
