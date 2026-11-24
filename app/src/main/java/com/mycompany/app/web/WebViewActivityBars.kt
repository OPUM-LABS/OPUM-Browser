@file:Suppress("unused", "UNUSED_PARAMETER", "UNUSED_VARIABLE", "NAME_SHADOWING", "DEPRECATION", "UNCHECKED_CAST", "KotlinConstantConditions", "RedundantSemicolon")

package com.mycompany.app.web

import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.MainSettings
import com.mycompany.app.db.book.DbBookFilter.Companion.addFilters; import com.mycompany.app.db.book.DbBookFilter.Companion.cached
import com.mycompany.app.pref.PrefPdf.Companion.get
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookFilter.Companion.get
import com.mycompany.app.main.MainUtil.filterFilePath
import com.mycompany.app.main.MainUtil.hasChar
import com.mycompany.app.main.MainUtil.openHeadConnection
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.main.MainUtil.trimTrailingSlash
import com.mycompany.app.db.book.DbBookUser.Companion.get
import com.mycompany.app.main.MainUtil.hostOfUrl
import com.mycompany.app.db.book.DbAdsCmd.Companion.cached; import com.mycompany.app.db.book.DbAdsCmd.Companion.getCmd
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainUtil.hostOf
import com.mycompany.app.main.MainUtil.openWithCookies
import com.mycompany.app.main.MainUtil.topBarHeight
import com.mycompany.app.main.MainUtil.bottomInsetTotal
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.requestHeadersFor2
import com.mycompany.app.main.MainUtil.urlInParens
import com.mycompany.app.main.MainUtil.siteNameOf
import com.mycompany.app.main.MainUtil.hostLabelUpper
import com.mycompany.app.main.MainUtil.hostNameOf
import com.mycompany.app.main.MainUtil.fileExtFromName
import com.mycompany.app.main.MainUtil.isYouTubeUrl
import com.mycompany.app.db.book.DbAdsCmd.Companion.get
import com.mycompany.app.main.MainUtil.onPageRenderGone
import com.mycompany.app.main.MainUtil.isDarkBars
import com.mycompany.app.main.MainUtil.offlineUrlFor
import com.mycompany.app.main.MainUtil.clearHandler
import com.mycompany.app.main.MainUtil.pxToDp
import com.mycompany.app.main.MainUtil.resetTextSizePref
import com.mycompany.app.main.MainUtil.backupBitmap
import com.mycompany.app.main.MainUtil.parentPathOf
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.destroyWebView
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.main.MainUtil.imageViewerIntent
import com.mycompany.app.main.MainUtil.imageCountChanged
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.main.MainUtil.applySystemUiFlags
import com.mycompany.app.main.MainUtil.applyScreenRotation
import com.mycompany.app.pref.PrefSet.putSpeedRate
import com.mycompany.app.db.book.DbBookTab.Companion.get; import com.mycompany.app.db.book.DbBookTab.Companion.isSaving
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.main.MainUtil.fillAllFlags
import com.mycompany.app.db.book.DbBookTab.Companion.updateParents
import com.mycompany.app.main.MainUtil.toggleSystemBars
import com.mycompany.app.main.MainUtil.applySystemBarsMode
import com.mycompany.app.main.MainUtil.updateCutoutInsets
import com.mycompany.app.main.MainUtil.loadDrawable
import com.mycompany.app.pref.PrefSet.putBoolNow
import com.mycompany.app.main.BitmapUtil.decodeResource
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookScript.Companion.get
import com.mycompany.app.main.MainUtil.scriptNameFor
import com.mycompany.app.db.DbUtil.existsWhere
import com.mycompany.app.main.MainUtil.trimMemoryCaches
import com.mycompany.app.main.MainUtil.copyFileOrUri
import com.mycompany.app.db.book.DbTabThumb.Companion.get
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.main.MainUtil.openHttpConnection
import com.mycompany.app.db.book.DbBookIcon.Companion.get
import com.mycompany.app.main.MainUtil.pageTitleForFile
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.main.MainUtil.applyRotationLock
import com.mycompany.app.main.MainUtil.reloadWithDesktopMode
import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUtil.mimeForExt
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.db.book.DbBookWeb.Companion.deleteById
import com.mycompany.app.main.MainUtil.languageTag
import com.mycompany.app.main.MainUtil.nextPageUrl
import com.mycompany.app.main.MainUtil.dispositionFileName
import com.mycompany.app.main.MainUtil.fileExtOf
import com.mycompany.app.main.MainUtil.saveBitmapPng
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.storageVolume
import com.mycompany.app.main.MainUri.createUri
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.main.MainUtil.transScriptJs
import com.mycompany.app.main.MainUtil.areaMultiJs
import com.mycompany.app.main.MainUtil.areaScriptJs
import com.mycompany.app.db.book.DbBookRecent.Companion.isEnabled
import com.mycompany.app.db.book.DbBookPms.Companion.get
import com.mycompany.app.db.book.DbBookPass.Companion.get
import com.mycompany.app.pref.PrefSync.Companion.get
import com.mycompany.app.pref.PrefWeb.Companion.get
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
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.db.book.DbBookWeb.Companion.get
import com.mycompany.app.main.MainUtil.pathOfUrl
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.removeFromParent
import com.mycompany.app.main.MainUtil.parseColorText
import com.mycompany.app.main.MainUtil.closeKeyboard
import com.mycompany.app.script.CriterionMatcher.matches
import com.mycompany.app.main.MainUtil.barTint
import com.mycompany.app.main.MainUtil.isFakeStream
import com.mycompany.app.main.MainUtil.firstUrlIn
import com.mycompany.app.main.MainUtil.themeIconColor
import com.mycompany.app.main.MainUtil.hasTopTitle
import com.mycompany.app.main.MainUtil.parseIntList
import com.mycompany.app.main.MainUtil.topBarSize
import com.mycompany.app.main.MainUtil.bottomBarHeightPref
import com.mycompany.app.main.MainUtil.searchImageUrl
import com.mycompany.app.main.MainUtil.statusBarInsetPx
import com.mycompany.app.main.MainUtil.iconResFor
import com.mycompany.app.main.MainUtil.navBarColor
import com.mycompany.app.main.MainNative.ampParts; import com.mycompany.app.main.MainNative.clearCache
import com.mycompany.app.main.MainUrl.streamKeys; import com.mycompany.app.main.MainUrl.pathEnd
import com.mycompany.app.main.MainUtil.videoMenuCount
import com.mycompany.app.db.book.DbBookDown.Companion.get
import com.mycompany.app.main.MainUtil.newCacheFilePath
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUri.isContentUri
import com.mycompany.app.main.MainUtil.mimeForUrl
import com.mycompany.app.main.MainUtil.downMenuCount
import com.mycompany.app.main.MainUtil.isDarkBarIcons
import com.mycompany.app.main.MainUtil.ensureWebViewDir
import com.mycompany.app.main.MainUtil.clearCacheDirs
import com.mycompany.app.main.MainUtil.deleteRecursive
import com.mycompany.app.db.book.DbBookTab.Companion.deleteById
import com.mycompany.app.main.MainUrl.isMediaUrl
import com.mycompany.app.main.MainUrl.isSubtitleUrl
import com.mycompany.app.main.MainUtil.mimeForPath
import com.mycompany.app.main.MainUrl.streamKeyIndex
import com.mycompany.app.main.MainUrl.mediaExts; import com.mycompany.app.main.MainUrl.innerHttpUrl
import com.mycompany.app.main.MainUrl.imageExts; import com.mycompany.app.main.MainUrl.findHttpUrl
import com.mycompany.app.db.book.DbBookRecent.Companion.deleteById
import com.mycompany.app.main.InitialSearch.initials; import com.mycompany.app.main.InitialSearch.initialMatch
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.appLocaleTag
import com.mycompany.app.main.MainUtil.durationText
import android.text.TextUtils
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainApp
import android.webkit.WebView
import com.mycompany.app.web.WebNestView
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.web.WebNestFrame
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefMain
import android.os.Build
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.db.book.DbBookTab
import com.mycompany.app.ui.browser.ComposeTabBar
import android.view.ViewGroup.MarginLayoutParams
import com.mycompany.app.dialog.DialogMenuMain
import com.mycompany.app.dialog.DialogMenuList
import com.mycompany.app.ui.browser.BarFace
import com.mycompany.app.wview.WebDownView
import com.mycompany.app.view.MyTouchFrame
import com.mycompany.app.view.MyWebCoord
import com.mycompany.app.dialog.DialogWebBookEdit
import com.mycompany.app.view.MySnackbar
import com.mycompany.app.dialog.DialogEditShort
import com.mycompany.app.view.MyBarView
import com.mycompany.app.view.MyEditAuto
import com.mycompany.app.view.MyFindView
import com.mycompany.app.view.MySnackbar.SnackItem
import com.mycompany.app.db.book.DbBookRecent
import com.mycompany.app.view.MyBarView.BarItem
import android.os.Handler
import android.view.View
import android.webkit.URLUtil
import ch.opumlabs.opumbrowser.R
import java.lang.Exception
import java.util.ArrayList
import kotlin.math.abs
import kotlin.math.max
import com.mycompany.app.web.WebViewActivity.Companion.barSizeFor

