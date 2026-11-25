package com.mycompany.app.ui.settings

import android.content.Context
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.MainSettings
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.rememberPref

/** What the address bar shows for [type] (0 domain, 1 address, 2 title), as a small pill under the choice. */
@Composable
private fun AddressLabelPreview(type: Int) {
    val text = when (type) {
        1 -> "https://en.wikipedia.org/wiki/Web_browser"
        2 -> "Web browser – Wikipedia"
        else -> "en.wikipedia.org"
    }
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp).height(40.dp)
            .clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.surfaceVariant).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(if (type == 2) R.drawable.outline_public_black_24 else R.drawable.outline_lock_black_24), contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp),
        )
        Text(
            text, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 8.dp),
        )
    }
}

/**
 * Toolbars & controls (was "Composition"; 2026-11-19 it lost the search rows to Start page & search and gained links to
 * the bar and menu editors and the gestures): toolbar, address bar, floating buttons, scrolling. Every
 * row opens a page of its own (2026-11-25: full screen, the address bar buttons and the back to top button were dialogs;
 * the one-handed mode button left for the floating buttons, which can do the same).
 */
class CompositionSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val path = intent.getStringExtra("EXTRA_PATH")
        val addrLabel = rememberPref({ PdfSettings.addrType.value }) { PdfSettings.edit(context) { set(PdfSettings.addrType, it) } }
        val pull = rememberPref({ ZoneSettings.pullRefresh.value }) { ZoneSettings.edit(context) { set(ZoneSettings.pullRefresh, it) } }
        val scrollPos = rememberPref({ ZoneSettings.scrollPos.value }) { ZoneSettings.edit(context) { set(ZoneSettings.scrollPos, it) } }
        val volScroll = rememberPref({ ZoneSettings.volScroll.value }) { ZoneSettings.edit(context) { set(ZoneSettings.volScroll, it) } }
        val scrollAnim = rememberPref({ ZtwoSettings.scrollAnim.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.scrollAnim, it) } }
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
            item {
                SettingRow(
                    title = stringResource(R.string.float_button),
                    onClick = { startActivity(Intent(context, FloatSettingsActivity::class.java)) },
                )
            }
            screenLinkRow(R.string.gesture) { it.startActivity(Intent(it, GestureSettingsActivity::class.java)) }
            settingsHeader(getString(R.string.address_bar))
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.address_label),
                    options = addressLabels.map { stringResource(it) },
                    selectedIndex = addrLabel.value,
                    onSelect = { addrLabel.set(it) },
                )
            }
            // what the address bar shows with the choice above
            item { AddressLabelPreview(addrLabel.value) }
            screenLinkRow(R.string.address_icon) { it.startActivity(Intent(it, AddrIconSettingsActivity::class.java)) }
            settingsHeader(getString(R.string.hd_scrolling))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.pull_refresh),
                    checked = pull.value,
                    onCheckedChange = pull::set,
                )
            }
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.show_scroll),
                    options = MainConst.scrollPosValues.map { stringResource(MainConst.scrollPosNames[it]) },
                    selectedIndex = MainConst.scrollPosValues.indexOf(scrollPos.value),
                    onSelect = { scrollPos.set(MainConst.scrollPosValues[it]) },
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.vol_scroll),
                    checked = volScroll.value,
                    onCheckedChange = volScroll::set,
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.use_scroll_anim),
                    checked = scrollAnim.value,
                    onCheckedChange = scrollAnim::set,
                )
            }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    addrLabel.reload(PdfSettings.addrType.value)
                    pull.reload(ZoneSettings.pullRefresh.value)
                    scrollPos.reload(ZoneSettings.scrollPos.value); volScroll.reload(ZoneSettings.volScroll.value); scrollAnim.reload(ZtwoSettings.scrollAnim.value)
                    tick++
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Labels of the address bar content (index = value). Used by the settings search index. */
        @JvmField
        val addressLabels: IntArray = intArrayOf(R.string.domain_name, R.string.url, R.string.title)

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
            if (PdfSettings.addrType.value != 0) PdfSettings.reset(context, listOf(PdfSettings.addrType))
            if (!MainSettings.addrItems.isDefault) MainSettings.reset(context, listOf(MainSettings.addrItems))
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
