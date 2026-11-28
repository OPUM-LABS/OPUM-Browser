@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon")

package com.mycompany.app.web

import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.pref.PrefPdf.Companion.get
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.main.MainUtil.hasChar
import com.mycompany.app.main.MainUtil.githubRawToRepo
import com.mycompany.app.main.MainUtil.wildcardHost
import com.mycompany.app.main.MainUtil.isFilmizle
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.data.book.DataBookLink.Companion.instance
import com.mycompany.app.main.MainUtil.hostOfUrl
import com.mycompany.app.main.MainUtil.appendAdsGoogleJs
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.data.DataUrl.Companion.instance
import com.mycompany.app.main.MainUtil.isEmgUrl
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.isHmgUrl
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.isLockEnabled
import com.mycompany.app.main.MainUtil.hostLabelUpper
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.isDarkMode
import com.mycompany.app.main.MainUtil.mediaNameFor
import com.mycompany.app.data.book.DataBookOver.Companion.instance
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.main.MainUtil.isLandscape
import com.mycompany.app.main.MainUtil.onPageRenderGone
import com.mycompany.app.main.MainUtil.isRtl
import com.mycompany.app.main.MainUtil.injectPhVideoJs
import com.mycompany.app.main.MainUtil.pxToDp
import com.mycompany.app.main.MainUtil.offlinePathFor
import com.mycompany.app.main.MainUtil.destroyWebView
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.main.MainUtil.setActivityOrientation
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.pref.PrefSet.putSpeedRate
import com.mycompany.app.db.book.DbBookTab
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.pref.PrefSet.putBoolNow
import com.mycompany.app.main.BitmapUtil.decodeResource
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.db.DbUtil.existsWhere
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.db.book.DbTabThumb.Companion.get
import com.mycompany.app.db.book.DbBookIcon.Companion.get
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.main.MainUtil.classSelectorJs
import com.mycompany.app.main.MainUtil.transLangFromCookie
import com.mycompany.app.main.MainUtil.langPrefix
import com.mycompany.app.data.DataTrans.Companion.instance
import com.mycompany.app.main.MainUtil.areaScriptJs
import com.mycompany.app.data.DataNews.Companion.instance
import com.mycompany.app.data.book.DataBookRecent.Companion.instance
import com.mycompany.app.db.book.DbBookPms.Companion.get
import com.mycompany.app.db.book.DbBookPass.Companion.get
import com.mycompany.app.pref.PrefSync.Companion.get
import com.mycompany.app.pref.PrefWeb.Companion.get
import com.mycompany.app.main.MainUtil.stripWww
import com.mycompany.app.db.book.DbBookDown.Companion.cached; import com.mycompany.app.db.book.DbBookDown.Companion.clearDone
import com.mycompany.app.db.book.DbBookRecent.Companion.cached; import com.mycompany.app.db.book.DbBookRecent.Companion.trimOld
import com.mycompany.app.main.MainUtil.openWithApp
import com.mycompany.app.db.book.DbBookAds.Companion.cached; import com.mycompany.app.db.book.DbBookAds.Companion.get
import com.mycompany.app.db.book.DbBookOver.Companion.cached; import com.mycompany.app.db.book.DbBookOver.Companion.get
import com.mycompany.app.db.book.DbBookPop.Companion.cached; import com.mycompany.app.db.book.DbBookPop.Companion.get
import com.mycompany.app.db.book.DbBookLink.Companion.cached; import com.mycompany.app.db.book.DbBookLink.Companion.get
import com.mycompany.app.db.book.DbBookJava.Companion.cached; import com.mycompany.app.db.book.DbBookJava.Companion.get
import com.mycompany.app.db.book.DbBookTmem.Companion.get
import com.mycompany.app.db.book.DbBookTrans.Companion.get
import com.mycompany.app.main.MainUtil.cancelToast
import com.mycompany.app.main.MainUtil.clearWebData
import com.mycompany.app.main.MainUtil.clearBrowsingData
import com.mycompany.app.main.MainUri.copyUri
import com.mycompany.app.main.MainUtil.copyText
import com.mycompany.app.main.MainUtil.isDcinsideImage
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.main.MainUtil.pathOfUrl
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.parseColorText
import com.mycompany.app.main.MainUtil.closeKeyboard
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.barTint
import com.mycompany.app.main.MainUtil.themeIconColor
import com.mycompany.app.main.MainUtil.parseIntList
import com.mycompany.app.main.MainUtil.bottomBarHeightPref
import com.mycompany.app.main.MainUtil.hasTopTabBar
import com.mycompany.app.main.MainUtil.shouldOpenInNewTab
import com.mycompany.app.main.MainUtil.googleRedirectTarget
import com.mycompany.app.main.MainUtil.editorTextColor
import com.mycompany.app.main.MainUtil.isKoreanLocale
import com.mycompany.app.main.MainUtil.navBarColor
import com.mycompany.app.main.MainUtil.setWindowStatusColor
import com.mycompany.app.main.MainUtil.isRootPath
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.main.MainUtil.urlFromText
import com.mycompany.app.data.DataAlbum.Companion.instance
import com.mycompany.app.data.DataPdf.Companion.instance
import com.mycompany.app.data.DataCmp.Companion.instance
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUtil.clearHistoryData
import com.mycompany.app.main.MainUtil.keepUriPermission
import com.mycompany.app.main.MainUtil.newCacheFilePath
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUri.isContentUri
import com.mycompany.app.main.MainUtil.blendColors
import com.mycompany.app.main.MainUtil.isDarkBarIcons
import com.mycompany.app.main.MainUtil.ensureWebViewDir
import com.mycompany.app.main.MainUtil.clearCacheDirs
import com.mycompany.app.main.MainUtil.deleteRecursive
import com.mycompany.app.main.MainUrl.isMediaUrl
import com.mycompany.app.main.MainUrl.isSubtitleUrl
import com.mycompany.app.main.MainUrl.streamKeyIndex
import com.mycompany.app.main.MainUrl.mediaExts; import com.mycompany.app.main.MainUrl.innerHttpUrl
import com.mycompany.app.main.MainUrl.imageExts; import com.mycompany.app.main.MainUrl.findHttpUrl
import com.mycompany.app.main.InitialSearch.initials; import com.mycompany.app.main.InitialSearch.initialMatch
import android.text.TextUtils
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainApp
import android.webkit.WebResourceRequest
import com.mycompany.app.web.WebNestView
import com.mycompany.app.data.DataUrl
import android.webkit.ValueCallback
import android.widget.RelativeLayout
import com.mycompany.app.ui.browser.IconButton
import com.mycompany.app.pref.PrefWeb
import android.widget.FrameLayout
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.db.DbUtil
import com.mycompany.app.view.MyWebBody
import android.view.MotionEvent
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefRead
import android.widget.ImageView.ScaleType
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.web.WebViewActivity.TabAddListener
import com.mycompany.app.script.ScriptCache
import com.mycompany.app.ui.browser.BarFace
import com.mycompany.app.web.WebViewActivity.PrevBackListener
import com.mycompany.app.view.MyWebCoord
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.script.ScriptCriteria
import com.mycompany.app.db.book.DbBookScript
import com.mycompany.app.data.DataAlbum
import com.mycompany.app.data.DataPdf
import com.mycompany.app.data.DataCmp
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
import androidx.core.app.ActivityCompat
import android.view.View
import java.lang.Exception
import java.util.ArrayList
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