/*
 * The browser activity's bars functions, moved out of `WebViewActivity` as
 * extension functions with unchanged bodies (grouped by the feature fields they use). The state still lives in
 * the activity; later sweeps move it into holders and give these functions real names.
 */

fun WebViewActivity.updateBarBehavior() {
    val webNestView = this.currentPage
    val locked = webNestView != null && (webNestView.sslShown || webNestView.crashShown)
    val myBehaviorTop = this.dialogsState.topBehavior
    if (myBehaviorTop != null) {
        myBehaviorTop.locked = locked
    }
    val myBehaviorBot = this.behaviorBot
    if (myBehaviorBot != null) {
        myBehaviorBot.locked = locked
    }
    this.barScroll?.publish(true)
}

fun WebViewActivity.playVideoUrl(source: String?, videoListListenerFlag: kotlin.Boolean, mode: Int) {
    val videoUrl: String?
    val fakeStream: kotlin.Boolean
    if (this.currentPage == null) {
        return
    }
    if (URLUtil.isNetworkUrl(source)) {
        videoUrl = source
        fakeStream = false
    } else {
        fakeStream = isFakeStream(source)
        videoUrl = firstUrlIn(source)
        if (!URLUtil.isNetworkUrl(videoUrl)) {
            showToast(this, AppR.string.not_support_video)
            return
        }
    }
    if (!fakeStream) {
        if ((videoListListenerFlag == true)) {
            openTextViewer(videoUrl, pageTitleOf(this.currentPage, videoUrl))
            return
        }
        val mime = mimeForExt(fileExtOf(videoUrl, false))
        if (!TextUtils.isEmpty(mime) && mime!!.startsWith("text")) {
            openTextViewer(videoUrl, pageTitleOf(this.currentPage, videoUrl))
            return
        }
    }
    if (fakeStream || !videoUrl!!.endsWith(".gif")) {
        playVideoInPlayer(videoUrl, source, mode == 2)
    } else {
        requestImagePreview(videoUrl, null, false)
    }
}

fun WebViewActivity.restorePageItem() {
    val pageItem = this.barsState.pageItem
    val animPageIndex = this.barsState.animPageIndex
    this.barsState.pageItem = null
    val webNestFrame: WebNestFrame? = if (pageItem == null) null else this.tabsState.currentFrame
    val webNestView: WebNestView? = if (pageItem == null || webNestFrame == null) null else this.currentPage
    if (pageItem == null || webNestFrame == null || webNestView == null) {
        return
    }
    val deskMode = webNestView.deskMode
    var webNestViewW = pageItem.webNestView
    if (webNestViewW == null && (webNestFrame.createPageAt(animPageIndex)
            .also { webNestViewW = it }) != null && !TextUtils.isEmpty(webNestViewW!!.blankPageUrl)
    ) {
        webNestViewW.setBarColors(pageThemeColor(), pageThemeLight())
    }
    if (webNestViewW != null) {
        this.currentPage = webNestViewW
        this.tabsState.currentFrame!!.pageIndex = animPageIndex
    }
    if (this.tabsState.layoutRtl) {
        this.currentPage!!.resetSwipeShade()
        this.tabsState.currentFrame!!.resetFakeX()
    }
    var url = this.currentPage!!.getUrl()
    if (TextUtils.isEmpty(url)) {
        url = this.currentPage!!.validPageUrl
    }
    val urlEmpty = TextUtils.isEmpty(url)
    val currentTabIndex = this.currentTabIndex
    if (urlEmpty) {
        val tab = tabItemAt(currentTabIndex)
        if (tab != null) {
            url = tab.tabUrl
        }
    } else {
        val tab = tabItemAt(currentTabIndex)
        if (tab != null) {
            tab.tabUrl = url
            tab.tabTitle = pageTitleOf(this.currentPage, url)
            tab.deskMode = deskMode
            loadTabItemAsync(tab)
        }
    }
    this.barsState.restoreDeskMode = deskMode
    this.dialogsState.restoredPageUrl = url
}

fun WebViewActivity.resetBarTouch() {
    val myTouchFrame = this.barsState.touchFrame
    if (myTouchFrame != null) {
        myTouchFrame.touchActive = false
    }
    val myBarView = this.barsState.topBarView
    if (myBarView != null) {
        myBarView.barTouched = false
    }
    val bottomBarView = this.barsState.bottomBarView
    if (bottomBarView != null) {
        bottomBarView.barTouched = false
    }
}

fun WebViewActivity.updateTransIcon(urlChanged: kotlin.Boolean) {
    val pageColor: Int
    val pageLight: Int
    if ((urlChanged == true)) {
        setBarAddress(barAddressText())
    } else {
        val showTrans = showTransIcon()
        if (PdfSettings.topToBottom.value) {
            pageColor = 0
            pageLight = 0
        } else {
            pageColor = pageThemeColor()
            pageLight = pageThemeLight()
        }
        val myBarView = this.barsState.topBarView
        if (myBarView != null) {
            val topTint = MainUtil.barTint(pageColor, this.onStartPage)
            val myIconView = myBarView.addrFace
            if (myIconView != null) {
                myIconView.setupIcons(this, showTrans, topTint)
                myIconView.setTrnsPreColor(if (showTrans) themeIconColor(topTint, pageLight) else 0)
            }
        }
        val bottomBarView = this.barsState.bottomBarView
        if (bottomBarView != null) {
            val bottomTint = MainUtil.barTint(MainUtil.bottomBarColor(), this.onStartPage)
            val addrFace = bottomBarView.addrFace
            if (addrFace != null) {
                addrFace.setupIcons(this, showTrans, bottomTint)
                addrFace.setTrnsPreColor(if (showTrans) themeIconColor(bottomTint, MainUtil.bottomBarLight()) else 0)
            }
        }
    }
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaUpdateTransFloat(this, urlChanged = (urlChanged) == true))
}

fun WebViewActivity.showMainMenu(view: View?) {
    val bottomBar: MyBarView?
    val topBar: MyBarView?
    if (this.tabsState.gestureDetector == null || this.isClosing || isAnyDialogOpen()) {
        return
    }
    if (MainSettings.menuType.value == 0 && this.mainMenuBusy) {
        return
    }
    closeMenuDialogs()
    if (this.currentPage != null) {
        this.mainMenuAnchor = view
        this.mainMenuActionDone = false
        this.mainMenuBusy = false
        val sheetMenu = MainSettings.menuType.value == 0 && ((this.barsState.bottomBarView.also {
            bottomBar = it
        }) == null || !bottomBar!!.hasMenu) && (((this.barsState.topBarView.also {
            topBar = it
        }) != null && topBar!!.hasMenu) || !hasTopTitle())
        if (this.mainMenuItems == null) {
            this.mainMenuItems = MainUtil.parseIntList(MainSettings.menuItems.value)
        }
        if (this.mainMenuItems2 == null) {
            this.mainMenuItems2 = MainUtil.parseIntList(MainSettings.midItems.value)
        }
        val landscape = isLandscapeScreen()
        val blockedCount = if (BrowserSettings.adsBlock.value || BrowserSettings.popBlock.value != 0) this.currentPage!!.blockedCount else 0
        var theme = AppR.style.DialogExpandTheme
        if (MainSettings.menuType.value == 0 || landscape) {
            theme = 0
        }
        val mainMenuActions = MainMenuActions(this)
        if (MainSettings.menuType.value == 0) {
            this.dialogsState.menuListDialog = DialogMenuList(
                this,
                this.dialogsState.appContext,
                this.dialogsState.webBody,
                this.mainMenuAnchor,
                this.mainMenuItems,
                this.mainMenuItems2,
                sheetMenu,
                this.tabsState.layoutRtl,
                blockedCount,
                mainMenuActions
            )
            return
        }
        val dialogMenuMain = DialogMenuMain(
            this,
            theme,
            this.mainMenuItems,
            this.mainMenuItems2,
            landscape,
            this.tabsState.layoutRtl,
            blockedCount,
            mainMenuActions
        )
        this.menuMainDialog = dialogMenuMain
        dialogMenuMain.setOnDismissListener(MenuDialogsDismissed(this))
        if (MainConst.isApi30Insets || theme != 0) {
            this.menuMainDialog!!.applyColors(this.onStartPage, useDarkWindow(), pageThemeColor(), navBarColorNow(), false)
        }
    }
}

