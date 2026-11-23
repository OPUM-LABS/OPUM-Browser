package com.mycompany.app.quick

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AbstractComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * One recent search in the search panel (`QuickAdapter` in search mode), drawn by Compose: history icon, the query
 * (up to two lines), a delete button and an arrow that puts the query into the search field. The row is the item
 * view, so its tap / long press and pressed background stay View listeners; the two buttons call [onDelete] /
 * [onFill] with this row (whose tag is the holder, as the old buttons' was).
 */
class QuickSearchRow(context: Context) : QuickRowView(context) {
    var text by mutableStateOf("")
    var textColor by mutableIntStateOf(-16777216)
    var historyIcon by mutableIntStateOf(0)
    var deleteIcon by mutableIntStateOf(0)
    var fillIcon by mutableIntStateOf(0)
    /** Pressed colour of the two buttons. */
    var pressColor by mutableIntStateOf(-2039584)
    var onDelete: (QuickSearchRow) -> Unit = {}
    var onFill: (QuickSearchRow) -> Unit = {}

    @Composable
    override fun Content() {
        Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.padding(start = 12.dp).size(24.dp)) {
                if (historyIcon != 0) Image(painterResource(historyIcon), null, Modifier.size(24.dp))
            }
            Text(
                text,
                style = TextStyle(color = Color(textColor), fontSize = 16.sp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
            )
            RowButton(deleteIcon) { onDelete(this@QuickSearchRow) }
            RowButton(fillIcon) { onFill(this@QuickSearchRow) }
        }
    }

    @Composable
    private fun RowButton(icon: Int, onClick: () -> Unit) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.width(48.dp),
            colors = IconButtonDefaults.iconButtonColors(contentColor = Color(pressColor)),
        ) {
            if (icon != 0) Image(painterResource(icon), null)
        }
    }
}