/*
 * The browser activity's misc functions, moved out of `WebViewActivity` as
 * extension functions with unchanged bodies (grouped by the feature fields they use). The state still lives in
 * the activity; later sweeps move it into holders and give these functions real names.
 */

fun WebViewActivity.openInNewTab(url: String?, tabAddListener: TabAddListener?) {
    addTabImpl(null, url, true, true, true, tabAddListener)
}

fun WebViewActivity.hideKeyboard() {
    val myWebCoord: MyWebCoord? = if (!this.keyboardOpen) null else this.webCoord
    if (!this.keyboardOpen || myWebCoord == null || this.keyboardHiding) {
        return
    }
    this.keyboardHiding = true
    closeKeyboard(this.dialogsState.appContext, myWebCoord)
    this.webCoord!!.postDelayed(WvaHideKeyboardTask(this), 100L)
}

fun WebViewActivity.siteFixModeFor(pageHost: String?): Int {
    if (this.barsState.siteFixMode.toInt() == 0 || this.onStartPage || TextUtils.isEmpty(pageHost)) {
        return 0
    }
    if (this.barsState.siteFixMode.toInt() == 1) {
        if (pageHost!!.endsWith("reddit.com")) {
            return this.barsState.siteFixMode.toInt()
        }
        return 0
    }
    if (pageHost!!.endsWith("github.com")) {
        return this.barsState.siteFixMode.toInt()
    }
    return 0
}

fun WebViewActivity.isTranslateActive(): kotlin.Boolean {
    if (!AlbumSettings.transIcon.value && !this.transActive && !this.autoTransPending) {
        return false
    }
    val onStartPage = this.onStartPage
    return !onStartPage
}

fun WebViewActivity.openLoadDialogFor(url: String?, forceLoad: kotlin.Boolean) {
    if (this.isClosing) {
        return
    }
    if (isEmgUrl(url)) {
        showLoadEmgDialog(url, forceLoad)
    } else if (isHmgUrl(url)) {
        showLoadHmgDialog(url, forceLoad)
    } else {
        showLoadImgDialog(url, forceLoad)
    }
}

fun WebViewActivity.useDarkWindow(): kotlin.Boolean {
    if (!isDarkMode()) {
        return false
    }
    if (this.onStartPage) {
        return true
    }
    return if (TtsSettings.webLand.value != 0 || TtsSettings.webLand2.value != 0) isLandscapeScreen() else false
}

fun WebViewActivity.deliverFileChooserResult(uriArr: Array<Uri?>?) {
    val valueCallback = this.fileChooserCallback
    if (valueCallback != null) {
        (valueCallback as ValueCallback<Array<Uri?>?>).onReceiveValue(uriArr)
    } else {
        val imageCallback = this.fileChooserCallback2
        if (imageCallback != null) {
            (imageCallback as ValueCallback<Array<Uri?>?>).onReceiveValue(uriArr)
        } else {
            val fileCallback = this.fileChooserCallback3
            if (fileCallback != null) {
                (fileCallback as ValueCallback<Array<Uri?>?>).onReceiveValue(uriArr)
            }
        }
    }
    this.fileChooserCallback = null
    this.fileChooserCallback2 = null
    this.fileChooserCallback3 = null
    this.cameraUri = null
}

fun WebViewActivity.applySideMargins(landscape: kotlin.Boolean) {
    val myWebCoord = this.webCoord
    val layoutParams: RelativeLayout.LayoutParams? = if (myWebCoord == null) null else (myWebCoord.getLayoutParams() as RelativeLayout.LayoutParams?)
    if (myWebCoord == null || layoutParams == null) {
        return
    }
    if ((landscape == true)) {
        layoutParams.leftMargin = TtsSettings.webLand.value
        layoutParams.rightMargin = TtsSettings.webLand2.value
    } else {
        layoutParams.leftMargin = 0
        layoutParams.rightMargin = 0
    }
}

fun WebViewActivity.addSideButton(themeColor: Int, themeLight: Int) {
    if (this.sideButton != null) {
        tintSideButton(themeColor, themeLight)
        val layoutParams = this.sideButton!!.getLayoutParams() as FrameLayout.LayoutParams?
        if (layoutParams != null) {
            if (PdfSettings.tabAdd.value == 3) {
                layoutParams.gravity = 5
            } else {
                layoutParams.gravity = 3
            }
        }
        this.sideButton!!.requestLayout()
        return
    }
    val myButtonImage = IconButton(this)
    this.sideButton = myButtonImage
    myButtonImage.setScaleType(ScaleType.CENTER_INSIDE)
    tintSideButton(themeColor, themeLight)
    val layoutParams = FrameLayout.LayoutParams(MainApp.dp48, -1)
    if (PdfSettings.tabAdd.value == 3) {
        layoutParams.gravity = 5
    } else {
        layoutParams.gravity = 3
    }
    this.barsState.touchFrame!!.addView(this.sideButton, layoutParams)
    this.sideButton!!.setNoAnim(MainUtil.hasTopTabBar())
    this.sideButton!!.setOnClickListener(WvaTabAddClick(this))
}

