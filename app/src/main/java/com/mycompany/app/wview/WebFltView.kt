package com.mycompany.app.wview

import android.content.Context
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.FloatSettings
import android.view.View
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefFloat
import com.mycompany.app.pref.PrefZtri

/**
 * The page's floating action buttons, one class for all [kind]s (0 was the one-handed mode button, gone 2026-11-25): 1 RSS, 2 lock, 3 translate,
 * 4 text zoom, 5 full screen, 6 / 7 / 8 the three user buttons (their action in `FloatSettings.flt1Icon.value/k/l`). Each kind has its
 * own colour, see-through level, "always shown" switch and stored position. Tap, move, fades and auto-hide come from
 * [FloatButton]; a tap reports the button's action code. [setPreview] is the settings dialog's live inPreview (centred,
 * position not stored, never auto-hides).
 */
class WebFltView(context: Context, kindCode: Int) : FloatButton(context) {
    fun interface FltTouchListener {
        fun onPressed(down: Boolean)
    }

    interface FltViewListener {
        /** Tapped, with the button's action code. */
        fun onTap(view: View?, button: Int)
        /** Went away (or the move hint should go). */
        fun onGone()
        /** Long pressed: offer the move hint. */
        fun onLongPress(view: View?)
    }

    /** Which button this is (1..8, see the class comment). */
    @JvmField val buttonKind: Byte = kindCode.toByte()
    /** Page scrolling keeps it shown (zoom etc.). */
    @JvmField var hideScrollBlocked = false

    /** The action code a tap reports (the kind's own action, or the user's choice for 6..8). */
    private var action: Int
    private var iconRes = 0
    private var touchListener: FltTouchListener? = null

    init {
        action = kindCode
        val res = when (kindCode) {
            1 -> ch.opumlabs.opumbrowser.R.drawable.outline_rss_feed_white_24
            2 -> ch.opumlabs.opumbrowser.R.drawable.outline_lock_white_24
            3 -> ch.opumlabs.opumbrowser.R.drawable.outline_g_translate_white_24
            4 -> ch.opumlabs.opumbrowser.R.drawable.outline_zoom_in_white_24
            5 -> ch.opumlabs.opumbrowser.R.drawable.outline_fullscreen_white_24
            6 -> { action = FloatSettings.flt1Icon.value; MainUtil.iconResFor(action, 2) }
            7 -> { action = FloatSettings.flt2Icon.value; MainUtil.iconResFor(action, 2) }
            else -> { action = FloatSettings.flt3Icon.value; MainUtil.iconResFor(action, 2) }
        }
        loadIcon(res)
    }

    override val tapCode get() = action

    /** True while a page action of this button is running (spinner). */
    val isLoading get() = spinning

    private fun colorPos(): Float = when (buttonKind.toInt()) {
        1 -> EditorSettings.newsPos.value
        2 -> EditorSettings.passPos.value
        3 -> EditorSettings.trnsPos.value
        4 -> EditorSettings.zoomPos.value
        5 -> 1.0f
        6 -> FloatSettings.flt1Pos.value
        7 -> FloatSettings.flt2Pos.value
        else -> FloatSettings.flt3Pos.value
    }

    private fun color(): Int = when (buttonKind.toInt()) {
        1 -> PrefEditor.newsArgb
        2 -> PrefEditor.passArgb
        3 -> PrefEditor.trnsArgb
        4 -> PrefEditor.zoomArgb
        5 -> -16777216
        6 -> PrefFloat.flt1Argb
        7 -> PrefFloat.flt2Argb
        else -> PrefFloat.flt3Argb
    }

    /** Icon / spinner alpha: 176 over a light page when the button is see-through (never for full screen). */
    private fun wantedAlpha(): Int = if (buttonKind.toInt() != 5 && colorPos() > 0.9 && AppState.darkWeb) 176 else 255

    private fun loadIcon(res: Int): Boolean {
        if (res == 0) return false
        iconRes = res
        iconAlpha = wantedAlpha()
        return setIconRes(res)
    }

    override fun isHideBlocked(): Boolean = inPreview || when (buttonKind.toInt()) {
        6 -> FloatSettings.flt1Show.value
        7 -> FloatSettings.flt2Show.value
        8 -> FloatSettings.flt3Show.value
        else -> hideBlocked
    }

    /** [isHideBlocked], or (buttons 0..5) page scrolling keeps it. */
    fun isKeptShown(): Boolean = isHideBlocked() || (buttonKind < 6 && hideScrollBlocked)

