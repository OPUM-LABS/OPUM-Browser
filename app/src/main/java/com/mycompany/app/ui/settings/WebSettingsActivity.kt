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
 * Settings › Web pages (was "Web content"). Only links since 2026-11-25: content and reading, links and identity,
 * user scripts and developer. Reset restores all three pages.
 */
class WebSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.set_web_pages),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            screenLinkRow(R.string.web_content_page) { it.startActivity(Intent(it, WebContentSettingsActivity::class.java)) }
            screenLinkRow(R.string.hd_links) { it.startActivity(Intent(it, WebLinksSettingsActivity::class.java)) }
            screenLinkRow(R.string.web_scripts_page) { it.startActivity(Intent(it, WebScriptSettingsActivity::class.java)) }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = { resetToDefaults(context) },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Two-finger zoom values in menu order, and the label of each value (index = value). */
        @JvmField
        val scaleValues: IntArray = intArrayOf(1, 2, 0)

        @JvmField
        val scaleLabels: IntArray = intArrayOf(R.string.not_used, R.string.web_page, R.string.only_text)

        /** Restores the defaults of the Web pages pages. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            WebContentSettingsActivity.resetToDefaults(context)
            WebLinksSettingsActivity.resetToDefaults(context)
            WebScriptSettingsActivity.resetToDefaults(context)
        }
    }
}