fun WebViewActivity.setUpEditorBar() {
    if (this.barsState.editorBar == null || this.editorBarReady) {
        return
    }
    this.editorBarReady = true
    this.engineIcon!!.applyShape(false, true)
    this.barsState.addrLeftIcon!!.applyShape(false, true)
    this.barsState.addrRightIcon!!.applyShape(false, true)
    this.deleteIcon!!.applyShape(false, true)
    this.barsState.editorBar!!.useExtraIds(MainUtil.parseIntList(MainSettings.addrItems.value))
    this.barsState.editorBar!!.setListener(WvaMainBarListener(this))
    this.engineIcon!!.updateEngineIcon()
    this.engineIcon!!.setOnClickListener(EngineMenuClick(this))
    this.barsState.addrLeftIcon!!.setOnClickListener(WvaEditorClearClick(this))
    this.barsState.addrRightIcon!!.setOnClickListener(WvaEditorCopyUrlClick(this))
    this.deleteIcon!!.setOnClickListener(ClearRecentClick(this))
    this.editAuto!!.setInputType(17)
    this.editAuto!!.setOldKeyListener(this.keyBackListener)
    this.editAuto!!.setEditAutoListener(WvaEditorAutoListener(this))
    this.editAuto!!.setOnEditorActionListener(WvaEditorAction(this))
    layoutBars()
    val handler: Handler? = if (this.pageState.startupBusy) null else this.mainHandler
    if (this.pageState.startupBusy || handler == null) {
        return
    }
    handler.post(WvaEditorBarColors(this))
}

fun WebViewActivity.createEditorBar() {
    if (this.barsState.editorBar != null) {
        return
    }
    val editText = AppR.id.edit_text
    val editorBar = com.mycompany.app.view.EditorBar(this)
    editorBar.setVisibility(View.GONE)
    val myEditAuto = MyEditAuto(this)
    myEditAuto.setId(editText)
    myEditAuto.setGravity(16)
    myEditAuto.setFocusable(true)
    myEditAuto.setFocusableInTouchMode(true)
    myEditAuto.setSingleLine(true)
    myEditAuto.setTextDirection(3)
    myEditAuto.setTextSize(1, 16.0f)
    myEditAuto.setTextCursorDrawable(AppR.drawable.edit_cursor)
    myEditAuto.setHint(AppR.string.web_edit_hint)
    myEditAuto.setImeOptions(268435459)
    myEditAuto.setSelectAllOnFocus(true)
    myEditAuto.setBackground(null)
    editorBar.setEdit(myEditAuto)
    this.barsState.editorBar = editorBar
    this.engineIcon = editorBar.engineIcon
    this.barsState.addrLeftIcon = editorBar.leftIcon
    this.barsState.addrRightIcon = editorBar.rightIcon
    this.deleteIcon = editorBar.deleteIcon
    this.editAuto = myEditAuto
    this.editorBarReady = false
    this.barsState.barLayoutMode = 0.toByte()
    this.barsState.editorBarColor = 1234
}

fun WebViewActivity.openFindBar(startText: String?) {
    if (this.findView == null) {
        val myFindView = MyFindView(this)
        this.findView = myFindView
        val webNestView = this.currentPage
        val singleBar = isSingleBarMode()
        val exitListener: WvaExitFindOnApply = WvaExitFindOnApply(this)
        myFindView.findWeb = webNestView
        myFindView.startText = startText
        myFindView.tintBar = singleBar
        myFindView.closeListener = exitListener
        val findView = this.findView
        if (findView != null) {
            if (findView.tintBar) {
                this.topBarFrame!!.setChild(findView, 3)
                this.topBarFrame!!.setVisibility(View.VISIBLE)
                this.bottomBarFrame!!.setVisibility(View.GONE)
                var barHeight = MainUtil.topBarSize()
                val minHeight = MainApp.dp48
                if (barHeight < minHeight) {
                    barHeight = minHeight
                }
                val bars = this.barScroll
                if (bars != null) {
                    bars.setRange(barHeight)
                    val webNestLayout = this.pageLayout
                    if (webNestLayout != null) {
                        if (BrowserSettings.fixTop.value) {
                            webNestLayout.setTranslationY((barHeight - MainUtil.topBarHeight()).toFloat())
                        } else {
                            webNestLayout.setTranslationY(barHeight.toFloat())
                        }
                    }
                }
            } else {
                this.bottomBarFrame!!.setChild(findView, 3)
                this.topBarFrame!!.setVisibility(View.VISIBLE)
                this.bottomBarFrame!!.setVisibility(View.VISIBLE)
                var barHeight = MainUtil.bottomBarHeightPref()
                val minHeight = MainApp.dp48
                if (barHeight < minHeight) {
                    barHeight = minHeight
                }
                if (BrowserSettings.fixBot.value) {
                    if (barHeight != 0) {
                        this.barsState.findBarMargin = barHeight
                        setPageBottomMargin(barHeight)
                    } else {
                        setPageBottomMargin(this.barsState.findBarMargin)
                        this.barsState.findBarMargin = 0
                    }
                }
            }
        }
        val handler = this.mainHandler
        if (handler == null) {
            return
        }
        handler.post(WvaFindViewRefresh(this))
    }
}

fun WebViewActivity.addTabBar(themeColor: Int, themeLight: Int) {
    if (this.barsState.touchFrame != null || this.topBarFrame == null || this.bottomBarFrame == null) {
        return
    }
    this.barsState.touchFrame = MyTouchFrame(this)
    this.tabBarView = ComposeTabBar(this)
    this.barsState.touchFrame!!.addView(this.tabBarView, -1, -1)
    val tabBar = BrowserSettings.tabBar.value
    if (tabBar != 0) {
        if (tabBar == 1 || tabBar == 2) {
            this.topBarFrame!!.setChild(this.barsState.touchFrame, 2)
        } else {
            this.bottomBarFrame!!.setChild(this.barsState.touchFrame, 2)
        }
    }
    val tabBarHeight = PdfSettings.tabAdd.value
    this.tabBarHeight = tabBarHeight
    if (tabBarHeight != 0) {
        val marginLayoutParams = this.tabBarView!!.getLayoutParams() as MarginLayoutParams?
        if (marginLayoutParams != null) {
            if (PdfSettings.tabAdd.value == 3) {
                marginLayoutParams.setMargins(0, 0, MainApp.dp48, 0)
            } else {
                marginLayoutParams.setMargins(MainApp.dp48, 0, 0, 0)
            }
        }
        addSideButton(themeColor, themeLight)
    }
}

fun WebViewActivity.barAddressText(): String? {
    var shownUrl: String? = null
    if (this.onStartPage || TextUtils.isEmpty(this.dialogsState.pageUrl)) {
        return null
    }
    if (PdfSettings.addrType.value == 2) {
        return pageTitleOf(this.currentPage, this.dialogsState.pageUrl)
    }
    if (!this.onStartPage) {
        shownUrl = this.dialogsState.pageUrl
        val webNestView = this.currentPage
        if (webNestView != null) {
            shownUrl = webNestView.displayUrl(shownUrl)
        }
    }
    if (sameObject(shownUrl, this.barsState.barAddressSource)) {
        return this.barsState.barAddressCached
    }
    this.barsState.barAddressSource = shownUrl
    val address = MainUtil.pathOfUrl(shownUrl, true)
    this.barsState.barAddressCached = address
    return address
}

fun WebViewActivity.pickBarItem(pos: Int, y: Int) {
    val myBarView = this.barsState.bottomBarView
    if (myBarView != null) {
        val ids = myBarView.shownIds
        val length = if (ids != null) ids.size else 0
        var barItem: MyBarView.BarItem? = null
        if (length != 0) {
            val index = myBarView.itemAt(pos, y)
            if (index >= 0 && index < length) {
                val actionId = myBarView.shownIds!![index]
                if (actionId != 0 && actionId != 68 && actionId >= 0 && actionId < com.mycompany.app.main.MainConst.mainMenuNames.size) {
                    barItem = MyBarView.BarItem()
                    barItem.myBarView = myBarView
                    barItem.index = actionId
                }
            }
        }
        if (barItem == null) {
            return
        }
        this.tabsState.touchBarView = barItem.myBarView
        this.tabsState.touchBarItem = barItem.index
    }
}

fun WebViewActivity.buildMainBars(hostText: String?, hostColor: Int, count: Int, title: String?, barWidth: Int) {
    val pageColor: Int
    val pageLight: Int
    val items = MainUtil.parseIntList(MainSettings.topItems.value)
    val showTrans = showTransIcon()
    if (PdfSettings.topToBottom.value) {
        pageColor = 0
        pageLight = 0
    } else {
        pageColor = pageThemeColor()
        pageLight = pageThemeLight()
    }
    val topTint = MainUtil.barTint(pageColor, this.onStartPage)
    val myBarView = this.barsState.topBarView
    if (myBarView != null) {
        myBarView.setupBar(this, items, hostText, title, hostColor, showTrans, this.currentTabIndex, count, this.onStartPage, topTint, pageLight, barWidth, 1)
        return
    }
    if (items == null || items.size == 0) {
        return
    }
    val newBar = MyBarView(this)
    this.barsState.topBarView = newBar
    newBar.setupBar(this, items, hostText, title, hostColor, showTrans, this.currentTabIndex, count, this.onStartPage, topTint, pageLight, barWidth, 1)
    if (PdfSettings.topToBottom.value) {
        this.bottomBarFrame!!.setChild(this.barsState.topBarView, 0)
    } else {
        this.topBarFrame!!.setChild(this.barsState.topBarView, 0)
    }
    this.barsState.topBarView!!.setListener(WvaTopBarListener(this))
    if (ZoneSettings.downPos.value == 1) {
        placeDownIcons()
    }
    if (ZoneSettings.youPos.value == 1) {
        placeYouIcons()
    }
}

