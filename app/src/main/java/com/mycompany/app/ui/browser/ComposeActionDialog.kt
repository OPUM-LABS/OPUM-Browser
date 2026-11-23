package com.mycompany.app.ui.browser

import android.app.Activity
import com.mycompany.app.ui.state.AppState
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp

/** One row of an action sheet: [id] is what the callback gets; the icon comes in a light-theme and a dark-theme drawable. */
class ActionItem(
    val id: Int,
    @DrawableRes val iconBlack: Int,
    @DrawableRes val iconDark: Int,
    @StringRes val labelRes: Int = 0,
    val label: String? = null,
)

/**
 * A bottom sheet with a list of icon + label actions and a Close button: the shape of many of the browser's old
 * `MainSelectAdapter` dialogs (open-as, tab menu, URL menu, file chooser source ...). [onPick] gets the row's id; the
 * subclass decides whether the sheet stays open (the old ones either dismissed in the listener or left it to the caller).
 * Close calls [dismiss] unless [closeCancels]. Shows itself when built.
 */
open class ComposeActionDialog(
    activity: Activity?,
    items: List<ActionItem>,
    private val onPick: (Int) -> Unit,
    private val closeCancels: Boolean = false,
) : ComposeBottomDialog(activity) {
    /** A row was picked; the default hands the id to the constructor's callback. */
    protected open fun onItem(id: Int) { onPick(id) }

    init {
        setSheet {
            val dark = AppState.darkUi
            androidx.compose.foundation.layout.Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                for (item in items) {
                    Row(
                        Modifier.fillMaxWidth().clickable { onItem(item.id) }.padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(painterResource(if (dark) item.iconDark else item.iconBlack), contentDescription = null, modifier = Modifier.size(24.dp))
                        Text(
                            item.label ?: stringResource(item.labelRes), style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp),
                        )
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { if (closeCancels) cancel() else dismiss() }) { Text(stringResource(R.string.close)) }
            }
        }
        show()
    }
}
