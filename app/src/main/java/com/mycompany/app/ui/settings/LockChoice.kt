package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.ui.components.EXTRA_FOCUS
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.prefs.OnScreenResume

/*
 * One lock or several (2026-11-27): the passwords (1), the blocked links (2) and the video touch lock (3) are guarded
 * by the app lock unless they are given a lock of their own (SecretSettings.ownLock / guardType). These rows show and
 * change that choice; the app lock itself is set on the Security page, reached from here and from Workspaces.
 */

/** "Lock with: the app lock / its own lock" for [pass]; [own] is the current choice, [onChange] gets the new one. */
@Composable
fun LockWithRow(own: Boolean, onChange: (Boolean) -> Unit) {
    SettingSegmentedRow(
        title = stringResource(R.string.lock_with),
        options = listOf(stringResource(R.string.lock_with_app), stringResource(R.string.lock_with_own)),
        selectedIndex = if (own) 1 else 0,
        onSelect = { onChange(it == 1) },
    )
}

/** Stores [pass]'s choice: its own lock ([own]) or the app lock. */
fun setOwnLock(context: Context, pass: Int, own: Boolean) {
    SecretSettings.edit(context) { set(SecretSettings.modeSetting(pass), if (own) 1 else 0) }
}

/** "App lock" with its type (or "Not used"); opens the Security page at the app lock to set or change it. */
@Composable
fun AppLockRow() {
    val context = LocalContext.current
    var lockType by remember { mutableIntStateOf(SecretSettings.lockType.value) }
    OnScreenResume { lockType = SecretSettings.lockType.value }
    val title = stringResource(R.string.app_lock)
    SettingChoiceRow(
        title = title,
        value = stringResource(MainConst.lockNames[lockType.coerceIn(0, MainConst.lockNames.size - 1)]),
        onClick = {
            context.startActivity(Intent(context, SecureSettingsActivity::class.java).putExtra(EXTRA_FOCUS, title))
        },
    )
}