fun WebViewActivity.resetGestureState(alert: kotlin.Boolean, resetIcons: kotlin.Boolean) {
    if ((alert == true)) {
        this.floatingState.gestureMode = 2.toByte()
        this.floatingState.gestureZone = 0.toByte()
        this.tabsState.gestureDirection = 0.toByte()
        this.barsState.edgeDirection = 0.toByte()
        this.floatingState.edgeHint = 0.toByte()
        this.tabsState.tabSwipeActive = false
    }
    this.barsState.resetIcons = (resetIcons) == true
    this.barsState.barTouchDown = false
    this.barsState.overScrollMoves = 0
    this.barsState.scrolledMoves = 0
    this.barsState.barTouchDecided = false
    this.floatingState.floatingShown = false
}

fun WebViewActivity.resetTranslate(urlChanged: kotlin.Boolean) {
    this.dialogsState.transState = 0.toByte()
    this.transActive = true
    this.miscState.transMsgPending = false
    this.dialogsState.transReady = false
    this.transLangCode = null
    this.transText = null
    this.transRequested = true
    if (AlbumSettings.transMode.value == 0) {
        this.autoTransPending = false
    }
    updateTransIcon(urlChanged)
}

fun WebViewActivity.placeYouIcons() {
    val oldIcon: com.mycompany.app.ui.browser.BarIndicator? = if (ZoneSettings.youPos.value != 1) this.topYouIcon else null
    if (ZoneSettings.youPos.value != 1 && oldIcon != null) {
        removeFromParent(oldIcon)
        this.topYouIcon!!.release()
        this.topYouIcon = null
    }
    val myIconView: com.mycompany.app.ui.browser.BarIndicator? = if (ZoneSettings.youPos.value != 2) this.bottomYouIcon else null
    if (ZoneSettings.youPos.value != 2 && myIconView != null) {
        removeFromParent(myIconView)
        this.bottomYouIcon!!.release()
        this.bottomYouIcon = null
    }
    val webDownView: WebDownView? = if (ZoneSettings.youPos.value != 3) this.videoButton else null
    if (ZoneSettings.youPos.value != 3 && webDownView != null) {
        removeFromParent(webDownView)
        this.videoButton!!.release()
        this.videoButton = null
    }
    val youPos = ZoneSettings.youPos.value
    if (youPos == 1) {
        val uiHandler: Handler? = if (this.barsState.topBarView == null) null else this.mainHandler
        if (this.barsState.topBarView == null || uiHandler == null) {
            return
        }
        if (this.topYouIcon != null) {
            tintYouIcon()
            return
        } else {
            uiHandler.post(WvaShowTopYouButton(this))
            return
        }
    }
    if (youPos == 2) {
        val uiHandler: Handler? = if (this.barsState.bottomBarView == null) null else this.mainHandler
        if (this.barsState.bottomBarView == null || uiHandler == null) {
            return
        }
        if (this.bottomYouIcon != null) {
            tintYouIcon()
            return
        } else {
            uiHandler.post(WvaShowBottomYouButton(this))
            return
        }
    }
    val handler: Handler? = if (youPos != 3 || this.dialogsState.webBody == null) null else this.mainHandler
    if (youPos != 3 || this.dialogsState.webBody == null || handler == null) {
        return
    }
    val videoButton = this.videoButton
    if (videoButton != null) {
        videoButton.refreshIcon()
    } else {
        handler.post(WvaShowDownPanel(this))
    }
}

fun WebViewActivity.applyBarScrollRange() {
    val appBarLayout = this.barScroll ?: return
    var range = max(MainUtil.topBarHeight(), MainUtil.bottomInsetTotal())
    if (range == 0) {
        range = MainApp.dp48
    } else if (BrowserSettings.fixTop.value) {
        range += MainApp.dp48
    }
    appBarLayout.setRange(range)
    val webNestLayout = this.pageLayout
    if (webNestLayout == null) {
        return
    }
    if (BrowserSettings.fixTop.value) {
        webNestLayout.setTranslationY(0.0f)
    } else {
        webNestLayout.setTranslationY(MainUtil.topBarHeight().toFloat())
    }
}

fun WebViewActivity.setBarAddress(url: String?) {
    val pageColor: Int
    val pageLight: Int
    val webViewActivity: WebViewActivity?
    val host: String?
    val addressPath = addressPathText()
    val scheme = pageScheme(url)
    val showTrans = showTransIcon()
    if (PdfSettings.topToBottom.value) {
        pageColor = 0
        pageLight = 0
    } else {
        pageColor = pageThemeColor()
        pageLight = pageThemeLight()
    }
    val myBarView = this.barsState.topBarView
    if (myBarView != null) {
        val topTint = MainUtil.barTint(pageColor, this.onStartPage)
        val myIconView = myBarView.addrFace
        if (myIconView != null) {
            webViewActivity = this
            host = url
            myIconView.setupHost(scheme, topTint, webViewActivity, host, addressPath, showTrans)
            myIconView.setTrnsPreColor(if (showTrans) themeIconColor(topTint, pageLight) else 0)
        } else {
            webViewActivity = this
            host = url
        }
    } else {
        webViewActivity = this
        host = url
    }
    val bottomBarView = webViewActivity.barsState.bottomBarView
    if (bottomBarView != null) {
        val bottomTint = MainUtil.barTint(MainUtil.bottomBarColor(), webViewActivity.onStartPage)
        val addrFace = bottomBarView.addrFace
        if (addrFace != null) {
            addrFace.setupHost(scheme, bottomTint, webViewActivity, host, addressPath, showTrans)
            addrFace.setTrnsPreColor(if (showTrans) themeIconColor(bottomTint, MainUtil.bottomBarLight()) else 0)
        }
    }
}

fun WebViewActivity.tintBars(barsTinted: kotlin.Boolean) {
    this.dialogsState.barsTinted = (barsTinted) == true
    val pageColor = if (!PdfSettings.topToBottom.value) pageThemeColor() else 0
    val myBarView = this.barsState.topBarView
    if (myBarView != null) {
        myBarView.applyStarred(MainUtil.barTint(pageColor, this.onStartPage), barsTinted)
    }
    val bottomBarView = this.barsState.bottomBarView
    if (bottomBarView != null) {
        bottomBarView.applyStarred(MainUtil.barTint(MainUtil.bottomBarColor(), this.onStartPage), barsTinted)
    }
}

fun WebViewActivity.showBookmarkEditDialog() {
    if (this.currentPage == null || this.isClosing || isAnyDialogOpen()) {
        return
    }
    val dialogWebBookEdit = this.dialogsState.bookEditDialog
    if (dialogWebBookEdit != null) {
        dialogWebBookEdit.dismiss()
        this.dialogsState.bookEditDialog = null
    }
    val url = currentPageUrl(true)
    if (TextUtils.isEmpty(url)) {
        return
    }
    val dialog =
        DialogWebBookEdit(this, null, url, pageTitleOf(this.currentPage, url), WvaBookEditListener(this))
    this.dialogsState.bookEditDialog = dialog
    dialog.setOnDismissListener(WvaBookEditDialogDismissed(this))
}

fun WebViewActivity.cancelSearchTask() {
    val searchTask = this.searchTask
    if (searchTask != null) {
        searchTask.cancelled = true
    }
    this.searchTask = null
    val webSearchAdapter = this.searchAdapter
    if (webSearchAdapter != null) {
        webSearchAdapter.lastCount = webSearchAdapter.getItemCount()
        webSearchAdapter.closeConnection(false)
        val mainListLoader = webSearchAdapter.listLoader
        if (mainListLoader != null) {
            mainListLoader.release()
            webSearchAdapter.listLoader = null
        }
        webSearchAdapter.webSearchListener = null
        webSearchAdapter.historyItems = null
        webSearchAdapter.bookItems = null
        webSearchAdapter.suggestItems = null
        webSearchAdapter.query = null
        clearHandler(webSearchAdapter.mainHandler)
        webSearchAdapter.mainHandler = null
        webSearchAdapter.searchExecutor = null
        webSearchAdapter.listView = null
        this.searchAdapter = null
    }
    val myEditAuto: MyEditAuto? = if (!ZtwoSettings.autoComp.value) null else this.editAuto
    if (!ZtwoSettings.autoComp.value || myEditAuto == null) {
        return
    }
    myEditAuto.clearSuggestions()
}

fun WebViewActivity.setDownIconAlert(alert: kotlin.Boolean) {
    val pageColor = if (!PdfSettings.topToBottom.value) pageThemeColor() else 0
    val myBarView = this.barsState.topBarView
    if (myBarView != null) {
        val topTint = MainUtil.barTint(pageColor, this.onStartPage)
        myBarView.downAlert = (alert) == true
        val myIconView = myBarView.downIcon
        if (myIconView != null) {
            myIconView.setImageResource(
                if ((alert == true)) AppR.drawable.outline_download_for_offline_red_24 else iconResFor(
                    64,
                    topTint
                )
            )
            myBarView.applyIconAlpha(myBarView.downIcon, 64, topTint)
        }
    }
    val bottomBarView = this.barsState.bottomBarView
    if (bottomBarView != null) {
        val plainTint = MainUtil.barTint(0, this.onStartPage)
        bottomBarView.downAlert = (alert) == true
        val downIcon = bottomBarView.downIcon
        if (downIcon != null) {
            downIcon.setImageResource(
                if ((alert == true)) AppR.drawable.outline_download_for_offline_red_24 else iconResFor(
                    64,
                    plainTint
                )
            )
            bottomBarView.applyIconAlpha(bottomBarView.downIcon, 64, plainTint)
        }
    }
    val handler: Handler? = if ((alert == true) && needsDownIcon()) this.mainHandler else null
    if ((alert == true) && needsDownIcon() && handler != null) {
        handler.post(WvaPlaceDownIconsTask(this))
    }
}

