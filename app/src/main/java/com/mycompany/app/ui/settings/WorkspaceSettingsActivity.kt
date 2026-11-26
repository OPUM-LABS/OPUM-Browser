package com.mycompany.app.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.Workspaces
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.browser.WORKSPACE_COLORS
import com.mycompany.app.ui.browser.WorkspaceDot
import com.mycompany.app.ui.browser.WorkspaceForm
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.OpumSheet
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.web.WebProfiles
import com.mycompany.app.web.WorkspaceData
import com.mycompany.app.ui.components.Sym

/**
 * Settings › Workspaces (2026-11-24): add, rename, colour, lock and delete workspaces. Opening one is done from the
 * browser's Workspaces menu action, which restarts the browser in it.
 */
class WorkspaceSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        // a locked workspace's lock must not be switched off (or the workspace deleted) without the lock
        var unlocked by remember { mutableStateOf(!needsLock()) }
        val lockLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
        ) { result -> if (result.resultCode == android.app.Activity.RESULT_OK) unlocked = true else finish() }
        androidx.compose.runtime.LaunchedEffect(Unit) {
            if (!unlocked) {
                lockLauncher.launch(com.mycompany.app.main.MainUtil.lockIntent(context, SecretSettings.lockType.value).putExtra("EXTRA_TYPE", 3))
            }
        }
        if (!unlocked) return
        // bumped after every change so the rows read the list again
        var tick by remember { mutableIntStateOf(0) }
        var adding by remember { mutableStateOf(false) }
        var editing by remember { mutableIntStateOf(-1) }
        var deleting by remember { mutableIntStateOf(-1) }
        var lockType by remember { mutableIntStateOf(SecretSettings.lockType.value) }
        OnScreenResume { lockType = SecretSettings.lockType.value; tick++ }
        // a workspace locked while the app has no lock yet: the lock is set up first (its type, then its PIN ...)
        var settingUp by remember { mutableIntStateOf(-1) }
        var choosingType by remember { mutableStateOf(false) }
        val lockAfterSetup = { number: Int ->
            lockType = SecretSettings.lockType.value
            if (lockType != 0 && number >= 0) {
                // set up for a workspace: the lock asks only for locked workspaces, not at every start
                SecretSettings.edit(context) { set(SecretSettings.lockSecret, true) }
                Workspaces.setLocked(context, number, true)
            }
            settingUp = -1
            tick++
        }
        val setupLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
        ) { result -> if (result.resultCode == android.app.Activity.RESULT_OK) lockAfterSetup(settingUp) else settingUp = -1 }

        @Suppress("UNUSED_VARIABLE") val read = tick
        val spaces = Workspaces.all()
        val current = Workspaces.currentNumber()
        val incognito = SyncSettings.secretMode.value

        SettingsScaffold(title = stringResource(R.string.workspaces), onBack = { finish() }) {
            item {
                SettingRow(
                    title = stringResource(R.string.space_main),
                    summary = stringResource(R.string.space_main_sum) +
                        if (current == Workspaces.MAIN && !incognito) " · " + stringResource(R.string.space_in_use) else "",
                    trailing = { WorkspaceDot(0) },
                )
            }
            for (space in spaces) {
                item {
                    val summary = listOfNotNull(
                        if (space.number == current && !incognito) stringResource(R.string.space_in_use) else null,
                        if (space.locked) stringResource(R.string.space_locked) else null,
                    ).joinToString(" · ")
                    SettingRow(
                        title = space.name,
                        summary = summary.ifEmpty { null },
                        onClick = { editing = space.number },
                        trailing = {
                            androidx.compose.foundation.layout.Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                if (space.locked) Icon(Sym.Lock, null, Modifier.padding(end = 8.dp))
                                WorkspaceDot(space.color)
                            }
                        },
                    )
                }
            }
            item {
                SettingRow(
                    title = stringResource(R.string.space_add),
                    iconRes = R.drawable.outline_add_black_24,
                    onClick = { adding = true },
                )
            }
            settingsFooter(getString(R.string.space_info) + "\n" + getString(R.string.space_open_info))
        }

        if (adding) {
            val base = stringResource(R.string.space_default_name)
            OpumSheet(onDismiss = { adding = false }, title = stringResource(R.string.space_new)) {
                WorkspaceForm(
                    initialName = Workspaces.freeName(base, spaces), initialColor = WORKSPACE_COLORS[1],
                    onSave = { name, color -> Workspaces.create(context, name, color); tick++; close() },
                    onCancel = { close() },
                )
            }
        }

        val edited = spaces.firstOrNull { it.number == editing }
        if (edited != null) {
            OpumSheet(onDismiss = { editing = -1 }, title = edited.name) {
                WorkspaceForm(
                    initialName = edited.name, initialColor = edited.color,
                    onSave = { name, color ->
                        Workspaces.rename(context, edited.number, name)
                        Workspaces.recolor(context, edited.number, color)
                        tick++
                        close()
                    },
                    onCancel = { close() },
                )
                SettingSwitchRow(
                    title = stringResource(R.string.space_lock),
                    summary = stringResource(if (lockType == 0) R.string.space_lock_setup else R.string.space_lock_sum),
                    checked = edited.locked && lockType != 0,
                    onCheckedChange = { on ->
                        if (on && lockType == 0) {
                            settingUp = edited.number
                            choosingType = true
                        } else {
                            Workspaces.setLocked(context, edited.number, on)
                            tick++
                        }
                    },
                )
                val inUse = edited.number == current && !incognito
                SettingRow(
                    title = stringResource(R.string.space_delete),
                    summary = if (inUse) stringResource(R.string.space_delete_current) else null,
                    enabled = !inUse,
                    onClick = { deleting = edited.number; close() },
                )
            }
        }

        if (choosingType) {
            val types = MainConst.lockValues.filter { it != 0 }
            ChoiceDialog(
                title = stringResource(R.string.app_lock),
                options = types.map { stringResource(MainConst.lockNames[MainConst.lockValues.indexOf(it)]) },
                selectedIndex = -1,
                onSelect = { index ->
                    val type = types[index]
                    if (type != 4) {
                        // password / pattern / PIN are set up on their own screen
                        setupLauncher.launch(MainUtil.lockIntent(context, type).putExtra("EXTRA_TYPE", 1))
                    } else if (MainUtil.canUseBiometric(context, true)) {
                        SecretSettings.editLocal { set(SecretSettings.lockType, type) }
                        SecretSettings.editLocal { set(SecretSettings.lockCode, "") }
                        PrefSecret.saveLock(context)
                        lockAfterSetup(settingUp)
                    } else {
                        settingUp = -1
                    }
                },
                onDismiss = { choosingType = false },
            )
        }

        val doomed = spaces.firstOrNull { it.number == deleting }
        if (doomed != null) {
            ConfirmDialog(
                message = getString(R.string.space_delete_ask, doomed.name),
                onConfirm = { delete(context, doomed.number); tick++ },
                onDismiss = { deleting = -1 },
            )
        }
    }

    /** Some workspace is locked and the app has a lock. */
    private fun needsLock(): Boolean = SecretSettings.lockType.value != 0 && Workspaces.all().any { it.locked }

    /** Takes the workspace off the list, then deletes its rows and its WebView profile. */
    private fun delete(context: Context, number: Int) {
        if (!Workspaces.remove(context, number)) return
        com.mycompany.app.web.SpaceCover.forget(context, number)
        val app = context.applicationContext
        MainApp.runInBackground(app, Runnable {
            WorkspaceData.deleteRows(app, number)
            // its WebView profile: now, or at the next start in normal mode
            MainApp.postToMain(app, Runnable { WebProfiles.dropDeleted(app) })
        })
    }
}

