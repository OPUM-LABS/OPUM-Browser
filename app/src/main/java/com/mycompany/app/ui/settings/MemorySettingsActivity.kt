package com.mycompany.app.ui.settings

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.store.MemoryPlan
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Performance & memory (reworked 2026-11-21). One choice of how the browser balances memory and speed
 * ([MemoryPlan]: automatic from the phone's memory, save memory, faster, or custom with the three limits), "Instant
 * back" (pages you leave stay ready), the sites never released, and smoother scrolling. The open-tab limit moved to
 * Tabs, web debugging to Web pages. When the phone runs low on memory, hidden pages are released first
 * (web/WebViewActivityMemory).
 */
class MemorySettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        var mode by remember { mutableIntStateOf(MemoryPlan.mode()) }
        val addPage = rememberPref({ ZtwoSettings.addPage.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.addPage, it) } }
        // the limits: changing one makes the mode custom
        @Composable
        fun limit(setting: com.mycompany.app.pref.store.Setting<Int>) = rememberPref({ setting.value }) { value ->
            ZtwoSettings.edit(context) { set(setting, value); set(ZtwoSettings.memoryMode, MemoryPlan.CUSTOM) }
            mode = MemoryPlan.CUSTOM
        }
        val pageMem = limit(ZtwoSettings.pageMemory)
        val tabMem = limit(ZtwoSettings.tabMemory)
        val keepDays = limit(ZtwoSettings.dayMemory)
        var choosing by remember { mutableStateOf(false) }
        var resetting by remember { mutableStateOf(false) }
        var version by remember { mutableIntStateOf(0) } // bumped by the reset for the shared rows

        fun reloadLimits() {
            pageMem.reload(ZtwoSettings.pageMemory.value)
            tabMem.reload(ZtwoSettings.tabMemory.value)
            keepDays.reload(ZtwoSettings.dayMemory.value)
        }

        fun choose(newMode: Int) {
            if (newMode == MemoryPlan.CUSTOM) {
                // the custom limits start from the ones in use
                ZtwoSettings.edit(context) {
                    set(ZtwoSettings.memoryMode, newMode)
                    set(ZtwoSettings.pageMemory, ZtwoSettings.pageMemory.value)
                    set(ZtwoSettings.tabMemory, ZtwoSettings.tabMemory.value)
                    set(ZtwoSettings.dayMemory, ZtwoSettings.dayMemory.value)
                }
            } else {
                ZtwoSettings.edit(context) { set(ZtwoSettings.memoryMode, newMode) }
                MemoryPlan.applyLimits()
            }
            mode = newMode
            reloadLimits()
        }

        SettingsScaffold(
            title = stringResource(R.string.set_performance),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.hd_memory))
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.mem_mode),
                    value = modeLabel(context, mode),
                    onClick = { choosing = true },
                )
            }
            if (mode == MemoryPlan.CUSTOM) {
                if (addPage.value) limitRows(R.string.mem_pages, pageMem, 3..30, 5)
                limitRows(R.string.mem_tabs, tabMem, 3..30, 5)
                limitRows(R.string.mem_release_days, keepDays, 1..30, 2, minOn = 1) { days(context, it) }
            }
            settingsFooter(limitsText(context, addPage.value) + "\n" + getString(R.string.mem_low_note))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.instant_back),
                    summary = stringResource(R.string.instant_back_sum),
                    checked = addPage.value,
                    onCheckedChange = addPage::set,
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.mem_keep_sites),
                    summary = stringResource(R.string.mem_keep_sites_sum),
                    onClick = { com.mycompany.app.ui.lists.SiteListActivity.open(context, 28) },
                )
            }
            settingsHeader(getString(R.string.hd_web_engine))
            performanceSection(version)
        }

        if (choosing) {
            ChoiceDialog(
                title = stringResource(R.string.mem_mode),
                options = MODES.map { modeLabel(context, it) },
                selectedIndex = MODES.indexOf(mode),
                onSelect = { choose(MODES[it]) },
                onDismiss = { choosing = false },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    resetPerformance(context)
                    mode = MemoryPlan.mode()
                    addPage.reload(ZtwoSettings.addPage.value)
                    reloadLimits()
                    version++
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        private val MODES = listOf(MemoryPlan.AUTO, MemoryPlan.SAVE, MemoryPlan.FAST, MemoryPlan.CUSTOM)

        /** A mode's name and what it does, e.g. "Automatic – set for this phone's 8 GB". */
        fun modeLabel(context: Context, mode: Int): String = when (mode) {
            MemoryPlan.AUTO -> context.getString(R.string.mem_mode_auto) + " – " +
                context.getString(R.string.mem_mode_auto_sum, gb(MemoryPlan.deviceMb))
            MemoryPlan.SAVE -> context.getString(R.string.mem_mode_save) + " – " + context.getString(R.string.mem_mode_save_sum)
            MemoryPlan.FAST -> context.getString(R.string.mem_mode_fast) + " – " + context.getString(R.string.mem_mode_fast_sum)
            else -> context.getString(R.string.mem_mode_custom) + " – " + context.getString(R.string.mem_mode_custom_sum)
        }

        /** The phone's memory rounded to whole GB as Android reports a little less ("8 GB"). */
        private fun gb(mb: Long): String = if (mb <= 0) "?" else "${Math.round(mb / 1024.0).coerceAtLeast(1)} GB"

        /** The limits in use, as a sentence ("unlimited" for a limit that is off). */
        fun limitsText(context: Context, instantBack: Boolean): String {
            fun n(v: Int, min: Int = 3) = if (v < min) context.getString(R.string.mem_unlimited) else v.toString()
            val pages = if (instantBack) n(ZtwoSettings.pageMemory.value) else "1"
            return context.getString(R.string.mem_now, pages, n(ZtwoSettings.tabMemory.value)) +
                (if (ZtwoSettings.dayMemory.value >= 1) " " + context.getString(R.string.mem_now_days, days(context, ZtwoSettings.dayMemory.value)) else "") +
                (if (MemoryPlan.prerender()) " " + context.getString(R.string.mem_prerender) else "")
        }

        private fun days(context: Context, n: Int): String = context.resources.getQuantityString(R.plurals.mem_days, n, n)

        /** Back to automatic, with "Instant back" on. */
        fun resetToDefaults(context: Context) {
            ZtwoSettings.edit(context) { set(ZtwoSettings.memoryMode, MemoryPlan.AUTO) }
            if (!ZtwoSettings.addPage.value) ZtwoSettings.reset(context, listOf(ZtwoSettings.addPage))
            MemoryPlan.applyLimits()
        }

    }
}
