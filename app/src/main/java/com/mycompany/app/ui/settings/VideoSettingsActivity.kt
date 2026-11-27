package com.mycompany.app.ui.settings

import android.content.Context
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.PdfSettings
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.mycompany.app.dialog.DialogSeekSimple
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.store.VideoSettings
import com.mycompany.app.main.MainApp
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingSliderRow
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

/** Video player settings: picture-in-picture, cut-out, drag gestures, double-tap areas and the touch lock type. */
class VideoSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val fromWeb = intent.getBooleanExtra("EXTRA_WEB", false)
        val sdk = Build.VERSION.SDK_INT
        val pip = rememberPref({ VideoSettings.pipOnHome.value }) { VideoSettings.edit(context) { set(VideoSettings.pipOnHome, it) } }
        val cutOut = rememberPref({ VideoSettings.cutOut.value }) { VideoSettings.edit(context) { set(VideoSettings.cutOut, it) } }
        val iconLong = rememberPref({ PdfSettings.iconLong.value }) { PdfSettings.edit(context) { set(PdfSettings.iconLong, it) } }
        val bright = rememberPref({ VideoSettings.dragBright.value }) { VideoSettings.edit(context) { set(VideoSettings.dragBright, it) } }
        val volume = rememberPref({ VideoSettings.dragVolume.value }) { VideoSettings.edit(context) { set(VideoSettings.dragVolume, it) } }
        val dragSeek = rememberPref({ VideoSettings.dragSeek.value }) { VideoSettings.edit(context) { set(VideoSettings.dragSeek, it) } }
        val sense = rememberPref({ VideoSettings.seekSense.value }) { VideoSettings.edit(context) { set(VideoSettings.seekSense, it) } }
        var lockType by remember { mutableIntStateOf(SecretSettings.touchLockType.value) }
        // the touch lock is unlocked with the app lock or a lock of its own (SecretSettings.guardType, 2026-11-27)
        var ownLock by remember { mutableStateOf(SecretSettings.ownLock(3)) }
        var choosing by remember { mutableIntStateOf(0) } // 3 icon type, 13 lock type
        var resetting by remember { mutableStateOf(false) }

        val lockResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // the lock-setup screen saves the new type itself
            if (it.resultCode == RESULT_OK) lockType = SecretSettings.touchLockType.value
        }

        SettingsScaffold(
            title = stringResource(R.string.setting),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.hd_player))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.pip_home),
                    summary = if (sdk < 26) stringResource(R.string.pip_info) else null,
                    checked = pip.value,
                    onCheckedChange = pip::set,
                )
            }
            if (sdk in 28..34) {
                item {
                    SettingSwitchRow(
                        title = stringResource(R.string.display_cutout),
                        checked = cutOut.value,
                        onCheckedChange = cutOut::set,
                    )
                }
            }
            if (fromWeb) {
                item {
                    SettingChoiceRow(
                        title = stringResource(R.string.trans_icon),
                        value = stringResource(if (iconLong.value) R.string.video_icon_long else R.string.video_icon_tap),
                        onClick = { choosing = 3 },
                    )
                }
            }
            if (sdk in 28..34) settingsFooter(getString(R.string.display_cutout_info))
            settingsHeader(getString(R.string.hd_drag))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.drag_bright),
                    checked = bright.value,
                    onCheckedChange = bright::set,
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.drag_volume),
                    checked = volume.value,
                    onCheckedChange = volume::set,
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.drag_seek),
                    checked = dragSeek.value,
                    onCheckedChange = dragSeek::set,
                )
            }
            // sensitivity only while seeking by dragging is on; a slider instead of the number dialog
            if (dragSeek.value) {
                item {
                    SettingSliderRow(
                        title = stringResource(R.string.swipe_sense),
                        value = sense.value,
                        range = 10..200,
                        label = { "$it%" },
                        onValueChange = sense::set,
                    )
                }
            }
            settingsFooter(
                getString(R.string.drag_bright_info) + "\n" + getString(R.string.drag_volume_info) + "\n" +
                    getString(R.string.drag_seek_info) + " " + getString(R.string.not_support_site)
            )
            settingsHeader(getString(R.string.hd_touch))
            item {
                SettingRow(
                    title = stringResource(R.string.double_tap),
                    summary = stringResource(R.string.not_support_site),
                    onClick = { startActivity(Intent(context, com.mycompany.app.ui.settings.VideoTapSettingsActivity::class.java)) },
                )
            }
            item { LockWithRow(ownLock) { own -> setOwnLock(context, 3, own); ownLock = SecretSettings.ownLock(3) } }
            if (ownLock) {
                item {
                    SettingRow(
                        title = stringResource(R.string.lock_type),
                        summary = stringResource(MainConst.lockNames[lockType]),
                        onClick = { choosing = 13 },
                    )
                }
            } else {
                item { AppLockRow() }
            }
            settingsFooter(getString(R.string.touch_lock_info))
        }

        if (choosing == 3) {
            ChoiceDialog(
                title = stringResource(R.string.trans_icon),
                options = listOf(stringResource(R.string.video_icon_tap), stringResource(R.string.video_icon_long)),
                selectedIndex = if (iconLong.value) 1 else 0,
                onSelect = { iconLong.set(it == 1) },
                onDismiss = { choosing = 0 },
            )
        }
        if (choosing == 13) {
            ChoiceDialog(
                title = stringResource(R.string.lock_type),
                options = MainConst.lockValues.map { stringResource(MainConst.lockNames[it]) },
                selectedIndex = MainConst.lockValues.indexOf(lockType),
                onSelect = { index ->
                    val type = MainConst.lockValues[index]
                    if (type != 4 && type != 0) {
                        // password / pattern types are set up on their own screen
                        val intent = MainUtil.lockIntent(context, type)
                        intent.putExtra("EXTRA_PASS", 3)
                        intent.putExtra("EXTRA_TYPE", 1)
                        lockResult.launch(intent)
                    } else if (SecretSettings.touchLockType.value != type && (type != 4 || MainUtil.canUseBiometric(context, true))) {
                        SecretSettings.editLocal { set(SecretSettings.touchLockType, type) }
                        SecretSettings.editLocal { set(SecretSettings.touchLockCode, "") }
                        PrefSecret.saveTouchLock(context)
                        lockType = type
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
                    pip.reload(VideoSettings.pipOnHome.value)
                    cutOut.reload(VideoSettings.cutOut.value)
                    iconLong.reload(PdfSettings.iconLong.value)
                    bright.reload(VideoSettings.dragBright.value)
                    volume.reload(VideoSettings.dragVolume.value)
                    dragSeek.reload(VideoSettings.dragSeek.value)
                    sense.reload(VideoSettings.seekSense.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /** Restores the defaults of this page: the changed keys are removed (default = no stored value). */
    private fun resetToDefaults(context: Context) {
        if (PdfSettings.iconLong.value) PdfSettings.reset(context, listOf(PdfSettings.iconLong))
        with(VideoSettings) {
            val page = listOf(pipOnHome, cutOut, dragBright, dragVolume, dragSeek, seekSense)
            val changedPage = page.filter { !it.isDefault }
            if (changedPage.isNotEmpty()) reset(context, changedPage)
            if (tapAreas.any { !it.isDefault }) reset(context, tapAreas)
        }
    }
}
