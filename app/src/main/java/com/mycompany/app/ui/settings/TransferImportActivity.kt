package com.mycompany.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.data.transfer.BrowserTransfer
import com.mycompany.app.data.transfer.TransferSummary
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.OpumComposeActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * "Import into OPUM Browser" in the share sheet: another app (Chrome's "Export passwords", a file manager, …) shares
 * a bookmark HTML file or a password CSV file; this translucent screen reads it, says what it found and asks before
 * importing (a shared file never changes anything without the user's OK), then shows the usual summary and closes.
 * Only `content://` streams are accepted. The import itself is [BrowserTransfer], as in Backup & restore.
 */
class TransferImportActivity : OpumComposeActivity() {

    private sealed interface Step {
        data object Reading : Step
        class Ask(val text: String, val kind: BrowserTransfer.FileKind) : Step
        data object Importing : Step
        class Done(val summary: TransferSummary) : Step
    }

    @Composable
    override fun Content() {
        val scope = rememberCoroutineScope()
        var step by remember { mutableStateOf<Step>(Step.Reading) }

        LaunchedEffect(Unit) {
            val uri = sharedUri(intent)
            val found = if (uri == null) null else withContext(Dispatchers.IO) {
                val text = BrowserTransfer.readText(applicationContext, uri) ?: return@withContext null
                val kind = try { BrowserTransfer.detect(text) } catch (_: Exception) { null } ?: return@withContext null
                Step.Ask(text, kind)
            }
            if (found == null || found.kind.entries == 0) {
                MainUtil.showToast(applicationContext, R.string.invalid_file)
                finish()
            } else {
                step = found
            }
        }

        when (val s = step) {
            Step.Reading, Step.Importing -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is Step.Ask -> AlertDialog(
                onDismissRequest = { finish() },
                title = {
                    Text(stringResource(if (s.kind.passwords) R.string.transfer_import_passwords else R.string.transfer_import_bookmarks))
                },
                text = { Text(stringResource(R.string.transfer_share_found, s.kind.entries)) },
                confirmButton = {
                    TextButton(onClick = {
                        step = Step.Importing
                        scope.launch {
                            val summary = withContext(Dispatchers.IO) {
                                try {
                                    if (s.kind.passwords) BrowserTransfer.importPasswords(applicationContext, s.text)
                                    else BrowserTransfer.importBookmarks(applicationContext, s.text)
                                } catch (_: Exception) {
                                    null
                                }
                            }
                            if (summary == null) {
                                MainUtil.showToast(applicationContext, R.string.invalid_file)
                                finish()
                            } else {
                                step = Step.Done(summary)
                            }
                        }
                    }) { Text(stringResource(R.string.backup_import)) }
                },
                dismissButton = { TextButton(onClick = { finish() }) { Text(stringResource(R.string.cancel)) } },
            )
            is Step.Done -> TransferSummaryDialog(s.summary) { finish() }
        }
    }

    /** The shared file (`EXTRA_STREAM`, or the data of a view intent), only as a `content://` URI. */
    private fun sharedUri(intent: Intent?): Uri? {
        intent ?: return null
        val uri: Uri? = if (intent.action == Intent.ACTION_SEND) {
            // the typed getParcelableExtra is Android 13+; the app runs from Android 12 (it crashed there)
            androidx.core.content.IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            intent.data
        }
        return uri?.takeIf { it.scheme == "content" }
    }
}
