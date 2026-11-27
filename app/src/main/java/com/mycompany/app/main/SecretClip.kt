package com.mycompany.app.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.PersistableBundle
import ch.opumlabs.opumbrowser.R

/**
 * Copying a password (2026-11-27): the clip is marked sensitive, so Android does not show it in the copy preview or
 * the keyboard's clipboard suggestions, and it is cleared after [CLEAR_AFTER_MS] if it is still on the clipboard.
 */
object SecretClip {
    private const val CLEAR_AFTER_MS = 60_000L
    private const val LABEL = "OPUM password"
    private val handler = Handler(Looper.getMainLooper())
    /** `ClipDescription.EXTRA_IS_SENSITIVE` (Android 13); Android 12L ignores it. */
    private const val IS_SENSITIVE = "android.content.extra.IS_SENSITIVE"

    fun copyPassword(context: Context?, password: String?) {
        if (context == null || password.isNullOrEmpty()) return
        val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return
        val clip = ClipData.newPlainText(LABEL, password)
        clip.description.extras = PersistableBundle().apply { putBoolean(IS_SENSITIVE, true) }
        try { clipboard.setPrimaryClip(clip) } catch (_: Exception) { return }
        MainUtil.showToast(context, R.string.copied_clipboard)
        val appContext = context.applicationContext
        handler.postDelayed({
            try {
                val cm = appContext.getSystemService(ClipboardManager::class.java) ?: return@postDelayed
                val now = cm.primaryClip
                if (now != null && now.description?.label == LABEL && now.itemCount > 0 && now.getItemAt(0).text?.toString() == password) {
                    cm.clearPrimaryClip()
                }
            } catch (_: Exception) {
            }
        }, CLEAR_AFTER_MS)
    }
}