fun WebViewActivity.runMenuItem(view: View?, itemId: Int) {
    // a family opens its members (MenuCatalog.families), wherever it was placed
    com.mycompany.app.ui.browser.MenuCatalog.family(itemId)?.let { family ->
        closeMenuDialogs()
        com.mycompany.app.ui.browser.ActionFamilySheet(this, family, this.onStartPage) { member -> runMenuItem(view, member) }
        return
    }
    this.dialogsState.menuItemCode = itemId
    this.dialogsState.menuItemView = view
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(MainMenuItemTask(this))
}

fun WebViewActivity.onDownIconClick(view: View?) {
    // the same button on a YouTube page: the YouTube actions
    if (this.dialogsState.onYouTube) {
        onVideoButtonClick(view)
        return
    }
    val showPip = ZoneSettings.showPip.value
    val showFull = ZoneSettings.showFull.value
    var index = if (showFull) (if (showPip) 1 else 0) + 1 else if (showPip) 1 else 0
    if (ZoneSettings.showDown.value) {
        index++
    }
    if (index > 1) {
        showVideoMenu(view, false)
        return
    }
    if (showPip) {
        runDownAction(2)
        return
    }
    if (showFull) {
        runDownAction(1)
    } else if (ReadSettings.guideDown.value) {
        showDownGuideDialog()
    } else {
        runDownAction(0)
    }
}

fun WebViewActivity.pageScheme(url: String?): Int {
    if (!TextUtils.isEmpty(url) && !TextUtils.isEmpty(this.dialogsState.pageUrl)) {
        if (this.dialogsState.pageUrl!!.startsWith("https://")) {
            // a certificate the phone does not trust: a warning instead of the lock (2026-11-28)
            return if (this.currentPage?.certProblem() == true) 3 else 1
        }
        if (this.dialogsState.pageUrl!!.startsWith("file://")) {
            return 2
        }
    }
    return 0
}

/**
 * The block page's "Go back": the page before in this tab (each page of a tab is its own WebView, so the browser's own
 * back, not WebView history), or the start page when there is none (a new tab).
 */
fun WebViewActivity.blockPageBack() {
    val page = this.currentPage ?: return
    val frame = this.tabsState.currentFrame
    when {
        frame != null && frame.pageIndex > 0 -> goBackPage(null)
        page.canGoBack() -> page.goBack()
        else -> page.loadUrl(com.mycompany.app.pref.store.BrowserSettings.homePage.value ?: "file:///android_asset/shortcut.html")
    }
}

/**
 * The block page's "Always allow this site": the site goes on the Web Filter's allowed sites (the same list as
 * Settings > Web Filter > Ads > allowed sites), then the page loads.
 */
fun WebViewActivity.allowBlockedSite() {
    val url = this.dialogsState.pageUrl ?: return
    val host = MainUtil.hostOfUrl(url, false)
    if (TextUtils.isEmpty(host)) return
    val context = this.dialogsState.appContext ?: return
    runInBackground(Runnable {
        try {
            val list = com.mycompany.app.data.book.DataBookAds.instance(context)
            if (!list.hasSiteOf(url, host)) {
                list.addSite(host)
                list.putItem(com.mycompany.app.db.book.DbBookAds.addPath(context, host))
            }
        } catch (_: Exception) {}
        this.webCoord?.post { reloadPage() }
    })
}

/** Opens the QR scanner (the address bar's QR button), asking for the camera first when needed. */
fun WebViewActivity.startQrScan() {
    if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
        launchQrScanner()
    } else {
        try { this.qrCamera.launch(android.Manifest.permission.CAMERA) } catch (_: Exception) {}
    }
}

/** The scanner itself, once the camera may be used. */
fun WebViewActivity.launchQrScanner() {
    val options = com.journeyapps.barcodescanner.ScanOptions()
        .setDesiredBarcodeFormats(com.journeyapps.barcodescanner.ScanOptions.QR_CODE)
        .setPrompt(getString(AppR.string.qr_scan_hint))
        .setBeepEnabled(false)
        .setOrientationLocked(false)
    try { this.qrScanner.launch(options) } catch (_: Exception) {}
}

/**
 * A scanned QR code: a web link opens in the current tab; anything else (a word, a search, a phone number) goes into
 * the address bar to check before it is sent. Nothing when the scan was cancelled.
 */
fun WebViewActivity.onQrScanned(contents: String?) {
    val text = contents?.trim().orEmpty()
    if (text.isEmpty() || this.isClosing) return
    val link = text.startsWith("http://", true) || text.startsWith("https://", true) ||
        (!text.contains(' ') && android.util.Patterns.WEB_URL.matcher(text).matches())
    if (link) {
        finishAddressEdit(text)
        return
    }
    setAddressEditing(true)
    val edit = this.editAuto ?: return
    edit.post {
        edit.setText(text)
        edit.setSelection(edit.length())
    }
}

fun WebViewActivity.finishAddressEdit(input: String?) {
    this.pageState.addressEditBusy = false
    setAddressEditing(false)
    if (TextUtils.isEmpty(input)) {
        return
    }
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaAddressSubmit(this, input = input))
}

fun WebViewActivity.startGoForward() {
    if (this.tabsState.startGoForwardBusy) {
        return
    }
    this.tabsState.startGoForwardBusy = true
    resetEdgeTouch()
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaGoForwardStart(this))
}

fun WebViewActivity.goToNextTab() {
    val arrayList: ArrayList<WebTabItem?>? = if (this.tabsState.tabSwipeState.toInt() != 0) null else this.tabsState.tabItems
    if (this.tabsState.tabSwipeState.toInt() != 0 || arrayList == null || arrayList.size < 2) {
        return
    }
    val myWebCoord: MyWebCoord? = if ((ZtwoSettings.tabLoop.value || this.currentTabIndex < this.tabsState.tabItems!!.size - 1)) this.webCoord else null
    if ((ZtwoSettings.tabLoop.value || this.currentTabIndex < this.tabsState.tabItems!!.size - 1) && myWebCoord != null) {
        myWebCoord.post(WvaSwipeToNextTab(this))
    }
}

fun WebViewActivity.applyPageBarColor(themeColor: Int, themeLight: Int) {
    if (this.quickState.editingAddress || !barColorsChanged(themeColor)) {
        return
    }
    applyThemeColors(themeColor, themeLight, navBarColorNow())
}

