package com.mycompany.app.view

import androidx.compose.material3.TextButton
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainListLoader.ListLoadListener
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.ui.browser.ComposeOverlay
import com.mycompany.app.web.WebNestFrame

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.coordinatorlayout.widget.CoordinatorLayout
import kotlin.math.abs
import kotlin.math.max

/**
 * The browser's bottom notice bar (save result, "undelete", "pop-up blocked", "open in app"). Compose now: this view is
 * a thin shell (it keeps the old constructor, members and the nested [SnackItem] / [SnackbarListener], and still sits in
 * the page layout with the app-bar following behaviour) around one Compose bar. Kinds ([snackType]): 0 text + icon buttons,
 * 1 text, 2 text + text button, 3 "open in app" (app icon and name), 4 pop-up blocked (site icon and name), 5 text +
 * two text buttons (a longer hint, e.g. a page whose scrolling is locked by a hidden dialog). A bar
 * hides itself after 3 s (kinds 1, 2) or 5 s, stays while touched, and is swiped away sideways or downwards; the first
 * one ever shown carries a short guide ([ZoneSettings.snkGuide.value]).
 */
@Suppress("deprecation")
class MySnackbar(context: Context?) : ComposeOverlay(context!!) {
    class SnackItem {
        @JvmField var siteUrl: String? = null
        @JvmField var url: String? = null
        @JvmField var intent: Intent? = null
        @JvmField var pkg: String? = null
        @JvmField var pendingUrl: String? = null
        @JvmField var frame: WebNestFrame? = null
    }

    interface SnackbarListener {
        fun onSecond()
        fun onDismissed()
        fun onFirst()
        fun onDismiss()
    }

    /** The bar kind, see the class comment. */
    @JvmField var snackType: Byte = 0
    /** Address for kinds 3 and 4. */
    @JvmField var siteUrl: String? = null
    /** Shows a dot on the second button. */
    @JvmField var secondActive: Boolean = false
    /** Bottom margin the app-bar behaviour measures from. */
    @JvmField var bottomShift: Int = 0
    /** True while a finger is on the bar (the activity checks it, and clears it when it takes over touches). */
    @JvmField var hintShown: Boolean = false

    private var activity: MainActivity? = null
    private var appBar: com.mycompany.app.ui.browser.BarScroll? = null
    /** Moves the bar down with the bottom bar while the page scrolls (was MyBehaviorSnack on the AppBarLayout). */
    private var follow: com.mycompany.app.ui.browser.BarScroll.OffsetListener? = null
    /** Set while the bar lives in the browser's Compose overlay stack instead of its host view. */
    private var overlays: com.mycompany.app.ui.browser.OverlayStack? = null
    private var host: ViewGroup? = null
    private var listener: SnackbarListener? = null
    private var messageRes = 0
    private var buttonOne = 0
    private var buttonTwo = 0
    private var item: SnackItem? = null
    private var host2: String? = null
    private var loader: MainListLoader? = null
    private var alive = true
    private var pendingFrame: WebNestFrame? = null

    // state the bar draws
    private var iconBitmap by mutableStateOf<Bitmap?>(null)
    private var nameText by mutableStateOf("")
    private var guide by mutableStateOf(false)
    private var hideRequest by mutableIntStateOf(0)

    var snackItem: SnackItem?
        get() { val value = item; item = null; return value }
        set(value) { item = value }

    private val botMargin: Int
        get() = if (appBar == null) MainApp.dp48 else max(MainApp.dp48, MainUtil.bottomInsetTotal())

    init {
        setPage({ AppState.darkUi }) { Bar() }
    }

    fun setActivity(mainActivity: MainActivity?) { activity = mainActivity }

    fun setAppBarView(appBarLayout: com.mycompany.app.ui.browser.BarScroll?) { appBar = appBarLayout }

    /** Closes the bar: [animated] slides it down, otherwise it is gone at once. */
    fun hide(animated: Boolean) {
        listener = null
        if (animated) hideRequest++ else finish()
    }

    fun showSingle(viewGroup: ViewGroup?, saveFail: Int, snackbarListener: SnackbarListener?) {
        snackType = 1
        show(viewGroup, saveFail, 0, 0, snackbarListener)
    }

    fun showWithButton(viewGroup: ViewGroup?, saveSuccess: Int, onApply: Int, snackbarListener: SnackbarListener?) {
        snackType = 0
        show(viewGroup, saveSuccess, onApply, 0, snackbarListener)
    }

