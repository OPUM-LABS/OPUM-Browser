package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.book.DbBookAgent
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Settings › Web pages › Links and identity (2026-11-25, was part of Web pages): opening links in apps, what a long
 * press on a link or an image does (many actions, so a dialog each), and the user agent (its own list screen).
 */
class WebLinksSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val linkApp = rememberPref({ AlbumSettings.linkApp.value }) { AlbumSettings.edit(context) { set(AlbumSettings.linkApp, it) } }
        val openLink = rememberPref({ ZoneSettings.openLink.value }) { ZoneSettings.edit(context) { set(ZoneSettings.openLink, it) } }
        val openImage = rememberPref({ ZoneSettings.openImage.value }) { ZoneSettings.edit(context) { set(ZoneSettings.openImage, it) } }
        // also on the menu's Desktop sheet; on by default on tablets (2026-11-27)
        val deskAll = rememberPref({ BrowserSettings.deskLock.value }) { BrowserSettings.edit(context) { set(BrowserSettings.deskLock, it) } }
        var tick by remember { mutableIntStateOf(0) }
        var choosing by remember { mutableIntStateOf(0) } // 1 link, 2 image
        var resetting by remember { mutableStateOf(false) }
        // the user agent list and the browser menus change these
        OnScreenResume { tick++; deskAll.reload(BrowserSettings.deskLock.value); openLink.reload(ZoneSettings.openLink.value); openImage.reload(ZoneSettings.openImage.value) }

        fun agentName(): String? =
            if (ZtwoSettings.agentType.value < 4) MainConst.agentNames[ZtwoSettings.agentType.value]
            else DbBookAgent.titleById(context, (ZtwoSettings.agentType.value - 100).toLong())

        SettingsScaffold(
            title = stringResource(R.string.hd_links),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            item { SettingSwitchRow(title = stringResource(R.string.link_app), checked = linkApp.value, onCheckedChange = linkApp::set) }
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.long_press_link),
                    value = stringResource(MainConst.linkMenuNames[openLink.value]),
                    onClick = { choosing = 1 },
                )
            }
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.long_press_image),
                    value = stringResource(MainConst.imageMenuNames[openImage.value]),
                    onClick = { choosing = 2 },
                )
            }
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.user_agent),
                    value = "$tick".let { agentName() ?: "" },
                    onClick = { context.startActivity(Intent(context, com.mycompany.app.ui.lists.UserAgentListActivity::class.java)) },
                )
            }
            item { SettingSwitchRow(title = stringResource(R.string.desk_all), checked = deskAll.value, onCheckedChange = deskAll::set) }
        }

        when (choosing) {
            1 -> ChoiceDialog(
                title = stringResource(R.string.long_press_link),
                options = MainConst.linkMenuValues.map { stringResource(MainConst.linkMenuNames[it]) },
                selectedIndex = MainConst.linkMenuValues.indexOf(openLink.value),
                onSelect = { openLink.set(MainConst.linkMenuValues[it]) },
                onDismiss = { choosing = 0 },
            )
            2 -> ChoiceDialog(
                title = stringResource(R.string.long_press_image),
                options = MainConst.imageMenuValues.map { stringResource(MainConst.imageMenuNames[it]) },
                selectedIndex = MainConst.imageMenuValues.indexOf(openImage.value),
                onSelect = { openImage.set(MainConst.imageMenuValues[it]) },
                onDismiss = { choosing = 0 },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    linkApp.reload(AlbumSettings.linkApp.value); deskAll.reload(BrowserSettings.deskLock.value); openLink.reload(ZoneSettings.openLink.value); openImage.reload(ZoneSettings.openImage.value)
                    tick++
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (ZtwoSettings.agentType.value != 0 || !TextUtils.isEmpty(ZtwoSettings.agentUser.value)) {
                ZtwoSettings.editLocal { set(ZtwoSettings.agentType, 0) }; ZtwoSettings.editLocal { set(ZtwoSettings.agentUser, "") }
                val p = PrefZtwo.get(context, false)
                p!!.removeKey("mAgentType"); p.removeKey("mAgentUser"); p.save()
            }
            if (AlbumSettings.linkApp.value) AlbumSettings.reset(context, listOf(AlbumSettings.linkApp))
            if (ZoneSettings.openLink.value != 0) ZoneSettings.reset(context, listOf(ZoneSettings.openLink))
            if (ZoneSettings.openImage.value != 0) ZoneSettings.reset(context, listOf(ZoneSettings.openImage))
            if (!BrowserSettings.deskLock.isDefault) BrowserSettings.reset(context, listOf(BrowserSettings.deskLock))
        }
    }
}
