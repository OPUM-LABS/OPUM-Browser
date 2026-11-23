package com.mycompany.app.ui.browser

import android.content.Context
import com.mycompany.app.pref.store.ZtriSettings
import android.graphics.Outline
import android.text.TextUtils
import android.view.View
import android.view.ViewOutlineProvider
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.hasKnownScheme
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.pref.PrefZtri
import java.util.concurrent.ExecutorService

/**
 * The "Copied text" row of the address editor (QuickSearch), Compose since 2026-11-17 (was `view/MyClipView`).
 * The logic is the old one: tapping the row opens the clipboard text ([MyClipListener.onOpenText]), the arrow fills the editor
 * with it ([MyClipListener.onFillText], a space added when it is not a URL), the eye shows a 100-character preview instead of
 * "Copied text"; the clipboard is read in the background. Fields and calls keep their old names for QuickSearch:
 * [darkColor] dark-page flag, [roundOn] round corners allowed, [reversedOrder] reversed (bar at the bottom), [clipListener]
 * listener.
 */
class ClipRow(context: Context) : ComposeOverlay(context) {
    interface MyClipListener {
        fun onOpenText(clipText: String?)

        fun onFillText(clipText: String?)
    }

    @JvmField var appContext: Context? = context
    @JvmField var darkColor: Boolean = false
    @JvmField var roundOn: Boolean = false
    @JvmField var reversedOrder: Boolean = false
    @JvmField var clipListener: MyClipListener? = null
    @JvmField var clipText: String? = null
    @JvmField var subText: String? = null

    private var built by mutableStateOf(false)
    private var busy = false
    private var loading = false
    private var executor: ExecutorService? = null
    private var rounded = false

    private var preview by mutableStateOf(false)
    private var previewText by mutableStateOf<String?>(null)
    private var dark by mutableStateOf(false)
    private var reversed by mutableStateOf(false)
    private var label by mutableStateOf(true)

    init {
        setPage({ dark }) { Content() }
    }

    @androidx.compose.runtime.Composable
    private fun Content() {
        if (!built) return
        val rowHeight = with(LocalDensity.current) { MainApp.dp48.toDp() }
        Row(
            Modifier.fillMaxSize().clickable { open() },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.width(rowHeight).fillMaxHeight(), contentAlignment = Alignment.Center) {
                Icon(painterResource(R.drawable.outline_content_copy_black_20), null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = if (!label) "" else if (preview) (previewText ?: "") else stringResource(R.string.copied_text),
                fontSize = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f).padding(end = 8.dp),
            )
            IconButton(onClick = { togglePreview() }) {
                Icon(
                    painterResource(if (preview) R.drawable.outline_visibility_off_black_20 else R.drawable.outline_visibility_black_20),
                    null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { fill() }) {
                Icon(
                    painterResource(if (reversed) R.drawable.outline_south_west_black_20 else R.drawable.outline_north_west_black_20),
                    null, tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    private fun readClip() {
        clipText = MainUtil.clipboardText(appContext)
        clipSub()
    }

    private fun clipSub() {
        var clipTextNow = clipText
        subText = clipTextNow
        if (clipTextNow == null) return
        if (clipTextNow.length > 100) clipTextNow = clipTextNow.substring(0, 100)
        if (!TextUtils.isEmpty(clipTextNow)) {
            clipTextNow = clipTextNow.replace(" ", " ").replace("-", "‑").replace("/", "⁄")
                .replace("[\\f\\n\\r\\t\\x0B]".toRegex(), "")
        }
        subText = clipTextNow
    }

    /** Row tap. */
    private fun open() {
        if (clipListener == null || busy) return
        busy = true
        run {
            if (TextUtils.isEmpty(clipText)) readClip()
            post {
                if (TextUtils.isEmpty(clipText)) showToast(appContext, R.string.empty) else clipListener?.onOpenText(clipText)
                busy = false
            }
        }
    }

    /** Arrow. */
    private fun fill() {
        if (clipListener == null || busy) return
        busy = true
        run {
            if (TextUtils.isEmpty(clipText)) readClip()
            post {
                val clipTextNow = clipText
                if (TextUtils.isEmpty(clipTextNow)) {
                    showToast(appContext, R.string.empty)
                } else {
                    clipListener?.onFillText(if (!hasKnownScheme(clipTextNow)) "$clipTextNow " else clipTextNow)
                }
                busy = false
            }
        }
    }

    /** Eye. */
    private fun togglePreview() {
        if (loading) return
        preview = !preview
        if (!preview) return
        if (!TextUtils.isEmpty(clipText)) {
            previewText = subText
            return
        }
        loading = true
        run {
            readClip()
            post {
                if (preview) previewText = subText
                loading = false
            }
        }
    }

    private fun run(task: Runnable) {
        var ex = executor
        if (ex == null || ex.isShutdown || ex.isTerminated) {
            ex = MainApp.executor(appContext) ?: return
            executor = ex
        }
        try {
            ex.execute(task)
        } catch (unused: Exception) {
        }
    }

    /** Hides the preview again. */
    fun hidePreview() {
        preview = false
    }

    /** Builds the row once. */
    fun build() {
        if (built) return
        built = true
        if (roundOn) applyRound()
        setColor(darkColor)
    }

    /** New clipboard text [str] (or read it); refreshes the preview when shown. */
    fun fillText(newText: String?) {
        if (loading || !built) return
        loading = true
        if (TextUtils.isEmpty(newText)) {
            readClip()
        } else {
            clipText = newText
            clipSub()
        }
        if (!preview) {
            loading = false
        } else {
            post {
                if (preview) previewText = subText
                loading = false
            }
        }
    }

    /** Rounded corners when the quick list is in its rounded style (ZtriSettings.qckSchType.value == 1). */
    fun applyRound() {
        val on = ZtriSettings.qckSchType.value == 1
        if (rounded == on) return
        rounded = on
        if (!on) {
            clipToOutline = false
        } else {
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, MainApp.dp16.toFloat())
                }
            }
            clipToOutline = true
        }
    }

    /** Shows ([clipShown]) or hides the row; [onStartPage] dark-page flag, [newReversedOrder] reversed. */
    fun applyState(clipShown: Boolean, onStartPage: Boolean, newReversedOrder: Boolean) {
        if (!built) return
        preview = false
        if (!clipShown) {
            label = false
            visibility = View.GONE
            return
        }
        reversedOrder = newReversedOrder
        setColor(onStartPage)
        if (roundOn) applyRound()
        label = true
        visibility = View.VISIBLE
    }

    fun setColor(darkFlag: Boolean) {
        darkColor = darkFlag
        dark = MainUtil.isDarkPage(darkFlag)
        reversed = reversedOrder
    }

}