fun WebViewActivity.finishTabAdd() {
    val isTop = this.tabsState.isTop
    val animateFrame = this.tabsState.animateFrame
    val tabAddListener = this.miscState.tabAddListener
    val reusedPending = this.tabsState.reusedPending
    this.miscState.tabAddListener = null
    if (reusedPending) {
        applyPageScripts(true)
    }
    this.pageState.popupPending = false
    if (tabAddListener != null) {
        tabAddListener.onTabAdded(isTop && !animateFrame)
    }
    this.videoState.addTabImplBusy = false
}

fun WebViewActivity.resetEdgeTouch() {
    this.miscState.touchDownEdge = 0
    this.miscState.edgeTouchSide = 0
    this.floatingState.edgeTouchActive = false
}

fun WebViewActivity.pauseWebDestroy() {
    val mainWebDestroy = this.webDestroy
    if (mainWebDestroy != null) {
        mainWebDestroy.paused = true
    }
}

fun WebViewActivity.loadScriptCache() {
    var scriptCacheTime: Long = 0
    var cursorG: Cursor? = null
    val context = this.dialogsState.appContext
    val dbBookScript = DbBookScript.cached
    if (!PdfSettings.userScript.value || context == null) {
        scriptCacheTime = 0
    } else {
        try {
            cursorG = DbUtil.query(
                DbBookScript.get(context).getWritableDatabase(),
                "DbBookScript_table",
                arrayOf<String?>("_time"),
                "_enabled=?",
                arrayOf<String?>("1"),
                null
            )
            if (cursorG != null) {
                try {
                    if (cursorG.moveToFirst()) {
                        val columnIndex = cursorG.getColumnIndex("_time")
                        scriptCacheTime = 0
                        do {
                            try {
                                scriptCacheTime += cursorG.getLong(columnIndex)
                            } catch (unused: Exception) {
                            }
                        } while (cursorG.moveToNext())
                    } else {
                        scriptCacheTime = 0
                    }
                } catch (unused2: Exception) {
                }
            } else {
                scriptCacheTime = 0
            }
        } catch (unused3: Exception) {
            cursorG = null
        }
        if (cursorG != null) {
            cursorG.close()
        }
        if (scriptCacheTime < 0) {
            scriptCacheTime = 0
        }
    }
    if (this.miscState.scriptCacheTime != scriptCacheTime) {
        this.miscState.scriptCacheTime = scriptCacheTime
        val scriptCache = this.miscState.scriptCache
        if (scriptCache != null) {
            val scriptCriteriaArr: Array<ScriptCriteria>? = scriptCache.criteriaList as Array<ScriptCriteria>?
            scriptCache.criteriaList = null
            if (scriptCriteriaArr != null) {
                try {
                    scriptCache.scriptsByUrl.clear()
                } catch (unused4: Exception) {
                }
            }
            this.miscState.scriptCache = null
        }
        this.miscState.scriptCacheUrl = null
        if (this.miscState.scriptCacheTime != 0L) {
            try {
                this.miscState.scriptCacheUrl = UUID.randomUUID().toString()
                val newScriptCache = ScriptCache()
                this.miscState.scriptCache = newScriptCache
                newScriptCache.loadCriteria(this.dialogsState.appContext)
                injectUserScripts(true)
            } catch (unused5: Exception) {
            }
        }
    }
}

fun WebViewActivity.holdMenuReopen() {
    val handler = this.mainHandler
    if (handler == null) {
        return
    }
    this.menuJustClosed = true
    handler.postDelayed(WvaMenuReopenRelease(this), 800L)
}

fun WebViewActivity.startWebClean() {
    var webCleanU = this.miscState.webClean
    if (webCleanU == null) {
        webCleanU = MainApp.webClean(this.dialogsState.appContext, true)
        this.miscState.webClean = webCleanU
        if (webCleanU == null) {
            return
        }
    }
    val context = this.dialogsState.appContext
    webCleanU.listener = this.webCleanListener
    webCleanU.fullLists = isKoreanLocale()
    if (webCleanU.listener == null || webCleanU.started) {
        return
    }
    webCleanU.started = true
    com.mycompany.app.web.filter.UboFilter.installServiceWorkerClient(context)
    MainApp.runInBackground(context, webCleanU.UserBlockCopyTask(webCleanU, context))
}

fun WebViewActivity.addJsBridge(webNestView: WebNestView?) {
    if (webNestView == null || webNestView.jsAdded) {
        return
    }
    webNestView.setJsAdded(true)
    webNestView.addJavascriptInterface(WvaWebAppInterface(this), "android")
    webNestView.uboCosmetic = com.mycompany.app.web.filter.UboCosmetic.attach(webNestView)
    // pages that claim a dark theme but stay light get the normal web dark mode (WebDarkFix)
    WebDarkFix.attach(webNestView)
    this.dialogsState.langCheckBusy = false
}

fun WebViewActivity.goBackFromBackKey(prevBackListener: PrevBackListener?) {
    val edgeGestureStarted = this.miscState.edgeGestureStarted
    this.miscState.edgeGestureStarted = false
    if (this.tabsState.goBackPageBusy) {
        if (prevBackListener != null) {
            prevBackListener.onBack(true)
            return
        }
        return
    }
    this.tabsState.goBackPageBusy = true
    resetEdgeTouch()
    this.miscState.fromEdgeGesture = edgeGestureStarted
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaTranslateRestoreBack(this, transBackListener = prevBackListener))
}

fun WebViewActivity.hasPrevTab(): kotlin.Boolean {
    var arrayList: ArrayList<WebTabItem?>? = null
    return ZtriSettings.lastSwipe.value && this.currentTabIndex > 0 && (this.tabsState.tabItems.also {
        arrayList = it
    }) != null && arrayList!!.size > 1
}

fun WebViewActivity.twoFingerDx(motionEvent: MotionEvent?): kotlin.Float {
    if (this.miscState.pinchStartX != null) {
        try {
            if (motionEvent!!.getPointerCount() >= 2) {
                val dx = motionEvent.getX(0) - this.miscState.pinchStartX!![0]
                if ((motionEvent.getX(1) - this.miscState.pinchStartX!![1]) * dx >= 0.0f) {
                    return dx
                }
            }
        } catch (unused: Exception) {
        }
    }
    return 0.0f
}

