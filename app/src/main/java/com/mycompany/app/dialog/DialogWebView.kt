package com.mycompany.app.dialog

import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import ch.opumlabs.opumbrowser.R
import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.text.TextUtils
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.webkit.ConsoleMessage
import android.webkit.DownloadListener
import android.webkit.JavascriptInterface
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.RenderProcessGoneDetail
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.mycompany.app.data.DataTrans
import com.mycompany.app.data.book.DataBookAds
import com.mycompany.app.dialog.DialogEditArea.EditAreaListener
import com.mycompany.app.dialog.DialogGuideArea.DialogAreaListener
import com.mycompany.app.dialog.DialogTransLang.TransLangListener
import com.mycompany.app.dialog.DialogTransLang.TransNotiListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUri.volumeLabel
import com.mycompany.app.main.MainUtil.areaMultiJs
import com.mycompany.app.main.MainUtil.isHttpUrl
import com.mycompany.app.main.MainUtil.areaUpScript
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.chromeVersion
import com.mycompany.app.main.MainUtil.destroyWebView
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.evalConfirmJs
import com.mycompany.app.main.MainUtil.hostOfUrl
import com.mycompany.app.main.MainUtil.langPrefix
import com.mycompany.app.main.MainUtil.evalLongPressJs
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.main.MainUtil.transLangFromCookie
import com.mycompany.app.main.MainUtil.checkTransClassJs
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainUtil.classSelectorJs
import com.mycompany.app.main.MainUtil.injectTransJs
import com.mycompany.app.main.MainUtil.checkTransJs
import com.mycompany.app.main.MainUtil.transScriptJs
import com.mycompany.app.main.MainUtil.parseTransResult
import com.mycompany.app.main.MainUtil.isRtl
import com.mycompany.app.main.MainUtil.setUserAgent
import com.mycompany.app.main.MainUtil.editTextValue
import com.mycompany.app.main.MainUtil.stripQuotes
import com.mycompany.app.main.MainUtil.setAlgorithmicDark
import com.mycompany.app.main.MainUtil.closeKeyboard
import com.mycompany.app.main.MainUtil.trimSpaceSlash
import com.mycompany.app.main.MainUtil.hideDivsByClass
import com.mycompany.app.main.MainUtil.hasKnownScheme
import com.mycompany.app.main.MainUtil.intentToUrl
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.unescapeJavaText
import com.mycompany.app.main.MainUtil.isYouTubeShorts
import com.mycompany.app.main.MainUtil.isYouTubeUrl
import com.mycompany.app.main.MainUtil.isYouTubeWatch
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.normalizeUrl
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.main.MainUtil.compareLongs
import com.mycompany.app.main.MainUtil.stripHostPrefix
import com.mycompany.app.main.MainUtil.youTubeIdOf
import com.mycompany.app.main.MainUtil.userFontResponse
import com.mycompany.app.main.MainUtil.openIntentUri
import com.mycompany.app.main.MainUtil.areaScriptJs
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefAlbum.Companion.saveTrans
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.ui.browser.DialogFrame
import com.mycompany.app.view.MyPopupMenu
import com.mycompany.app.view.MyPopupMenu.MyPopupListener
import com.mycompany.app.view.MyProgressBar
import com.mycompany.app.view.MyScrollBar
import com.mycompany.app.ui.browser.ScrollNavi
import com.mycompany.app.web.WebClean
import com.mycompany.app.web.WebNestView
import com.mycompany.app.web.WebNestView.WebViewListener
import com.mycompany.app.web.WebTransControl
import com.mycompany.app.web.WebTransControl.TransCtrlListener
import com.mycompany.app.wview.WebAreaView
import com.mycompany.app.wview.WebAreaView.WebAreaListener
import com.mycompany.app.wview.WebUpView
import com.mycompany.app.wview.WebUpView.UpViewListener
import java.net.URLDecoder
import kotlin.math.abs
import kotlin.math.max
import android.os.Looper
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.draw.clip
import com.mycompany.app.web.deleteSiteCookies
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * The in-app web sheet (pages opened from the browser: link preview, license, ad-filter / script pages, area blocking).
 * A tall sheet with the address field (Go loads it), Google-translate button (with its busy ring), refresh / stop,
 * the page, and Close / New URL / menu (translate automatically, open in browser / new tab / ..., block area).
 * The page itself is the old code: the `WebNestView` with its clients, the translate and block-area scripts and
 * controls, the scroll bar, the swipe-back / forward arrows, the go-to-top button and the progress line are the old
 * views inside one `AndroidView`; only the bars are Compose (on [ComposeBottomDialog]).
 * Same constructor, shows itself. [urlMode] 1 = image / popup menu, 2 = no menu button, 3 = license (fixed title, no New
 * URL), 4 = no New URL.
 */
