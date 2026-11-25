package com.mycompany.app.dialog

import com.mycompany.app.ui.components.AppListItem
import com.mycompany.app.main.RequestCode
import android.content.DialogInterface
import com.mycompany.app.pref.store.BrowserSettings
import android.content.Intent
import android.text.TextUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.data.book.DataBookPop
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookPop
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.ui.settings.CleanSettingsActivity
import com.mycompany.app.dialog.DialogSetAdblock.DialogAdsListener
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.RadioButton
import androidx.compose.foundation.layout.Box
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.browser.ComposeBottomDialog
import java.lang.ref.WeakReference

/**
 * Popup settings for the current page ([path] = page URL; [pageUrl] = normalised URL, [pageHost] = its host): the blocking mode
 * (`BrowserSettings.popBlock.value`, one of `CleanSettingsActivity.popValues`, picked from a menu), "allow popups on this site" / "on this
 * page" (stored by [DialogTask] in the background, `DataBookPop` + `DbBookPop`), the allow list
 * (`SiteListActivity` type 21) and a settings icon (Web Filter settings, index 11, request 35). On dismiss the listener
 * hears `(false, ad-block changed, m0, !n0, false, null)` as before; [listChanged] / [settingsKept] are set by `WebViewActivity`. Compose
 * now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogAllowPopup(
    webViewActivity: WebViewActivity?,
    path: String?,
    dialogAdsListener: DialogAdsListener?,
) : ComposeBottomDialog(webViewActivity) {
    @JvmField
    var hostActivity: WebViewActivity? = webViewActivity
    @JvmField
    var appContext: android.content.Context? = context
    @JvmField
    var adsListener: DialogAdsListener? = dialogAdsListener
    @JvmField
    var pageUrl: String? = MainUtil.trimTrailingSlash(path)
    @JvmField
    var pageHost: String? = MainUtil.hostOfUrl(pageUrl!!, true)
    @JvmField
    var blockTask: DialogTask? = null
    private var listOpen = false
    @JvmField
    var adsBlockStart: Boolean = false
    @JvmField
    var listChanged: Boolean = false
    @JvmField
    var settingsKept: Boolean = false

    /** Blocking mode, popups allowed on this site, on this page. */
    private var mode by mutableIntStateOf(BrowserSettings.popBlock.value)
    private var modeMenu by mutableStateOf(false)
    private var siteAllowed by mutableStateOf(false)
    private var pageAllowed by mutableStateOf(false)
    internal var busy by mutableStateOf(false)

    class DialogTask(dialogAllowPopup: DialogAllowPopup?, path: String?, isAdd: Boolean) :
        MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var path: String? = null
        @JvmField
        var isAdd: Boolean = false

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogAllowPopup)
            this.dialogRef = weakReference
            val dialogAllowPopup = weakReference.get() as DialogAllowPopup?
            if (dialogAllowPopup != null) {
                this.path = path
                this.isAdd = isAdd
                val i: Int = 0
                dialogAllowPopup.busy = true
            }
        
        }
    }

        public override fun doInBackground() {
            val dialogAllowPopup = this.dialogRef!!.get() as DialogAllowPopup?
            if (dialogAllowPopup == null || this.cancelled) {
                return
            }
            val context = dialogAllowPopup.appContext
            val path = this.path
            if (this.isAdd) {
                DataBookPop.instance(context).addSite(path)
                DbBookPop.addPath(dialogAllowPopup.appContext, path)
                return
            }
            val dataBookPopL = DataBookPop.instance(context)
            dataBookPopL.javaClass
            try {
                val arrayList: ArrayList<String?>? = dataBookPopL.sites
                if (arrayList != null && !arrayList.isEmpty() && !TextUtils.isEmpty(path)) {
                    dataBookPopL.sites!!.remove(path)
                }
            } catch (unused: Exception) {
            }
            val appContext = dialogAllowPopup.appContext
            val dbBookPop = DbBookPop.cached
            if (appContext == null || TextUtils.isEmpty(path)) {
                return
            }
            DbUtil.deleteWhere(
                DbBookPop.get(appContext).getWritableDatabase(),
                "DbBookPop_table",
                "_path=?",
                arrayOf<String?>(path!!)
            )
        }

        public override fun onCancelled() {
            val dialogAllowPopup = this.dialogRef!!.get() as DialogAllowPopup?
            if (dialogAllowPopup != null) {
                dialogAllowPopup.blockTask = null
                dialogAllowPopup.busy = false
            }
        }

        public override fun onPostExecute() {
            val dialogAllowPopup = this.dialogRef!!.get() as DialogAllowPopup?
            if (dialogAllowPopup != null) {
                dialogAllowPopup.blockTask = null
                dialogAllowPopup.busy = false
            }
        }
    }

    init {
        siteAllowed = readSiteAllowed()
        pageAllowed = DataBookPop.instance(appContext).hasPage(pageUrl)
        adsBlockStart = BrowserSettings.adsBlock.value
        setSheet {
            // like the old sheet: only the settings icon on top
            Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp, top = 8.dp)) {
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { openSettings() }) {
                    Icon(painterResource(AppR.drawable.outline_settings_black_20), contentDescription = null)
                }
            }
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                Box {
                    AppListItem(
                        headlineContent = { Text(stringResource(AppR.string.pop_block)) },
                        supportingContent = {
                            val info = CleanSettingsActivity.popInfos[mode]
                            Text(stringResource(CleanSettingsActivity.popLabels[mode]) + if (info != 0) "\n" + stringResource(info) else "")
                        },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        modifier = Modifier.clickable { modeMenu = true },
                    )
                    com.mycompany.app.ui.components.BubbleMenu(expanded = modeMenu, onDismiss = { modeMenu = false }) {
                        CleanSettingsActivity.popValues.forEach { v ->
                            DropdownMenuItem(
                                text = { Text(stringResource(CleanSettingsActivity.popLabels[v])) },
                                trailingIcon = { RadioButton(selected = mode == v, onClick = null) },
                                onClick = { modeMenu = false; changeMode(v) },
                            )
                        }
                    }
                }
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                SwitchRow(stringResource(AppR.string.pop_allow_site), pageHost, siteAllowed, !busy) { setBlock(true, it) }
                SwitchRow(stringResource(AppR.string.pop_allow_page), null, pageAllowed, !busy) { setBlock(false, it) }
                AppListItem(
                    headlineContent = { Text(stringResource(AppR.string.pop_white)) },
                    trailingContent = { Icon(painterResource(AppR.drawable.outline_chevron_right_white_24), contentDescription = null) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.clickable { openList() },
                )
            }
            Spacer(Modifier.padding(bottom = 16.dp))
        }
        show()
    }

    @Composable
    private fun SwitchRow(title: String, info: String?, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
        AppListItem(
            headlineContent = { Text(title) },
            supportingContent = info?.let { { Text(it) } },
            trailingContent = { Switch(checked = checked, onCheckedChange = onChange, enabled = enabled) },
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.clickable(enabled = enabled) { onChange(!checked) },
        )
    }

    private fun readSiteAllowed(): Boolean = siteAllowed(this)


    private fun changeMode(newMode: Int) {
        if (mode == newMode) return
        mode = newMode
        BrowserSettings.edit(appContext) { set(BrowserSettings.popBlock, newMode) }
    }

    /** Site ([site] = true, key [pageHost]) or page ([pageUrl]) allow switch: stored in the background. */
    private fun setBlock(site: Boolean, on: Boolean) {
        if (site) siteAllowed = on else pageAllowed = on
        blockTask?.cancelled = true
        blockTask = null
        val task = DialogTask(this, if (site) pageHost else pageUrl, on)
        blockTask = task
        task.start(appContext)
    }

    private fun openSettings() {
        val activity = hostActivity ?: return
        val intent = Intent(appContext, com.mycompany.app.ui.settings.PopupSettingsActivity::class.java)
        intent.putExtra("EXTRA_POPUP", true)
        intent.putExtra("EXTRA_NOTI", true)
        intent.putExtra("EXTRA_INDEX", 11)
        intent.putExtra("EXTRA_PATH", pageUrl)
        activity.launchExternalForResult(intent, RequestCode.DIALOG_SETTINGS)
    }

    /** The list now opens as a Compose screen, [listOpen] guards double opens. */
    private fun openList() {
        val host = hostActivity ?: return
        if (hostActivity == null || listOpen) return
        listOpen = true
        com.mycompany.app.ui.lists.SiteListActivity.open(host, 21, pageUrl) {
            listOpen = false
            if (hostActivity != null) refreshState(false)
        }
    }

    /** Re-reads the switches (after the exception list or the settings screen changed them). */
    fun refreshState(refresh: Boolean) {
        if (mode != BrowserSettings.popBlock.value) mode = BrowserSettings.popBlock.value
        val site = readSiteAllowed()
        if (siteAllowed != site) siteAllowed = site
        val page = DataBookPop.instance(appContext).hasPage(pageUrl)
        if (pageAllowed != page) pageAllowed = page
    }

    override fun canCancelFromScrim(): Boolean = !busy

    override fun dismiss() {
        blockTask?.cancelled = true
        blockTask = null
        val listener = adsListener
        adsListener = null
        listener?.onApply(false, adsBlockStart != BrowserSettings.adsBlock.value, listChanged, !settingsKept, false, null)
        hostActivity = null
        super.dismiss()
    }

    companion object {

        /** Whether the site [pageHost] is on the allowed-popups list. */
        @JvmStatic
        fun siteAllowed(dialog: DialogAllowPopup?): Boolean {
            return try {
                val pop = DataBookPop.instance(dialog!!.appContext)
                pop.sites != null && !pop.sites!!.isEmpty() && !TextUtils.isEmpty(dialog.pageHost) && pop.sites!!.contains(dialog.pageHost)
            } catch (exception: Exception) {
                false
            }
        }
    }
}