fun WebViewActivity.isBarUpdatePending(): kotlin.Boolean {
    return this.dialogsState.barUpdateBusy || isBusy()
}

fun WebViewActivity.setBarIconsLoading(loading: kotlin.Boolean) {
    val myBarView = this.barsState.topBarView
    if (myBarView != null) {
        myBarView.setIconLoad(loading)
    }
    val bottomBarView = this.barsState.bottomBarView
    if (bottomBarView != null) {
        bottomBarView.setIconLoad(loading)
    }
}

fun WebViewActivity.needsDownIcon(): kotlin.Boolean {
    var myBarView: MyBarView? = null
    val downPos = ZoneSettings.downPos.value
    if (downPos != 1) {
        return downPos == 2 && (this.barsState.bottomBarView.also { myBarView = it }) != null && myBarView!!.downIcon == null
    }
    val topBarView = this.barsState.topBarView
    return topBarView != null && topBarView.downIcon == null
}

fun WebViewActivity.updateNavButtons() {
    if (this.currentPage != null) {
        var found: kotlin.Boolean? = null
        var enabled: kotlin.Boolean? = null
        run label72@{
            run label71@{
                val tabItems = this.tabsState.tabItems
                enabled = true
                if (tabItems != null && tabItems.size != 0) {
                    if (ZtwoSettings.addPage.value) {
                        val currentFrame = this.tabsState.currentFrame
                        val pageValid = currentFrame!!.pageValid
                        if (pageValid != null && pageValid.canGoBack()) {
                            return@label71
                        }

                        val pageItems = currentFrame.pageItems
                        if (pageItems != null && pageItems.size >= 2 && currentFrame.currentPageIndex > 0) {
                            return@label71
                        }
                    } else if (this.currentPage!!.canGoBack()) {
                        return@label71
                    }

                    if (this.currentTabIndex > 0) {
                        return@label71
                    }
                }

                found = false
                return@label72
            }
            found = true
        }

        var clickEnabled: kotlin.Boolean
        run label54@{
        if (ZtwoSettings.addPage.value) {
            val frame = this.tabsState.currentFrame
            val validPage = frame!!.pageValid
            if (validPage != null) {
                clickEnabled = (enabled) == true
                if (validPage.canGoForward()) {
                    return@label54
                }
            }

            val items = frame.pageItems
            if (items != null) {
                val count = items.size
                if (count >= 2) {
                    clickEnabled = (enabled) == true
                    if (frame.currentPageIndex < count - 1) {
                        return@label54
                    }
                }
            }

            clickEnabled = false
        } else {
            clickEnabled = this.currentPage!!.canGoForward()
        }
        }

        var topBarView = this.barsState.topBarView
        if (topBarView != null) {
            var backIcon = topBarView.backIcon
            if (backIcon != null) {
                backIcon.setClickEnabled(found!!)
            }

            backIcon = topBarView.forwardIcon
            if (backIcon != null) {
                backIcon.setClickEnabled(clickEnabled)
            }
        }

        topBarView = this.barsState.bottomBarView
        if (topBarView != null) {
            var backButton = topBarView.backIcon
            if (backButton != null) {
                backButton.setClickEnabled(found!!)
            }

            backButton = topBarView.forwardIcon
            if (backButton != null) {
                backButton.setClickEnabled(clickEnabled)
            }
        }
    }
}

fun WebViewActivity.updateTabCountIcons() {
    val arrayList = this.tabsState.tabItems
    val size = if (arrayList != null) arrayList.size else 0
    val pageColor = if (!PdfSettings.topToBottom.value) pageThemeColor() else 0
    val myBarView = this.barsState.topBarView
    if (myBarView != null) {
        val currentTabIndex = this.currentTabIndex
        val topTint = MainUtil.barTint(pageColor, this.onStartPage)
        val myIconView = myBarView.tabFace
        if (myIconView != null) {
            myIconView.setupTab(currentTabIndex, size, topTint)
        }
    }
    val bottomBarView = this.barsState.bottomBarView
    if (bottomBarView != null) {
        val tabIndex = this.currentTabIndex
        val plainTint = MainUtil.barTint(0, this.onStartPage)
        val tabFace = bottomBarView.tabFace
        if (tabFace != null) {
            tabFace.setupTab(tabIndex, size, plainTint)
        }
    }
}

fun WebViewActivity.setBarsDisabled(mode: Int, sslShown: kotlin.Boolean) {
    if (this.currentPage != null) {
        if ((sslShown == true)) {
            this.tabsState.barsDisabledMode = mode.toByte()
        } else {
            this.tabsState.barsDisabledMode = 0.toByte()
        }
        applyBodyColors(pageThemeColor(), navBarColorNow())
        if (this.tabsState.barsDisabledMode.toInt() != 3) {
            this.topBarFrame!!.setDisabled(sslShown)
        }
        if (!this.quickState.editingAddress) {
            this.bottomBarFrame!!.setVisibility(if ((sslShown == true)) View.INVISIBLE else View.VISIBLE)
        }
        this.currentPage!!.setErrorView(!sslShown)
        val webFltView = this.floatingState.lockButton
        if (webFltView != null) {
            if ((sslShown == true) || this.quickState.editingAddress) {
                webFltView.setVisibility(View.GONE)
            } else {
                webFltView.setVisibility(View.VISIBLE)
            }
        }
    }
}

fun WebViewActivity.applyYouIconColor(color: Int, tintColor: Int) {
    var color = color
    var tintColor = tintColor
    val myIconView = this.topYouIcon
    if (myIconView == null && this.bottomYouIcon == null) {
        return
    }
    if (PdfSettings.topToBottom.value) {
        color = 0
        tintColor = 0
    }
    val onStartPage = this.onStartPage
    if (myIconView == null) {
        color = 0
    }
    val tint = MainUtil.barTint(color, onStartPage)
    if (this.barsState.youIconColor != tint) {
        this.barsState.youIconColor = tint
        val videoCount = videoMenuCount(tint, false)
        val topYouIcon = this.topYouIcon
        if (topYouIcon != null) {
            topYouIcon.setImageResource(videoCount)
            this.topYouIcon!!.setAlpha(BarFace.pressScale(tint))
            this.topYouIcon!!.setBgPreColor(themeIconColor(tint, tintColor))
        }
        val bottomYouIcon = this.bottomYouIcon
        if (bottomYouIcon != null) {
            bottomYouIcon.setImageResource(videoCount)
            this.bottomYouIcon!!.setAlpha(BarFace.pressScale(tint))
            this.bottomYouIcon!!.setBgPreColor(themeIconColor(tint, tintColor))
        }
    }
}

fun WebViewActivity.tabsForSave(): ArrayList<Any?>? {
    val arrayList = this.tabsState.tabItems
    var changed: ArrayList<Any?>? = null
    if (arrayList != null && !arrayList.isEmpty()) {
        try {
            val size = arrayList.size
            var previousUid: Long = -1
            var position = 0
            var index = 0
            while (index < size) {
                val entry: Any? = arrayList.get(index)
                index++
                val webTabItem = entry as WebTabItem?
                if (webTabItem != null) {
                    val newParent = webTabItem.parentId != previousUid
                    webTabItem.parentId = previousUid
                    webTabItem.position = position
                    previousUid = webTabItem.uid
                    position++
                    if (newParent) {
                        if (changed == null) {
                            changed = ArrayList<Any?>()
                        }
                        val copy = WebTabItem()
                        copy.uid = webTabItem.uid
                        copy.parentId = webTabItem.parentId
                        changed.add(copy)
                    }
                }
            }
        } catch (unused: Exception) {
        }
    }
    return changed
}

fun WebViewActivity.tintYouIcon() {
    var pageColor = if (!PdfSettings.topToBottom.value) pageThemeColor() else 0
    val onStartPage = this.onStartPage
    if (this.topYouIcon == null) {
        pageColor = 0
    }
    val videoCount = videoMenuCount(MainUtil.barTint(pageColor, onStartPage), false)
    val myIconView = this.topYouIcon
    if (myIconView != null) {
        myIconView.setImageResource(videoCount)
        return
    }
    val bottomYouIcon = this.bottomYouIcon
    if (bottomYouIcon != null) {
        bottomYouIcon.setImageResource(videoCount)
    }
}

