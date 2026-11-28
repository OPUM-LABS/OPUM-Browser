package com.mycompany.app.ui.settings

import android.content.Context
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.data.sync.SyncConfig
import com.mycompany.app.data.sync.SyncError
import com.mycompany.app.net.LocalNet
import com.mycompany.app.pref.store.Workspaces
import com.mycompany.app.sync.BookmarkSync
import com.mycompany.app.sync.BookmarkSyncStore
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader

/**
 * Bookmark sync (2026-11-28): where the workspace in use keeps its bookmarks together with the floccus extension of
 * the desktop browsers (a WebDAV folder or an S3 bucket, the file, an optional passphrase), "Sync now" and the result
 * of the last sync. Each workspace has its own setting; this page shows the one in use. Saved passwords are never
 * shown again: their field stays empty, and an empty field keeps them.
 */
class BookmarkSyncSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val number = remember { Workspaces.currentNumber() }
        var config by remember { mutableStateOf(BookmarkSyncStore.config(context, number)) }
        val done by BookmarkSync.done.collectAsState()
        val busySet by BookmarkSync.busy.collectAsState()
        val busy = number in busySet
        val status = remember(done) { BookmarkSyncStore.state(context, number).status }
        var editing by remember { mutableIntStateOf(0) }
        var trusting by remember { mutableStateOf<String?>(null) }

        fun store(changed: SyncConfig, syncNow: Boolean) {
            config = changed
            Thread {
                BookmarkSyncStore.save(context, number, changed)
                BookmarkSync.schedule(context)
                if (syncNow && changed.enabled && changed.isSet) BookmarkSync.sync(context, number)
            }.start()
        }

        SettingsScaffold(title = stringResource(R.string.bsync_title), onBack = { finish() }) {
            settingsFooter(getString(R.string.bsync_space, Workspaces.nameOf(number, getString(R.string.space_main))))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.bsync_on),
                    summary = stringResource(R.string.bsync_on_sum),
                    checked = config.enabled && config.isSet,
                    enabled = config.isSet,
                    onCheckedChange = { store(config.copy(enabled = it), it) },
                )
            }
            settingsHeader(getString(R.string.bsync_hd_server))
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.bsync_kind),
                    options = listOf(stringResource(R.string.bsync_webdav), stringResource(R.string.bsync_s3)),
                    selectedIndex = config.kind,
                    onSelect = { if (it != config.kind) store(config.copy(kind = it), false) },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.bsync_server),
                    summary = serverSummary(config),
                    onClick = { editing = 1 },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.bsync_file),
                    summary = config.file,
                    onClick = { editing = 2 },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.bsync_pass),
                    summary = stringResource(if (config.passphrase.isEmpty()) R.string.btn_off else R.string.btn_on),
                    onClick = { editing = 3 },
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.bsync_background),
                    summary = stringResource(R.string.bsync_background_sum),
                    checked = config.background,
                    onCheckedChange = { store(config.copy(background = it), false) },
                )
            }
            settingsHeader(getString(R.string.bsync_hd_status))
            item {
                SettingRow(
                    title = stringResource(R.string.bsync_now),
                    summary = if (busy) stringResource(R.string.bsync_busy) else statusText(context, status, config),
                    enabled = config.isSet && !busy,
                    onClick = { BookmarkSync.syncAsync(context, number) },
                )
            }
            val fp = status?.fingerprint
            if (status?.error == SyncError.CERT && fp != null && fp != config.pin) {
                item {
                    SettingRow(
                        title = stringResource(R.string.bsync_trust),
                        summary = stringResource(R.string.bsync_trust_sum),
                        onClick = { trusting = fp },
                    )
                }
            }
            if (status?.error == SyncError.LOCAL_NET) {
                item {
                    SettingRow(
                        title = stringResource(R.string.net_local_perm),
                        summary = stringResource(R.string.bsync_local_sum),
                        onClick = { if (!LocalNet.request(this@BookmarkSyncSettingsActivity)) LocalNet.openAppSettings(context) },
                    )
                }
            }
            settingsFooter(getString(R.string.bsync_info))
        }

        when (editing) {
            1 -> if (config.kind == SyncConfig.S3) {
                FieldsDialog(
                    title = stringResource(R.string.bsync_server),
                    fields = listOf(
                        Field(stringResource(R.string.bsync_endpoint), config.endpoint, KeyboardType.Uri, hint = "https://s3.eu-central-1.amazonaws.com"),
                        Field(stringResource(R.string.bsync_region), config.region, KeyboardType.Ascii, hint = "us-east-1"),
                        Field(stringResource(R.string.bsync_bucket), config.bucket, KeyboardType.Ascii),
                        Field(stringResource(R.string.bsync_access), config.accessKey, KeyboardType.Ascii),
                        Field(stringResource(R.string.bsync_secret), "", KeyboardType.Password, secret = true, saved = config.secretKey.isNotEmpty()),
                    ),
                    info = stringResource(R.string.bsync_s3_info),
                    onSave = { v ->
                        editing = 0
                        store(config.copy(endpoint = v[0], region = v[1], bucket = v[2], accessKey = v[3],
                            secretKey = v[4].ifEmpty { config.secretKey }, pin = "", enabled = true), true)
                    },
                    onDismiss = { editing = 0 },
                )
            } else {
                FieldsDialog(
                    title = stringResource(R.string.bsync_server),
                    fields = listOf(
                        Field(stringResource(R.string.bsync_url), config.url, KeyboardType.Uri, hint = "https://cloud.example.com/remote.php/dav/files/…/"),
                        Field(stringResource(R.string.bsync_user), config.user, KeyboardType.Email),
                        Field(stringResource(R.string.password), "", KeyboardType.Password, secret = true, saved = config.password.isNotEmpty()),
                    ),
                    info = stringResource(R.string.bsync_url_info),
                    onSave = { v ->
                        editing = 0
                        store(config.copy(url = v[0], user = v[1], password = v[2].ifEmpty { config.password }, pin = "", enabled = true), true)
                    },
                    onDismiss = { editing = 0 },
                )
            }
            2 -> FieldsDialog(
                title = stringResource(R.string.bsync_file),
                fields = listOf(Field(stringResource(R.string.bsync_file), config.file, KeyboardType.Uri, hint = SyncConfig.DEFAULT_FILE)),
                info = stringResource(R.string.bsync_file_info),
                onSave = { v ->
                    editing = 0
                    store(config.copy(file = v[0].trim('/').ifEmpty { SyncConfig.DEFAULT_FILE }), true)
                },
                onDismiss = { editing = 0 },
            )
            3 -> FieldsDialog(
                title = stringResource(R.string.bsync_pass),
                fields = listOf(Field(stringResource(R.string.bsync_pass), "", KeyboardType.Password, secret = true, saved = config.passphrase.isNotEmpty())),
                info = stringResource(R.string.bsync_pass_info),
                onSave = { v ->
                    editing = 0
                    if (v[0].isNotEmpty()) store(config.copy(passphrase = v[0]), true)
                },
                extra = if (config.passphrase.isNotEmpty()) (stringResource(R.string.bsync_pass_off) to {
                    editing = 0
                    store(config.copy(passphrase = ""), true)
                }) else null,
                onDismiss = { editing = 0 },
            )
        }
        trusting?.let { fp ->
            ConfirmDialog(
                title = stringResource(R.string.bsync_trust),
                message = getString(R.string.bsync_trust_ask, config.host ?: "", fp.chunked(2).joinToString(":").uppercase()),
                onConfirm = { trusting = null; store(config.copy(pin = fp), true) },
                onDismiss = { trusting = null },
            )
        }
    }

    private fun serverSummary(c: SyncConfig): String {
        if (!c.isSet) return getString(R.string.bsync_not_set)
        return if (c.kind == SyncConfig.S3) "${c.bucket} · ${c.host ?: c.endpoint}" else (c.user.takeIf { it.isNotEmpty() }?.let { "$it · " } ?: "") + (c.host ?: c.url)
    }

    companion object {
        fun statusText(context: Context, s: BookmarkSyncStore.Status?, config: SyncConfig): String {
            if (!config.isSet) return context.getString(R.string.bsync_not_set)
            if (s == null) return context.getString(R.string.bsync_never)
            val time = DateUtils.formatDateTime(context, s.time, DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_MONTH)
            if (s.error == 0) {
                val counts = if (s.added + s.changed + s.removed > 0) "\n" + context.getString(R.string.bsync_counts, s.added, s.changed, s.removed) else ""
                return context.getString(R.string.bsync_ok, time) + counts
            }
            val why = context.getString(
                when (s.error) {
                    SyncError.NETWORK -> R.string.bsync_e_network
                    SyncError.AUTH -> R.string.bsync_e_auth
                    SyncError.FOLDER -> R.string.bsync_e_folder
                    SyncError.LOCKED -> R.string.bsync_e_locked
                    SyncError.CERT -> R.string.bsync_e_cert
                    SyncError.PASSPHRASE -> R.string.bsync_e_pass
                    SyncError.NEED_PASSPHRASE -> R.string.bsync_e_need_pass
                    SyncError.BAD_FILE -> R.string.bsync_e_file
                    SyncError.LOCAL_NET -> R.string.bsync_e_local
                    SyncError.NOT_SET -> R.string.bsync_not_set
                    else -> R.string.bsync_e_server
                },
            )
            val detail = s.detail?.takeIf { s.error == SyncError.SERVER || s.error == SyncError.NETWORK }?.let { " ($it)" } ?: ""
            return context.getString(R.string.bsync_failed, time, why + detail)
        }
    }
}

