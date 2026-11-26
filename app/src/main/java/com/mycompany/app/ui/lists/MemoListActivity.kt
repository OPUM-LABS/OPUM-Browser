package com.mycompany.app.ui.lists

import com.mycompany.app.ui.components.AppListItem
import android.content.ContentValues
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookMemo
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingsTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date
import com.mycompany.app.ui.components.Sym

private const val TABLE = "DbBookMemo_table"

private data class Memo(val id: Long, val text: String, val time: Long, val order: Long)

/** The memo store: same table as the old memo list (`_text`, `_time`, `_order`; new memos get the current time as order). */
private class MemoStore(private val context: Context) {
    private fun db() = DbBookMemo.get(context).writableDatabase

    fun load(): List<Memo> {
        val result = ArrayList<Memo>()
        val cursor = DbUtil.query(db(), TABLE, null, null, null, "_order ASC") ?: return result
        try {
            val id = cursor.getColumnIndex("_id")
            val columnIndex = cursor.getColumnIndex("_text")
            val time = cursor.getColumnIndex("_time")
            val order = cursor.getColumnIndex("_order")
            while (cursor.moveToNext()) {
                result.add(Memo(cursor.getLong(id), cursor.getString(columnIndex) ?: "", cursor.getLong(time), cursor.getLong(order)))
            }
        } catch (_: Exception) {
        } finally {
            cursor.close()
        }
        // same ordering as before: order, then time
        return result.sortedWith(compareBy<Memo> { it.order }.thenBy { it.time })
    }

    fun save(id: Long, cropped: String) {
        val values = ContentValues()
        values.put("_text", cropped)
        if (id > 0) {
            DbUtil.updateById(db(), TABLE, values, id)
        } else {
            val now = System.currentTimeMillis()
            values.put("_time", now)
            values.put("_order", now)
            DbUtil.insert(db(), TABLE, values)
        }
    }

    fun delete(id: Long) = DbUtil.deleteById(db(), TABLE, id)

    fun deleteAll() = DbUtil.deleteWhere(db(), TABLE, null, null)

    fun setOrder(id: Long, order: Long) {
        val values = ContentValues()
        values.put("_order", order)
        DbUtil.updateById(db(), TABLE, values, id)
    }
}

/**
 * Memo list: short notes kept on the device. Tap a memo to copy it, long-press (or the menu) for edit, delete,
 * details and moving it up, down, to the top or the bottom; + adds one; the bar has search and delete all.
 * Replaces the old list screen (`MainListMemo`); same table, same order rules.
 */
class MemoListActivity : OpumComposeActivity() {

