package com.mycompany.app.dialog

import com.mycompany.app.main.MainActivity
import com.mycompany.app.pref.store.BrowserSettings
import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.content.Context
import android.content.DialogInterface
import android.database.Cursor
import android.os.Build
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.db.DbExport.RowWriter
import com.mycompany.app.db.DbExport.exportCsv
import com.mycompany.app.db.DbUtil.queryById
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.DbUtil.updateById
import com.mycompany.app.db.book.DbAdsCmd
import com.mycompany.app.db.book.DbBookAds
import com.mycompany.app.db.book.DbBookAgent
import com.mycompany.app.db.book.DbBookBlock
import com.mycompany.app.db.book.DbBookFilter
import com.mycompany.app.db.book.DbBookHistory
import com.mycompany.app.db.book.DbBookIcon
import com.mycompany.app.db.book.DbBookJava
import com.mycompany.app.db.book.DbBookLink
import com.mycompany.app.db.book.DbBookLocale
import com.mycompany.app.db.book.DbBookMemo
import com.mycompany.app.db.book.DbBookOver
import com.mycompany.app.db.book.DbBookPass
import com.mycompany.app.db.book.DbBookPop
import com.mycompany.app.db.book.DbBookQuick
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookRecent
import com.mycompany.app.db.book.DbBookScript
import com.mycompany.app.db.book.DbBookSearch
import com.mycompany.app.db.book.DbBookTab
import com.mycompany.app.db.book.DbBookTmem
import com.mycompany.app.db.book.DbBookTrans
import com.mycompany.app.db.book.DbBookUser
import com.mycompany.app.db.book.DbBookWeb
import com.mycompany.app.db.book.DbRecentLang
import com.mycompany.app.db.book.DbTabState
import com.mycompany.app.db.book.DbTabThumb
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.splitNumber
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.treeDisplayPath
import com.mycompany.app.main.MainUri.findChild
import com.mycompany.app.main.MainUri.externalVolumes
import com.mycompany.app.main.MainUriVol.createNumbered
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.main.MainUtil.legacyBackupKey
import com.mycompany.app.main.MainUtil.pickTreeFolder
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.editTextValue
import com.mycompany.app.main.MainUtil.backupDirPath
import com.mycompany.app.main.MainUtil.closeKeyboard
import com.mycompany.app.main.MainUtil.postRequestLayout
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.main.MainUtil.fileTimeStamp
import com.mycompany.app.main.MainUtil.clearWebData
import com.mycompany.app.main.MainUtil.newCacheFilePath
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.safeFileName
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.copyFile
import com.mycompany.app.main.MainUtil.deleteRecursive
import com.mycompany.app.pref.PrefPath
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.pref.PrefSet.remove
import com.mycompany.app.pref.PrefUtil.reversed
import com.mycompany.app.pref.PrefUtil.setDetail
import com.mycompany.app.pref.PrefWeb
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.Md5FileNameGenerator
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.model.ZipParameters
import java.io.BufferedWriter
import java.io.File
import java.lang.ref.WeakReference
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.mycompany.app.main.BackupCrypto
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Backup export (Settings > Backup > export): pick what to save (settings, quick access, bookmarks, history, tabs,
 * passwords), a file name and a folder, then [DialogTask] (unchanged) copies the databases / prefs, zips them with the
 * backup password and writes "<name>.dat" into the folder. Progress while saving, Cancel / Canceling, a message with
 * Retry when it failed or there was nothing to save; Success / Cancelled close it.
 * Compose now (on [ComposeBottomDialog]); same constructor, shows itself. [saveFolder] is still called with the folder the
 * system picker returned (request 19).
 */
@Suppress("deprecation")
class DialogBackupSave(settingBackup: android.app.Activity?) : ComposeBottomDialog(settingBackup) {
    /** Stand-in for the old check buttons: [checked] is what the task reads. */
    inner class Check {
        var checked by mutableStateOf(true)
    }

    @JvmField val mainHandler: Handler? = Handler(Looper.getMainLooper())
    @JvmField var progressMax: Int = 0
    @JvmField var progressCount: Int = 0
    @JvmField var lastProgressTime: Long = 0
    @JvmField var saveTask: DialogTask? = null
    @JvmField var mainBusy: Boolean = false
    @JvmField var cancelRequested: Boolean = false
    @JvmField var itemCount: Int = 0
    @JvmField var folderList: ArrayList<Any?>? = null
    @JvmField var folderPath: String? = null
    @JvmField var progressRunnable: Runnable
    @JvmField var saveMode: Byte
    @JvmField var hostActivity: android.app.Activity?
    @JvmField var appContext: Context?
    @JvmField var checkSetting: Check? = Check()
    @JvmField var checkQuick: Check? = Check()
    @JvmField var checkBookmark: Check? = Check()
    @JvmField var checkHistory: Check? = Check()
    @JvmField var checkTabs: Check? = Check()
    @JvmField var checkPassword: Check? = Check()
    @JvmField var checkAddress: Check? = Check()

    /** 0 = choices, 1 = saving (progress), 2 = message. */
    private var stage by mutableIntStateOf(0)
    private var progress by mutableFloatStateOf(0f)
    private var message by mutableIntStateOf(AppR.string.fail)
    private var mainLabel by mutableIntStateOf(AppR.string.save)
    private var mainOn by mutableStateOf(true)
    private var veil by mutableStateOf(false)
    private var name by mutableStateOf("")
    private var nameError by mutableStateOf(false)
    private var folder by mutableStateOf<String?>(null)
    private var folderError by mutableStateOf(false)
    private var targetError by mutableStateOf(false)
    private var password by mutableStateOf("")
    private var password2 by mutableStateOf("")
    private var passError by mutableIntStateOf(0)
    /** The password the running task encrypts with (cleared when the task ends). */
    @JvmField var pendingPassword: CharArray? = null
    private var menuOpen by mutableStateOf(false)
    private var menuItems by mutableStateOf<List<String>>(emptyList())
    private var executor: java.util.concurrent.ExecutorService? = null

