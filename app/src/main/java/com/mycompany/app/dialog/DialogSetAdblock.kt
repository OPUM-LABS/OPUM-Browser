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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.data.book.DataBookAds
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookAds
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.browser.ComposeBottomDialog
import java.lang.ref.WeakReference

/**
 * Ad-block settings for the current page ([path] = page URL; [pageUrl] = normalised URL, [pageHost] = its host): the global ad-block
 * switch (`BrowserSettings.adsBlock.value`), "allow ads on this site" / "on this page", "allow pop-ups on this site" (stored by [DialogTask] in the background,
 * `DataBookAds` + `DbBookAds`), the allow list (`SiteListActivity` type 19), a settings icon (Web Filter
 * settings, request 35) and Refresh. On dismiss the listener hears `(false, switch changed, m0, !n0, refresh, null)` as
 * before; [listChanged] / [settingsKept] are set by `WebViewActivity` after the settings screen returns. Compose now (on
 * [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogSetAdblock(
    mainActivity: MainActivity?,
    path: String?,
    dialogAdsListener: DialogAdsListener?,
    /** The page's WebView: what the filter did there is shown on top (`PageStats`, hidden elements, pop-ups). */
    pageView: android.webkit.WebView? = null,
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
    var adsBlockStart: Boolean = false
    @JvmField
    var listChanged: Boolean = false
    @JvmField
    var settingsKept: Boolean = false
    @JvmField
    var refreshPage: Boolean = false

    /** Ad-block on, ads allowed on this site, on this page. */
    private var blockOn by mutableStateOf(BrowserSettings.adsBlock.value)
    private var siteAllowed by mutableStateOf(false)
    private var pageAllowed by mutableStateOf(false)
    internal var busy by mutableStateOf(false)

    /** What the filter did on the page (null: nothing counted for it), elements hidden there (null: not known yet). */
    private val stats: com.mycompany.app.web.filter.PageStats.Snapshot? =
        com.mycompany.app.web.filter.PageStats.snapshot(pageView)?.takeIf {
            it.pageUrl == null || MainUtil.hostOfUrl(it.pageUrl, true) == pageHost
        }
    private var hidden by mutableStateOf<Int?>(null)
    private val popups: Int = (pageView as? com.mycompany.app.web.WebNestView)?.popsBlocked ?: 0
    private val totalBlocked: Long = com.mycompany.app.web.filter.PageStats.totalBlocked(context)
    private var showDomains by mutableStateOf(false)
    /** The site is on the pop-up allow list (`DataBookPop` / `DbBookPop`, as `DialogAllowPopup` stores it). */
    private var popsAllowed by mutableStateOf(false)

    interface DialogAdsListener {
        fun onApply(changed: Boolean, jsChanged: Boolean, listChanged: Boolean, userChanged: Boolean, refreshPage: Boolean, pageUrl: String?)
    }

    class DialogTask(dialogSetAdblock: DialogSetAdblock?, path: String?, isAdd: Boolean) :
        MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var path: String? = null
        @JvmField
        var isAdd: Boolean = false

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogSetAdblock)
            this.dialogRef = weakReference
            val dialogSetAdblock = weakReference.get() as DialogSetAdblock?
            if (dialogSetAdblock != null) {
                this.path = path
                this.isAdd = isAdd
                val i: Int = 0
                dialogSetAdblock.busy = true
            }
        
        }
    }

        public override fun doInBackground() {
            val dialogSetAdblock = this.dialogRef!!.get() as DialogSetAdblock?
            if (dialogSetAdblock == null || this.cancelled) {
                return
            }
            val context = dialogSetAdblock.appContext
            val path = this.path
            if (this.isAdd) {
                DataBookAds.instance(context).addSite(path)
                DbBookAds.addPath(dialogSetAdblock.appContext, path)
                return
            }
            val dataBookAdsL = DataBookAds.instance(context)
            dataBookAdsL.javaClass
            try {
                val arrayList: ArrayList<String?>? = dataBookAdsL.sites
                if (arrayList != null && !arrayList.isEmpty() && !TextUtils.isEmpty(path)) {
                    dataBookAdsL.sites!!.remove(path)
                }
            } catch (unused: Exception) {
            }
            val appContext = dialogSetAdblock.appContext
            val dbBookAds = DbBookAds.cached
            if (appContext == null || TextUtils.isEmpty(path)) {
                return
            }
            DbUtil.deleteWhere(
                DbBookAds.get(appContext).getWritableDatabase(),
                "DbBookAds_table",
                "_path=?",
                arrayOf<String?>(path!!)
            )
        }

        public override fun onCancelled() {
            val dialogSetAdblock = this.dialogRef!!.get() as DialogSetAdblock?
            if (dialogSetAdblock != null) {
                dialogSetAdblock.blockTask = null
                dialogSetAdblock.busy = false
            }
        }

        public override fun onPostExecute() {
            val dialogSetAdblock = this.dialogRef!!.get() as DialogSetAdblock?
            if (dialogSetAdblock != null) {
                dialogSetAdblock.blockTask = null
                dialogSetAdblock.busy = false
            }
        }
    }

    init {
        siteAllowed = readSiteAllowed()
        popsAllowed = try {
            com.mycompany.app.data.book.DataBookPop.instance(appContext).hasSiteOf(pageUrl, pageHost)
        } catch (_: Exception) {
            false
        }
        pageAllowed = DataBookAds.instance(appContext).hasPage(pageUrl)
        adsBlockStart = BrowserSettings.adsBlock.value
        if (stats != null) countHidden(pageView)
        setSheet {
            // title and the settings icon (Web Filter settings) on top
            Row(
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(AppR.string.mh_cleaner), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = { openSettings() }) {
                    Icon(painterResource(AppR.drawable.outline_settings_black_20), contentDescription = stringResource(AppR.string.setting))
                }
            }
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                Summary()
                HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                SwitchRow(stringResource(AppR.string.ads_block), stringResource(AppR.string.ads_block_info), blockOn, true) { changeBlock(it) }
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                SwitchRow(stringResource(AppR.string.ads_allow_site), pageHost, siteAllowed, !busy) { setBlock(true, it) }
                SwitchRow(stringResource(AppR.string.ads_allow_page), null, pageAllowed, !busy) { setBlock(false, it) }
                // the pop-up blocker is part of the web filter: its per-site exception lives here too
                if (BrowserSettings.popBlock.value != 0) {
                    SwitchRow(stringResource(AppR.string.pop_allow_site), pageHost, popsAllowed, true) { changePopsAllowed(it) }
                }
                AppListItem(
                    headlineContent = { Text(stringResource(AppR.string.ads_white)) },
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

    /** What the filter did on this page, like the uBlock Origin popup. */
    @Composable
    private fun Summary() {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp)) {
            if (!pageHost.isNullOrEmpty()) {
                Text(pageHost!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val note = when {
                !blockOn -> AppR.string.ubo_sum_off
                siteAllowed || pageAllowed -> AppR.string.ubo_sum_allowed
                stats == null -> AppR.string.ubo_sum_empty
                else -> 0
            }
            if (note != 0 || stats == null) {
                Text(stringResource(if (note != 0) note else AppR.string.ubo_sum_empty), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                val nf = java.text.NumberFormat.getIntegerInstance()
                val percent = if (stats.requests > 0) stats.blocked * 100 / stats.requests else 0
                Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.Bottom) {
                    Text(nf.format(stats.blocked), style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.primary)
                    Text(
                        " " + stringResource(AppR.string.ubo_sum_blocked) + " (" + percent + "%)",
                        style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 6.dp, bottom = 6.dp),
                    )
                }
                // as in uBlock Origin: a domain counts as connected when at least one of its requests went through
                val connected = stats.domains.count { it.requests > it.blocked }
                StatRow(stringResource(AppR.string.ubo_sum_domains), nf.format(connected) + " / " + nf.format(stats.domains.size)) {
                    showDomains = !showDomains
                }
                if (showDomains) {
                    Text(stringResource(AppR.string.ubo_sum_legend), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 12.dp))
                    for (d in stats.domains) {
                        Row(Modifier.fillMaxWidth().padding(start = 12.dp, top = 2.dp, bottom = 2.dp)) {
                            Text(
                                d.host, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f),
                                maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                color = if (d.blocked >= d.requests) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                nf.format(d.blocked) + " / " + nf.format(d.requests), style = MaterialTheme.typography.bodySmall,
                                color = if (d.blocked > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                StatRow(stringResource(AppR.string.ubo_sum_hidden), hidden?.let { nf.format(it) } ?: "–", null)
                StatRow(stringResource(AppR.string.ubo_sum_popups), nf.format(popups), null)
                StatRow(stringResource(AppR.string.ubo_sum_params), nf.format(stats.paramsRemoved), null)
            }
            StatRow(stringResource(AppR.string.ubo_sum_total), java.text.NumberFormat.getIntegerInstance().format(totalBlocked), null)
        }
    }

    @Composable
    private fun StatRow(label: String, value: String, onClick: (() -> Unit)?) {
        Row(
            Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable { onClick() } else Modifier).padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.bodyMedium)
            if (onClick != null) {
                Icon(
                    androidx.compose.material.icons.Icons.Filled.ArrowDropDown, contentDescription = null,
                    modifier = Modifier.padding(start = 4.dp).then(if (showDomains) Modifier.rotate(180f) else Modifier),
                )
            }
        }
    }

    /** Elements the cosmetic filter hid in the page's main frame (its style sheet's matches plus procedural ones). */
    private fun countHidden(view: android.webkit.WebView?) {
        if (view == null) return
        val js = "(function(){try{var s=document.querySelector('style[data-opum]');var set=new Set();" +
            "if(s&&s.sheet){var r=s.sheet.cssRules;for(var i=0;i<r.length;i++){try{var l=document.querySelectorAll(r[i].selectorText);" +
            "for(var j=0;j<l.length;j++)set.add(l[j]);}catch(e){}}}return set.size+((s&&s._opumProc)||0);}catch(e){return -1;}})()"
        try {
            view.evaluateJavascript(js) { result ->
                val n = result?.trim()?.toIntOrNull()
                if (n != null && n >= 0) hidden = n
            }
        } catch (_: Exception) {
        }
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

    private fun readSiteAllowed(): Boolean {
        val data = DataBookAds.instance(appContext)
        return try {
            data.sites != null && !data.sites!!.isEmpty() && !TextUtils.isEmpty(pageHost) && data.sites!!.contains(pageHost)
        } catch (unused: Exception) {
            false
        }
    }

    private fun changeBlock(on: Boolean) {
        blockOn = on
        BrowserSettings.edit(appContext) { set(BrowserSettings.adsBlock, on) }
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

    /** Puts the site on the pop-up allow list ([on]) or takes it off. */
    private fun changePopsAllowed(on: Boolean) {
        popsAllowed = on
        val key = pageHost ?: return
        val ctx = appContext
        Thread {
            val data = com.mycompany.app.data.book.DataBookPop.instance(ctx)
            if (on) {
                data.addSite(key)
                com.mycompany.app.db.book.DbBookPop.addPath(ctx, key)
            } else {
                try { data.sites?.remove(key) } catch (_: Exception) {}
                if (ctx != null) {
                    DbUtil.deleteWhere(com.mycompany.app.db.book.DbBookPop.get(ctx).writableDatabase, "DbBookPop_table", "_path=?", arrayOf(key))
                }
            }
        }.start()
    }

    private fun openSettings() {
        val activity = hostActivity ?: return
        val intent = Intent(appContext, com.mycompany.app.ui.settings.AdsSettingsActivity::class.java)
        intent.putExtra("EXTRA_POPUP", true)
        intent.putExtra("EXTRA_PATH", pageUrl)
        activity.launchExternalForResult(intent, RequestCode.DIALOG_SETTINGS)
    }

    /** The list now opens as a Compose screen, [listOpen] guards double opens. */
    private fun openList() {
        val host = hostActivity ?: return
        if (hostActivity == null || listOpen) return
        listOpen = true
        com.mycompany.app.ui.lists.SiteListActivity.open(host, 19, pageUrl) {
            listOpen = false
            if (hostActivity != null) refreshState(false)
        }
    }

    /** Re-reads the switches (after the exception list or the settings screen changed them). */
    fun refreshState(refresh: Boolean) {
        if (blockOn != BrowserSettings.adsBlock.value) blockOn = BrowserSettings.adsBlock.value
        val site = readSiteAllowed()
        if (siteAllowed != site) siteAllowed = site
        val page = DataBookAds.instance(appContext).hasPage(pageUrl)
        if (pageAllowed != page) pageAllowed = page
        val pops = try { com.mycompany.app.data.book.DataBookPop.instance(appContext).hasSiteOf(pageUrl, pageHost) } catch (_: Exception) { false }
        if (popsAllowed != pops) popsAllowed = pops
    }

    override fun canCancelFromScrim(): Boolean = !busy

    override fun dismiss() {
        blockTask?.cancelled = true
        blockTask = null
        val listener = adsListener
        adsListener = null
        listener?.onApply(false, adsBlockStart != BrowserSettings.adsBlock.value, listChanged, !settingsKept, refreshPage, null)
        hostActivity = null
        super.dismiss()
    }

    companion object {
    }
}
