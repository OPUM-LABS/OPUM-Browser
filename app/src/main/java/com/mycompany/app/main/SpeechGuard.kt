package com.mycompany.app.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import ch.opumlabs.opumbrowser.R

/**
 * Speaking that never fails silently (2026-11-26). The speech engine runs in its own process and can die (on some
 * emulators it crashes on every sentence); a dead engine refuses every call without a word, and a crash while speaking
 * reports nothing at all. [speak] therefore makes the engine again once when a call is refused, and a sentence that has
 * not started [START_WAIT] ms after asking counts as failed. A failure goes to the caller's `onFailed`, which usually
 * calls [showDown]. The owners keep their own [TextToSpeech]; they give [speak] a way to make a new one.
 *
 * The owner's [UtteranceProgressListener] calls [started] from `onStart` and [ended] from `onDone` / `onStop` /
 * `onError`, so the wait for the start ends.
 */
object SpeechGuard {
    /** How long a sentence may take to start before the engine counts as not answering. */
    const val START_WAIT = 5000L

    private val main = Handler(Looper.getMainLooper())
    private val waiting = HashMap<String, Runnable>()

    /**
     * Speaks [text] with [engine] ([prepare] sets language, speed and tone first). When the engine refuses (it died),
     * [recreate] makes a new one and calls back with it once it is ready (null when it could not), and the text is
     * tried once more. Returns true when [engine] took the text at once; false when it is being retried or failed
     * ([onFailed] is then called on the main thread).
     */
    fun speak(
        engine: TextToSpeech?,
        text: String,
        id: String,
        queue: Int = TextToSpeech.QUEUE_FLUSH,
        prepare: (TextToSpeech) -> Unit = {},
        recreate: ((TextToSpeech?) -> Unit) -> Unit,
        onFailed: () -> Unit,
    ): Boolean {
        if (attempt(engine, text, id, queue, prepare)) {
            watch(id, onFailed)
            return true
        }
        recreate { fresh ->
            main.post {
                if (attempt(fresh, text, id, queue, prepare)) watch(id, onFailed) else onFailed()
            }
        }
        return false
    }

    private fun attempt(engine: TextToSpeech?, text: String, id: String, queue: Int, prepare: (TextToSpeech) -> Unit): Boolean {
        if (engine == null) return false
        return try {
            prepare(engine)
            engine.speak(text, queue, null, id) == TextToSpeech.SUCCESS
        } catch (_: Exception) {
            false
        }
    }

    /** Makes an engine with [listener] and calls [onReady] with it when it is ready, or with null when it is not. */
    fun create(context: Context, listener: UtteranceProgressListener?, onReady: (TextToSpeech?) -> Unit): TextToSpeech? {
        var made: TextToSpeech? = null
        return try {
            made = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) onReady(made) else onReady(null)
            }
            if (listener != null) made.setOnUtteranceProgressListener(listener)
            made
        } catch (_: Exception) {
            onReady(null)
            null
        }
    }

    /** Lets an engine go, quietly. */
    fun release(engine: TextToSpeech?) {
        try { engine?.stop(); engine?.shutdown() } catch (_: Exception) {}
    }

    private fun watch(id: String, onFailed: () -> Unit) {
        main.post {
            waiting.remove(id)?.let(main::removeCallbacks)
            val timeout = Runnable { waiting.remove(id); onFailed() }
            waiting[id] = timeout
            main.postDelayed(timeout, START_WAIT)
        }
    }

    /** The sentence [id] started: it is not waited for any more. */
    fun started(id: String?) {
        if (id == null) return
        main.post { waiting.remove(id)?.let(main::removeCallbacks) }
    }

    /** The sentence [id] ended, was stopped or failed: it is not waited for any more. */
    fun ended(id: String?) = started(id)

    /** Stops every wait (the owner stopped speaking or closed). */
    fun cancelAll() {
        main.post {
            for (r in waiting.values) main.removeCallbacks(r)
            waiting.clear()
        }
    }

    /** "The speech engine isn't responding", with a button to Android's speech settings. */
    fun showDown(activity: Activity?) {
        if (activity == null || activity.isFinishing) return
        main.post {
            try {
                com.mycompany.app.dialog.DialogSetMsg(
                    activity, false, R.string.tts_engine_down, R.string.tts_open_settings, R.string.close,
                    object : com.mycompany.app.dialog.DialogSetMsg.DialogMsgListener {
                        override fun onConfirm() = openSettings(activity)
                        override fun onHelp() {}
                    },
                )
            } catch (_: Exception) {
            }
        }
    }

    /** Android's text-to-speech settings (engine, voices), or the accessibility settings where they live. */
    fun openSettings(context: Context) {
        try {
            context.startActivity(Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (_: Exception) {
            }
        }
    }
}
