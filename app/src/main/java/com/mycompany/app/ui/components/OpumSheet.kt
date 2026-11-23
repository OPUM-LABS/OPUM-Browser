package com.mycompany.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mycompany.app.ui.browser.SheetHandle
import com.mycompany.app.ui.browser.SheetTitle
import kotlinx.coroutines.launch

/** A modal sheet's state that opens fully (no half-open step): what `rememberModalBottomSheetState(skipPartiallyExpanded = true)` gave. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberFullSheetState(): SheetState =
    rememberBottomSheetState(initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded))

/** What sheet content can do: [close] slides the sheet down and then calls the sheet's `onDismiss`. */
class OpumSheetScope internal constructor(private val hide: () -> Unit) {
    fun close() = hide()
}

/**
 * OPUM's one sheet for Compose screens (2026-11-20), the twin of the browser's
 * `ComposeBottomDialog` sheets: same colour and corners, the drag handle, an optional [title] / [subtitle], the
 * [content] (scrolls when long, rises with the keyboard) and an optional action row at the bottom right. Replaces the
 * centred `AlertDialog`s for choices, settings values and forms; short destructive questions stay a centred
 * [ConfirmDialog].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpumSheet(
    onDismiss: () -> Unit,
    title: String? = null,
    subtitle: String? = null,
    actions: (@Composable OpumSheetScope.() -> Unit)? = null,
    content: @Composable OpumSheetScope.() -> Unit,
) {
    val state = rememberFullSheetState()
    val scope = rememberCoroutineScope()
    val dismiss by rememberUpdatedState(onDismiss)
    val sheetScope = remember { OpumSheetScope { scope.launch { state.hide() }.invokeOnCompletion { dismiss() } } }
    ModalBottomSheet(
        onDismissRequest = { dismiss() },
        sheetState = state,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { SheetHandle() },
    ) {
        Column(Modifier.imePadding()) {
            if (title != null) SheetTitle(title, subtitle)
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) { sheetScope.content() }
            if (actions != null) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                    sheetScope.actions()
                }
            }
        }
    }
}

/**
 * Drop-in for `AlertDialog(onDismissRequest, confirmButton, dismissButton, title, text)` that shows the same parts as an
 * [OpumSheet] (2026-11-20): [title] in the sheet's title style, [text] below it with the sheet's side padding, the
 * buttons at the bottom right. Lets the forms and choices of single screens move to the sheet by renaming the call.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpumSheetDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
) {
    // no outer scroll here (unlike OpumSheet): many of these texts scroll themselves (sort lists, editors), and a scroll
    // inside a scroll crashes. As in AlertDialog, the text gets the height that is left and scrolls on its own.
    val state = rememberFullSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = state,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { SheetHandle() },
    ) {
        Column(Modifier.imePadding()) {
            if (title != null) {
                Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 4.dp, bottom = 8.dp)) {
                    ProvideTextStyle(MaterialTheme.typography.titleLarge) { title() }
                }
            }
            if (text != null) {
                Box(Modifier.weight(1f, fill = false).fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp)) {
                    ProvideTextStyle(MaterialTheme.typography.bodyMedium) { text() }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                if (dismissButton != null) dismissButton()
                confirmButton()
            }
        }
    }
}

/** A sheet around free [content] (handle, colour, corners of [OpumSheet]), for pickers that draw their own title. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpumSheetBox(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val state = rememberFullSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { SheetHandle() },
    ) { Box(Modifier.imePadding()) { content() } }
}