    fun show(viewGroup: ViewGroup?, newMessage: Int, newButtonOne: Int, newButtonTwo: Int, snackbarListener: SnackbarListener?) {
        if (viewGroup == null || host != null) return
        host = viewGroup
        listener = snackbarListener
        messageRes = newMessage
        buttonOne = newButtonOne
        buttonTwo = newButtonTwo
        if (snackType.toInt() == 3 || snackType.toInt() == 4) prepareSite()
        viewGroup.post { attach() }
    }

    /** First-use guide done: it is not shown again. */
    private fun guideDone() {
        if (ZoneSettings.snkGuide.value) {
            ZoneSettings.edit(context) { set(ZoneSettings.snkGuide, false) }
        }
    }

    private fun attach() {
        val parent = host ?: return
        try {
            val margin = botMargin
            bottomShift = margin
            setPaddingRelative(0, 0, 0, if (snackType.toInt() == 3 || snackType.toInt() == 4) margin else margin + MainApp.dp8)
            val stack = (parent as? MyWebCoord)?.let { (context as? com.mycompany.app.web.WebViewActivity)?.floatingState?.overlays }
            if (stack != null) {
                // On the browser screen the bar is a slot of the Compose overlay stack (coordinator band).
                val params = com.mycompany.app.ui.browser.OverlayParams(-1, -2)
                params.gravity = 80
                overlays = stack
                stack.addCoordView(this, params)
                val bars = appBar
                if (bars != null && !BrowserSettings.fixBot.value) {
                    val shift = bottomShift
                    val listener = com.mycompany.app.ui.browser.BarScroll.OffsetListener { top ->
                        val range = bars.getHeight()
                        params.shiftY = if (range > 0) (top.toFloat() / range) * -shift else 0f
                        stack.relayout(this)
                    }
                    follow = listener
                    bars.offsetListeners.add(listener)
                    listener.onOffsetChanged(bars.getTop())
                }
            } else if (parent is CoordinatorLayout) {
                val params = CoordinatorLayout.LayoutParams(-1, -2)
                params.gravity = 80
                parent.addView(this, params)
                val bars = appBar
                if (bars != null && !BrowserSettings.fixBot.value) {
                    val shift = bottomShift
                    val listener = com.mycompany.app.ui.browser.BarScroll.OffsetListener { top ->
                        val range = bars.getHeight()
                        translationY = if (range > 0) (top.toFloat() / range) * -shift else 0f
                    }
                    follow = listener
                    bars.offsetListeners.add(listener)
                    listener.onOffsetChanged(bars.getTop())
                }
            } else if (parent is FrameLayout) {
                val params = FrameLayout.LayoutParams(-1, -2)
                params.gravity = 80
                parent.addView(this, params)
            } else if (parent is RelativeLayout) {
                val params = RelativeLayout.LayoutParams(-1, -2)
                params.addRule(12)
                parent.addView(this, params)
            }
        } catch (unused: Exception) {
        }
    }

    /** Everything is over: tell the listener, give back a pending page frame and leave the layout. */
    private fun finish() {
        if (!alive) return
        alive = false
        val pending = item
        if (pending != null) {
            item = null
            val frame = pending.frame
            if (frame != null && frame.alive) {
                if (host == null) {
                    frame.destroyFrame(null)
                } else {
                    frame.setValid(false)
                    pendingFrame = frame
                    host!!.post {
                        val pendingFrameNow = pendingFrame
                        pendingFrame = null
                        pendingFrameNow?.destroyFrame(null)
                    }
                }
            }
        }
        listener?.onDismiss()
        listener = null
        loader?.release()
        loader = null
        activity = null
        follow?.let { appBar?.offsetListeners?.remove(it) }
        follow = null
        appBar = null
        overlays?.removeView(this)
        overlays = null
        try { host?.removeView(this) } catch (unused: Exception) { }
        host = null
    }

    // --- site / app name and icon for kinds 3 and 4 ---

