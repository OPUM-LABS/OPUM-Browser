package com.mycompany.app.view

import android.view.Gravity
import android.view.View
import android.widget.PopupWindow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.mycompany.app.ui.components.TickSwitch as Switch
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.browser.ComposeOverlay
import com.mycompany.app.view.PopMenuItem

/**
 * The app's small anchored menus (rotation, speed, tab and quick menus, …): a list of [PopMenuItem]s with an optional
 * icon ([PopMenuItem.iconRes]), the label ([PopMenuItem.titleRes] string resource or [PopMenuItem.title] text), a check
 * mark ([PopMenuItem.isChecked]) or a switch ([PopMenuItem.isChecked] with [PopMenuItem.isShown]), and dimmed disabled
 * rows ([PopMenuItem.isDisabled]). Compose since 2026-11-16 (Material 3 surface in an anchored [PopupWindow]); the
 * constructor, [MyPopupListener] and the [MyPopupWrap] contract are unchanged:
 * - a row tap calls [MyPopupListener.onItem]; when it returns true the menu closes;
 * - [dismissNow] releases the menu and tells the listener ([MyPopupListener.onClose]), [dismiss] closes it; tapping
 *   outside or Back closes it too;
 * - [isChecked] tells whether the row with an id is checked; [menuKind] = 3 opens it at the anchor's end;
 * - `view` is the window the menu belongs to and `dark` the dark flag.
 */
class MyPopupMenu(
    mainActivity: MainActivity?,
    view: View?,
    view2: View?,
    arrayList: ArrayList<Any?>?,
    dark: Boolean,
    myPopupListener: MyPopupListener?
) : MyPopupWrap() {
    interface MyPopupListener {
        fun onClose()
        fun onItem(view: View?, id: Int): Boolean
    }

    /** Placement: 3 = at the anchor's end (others: the default drop-down). */
    @JvmField var menuKind: Byte = 0
    /** Speech-bubble outline with a tail pointing at the anchor (the address menu; set right after construction, the
     *  menu opens on the next frame). Not for [menuKind] 3. */
    @JvmField var bubble: Boolean = false
    private val TAIL_DP = 14
    /** Callers set this for menus opened from the full-screen video (no difference with the Material popup). */

    private class Content(context: android.content.Context) : ComposeOverlay(context) {
        fun set(dark: Boolean, content: @androidx.compose.runtime.Composable () -> Unit) = setPage({ dark }, content)
    }

    private var activity: MainActivity? = mainActivity
    private var anchor: View? = view2
    private val items: List<PopMenuItem> = arrayList?.mapNotNull { it as? PopMenuItem } ?: emptyList()
    private val dark = dark
    private var listener: MyPopupListener? = myPopupListener
    private var popup: PopupWindow? = null
    private var closing = false

    init {
        val host = mainActivity
        val a = view2
        if (host == null || a == null) {
            dismissNow()
        } else {
            // the old menu opened on the next frame, after the caller set [menuKind]
            a.post { show(host, a) }
        }
    }

    private fun show(host: MainActivity, anchorView: View) {
        if (activity == null) return
        try {
            val withIcons = items.any { it.iconRes != 0 }
            // Open above the anchor when it sits in the lower half of the screen (bars at the bottom). PopupWindow only
            // flips by itself when it knows the height, which a Compose menu does not have before it is drawn, so the
            // address menu opened downward and was cut off below the bottom bar (2026-11-20).
            val density = host.resources.displayMetrics.density
            val tailDp = if (bubble && menuKind.toInt() != 3) TAIL_DP else 0
            val menuPx = ((minOf(items.size * 52, 480) + 16 + tailDp) * density).toInt()
            val where = IntArray(2)
            anchorView.getLocationOnScreen(where)
            val screenH = host.resources.displayMetrics.heightPixels
            val up = where[1] + anchorView.height / 2 > screenH / 2 && where[1] + anchorView.height + menuPx > screenH
            // speech bubble: the tail points at the anchor's middle (the popup starts at the anchor's left edge and the
            // surface 8 dp further in); the tip reaches to 2 dp from the anchor
            val tailX = anchorView.width / 2f - 8 * density
            val gap = if (tailDp != 0) (6 * density).toInt() else 0
            val content = Content(host)
            content.set(dark) {
                val shape = if (tailDp != 0) BubbleShape(12.dp, tailDp.dp, tailX, tailOnTop = !up) else RoundedCornerShape(12.dp)
                Surface(
                    shape = shape, tonalElevation = 3.dp, shadowElevation = 8.dp,
                    modifier = Modifier.padding(8.dp),
                ) {
                    androidx.compose.foundation.layout.Column(
                        Modifier.padding(top = if (tailDp != 0 && !up) tailDp.dp else 0.dp, bottom = if (tailDp != 0 && up) tailDp.dp else 0.dp)
                            .width(menuWidth(host)).heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                    ) {
                        for (item in items) Row(item, withIcons)
                    }
                }
            }
            val window = PopupWindow(content, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, true)
            window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(0))
            window.setOnDismissListener { dismissNow() }
            popup = window
            val yOff = if (up) -(anchorView.height + menuPx) + gap else -gap
            if (menuKind.toInt() == 3) {
                window.showAsDropDown(anchorView, 0, yOff, Gravity.END)
            } else {
                window.showAsDropDown(anchorView, 0, yOff)
            }
        } catch (_: Exception) {
            dismissNow()
        }
    }

    /** Rounded box with a triangular tail on its top or bottom edge at [tailX] px (kept clear of the corners). */
    private class BubbleShape(
        private val radius: androidx.compose.ui.unit.Dp,
        private val tail: androidx.compose.ui.unit.Dp,
        private val tailX: Float,
        private val tailOnTop: Boolean,
    ) : androidx.compose.ui.graphics.Shape {
        override fun createOutline(
            size: androidx.compose.ui.geometry.Size,
            layoutDirection: androidx.compose.ui.unit.LayoutDirection,
            density: androidx.compose.ui.unit.Density,
        ): androidx.compose.ui.graphics.Outline {
            val r = with(density) { radius.toPx() }
            val t = with(density) { tail.toPx() }
            val top = if (tailOnTop) t else 0f
            val bottom = if (tailOnTop) size.height else size.height - t
            val half = t * 1.2f
            val cx = tailX.coerceIn(r + half, (size.width - r - half).coerceAtLeast(r + half))
            val box = androidx.compose.ui.graphics.Path().apply {
                addRoundRect(androidx.compose.ui.geometry.RoundRect(0f, top, size.width, bottom, androidx.compose.ui.geometry.CornerRadius(r)))
            }
            val tip = androidx.compose.ui.graphics.Path().apply {
                if (tailOnTop) {
                    moveTo(cx - half, top + 1f); lineTo(cx, 0f); lineTo(cx + half, top + 1f)
                } else {
                    moveTo(cx - half, bottom - 1f); lineTo(cx, size.height); lineTo(cx + half, bottom - 1f)
                }
                close()
            }
            return androidx.compose.ui.graphics.Outline.Generic(
                androidx.compose.ui.graphics.Path.combine(androidx.compose.ui.graphics.PathOperation.Union, box, tip)
            )
        }
    }

    /** 196 dp, or wider (up to 300 dp) for long labels; each row counts its icon and switch (a label with both wrapped). */
    private fun menuWidth(host: MainActivity): androidx.compose.ui.unit.Dp {
        val withIcons = items.any { it.iconRes != 0 }
        var wanted = 0
        for (item in items) {
            val s = if (item.titleRes != 0) host.getString(item.titleRes) else item.title
            // about 9 dp per character at the menu text size, the row's padding, and its icon / switch / check
            val row = 9 * (s?.length ?: 0) + 64 + (if (withIcons) 40 else 0) + (if (item.isShown) 64 else if (item.isChecked) 32 else 0)
            if (row > wanted) wanted = row
        }
        return wanted.coerceIn(196, 300).dp
    }

    @androidx.compose.runtime.Composable
    private fun Row(item: PopMenuItem, withIcons: Boolean) {
        val enabled = !item.isDisabled
        Row(
            Modifier.fillMaxWidth().height(52.dp).alpha(if (enabled) 1f else 0.2f)
                .then(if (enabled) Modifier.clickable { pick(item.id) } else Modifier)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (item.iconRes != 0) {
                Icon(painterResource(item.iconRes), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(16.dp))
            } else if (withIcons) {
                Spacer(Modifier.width(40.dp))
            }
            Text(
                if (item.titleRes != 0) stringResource(item.titleRes) else item.title ?: "",
                style = MaterialTheme.typography.bodyLarge, maxLines = 2, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (item.isShown) {
                Switch(checked = item.isChecked, onCheckedChange = null, modifier = Modifier.padding(start = 8.dp))
            } else if (item.isChecked) {
                Icon(painterResource(R.drawable.outline_check_black_24), contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp).size(24.dp))
            }
        }
    }

    private fun pick(id: Int) {
        val l = listener ?: return
        if (l.onItem(anchor, id)) dismiss()
    }

    /** Releases the menu and tells the listener, once. */
    override fun dismissNow() {
        val host = activity
        activity = null
        if (host == null) return
        val window = popup
        popup = null
        try { window?.dismiss() } catch (_: Exception) {}
        val listenerNow = listener
        listener = null
        anchor = null
        listenerNow?.onClose()
    }

    /** Closes the menu. */
    override fun dismiss() {
        if (closing) return
        closing = true
        dismissNow()
    }

    /** Whether the row with [id] is checked. */
    fun isChecked(id: Int): Boolean = items.firstOrNull { it.id == id }?.isChecked ?: false

}

