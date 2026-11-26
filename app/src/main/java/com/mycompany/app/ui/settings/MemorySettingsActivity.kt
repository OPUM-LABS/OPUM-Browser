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
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.rememberPref

/**
 * Performance & memory (reworked 2026-11-21, tidied 2026-11-26). Four buttons for how the browser balances memory and
 * speed ([MemoryPlan]: automatic from the phone's memory, save memory, faster, or custom), the three limits as one
 * slider each (greyed out unless custom), "Instant back" and the sites never released, and smoother scrolling. The open-tab limit moved to
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
        val preRaster = rememberPref({ ZoneSettings.preRaster.value }) { ZoneSettings.edit(context) { set(ZoneSettings.preRaster, it) } }
        var resetting by remember { mutableStateOf(false) }

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

        val custom = mode == MemoryPlan.CUSTOM
        val onlyCustom = stringResource(R.string.only_custom, stringResource(R.string.user_defined))

        SettingsScaffold(
            title = stringResource(R.string.set_performance),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            settingsHeader(getString(R.string.hd_memory))
            item {
                SettingSegmentedRow(
                    title = stringResource(R.string.mem_mode),
                    options = MODES.map { stringResource(modeName(it)) },
                    selectedIndex = MODES.indexOf(mode),
                    summary = modeSummary(context, mode),
                    onSelect = { choose(MODES[it]) },
                )
            }
            settingsFooter(limitsText(context, addPage.value))
            settingsHeader(getString(R.string.hd_mem_limits))
            limitSlider(R.string.mem_pages, pageMem, 30, 3, R.string.history_none, custom && addPage.value,
                if (!custom) onlyCustom else getString(R.string.instant_back_only, getString(R.string.instant_back)),
            ) { "$it" }
            limitSlider(R.string.mem_tabs, tabMem, 30, 3, R.string.history_none, custom, onlyCustom) { "$it" }
            limitSlider(R.string.mem_release_days, keepDays, 30, 1, R.string.never, custom, onlyCustom) { days(context, it) }
            settingsFooter(getString(R.string.mem_low_note))
            settingsHeader(getString(R.string.hd_keep_ready))
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
            settingsHeader(getString(R.string.hd_scrolling))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.pre_raster),
                    summary = stringResource(R.string.smooth_scroll_sum),
                    checked = preRaster.value,
                    onCheckedChange = preRaster::set,
                )
            }
        }

        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    mode = MemoryPlan.mode()
                    addPage.reload(ZtwoSettings.addPage.value)
                    reloadLimits()
                    preRaster.reload(ZoneSettings.preRaster.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        private val MODES = listOf(MemoryPlan.AUTO, MemoryPlan.SAVE, MemoryPlan.FAST, MemoryPlan.CUSTOM)

        private fun modeName(mode: Int): Int = when (mode) {
            MemoryPlan.AUTO -> R.string.mem_mode_auto
            MemoryPlan.SAVE -> R.string.mem_mode_save
            MemoryPlan.FAST -> R.string.mem_mode_fast
            else -> R.string.mem_mode_custom
        }

        /** What the chosen mode does, as a sentence under the buttons ("Set for this phone's 8 GB"). */
        private fun modeSummary(context: Context, mode: Int): String = when (mode) {
            MemoryPlan.AUTO -> context.getString(R.string.mem_mode_auto_sum, gb(MemoryPlan.deviceMb))
            MemoryPlan.SAVE -> context.getString(R.string.mem_mode_save_sum)
            MemoryPlan.FAST -> context.getString(R.string.mem_mode_fast_sum)
            else -> context.getString(R.string.mem_mode_custom_sum)
        }.replaceFirstChar { it.titlecase(java.util.Locale.getDefault()) }

        /** The phone's memory rounded to whole GB as Android reports a little less ("8 GB"). */
        private fun gb(mb: Long): String = if (mb <= 0) "?" else "${Math.round(mb / 1024.0).coerceAtLeast(1)} GB"

        /** The limits in use, as a sentence ("unlimited" for a limit that is off). */
        fun limitsText(context: Context, instantBack: Boolean): String {
            fun n(v: Int, min: Int = 3) = if (v < min) context.getString(R.string.mem_unlimited) else v.toString()
            val pages = if (instantBack) n(ZtwoSettings.pageMemory.value) else "1"
            return context.getString(R.string.mem_now, pages, n(ZtwoSettings.tabMemory.value)) +
                (if (ZtwoSettings.dayMemory.value >= 1) " " + context.getString(R.string.mem_now_days, days(context, ZtwoSettings.dayMemory.value)) else "") +
                (if (MemoryPlan.prerender()) " " + context.getString(R.string.mem_prerender) else "") +
                (if (MemoryPlan.yieldRenderer()) " " + context.getString(R.string.mem_yield_renderer) else "")
        }

        private fun days(context: Context, n: Int): String = context.resources.getQuantityString(R.plurals.mem_days, n, n)

        /** Back to automatic, with "Instant back" on and smoother scrolling off. */
        fun resetToDefaults(context: Context) {
            ZtwoSettings.edit(context) { set(ZtwoSettings.memoryMode, MemoryPlan.AUTO) }
            if (!ZtwoSettings.addPage.value) ZtwoSettings.reset(context, listOf(ZtwoSettings.addPage))
            MemoryPlan.applyLimits()
            if (ZoneSettings.preRaster.value) ZoneSettings.reset(context, listOf(ZoneSettings.preRaster))
        }

    }
}
