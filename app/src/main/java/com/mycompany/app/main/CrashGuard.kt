package com.mycompany.app.main

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock

/**
 * Breaks crash loops. The browser reopens the last page on start, so a page that crashes the app (as `x.com` did
 * through a runaway helper script) would crash it again on every start and lock the user out. CrashGuard counts
 * crashes that happen within [EARLY_MS] of a start; after [LIMIT] in a row the next start is a *safe start*: the
 * first restored tab opens the start page instead of its page ([takeSafeStart]). Twenty seconds without a crash
 * clear the count. The count lives in its own small preference file, apart from the app's settings.
 */
object CrashGuard {
    private const val FILE = "crash_guard"
    private const val KEY_COUNT = "early_crashes"
    private const val EARLY_MS = 20_000L
    private const val LIMIT = 2

    private var startedAt = 0L
    @Volatile
    private var safeStart = false

    /** Called once from `MainApp.onCreate`: decides on a safe start and watches for early crashes. */
    @SuppressLint("ApplySharedPref") // commit on purpose: the crash handler runs right before the process dies
    fun install(context: Context) {
        val app = context.applicationContext
        startedAt = SystemClock.elapsedRealtime()
        val prefs = app.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val count = prefs.getInt(KEY_COUNT, 0)
        if (count >= LIMIT) {
            safeStart = true
            prefs.edit().putInt(KEY_COUNT, 0).commit() // synchronous on purpose: see below
        }
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            try {
                if (SystemClock.elapsedRealtime() - startedAt < EARLY_MS) {
                    val now = prefs.getInt(KEY_COUNT, 0)
                    // commit, not apply: the process dies right after this handler, so the count must be on disk now
                    prefs.edit().putInt(KEY_COUNT, now + 1).commit()
                }
            } catch (_: Throwable) {
            }
            previous?.uncaughtException(thread, error)
        }
        // a start that survives the early window is a good start
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                if (prefs.getInt(KEY_COUNT, 0) != 0) prefs.edit().putInt(KEY_COUNT, 0).apply()
            } catch (_: Throwable) {
            }
        }, EARLY_MS)
    }

    /** True once, on a safe start: the caller shows the start page instead of restoring the crashing page. */
    fun takeSafeStart(): Boolean {
        if (!safeStart) return false
        safeStart = false
        return true
    }
}
