package com.mycompany.app.dialog

import android.content.Context
import com.mycompany.app.pref.store.ZtriSettings

/** Sites whose logins are never offered for saving ("Never for this site" in the save prompt, 2026-11-28). */
object PassNever {
    fun all(): List<String> = ZtriSettings.passNever.value.orEmpty().lines().map { it.trim() }.filter { it.isNotEmpty() }

    fun contains(host: String?): Boolean = !host.isNullOrEmpty() && host.lowercase() in all()

    fun add(context: Context?, host: String?) {
        if (host.isNullOrEmpty() || contains(host)) return
        write(context, all() + host.lowercase())
    }

    fun remove(context: Context?, host: String) = write(context, all() - host)

    private fun write(context: Context?, hosts: List<String>) {
        ZtriSettings.edit(context) { set(ZtriSettings.passNever, hosts.sorted().joinToString("\n")) }
    }
}
