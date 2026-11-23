package com.mycompany.app.view

import com.mycompany.app.ui.browser.BarFace
import com.mycompany.app.ui.state.AppState

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebView.FindListener
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.browser.ComposeOverlay

/**
 * The find-in-page bar (browser bar slot 3, and the page-source dialog). Compose now: back, text field, clear, "n / m"
 * counter, previous / next. The class keeps its old shape for `WebViewActivity` and `DialogViewSrc`:
 * [closeListener] (listener of the back button), [tintBar] (true when the bar sits at the top), [findWeb] (the WebView
 * to search), [startText] (text to start with), the [build] / [close] / [release] / [resetCount] / [applyColors] /
 * [focusEdit] methods, [findText], and [editField]. [editField] is still a real `EditText` (inside an
 * `AndroidView`) because the activity toggles its incognito-keyboard flag (`MainUtil.applyIncognitoIme`) and asks whether it has focus.
 */
class MyFindView(var ctx: Context?) : ComposeOverlay(ctx!!) {
    @JvmField var closeListener: DialogApplyListener? = null
    /** True when the bar is at the top of the screen. */
    @JvmField var tintBar: Boolean = false
    @JvmField var findWeb: WebView? = null
    @JvmField var startText: String? = null
    @JvmField var editField: MyEditPure? = null

    /** The widgets exist (after [build]); false before, and after [release]. */
    val isReady: Boolean get() = editField != null

    private class Colors(
        val key: Int,
        val background: Int,
        val field: Int,
        val content: Int,
        val hint: Int,
        val lightIcons: Boolean,
        val maxAlpha: Float,
        val pressed: Int,
    )

    private var colors by mutableStateOf<Colors?>(null)
    private var hasText by mutableStateOf(false)
    private var current by mutableIntStateOf(0)
    private var total by mutableIntStateOf(0)

    private fun toColor(argb: Int) = Color(argb)

