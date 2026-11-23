package com.mycompany.app.down

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf

/**
 * Small persistent download state for the UI (2026-11-20):
 * - why a download failed (a string resource, by download id) for the downloads list, which only said "Fail";
 * - how many downloads finished since the downloads list was last opened ([unseen]); the Downloads button (action 4)
 *   is filled with the app's main colour while it is above 0, wherever the button sits (top / bottom bar, menu,
 *   floating button). Opening the list or a finished file clears it.
 */
object DownState {
    private const val FILE = "down_state"
    private const val KEY_UNSEEN = "unseen"
    private const val PREFIX_FAIL = "fail_"

    /** Finished downloads not seen yet; observable (Compose redraws the button). */
    val unseen = mutableIntStateOf(-1)

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    @JvmStatic
    fun unseenCount(context: Context?): Int {
        if (unseen.intValue < 0 && context != null) unseen.intValue = prefs(context).getInt(KEY_UNSEEN, 0)
        return unseen.intValue.coerceAtLeast(0)
    }

    /** A download finished: one more unseen. */
    @JvmStatic
    fun markFinished(context: Context?) {
        context ?: return
        val n = unseenCount(context) + 1
        prefs(context).edit().putInt(KEY_UNSEEN, n).apply()
        unseen.intValue = n
        listeners.forEach { try { it.run() } catch (_: Exception) { } }
    }

    /** The downloads list (or a finished file) was opened. */
    @JvmStatic
    fun markSeen(context: Context?) {
        context ?: return
        if (unseenCount(context) == 0) return
        prefs(context).edit().putInt(KEY_UNSEEN, 0).apply()
        unseen.intValue = 0
        listeners.forEach { try { it.run() } catch (_: Exception) { } }
    }

    /** Views that are not Compose (menu grid, floating buttons) refresh through these. */
    private val listeners = java.util.concurrent.CopyOnWriteArrayList<Runnable>()

    @JvmStatic
    fun addListener(listener: Runnable) { listeners.addIfAbsent(listener) }

    @JvmStatic
    fun removeListener(listener: Runnable) { listeners.remove(listener) }

    @JvmStatic
    fun setFailReason(context: Context?, id: Long, resId: Int) {
        context ?: return
        val name = try { context.resources.getResourceEntryName(resId) } catch (_: Exception) { return }
        prefs(context).edit().putString(PREFIX_FAIL + id, name).apply()
    }

    @JvmStatic
    fun clearFailReason(context: Context?, id: Long) {
        context ?: return
        prefs(context).edit().remove(PREFIX_FAIL + id).apply()
    }

    /** The failure reason's string resource, or 0 when none was recorded. */
    @JvmStatic
    fun failReason(context: Context?, id: Long): Int {
        context ?: return 0
        val name = prefs(context).getString(PREFIX_FAIL + id, null) ?: return 0
        return context.resources.getIdentifier(name, "string", context.packageName)
    }

    /** The app's main colour (the Material 3 primary: the system accent; the app runs from Android 12). */
    @JvmStatic
    fun accent(context: Context, dark: Boolean): Int =
        context.getColor(if (dark) android.R.color.system_accent1_200 else android.R.color.system_accent1_600)

    /** Text / icon colour on [accent]. */
    @JvmStatic
    fun onAccent(context: Context, dark: Boolean): Int =
        context.getColor(if (dark) android.R.color.system_accent1_800 else android.R.color.system_accent1_0)
}
