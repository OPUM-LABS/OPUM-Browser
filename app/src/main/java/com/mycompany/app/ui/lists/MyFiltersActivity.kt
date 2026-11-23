package com.mycompany.app.ui.lists

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.data.book.DataBookFilter
import com.mycompany.app.data.book.DataBookUser
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookFilter
import com.mycompany.app.db.book.DbBookUser
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingsTopBar
import com.mycompany.app.web.filter.UboEngine
import com.mycompany.app.web.filter.UboFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * "My filters" (uBlock Origin style): the user's own rules as one text, one rule per line. Opened from User filter > Edit as text. Shows the enabled rules of `DbBookUser_table`; turned-off rules
 * stay as they are and are only counted. Lines the engine cannot parse are listed under the field ("Line n: …").
 * Save applies the difference: new lines become enabled rules (host column as `DialogEditCmd` sets it), lines that
 * match a turned-off rule turn it on, removed lines delete their rule; then the browser's rule cache and the
 * engine are refreshed. Back with changes asks before discarding.
 */
class MyFiltersActivity : OpumComposeActivity() {
    private class Row(val id: Long, val text: String, val enabled: Boolean)

    private var original by mutableStateOf<String?>(null)
    private var disabledCount by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        val app = applicationContext
        Thread({
            val rows = readRows(app)
            val text = rows.filter { it.enabled }.joinToString("\n") { it.text }
            runOnUiThread {
                disabledCount = rows.count { !it.enabled }
                original = text
            }
        }, "MyFiltersLoad").start()
    }

    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val loadedText = original
        var text by remember(loadedText) { mutableStateOf(loadedText ?: "") }
        var errors by remember { mutableStateOf<List<String>>(emptyList()) }
        var askDiscard by remember { mutableStateOf(false) }
        var saving by remember { mutableStateOf(false) }
        val changed = loadedText != null && text != loadedText

        // Validate (debounced) with the engine's parser.
        LaunchedEffect(text) {
            delay(400)
            val lines = text.lines()
            errors = withContext(Dispatchers.Default) {
                val out = ArrayList<String>()
                for ((i, raw) in lines.withIndex()) {
                    val line = raw.trim()
                    if (line.isEmpty() || line.startsWith("!")) continue
                    val err = UboEngine.validate(line) ?: continue
                    out.add(getString(R.string.ubo_line_error, i + 1, "$line  ($err)"))
                    if (out.size >= 20) break
                }
                out
            }
        }

        fun close() = if (changed) askDiscard = true else finish()
        BackHandler { close() }

        Scaffold(
            topBar = {
                SettingsTopBar(
                    title = stringResource(R.string.ubo_my_filters),
                    onBack = { close() },
                    actions = {
                        TextButton(enabled = changed && !saving, onClick = {
                            saving = true
                            val app = applicationContext
                            val newText = text
                            Thread({
                                save(app, newText)
                                runOnUiThread { finish() }
                            }, "MyFiltersSave").start()
                        }) { Text(stringResource(R.string.save)) }
                    },
                )
            },
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            ) {
                Text(
                    stringResource(R.string.ubo_my_filters_info), style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    enabled = loadedText != null && !saving,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 240.dp),
                )
                if (disabledCount > 0) {
                    Text(
                        "+ $disabledCount " + stringResource(R.string.not_used), style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                for (e in errors) {
                    Text(
                        e, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
        if (askDiscard) {
            ConfirmDialog(
                message = stringResource(R.string.exit_without_save),
                onConfirm = { finish() },
                onDismiss = { askDiscard = false },
            )
        }
    }

    companion object {
        private fun readRows(context: Context): List<Row> {
            val out = ArrayList<Row>()
            val cursor = DbUtil.query(
                DbBookUser.get(context).getWritableDatabase(), "DbBookUser_table",
                arrayOf<String?>("_id", "_path", "_use"), null, null, "_id ASC",
            ) ?: return out
            try {
                val cId = cursor.getColumnIndex("_id")
                val cPath = cursor.getColumnIndex("_path")
                val cUse = cursor.getColumnIndex("_use")
                if (cursor.moveToFirst()) do {
                    val p = cursor.getString(cPath) ?: continue
                    out.add(Row(cursor.getLong(cId), p, cursor.getInt(cUse) == 1))
                } while (cursor.moveToNext())
            } catch (_: Exception) {
            } finally {
                cursor.close()
            }
            return out
        }

        /** Applies [text] to `DbBookUser_table` (see the class comment). Runs off the main thread. */
        @JvmStatic
        fun save(context: Context, text: String) {
            val wanted = LinkedHashSet<String>()
            for (raw in text.lines()) {
                val line = raw.trim()
                if (line.isNotEmpty()) wanted.add(line)
            }
            val rows = readRows(context)
            val db = DbBookUser.get(context).getWritableDatabase()
            val enabledByText = rows.filter { it.enabled }.groupBy { it.text.trim() }
            val disabledByText = rows.filter { !it.enabled }.groupBy { it.text.trim() }
            // removed lines: delete their enabled rows
            for ((t, list) in enabledByText) {
                if (t !in wanted) list.forEach { DbUtil.deleteById(db, "DbBookUser_table", it.id) }
            }
            for (line in wanted) {
                if (enabledByText.containsKey(line)) continue
                val off = disabledByText[line]?.firstOrNull()
                if (off != null) {
                    DbBookUser.setUse(context, off.id, true)
                } else {
                    DbBookUser.saveRule(context, 0L, MainUtil.stripHostPrefix(DbBookUser.hostOfRule(line)), line)
                }
            }
            DataBookUser.instance(context).clear()
            if (wanted.isNotEmpty()) {
                DataBookFilter.instance(context).putItem(DbBookFilter.addPathItem(context, "sb_user_filter_path", "sb_user_filter_path"))
            }
            UboFilter.onListsChanged(context)
        }
    }
}
