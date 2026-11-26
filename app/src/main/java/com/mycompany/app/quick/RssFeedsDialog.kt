package com.mycompany.app.quick

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.ui.components.Sym

/**
 * The RSS reader's feed list (the gear on the RSS page): the feeds with a delete button each, and a field to add a feed
 * address. [onChanged] runs after every change (the page reloads its articles). Shows itself.
 */
class RssFeedsDialog(context: Context?, private val onChanged: () -> Unit) : ComposeBottomDialog(context) {
    init {
        setSheet {
            var feeds by remember { mutableStateOf(RssFeeds.feeds()) }
            var input by remember { mutableStateOf("") }
            var error by remember { mutableIntStateOf(0) }
            var checking by remember { mutableStateOf(false) }
            val failed = remember { RssFeeds.failed.toSet() }
            val scope = androidx.compose.runtime.rememberCoroutineScope()
            fun add() {
                if (checking) return
                if (RssFeeds.normalize(input) == null) {
                    error = R.string.rss_bad_address
                    return
                }
                // a website works too: the feed it announces is looked up (in the background)
                checking = true
                error = 0
                val typed = input
                scope.launch {
                    val feed = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) { RssFeeds.resolve(context, typed) }
                    checking = false
                    if (feed == null) {
                        error = R.string.rss_no_feed
                        return@launch
                    }
                    if (RssFeeds.add(context, feed)) onChanged()
                    feeds = RssFeeds.feeds()
                    input = ""
                }
            }
            Text(
                stringResource(R.string.rss_feeds),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 8.dp),
            )
            if (feeds.isEmpty()) {
                Text(
                    stringResource(R.string.rss_none),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                )
            }
            androidx.compose.foundation.layout.Column(Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                for (feed in feeds) {
                    Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.layout.Column(Modifier.weight(1f)) {
                            Text(RssFeeds.titleOf(feed), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge)
                            Text(feed.substringAfter("://"), maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (feed in failed) {
                                Text(stringResource(R.string.rss_failed), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                        IconButton(onClick = {
                            RssFeeds.remove(context, feed)
                            feeds = RssFeeds.feeds()
                            onChanged()
                        }) {
                            Icon(Sym.Delete, stringResource(R.string.delete))
                        }
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it; error = 0 },
                    singleLine = true,
                    isError = error != 0,
                    enabled = !checking,
                    label = { Text(stringResource(R.string.rss_address)) },
                    supportingText = {
                        Text(stringResource(when {
                            checking -> R.string.rss_checking
                            error != 0 -> error
                            else -> R.string.rss_address_info
                        }))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { add() }),
                    modifier = Modifier.weight(1f),
                )
                Button(modifier = Modifier.padding(top = 8.dp), onClick = { add() }, enabled = input.isNotBlank() && !checking) { Text(stringResource(R.string.add)) }
            }
        }
        show()
    }
}