private class Field(
    val label: String,
    val value: String,
    val type: KeyboardType,
    val hint: String? = null,
    val secret: Boolean = false,
    /** A secret is saved already: an empty field keeps it. */
    val saved: Boolean = false,
)

/** A few fields in one dialog: Next moves on, Done on the last one saves. */
@Composable
private fun FieldsDialog(
    title: String,
    fields: List<Field>,
    info: String?,
    onSave: (List<String>) -> Unit,
    onDismiss: () -> Unit,
    extra: Pair<String, () -> Unit>? = null,
) {
    val values = remember { mutableStateListOf<String>().apply { fields.forEach { add(it.value) } } }
    // one focus target per field: Next moves to the following one (moving focus by direction does not work in this dialog)
    val targets = remember { fields.map { FocusRequester() } }
    fun result() = values.mapIndexed { i, v -> if (fields[i].secret) v else v.trim() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                fields.forEachIndexed { i, f ->
                    val last = i == fields.lastIndex
                    OutlinedTextField(
                        value = values[i],
                        onValueChange = { values[i] = it },
                        singleLine = true,
                        label = { Text(f.label) },
                        placeholder = (if (f.saved) stringResource(R.string.bsync_keep) else f.hint)?.let { { Text(it) } },
                        visualTransformation = if (f.secret) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
                        keyboardOptions = KeyboardOptions(keyboardType = f.type, autoCorrectEnabled = false, imeAction = if (last) ImeAction.Done else ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { targets.getOrNull(i + 1)?.requestFocus() }, onDone = { onSave(result()) }),
                        modifier = Modifier.fillMaxWidth().padding(top = if (i == 0) 0.dp else 4.dp).focusRequester(targets[i]),
                    )
                }
                if (!info.isNullOrEmpty()) Text(info, modifier = Modifier.padding(top = 12.dp))
            }
        },
        confirmButton = { TextButton(onClick = { onSave(result()) }) { Text(stringResource(R.string.save)) } },
        dismissButton = {
            Row {
                if (extra != null) TextButton(onClick = extra.second) { Text(extra.first) }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        },
    )
}
