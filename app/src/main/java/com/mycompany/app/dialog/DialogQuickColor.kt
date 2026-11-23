package com.mycompany.app.dialog

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mycompany.app.main.MainConst
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Pick one of the quick-access icon colours (`MainConst.iconColors`); [selectedColor] is the one currently in use (shown with a check). A pick
 * goes to the listener and closes the sheet. Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
class DialogQuickColor(activity: Activity?, selectedColor: Int, quickColorListener: QuickColorListener?) : ComposeBottomDialog(activity) {
    private var listener: QuickColorListener? = quickColorListener

    interface QuickColorListener {
        fun onColor(color: Int)
    }

    init {
        setSheet @OptIn(ExperimentalLayoutApi::class) {
            FlowRow(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                for (argb in MainConst.iconColors) {
                    val selected = argb == selectedColor
                    Box(
                        Modifier.size(48.dp).clip(CircleShape).background(Color(argb))
                            .border(BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f)), CircleShape)
                            .clickable { listener?.onColor(argb); dismiss() },
                        contentAlignment = Alignment.Center,
                    ) { if (selected) Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White) }
                }
            }
        }
    }

    init { show() }

    override fun dismiss() {
        listener = null
        super.dismiss()
    }

    companion object {
    }
}