fun WebViewActivity.twoFingerDy(motionEvent: MotionEvent?): kotlin.Float {
    if (this.miscState.pinchStartY != null) {
        try {
            if (motionEvent!!.getPointerCount() >= 2) {
                val dy = motionEvent.getY(0) - this.miscState.pinchStartY!![0]
                if ((motionEvent.getY(1) - this.miscState.pinchStartY!![1]) * dy >= 0.0f) {
                    return dy
                }
            }
        } catch (unused: Exception) {
        }
    }
    return 0.0f
}

fun WebViewActivity.startPageSetup() {
    val myWebCoord = this.webCoord
    if (myWebCoord == null || this.miscState.startPageSetupDone) {
        return
    }
    this.miscState.startPageSetupDone = true
    this.setupLock = null
    if (!SyncSettings.splash.value) {
        if (this.miscState.startPageSetupBusy) {
            return
        } else {
            this.miscState.startPageSetupBusy = true
        }
    }
    myWebCoord.post(WvaStartPageSetupTask(this))
}

fun WebViewActivity.otherTabItemAt(currentTabIndex: Int): WebTabItem? {
    try {
        val arrayList = this.tabsState.tabItems
        if (arrayList == null || arrayList.size < 2 || currentTabIndex <= 0 || currentTabIndex >= this.tabsState.tabItems!!.size) {
            return null
        }
        return this.tabsState.tabItems!!.get(currentTabIndex)
    } catch (unused: Exception) {
        return null
    }
}

fun WebViewActivity.attachAnimListener(webNestView: WebNestView?) {
    if (webNestView != null) {
        webNestView.ensureWebViewClient()
        if (webNestView.animListener != null) {
            return
        }
        webNestView.setAnimListener(WvaWebAnimListener(this))
    }
}

fun WebViewActivity.urlSplitIndex(pageHost: String?, url: String?): Int {
    val length = if (this.quickState.fakeTsMisses > 10 || pageHost == null || pageHost.startsWith("manatoki") || pageHost.startsWith("newtoki")) 0 else url!!.length
    val splitIndex = if (this.quickState.fakeTsMisses > 10 || pageHost == null || pageHost.startsWith("manatoki") || pageHost.startsWith("newtoki") || length < 10) 0 else pathEnd(url)
    if (this.quickState.fakeTsMisses > 10 || pageHost == null || pageHost.startsWith("manatoki") || pageHost.startsWith("newtoki") || length < 10 || splitIndex < 10 || splitIndex >= length) {
        return 0
    }
    return splitIndex
}

fun WebViewActivity.trimOldTabs() {
    var oldestTab: Long = 0
    var myWebCoord: MyWebCoord? = null
    var arrayList: ArrayList<WebTabItem?>? = null
    arrayList = if (ZtwoSettings.tabMemory.value < 3) null else this.tabsState.tabItems
    if (ZtwoSettings.tabMemory.value < 3 || arrayList == null || arrayList.size <= ZtwoSettings.tabMemory.value) {
        oldestTab = 0
    } else {
        try {
            val list = this.tabUsageOrder
            if (list == null || list.isEmpty()) {
                oldestTab = 0
            } else {
                oldestTab = (this.tabUsageOrder!!.get(0) as Long)
            }
        } catch (unused: Exception) {
        }
    }
    myWebCoord = if (oldestTab == 0L) null else this.webCoord
    if (oldestTab == 0L || myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaTrimOldTabs(this, oldestTab))
}

fun WebViewActivity.refererForRequest(url: String?, webResourceRequest: WebResourceRequest?): String? {
    var referer: String? = null
    try {
        val requestHeaders = webResourceRequest!!.getRequestHeaders()
        if (requestHeaders == null) {
            return null
        }
        val refererText = requestHeaders.get("Referer")
        try {
            val requestUrl = urlFromText(url)
            if (!TextUtils.isEmpty(requestUrl)) {
                val cached = requestHeaders.get("Origin")
                val mainAppO: MainApp? = if (TextUtils.isEmpty(cached)) null else MainApp.from(this.dialogsState.appContext)
                if (TextUtils.isEmpty(cached) || mainAppO == null) {
                    return refererText
                }
                try {
                    var lruCache = mainAppO.hostCache
                    if (lruCache == null) {
                        lruCache = LruCache<String, String>(40)
                        mainAppO.hostCache = lruCache
                    }
                    if (requestUrl != null && cached != null) {
                        lruCache.put(requestUrl, cached)
                        MainUtil.hostOfUrl(requestUrl, false)?.let { lruCache.put(it, cached) }
                    }
                    return refererText
                } catch (unused: Exception) {
                    return refererText
                }
            }
            return refererText
        } catch (unused2: Exception) {
            referer = refererText
        }
    } catch (unused3: Exception) {
    }
    return referer
}

fun WebViewActivity.removeCoverView() {
    val coverScrollYNow = this.coverScrollY
    this.coverScrollY = 0
    val myCoverView = this.coverView
    if (myCoverView != null) {
        myCoverView.setBackground(null)
        this.coverView!!.release()
        val myWebBody = this.dialogsState.webBody
        if (myWebBody != null) {
            this.floatingState.overlays!!.removeView(this.coverView)
        }
        this.coverView = null
    }
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.startCapture(null, 0L, null)
    this.webCoord!!.postDelayed(WvaRestoreScroll(this, coverScrollY = coverScrollYNow), 300L)
}

fun WebViewActivity.showMoveHint(view: View?) {
    if (view == null || this.isClosing || this.videoState.moveHintBusy) {
        return
    }
    this.videoState.moveHintBusy = true
    this.miscState.moveHintX = view.getX().toInt()
    this.miscState.moveHintY = view.getY().toInt()
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaAddMoveHint(this))
}

/** Formerly cancelled the old engine's page scan; nothing to stop any more (kept for its callers). */
fun WebViewActivity.stopWebClean() {
}

fun WebViewActivity.touchTabUsage(currentTabIndex: Int) {
    val webTabItemD = tabItemAt(currentTabIndex)
    if (webTabItemD == null) {
        this.tabUsageOrder = null
        return
    }
    try {
        if (ZtwoSettings.tabMemory.value < 3) {
            webTabItemD.lastUsed = System.currentTimeMillis()
            DbBookTab.setLastUsed(this.dialogsState.appContext, webTabItemD.uid, webTabItemD.lastUsed)
            this.tabUsageOrder = null
            return
        }
        val list = this.tabUsageOrder
        if (list == null) {
            this.tabUsageOrder = ArrayList<Any?>()
        } else {
            val lastUsed = webTabItemD.lastUsed
            if (lastUsed != 0L) {
                list.remove(lastUsed)
            }
        }
        val now = System.currentTimeMillis()
        webTabItemD.lastUsed = now
        DbBookTab.setLastUsed(this.dialogsState.appContext, webTabItemD.uid, now)
        this.tabUsageOrder!!.add(now)
        val keepCount = min(ZtwoSettings.tabMemory.value, this.tabsState.tabItems!!.size)
        val size = this.tabUsageOrder!!.size
        if (size > keepCount) {
            this.tabUsageOrder = this.tabUsageOrder!!.subList(size - keepCount, size)
        }
    } catch (unused: Exception) {
    }
}