    init {
        this.progressRunnable = Runnable {
            val i2 = progressMax
            if (progressCount > i2) progressCount = i2
            progress = if (i2 <= 0) 0f else progressCount.toFloat() / i2
        }
        this.saveMode = 2.toByte()
        this.hostActivity = settingBackup
        this.appContext = context
        var stamp: String? = MainUtil.fileTimeStamp(System.currentTimeMillis())
        if (!TextUtils.isEmpty(stamp) && stamp!!.endsWith(".")) {
            stamp = stamp.let { s -> s.substring(0, s.length - 1) }
        }
        name = if (TextUtils.isEmpty(stamp)) "OPUM_backup" else "OPUM_backup_$stamp"
        setSheet { Sheet() }
        show()
        runAsync(Runnable {
            val ctx = appContext ?: return@Runnable
            val list = MainUri.externalVolumes(ctx)
            folderList = list
            PrefPath.picked = MainUri.findChild(appContext, PrefPath.picked!!, list)
            folderPath = MainUri.displayPath(ctx, MainUri.storageVolume())
            mainHandler?.post { showFolder() }
        })
    }

    private fun showFolder() {
        if (TextUtils.isEmpty(MainUri.storageVolume())) {
            folder = null
        } else {
            folder = folderPath
            folderError = false
        }
    }

