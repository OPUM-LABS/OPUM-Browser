package com.mycompany.app.ui.settings

import android.content.Intent
import android.text.TextUtils
import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogBackupLoad
import com.mycompany.app.dialog.DialogBackupSave
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader

/**
 * Backup & restore (tidied 2026-11-26): create a backup file or restore one; "Other browsers" imports and exports bookmarks (HTML) and passwords
 * (CSV) in the formats of Chrome and Firefox ([rememberBrowserTransfer]). Both dialogs ([DialogBackupLoad], [DialogBackupSave]) still do the real
 * work and are shown through `DialogShow.direct`; the export dialog's folder picker result (request code 19) is
 * forwarded to it. After a restore the app state is reloaded like the old screen did.
 */
class BackupSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val pickBackup = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode != RESULT_OK || data == null) return@rememberLauncherForActivityResult
            val uri = data.data
            val uriText = uri?.toString()
            if (uri == null || TextUtils.isEmpty(uriText)) {
                MainUtil.showToast(this, R.string.invalid_file)
            } else if ("dat" != MainUtil.lowerExtOf(MainUri.treeDisplayPath(this, uriText))) {
                MainUtil.showToast(this, R.string.invalid_file)
            } else {
                MainUtil.keepUriPermission(this, uri)
                showImport(uriText!!)
            }
        }

        val transfer = rememberBrowserTransfer()

        SettingsScaffold(title = stringResource(R.string.set_backup), onBack = { finish() }) {
            settingsHeader(getString(R.string.hd_backup))
            item {
                SettingRow(
                    title = stringResource(R.string.backup_create),
                    summary = stringResource(R.string.backup_create_sum),
                    onClick = { showExport() },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.backup_restore),
                    summary = stringResource(R.string.backup_restore_sum),
                    onClick = {
                        try {
                            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension("dat")
                            val intent = Intent("android.intent.action.OPEN_DOCUMENT")
                            intent.addCategory("android.intent.category.OPENABLE")
                            intent.type = if (TextUtils.isEmpty(mime)) "*/*" else mime
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                            pickBackup.launch(intent)
                        } catch (_: Exception) {
                        }
                    },
                )
            }
            settingsFooter(getString(R.string.backup_pw_page))
            settingsHeader(getString(R.string.hd_from_browsers))
            item {
                SettingRow(
                    title = stringResource(R.string.transfer_import_bookmarks),
                    summary = stringResource(R.string.transfer_import_bookmarks_summary),
                    onClick = transfer.importBookmarks,
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.transfer_import_passwords),
                    summary = stringResource(R.string.transfer_import_passwords_summary),
                    onClick = transfer.importPasswords,
                )
            }
            settingsHeader(getString(R.string.hd_to_browsers))
            item {
                SettingRow(
                    title = stringResource(R.string.transfer_export_bookmarks),
                    summary = stringResource(R.string.transfer_export_bookmarks_summary),
                    onClick = transfer.exportBookmarks,
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.transfer_export_passwords),
                    summary = stringResource(R.string.transfer_export_passwords_summary),
                    onClick = transfer.exportPasswords,
                )
            }
        }
    }

    private fun showImport(uri: String) {
        var applied = false
        com.mycompany.app.ui.prefs.DialogShow.direct(
            this as android.app.Activity,
            onDismiss = { if (applied) MainUtil.restartActivity(this, null, false) },
        ) { host ->
            DialogBackupLoad(host, uri, object : DialogApplyListener { override fun onApply() { applied = true } })
        }
    }

    private fun showExport() {
        var dialog: DialogBackupSave? = null
        com.mycompany.app.ui.prefs.DialogShow.direct(
            this as android.app.Activity,
            requestCodes = intArrayOf(19),
            onResult = { code, result, data ->
                val save = dialog
                if (save != null && code == 19 && result == RESULT_OK && data != null && save.appContext != null) {
                    val uri = data.data
                    if (uri == null) {
                        MainUtil.showToast(save.appContext, R.string.invalid_path)
                    } else {
                        val path = MainUri.treeDocUri(uri)
                        if (TextUtils.isEmpty(path)) {
                            MainUtil.showToast(save.appContext, R.string.invalid_path)
                        } else {
                            save.saveFolder(path)
                            MainUtil.keepUriPermission(save.appContext, uri)
                        }
                    }
                }
            },
        ) { host -> DialogBackupSave(host).also { dialog = it } }
    }
}
