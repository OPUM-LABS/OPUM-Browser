package com.mycompany.app.ui.settings

import android.content.ActivityNotFoundException
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.text.TextUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSetDown
import com.mycompany.app.dialog.DialogSetDown.SetDownListener
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefPath
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingSliderRow
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.rememberPref
import com.mycompany.app.main.MainUri.treeDocUri as uriToPath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Settings › Download (2026-11-26 tidy-up): where files go (built-in manager or another app, the folder through the
 * storage access picker), while downloading (notification, connections, retries), several downloads (how many at once,
 * the background pause) and the report mail. The app picker takes a plain Activity and is shown directly.
 */
class DownSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val path = intent.getStringExtra("EXTRA_PATH")
        val fastDown = rememberPref({ ZoneSettings.multiDown.value }) { ZoneSettings.edit(context) { set(ZoneSettings.multiDown, it) } }
        val retry = rememberPref({ ZoneSettings.retryDown.value }) { ZoneSettings.edit(context) { set(ZoneSettings.retryDown, it) } }
        val limit = rememberPref({ BrowserSettings.downLimit.value }) { BrowserSettings.edit(context) { set(BrowserSettings.downLimit, it) } }
        val backTime = rememberPref({ BrowserSettings.downBack.value }) { BrowserSettings.edit(context) { set(BrowserSettings.downBack, it) } }
        val noti = rememberPref({ ZoneSettings.notiDown.value }) { ZoneSettings.edit(context) { set(ZoneSettings.notiDown, it) } }
        var tick by remember { mutableIntStateOf(0) }
        var folderName by remember { mutableStateOf<String?>(null) }
        var volumes by remember { mutableStateOf<List<Any?>>(emptyList()) }
        var choosing by remember { mutableIntStateOf(0) } // 2 folder
        var resetting by remember { mutableStateOf(false) }

        // volumes and the display name of the folder come from storage queries: load them off the main thread
        LaunchedEffect(tick) {
            val loaded = withContext(Dispatchers.IO) {
                val list = MainUri.externalVolumes(context)
                if (tick == 0) PrefPath.picked = MainUri.findChild(context, PrefPath.picked!!, list)
                val current = MainUri.storageVolume()
                val name = if (TextUtils.isEmpty(current)) null else MainUri.displayPath(context, current)
                list to name
            }
            volumes = loaded.first ?: emptyList()
            folderName = loaded.second
        }
        OnScreenResume { tick++ }

        val folderResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode != RESULT_OK || data == null) return@rememberLauncherForActivityResult
            val uri = data.data
            val picked = if (uri == null) null else uriToPath(uri)
            if (uri == null || TextUtils.isEmpty(picked)) {
                MainUtil.showToast(context, R.string.invalid_path)
                return@rememberLauncherForActivityResult
            }
            if (picked != PrefPath.picked) {
                PrefPath.picked = picked
                PrefSet.putDownUri(context, MainUri.storageVolume())
                tick++
            }
            MainUtil.keepUriPermission(context, uri)
        }

        fun pickFolder() {
            try {
                val intent = Intent("android.intent.action.OPEN_DOCUMENT_TREE")
                val initial = MainUri.storageVolume()
                if (!TextUtils.isEmpty(initial)) {
                    intent.putExtra("android.provider.extra.INITIAL_URI", Uri.parse(initial))
                }
                folderResult.launch(intent)
            } catch (_: ActivityNotFoundException) {
                MainUtil.showToast(context, R.string.apps_none)
            } catch (_: Exception) {
                MainUtil.showToast(context, R.string.not_supported)
            }
        }

        fun pickApp() {
            val landscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            val dialog = DialogSetDown(this@DownSettingsActivity, "image/*", landscape, object : SetDownListener {
                override fun onPick(label: String?, packageName: String?, className: String?) {
                    if (TextUtils.isEmpty(label) || TextUtils.isEmpty(packageName) || TextUtils.isEmpty(className)) return
                    AlbumSettings.editLocal { set(AlbumSettings.dappName, label) }; AlbumSettings.editLocal { set(AlbumSettings.dappPkg, packageName) }; AlbumSettings.editLocal { set(AlbumSettings.dappCls, className) }
                    PrefAlbum.saveDefaultApp(context)
                    tick++
                }
            })
            dialog.setOnDismissListener { tick++ }
        }

        fun useBuiltIn() {
            if (!TextUtils.isEmpty(AlbumSettings.dappName.value) || !TextUtils.isEmpty(AlbumSettings.dappPkg.value) || !TextUtils.isEmpty(AlbumSettings.dappCls.value)) {
                AlbumSettings.editLocal { set(AlbumSettings.dappName, "") }; AlbumSettings.editLocal { set(AlbumSettings.dappPkg, "") }; AlbumSettings.editLocal { set(AlbumSettings.dappCls, "") }
                PrefAlbum.saveDefaultApp(context)
            }
            tick++
        }

        SettingsScaffold(
            title = stringResource(R.string.down_set),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.hd_where_files))
            item {
                // read again after the app picker closes
                val other = "$tick".let { !TextUtils.isEmpty(AlbumSettings.dappName.value) }
                SettingSegmentedRow(
                    title = stringResource(R.string.down_manager),
                    options = listOf(stringResource(R.string.down_builtin), stringResource(R.string.down_other_app)),
                    selectedIndex = if (other) 1 else 0,
                    onSelect = { index -> if (index == 0) useBuiltIn() else if (!other) pickApp() },
                )
            }
            item {
                val other = "$tick".let { !TextUtils.isEmpty(AlbumSettings.dappName.value) }
                SettingRow(
                    title = stringResource(R.string.down_app),
                    summary = if (other) AlbumSettings.dappName.value else stringResource(R.string.down_app_only, stringResource(R.string.down_other_app)),
                    enabled = other,
                    onClick = { pickApp() },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.down_location),
                    summary = folderName ?: stringResource(R.string.folder_not_selected),
                    onClick = { if (volumes.isEmpty()) pickFolder() else choosing = 2 },
                )
            }
            settingsHeader(getString(R.string.hd_while_down))
            item {
                SettingSwitchRow(title = stringResource(R.string.down_noti_done), checked = noti.value, onCheckedChange = noti::set)
            }
            item {
                // 0 = Auto (4 on Wi-Fi, 2 on mobile data / Data Saver / low-RAM phones), 1 = never split, 2–8 fixed
                val auto = com.mycompany.app.down.DownSpeed.autoConnections(context)
                SettingSliderRow(
                    title = stringResource(R.string.down_connections),
                    value = com.mycompany.app.down.DownSpeed.normalized(fastDown.value),
                    range = 0..com.mycompany.app.down.DownSpeed.MAX,
                    label = { if (it == 0) getString(R.string.down_auto, auto) else "$it" },
                    warnAbove = 6,
                    onValueChange = fastDown::set,
                )
            }
            item {
                SettingSliderRow(
                    title = stringResource(R.string.down_retries),
                    value = retry.value.coerceIn(0, 30),
                    range = 0..30,
                    onValueChange = retry::set,
                )
            }
            settingsFooter(getString(R.string.down_connections_info) + "\n" + getString(R.string.down_size_note))
            settingsHeader(getString(R.string.hd_several_down))
            // 1 = one at a time, 0 = unlimited, > 1 = how many
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.down_at_once),
                    options = listOf(stringResource(R.string.down_one_at_a_time), stringResource(R.string.history_none), stringResource(R.string.user_defined)),
                    selectedIndex = when { limit.value == 1 -> 0; limit.value <= 0 -> 1; else -> 2 },
                    onSelect = { index -> limit.set(when (index) { 0 -> 1; 1 -> 0; else -> 2 }) },
                )
            }
            item {
                SettingSliderRow(
                    title = stringResource(R.string.hd_count), value = limit.value.coerceIn(2, 100), range = 2..100,
                    onValueChange = limit::set, enabled = limit.value > 1,
                    summary = if (limit.value > 1) null else stringResource(R.string.only_custom, stringResource(R.string.user_defined)),
                )
            }
            item {
                SettingSliderRow(
                    title = stringResource(R.string.down_back_pause),
                    value = backTime.value,
                    range = 1..5,
                    label = { "$it " + getString(R.string.time_hour) },
                    onValueChange = backTime::set,
                )
            }
            settingsFooter(getString(R.string.down_queue_note) + "\n" + getString(R.string.down_back_note))
            settingsHeader(getString(R.string.hd_report))
            item {
                SettingRow(
                    title = stringResource(R.string.report_down_site),
                    onClick = {
                        try {
                            val mail = Intent("android.intent.action.SENDTO")
                            mail.data = Uri.parse("mailto:")
                            mail.putExtra("android.intent.extra.EMAIL", arrayOf<String?>("app@opum.ch"))
                            mail.putExtra("android.intent.extra.SUBJECT", getString(R.string.download))
                            mail.putExtra("android.intent.extra.TEXT", MainUtil.feedbackText(context, path))
                            startActivity(mail)
                        } catch (_: Exception) {
                            MainUtil.showToast(context, R.string.apps_none)
                        }
                    },
                )
            }
            settingsFooter(getString(R.string.report_down) + "\n" + getString(R.string.video_down_guide_3))
        }

        when (choosing) {
            2 -> ChoiceDialog(
                title = stringResource(R.string.down_location),
                options = volumes.map { MainUri.volumeLabel(context, it as String?) } + stringResource(R.string.direct_select),
                selectedIndex = volumes.indexOf(PrefPath.picked),
                onSelect = { index ->
                    if (index >= volumes.size) {
                        pickFolder()
                    } else {
                        val chosen = volumes[index] as String?
                        if (!TextUtils.isEmpty(chosen) && chosen != PrefPath.picked) {
                            PrefPath.picked = chosen
                            PrefSet.putDownUri(context, MainUri.storageVolume())
                            tick++
                        }
                    }
                },
                onDismiss = { choosing = 0 },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    fastDown.reload(ZoneSettings.multiDown.value); retry.reload(ZoneSettings.retryDown.value)
                    limit.reload(BrowserSettings.downLimit.value); backTime.reload(BrowserSettings.downBack.value); noti.reload(ZoneSettings.notiDown.value)
                    tick++
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (!TextUtils.isEmpty(AlbumSettings.dappName.value) || !TextUtils.isEmpty(AlbumSettings.dappPkg.value) || !TextUtils.isEmpty(AlbumSettings.dappCls.value)) {
                AlbumSettings.editLocal { set(AlbumSettings.dappName, "") }; AlbumSettings.editLocal { set(AlbumSettings.dappPkg, "") }; AlbumSettings.editLocal { set(AlbumSettings.dappCls, "") }
                val p = PrefAlbum.get(context, false)
                p!!.removeKey("mDappName2"); p.removeKey("mDappPkg2"); p.removeKey("mDappCls2"); p.save()
            }
            if (ZoneSettings.multiDown.value != 0) ZoneSettings.reset(context, listOf(ZoneSettings.multiDown))
            if (ZoneSettings.retryDown.value != 10) ZoneSettings.reset(context, listOf(ZoneSettings.retryDown))
            if (BrowserSettings.downLimit.value != 2) BrowserSettings.reset(context, listOf(BrowserSettings.downLimit))
            if (BrowserSettings.downBack.value != 2) BrowserSettings.reset(context, listOf(BrowserSettings.downBack))
            if (!ZoneSettings.notiDown.value) ZoneSettings.reset(context, listOf(ZoneSettings.notiDown))
        }
    }
}
