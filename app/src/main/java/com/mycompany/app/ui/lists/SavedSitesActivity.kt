package com.mycompany.app.ui.lists

import com.mycompany.app.ui.components.AppListItem
import android.content.Context
import com.mycompany.app.pref.store.ListSettings
import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.mycompany.app.data.book.DataBookBlock
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookBlock
import com.mycompany.app.dialog.DialogDeleteBook
import com.mycompany.app.dialog.DialogDeleteBook.DeleteBookListener
import com.mycompany.app.list.book.BookListBuilder
import com.mycompany.app.list.book.ListBookBlock
import com.mycompany.app.dialog.DialogEditUrl
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefList
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingsTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The user's blocked images (`EXTRA_TYPE` 23: the image address and the page it was blocked on). Same table, sorting
 * and caches as the old list dialog (`DialogListBook`); delete uses the old delete dialog (which also refreshes the
 * browser's caches). The site permissions list (type 30) was replaced by Site settings (`SiteSettingsActivity`) on
 * 2026-11-20.
 */
class SavedSitesActivity : OpumComposeActivity() {

    companion object {
        const val EXTRA_TYPE = "EXTRA_TYPE"

        /** From a page dialog (blocked images): [onClosed] runs when the list closes. */
        fun open(context: Context, type: Int, onClosed: (() -> Unit)?) {
            ListReturn.set(onClosed)
            context.startActivity(Intent(context, SavedSitesActivity::class.java).putExtra(EXTRA_TYPE, type))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) ListReturn.fire()
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context: Context = this
        val type = 23
        var items by remember { mutableStateOf<List<ChildItem>>(emptyList()) }
        var loaded by remember { mutableStateOf(false) }
        var reload by remember { mutableIntStateOf(0) }
        var query by remember { mutableStateOf("") }
        var searching by remember { mutableStateOf(false) }

        LaunchedEffect(reload) {
            items = withContext(Dispatchers.IO) { loadBlocked(context) }
            loaded = true
        }
        val shown = if (query.isBlank()) items else items.filter {
            (it.name ?: "").lowercase().contains(query.trim().lowercase()) || (it.path ?: "").lowercase().contains(query.trim().lowercase())
        }

        fun delete(list: List<ChildItem>, all: Boolean) {
            var dialog: DialogDeleteBook? = null
            dialog = DialogDeleteBook(
                this, type, ArrayList<Any?>(list), null, all, true,
                object : DeleteBookListener {
                    override fun onCancelled() {}
                    override fun onDeleted() {
                        reload++
                        dialog?.dismiss()
                    }
                },
            )
        }

        Scaffold(
            topBar = {
                SettingsTopBar(
                    title = stringResource(R.string.blocked_image),
                    onBack = { finish() },
                    actions = {
                        IconButton(onClick = { searching = !searching; if (!searching) query = "" }) {
                            Icon(if (searching) Icons.Filled.Close else Icons.Filled.Search, contentDescription = null)
                        }
                        IconButton(onClick = { if (items.isNotEmpty()) delete(items, true) }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete_all))
                        }
                    },
                )
            },
            floatingActionButton = {
                if (type == 23) {
                    FloatingActionButton(onClick = {
                        // old list: + opened the address editor for a new blocked image (it saves the entry itself)
                        var editor: DialogEditUrl? = null
                        editor = DialogEditUrl(this, 23, ChildItem().also { it.id = -1L }, object : DialogEditUrl.EditUrlListener {
                            override fun onSaved(id: Long, path: String?, extra: String?) {
                                editor?.dismiss()
                                reload++
                            }
                        })
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add))
                    }
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
                Box(Modifier.fillMaxSize()) {
                    if (shown.isEmpty() && loaded) {
                        com.mycompany.app.ui.components.EmptyState(icon = R.drawable.outline_web_black_24, title = stringResource(R.string.down_empty_filter))
                    }
                    LazyColumn(contentPadding = PaddingValues(bottom = 16.dp + padding.calculateBottomPadding())) {
                        shown.forEach { item ->
                            item(key = "${item.id}-${item.path}-${item.name}") {
                                AppListItem(
                                    leadingContent = {
                                        val res = if (item.iconRes != 0) item.iconRes else R.drawable.outline_public_black_24
                                        Icon(painterResource(res), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(32.dp))
                                    },
                                    headlineContent = { Text(item.name ?: "", maxLines = 2, overflow = TextOverflow.Ellipsis) },
                                    supportingContent = {
                                        Text(item.path ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    },
                                    trailingContent = {
                                        IconButton(onClick = { delete(listOf(item), false) }) {
                                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.delete))
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadBlocked(context: Context): List<ChildItem> {
        val result = ArrayList<ChildItem>()
        val cursor = DbUtil.query(DbBookBlock.get(context).writableDatabase, "DbBookBlock_table", null, null, null, null) ?: return result
        try {
            val id = cursor.getColumnIndex("_id")
            val path = cursor.getColumnIndex("_path")
            val image = cursor.getColumnIndex("_image")
            val time = cursor.getColumnIndex("_time")
            while (cursor.moveToNext()) {
                val page = cursor.getString(path)
                if (page.isNullOrEmpty()) continue
                val item = BookListBuilder.siteItem(cursor.getLong(id), page, cursor.getLong(time))
                item.name = cursor.getString(image)
                item.iconRes = R.drawable.outline_image_black_24
                item.subText = MainUtil.pathOfUrl(page, false)
                ListBookBlock.applyFold(item)
                result.add(item)
            }
        } catch (_: Exception) {
        } finally {
            cursor.close()
        }
        return sort(result, ListSettings.bookBlockFold.value, ListSettings.bookBlockItem.value, ListSettings.bookBlockRvse.value)
    }

    private fun sort(list: ArrayList<ChildItem>, group: Int, key: Int, reverse: Boolean): List<ChildItem> {
        return try {
            val mutable = ArrayList<ChildItem?>(list)
            java.util.Collections.sort(mutable, MainUtil.childComparator(group, key, reverse))
            mutable.filterNotNull()
        } catch (_: Exception) {
            list
        }
    }
}
