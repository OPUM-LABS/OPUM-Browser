package com.mycompany.app.ui.tips

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.SyncSettings

/**
 * One tip: a short title and sentence, optionally with an action button ([action] its label).
 * [ambient] tips come on their own while browsing (at most one per app start); the others answer something the user
 * just did (the first edge swipe) and may come any time.
 */
enum class Tip(val id: String, val title: Int, val text: Int, val action: Int = 0, val ambient: Boolean = true) {
    EDGE_SWIPE("edge", R.string.tip_edge_title, R.string.tip_edge_text, R.string.setting, ambient = false),
    HOLD_BUTTONS("hold", R.string.tip_hold_title, R.string.tip_hold_text),
    TABS("tabs", R.string.tip_tabs_title, R.string.tip_tabs_text),
    CUSTOMIZE("layout", R.string.tip_layout_title, R.string.tip_layout_text, R.string.tip_open),
    INCOGNITO("incognito", R.string.tip_incognito_title, R.string.tip_incognito_text),
    SETTINGS_SEARCH("search", R.string.tip_search_title, R.string.tip_search_text, ambient = false),
    /** The translate icon came up in the address bar for the first time (2026-11-26; a red dot on it before). */
    TRANSLATE("translate", R.string.tip_translate_title, R.string.tip_translate_text, ambient = false),
    /** After the menu was first closed: hold a button to change it (2026-11-26; an "Edit menu" button sat in its top row). */
    MENU("menu", R.string.tip_menu_title, R.string.tip_menu_text, ambient = false),
}

/**
 * When tips show (2026-11-20, replaces the green one-time guides): each tip once, never during the setup wizard,
 * ambient tips at most one per app start and only after a few pages, spread over the first sessions:
 * [Tip.HOLD_BUTTONS] from the first start, [Tip.TABS] once two tabs are open, [Tip.CUSTOMIZE] from the second start,
 * [Tip.INCOGNITO] from the third. Everything can be turned off (Settings > General > Show tips) and shown again.
 */
object TipManager {
    /** For tests: forget this app start's state (a new process). */
    internal fun newProcessForTest() {
        sessionCounted = false
        shownThisSession = false
        pagesThisSession = 0
        lastPage = null
        eventPending = false
    }

    /** Pages finished before an ambient tip may come in a session. */
    private const val PAGES_BEFORE_TIP = 3

    private var sessionCounted = false
    private var shownThisSession = false
    private var pagesThisSession = 0
    private var lastPage: String? = null
    /** An event tip is waiting to be shown: no ambient tip meanwhile. */
    private var eventPending = false

    private fun seenList(): List<String> = (ReadSettings.tipsSeen.value ?: "").split(',')

    fun enabled(): Boolean = ReadSettings.tipsOn.value && !SyncSettings.splash.value

    fun seen(tip: Tip): Boolean = seenList().contains(tip.id)

    private fun seenMarker(id: String): Boolean = seenList().contains(id)

    private fun mark(context: Context?, id: String) {
        val list = seenList().filter { it.isNotEmpty() }
        if (id in list) return
        ReadSettings.edit(context) { set(ReadSettings.tipsSeen, (list + id).joinToString(",")) }
    }

    fun markSeen(context: Context?, tip: Tip) = mark(context, tip.id)

    /** All tips come again (Settings > General > Show tips again). */
    fun reset(context: Context?) {
        ReadSettings.edit(context) { set(ReadSettings.tipsSeen, ""); set(ReadSettings.tipsOn, true) }
        shownThisSession = false
    }

    /** Counts browser starts (once per app process). */
    fun sessionStarted(context: Context?) {
        if (sessionCounted) return
        sessionCounted = true
        ReadSettings.edit(context) { set(ReadSettings.tipSessions, ReadSettings.tipSessions.value + 1) }
    }

    /** A tip that answers an action, or null when it was shown already or tips are off. */
    fun forEvent(tip: Tip): Tip? {
        if (!enabled() || seen(tip)) return null
        eventPending = true
        return tip
    }

    /** [tip] is on screen: it does not come again, and an ambient one ends this start's ambient tips. */
    fun shown(context: Context?, tip: Tip) {
        if (tip.ambient) shownThisSession = true else eventPending = false
        markSeen(context, tip)
    }

    /**
     * A page finished loading ([url], [tabCount] tabs open): the ambient tip that fits now, or null (call [shown]
     * once it is on screen).
     */
    fun onPageLoaded(url: String?, tabCount: Int): Tip? {
        if (url.isNullOrEmpty() || url == lastPage) return null
        lastPage = url
        pagesThisSession++
        if (!enabled() || shownThisSession || eventPending || pagesThisSession < PAGES_BEFORE_TIP) return null
        val session = ReadSettings.tipSessions.value
        val tip = when {
            !seen(Tip.TABS) && tabCount >= 2 -> Tip.TABS
            !seen(Tip.HOLD_BUTTONS) -> Tip.HOLD_BUTTONS
            !seen(Tip.CUSTOMIZE) && session >= 2 -> Tip.CUSTOMIZE
            !seen(Tip.INCOGNITO) && session >= 3 -> Tip.INCOGNITO
            else -> null
        }
        return tip
    }

    /** Settings opened: from the second visit on, the settings-search tip (once). */
    fun onSettingsOpened(context: Context?): Tip? {
        if (!enabled() || seen(Tip.SETTINGS_SEARCH)) return null
        if (!seenMarker("settings_visit")) {
            mark(context, "settings_visit")
            return null
        }
        markSeen(context, Tip.SETTINGS_SEARCH)
        return Tip.SETTINGS_SEARCH
    }
}

/**
 * The tip card: Material 3 "rich tooltip" look in the theme's inverse colours (dark on light themes, light on dark
 * ones, tinted by the colour theme), a bulb, title, text, an optional action and "Got it".
 */
@Composable
fun TipCard(tip: Tip, onDismiss: () -> Unit, onAction: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
        shadowElevation = 6.dp,
    ) {
        Column(Modifier.padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 4.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(painterResource(R.drawable.outline_lightbulb_24), null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.inversePrimary)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(stringResource(tip.title), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(tip.text), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (tip.action != 0 && onAction != null) {
                    TextButton(onClick = { onAction(); onDismiss() }) {
                        Text(stringResource(tip.action), color = MaterialTheme.colorScheme.inversePrimary)
                    }
                }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.tip_got_it), color = MaterialTheme.colorScheme.inversePrimary) }
            }
        }
    }
}
