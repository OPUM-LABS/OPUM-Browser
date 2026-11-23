package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.lists.SiteListActivity
import com.mycompany.app.ui.prefs.OnScreenResume

/**
 * Site settings, like Chrome's (2026-11-20, owner's request; replaces the "Site permissions" list):
 *  - start page: "All sites", the permission categories (location, camera, microphone, protected content) with their
 *    default, and JavaScript / pop-ups / ads, which open their existing exception lists (their global switches stay in
 *    Privacy and Web Filter, so nothing is set in two places),
 *  - a category: "Ask before allowing" (off = no site can use it, `BrowserSettings.sitePermBlock`) and the sites
 *    allowed / blocked,
 *  - all sites: every site with a saved decision, a list entry or stored data,
 *  - one site ([EXTRA_SITE] opens it directly, from the lock sheet and the permission prompt): its permissions
 *    (ask / allowed / blocked), JavaScript, ads and pop-ups, its stored data and "Clear and reset".
 * Data: [SiteData].
 */
class SiteSettingsActivity : OpumComposeActivity() {
    companion object {
        const val EXTRA_SITE = "EXTRA_SITE"

        /** Opens the page of [host] (or the start page when it is empty). */
        fun open(context: Context, host: String?) {
            val intent = Intent(context, SiteSettingsActivity::class.java)
            if (!host.isNullOrEmpty()) intent.putExtra(EXTRA_SITE, host.lowercase())
            context.startActivity(intent)
        }
    }

    /** What is shown: [screen] 0 start, 1 category [kind], 2 all sites, 3 one [site]. */
    private var screen by mutableIntStateOf(0)
    private var kind by mutableIntStateOf(0)
    private var site by mutableStateOf<String?>(null)
    /** Opened for one site: Back closes the page. */
    private var direct = false

