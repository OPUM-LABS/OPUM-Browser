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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.Workspaces
import com.mycompany.app.ui.components.AppListItem
import com.mycompany.app.ui.components.ColorWheelPicker
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.web.WorkspaceSwitch
import com.mycompany.app.ui.components.Sym

/** The ready colours a workspace can have (0 = none), light enough for both themes; any other comes from the wheel. */
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

/** The rainbow of the "any colour" swatch. */
private val WHEEL_BRUSH = Brush.sweepGradient(
    listOf(Color.Red, Color.Magenta, Color.Blue, Color.Cyan, Color.Green, Color.Yellow, Color.Red),
)

/**
 * Name and colour of a new or existing workspace; [onSave] gets the trimmed name (never empty). The first swatch
 * (2026-11-25) opens the colour wheel for any colour; it shows that colour once one is picked.
 */
@Composable
fun WorkspaceForm(initialName: String, initialColor: Int, onSave: (String, Int) -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    var color by remember { mutableIntStateOf(initialColor) }
    val ready = WORKSPACE_COLORS.filter { it != 0 }
    // a colour of its own: the wheel's swatch shows it
    val own = color != 0 && color !in ready
    var wheel by remember { mutableStateOf(false) }
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
            val wheelLabel = stringResource(R.string.theme_color_wheel)
            Box(
                Modifier.size(32.dp).clip(CircleShape).clickable { wheel = !wheel }
                    .semantics { contentDescription = wheelLabel },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier.size(26.dp).clip(CircleShape)
                        .then(if (own) Modifier.background(Color(color)) else Modifier.background(WHEEL_BRUSH)),
                )
                if (own) Icon(Sym.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
            }
            for (c in ready) {
                Box(
                    Modifier.size(32.dp).clip(CircleShape).clickable { color = c; wheel = false },
                    contentAlignment = Alignment.Center,
                ) {
                    WorkspaceDot(c, 26.dp)
                    if (c == color) {
                        Icon(Sym.Check, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                    }
                }
            }
        }
        if (wheel) {
            ColorWheelPicker(
                color = if (color != 0) color else ready.first(),
                onChange = { color = it or (0xFF shl 24) },
                onCommit = { color = it or (0xFF shl 24) },
                wheelDescription = stringResource(R.string.theme_color_wheel),
                brightnessLabel = stringResource(R.string.brightness),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
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
class WorkspaceSheet private constructor(private val wa: WebViewActivity, startAdding: Boolean) : ComposeBottomDialog(wa) {
    init {
        setSheet {
            var adding by remember { mutableStateOf(startAdding) }
            val incognito = SyncSettings.secretMode.value
            val current = if (incognito) -1 else Workspaces.currentNumber()
            if (adding) {
                SheetTitle(stringResource(R.string.space_new))
                val base = stringResource(R.string.space_default_name)
                // the colour wheel makes the form taller than some screens
                Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                    WorkspaceForm(
                        initialName = Workspaces.freeName(base, Workspaces.all()), initialColor = WORKSPACE_COLORS[1],
                        onSave = { name, color ->
                            val number = Workspaces.create(context, name, color)
                            dismiss()
                            WorkspaceSwitch.open(wa, number)
                        },
                        onCancel = { adding = false },
                    )
                }
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
            ActionRow(stringResource(R.string.space_new), { Icon(Sym.Add, null) }) { adding = true }
            ActionRow(stringResource(R.string.space_manage), { Icon(Sym.Settings, null) }) {
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
                    if (locked) Icon(Sym.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    if (chosen) Icon(Sym.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp))
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
        /** [adding]: open straight at the form for a new workspace (the tab overview's "New workspace"). */
        fun show(wa: WebViewActivity, adding: Boolean = false) {
            WorkspaceSheet(wa, adding)
        }
    }
}