fun WebViewActivity.watchWindowHeight() {
    if (MainConst.isApi30) {
        return
    }
    if (!this.miscState.windowHeightWatched) {
        this.miscState.windowHeightWatched = true
        this.heightListener = WvaWindowHeightListener(this)
    }
    if (this.miscState.barsSettingApplyBusy) {
        this.miscState.barsSettingApplyBusy = false
        val handler = this.mainHandler
        if (handler == null) {
            return
        }
        handler.postDelayed(WvaWindowHeightCheck(this), 800L)
    }
}

fun WebViewActivity.clearDataCaches() {
    if (this.dialogsState.loadDialogKind.toInt() != 0 || this.miscState.dataCachesDirty) {
        this.dialogsState.loadDialogKind = 0.toByte()
        this.miscState.dataCachesDirty = false
        DataAlbum.instance(this.dialogsState.appContext).clear()
        DataPdf.instance(this.dialogsState.appContext).clear()
        DataCmp.instance(this.dialogsState.appContext).clear()
        val dataUrlB = DataUrl.instance(this.dialogsState.appContext)
        dataUrlB.urls = null
        dataUrlB.thumbUrls = null
    }
}

fun WebViewActivity.tabItemAt(currentTabIndex: Int): WebTabItem? {
    try {
        val arrayList = this.tabsState.tabItems
        if (arrayList == null || currentTabIndex < 0 || currentTabIndex >= arrayList.size) {
            return null
        }
        return this.tabsState.tabItems!!.get(currentTabIndex)
    } catch (unused: Exception) {
        return null
    }
}

fun WebViewActivity.restartApp() {
    if (this.miscState.restartAppBusy) {
        return
    }
    this.miscState.restartAppBusy = true
    Thread(RestartAppTask(this)).start()
}

/**
 * The first edge swipe: the gesture still runs, and the edge-swipe tip says that it can be changed (2026-11-20; it
 * used to block the swipe and show a green card).
 */
fun WebViewActivity.showGestureGuide() {
    if (!ReadSettings.guideGesture.value) return
    ReadSettings.edit(this.dialogsState.appContext) { set(ReadSettings.guideGesture, false) }
    val tip = com.mycompany.app.ui.tips.TipManager.forEvent(com.mycompany.app.ui.tips.Tip.EDGE_SWIPE) ?: return
    // it answers what the user just did: it takes the place of a tip that is still open
    // (a moment later: the swipe may close a tab, and its "Undelete" message comes first; showTip waits for it)
    this.miscState.tipView?.close()
    this.mainHandler?.postDelayed({ showTip(tip) }, 1500)
}

/** Posted once per app start at most, so the many bar updates do not queue it again. */
private var translateTipPosted = false

/**
 * The translate icon is in the address bar: the first time, the translate tip says what it does (2026-11-26; a red
 * dot sat on the icon before, until the translator was opened from it).
 */
fun WebViewActivity.showTranslateTip() {
    if (translateTipPosted) return
    val tip = com.mycompany.app.ui.tips.TipManager.forEvent(com.mycompany.app.ui.tips.Tip.TRANSLATE) ?: return
    translateTipPosted = true
    this.mainHandler?.postDelayed({ showTip(tip) }, 1500)
}

/**
 * Shows [tip] above the bottom bar (one at a time, not over dialogs or the setup wizard). While a message bar is at the
 * bottom (undo, pop-up blocked, …) it waits for it to go, up to 20 s, instead of stacking on it.
 */
fun WebViewActivity.showTip(tip: com.mycompany.app.ui.tips.Tip, attempt: Int = 0) {
    val overlays = this.floatingState.overlays ?: return
    val context = this.dialogsState.appContext ?: return
    if (this.dialogsState.webBody == null || this.isClosing) return
    if (this.miscState.tipView != null && tip.ambient) return
    // an event tip (it answers something the user did) waits for a message bar, an open tip or a dialog to go
    val busy = this.snackbar != null || this.dialogsState.tabUndoSnackbar != null ||
        this.dialogsState.popupBlockSnackbar != null || this.dialogsState.appBlockSnackbar != null ||
        this.miscState.tipView != null || isAnyDialogOpen()
    if (busy) {
        if (attempt < 20) this.mainHandler?.postDelayed({ showTip(tip, attempt + 1) }, 1000)
        return
    }
    if (!com.mycompany.app.ui.tips.TipManager.enabled()) return
    val view = com.mycompany.app.ui.browser.TipOverlay(
        this, tip,
        onAction = { openTipAction(tip) },
        onGone = { v ->
            overlays.removeView(v)
            if (this.miscState.tipView === v) this.miscState.tipView = null
        },
    )
    val params = com.mycompany.app.ui.browser.OverlayParams(-1, -2)
    params.anchor = com.mycompany.app.ui.browser.OverlayParams.BOTTOM_BAR
    params.anchorGravity = 48
    params.gravity = 48
    overlays.addCoordView(view, params)
    this.miscState.tipView = view
    com.mycompany.app.ui.tips.TipManager.shown(context, tip)
}

/** The tip's action button: the matching settings page. */
fun WebViewActivity.openTipAction(tip: com.mycompany.app.ui.tips.Tip) {
    val target = when (tip) {
        com.mycompany.app.ui.tips.Tip.EDGE_SWIPE -> android.content.Intent(this, com.mycompany.app.ui.settings.GestureSettingsActivity::class.java)
            .putExtra("EXTRA_NOTI", true).putExtra("EXTRA_INDEX", 1)
        com.mycompany.app.ui.tips.Tip.CUSTOMIZE -> android.content.Intent(this, com.mycompany.app.ui.settings.LayoutSettingsActivity::class.java)
        else -> return
    }
    try { startActivity(target) } catch (_: Exception) {}
}