/** Row model of [MyPopupMenu] and of the menus that build item lists for it (was nested in `MyPopupAdapter`). */
class PopMenuItem {
    constructor() : super()

    @JvmField
    var id: Int = 0
    @JvmField
    var iconRes: Int = 0
    @JvmField
    var titleRes: Int = 0
    @JvmField
    var title: String? = null
    @JvmField
    var isChecked: Boolean = false
    @JvmField
    var isShown: Boolean = false
    @JvmField
    var isDisabled: Boolean = false

    constructor(i: Int, i2: Int, z: Boolean, obj: Any?) {
        this.id = i
        this.titleRes = i2
        this.isChecked = false
        this.isShown = false
        this.isDisabled = z
    }

    constructor(i: Int, i2: Int) {
        this.id = i
        this.titleRes = i2
    }

    constructor(i: Int, str: String?) {
        this.id = i
        this.title = str
    }

    constructor(i: Int, i2: Int, z: Boolean) {
        this.id = i
        this.titleRes = i2
        this.isChecked = z
    }

    constructor(str: String?, z: Boolean, i: Int) {
        this.id = i
        this.title = str
        this.isChecked = z
    }

    constructor(i: Int, i2: Int, i3: Int, z: Boolean) {
        this.id = i
        this.titleRes = i2
        this.isChecked = z
        this.isShown = true
    }

    constructor(i: Int, i2: Int, str: String?) {
        this.id = i
        this.iconRes = i2
        this.title = str
    }
}

/** What the activity can close or update while a popup menu is open (was `view/MyPopupWrap.kt`). */
@Suppress("deprecation")
open class MyPopupWrap {
    open fun dismissNow() {
    }

    open fun dismiss() {
    }
}
