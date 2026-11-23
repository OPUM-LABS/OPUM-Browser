package com.mycompany.app.ui.lists

import com.mycompany.app.ui.components.AppListItem
import android.content.ContentValues
import com.mycompany.app.pref.store.ZtwoSettings
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookAgent
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingsTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TABLE = "DbBookAgent_table"

private data class Agent(val id: Long, val title: String, val text: String)

private class AgentStore(private val context: Context) {
    private fun db() = DbBookAgent.get(context).writableDatabase

    fun load(): List<Agent> {
        val result = ArrayList<Agent>()
        val cursor = DbUtil.query(db(), TABLE, null, null, null, null) ?: return result
        try {
            val id = cursor.getColumnIndex("_id")
            val title = cursor.getColumnIndex("_title")
            val columnIndex = cursor.getColumnIndex("_text")
            while (cursor.moveToNext()) {
                val value = cursor.getString(columnIndex)
                if (value.isNullOrEmpty()) continue
                result.add(Agent(cursor.getLong(id), cursor.getString(title) ?: "", value))
            }
        } catch (_: Exception) {
        } finally {
            cursor.close()
        }
        return result
    }

    fun save(id: Long, title: String, body: String) {
        val values = ContentValues()
        values.put("_title", title)
        values.put("_text", body)
        if (id > 0) DbUtil.updateById(db(), TABLE, values, id) else DbUtil.insert(db(), TABLE, values)
    }

    fun delete(id: Long) = DbUtil.deleteById(db(), TABLE, id)

    fun deleteAll() = DbUtil.deleteWhere(db(), TABLE, null, null)
}

/**
 * User agent chooser: the four built-in agents (OPUM, Android, iPhone, iPad) and your own (a name and the agent
 * string). Tapping one selects it (stored in `ZtwoSettings.agentType`: 0-3 built-in, 100 + id custom) and closes the
 * screen; long-press a custom one to edit or delete it, + adds one. Replaces the old list screen (`MainListAgent`, now removed).
 */
class UserAgentListActivity : OpumComposeActivity() {

    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context: Context = this
        val store = remember { AgentStore(context) }
        var agents by remember { mutableStateOf<List<Agent>>(emptyList()) }
        var editing by remember { mutableStateOf<Agent?>(null) }
        var adding by remember { mutableStateOf(false) }
        var menuFor by remember { mutableStateOf<Agent?>(null) }
        var deleting by remember { mutableStateOf<Agent?>(null) }
        var deletingAll by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        suspend fun reload() { agents = withContext(Dispatchers.IO) { store.load() } }
        LaunchedEffect(Unit) { reload() }
        fun change(block: () -> Unit) { scope.launch(Dispatchers.IO) { block(); reload() } }

        fun choose(type: Int, agentText: String?) {
            val changed = ZtwoSettings.agentType.value != type || !MainUtil.sameObject(ZtwoSettings.agentUser.value, agentText)
            if (changed) {
                ZtwoSettings.editLocal { set(ZtwoSettings.agentType, type) }
                ZtwoSettings.editLocal { set(ZtwoSettings.agentUser, agentText) }
                PrefZtwo.saveAgent(context)
            }
            finish()
        }

        Scaffold(
            topBar = {
                SettingsTopBar(
                    title = stringResource(R.string.user_agent),
                    onBack = { finish() },
                    actions = {
                        IconButton(onClick = { if (agents.isNotEmpty()) deletingAll = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete_all))
                        }
                    },
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { adding = true }) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
                }
            },
        ) { padding ->
            LazyColumn(
                Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()),
                contentPadding = PaddingValues(bottom = 96.dp + padding.calculateBottomPadding()),
            ) {
                items(MainConst.agentNames.size.coerceAtMost(4)) { index ->
                    AppListItem(
                        headlineContent = { Text(MainConst.agentNames[index] ?: "") },
                        leadingContent = { RadioButton(selected = ZtwoSettings.agentType.value == index, onClick = { choose(index, null) }) },
                        modifier = Modifier.clickable { choose(index, null) },
                    )
                }
                if (agents.isNotEmpty()) item { HorizontalDivider(Modifier.padding(vertical = 8.dp)) }
                items(agents, key = { it.id }) { agent ->
                    AppListItem(
                        headlineContent = { Text(agent.title.ifEmpty { agent.text }, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        supportingContent = { Text(agent.text, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                        leadingContent = {
                            RadioButton(selected = ZtwoSettings.agentType.value == 100 + agent.id.toInt(), onClick = { choose(100 + agent.id.toInt(), agent.text) })
                        },
                        modifier = Modifier.combinedClickable(
                            onClick = { choose(100 + agent.id.toInt(), agent.text) },
                            onLongClick = { menuFor = agent },
                        ),
                    )
                }
            }
        }

        if (adding) {
            AgentDialog(null, onDismiss = { adding = false }) { title, agentText ->
                change { store.save(0, title, agentText) }
                adding = false
            }
        }
        editing?.let { agent ->
            AgentDialog(agent, onDismiss = { editing = null }) { title, agentText ->
                change {
                    store.save(agent.id, title, agentText)
                    // keep the browser on the edited agent string when this one is the selected agent
                    if (ZtwoSettings.agentType.value == 100 + agent.id.toInt() && ZtwoSettings.agentUser.value != agentText) {
                        ZtwoSettings.editLocal { set(ZtwoSettings.agentUser, agentText) }
                        PrefZtwo.saveAgent(context)
                    }
                }
                editing = null
            }
        }
        menuFor?.let { agent ->
            com.mycompany.app.ui.components.OpumSheetDialog(
                onDismissRequest = { menuFor = null },
                title = { Text(agent.title.ifEmpty { agent.text }, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                text = {
                    Column {
                        Text(
                            stringResource(R.string.edit),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth().clickable { menuFor = null; editing = agent }.padding(vertical = 12.dp),
                        )
                        Text(
                            stringResource(R.string.delete),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth().clickable { menuFor = null; deleting = agent }.padding(vertical = 12.dp),
                        )
                    }
                },
                confirmButton = {},
                dismissButton = { TextButton(onClick = { menuFor = null }) { Text(stringResource(R.string.cancel)) } },
            )
        }
        deleting?.let { agent ->
            ConfirmDialog(
                message = agent.title.ifEmpty { agent.text },
                title = stringResource(R.string.delete),
                onConfirm = { change { store.delete(agent.id) } },
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
    }
}

@Composable
private fun AgentDialog(agent: Agent?, onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var title by remember { mutableStateOf(agent?.title ?: "") }
    var agentText by remember { mutableStateOf(agent?.text ?: "") }
    com.mycompany.app.ui.components.OpumSheetDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.user_agent)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = agentText,
                    onValueChange = { agentText = it },
                    minLines = 2,
                    maxLines = 6,
                    label = { Text(stringResource(R.string.user_agent)) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    title.isBlank() -> MainUtil.showToast(context, R.string.input_name)
                    agentText.isBlank() -> MainUtil.showToast(context, R.string.empty)
                    else -> onSave(title.trim(), agentText.trim())
                }
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