/** A page finished loading: maybe an ambient tip (ui/tips), a moment later. */
fun WebViewActivity.maybeShowPageTip() {
    if (this.onStartPage) return
    val tip = com.mycompany.app.ui.tips.TipManager.onPageLoaded(this.dialogsState.pageUrl, this.tabsState.tabItems?.size ?: 0) ?: return
    this.mainHandler?.postDelayed({ showTip(tip) }, 1500)
}

fun WebViewActivity.removeIntro() {
    val intro = this.miscState.introView
    if (intro != null) {
        intro.animate().cancel()
        if (this.dialogsState.webBody != null) this.floatingState.overlays?.removeView(intro)
        this.miscState.introView = null
    }
    this.miscState.glide = null
}

fun WebViewActivity.clearBackPlay(enabled: kotlin.Boolean) {
    this.videoState.backPlayOn = false
    this.dialogsState.backPlayOn = false
    this.dialogsState.backPlaySrc = null
    if ((enabled == true)) {
        clearHistoryData(this.dialogsState.appContext)
    }
}

fun WebViewActivity.edgeAt(touchX: kotlin.Float): Int {
    if (this.dialogsState.webBody == null || !hasSideGestures()) {
        return 0
    }
    if (touchX < MainApp.dp24) {
        return 1
    }
    return if (touchX > ((this.dialogsState.webBody!!.getWidth() - MainApp.dp24).toFloat())) 2 else 0
}

fun WebViewActivity.isSameRatePage(): kotlin.Boolean {
    try {
        val rateScriptUrl = this.miscState.rateScriptUrl
        val length = if (rateScriptUrl == null) 0 else rateScriptUrl.length
        val rateUrl: String? = if (rateScriptUrl == null || length == 0) null else this.dialogsState.pageUrl
        val rateLength = if (rateScriptUrl == null || length == 0 || rateUrl == null) 0 else rateUrl.length
        if (rateScriptUrl == null || length == 0 || rateUrl == null || rateLength == 0) {
            return false
        }
        if (length > rateLength) {
            return this.miscState.rateScriptUrl!!.startsWith(this.dialogsState.pageUrl!!)
        }
        return if (length < rateLength) this.dialogsState.pageUrl!!.startsWith(this.miscState.rateScriptUrl!!) else (this.miscState.rateScriptUrl == this.dialogsState.pageUrl)
    } catch (unused: Exception) {
        return false
    }
}

fun WebViewActivity.switchSecretMode(secret: kotlin.Boolean) {
    if (this.dialogsState.switchSecretModeBusy) {
        return
    }
    this.dialogsState.switchSecretModeBusy = true
    this.pageState.restartBrowserBusy = false
    SyncSettings.editLocal { set(SyncSettings.secretMode, (secret) == true) }
    runInBackground(WvaSwitchSecretModeTask(this))
}

fun WebViewActivity.clearPermissionRequest() {
    this.permissionRequest2 = null
    this.geoOrigin = null
    this.geoCallback = null
    this.pageState.pendingPermissionAllow = 0
    this.permissionKind = 0
}

fun WebViewActivity.openIntentUrl(path: String?, name: String?) {
    if (this.miscState.openIntentUrlBusy) {
        return
    }
    this.miscState.openIntentUrlBusy = true
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaOpenIntentUrlTask(this, path = path, name = name))
}

fun WebViewActivity.requestSetDownDialog(url: String?, name: String?, dirName: String?) {
    if (this.miscState.requestSetDownDialogBusy) {
        return
    }
    this.miscState.requestSetDownDialogBusy = true
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaShowSetDownDialog(this, url = url, name = name, dirName = dirName))
}

fun WebViewActivity.isPortraitSingleWindow(): kotlin.Boolean {
    val myWebBody = this.dialogsState.webBody
    if (myWebBody == null || myWebBody.getWidth() > myWebBody.getHeight()) {
        return false
    }
    return !isInMultiWindowMode()
}

fun WebViewActivity.updateSecureFlag(secure: kotlin.Boolean) {
    var secure = secure
    val shotType = SecretSettings.shotType.value
    var secureOn = false
    if (shotType == 0 || (SecretSettings.shotSecret.value && !SyncSettings.secretMode.value)) {
        secure = secureOn
    } else {
        secureOn = true
        if (shotType == 1) {
            secure = secureOn
        }
    }
    if (this.secureFlagOn == secure) {
        return
    }
    this.secureFlagOn = (secure) == true
    getWindow().clearFlags(8192)
}

fun WebViewActivity.topBarModeHidden(): kotlin.Boolean {
    return !BrowserSettings.showNavi.value && ZoneSettings.gesLeft.value == 2 && hasSideGestures()
}

fun WebViewActivity.applyTransLang(): kotlin.Boolean {
    val lastTransLang = this.lastTransLang
    this.lastTransLang = null
    if (this.dialogsState.transState.toInt() != 3 || TextUtils.isEmpty(lastTransLang) || TextUtils.isEmpty(AlbumSettings.transPick.value) || lastTransLang == AlbumSettings.transPick.value) {
        return false
    }
    val transPick = AlbumSettings.transPick.value
    if (TextUtils.isEmpty(transPick)) {
        return true
    }
    runInBackground(WvaApplyTransLangTask(this, transPick))
    return true
}

fun WebViewActivity.bottomBarModeHidden(): kotlin.Boolean {
    return !BrowserSettings.showNavi.value && ZoneSettings.gesRight.value == 2 && hasSideGestures()
}

fun WebViewActivity.isSingleBarMode(): kotlin.Boolean {
    return this.barsState.barLayoutMode.toInt() == 1
}

fun WebViewActivity.removeHandModeBar() {
    val myWebBody = this.dialogsState.webBody
    if (myWebBody == null) {
        return
    }
    myWebBody.handIconRes = 0
    if (myWebBody.handNotiIcon != null) {
        myWebBody.handNotiIcon = null
        myWebBody.invalidate()
    }
    val relativeLayout = this.dialogsState.handModeBar
    if (relativeLayout != null) {
        this.dialogsState.webBody!!.removeView(relativeLayout)
        this.dialogsState.handModeBar = null
    }
}