    /** Sets up the bar (once) for the current [findWeb] and applies the colours. */
    fun build(normal: Int, pressed: Int, onStartPage: Boolean) {
        val context = ctx
        if (editField != null || context == null) return
        val edit = MyEditPure(context)
        edit.setSingleLine(true)
        edit.setTextDirection(3)
        edit.setTextSize(1, 16.0f)
        edit.setTextCursorDrawable(R.drawable.edit_cursor)
        edit.setHint(R.string.find_word)
        edit.setImeOptions(268435459)
        edit.setSelectAllOnFocus(true)
        edit.background = null
        editField = edit
        resetCount()
        applyColors(normal, pressed, onStartPage)
        setPage({ AppState.darkUi }) { FindBar() }
        findWeb?.setFindListener(object : FindListener {
            override fun onFindResultReceived(active: Int, matches: Int, done: Boolean) {
                if (!done || editField == null) return
                if (matches == 0) { resetCount(); return }
                current = active + 1
                total = matches
            }
        })
        MainUtil.disablePersonalizedIme(edit)
        edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                if (editField == null) return
                if (!TextUtils.isEmpty(editable)) {
                    hasText = true
                    findWeb?.findAllAsync(editable.toString())
                } else {
                    hasText = false
                    resetCount()
                    findWeb?.clearMatches()
                }
            }
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        edit.setOnEditorActionListener { _, _, _ -> findWeb?.findNext(true); true }
        if (visibility == VISIBLE) focusEdit()
    }

    /** Hides the bar and forgets the search. */
    fun close() {
        val edit = editField ?: return
        MainUtil.closeKeyboard(ctx, edit)
        visibility = GONE
        hasText = false
        edit.setText(null as CharSequence?)
        resetCount()
        findWeb?.clearMatches()
    }

    /** Releases the bar (the activity / dialog is going away). */
    fun release() {
        findWeb?.let { it.setFindListener(null); it.clearMatches() }
        findWeb = null
        (editField?.parent as? ViewGroup)?.removeView(editField)
        editField = null
        ctx = null
        closeListener = null
        startText = null
        colors = null
    }

    /** Resets the counter to "0 / 0". */
    fun resetCount() {
        current = 0
        total = 0
    }

    /** Applies the bar colours; [statusColor] is the bar colour setting, [naviColor] the accent, [onStartPage] the bar's dark flag. */
    fun applyColors(statusColor: Int, naviColor: Int, onStartPage: Boolean) {
        if (editField == null) return
        val barColor = MainUtil.barTint(if (!tintBar) 0 else statusColor, onStartPage)
        if (colors?.key == barColor) return
        val background = if (MainUtil.isDarkBars(onStartPage)) 0 else if (barColor == 0) -1 else if (barColor == 1) -16777216 else barColor
        val content = BarFace.Companion.pressColorFor(barColor, false)
        val newColors = Colors(
            key = barColor,
            background = background,
            field = BarFace.Companion.pillColorFor(barColor, naviColor, false),
            content = content,
            hint = BarFace.Companion.pressColorFor(barColor, true),
            lightIcons = barColor != 0,
            maxAlpha = BarFace.Companion.pressScale(barColor),
            pressed = MainUtil.themeIconColor(barColor, naviColor),
        )
        colors = newColors
        editField?.setTextColor(content)
        editField?.setHintTextColor(newColors.hint)
    }

    /** Puts the cursor in the field and shows the keyboard; fills in [startText] first when there is one. */
    fun focusEdit() {
        val edit = editField ?: return
        post {
            val field = editField ?: return@post
            field.requestFocus()
            val initial = startText
            if (TextUtils.isEmpty(initial)) {
                postDelayed({ editField?.let { MainUtil.openKeyboard(ctx, it) } }, 200L)
            } else {
                post {
                    val startTextNow = startText
                    startText = null
                    val target = editField
                    if (!TextUtils.isEmpty(startTextNow) && target != null) {
                        target.setText(startTextNow)
                        target.setSelection(startTextNow!!.length)
                        postDelayed({ editField?.let { MainUtil.openKeyboard(ctx, it) } }, 200L)
                    }
                }
            }
        }
        @Suppress("UNUSED_EXPRESSION") edit
    }

    val findText: String?
        get() = if (!TextUtils.isEmpty(startText)) startText else MainUtil.editTextValue(editField, false)

    @Composable
    private fun px(value: Int): Dp = with(LocalDensity.current) { value.toDp() }

    @Composable
    private fun FindBar() {
        val c = colors ?: return
        val rowHeight = px(MainApp.dp48)
        val buttonWidth = px(MainApp.dp48)
        val edit = editField ?: return
        val enabled = total > 0
        Box(Modifier.fillMaxSize().background(toColor(c.background)), contentAlignment = Alignment.Center) {
            Box(
                Modifier.fillMaxWidth().padding(horizontal = px(MainApp.dp4)).height(rowHeight - px(MainApp.dp8) * 2)
                    .background(toColor(c.field), RoundedCornerShape(px(MainApp.dp16))),
            )
            Row(Modifier.fillMaxWidth().height(rowHeight), verticalAlignment = Alignment.CenterVertically) {
                IconSlot(buttonWidth, rowHeight, c.maxAlpha, true, { closeListener?.onApply() }) {
                    Image(iconOf(c.lightIcons, R.drawable.outline_chevron_left_black_24, R.drawable.outline_chevron_left_dark_24), null)
                }
                AndroidView(
                    factory = { _ ->
                        (edit.parent as? ViewGroup)?.removeView(edit)
                        edit
                    },
                    modifier = Modifier.weight(1f).height(rowHeight),
                )
                if (hasText) {
                    IconSlot(buttonWidth, rowHeight, c.maxAlpha, true, {
                        edit.setText(null as CharSequence?)
                        resetCount()
                        findWeb?.clearMatches()
                    }) { Image(iconOf(c.lightIcons, R.drawable.outline_cancel_black_18, R.drawable.outline_cancel_dark_18), null) }
                }
                Text(
                    "$current / $total",
                    color = toColor(c.content), fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = px(MainApp.dp8)).alpha(if (enabled) 1f else 0.4f),
                )
                IconSlot(buttonWidth, rowHeight, c.maxAlpha, enabled, { findWeb?.findNext(false) }) {
                    Image(iconOf(c.lightIcons, R.drawable.outline_keyboard_arrow_up_black_24, R.drawable.outline_keyboard_arrow_up_dark_24), null)
                }
                IconSlot(buttonWidth, rowHeight, c.maxAlpha, enabled, { findWeb?.findNext(true) }) {
                    Image(iconOf(c.lightIcons, R.drawable.outline_keyboard_arrow_down_black_24, R.drawable.outline_keyboard_arrow_down_dark_24), null)
                }
            }
        }
    }

    @Composable
    private fun iconOf(lightIcons: Boolean, black: Int, dark: Int) = painterResource(if (lightIcons) dark else black)

    @Composable
    private fun IconSlot(width: Dp, height: Dp, maxAlpha: Float, enabled: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
        Box(
            Modifier.width(width).height(height).alpha(if (enabled) maxAlpha else maxAlpha * 0.4f)
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) { Box(Modifier.size(24.dp)) { content() } }
    }
}
