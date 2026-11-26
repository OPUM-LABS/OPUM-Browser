package com.mycompany.app.ui.lists

import com.mycompany.app.ui.components.AppListItem
import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.list.ListTask
import com.mycompany.app.list.ListTask.ListTaskConfig
import com.mycompany.app.list.ListTask.ListTaskSimpleListener
import com.mycompany.app.list.ListTaskAlbum
import com.mycompany.app.list.ListTaskCmp
import com.mycompany.app.list.ListTaskPdf
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingsTopBar
import com.mycompany.app.ui.components.Sym

/**
 * "Jump to another book" list of the image/PDF/ZIP viewer: the same albums as [AlbumListActivity] (`EXTRA_TYPE` 1, 2
 * or 3; 12 behaves like 1) with the one being viewed (`EXTRA_PATH`) marked and scrolled into view. Choosing another
 * returns it as `EXTRA_PATH` + `EXTRA_INDEX` (RESULT_OK); choosing the current one just closes. Read only: no
 * selection, delete or scan here. Replaces `MainListImage`.
 */
class BookPickerActivity : OpumComposeActivity() {

    private var type = 1
    private var currentPath: String? = null
    private var items by mutableStateOf<List<ChildItem>>(emptyList())
    private var loading by mutableStateOf(true)
    private var task: ListTask? = null

    private val listener = object : ListTaskSimpleListener() {
        override fun onDone() { runOnUiThread { loading = true } }
        override fun onStart() { runOnUiThread { loading = false } }
        override fun onConfig(listTaskConfig: ListTaskConfig?) {
            val list = listTaskConfig?.items?.filterNotNull() ?: emptyList()
            runOnUiThread { items = list; loading = false }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        type = intent.getIntExtra("EXTRA_TYPE", 1)
        currentPath = intent.getStringExtra("EXTRA_PATH")
        super.onCreate(savedInstanceState)
        val context = applicationContext
        task = when (type) {
            2 -> ListTaskPdf(context, listener)
            3 -> ListTaskCmp(context, listener)
            else -> ListTaskAlbum(context, listener)
        }
        task?.loadFolder(currentPath, false, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        task?.cancel()
        task = null
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        var query by remember { mutableStateOf("") }
        var searching by remember { mutableStateOf(false) }
        val listState = rememberLazyListState()
        val shown = if (query.isBlank()) items else items.filter { it.name?.lowercase()?.contains(query.trim().lowercase()) == true }

        LaunchedEffect(items) {
            val target = items.indexOfFirst { it.path == currentPath }
            if (target >= 0 && query.isBlank()) listState.scrollToItem(target)
        }

        fun choose(item: ChildItem) {
            if (!MainUri.isFile(applicationContext, item.path)) {
                MainUtil.showToast(this, R.string.invalid_path)
                return
            }
            if (type != 12 && item.path == currentPath) {
                finish()
                return
            }
            setResult(RESULT_OK, Intent().putExtra("EXTRA_PATH", item.path).putExtra("EXTRA_INDEX", item.position))
            finish()
        }

        Scaffold(
            topBar = {
                SettingsTopBar(
                    title = stringResource(when (type) { 2 -> R.string.pdf; 3 -> R.string.zip; else -> R.string.album }),
                    onBack = { finish() },
                    actions = {
                        IconButton(onClick = { searching = !searching; if (!searching) query = "" }) {
                            Icon(if (searching) Sym.Close else Sym.Search, contentDescription = null)
                        }
                    },
                )
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
                Box(Modifier.fillMaxSize()) {
                    if (shown.isEmpty()) {
                        if (loading) CircularProgressIndicator(Modifier.align(Alignment.Center))
                        else com.mycompany.app.ui.components.EmptyState(icon = R.drawable.outline_folder_black_24, title = stringResource(R.string.down_empty_filter))
                    } else {
                        LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 16.dp + padding.calculateBottomPadding())) {
                            items(shown.size, key = { shown[it].path ?: it.toString() }) { index ->
                                val item = shown[index]
                                val current = item.path == currentPath
                                AppListItem(
                                    colors = ListItemDefaults.colors(
                                        containerColor = if (current) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    ),
                                    leadingContent = {
                                        val res = if (item.iconRes != 0) item.iconRes else R.drawable.outline_public_black_24
                                        Icon(painterResource(res), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(32.dp))
                                    },
                                    headlineContent = { Text(item.name ?: "", maxLines = 2, overflow = TextOverflow.Ellipsis) },
                                    supportingContent = {
                                        val date = if (item.time > 0) MainUtil.formatDate(item.time, null, "yyyy.MM.dd") else null
                                        val size = if (item.size > 0) MainUtil.sizeLabel(item.size) else null
                                        Text(listOfNotNull(date, size).joinToString("   "))
                                    },
                                    modifier = Modifier.clickable { choose(item) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