fun WebViewActivity.requestSitePermission(permission: Int): kotlin.Boolean {
    if (permission == 1) {
        ActivityCompat.requestPermissions(
            this,
            arrayOf<String?>("android.permission.CAMERA", "android.permission.RECORD_AUDIO"),
            21
        )
        return true
    }
    if (permission == 2) {
        ActivityCompat.requestPermissions(this, arrayOf<String?>("android.permission.CAMERA"), 22)
        return true
    }
    if (permission == 3) {
        ActivityCompat.requestPermissions(this, arrayOf<String?>("android.permission.RECORD_AUDIO"), 23)
        return true
    }
    if (permission != 4) {
        return false
    }
    ActivityCompat.requestPermissions(this, arrayOf<String?>("android.permission.ACCESS_FINE_LOCATION"), 24)
    return true
}

fun WebViewActivity.isScriptHost(url: String?): kotlin.Boolean {
    if (TextUtils.isEmpty(url)) {
        return false
    }
    return url == this.miscState.scriptCacheUrl
}

fun WebViewActivity.runDownAction(action: Int) {
    if (this.miscState.runDownActionBusy) {
        return
    }
    this.miscState.runDownActionBusy = true
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaRunDownActionTask(this, action = action.toByte()))
}

fun WebViewActivity.tintSideButton(themeColor: Int, themeLight: Int) {
    var themeColor = themeColor
    var themeLight = themeLight
    if (this.sideButton == null) {
        return
    }
    if (isDarkBarIcons(themeColor, this.onStartPage)) {
        themeColor = 0
        themeLight = 0
    }
    val tint = MainUtil.barTint(themeColor, this.onStartPage)
    val myButtonImage = this.sideButton
    if (tint == 0) {
        myButtonImage!!.setImageResource(AppR.drawable.outline_add_black_24)
    } else {
        myButtonImage!!.setImageResource(AppR.drawable.outline_add_dark_24)
    }
    this.sideButton!!.setAlpha(BarFace.pressScale(tint))
    this.sideButton!!.setBgPreColor(themeIconColor(tint, themeLight))
}

fun WebViewActivity.requestImagePreview(imageUrl: String?, referer: String?, fromLink: kotlin.Boolean) {
    if (this.miscState.requestImagePreviewBusy) {
        return
    }
    this.miscState.requestImagePreviewBusy = true
    this.dialogsState.previewImageUrl = imageUrl
    this.dialogsState.previewReferer = referer
    this.dialogsState.previewFromLink = (fromLink) == true
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(OpenImagePreviewTask(this))
}

fun WebViewActivity.openTextViewer(url: String?, title: String?) {
    if (this.miscState.openTextViewerBusy) {
        return
    }
    this.miscState.openTextViewerBusy = true
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaOpenTextViewerTask(this, url = url, title = title))
}

fun WebViewActivity.isPortalHome(): kotlin.Boolean {
    val pageHost = this.dialogsState.pageHost
    if (TextUtils.isEmpty(pageHost)) {
        return false
    }
    if (pageHost!!.endsWith("instagram.com")) {
        val pageUrl = this.dialogsState.pageUrl
        return pageUrl != null && pageUrl.length < 27
    }
    if (pageHost.endsWith("naver.com")) {
        val pageUrl = this.dialogsState.pageUrl
        return pageUrl != null && pageUrl.length < 21
    }
    if (pageHost.endsWith("daum.net")) {
        return "https://m.daum.net/?nil_top=mobile" == this.dialogsState.pageUrl
    }
    return false
}

fun WebViewActivity.forgetTabItem(webTabItem: WebTabItem?) {
    if (ZtwoSettings.tabMemory.value < 3 || webTabItem == null || webTabItem.lastUsed == 0L) {
        return
    }
    try {
        val list = this.tabUsageOrder
        if (list == null || list.isEmpty()) {
            return
        }
        this.tabUsageOrder!!.remove(webTabItem.lastUsed)
    } catch (unused: Exception) {
    }
}

fun WebViewActivity.isZoneGestureOff(): kotlin.Boolean {
    val rightGesture = if (ZoneSettings.gesTop.value == 0 || ZoneSettings.gesBot.value == 0) 0 else ZoneSettings.gesLeft.value
    val leftGesture = if (ZoneSettings.gesTop.value == 0 || ZoneSettings.gesBot.value == 0 || rightGesture == 0) 0 else ZoneSettings.gesRight.value
    if (ZoneSettings.gesTop.value == 0 || ZoneSettings.gesBot.value == 0 || rightGesture == 0 || leftGesture == 0 || ZoneSettings.gesCenter.value == 0) {
        return true
    }
    if (BrowserSettings.showNavi.value) {
        return false
    }
    return (rightGesture == 2 || leftGesture == 2) && hasSideGestures()
}

fun WebViewActivity.finishActionMode() {
    val actionMode = this.dialogsState.actionMode
    if (actionMode != null) {
        actionMode.finish()
        this.dialogsState.actionMode = null
    }
}

fun WebViewActivity.swipeForwardOpensTab(): kotlin.Boolean {
    if (this.miscState.touchDownEdge != 0) {
        return false
    }
    val layoutRtl = this.tabsState.layoutRtl
    val addPage = ZtwoSettings.addPage.value
    if (layoutRtl) {
        return addPage && !pageCanGoBack()
    }
    return addPage && !pageCanGoForward()
}

fun WebViewActivity.resumeWebDestroy() {
    val mainWebDestroy = this.webDestroy
    if (mainWebDestroy != null) {
        mainWebDestroy.paused = false
        mainWebDestroy.start(false)
    }
}

fun WebViewActivity.saveBookIcon(url: String?, title: String?, bitmap: Bitmap?, isBook: kotlin.Boolean, update: kotlin.Boolean) {
    runInBackground(WvaSaveBookIconTask(this, url = url, title = title, bitmap = bitmap, isBook = (isBook) == true, update = (update) == true))
}

fun WebViewActivity.swipeBackOpensTab(): kotlin.Boolean {
    if (this.miscState.touchDownEdge != 0) {
        return false
    }
    val layoutRtl = this.tabsState.layoutRtl
    val addPage = ZtwoSettings.addPage.value
    if (layoutRtl) {
        return addPage && !pageCanGoForward()
    }
    return addPage && !pageCanGoBack()
}

fun WebViewActivity.showTransIcon(): kotlin.Boolean {
    if (AlbumSettings.transHide.value) return false
    if (isTranslateActive()) {
        return !AlbumSettings.transPage.value
    }
    return false
}
