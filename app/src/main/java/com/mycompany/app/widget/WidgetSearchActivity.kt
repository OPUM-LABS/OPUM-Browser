package com.mycompany.app.widget

import androidx.compose.runtime.mutableIntStateOf
import com.mycompany.app.ui.components.AppListItem
import android.content.ActivityNotFoundException
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.BrowserSettings
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.webkit.URLUtil
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookHistory
import com.mycompany.app.db.book.DbBookQuick
import com.mycompany.app.db.book.DbBookRecent
import com.mycompany.app.db.book.DbBookSearch
import com.mycompany.app.db.book.DbBookWeb
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.web.WebSearch.WebSchItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The search box opened from the home-screen search widget and the lock screen's voice search: type to get suggestions
 * (recent searches, history, bookmarks and quick links, each as enabled in the settings), pick one or press search to
 * open it in the browser; the engine button switches the search engine and the microphone starts voice search
 * (`EXTRA_VOICE` starts it straight away). When the app is locked and the screen was not opened from the lock
 * screen (`EXTRA_LOCK`) it hands over to the lock screen first. The class name stays because the widget's pending
 * intents and the lock screen refer to it. Replaces the old `MainActivity`-based screen.
 */
class WidgetSearchActivity : OpumComposeActivity() {

    private class Suggestion(val suggestKind: Int, val id: Long, val title: String?, val address: String?)

