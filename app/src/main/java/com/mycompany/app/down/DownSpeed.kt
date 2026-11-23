package com.mycompany.app.down

import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import com.mycompany.app.pref.store.ZoneSettings

/**
 * How many connections a split ("fast") download uses (2026-11-20). The setting (`ZoneSettings.multiDown`) is 0 for
 * Auto (the default) or a fixed 1–[MAX] (1 = never split). Auto: 4 on Wi-Fi / unmetered networks, 2 on mobile data,
 * with Data Saver on or on low-RAM phones. Downloading is limited by the network, the server and the storage, not
 * by the CPU, so the number of cores is not used; many servers slow down or block more than 4–8 connections from
 * one device. Files under 8 MB use one connection anyway (`MainDownSvc.partSize`: at least 4 MB per part), and so
 * do servers that do not answer ranges (`MainUtil.supportsRanges`).
 */
object DownSpeed {
    const val MAX = 8

    @JvmStatic
    fun connections(context: Context?): Int {
        val set = ZoneSettings.multiDown.value
        return when (val n = normalized(set)) {
            0 -> autoConnections(context)
            else -> n
        }
    }

    /** The stored value as the setting shows it: the old default 10 is Auto, other old values above [MAX] are [MAX]. */
    @JvmStatic
    fun normalized(set: Int): Int = when {
        set == 10 -> 0
        set > MAX -> MAX
        set < 0 -> 0
        else -> set
    }

    @JvmStatic
    fun autoConnections(context: Context?): Int {
        val ctx = context ?: return 2
        return try {
            val cm = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val metered = cm?.isActiveNetworkMetered ?: true
            val saver = cm?.restrictBackgroundStatus == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED
            val lowRam = (ctx.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.isLowRamDevice == true
            if (metered || saver || lowRam) 2 else 4
        } catch (_: Exception) {
            2
        }
    }
}