fun WebViewActivity.resumeAddressEditAfterAdd() {
    val editAfterAdd = this.barsState.addressEditAfterAdd && BrowserSettings.tabKeypad.value
    this.barsState.addressEditAfterAdd = false
    val myEditAuto: MyEditAuto? = if (!editAfterAdd) null else this.editAuto
    if (!editAfterAdd || myEditAuto == null) {
        return
    }
    myEditAuto.postDelayed(WvaStartAddressEdit(this), 200L)
}

fun WebViewActivity.edgeSwipeDirection(touchX: kotlin.Float, touchY: kotlin.Float): Int {
    if (this.miscState.touchDownEdge != 0 && abs(touchX - this.barsState.touchStartX) > abs(touchY - this.barsState.touchStartY)) {
        return if (touchX > this.barsState.touchStartX) 1 else 2
    }
    return 0
}

fun WebViewActivity.pageThemeLight(): Int {
    val webNestView = this.currentPage
    val themeHead = BrowserSettings.headMode()
    if (themeHead == 0) {
        return 0
    }
    if (themeHead == 2) {
        val iconColor = PrefWeb.iconColor
        if (iconColor != 0) {
            return iconColor
        }
        MainUtil.resetTextSizePref()
        return PrefWeb.iconColor
    }
    if (this.onStartPage || webNestView == null) {
        return 0
    }
    if (!webNestView.blankPending) {
        return webNestView.themeLight
    }
    webNestView.setBarColors(this.dialogsState.themeColor, this.miscState.themeLight)
    return this.miscState.themeLight
}

fun WebViewActivity.showAppBlockedSnackbar(siteUrl: String?, url: String?) {
    val snackItem = SnackItem()
    snackItem.siteUrl = siteUrl
    snackItem.url = url
    updateUpButton(false)
    closeAllSnackbars()
    val mySnackbar = MySnackbar(this)
    this.dialogsState.appBlockSnackbar = mySnackbar
    mySnackbar.setAppBarView(this.barScroll)
    this.dialogsState.appBlockSnackbar!!.snackItem = snackItem
    this.dialogsState.appBlockSnackbar!!.setActivity(this)
    val urlEmpty = TextUtils.isEmpty(url)
    val appBlockSnackbar = this.dialogsState.appBlockSnackbar
    val myWebCoord = this.webCoord
    if (urlEmpty) {
        val appBlockNoti = AppR.string.app_block_noti
        val wvaShowAppBlockedSnackbarListener = WvaShowAppBlockedSnackbarListener(this)
        appBlockSnackbar!!.snackType = 3.toByte()
        appBlockSnackbar.siteUrl = siteUrl
        appBlockSnackbar.show(myWebCoord, appBlockNoti, 1, 0, wvaShowAppBlockedSnackbarListener)
        return
    }
    val messageRes = AppR.string.app_block_noti
    val wvaAppBlockedSnackbarTask = WvaAppBlockedSnackbarTask(this)
    appBlockSnackbar!!.snackType = 3.toByte()
    appBlockSnackbar.siteUrl = siteUrl
    appBlockSnackbar.show(myWebCoord, messageRes, 4, 1, wvaAppBlockedSnackbarTask)
}

fun WebViewActivity.refreshBarFrames() {
    val myBarFrame = this.topBarFrame
    if (myBarFrame != null) {
        myBarFrame.updateLayout()
    }
    val bottomBarFrame = this.bottomBarFrame
    if (bottomBarFrame != null) {
        bottomBarFrame.updateLayout()
    }
}

fun WebViewActivity.pageTitleOf(webView: WebView?, pageUrl: String?): String? {
    if (webView != null) {
        val title = webView.getTitle()
        if (!TextUtils.isEmpty(title)) {
            val siteName = MainUtil.siteNameOf(title, false)
            if (!TextUtils.isEmpty(siteName)) {
                return siteName
            }
        }
    }
    val hostLabel = hostLabelUpper(hostNameOf(pageUrl, true))
    return if (!TextUtils.isEmpty(hostLabel)) hostLabel else getString(AppR.string.no_title)
}

fun WebViewActivity.showDownIcons(show: kotlin.Boolean) {
    val myIconView = this.topDownIcon
    if (myIconView != null) {
        if ((show == true) && ZoneSettings.downPos.value == 1) {
            myIconView.setVisibility(View.VISIBLE)
        } else {
            myIconView.setVisibility(View.GONE)
        }
    }
    val bottomDownIcon = this.barsState.bottomDownIcon
    if (bottomDownIcon != null) {
        if ((show == true) && ZoneSettings.downPos.value == 2) {
            bottomDownIcon.setVisibility(View.VISIBLE)
        } else {
            bottomDownIcon.setVisibility(View.GONE)
        }
    }
}

fun WebViewActivity.openTextLink(url: String?): kotlin.Boolean {
    val host = hostOfUrl(url, true)
    if (TextUtils.isEmpty(host)) {
        return false
    }
    if (host == "github.com") {
        if (url!!.endsWith("?raw=true")) {
            openTextViewer(url, pageTitleOf(this.currentPage, url))
            return true
        }
    } else if (host == "raw.githubusercontent.com") {
        val mime = mimeForUrl(url)
        if (!TextUtils.isEmpty(mime) && mime!!.startsWith("text")) {
            openTextViewer(url, pageTitleOf(this.currentPage, url))
            return true
        }
    } else if (host!!.contains("easylist") && url!!.endsWith(".txt")) {
        openTextViewer(url, pageTitleOf(this.currentPage, url))
        return true
    }
    return false
}

fun WebViewActivity.barColorsChanged(color: Int): kotlin.Boolean {
    var color = color
    if (this.dialogsState.themeColor != color) {
        return true
    }
    val myBarView = this.barsState.topBarView
    if (myBarView != null) {
        if (PdfSettings.topToBottom.value) {
            color = 0
        }
        if (myBarView.colorType != MainUtil.barTint(color, this.onStartPage)) {
            return true
        }
    }
    val bottomBarView = this.barsState.bottomBarView
    return if (bottomBarView == null || bottomBarView.colorType == MainUtil.barTint(
            0,
            this.onStartPage
        )
    ) false else true
}

fun WebViewActivity.showYouIcons(show: kotlin.Boolean) {
    val myIconView = this.topYouIcon
    if (myIconView != null) {
        if ((show == true) && ZoneSettings.youPos.value == 1) {
            myIconView.setVisibility(View.VISIBLE)
        } else {
            myIconView.setVisibility(View.GONE)
        }
    }
    val bottomYouIcon = this.bottomYouIcon
    if (bottomYouIcon != null) {
        if ((show == true) && ZoneSettings.youPos.value == 2) {
            bottomYouIcon.setVisibility(View.VISIBLE)
        } else {
            bottomYouIcon.setVisibility(View.GONE)
        }
    }
}

fun WebViewActivity.closeFindBar() {
    val myFindView = this.findView
    if (myFindView != null) {
        if (myFindView.tintBar) {
            myFindView.release()
            this.topBarFrame!!.removeChild(this.findView)
            this.findView = null
            this.topBarFrame!!.setVisibility(View.VISIBLE)
            this.bottomBarFrame!!.setVisibility(View.VISIBLE)
            applyBarScrollRange()
        } else {
            myFindView.release()
            this.bottomBarFrame!!.removeChild(this.findView)
            this.findView = null
            this.topBarFrame!!.setVisibility(View.VISIBLE)
            this.bottomBarFrame!!.setVisibility(View.VISIBLE)
            if (BrowserSettings.fixBot.value) {
                setPageBottomMargin(this.barsState.findBarMargin)
                this.barsState.findBarMargin = 0
            }
        }
        val appBarLayout = this.barScroll
        if (appBarLayout != null) {
            appBarLayout.setExpanded(true)
        }
    }
}

fun WebViewActivity.applyDownIconColor(color: Int, tintColor: Int) {
    var color = color
    var tintColor = tintColor
    val myIconView = this.topDownIcon
    if (myIconView == null && this.barsState.bottomDownIcon == null) {
        return
    }
    if (PdfSettings.topToBottom.value) {
        color = 0
        tintColor = 0
    }
    val onStartPage = this.onStartPage
    if (myIconView == null) {
        color = 0
    }
    val tint = MainUtil.barTint(color, onStartPage)
    if (this.downIconColor != tint) {
        this.downIconColor = tint
        val downCount = downMenuCount(tint, false)
        val topDownIcon = this.topDownIcon
        if (topDownIcon != null) {
            topDownIcon.setImageResource(downCount)
            this.topDownIcon!!.setAlpha(BarFace.pressScale(tint))
            this.topDownIcon!!.setBgPreColor(themeIconColor(tint, tintColor))
        }
        val bottomDownIcon = this.barsState.bottomDownIcon
        if (bottomDownIcon != null) {
            bottomDownIcon.setImageResource(downCount)
            this.barsState.bottomDownIcon!!.setAlpha(BarFace.pressScale(tint))
            this.barsState.bottomDownIcon!!.setBgPreColor(themeIconColor(tint, tintColor))
        }
    }
}