    override fun readPos(): FloatArray = when (buttonKind.toInt()) {
        1 -> floatArrayOf(ZtriSettings.newsLtX.value, ZtriSettings.newsRtX.value, ZtriSettings.newsUpY.value, ZtriSettings.newsDnY.value)
        2 -> floatArrayOf(ZtriSettings.passLtX.value, ZtriSettings.passRtX.value, ZtriSettings.passUpY.value, ZtriSettings.passDnY.value)
        3 -> floatArrayOf(ZtriSettings.trnsLtX.value, ZtriSettings.trnsRtX.value, ZtriSettings.trnsUpY.value, ZtriSettings.trnsDnY.value)
        4 -> floatArrayOf(ZtriSettings.zoomLtX.value, ZtriSettings.zoomRtX.value, ZtriSettings.zoomUpY.value, ZtriSettings.zoomDnY.value)
        5 -> floatArrayOf(ZtriSettings.comicLtX.value, ZtriSettings.comicRtX.value, ZtriSettings.comicUpY.value, ZtriSettings.comicDnY.value)
        6 -> floatArrayOf(FloatSettings.flt1LtX.value, FloatSettings.flt1RtX.value, FloatSettings.flt1UpY.value, FloatSettings.flt1DnY.value)
        7 -> floatArrayOf(FloatSettings.flt2LtX.value, FloatSettings.flt2RtX.value, FloatSettings.flt2UpY.value, FloatSettings.flt2DnY.value)
        else -> floatArrayOf(FloatSettings.flt3LtX.value, FloatSettings.flt3RtX.value, FloatSettings.flt3UpY.value, FloatSettings.flt3DnY.value)
    }

    override fun writePos(left: Float, right: Float, top: Float, bottom: Float) {
        when (buttonKind.toInt()) {
            1 -> { ZtriSettings.editLocal { set(ZtriSettings.newsLtX, left) }; ZtriSettings.editLocal { set(ZtriSettings.newsRtX, right) }; ZtriSettings.editLocal { set(ZtriSettings.newsUpY, top) }; ZtriSettings.editLocal { set(ZtriSettings.newsDnY, bottom) } }
            2 -> { ZtriSettings.editLocal { set(ZtriSettings.passLtX, left) }; ZtriSettings.editLocal { set(ZtriSettings.passRtX, right) }; ZtriSettings.editLocal { set(ZtriSettings.passUpY, top) }; ZtriSettings.editLocal { set(ZtriSettings.passDnY, bottom) } }
            3 -> { ZtriSettings.editLocal { set(ZtriSettings.trnsLtX, left) }; ZtriSettings.editLocal { set(ZtriSettings.trnsRtX, right) }; ZtriSettings.editLocal { set(ZtriSettings.trnsUpY, top) }; ZtriSettings.editLocal { set(ZtriSettings.trnsDnY, bottom) } }
            4 -> { ZtriSettings.editLocal { set(ZtriSettings.zoomLtX, left) }; ZtriSettings.editLocal { set(ZtriSettings.zoomRtX, right) }; ZtriSettings.editLocal { set(ZtriSettings.zoomUpY, top) }; ZtriSettings.editLocal { set(ZtriSettings.zoomDnY, bottom) } }
            5 -> { ZtriSettings.editLocal { set(ZtriSettings.comicLtX, left) }; ZtriSettings.editLocal { set(ZtriSettings.comicRtX, right) }; ZtriSettings.editLocal { set(ZtriSettings.comicUpY, top) }; ZtriSettings.editLocal { set(ZtriSettings.comicDnY, bottom) } }
            6 -> { FloatSettings.editLocal { set(FloatSettings.flt1LtX, left) }; FloatSettings.editLocal { set(FloatSettings.flt1RtX, right) }; FloatSettings.editLocal { set(FloatSettings.flt1UpY, top) }; FloatSettings.editLocal { set(FloatSettings.flt1DnY, bottom) } }
            7 -> { FloatSettings.editLocal { set(FloatSettings.flt2LtX, left) }; FloatSettings.editLocal { set(FloatSettings.flt2RtX, right) }; FloatSettings.editLocal { set(FloatSettings.flt2UpY, top) }; FloatSettings.editLocal { set(FloatSettings.flt2DnY, bottom) } }
            else -> { FloatSettings.editLocal { set(FloatSettings.flt3LtX, left) }; FloatSettings.editLocal { set(FloatSettings.flt3RtX, right) }; FloatSettings.editLocal { set(FloatSettings.flt3UpY, top) }; FloatSettings.editLocal { set(FloatSettings.flt3DnY, bottom) } }
        }
    }