    @Composable
    private fun ColumnScope.Sheet() {
        when (stage) {
            0 -> Choices(Modifier.weight(1f, fill = false))
            1 -> Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp)) {
                Text(stringResource(AppR.string.saving), style = MaterialTheme.typography.bodyLarge)
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
            }
            else -> Box(Modifier.fillMaxWidth().heightIn(min = 88.dp).padding(16.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(message), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            }
        }
        if (veil) {
            LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp))
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { onMain() }, enabled = mainOn) { Text(stringResource(mainLabel)) }
        }
    }

    @Composable
    private fun Choices(modifier: Modifier) {
        Column(modifier.verticalScroll(rememberScrollState()).padding(top = 8.dp)) {
            if (targetError) {
                Text(
                    stringResource(AppR.string.backup_target), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                )
            }
            CheckRow(AppR.string.setting, checkSetting)
            CheckRow(AppR.string.quick_access, checkQuick)
            CheckRow(AppR.string.bookmark, checkBookmark)
            CheckRow(AppR.string.history, checkHistory)
            CheckRow(AppR.string.tab_item, checkTabs)
            CheckRow(AppR.string.password, checkPassword)
            CheckRow(AppR.string.addresses, checkAddress)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text(stringResource(AppR.string.name)) },
                singleLine = true,
                isError = nameError,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onEditorDone() }),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; passError = 0 },
                label = { Text(stringResource(AppR.string.backup_password)) },
                singleLine = true,
                isError = passError != 0,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                supportingText = {
                    Text(stringResource(if (passError == 1) AppR.string.backup_password_short else AppR.string.backup_password_note))
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
            )
            OutlinedTextField(
                value = password2,
                onValueChange = { password2 = it; passError = 0 },
                label = { Text(stringResource(AppR.string.backup_password_confirm)) },
                singleLine = true,
                isError = passError == 2,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onEditorDone() }),
                supportingText = if (passError == 2) ({ Text(stringResource(AppR.string.backup_password_mismatch)) }) else null,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
            )
            Box {
                Column(Modifier.fillMaxWidth().clickable { openFolders() }.padding(horizontal = 24.dp, vertical = 12.dp)) {
                    Text(stringResource(AppR.string.save_location), style = MaterialTheme.typography.bodyLarge)
                    val folderNow = folder
                    Text(
                        if (folderNow == null) stringResource(AppR.string.folder_not_selected) else folderNow,
                        style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis,
                        color = if (folderNow == null || folderError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    )
                }
                com.mycompany.app.ui.components.BubbleMenu(expanded = menuOpen, onDismiss = { menuOpen = false }) {
                    val items = menuItems
                    items.forEachIndexed { position, label ->
                        DropdownMenuItem(text = { Text(label) }, onClick = { menuOpen = false; pickFolder(position) })
                    }
                    DropdownMenuItem(
                        text = { Text(stringResource(AppR.string.direct_select)) },
                        onClick = { menuOpen = false; pickFolder(items.size) },
                    )
                }
            }
        }
    }

    @Composable
    private fun CheckRow(title: Int, check: Check?) {
        val c = check ?: return
        Row(
            Modifier.fillMaxWidth().clickable { c.checked = !c.checked; targetError = false }.padding(start = 24.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(title), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Checkbox(c.checked, { c.checked = it; targetError = false })
        }
    }

    private var outside = true

    override fun setCanceledOnTouchOutside(cancel: Boolean) {
        outside = cancel
        super.setCanceledOnTouchOutside(cancel)
    }

    override fun canCancelFromScrim(): Boolean = outside

    fun runAsync(runnable: Runnable?) {
        var ex = executor
        if (ex == null || ex.isShutdown() || ex.isTerminated()) {
            ex = MainApp.executor(context) ?: return
            executor = ex
        }
        try { ex.execute(runnable) } catch (unused: Exception) { }
    }

    /** Folder row: the known folders plus "direct select", or straight to the picker. */
    private fun openFolders() {
        val list = folderList
        val ctx = appContext ?: return
        if (list == null || list.isEmpty()) {
            pickTreeFolder(hostActivity, MainUri.storageVolume())
            return
        }
        menuItems = list.map { MainUri.volumeLabel(ctx, it as String?) }
        menuOpen = true
    }

    private fun pickFolder(size: Int) {
        val list = folderList
        if (list == null || size >= list.size) {
            pickTreeFolder(hostActivity, MainUri.storageVolume())
            return
        }
        saveFolder(list[size] as String?)
    }

    /** Save / Cancel / Retry. */
    private fun onMain() {
        if (mainBusy) return
        mainBusy = true
        mainHandler?.post {
            if (saveTask != null) cancelSave() else startSave()
            mainBusy = false
        }
    }

    /** Done on the keyboard. */
    private fun onEditorDone() {
        if (mainBusy || stage != 0) return
        mainBusy = true
        mainHandler?.post {
            startSave()
            mainBusy = false
        }
    }

    class DialogTask(dialogBackupSave: DialogBackupSave?, name: String?, enabled: Boolean) : MyAsyncTask() {
        @JvmField var dialogRef: WeakReference<*>? = WeakReference<Any?>(dialogBackupSave)
        @JvmField var name: String? = name
        @JvmField var saved: Boolean = false

        init {
            val d = dialogBackupSave
            if (d != null) {
                d.cancelRequested = false
                d.itemCount = 0
                d.stage = 1
                d.applyMainOn(true)
                d.mainLabel = AppR.string.cancel
                d.setCanceledOnTouchOutside(false)
                d.progressCount = 0
                var i: Int
                val myButtonCheck = d.checkSetting
                if (myButtonCheck == null) {
                    i = 0
                } else {
                    var z2: Boolean
                    var z3 = true
                    var i2 = if (myButtonCheck.checked) 18 else 0
                    i2 = if (d.checkQuick!!.checked) i2 + 1 else i2
                    i2 = if (d.checkBookmark!!.checked) i2 + 1 else i2
                    if (d.checkHistory!!.checked) {
                        i2++
                        z2 = true
                    } else {
                        z2 = false
                    }
                    if (d.checkTabs!!.checked) {
                        i2 += 3
                    } else {
                        z3 = z2
                    }
                    i2 = if (z3) i2 + 1 else i2
                    i = (if (d.checkPassword!!.checked) i2 + 1 else i2) + (if (d.checkAddress!!.checked) 1 else 0) + 5
                }
                d.progressMax = i
                d.lastProgressTime = 0L
                d.progress = 0f
                d.applyVeil(false)
            }
        }

        /**
         * Saves a backup: copies the selected databases and preference files into a temp folder, adds the four
         * F() exports, zips the folder to "<dir>2", encrypts it with the backup password and writes it into the chosen
         * folder through MainUri (any document-tree folder, Google Drive included, via the system picker).
         * Cancelling (D()) stops immediately; h holds the result.
        </dir> */
        public override fun doInBackground() {
            val dialog = this.dialogRef!!.get() as DialogBackupSave?
            if (dialog == null || this.cancelled) {
                return
            }
            val name = this.name!!
            val context = dialog.appContext
            var saved = false
            if (dialog.checkSetting == null) {
                this.saved = false
                return
            }
            val dir = MainUtil.newCacheFilePath(context)
            if (TextUtils.isEmpty(dir)) {
                this.saved = false
                return
            }
            val tempDir = File(dir!!)
            MainUtil.deleteRecursive(tempDir)
            if (!tempDir.mkdirs()) {
                this.saved = false
                return
            }
            var zipPath: String? = null
            try {
                val list: ArrayList<Any?> = ArrayList<Any?>()
                if (dialog.checkSetting!!.checked) {
                    for (index in 0..17) {
                        val file = dialog.exportDb(context, dir, dbNames[index])
                        if (file != null) {
                            list.add(file)
                        }
                        if (dialog.isCancelled()) {
                            this.saved = false
                            return
                        }
                        dialog.stepProgress()
                    }
                }
                if (dialog.checkQuick!!.checked) {
                    if (!addCounted(dialog, context, dir, "DbBookQuick.db", list)) {
                        return
                    }
                }
                if (dialog.checkBookmark!!.checked) {
                    if (!addCounted(dialog, context, dir, "DbBookWeb.db", list)) {
                        return
                    }
                }
                var withIcons = false
                if (dialog.checkHistory!!.checked) {
                    val file = dialog.exportDb(context, dir, "DbBookHistory.db")
                    if (file != null) {
                        list.add(file)
                        withIcons = true
                    } else {
                        dialog.itemCount++
                    }
                    if (dialog.isCancelled()) {
                        this.saved = false
                        return
                    }
                    dialog.stepProgress()
                }
                if (dialog.checkTabs!!.checked) {
                    val file = dialog.exportDb(context, dir, "DbBookTab3.db")
                    val tabs = file != null
                    if (tabs) {
                        list.add(file)
                        withIcons = true
                    } else {
                        dialog.itemCount++
                    }
                    if (dialog.isCancelled()) {
                        this.saved = false
                        return
                    }
                    dialog.stepProgress()
                    if (tabs) {
                        for (extra in arrayOf<String?>("DbTabState.db")) {
                            val fileValue = dialog.exportDb(context, dir, extra)
                            if (fileValue != null) {
                                list.add(fileValue)
                            }
                            if (dialog.isCancelled()) {
                                this.saved = false
                                return
                            }
                            dialog.stepProgress()
                        }
                    }
                }
                if (withIcons) {
                    val file = dialog.exportDb(context, dir, "DbBookIcon.db")
                    if (file != null) {
                        list.add(file)
                    }
                    if (dialog.isCancelled()) {
                        this.saved = false
                        return
                    }
                    dialog.stepProgress()
                }
                if (dialog.checkPassword!!.checked) {
                    if (!addCounted(dialog, context, dir, "DbBookPass.db", list)) {
                        return
                    }
                }
                if (dialog.checkAddress!!.checked) {
                    if (!addCounted(dialog, context, dir, com.mycompany.app.db.book.DbBookAddr.NAME, list)) {
                        return
                    }
                }
                if (list.isEmpty()) {
                    this.saved = false
                    return
                }
                var prefs: Array<File>? = null
                if (context != null) {
                    val prefDir = File(context.getFilesDir(), ".pref")
                    if (prefDir.isDirectory()) {
                        prefs = prefDir.listFiles()
                    }
                }
                // the settings files only with "Settings" (2026-11-25: they were always saved); the workspace list always,
                // it belongs to the data (which rows are which workspace's)
                val withSettings = dialog.checkSetting!!.checked
                if (prefs != null) {
                    for (pref in prefs) {
                        val prefName = pref.getName()
                        if (TextUtils.isEmpty(prefName)) {
                            continue
                        }
                        if (!withSettings && prefName != "PrefSpace") {
                            continue
                        }
                        for (index in secretTables.indices) {
                            if (prefName == secretTables[index]) {
                                var copy: File? = copyHashed(pref, dir, prefName)
                                // the sync passwords go into the backup opened, so it restores on any phone (see PassCrypt)
                                if (copy != null && prefName == com.mycompany.app.pref.PrefBookSync.NAME) {
                                    try { com.mycompany.app.sync.BookmarkSyncStore.openAllIn(copy) } catch (_: Exception) { copy.delete(); copy = null }
                                }
                                if (copy != null) {
                                    list.add(copy)
                                }
                                if (dialog.isCancelled()) {
                                    this.saved = false
                                    return
                                }
                                break
                            }
                        }
                    }
                }
                dialog.stepProgress()
                if (list.isEmpty()) {
                    this.saved = false
                    return
                }
                val exports = arrayOf<BooleanArray>(
                    booleanArrayOf(false, false),
                    booleanArrayOf(false, true),
                    booleanArrayOf(true, false),
                    booleanArrayOf(true, true)
                )
                for (export in exports) {
                    // the start page's background pictures are settings too
                    if (!withSettings) break
                    val file: File? = copyBackImage(context, dir, export[0], export[1])
                    if (file != null) {
                        list.add(file)
                    }
                    if (dialog.isCancelled()) {
                        this.saved = false
                        return
                    }
                }
                dialog.stepProgress()
                zipPath = dir + "2"
                val zip = File(zipPath)
                if (zip.exists()) {
                    zip.delete()
                }
                val zipped = dialog.zipFiles(list, zipPath)
                val secret = dialog.pendingPassword
                var outPath: String? = zipPath
                if (zipped && secret != null) {
                    outPath = zipPath + "e"
                    if (!BackupCrypto.encrypt(zipPath, outPath, secret)) {
                        this.saved = false
                        return
                    }
                }
                try {
                    dialog.stepProgress()
                    val item = MainUri.createUri(context, MainUri.storageVolume(), null, name)
                    if (item != null && zipped) {
                        saved = MainUri.moveUri(context!!, outPath, item.uriString, false, true)
                    }
                } catch (exception: Exception) {
                    saved = false
                }
                if (outPath != zipPath) {
                    File(outPath).delete()
                }
                dialog.stepProgress()
            } catch (ignored: Throwable) {
            }
            MainUtil.deleteRecursive(tempDir)
            if (!TextUtils.isEmpty(zipPath)) {
                File(zipPath!!).delete()
            }
            dialog.stepProgress()
            this.saved = saved
        }

        /** Adds one database copy (counting a failure); returns false when cancelled.  */
        fun addCounted(
            dialog: DialogBackupSave?,
            context: Context?,
            dir: String?,
            db: String?,
            list: ArrayList<Any?>?
        ): Boolean {
            val file = dialog!!.exportDb(context, dir, db)
            if (file != null) {
                list!!.add(file)
            } else {
                dialog.itemCount++
            }
            if (dialog.isCancelled()) {
                this.saved = false
                return false
            }
            dialog.stepProgress()
            return true
        }

        public override fun onCancelled() {
            val d = this.dialogRef!!.get() as DialogBackupSave? ?: return
            d.saveTask = null
            showToast(d.appContext, AppR.string.cancelled)
            d.dismiss()
        }

        public override fun onPostExecute() {
            val d = this.dialogRef!!.get() as DialogBackupSave? ?: return
            d.saveTask = null
            if (d.isCancelled()) {
                showToast(d.appContext, AppR.string.cancelled)
                d.dismiss()
                return
            }
            if (this.saved) {
                showToast(d.appContext, AppR.string.success)
                d.dismiss()
                return
            }
            if (d.appContext == null) {
                return
            }
            d.applyVeil(false)
            val myButtonCheck = d.checkSetting
            if (myButtonCheck == null || myButtonCheck.checked) {
                d.showMessage(AppR.string.fail)
            } else {
                val skipped = (if (d.checkQuick!!.checked) 1 else 0) + (if (d.checkBookmark!!.checked) 1 else 0) + (if (d.checkHistory!!.checked) 1 else 0) +
                    (if (d.checkTabs!!.checked) 1 else 0) + (if (d.checkPassword!!.checked) 1 else 0) + (if (d.checkAddress!!.checked) 1 else 0)
                d.showMessage(if (skipped == d.itemCount) AppR.string.save_empty else AppR.string.fail)
            }
            d.mainLabel = AppR.string.retry
            d.applyMainOn(true)
            d.setCanceledOnTouchOutside(true)
        }
    }

    fun exportDb(context: Context?, dir: String?, extra: String?): File? {
        if (this.saveMode.toInt() == 2) {
            if (context == null) {
                return null
            }
            val dbFile = context.getDatabasePath(extra)
            if (dbFile == null || !dbFile.exists()) {
                return null
            }
            val copy = compactCopy(dbFile, dir) ?: copyHashed(dbFile, dir, dbFile.getName())
            // saved passwords go into the backup opened, so it restores on any phone (see PassCrypt)
            if (copy != null && "DbBookPass.db" == extra) {
                try { DbBookPass.openAllIn(copy) } catch (_: Exception) { copy.delete(); return null }
            }
            if (copy != null && com.mycompany.app.db.book.DbBookAddr.NAME == extra) {
                try { com.mycompany.app.db.book.DbBookAddr.openAllIn(copy) } catch (_: Exception) { copy.delete(); return null }
            }
            return copy
        }
        if (context == null) {
            return null
        }
        val dbPath = context.getDatabasePath(extra)
        if (dbPath == null || TextUtils.isEmpty(dbPath.getName())) {
            return null
        }
        val md5FileNameGenerator = dir + "/" + Md5FileNameGenerator().generate(dbPath.getName())
        if ("DbAdsCmd.db" == extra) {
            if (TextUtils.isEmpty(md5FileNameGenerator)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = query(
                    DbAdsCmd.get(context).getWritableDatabase(),
                    "DbAdsCmd_table",
                    null,
                    null,
                    null,
                    null
                )
            } catch (unused: Exception) {
                cursor = null
            }
            return exportCsv(cursor, md5FileNameGenerator, RowWriter { bufferedWriter: BufferedWriter?, cursor2: Cursor? ->
                MainUtil.writeLineMark(bufferedWriter!!)
                        && MainUtil.writeStringEntry(bufferedWriter, "_host", cursor2!!.getString(cursor2.getColumnIndex("_host")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_ele", cursor2.getString(cursor2.getColumnIndex("_ele")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_doc", cursor2.getString(cursor2.getColumnIndex("_doc")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_rsv1", cursor2.getString(cursor2.getColumnIndex("_rsv1")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_rsv2", cursor2.getString(cursor2.getColumnIndex("_rsv2")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_time", cursor2.getLong(cursor2.getColumnIndex("_time")))
            })
        } else if ("DbBookAds.db" == extra) {
            if (TextUtils.isEmpty(md5FileNameGenerator)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = query(
                    DbBookAds.get(context).getWritableDatabase(),
                    "DbBookAds_table",
                    null,
                    null,
                    null,
                    null
                )
            } catch (unused: Exception) {
                cursor = null
            }
            return exportCsv(cursor, md5FileNameGenerator, RowWriter { bufferedWriter: BufferedWriter?, cursor2: Cursor? ->
                MainUtil.writeLineMark(bufferedWriter!!)
                        && MainUtil.writeStringEntry(bufferedWriter, "_path", cursor2!!.getString(cursor2.getColumnIndex("_path")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_title", cursor2.getString(cursor2.getColumnIndex("_title")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_time", cursor2.getLong(cursor2.getColumnIndex("_time")))
            })
        } else if ("DbBookAgent.db" == extra) {
            if (TextUtils.isEmpty(md5FileNameGenerator)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = query(
                    DbBookAgent.get(context).getWritableDatabase(),
                    "DbBookAgent_table",
                    null,
                    null,
                    null,
                    null
                )
            } catch (unused: Exception) {
                cursor = null
            }
            return exportCsv(cursor, md5FileNameGenerator, RowWriter { bufferedWriter: BufferedWriter?, cursor2: Cursor? ->
                MainUtil.writeLineMark(bufferedWriter!!)
                        && MainUtil.writeStringEntry(bufferedWriter, "_title", cursor2!!.getString(cursor2.getColumnIndex("_title")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_text", cursor2.getString(cursor2.getColumnIndex("_text")))
            })
        } else if ("DbBookBlock.db" == extra) {
            if (TextUtils.isEmpty(md5FileNameGenerator)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = query(
                    DbBookBlock.get(context).getWritableDatabase(),
                    "DbBookBlock_table",
                    null,
                    null,
                    null,
                    null
                )
            } catch (unused: Exception) {
                cursor = null
            }
            return exportCsv(cursor, md5FileNameGenerator, RowWriter { bufferedWriter: BufferedWriter?, cursor2: Cursor? ->
                MainUtil.writeLineMark(bufferedWriter!!)
                        && MainUtil.writeIntEntry(bufferedWriter, "_type", cursor2!!.getInt(cursor2.getColumnIndex("_type")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_path", cursor2.getString(cursor2.getColumnIndex("_path")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_image", cursor2.getString(cursor2.getColumnIndex("_image")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_time", cursor2.getLong(cursor2.getColumnIndex("_time")))
            })
        } else if ("DbBookFilter.db" == extra) {
            if (TextUtils.isEmpty(md5FileNameGenerator)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = query(
                    DbBookFilter.get(context).getWritableDatabase(),
                    "DbBookFilter_table",
                    null,
                    null,
                    null,
                    null
                )
            } catch (unused: Exception) {
                cursor = null
            }
            return exportCsv(cursor, md5FileNameGenerator, RowWriter { bufferedWriter: BufferedWriter?, cursor2: Cursor? ->
                MainUtil.writeLineMark(bufferedWriter!!)
                        && MainUtil.writeStringEntry(bufferedWriter, "_path", cursor2!!.getString(cursor2.getColumnIndex("_path")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_title", cursor2.getString(cursor2.getColumnIndex("_title")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_time", cursor2.getLong(cursor2.getColumnIndex("_time")))
                        && MainUtil.writeIntEntry(bufferedWriter, "_use", cursor2.getInt(cursor2.getColumnIndex("_use")))
            })
        } else if ("DbBookJava.db" == extra) {
            if (TextUtils.isEmpty(md5FileNameGenerator)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = query(
                    DbBookJava.get(context).getWritableDatabase(),
                    "DbBookJava_table",
                    null,
                    null,
                    null,
                    null
                )
            } catch (unused: Exception) {
                cursor = null
            }
            return exportCsv(cursor, md5FileNameGenerator, RowWriter { bufferedWriter: BufferedWriter?, cursor2: Cursor? ->
                MainUtil.writeLineMark(bufferedWriter!!)
                        && MainUtil.writeStringEntry(bufferedWriter, "_path", cursor2!!.getString(cursor2.getColumnIndex("_path")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_title", cursor2.getString(cursor2.getColumnIndex("_title")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_time", cursor2.getLong(cursor2.getColumnIndex("_time")))
            })
        } else if ("DbBookLink.db" == extra) {
            if (TextUtils.isEmpty(md5FileNameGenerator)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = query(
                    DbBookLink.get(context).getWritableDatabase(),
                    "DbBookLink_table",
                    null,
                    null,
                    null,
                    null
                )
            } catch (unused: Exception) {
                cursor = null
            }
            return exportCsv(cursor, md5FileNameGenerator, RowWriter { bufferedWriter: BufferedWriter?, cursor2: Cursor? ->
                MainUtil.writeLineMark(bufferedWriter!!)
                        && MainUtil.writeStringEntry(bufferedWriter, "_path", cursor2!!.getString(cursor2.getColumnIndex("_path")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_title", cursor2.getString(cursor2.getColumnIndex("_title")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_time", cursor2.getLong(cursor2.getColumnIndex("_time")))
            })
        } else if ("DbBookLocale.db" == extra) {
            if (TextUtils.isEmpty(md5FileNameGenerator)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = query(
                    DbBookLocale.get(context).getWritableDatabase(),
                    "DbBookLocale_table",
                    null,
                    null,
                    null,
                    null
                )
            } catch (unused: Exception) {
                cursor = null
            }
            return exportCsv(cursor, md5FileNameGenerator, RowWriter { bufferedWriter: BufferedWriter?, cursor2: Cursor? ->
                MainUtil.writeLineMark(bufferedWriter!!)
                        && MainUtil.writeStringEntry(bufferedWriter, "_code", cursor2!!.getString(cursor2.getColumnIndex("_code")))
                        && MainUtil.writeIntEntry(bufferedWriter, "_index", cursor2.getInt(cursor2.getColumnIndex("_index")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_name", cursor2.getString(cursor2.getColumnIndex("_name")))
            })
        } else if ("DbBookMemo.db" == extra) {
            if (TextUtils.isEmpty(md5FileNameGenerator)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = query(
                    DbBookMemo.get(context).getWritableDatabase(),
                    "DbBookMemo_table",
                    null,
                    null,
                    null,
                    null
                )
            } catch (unused: Exception) {
                cursor = null
            }
            return exportCsv(cursor, md5FileNameGenerator, RowWriter { bufferedWriter: BufferedWriter?, cursor2: Cursor? ->
                MainUtil.writeLineMark(bufferedWriter!!)
                        && MainUtil.writeStringEntry(bufferedWriter, "_text", cursor2!!.getString(cursor2.getColumnIndex("_text")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_time", cursor2.getLong(cursor2.getColumnIndex("_time")))
                        && MainUtil.writeIntEntry(bufferedWriter, "_order", cursor2.getInt(cursor2.getColumnIndex("_order")))
            })
        } else if ("DbBookOver.db" == extra) {
            if (TextUtils.isEmpty(md5FileNameGenerator)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = query(
                    DbBookOver.get(context).getWritableDatabase(),
                    "DbBookOver_table",
                    null,
                    null,
                    null,
                    null
                )
            } catch (unused: Exception) {
                cursor = null
            }
            return exportCsv(cursor, md5FileNameGenerator, RowWriter { bufferedWriter: BufferedWriter?, cursor2: Cursor? ->
                MainUtil.writeLineMark(bufferedWriter!!)
                        && MainUtil.writeStringEntry(bufferedWriter, "_path", cursor2!!.getString(cursor2.getColumnIndex("_path")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_title", cursor2.getString(cursor2.getColumnIndex("_title")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_time", cursor2.getLong(cursor2.getColumnIndex("_time")))
            })
        } else if ("DbBookPop.db" == extra) {
            if (TextUtils.isEmpty(md5FileNameGenerator)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = query(
                    DbBookPop.get(context).getWritableDatabase(),
                    "DbBookPop_table",
                    null,
                    null,
                    null,
                    null
                )
            } catch (unused: Exception) {
                cursor = null
            }
            return exportCsv(cursor, md5FileNameGenerator, RowWriter { bufferedWriter: BufferedWriter?, cursor2: Cursor? ->
                MainUtil.writeLineMark(bufferedWriter!!)
                        && MainUtil.writeStringEntry(bufferedWriter, "_path", cursor2!!.getString(cursor2.getColumnIndex("_path")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_title", cursor2.getString(cursor2.getColumnIndex("_title")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_time", cursor2.getLong(cursor2.getColumnIndex("_time")))
            })
        } else if ("DbBookRecent.db" == extra) {
            if (TextUtils.isEmpty(md5FileNameGenerator)) {
                return null
            }
            var cursor: Cursor?
            try {
                cursor = query(
                    DbBookRecent.get(context).getWritableDatabase(),
                    "DbBookRecent_table",
                    null,
                    null,
                    null,
                    null
                )
            } catch (unused: Exception) {
                cursor = null
            }
            return exportCsv(cursor, md5FileNameGenerator, RowWriter { bufferedWriter: BufferedWriter?, cursor2: Cursor? ->
                MainUtil.writeLineMark(bufferedWriter!!)
                        && MainUtil.writeIntEntry(bufferedWriter, "_secret", cursor2!!.getInt(cursor2.getColumnIndex("_secret")))
                        && MainUtil.writeStringEntry(bufferedWriter, "_title", cursor2.getString(cursor2.getColumnIndex("_title")))
                        && MainUtil.writeLongEntry(bufferedWriter, "_time", cursor2.getLong(cursor2.getColumnIndex("_time")))
            })
        } else if ("DbBookScript.db" == extra) {
            return DbBookScript.exportFile(context, md5FileNameGenerator)
        } else if ("DbBookSearch.db" == extra) {
            return DbBookSearch.exportFile(context, md5FileNameGenerator)
        } else if ("DbBookTmem.db" == extra) {
            return DbBookTmem.exportFile(context, md5FileNameGenerator)
        } else if ("DbBookTrans.db" == extra) {
            return DbBookTrans.exportFile(context, md5FileNameGenerator)
        } else if ("DbBookUser.db" == extra) {
            return DbBookUser.exportFile(context, md5FileNameGenerator)
        } else if ("DbRecentLang.db" == extra) {
            return DbRecentLang.exportFile(context, md5FileNameGenerator)
        } else if ("DbBookHistory.db" == extra) {
            return DbBookHistory.exportFile(context, md5FileNameGenerator)
        } else if ("DbBookIcon.db" == extra) {
            return DbBookIcon.exportFile(context, md5FileNameGenerator)
        } else if ("DbBookPass.db" == extra) {
            return DbBookPass.exportFile(context, md5FileNameGenerator)
        } else if ("DbBookQuick.db" == extra) {
            return DbBookQuick.exportFile(context, md5FileNameGenerator)
        } else if ("DbBookTab3.db" == extra) {
            return DbBookTab.exportFile(context, md5FileNameGenerator)
        } else if ("DbBookWeb.db" == extra) {
            return DbBookWeb.exportFile(context, md5FileNameGenerator)
        } else if ("DbTabState.db" == extra) {
            return DbTabState.exportFile(context, md5FileNameGenerator)
        } else if ("DbTabThumb.db" == extra) {
            return DbTabThumb.exportFile(context, md5FileNameGenerator)
        }
        return null
    }

    fun isCancelled(): Boolean {
        if (this.cancelRequested) {
            return true
        }
        val dialogTask = this.saveTask
        return dialogTask != null && dialogTask.cancelled
    }

    /** Cancel while saving (or close). */
    fun cancelSave() {
        if (this.saveTask == null) {
            dismiss()
            return
        }
        applyVeil(true)
        showMessage(AppR.string.canceling)
        applyMainOn(false)
        this.cancelRequested = true
        this.saveTask?.cancelled = true
        this.saveTask = null
    }

    /** Checks the folder, name and targets, then starts the task. */
    fun startSave() {
        if (this.appContext == null) {
            return
        }
        if (TextUtils.isEmpty(MainUri.storageVolume())) {
            folderError = true
            showToast(this.appContext, AppR.string.select_dir)
            return
        }
        val trimmedName = name.trim { it <= ' ' }
        if (TextUtils.isEmpty(trimmedName)) {
            nameError = true
            showToast(this.appContext, AppR.string.input_name)
            return
        }
        if (trimmedName.toByteArray().size > 200) {
            nameError = true
            showToast(this.appContext, AppR.string.long_name)
            return
        }
        if (!this.checkSetting!!.checked && !this.checkQuick!!.checked && !this.checkBookmark!!.checked && !this.checkHistory!!.checked && !this.checkTabs!!.checked && !this.checkPassword!!.checked && !this.checkAddress!!.checked) {
            targetError = true
            showToast(this.appContext, AppR.string.backup_target)
            return
        }
        if (password.length < BackupCrypto.MIN_PASSWORD) {
            passError = 1
            showToast(this.appContext, AppR.string.backup_password_short)
            return
        }
        if (password != password2) {
            passError = 2
            showToast(this.appContext, AppR.string.backup_password_mismatch)
            return
        }
        pendingPassword = password.toCharArray()
        val fileName = MainUtil.safeFileName(trimmedName + ".dat")
        closeKeyboard(this.appContext, window?.decorView)
        this.saveTask?.cancelled = true
        this.saveTask = null
        val dialogTask = DialogTask(this, fileName!!, false)
        this.saveTask = dialogTask
        dialogTask.start(this.appContext)
    }

    /** Main button on / off. */
    fun applyMainOn(newMainOn: Boolean) {
        mainOn = newMainOn
    }

    /** Busy bar on / off. */
    fun applyVeil(newVeil: Boolean) {
        veil = newVeil
    }

    fun showMessage(newMessage: Int) {
        message = newMessage
        stage = 2
    }

    fun stepProgress() {
        val progressMax = this.progressMax
        if (progressMax != 0) {
            val progressCount = this.progressCount + 1
            this.progressCount = progressCount
            if (progressCount < progressMax) {
                val now = System.currentTimeMillis()
                if (now - this.lastProgressTime < 200) {
                    return
                } else {
                    this.lastProgressTime = now
                }
            }
            val handler = this.mainHandler ?: return
            val runnable = this.progressRunnable
            handler.removeCallbacks(runnable)
            handler.post(runnable)
        }
    }

    /** Folder chosen (from the menu or the system picker). */
    fun saveFolder(picked: String?) {
        if (TextUtils.isEmpty(picked) || picked == PrefPath.picked) {
            return
        }
        PrefPath.picked = picked
        runAsync(Runnable {
            val ctx = appContext ?: return@Runnable
            val volume = MainUri.storageVolume()
            PrefSet.putDownUri(ctx, volume)
            folderPath = MainUri.displayPath(ctx, volume)
            mainHandler?.post { showFolder() }
        })
    }

    @Throws(Throwable::class)
    fun zipFiles(arrayList: ArrayList<Any?>?, zipPath: String?): Boolean {
        if (!arrayList!!.isEmpty()) {
            try {
                val zipFile = ZipFile(zipPath)
                zipFile.setFileNameCharset(MainConst.defaultCharset)
                val progressMonitor = zipFile.progressMonitor
                val zipParameters = ZipParameters()
                zipParameters.compressionMethod = 8
                zipParameters.compressionLevel = 9
                // The archive is encrypted as a whole afterwards with the user's password (BackupCrypto).
                zipParameters.isEncryptFiles = false
                zipFile.addFiles(arrayList, zipParameters)
                if (progressMonitor.result != 2) {
                    return true
                }
            } catch (unused: Exception) {
            }
        }
        return false
    }

    override fun cancel() {
        cancelSave()
    }

    override fun dismiss() {
        pendingPassword?.fill(' ')
        pendingPassword = null
        this.saveTask?.cancelled = true
        this.saveTask = null
        this.mainHandler?.removeCallbacks(this.progressRunnable)
        this.checkSetting = null
        this.checkQuick = null
        this.checkBookmark = null
        this.checkHistory = null
        this.checkTabs = null
        this.checkPassword = null
        this.checkAddress = null
        this.hostActivity = null
        this.appContext = null
        this.folderList = null
        this.folderPath = null
        super.dismiss()
    }

    companion object {
        @JvmField
        val secretTables: Array<String?> = arrayOf<String?>(
            "PrefAlbum",
            "PrefEditor",
            "PrefFloat",
            "PrefImage",
            "PrefList",
            "PrefMain",
            "PrefPdf",
            "PrefRead",
            "PrefSecret",
            "PrefSync",
            "PrefTts",
            "PrefVideo",
            "PrefWeb",
            "PrefZone",
            "PrefZtwo",
            "PrefZtri",
            // the workspaces (2026-11-24): they stay until the user deletes them, so backups carry them
            "PrefSpace",
            // where the workspaces sync their bookmarks (2026-11-28), with the settings
            com.mycompany.app.pref.PrefBookSync.NAME
        )
        @JvmField
        val secretTablesAll: Array<String?> =
            arrayOf<String?>("back.jpg", "nor_land.jpg", "sec_port.jpg", "sec_land.jpg")
        @JvmField
        val dbNames: Array<String?> = arrayOf<String?>(
            "DbAdsCmd.db",
            "DbBookAds.db",
            "DbBookAgent.db",
            "DbBookBlock.db",
            "DbBookFilter.db",
            "DbBookJava.db",
            "DbBookLink.db",
            "DbBookLocale.db",
            "DbBookMemo.db",
            "DbBookOver.db",
            "DbBookPop.db",
            "DbBookRecent.db",
            "DbBookScript.db",
            "DbBookSearch.db",
            "DbBookTmem.db",
            "DbBookTrans.db",
            "DbBookUser.db",
            "DbRecentLang.db"
        )
        @JvmField
        val prefNames: Array<String?> = arrayOf<String?>(
            "DbBookHistory.db",
            "DbBookIcon.db",
            "DbBookPass.db",
            "DbBookAddr.db",
            "DbBookQuick.db",
            "DbBookTab3.db",
            "DbBookWeb.db",
            "DbTabState.db",
            "DbTabThumb.db"
        )

        /** Copies a database with VACUUM INTO, which drops free pages and rebuilds indexes compactly. */
        @JvmStatic
        fun compactCopy(dbFile: File, dir: String?): File? {
            val dest = File(dir + "/" + Md5FileNameGenerator().generate(dbFile.getName()))
            dest.delete()
            return try {
                val db = android.database.sqlite.SQLiteDatabase.openDatabase(
                    dbFile.getPath(), null, android.database.sqlite.SQLiteDatabase.OPEN_READWRITE,
                )
                try {
                    db.execSQL("VACUUM INTO ?", arrayOf<Any>(dest.getPath()))
                } finally {
                    db.close()
                }
                if (dest.length() > 0) dest else null
            } catch (exception: Exception) {
                dest.delete()
                null
            }
        }

        @JvmStatic
        fun copyHashed(file: File?, dir: String?, prefName: String?): File? {
            if (TextUtils.isEmpty(prefName)) {
                return null
            }
            val sbV = StringBuilder().append(dir).append("/")
            sbV.append(Md5FileNameGenerator().generate(prefName))
            val path = sbV.toString()
            if (MainUtil.copyFile(file!!.getPath(), path)) {
                return File(path)
            }
            return null
        }

        @JvmStatic
        fun copyBackImage(context: Context?, dir: String?, secret: Boolean, withName: Boolean): File? {
            if (secret) {
                if (!BrowserSettings.quickBackSecret.value) {
                    return null
                }
            } else if (!BrowserSettings.quickBack.value) {
                return null
            }
            try {
                val backDir = backupDirPath(context, secret, withName)
                if (TextUtils.isEmpty(backDir)) {
                    return null
                }
                val file = File(backDir!!)
                if (file.exists()) {
                    return copyHashed(file, dir, file.getName())
                }
                return null
            } catch (unused: Exception) {
                return null
            }
        }
    }
}
