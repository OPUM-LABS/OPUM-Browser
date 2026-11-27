package com.mycompany.app.ui.settings

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.InfoDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter

/**
 * Settings › Web filter › Blocked links and images (2026-11-25, was part of Web filter): the list of blocked links,
 * which opens behind its own lock screen, that lock, and the list of blocked images. No reset: the lock is changed
 * only by confirming it.
 */
class BlockedSettingsActivity : WebFilterActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        var linkLock by remember { mutableIntStateOf(SecretSettings.linkLockType.value) }
        // the blocked links are guarded by the app lock or a lock of their own (SecretSettings.guardType, 2026-11-27)
        var ownLock by remember { mutableStateOf(SecretSettings.ownLock(2)) }
        var launchPurpose by remember { mutableIntStateOf(0) } // 1 open blocked links, 2 set lock, 3 clear lock
        var choosing by remember { mutableStateOf(false) }
        var lockGuide by remember { mutableStateOf(false) }

        val lockResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode != RESULT_OK) { launchPurpose = 0; return@rememberLauncherForActivityResult }
            when (launchPurpose) {
                1 -> if (SecretSettings.guardType(2) != 0) openSiteList(22)
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

        SettingsScaffold(
            title = stringResource(R.string.wf_blocked_page),
            onBack = { finish() },
        ) {
            item {
                SettingRow(
                    title = stringResource(R.string.blocked_link),
                    onClick = {
                        if (SecretSettings.guardType(2) != 0) {
                            launchPurpose = 1
                            lockResult.launch(
                                MainUtil.lockIntent(context, SecretSettings.guardType(2)).putExtra("EXTRA_PASS", SecretSettings.guardPass(2)).putExtra("EXTRA_TYPE", 2)
                            )
                        } else {
                            lockGuide = true
                        }
                    },
                )
            }
            item { LockWithRow(ownLock) { own -> setOwnLock(context, 2, own); ownLock = SecretSettings.ownLock(2) } }
            if (ownLock) {
                item {
                    SettingChoiceRow(
                        title = stringResource(R.string.lock_type),
                        value = stringResource(MainConst.lockNames[linkLock]),
                        onClick = { choosing = true },
                    )
                }
            } else {
                item { AppLockRow() }
            }
            settingsFooter(getString(R.string.wf_lock_info))
            item { SettingRow(title = stringResource(R.string.blocked_image), onClick = { openSiteList(23) }) }
        }

        if (choosing) {
            ChoiceDialog(
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
                onDismiss = { choosing = false },
            )
        }
        if (lockGuide) {
            InfoDialog(
                title = stringResource(R.string.blocked_link),
                message = stringResource(R.string.password_lock_1),
                onDismiss = { lockGuide = false },
            )
        }
    }
}
