package com.mycompany.app.ui.settings

import android.content.Context
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.MainSettings
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogEditIcon
import com.mycompany.app.dialog.DialogEditUp
import com.mycompany.app.dialog.DialogSeekSimple
import com.mycompany.app.dialog.DialogSetAddr
import com.mycompany.app.dialog.DialogSetFull
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Toolbars & controls (was "Composition"; 2026-11-19 it lost the search rows to Start page & search and gained links to
 * the bar and menu editors and the gestures): toolbar, address bar, floating and one-hand buttons, scrolling. The dialogs that
 * only accept the legacy activity type go through `DialogShow.direct`; [DialogSetAddr] and [DialogSeekSimple]
 * take a plain Activity and are shown directly. Land mode and the one-hand area are still legacy screens.
 */
class CompositionSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val path = intent.getStringExtra("EXTRA_PATH")
        val addrLabel = rememberPref({ PdfSettings.addrType.value }) { PdfSettings.edit(context) { set(PdfSettings.addrType, it) } }
        val handIcon = rememberPref({ ZtriSettings.handIcon.value }) { ZtriSettings.edit(context) { set(ZtriSettings.handIcon, it) } }
        val pull = rememberPref({ ZoneSettings.pullRefresh.value }) { ZoneSettings.edit(context) { set(ZoneSettings.pullRefresh, it) } }
        val scrollPos = rememberPref({ ZoneSettings.scrollPos.value }) { ZoneSettings.edit(context) { set(ZoneSettings.scrollPos, it) } }
        val volScroll = rememberPref({ ZoneSettings.volScroll.value }) { ZoneSettings.edit(context) { set(ZoneSettings.volScroll, it) } }
        val scrollAnim = rememberPref({ ZtwoSettings.scrollAnim.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.scrollAnim, it) } }
        var handColor by remember { mutableIntStateOf(PrefEditor.withAlpha(EditorSettings.handColor.value, EditorSettings.handAlpha.value)) }
        var tick by remember { mutableIntStateOf(0) }
        var resetting by remember { mutableStateOf(false) }

        OnScreenResume {
            handColor = PrefEditor.withAlpha(EditorSettings.handColor.value, EditorSettings.handAlpha.value)
            tick++
        }

        SettingsScaffold(
            title = stringResource(R.string.set_toolbars),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.hd_bars))
            // the bar and menu editors, which had their own entries on the settings start page
            screenLinkRow(R.string.layout_title) { it.startActivity(Intent(it, LayoutSettingsActivity::class.java)) }
            screenLinkRow(R.string.list_menu) { it.startActivity(Intent(it, MenuSettingsActivity::class.java)) }
            item {
                SettingRow(
                    title = stringResource(R.string.full_tool),
                    onClick = {
                        com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host -> DialogSetFull(host, null) }
                    },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.land_mode),
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
            item {
                SettingRow(
                    title = stringResource(R.string.address_icon),
                    onClick = {
                        val dialog = DialogSetAddr(this@CompositionSettingsActivity)
                        dialog.setOnDismissListener { tick++ }
                    },
                )
            }
            settingsHeader(getString(R.string.hd_one_hand))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.onehand_icon),
                    checked = handIcon.value,
                    onCheckedChange = handIcon::set,
                )
            }
            if (handIcon.value) {
                item {
                    SettingRow(
                        title = stringResource(R.string.icon_color),
                        onClick = { com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host -> DialogEditIcon(host, 8, null) } },
                        trailing = {
                            Box(
                                Modifier
                                    .size(28.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(Color(handColor))
                                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                            )
                        },
                    )
                }
                item {
                    SettingRow(
                        title = stringResource(R.string.onehand_area),
                        onClick = { startActivity(Intent(context, com.mycompany.app.ui.settings.HandAreaSettingsActivity::class.java)) },
                    )
                }
            }
            settingsFooter(getString(R.string.drag_move_guide))
            settingsHeader(getString(R.string.hd_scrolling))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.pull_refresh),
                    checked = pull.value,
                    onCheckedChange = pull::set,
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.show_up),
                    onClick = { com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host -> DialogEditUp(host) } },
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
                    handIcon.reload(ZtriSettings.handIcon.value); pull.reload(ZoneSettings.pullRefresh.value)
                    scrollPos.reload(ZoneSettings.scrollPos.value); volScroll.reload(ZoneSettings.volScroll.value); scrollAnim.reload(ZtwoSettings.scrollAnim.value)
                    handColor = PrefEditor.withAlpha(EditorSettings.handColor.value, EditorSettings.handAlpha.value)
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
            val color = MainConst.gradientColors[5]
            val pos = MainConst.presetPositions[5]
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
            if (ZtriSettings.handIcon.value) ZtriSettings.reset(context, listOf(ZtriSettings.handIcon))
            if (EditorSettings.handAlpha.value != 0 || EditorSettings.handColor.value != color || EditorSettings.handPos.value.compareTo(pos) != 0) {
                EditorSettings.editLocal { set(EditorSettings.handAlpha, 0) }; EditorSettings.editLocal { set(EditorSettings.handColor, color) }; EditorSettings.editLocal { set(EditorSettings.handPos, pos) }; PrefEditor.handArgb = PrefEditor.withAlpha(color, 0)
                val p = PrefEditor.get(context)
                p!!.removeKey("mHandAlpha"); p.removeKey("mHandColor"); p.removeKey("mHandPos"); p.save()
            }
            if (TtsSettings.handTop.value != 60) TtsSettings.reset(context, listOf(TtsSettings.handTop))
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
