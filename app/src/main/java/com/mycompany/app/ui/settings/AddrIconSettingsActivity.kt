package com.mycompany.app.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingsScaffold

/**
 * Settings › Toolbars & controls › Search bar button (2026-11-25, was a dialog): the buttons inside the address bar,
 * edited with [AddrIconEditor]; every change is stored at once.
 */
class AddrIconSettingsActivity : OpumComposeActivity() {
    private var editor: AddrIconEditor? = null

    @Composable
    override fun Content() {
        val shown = remember { editor ?: AddrIconEditor(this).also { editor = it } }
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.address_icon),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            item { shown.Content() }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = { shown.reset() },
                onDismiss = { resetting = false },
            )
        }
    }

    override fun onDestroy() {
        editor?.release()
        editor = null
        super.onDestroy()
    }
}