    private var perms by mutableStateOf<Map<String, SiteData.Perms>>(emptyMap())
    private var lists by mutableStateOf<Map<Int, Map<String, List<com.mycompany.app.ui.lists.SiteEntry>>>>(emptyMap())
    private var storage by mutableStateOf<Map<String, Long>>(emptyMap())
    private var defaults by mutableIntStateOf(BrowserSettings.sitePermBlock.value)

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        val host = intent.getStringExtra(EXTRA_SITE)
        if (!host.isNullOrEmpty()) {
            site = host
            screen = 3
            direct = true
        }
        super.onCreate(savedInstanceState)
    }

    private fun reload() {
        val ctx = applicationContext
        Thread {
            val p = SiteData.permissions(ctx)
            val l = mapOf(
                SiteData.ADS to SiteData.listHosts(ctx, SiteData.ADS),
                SiteData.POPS to SiteData.listHosts(ctx, SiteData.POPS),
                SiteData.JS to SiteData.listHosts(ctx, SiteData.JS),
            )
            runOnUiThread {
                perms = p
                lists = l
            }
        }.start()
        SiteData.storage { storage = it }
    }

    private fun background(block: (Context) -> Unit) {
        val ctx = applicationContext
        Thread {
            block(ctx)
            runOnUiThread { reload() }
        }.start()
    }

    private fun back() {
        when {
            screen == 0 || (direct && screen == 3) -> finish()
            screen == 3 && kind != 0 -> screen = 1
            screen == 3 && site != null && cameFromAll -> screen = 2
            else -> screen = 0
        }
    }

    private var cameFromAll = false

    @Composable
    override fun Content() {
        OnScreenResume { reload() }
        BackHandler(enabled = !(screen == 0 || (direct && screen == 3))) { back() }
        when (screen) {
            1 -> Category()
            2 -> AllSites()
            3 -> Site()
            else -> Start()
        }
    }

    private fun permState(host: String, bit: Int): Int {
        val p = perms[host] ?: return 0
        return if ((p.allow and bit) != 0) 1 else if ((p.block and bit) != 0) 2 else 0
    }

    @Composable
    private fun Start() {
        val ctx = this
        SettingsScaffold(title = stringResource(R.string.pms_site_settings), onBack = { finish() }) {
            item {
                SettingRow(title = stringResource(R.string.ss_all_sites), onClick = { kind = 0; screen = 2 })
            }
            settingsHeader(getString(R.string.site_info_permissions))
            for ((bit, label) in SiteData.permissionKinds) {
                item {
                    SettingRow(
                        title = stringResource(label),
                        summary = stringResource(if ((defaults and bit) != 0) R.string.ss_blocked else R.string.ss_ask),
                        onClick = { kind = bit; screen = 1 },
                    )
                }
            }
            settingsHeader(getString(R.string.ss_hd_content))
            item {
                SettingRow(
                    title = "JavaScript",
                    summary = stringResource(if (BrowserSettings.enableJs.value) R.string.ss_allowed else R.string.ss_blocked),
                    onClick = { SiteListActivity.open(ctx, SiteData.JS) },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.hd_popups),
                    summary = stringResource(CleanSettingsActivity.popLabels[BrowserSettings.popBlock.value.coerceIn(0, CleanSettingsActivity.popLabels.size - 1)]),
                    onClick = { SiteListActivity.open(ctx, SiteData.POPS) },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.hd_ads),
                    summary = stringResource(if (BrowserSettings.adsBlock.value) R.string.ss_blocked else R.string.ss_allowed),
                    onClick = { SiteListActivity.open(ctx, SiteData.ADS) },
                )
            }
            settingsFooter(getString(R.string.ss_content_info))
        }
    }

    @Composable
    private fun Category() {
        val bit = kind
        val label = SiteData.permissionKinds.firstOrNull { it.first == bit }?.second ?: R.string.site_info_permissions
        val allowed = perms.filter { (it.value.allow and bit) != 0 }.keys.sorted()
        val blocked = perms.filter { (it.value.block and bit) != 0 }.keys.sorted()
        SettingsScaffold(title = stringResource(label), onBack = { back() }) {
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.ss_ask_switch),
                    checked = (defaults and bit) == 0,
                    onCheckedChange = { on ->
                        defaults = if (on) defaults and bit.inv() else defaults or bit
                        val value = defaults
                        BrowserSettings.edit(this@SiteSettingsActivity) { set(BrowserSettings.sitePermBlock, value) }
                    },
                )
            }
            settingsFooter(getString(R.string.ss_ask_off_info))
            if (allowed.isEmpty() && blocked.isEmpty()) {
                settingsFooter(getString(R.string.ss_no_sites))
            }
            if (allowed.isNotEmpty()) {
                settingsHeader(getString(R.string.ss_allowed))
                for (host in allowed) item(key = "a$host") { SiteRow(host, bit) }
            }
            if (blocked.isNotEmpty()) {
                settingsHeader(getString(R.string.ss_blocked))
                for (host in blocked) item(key = "b$host") { SiteRow(host, bit) }
            }
        }
    }

    /** A site in a category: opens the site, the cross forgets the decision. */
    @Composable
    private fun SiteRow(host: String, bit: Int) {
        SettingRow(
            title = host,
            onClick = { site = host; cameFromAll = false; screen = 3 },
            trailing = {
                IconButton(onClick = { background { SiteData.setPermission(it, host, bit, 0) } }) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.delete))
                }
            },
        )
    }

    @Composable
    private fun AllSites() {
        val hosts = (perms.keys + (lists.values.flatMap { it.keys }) + storage.keys).filter { it.isNotEmpty() }.toSortedSet()
        SettingsScaffold(title = stringResource(R.string.ss_all_sites), onBack = { back() }) {
            if (hosts.isEmpty()) settingsFooter(getString(R.string.ss_all_empty))
            for (host in hosts) {
                item(key = host) {
                    SettingRow(
                        title = host,
                        summary = siteSummary(host),
                        onClick = { site = host; kind = 0; cameFromAll = true; screen = 3 },
                    )
                }
            }
        }
    }

    /** One line about a site: allowed / blocked permissions, list entries, stored data. */
    @Composable
    private fun siteSummary(host: String): String? {
        val parts = ArrayList<String>()
        for ((bit, label) in SiteData.permissionKinds) {
            when (permState(host, bit)) {
                1 -> parts.add(stringResource(label) + ": " + stringResource(R.string.ss_allowed))
                2 -> parts.add(stringResource(label) + ": " + stringResource(R.string.ss_blocked))
            }
        }
        if (lists[SiteData.ADS]?.containsKey(host) == true) parts.add(stringResource(R.string.hd_ads) + ": " + stringResource(R.string.ss_allowed))
        if (lists[SiteData.POPS]?.containsKey(host) == true) parts.add(stringResource(R.string.hd_popups) + ": " + stringResource(R.string.ss_allowed))
        if (lists[SiteData.JS]?.containsKey(host) == true) parts.add("JavaScript: " + stringResource(R.string.ss_blocked))
        storage[host]?.let { if (it > 0) parts.add(stringResource(R.string.ss_stored, android.text.format.Formatter.formatShortFileSize(this, it))) }
        return parts.joinToString(" · ").ifEmpty { null }
    }

    @Composable
    private fun Site() {
        val host = site ?: return
        var confirm by remember { mutableStateOf(false) }
        SettingsScaffold(title = host, onBack = { back() }) {
            settingsHeader(getString(R.string.site_info_permissions))
            for ((bit, label) in SiteData.permissionKinds) {
                item(key = "p$bit") {
                    SettingSegmentedRow(
                        title = stringResource(label),
                        options = listOf(stringResource(R.string.ss_ask), stringResource(R.string.ss_allowed), stringResource(R.string.ss_blocked)),
                        selectedIndex = permState(host, bit),
                        onSelect = { state -> background { SiteData.setPermission(it, host, bit, state) } },
                    )
                }
            }
            settingsHeader(getString(R.string.ss_hd_content))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.ss_allow_js),
                    checked = lists[SiteData.JS]?.containsKey(host) != true,
                    onCheckedChange = { on -> background { SiteData.setListed(it, SiteData.JS, host, !on) } },
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.ss_block_ads),
                    checked = lists[SiteData.ADS]?.containsKey(host) != true,
                    onCheckedChange = { on -> background { SiteData.setListed(it, SiteData.ADS, host, !on) } },
                )
            }
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.ss_block_pops),
                    checked = lists[SiteData.POPS]?.containsKey(host) != true,
                    onCheckedChange = { on -> background { SiteData.setListed(it, SiteData.POPS, host, !on) } },
                )
            }
            settingsHeader(getString(R.string.storage))
            item {
                val bytes = storage[host] ?: 0L
                SettingRow(
                    title = stringResource(R.string.ss_clear_reset),
                    summary = if (bytes > 0) stringResource(R.string.ss_stored, android.text.format.Formatter.formatShortFileSize(this@SiteSettingsActivity, bytes)) else null,
                    onClick = { confirm = true },
                )
            }
        }
        if (confirm) {
            AlertDialog(
                onDismissRequest = { confirm = false },
                title = { Text(stringResource(R.string.ss_clear_reset)) },
                text = { Text(stringResource(R.string.ss_clear_reset_info, host)) },
                confirmButton = {
                    TextButton(onClick = {
                        confirm = false
                        SiteData.clearData(host) // WebStorage / cookies: main thread
                        background { SiteData.clearAndReset(it, host) }
                        com.mycompany.app.main.MainUtil.showToast(this@SiteSettingsActivity, R.string.site_info_cleared)
                    }) { Text(stringResource(R.string.site_info_clear_ok)) }
                },
                dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.cancel)) } },
            )
        }
    }
}
