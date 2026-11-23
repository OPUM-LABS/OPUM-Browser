package com.mycompany.app.web

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup

/**
 * Debug-only trace of the "add tab" chain in [WebViewActivity] (`onNewIntent` → `addTabImpl` → posted steps 93…100), to catch
 * the rare state where the previous tab's `WebNestFrame` stays visible on top of the new one. Logs with tag [TAG] only in debuggable builds;
 * after a tab was added it watches the frame container and logs a `STALE FRAME` warning when, once the hand-over should
 * long be done, the new frame is still hidden or more than one frame is visible.
 * Remove once the cause is found.
 */
object TabFrameLog {
    const val TAG = "OPUM_TABS"
    private var debuggable: Boolean? = null
    /** Bumped by every add, so a delayed check only reports for the newest add. */
    private var generation = 0

    private fun on(context: Context?): Boolean {
        val known = debuggable
        if (known != null) return known
        val value = context != null && (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        debuggable = value
        return value
    }

    private fun id(view: View?): String = if (view == null) "null" else Integer.toHexString(System.identityHashCode(view))

    private fun vis(view: View): String = when (view.visibility) {
        View.VISIBLE -> "V"
        View.INVISIBLE -> "I"
        else -> "G"
    }

    /** The page frames next to [current] (its parent's `WebNestFrame` children) as "id:visibility", current marked with *. */
    private fun frames(current: View?): String {
        val parent = current?.parent as? ViewGroup ?: return "(no container)"
        val out = StringBuilder()
        for (index in 0 until parent.childCount) {
            val child = parent.getChildAt(index)
            if (child !is WebNestFrame) continue
            if (out.isNotEmpty()) out.append(' ')
            out.append(id(child)).append(':').append(vis(child))
            if (child === current) out.append('*')
        }
        return out.toString()
    }

    @JvmStatic
    fun step(context: Context?, name: String, current: View?, extra: String = "") {
        if (!on(context)) return
        try {
            Log.d(TAG, "$name current=${id(current)} frames=[${frames(current)}] $extra")
        } catch (_: Exception) {
        }
    }

    /**
     * At the end of the chain: look at the frames again until the hand-over (`finishTabFrameAnim`) is done. The new frame normally stays
     * invisible, and the old one visible, for 1.5–3 s after step 100, so a bad state is only reported when it is still
     * there after [CHECKS] looks [CHECK_MS] apart (about 7.5 s); a newer add cancels the check.
     */
    @JvmStatic
    fun checkAfterAdd(context: Context?, currentNow: () -> View?) {
        if (!on(context)) return
        val mine = ++generation
        val handler = Handler(Looper.getMainLooper())
        var round = 0
        lateinit var look: Runnable
        look = Runnable {
            try {
                if (mine != generation) return@Runnable
                round++
                val current = currentNow()
                val parent = current?.parent as? ViewGroup ?: return@Runnable
                var visible = 0
                for (index in 0 until parent.childCount) {
                    val child = parent.getChildAt(index)
                    if (child is WebNestFrame && child.visibility == View.VISIBLE) visible++
                }
                val bad = when {
                    current.visibility != View.VISIBLE -> "current frame not visible"
                    visible > 1 -> "$visible frames visible"
                    else -> null
                }
                when {
                    bad == null -> Log.d(TAG, "after add ok (${round * CHECK_MS} ms): frames=[${frames(current)}]")
                    round < CHECKS -> handler.postDelayed(look, CHECK_MS)
                    else -> Log.w(TAG, "STALE FRAME: $bad ${round * CHECK_MS} ms after add, current=${id(current)} frames=[${frames(current)}]")
                }
            } catch (_: Exception) {
            }
        }
        handler.postDelayed(look, CHECK_MS)
    }

    private const val CHECKS = 5
    private const val CHECK_MS = 1500L
}
