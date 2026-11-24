package com.mycompany.app.pref.store

import java.io.File

/**
 * The memory modes of Settings > Performance & memory (2026-11-21). Instead of three counters, the user picks how the
 * browser should balance memory and speed; the counters ([ZtwoSettings.pageMemory], [ZtwoSettings.tabMemory],
 * [ZtwoSettings.dayMemory]) are then set from the mode, in memory only (the stored values stay those of the custom
 * mode). [AUTO] picks them from the phone's memory. Every reader of the counters works as before.
 */
object MemoryPlan {
    const val AUTO = 0
    const val SAVE = 1
    const val FAST = 2
    const val CUSTOM = 3

    /** Pages kept in memory per tab (back without reloading), tabs kept in memory, days before an unseen page is released. */
    data class Limits(val pages: Int, val tabs: Int, val days: Int)

    /** The phone's memory in MB (from /proc/meminfo; 0 when unknown), read once. */
    val deviceMb: Long by lazy {
        try {
            File("/proc/meminfo").useLines { lines ->
                lines.firstOrNull { it.startsWith("MemTotal:") }?.split(Regex("\\s+"))?.getOrNull(1)?.toLongOrNull()?.div(1024) ?: 0L
            }
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * The limits of [mode] on a phone with [deviceMb] MB of memory (what Android reports, a little under the nominal
     * size: a "4 GB" phone reports about 3.6 GB), or null for [CUSTOM].
     */
    fun limitsFor(mode: Int, deviceMb: Long): Limits? = when (mode) {
        SAVE -> Limits(3, 3, 1)
        FAST -> Limits(10, 10, 7)
        AUTO -> when {
            deviceMb <= 0L -> Limits(4, 4, 2)
            deviceMb < 3500 -> Limits(3, 3, 1)
            deviceMb < 5500 -> Limits(4, 4, 2)
            deviceMb < 9000 -> Limits(5, 6, 2)
            else -> Limits(8, 8, 3)
        }
        else -> null
    }

    /**
     * The mode for settings that never chose one: the old defaults (back without reload on, 5 pages, 5 tabs, 2 days)
     * become [AUTO]; anything the user changed stays as it is ([CUSTOM]).
     */
    fun initialMode(addPage: Boolean, pages: Int, tabs: Int, days: Int): Int =
        if (addPage && pages == 5 && tabs == 5 && days == 2) AUTO else CUSTOM

    /**
     * Whether pages a site marks as likely next (its speculation rules) may be prepared in advance: costs memory and
     * data, so only in [FAST], and in [AUTO] on phones with about 6 GB or more; never while saving data ([saveData]).
     */
    fun prerenderFor(mode: Int, deviceMb: Long, saveData: Boolean): Boolean = !saveData && when (mode) {
        FAST -> true
        AUTO -> deviceMb >= 5500
        else -> false
    }

    /** [prerenderFor] with the current settings. */
    fun prerender(): Boolean = prerenderFor(mode(), deviceMb, PdfSettings.saveData.value)

    /**
     * Whether the page renderer may be let go before the app when the browser is out of sight and the phone needs
     * memory (WebView renderer priority "waived when not visible"): Android then ends the renderer first and keeps
     * the app, and the browser rebuilds its pages in place when it is back (web/RendererRecovery), a warm start
     * instead of a cold one. Not in [FAST] (tabs stay ready), nor in [AUTO] on phones with about 6 GB or more (they
     * rarely run short), and never while background play is on ([backPlay]: the sound comes from the renderer).
     */
    fun yieldRendererFor(mode: Int, deviceMb: Long, backPlay: Boolean): Boolean = !backPlay && when (mode) {
        FAST -> false
        AUTO -> deviceMb in 1 until 5500
        else -> true
    }

    /** [yieldRendererFor] with the current settings. */
    fun yieldRenderer(): Boolean = yieldRendererFor(mode(), deviceMb, ZoneSettings.backPlay.value)

    /** The mode in use ([ZtwoSettings.memoryMode]; [applyLimits] fills in the first one). */
    fun mode(): Int = ZtwoSettings.memoryMode.value.takeIf { it in AUTO..CUSTOM } ?: CUSTOM

    /**
     * Sets the counters for the current mode (after loading the settings and when the mode changes). Settings that
     * never chose a mode get [initialMode], in memory only, decided from the stored counters before they change.
     */
    fun applyLimits() {
        if (ZtwoSettings.memoryMode.value !in AUTO..CUSTOM) {
            val first = initialMode(ZtwoSettings.addPage.value, ZtwoSettings.pageMemory.value, ZtwoSettings.tabMemory.value, ZtwoSettings.dayMemory.value)
            ZtwoSettings.editLocal { set(ZtwoSettings.memoryMode, first) }
        }
        val limits = limitsFor(mode(), deviceMb) ?: return
        ZtwoSettings.editLocal {
            set(ZtwoSettings.pageMemory, limits.pages)
            set(ZtwoSettings.tabMemory, limits.tabs)
            set(ZtwoSettings.dayMemory, limits.days)
        }
    }
}