    private fun prepareSite() {
        val address = MainUtil.googleRedirectTarget(siteUrl)
        siteUrl = address
        var name = MainUtil.stripHostPrefix(MainUtil.hostOfUrl(address!!, true))
        host2 = name
        if (TextUtils.isEmpty(name)) name = address
        nameText = name ?: ""
        if (snackType.toInt() == 3) {
            // the app that would open the link: its name and icon arrive from a worker
            activity?.runInBackground { findApp() }
        } else if (!TextUtils.isEmpty(address)) {
            val cached = MainUtil.cachedIcon(host2)
            if (MainUtil.isBitmapOk(cached)) {
                iconBitmap = cached
            } else {
                val child = ChildItem()
                child.listType = 18
                child.itemType = 11.toByte()
                child.path = address
                val target = ImageView(context)
                target.setTag(0)
                val mainListLoader = MainListLoader(context, false, object : ListLoadListener {
                    override fun onFailed(childItem: ChildItem?, view: View?) {}
                    override fun onLoaded(childItem: ChildItem?, view: View?, bitmap: Bitmap?) {
                        if (MainUtil.isBitmapOk(bitmap)) iconBitmap = bitmap
                    }
                })
                loader = mainListLoader
                mainListLoader.enqueue(child, target)
            }
        }
    }

    /** Finds the app that would open the link: its name and icon for the "open in app" bar. */
    private fun findApp() {
        val act = activity ?: return
        val pm = act.getPackageManager() ?: return
        val url = siteUrl
        var pkg: String? = null
        if (!TextUtils.isEmpty(url)) {
            pkg = playStorePackage(pm, url)
            if (TextUtils.isEmpty(pkg)) {
                val flags = 268436992
                try {
                    val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
                    intent.addCategory("android.intent.category.BROWSABLE")
                    intent.setFlags(flags)
                    intent.setComponent(null)
                    intent.setSelector(null)
                    pkg = defaultPackage(pm, intent)
                } catch (unused: Exception) {
                    pkg = null
                }
                if (TextUtils.isEmpty(pkg)) {
                    try {
                        val view = Intent("android.intent.action.VIEW", Uri.parse(url))
                        view.addCategory("android.intent.category.BROWSABLE")
                        view.setFlags(flags)
                        pkg = defaultPackage(pm, view)
                    } catch (unused: Exception) {
                        pkg = null
                    }
                    if (TextUtils.isEmpty(pkg)) pkg = null
                }
            }
        }
        var label: CharSequence? = null
        var icon: Bitmap? = null
        if (!TextUtils.isEmpty(pkg)) {
            try {
                label = pm.getApplicationLabel(pm.getApplicationInfo(pkg!!, 128))
            } catch (unused: Exception) {
                label = null
            }
            val key = com.mycompany.app.glide.ImageCache.key(2, pkg)
            var cached = com.mycompany.app.glide.ImageCache.get(key)
            if (MainUtil.isBitmapOk(cached)) {
                icon = cached
            } else {
                try {
                    cached = MainUtil.drawableToBitmap(MainApp.dp40 / 2, pm.getApplicationIcon(pkg!!))
                } catch (unused: Exception) {
                }
                if (MainUtil.isBitmapOk(cached)) {
                    com.mycompany.app.glide.ImageCache.put(key, cached)
                    icon = cached
                }
            }
        }
        if (!MainUtil.isBitmapOk(icon)) icon = MainUtil.cachedIcon(host2)
        val labelFinal = label
        val iconFinal = icon
        host?.post {
            if (!alive) return@post
            if (MainUtil.isBitmapOk(iconFinal)) iconBitmap = iconFinal
            if (!TextUtils.isEmpty(labelFinal)) nameText = labelFinal.toString()
        }
    }

    private fun playStorePackage(pm: PackageManager?, url: String?): String? {
        if (!url!!.startsWith("https://play.google.com/store/apps/details?id=")) return null
        try {
            val amp = url.indexOf(38.toChar(), 46)
            if (amp <= 46 || amp >= url.length) return null
            val id = url.substring(46, amp)
            if (TextUtils.isEmpty(id)) return null
            return if (pm!!.getLaunchIntentForPackage(id) == null) "com.android.vending" else id
        } catch (unused: Exception) {
            return null
        }
    }

    // --- the bar ---

    private fun drawableBitmap(res: Int, sizePx: Int): ImageBitmap? {
        if (res == 0) return null
        return try {
            ContextCompat.getDrawable(context, res)?.toBitmap(sizePx, sizePx)?.asImageBitmap()
        } catch (unused: Exception) {
            null
        }
    }

