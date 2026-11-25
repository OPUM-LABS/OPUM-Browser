package com.mycompany.app.dialog

import android.content.Context
import com.mycompany.app.main.RequestCode
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.BrowserSettings
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.text.TextUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.data.book.DataBookLink
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookLink
import com.mycompany.app.dialog.DialogSetAdblock.DialogAdsListener
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainListLoader.ListLoadListener
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.web.WebViewActivity
import java.lang.ref.WeakReference

/**
 * "Block link" for one link ([path] = page, [link] = link; [linkUrl] = normalised link, [linkHost] = its host): header with the
 * link's favicon (or a letter) and address plus a settings icon (Web Filter settings, index 17 / 16). Without an app
 * lock (`SecretSettings.linkLockType.value` == 0) the feature is unavailable and the sheet offers the lock type (M3 menu over
 * `MainConst.lockValues`; biometric (4) is set directly, the others open the lock-setup screen, request 3) and the explanation.
 * With a lock: "block links to this site" / "this link" switches (kept [DialogTask], `DataBookLink` / `DbBookLink`) and
 * the blocked-link list (`SiteListActivity` type 22). `WebViewActivity` sets [listChanged] / [settingsKept] and calls [refresh] after
 * its screens; the dismiss callback is unchanged. Compose now (on [ComposeBottomDialog]); same constructor, shows
 * itself.
 */