    @OptIn(ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context: Context = this
        val store = remember { MemoStore(context) }
        var memos by remember { mutableStateOf<List<Memo>>(emptyList()) }
        var loaded by remember { mutableStateOf(false) }
        var query by remember { mutableStateOf("") }
        var searching by remember { mutableStateOf(false) }
        var editing by remember { mutableStateOf<Memo?>(null) }
        var adding by remember { mutableStateOf(false) }
        var menuFor by remember { mutableStateOf<Memo?>(null) }
        var deleting by remember { mutableStateOf<Memo?>(null) }
        var deletingAll by remember { mutableStateOf(false) }
        var details by remember { mutableStateOf<Memo?>(null) }

        suspend fun reload() { memos = withContext(Dispatchers.IO) { store.load() }; loaded = true }
        LaunchedEffect(Unit) { reload() }
        val scope = androidx.compose.runtime.rememberCoroutineScope()
        fun change(block: () -> Unit) {
            scope.launch(Dispatchers.IO) { block(); reload() }
        }

        val shown = if (query.isBlank()) memos else memos.filter { it.text.contains(query.trim(), ignoreCase = true) }

        Scaffold(
            topBar = {
                SettingsTopBar(
                    title = stringResource(R.string.memo_title),
                    onBack = { finish() },
                    actions = {
                        IconButton(onClick = { searching = !searching; if (!searching) query = "" }) {
                            Icon(if (searching) Sym.Close else Sym.Search, contentDescription = null)
                        }
                        IconButton(onClick = { if (memos.isNotEmpty()) deletingAll = true }) {
                            Icon(Sym.Delete, contentDescription = stringResource(R.string.delete_all))
                        }
                    },
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { adding = true }) {
                    Icon(Sym.Add, contentDescription = stringResource(R.string.add))
                }
            },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
                if (searching) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        label = { Text(stringResource(R.string.search_url)) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
                LazyColumn(contentPadding = PaddingValues(bottom = 96.dp + padding.calculateBottomPadding())) {
                    items(shown, key = { it.id }) { memo ->
                        AppListItem(
                            headlineContent = { Text(memo.text, maxLines = 4, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.combinedClickable(
                                onClick = { MainUtil.copyTextToast(context, R.string.copied_clipboard, "Copied memo", memo.text) },
                                onLongClick = { menuFor = memo },
                            ),
                        )
                    }
                }
            }
        }

        if (adding) {
            MemoDialog(initial = "", onDismiss = { adding = false }) { title2 ->
                change { store.save(0, title2) }
                adding = false
            }
        }
        editing?.let { memo ->
            MemoDialog(initial = memo.text, onDismiss = { editing = null }) { title2 ->
                change { store.save(memo.id, title2) }
                editing = null
            }
        }
        menuFor?.let { memo ->
            val index = memos.indexOfFirst { it.id == memo.id }
            fun swap(other: Memo) = change { store.setOrder(memo.id, other.order); store.setOrder(other.id, memo.order) }
            com.mycompany.app.ui.components.OpumSheetDialog(
                onDismissRequest = { menuFor = null },
                title = { Text(memo.text, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                text = {
                    Column {
                        MemoAction(stringResource(R.string.edit)) { menuFor = null; editing = memo }
                        MemoAction(stringResource(R.string.delete)) { menuFor = null; deleting = memo }
                        MemoAction(stringResource(R.string.details)) { menuFor = null; details = memo }
                        if (index > 0) {
                            MemoAction(stringResource(R.string.move_top)) {
                                menuFor = null; change { store.setOrder(memo.id, memos.first().order - 1) }
                            }
                            MemoAction(stringResource(R.string.move_up)) { menuFor = null; swap(memos[index - 1]) }
                        }
                        if (index in 0 until memos.size - 1) {
                            MemoAction(stringResource(R.string.move_down)) { menuFor = null; swap(memos[index + 1]) }
                            MemoAction(stringResource(R.string.move_bot)) {
                                menuFor = null; change { store.setOrder(memo.id, memos.last().order + 1) }
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = { TextButton(onClick = { menuFor = null }) { Text(stringResource(R.string.cancel)) } },
            )
        }
        deleting?.let { memo ->
            ConfirmDialog(
                message = memo.text.take(120),
                title = stringResource(R.string.delete),
                onConfirm = { change { store.delete(memo.id) } },
                onDismiss = { deleting = null },
            )
        }
        if (deletingAll) {
            ConfirmDialog(
                message = stringResource(R.string.del_all_confirm),
                title = stringResource(R.string.delete_all),
                onConfirm = { change { store.deleteAll() } },
                onDismiss = { deletingAll = false },
            )
        }
        details?.let { memo ->
            com.mycompany.app.ui.components.OpumSheetDialog(
                onDismissRequest = { details = null },
                title = { Text(stringResource(R.string.details)) },
                text = {
                    Column {
                        Text(memo.text, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            DateFormat.getDateTimeInstance().format(Date(memo.time)),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                },
                confirmButton = { TextButton(onClick = { details = null }) { Text(stringResource(R.string.ok)) } },
            )
        }
    }
}

@Composable
private fun MemoAction(label: String, onClick: () -> Unit) {
    Text(
        label,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
    )
}

@Composable
private fun MemoDialog(initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var url by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.memo_title)) },
        text = {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                minLines = 3,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (url.isBlank()) MainUtil.showToast(context, R.string.empty) else onSave(url)
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
