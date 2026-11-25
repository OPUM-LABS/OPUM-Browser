package com.mycompany.app.dialog

import com.mycompany.app.ui.components.AppListItem
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.data.book.DataBookJava
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookJava
import com.mycompany.app.dialog.DialogSetAdblock.DialogAdsListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.browser.ComposeBottomDialog
import java.lang.ref.WeakReference

/**
 * JavaScript settings for the current page ([path] = page URL; [pageUrl] = normalised URL, [pageHost] = its host): the global
 * JavaScript switch (`BrowserSettings.enableJs.value`), "block on this site" / "block on this page" (stored by [DialogTask] in the
 * background, `DataBookJava` + `DbBookJava`), the exception list (`SiteListActivity` type 27), a settings icon
 * (Advanced settings, index 8) and Refresh. On dismiss the listener hears whether JavaScript is now effectively on
 * and whether it changed, as before. Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogSetJava(
    mainActivity: MainActivity?,
    path: String?,
    dialogAdsListener: DialogAdsListener?,
) : ComposeBottomDialog(mainActivity) {
    @JvmField
    var hostActivity: MainActivity? = mainActivity
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
    var jsStart: Boolean = false
    @JvmField
    var refreshPage: Boolean = false
    @JvmField
    var darkInfo: String? = null

    /** JavaScript on, blocked on this site, blocked on this page. */
    private var jsOn by mutableStateOf(BrowserSettings.enableJs.value)
    private var siteBlocked by mutableStateOf(false)
    private var pageBlocked by mutableStateOf(false)
    internal var busy by mutableStateOf(false)

    class DialogTask(dialogSetJava: DialogSetJava?, path: String?, isAdd: Boolean) :
        MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var path: String? = null
        @JvmField
        var isAdd: Boolean = false

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogSetJava)
            this.dialogRef = weakReference
            val dialogSetJava = weakReference.get() as DialogSetJava?
            if (dialogSetJava != null) {
                this.path = path
                this.isAdd = isAdd
                val i: Int = 0
                dialogSetJava.busy = true
            }
        
        }
    }

        public override fun doInBackground() {
            val dialogSetJava = this.dialogRef!!.get() as DialogSetJava?
            if (dialogSetJava == null || this.cancelled) {
                return
            }
            val context = dialogSetJava.appContext
            val path = this.path
            if (this.isAdd) {
                DataBookJava.instance(context).addSite(path)
                DbBookJava.addPath(dialogSetJava.appContext, path)
                return
            }
            val dataBookJavaL = DataBookJava.instance(context)
            dataBookJavaL.javaClass
            try {
                val arrayList: ArrayList<String?>? = dataBookJavaL.sites
                if (arrayList != null && !arrayList.isEmpty() && !TextUtils.isEmpty(path)) {
                    dataBookJavaL.sites!!.remove(path)
                }
            } catch (unused: Exception) {
            }
            val appContext = dialogSetJava.appContext
            val dbBookJava = DbBookJava.cached
            if (appContext == null || TextUtils.isEmpty(path)) {
                return
            }
            DbUtil.deleteWhere(
                DbBookJava.get(appContext).getWritableDatabase(),
                "DbBookJava_table",
                "_path=?",
                arrayOf<String?>(path!!)
            )
        }

        public override fun onCancelled() {
            val dialogSetJava = this.dialogRef!!.get() as DialogSetJava?
            if (dialogSetJava != null) {
                dialogSetJava.blockTask = null
                dialogSetJava.busy = false
            }
        }

        public override fun onPostExecute() {
            val dialogSetJava = this.dialogRef!!.get() as DialogSetJava?
            if (dialogSetJava != null) {
                dialogSetJava.blockTask = null
                dialogSetJava.busy = false
            }
        }
    }

    init {
        siteBlocked = readSiteBlocked()
        pageBlocked = DataBookJava.instance(appContext).hasPage(pageUrl)
        jsStart = BrowserSettings.enableJs.value && !siteBlocked && !pageBlocked
        val info = StringBuilder()
        info.append(appContext!!.getString(AppR.string.java_script_info)).append("\n")
        darkInfo = info.append(appContext!!.getString(AppR.string.dark_mode_info_2)).toString()
        val infoText = darkInfo
        setSheet {
            // like the old sheet: only the settings icon on top (the first row already names JavaScript)
            Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp, top = 8.dp)) {
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { openSettings() }) {
                    Icon(painterResource(AppR.drawable.outline_settings_black_20), contentDescription = null)
                }
            }
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                SwitchRow(stringResource(AppR.string.java_script), infoText, jsOn, true) { setJs(it) }
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                SwitchRow(stringResource(AppR.string.js_block_site), pageHost, siteBlocked, !busy) { setBlock(true, it) }
                SwitchRow(stringResource(AppR.string.js_block_page), null, pageBlocked, !busy) { setBlock(false, it) }
                AppListItem(
                    headlineContent = { Text(stringResource(AppR.string.js_black)) },
                    trailingContent = { Icon(painterResource(AppR.drawable.outline_chevron_right_white_24), contentDescription = null) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.clickable { openList() },
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { refreshPage = true; dismiss() }) { Text(stringResource(AppR.string.refresh)) }
            }
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

    private fun readSiteBlocked(): Boolean {
        val data = DataBookJava.instance(appContext)
        return try {
            data.sites != null && !data.sites!!.isEmpty() && !TextUtils.isEmpty(pageHost) && data.sites!!.contains(pageHost)
        } catch (unused: Exception) {
            false
        }
    }

    private fun setJs(on: Boolean) {
        jsOn = on
        BrowserSettings.edit(appContext) { set(BrowserSettings.enableJs, on) }
    }

    /** Site ([site] = true, key [pageHost]) or page ([pageUrl]) block switch: stored in the background. */
    private fun setBlock(site: Boolean, on: Boolean) {
        if (site) siteBlocked = on else pageBlocked = on
        blockTask?.cancelled = true
        blockTask = null
        val task = DialogTask(this, if (site) pageHost else pageUrl, on)
        blockTask = task
        task.start(appContext)
    }

    private fun openSettings() {
        val activity = hostActivity ?: return
        // JavaScript lives on the Privacy page since the settings were regrouped (2026-11-19)
        val intent = Intent(appContext, com.mycompany.app.ui.settings.SiteSafetySettingsActivity::class.java)
        intent.putExtra("EXTRA_NOTI", true)
        intent.putExtra("EXTRA_INDEX", 8)
        intent.putExtra("EXTRA_PATH", pageUrl)
        activity.startActivity(intent)
    }

    /** The list now opens as a Compose screen, [listOpen] guards double opens. */
    private fun openList() {
        val host = hostActivity ?: return
        if (hostActivity == null || listOpen) return
        listOpen = true
        com.mycompany.app.ui.lists.SiteListActivity.open(host, 27, pageUrl) {
            listOpen = false
            if (hostActivity != null) refreshState(false)
        }
    }

    /** Re-reads the switches (after the exception list or the settings screen changed them). */
    fun refreshState(refresh: Boolean) {
        if (jsOn != BrowserSettings.enableJs.value) jsOn = BrowserSettings.enableJs.value
        val site = readSiteBlocked()
        if (siteBlocked != site) siteBlocked = site
        val page = DataBookJava.instance(appContext).hasPage(pageUrl)
        if (pageBlocked != page) pageBlocked = page
    }

    override fun canCancelFromScrim(): Boolean = !busy

    override fun dismiss() {
        blockTask?.cancelled = true
        blockTask = null
        val listener = adsListener
        adsListener = null
        if (listener != null) {
            val jsOn = BrowserSettings.enableJs.value && !siteBlocked && !pageBlocked
            listener.onApply(jsStart != jsOn, jsOn, false, false, refreshPage, null)
        }
        hostActivity = null
        super.dismiss()
    }

    companion object {
    }
}
