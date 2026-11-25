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
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingsScaffold

/**
 * Settings › Tabs. Only links since 2026-11-25: the tab bar and the tab button (with the phone preview), opening
 * tabs, closing tabs. Reset restores all of them.
 */
class TabSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.tab_item),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            screenLinkRow(R.string.tab_bar_page) { it.startActivity(Intent(it, TabBarSettingsActivity::class.java)) }
            screenLinkRow(R.string.tab_open_page) { it.startActivity(Intent(it, TabOpenSettingsActivity::class.java)) }
            screenLinkRow(R.string.tab_close_page) { it.startActivity(Intent(it, TabCloseSettingsActivity::class.java)) }
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
        /** Restores the defaults of the tab pages. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            TabBarSettingsActivity.resetToDefaults(context)
            TabOpenSettingsActivity.resetToDefaults(context)
            TabCloseSettingsActivity.resetToDefaults(context)
            // the tab overview grid's look (set in the overview itself)
            val color = MainConst.gradientColors[5]
            val pos = MainConst.presetPositions[5]
            if (EditorSettings.gridAlpha.value != 0 || EditorSettings.gridColor.value != color || EditorSettings.gridPos.value.compareTo(pos) != 0) {
                EditorSettings.editLocal { set(EditorSettings.gridAlpha, 0) }; EditorSettings.editLocal { set(EditorSettings.gridColor, color) }; EditorSettings.editLocal { set(EditorSettings.gridPos, pos) }; PrefEditor.gridArgb = PrefEditor.withAlpha(color, 0)
                val p = PrefEditor.get(context)
                p!!.removeKey("mGridAlpha"); p.removeKey("mGridColor"); p.removeKey("mGridPos"); p.save()
            }
        }
    }
}
