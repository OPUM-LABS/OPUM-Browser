package com.mycompany.app.view

import com.mycompany.app.ui.browser.BarFace
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.MainSettings

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView.ScaleType
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.Canvas
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.themeIconColor
import com.mycompany.app.main.MainUtil.parseIntList
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.ui.browser.ComposeOverlay
import com.mycompany.app.ui.browser.BarFace.AddrIconListener
import com.mycompany.app.ui.browser.BarGroups
import com.mycompany.app.ui.browser.groupNotch

/**
 * One plain icon button of a [MyBarView] as state (image, alpha, visibility, enabled, press colour). Keeps the method names
 * the old `MyIconView` buttons had so the bar code and the activity (`setClickEnabled` for back / forward, ...) read the same.
 */
class BarIcon {
    var res by mutableIntStateOf(0)
    var shown by mutableIntStateOf(8)
    var alphaMax by mutableFloatStateOf(1f)
    var enabledNow by mutableStateOf(true)
    var press by mutableIntStateOf(0)

    fun setImageResource(resource: Int) { res = resource }
    fun setVisibility(value: Int) { shown = value }
    fun setMaxAlpha(value: Float) { alphaMax = value }
    fun setClickEnabled(value: Boolean) { enabledNow = value }
    fun setBgPreColor(color: Int) { press = color }
}

/**
 * The browser's button bars: the address bar with the URL pill, the toolbar, and the same bars inside the menus. Compose
 * now. The bar is configured with a list of button ids (`a(...)`); plain buttons are drawn here ([BarIcon] state), while the
 * URL pill (id 1) and the tab-count button (id 30), which draw custom content, are still the `MyIconView` and are hosted in
 * `AndroidView`s, as are the download / YouTube indicator icons the activity adds with `addView`. API, fields and callbacks
 * are those of the old LinearLayout bar, so `WebViewActivity` and the menu dialogs use it as before; the listeners receive
 * the bar (not the tapped button) as the `View` for plain buttons.
 */
class MyBarView(context: Context?) : ComposeOverlay(context!!) {
    /** Bar kind: 0 menu bar, 1 address bar, 2 toolbar. */
    @JvmField var barKind: Byte = 0
    @JvmField var clickListener: BarListener? = null
    @JvmField var itemIds: IntArray? = null
    @JvmField var longIds: IntArray? = null
    @JvmField var shownIds: IntArray? = null
    /** The URL pill (id 1) and the tab-count button (id 30), hosted `MyIconView`s. */
    @JvmField var addrFace: BarFace? = null
    @JvmField var tabFace: BarFace? = null
    /** Plain buttons the activity or the bar read: back (26), forward (27), stars (63, 29), offline download (64), reload / stop (35). */
    @JvmField var backIcon: BarIcon? = null
    @JvmField var forwardIcon: BarIcon? = null
    @JvmField var starIcon: BarIcon? = null
    @JvmField var star2Icon: BarIcon? = null
    @JvmField var downIcon: BarIcon? = null
    @JvmField var loadIcon: BarIcon? = null
    @JvmField var starred: Boolean = false
    @JvmField var downAlert: Boolean = false
    /** The bar holds the URL pill. */
    @JvmField var hasAddress: Boolean = false
    @JvmField var hasMenu: Boolean = false
    /** Compact layout: not enough room for the buttons next to the pill. */
    @JvmField var tightFit: Boolean = false
    @JvmField var iconLoading: Boolean = false
    @JvmField var colorType: Int = 0
    @JvmField var filterTint: Int = 0
    /** The reload / stop button (35) sits inside the URL pill's right end instead of a slot of its own (address bar holding both). */
    private var reloadInPill by mutableStateOf(false)
    /** True when the last touch event was handled inside the bar. */
    @JvmField var barTouched: Boolean = false

    private class Entry(val index: Int, val id: Int) {
        var icon: BarIcon? = null
        var host: BarFace? = null
    }

