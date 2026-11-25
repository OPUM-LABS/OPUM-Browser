package com.mycompany.app.ui.settings

import android.content.Context
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.prefs.OnScreenResume

/**
 * Toolbars & controls (was "Composition"; 2026-11-19 it lost the search rows to Start page & search and gained links to
 * the bar and menu editors and the gestures): toolbar, address bar, gestures. Only links: every
 * row opens a page of its own (2026-11-25: full screen, the address bar buttons and the back to top button were dialogs;
 * the one-handed mode button left for the floating buttons, which can do the same). The bars, scrolling and the
 * floating buttons are on [FullScreenSettingsActivity] with the preview, the address bar on [AddressBarSettingsActivity].
 * Reset restores all of them.
 */
class CompositionSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val path = intent.getStringExtra("EXTRA_PATH")
        var tick by remember { mutableIntStateOf(0) }
        var resetting by remember { mutableStateOf(false) }

        OnScreenResume { tick++ }

        SettingsScaffold(
            title = stringResource(R.string.set_toolbars),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.hd_bars))
            // the bar and menu editors, which had their own entries on the settings start page
            screenLinkRow(R.string.layout_title) { it.startActivity(Intent(it, LayoutSettingsActivity::class.java)) }
            screenLinkRow(R.string.list_menu) { it.startActivity(Intent(it, MenuSettingsActivity::class.java)) }
            screenLinkRow(R.string.full_tool) { it.startActivity(Intent(it, FullScreenSettingsActivity::class.java)) }
            screenLinkRow(R.string.address_bar) { it.startActivity(Intent(it, AddressBarSettingsActivity::class.java)) }
            item {
                SettingRow(
                    title = stringResource(R.string.land_mode),
                    summary = stringResource(R.string.land_mode_sum),
                    onClick = {
                        startActivity(Intent(context, com.mycompany.app.ui.settings.LandSettingsActivity::class.java).putExtra("EXTRA_PATH", path))
                    },
                )
            }
            settingsHeader(getString(R.string.hd_buttons))
            screenLinkRow(R.string.gesture) { it.startActivity(Intent(it, GestureSettingsActivity::class.java)) }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    tick++
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Restores the defaults of this page, including the floating buttons (suggestions and recent searches: [StartSearchSettingsActivity]). */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (!BrowserSettings.showStatus.value || !BrowserSettings.showNavi.value || BrowserSettings.fixTop.value || BrowserSettings.fixBot.value) {
                BrowserSettings.editLocal { set(BrowserSettings.showStatus, true) }; BrowserSettings.editLocal { set(BrowserSettings.showNavi, true) }; BrowserSettings.editLocal { set(BrowserSettings.fixTop, false) }; BrowserSettings.editLocal { set(BrowserSettings.fixBot, false) }
                val p = PrefWeb.get(context, false)
                p!!.removeKey("mShowStatus"); p.removeKey("mShowNavi"); p.removeKey("mFixTop"); p.removeKey("mFixBot"); p.save()
            }
            if (TtsSettings.webLand.value != 0 || TtsSettings.webLand2.value != 0) {
                TtsSettings.editLocal { set(TtsSettings.webLand, 0) }; TtsSettings.editLocal { set(TtsSettings.webLand2, 0) }
                val p = PrefTts.get(context, false)
                p!!.removeKey("mWebLand1"); p.removeKey("mWebLand2"); p.save()
            }
            AddressBarSettingsActivity.resetToDefaults(context)
            if (!ZoneSettings.pullRefresh.value) ZoneSettings.reset(context, listOf(ZoneSettings.pullRefresh))
            if (ZoneSettings.showUpPos.value != 2) ZoneSettings.reset(context, listOf(ZoneSettings.showUpPos))
            if (EditorSettings.upAlpha.value != 25) EditorSettings.reset(context, listOf(EditorSettings.upAlpha))
            val scroll = if (MainUtil.isRtl(context)) 1 else 3
            if (ZoneSettings.scrollPos.value != scroll) { ZoneSettings.editLocal { set(ZoneSettings.scrollPos, scroll) }; PrefSet.removeNow(context, 15, "mScrollPos") }
            if (ZoneSettings.volScroll.value) ZoneSettings.reset(context, listOf(ZoneSettings.volScroll))
            if (ZtwoSettings.scrollAnim.value) ZtwoSettings.reset(context, listOf(ZtwoSettings.scrollAnim))
            FloatSettingsActivity.resetToDefaults(context)
        }
    }
}