fun WebViewActivity.tintDownIcon() {
    var pageColor = if (!PdfSettings.topToBottom.value) pageThemeColor() else 0
    val onStartPage = this.onStartPage
    if (this.topDownIcon == null) {
        pageColor = 0
    }
    val downCount = downMenuCount(MainUtil.barTint(pageColor, onStartPage), false)
    val myIconView = this.topDownIcon
    if (myIconView != null) {
        myIconView.setImageResource(downCount)
        return
    }
    val bottomDownIcon = this.barsState.bottomDownIcon
    if (bottomDownIcon != null) {
        bottomDownIcon.setImageResource(downCount)
    }
}

fun WebViewActivity.setAddressEditing(editing: kotlin.Boolean) {
    var myWebCoord: MyWebCoord? = null
    val found: kotlin.Boolean
    if (this.pageState.addressEditBusy || this.editAuto == null || this.quickState.editingAddress == editing) {
        return
    }
    this.quickState.editingAddress = (editing) == true
    this.pageState.addressEditBusy = (editing) == true
    val pageColor = pageThemeColor()
    val pageLight = pageThemeLight()
    if (this.videoState.quickSchEdit == null) {
        lockBarScroll(editing)
    }
    val myBarFrame = this.topBarFrame
    if (myBarFrame != null) {
        if (!editing) {
            myBarFrame.setVisibility(View.VISIBLE)
        } else if (isSingleBarMode()) {
            this.topBarFrame!!.setVisibility(View.GONE)
        }
    }
    val bottomBarFrame = this.bottomBarFrame
    if (bottomBarFrame != null) {
        if (MainConst.isApi30) {
            if ((editing == true)) {
                bottomBarFrame.setVisibility(View.GONE)
            } else {
                bottomBarFrame.setVisibility(View.VISIBLE)
            }
        } else if ((editing == true)) {
            bottomBarFrame.setVisibility(View.GONE)
        } else {
            val webCoord = this.webCoord
            if (webCoord != null) {
                webCoord.postDelayed(WvaShowBottomBarFrame(this), 200L)
            }
        }
    }
    layoutBars()
    val myAddrView = this.barsState.editorBar
    if (myAddrView != null) {
        if ((editing == true)) {
            var addressText = this.dialogsState.addressMenuText
            if (TextUtils.isEmpty(addressText)) {
                if (this.onStartPage) {
                    addressText = null
                } else {
                    addressText = this.dialogsState.pageUrl
                    val webNestView = this.currentPage
                    if (webNestView != null) {
                        addressText = webNestView.displayUrl(addressText)
                    }
                }
                if (TextUtils.isEmpty(addressText)) {
                    found = true
                } else {
                    found = false
                }
            } else {
                found = false
            }
            if (found) {
                this.editAuto!!.setText(null as CharSequence?)
            }
            this.editAuto!!.setTextAt(-1, addressText)
            setAddressEditMode(found)
            if (this.barsState.editorBar != null) {
                val singleBar = isSingleBarMode()
                val topTint = MainUtil.barTint(if (singleBar) pageColor else 0, this.onStartPage)
                val darkBars = isDarkBars(this.onStartPage)
                if (this.barsState.editorBarColor != topTint) {
                    this.barsState.editorBarColor = topTint
                    this.barsState.editorBarDark = darkBars
                    val iconColor = themeIconColor(topTint, pageLight)
                    this.barsState.editorBar!!.applyExtraTint(topTint, iconColor)
                    this.barsState.editorBar!!.applyColors(topTint, pageLight, darkBars, singleBar)
                    this.editAuto!!.setTextColor(BarFace.pressColorFor(topTint, false))
                    this.editAuto!!.setHintTextColor(BarFace.pressColorFor(topTint, true))
                    val myIconView = this.barsState.addrLeftIcon
                    if (topTint == 0) {
                        myIconView!!.setImageResource(AppR.drawable.outline_cancel_black_18)
                        this.barsState.addrRightIcon!!.setImageResource(AppR.drawable.outline_content_copy_black_18)
                        this.deleteIcon!!.setImageResource(AppR.drawable.outline_delete_black_20)
                    } else {
                        myIconView!!.setImageResource(AppR.drawable.outline_cancel_dark_18)
                        this.barsState.addrRightIcon!!.setImageResource(AppR.drawable.outline_content_copy_dark_18)
                        this.deleteIcon!!.setImageResource(AppR.drawable.outline_delete_dark_20)
                    }
                    val pressScale = BarFace.pressScale(topTint)
                    this.barsState.addrLeftIcon!!.setMaxAlpha(pressScale)
                    this.barsState.addrRightIcon!!.setMaxAlpha(pressScale)
                    this.deleteIcon!!.setMaxAlpha(pressScale)
                    this.engineIcon!!.setBgPreColor(iconColor)
                    this.barsState.addrLeftIcon!!.setBgPreColor(iconColor)
                    this.barsState.addrRightIcon!!.setBgPreColor(iconColor)
                    this.deleteIcon!!.setBgPreColor(iconColor)
                } else if (this.barsState.editorBarDark != darkBars) {
                    this.barsState.editorBarDark = darkBars
                    this.barsState.editorBar!!.applyColors(topTint, pageLight, darkBars, singleBar)
                } else {
                    val editorBar = this.barsState.editorBar
                    if (singleBar != editorBar!!.editing) {
                        editorBar.applyColors(topTint, pageLight, darkBars, singleBar)
                    }
                }
                this.engineIcon!!.updateEngineIcon()
            }
            this.barsState.editorBar!!.setVisibility(View.VISIBLE)
        } else {
            myAddrView.setVisibility(View.GONE)
        }
    }
    if ((editing == true)) {
        this.barScroll!!.setExpanded(true)
        hideFloatingButtons()
        val webFltView = this.floatingState.lockButton
        if (webFltView != null) {
            webFltView.setVisibility(View.GONE)
        }
    } else {
        closeEngineMenu()
        closeAddressPrefixMenu()
        updateBarBehavior()
        val lockButton = this.floatingState.lockButton
        if (lockButton != null) {
            lockButton.setVisibility(View.VISIBLE)
        }
        applyPageBarColor(pageColor, pageLight)
        updateTransFloatButton(true)
        updateUpButton(true)
        updateImageButton(true)
        updateVideoButton(true)
        showUserButtons(true)
        updatePullButton(true)
        if (!ZtwoSettings.quickDown.value) {
            updateRssButton(true)
        }
        updateTranslateButton(true)
        updateZoomButton(true)
        cancelSearchTask()
    }
    if (!editing) {
        val currentPage = this.currentPage
        if (currentPage != null) {
            currentPage.setQuickSearch(false)
            applyEditColor(false)
            val quickSearch = this.quickSearch
            if (quickSearch != null) {
                quickSearch.applyState(false, this.onStartPage, !isSingleBarMode())
            }
            closeQuickSubView()
        }
    } else if (this.quickState.editingAddress && (this.webCoord.also { myWebCoord = it }) != null) {
        myWebCoord!!.post(WvaShowQuickSearch(this))
    }
    val myEditAuto = this.editAuto
    if (myEditAuto == null) {
        return
    }
    if ((editing == true)) {
        myEditAuto.post(WvaEditorFocusSequence(this))
    } else {
        closeKeyboard(this.dialogsState.appContext, myEditAuto)
    }
}

fun WebViewActivity.playVideoInPlayer(url: String?, referer: String?, externalFlag: kotlin.Boolean) {
    if (this.barsState.playVideoInPlayerBusy) {
        return
    }
    this.barsState.playVideoInPlayerBusy = true
    val myWebCoord = this.webCoord
    if (myWebCoord == null) {
        return
    }
    myWebCoord.post(WvaPlayVideoInPlayerTask(this, external = (externalFlag) == true, url = url, referer = referer))
}

fun WebViewActivity.layoutBars() {
    var editorHeight: Int
    val editorGravity: Int
    var keyboardPad: Int
    val topPad: Int
    if (this.barsState.editorBar == null || this.webCoord == null) {
        return
    }
    val firstLayout = this.barsState.barLayoutMode.toInt() == 0
    val myBarView = this.barsState.bottomBarView
    if (myBarView == null || !myBarView.hasAddress) {
        val topBarView = this.barsState.topBarView
        if (topBarView == null || !topBarView.hasAddress) {
            if (hasTopTitle()) {
                this.barsState.barLayoutMode = 2.toByte()
            } else {
                this.barsState.barLayoutMode = 1.toByte()
            }
        } else if (PdfSettings.topToBottom.value) {
            this.barsState.barLayoutMode = 2.toByte()
        } else {
            this.barsState.barLayoutMode = 1.toByte()
        }
    } else {
        this.barsState.barLayoutMode = 2.toByte()
    }
    if (this.quickSearch != null) {
        if (isSingleBarMode()) {
            topPad = barSizeFor(true)
            keyboardPad = 0
        } else {
            keyboardPad = barSizeFor(false)
            topPad = 0
        }
        if (MainConst.isApi30) {
            keyboardPad += this.keyboardBottomMargin
        }
        val quickSearch = this.quickSearch
        if (quickSearch!!.padTop != topPad || quickSearch.padKeyboard != keyboardPad) {
            quickSearch.padTop = topPad
            quickSearch.padKeyboard = keyboardPad
            quickSearch.setPadding(quickSearch.padLeft, topPad, quickSearch.padRight, quickSearch.padBottom + keyboardPad)
            quickSearch.updateBodyPadding()
        }
    }
    if (this.editAuto != null) {
        val bottomPad = if (isSingleBarMode()) 0 else this.keyboardBottomMargin
        if (this.barsState.editorBar!!.getPaddingBottom() != bottomPad) {
            this.barsState.editorBar!!.setPadding(0, 0, 0, bottomPad)
        }
        if (isSingleBarMode()) {
            editorHeight = barSizeFor(true)
            editorGravity = 48
        } else {
            editorHeight = barSizeFor(false)
            if (MainConst.isApi30) {
                editorHeight += this.keyboardBottomMargin
            }
            editorGravity = 80
        }
        if (firstLayout) {
            val layoutParams = com.mycompany.app.ui.browser.OverlayParams(-1, editorHeight)
            layoutParams.gravity = editorGravity
            this.floatingState.overlays!!.addCoordView(this.barsState.editorBar, layoutParams)
            return
        }
        val params = this.floatingState.overlays?.params(this.barsState.editorBar)
        if (params != null) {
            if (params.height == editorHeight && params.gravity == editorGravity) {
                return
            }
            params.height = editorHeight
            params.gravity = editorGravity
            this.floatingState.overlays?.relayout(this.barsState.editorBar)
        }
    }
}

