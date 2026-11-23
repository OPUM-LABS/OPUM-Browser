package com.mycompany.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.em
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * The one empty-list look (2026-11-20): a large muted [icon], a [title] and an optional [hint]
 * below it, then optional [action] content (a button), centred in the free space. With [hintIcon], each "★" in the hint
 * is drawn as that icon (2026-11-22: the bookmarks hint shows the real "add bookmark" icon; translations keep the ★).
 */
@Composable
fun EmptyState(icon: Int, title: String, hint: String? = null, modifier: Modifier = Modifier, hintIcon: Int? = null, action: (@Composable () -> Unit)? = null) {
    Column(
        modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(painterResource(icon), null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp).padding(bottom = 8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        if (!hint.isNullOrEmpty()) {
            val parts = hint.split('★')
            val text = buildAnnotatedString {
                parts.forEachIndexed { index, part ->
                    append(part)
                    if (index < parts.lastIndex) { if (hintIcon != null) appendInlineContent("icon", "★") else append('★') }
                }
            }
            val inline = if (hintIcon == null) emptyMap() else mapOf(
                "icon" to InlineTextContent(Placeholder(1.4.em, 1.4.em, PlaceholderVerticalAlign.TextCenter)) {
                    Icon(painterResource(hintIcon), null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxSize())
                },
            )
            Text(
                text, inlineContent = inline, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp),
            )
        }
        if (action != null) Column(Modifier.padding(top = 16.dp)) { action() }
    }
}
