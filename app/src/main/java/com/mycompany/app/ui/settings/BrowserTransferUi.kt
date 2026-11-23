package com.mycompany.app.ui.settings

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.data.transfer.BrowserTransfer
import com.mycompany.app.data.transfer.TransferSummary
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.SecretSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** The four actions of the "Other browsers" rows; get one with [rememberBrowserTransfer]. */
class BrowserTransferActions(
    val importBookmarks: () -> Unit,
    val importPasswords: () -> Unit,
    val exportBookmarks: () -> Unit,
    val exportPasswords: () -> Unit,
)

/**
 * Import and export of bookmarks (Netscape HTML) and passwords (Chrome / Firefox CSV) with the Android file picker,
 * for switching to and from Chrome, Firefox, Edge and Brave. Used by Backup & restore and the password list; the
 * bookmark list (`WebBookmarksDialog`, a dialog without its own result launchers) uses [BrowserTransfer] directly.
 *
 * Exporting passwords first warns that the CSV file is not encrypted, then asks for the password lock (or the app
 * lock) when one is set, else for the device's screen lock when the device has one. After an import a summary shows
 * how many entries were added, skipped as already saved, and could not be used. [onChanged] runs after an import
 * that added something.
 */
@Composable
fun rememberBrowserTransfer(onChanged: () -> Unit = {}): BrowserTransferActions {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var summary by remember { mutableStateOf<TransferSummary?>(null) }
    var warnPasswords by remember { mutableStateOf(false) }

    fun run(work: suspend () -> Unit) {
        if (busy) return
        busy = true
        scope.launch {
            try {
                work()
            } finally {
                busy = false
            }
        }
    }

    fun importFile(uri: Uri?, passwords: Boolean) {
        if (uri == null) return
        run {
            val result = withContext(Dispatchers.IO) { BrowserTransfer.importFromUri(context, uri, passwords) }
            if (result == null) {
                MainUtil.showToast(context, R.string.invalid_file)
            } else {
                summary = result
                if (result.imported > 0) onChanged()
            }
        }
    }

    fun exportFile(uri: Uri?, passwords: Boolean) {
        if (uri == null) return
        run {
            val count = withContext(Dispatchers.IO) { BrowserTransfer.exportToUri(context, uri, passwords) }
            showExportResult(context, count)
        }
    }

    val openBookmarks = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { importFile(it, false) }
    val openPasswords = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { importFile(it, true) }
    val createBookmarks = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/html")) { exportFile(it, false) }
    val createPasswords = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { exportFile(it, true) }

    fun pickPasswordTarget() {
        try {
            createPasswords.launch(BrowserTransfer.exportFileName(true))
        } catch (_: Exception) {
        }
    }

    val unlock = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) pickPasswordTarget()
    }

    fun authenticateThenExport() {
        val intent = authIntent(context)
        if (intent == null) {
            pickPasswordTarget()
        } else {
            try {
                unlock.launch(intent)
            } catch (_: Exception) {
            }
        }
    }

    summary?.let { TransferSummaryDialog(it) { summary = null } }
    if (warnPasswords) {
        AlertDialog(
            onDismissRequest = { warnPasswords = false },
            title = { Text(stringResource(R.string.transfer_password_warning_title)) },
            text = { Text(stringResource(R.string.transfer_password_warning)) },
            confirmButton = {
                TextButton(onClick = { warnPasswords = false; authenticateThenExport() }) {
                    Text(stringResource(R.string.backup_export))
                }
            },
            dismissButton = { TextButton(onClick = { warnPasswords = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }

    return remember {
        BrowserTransferActions(
            importBookmarks = {
                try {
                    openBookmarks.launch(BrowserTransfer.BOOKMARK_MIME_TYPES)
                } catch (_: Exception) {
                }
            },
            importPasswords = {
                try {
                    openPasswords.launch(BrowserTransfer.PASSWORD_MIME_TYPES)
                } catch (_: Exception) {
                }
            },
            exportBookmarks = {
                scope.launch {
                    val count = withContext(Dispatchers.IO) { BrowserTransfer.bookmarkCount(context) }
                    if (count == 0) {
                        MainUtil.showToast(context, R.string.no_bookmark)
                    } else {
                        try {
                            createBookmarks.launch(BrowserTransfer.exportFileName(false))
                        } catch (_: Exception) {
                        }
                    }
                }
            },
            exportPasswords = {
                scope.launch {
                    val count = withContext(Dispatchers.IO) { BrowserTransfer.passwordCount(context) }
                    if (count == 0) MainUtil.showToast(context, R.string.no_password) else warnPasswords = true
                }
            },
        )
    }
}

/** The "Import finished" dialog: imported / skipped (already saved) / failed. */
@Composable
fun TransferSummaryDialog(summary: TransferSummary, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.transfer_summary_title)) },
        text = { Text(stringResource(R.string.transfer_summary, summary.imported, summary.skipped, summary.failed)) },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.ok)) } },
    )
}

/** Toast after an export: "Exported: n", or "Save failed" for a negative [count]. */
fun showExportResult(context: Context, count: Int) {
    if (count < 0) MainUtil.showToast(context, R.string.save_fail)
    else MainUtil.showToastText(context, context.getString(R.string.transfer_export_done, count))
}

/**
 * What has to be passed before passwords leave the app: the password lock, else the app lock, else the device's
 * screen lock; null when none of them is set.
 */
private fun authIntent(context: Context): Intent? {
    val passLock = SecretSettings.passLockType.value
    if (passLock != 0) {
        return MainUtil.lockIntent(context, passLock).putExtra("EXTRA_PASS", 1).putExtra("EXTRA_TYPE", 2)
    }
    val appLock = SecretSettings.lockType.value
    if (appLock != 0) {
        return MainUtil.lockIntent(context, appLock).putExtra("EXTRA_TYPE", 2)
    }
    val keyguard = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager ?: return null
    if (!keyguard.isDeviceSecure) return null
    @Suppress("DEPRECATION")
    return keyguard.createConfirmDeviceCredentialIntent(
        context.getString(R.string.transfer_export_passwords),
        context.getString(R.string.transfer_password_auth),
    )
}
