package com.mycompany.app.ui.settings

import com.mycompany.app.ui.components.AppListItem
import android.app.Activity
import com.mycompany.app.pref.store.SecretSettings
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.ui.OpumComposeActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextOverflow
import com.mycompany.app.ui.components.Sym

/**
 * Settings start page: the settings pages in five colour-coded groups (2026-11-19) plus the settings search.
 * It also hands the results of its child pages back to the browser (preload / filter / status), as the old
 * `SettingMain` did.
 */
class MainSettingsActivity : OpumComposeActivity() {
    private var path: String? = null
    private var preload = false
    private var filter = false
    private var status = false

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        path = intent.getStringExtra(EXTRA_PATH)
        super.onCreate(savedInstanceState)
    }

    override fun finish() {
        if (preload || status) {
            val result = Intent()
            if (preload) {
                result.putExtra("EXTRA_PRELOAD", true)
                result.putExtra("EXTRA_FILTER", filter)
            }
            if (status) result.putExtra("EXTRA_STATUS", true)
            setResult(Activity.RESULT_OK, result)
        }
        super.finish()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        var searching by remember { mutableStateOf(false) }
        var query by remember { mutableStateOf("") }
        // What to start once the app lock has been passed: the page and the option to highlight.
        var pendingPage by remember { mutableStateOf<Pair<Int, Int>?>(null) }
        var pendingFromSearch by remember { mutableStateOf(false) }

        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val page = pendingPage
            pendingPage = null
            if (result.resultCode != Activity.RESULT_OK || page == null) return@rememberLauncherForActivityResult
            when (page.first) {
                // Lock screen passed: open the protected page itself.
                SettingsPage.SECURITY, SettingsPage.SECRET, SettingsPage.PASSWORD ->
                    openUnlocked(page.first, pendingFromSearch, page.second)
            }
        }
        val childLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                // SettingClean reports that filters changed; the other result-bearing pages only report "changed".
                if (data != null && data.hasExtra("EXTRA_FILTER")) {
                    preload = true
                    if (!filter) filter = data.getBooleanExtra("EXTRA_FILTER", true)
                } else if (data != null && lastChildWasClean) {
                    preload = true
                    filter = true
                } else {
                    status = true
                }
            }
        }
        this.childLauncher = childLauncher
        this.lockLauncher = launcher
        this.setPending = { page, fromSearch ->
            pendingPage = page
            pendingFromSearch = fromSearch
        }

        // the settings-search tip, from the second visit on (ui/tips)
        var tip by remember { mutableStateOf(com.mycompany.app.ui.tips.TipManager.onSettingsOpened(this)) }
        LaunchedEffect(tip) { tip?.let { com.mycompany.app.ui.tips.TipManager.shown(this@MainSettingsActivity, it) } }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        if (searching) {
                            val focus = remember { FocusRequester() }
                            LaunchedEffect(Unit) { focus.requestFocus() }
                            TextField(
                                value = query,
                                onValueChange = { query = it },
                                singleLine = true,
                                placeholder = { Text(stringResource(R.string.abc_searchview_description_search)) },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                ),
                                modifier = Modifier.fillMaxWidth().focusRequester(focus),
                            )
                        } else {
                            Text(stringResource(R.string.setting))
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { if (searching) { searching = false; query = "" } else finish() }) {
                            Icon(
                                Sym.ArrowBack,
                                contentDescription = stringResource(R.string.abc_action_bar_up_description),
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            if (searching && query.isNotEmpty()) query = "" else searching = !searching
                            if (!searching) query = ""
                        }) {
                            Icon(
                                if (searching) Sym.Close else Sym.Search,
                                contentDescription = stringResource(R.string.abc_searchview_description_search),
                            )
                        }
                    },
                )
            },
        ) { padding ->
            val side = com.mycompany.app.ui.components.readableSide()
            if (searching && query.isNotBlank()) {
                SearchResults(query = query, modifier = Modifier.padding(padding)) { page, option, title ->
                    openPage(page, fromSearch = true, option = option, focus = title)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(start = 16.dp + side, end = 16.dp + side, bottom = 24.dp),
                ) {
                    tip?.let { shownTip ->
                        item(key = "tip") {
                            com.mycompany.app.ui.tips.TipCard(shownTip, onDismiss = { tip = null }, modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
                        }
                    }
                    items(settingsPageGroups, key = { it.titleRes }) { group ->
                        PageGroup(group) { entry -> openPage(entry.id, fromSearch = false, option = 0) }
                    }
                }
            }
        }
    }

    /** One group of the start page: a small header and its rows on a rounded card, icons in the group's colour. */
    @Composable
    private fun PageGroup(group: SettingsPageGroup, onOpen: (SettingsPageEntry) -> Unit) {
        val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
        val tint = Color(if (dark) group.colorDark else group.color)
        Column {
            Text(
                text = stringResource(group.titleRes),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp, top = 20.dp, bottom = 8.dp),
            )
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                Column {
                    group.entries.forEachIndexed { index, entry ->
                        if (index > 0) HorizontalDivider(modifier = Modifier.padding(start = 72.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        PageRow(entry, tint) { onOpen(entry) }
                    }
                }
            }
        }
    }

    @Composable
    private fun PageRow(entry: SettingsPageEntry, tint: Color, onClick: () -> Unit) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(tint.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(painterResource(entry.iconRes), contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(stringResource(entry.titleRes), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                if (entry.summaryRes != 0) {
                    Text(
                        stringResource(entry.summaryRes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        // two lines: at large text sizes or in longer languages one line kept only the first item
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Icon(
                Sym.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp).size(20.dp),
            )
        }
    }

    @Composable
    private fun SearchResults(query: String, modifier: Modifier, onOpen: (page: Int, option: Int, title: String?) -> Unit) {
        val index = remember { SettingSearchIndex.build(this) }
        val needle = query.trim().lowercase()
        val hits = remember(needle) {
            index.orEmpty().filter { item ->
                item.titleLower.contains(needle) || (item.summaryLower?.contains(needle) == true)
            }
        }
        val side = com.mycompany.app.ui.components.readableSide()
        LazyColumn(modifier = modifier, contentPadding = PaddingValues(horizontal = side)) {
            items(hits) { item ->
                AppListItem(
                    modifier = Modifier.clickable { onOpen(item.itemId, item.optionId, item.titleText) },
                    headlineContent = { Text(item.titleText) },
                    supportingContent = item.summaryText?.takeIf { it.isNotEmpty() }?.let { { Text(it) } },
                )
            }
        }
    }

    private lateinit var childLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private lateinit var lockLauncher: androidx.activity.result.ActivityResultLauncher<Intent>
    private var setPending: (Pair<Int, Int>?, Boolean) -> Unit = { _, _ -> }
    private var lastChildWasClean = false

    /** Opens [page], going through the app lock first for the protected pages. */
    /** Title of the option the search opened a locked page for; handed on once the lock is passed. */
    private var pendingFocus: String? = null

    private fun openPage(page: Int, fromSearch: Boolean, option: Int, focus: String? = null) {
        pendingFocus = focus
        when (page) {
            SettingsPage.RATE -> MainUtil.openPlayStore(this, packageName)
            SettingsPage.SHARE -> MainUtil.shareText(
                this,
                "https://play.google.com/store/apps/details?id=$packageName",
                getString(R.string.app_name),
            )
            SettingsPage.FEEDBACK -> sendFeedback()
            SettingsPage.SECURITY -> {
                // only locked workspaces: the lock settings themselves ask (the whole-app lock already asked at start)
                val lockType = SecretSettings.lockType.value
                if (lockType == 0 || !SecretSettings.lockSecret.value) openUnlocked(page, fromSearch, option)
                else startLock(lockType, page, fromSearch, option, password = false)
            }
            SettingsPage.PASSWORD -> {
                val lockType = SecretSettings.guardType(1)
                if (lockType == 0) openUnlocked(page, fromSearch, option)
                else startLock(lockType, page, fromSearch, option, password = true)
            }
            else -> openUnlocked(page, fromSearch, option)
        }
    }

    private fun startLock(lockType: Int, page: Int, fromSearch: Boolean, option: Int, password: Boolean) {
        val lock = MainUtil.lockIntent(this, lockType)
        if (password) lock.putExtra("EXTRA_PASS", SecretSettings.guardPass(1))
        lock.putExtra("EXTRA_TYPE", 2)
        setPending(page to option, fromSearch)
        lockLauncher.launch(lock)
    }

    private fun openUnlocked(page: Int, fromSearch: Boolean, option: Int) {
        val intent = settingsPageIntent(this, page, fromSearch, option, path, pendingFocus) ?: return
        lastChildWasClean = page == SettingsPage.CLEAN
        when (page) {
            SettingsPage.CLEAN, SettingsPage.SECRET, SettingsPage.PRIVACY -> childLauncher.launch(intent)
            else -> startActivity(intent)
        }
    }

    private fun sendFeedback() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("app@opum.ch"))
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback))
            intent.putExtra(Intent.EXTRA_TEXT, MainUtil.feedbackText(this, path))
            startActivity(intent)
        } catch (unused: ActivityNotFoundException) {
            MainUtil.showToast(this, R.string.apps_none)
        } catch (unused: Exception) {
            MainUtil.showToast(this, R.string.apps_none)
        }
    }
}
