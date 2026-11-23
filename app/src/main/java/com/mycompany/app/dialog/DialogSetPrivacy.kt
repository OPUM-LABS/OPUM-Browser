package com.mycompany.app.dialog

import com.mycompany.app.ui.components.AppListItem
import com.mycompany.app.main.RequestCode
import android.content.Context
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.BrowserSettings
import android.content.Intent
import android.os.Handler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.browser.ComposeBottomDialog
import android.os.Looper
import android.webkit.WebViewDatabase
import com.bumptech.glide.Glide
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.db.book.DbBookDown
import com.mycompany.app.db.book.DbBookHistory
import com.mycompany.app.db.book.DbBookIcon
import com.mycompany.app.db.book.DbBookRecent
import com.mycompany.app.db.book.DbBookTab
import com.mycompany.app.db.book.DbBookTab.Companion.clearSecret
import com.mycompany.app.main.MainUtil.clearCookies
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.pref.PrefSync

/**
 * Clear browsing data. Six check boxes (mask [deleteMask]: 2 recent searches, 4 history, 8 cookies / site data, 16 cache,
 * 32 normal tabs, 64 secret tabs). With [exitMode] = true it edits "delete on exit" (`BrowserSettings.exitDelete.value`, Apply); otherwise it
 * deletes now (`BrowserSettings.dataDelete.value` remembered; Delete disabled with nothing ticked; the deletion itself, [DeletedTask],
 * is unchanged and runs in the background; [TabDeletedListener] hears when the current tabs were deleted). With [fromMenu]
 * a settings icon opens the privacy settings (request 35; `WebViewActivity` then sets [settingsOpened] and calls [loadMask]). Compose now
 * (on [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogSetPrivacy(
    mainActivity: android.app.Activity?,
    exitModeParam: Boolean,
    fromMenuParam: Boolean,
    tabDeletedListener: TabDeletedListener?,
) : ComposeBottomDialog(mainActivity) {
    @JvmField
    var hostActivity: android.app.Activity? = mainActivity
    @JvmField
    var appContext: Context? = context
    @JvmField
    var deletedListener: TabDeletedListener? = tabDeletedListener
    @JvmField
    var exitMode: Boolean = exitModeParam
    @JvmField
    var fromMenu: Boolean = fromMenuParam
    @JvmField
    var deleteMask: Int = 0
    @JvmField
    var settingsOpened: Boolean = false
    @JvmField
    var applying: Boolean = false
    @JvmField
    var tabsDeleted: Boolean = false

    internal val ui = Handler(android.os.Looper.getMainLooper())
    private var mask by mutableIntStateOf(0)
    private var deleting by mutableStateOf(false)

    private class Row6(val bit: Int, val title: Int, val info: Int)

    private val rows = listOf(
        Row6(2, AppR.string.recent_search, 0),
        Row6(4, AppR.string.history, 0),
        Row6(8, AppR.string.cookie, AppR.string.cookie_info),
        Row6(16, AppR.string.cache, AppR.string.cache_info),
        Row6(32, AppR.string.normal_tab, 0),
        Row6(64, AppR.string.secret_tab, 0),
    )

    internal inner class DeletedTask : Runnable {
        internal inner class DeletedDoneStep : Runnable {
            override fun run() {
                val dialogSetPrivacy = this@DialogSetPrivacy
                if (dialogSetPrivacy.appContext == null) {
                    return
                }
                if (dialogSetPrivacy.tabsDeleted) {
                    dialogSetPrivacy.settingsOpened = false
                    val tabDeletedListener = dialogSetPrivacy.deletedListener
                    if (tabDeletedListener != null) {
                        tabDeletedListener.onDeleted()
                    }
                }
                if (dialogSetPrivacy.deleteMask != 0) {
                    showToast(dialogSetPrivacy.appContext, AppR.string.deleted)
                }
                dialogSetPrivacy.dismiss()
            }
        }
        /**
         * Deletes the data chosen in the dialog (flags in [deleteMask]): 2 recent searches, 4 history, 8 MainUtil.ensureWebViewDir (site data),
         * 16 caches + download temp files, 32 normal tabs, 64 secret tabs. Runs off the main thread.
         */
        override fun run() {
            val dialog = this@DialogSetPrivacy
            val context = dialog.appContext
            if (context == null) {
                return
            }
            val flags = dialog.deleteMask
            if ((flags and 2) == 2) {
                DbBookRecent.clearAll(context)
            }
            var history = false
            if ((flags and 4) == 4) {
                DbBookHistory.clearAll(dialog.appContext)
                history = true
            }
            if ((flags and 8) == 8) {
                MainUtil.ensureWebViewDir(dialog.appContext)
            }
            if ((flags and 16) == 16) {
                val ctx = dialog.appContext
                com.mycompany.app.glide.ImageCache.clearMemory()
                try {
                    val glide = Glide.get(ctx!!)
                    require(Looper.myLooper() != Looper.getMainLooper()) { "You must call this method on a background thread" }
                    glide.clearDiskCache()
                } catch (unused2: Exception) {
                }
                MainUtil.clearCacheDirs(dialog.appContext)
                DbBookDown.clearDone(dialog.appContext)
            }
            val normalTabs = (flags and 32) == 32
            val secretTabs = (flags and 64) == 64
            val currentDeleted = (normalTabs && !SyncSettings.secretMode.value) || (secretTabs && SyncSettings.secretMode.value)
            if (normalTabs) {
                if (secretTabs) {
                    DbBookTab.clearAll(dialog.appContext)
                } else {
                    clearSecret(dialog.appContext, false)
                }
            } else if (secretTabs) {
                clearSecret(dialog.appContext, true)
            }
            if (history && normalTabs && secretTabs && dialog.appContext != null) {
                deleteWhere(DbBookIcon.get(dialog.appContext).getWritableDatabase(), "DbBookIcon_table", null, null)
            }
            dialog.tabsDeleted = currentDeleted
            dialog.ui.post(DeletedDoneStep())
        }
    }

    interface TabDeletedListener {
        fun onDeleted()
    }

    init {
        loadMask()
        setSheet {
            if (fromMenu) {
                Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp, top = 8.dp)) {
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { openSettings() }) {
                        Icon(painterResource(AppR.drawable.outline_settings_black_20), contentDescription = null)
                    }
                }
            } else {
                Spacer(Modifier.padding(top = 12.dp))
            }
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                rows.forEach { r ->
                    val on = (mask and r.bit) == r.bit
                    AppListItem(
                        headlineContent = { Text(stringResource(r.title)) },
                        supportingContent = if (r.info != 0) ({ Text(stringResource(r.info)) }) else null,
                        trailingContent = { Checkbox(checked = on, onCheckedChange = { set(r.bit, it) }, enabled = !deleting) },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        modifier = Modifier.clickable(enabled = !deleting) { set(r.bit, !on) },
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { apply() }, enabled = !deleting && (exitMode || mask != 0)) {
                    Text(stringResource(if (exitMode) AppR.string.apply else AppR.string.delete))
                }
            }
        }
        show()
    }

    private fun set(bit: Int, on: Boolean) {
        deleteMask = if (on) deleteMask or bit else deleteMask and bit.inv()
        mask = deleteMask
    }

    /** (Re)reads the mask from the prefs; the rows follow. Returns nothing now (the old adapter list is gone). */
    fun loadMask(): ArrayList<Any?>? {
        deleteMask = if (exitMode) BrowserSettings.exitDelete.value else BrowserSettings.dataDelete.value
        mask = deleteMask
        return null
    }

    private fun openSettings() {
        val activity = hostActivity ?: return
        val intent = Intent(appContext, com.mycompany.app.ui.settings.PrivacySettingsActivity::class.java)
        intent.putExtra("EXTRA_POPUP", true)
        (activity as? com.mycompany.app.main.ActivityHost)?.launchExternalForResult(intent, RequestCode.DIALOG_SETTINGS)
    }

    private fun apply() {
        val context = appContext ?: return
        if (applying) return
        applying = true
        if (exitMode) {
            if (BrowserSettings.exitDelete.value != deleteMask) {
                BrowserSettings.edit(context) { set(BrowserSettings.exitDelete, deleteMask) }
            }
            dismiss()
            return
        }
        deleting = true
        if (BrowserSettings.dataDelete.value != deleteMask) {
            BrowserSettings.edit(context) { set(BrowserSettings.dataDelete, deleteMask) }
        }
        if ((deleteMask and 4) == 4) MainUtil.clearWebData(context)
        val work = DeletedTask()
        if ((deleteMask and 8) != 8) {
            runInBackground(work)
        } else {
            // site data: WebView storage (callback on the main thread), then HTTP auth, then cookies, then the rest
            MainUtil.clearBrowsingData(ui, Runnable {
                ui.post {
                    val ctx = appContext ?: return@post
                    try {
                        android.webkit.WebViewDatabase.getInstance(ctx).clearHttpAuthUsernamePassword()
                    } catch (unused: Exception) {
                    } catch (unused: NoSuchMethodError) {
                    }
                    ui.post {
                        val appCtx = appContext ?: return@post
                        MainUtil.clearCookies(appCtx)
                        runInBackground(work)
                    }
                }
            })
        }
    }

    private fun runInBackground(runnable: Runnable) {
        val ex = MainApp.executor(appContext)
        try {
            if (ex != null) ex.execute(runnable) else Thread(runnable).start()
        } catch (unused: Exception) {
        }
    }

    override fun canCancelFromScrim(): Boolean = !deleting

    override fun dismiss() {
        if (settingsOpened) {
            settingsOpened = false
            deletedListener?.onDeleted()
        }
        hostActivity = null
        appContext = null
        deletedListener = null
        super.dismiss()
    }

    companion object {
    }
}