    private var voiceAtStart = false
    private var redirected = false

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        val fromLock = intent.getBooleanExtra("EXTRA_LOCK", false)
        voiceAtStart = intent.getBooleanExtra("EXTRA_VOICE", false)
        val context = applicationContext
        MainApp.loadSettingsOnce(context, resources)
        if (fromLock || MainUtil.isLockEnabled()) MainApp.from(context)?.skipLockOnce = true
        if (!fromLock && MainUtil.isLockEnabled()) {
            redirected = true
            super.onCreate(savedInstanceState)
            finishAffinity()
            startActivity(MainUtil.lockIntent(context, SecretSettings.lockType.value).putExtra("EXTRA_TYPE", 4).putExtra("EXTRA_VOICE", voiceAtStart))
            return
        }
        super.onCreate(savedInstanceState)
    }

    private fun open(address: String?) {
        if (address.isNullOrEmpty()) return
        finishAffinity()
        startActivity(MainUtil.browserIntent(applicationContext).putExtra("EXTRA_PATH", address))
    }

    private fun search(query: String) {
        val value = query.trim()
        if (value.isEmpty()) return
        open(MainUtil.normalizeUrl(applicationContext, value))
    }

    private fun loadSuggestions(): List<Suggestion> {
        val context = applicationContext
        val result = ArrayList<Suggestion>()
        val flags = BrowserSettings.sugType.value
        val recentOn = (flags and 2) == 2
        val historyOn = (flags and 4) == 4
        val bookmarksOn = (flags and 8) == 8
        val quickOn = (flags and 16) == 16
        val secret = com.mycompany.app.pref.store.Workspaces.dataKey()
        if (recentOn && DbBookRecent.isEnabled()) {
            try {
                DbBookRecent.loadItems(context)?.forEach { item -> if (item != null) result.add(Suggestion(37, item.id, item.title, null)) }
            } catch (_: Exception) {
            }
        }
        if (quickOn) {
            try {
                DbUtil.query(DbBookQuick.get(context).writableDatabase, "DbBookQuick_table", arrayOf("_id", "_path", "_title"), "_secret=?", arrayOf(com.mycompany.app.pref.store.Workspaces.sharedKey()), null)?.use {
                    val id = it.getColumnIndex("_id"); val path = it.getColumnIndex("_path"); val title = it.getColumnIndex("_title")
                    while (it.moveToNext()) {
                        val address = it.getString(path)
                        if (URLUtil.isNetworkUrl(address)) result.add(Suggestion(32, it.getLong(id), it.getString(title), address))
                    }
                }
            } catch (_: Exception) {
            }
        }
        if (historyOn && BrowserSettings.historyTime.value != 0 && !(SyncSettings.secretMode.value && SecretSettings.secretHist.value == 0L)) {
            var since = 0L
            if (BrowserSettings.historyTime.value != -1) {
                since = System.currentTimeMillis() - BrowserSettings.historyTime.value * 86400000L
                if (SyncSettings.secretMode.value && SecretSettings.secretHist.value > since) since = SecretSettings.secretHist.value
            }
            try {
                DbUtil.query(DbBookHistory.get(context).writableDatabase, "DbBookHistory_table", arrayOf("_id", "_path", "_title"), "_secret=? AND _time>?", arrayOf(secret, since.toString()), "_time DESC")?.use {
                    val id = it.getColumnIndex("_id"); val path = it.getColumnIndex("_path"); val title = it.getColumnIndex("_title")
                    var count = 0
                    while (it.moveToNext() && count < 100) {
                        val address = it.getString(path)
                        if (URLUtil.isNetworkUrl(address)) { result.add(Suggestion(18, it.getLong(id), it.getString(title), address)); count++ }
                    }
                }
            } catch (_: Exception) {
            }
        }
        if (bookmarksOn) {
            try {
                DbUtil.query(DbBookWeb.get(context).writableDatabase, "DbBookWeb_table", arrayOf("_id", "_isdir", "_path", "_title"), "_secret=?", arrayOf(com.mycompany.app.pref.store.Workspaces.sharedKey()), null)?.use {
                    val id = it.getColumnIndex("_id"); val dir = it.getColumnIndex("_isdir"); val path = it.getColumnIndex("_path"); val title = it.getColumnIndex("_title")
                    while (it.moveToNext()) {
                        if (it.getInt(dir) == 1) continue
                        val address = it.getString(path)
                        if (URLUtil.isNetworkUrl(address)) result.add(Suggestion(17, it.getLong(id), it.getString(title), address))
                    }
                }
            } catch (_: Exception) {
            }
        }
        return result
    }

    private fun startVoice(launch: (Intent) -> Unit) {
        try {
            val speech = Intent("android.speech.action.RECOGNIZE_SPEECH")
            speech.putExtra("android.speech.extra.LANGUAGE_MODEL", "web_search")
            MainUtil.languageTag(AppState.appLocale)?.takeIf { it.isNotEmpty() }?.let { speech.putExtra("android.speech.extra.LANGUAGE", it) }
            launch(speech)
        } catch (_: ActivityNotFoundException) {
            MainUtil.showToast(this, R.string.apps_none)
        } catch (_: Exception) {
            MainUtil.showToast(this, R.string.not_supported)
        }
    }

    @Composable
    override fun Content() {
        if (redirected) return
        val context: Context = this
        var query by remember { mutableStateOf("") }
        var items by remember { mutableStateOf<List<Suggestion>>(emptyList()) }
        var clip by remember { mutableStateOf<String?>(null) }
        var engineMenu by remember { mutableStateOf(false) }
        var engineTick by remember { mutableIntStateOf(0) }
        var customEngines by remember { mutableStateOf<List<WebSchItem>>(emptyList()) }
        var clearAsk by remember { mutableStateOf(false) }
        val focus = remember { FocusRequester() }
        val voice = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val spoken = result.data?.getStringArrayListExtra("android.speech.extra.RESULTS")?.firstOrNull()
            if (result.resultCode == RESULT_OK && !spoken.isNullOrEmpty()) open(MainUtil.searchUrlFor(spoken)) else if (result.resultCode == RESULT_OK) MainUtil.showToast(this, R.string.fail)
        }

        LaunchedEffect(Unit) {
            items = withContext(Dispatchers.IO) { loadSuggestions() }
            customEngines = withContext(Dispatchers.IO) { (DbBookSearch.engines(context) as? List<*>)?.filterIsInstance<WebSchItem>() ?: emptyList() }
            try { focus.requestFocus() } catch (_: Exception) {}
            if (voiceAtStart) { voiceAtStart = false; startVoice { voice.launch(it) } }
        }
        LaunchedEffect(Unit) {
            try {
                val manager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clip = manager.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString()?.trim()?.takeIf { it.isNotEmpty() && it.length < 2000 }
            } catch (_: Exception) {
            }
        }

        val shown = remember(items, query) {
            val words = query.trim().lowercase().split(" ").filter { it.isNotEmpty() }
            if (words.isEmpty()) items.filter { it.suggestKind == 37 || it.suggestKind == 32 }.take(30)
            else items.filter { item ->
                val haystack = ((item.title ?: "") + " " + (item.address ?: "")).lowercase()
                words.all { haystack.contains(it) }
            }.take(40)
        }

        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { finish() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
                    OutlinedTextField(
                        value = query, onValueChange = { query = it },
                        modifier = Modifier.weight(1f).focusRequester(focus),
                        singleLine = true,
                        placeholder = { Text(stringResource(R.string.web_edit_hint)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { search(query) }),
                        leadingIcon = {
                            Box {
                                IconButton(onClick = { engineMenu = true }) {
                                    val selected = ZtwoSettings.searchType.value
                                    @Suppress("UNUSED_EXPRESSION") engineTick
                                    if (selected in 0 until MainConst.engineIcons.size) com.mycompany.app.ui.components.EngineLogo(selected, 24.dp) { Icon(painterResource(MainConst.engineIcons[selected]), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(24.dp)) }
                                    else Icon(Icons.Filled.Search, contentDescription = null)
                                }
                                com.mycompany.app.ui.components.BubbleMenu(expanded = engineMenu, onDismiss = { engineMenu = false }) {
                                    val enabled = MainUtil.zoneFlags()
                                    val korean = MainUtil.isKoreanLocale()
                                    for (position in 0 until MainConst.engineNames.size) {
                                        val index = if (korean) MainConst.engineOrderKo[position] else position
                                        if (index !in enabled.indices || !enabled[index]) continue
                                        DropdownMenuItem(
                                            leadingIcon = { com.mycompany.app.ui.components.EngineLogo(index, 24.dp) { Icon(painterResource(MainConst.engineIcons[index]), null, tint = Color.Unspecified, modifier = Modifier.size(24.dp)) } },
                                            text = { Text(stringResource(MainConst.engineNames[index])) },
                                            onClick = {
                                                engineMenu = false
                                                if (ZtwoSettings.searchType.value != index) { ZtwoSettings.editLocal { set(ZtwoSettings.searchType, index) }; ZtwoSettings.editLocal { set(ZtwoSettings.searchUser, null) }; ZtwoSettings.editLocal { set(ZtwoSettings.searchColor, 0) }; PrefZtwo.saveSearch(applicationContext); engineTick++ }
                                            },
                                        )
                                    }
                                    for (engine in customEngines) {
                                        DropdownMenuItem(
                                            text = { Text(engine.name ?: "") },
                                            onClick = {
                                                engineMenu = false
                                                val index = engine.id.toInt() + 100
                                                ZtwoSettings.editLocal { set(ZtwoSettings.searchType, index) }; ZtwoSettings.editLocal { set(ZtwoSettings.searchUser, engine.engineUrl) }; ZtwoSettings.editLocal { set(ZtwoSettings.searchColor, engine.color) }; PrefZtwo.saveSearch(applicationContext); engineTick++
                                            },
                                        )
                                    }
                                }
                            }
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, contentDescription = null) }
                            else IconButton(onClick = { startVoice { voice.launch(it) } }) { Icon(painterResource(R.drawable.outline_mic_black_24), contentDescription = stringResource(R.string.voice_title), tint = MaterialTheme.colorScheme.onSurface) }
                        },
                    )
                    if (items.any { it.suggestKind == 37 }) {
                        IconButton(onClick = { clearAsk = true }) { Icon(Icons.Filled.Delete, contentDescription = null) }
                    }
                }
                LazyColumn(Modifier.weight(1f)) {
                    val pasted = clip
                    if (pasted != null && query.isEmpty()) {
                        item(key = "clip") {
                            AppListItem(
                                headlineContent = { Text(pasted, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                modifier = Modifier.clickable { if (URLUtil.isValidUrl(pasted)) open(pasted) else search(pasted) },
                            )
                        }
                    }
                    items(shown, key = { "${it.suggestKind}-${it.id}" }) { suggestion ->
                        AppListItem(
                            leadingContent = {
                                Icon(
                                    painterResource(when (suggestion.suggestKind) { 37 -> R.drawable.outline_search_black_24; 17 -> R.drawable.outline_kid_star_black_24; 32 -> R.drawable.outline_public_black_24; else -> R.drawable.outline_history_black_24 }),
                                    contentDescription = null, tint = MaterialTheme.colorScheme.onSurface,
                                )
                            },
                            headlineContent = { Text(suggestion.title?.takeIf { it.isNotEmpty() } ?: suggestion.address ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            supportingContent = suggestion.address?.let { address -> { Text(address, maxLines = 1, overflow = TextOverflow.Ellipsis) } },
                            modifier = Modifier.clickable { if (suggestion.suggestKind == 37) search(suggestion.title ?: "") else open(suggestion.address) },
                        )
                    }
                }
            }
        }

        if (clearAsk) {
            AlertDialog(
                onDismissRequest = { clearAsk = false },
                text = { Text(stringResource(R.string.recent_delete)) },
                confirmButton = {
                    TextButton(colors = androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = androidx.compose.material3.MaterialTheme.colorScheme.error), onClick = {
                        clearAsk = false
                        items = items.filter { it.suggestKind != 37 }
                        Thread { DbBookRecent.clearSecret(applicationContext, SyncSettings.secretMode.value) }.start()
                    }) { Text(stringResource(R.string.delete)) }
                },
                dismissButton = { TextButton(onClick = { clearAsk = false }) { Text(stringResource(R.string.cancel)) } },
            )
        }
    }
}
