package com.mycompany.app.ui.browser

import com.mycompany.app.ui.components.AppListItem
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip

/**
 * A sheet with one tappable row per label (the old `MyDialogBottom` + `SettingListAdapter` pickers built inline, e.g.
 * the browser's capture mode "Normal mode" / "Scroll mode"). [onPick] gets the row index; the caller closes the sheet
 * (the old pickers did that in their listener). Shows itself.
 */
class ChoiceSheetDialog(context: Context?, labels: List<Int>, onPick: (Int) -> Unit) : ComposeBottomDialog(context) {
    init {
        setSheet {
            labels.forEachIndexed { index, label ->
                AppListItem(
                    headlineContent = { Text(stringResource(label)) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth().clickable { onPick(index) },
                )
            }
            androidx.compose.foundation.layout.Spacer(Modifier.padding(bottom = 8.dp))
        }
        show()
    }
}

/**
 * A sheet that holds an existing view (the old view-hosting bottom sheets, e.g. the reader's translate
 * bar). [dim] false leaves the page behind undimmed, as the old host did by clearing the window dim flag. [heightPx] > 0
 * gives the view that height (for views that fill their host, like the translate bar); 0 lets it wrap. Shows itself.
 */
class ViewSheetDialog(context: Context?, view: View, private val dim: Boolean, heightPx: Int = 0) : ComposeBottomDialog(context) {
    override val scrimAlpha: Float get() = if (dim) 0.32f else 0f

    init {
        (view.parent as? ViewGroup)?.removeView(view)
        setSheet {
            val density = androidx.compose.ui.platform.LocalDensity.current
            val sized = if (heightPx > 0) Modifier.height(with(density) { heightPx.toDp() }) else Modifier
            AndroidView(factory = { view }, modifier = Modifier.fillMaxWidth().then(sized).padding(bottom = 8.dp))
        }
        show()
    }
}

/**
 * The members of a family of actions ([MenuCatalog.Family], 2026-11-27): its name and its actions as tiles, like the
 * menu's; a tap runs the action. Actions that need a web page are dimmed on the start page. Shows itself.
 */
class ActionFamilySheet(context: Context?, family: MenuCatalog.Family, onStartPage: Boolean, onPick: (Int) -> Unit) : ComposeBottomDialog(context) {
    init {
        setSheet {
            Text(
                stringResource(family.label), style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 12.dp),
            )
            androidx.compose.foundation.layout.FlowRow(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                maxItemsInEachRow = 4,
            ) {
                for (id in family.members) {
                    val unavailable = onStartPage && MenuCatalog.isPageOnly(id)
                    androidx.compose.foundation.layout.Column(
                        Modifier.weight(1f).clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                            .clickable(enabled = !unavailable) { dismiss(); onPick(id) }
                            .padding(vertical = 10.dp, horizontal = 4.dp)
                            .alpha(if (unavailable) 0.38f else 1f),
                        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    ) {
                        MenuActionIcon(id, 28.dp, live = true)
                        Text(
                            stringResource(MenuCatalog.labelRes(id)), style = MaterialTheme.typography.labelMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center, maxLines = 2,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
                // an incomplete last row keeps the tiles their size
                repeat((4 - family.members.size % 4) % 4) { androidx.compose.foundation.layout.Spacer(Modifier.weight(1f)) }
            }
            androidx.compose.foundation.layout.Spacer(Modifier.padding(bottom = 12.dp))
        }
        show()
    }
}
