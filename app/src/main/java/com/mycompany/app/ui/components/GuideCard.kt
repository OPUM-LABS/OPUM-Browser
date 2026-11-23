package com.mycompany.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.theme.OpumDimens

/**
 * The one guide / hint card (2026-11-20): dark translucent rounded card (it sits over
 * pages, pictures and video, so it is dark in both themes), white [icon] and [text], and a close button in the corner
 * when [onClose] is set. Used by the reader's pinch guide and the full-screen video guide.
 */
@Composable
fun GuideCard(text: String, modifier: Modifier = Modifier, icon: Int? = null, iconSize: Dp = 48.dp, onClose: (() -> Unit)? = null) {
    Box(modifier.background(Color(0xD9000000), RoundedCornerShape(OpumDimens.popupCorner))) {
        Column(
            Modifier.widthIn(max = 320.dp).padding(horizontal = 24.dp, vertical = 20.dp).padding(top = if (onClose != null) 16.dp else 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (icon != null) Icon(painterResource(icon), null, tint = Color.White, modifier = Modifier.size(iconSize).padding(bottom = 8.dp))
            Text(text, color = Color.White, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
        }
        if (onClose != null) {
            Box(
                Modifier.align(Alignment.TopEnd).padding(4.dp).size(40.dp)
                    .clickable(remember { MutableInteractionSource() }, ripple(bounded = false, radius = 20.dp, color = Color.White)) { onClose() }
                    .background(Color(0x33FFFFFF), CircleShape),
                contentAlignment = Alignment.Center,
            ) { Icon(painterResource(R.drawable.outline_close_white_24), null, tint = Color.White, modifier = Modifier.size(20.dp)) }
        }
    }
}
