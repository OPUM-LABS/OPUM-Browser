package com.mycompany.app.dialog

import android.content.Intent
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.AlbumSettings
import android.text.TextUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.data.book.DataBookTrans
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookTrans
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.dialog.DialogTransLang.TransLangListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.settings.TransSettingsActivity
import com.mycompany.app.web.WebViewActivity

/**
 * The translate sheet of the browser: detect mode, language, "always show the translate button", turning automatic
 * translation off for this site / this page, the blacklist, and the Translate button. Compose now (on
 * [ComposeBottomDialog]). Same constructor and [DialogApplyListener] (called by Translate); `WebViewActivity` calls [refreshState]
 * when it resumes. The blacklist opens as `SiteListActivity` type 29. The site / page
 * switches write the translation block list on a background thread and lock the sheet meanwhile, as before.
 */
class DialogSetTrans(
    webViewActivity: WebViewActivity?,
    pathParam: String?,
    dialogApplyListener: DialogApplyListener?,
) : ComposeBottomDialog(webViewActivity) {
    private var activity: WebViewActivity? = webViewActivity
    private var listener: DialogApplyListener? = dialogApplyListener
    private val path: String? = MainUtil.trimTrailingSlash(pathParam)
    private val site: String? = MainUtil.hostOfUrl(path!!, true)

    /** The sub-dialogs (old legacy dialogs, shown on top of this sheet). */
    @JvmField var langDialog: DialogTransLang? = null
    private var listOpen = false

    private var mode by mutableIntStateOf(AlbumSettings.transMode.value)
    private var language by mutableStateOf(AlbumSettings.transPick.value)
    private var iconAlways by mutableStateOf(AlbumSettings.transIcon.value)
    private var siteOff by mutableStateOf(false)
    private var pageOff by mutableStateOf(false)
    private var busy by mutableStateOf(false)

    override fun canCancelFromScrim(): Boolean = !busy

    private fun siteBlockedNow(): Boolean = try {
        val sites = DataBookTrans.instance(context).sites as ArrayList<*>?
        sites != null && sites.isNotEmpty() && !TextUtils.isEmpty(site) && sites.contains(site)
    } catch (_: Exception) {
        false
    }

    private fun pageBlockedNow(): Boolean = DataBookTrans.instance(context).hasSite(path)

    /** Re-reads what the settings sub-screens may have changed; [refresh] is passed on to the open blacklist dialog. */
    fun refreshState(refresh: Boolean) {
        if (activity == null) return
        mode = AlbumSettings.transMode.value
        language = AlbumSettings.transPick.value
        iconAlways = AlbumSettings.transIcon.value
        siteOff = siteBlockedNow()
        pageOff = pageBlockedNow()
    }

    /** Adds or removes [item] (a host or a page address) in the translation block list, off the main thread. */
    private fun setBlocked(item: String?, blocked: Boolean) {
        busy = true
        val app = context.applicationContext
        Thread {
            try {
                if (blocked) {
                    DataBookTrans.instance(app).addSite(item)
                    DbBookTrans.addPath(app, item)
                } else {
                    try {
                        val list = DataBookTrans.instance(app).sites as ArrayList<*>?
                        if (list != null && list.isNotEmpty() && !TextUtils.isEmpty(item)) (list as MutableList<*>).remove(item)
                    } catch (_: Exception) {
                    }
                    if (!TextUtils.isEmpty(item)) {
                        DbUtil.deleteWhere(DbBookTrans.get(app).writableDatabase, "DbBookTrans_table", "_path=?", arrayOf(item!!))
                    }
                }
            } catch (_: Exception) {
            }
            window?.decorView?.post { busy = false }
        }.start()
    }

    private fun openLanguage() {
        val host = activity ?: return
        if (langDialog != null) return
        val dialog = DialogTransLang(host, false, object : TransLangListener {
            override fun onLanguage(lang: String?) {
                langDialog?.dismiss()
                langDialog = null
                language = AlbumSettings.transPick.value
            }
        })
        langDialog = dialog
        dialog.setOnDismissListener {
            langDialog?.dismiss()
            langDialog = null
        }
    }

    /** The list now opens as a Compose screen, [listOpen] guards double opens. */
    private fun openBlacklist() {
        val host = activity ?: return
        if (langDialog != null || listOpen) return
        listOpen = true
        com.mycompany.app.ui.lists.SiteListActivity.open(host, 29, path) {
            listOpen = false
            if (activity != null) refreshState(false)
        }
    }

    init {
        mode = AlbumSettings.transMode.value
        siteOff = siteBlockedNow()
        pageOff = pageBlockedNow()
        setSheet {
            val dark = AppState.darkUi
            Box(Modifier.fillMaxWidth().padding(top = 8.dp, end = 8.dp)) {
                Image(
                    painterResource(if (dark) R.drawable.trans_logo_regular_white else R.drawable.trans_logo_regular_color),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).width(176.dp).height(40.dp),
                )
                IconButton(
                    enabled = !busy,
                    onClick = {
                        val host = activity ?: return@IconButton
                        val intent = Intent(host, TransSettingsActivity::class.java)
                        intent.putExtra("EXTRA_PATH", path)
                        host.startActivity(intent)
                    },
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) {
                    Icon(painterResource(R.drawable.outline_settings_black_20), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            HorizontalDivider()
            LazyColumn(Modifier.weight(1f, fill = false)) {
                item {
                    var menu by remember { mutableStateOf(false) }
                    Box {
                        SettingRow(
                            title = stringResource(R.string.trans_when),
                            summary = stringResource(TransSettingsActivity.modeLabels[mode]) +
                                if (TransSettingsActivity.modeInfos[mode] != 0) "\n" + stringResource(TransSettingsActivity.modeInfos[mode]) else "",
                            enabled = !busy,
                            onClick = { menu = true },
                        )
                        com.mycompany.app.ui.components.BubbleMenu(expanded = menu, onDismiss = { menu = false }) {
                            for (index in 0..2) {
                                val value = TransSettingsActivity.modeValues[index]
                                DropdownMenuItem(
                                    text = { Text(stringResource(TransSettingsActivity.modeLabels[value])) },
                                    onClick = {
                                        menu = false
                                        if (AlbumSettings.transMode.value != value) {
                                            AlbumSettings.edit(context) { set(AlbumSettings.transMode, value) }
                                            mode = value
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
                item { SettingChoiceRow(stringResource(R.string.locale), language ?: "", onClick = { if (!busy) openLanguage() }) }
                item {
                    SettingSwitchRow(
                        title = stringResource(R.string.trans_icon_always), checked = iconAlways, enabled = !busy,
                        onCheckedChange = {
                            AlbumSettings.edit(context) { set(AlbumSettings.transIcon, it) }
                            iconAlways = it
                        },
                    )
                }
                item { HorizontalDivider() }
                item {
                    SettingSwitchRow(
                        title = stringResource(R.string.trans_block_site), checked = siteOff, enabled = !busy,
                        onCheckedChange = { siteOff = it; setBlocked(site, it) },
                    )
                }
                item {
                    SettingSwitchRow(
                        title = stringResource(R.string.trans_block_page), checked = pageOff, enabled = !busy,
                        onCheckedChange = { pageOff = it; setBlocked(path, it) },
                    )
                }
                item { SettingRow(title = stringResource(R.string.trans_except), enabled = !busy, onClick = { openBlacklist() }) }
            }
            Button(
                onClick = { listener?.onApply() },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) { Text(stringResource(R.string.translate)) }
        }
        show()
    }

    override fun dismiss() {
        langDialog?.dismiss()
        langDialog = null
        activity = null
        listener = null
        super.dismiss()
    }
}
