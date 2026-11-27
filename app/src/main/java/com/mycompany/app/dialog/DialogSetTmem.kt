package com.mycompany.app.dialog

import android.content.Context
import com.mycompany.app.pref.store.ZtwoSettings
import android.content.DialogInterface
import android.content.Intent
import android.text.TextUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import com.mycompany.app.ui.components.TickSwitch as Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.data.book.DataBookTmem
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookTmem
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.ui.browser.ComposeBottomDialog
import java.lang.ref.WeakReference

/**
 * Tab memory for the current page ([path] = page URL; [pageUrl] = normalised URL, [pageHost] = its host): the memory limit
 * (`ZtwoSettings.tabMemory.value`; tap = menu "None" / "Setting", the latter opens `DialogSeekSimple` kind 11), "on this site" /
 * "on this page" switches (stored by [DialogTask], `DataBookTmem` + `DbBookTmem`), the block list
 * (`SiteListActivity` type 28) and a settings icon (Memory settings, index 7). Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogSetTmem(mainActivity: MainActivity?, path: String?) : ComposeBottomDialog(mainActivity) {
    @JvmField
    var hostActivity: MainActivity? = mainActivity
    @JvmField
    var appContext: Context? = context
    @JvmField
    var pageUrl: String? = MainUtil.trimTrailingSlash(path)
    @JvmField
    var pageHost: String? = MainUtil.hostOfUrl(pageUrl!!, true)
    @JvmField
    var blockTask: DialogTask? = null
    @JvmField
    var seekDialog: DialogSeekSimple? = null
    private var listOpen = false

    private var limit by mutableIntStateOf(ZtwoSettings.tabMemory.value)
    private var siteBlocked by mutableStateOf(false)
    private var pageBlocked by mutableStateOf(false)
    private var limitMenu by mutableStateOf(false)
    internal var busy by mutableStateOf(false)

    class DialogTask(dialogSetTmem: DialogSetTmem?, path: String?, isAdd: Boolean) :
        MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var path: String? = null
        @JvmField
        var isAdd: Boolean = false

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogSetTmem)
            this.dialogRef = weakReference
            val dialogSetTmem = weakReference.get() as DialogSetTmem?
            if (dialogSetTmem != null) {
                this.path = path
                this.isAdd = isAdd
                val i: Int = 0
                dialogSetTmem.busy = true
            }
        
        }
    }

        public override fun doInBackground() {
            val dialogSetTmem = this.dialogRef!!.get() as DialogSetTmem?
            if (dialogSetTmem == null || this.cancelled) {
                return
            }
            val context = dialogSetTmem.appContext
            val path = this.path
            if (this.isAdd) {
                DataBookTmem.instance(context).addSite(path)
                DbBookTmem.addPath(dialogSetTmem.appContext, path)
                return
            }
            val dataBookTmemL = DataBookTmem.instance(context)
            dataBookTmemL.javaClass
            try {
                val arrayList: ArrayList<String?>? = dataBookTmemL.sites
                if (arrayList != null && !arrayList.isEmpty() && !TextUtils.isEmpty(path)) {
                    dataBookTmemL.sites!!.remove(path)
                }
            } catch (unused: Exception) {
            }
            val appContext = dialogSetTmem.appContext
            val dbBookTmem = DbBookTmem.cached
            if (appContext == null || TextUtils.isEmpty(path)) {
                return
            }
            DbUtil.deleteWhere(
                DbBookTmem.get(appContext).getWritableDatabase(),
                "DbBookTmem_table",
                "_path=?",
                arrayOf<String?>(path!!)
            )
        }

        public override fun onCancelled() {
            val dialogSetTmem = this.dialogRef!!.get() as DialogSetTmem?
            if (dialogSetTmem != null) {
                dialogSetTmem.blockTask = null
                dialogSetTmem.busy = false
            }
        }

        public override fun onPostExecute() {
            val dialogSetTmem = this.dialogRef!!.get() as DialogSetTmem?
            if (dialogSetTmem != null) {
                dialogSetTmem.blockTask = null
                dialogSetTmem.busy = false
            }
        }
    }

    init {
        siteBlocked = readSiteBlocked()
        pageBlocked = DataBookTmem.instance(appContext).hasSite(pageUrl)
        setSheet {
            Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp, top = 8.dp)) {
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { openSettings() }) {
                    Icon(painterResource(AppR.drawable.outline_settings_black_20), contentDescription = null)
                }
            }
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                Box {
                    ListItem(
                        headlineContent = { Text(stringResource(AppR.string.mem_limit)) },
                        supportingContent = { Text((limitText(limit) ?: "") + "\n" + (infoText(context) ?: "")) },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        modifier = Modifier.clickable { limitMenu = true },
                    )
                    com.mycompany.app.ui.components.BubbleMenu(expanded = limitMenu, onDismiss = { limitMenu = false }) {
                        DropdownMenuItem(text = { Text(stringResource(AppR.string.history_none)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(AppR.drawable.outline_block_black_24) }, onClick = { limitMenu = false; applyLimit(0) })
                        DropdownMenuItem(text = { Text(stringResource(AppR.string.setting)) }, leadingIcon = { com.mycompany.app.ui.components.MenuIcon(AppR.drawable.outline_settings_black_20) }, onClick = { limitMenu = false; openSeek() })
                    }
                }
                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                SwitchRow(stringResource(AppR.string.mem_block_site), pageHost, siteBlocked, !busy) { setBlock(true, it) }
                SwitchRow(stringResource(AppR.string.mem_block_page), null, pageBlocked, !busy) { setBlock(false, it) }
                ListItem(
                    headlineContent = { Text(stringResource(AppR.string.mem_block_list)) },
                    trailingContent = { Icon(painterResource(AppR.drawable.outline_chevron_right_white_24), contentDescription = null) },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.clickable { openList() },
                )
                Spacer(Modifier.padding(bottom = 16.dp))
            }
        }
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

    private fun readSiteBlocked(): Boolean {
        val data = DataBookTmem.instance(appContext)
        return try {
            data.sites != null && !data.sites!!.isEmpty() && !TextUtils.isEmpty(pageHost) && data.sites!!.contains(pageHost)
        } catch (unused: Exception) {
            false
        }
    }

    /** Site ([site] = true, key [pageHost]) or page ([pageUrl]) switch: stored in the background. */
    private fun setBlock(site: Boolean, on: Boolean) {
        if (site) siteBlocked = on else pageBlocked = on
        blockTask?.cancelled = true
        blockTask = null
        val task = DialogTask(this, if (site) pageHost else pageUrl, on)
        blockTask = task
        task.start(appContext)
    }

    private fun openSeek() {
        if (hostActivity == null || seekDialog != null) return
        var value = ZtwoSettings.tabMemory.value
        if (value < 3) value = 5
        val seek = DialogSeekSimple(hostActivity, 11, value, object : DialogSeekListener {
            override fun onValue(newValue: Int) { applyLimit(newValue) }
        })
        seekDialog = seek
        seek.setOnDismissListener(DialogInterface.OnDismissListener {
            seekDialog?.dismiss()
            seekDialog = null
        })
    }

    private fun openSettings() {
        val activity = hostActivity ?: return
        val intent = Intent(appContext, com.mycompany.app.ui.settings.MemorySettingsActivity::class.java)
        intent.putExtra("EXTRA_NOTI", true)
        intent.putExtra("EXTRA_INDEX", 7)
        intent.putExtra("EXTRA_PATH", pageUrl)
        activity.startActivity(intent)
    }

    /** The list now opens as a Compose screen, [listOpen] guards double opens. */
    private fun openList() {
        val host = hostActivity ?: return
        if (hostActivity == null || seekDialog != null || listOpen) return
        listOpen = true
        com.mycompany.app.ui.lists.SiteListActivity.open(host, 28, pageUrl) {
            listOpen = false
            if (hostActivity != null) refreshState(false)
        }
    }

    /** "None" below 3 tabs, otherwise the number. */
    fun limitText(limit: Int): String? {
        val context = appContext ?: return null
        return if (limit < 3) context.getString(AppR.string.history_none) else limit.toString()
    }

    /** Re-reads the rows (after the block list or the settings screen changed them). */
    fun refreshState(refresh: Boolean) {
        if (limit != ZtwoSettings.tabMemory.value) limit = ZtwoSettings.tabMemory.value
        val site = readSiteBlocked()
        if (siteBlocked != site) siteBlocked = site
        val page = DataBookTmem.instance(appContext).hasSite(pageUrl)
        if (pageBlocked != page) pageBlocked = page
    }

    /** Stores a new limit. */
    fun applyLimit(newLimit: Int) {
        if (ZtwoSettings.tabMemory.value == newLimit) return
        // a limit set here makes the memory mode custom (Settings > Performance & memory)
        ZtwoSettings.edit(appContext) {
            set(ZtwoSettings.tabMemory, newLimit)
            set(ZtwoSettings.memoryMode, com.mycompany.app.pref.store.MemoryPlan.CUSTOM)
        }
        limit = newLimit
    }

    override fun canCancelFromScrim(): Boolean = !busy

    override fun dismiss() {
        blockTask?.cancelled = true
        blockTask = null
        val seek = seekDialog
        seekDialog = null
        seek?.dismiss()
        hostActivity = null
        super.dismiss()
    }

    companion object {
        @JvmStatic
        fun infoText(context: Context?): String? {
            if (context == null) {
                return null
            }
            val sb = StringBuilder()
            if (ZtwoSettings.tabMemory.value > 2) {
                sb.append(context.getString(AppR.string.limit_info_2)).append("\n")
            }
            return sb.append(context.getString(AppR.string.tab_tip)).toString()
        }
    }
}
