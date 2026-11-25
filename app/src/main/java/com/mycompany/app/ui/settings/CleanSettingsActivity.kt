package com.mycompany.app.ui.settings

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingsScaffold

/**
 * Settings › Web filter. Only links since 2026-11-25: ads and cookie banners, pop-ups and overlays, blocked links and
 * images. The browser opens this page (or one of the three) for a result; a filter change on a page opened from here
 * is passed on ([WebFilterActivity]). Reset restores all three pages.
 */
class CleanSettingsActivity : WebFilterActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        var resetting by remember { mutableStateOf(false) }
        val pageResult = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { takeResult(it.data) }

        SettingsScaffold(
            title = stringResource(R.string.clean_mode),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            item { SettingRow(title = stringResource(R.string.wf_ads_page), onClick = { pageResult.launch(pageIntent(AdsSettingsActivity::class.java)) }) }
            item { SettingRow(title = stringResource(R.string.wf_pop_page), onClick = { pageResult.launch(pageIntent(PopupSettingsActivity::class.java)) }) }
            item { SettingRow(title = stringResource(R.string.wf_blocked_page), onClick = { pageResult.launch(pageIntent(BlockedSettingsActivity::class.java)) }) }
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

        /** Restores the defaults of the Web filter pages. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            AdsSettingsActivity.resetToDefaults(context)
            PopupSettingsActivity.resetToDefaults(context)
        }
    }
}