fun WebViewActivity.updateDownBadge() {
    this.videoDownCount = 0
    val webNestView = this.currentPage
    if (webNestView != null) {
        val myIconView = this.topDownIcon
        if (myIconView != null) {
            myIconView.showDot(webNestView.downNoti, true)
            return
        }
        val bottomDownIcon = this.barsState.bottomDownIcon
        if (bottomDownIcon != null) {
            bottomDownIcon.showDot(webNestView.downNoti, true)
            return
        }
        val webDownView = this.imageButton
        if (webDownView != null) {
            webDownView.setNoti(webNestView.downNoti)
        }
    }
}

fun WebViewActivity.setAddressEditMode(editing: kotlin.Boolean) {
    val myAddrView = this.barsState.editorBar
    if (myAddrView == null) {
        return
    }
    myAddrView.showExtras(editing)
    val myIconView = this.barsState.addrLeftIcon
    if (editing) {
        myIconView!!.setVisibility(8)
        this.barsState.addrRightIcon!!.setVisibility(8)
        this.deleteIcon!!.setVisibility(8)
        return
    }
    myIconView!!.setVisibility(0)
    this.barsState.addrRightIcon!!.setVisibility(0)
    val recentEnabled = DbBookRecent.isEnabled()
    val deleteIcon = this.deleteIcon
    if (recentEnabled) {
        deleteIcon!!.setVisibility(0)
    } else {
        deleteIcon!!.setVisibility(8)
    }
}

fun WebViewActivity.removeTabAt(index: Int) {
    synchronized(this.tabLock) {
        val tab = tabItemAt(index)
        if (tab == null) {
            return
        }
        forgetTabItem(tab)
        this.tabsState.tabItems!!.removeAt(index)
        val currentTabIndex = this.currentTabIndex
        if (index <= currentTabIndex) {
            this.currentTabIndex = currentTabIndex - 1
        }
        clampTabIndex()
        val uid = tab.uid
        DbBookTab.updateParents(this.dialogsState.appContext, (tabsForSave() as ArrayList<WebTabItem?>?))
        DbBookTab.deleteById(this.dialogsState.appContext, uid)
    }
}

fun WebViewActivity.buildBottomBar(hostText: String?, hostColor: Int, count: Int, title: String?, barWidth: Int) {
    val items = MainUtil.parseIntList(MainSettings.botItems.value)
    val showTrans = showTransIcon()
    val bottomTint = MainUtil.barTint(MainUtil.bottomBarColor(), this.onStartPage)
    val light = MainUtil.bottomBarLight()
    val myBarView = this.barsState.bottomBarView
    if (myBarView != null) {
        myBarView.setupBar(this, items, hostText, title, hostColor, showTrans, this.currentTabIndex, count, this.onStartPage, bottomTint, light, barWidth, 2)
        return
    }
    if (items == null || items.size == 0) {
        return
    }
    val newBar = MyBarView(this)
    this.barsState.bottomBarView = newBar
    newBar.setupBar(this, items, hostText, title, hostColor, showTrans, this.currentTabIndex, count, this.onStartPage, bottomTint, light, barWidth, 2)
    this.bottomBarFrame!!.setChild(this.barsState.bottomBarView, 1)
    this.barsState.bottomBarView!!.setListener(WvaBottomBarListener(this))
    if (ZoneSettings.downPos.value == 2) {
        placeDownIcons()
    }
    if (ZoneSettings.youPos.value == 2) {
        placeYouIcons()
    }
}

fun WebViewActivity.showEditShortDialog() {
    if (this.currentPage == null || this.isClosing || isAnyDialogOpen()) {
        return
    }
    val dialogEditShort = this.editShortDialog
    if (dialogEditShort != null) {
        dialogEditShort.dismiss()
        this.editShortDialog = null
    }
    val url = currentPageUrl(true)
    if (TextUtils.isEmpty(url)) {
        return
    }
    val dialog =
        DialogEditShort(this, url, pageTitleOf(this.currentPage, url), 0, WvaEditShortListener(this))
    this.editShortDialog = dialog
    dialog.setOnDismissListener(WvaEditShortDialogDismissed(this))
}

fun WebViewActivity.placeDownIcons() {
    val oldIcon: com.mycompany.app.ui.browser.BarIndicator? = if (ZoneSettings.downPos.value != 1) this.topDownIcon else null
    if (ZoneSettings.downPos.value != 1 && oldIcon != null) {
        removeFromParent(oldIcon)
        this.topDownIcon!!.release()
        this.topDownIcon = null
    }
    val myIconView: com.mycompany.app.ui.browser.BarIndicator? = if (ZoneSettings.downPos.value != 2) this.barsState.bottomDownIcon else null
    if (ZoneSettings.downPos.value != 2 && myIconView != null) {
        removeFromParent(myIconView)
        this.barsState.bottomDownIcon!!.release()
        this.barsState.bottomDownIcon = null
    }
    val webDownView: WebDownView? = if (ZoneSettings.downPos.value != 3) this.imageButton else null
    if (ZoneSettings.downPos.value != 3 && webDownView != null) {
        removeFromParent(webDownView)
        this.imageButton!!.release()
        this.imageButton = null
    }
    val downPos = ZoneSettings.downPos.value
    if (downPos == 1) {
        val uiHandler: Handler? = if (this.barsState.topBarView == null) null else this.mainHandler
        if (this.barsState.topBarView == null || uiHandler == null) {
            return
        }
        if (this.topDownIcon != null) {
            tintDownIcon()
            return
        } else {
            uiHandler.post(WvaShowTopDownButton(this))
            return
        }
    }
    if (downPos == 2) {
        val uiHandler: Handler? = if (this.barsState.bottomBarView == null) null else this.mainHandler
        if (this.barsState.bottomBarView == null || uiHandler == null) {
            return
        }
        if (this.barsState.bottomDownIcon != null) {
            tintDownIcon()
            return
        } else {
            uiHandler.post(WvaShowBottomDownButton(this))
            return
        }
    }
    val handler: Handler? = if (downPos != 3 || this.dialogsState.webBody == null) null else this.mainHandler
    if (downPos != 3 || this.dialogsState.webBody == null || handler == null) {
        return
    }
    val imageButton = this.imageButton
    if (imageButton != null) {
        imageButton.refreshIcon()
    } else {
        handler.post(WvaShowDownNotiPanel(this))
    }
}

fun WebViewActivity.detectSiteFix() {
    if (this.currentPage == null) {
        return
    }
    this.transCheckedUrl = this.dialogsState.pageUrl
    this.barsState.siteFixMode = 0.toByte()
    if (!TextUtils.isEmpty(this.dialogsState.pageHost)) {
        if (this.dialogsState.pageHost!!.endsWith("reddit.com")) {
            this.barsState.siteFixMode = 1.toByte()
        } else if (this.dialogsState.pageHost!!.endsWith("github.com")) {
            this.barsState.siteFixMode = 2.toByte()
        }
    }
    val siteFixMode = this.barsState.siteFixMode
    this.dialogsState.siteFixModeNow = siteFixMode.toInt()
    if (siteFixMode.toInt() == 0) {
        evalJs(
            this.currentPage,
            "(function(){android.onDocHtml(document.documentElement.innerHTML);})();",
            true
        )
        return
    }
    resetTranslate(false)
    this.currentPage!!.prepareReload()
    stopTts()
}

fun WebViewActivity.tintBarFrames(themeColor: Int) {
    val darkBars = isDarkBars(this.onStartPage)
    val myBarFrame = this.topBarFrame
    if (myBarFrame != null) {
        myBarFrame.applyTintFor(themeColor, darkBars, this.onStartPage)
    }
    val bottomBarFrame = this.bottomBarFrame
    if (bottomBarFrame != null) {
        bottomBarFrame.applyTint(darkBars, this.onStartPage)
    }
}
