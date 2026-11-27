package com.mycompany.app.dialog

import com.mycompany.app.main.MainActivity
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.ImageSettings
import com.mycompany.app.main.MainUtil
import com.mycompany.app.db.DbUtil
import com.mycompany.app.script.Script
import com.mycompany.app.db.book.DbBookQuick
import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap.CompressFormat
import android.net.VpnService
import android.text.TextUtils
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.compress.CompressUtilZip
import com.mycompany.app.compress.CompressUtilZip.Companion.canOpen
import com.mycompany.app.compress.CompressUtilZip2
import com.mycompany.app.compress.CompressUtilZip2.Companion.extractStream
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.DbUtil.queryById
import com.mycompany.app.db.DbUtil.query
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
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookRecent
import com.mycompany.app.db.book.DbBookScript
import com.mycompany.app.db.book.DbBookScript.Companion.saveScript
import com.mycompany.app.db.book.DbBookSearch
import com.mycompany.app.db.book.DbBookTab
import com.mycompany.app.db.book.DbBookTabOld
import com.mycompany.app.db.book.DbBookTabOld.Companion.readTabs
import com.mycompany.app.db.book.DbBookTmem
import com.mycompany.app.db.book.DbBookTrans
import com.mycompany.app.db.book.DbBookUser
import com.mycompany.app.db.book.DbBookWeb
import com.mycompany.app.db.book.DbRecentLang
import com.mycompany.app.db.book.DbTabState
import com.mycompany.app.db.book.DbTabThumb
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUri.splitNumber
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.findChild
import com.mycompany.app.main.MainUri.externalVolumes
import com.mycompany.app.main.MainUriVol.createNumbered
import com.mycompany.app.main.MainUtil.legacyBackupKey
import com.mycompany.app.main.MainUtil.parseColorSafe
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.imageTypeOf
import com.mycompany.app.main.MainUtil.dataUriExt
import com.mycompany.app.main.MainUtil.extOf
import com.mycompany.app.main.MainUtil.postRequestLayout
import com.mycompany.app.main.MainUtil.openFileInput
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.openFileOutput
import com.mycompany.app.main.MainUtil.isBitmapOk
import com.mycompany.app.main.MainUtil.decodeDataUri
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.main.MainUtil.newCacheFilePath
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.main.MainUtil.copyFile
import com.mycompany.app.main.MainUtil.deleteRecursive
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.pref.PrefImage
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.pref.PrefUtil.reversed
import com.mycompany.app.pref.PrefUtil.setDetail
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefZone.Companion.load
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.script.Script.Companion.parse
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.Md5FileNameGenerator
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.model.FileHeader
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.util.Locale
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.mycompany.app.main.BackupCrypto
import androidx.compose.ui.unit.dp
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Backup import (Settings > Backup > import, [backupPath] the picked file): pick what to restore (settings, quick access,
 * bookmarks, history, tabs, passwords), then [DialogTask] (unchanged) unzips the backup and restores the chosen parts.
 * Loading / progress while it runs, Cancel / Canceling; afterwards "restart the app" (and [dialogApplyListener]),
 * "not changed", the "backup changed" note or Fail + Retry. Cancelled closes it.
 * Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogBackupLoad(
    settingBackup: android.app.Activity?,
    backupPath: String?,
    dialogApplyListener: DialogApplyListener?
) : ComposeBottomDialog(settingBackup) {
    /** Stand-in for the old check buttons: [checked] is what the task reads. */
    inner class Check {
        var checked by mutableStateOf(true)
    }

    @JvmField val mainHandler: Handler? = Handler(Looper.getMainLooper())
    @JvmField var loadTask: DialogTask? = null
    @JvmField var mainBusy: Boolean = false
    @JvmField var backupPath: String? = null
    @JvmField var zipPath: String? = null
    @JvmField var unpackDir: String? = null
    @JvmField var cancelRequested: Boolean = false
    @JvmField var dataChanged: Boolean = false
    @JvmField var finished: Boolean = false
    @JvmField var scriptLang: String? = null
    @JvmField var scriptLocale: String? = null
    @JvmField var backImageSet: Boolean = false
    @JvmField var secretImageSet: Boolean = false
    @JvmField var totalFiles: Int = 0
    @JvmField var progressRunnable: Runnable? = null
    @JvmField var appContext: Context? = null
    @JvmField var applyListener: DialogApplyListener? = null
    @JvmField var checkSetting: Check? = Check()
    @JvmField var checkQuick: Check? = Check()
    @JvmField var checkBookmark: Check? = Check()
    @JvmField var checkHistory: Check? = Check()
    @JvmField var checkTabs: Check? = Check()
    @JvmField var checkPassword: Check? = Check()
    @JvmField var progressMax: Int = 0
    @JvmField var progressCount: Int = 0
    @JvmField var lastProgressTime: Long = 0

    /** 0 = choices, 1 = progress, 2 = message. */
    private var stage by mutableIntStateOf(0)
    private var progress by mutableFloatStateOf(0f)
    private var message by mutableIntStateOf(AppR.string.loading)
    private var messageText by mutableStateOf<String?>(null)
    private var mainLabel by mutableIntStateOf(AppR.string.backup_import)
    private var mainOn by mutableStateOf(true)
    private var busy by mutableStateOf(false)
    private var targetError by mutableStateOf(false)
    /** Password for backups saved with one (BackupCrypto); 1 = missing, 2 = wrong. */
    private var password by mutableStateOf("")
    private var passError by mutableIntStateOf(0)
    /** True once [zipPath] holds a decrypted password backup: its zip has no archive password. */
    @JvmField var plainZip: Boolean = false
    private var outside = true
    private var executor: java.util.concurrent.ExecutorService? = null

    init {
        this.progressRunnable = Runnable {
            val i2 = progressMax
            if (progressCount > i2) progressCount = i2
            progress = if (i2 <= 0) 0f else progressCount.toFloat() / i2
        }
        this.appContext = context
        this.applyListener = dialogApplyListener
        this.backupPath = backupPath
        setSheet { Sheet() }
        show()
    }

    @Composable
    private fun ColumnScope.Sheet() {
        when (stage) {
            0 -> Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(top = 8.dp)) {
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
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; passError = 0 },
                    label = { Text(stringResource(AppR.string.backup_password)) },
                    singleLine = true,
                    isError = passError != 0,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(onDone = { if (mainOn) onMain() }),
                    supportingText = {
                        Text(stringResource(when (passError) {
                            1 -> AppR.string.backup_password_needed
                            2 -> AppR.string.backup_password_wrong
                            else -> AppR.string.backup_password_load_note
                        }))
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                )
            }
            1 -> Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 20.dp)) {
                Text(stringResource(AppR.string.loading), style = MaterialTheme.typography.bodyLarge)
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
            }
            else -> Box(
                Modifier.fillMaxWidth().weight(1f, fill = false).verticalScroll(rememberScrollState()).heightIn(min = 88.dp).padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(messageText ?: stringResource(message), style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            }
        }
        if (busy) {
            LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp))
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { onMain() }, enabled = mainOn) { Text(stringResource(mainLabel)) }
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

    /** Import / Cancel / Retry / OK. */
    private fun onMain() {
        if (mainBusy) return
        mainBusy = true
        mainHandler?.post {
            run step@{
                if (dataChanged || finished) {
                    dismiss()
                    return@step
                }
                val dialogTask = loadTask
                if (dialogTask != null) {
                    cancelLoad()
                    return@step
                }
                val c = checkSetting ?: return@step
                if (!c.checked && !checkQuick!!.checked && !checkBookmark!!.checked && !checkHistory!!.checked && !checkTabs!!.checked && !checkPassword!!.checked) {
                    targetError = true
                    showToast(appContext, AppR.string.backup_target)
                    return@step
                }
                val task = DialogTask(this)
                loadTask = task
                task.start(appContext)
            }
            mainBusy = false
        }
    }

    internal inner class DeleteTask(@JvmField val tempPath: String?,
                                          @JvmField val segmentDir: String?
    ) : Runnable {
        override fun run() {
            val tempPath = this.tempPath
            if (!TextUtils.isEmpty(tempPath)) {
                File(tempPath!!).delete()
            }
            MainUtil.noOp(this.segmentDir)
        }
    }

    /** Progress view for the row-by-row format (old `ApplyBusyTask`). */
    internal inner class ApplyBusyTask : Runnable {
        override fun run() {
            if (appContext == null) return
            progressCount = 0
            progressMax = totalFiles
            lastProgressTime = 0L
            progress = 0f
            applyBusy(false)
            messageText = null
            stage = 1
        }
    }

    class DbItem {
        @JvmField
        var db: SQLiteDatabase? = null
        @JvmField
        var table: String? = null
        @JvmField
        var cleared: Boolean = false
    }

    class DialogTask(dialogBackupLoad: DialogBackupLoad?) : MyAsyncTask() {
        @JvmField var dialogRef: WeakReference<*>? = WeakReference<Any?>(dialogBackupLoad)
        @JvmField var path: String? = null
        @JvmField var isValid: Boolean = false
        @JvmField var done: Boolean = false
        /** 1 = the backup needs a password, 2 = the password is wrong. */
        @JvmField var passIssue: Int = 0
        @JvmField var secret: CharArray? = null

        init {
            val d = dialogBackupLoad
            if (d != null) {
                this.path = d.backupPath
                this.secret = if (d.password.isEmpty()) null else d.password.toCharArray()
                d.cancelRequested = false
                d.dataChanged = false
                d.finished = false
                d.applyBusy(true)
                d.showMessage(AppR.string.loading)
                d.applyMainOn(true)
                d.mainLabel = AppR.string.cancel
                d.setCanceledOnTouchOutside(false)
            }
        }

        /**
         * Restores a backup. Gets the zip ([zipPath], else copies it from the chosen document, which can be on Google Drive through the system picker),
         * checks it, then extracts with the first matching format (2, 3, then 0) into [unpackDir].
         * Format 2: database files are copied over the app databases. Format 3: each table file is a row dump
         * ("//|#_line_#|//" between rows, three lines per column) replayed into the table, scripts are re-parsed.
         * Format 0 (oldest): a single "db_file" with "||name" sections replayed row by row. Preferences are
         * restored last. The result reports a bad zip or the outcome.
         */
        public override fun doInBackground() {
            val dialog = this.dialogRef!!.get() as DialogBackupLoad?
            if (dialog == null || this.cancelled) {
                return
            }
            if (!fetchZip(dialog) || this.cancelled) {
                return
            }
            if (!dialog.plainZip && BackupCrypto.isPasswordBackup(dialog.zipPath)) {
                val secret = this.secret
                if (secret == null) {
                    this.passIssue = 1
                    return
                }
                val plain = dialog.zipPath + "d"
                val result = BackupCrypto.decrypt(dialog.zipPath!!, plain, secret)
                if (result != BackupCrypto.OK) {
                    this.passIssue = if (result == BackupCrypto.WRONG_PASSWORD) 2 else 0
                    this.isValid = result != BackupCrypto.WRONG_PASSWORD
                    return
                }
                File(dialog.zipPath!!).delete()
                dialog.zipPath = plain
                dialog.plainZip = true
            }
            val valid = canOpen(dialog.appContext, dialog.zipPath)
            this.isValid = !valid
            if (!valid || this.cancelled) {
                return
            }
            val mode: Int
            try {
                if (dialog.unzipLegacy(2)) {
                    mode = 2
                } else {
                    if (this.cancelled) {
                        return
                    }
                    if (dialog.unzipLegacy(3)) {
                        mode = 3
                    } else {
                        if (this.cancelled) {
                            return
                        }
                        if (dialog.unzipLegacy(0)) {
                            mode = 0
                        } else {
                            if (this.cancelled) {
                                return
                            }
                            this.isValid = !CompressUtilZip2.extractStream(dialog.zipPath, legacyBackupKey(0))
                            return
                        }
                    }
                }
            } catch (throwable: Throwable) {
                return
            }
            if (this.cancelled || TextUtils.isEmpty(dialog.unpackDir) || !File(dialog.unpackDir!!).isDirectory()) {
                return
            }
            val files = File(dialog.unpackDir!!).listFiles()
            if (files == null || files.size == 0) {
                return
            }
            var names: ArrayList<PrefName>? = null
            if (mode != 0) {
                names = ArrayList<PrefName>()
                for (db in DialogBackupSave.Companion.dbNames) {
                    names.add(prefName(db))
                }
                for (db in DialogBackupSave.Companion.prefNames) {
                    names.add(prefName(db))
                }
                if (mode == 3) {
                    var total = files.size + 1
                    for (file in files) {
                        val db: String? = dbName(names, file.getName())
                        if (!TextUtils.isEmpty(db)) {
                            total += Companion.currentRows(dialog.appContext, db!!)
                        }
                    }
                    dialog.totalFiles = total
                    if (dialog.mainHandler != null) {
                        dialog.mainHandler.post(dialog.ApplyBusyTask())
                    }
                }
            }
            dialog.copyBackupFiles(dialog.appContext!!, files, true)
            dialog.copyBackupFiles(dialog.appContext!!, files, false)
            val done: Boolean
            if (mode == 0) {
                done = restoreLegacy(dialog)
            } else if (mode == 2) {
                done = restoreFiles(dialog, files, names)
            } else {
                for (file in files) {
                    val db: String? = dbName(names, file.getName())
                    if (TextUtils.isEmpty(db)) {
                        continue
                    }
                    if ("DbBookScript.db" == db) {
                        replayScripts(dialog, file, db)
                    } else {
                        replayRows(dialog, file, db)
                    }
                    dialog.stepProgress()
                }
                done = true
            }
            this.done = done
            if (done || dialog.dataChanged) {
                dialog.clearTemp(true)
            }
            dialog.stepProgress()
        }

        /** Makes the backup zip available as [zipPath]; returns false when it cannot be fetched.  */
        fun fetchZip(dialog: DialogBackupLoad?): Boolean {
            val context = dialog!!.appContext
            if (context == null) {
                return false
            }
            if (!TextUtils.isEmpty(dialog.zipPath) && File(dialog.zipPath!!).exists()) {
                return true
            }
            val temp = MainUtil.newCacheFilePath(context)
            if (TextUtils.isEmpty(temp)) {
                return false
            }
            val ok = MainUtil.copyFileOrUri(context, this.path, temp)
            if (ok) {
                dialog.zipPath = temp
            }
            return ok
        }

        /** Restart note + OK, after something was restored. */
        private fun restored(dialogBackupLoad: DialogBackupLoad) {
            // workspaces made after the backup are gone; the next start deletes what is left of them in other tables
            com.mycompany.app.pref.store.WorkspaceSettings.editNow(dialogBackupLoad.appContext) {
                set(com.mycompany.app.pref.store.WorkspaceSettings.restored, true)
            }
            dialogBackupLoad.showMessage(AppR.string.app_restart)
            dialogBackupLoad.applyListener?.onApply()
            dialogBackupLoad.applyBusy(false)
            dialogBackupLoad.applyMainOn(true)
            dialogBackupLoad.mainLabel = AppR.string.ok
            dialogBackupLoad.setCanceledOnTouchOutside(true)
        }

        public override fun onCancelled() {
            val d = this.dialogRef!!.get() as DialogBackupLoad? ?: return
            d.loadTask = null
            if (d.appContext == null) {
                return
            }
            if (!d.dataChanged) {
                showToast(d.appContext, AppR.string.cancelled)
                d.dismiss()
                return
            }
            restored(d)
        }

        public override fun onPostExecute() {
            val d = this.dialogRef!!.get() as DialogBackupLoad? ?: return
            d.loadTask = null
            if (d.appContext == null) {
                return
            }
            if (d.dataChanged) {
                restored(d)
                return
            }
            if (d.isCancelled()) {
                showToast(d.appContext, AppR.string.cancelled)
                d.dismiss()
                return
            }
            this.secret?.fill(' ')
            if (this.passIssue != 0) {
                d.passError = this.passIssue
                d.applyBusy(false)
                d.stage = 0
                d.mainLabel = AppR.string.backup_import
                d.applyMainOn(true)
                d.setCanceledOnTouchOutside(true)
                return
            }
            if (!this.isValid) {
                d.applyBusy(false)
                if (!this.done) {
                    d.showMessage(AppR.string.fail)
                    d.mainLabel = AppR.string.retry
                } else {
                    d.finished = true
                    d.showMessage(AppR.string.not_changed)
                    d.mainLabel = AppR.string.ok
                }
                d.applyMainOn(true)
                d.setCanceledOnTouchOutside(true)
                return
            }
            d.finished = true
            val sb = StringBuilder()
            sb.append(d.appContext!!.getString(AppR.string.backup_changed_1)).append("\n")
            sb.append(d.appContext!!.getString(AppR.string.backup_changed_2)).append("\n")
            sb.append(d.appContext!!.getString(AppR.string.backup_changed_3))
            d.applyBusy(false)
            d.messageText = sb.toString()
            d.stage = 2
            d.applyMainOn(true)
            d.mainLabel = AppR.string.ok
            d.setCanceledOnTouchOutside(true)
        }

        companion object {
            private fun prefName(db: String?): PrefName {
                val name = PrefName()
                name.db = db
                name.fileName = Md5FileNameGenerator().generate(db)
                return name
            }

            /** Database name for an extracted file (stored under the md5 of the name), or null.  */
            private fun dbName(names: ArrayList<PrefName>?, fileName: String?): String? {
                if (TextUtils.isEmpty(fileName) || names == null) {
                    return null
                }
                for (name in names) {
                    if (fileName == name.fileName) {
                        return name.db
                    }
                }
                return null
            }

            /** Row count of the app's own table for the progress total (only the row-by-row tables count).  */
            private fun currentRows(context: Context?, db: String?): Int {
                if (context == null) {
                    return 0
                }
                val helper: SQLiteOpenHelper?
                val table: String?
                when (db) {
                    "DbBookScript.db" -> {
                        helper = DbBookScript.get(context)
                        table = "DbBookScript_table"
                    }

                    "DbBookIcon.db" -> {
                        helper = DbBookIcon.get(context)
                        table = "DbBookIcon_table"
                    }

                    "DbBookPass.db" -> {
                        helper = DbBookPass.get(context)
                        table = "DbBookPass_table"
                    }

                    "DbBookQuick.db" -> {
                        helper = DbBookQuick.get(context)
                        table = "DbBookQuick_table"
                    }

                    "DbBookSearch.db" -> {
                        helper = DbBookSearch.get(context)
                        table = "DbBookSearch_table"
                    }

                    "DbBookWeb.db" -> {
                        helper = DbBookWeb.get(context)
                        table = "DbBookWeb_table"
                    }

                    "DbTabThumb.db" -> {
                        helper = DbTabThumb.get(context)
                        table = "DbTabThumb_table"
                    }

                    else -> return 0
                }
                var cursor: Cursor? = null
                var count = 0
                try {
                    cursor = DbUtil.query(helper.getWritableDatabase(), table, null, null, null, null)
                    if (cursor != null) {
                        count = cursor.getCount()
                    }
                } catch (ignored: Exception) {
                    count = 0
                }
                if (cursor != null) {
                    cursor.close()
                }
                return count
            }

            /** Oldest format: one "db_file" with "||name" section headers.  */
            private fun restoreLegacy(dialog: DialogBackupLoad?): Boolean {
                var `in`: InputStream? = null
                var reader: BufferedReader? = null
                var ok = false
                try {
                    `in` = openFileInput(dialog!!.unpackDir + "/" + Md5FileNameGenerator().generate("db_file"))
                    reader = BufferedReader(InputStreamReader(`in`))
                    var lines: ArrayList<Any?> = ArrayList<Any?>()
                    var section: String? = null
                    var line: String
                    while ((reader.readLine().also { line = it }) != null && !dialog.isCancelled()) {
                        if (line.startsWith("||")) {
                            dialog.importLines(dialog.appContext, section, lines)
                            section = line.substring(2)
                            lines = ArrayList<Any?>()
                        } else {
                            lines.add(line)
                        }
                    }
                    if (!dialog.isCancelled()) {
                        dialog.importLines(dialog.appContext, section, lines)
                    }
                    ok = true
                } catch (ignored: Exception) {
                }
                try {
                    if (reader != null) {
                        reader.close()
                    }
                } catch (ignored: Exception) {
                }
                try {
                    if (`in` != null) {
                        `in`.close()
                    }
                } catch (ignored: Exception) {
                }
                return ok
            }

            /** Format 2: copies each database file over the app's database.  */
            private fun restoreFiles(
                dialog: DialogBackupLoad?,
                files: Array<File>?,
                names: ArrayList<PrefName>?
            ): Boolean {
                val context = dialog!!.appContext
                for (file in files!!) {
                    val db: String? = dbName(names, file.getName())
                    if (TextUtils.isEmpty(db)) {
                        continue
                    }
                    val target = context!!.getDatabasePath(db)
                    if (target == null) {
                        continue
                    }
                    if (target.exists()) {
                        target.delete()
                    }
                    MainUtil.copyFile(file.getPath(), target.getPath())
                    dialog.dataChanged = true
                }
                return true
            }

            /**
             * Format 3 scripts: blocks between separators are name, second line, then the script body; each parsed
             * script is saved. The table is cleared before the first one.
             */
            private fun replayScripts(dialog: DialogBackupLoad?, file: File?, db: String?) {
                val context = dialog!!.appContext
                if (context == null) {
                    return
                }
                val item = dialog.openDbItem(context, db)
                if (item == null) {
                    return
                }
                var `in`: InputStream? = null
                var reader: BufferedReader? = null
                try {
                    `in` = openFileInput(file!!.getPath())
                    reader = BufferedReader(InputStreamReader(`in`))
                    var first = true
                    var name: String? = null
                    var second: String? = null
                    var body: StringBuilder? = null
                    var line: String?
                    while ((reader.readLine().also { line = it }) != null && !dialog.isCancelled()) {
                        if (line!!.startsWith(ROW_SEPARATOR)) {
                            if (body != null) {
                                first =
                                    saveScript(dialog, item, first, name, second, body.toString())
                                body = null
                            }
                            name = null
                            second = null
                        } else if (TextUtils.isEmpty(name)) {
                            name = line
                        } else if (TextUtils.isEmpty(second)) {
                            second = line
                        } else {
                            if (body == null) {
                                body = StringBuilder()
                            } else {
                                body.append("\n")
                            }
                            body.append(line)
                        }
                    }
                    if (body != null) {
                        saveScript(dialog, item, first, name, second, body.toString())
                    }
                } catch (ignored: Exception) {
                }
                try {
                    if (reader != null) {
                        reader.close()
                    }
                } catch (ignored: Exception) {
                }
                try {
                    if (`in` != null) {
                        `in`.close()
                    }
                } catch (ignored: Exception) {
                }
            }

            private fun saveScript(
                dialog: DialogBackupLoad?,
                item: DbItem?,
                first: Boolean,
                name: String?,
                second: String?,
                body: String?
            ): Boolean {
                if (first) {
                    deleteWhere(item!!.db, item.table, null, null)
                }
                dialog!!.ensureScriptLocale()
                val script = Script.parse(dialog.appContext, name, second, body, dialog.scriptLang, dialog.scriptLocale, false)
                if (script != null && DbBookScript.saveScript(dialog.appContext, script, null, dialog.scriptLang, dialog.scriptLocale) != null) {
                    dialog.dataChanged = true
                }
                dialog.stepProgress()
                return false
            }

            /** Format 3 tables: rows of (column, type, value) line triples, inserted after clearing the table.  */
            private fun replayRows(dialog: DialogBackupLoad?, file: File?, db: String?) {
                val context = dialog!!.appContext
                if (context == null) {
                    return
                }
                val item = dialog.openDbItem(context, db)
                if (item == null) {
                    return
                }
                val perRow =
                    "DbBookIcon.db" == db || "DbBookPass.db" == db || "DbBookQuick.db" == db
                            || "DbBookSearch.db" == db || "DbBookWeb.db" == db || "DbTabThumb.db" == db
                var `in`: InputStream? = null
                var reader: BufferedReader? = null
                try {
                    `in` = openFileInput(file!!.getPath())
                    reader = BufferedReader(InputStreamReader(`in`))
                    var first = true
                    var values: ContentValues? = null
                    var line: String?
                    while ((reader.readLine().also { line = it }) != null && !dialog.isCancelled()) {
                        if (line!!.startsWith(ROW_SEPARATOR)) {
                            if (values != null) {
                                first = insertRow(dialog, item, first, values, perRow)
                                values = null
                            }
                            continue
                        }
                        val type = reader.readLine()
                        if (type == null) {
                            break
                        }
                        val value = reader.readLine()
                        if (value == null) {
                            break
                        }
                        values = fillValues(values, context, line, type, value)
                    }
                    if (values != null) {
                        insertRow(dialog, item, first, values, perRow)
                    }
                } catch (ignored: Exception) {
                }
                try {
                    if (reader != null) {
                        reader.close()
                    }
                } catch (ignored: Exception) {
                }
                try {
                    if (`in` != null) {
                        `in`.close()
                    }
                } catch (ignored: Exception) {
                }
                // backups carry the passwords opened (any phone can restore them): seal them with this phone's key
                if ("DbBookPass.db" == db) DbBookPass.sealPlain(context)
            }

            private fun insertRow(
                dialog: DialogBackupLoad?,
                item: DbItem?,
                first: Boolean,
                values: ContentValues?,
                perRow: Boolean
            ): Boolean {
                if (first) {
                    deleteWhere(item!!.db, item.table, null, null)
                }
                insert(item!!.db, item.table, values)
                dialog!!.dataChanged = true
                if (perRow) {
                    dialog.stepProgress()
                }
                return false
            }

            private const val ROW_SEPARATOR = "//|#_line_#|//"
        }
    }

    private class PrefName {
        var db: String? = null
        var fileName: String? = null
    }

    fun clearTemp(all: Boolean) {
        if (TextUtils.isEmpty(this.zipPath) && TextUtils.isEmpty(this.unpackDir)) {
            return
        }
        val zipPath = this.zipPath
        val unpackDir = this.unpackDir
        this.zipPath = null
        this.unpackDir = null
        if (!all) {
            runAsync(DeleteTask(zipPath, unpackDir))
            return
        }
        if (!TextUtils.isEmpty(zipPath)) {
            File(zipPath!!).delete()
        }
        MainUtil.noOp(unpackDir)
    }

    @Throws(Throwable::class)
    fun unzipLegacy(keyVersion: Int): Boolean {
        var arrayListD: ArrayList<Any?>?
        val context: Context? = this.appContext
        if (context == null) {
            return false
        }
        var outputStreamB: OutputStream? = null
        val compressUtilZip = CompressUtilZip(context, this.zipPath, null)
        compressUtilZip.charsetName = MainConst.defaultCharset
        // A decrypted password backup holds a plain zip; zip4j ignores the archive password for plain entries,
        // and restoring needs the zip4j path, which is only taken with a password set.
        compressUtilZip.password = legacyBackupKey(keyVersion)
        if (!compressUtilZip.open() || compressUtilZip.entryTotal == 0) {
            return false
        }
        if (TextUtils.isEmpty(this.unpackDir)) {
            this.unpackDir = (StringBuilder()).append(this.zipPath).append("2").toString()
        }
        val file = File(this.unpackDir!!)
        MainUtil.deleteRecursive(file)
        if (!file.mkdirs()) {
            return false
        }
        val unpackDir = this.unpackDir
        val zip: CompressUtilZip2? = if (TextUtils.isEmpty(compressUtilZip.path)) null else compressUtilZip.zip2
        val zipFile: ZipFile? = if (TextUtils.isEmpty(compressUtilZip.path) || zip == null) null else zip.openZip
        if (TextUtils.isEmpty(compressUtilZip.path) || zip == null || zipFile == null) {
            return false
        }
        try {
            arrayListD = zipFile.getFileHeaders() as ArrayList<Any?>?
        } catch (unused: Exception) {
            arrayListD = null
        }
        if (arrayListD == null || arrayListD.isEmpty()) {
            return false
        }
        val size = arrayListD.size
        var found = false
        var index = 0
        while (index < size) {
            val rawFileHeader = arrayListD.get(index)
            index++
            val fileHeader = rawFileHeader as FileHeader?
            if (fileHeader != null) {
                try {
                    if (!fileHeader.isDirectory) {
                        val zipInputStreamE = zip.openZip!!.getInputStream(fileHeader)
                        outputStreamB = MainUtil.openFileOutput(unpackDir + "/" + fileHeader.fileName, false)
                        val bArr = ByteArray(8192)
                        while (true) {
                            val count = zipInputStreamE.read(bArr, 0, 8192)
                            if (count == -1) {
                                break
                            }
                            outputStreamB!!.write(bArr, 0, count)
                        }
                        found = true
                    }
                } catch (unused2: Exception) {
                }
            }
        }
        if (outputStreamB != null) {
            try {
                outputStreamB.close()
            } catch (unused3: Exception) {
            }
        }
        return found
    }

    fun openDbItem(context: Context?, db: String?): DbItem? {
        val writableDatabase: SQLiteDatabase?
        val table: String?
        val myButtonCheck: Check?
        if (context != null && !TextUtils.isEmpty(db)) {
            var cleared = false
            if ("DbAdsCmd.db" == db) {
                val checkSetting = this.checkSetting
                if (checkSetting == null || !checkSetting.checked) {
                    return null
                }
                writableDatabase = DbAdsCmd.get(context).getWritableDatabase()
                table = "DbAdsCmd_table"
            } else if ("DbBookAds.db" == db) {
                val checkSetting = this.checkSetting
                if (checkSetting == null || !checkSetting.checked) {
                    return null
                }
                writableDatabase = DbBookAds.get(context).getWritableDatabase()
                table = "DbBookAds_table"
            } else if ("DbBookAgent.db" == db) {
                val checkSetting = this.checkSetting
                if (checkSetting == null || !checkSetting.checked) {
                    return null
                }
                writableDatabase = DbBookAgent.get(context).getWritableDatabase()
                table = "DbBookAgent_table"
            } else if ("DbBookBlock.db" == db) {
                val checkSetting = this.checkSetting
                if (checkSetting == null || !checkSetting.checked) {
                    return null
                }
                writableDatabase = DbBookBlock.get(context).getWritableDatabase()
                table = "DbBookBlock_table"
            } else if ("DbBookFilter.db" == db) {
                val checkSetting = this.checkSetting
                if (checkSetting == null || !checkSetting.checked) {
                    return null
                }
                writableDatabase = DbBookFilter.get(context).getWritableDatabase()
                table = "DbBookFilter_table"
            } else if ("DbBookJava.db" == db) {
                val checkSetting = this.checkSetting
                if (checkSetting == null || !checkSetting.checked) {
                    return null
                }
                writableDatabase = DbBookJava.get(context).getWritableDatabase()
                table = "DbBookJava_table"
            } else if ("DbBookLink.db" == db) {
                val checkSetting = this.checkSetting
                if (checkSetting == null || !checkSetting.checked) {
                    return null
                }
                writableDatabase = DbBookLink.get(context).getWritableDatabase()
                table = "DbBookLink_table"
            } else if ("DbBookLocale.db" == db) {
                val checkSetting = this.checkSetting
                if (checkSetting == null || !checkSetting.checked) {
                    return null
                }
                writableDatabase = DbBookLocale.get(context).getWritableDatabase()
                table = "DbBookLocale_table"
            } else if ("DbBookMemo.db" == db) {
                val checkSetting = this.checkSetting
                if (checkSetting == null || !checkSetting.checked) {
                    return null
                }
                writableDatabase = DbBookMemo.get(context).getWritableDatabase()
                table = "DbBookMemo_table"
            } else if ("DbBookOver.db" == db) {
                val checkSetting = this.checkSetting
                if (checkSetting == null || !checkSetting.checked) {
                    return null
                }
                writableDatabase = DbBookOver.get(context).getWritableDatabase()
                table = "DbBookOver_table"
            } else if ("DbBookPop.db" == db) {
                val checkSetting = this.checkSetting
                if (checkSetting == null || !checkSetting.checked) {
                    return null
                }
                writableDatabase = DbBookPop.get(context).getWritableDatabase()
                table = "DbBookPop_table"
            } else if ("DbBookRecent.db" == db) {
                val checkSetting = this.checkSetting
                if (checkSetting == null || !checkSetting.checked) {
                    return null
                }
                writableDatabase = DbBookRecent.get(context).getWritableDatabase()
                table = "DbBookRecent_table"
            } else if ("DbBookScript.db" == db) {
                val checkSetting = this.checkSetting
                if (checkSetting == null || !checkSetting.checked) {
                    return null
                }
                writableDatabase = DbBookScript.get(context).getWritableDatabase()
                table = "DbBookScript_table"
            } else if ("DbBookSearch.db" == db) {
                val checkSetting = this.checkSetting
                if (checkSetting == null || !checkSetting.checked) {
                    return null
                }
                writableDatabase = DbBookSearch.get(context).getWritableDatabase()
                table = "DbBookSearch_table"
            } else if ("DbBookTmem.db" == db) {
                val checkSetting = this.checkSetting
                if (checkSetting == null || !checkSetting.checked) {
                    return null
                }
                writableDatabase = DbBookTmem.get(context).getWritableDatabase()
                table = "DbBookTmem_table"
            } else if ("DbBookTrans.db" == db) {
                val checkSetting = this.checkSetting
                if (checkSetting == null || !checkSetting.checked) {
                    return null
                }
                writableDatabase = DbBookTrans.get(context).getWritableDatabase()
                table = "DbBookTrans_table"
            } else if ("DbBookUser.db" == db) {
                val checkSetting = this.checkSetting
                if (checkSetting == null || !checkSetting.checked) {
                    return null
                }
                writableDatabase = DbBookUser.get(context).getWritableDatabase()
                table = "DbBookUser_table"
            } else if ("DbRecentLang.db" == db) {
                val checkSetting = this.checkSetting
                if (checkSetting == null || !checkSetting.checked) {
                    return null
                }
                writableDatabase = DbRecentLang.get(context).getWritableDatabase()
                table = "DbRecentLang_table"
            } else if ("DbBookQuick.db" == db) {
                val checkQuick = this.checkQuick
                if (checkQuick == null || !checkQuick.checked) {
                    return null
                }
                writableDatabase = DbBookQuick.get(context).getWritableDatabase()
                table = "DbBookQuick_table"
            } else if ("DbBookWeb.db" == db) {
                val checkBookmark = this.checkBookmark
                if (checkBookmark == null || !checkBookmark.checked) {
                    return null
                }
                writableDatabase = DbBookWeb.get(context).getWritableDatabase()
                table = "DbBookWeb_table"
            } else if ("DbBookHistory.db" == db) {
                val checkHistory = this.checkHistory
                if (checkHistory == null || !checkHistory.checked) {
                    return null
                }
                writableDatabase = DbBookHistory.get(context).getWritableDatabase()
                table = "DbBookHistory_table"
            } else if ("DbBookIcon.db" == db) {
                val checkHistory = this.checkHistory
                if ((checkHistory == null || !checkHistory.checked) && ((this.checkTabs.also {
                        myButtonCheck = it
                    }) == null || !myButtonCheck!!.checked)) {
                    return null
                }
                writableDatabase = DbBookIcon.get(context).getWritableDatabase()
                table = "DbBookIcon_table"
            } else if ("DbBookTab2.db" == db) {
                val checkTabs = this.checkTabs
                if (checkTabs == null || !checkTabs.checked) {
                    return null
                }
                writableDatabase = DbBookTabOld.get(context).getWritableDatabase()
                table = "DbBookTab2_table"
                cleared = true
            } else if ("DbBookTab3.db" == db) {
                val checkTabs = this.checkTabs
                if (checkTabs == null || !checkTabs.checked) {
                    return null
                }
                writableDatabase = DbBookTab.get(context).getWritableDatabase()
                table = "DbBookTab3_table"
            } else if ("DbTabState.db" == db) {
                val checkTabs = this.checkTabs
                if (checkTabs == null || !checkTabs.checked) {
                    return null
                }
                writableDatabase = DbTabState.get(context).getWritableDatabase()
                table = "DbTabState_table"
            } else if ("DbTabThumb.db" == db) {
                val checkTabs = this.checkTabs
                if (checkTabs == null || !checkTabs.checked) {
                    return null
                }
                writableDatabase = DbTabThumb.get(context).getWritableDatabase()
                table = "DbTabThumb_table"
            } else if ("DbBookPass.db" == db) {
                val checkPassword = this.checkPassword
                if (checkPassword == null || !checkPassword.checked) {
                    return null
                }
                writableDatabase = DbBookPass.get(context).getWritableDatabase()
                table = "DbBookPass_table"
            } else {
                writableDatabase = null
                table = null
            }
            if (writableDatabase != null && table != null) {
                val dbItem = DbItem()
                dbItem.db = writableDatabase
                dbItem.table = table
                dbItem.cleared = cleared
                return dbItem
            }
        }
        return null
    }

    fun isCancelled(): Boolean {
        if (this.cancelRequested) {
            return true
        }
        val dialogTask = this.loadTask
        return dialogTask != null && dialogTask.cancelled
    }

    /** Cancel while importing (or close). */
    fun cancelLoad() {
        if (this.loadTask == null) {
            dismiss()
            return
        }
        applyBusy(true)
        showMessage(AppR.string.canceling)
        applyMainOn(false)
        this.cancelRequested = true
        this.loadTask?.cancelled = true
        this.loadTask = null
    }

    fun ensureScriptLocale() {
        val localeI: Locale? = if (!TextUtils.isEmpty(this.scriptLang)) null else MainApp.currentLocale()
        if (!TextUtils.isEmpty(this.scriptLang) || localeI == null) {
            return
        }
        val language = localeI.getLanguage()
        if (TextUtils.isEmpty(language)) {
            return
        }
        val locale = Locale.US
        val lowerCase = language.lowercase(locale)
        val country = localeI.getCountry()
        val localeTag = if (!TextUtils.isEmpty(country)) "$lowerCase-${country.lowercase(locale)}" else null
        this.scriptLang = lowerCase
        this.scriptLocale = localeTag
    }

    /** Main button on / off. */
    fun applyMainOn(newMainOn: Boolean) {
        mainOn = newMainOn
    }

    fun importLines(context: Context?, section: String?, arrayList: ArrayList<Any?>?) {
        if (context == null || TextUtils.isEmpty(section) || arrayList!!.isEmpty()) {
            return
        }
        val dbItemC = openDbItem(context, section + ".db")
        if (dbItemC != null) {
            val size = arrayList.size
            var found = false
            var first = true
            var itemIndex = 0
            while (itemIndex < size) {
                val item: Any? = arrayList.get(itemIndex)
                itemIndex++
                val line = item as String?
                if (isCancelled()) {
                    return
                }
                val parts: Array<String?>? = if (line != null) line.split("##".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray() else null
                if (line != null && parts != null && parts.size != 0 && parts.size % 3 == 0) {
                    try {
                        val length = parts.size
                        var contentValuesH: ContentValues? = null
                        var index = 0
                        while (index < length) {
                            if (isCancelled()) {
                                return
                            }
                            contentValuesH = fillValues(
                                contentValuesH,
                                context,
                                parts[index],
                                parts[index + 1],
                                parts[index + 2]
                            )
                            index += 3
                        }
                        if (contentValuesH != null) {
                            if (first) {
                                try {
                                    deleteWhere(dbItemC.db, dbItemC.table, null, null)
                                    first = false
                                } catch (unused: Exception) {
                                    first = false
                                }
                            }
                            insert(dbItemC.db, dbItemC.table, contentValuesH)
                            this.dataChanged = true
                            found = true
                        }
                    } catch (unused2: Exception) {
                        continue
                    }
                }
            }
            if (dbItemC.cleared && found) {
                DbBookTab.clearAll(context)
                DbBookTabOld.readTabs(context, false)
                DbBookTabOld.readTabs(context, true)
                deleteWhere(DbBookTabOld.get(context).getWritableDatabase(), "DbBookTab2_table", null, null)
            }
        }
    }

    /** Busy bar on / off. */
    fun applyBusy(newBusy: Boolean) {
        busy = newBusy
    }

    fun showMessage(newMessage: Int) {
        message = newMessage
        messageText = null
        stage = 2
    }

    fun copyBackupFiles(context: Context?, fileArr: Array<File>?, secret: Boolean) {
        var nameText: String?
        var myButtonCheck: Check? = null
        if (fileArr!!.size != 0) {
            val filesDir = context!!.getFilesDir().toString() + "/" + (if (secret) ".back" else ".pref")
            val file = File(filesDir)
            if (!file.isDirectory()) {
                file.mkdir()
            }
            val checkSetting = this.checkSetting
            val withImages = if (checkSetting == null || !checkSetting.checked || (this.checkTabs.also {
                    myButtonCheck = it
                }) == null || myButtonCheck!!.checked) false else true
            // the settings files and background pictures only with "Settings" (2026-11-25: they were always restored);
            // the workspace list always, merged with the phone's (see PrefSpace below)
            val withSettings = checkSetting != null && checkSetting.checked
            val spacesBefore = com.mycompany.app.pref.store.Workspaces.all()
            val currentBefore = com.mycompany.app.pref.store.Workspaces.currentNumber()
            val tabsBefore = com.mycompany.app.pref.store.WorkspaceSettings.tabs.value
            val nextBefore = com.mycompany.app.pref.store.WorkspaceSettings.next.value
            val dropBefore = com.mycompany.app.pref.store.WorkspaceSettings.drop.value
            val names: Array<String?> =
                if (secret) DialogBackupSave.Companion.secretTablesAll else DialogBackupSave.Companion.secretTables
            val arrayList: ArrayList<Any?> = ArrayList<Any?>()
            for (db in names) {
                val prefName = PrefName()
                prefName.db = db
                prefName.fileName = Md5FileNameGenerator().generate(db)
                arrayList.add(prefName)
            }
            for (file2 in fileArr) {
                val name = file2.getName()
                if (!TextUtils.isEmpty(name)) {
                    val size = arrayList.size
                    var index = 0
                    while (true) {
                        if (index >= size) {
                            nameText = null
                            break
                        }
                        val rawPref: Any? = arrayList.get(index)
                        index++
                        val prefName: PrefName = rawPref as PrefName
                        if (name == prefName.fileName) {
                            nameText = prefName.db
                            break
                        }
                    }
                    if (!TextUtils.isEmpty(nameText) && (withSettings || (!secret && "PrefSpace" == nameText))) {
                        val targetPath = "$filesDir/$nameText"
                        val target = File(targetPath)
                        if (target.exists()) {
                            target.delete()
                        }
                        MainUtil.copyFile(file2.getPath(), targetPath)
                        if (secret) {
                            if ("back.jpg" == nameText || "nor_land.jpg" == nameText) {
                                this.backImageSet = true
                            } else if ("sec_port.jpg" == nameText || "sec_land.jpg" == nameText) {
                                this.secretImageSet = true
                            }
                            stepProgress()
                        } else {
                            if ("PrefSync" == nameText) {
                                if (withImages) {
                                    PrefSync.load(context, true)
                                    val prefSyncR = PrefSync.get(context, false)
                                    if (prefSyncR != null) {
                                        prefSyncR.putInt(0, "mNormalIndex")
                                        prefSyncR.putInt(0, "mSecretIndex")
                                        prefSyncR.save()
                                    }
                                }
                            } else if ("PrefSpace" == nameText) {
                                // the backup's workspaces join the phone's: none of the phone's disappears (its rows may
                                // not be in this restore), and those only in the backup come back with their rows
                                com.mycompany.app.pref.PrefSpace.load(context, true)
                                com.mycompany.app.pref.store.Workspaces.mergeRestored(context, spacesBefore, currentBefore, tabsBefore, nextBefore, dropBefore)
                            } else if ("PrefImage" == nameText) {
                                PrefImage.load(context, true)
                                val prefImageR = PrefImage.get(context, false)
                                if (prefImageR != null) {
                                    prefImageR.putInt(com.mycompany.app.pref.store.ImageSettings.pageIndex.value, "mIndex")
                                    prefImageR.putInt(0, "mPage")
                                    prefImageR.save()
                                }
                            } else if ("PrefMain" == nameText) {
                                PrefMain.load(context, true)
                                val prefMainR = PrefMain.get(context, false)
                                if (prefMainR != null) {
                                    prefMainR.putInt(0, "mStatusHeight")
                                    prefMainR.putBool("mStatusConfirm", false)
                                    prefMainR.putInt(0, "mNaviHeight2")
                                    prefMainR.putBool("mNaviConfirm", false)
                                    prefMainR.save()
                                }
                            } else if ("PrefPdf" == nameText) {
                                PrefPdf.load(context, true)
                                val prefPdfR = PrefPdf.get(context, false)
                                if (prefPdfR != null) {
                                    prefPdfR.putInt(0, "mMaxTexSize")
                                    prefPdfR.putBool("mPayConfirm", false)
                                    prefPdfR.save()
                                }
                            } else if ("PrefRead" == nameText) {
                                PrefRead.load(context, true)
                                val prefReadR = PrefRead.get(context, false)
                                if (prefReadR != null) {
                                    prefReadR.putBool("mUserFont", false)
                                    prefReadR.putString("mFontPath", "")
                                    prefReadR.save()
                                }
                            } else if ("PrefSecret" == nameText) {
                                PrefSecret.load(context, true)
                                val prefSecretR = PrefSecret.get(context, false)
                                if (prefSecretR != null) {
                                    prefSecretR.putBool("mLockSkip", false)
                                    prefSecretR.save()
                                }
                            } else if ("PrefTts" == nameText) {
                                PrefTts.load(context, true)
                                val prefTtsR = PrefTts.get(context, false)
                                if (prefTtsR != null) {
                                    prefTtsR.putBool("mSkipKeypad", false)
                                    if (TtsSettings.vpnMode.value) {
                                        try {
                                            if (VpnService.prepare(context) != null) {
                                                TtsSettings.editLocal { set(TtsSettings.vpnMode, false) }
                                                prefTtsR.putBool("mVpnMode", false)
                                            }
                                        } catch (unused: Exception) {
                                        }
                                    }
                                    prefTtsR.save()
                                }
                            } else if ("PrefWeb" == nameText) {
                                val backImageSet = this.backImageSet
                                val secretImageSet = this.secretImageSet
                                PrefWeb.load(context, true)
                                val prefWebR: PrefWeb? = if ((!BrowserSettings.quickBack.value || !backImageSet || !BrowserSettings.quickBackSecret.value || !secretImageSet)) PrefWeb.get(
                                        context,
                                        false
                                    ) else null
                                if ((!BrowserSettings.quickBack.value || !backImageSet || !BrowserSettings.quickBackSecret.value || !secretImageSet) && prefWebR != null) {
                                    if (BrowserSettings.quickBack.value && !backImageSet) {
                                        prefWebR.putBool("mQuickBack", false)
                                    }
                                    if (BrowserSettings.quickBackSecret.value && !secretImageSet) {
                                        prefWebR.putBool("mQckBckSec", false)
                                    }
                                    prefWebR.save()
                                }
                            } else if ("PrefZone" == nameText) {
                                PrefZone.load(context, context.getResources(), true)
                                val prefZoneR = PrefZone.get(context, false)
                                if (prefZoneR != null) {
                                    prefZoneR.putBool("mUserFont", false)
                                    prefZoneR.putString("mFontPath", "")
                                    prefZoneR.save()
                                }
                            } else if ("PrefZtwo" == nameText) {
                                PrefZtwo.load(context, true)
                                val prefZtwoR = PrefZtwo.get(context, false)
                                if (prefZtwoR != null) {
                                    prefZtwoR.putString("mSoulAgent", "")
                                    prefZtwoR.save()
                                }
                            }
                            this.dataChanged = true
                            stepProgress()
                        }
                    }
                }
            }
        }
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
            val handler = this.mainHandler
            if (handler == null) {
                return
            }
            val runnable = this.progressRunnable
            handler.removeCallbacks(runnable!!)
            this.mainHandler.post(runnable)
        }
    }

    override fun cancel() {
        cancelLoad()
    }

    override fun dismiss() {
        if (this.appContext != null) {
            this.loadTask?.cancelled = true
            this.loadTask = null
            clearTemp(false)
        }
        this.progressRunnable?.let { this.mainHandler?.removeCallbacks(it) }
        this.checkSetting = null
        this.checkQuick = null
        this.checkBookmark = null
        this.checkHistory = null
        this.checkTabs = null
        this.checkPassword = null
        this.appContext = null
        this.applyListener = null
        this.backupPath = null
        this.scriptLang = null
        this.scriptLocale = null
        super.dismiss()
    }

    companion object {
        @JvmStatic
        fun fillValues(
            contentValues: ContentValues?,
            context: Context?,
            line: String?,
            type: String?,
            part: String?
        ): ContentValues? {
            var contentValues = contentValues
            var longNumber: Long
            if (context != null && !TextUtils.isEmpty(line) && !TextUtils.isEmpty(type) && !TextUtils.isEmpty(
                    part
                )
            ) {
                try {
                    if ("s" == type) {
                        if (part != "null") {
                            if (contentValues == null) {
                                contentValues = ContentValues()
                            }
                            contentValues.put(line, part)
                        }
                    } else if ("b" == type) {
                        if (part != "null") {
                            val bitmapC = MainUtil.decodeDataUri(context, part)
                            if (MainUtil.isBitmapOk(bitmapC)) {
                                val byteArrayOutputStream = ByteArrayOutputStream()
                                bitmapC!!.compress(CompressFormat.PNG, 100, byteArrayOutputStream)
                                if (contentValues == null) {
                                    contentValues = ContentValues()
                                }
                                contentValues.put(line, byteArrayOutputStream.toByteArray())
                                byteArrayOutputStream.close()
                            }
                        }
                    } else if ("i" == type) {
                        val color = parseColorSafe(part)
                        if (color != -1) {
                            if (contentValues == null) {
                                contentValues = ContentValues()
                            }
                            contentValues.put(line, color)
                        }
                    } else if ("l" == type) {
                        if (TextUtils.isEmpty(part)) {
                            longNumber = -1
                        } else {
                            try {
                                longNumber = part!!.toLong()
                            } catch (unused: Exception) {
                                longNumber = -1
                            }
                        }
                        if (longNumber != -1L) {
                            if (contentValues == null) {
                                contentValues = ContentValues()
                            }
                            contentValues.put(line, longNumber)
                        }
                    }
                } catch (unused2: Exception) {
                }
            }
            return contentValues
        }
    }
}