    @Composable
    private fun Bar() {
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        val dark = AppState.darkUi
        val slide = remember { Animatable(MainApp.dp48.toFloat()) }
        val dragX = remember { Animatable(0f) }
        val dragY = remember { Animatable(0f) }
        var shown by remember { mutableStateOf(false) }
        var leaving by remember { mutableStateOf(false) }
        var pressed by remember { mutableStateOf(false) }
        var dragging by remember { mutableStateOf(false) }
        var restart by remember { mutableIntStateOf(0) }
        var barHeight by remember { mutableIntStateOf(0) }
        val type = snackType.toInt()
        val reach = MainApp.dp64.toFloat().coerceAtLeast(1f)

        LaunchedEffect(Unit) {
            slide.animateTo(0f, tween(180))
            shown = true
            if (ZoneSettings.snkGuide.value) guide = true
        }
        // hide itself after a while, but not while touched
        LaunchedEffect(shown, pressed, dragging, guide, restart) {
            if (!shown || pressed || dragging || leaving) return@LaunchedEffect
            delay(if (ZoneSettings.snkGuide.value || guide) 15000L else if (type == 1 || type == 2) 3000L else if (type == 5) 8000L else 5000L)
            if (leaving) return@LaunchedEffect
            leaving = true
            slide.animateTo(MainApp.dp48.toFloat(), tween(180))
            finish()
        }
        // closed from outside (animated)
        val hiding = hideRequest > 0
        LaunchedEffect(hiding) {
            if (!hiding || leaving) return@LaunchedEffect
            leaving = true
            guide = false
            slide.animateTo(MainApp.dp48.toFloat(), tween(180))
            finish()
        }

        fun press(second: Boolean) {
            if (dragging || leaving) return
            if (second) listener?.onSecond() else listener?.onFirst()
            guideDone()
            hideRequest++
        }

        val surfaceColor = if (dark) Color(-14606047) else Color.White
        val textColor = if (dark) Color(-328966) else Color.Black
        // the first-use hint in the tip look (theme inverse colours) instead of fixed green (2026-11-20)
        val guideColor = MaterialTheme.colorScheme.inverseSurface
        val guideInk = MaterialTheme.colorScheme.inverseOnSurface
        val corner = with(density) { MainApp.dp8.toDp() }

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
            Column(
                Modifier.fillMaxWidth(if (type == 2) 0.7f else 0.75f)
                    .graphicsLayer {
                        translationX = dragX.value
                        translationY = slide.value + dragY.value
                        alpha = (1f - (abs(dragX.value) + abs(dragY.value)) / reach).coerceIn(0f, 1f)
                    }
                    .onSizeChanged { barHeight = it.height }
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                            pressed = true
                            hintShown = true
                            var axis = 0 // 1 vertical, 2 horizontal
                            val slop = MainApp.dp4.toFloat()
                            var last = Offset.Zero
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                if (!change.pressed) break
                                val delta = change.position - down.position
                                if (axis == 0) {
                                    if (abs(delta.x) > abs(delta.y)) { if (abs(delta.x) >= slop) axis = 2 } else if (abs(delta.y) >= slop) axis = 1
                                }
                                if (axis != 0 && !leaving) {
                                    change.consume()
                                    dragging = true
                                    last = delta
                                    scope.launch { if (axis == 2) dragX.snapTo(delta.x) else dragY.snapTo(delta.y) }
                                }
                            }
                            pressed = false
                            hintShown = false
                            if (axis != 0 && !leaving) {
                                val far = if (axis == 2) abs(last.x) >= MainApp.dp48 else abs(last.y) >= barHeight / 2f
                                scope.launch {
                                    if (far) {
                                        leaving = true
                                        guide = false
                                        listener?.onDismissed()
                                        val toX = if (axis == 2) (if (last.x < 0) -size.width.toFloat() else size.width.toFloat()) else 0f
                                        val toY = if (axis == 1) (if (last.y < 0) -barHeight.toFloat() else barHeight.toFloat()) else 0f
                                        if (axis == 2) dragX.animateTo(toX, tween(180)) else dragY.animateTo(toY, tween(180))
                                        finish()
                                    } else {
                                        if (axis == 2) dragX.animateTo(0f, tween(180)) else dragY.animateTo(0f, tween(180))
                                        dragging = false
                                    }
                                }
                            } else {
                                dragging = false
                            }
                        }
                    },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (guide) {
                    Guide(guideColor, guideInk, corner) { guideDone(); guide = false; restart++ }
                }
                Surface(
                    shape = RoundedCornerShape(corner), color = surfaceColor,
                    border = BorderStroke(if (guide) 2.dp else 1.dp, if (guide) guideColor else Color(-9079435)),
                ) {
                    when (type) {
                        1 -> Text(
                            stringResource(messageRes), color = textColor, fontSize = 14.sp, textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().heightIn(min = with(density) { MainApp.dp48.toDp() }).padding(with(density) { MainApp.dp8.toDp() }),
                        )
                        2 -> Box(
                            Modifier.fillMaxWidth().height(with(density) { MainApp.dp48.toDp() }).clickable { press(false) },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(stringResource(buttonOne), color = if (dark) Color(-328966) else Color(-14784824), fontSize = 14.sp)
                        }
                        3, 4 -> SiteBar(textColor, dark, density, ::press)
                        5 -> Column(Modifier.fillMaxWidth().padding(start = with(density) { MainApp.dp16.toDp() }, top = with(density) { MainApp.dp8.toDp() }, end = with(density) { MainApp.dp8.toDp() })) {
                            Text(stringResource(messageRes), color = textColor, fontSize = 14.sp, modifier = Modifier.padding(top = with(density) { MainApp.dp8.toDp() }, end = with(density) { MainApp.dp8.toDp() }))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                val accent = if (dark) Color(-328966) else Color(-14784824)
                                if (buttonTwo != 0) TextButton(onClick = { press(true) }) { Text(stringResource(buttonTwo), color = accent, fontSize = 14.sp) }
                                if (buttonOne != 0) TextButton(onClick = { press(false) }) { Text(stringResource(buttonOne), color = accent, fontSize = 14.sp) }
                            }
                        }
                        else -> Row(Modifier.fillMaxWidth().padding(horizontal = with(density) { MainApp.dp8.toDp() }), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                stringResource(messageRes), color = textColor, fontSize = 14.sp,
                                modifier = Modifier.weight(1f).heightIn(min = with(density) { MainApp.dp48.toDp() })
                                    .padding(start = with(density) { MainApp.dp16.toDp() }, top = with(density) { MainApp.dp8.toDp() }, end = with(density) { MainApp.dp8.toDp() }, bottom = with(density) { MainApp.dp8.toDp() }),
                            )
                            if (buttonOne != 0) IconBtn(buttonOne, dark, false, density) { press(false) }
                            if (buttonTwo != 0) IconBtn(buttonTwo, dark, secondActive, density) { press(true) }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun IconBtn(kindIcon: Int, dark: Boolean, dot: Boolean, density: androidx.compose.ui.unit.Density, onClick: () -> Unit) {
        val side = with(density) { MainApp.dp48.toDp() }
        val bitmap = remember(kindIcon, dark) { drawableBitmap(iconFor(kindIcon), MainApp.dp48 / 2) }
        Box(Modifier.size(side).clip(CircleShape).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
            if (bitmap != null) Image(bitmap, contentDescription = null, modifier = Modifier.size(side / 2))
            if (dot) Box(Modifier.align(Alignment.TopEnd).padding(10.dp).size(8.dp).clip(CircleShape).background(Color(-65536)))
        }
    }

    @Composable
    private fun SiteBar(textColor: Color, dark: Boolean, density: androidx.compose.ui.unit.Density, press: (Boolean) -> Unit) {
        val avatar = with(density) { MainApp.dp40.toDp() }
        val blockIcon = remember(dark) { drawableBitmap(if (dark) AppR.drawable.outline_block_dark_24 else AppR.drawable.outline_block_black_24, MainApp.dp16 + MainApp.dp4) }
        val fallback = remember(dark) { drawableBitmap(if (snackType.toInt() == 3) { if (dark) AppR.drawable.outline_android_dark_24 else AppR.drawable.outline_android_black_24 } else { if (dark) AppR.drawable.outline_public_dark_24 else AppR.drawable.outline_public_black_24 }, MainApp.dp40) }
        Column {
            Row(Modifier.fillMaxWidth().height(36.dp).padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                if (blockIcon != null) Image(blockIcon, contentDescription = null, modifier = Modifier.size(24.dp))
                Text(stringResource(messageRes), color = textColor, fontSize = 14.sp, modifier = Modifier.padding(start = 14.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(-9079435).copy(alpha = 0.5f)))
            Row(Modifier.fillMaxWidth().padding(horizontal = with(density) { MainApp.dp8.toDp() }), verticalAlignment = Alignment.CenterVertically) {
                val shownIcon = iconBitmap?.asImageBitmap() ?: fallback
                if (shownIcon != null) {
                    Image(shownIcon, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(avatar).clip(CircleShape))
                } else {
                    Box(Modifier.size(avatar))
                }
                Text(
                    nameText, color = textColor, fontSize = 14.sp, maxLines = 2, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).heightIn(min = with(density) { MainApp.dp48.toDp() }).padding(with(density) { MainApp.dp8.toDp() }),
                )
                if (buttonOne != 0) IconBtn(buttonOne, dark, false, density) { press(false) }
                if (buttonTwo != 0) IconBtn(buttonTwo, dark, secondActive, density) { press(true) }
            }
        }
    }

    /** The first-use hint above the bar: swipe to remove, with a pointer down to the bar. */
    @Composable
    private fun Guide(color: Color, ink: Color, corner: androidx.compose.ui.unit.Dp, onClose: () -> Unit) {
        val swipeH = remember { drawableBitmap(AppR.drawable.outline_swipe_white_24, 96) }
        val swipeV = remember { drawableBitmap(AppR.drawable.outline_swipe_vertical_white_24, 96) }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(shape = RoundedCornerShape(corner), color = color) {
                Box {
                    Column(Modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                            if (swipeH != null) Image(swipeH, contentDescription = null, modifier = Modifier.size(36.dp), colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(ink))
                            if (swipeV != null) Image(swipeV, contentDescription = null, modifier = Modifier.size(36.dp), colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(ink))
                        }
                        Text(
                            stringResource(AppR.string.swipe_remove) + "\n\n" + stringResource(AppR.string.snack_guide),
                            color = ink, fontSize = 14.sp, textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                    Icon(
                        Icons.Filled.Close, contentDescription = null, tint = ink,
                        modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(40.dp).clip(CircleShape).clickable(onClick = onClose).padding(8.dp),
                    )
                }
            }
            Canvas(Modifier.size(width = 14.dp, height = 8.dp)) {
                val path = Path().apply { moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(size.width / 2f, size.height); close() }
                drawPath(path, color)
            }
        }
    }

    companion object {

        @JvmStatic
        fun iconFor(type: Int): Int {
            if (AppState.darkUi) {
                return when (type) {
                    1 -> AppR.drawable.outline_open_in_new_dark_24
                    2 -> AppR.drawable.outline_folder_dark_24
                    3 -> AppR.drawable.outline_menu_dark_24
                    4 -> AppR.drawable.outline_add_box_dark_24
                    5 -> AppR.drawable.outline_refresh_dark_24
                    else -> 0
                }
            }
            return when (type) {
                1 -> AppR.drawable.outline_open_in_new_black_24
                2 -> AppR.drawable.outline_folder_black_24
                3 -> AppR.drawable.outline_menu_black_24
                4 -> AppR.drawable.outline_add_box_black_24
                5 -> AppR.drawable.outline_refresh_black_24
                else -> 0
            }
        }

        @JvmStatic
        fun packageOf(resolveInfo: ResolveInfo?): String? {
            val activityInfo: ActivityInfo? = if (resolveInfo == null) null else resolveInfo.activityInfo
            if (resolveInfo == null || activityInfo == null) return null
            val packageName = activityInfo.packageName
            if (TextUtils.isEmpty(packageName) || packageName == "android" || packageName == "ch.opumlabs.opumbrowser") return null
            return packageName
        }

        @JvmStatic
        fun defaultPackage(packageManager: PackageManager?, intent: Intent?): String? {
            try {
                val found: String? = packageOf(
                    if (Build.VERSION.SDK_INT >= 33) packageManager!!.resolveActivity(intent!!, PackageManager.ResolveInfoFlags.of(0L))
                    else packageManager!!.resolveActivity(intent!!, 0),
                )
                if (!TextUtils.isEmpty(found)) return found
            } catch (unused: Exception) {
            }
            try {
                val list = packageManager!!.queryIntentActivities(intent!!, 0)
                if (!list.isEmpty()) {
                    for (info in list) {
                        val name: String? = packageOf(info)
                        if (!TextUtils.isEmpty(name)) return name
                    }
                }
            } catch (unused2: Exception) {
            }
            if (TextUtils.isEmpty(intent!!.getPackage())) return null
            return "com.android.vending"
        }
    }
}
