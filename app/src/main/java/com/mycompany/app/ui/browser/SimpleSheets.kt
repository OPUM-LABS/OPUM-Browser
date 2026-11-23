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
