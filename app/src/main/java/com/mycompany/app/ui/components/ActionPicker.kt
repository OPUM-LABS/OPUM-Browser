package com.mycompany.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainUtil

/**
 * Picker for one of the browser actions (the ones that can be bound to gestures, buttons and menus). Replaces the old
 * `DialogSetItem`. Action ids are 3..73 (68 does not exist); names and icons come from `MainUtil.menuIconAt` / `MainUtil.menuIconDarkAt`.
 * [usedIds] are actions that are already assigned elsewhere: they stay selectable but are shown dimmed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionPickerSheet(
    selectedId: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
    usedIds: Set<Int> = emptySet(),
) {
    val iconStyle = remember { MainUtil.barTint(0, false) }
    // Actions that were removed from the app keep their slot but are labelled "Not used": do not offer them.
    val all = (3..73).filter { it != 68 && MainUtil.menuIconAt(it) != R.string.not_used }.map { id ->
        ActionItem(id, stringResource(MainUtil.menuIconAt(id)), MainUtil.menuIconDarkAt(id, iconStyle))
    }.sortedBy { it.name.lowercase() }
    var query by remember { mutableStateOf("") }
    val items = remember(query, all) {
        val needle = query.trim().lowercase()
        if (needle.isEmpty()) all else all.filter { it.name.lowercase().contains(needle) }
    }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberFullSheetState()) {
        Column {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                placeholder = { Text(stringResource(R.string.abc_searchview_description_search)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyColumn {
                items(items, key = { it.id }) { item ->
                    AppListItem(
                        modifier = Modifier
                            .clickable { onSelect(item.id); onDismiss() }
                            .alpha(if (item.id in usedIds) 0.5f else 1f),
                        headlineContent = { Text(item.name) },
                        leadingContent = {
                            if (item.iconRes != 0) Icon(painterResource(item.iconRes), contentDescription = null)
                        },
                        trailingContent = { if (item.id == selectedId) Checkbox(checked = true, onCheckedChange = null) },
                    )
                }
            }
        }
    }
}

private class ActionItem(val id: Int, val name: String, val iconRes: Int)