    override fun savePos() {
        val kindIndex = buttonKind.toInt()
        if (kindIndex >= 6) {
            val pref = PrefFloat.get(context) ?: return
            val n = kindIndex - 5
            val pos = readPos()
            pref.putFloat("mFlt${n}LtX", pos[0])
            pref.putFloat("mFlt${n}RtX", pos[1])
            pref.putFloat("mFlt${n}UpY", pos[2])
            pref.putFloat("mFlt${n}DnY", pos[3])
            pref.saveAsync()
            return
        }
        val pref = PrefZtri.get(context) ?: return
        val name = when (kindIndex) {
            1 -> "News"
            2 -> "Pass"
            3 -> "Trns"
            4 -> "Zoom"
            else -> "Comic"
        }
        val pos = readPos()
        pref.putFloat("m${name}LtX", pos[0])
        pref.putFloat("m${name}RtX", pos[1])
        pref.putFloat("m${name}UpY", pos[2])
        pref.putFloat("m${name}DnY", pos[3])
        pref.saveAsync()
    }

    override fun defaultPos(areaWidth: Int, areaHeight: Int): IntArray = when (buttonKind.toInt()) {
        1 -> intArrayOf((areaWidth - size) - MainApp.dp8, ((areaHeight - size) - MainUtil.bottomInsetTotal()) - MainApp.dp16)
        2, 5 -> intArrayOf((areaWidth - size) - MainApp.dp16, MainUtil.topBarHeight() + MainApp.dp16)
        3 -> intArrayOf(((areaWidth - size) - MainApp.dp48) - MainApp.dp4, (((areaHeight - size) - MainUtil.bottomInsetTotal()) - MainApp.dp16) - MainApp.dp4)
        else -> intArrayOf((areaWidth - size) / 2, (areaHeight - size) / 2)
    }

    override fun onFinger(down: Boolean) {
        touchListener?.onPressed(down)
    }

    fun setTouchListener(fltTouchListener: FltTouchListener?) {
        touchListener = fltTouchListener
    }

    /** Settings inPreview: centred, position not stored, never auto-hides. */
    fun setPreview(on: Boolean) = startPreview(on)

    /** Spinner instead of the icon while the button's action runs. */
    fun setLoad(on: Boolean) {
        if (spinning == on) return
        spinning = on
        if (on) loadingAlpha = wantedAlpha()
        invalidate()
    }

    /** Switches a user button to another action and its icon. */
    fun setIconView(code: Int) {
        if (action == code) return
        action = code
        loadIcon(MainUtil.iconResFor(code, 2))
        invalidate()
    }

    /** Re-reads the icon / spinner transparency (the page went light or dark); [publish] redraws. True if changed. */
    fun refreshAlpha(publish: Boolean): Boolean {
        val changed = setIconAlpha(wantedAlpha())
        if (changed && publish) invalidate()
        return changed
    }

    /** If this button runs [code], re-reads its icon (it can depend on the theme); [publish] redraws. */
    fun refreshIconFor(code: Int, publish: Boolean): Boolean {
        if (action != code) return false
        val res = MainUtil.iconResFor(code, 2)
        if (iconRes == res) return false
        val changed = loadIcon(res)
        if (changed && publish) invalidate()
        return changed
    }

    /** Theme changed: refresh the dark-mode icon (action 22) and the transparency. */
    fun refreshTheme() {
        val a = refreshIconFor(22, false)
        val changed = refreshAlpha(false)
        if (a || changed) invalidate()
    }

    /** Downloads (action 4) with finished downloads unseen: the app's main colour (2026-11-20). */
    private fun downAccent(): Boolean = action == 4 && com.mycompany.app.down.DownState.unseenCount(context) > 0

    private val unseenListener = Runnable { post { refreshColors() } }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        com.mycompany.app.down.DownState.addListener(unseenListener)
        refreshColors()
    }

    override fun onDetachedFromWindow() {
        com.mycompany.app.down.DownState.removeListener(unseenListener)
        super.onDetachedFromWindow()
    }

    /** Re-reads the colour, outline and transparency settings. */
    fun refreshColors() {
        val newBgColor = if (downAccent()) com.mycompany.app.down.DownState.accent(context, AppState.darkUi) else color()
        var changed = false
        if (bgColor != newBgColor) { bgColor = newBgColor; changed = true }
        val ring = if (colorPos() > 0.9) -1066044043 else 0
        if (ringColor != ring) { ringColor = ring; changed = true }
        if (setIconAlpha(wantedAlpha())) changed = true
        if (changed) invalidate()
    }
}
