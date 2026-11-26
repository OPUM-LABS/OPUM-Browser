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
 * Settings › Start page & search. Only links since 2026-11-26: the start pages ([StartPagesSettingsActivity]: home
 * page, start page, new tab) and search ([SearchSettingsActivity]: engine, suggestions, typing, recent searches).
 * Reset restores both.
 */
class StartSearchSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val path = intent.getStringExtra("EXTRA_PATH")
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.set_start_search),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            screenLinkRow(R.string.hd_start_pages, getString(R.string.start_pages_sum)) {
                it.startActivity(Intent(it, StartPagesSettingsActivity::class.java).putExtra("EXTRA_PATH", path))
            }
            screenLinkRow(R.string.hd_search, getString(R.string.search_page_sum)) {
                it.startActivity(Intent(it, SearchSettingsActivity::class.java))
            }
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
        /** Restores the defaults of both pages. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            StartPagesSettingsActivity.resetToDefaults(context)
            SearchSettingsActivity.resetToDefaults(context)
        }
    }
}
