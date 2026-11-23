package com.mycompany.app.wview

import android.content.Context
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.TtsSettings
import android.graphics.RectF
import com.mycompany.app.main.MainApp
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefTts

/**
 * Floating read-aloud button: a spinner while speech starts, then a white "stop" square. Tap, move and fades come from
 * [FloatButton]; it does not auto-hide, the activity shows it with [appear] and fades it out with [fadeAway] (which waits
 * for a finger to lift first). Position settings live in `PrefTts`.
 */
class WebTtsView(context: Context) : FloatButton(context) {
    /** [fadeAway] was asked while a finger was down. */
    private var fadePending = false
    private var stop: RectF? = null

    fun setTtsListener(fltViewListener: WebFltView.FltViewListener?) = setFltListener(fltViewListener)

    override val autoHides get() = false

    override fun readPos() = floatArrayOf(TtsSettings.ttsLtX.value, TtsSettings.ttsRtX.value, TtsSettings.ttsUpY.value, TtsSettings.ttsDnY.value)

    override fun writePos(left: Float, right: Float, top: Float, bottom: Float) {
        TtsSettings.editLocal { set(TtsSettings.ttsLtX, left) }; TtsSettings.editLocal { set(TtsSettings.ttsRtX, right) }; TtsSettings.editLocal { set(TtsSettings.ttsUpY, top) }; TtsSettings.editLocal { set(TtsSettings.ttsDnY, bottom) }
    }

    override fun savePos() {
        PrefTts.saveTtsPos(context)
    }

    override fun defaultPos(areaWidth: Int, areaHeight: Int): IntArray {
        var y = (areaHeight - size) / 2
        if (areaWidth < areaHeight) y += MainApp.dp64
        return intArrayOf((areaWidth - size) / 2, y)
    }

    /** Shows the button with a fade-in: the spinner while [starting], else the stop square. */
    fun appear(starting: Boolean) {
        fadePending = false
        setLoading(starting)
        fadeInNow()
    }

    override fun onMoveStarted() = appear(false)

    /** Fades the button out, or as soon as the finger lifts. */
    fun fadeAway() {
        if (isFadingOut || visibility != VISIBLE) return
        if (touching || tapping || moving) {
            fadePending = true
            return
        }
        fadeOutNow()
    }

    override fun onFinger(down: Boolean) {
        if (down) fadePending = false
    }

    override fun afterRelease() {
        if (fadePending) {
            fadePending = false
            fadeAway()
        }
    }

    override fun onHidden() {
        spinning = false
    }

    private fun setLoading(on: Boolean) {
        spinning = on
        if (!on && stop == null) {
            val inset = ((MainApp.dp48 - MainApp.dp16) / 2).toFloat()
            stop = RectF(inset, inset, inset + MainApp.dp16, inset + MainApp.dp16)
        }
        invalidate()
    }

    override fun decorate(face: Face) {
        face.stopRect = stop
        face.stopColor = -1
    }

    /** Re-reads the colour, outline and spinner transparency settings. */
    fun refreshColors() {
        val color = PrefEditor.color
        val clear = EditorSettings.ttsPos.value > 0.9
        var changed = false
        if (bgColor != color) { bgColor = color; changed = true }
        val ring = if (clear) -1066044043 else 0
        if (ringColor != ring) { ringColor = ring; changed = true }
        if (setIconAlpha(if (clear && AppState.darkWeb) 176 else 255)) changed = true
        if (changed) invalidate()
    }
}