@Suppress("deprecation")
class DialogWebView(
    mainActivity: android.app.Activity?,
    pageUrlParam: String?,
    referer: String?,
    external: Boolean,
    urlModeParam: Int,
    dialogWebListener: DialogWebListener?
) : ComposeBottomDialog(mainActivity) {
    /** Stand-in for the old refresh / stop image buttons (only their visibility mattered). */
    inner class Vis(init: Int) {
        var vis by mutableIntStateOf(init)
        fun setVisibility(newVis: Int) { vis = newVis }
        fun getVisibility(): Int = vis
        fun refresh() {}
    }

    /** Stand-in for the old busy ring over the translate button. */
    inner class Cover {
        var vis by mutableIntStateOf(8)
        fun setVisibility(newVis: Int) { vis = newVis }
        fun getVisibility(): Int = vis
        fun hide() { vis = 8 }
        fun post(r: Runnable?): Boolean = r != null && mainHandler?.post(r) == true
    }

    /** Stand-in for the old address field. */
    inner class Edit {
        var text by mutableStateOf("")
        var focused = false
        fun setText(retry: CharSequence?) { text = retry?.toString() ?: "" }
        fun setText(res: Int) { text = appCtx?.getString(res) ?: "" }
        fun isFocused(): Boolean = focused
        fun clearFocus() { focusManager?.clearFocus() }
        fun str(): String = text
    }

    @JvmField val mainHandler: Handler? = Handler(Looper.getMainLooper())
    @JvmField var menuTransControl: WebTransControl? = null
    @JvmField var customView: View? = null
    @JvmField var gestureDetect: GestureDetector? = null
    @JvmField var pendingAction: Boolean = false
    @JvmField var clickedOnce: Boolean = false
    @JvmField var transRunning: Boolean = false
    @JvmField var touchX: Float = 0f
    @JvmField var pendingJs: String? = null
    @JvmField var touchY: Float = 0f
    @JvmField var lastY: Float = 0f
    @JvmField var upViewShown: Boolean = false
    @JvmField var swipeState: Byte = 0
    @JvmField var swipeDir: Byte = 0
    @JvmField var swipeEdge: Byte = 0
    @JvmField var swipeMoved: Boolean = false
    @JvmField var moveCountX: Int = 0
    @JvmField var moveCountY: Int = 0
    @JvmField var swipeStarted: Boolean = false
    @JvmField var moreMenu: MyPopupMenu? = null
    @JvmField var loadPending: Boolean = false
    @JvmField var areaText: String? = null
    @JvmField var areaPreviewJs: String? = null
    @JvmField var loadCover: Cover? = Cover()
    @JvmField var badgeView: View? = null
    @JvmField var touchSlop: Int = 0
    @JvmField var transControl: WebTransControl? = null
    @JvmField var hostActivity: android.app.Activity? = null
    @JvmField var transFrame: FrameLayout? = null
    @JvmField var appCtx: Context? = null
    @JvmField var confirmPending: Boolean = false
    @JvmField var webListener: DialogWebListener? = null
    @JvmField var transState: Byte = 0
    @JvmField var layoutRtl: Boolean = false
    @JvmField var transOn: Boolean = false
    @JvmField var urlMode: Byte = 0
    @JvmField var transLang: String? = null
    @JvmField var sheetShown: Boolean = false
    @JvmField var transPick: String? = null
    @JvmField var pageUrl: String? = null
    @JvmField var detectedLang: String? = null
    @JvmField var pageHost: String? = null
    @JvmField var scrollBase: Int = 0
    @JvmField var referer: String? = null
    @JvmField var scrollShift: Int = 0
    @JvmField var prevUrl: String? = null
    @JvmField var transLangDialog: DialogTransLang? = null
    @JvmField var urlBlocked: Boolean = false
    @JvmField var notiListener: TransNotiListener? = null
    @JvmField var historyUrl: String? = null
    @JvmField var cleanEngine: WebClean? = null
    @JvmField var dialogFrame: DialogFrame? = null
    @JvmField var areaView: WebAreaView? = null
    @JvmField var areaTouched: Boolean = false
    @JvmField var firstLoad: Boolean = false
    @JvmField var areaJs: String? = null
    @JvmField var areaJsA: String? = null
    @JvmField var areaJsB: String? = null
    @JvmField var areaGuideDialog: DialogGuideArea? = null
    @JvmField var urlEdit: Edit? = Edit()
    @JvmField var transGuideDialog: DialogGuideArea? = null
    @JvmField var progressVis: Vis? = Vis(0)
    @JvmField var editAreaDialog: DialogEditArea? = null
    @JvmField var stopVis: Vis? = Vis(8)
    @JvmField var dismissed: Boolean = false
    @JvmField var webFrameView: FrameLayout? = null
    @JvmField var finishHandled: Boolean = false
    @JvmField var page: WebNestView? = null
    @JvmField var lastFinishedUrl: String? = null
    @JvmField var progressBar: MyProgressBar? = null
    @JvmField var progressValue: Int = 0
    @JvmField var scrollBarView: MyScrollBar? = null
    @JvmField var progressRunnable: Runnable? = null
    @JvmField var upView: WebUpView? = null
    @JvmField var scrollLocked: Boolean = false
    @JvmField var naviLeft: ScrollNavi? = null
    @JvmField var scrollRunnable: Runnable? = null
    @JvmField var naviRight: ScrollNavi? = null
    @JvmField var jsResultY: String? = null
    @JvmField var jsResultZ: String? = null

    private var focusManager: FocusManager? = null
    private var menuOpen by mutableStateOf(false)
    private var menuNoti by mutableStateOf(false)
    /** Invisible view placed over the Compose translate button, so the translate control can point at it. */
    private var transAnchor: View? = null
    private var transX = 0f
    private var transY = 0f
    private var transW = 0
    private var transH = 0
    private var executor: java.util.concurrent.ExecutorService? = null

    override val scrimAlpha: Float get() = 0.6f

    init {
        this.progressRunnable = SetProgressTask()
        this.scrollRunnable = UpdatePosTask()
        this.hostActivity = mainActivity
        this.appCtx = context
        this.webListener = dialogWebListener
        this.urlMode = (urlModeParam).toByte()
        this.pageUrl = pageUrlParam
        this.referer = referer
        this.pageHost = hostOfUrl(pageUrlParam, true)
        if (external) {
            this.prevUrl = this.pageUrl
            this.urlBlocked = true
        }
        this.cleanEngine = MainApp.webClean(this.appCtx, false)
        this.layoutRtl = isRtl(this.appCtx)
        this.touchSlop = MainApp.dp80 / 2
        if (urlMode.toInt() != 2 && urlMode.toInt() != 3 && urlMode.toInt() != 4) {
            this.confirmPending = ReadSettings.viewTrans.value
        }
        if (urlMode.toInt() == 3) {
            urlEdit!!.setText(AppR.string.show_license)
        } else {
            urlEdit!!.setText(pageUrl)
        }
        menuNoti = ZtwoSettings.notiMenu.value
        buildViews()
        this.gestureDetect = GestureDetector(this.appCtx, NavForwardGestures())
        setScreen { Screen() }
        show()
        mainHandler?.post(AttachTask())
    }

    private val hasNewUrl: Boolean get() = urlMode.toInt() != 3 && urlMode.toInt() != 4
    private val hasMenu: Boolean get() = urlMode.toInt() != 2 && urlMode.toInt() != 3 && urlMode.toInt() != 4

    /** Where the main address bar is (the preview's bar goes on the same side): measured once when the preview opens. */
    // lazy: this property comes after the init block that already draws the screen
    private val barOnTop: Boolean by lazy { com.mycompany.app.ui.browser.previewBarOnTop() }

    /** The view the address menu points at (inside the bar's site name). */
    private var addressAnchor: View? = null
    private var addressMenu: MyPopupMenu? = null

    /** Lock in the bar: the security sheet of the previewed page (needs the browser activity). */
    private fun showSecurity() {
        val wa = hostActivity as? com.mycompany.app.web.WebViewActivity ?: return
        com.mycompany.app.dialog.SiteSecurityDialog(wa, page) { DialogWebCerti(wa, page) }
    }

    /**
     * Long press on the site name: the address menu of the main bar (same items, icons and speech bubble), acting on
     * the previewed page (2026-11-20, owner).
     */
    private fun showAddressMenu() {
        val host = hostActivity as? com.mycompany.app.main.MainActivity ?: return
        val anchor = addressAnchor ?: return
        val url = page?.url ?: pageUrl ?: return
        addressMenu?.dismissNow()
        val dark = com.mycompany.app.ui.state.AppState.darkUi
        fun icon(item: com.mycompany.app.view.PopMenuItem, black: Int, darkRes: Int) = item.also { it.iconRes = if (dark) darkRes else black }
        val items = ArrayList<Any?>()
        items.add(icon(com.mycompany.app.view.PopMenuItem(0, AppR.string.copy_url), AppR.drawable.outline_link_black_24, AppR.drawable.outline_link_dark_24))
        items.add(icon(com.mycompany.app.view.PopMenuItem(1, AppR.string.share_url), AppR.drawable.outline_share_black_24, AppR.drawable.outline_share_dark_24))
        items.add(icon(com.mycompany.app.view.PopMenuItem(2, AppR.string.paste_url), AppR.drawable.outline_content_paste_black_24, AppR.drawable.outline_content_paste_dark_24))
        items.add(icon(com.mycompany.app.view.PopMenuItem(3, AppR.string.open_copied_url), AppR.drawable.outline_open_in_new_black_24, AppR.drawable.outline_open_in_new_dark_24))
        if (webListener != null) {
            items.add(icon(com.mycompany.app.view.PopMenuItem(4, AppR.string.open_new_tab), AppR.drawable.outline_tab_new_right_black_24, AppR.drawable.outline_tab_new_right_dark_24))
            items.add(icon(com.mycompany.app.view.PopMenuItem(5, AppR.string.open_with), AppR.drawable.outline_apps_black_24, AppR.drawable.outline_apps_dark_24))
        }
        val siteHost = try { android.net.Uri.parse(url).host } catch (_: Exception) { null }
        if (!siteHost.isNullOrEmpty()) {
            items.add(icon(com.mycompany.app.view.PopMenuItem(6, host.getString(AppR.string.delete_cookie) + " (" + siteHost.removePrefix("www.") + ")"),
                AppR.drawable.outline_delete_sweep_black_24, AppR.drawable.outline_delete_sweep_dark_24))
        }
        if (url.startsWith("https://") && host is com.mycompany.app.web.WebViewActivity) {
            items.add(icon(com.mycompany.app.view.PopMenuItem(7, AppR.string.view_certi), AppR.drawable.outline_verified_user_black_24, AppR.drawable.outline_verified_user_dark_24))
        }
        val menu = MyPopupMenu(host, null, anchor, items, dark, object : MyPopupListener {
            override fun onClose() { addressMenu?.dismissNow(); addressMenu = null }
            override fun onItem(view: View?, id: Int): Boolean { addressAction(id, url); return true }
        })
        menu.bubble = true
        addressMenu = menu
    }

    private fun clipboardText(): String? = try {
        val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        cm.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(context)?.toString()?.trim()
    } catch (_: Exception) { null }

    private fun addressAction(id: Int, url: String) {
        val title = page?.title
        when (id) {
            0 -> MainUtil.copyTextToast(context, AppR.string.copied_clipboard, "Copied URL", url)
            1 -> MainUtil.shareText(hostActivity, url, title)
            2, 3 -> {
                // paste: into the address (to edit); open copied: load it here
                val clip = clipboardText()
                if (clip.isNullOrEmpty()) { showToast(context, AppR.string.empty); return }
                urlEdit?.text = clip
                if (id == 3) mainHandler?.post { updateUrlText() }
            }
            4 -> webListener?.onAction(3, url, title)
            5 -> webListener?.onAction(10, url, title)
            6 -> {
                val wa = hostActivity as? com.mycompany.app.web.WebViewActivity
                val siteHost = try { android.net.Uri.parse(url).host } catch (_: Exception) { null } ?: return
                val done = Runnable { mainHandler?.post { showToast(context, AppR.string.deleted) } }
                if (androidx.webkit.WebViewFeature.isFeatureSupported(androidx.webkit.WebViewFeature.DELETE_BROWSING_DATA)) {
                    try {
                        androidx.webkit.WebStorageCompat.deleteBrowsingDataForSite(android.webkit.WebStorage.getInstance(), siteHost, { it.run() }, done)
                        return
                    } catch (_: Exception) {
                    }
                }
                if (wa != null) { with(wa) { deleteSiteCookies(url, siteHost) } }
                done.run()
            }
            7 -> (hostActivity as? com.mycompany.app.web.WebViewActivity)?.let { DialogWebCerti(it, page) }
        }
    }

    /** The address as a site name ("heise.de"), or the text itself when it is no address (license title, empty). */
    private fun shortHost(text: String): String {
        val host = try { android.net.Uri.parse(text).host } catch (_: Exception) { null }
        return host?.removePrefix("www.")?.takeIf { it.isNotEmpty() } ?: text
    }

    /**
     * The link preview (reworked 2026-11-20, owner): a floating card with a gap to the screen edges and a dashed
     * outline, so it reads as temporary; one compact bar on the side where the main address bar is (top / bottom):
     * close, a "Preview" badge with the site name (tap it to edit the address), reload / stop, open as tab, and the
     * menu (translate and the rest). Dragging the bar down closes the preview.
     */
    @Composable
    private fun Screen() {
        focusManager = LocalFocusManager.current
        com.mycompany.app.ui.browser.PreviewCard(
            barOnTop = barOnTop,
            onClosed = { dismiss() },
            onOutside = { cancelFromOutside() },
            bar = { PreviewBar() },
        ) {
            val root = dialogFrame
            if (root != null) {
                AndroidView(factory = { root }, modifier = Modifier.fillMaxSize())
            }
        }
    }

    /** The preview's only bar (the card draws the drag handle): close · lock + site · reload / stop · open as tab · menu. */
    @Composable
    private fun PreviewBar() {
        var editing by androidx.compose.runtime.remember { mutableStateOf(false) }
        val focus = androidx.compose.runtime.remember { androidx.compose.ui.focus.FocusRequester() }
        run {
            Row(Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { CloseClick().onClick(null) }) {
                    Icon(painterResource(AppR.drawable.outline_close_black_24), stringResource(AppR.string.close))
                }
                val ed = urlEdit
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (editing && ed != null && urlMode.toInt() != 3) {
                        BasicTextField(
                            value = ed.text,
                            onValueChange = { ed.text = it },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = { editing = false; mainHandler?.post { updateUrlText() } }),
                            modifier = Modifier.fillMaxWidth().focusRequester(focus).onFocusChanged {
                                ed.focused = it.isFocused
                                if (!it.isFocused && editing) editing = false
                            },
                        )
                        androidx.compose.runtime.LaunchedEffect(Unit) { focus.requestFocus() }
                    } else {
                        val text = ed?.text ?: ""
                        val secure = text.startsWith("https://")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (secure) {
                                // the lock: the site's security sheet, as on the main address bar
                                Icon(
                                    painterResource(AppR.drawable.outline_lock_black_24), stringResource(AppR.string.view_certi),
                                    Modifier.size(32.dp).clip(androidx.compose.foundation.shape.CircleShape).clickable { showSecurity() }.padding(7.dp),
                                )
                            }
                            Box(
                                Modifier.weight(1f, fill = false)
                                    .combinedClickable(
                                        enabled = ed != null && urlMode.toInt() != 3,
                                        onClick = { editing = true },
                                        onLongClick = { showAddressMenu() },
                                    ),
                            ) {
                                // anchor for the address menu (a View, like the main bar's)
                                AndroidView(factory = { c -> View(c).also { addressAnchor = it } }, modifier = Modifier.matchParentSize())
                                Text(
                                    if (text.isEmpty()) stringResource(AppR.string.web_edit_hint) else shortHost(text),
                                    style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                    color = if (text.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(start = if (secure) 2.dp else 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                                )
                            }
                        }
                    }
                    if (loadCover?.vis == 0) {
                        // translating
                        CircularProgressIndicator(Modifier.size(18.dp).align(Alignment.CenterEnd), strokeWidth = 2.dp)
                    }
                }
                if (progressVis?.vis == 0) {
                    IconButton(onClick = { ShowStopClick().onClick(null) }) {
                        Icon(painterResource(AppR.drawable.outline_refresh_black_24), stringResource(AppR.string.refresh))
                    }
                }
                if (stopVis?.vis == 0) {
                    IconButton(onClick = { SyncProgressClick().onClick(null) }) {
                        Icon(painterResource(AppR.drawable.baseline_stop_black_24), stringResource(AppR.string.stop))
                    }
                }
                if (hasNewUrl) {
                    IconButton(onClick = { ActionClick().onClick(null) }) {
                        Icon(painterResource(AppR.drawable.outline_tab_new_right_black_24), stringResource(AppR.string.new_url))
                    }
                }
                if (hasMenu || !sheetShown) {
                    Box(Modifier.onGloballyPositioned {
                        // the translate control opens at the menu button
                        val p = it.positionInWindow()
                        transX = p.x; transY = p.y; transW = it.size.width; transH = it.size.height
                    }) {
                        IconButton(onClick = { openMenu() }) {
                            BadgedBox(badge = { if (menuNoti) Badge() }) {
                                Icon(painterResource(AppR.drawable.outline_more_vert_black_24), null)
                            }
                        }
                        com.mycompany.app.ui.components.BubbleMenu(expanded = menuOpen, onDismiss = { menuOpen = false }) {
                            if (!sheetShown) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(AppR.string.translate)) },
                                    leadingIcon = { Icon(painterResource(AppR.drawable.outline_g_translate_black_24), null, Modifier.size(20.dp)) },
                                    onClick = { menuOpen = false; onTranslate() },
                                )
                            }
                            if (hasMenu) for (item in menuItems()) {
                                // icons as in the link long-press menu (same actions)
                                val iconRes = when (item.first) {
                                    100 -> AppR.drawable.outline_translate_black_24
                                    101 -> AppR.drawable.outline_block_black_24
                                    6 -> if (SyncSettings.secretMode.value) AppR.drawable.outline_mood_black_24 else MainConst.linkMenuIconsBlack[6]
                                    else -> if (urlMode.toInt() != 1 && item.first in MainConst.linkMenuIconsBlack.indices) MainConst.linkMenuIconsBlack[item.first] else 0
                                }
                                DropdownMenuItem(
                                    leadingIcon = if (iconRes != 0) { { Icon(painterResource(iconRes), null, Modifier.size(20.dp)) } } else null,
                                    text = { Text(stringResource(item.second)) },
                                    trailingIcon = if (item.third) {
                                        { Icon(painterResource(AppR.drawable.outline_check_black_24), null, Modifier.size(20.dp)) }
                                    } else null,
                                    onClick = { menuOpen = false; DismissNowStep().onItem(null, item.first) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    /** Menu entries (id, label, checked). */
    private fun menuItems(): List<Triple<Int, Int, Boolean>> {
        if (urlMode.toInt() == 1) {
            return listOf(
                Triple(0, AppR.string.only_image, false),
                Triple(1, AppR.string.image_list, false),
                Triple(2, AppR.string.pop_allow, false),
            )
        }
        val menuNames = MainConst.linkMenuNames
        return listOf(
            Triple(100, AppR.string.trans_auto, ReadSettings.viewTrans.value),
            Triple(2, menuNames[2], false),
            Triple(5, menuNames[5], false),
            Triple(6, if (SyncSettings.secretMode.value) AppR.string.normal_tab else menuNames[6], false),
            Triple(7, menuNames[7], false),
            Triple(8, menuNames[8], false),
            Triple(101, AppR.string.block_area, false),
        )
    }

    /** Menu button: clears the one-time dot, then opens the menu. */
    private fun openMenu() {
        if (ZtwoSettings.notiMenu.value) {
            ZtwoSettings.edit(this.appCtx) { set(ZtwoSettings.notiMenu, false) }
            menuNoti = false
        }
        if (this.hostActivity == null) return
        menuOpen = true
    }

    /** Translate button (old `HasDialogClick`, with the anchor placed over the Compose button). */
    private fun onTranslate() {
        val root = dialogFrame ?: return
        val anchor = transAnchor ?: return
        val loc = IntArray(2)
        root.getLocationInWindow(loc)
        anchor.layoutParams = RelativeLayout.LayoutParams(maxOf(transW, 1), maxOf(transH, 1))
        anchor.translationX = transX - loc[0]
        anchor.translationY = transY - loc[1]
        HasDialogClick().onClick(anchor)
    }

    fun runInBackground(runnable: Runnable?) {
        var ex = executor
        if (ex == null || ex.isShutdown() || ex.isTerminated()) {
            ex = MainApp.executor(context) ?: return
            executor = ex
        }
        try { ex.execute(runnable) } catch (unused: Exception) { }
    }

    /** The old page area (everything below the address bar and above the buttons). */
    private fun buildViews() {
        val context = this.appCtx ?: return
        val root = DialogFrame(context)
        val webFrame = FrameLayout(context)
        root.addView(webFrame, RelativeLayout.LayoutParams(-1, -1))
        val badge = View(context)
        badge.setVisibility(View.GONE)
        val badgeLp = FrameLayout.LayoutParams(dpToPx(context, 130.0f).toInt(), dpToPx(context, 24.0f).toInt())
        badgeLp.gravity = 85
        webFrame.addView(badge, badgeLp)
        val progress = MyProgressBar(context)
        progress.fixedColor = true
        progress.showTrack = true
        progress.barHeightPx = dpToPx(context, 1.5f).toInt()
        progress.drawSkipped = true
        progress.setVisibility(View.GONE)
        root.addView(progress, RelativeLayout.LayoutParams(-1, dpToPx(context, 2.0f).toInt()))
        val naviL = ScrollNavi(context)
        naviL.setVisibility(View.INVISIBLE)
        val nlLp = RelativeLayout.LayoutParams(MainApp.dp64, MainApp.dp64)
        nlLp.addRule(RelativeLayout.CENTER_VERTICAL)
        nlLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT)
        root.addView(naviL, nlLp)
        val naviR = ScrollNavi(context)
        naviR.setVisibility(View.INVISIBLE)
        val nrLp = RelativeLayout.LayoutParams(MainApp.dp64, MainApp.dp64)
        nrLp.addRule(RelativeLayout.CENTER_VERTICAL)
        nrLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        root.addView(naviR, nrLp)
        val scrollBar = MyScrollBar(context)
        scrollBar.useCompact()
        scrollBar.topInset = 0
        scrollBar.bottomInset = maxOf(MainApp.dp56 - MainApp.dp48, 0)
        scrollBar.setVisibility(View.INVISIBLE)
        val sbLp = RelativeLayout.LayoutParams(dpToPx(context, 32.0f).toInt(), -1)
        sbLp.addRule(11)
        root.addView(scrollBar, sbLp)
        val anchor = View(context)
        anchor.isClickable = false
        root.addView(anchor, RelativeLayout.LayoutParams(1, 1))
        this.dialogFrame = root
        this.webFrameView = webFrame
        this.progressBar = progress
        this.naviLeft = naviL
        this.naviRight = naviR
        this.scrollBarView = scrollBar
        this.badgeView = badge
        this.transAnchor = anchor
        // theme / listeners (old UpdateUpViewTask)
        if (AppState.darkUi) {
            progress.applyColors(-922746881, -16777216)
        } else {
            progress.applyColors(-13022805, -460552)
        }
        webFrame.setBackgroundColor(if (AppState.darkWeb) -14606047 else -1)
        naviL.setup(this.layoutRtl, true)
        naviR.setup(this.layoutRtl, false)
        if (ZoneSettings.scrollPos.value != 0) {
            scrollBar.setPreColor(if (AppState.darkUi) -12632257 else -2434342)
            if (ZoneSettings.scrollPos.value == 1) {
                sbLp.removeRule(11)
                sbLp.addRule(9)
                scrollBar.setPosLeft(true)
            }
            scrollBar.setListener(ScrollToStep())
        }
    }

    internal inner class HasDialogClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogWebView = this@DialogWebView
            if (dialogWebView.hasDialog() || view == null || dialogWebView.dialogFrame == null) {
                return
            }
            dialogWebView.removeTransFrame()
            if (dialogWebView.menuTransControl == null) {
                dialogWebView.menuTransControl = WebTransControl(dialogWebView.hostActivity!!)
                dialogWebView.customView = view
                val handler = dialogWebView.mainHandler
                if (handler != null) {
                    handler.post(dialogWebView.ShowTransMenuTask())
                }
            }
        }
    }

    internal inner class NavForwardGestures : SimpleOnGestureListener() {
        override fun onFling(down: MotionEvent?, move: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            val dialogWebView = this@DialogWebView
            val layoutRtl = dialogWebView.layoutRtl
            if (dialogWebView.page == null) {
                return false
            }
            if (dialogWebView.swipeState.toInt() == 0) {
                val swipeEdge = dialogWebView.swipeEdge
                if (swipeEdge.toInt() == 1) {
                    if (velocityX > 600.0f) {
                        if (layoutRtl) {
                            dialogWebView.navForward()
                        } else {
                            dialogWebView.navBack()
                        }
                    }
                } else if (swipeEdge.toInt() == 2 && velocityX < -600.0f) {
                    if (layoutRtl) {
                        dialogWebView.navBack()
                    } else {
                        dialogWebView.navForward()
                    }
                }
            }
            return super.onFling(down, move, velocityX, velocityY)
        }
    }

    internal inner class AttachTask : Runnable {
        override fun run() {
            val dialogWebView = this@DialogWebView
            if (dialogWebView.webFrameView != null) {
                val webNestView = WebNestView(dialogWebView.hostActivity)
                dialogWebView.page = webNestView
                // element hiding / scriptlets of the uBlock Origin-compatible engine, as in the browser tabs
                webNestView.uboCosmetic = com.mycompany.app.web.filter.UboCosmetic.attach(webNestView)
                webNestView.setVerticalScrollBarEnabled(ZoneSettings.scrollPos.value == 0)
                dialogWebView.webFrameView!!.addView(dialogWebView.page, 0, FrameLayout.LayoutParams(-1, -1))
                val handler = dialogWebView.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(dialogWebView.SetupPageTask())
            }
        }
    }

    internal inner class SetupPageTask : Runnable {

        internal inner class OpenPageStep : Runnable {

            internal inner class OpenUrlTask : Runnable {

                internal inner class AddBridgeStep : Runnable {

                    internal inner class OpenUrlStep : Runnable {

                        internal inner class PlaceBarsStep : Runnable {
                            override fun run() {
                                var rightMargin: Int = 0
                                val gravity: Int
                                val leftMargin: Int
                                val dialogWebView = this@DialogWebView
                                if (dialogWebView.page == null || dialogWebView.upView != null || dialogWebView.webFrameView == null) {
                                    return
                                }
                                val upPos = ZoneSettings.showUpPos.value
                                if (upPos == 1) {
                                    leftMargin = MainApp.dp4
                                    gravity = 83
                                    rightMargin = 0
                                } else if (upPos == 2) {
                                    gravity = 81
                                    leftMargin = 0
                                    rightMargin = 0
                                } else {
                                    if (upPos != 3) {
                                        return
                                    }
                                    gravity = 85
                                    rightMargin = MainApp.dp4
                                    leftMargin = 0
                                }
                                val dp48Px = MainApp.dp48
                                val layoutParams = FrameLayout.LayoutParams(dp48Px, dp48Px)
                                layoutParams.bottomMargin = MainApp.dp8
                                layoutParams.gravity = gravity
                                layoutParams.leftMargin = leftMargin
                                layoutParams.rightMargin = rightMargin
                                try {
                                    val webUpView = WebUpView(dialogWebView.appCtx ?: throw IllegalStateException("no context"))
                                    dialogWebView.upView = webUpView
                                    webUpView.setVisibility(View.GONE)
                                    dialogWebView.upView!!.setBgColors(false)
                                    val upView = dialogWebView.upView
                                    val upViewActions = dialogWebView.UpViewActions()
                                    upView!!.longPressEnabled = false
                                    upView.listener = upViewActions
                                    dialogWebView.webFrameView!!.addView(upView, layoutParams)
                                } catch (unused: Exception) {
                                }
                            }
                        }

                        override fun run() {
                            val openUrlTask = this@OpenUrlTask
                            val dialogWebView = this@DialogWebView
                            val webNestView = dialogWebView.page
                            if (webNestView != null) {
                                webNestView.openUrl(dialogWebView.pageUrl, dialogWebView.referer)
                                this@DialogWebView.setProgress(0)
                                val dialog = this@DialogWebView
                                if (!dialog.firstLoad) {
                                    dialog.firstLoad = true
                                }
                                val handler: Handler? = if (ZoneSettings.showUpPos.value == 0) null else dialog.mainHandler
                                if (ZoneSettings.showUpPos.value == 0 || handler == null) {
                                    return
                                }
                                handler.post(PlaceBarsStep())
                            }
                        }
                    }

                    override fun run() {
                        val openPageStep = this@OpenPageStep
                        val dialogWebView = this@DialogWebView
                        val webNestView = dialogWebView.page
                        if (webNestView != null) {
                            dialogWebView.loadPending = true
                            webNestView.addJavascriptInterface(
                                dialogWebView.WebAppInterface(),
                                "android"
                            )
                            val handler = this@DialogWebView.mainHandler
                            if (handler == null) {
                                return
                            }
                            handler.post(OpenUrlStep())
                        }
                    }
                }

                override fun run() {
                    val setupPageTask = this@SetupPageTask
                    val dialogWebView = this@DialogWebView
                    val webNestView = dialogWebView.page
                    if (webNestView != null) {
                        webNestView.setWebViewClient(dialogWebView.LocalWebViewClient())
                        webNestView.setWebChromeClient(dialogWebView.LocalChromeClient())
                        webNestView.setListener(dialogWebView.PageListener())
                        webNestView.setDownloadListener(dialogWebView.PageDownloadListener())
                        val handler = this@DialogWebView.mainHandler
                        if (handler == null) {
                            return
                        }
                        handler.post(AddBridgeStep())
                    }
                }
            }

            override fun run() {
                val dialogWebView = this@DialogWebView
                val webNestView = dialogWebView.page
                if (webNestView != null) {
                    setAlgorithmicDark(webNestView.getSettings(), AppState.darkWeb)
                    val handler = dialogWebView.mainHandler
                    if (handler == null) {
                        return
                    }
                    handler.post(OpenUrlTask())
                }
            }
        }

        override fun run() {
            val dialogWebView = this@DialogWebView
            val webNestView = dialogWebView.page
            if (webNestView != null) {
                val index = ZoneSettings.textSize.value
                if (index < 50 || index > 500) {
                    ZoneSettings.editLocal { set(ZoneSettings.textSize, 100) }
                }
                val settings = webNestView.getSettings()
                settings.setTextZoom(ZoneSettings.textSize.value)
                settings.setSupportZoom(true)
                settings.setBuiltInZoomControls(true)
                settings.setDisplayZoomControls(false)
                settings.setUseWideViewPort(true)
                settings.setLoadWithOverviewMode(true)
                settings.setDomStorageEnabled(true)
                settings.setJavaScriptCanOpenWindowsAutomatically(false)
                settings.setSupportMultipleWindows(false)
                settings.setMediaPlaybackRequiresUserGesture(false)
                settings.setDatabaseEnabled(true)
                settings.setMixedContentMode(0)
                settings.setAllowFileAccess(true)
                if (ZoneSettings.hideImage.value) {
                    settings.setLoadsImagesAutomatically(false)
                }
                val deskMode = webNestView.deskMode
                val context = dialogWebView.appCtx
                if (deskMode) {
                    setUserAgent(settings, chromeVersion(context))
                } else {
                    webNestView.applyUserAgent(context, ZtwoSettings.agentType.value, settings)
                }
                webNestView.setEnableJs(BrowserSettings.enableJs.value)
                webNestView.setViewCookie(dialogWebView.appCtx)
                webNestView.setOverScrollMode(2)
                val handler = dialogWebView.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(OpenPageStep())
            }
        }
    }

    internal inner class UpViewActions : UpViewListener {
        override fun onTap() {
            val dialogWebView = this@DialogWebView
            val webNestView = dialogWebView.page
            if (webNestView == null || webNestView.coversPage()) {
                return
            }
            val scrollAnim = ZtwoSettings.scrollAnim.value
            val page = dialogWebView.page
            if (scrollAnim) {
                page!!.pageUp(true)
            } else if (page!!.getScrollY() > 0) {
                dialogWebView.page!!.scrollTo(0, 0)
            }
        }

        override fun onHold() {
        }
    }

    internal inner class PageListener : WebViewListener {
        override fun onCrashAction(action: Int) {
        }

        override fun openAdvanced() {
        }

        override fun onProgress(progress: Int) {
            var offset = 0
            val starting = progress < 1
            val dialogWebView = this@DialogWebView
            dialogWebView.updateUpView()
            val scrollBase = dialogWebView.scrollBase
            val view = dialogWebView.badgeView
            val height = if (view != null && view.getVisibility() == View.VISIBLE) dialogWebView.badgeView!!.getHeight() else 0
            if (view != null && view.getVisibility() == View.VISIBLE && height != 0) {
                val scrollShift = (progress - scrollBase) + dialogWebView.scrollShift
                dialogWebView.scrollShift = scrollShift
                if (scrollShift > height) {
                    dialogWebView.scrollShift = height
                    offset = height
                } else if (scrollShift < 0) {
                    dialogWebView.scrollShift = 0
                } else {
                    offset = scrollShift
                }
                dialogWebView.badgeView!!.setAlpha(1.0f - (offset / height))
                dialogWebView.badgeView!!.setTranslationY(dialogWebView.scrollShift.toFloat())
            }
            dialogWebView.scrollBase = progress
            val runnable = dialogWebView.scrollRunnable
            val myScrollBar = dialogWebView.scrollBarView
            if (myScrollBar == null || dialogWebView.scrollLocked) {
                return
            }
            dialogWebView.scrollLocked = true
            myScrollBar.removeCallbacks(runnable)
            dialogWebView.scrollBarView!!.post(runnable)
        }

        override fun canPull(): Boolean {
            return false
        }

        override fun onScrollStop() {
        }

        override fun onBlockCount(count: Int) {
        }

        override fun onPull(x: Float, y: Float, action: Int): Boolean {
            return false
        }

        override fun onPageCommit(pageUrl: String?) {
        }
    }

    internal inner class PageDownloadListener : DownloadListener {
        override fun onDownloadStart(
            url: String?,
            userAgent: String?,
            contentDisposition: String?,
            mimeType: String?,
            contentLength: Long
        ) {
            val dialogWebListener = this@DialogWebView.webListener
            if (dialogWebListener != null) {
                dialogWebListener.onDownload(url, contentDisposition, mimeType, contentLength)
            }
        }
    }

    internal inner class RemoveJsBridgeTask : Runnable {
        override fun run() {
            val dialogWebView = this@DialogWebView
            val webNestView = dialogWebView.page
            if (webNestView == null) {
                return
            }
            dialogWebView.loadPending = false
            webNestView.removeJavascriptInterface("android")
        }
    }

    internal inner class AddJsBridgeTask : Runnable {
        override fun run() {
            val dialogWebView = this@DialogWebView
            val webNestView = dialogWebView.page
            if (webNestView == null) {
                return
            }
            dialogWebView.loadPending = true
            webNestView.addJavascriptInterface(dialogWebView.WebAppInterface(), "android")
        }
    }

    internal inner class ClearWebCacheTask : Runnable {
        override fun run() {
            val dialogWebView = this@DialogWebView
            clearWebCache(dialogWebView.page)
            dialogWebView.finishHandled = false
        }
    }

    internal inner class DismissTask : Runnable {
        override fun run() {
            this@DialogWebView.dismiss()
        }
    }

    internal inner class SetProgressTask : Runnable {
        override fun run() {
            val dialogWebView = this@DialogWebView
            dialogWebView.setProgress(dialogWebView.progressValue)
        }
    }

    internal inner class UpdatePosTask : Runnable {
        override fun run() {
            val dialogWebView = this@DialogWebView
            val myScrollBar = dialogWebView.scrollBarView
            if (myScrollBar != null) {
                myScrollBar.updatePos(0, 0)
            }
            dialogWebView.scrollLocked = false
        }
    }

    internal inner class DismissNowStep : MyPopupListener {
        override fun onClose() {
            val dialogWebView = this@DialogWebView
            val myPopupMenu = dialogWebView.moreMenu
            if (myPopupMenu != null) {
                myPopupMenu.dismissNow()
                dialogWebView.moreMenu = null
            }
        }

        override fun onItem(view: View?, id: Int): Boolean {
            val dialogWebView = this@DialogWebView
            val dialogWebListener = dialogWebView.webListener
            if (dialogWebListener != null) {
                if (dialogWebView.urlMode.toInt() == 1) {
                    if (id == 0) {
                        dialogWebListener.onShown()
                        return true
                    }
                    if (id == 1) {
                        dialogWebListener.onHidden()
                        return true
                    }
                    dialogWebListener.onClosed()
                    return true
                }
                if (id == 100) {
                    val viewTrans = !ReadSettings.viewTrans.value
                    ReadSettings.edit(dialogWebView.appCtx) { set(ReadSettings.viewTrans, viewTrans) }
                    if (ReadSettings.viewTrans.value) {
                        if (!dialogWebView.loadPending) {
                            showToast(dialogWebView.appCtx, AppR.string.http_warning)
                            return true
                        }
                        if (dialogWebView.transState.toInt() != 1) {
                            dialogWebView.stopTrans()
                            if (dialogWebView.transState.toInt() == 3) {
                                dialogWebView.confirmArea(false)
                            }
                            dialogWebView.startTrans(dialogWebView.transLang)
                            if (dialogWebView.transState.toInt() == 3 && TextUtils.isEmpty(dialogWebView.transLang)) {
                                showToast(dialogWebView.appCtx, AppR.string.select_lang)
                                return true
                            }
                        }
                    }
                } else {
                    if (id != 101) {
                        dialogWebListener.onAction(id, dialogWebView.pageUrl, dialogWebView.page!!.getTitle())
                        return true
                    }
                    if (dialogWebView.appCtx != null && dialogWebView.areaView == null && dialogWebView.webFrameView != null && !dialogWebView.pendingAction) {
                        dialogWebView.pendingAction = true
                        dialogWebView.areaTouched = false
                        val handler = dialogWebView.mainHandler
                        if (handler != null) {
                            handler.post(dialogWebView.OpenAreaViewTask())
                        }
                    }
                }
            }
            return true
        }
    }

    internal inner class ShowCoverTask : Runnable {
        override fun run() {
            val myCoverView = this@DialogWebView.loadCover
            if (myCoverView == null) {
                return
            }
            myCoverView.setVisibility(0)
        }
    }

    internal inner class HideCoverTask : Runnable {
        override fun run() {
            val dialogWebView = this@DialogWebView
            val myCoverView = dialogWebView.loadCover
            if (myCoverView == null) {
                return
            }
            myCoverView.setVisibility(8)
            dialogWebView.loadCover!!.hide()
            dialogWebView.loadCover = null
        }
    }

    internal inner class EvalJsTask(@JvmField val outer: DialogWebView,
                                          @JvmField val useScript: Boolean
    ) : Runnable {
        override fun run() {
            val sbK = transScriptJs()
            val dialogWebView = this.outer
            var script: String? = null
            if (sbK != null) {
                if (this.useScript) {
                    script = sbK.toString()
                } else {
                    val classJs = classSelectorJs(dialogWebView.detectedLang)
                    if (!TextUtils.isEmpty(classJs)) {
                        sbK.insert(0, classJs)
                        script = sbK.toString()
                    }
                }
            }
            evalJs(dialogWebView.page, script, true)
        }
    }

    internal inner class TransLangFromCookieCallback : ValueCallback<String> {
        override fun onReceiveValue(cookies: String?) {
            val cookieLang = transLangFromCookie(cookies)
            if (TextUtils.isEmpty(cookieLang) || cookieLang == AlbumSettings.transCode.value) {
                return
            }
            AlbumSettings.edit(this@DialogWebView.appCtx) { set(AlbumSettings.transCode, cookieLang) }
        }
    }

    internal inner class ParseTransResultTask : Runnable {
        override fun run() {
            val dialogWebView = this@DialogWebView
            val jsResultY = dialogWebView.jsResultY
            dialogWebView.jsResultY = null
            if (dialogWebView.page == null) {
                return
            }
            parseTransResult(dialogWebView.appCtx, jsResultY)
        }
    }

    internal inner class LangPrefixTask : Runnable {
        override fun run() {
            val dialogWebView = this@DialogWebView
            val jsResultZ = dialogWebView.jsResultZ
            dialogWebView.jsResultZ = null
            if (dialogWebView.page == null || !TextUtils.isEmpty(dialogWebView.detectedLang)) {
                return
            }
            val prefix = langPrefix(jsResultZ)
            dialogWebView.detectedLang = prefix
            if (prefix.length > 2) {
                hideDivsByClass(dialogWebView.page, prefix)
            }
        }
    }

    internal inner class EvalLongPressJsTask(@JvmField val outer: DialogWebView,
                                          @JvmField val script: String?
    ) : Runnable {
        override fun run() {
            evalLongPressJs(this.outer.page, this.script)
        }
    }

    internal inner class ShowTransMenuTask : Runnable {

        internal inner class PlaceTransMenuStep : Runnable {
            override fun run() {
                var leftMargin: Int
                var popupWidth: Int = 0
                val dialogWebView = this@DialogWebView
                val webTransControl = dialogWebView.menuTransControl
                val view = dialogWebView.customView
                dialogWebView.menuTransControl = null
                dialogWebView.customView = null
                if (webTransControl == null || dialogWebView.transControl != null) {
                    return
                }
                dialogWebView.transControl = webTransControl
                try {
                    val myDialogRelative = dialogWebView.dialogFrame
                    val dialogPos = IntArray(2)
                    val viewPos = IntArray(2)
                    myDialogRelative!!.getLocationOnScreen(dialogPos)
                    view!!.getLocationOnScreen(viewPos)
                    var pivotYPx = 0
                    val offsetX = viewPos[0] - dialogPos[0]
                    val offsetY = viewPos[1] - dialogPos[1]
                    val width = view.getWidth()
                    val height = view.getHeight()
                    val boxWidth = myDialogRelative.getWidth()
                    var boxHeight = myDialogRelative.getHeight()
                    val pivotXPx = offsetX + width
                    val dp48Px = MainApp.dp48
                    var pivotX = pivotXPx - (dp48Px / 2)
                    if (pivotX > boxWidth) {
                        pivotX = boxWidth
                    }
                    if (pivotX < 0) {
                        pivotX = 0
                    }
                    var popupWidth = if (boxWidth <= boxHeight) 0 else (dp48Px * 7)
                    if (boxWidth <= boxHeight || boxWidth <= popupWidth) {
                        leftMargin = 0
                        popupWidth = -1
                    } else {
                        leftMargin = pivotX - (popupWidth / 2)
                        val maxLeft = boxWidth - popupWidth
                        if (leftMargin > maxLeft) {
                            leftMargin = maxLeft
                        }
                        if (leftMargin < 0) {
                            leftMargin = 0
                        }
                        pivotX -= leftMargin
                        val maxPivotX = boxWidth - leftMargin
                        if (pivotX > maxPivotX) {
                            pivotX = maxPivotX
                        }
                        if (pivotX < 0) {
                            pivotX = 0
                        }
                    }
                    val popupHeight = MainApp.dp8 * 9
                    var topMargin = MainApp.dp48 + offsetY
                    val maxTop = boxHeight - popupHeight
                    if (topMargin > maxTop) {
                        topMargin = maxTop
                    }
                    if (topMargin < 0) {
                        var index = offsetY + (height / 2)
                        if (index > popupHeight) {
                            index = popupHeight
                        }
                        if (index <= boxHeight) {
                            boxHeight = index
                        }
                        topMargin = 0
                        if (boxHeight >= 0) {
                            pivotYPx = boxHeight
                        }
                    }
                    val transControl = dialogWebView.transControl
                    transControl!!.pivotXPx = pivotX
                    transControl.pivotYPx = pivotYPx
                    transControl.setVisibility(View.INVISIBLE)
                    dialogWebView.transControl!!.setListener(dialogWebView.TransControlActions())
                    val layoutParams = FrameLayout.LayoutParams(popupWidth, popupHeight)
                    if (popupWidth != -1) {
                        if (dialogWebView.layoutRtl) {
                            layoutParams.rightMargin = boxWidth - (leftMargin + popupWidth)
                        } else {
                            layoutParams.leftMargin = leftMargin
                        }
                    }
                    layoutParams.topMargin = topMargin
                    val frameLayout = FrameLayout(dialogWebView.appCtx!!)
                    dialogWebView.transFrame = frameLayout
                    frameLayout.addView(dialogWebView.transControl, layoutParams)
                    dialogWebView.transFrame!!.setOnClickListener(dialogWebView.TransFrameClick())
                    myDialogRelative.addView(dialogWebView.transFrame, -1, -1)
                    dialogWebView.mainHandler!!.post(dialogWebView.ShowTransStateTask())
                } catch (unused: Exception) {
                }
            }
        }

        override fun run() {
            val dialogWebView = this@DialogWebView
            val webTransControl = dialogWebView.menuTransControl
            if (webTransControl != null) {
                webTransControl.applyMode(1)
            }
            val handler = dialogWebView.mainHandler
            if (handler == null) {
                return
            }
            handler.post(PlaceTransMenuStep())
        }
    }

    internal inner class TransControlActions : TransCtrlListener {
        override fun onClose() {
            this@DialogWebView.removeTransFrame()
        }

        override fun onOriginal() {
            val dialogWebView = this@DialogWebView
            if (!dialogWebView.loadPending) {
                showToast(dialogWebView.appCtx, AppR.string.http_warning)
                return
            }
            if (dialogWebView.transState.toInt() == 1) {
                showToast(dialogWebView.appCtx, AppR.string.wait_retry)
                return
            }
            dialogWebView.hideCover()
            dialogWebView.stopTrans()
            if (dialogWebView.transState.toInt() == 3) {
                dialogWebView.confirmArea(true)
            }
        }

        override fun onMenu(view: View?) {
            val dialogWebView = this@DialogWebView
            if (dialogWebView.hostActivity == null || dialogWebView.transLangDialog != null) {
                return
            }
            dialogWebView.dismissTransLang()
            if (!DataTrans.instance(dialogWebView.appCtx).hasLanguages() && dialogWebView.transState.toInt() == 1) {
                showToast(dialogWebView.appCtx, AppR.string.wait_retry)
                return
            }
            val dialogTransLang =
                DialogTransLang(dialogWebView.hostActivity, false, dialogWebView.TransLangPicked())
            dialogWebView.transLangDialog = dialogTransLang
            if (dialogWebView.notiListener != null) {
                dialogTransLang.transListener = dialogWebView.TransNotiDone()
            }
            dialogTransLang.setOnDismissListener(dialogWebView.TransLangDialogDismiss())
        }

        override fun onTranslate() {
            val dialogWebView = this@DialogWebView
            if (!dialogWebView.loadPending) {
                showToast(dialogWebView.appCtx, AppR.string.http_warning)
                return
            }
            if (dialogWebView.transState.toInt() == 1) {
                showToast(dialogWebView.appCtx, AppR.string.wait_retry)
                return
            }
            dialogWebView.stopTrans()
            if (dialogWebView.transState.toInt() == 3) {
                dialogWebView.confirmArea(false)
            }
            dialogWebView.startTrans(dialogWebView.transLang)
            if (dialogWebView.transState.toInt() == 3 && TextUtils.isEmpty(dialogWebView.transLang)) {
                showToast(dialogWebView.appCtx, AppR.string.select_lang)
            }
        }
    }

    internal inner class TransFrameClick : View.OnClickListener {
        override fun onClick(view: View?) {
            this@DialogWebView.stopTrans()
        }
    }

    internal inner class ShowStopClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogWebView = this@DialogWebView
            val myButtonImage: Vis? = if (dialogWebView.progressVis == null || dialogWebView.page == null) null else dialogWebView.stopVis
            if (dialogWebView.progressVis == null || dialogWebView.page == null || myButtonImage == null || myButtonImage.getVisibility() == 0) {
                return
            }
            dialogWebView.progressVis!!.setVisibility(8)
            dialogWebView.stopVis!!.setVisibility(0)
            dialogWebView.updateUrlText()
        }
    }

    internal inner class ShowTransStateTask : Runnable {
        override fun run() {
            val dialogWebView = this@DialogWebView
            val webTransControl = dialogWebView.transControl
            if (webTransControl == null) {
                return
            }
            val transState = dialogWebView.transState
            webTransControl.showState(dialogWebView.transLang, dialogWebView.transOn, transState.toInt())
            dialogWebView.transControl!!.animateShow(true)
        }
    }

    internal inner class TransLangPicked : TransLangListener {
        override fun onLanguage(lang: String?) {
            val dialogWebView = this@DialogWebView
            dialogWebView.dismissTransLang()
            dialogWebView.stopTrans()
            if (dialogWebView.transState.toInt() == 3) {
                dialogWebView.confirmPending = true
                if (!TextUtils.isEmpty(lang)) {
                    dialogWebView.runInBackground(EvalLongPressJsTask(dialogWebView, lang))
                }
            }
            dialogWebView.startTrans(lang)
        }
    }

    internal inner class TransNotiDone : TransNotiListener {
        override fun onDone() {
            val dialogWebView = this@DialogWebView
            dialogWebView.dismissTransLang()
            val transNotiListener = dialogWebView.notiListener
            if (transNotiListener != null) {
                transNotiListener.onDone()
            }
        }
    }

    internal inner class TransLangDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            this@DialogWebView.dismissTransLang()
        }
    }

    internal inner class OpenAreaViewTask : Runnable {
        override fun run() {
            val dialogWebView = this@DialogWebView
            if (dialogWebView.areaView != null || dialogWebView.webFrameView == null) {
                dialogWebView.pendingAction = false
                return
            }
            try {
                val webAreaView = WebAreaView(dialogWebView.appCtx ?: run { dialogWebView.pendingAction = false; return })
                dialogWebView.areaView = webAreaView
                webAreaView.setVisibility(View.INVISIBLE)
                dialogWebView.areaView!!.setFltListener(dialogWebView.AreaActions())
                val frameLayout = dialogWebView.webFrameView
                val areaView = dialogWebView.areaView
                frameLayout!!.addView(
                    areaView,
                    areaView!!.getViewWidth(),
                    dialogWebView.areaView!!.getViewHeight()
                )
                val handler = dialogWebView.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(dialogWebView.PlaceAreaViewTask())
            } catch (unused: Exception) {
                dialogWebView.pendingAction = false
            }
        }
    }

    internal inner class AreaActions : WebAreaListener {
        override fun onClose() {
            this@DialogWebView.clearArea(true)
        }

        override fun onDone() {
            val dialogWebView = this@DialogWebView
            val webNestView: WebNestView? = if (dialogWebView.areaView == null) null else dialogWebView.page
            if (dialogWebView.areaView == null || webNestView == null) {
                return
            }
            webNestView.evaluateJavascript(
                "(function(){if(!window.sbcmd)return null;var cmd=window.sbcmd;var eles=window.sbattr;if(eles){eles.forEach((ele)=>{if(!ele)ele='null';cmd+='!@!'+ele;});}return cmd;})();",
                dialogWebView.AreaTextCallback()
            )
        }

        override fun onMultiToggled() {
            val dialogWebView = this@DialogWebView
            if (dialogWebView.areaView != null) {
                if (TextUtils.isEmpty(dialogWebView.areaJsA)) {
                    val singleJs = areaMultiJs(0)
                    dialogWebView.areaJsA = singleJs
                    if (TextUtils.isEmpty(singleJs)) {
                        return
                    }
                }
                if (TextUtils.isEmpty(dialogWebView.areaJsB)) {
                    val multiJs = areaMultiJs(1)
                    dialogWebView.areaJsB = multiJs
                    if (TextUtils.isEmpty(multiJs)) {
                        return
                    }
                }
                val sb = StringBuilder()
                sb.append(dialogWebView.areaJsA)
                sb.append(if (ReadSettings.areaMulti.value) "true;" else "false;")
                sb.append(dialogWebView.areaJsB)
                evalJs(dialogWebView.page, sb.toString(), true)
            }
        }

        override fun onHelp() {
            val dialogWebView = this@DialogWebView
            if (dialogWebView.hostActivity == null || dialogWebView.hasDialog()) {
                return
            }
            val dialogGuideArea = dialogWebView.areaGuideDialog
            if (dialogGuideArea != null) {
                dialogGuideArea.dismiss()
                dialogWebView.areaGuideDialog = null
            }
            val areaGuideDialog =
                DialogGuideArea(dialogWebView.hostActivity, 0, dialogWebView.GuideAreaListener())
            dialogWebView.areaGuideDialog = areaGuideDialog
            areaGuideDialog.setOnDismissListener(dialogWebView.AreaGuideDialogDismiss())
        }
    }

    internal inner class PlaceAreaViewTask : Runnable {
        override fun run() {
            val dialogWebView = this@DialogWebView
            val webAreaView = dialogWebView.areaView
            if (webAreaView != null) {
                webAreaView.placeIn(dialogWebView.webFrameView, -5678, -5678)
                dialogWebView.areaView!!.setVisibility(View.VISIBLE)
            }
            dialogWebView.transRunning = false
            if (dialogWebView.areaView != null) {
                dialogWebView.transRunning = true
                dialogWebView.runInBackground(dialogWebView.AreaScriptTask())
            }
            dialogWebView.pendingAction = false
        }
    }

    internal inner class ReleaseTask : Runnable {
        override fun run() {
            val dialogWebView = this@DialogWebView
            val webAreaView = dialogWebView.areaView
            if (webAreaView != null) {
                webAreaView.release()
                val frameLayout = dialogWebView.webFrameView
                if (frameLayout != null) {
                    frameLayout.removeView(dialogWebView.areaView)
                }
                dialogWebView.areaView = null
                dialogWebView.areaJs = null
                dialogWebView.areaJsA = null
                dialogWebView.areaJsB = null
                val webNestView = dialogWebView.page
                if (webNestView != null) {
                    webNestView.setOnTouchListener(null)
                }
            }
        }
    }

    internal inner class AreaScriptTask : Runnable {

        internal inner class AreaScriptStep : Runnable {
            override fun run() {
                val dialogWebView = this@DialogWebView
                val webNestView: WebNestView? = if (dialogWebView.areaView != null) dialogWebView.page else null
                if (dialogWebView.areaView != null && webNestView != null) {
                    webNestView.setOnTouchListener(dialogWebView.AreaTouch())
                }
                dialogWebView.transRunning = false
            }
        }

        override fun run() {
            val dialogWebView = this@DialogWebView
            if (dialogWebView.areaView != null) {
                val areaJs = MainUtil.areaScriptJs(false)
                if (!TextUtils.isEmpty(areaJs) && dialogWebView.areaView != null) {
                    evalJs(dialogWebView.page, areaJs, true)
                }
            }
            val myDialogRelative = dialogWebView.dialogFrame
            if (myDialogRelative == null) {
                return
            }
            myDialogRelative.post(AreaScriptStep())
        }
    }

    internal inner class AreaTouch : OnTouchListener {
        override fun onTouch(view: View?, event: MotionEvent?): Boolean {
            val actionMasked = event!!.getActionMasked()
            val dialogWebView = this@DialogWebView
            if (actionMasked == 0) {
                if (dialogWebView.areaView != null) {
                    dialogWebView.areaTouched = true
                    evalJs(dialogWebView.page, "window.sbtouch=true;", true)
                    return false
                }
            } else if (actionMasked == 1) {
                if (dialogWebView.areaView != null) {
                    dialogWebView.areaTouched = false
                    val myDialogRelative = dialogWebView.dialogFrame
                    if (myDialogRelative != null) {
                        myDialogRelative.postDelayed(dialogWebView.AreaUpScriptTask(), 200L)
                        return false
                    }
                }
            }
            return false
        }
    }

    internal inner class SyncProgressClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogWebView = this@DialogWebView
            val webNestView = dialogWebView.page
            if (webNestView == null) {
                return
            }
            dialogWebView.setProgress(webNestView.getProgress())
            dialogWebView.page!!.stopLoading()
        }
    }

    internal inner class AreaUpScriptTask : Runnable {
        override fun run() {
            val dialogWebView = this@DialogWebView
            if (dialogWebView.areaView == null || dialogWebView.areaTouched) {
                return
            }
            if (TextUtils.isEmpty(dialogWebView.areaJs)) {
                val upScript = areaUpScript()
                dialogWebView.areaJs = upScript
                if (TextUtils.isEmpty(upScript)) {
                    return
                }
            }
            evalJs(dialogWebView.page, dialogWebView.areaJs, true)
        }
    }

    internal inner class AreaTextCallback : ValueCallback<String> {

        internal inner class ApplyAreaTextStep : Runnable {

            override fun run() {
                val css: String?
                val dialogWebView = this@DialogWebView
                val pendingJs = dialogWebView.pendingJs
                dialogWebView.pendingJs = null
                if (dialogWebView.hostActivity == null || dialogWebView.hasDialog()) {
                    return
                }
                if (TextUtils.isEmpty(pendingJs)) {
                    css = null
                } else {
                    val resultText = MainUtil.unescapeJavaText(stripQuotes(pendingJs))
                    if (TextUtils.isEmpty(resultText) || "null" == resultText || "undefined" == resultText) {
                        css = null
                    } else {
                        css = resultText
                    }
                }
                if (TextUtils.isEmpty(css)) {
                    showToast(dialogWebView.appCtx, AppR.string.backup_target)
                    return
                }
                val dialogEditArea = dialogWebView.editAreaDialog
                if (dialogEditArea != null) {
                    dialogEditArea.dismiss()
                    dialogWebView.editAreaDialog = null
                }
                val editAreaDialog = DialogEditArea(
                    dialogWebView.hostActivity,
                    dialogWebView.pageUrl,
                    dialogWebView.pageHost,
                    css,
                    dialogWebView.transState.toInt() == 3 && dialogWebView.transOn,
                    dialogWebView.EditAreaApplied()
                )
                dialogWebView.editAreaDialog = editAreaDialog
                editAreaDialog.setOnDismissListener(dialogWebView.EditAreaDialogDismiss())
            }
        }

        override fun onReceiveValue(value: String?) {
            val pendingJs = value
            val dialogWebView = this@DialogWebView
            if (dialogWebView.areaView != null) {
                dialogWebView.pendingJs = pendingJs
                val myDialogRelative = dialogWebView.dialogFrame
                if (myDialogRelative == null) {
                    return
                }
                myDialogRelative.post(ApplyAreaTextStep())
            }
        }
    }

    internal inner class GuideAreaListener : DialogAreaListener {
        override fun onPick() {
        }

        override fun onHelp() {
            val dialogWebView = this@DialogWebView
            val dialogGuideArea: DialogGuideArea? = if (dialogWebView.hostActivity == null) null else dialogWebView.transGuideDialog
            if (dialogWebView.hostActivity == null || dialogGuideArea != null) {
                return
            }
            val transGuideDialog = DialogGuideArea(dialogWebView.hostActivity, 1, null)
            dialogWebView.transGuideDialog = transGuideDialog
            transGuideDialog.setOnDismissListener(dialogWebView.TransGuideDialogDismiss())
        }
    }

    internal inner class AreaGuideDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogWebView = this@DialogWebView
            val dialogGuideArea = dialogWebView.areaGuideDialog
            if (dialogGuideArea != null) {
                dialogGuideArea.dismiss()
                dialogWebView.areaGuideDialog = null
            }
        }
    }

    internal inner class TransGuideDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogWebView = this@DialogWebView
            val dialogGuideArea = dialogWebView.transGuideDialog
            if (dialogGuideArea != null) {
                dialogGuideArea.dismiss()
                dialogWebView.transGuideDialog = null
            }
        }
    }

    internal inner class EditAreaApplied : EditAreaListener {
        override fun onApply(host: String?, cmd: String?, script: String?) {
            if (TextUtils.isEmpty(cmd)) {
                return
            }
            val dialogWebView = this@DialogWebView
            // the element picker saved a user rule: the uBlock Origin-compatible engine picks it up
            com.mycompany.app.web.filter.UboFilter.onListsChanged(dialogWebView.appCtx)
            evalJs(dialogWebView.page, script, true)
        }
    }

    internal inner class EditAreaDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogWebView = this@DialogWebView
            val dialogEditArea = dialogWebView.editAreaDialog
            if (dialogEditArea != null) {
                dialogEditArea.dismiss()
                dialogWebView.editAreaDialog = null
            }
        }
    }

    internal inner class ScrollToStep : MyScrollBar.ScrollBarListener {
        override fun scrollTo(position: Int) {
            val webNestView = this@DialogWebView.page
            if (webNestView == null) {
                return
            }
            webNestView.scrollTo(0, position)
        }

        override fun scrollPos(): Int {
            val webNestView = this@DialogWebView.page
            if (webNestView == null) {
                return 0
            }
            return webNestView.scrollTop
        }

        override fun onDragEnd() {
        }

        override fun scrollRange(): Int {
            val webNestView = this@DialogWebView.page
            if (webNestView == null) {
                return 0
            }
            return webNestView.scrollRange
        }

        override fun scrollExtent(): Int {
            val webNestView = this@DialogWebView.page
            if (webNestView == null) {
                return 0
            }
            return webNestView.scrollExtent
        }
    }

    internal inner class CloseClick : View.OnClickListener {
        override fun onClick(view: View?) {
            this@DialogWebView.dismiss()
        }
    }

    internal inner class ActionClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogWebView = this@DialogWebView
            val dialogWebListener = dialogWebView.webListener
            val webNestView: WebNestView? = if (dialogWebListener == null) null else dialogWebView.page
            if (dialogWebListener == null || webNestView == null) {
                return
            }
            if (dialogWebView.urlMode.toInt() != 1) {
                dialogWebListener.onAction(3, dialogWebView.pageUrl, webNestView.getTitle())
                return
            }
            if (dialogWebView.clickedOnce) {
                return
            }
            dialogWebView.clickedOnce = true
            try {
                dialogWebView.webFrameView!!.removeView(webNestView)
                val page = dialogWebView.page
                dialogWebView.page = null
                dialogWebView.webListener!!.onNewWindow(page, page!!.getUrl())
            } catch (unused: Exception) {
            }
            dialogWebView.clickedOnce = false
        }
    }

    interface DialogWebListener {
        fun onAction(action: Int, url: String?, label: String?)

        fun onClosed()

        fun onDownload(url: String?, userAgent: String?, mimeType: String?, size: Long)

        fun onNewWindow(webNestView: WebNestView?, url: String?)

        fun onShown()

        fun onHidden()
    }

    private inner class LocalChromeClient : WebChromeClient() {
        /**
         * Watches page console errors: an uncaught "borderRadius" error means the area-select script broke (handled
         * once via AreaScriptTask); a refused Google Translate script/stylesheet (CSP) while translating
         * switches the translation to mode 2 and reloads it (E).
         */
        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
            val dialog = this@DialogWebView
            val message = if (consoleMessage == null) null else consoleMessage.message()
            if (dialog.areaView != null && !TextUtils.isEmpty(message) && message!!.startsWith("Uncaught")
                && message.indexOf("borderRadius", 8) != -1
            ) {
                if (dialog.areaView != null && !dialog.transRunning) {
                    dialog.transRunning = true
                    dialog.runInBackground(AreaScriptTask())
                }
            } else if (dialog.confirmPending && !TextUtils.isEmpty(message)) {
                val at = message!!.indexOf("Refused to load the s")
                if (at != -1 && (message.startsWith(
                        "cript",
                        at + 21
                    ) || message.startsWith("tylesheet", at + 21))
                    && message.indexOf("https://translate.google", at + 27) != -1
                ) {
                    dialog.transState = 2
                    dialog.transOn = false
                    dialog.transLang = null
                    dialog.transPick = null
                    dialog.hideCover()
                }
            }
            return super.onConsoleMessage(consoleMessage)
        }

        override fun onJsAlert(
            webView: WebView?,
            url: String?,
            message: String?,
            result: JsResult?
        ): Boolean {
            if (result == null) {
                return false
            }
            val dialogWebView = this@DialogWebView
            if (dialogWebView.areaView != null) {
                result.cancel()
                return true
            }
            if (TextUtils.isEmpty(message) || !message!!.startsWith("sb:")) {
                return false
            }
            if (message == "sb:ads_preview") {
                val webNestView = dialogWebView.page
                if (webNestView == null) {
                    return false
                }
                dialogWebView.prevUrl = dialogWebView.pageUrl
                dialogWebView.urlBlocked = true
                webNestView.prepareReload()
            } else if (message == "sb:ads_open") {
                val webNestView = dialogWebView.page
                if (webNestView == null) {
                    return false
                }
                dialogWebView.prevUrl = dialogWebView.pageUrl
                dialogWebView.urlBlocked = true
                webNestView.prepareReload()
            } else {
                val dialogWebListener: DialogWebListener? = if (message != "sb:link_setting") null else dialogWebView.webListener
                if (message != "sb:link_setting" || dialogWebListener == null) {
                    return false
                }
                dialogWebListener.onAction(11, dialogWebView.pageUrl, null)
            }
            result.confirm()
            return true
        }

        override fun onJsConfirm(
            webView: WebView?,
            url: String?,
            message: String?,
            result: JsResult?
        ): Boolean {
            if (result == null) {
                return false
            }
            if (this@DialogWebView.areaView == null) {
                return super.onJsConfirm(webView, url, message, result)
            }
            result.cancel()
            return true
        }

        override fun onJsPrompt(
            webView: WebView?,
            url: String?,
            message: String?,
            defaultValue: String?,
            result: JsPromptResult?
        ): Boolean {
            if (result == null) {
                return false
            }
            if (this@DialogWebView.areaView == null) {
                return super.onJsPrompt(webView, url, message, defaultValue, result)
            }
            result.cancel()
            return true
        }

        override fun onProgressChanged(webView: WebView?, newProgress: Int) {
            val dialogWebView = this@DialogWebView
            if (dialogWebView.page == null) {
                return
            }
            dialogWebView.setProgress(newProgress)
        }
    }

    private inner class WebAppInterface {

        inner class PageReadyStep : Runnable {
            override fun run() {
                var webNestView: WebNestView? = null
                val dialogWebView = this@DialogWebView
                if (dialogWebView.appCtx != null) {
                    dialogWebView.hideCover()
                    val view: View? = if (dialogWebView.dialogFrame != null) dialogWebView.badgeView else null
                    if (dialogWebView.dialogFrame != null && view != null) {
                        val layoutParams: FrameLayout.LayoutParams? = if (ZoneSettings.showUpPos.value == 3) (view.getLayoutParams() as FrameLayout.LayoutParams?) else null
                        if (ZoneSettings.showUpPos.value == 3 && layoutParams != null) {
                            layoutParams.gravity = 83
                        }
                        val badgeView = dialogWebView.badgeView
                        if (badgeView != null) {
                            if (AppState.darkUi) {
                                badgeView.setBackgroundResource(AppR.drawable.trans_logo_short_back_dark)
                            } else {
                                badgeView.setBackgroundResource(AppR.drawable.trans_logo_short_back_color)
                            }
                        }
                        if (dialogWebView.transState.toInt() != 3 || !dialogWebView.transOn) {
                            dialogWebView.badgeView!!.setVisibility(View.GONE)
                        } else if (dialogWebView.badgeView!!.getVisibility() != View.VISIBLE) {
                            dialogWebView.scrollShift = 0
                            dialogWebView.badgeView!!.setAlpha(1.0f)
                            dialogWebView.badgeView!!.setTranslationY(0.0f)
                            dialogWebView.badgeView!!.setVisibility(View.VISIBLE)
                        }
                    }
                    val webTransControl = dialogWebView.transControl
                    if (webTransControl != null) {
                        webTransControl.showState(
                            dialogWebView.transLang,
                            dialogWebView.transOn,
                            dialogWebView.transState.toInt()
                        )
                    }
                    if (dialogWebView.transState.toInt() != 1) {
                        val transPick = dialogWebView.transPick
                        dialogWebView.transPick = null
                        if (!TextUtils.isEmpty(transPick)) {
                            dialogWebView.confirmPending = true
                            if (!TextUtils.isEmpty(transPick)) {
                                dialogWebView.runInBackground(EvalLongPressJsTask(dialogWebView, transPick))
                            }
                        } else if (!TextUtils.isEmpty(AlbumSettings.transPick.value) && (dialogWebView.page.also {
                                webNestView = it
                            }) != null) {
                            webNestView!!.evaluateJavascript(
                                "document.cookie",
                                dialogWebView.TransLangFromCookieCallback()
                            )
                        }
                        val context = dialogWebView.appCtx
                        if (context != null && !DataTrans.instance(context).hasLanguages()) {
                            injectTransJs(dialogWebView.page)
                        }
                        if (TextUtils.isEmpty(dialogWebView.detectedLang)) {
                            checkTransClassJs(dialogWebView.page)
                        }
                    }
                }
            }
        }

        @JavascriptInterface
        fun onJsResult(callback: String?, jsResultY: String?) {
            if (TextUtils.isEmpty(callback)) {
                return
            }
            val isTransUser = callback == "onTransUser"
            val dialogWebView = this@DialogWebView
            if (isTransUser) {
                if ("1" == jsResultY) {
                    dialogWebView.transState = 2.toByte()
                    dialogWebView.transOn = false
                    dialogWebView.transLang = null
                    dialogWebView.transPick = null
                    dialogWebView.hideCover()
                    return
                }
                val transExists = "2" == jsResultY
                val myCoverView = dialogWebView.loadCover
                if (myCoverView != null) {
                    myCoverView.post(dialogWebView.ShowCoverTask())
                }
                dialogWebView.runInBackground(EvalJsTask(dialogWebView, transExists))
                return
            }
            if (callback == "onTransList") {
                if (dialogWebView.page == null || TextUtils.isEmpty(jsResultY)) {
                    return
                }
                dialogWebView.jsResultY = jsResultY
                dialogWebView.runInBackground(dialogWebView.ParseTransResultTask())
                return
            }
            if (callback == "onTransClass") {
                if (dialogWebView.page != null) {
                    if ("0" == jsResultY) {
                        dialogWebView.detectedLang = "-"
                        return
                    }
                    if (TextUtils.isEmpty(jsResultY)) {
                        dialogWebView.detectedLang = "-"
                    } else if (TextUtils.isEmpty(dialogWebView.detectedLang)) {
                        dialogWebView.jsResultZ = jsResultY
                        dialogWebView.runInBackground(dialogWebView.LangPrefixTask())
                    }
                }
            }
        }

        @JavascriptInterface
        fun onObserDet(transLang: String?, transOn: Int) {
            val dialogWebView = this@DialogWebView
            if (transOn == 0) {
                dialogWebView.transState = 1.toByte()
            } else {
                dialogWebView.transState = 3.toByte()
                dialogWebView.transOn = transOn == 2
                dialogWebView.transLang = transLang
                if (TextUtils.isEmpty(AlbumSettings.transPick.value)) {
                    AlbumSettings.editLocal { set(AlbumSettings.transPick, transLang) }
                    AlbumSettings.editLocal { set(AlbumSettings.transCode, "") }
                    PrefAlbum.saveTrans(dialogWebView.appCtx)
                }
                if (MainUtil.sameObject(dialogWebView.transPick, transLang)) {
                    dialogWebView.transPick = null
                }
            }
            val handler = dialogWebView.mainHandler
            if (handler == null) {
                return
            }
            handler.post(PageReadyStep())
        }
    }

    fun loadPage(pageUrl: String?) {
        if (this.page != null) {
            val isHttp = isHttpUrl(pageUrl)
            val loadPending = this.loadPending
            if (isHttp) {
                if (loadPending) {
                    this.loadPending = false
                    val webNestView = this.page
                    if (webNestView != null) {
                        webNestView.post(RemoveJsBridgeTask())
                        return
                    }
                    return
                }
                return
            }
            if (loadPending) {
                return
            }
            this.loadPending = true
            val webNestView = this.page
            if (webNestView == null) {
                return
            }
            webNestView.post(AddJsBridgeTask())
        }
    }

    fun updateUpView() {
        val webUpView: WebUpView? = if (ZoneSettings.showUpPos.value == 0) null else this.upView
        if (ZoneSettings.showUpPos.value == 0 || webUpView == null) {
            return
        }
        if (!this.upViewShown) {
            webUpView.hide()
            return
        }
        val scrollY = this.page!!.getScrollY()
        val height = this.webFrameView!!.getHeight()
        val upView = this.upView
        if (scrollY <= height) {
            upView!!.hide()
        } else {
            upView!!.scheduleHide()
            upView.setVisibility(View.VISIBLE)
        }
    }

    fun clearArea(confirm: Boolean) {
        if (this.areaView != null) {
            if (confirm) {
                evalJs(
                    this.page,
                    "(function(){window.sblast=null;window.sbchkc=false;myClr();document.removeEventListener('touchstart',myTc1);document.removeEventListener('touchmove',myTc2);document.removeEventListener('touchend',myTc3);})();",
                    true
                )
            }
            val myDialogRelative = this.dialogFrame
            if (myDialogRelative == null) {
                return
            }
            myDialogRelative.post(ReleaseTask())
        }
    }

    fun dismissTransLang() {
        val dialogTransLang = this.transLangDialog
        if (dialogTransLang != null) {
            dialogTransLang.dismiss()
            this.transLangDialog = null
        }
    }

    fun hideCover() {
        val myCoverView = this.loadCover
        if (myCoverView == null) {
            return
        }
        myCoverView.post(HideCoverTask())
    }

    fun stopTrans() {
        val webTransControl = this.transControl
        if (webTransControl != null) {
            webTransControl.animateShow(false)
        }
    }

    fun hasDialog(): Boolean {
        return if (this.transLangDialog == null && this.areaGuideDialog == null && this.transGuideDialog == null && this.editAreaDialog == null && this.transControl == null) false else true
    }

    fun updateUrlText() {
        var shownUrl: String?
        if (this.urlEdit == null || this.page == null) {
            return
        }
        clearArea(true)
        if (this.urlMode.toInt() == 3) {
            shownUrl = this.pageUrl
        } else {
            shownUrl = trimSpaceSlash(this.urlEdit!!.str())
            if (TextUtils.isEmpty(shownUrl)) {
                shownUrl = this.pageUrl
                if (TextUtils.isEmpty(shownUrl)) {
                    showToast(this.appCtx, AppR.string.empty)
                    this.progressVis!!.setVisibility(0)
                    this.stopVis!!.setVisibility(8)
                    return
                }
            }
        }
        val myEditPure = this.urlEdit
        if (myEditPure != null) {
            myEditPure.clearFocus()
            closeKeyboard(this.appCtx, window?.decorView)
        }
        val samePage = MainUtil.sameObject(shownUrl, this.pageUrl)
        val webNestView = this.page
        if (samePage) {
            webNestView!!.prepareReload()
        } else {
            webNestView!!.openUrl(MainUtil.normalizeUrl(null, shownUrl), null)
        }
    }

    fun pausePage() {
        val webNestView = this.page
        if (webNestView != null) {
            webNestView.pauseOnce()
        }
    }

    fun removeTransFrame() {
        val frameLayout = this.transFrame
        val webTransControl = this.transControl
        this.transFrame = null
        this.transControl = null
        if (frameLayout != null) {
            try {
                val myDialogRelative = this.dialogFrame
                if (myDialogRelative != null) {
                    myDialogRelative.removeView(frameLayout)
                }
            } catch (unused: Exception) {
                return
            }
        }
        if (webTransControl != null) {
            webTransControl.release()
        }
    }

    fun resumePage() {
        val webNestView = this.page
        if (webNestView != null) {
            webNestView.onResume()
        }
    }

    fun confirmArea(confirm: Boolean) {
        this.confirmPending = !confirm
        val webNestView = this.page
        if (confirm) {
            evalConfirmJs(webNestView, "restore")
        } else {
            evalConfirmJs(webNestView, "confirm")
        }
    }

    fun startTrans(transPick: String?) {
        this.confirmPending = true
        if (TextUtils.isEmpty(transPick)) {
            this.transPick = AlbumSettings.transPick.value
        } else {
            this.transPick = transPick
        }
        checkTransJs(this.page, "onTransUser")
    }

    fun startNavi(start: Boolean, animate: Boolean) {
        if (this.swipeState.toInt() != 0 || this.page == null) {
            return
        }
        this.swipeState = 2.toByte()
        this.swipeDir = 0.toByte()
        this.swipeEdge = 0.toByte()
        this.swipeMoved = false
        this.moveCountX = 0
        this.moveCountY = 0
        this.swipeStarted = false
        if (!start) {
            val myScrollNavi = this.naviLeft
            if (myScrollNavi != null) {
                myScrollNavi.hide()
            }
            val naviRight = this.naviRight
            if (naviRight != null) {
                naviRight.hide()
                return
            }
            return
        }
        val naviLeft = this.naviLeft
        if (this.layoutRtl) {
            if (animate) {
                if (naviLeft != null) {
                    naviLeft.hide()
                }
                val myScrollNavi = this.naviRight
                if (myScrollNavi != null) {
                    myScrollNavi.hide()
                    return
                }
                return
            }
            if (naviLeft != null) {
                naviLeft.hide()
            }
            val myScrollNavi = this.naviRight
            if (myScrollNavi != null) {
                myScrollNavi.hide()
                return
            }
            return
        }
        if (animate) {
            if (naviLeft != null) {
                naviLeft.hide()
            }
            val myScrollNavi = this.naviRight
            if (myScrollNavi != null) {
                myScrollNavi.hide()
                return
            }
            return
        }
        if (naviLeft != null) {
            naviLeft.hide()
        }
        val myScrollNavi = this.naviRight
        if (myScrollNavi != null) {
            myScrollNavi.hide()
        }
    }

    fun navForward() {
        startNavi(true, false)
        val webNestView = this.page
        if (webNestView == null || !webNestView.canGoForward()) {
            return
        }
        this.page!!.goForward()
    }

    fun handleUrl(webNestView: WebNestView?, source: String?): Boolean {
        var source = source
        if (this.areaView == null) {
            if (webNestView == null || TextUtils.isEmpty(source)) {
                return false
            }
            if (source!!.startsWith("http")) {
                if (!source.startsWith("://", 4) && !source.startsWith("s://", 4)) {
                    try {
                        source = URLDecoder.decode(source, "UTF-8")
                    } catch (unused: Exception) {
                    }
                }
                val sourceUrl = source
                if (URLUtil.isNetworkUrl(sourceUrl) && sourceUrl!!.endsWith(".pdf")) {
                    val dialogWebListener = this.webListener
                    if (dialogWebListener != null) {
                        dialogWebListener.onDownload(sourceUrl, null, "application/pdf", 0L)
                    }
                } else {
                    source = sourceUrl
                }
            } else if (source.startsWith("tel:") || source.startsWith("mailto:") || source.startsWith("sms:")) {
                MainUtil.openIntentUri(this.hostActivity, source)
                return true
            }
            if (hasKnownScheme(source)) {
                return false
            }
            val intentUrl = intentToUrl(source, true)
            if (TextUtils.isEmpty(intentUrl)) {
                return false
            }
            webNestView.openUrl(intentUrl, null)
            return true
        }
        val myProgressBar = this.progressBar
        if (myProgressBar != null && !myProgressBar.drawSkipped) {
            setProgress(100)
            return true
        }
        return true
    }

    fun navBack() {
        startNavi(true, true)
        val webNestView = this.page
        if (webNestView == null || !webNestView.canGoBack()) {
            return
        }
        this.page!!.goBack()
    }

    fun setProgress(newProgressValue: Int) {
        this.progressValue = newProgressValue
        val myProgressBar = this.progressBar
        if (myProgressBar != null) {
            if (this.areaView != null) {
                myProgressBar.setSkipDraw(true)
                this.progressVis!!.setVisibility(0)
                this.stopVis!!.setVisibility(8)
                return
            }
            val progressValue = Math.round(myProgressBar.progress)
            if (newProgressValue == 100 && progressValue == 100) {
                this.progressBar!!.setSkipDraw(true)
                this.progressVis!!.setVisibility(0)
                this.stopVis!!.setVisibility(8)
                return
            }
            this.progressVis!!.setVisibility(8)
            this.stopVis!!.setVisibility(0)
            val progressBar = this.progressBar
            if (progressBar!!.drawSkipped) {
                progressBar.progress = 0.0f
                this.progressBar!!.setSkipDraw(false)
                setProgress(max(newProgressValue, 50))
            } else if (progressValue < newProgressValue) {
                progressBar.progress = (progressValue + 3).toFloat()
                val runnable = this.progressRunnable
                if (runnable != null) {
                    this.progressBar!!.removeCallbacks(runnable)
                    this.progressBar!!.post(runnable)
                }
            }
        }
    }

    fun applyAreaStyle() {
        val webNestView = this.page
        if (webNestView != null) {
            webNestView.applyPageStyle(true, -1, null)
            if (this.page != null) {
                val areaText = this.areaText
                if (TextUtils.isEmpty(areaText) || areaText != MainUtil.stripHostPrefix(this.pageHost)) {
                    return
                }
                evalJsNow(this.page, this.areaPreviewJs, true)
            }
        }
    }

    override fun dismiss() {
        if (this.dismissed) {
            return
        }
        this.dismissed = true
        if (this.appCtx != null) {
            dismissTransLang()
            this.areaGuideDialog?.dismiss()
            this.areaGuideDialog = null
            this.transGuideDialog?.dismiss()
            this.transGuideDialog = null
            this.editAreaDialog?.dismiss()
            this.editAreaDialog = null
            this.moreMenu?.dismissNow()
            this.moreMenu = null
            val webNestView = this.page
            if (webNestView != null) {
                destroyWebView(webNestView, true)
                this.page = null
            }
            removeTransFrame()
            this.dialogFrame?.release()
            this.dialogFrame = null
            this.progressBar?.release()
            this.progressBar = null
            this.scrollBarView?.releaseAll()
            this.scrollBarView = null
            this.upView?.release()
            this.upView = null
            this.naviLeft?.release()
            this.naviLeft = null
            this.naviRight?.release()
            this.naviRight = null
            this.loadCover?.hide()
            this.loadCover = null
            this.badgeView = null
            this.transAnchor = null
            this.transLang = null
            this.transPick = null
            this.detectedLang = null
            this.hostActivity = null
            this.appCtx = null
            this.webListener = null
            this.pageHost = null
            this.referer = null
            this.prevUrl = null
            this.historyUrl = null
            this.webFrameView = null
            this.gestureDetect = null
            this.areaText = null
            this.areaPreviewJs = null
            this.notiListener = null
            this.cleanEngine = null
        }
        super.dismiss()
    }

    /**
     * Edge-swipe navigation: a touch starting at the left/right edge zone (ZoneSettings.portAreaLeft.value/S widths) or, when enabled,
     * anywhere (swipe state 3) and moving horizontally drags the back/forward indicator (NAVL/NAVR); releasing past the
     * threshold goes back or forward (mirrored for RTL, A). Vertical scrolling or a page that scrolls
     * horizontally cancels it. Always passes the event on.
     */
    public override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (this.gestureDetect != null) {
            this.gestureDetect!!.onTouchEvent(ev)
        }
        val action = ev.getActionMasked()
        if (action == 0) {
            swipeDown(ev)
        } else if (action == 1 || action == 3) {
            swipeUp(this.layoutRtl)
        } else if (action == 2) {
            swipeMove(ev, this.layoutRtl)
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun swipeReset() {
        this.swipeState = 2
        this.swipeDir = 0
        this.swipeEdge = 0
        this.swipeMoved = false
        this.moveCountX = 0
        this.moveCountY = 0
        this.swipeStarted = false
    }

    private fun swipeDown(ev: MotionEvent?) {
        swipeReset()
        if (this.page == null) {
            return
        }
        this.touchX = ev!!.getX()
        this.touchY = ev.getY()
        this.lastY = this.touchY
        val left = ZoneSettings.portAreaLeft.value
        if (this.touchX < MainApp.dp10 + left) {
            if (this.touchX < left && ZoneSettings.gesLeft.value == 0) {
                this.swipeState = 0
                this.swipeDir = 1
                this.swipeMoved = true
            }
            return
        }
        val right = ZoneSettings.portAreaRight.value
        if (this.touchX > this.dialogFrame!!.getWidth() - (MainApp.dp10 + right)) {
            if (this.touchX > this.dialogFrame!!.getWidth() - right && ZoneSettings.gesRight.value == 0) {
                this.swipeState = 0
                this.swipeDir = 2
                this.swipeMoved = true
            }
            return
        }
        if (ZoneSettings.gesCenter.value == 0) {
            this.swipeState = 0
            this.swipeDir = 3
            this.swipeMoved = TextUtils.isEmpty(this.pageUrl)
        }
    }

    private fun swipeCancel() {
        this.swipeState = 2
        this.swipeDir = 0
    }

    private fun swipeMove(ev: MotionEvent?, rtl: Boolean) {
        val touchX = ev!!.getX()
        val lastY = ev.getY()
        val slop = MainApp.dp10.toFloat()
        if (lastY - this.lastY > slop) {
            this.lastY = lastY
            this.upViewShown = !(this.scrollBarView != null && this.scrollBarView!!.dragging)
        } else if (this.lastY - lastY > slop) {
            this.lastY = lastY
            this.upViewShown = this.scrollBarView != null && this.scrollBarView!!.dragging
        }
        if (this.swipeDir.toInt() == 0) {
            return
        }
        val touch = this.touchSlop
        if (this.swipeEdge.toInt() == 0) {
            val web = this.page
            if (web == null) {
                return
            }
            if (!this.swipeMoved) {
                if (web.hasScrolled) {
                    if (this.swipeDir.toInt() == 3) {
                        swipeCancel()
                        return
                    }
                    this.moveCountY++
                    if (this.moveCountY > 2) {
                        swipeCancel()
                        return
                    }
                }
            } else if (this.scrollBarView != null && this.scrollBarView!!.dragging) {
                swipeCancel()
                return
            }
            if (!this.swipeStarted) {
                if (this.swipeMoved) {
                    if (abs(touchX - this.touchX) > touch) {
                        this.swipeStarted = true
                        this.moveCountY = 0
                    }
                } else {
                    val moves: Int
                    if (web.isOverScrolled()) {
                        this.moveCountX++
                        this.moveCountY = 0
                        moves = this.moveCountX
                    } else {
                        this.moveCountX = 0
                        moves = 0
                    }
                    this.swipeStarted = if (this.swipeDir.toInt() == 3) moves > 2 else moves > 0
                }
            } else {
                if (touchX > this.touchX) {
                    if ((this.swipeDir.toInt() == 1 || this.swipeDir.toInt() == 3) && this.swipeState.toInt() == 0 && !(if (rtl) web.canGoForward() else web.canGoBack())) {
                        swipeCancel()
                        return
                    }
                } else if ((this.swipeDir.toInt() == 2 || this.swipeDir.toInt() == 3) && this.swipeState.toInt() == 0 && !(if (rtl) web.canGoBack() else web.canGoForward())) {
                    swipeCancel()
                    return
                }
                this.swipeEdge = (if (touchX > this.touchX) 1 else 2).toByte()
                this.touchX = touchX
                this.touchY = lastY
            }
        }
        if (this.swipeEdge.toInt() == 0 || this.page == null) {
            return
        }
        this.page!!.swipeTouch = true
        if (this.swipeState.toInt() != 0) {
            return
        }
        if (this.swipeEdge.toInt() == 1) {
            if (this.naviLeft != null && (this.naviLeft!!.isIdle() || (touchX > this.touchX && abs(lastY - this.touchY) < touch))) {
                this.naviLeft!!.onScroll(this.touchX, touchX)
            }
        } else if (this.naviRight != null && (this.naviRight!!.isIdle() || (touchX < this.touchX && abs(lastY - this.touchY) < touch))) {
            this.naviRight!!.onScroll(this.touchX, touchX)
        }
    }

    private fun swipeUp(rtl: Boolean) {
        if (this.page == null) {
            return
        }
        if (this.swipeState.toInt() == 0) {
            if (this.swipeEdge.toInt() == 1 && this.naviLeft != null && this.naviLeft!!.isOut()) {
                if (rtl) {
                    this.navForward()
                } else {
                    this.navBack()
                }
            } else if (this.swipeEdge.toInt() == 2 && this.naviRight != null && this.naviRight!!.isOut()) {
                if (rtl) {
                    this.navBack()
                } else {
                    this.navForward()
                }
            } else {
                this.startNavi(false, false)
            }
        }
        swipeReset()
        if (this.naviLeft != null) {
            this.naviLeft!!.hide()
        }
        if (this.naviRight != null) {
            this.naviRight!!.hide()
        }
        if (this.scrollBarView != null) {
            this.scrollBarView!!.restartTimer()
        }
    }

        fun stopTransIfAny() {
        if (this.transControl != null) {
            stopTrans()
            return
        }
        if (this.areaView != null) {
            clearArea(true)
            return
        }
        val webNestView = this.page
        if (webNestView == null || !webNestView.canGoBack()) {
            dismiss()
        } else {
            this.page!!.goBack()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (menuOpen) {
            menuOpen = false
            return
        }
        stopTransIfAny()
    }

    private inner class LocalWebViewClient : WebViewClient() {

        inner class PageStyleTask : Runnable {
            override fun run() {
                val dialogWebView = this@DialogWebView
                dialogWebView.applyAreaStyle()
                val webNestView: WebNestView? = if (BrowserSettings.adsBlock.value) dialogWebView.page else null
                if (BrowserSettings.adsBlock.value && webNestView != null) {
                    webNestView.startAdsClean(dialogWebView.pageUrl, dialogWebView.pageHost, false)
                }
                dialogWebView.clearArea(false)
            }
        }

        inner class UpdateUpViewTask : Runnable {
            override fun run() {
                val dialogWebView = this@DialogWebView
                dialogWebView.updateUpView()
            }
        }

        inner class ApplyAreaStyleTask : Runnable {
            override fun run() {
                val dialogWebView = this@DialogWebView
                dialogWebView.applyAreaStyle()
                val webNestView: WebNestView? = if (!BrowserSettings.adsBlock.value) null else dialogWebView.page
                if (!BrowserSettings.adsBlock.value || webNestView == null) {
                    return
                }
                webNestView.startAdsClean(dialogWebView.pageUrl, dialogWebView.pageHost, false)
            }
        }

        inner class UpdateUpViewStep : Runnable {
            override fun run() {
                val dialogWebView = this@DialogWebView
                dialogWebView.updateUpView()
            }
        }

        inner class ApplyAreaStyleAgainTask : Runnable {
            override fun run() {
                val dialogWebView = this@DialogWebView
                dialogWebView.applyAreaStyle()
            }
        }

        override fun doUpdateVisitedHistory(webView: WebView?, url: String?, isReload: Boolean) {
            val dialogWebView = this@DialogWebView
            var found = false
            if (!MainUtil.sameObject(dialogWebView.pageUrl, url)) {
                val youTubeId = if (MainUtil.isYouTubeUrl(url) && MainUtil.isYouTubeWatch(url) && !MainUtil.isYouTubeShorts(url)) MainUtil.youTubeIdOf(url) else null
                if (!TextUtils.isEmpty(youTubeId) && youTubeId != dialogWebView.historyUrl) {
                    found = true
                }
                dialogWebView.historyUrl = youTubeId
            }
            dialogWebView.pageUrl = url
            dialogWebView.pageHost = hostOfUrl(url, true)
            val myEditPure: Edit? = if (dialogWebView.urlMode.toInt() != 3) dialogWebView.urlEdit else null
            if (dialogWebView.urlMode.toInt() != 3 && myEditPure != null && !myEditPure.isFocused()) {
                dialogWebView.urlEdit!!.setText(dialogWebView.pageUrl)
            }
            if (found) {
                val webNestView = dialogWebView.page
                if (webNestView == null) {
                    return
                }
                webNestView.reload()
                return
            }
            if (dialogWebView.confirmPending) {
                dialogWebView.hideCover()
                dialogWebView.startTrans(null)
            }
            dialogWebView.runInBackground(ApplyAreaStyleAgainTask())
        }

        override fun onPageFinished(webView: WebView?, url: String?) {
            val dialogWebView = this@DialogWebView
            val webNestView = dialogWebView.page
            if (webNestView == null) {
                return
            }
            webNestView.setWebLoading(false)
            dialogWebView.loadPage(url)
            if (!dialogWebView.finishHandled && !TextUtils.isEmpty(url) && (url != dialogWebView.lastFinishedUrl)) {
                dialogWebView.finishHandled = true
                dialogWebView.lastFinishedUrl = url
                val handler = dialogWebView.mainHandler
                if (handler != null) {
                    handler.post(dialogWebView.ClearWebCacheTask())
                }
            }
            dialogWebView.pageUrl = url
            dialogWebView.pageHost = hostOfUrl(url, true)
            dialogWebView.runInBackground(ApplyAreaStyleTask())
            if (BrowserSettings.enableJs.value) {
                dialogWebView.page!!.applyJsBlock(dialogWebView.pageUrl, dialogWebView.pageHost, true)
            }
            val myEditPure: Edit? = if (dialogWebView.urlMode.toInt() != 3) dialogWebView.urlEdit else null
            if (dialogWebView.urlMode.toInt() != 3 && myEditPure != null && !myEditPure.isFocused()) {
                dialogWebView.urlEdit!!.setText(dialogWebView.pageUrl)
            }
            if (dialogWebView.confirmPending) {
                dialogWebView.hideCover()
                dialogWebView.startTrans(null)
            }
            dialogWebView.page!!.post(UpdateUpViewStep())
        }

        override fun onPageStarted(webView: WebView?, url: String?, bitmap: Bitmap?) {
            val dialogWebView = this@DialogWebView
            val webNestView = dialogWebView.page
            if (webNestView == null) {
                return
            }
            webNestView.setWebLoading(true)
            dialogWebView.loadPage(url)
            dialogWebView.pageUrl = url
            dialogWebView.pageHost = hostOfUrl(url, true)
            dialogWebView.runInBackground(PageStyleTask())
            if (BrowserSettings.enableJs.value) {
                dialogWebView.page!!.applyJsBlock(dialogWebView.pageUrl, dialogWebView.pageHost, false)
            }
            val myEditPure: Edit? = if (dialogWebView.urlMode.toInt() != 3) dialogWebView.urlEdit else null
            if (dialogWebView.urlMode.toInt() != 3 && myEditPure != null && !myEditPure.isFocused()) {
                dialogWebView.urlEdit!!.setText(dialogWebView.pageUrl)
            }
            dialogWebView.page!!.post(UpdateUpViewTask())
        }

        override fun onRenderProcessGone(
            webView: WebView?,
            renderProcessGoneDetail: RenderProcessGoneDetail?
        ): Boolean {
            val dialogWebView = this@DialogWebView
            dialogWebView.page = null
            onRenderGone(webView, renderProcessGoneDetail)
            val handler = dialogWebView.mainHandler
            if (handler == null) {
                return true
            }
            handler.post(dialogWebView.DismissTask())
            return true
        }

        override fun shouldInterceptRequest(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): WebResourceResponse? {
            val dialogWebView = this@DialogWebView
            if (dialogWebView.page == null || webResourceRequest == null || webResourceRequest.getUrl() == null) {
                return null
            }
            val url = webResourceRequest.getUrl().toString()
            dialogWebView.loadPage(url)
            val webResourceResponseU: WebResourceResponse? = if (ZoneSettings.userFont.value) MainUtil.userFontResponse(dialogWebView.appCtx, url) else null
            if (ZoneSettings.userFont.value && webResourceResponseU != null) {
                return webResourceResponseU
            }
            if (!BrowserSettings.adsBlock.value) {
                return null
            }
            if (!MainUtil.sameObject(dialogWebView.prevUrl, dialogWebView.pageUrl)) {
                dialogWebView.prevUrl = dialogWebView.pageUrl
                dialogWebView.urlBlocked =
                    DataBookAds.instance(dialogWebView.appCtx).hasSiteOf(dialogWebView.pageUrl, dialogWebView.pageHost)
            }
            val webClean: WebClean? = if (dialogWebView.urlBlocked) null else dialogWebView.cleanEngine
            val webResourceResponseJ: WebResourceResponse? = if (dialogWebView.urlBlocked || webClean == null) null else webClean.intercept(
                    dialogWebView.appCtx,
                    webView,
                    webResourceRequest,
                    dialogWebView.pageUrl,
                    dialogWebView.pageHost,
                    url,
                    0
                )
            if (dialogWebView.urlBlocked || webClean == null || webResourceResponseJ == null) {
                return null
            }
            return webResourceResponseJ
        }

        override fun shouldOverrideUrlLoading(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): Boolean {
            val dialogWebView = this@DialogWebView
            if (dialogWebView.page == null || webResourceRequest == null || webResourceRequest.getUrl() == null) {
                return true
            }
            val source = webResourceRequest.getUrl().toString()
            if (TextUtils.isEmpty(source)) {
                return true
            }
            dialogWebView.loadPage(source)
            return dialogWebView.handleUrl(dialogWebView.page, source)
        }
    }

    companion object {
    }
}
