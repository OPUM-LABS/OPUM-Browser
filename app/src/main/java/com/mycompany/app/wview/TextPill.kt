package com.mycompany.app.wview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp

/** The look of the one-line pills ([WebMoveText], [WebAreaText]): list background, 14 sp text, up to 2 lines. */
@Composable
internal fun TextPill(label: String, dark: Boolean, onClick: () -> Unit) {
    val pad = with(LocalDensity.current) { MainApp.dp8.toDp() }
    Box(
        Modifier.fillMaxSize()
            .background(if (dark) colorResource(R.color.dark_bg) else Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = pad),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label, color = if (dark) Color(0xFFFAFAFA) else Color.Black, fontSize = 14.sp, maxLines = 2,
            overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center,
        )
    }
}