    private val entries = mutableStateListOf<Entry>()
    private val extras = mutableStateListOf<View>()
    private val rects = HashMap<Int, Rect>()
    private var compact by mutableStateOf(false)
    private var filter by mutableIntStateOf(0)
    private var pill: BarFace? = null
    private var tabs: BarFace? = null
    private var pressColor by mutableIntStateOf(0)
    /** Bumped when a hosted icon (pill, tab count, …) changes visibility; a GONE one takes no room in the row. */
    private var hostTick by mutableIntStateOf(0)
    /** Index of the group whose pill is open, or -1. */
    private var openGroup by mutableIntStateOf(-1)

    interface BarListener {
        fun onItemClick(id: Int, view: View?, isLong: Boolean)
    }

    class BarItem {
        @JvmField var myBarView: View? = null
        @JvmField var index: Int = 0
    }

    init {
        setPage({ AppState.darkUi }) { Bar() }
    }

    /** Adds one of the activity's indicator icons (download / YouTube) after the buttons. */
    override fun addView(child: View?, width: Int, height: Int) {
        if (child == null) return
        (child.parent as? ViewGroup)?.removeView(child)
        if (!extras.contains(child)) {
            child.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(view: View) {}
                override fun onViewDetachedFromWindow(view: View) {
                    // removed by the activity (its parent is gone): forget it; a detach with the bar itself keeps it
                    post { if (view.parent == null) extras.remove(view) }
                }
            })
            extras.add(child)
        }
        (child as? com.mycompany.app.ui.browser.BarIndicator)?.onVisibility = Runnable { hostTick++ }
    }

    private fun validId(id: Int) = (id != 0 && id != 68 && id >= 0 && id < 74) || BarGroups.isGroup(id)

    private fun layoutFor(id: Int): Pair<Int, Int> {
        val widthPx: Int
        val weight: Int
        // Reads the Compose state mirror of `tightFit`: Slot() is skipped when only the bar width changes, so reading the plain
        // field left the slots at their old sizes (e.g. squeezed after leaving picture-in-picture or split screen).
        if (compact) {
            widthPx = if (id == 1 || id != 30) 0 else MainApp.dp48
            weight = if (id == 1) 4 else if (id != 30) 1 else 0
        } else if (!hasAddress || id == 1) {
            widthPx = 0
            weight = 1
        } else {
            widthPx = MainApp.dp48
            weight = 0
        }
        return widthPx to weight
    }

    /** Sets the bar up for the button ids [iArr]; most parameters are what the old LinearLayout bar took (see the activity). */
    fun setupBar(
        context: Context?, ids: IntArray?, hostText: String?, title: String?, hostColor: Int, secret: Boolean,
        currentTabIndex: Int, size: Int, onStartPage: Boolean, newColorType: Int, iconColor: Int, barWidth: Int, x: Int,
    ) {
        this.backIcon?.setClickEnabled(true)
        this.forwardIcon?.setClickEnabled(true)
        this.tabFace?.setupTab(0, 0, 0)
        this.addrFace = null; this.tabFace = null
        this.backIcon = null; this.forwardIcon = null; this.starIcon = null; this.star2Icon = null; this.downIcon = null; this.loadIcon = null
        this.itemIds = ids
        this.barKind = x.toByte()
        openGroup = -1
        if (x == 2) {
            this.longIds = parseIntList(MainSettings.botLongs.value)
            this.shownIds = parseIntList(MainSettings.botSwipe.value)
        }
        val length = ids?.size ?: 0
        hasAddress = false; hasMenu = false; tightFit = false
        if (barKind.toInt() != 0) {
            var widthNow = barWidth
            for (index in 0 until length) {
                val id = ids!![index]
                if (id == 1) {
                    hasAddress = true
                    if (widthNow == 0) widthNow = getWidth()
                    if ((length + 3) * MainApp.dp48 > widthNow) tightFit = true
                } else if (id == 2) hasMenu = true
            }
        }
        compact = tightFit
        // the reload / stop button lives at the end of the address pill, in whichever bar holds the pill (2026-11-19); a
        // separate reload button in the same bar is not drawn
        reloadInPill = barKind.toInt() != 0 && hasAddress
        colorType = newColorType
        val press = themeIconColor(newColorType, iconColor)
        pressColor = press
        val list = ArrayList<Entry>()
        val host = context ?: getContext()
        for (index in 0 until length) {
            val id = ids!![index]
            val entry = Entry(index, id)
            if (id == 1 || id == 30) {
                val view = (if (id == 1) pill else tabs) ?: BarFace(host).also {
                    it.setScaleType(ScaleType.CENTER_INSIDE)
                    if (id == 1) pill = it else tabs = it
                }
                view.setTag(index)
                view.onVisibility = Runnable { hostTick++ }
                entry.host = view
                if (id == 1) addrFace = view else tabFace = view
            } else {
                val icon = BarIcon()
                entry.icon = icon
                when (id) {
                    26 -> backIcon = icon
                    27 -> this.forwardIcon = icon
                    63 -> starIcon = icon
                    29 -> star2Icon = icon
                    64 -> downIcon = icon
                    35 -> loadIcon = icon
                }
            }
            list.add(entry)
        }
        for (entry in list) {
            val id = entry.id
            val view = entry.host
            val icon = entry.icon
            if (!validId(id)) {
                if (id == 68) { icon?.setVisibility(4) } else { icon?.setVisibility(8); view?.setVisibility(8) }
                continue
            }
            if (icon != null) icon.setVisibility(0)
            view?.setVisibility(0)
            if (id == 1 && view != null) {
                view.setupBackground(newColorType, iconColor, onStartPage, barKind.toInt() == 1)
                view.setupHost(hostColor, newColorType, host, hostText, title, secret)
            } else if (id == 30 && view != null) {
                view.setupTab(currentTabIndex, size, newColorType)
            } else if (icon != null) {
                icon.setImageResource(imageFor(id, newColorType))
            }
            if (view != null) {
                view.setCircle(id == 1, id != 1)
                view.setBgPreColor(press)
                if (id == 1) {
                    view.setHostPreColor(press)
                    view.setTrnsPreColor(if (secret) press else 0)
                    view.setNoAnim(false)
                } else view.setNoAnim(barKind.toInt() == 2)
                view.setOnClickListener { view2 -> this.clickListener?.onItemClick(id, view2, false) }
                when {
                    barKind.toInt() == 1 -> view.setOnLongClickListener(if (id == 1) View.OnLongClickListener { view2 -> this.clickListener?.onItemClick(51, view2, true); true } else null)
                    barKind.toInt() == 2 -> {
                        val longs = this.longIds
                        view.setOnLongClickListener(if (longs != null && entry.index < longs.size)
                            View.OnLongClickListener { view2 -> this.clickListener?.onItemClick(longs[entry.index], view2, true); true } else null)
                    }
                    else -> view.setOnLongClickListener(View.OnLongClickListener { view2 -> this.clickListener?.onItemClick(0, view2, true); true })
                }
                if (id == 1) view.setAddrIconListener(object : AddrIconListener {
                    override fun onClick(face: View?) { this@MyBarView.clickListener?.onItemClick(1001, face, false) }
                    // a tap on the lock (BarFace calls it onLongClick): the site's security sheet (2026-11-20; it opened
                    // the address menu, which a long press on the address bar still opens)
                    override fun onLongClick(face: View?) { this@MyBarView.clickListener?.onItemClick(1002, face, false) }
                })
            }
            if (icon != null) {
                applyIconAlpha(icon, id, newColorType)
                icon.setBgPreColor(press)
            }
        }
        if (reloadInPill && loadIcon == null) {
            // no reload button in the list: the pill still gets one
            loadIcon = BarIcon().also {
                it.setVisibility(0)
                it.setImageResource(MainUtil.iconResFor(35, newColorType))
                applyIconAlpha(it, 35, newColorType)
                it.setBgPreColor(press)
            }
        }
        rects.clear()
        entries.clear()
        entries.addAll(list)
    }

    private fun imageFor(id: Int, colour: Int): Int = when {
        id == 63 -> if (starred) R.drawable.outline_kid_star_yellow_24 else MainUtil.iconResFor(63, colour)
        id == 29 -> if (starred) R.drawable.outline_kid_star_2_yellow_24 else MainUtil.iconResFor(29, colour)
        id == 64 -> if (downAlert) R.drawable.outline_download_for_offline_red_24 else MainUtil.iconResFor(64, colour)
        id == 2 -> MainUtil.moreIconFor(colour, !PdfSettings.topToBottom.value && barKind.toInt() == 1)
        else -> MainUtil.iconResFor(id, colour)
    }

    /** Alpha of a button by its id and state (the starred / downloading states show at full strength). */
    fun applyIconAlpha(icon: BarIcon?, id: Int, colour: Int) {
        if (icon == null) return
        if (id == 63 || id == 29) {
            icon.setMaxAlpha(if (starred) 1.0f else BarFace.Companion.pressScale(colour))
            return
        }
        if (id != 64) icon.setMaxAlpha(BarFace.Companion.pressScale(colour))
        else icon.setMaxAlpha(if (downAlert) 1.0f else BarFace.Companion.pressScale(colour))
    }

    /** Bookmark state of the star buttons. */
    fun applyStarred(colour: Int, newStarred: Boolean) {
        starred = newStarred
        starIcon?.let { it.setImageResource(if (newStarred) R.drawable.outline_kid_star_yellow_24 else MainUtil.iconResFor(63, colour)); applyIconAlpha(it, 63, colour) }
        star2Icon?.let { it.setImageResource(if (newStarred) R.drawable.outline_kid_star_2_yellow_24 else MainUtil.iconResFor(29, colour)); applyIconAlpha(it, 29, colour) }
    }

    /** Re-applies colours, texts and states to all buttons (page / tab / theme changed). */
    fun retint(context: Context?, host: String?, title: String?, hostColor: Int, secret: Boolean, currentTabIndex: Int, size: Int, onStartPage: Boolean, newColorType: Int, iconColor: Int) {
        val ids = itemIds ?: return
        if (ids.isEmpty() || entries.size != ids.size) return
        colorType = newColorType
        val press = themeIconColor(newColorType, iconColor)
        pressColor = press
        for (entry in entries) {
            val id = entry.id
            val view = entry.host
            val icon = entry.icon
            if (id == 1 && view != null) {
                view.setupBackground(newColorType, iconColor, onStartPage, barKind.toInt() == 1)
                view.setupHost(hostColor, newColorType, context, host, title, secret)
            } else if (id == 30 && view != null) {
                view.setupTab(currentTabIndex, size, newColorType)
            } else if (icon != null && validId(id)) {
                icon.setImageResource(imageFor(id, newColorType))
            }
            if (view != null) {
                view.setCircle(id == 1, id != 1)
                view.setBgPreColor(press)
                if (id == 1) { view.setHostPreColor(press); view.setTrnsPreColor(if (secret) press else 0) }
            }
            if (icon != null) { applyIconAlpha(icon, id, newColorType); icon.setBgPreColor(press) }
        }
        // the pill's own reload button (when it is not one of the entries)
        val pillReload = loadIcon
        if (reloadInPill && pillReload != null && entries.none { it.icon === pillReload }) {
            pillReload.setImageResource(if (iconLoading) (if (MainUtil.barInk(newColorType) == 0) R.drawable.outline_close_black_24 else R.drawable.outline_close_dark_24) else MainUtil.iconResFor(35, newColorType))
            applyIconAlpha(pillReload, 35, newColorType)
            pillReload.setBgPreColor(press)
        }
    }

    /** New image for the plain button with id [code] (colour type [colour]). */
    fun updateMoreIcon(code: Int, colour: Int) {
        for (entry in entries) {
            if (entry.id == code) {
                val icon = entry.icon ?: return
                icon.setImageResource(if (code == 2) MainUtil.moreIconFor(colour, !PdfSettings.topToBottom.value && barKind.toInt() == 1) else MainUtil.iconResFor(code, colour))
                applyIconAlpha(icon, code, colour)
                return
            }
        }
    }

    fun setFilterColor(newFilterTint: Int) { if (filterTint != newFilterTint) { filterTint = newFilterTint; filter = newFilterTint } }

    /** Reload / stop button: stop (close) icon while loading. */
    fun setIconLoad(newIconLoading: Boolean) {
        val icon = loadIcon ?: return
        val colour = colorType
        iconLoading = newIconLoading
        if (!newIconLoading) icon.setImageResource(MainUtil.iconResFor(35, colour))
        else icon.setImageResource(if (MainUtil.barInk(colour) == 0) R.drawable.outline_close_black_24 else R.drawable.outline_close_dark_24)
        applyIconAlpha(icon, 35, colour)
    }

    fun setListener(barListener: BarListener?) { clickListener = barListener }

    /** Releases the bar (the owner is going away). */
    fun release() {
        clickListener = null; itemIds = null; longIds = null; shownIds = null
        addrFace = null; backIcon = null; forwardIcon = null; tabFace = null; starIcon = null; star2Icon = null; downIcon = null; loadIcon = null
        entries.clear()
        extras.clear()
        pill = null; tabs = null
    }

    /** Index of the shown button under the screen point ([x], [y]), or -1; used for the toolbar's slide gestures. */
    fun itemAt(x: Int, index: Int): Int {
        val where = IntArray(2)
        getLocationOnScreen(where)
        for (entry in entries) {
            if (!validId(entry.id)) continue
            val shown = entry.icon?.let { it.shown == 0 } ?: entry.host?.isShown() ?: false
            if (!shown) continue
            val rect = rects[entry.index] ?: continue
            if (x >= where[0] + rect.left && x < where[0] + rect.right && index >= where[1] + rect.top && index < where[1] + rect.bottom) return entry.index
        }
        return -1
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val handled = super.dispatchTouchEvent(event)
        barTouched = handled
        return handled
    }

    @OptIn(ExperimentalFoundationApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
    @Composable
    private fun RowScope.Slot(entry: Entry) {
        val (widthPx, weight) = layoutFor(entry.id)
        val sizeModifier = if (weight > 0) Modifier.weight(weight.toFloat()) else Modifier.width(with(LocalDensity.current) { widthPx.toDp() })
        val tracked = sizeModifier.fillMaxHeight().onGloballyPositioned { rects[entry.index] = it.boundsInRoot() }
        val host = entry.host
        val icon = entry.icon
        if (host != null) {
            @Suppress("UNUSED_VARIABLE") val tick = hostTick
            // A GONE icon takes no room (it used to keep a weighted slot and pushed the other buttons inward).
            if (host.getVisibility() == View.GONE) return
            // URL pill / tab count: drawn here with Compose from the face's state; touches go to its tap-zone logic
            // (same MotionEvents as the old view got), and a plain anchor view sits underneath for popups.
            var faceTick by remember(host) { mutableIntStateOf(0) }
            DisposableEffect(host) {
                host.onRedraw = Runnable { faceTick++ }
                host.invalidate()
                onDispose {
                    host.onRedraw = null
                    host.onDetachedFromWindow()
                }
            }
            val measurer = rememberTextMeasurer()
            val hostLabel = remember(entry.id) { com.mycompany.app.ui.browser.MenuCatalog.label(context, entry.id) }
            // drawn faces have no semantics of their own: a name and a click for TalkBack / tests (2026-11-21)
            val hostSemantics = Modifier.semantics {
                contentDescription = hostLabel
                role = androidx.compose.ui.semantics.Role.Button
                onClick { host.clickListener?.onClick(host.anchor); true }
            }
            Box(tracked.onSizeChanged { host.onSizeChanged(it.width, it.height, 0, 0) }.then(hostSemantics)) {
                AndroidView(factory = { _ -> (host.anchor.parent as? ViewGroup)?.removeView(host.anchor); host.anchor }, modifier = Modifier.fillMaxSize())
                Canvas(Modifier.fillMaxSize().pointerInteropFilter { event -> host.onTouchEvent(event) }) {
                    @Suppress("UNUSED_VARIABLE") val faceTickNow = faceTick
                    if (host.faceAlpha < 1f) {
                        drawContext.canvas.saveLayer(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height),
                            androidx.compose.ui.graphics.Paint().apply { alpha = host.faceAlpha })
                        host.drawComposeFace(this, measurer)
                        drawContext.canvas.restore()
                    } else {
                        host.drawComposeFace(this, measurer)
                    }
                }
                // reload / stop at the pill's right end (where other browsers keep it); the pill keeps that room free
                val reload = if (entry.id == 1 && reloadInPill) loadIcon else null
                androidx.compose.runtime.SideEffect { host.endReserve = if (reload != null) MainApp.dp40 - MainApp.dp4 else 0 }
                if (reload != null && reload.res != 0) {
                    Box(
                        Modifier.align(Alignment.CenterEnd).padding(end = 8.dp).width(32.dp).fillMaxHeight()
                            .alpha(if (reload.enabledNow) reload.alphaMax else 0.4f)
                            .combinedClickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false, radius = 16.dp, color = Color(reload.press)),
                                enabled = reload.enabledNow,
                                onClick = { clickListener?.onItemClick(35, this@MyBarView, false) },
                            ),
                        contentAlignment = Alignment.Center,
                    ) { Image(painterResource(reload.res), null, modifier = Modifier.size(20.dp)) }
                }
            }
            return
        }
        if (icon == null || icon.shown == 8) return
        if (entry.id == 35 && reloadInPill) return // drawn inside the pill
        if (icon.shown == 4) { Box(tracked); return }
        val index = entry.index
        val id = entry.id
        val longs = longIds
        val onLong: (() -> Unit)? = when {
            barKind.toInt() == 1 -> null
            barKind.toInt() == 2 -> if (longs != null && index < longs.size) ({ clickListener?.onItemClick(longs[index], this@MyBarView, true) }) else null
            else -> ({ clickListener?.onItemClick(0, this@MyBarView, true) })
        }
        val unseenFill = id == 4 && com.mycompany.app.down.DownState.unseen.intValue.let {
            if (it < 0) com.mycompany.app.down.DownState.unseenCount(context) > 0 else it > 0
        }
        val iconLabel = remember(id) { com.mycompany.app.ui.browser.MenuCatalog.label(context, id) }
        Box(
            // the action's name (TalkBack read nothing on these buttons; 2026-11-21)
            tracked.semantics { contentDescription = iconLabel }
                .alpha(if (!icon.enabledNow) 0.4f else if (unseenFill) 1f else icon.alphaMax)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, color = Color(icon.press)),
                    enabled = icon.enabledNow,
                    onClick = {
                        if (BarGroups.isGroup(id)) openGroup = if (openGroup == index) -1 else index
                        else clickListener?.onItemClick(id, this@MyBarView, false)
                    },
                    onLongClick = onLong,
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Downloads (action 4): filled with the app's main colour while finished downloads are unseen (2026-11-20)
            if (unseenFill) {
                val dark = com.mycompany.app.ui.state.AppState.darkUi
                Box(
                    Modifier.size(38.dp).background(Color(com.mycompany.app.down.DownState.accent(context, dark)), androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    if (icon.res != 0) Image(
                        painterResource(icon.res), null,
                        colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color(com.mycompany.app.down.DownState.onAccent(context, dark))),
                    )
                }
            } else if (icon.res != 0) {
                if (BarGroups.isGroup(id)) {
                    Image(painterResource(icon.res), null, modifier = Modifier.groupNotch(inkColor()))
                } else Image(painterResource(icon.res), null)
            }
            if (BarGroups.isGroup(id) && openGroup == index) GroupPill(id)
        }
    }

    /** The colour of this bar's icons (black icons on light bars, light ones on dark bars). */
    private fun inkColor(): Color = if (MainUtil.barInk(colorType) == 0) Color(0xFF212121) else Color(0xFFF1F1F1)

    /**
     * The open group [id]: its buttons in a pill above the button (below it when the bar is in the upper half of the
     * screen), in the bar's colour, the lead button nearest the bar. A tap runs the button and closes the pill; a tap
     * outside closes it.
     */
    @Composable
    private fun GroupPill(id: Int) {
        val members = BarGroups.members(id)
        if (members.isEmpty()) { openGroup = -1; return }
        val gap = with(LocalDensity.current) { 6.dp.roundToPx() }
        val position = remember(gap) { GroupPillPosition(gap) }
        val colour = colorType
        val back = Color(when (colour) { 0 -> MyBarFrame.BAR_LIGHT; 1 -> MyBarFrame.BAR_DARK; else -> colour } or -0x1000000)
        val ink = inkColor()
        androidx.compose.ui.window.Popup(
            popupPositionProvider = position,
            onDismissRequest = { openGroup = -1 },
            properties = androidx.compose.ui.window.PopupProperties(focusable = true),
        ) {
            androidx.compose.material3.Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(26.dp), color = back, shadowElevation = 8.dp,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, ink.copy(alpha = 0.16f)),
            ) {
                androidx.compose.foundation.layout.Column(Modifier.padding(vertical = 4.dp)) {
                    // the lead nearest the bar: last when the pill opens upward
                    for (member in (if (position.above) members.reversed() else members)) {
                        Box(
                            Modifier.size(48.dp).combinedClickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false, radius = 22.dp, color = Color(pressColor)),
                                onClick = {
                                    openGroup = -1
                                    clickListener?.onItemClick(member, this@MyBarView, false)
                                },
                            ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(painterResource(MainUtil.iconResFor(member, colour)), null, modifier = Modifier.alpha(BarFace.Companion.pressScale(colour)))
                        }
                    }
                }
            }
        }
    }

    /** Places the group pill centred on its button, above it in the lower half of the window and below it otherwise. */
    private class GroupPillPosition(private val gap: Int) : androidx.compose.ui.window.PopupPositionProvider {
        var above by mutableStateOf(true)
        override fun calculatePosition(
            anchorBounds: androidx.compose.ui.unit.IntRect, windowSize: androidx.compose.ui.unit.IntSize,
            layoutDirection: androidx.compose.ui.unit.LayoutDirection, popupContentSize: androidx.compose.ui.unit.IntSize,
        ): androidx.compose.ui.unit.IntOffset {
            val up = anchorBounds.center.y > windowSize.height / 2
            if (up != above) above = up
            val x = (anchorBounds.center.x - popupContentSize.width / 2).coerceIn(0, (windowSize.width - popupContentSize.width).coerceAtLeast(0))
            val y = if (up) anchorBounds.top - popupContentSize.height - gap else anchorBounds.bottom + gap
            return androidx.compose.ui.unit.IntOffset(x, y.coerceIn(0, (windowSize.height - popupContentSize.height).coerceAtLeast(0)))
        }
    }

    @Composable
    private fun Bar() {
        Box(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxSize().onSizeChanged { size ->
                    if (hasAddress) {
                        val needed = ((itemIds?.size ?: 0) + 3) * MainApp.dp48 > size.width
                        if (needed != tightFit) { tightFit = needed; compact = needed }
                    }
                },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                @Suppress("UNUSED_EXPRESSION") compact
                for (entry in entries) Slot(entry)
                @Suppress("UNUSED_VARIABLE") val tick = hostTick
                for (view in extras) {
                    // A GONE indicator takes no room (it kept its width and pushed the buttons inward).
                    if (view.visibility == View.GONE) continue
                    // without the URL pill every button, indicators included, gets the same share of the width, so the
                    // spacing stays even for any number of buttons
                    AndroidView(
                        factory = { _ -> (view.parent as? ViewGroup)?.removeView(view); view },
                        modifier = (if (hasAddress) Modifier.width(with(LocalDensity.current) { MainApp.dp48.toDp() }) else Modifier.weight(1f)).fillMaxHeight(),
                    )
                }
            }
            if (filter != 0) Box(Modifier.fillMaxSize().background(Color(filter)))
        }
    }

    companion object {

        @JvmStatic
        fun splitWidth(width: Int, first: Boolean): Int {
            val found: Boolean
            val topItems = parseIntList(MainSettings.topItems.value)
            var length = if (topItems != null) topItems.size else 0
            if (length == 0) {
                return -1
            }
            val bottomItems = parseIntList(MainSettings.botItems.value)
            var bottomCount = if (bottomItems != null) bottomItems.size else 0
            if (bottomCount == 0) {
                return -1
            }
            var itemIndex = 0
            while (true) {
                if (itemIndex >= length) {
                    found = false
                    break
                }
                if (topItems!![itemIndex] == 1) {
                    length += 3
                    found = true
                    break
                }
                itemIndex++
            }
            if (!found && bottomCount != 0) {
                var index = 0
                while (index < bottomCount) {
                    if (bottomItems!![index] == 1) {
                        bottomCount += 3
                        break
                    }
                    index++
                }
            }
            val firstWidth = ((width / (bottomCount + length)) * length)
            return if (first) firstWidth else width - firstWidth
        }
    }
}