@Suppress("deprecation")
class DialogBlockLink(
    webViewActivity: WebViewActivity?,
    path: String?,
    link: String?,
    dialogAdsListener: DialogAdsListener?,
) : ComposeBottomDialog(webViewActivity) {
    @JvmField
    var hostActivity: WebViewActivity? = webViewActivity
    @JvmField
    var appContext: Context? = context
    @JvmField
    var adsListener: DialogAdsListener? = dialogAdsListener
    @JvmField
    var pageUrl: String? = MainUtil.trimTrailingSlash(path)
    @JvmField
    var linkUrl: String? = MainUtil.trimTrailingSlash(link)
    @JvmField
    var linkHost: String? = MainUtil.hostOfUrl(linkUrl!!, true)
    @JvmField
    var blockTask: DialogTask? = null
    private var listOpen = false
    @JvmField
    var linkLockType: Int = 0
    @JvmField
    var siteBlock: Boolean = false
    @JvmField
    var pageBlock: Boolean = false
    @JvmField
    var adsBlockStart: Boolean = BrowserSettings.adsBlock.value
    @JvmField
    var listChanged: Boolean = false
    @JvmField
    var settingsKept: Boolean = false
    @JvmField
    var listLoader: MainListLoader? = null

    private var lockType by mutableIntStateOf(0)
    private var siteBlocked by mutableStateOf(false)
    private var pageBlocked by mutableStateOf(false)
    private var lockMenu by mutableStateOf(false)
    private var favicon by mutableStateOf<Bitmap?>(null)
    internal var busy by mutableStateOf(false)

    class DialogTask(dialogBlockLink: DialogBlockLink?, path: String?, isAdd: Boolean) :
        MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var path: String? = null
        @JvmField
        var isAdd: Boolean = false

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogBlockLink)
            this.dialogRef = weakReference
            val dialogBlockLink = weakReference.get() as DialogBlockLink?
            if (dialogBlockLink != null) {
                this.path = path
                this.isAdd = isAdd
                val i: Int = 0
                dialogBlockLink.busy = true
            }
        
        }
    }

        public override fun doInBackground() {
            val dialogBlockLink = this.dialogRef!!.get() as DialogBlockLink?
            if (dialogBlockLink == null || this.cancelled) {
                return
            }
            val context = dialogBlockLink.appContext
            val path = this.path
            if (this.isAdd) {
                DataBookLink.instance(context).addLink(path)
                DbBookLink.addPath(dialogBlockLink.appContext, path)
                return
            }
            val dataBookLinkM = DataBookLink.instance(context)
            dataBookLinkM.javaClass
            try {
                val arrayList: ArrayList<String?>? = dataBookLinkM.links
                if (arrayList != null && !arrayList.isEmpty() && !TextUtils.isEmpty(path)) {
                    dataBookLinkM.links!!.remove(path)
                }
            } catch (unused: Exception) {
            }
            val appContext = dialogBlockLink.appContext
            val dbBookLink = DbBookLink.cached
            if (appContext == null || TextUtils.isEmpty(path)) {
                return
            }
            DbUtil.deleteWhere(
                DbBookLink.get(appContext).getWritableDatabase(),
                "DbBookLink_table",
                "_path=?",
                arrayOf<String?>(path!!)
            )
        }

        public override fun onCancelled() {
            val dialogBlockLink = this.dialogRef!!.get() as DialogBlockLink?
            if (dialogBlockLink != null) {
                dialogBlockLink.blockTask = null
                dialogBlockLink.busy = false
            }
        }

        public override fun onPostExecute() {
            val dialogBlockLink = this.dialogRef!!.get() as DialogBlockLink?
            if (dialogBlockLink != null) {
                dialogBlockLink.blockTask = null
                dialogBlockLink.busy = false
            }
        }
    }

    init {
        refresh()
        setSheet {
            Row(
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val bmp = favicon
                if (bmp != null) {
                    Image(bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.size(32.dp).clip(CircleShape))
                } else {
                    Box(
                        Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            (letterHost() ?: "?").take(1).uppercase(),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
                Text(
                    title, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                )
                IconButton(onClick = { openSettings() }) {
                    Icon(painterResource(AppR.drawable.outline_settings_black_20), contentDescription = null)
                }
            }
            HorizontalDivider()
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                if (lockType == 0) {
                    Box {
                        ListItem(
                            headlineContent = { Text(stringResource(AppR.string.lock_type)) },
                            supportingContent = { Text(stringResource(MainConst.lockNames[lockType])) },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            modifier = Modifier.clickable { lockMenu = true },
                        )
                        com.mycompany.app.ui.components.BubbleMenu(expanded = lockMenu, onDismiss = { lockMenu = false }) {
                            MainConst.lockValues.forEach { v ->
                                DropdownMenuItem(
                                    text = { Text(stringResource(MainConst.lockNames[v])) },
                                    trailingIcon = { RadioButton(selected = SecretSettings.linkLockType.value == v, onClick = null) },
                                    onClick = { lockMenu = false; chooseLock(v) },
                                )
                            }
                        }
                    }
                    Text(
                        stringResource(AppR.string.password_lock_1),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    )
                } else {
                    SwitchRow(stringResource(AppR.string.link_block_site), linkHost, siteBlocked, !busy) { setBlock(true, it) }
                    SwitchRow(stringResource(AppR.string.link_block_page), null, pageBlocked, !busy) { setBlock(false, it) }
                    ListItem(
                        headlineContent = { Text(stringResource(AppR.string.blocked_link)) },
                        trailingContent = { Icon(painterResource(AppR.drawable.outline_chevron_right_white_24), contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        modifier = Modifier.clickable { openList() },
                    )
                }
                Spacer(Modifier.padding(bottom = 16.dp))
            }
        }
        loadFavicon()
        show()
    }

    @Composable
    private fun SwitchRow(title: String, info: String?, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = info?.let { { Text(it) } },
            trailingContent = { Switch(checked = checked, onCheckedChange = onChange, enabled = enabled) },
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.clickable(enabled = enabled) { onChange(!checked) },
        )
    }

    private val title: String = run {
        val decoded = MainUtil.urlDecode(linkUrl)
        if (TextUtils.isEmpty(decoded)) linkUrl ?: "" else decoded!!
    }

    /** Host for the letter icon: without a leading "x." or "www.". */
    private fun letterHost(): String? {
        val page = linkUrl ?: return null
        var host = MainUtil.hostOfUrl(page, true)
        if (TextUtils.isEmpty(host)) {
            host = page
        } else if (host!!.length > 2 && host.startsWith(".", 1)) {
            host = host.substring(2)
        } else if (host.length > 4 && host.startsWith("www.")) {
            host = host.substring(4)
        }
        return host
    }

    private fun loadFavicon() {
        val page = linkUrl
        if (TextUtils.isEmpty(page)) return
        val item = ChildItem()
        item.listType = 18
        item.itemType = 11.toByte()
        item.path = page
        val cached = MainListLoader.cachedIconFor(item)
        if (MainUtil.isBitmapOk(cached)) {
            favicon = cached
            return
        }
        val ctx = appContext ?: return
        val target = android.view.View(ctx)
        target.setTag(0)
        val loader = MainListLoader(ctx, false, object : ListLoadListener {
            override fun onFailed(childItem: ChildItem?, view: android.view.View?) {}
            override fun onLoaded(childItem: ChildItem?, view: android.view.View?, bitmap: Bitmap?) {
                if (MainUtil.isBitmapOk(bitmap)) favicon = bitmap
            }
        })
        listLoader = loader
        loader.enqueue(item, target)
    }

    /** Re-reads lock type and both switches (also called by `WebViewActivity` after the lock / settings screens). */
    fun refresh() {
        linkLockType = SecretSettings.linkLockType.value
        lockType = linkLockType
        siteBlock = DataBookLink.instance(appContext).hasExact(linkHost)
        pageBlock = DataBookLink.instance(appContext).hasSite(linkUrl)
        siteBlocked = siteBlock
        pageBlocked = pageBlock
    }

    private fun setBlock(site: Boolean, on: Boolean) {
        if (site) {
            siteBlock = on
            siteBlocked = on
        } else {
            pageBlock = on
            pageBlocked = on
        }
        blockTask?.cancelled = true
        blockTask = null
        val task = DialogTask(this, if (site) linkHost else linkUrl, on)
        blockTask = task
        task.start(appContext)
    }

    private fun chooseLock(type: Int) {
        if (type == 0) return
        if (type == 4) {
            if (SecretSettings.linkLockType.value != type && MainUtil.canUseBiometric(appContext, true)) {
                SecretSettings.editLocal { set(SecretSettings.linkLockType, type) }
                SecretSettings.editLocal { set(SecretSettings.linkLockCode, "") }
                PrefSecret.saveLinkLock(appContext)
                refresh()
            }
            return
        }
        val activity = hostActivity ?: return
        val intent = MainUtil.lockIntent(appContext, type)
        intent.putExtra("EXTRA_PASS", 2)
        intent.putExtra("EXTRA_TYPE", 1)
        activity.launchExternalForResult(intent, RequestCode.UNLOCK_SECRET)
    }

    private fun openSettings() {
        val activity = hostActivity ?: return
        val intent = Intent(appContext, com.mycompany.app.ui.settings.BlockedSettingsActivity::class.java)
        intent.putExtra("EXTRA_POPUP", true)
        intent.putExtra("EXTRA_NOTI", true)
        intent.putExtra("EXTRA_INDEX", if (SecretSettings.linkLockType.value == 0) 17 else 16)
        intent.putExtra("EXTRA_PATH", pageUrl)
        activity.launchExternalForResult(intent, RequestCode.DIALOG_SETTINGS)
    }

    /** The list now opens as a Compose screen, [listOpen] guards double opens. */
    private fun openList() {
        val host = hostActivity ?: return
        if (hostActivity == null || listOpen) return
        listOpen = true
        com.mycompany.app.ui.lists.SiteListActivity.open(host, 22, pageUrl) {
            listOpen = false
            if (hostActivity != null) refreshBlock(false)
        }
    }

    /** Refresh after a sub-list, and pass [refreshFlag] on to the open list. */
    fun refreshBlock(refreshFlag: Boolean) {
        refresh()
    }

    override fun canCancelFromScrim(): Boolean = !busy

    override fun dismiss() {
        blockTask?.cancelled = true
        blockTask = null
        val listener = adsListener
        adsListener = null
        listener?.onApply(false, adsBlockStart != BrowserSettings.adsBlock.value, listChanged, !settingsKept, false, null)
        listLoader?.release()
        listLoader = null
        hostActivity = null
        super.dismiss()
    }

    companion object {
    }
}
