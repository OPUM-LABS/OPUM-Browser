package com.mycompany.app.ui.browser

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.Workspaces
import com.mycompany.app.ui.components.AppListItem
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.web.WorkspaceSwitch

/** The colours a workspace can have (0 = none), light enough for both themes. */
val WORKSPACE_COLORS = intArrayOf(
    0, 0xFF1E88E5.toInt(), 0xFF43A047.toInt(), 0xFFF4511E.toInt(), 0xFF8E24AA.toInt(),
    0xFFFDD835.toInt(), 0xFF00ACC1.toInt(), 0xFFD81B60.toInt(), 0xFF6D4C41.toInt(),
)

/** A workspace's colour as a dot (an outlined circle when it has none). */
@Composable
fun WorkspaceDot(color: Int, size: Dp = 20.dp) {
    val outline = MaterialTheme.colorScheme.outline
    Box(
        Modifier.size(size).clip(CircleShape)
            .then(if (color != 0) Modifier.background(Color(color)) else Modifier.border(2.dp, outline, CircleShape)),
    )
}

/** Name and colour of a new or existing workspace; [onSave] gets the trimmed name (never empty). */
@Composable
fun WorkspaceForm(initialName: String, initialColor: Int, onSave: (String, Int) -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    var color by remember { mutableIntStateOf(initialColor) }
    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = name, onValueChange = { name = it.take(40) }, singleLine = true,
            label = { Text(stringResource(R.string.space_name)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        )
        Text(
            stringResource(R.string.color_title), style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 8.dp),
        )
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (c in WORKSPACE_COLORS) {
                Box(
                    Modifier.size(32.dp).clip(CircleShape).clickable { color = c },
                    contentAlignment = Alignment.Center,
                ) {
                    WorkspaceDot(c, 26.dp)
                    if (c == color) {
                        Icon(
                            Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp),
                            tint = if (c == 0) MaterialTheme.colorScheme.onSurface else Color.White,
                        )
                    }
                }
            }
        }
        SheetButtons(
            onMain = { onSave(name.trim(), color) }, mainLabel = R.string.ok,
            onCancel = onCancel, mainEnabled = name.isNotBlank(),
        )
    }
}

/**
 * The Workspaces menu action (menu id 74, 2026-11-24): Main and the workspaces to open one, a new workspace (opened
 * right away) and the settings page to manage them.
 */
class WorkspaceSheet private constructor(private val wa: WebViewActivity) : ComposeBottomDialog(wa) {
    init {
        setSheet {
            var adding by remember { mutableStateOf(false) }
            val incognito = SyncSettings.secretMode.value
            val current = if (incognito) -1 else Workspaces.currentNumber()
            if (adding) {
                SheetTitle(stringResource(R.string.space_new))
                val base = stringResource(R.string.space_default_name)
                WorkspaceForm(
                    initialName = Workspaces.freeName(base, Workspaces.all()), initialColor = WORKSPACE_COLORS[1],
                    onSave = { name, color ->
                        val number = Workspaces.create(context, name, color)
                        dismiss()
                        WorkspaceSwitch.open(wa, number)
                    },
                    onCancel = { adding = false },
                )
                return@setSheet
            }
            SheetTitle(stringResource(R.string.workspaces), if (incognito) stringResource(R.string.space_from_incognito) else null)
            SpaceRow(stringResource(R.string.space_main), 0, locked = false, chosen = current == Workspaces.MAIN) {
                dismiss()
                WorkspaceSwitch.open(wa, Workspaces.MAIN)
            }
            for (space in Workspaces.all()) {
                SpaceRow(space.name, space.color, space.locked, chosen = current == space.number) {
                    dismiss()
                    WorkspaceSwitch.open(wa, space.number)
                }
            }
            ActionRow(stringResource(R.string.space_new), { Icon(Icons.Default.Add, null) }) { adding = true }
            ActionRow(stringResource(R.string.space_manage), { Icon(Icons.Default.Settings, null) }) {
                dismiss()
                wa.startActivity(Intent(wa, com.mycompany.app.ui.settings.WorkspaceSettingsActivity::class.java))
            }
            Spacer(Modifier.padding(bottom = 8.dp))
        }
        show()
    }

    @Composable
    private fun SpaceRow(name: String, color: Int, locked: Boolean, chosen: Boolean, onClick: () -> Unit) {
        AppListItem(
            headlineContent = { Text(name) },
            leadingContent = { WorkspaceDot(color) },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (locked) Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    if (chosen) Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp))
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        )
    }

    @Composable
    private fun ActionRow(title: String, icon: @Composable () -> Unit, onClick: () -> Unit) {
        AppListItem(
            headlineContent = { Text(title) },
            leadingContent = icon,
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        )
    }

    companion object {
        fun show(wa: WebViewActivity) {
            WorkspaceSheet(wa)
        }
    }
}
