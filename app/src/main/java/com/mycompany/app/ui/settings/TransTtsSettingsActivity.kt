package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
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
 * Settings › Translate & read aloud. Only links since 2026-11-26: the translator ([TransSettingsActivity]) and text
 * to speech ([TtsSettingsActivity]). Reset restores both.
 */
class TransTtsSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.set_trans_tts),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            screenLinkRow(R.string.translator) { it.startActivity(Intent(it, TransSettingsActivity::class.java)) }
            screenLinkRow(R.string.tts_mode) { it.startActivity(Intent(it, TtsSettingsActivity::class.java)) }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    TransSettingsActivity.resetToDefaults(context)
                    TtsSettingsActivity.resetToDefaults(context)
                },
                onDismiss = { resetting = false },
            )
        }
    }
}
